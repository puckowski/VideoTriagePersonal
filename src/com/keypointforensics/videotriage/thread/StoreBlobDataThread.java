package com.keypointforensics.videotriage.thread;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import com.keypointforensics.videotriage.blob.Blob;
import com.keypointforensics.videotriage.blob.context.BlobContextList;
import com.keypointforensics.videotriage.gui.main.controller.CameraControllerRegistry;
import com.keypointforensics.videotriage.params.WriteRuntimeParams;
import com.keypointforensics.videotriage.sqlite.BlobRecord;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ImageUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.WriteUtils;

public class StoreBlobDataThread extends Thread {

	/*
	 * Author: Daniel Puckowski
	 */
	
	private static final String DEFAULT_MESSAGE_DIGEST_METHOD = "SHA-1";
	private static final String DEFAULT_IMAGE_EXTENSION       = ".jpg";
	private static final float  DEFAULT_IMAGE_WRITE_QUALITY   = 1.0f; 
	
	private final CameraControllerRegistry CONTROLLER_REGISTRY;
	private final String                   CONTROLLER_ID;
	private final BufferedImage            IMAGE;
	private final ArrayList<Blob>          BLOB_LIST;
	private final String                   TIME_STAMP;
	private final int                      MINIMUM_MASS;
	private final String                   IP_ADDRESS;
	private final String                   PORT;
	private final String                   DATABASE_NAME;
	private final BlobContextList          BLOB_CONTEXT_LIST;
	private final String                   VIDEO_FILENAME;
	private final String                   FRAME_INDEX;
	
	public StoreBlobDataThread(final CameraControllerRegistry cameraControllerRegistry,
			final String controllerId, final BufferedImage image, final List<Blob> blobList, 
			final String timeStamp, final int minimumMass, 
			final String ipAddress, final String port,
			final BlobContextList blobContextList, final String videoFilename,
			final String frameIndex) {
		CONTROLLER_REGISTRY = cameraControllerRegistry;
		CONTROLLER_ID       = controllerId;
		IMAGE               = image;
		BLOB_LIST           = (ArrayList<Blob>) blobList;
		TIME_STAMP          = timeStamp;
		MINIMUM_MASS        = minimumMass;
		IP_ADDRESS          = ipAddress;
		PORT                = port;
		DATABASE_NAME       = CameraControllerRegistry.INSTANCE.getController(CONTROLLER_ID).getDatabaseName();
		BLOB_CONTEXT_LIST   = blobContextList;
		VIDEO_FILENAME      = videoFilename;
		FRAME_INDEX         = frameIndex;
	}
	
	@Override
	public void run() {	
		ThreadUtils.addThreadToHandleList("StoreBlob Run", this);
		
		if(BLOB_LIST.isEmpty() == true) {			
			ThreadUtils.removeThreadFromHandleList(this);
			
			return;
		}
		
		Blob blob = null;
		BufferedImage blobImage = null;
		String blobImageHash = null;
		String blobImageFilename = null;
		
		final String folderString = FileUtils.CAPTURES_DIRECTORY;
		
		String folderFromTimeStamp = null;
		
		if(TIME_STAMP != null) {
			folderFromTimeStamp = TIME_STAMP.substring(0, TIME_STAMP.indexOf(" ")); // [0, " ")
		}
		else {
			folderFromTimeStamp = "No Date Recorded";
		}
		
		String databaseNamePrefix = DATABASE_NAME;
		if(databaseNamePrefix.contains(File.separator)) {
			databaseNamePrefix = databaseNamePrefix.substring(databaseNamePrefix.lastIndexOf(File.separator) + 1, databaseNamePrefix.length());
		}
		//if(databaseNamePrefix.contains(".")) {
		//	databaseNamePrefix = databaseNamePrefix.substring(0, databaseNamePrefix.lastIndexOf("."));
		//}
		databaseNamePrefix += File.separator;
		
		String outputFolder = folderString + databaseNamePrefix;
		boolean folderExists = FileUtils.isDirectoryExist(outputFolder);
		
		if(folderExists == false) {			
			final boolean success = FileUtils.createDirectory(outputFolder);

			if(success == false) {				
				ThreadUtils.removeThreadFromHandleList(this);
				
				return;
			}
		}
		
		outputFolder = folderString + databaseNamePrefix + folderFromTimeStamp;
		folderExists = FileUtils.isDirectoryExist(outputFolder);

		if(folderExists == false) {			
			final boolean success = FileUtils.createDirectory(outputFolder);

			if(success == false) {				
				ThreadUtils.removeThreadFromHandleList(this);
				
				return;
			}
		}

		final ArrayList<BlobRecord> blobRecords = new ArrayList<BlobRecord>();
		WriteRuntimeParams writeParams = null;
		
		try {
			writeParams = CONTROLLER_REGISTRY.getWriteParams(CONTROLLER_ID);
		} catch(NullPointerException nullWriteParamsException) {
			//nullWriteParamsException.printStackTrace();
		}
		
		while (Thread.currentThread().isInterrupted() == false) {
			for (int i = 0; i < BLOB_LIST.size(); ++i) {				
				blob = BLOB_LIST.get(i);
				
				if(blob.mass < MINIMUM_MASS) {
					continue;
				}
								
				blobImage = ImageUtils.cropImage(IMAGE, blob.x, blob.y, blob.width, blob.height);

				//writeParams = CONTROLLER_REGISTRY.getWriteParams(CONTROLLER_ID);
				if(writeParams != null && writeParams.getEntropyFilterState() == true) {
					if(ImageUtils.getShannonEntropy(blobImage) < writeParams.getEntropyThreshold()) {
						continue;
					}
				} else if(writeParams == null) {
					if(ImageUtils.getShannonEntropy(blobImage) < WriteRuntimeParams.DEFAULT_ENTROPY_THRESHOLD) {
						continue;
					}
				}
				
				try {
					blobImageHash = getBlobImageSha1Hash(blobImage);
				} catch (NoSuchAlgorithmException e) {					
					blobImageHash = null;
				} catch (Exception exception) {					
					blobImageHash = null;
				}

				if(blobImageHash == null) {					
					blobImageHash = String.valueOf(i);
				} else if(blobImageHash.isEmpty() == true) {					
					blobImageHash = String.valueOf(i);
				}

				if(TIME_STAMP != null) {
					blobImageFilename = outputFolder + FileUtils.FILE_SEPARATOR + TIME_STAMP + "_" + i + "_" + blobImageHash + DEFAULT_IMAGE_EXTENSION;
				} else {
					blobImageFilename = outputFolder + FileUtils.FILE_SEPARATOR + "No Date Recorded" + "_" + i + "_" + blobImageHash + DEFAULT_IMAGE_EXTENSION;
				}
				
				boolean success = ImageUtils.saveBufferedImage(blobImage, blobImageFilename, DEFAULT_IMAGE_WRITE_QUALITY);
				
				try {
					BLOB_CONTEXT_LIST.writeBlobData(VIDEO_FILENAME, FRAME_INDEX, blobImageFilename);
				} catch(NullPointerException blobContextListException) {
					//blobContextListException.printStackTrace();
				}
				
				blobImage = null; 

				if(success == true) {	
					if(TIME_STAMP != null) {
						blobRecords.add(new BlobRecord(blobImageFilename, TIME_STAMP.substring(0, TIME_STAMP.indexOf(" ")), TIME_STAMP.substring(TIME_STAMP.indexOf(" "), TIME_STAMP.length()), IP_ADDRESS, PORT));
					} else {
						blobRecords.add(new BlobRecord(blobImageFilename, "No Date Recorded", "", IP_ADDRESS, PORT));
					}
				}
				
				BLOB_LIST.set(i, null); 
			}
			
			break;
		}
					
		WriteBlobTableThread writeBlobTableThread = new WriteBlobTableThread(blobRecords, DATABASE_NAME);
		WriteUtils.mDatabaseWritePool.execute(writeBlobTableThread);
		
		ThreadUtils.removeThreadFromHandleList(this);
	}
	
	private String getBlobImageSha1Hash(final BufferedImage blobImage) throws NoSuchAlgorithmException, Exception {
		if(blobImage == null) {			
			return null;
		}
		
		byte[] data = ((DataBufferByte) blobImage.getData().getDataBuffer()).getData();
		
		MessageDigest messageDigest = null;
		messageDigest = MessageDigest.getInstance(DEFAULT_MESSAGE_DIGEST_METHOD);
		messageDigest.update(data);
		
        byte[] hash = messageDigest.digest();
        data = null;
        
        return returnHex(hash);
	}

	private String returnHex(final byte[] inBytes) {
		String hexString = "";
		
		for (int i = 0; i < inBytes.length; ++i) { 
			hexString += Integer.toString((inBytes[i] & 0xff) + 0x100, 16).substring(1);
		} 
		
		return hexString;
	}   
}

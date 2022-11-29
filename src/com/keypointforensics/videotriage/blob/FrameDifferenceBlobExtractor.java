package com.keypointforensics.videotriage.blob;

import java.util.*;

import com.keypointforensics.videotriage.gui.main.controller.CameraControllerRegistry;
import com.keypointforensics.videotriage.util.BlobUtils;

import java.awt.*;
import java.awt.image.*;

public class FrameDifferenceBlobExtractor implements BlobExtractor {
	
	/*
	 * Author: Daniel Puckowski
	 */
	
	private final CameraControllerRegistry CONTROLLER_REGISTRY = CameraControllerRegistry.INSTANCE;
	
	private String          mControllerId;
	private ArrayList<Blob> mBlobList;
	private byte[]          mBlobPixelData;
	
	public FrameDifferenceBlobExtractor(final String controllerId) {
		mControllerId = controllerId;
		mBlobList     = new ArrayList<Blob>();
	}

	public String getControllerId() {
		return mControllerId;
	}
	
	public void setControllerId(final String controllerId) {
		mControllerId = controllerId;
	}
	
	public void extractBlobs(final BufferedImage currentImage) {
		final int width = currentImage.getWidth();
		final int height = currentImage.getHeight();
		final Raster raster = currentImage.getData();
		final DataBuffer buffer = raster.getDataBuffer();
		final int type = buffer.getDataType();
		
		if (type != DataBuffer.TYPE_BYTE) {			
			return;
		}
		else if (buffer.getNumBanks() != 1) {			
			return;
		}

		DataBufferByte byteBuffer = (DataBufferByte) buffer;
		byte[] srcData = byteBuffer.getData(0);

		if (width * height * 3 != srcData.length) {			
			return;
		}

		byte[] monoData = new byte[width * height];
		int srcPtr = 0, monoPtr = 0, val = 0;
		
		while (srcPtr < srcData.length) {
			val = ((srcData[srcPtr] & 0xFF) + (srcData[srcPtr + 1] & 0xFF) + (srcData[srcPtr + 2] & 0xFF)) / 3;
			monoData[monoPtr] = (val > 128) ? (byte) 0xFF : 0;

			srcPtr += 3;
			monoPtr += 1;
		}

		mBlobPixelData = new byte[srcData.length];
		
		BinaryFrameDifferenceBlobExtractor finder = new BinaryFrameDifferenceBlobExtractor(width, height, 
			CONTROLLER_REGISTRY.getBlobParams(mControllerId).getExpandBlobs(),
			CONTROLLER_REGISTRY.getBlobParams(mControllerId).getExpansionPercent());
		
		mBlobList = new ArrayList<Blob>();
		finder.detectBlobs(monoData, mBlobPixelData, 0, -1, (byte) 0, mBlobList);
		
		if(CONTROLLER_REGISTRY.getBlobParams(mControllerId).getAttemptToMerge() == true) {	
			mBlobList = (ArrayList<Blob>) BlobUtils.mergeBlobs(mBlobList);
		}
	}

	public BufferedImage drawBlobBounds(final BufferedImage sourceImage, final int minimumBlobMass) {
		if(CONTROLLER_REGISTRY.getBlobParams(mControllerId).getBorderDisplay() == false) {			
			return sourceImage; 
		}
		
		Graphics2D graphics2d = (Graphics2D) sourceImage.getGraphics();
		graphics2d.setColor(CONTROLLER_REGISTRY.getBlobParams(mControllerId).getBorderColor());
		graphics2d.setStroke(new BasicStroke(CONTROLLER_REGISTRY.getBlobParams(mControllerId).getBorderWidthInt()));
		
		for (Blob blob : mBlobList) {
			if(blob == null) {
				
				continue;
			}
			else if (blob.mass < minimumBlobMass) {
				
				continue;
			}

			graphics2d.drawRect(blob.xMin, blob.yMin, (blob.xMax - blob.xMin), (blob.yMax - blob.yMin));
		}
		
		/*
		if(CONTROLLER_REGISTRY.getBlobParams(mControllerId).getAttemptToMerge() == true) {				
			ArrayList<Rectangle> merged = new ArrayList<Rectangle>();

			for (Blob blob : mBlobList) {
				if(blob == null) {
					
					continue;
				}
				else if (blob.mass < minimumBlobMass) {	
					
					continue;
				}

				merged.add(new Rectangle(blob.x, blob.y, blob.width, blob.height));
			}
			
			merged = (ArrayList<Rectangle>) BlobUtils.merge(merged); 
			
			if(merged == null) {
				return sourceImage; 
			}
						
			Graphics2D graphics2d = (Graphics2D) sourceImage.getGraphics();
			graphics2d.setColor(CONTROLLER_REGISTRY.getBlobParams(mControllerId).getBorderColor());				
			graphics2d.setStroke(new BasicStroke(CONTROLLER_REGISTRY.getBlobParams(mControllerId).getBorderWidthInt()));
			
			for (Rectangle rect : merged) {
				graphics2d.drawRect(rect.x, rect.y, rect.width, rect.height);
			}
		}
		else {	
			Graphics2D graphics2d = (Graphics2D) sourceImage.getGraphics();
			graphics2d.setColor(CONTROLLER_REGISTRY.getBlobParams(mControllerId).getBorderColor());
			graphics2d.setStroke(new BasicStroke(CONTROLLER_REGISTRY.getBlobParams(mControllerId).getBorderWidthInt()));
			
			for (Blob blob : mBlobList) {
				if(blob == null) {
					
					continue;
				}
				else if (blob.mass < minimumBlobMass) {
					
					continue;
				}

				graphics2d.drawRect(blob.xMin, blob.yMin, (blob.xMax - blob.xMin), (blob.yMax - blob.yMin));
			}
		}
		*/
		
		return sourceImage;
	}
	
	public ArrayList<Blob> getBlobs() {
		//if(CONTROLLER_REGISTRY.getBlobParams(mControllerId).getAttemptToMerge() == true) {	
		//	return (ArrayList<Blob>) BlobUtils.mergeBlobs(mBlobList);
		//}
		//else {
			return mBlobList;
		//}
	}

	public byte[] getBlobPixelData() {
		return mBlobPixelData;
	}
}

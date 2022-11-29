package com.keypointforensics.videotriage.blob;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.keypointforensics.videotriage.blob.context.BlobContextList;
import com.keypointforensics.videotriage.gui.controller.local.LocalCameraController;
import com.keypointforensics.videotriage.gui.controller.remote.RemoteCameraController;
import com.keypointforensics.videotriage.gui.imagepanel.VideoFeedImagePanel;
import com.keypointforensics.videotriage.gui.main.CameraPreviewPanel;
import com.keypointforensics.videotriage.gui.main.controller.CameraController;
import com.keypointforensics.videotriage.gui.main.controller.CameraControllerRegistry;
import com.keypointforensics.videotriage.image.ByteToImageConverter;
import com.keypointforensics.videotriage.image.FastRgb;
import com.keypointforensics.videotriage.statusbar.StatusBar;
import com.keypointforensics.videotriage.thread.StoreBlobDataThread;
import com.keypointforensics.videotriage.timer.TimeWatch;
import com.keypointforensics.videotriage.util.ImageUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.Utils;
import com.keypointforensics.videotriage.util.WriteUtils;

public class RemoteBlobDetectionThread extends Thread {

	/*
	 * Author: Daniel Puckowski
	 */
	
	private static final int IMAGE_THRESHOLD_BACKGROUND = 255;
	private static final int IMAGE_THRESHOLD_FOREGROUND = 0;
	
	private final CameraControllerRegistry CONTROLLER_REGISTRY     = CameraControllerRegistry.INSTANCE;
	private final AlphaComposite           DEFAULT_ALPHA_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);
	
	private final String                   CONTROLLER_ID;
	private final BufferedImage            BACKGROUND_IMAGE;
	private final FastRgb                  FAST_BACKGROUND_RGB;
	private final BufferedImage            CURRENT_IMAGE;
	private final String                   CURRENT_IMAGE_ABSOLUTE_PATH;
	private final BlobContextList          BLOB_CONTEXT_LIST;
	
	private VideoFeedImagePanel mGraphicsPanel;
	private CameraPreviewPanel  mPreviewPanel;
	private StatusBar           mStatusBar;
	private String              mVideoFilename;
	private String              mFrameIndex;
	
	public RemoteBlobDetectionThread(final String controllerId, final BufferedImage backgroundImage, 
			final FastRgb fastBackgroundRgb, final BufferedImage currentImage, 
			VideoFeedImagePanel graphicsPanel, final CameraPreviewPanel previewPanel, 
			StatusBar statusBar, final BlobContextList blobContextList, 
			final String currentImageAbsolutePath, final String originalVideoFilename) {	
		CONTROLLER_ID               = controllerId;
		BACKGROUND_IMAGE            = backgroundImage;
		FAST_BACKGROUND_RGB         = fastBackgroundRgb;
		CURRENT_IMAGE               = currentImage;
		CURRENT_IMAGE_ABSOLUTE_PATH = currentImageAbsolutePath;
		BLOB_CONTEXT_LIST           = blobContextList;
		
		mGraphicsPanel      = graphicsPanel;
		mPreviewPanel       = previewPanel;
		mStatusBar          = statusBar;
		mVideoFilename      = originalVideoFilename;
		
		determineFrameIndex();
	}
	
	private void determineFrameIndex() {
		mFrameIndex = CURRENT_IMAGE_ABSOLUTE_PATH;
	}
	
	private void calculateFrameDifference(final FastRgb fastBackgroundRgb, final FastRgb fastCurrentRgb) {
		final int threshold = CONTROLLER_REGISTRY.getBackgroundParams(CONTROLLER_ID).getThresholdInt();
		final int backgroundImageWidth = BACKGROUND_IMAGE.getWidth();
		final int backgroundImageHeight = BACKGROUND_IMAGE.getHeight();
		final int diffPrecompute = (255 << 24);
		
		int argb0, argb1, b0, b1, bDiff, diff;
		
		for (int x = 0; x < backgroundImageWidth; ++x) {
			for (int y = 0; y < backgroundImageHeight; ++y) {
				argb0 = fastBackgroundRgb.get(x, y);
				argb1 = fastCurrentRgb.get(x, y);

				b0 = argb0 & 0xFF;
				b1 = argb1 & 0xFF;
				bDiff = Math.abs(b1 - b0);

				if (bDiff < threshold) {
					bDiff = IMAGE_THRESHOLD_BACKGROUND;
				}
				else {
					bDiff = IMAGE_THRESHOLD_FOREGROUND;
				}

				diff = diffPrecompute | (bDiff << 16) | (bDiff << 8) | bDiff;
				CURRENT_IMAGE.setRGB(x, y, diff);
			}
		}
	}
	
	@Override
	public void run() {
		ThreadUtils.addThreadToHandleList("BlobDetectionThread", this);
		
		final int minimumMass = (int) (((double) BACKGROUND_IMAGE.getWidth() * (double) BACKGROUND_IMAGE.getHeight()) * CONTROLLER_REGISTRY.getMassParams(CONTROLLER_ID).getConsiderationThresholdDouble()); // 0.001
		final boolean writeToDatabaseEnabled = CONTROLLER_REGISTRY.getWriteParams(CONTROLLER_ID).getWriteState();
		
		final BlobExtractorFactory blobExtractorFactory = new BlobExtractorFactory();
		final TimeWatch watch = TimeWatch.start();

		while (Thread.currentThread().isInterrupted() == false) {
			BufferedImage currentCopy = ImageUtils.copyBufferedImage(CURRENT_IMAGE);
			BufferedImage writeCopy = null;
			
			if(CONTROLLER_REGISTRY.getSourceParams(CONTROLLER_ID).getDrawOnSourceEnabled() == false) {
				writeCopy = ImageUtils.copyBufferedImage(CURRENT_IMAGE);
			}
			
			FastRgb fastCurrentRgb = new FastRgb(CURRENT_IMAGE);
			
			calculateFrameDifference(FAST_BACKGROUND_RGB, fastCurrentRgb);
			
			BlobExtractor blobExtractor = blobExtractorFactory.getExtractor(BlobExtractorFactory.FRAME_DIFFERENCE_EXTRACTOR, CONTROLLER_ID);
			blobExtractor.extractBlobs(CURRENT_IMAGE);
					
			if(writeToDatabaseEnabled == true) {
				//TODO keep?
				if(BLOB_CONTEXT_LIST.getHasCoordinateWriter() == true) {
					BLOB_CONTEXT_LIST.writeCoordinateData((ArrayList<Blob>) blobExtractor.getBlobs());
				}
				
				//if(CONTROLLER_REGISTRY.getSourceParams(CONTROLLER_ID).getDrawOnSourceEnabled() == true) {
				//	startBlobStoreThread(currentCopy, blobExtractor.getBlobs(), Utils.getTimeStamp(), minimumMass);
				//}
				//else {
					startBlobStoreThread(currentCopy, blobExtractor.getBlobs(), Utils.getTimeStamp(), minimumMass);
				//}
			}
			
			//highlightBlobPixels(currentCopy, blobExtractor);
			
			//mGraphicsPanel.update(blobExtractor.drawBlobBounds(currentCopy, minimumMass));
			//mPreviewPanel.update(currentCopy);
			 
			if(CONTROLLER_REGISTRY.getSourceParams(CONTROLLER_ID).getDrawOnSourceEnabled() == false) {
				highlightBlobPixels(writeCopy, blobExtractor);
						
				mGraphicsPanel.update(blobExtractor.drawBlobBounds(writeCopy, minimumMass));
				mPreviewPanel.update(writeCopy);
			}
			else {
				highlightBlobPixels(currentCopy, blobExtractor);
				
				mGraphicsPanel.update(blobExtractor.drawBlobBounds(currentCopy, minimumMass));
				mPreviewPanel.update(currentCopy);
			}
			
			mStatusBar.incrementFrameCount(watch.time(TimeUnit.MILLISECONDS)); // Another frame processed.
			
			break;
		}
				
		ThreadUtils.removeThreadFromHandleList(this);
	}

	public void startBlobStoreThread(final BufferedImage currentImage, final List<Blob> blobList, final String timeStamp, final int minimumMass) {
		CameraController cameraController = CONTROLLER_REGISTRY.getController(CONTROLLER_ID);
		
		if(cameraController instanceof RemoteCameraController) {
			RemoteCameraController remoteCameraController = (RemoteCameraController) cameraController;
			cameraController = null;
			
			final String ipString = remoteCameraController.getIpString();
			final String portString = remoteCameraController.getPortString();
			
			StoreBlobDataThread storeBlobDataThread = new StoreBlobDataThread(CONTROLLER_REGISTRY, CONTROLLER_ID, 
					currentImage, blobList, timeStamp, minimumMass, ipString, portString, BLOB_CONTEXT_LIST, mVideoFilename, mFrameIndex);
			WriteUtils.mImageWritePool.execute(storeBlobDataThread);
		}
		else if(cameraController instanceof LocalCameraController) {
			LocalCameraController localCameraController = (LocalCameraController) cameraController;
			cameraController = null;

			final String ipString = localCameraController.getLocalFileField().getText();//.toString();
			final String portString = "";
			
			StoreBlobDataThread storeBlobDataThread = new StoreBlobDataThread(CONTROLLER_REGISTRY, CONTROLLER_ID, 
					currentImage, blobList, timeStamp, minimumMass, ipString, portString, BLOB_CONTEXT_LIST, mVideoFilename, mFrameIndex);
			WriteUtils.mImageWritePool.execute(storeBlobDataThread);
		}
	}
	
	public void highlightBlobPixels(final BufferedImage currentCopy, BlobExtractor blobExtractor) {
		Graphics2D graphics2d = (Graphics2D) currentCopy.getGraphics();
		graphics2d.setComposite(DEFAULT_ALPHA_COMPOSITE);
		
		if(blobExtractor instanceof FrameDifferenceBlobExtractor) {			
			FrameDifferenceBlobExtractor frameDifferenceExtractor = (FrameDifferenceBlobExtractor) blobExtractor;
			ByteToImageConverter byteToImageConverter = new ByteToImageConverter(CONTROLLER_ID, currentCopy.getWidth(), currentCopy.getHeight(), frameDifferenceExtractor.getBlobPixelData());
			
			graphics2d.drawImage(byteToImageConverter.getImage(), 0, 0, null);
			
			byteToImageConverter = null;
		}
	}
}

package com.keypointforensics.videotriage.thread;

import java.awt.image.BufferedImage;

import com.keypointforensics.videotriage.gui.imagepanel.VideoFeedImagePanel;
import com.keypointforensics.videotriage.gui.main.CameraPreviewPanel;
import com.keypointforensics.videotriage.local.LocalViewProcessor;
import com.keypointforensics.videotriage.util.ThreadUtils;

public class StopLocalViewThread extends Thread {

	/*
	 * Author: Daniel Puckowski
	 */
	 
	private final LocalViewProcessor REMOTE_VIEW_THREAD;
	private final VideoFeedImagePanel GRAPHICS_PANEL;
	private final CameraPreviewPanel  PREVIEW_PANEL;
	
	public StopLocalViewThread(final LocalViewProcessor localViewProcessor, final VideoFeedImagePanel viewFeedImagePanel, final CameraPreviewPanel cameraPreviewPanel) {
		REMOTE_VIEW_THREAD = localViewProcessor;
		GRAPHICS_PANEL     = viewFeedImagePanel;
		PREVIEW_PANEL      = cameraPreviewPanel;
	}
	
	@Override
	public void run() {
		ThreadUtils.addThreadToHandleList("StopLocal Run", this);
		
		if(REMOTE_VIEW_THREAD != null) {
			REMOTE_VIEW_THREAD.interrupt();
		}
		
		try {
			Thread.sleep(ThreadUtils.DEFAULT_BLOCK_MILLIS);
		} catch (Exception exception) {

		}
				
		final BufferedImage currentImage = null;
		
		if(GRAPHICS_PANEL != null) {
			GRAPHICS_PANEL.update(currentImage);
		}
		if(PREVIEW_PANEL != null) {
			PREVIEW_PANEL.update(currentImage); 
		}
		
		ThreadUtils.removeThreadFromHandleList(this);
	}
}

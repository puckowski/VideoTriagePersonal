package com.keypointforensics.videotriage.local;

import com.keypointforensics.videotriage.gui.imagepanel.VideoFeedImagePanel;
import com.keypointforensics.videotriage.gui.main.CameraPreviewPanel;
import com.keypointforensics.videotriage.statusbar.StatusBar;

public interface LocalViewProcessor {

	/*
	 * Author: Daniel Puckowski
	 */
	
	public boolean isRunning();
	public void setRunning(final boolean isRunning);
	public String getIp();
	public void setIp(final String ip);
	public String getPort();
	public void setPort(final String port);
	public VideoFeedImagePanel getGraphicsPanel();
	public void setGraphicsPanel(final VideoFeedImagePanel graphicsPanel);
	public CameraPreviewPanel getPreviewPanel();
	public void setOriginalVideoFilename(final String originalVideoFilename);
	
	public String getControllerId();
	public void   setControllerId(final String controllerId);
	
	public void attachStatusBar(final StatusBar statusBar);
	public void attachPreviewPanel(final CameraPreviewPanel previewPanel);
	
	public void run();
	public void start();
	public void interrupt();
	public void resetBackground();
	
	public String getLastImageHash();
	
	public int getProgressTarget();
	public int getProgress();
	public boolean isProgressIndeterminate();
}

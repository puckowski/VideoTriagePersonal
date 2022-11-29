package com.keypointforensics.videotriage.processor;

import java.awt.image.BufferedImage;

import com.keypointforensics.videotriage.blob.context.BlobContextList;
import com.keypointforensics.videotriage.gui.imagepanel.VideoFeedImagePanel;
import com.keypointforensics.videotriage.gui.main.CameraPreviewPanel;
import com.keypointforensics.videotriage.gui.main.controller.CameraControllerRegistry;
import com.keypointforensics.videotriage.image.FastRgb;
import com.keypointforensics.videotriage.statusbar.StatusBar;
import com.keypointforensics.videotriage.statusbar.StatusBarBundle;

public class BaseProcessor extends Thread {

	/*
	 * Author: Daniel Puckowski
	 */
	
	protected final CameraControllerRegistry CONTROLLER_REGISTRY = CameraControllerRegistry.INSTANCE;
	protected final BlobContextList          BLOB_CONTEXT_LIST;
	
	protected String                  mControllerId;
	protected String                  mIp;
	protected String                  mPort;
	protected VideoFeedImagePanel     mGraphicsPanel;
	protected CameraPreviewPanel      mPreviewPanel;
	protected boolean                 mResetBackground;
	protected BufferedImage           mBackgroundImage;
	protected FastRgb                 mFastBackgroundRgb;
	protected StatusBar               mStatusBar;
	protected StatusBarBundle         mStatusBarBundle;
	protected boolean                 mRunning;
	protected String                  mLastImageHash;
	
	public BaseProcessor(final BlobContextList blobContextList) {
		BLOB_CONTEXT_LIST = blobContextList;
	}
	
	public BaseProcessor(final String controllerId, final String ip, final String port, 
			final VideoFeedImagePanel graphicsPanel, final BlobContextList blobContextList) {
		BLOB_CONTEXT_LIST = blobContextList;
		
		mControllerId     = controllerId;
		mIp               = ip;
		mPort             = port;
		mGraphicsPanel    = graphicsPanel;
		mLastImageHash    = null;
		
		int lastIndexOfSlash = mIp.lastIndexOf("\\");
		if(lastIndexOfSlash > 0) {
			mIp = mIp.substring(lastIndexOfSlash + 1, mIp.length());
		}
	}
	
	public boolean isRunning() {
		return mRunning;
	}
		
	public String getIp() {
		return mIp;
	}
	
	public void setIp(final String ip) {
		mIp = ip;
	}
	
	public String getPort() {
		return mPort;
	}
	
	public void setPort(final String port) {
		mPort = port;
	}
	
	public VideoFeedImagePanel getGraphicsPanel() {
		return mGraphicsPanel;
	}
	
	public void setGraphicsPanel(final VideoFeedImagePanel graphicsPanel) {
		mGraphicsPanel = graphicsPanel;
	}
	
	public CameraPreviewPanel getPreviewPanel() {
		return mPreviewPanel;
	}
	
	public String getLastImageHash() {
		return mLastImageHash;
	}

	public void resetBackground() {
		mResetBackground = true;
	}
	
	public String getControllerId() {
		return mControllerId;
	}
	
	public void setControllerId(final String controllerId) {
		mControllerId = controllerId;
	}
	
	public void attachStatusBar(final StatusBar statusBar) {	
		mStatusBar = statusBar;
		mStatusBarBundle = new StatusBarBundle(mGraphicsPanel, mIp, mPort);
	}
	
	public void attachPreviewPanel(final CameraPreviewPanel previewPanel) {
		mPreviewPanel = previewPanel;
	}
	
	public void setRunning(final boolean isRunning) {
		mRunning = isRunning;
	}
	
	@Override
	public void run() {		
		mRunning = false; 
	}
}

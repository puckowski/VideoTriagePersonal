package com.keypointforensics.videotriage.remote;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.keypointforensics.videotriage.gui.imagepanel.VideoFeedImagePanel;
import com.keypointforensics.videotriage.gui.main.CameraPreviewPanel;
import com.keypointforensics.videotriage.gui.main.controller.CameraControllerRegistry;
import com.keypointforensics.videotriage.params.EBackgroundMethod;
import com.keypointforensics.videotriage.pool.SilentRejectedExecutionHandler;
import com.keypointforensics.videotriage.statusbar.StatusBar;
import com.keypointforensics.videotriage.statusbar.StatusBarBundle;
import com.keypointforensics.videotriage.util.SystemUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;

public class BaseControllerViewProcessor extends Thread { 

	/*
	 * Author: Daniel Puckowski
	 */
	
	private final CameraControllerRegistry CONTROLLER_REGISTRY = CameraControllerRegistry.INSTANCE;
	
	private String                  mControllerId;
	private String                  mIp;
	private String                  mPort;
	private VideoFeedImagePanel     mGraphicsPanel;
	private CameraPreviewPanel      mPreviewPanel;
	private boolean                 mResetBackground;
	private BufferedImage           mBackgroundImage;
	private StatusBar               mStatusBar;
	private StatusBarBundle         mStatusBarBundle;
	private boolean                 mRunning;
	private String                  mLastImageHash;
	private EBackgroundMethod       mBackgroundMethod;
	
	public BaseControllerViewProcessor() {
	}
	
	public BaseControllerViewProcessor(final String controllerId, final String ip, final String port, final VideoFeedImagePanel graphicsPanel) {
		mControllerId            = controllerId;
		mIp                      = ip;
		
		if(mIp.endsWith(File.separator) == false) {
			mIp += File.separator;
		}
		
		mPort                    = port;
		mGraphicsPanel           = graphicsPanel;
		mLastImageHash           = null;
		mBackgroundMethod        = CONTROLLER_REGISTRY.getController(mControllerId).getBackgroundParams().getBackgroundMethod();
	}
	
	public boolean isRunning() {
		return mRunning;
	}
		
	public String getIp() {
		return mIp;
	}
	
	public void setIp(final String ip) {
		mIp = ip;
		
		if(mIp.endsWith(File.separator) == false) {
			mIp += File.separator;
		}
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
		if(statusBar == null) {			
			return;
		}
		
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
		ThreadUtils.addThreadToHandleList("BaseCont Run", this);
				
		mRunning = true;
				
		SilentRejectedExecutionHandler rejectionHandler = new SilentRejectedExecutionHandler(true);
		ThreadFactory threadFactory = Executors.defaultThreadFactory();

		final int numberOfCores = SystemUtils.getNumberOfSystemCores();
		ThreadPoolExecutor executorPool = new ThreadPoolExecutor(numberOfCores, (numberOfCores * 2), 100, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(100), threadFactory, rejectionHandler);

		BufferedImage currentImage = null;

		ThreadUtils.blockThread(500, "Pause in BaseControllerViewProcessor");
		
		while (Thread.currentThread().isInterrupted() == false && mRunning) { 			
			Thread.yield();
		}		

		executorPool.shutdown();
		executorPool.shutdownNow();
		while(executorPool.getActiveCount() > 0);
		
		ThreadUtils.blockThread(500, "Pause in BaseControllerViewProcessor");
		
		currentImage = null;	
		mGraphicsPanel.update(currentImage);
		mPreviewPanel.update(currentImage);  
		
		mRunning = false;
		
		ThreadUtils.removeThreadFromHandleList(this);
	}
}

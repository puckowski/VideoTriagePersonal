package com.keypointforensics.videotriage.remote;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.ds.ipcam.IpCamDevice;
import com.github.sarxos.webcam.ds.ipcam.IpCamDeviceRegistry;
import com.github.sarxos.webcam.ds.ipcam.IpCamDriver;
import com.github.sarxos.webcam.ds.ipcam.IpCamMode;
import com.keypointforensics.videotriage.blob.RemoteBlobDetectionThread;
import com.keypointforensics.videotriage.blob.context.BlobContextList;
import com.keypointforensics.videotriage.gui.imagepanel.VideoFeedImagePanel;
import com.keypointforensics.videotriage.gui.main.CameraPreviewPanel;
import com.keypointforensics.videotriage.gui.main.NoSignalDisplayImageSingleton;
import com.keypointforensics.videotriage.gui.main.controller.CameraControllerRegistry;
import com.keypointforensics.videotriage.image.FastRgb;
import com.keypointforensics.videotriage.network.NetworkStatusThread;
import com.keypointforensics.videotriage.pool.SilentRejectedExecutionHandler;
import com.keypointforensics.videotriage.statusbar.StatusBar;
import com.keypointforensics.videotriage.statusbar.StatusBarBundle;
import com.keypointforensics.videotriage.util.ColorUtils;
import com.keypointforensics.videotriage.util.ImageUtils;
import com.keypointforensics.videotriage.util.SystemUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;

public class GenericRemoteViewProcessor extends Thread implements RemoteViewProcessor {

	/*
	 * Author: Daniel Puckowski
	 */
	
	private NoSignalDisplayImageSingleton mNoSignalDisplayImage = NoSignalDisplayImageSingleton.INSTANCE;
	
	private final CameraControllerRegistry CONTROLLER_REGISTRY = CameraControllerRegistry.INSTANCE;
	private final BlobContextList          BLOB_CONTEXT_LIST;
	private final String                   HTTP_PREFIX_STRING  = "http://";
	
	private String                  mControllerId;
	private String                  mIp;
	private String                  mPort;
	private VideoFeedImagePanel     mGraphicsPanel;
	private CameraPreviewPanel      mPreviewPanel;
	private RemoteViewAuthenticator mAuthenticator;
	private String                  mRemoteUrlString;
	private boolean                 mResetBackground;
	private BufferedImage           mBackgroundImage;
	private FastRgb                 mFastBackgroundRgb;
	private StatusBar               mStatusBar;
	private StatusBarBundle         mStatusBarBundle;
	private NetworkStatusThread     mNetworkStatusThread;
	private boolean                 mRunning;
	private String                  mLastImageHash;
	
	private IpCamDevice             mMjepgIpCamDevice;
	
	//static {
	//	Webcam.setDriver(new IpCamDriver());
	//}
	
	public GenericRemoteViewProcessor(final BlobContextList blobContextList) {
		BLOB_CONTEXT_LIST = blobContextList;
		
		Webcam.setDriver(new IpCamDriver());
	}
	
	public GenericRemoteViewProcessor(final String controllerId, final String ip, final String port, final VideoFeedImagePanel graphicsPanel,
			final RemoteViewAuthenticator authenticator, final BlobContextList blobContextList) {
		BLOB_CONTEXT_LIST = blobContextList;

		mControllerId     = controllerId;
		mIp               = ip;
		mPort             = port;
		mGraphicsPanel    = graphicsPanel;
		mAuthenticator    = authenticator;
		mLastImageHash    = null;
		
		mMjepgIpCamDevice = null;
		
		Webcam.setDriver(new IpCamDriver());
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
	
	public RemoteViewAuthenticator getAuthenticator() {
		return mAuthenticator;
	}
	
	public void setAuthenticator(final RemoteViewAuthenticator authenticator) {
		mAuthenticator = authenticator;
	}
	
	public CameraPreviewPanel getPreviewPanel() {
		return mPreviewPanel;
	}
	
	public String getRemoteUrlString() {
		if(mRemoteUrlString == null) {			
			mRemoteUrlString = mIp;
			
			if(mRemoteUrlString.startsWith(HTTP_PREFIX_STRING) == false) {
				mRemoteUrlString = HTTP_PREFIX_STRING + mRemoteUrlString;
			}		
		}
		
		return mRemoteUrlString;
	}
	
	public String getLastImageHash() {
		return mLastImageHash;
	}

	public void resetBackground() {
		mResetBackground = true;
	}
	
	public void startNetworkThreads(final URL url) {		
		mNetworkStatusThread = new NetworkStatusThread(url);
		mNetworkStatusThread.start();
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
	
	private boolean isMjpegStream() {
		String urlString = getRemoteUrlString();
		
		if(urlString.toLowerCase().contains("mjpg") == true
				|| urlString.toLowerCase().contains("mjpeg") == true) {
			return true;
		} else {
			return false;
		}
	}
	
	private BufferedImage loadNextImage(URL url) throws IOException {
		if(mMjepgIpCamDevice == null || isMjpegStream() == false) {
			return ImageIO.read(url);
		} else {
			BufferedImage mjpegImage;
			
			do {
				mjpegImage = mMjepgIpCamDevice.getImage();
				
				//try {
				//	Thread.sleep(10);
				//} catch (InterruptedException interruptedException) {
				//	interruptedException.printStackTrace();
				//}
			} while (ImageUtils.getShannonEntropy(mjpegImage) < 5.0);
			
			return mjpegImage;
		}
	}
	
	@Override
	public void run() {
		ThreadUtils.addThreadToHandleList("GenRem Run", this);
		
		//TODO keep?
		if(CONTROLLER_REGISTRY.getBlobParams(mControllerId).getSaveBlobCoordinates() == true) {
			try {
				BLOB_CONTEXT_LIST.initializeCoordinateWriter();
			} catch (IOException ioException) {
				//ioException.printStackTrace();
			}
		} else {
			BLOB_CONTEXT_LIST.setHasCoordinateWriter(false);
		}
		
		Authenticator.setDefault(mAuthenticator);
		URL url = null;
		String urlString = getRemoteUrlString();
		
		try {
			url = new URL(urlString);
		} catch (MalformedURLException malformedUrlException) {			
			ThreadUtils.removeThreadFromHandleList(this);
			
			return; 
		}
		
		if(isMjpegStream() == true) {
			mMjepgIpCamDevice = new IpCamDevice("MjpegCamera", url, IpCamMode.PUSH);
			IpCamDeviceRegistry.register(mMjepgIpCamDevice);
			
			WebcamPanel webcamPanel = new WebcamPanel(Webcam.getDefault());
			webcamPanel = null;
		} else {
			mMjepgIpCamDevice = null;
		}
		
		urlString = null;
		mRunning = true;
		
		startNetworkThreads(url);
		
		SilentRejectedExecutionHandler rejectionHandler = new SilentRejectedExecutionHandler(true);
		ThreadFactory threadFactory = Executors.defaultThreadFactory();

		final int numberOfCores = SystemUtils.getNumberOfSystemCores();
		ThreadPoolExecutor executorPool = new ThreadPoolExecutor(numberOfCores, (numberOfCores * 2), 100, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(100), threadFactory, rejectionHandler);

		BufferedImage currentImage = null;
		boolean wroteFrameData = false;
		
		while (Thread.currentThread().isInterrupted() == false && mNetworkStatusThread.isOk() == true && mRunning) {
			try {
				if (mBackgroundImage == null) {
					//mBackgroundImage = ImageIO.read(url);
					mBackgroundImage = loadNextImage(url);
					
					if(mBackgroundImage == null) {
						mGraphicsPanel.update(mNoSignalDisplayImage.getImage());
						continue;
					}
					
					mFastBackgroundRgb = new FastRgb(mBackgroundImage);
					
					if(BLOB_CONTEXT_LIST.getHasCoordinateWriter() == true && wroteFrameData == false) {
						BLOB_CONTEXT_LIST.writeFrameData(mBackgroundImage.getWidth(), mBackgroundImage.getHeight());
						wroteFrameData = true;
					}
				}
				else if (mResetBackground == true) {
					mResetBackground = false;
					//mBackgroundImage = ImageIO.read(url);
					mBackgroundImage = loadNextImage(url);
					mFastBackgroundRgb = new FastRgb(mBackgroundImage);
				}
				else {
					//currentImage = ImageIO.read(url);
					currentImage = loadNextImage(url);
					BufferedImage im = ImageUtils.copyBufferedImage(currentImage);
					if(mBackgroundImage.getWidth() != currentImage.getWidth()) {
	
						mBackgroundImage = null;
						continue;
					}
					else if(mBackgroundImage.getHeight() != currentImage.getHeight()) {
						mBackgroundImage = null;
						continue;
					}
				
					executorPool.execute(new RemoteBlobDetectionThread(mControllerId, mBackgroundImage, mFastBackgroundRgb, currentImage, mGraphicsPanel, mPreviewPanel, mStatusBar, BLOB_CONTEXT_LIST, url.toString(), mIp));
					
					if(CONTROLLER_REGISTRY.getBackgroundParams(mControllerId).getAutoUpdate() == true) {
						FastRgb rgbOne = new FastRgb(im);
						FastRgb rgbTwo = new FastRgb(mBackgroundImage);
						int blendedColorArgb = -1;
						
						for (int x = 0; x < rgbOne.getWidth(); ++x) {
							for (int y = 0; y < rgbOne.getHeight(); ++y) {
								blendedColorArgb = ColorUtils.blendFast(rgbOne.get(x, y), rgbTwo.get(x, y));
								
								mBackgroundImage.setRGB(x, y, blendedColorArgb);
							}
						}
						
						mFastBackgroundRgb = new FastRgb(mBackgroundImage);
					}
				}
				
				currentImage = null;
				mStatusBar.updateStatusBar(mStatusBarBundle);
			} catch (Exception generalException) {				
				mGraphicsPanel.update(mNoSignalDisplayImage.getImage());
			}
		}

		executorPool.shutdown();
		executorPool.shutdownNow();
		while(executorPool.getActiveCount() > 0);
		
		mNetworkStatusThread.interrupt();
		
		currentImage = null;	
		mGraphicsPanel.update(currentImage);
		mPreviewPanel.update(currentImage); 
		
		mRunning = false;
		
		ThreadUtils.removeThreadFromHandleList(this);
	}
}

package com.keypointforensics.videotriage.local;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.imageio.ImageIO;

import com.keypointforensics.videotriage.blob.Blob;
import com.keypointforensics.videotriage.blob.BlobDetectionThread;
import com.keypointforensics.videotriage.blob.context.BlobContextList;
import com.keypointforensics.videotriage.gui.imagepanel.VideoFeedImagePanel;
import com.keypointforensics.videotriage.image.FastRgb;
import com.keypointforensics.videotriage.legacy.WindowsVideoFrameExtractorLegacy;
import com.keypointforensics.videotriage.pool.SilentRejectedExecutionHandler;
import com.keypointforensics.videotriage.processor.BaseProcessor;
import com.keypointforensics.videotriage.staticparams.LastFrameRuntimeParams;
import com.keypointforensics.videotriage.thread.ExtractVideoFrameThread;
import com.keypointforensics.videotriage.util.ColorUtils;
import com.keypointforensics.videotriage.util.SystemUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;

public class LocalMeanFilterProcessor extends BaseProcessor implements LocalViewProcessor {

	/*
	 * Author: Daniel Puckowski
	 */
		
	private int                      mFrameCount;
	private int                      currentFileIndex;
	private String                   mCurrentFilePrefix;
	private String                   mCreationDateString;
	private String                   mOriginalVideoFilename;
	private ExtractVideoFrameThread  mExtractVideoFrameThread;
		
	private int mProgressTarget;
	private int mCurrentProgress;
	private boolean mProgressIndeterminate;
	private boolean mLastIndexGreaterThanCount;
	
	//public LocalMeanFilterProcessor(final int preliminaryProgressTarget, final BlobContextList blobContextList) {
	public LocalMeanFilterProcessor(final BlobContextList blobContextList) {
		super(blobContextList);
				
		mProgressTarget          = 0; //= preliminaryProgressTarget;
		mFrameCount              = 0;
		currentFileIndex         = 1; 
		mExtractVideoFrameThread = null;
		mCurrentFilePrefix       = mIp + "image-";
	}
	
	public LocalMeanFilterProcessor(final String controllerId, final String ip, final String port, 
			final VideoFeedImagePanel graphicsPanel, final BlobContextList blobContextList) {
		super(controllerId, ip, port, graphicsPanel, blobContextList);
				
		mProgressTarget          = 0;
		mFrameCount              = 0;
		currentFileIndex         = 1; 
		mExtractVideoFrameThread = null;
		mCurrentFilePrefix       = mIp + "image-";
	}

	@Override
	public void setOriginalVideoFilename(final String originalVideoFilename) {
		mOriginalVideoFilename = originalVideoFilename;
	}
	
	public void setCreationDateString(final String videoCreationDateString) {
		mCreationDateString = videoCreationDateString;
	}
	
	@Override
	public void setIp(final String ip) {
		mIp = ip;
		
		if(mIp.endsWith(File.separator) == false) {
			mIp += File.separator;
		}
		
		mCurrentFilePrefix = mIp + "image-";
	}
	
	public void setExtractVideoFrameThread(final ExtractVideoFrameThread extractVideoFrameThread) {
		mExtractVideoFrameThread = extractVideoFrameThread;
	}
	
	private String getCurrentFileString(final int currentFileIndex) {
		return mCurrentFilePrefix + String.format("%07d", currentFileIndex) + ".jpeg";
	}
	
	private void determineLastFrameIndex(final File absoluteExtractDirectory) {
		if(mLastIndexGreaterThanCount == true) {
			return;
		}
		
		String[] absoluteExtractDirectoryList = absoluteExtractDirectory.list();
		mFrameCount = absoluteExtractDirectoryList.length;
		//TODO
		mProgressTarget = mFrameCount;//absoluteExtractDirectory.list().length;
		
		if(mFrameCount == 0) {
			return;
		}
		
		//
		String lastDirectoryItem = absoluteExtractDirectoryList[absoluteExtractDirectoryList.length - 1];
		
		if(lastDirectoryItem.contains("-") == true && lastDirectoryItem.contains(".") == true) {
			String lastIndexString = lastDirectoryItem.substring(lastDirectoryItem.lastIndexOf("-") + 1, lastDirectoryItem.lastIndexOf("."));
			Integer lastIndex = Integer.parseInt(lastIndexString);
						
			if(lastIndex > mFrameCount) {
				mFrameCount = lastIndex;
				//TODO mprogtarget redundant?
				mProgressTarget = mFrameCount;
				
				mLastIndexGreaterThanCount = true;
			}
		}
		//
	}
	
	private void runExhaustive(final File absoluteExtractDirectory) {
		determineLastFrameIndex(absoluteExtractDirectory);
		
		final String absoluteExtractDirectoryString = absoluteExtractDirectory.getAbsolutePath();
		
		if(LastFrameRuntimeParams.lastFrameProcessedMap.containsKey(mIp) == true) {
			currentFileIndex = LastFrameRuntimeParams.lastFrameProcessedMap.get(mIp);
		} else {
			LastFrameRuntimeParams.lastFrameProcessedMap.put(mIp, currentFileIndex);
		}
		
		SilentRejectedExecutionHandler rejectionHandler = new SilentRejectedExecutionHandler(true);
		ThreadFactory threadFactory = Executors.defaultThreadFactory();

		final int numberOfCores = SystemUtils.getNumberOfSystemCores();
		ThreadPoolExecutor executorPool = new ThreadPoolExecutor(numberOfCores * 2, (numberOfCores * 4), 100, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(100), threadFactory, rejectionHandler);

		BufferedImage currentImage = null;
		String currentFileString = "";
		File currentFile = null;
		FastRgb rgbOne = null;
		FastRgb rgbTwo = null;
		int blendedColorArgb;
		int x, y;
		boolean wroteFrameData = false;
		
		do { 
			try {
				if (mBackgroundImage == null) {
					currentFileString = getCurrentFileString(currentFileIndex);
					currentFileIndex++;
					
					currentFile = new File(currentFileString);
					while(currentFile.exists() == false)
						Thread.yield();
					
					mBackgroundImage = ImageIO.read(currentFile);
					mFastBackgroundRgb = new FastRgb(mBackgroundImage);
					
					if(BLOB_CONTEXT_LIST.getHasCoordinateWriter() == true && wroteFrameData == false) {
						BLOB_CONTEXT_LIST.writeFrameData(mBackgroundImage.getWidth(), mBackgroundImage.getHeight());
						wroteFrameData = true;
					}
				}
				else if (mResetBackground == true) {
					mResetBackground = false;
							
					currentFileString = getCurrentFileString(currentFileIndex);
					currentFileIndex++;
					
					currentFile = new File(currentFileString);
					while(currentFile.exists() == false)
						Thread.yield();
					
					mBackgroundImage = ImageIO.read(currentFile);
					mFastBackgroundRgb = new FastRgb(mBackgroundImage);
				}
				else {
					currentFileString = getCurrentFileString(currentFileIndex);
					currentFileIndex++;
					
					currentFile = new File(currentFileString);
					while(currentFile.exists() == false)
						Thread.yield();
					
					currentImage = ImageIO.read(currentFile);
					
					if(CONTROLLER_REGISTRY.getBackgroundParams(mControllerId).getAutoUpdate() == true) {
						rgbOne = new FastRgb(currentImage);
						rgbTwo = new FastRgb(mBackgroundImage);
						blendedColorArgb = -1;
						
						for (x = 0; x < rgbOne.getWidth(); ++x) {
							for (y = 0; y < rgbOne.getHeight(); ++y) {
								blendedColorArgb = ColorUtils.blendFast(rgbOne.get(x, y), rgbTwo.get(x, y));//.synchronizedBlendFast(rgbOne.get(x, y), rgbTwo.get(x, y));
								
								mBackgroundImage.setRGB(x, y, blendedColorArgb);
							}
						}
						
						mFastBackgroundRgb = new FastRgb(mBackgroundImage);
					}	
					
					executorPool.execute(new BlobDetectionThread(mControllerId, mBackgroundImage, mFastBackgroundRgb, currentImage, mGraphicsPanel, mPreviewPanel, 
							mStatusBar, BLOB_CONTEXT_LIST, currentFileString, mOriginalVideoFilename, mCreationDateString));			
				}
				
				currentImage = null;
				mStatusBar.updateStatusBar(mStatusBarBundle);
			} catch (Exception generalException) {				
				//TODO make general exception?
				//ioException.printStackTrace();
				//generalException.printStackTrace();
			}
			
			determineLastFrameIndex(absoluteExtractDirectory);

			if(currentFileIndex >= mFrameCount) {
				if(currentFileIndex >= mProgressTarget) {
					mProgressIndeterminate = true;
				}
			}
			else {
				mProgressIndeterminate = false;
				mCurrentProgress = currentFileIndex;	
			}
			
			if(currentFileIndex >= mFrameCount) {
				ExecutorService service = Executors.newSingleThreadExecutor();
				Thread t = null;
			    Future<?> f = null;
			    
				try {
				    t = new Thread() {
				        @Override
				        public void run() {	
				    		ThreadUtils.addThreadToHandleList("LocMean Count", this);

				    		while(Thread.interrupted() == false && currentFileIndex >= mFrameCount 
				    			&& ExtractVideoFrameThread.mExtractionSet.contains(absoluteExtractDirectoryString) == true)
				    		{
				    			ThreadUtils.blockThread(250, "Brief wait to confirm that there are in fact no more frames to process.");
								
								mFrameCount = absoluteExtractDirectory.list().length;
							
								if(currentFileIndex >= mProgressTarget) {
									mStatusBar.clear(mStatusBarBundle);
									
									mGraphicsPanel.clear(); 
									mPreviewPanel.clear();  
								}
				    		}
				    		
				    		ThreadUtils.removeThreadFromHandleList(this);
				        }
				    };
	
				    f = service.submit(t);
	
				    f.get(30, TimeUnit.SECONDS); 
				}
				catch (final InterruptedException e) {

				}
				catch (final TimeoutException e) {

				}
				catch (final ExecutionException e) {
					
				}
				finally {
					t.interrupt();
				    f.cancel(true);
				    
				    service.shutdown();
				    service.shutdownNow();
					
				    if(currentFileIndex >= mFrameCount) {
				    	break;
				    }
				}
			}
			
			LastFrameRuntimeParams.lastFrameProcessedMap.replace(mIp, currentFileIndex);
		}
		while (Thread.currentThread().isInterrupted() == false && currentFileIndex < mFrameCount && mRunning);
		
		mStatusBar.clear(mStatusBarBundle);
		
		currentImage = null;	
		mGraphicsPanel.update(currentImage);
		mPreviewPanel.update(currentImage);  
		
		//mLastNFrames.clear();
		//mLastNFrames = null;
		
		executorPool.shutdown();
		executorPool.shutdownNow();
		while(executorPool.getActiveCount() > 0);
	}
	
	private void runKeyFinished(final File absoluteExtractDirectory) {
		determineLastFrameIndex(absoluteExtractDirectory);
		
		final String absoluteExtractDirectoryString = absoluteExtractDirectory.getAbsolutePath();
		
		if(LastFrameRuntimeParams.lastFrameProcessedMap.containsKey(mIp) == true) {
			currentFileIndex = LastFrameRuntimeParams.lastFrameProcessedMap.get(mIp);
		} else {
			LastFrameRuntimeParams.lastFrameProcessedMap.put(mIp, currentFileIndex);
		}
		
		SilentRejectedExecutionHandler rejectionHandler = new SilentRejectedExecutionHandler(true);
		ThreadFactory threadFactory = Executors.defaultThreadFactory();

		final int numberOfCores = SystemUtils.getNumberOfSystemCores();
		ThreadPoolExecutor executorPool = new ThreadPoolExecutor(numberOfCores * 2, (numberOfCores * 4), 100, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(100), threadFactory, rejectionHandler);

		BufferedImage currentImage = null;
		String currentFileString = "";
		File currentFile = null;
		FastRgb rgbOne = null;
		FastRgb rgbTwo = null;
		int blendedColorArgb;
		int x, y;
		boolean wroteFrameData = false;
		
		do { 
			try {
				if (mBackgroundImage == null) {
					currentFileString = getCurrentFileString(currentFileIndex);
					currentFileIndex++;
					
					currentFile = new File(currentFileString);
					if(currentFile.exists() == false)
						continue;
					
					mBackgroundImage = ImageIO.read(currentFile);
					mFastBackgroundRgb = new FastRgb(mBackgroundImage);
					
					if(BLOB_CONTEXT_LIST.getHasCoordinateWriter() == true && wroteFrameData == false) {
						BLOB_CONTEXT_LIST.writeFrameData(mBackgroundImage.getWidth(), mBackgroundImage.getHeight());
						wroteFrameData = true;
					}
				}
				else if (mResetBackground == true) {
					mResetBackground = false;
							
					currentFileString = getCurrentFileString(currentFileIndex);
					currentFileIndex++;
					
					currentFile = new File(currentFileString);
					if(currentFile.exists() == false)
						continue;
					
					mBackgroundImage = ImageIO.read(currentFile);
					mFastBackgroundRgb = new FastRgb(mBackgroundImage);
				}
				else {
					currentFileString = getCurrentFileString(currentFileIndex);
					currentFileIndex++;
					
					currentFile = new File(currentFileString);
					if(currentFile.exists() == false)
						continue;
					
					currentImage = ImageIO.read(currentFile);
					
					if(CONTROLLER_REGISTRY.getBackgroundParams(mControllerId).getAutoUpdate() == true) {
						rgbOne = new FastRgb(currentImage);
						rgbTwo = new FastRgb(mBackgroundImage);
						blendedColorArgb = -1;
						
						for (x = 0; x < rgbOne.getWidth(); ++x) {
							for (y = 0; y < rgbOne.getHeight(); ++y) {
								blendedColorArgb = ColorUtils.blendFast(rgbOne.get(x, y), rgbTwo.get(x, y));//.synchronizedBlendFast(rgbOne.get(x, y), rgbTwo.get(x, y));
								
								mBackgroundImage.setRGB(x, y, blendedColorArgb);
							}
						}
						
						mFastBackgroundRgb = new FastRgb(mBackgroundImage);
					}	
					
					executorPool.execute(new BlobDetectionThread(mControllerId, mBackgroundImage, mFastBackgroundRgb, currentImage, mGraphicsPanel, mPreviewPanel, 
							mStatusBar, BLOB_CONTEXT_LIST, currentFileString, mOriginalVideoFilename, mCreationDateString));			
				}
				
				currentImage = null;
				mStatusBar.updateStatusBar(mStatusBarBundle);
			} catch (IOException ioException) {				
				//TODO make general exception?
				ioException.printStackTrace();
			}
			
			determineLastFrameIndex(absoluteExtractDirectory);

			if(currentFileIndex > mFrameCount) {
				if(currentFileIndex > mProgressTarget) {
					mProgressIndeterminate = true;
				}
			}
			else {
				mProgressIndeterminate = false;
				mCurrentProgress = currentFileIndex;	
			}

			if(currentFileIndex > mFrameCount) {
				ExecutorService service = Executors.newSingleThreadExecutor();
				Thread t = null;
			    Future<?> f = null;
			    
				try {
				    t = new Thread() {
				        @Override
				        public void run() {	
				    		ThreadUtils.addThreadToHandleList("LocMean Count", this);

				    		while(Thread.interrupted() == false && currentFileIndex > mFrameCount 
				    			&& ExtractVideoFrameThread.mExtractionSet.contains(absoluteExtractDirectoryString) == true)
				    		{
				    			ThreadUtils.blockThread(250, "Brief wait to confirm that there are in fact no more frames to process.");
								
								mFrameCount = absoluteExtractDirectory.list().length;
							
								if(currentFileIndex > mProgressTarget) {
									mStatusBar.clear(mStatusBarBundle);
									
									mGraphicsPanel.clear(); 
									mPreviewPanel.clear();  
								}
				    		}
				    		
				    		ThreadUtils.removeThreadFromHandleList(this);
				        }
				    };
	
				    f = service.submit(t);
	
				    f.get(30, TimeUnit.SECONDS); 
				}
				catch (final InterruptedException e) {

				}
				catch (final TimeoutException e) {

				}
				catch (final ExecutionException e) {
					
				}
				finally {
					t.interrupt();
				    f.cancel(true);
				    
				    service.shutdown();
				    service.shutdownNow();
					
				    if(currentFileIndex > mFrameCount) {
				    	break;
				    }
				}
			}
			
			LastFrameRuntimeParams.lastFrameProcessedMap.replace(mIp, currentFileIndex);
		}
		while (Thread.currentThread().isInterrupted() == false && currentFileIndex <= mFrameCount && mRunning);
		
		mStatusBar.clear(mStatusBarBundle);
		
		currentImage = null;	
		mGraphicsPanel.update(currentImage);
		mPreviewPanel.update(currentImage);  
		
		//mLastNFrames.clear();
		//mLastNFrames = null;
		
		executorPool.shutdown();
		executorPool.shutdownNow();
		while(executorPool.getActiveCount() > 0);
	}
	
	@Override
	public void run() {		
		ThreadUtils.addThreadToHandleList("LocMean Run", this);
		
		mLastIndexGreaterThanCount = false;
		
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
		
		mStatusBarBundle.setUpdatedSource(mOriginalVideoFilename);

		mRunning = true;
				
		final File absoluteExtractDirectory = new File(mIp);
		final String absoluteExtractDirectoryString = absoluteExtractDirectory.getAbsolutePath();
		
		if(WindowsVideoFrameExtractorLegacy.isKeyframeDirectory(absoluteExtractDirectoryString) == true) {
			runKeyFinished(absoluteExtractDirectory);
		} else {
			runExhaustive(absoluteExtractDirectory);
		}
				
		if(mExtractVideoFrameThread != null) {
			mExtractVideoFrameThread.interrupt();
		}
		
		mProgressIndeterminate = false;
		mProgressTarget = 0;
		
		mRunning = false;
	
		ThreadUtils.removeThreadFromHandleList(this);
	}

	@Override
	public int getProgressTarget() {
		return mProgressTarget;
	}

	@Override
	public int getProgress() {
		return mCurrentProgress;
	}

	@Override
	public boolean isProgressIndeterminate() {
		return mProgressIndeterminate;
	}
}

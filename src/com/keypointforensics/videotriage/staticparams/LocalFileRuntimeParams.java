package com.keypointforensics.videotriage.staticparams;

import com.keypointforensics.videotriage.gui.main.controller.CameraControllerPreferencesBundle;
import com.keypointforensics.videotriage.thread.ProcessLocalFileThread;

public class LocalFileRuntimeParams {
	
	/*
	 * Author: Daniel Puckowski
	 */
	
	private static final int                    DEFAULT_FRAMES_PER_SECOND_TARGET = 2;
	private static int mFramesPerSecondTarget = DEFAULT_FRAMES_PER_SECOND_TARGET;
	
	private static final int                         DEFAULT_NUMBER_OF_CONCURRENT_MONITORS = 2;
	private static int mNumberOfConcurrentMonitors = DEFAULT_NUMBER_OF_CONCURRENT_MONITORS;
		
	private static ProcessLocalFileThread mProcessLocalFileThread;
	
	private static CameraControllerPreferencesBundle mCameraControllerPreferencesBundle;
	
	public LocalFileRuntimeParams() {
	}
	
	public static void setGlobalFramesPerSecondTarget(final int globalFramesPerSecondTarget) {
		mFramesPerSecondTarget = globalFramesPerSecondTarget;
	}
	
	public static int getGlobalFramesPerSecondTarget() {
		return mFramesPerSecondTarget;
	}
	
	public static void setGlobalNumberOfConcurrentMonitors(final int globalNumberOfConcurrentMonitors) {
		mNumberOfConcurrentMonitors = globalNumberOfConcurrentMonitors;
	}
	
	public static int getGlobalNumberOfConcurrentMonitors() {
		return mNumberOfConcurrentMonitors;
	}
	
	public static void setGlobalProcessLocalFileThread(final ProcessLocalFileThread processLocalFileThread) {
		mProcessLocalFileThread = processLocalFileThread;
		LastFrameRuntimeParams.lastFrameProcessedMap.clear();
	}
	
	public static ProcessLocalFileThread getGlobalProcessLocalFileThread() {
		return mProcessLocalFileThread;
	}
	
	public static void setCameraControllerPreferencesBundle(final CameraControllerPreferencesBundle cameraControllerPreferencesBundle) {
		mCameraControllerPreferencesBundle = cameraControllerPreferencesBundle;
	}
	
	public static CameraControllerPreferencesBundle getCameraControllerPreferencesBundle() {
		return mCameraControllerPreferencesBundle;
	} 
}

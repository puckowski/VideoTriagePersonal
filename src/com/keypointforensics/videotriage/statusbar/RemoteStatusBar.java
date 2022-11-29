package com.keypointforensics.videotriage.statusbar;

import java.util.concurrent.atomic.AtomicLong;

import com.keypointforensics.videotriage.gui.main.controller.CameraControllerRegistry;
import com.keypointforensics.videotriage.params.StatusBarRuntimeParams;
import com.keypointforensics.videotriage.util.Utils;

public class RemoteStatusBar implements StatusBar {

	/*
	 * Author: Daniel Puckowski
	 */
	
	public static final int MAXIMUM_IP_STRING_LENGTH = 40;
	
	private final CameraControllerRegistry CONTROLLER_REGISTRY        = CameraControllerRegistry.INSTANCE;
	private final double                   ONE_SECOND_IN_MILLIS       = 1000.0;
	private final int                      DEFAULT_TARGET_FRAME_COUNT = 10;
	
	private final int                      TARGET_FRAME_COUNT;
	private final String                   CONTROLLER_ID;
	
	private AtomicLong mFramesProcessedCount;
	private long       mTotalMillis;
	private double     mAverageMillisPerFrame;
	private int        mFramesPerSecond;
	
	public RemoteStatusBar(final String controllerId) {
		CONTROLLER_ID         = controllerId;
		TARGET_FRAME_COUNT    = DEFAULT_TARGET_FRAME_COUNT;
		mFramesProcessedCount = new AtomicLong();
	}
	
	public RemoteStatusBar(final String controllerId, final int targetFrameCount) {
		CONTROLLER_ID         = controllerId;
		TARGET_FRAME_COUNT    = targetFrameCount;
		mFramesProcessedCount = new AtomicLong();
	}
	
	public void reset() {		
		mTotalMillis           = 0;
		mAverageMillisPerFrame = 0;
		mFramesPerSecond       = 0;
		
		mFramesProcessedCount.set(0); 
	}
	
	public long getFrameCount() {
		return mFramesProcessedCount.get();
	}
	
	public void incrementFrameCount(final long millisForFrame) {
		if(CONTROLLER_REGISTRY.getStatusBarParams(CONTROLLER_ID).getEnabled() == false) {			
			return;
		}
		
		mTotalMillis += millisForFrame;
		mFramesProcessedCount.getAndIncrement();
	}
	
	public void clear(final StatusBarBundle bundle) {	
		bundle.getGraphicsPanel().updateStatusString("");
	}
	
	public void updateStatusBar(final StatusBarBundle bundle) {	
		final StatusBarRuntimeParams statusBarParams = CONTROLLER_REGISTRY.getStatusBarParams(CONTROLLER_ID);
		
		if(statusBarParams.getEnabled() == false) {			
			return;
		}
		
		final double count = getFrameCount();

		if(count >= TARGET_FRAME_COUNT) {
			calculateFramesPerSecond(count);
			bundle.getGraphicsPanel().updateStatusString("FPS: " + mFramesPerSecond + "  " + "IP: " + bundle.getIp() + "  " + "Port: " + bundle.getPort() + "  " + Utils.getTimeStamp());
									
			reset();
		}
	}
	
	public void calculateFramesPerSecond(final double count) {
		if(count != TARGET_FRAME_COUNT) {			
			return;
		}
		
		mAverageMillisPerFrame = (double) mTotalMillis / count;
		mFramesPerSecond = (int) Math.floor(ONE_SECOND_IN_MILLIS / mAverageMillisPerFrame);	
	}
}

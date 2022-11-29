package com.keypointforensics.videotriage.params;

public class BackgroundRuntimeParams {

	/*
	 * Author: Daniel Puckowski
	 */
	
	public static final int  INVALID_BACKGROUND_THRESHOLD = -1;
	public static final int  MINIMUM_BACKGROUND_THRESHOLD = 0;
	public static final int  MAXIMUM_BACKGROUND_THRESHOLD = 255;
	
	private static final int           DEFAULT_BACKGROUND_THRESHOLD = 30; 
	private int mBackgroundThreshold = INVALID_BACKGROUND_THRESHOLD;
	
	private static final boolean       DEFAULT_AUTO_UPDATE_STATE = true;
	private boolean mAutoUpdateState = DEFAULT_AUTO_UPDATE_STATE;
	
	private EBackgroundMethod DEFAULT_BACKGROUND_METHOD = EBackgroundMethod.BLENDED_FRAME_DIFFERENCE;
	private EBackgroundMethod mBackgroundMethod         = DEFAULT_BACKGROUND_METHOD;
	
	public BackgroundRuntimeParams() {
		getThreshold();
		getAutoUpdateInitial();
	}
	
	private void getAutoUpdateInitial() {
		mAutoUpdateState = DEFAULT_AUTO_UPDATE_STATE;
	}
	
	public boolean getAutoUpdate() {
		return mAutoUpdateState;
	}
	
	public void setAutoUpdate(final boolean newAutoUpdateState) {
		mAutoUpdateState = newAutoUpdateState;
	}
	
	private void getThreshold() {
		if(mBackgroundThreshold == INVALID_BACKGROUND_THRESHOLD) {
			mBackgroundThreshold = DEFAULT_BACKGROUND_THRESHOLD;
		}
	}
	
	public int getThresholdInt() {		
		return mBackgroundThreshold;
	}
	
	public void setThreshold(final int newBackgroundThreshold) {
		if(newBackgroundThreshold >= 0 && newBackgroundThreshold <= MAXIMUM_BACKGROUND_THRESHOLD) {			
			mBackgroundThreshold = newBackgroundThreshold;
		} 
	}
	
	public EBackgroundMethod getBackgroundMethod() {
		return mBackgroundMethod;
	}
	
	public void setBackgroundMethod(final EBackgroundMethod backgroundMethod) {
		mBackgroundMethod = backgroundMethod;
	}
}

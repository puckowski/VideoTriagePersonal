package com.keypointforensics.videotriage.params;

public class StatusBarRuntimeParams {

	/*
	 * Author: Daniel Puckowski
	 */
	
	private final boolean               DEFAULT_STATUS_BAR_ENABLED = true;
	private boolean mStatusBarEnabled = DEFAULT_STATUS_BAR_ENABLED;
	
	public StatusBarRuntimeParams() {
		getEnabledInitial();
	}
	
	private void getEnabledInitial() {
		mStatusBarEnabled = DEFAULT_STATUS_BAR_ENABLED;
	}
	
	public boolean getEnabled() {		
		return mStatusBarEnabled;
	}
	
	public void setEnabled(final boolean statusBarEnabled) {
		mStatusBarEnabled = statusBarEnabled;
	}
}

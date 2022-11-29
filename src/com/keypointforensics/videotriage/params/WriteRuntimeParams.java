package com.keypointforensics.videotriage.params;

public class WriteRuntimeParams {
	
	/*
	 * Author: Daniel Puckowski
	 */
	
	public static final double MINIMUM_ENTROPY_THRESHOLD = 6.0;
	public static final double MAXIMUM_ENTROPY_THRESHOLD = 7.5;
	
	private final boolean         DEFAULT_WRITE_STATE = false;
	private boolean mWriteState = DEFAULT_WRITE_STATE;
	
	public static final boolean             DEFAULT_ENTROPY_FILTER_STATE = true;
	private boolean mThresholdFilterState = DEFAULT_ENTROPY_FILTER_STATE;
	
	public static final double         DEFAULT_ENTROPY_THRESHOLD = 7.0;
	private double mEntropyThreshold = DEFAULT_ENTROPY_THRESHOLD;
	
	public WriteRuntimeParams() {
		getWriteStateInitial();
		getEntropyFilterStateInitial();
		getEntropyThresholdInitial();
	}
	
	private void getWriteStateInitial() {
		mWriteState = DEFAULT_WRITE_STATE;
	}
	
	public boolean getWriteState() {		
		return mWriteState;
	}
	
	public void setWriteState(final boolean newWriteState) {
		mWriteState = newWriteState;
	}
	
	private void getEntropyFilterStateInitial() {
		mThresholdFilterState = DEFAULT_ENTROPY_FILTER_STATE;
	}
	
	public boolean getEntropyFilterState() {
		return mThresholdFilterState;
	}
	
	public void setEntropyFilterState(final boolean newThresholdFilterState) {
		mThresholdFilterState = newThresholdFilterState;
	}
	
	private void getEntropyThresholdInitial() {
		mEntropyThreshold = DEFAULT_ENTROPY_THRESHOLD;
	}
	
	public double getEntropyThreshold() {
		return mEntropyThreshold;
	}
	
	public double getEntropyThresholdForSlider() {
		return ((mEntropyThreshold - MINIMUM_ENTROPY_THRESHOLD) / 1.5) * 100.0;
	}
	
	public void setEntropyThreshold(final double newEntropyThreshold) {
		mEntropyThreshold = ((newEntropyThreshold / 100.0) * 1.5) + MINIMUM_ENTROPY_THRESHOLD;
	}
}

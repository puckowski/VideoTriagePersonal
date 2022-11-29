package com.keypointforensics.videotriage.params;

public class SourceRuntimeParams {

	/*
	 * Author: Daniel Puckowski
	 */
	
	private final boolean                  DEFAULT_DRAW_ON_SOURCE_ENABLED = false;
	private boolean mDrawOnSourceEnabled = DEFAULT_DRAW_ON_SOURCE_ENABLED;
	
	private final boolean                      DEFAULT_EXHAUSTIVE_SEARCH_ENABLED = false;
	private boolean mExhaustiveSearchEnabled = DEFAULT_EXHAUSTIVE_SEARCH_ENABLED;
	
	public SourceRuntimeParams() {
		setDrawOnSourceInitial();
		setExhaustiveSearchInitial();
	}
	
	private void setDrawOnSourceInitial() {
		mDrawOnSourceEnabled = DEFAULT_DRAW_ON_SOURCE_ENABLED;
	}
	
	public boolean getDrawOnSourceEnabled() {		
		return mDrawOnSourceEnabled;
	}
	
	public void setDrawOnSourceEnabled(final boolean drawOnSourceEnabled) {
		mDrawOnSourceEnabled = drawOnSourceEnabled;
	}
	
	private void setExhaustiveSearchInitial() {
		mExhaustiveSearchEnabled = DEFAULT_EXHAUSTIVE_SEARCH_ENABLED;
	}
	
	public boolean getExhaustiveSearchEnabled() {
		return mExhaustiveSearchEnabled;
	}
	
	public void setExhaustiveSearchEnabled(final boolean exhaustiveSearchEnabled) {
		mExhaustiveSearchEnabled = exhaustiveSearchEnabled;
	}
}

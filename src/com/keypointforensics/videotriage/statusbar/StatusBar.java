package com.keypointforensics.videotriage.statusbar;

public interface StatusBar {

	/*
	 * Author: Daniel Puckowski
	 */
	
	public void reset();
	public long getFrameCount();
	public void incrementFrameCount(final long millisForFrame);
	public void updateStatusBar(final StatusBarBundle bundle);
	public void calculateFramesPerSecond(final double count);
	public void clear(final StatusBarBundle bundle);
}

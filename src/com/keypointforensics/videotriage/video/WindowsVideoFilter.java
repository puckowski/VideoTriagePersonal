package com.keypointforensics.videotriage.video;

import com.keypointforensics.videotriage.progress.ProgressBundle;

public abstract class WindowsVideoFilter {

	protected int mNumberOfFramesProcessed;
	
	public int getNumberOfFramesProcessed() {
		return mNumberOfFramesProcessed;
	}
	
	public abstract String apply(final String absoluteFilePath, final int frameOffset, final ProgressBundle progressBundle) throws InterruptedException;
	
}

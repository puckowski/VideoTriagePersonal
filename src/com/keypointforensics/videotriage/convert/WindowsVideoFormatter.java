package com.keypointforensics.videotriage.convert;

import com.keypointforensics.videotriage.progress.ProgressBundle;

public abstract class WindowsVideoFormatter {

	protected int mNumberOfFramesProcessed;
	
	public int getNumberOfFramesProcessed() {
		return mNumberOfFramesProcessed;
	}
	
	public abstract String apply(final String absoluteFilePath, final int frameOffset, final ProgressBundle progressBundle) throws InterruptedException;
	
}

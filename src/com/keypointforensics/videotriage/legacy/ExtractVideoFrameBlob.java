package com.keypointforensics.videotriage.legacy;

import com.keypointforensics.videotriage.thread.ExtractVideoFrameThread;

public class ExtractVideoFrameBlob {

	/*
	 * Author: Daniel Puckowski
	 */
	
	private final ExtractVideoFrameThread EXTRACT_VIDEO_FRAME_THREAD;
	private final String ABSOLUTE_EXTRACT_PATH;
	
	public ExtractVideoFrameBlob(final ExtractVideoFrameThread extractVideoFrameThread, final String absoluteExtractPath) {
		EXTRACT_VIDEO_FRAME_THREAD = extractVideoFrameThread;
		ABSOLUTE_EXTRACT_PATH = absoluteExtractPath;
	}
	
	public ExtractVideoFrameThread getExtractVideoFrameThread() {
		return EXTRACT_VIDEO_FRAME_THREAD;
	}
	
	public String getAbsoluteExtractPath() {
		return ABSOLUTE_EXTRACT_PATH;
	}
}

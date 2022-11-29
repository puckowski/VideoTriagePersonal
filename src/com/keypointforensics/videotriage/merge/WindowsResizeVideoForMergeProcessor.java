package com.keypointforensics.videotriage.merge;

import java.io.File;

import com.keypointforensics.videotriage.legacy.FileUtilsLegacy;
import com.keypointforensics.videotriage.legacy.WindowsVideoFrameExtractorLegacy;
import com.keypointforensics.videotriage.progress.ProgressBundle;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.video.WindowsVideoFilter;

public class WindowsResizeVideoForMergeProcessor extends WindowsVideoFilter {

	private final String SUFFIX_TEMPORAL_AVERAGING_DENOISER = "_uniform_merge";
	
	private final int VIDEO_HEIGHT;
	private final int VIDEO_WIDTH;
	private final int VIDEO_FRAME_RATE;
	
	public WindowsResizeVideoForMergeProcessor(final int videoHeight, final int videoWidth, final int videoFrameRate) {
		VIDEO_HEIGHT     = videoHeight;
		VIDEO_WIDTH      = videoWidth;
		VIDEO_FRAME_RATE = videoFrameRate;
	}
	
	@Override
	public String apply(String absoluteFilePath, final int frameOffset, final ProgressBundle progressBundle) throws InterruptedException {
		String outputVideoAbsolutePath = FileUtils.getShortFilename(absoluteFilePath);
		outputVideoAbsolutePath = outputVideoAbsolutePath.substring(0, outputVideoAbsolutePath.lastIndexOf(".")) + SUFFIX_TEMPORAL_AVERAGING_DENOISER +
				outputVideoAbsolutePath.substring(outputVideoAbsolutePath.lastIndexOf("."), outputVideoAbsolutePath.length());
		
		String newAbsoluteFilePath = FileUtils.MERGED_DIRECTORY + outputVideoAbsolutePath;

		File checkExistingFile = new File(newAbsoluteFilePath);
		checkExistingFile.delete();
		
		final String command = "\"" + FileUtilsLegacy.BINARIES_DIRECTORY + WindowsVideoFrameExtractorLegacy.FFMPEG4_EXECUTABLE_NAME + "\" -threads 2 " + "-i " + "\"" + absoluteFilePath + "\""
			+ " -vcodec libx264 -s " + VIDEO_HEIGHT + "x" + VIDEO_WIDTH + " -r " + VIDEO_FRAME_RATE + " -strict experimental \"" + outputVideoAbsolutePath + "\"";
		//-acodec libvo_aacenc 
		
		GenericVideoMergeThread genericVideoMergeThread = new GenericVideoMergeThread(command, frameOffset, progressBundle);
		genericVideoMergeThread.start();
		genericVideoMergeThread.join();
			
		mNumberOfFramesProcessed = genericVideoMergeThread.getFinalFrameCount();
		
		return newAbsoluteFilePath;
	}

}
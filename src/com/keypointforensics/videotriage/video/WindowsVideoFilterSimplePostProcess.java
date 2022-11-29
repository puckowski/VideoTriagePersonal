package com.keypointforensics.videotriage.video;

import java.io.File;

import com.keypointforensics.videotriage.legacy.FileUtilsLegacy;
import com.keypointforensics.videotriage.legacy.WindowsVideoFrameExtractorLegacy;
import com.keypointforensics.videotriage.progress.ProgressBundle;
import com.keypointforensics.videotriage.thread.VideoDeshakeFilterThread;
import com.keypointforensics.videotriage.util.FileUtils;

public class WindowsVideoFilterSimplePostProcess extends WindowsVideoFilter {

	private final String SUFFIX_SIMPLE_POST_PROCESSING = "_simple_post_process";
	
	@Override
	public String apply(String absoluteFilePath, final int frameOffset, final ProgressBundle progressBundle) throws InterruptedException {
		String outputVideoAbsolutePath = FileUtils.getShortFilename(absoluteFilePath);
		outputVideoAbsolutePath = outputVideoAbsolutePath.substring(0, outputVideoAbsolutePath.lastIndexOf(".")) + SUFFIX_SIMPLE_POST_PROCESSING +
				outputVideoAbsolutePath.substring(outputVideoAbsolutePath.lastIndexOf("."), outputVideoAbsolutePath.length());
		
		String newAbsoluteFilePath = FileUtils.ENHANCED_DIRECTORY + outputVideoAbsolutePath;

		File checkExistingFile = new File(newAbsoluteFilePath);
		checkExistingFile.delete();
		
		final String command = "\"" + FileUtilsLegacy.BINARIES_DIRECTORY + WindowsVideoFrameExtractorLegacy.FFMPEG4_EXECUTABLE_NAME + "\" -threads 2 " + "-i " + "\"" + absoluteFilePath + "\""
			+ " -vf spp=5:10:0:1 -c:a copy \"" + outputVideoAbsolutePath + "\"";

		VideoDeshakeFilterThread videoDeshakeFilterThread = new VideoDeshakeFilterThread(command, frameOffset, progressBundle);
		videoDeshakeFilterThread.start();
		videoDeshakeFilterThread.join();
			
		mNumberOfFramesProcessed = videoDeshakeFilterThread.getFinalFrameCount();
		
		return newAbsoluteFilePath;
	}

}
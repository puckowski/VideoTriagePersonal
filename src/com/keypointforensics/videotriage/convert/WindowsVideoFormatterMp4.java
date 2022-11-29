package com.keypointforensics.videotriage.convert;

import java.io.File;

import com.keypointforensics.videotriage.legacy.FileUtilsLegacy;
import com.keypointforensics.videotriage.legacy.WindowsVideoFrameExtractorLegacy;
import com.keypointforensics.videotriage.progress.ProgressBundle;
import com.keypointforensics.videotriage.thread.VideoConvertThread;
import com.keypointforensics.videotriage.util.FileUtils;

public class WindowsVideoFormatterMp4 extends WindowsVideoFormatter {

	private final String FILE_EXTENSION = ".mp4";
	
	@Override
	public String apply(String absoluteFilePath, final int frameOffset, final ProgressBundle progressBundle) throws InterruptedException {
		String outputVideoAbsolutePath = FileUtils.getShortFilename(absoluteFilePath);
		outputVideoAbsolutePath = outputVideoAbsolutePath.substring(0, outputVideoAbsolutePath.lastIndexOf(".")) + FILE_EXTENSION;
		
		String newAbsoluteFilePath = FileUtils.EXPORTS_DIRECTORY + outputVideoAbsolutePath;

		File checkExistingFile = new File(newAbsoluteFilePath);
		checkExistingFile.delete();
		
		final String command = "\"" + FileUtilsLegacy.BINARIES_DIRECTORY + WindowsVideoFrameExtractorLegacy.FFMPEG4_EXECUTABLE_NAME + "\" -threads 2 " + "-i " + "\"" + absoluteFilePath + "\""
			+ " \"" + outputVideoAbsolutePath + "\"";

		VideoConvertThread videoConvertThread = new VideoConvertThread(command, frameOffset, progressBundle);
		videoConvertThread.start();
		videoConvertThread.join();
			
		mNumberOfFramesProcessed = videoConvertThread.getFinalFrameCount();
		
		return newAbsoluteFilePath;
	}

}
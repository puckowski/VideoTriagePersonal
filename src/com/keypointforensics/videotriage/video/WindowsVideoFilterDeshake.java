package com.keypointforensics.videotriage.video;

import java.io.File;

import com.keypointforensics.videotriage.legacy.FileUtilsLegacy;
import com.keypointforensics.videotriage.legacy.WindowsVideoFrameExtractorLegacy;
import com.keypointforensics.videotriage.progress.ProgressBundle;
import com.keypointforensics.videotriage.thread.VideoDeshakeFilterThread;
import com.keypointforensics.videotriage.thread.VideoDeshakeFilterThreadUnmonitored;
import com.keypointforensics.videotriage.util.FileUtils;

public class WindowsVideoFilterDeshake extends WindowsVideoFilter {

	private final String SUFFIX_STABILIZED = "_stabilized";
	private final String STABILIZATION_TRANSFORM_FILE_RELATIVE = "transforms.trf";
	
	@Override
	public String apply(String absoluteFilePath, final int frameOffset, final ProgressBundle progressBundle) throws InterruptedException {
		String outputVideoAbsolutePath = FileUtils.getShortFilename(absoluteFilePath);
		outputVideoAbsolutePath = outputVideoAbsolutePath.substring(0, outputVideoAbsolutePath.lastIndexOf(".")) + SUFFIX_STABILIZED +
				outputVideoAbsolutePath.substring(outputVideoAbsolutePath.lastIndexOf("."), outputVideoAbsolutePath.length());
		
		String newAbsoluteFilePath = FileUtils.ENHANCED_DIRECTORY + outputVideoAbsolutePath;

		File checkExistingFile = new File(newAbsoluteFilePath);
		checkExistingFile.delete();
		
		File transformsFileCheck = new File(FileUtils.ENHANCED_DIRECTORY + STABILIZATION_TRANSFORM_FILE_RELATIVE);
		transformsFileCheck.delete();
		
		final String analyzeVideoFileCommand = "\"" + FileUtilsLegacy.BINARIES_DIRECTORY
				+ WindowsVideoFrameExtractorLegacy.FFMPEG4_EXECUTABLE_NAME + "\" -threads 2 " 
				+ "-i " + "\"" + absoluteFilePath + "\" -vf vidstabdetect -f null -";
		
		progressBundle.progressBar.setIndeterminate(true);
		
		VideoDeshakeFilterThreadUnmonitored videoDeshakeFilterThreadUnmonitored = new VideoDeshakeFilterThreadUnmonitored(analyzeVideoFileCommand);
		videoDeshakeFilterThreadUnmonitored.start();
		videoDeshakeFilterThreadUnmonitored.join();
		
		progressBundle.progressBar.setIndeterminate(false);
		
		final String stabilizeVideoCommand = "\"" + FileUtilsLegacy.BINARIES_DIRECTORY
			+ WindowsVideoFrameExtractorLegacy.FFMPEG4_EXECUTABLE_NAME + "\" -threads 2 " 
			+ "-i " + "\"" + absoluteFilePath + "\" -vf vidstabtransform=smoothing=30:input=\"" + STABILIZATION_TRANSFORM_FILE_RELATIVE + "\" -c:a copy \"" + newAbsoluteFilePath + "\"";
		
		VideoDeshakeFilterThread videoDeshakeFilterThread = new VideoDeshakeFilterThread(stabilizeVideoCommand, frameOffset, progressBundle);
		videoDeshakeFilterThread.start();
		videoDeshakeFilterThread.join();
		
		File transformFile = new File(FileUtils.ENHANCED_DIRECTORY + STABILIZATION_TRANSFORM_FILE_RELATIVE);
		transformFile.delete();
		
		mNumberOfFramesProcessed = videoDeshakeFilterThread.getFinalFrameCount();
		
		return newAbsoluteFilePath;
	}

}

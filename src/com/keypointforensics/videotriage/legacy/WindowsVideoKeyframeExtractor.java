package com.keypointforensics.videotriage.legacy;

import com.keypointforensics.videotriage.gui.wizard.preview.VideoKeyframeProgressBundle;
import com.keypointforensics.videotriage.progress.ProgressBundle;
import com.keypointforensics.videotriage.thread.ExtractKeyframeThread;
import com.keypointforensics.videotriage.thread.ExtractKeyframeThreadUnmonitored;

public class WindowsVideoKeyframeExtractor {

	public static final int DEFAULT_FRAMES_PER_SECOND_TARGET = 24;
	public static final int DEFAULT_WIDTH                    = 640;
	public static final int DEFAULT_HEIGHT                   = 360;
	public static final int DEFAULT_NUMBER_OF_ZEROS_PADDING  = 7;
	public static final int DEFAULT_CONSTANT_RATE_FACTOR     = 28;
	
	public static void mergeKeyframesIntoVideo(final ProgressBundle progressBundle, final int videoFrameOffset, final String absoluteInputDirectory, String absoluteOutputPath) {
		mergeKeyframesIntoVideo(progressBundle, videoFrameOffset, absoluteInputDirectory, DEFAULT_FRAMES_PER_SECOND_TARGET, DEFAULT_WIDTH, DEFAULT_HEIGHT,
			DEFAULT_NUMBER_OF_ZEROS_PADDING, DEFAULT_CONSTANT_RATE_FACTOR, absoluteOutputPath);
	}
	
	public static VideoKeyframeProgressBundle mergeKeyframesIntoVideoJoined(final ProgressBundle progressBundle, final int videoFrameOffset, final String absoluteInputDirectory, String absoluteOutputPath) {
		return mergeKeyframesIntoVideoJoined(progressBundle, videoFrameOffset, absoluteInputDirectory, DEFAULT_FRAMES_PER_SECOND_TARGET, DEFAULT_WIDTH, DEFAULT_HEIGHT,
			DEFAULT_NUMBER_OF_ZEROS_PADDING, DEFAULT_CONSTANT_RATE_FACTOR, absoluteOutputPath);
	}
	
	public static void mergeKeyframesIntoVideo(final ProgressBundle progressBundle, final int videoFrameOffset, final String absoluteInputDirectory, final int framesPerSecondTarget, final int width, final int height, 
			int numberOfZerosPadding, int constantRateFactor, String absoluteOutputPath) {
		String command = "\"" + FileUtilsLegacy.BINARIES_DIRECTORY
			+ WindowsVideoFrameExtractorLegacy.FFMPEG_EXECUTABLE_NAME + "\" -threads 2 " 
			+ "-r " + framesPerSecondTarget + " -f image2 -s " + width + "x" + height 
			+ " -i image-%" + String.valueOf(numberOfZerosPadding) + "d.jpeg -vcodec libx264 -crf " + constantRateFactor + " -pix_fmt yuv420p \"" +
			absoluteOutputPath + "\"";
		
		ExtractKeyframeThread extractKeyframeThread = new ExtractKeyframeThread(absoluteInputDirectory, command, videoFrameOffset, progressBundle);
		extractKeyframeThread.start();
	}
	
	public static VideoKeyframeProgressBundle mergeKeyframesIntoVideoJoined(final ProgressBundle progressBundle, final int videoFrameOffset, final String absoluteInputDirectory, final int framesPerSecondTarget, final int width, final int height, 
			int numberOfZerosPadding, int constantRateFactor, String absoluteOutputPath) {
		VideoKeyframeProgressBundle videoKeyframeProgressBundle = new VideoKeyframeProgressBundle();
		
		String command = "\"" + FileUtilsLegacy.BINARIES_DIRECTORY
			+ WindowsVideoFrameExtractorLegacy.FFMPEG_EXECUTABLE_NAME + "\" -threads 2 " 
			+ "-r " + framesPerSecondTarget + " -f image2 -s " + width + "x" + height 
			+ " -i image-%" + String.valueOf(numberOfZerosPadding) + "d.jpeg -vcodec libx264 -crf " + constantRateFactor + " -pix_fmt yuv420p \"" +
			absoluteOutputPath + "\"";
		
		ExtractKeyframeThread extractKeyframeThread = new ExtractKeyframeThread(absoluteInputDirectory, command, videoFrameOffset, progressBundle);
		extractKeyframeThread.start();
		
		try {
			extractKeyframeThread.join();
		} catch (InterruptedException interruptedException) {
			//interruptedException.printStackTrace();
			
			return videoKeyframeProgressBundle;
		}
		
		videoKeyframeProgressBundle.progressGained = extractKeyframeThread.getFinalFrameCount();
		videoKeyframeProgressBundle.wasSuccessful = true;
		
		return videoKeyframeProgressBundle;
	}
	
	public static VideoKeyframeProgressBundle extractKeyframesFromVideoJoined(String absoluteVideoPath, String absoluteOutputDirectory) {
		return extractKeyframesFromVideoJoined(absoluteVideoPath, absoluteOutputDirectory, DEFAULT_NUMBER_OF_ZEROS_PADDING);
	}
	
	public static VideoKeyframeProgressBundle extractKeyframesFromVideoJoined(String absoluteVideoPath, String absoluteOutputDirectory, int numberOfZerosPadding) {						
		VideoKeyframeProgressBundle videoKeyframeProgressBundle = new VideoKeyframeProgressBundle();
		
		String command = "\"" + FileUtilsLegacy.BINARIES_DIRECTORY
			+ WindowsVideoFrameExtractorLegacy.FFMPEG_EXECUTABLE_NAME + "\" -threads 2 " 
			+ "-discard nokey -i " + "\"" + absoluteVideoPath + "\" "
			+ "-q:v 2 -vf select=\"eq(pict_type\\,PICT_TYPE_I)\" -vsync 0 \"image-%" + String.valueOf(numberOfZerosPadding)
			+ "d.jpeg\"";
		
		ExtractKeyframeThreadUnmonitored extractKeyframeThreadUnmonitored = new ExtractKeyframeThreadUnmonitored(absoluteOutputDirectory, command);
		extractKeyframeThreadUnmonitored.start();
		
		try {
			extractKeyframeThreadUnmonitored.join();
		} catch (InterruptedException interruptedException) {
			//interruptedException.printStackTrace();
			
			return videoKeyframeProgressBundle;
		}
		
		videoKeyframeProgressBundle.progressGained = 1;
		videoKeyframeProgressBundle.wasSuccessful = true;
		
		return videoKeyframeProgressBundle;
	}
	
	public static void extractKeyframesFromVideo(String absoluteVideoPath, String absoluteOutputDirectory) {
		extractKeyframesFromVideo(absoluteVideoPath, absoluteOutputDirectory, DEFAULT_NUMBER_OF_ZEROS_PADDING);
	}
	
	public static void extractKeyframesFromVideo(String absoluteVideoPath, String absoluteOutputDirectory, int numberOfZerosPadding) {						
		String command = "\"" + FileUtilsLegacy.BINARIES_DIRECTORY
			+ WindowsVideoFrameExtractorLegacy.FFMPEG_EXECUTABLE_NAME + "\" -threads 2 " 
			+ "-discard nokey -i " + "\"" + absoluteVideoPath + "\" "
			+ "-q:v 2 -vf select=\"eq(pict_type\\,PICT_TYPE_I)\" -vsync 0 \"image-%" + String.valueOf(numberOfZerosPadding)
			+ "d.jpeg\"";
		
		ExtractKeyframeThreadUnmonitored extractKeyframeThreadUnmonitored = new ExtractKeyframeThreadUnmonitored(absoluteOutputDirectory, command);
		extractKeyframeThreadUnmonitored.start();
	}
}

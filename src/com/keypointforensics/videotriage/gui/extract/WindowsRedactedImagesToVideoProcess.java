package com.keypointforensics.videotriage.gui.extract;

import java.io.File;

import com.keypointforensics.videotriage.legacy.FileUtilsLegacy;
import com.keypointforensics.videotriage.legacy.WindowsVideoFrameExtractorLegacy;
import com.keypointforensics.videotriage.progress.ProgressBundle;
import com.keypointforensics.videotriage.util.FileUtils;

public class WindowsRedactedImagesToVideoProcess {
	
	public String apply(String redactedImageFileFormat, String outputVideoFilename, final ProgressBundle progressBundle) throws InterruptedException {
		outputVideoFilename = FileUtils.getShortFilename(outputVideoFilename);
	
		String newAbsoluteFilePath = FileUtils.REDACT_DIRECTORY + outputVideoFilename;

		File checkExistingFile = new File(newAbsoluteFilePath);
		checkExistingFile.delete();
		
		final String command = "\"" + FileUtilsLegacy.BINARIES_DIRECTORY + WindowsVideoFrameExtractorLegacy.FFMPEG4_EXECUTABLE_NAME + "\" -threads 2 " + "-i " 
			+ "" + redactedImageFileFormat + " \"" + newAbsoluteFilePath + "\"";
		
		ImageToVideoThread imageToVideoThread = new ImageToVideoThread(command, 0, progressBundle);
		imageToVideoThread.start();
		imageToVideoThread.join();
		
		//progressBundle.progressBar.repaint();
		//progressBundle.frame.dispose();
		
		return newAbsoluteFilePath;
	}

}
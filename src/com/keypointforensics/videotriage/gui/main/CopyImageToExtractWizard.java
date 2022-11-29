package com.keypointforensics.videotriage.gui.main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.swing.JOptionPane;

import com.keypointforensics.videotriage.legacy.UtilsLegacy;
import com.keypointforensics.videotriage.progress.ProgressBundle;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ProgressUtils;
import com.keypointforensics.videotriage.util.Utils;

public class CopyImageToExtractWizard {

	public CopyImageToExtractWizard() {
		
	}
	
	public void copyImagesToExtractFolder(final String directoryToCopy) {
		File[] files = new File(directoryToCopy).listFiles();
		
		ProgressBundle progressBundle = ProgressUtils.getProgressBundle("Importing Images...", files.length);
		
		String shortDirectoryName = FileUtils.getLastDirectory(directoryToCopy);
		String outputDirectory = FileUtils.EXTRACTS_DIRECTORY + shortDirectoryName + File.separator;
		File outputFile;
		
		File makeOutputDirectory = new File(outputDirectory);
		
		if(makeOutputDirectory.exists() == true) {
			final int deleteExistingChoice = UtilsLegacy.displayConfirmDialog("Notice", "Selected folder \"" + shortDirectoryName + "\" already appears to be imported.\n" +
				"Remove existing folder?");
			
			if(deleteExistingChoice == JOptionPane.OK_OPTION) {			
				FileUtils.deleteDirectoryContents(makeOutputDirectory, true);
			} else {
				return;
			}
		} else {
			makeOutputDirectory.mkdir();
		}
		
		int errorCount = 0;
		
		for(File fileToCopy : files) {
			outputFile = new File(outputDirectory + FileUtils.getShortFilename(fileToCopy.getAbsolutePath()));
			
			try {
				Files.copy(fileToCopy.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException ioException) {
				//ioException.printStackTrace();
				
				errorCount++;
			}
			
			progressBundle.progressBar.setValue(progressBundle.progressBar.getValue() + 1);
			progressBundle.progressBar.repaint();
		}
		
		progressBundle.frame.dispose();
		
		if(errorCount == 0) {
			Utils.displayMessageDialog("Images Imported", "All images imported successfully.");
		} else {
			Utils.displayMessageDialog("Import Error", "Failed to import " + errorCount + " images.");
		}
	}
}

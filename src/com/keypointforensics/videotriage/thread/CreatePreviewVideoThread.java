package com.keypointforensics.videotriage.thread;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.keypointforensics.videotriage.gui.localfile.wizard.video.FailedToProcessListWindow;
import com.keypointforensics.videotriage.gui.wizard.preview.VideoKeyframeProgressBundle;
import com.keypointforensics.videotriage.legacy.FileUtilsLegacy;
import com.keypointforensics.videotriage.legacy.UtilsLegacy;
import com.keypointforensics.videotriage.legacy.WindowsVideoKeyframeExtractor;
import com.keypointforensics.videotriage.progress.ProgressBundle;
import com.keypointforensics.videotriage.util.CursorUtils;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ProgressUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;

public class CreatePreviewVideoThread extends Thread {

	private final ArrayList<String> FILES_TO_PROCESS;
	private final int PREVIEW_WIDTH;
	private final int PREVIEW_HEIGHT;
	private final JFrame PARENT_FRAME;
	
	private ArrayList<String> mFilesProcessed;
	
	private ProgressBundle mProgressBundle;
	private int mVideoFrameTarget;
	private int mVideoFrameTargetInitial;
	private int mVideoFrameProgress;
	
	public CreatePreviewVideoThread(final JFrame parentFrame, final ArrayList<String> filesToProcess, final int previewWidth, final int previewHeight) {
		FILES_TO_PROCESS = filesToProcess;
		PREVIEW_WIDTH = previewWidth;
		PREVIEW_HEIGHT = previewHeight;
		PARENT_FRAME = parentFrame;
		
		mFilesProcessed = new ArrayList<String>();
	}
	
	public ArrayList<String> getListOfProcessedFiles() {
		return mFilesProcessed;
	}
	
	@Override
	public void start() {
		run();
	}
	
	private String getCurrentOutputFile(final String currentFileToProcess) {
		String currentOutputFile = FileUtils.getShortFilename(currentFileToProcess);
		
		if(currentOutputFile.contains(".") == true) {
			currentOutputFile = currentOutputFile.substring(0, currentOutputFile.indexOf(".")) + "_preview_" + PREVIEW_WIDTH + "x" + PREVIEW_HEIGHT + "_"
				+ currentOutputFile.substring(currentOutputFile.indexOf("."), currentOutputFile.length());
		} else {
			currentOutputFile += "_preview_" + PREVIEW_WIDTH + "x" + PREVIEW_HEIGHT + ".mp4";
		}
		
		currentOutputFile = FileUtils.PREVIEWS_DIRECTORY + currentOutputFile;
		
		return currentOutputFile;
	}
	
	private File getCurrentWorkingDirectory(final String currentFileToProcess) {
		return new File(FileUtils.TEMPORARY_DIRECTORY + FileUtils.getShortFilename(currentFileToProcess));			
	}
	
	@Override
	public void run() {
		ThreadUtils.addThreadToHandleList("CreatePreviewThread Run", this);
		
		CursorUtils.setBusyCursor(PARENT_FRAME);
		
		final int filesToProcessSize = FILES_TO_PROCESS.size();
		
		int failedToProcessCount = 0;
		HashSet<String> failedToProcessPathSet = new HashSet<String>();
		
		mVideoFrameTarget = 0;
		mVideoFrameTargetInitial = filesToProcessSize;
		mVideoFrameProgress = 0;
		
		if(filesToProcessSize > 1) {
			mProgressBundle = ProgressUtils.getProgressBundle("Create Previews Progress...", mVideoFrameTarget);
		} else {
			mProgressBundle = ProgressUtils.getProgressBundle("Create Preview Progress...", mVideoFrameTarget);
		}
		
		//mProgressBundle.progressBar.setIndeterminate(true);
		mProgressBundle.progressBar.setIndeterminate(false);
		mProgressBundle.progressBar.setValue(mVideoFrameProgress);
		mProgressBundle.progressBar.setMaximum(mVideoFrameTargetInitial);
		mProgressBundle.progressBar.repaint();
		
		File currentWorkingDirectory;
		String currentWorkingDirectoryString;
		String currentOutputFileString;
		File currentOutputFile;
				
		VideoKeyframeProgressBundle videoKeyframeProgressBundle = null;
		
		for(String currentFileToProcess : FILES_TO_PROCESS) {
			currentWorkingDirectory = getCurrentWorkingDirectory(currentFileToProcess);
			currentWorkingDirectoryString = currentWorkingDirectory.getAbsolutePath();
			currentWorkingDirectory.mkdir();
						
			videoKeyframeProgressBundle = WindowsVideoKeyframeExtractor.extractKeyframesFromVideoJoined(currentFileToProcess, currentWorkingDirectoryString);
		
			if(videoKeyframeProgressBundle.wasSuccessful == true) {
				mProgressBundle.progressBar.setValue(mProgressBundle.progressBar.getValue() + videoKeyframeProgressBundle.progressGained);
				mProgressBundle.progressBar.repaint();
			} else {
				failedToProcessCount++;
				failedToProcessPathSet.add(currentFileToProcess);
			}
			
			mVideoFrameTarget += currentWorkingDirectory.list().length;
		}
		
		mVideoFrameProgress = 0;
		
		mProgressBundle.progressBar.setIndeterminate(false);
		mProgressBundle.progressBar.setValue(mVideoFrameProgress);
		mProgressBundle.progressBar.setMaximum(mVideoFrameTarget);
		mProgressBundle.progressBar.repaint();
	
		for(String currentFileToProcess : FILES_TO_PROCESS) {
			currentWorkingDirectory = getCurrentWorkingDirectory(currentFileToProcess);
			currentWorkingDirectoryString = currentWorkingDirectory.getAbsolutePath();
			
			currentOutputFileString = getCurrentOutputFile(currentFileToProcess);
			currentOutputFile = new File(currentOutputFileString);
			
			if(currentOutputFile.exists() == true) {
				currentOutputFile.delete();
			}
			
			videoKeyframeProgressBundle = WindowsVideoKeyframeExtractor.mergeKeyframesIntoVideoJoined(mProgressBundle, mVideoFrameProgress, 
					currentWorkingDirectoryString, 
					WindowsVideoKeyframeExtractor.DEFAULT_FRAMES_PER_SECOND_TARGET, 
					PREVIEW_WIDTH, PREVIEW_HEIGHT, 
					WindowsVideoKeyframeExtractor.DEFAULT_NUMBER_OF_ZEROS_PADDING, 
					WindowsVideoKeyframeExtractor.DEFAULT_CONSTANT_RATE_FACTOR, 
					currentOutputFileString);	
			
			if(videoKeyframeProgressBundle.wasSuccessful == true) {
				//mProgressBundle.progressBar.setValue(mProgressBundle.progressBar.getValue() + videoKeyframeProgressBundle.progressGained);
				//mProgressBundle.progressBar.repaint();
				
				mVideoFrameProgress += videoKeyframeProgressBundle.progressGained;
			} else {
				failedToProcessCount++;
				failedToProcessPathSet.add(currentFileToProcess);
			}
			
			mFilesProcessed.add(currentOutputFileString);
		}
		
		mProgressBundle.progressBar.repaint();
		mProgressBundle.frame.dispose();
		
		//File folderToDelete;
		
		for(String currentFileToProcess : FILES_TO_PROCESS) {
			currentWorkingDirectory = getCurrentWorkingDirectory(currentFileToProcess);

			FileUtilsLegacy.deleteDirectoryContents(currentWorkingDirectory, true);
			currentWorkingDirectory.delete();
		}
		
		if(failedToProcessCount > 0) {
			buildAndDisplayFailedToProcessDialog(failedToProcessPathSet);
		}
						
		if(filesToProcessSize > 1) {
			displayOpenPreviewsVideoFolderDialog("Finished Creating Previews", "Open preview video folder?");
		} else {
			displayOpenPreviewsVideoFolderDialog("Finished Creating Preview", "Open preview video folder?");
		}
		
		CursorUtils.setDefaultCursor(PARENT_FRAME);
		
		ThreadUtils.removeThreadFromHandleList(this);
	}
	
	private void buildAndDisplayFailedToProcessDialog(HashSet<String> setOfFailedAbsolutePaths) {
		ArrayList<String> listOfFailedAbsolutePaths = new ArrayList<String>();
		
		for(String failedAbsolutePath : setOfFailedAbsolutePaths) {
			listOfFailedAbsolutePaths.add(failedAbsolutePath);
		}
		
		FailedToProcessListWindow failedToProcessListWindow = new FailedToProcessListWindow(listOfFailedAbsolutePaths);
		failedToProcessListWindow.buildAndDisplay();
	}
	
	public static void performOpenPreviewsVideoFolderAction() {
		final File file = new File(FileUtils.PREVIEWS_DIRECTORY);
		final Desktop desktop = Desktop.getDesktop();
		
		try {
			desktop.browse(file.toURI());
		} catch (IOException ioException) {

		}
	}
	
	private void displayOpenPreviewsVideoFolderDialog(final String title, final String message) {
		final int openPreviewVideoFolderSelection = UtilsLegacy.displayConfirmDialog(title, message);
		
		if(openPreviewVideoFolderSelection == JOptionPane.OK_OPTION) {
			performOpenPreviewsVideoFolderAction();
		}
	}
}

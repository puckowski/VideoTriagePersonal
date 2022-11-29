package com.keypointforensics.videotriage.thread;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.keypointforensics.videotriage.gui.localfile.wizard.video.FailedToProcessListWindow;
import com.keypointforensics.videotriage.legacy.FileUtilsLegacy;
import com.keypointforensics.videotriage.legacy.UtilsLegacy;
import com.keypointforensics.videotriage.legacy.WindowsVideoFrameExtractorLegacy;
import com.keypointforensics.videotriage.progress.ProgressBundle;
import com.keypointforensics.videotriage.util.CursorUtils;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ProgressUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;

public class CreateResizedVideoThread extends Thread {

	private final ArrayList<String> FILES_TO_PROCESS;
	private final int RESIZE_WIDTH;
	private final int RESIZE_HEIGHT;
	private final JFrame PARENT_FRAME;
	
	private ArrayList<String> mFilesProcessed;
	
	private ProgressBundle mProgressBundle;
	private int mVideoFrameTarget;
	private int mVideoFrameProgress;
	
	public CreateResizedVideoThread(final JFrame parentFrame, final ArrayList<String> filesToProcess, final int previewWidth, final int previewHeight) {
		FILES_TO_PROCESS = filesToProcess;
		RESIZE_WIDTH = previewWidth;
		RESIZE_HEIGHT = previewHeight;
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
			currentOutputFile = currentOutputFile.substring(0, currentOutputFile.indexOf(".")) + "_resized_" + RESIZE_WIDTH + "x" + RESIZE_HEIGHT + "_"
				+ currentOutputFile.substring(currentOutputFile.indexOf("."), currentOutputFile.length());
		} else {
			currentOutputFile += "_resized_" + RESIZE_WIDTH + "x" + RESIZE_HEIGHT + ".mp4";
		}
		
		currentOutputFile = FileUtils.RESIZED_DIRECTORY + currentOutputFile;
		
		return currentOutputFile;
	}
	
	@Override
	public void run() {
		ThreadUtils.addThreadToHandleList("CreateResizedThread Run", this);
		
		CursorUtils.setBusyCursor(PARENT_FRAME);
		
		final int filesToProcessSize = FILES_TO_PROCESS.size();
		
		int failedToProcessCount = 0;
		HashSet<String> failedToProcessPathSet = new HashSet<String>();
		
		mVideoFrameTarget = 0;
		mVideoFrameProgress = 0;
		
		if(filesToProcessSize > 1) {
			mProgressBundle = ProgressUtils.getProgressBundle("Create Resized Videos Progress...", mVideoFrameTarget);
		} else {
			mProgressBundle = ProgressUtils.getProgressBundle("Create Resized Video Progress...", mVideoFrameTarget);
		}
		
		String currentOutputFileString;
		File currentOutputFile;
						
		mVideoFrameProgress = 0;
		
		mProgressBundle.progressBar.setIndeterminate(true);
		mProgressBundle.progressBar.repaint();
		
		for(String currentFileToProcess : FILES_TO_PROCESS) {
			mVideoFrameTarget += WindowsVideoFrameExtractorLegacy.getVideoFrameCount(currentFileToProcess);
		}

		mProgressBundle.progressBar.setIndeterminate(false);
		mProgressBundle.progressBar.setValue(mVideoFrameProgress);
		mProgressBundle.progressBar.setMaximum(mVideoFrameTarget);
		mProgressBundle.progressBar.repaint();
	
		final String commandPrefix = "\"" + FileUtilsLegacy.BINARIES_DIRECTORY + WindowsVideoFrameExtractorLegacy.FFMPEG4_EXECUTABLE_NAME + "\" -threads 2 -i";
		
		for(String currentFileToProcess : FILES_TO_PROCESS) {
			currentOutputFileString = getCurrentOutputFile(currentFileToProcess);
			currentOutputFile = new File(currentOutputFileString);
			
			if(currentOutputFile.exists() == true) {
				currentOutputFile.delete();
			}
			
			final String command = commandPrefix 
				+ " \"" + currentFileToProcess + "\" -vf scale=" + RESIZE_WIDTH + ":" + RESIZE_HEIGHT + " -c:a copy \"" + currentOutputFile.getAbsolutePath() + "\"";
			
			ExecuteResizeVideoCommandThread executeResizeVideoCommandThread = new ExecuteResizeVideoCommandThread(command, mProgressBundle.progressBar.getValue(), mProgressBundle);
			executeResizeVideoCommandThread.start();
			
			try {
				executeResizeVideoCommandThread.join();
			} catch (InterruptedException interruptedException) {
				//interruptedException.printStackTrace();
				
				failedToProcessCount++;
				failedToProcessPathSet.add(currentFileToProcess);
			}
						
			mFilesProcessed.add(currentOutputFileString);
		}
		
		mProgressBundle.progressBar.repaint();
		mProgressBundle.frame.dispose();
		
		if(failedToProcessCount > 0) {
			buildAndDisplayFailedToProcessDialog(failedToProcessPathSet);
		}
						
		if(filesToProcessSize > 1) {
			displayOpenResizedVideoFolderDialog("Finished Creating Resized Videos", "Open resized video folder?");
		} else {
			displayOpenResizedVideoFolderDialog("Finished Creating Resized Video", "Open resized video folder?");
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
	
	public static void performOpenResizedVideoFolderAction() {
		final File file = new File(FileUtils.RESIZED_DIRECTORY);
		final Desktop desktop = Desktop.getDesktop();
		
		try {
			desktop.browse(file.toURI());
		} catch (IOException ioException) {

		}
	}
	
	private void displayOpenResizedVideoFolderDialog(final String title, final String message) {
		final int openResizedVideoFolderSelection = UtilsLegacy.displayConfirmDialog(title, message);
		
		if(openResizedVideoFolderSelection == JOptionPane.OK_OPTION) {
			performOpenResizedVideoFolderAction();
		}
	}
}

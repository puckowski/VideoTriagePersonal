package com.keypointforensics.videotriage.thread;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.keypointforensics.videotriage.convert.EVideoTriageVideoFormat;
import com.keypointforensics.videotriage.convert.WindowsVideoFormatterAvi;
import com.keypointforensics.videotriage.convert.WindowsVideoFormatterMov;
import com.keypointforensics.videotriage.convert.WindowsVideoFormatterMp4;
import com.keypointforensics.videotriage.gui.localfile.wizard.convert.ConvertLocalFileWizardWindow;
import com.keypointforensics.videotriage.gui.localfile.wizard.video.FailedToProcessListWindow;
import com.keypointforensics.videotriage.legacy.UtilsLegacy;
import com.keypointforensics.videotriage.legacy.WindowsVideoFrameExtractorLegacy;
import com.keypointforensics.videotriage.progress.ProgressBundle;
import com.keypointforensics.videotriage.util.CursorUtils;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ProgressUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;

public class ConvertLocalVideoFilesThread extends Thread {

	/*
	 * Author: Daniel Puckowski
	 */
	
	private final ConvertLocalFileWizardWindow CONVERT_LOCAL_FILE_WIZARD_WINDOW;
	private final ArrayList<String>            VIDEO_FILE_PATH_LIST;
	private final EVideoTriageVideoFormat      VIDEO_TRIAGE_VIDEO_FORMAT_ENUM;
		
	private int mVideoFrameTarget;
	private int mVideoFrameProgress;
	private ProgressBundle mProgressBundle;
	
	public ConvertLocalVideoFilesThread(final ConvertLocalFileWizardWindow convertLocalFileWizardWindow,
			final ArrayList<String> videoFilePathList, final EVideoTriageVideoFormat videoTriageVideoFormatEnum) {
		CONVERT_LOCAL_FILE_WIZARD_WINDOW = convertLocalFileWizardWindow;
		VIDEO_FILE_PATH_LIST             = videoFilePathList;
		VIDEO_TRIAGE_VIDEO_FORMAT_ENUM   = videoTriageVideoFormatEnum;
	}
	
	public static void performOpenExportsFolderAction() {
		final File file = new File(FileUtils.EXPORTS_DIRECTORY);
		final Desktop desktop = Desktop.getDesktop();
		
		try {
			desktop.browse(file.toURI());
		} catch (IOException ioException) {
			
		}
	}
	
	private void displayOpenExportsFolderDialog(final String title, final String message) {
		final int openExportsFolderSelection = UtilsLegacy.displayConfirmDialog(title, message);
		
		if(openExportsFolderSelection == JOptionPane.OK_OPTION) {
			performOpenExportsFolderAction();
		}
	}
	
	private void performConvertToMp4Action(final String absoluteVideoFilePath) throws InterruptedException {
		Thread convertMp4VideoThread = new Thread() {
			@Override
			public void run() {
				ThreadUtils.addThreadToHandleList("ConvertLocalVideo ConvertMp4", this);
								
				WindowsVideoFormatterMp4 windowVideoFormatterMp4 = new WindowsVideoFormatterMp4();
				
				try {
					windowVideoFormatterMp4.apply(absoluteVideoFilePath, mVideoFrameProgress, mProgressBundle);
					mVideoFrameProgress += windowVideoFormatterMp4.getNumberOfFramesProcessed();
				} catch (InterruptedException interruptedException) {
					//interruptedException.printStackTrace();
				}
								
				ThreadUtils.removeThreadFromHandleList(this);
			}
		};
		
		convertMp4VideoThread.start();
		convertMp4VideoThread.join();
	}
	
	private void performConvertToMovAction(final String absoluteVideoFilePath) throws InterruptedException {
		Thread convertMp4VideoThread = new Thread() {
			@Override
			public void run() {
				ThreadUtils.addThreadToHandleList("ConvertLocalVideo ConvertMov", this);
								
				WindowsVideoFormatterMov windowVideoFormatterMov = new WindowsVideoFormatterMov();
				
				try {
					windowVideoFormatterMov.apply(absoluteVideoFilePath, mVideoFrameProgress, mProgressBundle);
					mVideoFrameProgress += windowVideoFormatterMov.getNumberOfFramesProcessed();
				} catch (InterruptedException interruptedException) {
					//interruptedException.printStackTrace();
				}
								
				ThreadUtils.removeThreadFromHandleList(this);
			}
		};
		
		convertMp4VideoThread.start();
		convertMp4VideoThread.join();
	}
	
	private void performConvertToAviAction(final String absoluteVideoFilePath) throws InterruptedException {
		Thread convertMp4VideoThread = new Thread() {
			@Override
			public void run() {
				ThreadUtils.addThreadToHandleList("ConvertLocalVideo ConvertAvi", this);
								
				WindowsVideoFormatterAvi windowVideoFormatterAvi = new WindowsVideoFormatterAvi();
				
				try {
					windowVideoFormatterAvi.apply(absoluteVideoFilePath, mVideoFrameProgress, mProgressBundle);
					mVideoFrameProgress += windowVideoFormatterAvi.getNumberOfFramesProcessed();
				} catch (InterruptedException interruptedException) {
					//interruptedException.printStackTrace();
				}
								
				ThreadUtils.removeThreadFromHandleList(this);
			}
		};
		
		convertMp4VideoThread.start();
		convertMp4VideoThread.join();
	}
	
	private void convertVideo(final String absoluteVideoFilePath) throws InterruptedException {
		switch(VIDEO_TRIAGE_VIDEO_FORMAT_ENUM) {
			case MP4: {
				performConvertToMp4Action(absoluteVideoFilePath);
				
				break;
			}
			case MOV: {
				performConvertToMovAction(absoluteVideoFilePath);
				
				break;
			}
			case AVI: {
				performConvertToAviAction(absoluteVideoFilePath);
				
				break;
			}
		}
	}
	
	private void buildAndDisplayFailedToProcessDialog(ArrayList<String> listOfFailedAbsolutePaths) {
		FailedToProcessListWindow failedToProcessListWindow = new FailedToProcessListWindow(listOfFailedAbsolutePaths);
		failedToProcessListWindow.buildAndDisplay();
	}
	
	@Override
	public void start() {
		run();
	}
	
	@Override
	public void run() {
		ThreadUtils.addThreadToHandleList("ConvertLocalVideo Run", this);
		
		CursorUtils.setBusyCursor(CONVERT_LOCAL_FILE_WIZARD_WINDOW);
		
		final int videoFilePathListSize = VIDEO_FILE_PATH_LIST.size();
			
		int failedToProcessCount = 0;
		ArrayList<String> failedToProcessPathList = new ArrayList<String>();
				
		mVideoFrameTarget = 0;
		mVideoFrameProgress = 0;
		
		for(String absoluteVideoFilePath : VIDEO_FILE_PATH_LIST) {
			mVideoFrameTarget += WindowsVideoFrameExtractorLegacy.getVideoFrameCount(absoluteVideoFilePath);
		}
		
		if(videoFilePathListSize > 1) {
			mProgressBundle = ProgressUtils.getProgressBundle("Convert Videos Progress...", mVideoFrameTarget);
		} else {
			mProgressBundle = ProgressUtils.getProgressBundle("Convert Video Progress...", mVideoFrameTarget);
		}
		
		for(String absoluteVideoFilePath : VIDEO_FILE_PATH_LIST) {
			try {
				convertVideo(absoluteVideoFilePath);
			} catch (InterruptedException interruptedException) {
				//interruptedException.printStackTrace();
				
				failedToProcessCount++;
				failedToProcessPathList.add(absoluteVideoFilePath);
			}
		}
		
		mProgressBundle.progressBar.repaint();
		mProgressBundle.frame.dispose();
		
		if(failedToProcessCount > 0) {
			buildAndDisplayFailedToProcessDialog(failedToProcessPathList);
		}
		
		CursorUtils.setDefaultCursor(CONVERT_LOCAL_FILE_WIZARD_WINDOW);
		
		ThreadUtils.removeThreadFromHandleList(this);
		
		if(videoFilePathListSize > 1) {
			displayOpenExportsFolderDialog("Finished Converting Videos", "Open exports folder?");
		} else {
			displayOpenExportsFolderDialog("Finished Converting Video", "Open exports folder?");
		}
	}
}

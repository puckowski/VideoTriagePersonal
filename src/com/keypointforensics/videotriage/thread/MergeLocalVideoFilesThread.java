package com.keypointforensics.videotriage.thread;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.UUID;

import javax.swing.JOptionPane;

import com.keypointforensics.videotriage.casestore.CaseMetadataWriter;
import com.keypointforensics.videotriage.gui.localfile.wizard.video.EnhanceLocalFileWizardWindow;
import com.keypointforensics.videotriage.gui.localfile.wizard.video.FailedToProcessListWindow;
import com.keypointforensics.videotriage.legacy.FileUtilsLegacy;
import com.keypointforensics.videotriage.legacy.UtilsLegacy;
import com.keypointforensics.videotriage.legacy.WindowsVideoFrameExtractorLegacy;
import com.keypointforensics.videotriage.merge.MergeLocalFileWizardWindow;
import com.keypointforensics.videotriage.merge.UnmonitoredGenericVideoMergeThread;
import com.keypointforensics.videotriage.merge.WindowsResizeVideoForMergeProcessor;
import com.keypointforensics.videotriage.progress.ProgressBundle;
import com.keypointforensics.videotriage.util.CursorUtils;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ProgressUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.Utils;

public class MergeLocalVideoFilesThread extends Thread {

	/*
	 * Author: Daniel Puckowski
	 */
	
	private final MergeLocalFileWizardWindow MERGE_LOCAL_FILE_WIZARD_WINDOW;
	private final ArrayList<String>          VIDEO_FILE_PATH_LIST;
	private final int                        VIDEO_HEIGHT;
	private final int                        VIDEO_WIDTH;
	private final int                        VIDEO_FRAME_RATE;
	
	private String mContextFilename;
	
	private int mVideoFrameTarget;
	private int mVideoFrameProgress;
	private ProgressBundle mProgressBundle;
	
	private ArrayList<String> mPreprocessedVideoList;
	
	public MergeLocalVideoFilesThread(final MergeLocalFileWizardWindow mergeLocalFileWizardWindow,
			final ArrayList<String> videoFilePathList, final int videoHeight, final int videoWidth, final int videoFrameRate) {
		MERGE_LOCAL_FILE_WIZARD_WINDOW = mergeLocalFileWizardWindow;
		VIDEO_FILE_PATH_LIST           = videoFilePathList;
		VIDEO_HEIGHT                   = videoHeight;
		VIDEO_WIDTH                    = videoWidth;
		VIDEO_FRAME_RATE               = videoFrameRate;
		
		mPreprocessedVideoList = new ArrayList<String>();
	}
	
	public MergeLocalVideoFilesThread(final MergeLocalFileWizardWindow mergeLocalFileWizardWindow, final String contextFilename,
			final ArrayList<String> videoFilePathList, final int videoHeight, final int videoWidth, final int videoFrameRate) {
		MERGE_LOCAL_FILE_WIZARD_WINDOW = mergeLocalFileWizardWindow;
		VIDEO_FILE_PATH_LIST           = videoFilePathList;
		VIDEO_HEIGHT                   = videoHeight;
		VIDEO_WIDTH                    = videoWidth;
		VIDEO_FRAME_RATE               = videoFrameRate;
		
		mContextFilename = contextFilename;
		
		mPreprocessedVideoList = new ArrayList<String>();
	}
	
	public static void performOpenMergedVideoFolderAction() {
		final File file = new File(FileUtils.MERGED_DIRECTORY);
		final Desktop desktop = Desktop.getDesktop();
		
		try {
			desktop.browse(file.toURI());
		} catch (IOException ioException) {

		}
	}
	
	private void displayOpenMergedVideoFolderDialog(final String title, final String message) {
		final int openEnhancedVideoFolderSelection = UtilsLegacy.displayConfirmDialog(title, message);
		
		if(openEnhancedVideoFolderSelection == JOptionPane.OK_OPTION) {
			performOpenMergedVideoFolderAction();
		}
	}
	
	private void performResizeVideoForMergeAction(final String absoluteVideoFilePath) throws InterruptedException {
		Thread temporalAveragingDenoiserVideoThread = new Thread() {
			@Override
			public void run() {
				ThreadUtils.addThreadToHandleList("MergeLocalVideoFilesThread TemporalAveragingDenoiseVideo", this);
								
				WindowsResizeVideoForMergeProcessor windowsResizeVideoForMergeProcessor = new WindowsResizeVideoForMergeProcessor(VIDEO_HEIGHT, VIDEO_WIDTH, VIDEO_FRAME_RATE);

				try {
					mPreprocessedVideoList.add(windowsResizeVideoForMergeProcessor.apply(absoluteVideoFilePath, mVideoFrameProgress, mProgressBundle));
					mVideoFrameProgress += windowsResizeVideoForMergeProcessor.getNumberOfFramesProcessed();
				} catch (InterruptedException interruptedException) {
					//interruptedException.printStackTrace();
				}
				
				ThreadUtils.removeThreadFromHandleList(this);
			}
		};
		
		temporalAveragingDenoiserVideoThread.start();
		temporalAveragingDenoiserVideoThread.join();
	}
	
	private void preprocessVideoForMergeAction(final String absoluteVideoFilePath) throws InterruptedException {		
		performResizeVideoForMergeAction(absoluteVideoFilePath);
	}
	
	private void buildAndDisplayFailedToProcessDialog(ArrayList<String> listOfFailedAbsolutePaths) {
		FailedToProcessListWindow failedToProcessListWindow = new FailedToProcessListWindow(listOfFailedAbsolutePaths);
		failedToProcessListWindow.buildAndDisplay();
	}
	
	@Override
	public void start() {
		run();
	}
	
	private int getNextMergedIndex() {
		ArrayList<String> listOfFilesInMerged = (ArrayList<String>) FileUtilsLegacy.parseDirectoryRecursiveForAll(FileUtils.MERGED_DIRECTORY);
		
		int index = 0;
		
		for(String fileInMerged : listOfFilesInMerged) {
			if(fileInMerged.contains("_merged_") == true && fileInMerged.endsWith(".mp4") == true) {
				index++;
			}
		}
		
		return index;
	}
	
	@Override
	public void run() {
		ThreadUtils.addThreadToHandleList("MergeLocalVideoFilesThread Run", this);
		
		CursorUtils.setBusyCursor(MERGE_LOCAL_FILE_WIZARD_WINDOW);
		
		//ProgressBundle deleteProgressBundle = null;
		final int videoFilePathListSize = VIDEO_FILE_PATH_LIST.size();
		
		//if(videoFilePathListSize > 1) {
		//	deleteProgressBundle = ProgressUtils.getProgressBundle("Enhance Videos Progress...", VIDEO_FILE_PATH_LIST.size());
		//}
			
		int failedToProcessCount = 0;
		ArrayList<String> failedToProcessPathList = new ArrayList<String>();
				
		mVideoFrameTarget = 0;
		mVideoFrameProgress = 0;
		
		for(String absoluteVideoFilePath : VIDEO_FILE_PATH_LIST) {
			mVideoFrameTarget += Math.floor(WindowsVideoFrameExtractorLegacy.getVideoDurationInSeconds(absoluteVideoFilePath) * VIDEO_FRAME_RATE);
		}
		
		if(videoFilePathListSize > 1) {
			mProgressBundle = ProgressUtils.getProgressBundle("Merge Videos Progress...", mVideoFrameTarget);
		} else {
			mProgressBundle = ProgressUtils.getProgressBundle("Merge Video Progress...", mVideoFrameTarget);
		}
		
		for(String absoluteVideoFilePath : VIDEO_FILE_PATH_LIST) {
			try {
				preprocessVideoForMergeAction(absoluteVideoFilePath);
			} catch (InterruptedException interruptedException) {
				//interruptedException.printStackTrace();
				
				failedToProcessCount++;
				failedToProcessPathList.add(absoluteVideoFilePath);
			}
			
			//if(deleteProgressBundle != null) {
			//	deleteProgressBundle.progressBar.setValue(deleteProgressBundle.progressBar.getValue() + 1);
			//	deleteProgressBundle.progressBar.repaint();
			//}
		}
		
		//if(deleteProgressBundle != null) {
		//	deleteProgressBundle.progressBar.setValue(deleteProgressBundle.progressBar.getValue() + 1);
		//	deleteProgressBundle.progressBar.repaint();
		//	deleteProgressBundle.frame.dispose();
		//}
		
		mProgressBundle.progressBar.setIndeterminate(true);
		mProgressBundle.progressBar.repaint();
		
		UUID uuid = UUID.randomUUID();
        String randomUUIDString = uuid.toString();
        
		PrintWriter preprocessedListWriter;
		final String preprocessedListFilename = FileUtils.MERGED_DIRECTORY + randomUUIDString + ".txt";
		//final String outputFilename = randomUUIDString.substring(0, randomUUIDString.indexOf("-")) + "_merged_" + getNextMergedIndex() + ".mp4";
		final String outputFilename = Utils.getTimeStamp() + "_merged_" + getNextMergedIndex() + ".mp4";
		
		try {
			preprocessedListWriter = new PrintWriter(preprocessedListFilename);
			
			for(String preprocessedVideoFile : mPreprocessedVideoList) {
				preprocessedListWriter.println("file '" + preprocessedVideoFile + "'");
			}
			
			preprocessedListWriter.flush();
			preprocessedListWriter.close();
			
			final String command = "\"" + FileUtilsLegacy.BINARIES_DIRECTORY + WindowsVideoFrameExtractorLegacy.FFMPEG4_EXECUTABLE_NAME + "\" -threads 2 " + 
				"-f concat -safe 0 -i \"" + preprocessedListFilename + "\" -c copy \"" + outputFilename + "\"";

			UnmonitoredGenericVideoMergeThread unmonitoredGenericVideoMergeThread =  new UnmonitoredGenericVideoMergeThread(command);
			unmonitoredGenericVideoMergeThread.start();
			unmonitoredGenericVideoMergeThread.join();
			
			if(mContextFilename != null) {
				CaseMetadataWriter.writeNewMergedSourceToContext(mContextFilename, FileUtils.MERGED_DIRECTORY + outputFilename);
			}
		} catch (FileNotFoundException fileNotFoundException) {
			//fileNotFoundException.printStackTrace();
			
			failedToProcessCount++;
			failedToProcessPathList.add(outputFilename);
		} catch (InterruptedException interruptedException) {
			//interruptedException.printStackTrace();
			
			failedToProcessCount++;
			failedToProcessPathList.add(outputFilename);
		}
		
		for(String preprocessedVideoFile : mPreprocessedVideoList) {
			FileUtils.deleteFile(new File(preprocessedVideoFile));
		}
		
		File deleteProcessedList = new File(preprocessedListFilename);
		deleteProcessedList.delete();
		
		mProgressBundle.progressBar.repaint();
		mProgressBundle.frame.dispose();
		
		if(failedToProcessCount > 0) {
			buildAndDisplayFailedToProcessDialog(failedToProcessPathList);
		}
		
		CursorUtils.setDefaultCursor(MERGE_LOCAL_FILE_WIZARD_WINDOW);
		
		ThreadUtils.removeThreadFromHandleList(this);
		
		if(videoFilePathListSize > 1) {
			displayOpenMergedVideoFolderDialog("Finished Merging Videos", "Open merged video folder?");
		} else {
			displayOpenMergedVideoFolderDialog("Finished Merging Video", "Open merged video folder?");
		}
	}
}

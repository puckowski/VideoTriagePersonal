package com.keypointforensics.videotriage.thread;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.keypointforensics.videotriage.gui.main.GuiMain;
import com.keypointforensics.videotriage.legacy.UtilsLegacy;
import com.keypointforensics.videotriage.legacy.WindowsVideoFrameExtractorLegacy;
import com.keypointforensics.videotriage.progress.ProgressBundle;
import com.keypointforensics.videotriage.util.CursorUtils;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ImageUtils;
import com.keypointforensics.videotriage.util.ProgressUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;

public class CreateVideoPreviewImageThread extends Thread {

	private final GuiMain GUI_MAIN;
	private final ArrayList<String> VIDEO_FILES_TO_PREVIEW;
	private final int NUMBER_OF_PREVIEW_IMAGES;
	private final int NUMBER_OF_ROWS;
	private final int NUMBER_OF_COLUMNS;
	
	public CreateVideoPreviewImageThread(final GuiMain guiMain, final ArrayList<String> videoFilesToPreview, final int numberOfPreviewImages) {
		GUI_MAIN                 = guiMain;
		VIDEO_FILES_TO_PREVIEW   = videoFilesToPreview;
		NUMBER_OF_PREVIEW_IMAGES = numberOfPreviewImages;
		NUMBER_OF_ROWS           = (int) Math.sqrt(numberOfPreviewImages);
		NUMBER_OF_COLUMNS        = NUMBER_OF_ROWS;
	}
	
	private void performOpenPreviewFolderAction() {
		final File file = new File(FileUtils.PREVIEWS_DIRECTORY);
		final Desktop desktop = Desktop.getDesktop();
		
		try {
			desktop.browse(file.toURI());
		} catch (IOException ioException) {

		}
	}
	
	@Override	
	public void start() {
		run();
	}
	
	@Override
	public void run() {
		ThreadUtils.addThreadToHandleList("CreateVidPreviewImage Run", this);
		
		CursorUtils.setBusyCursor(GUI_MAIN);
		
		ProgressBundle progressBundle = ProgressUtils.getProgressBundle("Creating Video Previews...", VIDEO_FILES_TO_PREVIEW.size());
		
		String videoToCreatePreviewFor;
		String videoToCreateWithoutExtension;
		String outputFileString;
		File checkOutputFile;
		
		for(int i = 0; i < VIDEO_FILES_TO_PREVIEW.size(); ++i) {
			videoToCreatePreviewFor = VIDEO_FILES_TO_PREVIEW.get(i);
		
			videoToCreateWithoutExtension = FileUtils.getShortFilename(videoToCreatePreviewFor);
			
			if(videoToCreateWithoutExtension.contains(".") == true) {
				videoToCreateWithoutExtension = videoToCreateWithoutExtension.substring(0, videoToCreateWithoutExtension.lastIndexOf("."));
			}
			
			videoToCreateWithoutExtension += "_preview";
			
			try {
				ArrayList<String> files = WindowsVideoFrameExtractorLegacy.extractPreviewFramesJoinedByIndexWithAbsoluteList(videoToCreatePreviewFor, NUMBER_OF_PREVIEW_IMAGES);
				
				outputFileString = FileUtils.PREVIEWS_DIRECTORY + videoToCreateWithoutExtension + ".jpg";
				checkOutputFile = new File(outputFileString);
				
				if(checkOutputFile.exists() == true) {
					checkOutputFile.delete();
				}
				
				ImageUtils.getMontageUnmonitored(outputFileString, files, NUMBER_OF_ROWS, NUMBER_OF_COLUMNS);
			} catch (InterruptedException interruptedException) {
				//interruptedException.printStackTrace();
			}
			
			progressBundle.progressBar.setValue((i + 1));
			progressBundle.progressBar.repaint();
		}
		
		progressBundle.progressBar.repaint();
		progressBundle.frame.dispose();
		
		CursorUtils.setDefaultCursor(GUI_MAIN);
		
		ThreadUtils.removeThreadFromHandleList(this);
		
		final int openPreviewFolderChoice = UtilsLegacy.displayConfirmDialog("Finished Processing", "Open preview folder?");
		
		if(openPreviewFolderChoice == JOptionPane.OK_OPTION) {			
			performOpenPreviewFolderAction();
		} else {
			return;
		}
	}
}

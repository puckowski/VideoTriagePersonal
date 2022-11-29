package com.keypointforensics.videotriage.thread;

import java.io.File;
import java.util.ArrayList;

import com.keypointforensics.videotriage.legacy.WindowsVideoFrameExtractorLegacy;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ImageUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;

public class CreateSingleVideoPreviewImageThread extends Thread {

	private final String VIDEO_FILE_TO_PREVIEW;
	private final int NUMBER_OF_PREVIEW_IMAGES;
	private final int NUMBER_OF_ROWS;
	private final int NUMBER_OF_COLUMNS;
	
	private String mPreviewImage;
	
	public CreateSingleVideoPreviewImageThread(final String videoFileToPreview, final int numberOfPreviewImages) {
		VIDEO_FILE_TO_PREVIEW    = videoFileToPreview;
		NUMBER_OF_PREVIEW_IMAGES = numberOfPreviewImages;
		NUMBER_OF_ROWS           = (int) Math.sqrt(numberOfPreviewImages);
		NUMBER_OF_COLUMNS        = NUMBER_OF_ROWS;
	}
	
	@Override	
	public void start() {
		run();
	}
	
	public String getPreviewImageAbsolutePath() {
		return mPreviewImage;
	}
	
	@Override
	public void run() {
		ThreadUtils.addThreadToHandleList("CreateSingleVidPreviewImage Run", this);
						
		String videoToCreateWithoutExtension;
		File checkOutputFile;
				
		videoToCreateWithoutExtension = FileUtils.getShortFilename(VIDEO_FILE_TO_PREVIEW);
			
		if(videoToCreateWithoutExtension.contains(".") == true) {
			videoToCreateWithoutExtension = videoToCreateWithoutExtension.substring(0, videoToCreateWithoutExtension.lastIndexOf("."));
		}
			
		videoToCreateWithoutExtension += "_preview";
			
		try {
			ArrayList<String> files = WindowsVideoFrameExtractorLegacy.extractPreviewFramesJoinedByIndexWithAbsoluteList(VIDEO_FILE_TO_PREVIEW, NUMBER_OF_PREVIEW_IMAGES);
				
			mPreviewImage = FileUtils.PREVIEWS_DIRECTORY + videoToCreateWithoutExtension + ".jpg";
			checkOutputFile = new File(mPreviewImage);
				
			if(checkOutputFile.exists() == true) {
				checkOutputFile.delete();
			}
				
			ImageUtils.getMontageUnmonitored(mPreviewImage, files, NUMBER_OF_ROWS, NUMBER_OF_COLUMNS);
		} catch (InterruptedException interruptedException) {
			//interruptedException.printStackTrace();
		}
				
		ThreadUtils.removeThreadFromHandleList(this);
	}
}

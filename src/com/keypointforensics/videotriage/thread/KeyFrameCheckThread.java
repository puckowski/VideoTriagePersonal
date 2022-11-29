package com.keypointforensics.videotriage.thread;

import java.io.File;
import java.util.ArrayList;

import com.keypointforensics.videotriage.legacy.FileUtilsLegacy;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;

public class KeyFrameCheckThread extends Thread {

	/*
	 * Author: Daniel Puckowski
	 */	
	
	private final String BLOB_CONTEXT_FILE_NAME;
		
	private boolean mModifiedContextFile;
	
	public KeyFrameCheckThread(final String blobContextFileName) {
		BLOB_CONTEXT_FILE_NAME = blobContextFileName;
	}
	
	public boolean getModifiedContextFile() {
		return mModifiedContextFile;
	}
	
	@Override
	public void start() {
		run();
	}
	
	@Override
	public void run() {			
		ThreadUtils.addThreadToHandleList("KeyFrameCheckThread Run", this);
		
		ArrayList<String> processingDirectoryListing = (ArrayList<String>) FileUtilsLegacy.parseDirectory(FileUtils.PROCESSING_DIRECTORY);
		
		File keyFrameFile;
		
		for(String potentialKeyFrameList : processingDirectoryListing) {
			if(potentialKeyFrameList.endsWith(CorrectKeyFrameExtractThread.KEYFRAME_TIME_FILENAME_EXTENSION) == true) {		
				keyFrameFile = new File(potentialKeyFrameList);
					
				CorrectKeyFrameExtractThread correctKeyFrameExtractThread = new CorrectKeyFrameExtractThread(keyFrameFile.getAbsolutePath(), BLOB_CONTEXT_FILE_NAME);
				correctKeyFrameExtractThread.start();
						
				try {
					correctKeyFrameExtractThread.join();
				} catch (InterruptedException interruptedException) {
					interruptedException.printStackTrace();
				}
										
				keyFrameFile.delete();
						
				mModifiedContextFile = true;
			}
		}
		
		ThreadUtils.removeThreadFromHandleList(this);
	}
}
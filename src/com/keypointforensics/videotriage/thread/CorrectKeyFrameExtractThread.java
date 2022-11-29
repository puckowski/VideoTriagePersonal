package com.keypointforensics.videotriage.thread;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.keypointforensics.videotriage.legacy.FileUtilsLegacy;
import com.keypointforensics.videotriage.legacy.WindowsVideoFrameExtractorLegacy;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;

public class CorrectKeyFrameExtractThread extends Thread {

	/*
	 * Author: Daniel Puckowski
	 */
	public static final String KEYFRAME_TIME_FILENAME_EXTENSION        = "_keyframe_list.txt";
	public static final int    KEYFRAME_TIME_FILENAME_EXTENSION_LENGTH = 18;
	
	private final String KEYFRAME_ABSOLUTE_PATH;	
	private final String BLOB_CONTEXT_FILE_NAME;
	private final String CAPTURE_FOLDER_NAME;
	
	public CorrectKeyFrameExtractThread(String keyframeAbsolutePath, String blobContextFileName) {	
		File checkTemporaryContextFile = new File(blobContextFileName + ".tmp");
		
		if(checkTemporaryContextFile.exists() == true) {
			blobContextFileName += ".tmp";
		}
		
		KEYFRAME_ABSOLUTE_PATH = keyframeAbsolutePath;
		BLOB_CONTEXT_FILE_NAME = blobContextFileName;
		
		String keyFrameVideoFile = FileUtils.getShortFilename(keyframeAbsolutePath);
		keyFrameVideoFile = keyFrameVideoFile.substring(0, keyFrameVideoFile.length() - KEYFRAME_TIME_FILENAME_EXTENSION_LENGTH);
		
		CAPTURE_FOLDER_NAME = FileUtils.EXTRACTS_DIRECTORY + keyFrameVideoFile + File.separator;
	}

	@Override
	public void start() {
		run();
	}
	
	private void readTimeStampData(final ArrayList<String> timeStampList) {
		try(BufferedReader br = new BufferedReader(new FileReader(new File(KEYFRAME_ABSOLUTE_PATH)))) {
		    for(String line; (line = br.readLine()) != null; ) {
		    	if(line.isEmpty()) {
		    		continue;
		    	}

		    	line = line.substring(line.lastIndexOf(":") + 1, line.length());
		        line = line.substring(0, line.indexOf("."));
		        
		        timeStampList.add(line);
		    }
		} catch (FileNotFoundException fileNotFoundException) {
			fileNotFoundException.printStackTrace();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}
	
	private void readBlobContextLines(final ArrayList<String> blobContextLines) {
		try(BufferedReader br = new BufferedReader(new FileReader(new File(BLOB_CONTEXT_FILE_NAME)))) {
		    for(String line; (line = br.readLine()) != null; ) {
		    	if(line.isEmpty()) {
		    		continue;
		    	}
		    	
		    	blobContextLines.add(line);
		    }
		} catch (FileNotFoundException fileNotFoundException) {
			fileNotFoundException.printStackTrace();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}
	
	private void reconstructBlobContextFile(final ArrayList<String> extractFilesAndKeyFile, final ArrayList<String> timeStampList, final ArrayList<String> blobContextLines) {
		File currentFile;
		String currentFileName;
		String originalFilename;
		int frameIndex;
		
		HashMap<String, String> replacementMap = new HashMap<String, String>();
		
		for(int i = 0; i < extractFilesAndKeyFile.size(); ++i) {
			currentFile = new File(extractFilesAndKeyFile.get(i));
			originalFilename = currentFile.getAbsolutePath();
			
			frameIndex = Integer.parseInt(timeStampList.get(i));
			currentFileName = CAPTURE_FOLDER_NAME + "image-" + String.format("%07d", frameIndex) + ".jpeg";

			currentFile.renameTo(new File(currentFileName));
			replacementMap.put(originalFilename, currentFileName);
		}
		
		for(int n = 0; n < blobContextLines.size(); ++n) {
			if(replacementMap.containsKey(blobContextLines.get(n))) {
				blobContextLines.set(n, replacementMap.get(blobContextLines.get(n)));
			}
		}
	}
	
	private void createKeyframeNoticeFile() {
		File keyframeNoticeFile = new File(FileUtils.PROCESSING_DIRECTORY + FileUtils.getLastDirectory(CAPTURE_FOLDER_NAME) + WindowsVideoFrameExtractorLegacy.KEYFRAME_NOTICE_FILENAME_EXTENSION);
		try {
			keyframeNoticeFile.createNewFile();
		} catch (IOException ioException) {
			//ioException.printStackTrace();
		}
	}
	
	@Override
	public void run() {			
		ThreadUtils.addThreadToHandleList("CorrectKeyFrameExtractThread Run", this);
		
		ArrayList<String> timeStampList = new ArrayList<String>();
		readTimeStampData(timeStampList);
		
		//
		ArrayList<String> blobContextLines = new ArrayList<String>();
		readBlobContextLines(blobContextLines);
		//
		
		ArrayList<String> extractFiles = (ArrayList<String>) FileUtilsLegacy.parseDirectoryRecursiveForAll(CAPTURE_FOLDER_NAME);
		Collections.sort(extractFiles);
		
		reconstructBlobContextFile(extractFiles, timeStampList, blobContextLines);
		
		PrintWriter newBlobContextFileWriter;
		
		String outputContextFilename = BLOB_CONTEXT_FILE_NAME;
		
		if(outputContextFilename.endsWith(".tmp") == false) {
			outputContextFilename += ".tmp";
		}
		
		try {
			newBlobContextFileWriter = new PrintWriter(outputContextFilename);
			
			for(int i = 0; i < blobContextLines.size(); ++i) {
				newBlobContextFileWriter.println(blobContextLines.get(i));
			}
			
			newBlobContextFileWriter.flush();
			newBlobContextFileWriter.close();
		} catch (FileNotFoundException fileNotFoundException) {
			//fileNotFoundException.printStackTrace();
		}
		
		createKeyframeNoticeFile();
		
		ThreadUtils.removeThreadFromHandleList(this);
	}
}
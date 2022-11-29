package com.keypointforensics.videotriage.thread;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

import com.keypointforensics.videotriage.util.ThreadUtils;

public class ExtractVideoFrameThread extends Thread {

	/*
	 * Author: Daniel Puckowski
	 */
		
	public static HashSet<String> mExtractionSet;
	
	private final String CAPTURE_FOLDER_NAME;
	private final String COMMAND;
	
	private File mDeleteOnTerminationFile;
	
	public ExtractVideoFrameThread(final String captureFolderName, final String command) {
		CAPTURE_FOLDER_NAME = captureFolderName;
		COMMAND = command;
	}
	
	public static void initializeExtractionSet() {
		mExtractionSet = new HashSet<String>();
	}
	
	public ExtractVideoFrameThread(final String captureFolderName, final String command, final File deleteOnTerminationFile) {
		CAPTURE_FOLDER_NAME = captureFolderName;
		COMMAND = command;
		
		mDeleteOnTerminationFile = deleteOnTerminationFile;
	}
	
	private void changeDirectoryAndExecute() throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", "cd \"" + CAPTURE_FOLDER_NAME + "\" && dir && " + COMMAND);
		processBuilder.redirectErrorStream(true);
		
		Process process = processBuilder.start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
	
		while (true) {
			line = reader.readLine();

			if (line == null) {
				break;
			}
		}
	}
	
	@Override
	public void run() {	
		ThreadUtils.addThreadToHandleList("ExtractFrame Run", this);

		mExtractionSet.add(CAPTURE_FOLDER_NAME);
		
		try {
			changeDirectoryAndExecute();
		} catch (IOException ioException) {
			
		}
		
		if(mDeleteOnTerminationFile != null) {
			mDeleteOnTerminationFile.delete();
		}
		
		mExtractionSet.remove(CAPTURE_FOLDER_NAME);
		
		ThreadUtils.removeThreadFromHandleList(this);
	}
}

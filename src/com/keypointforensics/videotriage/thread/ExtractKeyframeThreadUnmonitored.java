package com.keypointforensics.videotriage.thread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.keypointforensics.videotriage.util.ThreadUtils;

public class ExtractKeyframeThreadUnmonitored extends Thread {

	/*
	 * Author: Daniel Puckowski
	 */
			
	private final String CAPTURE_FOLDER_NAME;
	private final String COMMAND;

	public ExtractKeyframeThreadUnmonitored(final String captureFolderName, final String command) {
		CAPTURE_FOLDER_NAME = captureFolderName;
		COMMAND             = command;
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
		ThreadUtils.addThreadToHandleList("ExtractKeyframeUnmonitored Run", this);
		
		try {
			changeDirectoryAndExecute();
		} catch (IOException ioException) {
			
		}
		
		ThreadUtils.removeThreadFromHandleList(this);
	}
}

package com.keypointforensics.videotriage.thread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;

public class VideoDeshakeFilterThreadUnmonitored extends Thread {

	/*
	 * Author: Daniel Puckowski
	 */
	
	private final String COMMAND;
		
	public VideoDeshakeFilterThreadUnmonitored(final String command) {
		COMMAND                  = command;
	}

	private void execute() throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", "cd \"" + FileUtils.ENHANCED_DIRECTORY + "\" && dir && " + COMMAND);
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
	public void start() {
		run();
	}
	
	@Override
	public void run() {	
		ThreadUtils.addThreadToHandleList("VideoDeshakeFilter Run", this);
				
		try {
			execute();
		} catch (IOException ioException) {
			
		}
				
		ThreadUtils.removeThreadFromHandleList(this);
	}
}

package com.keypointforensics.videotriage.thread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;

public class ExtractVideoPreviewFrameThreadUnmonitored extends Thread {

	private final String COMMAND;
	
	public ExtractVideoPreviewFrameThreadUnmonitored(final String command) {
		COMMAND = command;
	}

	private void execute() throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", "cd \"" + FileUtils.TEMPORARY_DIRECTORY + "\" && dir && " + COMMAND);
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
		ThreadUtils.addThreadToHandleList("ExtractPreviewFrameUnmon Run", this);
				
		try {
			execute();
		} catch (IOException ioException) {
			
		}
				
		ThreadUtils.removeThreadFromHandleList(this);
	}
}

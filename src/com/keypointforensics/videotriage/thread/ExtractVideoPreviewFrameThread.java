package com.keypointforensics.videotriage.thread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.keypointforensics.videotriage.progress.ProgressBundle;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;

public class ExtractVideoPreviewFrameThread extends Thread {

	private final String         COMMAND;
	private final ProgressBundle PROGRESS_BUNDLE;
	
	public ExtractVideoPreviewFrameThread(final String command, final ProgressBundle progressBundle) {
		COMMAND = command;
		PROGRESS_BUNDLE = progressBundle;
	}

	private void execute() throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", "cd \"" + FileUtils.TEMPORARY_DIRECTORY + "\" && dir && " + COMMAND);
		processBuilder.redirectErrorStream(true);

		Process process = processBuilder.start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		int progress;
		
		while (true) {
			line = reader.readLine();
						
			if (line == null) {
				break;
			} else if(line.startsWith("frame=") == true) {
				line = line.substring(6, line.length()).trim();
				
				progress = Integer.parseInt(line.substring(0, line.indexOf(" ")));
				
				PROGRESS_BUNDLE.progressBar.setValue(progress);
				PROGRESS_BUNDLE.progressBar.repaint();
			}
		}
		
		PROGRESS_BUNDLE.frame.dispose();
	}
	
	@Override
	public void start() {
		run();
	}
	
	@Override
	public void run() {	
		ThreadUtils.addThreadToHandleList("ExtractPreviewFrame Run", this);
				
		try {
			execute();
		} catch (IOException ioException) {
			
		}
				
		ThreadUtils.removeThreadFromHandleList(this);
	}
}

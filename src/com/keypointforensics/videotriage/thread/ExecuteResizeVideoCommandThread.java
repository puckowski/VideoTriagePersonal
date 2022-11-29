package com.keypointforensics.videotriage.thread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.keypointforensics.videotriage.progress.ProgressBundle;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;

public class ExecuteResizeVideoCommandThread extends Thread {

	/*
	 * Author: Daniel Puckowski
	 */
			
	private final String         COMMAND;
	private final int            VIDEO_FRAME_COUNT_OFFSET;
	private final ProgressBundle PROGRESS_BUNDLE;
	
	private int mFrameProgress;
	
	public ExecuteResizeVideoCommandThread(final String command, final int videoFrameCountOffset, final ProgressBundle progressBundle) {
		COMMAND                  = command;
		VIDEO_FRAME_COUNT_OFFSET = videoFrameCountOffset;
		PROGRESS_BUNDLE          = progressBundle;
		
		mFrameProgress = 0;
	}

	public int getFinalFrameCount() {
		return mFrameProgress;
	}
	
	private void changeDirectoryAndExecute() throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", "cd \"" + FileUtils.TEMPORARY_DIRECTORY + "\" && dir && " + COMMAND);
		processBuilder.redirectErrorStream(true);
		
		Process process = processBuilder.start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		
		while (true) {
			line = reader.readLine();
						
			if(line != null && line.startsWith("frame=") == true && line.contains("fps") == true) {
				line = line.substring(0, line.indexOf("fps"));
				line = line.substring(line.indexOf("=") + 1, line.length()).trim();
				
				mFrameProgress = Integer.parseInt(line);
				
				PROGRESS_BUNDLE.progressBar.setValue(VIDEO_FRAME_COUNT_OFFSET + mFrameProgress);
				PROGRESS_BUNDLE.progressBar.repaint();
			}
			
			if (line == null) {
				break;
			}
		}
	}
	
	@Override
	public void run() {	
		ThreadUtils.addThreadToHandleList("ExecuteResize Run", this);
		
		try {
			changeDirectoryAndExecute();
		} catch (IOException ioException) {
			
		}
		
		ThreadUtils.removeThreadFromHandleList(this);
	}
}

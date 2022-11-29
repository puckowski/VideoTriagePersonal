package com.keypointforensics.videotriage.thread;

import com.keypointforensics.videotriage.gui.main.GuiMain;
import com.keypointforensics.videotriage.local.LocalViewProcessor;
import com.keypointforensics.videotriage.util.ThreadUtils;

public class ProgressBarUpdateThread extends Thread {

	/*
	 * Author: Daniel Puckowski
	 */

	private final GuiMain GUI_MAIN;
	
	private LocalViewProcessor mLocalViewProcessor;
	
	private String mLastControllerId;
	private String mControllerId;
	private boolean mRunning;
	
	public ProgressBarUpdateThread(final GuiMain guiMain) {
		GUI_MAIN = guiMain;
	}
	
	public void setRunning(final boolean isRunning) {
		mRunning = isRunning;
	}
	
	public void setLocalViewProcessor(String newControllerId) {		
		mLastControllerId = mControllerId;
		mControllerId = newControllerId;
		
		GUI_MAIN.setProgressBarId(mControllerId);
		
		GUI_MAIN.getProgressBar().setMaximum(0);
		GUI_MAIN.getProgressBar().setValue(0);
		GUI_MAIN.getProgressBar().setIndeterminate(false);
		GUI_MAIN.getProgressBar().repaint();
	}
	
	@Override
	public void run() {
		//ThreadUtils.addThreadToHandleList("ProgressUpdate Run", this);
		
		mRunning = true;
	
		while (Thread.currentThread().isInterrupted() == false && mRunning) {
			if(mControllerId == null) {
				Thread.yield();

				ThreadUtils.blockThread(100, "Busy pause");
				
				continue;
			}
			
			if(mLocalViewProcessor == null) {
				mLocalViewProcessor = GUI_MAIN.getController(mControllerId).getLocalViewProcessor();
				
				continue;
			}
			else if(mControllerId.equals(mLastControllerId) == false) {
				mLocalViewProcessor = GUI_MAIN.getController(mControllerId).getLocalViewProcessor();
				mLastControllerId = mControllerId;
				
				continue;
			}
			else if(mLocalViewProcessor.isRunning() == false) {
				GUI_MAIN.getProgressBar().setMaximum(0);
				GUI_MAIN.getProgressBar().setValue(0);
				GUI_MAIN.getProgressBar().setIndeterminate(false);
				GUI_MAIN.getProgressBar().repaint();
				
				mLocalViewProcessor = null;
				mControllerId = null;
				
				continue;
			}
			else if(mLocalViewProcessor.isProgressIndeterminate() == true) {
				GUI_MAIN.getProgressBar().setIndeterminate(true);
			}
			else {
				GUI_MAIN.getProgressBar().setIndeterminate(false);
				GUI_MAIN.getProgressBar().setMaximum(mLocalViewProcessor.getProgressTarget());
				GUI_MAIN.getProgressBar().setValue(mLocalViewProcessor.getProgress());
			}
			
			GUI_MAIN.getProgressBar().repaint();
			
			ThreadUtils.blockThread(50, "Active pause");
		}
		
		mRunning = false;
		
		//ThreadUtils.removeThreadFromHandleList(this);
	}
}

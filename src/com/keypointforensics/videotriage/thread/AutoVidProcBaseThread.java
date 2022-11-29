package com.keypointforensics.videotriage.thread;

public class AutoVidProcBaseThread extends Thread {

	/*
	 * Author: Daniel Puckowski
	 */
	
	private volatile boolean mRunning;
	
	public AutoVidProcBaseThread() {
		mRunning = true;
	}
	
	public boolean isRunning() {
		return mRunning;
	}
	
	public void setRunning(final boolean newRunState) {
		mRunning = newRunState;
	}
	
	public void stopRunning() {
		mRunning = false;
		this.interrupt();
	}
	
	@Override 
	public void run() {		
		return;
	}
}

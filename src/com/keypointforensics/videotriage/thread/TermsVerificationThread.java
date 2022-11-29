package com.keypointforensics.videotriage.thread;

import com.keypointforensics.videotriage.util.ThreadUtils;

public class TermsVerificationThread extends Thread {

	/*
	 * Author: Daniel Puckowski
	 */
	
	private volatile boolean mRunning;
	private volatile boolean mSuccess;
	
	public TermsVerificationThread() {
	}
	
	public boolean isRunning() {
		return mRunning;
	}
	
	public boolean verificationSuccessful() {
		return mSuccess;
	}
	
	public void verificationAttempted(final boolean success) {
		mRunning = false;
		mSuccess = success;
	}
	
	@Override
	public void run() {		
		mRunning = true;
		
		while(mRunning == true) {
			Thread.yield();
			
			ThreadUtils.blockThread(50, "Busy pause");
		}
	}
}

package com.keypointforensics.videotriage.thread;

public class ThreadCountMonitorThread extends Thread {

	/*
	 * Author: Daniel Puckowski
	 */
	
	private boolean mRunning;
	
	public ThreadCountMonitorThread() {
		//mRunning = true;
	}
	
	public void stopRunning() {		
		mRunning = false;
	}
	
	@Override
	public void run() {		
		int currentThreadCount = 0;
		
		/*
		while(mRunning) {
			currentThreadCount = ManagementFactory.getThreadMXBean().getThreadCount();
			
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
			
			}
		}
		*/
	}
}

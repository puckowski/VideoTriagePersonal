package com.keypointforensics.videotriage.legacy;

public interface FrsThreadLegacy extends Runnable {

	/*
	 * Author: Daniel Puckowski
	 */
	
	public static final int PROBE   = 0;
	
	public static final int COMPARE = 1;
	
	public void run();
	public void cleanup();
	public int getObjectCount();
	public int numberOfObjectsComputed();
	public boolean hasFinishedComputing();
}

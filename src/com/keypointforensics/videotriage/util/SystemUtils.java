package com.keypointforensics.videotriage.util;

public class SystemUtils {
	
	/*
	 * Author: Daniel Puckowski
	 */
	
	public static int getNumberOfSystemCores() {
		final int numberOfCores = Runtime.getRuntime().availableProcessors();
				
		return numberOfCores; 
	}
}
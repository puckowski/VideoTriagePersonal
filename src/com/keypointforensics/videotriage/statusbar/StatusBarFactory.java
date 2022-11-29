package com.keypointforensics.videotriage.statusbar;

public class StatusBarFactory {

	/*
	 * Author: Daniel Puckowski
	 */
	
	public static final String REMOTE_STATUS_BAR = "Remote";
	public static final String LOCAL_STATUS_BAR  = "Local";
	
	public StatusBar getStatusBar(final String statusBarName, final String controllerId) {
		if(statusBarName.equals(REMOTE_STATUS_BAR)) {			
			return (StatusBar) new RemoteStatusBar(controllerId);
		}
		else if(statusBarName.equals(LOCAL_STATUS_BAR)) {			
			return (StatusBar) new LocalStatusBar(controllerId);
		}
				
		return null;
	}
}

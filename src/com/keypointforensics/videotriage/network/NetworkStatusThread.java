package com.keypointforensics.videotriage.network;

import java.net.URL;
import java.net.URLConnection;

import com.keypointforensics.videotriage.util.ThreadUtils;

public class NetworkStatusThread extends Thread {

	/*
	 * Author: Daniel Puckowski
	 */
	
	private final int DEFAULT_ACCEPTABLE_FAIL_COUNT = 4;    
	private final int DEFAULT_SLEEP_MILLIS          = 4000; 
	
	private final int ACCEPTABLE_FAIL_COUNT;
	
	private URL     mUrl;
	private boolean mStatusOk;
	private int     mFailCount;

	public NetworkStatusThread(final URL urlToMonitor) {
		ACCEPTABLE_FAIL_COUNT = DEFAULT_ACCEPTABLE_FAIL_COUNT;
		
		mUrl      = urlToMonitor;
		mStatusOk = true; 
	}

	public boolean isOk() {
		return mStatusOk;
	}
	
	public void run() {
		ThreadUtils.addThreadToHandleList("Network Run", this);
		
		if(mUrl == null) {
			mStatusOk = false;
			
			ThreadUtils.removeThreadFromHandleList(this);
			
			return;
		}
				
		while(Thread.currentThread().isInterrupted() == false && mStatusOk == true) {
			try {
				URLConnection myUrlConnection = mUrl.openConnection();
				myUrlConnection.connect();
				
				if(mFailCount > 0) {					
					mFailCount--;
				}
			} catch(Exception e) {				
				mFailCount++;
			}
			
			if(mFailCount == ACCEPTABLE_FAIL_COUNT) {
				mStatusOk = false; 
			}
			
			try {
				Thread.sleep(DEFAULT_SLEEP_MILLIS);
			}
			catch(Exception exception) {				
				if(mFailCount > 0) {					
					mFailCount--;
				}
			}
		}
				
		ThreadUtils.removeThreadFromHandleList(this);
	}
}

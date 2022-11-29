package com.keypointforensics.videotriage.pool;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
 
public class SilentRejectedExecutionHandler implements RejectedExecutionHandler {
 
	/*
	 * Author: Daniel Puckowski
	 */
	
	//private final boolean LOG_REJECTION;
	
	public SilentRejectedExecutionHandler(final boolean logRejection) {
		//LOG_REJECTION = logRejection;
	}
	
    @Override
    public void rejectedExecution(final Runnable runnable, final ThreadPoolExecutor executor) {
    	//if(LOG_REJECTION == false) {
    		
    	//	return;
    	//}
    	
    }
}
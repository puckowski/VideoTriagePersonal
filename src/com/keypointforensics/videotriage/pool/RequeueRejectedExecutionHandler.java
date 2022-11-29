package com.keypointforensics.videotriage.pool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import com.keypointforensics.videotriage.util.ThreadUtils;
 
public class RequeueRejectedExecutionHandler implements RejectedExecutionHandler {
 
	/*
	 * Author: Daniel Puckowski
	 */
		
	private final int THREAD_POOL_QUEUE_SIZE_MAX;
	
	public RequeueRejectedExecutionHandler(final int threadPoolQueueSize) {
		THREAD_POOL_QUEUE_SIZE_MAX = threadPoolQueueSize;
	}
	
    @Override
    public void rejectedExecution(final Runnable runnable, final ThreadPoolExecutor executor) {
    	BlockingQueue<Runnable> runnableQueue = executor.getQueue();
    	
    	while(runnableQueue.size() == THREAD_POOL_QUEUE_SIZE_MAX) {
    		if(runnableQueue.offer(runnable) == true) {
    			break;
    		} else {
    			ThreadUtils.blockThread(ThreadUtils.DEFAULT_BLOCK_MILLIS, "RequeueRejectedExecutionHandler pause");
    		}
    	}
    }
}
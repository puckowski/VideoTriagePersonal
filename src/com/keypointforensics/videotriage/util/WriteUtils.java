package com.keypointforensics.videotriage.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.keypointforensics.videotriage.pool.RequeueRejectedExecutionHandler;
import com.keypointforensics.videotriage.pool.SilentRejectedExecutionHandler;

public class WriteUtils {

	/*
	 * Author: Daniel Puckowski
	 */
	
	private static final int DEFAULT_CPU_POOL_SIZE       = 1;  
	private static final int DEFAULT_MAXIMUM_POOL_SIZE   = 1; 
	
	private static final int DEFAULT_TIMEOUT_SECONDS     = 180;  
	private static final int DEFAULT_BLOCKING_QUEUE_SIZE = 10000; 
	
	public static ThreadPoolExecutor mImageWritePool;    
	public static ThreadPoolExecutor mDatabaseWritePool; 
	public static ThreadPoolExecutor mPreviewExtractionPool; 
	
	public static void init() {				
		initImageWritePool(); 
		initDatabaseWritePool(); 
		initPreviewExtractionPool();
	}
	
	private static void initImageWritePool() {
		SilentRejectedExecutionHandler rejectionHandler = new SilentRejectedExecutionHandler(false);
		ThreadFactory threadFactory = Executors.defaultThreadFactory();
		
		mImageWritePool = new ThreadPoolExecutor(DEFAULT_CPU_POOL_SIZE, DEFAULT_MAXIMUM_POOL_SIZE, DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(DEFAULT_BLOCKING_QUEUE_SIZE), threadFactory, rejectionHandler);
	}
	
	private static void initDatabaseWritePool() {
		SilentRejectedExecutionHandler rejectionHandler = new SilentRejectedExecutionHandler(false);
		ThreadFactory threadFactory = Executors.defaultThreadFactory();
		
		mDatabaseWritePool = new ThreadPoolExecutor(DEFAULT_CPU_POOL_SIZE, DEFAULT_MAXIMUM_POOL_SIZE, DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(DEFAULT_BLOCKING_QUEUE_SIZE), threadFactory, rejectionHandler);
	}
	
	private static void initPreviewExtractionPool() {
		RequeueRejectedExecutionHandler rejectionHandler = new RequeueRejectedExecutionHandler(DEFAULT_BLOCKING_QUEUE_SIZE);
		ThreadFactory threadFactory = Executors.defaultThreadFactory();
		
		mPreviewExtractionPool = new ThreadPoolExecutor(2, 4, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(DEFAULT_BLOCKING_QUEUE_SIZE), threadFactory, rejectionHandler);
	}
}

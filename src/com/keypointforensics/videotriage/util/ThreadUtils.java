package com.keypointforensics.videotriage.util;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.keypointforensics.videotriage.gui.main.GuiMain;
import com.keypointforensics.videotriage.progress.ProgressBundle;

public class ThreadUtils {

	/*
	 * Author: Daniel Puckowski
	 */
	
	public static final int DEFAULT_BLOCK_MILLIS = 400; 
	public static final int DEFAULT_INTERRUPT_BLOCK_MILLIS = 1000; //2000
	
	public static final ConcurrentLinkedQueue<Thread> GLOBAL_THREAD_HANDLE_LIST = new ConcurrentLinkedQueue<Thread>();
	private static final Lock GLOBAL_THREAD_HANDLE_LOCK = new ReentrantLock();
	
	public static void addThreadToHandleList(final String shortDescription, Thread newThread) {
		GLOBAL_THREAD_HANDLE_LOCK.lock();
		GLOBAL_THREAD_HANDLE_LIST.add(newThread);
		GLOBAL_THREAD_HANDLE_LOCK.unlock();
	}
	
	public static void removeThreadFromHandleList(Thread newThread) {
		GLOBAL_THREAD_HANDLE_LOCK.lock();
		GLOBAL_THREAD_HANDLE_LIST.remove(newThread);
		GLOBAL_THREAD_HANDLE_LOCK.unlock();
	}
	
	public static void stopAllKnownTasks() {		
		ProgressBundle killTaskProgressBundle = ProgressUtils.getProgressBundle("Stopping All Tasks...");
		
		Thread knownThread = null;		
		int currentInterruptIndex = 0;
		
		GLOBAL_THREAD_HANDLE_LOCK.lock();
		
		final int knownThreadCount = GLOBAL_THREAD_HANDLE_LIST.size();
		
		killTaskProgressBundle.progressBar.setMaximum(knownThreadCount);
		
		while(currentInterruptIndex < knownThreadCount) {
			knownThread = GLOBAL_THREAD_HANDLE_LIST.remove();
			
			knownThread.interrupt();
			ThreadUtils.blockThread(DEFAULT_INTERRUPT_BLOCK_MILLIS, "Interrupted thread. Pausing a moment for thread to catch up.");
			
			knownThread.stop();
			knownThread = null;
			
			currentInterruptIndex++;
			killTaskProgressBundle.progressBar.setValue(currentInterruptIndex);
		}
		
		GLOBAL_THREAD_HANDLE_LOCK.unlock();
		
		killTaskProgressBundle.frame.dispose();
	}
	
	public static void stopAllKnownTasks(final GuiMain guiMain) {
		CursorUtils.setBusyCursor(guiMain);
		
		ProgressBundle killTaskProgressBundle = ProgressUtils.getProgressBundle("Stopping All Tasks...");
		
		Thread knownThread = null;		
		int currentInterruptIndex = 0;
		
		GLOBAL_THREAD_HANDLE_LOCK.lock();
		
		final int knownThreadCount = GLOBAL_THREAD_HANDLE_LIST.size();
		
		killTaskProgressBundle.progressBar.setMaximum(knownThreadCount);
		
		while(currentInterruptIndex < knownThreadCount) {
			knownThread = GLOBAL_THREAD_HANDLE_LIST.remove();
			
			knownThread.interrupt();
			ThreadUtils.blockThread(DEFAULT_INTERRUPT_BLOCK_MILLIS, "Interrupted thread. Pausing a moment for thread to catch up.");
			
			knownThread.stop();
			knownThread = null;
			
			currentInterruptIndex++;
			killTaskProgressBundle.progressBar.setValue(currentInterruptIndex);
		}
		
		GLOBAL_THREAD_HANDLE_LOCK.unlock();
		
		killTaskProgressBundle.frame.dispose();
		
		CursorUtils.setDefaultCursor(guiMain);
	}
	
	public static void blockThread(final int millisToBlock, final String message) {
		try {			
			Thread.sleep(millisToBlock);
		} catch (Exception exception) {

		}
	}
}

package com.keypointforensics.videotriage.util;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReportUtils {

	public static final Lock GLOBAL_REPORT_CONTEXT_LOCK = new ReentrantLock();
	
}

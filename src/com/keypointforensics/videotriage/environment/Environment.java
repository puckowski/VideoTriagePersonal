package com.keypointforensics.videotriage.environment;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.keypointforensics.videotriage.legacy.WindowsVideoFrameExtractorLegacy;
import com.keypointforensics.videotriage.thread.ExtractVideoFrameThread;
import com.keypointforensics.videotriage.thread.ThreadCountMonitorThread;
import com.keypointforensics.videotriage.util.IoUtils;
import com.keypointforensics.videotriage.util.WriteUtils;
import com.keypointforensics.videotriage.window.WindowRegistry;

public class Environment {

	/*
	 * Author: Daniel Puckowski
	 */
	
	public static void setup() {
		//IoUtils.redirectStandardErrorStream();
		
		ThreadCountMonitorThread threadCountMonitorThread = new ThreadCountMonitorThread();
		threadCountMonitorThread.start();
		
		WindowsVideoFrameExtractorLegacy.setSupportingBinaryNames();
		ExtractVideoFrameThread.initializeExtractionSet();
		
		WindowRegistry.initializeWindowRegistry();
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			
		} catch (InstantiationException e) {
			
		} catch (IllegalAccessException e) {
			
		} catch (UnsupportedLookAndFeelException e) {
			
		}
		
		WriteUtils.init();
	}
}

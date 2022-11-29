package com.keypointforensics.videotriage.window;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JFrame;

public class WindowRegistry {

	private static WindowRegistry mInstance;
	
	private final ConcurrentHashMap<JFrame, String> mFrameMap;
	
	public WindowRegistry() {
		mFrameMap = new ConcurrentHashMap<JFrame, String>();
	}
	
	public static void initializeWindowRegistry() {
		if(mInstance == null) {
			mInstance = new WindowRegistry();
		}
	}
	
	public static WindowRegistry getInstance() {
		//if(mInstance == null) {
		//	mInstance = new WindowRegistry();
		//}
		
		return mInstance;
	}
	
	public void registerFrame(final JFrame frame, final String shortDescription) {
		mFrameMap.put(frame, shortDescription);
	}
	
	public void closeAllActiveFrames() {
		for(Entry<JFrame, String> frameEntry : mFrameMap.entrySet()) {
			frameEntry.getKey().dispose();
			mFrameMap.remove(frameEntry.getKey());
		}
	}
	
	public void closeFrames(final ArrayList<JFrame> framesToClose) {
		for(JFrame frameToClose : framesToClose) {
			frameToClose.dispose();
			mFrameMap.remove(frameToClose);
		}
	}
}

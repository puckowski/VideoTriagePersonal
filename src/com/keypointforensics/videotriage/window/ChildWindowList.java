package com.keypointforensics.videotriage.window;

import java.util.ArrayList;

import javax.swing.JFrame;

public class ChildWindowList {

	private ArrayList<JFrame> mWindowList;
	
	public ChildWindowList() {
		mWindowList = new ArrayList<JFrame>();
	}
	
	public void addWindow(JFrame frame) {
		mWindowList.add(frame);
	}
	
	public ArrayList<JFrame> getWindows() {
		return mWindowList;
	}
}

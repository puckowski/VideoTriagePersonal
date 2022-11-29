package com.keypointforensics.videotriage.window;

import java.awt.event.WindowAdapter;

public class CloseChildrenWindowAdapter extends WindowAdapter {

	private ChildWindowList mChildWindowList;
	
	public CloseChildrenWindowAdapter(ChildWindowList childWindowList) {
		mChildWindowList = childWindowList;
	}
	
	@Override
    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
        WindowRegistry.getInstance().closeFrames(mChildWindowList.getWindows());
    }
}

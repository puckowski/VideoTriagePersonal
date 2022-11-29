package com.keypointforensics.videotriage.gui.main;

import javax.swing.JComponent;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

public class RequestFocusListener implements AncestorListener {
	
	/*
	 * Author: Daniel Puckowski
	 */

	private boolean mRemoveListener;
	
	public RequestFocusListener() {
		this(true);
	}

	public RequestFocusListener(boolean removeListener) {
		mRemoveListener = removeListener;
	}

	@Override
	public void ancestorAdded(AncestorEvent ancestorEvent) {
		JComponent component = ancestorEvent.getComponent();
		component.requestFocusInWindow();
		
		if (mRemoveListener == true) {
			component.removeAncestorListener(this);
		}
	}

	@Override
	public void ancestorMoved(AncestorEvent ancestorEvent) {
		
	}

	@Override
	public void ancestorRemoved(AncestorEvent ancestorEvent) {
		
	}
}
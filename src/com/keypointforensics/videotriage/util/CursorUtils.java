package com.keypointforensics.videotriage.util;

import java.awt.Cursor;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class CursorUtils {

	/*
	 * Author: Daniel Puckowski
	 */
	
	public static void setBusyCursor(final JComponent component) {
		component.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	
	public static void setBusyCursor(final JFrame frame) {
		frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	
	public static void setDefaultCursor(final JComponent component) {
		component.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	
	public static void setDefaultCursor(final JFrame frame) {
		frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
}

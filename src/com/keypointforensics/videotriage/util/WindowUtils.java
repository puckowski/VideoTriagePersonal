package com.keypointforensics.videotriage.util;

import java.awt.Image;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

public class WindowUtils {
	
	/*
	 * Author: Daniel Puckowski
	 */
	
	public static final int    DEFAULT_SCROLLING_INCREMENT = 16; 
	public static final String DEFAULT_FRAME_ICON          = FileUtils.GRAPHICS_DIRECTORY + "frame_icon.png";
	
	public static void setFrameIcon(final JFrame frame) {
		Image icon = ImageUtils.loadBufferedImage(DEFAULT_FRAME_ICON);
		frame.setIconImage(icon);
	}
	
	public static void center(final JFrame frameToCenter) {
		frameToCenter.setLocationRelativeTo(null);
	}

	public static void maximize(final JFrame frameToMaximize) {
		frameToMaximize.setExtendedState(JFrame.MAXIMIZED_BOTH); 
	}
	
	public static void center(final JDialog dialog) {
		dialog.setLocationRelativeTo(null);
	}

	public static void setScrollBarIncrement(final JScrollPane scrollPane) {	
		scrollPane.getVerticalScrollBar().setUnitIncrement(DEFAULT_SCROLLING_INCREMENT);	
	}

	public static void setTextAreaUpdatePolicy(final JTextArea textArea) {	
		DefaultCaret caret = (DefaultCaret) textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	}
}

package com.keypointforensics.videotriage.gui.splash;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;

import com.keypointforensics.videotriage.gui.imagepanel.ScalableSimpleImagePanel;
import com.keypointforensics.videotriage.gui.imagepanel.SimpleImagePanel;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ImageUtils;
import com.keypointforensics.videotriage.util.WindowUtils;

public class SplashScreen extends JFrame {
	
	/*
	 * Author: Daniel Puckowski
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1412117439333999839L;
	
	private static final String SPLASH_SCREEN_IMAGE_FILENAME = "splash_screen_image.png";
	private static final int    DEFAULT_FRAME_WIDTH          = 478;
	private static final int    DEFAULT_FRAME_HEIGHT         = 478;
	private static final int    DEFAULT_SPLASH_TIME_MILLIS   = 2000; // 2.0 seconds
	
	private ScalableSimpleImagePanel mTitlePanel;
	
	public SplashScreen() {
		mTitlePanel = new ScalableSimpleImagePanel(true);
		mTitlePanel.update(ImageUtils.loadBufferedImage(FileUtils.GRAPHICS_DIRECTORY + SPLASH_SCREEN_IMAGE_FILENAME));
		
		WindowUtils.setFrameIcon(this);
		this.setLayout(new BorderLayout());
		this.add(mTitlePanel, BorderLayout.CENTER);
		this.setPreferredSize(new Dimension(DEFAULT_FRAME_WIDTH, DEFAULT_FRAME_HEIGHT));
		this.setUndecorated(true);
		this.pack();
	}
	
	public void display() {
		this.setVisible(true);
		WindowUtils.center(this);
		
		try {
			Thread.sleep(DEFAULT_SPLASH_TIME_MILLIS);
		} catch(Exception exception) {
			
		}
	}
}
package com.keypointforensics.videotriage.gui.imagepanel;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class SimpleImagePanel extends JPanel {

	/*
	 * Author: Daniel Puckowski
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2883141399639677191L;
	
	private BufferedImage mImage;
	
	protected volatile boolean mTimerActive;

	protected boolean mHadSignal;
	protected int mNoSignalTickCount;
	protected BufferedImage mLastBufferedImage;
	
	public void update(final BufferedImage image) {
		if(image == null) {			
			return;
		}
		
		mImage = image;
		this.repaint();
	}
	
	public void nullify() {
		mImage = null;
		mLastBufferedImage = null;
		
		this.revalidate();
	}
	
	@Override
	public void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		
		if (mImage != null) {
			graphics.drawImage(mImage, 0, 0, this);
			mLastBufferedImage = mImage;
		}
	}
}

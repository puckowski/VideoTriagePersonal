package com.keypointforensics.videotriage.gui.imagepanel;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import com.keypointforensics.videotriage.util.DimenUtils;
import com.keypointforensics.videotriage.util.ImageUtils;

public class CenteredSimpleImagePanel extends JPanel {

	/*
	 * Author: Daniel Puckowski
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2883141399639677191L;
	
	private String mImageAbsolutePath;
	
	private BufferedImage mImage;
		
	private int mOriginalWidth;
	private int mOriginalHeight;
	
	public CenteredSimpleImagePanel(final String imageAbsolutePath) {
		mImageAbsolutePath = imageAbsolutePath;
		
		update(ImageUtils.loadBufferedImage(mImageAbsolutePath));
	}
	
	public void update(final BufferedImage image) {
		if(image == null) {			
			return;
		}
		
		mImage = image;
		mOriginalWidth = mImage.getWidth();
		mOriginalHeight = mImage.getHeight();
		this.repaint();
	}
	
	public void nullify() {
		mImage = null;
		
		this.revalidate();
	}
	
	@Override
	public void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		
		if (mImage != null) {
			int height = this.getHeight();
			int width = this.getWidth();
			Dimension resized = DimenUtils.getScaledDimension(new Dimension(mOriginalWidth, mOriginalHeight), new Dimension(width, height));
			
			int x = (getWidth() - resized.width) / 2;
			
			graphics.drawImage(mImage, x, 0, resized.width, resized.height, this);
		}
	}
}

package com.keypointforensics.videotriage.gui.imagepanel;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import com.keypointforensics.videotriage.util.DimenUtils;
import com.keypointforensics.videotriage.util.ImageUtils;

public class ScalableSimpleImagePanel extends SimpleImagePanel {

	/*
	 * Author: Daniel Puckowski
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7874245682392760248L;
	
	public static final boolean SCALE_TO_FIT_ENABLED  = true;
	public static final boolean SCALE_TO_FIT_DISABLED = false;

	public static final int SINGLE_CLICK = 1;

	protected final boolean SCALE_TO_FIT;

	protected String mImageAbsolutePath;
	
	private int mOriginalWidth;
	private int mOriginalHeight;
	
	private BufferedImage mImage;
	
	public ScalableSimpleImagePanel(final boolean scaleToFit) {
		SCALE_TO_FIT = scaleToFit;		
	}
	
	public ScalableSimpleImagePanel(final String imageAbsolutePath, final boolean scaleToFit) {
		mImageAbsolutePath = imageAbsolutePath;
		SCALE_TO_FIT        = scaleToFit;
		
		update(ImageUtils.loadBufferedImage(mImageAbsolutePath));
	}
	
	public String getAbsolutePath() {
		return mImageAbsolutePath;
	}
	
	public void clear() {
		mImage = null;
		this.repaint();
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
	
	@Override
	public void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		
		drawGalleryImage(graphics);
	}
	
	private void drawGalleryImage(final Graphics graphics) {
		if (mImage != null && SCALE_TO_FIT == false) {
			graphics.drawImage(mImage, 0, 0, this);
		}
		else if(mImage != null && SCALE_TO_FIT == true) {
			int height = this.getHeight();
			int width = this.getWidth();
			Dimension resized = DimenUtils.getScaledDimension(new Dimension(mOriginalWidth, mOriginalHeight), new Dimension(width, height));
			
			graphics.drawImage(mImage, 0, 0, resized.width, resized.height, this);
		} 
	}
}

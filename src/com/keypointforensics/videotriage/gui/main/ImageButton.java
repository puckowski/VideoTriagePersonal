package com.keypointforensics.videotriage.gui.main;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.JButton;

import com.keypointforensics.videotriage.util.DimenUtils;

public class ImageButton extends JButton {
	
	/*
	 * Author: Daniel Puckowski
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3807285244309277455L;
	
	private static final float MOUSE_HOVERING_ALPHA = 0.5f;
	private static final float MOUSE_EXITED_ALPHA   = 1;

	private final Image IMAGE;
	
	private float     mHoverAlpha = MOUSE_EXITED_ALPHA;
	private int       mLastWidth;
	private int       mLastHeight;
	private Dimension mDimensions;
	
	public ImageButton(final Image image) {
		IMAGE = image;
		this.setContentAreaFilled(false);

		this.addMouseListener(new java.awt.event.MouseAdapter() {
		    public void mouseEntered(java.awt.event.MouseEvent evt) {
		        mHoverAlpha = MOUSE_HOVERING_ALPHA;
		        ImageButton.this.repaint();
		    }
		    public void mouseExited(java.awt.event.MouseEvent evt) {
		        mHoverAlpha = MOUSE_EXITED_ALPHA;
		        ImageButton.this.repaint();
		    }
		});
	}

	@Override
	public void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);

		if (IMAGE != null) {
			final int width = this.getWidth();
			final int height = this.getHeight();
			
			if(width != mLastWidth || height != mLastHeight) {				
				Dimension finalDimensions = DimenUtils.getScaledDimension(
					new Dimension(IMAGE.getWidth(this), IMAGE.getHeight(this)),
					new Dimension(this.getWidth(), this.getHeight()));
			
				mDimensions = finalDimensions;
				mLastWidth = width;
				mLastHeight = height;
			}

			Graphics2D graphics2d = (Graphics2D) graphics;
			graphics2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, mHoverAlpha));
			
			graphics.drawImage(IMAGE, 0, 0, mDimensions.width, mDimensions.height, this);
		}
	}
}
package com.keypointforensics.videotriage.gui.imagepanel;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import com.keypointforensics.videotriage.util.FontUtils;

public class DeprecatedTitlePanel extends SimpleImagePanel {

	/*
	 * Author: Daniel Puckowski
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7874245682392760248L;
	
	public static final boolean SCALE_TO_FIT_ENABLED  = true;
	public static final boolean SCALE_TO_FIT_DISABLED = false;

	private final boolean SCALE_TO_FIT;

	private BufferedImage mImage;
	private String        mTitle;
	
	public DeprecatedTitlePanel(final String title, final boolean scaleToFit) {
		mTitle       = title;
		SCALE_TO_FIT = scaleToFit;
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
		this.repaint();
	}
	
	@Override
	public void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		
		drawTitleImage(graphics);
		drawTitle(graphics);
	}
	
	private void drawTitleImage(final Graphics graphics) {
		if (mImage != null && SCALE_TO_FIT == false) {
			graphics.drawImage(mImage, 0, 0, this);
		}
		else if(mImage != null && SCALE_TO_FIT == true) {
			int width = this.getWidth();
			
			int height = this.getHeight();
			
			graphics.drawImage(mImage, 0, 0, width, height, this); 
		} 
	}
	
	private void drawTitle(final Graphics graphics) {
		graphics.setFont(FontUtils.TITLE_FONT);
		graphics.setColor(Color.WHITE);
		
		if (mTitle != null) {
			Graphics2D graphics2d = (Graphics2D) graphics;
			FontMetrics fontMetrics = graphics2d.getFontMetrics();
			Rectangle2D rect = fontMetrics.getStringBounds(mTitle, graphics2d);
			
			int x = (this.getWidth() - (int) rect.getWidth()) / 2;
			int y = (this.getHeight() - (int) rect.getHeight()) / 2 + fontMetrics.getAscent();
						
			graphics.drawString(mTitle, x, y);
		}
	}
}

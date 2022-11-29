package com.keypointforensics.videotriage.gui.imagepanel;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import javax.swing.Timer;

import com.keypointforensics.videotriage.gui.main.controller.CameraControllerRegistry;
import com.keypointforensics.videotriage.util.FontUtils;

public class VideoFeedImagePanel extends SimpleImagePanel {

	/*
	 * Author: Daniel Puckowski
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -856634089603790044L;

	private final CameraControllerRegistry CONTROLLER_REGISTRY = CameraControllerRegistry.INSTANCE;
	
	public static final int ROTATION_INVALID_STATE = -1;
	public static final int NO_ROTATION            = 0;
	
	private final String CONTROLLER_ID;
	
	private BufferedImage     mImage;
	private int               mRotateDegrees;
	private AffineTransform   mTransform; 
	private AffineTransformOp mTransformOp;
	private String            mStatusString;
	
	public VideoFeedImagePanel(final String controllerId) {
		CONTROLLER_ID = controllerId;
	}
	
	public String getControllerId() {
		return CONTROLLER_ID;
	}
	
	public String getStatusString() {
		return mStatusString;
	}
	
	public void setStatusString(final String newStatusString) {
		mStatusString = newStatusString;
	}
	
	public int getRotateDegrees() {
		return mRotateDegrees;
	}
	
	public void setRotateDegrees(final int degrees) {
		mRotateDegrees = degrees;

		mTransform = null;
		mTransformOp = null;
	}
	
	public BufferedImage getImage() {
		return mImage;
	}
	
	public void update(final BufferedImage image) {
		mImage = image;
		this.repaint();
	}

	public void clear() {
		mImage = null;
		this.repaint();
	}
	
	public void updateStatusString(final String newStatusString) {
		if(newStatusString == null) {			
			return;
		} else if(newStatusString.isEmpty() == true) {			
			return;
		}
		
		mStatusString = newStatusString;
	}
	
	protected AffineTransformOp getTransformOp() {
		if(mTransform == null) {			
			mTransform = new AffineTransform();
		} 
		
		mTransform.rotate(Math.toRadians(mRotateDegrees), mImage.getWidth() / 2, mImage.getHeight() / 2);
		
		if(mTransformOp == null) {			
			mTransformOp = new AffineTransformOp(mTransform, AffineTransformOp.TYPE_BILINEAR);
		}
		
		return mTransformOp;
	}
	
	@Override
	public void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		
		if (mImage != null) {
			drawVideoFeedImage(graphics);
			
			mHadSignal = true;
			mLastBufferedImage = mImage;
		} /* else if(mHadSignal == false) {
			//
		} */else if(mNoSignalTickCount < 50) {
			graphics.drawImage(mLastBufferedImage, 0, 0, this.getWidth(), this.getHeight(), this);
			
			mNoSignalTickCount++;
			/*
			if (mTimerActive == false) {
				mTimerActive = true;
				int delay = 1000;
				
				Timer timer = new Timer(delay, new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						VideoFeedImagePanel.this.revalidate();
						mTimerActive = false;
					}
				});
				
				timer.setRepeats(false);
				timer.start();
			}
			*/
		} else {
			mLastBufferedImage = null;
			mNoSignalTickCount = 0;
			mHadSignal = false;	
		}
	}
	
	private void drawVideoFeedImage(final Graphics graphics) {
		if (mRotateDegrees != 0) {			
		    mImage = getTransformOp().filter(mImage, null);
		}

		graphics.drawImage(mImage, 0, 0, this.getWidth(), this.getHeight(), this);
		
		drawStatusString(graphics);
	}
	
	private void drawStatusString(final Graphics graphics) {
		if (mStatusString != null && CONTROLLER_REGISTRY.getStatusBarParams(CONTROLLER_ID).getEnabled() == true) {
			Font oldFont = graphics.getFont();
			Color oldColor = graphics.getColor();
			
			graphics.setFont(FontUtils.DEFAULT_VIDEO_FEED_FONT);
			graphics.setColor(Color.WHITE);
			
			Graphics2D graphics2d = (Graphics2D) graphics;
			FontMetrics fontMetrics = graphics2d.getFontMetrics();
			Rectangle2D rect = fontMetrics.getStringBounds(mStatusString, graphics2d);

			if(rect.getWidth() > this.getWidth())
			{
				String temporaryStatusString = mStatusString.substring(0, mStatusString.indexOf(" ", 5)).trim();
				rect = fontMetrics.getStringBounds(temporaryStatusString, graphics2d);
				
				if(rect.getWidth() <= this.getWidth()) 
				{
					int x = (this.getWidth() - (int) rect.getWidth()) / 2;
					int y = (this.getHeight() - (int) (rect.getHeight() * 2)) + fontMetrics.getAscent();
					
					graphics.drawString(temporaryStatusString, x, y);
				}
			}
			else if(rect.getHeight() > this.getHeight())
			{
				String temporaryStatusString = mStatusString.substring(0, mStatusString.indexOf(" ", 5)).trim();
				rect = fontMetrics.getStringBounds(temporaryStatusString, graphics2d);
				
				if(rect.getHeight() <= this.getHeight()) 
				{
					int x = (this.getWidth() - (int) rect.getWidth()) / 2;
					int y = (this.getHeight() - (int) (rect.getHeight() * 2)) + fontMetrics.getAscent();
					
					graphics.drawString(temporaryStatusString, x, y);
				}
			}
			else
			{
				int x = (this.getWidth() - (int) rect.getWidth()) / 2;
				int y = (this.getHeight() - (int) (rect.getHeight() * 2)) + fontMetrics.getAscent();
				
				graphics.drawString(mStatusString, x, y);
			}
			
			graphics.setFont(oldFont);
			graphics.setColor(oldColor);
		}
	}
}

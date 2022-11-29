package com.keypointforensics.videotriage.gui.imagepanel;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.Timer;

import com.keypointforensics.videotriage.gui.main.NoSignalPreviewImageSingleton;

public class VideoFeedPreviewPanel extends VideoFeedImagePanel {

	/*
	 * Author: Daniel Puckowski
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -383586652975162595L;
		
	private NoSignalPreviewImageSingleton mNoSignalImage = NoSignalPreviewImageSingleton.INSTANCE;
	
	public VideoFeedPreviewPanel(final String controllerId) {
		super(controllerId);
	}
	
	@Override
	public void updateStatusString(final String newStatusString) {
		if(newStatusString == null) {			
			return;
		} 
		else if(newStatusString.isEmpty() == true) {			
			return;
		}
		
		setStatusString(newStatusString);
	}
	
	public void clear() {
		super.clear();
	}
	
	@Override
	public void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		
		drawPreviewImage(graphics);
	}
	
	private void drawPreviewImage(final Graphics graphics) {
		BufferedImage image = getImage();

		if (image != null) {
			graphics.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), this);
			
			mHadSignal = true;
			mLastBufferedImage = image;
		} else if(mHadSignal == false) {
			graphics.drawImage(mNoSignalImage.getImage(), 0, 0, this.getWidth(), this.getHeight(), this);
		} else if(mNoSignalTickCount < 50) {
			graphics.drawImage(mLastBufferedImage, 0, 0, this.getWidth(), this.getHeight(), this);
			
			mNoSignalTickCount++;
			
			/*
			if (mTimerActive == false) {
				mTimerActive = true;
				int delay = 1000;

				Timer timer = new Timer(delay, new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						VideoFeedPreviewPanel.this.revalidate();
						mTimerActive = false;
					}
				});

				timer.setRepeats(false);
				timer.start();
			}
			*/
		} else {
			graphics.drawImage(mNoSignalImage.getImage(), 0, 0, this.getWidth(), this.getHeight(), this);
			
			mLastBufferedImage = null;
			mNoSignalTickCount = 0;
			mHadSignal = false;
		}
	}
}

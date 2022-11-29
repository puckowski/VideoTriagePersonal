package com.keypointforensics.videotriage.gui.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.keypointforensics.videotriage.gui.imagepanel.VideoFeedPreviewPanel;
import com.keypointforensics.videotriage.util.FontUtils;

public class CameraPreviewPanel extends JPanel {

	/*
	 * Author: Daniel Puckowski
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8671946587650922990L;
	
	private static final String START_BUTTON_TEXT = "Start";
	private static final String STOP_BUTTON_TEXT  = "Stop";
	private static final String CLOSE_BUTTON_TEXT = "Close";
	
	private static final int PREFERRED_PIXEL_WIDTH  = 320;
	private static final int PREFERRED_PIXEL_HEIGHT = 320;
	
	private final String  CONTROLLER_ID;
	private final GuiMain GUI_MAIN;
	
	private JLabel                mControllerLabel;
	private VideoFeedPreviewPanel mVideoFeedPreview;
	
	private JButton               mStopButton;
	private JButton               mStartButton;
	private JButton               mCloseButton;
	
	public CameraPreviewPanel(final String controllerId, final GuiMain guiMain) {
		this.setLayout(new BorderLayout());
		
		CONTROLLER_ID = controllerId;
		GUI_MAIN      = guiMain;
		
		buildPanel();
	}
	
	public void clear() {
		mVideoFeedPreview.clear();
	}
	
	public String getControllerId() {
		return CONTROLLER_ID;
	}
	
	public void setRotateDegrees(final int newRotateDegrees) {
		if(mVideoFeedPreview != null) {			
			mVideoFeedPreview.setRotateDegrees(newRotateDegrees);
		}
	}
	
	public void update(final BufferedImage newImage) {
		//if(mVideoFeedPreview != null) {
			mVideoFeedPreview.update(newImage);
		//}
	}
	
	public void updateStatusString(final String newStatusString) {
		if(newStatusString == null) {			
			return;
		}
		else if(newStatusString.isEmpty() == true) {			
			return;
		}
		
		if(mVideoFeedPreview != null) {
			mVideoFeedPreview.updateStatusString(newStatusString);
		}
	}
	
	private void buildStopButton() {
		mStopButton = new JButton(STOP_BUTTON_TEXT);
		mStopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				GUI_MAIN.stopController(CONTROLLER_ID);
			}	
		});
	}
	
	private void buildStartButton() {
		mStartButton = new JButton(START_BUTTON_TEXT);
		mStartButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				GUI_MAIN.startController(CONTROLLER_ID);
			}	
		});
	}
	
	private void buildCloseButton() {
		mCloseButton = new JButton(CLOSE_BUTTON_TEXT);
		mCloseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				GUI_MAIN.removeController(CONTROLLER_ID);
			}	
		});
	}
	
	private void buildPanel() {		
		mControllerLabel = new JLabel(CONTROLLER_ID, SwingConstants.CENTER);
		mControllerLabel.setFont(FontUtils.DEFAULT_FONT);
		this.add(mControllerLabel, BorderLayout.NORTH);
		
		mVideoFeedPreview = new VideoFeedPreviewPanel(CONTROLLER_ID);
		this.add(mVideoFeedPreview, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel(new FlowLayout());

		buildStopButton();
		buildStartButton();
		buildCloseButton();
		
		buttonPanel.add(mStartButton);
		buttonPanel.add(mStopButton);
		buttonPanel.add(mCloseButton);
		
		this.add(buttonPanel, BorderLayout.SOUTH);
		this.setPreferredSize(new Dimension(PREFERRED_PIXEL_WIDTH, PREFERRED_PIXEL_HEIGHT));
	}
}

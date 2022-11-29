package com.keypointforensics.videotriage.statusbar;

import com.keypointforensics.videotriage.gui.imagepanel.VideoFeedImagePanel;

public class StatusBarBundle {

	/*
	 * Author: Daniel Puckowski
	 */
	
	private final VideoFeedImagePanel GRAPHICS_PANEL;
	private final String              PORT_STRING;
	
	private String mIpString;
	
	public StatusBarBundle(final VideoFeedImagePanel graphicsPanel, final String ip, final String port) {
		GRAPHICS_PANEL       = graphicsPanel;
		PORT_STRING          = port;
		
		mIpString = ip;
	}
	
	public VideoFeedImagePanel getGraphicsPanel() {
		return GRAPHICS_PANEL;
	}
	
	public String getIp() {
		return mIpString;
	}
	
	public void setUpdatedSource(final String updatedSource) {
		mIpString = updatedSource;
	}
	
	public String getPort() {
		return PORT_STRING;
	}
	
	public int getSourceWidth() {
		return GRAPHICS_PANEL.getImage().getWidth();
	}
	
	public int getSourceHeight() {
		return GRAPHICS_PANEL.getImage().getHeight();
	}
}

package com.keypointforensics.videotriage.gui.main;

import java.awt.image.BufferedImage;

import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ImageUtils;

public class NoSignalDisplayImageSingleton {
	
	/*
	 * Author: Daniel Puckowski
	 */
	
	public static final NoSignalDisplayImageSingleton INSTANCE = new NoSignalDisplayImageSingleton();
	
	private final String        NO_SIGNAL_IMAGE_FILENAME = "video_feed_no_signal_display.jpg";
	private final BufferedImage NO_SIGNAL_IMAGE;
	
	private NoSignalDisplayImageSingleton() {
		NO_SIGNAL_IMAGE = ImageUtils.loadBufferedImage(FileUtils.GRAPHICS_DIRECTORY + this.NO_SIGNAL_IMAGE_FILENAME);
	}
	
	public BufferedImage getImage() {
		return NO_SIGNAL_IMAGE;
	}
}
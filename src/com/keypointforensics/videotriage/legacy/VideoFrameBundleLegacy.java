package com.keypointforensics.videotriage.legacy;

import java.awt.Color;
import java.awt.image.BufferedImage;

import com.keypointforensics.videotriage.util.ColorUtils;

public class VideoFrameBundleLegacy implements FrsBundleLegacy {
	
	/*
	 * Author: Daniel Puckowski
	 */
	
	private String mFilename;          
	private BufferedImage mVideoFrame; 
	
	private Color mColorData;
	
	private int mOccurrences;
	
	public VideoFrameBundleLegacy(String filename, BufferedImage image) {		
		mFilename = filename;
		mVideoFrame = image;
		
		mOccurrences = 1;
		
		mColorData = ColorUtils.computeColorData(mColorData, mVideoFrame);
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "\tHash code: " + this.hashCode() + "\tFilename: " + mFilename + "\tOccurrences: " + mOccurrences;
	}
	
	public int getOccurrences() {
		return mOccurrences;
	}
	
	public void setOccurrences(int occurrences) {
		mOccurrences = occurrences;
	}
	
	public void incrementOccurrences() {
		++mOccurrences;
	}
	
	public String getFilename() {
		return mFilename;
	}

	public BufferedImage getImage() {
		return mVideoFrame;
	}

	public void setFilename(String filename) {
		mFilename = filename;
	}

	public void setImage(BufferedImage faceImage) {
		if(mVideoFrame != null) { 
			mVideoFrame.flush();
			mVideoFrame = null;
		}
		
		mVideoFrame = faceImage;
	}
	
	public void cleanup() {
		mFilename = null;
		
		if(mVideoFrame != null) {
			mVideoFrame.flush();
			mVideoFrame = null;
		}
		
		mColorData = null;
	}
	
	public Color getColorData() {
		return mColorData;
	}
}

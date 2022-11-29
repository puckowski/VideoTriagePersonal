package com.keypointforensics.videotriage.blob;

import java.awt.Rectangle;

public class Blob extends Rectangle {
	
	/*
	 * Author: Daniel Puckowski
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4394972260487547838L;

	public int xMin;
	public int xMax;
	public int yMin;
	public int yMax;
	public int mass;

	public Blob(final int expandWidth, final int expandHeight, final int videoWidth, final int videoHeight, int xMin, int xMax, int yMin, int yMax, final int mass) {
		xMin -= expandWidth;
		yMin -= expandHeight;
			
		if(xMin < 0) {
			xMin = 0;
		}
			
		if(yMin < 0) {
			yMin = 0;
		}
		
		xMax += expandWidth;
		yMax += expandHeight;
			
		if(xMax > videoWidth) {
			xMax = videoWidth;
		}
			
		if(yMax > videoHeight) {
			yMax = videoHeight;
		}
		
		this.xMin = xMin;
		this.xMax = xMax;
		this.yMin = yMin;
		this.yMax = yMax;
		this.mass = mass;
	
		this.x      = xMin;
		this.width  = xMax - xMin;
		this.y      = yMin;
		this.height = yMax - yMin;
	}
	
	public Blob(int xMin, int xMax, int yMin, int yMax, final int mass) {
		this.xMin = xMin;
		this.xMax = xMax;
		this.yMin = yMin;
		this.yMax = yMax;
		this.mass = mass;
		
		this.x      = xMin;
		this.width  = xMax - xMin;
		this.y      = yMin;
		this.height = yMax - yMin;
	}
	
	public Blob(Rectangle fromRectangle, Blob firstBlob, Blob secondBlob) {
		this.xMin = fromRectangle.x;
		this.xMax = fromRectangle.x + fromRectangle.width;
		this.yMin = fromRectangle.y;
		this.yMax = fromRectangle.y + fromRectangle.height;
		this.mass = firstBlob.mass + secondBlob.mass;

		this.x      = xMin;
		this.width  = fromRectangle.width;
		this.y      = yMin;
		this.height = fromRectangle.height;
	}
}
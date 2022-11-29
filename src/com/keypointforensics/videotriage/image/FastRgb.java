package com.keypointforensics.videotriage.image;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class FastRgb {

	/*
	 * Author: Daniel Puckowski
	 */
	
	public static final int DEFAULT_ARGB_VALUE   = -16777216;
	public static final int INVALID_VALUE        = -1;
	public static final int DEFAULT_PIXEL_LENGTH = 3; 
	public static final int RGBA_PIXEL_LENGTH    = 4; 
	
	private final int     WIDTH;
	private final int     HEIGHT;
	private final int     TYPE;
	private final int     PIXEL_LENGTH;
	private final byte[]  PIXELS;
	private final int     LENGTH_BY_WIDTH_PRECOMPUTE;
	
	public FastRgb(final BufferedImage image) {
		PIXELS = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		
		WIDTH = image.getWidth();
		HEIGHT = image.getHeight();
		TYPE = image.getType();
			
		PIXEL_LENGTH = DEFAULT_PIXEL_LENGTH; 
		LENGTH_BY_WIDTH_PRECOMPUTE = PIXEL_LENGTH * WIDTH;
	}
	
	public int getWidth() {
		return WIDTH;
	}
	
	public int getHeight() {
		return HEIGHT;
	}
	
	public int getType() {
		return TYPE;
	}
	
	public int get(final int x, final int y) {
		int pos = (y * LENGTH_BY_WIDTH_PRECOMPUTE) + (x * PIXEL_LENGTH);

		int argb = DEFAULT_ARGB_VALUE; 
		
		argb += ((int) PIXELS[pos++] & 0xff);         
		argb += (((int) PIXELS[pos++] & 0xff) << 8);  
		argb += (((int) PIXELS[pos++] & 0xff) << 16); 
		
		return argb;
	}
}

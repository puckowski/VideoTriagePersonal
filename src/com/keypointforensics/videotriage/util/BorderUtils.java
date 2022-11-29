package com.keypointforensics.videotriage.util;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

public class BorderUtils {

	/*
	 * Author: Daniel Puckowski
	 */
	
	private final static int DEFAULT_EMPTY_BORDER_WIDTH = 5;
	
	private static Border mEmptyBorder;
	
	public static Border getEmptyBorder() {
		if(mEmptyBorder == null) {
			mEmptyBorder = BorderFactory.createEmptyBorder(DEFAULT_EMPTY_BORDER_WIDTH, DEFAULT_EMPTY_BORDER_WIDTH, DEFAULT_EMPTY_BORDER_WIDTH, DEFAULT_EMPTY_BORDER_WIDTH);
		}
		
		return mEmptyBorder;
	}
}

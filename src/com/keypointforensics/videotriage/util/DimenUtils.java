package com.keypointforensics.videotriage.util;

import java.awt.Dimension;

public class DimenUtils {

	/*
	 * Author: Daniel Puckowski
	 */
	
	public static Dimension getScaledDimension(final Dimension currentDimensions, final Dimension boundaryDimensions) {
		final int originalWidth = currentDimensions.width;
		final int originalHeight = currentDimensions.height;
		
		final int boundWidth = boundaryDimensions.width;
		final int boundHeight = boundaryDimensions.height;
		
		int newWidth = originalWidth;
		int newHeight = originalHeight;

		if (originalWidth > boundWidth) {
			newWidth = boundWidth;
			
			if(newWidth != 0) {
				newHeight = (newWidth * originalHeight) / originalWidth;
			}
			else {				
				return null;
			}
		} else if (originalWidth < boundWidth) {
			newWidth = boundWidth;
			newHeight = (newWidth * originalHeight) / originalWidth;
		}

		if (newHeight > boundHeight) {
			newHeight = boundHeight;
			
			if(newHeight != 0) {
				newWidth = (newHeight * originalWidth) / originalHeight;
			}
			else {				
				return null;
			}
		}
		
		return new Dimension(newWidth, newHeight);
	}
}

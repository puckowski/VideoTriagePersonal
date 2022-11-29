package com.keypointforensics.videotriage.util;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class ColorUtils {

	/*
	 * Author: Daniel Puckowski
	 */

	private static final float LHS_BLEND_RATIO = 0.5f;
	private static final float RHS_BLEND_RATIO = 0.5f;
	
	public static int blendFast(final int argb1, final int argb2) {
		int mA1 = (argb1 >> 24 & 0xff);
		int mR1 = ((argb1 & 0xff0000) >> 16);
		int mG1 = ((argb1 & 0xff00) >> 8);
		int mB1 = (argb1 & 0xff);

		int mA2 = (argb2 >> 24 & 0xff);
		int mR2 = ((argb2 & 0xff0000) >> 16);
		int mG2 = ((argb2 & 0xff00) >> 8);
		int mB2 = (argb2 & 0xff);

		int mA = (int) ((mA1 * LHS_BLEND_RATIO) + (mA2 * RHS_BLEND_RATIO));
		int mR = (int) ((mR1 * LHS_BLEND_RATIO) + (mR2 * RHS_BLEND_RATIO));
		int mG = (int) ((mG1 * LHS_BLEND_RATIO) + (mG2 * RHS_BLEND_RATIO));
		int mB = (int) ((mB1 * LHS_BLEND_RATIO) + (mB2 * RHS_BLEND_RATIO));

		return mA << 24 | mR << 16 | mG << 8 | mB;
	}

	public static Color computeColorData(Color color, BufferedImage videoFrame) {
		int width = videoFrame.getWidth();
		int height = videoFrame.getHeight();

		int[] dataBuffInt = videoFrame.getRGB(0, 0, width, height, null, 0, width); 

		color = new Color(dataBuffInt[100]);
		    
		/*
			System.out.println(mColorData.getRed());   // = (dataBuffInt[100] >> 16) & 0xFF
			System.out.println(mColorData.getGreen()); // = (dataBuffInt[100] >> 8)  & 0xFF
			System.out.println(mColorData.getBlue());  // = (dataBuffInt[100] >> 0)  & 0xFF
	        System.out.println(mColorData.getAlpha()); // = (dataBuffInt[100] >> 24) & 0xFF
		*/
		
		return color;
	}
}


package com.keypointforensics.videotriage.image.match;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.Serializable;

/**
 * ABOUT generateIntegralImage
 * 
 * When OpenSURF stores it's version of the integral image, some slight rounding
 * actually occurs, it doesn't maintain the same values from when it calculates
 * the integral image to when it calls BoxIntegral on the same data
 * 
 *         Example from C++ OpenSURF - THIS DOESN'T HAPPEN IN THE JAVA VERSION
 * 
 *         IntegralImage Values at Calculation Time: integralImage[11][9] =
 *         33.69019699 integralImage[16][9] = 47.90196228 integralImage[11][18]
 *         = 65.84313202 integralImage[16][18] = 93.58038330
 * 
 * 
 *         integralImage[11][18] = 65.84313202 que? integralImage[18][11] =
 *         64.56079102
 * 
 *         IntegralImage Values at BoxIntegral Time: img[11][9] = 33.83921814
 *         img[11][18] = 64.56079102 img[16][9] = 48.76078796 img[16][18] =
 *         93.03530884
 *
 */
public class IntegralImage implements Serializable {

	/**
	 * Generated via Eclipse
	 */
	private static final long serialVersionUID = -5963845072379276182L;

	private float[][] mIntImage;
	private int mWidth = -1;
	private int mHeight = -1;

	public float[][] getValues() {
		return mIntImage;
	}

	public int getWidth() {
		return mWidth;
	}

	public int getHeight() {
		return mHeight;
	}

	public float getValue(int column, int row) {
		return mIntImage[column][row];
	}

	public IntegralImage(BufferedImage input) {
		mIntImage = new float[input.getWidth()][input.getHeight()];
		mWidth = mIntImage.length;
		mHeight = mIntImage[0].length;

		int width = input.getWidth();
		int height = input.getHeight();

		WritableRaster raster = input.getRaster();
		int[] pixel = new int[4];
		float sum;
		for (int y = 0; y < height; y++) {
			sum = 0F;
			for (int x = 0; x < width; x++) {
				raster.getPixel(x, y, pixel);
				/**
				 * TODO: FIX LOSS IN PRECISION HERE, DON'T ROUND BEFORE THE DIVISION (OR AFTER,
				 * OR AT ALL) This was done to match the C++ version, can be removed after
				 * confident that it's working correctly.
				 */
				float intensity = Math.round((0.299D * pixel[0] + 0.587D * pixel[1] + 0.114D * pixel[2])) / 255F;
				sum += intensity;
				if (y == 0) {
					mIntImage[x][y] = sum;
				} else {
					mIntImage[x][y] = sum + mIntImage[x][y - 1];
				}
			}
		}
	}
}

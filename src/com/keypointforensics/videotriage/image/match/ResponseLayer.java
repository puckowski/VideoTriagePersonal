
package com.keypointforensics.videotriage.image.match;

import java.io.Serializable;

public class ResponseLayer implements Serializable {

	private static final long serialVersionUID = -6212835326065799379L;
	private int mWidth;
	private int mHeight;
	private int mStep;
	private int mFilter;

	private char[][] mLaplacian;
	private double[][] mResponses;

	ResponseLayer(int width, int height, int step, int filter, IntegralImage integralImage) {
		mWidth = width;
		mHeight = height;
		mStep = step;
		mFilter = filter;

		mLaplacian = new char[mWidth][mHeight];
		mResponses = new double[mWidth][mHeight];

		buildResponseLayer(integralImage);
	}

	private void buildResponseLayer(IntegralImage img) {
		int b = (mFilter - 1) / 2 + 1;
		int l = mFilter / 3;
		int w = mFilter;
		double inverse_area = 1D / (w * w);
		double Dxx, Dyy, Dxy;
		// System.out.println("w: " + mWidth + ", h: " + mHeight + ", step: " + mStep +
		// ", filter: " + mFilter);
		// System.out.println("filter: " + mFilter + ", b: " + b + ", l: " + l + ", w: "
		// + w);
		// System.out.println("inverse area = " + inverse_area);
		for (int r, c, ar = 0; ar < mHeight; ++ar) {
			for (int ac = 0; ac < mWidth; ++ac) {
				r = ar * mStep;
				c = ac * mStep;

				// Compute response components
				Dxx = ImageTransformUtils.BoxIntegral(img, r - l + 1, c - b, 2 * l - 1, w)
						- ImageTransformUtils.BoxIntegral(img, r - l + 1, c - l / 2, 2 * l - 1, l) * 3;
				Dyy = ImageTransformUtils.BoxIntegral(img, r - b, c - l + 1, w, 2 * l - 1)
						- ImageTransformUtils.BoxIntegral(img, r - l / 2, c - l + 1, l, 2 * l - 1) * 3;
				Dxy = +ImageTransformUtils.BoxIntegral(img, r - l, c + 1, l, l)
						+ ImageTransformUtils.BoxIntegral(img, r + 1, c - l, l, l)
						- ImageTransformUtils.BoxIntegral(img, r - l, c - l, l, l)
						- ImageTransformUtils.BoxIntegral(img, r + 1, c + 1, l, l);

				// System.out.println("dxx: " + Dxx + ", dyy: " + Dyy + ", Dxy: " + Dxy);

				// Normalise the filter responses with respect to their size
				Dxx *= inverse_area;
				Dyy *= inverse_area;
				Dxy *= inverse_area;

				// Get the determinant of hessian response & laplacian sign
				mResponses[ac][ar] = (Dxx * Dyy - 0.81f * Dxy * Dxy);
				mLaplacian[ac][ar] = (char) (Dxx + Dyy >= 0 ? 1 : 0);
			}
		}
	}

	public double getResponse(int row, int col) {
		return mResponses[col][row];
	}

	public char getLaplacian(int row, int col) {
		return mLaplacian[col][row];
	}

	public int getWidth() {
		return mWidth;
	}

	public int getHeight() {
		return mHeight;
	}

	public int getFilter() {
		return mFilter;
	}

	public int getStep() {
		return mStep;
	}

	public double getResponse(int row, int col, ResponseLayer src) {
		int scale = getWidth() / src.getWidth();
		return getResponse(row * scale, col * scale);
	}

	public float getLaplacian(int row, int col, ResponseLayer src) {
		int scale = getWidth() / src.getWidth();
		return getLaplacian(row * scale, col * scale);
	}
}

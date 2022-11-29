package com.keypointforensics.videotriage.util;

public class KernelUtils {

	public static final int DEFAULT_BLUR_KERNEL_SQUARED_SIZE = 400;
	public static final int DEFAULT_BLUR_KERNEL_SIZE = 20;
	
	public static float[] getBlurKernel(final int blurKernelSize) {
		float matrix[] = new float[blurKernelSize];
		
		final float divisor = blurKernelSize;
		
		for(int i = 0; i < blurKernelSize; ++i) {
			matrix[i] = 1.0f / divisor;
		}
		
		return matrix;
	}
	
}

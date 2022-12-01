package com.keypointforensics.videotriage.detect;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MultiThreadedDetectorAlt extends DetectorAlt {

	protected static final int MINIMUM_DENSITY_THRESHOLD = -20;
	protected static final int MAXIMUM_DENSITY_THRESHOLD = 60000;
	
	private final int NUMBER_OF_CPU_CORES;

	public MultiThreadedDetectorAlt(final int numberOfCpuCores, HaarCascade haarCascade, float baseScale,
			float scaleInc, float increment) {
		super(haarCascade, baseScale, scaleInc, increment);

		NUMBER_OF_CPU_CORES = numberOfCpuCores;
	}

	@Override
	List<Rectangle> getFeatures(final int width, final int height, float maxScale, final int[] weightedGrayImage,
			final int[] weightedGrayImageSquared) {
		final int[] cannyIntegral = cannyPruner.getIntegralCanny(weightedGrayImageSquared, width, height);

		final List<Rectangle> features = new ArrayList<Rectangle>();
		ExecutorService threadPool = Executors.newFixedThreadPool(NUMBER_OF_CPU_CORES);
		for (float scale = baseScale; scale < maxScale; scale *= scale_inc) {
			final int scaledFeatureStep = (int) (scale * haarCascade.cascadeWidth * increment);
			final int scaledFeatureWidth = (int) (scale * haarCascade.cascadeWidth);
			final float scale_f = scale;

			for (int i = 0; i < width - scaledFeatureWidth; i += scaledFeatureStep) {
				final int i_f = i;
				threadPool.execute(new Runnable() {
					public void run() {
						for (int j = 0; j < height - scaledFeatureWidth; j += scaledFeatureStep) {
							final int edges_density = cannyIntegral[i_f + scaledFeatureWidth
									+ (j + scaledFeatureWidth) * width] + cannyIntegral[i_f + (j) * width]
									- cannyIntegral[i_f + (j + scaledFeatureWidth) * width]
									- cannyIntegral[i_f + scaledFeatureWidth + (j) * width];
							final int densityValue = edges_density / scaledFeatureWidth / scaledFeatureWidth;
							
							if (densityValue < MINIMUM_DENSITY_THRESHOLD || densityValue > MAXIMUM_DENSITY_THRESHOLD) {
								continue;
							}

							Rectangle rectangle = haarCascade.getFeature(weightedGrayImage, weightedGrayImageSquared,
									width, height, i_f, j, scale_f, scaledFeatureWidth);

							if (rectangle != null) {
								synchronized (features) {
									features.add(rectangle);
								}
							}
						}
					}
				});
			}
		}

		threadPool.shutdown(); // we won't add anymore

		try {
			threadPool.awaitTermination(60, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return (features);
	}
}
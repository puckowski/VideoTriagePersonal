package com.keypointforensics.videotriage.filter;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class EmbossFilter {

	private final Kernel EMBOSS_KERNEL = new Kernel(3, 3, new float[] {
		-2, 0, 0,
		0, 1, 0,
		0, 0, 2 
	});
	
	public BufferedImage filter(BufferedImage toFilter) {
		BufferedImageOp op = new ConvolveOp(EMBOSS_KERNEL);
		toFilter = op.filter(toFilter, null);

		return toFilter;

	}
}
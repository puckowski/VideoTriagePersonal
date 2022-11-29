package com.keypointforensics.videotriage.blob;

import java.awt.image.BufferedImage;
import java.util.List;

public interface BlobExtractor {

	/*
	 * Author: Daniel Puckowski
	 */
	
	public String getControllerId();
	public void setControllerId(final String controllerId);
	public void extractBlobs(final BufferedImage currentImage);
	public BufferedImage drawBlobBounds(final BufferedImage sourceImage, final int minimumBlobMass);
	public List<Blob> getBlobs();
}

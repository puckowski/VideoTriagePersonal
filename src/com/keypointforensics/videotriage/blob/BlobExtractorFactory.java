package com.keypointforensics.videotriage.blob;

public class BlobExtractorFactory {

	/*
	 * Author: Daniel Puckowski
	 */
	
	public static final String FRAME_DIFFERENCE_EXTRACTOR = "FrameDifference";
	
	public BlobExtractor getExtractor(final String typeOfExtractor, final String controllerId) {
		if(typeOfExtractor.equals(FRAME_DIFFERENCE_EXTRACTOR)) {			
			return new FrameDifferenceBlobExtractor(controllerId);
		}
				
		return null;
	}
}

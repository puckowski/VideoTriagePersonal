package com.keypointforensics.videotriage.report;

import com.keypointforensics.videotriage.blob.context.BlobContextListParser;

public class PedestrianExtractPageGenerator extends BaseDetectionPageGenerator {
	
	public static final String LICENSE_PLATE_PAGE_NAME_SHORT = "pedestrian.html";
	public static final String LICENSE_PLATE_TABLE_HEADER_LABEL = "Pedestrians";
	
	public PedestrianExtractPageGenerator(final BlobContextListParser blobContextListParser, final String absoluteFaceFolderPath, final String reportRoot, final String reportPageHeader) {
		super(blobContextListParser, absoluteFaceFolderPath, reportRoot, reportPageHeader);
	}
	
	@Override
	public String getDetectionPageNameShort() {
		return LICENSE_PLATE_PAGE_NAME_SHORT;
	}

	@Override
	protected String getTableHeaderLabel() {
		return LICENSE_PLATE_TABLE_HEADER_LABEL;
	}

}

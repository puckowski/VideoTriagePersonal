package com.keypointforensics.videotriage.report;

import com.keypointforensics.videotriage.blob.context.BlobContextListParser;

public class CarExtractPageGenerator extends BaseDetectionPageGenerator {
	
	public static final String CAR_PAGE_NAME_SHORT = "car.html";
	public static final String CAR_TABLE_HEADER_LABEL = "Cars";
	
	public CarExtractPageGenerator(final BlobContextListParser blobContextListParser, final String absoluteFaceFolderPath, final String reportRoot, final String reportPageHeader) {
		super(blobContextListParser, absoluteFaceFolderPath, reportRoot, reportPageHeader);
	}
	
	@Override
	public String getDetectionPageNameShort() {
		return CAR_PAGE_NAME_SHORT;
	}

	@Override
	protected String getTableHeaderLabel() {
		return CAR_TABLE_HEADER_LABEL;
	}

}

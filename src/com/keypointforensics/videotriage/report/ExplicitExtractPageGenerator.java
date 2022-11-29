package com.keypointforensics.videotriage.report;

import com.keypointforensics.videotriage.blob.context.BlobContextListParser;

public class ExplicitExtractPageGenerator extends BaseDetectionPageGenerator {
	
	public static final String EXPLICIT_PAGE_NAME_SHORT = "explicit.html";
	public static final String EXPLICIT_TABLE_HEADER_LABEL = "Explicit Images";
	
	public ExplicitExtractPageGenerator(final BlobContextListParser blobContextListParser, final String absoluteFaceFolderPath, final String reportRoot, final String reportPageHeader) {
		super(blobContextListParser, absoluteFaceFolderPath, reportRoot, reportPageHeader);
	}
	
	@Override
	public String getDetectionPageNameShort() {
		return EXPLICIT_PAGE_NAME_SHORT;
	}

	@Override
	protected String getTableHeaderLabel() {
		return EXPLICIT_TABLE_HEADER_LABEL;
	}

}

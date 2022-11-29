package com.keypointforensics.videotriage.report;

import com.keypointforensics.videotriage.blob.context.BlobContextListParser;

public class FaceExtractPageGenerator extends BaseDetectionPageGenerator {
	
	public static final String FACE_PAGE_NAME_SHORT = "face.html";
	public static final String FACE_TABLE_HEADER_LABEL = "Faces";
	
	public FaceExtractPageGenerator(final BlobContextListParser blobContextListParser, final String absoluteFaceFolderPath, final String reportRoot, final String reportPageHeader) {
		super(blobContextListParser, absoluteFaceFolderPath, reportRoot, reportPageHeader);
	}
	
	@Override
	public String getDetectionPageNameShort() {
		return FACE_PAGE_NAME_SHORT;
	}

	@Override
	protected String getTableHeaderLabel() {
		return FACE_TABLE_HEADER_LABEL;
	}

}

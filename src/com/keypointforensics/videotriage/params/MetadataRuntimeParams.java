package com.keypointforensics.videotriage.params;

public class MetadataRuntimeParams {

	/*
	 * Author: Daniel Puckowski
	 */
	
	public static final int DATE_CREATION_MODE_REAL_ONLY       = 0;
	public static final int DATE_CREATION_MODE_INSTANT_ONLY    = 1;
	public static final int DATE_CREATION_MODE_REAL_OR_INSTANT = 2;
	
	private final int DEFAULT_DATE_CREATION_MODE = DATE_CREATION_MODE_REAL_OR_INSTANT;
	private int mCreationDateMode                = DEFAULT_DATE_CREATION_MODE;
	
	public MetadataRuntimeParams() {
		//getRealCreationDateEnabledInitial();
	}
	
	//private void getCreationDateModeInitial() {
	//	mCreationDateMode = DEFAULT_DATE_CREATION_MODE;
	//}
	
	public int getDateCreationMode() {		
		return mCreationDateMode;
	}
	
	public void setDateCreationMode(final int newDateCreationMode) {
		mCreationDateMode = newDateCreationMode;
	}
}

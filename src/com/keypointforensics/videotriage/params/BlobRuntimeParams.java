package com.keypointforensics.videotriage.params;

import java.awt.Color;

public class BlobRuntimeParams {
	
	/*
	 * Author: Daniel Puckowski
	 */
	
	private final int DEFAULT_BLOB_BORDER_WIDTH = 2;
	
	private final Color              DEFAULT_BLOB_BORDER_COLOR = Color.RED;
	private Color mBlobBorderColor = DEFAULT_BLOB_BORDER_COLOR;
	
	private final boolean                DEFAULT_BLOB_BORDER_DISPLAY = true;
	private boolean mBlobBorderDisplay = DEFAULT_BLOB_BORDER_DISPLAY;
	
	private final boolean             DEFAULT_ATTEMPT_TO_MERGE = true;
	private boolean mAttemptToMerge = DEFAULT_ATTEMPT_TO_MERGE;
	
	private final boolean             DEFAULT_HIGHLIGHT_BLOBS = true;
	private boolean mHighlightBlobs = DEFAULT_HIGHLIGHT_BLOBS;
	
	private final boolean          DEFAULT_EXPAND_BLOBS = true;
	private boolean mExpandBlobs = DEFAULT_EXPAND_BLOBS;
	
	private final double               DEFAULT_BLOB_EXPANSION_PERCENT = 0.05;
	private double mExpansionPercent = DEFAULT_BLOB_EXPANSION_PERCENT;
	
	private final boolean                  SAVE_BLOB_COORDINATES = false;
	private boolean mSaveBlobCoordinates = SAVE_BLOB_COORDINATES;
	
	public BlobRuntimeParams() {
		getBorderColorInitial();
		getBorderDisplayInitial();
		getAttemptToMergeInitial();
		getHighlightBlobsInitial();
		getExpandBlobsInitial();
		getExpansionPercentInitial();
		getSaveBlobCoordinatesInitial();
	}
	
	private void getSaveBlobCoordinatesInitial() {
		mSaveBlobCoordinates = SAVE_BLOB_COORDINATES;
	}
	
	public boolean getSaveBlobCoordinates() {
		return mSaveBlobCoordinates;
	}
	
	public void setSaveBlobCoordintates(final boolean newSaveBlobCoordinates) {
		mSaveBlobCoordinates = newSaveBlobCoordinates;
	}
	
	private void getExpansionPercentInitial() {
		mExpansionPercent = DEFAULT_BLOB_EXPANSION_PERCENT;
	}
	
	public double getExpansionPercent() {		
		return mExpansionPercent;
	}
	
	public void setExpansionPercent(final double newExpansionPercent) {
		mExpansionPercent = newExpansionPercent;
	}
	
	private void getExpandBlobsInitial() {
		mExpandBlobs = DEFAULT_EXPAND_BLOBS;
	}
	
	public boolean getExpandBlobs() {		
		return mExpandBlobs;
	}
	
	public void setExpandBlobs(final boolean newExpandBlobs) {
		mExpandBlobs = newExpandBlobs;
	}
	
	private void getHighlightBlobsInitial() {
		mHighlightBlobs = DEFAULT_HIGHLIGHT_BLOBS;
	}
	
	public boolean getHighlightBlobs() {		
		return mHighlightBlobs;
	}
	
	public void setHighlightBlobs(final boolean newHighlightBlobs) {
		mHighlightBlobs = newHighlightBlobs;
	}
	
	private void getAttemptToMergeInitial() {
		mAttemptToMerge = DEFAULT_ATTEMPT_TO_MERGE;
	}
	
	public boolean getAttemptToMerge() {		
		return mAttemptToMerge;
	}
	
	public void setAttemptToMerge(final boolean newAttemptToMerge) {
		mAttemptToMerge = newAttemptToMerge;
	}
	
	private void getBorderDisplayInitial() {
		mBlobBorderDisplay = DEFAULT_BLOB_BORDER_DISPLAY;
	}
	
	public boolean getBorderDisplay() {		
		return mBlobBorderDisplay;
	}
	
	public void setBorderDisplay(final boolean newBorderDisplay) {
		mBlobBorderDisplay = newBorderDisplay;
	}
	
	public int getBorderWidthInt() {		
		return DEFAULT_BLOB_BORDER_WIDTH; 
	}
	
	private void getBorderColorInitial() {
		if(mBlobBorderColor == null) {
			mBlobBorderColor = DEFAULT_BLOB_BORDER_COLOR;
		}
	}
	
	public Color getBorderColor() {		
		return mBlobBorderColor;
	}
	
	public void setBorderColor(final Color newBorderColor) {
		mBlobBorderColor = newBorderColor;
	}
}

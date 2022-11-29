package com.keypointforensics.videotriage.detect;

import com.keypointforensics.videotriage.gui.main.GuiMain;

public abstract class ExtractDetectionModule {

	/*
	 * Author: Daniel Puckowski
	 */
	
	protected final int NO_AUTOMATIC_GENTLE_FILTER        = -1;
	protected final int DEFAULT_SEARCH_SLEEP_MILLISECONDS = 3000;
	
	protected final GuiMain GUI_MAIN;
	protected final String DATABASE_NAME;
	protected final String EXTRACT_DIRECTORY;
	
	protected String mDatabasePath;
	
	protected int mNumberOfCpuCores;
	protected boolean mExhaustiveSearch;
	protected double mCustomSimilarityPercent;
	protected double mMinimumFaceArea;
	protected boolean mPreAnalyzeResults;
	
	protected HaarDetector mHaarDetector;
			
	public ExtractDetectionModule(final GuiMain guiMain, final String databaseName, final String extractDirectory) {
		GUI_MAIN          = guiMain;
		DATABASE_NAME     = databaseName;
		EXTRACT_DIRECTORY = extractDirectory;
		
		mCustomSimilarityPercent = SimpleFalsePositiveRemover.DEFAULT_SURF_FREE_ORIENTED_MATCH_PERCENT;
		mPreAnalyzeResults = true;
		
		createDetectionDatabaseFolderIfNecessary();
	}
	
	protected abstract void createDetectionDatabaseFolderIfNecessary();
	public abstract void performDatabaseCrawlAction();
	
	public void setMinimumDetectionSize(int newMinimumFaceSize) {
		mMinimumFaceArea = newMinimumFaceSize / 100.0;
	}
	
	public void setSimilarityPercent(int newSimilarityPercent) {
		mCustomSimilarityPercent = newSimilarityPercent;
	}
	
	public void setNumberOfCpuCores(int newNumberOfCpuCores) {
		mNumberOfCpuCores = newNumberOfCpuCores;
	}
	
	public void setExhaustiveSearch(boolean newExhaustiveSearchState) {
		mExhaustiveSearch = newExhaustiveSearchState;
	}
	
	public String getDetectionDatabaseFolder() {
		return mDatabasePath;
	}
	
	public void setPreAnalyzeResults(boolean preAnalyzeResults) {
		mPreAnalyzeResults = preAnalyzeResults;
	}
	
}

package com.keypointforensics.videotriage.detect;

import java.io.File;

import com.keypointforensics.videotriage.gui.main.GuiMain;
import com.keypointforensics.videotriage.util.FileUtils;

public abstract class DetectionModule {

	/*
	 * Author: Daniel Puckowski
	 */
	
	protected final int NO_AUTOMATIC_GENTLE_FILTER        = -1;
	protected final int DEFAULT_SEARCH_SLEEP_MILLISECONDS = 3000;
	
	protected final GuiMain GUI_MAIN;
	protected final String DATABASE_NAME;
	
	protected String mDatabasePath;
	
	protected int mNumberOfCpuCores;
	protected boolean mExhaustiveSearch;
	protected double mCustomSimilarityPercent;
	protected double mMinimumFaceArea;
	protected boolean mPreAnalyzeResults;
	
	protected HaarDetector mHaarDetector;
		
	public DetectionModule(final GuiMain guiMain, final String databaseName) {
		GUI_MAIN      = guiMain;
		DATABASE_NAME = databaseName;
		
		mCustomSimilarityPercent = SimpleFalsePositiveRemover.DEFAULT_SURF_FREE_ORIENTED_MATCH_PERCENT;
		mPreAnalyzeResults = true;
		
		createDetectionDatabaseFolderIfNecessary();
	}
	
	protected abstract void createDetectionDatabaseFolderIfNecessary();
	public abstract void performDatabaseCrawlAction();
	
	public String getCaptureDirectoryFromDatabasePath() {
		String databaseNameShort = FileUtils.getShortFilename(DATABASE_NAME);
		
		File databaseFolder = new File(FileUtils.CAPTURES_DIRECTORY + databaseNameShort);
		
		if(databaseFolder.exists() == false) {
			databaseFolder.mkdir();
		}
		
		return databaseFolder.getAbsolutePath();
	}
	
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

package com.keypointforensics.videotriage.legacy;

import java.util.ArrayList;

import com.keypointforensics.videotriage.list.SortedList;
import com.keypointforensics.videotriage.report.ReportCaptureBundle;

public class VideoFalsePositiveBundleLegacy {

	/*
	 * Author: Daniel Puckowski
	 */
	
	private ArrayList<String> mCaptureFilenames;
	private ArrayList<String> mFalsePositiveFilenames;
	private String mPath;
	
	public VideoFalsePositiveBundleLegacy(final String path) {
		mCaptureFilenames = new ArrayList<String>();
		mFalsePositiveFilenames = new ArrayList<String>();
		mPath = path;
		
		mCaptureFilenames = (ArrayList<String>) FileUtilsLegacy.parseDirectoryRecursiveForImagesWithoutFalsePositives(mPath);
	}

	public VideoFalsePositiveBundleLegacy(final String path, final boolean simpleRecursiveParse) {
		mCaptureFilenames = new ArrayList<String>();
		mFalsePositiveFilenames = new ArrayList<String>();
		mPath = path;
		
		if(simpleRecursiveParse == true) {
			mCaptureFilenames = (ArrayList<String>) FileUtilsLegacy.parseDirectoryRecursiveForAll(mPath);
		} else {
			mCaptureFilenames = (ArrayList<String>) FileUtilsLegacy.parseDirectoryRecursiveForImagesWithoutFalsePositives(mPath);
		}
	}
	
	public VideoFalsePositiveBundleLegacy(final ArrayList<String> listOfImages) {
		mCaptureFilenames = new ArrayList<String>();
		mFalsePositiveFilenames = new ArrayList<String>();
		
		mCaptureFilenames = listOfImages;
	}
	
	public void removeCaptureFilenames(final ArrayList<String> filenamesToDelete) {
		for(String filenameToDelete : filenamesToDelete) {
			mCaptureFilenames.remove(filenameToDelete);
		}
	}
	
	public ArrayList<String> getCaptureFilenames() {		
		return mCaptureFilenames;
	}
	
	public SortedList<ReportCaptureBundle> getCaptureFilenamesSorted() {
		SortedList<ReportCaptureBundle> reportCaptureBundles = new SortedList<ReportCaptureBundle>();
		
		for(String absoluteCaptureFilename : mCaptureFilenames) {
			reportCaptureBundles.add(new ReportCaptureBundle(absoluteCaptureFilename));
		}
		
		return reportCaptureBundles;
	}
	
	public ArrayList<String> getFalsePositiveFilenames() {
		return mFalsePositiveFilenames;
	}
}

package com.keypointforensics.videotriage.legacy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class VideoFalsePositiveRemoverLegacy implements FrsThreadLegacy {
	
	/*
	 * Author: Daniel Puckowski
	 */
	
	private final static double DEFAULT_FALSE_POSITIVE_THRESHOLD = 0.10;

	private ArrayList<String> mFilenames;
	
	private int mNumberComputed;
	
	public VideoFalsePositiveRemoverLegacy() {
		mFilenames = null;
		mNumberComputed = 0;
	}
	
	public VideoFalsePositiveRemoverLegacy(ArrayList<String> filenames) {
		mFilenames = filenames;
		mNumberComputed = 0;
	}

	public void removeFalsePositives(ArrayList<String> filenames) throws IOException {
		if(filenames == null || filenames.isEmpty() == true) { 
			return;
		}
	
		ArrayList<VideoFrameBundleLegacy> VideoFrameBundleLegacys = new ArrayList<VideoFrameBundleLegacy>();
		ArrayList<VideoFrameBundleLegacy> bundlesToRemove = new ArrayList<VideoFrameBundleLegacy>();
		VideoFrameBundleLegacy videoBundle = null;
		
		for(int i = 0; i < filenames.size(); ++i) {
			
			videoBundle = new VideoFrameBundleLegacy(filenames.get(i), ImageIO.read(new File(filenames.get(i))));			
			VideoFrameBundleLegacys.add(videoBundle);
		}
		
		final int originalDetectionCount = filenames.size();
		int toleranceThreshold = (int) Math.ceil(((double) (DEFAULT_FALSE_POSITIVE_THRESHOLD - 0.090) * (double) originalDetectionCount));
			
		videoBundle = null;
		
		for(int i = 0; i < VideoFrameBundleLegacys.size(); ++i) { 
			bundlesToRemove.clear();
			videoBundle = VideoFrameBundleLegacys.get(i);
			bundlesToRemove.add(videoBundle);
			
			for(int n = 0; n < VideoFrameBundleLegacys.size(); ++n) {
				if(n == i) { 
					continue;
				}
			
				if(ImageUtilsLegacy.getImagesPercentageDifference(videoBundle.getImage(), VideoFrameBundleLegacys.get(n).getImage(), 152) <= (ImageUtilsLegacy.DEFAULT_IMAGE_DIFFERENCE_SIMILAR_PERCENTAGE)) {
					bundlesToRemove.add(VideoFrameBundleLegacys.get(n));
				}
				else if(ImageUtilsLegacy.getImagesSimilarityScore(videoBundle.getImage(), VideoFrameBundleLegacys.get(n).getImage(), 152) >= (ImageUtilsLegacy.DEFAULT_IMAGE_SSIM_VERY_SIMILAR_PERCENTAGE - 4.0)) { //.DEFAULT_IMAGE_SIMILAR_PERCENTAGE) {
					bundlesToRemove.add(VideoFrameBundleLegacys.get(n));
				}
			}
			
			if(bundlesToRemove.size() >= toleranceThreshold) { 
				for(int t = 0; t < bundlesToRemove.size(); ++t) {
					VideoFrameBundleLegacys.remove(bundlesToRemove.get(t));
				}
				
				i--;
			}
			
			mNumberComputed++;
		}
	}
	
	public void removeFalsePositives(String filename, ArrayList<String> filenames, final boolean deleteSelf, ArrayList<VideoFrameBundleLegacy> preexistingVideoFrameBundleLegacys) throws IOException {
		if(filenames == null || filenames.isEmpty() == true) { 	
			return;
		}
		
		ArrayList<VideoFrameBundleLegacy> bundlesToRemove = new ArrayList<VideoFrameBundleLegacy>();
		ArrayList<VideoFrameBundleLegacy> VideoFrameBundleLegacys = null;
		
		if(preexistingVideoFrameBundleLegacys != null && preexistingVideoFrameBundleLegacys.isEmpty() == false) {
			VideoFrameBundleLegacys = preexistingVideoFrameBundleLegacys;
		} else {
			VideoFrameBundleLegacys = buildVideoFrameBundles(filenames);
		}
	
		VideoFrameBundleLegacy videoBundle = null;
		
		for(int i = 0; i < VideoFrameBundleLegacys.size(); ++i) { 
			if(VideoFrameBundleLegacys.get(i).getFilename().equals(filename) == false) {
				continue;
			}
			
			bundlesToRemove.clear();
			videoBundle = VideoFrameBundleLegacys.get(i);
			
			if(deleteSelf == true) {
				bundlesToRemove.add(videoBundle);
			}
			
			for(int n = 0; n < VideoFrameBundleLegacys.size(); ++n) {
				if(n == i) {
					continue;
				}
			
				if(ImageUtilsLegacy.getImagesPercentageDifference(videoBundle.getImage(), VideoFrameBundleLegacys.get(n).getImage(), 152) <= (ImageUtilsLegacy.DEFAULT_IMAGE_DIFFERENCE_SIMILAR_PERCENTAGE)) {
					bundlesToRemove.add(VideoFrameBundleLegacys.get(n));
				}
				else if(ImageUtilsLegacy.getImagesSimilarityScore(videoBundle.getImage(), VideoFrameBundleLegacys.get(n).getImage(), 152) >= (ImageUtilsLegacy.DEFAULT_IMAGE_SSIM_VERY_SIMILAR_PERCENTAGE)) {//(ImageUtilsLegacy.DEFAULT_IMAGE_SIMILAR_PERCENTAGE)) {
					bundlesToRemove.add(VideoFrameBundleLegacys.get(n));
				}
			}

			for(int t = 0; t < bundlesToRemove.size(); ++t) {
				VideoFrameBundleLegacys.remove(bundlesToRemove.get(t));
			}
				
			break;
		}
	}

	public ArrayList<VideoFrameBundleLegacy> buildVideoFrameBundles(ArrayList<String> filenames) throws IOException {
		filenames = SearchUtilsLegacy.formatResultList(filenames);
		
		ArrayList<VideoFrameBundleLegacy> VideoFrameBundleLegacys = new ArrayList<VideoFrameBundleLegacy>();
		VideoFrameBundleLegacy videoBundle = null;
		
		for(int i = 0; i < filenames.size(); ++i) {
			videoBundle = new VideoFrameBundleLegacy(filenames.get(i), ImageIO.read(new File(filenames.get(i))));			
			VideoFrameBundleLegacys.add(videoBundle);
		}
		
		return VideoFrameBundleLegacys;
	}
	
	@Override
	public void run() {		
		boolean success = true;
		
		if(mFilenames != null && mFilenames.size() > 0) {
			try {
				removeFalsePositives(mFilenames);
			} catch (IOException e) {				
				mNumberComputed = mFilenames.size();
	
				success = false; 
			}
		}
		
		if(success == true) {

		}
	}

	@Override
	public void cleanup() {
		if (mFilenames != null) {
			mFilenames.clear();
			mFilenames = null;
		}
	
		mNumberComputed = 0;
	}

	@Override
	public int getObjectCount() {
		return mFilenames.size();
	}

	@Override
	public int numberOfObjectsComputed() {
		return mNumberComputed;
	}

	@Override
	public boolean hasFinishedComputing() {
		if(mFilenames == null) {
			return true; 
		}
		
		if(mNumberComputed == mFilenames.size()) {
			return true;
		} else {
			return false;
		}
	}
}

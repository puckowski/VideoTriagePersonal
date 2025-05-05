package com.keypointforensics.videotriage.detect;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.keypointforensics.videotriage.image.match.ORBFeatureExtractor;
import com.keypointforensics.videotriage.image.match.ORBMatcher;
import com.keypointforensics.videotriage.image.match.OrbKeypoint;
import com.keypointforensics.videotriage.legacy.FileUtilsLegacy;
import com.keypointforensics.videotriage.progress.ProgressBundle;
import com.keypointforensics.videotriage.util.ImageUtils;

public class SimpleFalsePositiveRemover {

	public static final double DEFAULT_ORB_MATCH_PERCENT = 70.0;
	
	public SimpleFalsePositiveRemover() {
		
	}
	
	public void removeFalsePositives(String directoryToFilter, double customMatchPercent, int numberOfFilesToProcess, ProgressBundle progressBundle) {
		final ArrayList<String> filenames = (ArrayList<String>) FileUtilsLegacy.parseDirectoryRecursiveForAll(directoryToFilter);
	
		BufferedImage originalImage, compareImage;
		File toDelete;
		
		for(int i = 0; i < filenames.size(); ++i) { 
			originalImage = ImageUtils.loadBufferedImage(filenames.get(i));
			
			if(originalImage == null) {
				continue;
			}
			
			if (originalImage == null) {
				continue;
			}

			final List<OrbKeypoint> keypoints = ORBFeatureExtractor.getKeypointsForImage(ImageUtils.getGrayscaleArray(originalImage));
			
			for(int n = i + 1; n < (i + numberOfFilesToProcess) && n < filenames.size(); ++n) {
				//if(n == i) { 
				//	continue;
				//}
				
				compareImage = ImageUtils.loadBufferedImage(filenames.get(n));
				
				if(compareImage == null) {
					continue;
				}

				final List<OrbKeypoint> compareKeypoints = ORBFeatureExtractor.getKeypointsForImage(ImageUtils.getGrayscaleArray(compareImage));
				final List<ORBMatcher.Match> matches = ORBMatcher.match(keypoints, compareKeypoints);

				//TODO const
				if(((double) matches.size() / (double) keypoints.size()) >= customMatchPercent) {
					toDelete = new File(filenames.get(n));
					toDelete.delete();
					
					//i--;
					//n--;
					
					numberOfFilesToProcess++;
				}
				/*else if(ImageUtilsLegacy.getImagesPercentageDifference(originalImage, compareImage, 152) <= 18) {
					toDelete = new File(filenames.get(n));
					toDelete.delete();
					
					//i--;
					n--;
					
					//numberOfFilesToProcess++;
				}
				else if(ImageUtilsLegacy.getImagesSimilarityScore(originalImage, compareImage, 152) >= 0.29) {
					toDelete = new File(filenames.get(n));
					toDelete.delete();
					
					//i--;
					n--;
					
					//numberOfFilesToProcess++;
				}*/
			}
			
			progressBundle.progressBar.setValue(i);
			progressBundle.progressBar.repaint();
		}
	}
	
	public void removeFalsePositives(String directoryToFilter, int numberOfFilesToProcess, ProgressBundle progressBundle) {
		final ArrayList<String> filenames = (ArrayList<String>) FileUtilsLegacy.parseDirectoryRecursiveForAll(directoryToFilter);
	
		BufferedImage originalImage, compareImage;
		File toDelete;
		
		for(int i = 0; i < filenames.size(); ++i) { 
			originalImage = ImageUtils.loadBufferedImage(filenames.get(i));
			
			if(originalImage == null) {
				continue;
			}

			final List<OrbKeypoint> keypoints = ORBFeatureExtractor.getKeypointsForImage(ImageUtils.getGrayscaleArray(originalImage));
			
			for(int n = i; n < (i + numberOfFilesToProcess) && n < filenames.size(); ++n) {
				if(n == i) { 
					continue;
				}
				
				compareImage = ImageUtils.loadBufferedImage(filenames.get(n));
				
				if(compareImage == null) {
					continue;
				}
				
				final List<OrbKeypoint> compareKeypoints = ORBFeatureExtractor.getKeypointsForImage(ImageUtils.getGrayscaleArray(compareImage));
				final List<ORBMatcher.Match> matches = ORBMatcher.match(keypoints, compareKeypoints);
				
				//TODO const
				if(((double) matches.size() / (double) keypoints.size()) >= DEFAULT_ORB_MATCH_PERCENT) {
					toDelete = new File(filenames.get(n));
					toDelete.delete();
					
					//i--;
					//n--;
					
					numberOfFilesToProcess++;
				}
				/*else if(ImageUtilsLegacy.getImagesPercentageDifference(originalImage, compareImage, 152) <= mCurrentImageDifferenceThreshold) {
					toDelete = new File(filenames.get(n));
					toDelete.delete();
					
					i--;
					n--;
					
					numberOfFilesToProcess++;
				}
				else if(ImageUtilsLegacy.getImagesSimilarityScore(originalImage, compareImage, 152) >= mCurrentImageSimilarityThreshold) {
					toDelete = new File(filenames.get(n));
					toDelete.delete();
					
					i--;
					n--;
					
					numberOfFilesToProcess++;
				}*/
			}
			
			progressBundle.progressBar.setValue(progressBundle.progressBar.getValue() + 1);
			progressBundle.progressBar.repaint();
		}
	}
	
}

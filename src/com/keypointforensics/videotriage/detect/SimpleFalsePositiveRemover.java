package com.keypointforensics.videotriage.detect;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import com.keypointforensics.videotriage.image.match.SurfComparator;
import com.keypointforensics.videotriage.legacy.FileUtilsLegacy;
import com.keypointforensics.videotriage.progress.ProgressBundle;
import com.keypointforensics.videotriage.util.ImageUtils;

public class SimpleFalsePositiveRemover {

	public static final double DEFAULT_SURF_FREE_ORIENTED_MATCH_PERCENT = 70.0;
	
	public SimpleFalsePositiveRemover() {
		
	}
	
	public void removeFalsePositives(String directoryToFilter, double customMatchPercent, int numberOfFilesToProcess, ProgressBundle progressBundle) {
		final ArrayList<String> filenames = (ArrayList<String>) FileUtilsLegacy.parseDirectoryRecursiveForAll(directoryToFilter);
	
		BufferedImage originalImage, compareImage;
		File toDelete;
		
		SurfComparator surfComparator = new SurfComparator(false);
		
		for(int i = 0; i < filenames.size(); ++i) { 
			originalImage = ImageUtils.loadBufferedImage(filenames.get(i));
			
			if(originalImage == null) {
				continue;
			}
			
			surfComparator.init(originalImage);
			
			for(int n = i + 1; n < (i + numberOfFilesToProcess) && n < filenames.size(); ++n) {
				//if(n == i) { 
				//	continue;
				//}
				
				compareImage = ImageUtils.loadBufferedImage(filenames.get(n));
				
				if(compareImage == null) {
					continue;
				}

				//TODO const
				if(surfComparator.compare(compareImage) >= customMatchPercent) {
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
		
		SurfComparator surfComparator = new SurfComparator(false);
		
		for(int i = 0; i < filenames.size(); ++i) { 
			originalImage = ImageUtils.loadBufferedImage(filenames.get(i));
			
			if(originalImage == null) {
				continue;
			}
			
			surfComparator.init(originalImage);
			
			for(int n = i; n < (i + numberOfFilesToProcess) && n < filenames.size(); ++n) {
				if(n == i) { 
					continue;
				}
				
				compareImage = ImageUtils.loadBufferedImage(filenames.get(n));
				
				if(compareImage == null) {
					continue;
				}

				//TODO const
				if(surfComparator.compare(compareImage) >= DEFAULT_SURF_FREE_ORIENTED_MATCH_PERCENT) {
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

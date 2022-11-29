package com.keypointforensics.videotriage.detect;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import com.keypointforensics.videotriage.progress.ProgressBundle;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.HashUtils;
import com.keypointforensics.videotriage.util.ImageUtils;
import com.keypointforensics.videotriage.util.ProgressUtils;

public class HaarDetector {

	public static final double DEFAULT_EXPANSION_WIDTH_PERCENT  = 0.05;
	public static final double DEFAULT_EXPANSION_HEIGHT_PERCENT = 0.05;
	
	private String mDatabasePath;
	private int mNumberOfCpuCores;
	private double mMinimumDetectionArea;
	
	private double mExpansionWidthPercent;
	private double mExpansionHeightPercent;
	
	public HaarDetector(final String databasePath, final int numberOfCpuCores, final double minimumDetectionArea) {
		mDatabasePath = databasePath;
		mNumberOfCpuCores = numberOfCpuCores;
		mMinimumDetectionArea = minimumDetectionArea;
		
		setExpansionWidthPercent(DEFAULT_EXPANSION_WIDTH_PERCENT);
		setExpansionHeightPercent(DEFAULT_EXPANSION_HEIGHT_PERCENT);
	}
	
	public void setExpansionWidthPercent(final double expansionWidthPercent) {
		mExpansionWidthPercent = expansionWidthPercent;
	}
	
	public void setExpansionHeightPercent(final double expansionHeightPercent) {
		mExpansionHeightPercent = expansionHeightPercent;
	}
	
	private int areaOfImage(final BufferedImage image) {
		return image.getWidth() * image.getHeight();
	}
	
	private Rectangle expandRectangle(Rectangle currentRectangle, BufferedImage searchImage) {
		int expandWidth = (int) ((double) searchImage.getWidth() * mExpansionWidthPercent);
		int expandHeight = (int) ((double) searchImage.getHeight() * mExpansionHeightPercent);
		
		currentRectangle.x -= expandWidth;
		currentRectangle.y -= expandHeight;
			
		if(currentRectangle.x < 0) {
			currentRectangle.x = 0;
		}
			
		if(currentRectangle.y < 0) {
			currentRectangle.y = 0;
		}
		
		currentRectangle.width += expandWidth;
		currentRectangle.height += expandHeight;
			
		if(currentRectangle.width > searchImage.getWidth()) {
			currentRectangle.width = searchImage.getWidth();
		}
			
		if(currentRectangle.height > searchImage.getHeight()) {
			currentRectangle.height = searchImage.getHeight();
		}
		
		return currentRectangle;
	}
	
	public ProgressBundle performExhaustiveSearch(final ArrayList<String> haarCascadeList, ProgressBundle searchProgressBundle, final ArrayList<String> allCapturePaths) {
		searchProgressBundle = ProgressUtils.getProgressBundle("Searching Captures...", (allCapturePaths.size() * haarCascadeList.size()) + 1);
		
		HaarCascade cascade;
		DetectorAlt detector;
		List<Rectangle> rectangles;
		BufferedImage searchImage, nextSubimage;
		File toSave = null;
		String toSaveWithoutExtension;
		int originalArea, subArea;
		
		for(String faceCascade : haarCascadeList) {		
			cascade = HaarCascade.create(faceCascade);	
			detector = new MultiThreadedDetectorAlt(mNumberOfCpuCores, cascade, 2f, 1.1f, 0.05f);
			
			for(String capturePath : allCapturePaths) {
				searchImage = ImageUtils.loadBufferedImage(capturePath);
				originalArea = areaOfImage(searchImage);
				
				rectangles = detector.getFeatures(capturePath);
			    rectangles = RectanglePruner.merge(rectangles, 1);
					    
			    for(Rectangle currentBounds : rectangles) {
			    	currentBounds = expandRectangle(currentBounds, searchImage);
					nextSubimage = searchImage.getSubimage(currentBounds.x, currentBounds.y, currentBounds.width, currentBounds.height);				
					subArea = areaOfImage(nextSubimage);
					
					if(subArea < ((double) originalArea * mMinimumDetectionArea)) {
						continue;
					} else if(ImageUtils.getShannonEntropy(nextSubimage) < 7.0) {
						continue;
					}
					
					//if((double) subArea / (double) originalArea >= 5.0) {
					try {
						//toSave = new File(mDatabasePath + "face_" + HashUtils.getBlobImageSha1Hash(nextSubimage) + ".jpg");
						toSaveWithoutExtension = FileUtils.getShortFilename(capturePath);
						toSaveWithoutExtension = toSaveWithoutExtension.substring(0, toSaveWithoutExtension.lastIndexOf("."));
						
						toSave = new File(mDatabasePath + toSaveWithoutExtension + "_" + HashUtils.getBlobImageSha1Hash(nextSubimage) + ".jpg");
					} catch (NoSuchAlgorithmException noSuchAlgorithmException) {
						noSuchAlgorithmException.printStackTrace();
					}
					
					if(toSave.exists() == false) {
						ImageUtils.saveBufferedImage(nextSubimage, toSave.getAbsolutePath());
					}
					//}
				}
			    
			    searchProgressBundle.progressBar.setValue(searchProgressBundle.progressBar.getValue() + 1);
				searchProgressBundle.progressBar.repaint();
			}
		}
		
		return searchProgressBundle;
	}
	
	public ProgressBundle performStandardSearch(final String haarCascade, ProgressBundle searchProgressBundle, final ArrayList<String> allCapturePaths) {
		searchProgressBundle = ProgressUtils.getProgressBundle("Searching Captures...", allCapturePaths.size() + 1);

		HaarCascade cascade = HaarCascade.create(FileUtils.CASCADES_DIRECTORY + haarCascade);
		DetectorAlt detector = new MultiThreadedDetectorAlt(mNumberOfCpuCores, cascade, 2f, 1.1f, 0.05f);
		List<Rectangle> rectangles;
		BufferedImage searchImage, nextSubimage;
		File toSave = null;
		String toSaveWithoutExtension;
		int originalArea, subArea;
				
		for(String capturePath : allCapturePaths) {
			searchImage = ImageUtils.loadBufferedImage(capturePath);
			originalArea = areaOfImage(searchImage);
			
			rectangles = detector.getFeatures(capturePath);
		    rectangles = RectanglePruner.merge(rectangles, 1);
				    
		    for(Rectangle currentBounds : rectangles) {
		    	currentBounds = expandRectangle(currentBounds, searchImage);
				nextSubimage = searchImage.getSubimage(currentBounds.x, currentBounds.y, currentBounds.width, currentBounds.height);				
				subArea = areaOfImage(nextSubimage);
				
				if(subArea < ((double) originalArea * mMinimumDetectionArea)) {
					continue;
				} else if(ImageUtils.getShannonEntropy(nextSubimage) < 7.0) {
					continue;
				}
				
				//if((double) subArea / (double) originalArea >= 5.0) {
				try {
					//toSave = new File(mDatabasePath + "face_" + HashUtils.getBlobImageSha1Hash(nextSubimage) + ".jpg");
					toSaveWithoutExtension = FileUtils.getShortFilename(capturePath);
					toSaveWithoutExtension = toSaveWithoutExtension.substring(0, toSaveWithoutExtension.lastIndexOf("."));
					
					toSave = new File(mDatabasePath + toSaveWithoutExtension + "_" + HashUtils.getBlobImageSha1Hash(nextSubimage) + ".jpg");
				} catch (NoSuchAlgorithmException noSuchAlgorithmException) {
					noSuchAlgorithmException.printStackTrace();
				}
				
				if(toSave.exists() == false) {
					ImageUtils.saveBufferedImage(nextSubimage, toSave.getAbsolutePath());
				}
				//}
			}
		    
		    searchProgressBundle.progressBar.setValue(searchProgressBundle.progressBar.getValue() + 1);
			searchProgressBundle.progressBar.repaint();
		}
		
		return searchProgressBundle;
	}
	
}

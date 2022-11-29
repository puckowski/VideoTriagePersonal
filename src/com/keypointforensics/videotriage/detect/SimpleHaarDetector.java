package com.keypointforensics.videotriage.detect;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ImageUtils;

public class SimpleHaarDetector {

	public static final double DEFAULT_EXPANSION_WIDTH_PERCENT  = 0.01;
	public static final double DEFAULT_EXPANSION_HEIGHT_PERCENT = 0.01;
	
	public static final int DEFAULT_MAXIMUM_EXPANSION_WIDTH  = 25;
	public static final int DEFAULT_MAXIMUM_EXPANSION_HEIGHT = 25;
	
	private int mNumberOfCpuCores;
	
	private double mExpansionWidthPercent;
	private double mExpansionHeightPercent;
	
	public SimpleHaarDetector(final int numberOfCpuCores) {
		mNumberOfCpuCores = numberOfCpuCores;
		
		setExpansionWidthPercent(DEFAULT_EXPANSION_WIDTH_PERCENT);
		setExpansionHeightPercent(DEFAULT_EXPANSION_HEIGHT_PERCENT);
	}
	
	public void setExpansionWidthPercent(final double expansionWidthPercent) {
		mExpansionWidthPercent = expansionWidthPercent;
	}
	
	public void setExpansionHeightPercent(final double expansionHeightPercent) {
		mExpansionHeightPercent = expansionHeightPercent;
	}
	
	private Rectangle expandRectangle(Rectangle currentRectangle, BufferedImage searchImage) {
		int expandWidth = (int) ((double) searchImage.getWidth() * mExpansionWidthPercent);
		int expandHeight = (int) ((double) searchImage.getHeight() * mExpansionHeightPercent);
		
		if(expandWidth > DEFAULT_MAXIMUM_EXPANSION_WIDTH) {
			expandWidth = DEFAULT_MAXIMUM_EXPANSION_WIDTH;
		}
		
		if(expandHeight > DEFAULT_MAXIMUM_EXPANSION_HEIGHT) {
			expandHeight = DEFAULT_MAXIMUM_EXPANSION_HEIGHT;
		}
		
		currentRectangle.x -= expandWidth;
		currentRectangle.y -= expandHeight;
			
		if(currentRectangle.x < 0) {
			currentRectangle.x = 0;
		}
			
		if(currentRectangle.y < 0) {
			currentRectangle.y = 0;
		}
		
		currentRectangle.width += (expandWidth * 2);
		currentRectangle.height += (expandHeight * 2);
			
		if(currentRectangle.width > searchImage.getWidth()) {
			currentRectangle.width = searchImage.getWidth();
		}
			
		if(currentRectangle.height > searchImage.getHeight()) {
			currentRectangle.height = searchImage.getHeight();
		}
		
		return currentRectangle;
	}
	
	public ArrayList<Rectangle> performSearch(final String haarCascade, final ArrayList<String> allCapturePaths) {		
		ArrayList<Rectangle> allRectangles = new ArrayList<Rectangle>();
		
		HaarCascade cascade;
		DetectorAlt detector;
		List<Rectangle> rectangles;
		//BufferedImage searchImage;
		
		cascade = HaarCascade.create(FileUtils.CASCADES_DIRECTORY + haarCascade);	
		detector = new MultiThreadedDetectorAlt(mNumberOfCpuCores, cascade, 1f, 1.1f, 0.05f); // 1f, 1.25f, 0.1f);
		
		for(String capturePath : allCapturePaths) {	
			//searchImage = ImageUtils.loadBufferedImage(capturePath);

			rectangles = detector.getFeatures(capturePath);
			rectangles = RectanglePruner.merge(rectangles, 1);
			    
			for(Rectangle currentBounds : rectangles) {
			    //currentBounds = expandRectangle(currentBounds, searchImage);
				allRectangles.add(currentBounds);
			}
		}
		
		return allRectangles;
	}
	
}

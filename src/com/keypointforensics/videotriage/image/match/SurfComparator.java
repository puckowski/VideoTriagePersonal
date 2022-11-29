
package com.keypointforensics.videotriage.image.match;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import org.apache.commons.math3.util.Pair;

public class SurfComparator extends JPanel {

	private static final long serialVersionUID = 1433189931871871332L;

	private final boolean mUpright;
	
	private Surf mSurfA;
	private Surf mSurfB;

	private Map<SURFInterestPoint, SURFInterestPoint> mMatchingPoints;
	
	private int mNumberOfMatches;
	private double mMatchPercent;
	private List<Pair<Float, Float>> mMatchList;
	private int mNumberOfReferencePoints;
	
	public SurfComparator(boolean upright) {
		mUpright = upright;
	}
	
	public void init(BufferedImage image) {
		mSurfA = new Surf(image);
		
		mNumberOfReferencePoints = mUpright ? mSurfA.getUprightInterestPoints().size()
				: mSurfA.getFreeOrientedInterestPoints().size();
	}
	
	public double compare(BufferedImage imageB) {
		mSurfB = new Surf(imageB);

		mMatchingPoints = mSurfA.getMatchingPoints(mSurfB, mUpright);	
		mMatchList = getCompareCartesianCoordinates();				
		mNumberOfMatches = mMatchList.size();
		
		setMatchPercent();
		
		return mMatchPercent;
	}
	
	public double compare(BufferedImage image, BufferedImage imageB) {
		mSurfA = new Surf(image);
		mSurfB = new Surf(imageB);

		mNumberOfReferencePoints = mUpright ? mSurfA.getUprightInterestPoints().size()
				: mSurfA.getFreeOrientedInterestPoints().size();
		
		mMatchingPoints = mSurfA.getMatchingPoints(mSurfB, mUpright);		
		mMatchList = getCompareCartesianCoordinates();		
		mNumberOfMatches = mMatchList.size();
		
		setMatchPercent();
		
		return mMatchPercent;
	}
	
	private void setMatchPercent() {
		mMatchPercent = (double) mNumberOfMatches;
				
		if(mNumberOfMatches > 0) {
			mMatchPercent /= mNumberOfReferencePoints;
			mMatchPercent *= 100.0;
		}
	}
	
	private List<Pair<Float, Float>> getCompareCartesianCoordinates() {
		List<Pair<Float, Float>> list = new ArrayList<Pair<Float, Float>>();

		for (SURFInterestPoint point : mUpright ? mSurfA.getUprightInterestPoints()
				: mSurfA.getFreeOrientedInterestPoints()) {
			if (mMatchingPoints.containsKey(point)) {
				list.add(new Pair<Float, Float>(point.getX(), point.getY()));
			}
		}

		return list;
	}
	
}

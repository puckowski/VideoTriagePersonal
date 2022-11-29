package com.keypointforensics.videotriage.chart;

public class HeatMapTile {

	private int xStart;
	private int yStart;
	
	private int xEnd;
	private int yEnd;
	
	private int mWeight;
	
	public HeatMapTile(int xStart, int yStart, int xEnd, int yEnd) {
		this.xStart = xStart;
		this.yStart = yStart;
		
		this.xEnd = xEnd;
		this.yEnd = yEnd;
		
		mWeight = 0;
	}
	
	public boolean isCoordinateWithin(int x, int y) {
		if(x >= xStart && x <= xEnd) {
			if(y >= yStart && y <= yEnd) {
				return true;
			}
		}
		
		return false;
	}
	
	public int getWeight() {
		return mWeight;
	}
	
	public void increaseWeight() {
		mWeight++;
	}
	
	public void setWeight(final int newWeight) {
		mWeight = newWeight;
	}
}

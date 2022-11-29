package com.keypointforensics.videotriage.image.match;

public interface InterestPoint extends Clusterable {
	public double getDistance(InterestPoint point);
}

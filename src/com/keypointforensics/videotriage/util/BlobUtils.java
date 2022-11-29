package com.keypointforensics.videotriage.util;

import java.awt.Rectangle;
import java.util.List;

import com.keypointforensics.videotriage.blob.Blob;

public class BlobUtils {
	
	/*
	 * Author: Daniel Puckowski
	 */
	
	public static List<Rectangle> merge(final List<Rectangle> rectangles) {
		if(rectangles.size() < 2) {
			return rectangles;
		}
		
		Rectangle r1, r2;
			
		outerCheckIntersectLoop:
		for(int i = 0; i < rectangles.size(); ++i) {			
			for(int j = 0; j < rectangles.size(); ++j) {
				if(i == j) {
					
					continue;
				}
				
				r1 = rectangles.get(i);
				r2 = rectangles.get(j);
				
				if(r1.intersects(r2) == true || r1.contains(r2) == true) {						
					rectangles.set(i, r1.union(r2));
					rectangles.remove(j);
					
					if(i >= rectangles.size()) {
						break outerCheckIntersectLoop;
					}
					
					j--;
				}
			}
		}
				
		return rectangles;
	}
	
	public static List<Blob> mergeBlobs(final List<Blob> rectangles) {
		if(rectangles.size() < 2) {
			return rectangles;
		}
		
		Rectangle r1, r2;
				
		outerCheckIntersectLoop:
		for(int i = 0; i < rectangles.size(); ++i) {	
			for(int j = 0; j < rectangles.size(); ++j) {
				if(i == j) {
					
					continue;
				}
								
				r1 = rectangles.get(i);
				r2 = rectangles.get(j);
				
				if(r1.intersects(r2) == true || r1.contains(r2) == true) {	
					Blob mergedBlob = new Blob(r1.union(r2), (Blob) r1, (Blob) r2);
					
					rectangles.set(i, mergedBlob);
					rectangles.remove(j);
					
					if(i >= rectangles.size()) {
						break outerCheckIntersectLoop;
					}
					
					j--;
				}
			}
		}
				
		return rectangles;
	}
}

package com.keypointforensics.videotriage.blob;

import java.util.*;

public class BinaryFrameDifferenceBlobExtractor {

	/*
	 * Author: Daniel Puckowski
	 */
	
	private static final byte DEFAULT_FOREGROUND_VALUE = 127; 
	
	private final int   WIDTH;
	private final int   HEIGHT;
	
	private final boolean EXPAND;
	private final double  EXPANSION_PERCENT;
	
	private final int[] LABEL_BUFFER;
	private final int[] LABEL_TABLE;
	private final int[] X_MIN_TABLE;
	private final int[] X_MAX_TABLE;
	private final int[] Y_MIN_TABLE;
	private final int[] Y_MAX_TABLE;
	private final int[] MASS_TABLE;
	
	public BinaryFrameDifferenceBlobExtractor(final int width, final int height, final boolean expand, final double expansionPercent) {
		WIDTH  = width;
		HEIGHT = height;

		EXPAND = expand;
		EXPANSION_PERCENT = expansionPercent;
		
		LABEL_BUFFER = new int[width * height];

		final int tableSize = width * height / 4;

		LABEL_TABLE = new int[tableSize];
		X_MIN_TABLE = new int[tableSize];
		X_MAX_TABLE = new int[tableSize];
		Y_MIN_TABLE = new int[tableSize];
		Y_MAX_TABLE = new int[tableSize];
		MASS_TABLE  = new int[tableSize];
	}

	public List<Blob> detectBlobs(final byte[] srcData, final byte[] dstData, final int minBlobMass, final int maxBlobMass, final byte matchVal, List<Blob> blobList) {
		if (dstData != null && dstData.length != srcData.length * 3) {
			return null;
		}
		
		int expandWidth = 0;
		int expandHeight = 0;
		
		if(EXPAND == true) {
			expandWidth = (int) (EXPANSION_PERCENT * ((double) WIDTH));
			expandHeight = (int) (EXPANSION_PERCENT * ((double) HEIGHT));
		}

		int srcPtr = 0;
		int aPtr   = -WIDTH - 1;
		int bPtr   = -WIDTH;
		int cPtr   = -WIDTH + 1;
		int dPtr   = -1;

		int label = 1;

		int min;
		
		int aLabel;
		int bLabel;
		int cLabel;
		int dLabel;
		
		for (int y = 0; y < HEIGHT; ++y) {
			for (int x = 0; x < WIDTH; ++x) {
				LABEL_BUFFER[srcPtr] = 0;

				if (srcData[srcPtr] == matchVal) {
					aLabel = (x > 0 && y > 0) ? LABEL_TABLE[LABEL_BUFFER[aPtr]] : 0;
					bLabel = (y > 0) ? LABEL_TABLE[LABEL_BUFFER[bPtr]] : 0;
					cLabel = (x < WIDTH - 1 && y > 0) ? LABEL_TABLE[LABEL_BUFFER[cPtr]] : 0;
					dLabel = (x > 0) ? LABEL_TABLE[LABEL_BUFFER[dPtr]] : 0;

					min = Integer.MAX_VALUE;
					
					if (aLabel != 0 && aLabel < min) {
						min = aLabel;
					}
					if (bLabel != 0 && bLabel < min) {
						min = bLabel;
					}
					if (cLabel != 0 && cLabel < min) {
						min = cLabel;
					}
					if (dLabel != 0 && dLabel < min) {
						min = dLabel;
					}

					if (min == Integer.MAX_VALUE) {
						LABEL_BUFFER[srcPtr] = label;
						LABEL_TABLE[label]   = label;

						Y_MIN_TABLE[label] = y;
						Y_MAX_TABLE[label] = y;
						X_MIN_TABLE[label] = x;
						X_MAX_TABLE[label] = x;
						MASS_TABLE[label]  = 1;

						label++;
					}
					
					else {
						LABEL_BUFFER[srcPtr] = min;

						Y_MAX_TABLE[min] = y;
						MASS_TABLE[min]++;
						
						if (x < X_MIN_TABLE[min]) {
							X_MIN_TABLE[min] = x;
						}
						if (x > X_MAX_TABLE[min]) {
							X_MAX_TABLE[min] = x;
						}

						if (aLabel != 0) {
							LABEL_TABLE[aLabel] = min;
						}
						if (bLabel != 0) {
							LABEL_TABLE[bLabel] = min;
						}
						if (cLabel != 0) {
							LABEL_TABLE[cLabel] = min;
						}
						if (dLabel != 0) {
							LABEL_TABLE[dLabel] = min;
						}
					}
				}

				srcPtr++;
				aPtr++;
				bPtr++;
				cPtr++;
				dPtr++;
			}
		}

		if (blobList == null) {
			blobList = new ArrayList<Blob>();
		}
		
		int l;
		
		for (int i = label - 1; i > 0; --i) {
			if (LABEL_TABLE[i] != i) {
				if (X_MAX_TABLE[i] > X_MAX_TABLE[LABEL_TABLE[i]]) {
					X_MAX_TABLE[LABEL_TABLE[i]] = X_MAX_TABLE[i];
				}
				if (X_MIN_TABLE[i] < X_MIN_TABLE[LABEL_TABLE[i]]) {
					X_MIN_TABLE[LABEL_TABLE[i]] = X_MIN_TABLE[i];
				}
				if (Y_MAX_TABLE[i] > Y_MAX_TABLE[LABEL_TABLE[i]]) {
					Y_MAX_TABLE[LABEL_TABLE[i]] = Y_MAX_TABLE[i];
				}
				if (Y_MIN_TABLE[i] < Y_MIN_TABLE[LABEL_TABLE[i]]) {
					Y_MIN_TABLE[LABEL_TABLE[i]] = Y_MIN_TABLE[i];
				}
				
				MASS_TABLE[LABEL_TABLE[i]] += MASS_TABLE[i];

				l = i;
				
				while (l != LABEL_TABLE[l]) {
					l = LABEL_TABLE[l];
				}
				
				LABEL_TABLE[i] = l;
			} 
			else {				
				if (i == LABEL_BUFFER[0]) {
					
					continue; 
				}
				if (i == LABEL_BUFFER[WIDTH]) {
					
					continue; 
				}
				if (i == LABEL_BUFFER[(WIDTH * HEIGHT) - WIDTH + 1]) {
					
					continue;
				}
				if (i == LABEL_BUFFER[(WIDTH * HEIGHT) - 1]) {
					
					continue; 
				}

				if (MASS_TABLE[i] >= minBlobMass && (MASS_TABLE[i] <= maxBlobMass || maxBlobMass == -1)) {
					if(EXPAND) {
						Blob blob = new Blob(expandWidth, expandHeight, WIDTH, HEIGHT, X_MIN_TABLE[i], X_MAX_TABLE[i], Y_MIN_TABLE[i], Y_MAX_TABLE[i], MASS_TABLE[i]);
						blobList.add(blob);
					}
					else {
						Blob blob = new Blob(X_MIN_TABLE[i], X_MAX_TABLE[i], Y_MIN_TABLE[i], Y_MAX_TABLE[i], MASS_TABLE[i]);
						blobList.add(blob);
					}
				}
			}
		}

		if (dstData != null) {
			for (int i = label - 1; i > 0; --i) {
				if (LABEL_TABLE[i] != i) {
					l = i;
					
					while (l != LABEL_TABLE[l]) {
						l = LABEL_TABLE[l];
					}
					
					LABEL_TABLE[i] = l;
				}
			}

			int newLabel = 0;
			
			for (int i = 1; i < label; ++i) {
				if (LABEL_TABLE[i] == i) {
					LABEL_TABLE[i] = newLabel++;
				}
				else {
					LABEL_TABLE[i] = LABEL_TABLE[LABEL_TABLE[i]];
				}
			}

			srcPtr = 0;
			int dstPtr = 0;
			
			while (srcPtr < srcData.length) {
				if (srcData[srcPtr] == matchVal) {
					dstData[dstPtr]     = DEFAULT_FOREGROUND_VALUE; 
					dstData[dstPtr + 1] = DEFAULT_FOREGROUND_VALUE;
					dstData[dstPtr + 2] = DEFAULT_FOREGROUND_VALUE;
				} else {
					dstData[dstPtr]     = 0;
					dstData[dstPtr + 1] = 0;
					dstData[dstPtr + 2] = 0;
				}

				srcPtr++;
				dstPtr += 3;
			}
		}
		
		return blobList;
	}
}

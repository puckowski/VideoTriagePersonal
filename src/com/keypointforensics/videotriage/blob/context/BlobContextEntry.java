package com.keypointforensics.videotriage.blob.context;

import java.io.File;

public class BlobContextEntry {

	/*
	 * Author: Daniel Puckowski
	 */
		
	public String videoFilename;
	public String extractFrameIndex;
	public String shortExtractFrameIndex;
	public String extractFilename;
	public String shortExtractFilename;
	public int    frameIndex;
	public int    framesPerSecondTarget;
	
	public BlobContextEntry(final String videoFilename, final String extractFrameIndex, final String extractFilename) {
		this.videoFilename = videoFilename;
		this.extractFrameIndex = extractFrameIndex;
		shortExtractFrameIndex = extractFrameIndex;
		
		if(shortExtractFrameIndex.contains(File.separator) == true) {
			shortExtractFrameIndex = shortExtractFrameIndex.substring(0, shortExtractFrameIndex.lastIndexOf(File.separator));
		}
				
		this.extractFilename = extractFilename;
		shortExtractFilename = extractFilename;
		
		if(shortExtractFilename.contains(File.separator) == true) {
			shortExtractFilename = shortExtractFilename.substring(shortExtractFilename.lastIndexOf(File.separator) + 1, shortExtractFilename.length());
		}
		
		String extractFrameIndexShort = extractFrameIndex;
		
		if(extractFrameIndexShort.contains(File.separator) == true) {
			extractFrameIndexShort = extractFrameIndexShort.substring(extractFrameIndexShort.lastIndexOf(File.separator) + 1, extractFrameIndexShort.length());
			frameIndex = Integer.parseInt(extractFrameIndexShort.substring(extractFrameIndexShort.indexOf("-") + 1, extractFrameIndexShort.indexOf(".")));
		}
		else {			
			frameIndex = -1;
		}
		
		framesPerSecondTarget = -1; 
		
		if(extractFrameIndex.contains(File.separator) == true) {
			String framesPerSecondString = extractFrameIndex.substring(0, extractFrameIndex.lastIndexOf(File.separator));
			
			if(framesPerSecondString.contains(File.separator) == true) {
				framesPerSecondString = framesPerSecondString.substring(framesPerSecondString.lastIndexOf(File.separator) + 1, framesPerSecondString.length());
			
				framesPerSecondString = framesPerSecondString.replace("video_frames_", ""); 
				framesPerSecondTarget = Integer.parseInt(framesPerSecondString.substring(0, framesPerSecondString.indexOf("_")));
			}
		}
	}
}

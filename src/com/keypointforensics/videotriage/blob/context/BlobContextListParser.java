package com.keypointforensics.videotriage.blob.context;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.keypointforensics.videotriage.legacy.VideoFalsePositiveBundleLegacy;

public class BlobContextListParser {

	/*
	 * Author: Daniel Puckowski
	 */
	
	private final String CONTEXT_FILE_ABSOLUTE_PATH;
	private HashMap<String, BlobContextEntry> mBlobContextEntries;
	private ArrayList<String> mCaptureFilenames;
	
	public BlobContextListParser(final String contextFileAbsolutePath, final VideoFalsePositiveBundleLegacy falsePositiveBundle) {
		CONTEXT_FILE_ABSOLUTE_PATH = contextFileAbsolutePath;
		mBlobContextEntries = new HashMap<String, BlobContextEntry>();
		mCaptureFilenames = falsePositiveBundle.getCaptureFilenames(); 
	}
	
	public void parseEntries() throws FileNotFoundException, IOException {			
		try(BufferedReader br = new BufferedReader(new FileReader(CONTEXT_FILE_ABSOLUTE_PATH))) {		    
		    String line, line1 = null, line2 = null, line3 = null;
		    int currentIndex = 0;
		    BlobContextEntry blobContextEntry;
		    
			while ((line = br.readLine()) != null) {
				if(line.isEmpty() == true) {
					
					continue;
				}
				else if(line.equals("=====") == true) {
					
					continue;
				}
				else if(line.equals("")) {
					
					continue;
				}
				
				if(currentIndex == 0) {
					line1 = line;
					currentIndex++;
					
					continue;
				}
				else if(currentIndex == 1) {
					line2 = line;
					currentIndex++;
					
					continue;
				}
				else if(currentIndex == 2) {
					line3 = line;
					line3 = line3.toLowerCase();
					currentIndex = 0;
					
					try {
						blobContextEntry = new BlobContextEntry(line1, line2, line3);
					}
					catch(NumberFormatException numberFormatException) {						
						blobContextEntry = null;
					}
					
					if(blobContextEntry != null)
					{
						if(mCaptureFilenames.contains(blobContextEntry.extractFilename)) {
							if(mBlobContextEntries.containsKey(blobContextEntry.extractFilename) == false) {
								mBlobContextEntries.put(blobContextEntry.shortExtractFilename, blobContextEntry);
							}
						}
					}
					
					continue;
				}
		    }
		}
	}
	
	public BlobContextEntry getEntryByFilename(final String shortExtractFilename) {
		return mBlobContextEntries.get(shortExtractFilename);
	}
}

package com.keypointforensics.videotriage.blob.context;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.keypointforensics.videotriage.blob.Blob;
import com.keypointforensics.videotriage.util.FileUtils;

public class BlobContextList {

	/*
	 * Author: Daniel Puckowski
	 */
	
	public static final String FRAME_BOUNDARY_TAG = "FRAME ";
	
	private final boolean CONTEXT_FILE_MODE_APPEND = true;
	
	private final String BLOB_CONTEXT_CASE_NAME;
	private final String BLOB_CONTEXT_CASE_FOLDER_PATH;
	
	private String      mContextFilename;
	private PrintWriter mPrintWriter;
	
	private String mCoordinateFilename;
	private PrintWriter mCoordinateWriter;
	
	private boolean mHasCoordinateWriter;
	
	public BlobContextList(final String absoluteCaseFolderPath, final String caseName) {
		BLOB_CONTEXT_CASE_FOLDER_PATH = absoluteCaseFolderPath;
		BLOB_CONTEXT_CASE_NAME        = caseName;
		
		createContextFilename();
		
		try {
			openContextFile(); 
		} catch (IOException e) {

		} 
		
		mHasCoordinateWriter = false;
	}
	
	public boolean getHasCoordinateWriter() {
		return mHasCoordinateWriter;
	}
	
	public void setHasCoordinateWriter(final boolean newHasCoordinateWriter) {
		mHasCoordinateWriter = newHasCoordinateWriter;
	}
	
	public void initializeCoordinateWriter() throws IOException {
		if(mCoordinateWriter == null) {
			mCoordinateFilename = mContextFilename.substring(0, mContextFilename.lastIndexOf("_"));
			mCoordinateFilename += "_coord.txt";
			
			FileWriter fileWriter = new FileWriter(mCoordinateFilename, CONTEXT_FILE_MODE_APPEND);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter, 4000);
			mCoordinateWriter = new PrintWriter(bufferedWriter);
		}
		
		mHasCoordinateWriter = true;
	}
	
	public String getCoordinateFilename() {
		if(mCoordinateFilename == null && mContextFilename != null) {
			mCoordinateFilename = mContextFilename.substring(0, mContextFilename.lastIndexOf("_"));
			mCoordinateFilename += "_coord.txt";
		}
		
		return mCoordinateFilename;
	}
	
	public String getContextFilename() {
		return mContextFilename;
	}
	
	public void setContextFilename(final String newContextFilename) {
		mContextFilename = newContextFilename;
	}
	
	private void createContextFilename() {
		mContextFilename = FileUtils.CONTEXT_DIRECTORY; 
		
		if(mContextFilename.endsWith(File.separator) == false) {
			mContextFilename += File.separator;
		}
		
		mContextFilename += BLOB_CONTEXT_CASE_NAME + "_blob_context.txt";
	}
	
	private void openContextFile() throws IOException {
		FileWriter fileWriter = new FileWriter(mContextFilename, CONTEXT_FILE_MODE_APPEND);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter, 4000);
		mPrintWriter = new PrintWriter(bufferedWriter);
		
		mPrintWriter.write("=====\n"); 
		mPrintWriter.flush();
	}
	
	public void closeContextFile() {
		if(mPrintWriter != null) {
			mPrintWriter.flush();
			mPrintWriter.close();
			mPrintWriter = null;
		}
		
		if(mCoordinateWriter != null) {
			closeCoordinateFile();
		}
	}
	
	public void closeCoordinateFile() {
		if(mCoordinateWriter != null) {
			mCoordinateWriter.flush();
			mCoordinateWriter.close();
			mCoordinateWriter = null;
		}
	}
	
	public void writeCoordinateData(final ArrayList<Blob> blobsToProcess) {
		int centerX, centerY;
		
		for(Blob currentBlob : blobsToProcess) {
			centerX = (currentBlob.xMin + currentBlob.xMax) / 2;
			centerY = (currentBlob.yMin + currentBlob.yMax) / 2;
			
			mCoordinateWriter.println(centerX + " " + centerY);
			mCoordinateWriter.flush();
		}
	}
	
	public void writeFrameData(final int frameWidth, final int frameHeight) {
		mCoordinateWriter.println(FRAME_BOUNDARY_TAG + frameWidth + " " + frameHeight);
		mCoordinateWriter.flush();
	}
	
	public String getCaseName() {
		return BLOB_CONTEXT_CASE_NAME;
	}
	
	public String getCaseAbsolutePath() {
		return BLOB_CONTEXT_CASE_FOLDER_PATH;
	}
	
	//TODO needs sync?
	synchronized public void writeBlobData(final String videoFilename, final String extractFrameIndex, final String extractFilename) {
		final String newAppendData = videoFilename + "\n" + extractFrameIndex + "\n" + extractFilename + "\n";
		
		mPrintWriter.write(newAppendData);
		mPrintWriter.flush();
	}
}
package com.keypointforensics.videotriage.sqlite;

import java.io.File;
import java.util.Objects;

public class BlobRecord {
	
	/*
	 * Author: Daniel Puckowski
	 */
	
	private final String FILENAME;
	private final String IP;
	private final String PORT;
	
	private String mMmDdYyyyTimeStamp;
	private String mHhMmSsAndHash;
	
	public BlobRecord(final String filename, final String mmDdYyyyTimeStamp, final String hhMmSsAndHash, final String ip, final String port) {
		FILENAME              = filename; 
		IP                    = ip;
		PORT                  = port;
		
		mMmDdYyyyTimeStamp = mmDdYyyyTimeStamp;
		mHhMmSsAndHash     = hhMmSsAndHash;
	}
	
	public String getTimeStampLong() {
		return getMmDdYyyyTimeStamp() + getHhMmSsAndHash();
	}
	
	public String getMmDdYyyyTimeStamp() {
		return mMmDdYyyyTimeStamp;
	}
	
	public String getHhMmSsAndHash() {
		return mHhMmSsAndHash;
	}
	
	public String getFilename() {
		return FILENAME;
	}
	
	public String getFilenameShort() {
		return FILENAME.substring(FILENAME.lastIndexOf(File.separator) + 1, FILENAME.length());
	}
	
	public String getIp() {
		return IP;
	}
	
	public String getPort() {
		return PORT;
	}
	
	public void formatTimeStamp() {
		if(mMmDdYyyyTimeStamp.contains("-") == true) {
			mMmDdYyyyTimeStamp = mMmDdYyyyTimeStamp.replace("-", "\\").replace("-", "\\");
		}
		
		if(mHhMmSsAndHash.contains("-") == true) {
			mHhMmSsAndHash = mHhMmSsAndHash.replace("-", ":").replace("-", ":");
		}
	}
	
	public String getDateString() {
		return mMmDdYyyyTimeStamp + mHhMmSsAndHash;
	}
	
	@Override
	public String toString() {
		return "BlobRecord = {\nFilename: " + FILENAME + "\nTime stamp: " + mMmDdYyyyTimeStamp + mHhMmSsAndHash + "\nIP: " + IP + "\nPort: " + PORT + "\n}";
	}
	
	@Override
	public boolean equals(Object otherObject) {
		if(otherObject instanceof BlobRecord == false) {
			return false;
		}
		
		BlobRecord otherRecord = (BlobRecord) otherObject;
		
		if(this.toString().equals(otherRecord.toString()) == true) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(FILENAME, IP, PORT, mMmDdYyyyTimeStamp, mHhMmSsAndHash);
	}
}
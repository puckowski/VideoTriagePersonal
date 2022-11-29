package com.keypointforensics.videotriage.report;

import java.io.File;

import com.keypointforensics.videotriage.util.StringUtils;

public class ReportCaptureBundle implements Comparable {

	private String CAPTURE_ABSOLUTE_PATH;
	private String CAPTURE_EVENT_DATE;
	
	private int CAPTURE_MONTH;
	private int CAPTURE_DAY;
	private int CAPTURE_YEAR;
	private int CAPTURE_HOUR;
	private int CAPTURE_MINUTE;
	private int CAPTURE_SECOND;
	
	private String mShortExtractFilenameLowerCase;
	
	private double mSecondsIntoVideo;
	private int mSecondsIntoVideoRounded;
	
	public ReportCaptureBundle(String captureAbsolutePath) {
		CAPTURE_ABSOLUTE_PATH = captureAbsolutePath;
		
		String videoCreationDate = captureAbsolutePath.substring(captureAbsolutePath.lastIndexOf(File.separator) + 1, captureAbsolutePath.length());
		CAPTURE_EVENT_DATE = videoCreationDate.substring(0, videoCreationDate.indexOf("_"));
		
		captureAbsolutePath = videoCreationDate;

		parseDateString(captureAbsolutePath);
	}
	
	public void setShortExtractFilenameLowerCase(final String newShortExtractFilenameLowerCase) {
		mShortExtractFilenameLowerCase = newShortExtractFilenameLowerCase;
	}
	
	public String getShortExtractFilenameLowerCase() {
		return mShortExtractFilenameLowerCase;
	}
	
	public void setSecondsIntoVideo(final double secondsIntoVideo) {
		mSecondsIntoVideo = secondsIntoVideo;
		mSecondsIntoVideoRounded = (int) Math.round(secondsIntoVideo);
	}
	
	public double getSecondsIntoVideo() {
		return mSecondsIntoVideo;
	}
	
	public int getSecondsIntoVideoRounded() {
		return mSecondsIntoVideoRounded;
	}
	
	public void parseDateString(String dateString) {
		if(dateString.contains("-") == true && StringUtils.countSubstringOccurrences(dateString, "-") == 4
				&& StringUtils.countSubstringOccurrences(dateString, "_") == 1) {
			int indexOfFirstDash = dateString.indexOf("-");
			CAPTURE_MONTH = Integer.valueOf(dateString.substring(0, indexOfFirstDash));
			dateString = dateString.substring(indexOfFirstDash + 1, dateString.length());
			
			indexOfFirstDash = dateString.indexOf("-");
			CAPTURE_DAY = Integer.valueOf(dateString.substring(0, indexOfFirstDash));
			dateString = dateString.substring(indexOfFirstDash + 1, dateString.length());
			
			indexOfFirstDash = dateString.indexOf(" ");
			CAPTURE_YEAR = Integer.valueOf(dateString.substring(0, indexOfFirstDash));
			dateString = dateString.substring(indexOfFirstDash + 1, dateString.length());
			
			indexOfFirstDash = dateString.indexOf("-");
			CAPTURE_HOUR = Integer.valueOf(dateString.substring(0, indexOfFirstDash));
			dateString = dateString.substring(indexOfFirstDash + 1, dateString.length());
			
			indexOfFirstDash = dateString.indexOf("-");
			CAPTURE_MINUTE = Integer.valueOf(dateString.substring(0, indexOfFirstDash));
			dateString = dateString.substring(indexOfFirstDash + 1, dateString.length());
			
			if(dateString.contains("_") == true) {
				indexOfFirstDash = dateString.indexOf("_");
				CAPTURE_SECOND = Integer.valueOf(dateString.substring(0, indexOfFirstDash));
			}
			else {
				CAPTURE_SECOND = Integer.valueOf(dateString);
			}
		}
	}
	
	public void setCaptureEventDate(final String newCaptureEventDate) {
		CAPTURE_EVENT_DATE = newCaptureEventDate;
	}
	
	public String getCaptureAbsolutePath() {
		return CAPTURE_ABSOLUTE_PATH;
	}
	
	public String getCaptureEventDate() {
		return CAPTURE_EVENT_DATE;
	}
	
	public int getMonth() {
		return CAPTURE_MONTH;
	}
	
	public int getDay() {
		return CAPTURE_DAY;
	}
	
	public int getYear() {
		return CAPTURE_YEAR;
	}
	
	public int getHour() {
		return CAPTURE_HOUR;
	}
	
	public int getMinute() {
		return CAPTURE_MINUTE;
	}
	
	public int getSecond() {
		return CAPTURE_SECOND;
	}
	
	@Override
    public int compareTo(Object other) {
		if(CAPTURE_EVENT_DATE.contains("-") == false) {
			return 1;
		}
		
        ReportCaptureBundle otherCaptureBundle = (ReportCaptureBundle) other;
        
        if(getYear() < otherCaptureBundle.getYear()) {
        	return -1;
        }
        else if(getYear() == otherCaptureBundle.getYear()) {
        	if(getMonth() < otherCaptureBundle.getMonth()) {
            	return -1;
            }
        	else if(getMonth() == otherCaptureBundle.getMonth()) {
        		if(getDay() < otherCaptureBundle.getDay()) {
                	return -1;
                }
        		else if(getDay() == otherCaptureBundle.getDay()) {
        			if(getHour() < otherCaptureBundle.getHour()) {
                    	return -1;
                    }
        			else if(getHour() == otherCaptureBundle.getHour()) {
        				if(getMinute() < otherCaptureBundle.getMinute()) {
                        	return -1;
                        }
        				else if(getMinute() == otherCaptureBundle.getMinute()) {
        					if(getSecond() < otherCaptureBundle.getSecond()) {
                            	return -1;
                            }
        					else if(getSecond() == otherCaptureBundle.getSecond()) {
                            	return 0;
                            }
        					else {
        						return 1;
        					}
                        }
        				else {
        					return 1;
        				}
                    }
        			else {
        				return 1;
        			}
                }
        		else {
        			return 1;
        		}
            }
        	else {
        		return 1;
        	}
        }
        else {
        	return 1;
        }
    }
}

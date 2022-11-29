package com.keypointforensics.videotriage.report.stats;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import com.keypointforensics.videotriage.detect.EVideoTriageDetectionModule;
import com.keypointforensics.videotriage.legacy.WindowsVideoFrameExtractorLegacy;
import com.keypointforensics.videotriage.util.DateUtils;
import com.keypointforensics.videotriage.util.Utils;

public class StatisticsPageGenerator {

	private final String STATISTICS_PAGE_FILENAME = "statistics.html";
	
	private String mReportPageHeader;
	private String mReportRoot;
	
	private String mStatisticsFolderPath;
	private String mPageName;
	
	private ArrayList<Date> mEventTimeline;	
	private HashSet<Date> mUniqueDaySet;
	private long mMassSum;
	private int mVideoCount;
	private int mNumberOfFrames;
	private int mCaptureCount;
	private double mAverageMass;
	private long mSecondsElapsed;
	
	private HashMap<EVideoTriageDetectionModule, Integer> mDetectionMap;
	
	/*
	private int mCarDetectionCount;
	private int mPedestrianDetectionCount;
	private int mFaceDetectionCount;
	private int mLicensePlateDetectionCount;
	
	private boolean mHasCarDetections;
	private boolean mHasPedestrianDetections;
	private boolean mHasFaceDetections;
	private boolean mHasLicensePlateDetections;
	*/
	
	private StringBuilder mPageBuilder;
	
	public StatisticsPageGenerator() {	
		mEventTimeline = new ArrayList<Date>();
		mUniqueDaySet = new HashSet<Date>();
		mMassSum = 0;
		mVideoCount = 0;
		mNumberOfFrames = 0;
		mCaptureCount = 0;
		mSecondsElapsed = 0;
		
		mDetectionMap = new HashMap<EVideoTriageDetectionModule, Integer>();
		
		mPageBuilder = new StringBuilder(2000);
	}
	
	public void setCarDetectionCount(final int newCarDetectionCount) {
		//mCarDetectionCount = newCarDetectionCount;
		//mHasCarDetections = true;
		
		mDetectionMap.put(EVideoTriageDetectionModule.DETECTION_MODULE_CAR, newCarDetectionCount);
	}
	
	public void setPedestrianDetectionCount(final int newPedestrianDetectionCount) {
		//mPedestrianDetectionCount = newPedestrianDetectionCount;
		//mHasPedestrianDetections = true;
		
		mDetectionMap.put(EVideoTriageDetectionModule.DETECTION_MODULE_PEDESTRIAN, newPedestrianDetectionCount);
	}
	
	public void setFaceDetectionCount(final int newFaceDetectionCount) {
		//mFaceDetectionCount = newFaceDetectionCount;
		//mHasFaceDetections = true;
		
		mDetectionMap.put(EVideoTriageDetectionModule.DETECTION_MODULE_FACE, newFaceDetectionCount);
	}
	
	public void setLicensePlateDetectionCount(final int newLicensePlateDetectionCount) {
		//mLicensePlateDetectionCount = newLicensePlateDetectionCount;
		//mHasLicensePlateDetections = true;
		
		mDetectionMap.put(EVideoTriageDetectionModule.DETECTION_MODULE_LICENSE_PLATE, newLicensePlateDetectionCount);
	}
	
	public void setExplicitDetectionCount(final int newExplicitDetectionCount) {		
		mDetectionMap.put(EVideoTriageDetectionModule.DETECTION_MODULE_EXPLICIT, newExplicitDetectionCount);
	}
	
	public void setReportPageHeader(final String reportPageHeader) {
		mReportPageHeader = reportPageHeader;
	}
	
	public void setReportRoot(final String reportRoot) {
		mReportRoot = reportRoot;
	}
	
	public void incrementVideoCount() {
		mVideoCount++;
	}
	
	public void addFrameCount(final int additionalFrameCount) {
		mNumberOfFrames += additionalFrameCount;
	}
	
	public void addEvent(final String eventTime, final long mass) {		
		mCaptureCount++;
		
		final int indexOfSpace = eventTime.indexOf(" ");
		String date = eventTime.substring(0, indexOfSpace + 1);
		String time = eventTime.substring(indexOfSpace + 1).replace("-", ":");
		
		final String correctedEventTime = date + time;
		
		try {
			Date newDate = new SimpleDateFormat(Utils.REPORT_DATE_FORMAT_DASH_LONG).parse(correctedEventTime);
			mEventTimeline.add(newDate);
			
			newDate = new SimpleDateFormat(Utils.REPORT_DATE_FORMAT_DASH_SHORT).parse(date.trim());
			mUniqueDaySet.add(newDate);
		} catch (ParseException parseException) {
			//parseException.printStackTrace();
		} 
		
		mMassSum += mass;
	}

	public void addVideoForSecondsElapsed(final String absoluteVideoFilename) {
		mSecondsElapsed += (long) Math.floor(WindowsVideoFrameExtractorLegacy.getVideoDurationInSeconds(absoluteVideoFilename));
	}
	
	private void sortDates() {
		Collections.sort(mEventTimeline, new Comparator<Date>() {		 
            @Override
            public int compare(Date o1, Date o2) {
                return o1.compareTo(o2);
            }
        });
	}
	
	private void calculateAverageMass() {
		if(mEventTimeline.isEmpty() == false) {
			mAverageMass = ((double) mMassSum / (double) mCaptureCount);
		}
	}
	
	public void buildPage() {
		sortDates();
		calculateAverageMass();
		
		NumberFormat numberFormat = NumberFormat.getInstance();
		numberFormat.setMaximumFractionDigits(2); 
		
		mPageBuilder.append(mReportPageHeader);
		
		mPageBuilder.append("<table class=\"table-fill\"><thead><tr><th class=\"text-left\">Statistic</th><th>Value</th></tr></thead><tbody class=\"table-hover\">");

		mPageBuilder.append("<tr class=\"outer\"><td class=\"text-left\">");
		mPageBuilder.append("Videos Processed");
		mPageBuilder.append("</td><td class=\\\"text-left\\\">");
		mPageBuilder.append(mVideoCount);
		mPageBuilder.append("</td></tr>");
		
		mPageBuilder.append("<tr class=\"outer\"><td class=\"text-left\">");
		mPageBuilder.append("Frames Processed");
		mPageBuilder.append("</td><td class=\\\"text-left\\\">");
		mPageBuilder.append(mNumberOfFrames);
		mPageBuilder.append("</td></tr>");
		
		mPageBuilder.append("<tr class=\"outer\"><td class=\"text-left\">");
		mPageBuilder.append("Captures");
		mPageBuilder.append("</td><td class=\\\"text-left\\\">");
		mPageBuilder.append(mCaptureCount);
		mPageBuilder.append("</td></tr>");
		
		mPageBuilder.append("<tr class=\"outer\"><td class=\"text-left\">");
		mPageBuilder.append("Average Captures Per Frame");
		mPageBuilder.append("</td><td class=\\\"text-left\\\">");
		if(mCaptureCount > 0) {
			mPageBuilder.append(numberFormat.format((double) mCaptureCount / (double) mNumberOfFrames));
		} else {
			mPageBuilder.append("0.00");
		}
		mPageBuilder.append("</td></tr>");
		
		if(mEventTimeline.isEmpty() == false) {
			final Date startDate = mEventTimeline.get(0);
			final Date stopDate = mEventTimeline.get(mEventTimeline.size() - 1);
			
			mPageBuilder.append("<tr class=\"outer\"><td class=\"text-left\">");
			mPageBuilder.append("Start Date");
			mPageBuilder.append("</td><td class=\\\"text-left\\\">");
			mPageBuilder.append(startDate);
			mPageBuilder.append("</td></tr>");
			
			mPageBuilder.append("<tr class=\"outer\"><td class=\"text-left\">");
			mPageBuilder.append("End Date");
			mPageBuilder.append("</td><td class=\\\"text-left\\\">");
			mPageBuilder.append(stopDate);
			mPageBuilder.append("</td></tr>");
			
			mPageBuilder.append("<tr class=\"outer\"><td class=\"text-left\">");
			mPageBuilder.append("Time Elapsed");
			mPageBuilder.append("</td><td class=\\\"text-left\\\">");
			mPageBuilder.append(DateUtils.getDaysElapsedBetweenDates(startDate, stopDate));
			mPageBuilder.append("</td></tr>");
			
			mPageBuilder.append("<tr class=\"outer\"><td class=\"text-left\">");
			mPageBuilder.append("Total Video Duration");
			mPageBuilder.append("</td><td class=\\\"text-left\\\">");
			mPageBuilder.append(DateUtils.calculateTimeFromSeconds(mSecondsElapsed));
			mPageBuilder.append("</td></tr>");
			
			mPageBuilder.append("<tr class=\"outer\"><td class=\"text-left\">");
			mPageBuilder.append("Days With Events");
			mPageBuilder.append("</td><td class=\\\"text-left\\\">");
			mPageBuilder.append(mUniqueDaySet.size());
			mPageBuilder.append("</td></tr>");
		}
		
		if(mDetectionMap.containsKey(EVideoTriageDetectionModule.DETECTION_MODULE_FACE) == true) {
			mPageBuilder.append("<tr class=\"outer\"><td class=\"text-left\">");
			mPageBuilder.append("Face Detections");
			mPageBuilder.append("</td><td class=\\\"text-left\\\">");
			mPageBuilder.append(mDetectionMap.get(EVideoTriageDetectionModule.DETECTION_MODULE_FACE));
			mPageBuilder.append("</td></tr>");
		}

		if(mDetectionMap.containsKey(EVideoTriageDetectionModule.DETECTION_MODULE_PEDESTRIAN) == true) {
			mPageBuilder.append("<tr class=\"outer\"><td class=\"text-left\">");
			mPageBuilder.append("Pedestrian Detections");
			mPageBuilder.append("</td><td class=\\\"text-left\\\">");
			mPageBuilder.append(mDetectionMap.get(EVideoTriageDetectionModule.DETECTION_MODULE_PEDESTRIAN));
			mPageBuilder.append("</td></tr>");
		}

		if(mDetectionMap.containsKey(EVideoTriageDetectionModule.DETECTION_MODULE_CAR) == true) {
			mPageBuilder.append("<tr class=\"outer\"><td class=\"text-left\">");
			mPageBuilder.append("Car Detections");
			mPageBuilder.append("</td><td class=\\\"text-left\\\">");
			mPageBuilder.append(mDetectionMap.get(EVideoTriageDetectionModule.DETECTION_MODULE_CAR));
			mPageBuilder.append("</td></tr>");
		}
		
		if(mDetectionMap.containsKey(EVideoTriageDetectionModule.DETECTION_MODULE_LICENSE_PLATE) == true) {
			mPageBuilder.append("<tr class=\"outer\"><td class=\"text-left\">");
			mPageBuilder.append("License Plate Detections");
			mPageBuilder.append("</td><td class=\\\"text-left\\\">");
			mPageBuilder.append(mDetectionMap.get(EVideoTriageDetectionModule.DETECTION_MODULE_LICENSE_PLATE));
			mPageBuilder.append("</td></tr>");
		}

		if(mDetectionMap.containsKey(EVideoTriageDetectionModule.DETECTION_MODULE_EXPLICIT) == true) {
			mPageBuilder.append("<tr class=\"outer\"><td class=\"text-left\">");
			mPageBuilder.append("Explicit Image Detections");
			mPageBuilder.append("</td><td class=\\\"text-left\\\">");
			mPageBuilder.append(mDetectionMap.get(EVideoTriageDetectionModule.DETECTION_MODULE_EXPLICIT));
			mPageBuilder.append("</td></tr>");
		}
		
		mPageBuilder.append("<tr class=\"outer\"><td class=\"text-left\">");
		mPageBuilder.append("Total Mass");
		mPageBuilder.append("</td><td class=\\\"text-left\\\">");
		mPageBuilder.append(numberFormat.format(mMassSum));
		mPageBuilder.append("</td></tr>");
		
		mPageBuilder.append("<tr class=\"outer\"><td class=\"text-left\">");
		mPageBuilder.append("Average Mass");
		mPageBuilder.append("</td><td class=\\\"text-left\\\">");
		mPageBuilder.append(numberFormat.format(mAverageMass));
		mPageBuilder.append("</td></tr>");
		
		mPageBuilder.append("</tbody></table><br><div align=\"center\"><a href=\"");
		mPageBuilder.append(mReportRoot);
		mPageBuilder.append("\">Home</a></div></body><html>");
		
		PrintWriter pageWriter = null;
		
		File statisticsPageFile = new File(mPageName);
		
		try {
			pageWriter = new PrintWriter(statisticsPageFile);
			
			pageWriter.append(mPageBuilder.toString());
			
			pageWriter.flush();
			pageWriter.close();
		} catch (FileNotFoundException fileNotFoundException) {
			//fileNotFoundException.printStackTrace();
		}
	}
	
	public void buildEmbeddablePage() {
		sortDates();
		calculateAverageMass();
		
		NumberFormat numberFormat = NumberFormat.getInstance();
		numberFormat.setMaximumFractionDigits(2); 
				
		mPageBuilder.append("<table class=\"table-fill\"><thead><tr><th class=\"text-left\">Statistic</th><th>Value</th></tr></thead><tbody class=\"table-hover\">");

		mPageBuilder.append("<tr class=\"outer\"><td class=\"text-left\">");
		mPageBuilder.append("Videos Processed");
		mPageBuilder.append("</td><td class=\\\"text-left\\\">");
		mPageBuilder.append(mVideoCount);
		mPageBuilder.append("</td></tr>");
		
		mPageBuilder.append("<tr class=\"outer\"><td class=\"text-left\">");
		mPageBuilder.append("Frames Processed");
		mPageBuilder.append("</td><td class=\\\"text-left\\\">");
		mPageBuilder.append(mNumberOfFrames);
		mPageBuilder.append("</td></tr>");
		
		mPageBuilder.append("<tr class=\"outer\"><td class=\"text-left\">");
		mPageBuilder.append("Captures");
		mPageBuilder.append("</td><td class=\\\"text-left\\\">");
		mPageBuilder.append(mCaptureCount);
		mPageBuilder.append("</td></tr>");
		
		mPageBuilder.append("<tr class=\"outer\"><td class=\"text-left\">");
		mPageBuilder.append("Average Captures Per Frame");
		mPageBuilder.append("</td><td class=\\\"text-left\\\">");
		mPageBuilder.append(numberFormat.format((double) mCaptureCount / (double) mNumberOfFrames));
		mPageBuilder.append("</td></tr>");
		
		if(mEventTimeline.isEmpty() == false) {
			mPageBuilder.append("<tr class=\"outer\"><td class=\"text-left\">");
			mPageBuilder.append("Start Date");
			mPageBuilder.append("</td><td class=\\\"text-left\\\">");
			mPageBuilder.append(mEventTimeline.get(0));
			mPageBuilder.append("</td></tr>");
			
			mPageBuilder.append("<tr class=\"outer\"><td class=\"text-left\">");
			mPageBuilder.append("End Date");
			mPageBuilder.append("</td><td class=\\\"text-left\\\">");
			mPageBuilder.append(mEventTimeline.get(mEventTimeline.size() - 1));
			mPageBuilder.append("</td></tr>");
			
			mPageBuilder.append("<tr class=\"outer\"><td class=\"text-left\">");
			mPageBuilder.append("Days With Events");
			mPageBuilder.append("</td><td class=\\\"text-left\\\">");
			mPageBuilder.append(mUniqueDaySet.size());
			mPageBuilder.append("</td></tr>");
		}
		
		if(mDetectionMap.containsKey(EVideoTriageDetectionModule.DETECTION_MODULE_FACE) == true) {
			mPageBuilder.append("<tr class=\"outer\"><td class=\"text-left\">");
			mPageBuilder.append("Face Detections");
			mPageBuilder.append("</td><td class=\\\"text-left\\\">");
			mPageBuilder.append(mDetectionMap.get(EVideoTriageDetectionModule.DETECTION_MODULE_FACE));
			mPageBuilder.append("</td></tr>");
		}

		if(mDetectionMap.containsKey(EVideoTriageDetectionModule.DETECTION_MODULE_PEDESTRIAN) == true) {
			mPageBuilder.append("<tr class=\"outer\"><td class=\"text-left\">");
			mPageBuilder.append("Pedestrian Detections");
			mPageBuilder.append("</td><td class=\\\"text-left\\\">");
			mPageBuilder.append(mDetectionMap.get(EVideoTriageDetectionModule.DETECTION_MODULE_PEDESTRIAN));
			mPageBuilder.append("</td></tr>");
		}

		if(mDetectionMap.containsKey(EVideoTriageDetectionModule.DETECTION_MODULE_CAR) == true) {
			mPageBuilder.append("<tr class=\"outer\"><td class=\"text-left\">");
			mPageBuilder.append("Car Detections");
			mPageBuilder.append("</td><td class=\\\"text-left\\\">");
			mPageBuilder.append(mDetectionMap.get(EVideoTriageDetectionModule.DETECTION_MODULE_CAR));
			mPageBuilder.append("</td></tr>");
		}
		
		if(mDetectionMap.containsKey(EVideoTriageDetectionModule.DETECTION_MODULE_LICENSE_PLATE) == true) {
			mPageBuilder.append("<tr class=\"outer\"><td class=\"text-left\">");
			mPageBuilder.append("License Plate Detections");
			mPageBuilder.append("</td><td class=\\\"text-left\\\">");
			mPageBuilder.append(mDetectionMap.get(EVideoTriageDetectionModule.DETECTION_MODULE_LICENSE_PLATE));
			mPageBuilder.append("</td></tr>");
		}
		
		if(mDetectionMap.containsKey(EVideoTriageDetectionModule.DETECTION_MODULE_EXPLICIT) == true) {
			mPageBuilder.append("<tr class=\"outer\"><td class=\"text-left\">");
			mPageBuilder.append("Explicit Image Detections");
			mPageBuilder.append("</td><td class=\\\"text-left\\\">");
			mPageBuilder.append(mDetectionMap.get(EVideoTriageDetectionModule.DETECTION_MODULE_EXPLICIT));
			mPageBuilder.append("</td></tr>");
		}

		mPageBuilder.append("<tr class=\"outer\"><td class=\"text-left\">");
		mPageBuilder.append("Total Mass");
		mPageBuilder.append("</td><td class=\\\"text-left\\\">");
		mPageBuilder.append(numberFormat.format(mMassSum));
		mPageBuilder.append("</td></tr>");
		
		mPageBuilder.append("<tr class=\"outer\"><td class=\"text-left\">");
		mPageBuilder.append("Average Mass");
		mPageBuilder.append("</td><td class=\\\"text-left\\\">");
		mPageBuilder.append(numberFormat.format(mAverageMass));
		mPageBuilder.append("</td></tr>");
		
		mPageBuilder.append("</tbody></table><br>");
	}
	
	public String getEmbeddablePage() {
		return mPageBuilder.toString();
	}
	
	private void setStatisticsPageName() {
		mPageName = mStatisticsFolderPath + STATISTICS_PAGE_FILENAME;
	}
	
	public void setStatisticsFolderPath(final String statisticsFolderPath) {
		mStatisticsFolderPath = statisticsFolderPath;
		
		setStatisticsPageName();
	}
	
	public String getStatisticsPageName() {
		return mPageName;
	}
}

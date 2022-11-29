package com.keypointforensics.videotriage.chart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.keypointforensics.videotriage.detect.EVideoTriageDetectionModule;
import com.keypointforensics.videotriage.util.MapUtils;

public class DetectionCompositionPieChartBuilder extends VideoTriageChartBuilder {
	
	private final String JAVASCRIPT_FILENAME_SHORT = "detection_pie.js";
	private final String UNIQUE_DIV_NAME           = "DetectionPie";
	
	private HashMap<EVideoTriageDetectionModule, Integer> mDetectionMap;
	
	private StringBuilder mChartBuilder;
	private StringBuilder mHtmlBuilder;
	
	public DetectionCompositionPieChartBuilder(final String reportRoot, final String reportPageHeader) {
		super(reportRoot, reportPageHeader);
		
		mDetectionMap  = new HashMap<EVideoTriageDetectionModule, Integer>();
		mChartBuilder  = new StringBuilder(1000);
		mHtmlBuilder   = new StringBuilder(2000);
		
		prepareNewTimeSeriesChart();
	}
	
	public void setJavaScriptFilenameShort() {
		mJavaScriptFilenameShort = JAVASCRIPT_FILENAME_SHORT;
	}
	
	private void prepareNewTimeSeriesChart() {
		mChartBuilder.append("var layout={margin:{r:180,t:40,b:140,l:100},title:'Detection Composition'};var data=[{type:'pie',values:[");
	}
	
	public void addEvents(final EVideoTriageDetectionModule videoTriageDetectionModule, final ArrayList<String> eventTimes) {
		mDetectionMap.put(videoTriageDetectionModule, eventTimes.size());
	}
	
	private void closeDetectionCompositionChart(boolean isReportPaginated) {
		if(isReportPaginated == true) {
			mChartBuilder.append("}]; Plotly.newPlot('myDiv', data, layout);");
		} else {
			mChartBuilder.append("}]; Plotly.newPlot('myDiv" + UNIQUE_DIV_NAME + "', data, layout);");
		}
	}
	
	private void buildHtmlFileContents(boolean isReportPaginated) {
		if(isReportPaginated == true) {
			final int indexOfHtmlHead = REPORT_PAGE_HEADER.indexOf("<head>") + 6;
			String startOfHeader = REPORT_PAGE_HEADER.substring(0, indexOfHtmlHead);
			String restOfHeader = REPORT_PAGE_HEADER.substring(indexOfHtmlHead, REPORT_PAGE_HEADER.length());
				
			mHtmlBuilder.append(startOfHeader);
			mHtmlBuilder.append("<script src=\"chart.js\"></script>");
			mHtmlBuilder.append(restOfHeader);
			mHtmlBuilder.append("<div id=\"myDiv\"></div><script src=\"");
			mHtmlBuilder.append(JAVASCRIPT_FILENAME_SHORT);
			mHtmlBuilder.append("\"></script>");//</body><html>");
				
			mHtmlBuilder.append("<br><div align=\"center\"><a href=\"");
			mHtmlBuilder.append(REPORT_ROOT);
			mHtmlBuilder.append("\">Home</a></div></body></html>");
		} else {
			mHtmlBuilder.append("<div id=\"myDiv" + UNIQUE_DIV_NAME + "\" style=\"height: 75%;\"></div><script src=\"");
			mHtmlBuilder.append(JAVASCRIPT_FILENAME_SHORT);
			mHtmlBuilder.append("\"></script>");//</body><html>");
			
			mHtmlBuilder.append("<br>");
		}
	}
	
	private void appendDetectionValues() {
		mDetectionMap = (HashMap<EVideoTriageDetectionModule, Integer>) MapUtils.sortByKey(mDetectionMap);
		
		boolean firstEntry = true;
		
		for(Entry<EVideoTriageDetectionModule, Integer> detectionEntry : mDetectionMap.entrySet()) {
			if(firstEntry == true) {
				firstEntry = false;
				
				mChartBuilder.append(detectionEntry.getValue());
			} else {
				mChartBuilder.append(",");
				mChartBuilder.append(detectionEntry.getValue());
			}
		}
	}
	
	private void closeDetectionValues() {
		mChartBuilder.append("],");
	}
	
	private void appendDetectionLabels() {
		mChartBuilder.append("labels:[");
		
		mDetectionMap = (HashMap<EVideoTriageDetectionModule, Integer>) MapUtils.sortByKey(mDetectionMap);
		
		boolean firstEntry = true;
		
		for(Entry<EVideoTriageDetectionModule, Integer> detectionEntry : mDetectionMap.entrySet()) {
			if(firstEntry == true) {
				firstEntry = false;
				
				mChartBuilder.append("'");
				mChartBuilder.append(detectionEntry.getKey().getChartDisplayName());
				mChartBuilder.append("'");
			} else {
				mChartBuilder.append(",");
				mChartBuilder.append("'");
				mChartBuilder.append(detectionEntry.getKey().getChartDisplayName());
				mChartBuilder.append("'");
			}
		}
	}
	
	private void closeDetectionLabels() {
		mChartBuilder.append("],");
	}
	
	private void appendFormattingData() {
		mChartBuilder.append("hole:0.25,direction:'clockwise',marker:{line:{color:'black',width:2}},textfont:{color:'white',");
		mChartBuilder.append("size:18},hoverlabel:{bgcolor:'black',bordercolor:'black',font:{color:'white',size:18}}");
	}
	
	public void build(boolean isReportPaginated) {	
		appendDetectionValues();
		closeDetectionValues();
		
		appendDetectionLabels();
		closeDetectionLabels();
		
		appendFormattingData();
		
		closeDetectionCompositionChart(isReportPaginated);
		buildHtmlFileContents(isReportPaginated);
	}
	
	public String getChart() {
		return mChartBuilder.toString();
	}

	@Override
	public String getHtmlFile() {		
		return mHtmlBuilder.toString();
	}
}

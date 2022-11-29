package com.keypointforensics.videotriage.chart;

import java.util.ArrayList;
import java.util.HashMap;

public class TimeSeriesPedestrianDetectionChartBuilder extends VideoTriageChartBuilder {

	private final String JAVASCRIPT_FILENAME_SHORT = "time_pedestrian.js";
	private final String UNIQUE_DIV_NAME           = "TimePedestrian";
	
	private ArrayList<String> mEventTimeline;
	private HashMap<String, Integer> mEventMap;

	private StringBuilder mChartBuilder;
	private StringBuilder mHtmlBuilder;

	private boolean mFirstTimeEvent;
	private boolean mFirstTimeValue;

	public TimeSeriesPedestrianDetectionChartBuilder(final String reportRoot, final String reportPageHeader) {
			super(reportRoot, reportPageHeader);
			
			mEventTimeline = new ArrayList<String>();
			mEventMap      = new HashMap<String, Integer>();
			mChartBuilder  = new StringBuilder(1000);
			mHtmlBuilder   = new StringBuilder(2000);
			
			mFirstTimeEvent = true;
			mFirstTimeValue = true;
			
			prepareNewTimeSeriesChart();
		}

	public void setJavaScriptFilenameShort() {
		mJavaScriptFilenameShort = JAVASCRIPT_FILENAME_SHORT;
	}

	private void prepareNewTimeSeriesChart() {
		mChartBuilder.append(
				"var layout={margin:{r:180,t:40,b:140,l:100},title:'Time Series Pedestrian Detection',xaxis:{title:'Date',tickangle:45},yaxis:{title:'Number of Pedestrians'}};var data=[{type:'scatter',x:[");
	}

	public void addEvents(final ArrayList<String> eventTimes) {
		for(String eventTime : eventTimes) {
			addEvent(eventTime);
		}
	}
	
	public void addEvent(final String eventTime) {
		final int indexOfSpace = eventTime.indexOf(" ");
		String date = eventTime.substring(0, indexOfSpace + 1);
		String time = eventTime.substring(indexOfSpace + 1).replace("-", ":");

		final String correctedEventTime = date + time;

		mEventTimeline.add(correctedEventTime);

		if (mEventMap.containsKey(correctedEventTime) == false) {
			mEventMap.put(correctedEventTime, 1);
		} else {
			mEventMap.replace(correctedEventTime, mEventMap.get(correctedEventTime) + 1);
		}
	}

	private void appendTimeEvent(final String event) {
		if (mFirstTimeEvent == false) {
			mChartBuilder.append(",'");
		} else {
			mChartBuilder.append("'");
			mFirstTimeEvent = false;
		}

		mChartBuilder.append(event);
		mChartBuilder.append("'");
	}

	private void closeTimeEvents() {
		mChartBuilder.append("],y: [");
	}

	private void appendTimeValue(final int value) {
		if (mFirstTimeValue == false) {
			mChartBuilder.append(",");
		} else {
			mFirstTimeValue = false;
		}

		mChartBuilder.append(value);
	}

	private void closeTimeValues() {
		mChartBuilder.append("]");
	}

	private void closeTimeSeriesChart(boolean isReportPaginated) {
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
			mHtmlBuilder.append("\"></script>");// </body><html>");
	
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

	public void build(boolean isReportPaginated) {	
		for (int i = 0; i < mEventTimeline.size(); ++i) {
			appendTimeEvent(mEventTimeline.get(i));
		}

		closeTimeEvents();

		for (int i = 0; i < mEventTimeline.size(); ++i) {
			appendTimeValue(mEventMap.get(mEventTimeline.get(i)));
		}

		closeTimeValues();

		closeTimeSeriesChart(isReportPaginated);
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

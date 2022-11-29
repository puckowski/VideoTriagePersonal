package com.keypointforensics.videotriage.chart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class ReportChartSettings {

	private HashSet<EVideoTriageChart> mChartSet;
	
	private HashMap<EVideoTriageChart, VideoTriageChartBuilder> mChartBuilders;
	private String mReportPageHeader;
	private String mReportRoot;
	private ArrayList<String> mProcessedVideoList;
	
	public ReportChartSettings(final ArrayList<String> listOfProcessedVideos) {
		mChartSet = new HashSet<EVideoTriageChart>();
		mChartBuilders = new HashMap<EVideoTriageChart, VideoTriageChartBuilder>();
		mProcessedVideoList = listOfProcessedVideos;
	}
	
	private ArrayList<String> getCopyOfProcessedVideoList() {
		ArrayList<String> listOfProcessedVideos = new ArrayList<String>(mProcessedVideoList);
		
		return listOfProcessedVideos;
	}
	
	public void addChartRequest(EVideoTriageChart videoTriageChart) {
		mChartSet.add(videoTriageChart);
	}
	
	public void removeChartRequest(EVideoTriageChart videoTriageChart) {
		mChartSet.remove(videoTriageChart);
	}
	
	public boolean hasRequestedChartBuilder(EVideoTriageChart chartType) {
		return mChartSet.contains(chartType);
	}
	
	public boolean hasRequestedCharts() {
		return (mChartSet.isEmpty() == false);
	}
	
	public void initializeChartBuilders(final String reportRoot, final String reportPageHeader) {
		mReportRoot = reportRoot;
		mReportPageHeader = reportPageHeader;
		
		for(EVideoTriageChart chartType : mChartSet) {
			switch(chartType) {
				case TIME_SERIES_STANDARD: {
					mChartBuilders.put(EVideoTriageChart.TIME_SERIES_STANDARD, new TimeSeriesChartBuilder(mReportRoot, mReportPageHeader));
					
					break;
				}
				case TIME_SERIES_SCATTER: {
					mChartBuilders.put(EVideoTriageChart.TIME_SERIES_SCATTER, new TimeSeriesScatterChartBuilder(mReportRoot, mReportPageHeader));
					
					break;
				}
				case TIME_SERIES_FILLED_AREA: {
					mChartBuilders.put(EVideoTriageChart.TIME_SERIES_FILLED_AREA, new TimeSeriesFilledAreaChartBuilder(mReportRoot, mReportPageHeader));
					
					break;
				}
				case TIME_SERIES_MASS: {
					mChartBuilders.put(EVideoTriageChart.TIME_SERIES_MASS, new TimeSeriesMassChartBuilder(mReportRoot, mReportPageHeader));
					
					break;
				}
				case TIME_SERIES_FACES: {
					mChartBuilders.put(EVideoTriageChart.TIME_SERIES_FACES, new TimeSeriesFaceDetectionChartBuilder(mReportRoot, mReportPageHeader));
					
					break;
				}
				case TIME_SERIES_LICENSE_PLATES: {
					mChartBuilders.put(EVideoTriageChart.TIME_SERIES_LICENSE_PLATES, new TimeSeriesLicensePlateDetectionChartBuilder(mReportRoot, mReportPageHeader));
					
					break;
				}
				case TIME_SERIES_PEDESTRIANS: {
					mChartBuilders.put(EVideoTriageChart.TIME_SERIES_PEDESTRIANS, new TimeSeriesPedestrianDetectionChartBuilder(mReportRoot, mReportPageHeader));
					
					break;
				}
				case TIME_SERIES_CARS: {
					mChartBuilders.put(EVideoTriageChart.TIME_SERIES_CARS, new TimeSeriesCarDetectionChartBuilder(mReportRoot, mReportPageHeader));
					
					break;
				}
				case BLOB_TRACKING_SCATTER_STANDARD: {
					mChartBuilders.put(EVideoTriageChart.BLOB_TRACKING_SCATTER_STANDARD, new BlobTrackingScatterChartBuilder(mReportRoot, mReportPageHeader));
					
					break;
				}
				case BLOB_TRACKING_SCATTER_BACKGROUND: {
					mChartBuilders.put(EVideoTriageChart.BLOB_TRACKING_SCATTER_BACKGROUND, new BlobTrackingScatterBackgroundChartBuilder(mReportRoot, mReportPageHeader, getCopyOfProcessedVideoList()));
					
					break;
				}
				case BLOB_TRACKING_HEAT_MAP_STANDARD: {
					mChartBuilders.put(EVideoTriageChart.BLOB_TRACKING_HEAT_MAP_STANDARD, new BlobTrackingHeatMapChartBuilder(mReportRoot, mReportPageHeader));
					
					break;
				}
				case BAR_CHART_HOUR_OF_DAY_STANDARD: {
					mChartBuilders.put(EVideoTriageChart.BAR_CHART_HOUR_OF_DAY_STANDARD, new HourOfDayBarChartBuilder(mReportRoot, mReportPageHeader));
					
					break;
				}
				case HISTOGRAM_CONTOUR_BACKGROUND: {
					mChartBuilders.put(EVideoTriageChart.HISTOGRAM_CONTOUR_BACKGROUND, new HistogramContourBackgroundChartBuilder(mReportRoot, mReportPageHeader, getCopyOfProcessedVideoList()));
					
					break;
				}
				case DETECTION_COMPOSITION_PIE_STANDARD: {
					mChartBuilders.put(EVideoTriageChart.DETECTION_COMPOSITION_PIE_STANDARD, new DetectionCompositionPieChartBuilder(mReportRoot, mReportPageHeader));
					
					break;
				}
				case TIME_SERIES_EXPLICIT: {
					mChartBuilders.put(EVideoTriageChart.TIME_SERIES_EXPLICIT, new TimeSeriesExplicitDetectionChartBuilder(mReportRoot, mReportPageHeader));
					
					break;
				}
			}
		}
	}
	
	public ArrayList<EVideoTriageChart> getSortedChartSet() {
		ArrayList<EVideoTriageChart> sortedList = new ArrayList<EVideoTriageChart>(mChartSet);
		Collections.sort(sortedList, new VideoTriageChartComparator());
		
		return sortedList;
	}
 
	public HashMap<EVideoTriageChart, VideoTriageChartBuilder> getChartBuilders() {
		return mChartBuilders;
	}
	
	public VideoTriageChartBuilder getChartBuilder(EVideoTriageChart chartType) {
		return mChartBuilders.get(chartType);
	}
	
	public boolean containsChartBuilder(EVideoTriageChart chartType) {
		return mChartBuilders.containsKey(chartType);
	}
	
}

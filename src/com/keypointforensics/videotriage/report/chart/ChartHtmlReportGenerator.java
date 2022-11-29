package com.keypointforensics.videotriage.report.chart;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import com.keypointforensics.videotriage.blob.context.BlobContextEntry;
import com.keypointforensics.videotriage.blob.context.BlobContextList;
import com.keypointforensics.videotriage.blob.context.BlobContextListParser;
import com.keypointforensics.videotriage.chart.BlobTrackingHeatMapChartBuilder;
import com.keypointforensics.videotriage.chart.BlobTrackingScatterBackgroundChartBuilder;
import com.keypointforensics.videotriage.chart.BlobTrackingScatterChartBuilder;
import com.keypointforensics.videotriage.chart.EVideoTriageChart;
import com.keypointforensics.videotriage.chart.HistogramContourBackgroundChartBuilder;
import com.keypointforensics.videotriage.chart.HourOfDayBarChartBuilder;
import com.keypointforensics.videotriage.chart.ReportChartSettings;
import com.keypointforensics.videotriage.chart.TimeSeriesChartBuilder;
import com.keypointforensics.videotriage.chart.TimeSeriesFilledAreaChartBuilder;
import com.keypointforensics.videotriage.chart.TimeSeriesMassChartBuilder;
import com.keypointforensics.videotriage.chart.TimeSeriesScatterChartBuilder;
import com.keypointforensics.videotriage.chart.VideoTriageChartBuilder;
import com.keypointforensics.videotriage.legacy.VideoFalsePositiveBundleLegacy;
import com.keypointforensics.videotriage.list.SortedList;
import com.keypointforensics.videotriage.progress.ProgressBundle;
import com.keypointforensics.videotriage.report.ReportCaptureBundle;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ProgressUtils;
import com.keypointforensics.videotriage.util.Utils;

public class ChartHtmlReportGenerator {

	/*
	 * Author: Daniel Puckowski
	 */

	private final DateFormat VIDEO_CREATION_DATE_FORMATTER = new SimpleDateFormat("MM-dd-yyyy HH-mm-ss");
	
	private final String RELATIVE_CAPTURE_FOLDER_PATH_ROOT         = "captures";
	private final String RELATIVE_PAGES_FOLDER_PATH_ROOT           = "pages";
	private final String RELATIVE_CHART_FOLDER_PATH_ROOT           = "charts";
	
	private final BlobContextList BLOB_CONTEXT_LIST;
	private final boolean         REPORT_ICON_ENABLED;
	private final boolean         CUSTOM_REPORT_ICON_ENABLED;
	private final File            CUSTOM_REPORT_ICON_FILE;
	private final boolean         REPORT_PAGINATION_ENABLED;
	
	private final ReportChartSettings REPORT_CHART_SETTINGS;
	
	private String mCaseName;
	private StringBuilder mBuilder;
	private String mReportFilename;
	private String mReportFilenameRoot;
	private String mCapturePath;
	private String mReportFolderName;
	private String mPagesFolderName;
	private VideoFalsePositiveBundleLegacy mFalsePositiveBundle;
	private BlobContextListParser mBlobContextListParser;
	private BlobContextEntry mBlobContextEntry;
	private int mOriginalKeypointCount;
	private boolean mHasCharts;
	private String mReportPageHeader;
	
	private Calendar mGregorianCalendar;
	private Date mTempDate;

	private int mPage;

	private SortedList<ReportCaptureBundle> mCaptures;
	
	private ProgressBundle mProgressBundle;
		
	public ChartHtmlReportGenerator(final String caseName, final String capturePath,
			final VideoFalsePositiveBundleLegacy falsePositiveBundle, final BlobContextList blobContextList,
			final boolean reportIconEnabled, final boolean customReportIconEnabled, final File customIconFile,
			final boolean reportPaginationEnabled, final ReportChartSettings reportChartSettings) {
		BLOB_CONTEXT_LIST          = blobContextList;
		REPORT_ICON_ENABLED        = reportIconEnabled;
		CUSTOM_REPORT_ICON_ENABLED = customReportIconEnabled;
		CUSTOM_REPORT_ICON_FILE    = customIconFile;
		REPORT_PAGINATION_ENABLED  = reportPaginationEnabled;
		REPORT_CHART_SETTINGS      = reportChartSettings;
		
		mCaseName = caseName;
		
		mBuilder = new StringBuilder(2000);
		mCapturePath = capturePath;
		mFalsePositiveBundle = falsePositiveBundle;
		mHasCharts = REPORT_CHART_SETTINGS.hasRequestedCharts();
		
		mGregorianCalendar = new GregorianCalendar();

		createReportFolder();
	}
	
	private void createReportFolder() {
		if(mCapturePath.contains(File.separator)) {
			mCapturePath = mCapturePath.substring(mCapturePath.lastIndexOf(File.separator) + 1, mCapturePath.length());
		}
				
		mReportFolderName = FileUtils.REPORTS_DIRECTORY + mCapturePath + "_" + Utils.getTimeStamp() + File.separator;
		
		File reportFolder = new File(mReportFolderName);
		final File captureFolder = new File(mReportFolderName + RELATIVE_CAPTURE_FOLDER_PATH_ROOT);
		
		mReportFilename = mReportFolderName + mCapturePath + "_report.html";
		mReportFilenameRoot = mReportFilename;
		
		reportFolder.mkdir();
		captureFolder.mkdir();
		
		if(REPORT_PAGINATION_ENABLED == true) {
			File pagesFolder = new File(mReportFolderName + RELATIVE_PAGES_FOLDER_PATH_ROOT);
			mPagesFolderName = pagesFolder.getAbsolutePath() + File.separator;
			pagesFolder.mkdir();
			
			//
			//try {
				//Files.copy(Paths.get(FileUtils.FONT_DIRECTORY + "default_font.css"),
				//		Paths.get(mPagesFolderName + "default_font.css"), StandardCopyOption.REPLACE_EXISTING);
				//Files.copy(Paths.get(FileUtils.GRAPHICS_DIRECTORY + "favicon.png"),
				//		Paths.get(mPagesFolderName + "favicon.png"), StandardCopyOption.REPLACE_EXISTING);
			//} catch (IOException e) {
			//	e.printStackTrace();
			//}
			//
		}
		
		mBlobContextListParser = new BlobContextListParser(BLOB_CONTEXT_LIST.getContextFilename(), mFalsePositiveBundle);
		
		try {
			mBlobContextListParser.parseEntries();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	private void addCaptureRowChartEvent(final ReportCaptureBundle reportCaptureBundle) {
		//
		final String captureEventDate = reportCaptureBundle.getCaptureEventDate();
		
		if(mHasCharts == true) {		
			if(REPORT_CHART_SETTINGS.containsChartBuilder(EVideoTriageChart.TIME_SERIES_STANDARD) == true) {
				TimeSeriesChartBuilder timeSeriesChartBuilder = (TimeSeriesChartBuilder) REPORT_CHART_SETTINGS.getChartBuilder(EVideoTriageChart.TIME_SERIES_STANDARD);
				timeSeriesChartBuilder.addEvent(captureEventDate);
			}
			if(REPORT_CHART_SETTINGS.containsChartBuilder(EVideoTriageChart.TIME_SERIES_SCATTER) == true) {
				TimeSeriesScatterChartBuilder timeSeriesScatterChartBuilder = (TimeSeriesScatterChartBuilder) REPORT_CHART_SETTINGS.getChartBuilder(EVideoTriageChart.TIME_SERIES_SCATTER);
				timeSeriesScatterChartBuilder.addEvent(captureEventDate);
			}
			if(REPORT_CHART_SETTINGS.containsChartBuilder(EVideoTriageChart.TIME_SERIES_FILLED_AREA) == true) {
				TimeSeriesFilledAreaChartBuilder timeSeriesFilledAreaChartBuilder = (TimeSeriesFilledAreaChartBuilder) REPORT_CHART_SETTINGS.getChartBuilder(EVideoTriageChart.TIME_SERIES_FILLED_AREA);
				timeSeriesFilledAreaChartBuilder.addEvent(captureEventDate);
			}
			if(REPORT_CHART_SETTINGS.containsChartBuilder(EVideoTriageChart.TIME_SERIES_MASS) == true) {
				TimeSeriesMassChartBuilder timeSeriesMassChartBuilder = (TimeSeriesMassChartBuilder) REPORT_CHART_SETTINGS.getChartBuilder(EVideoTriageChart.TIME_SERIES_MASS);
				timeSeriesMassChartBuilder.addEvent(captureEventDate, new File(reportCaptureBundle.getCaptureAbsolutePath()).length());
			}
			if(REPORT_CHART_SETTINGS.containsChartBuilder(EVideoTriageChart.BAR_CHART_HOUR_OF_DAY_STANDARD) == true) {
				HourOfDayBarChartBuilder hourOfDayBarChartBuilder = (HourOfDayBarChartBuilder) REPORT_CHART_SETTINGS.getChartBuilder(EVideoTriageChart.BAR_CHART_HOUR_OF_DAY_STANDARD);
				hourOfDayBarChartBuilder.addEvent(captureEventDate);
			}
		}
		//
	}

	private void updateReportFilenameForPage() {
		if(mPage > 0) {
			mReportFilename = mPagesFolderName + mCapturePath + "_" + (mPage + 1) + "_report.html";
		} else {
			mReportFilename = mReportFolderName + mCapturePath + "_" + (mPage + 1) + "_report.html";
		}
	}
	
	private void writeReport(boolean withNavigationFooter) throws UnsupportedEncodingException, FileNotFoundException, IOException {		
		if (mPage == 0) {
			if(REPORT_PAGINATION_ENABLED == true) {
				mBuilder.append("<br><div class=\"table-title\"><table align=\"center\"></table></div>");
			}
			
			try (Writer writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(mReportFilename), "utf-8"))) {
				writer.write(mBuilder.toString());
			}
		} else {
			updateReportFilenameForPage();

			if(REPORT_PAGINATION_ENABLED == true) {
				mBuilder.append("<br><div class=\"table-title\"><table align=\"center\"></table></div>");
			}
			
			try (Writer writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(mReportFilename), "utf-8"))) {
				writer.write(mBuilder.toString());
			}
		}
	}
	
	private void createChartFolder() {
		File chartFolder = new File(mReportFolderName + RELATIVE_CHART_FOLDER_PATH_ROOT);
		chartFolder.mkdir();
		
		/*
		try {
			Files.copy(Paths.get(FileUtils.GRAPHICS_DIRECTORY + "favicon.png"),
					Paths.get(mReportFolderName + RELATIVE_CHART_FOLDER_PATH_ROOT + File.separator + "favicon.png"), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
	}
	
	public String createRootPage() {
		mProgressBundle = ProgressUtils.getIndeterminateProgressBundle("Creating Report...");
		
		try {
			//
			if(REPORT_ICON_ENABLED == true) {
				if (CUSTOM_REPORT_ICON_ENABLED == true && CUSTOM_REPORT_ICON_FILE.exists() == true) {
					Files.copy(Paths.get(CUSTOM_REPORT_ICON_FILE.getAbsolutePath()),
						Paths.get(mReportFolderName + "report_icon.png"), StandardCopyOption.REPLACE_EXISTING);
				} else {
					if(CUSTOM_REPORT_ICON_ENABLED == true) {
						final int dialogSelection = Utils.displayConfirmDialog("Report Icon Error",
								"Failed to load custom report icon. Use default icon?");
		
						if (dialogSelection == JOptionPane.YES_OPTION) {
							mBuilder.append("<td><img width=\"120\" height=\"120\" src=\"report_icon.png\"/></td>");
						}
					}
					
					Files.copy(Paths.get(FileUtils.GRAPHICS_DIRECTORY + "splash_screen_image.png"),
							Paths.get(mReportFolderName + "report_icon.png"), StandardCopyOption.REPLACE_EXISTING);
				} 
			}
			//
			
			Files.copy(Paths.get(FileUtils.GRAPHICS_DIRECTORY + "favicon.png"),
					Paths.get(mReportFolderName + "favicon.png"), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(Paths.get(FileUtils.FONT_DIRECTORY + "default_font.css"),
					Paths.get(mReportFolderName + "default_font.css"), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		mCaptures = mFalsePositiveBundle.getCaptureFilenamesSorted();
		mOriginalKeypointCount = mCaptures.size();
		
		//mProgressBundle = ProgressUtils.getProgressBundle("Creating Report...", mCaptures.size());
		
		double secondsIntoVideo;
		String shortExtractFilenameLowerCase;
		String modifiedDateString;

		for (ReportCaptureBundle capture : mCaptures) {
			shortExtractFilenameLowerCase = capture.getCaptureAbsolutePath().toLowerCase();

			if (shortExtractFilenameLowerCase.contains(File.separator) == true) {
				shortExtractFilenameLowerCase = shortExtractFilenameLowerCase.substring(
						shortExtractFilenameLowerCase.lastIndexOf(File.separator) + 1,
						shortExtractFilenameLowerCase.length());
			}

			capture.setShortExtractFilenameLowerCase(shortExtractFilenameLowerCase);

			mBlobContextEntry = mBlobContextListParser.getEntryByFilename(shortExtractFilenameLowerCase);
			
			// Establish time...
			//TODO
			if(mBlobContextEntry == null) {
				//continue;
			}
			
			secondsIntoVideo = ((double) mBlobContextEntry.frameIndex
					/ (double) mBlobContextEntry.framesPerSecondTarget);

			capture.setSecondsIntoVideo(secondsIntoVideo);
			//
			
			try {
				mTempDate = VIDEO_CREATION_DATE_FORMATTER.parse(capture.getCaptureEventDate());
			} catch (ParseException parseException) {
				// parseException.printStackTrace();
			}

			if(mTempDate != null) {
				mGregorianCalendar.setTime(mTempDate);
				mGregorianCalendar.add(Calendar.SECOND, (int) Math.round(secondsIntoVideo));
				mTempDate = mGregorianCalendar.getTime();
	
				modifiedDateString = VIDEO_CREATION_DATE_FORMATTER.format(mTempDate).toString();
			
				capture.parseDateString(modifiedDateString);
				capture.setCaptureEventDate(modifiedDateString);
			} else {
				capture.setCaptureEventDate("No Date Recorded");
			}
		}
		
		Collections.sort(mCaptures);
		
		//
		mBuilder.append(
				"<html lang=\"en\"><head><link rel=\"stylesheet\" type=\"text/css\" href=\"default_font.css\"/><link rel=\"icon\" type=\"image/png\" href=\"favicon.png\"><meta charset=\"utf-8\" /><style>body {font-family: \"Roboto\", helvetica, arial, sans-serif;font-size: 16px;font-weight: 400;text-rendering: optimizeLegibility;}div.table-title {display: block;margin: auto;text-align:center;padding:5px;width: 99%;}.table-title h3 {color: #666B85;font-size: 30px;font-weight: 400;font-style:normal;font-family: \"Roboto\", helvetica, arial, sans-serif;padding:0px;margin:0px;}.table-title p {color: #666B85;font-size: 18px;font-weight: 400;font-style:normal;font-family: \"Roboto\", helvetica, arial, sans-serif;}.table-fill {background: white;border-radius:3px;border-collapse: collapse;margin: auto;max-width: 1920px;padding:5px;width: 99%;box-shadow: 0 5px 10px rgba(0, 0, 0, 0.1);animation: float 5s infinite;}th {color:#D5DDE5;background:#1b1e24;border-bottom:4px solid #9ea7af;border-right: 1px solid #343a45;font-size:23px;font-weight: 100;padding:24px;text-align:left;vertical-align:middle;}th:first-child {border-top-left-radius:3px;}th:last-child {border-top-right-radius:3px;border-right:none;}tr.outer {border-top: 1px solid #C1C3D1;border-bottom-: 1px solid #C1C3D1;color:#666B85;font-size:16px;font-weight:normal;}tr.outer:first-child {border-top:none;}tr.outer:last-child {border-bottom:none;}tr.outer:nth-child(odd) td {background:#EBEBEB;}tr.outer:last-child td:first-child {border-bottom-left-radius:3px;}tr.outer:last-child td:last-child {border-bottom-right-radius:3px;}td {background:#FFFFFF;padding:20px;text-align:left;vertical-align:middle;font-weight:300;font-size:18px;border-right: 1px solid #C1C3D1;}td:last-child {border-right: 0px;}th.text-left {text-align: left;}th.text-center {text-align: center;}th.text-right {text-align: right;}td.text-left {text-align: left;}td.text-center {text-align: center;}td.text-right {text-align: right;}</style><title>");

		String temporaryCaseName = mCaseName;
		if (temporaryCaseName.contains(File.separator) == true) {
			temporaryCaseName = temporaryCaseName.substring(temporaryCaseName.lastIndexOf(File.separator) + 1, temporaryCaseName.length());
		}

		if (temporaryCaseName.equals("db.videotriage") == true) {
			temporaryCaseName = "Temporary " + Utils.SOFTWARE_NAME + " Case";
		}

		mBuilder.append("Case: ");
		mBuilder.append(temporaryCaseName);

		mBuilder.append(
				"</title><meta name=\"viewport\" content=\"initial-scale=1.0; maximum-scale=1.0; width=device-width;\"></head><body><div class=\"table-title\"><table align=\"center\"><tr>");

		if(REPORT_ICON_ENABLED == true) {
			mBuilder.append("<td><img width=\"120\" height=\"120\" src=\"report_icon.png\"/></td>");
		}
		
		/*
		if (REPORT_ICON_ENABLED == true) {
			if (CUSTOM_REPORT_ICON_ENABLED == false) {
				mBuilder.append("<td><img width=\"120\" height=\"120\" src=\"");//file:///");
				//mBuilder.append(mReportFolderName);
				mBuilder.append("report_icon.png");//splash_screen_image.png");
				mBuilder.append("\"/></td>");
			} else if (CUSTOM_REPORT_ICON_ENABLED == true && CUSTOM_REPORT_ICON_FILE.exists() == true) {
				mBuilder.append("<td><img width=\"120\" height=\"120\" src=\"");//file:///");
				mBuilder.append("report_icon.png");//CUSTOM_REPORT_ICON_FILE.getAbsolutePath());
				mBuilder.append("\"/></td>");
			} else if (CUSTOM_REPORT_ICON_ENABLED == true) {
				final int dialogSelection = Utils.displayConfirmDialog("Report Icon Error",
						"Failed to load custom report icon. Use default icon?");

				if (dialogSelection == JOptionPane.YES_OPTION) {
					mBuilder.append("<td><img width=\"120\" height=\"120\" src=\"");//file:///");
					//mBuilder.append(mReportFolderName);
					mBuilder.append("report_icon.png");//splash_screen_image.png");
					mBuilder.append("\"/></td>");
				}
			}
		}
		*/
		
		mBuilder.append("<td><h3>Case: ");
		mBuilder.append(temporaryCaseName);
		mBuilder.append("</h3><p>");
		mBuilder.append(Utils.getTimeStampForReport());
		mBuilder.append("<br>");
		mBuilder.append(mOriginalKeypointCount);
		mBuilder.append(" key points</p></td></tr></table></div><br>");
				
		mReportPageHeader = mBuilder.toString();
		mReportPageHeader = mReportPageHeader.replaceFirst("favicon.png", "../favicon.png");
		mReportPageHeader = mReportPageHeader.replaceFirst("default_font.css", "../default_font.css");
		mReportPageHeader = mReportPageHeader.replaceFirst("report_icon.png", "../report_icon.png");
		REPORT_CHART_SETTINGS.initializeChartBuilders(mReportFilenameRoot, mReportPageHeader);
		//
		
		//
		if(mHasCharts == true) {
			if(REPORT_CHART_SETTINGS.containsChartBuilder(EVideoTriageChart.BLOB_TRACKING_SCATTER_STANDARD) == true ||
				REPORT_CHART_SETTINGS.containsChartBuilder(EVideoTriageChart.BLOB_TRACKING_HEAT_MAP_STANDARD) == true ||
				REPORT_CHART_SETTINGS.containsChartBuilder(EVideoTriageChart.BLOB_TRACKING_SCATTER_BACKGROUND) == true ||
				REPORT_CHART_SETTINGS.containsChartBuilder(EVideoTriageChart.HISTOGRAM_CONTOUR_BACKGROUND) == true) {
				ArrayList<String> coordinateLines = new ArrayList<String>();
				ArrayList<String> frameBounds = new ArrayList<String>();
				
				final String coordinateFilename = BLOB_CONTEXT_LIST.getCoordinateFilename();
				final File recordedCoordinateFile = new File(coordinateFilename);
				
				if(recordedCoordinateFile.exists() == true) {
					try(BufferedReader br = new BufferedReader(new FileReader(recordedCoordinateFile))) {
					    for(String line; (line = br.readLine()) != null; ) {
					    	if(line.startsWith(BlobContextList.FRAME_BOUNDARY_TAG) == false) {
					    		coordinateLines.add(line);
					    	} else {
					    		frameBounds.add(line);
					    	}
					    }
					} catch (FileNotFoundException fileNotFoundException) {
						//fileNotFoundException.printStackTrace();
					} catch (IOException ioException) {
						//ioException.printStackTrace();
					}
				}
				
				if(REPORT_CHART_SETTINGS.containsChartBuilder(EVideoTriageChart.BLOB_TRACKING_SCATTER_STANDARD) == true) {
					BlobTrackingScatterChartBuilder blobTrackingScatterChartBuilder = (BlobTrackingScatterChartBuilder) REPORT_CHART_SETTINGS.getChartBuilder(EVideoTriageChart.BLOB_TRACKING_SCATTER_STANDARD);
					
					for(String coordinateLine : coordinateLines) {
						blobTrackingScatterChartBuilder.addEvent(coordinateLine);
					}
					
					for(String frameBound : frameBounds) {
						blobTrackingScatterChartBuilder.addFrameBound(frameBound);
					}
				}
				
				if(REPORT_CHART_SETTINGS.containsChartBuilder(EVideoTriageChart.BLOB_TRACKING_HEAT_MAP_STANDARD) == true) {
					BlobTrackingHeatMapChartBuilder blobTrackingHeatMapChartBuilder = (BlobTrackingHeatMapChartBuilder) REPORT_CHART_SETTINGS.getChartBuilder(EVideoTriageChart.BLOB_TRACKING_HEAT_MAP_STANDARD);
					
					for(String coordinateLine : coordinateLines) {
						blobTrackingHeatMapChartBuilder.addEvent(coordinateLine);
					}
				}
				
				if(REPORT_CHART_SETTINGS.containsChartBuilder(EVideoTriageChart.BLOB_TRACKING_SCATTER_BACKGROUND) == true) {
					BlobTrackingScatterBackgroundChartBuilder blobTrackingScatterBackgroundChartBuilder = (BlobTrackingScatterBackgroundChartBuilder) REPORT_CHART_SETTINGS.getChartBuilder(EVideoTriageChart.BLOB_TRACKING_SCATTER_BACKGROUND);
					
					for(String coordinateLine : coordinateLines) {
						blobTrackingScatterBackgroundChartBuilder.addEvent(coordinateLine);
					}
					
					for(String frameBound : frameBounds) {
						blobTrackingScatterBackgroundChartBuilder.addFrameBound(frameBound);
					}
				}
				
				if(REPORT_CHART_SETTINGS.containsChartBuilder(EVideoTriageChart.HISTOGRAM_CONTOUR_BACKGROUND) == true) {
					HistogramContourBackgroundChartBuilder histogramContourBackgroundChartBuilder = (HistogramContourBackgroundChartBuilder) REPORT_CHART_SETTINGS.getChartBuilder(EVideoTriageChart.HISTOGRAM_CONTOUR_BACKGROUND);
					
					for(String coordinateLine : coordinateLines) {
						histogramContourBackgroundChartBuilder.addEvent(coordinateLine);
					}
					
					for(String frameBound : frameBounds) {
						histogramContourBackgroundChartBuilder.addFrameBound(frameBound);
					}
				}
			}
		}
		//
		
		//
		if(mHasCharts == true && REPORT_PAGINATION_ENABLED == true) {
			createChartFolder();
			
			try {
				Files.copy(Paths.get(FileUtils.CHART_DIRECTORY + "chart.js"),
						Paths.get(mReportFolderName + RELATIVE_CHART_FOLDER_PATH_ROOT + File.separator + "chart.js"), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			
			ArrayList<EVideoTriageChart> sortedChartSet = REPORT_CHART_SETTINGS.getSortedChartSet();
			
			mBuilder.append("<table class=\"table-fill\"><thead><tr><th class=\"text-left\">Charts</th></tr></thead><tbody class=\"table-hover\">");

			for(int i = 0; i < sortedChartSet.size(); ++i) {
				mBuilder.append("<tr class=\"outer\"><td class=\"text-left\"><a href=\"");
				mBuilder.append(RELATIVE_CHART_FOLDER_PATH_ROOT + File.separator + sortedChartSet.get(i).getPageNameShort());
				mBuilder.append("\">");
				mBuilder.append(sortedChartSet.get(i).toString());
				mBuilder.append("</a></td></tr>");
			}
			
			mBuilder.append("</tbody></table><br>");
		} else if(mHasCharts == true && REPORT_PAGINATION_ENABLED == false) {//else {
			try {
				Files.copy(Paths.get(FileUtils.CHART_DIRECTORY + "chart.js"),
						Paths.get(mReportFolderName + File.separator + "chart.js"), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			
			mBuilder.append("<script src=\"chart.js\"></script>");		
			mBuilder.append("<table class=\"table-fill\"><thead><tr><th class=\"text-left\">Charts</th></tr></thead></table><br>");
		}
		//
		
		//
		for(int i = 0; i < mCaptures.size(); ++i) {
			addCaptureRowChartEvent(mCaptures.get(i));
		}
		
		if(mHasCharts == true) {
			HashMap<EVideoTriageChart, VideoTriageChartBuilder> reportChartBuilders = REPORT_CHART_SETTINGS.getChartBuilders();
			PrintWriter chartDataWriter;
			VideoTriageChartBuilder chartBuilder;
			EVideoTriageChart chartType;
			String absoluteDataFileName;
			String absolutePageFileName;
			String chartDirectoryRoot;
			
			//final String chartDirectoryRoot = mReportFolderName + RELATIVE_CHART_FOLDER_PATH_ROOT + File.separator;
			
			for(Entry<EVideoTriageChart, VideoTriageChartBuilder> chartBuilderEntry : reportChartBuilders.entrySet()) {
				chartBuilder = chartBuilderEntry.getValue();
				chartBuilder.build(REPORT_PAGINATION_ENABLED);
				
				chartType = chartBuilderEntry.getKey();
				
				chartDirectoryRoot = null;
				
				if(REPORT_PAGINATION_ENABLED == true) {
					chartDirectoryRoot = mReportFolderName + RELATIVE_CHART_FOLDER_PATH_ROOT + File.separator;
				} else {
					chartDirectoryRoot = mReportFolderName + File.separator;
				}

				absoluteDataFileName = chartDirectoryRoot + chartType.getDataNameShort();
				
				try {
					chartDataWriter = new PrintWriter(absoluteDataFileName);
					chartDataWriter.println(chartBuilderEntry.getValue().getChart());
					chartDataWriter.flush();
					chartDataWriter.close();
				} catch (FileNotFoundException fileNotFoundException) {
					fileNotFoundException.printStackTrace();
				}
				
				if(REPORT_PAGINATION_ENABLED == true) {
					absolutePageFileName = chartDirectoryRoot + chartType.getPageNameShort();
					
					try {
						chartDataWriter = new PrintWriter(absolutePageFileName);
						chartDataWriter.println(chartBuilderEntry.getValue().getHtmlFile());
						chartDataWriter.flush();
						chartDataWriter.close();
					} catch (FileNotFoundException fileNotFoundException) {
						fileNotFoundException.printStackTrace();
					}	
				} else {
					mBuilder.append(chartBuilder.getHtmlFile());
					//mBuilder.append(chartBuilder.getChart());
				}
			}
		}
		//
		
		//mProgressBundle.progressBar.setIndeterminate(true);
		//mProgressBundle.progressBar.repaint();
		
		if (mBuilder.length() > 0) {
			mBuilder.append("</tbody></table></body></html>");

			try {
				writeReport(false);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//mProgressBundle.progressBar.setValue(mProgressBundle.progressBar.getValue() + 1);
		//mProgressBundle.progressBar.repaint();
		mProgressBundle.frame.dispose();
		
		return mReportFilenameRoot;
	}
}

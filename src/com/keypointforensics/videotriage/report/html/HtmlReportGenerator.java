package com.keypointforensics.videotriage.report.html;

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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import com.keypointforensics.videotriage.blob.context.BlobContextEntry;
import com.keypointforensics.videotriage.blob.context.BlobContextList;
import com.keypointforensics.videotriage.blob.context.BlobContextListParser;
import com.keypointforensics.videotriage.casestore.CaseMetadataWriter;
import com.keypointforensics.videotriage.chart.BlobTrackingHeatMapChartBuilder;
import com.keypointforensics.videotriage.chart.BlobTrackingScatterBackgroundChartBuilder;
import com.keypointforensics.videotriage.chart.BlobTrackingScatterChartBuilder;
import com.keypointforensics.videotriage.chart.DetectionCompositionPieChartBuilder;
import com.keypointforensics.videotriage.chart.EVideoTriageChart;
import com.keypointforensics.videotriage.chart.HistogramContourBackgroundChartBuilder;
import com.keypointforensics.videotriage.chart.HourOfDayBarChartBuilder;
import com.keypointforensics.videotriage.chart.ReportChartSettings;
import com.keypointforensics.videotriage.chart.TimeSeriesCarDetectionChartBuilder;
import com.keypointforensics.videotriage.chart.TimeSeriesChartBuilder;
import com.keypointforensics.videotriage.chart.TimeSeriesFaceDetectionChartBuilder;
import com.keypointforensics.videotriage.chart.TimeSeriesFilledAreaChartBuilder;
import com.keypointforensics.videotriage.chart.TimeSeriesLicensePlateDetectionChartBuilder;
import com.keypointforensics.videotriage.chart.TimeSeriesMassChartBuilder;
import com.keypointforensics.videotriage.chart.TimeSeriesPedestrianDetectionChartBuilder;
import com.keypointforensics.videotriage.chart.TimeSeriesScatterChartBuilder;
import com.keypointforensics.videotriage.chart.VideoTriageChartBuilder;
import com.keypointforensics.videotriage.detect.EVideoTriageDetectionModule;
import com.keypointforensics.videotriage.legacy.FileUtilsLegacy;
import com.keypointforensics.videotriage.legacy.VideoFalsePositiveBundleLegacy;
import com.keypointforensics.videotriage.list.SortedList;
import com.keypointforensics.videotriage.progress.ProgressBundle;
import com.keypointforensics.videotriage.report.CarExtractPageGenerator;
import com.keypointforensics.videotriage.report.FaceExtractPageGenerator;
import com.keypointforensics.videotriage.report.LicensePlateExtractPageGenerator;
import com.keypointforensics.videotriage.report.NotePageGenerator;
import com.keypointforensics.videotriage.report.NoteworthyFramePageGenerator;
import com.keypointforensics.videotriage.report.PedestrianExtractPageGenerator;
import com.keypointforensics.videotriage.report.ReportCaptureBundle;
import com.keypointforensics.videotriage.report.VideoMetadataPageGenerator;
import com.keypointforensics.videotriage.report.stats.StatisticsPageGenerator;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ProgressUtils;
import com.keypointforensics.videotriage.util.Utils;

public class HtmlReportGenerator {

	/*
	 * Author: Daniel Puckowski
	 */

	private final DateFormat VIDEO_CREATION_DATE_FORMATTER = new SimpleDateFormat("MM-dd-yyyy HH-mm-ss");
	
	private final String RELATIVE_CAPTURE_FOLDER_PATH_ROOT         = "captures";
	private final String RELATIVE_PAGES_FOLDER_PATH_ROOT           = "pages";
	private final String RELATIVE_CHART_FOLDER_PATH_ROOT           = "charts";
	private final String RELATIVE_STATISTICS_FOLDER_PATH_ROOT      = "statistics";
	private final String RELATIVE_NOTES_FOLDER_PATH_ROOT           = "notes";
	
	private final int DEFAULT_REPORT_PAGE_COUNT  = 40;
	private final int DEFAULT_PAGE_ITEMS_PER_ROW = 25;
	
	private final BlobContextList BLOB_CONTEXT_LIST;
	private final boolean         REPORT_ICON_ENABLED;
	private final boolean         CUSTOM_REPORT_ICON_ENABLED;
	private final File            CUSTOM_REPORT_ICON_FILE;
	private final boolean         REPORT_PAGINATION_ENABLED;
	private final String          CONTEXT_FILENAME;
	
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
	private HashMap<String, Integer> mExtractCountForVideoMap;
	private int mCurrentRowIndex;
	private int mOriginalKeypointCount;
	private boolean mHasCharts;
	private boolean mHasEvidenceListing;
	private boolean mHasEnhancedEvidenceListing;
	private String mReportPageHeader;
	private boolean mHasVideoMetadataPage;
	private boolean mHasFaceDetectionPage;
	private boolean mHasLicensePlateDetectionPage;
	private boolean mHasPedestrianDetectionPage;
	private boolean mHasCarDetectionPage;
	private boolean mHasExplicitDetectionPage;
	private boolean mHasStatisticsPage;
	private boolean mHasReportExtractsPage;
	private boolean mHasRedactedEvidenceListing;
	private boolean mHasMergedListing;
	
	private StatisticsPageGenerator mStatisticsPageGenerator;
	
	private Calendar mGregorianCalendar;
	private Date mTempDate;

	private int mPage;

	private SortedList<ReportCaptureBundle> mCaptures;
	
	private ProgressBundle mProgressBundle;
		
	public HtmlReportGenerator(final String caseName, final String capturePath,
			final VideoFalsePositiveBundleLegacy falsePositiveBundle, final BlobContextList blobContextList,
			final boolean reportIconEnabled, final boolean customReportIconEnabled, final File customIconFile,
			final boolean reportPaginationEnabled, final ReportChartSettings reportChartSettings,
			final boolean hasVideoMetadataPage, final boolean hasStatisticsPage) {
		BLOB_CONTEXT_LIST          = blobContextList;
		REPORT_ICON_ENABLED        = reportIconEnabled;
		CUSTOM_REPORT_ICON_ENABLED = customReportIconEnabled;
		CUSTOM_REPORT_ICON_FILE    = customIconFile;
		REPORT_PAGINATION_ENABLED  = reportPaginationEnabled;
		REPORT_CHART_SETTINGS      = reportChartSettings;
		CONTEXT_FILENAME           = CaseMetadataWriter.getContextFilenameFromDatabaseName(caseName);
		
		mCaseName = caseName;
		
		mBuilder = new StringBuilder(2000);
		mCapturePath = capturePath;
		mFalsePositiveBundle = falsePositiveBundle;
		mExtractCountForVideoMap = new HashMap<String, Integer>();
		mCurrentRowIndex = 1;
		mHasCharts = REPORT_CHART_SETTINGS.hasRequestedCharts();
		mHasVideoMetadataPage = hasVideoMetadataPage;
		
		mGregorianCalendar = new GregorianCalendar();

		mHasEvidenceListing = true;
		mHasEnhancedEvidenceListing = true;
		mHasFaceDetectionPage = true;
		mHasLicensePlateDetectionPage = true;
		mHasPedestrianDetectionPage = true;
		mHasCarDetectionPage = true;
		mHasExplicitDetectionPage = true;
		mHasRedactedEvidenceListing = true;
		mHasMergedListing = true;
		
		mHasStatisticsPage = hasStatisticsPage;
		
		if(mHasStatisticsPage == true) {
			mStatisticsPageGenerator = new StatisticsPageGenerator();
		}
		
		mHasReportExtractsPage = checkReportExtractsPage();
		
		createReportFolder();
	}
	
	private boolean checkReportExtractsPage() {
		return new File(FileUtils.REPORT_EXTRACTS_DIRECTORY + getFormattedCaseName()).exists();
	}
	
	private String getFormattedCaseName() {
		String formattedCaseName = mCaseName;
		
		if(formattedCaseName.contains(File.separator) == true) {
			formattedCaseName = formattedCaseName.substring(formattedCaseName.lastIndexOf(File.separator) + 1, formattedCaseName.length());
		}
		
		return formattedCaseName;
	}
	
	//
	private void createStatisticsFolder() {
		File statisticsFolder = new File(mReportFolderName + RELATIVE_STATISTICS_FOLDER_PATH_ROOT);
		statisticsFolder.mkdir();
	}
	//
	
	//
	private void createNotesFolder() {
		File notesFolder = new File(mReportFolderName + RELATIVE_NOTES_FOLDER_PATH_ROOT);
		notesFolder.mkdir();
	}
	//
	
	//
	private boolean checkCaseHasCars() {
		File carExtractFolderOriginal = new File(FileUtils.CARS_DIRECTORY + getFormattedCaseName());
		
		return carExtractFolderOriginal.exists();
	}
	//
	
	//
	private boolean checkCaseHasExplicitImages() {
		File explicitExtractFolderOriginal = new File(FileUtils.EXPLICIT_DIRECTORY + getFormattedCaseName());
		
		return explicitExtractFolderOriginal.exists();
	}
	//
	
	//
	private boolean checkCaseHasPedestrians() {
		File pedestrianExtractFolderOriginal = new File(FileUtils.PEDESTRIANS_DIRECTORY + getFormattedCaseName());
		
		return pedestrianExtractFolderOriginal.exists();
	}
	//
	
	//
	private boolean checkCaseHasFaces() {
		File faceExtractFolderOriginal = new File(FileUtils.FACES_DIRECTORY + getFormattedCaseName());
		
		return faceExtractFolderOriginal.exists();
	}
	//
	
	//
	private boolean checkCaseHasLicensePlates() {
		File licensePlatesExtractFolderOriginal = new File(FileUtils.PLATES_DIRECTORY + getFormattedCaseName());
		
		return licensePlatesExtractFolderOriginal.exists();
	}
	//
	
	private void copyCapturesToCaptureFolder(final String captureFolderAbsolutePath) {
		String originalFilename, filename, capturePath = null;
		File originalFile, copyToFile;
		
		for(int i = 0; i < mFalsePositiveBundle.getCaptureFilenames().size(); ++i) {
			filename = mFalsePositiveBundle.getCaptureFilenames().get(i);
			originalFilename = filename;
			
			if(filename.contains(File.separator)) {
				filename = filename.substring(filename.lastIndexOf(File.separator) + 1, filename.length());
			}
			
			capturePath = captureFolderAbsolutePath + filename;
			
			mFalsePositiveBundle.getCaptureFilenames().set(i, capturePath);

			originalFile = new File(originalFilename);
			copyToFile = new File(capturePath);
			
			try {
				Files.copy(originalFile.toPath(), copyToFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void createReportFolder() {
		if(mCapturePath.contains(File.separator)) {
			mCapturePath = mCapturePath.substring(mCapturePath.lastIndexOf(File.separator) + 1, mCapturePath.length());
		}
				
		mReportFolderName = FileUtils.REPORTS_DIRECTORY + mCapturePath + "_" + Utils.getTimeStamp() + File.separator;
		
		File reportFolder = new File(mReportFolderName);
		final File captureFolder = new File(mReportFolderName + RELATIVE_CAPTURE_FOLDER_PATH_ROOT);
		final String captureFolderAbsolutePath = captureFolder.getAbsolutePath() + File.separator;
		
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
		
		copyCapturesToCaptureFolder(captureFolderAbsolutePath);
	}
	
	private int getVideoExtractCount(final HashMap<String, Integer> extractCountForVideoMap, final ReportCaptureBundle reportCaptureBundle, final BlobContextEntry blobContextEntry) {
		int videoExtractCount = -1;
		if(blobContextEntry.shortExtractFrameIndex.contains("http") == true) {
			extractCountForVideoMap.put(blobContextEntry.videoFilename, -1);
		} else if (extractCountForVideoMap.containsKey(blobContextEntry.videoFilename) == false) {
			videoExtractCount = new File(blobContextEntry.shortExtractFrameIndex).list().length;

			extractCountForVideoMap.put(blobContextEntry.videoFilename, videoExtractCount);
		} else {
			videoExtractCount = extractCountForVideoMap.get(blobContextEntry.videoFilename);
		}

		return videoExtractCount;
	}
	
	private void addCaptureRowChartEvent(final ReportCaptureBundle reportCaptureBundle) {
		//
		final String captureEventDate = reportCaptureBundle.getCaptureEventDate();
		
		if(mHasStatisticsPage == true) {
			mStatisticsPageGenerator.addEvent(captureEventDate, new File(reportCaptureBundle.getCaptureAbsolutePath()).length());
		}
		
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
	
	private void addCaptureRow(final ReportCaptureBundle reportCaptureBundle) {
		mBlobContextEntry = mBlobContextListParser.getEntryByFilename(reportCaptureBundle.getShortExtractFilenameLowerCase());
		
		mBuilder.append("<tr class=\"outer\"><td class=\"text-left\">");
		mBuilder.append(mCurrentRowIndex);
		mBuilder.append("</td>");

		mBuilder.append("<td class=\"text-left\"><a href=\"file:///");
		mBuilder.append(reportCaptureBundle.getCaptureAbsolutePath());
		mBuilder.append("\">");
		mBuilder.append(reportCaptureBundle.getCaptureAbsolutePath());
		mBuilder.append("</a><br><p>");

		mCurrentRowIndex++;

		mBuilder.append("<table><tr><td>From video: </td><td>");
		mBuilder.append(mBlobContextEntry.videoFilename);
		mBuilder.append("</td></tr>");

		int videoExtractCount = getVideoExtractCount(mExtractCountForVideoMap, reportCaptureBundle, mBlobContextEntry);

		if (videoExtractCount > 0) {
			mBuilder.append("<tr><td>Seconds into video: </td><td>");
			mBuilder.append(reportCaptureBundle.getSecondsIntoVideo());

			mBuilder.append("</td></tr><tr><td>Video time (rounded): </td><td>");
			mBuilder.append(LocalTime.MIN.plusSeconds(reportCaptureBundle.getSecondsIntoVideoRounded()));
			mBuilder.append("</td></tr>");
		}
		
		mBuilder.append("</td></tr><tr><td>Event time (rounded): </td><td>");
		mBuilder.append(reportCaptureBundle.getCaptureEventDate().replaceFirst(Pattern.quote("-"), "/")
				.replaceFirst(Pattern.quote("-"), "/").replace("-", ":"));
		mBuilder.append("</td></tr>");
		//
		
		mBuilder.append("</table></p></td><td class=\"text-left\" width=\"300px\" height=\"300px\"><a href=\"file:///");
		mBuilder.append(reportCaptureBundle.getCaptureAbsolutePath());
		mBuilder.append("\"><img src=\"file:///");
		mBuilder.append(reportCaptureBundle.getCaptureAbsolutePath());
		mBuilder.append("\" width=\"300px\" height=\"300px\"/></a></td></tr>");

		mBlobContextEntry = null;
		
		mProgressBundle.progressBar.setValue(mProgressBundle.progressBar.getValue() + 1);
		mProgressBundle.progressBar.repaint();
	}

	private void updateReportFilenameForPage() {
		if(mPage > 0) {
			mReportFilename = mPagesFolderName + mCapturePath + "_" + (mPage + 1) + "_report.html";
		} else {
			mReportFilename = mReportFolderName + mCapturePath + "_" + (mPage + 1) + "_report.html";
		}
	}
	
	private String getReportFilenameForPage(final int forPage) {
		if(forPage == 0) {
			return mReportFilenameRoot;
		}
		else {
			return mPagesFolderName + mCapturePath + "_" + (forPage + 1) + "_report.html";
		}
	}
	
	private void writeReport(boolean withNavigationFooter) throws UnsupportedEncodingException, FileNotFoundException, IOException {		
		if (mPage == 0) {
			if(REPORT_PAGINATION_ENABLED == true) {
				mBuilder.append("<br><div class=\"table-title\"><table align=\"center\"><tr>");
				
				int itemsPerRow = 0;
				
				for(int i = 0; i < (int) Math.ceil((double) mOriginalKeypointCount / DEFAULT_REPORT_PAGE_COUNT); ++i) {
					mBuilder.append("<td><a href=\"");
					mBuilder.append(getReportFilenameForPage(i));
					mBuilder.append("\">");
					mBuilder.append((i + 1));
					mBuilder.append("</a></td>");
					
					itemsPerRow++;
					
					if(itemsPerRow == DEFAULT_PAGE_ITEMS_PER_ROW) {
						itemsPerRow = 0;
						
						mBuilder.append("</tr><tr>");
					}
				}
				mBuilder.append("</tr></table></div>");
			}
			
			try (Writer writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(mReportFilename), "utf-8"))) {
				writer.write(mBuilder.toString());
			}
		} else {
			updateReportFilenameForPage();

			if(REPORT_PAGINATION_ENABLED == true) {
				mBuilder.append("<br><div class=\"table-title\"><table align=\"center\"><tr>");
				
				int itemsPerRow = 0;
				
				for(int i = 0; i < (int) Math.ceil((double) mOriginalKeypointCount / DEFAULT_REPORT_PAGE_COUNT); ++i) {
					mBuilder.append("<td><a href=\"");
					mBuilder.append(getReportFilenameForPage(i));
					mBuilder.append("\">");
					mBuilder.append((i + 1));
					mBuilder.append("</a></td>");
					
					itemsPerRow++;
					
					if(itemsPerRow == DEFAULT_PAGE_ITEMS_PER_ROW) {
						itemsPerRow = 0;
						
						mBuilder.append("</tr><tr>");
					}
				}
				mBuilder.append("</tr></table></div>");
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
		
		final int staticsPageInsertionIndex = mBuilder.length();
		
		mReportPageHeader = mBuilder.toString();
		mReportPageHeader = mReportPageHeader.replaceFirst("favicon.png", "../favicon.png");
		mReportPageHeader = mReportPageHeader.replaceFirst("default_font.css", "../default_font.css");
		mReportPageHeader = mReportPageHeader.replaceFirst("report_icon.png", "../report_icon.png");
		REPORT_CHART_SETTINGS.initializeChartBuilders(mReportFilenameRoot, mReportPageHeader);
		//
		
		//
		if(mHasStatisticsPage == true) {
			if (REPORT_PAGINATION_ENABLED == true) {
				createStatisticsFolder();
				mStatisticsPageGenerator.setStatisticsFolderPath(mReportFolderName + RELATIVE_STATISTICS_FOLDER_PATH_ROOT + File.separator);
							
				mBuilder.append("<table class=\"table-fill\"><thead><tr><th class=\"text-left\">Overview</th></tr></thead><tbody class=\"table-hover\">");
				mBuilder.append("<tr class=\"outer\"><td class=\"text-left\"><a href=\"file:///");
				mBuilder.append(mStatisticsPageGenerator.getStatisticsPageName());
				mBuilder.append("\">Statistics</a></td></tr></tbody></table><br>");
			}
		}
		//
		
		//
		NotePageGenerator mNotesPageGenerator = new NotePageGenerator(mCaseName);
		if(mNotesPageGenerator.hasNotes() == true) {//mHasNotesPage == true) {
			//NotePageGenerator mNotesPageGenerator = new NotePageGenerator(mCaseName);
			mNotesPageGenerator.setReportRoot(mReportFilenameRoot);
			mNotesPageGenerator.setReportPageHeader(mReportPageHeader);
			
			if (REPORT_PAGINATION_ENABLED == true) {
				createNotesFolder();
				mNotesPageGenerator.setNotesFolderPath(mReportFolderName + RELATIVE_NOTES_FOLDER_PATH_ROOT + File.separator);
				mNotesPageGenerator.buildPage();
				
				mBuilder.append("<table class=\"table-fill\"><thead><tr><th class=\"text-left\">Case Work</th></tr></thead><tbody class=\"table-hover\">");
				mBuilder.append("<tr class=\"outer\"><td class=\"text-left\"><a href=\"file:///");
				mBuilder.append(mNotesPageGenerator.getNotesPageName());
				mBuilder.append("\">Notes</a></td></tr></tbody></table><br>");
			} else {
				mNotesPageGenerator.buildEmbeddablePage();
				
				mBuilder.append(mNotesPageGenerator.getEmbeddablePage());
			}
		}
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
		if(mHasEvidenceListing == true) {
			EvidenceHtmlGenerator evidenceHtmlGenerator = new EvidenceHtmlGenerator(mBlobContextListParser, mCaptures, mBuilder);
			evidenceHtmlGenerator.create();
		}
		//
		
		//
		if(mHasEnhancedEvidenceListing == true) {
			EnhancedEvidenceHtmlGenerator enhancedEvidenceHtmlGenerator = new EnhancedEvidenceHtmlGenerator(mBuilder, CONTEXT_FILENAME);
			enhancedEvidenceHtmlGenerator.create();
		}
		//
		
		//
		if(mHasRedactedEvidenceListing == true) {
			RedactedEvidenceHtmlGenerator redactedEvidenceHtmlGenerator = new RedactedEvidenceHtmlGenerator(mBuilder, CONTEXT_FILENAME);
			redactedEvidenceHtmlGenerator.create();
		}
		//
		
		//
		if(mHasMergedListing == true) {
			MergedEvidenceHtmlGenerator mergedEvidenceHtmlGenerator = new MergedEvidenceHtmlGenerator(mBuilder, CONTEXT_FILENAME);
			mergedEvidenceHtmlGenerator.create();
		}
		//
		
		//
		if(mHasVideoMetadataPage == true) {
			MetadataHtmlGenerator metadataHtmlGenerator = new MetadataHtmlGenerator(mBuilder, REPORT_PAGINATION_ENABLED, mReportFolderName,
				mBlobContextListParser, mReportPageHeader, mReportFilenameRoot, mCaptures);
			metadataHtmlGenerator.create();
		}
		//
		
		//
		if(mHasReportExtractsPage == true) {
			ExtractHtmlGenerator extractHtmlGenerator = new ExtractHtmlGenerator(mBuilder, REPORT_PAGINATION_ENABLED, 
				mReportFolderName, mReportPageHeader, mReportFilenameRoot,
				getFormattedCaseName());
			extractHtmlGenerator.create();
		}
		//
		
		//
		if(checkCaseHasFaces() == true || checkCaseHasLicensePlates() == true 
			|| checkCaseHasPedestrians() == true || checkCaseHasCars() == true || checkCaseHasExplicitImages() == true) {
			mBuilder.append("<table class=\"table-fill\"><thead><tr><th class=\"text-left\">Detection</th></tr></thead><tbody class=\"table-hover\">");
		}
		//
	
		//
		if(mHasCarDetectionPage == true && checkCaseHasCars() == true) {
			CarHtmlGenerator carHtmlGenerator = new CarHtmlGenerator(mBuilder, REPORT_PAGINATION_ENABLED,
				mReportFolderName, mBlobContextListParser, mReportPageHeader,
				mReportFilenameRoot, mHasCharts, getFormattedCaseName(),
				REPORT_CHART_SETTINGS, mHasStatisticsPage, 
				mStatisticsPageGenerator);
			carHtmlGenerator.create();
		}
		//
		
		//
		if(mHasPedestrianDetectionPage == true && checkCaseHasPedestrians() == true) {
			PedestrianHtmlGenerator pedestrianHtmlGenerator = new PedestrianHtmlGenerator(mBuilder, REPORT_PAGINATION_ENABLED,
				mReportFolderName, mBlobContextListParser, mReportPageHeader,
				mReportFilenameRoot, mHasCharts, getFormattedCaseName(),
				REPORT_CHART_SETTINGS, mHasStatisticsPage, 
				mStatisticsPageGenerator);
			pedestrianHtmlGenerator.create();
		}
		//
		
		//
		if(mHasFaceDetectionPage == true && checkCaseHasFaces() == true) {
			FaceHtmlGenerator faceHtmlGenerator = new FaceHtmlGenerator(mBuilder, REPORT_PAGINATION_ENABLED,
					mReportFolderName, mBlobContextListParser, mReportPageHeader,
					mReportFilenameRoot, mHasCharts, getFormattedCaseName(),
					REPORT_CHART_SETTINGS, mHasStatisticsPage, 
					mStatisticsPageGenerator);
			faceHtmlGenerator.create();
		}
		//
	
		//
		if(mHasLicensePlateDetectionPage == true && checkCaseHasLicensePlates() == true) {
			LicensePlateHtmlGenerator licensePlateHtmlGenerator = new LicensePlateHtmlGenerator(mBuilder, REPORT_PAGINATION_ENABLED,
					mReportFolderName, mBlobContextListParser, mReportPageHeader,
					mReportFilenameRoot, mHasCharts, getFormattedCaseName(),
					REPORT_CHART_SETTINGS, mHasStatisticsPage, 
					mStatisticsPageGenerator);
			licensePlateHtmlGenerator.create();
		}
		//
		
		//
		if(mHasExplicitDetectionPage == true && checkCaseHasExplicitImages() == true) {
			ExplicitHtmlGenerator explicitHtmlGenerator = new ExplicitHtmlGenerator(mBuilder, REPORT_PAGINATION_ENABLED,
				mReportFolderName, mBlobContextListParser, mReportPageHeader,
				mReportFilenameRoot, mHasCharts, getFormattedCaseName(),
				REPORT_CHART_SETTINGS, mHasStatisticsPage, 
				mStatisticsPageGenerator);
			explicitHtmlGenerator.create();
		}
		//
		
		//
		if(checkCaseHasFaces() == true || checkCaseHasLicensePlates() == true 
				|| checkCaseHasPedestrians() == true || checkCaseHasCars() == true || checkCaseHasExplicitImages() == true) {
			mBuilder.append("</tbody></table><br>");
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
		
		mBuilder.append("<table class=\"table-fill\"><thead><tr><th class=\"text-left\">ID</th><th class=\"text-left\">Key Point</th><th class=\"text-left\">Capture</th></tr></thead><tbody class=\"table-hover\">");

		int rowsWritten = 0;

		mProgressBundle.progressBar.setIndeterminate(false);
		mProgressBundle.progressBar.setValue(0);
		mProgressBundle.progressBar.setMaximum(mCaptures.size());
		mProgressBundle.progressBar.repaint();
		
		while (mCaptures.isEmpty() == false) {
			addCaptureRow(mCaptures.get(0));
			mCaptures.remove(0);

			if (REPORT_PAGINATION_ENABLED == true) {
				rowsWritten++;
				
				if(rowsWritten == DEFAULT_REPORT_PAGE_COUNT) {
					rowsWritten = 0;
	
					mBuilder.append("</tbody></table></body></html>");
	
					try {
						writeReport(true);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
	
					mPage++;
					createNextPage();
				}
			}
		}

		mProgressBundle.progressBar.setIndeterminate(true);
		mProgressBundle.progressBar.repaint();
		
		if(mHasStatisticsPage == true) {
			for(Entry<String, Integer> extractCountEntry : mExtractCountForVideoMap.entrySet()) {
				mStatisticsPageGenerator.addFrameCount(extractCountEntry.getValue());
				mStatisticsPageGenerator.addVideoForSecondsElapsed(extractCountEntry.getKey());
				mStatisticsPageGenerator.incrementVideoCount();
			}
			
			if(REPORT_PAGINATION_ENABLED == true) {
				mStatisticsPageGenerator.setReportPageHeader(mReportPageHeader);
				mStatisticsPageGenerator.setReportRoot(mReportFilenameRoot);
				mStatisticsPageGenerator.buildPage();
			} else {
				mStatisticsPageGenerator.buildEmbeddablePage();
				
				mBuilder.insert(staticsPageInsertionIndex, mStatisticsPageGenerator.getEmbeddablePage());
			}
		}
		
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
		
		mProgressBundle.progressBar.setValue(mProgressBundle.progressBar.getValue() + 1);
		mProgressBundle.progressBar.repaint();
		mProgressBundle.frame.dispose();
		
		return mReportFilenameRoot;
	}

	private void createNextPage() {
		mBuilder.setLength(0);
		mBuilder.append(mReportPageHeader);
		mBuilder.append("<table class=\"table-fill\"><thead><tr><th class=\"text-left\">ID</th><th class=\"text-left\">Key Point</th><th class=\"text-left\">Capture</th></tr></thead><tbody class=\"table-hover\">");

		int rowsWritten = 0;

		while (mCaptures.isEmpty() == false) {
			addCaptureRow(mCaptures.get(0));
			mCaptures.remove(0);
			
			if (REPORT_PAGINATION_ENABLED == true) {
				rowsWritten++;
				
				if(rowsWritten == DEFAULT_REPORT_PAGE_COUNT) {
					rowsWritten = 0;
	
					mBuilder.append("</tbody></table></body></html>");
	
					try {
						writeReport(true);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
	
					mPage++;
					createNextPage();
				}
			}
		}

		if (mBuilder.length() > 0) {
			mBuilder.append("</tbody></table></body></html>");

			try {
				writeReport(true);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		mBuilder.setLength(0);
	}
}

package com.keypointforensics.videotriage.report.html;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import com.keypointforensics.videotriage.blob.context.BlobContextListParser;
import com.keypointforensics.videotriage.chart.DetectionCompositionPieChartBuilder;
import com.keypointforensics.videotriage.chart.EVideoTriageChart;
import com.keypointforensics.videotriage.chart.ReportChartSettings;
import com.keypointforensics.videotriage.chart.TimeSeriesExplicitDetectionChartBuilder;
import com.keypointforensics.videotriage.detect.EVideoTriageDetectionModule;
import com.keypointforensics.videotriage.legacy.FileUtilsLegacy;
import com.keypointforensics.videotriage.list.SortedList;
import com.keypointforensics.videotriage.report.ExplicitExtractPageGenerator;
import com.keypointforensics.videotriage.report.ReportCaptureBundle;
import com.keypointforensics.videotriage.report.stats.StatisticsPageGenerator;
import com.keypointforensics.videotriage.util.FileUtils;

public class ExplicitHtmlGenerator {

	private final String RELATIVE_EXPLICIT_FOLDER_PATH_ROOT = "explicit";

	private final StringBuilder PAGE_BUILDER;
	private final boolean REPORT_PAGINATION_ENABLED;
	private final String REPORT_FOLDER_NAME;
	private final BlobContextListParser BLOB_CONTEXT_LIST_PARSER;
	private final String REPORT_PAGE_HEADER;
	private final String REPORT_FILENAME_ROOT;
	private final boolean HAS_CHARTS;
	private final String FORMATTED_CASE_NAME;
	private final ReportChartSettings REPORT_CHART_SETTINGS;
	private final boolean HAS_STATISTICS_PAGE;
	private final StatisticsPageGenerator STATISTICS_PAGE_GENERATOR;

	public ExplicitHtmlGenerator(final StringBuilder pageBuilder, final boolean reportPaginationEnabled,
			final String reportFolderName, final BlobContextListParser blobContextListParser, final String reportPageHeader,
			final String reportFilenameRoot, final boolean hasCharts, final String formattedCaseName,
			final ReportChartSettings reportChartSettings, final boolean hasStatisticsPage, 
			final StatisticsPageGenerator statisticsPageGenerator) {
		PAGE_BUILDER = pageBuilder;
		REPORT_PAGINATION_ENABLED = reportPaginationEnabled;
		REPORT_FOLDER_NAME = reportFolderName;
		BLOB_CONTEXT_LIST_PARSER = blobContextListParser;
		REPORT_PAGE_HEADER = reportPageHeader;
		REPORT_FILENAME_ROOT = reportFilenameRoot;
		HAS_CHARTS = hasCharts;
		FORMATTED_CASE_NAME = formattedCaseName;
		REPORT_CHART_SETTINGS = reportChartSettings;
		HAS_STATISTICS_PAGE = hasStatisticsPage;
		STATISTICS_PAGE_GENERATOR = statisticsPageGenerator;
	}
	
	private void createExplicitExtractFolder() {
		File explicitFolder = new File(REPORT_FOLDER_NAME + RELATIVE_EXPLICIT_FOLDER_PATH_ROOT);
		explicitFolder.mkdir();
	}
	
	private void copyExplicitImagesToExplicitFolder() {
		File explicitExtractFolderOriginal = new File(FileUtils.EXPLICIT_DIRECTORY + FORMATTED_CASE_NAME);
		ArrayList<String> explicitExtracts = (ArrayList<String>) FileUtilsLegacy.parseDirectoryRecursiveForAll(explicitExtractFolderOriginal.getAbsolutePath());

		final String rootExtractPath = REPORT_FOLDER_NAME + RELATIVE_EXPLICIT_FOLDER_PATH_ROOT + File.separator;

		File originalFile, copyToFile;
		
		for(String explicitExtract : explicitExtracts) {
			originalFile = new File(explicitExtract);
			copyToFile = new File(rootExtractPath + FileUtils.getShortFilename(explicitExtract));
			
			try {
				Files.copy(originalFile.toPath(), copyToFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void create() {
		createExplicitExtractFolder();
		copyExplicitImagesToExplicitFolder();
		
		ExplicitExtractPageGenerator explicitExtractPageGenerator = new ExplicitExtractPageGenerator(BLOB_CONTEXT_LIST_PARSER, REPORT_FOLDER_NAME + RELATIVE_EXPLICIT_FOLDER_PATH_ROOT + File.separator,
				REPORT_FILENAME_ROOT, REPORT_PAGE_HEADER);
		
		if(REPORT_PAGINATION_ENABLED == true) {
			explicitExtractPageGenerator.buildDetectionPage();
			
			//PAGE_BUILDER.append("<table class=\"table-fill\"><thead><tr><th class=\"text-left\">Detection</th></tr></thead><tbody class=\"table-hover\">");
			PAGE_BUILDER.append("<tr class=\"outer\"><td class=\"text-left\"><a href=\"");
			PAGE_BUILDER.append(RELATIVE_EXPLICIT_FOLDER_PATH_ROOT + File.separator + explicitExtractPageGenerator.getDetectionPageNameShort());
			PAGE_BUILDER.append("\">");
			PAGE_BUILDER.append("View Explicit Images");
			PAGE_BUILDER.append("</a></td></tr>");
			//PAGE_BUILDER.append("</tbody></table><br>");
		} else {
			PAGE_BUILDER.append(explicitExtractPageGenerator.getFlatPageTableData());
		}

		ArrayList<String> explicitDetectionList = explicitExtractPageGenerator.getDetectionEventList();
		
		if(HAS_STATISTICS_PAGE == true) {
			STATISTICS_PAGE_GENERATOR.setExplicitDetectionCount(explicitDetectionList.size());
		}
		
		if(HAS_CHARTS == true) {
			if(REPORT_CHART_SETTINGS.containsChartBuilder(EVideoTriageChart.TIME_SERIES_EXPLICIT) == true) {			
				TimeSeriesExplicitDetectionChartBuilder timeSeriesExplicitDetectionChartBuilder = (TimeSeriesExplicitDetectionChartBuilder) REPORT_CHART_SETTINGS.getChartBuilder(EVideoTriageChart.TIME_SERIES_EXPLICIT);
				timeSeriesExplicitDetectionChartBuilder.addEvents(explicitDetectionList);
			}
			
			if(REPORT_CHART_SETTINGS.containsChartBuilder(EVideoTriageChart.DETECTION_COMPOSITION_PIE_STANDARD) == true) {
				DetectionCompositionPieChartBuilder detectionCompositionPieChartBuilder = (DetectionCompositionPieChartBuilder) REPORT_CHART_SETTINGS.getChartBuilder(EVideoTriageChart.DETECTION_COMPOSITION_PIE_STANDARD);
				detectionCompositionPieChartBuilder.addEvents(EVideoTriageDetectionModule.DETECTION_MODULE_EXPLICIT, explicitDetectionList);
			}
		}
	}
}

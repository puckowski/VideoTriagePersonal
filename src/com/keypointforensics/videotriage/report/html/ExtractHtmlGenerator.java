package com.keypointforensics.videotriage.report.html;

import java.io.File;

import com.keypointforensics.videotriage.blob.context.BlobContextListParser;
import com.keypointforensics.videotriage.chart.ReportChartSettings;
import com.keypointforensics.videotriage.report.NoteworthyFramePageGenerator;
import com.keypointforensics.videotriage.report.stats.StatisticsPageGenerator;

public class ExtractHtmlGenerator {

	private final String RELATIVE_REPORT_EXTRACTS_FOLDER_PATH_ROOT = "extracts";

	private final StringBuilder PAGE_BUILDER;
	private final boolean REPORT_PAGINATION_ENABLED;
	private final String REPORT_FOLDER_NAME;
	private final String REPORT_PAGE_HEADER;
	private final String REPORT_FILENAME_ROOT;
	private final String FORMATTED_CASE_NAME;
	
	public ExtractHtmlGenerator(final StringBuilder pageBuilder, final boolean reportPaginationEnabled, 
			final String reportFolderName, final String reportPageHeader, final String reportFilenameRoot,
			final String formattedCaseName) {
		PAGE_BUILDER = pageBuilder;
		REPORT_PAGINATION_ENABLED = reportPaginationEnabled;
		REPORT_FOLDER_NAME = reportFolderName;
		REPORT_PAGE_HEADER = reportPageHeader;
		REPORT_FILENAME_ROOT = reportFilenameRoot;
		FORMATTED_CASE_NAME = formattedCaseName;
	}
	
	private void createReportExtractFolder() {
		File reportExtractsFolder = new File(REPORT_FOLDER_NAME + RELATIVE_REPORT_EXTRACTS_FOLDER_PATH_ROOT);
		reportExtractsFolder.mkdir();
		
		/*
		try {
			Files.copy(Paths.get(FileUtils.GRAPHICS_DIRECTORY + "favicon.png"),
					Paths.get(mReportFolderName + RELATIVE_VIDEO_METADATA_FOLDER_PATH_ROOT + File.separator + "favicon.png"), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
	}
	
	public void create() {
		createReportExtractFolder();
		
		NoteworthyFramePageGenerator noteworthyFramePageGenerator = new NoteworthyFramePageGenerator(REPORT_FOLDER_NAME + RELATIVE_REPORT_EXTRACTS_FOLDER_PATH_ROOT + File.separator,
			FORMATTED_CASE_NAME, REPORT_FILENAME_ROOT, REPORT_PAGE_HEADER);
		
		if (REPORT_PAGINATION_ENABLED == true) {
			PAGE_BUILDER.append("<table class=\"table-fill\"><thead><tr><th class=\"text-left\">Frame Extracts</th></tr></thead><tbody class=\"table-hover\">");
			PAGE_BUILDER.append("<tr class=\"outer\"><td class=\"text-left\"><a href=\"file:///");
			PAGE_BUILDER.append(noteworthyFramePageGenerator.getFramePageName());
			PAGE_BUILDER.append("\">View Noteworthy Frames</a></td></tr></tbody></table><br>");
			
			noteworthyFramePageGenerator.buildFramePage();
		} else {
			PAGE_BUILDER.append(noteworthyFramePageGenerator.getFlatPageTableData());
		}
	}
}

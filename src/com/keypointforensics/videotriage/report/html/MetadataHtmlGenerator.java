package com.keypointforensics.videotriage.report.html;

import java.io.File;
import java.util.HashSet;

import com.keypointforensics.videotriage.blob.context.BlobContextEntry;
import com.keypointforensics.videotriage.blob.context.BlobContextListParser;
import com.keypointforensics.videotriage.list.SortedList;
import com.keypointforensics.videotriage.report.ReportCaptureBundle;
import com.keypointforensics.videotriage.report.VideoMetadataPageGenerator;

public class MetadataHtmlGenerator {

	private final String RELATIVE_VIDEO_METADATA_FOLDER_PATH_ROOT = "metadata";

	private final StringBuilder PAGE_BUILDER;
	private final boolean REPORT_PAGINATION_ENABLED;
	private final String REPORT_FOLDER_NAME;
	private final BlobContextListParser BLOB_CONTEXT_LIST_PARSER;
	private final String REPORT_PAGE_HEADER;
	private final String REPORT_FILENAME_ROOT;
	private final SortedList<ReportCaptureBundle> CAPTURES;
	
	public MetadataHtmlGenerator(final StringBuilder pageBuilder, final boolean reportPaginationEnabled,
		final String reportFolderName, final BlobContextListParser blobContextListParser, final String reportPageHeader,
		final String reportFilenameRoot, final SortedList<ReportCaptureBundle> captures) {
		PAGE_BUILDER = pageBuilder;
		REPORT_PAGINATION_ENABLED = reportPaginationEnabled;
		REPORT_FOLDER_NAME = reportFolderName;
		BLOB_CONTEXT_LIST_PARSER = blobContextListParser;
		REPORT_PAGE_HEADER = reportPageHeader;
		REPORT_FILENAME_ROOT = reportFilenameRoot;
		CAPTURES = captures;
	}
	
	/**/
	private void createVideoMetadataFolder() {
		File metadataFolder = new File(REPORT_FOLDER_NAME + RELATIVE_VIDEO_METADATA_FOLDER_PATH_ROOT);
		metadataFolder.mkdir();
		
		/*
		try {
			Files.copy(Paths.get(FileUtils.GRAPHICS_DIRECTORY + "favicon.png"),
					Paths.get(mReportFolderName + RELATIVE_VIDEO_METADATA_FOLDER_PATH_ROOT + File.separator + "favicon.png"), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
	}
	/**/
	
	public void create() {
		if (REPORT_PAGINATION_ENABLED == true) {
			createVideoMetadataFolder();
			
			HashSet<String> metadataAddedSet = new HashSet<String>();
			VideoMetadataPageGenerator videoMetadataPageGenerator = new VideoMetadataPageGenerator(REPORT_FOLDER_NAME + RELATIVE_VIDEO_METADATA_FOLDER_PATH_ROOT + File.separator,
				REPORT_FILENAME_ROOT, REPORT_PAGE_HEADER);
				
			ReportCaptureBundle reportCaptureBundle = null;
			BlobContextEntry blobContextEntry = null;
			String videoFilename = null;
				
			//PAGE_BUILDER.append("<table class=\"table-fill\"><thead><tr><th class=\"text-left\">Video Details</th></tr></thead><tbody class=\"table-hover\">");
				
			for(int i = 0; i < CAPTURES.size(); ++i) {
				reportCaptureBundle = CAPTURES.get(i);
				blobContextEntry = BLOB_CONTEXT_LIST_PARSER.getEntryByFilename(reportCaptureBundle.getShortExtractFilenameLowerCase());
				videoFilename = blobContextEntry.videoFilename;
					
				if(metadataAddedSet.contains(videoFilename) == false) {
					metadataAddedSet.add(videoFilename);
					videoMetadataPageGenerator.addAbsoluteVideoFilename(videoFilename);
						
					//PAGE_BUILDER.append("<tr class=\"outer\"><td class=\"text-left\"><a href=\"");
					//PAGE_BUILDER.append(RELATIVE_VIDEO_METADATA_FOLDER_PATH_ROOT + File.separator + videoMetadataPageGenerator.getMetadataPageNameForAbsoluteFilename(videoFilename));
					//PAGE_BUILDER.append("\">");
					//PAGE_BUILDER.append(videoFilename);
					//PAGE_BUILDER.append("</a></td></tr>");
				}
			}
				
			//PAGE_BUILDER.append("</tbody></table><br>");
				
			videoMetadataPageGenerator.buildMetadataPage();
				
			PAGE_BUILDER.append("<table class=\"table-fill\"><thead><tr><th class=\"text-left\">Metadata</th></tr></thead><tbody class=\"table-hover\">");
			PAGE_BUILDER.append("<tr class=\"outer\"><td class=\"text-left\"><a href=\"file:///");
			PAGE_BUILDER.append(videoMetadataPageGenerator.getMetadataPageName());
			PAGE_BUILDER.append("\">View Metadata</a></td></tr></tbody></table><br>");
		} else {				
			HashSet<String> metadataAddedSet = new HashSet<String>();
			VideoMetadataPageGenerator videoMetadataPageGenerator = new VideoMetadataPageGenerator(REPORT_FOLDER_NAME + RELATIVE_VIDEO_METADATA_FOLDER_PATH_ROOT + File.separator,
				REPORT_FILENAME_ROOT, REPORT_PAGE_HEADER);
				
			ReportCaptureBundle reportCaptureBundle = null;
			BlobContextEntry blobContextEntry = null;
			String videoFilename = null;
				
			//PAGE_BUILDER.append("<table class=\"table-fill\"><thead><tr><th class=\"text-left\">Video Details</th></tr></thead><tbody class=\"table-hover\">");
				
			for(int i = 0; i < CAPTURES.size(); ++i) {
				reportCaptureBundle = CAPTURES.get(i);
				blobContextEntry = BLOB_CONTEXT_LIST_PARSER.getEntryByFilename(reportCaptureBundle.getShortExtractFilenameLowerCase());
				videoFilename = blobContextEntry.videoFilename;
					
				if(metadataAddedSet.contains(videoFilename) == false) {
					metadataAddedSet.add(videoFilename);
					videoMetadataPageGenerator.addAbsoluteVideoFilename(videoFilename);
						
					//PAGE_BUILDER.append("<tr class=\"outer\"><td class=\"text-left\"><a href=\"");
					//PAGE_BUILDER.append(RELATIVE_VIDEO_METADATA_FOLDER_PATH_ROOT + File.separator + videoMetadataPageGenerator.getMetadataPageNameForAbsoluteFilename(videoFilename));
					//PAGE_BUILDER.append("\">");
					//PAGE_BUILDER.append(videoFilename);
					//PAGE_BUILDER.append("</a></td></tr>");
				}
			}
				
			//PAGE_BUILDER.append("</tbody></table><br>");
				
			PAGE_BUILDER.append(videoMetadataPageGenerator.getEmbeddableMetadataPage());
		}
	}
}

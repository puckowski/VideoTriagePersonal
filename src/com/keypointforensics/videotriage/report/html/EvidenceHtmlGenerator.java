package com.keypointforensics.videotriage.report.html;

import java.util.HashSet;

import com.keypointforensics.videotriage.blob.context.BlobContextEntry;
import com.keypointforensics.videotriage.blob.context.BlobContextListParser;
import com.keypointforensics.videotriage.list.SortedList;
import com.keypointforensics.videotriage.report.ReportCaptureBundle;

public class EvidenceHtmlGenerator {

	private final BlobContextListParser BLOB_CONTEXT_LIST_PARSER;
	private final SortedList<ReportCaptureBundle> CAPTURES;
	private final StringBuilder PAGE_BUILDER;
	
	public EvidenceHtmlGenerator(final BlobContextListParser blobContextListParser, final SortedList<ReportCaptureBundle> captures,
		final StringBuilder pageBuilder) {
		BLOB_CONTEXT_LIST_PARSER = blobContextListParser;
		CAPTURES = captures;
		PAGE_BUILDER = pageBuilder;
	}
	
	public void create() {
		PAGE_BUILDER.append("<table class=\"table-fill\"><thead><tr><th class=\"text-left\">Evidence</th></tr></thead><tbody class=\"table-hover\">");

		HashSet<String> sourceAddedSet = new HashSet<String>();
		
		ReportCaptureBundle reportCaptureBundle = null;
		BlobContextEntry blobContextEntry = null;
		String videoFilename = null;
		
		for(int i = 0; i < CAPTURES.size(); ++i) {
			reportCaptureBundle = CAPTURES.get(i);
			blobContextEntry = BLOB_CONTEXT_LIST_PARSER.getEntryByFilename(reportCaptureBundle.getShortExtractFilenameLowerCase());
			videoFilename = blobContextEntry.videoFilename;
			
			if(sourceAddedSet.contains(videoFilename) == false) {
				sourceAddedSet.add(videoFilename);
				
				PAGE_BUILDER.append("<tr class=\"outer\"><td class=\"text-left\"><a href=\"");
				
				if(videoFilename.contains("http") == false) {
					PAGE_BUILDER.append("file:///");
					PAGE_BUILDER.append(videoFilename);
				} else {
					PAGE_BUILDER.append(videoFilename);
				}
				
				PAGE_BUILDER.append("\">");
				PAGE_BUILDER.append(videoFilename);
				PAGE_BUILDER.append("</a></td></tr>");
			}
		}

		PAGE_BUILDER.append("</tbody></table><br>");
	}
}

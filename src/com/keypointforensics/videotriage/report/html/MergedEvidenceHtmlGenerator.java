package com.keypointforensics.videotriage.report.html;

import java.util.ArrayList;

import com.keypointforensics.videotriage.casestore.CaseMetadataWriter;

public class MergedEvidenceHtmlGenerator {

	private final StringBuilder PAGE_BUILDER;
	private final String CONTEXT_FILENAME;
		
	public MergedEvidenceHtmlGenerator(final StringBuilder pageBuilder, final String contextFilename) {
		PAGE_BUILDER = pageBuilder;
		CONTEXT_FILENAME = contextFilename;
	}
		
	public void create() {
		ArrayList<String> mergedVideoFiles = CaseMetadataWriter.getMergedVideoSourceListing(CONTEXT_FILENAME);
			
		if(mergedVideoFiles.isEmpty() == false) {
			PAGE_BUILDER.append("<table class=\"table-fill\"><thead><tr><th class=\"text-left\">Merged Evidence</th></tr></thead><tbody class=\"table-hover\">");

			for(int i = 0; i < mergedVideoFiles.size(); ++i) {
				PAGE_BUILDER.append("<tr class=\"outer\"><td class=\"text-left\"><a href=\"");
				PAGE_BUILDER.append("file:///");
				PAGE_BUILDER.append(mergedVideoFiles.get(i));
				PAGE_BUILDER.append("\">");
				PAGE_BUILDER.append(mergedVideoFiles.get(i));
				PAGE_BUILDER.append("</a></td></tr>");
			}
		
			PAGE_BUILDER.append("</tbody></table><br>");
		}
	}
}

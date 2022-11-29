package com.keypointforensics.videotriage.report.html;

import java.util.ArrayList;

import com.keypointforensics.videotriage.casestore.CaseMetadataWriter;

public class EnhancedEvidenceHtmlGenerator {

	private final StringBuilder PAGE_BUILDER;
	private final String CONTEXT_FILENAME;
	
	public EnhancedEvidenceHtmlGenerator(final StringBuilder pageBuilder, final String contextFilename) {
		PAGE_BUILDER = pageBuilder;
		CONTEXT_FILENAME = contextFilename;
	}
	
	public void create() {
		ArrayList<String> enhancedVideoFiles = CaseMetadataWriter.getEnhancedVideoSourceListing(CONTEXT_FILENAME);
		
		if(enhancedVideoFiles.isEmpty() == false) {
			PAGE_BUILDER.append("<table class=\"table-fill\"><thead><tr><th class=\"text-left\">Enhanced Evidence</th></tr></thead><tbody class=\"table-hover\">");

			for(int i = 0; i < enhancedVideoFiles.size(); ++i) {
				PAGE_BUILDER.append("<tr class=\"outer\"><td class=\"text-left\"><a href=\"");
				PAGE_BUILDER.append("file:///");
				PAGE_BUILDER.append(enhancedVideoFiles.get(i));
				PAGE_BUILDER.append("\">");
				PAGE_BUILDER.append(enhancedVideoFiles.get(i));
				PAGE_BUILDER.append("</a></td></tr>");
			}
	
			PAGE_BUILDER.append("</tbody></table><br>");
		}
	}
}

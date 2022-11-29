package com.keypointforensics.videotriage.chart;

public abstract class VideoTriageChartBuilder {

	protected final String REPORT_PAGE_HEADER;
	protected final String REPORT_ROOT;
	
	protected String mJavaScriptFilenameShort;
	
	public VideoTriageChartBuilder(final String reportRoot, String reportPageHeader) {
		reportPageHeader = reportPageHeader.replace("url(default_font.css);", "url(../default_font.css);");
		reportPageHeader = reportPageHeader.replaceAll("href=\"favicon.png\"", "href=\"../favicon.png\"");
		
		REPORT_PAGE_HEADER = reportPageHeader;
		REPORT_ROOT        = reportRoot;
	}
	
	public String getReportRoot() {
		return REPORT_ROOT;
	}
	
	public String getJavaScriptFilenameShort() {
		return mJavaScriptFilenameShort;
	}
	
	public abstract void build(boolean isReportPaginated);
	public abstract String getChart();
	public abstract String getHtmlFile();
	
	public abstract void setJavaScriptFilenameShort();
	
}

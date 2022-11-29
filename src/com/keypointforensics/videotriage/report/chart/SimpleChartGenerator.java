package com.keypointforensics.videotriage.report.chart;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.keypointforensics.videotriage.blob.context.BlobContextList;
import com.keypointforensics.videotriage.casestore.CaseMetadataWriter;
import com.keypointforensics.videotriage.chart.ReportChartSettings;
import com.keypointforensics.videotriage.gui.gallery.BaseImageGallery;
import com.keypointforensics.videotriage.report.ImageGallery;
import com.keypointforensics.videotriage.report.ReportChartPreferencesWindow;
import com.keypointforensics.videotriage.report.html.HtmlReportGenerator;
import com.keypointforensics.videotriage.thread.KeyFrameCheckThread;
import com.keypointforensics.videotriage.util.ReportUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.window.ChildWindowList;
import com.keypointforensics.videotriage.window.WindowRegistry;

public class SimpleChartGenerator extends BaseImageGallery {

	/*
	 * Author: Daniel Puckowski
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 232646536271988938L;
	
	private static final boolean DEFAULT_REPORT_ICON_ENABLED        = true;
	private static final boolean DEFAULT_CUSTOM_REPORT_ICON_ENABLED = false;
	private static final boolean DEFAULT_REPORT_PAGINATION_ENABLED  = true;
	
	private final BlobContextList BLOB_CONTEXT_LIST;
	private final String CASE_NAME;
    
	private boolean mReportIconEnabled;
	private boolean mCustomReportIconEnabled;
	private File    mCustomReportIconFile;
	private boolean mReportPaginationEnabled;
	private boolean mMetadataPageEnabled;
	private boolean mStatisticsPageEnabled;
	
	private ReportChartSettings mReportChartSettings;
	
	private ChildWindowList mChildWindowList;
	
	public SimpleChartGenerator(final String caseName, final String path, final BlobContextList blobContextList) {
		super(path, false);
		
		mChildWindowList = new ChildWindowList();
		
		BLOB_CONTEXT_LIST = blobContextList;
		CASE_NAME = caseName;
		
		mReportIconEnabled       = DEFAULT_REPORT_ICON_ENABLED;
		mCustomReportIconEnabled = DEFAULT_CUSTOM_REPORT_ICON_ENABLED;
		mReportPaginationEnabled = DEFAULT_REPORT_PAGINATION_ENABLED;
		
		String contextFilename = CaseMetadataWriter.getContextFilenameFromDatabaseName(caseName);
		ArrayList<String> enhancedVideoFiles = CaseMetadataWriter.getVideoSourceListing(contextFilename);
		
		mReportChartSettings = new ReportChartSettings(enhancedVideoFiles);
	}
	
	@Override
	public void build() {
		new Thread() {
			@Override
			public void run() {
				ThreadUtils.addThreadToHandleList("SimpleChartGenerator ChartSettings", this);
				
				SimpleReportChartPreferencesWindow reportChartPreferencesWindow = new SimpleReportChartPreferencesWindow(SimpleChartGenerator.this);
				//mChildWindowList.addWindow(reportChartPreferencesWindow);
				
				ThreadUtils.removeThreadFromHandleList(this);
			}
		}.start();
	}
	
	public BlobContextList getBlobContextList() {
		return BLOB_CONTEXT_LIST;
	}
	
	public String getCapturePath() {
		return path;
	}
	
	public boolean getStatisticsPageEnabled() {
		return mStatisticsPageEnabled;
	}
	
	public void setStatisticsPageEnabled(final boolean newStatisticsPageEnabled) {
		mStatisticsPageEnabled = newStatisticsPageEnabled;
	}
	
	public boolean getMetadataPageEnabled() {
		return mMetadataPageEnabled;
	}
	
	public void setMetadataPageEnabled(final boolean newMetadataPageEnabled) {
		mMetadataPageEnabled = newMetadataPageEnabled;
	}
	
	public ReportChartSettings getReportChartSettings() {
		return mReportChartSettings;
	}
	
	public void setReportPaginationEnabled(final boolean newReportPaginationEnabled) {
		mReportPaginationEnabled = newReportPaginationEnabled;
	}
	
	public boolean getReportPaginationEnabled() {
		return mReportPaginationEnabled;
	}
	
	public void setCustomReportIconFile(final File newCustomReportIconFile) {
		mCustomReportIconFile = newCustomReportIconFile;
	}
	
	public File getCustomReportIconFile() {
		return mCustomReportIconFile;
	}
	
	public void setCustomReportIconEnabled(final boolean newCustomReportIconState) {
		mCustomReportIconEnabled = newCustomReportIconState;
	}
	
	public boolean getCustomReportIconEnabled() {
		return mCustomReportIconEnabled;
	}
	
	public void setReportIconEnabled(final boolean newReportIconState) {
		mReportIconEnabled = newReportIconState;
	}
	
	public boolean getReportIconEnabled() {
		return mReportIconEnabled;
	}

	private void performKeyFrameCheck() {
		ReportUtils.GLOBAL_REPORT_CONTEXT_LOCK.lock();
		
		KeyFrameCheckThread keyFrameCheckThread = new KeyFrameCheckThread(BLOB_CONTEXT_LIST.getContextFilename());
		keyFrameCheckThread.start();

		try {
			keyFrameCheckThread.join();
		} catch (InterruptedException interruptedException) {
			//interruptedException.printStackTrace();
		}
		
		boolean modifiedContextFile = keyFrameCheckThread.getModifiedContextFile();
		
		if(modifiedContextFile == true) {
			BLOB_CONTEXT_LIST.setContextFilename(BLOB_CONTEXT_LIST.getContextFilename() + ".tmp");
		}
		
		ReportUtils.GLOBAL_REPORT_CONTEXT_LOCK.unlock();
	}
	
	public void performCreateReportAction() {
		performKeyFrameCheck();
		
		disableFurtherActions();
		
		ChartHtmlReportGenerator htmlReportGenerator = new ChartHtmlReportGenerator(CASE_NAME, path, 
				mFalsePositiveRemover.getFalsePositiveBundle(), BLOB_CONTEXT_LIST,
				mReportIconEnabled, mCustomReportIconEnabled, mCustomReportIconFile,
				mReportPaginationEnabled, mReportChartSettings);
		String reportFolderName = htmlReportGenerator.createRootPage();
		try {
			Desktop.getDesktop().open(new File(reportFolderName));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		clearPriorSelectedStates();
				
		WindowRegistry.getInstance().closeFrames(mChildWindowList.getWindows());
		this.dispose();
	}

	@Override
	protected void performSaveChangesAction() {
		
	}

	@Override
	protected void buildMenuBar() {
		
	}

	@Override
	protected void disableFurtherActions() {
		
	}

	@Override
	protected void enableFurtherActions() {
		
	}
	
}

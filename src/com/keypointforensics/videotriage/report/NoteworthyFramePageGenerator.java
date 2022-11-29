package com.keypointforensics.videotriage.report;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.keypointforensics.videotriage.legacy.FileUtilsLegacy;
import com.keypointforensics.videotriage.list.SortedList;
import com.keypointforensics.videotriage.util.FileUtils;

public class NoteworthyFramePageGenerator {
	
	private final int PAGE_DATA_COLUMN_COUNT = 4;
	
	public static final String NOTEWORTHY_PAGE_NAME_SHORT = "noteworthy.html";
	public static final String NOTEWORTHY_FRAME_TABLE_HEADER_LABEL = "Noteworthy Frames";
	
	protected final String ABSOLUTE_EXTRACT_FOLDER_PATH;
	protected final String CASE_NAME;
	protected final String REPORT_ROOT;
	
	protected String mReportPageHeader;
	protected SortedList<String> mNoteworthyFrameFilenameList;
	
	public NoteworthyFramePageGenerator(final String absoluteExtractFolderPath, final String caseName, 
			final String reportRoot, final String reportPageHeader) {
		ABSOLUTE_EXTRACT_FOLDER_PATH = absoluteExtractFolderPath;
		CASE_NAME   = caseName;
		REPORT_ROOT = reportRoot;
		
		mReportPageHeader = reportPageHeader;
		
		loadNoteworthyFramesIntoSortedList();
	}

	private void loadNoteworthyFramesIntoSortedList() {
		ArrayList<String> unsortedReportExtractList = (ArrayList<String>) FileUtilsLegacy.parseDirectoryRecursiveForAll(FileUtils.REPORT_EXTRACTS_DIRECTORY + CASE_NAME);
	
		mNoteworthyFrameFilenameList = new SortedList<String>();
		
		for(String unsortedReportExtract : unsortedReportExtractList) {
			mNoteworthyFrameFilenameList.insertSorted(unsortedReportExtract);
		}
	}
	
	public String getTableHeaderLabel() {
		return NOTEWORTHY_FRAME_TABLE_HEADER_LABEL;
	}
	
	public String getFramePageNameShort() {
		return NOTEWORTHY_PAGE_NAME_SHORT;
	}
	
	public String getFramePageName() {
		return ABSOLUTE_EXTRACT_FOLDER_PATH + getFramePageNameShort();
	}
	
	private int getFrameIndexFromAbsoluteFilename(final String absoluteFrameFilename) {
		String shortFilename = FileUtils.getShortFilename(absoluteFrameFilename);
		
		try {
			return Integer.parseInt(shortFilename.substring(shortFilename.indexOf("-") + 1, shortFilename.indexOf(".")));			
		} catch(NumberFormatException numberFormatException) {
			//numberFormatException.printStackTrace();
		}
		
		return -1;
	}
	
	public String getFlatPageTableData() {		
		StringBuilder pageBuilder = new StringBuilder(2000);
		
		pageBuilder.append("<table align=\"center\" class=\"table-title\"><tr><td><h3 align=\"center\">");
		pageBuilder.append(getTableHeaderLabel());
		pageBuilder.append("</h3><p align=\"center\">");
		pageBuilder.append(mNoteworthyFrameFilenameList.size());
		pageBuilder.append(" Frame");
		if(mNoteworthyFrameFilenameList.size() > 1) {
			pageBuilder.append("s");
		}
		pageBuilder.append("</p></td></tr></table><table class=\"table-fill\"><thead><tr><th></th><th></th><th></th><th></th></thead><tbody class=\"table-hover\">");
		
		int columnIndex = 0;
		
		for(String noteworthyFrameAbsolutePath : mNoteworthyFrameFilenameList) {
			if(columnIndex == 0) {
				pageBuilder.append("<tr>");
			}
		 
			pageBuilder.append("<td class=\"text-left\" width=\"300px\" height=\"300px\" style=\"vertical-align: top\"><a href=\"file:///");
			pageBuilder.append(noteworthyFrameAbsolutePath);
			pageBuilder.append("\"><img src=\"file:///");
			pageBuilder.append(noteworthyFrameAbsolutePath);
			pageBuilder.append("\" width=\"300px\" height=\"300px\"/></a><br><p>Frame ");
			
			int index = getFrameIndexFromAbsoluteFilename(noteworthyFrameAbsolutePath);
			
			if(index != -1) {
				pageBuilder.append(index);
			} else {
				pageBuilder.append(FileUtils.getShortFilename(noteworthyFrameAbsolutePath));
			}
			
			pageBuilder.append("</p>");
			
			pageBuilder.append("</td>");
			
			columnIndex++;
			
			if(columnIndex == PAGE_DATA_COLUMN_COUNT) {
				pageBuilder.append("</tr>");
				columnIndex = 0;
			}
		}
		
		pageBuilder.append("</tbody></table><br>");
		
		return pageBuilder.toString();
	}
	
	public void buildFramePage() {				
		File metadataPageFile = new File(ABSOLUTE_EXTRACT_FOLDER_PATH + getFramePageNameShort());
		StringBuilder pageBuilder = new StringBuilder(2000);
		
		mReportPageHeader = mReportPageHeader.substring(0, mReportPageHeader.lastIndexOf("<br>"));
		pageBuilder.append(mReportPageHeader);
		
		pageBuilder.append("<table align=\"center\" class=\"table-title\"><tr><td><h3 align=\"center\">");
		pageBuilder.append(getTableHeaderLabel());
		pageBuilder.append("</h3><p align=\"center\">");
		pageBuilder.append(mNoteworthyFrameFilenameList.size());
		pageBuilder.append(" Frame");
		if(mNoteworthyFrameFilenameList.size() > 1) {
			pageBuilder.append("s");
		}
		pageBuilder.append("</p></td></tr></table><table class=\"table-fill\"><thead><tr><th></th><th></th><th></th><th></th></thead><tbody class=\"table-hover\">");
		
		int columnIndex = 0;
		
		for(String noteworthyFrameAbsolutePath : mNoteworthyFrameFilenameList) {
			if(columnIndex == 0) {
				pageBuilder.append("<tr>");
			}
		 
			pageBuilder.append("<td class=\"text-left\" width=\"300px\" height=\"300px\" style=\"vertical-align: top\"><a href=\"file:///");
			pageBuilder.append(noteworthyFrameAbsolutePath);
			pageBuilder.append("\"><img src=\"file:///");
			pageBuilder.append(noteworthyFrameAbsolutePath);
			pageBuilder.append("\" width=\"300px\" height=\"300px\"/></a><br><p>Frame ");

			int index = getFrameIndexFromAbsoluteFilename(noteworthyFrameAbsolutePath);
			
			if(index != -1) {
				pageBuilder.append(index);
			} else {
				pageBuilder.append(FileUtils.getShortFilename(noteworthyFrameAbsolutePath));
			}
			
			pageBuilder.append("</p>"); 
			
			pageBuilder.append("</td>");
			
			columnIndex++;
			
			if(columnIndex == PAGE_DATA_COLUMN_COUNT) {
				pageBuilder.append("</tr>");
				columnIndex = 0;
			}
		}
		
		pageBuilder.append("</tbody></table><br><div align=\"center\"><a href=\"");
		pageBuilder.append(REPORT_ROOT);
		pageBuilder.append("\">Home</a></div></body><html>");
		
		PrintWriter pageWriter = null;
		
		try {
			pageWriter = new PrintWriter(metadataPageFile);
			
			pageWriter.append(pageBuilder.toString());
			
			pageWriter.flush();
			pageWriter.close();
		} catch (FileNotFoundException e) {
			//e.printStackTrace();
		}
	}
	
}

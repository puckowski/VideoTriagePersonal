package com.keypointforensics.videotriage.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.keypointforensics.videotriage.legacy.FileUtilsLegacy;
import com.keypointforensics.videotriage.util.FileUtils;

public class NotePageGenerator {

	private final String NOTES_PAGE_FILENAME = "notes.html";
	
	private final String CASE_NAME;
	
	private String mReportPageHeader;
	private String mReportRoot;
	
	private String mNotesFolderPath;
	private String mPageName;
		
	private StringBuilder mPageBuilder;
	
	private ArrayList<String> mCaseNotes;
	
	public NotePageGenerator(final String caseName) {
		CASE_NAME = FileUtils.getShortFilename(caseName);
		
		mPageBuilder = new StringBuilder(2000);
		mCaseNotes = new ArrayList<String>();
		
		loadCaseNotes();
	}
	
	public boolean hasNotes() {
		return (mCaseNotes.isEmpty() == false);
	}
	
	private void loadCaseNotes() {
		mCaseNotes.addAll(FileUtilsLegacy.parseDirectoryRecursiveForAll(FileUtils.NOTES_DIRECTORY + CASE_NAME));
	}
	
	public void setReportPageHeader(final String reportPageHeader) {
		mReportPageHeader = reportPageHeader;
	}
	
	public void setReportRoot(final String reportRoot) {
		mReportRoot = reportRoot;
	}
	
	public void buildPage() {
		mPageBuilder.append(mReportPageHeader);
		
		mPageBuilder.append("<table class=\"table-fill\"><thead><tr><th class=\"text-left\" width=\"300px\">Note Name</th><th>Note Details</th></tr></thead><tbody class=\"table-hover\">");

		//
		for(String caseNote : mCaseNotes) {
			mPageBuilder.append("<tr class=\"outer\"><td class=\"text-left\" style=\"vertical-align: top\">");
			mPageBuilder.append(FileUtils.getShortFilename(caseNote));
			mPageBuilder.append("</td><td class=\"text-left\">");
			try(BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(caseNote)))) {
			    for(String line; (line = bufferedReader.readLine()) != null; ) {
			        mPageBuilder.append(line);
			        mPageBuilder.append("<br>");
			    }
			} catch (FileNotFoundException fileNotFoundException) {
				//fileNotFoundException.printStackTrace();
			} catch (IOException ioException) {
				//ioException.printStackTrace();
			}
			mPageBuilder.append("</td></tr>");
		}
		//
		
		mPageBuilder.append("</tbody></table><br><div align=\"center\"><a href=\"");
		mPageBuilder.append(mReportRoot);
		mPageBuilder.append("\">Home</a></div></body><html>");
		
		PrintWriter pageWriter = null;
		
		File statisticsPageFile = new File(mPageName);
		
		try {
			pageWriter = new PrintWriter(statisticsPageFile);
			
			pageWriter.append(mPageBuilder.toString());
			
			pageWriter.flush();
			pageWriter.close();
		} catch (FileNotFoundException fileNotFoundException) {
			//fileNotFoundException.printStackTrace();
		}
	}
	
	public void buildEmbeddablePage() {		
		mPageBuilder.append("<table class=\"table-fill\"><thead><tr><th class=\"text-left\" width=\"300px\">Note Name</th><th>Note Details</th></tr></thead><tbody class=\"table-hover\">");

		//
		for(String caseNote : mCaseNotes) {
			mPageBuilder.append("<tr class=\"outer\"><td class=\"text-left\" style=\"vertical-align: top\">");
			mPageBuilder.append(FileUtils.getShortFilename(caseNote));
			mPageBuilder.append("</td><td class=\\\"text-left\\\">");
			try(BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(caseNote)))) {
			    for(String line; (line = bufferedReader.readLine()) != null; ) {
			        mPageBuilder.append(line);
			        mPageBuilder.append("<br>");
			    }
			} catch (FileNotFoundException fileNotFoundException) {
				//fileNotFoundException.printStackTrace();
			} catch (IOException ioException) {
				//ioException.printStackTrace();
			}
			mPageBuilder.append("</td></tr>");
		}
		//
		
		mPageBuilder.append("</tbody></table><br>");
	}
	
	public String getEmbeddablePage() {
		return mPageBuilder.toString();
	}
	
	private void setNotesPageName() {
		mPageName = mNotesFolderPath + NOTES_PAGE_FILENAME;
	}
	
	public void setNotesFolderPath(final String statisticsFolderPath) {
		mNotesFolderPath = statisticsFolderPath;
		
		setNotesPageName();
	}
	
	public String getNotesPageName() {
		return mPageName;
	}
}

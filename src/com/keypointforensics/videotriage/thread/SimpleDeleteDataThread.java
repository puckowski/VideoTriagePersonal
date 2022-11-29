package com.keypointforensics.videotriage.thread;

import java.io.File;
import java.util.ArrayList;

import org.tmatesoft.sqljet.core.SqlJetException;

import com.keypointforensics.videotriage.legacy.FileUtilsLegacy;
import com.keypointforensics.videotriage.progress.ProgressBundle;
import com.keypointforensics.videotriage.sqlite.CaseSqliteDatabaseHelper;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ProgressUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;

public class SimpleDeleteDataThread extends Thread {

	/*
	 * Author: Daniel Puckowski
	 */
	
	public SimpleDeleteDataThread() {
		
	}
	
	@Override
	public void run() {
		ThreadUtils.addThreadToHandleList("DelDat Run", this);
				
		ProgressBundle deleteProgressBundle = ProgressUtils.getProgressBundle("Delete Data Progress...", 17);
	
		//deleteProgressBundle.progressBar.setValue(deleteProgressBundle.progressBar.getValue() + 1);
		//deleteProgressBundle.progressBar.repaint();
		
		FileUtils.deleteDirectoryContents(new File(FileUtils.CAPTURES_DIRECTORY), true);
		
		deleteProgressBundle.progressBar.setValue(deleteProgressBundle.progressBar.getValue() + 1);
		deleteProgressBundle.progressBar.repaint();
		
		ArrayList<String> namesInDatabaseFolder = (ArrayList<String>) FileUtilsLegacy.parseDirectory(FileUtils.DATABASE_DIRECTORY);
		CaseSqliteDatabaseHelper caseDbHelper = null;
			
		for(String nameInDatabaseFolder : namesInDatabaseFolder) {
			try {
				caseDbHelper = CaseSqliteDatabaseHelper.getInstance(nameInDatabaseFolder);
				caseDbHelper.dropAllTablesAndIndices(caseDbHelper.getDb(nameInDatabaseFolder));
				caseDbHelper.deleteDbFile(caseDbHelper.getDbFile(nameInDatabaseFolder));
			} catch (SqlJetException sqlJetException) {
	
			}
				
			caseDbHelper = null;
		}
			
		FileUtils.deleteDirectoryContents(new File(FileUtils.DATABASE_DIRECTORY), true);
		
		deleteProgressBundle.progressBar.setValue(deleteProgressBundle.progressBar.getValue() + 1);
		deleteProgressBundle.progressBar.repaint();
		
		FileUtils.deleteDirectoryContents(new File(FileUtils.CONTEXT_DIRECTORY), true);
		
		deleteProgressBundle.progressBar.setValue(deleteProgressBundle.progressBar.getValue() + 1);
		deleteProgressBundle.progressBar.repaint();
		
		FileUtils.deleteDirectoryContents(new File(FileUtils.ENHANCED_DIRECTORY), true);
		
		deleteProgressBundle.progressBar.setValue(deleteProgressBundle.progressBar.getValue() + 1);
		deleteProgressBundle.progressBar.repaint();
		
		FileUtils.deleteDirectoryContents(new File(FileUtils.EXPORTS_DIRECTORY), true);
		
		deleteProgressBundle.progressBar.setValue(deleteProgressBundle.progressBar.getValue() + 1);
		deleteProgressBundle.progressBar.repaint();
		
		FileUtils.deleteDirectoryContents(new File(FileUtilsLegacy.VIDEO_FRAME_EXTRACTS_DIRECTORY), true);
		
		deleteProgressBundle.progressBar.setValue(deleteProgressBundle.progressBar.getValue() + 1);
		deleteProgressBundle.progressBar.repaint();
		
		FileUtils.deleteDirectoryContents(new File(FileUtils.PLATES_DIRECTORY), true);
		FileUtils.deleteDirectoryContents(new File(FileUtils.FACES_DIRECTORY), true);
		FileUtils.deleteDirectoryContents(new File(FileUtils.PEDESTRIANS_DIRECTORY), true);
		FileUtils.deleteDirectoryContents(new File(FileUtils.CARS_DIRECTORY), true);
		FileUtils.deleteDirectoryContents(new File(FileUtils.EXPLICIT_DIRECTORY), true);
		
		deleteProgressBundle.progressBar.setValue(deleteProgressBundle.progressBar.getValue() + 1);
		deleteProgressBundle.progressBar.repaint();
		
		FileUtils.deleteDirectoryContents(new File(FileUtils.FILTERED_DIRECTORY), true);
		
		deleteProgressBundle.progressBar.setValue(deleteProgressBundle.progressBar.getValue() + 1);
		deleteProgressBundle.progressBar.repaint();
		
		FileUtils.deleteDirectoryContents(new File(FileUtilsLegacy.TEMPORARY_DIRECTORY), true);
		
		deleteProgressBundle.progressBar.setValue(deleteProgressBundle.progressBar.getValue() + 1);
		deleteProgressBundle.progressBar.repaint();
		
		FileUtils.deleteDirectoryContents(new File(FileUtils.RESIZED_DIRECTORY), true);
		
		deleteProgressBundle.progressBar.setValue(deleteProgressBundle.progressBar.getValue() + 1);
		deleteProgressBundle.progressBar.repaint();
		
		FileUtils.deleteDirectoryContents(new File(FileUtils.REPORT_EXTRACTS_DIRECTORY), true);
		
		File temporaryReportExtractsDirectory = new File(FileUtils.REPORT_EXTRACTS_TEMPORARY_DIRECTORY);
		temporaryReportExtractsDirectory.mkdir();
		
		deleteProgressBundle.progressBar.setValue(deleteProgressBundle.progressBar.getValue() + 1);
		deleteProgressBundle.progressBar.repaint();
		
		FileUtils.deleteDirectoryContents(new File(FileUtils.REDACT_DIRECTORY), true);
		
		deleteProgressBundle.progressBar.setValue(deleteProgressBundle.progressBar.getValue() + 1);
		deleteProgressBundle.progressBar.repaint();
		
		FileUtils.deleteDirectoryContents(new File(FileUtils.PROCESSING_DIRECTORY), true);
		
		deleteProgressBundle.progressBar.setValue(deleteProgressBundle.progressBar.getValue() + 1);
		deleteProgressBundle.progressBar.repaint();
		
		FileUtils.deleteDirectoryContents(new File(FileUtils.PREVIEWS_DIRECTORY), true);
		
		deleteProgressBundle.progressBar.setValue(deleteProgressBundle.progressBar.getValue() + 1);
		deleteProgressBundle.progressBar.repaint();
		
		FileUtils.deleteDirectoryContents(new File(FileUtils.NOTES_DIRECTORY), true);
		
		deleteProgressBundle.progressBar.setValue(deleteProgressBundle.progressBar.getValue() + 1);
		deleteProgressBundle.progressBar.repaint();
		
		FileUtils.deleteDirectoryContents(new File(FileUtils.MERGED_DIRECTORY), true);
		
		deleteProgressBundle.progressBar.setValue(deleteProgressBundle.progressBar.getValue() + 1);
		deleteProgressBundle.progressBar.repaint();
		
		FileUtils.deleteDirectoryContents(new File(FileUtils.REPORTS_DIRECTORY), true);
	
		deleteProgressBundle.progressBar.setValue(deleteProgressBundle.progressBar.getValue() + 1);
		deleteProgressBundle.progressBar.repaint();
		deleteProgressBundle.frame.dispose();
						
		ThreadUtils.removeThreadFromHandleList(this);
	}
}

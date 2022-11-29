package com.keypointforensics.videotriage.thread;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;

import org.tmatesoft.sqljet.core.SqlJetException;

import com.keypointforensics.videotriage.gui.data.CaseDialogDeleteDataWindow;
import com.keypointforensics.videotriage.gui.main.SelectCaseDialog;
import com.keypointforensics.videotriage.legacy.FileUtilsLegacy;
import com.keypointforensics.videotriage.progress.ProgressBundle;
import com.keypointforensics.videotriage.sqlite.CaseSqliteDatabaseHelper;
import com.keypointforensics.videotriage.util.CursorUtils;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;

public class CaseDialogDeleteDataThread extends Thread {

	/*
	 * Author: Daniel Puckowski
	 */
	
	private final CaseDialogDeleteDataWindow DELETE_DATA_WINDOW;
	private final SelectCaseDialog           SELECT_CASE_DIALOG;
	
	//private final JCheckBox LOG_CHECK_BOX;
	private final JCheckBox IMAGES_CHECK_BOX;
	private final JCheckBox DATABASES_CHECK_BOX;
	private final JCheckBox ENHANCED_CHECK_BOX;
	private final JCheckBox EXPORTS_CHECK_BOX;
	private final JCheckBox EXTRACTS_CHECK_BOX;
	private final JCheckBox DETECTIONS_CHECK_BOX;
	private final JCheckBox FILTERED_CHECK_BOX;
	private final JCheckBox TEMPORARY_CHECK_BOX;
	private final JCheckBox REPORTS_CHECK_BOX;
	private final JCheckBox PROCESSING_CHECK_BOX;
	private final JCheckBox PREVIEW_CHECK_BOX;
	private final JCheckBox RESIZED_CHECK_BOX;
	private final JCheckBox REPORT_EXTRACTS_CHECK_BOX;
	private final JCheckBox REDACT_CHECK_BOX;
	private final JCheckBox MERGED_CHECK_BOX;
	private final JCheckBox NOTES_CHECK_BOX;
	
	private volatile boolean mDone;
	
	private ProgressBundle mDeleteProgressBundle;
	
	public CaseDialogDeleteDataThread(final CaseDialogDeleteDataWindow caseDialogDeleteDataWindow, final SelectCaseDialog selectCaseDialog,
			final JCheckBox imagesCheckBox, 
			final JCheckBox databasesCheckBox, final JCheckBox enhancedCheckBox, final JCheckBox exportsCheckBox, 
			final JCheckBox extractsCheckBox, final JCheckBox detectionsCheckBox, final JCheckBox filteredCheckBox, final JCheckBox temporaryCheckBox,
			final JCheckBox reportsCheckBox, final JCheckBox processingCheckBox, final JCheckBox previewCheckBox, 
			final JCheckBox resizedCheckBox, final JCheckBox reportExtractsCheckBox, final JCheckBox redactCheckBox, final JCheckBox mergedCheckBox,
			final JCheckBox notesCheckBox, final ProgressBundle deleteProgressBundle) {
		DELETE_DATA_WINDOW        = caseDialogDeleteDataWindow;
		SELECT_CASE_DIALOG        = selectCaseDialog;
		//LOG_CHECK_BOX             = logCheckBox;
		IMAGES_CHECK_BOX          = imagesCheckBox;
		DATABASES_CHECK_BOX       = databasesCheckBox;
		ENHANCED_CHECK_BOX        = enhancedCheckBox;
		EXPORTS_CHECK_BOX         = exportsCheckBox;
		EXTRACTS_CHECK_BOX        = extractsCheckBox;
		DETECTIONS_CHECK_BOX      = detectionsCheckBox;
		FILTERED_CHECK_BOX        = filteredCheckBox;
		TEMPORARY_CHECK_BOX       = temporaryCheckBox;
		REPORTS_CHECK_BOX         = reportsCheckBox;
		PROCESSING_CHECK_BOX      = processingCheckBox;
		PREVIEW_CHECK_BOX         = previewCheckBox;
		RESIZED_CHECK_BOX         = resizedCheckBox;
		REPORT_EXTRACTS_CHECK_BOX = reportExtractsCheckBox;
		REDACT_CHECK_BOX          = redactCheckBox;
		MERGED_CHECK_BOX          = mergedCheckBox;
		NOTES_CHECK_BOX           = notesCheckBox;
		
		mDeleteProgressBundle = deleteProgressBundle;
		
		mDone = false;
	}
	
	public boolean isDone() {
		return mDone;
	}
	
	@Override
	public void run() {
		ThreadUtils.addThreadToHandleList("CaseDelDialog Run", this);
		
		CursorUtils.setBusyCursor(SELECT_CASE_DIALOG);
	
		DELETE_DATA_WINDOW.setTitle("Deleting...");
		
		//if(LOG_CHECK_BOX.isSelected() == true) {
		//	final boolean success = FileUtils.deleteDirectoryContents(new File(FileUtils.LOGS_DIRECTORY), true);
		//}
		
		mDeleteProgressBundle.progressBar.setValue(mDeleteProgressBundle.progressBar.getValue() + 1);
		mDeleteProgressBundle.progressBar.repaint();
		
		if(IMAGES_CHECK_BOX.isSelected() == true) {
			final boolean success = FileUtils.deleteDirectoryContents(new File(FileUtils.CAPTURES_DIRECTORY), true);
		}
		
		mDeleteProgressBundle.progressBar.setValue(mDeleteProgressBundle.progressBar.getValue() + 1);
		mDeleteProgressBundle.progressBar.repaint();
		
		if(DATABASES_CHECK_BOX.isSelected() == true) {			
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
			
			final boolean databaseSuccess = FileUtils.deleteDirectoryContents(new File(FileUtils.DATABASE_DIRECTORY), true);
			final boolean contextSuccess = FileUtils.deleteDirectoryContents(new File(FileUtils.CONTEXT_DIRECTORY), true);
		}
		
		mDeleteProgressBundle.progressBar.setValue(mDeleteProgressBundle.progressBar.getValue() + 1);
		mDeleteProgressBundle.progressBar.repaint();
		
		if(ENHANCED_CHECK_BOX.isSelected() == true) {
			final boolean success = FileUtils.deleteDirectoryContents(new File(FileUtils.ENHANCED_DIRECTORY), true);
		}
		
		mDeleteProgressBundle.progressBar.setValue(mDeleteProgressBundle.progressBar.getValue() + 1);
		mDeleteProgressBundle.progressBar.repaint();
		
		if(EXPORTS_CHECK_BOX.isSelected() == true) {
			final boolean success = FileUtils.deleteDirectoryContents(new File(FileUtils.EXPORTS_DIRECTORY), true);
		}
		
		mDeleteProgressBundle.progressBar.setValue(mDeleteProgressBundle.progressBar.getValue() + 1);
		mDeleteProgressBundle.progressBar.repaint();
		
		if(EXTRACTS_CHECK_BOX.isSelected() == true) {
			final boolean success = FileUtils.deleteDirectoryContents(new File(FileUtilsLegacy.VIDEO_FRAME_EXTRACTS_DIRECTORY), true);
		}
		
		mDeleteProgressBundle.progressBar.setValue(mDeleteProgressBundle.progressBar.getValue() + 1);
		mDeleteProgressBundle.progressBar.repaint();
		
		if(DETECTIONS_CHECK_BOX.isSelected() == true) {
			final boolean platesSuccess = FileUtils.deleteDirectoryContents(new File(FileUtils.PLATES_DIRECTORY), true);
			final boolean facesSuccess = FileUtils.deleteDirectoryContents(new File(FileUtils.FACES_DIRECTORY), true);
			final boolean pedestriansSuccess = FileUtils.deleteDirectoryContents(new File(FileUtils.PEDESTRIANS_DIRECTORY), true);
			final boolean carsSuccess = FileUtils.deleteDirectoryContents(new File(FileUtils.CARS_DIRECTORY), true);
			final boolean explicitSuccess = FileUtils.deleteDirectoryContents(new File(FileUtils.EXPLICIT_DIRECTORY), true);
		}
		
		mDeleteProgressBundle.progressBar.setValue(mDeleteProgressBundle.progressBar.getValue() + 1);
		mDeleteProgressBundle.progressBar.repaint();
		
		if(FILTERED_CHECK_BOX.isSelected() == true) {
			final boolean success = FileUtils.deleteDirectoryContents(new File(FileUtils.FILTERED_DIRECTORY), true);
		}
		
		mDeleteProgressBundle.progressBar.setValue(mDeleteProgressBundle.progressBar.getValue() + 1);
		mDeleteProgressBundle.progressBar.repaint();
		
		if(TEMPORARY_CHECK_BOX.isSelected() == true) {
			final boolean success = FileUtils.deleteDirectoryContents(new File(FileUtilsLegacy.TEMPORARY_DIRECTORY), true);
		}

		mDeleteProgressBundle.progressBar.setValue(mDeleteProgressBundle.progressBar.getValue() + 1);
		mDeleteProgressBundle.progressBar.repaint();
		
		if(REPORTS_CHECK_BOX.isSelected() == true) {
			final boolean success = FileUtils.deleteDirectoryContents(new File(FileUtils.REPORTS_DIRECTORY), true);
		}
		
		mDeleteProgressBundle.progressBar.setValue(mDeleteProgressBundle.progressBar.getValue() + 1);
		mDeleteProgressBundle.progressBar.repaint();
		
		if(PROCESSING_CHECK_BOX.isSelected() == true) {
			final boolean success = FileUtils.deleteDirectoryContents(new File(FileUtils.PROCESSING_DIRECTORY), true);
		}
		
		mDeleteProgressBundle.progressBar.setValue(mDeleteProgressBundle.progressBar.getValue() + 1);
		mDeleteProgressBundle.progressBar.repaint();
		
		if(PREVIEW_CHECK_BOX.isSelected() == true) {
			final boolean success = FileUtils.deleteDirectoryContents(new File(FileUtils.PREVIEWS_DIRECTORY), true);
		}
		
		mDeleteProgressBundle.progressBar.setValue(mDeleteProgressBundle.progressBar.getValue() + 1);
		mDeleteProgressBundle.progressBar.repaint();
		
		if(RESIZED_CHECK_BOX.isSelected() == true) {
			final boolean success = FileUtils.deleteDirectoryContents(new File(FileUtils.RESIZED_DIRECTORY), true);
		}
		
		mDeleteProgressBundle.progressBar.setValue(mDeleteProgressBundle.progressBar.getValue() + 1);
		mDeleteProgressBundle.progressBar.repaint();
		
		if(REPORT_EXTRACTS_CHECK_BOX.isSelected() == true) {
			final boolean success = FileUtils.deleteDirectoryContents(new File(FileUtils.REPORT_EXTRACTS_DIRECTORY), true);
			
			FileUtils.createDirectory(FileUtils.REPORT_EXTRACTS_TEMPORARY_DIRECTORY);
		}
		
		mDeleteProgressBundle.progressBar.setValue(mDeleteProgressBundle.progressBar.getValue() + 1);
		mDeleteProgressBundle.progressBar.repaint();
		
		if(REDACT_CHECK_BOX.isSelected() == true) {
			final boolean success = FileUtils.deleteDirectoryContents(new File(FileUtils.REDACT_DIRECTORY), true);
		}
		
		mDeleteProgressBundle.progressBar.setValue(mDeleteProgressBundle.progressBar.getValue() + 1);
		mDeleteProgressBundle.progressBar.repaint();
		
		if(MERGED_CHECK_BOX.isSelected() == true) {
			final boolean success = FileUtils.deleteDirectoryContents(new File(FileUtils.MERGED_DIRECTORY), true);
		}
		
		mDeleteProgressBundle.progressBar.setValue(mDeleteProgressBundle.progressBar.getValue() + 1);
		mDeleteProgressBundle.progressBar.repaint();
		
		if(NOTES_CHECK_BOX.isSelected() == true) {
			final boolean success = FileUtils.deleteDirectoryContents(new File(FileUtils.NOTES_DIRECTORY), true);
		}
		
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	mDeleteProgressBundle.progressBar.setValue(mDeleteProgressBundle.progressBar.getValue() + 1);
		    	mDeleteProgressBundle.progressBar.repaint();
		    	mDeleteProgressBundle.frame.dispose();
		    	
		    	SELECT_CASE_DIALOG.updateDatabaseList();
		    	SELECT_CASE_DIALOG.invalidate();
		    	SELECT_CASE_DIALOG.repaint();
		    }
		});
		
		CursorUtils.setDefaultCursor(SELECT_CASE_DIALOG);
		
		ThreadUtils.removeThreadFromHandleList(this);
	}
}

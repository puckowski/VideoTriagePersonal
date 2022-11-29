package com.keypointforensics.videotriage.thread;

import java.io.File;

import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.Utils;

public class DeleteCaseDataThread extends Thread {

	/*
	 * Author: Daniel Puckowski
	 */
	
	private final String CASE_SELECTION;
	private final String CASE_DATABASE_NAME;
	private final String BLOB_CONTEXT_NAME;
	private final String CONTEXT_NAME;
	private final String CAPTURE_NAME;
	private final String NOTES_NAME;
	private final String REPORT_EXTRACTS_NAME;
	
	public DeleteCaseDataThread(String caseSelection, final String caseDatabaseName, final String blobContextName, final String contextName, final String captureName,
		final String notesName, final String reportExtractsName) {
		if(caseSelection.equals("db.videotriage") == true) {
			caseSelection = "Test Database";
		}
		
		CASE_SELECTION = caseSelection;
		CASE_DATABASE_NAME = caseDatabaseName;
		BLOB_CONTEXT_NAME = blobContextName;
		CONTEXT_NAME = contextName;
		CAPTURE_NAME = captureName;
		NOTES_NAME = notesName;
		REPORT_EXTRACTS_NAME = reportExtractsName;
	}
	
	@Override
	public void run() {			
		ThreadUtils.addThreadToHandleList("DelCaseDat Run", this);

		int failedToDeleteCount = 0;
		File currentFile;
		
		while (Thread.currentThread().isInterrupted() == false) {			
			//ThreadUtils.stopAllKnownTasks();
			//System.gc();
			
			currentFile = new File(CASE_DATABASE_NAME);
			if(currentFile.exists() == true) {
				final boolean caseDatabaseResult = FileUtils.deleteFile(currentFile);
				
				if(caseDatabaseResult == false) {
					failedToDeleteCount++;
				}
			}
			
			currentFile = new File(BLOB_CONTEXT_NAME);
			if(currentFile.exists() == true) {
				final boolean caseBlobContextResult = FileUtils.deleteFile(currentFile);
				
				if(caseBlobContextResult == false) {
					failedToDeleteCount++;
				}
			}
			
			currentFile = new File(CONTEXT_NAME);
			if(currentFile.exists() == true) {
				final boolean caseContextResult = FileUtils.deleteFile(currentFile);
				
				if(caseContextResult == false) {
					failedToDeleteCount++;
				}
			}
			
			currentFile = new File(CAPTURE_NAME);
			if(currentFile.exists() == true) {
				final boolean caseCaptureFolderItemsResult = FileUtils.deleteDirectoryContents(currentFile, true);
				final boolean caseCaptureFolderResult = FileUtils.deleteFileOrDirectory(currentFile);
				
				if(caseCaptureFolderItemsResult == false) {
					failedToDeleteCount++;
				}
				
				if(caseCaptureFolderResult == false) {
					failedToDeleteCount++;
				}
			}

			currentFile = new File(NOTES_NAME);
			if(currentFile.exists() == true) {
				final boolean notesFolderItemsResult = FileUtils.deleteDirectoryContents(currentFile, true);
				final boolean notesFolderResult = FileUtils.deleteFileOrDirectory(currentFile);
				
				if(notesFolderItemsResult == false) {
					failedToDeleteCount++;
				}
				
				if(notesFolderResult == false) {
					failedToDeleteCount++;
				}
			}
			
			currentFile = new File(REPORT_EXTRACTS_NAME);
			if(currentFile.exists() == true) {
				final boolean reportExtractsFolderItemsResult = FileUtils.deleteDirectoryContents(currentFile, true);
				final boolean reportExtractsFolderResult = FileUtils.deleteFileOrDirectory(currentFile);
				
				if(reportExtractsFolderItemsResult == false) {
					failedToDeleteCount++;
				}
				
				if(reportExtractsFolderResult == false) {
					failedToDeleteCount++;
				}
			}
			
			final String databaseNameShort = FileUtils.getShortFilename(CASE_DATABASE_NAME);
			
			currentFile = new File(FileUtils.FACES_DIRECTORY + databaseNameShort);
			if(currentFile.exists() == true) {
				final boolean caseFacesResult = FileUtils.deleteDirectoryContents(currentFile, true);
				final boolean caseFacesFolderResult = FileUtils.deleteFileOrDirectory(currentFile);
				
				if(caseFacesResult == false) {
					failedToDeleteCount++;
				}
				
				if(caseFacesFolderResult == false) {
					failedToDeleteCount++;
				}
			}
			
			currentFile = new File(FileUtils.LICENSE_DIRECTORY + databaseNameShort);
			if(currentFile.exists() == true) {
				final boolean caseLicensePlatesResult = FileUtils.deleteDirectoryContents(currentFile, true);
				final boolean caseLicensePlatesFolderResult = FileUtils.deleteFileOrDirectory(currentFile);
				
				if(caseLicensePlatesResult == false) {
					failedToDeleteCount++;
				}
				
				if(caseLicensePlatesFolderResult == false) {
					failedToDeleteCount++;
				}
			}
			
			currentFile = new File(FileUtils.PEDESTRIANS_DIRECTORY + databaseNameShort);
			if(currentFile.exists() == true) {
				final boolean casePedestriansResult = FileUtils.deleteDirectoryContents(currentFile, true);
				final boolean casePedestriansFolderResult = FileUtils.deleteFileOrDirectory(currentFile);
				
				if(casePedestriansResult == false) {
					failedToDeleteCount++;
				}
				
				if(casePedestriansFolderResult == false) {
					failedToDeleteCount++;
				}
			}
			
			currentFile = new File(FileUtils.EXPLICIT_DIRECTORY + databaseNameShort);
			if(currentFile.exists() == true) {
				final boolean caseExplicitResult = FileUtils.deleteDirectoryContents(currentFile, true);
				final boolean caseExplicitFolderResult = FileUtils.deleteFileOrDirectory(currentFile);
				
				if(caseExplicitResult == false) {
					failedToDeleteCount++;
				}
				
				if(caseExplicitFolderResult == false) {
					failedToDeleteCount++;
				}
			}
			
			currentFile = new File(FileUtils.REPORT_EXTRACTS_DIRECTORY + databaseNameShort);
			if(currentFile.exists() == true) {
				final boolean caseReportExtractsResult = FileUtils.deleteDirectoryContents(currentFile, true);
				final boolean caseReportExtractsFolderResult = FileUtils.deleteFileOrDirectory(currentFile);
				
				if(caseReportExtractsResult == false) {
					failedToDeleteCount++;
				}
				
				if(caseReportExtractsFolderResult == false) {
					failedToDeleteCount++;
				}
			}
			
			/*
			if(caseDatabaseResult == false ||
				caseBlobContextResult == false ||
				caseCaptureFolderItemsResult == false ||
				caseCaptureFolderResult == false ||
				caseContextResult == false// ||
				//caseFacesResult == false ||
				//caseLicensePlatesResult == false ||
				//casePedestriansResult == false
				) {
				Utils.displayMessageDialog("Deletion Error", "Could not delete all case data for case: " + CASE_SELECTION);
			}
			*/
			
			if(failedToDeleteCount > 0) {
				Utils.displayMessageDialog("Deletion Error", "Could not delete all case data for case: " + CASE_SELECTION);
			}
			
			break;
		}
		
		ThreadUtils.removeThreadFromHandleList(this);
	}
}

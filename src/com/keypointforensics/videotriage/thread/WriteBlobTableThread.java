package com.keypointforensics.videotriage.thread;

import java.util.ArrayList;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import com.keypointforensics.videotriage.sqlite.BlobRecord;
import com.keypointforensics.videotriage.sqlite.CaseSqliteDatabaseHelper;
import com.keypointforensics.videotriage.util.ThreadUtils;

public class WriteBlobTableThread extends Thread {

	/*
	 * Author: Daniel Puckowski
	 */
	
	private final ArrayList<BlobRecord> BLOB_RECORDS;
	private final String                DATABASE_NAME;
	
	public WriteBlobTableThread(final ArrayList<BlobRecord> blobRecords, final String databaseName) {
		BLOB_RECORDS  = blobRecords;
		DATABASE_NAME = databaseName;
	}
	
	@Override
	public void run() {			
		ThreadUtils.addThreadToHandleList("WriteBlobTab Run", this);

		while (Thread.currentThread().isInterrupted() == false) {			
			CaseSqliteDatabaseHelper dbHelper = CaseSqliteDatabaseHelper.getInstance(DATABASE_NAME);
			SqlJetDb db = null;

			final String dbName = DATABASE_NAME; 
			
			try {
				db = dbHelper.getDb(dbName);
			} catch (SqlJetException sqlJetException) {

			}

			//BlobRecord record = null;
			
			for (int i = 0; i < BLOB_RECORDS.size(); ++i) {
				//record = BLOB_RECORDS.get(i);
				
				try {
					dbHelper.insertBlobRecord(db, BLOB_RECORDS.get(i));
				} catch (SqlJetException sqlJetException) {

				}
				
				BLOB_RECORDS.set(i, null);
			}

			//
			dbHelper.closeDb(db);
			
			break;
		}
		
		ThreadUtils.removeThreadFromHandleList(this);
	}
}

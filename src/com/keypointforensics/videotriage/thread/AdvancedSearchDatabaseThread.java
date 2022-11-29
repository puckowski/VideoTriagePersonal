package com.keypointforensics.videotriage.thread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import com.keypointforensics.videotriage.gui.database.CustomTableModel;
import com.keypointforensics.videotriage.gui.database.DatabaseBrowseWindow;
import com.keypointforensics.videotriage.sqlite.BlobRecord;
import com.keypointforensics.videotriage.sqlite.CaseSqliteDatabaseHelper;
import com.keypointforensics.videotriage.sqlite.MySqliteDatabaseHelper;
import com.keypointforensics.videotriage.util.CursorUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;

public class AdvancedSearchDatabaseThread extends Thread {

	/*
	 * Author: Daniel Puckowski
	 */
	
	private static final String EMPTY_TIME_STAMP = "--";
	
	private final DatabaseBrowseWindow DB_BROWSE_WINDOW;
	private final JTable RESULT_TABLE;
	private final String AFTER_TIME_STAMP;
	private final String BEFORE_TIME_STAMP;
	private final String FILENAME;
	private final String IP_ADDRESS;
	private final String PORT;
	
	private ArrayList<BlobRecord> mBlobRecordsList;
	private ConcurrentHashMap<BlobRecord, Integer> mBlobRecordsMap;
	
	public AdvancedSearchDatabaseThread(final DatabaseBrowseWindow dbBrowseWindow, final JTable resultTable, final String afterTimeStampString, final String beforeTimeStampString,
			final String filenameString, final String ipAddressString, final String portString, ArrayList<BlobRecord> blobRecordsList, ConcurrentHashMap<BlobRecord, Integer> blobRecordsMap) {
		DB_BROWSE_WINDOW  = dbBrowseWindow;
		RESULT_TABLE      = resultTable;
		AFTER_TIME_STAMP  = afterTimeStampString;
		BEFORE_TIME_STAMP = beforeTimeStampString;
		FILENAME          = filenameString;
		IP_ADDRESS        = ipAddressString;
		PORT              = portString;
		
		mBlobRecordsList = blobRecordsList;
		mBlobRecordsMap  = blobRecordsMap;
	}
	
	@Override
	public void run() {
		ThreadUtils.addThreadToHandleList("AdvancedSearchDb Run", this);
		
		CursorUtils.setBusyCursor(DB_BROWSE_WINDOW.getRootPane());
		
		if(mBlobRecordsList == null) {
			mBlobRecordsList = new ArrayList<BlobRecord>();
		}
		else {
			mBlobRecordsList.clear();
		}
		
		if(mBlobRecordsMap == null) {
			mBlobRecordsMap = new ConcurrentHashMap<BlobRecord, Integer>();
		} 
		else {
			mBlobRecordsMap.clear();
		}
		
		final CaseSqliteDatabaseHelper dbHelper = CaseSqliteDatabaseHelper.getInstance(DB_BROWSE_WINDOW.getDatabaseName());
		BlobRecord blobRecord = null;
		
		try {
			ArrayList<BlobRecord> blobRecordsForTimeStamp = null, blobRecordsForFilename = null,
					blobRecordsForIpAddress = null, blobRecordsForPort = null;
			boolean performedSearch = false;
			int searchCount = 0;
			
			if(BEFORE_TIME_STAMP != null && AFTER_TIME_STAMP != null) {
				if (BEFORE_TIME_STAMP.equals(EMPTY_TIME_STAMP) == false && AFTER_TIME_STAMP.equals(EMPTY_TIME_STAMP) == false) {
					performedSearch = true;
					searchCount++;
					
					SqlJetDb db = dbHelper.getDb(DB_BROWSE_WINDOW.getDatabaseName());
					ISqlJetTable table = dbHelper.getTable(db, MySqliteDatabaseHelper.TABLE_NAME);
	
					blobRecordsForTimeStamp = dbHelper.getTargetedRecordsWithTimeStampByHourFormatted(db, table, AFTER_TIME_STAMP, BEFORE_TIME_STAMP);
				}
			}
			
			if (FILENAME != null && FILENAME.isEmpty() == false) {
				performedSearch = true;
				searchCount++;
				
				SqlJetDb db = dbHelper.getDb(DB_BROWSE_WINDOW.getDatabaseName());
				ISqlJetTable table = dbHelper.getTable(db, MySqliteDatabaseHelper.TABLE_NAME);
	
				blobRecordsForFilename = dbHelper.getAllRecordsWithFilename(db, table, FILENAME);
			}

			if (IP_ADDRESS != null && IP_ADDRESS.isEmpty() == false) {
				performedSearch = true;
				searchCount++;
				
				SqlJetDb db = dbHelper.getDb(DB_BROWSE_WINDOW.getDatabaseName());
				ISqlJetTable table = dbHelper.getTable(db, MySqliteDatabaseHelper.TABLE_NAME);

				blobRecordsForIpAddress = dbHelper.getAllRecordsWithIp(db, table, IP_ADDRESS);
			}

			if (PORT != null && PORT.isEmpty() == false) {
				performedSearch = true;
				searchCount++;
				
				SqlJetDb db = dbHelper.getDb(DB_BROWSE_WINDOW.getDatabaseName());
				ISqlJetTable table = dbHelper.getTable(db, MySqliteDatabaseHelper.TABLE_NAME);
				
				blobRecordsForPort = dbHelper.getAllRecordsWithPort(db, table, PORT);
			}
			
			if (blobRecordsForTimeStamp != null && blobRecordsForTimeStamp.isEmpty() == false) {
				for (int i = 0; i < blobRecordsForTimeStamp.size(); ++i) {
					blobRecord = blobRecordsForTimeStamp.get(i);

					if (mBlobRecordsMap.containsKey(blobRecord) == false) {
						mBlobRecordsMap.put(blobRecord, 1);
					} else {
						mBlobRecordsMap.replace(blobRecord, mBlobRecordsMap.get(blobRecord) + 1);
					}
				}
			}

			if (blobRecordsForFilename != null && blobRecordsForFilename.isEmpty() == false) {
				for (int i = 0; i < blobRecordsForFilename.size(); ++i) {
					blobRecord = blobRecordsForFilename.get(i);

					if (mBlobRecordsMap.containsKey(blobRecord) == false) {
						mBlobRecordsMap.put(blobRecord, 1);
					} else {
						mBlobRecordsMap.replace(blobRecord, mBlobRecordsMap.get(blobRecord) + 1);
					}
				}
			}

			if (blobRecordsForIpAddress != null && blobRecordsForIpAddress.isEmpty() == false) {
				for (int i = 0; i < blobRecordsForIpAddress.size(); ++i) {
					blobRecord = blobRecordsForIpAddress.get(i);

					if (mBlobRecordsMap.containsKey(blobRecord) == false) {
						mBlobRecordsMap.put(blobRecord, 1);
					} else {
						mBlobRecordsMap.replace(blobRecord, mBlobRecordsMap.get(blobRecord) + 1);
					}
				}
			}

			if (blobRecordsForPort != null && blobRecordsForPort.isEmpty() == false) {
				for (int i = 0; i < blobRecordsForPort.size(); ++i) {
					blobRecord = blobRecordsForPort.get(i);

					if (mBlobRecordsMap.containsKey(blobRecord) == false) {
						mBlobRecordsMap.put(blobRecord, 1);
					} else {
						mBlobRecordsMap.replace(blobRecord, mBlobRecordsMap.get(blobRecord) + 1);
					}
				}
			}
			
			if(performedSearch == false) {
				try {
					SqlJetDb db = dbHelper.getDb(DB_BROWSE_WINDOW.getDatabaseName());
					ISqlJetTable table = dbHelper.getTable(db, MySqliteDatabaseHelper.TABLE_NAME);
					
					mBlobRecordsList.addAll(dbHelper.getAllRecords(db, table, true));
				}  catch (SqlJetException sqlJetException) {
						
				}
			} else {
				for(Entry<BlobRecord, Integer> blobRecordEntry : mBlobRecordsMap.entrySet()) {
					if(blobRecordEntry.getValue() < searchCount) {
						mBlobRecordsMap.remove(blobRecordEntry.getKey());
					}
				}
			}
		} catch (SqlJetException sqlJetException) {
	
		}
		
		for (Entry<BlobRecord, Integer> entry : mBlobRecordsMap.entrySet()) {
			blobRecord = entry.getKey();

			mBlobRecordsList.add(blobRecord);
		}
		
		RESULT_TABLE.removeAll();
		
		if(mBlobRecordsList.isEmpty() == true) {
			DefaultTableModel model = new DefaultTableModel(new String[] { "Search Results" }, 0);
			RESULT_TABLE.setModel(model);
			model.addRow(new Object[]{ "No records found." });
		}
		else {
			CustomTableModel tableModel = new CustomTableModel(mBlobRecordsList);
			RESULT_TABLE.setModel(tableModel);
		}

		RESULT_TABLE.revalidate();
		
		CursorUtils.setDefaultCursor(DB_BROWSE_WINDOW.getRootPane());
		
		ThreadUtils.removeThreadFromHandleList(this);
	}
}

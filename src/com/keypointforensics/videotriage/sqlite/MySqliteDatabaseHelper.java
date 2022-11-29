package com.keypointforensics.videotriage.sqlite;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.schema.ISqlJetIndexDef;
import org.tmatesoft.sqljet.core.schema.ISqlJetTableDef;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.ISqlJetTransaction;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import com.keypointforensics.videotriage.util.FileUtils;

public class MySqliteDatabaseHelper implements SqliteDatabaseHelper {

	/*
	 * Author: Daniel Puckowski
	 */
	
	private static MySqliteDatabaseHelper INSTANCE;
	private static final int              DATABASE_VERSION = 9;
	
	public static final boolean DELETE_EXISTING_DB_FILE      = true;
	public static final boolean KEEP_EXISTING_DB_FILE        = false;
	public static final boolean PRINT_RECORDS_WITH_ROW_ID    = true;
	public static final boolean PRINT_RECORDS_WITHOUT_ROW_ID = false;
	
	public static final String DB_NAME          = "db.videotriage";
	public static final String ABSOLUTE_DB_NAME = FileUtils.DATABASE_DIRECTORY + "db.videotriage";
    
    public static final String TABLE_NAME = "blobs";

    public static final String MMDDYYYY_TIME_STAMP_FIELD = "ddmmyyy_time_stamp";
    public static final String HHMMSS_TIME_STAMP_FIELD   = "hhmmss_time_stamp";
    public static final String FILENAME_FIELD            = "filename";
    public static final String IP_FIELD                  = "ip";
    public static final String PORT_FIELD                = "port";
    
    public static final String MMDDYYYY_TIME_STAMP_INDEX = "ddmmyyyy_time_stamp_index";
    public static final String HHMMSS_TIME_STAMP_INDEX   = "hhmmss_time_stamp_index";
    public static final String FILENAME_INDEX            = "filename_index";
    public static final String IP_INDEX                  = "ip_index";
    public static final String PORT_INDEX                = "port_index";
    
    public MySqliteDatabaseHelper() {
    	
    }
    
    public static MySqliteDatabaseHelper getInstance() {
    	INSTANCE = new MySqliteDatabaseHelper(); 
    	
    	return INSTANCE;
    }
    
    public int getDbVersion() {
    	
    	return DATABASE_VERSION;
    }
    
	public void initDb(final boolean deleteExistingDbFile) throws SqlJetException {
    	File dbFile = getDbFile(ABSOLUTE_DB_NAME);
            	
    	if(dbFile.exists() == true) {    		
    		return;
    	}
    	
    	if(deleteExistingDbFile == true) {
    		deleteDbFile(dbFile);
    	}        
        
        SqlJetDb db = SqlJetDb.open(dbFile, true);
        db.getOptions().setAutovacuum(true);
       
        db.runTransaction(new ISqlJetTransaction() {
            public Object run(SqlJetDb db) throws SqlJetException {
                db.getOptions().setUserVersion(1);
                return true;
            }
        }, SqlJetTransactionMode.WRITE);
        
        db.beginTransaction(SqlJetTransactionMode.WRITE);
        
        try {            
            final String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" + FILENAME_FIELD + " TEXT NOT NULL PRIMARY KEY , " + MMDDYYYY_TIME_STAMP_FIELD + " TEXT NOT NULL, " + HHMMSS_TIME_STAMP_FIELD + " TEXT NOT NULL, " + IP_FIELD + " TEXT NOT NULL, " + PORT_FIELD + " TEXT NOT NULL)";

            final String createTimeStampIndexQuery = "CREATE INDEX " + MMDDYYYY_TIME_STAMP_INDEX + " ON " + TABLE_NAME + "(" +  MMDDYYYY_TIME_STAMP_FIELD + ")";
            final String createFilenameIndexQuery = "CREATE INDEX " + FILENAME_INDEX + " ON " + TABLE_NAME + "(" +  FILENAME_FIELD + ")";
            final String createIpIndexQuery = "CREATE INDEX " + IP_INDEX + " ON " + TABLE_NAME + "(" +  IP_FIELD + ")";
            final String createPortIndexQuery = "CREATE INDEX " + PORT_INDEX + " ON " + TABLE_NAME + "(" +  PORT_FIELD + ")";
            
            db.createTable(createTableQuery);
            
            db.createIndex(createTimeStampIndexQuery);
            db.createIndex(createFilenameIndexQuery);
            db.createIndex(createIpIndexQuery);
            db.createIndex(createPortIndexQuery);
        } finally {
            db.commit();
        }
        
        closeDb(db);
    }
	
	public void deleteDbFile(final File dbFile) {
		SqlJetDb db = null;
		
		try {
			db = getDb(dbFile);
			closeDb(db);
		} catch (SqlJetException sqlJetException) {
		
		}
		
		dbFile.delete();
	}
	
	public File getDbFile(final String dbName) {
		if(dbName.startsWith(FileUtils.DATABASE_DIRECTORY) == false) {
			return new File(FileUtils.DATABASE_DIRECTORY + dbName);
		}
		else {
			return new File(ABSOLUTE_DB_NAME);
		}
    }
    
    private SqlJetDb getDb(final File dbFile) throws SqlJetException {
    	return SqlJetDb.open(dbFile, true);
    }
    
    public SqlJetDb getDb(final String dbName) throws SqlJetException {
    	return getDb(getDbFile(dbName));
    }
    
    public ISqlJetTable getTable(final SqlJetDb db, final String tableName) throws SqlJetException {
    	 return db.getTable(TABLE_NAME);
    }
    
    public void closeDb(final SqlJetDb db) {
    	try {
			db.close();
		} catch (SqlJetException sqlJetException) {
			
		} finally {
			
		}
    }
    
    public void printAllRecordsByPrimaryKey(final SqlJetDb db) throws SqlJetException {
    	ISqlJetTable table = db.getTable(TABLE_NAME);

        db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
        
        try {
            printBlobRecords(table.order(table.getPrimaryKeyIndexName()), PRINT_RECORDS_WITHOUT_ROW_ID);
        } finally {
            db.commit();
        }
        
        closeDb(db);
    }
    
    public void printAllRecordsWithTimeStamp(final SqlJetDb db, final ISqlJetTable table, final String timeStamp) throws SqlJetException {
    	db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
        
    	try {
            printBlobRecords(table.lookup(MMDDYYYY_TIME_STAMP_INDEX, timeStamp), PRINT_RECORDS_WITH_ROW_ID);
        } finally {
            db.commit();
        }
    	
    	closeDb(db);
    }
    
    public ArrayList<BlobRecord> getAllRecordsWithTimeStamp(final SqlJetDb db, final ISqlJetTable table, final String timeStamp) throws SqlJetException {
    	ArrayList<BlobRecord> blobRecords = null; 
    	
    	db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
        
    	try {
            blobRecords = getBlobRecordList(table.lookup(MMDDYYYY_TIME_STAMP_INDEX, timeStamp));
        } finally {
            db.commit();
        }
    	
    	closeDb(db);
    	
    	return blobRecords;
    }
    
    public void printAllRecordsWithFilename(final SqlJetDb db, final ISqlJetTable table, final String filename) throws SqlJetException {
    	db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
        
    	try {
            printBlobRecords(table.lookup(FILENAME_INDEX, filename), PRINT_RECORDS_WITH_ROW_ID);
        } finally {
            db.commit();
        }
    	
    	closeDb(db);
    }
    
    public ArrayList<BlobRecord> getAllRecordsWithFilename(final SqlJetDb db, final ISqlJetTable table, final String filename) throws SqlJetException {
    	ArrayList<BlobRecord> blobRecords = null;
    	
    	db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
        
    	try {
            blobRecords = getBlobRecordList(table.lookup(FILENAME_INDEX, filename));
        } finally {
            db.commit();
        }
    	
    	closeDb(db);
    	
    	return blobRecords;
    }
    
    public void printAllRecordsWithIp(final SqlJetDb db, final ISqlJetTable table, final String ip) throws SqlJetException {
    	db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
        
    	try {
            printBlobRecords(table.lookup(IP_INDEX, ip), PRINT_RECORDS_WITH_ROW_ID);
        } finally {
            db.commit();
        }
    	
    	closeDb(db);
    }
    
    public ArrayList<BlobRecord> getAllRecordsWithIp(final SqlJetDb db, final ISqlJetTable table, final String ip) throws SqlJetException {
    	ArrayList<BlobRecord> blobRecords = null; 
    	
    	db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
        
    	try {
    		blobRecords = getBlobRecordList(table.lookup(IP_INDEX, ip));
        } finally {
            db.commit();
        }
    	
    	closeDb(db);
    	
    	return blobRecords;
    }
    
    public void printAllRecordsWithPort(final SqlJetDb db, final ISqlJetTable table, final String port) throws SqlJetException {
    	db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
        
        try {
            printBlobRecords(table.lookup(PORT_INDEX, port), PRINT_RECORDS_WITH_ROW_ID);
        } finally {
            db.commit();
        }
        
        closeDb(db);
    }
    
    public ArrayList<BlobRecord> getAllRecordsWithPort(final SqlJetDb db, final ISqlJetTable table, final String port) throws SqlJetException {
        ArrayList<BlobRecord> blobRecords = null; 
        
    	db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
        
        try {
        	blobRecords = getBlobRecordList(table.lookup(PORT_INDEX, port));
        } finally {
            db.commit();
        }
        
        closeDb(db);
        
        return blobRecords;
    }
    
    private void printBlobRecords(final ISqlJetCursor cursor, final boolean printRowId) throws SqlJetException {
    	try {
            if (!cursor.eof()) {
                do {
                	if(printRowId == true) {
	                    System.out.println(cursor.getRowId() + " : " + 
	                    		cursor.getString(MMDDYYYY_TIME_STAMP_FIELD) + "\t" + 
	                    		cursor.getString(HHMMSS_TIME_STAMP_FIELD) + "\t" + 
	                    		cursor.getString(FILENAME_FIELD) + "\t" +
	                    		cursor.getString(IP_FIELD) + "\t" + 
	                    		cursor.getString(PORT_FIELD));
                	} 
                	else {
                		System.out.println(cursor.getString(MMDDYYYY_TIME_STAMP_FIELD) + "\t" + 
                        		cursor.getString(HHMMSS_TIME_STAMP_FIELD) + "\t" + 
                        		cursor.getString(FILENAME_FIELD) + "\t" +
                        		cursor.getString(IP_FIELD) + "\t" + 
                        		cursor.getString(PORT_FIELD));
                	}
                } while(cursor.next());
            }
        } finally {
            cursor.close();
        }
    }
    
    private ArrayList<BlobRecord> getBlobRecordList(final ISqlJetCursor cursor) throws SqlJetException {
    	ArrayList<BlobRecord> blobRecords = new ArrayList<BlobRecord>();
        BlobRecord blobRecord = null;
        
    	try {
            if (!cursor.eof()) {
                do {
                	blobRecord = new BlobRecord(cursor.getString(FILENAME_FIELD), cursor.getString(MMDDYYYY_TIME_STAMP_FIELD), cursor.getString(HHMMSS_TIME_STAMP_FIELD), cursor.getString(IP_FIELD), cursor.getString(PORT_FIELD));
                	
                	blobRecords.add(blobRecord);
                } while(cursor.next());
            }
        } finally {
            cursor.close();
        }
    	
    	return blobRecords;
    }
    
    public void insertBlobRecord(final SqlJetDb db, final BlobRecord blobRecord) throws SqlJetException {
    	db.beginTransaction(SqlJetTransactionMode.WRITE);
        
        try {
            ISqlJetTable table = db.getTable(TABLE_NAME);

            table.insert(blobRecord.getFilename(), blobRecord.getMmDdYyyyTimeStamp(), blobRecord.getHhMmSsAndHash(), blobRecord.getIp(), blobRecord.getPort());
        } finally {
            db.commit();
        }
        
        closeDb(db);
    }
    
    public void dropTable(final SqlJetDb db, final String tableNameToDelete) throws SqlJetException {
    	db.beginTransaction(SqlJetTransactionMode.WRITE);
        
        try {
            Set<String> tables = db.getSchema().getTableNames();
            
            for (String tableName : tables) {
                ISqlJetTableDef tableDef = db.getSchema().getTable(tableName);
                
                if(tableDef.getName().equals(tableNameToDelete) == true) {
                	db.dropTable(tableDef.getName());
                	
                	break;
                }
            }
        } finally {
            db.commit();
        }
        
        closeDb(db);
    }
    
    public void dropIndex(final SqlJetDb db, final String indexToDelete) throws SqlJetException {
    	db.beginTransaction(SqlJetTransactionMode.WRITE);
        
        try {
            Set<String> tables = db.getSchema().getTableNames();
            
            for (String tableName : tables) {
                ISqlJetTableDef tableDef = db.getSchema().getTable(tableName);
                Set<ISqlJetIndexDef> tableIndices = db.getSchema().getIndexes(tableDef.getName());
                
                for (ISqlJetIndexDef indexDef : tableIndices) {              
                    if (!indexDef.isImplicit() && indexDef.getName().equals(indexToDelete) == true) {
                        db.dropIndex(indexDef.getName());
                        
                        break; 
                    }
                }
            }
        } finally {
            db.commit();
        }
        
        closeDb(db);
    }
    
    public void dropAllTablesAndIndices(final SqlJetDb db) throws SqlJetException {
    	db.beginTransaction(SqlJetTransactionMode.WRITE);
        
        try {
            Set<String> tables = db.getSchema().getTableNames();
            
            for (String tableName : tables) {
                ISqlJetTableDef tableDef = db.getSchema().getTable(tableName);
                Set<ISqlJetIndexDef> tableIndices = db.getSchema().getIndexes(tableDef.getName());
                
                for (ISqlJetIndexDef indexDef : tableIndices) {              
                    if (!indexDef.isImplicit()) {
                        db.dropIndex(indexDef.getName());
                    }
                }
                
                db.dropTable(tableDef.getName());
            }
        } finally {
            db.commit();
        }
        
        closeDb(db);
    }
}

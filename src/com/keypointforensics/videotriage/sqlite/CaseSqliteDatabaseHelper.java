package com.keypointforensics.videotriage.sqlite;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.schema.ISqlJetIndexDef;
import org.tmatesoft.sqljet.core.schema.ISqlJetTableDef;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.ISqlJetTransaction;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import com.keypointforensics.videotriage.gui.database.AdvancedDatabaseSearchWindow;

public class CaseSqliteDatabaseHelper implements SqliteDatabaseHelper {

	/*
	 * Author: Daniel Puckowski
	 */
	
	public static final SimpleDateFormat DEFAULT_SHORT_DATE_FORMAT = new SimpleDateFormat("MM-dd-yyyy");
	
	private static CaseSqliteDatabaseHelper INSTANCE;
	
	private static final int    DATABASE_VERSION = 9;
	
	public static final boolean DELETE_EXISTING_DB_FILE      = true;
	public static final boolean KEEP_EXISTING_DB_FILE        = false;
	public static final boolean PRINT_RECORDS_WITH_ROW_ID    = true;
	public static final boolean PRINT_RECORDS_WITHOUT_ROW_ID = false;

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
    
	public static String mDatabaseName;

    public CaseSqliteDatabaseHelper(final String databaseName) {
    	mDatabaseName = databaseName;
    }
    
    public static CaseSqliteDatabaseHelper getInstance(final String databaseName) {
    	INSTANCE = new CaseSqliteDatabaseHelper(databaseName); 

    	return INSTANCE;
    }
    
    public int getDbVersion() {
    	
    	return DATABASE_VERSION;
    }
    
	public void initDb(final boolean deleteExistingDbFile) throws SqlJetException {
    	File dbFile = getDbFile(mDatabaseName); 

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
	
	public void deleteDbFile(final String dbName) {
		SqlJetDb db = null;
		File dbFile = getDbFile(dbName);
		
		try {
			db = getDb(dbFile);
			closeDb(db);
		} catch (SqlJetException sqlJetException) {
	
		}
		
		dbFile.delete();
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
    	return new File(dbName);
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
    
    public void closeDb(final String dbName) throws SqlJetException {
    	SqlJetDb db = getDb(getDbFile(dbName));
    	
    	closeDb(db);
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
    	
    	if(timeStamp.contains("*") == false)
    	{
        	db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
        	
	    	try {
	            blobRecords = getBlobRecordList(table.lookup(MMDDYYYY_TIME_STAMP_INDEX, timeStamp));
	        } finally {
	            db.commit();
	        }
    	}
    	else
    	{
    		String timeStampTemp = timeStamp;
    		
    		String monthString = timeStampTemp.substring(0, timeStampTemp.indexOf("-"));
    		timeStampTemp = timeStampTemp.substring(timeStampTemp.indexOf("-") + 1, timeStampTemp.length());
    		
    		String dayString = timeStampTemp.substring(0, timeStampTemp.indexOf("-"));
    		timeStampTemp = timeStampTemp.substring(timeStampTemp.indexOf("-") + 1, timeStampTemp.length());
    		
    		String yearString = timeStampTemp.substring(0, timeStampTemp.length());
    		
    		boolean loopMonth = monthString.equals("*");
    		boolean loopDay = dayString.equals("*");
    		boolean loopYear = yearString.equals("*");
    		    		
    		int i;
    		ArrayList<String> timeStampList = new ArrayList<String>();
    		
    		if(loopMonth == true)
    		{
    			for(i = 1; i <= 12; ++i) 
    			{
    				timeStampList.add(String.format("%02d", i) + "-" + dayString + "-" + yearString);
    			}
    		}
    		
    		if(loopDay == true)
    		{
    			for(i = 1; i <= 31; ++i) 
    			{
    				timeStampList.add(monthString + "-" + String.format("%02d", i) + "-" + yearString);
    			}
    		}
    		
    		if(loopYear == true)
    		{
    			final int currentYear = Calendar.getInstance().get(Calendar.YEAR);
    			
    			//Digital video was first introduced commercially in 1986 with the Sony D1 format
    			for(i = 1986; i <= currentYear; ++i) 
    			{
    				timeStampList.add(monthString + "-" + dayString + "-" + String.valueOf(i));
    			}
    		}
    		
    		boolean replacementMade;

    		do
    		{
        		replacementMade = false;
        		
    			replacementMade = (replacementMade | expandMonthTimeStamps(timeStampList));
    			replacementMade = (replacementMade | expandDayTimeStamps(timeStampList));
    			replacementMade = (replacementMade | expandYearTimeStamps(timeStampList));
    		}
    		while(replacementMade == true);
    		
    		blobRecords = new ArrayList<BlobRecord>();
    		HashSet<String> searchStamps = new HashSet<String>();
    		
    		for(String searchStamp : timeStampList) {
    			searchStamps.add(searchStamp);
    		}
    		
    		timeStampList = null;
    		
    		for(String searchStamp : searchStamps)
    		{
    	    	db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
    	    	
    			try {
    	            blobRecords.addAll(getBlobRecordList(table.lookup(MMDDYYYY_TIME_STAMP_INDEX, searchStamp)));
    	        } finally {   	    
    	            db.commit();
    	        }
    		}
    	}
    	
    	closeDb(db);
    	
    	return blobRecords;
    }
    
    private ArrayList<Date> getListOfDaysBetweenTwoDates(Date startDate, Date endDate) {
        ArrayList<Date> result = new ArrayList<Date>();

        Calendar start = Calendar.getInstance();
        start.setTime(startDate);
        
        Calendar end = Calendar.getInstance();
        end.setTime(endDate);
        end.add(Calendar.DAY_OF_YEAR, 1); //Add 1 day to endDate to make sure endDate is included into the final list
       
        while (start.before(end)) {
            result.add(start.getTime());
            start.add(Calendar.DAY_OF_YEAR, 1);
        }
        
        return result;
    }
    
    private ArrayList<String> getListOfSearchStampsForDates(final String startTimeStamp, final String stopTimeStamp) {
    	ArrayList<String> searchStamps = new ArrayList<String>();
    	    	
    	Date startDate = null;
		try {
			startDate = DEFAULT_SHORT_DATE_FORMAT.parse(startTimeStamp);
		} catch (ParseException parseException) {
			parseException.printStackTrace();
			
			return searchStamps;
		}
		
    	Date stopDate = null;
		try {
			stopDate = DEFAULT_SHORT_DATE_FORMAT.parse(stopTimeStamp);
		} catch (ParseException parseException) {
			parseException.printStackTrace();
			
			return searchStamps;
		}
    	
    	ArrayList<Date> allDates = getListOfDaysBetweenTwoDates(startDate, stopDate);

    	for(Date date : allDates) {
    		searchStamps.add(DEFAULT_SHORT_DATE_FORMAT.format(date));
    	}
    	
    	return searchStamps;
    }
    
    private ArrayList<String> getListOfSearchStampsForDates(final Date startDate, final Date stopDate) {
    	ArrayList<String> searchStamps = new ArrayList<String>();
    	
    	ArrayList<Date> allDates = getListOfDaysBetweenTwoDates(startDate, stopDate);
    	    	
    	for(Date date : allDates) {
    		searchStamps.add(DEFAULT_SHORT_DATE_FORMAT.format(date));
    	}
    	
    	return searchStamps;
    }
    
    public ArrayList<BlobRecord> getTargetedRecordsWithTimeStampByHourFormatted(final SqlJetDb db, final ISqlJetTable table, 
    		final String startTimeStamp, final String stopTimeStamp) throws SqlJetException {
    	ArrayList<BlobRecord> allBlobRecords = new ArrayList<BlobRecord>(); 
    	    	
    	Date startDate = null;
		try {
			startDate = AdvancedDatabaseSearchWindow.ADVANCED_SEARCH_DATE_FORMAT.parse(startTimeStamp);
		} catch (ParseException parseException) {
			parseException.printStackTrace();
			
			return allBlobRecords;
		}
		
    	Date stopDate = null;
		try {
			stopDate = AdvancedDatabaseSearchWindow.ADVANCED_SEARCH_DATE_FORMAT.parse(stopTimeStamp);
		} catch (ParseException parseException) {
			parseException.printStackTrace();
			
			return allBlobRecords;
		}
		
		String searchStartTimeStamp = startTimeStamp.substring(0, startTimeStamp.indexOf(" "));
		String searchStopTimeStamp = stopTimeStamp.substring(0, stopTimeStamp.indexOf(" "));
		
    	ArrayList<String> searchStamps = getListOfSearchStampsForDates(searchStartTimeStamp, searchStopTimeStamp);
    		
	    for(String searchStamp : searchStamps)
    	{
    	    db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
    	    	
    		try {
    			allBlobRecords.addAll(getBlobRecordListWithinDateRange(table.lookup(MMDDYYYY_TIME_STAMP_INDEX, searchStamp), startDate, stopDate));
    	    } finally {   	    
    	    	db.commit();
    	    }
    	}
    	
    	for(BlobRecord blobRecord : allBlobRecords) {
    		blobRecord.formatTimeStamp();
    	}
    	
    	closeDb(db);
    	
    	return allBlobRecords;
    }
    
    public ArrayList<BlobRecord> getTargetedRecordsWithTimeStampFormatted(final SqlJetDb db, final ISqlJetTable table, 
    		final String startTimeStamp, final String stopTimeStamp) throws SqlJetException {
    	ArrayList<BlobRecord> allBlobRecords = new ArrayList<BlobRecord>(); 
    	
    	ArrayList<String> searchStamps = getListOfSearchStampsForDates(startTimeStamp, stopTimeStamp);
    		
	    for(String searchStamp : searchStamps)
    	{
    	    db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
    	    	
    		try {
    			allBlobRecords.addAll(getBlobRecordList(table.lookup(MMDDYYYY_TIME_STAMP_INDEX, searchStamp)));
    			
    	    } finally {   	    
    	    	db.commit();
    	    }
    	}
    	
    	for(BlobRecord blobRecord : allBlobRecords) {
    		blobRecord.formatTimeStamp();
    	}
    	
    	closeDb(db);
    	
    	return allBlobRecords;
    }
    
    public ArrayList<BlobRecord> getAllRecordsWithTimeStampByHourFormatted(final SqlJetDb db, final ISqlJetTable table, final String timeStamp) throws SqlJetException {
    	ArrayList<BlobRecord> blobRecords = null; 
    	
    	if(timeStamp.contains("*") == false)
    	{
        	db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
        	
	    	try {
	            blobRecords = getBlobRecordList(table.lookup(HHMMSS_TIME_STAMP_INDEX, timeStamp));
	        } finally {
	            db.commit();
	        }
    	}
    	else
    	{
    		String timeStampTemp = timeStamp;
    		
    		String hourString = timeStampTemp.substring(0, timeStampTemp.indexOf("-"));
    		timeStampTemp = timeStampTemp.substring(timeStampTemp.indexOf("-") + 1, timeStampTemp.length());
    		
    		String minuteString = timeStampTemp.substring(0, timeStampTemp.indexOf("-"));
    		timeStampTemp = timeStampTemp.substring(timeStampTemp.indexOf("-") + 1, timeStampTemp.length());
    		
    		String secondString = timeStampTemp.substring(0, timeStampTemp.length());
    		
    		boolean loopHour = hourString.equals("*");
    		boolean loopMinute = minuteString.equals("*");
    		boolean loopSecond = secondString.equals("*");
    		    		
    		int i;
    		ArrayList<String> timeStampList = new ArrayList<String>();
    		
    		if(loopHour == true)
    		{
    			for(i = 0; i <= 23; ++i) 
    			{
    				timeStampList.add(String.format("%02d", i) + "-" + minuteString + "-" + secondString);
    			}
    		}
    		
    		if(loopMinute == true)
    		{
    			for(i = 0; i <= 59; ++i) 
    			{
    				timeStampList.add(hourString + "-" + String.format("%02d", i) + "-" + secondString);
    			}
    		}
    		
    		if(loopSecond == true)
    		{    			
    			for(i = 0; i <= 59; ++i) 
    			{
    				timeStampList.add(hourString + "-" + minuteString + "-" + String.format("%02d", i));
    			}
    		}
    		
    		boolean replacementMade;

    		do
    		{
        		replacementMade = false;
        		
    			replacementMade = (replacementMade | expandHourTimeStamps(timeStampList));
    			replacementMade = (replacementMade | expandMinuteTimeStamps(timeStampList));
    			replacementMade = (replacementMade | expandSecondTimeStamps(timeStampList));
    		}
    		while(replacementMade == true);
    		
    		blobRecords = new ArrayList<BlobRecord>();
    		HashSet<String> searchStamps = new HashSet<String>();
    		
    		for(String searchStamp : timeStampList) {
    			searchStamps.add(searchStamp);
    		}
    		
    		timeStampList = null;
    		
    		for(String searchStamp : searchStamps)
    		{
    	    	db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
    	    	
    			try {
    	            blobRecords.addAll(getBlobRecordList(table.lookup(HHMMSS_TIME_STAMP_INDEX, searchStamp)));
    	        } finally {   	    
    	            db.commit();
    	        }
    		}
    	}
    	
    	for(BlobRecord blobRecord : blobRecords) {
    		blobRecord.formatTimeStamp();
    	}
    	
    	closeDb(db);
    	
    	return blobRecords;
    }
    
    public ArrayList<BlobRecord> getAllRecordsWithTimeStampFormatted(final SqlJetDb db, final ISqlJetTable table, final String timeStamp) throws SqlJetException {
    	ArrayList<BlobRecord> blobRecords = null; 
    	
    	if(timeStamp.contains("*") == false)
    	{
        	db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
        	
	    	try {
	            blobRecords = getBlobRecordList(table.lookup(MMDDYYYY_TIME_STAMP_INDEX, timeStamp));
	        } finally {
	            db.commit();
	        }
    	}
    	else
    	{
    		String timeStampTemp = timeStamp;
    		
    		String monthString = timeStampTemp.substring(0, timeStampTemp.indexOf("-"));
    		timeStampTemp = timeStampTemp.substring(timeStampTemp.indexOf("-") + 1, timeStampTemp.length());
    		
    		String dayString = timeStampTemp.substring(0, timeStampTemp.indexOf("-"));
    		timeStampTemp = timeStampTemp.substring(timeStampTemp.indexOf("-") + 1, timeStampTemp.length());
    		
    		String yearString = timeStampTemp.substring(0, timeStampTemp.length());
    		
    		boolean loopMonth = monthString.equals("*");
    		boolean loopDay = dayString.equals("*");
    		boolean loopYear = yearString.equals("*");
    		    		
    		int i;
    		ArrayList<String> timeStampList = new ArrayList<String>();
    		
    		if(loopMonth == true)
    		{
    			for(i = 1; i <= 12; ++i) 
    			{
    				timeStampList.add(String.format("%02d", i) + "-" + dayString + "-" + yearString);
    			}
    		}
    		
    		if(loopDay == true)
    		{
    			for(i = 1; i <= 31; ++i) 
    			{
    				timeStampList.add(monthString + "-" + String.format("%02d", i) + "-" + yearString);
    			}
    		}
    		
    		if(loopYear == true)
    		{
    			final int currentYear = Calendar.getInstance().get(Calendar.YEAR);
    			
    			//Digital video was first introduced commercially in 1986 with the Sony D1 format
    			for(i = 1986; i <= currentYear; ++i) 
    			{
    				timeStampList.add(monthString + "-" + dayString + "-" + String.valueOf(i));
    			}
    		}
    		
    		boolean replacementMade;

    		do
    		{
        		replacementMade = false;
        		
    			replacementMade = (replacementMade | expandMonthTimeStamps(timeStampList));
    			replacementMade = (replacementMade | expandDayTimeStamps(timeStampList));
    			replacementMade = (replacementMade | expandYearTimeStamps(timeStampList));
    		}
    		while(replacementMade == true);
    		
    		blobRecords = new ArrayList<BlobRecord>();
    		HashSet<String> searchStamps = new HashSet<String>();
    		
    		for(String searchStamp : timeStampList) {
    			searchStamps.add(searchStamp);
    		}
    		
    		timeStampList = null;
    		
    		for(String searchStamp : searchStamps)
    		{
    	    	db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
    	    	
    			try {
    	            blobRecords.addAll(getBlobRecordList(table.lookup(MMDDYYYY_TIME_STAMP_INDEX, searchStamp)));
    	        } finally {   	    
    	            db.commit();
    	        }
    		}
    	}
    	
    	for(BlobRecord blobRecord : blobRecords) {
    		blobRecord.formatTimeStamp();
    	}
    	
    	closeDb(db);
    	
    	return blobRecords;
    }
    
    private String getDatePaddedString(String datePiece)
    {
    	if(datePiece.equals("*") == true) 
    	{
    		return datePiece;
    	}
    	else if(datePiece.length() == 1)
    	{
    		return "0" + datePiece;
    	}
    	else
    	{
    		return datePiece;
    	}
    }
    
    private boolean expandDayTimeStamps(final ArrayList<String> timeStampList)
    {
    	boolean madeReplacement = false;
    	String timeStampTemp;    	
    	int n;
    	
    	for(int i = 0; i < timeStampList.size(); ++i) 
    	{
    		timeStampTemp = timeStampList.get(i);
    		
    		String monthString = timeStampTemp.substring(0, timeStampTemp.indexOf("-"));
    		timeStampTemp = timeStampTemp.substring(timeStampTemp.indexOf("-") + 1, timeStampTemp.length());
    		
    		String dayString = timeStampTemp.substring(0, timeStampTemp.indexOf("-"));
    		timeStampTemp = timeStampTemp.substring(timeStampTemp.indexOf("-") + 1, timeStampTemp.length());
    		
    		String yearString = timeStampTemp.substring(0, timeStampTemp.length());
    		
    		if(dayString.equals("*") == true)
    		{  
    			timeStampList.remove(i);
    			i--;
    			
    			madeReplacement = true;
    			
    			for(n = 1; n <= 31; ++n) 
    			{
    				timeStampList.add(getDatePaddedString(monthString) + "-" + getDatePaddedString(String.valueOf(n)) + "-" + yearString);
    			}
    		}
    	}

    	return madeReplacement;
    }
    
    private boolean expandMonthTimeStamps(final ArrayList<String> timeStampList)
    {
    	boolean madeReplacement = false;
    	String timeStampTemp;    	
    	int n;
    	
    	for(int i = 0; i < timeStampList.size(); ++i) 
    	{
    		timeStampTemp = timeStampList.get(i);
    		
    		String monthString = timeStampTemp.substring(0, timeStampTemp.indexOf("-"));
    		timeStampTemp = timeStampTemp.substring(timeStampTemp.indexOf("-") + 1, timeStampTemp.length());
    		
    		String dayString = timeStampTemp.substring(0, timeStampTemp.indexOf("-"));
    		timeStampTemp = timeStampTemp.substring(timeStampTemp.indexOf("-") + 1, timeStampTemp.length());
    		
    		String yearString = timeStampTemp.substring(0, timeStampTemp.length());
    		
    		if(monthString.equals("*") == true)
    		{  
    			timeStampList.remove(i);
    			i--;
    			
    			madeReplacement = true;
    			
    			for(n = 1; n <= 12; ++n) 
    			{
    				timeStampList.add(getDatePaddedString(String.valueOf(n)) + "-" + getDatePaddedString(dayString) + "-" + yearString);
    			}
    		}
    	}

    	return madeReplacement;
    }
    
    private boolean expandYearTimeStamps(final ArrayList<String> timeStampList)
    {
    	boolean madeReplacement = false;
    	String timeStampTemp;    	
    	int n;
    	
		final int currentYear = Calendar.getInstance().get(Calendar.YEAR);

    	for(int i = 0; i < timeStampList.size(); ++i) 
    	{
    		timeStampTemp = timeStampList.get(i);
    		
    		String monthString = timeStampTemp.substring(0, timeStampTemp.indexOf("-"));
    		timeStampTemp = timeStampTemp.substring(timeStampTemp.indexOf("-") + 1, timeStampTemp.length());
    		
    		String dayString = timeStampTemp.substring(0, timeStampTemp.indexOf("-"));
    		timeStampTemp = timeStampTemp.substring(timeStampTemp.indexOf("-") + 1, timeStampTemp.length());
    		
    		String yearString = timeStampTemp.substring(0, timeStampTemp.length());
    		
    		if(yearString.equals("*") == true)
    		{  
    			timeStampList.remove(i);
    			i--;
    			
    			madeReplacement = true;
    		    		
    			//Digital video was first introduced commercially in 1986 with the Sony D1 format
    			for(n = 1986; n <= currentYear; ++n) 
    			{
    				timeStampList.add(monthString + "-" + dayString + "-" + String.valueOf(n));
    			}
    		}
    	}

    	return madeReplacement;
    }
    
    private boolean expandMinuteTimeStamps(final ArrayList<String> timeStampList)
    {
    	boolean madeReplacement = false;
    	String timeStampTemp;    	
    	int n;
    	
    	for(int i = 0; i < timeStampList.size(); ++i) 
    	{
    		timeStampTemp = timeStampList.get(i);
    		
    		String hourString = timeStampTemp.substring(0, timeStampTemp.indexOf("-"));
    		timeStampTemp = timeStampTemp.substring(timeStampTemp.indexOf("-") + 1, timeStampTemp.length());
    		
    		String minuteString = timeStampTemp.substring(0, timeStampTemp.indexOf("-"));
    		timeStampTemp = timeStampTemp.substring(timeStampTemp.indexOf("-") + 1, timeStampTemp.length());
    		
    		String secondString = timeStampTemp.substring(0, timeStampTemp.length());
    		
    		if(minuteString.equals("*") == true)
    		{  
    			timeStampList.remove(i);
    			i--;
    			
    			madeReplacement = true;
    			
    			for(n = 0; n <= 59; ++n) 
    			{
    				timeStampList.add(getDatePaddedString(hourString) + "-" + String.format("%02d", n) + "-" + getDatePaddedString(secondString));
    			}
    		}
    	}

    	return madeReplacement;
    }
    
    private boolean expandHourTimeStamps(final ArrayList<String> timeStampList)
    {
    	boolean madeReplacement = false;
    	String timeStampTemp;    	
    	int n;
    	
    	for(int i = 0; i < timeStampList.size(); ++i) 
    	{
    		timeStampTemp = timeStampList.get(i);
    		
    		String hourString = timeStampTemp.substring(0, timeStampTemp.indexOf("-"));
    		timeStampTemp = timeStampTemp.substring(timeStampTemp.indexOf("-") + 1, timeStampTemp.length());
    		
    		String minuteString = timeStampTemp.substring(0, timeStampTemp.indexOf("-"));
    		timeStampTemp = timeStampTemp.substring(timeStampTemp.indexOf("-") + 1, timeStampTemp.length());
    		
    		String secondString = timeStampTemp.substring(0, timeStampTemp.length());
    		
    		if(hourString.equals("*") == true)
    		{  
    			timeStampList.remove(i);
    			i--;
    			
    			madeReplacement = true;
    			
    			for(n = 0; n <= 23; ++n) 
    			{
    				timeStampList.add(String.format("%02d", n) + "-" + getDatePaddedString(minuteString) + "-" + getDatePaddedString(secondString));
    			}
    		}
    	}

    	return madeReplacement;
    }
    
    private boolean expandSecondTimeStamps(final ArrayList<String> timeStampList)
    {
    	boolean madeReplacement = false;
    	String timeStampTemp;    	
    	int n;
    	
    	for(int i = 0; i < timeStampList.size(); ++i) 
    	{
    		timeStampTemp = timeStampList.get(i);
    		
    		String hourString = timeStampTemp.substring(0, timeStampTemp.indexOf("-"));
    		timeStampTemp = timeStampTemp.substring(timeStampTemp.indexOf("-") + 1, timeStampTemp.length());
    		
    		String minuteString = timeStampTemp.substring(0, timeStampTemp.indexOf("-"));
    		timeStampTemp = timeStampTemp.substring(timeStampTemp.indexOf("-") + 1, timeStampTemp.length());
    		
    		String secondString = timeStampTemp.substring(0, timeStampTemp.length());
    		
    		if(secondString.equals("*") == true)
    		{  
    			timeStampList.remove(i);
    			i--;
    			
    			madeReplacement = true;
    		    			
    			for(n = 0; n <= 59; ++n) 
    			{
    				timeStampList.add(getDatePaddedString(hourString) + "-" + getDatePaddedString(minuteString) + "-" + String.format("%02d", n));
    			}
    		}
    	}

    	return madeReplacement;
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
    
    public ArrayList<BlobRecord> getAllRecords(final SqlJetDb db, final ISqlJetTable table, final boolean formatTimestamps) throws SqlJetException {        
        ArrayList<BlobRecord> blobRecords = null; 
        
    	db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
        
        try {
        	blobRecords = getBlobRecordList(table.open());
        } finally {
            db.commit();
        }
        
        closeDb(db);
        
        if(formatTimestamps == true) {
        	for(BlobRecord blobRecord : blobRecords) {
        		blobRecord.formatTimeStamp();
        	}
        }
        
        return blobRecords;
    }
    
    public ArrayList<BlobRecord> getAllRecords(final SqlJetDb db, final ISqlJetTable table) throws SqlJetException {        
        ArrayList<BlobRecord> blobRecords = null; 
        
    	db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
        
        try {
        	blobRecords = getBlobRecordList(table.open());
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
   
    private boolean isWithinDateRange(final Date startDate, final Date stopDate, final SimpleDateFormat simpleDateFormat, String testDateString) {
    	Date testDate = null;
    	
		try {
			testDate = simpleDateFormat.parse(testDateString);
		} catch (ParseException parseException) {
			parseException.printStackTrace();
			
			return false;
		}
		
		return isWithinDateRange(startDate, stopDate, testDate);
    }
    
    private boolean isWithinDateRange(final Date startDate, final Date stopDate, Date testDate) {
    	return !(testDate.before(startDate) || testDate.after(stopDate));
    }
    
    private ArrayList<BlobRecord> getBlobRecordListWithinDateRange(final ISqlJetCursor cursor, final Date startDate, final Date stopDate) throws SqlJetException {
    	ArrayList<BlobRecord> blobRecords = new ArrayList<BlobRecord>();
        BlobRecord blobRecord = null;
                
    	try {
            if (!cursor.eof()) {
                do {
                	blobRecord = new BlobRecord(cursor.getString(FILENAME_FIELD), cursor.getString(MMDDYYYY_TIME_STAMP_FIELD), cursor.getString(HHMMSS_TIME_STAMP_FIELD), cursor.getString(IP_FIELD), cursor.getString(PORT_FIELD));
                	
                	if(isWithinDateRange(startDate, stopDate, AdvancedDatabaseSearchWindow.ADVANCED_SEARCH_DATE_FORMAT, blobRecord.getDateString()) == true)  {
                		blobRecords.add(blobRecord);
                	}
                } while(cursor.next());
            }
        } finally {
            cursor.close();
        }
    	
    	return blobRecords;
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

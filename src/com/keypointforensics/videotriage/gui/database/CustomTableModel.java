package com.keypointforensics.videotriage.gui.database;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import com.keypointforensics.videotriage.sqlite.BlobRecord;

public class CustomTableModel extends AbstractTableModel {

	/*
	 * Author: Daniel Puckowski
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1270810636203320124L;

	private final ArrayList<BlobRecord> BLOB_RECORDS;
	
	private String[]   mColumnNames     = TableColumn.getNames();
	private Class<?>[] mColumnClasses   = TableConstants.COLUMN_CLASSES;

	public CustomTableModel(final ArrayList<BlobRecord> blobRecords) {
		BLOB_RECORDS = blobRecords;
	}

	public int getColumnCount() {
		return TableConstants.COLUMN_CLASSES.length;
	}

	public int getRowCount() {
		return BLOB_RECORDS.size();
	}

	public String getColumnName(final int col) {
		return mColumnNames[col];
	}

	public Class<?> getColumnClass(final int col) {
		return mColumnClasses[col];
	}

	public Object getValueAt(final int row, final int col) {
		if(BLOB_RECORDS.isEmpty() == true) {			
			return null;
		}
		else if(row < 0) {			
			return null;
		}
		else if(col < 0) {			
			return null;
		}
		
		final TableColumn tableColumn = TableColumn.fromIndex(col);
				
		switch (tableColumn) {
		case FILENAME:
			
			return BLOB_RECORDS.get(row).getFilename();
		case TIME_STAMP:
			
			return BLOB_RECORDS.get(row).getTimeStampLong(); 
		case IP_ADDRESS:
			
			return BLOB_RECORDS.get(row).getIp();
		case PORT:
			
			return BLOB_RECORDS.get(row).getPort();
		default:			
			return null;
		}
	}

}
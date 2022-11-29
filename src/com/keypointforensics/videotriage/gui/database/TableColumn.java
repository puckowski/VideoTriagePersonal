package com.keypointforensics.videotriage.gui.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum TableColumn {

	/*
	 * Author: Daniel Puckowski
	 */
	
	FILENAME(0, "Filename"), 
	TIME_STAMP(1, "Time Stamp"), 
	IP_ADDRESS(2, "Source"), 
	PORT(3, "Port");

	private static final Map<Integer, TableColumn> COLUMN_INDEX_NAME_MAP = new HashMap<>();
	private static final List<String>              NAMES = new ArrayList<>();
	
	private int    mIndex;
	private String mName;
	
	private TableColumn(final int index, final String name) {
		mIndex = index;
		mName  = name;
	}

	static {
		for (TableColumn column : TableColumn.values()) {
			COLUMN_INDEX_NAME_MAP.put(column.mIndex, column);
			NAMES.add(column.mName);
		}
	}

	public static TableColumn fromIndex(int colIndex) {
		TableColumn columnName = COLUMN_INDEX_NAME_MAP.get(colIndex);
		
		return (columnName != null) ? columnName : null;
	}

	public static String[] getNames() {
		return NAMES.toArray(new String[NAMES.size()]);
	}
}

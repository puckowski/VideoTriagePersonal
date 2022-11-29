package com.keypointforensics.videotriage.sqlite;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

public interface SqliteDatabaseHelper {

	/*
	 * Author: Daniel Puckowski
	 */
	
	public int getDbVersion();
	public void initDb(final boolean deleteExistingDbFile) throws SqlJetException;
	public SqlJetDb getDb(final String dbName) throws SqlJetException;
	public void closeDb(final SqlJetDb db);
	public void dropTable(final SqlJetDb db, final String tableNameToDelete) throws SqlJetException;
	public void dropIndex(final SqlJetDb db, final String indexToDelete) throws SqlJetException;
	public void dropAllTablesAndIndices(final SqlJetDb db) throws SqlJetException;
}

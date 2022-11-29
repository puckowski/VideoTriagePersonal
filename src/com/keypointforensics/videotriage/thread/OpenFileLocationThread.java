package com.keypointforensics.videotriage.thread;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import javax.swing.JTable;

import com.keypointforensics.videotriage.gui.database.DatabaseBrowseWindow;
import com.keypointforensics.videotriage.util.CursorUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;

public class OpenFileLocationThread extends Thread {

	/*
	 * Author: Daniel Puckowski
	 */
	
	private static final int INVALID_SELECTION = -1;
	
	private final JTable         RESULT_TABLE;
	private final DatabaseBrowseWindow DB_BROWSE_WINDOW;
	
	public OpenFileLocationThread(final DatabaseBrowseWindow dbBrowseWindow, final JTable resultTable) {
		RESULT_TABLE     = resultTable;
		DB_BROWSE_WINDOW = dbBrowseWindow;
	}
	
	@Override
	public void run() {
		ThreadUtils.addThreadToHandleList("OpenFileLoc Run", this);
		
		if (RESULT_TABLE.getRowCount() == 0) {			
			ThreadUtils.removeThreadFromHandleList(this);
			
			return;
		}
		else if(RESULT_TABLE.getSelectedRow() == INVALID_SELECTION) {			
			ThreadUtils.removeThreadFromHandleList(this);
			
			return;
		}
		else if(RESULT_TABLE.getColumnCount() == 0) {
			ThreadUtils.removeThreadFromHandleList(this);
			
			return;
		}
		else if(RESULT_TABLE.getSelectedColumn() == INVALID_SELECTION) {			
			ThreadUtils.removeThreadFromHandleList(this);
			
			return;
		}
		
		String filename = (String) RESULT_TABLE.getValueAt(RESULT_TABLE.getSelectedRow(), 0); 
		
		CursorUtils.setBusyCursor(DB_BROWSE_WINDOW.getRootPane());
				
		filename = filename.substring(0, filename.lastIndexOf(File.separator));
		final File file = new File(filename);
		final Desktop desktop = Desktop.getDesktop();
		
		try {
			desktop.browse(file.toURI());
		} catch (IOException ioException) {

		}
		
		CursorUtils.setDefaultCursor(DB_BROWSE_WINDOW.getRootPane());
		
		ThreadUtils.removeThreadFromHandleList(this);
	}
}

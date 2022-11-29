package com.keypointforensics.videotriage.report;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import com.keypointforensics.videotriage.util.WindowUtils;

public class ZipFileDropList extends JPanel {

	/*
	 * Author: Daniel Puckowski
	 */
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 2460800562707723383L;
	
	class IconDecoratedListModel<T> extends DefaultListModel<T> {
	    public void update(int index) {
	        fireContentsChanged(this, index, index);
	    }
	}
	
	private IconDecoratedListModel listModel = new IconDecoratedListModel();
    private JScrollPane jScrollPane1;
    private JList list;
    
    private boolean mIsEmpty;
    
    private final ZipFileListCellRenderer mCellRenderer;
	
    /**
     * Create the panel.
     */
    public ZipFileDropList() {    	
    	this.setLayout(new BorderLayout());
    	
        list = new JList();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(10);
        list.setModel(listModel);
        list.setDragEnabled(true);
        mCellRenderer = new ZipFileListCellRenderer();
        list.setCellRenderer(mCellRenderer);
        jScrollPane1 = new JScrollPane(list);
        WindowUtils.setScrollBarIncrement(jScrollPane1);
        
        this.add(new JLabel("Files To Process"), BorderLayout.NORTH);
        add(jScrollPane1, BorderLayout.CENTER);
        
        checkIfEmpty();
    }
	
    public String getSelectedReportAbsolutePath() {
    	int selectedIndex = list.getSelectedIndex();
    	
    	if(selectedIndex >= 0) {
    		return ((File) listModel.get(selectedIndex)).getAbsolutePath();
    	} else {
    		return null;
    	}
    }
    
    private void clearIfEmpty() {
    	if(mIsEmpty == true) {
    		listModel.removeAllElements();
    	}
    	
    	list.revalidate(); 
    }
    
    private void checkIfEmpty() {
    	if(listModel.isEmpty() == true) {
    		mIsEmpty = true;
    		listModel.addElement(new JLabel("No reports created yet. Click \"Create\" in the \"Report\" menu to create reports."));
    	}
    	else {
    		mIsEmpty = false;
    	}
    	
    	list.revalidate(); 
    }
    
    private volatile boolean mRevalidatingListModel;
    
    public void addFile(final String absoluteFilePath) {										
		clearIfEmpty();

		listModel.addElement(new File(absoluteFilePath));
		
		checkIfEmpty();
    }
    
    public void clearList() {
    	listModel.removeAllElements();
    	list.revalidate(); 
    }
 
}
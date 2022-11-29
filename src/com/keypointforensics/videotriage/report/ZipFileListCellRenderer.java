package com.keypointforensics.videotriage.report;

import java.awt.Color;
import java.awt.Component;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.filechooser.FileSystemView;

public class ZipFileListCellRenderer extends DefaultListCellRenderer {

    private static final long serialVersionUID = -7799441088157759804L;
    
    private static final boolean SHOW_ICON_PREVIEWS_DEFAULT = true;
    
    private FileSystemView fileSystemView;
    private JLabel label;
    private Color mOriginalColor;
        
    private boolean mShowIconPreviews;
    
    public ZipFileListCellRenderer() {	
        label = new JLabel();
        mOriginalColor = label.getBackground();
        label.setOpaque(true);
        fileSystemView = FileSystemView.getFileSystemView();
        
        mShowIconPreviews = SHOW_ICON_PREVIEWS_DEFAULT;
    }

    public void setShowIconPreviews(final boolean showIconPreviews) {
    	mShowIconPreviews = showIconPreviews;
    }
    
    public boolean getShowIconPreviews() {
    	return mShowIconPreviews;
    }
    
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean selected, boolean expanded) {
    	super.getListCellRendererComponent(list, value, index, selected, expanded);
    	
    	if(value instanceof JLabel) {
    		return (JLabel) value;
    	}
    	else if(value instanceof File) {
	        File file = (File) value;
	        
	        label.setIcon(fileSystemView.getSystemIcon(file));    
	        label.setText(file.getAbsolutePath());
	        label.setToolTipText(file.getName());
	
	        if (selected) {
	            label.setBackground(Color.WHITE);
		        label.setBorder(BorderFactory.createDashedBorder(new Color(173, 173, 173), 2, 2));
	        }
	        else {
	            label.setBackground(mOriginalColor);
	            label.setBorder(null);
	        }
	        
	        return label;
    	}
    	else {    		
    		return null;
    	}
    }
}
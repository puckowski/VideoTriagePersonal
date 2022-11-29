package com.keypointforensics.videotriage.image.match;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.filechooser.FileSystemView;

import com.keypointforensics.videotriage.gui.imagepanel.ScalableSimpleImagePanel;
import com.keypointforensics.videotriage.legacy.WindowsVideoFrameExtractorLegacy;
import com.keypointforensics.videotriage.util.ImageUtils;

public class CaptureFileListCellRenderer extends DefaultListCellRenderer {

    private static final long serialVersionUID = -7799441088157759804L;
    
    private static final boolean SHOW_ICON_PREVIEWS_DEFAULT = true;
    
    private FileSystemView fileSystemView;
    private JLabel label;
    private Color mOriginalColor;
    
    private final ScalableSimpleImagePanel PREVIEW_PANEL;
    
    private boolean mShowIconPreviews;
    
    public CaptureFileListCellRenderer(final ScalableSimpleImagePanel previewPanel) {
    	PREVIEW_PANEL = previewPanel;
    	
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
	        BufferedImage previewBufferedImage = null;
	        
	        if(mShowIconPreviews == true) {
	        	previewBufferedImage = ImageUtils.loadBufferedImage(file.getAbsolutePath());
		        
		        if(previewBufferedImage != null) { 
		        	label.setIcon(new ImageIcon(ImageUtils.getScaledImageWithAspectRatio(previewBufferedImage, 100, 50)));
		        	label.setPreferredSize(new Dimension(200, 50));
		        }
	        }
	        else {
	        	label.setIcon(fileSystemView.getSystemIcon(file));
	        }
	        
	        label.setText(file.getAbsolutePath());
	        label.setToolTipText(file.getName());
	
	        if (selected) {
	            label.setBackground(Color.WHITE);
	            
	            if(previewBufferedImage != null) {
	            	PREVIEW_PANEL.update(previewBufferedImage);
	            }
	            else {
	            	final String videoFilePath = WindowsVideoFrameExtractorLegacy.checkExtractedPreviewFrame(file.getAbsolutePath());
			        
			        if(videoFilePath != null) {
			        	previewBufferedImage = ImageUtils.loadBufferedImage(videoFilePath);
			        }
			        
			        PREVIEW_PANEL.update(previewBufferedImage);
	            }
	            
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
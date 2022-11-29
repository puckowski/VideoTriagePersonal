package com.keypointforensics.videotriage.gui.main;

import javax.swing.*;

import com.keypointforensics.videotriage.util.ImageUtils;

import java.awt.*;
import java.beans.*;
import java.io.File;

public class FileSelectImagePreviewAccessory extends JPanel implements PropertyChangeListener {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -7981171105626979139L;

    private final int IMAGE_PREVIEW_SIZE = 240;

	private final Color BACKGROUND_COLOR;
	
	private int mWidth;
	private int mHeight;
    private Image mImage;
    
    public FileSelectImagePreviewAccessory() {
        setPreferredSize(new Dimension(IMAGE_PREVIEW_SIZE, -1));
        BACKGROUND_COLOR = getBackground();
    }
    
    public void propertyChange(PropertyChangeEvent e) {
        String propertyName = e.getPropertyName();
       
        if (propertyName.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
            File selection = (File)e.getNewValue();
            String name;
            
            if (selection == null)
                return;
            else
                name = selection.getAbsolutePath();
            	
            if(name != null) {
            	mImage = ImageUtils.loadBufferedImage(name);
	            scaleImage();
	            repaint();
            }
        }
    }
    
    private void scaleImage() {
    	if(mImage == null) {
    		return;
    	}
    	
        mWidth = mImage.getWidth(this);
        mHeight = mImage.getHeight(this);
        double ratio = 1.0;
       
        if (mWidth >= mHeight) {
            ratio = (double) (IMAGE_PREVIEW_SIZE - 5) / mWidth;
            mWidth = IMAGE_PREVIEW_SIZE - 5;
            mHeight = (int) (mHeight * ratio);
        }
        else {
            if (getHeight() > IMAGE_PREVIEW_SIZE) {
                ratio = (double) (IMAGE_PREVIEW_SIZE - 5) / mHeight;
                mHeight = IMAGE_PREVIEW_SIZE - 5;
                mWidth = (int) (mWidth * ratio);
            }
            else {
                ratio = (double) getHeight() / mHeight;
                mHeight = getHeight();
                mWidth = (int) (mWidth * ratio);
            }
        }
                
        mImage = mImage.getScaledInstance(mWidth, mHeight, Image.SCALE_DEFAULT);
    }
    
    public void paintComponent(Graphics graphics) {
        graphics.setColor(BACKGROUND_COLOR);
        graphics.fillRect(0, 0, getWidth(), getHeight());
        graphics.drawImage(mImage, getWidth() / 2 - mWidth / 2 + 5, getHeight() / 2 - mHeight / 2, this);
    } 
}
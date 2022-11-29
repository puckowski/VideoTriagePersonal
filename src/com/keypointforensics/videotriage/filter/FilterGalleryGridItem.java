package com.keypointforensics.videotriage.filter;

import java.awt.BorderLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.keypointforensics.videotriage.gui.imagepanel.FilterGalleryImagePanel;
import com.keypointforensics.videotriage.util.ImageUtils;

public class FilterGalleryGridItem extends JPanel {

	/*
	 * Author: Daniel Puckowski
	 */

	/**
	 * 
	 */
	private static final long serialVersionUID = 8487543902860460807L;

	private final String ORIGINAL_IMAGE_ABSOLUTE_PATH;

	private FilterGalleryImagePanel mGalleryImagePanel;
	private JCheckBox mSelectedCheckBox;

	public FilterGalleryGridItem(final String imageAbsolutePath, final boolean scaleToFit) {
		ORIGINAL_IMAGE_ABSOLUTE_PATH = imageAbsolutePath;
		
		mSelectedCheckBox = new JCheckBox("Check Image");
		mGalleryImagePanel = new FilterGalleryImagePanel(imageAbsolutePath, scaleToFit);
		
		buildPanel();
	}

	public void update() {
		mGalleryImagePanel.update(ImageUtils.loadBufferedImage(ORIGINAL_IMAGE_ABSOLUTE_PATH));
		this.revalidate();
	}
	
	public void update(final String imageAbsolutePath) {
		mGalleryImagePanel.update(ImageUtils.loadBufferedImage(imageAbsolutePath));
		//mImageAbsolutePath = imageAbsolutePath;
		this.revalidate();
	}
	
	public String getImageAbsolutePath() {
		return ORIGINAL_IMAGE_ABSOLUTE_PATH;
	}

	public boolean isSelected() {
		return mSelectedCheckBox.isSelected();
	}

	public void setSelected(final boolean newSelectedState) {
		mSelectedCheckBox.setSelected(newSelectedState);
	}

	private void buildPanel() {
		this.setLayout(new BorderLayout());

		this.add(mGalleryImagePanel, BorderLayout.CENTER);
		this.add(mSelectedCheckBox, BorderLayout.SOUTH);
	}
}

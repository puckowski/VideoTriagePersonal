package com.keypointforensics.videotriage.report;

import java.awt.BorderLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import com.keypointforensics.videotriage.gui.imagepanel.GalleryImagePanel;
import com.keypointforensics.videotriage.util.ImageUtils;

public class GalleryGridItem extends JPanel {

	/*
	 * Author: Daniel Puckowski
	 */

	/**
	 * 
	 */
	private static final long serialVersionUID = 8487543902860460807L;

	private GalleryImagePanel mGalleryImagePanel;
	private JCheckBox mSelectedCheckBox;
	private String mImageAbsolutePath;

	public GalleryGridItem(final String imageAbsolutePath, final boolean scaleToFit) {
		mSelectedCheckBox = new JCheckBox("Check Image");
		mGalleryImagePanel = new GalleryImagePanel(imageAbsolutePath, scaleToFit, mSelectedCheckBox);
		mImageAbsolutePath = imageAbsolutePath;
		
		buildPanel();
	}

	public void update() {
		mGalleryImagePanel.update(ImageUtils.loadBufferedImage(mImageAbsolutePath));
		this.revalidate();
	}
	
	public void update(final String imageAbsolutePath) {
		mGalleryImagePanel.update(ImageUtils.loadBufferedImage(imageAbsolutePath));
		mImageAbsolutePath = imageAbsolutePath;
		this.revalidate();
	}
	
	public String getImageAbsolutePath() {
		return mImageAbsolutePath;
	}

	public boolean isSelected() {
		return mSelectedCheckBox.isSelected();
	}

	public void setSelected(final boolean newSelectedState) {
		mSelectedCheckBox.setSelected(newSelectedState);
	}

	public void setChildPopupMenu(final JPopupMenu popupMenu) {
		mGalleryImagePanel.setComponentPopupMenu(popupMenu);
		mGalleryImagePanel.revalidate();
	}

	private void buildPanel() {
		this.setLayout(new BorderLayout());

		this.add(mGalleryImagePanel, BorderLayout.CENTER);
		this.add(mSelectedCheckBox, BorderLayout.SOUTH);
	}
}

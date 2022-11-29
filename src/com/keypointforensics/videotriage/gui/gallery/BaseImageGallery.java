package com.keypointforensics.videotriage.gui.gallery;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import com.keypointforensics.videotriage.gui.imagepanel.GalleryImagePanel;
import com.keypointforensics.videotriage.legacy.FileUtilsLegacy;
import com.keypointforensics.videotriage.legacy.VideoFalsePositiveRemoverByReferenceLegacy;
import com.keypointforensics.videotriage.progress.ProgressBundle;
import com.keypointforensics.videotriage.report.GalleryGridItem;
import com.keypointforensics.videotriage.util.CursorUtils;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ProgressUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.Utils;
import com.keypointforensics.videotriage.window.ChildWindowList;
import com.keypointforensics.videotriage.window.WindowRegistry;

public abstract class BaseImageGallery extends JFrame {

	/*
	 * Author: Daniel Puckowski
	 */
	
	/**
	 * 
	 */
	protected static final long serialVersionUID = 232646536271988938L;
	
	protected static final String NO_PREVIEW_AVAILABLE_IMAGE_PATH = FileUtils.GRAPHICS_DIRECTORY + "no_preview_available.jpg";
	
	protected int currentIndex;
	protected int maxIndex;
	protected int numberToPopulate;
	protected VideoFalsePositiveRemoverByReferenceLegacy mFalsePositiveRemover;
	
	protected final JPanel contentPanel = new JPanel();
	protected final String path;
	
	protected ArrayList<String> imgPaths;
	protected HashSet<String> mPriorSelectedState;
	
	protected JLabel mPageLabel;
	protected int mPageIndex;
	protected int mOldNumberOfPages;
	
	protected boolean mDisableFurtherActions;
	
	protected JMenuBar mMenuBar;
    
	protected ChildWindowList mChildWindowList;
	
	public BaseImageGallery(final String path) {
		this.setLayout(new BorderLayout());
		
		mChildWindowList = new ChildWindowList();
		
		this.path = path;
		mFalsePositiveRemover = new VideoFalsePositiveRemoverByReferenceLegacy(path, true);
		mPriorSelectedState = new HashSet<String>();
		
		imgPaths = (ArrayList<String>) FileUtilsLegacy.parseDirectoryRecursiveForImages(path);
		Collections.sort(imgPaths);
		
		mOldNumberOfPages = (int) Math.ceil((double) imgPaths.size() / 16.0);
		
		if(mOldNumberOfPages == 0) {
			mPageIndex = 0;
		}
		else {
			mPageIndex = 1;
		}
		
		mDisableFurtherActions = false;
		
		WindowRegistry.getInstance().registerFrame(this, "BaseImageGallery");
	}
	
	public BaseImageGallery(final String path, boolean simpleRecursiveParse) {
		this.setLayout(new BorderLayout());
		
		this.path = path;
		mFalsePositiveRemover = new VideoFalsePositiveRemoverByReferenceLegacy(path, simpleRecursiveParse);
		mPriorSelectedState = new HashSet<String>();
		
		imgPaths = (ArrayList<String>) FileUtilsLegacy.parseDirectoryRecursiveForImages(path);
		Collections.sort(imgPaths);
		
		mOldNumberOfPages = (int) Math.ceil((double) imgPaths.size() / 16.0);
		
		if(mOldNumberOfPages == 0) {
			mPageIndex = 0;
		}
		else {
			mPageIndex = 1;
		}
		
		mDisableFurtherActions = false;
		
		WindowRegistry.getInstance().registerFrame(this, "BaseImageGallery");
	}
	
	public String getRootPath() {		
		return path;
	}
	
	public VideoFalsePositiveRemoverByReferenceLegacy getFalsePositiveRemover() {
		return mFalsePositiveRemover;
	}
	
	protected void clearPriorSelectedStates() {
		if(mPriorSelectedState != null) {
			mPriorSelectedState.clear();
		}
		
		GalleryGridItem galleryGridItem = null;
		
		for(int i = 0; i < contentPanel.getComponentCount(); ++i) {
			if(contentPanel.getComponent(i) instanceof GalleryGridItem) {
				galleryGridItem = (GalleryGridItem) contentPanel.getComponent(i);
				
				//if(galleryGridItem.isSelected() == true) {
					galleryGridItem.setSelected(false);
				//}
			}
		}
	}
	
	protected abstract void build();
	protected abstract void buildMenuBar();
	protected abstract void performSaveChangesAction();
	protected abstract void disableFurtherActions();
	protected abstract void enableFurtherActions();
	
	protected String getUpdatedPageLabelText() {			
		String numberOfPagesString = String.valueOf(Math.ceil((double) imgPaths.size() / 16.0));

		return "Images: " + imgPaths.size() + "    Page: " + mPageIndex + "/" + numberOfPagesString.substring(0, numberOfPagesString.indexOf("."));
	}
	
	protected void performPreviousAction() {
		disableFurtherActions();
		
		savePriorSelectedStates();
		
		EventQueue.invokeLater(new Runnable() { public void run() {
			contentPanel.removeAll();
			contentPanel.revalidate();
			}
		});
		
		maxIndex = imgPaths.size();
		
		if(imgPaths.size() < 16)
		{
			currentIndex = 0;
			numberToPopulate = 16;
						
			while(numberToPopulate > 0 && currentIndex < imgPaths.size()) {				
				GalleryGridItem tp = new GalleryGridItem(imgPaths.get(currentIndex), GalleryImagePanel.SCALE_TO_FIT_ENABLED); 
				addPopupMenuToGalleryGridItem(tp);
				
				EventQueue.invokeLater(new Runnable() { public void run() {
				contentPanel.add(tp);
				}
				});
				
				currentIndex++;
				numberToPopulate--;
			}
			
			while(numberToPopulate > 0)
			{
				GalleryGridItem tp = new GalleryGridItem(NO_PREVIEW_AVAILABLE_IMAGE_PATH, GalleryImagePanel.SCALE_TO_FIT_ENABLED); 
				addPopupMenuToGalleryGridItem(tp);
				
				EventQueue.invokeLater(new Runnable() { public void run() {
				contentPanel.add(tp);
				}
				});
				
				numberToPopulate--;
			}
		}
		else
		{
			currentIndex -= 32;
			if(currentIndex < 0) {
				currentIndex = maxIndex + currentIndex;
			}
			numberToPopulate = 16;
			
			while(numberToPopulate > 0 && imgPaths.size() > currentIndex && currentIndex >= 0) {
				GalleryGridItem tp = new GalleryGridItem(imgPaths.get(currentIndex), GalleryImagePanel.SCALE_TO_FIT_ENABLED); 
				addPopupMenuToGalleryGridItem(tp);
				
				EventQueue.invokeLater(new Runnable() { public void run() {
				contentPanel.add(tp);
				}
				});
				
				currentIndex++;
				numberToPopulate--;
				
				if(currentIndex >= maxIndex) {
					currentIndex = 0;
				}
			}
		}
		
		mPageIndex--;
		
		if(mPageIndex < 1)	
		{
			mPageIndex = mOldNumberOfPages;
		}
		
		EventQueue.invokeLater(new Runnable() { public void run() {
		mPageLabel.setText(getUpdatedPageLabelText());
		}
		});
		
		EventQueue.invokeLater(new Runnable() { public void run() {
		reinstatePriorSelectedStates();
		}
		});
		
		EventQueue.invokeLater(new Runnable() { public void run() {
		contentPanel.revalidate();
		}
		});
		
		enableFurtherActions();
	}
	
	protected void performNextAction() {
		disableFurtherActions();
		
		savePriorSelectedStates();
		
		EventQueue.invokeLater(new Runnable() { public void run() {
			contentPanel.removeAll();
			contentPanel.revalidate();
			}
		});
		
		maxIndex = imgPaths.size();

		if(imgPaths.size() < 16)
		{
			currentIndex = 0;
			numberToPopulate = 16;
			
			while(numberToPopulate > 0 && currentIndex < imgPaths.size()) {				
				GalleryGridItem tp = new GalleryGridItem(imgPaths.get(currentIndex), GalleryImagePanel.SCALE_TO_FIT_ENABLED); 
				addPopupMenuToGalleryGridItem(tp);
				
				EventQueue.invokeLater(new Runnable() { public void run() {
				contentPanel.add(tp);
				}
				});
				
				currentIndex++;
				numberToPopulate--;
			}
			
			while(numberToPopulate > 0)
			{
				GalleryGridItem tp = new GalleryGridItem(NO_PREVIEW_AVAILABLE_IMAGE_PATH, GalleryImagePanel.SCALE_TO_FIT_ENABLED); 
				addPopupMenuToGalleryGridItem(tp);
				
				EventQueue.invokeLater(new Runnable() { public void run() {
				contentPanel.add(tp);
				}
				});
				
				numberToPopulate--;
			}
		}
		else
		{
			if(currentIndex >= maxIndex) {
				currentIndex = 0 + Math.abs(maxIndex - currentIndex);
			}
			numberToPopulate = 16;
			
			while(numberToPopulate > 0 && imgPaths.size() > currentIndex && currentIndex >= 0) {
				GalleryGridItem tp = new GalleryGridItem(imgPaths.get(currentIndex), GalleryImagePanel.SCALE_TO_FIT_ENABLED); 
				addPopupMenuToGalleryGridItem(tp);
				
				EventQueue.invokeLater(new Runnable() { public void run() {
				contentPanel.add(tp);
				}
				});
				
				currentIndex++;
				numberToPopulate--;
				
				if(currentIndex >= maxIndex) {
					currentIndex = 0;
				}
			}
		}
		
		//TODO
		if(imgPaths.size() > 16) {
			mPageIndex++;
		}
		
		if(mPageIndex > mOldNumberOfPages)	
		{
			mPageIndex = 1;
		}
		 
		EventQueue.invokeLater(new Runnable() { public void run() {
		mPageLabel.setText(getUpdatedPageLabelText());
		}
		});
		
		EventQueue.invokeLater(new Runnable() { public void run() {
		reinstatePriorSelectedStates();
		}
		});
		
		EventQueue.invokeLater(new Runnable() { public void run() {
		contentPanel.revalidate();	
		}
		});
		
		enableFurtherActions();
	}
	
	protected void addPopupMenuToGalleryGridItem(final GalleryGridItem galleryGridItem) {
		JPopupMenu popupMenu = new JPopupMenu();

		JMenuItem menuItem = new JMenuItem("Mark All");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(mDisableFurtherActions == true) {
					
					return;
				}
				
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ImageGallery MarkAll", this);
												
						CursorUtils.setBusyCursor(BaseImageGallery.this);
						
						galleryGridItem.setSelected(true);
						performMarkAllAction();
						
						CursorUtils.setDefaultCursor(BaseImageGallery.this);
												
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
		});
		popupMenu.add(menuItem);
		
		menuItem = new JMenuItem("Mark All But One");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(mDisableFurtherActions == true) {
					
					return;
				}
				
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ImageGallery MarkAllBut", this);
												
						CursorUtils.setBusyCursor(BaseImageGallery.this);
						
						galleryGridItem.setSelected(true);
						performMarkAllButOneAction();
						
						CursorUtils.setDefaultCursor(BaseImageGallery.this);
												
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
		});
		popupMenu.add(menuItem);
		
		menuItem = new JMenuItem("Mark One");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(mDisableFurtherActions == true) {
					
					return;
				}
				
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ImageGallery MarkOne", this);
												
						CursorUtils.setBusyCursor(BaseImageGallery.this);
						
						galleryGridItem.setSelected(true);
						performMarkOneAction();
						
						CursorUtils.setDefaultCursor(BaseImageGallery.this);
												
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
		});
		popupMenu.add(menuItem);
		
		galleryGridItem.setChildPopupMenu(popupMenu);
	}
	
	public void performAutomatedMarkAllButOneAction() {
		disableFurtherActions();
				
		//GalleryGridItem galleryGridItem = null;
		
		int progressIndex = 0;
		
		ProgressBundle progressBundle = ProgressUtils.getProgressBundle("Automatic Review Progress...", imgPaths.size());
		
		if(imgPaths.isEmpty() == false) {
			while(true) {					
				//galleryGridItem = new GalleryGridItem(imgPaths.get(progressIndex), GalleryImagePanel.SCALE_TO_FIT_ENABLED); 
				
				try {
					getFalsePositiveRemover().removeFalsePositive(imgPaths.get(progressIndex), false, 50);//galleryGridItem.getImageAbsolutePath(), false, 50);
				} catch (IOException ioException) {
					//ThreadUtils.removeThreadFromHandleList(this);
						
					return;
				}
				
				progressIndex++;
				progressBundle.progressBar.setMaximum(imgPaths.size());
				progressBundle.progressBar.setValue(progressIndex);
				
				if(progressIndex >= imgPaths.size()) {
					break;
				}
			}
		}
		
		progressBundle.frame.dispose();
		
		imgPaths = mFalsePositiveRemover.getFalsePositiveBundle().getCaptureFilenames();
		
		int newNumberOfPages = (int) Math.ceil((double) imgPaths.size() / 16.0);
		int pageDifference = newNumberOfPages - mOldNumberOfPages;
		mPageIndex += pageDifference;
		
		if(newNumberOfPages < 1)
		{
			mPageIndex = 0;
		}
		else if(mPageIndex < 1)
		{
			mPageIndex = 1;
		}
		
		mOldNumberOfPages = newNumberOfPages;	

		currentIndex = 0;
		
		refresh();
		//clearPriorSelectedStates();
		
		Utils.displayMessageDialog("Automatic Review", Utils.SOFTWARE_NAME + " has finished automatically reviewing video processing results.");
		
		enableFurtherActions();
	}
	
	protected void performMarkAllButOneAction() {
		disableFurtherActions();
		
		savePriorSelectedStates();		
				
		ArrayList<String> filenamesToDelete = new ArrayList<String>();
		
		for(String imageAbsolutePath : mPriorSelectedState) {	
			filenamesToDelete.add(imageAbsolutePath);
		}		
			
		Thread markThread = new Thread() {
			@Override
			public void run() {
				ThreadUtils.addThreadToHandleList("GalleryGrid MarkAllButOne", this);
										
				try {
					getFalsePositiveRemover().removeFalsePositives(filenamesToDelete, false);
				} catch (IOException ioException) {
					ThreadUtils.removeThreadFromHandleList(this);
						
					return;
				}
					
				ThreadUtils.removeThreadFromHandleList(this);
			}
		};
			
		try {
			markThread.start();
			markThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		imgPaths = mFalsePositiveRemover.getFalsePositiveBundle().getCaptureFilenames();
				
		int newNumberOfPages = (int) Math.ceil((double) imgPaths.size() / 16.0);
		int pageDifference = newNumberOfPages - mOldNumberOfPages;

		mPageIndex += pageDifference;
		
		if(newNumberOfPages < 1)
		{
			mPageIndex = 0;
		}
		else if(mPageIndex < 1)
		{
			mPageIndex = 1;
		}
		
		mOldNumberOfPages = newNumberOfPages;	

		currentIndex -= (16 * pageDifference); 
		
		refresh();
		clearPriorSelectedStates();
				
		enableFurtherActions();
	}
	
	protected void performMarkAllAction() {
		disableFurtherActions();
		
		savePriorSelectedStates();		
				
		ArrayList<String> filenamesToDelete = new ArrayList<String>();
		
		for(String imageAbsolutePath : mPriorSelectedState) {
			filenamesToDelete.add(imageAbsolutePath);
		}	
			
		Thread markThread = new Thread() {
			@Override
			public void run() {
				ThreadUtils.addThreadToHandleList("GalleryGrid MarkAll", this);
						
				try {
					getFalsePositiveRemover().removeFalsePositives(filenamesToDelete, true);
				} catch (IOException ioException) {
					ThreadUtils.removeThreadFromHandleList(this);
						
					return;
				}
					
				ThreadUtils.removeThreadFromHandleList(this);
			}
		};
			
		try {
			markThread.start();
			markThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
			
		imgPaths = mFalsePositiveRemover.getFalsePositiveBundle().getCaptureFilenames();
				
		int newNumberOfPages = (int) Math.ceil((double) imgPaths.size() / 16.0);
		int pageDifference = newNumberOfPages - mOldNumberOfPages;

		mPageIndex += pageDifference;
		
		if(newNumberOfPages < 1)
		{
			mPageIndex = 0;
		}
		else if(mPageIndex < 1)
		{
			mPageIndex = 1;
		}
		
		mOldNumberOfPages = newNumberOfPages;	

		currentIndex -= (16 * pageDifference);
		
		refresh();
		clearPriorSelectedStates();
				
		enableFurtherActions();
	}
	
	protected void performMarkOneAction() {
		disableFurtherActions();
		
		savePriorSelectedStates();		
				
		ArrayList<String> filenamesToDelete = new ArrayList<String>();
		
		for(String imageAbsolutePath : mPriorSelectedState) {
			filenamesToDelete.add(imageAbsolutePath);
		}	
			
		Thread markThread = new Thread() {
			@Override
			public void run() {
				ThreadUtils.addThreadToHandleList("GalleryGrid MarkOne", this);
						
				mFalsePositiveRemover.getFalsePositiveBundle().removeCaptureFilenames(filenamesToDelete);
					
				ThreadUtils.removeThreadFromHandleList(this);
			}
		};
			
		try {
			markThread.start();
			markThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
			
		imgPaths = mFalsePositiveRemover.getFalsePositiveBundle().getCaptureFilenames();
				
		int newNumberOfPages = (int) Math.ceil((double) imgPaths.size() / 16.0);
		int pageDifference = newNumberOfPages - mOldNumberOfPages;

		mPageIndex += pageDifference;
		
		if(newNumberOfPages < 1)
		{
			mPageIndex = 0;
		}
		else if(mPageIndex < 1)
		{
			mPageIndex = 1;
		}
		
		mOldNumberOfPages = newNumberOfPages;	

		currentIndex -= (16 * pageDifference);
		
		refresh();
		clearPriorSelectedStates();
				
		enableFurtherActions();
	}
	
	protected void savePriorSelectedStates() {				
		String imageAbsolutePath;
		
		for(int i = 0; i < contentPanel.getComponentCount(); ++i) {
			if(contentPanel.getComponent(i) instanceof GalleryGridItem) {
				final GalleryGridItem mCurrentGalleryGridItem = (GalleryGridItem) contentPanel.getComponent(i);
				
				if(mCurrentGalleryGridItem.isSelected() == true) {
					imageAbsolutePath = mCurrentGalleryGridItem.getImageAbsolutePath();
					
					if(mPriorSelectedState.contains(imageAbsolutePath) == false) {
						mPriorSelectedState.add(imageAbsolutePath);
					}
				}
			}
		}
	}
	
	protected void reinstatePriorSelectedStates() {		
		for(int i = 0; i < contentPanel.getComponentCount(); ++i) {
			if(contentPanel.getComponent(i) instanceof GalleryGridItem) {
				final GalleryGridItem mCurrentGalleryGridItem = (GalleryGridItem) contentPanel.getComponent(i);
				
				if(mPriorSelectedState.contains(mCurrentGalleryGridItem.getImageAbsolutePath()) == true) {				
					EventQueue.invokeLater(new Runnable() { public void run() {
							mCurrentGalleryGridItem.setSelected(true);
						}
					});
				}
			}
		}
	}
	
	public void refresh() {
		imgPaths = mFalsePositiveRemover.getFalsePositiveBundle().getCaptureFilenames();
		Collections.sort(imgPaths);
		
		EventQueue.invokeLater(new Runnable() { public void run() {
				contentPanel.removeAll();
				contentPanel.revalidate();
			}
		});
		
		maxIndex = imgPaths.size();
				
		if(imgPaths.size() <= 16)
		{
			currentIndex = 0;
			numberToPopulate = 16;
						
			while(numberToPopulate > 0 && currentIndex < imgPaths.size()) {	
				GalleryGridItem tp = new GalleryGridItem(imgPaths.get(currentIndex), GalleryImagePanel.SCALE_TO_FIT_ENABLED);
				addPopupMenuToGalleryGridItem(tp);
				
				EventQueue.invokeLater(new Runnable() { public void run() {
						contentPanel.add(tp);
					}
				});
				
				currentIndex++;
				numberToPopulate--;
			}
			
			while(numberToPopulate > 0)
			{
				GalleryGridItem tp = new GalleryGridItem(NO_PREVIEW_AVAILABLE_IMAGE_PATH, GalleryImagePanel.SCALE_TO_FIT_ENABLED); 
				
				EventQueue.invokeLater(new Runnable() { public void run() {
						contentPanel.add(tp);
					}
				});
				
				numberToPopulate--;
			}
		}
		else
		{
			currentIndex -= 16;
			if(currentIndex >= (imgPaths.size() - 16) == true) {
				do {
					currentIndex -= 16;
				} while (currentIndex >= (imgPaths.size() - 16) == true);
			}
			else if(currentIndex < 0) {
				currentIndex = (maxIndex - 1) + currentIndex;
			}
			
			if(currentIndex < 0) {
				currentIndex = 0;
			}
			
			numberToPopulate = 16;
			
			while(numberToPopulate > 0 && imgPaths.size() > currentIndex && currentIndex >= 0) {				
				GalleryGridItem tp = new GalleryGridItem(imgPaths.get(currentIndex), GalleryImagePanel.SCALE_TO_FIT_ENABLED); 
				addPopupMenuToGalleryGridItem(tp);

				EventQueue.invokeLater(new Runnable() { public void run() {
						contentPanel.add(tp);
					}
				});
				
				currentIndex++;
				numberToPopulate--;
				
				if(currentIndex >= maxIndex) {
					currentIndex = 0;
				}
			}
		}

		EventQueue.invokeLater(new Runnable() { public void run() {
				mPageLabel.setText(getUpdatedPageLabelText());
				contentPanel.revalidate();
			}
		});
	}
}

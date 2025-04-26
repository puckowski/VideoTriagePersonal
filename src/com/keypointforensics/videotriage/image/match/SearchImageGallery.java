package com.keypointforensics.videotriage.image.match;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import com.keypointforensics.videotriage.gui.imagepanel.GalleryImagePanel;
import com.keypointforensics.videotriage.legacy.UtilsLegacy;
import com.keypointforensics.videotriage.report.GalleryGridItem;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.FontUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.Utils;
import com.keypointforensics.videotriage.util.WebUtils;
import com.keypointforensics.videotriage.util.WindowUtils;
import com.keypointforensics.videotriage.window.WindowRegistry;

public class SearchImageGallery extends JFrame {

	/*
	 * Author: Daniel Puckowski
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 232646536271988938L;
	
	private static final String  NO_PREVIEW_AVAILABLE_IMAGE_PATH = FileUtils.GRAPHICS_DIRECTORY + "no_preview_available.jpg";
		
	private int currentIndex;
	private int maxIndex;
	private int numberToPopulate;
	
	private final JPanel contentPanel = new JPanel();
	
	private ArrayList<String> imgPaths;
	private HashSet<String> mPriorSelectedState;
	
	private JLabel mPageLabel;
	private int mPageIndex;
	private int mOldNumberOfPages;
	
	private JButton mPreviousButton;
	private JButton mNextButton;
	private JButton mDeleteSelectedButton;
	private JButton mMarkAllButton;
	private JButton mClearAllButton;
	
	private boolean mDisableFurtherActions;
	
	private JMenuBar mMenuBar;
	
	private JMenuItem mExitMenuItem;
	private JMenuItem mAboutMenuItem;
	private JMenuItem mPreviousMenuItem;
	private JMenuItem mNextMenuItem;
	private JMenuItem mDeleteSelectedMenuItem;
	private JMenuItem mMarkAllMenuItem;
	private JMenuItem mClearAllMenuItem;
	private JMenuItem mDocumentationMenuItem;
	
	public SearchImageGallery(final ArrayList<String> filesToReview) {	
		this.setLayout(new BorderLayout());
				
		mPriorSelectedState = new HashSet<String>();
		
		imgPaths = filesToReview;
		Collections.sort(imgPaths);
		
		mOldNumberOfPages = (int) Math.ceil((double) imgPaths.size() / 16.0);
		
		if(mOldNumberOfPages == 0) {
			mPageIndex = 0;
		}
		else {
			mPageIndex = 1;
		}
		
		mDisableFurtherActions = false;
		
		WindowRegistry.getInstance().registerFrame(this, "SearchImageGallery");
	}
	
	private void disableFurtherActions() {
		mMenuBar.setEnabled(false);
		
		mPreviousButton.setEnabled(false);
		mNextButton.setEnabled(false);
		mDeleteSelectedButton.setEnabled(false);
		mMarkAllButton.setEnabled(false);
		mClearAllButton.setEnabled(false);
		
		mAboutMenuItem.setEnabled(false);
		mPreviousMenuItem.setEnabled(false);
		mNextMenuItem.setEnabled(false);
		mDeleteSelectedMenuItem.setEnabled(false);
		mMarkAllMenuItem.setEnabled(false);
		mClearAllMenuItem.setEnabled(false);
		mDocumentationMenuItem.setEnabled(false);
		
		mDisableFurtherActions = true;
	}
	
	private void enableFurtherActions() {
		mMenuBar.setEnabled(true);
		
		mPreviousButton.setEnabled(true);
		mNextButton.setEnabled(true);
		mDeleteSelectedButton.setEnabled(true);
		mMarkAllButton.setEnabled(true);
		mClearAllButton.setEnabled(true);
		
		mAboutMenuItem.setEnabled(true);
		mPreviousMenuItem.setEnabled(true);
		mNextMenuItem.setEnabled(true);
		mDeleteSelectedMenuItem.setEnabled(true);
		mMarkAllMenuItem.setEnabled(true);
		mClearAllMenuItem.setEnabled(true);
		mDocumentationMenuItem.setEnabled(true);
		
		mDisableFurtherActions = false;
	}
	
	private void buildMenuBar() {		
		JMenu menu;

		mMenuBar = new JMenuBar();

		menu = new JMenu("File");
		mMenuBar.add(menu);
		
		mExitMenuItem = new JMenuItem("Exit");
		mExitMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SearchImageGallery Exit", this);
						
						SearchImageGallery.this.dispose();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(mExitMenuItem);
	
		menu = new JMenu("Results");
		mMenuBar.add(menu);
		
		mPreviousMenuItem = new JMenuItem("Previous");
		mPreviousMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				if(mDisableFurtherActions == true) {
					
					return;
				}
				
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SearchImageGallery Previous", this);
						
						performPreviousAction();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(mPreviousMenuItem);
		
		mNextMenuItem = new JMenuItem("Next");
		mNextMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				if(mDisableFurtherActions == true) {
					
					return;
				}
				
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SearchImageGallery Next", this);
						
						performNextAction();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(mNextMenuItem);
		
		mDeleteSelectedMenuItem = new JMenuItem("Delete");
		mDeleteSelectedMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				if(mDisableFurtherActions == true) {
					
					return;
				}
				
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SearchImageGallery DeleteSelected", this);
							
						performDeleteSelectedAction();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(mDeleteSelectedMenuItem);
		
		mClearAllMenuItem = new JMenuItem("Clear All");
		mClearAllMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				if(mDisableFurtherActions == true) {
					
					return;
				}
				
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SearchImageGallery ClearAll", this);
							
						performClearAllAction();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(mClearAllMenuItem);
		
		mMarkAllMenuItem = new JMenuItem("Mark All");
		mMarkAllMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				if(mDisableFurtherActions == true) {
					
					return;
				}
				
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SearchImageGallery MarkAll", this);
							
						performMarkAllAction();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(mMarkAllMenuItem);
			
		menu = new JMenu("Help");
		mMenuBar.add(menu);
		
		mDocumentationMenuItem = new JMenuItem("Open Documentation");
		mDocumentationMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SearchImageGallery OpenDoc", this);
						
						try {
							WebUtils.openWebpage(new URL(WebUtils.URL_STRING_DOCUMENTATION));
						} catch (MalformedURLException malformedUrlException) {
							//malformedUrlException.printStackTrace();
						}
					
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(mDocumentationMenuItem);
		
		mAboutMenuItem = new JMenuItem("About");
		mAboutMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				if(mDisableFurtherActions == true) {
					
					return;
				}
				
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SearchImageGallery About", this);
						
						Utils.displayMessageDialog("About", Utils.SOFTWARE_NAME + " \nVersion: " + Utils.SOFTWARE_VERSION + "\n© " + Utils.AUTHOR_NAME);
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(mAboutMenuItem);
		
		this.setJMenuBar(mMenuBar);
	}
	
	public void build() {
		buildMenuBar();
		
		contentPanel.setLayout(new GridLayout(4, 4));
		
		JTabbedPane imageTabPane = new JTabbedPane();
		imageTabPane.addTab("Processing Results", contentPanel);

		maxIndex = imgPaths.size();
		currentIndex = 0;
		numberToPopulate = 16;
			
		while(numberToPopulate > 0 && imgPaths.size() > currentIndex && currentIndex >= 0) {
			GalleryGridItem tp = new GalleryGridItem(imgPaths.get(currentIndex), GalleryImagePanel.SCALE_TO_FIT_ENABLED); 
			addPopupMenuToGalleryGridItem(tp);
			contentPanel.add(tp);
				
			currentIndex++;
			numberToPopulate--;
		}
	
		while(numberToPopulate > 0 && currentIndex >= 0) {
			GalleryGridItem tp = new GalleryGridItem(NO_PREVIEW_AVAILABLE_IMAGE_PATH, GalleryImagePanel.SCALE_TO_FIT_ENABLED); 
			addPopupMenuToGalleryGridItem(tp);
			contentPanel.add(tp);
				
			currentIndex++;
			numberToPopulate--;
		}
		
		mPreviousButton = new JButton("Previous");
		mPreviousButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(mDisableFurtherActions == true) {
					
					return;
				}
				
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SearchImageGallery Prev", this);
						
						performPreviousAction();	
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();		
			}
		});
		
		mDeleteSelectedButton = new JButton("Delete");
		mDeleteSelectedButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(mDisableFurtherActions == true) {
					
					return;
				}
				
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SearchImageGallery Prev", this);
						
						performDeleteSelectedAction();		
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();		
			}
		});
		
		mClearAllButton = new JButton("Clear All");
		mClearAllButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(mDisableFurtherActions == true) {
					
					return;
				}
				
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SearchImageGallery Prev", this);
						
						performClearAllAction();	
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();		
			}
		});
		
		mMarkAllButton = new JButton("Mark All");
		mMarkAllButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(mDisableFurtherActions == true) {
					
					return;
				}
				
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SearchImageGallery Prev", this);
						
						performMarkAllAction();	
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();		
			}
		});
		
		mNextButton = new JButton("Next");
		mNextButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(mDisableFurtherActions == true) {
					
					return;
				}
				
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SearchImageGallery Next", this);
						
						performNextAction();	
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();	
			}
		});
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(mPreviousButton);
		buttonPanel.add(mDeleteSelectedButton);
		buttonPanel.add(mClearAllButton);
		buttonPanel.add(mMarkAllButton);
		buttonPanel.add(mNextButton);
		
		JPanel buttonAndPagePanel = new JPanel();
		buttonAndPagePanel.setLayout(new BorderLayout());
		
		buttonAndPagePanel.add(buttonPanel, BorderLayout.CENTER);
		
		mPageLabel = new JLabel();
		mPageLabel.setHorizontalAlignment(SwingConstants.CENTER);
		mPageLabel.setFont(FontUtils.DEFAULT_FONT);
		mPageLabel.setText(getUpdatedPageLabelText());
		
		buttonAndPagePanel.add(mPageLabel, BorderLayout.SOUTH);
		
		this.add(imageTabPane, BorderLayout.CENTER); 
		this.add(buttonAndPagePanel, BorderLayout.SOUTH);
		
		WindowUtils.setFrameIcon(this);
		this.setTitle("Create Report");
		this.setPreferredSize(new Dimension(900, 1000));
		this.pack();
		WindowUtils.maximize(this);
		this.setVisible(true);
		//WindowUtils.center(this);
	}
	
	private String getUpdatedPageLabelText() {			
		String numberOfPagesString = String.valueOf(Math.ceil((double) imgPaths.size() / 16.0));

		return "Images: " + imgPaths.size() + "    Page: " + mPageIndex + "/" + numberOfPagesString.substring(0, numberOfPagesString.indexOf("."));
	}
	
	private void performPreviousAction() {
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
	
	private void clearPriorSelectedStates() {
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
	
	private void performDeleteSelectedAction() {
		final int confirmSaveChoice = UtilsLegacy.displayConfirmDialog("Notice", "This operation cannot be undone.\n" +
				"Do you wish to delete marked search results?");
		
		if(confirmSaveChoice == JOptionPane.OK_OPTION) {
			disableFurtherActions();
			
			savePriorSelectedStates();		
					
			File toDelete = null;
			
			for(String imageAbsolutePath : mPriorSelectedState) {	
				imgPaths.remove(imageAbsolutePath);
				
				toDelete = new File(imageAbsolutePath);
				toDelete.delete();
			}		
			
			refresh();
			clearPriorSelectedStates();
			
			enableFurtherActions();
		}
	}
	
	private void performMarkAllAction() {
		disableFurtherActions();
		
		savePriorSelectedStates();		
				
		GalleryGridItem galleryGridItem = null;
		
		for(int i = 0; i < contentPanel.getComponentCount(); ++i) {
			if(contentPanel.getComponent(i) instanceof GalleryGridItem) {
				galleryGridItem = (GalleryGridItem) contentPanel.getComponent(i);
				
				//if(galleryGridItem.isSelected() == false) {
					galleryGridItem.setSelected(true);
				//}
			}
		}
		
		//refresh();
		//clearPriorSelectedStates();
				
		enableFurtherActions();
	}
	
	private void performClearAllAction() {
		disableFurtherActions();
		
		savePriorSelectedStates();		
				
		GalleryGridItem galleryGridItem = null;
		
		for(int i = 0; i < contentPanel.getComponentCount(); ++i) {
			if(contentPanel.getComponent(i) instanceof GalleryGridItem) {
				galleryGridItem = (GalleryGridItem) contentPanel.getComponent(i);
				
				//if(galleryGridItem.isSelected() == false) {
					galleryGridItem.setSelected(false);
				//}
			}
		}
		
		//refresh();
		//clearPriorSelectedStates();
				
		enableFurtherActions();
	}
	
	private void performNextAction() {
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
		
		mPageIndex++;
		
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
	
	private void addPopupMenuToGalleryGridItem(final GalleryGridItem galleryGridItem) {
		JPopupMenu popupMenu = new JPopupMenu();
		
		galleryGridItem.setChildPopupMenu(popupMenu);
	}
	
	private void savePriorSelectedStates() {				
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
	
	private void reinstatePriorSelectedStates() {		
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
		Collections.sort(imgPaths);
		
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
		
		EventQueue.invokeLater(new Runnable() { public void run() {
				mPageLabel.setText(getUpdatedPageLabelText());
				contentPanel.revalidate();
			}
		});
	}
}

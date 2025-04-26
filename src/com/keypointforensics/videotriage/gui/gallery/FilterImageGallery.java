package com.keypointforensics.videotriage.gui.gallery;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import com.keypointforensics.videotriage.blob.context.BlobContextList;
import com.keypointforensics.videotriage.filter.CannyEdgeFilter;
import com.keypointforensics.videotriage.filter.ContrastFilter;
import com.keypointforensics.videotriage.filter.ContrastStretchFilter;
import com.keypointforensics.videotriage.filter.ConvolveFilter;
import com.keypointforensics.videotriage.filter.EVideoTriageFilter;
import com.keypointforensics.videotriage.filter.EmbossFilter;
import com.keypointforensics.videotriage.filter.FilterGalleryGridItem;
import com.keypointforensics.videotriage.filter.GaussianFilter;
import com.keypointforensics.videotriage.filter.HistogramEqualizationFilter;
import com.keypointforensics.videotriage.filter.MedianFilter;
import com.keypointforensics.videotriage.filter.ReduceNoiseFilter;
import com.keypointforensics.videotriage.filter.SharpenFilter;
import com.keypointforensics.videotriage.filter.UnsharpFilter;
import com.keypointforensics.videotriage.gui.imagepanel.GalleryImagePanel;
import com.keypointforensics.videotriage.gui.imagepanel.ScalableSimpleImagePanel;
import com.keypointforensics.videotriage.legacy.FileUtilsLegacy;
import com.keypointforensics.videotriage.legacy.UtilsLegacy;
import com.keypointforensics.videotriage.util.BorderUtils;
import com.keypointforensics.videotriage.util.CursorUtils;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.FontUtils;
import com.keypointforensics.videotriage.util.ImageUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.Utils;
import com.keypointforensics.videotriage.util.WebUtils;
import com.keypointforensics.videotriage.util.WindowUtils;
import com.keypointforensics.videotriage.window.CloseChildrenWindowAdapter;
import com.keypointforensics.videotriage.window.WindowRegistry;

public class FilterImageGallery extends JFrame implements MouseListener {

	/*
	 * Author: Daniel Puckowski
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 232646536271988938L;
	
	private static final String NO_PREVIEW_AVAILABLE_IMAGE_PATH = FileUtils.GRAPHICS_DIRECTORY + "no_preview_available.jpg";
	
	private int currentIndex;
	private int maxIndex;
	private int numberToPopulate;
	
	private final JPanel contentPanel = new JPanel();
	private final String path;
	
	private ArrayList<String> imgPaths;
	private HashSet<String> mPriorSelectedState;
	
	private JLabel mPageLabel;
	private int mPageIndex;
	private int mOldNumberOfPages;
	
	private JButton mPreviousButton;
	private JButton mNextButton;
	private JButton mMarkAllForCheckedButton;
	private JButton mUndoButton;
	private JButton mSaveButton;
	
	private boolean mDisableFurtherActions;
	
	private JMenuBar mMenuBar;
	
	private JMenuItem mExitMenuItem;
	private JMenuItem mAboutMenuItem;
	private JMenuItem mPreviousMenuItem;
	private JMenuItem mNextMenuItem;
	private JMenuItem mUndoMenuItem;
	private JMenuItem mSaveMenuItem;
	private JMenuItem mApplyMenuItem;
	private JMenuItem mDocumentationMenuItem;
	
	private String mPreviewImageAbsolutePath;
	
	//
	private final EVideoTriageFilter[] VIDEO_TRIAGE_FILTER_LIST =  { 
		EVideoTriageFilter.CONTRAST_STRETCH_STANDARD,
		EVideoTriageFilter.HISTOGRAM_EQUALIZATION_STANDARD,
		EVideoTriageFilter.GAUSSIAN_FILTER_STANDARD,
		EVideoTriageFilter.MEDIAN_FILTER_STANDARD,
		EVideoTriageFilter.REDUCE_NOISE_FILTER_STANDARD,
		EVideoTriageFilter.SHARPEN_FILTER_STANDARD, 
		EVideoTriageFilter.SHARPEN_FILTER_MASKING,
		EVideoTriageFilter.CONTRAST_FILTER_STANDARD,
		EVideoTriageFilter.CONVOLVE_FILTER_STANDARD,
		EVideoTriageFilter.EMBOSS_STANDARD,
		EVideoTriageFilter.CANNY_EDGE_STANDARD
	};
	
	private JComboBox<EVideoTriageFilter> mFilterComboBox = new JComboBox<EVideoTriageFilter>(VIDEO_TRIAGE_FILTER_LIST);
	
	private ScalableSimpleImagePanel mImagePreviewPanel;
	private DefaultListModel<String> mFilterQueueListModel;
	
	private HashSet<String> mModifiedImageSet = new HashSet<String>();
	private HashMap<String, ArrayList<Stack<EVideoTriageFilter>>> mFilterOperationMap = new HashMap<String, ArrayList<Stack<EVideoTriageFilter>>>();
	private Stack<ArrayList<String>> mUndoFileListStack = new Stack<ArrayList<String>>();
	private Queue<EVideoTriageFilter> mFilterQueue = new LinkedList<EVideoTriageFilter>();
	//
	
	public FilterImageGallery(final String path, final BlobContextList blobContextList) {	
		this.setLayout(new BorderLayout());
		
		this.path = path;
		mPriorSelectedState = new HashSet<String>();
		
		imgPaths = (ArrayList<String>) FileUtilsLegacy.parseDirectoryRecursiveForImages(path);
		
		mOldNumberOfPages = (int) Math.ceil((double) imgPaths.size() / 16.0);
		
		if(mOldNumberOfPages == 0) {
			mPageIndex = 0;
		}
		else {
			mPageIndex = 1;
		}
		
		mDisableFurtherActions = false;
		
		WindowRegistry.getInstance().registerFrame(this, "FilterImageGallery");
	}
	
	public String getRootPath() {		
		return path;
	}
	
	private void disableFurtherActions() {
		mMenuBar.setEnabled(false);
		
		mPreviousButton.setEnabled(false);
		mNextButton.setEnabled(false);
		mMarkAllForCheckedButton.setEnabled(false);
		mUndoButton.setEnabled(false);
		mSaveButton.setEnabled(false);
		
		mDisableFurtherActions = true;
	}
	
	private void enableFurtherActions() {
		mMenuBar.setEnabled(true);
		
		mPreviousButton.setEnabled(true);
		mNextButton.setEnabled(true);
		mMarkAllForCheckedButton.setEnabled(true);
		mUndoButton.setEnabled(true);
		mSaveButton.setEnabled(true);
		
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
						ThreadUtils.addThreadToHandleList("FilterImageGallery Exit", this);
						
						FilterImageGallery.this.dispose();
						
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
						ThreadUtils.addThreadToHandleList("FilterImageGallery Prev", this);
												
						disableFurtherActions();
						
						CursorUtils.setBusyCursor(FilterImageGallery.this);
						
						performPreviousAction();		
						
						CursorUtils.setDefaultCursor(FilterImageGallery.this);
						
						enableFurtherActions();
						
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
						ThreadUtils.addThreadToHandleList("FilterImageGallery Next", this);				
						
						disableFurtherActions();
						
						CursorUtils.setBusyCursor(FilterImageGallery.this);
						
						performNextAction();		
						
						CursorUtils.setDefaultCursor(FilterImageGallery.this);
						
						enableFurtherActions();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(mNextMenuItem);
		
		menu.addSeparator();
		
		mUndoMenuItem = new JMenuItem("Undo"); 
		mUndoMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(mDisableFurtherActions == true) {
					
					return;
				} else if(mUndoFileListStack.isEmpty() == true) {
					
					return;
				}
				
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("FilterImageGallery Undo", this);
											
						disableFurtherActions();
						
						CursorUtils.setBusyCursor(FilterImageGallery.this);
						
						undoLastFilterStack();		
						
						CursorUtils.setDefaultCursor(FilterImageGallery.this);
						
						enableFurtherActions();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(mUndoMenuItem);
		
		mApplyMenuItem = new JMenuItem("Apply"); 
		mApplyMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(mDisableFurtherActions == true) {
					
					return;
				}
				
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("FilterImageGallery Apply", this);					
						
						disableFurtherActions();
						
						CursorUtils.setBusyCursor(FilterImageGallery.this);
						
						performApplyAllFiltersAction();		
						
						CursorUtils.setDefaultCursor(FilterImageGallery.this);
						
						enableFurtherActions();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(mApplyMenuItem);
		
		mSaveMenuItem = new JMenuItem("Save"); 
		mSaveMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(mDisableFurtherActions == true) {
					
					return;
				}
				
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("FilterImageGallery Save", this);				
						
						disableFurtherActions();
						
						CursorUtils.setBusyCursor(FilterImageGallery.this);
						
						saveAppliedFilters();		
						
						CursorUtils.setDefaultCursor(FilterImageGallery.this);
						
						enableFurtherActions();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(mSaveMenuItem);
		
		menu = new JMenu("Help");
		mMenuBar.add(menu);
		
		mDocumentationMenuItem = new JMenuItem("Open Documentation");
		mDocumentationMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("FilterImageGallery OpenDoc", this);
						
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
						ThreadUtils.addThreadToHandleList("FilterImageGallery About", this);
						
						Utils.displayMessageDialog("About", Utils.SOFTWARE_NAME + " \nVersion: " + Utils.SOFTWARE_VERSION + "\n© " + Utils.AUTHOR_NAME);
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(mAboutMenuItem);
		
		this.setJMenuBar(mMenuBar);
	}
		
	private void clearPriorSelectedStates() {
		if(mPriorSelectedState != null) {
			mPriorSelectedState.clear();
		}
		
		FilterGalleryGridItem galleryGridItem = null;
		
		for(int i = 0; i < contentPanel.getComponentCount(); ++i) {
			if(contentPanel.getComponent(i) instanceof FilterGalleryGridItem) {
				galleryGridItem = (FilterGalleryGridItem) contentPanel.getComponent(i);
				galleryGridItem.setSelected(false);
			}
		}
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
			FilterGalleryGridItem tp = new FilterGalleryGridItem(imgPaths.get(currentIndex), GalleryImagePanel.SCALE_TO_FIT_ENABLED); 
			tp.addMouseListener(this);
			contentPanel.add(tp);
				
			currentIndex++;
			numberToPopulate--;
		}
	
		while(numberToPopulate > 0 && currentIndex >= 0) {
			FilterGalleryGridItem tp = new FilterGalleryGridItem(NO_PREVIEW_AVAILABLE_IMAGE_PATH, GalleryImagePanel.SCALE_TO_FIT_ENABLED); 
			tp.addMouseListener(this);
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
						ThreadUtils.addThreadToHandleList("FilterImageGallery Prev", this);					
						
						disableFurtherActions();
						
						CursorUtils.setBusyCursor(FilterImageGallery.this);
						
						performPreviousAction();	
						
						CursorUtils.setDefaultCursor(FilterImageGallery.this);
						
						enableFurtherActions();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();		
			}
		});
		
		mUndoButton = new JButton("Undo");
		mUndoButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(mDisableFurtherActions == true) {
					
					return;
				}
				
				new Thread() {
					@Override
					public void run() {		
						ThreadUtils.addThreadToHandleList("FilterImageGallery Undo", this);						
						
						disableFurtherActions();
						
						CursorUtils.setBusyCursor(FilterImageGallery.this);
						
						undoLastFilterStack();		
						
						CursorUtils.setDefaultCursor(FilterImageGallery.this);
						
						enableFurtherActions();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();		
			}
		});
		
		mSaveButton = new JButton("Save");
		mSaveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(mDisableFurtherActions == true) {
					
					return;
				}
				
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("FilterImageGallery Save", this);						
						
						disableFurtherActions();
						
						CursorUtils.setBusyCursor(FilterImageGallery.this);
						
						saveAppliedFilters();		
						
						CursorUtils.setDefaultCursor(FilterImageGallery.this);
						
						enableFurtherActions();
						
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
						ThreadUtils.addThreadToHandleList("FilterImageGallery Next", this);						
												
						disableFurtherActions();
						
						CursorUtils.setBusyCursor(FilterImageGallery.this);
						
						performNextAction();	
						
						CursorUtils.setDefaultCursor(FilterImageGallery.this);
						
						enableFurtherActions();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();	
			}
		});
		
		mMarkAllForCheckedButton = new JButton("Apply");
		mMarkAllForCheckedButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(mDisableFurtherActions == true) {
					
					return;
				}
					
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("FilterImageGallery ApplyFilters", this);
						
						disableFurtherActions();
						
						CursorUtils.setBusyCursor(FilterImageGallery.this);
						
						performApplyAllFiltersAction();
						
						CursorUtils.setDefaultCursor(FilterImageGallery.this);
						
						enableFurtherActions();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();	
			}
		});
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(mUndoButton);
		buttonPanel.add(mPreviousButton);
		buttonPanel.add(mMarkAllForCheckedButton);
		buttonPanel.add(mNextButton);
		buttonPanel.add(mSaveButton);
		
		JPanel buttonAndPagePanel = new JPanel();
		buttonAndPagePanel.setLayout(new BorderLayout());
		
		buttonAndPagePanel.add(buttonPanel, BorderLayout.CENTER);
		
		mPageLabel = new JLabel();
		mPageLabel.setHorizontalAlignment(SwingConstants.CENTER);
		mPageLabel.setFont(FontUtils.DEFAULT_FONT);
		mPageLabel.setText(getUpdatedPageLabelText());
		
		buttonAndPagePanel.add(mPageLabel, BorderLayout.SOUTH);
		
		JPanel galleryPane = new JPanel();
		galleryPane.setLayout(new BorderLayout());
		galleryPane.add(imageTabPane, BorderLayout.CENTER);
		galleryPane.add(buttonAndPagePanel, BorderLayout.SOUTH);
		
		JPanel filterPanel = new JPanel();
		filterPanel.setBorder(BorderUtils.getEmptyBorder());
		filterPanel.setLayout(new BorderLayout());
		
		JPanel northFilterPanel = new JPanel();
		northFilterPanel.setLayout(new GridLayout(2, 1));
		
		mFilterComboBox.setSelectedIndex(0);
	
		northFilterPanel.add(mFilterComboBox);

		mFilterQueueListModel = new DefaultListModel<String>();  
        final JList<String> filterQueueList = new JList<>(mFilterQueueListModel);  
        filterQueueList.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
        JScrollPane filterListScrollPane = new JScrollPane(filterQueueList);
        
        JButton addFilterButton = new JButton("Add Filter");
		addFilterButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(mDisableFurtherActions == true) {
					
					return;
				}
					
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("FilterImageGallery AddFilter", this);
						
						disableFurtherActions();
						
						CursorUtils.setBusyCursor(FilterImageGallery.this);
						
						mFilterQueue.add((EVideoTriageFilter) mFilterComboBox.getSelectedItem());
						mFilterQueueListModel.addElement(mFilterComboBox.getSelectedItem().toString());
						
						CursorUtils.setDefaultCursor(FilterImageGallery.this);
						
						enableFurtherActions();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();	
			}
		});
		
        JButton removeFilterButton = new JButton("Remove Filter");
		removeFilterButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(mDisableFurtherActions == true) {
					
					return;
				}
					
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("FilterImageGallery RemoveFilter", this);
						
						disableFurtherActions();
						
						CursorUtils.setBusyCursor(FilterImageGallery.this);
						
						mFilterQueueListModel.remove(filterQueueList.getSelectedIndex());
						filterQueueList.revalidate();
						
						CursorUtils.setDefaultCursor(FilterImageGallery.this);
						
						enableFurtherActions();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();	
			}
		});
		
		JPanel filterButtonPanel = new JPanel();
		filterButtonPanel.setLayout(new FlowLayout());
		
		filterButtonPanel.add(addFilterButton);
		filterButtonPanel.add(removeFilterButton);
		
		northFilterPanel.add(filterButtonPanel);//addFilter);
        
        JPanel southFilterPanel = new JPanel();
        southFilterPanel.setLayout(new BorderLayout());
        southFilterPanel.add(filterListScrollPane, BorderLayout.CENTER);
        southFilterPanel.add(northFilterPanel, BorderLayout.NORTH);
         
        filterPanel.add(southFilterPanel, BorderLayout.SOUTH);
         
        mImagePreviewPanel = new ScalableSimpleImagePanel(true);
        mImagePreviewPanel.setPreferredSize(new Dimension(640, 480));
         
        filterPanel.add(mImagePreviewPanel, BorderLayout.CENTER);
        
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, galleryPane, filterPanel);
        mainSplitPane.setResizeWeight(0.60);
        mainSplitPane.setOneTouchExpandable(true);
        mainSplitPane.setContinuousLayout(true);
        
		this.add(mainSplitPane, BorderLayout.CENTER);
				
		WindowUtils.setFrameIcon(this);
		this.setTitle("Enhance Results");
		this.setPreferredSize(new Dimension(1400, 1000));
		this.pack();
		mainSplitPane.setDividerLocation(0.60);
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
				FilterGalleryGridItem tp = new FilterGalleryGridItem(imgPaths.get(currentIndex), GalleryImagePanel.SCALE_TO_FIT_ENABLED); 
				tp.addMouseListener(this);
				
				EventQueue.invokeLater(new Runnable() { public void run() {
				contentPanel.add(tp);
				}
				});
				
				currentIndex++;
				numberToPopulate--;
			}
			
			while(numberToPopulate > 0)
			{
				FilterGalleryGridItem tp = new FilterGalleryGridItem(NO_PREVIEW_AVAILABLE_IMAGE_PATH, GalleryImagePanel.SCALE_TO_FIT_ENABLED); 
				tp.addMouseListener(this);
				
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
				String imageAbsolutePath = imgPaths.get(currentIndex);
				
				FilterGalleryGridItem tp = new FilterGalleryGridItem(imageAbsolutePath, GalleryImagePanel.SCALE_TO_FIT_ENABLED); 
				tp.addMouseListener(this);
				
				if(mModifiedImageSet.contains(imageAbsolutePath)) {
					tp.update(FileUtils.FILTERED_DIRECTORY + FileUtils.getShortFilename(imageAbsolutePath));
				}
				
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
				FilterGalleryGridItem tp = new FilterGalleryGridItem(imgPaths.get(currentIndex), GalleryImagePanel.SCALE_TO_FIT_ENABLED); 
				tp.addMouseListener(this);
				
				EventQueue.invokeLater(new Runnable() { public void run() {
				contentPanel.add(tp);
				}
				});
				
				currentIndex++;
				numberToPopulate--;
			}
			
			while(numberToPopulate > 0)
			{
				FilterGalleryGridItem tp = new FilterGalleryGridItem(NO_PREVIEW_AVAILABLE_IMAGE_PATH, GalleryImagePanel.SCALE_TO_FIT_ENABLED); 
				tp.addMouseListener(this);
				
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
				String imageAbsolutePath = imgPaths.get(currentIndex);

				FilterGalleryGridItem tp = new FilterGalleryGridItem(imageAbsolutePath, GalleryImagePanel.SCALE_TO_FIT_ENABLED); 
				tp.addMouseListener(this);
				
				if(mModifiedImageSet.contains(imageAbsolutePath)) {
					tp.update(FileUtils.FILTERED_DIRECTORY + FileUtils.getShortFilename(imageAbsolutePath));
				}
				
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
	
	private void applyFilterToImage(final String imageAbsolutePath, final EVideoTriageFilter filterToApply) {
		BufferedImage toFilter = null;
		
		if(mModifiedImageSet.contains(imageAbsolutePath) == false) {
			toFilter = ImageUtils.loadBufferedImage(imageAbsolutePath);
			mModifiedImageSet.add(imageAbsolutePath);
		} else {
			toFilter = ImageUtils.loadBufferedImage(FileUtils.FILTERED_DIRECTORY + FileUtils.getShortFilename(imageAbsolutePath));
			FileUtils.deleteFile(new File(FileUtils.FILTERED_DIRECTORY + FileUtils.getShortFilename(imageAbsolutePath)));
		}
		
		switch(filterToApply) {
			case SHARPEN_FILTER_STANDARD: {
				SharpenFilter sharpenFilter = new SharpenFilter(); 
				toFilter = sharpenFilter.filter(toFilter, null); 
				
				break;
			}
			case SHARPEN_FILTER_MASKING: {
				UnsharpFilter unsharpFilter = new UnsharpFilter(); 
				toFilter = unsharpFilter.filter(toFilter, null); 
				
				break;
			}
			case CONTRAST_FILTER_STANDARD: {
				ContrastFilter contrastFilter = new ContrastFilter(); 
				toFilter = contrastFilter.filter(toFilter, null); 
				
				break;
			}
			case CONVOLVE_FILTER_STANDARD: {
				ConvolveFilter convolveFilter = new ConvolveFilter(); 
				toFilter = convolveFilter.filter(toFilter, null); 
				
				break;
			}
			case GAUSSIAN_FILTER_STANDARD: {
				GaussianFilter gaussianFilter = new GaussianFilter(); 
				toFilter = gaussianFilter.filter(toFilter, null); 
				
				break;
			}
			case MEDIAN_FILTER_STANDARD: {
				MedianFilter medianFilter = new MedianFilter();
				toFilter = medianFilter.filter(toFilter, null); 
				
				break;
			}
			case REDUCE_NOISE_FILTER_STANDARD: {
				ReduceNoiseFilter reduceNoiseFilter = new ReduceNoiseFilter(); 
				toFilter = reduceNoiseFilter.filter(toFilter, null); 
				
				break;
			}
			case HISTOGRAM_EQUALIZATION_STANDARD: {
				HistogramEqualizationFilter histogramEqualizationFilter = new HistogramEqualizationFilter();
				toFilter = histogramEqualizationFilter.computeHistogramEQ(toFilter);
				
				break;
			}
			case CONTRAST_STRETCH_STANDARD: {
				ContrastStretchFilter contrastStretchFilter = new ContrastStretchFilter();
				toFilter = contrastStretchFilter.whiteBalanceBuffImage(toFilter);
				
				break;
			}
			case EMBOSS_STANDARD: {
				EmbossFilter embossFilter = new EmbossFilter();
				toFilter = embossFilter.filter(toFilter);
				
				break;
			}
			case CANNY_EDGE_STANDARD: {
				CannyEdgeFilter cannyEdgeFilter = new CannyEdgeFilter();
				cannyEdgeFilter.setSourceImage(toFilter);
				cannyEdgeFilter.process();
				toFilter = cannyEdgeFilter.getEdgesImage();
				
				break;
			}
		}
		
		FileUtils.deleteFile(new File(FileUtils.FILTERED_DIRECTORY + FileUtils.getShortFilename(imageAbsolutePath)));
		ImageUtils.saveBufferedImage(toFilter, FileUtils.FILTERED_DIRECTORY + FileUtils.getShortFilename(imageAbsolutePath));
	}
	
	private void saveAppliedFilters() {
		if(mModifiedImageSet.isEmpty() == true) {
			return;
		}
		
		final int confirmSaveChoice = UtilsLegacy.displayConfirmDialog("Notice", "This operation may not be undone.\n" +
				"Are you sure you want to save your changes?");
		
		if(confirmSaveChoice != JOptionPane.OK_OPTION) {			
			return;
		}
		
		for(String imageAbsolutePath : mModifiedImageSet) {
			FileUtils.deleteFile(new File(imageAbsolutePath));
			FileUtilsLegacy.moveFileToNewDirectory(FileUtils.FILTERED_DIRECTORY + FileUtils.getShortFilename(imageAbsolutePath), FileUtils.getFileDirectory(imageAbsolutePath));
			//mModifiedImageSet.remove(imageAbsolutePath);
		}
		
		mModifiedImageSet.clear();
		mFilterQueue.clear();
		
		refresh();
	}
	
	private void undoLastFilterStack() {		
		if(mUndoFileListStack.isEmpty() == true) {
			return;
		}
		
		ArrayList<String> undoFileList = mUndoFileListStack.peek();//pop();
		
		for(String absoluteImageFilePath : undoFileList) {
			if(mFilterOperationMap.containsKey(absoluteImageFilePath) == true) {				
				ArrayList<Stack<EVideoTriageFilter>> opstackList = mFilterOperationMap.get(absoluteImageFilePath);
				opstackList.remove(opstackList.size() - 1);
				
				mModifiedImageSet.remove(absoluteImageFilePath);
								
				if(opstackList.isEmpty() == true) {
					mFilterOperationMap.remove(absoluteImageFilePath);

					continue;
				}
				
				Stack<EVideoTriageFilter> opstack = opstackList.get(opstackList.size() - 1);
	
				for(int i = 0; i < opstack.size(); ++i) {
					applyFilterToImage(absoluteImageFilePath, opstack.get(i));
				}
				
				mFilterOperationMap.replace(absoluteImageFilePath, opstackList);				
				mModifiedImageSet.add(absoluteImageFilePath);
			}
		}

		refresh();
		mUndoFileListStack.pop();
	}
	
	private void performApplyAllFiltersAction() {
		disableFurtherActions();
		
		savePriorSelectedStates();		
				
		ArrayList<String> filenamesToDelete = new ArrayList<String>();
		
		for(String imageAbsolutePath : mPriorSelectedState) {
			filenamesToDelete.add(imageAbsolutePath);
		}	
			
		if(filenamesToDelete.isEmpty() == true
			|| mFilterQueue.isEmpty() == true) {
			return;
		}
		
		Thread markThread = new Thread() {
			@Override
			public void run() {
				ThreadUtils.addThreadToHandleList("GalleryGrid MarkAll", this);
				
				//
				for(String absoluteImageFilePath : filenamesToDelete) {
					if(mFilterOperationMap.containsKey(absoluteImageFilePath) == false) {
						mFilterOperationMap.put(absoluteImageFilePath, new ArrayList<Stack<EVideoTriageFilter>>());
					}
					
					Stack<EVideoTriageFilter> filterStack = new Stack<EVideoTriageFilter>();
					
					for(EVideoTriageFilter currentFilter : mFilterQueue) {
						filterStack.add(currentFilter);
					}
										
					mFilterOperationMap.get(absoluteImageFilePath).add(filterStack);
				}
				//
				
				for(EVideoTriageFilter currentFilter : mFilterQueue) {
					mUndoFileListStack.add(filenamesToDelete);

					for(String absoluteImageFilePath : filenamesToDelete) {
						applyFilterToImage(absoluteImageFilePath, currentFilter);
					}
				}
				
				mFilterQueue.clear();
				mFilterQueueListModel.clear();
				
				ThreadUtils.removeThreadFromHandleList(this);
			}
		};
			
		try {
			markThread.start();
			markThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
							
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
		
	private void savePriorSelectedStates() {				
		String imageAbsolutePath;
		
		for(int i = 0; i < contentPanel.getComponentCount(); ++i) {
			if(contentPanel.getComponent(i) instanceof FilterGalleryGridItem) {
				final FilterGalleryGridItem mCurrentGalleryGridItem = (FilterGalleryGridItem) contentPanel.getComponent(i);
				
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
			if(contentPanel.getComponent(i) instanceof FilterGalleryGridItem) {
				final FilterGalleryGridItem mCurrentGalleryGridItem = (FilterGalleryGridItem) contentPanel.getComponent(i);
				
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
		maxIndex = imgPaths.size();
		
		final int componentCount = contentPanel.getComponentCount();
		Component genericComponent = null;
		//GalleryGridItem galleryGridItem = null;

		for(int i = 0; i < componentCount; ++i) {
			genericComponent = contentPanel.getComponent(i);
			
			if(genericComponent instanceof FilterGalleryGridItem) {
				FilterGalleryGridItem galleryGridItem = (FilterGalleryGridItem) genericComponent;

				if(mPriorSelectedState.contains(galleryGridItem.getImageAbsolutePath()) == true
					|| mUndoFileListStack.peek().contains(galleryGridItem.getImageAbsolutePath()) == true) {
					EventQueue.invokeLater(new Runnable() { public void run() {
						if(mModifiedImageSet.contains(galleryGridItem.getImageAbsolutePath())) {
							galleryGridItem.update(FileUtils.FILTERED_DIRECTORY + FileUtils.getShortFilename(galleryGridItem.getImageAbsolutePath()));
						} else {
							galleryGridItem.update();
						}
					}
					});
				}
			}
		}
		
		updatePreviewPanel();
				
		EventQueue.invokeLater(new Runnable() { public void run() {
				contentPanel.revalidate();
			}
		});
	}

	private void updatePreviewPanel() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				if (mModifiedImageSet.contains(mPreviewImageAbsolutePath) == true) {
					mImagePreviewPanel.update(ImageUtils.loadBufferedImage(
							FileUtils.FILTERED_DIRECTORY + FileUtils.getShortFilename(mPreviewImageAbsolutePath)));
					mImagePreviewPanel.revalidate();					
				} else {
					mImagePreviewPanel.update(ImageUtils.loadBufferedImage(mPreviewImageAbsolutePath));
					mImagePreviewPanel.revalidate();					
				}
			}
		});
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0) {
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		Component clickedComponent = arg0.getComponent();
		
		if(clickedComponent instanceof FilterGalleryGridItem) {
			FilterGalleryGridItem clickedFilterGalleryGridItem = (FilterGalleryGridItem) clickedComponent;
			
			// dup update code?
			if(mModifiedImageSet.contains(clickedFilterGalleryGridItem.getImageAbsolutePath()) == true) {
				mImagePreviewPanel.update(ImageUtils.loadBufferedImage(FileUtils.FILTERED_DIRECTORY + FileUtils.getShortFilename(clickedFilterGalleryGridItem.getImageAbsolutePath())));
			} else {
				mImagePreviewPanel.update(ImageUtils.loadBufferedImage(clickedFilterGalleryGridItem.getImageAbsolutePath()));
			}
			//
			
			mPreviewImageAbsolutePath = clickedFilterGalleryGridItem.getImageAbsolutePath();
			
			mImagePreviewPanel.revalidate();
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		
	}
}

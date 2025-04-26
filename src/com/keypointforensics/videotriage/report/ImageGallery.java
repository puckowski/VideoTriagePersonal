package com.keypointforensics.videotriage.report;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import com.keypointforensics.videotriage.blob.context.BlobContextList;
import com.keypointforensics.videotriage.casestore.CaseMetadataWriter;
import com.keypointforensics.videotriage.chart.ReportChartSettings;
import com.keypointforensics.videotriage.gui.gallery.BaseImageGallery;
import com.keypointforensics.videotriage.gui.imagepanel.GalleryImagePanel;
import com.keypointforensics.videotriage.report.html.HtmlReportGenerator;
import com.keypointforensics.videotriage.thread.KeyFrameCheckThread;
import com.keypointforensics.videotriage.util.CursorUtils;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.FontUtils;
import com.keypointforensics.videotriage.util.ReportUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.Utils;
import com.keypointforensics.videotriage.util.WebUtils;
import com.keypointforensics.videotriage.util.WindowUtils;
import com.keypointforensics.videotriage.window.ChildWindowList;
import com.keypointforensics.videotriage.window.CloseChildrenWindowAdapter;
import com.keypointforensics.videotriage.window.WindowRegistry;

public class ImageGallery extends BaseImageGallery {

	/*
	 * Author: Daniel Puckowski
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 232646536271988938L;
	
	private static final String  NO_PREVIEW_AVAILABLE_IMAGE_PATH    = FileUtils.GRAPHICS_DIRECTORY + "no_preview_available.jpg";
	private static final boolean DEFAULT_REPORT_ICON_ENABLED        = true;
	private static final boolean DEFAULT_CUSTOM_REPORT_ICON_ENABLED = false;
	private static final boolean DEFAULT_REPORT_PAGINATION_ENABLED  = true;
	private static final boolean DEFAULT_METADATA_PAGE_ENABLED      = false;
	private static final boolean DEFAULT_STATISTICS_PAGE_ENABLED    = false;
	
	private final BlobContextList BLOB_CONTEXT_LIST;
	private final String CASE_NAME;
	
	private JButton mPreviousButton;
	private JButton mNextButton;
	private JButton mMarkAllButOneForCheckedButton;
	private JButton mMarkAllForCheckedButton;
	private JButton mMarkOneCheckedButton;
	private JButton mCreateReportButton;
	private JButton mSaveChangesButton;
			
	private JMenuItem mExitMenuItem;
	private JMenuItem mAdvancedSettingsMenuItem;
	private JMenuItem mAutomaticReviewMenuItem;
	private JMenuItem mPreviousMenuItem;
	private JMenuItem mNextMenuItem;
	private JMenuItem mMarkAllButOneMenuItem;
	private JMenuItem mMarkAllMenuItem;
	private JMenuItem mMarkOneMenuItem;
	private JMenuItem mCreateReportMenuItem;
	private JMenuItem mAboutMenuItem;
    private JMenuItem mChartSettingsMenuItem;
    private JMenuItem mSaveChangesMenuItem;
    private JMenuItem mDocumentationMenuItem;
    
	private boolean mReportIconEnabled;
	private boolean mCustomReportIconEnabled;
	private File    mCustomReportIconFile;
	private boolean mReportPaginationEnabled;
	private boolean mMetadataPageEnabled;
	private boolean mStatisticsPageEnabled;
	
	private ReportChartSettings mReportChartSettings;
	
	private ChildWindowList mChildWindowList;
	
	public ImageGallery(final String caseName, final String path, final BlobContextList blobContextList) {
		super(path, false);
		
		mChildWindowList = new ChildWindowList();
		
		BLOB_CONTEXT_LIST = blobContextList;
		CASE_NAME = caseName;
		
		mReportIconEnabled       = DEFAULT_REPORT_ICON_ENABLED;
		mCustomReportIconEnabled = DEFAULT_CUSTOM_REPORT_ICON_ENABLED;
		mReportPaginationEnabled = DEFAULT_REPORT_PAGINATION_ENABLED;
		mMetadataPageEnabled     = DEFAULT_METADATA_PAGE_ENABLED;
		mStatisticsPageEnabled   = DEFAULT_STATISTICS_PAGE_ENABLED;
		
		String contextFilename = CaseMetadataWriter.getContextFilenameFromDatabaseName(caseName);
		ArrayList<String> enhancedVideoFiles = CaseMetadataWriter.getVideoSourceListing(contextFilename);
		
		mReportChartSettings = new ReportChartSettings(enhancedVideoFiles);
	}
	
	public BlobContextList getBlobContextList() {
		return BLOB_CONTEXT_LIST;
	}
	
	public String getCapturePath() {
		return path;
	}
	
	public boolean getStatisticsPageEnabled() {
		return mStatisticsPageEnabled;
	}
	
	public void setStatisticsPageEnabled(final boolean newStatisticsPageEnabled) {
		mStatisticsPageEnabled = newStatisticsPageEnabled;
	}
	
	public boolean getMetadataPageEnabled() {
		return mMetadataPageEnabled;
	}
	
	public void setMetadataPageEnabled(final boolean newMetadataPageEnabled) {
		mMetadataPageEnabled = newMetadataPageEnabled;
	}
	
	public ReportChartSettings getReportChartSettings() {
		return mReportChartSettings;
	}
	
	public void setReportPaginationEnabled(final boolean newReportPaginationEnabled) {
		mReportPaginationEnabled = newReportPaginationEnabled;
	}
	
	public boolean getReportPaginationEnabled() {
		return mReportPaginationEnabled;
	}
	
	public void setCustomReportIconFile(final File newCustomReportIconFile) {
		mCustomReportIconFile = newCustomReportIconFile;
	}
	
	public File getCustomReportIconFile() {
		return mCustomReportIconFile;
	}
	
	public void setCustomReportIconEnabled(final boolean newCustomReportIconState) {
		mCustomReportIconEnabled = newCustomReportIconState;
	}
	
	public boolean getCustomReportIconEnabled() {
		return mCustomReportIconEnabled;
	}
	
	public void setReportIconEnabled(final boolean newReportIconState) {
		mReportIconEnabled = newReportIconState;
	}
	
	public boolean getReportIconEnabled() {
		return mReportIconEnabled;
	}

	protected void disableFurtherActions() {
		mMenuBar.setEnabled(false);
		
		mPreviousButton.setEnabled(false);
		mNextButton.setEnabled(false);
		mMarkAllButOneForCheckedButton.setEnabled(false);
		mMarkAllForCheckedButton.setEnabled(false);
		mMarkOneCheckedButton.setEnabled(false);
		mCreateReportButton.setEnabled(false);
		mSaveChangesButton.setEnabled(false);
		
		mAdvancedSettingsMenuItem.setEnabled(false);
		mAutomaticReviewMenuItem.setEnabled(false);
		mPreviousMenuItem.setEnabled(false);
		mNextMenuItem.setEnabled(false);
		mMarkAllButOneMenuItem.setEnabled(false);
		mMarkAllMenuItem.setEnabled(false);
		mMarkOneMenuItem.setEnabled(false);
		mCreateReportMenuItem.setEnabled(false);
		mAboutMenuItem.setEnabled(false);
		mChartSettingsMenuItem.setEnabled(false);
		mSaveChangesMenuItem.setEnabled(false);
		mDocumentationMenuItem.setEnabled(false);
		
		mDisableFurtherActions = true;
	}
	
	protected void enableFurtherActions() {
		mMenuBar.setEnabled(true);
		
		mPreviousButton.setEnabled(true);
		mNextButton.setEnabled(true);
		mMarkAllButOneForCheckedButton.setEnabled(true);
		mMarkAllForCheckedButton.setEnabled(true);
		mMarkOneCheckedButton.setEnabled(true);
		mCreateReportButton.setEnabled(true);
		mSaveChangesButton.setEnabled(true);
		
		mAdvancedSettingsMenuItem.setEnabled(true);
		mAutomaticReviewMenuItem.setEnabled(true);
		mPreviousMenuItem.setEnabled(true);
		mNextMenuItem.setEnabled(true);
		mMarkAllButOneMenuItem.setEnabled(true);
		mMarkAllMenuItem.setEnabled(true);
		mMarkOneMenuItem.setEnabled(true);
		mCreateReportMenuItem.setEnabled(true);
		mAboutMenuItem.setEnabled(true);
		mChartSettingsMenuItem.setEnabled(true);
		mSaveChangesMenuItem.setEnabled(true);
		mDocumentationMenuItem.setEnabled(true);
		
		mDisableFurtherActions = false;
	}
	
	protected void buildMenuBar() {		
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
						ThreadUtils.addThreadToHandleList("ImageGallery Exit", this);
						
						WindowRegistry.getInstance().closeFrames(mChildWindowList.getWindows());
						ImageGallery.this.dispose();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(mExitMenuItem);
		
		menu = new JMenu("Settings");
		mMenuBar.add(menu);
		
		mChartSettingsMenuItem = new JMenuItem("Chart Settings"); 
		mChartSettingsMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				if(mDisableFurtherActions == true) {
					
					return;
				}
				
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ImageGallery ChartSettings", this);
						
						ReportChartPreferencesWindow reportChartPreferencesWindow = new ReportChartPreferencesWindow(ImageGallery.this);
						mChildWindowList.addWindow(reportChartPreferencesWindow);
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(mChartSettingsMenuItem);
		
		mAdvancedSettingsMenuItem = new JMenuItem("Advanced Settings"); 
		mAdvancedSettingsMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				if(mDisableFurtherActions == true) {
					
					return;
				}
				
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ImageGallery Settings", this);
						
						ReportPreferencesWindow reportPreferencesWindow = new ReportPreferencesWindow(ImageGallery.this, mFalsePositiveRemover);
						mChildWindowList.addWindow(reportPreferencesWindow);
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(mAdvancedSettingsMenuItem);
		
		menu = new JMenu("Results");
		mMenuBar.add(menu);
		
		mAutomaticReviewMenuItem = new JMenuItem("Automatic Review"); 
		mAutomaticReviewMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				if(mDisableFurtherActions == true) {
					
					return;
				}
				
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ImageGallery Review", this);
	
						AutomaticReviewWindow automaticReviewWindow = new AutomaticReviewWindow(ImageGallery.this, mFalsePositiveRemover);
						mChildWindowList.addWindow(automaticReviewWindow);
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(mAutomaticReviewMenuItem);
		
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
						ThreadUtils.addThreadToHandleList("ImageGallery Prev", this);
						
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
						ThreadUtils.addThreadToHandleList("ImageGallery Next", this);
						
						performNextAction();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(mNextMenuItem);
		
		menu.addSeparator();
		
		mMarkAllButOneMenuItem = new JMenuItem("Mark All But One"); 
		mMarkAllButOneMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(mDisableFurtherActions == true) {
					
					return;
				}
				
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ImageGallery MarkAllBut", this);
						
						performMarkAllButOneAction();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(mMarkAllButOneMenuItem);
		
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
						ThreadUtils.addThreadToHandleList("ImageGallery MarkAll", this);
						
						performMarkAllAction();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(mMarkAllMenuItem);
		
		mMarkOneMenuItem = new JMenuItem("Mark One"); 
		mMarkOneMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(mDisableFurtherActions == true) {
					
					return;
				}
				
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ImageGallery MarkOne", this);
						
						performMarkOneAction();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(mMarkOneMenuItem);
		
		menu.addSeparator();
		
		mSaveChangesMenuItem = new JMenuItem("Save"); 
		mSaveChangesMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(mDisableFurtherActions == true) {
					
					return;
				}
				
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ImageGallery SaveChanges", this);
						
						performSaveChangesAction();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(mSaveChangesMenuItem);
		
		mCreateReportMenuItem = new JMenuItem("Create Report"); 
		mCreateReportMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				if(mDisableFurtherActions == true) {
					
					return;
				}
				
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ImageGallery Create", this);
						
						performCreateReportAction();
						
						WindowRegistry.getInstance().closeFrames(mChildWindowList.getWindows());
						ImageGallery.this.dispose();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(mCreateReportMenuItem);
		
		menu = new JMenu("Help");
		mMenuBar.add(menu);
		
		mDocumentationMenuItem = new JMenuItem("Open Documentation");
		mDocumentationMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ImageGallery OpenDoc", this);
						
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
						ThreadUtils.addThreadToHandleList("ImageGallery About", this);
						
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
						ThreadUtils.addThreadToHandleList("ImageGallery Prev", this);
						
						performPreviousAction();	
						
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
						ThreadUtils.addThreadToHandleList("ImageGallery Next", this);
						
						performNextAction();	
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();	
			}
		});
		
		mMarkAllButOneForCheckedButton = new JButton("Mark All But One");
		mMarkAllButOneForCheckedButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(mDisableFurtherActions == true) {
					
					return;
				}
				
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ImageGallery MarkAllBut", this);
						
						disableFurtherActions();
						
						CursorUtils.setBusyCursor(ImageGallery.this);
						
						performMarkAllButOneAction();
						
						CursorUtils.setDefaultCursor(ImageGallery.this);
						
						enableFurtherActions();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();	
			}
		});
		
		mMarkAllForCheckedButton = new JButton("Mark All");
		mMarkAllForCheckedButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(mDisableFurtherActions == true) {
					
					return;
				}
					
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ImageGallery MarkAll", this);
						
						disableFurtherActions();
						
						CursorUtils.setBusyCursor(ImageGallery.this);
						
						performMarkAllAction();
						
						CursorUtils.setDefaultCursor(ImageGallery.this);
						
						enableFurtherActions();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();	
			}
		});
		
		mMarkOneCheckedButton = new JButton("Mark One");
		mMarkOneCheckedButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(mDisableFurtherActions == true) {
					
					return;
				}
					
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ImageGallery MarkOne", this);
						
						disableFurtherActions();
						
						CursorUtils.setBusyCursor(ImageGallery.this);
						
						performMarkOneAction();
						
						CursorUtils.setDefaultCursor(ImageGallery.this);
						
						enableFurtherActions();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();	
			}
		});
		
		mSaveChangesButton = new JButton("Save");
		mSaveChangesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(mDisableFurtherActions == true) {
					
					return;
				}
					
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ImageGallery SaveChanges", this);
						
						disableFurtherActions();
						
						CursorUtils.setBusyCursor(ImageGallery.this);
						
						performSaveChangesAction();
						
						CursorUtils.setDefaultCursor(ImageGallery.this);
						
						enableFurtherActions();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();	
			}
		});
		
		mCreateReportButton = new JButton("Create Report");
		mCreateReportButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(mDisableFurtherActions == true) {
					
					return;
				}
				
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ImageGallery Create", this);
						
						disableFurtherActions();
						
						CursorUtils.setBusyCursor(ImageGallery.this);
						
						performCreateReportAction();
						
						CursorUtils.setDefaultCursor(ImageGallery.this);
						
						enableFurtherActions();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
		});
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(mPreviousButton);
		buttonPanel.add(mMarkAllButOneForCheckedButton);
		buttonPanel.add(mMarkAllForCheckedButton);
		buttonPanel.add(mMarkOneCheckedButton);
		buttonPanel.add(mSaveChangesButton);
		buttonPanel.add(mNextButton);
		buttonPanel.add(mCreateReportButton);
		
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
		
		this.addWindowListener(new CloseChildrenWindowAdapter(mChildWindowList));
		
		WindowUtils.setFrameIcon(this);
		this.setTitle("Create Report");
		this.setPreferredSize(new Dimension(900, 1000));
		this.pack();
		WindowUtils.maximize(this);
		this.setVisible(true);
		//WindowUtils.center(this);
	}
	
	private void performKeyFrameCheck() {
		ReportUtils.GLOBAL_REPORT_CONTEXT_LOCK.lock();
		
		KeyFrameCheckThread keyFrameCheckThread = new KeyFrameCheckThread(BLOB_CONTEXT_LIST.getContextFilename());
		keyFrameCheckThread.start();

		try {
			keyFrameCheckThread.join();
		} catch (InterruptedException interruptedException) {
			//interruptedException.printStackTrace();
		}
		
		boolean modifiedContextFile = keyFrameCheckThread.getModifiedContextFile();
		
		if(modifiedContextFile == true) {
			BLOB_CONTEXT_LIST.setContextFilename(BLOB_CONTEXT_LIST.getContextFilename() + ".tmp");
		}
		
		ReportUtils.GLOBAL_REPORT_CONTEXT_LOCK.unlock();
	}
	
	private void performCreateReportAction() {
		performKeyFrameCheck();
		
		disableFurtherActions();
		
		HtmlReportGenerator htmlReportGenerator = new HtmlReportGenerator(CASE_NAME, path, 
				mFalsePositiveRemover.getFalsePositiveBundle(), BLOB_CONTEXT_LIST,
				mReportIconEnabled, mCustomReportIconEnabled, mCustomReportIconFile,
				mReportPaginationEnabled, mReportChartSettings, mMetadataPageEnabled,
				mStatisticsPageEnabled);
		String reportFolderName = htmlReportGenerator.createRootPage();
		try {
			Desktop.getDesktop().open(new File(reportFolderName));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		clearPriorSelectedStates();
				
		WindowRegistry.getInstance().closeFrames(mChildWindowList.getWindows());
		this.dispose();
	}

	@Override
	protected void performSaveChangesAction() {
		// 
	}
	
}

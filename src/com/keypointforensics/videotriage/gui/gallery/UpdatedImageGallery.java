package com.keypointforensics.videotriage.gui.gallery;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import com.keypointforensics.videotriage.gui.imagepanel.GalleryImagePanel;
import com.keypointforensics.videotriage.legacy.FileUtilsLegacy;
import com.keypointforensics.videotriage.legacy.UtilsLegacy;
import com.keypointforensics.videotriage.report.GalleryGridItem;
import com.keypointforensics.videotriage.util.CursorUtils;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.FontUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.Utils;
import com.keypointforensics.videotriage.util.WebUtils;
import com.keypointforensics.videotriage.util.WindowUtils;
import com.keypointforensics.videotriage.window.CloseChildrenWindowAdapter;
import com.keypointforensics.videotriage.window.WindowRegistry;

public class UpdatedImageGallery extends BaseImageGallery {

	/*
	 * Author: Daniel Puckowski
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 232646536271988938L;
		
	private JButton mPreviousButton;
	private JButton mNextButton;
	private JButton mMarkAllButOneForCheckedButton;
	private JButton mMarkAllForCheckedButton;
	private JButton mMarkOneCheckedButton;
	private JButton mSaveChangesButton;
		
	private JMenuBar mMenuBar;
	
	private JMenuItem mExitMenuItem;
	private JMenuItem mAdvancedSettingsMenuItem;
	private JMenuItem mAutomaticReviewMenuItem;
	private JMenuItem mPreviousMenuItem;
	private JMenuItem mNextMenuItem;
	private JMenuItem mMarkAllButOneMenuItem;
	private JMenuItem mMarkAllMenuItem;
	private JMenuItem mMarkOneMenuItem;
	private JMenuItem mAboutMenuItem;
    private JMenuItem mSaveChangesMenuItem;
    private JMenuItem mDocumentationMenuItem;
    
	public UpdatedImageGallery(final String path) {
		super(path);
	}
	
	public String getRootPath() {		
		return path;
	}
	
	protected void disableFurtherActions() {
		mMenuBar.setEnabled(false);
		
		mPreviousButton.setEnabled(false);
		mNextButton.setEnabled(false);
		mMarkAllButOneForCheckedButton.setEnabled(false);
		mMarkAllForCheckedButton.setEnabled(false);
		mMarkOneCheckedButton.setEnabled(false);
		mSaveChangesButton.setEnabled(false);
		
		mAdvancedSettingsMenuItem.setEnabled(false);
		mAutomaticReviewMenuItem.setEnabled(false);
		mPreviousMenuItem.setEnabled(false);
		mNextMenuItem.setEnabled(false);
		mMarkAllButOneMenuItem.setEnabled(false);
		mMarkAllMenuItem.setEnabled(false);
		mMarkOneMenuItem.setEnabled(false);
		mAboutMenuItem.setEnabled(false);
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
		mSaveChangesButton.setEnabled(true);
		
		mAdvancedSettingsMenuItem.setEnabled(true);
		mAutomaticReviewMenuItem.setEnabled(true);
		mPreviousMenuItem.setEnabled(true);
		mNextMenuItem.setEnabled(true);
		mMarkAllButOneMenuItem.setEnabled(true);
		mMarkAllMenuItem.setEnabled(true);
		mMarkOneMenuItem.setEnabled(true);
		mAboutMenuItem.setEnabled(true);
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
						UpdatedImageGallery.this.dispose();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(mExitMenuItem);
		
		menu = new JMenu("Settings");
		mMenuBar.add(menu);
		
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
						
						UpdatedReportPreferencesWindow reportPreferencesWindow = new UpdatedReportPreferencesWindow(mFalsePositiveRemover);
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
	
						UpdatedAutomaticReviewWindow automaticReviewWindow = new UpdatedAutomaticReviewWindow(UpdatedImageGallery.this, mFalsePositiveRemover);
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
						
						CursorUtils.setBusyCursor(UpdatedImageGallery.this);
						
						performMarkAllButOneAction();
						
						CursorUtils.setDefaultCursor(UpdatedImageGallery.this);
						
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
						
						CursorUtils.setBusyCursor(UpdatedImageGallery.this);
						
						performMarkAllAction();
						
						CursorUtils.setDefaultCursor(UpdatedImageGallery.this);
						
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
						
						CursorUtils.setBusyCursor(UpdatedImageGallery.this);
						
						performMarkOneAction();
						
						CursorUtils.setDefaultCursor(UpdatedImageGallery.this);
						
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
						
						CursorUtils.setBusyCursor(UpdatedImageGallery.this);
						
						performSaveChangesAction();
						
						CursorUtils.setDefaultCursor(UpdatedImageGallery.this);
						
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
		
		this.addComponentListener(new ComponentListener() {
		    @Override
			public void componentResized(ComponentEvent componentEvent) {
		    	refresh();
		    }

			@Override
			public void componentHidden(ComponentEvent componentEvent) {				
			}

			@Override
			public void componentMoved(ComponentEvent componentEvent) {				
			}

			@Override
			public void componentShown(ComponentEvent componentEvent) {				
			}
		});
		
		this.addWindowListener(new CloseChildrenWindowAdapter(mChildWindowList));
		
		WindowUtils.setFrameIcon(this);
		this.setTitle("Review Search");
		this.setPreferredSize(new Dimension(900, 1000));
		this.pack();
		WindowUtils.maximize(this);
		this.setVisible(true);
		//WindowUtils.center(this);
	}

	@Override
	protected void performSaveChangesAction() {
		final int confirmSaveChoice = UtilsLegacy.displayConfirmDialog("Notice", "This operation cannot be undone.\n" +
				"Do you wish to delete filtered captures?");
		
		for(int i = 0; i < imgPaths.size(); ++i) {
			imgPaths.set(i, imgPaths.get(i).toLowerCase());
		}
		
		if(confirmSaveChoice == JOptionPane.OK_OPTION) {
			final ArrayList<String> listOfAllFiles = (ArrayList<String>) FileUtilsLegacy.parseDirectoryRecursiveForAll(path);
			
			for(int i = 0; i < listOfAllFiles.size(); ++i) {
				listOfAllFiles.set(i, listOfAllFiles.get(i).toLowerCase());
			}
			
			for(String possibleFileToDelete : listOfAllFiles) {
				if(imgPaths.contains(possibleFileToDelete) == false) {
					FileUtils.deleteFile(new File(possibleFileToDelete));
				}
			}
		}
		
		refresh();
	}

}

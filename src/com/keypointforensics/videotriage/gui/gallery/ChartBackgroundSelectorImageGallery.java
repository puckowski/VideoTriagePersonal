package com.keypointforensics.videotriage.gui.gallery;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
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

import com.keypointforensics.videotriage.gui.imagepanel.GalleryImagePanel;
import com.keypointforensics.videotriage.legacy.UtilsLegacy;
import com.keypointforensics.videotriage.legacy.WindowsVideoFrameExtractorLegacy;
import com.keypointforensics.videotriage.report.GalleryGridItem;
import com.keypointforensics.videotriage.util.CursorUtils;
import com.keypointforensics.videotriage.util.FontUtils;
import com.keypointforensics.videotriage.util.ImageUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.Utils;
import com.keypointforensics.videotriage.util.WebUtils;
import com.keypointforensics.videotriage.util.WindowUtils;
import com.keypointforensics.videotriage.window.CloseChildrenWindowAdapter;
import com.keypointforensics.videotriage.window.WindowRegistry;

public class ChartBackgroundSelectorImageGallery extends BaseFileListImageGallery {

	/*
	 * Author: Daniel Puckowski
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 232646536271988938L;
		
	private final String DEFAULT_BASE_64_FORMAT_NAME = "png";
	
	private JButton mPreviousButton;
	private JButton mNextButton;
	private JButton mSelectChangesButton;
		
	private JMenuBar mMenuBar;
	
	private JMenuItem mExitMenuItem;
	private JMenuItem mPreviousMenuItem;
	private JMenuItem mNextMenuItem;
	private JMenuItem mAboutMenuItem;
    private JMenuItem mSelectChangesMenuItem;
    private JMenuItem mDocumentationMenuItem;
    
    private String mBase64ImageData;
    private int mSelectedImageWidth;
    private int mSelectedImageHeight;
    
	public ChartBackgroundSelectorImageGallery(final ArrayList<String> listOfImageFiles) {
		super(listOfImageFiles);
		
		for(int i = 0; i < imgPaths.size(); ++i) {
			imgPaths.set(i, WindowsVideoFrameExtractorLegacy.extractPreviewFrame(imgPaths.get(i)));
		}
	}
	
	public String getBase64ImageData() {
		return mBase64ImageData;
	}
	
	public int getSelectedImageWidth() {
		return mSelectedImageWidth;
	}
	
	public int getSelectedImageHeight() {
		return mSelectedImageHeight;
	}
	
	protected void disableFurtherActions() {
		mMenuBar.setEnabled(false);
		
		mPreviousButton.setEnabled(false);
		mNextButton.setEnabled(false);
		mSelectChangesButton.setEnabled(false);
		
		mPreviousMenuItem.setEnabled(false);
		mNextMenuItem.setEnabled(false);
		mAboutMenuItem.setEnabled(false);
		mSelectChangesMenuItem.setEnabled(false);
		mDocumentationMenuItem.setEnabled(false);
		
		mDisableFurtherActions = true;
	}
	
	protected void enableFurtherActions() {
		mMenuBar.setEnabled(true);
		
		mPreviousButton.setEnabled(true);
		mNextButton.setEnabled(true);
		mSelectChangesButton.setEnabled(true);
		
		mPreviousMenuItem.setEnabled(true);
		mNextMenuItem.setEnabled(true);
		mAboutMenuItem.setEnabled(true);
		mSelectChangesMenuItem.setEnabled(true);
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
						ThreadUtils.addThreadToHandleList("BackgroundSelectorImageGallery Exit", this);
						
						WindowRegistry.getInstance().closeFrames(mChildWindowList.getWindows());
						ChartBackgroundSelectorImageGallery.this.dispose();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(mExitMenuItem);
		
		menu = new JMenu("Settings");
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
						ThreadUtils.addThreadToHandleList("BackgroundSelectorImageGallery Prev", this);
						
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
						ThreadUtils.addThreadToHandleList("BackgroundSelectorImageGallery Next", this);
						
						performNextAction();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(mNextMenuItem);
		
		mSelectChangesMenuItem = new JMenuItem("Select"); 
		mSelectChangesMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(mDisableFurtherActions == true) {
					
					return;
				}
				
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("BackgroundSelectorImageGallery SelectBackground", this);
						
						performSelectBackgroundAction();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(mSelectChangesMenuItem);
		
		menu = new JMenu("Help");
		mMenuBar.add(menu);
		
		mDocumentationMenuItem = new JMenuItem("Open Documentation");
		mDocumentationMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("BackgroundSelectorImageGallery OpenDoc", this);
						
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
						ThreadUtils.addThreadToHandleList("BackgroundSelectorImageGallery About", this);
						
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
		imageTabPane.addTab("Processed Videos", contentPanel);

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
						ThreadUtils.addThreadToHandleList("BackgroundSelectorImageGallery Prev", this);
						
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
						ThreadUtils.addThreadToHandleList("BackgroundSelectorImageGallery Next", this);
						
						performNextAction();	
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();	
			}
		});
		
		mSelectChangesButton = new JButton("Select");
		mSelectChangesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(mDisableFurtherActions == true) {
					
					return;
				}
					
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("BackgroundSelectorImageGallery SelectBackground", this);
						
						disableFurtherActions();
						
						CursorUtils.setBusyCursor(ChartBackgroundSelectorImageGallery.this);
						
						performSelectBackgroundAction();
						
						CursorUtils.setDefaultCursor(ChartBackgroundSelectorImageGallery.this);
						
						enableFurtherActions();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();	
			}
		});
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(mPreviousButton);
		buttonPanel.add(mSelectChangesButton);
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
		this.setTitle("Select Background");
		this.setPreferredSize(new Dimension(900, 1000));
		this.pack();
		WindowUtils.maximize(this);
		this.setVisible(true);
		//WindowUtils.center(this);
	}
	
	@Override
	protected void performSelectBackgroundAction() {
		savePriorSelectedStates();
		
		ArrayList<String> selectedBackgroundImages = new ArrayList<String>();
		
		for(String imageAbsolutePath : mPriorSelectedState) {
			selectedBackgroundImages.add(imageAbsolutePath);
		}	
			
		if(selectedBackgroundImages.size() > 1) {
			UtilsLegacy.displayMessageDialog("Notice", "Only one background image may be selected.\n"
				+ "A chart may only have one background.");
		} else if(selectedBackgroundImages.size() == 1) {
			BufferedImage selectedImage = ImageUtils.loadBufferedImage(selectedBackgroundImages.get(0));
			
			mSelectedImageWidth = selectedImage.getWidth();
			mSelectedImageHeight = selectedImage.getHeight();
			
			mBase64ImageData = ImageUtils.getImageBase64String(selectedImage, DEFAULT_BASE_64_FORMAT_NAME);
			
			this.dispose();
		} else {
			UtilsLegacy.displayMessageDialog("Notice", "No background image has been selected.\n"
					+ "Please select a background image.");
		}
	}

}

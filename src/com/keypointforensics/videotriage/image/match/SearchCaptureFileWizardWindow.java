package com.keypointforensics.videotriage.image.match;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.keypointforensics.videotriage.gui.gallery.FileListImageGallery;
import com.keypointforensics.videotriage.gui.imagepanel.ScalableSimpleImagePanel;
import com.keypointforensics.videotriage.gui.main.FileSelectImagePreviewAccessory;
import com.keypointforensics.videotriage.legacy.FileUtilsLegacy;
import com.keypointforensics.videotriage.progress.ProgressBundle;
import com.keypointforensics.videotriage.util.BorderUtils;
import com.keypointforensics.videotriage.util.CursorUtils;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ImageUtils;
import com.keypointforensics.videotriage.util.LocalFileWizardUtils;
import com.keypointforensics.videotriage.util.ProgressUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.Utils;
import com.keypointforensics.videotriage.util.WebUtils;
import com.keypointforensics.videotriage.util.WindowUtils;
import com.keypointforensics.videotriage.window.WindowRegistry;

public class SearchCaptureFileWizardWindow extends JFrame implements ActionListener, ItemListener, ChangeListener {

	/*
	 * Author: Daniel Puckowski
	 */
	
	private static final long serialVersionUID = 4271388619852805707L;
	
	private static final BufferedImage NO_PREVIEW_AVAILABLE_IMAGE = ImageUtils.loadBufferedImage(FileUtils.GRAPHICS_DIRECTORY + "no_preview_available.jpg");

	private static final int CONTROL_GRID_LAYOUT_ROWS     = 10;
	private static final int CONTROL_GRID_LAYOUT_COLUMNS  = 1;
	
	private final String DATABASE_NAME;
	private final double DEFAULT_SURF_FREE_ORIENTED_MATCH_PERCENT = 10.0;
	
	private CaptureFileDropList mFileDropList;
	private JButton mSelectFilesButton;
	private JButton mStartButton;
	private ScalableSimpleImagePanel mPreviewPanel;
	private JButton mRemoveSelectedButton;
	private JCheckBox mShowPreviewsCheckBox;
	private JSlider mSurfSimilaritySlider;
	
	private double mSurfFreeOrientedMatchPercent;
	
	public SearchCaptureFileWizardWindow(final String databaseName) {		
		DATABASE_NAME = databaseName;
		
		mSurfFreeOrientedMatchPercent = DEFAULT_SURF_FREE_ORIENTED_MATCH_PERCENT;
		
		buildFrame();
	}
	
	private void buildFrame() {		
		buildMenuBar();
		
		this.setLayout(new BorderLayout());
		
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new GridLayout(CONTROL_GRID_LAYOUT_ROWS, CONTROL_GRID_LAYOUT_COLUMNS));
		contentPanel.setBorder(BorderUtils.getEmptyBorder());				
		
		JScrollPane contentScrollPane = new JScrollPane(contentPanel);
		contentScrollPane.setPreferredSize(new Dimension(440, 720));
		WindowUtils.setScrollBarIncrement(contentScrollPane);
		
		JTabbedPane settingsTabPane = new JTabbedPane();
		settingsTabPane.addTab("Search Captures Controls", contentScrollPane);
		
		JPanel previewContentPanel = new JPanel();
		previewContentPanel.setLayout(new BorderLayout());
		previewContentPanel.setBorder(BorderUtils.getEmptyBorder()); 
		
		mPreviewPanel = new ScalableSimpleImagePanel(true);
		mPreviewPanel.update(NO_PREVIEW_AVAILABLE_IMAGE);
		mPreviewPanel.setPreferredSize(new Dimension(320, 320));
		mPreviewPanel.setBorder(BorderUtils.getEmptyBorder());
			
		JTextArea videoInformationTextArea = new JTextArea(10, 60);
		videoInformationTextArea.setEditable(false);
		JScrollPane videoInformationTextAreaScrollPane = new JScrollPane(videoInformationTextArea);
		WindowUtils.setScrollBarIncrement(videoInformationTextAreaScrollPane);
		
		previewContentPanel.add(new JLabel("Search Image Preview"), BorderLayout.NORTH);
		previewContentPanel.add(mPreviewPanel, BorderLayout.CENTER);
		
		JLabel fileSelectionLabel = new JLabel("List of Files to Search By");
		contentPanel.add(fileSelectionLabel);
		
		mFileDropList = new CaptureFileDropList(mPreviewPanel);
		mFileDropList.setPreferredSize(new Dimension(600, 300));
		
		mSelectFilesButton = new JButton("Select Files");
		mSelectFilesButton.addActionListener(this);
		contentPanel.add(mSelectFilesButton);
		
		mRemoveSelectedButton = new JButton("Remove Selected Image");
		mRemoveSelectedButton.addActionListener(this);
		contentPanel.add(mRemoveSelectedButton);
		        
		mShowPreviewsCheckBox = new JCheckBox("Show Icon Previews", true);
		mShowPreviewsCheckBox.addItemListener(this);
		contentPanel.add(mShowPreviewsCheckBox);
		
		JLabel searchSimilarityLevelLabel = new JLabel("Search Similarity Level");
		contentPanel.add(searchSimilarityLevelLabel);
		
		int sliderMin = 1;
		int sliderMax = 100;
		int sliderInit = (int) Math.round(mSurfFreeOrientedMatchPercent);
		mSurfSimilaritySlider = new JSlider(JSlider.HORIZONTAL, sliderMin,
				sliderMax, sliderInit);
		mSurfSimilaritySlider.addChangeListener(this);
		try {
			mSurfSimilaritySlider.removeMouseWheelListener(mSurfSimilaritySlider.getMouseWheelListeners()[0]);
		} catch(Exception exception) {	

		}
		mSurfSimilaritySlider.setMajorTickSpacing(9);
		mSurfSimilaritySlider.setPaintTicks(true);
		mSurfSimilaritySlider.setPaintLabels(true);
		contentPanel.add(mSurfSimilaritySlider);
		
		mStartButton = new JButton("Start Processing");
		mStartButton.addActionListener(this);
		contentPanel.add(mStartButton);
		
		JSplitPane leftSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, settingsTabPane, mFileDropList);
		JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSplitPane, previewContentPanel);
		
		this.add(mainSplitPane, BorderLayout.CENTER);
		
		WindowRegistry.getInstance().registerFrame(this, "SearchCaptureFileWizard");
		
		this.setTitle("Search Capture File Wizard");
		this.pack();
		WindowUtils.setFrameIcon(this);
		WindowUtils.maximize(this);
		this.setVisible(true);
		//WindowUtils.center(this);
	}
	
	@Override
	public void itemStateChanged(ItemEvent event) {
		if (event.getSource() instanceof JCheckBox) {
			JCheckBox checkBox = (JCheckBox) event.getSource();

			if(checkBox == mShowPreviewsCheckBox) {
				mFileDropList.setShowIconPreviews(mShowPreviewsCheckBox.isSelected());
			}
		}
	}
	
	private void performSelectFilesAction() {		
		Boolean old = UIManager.getBoolean("FileChooser.readOnly");  
		UIManager.put("FileChooser.readOnly", Boolean.TRUE);  		
		
		final JFileChooser fileChooser = new JFileChooser(); 
		fileChooser.setMultiSelectionEnabled(true);
		final FileSelectImagePreviewAccessory imagePreviewAccessory = new FileSelectImagePreviewAccessory();
		fileChooser.setAccessory(imagePreviewAccessory);
		fileChooser.addPropertyChangeListener(imagePreviewAccessory);
		
		final int fileChooserResult = fileChooser.showOpenDialog(this);
				
		if(fileChooserResult == JFileChooser.APPROVE_OPTION) {
			CursorUtils.setBusyCursor(this);
			
			final File[] selectedFiles = fileChooser.getSelectedFiles();
			//File selectedFile = null;
			
			for(int i = 0; i < selectedFiles.length; ++i) {
				final File selectedFile = selectedFiles[i];
				
				if(selectedFile.isDirectory()) {
					ArrayList<String> allFilesInFolderAndSubfolders = (ArrayList<String>) FileUtilsLegacy.parseDirectoryRecursiveForAll(selectedFile.getAbsolutePath());
												
					for(String pathOfFileInFolder : allFilesInFolderAndSubfolders) {
						EventQueue.invokeLater(new Runnable() { public void run() {
								mFileDropList.addFile(pathOfFileInFolder);
								mFileDropList.revalidate();
							}
						});
					}
				}
				else {
					EventQueue.invokeLater(new Runnable() { public void run() {
							mFileDropList.addFile(selectedFile.getAbsolutePath());
							mFileDropList.revalidate();
						}
					});
				}
			}
			
			CursorUtils.setDefaultCursor(this);
		}
		
		//mFileDropList.revalidate();
		
		UIManager.put("FileChooser.readOnly", old); 
	}
	
	private String getCaptureDirectoryFromDatabasePath() {
		String dbNameFmt = DATABASE_NAME;
		
		if(dbNameFmt.contains(File.separator)) {
			dbNameFmt = dbNameFmt.substring(dbNameFmt.lastIndexOf(File.separator) + 1, dbNameFmt.length());
		}
		
		//if(dbNameFmt.contains(".")) {
		//	dbNameFmt = dbNameFmt.substring(0, dbNameFmt.lastIndexOf("."));
		//}
		
		return FileUtils.CAPTURES_DIRECTORY + dbNameFmt;
	}
	
	private void performStartAction() {
		CursorUtils.setBusyCursor(this);
		
		String capturesDirectory = getCaptureDirectoryFromDatabasePath();
		
		ArrayList<String> allCapturePaths = (ArrayList<String>) FileUtilsLegacy.parseDirectoryRecursiveForAll(capturesDirectory);
		
		SurfComparator surfComparator = new SurfComparator(false);
		double matchPercent;

		ArrayList<String> localFilesToProcess = LocalFileWizardUtils.getLocalWizardListOfFiles(mFileDropList.getFileList());
		
		ProgressBundle searchProgressBundle = ProgressUtils.getProgressBundle("Searching Captures...", allCapturePaths.size());

		ArrayList<String> filesToReview = new ArrayList<String>();
		
		for(String localFileToProcess : localFilesToProcess) {
			surfComparator.init(ImageUtils.loadBufferedImage(localFileToProcess));
			
			for(String capturePath : allCapturePaths) {
				matchPercent = surfComparator.compare(ImageUtils.loadBufferedImage(capturePath));
							
				if(matchPercent >= mSurfFreeOrientedMatchPercent) {
					filesToReview.add(capturePath);
				}
				
				searchProgressBundle.progressBar.setValue(searchProgressBundle.progressBar.getValue() + 1);
				searchProgressBundle.progressBar.repaint();
			}
		}
		
		searchProgressBundle.progressBar.setValue(searchProgressBundle.progressBar.getValue() + 1);
		searchProgressBundle.progressBar.repaint();
		searchProgressBundle.frame.dispose();
		
		CursorUtils.setDefaultCursor(this);
		
		//SearchImageGallery searchImageGallery = new SearchImageGallery(filesToReview);
		//searchImageGallery.build();
		
		if(filesToReview.isEmpty() == false) {
			FileListImageGallery fileListImageGallery = new FileListImageGallery(filesToReview);
			fileListImageGallery.build();
		} else {
			Utils.displayMessageDialog("None Found", "No similar images found in database.");
		}
		
		this.dispose();
	}
	
	private void performRemoveSelectedVideoAction() {
		CursorUtils.setBusyCursor(this);
		
		mFileDropList.removeSelectedIndex();
		
		CursorUtils.setDefaultCursor(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() instanceof JButton) {
			JButton button = (JButton) event.getSource();

			if(button == mSelectFilesButton) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SearchCaptureWizWindow SelectFiles", this);
							
						performSelectFilesAction();
							
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			else if(button == mStartButton) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SearchCaptureWizWindow PerformStart", this);
							
						performStartAction();
							
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			else if(button == mRemoveSelectedButton) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SearchCaptureWizWindow RemoveSelected", this);
							
						performRemoveSelectedVideoAction();
							
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
		}
	}
	
	private void buildMenuBar() {		
		JMenuBar menuBar;
		JMenu menu;

		menuBar = new JMenuBar();

		menu = new JMenu("File");
		menuBar.add(menu);
		
		JMenuItem menuItem = new JMenuItem("Exit");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SearchCaptureWizWindow Exit", this);
						
						SearchCaptureFileWizardWindow.this.dispose();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menu = new JMenu("Search");
		menuBar.add(menu);
		
		menuItem = new JMenuItem("Select Files");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SearchCaptureWizWindow Select", this);
						
						performSelectFilesAction();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Remove Selected Image");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SearchCaptureWizWindow RemoveSelected", this);
							
						performRemoveSelectedVideoAction();
							
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Start Processing");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SearchCaptureWizWindow Start", this);
						
						performStartAction();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menu = new JMenu("Help");
		menuBar.add(menu);
		
		menuItem = new JMenuItem("Open Documentation");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SearchCaptureWizWindow OpenDoc", this);
						
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
		menu.add(menuItem);
		
		menuItem = new JMenuItem("About");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SearchCaptureWizWindow About", this);
						
						Utils.displayMessageDialog("About", Utils.SOFTWARE_NAME + " \nVersion: " + Utils.SOFTWARE_VERSION + "\n© " + Utils.AUTHOR_NAME);
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		this.setJMenuBar(menuBar);
	}
	
	@Override
	public void stateChanged(ChangeEvent changeEvent) {
		Object changeSource = changeEvent.getSource();
		
		if(changeSource instanceof JSlider) {
			JSlider sliderSource = (JSlider) changeSource;
			
			if(sliderSource == mSurfSimilaritySlider) {
				mSurfFreeOrientedMatchPercent = sliderSource.getValue();
			}
		}
	}
}

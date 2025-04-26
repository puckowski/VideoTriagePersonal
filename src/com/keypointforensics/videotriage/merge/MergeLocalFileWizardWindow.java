package com.keypointforensics.videotriage.merge;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import com.keypointforensics.videotriage.gui.imagepanel.ScalableSimpleImagePanel;
import com.keypointforensics.videotriage.gui.localfile.wizard.FileDropList;
import com.keypointforensics.videotriage.gui.localfile.wizard.IconDecoratedListModel;
import com.keypointforensics.videotriage.gui.main.FileSelectVideoPreviewAccessory;
import com.keypointforensics.videotriage.gui.resize.ResizeLocalFileWizardWindow;
import com.keypointforensics.videotriage.legacy.FileUtilsLegacy;
import com.keypointforensics.videotriage.thread.CreateResizedVideoThread;
import com.keypointforensics.videotriage.thread.MergeLocalVideoFilesThread;
import com.keypointforensics.videotriage.util.BorderUtils;
import com.keypointforensics.videotriage.util.CursorUtils;
import com.keypointforensics.videotriage.util.ImageUtils;
import com.keypointforensics.videotriage.util.LocalFileWizardUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.Utils;
import com.keypointforensics.videotriage.util.WebUtils;
import com.keypointforensics.videotriage.util.WindowUtils;
import com.keypointforensics.videotriage.window.WindowRegistry;

public class MergeLocalFileWizardWindow extends JFrame implements ActionListener, ItemListener {

	/*
	 * Author: Daniel Puckowski
	 */
	
	private static final long serialVersionUID = 4271388619852805707L;
	
	private static final int CONTROL_GRID_LAYOUT_ROWS     = 12;
	private static final int CONTROL_GRID_LAYOUT_COLUMNS  = 1;
	
	private FileDropList mFileDropList;
	private JButton mSelectFilesButton;
	private JButton mIncreaseRankButton;
	private JButton mDecreaseRankButton;
	private JButton mStartButton;
	private ScalableSimpleImagePanel mPreviewPanel;
	private JButton mRemoveSelectedButton;
	private JCheckBox mShowPreviewsCheckBox;
	
	//
	private final String[] PREVIEW_RESOLUTION_LIST =  { 
		"320 x 240   4:3",
		"480 x 320   3:2",
		"640 x 360   16:9",
		"960 x 540   16:9",
		"960 x 640   3:2",
		"960 x 720   4:3",
		"1024 x 768  4:3",
		"1280 x 720  16:9",
		"1280 x 960  4:3",
		"1366 x 768  16:9",
		"1440 x 960  3:2",
		"1600 x 900  16:9",
		"1920 x 1080 16:9",
		"1920 x 1280 3:2",
		"1920 x 1440 4:3",
		"2160 x 1440 3:2",
		"2560 x 1440 16:9"
	};
	
	private final String[] FRAME_RATE_LIST =  { 
		"24 frames per second",
		"30 frames per second",
		"48 frames per second",
		"60 frames per second"
	};

	private final String CONTEXT_FILENAME;
	
	private JComboBox<String> mResolutionComboBox = new JComboBox<String>(PREVIEW_RESOLUTION_LIST);
	private JComboBox<String> mFrameRateComboBox = new JComboBox<String>(FRAME_RATE_LIST);

	public MergeLocalFileWizardWindow(final String contextFilename) {		
		CONTEXT_FILENAME = contextFilename;

		buildFrame();
				
		WindowRegistry.getInstance().registerFrame(this, "MergeLocalFileWizard");
	}
	
	private int getSelectedPreviewHeight() {
		String selection = (String) mResolutionComboBox.getSelectedItem();
		
		selection = selection.substring(selection.indexOf("x") + 2, selection.length());
		selection = selection.substring(0, selection.indexOf(" "));
		
		return Integer.parseInt(selection);
	}
	
	private int getSelectedPreviewWidth() {
		String selection = (String) mResolutionComboBox.getSelectedItem();
		
		selection = selection.substring(0, selection.indexOf(" "));
		
		return Integer.parseInt(selection);
	}
	
	private int getSelectedFrameRate() {
		String selection = (String) mFrameRateComboBox.getSelectedItem();
		
		selection = selection.substring(0, selection.indexOf(" "));
		
		return Integer.parseInt(selection);
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
		settingsTabPane.addTab("Merge Local File Controls", contentScrollPane);
		
		JPanel previewContentPanel = new JPanel();
		previewContentPanel.setLayout(new BorderLayout());
		previewContentPanel.setBorder(BorderUtils.getEmptyBorder()); 
		
		mPreviewPanel = new ScalableSimpleImagePanel(true);//DeprecatedTitlePanel("", true);
		mPreviewPanel.update(ImageUtils.NO_PREVIEW_AVAILABLE_IMAGE);
		mPreviewPanel.setPreferredSize(new Dimension(320, 320));
		mPreviewPanel.setBorder(BorderUtils.getEmptyBorder());
		
		JPanel textAreaPanel = new JPanel();
		textAreaPanel.setLayout(new BorderLayout());
		
		JTextArea videoInformationTextArea = new JTextArea(10, 60);
		videoInformationTextArea.setEditable(false);
		JScrollPane videoInformationTextAreaScrollPane = new JScrollPane(videoInformationTextArea);
		WindowUtils.setScrollBarIncrement(videoInformationTextAreaScrollPane);
		
		textAreaPanel.add(new JLabel("Video File Information"), BorderLayout.NORTH);
		textAreaPanel.add(videoInformationTextAreaScrollPane, BorderLayout.CENTER);
		
		previewContentPanel.add(new JLabel("Video Preview"), BorderLayout.NORTH);
		previewContentPanel.add(mPreviewPanel, BorderLayout.CENTER);
		previewContentPanel.add(textAreaPanel, BorderLayout.SOUTH);
		
		JLabel fileSelectionLabel = new JLabel("List of Files to Process");
		contentPanel.add(fileSelectionLabel);
		
		mFileDropList = new FileDropList(mPreviewPanel, videoInformationTextArea);
		mFileDropList.setPreferredSize(new Dimension(600, 300));
		
		mSelectFilesButton = new JButton("Select Files");
		mSelectFilesButton.addActionListener(this);
		contentPanel.add(mSelectFilesButton);
		
		mIncreaseRankButton = new JButton("Increase Video Rank");
		mIncreaseRankButton.addActionListener(this);
		contentPanel.add(mIncreaseRankButton);
		
		mDecreaseRankButton = new JButton("Decrease Video Rank");
		mDecreaseRankButton.addActionListener(this);
		contentPanel.add(mDecreaseRankButton);
		
		mRemoveSelectedButton = new JButton("Remove Selected Video");
		mRemoveSelectedButton.addActionListener(this);
		contentPanel.add(mRemoveSelectedButton);
		    
		JLabel resolutionComboBoxLabel = new JLabel("Video Resize Resolution");
		contentPanel.add(resolutionComboBoxLabel);
		
		mResolutionComboBox.setSelectedIndex(-1);
		contentPanel.add(mResolutionComboBox);
		
		JLabel frameRateComboBoxLabel = new JLabel("Video Frame Rate");
		contentPanel.add(frameRateComboBoxLabel);
		
		mFrameRateComboBox.setSelectedIndex(-1);
		contentPanel.add(mFrameRateComboBox);
		
		mShowPreviewsCheckBox = new JCheckBox("Show Icon Previews", true);
		mShowPreviewsCheckBox.addItemListener(this);
		contentPanel.add(mShowPreviewsCheckBox);
		
		mStartButton = new JButton("Start Processing");
		mStartButton.addActionListener(this);
		contentPanel.add(mStartButton);
		
		JSplitPane leftSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, settingsTabPane, mFileDropList);
		JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSplitPane, previewContentPanel);
		
		this.add(mainSplitPane, BorderLayout.CENTER);
		
		this.setTitle("Merge Local File Wizard");
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
		final FileSelectVideoPreviewAccessory imagePreviewAccessory = new FileSelectVideoPreviewAccessory();
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
	
	private void performStartAction() {
		ArrayList<String> localFilesToProcess = LocalFileWizardUtils.getLocalWizardListOfFiles(mFileDropList.getFileList());

		if(localFilesToProcess.isEmpty() == true) {
			return;
		} 
		
		if(mResolutionComboBox.getSelectedIndex() == -1) {
			Utils.displayMessageDialog("No Resolution", "Please select a merged video resolution from the drop-down menu.");
			
			return;
		}
		else if(mFrameRateComboBox.getSelectedIndex() == -1) {
			Utils.displayMessageDialog("No Frame Rate", "Please select a merged video frame rate from the drop-down menu.");
			
			return;
		}
				
		MergeLocalVideoFilesThread mergeLocalVideoFilesThread = new MergeLocalVideoFilesThread(this, CONTEXT_FILENAME,
				localFilesToProcess, getSelectedPreviewWidth(), getSelectedPreviewHeight(), getSelectedFrameRate()); 
		mergeLocalVideoFilesThread.start();
		
		try {
			mergeLocalVideoFilesThread.join();
		} catch (InterruptedException interruptedException) {
			//interruptedException.printStackTrace();
		}
	
		mFileDropList.clearListAndPreview();
	}
	
	private void performIncreaseVideoRankAction() {
		CursorUtils.setBusyCursor(this);
		
		final IconDecoratedListModel iconDecoratedListModel = mFileDropList.getListModel();
		final JList fileList = mFileDropList.getList();

		final int selectedIndex = fileList.getSelectedIndex();
		
		if(selectedIndex < 0) {
			CursorUtils.setDefaultCursor(this);
			
			return;
		}
		
		if(selectedIndex > 0) {
			final int precedingIndex = selectedIndex - 1;
			
			Object selectedComponent = iconDecoratedListModel.getElementAt(selectedIndex);
			Object precedingComponent = iconDecoratedListModel.getElementAt(precedingIndex);
			
			iconDecoratedListModel.setElementAt(selectedComponent, precedingIndex);
			iconDecoratedListModel.setElementAt(precedingComponent, selectedIndex);
		}
		
		CursorUtils.setDefaultCursor(this);
	}
	
	private void performDecreaseVideoRankAction() {
		CursorUtils.setBusyCursor(this);
		
		final IconDecoratedListModel iconDecoratedListModel = mFileDropList.getListModel();
		final JList fileList = mFileDropList.getList();
		
		final int selectedIndex = fileList.getSelectedIndex();
		
		if(selectedIndex < 0) {
			CursorUtils.setDefaultCursor(this);
			
			return;
		}
		
		if(selectedIndex < (iconDecoratedListModel.getSize() - 1)) {
			final int followingIndex = selectedIndex + 1;
			
			Object selectedComponent = iconDecoratedListModel.getElementAt(selectedIndex);
			Object followingComponent = iconDecoratedListModel.getElementAt(followingIndex);
			
			iconDecoratedListModel.setElementAt(selectedComponent, followingIndex);
			iconDecoratedListModel.setElementAt(followingComponent, selectedIndex);
		}
		
		CursorUtils.setDefaultCursor(this);
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
						ThreadUtils.addThreadToHandleList("MergeLocFileWizWindow SelectFiles", this);
							
						performSelectFilesAction();
							
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			else if(button == mStartButton) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("MergeLocFileWizWindow PerformStart", this);
							
						performStartAction();
							
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			else if(button == mRemoveSelectedButton) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("MergeLocFileWizWindow RemoveSelected", this);
							
						performRemoveSelectedVideoAction();
							
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			else if(button == mIncreaseRankButton) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("MergeLocFileWizWindow IncreaseRank", this);
							
						performIncreaseVideoRankAction();
							
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			else if(button == mDecreaseRankButton) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("MergeLocFileWizWindow DecreaseRank", this);
							
						performDecreaseVideoRankAction();
							
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
						ThreadUtils.addThreadToHandleList("MergeLocFileWizWindow Exit", this);
						
						MergeLocalFileWizardWindow.this.dispose();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menu = new JMenu("Merge");
		menuBar.add(menu);
		
		menuItem = new JMenuItem("Select Files");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("MergeLocFileWizWindow Select", this);
						
						performSelectFilesAction();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		
		menu.add(menuItem);
		menuItem = new JMenuItem("Increase Video Rank");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("MergeLocFileWizWindow IncreaseRank", this);
							
						performIncreaseVideoRankAction();
							
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menu.add(menuItem);
		menuItem = new JMenuItem("Decrease Video Rank");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("MergeLocFileWizWindow DecreaseRank", this);
							
						performDecreaseVideoRankAction();
							
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menu.add(menuItem);
		menuItem = new JMenuItem("Remove Selected Video");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("MergeLocFileWizWindow RemoveSelected", this);
							
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
						ThreadUtils.addThreadToHandleList("MergeLocFileWizWindow Start", this);
						
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
						ThreadUtils.addThreadToHandleList("MergeLocFileWizWindow OpenDoc", this);
						
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
						ThreadUtils.addThreadToHandleList("MergeLocFileWizWindow Exit", this);
						
						Utils.displayMessageDialog("About", Utils.SOFTWARE_NAME + " \nVersion: " + Utils.SOFTWARE_VERSION + "\n© " + Utils.AUTHOR_NAME);
					
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		this.setJMenuBar(menuBar);
	}
}

package com.keypointforensics.videotriage.gui.localfile.wizard;

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

import com.keypointforensics.videotriage.audit.CaseAuditor;
import com.keypointforensics.videotriage.audit.LogLevel;
import com.keypointforensics.videotriage.gui.imagepanel.ScalableSimpleImagePanel;
import com.keypointforensics.videotriage.gui.main.FileSelectVideoPreviewAccessory;
import com.keypointforensics.videotriage.gui.main.GuiMain;
import com.keypointforensics.videotriage.gui.main.LocalEvidenceMetadataWindow;
import com.keypointforensics.videotriage.legacy.FileUtilsLegacy;
import com.keypointforensics.videotriage.staticparams.LocalFileRuntimeParams;
import com.keypointforensics.videotriage.thread.ProcessLocalFileThread;
import com.keypointforensics.videotriage.util.BorderUtils;
import com.keypointforensics.videotriage.util.CursorUtils;
import com.keypointforensics.videotriage.util.ImageUtils;
import com.keypointforensics.videotriage.util.LocalFileWizardUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.Utils;
import com.keypointforensics.videotriage.util.WebUtils;
import com.keypointforensics.videotriage.util.WindowUtils;
import com.keypointforensics.videotriage.window.ChildWindowList;
import com.keypointforensics.videotriage.window.CloseChildrenWindowAdapter;
import com.keypointforensics.videotriage.window.WindowRegistry;

public class LocalFileWizardWindow extends JFrame implements ChangeListener, ActionListener, ItemListener {

	/*
	 * Author: Daniel Puckowski
	 */
	
	private static final long serialVersionUID = 4271388619852805707L;
	
	private static final int CONTROL_GRID_LAYOUT_ROWS     = 10;
	private static final int CONTROL_GRID_LAYOUT_COLUMNS  = 1;
	
	private FileDropList mFileDropList;
	private JButton mSelectFilesButton;
	private JSlider mFramesPerSecondSlider;
	private JSlider mNumberOfMonitorsSlider;
	private JButton mStartButton;
	private JButton mAdvancedPreferencesButton;
	private ScalableSimpleImagePanel mPreviewPanel;
	private JButton mRemoveSelectedButton;
	private JCheckBox mShowPreviewsCheckBox;
	
	private final GuiMain GUI_MAIN;
	
	private final LocalCameraControllerPreferencesWindow LOCAL_CAMERA_CONTROLLER_PREFERENCES_WINDOW;
	
	private ChildWindowList mChildWindowList;
	
	public LocalFileWizardWindow(GuiMain guiMain) {
		GUI_MAIN                                   = guiMain;
		LOCAL_CAMERA_CONTROLLER_PREFERENCES_WINDOW = new LocalCameraControllerPreferencesWindow("Local File Settings", this); 
		
		mChildWindowList = new ChildWindowList();
		mChildWindowList.addWindow(LOCAL_CAMERA_CONTROLLER_PREFERENCES_WINDOW);
		
		buildFrame();
		
		WindowRegistry.getInstance().registerFrame(this, "LocalFileWizard");
	}
	
	public void setFramesPerSecondSliderState(final boolean framesPerSecondSliderEnabled) {
		mFramesPerSecondSlider.setEnabled(framesPerSecondSliderEnabled);
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
		settingsTabPane.addTab("Local File Settings", contentScrollPane);
		
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
		
		mRemoveSelectedButton = new JButton("Remove Selected Video");
		mRemoveSelectedButton.addActionListener(this);
		contentPanel.add(mRemoveSelectedButton);
		
		mShowPreviewsCheckBox = new JCheckBox("Show Icon Previews", true);
		mShowPreviewsCheckBox.addItemListener(this);
		contentPanel.add(mShowPreviewsCheckBox);
		
		JLabel framesPerSecondLabel = new JLabel("Frames Per Second Target");
		mFramesPerSecondSlider = new JSlider();
		int sliderMin = 2;
		int sliderMax = 30;
		int sliderInit = LocalFileRuntimeParams.getGlobalFramesPerSecondTarget();

		mFramesPerSecondSlider = new JSlider(JSlider.HORIZONTAL, sliderMin,
				sliderMax, sliderInit);
		mFramesPerSecondSlider.addChangeListener(this);

		mFramesPerSecondSlider.setMajorTickSpacing(4);
		mFramesPerSecondSlider.setPaintTicks(true);
		mFramesPerSecondSlider.setPaintLabels(true);
		mFramesPerSecondSlider.setEnabled(LOCAL_CAMERA_CONTROLLER_PREFERENCES_WINDOW.getLocalCameraController().isExhaustiveSearchEnabled());
		
		contentPanel.add(framesPerSecondLabel);
		contentPanel.add(mFramesPerSecondSlider);
		
		JLabel numberOfMonitorsLabel = new JLabel("Number of Concurrent Monitors");
		mNumberOfMonitorsSlider = new JSlider();
		sliderMin = 1;
		sliderMax = 10;
		sliderInit = LocalFileRuntimeParams.getGlobalNumberOfConcurrentMonitors();

		mNumberOfMonitorsSlider = new JSlider(JSlider.HORIZONTAL, sliderMin,
				sliderMax, sliderInit);
		mNumberOfMonitorsSlider.addChangeListener(this);

		mNumberOfMonitorsSlider.setMajorTickSpacing(1);
		mNumberOfMonitorsSlider.setPaintTicks(true);
		mNumberOfMonitorsSlider.setPaintLabels(true);
		
		contentPanel.add(numberOfMonitorsLabel);
		contentPanel.add(mNumberOfMonitorsSlider);
		
		mAdvancedPreferencesButton = new JButton("Advanced Preferences");
		mAdvancedPreferencesButton.addActionListener(this);
		contentPanel.add(mAdvancedPreferencesButton);
		
		mStartButton = new JButton("Start Processing");
		mStartButton.addActionListener(this);
		contentPanel.add(mStartButton);
		
		JSplitPane leftSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, settingsTabPane, mFileDropList);
		JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSplitPane, previewContentPanel);
		
		this.add(mainSplitPane, BorderLayout.CENTER);
		
		this.addWindowListener(new CloseChildrenWindowAdapter(mChildWindowList));
		
		this.setTitle("Local File Wizard");
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

		ProcessLocalFileThread processLocalFileThread = new ProcessLocalFileThread(GUI_MAIN, localFilesToProcess);
		processLocalFileThread.start();
		LocalFileRuntimeParams.setGlobalProcessLocalFileThread(processLocalFileThread);
		
		//TODO add new thread to cancel force global thread on stop
		new Thread() {
			@Override
			public void run() {	
				ThreadUtils.addThreadToHandleList("LocFileWizWindow ForceTerminateLocal", this);
				
				while(processLocalFileThread.isRunning() == true && processLocalFileThread.isInterrupted() == false) {
					Thread.yield();
				}
				
				LocalFileRuntimeParams.setGlobalProcessLocalFileThread(null);
				LocalFileRuntimeParams.setCameraControllerPreferencesBundle(null);
				
				ThreadUtils.removeThreadFromHandleList(this);
			}
		}.start();
		
		WindowRegistry.getInstance().closeFrames(mChildWindowList.getWindows());
		this.dispose();
	}
	
	private void performRemoveSelectedVideoAction() {
		CursorUtils.setBusyCursor(this);
		
		mFileDropList.removeSelectedIndex();
		
		CursorUtils.setDefaultCursor(this);
	}
	
	private void performOpenPreferencesWindowAction() {
		CursorUtils.setBusyCursor(this);
		
		LOCAL_CAMERA_CONTROLLER_PREFERENCES_WINDOW.display();
		
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
						ThreadUtils.addThreadToHandleList("LocFileWizWindow SelectFiles", this);
							
						performSelectFilesAction();
							
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			else if(button == mStartButton) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("LocFileWizWindow PerformStart", this);
							
						performStartAction();
							
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			else if(button == mAdvancedPreferencesButton) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("LocFileWizWindow OpenPreferences", this);
							
						performOpenPreferencesWindowAction();
							
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			else if(button == mRemoveSelectedButton) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("LocFileWizWindow RemoveSelected", this);
							
						performRemoveSelectedVideoAction();
							
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
		}
	}
	
	@Override
	public void stateChanged(ChangeEvent changeEvent) {
		JSlider slider = (JSlider) changeEvent.getSource();

		if (slider == mFramesPerSecondSlider
				&& !slider.getValueIsAdjusting()) {
			CaseAuditor.log(LogLevel.INFO, "Frames per second target " + mFramesPerSecondSlider.getValue());
			LocalFileRuntimeParams.setGlobalFramesPerSecondTarget(mFramesPerSecondSlider.getValue());
		} else if (slider == mNumberOfMonitorsSlider
				&& !slider.getValueIsAdjusting()) {
			LocalFileRuntimeParams.setGlobalNumberOfConcurrentMonitors(mNumberOfMonitorsSlider.getValue());
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
						ThreadUtils.addThreadToHandleList("LocFileWizWindow Exit", this);
						
						WindowRegistry.getInstance().closeFrames(mChildWindowList.getWindows());
						LocalFileWizardWindow.this.dispose();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menu = new JMenu("Review");
		menuBar.add(menu);
		
		menuItem = new JMenuItem("Select Files");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("LocFileWizWindow Select", this);
						
						performSelectFilesAction();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Remove Selected Video");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("LocFileWizWindow RemoveSelected", this);
							
						performRemoveSelectedVideoAction();
							
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Advanced Preferences");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("LocFileWizWindow Pref", this);
						
						performOpenPreferencesWindowAction();
						
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
						ThreadUtils.addThreadToHandleList("LocFileWizWindow Start", this);
						
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
						ThreadUtils.addThreadToHandleList("LocFileWizWindow OpenDoc", this);
						
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
						ThreadUtils.addThreadToHandleList("LocFileWizWindow About", this);
						
						Utils.displayMessageDialog("About", Utils.SOFTWARE_NAME + " \nVersion: " + Utils.SOFTWARE_VERSION + "\n� " + Utils.AUTHOR_NAME);
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		this.setJMenuBar(menuBar);
	}
}

package com.keypointforensics.videotriage.detect;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.keypointforensics.videotriage.detect.car.DatabaseCarCrawler;
import com.keypointforensics.videotriage.detect.car.ExtractCarCrawler;
import com.keypointforensics.videotriage.detect.explicit.ExtractExplicitCrawler;
import com.keypointforensics.videotriage.detect.face.DatabaseFaceCrawler;
import com.keypointforensics.videotriage.detect.face.ExtractFaceCrawler;
import com.keypointforensics.videotriage.detect.license.DatabaseLicensePlateCrawler;
import com.keypointforensics.videotriage.detect.license.ExtractLicensePlateCrawler;
import com.keypointforensics.videotriage.detect.pedestrian.DatabasePedestrianCrawler;
import com.keypointforensics.videotriage.detect.pedestrian.ExtractPedestrianCrawler;
import com.keypointforensics.videotriage.gui.gallery.UpdatedImageGallery;
import com.keypointforensics.videotriage.gui.main.GuiMain;
import com.keypointforensics.videotriage.legacy.FileUtilsLegacy;
import com.keypointforensics.videotriage.legacy.UtilsLegacy;
import com.keypointforensics.videotriage.util.BorderUtils;
import com.keypointforensics.videotriage.util.CursorUtils;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.Utils;
import com.keypointforensics.videotriage.util.WebUtils;
import com.keypointforensics.videotriage.util.WindowUtils;
import com.keypointforensics.videotriage.window.WindowRegistry;

public class ExtractDetectionModuleWizardWindow extends JFrame implements ActionListener, ItemListener, ChangeListener {

	/*
	 * Author: Daniel Puckowski
	 */
	
	private static final long serialVersionUID = 4271388619852805707L;
	
	private static final int CONTROL_GRID_LAYOUT_ROWS     = 13;
	private static final int CONTROL_GRID_LAYOUT_COLUMNS  = 1;
	
	private JList<EVideoTriageDetectionModule> mDetectionModuleList;
	private DefaultListModel<EVideoTriageDetectionModule> mDetectionModuleListModel;
	 
	private JButton mAddDetectionModuleButton;
	private JButton mStartButton;
	private JButton mRemoveSelectedModuleButton;
	
	private JSlider mCpuCoreUsageSlider;
	private JSlider mSurfSimilaritySlider;
	private JSlider mMinimumSizeSlider;
	
	private JCheckBox mExhaustiveSearchCheckBox;
	private JCheckBox mPreAnalyzeResultsCheckBox;
	
	//
	private final EVideoTriageDetectionModule[] VIDEO_TRIAGE_DETECTION_MODULE_LIST =  { 
		EVideoTriageDetectionModule.DETECTION_MODULE_FACE,
		EVideoTriageDetectionModule.DETECTION_MODULE_LICENSE_PLATE,
		EVideoTriageDetectionModule.DETECTION_MODULE_PEDESTRIAN,
		EVideoTriageDetectionModule.DETECTION_MODULE_CAR,
		EVideoTriageDetectionModule.DETECTION_MODULE_EXPLICIT
	};
	
	private JComboBox<EVideoTriageDetectionModule> mDetectionModuleComboBox = new JComboBox<EVideoTriageDetectionModule>(VIDEO_TRIAGE_DETECTION_MODULE_LIST);
	//
	
	private final GuiMain GUI_MAIN;
	private final String DATABASE_NAME;
	private final String EXTRACT_DIRECTORY;
	
	public ExtractDetectionModuleWizardWindow(final GuiMain guiMain, final String databaseName, final String extractDirectory) {		
		GUI_MAIN          = guiMain;
		DATABASE_NAME     = databaseName;
		EXTRACT_DIRECTORY = extractDirectory;
		
		if(EXTRACT_DIRECTORY == null) {
			return;
		}
		else if(EXTRACT_DIRECTORY.startsWith(FileUtils.EXTRACTS_DIRECTORY) == false) {
			Utils.displayMessageDialog("Directory Error", "Please select a valid video extraction directory for detection.");
		
			return;
		}
		
		buildFrame();
		
		WindowRegistry.getInstance().registerFrame(this, "DetectionModuleWizard");
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
		settingsTabPane.addTab("Detection Settings", contentScrollPane);
		
		JLabel selectVideoFilterLabel = new JLabel("Select Detection Module");
		contentPanel.add(selectVideoFilterLabel);
		
		mDetectionModuleComboBox.setSelectedIndex(0);
		contentPanel.add(mDetectionModuleComboBox);
		
		mAddDetectionModuleButton = new JButton("Add Detection Module");
		mAddDetectionModuleButton.addActionListener(this);
		contentPanel.add(mAddDetectionModuleButton);
		
		mRemoveSelectedModuleButton = new JButton("Remove Selected Module");
		mRemoveSelectedModuleButton.addActionListener(this);
		contentPanel.add(mRemoveSelectedModuleButton);
		
		JLabel cpuCoreUsageLabel = new JLabel("CPU Core Usage");
		contentPanel.add(cpuCoreUsageLabel);
		
		int sliderMin = 1;
		int sliderMax = Runtime.getRuntime().availableProcessors();
		int sliderInit = (Runtime.getRuntime().availableProcessors() >= 4) ? 4 : 1;
		mCpuCoreUsageSlider = new JSlider(JSlider.HORIZONTAL, sliderMin,
				sliderMax, sliderInit);
		mCpuCoreUsageSlider.addChangeListener(this);
		try {
			mCpuCoreUsageSlider.removeMouseWheelListener(mCpuCoreUsageSlider.getMouseWheelListeners()[0]);
		} catch(Exception exception) {	

		}
		mCpuCoreUsageSlider.setMajorTickSpacing(2);
		mCpuCoreUsageSlider.setPaintTicks(true);
		mCpuCoreUsageSlider.setPaintLabels(true);
		contentPanel.add(mCpuCoreUsageSlider);
		
		mPreAnalyzeResultsCheckBox = new JCheckBox("Pre-analyze Results");
		mPreAnalyzeResultsCheckBox.setSelected(true);
		mPreAnalyzeResultsCheckBox.addChangeListener(this);
		contentPanel.add(mPreAnalyzeResultsCheckBox);
		
		JLabel resultAnalysisLevelLabel = new JLabel("Result Filter Level");
		contentPanel.add(resultAnalysisLevelLabel);
		
		sliderMin = 1;
		sliderMax = 100;
		sliderInit = 40;
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
		
		JLabel mimimumFaceSizeLabel = new JLabel("Minimum Area Percent");
		contentPanel.add(mimimumFaceSizeLabel);
		
		sliderMin = 1;
		sliderMax = 25;
		sliderInit = 2;
		mMinimumSizeSlider = new JSlider(JSlider.HORIZONTAL, sliderMin,
				sliderMax, sliderInit);
		mMinimumSizeSlider.addChangeListener(this);
		try {
			mMinimumSizeSlider.removeMouseWheelListener(mMinimumSizeSlider.getMouseWheelListeners()[0]);
		} catch(Exception exception) {	

		}
		mMinimumSizeSlider.setMajorTickSpacing(4);
		mMinimumSizeSlider.setPaintTicks(true);
		mMinimumSizeSlider.setPaintLabels(true);
		contentPanel.add(mMinimumSizeSlider);
		
		mExhaustiveSearchCheckBox = new JCheckBox("Exhaustive Search");
		mExhaustiveSearchCheckBox.setSelected(false);
		contentPanel.add(mExhaustiveSearchCheckBox);
		
		mDetectionModuleListModel = new DefaultListModel<EVideoTriageDetectionModule>();
		mDetectionModuleList = new JList<EVideoTriageDetectionModule>(mDetectionModuleListModel);
		mDetectionModuleList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		mDetectionModuleList.setBorder(BorderUtils.getEmptyBorder());
		JScrollPane detectionModuleScrollPane = new JScrollPane(mDetectionModuleList);
		   
		JPanel detectionModuleListPanel = new JPanel();
		detectionModuleListPanel.setBorder(BorderUtils.getEmptyBorder());
		detectionModuleListPanel.setLayout(new BorderLayout());
		detectionModuleListPanel.add(detectionModuleScrollPane, BorderLayout.CENTER);
		
		JLabel listOfModulesToRunLabel = new JLabel("Detection Module List");
		detectionModuleListPanel.add(listOfModulesToRunLabel, BorderLayout.NORTH);
		 
		mStartButton = new JButton("Start Processing");
		mStartButton.addActionListener(this);
		contentPanel.add(mStartButton);
		
		JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, settingsTabPane, detectionModuleListPanel);//mFileDropList);
		
		this.add(mainSplitPane, BorderLayout.CENTER);
		
		this.setTitle("Detection Module Settings Wizard");
		this.pack();
		WindowUtils.setFrameIcon(this);
		WindowUtils.maximize(this);
		this.setVisible(true);
		//WindowUtils.center(this);
	}
	
	private void performSelectFilesAction() {		
		EVideoTriageDetectionModule selectedDetectionModule = mDetectionModuleComboBox.getItemAt(mDetectionModuleComboBox.getSelectedIndex());
		
		mDetectionModuleListModel.addElement(selectedDetectionModule);
		mDetectionModuleList.revalidate();
	}
	
	private void performStartAction() {
		CursorUtils.setBusyCursor(GUI_MAIN);
		
		int numberOfModulesToRun = mDetectionModuleListModel.getSize();
		
		ArrayList<String> listOfDatabasePaths = new ArrayList<String>();
		
		for(int i = 0; i < numberOfModulesToRun; ++i) {
			EVideoTriageDetectionModule detectionModuleEnum = mDetectionModuleListModel.getElementAt(i);
			ExtractDetectionModule detectionModule = null;
					
			switch(detectionModuleEnum) {
				case DETECTION_MODULE_FACE: {
					detectionModule = new ExtractFaceCrawler(GUI_MAIN, DATABASE_NAME, EXTRACT_DIRECTORY);
							
					break;
				}
				case DETECTION_MODULE_LICENSE_PLATE: {
					detectionModule = new ExtractLicensePlateCrawler(GUI_MAIN, DATABASE_NAME, EXTRACT_DIRECTORY);
							
					break;
				}
				case DETECTION_MODULE_PEDESTRIAN: {
					detectionModule = new ExtractPedestrianCrawler(GUI_MAIN, DATABASE_NAME, EXTRACT_DIRECTORY);
					
					break;
				}
				case DETECTION_MODULE_CAR: {
					detectionModule = new ExtractCarCrawler(GUI_MAIN, DATABASE_NAME, EXTRACT_DIRECTORY);
					
					break;
				}
				case DETECTION_MODULE_EXPLICIT: {
					detectionModule = new ExtractExplicitCrawler(GUI_MAIN, DATABASE_NAME, EXTRACT_DIRECTORY);
					
					break;
				}
			}
			
			if(detectionModule != null) {
				detectionModule.setNumberOfCpuCores(mCpuCoreUsageSlider.getValue());
				detectionModule.setExhaustiveSearch(mExhaustiveSearchCheckBox.isSelected());
				detectionModule.setSimilarityPercent(mSurfSimilaritySlider.getValue());
				detectionModule.setMinimumDetectionSize(mMinimumSizeSlider.getValue());
				detectionModule.setPreAnalyzeResults(mPreAnalyzeResultsCheckBox.isSelected());
				
				detectionModule.performDatabaseCrawlAction();
				
				listOfDatabasePaths.add(detectionModule.getDetectionDatabaseFolder());
			}
		}
		
		final int showAllDetectionsChoice = UtilsLegacy.displayConfirmDialog("Detection Complete", "Show all detection windows for manual review?");
			
		if(showAllDetectionsChoice == JOptionPane.OK_OPTION) {			
			//ArrayList<String> allDetectionPaths = null;
			
			for(String databasePath : listOfDatabasePaths) {
				//allDetectionPaths = (ArrayList<String>) FileUtilsLegacy.parseDirectoryRecursiveForAll(databasePath);
			
				//if(allDetectionPaths.isEmpty() == false) {
					//SearchImageGallery searchImageGallery = new SearchImageGallery(allDetectionPaths);
					//searchImageGallery.build();
					
					UpdatedImageGallery updatedImageGallery = new UpdatedImageGallery(databasePath);
					updatedImageGallery.build();
				//}
			}
		} else {
			//
		}
		
		CursorUtils.setDefaultCursor(GUI_MAIN);
	}
	
	private void performRemoveSelectedVideoAction() {
		CursorUtils.setBusyCursor(this);
		
		final int selectedIndex = mDetectionModuleList.getSelectedIndex();
		
		if(selectedIndex >= 0 && selectedIndex < mDetectionModuleListModel.size()) {
			mDetectionModuleListModel.remove(mDetectionModuleList.getSelectedIndex());
			mDetectionModuleList.revalidate();
		}
		
		CursorUtils.setDefaultCursor(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() instanceof JButton) {
			JButton button = (JButton) event.getSource();

			if(button == mAddDetectionModuleButton) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ExtractDetectionModWizWindow AddDetectionModule", this);
							
						performSelectFilesAction();
							
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			else if(button == mStartButton) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ExtractDetectionModWizWindow PerformStart", this);
						
						if(mDetectionModuleListModel.size() == 0) {
							ThreadUtils.removeThreadFromHandleList(this);
							
							return;
						}
						
						ExtractDetectionModuleWizardWindow.this.dispose();
						
						performStartAction();
							
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			else if(button == mRemoveSelectedModuleButton) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ExtractDetectionModWizWindow RemoveSelectedModule", this);
							
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
						ThreadUtils.addThreadToHandleList("ExtractDetectionModWizWindow Exit", this);
						
						ExtractDetectionModuleWizardWindow.this.dispose();
						
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
						ThreadUtils.addThreadToHandleList("ExtractDetectionModWizWindow OpenDoc", this);
						
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
						ThreadUtils.addThreadToHandleList("ExtractDetectionModWizWindow Exit", this);
						
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
		Object sourceObject = changeEvent.getSource();
		
		if(sourceObject instanceof JCheckBox) {
			JCheckBox sourceCheckBox = (JCheckBox) sourceObject;
			
			if(sourceCheckBox == mPreAnalyzeResultsCheckBox) {
				if(sourceCheckBox.isSelected() == true) {
					mSurfSimilaritySlider.setEnabled(true);
				} else {
					mSurfSimilaritySlider.setEnabled(false);
				}
			}
		}
	}
	
	
	@Override
	public void itemStateChanged(ItemEvent event) {
		
	}
}

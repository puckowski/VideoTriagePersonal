package com.keypointforensics.videotriage.gui.controller.local;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.keypointforensics.videotriage.blob.context.BlobContextList;
import com.keypointforensics.videotriage.casestore.CaseMetadataWriter;
import com.keypointforensics.videotriage.gui.imagepanel.VideoFeedImagePanel;
import com.keypointforensics.videotriage.gui.localfile.wizard.LocalFileWizardWindow;
import com.keypointforensics.videotriage.gui.main.CameraPreviewPanel;
import com.keypointforensics.videotriage.gui.main.GuiMain;
import com.keypointforensics.videotriage.gui.main.RotateSteppingSlider;
import com.keypointforensics.videotriage.gui.main.controller.CameraController;
import com.keypointforensics.videotriage.gui.main.controller.CameraControllerPreferencesBundle;
import com.keypointforensics.videotriage.legacy.ExtractVideoFrameBlob;
import com.keypointforensics.videotriage.legacy.OsUtilsLegacy;
import com.keypointforensics.videotriage.legacy.OsUtilsLegacy.FrsOsType;
import com.keypointforensics.videotriage.legacy.WindowsVideoFrameExtractorLegacy;
import com.keypointforensics.videotriage.local.LocalMeanFilterProcessor;
import com.keypointforensics.videotriage.local.LocalViewProcessor;
import com.keypointforensics.videotriage.params.BackgroundRuntimeParams;
import com.keypointforensics.videotriage.params.BlobRuntimeParams;
import com.keypointforensics.videotriage.params.MassRuntimeParams;
import com.keypointforensics.videotriage.params.MetadataRuntimeParams;
import com.keypointforensics.videotriage.params.SourceRuntimeParams;
import com.keypointforensics.videotriage.params.StatusBarRuntimeParams;
import com.keypointforensics.videotriage.params.WriteRuntimeParams;
import com.keypointforensics.videotriage.processor.ViewProcessorFactory;
import com.keypointforensics.videotriage.staticparams.LocalFileRuntimeParams;
import com.keypointforensics.videotriage.statusbar.StatusBar;
import com.keypointforensics.videotriage.statusbar.StatusBarFactory;
import com.keypointforensics.videotriage.thread.StopLocalViewThread;
import com.keypointforensics.videotriage.util.BorderUtils;
import com.keypointforensics.videotriage.util.WindowUtils;

public class LocalCameraController extends CameraController implements ActionListener, ChangeListener, ItemListener {

	/*
	 * Author: Daniel Puckowski
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -9135371387119060753L;
	
	private final Color[] BLOB_BORDER_COLOR_OPTIONS = {
			Color.RED,
			Color.YELLOW,
			Color.GREEN,
			Color.WHITE,
			Color.GRAY
	};
	
	private final int[] VIDEO_CREATION_DATE_OPTIONS = {
			MetadataRuntimeParams.DATE_CREATION_MODE_REAL_OR_INSTANT,
			MetadataRuntimeParams.DATE_CREATION_MODE_REAL_ONLY,
			MetadataRuntimeParams.DATE_CREATION_MODE_INSTANT_ONLY
	};
	
	private final String[] BLOB_BORDER_COLOR_OPTION_STRINGS = {
			"Red",
			"Yellow",
			"Green",
			"White",
			"Gray"
	};
	
	private final String[] VIDEO_CREATION_DATE_OPTION_STRINGS = {
			"Prefer Date Created",
			"Date Created",
			"Time Processed",
	};
	
	private final int DEFAULT_SLIDER_VALUE_MULTIPLIER = 90;
	
	private final String          CONTROLLER_ID;
	private final GuiMain         GUI_MAIN;
	private final String          DATABASE_NAME;
	private final BlobContextList BLOB_CONTEXT_LIST;
	
	private MassRuntimeParams       mMassParams;
	private BlobRuntimeParams       mBlobParams;
	private WriteRuntimeParams      mWriteParams;
	private StatusBarRuntimeParams  mStatusBarParams;
	private BackgroundRuntimeParams mBackgroundParams;
	private SourceRuntimeParams     mSourceParams;
	private MetadataRuntimeParams   mMetadataParams;
	
	private int                     mRotateDegrees;
	private VideoFeedImagePanel     mGraphicsPanel;
	private CameraPreviewPanel      mPreviewPanel;
	
	private JPanel                  mGeneralPanel;
	
	private JTextField              mLocalFileField;
		
	private JButton                 mRemoteStartButton;
	private JButton                 mRemoteResetBackgroundButton;
	private JButton                 mRemoteStopButton;
	
	private RotateSteppingSlider    mRemoteRotateSlider;
	
	private JSlider                 mMassThresholdSlider;
	private JSlider                 mBackgroundThresholdSlider;
	private JSlider                 mMassConsiderationThresholdSlider;
	private JSlider                 mBlobExpansionPercentSlider;
	private JSlider                 mCaptureEntropyFilterSlider;
	
	private JCheckBox               mBlobBorderCheckBox;
	private JCheckBox               mStatusBarEnabledCheckBox;
	private JCheckBox               mAttemptToMergeCheckBox;
	private JCheckBox               mHighlightBlobsCheckBox;
	private JCheckBox               mWriteCheckBox;
	private JCheckBox               mAutoUpdateCheckBox;
	private JCheckBox               mDrawOnSourceCheckBox;
	private JCheckBox               mExpandBlobsCheckBox;
	private JCheckBox               mEntropyFilterCheckBox;
	private JCheckBox               mExhaustiveSearchCheckBox;
	private JCheckBox               mRecordCoordinatesCheckBox;
	
	private JScrollPane             mLeftScrollPane;
	
	private JComboBox<String>       mBlobBorderColorCombo;
	private JComboBox<String>       mVideoCreationDateCombo;
	
	private JLabel                  mLocalFileFieldLabel; 

	private LocalViewProcessor      mLocalViewThread;
			
	public LocalCameraController(final String cameraId, final GuiMain guiMain, final String databaseName, final BlobContextList blobContextList) {
		CONTROLLER_ID     = cameraId;
		GUI_MAIN          = guiMain;
		DATABASE_NAME     = databaseName;
		BLOB_CONTEXT_LIST = blobContextList;
		
		init();
		build();
	}
	
	@Override
	public String getDatabaseName() {
		return DATABASE_NAME;
	}
	
	public String getControllerId() {
		return CONTROLLER_ID;
	}
	
	public JTextField getLocalFileField() {
		return mLocalFileField;
	}
	
	public boolean isExhaustiveSearchEnabled() {
		return mExhaustiveSearchCheckBox.isSelected();
	}
	
	private void buildTextFields() {				
		mLocalFileField = new JTextField();
		mLocalFileField.setMaximumSize(new Dimension(Integer.MAX_VALUE, mLocalFileField.getPreferredSize().height));
		mLocalFileField.setColumns(20); 
	}
	
	private void buildButtons() {		
		mRemoteStartButton = new JButton("Start");
		mRemoteStartButton.addActionListener(this);
		mRemoteResetBackgroundButton = new JButton("Reset Background");
		mRemoteResetBackgroundButton.addActionListener(this);
		mRemoteStopButton = new JButton("Stop");
		mRemoteStopButton.addActionListener(this);
	}
	
	private void buildSliders() {		
		mRemoteRotateSlider = new RotateSteppingSlider();
		mRemoteRotateSlider.addChangeListener(this);
		try {
			mRemoteRotateSlider.removeMouseWheelListener(mRemoteRotateSlider.getMouseWheelListeners()[0]);
		} catch(Exception exception) {	

		}
		
		int sliderMin = 0;
		int sliderMax = 100;
		int sliderInit = mMassParams.getThresholdInt();
		mMassThresholdSlider = new JSlider(JSlider.HORIZONTAL, sliderMin,
				sliderMax, sliderInit);
		mMassThresholdSlider.addChangeListener(this);
		try {
			mMassThresholdSlider.removeMouseWheelListener(mMassThresholdSlider.getMouseWheelListeners()[0]);
		} catch(Exception exception) {	

		}
		mMassThresholdSlider.setMajorTickSpacing(20);
		mMassThresholdSlider.setPaintTicks(true);
		mMassThresholdSlider.setPaintLabels(true);
		
		sliderMin = 0;
		sliderMax = 255;
		sliderInit = mBackgroundParams.getThresholdInt();
		mBackgroundThresholdSlider = new JSlider(JSlider.HORIZONTAL, sliderMin,
				sliderMax, sliderInit);
		mBackgroundThresholdSlider.addChangeListener(this);
		try {
			mBackgroundThresholdSlider.removeMouseWheelListener(mBackgroundThresholdSlider.getMouseWheelListeners()[0]);
		} catch(Exception eception) {

		}
		mBackgroundThresholdSlider.setMajorTickSpacing(51);
		mBackgroundThresholdSlider.setPaintTicks(true);
		mBackgroundThresholdSlider.setPaintLabels(true);
		
		sliderMin = 0;
		sliderMax = 100;
		sliderInit = mMassParams.getConsiderationThresholdInt();
		mMassConsiderationThresholdSlider = new JSlider(JSlider.HORIZONTAL, sliderMin,
				sliderMax, sliderInit);
		mMassConsiderationThresholdSlider.addChangeListener(this);
		try {
			mMassConsiderationThresholdSlider.removeMouseWheelListener(mMassConsiderationThresholdSlider.getMouseWheelListeners()[0]);
		} catch(Exception exception) {	

		}
		mMassConsiderationThresholdSlider.setMajorTickSpacing(20);
		mMassConsiderationThresholdSlider.setPaintTicks(true);
		mMassConsiderationThresholdSlider.setPaintLabels(true);
		
		sliderMin = 0;
		sliderMax = 100;
		sliderInit = (int) (mBlobParams.getExpansionPercent() * 100.0);
		mBlobExpansionPercentSlider = new JSlider(JSlider.HORIZONTAL, sliderMin,
				sliderMax, sliderInit);
		mBlobExpansionPercentSlider.addChangeListener(this);
		try {
			mBlobExpansionPercentSlider.removeMouseWheelListener(mBlobExpansionPercentSlider.getMouseWheelListeners()[0]);
		} catch(Exception exception) {	

		}
		mBlobExpansionPercentSlider.setMajorTickSpacing(20);
		mBlobExpansionPercentSlider.setPaintTicks(true);
		mBlobExpansionPercentSlider.setPaintLabels(true);
		
		sliderMin = 0;
		sliderMax = 100;
		sliderInit = (int) (mWriteParams.getEntropyThresholdForSlider());
		mCaptureEntropyFilterSlider = new JSlider(JSlider.HORIZONTAL, sliderMin,
				sliderMax, sliderInit);
		mCaptureEntropyFilterSlider.addChangeListener(this);
		try {
			mCaptureEntropyFilterSlider.removeMouseWheelListener(mCaptureEntropyFilterSlider.getMouseWheelListeners()[0]);
		} catch(Exception exception) {	

		}
		mCaptureEntropyFilterSlider.setMajorTickSpacing(20);
		mCaptureEntropyFilterSlider.setPaintTicks(true);
		mCaptureEntropyFilterSlider.setPaintLabels(true);
	}
	
	private void buildCheckBoxes() {		
		mBlobBorderCheckBox = new JCheckBox("Display Object Borders");
		mBlobBorderCheckBox.setSelected(mBlobParams.getBorderDisplay());
		mBlobBorderCheckBox.addItemListener(this);
		mStatusBarEnabledCheckBox = new JCheckBox("Status Bar Enabled");
		mStatusBarEnabledCheckBox.setSelected(mStatusBarParams.getEnabled());
		mStatusBarEnabledCheckBox.addItemListener(this);
		mAttemptToMergeCheckBox = new JCheckBox("Merge Objects");
		mAttemptToMergeCheckBox.setSelected(mBlobParams.getAttemptToMerge());
		mAttemptToMergeCheckBox.addItemListener(this);
		mHighlightBlobsCheckBox = new JCheckBox("Highlight Objects");
		mHighlightBlobsCheckBox.setSelected(mBlobParams.getHighlightBlobs());
		mHighlightBlobsCheckBox.addItemListener(this);
		mWriteCheckBox = new JCheckBox("Enable Database");
		mWriteCheckBox.setSelected(mWriteParams.getWriteState());
		mWriteCheckBox.addItemListener(this);
		mAutoUpdateCheckBox = new JCheckBox("Enable Smart Background");
		mAutoUpdateCheckBox.setSelected(mBackgroundParams.getAutoUpdate());
		mAutoUpdateCheckBox.addItemListener(this);
		mDrawOnSourceCheckBox = new JCheckBox("Draw On Source");
		mDrawOnSourceCheckBox.setSelected(mSourceParams.getDrawOnSourceEnabled());
		mDrawOnSourceCheckBox.addItemListener(this);
		mExpandBlobsCheckBox = new JCheckBox("Expand Objects");
		mExpandBlobsCheckBox.setSelected(mBlobParams.getExpandBlobs());
		mExpandBlobsCheckBox.addItemListener(this);
		mEntropyFilterCheckBox = new JCheckBox("Enable Entropy Filtering");
		mEntropyFilterCheckBox.setSelected(mWriteParams.getEntropyFilterState());
		mEntropyFilterCheckBox.addItemListener(this);
		mExhaustiveSearchCheckBox = new JCheckBox("Exhaustive Search Enabled");
		mExhaustiveSearchCheckBox.setSelected(mSourceParams.getExhaustiveSearchEnabled());
		mExhaustiveSearchCheckBox.addItemListener(this);
		
		mRecordCoordinatesCheckBox = new JCheckBox("Record Coordinates");
		mRecordCoordinatesCheckBox.setSelected(mBlobParams.getSaveBlobCoordinates());
		mRecordCoordinatesCheckBox.addItemListener(this);
		mRecordCoordinatesCheckBox.setEnabled(false);
	}
	
	private void buildComboBoxes() {		
		mBlobBorderColorCombo = new JComboBox<String>(BLOB_BORDER_COLOR_OPTION_STRINGS);
		mBlobBorderColorCombo.setSelectedItem(0);
		mBlobBorderColorCombo.addActionListener(this);
		
		mVideoCreationDateCombo = new JComboBox<String>(VIDEO_CREATION_DATE_OPTION_STRINGS);
		mVideoCreationDateCombo.setSelectedItem(0);
		mVideoCreationDateCombo.addActionListener(this);
	}
	
	private JScrollPane createLeftScrollPane() {
		JTabbedPane leftPanel = new JTabbedPane(); 
		
		buildTextFields();
		mLocalFileFieldLabel = new JLabel("Local File");
		
		buildButtons();
		
		buildSliders();
		JLabel rotateSliderLabel               = new JLabel("Rotate Source");	
		JLabel massThresholdLabel              = new JLabel("Set Mass Threshold");
		JLabel backgroundThresholdLabel        = new JLabel("Set Background Threshold");
		JLabel massConsiderationThresholdLabel = new JLabel("Mass Consideration Threshold");
		JLabel blobExpansionPercentLabel       = new JLabel("Object Expansion Percent");
		//JLabel captureEntropyFilterLabel       = new JLabel("Set Entropy Threshold");
		
		buildCheckBoxes();
		
		buildComboBoxes();
		JLabel blobBorderColorLabel = new JLabel("Object Border Color");
		
		mGeneralPanel = new JPanel();
		mGeneralPanel.setLayout(new GridLayout(13, 1));
		mGeneralPanel.add(mLocalFileFieldLabel);
		mGeneralPanel.add(mLocalFileField);
		mGeneralPanel.add(mWriteCheckBox);
		mGeneralPanel.add(mAutoUpdateCheckBox);
		mGeneralPanel.add(mStatusBarEnabledCheckBox);
		mGeneralPanel.add(mEntropyFilterCheckBox);
		mGeneralPanel.add(mRemoteStartButton);
		mGeneralPanel.add(mRemoteResetBackgroundButton);
		mGeneralPanel.add(mRemoteStopButton);
		mGeneralPanel.setBorder(BorderUtils.getEmptyBorder());
		
		JPanel sourcePanel = new JPanel();
		sourcePanel.setLayout(new GridLayout(13, 1));
		//TODO
		//sourcePanel.add(mExhaustiveSearchCheckBox);
		sourcePanel.add(rotateSliderLabel);
		sourcePanel.add(mRemoteRotateSlider);
		sourcePanel.add(mDrawOnSourceCheckBox);
		sourcePanel.setBorder(BorderUtils.getEmptyBorder());
		
		JPanel blobPanel = new JPanel();
		blobPanel.setLayout(new GridLayout(13, 1));
		blobPanel.add(mRecordCoordinatesCheckBox);
		blobPanel.add(mBlobBorderCheckBox);
		blobPanel.add(mHighlightBlobsCheckBox);
		blobPanel.add(mAttemptToMergeCheckBox);
		blobPanel.add(mExpandBlobsCheckBox);
		blobPanel.add(blobExpansionPercentLabel);
		blobPanel.add(mBlobExpansionPercentSlider);
		blobPanel.add(blobBorderColorLabel);
		blobPanel.add(mBlobBorderColorCombo);
		blobPanel.setBorder(BorderUtils.getEmptyBorder());
		
		JPanel advancedPanel = new JPanel();
		advancedPanel.setLayout(new GridLayout(13, 1));
		advancedPanel.add(massThresholdLabel);
		advancedPanel.add(mMassThresholdSlider);
		advancedPanel.add(massConsiderationThresholdLabel);
		advancedPanel.add(mMassConsiderationThresholdSlider);
		advancedPanel.add(backgroundThresholdLabel);
		advancedPanel.add(mBackgroundThresholdSlider);
		//advancedPanel.add(captureEntropyFilterLabel);
		//advancedPanel.add(mCaptureEntropyFilterSlider);
		advancedPanel.setBorder(BorderUtils.getEmptyBorder());
		
		leftPanel.addTab("General", null, mGeneralPanel, "Standard tracking configuration");
		leftPanel.addTab("Source", null, sourcePanel, "Video source configuration");
		leftPanel.addTab("Detection", null, blobPanel, "Video detection configuration");
		leftPanel.addTab("Advanced", null, advancedPanel, "Advanced tracking configuration");
		
		JScrollPane leftScrollPane = new JScrollPane(leftPanel);
		WindowUtils.setScrollBarIncrement(leftScrollPane);
		leftScrollPane.setPreferredSize(new Dimension(260, 650));
		leftScrollPane.setMinimumSize(new Dimension(260, 650));
		
		return leftScrollPane;
	}
	
	public JScrollPane createLeftScrollPane(final int gridLayoutRows) {
		JTabbedPane leftPanel = new JTabbedPane(); 
		
		buildTextFields();
		mLocalFileFieldLabel = new JLabel("Local File");
		
		buildButtons();
		
		buildSliders();
		JLabel rotateSliderLabel               = new JLabel("Rotate Source");	
		JLabel massThresholdLabel              = new JLabel("Set Mass Threshold");
		JLabel backgroundThresholdLabel        = new JLabel("Set Background Threshold");
		JLabel massConsiderationThresholdLabel = new JLabel("Mass Consideration Threshold");
		JLabel blobExpansionPercentLabel       = new JLabel("Object Expansion Percent");
		JLabel captureEntropyFilterLabel       = new JLabel("Set Entropy Threshold");
		
		buildCheckBoxes();
		
		buildComboBoxes();
		JLabel blobBorderColorLabel   = new JLabel("Object Border Color");
		JLabel videoCreationDateLabel = new JLabel("Report Date");
		
		mGeneralPanel = new JPanel();
		mGeneralPanel.setLayout(new GridLayout(gridLayoutRows, 1));
		mGeneralPanel.add(mLocalFileFieldLabel);
		mGeneralPanel.add(mLocalFileField);
		mGeneralPanel.add(mWriteCheckBox);
		mGeneralPanel.add(mAutoUpdateCheckBox);
		mGeneralPanel.add(mStatusBarEnabledCheckBox);
		mGeneralPanel.add(mEntropyFilterCheckBox);
		mGeneralPanel.add(mRemoteStartButton);
		mGeneralPanel.add(mRemoteResetBackgroundButton);
		mGeneralPanel.add(mRemoteStopButton);
		mGeneralPanel.setBorder(BorderUtils.getEmptyBorder());
		
		JPanel sourcePanel = new JPanel();
		sourcePanel.setLayout(new GridLayout(gridLayoutRows, 1));
		//TODO
		sourcePanel.add(mExhaustiveSearchCheckBox);
		sourcePanel.add(rotateSliderLabel);
		sourcePanel.add(mRemoteRotateSlider);
		sourcePanel.add(mDrawOnSourceCheckBox);
		sourcePanel.add(videoCreationDateLabel);
		sourcePanel.add(mVideoCreationDateCombo);
		sourcePanel.setBorder(BorderUtils.getEmptyBorder());
		
		JPanel blobPanel = new JPanel();
		blobPanel.setLayout(new GridLayout(gridLayoutRows, 1));
		blobPanel.add(mRecordCoordinatesCheckBox);
		blobPanel.add(mBlobBorderCheckBox);
		blobPanel.add(mHighlightBlobsCheckBox);
		blobPanel.add(mAttemptToMergeCheckBox);
		blobPanel.add(mExpandBlobsCheckBox);
		blobPanel.add(blobExpansionPercentLabel);
		blobPanel.add(mBlobExpansionPercentSlider);
		blobPanel.add(blobBorderColorLabel);
		blobPanel.add(mBlobBorderColorCombo);
		blobPanel.setBorder(BorderUtils.getEmptyBorder());
		
		JPanel advancedPanel = new JPanel();
		advancedPanel.setLayout(new GridLayout(gridLayoutRows, 1));
		advancedPanel.add(massThresholdLabel);
		advancedPanel.add(mMassThresholdSlider);
		advancedPanel.add(massConsiderationThresholdLabel);
		advancedPanel.add(mMassConsiderationThresholdSlider);
		advancedPanel.add(backgroundThresholdLabel);
		advancedPanel.add(mBackgroundThresholdSlider);
		advancedPanel.add(captureEntropyFilterLabel);
		advancedPanel.add(mCaptureEntropyFilterSlider);
		advancedPanel.setBorder(BorderUtils.getEmptyBorder());
		
		leftPanel.addTab("General", null, mGeneralPanel, "Standard tracking configuration");
		leftPanel.addTab("Source", null, sourcePanel, "Video source configuration");
		leftPanel.addTab("Detection", null, blobPanel, "Video detection configuration");
		leftPanel.addTab("Advanced", null, advancedPanel, "Advanced tracking configuration");
		
		JScrollPane leftScrollPane = new JScrollPane(leftPanel);
		WindowUtils.setScrollBarIncrement(leftScrollPane);
		leftScrollPane.setPreferredSize(new Dimension(260, 650));
		leftScrollPane.setMinimumSize(new Dimension(260, 650));
		
		return leftScrollPane;
	}
	
	private void formatLeftScrollPane(final boolean componentsVisible) {
		mLocalFileFieldLabel.setVisible(componentsVisible);
		mLocalFileField.setVisible(componentsVisible);
	}
	
	public void formatLeftScrollPaneForPreferences() {
		formatLeftScrollPane(false);
		
		mGeneralPanel.remove(mLocalFileFieldLabel);
		mGeneralPanel.remove(mLocalFileField);
		
		mGeneralPanel.remove(mRemoteStartButton);
		mGeneralPanel.remove(mRemoteResetBackgroundButton);
		mGeneralPanel.remove(mRemoteStopButton);
		
		mGeneralPanel.revalidate();
	}
	
	public void formatLeftScrollPaneForProcessing() {
		formatLeftScrollPane(true);
		
		mGeneralPanel.revalidate();
	}
	
	public JScrollPane getLeftScrollPane() {
		return mLeftScrollPane;
	}

	private void build() {		
		mPreviewPanel = new CameraPreviewPanel(CONTROLLER_ID, GUI_MAIN);
		
		mLeftScrollPane = createLeftScrollPane();
		
		mGraphicsPanel = new VideoFeedImagePanel(CONTROLLER_ID);
		mGraphicsPanel.setPreferredSize(new Dimension(640, 480));
        
		JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mLeftScrollPane, mGraphicsPanel);
		mainSplitPane.setOneTouchExpandable(true);
        mainSplitPane.setContinuousLayout(true);
        
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(mainSplitPane, BorderLayout.CENTER);
	
		this.setLayout(new BorderLayout());
		this.add(panel, BorderLayout.CENTER);
	}
	
	public void setLocalFileMode() {		
		CameraControllerPreferencesBundle cameraControllerPreferencesBundle = LocalFileRuntimeParams.getCameraControllerPreferencesBundle();
		
		if(cameraControllerPreferencesBundle != null) {
			setAllParams(cameraControllerPreferencesBundle);
		}
	
		mPreviewPanel.invalidate();
		mPreviewPanel.repaint();
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() instanceof JButton) {
			JButton button = (JButton) event.getSource();

			if (button == mRemoteStartButton) {
				processRemoteCamera();
			} else if (button == mRemoteStopButton) {
				if(mLocalViewThread != null) {
					stopRemoteCamera();
				}
			} else if (button == mRemoteResetBackgroundButton) {
				if(mLocalViewThread != null) {
					mLocalViewThread.resetBackground();
				}
			}
		} else if(event.getSource() instanceof JComboBox) {
			JComboBox<?> comboBox = (JComboBox<?>) event.getSource();
			
			if (comboBox == mBlobBorderColorCombo) {
				mBlobParams.setBorderColor(BLOB_BORDER_COLOR_OPTIONS[mBlobBorderColorCombo.getSelectedIndex()]);
			}
			else if(comboBox == mVideoCreationDateCombo) {
				mMetadataParams.setDateCreationMode(VIDEO_CREATION_DATE_OPTIONS[mVideoCreationDateCombo.getSelectedIndex()]);
			}
		}
	}

	@Override
	public void stateChanged(ChangeEvent event) {
		if (event.getSource() instanceof JSlider) {
			JSlider slider = (JSlider) event.getSource();
			
			if(slider.getValueIsAdjusting() == true) {
				return;
			}
			
			if (slider == mRemoteRotateSlider) {
				mRotateDegrees = mRemoteRotateSlider.getValue() * DEFAULT_SLIDER_VALUE_MULTIPLIER;
				mGraphicsPanel.setRotateDegrees(mRotateDegrees);
				mPreviewPanel.setRotateDegrees(mRotateDegrees);
			} else if(slider == mMassThresholdSlider) {
				mMassParams.setThreshold(mMassThresholdSlider.getValue());
			} else if(slider == mMassConsiderationThresholdSlider) {
				mMassParams.setConsiderationThreshold(mMassConsiderationThresholdSlider.getValue());
			} else if(slider == mBackgroundThresholdSlider) {
				mBackgroundParams.setThreshold(mBackgroundThresholdSlider.getValue());
			} else if(slider == mBlobExpansionPercentSlider) {
				mBlobParams.setExpansionPercent((int) (mBlobExpansionPercentSlider.getValue() / 100.0));
			} else if(slider == mCaptureEntropyFilterSlider) {
				mWriteParams.setEntropyThreshold(mCaptureEntropyFilterSlider.getValue());
			}
		}	
	}
	
	@Override
	public void itemStateChanged(ItemEvent event) {
		if (event.getSource() instanceof JCheckBox) {
			JCheckBox checkBox = (JCheckBox) event.getSource();

			if(checkBox == mBlobBorderCheckBox) {
				mBlobParams.setBorderDisplay(mBlobBorderCheckBox.isSelected());
			} else if(checkBox == mStatusBarEnabledCheckBox) {
				mStatusBarParams.setEnabled(mStatusBarEnabledCheckBox.isSelected());
			} else if(checkBox == mAttemptToMergeCheckBox) {
				mBlobParams.setAttemptToMerge(mAttemptToMergeCheckBox.isSelected());
			} else if(checkBox == mHighlightBlobsCheckBox) {
				mBlobParams.setHighlightBlobs(mHighlightBlobsCheckBox.isSelected());
			} else if(checkBox == mWriteCheckBox) {
				mWriteParams.setWriteState(mWriteCheckBox.isSelected());
				
				if(mWriteCheckBox.isSelected() == false) {
					mRecordCoordinatesCheckBox.setEnabled(false);
				} else {
					mRecordCoordinatesCheckBox.setEnabled(true);
				}
			} else if(checkBox == mAutoUpdateCheckBox) {
				mBackgroundParams.setAutoUpdate(mAutoUpdateCheckBox.isSelected());
			} else if(checkBox == mDrawOnSourceCheckBox) {
				mSourceParams.setDrawOnSourceEnabled(mDrawOnSourceCheckBox.isSelected());
			} else if(checkBox == mExpandBlobsCheckBox) {
				mBlobParams.setExpandBlobs(mExpandBlobsCheckBox.isSelected());
				
				if(mExpandBlobsCheckBox.isSelected() == false) {
					mBlobExpansionPercentSlider.setEnabled(false);
				} else {
					mBlobExpansionPercentSlider.setEnabled(true);
				}
			} else if(checkBox == mEntropyFilterCheckBox) {
				mWriteParams.setEntropyFilterState(mEntropyFilterCheckBox.isSelected());
			} else if(checkBox == mExhaustiveSearchCheckBox) {
				mSourceParams.setExhaustiveSearchEnabled(mExhaustiveSearchCheckBox.isSelected());
			} else if(checkBox == mRecordCoordinatesCheckBox) {
				mBlobParams.setSaveBlobCoordintates(mRecordCoordinatesCheckBox.isSelected());
			}
		}
	}
	
	@Override
	public void init() {		
		mMassParams = new MassRuntimeParams();
		mBackgroundParams = new BackgroundRuntimeParams();
		mBlobParams = new BlobRuntimeParams();
		mStatusBarParams = new StatusBarRuntimeParams();
		mWriteParams = new WriteRuntimeParams();
		mSourceParams = new SourceRuntimeParams();
		mMetadataParams = new MetadataRuntimeParams();
	}
	
	@Override
	public boolean isRunning() {		
		if(mLocalViewThread != null) {
			return mLocalViewThread.isRunning();
		}
		
		return false;
	}
	
	@Override
	public void forceShutdown() {		
		if (mLocalViewThread != null) {
			mLocalViewThread.setRunning(false);
			mLocalViewThread.interrupt();
		}
	}
	
	public String getIpString() {
		return mLocalFileField.getText();
	}
	
	@Override
	public void processRemoteCamera() {
		ExtractVideoFrameBlob extractVideoFrameBlob = null;
		String ipString = getIpString();
		
		CaseMetadataWriter.writeNewSourceToContext(GUI_MAIN.getContextFilename(), ipString);
		
		if(mLocalViewThread != null) {
			if(mLocalViewThread.isRunning() == true) {
			
				return;
			}
			else if(mLocalViewThread.isRunning() == false) {
			
				mLocalViewThread = null;
			}
		}
				
		ViewProcessorFactory viewProcessorFactory = new ViewProcessorFactory();
		
		LocalViewProcessor localViewProcessor  = null;
					
		final FrsOsType osType = OsUtilsLegacy.getOperatingSystemType();
			
		if(osType == FrsOsType.Windows)
		{
			final String originalFilename = mLocalFileField.getText();
			
			//int videoFrameCountEstimate = (int) WindowsVideoFrameExtractorLegacy.getVideoDurationInSeconds(originalFilename);
			//videoFrameCountEstimate *= LocalFileRuntimeParams.getGlobalFramesPerSecondTarget();
			String creationDateString = null;
			
			if(mMetadataParams.getDateCreationMode() == MetadataRuntimeParams.DATE_CREATION_MODE_REAL_ONLY ||
					mMetadataParams.getDateCreationMode() == MetadataRuntimeParams.DATE_CREATION_MODE_REAL_OR_INSTANT) {
				creationDateString = WindowsVideoFrameExtractorLegacy.getVideoCreationDateMetadata(originalFilename);
			}
			
			extractVideoFrameBlob = WindowsVideoFrameExtractorLegacy.extractVideoFrames(originalFilename, mSourceParams.getExhaustiveSearchEnabled(), 24, 7);
			ipString = extractVideoFrameBlob.getAbsoluteExtractPath();

			//localViewProcessor = (LocalMeanFilterProcessor) viewProcessorFactory.getLocalProcessor(originalFilename, videoFrameCountEstimate, ViewProcessorFactory.LOCAL_FILE_PROCESSOR, BLOB_CONTEXT_LIST);
			localViewProcessor = (LocalMeanFilterProcessor) viewProcessorFactory.getLocalProcessor(originalFilename, ViewProcessorFactory.LOCAL_FILE_PROCESSOR, BLOB_CONTEXT_LIST);
			((LocalMeanFilterProcessor) localViewProcessor).setExtractVideoFrameThread(extractVideoFrameBlob.getExtractVideoFrameThread());
			((LocalMeanFilterProcessor) localViewProcessor).setCreationDateString(creationDateString);
		}
		
		StatusBarFactory statusBarFactory = new StatusBarFactory();
		
		if(localViewProcessor != null) {
			StatusBar localStatusBar = statusBarFactory.getStatusBar(StatusBarFactory.LOCAL_STATUS_BAR, CONTROLLER_ID);
			
			localViewProcessor.setControllerId(CONTROLLER_ID);
			localViewProcessor.setIp(ipString);
			localViewProcessor.setGraphicsPanel(mGraphicsPanel);
			localViewProcessor.attachStatusBar(localStatusBar);
			localViewProcessor.attachPreviewPanel(mPreviewPanel);
			
			mLocalViewThread = localViewProcessor;
			localViewProcessor.start();
		}
	}
	
	@Override
	public void stopRemoteCamera() {
		if(mLocalViewThread != null) {
			mLocalViewThread.setRunning(false);
			
			StopLocalViewThread stopLocalViewThread = new StopLocalViewThread(mLocalViewThread, mGraphicsPanel, mPreviewPanel);
			stopLocalViewThread.start();
	
			mLocalViewThread = null;
		}
	}
	
	@Override
	public CameraPreviewPanel getPreviewPanel() {
		return mPreviewPanel;
	}

	@Override
	public VideoFeedImagePanel getVideoFeedPanel() {
		return mGraphicsPanel;
	}
	
	@Override
	public MassRuntimeParams getMassParams() {
		return mMassParams;
	}
	
	@Override
	public void setMassParams(final MassRuntimeParams massParams) {
		mMassParams = massParams;
	}
	
	@Override
	public WriteRuntimeParams getWriteParams() {
		return mWriteParams;
	}
	
	@Override
	public void setWriteParams(final WriteRuntimeParams writeParams) {
		mWriteParams = writeParams;
	}
	
	@Override
	public BackgroundRuntimeParams getBackgroundParams() {
		return mBackgroundParams;
	}
	
	@Override
	public void setBackgroundParams(final BackgroundRuntimeParams backgroundParams) {
		mBackgroundParams = backgroundParams;
	}
	
	@Override
	public BlobRuntimeParams getBlobParams() {
		return mBlobParams;
	}
	
	@Override
	public void setBlobParams(final BlobRuntimeParams blobParams) {
		mBlobParams = blobParams;
	}
	
	@Override
	public StatusBarRuntimeParams getStatusBarParams() {
		return mStatusBarParams;
	}
	
	@Override
	public void setStatusBarParams(final StatusBarRuntimeParams statusBarParams) {
		mStatusBarParams = statusBarParams;
	}
	
	@Override
	public SourceRuntimeParams getSourceParams() {
		return mSourceParams;
	}
	
	@Override
	public void setSourceParams(final SourceRuntimeParams sourceParams) {
		mSourceParams = sourceParams;
	}
	
	@Override
	public void setMetadataParams(final MetadataRuntimeParams newMetadataRuntimeParams) {
		mMetadataParams = newMetadataRuntimeParams;
	}
	
	@Override
	public MetadataRuntimeParams getMetadataParams() {
		return mMetadataParams;
	}
	
	public void setAllParams(final CameraControllerPreferencesBundle cameraControllerPreferencesBundle) {
		mMassParams       = cameraControllerPreferencesBundle.mMassParams;
		mWriteParams      = cameraControllerPreferencesBundle.mWriteParams;
		mBackgroundParams = cameraControllerPreferencesBundle.mBackgroundParams;
		mBlobParams       = cameraControllerPreferencesBundle.mBlobParams;
		mStatusBarParams  = cameraControllerPreferencesBundle.mStatusBarParams;
		mSourceParams     = cameraControllerPreferencesBundle.mSourceParams;
		mMetadataParams   = cameraControllerPreferencesBundle.mMetadataParams;
		
		mRemoteRotateSlider.setValue(cameraControllerPreferencesBundle.mRotateDegrees);
		mGraphicsPanel.setRotateDegrees(mRotateDegrees);
		mPreviewPanel.setRotateDegrees(mRotateDegrees);
		
		mMassThresholdSlider.setValue(cameraControllerPreferencesBundle.mMassParams.getThresholdInt());
		mMassConsiderationThresholdSlider.setValue(cameraControllerPreferencesBundle.mMassParams.getConsiderationThresholdInt());
		mBackgroundThresholdSlider.setValue(cameraControllerPreferencesBundle.mBackgroundParams.getThresholdInt());
		mBlobExpansionPercentSlider.setValue((int) (cameraControllerPreferencesBundle.mBlobParams.getExpansionPercent() * 100.0));
		
		mBlobBorderCheckBox.setSelected(cameraControllerPreferencesBundle.mBlobParams.getBorderDisplay());
		mStatusBarEnabledCheckBox.setSelected(cameraControllerPreferencesBundle.mStatusBarParams.getEnabled());
		mAttemptToMergeCheckBox.setSelected(cameraControllerPreferencesBundle.mBlobParams.getAttemptToMerge());
		mHighlightBlobsCheckBox.setSelected(cameraControllerPreferencesBundle.mBlobParams.getHighlightBlobs());
		mWriteCheckBox.setSelected(cameraControllerPreferencesBundle.mWriteParams.getWriteState());
		mAutoUpdateCheckBox.setSelected(cameraControllerPreferencesBundle.mBackgroundParams.getAutoUpdate());
		mDrawOnSourceCheckBox.setSelected(cameraControllerPreferencesBundle.mSourceParams.getDrawOnSourceEnabled());
		mExpandBlobsCheckBox.setSelected(cameraControllerPreferencesBundle.mBlobParams.getExpandBlobs());
		mEntropyFilterCheckBox.setSelected(cameraControllerPreferencesBundle.mWriteParams.getEntropyFilterState());
		mExhaustiveSearchCheckBox.setSelected(cameraControllerPreferencesBundle.mSourceParams.getExhaustiveSearchEnabled());
		mRecordCoordinatesCheckBox.setSelected(cameraControllerPreferencesBundle.mBlobParams.getSaveBlobCoordinates());
		
		for(int i = 0; i < BLOB_BORDER_COLOR_OPTIONS.length; ++i) {
			if(BLOB_BORDER_COLOR_OPTIONS[i] == cameraControllerPreferencesBundle.mBlobParams.getBorderColor()) {
				mBlobBorderColorCombo.setSelectedIndex(i);
			}
		}
	}
	
	public CameraControllerPreferencesBundle getAllParams() {
		final CameraControllerPreferencesBundle cameraControllerPreferencesBundle = new CameraControllerPreferencesBundle(mMassParams, 
				mWriteParams, mBackgroundParams, mBlobParams, mStatusBarParams, mSourceParams, mMetadataParams, mRotateDegrees);
		
		return cameraControllerPreferencesBundle;
	}
	
	@Override
	public LocalViewProcessor getLocalViewProcessor() {
		return mLocalViewThread;
	}
}

package com.keypointforensics.videotriage.gui.controller.remote;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import com.keypointforensics.videotriage.blob.context.BlobContextList;
import com.keypointforensics.videotriage.casestore.CaseMetadataWriter;
import com.keypointforensics.videotriage.gui.imagepanel.VideoFeedImagePanel;
import com.keypointforensics.videotriage.gui.main.CameraPreviewPanel;
import com.keypointforensics.videotriage.gui.main.GuiMain;
import com.keypointforensics.videotriage.gui.main.RotateSteppingSlider;
import com.keypointforensics.videotriage.gui.main.controller.CameraController;
import com.keypointforensics.videotriage.gui.main.controller.CameraControllerInterface;
import com.keypointforensics.videotriage.gui.main.controller.CameraControllerPreferencesBundle;
import com.keypointforensics.videotriage.legacy.ExtractVideoFrameBlob;
import com.keypointforensics.videotriage.legacy.OsUtilsLegacy;
import com.keypointforensics.videotriage.legacy.WindowsVideoFrameExtractorLegacy;
import com.keypointforensics.videotriage.legacy.OsUtilsLegacy.FrsOsType;
import com.keypointforensics.videotriage.local.LocalMeanFilterProcessor;
import com.keypointforensics.videotriage.local.LocalViewProcessor;
import com.keypointforensics.videotriage.params.BackgroundRuntimeParams;
import com.keypointforensics.videotriage.params.BlobRuntimeParams;
import com.keypointforensics.videotriage.params.MassRuntimeParams;
import com.keypointforensics.videotriage.params.SourceRuntimeParams;
import com.keypointforensics.videotriage.params.StatusBarRuntimeParams;
import com.keypointforensics.videotriage.params.WriteRuntimeParams;
import com.keypointforensics.videotriage.processor.ViewProcessorFactory;
import com.keypointforensics.videotriage.remote.GenericRemoteViewProcessor;
import com.keypointforensics.videotriage.remote.RemoteViewAuthenticator;
import com.keypointforensics.videotriage.remote.RemoteViewProcessor;
import com.keypointforensics.videotriage.staticparams.LocalFileRuntimeParams;
import com.keypointforensics.videotriage.statusbar.StatusBar;
import com.keypointforensics.videotriage.statusbar.StatusBarFactory;
import com.keypointforensics.videotriage.thread.StopLocalViewThread;
import com.keypointforensics.videotriage.thread.StopRemoteViewThread;
import com.keypointforensics.videotriage.util.BorderUtils;
import com.keypointforensics.videotriage.util.WindowUtils;

public class RemoteCameraController extends CameraController implements CameraControllerInterface, ActionListener, ChangeListener, ItemListener {

	/*
	 * Author: Daniel Puckowski
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -9135371387119060753L;
	
	private static final String HTTP_PREFIX_STRING = "http://";
	
	private final Color[] BLOB_BORDER_COLOR_OPTIONS = {
			Color.RED,
			Color.YELLOW,
			Color.GREEN,
			Color.WHITE,
			Color.GRAY
	};
	
	private final String[] BLOB_BORDER_COLOR_OPTION_STRINGS = {
			"Red",
			"Yellow",
			"Green",
			"White",
			"Gray"
	};
	
	private final String[] URL_FIELD_OPTION_STRINGS = {
            "Custom URL"
	};
	
	private final int     DEFAULT_SLIDER_VALUE_MULTIPLIER = 90;
	private final int     CUSTOM_URL_SELECTION            = 0;
	private final String  EMPTY_TEXT                      = "";
	
	private final String          CONTROLLER_ID;
	private final GuiMain         GUI_MAIN;
	private final String  		  DATABASE_NAME;
	private final BlobContextList BLOB_CONTEXT_LIST;
	
	private MassRuntimeParams       mMassParams;
	private BlobRuntimeParams       mBlobParams;
	private WriteRuntimeParams      mWriteParams;
	private StatusBarRuntimeParams  mStatusBarParams;
	private BackgroundRuntimeParams mBackgroundParams;
	private SourceRuntimeParams     mSourceParams;
	
	private int                     mRotateDegrees;
	private VideoFeedImagePanel     mGraphicsPanel;
	private CameraPreviewPanel      mPreviewPanel;
	
	private JTextField              mPortField;
	private JTextField              mUsernameField;
	
	private JPasswordField          mPasswordField;
	
	private JButton                 mRemoteStartButton;
	private JButton                 mRemoteResetBackgroundButton;
	private JButton                 mRemoteStopButton;
	
	private RotateSteppingSlider    mRemoteRotateSlider;
	
	private JSlider                 mMassThresholdSlider;
	private JSlider                 mBackgroundThresholdSlider;
	private JSlider                 mMassConsiderationThresholdSlider;
	private JSlider                 mBlobExpansionPercentSlider;
	
	private JCheckBox               mBlobBorderCheckBox;
	private JCheckBox               mStatusBarEnabledCheckBox;
	private JCheckBox               mAttemptToMergeCheckBox;
	private JCheckBox               mHighlightBlobsCheckBox;
	private JCheckBox               mWriteCheckBox;
	private JCheckBox               mAutoUpdateCheckBox;
	private JCheckBox               mDrawOnSourceCheckBox;
	private JCheckBox               mExpandBlobsCheckBox;
	private JCheckBox               mRecordCoordinatesCheckBox;
	
	private JComboBox<String>       mIpOrUrlField;
	private JComboBox<String>       mBlobBorderColorCombo;
	
	private JLabel                  mUrlFieldLabel; 

	private RemoteViewProcessor     mRemoteViewThread;
	private LocalViewProcessor      mLocalViewThread;
	
	private String                  mLastIpOrUrlInput;
	
	public RemoteCameraController(final String cameraId, final GuiMain guiMain, final String databaseName, final BlobContextList blobContextList) {
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
	
	public JComboBox<String> getIpOrUrlField() {
		return mIpOrUrlField;
	}
	
	private void buildTextFields() {				
		mPortField = new JTextField("8080");
		mPortField.setMaximumSize(new Dimension(Integer.MAX_VALUE, mPortField.getPreferredSize().height));
		mPortField.setColumns(20); 
		
		mUsernameField = new JTextField("username");
		mUsernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, mUsernameField.getPreferredSize().height));
		mUsernameField.setColumns(20); 
		
		mPasswordField = new JPasswordField("password");
		mPasswordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, mPasswordField.getPreferredSize().height));
		mPasswordField.setColumns(20); 
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
		mRecordCoordinatesCheckBox = new JCheckBox("Record Coordinates");
		mRecordCoordinatesCheckBox.setSelected(mBlobParams.getSaveBlobCoordinates());
		mRecordCoordinatesCheckBox.addItemListener(this);
	}
	
	private void buildComboBoxes() {		
		mBlobBorderColorCombo = new JComboBox<String>(BLOB_BORDER_COLOR_OPTION_STRINGS);
		mBlobBorderColorCombo.setSelectedItem(0);
		mBlobBorderColorCombo.addActionListener(this);
		
		mIpOrUrlField = new JComboBox<String>(URL_FIELD_OPTION_STRINGS);
		mIpOrUrlField.setEditable(true);
		mIpOrUrlField.addActionListener(this);
		final JTextComponent ipOrUrlTextComponent = (JTextComponent) mIpOrUrlField.getEditor().getEditorComponent();
		ipOrUrlTextComponent.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent event) {
				
			}

			@Override
			public void insertUpdate(DocumentEvent event) {
				if(mUrlFieldLabel.getText().equals(URL_FIELD_OPTION_STRINGS[CUSTOM_URL_SELECTION]) == true) {
					String selection = getIpString(); 
					
					if(selection == null) {

						return;
					}
					else if(selection.isEmpty() == true) {

						return;
					}
					else if(selection.equals(URL_FIELD_OPTION_STRINGS[CUSTOM_URL_SELECTION]) == true) {

						return;
					}
					
					mLastIpOrUrlInput = selection;
					
					if(selection.startsWith(HTTP_PREFIX_STRING) == false) {
						selection = HTTP_PREFIX_STRING + selection;
					}
					
					URL url = null;

					try {
						url = new URL(selection);

						int urlPort = url.getPort();
						
						if(urlPort != -1) {
							final String portString = String.valueOf(urlPort);
							mPortField.setText(portString);
						}
					} catch (MalformedURLException malformedUrlException) {

					}	
				}
			}

			@Override
			public void removeUpdate(DocumentEvent event) {
				if(mUrlFieldLabel.getText().equals(URL_FIELD_OPTION_STRINGS[CUSTOM_URL_SELECTION]) == true) {
					String selection = getIpString();
					
					if(selection == null) {
						
						return;
					}
					else if(selection.isEmpty() == true) {
						
						return;
					}
					
					if(selection.startsWith(HTTP_PREFIX_STRING) == false) {
						selection = HTTP_PREFIX_STRING + selection;
					}
					
					mLastIpOrUrlInput = selection;

					URL url = null;
					
					try {
						url = new URL(selection);
						
						int urlPort = url.getPort();
						
						if(urlPort != -1) {
							final String portString = String.valueOf(url.getPort());
							mPortField.setText(portString);
						}
					} catch (MalformedURLException malformedUrlException) {

					}	
				}
			} 
		});
		mIpOrUrlField.setSelectedItem(EMPTY_TEXT);
	}
	
	private void build() {		
		mPreviewPanel = new CameraPreviewPanel(CONTROLLER_ID, GUI_MAIN);
		
		JTabbedPane leftPanel = new JTabbedPane(); 
		
		buildTextFields();
		mUrlFieldLabel = new JLabel("Custom URL"); 
		JLabel portFieldLabel = new JLabel("Port");
		JLabel usernameFieldLabel = new JLabel("Username");
		JLabel passwordFieldLabel = new JLabel("Password");
		
		buildButtons();
		
		buildSliders();
		JLabel rotateSliderLabel = new JLabel("Rotate Source");	
		JLabel massThresholdLabel = new JLabel("Set Mass Threshold");
		JLabel backgroundThresholdLabel = new JLabel("Set Background Threshold");
		JLabel massConsiderationThresholdLabel = new JLabel("Mass Consideration Threshold");
		JLabel blobExpansionPercentLabel = new JLabel("Object Expansion Percent");
		
		buildCheckBoxes();
		
		buildComboBoxes();
		JLabel blobBorderColorLabel = new JLabel("Object Border Color");
		
		JPanel generalPanel = new JPanel();
		generalPanel.setLayout(new GridLayout(13, 1));
		generalPanel.add(mUrlFieldLabel);
		generalPanel.add(mIpOrUrlField);//mIpField);
		generalPanel.add(portFieldLabel);
		generalPanel.add(mPortField);
		generalPanel.add(mWriteCheckBox);
		generalPanel.add(mWriteCheckBox);
		generalPanel.add(mAutoUpdateCheckBox);
		generalPanel.add(mStatusBarEnabledCheckBox);
		generalPanel.add(mRemoteStartButton);
		generalPanel.add(mRemoteResetBackgroundButton);
		generalPanel.add(mRemoteStopButton);
		generalPanel.setBorder(BorderUtils.getEmptyBorder());
		
		JPanel sourcePanel = new JPanel();
		sourcePanel.setLayout(new GridLayout(13, 1));
		sourcePanel.add(usernameFieldLabel);
		sourcePanel.add(mUsernameField);
		sourcePanel.add(passwordFieldLabel);
		sourcePanel.add(mPasswordField);
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
		advancedPanel.setBorder(BorderUtils.getEmptyBorder());
		
		leftPanel.addTab("General", null, generalPanel, "Standard tracking configuration");
		leftPanel.addTab("Source", null, sourcePanel, "Video source configuration");
		leftPanel.addTab("Detection", null, blobPanel, "Video detection configuration");
		leftPanel.addTab("Advanced", null, advancedPanel, "Advanced tracking configuration");
		
		JScrollPane leftScrollPane = new JScrollPane(leftPanel);
		WindowUtils.setScrollBarIncrement(leftScrollPane);
		leftScrollPane.setPreferredSize(new Dimension(260, 650));
		leftScrollPane.setMinimumSize(new Dimension(260, 650));
		
		mGraphicsPanel = new VideoFeedImagePanel(CONTROLLER_ID);
		mGraphicsPanel.setPreferredSize(new Dimension(640, 480));
        
		JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScrollPane, mGraphicsPanel);
		mainSplitPane.setOneTouchExpandable(true);
        mainSplitPane.setContinuousLayout(true);
        
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(mainSplitPane, BorderLayout.CENTER);
	
		this.setLayout(new BorderLayout());
		this.add(panel, BorderLayout.CENTER);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() instanceof JButton) {
			JButton button = (JButton) event.getSource();

			if (button == mRemoteStartButton) {
				processRemoteCamera();
			} else if (button == mRemoteStopButton) {
				if (mRemoteViewThread != null) {
					stopRemoteCamera();
				}
				else if(mLocalViewThread != null) {
					stopRemoteCamera();
				}
			} else if (button == mRemoteResetBackgroundButton) {
				if (mRemoteViewThread != null) {
					mRemoteViewThread.resetBackground();
				}
				else if(mLocalViewThread != null) {
					mLocalViewThread.resetBackground();
				}
			}
		} else if(event.getSource() instanceof JComboBox) {
			JComboBox<?> comboBox = (JComboBox<?>) event.getSource();
			
			if(comboBox == mIpOrUrlField) {
				final String selection = getIpString();

				if(selection == null) {					
					return;
				}
				else if(selection.isEmpty() == true) {					
					return;
				}
				
				if(selection.equals(URL_FIELD_OPTION_STRINGS[CUSTOM_URL_SELECTION]) == true) {
					mUrlFieldLabel.setText(URL_FIELD_OPTION_STRINGS[CUSTOM_URL_SELECTION]);
					mIpOrUrlField.setSelectedItem(mLastIpOrUrlInput);
					
					mPreviewPanel.invalidate();
					mPreviewPanel.repaint();
				}
			}
			else if (comboBox == mBlobBorderColorCombo) {
				mBlobParams.setBorderColor(BLOB_BORDER_COLOR_OPTIONS[mBlobBorderColorCombo.getSelectedIndex()]);
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
			} else if(slider == mBackgroundThresholdSlider) {
				mBackgroundParams.setThreshold(mBackgroundThresholdSlider.getValue());
			} else if(slider == mMassConsiderationThresholdSlider) {
				mMassParams.setConsiderationThreshold(mMassConsiderationThresholdSlider.getValue());
			} else if(slider == mBackgroundThresholdSlider) {
				mBackgroundParams.setThreshold(mBackgroundThresholdSlider.getValue());
			} else if(slider == mBlobExpansionPercentSlider) {
				mBlobParams.setExpansionPercent((int) (mBlobExpansionPercentSlider.getValue() / 100.0));
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
			} else if(checkBox == mAutoUpdateCheckBox) {
				mBackgroundParams.setAutoUpdate(mAutoUpdateCheckBox.isSelected());
			} else if(checkBox == mDrawOnSourceCheckBox) {
				mSourceParams.setDrawOnSourceEnabled(mDrawOnSourceCheckBox.isSelected());
			} else if(checkBox == mExpandBlobsCheckBox) {
				mBlobParams.setExpandBlobs(mExpandBlobsCheckBox.isSelected());
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
	}
	
	@Override
	public boolean isRunning() {		
		if (mRemoteViewThread != null) {
			return mRemoteViewThread.isRunning();
		}
		else if(mLocalViewThread != null) {
			return mLocalViewThread.isRunning();
		}
		
		return false;
	}
	
	@Override
	public void forceShutdown() {		
		if (mRemoteViewThread != null) {		
			mRemoteViewThread.setRunning(false);
			mRemoteViewThread.interrupt();
		}
		else if (mLocalViewThread != null) {			
			mLocalViewThread.setRunning(false);
			mLocalViewThread.interrupt();
		}
	}
	
	public String getIpString() {
		if(mIpOrUrlField == null) {
			return null;
		}
		
		final String selection = (String) mIpOrUrlField.getEditor().getItem();

		return selection;
	}

	public String getPortString() {
		return mPortField.getText();
	}

	public boolean getCustomUrlEnabled(){
		if(mUrlFieldLabel == null) {			
			return false;
		}
				
		if(mUrlFieldLabel.getText().equals(URL_FIELD_OPTION_STRINGS[CUSTOM_URL_SELECTION]) == true) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public String getUsername() {
		return mUsernameField.getText();
	}
	
	public char[] getPassword() {
		return mPasswordField.getPassword();
	}
	
	@Override
	public void processRemoteCamera() {
		ExtractVideoFrameBlob extractVideoFrameBlob = null;
		String ipString = getIpString();
		
		CaseMetadataWriter.writeNewSourceToContext(GUI_MAIN.getContextFilename(), ipString);
		
		if(ipString == null) {			
			return;
		} 
		else if(ipString.isEmpty() == true) {			
			return;
		}
		
		final String portString = getPortString();
		
		if(portString == null) {			
			return;
		} 
		else if(portString.isEmpty() == true) {			
			return;
		}
				
		if(mRemoteViewThread != null) {
			if(mRemoteViewThread.isRunning() == true) {
				return;
			}
			else if(mRemoteViewThread.isRunning() == false) {
				mRemoteViewThread = null;
			}
		} 
		
		if(mLocalViewThread != null) {
			if(mLocalViewThread.isRunning() == true) {
				return;
			}
			else if(mLocalViewThread.isRunning() == false) {
				mLocalViewThread = null;
			}
		}
				
		ViewProcessorFactory viewProcessorFactory = new ViewProcessorFactory();
		
		RemoteViewProcessor remoteViewProcessor = null;
		LocalViewProcessor  localViewProcessor  = null;
		
		if(mUrlFieldLabel.getText().equals(URL_FIELD_OPTION_STRINGS[CUSTOM_URL_SELECTION]) == true) {			
			remoteViewProcessor = (GenericRemoteViewProcessor) viewProcessorFactory.getRemoteProcessor(GUI_MAIN, ViewProcessorFactory.GENERIC_REMOTE_PROCESSOR, BLOB_CONTEXT_LIST);
		}
		else if(mUrlFieldLabel.getText().equals("Local File") == true) { 			
			final FrsOsType osType = OsUtilsLegacy.getOperatingSystemType();
			
				if(osType == FrsOsType.Windows)
				{
					final String sourceUrl = mIpOrUrlField.getSelectedItem().toString();
					
					int videoFrameCountEstimate = (int) WindowsVideoFrameExtractorLegacy.getVideoDurationInSeconds(sourceUrl);
					videoFrameCountEstimate *= LocalFileRuntimeParams.getGlobalFramesPerSecondTarget();
					
					extractVideoFrameBlob = WindowsVideoFrameExtractorLegacy.extractVideoFrames(sourceUrl, mSourceParams.getExhaustiveSearchEnabled(), 24, 7);
					ipString = extractVideoFrameBlob.getAbsoluteExtractPath();

					//localViewProcessor = (LocalMeanFilterProcessor) viewProcessorFactory.getLocalProcessor(sourceUrl, videoFrameCountEstimate, ViewProcessorFactory.LOCAL_FILE_PROCESSOR, BLOB_CONTEXT_LIST);
					localViewProcessor = (LocalMeanFilterProcessor) viewProcessorFactory.getLocalProcessor(sourceUrl, ViewProcessorFactory.LOCAL_FILE_PROCESSOR, BLOB_CONTEXT_LIST);
					((LocalMeanFilterProcessor) localViewProcessor).setExtractVideoFrameThread(extractVideoFrameBlob.getExtractVideoFrameThread());
				}
				else
				{
					return;
				}
		
		}
	
		if(remoteViewProcessor == null && localViewProcessor == null)
		{
			if(remoteViewProcessor == null) {				
				return;
			}
		}
					
		StatusBarFactory statusBarFactory = new StatusBarFactory();
		
		if(remoteViewProcessor != null) {
			StatusBar remoteStatusBar = statusBarFactory.getStatusBar(StatusBarFactory.REMOTE_STATUS_BAR, CONTROLLER_ID);
			RemoteViewAuthenticator authenticator = new RemoteViewAuthenticator(getUsername(), getPassword());

			remoteViewProcessor.setControllerId(CONTROLLER_ID);
			remoteViewProcessor.setIp(ipString);
			remoteViewProcessor.setPort(portString);
			remoteViewProcessor.setGraphicsPanel(mGraphicsPanel);
			remoteViewProcessor.setAuthenticator(authenticator);
			remoteViewProcessor.attachStatusBar(remoteStatusBar);
			remoteViewProcessor.attachPreviewPanel(mPreviewPanel);
			
			mRemoteViewThread = remoteViewProcessor;
			remoteViewProcessor.start();
		}
		else if(localViewProcessor != null) {
			StatusBar localStatusBar = statusBarFactory.getStatusBar(StatusBarFactory.LOCAL_STATUS_BAR, CONTROLLER_ID);
			
			localViewProcessor.setControllerId(CONTROLLER_ID);
			localViewProcessor.setIp(ipString);
			localViewProcessor.setPort(portString);
			localViewProcessor.setGraphicsPanel(mGraphicsPanel);
			localViewProcessor.attachStatusBar(localStatusBar);
			localViewProcessor.attachPreviewPanel(mPreviewPanel);
			
			mLocalViewThread = localViewProcessor;
			localViewProcessor.start();
		}
	}
	
	@Override
	public void stopRemoteCamera() {
		if(mRemoteViewThread != null) {			
			mRemoteViewThread.setRunning(false);
			
			StopRemoteViewThread stopRemoteViewThread = new StopRemoteViewThread(mRemoteViewThread, mGraphicsPanel, mPreviewPanel);
			stopRemoteViewThread.start();
	
			mRemoteViewThread = null;
		}
		else if(mLocalViewThread != null) {
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
		if(massParams == null) {			
			return;
		}
		
		mMassParams = massParams;
	}
	
	@Override
	public WriteRuntimeParams getWriteParams() {
		return mWriteParams;
	}
	
	@Override
	public void setWriteParams(final WriteRuntimeParams writeParams) {
		if(writeParams == null) {			
			return;
		}
		
		mWriteParams = writeParams;
	}
	
	@Override
	public BackgroundRuntimeParams getBackgroundParams() {
		return mBackgroundParams;
	}
	
	@Override
	public void setBackgroundParams(final BackgroundRuntimeParams backgroundParams) {
		if(backgroundParams == null) {			
			return;
		}
		
		mBackgroundParams = backgroundParams;
	}
	
	@Override
	public BlobRuntimeParams getBlobParams() {
		return mBlobParams;
	}
	
	@Override
	public void setBlobParams(final BlobRuntimeParams blobParams) {
		if(blobParams == null) {			
			return;
		}
		
		mBlobParams = blobParams;
	}
	
	@Override
	public StatusBarRuntimeParams getStatusBarParams() {
		return mStatusBarParams;
	}
	
	@Override
	public void setStatusBarParams(final StatusBarRuntimeParams statusBarParams) {
		if(statusBarParams == null) {			
			return;
		}
		
		mStatusBarParams = statusBarParams;
	}

	@Override
	public SourceRuntimeParams getSourceParams() {
		return mSourceParams;
	}
	
	@Override
	public void setSourceParams(final SourceRuntimeParams sourceParams) {
		if(sourceParams == null) {			
			return;
		}
		
		mSourceParams = sourceParams;
	}
	
	@Override
	public void setAllParams(CameraControllerPreferencesBundle cameraControllerPreferencesBundle) {
	
		return;
	}

	@Override
	public CameraControllerPreferencesBundle getAllParams() {
		
		return null;
	}
}

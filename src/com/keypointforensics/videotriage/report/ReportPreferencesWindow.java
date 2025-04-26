package com.keypointforensics.videotriage.report;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.keypointforensics.videotriage.gui.main.FileSelectImagePreviewAccessory;
import com.keypointforensics.videotriage.legacy.VideoFalsePositiveRemoverByReferenceLegacy;
import com.keypointforensics.videotriage.util.BorderUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.Utils;
import com.keypointforensics.videotriage.util.WebUtils;
import com.keypointforensics.videotriage.util.WindowUtils;
import com.keypointforensics.videotriage.window.WindowRegistry;

public class ReportPreferencesWindow extends JFrame implements ChangeListener, ItemListener, ActionListener {

	/*
	 * Author: Daniel Puckowski
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3931002670526389551L;

	private final VideoFalsePositiveRemoverByReferenceLegacy VIDEO_FALSE_POSITIVE_REMOVER_BY_REFERENCE_LEGACY;
	private final ImageGallery                               IMAGE_GALLERY;
	
	private final int GRID_LAYOUT_ROWS    = 20;
	private final int GRID_LAYOUT_COLUMNS = 1;
	
	private JSlider    imageSurfSimilaritySlider;
	private JSlider    imageDifferenceThresholdSlider;
	private JSlider    imageSimilarityThresholdSlider;
	private JCheckBox  mReportPaginationCheckBox;
	private JCheckBox  mReportIconCheckBox;
	private JCheckBox  mCustomReportIconCheckBox;
	private JTextField mCustomReportIconField;
	private JButton    mCustomReportIconButton;
	private JCheckBox  mMetadataPageCheckBox;
	private JCheckBox  mStatisticsPageCheckBox;
	
	private boolean mIgnoreReportFieldUpdateOnce;
	
	public ReportPreferencesWindow(final ImageGallery imageGallery, final VideoFalsePositiveRemoverByReferenceLegacy videoFalsePositiveRemoverByReferenceLegacy) {
		VIDEO_FALSE_POSITIVE_REMOVER_BY_REFERENCE_LEGACY = videoFalsePositiveRemoverByReferenceLegacy;
		IMAGE_GALLERY = imageGallery;
		
		buildMenuBar();
		
		this.setLayout(new BorderLayout());
		
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout());

		JPanel controlsPanel = new JPanel();
		controlsPanel.setLayout(new GridLayout(GRID_LAYOUT_ROWS, GRID_LAYOUT_COLUMNS));
		controlsPanel.setBorder(BorderUtils.getEmptyBorder());
		
		JLabel imageSurfSimilarityLabel = new JLabel("Image Descriptor Similarity Value");
		controlsPanel.add(imageSurfSimilarityLabel);
		int sliderMin = 1;
		int sliderMax = 100;
		int sliderInit = (int) videoFalsePositiveRemoverByReferenceLegacy.getCurrentImageSurfSimilarity();
		imageSurfSimilaritySlider = new JSlider(JSlider.HORIZONTAL, sliderMin,
				sliderMax, sliderInit);
		imageSurfSimilaritySlider.addChangeListener(this);
		try {
			imageSurfSimilaritySlider.removeMouseWheelListener(imageSurfSimilaritySlider.getMouseWheelListeners()[0]);
		} catch(Exception exception) {	

		}
		imageSurfSimilaritySlider.setMajorTickSpacing(9);
		imageSurfSimilaritySlider.setPaintTicks(true);
		imageSurfSimilaritySlider.setPaintLabels(true);
		controlsPanel.add(imageSurfSimilaritySlider);
		imageSurfSimilarityLabel = new JLabel("Worst match is 1. Perfect match is 100. Lower value will eliminate more files.");
		controlsPanel.add(imageSurfSimilarityLabel);
		
		JLabel imageDifferenceThresholdLabel = new JLabel("Image Difference Threshold Value");
		controlsPanel.add(imageDifferenceThresholdLabel);
		sliderMin = 5;
		sliderMax = 35;
		sliderInit = (int) videoFalsePositiveRemoverByReferenceLegacy.getCurrentImageDifferenceThreshold();
		imageDifferenceThresholdSlider = new JSlider(JSlider.HORIZONTAL, sliderMin,
				sliderMax, sliderInit);
		imageDifferenceThresholdSlider.addChangeListener(this);
		try {
			imageDifferenceThresholdSlider.removeMouseWheelListener(imageDifferenceThresholdSlider.getMouseWheelListeners()[0]);
		} catch(Exception exception) {	

		}
		imageDifferenceThresholdSlider.setMajorTickSpacing(5);
		imageDifferenceThresholdSlider.setPaintTicks(true);
		imageDifferenceThresholdSlider.setPaintLabels(true);
		controlsPanel.add(imageDifferenceThresholdSlider);
		imageDifferenceThresholdLabel = new JLabel("Worst match is 35. Perfect match is 5. Lower value will eliminate fewer files.");
		controlsPanel.add(imageDifferenceThresholdLabel);
		
		JLabel imageSimilarityThresholdLabel = new JLabel("Image Similarity Threshold Value");
		controlsPanel.add(imageSimilarityThresholdLabel);
		sliderMin = -100;
		sliderMax = 100;
		sliderInit = (int) (videoFalsePositiveRemoverByReferenceLegacy.getCurrentImageSimilarityThreshold() * 100.0);
		imageSimilarityThresholdSlider = new JSlider(JSlider.HORIZONTAL, sliderMin,
				sliderMax, sliderInit);
		imageSimilarityThresholdSlider.addChangeListener(this);
		try {
			imageSimilarityThresholdSlider.removeMouseWheelListener(imageSimilarityThresholdSlider.getMouseWheelListeners()[0]);
		} catch(Exception exception) {	

		}
		imageSimilarityThresholdSlider.setMajorTickSpacing(25);
		imageSimilarityThresholdSlider.setPaintTicks(true);
		imageSimilarityThresholdSlider.setPaintLabels(true);
		controlsPanel.add(imageSimilarityThresholdSlider);
		imageSimilarityThresholdLabel = new JLabel("Worst match is -100. Perfect match is 100. Lower value will eliminate more files.");
		controlsPanel.add(imageSimilarityThresholdLabel);
		
		mReportPaginationCheckBox = new JCheckBox("Report Pagination Enabled");
		mReportPaginationCheckBox.setSelected(IMAGE_GALLERY.getReportPaginationEnabled());
		mReportPaginationCheckBox.addItemListener(this);
		controlsPanel.add(mReportPaginationCheckBox);
		
		mStatisticsPageCheckBox = new JCheckBox("Create Statistics Page");
		mStatisticsPageCheckBox.setSelected(IMAGE_GALLERY.getStatisticsPageEnabled());
		mStatisticsPageCheckBox.addItemListener(this);
		controlsPanel.add(mStatisticsPageCheckBox);
		
		mMetadataPageCheckBox = new JCheckBox("Create Video Metadata Page");
		mMetadataPageCheckBox.setSelected(IMAGE_GALLERY.getMetadataPageEnabled());
		mMetadataPageCheckBox.addItemListener(this);
		controlsPanel.add(mMetadataPageCheckBox);
		
		mReportIconCheckBox = new JCheckBox("Report Icon Enabled");
		mReportIconCheckBox.setSelected(IMAGE_GALLERY.getReportIconEnabled());
		mReportIconCheckBox.addItemListener(this);
		controlsPanel.add(mReportIconCheckBox);
		
		mCustomReportIconCheckBox = new JCheckBox("Use Custom Report Icon");
		mCustomReportIconCheckBox.setSelected(IMAGE_GALLERY.getCustomReportIconEnabled());
		mCustomReportIconCheckBox.addItemListener(this);
		controlsPanel.add(mCustomReportIconCheckBox);
		
		JPanel customReportIconPanel = new JPanel();
		customReportIconPanel.setLayout(new BorderLayout());
		customReportIconPanel.setBorder(BorderUtils.getEmptyBorder());
		
		mCustomReportIconField = new JTextField();
		addCustomDocumentListenerToReportField();
		customReportIconPanel.add(mCustomReportIconField, BorderLayout.CENTER);
		
		mCustomReportIconButton = new JButton("Select Icon");
		mCustomReportIconButton.addActionListener(this);
		customReportIconPanel.add(mCustomReportIconButton, BorderLayout.EAST);
		
		controlsPanel.add(customReportIconPanel);
		
		JButton resetImageSurfSimilarityButton = new JButton("Reset Image Descriptor Similarity");
		resetImageSurfSimilarityButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				performResetImageSurfSimilarityAction();
			}			
		});
		controlsPanel.add(resetImageSurfSimilarityButton);
		
		JButton resetImageDifferenceThresholdButton = new JButton("Reset Image Difference Threshold");
		resetImageDifferenceThresholdButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				performResetImageDifferenceAction();
			}			
		});
		controlsPanel.add(resetImageDifferenceThresholdButton);
		
		JButton resetImageSimilarityThresholdButton = new JButton("Reset Image Similarity Threshold");
		resetImageSimilarityThresholdButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				performResetImageSimilarityAction();
			}			
		});
		controlsPanel.add(resetImageSimilarityThresholdButton);
		
		JButton resetFalsePositiveAndMinimumButton = new JButton("Reset All Preferences");
		resetFalsePositiveAndMinimumButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				performResetMinimumImagesAndFalsePositiveAction();
			}			
		});
		controlsPanel.add(resetFalsePositiveAndMinimumButton);
		
		JButton closeWindowButton = new JButton("Done");
		closeWindowButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				ReportPreferencesWindow.this.dispose();
			}			
		});
		controlsPanel.add(closeWindowButton);
		
		JTabbedPane settingsTabPane = new JTabbedPane();
		settingsTabPane.addTab("Advanced Settings", controlsPanel);
		
		contentPanel.add(settingsTabPane, BorderLayout.CENTER); 
		
		this.add(contentPanel, BorderLayout.CENTER);
		
		WindowUtils.setFrameIcon(this);
		this.setTitle("Report Settings");
		this.setPreferredSize(new Dimension(660, 780));
		this.pack();
		this.setVisible(true);
		WindowUtils.center(this);
		
		WindowRegistry.getInstance().registerFrame(this, "ReportPreferences");
	}
	
	private void addCustomDocumentListenerToReportField() {
		mCustomReportIconField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {

			}

			@Override
			public void removeUpdate(DocumentEvent e) {

			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				mCustomReportIconCheckBox.setSelected(true);
				
				if (mIgnoreReportFieldUpdateOnce == false) {
					IMAGE_GALLERY.setCustomReportIconFile(new File(mCustomReportIconField.getText()));
				} else {
					mIgnoreReportFieldUpdateOnce = false;
				}
			}
		});
	}
	
	private void performSelectFileAction() {		
		Boolean old = UIManager.getBoolean("FileChooser.readOnly");  
		UIManager.put("FileChooser.readOnly", Boolean.TRUE);  		
		
		final JFileChooser fileChooser = new JFileChooser(); 
		final FileSelectImagePreviewAccessory imagePreviewAccessory = new FileSelectImagePreviewAccessory();
		fileChooser.setAccessory(imagePreviewAccessory);
		fileChooser.addPropertyChangeListener(imagePreviewAccessory);
		fileChooser.setMultiSelectionEnabled(false);
		
		final int fileChooserResult = fileChooser.showOpenDialog(this);
				
		if(fileChooserResult == JFileChooser.APPROVE_OPTION) {
			final File selectedFile = fileChooser.getSelectedFile();
			
			IMAGE_GALLERY.setCustomReportIconFile(selectedFile);
			mCustomReportIconField.setText(selectedFile.getAbsolutePath());
			mIgnoreReportFieldUpdateOnce = true;
		}
		
		UIManager.put("FileChooser.readOnly", old); 
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() instanceof JButton) {
			JButton button = (JButton) event.getSource();

			if(button == mCustomReportIconButton) {
				performSelectFileAction();
			}
		}
	}
	
	@Override
	public void itemStateChanged(ItemEvent event) {
		if (event.getSource() instanceof JCheckBox) {
			JCheckBox checkBox = (JCheckBox) event.getSource();

			if(checkBox == mReportPaginationCheckBox) {
				IMAGE_GALLERY.setReportPaginationEnabled(mReportPaginationCheckBox.isSelected());
			}
			else if (checkBox == mReportIconCheckBox) {
				IMAGE_GALLERY.setReportIconEnabled(mReportIconCheckBox.isSelected());
			}
			else if (checkBox == mCustomReportIconCheckBox) {
				IMAGE_GALLERY.setCustomReportIconEnabled(mCustomReportIconCheckBox.isSelected());
			}
			else if(checkBox == mMetadataPageCheckBox) {
				IMAGE_GALLERY.setMetadataPageEnabled(mMetadataPageCheckBox.isSelected());
			}
			else if(checkBox == mStatisticsPageCheckBox) {
				IMAGE_GALLERY.setStatisticsPageEnabled(mStatisticsPageCheckBox.isSelected());
			}
		}
	}
	
	private void performResetImageSurfSimilarityAction() {
		VIDEO_FALSE_POSITIVE_REMOVER_BY_REFERENCE_LEGACY.resetCurrentImageSurfSimilarity();
		imageSurfSimilaritySlider.setValue((int) VIDEO_FALSE_POSITIVE_REMOVER_BY_REFERENCE_LEGACY.getCurrentImageSurfSimilarity()); 
	}
	
	private void performResetImageSimilarityAction() {
		VIDEO_FALSE_POSITIVE_REMOVER_BY_REFERENCE_LEGACY.resetCurrentImageSimilarityThreshold();
		imageSimilarityThresholdSlider.setValue((int) VIDEO_FALSE_POSITIVE_REMOVER_BY_REFERENCE_LEGACY.getCurrentImageSimilarityThreshold()); 
	}
	
	private void performResetImageDifferenceAction() {
		VIDEO_FALSE_POSITIVE_REMOVER_BY_REFERENCE_LEGACY.resetCurrentImageDifferenceThreshold();
		imageDifferenceThresholdSlider.setValue((int) VIDEO_FALSE_POSITIVE_REMOVER_BY_REFERENCE_LEGACY.getCurrentImageDifferenceThreshold()); 
	}
	
	private void performResetMinimumImagesAndFalsePositiveAction() {
		performResetImageSimilarityAction();
		performResetImageDifferenceAction();
		performResetImageSurfSimilarityAction();
	}
	
	@Override
	public void stateChanged(ChangeEvent event) {
		if (event.getSource() instanceof JSlider) {
			JSlider slider = (JSlider) event.getSource();
			
			if(slider.getValueIsAdjusting() == true) {
				return;
			}
			
			if(slider == imageSimilarityThresholdSlider) {
				int imageSimilarityThresholdValue = imageSimilarityThresholdSlider.getValue();
								
				VIDEO_FALSE_POSITIVE_REMOVER_BY_REFERENCE_LEGACY.setCurrentImageSimilarityThreshold(imageSimilarityThresholdValue);
			}
			else if(slider == imageDifferenceThresholdSlider) {
				int imageDifferenceThresholdValue = imageDifferenceThresholdSlider.getValue();
								
				VIDEO_FALSE_POSITIVE_REMOVER_BY_REFERENCE_LEGACY.setCurrentImageDifferenceThreshold(imageDifferenceThresholdValue);
			}
			else if(slider == imageSurfSimilaritySlider) {
				int imageSurfSimilarityValue = imageSurfSimilaritySlider.getValue();
				
				VIDEO_FALSE_POSITIVE_REMOVER_BY_REFERENCE_LEGACY.setCurrentImageSurfSimilarity(imageSurfSimilarityValue);
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
						ThreadUtils.addThreadToHandleList("ReportPref Exit", this);
						
						ReportPreferencesWindow.this.dispose();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menu = new JMenu("Settings");
		menuBar.add(menu);
		
		menuItem = new JMenuItem("Reset Image Difference Threshold");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ReportPref ResetImDiff", this);
						
						performResetImageDifferenceAction();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Reset Image Similarity Threshold");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ReportPref ResetImSim", this);
						
						performResetImageSimilarityAction();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Reset All Preferences");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ReportPref ResetAll", this);
						
						performResetMinimumImagesAndFalsePositiveAction();
						
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
						ThreadUtils.addThreadToHandleList("ReportPref OpenDoc", this);
						
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
						ThreadUtils.addThreadToHandleList("ReportPref About", this);
						
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

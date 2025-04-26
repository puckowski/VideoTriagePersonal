package com.keypointforensics.videotriage.gui.gallery;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.keypointforensics.videotriage.legacy.VideoFalsePositiveRemoverByReferenceLegacy;
import com.keypointforensics.videotriage.util.BorderUtils;
import com.keypointforensics.videotriage.util.CursorUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.Utils;
import com.keypointforensics.videotriage.util.WebUtils;
import com.keypointforensics.videotriage.util.WindowUtils;
import com.keypointforensics.videotriage.window.WindowRegistry;

public class FileListAutomaticReviewWindow extends JFrame implements ChangeListener {

	/*
	 * Author: Daniel Puckowski
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3931002670526389551L;

	private final FileListImageGallery                       IMAGE_GALLERY;
	private final VideoFalsePositiveRemoverByReferenceLegacy VIDEO_FALSE_POSITIVE_REMOVER_BY_REFERENCE_LEGACY;
	
	private JSlider imageDifferenceThresholdSlider;
	private JSlider imageSimilarityThresholdSlider;
	private JSlider imageSurfThresholdSlider;
	
	public FileListAutomaticReviewWindow(final FileListImageGallery imageGallery, final VideoFalsePositiveRemoverByReferenceLegacy videoFalsePositiveRemoverByReferenceLegacy) {
		IMAGE_GALLERY = imageGallery;
		VIDEO_FALSE_POSITIVE_REMOVER_BY_REFERENCE_LEGACY = videoFalsePositiveRemoverByReferenceLegacy;
		
		buildMenuBar();
		
		this.setLayout(new BorderLayout());
		
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout());

		JPanel controlsPanel = new JPanel();
		controlsPanel.setBorder(BorderUtils.getEmptyBorder());
		controlsPanel.setLayout(new GridLayout(15, 1));

		JLabel imageSurfThresholdLabel = new JLabel("Image Descriptor Similarity Value");
		controlsPanel.add(imageSurfThresholdLabel);
		int sliderMin = 1;
		int sliderMax = 100;
		int sliderInit = (int) (videoFalsePositiveRemoverByReferenceLegacy.getCurrentImageSurfSimilarity() * 100.0);
		imageSurfThresholdSlider = new JSlider(JSlider.HORIZONTAL, sliderMin,
				sliderMax, sliderInit);
		imageSurfThresholdSlider.addChangeListener(this);
		try {
			imageSurfThresholdSlider.removeMouseWheelListener(imageSurfThresholdSlider.getMouseWheelListeners()[0]);
		} catch(Exception exception) {	

		}
		imageSurfThresholdSlider.setMajorTickSpacing(9);
		imageSurfThresholdSlider.setPaintTicks(true);
		imageSurfThresholdSlider.setPaintLabels(true);
		controlsPanel.add(imageSurfThresholdSlider);
		imageSurfThresholdLabel = new JLabel("Worst match is 1. Perfect match is 100. Lower value will eliminate more files.");
		controlsPanel.add(imageSurfThresholdLabel);
		
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
		
		JButton resetImageSurfThresholdButton = new JButton("Reset Image Descriptor Similarity");
		resetImageSurfThresholdButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				performResetImageSurfSimilarityAction();
			}			
		});
		controlsPanel.add(resetImageSurfThresholdButton);
		
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
				Utils.displayMessageDialog("Automatic Review", "Click OK to start automatic review of video processing results.");
				
				FileListAutomaticReviewWindow.this.dispose();
				
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("AutReview Review", this);
						
						CursorUtils.setBusyCursor(IMAGE_GALLERY);
						
						IMAGE_GALLERY.performAutomatedMarkAllButOneAction();
						
						CursorUtils.setDefaultCursor(IMAGE_GALLERY);
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}			
		});
		controlsPanel.add(closeWindowButton);
		
		JTabbedPane settingsTabPane = new JTabbedPane();
		settingsTabPane.addTab("Advanced Settings", controlsPanel);
		
		contentPanel.add(settingsTabPane, BorderLayout.CENTER); 
		
		this.add(contentPanel, BorderLayout.CENTER);
		
		WindowRegistry.getInstance().registerFrame(this, "FileListAutomaticReview");
		
		WindowUtils.setFrameIcon(this);
		this.setTitle("Report Settings");
		this.setPreferredSize(new Dimension(660, 630));
		this.pack();
		this.setVisible(true);
		WindowUtils.center(this);
	}

	private void performResetImageSimilarityAction() {
		VIDEO_FALSE_POSITIVE_REMOVER_BY_REFERENCE_LEGACY.resetCurrentImageSimilarityThreshold();
		imageSimilarityThresholdSlider.setValue((int) VIDEO_FALSE_POSITIVE_REMOVER_BY_REFERENCE_LEGACY.getCurrentImageSimilarityThreshold()); 
	}
	
	private void performResetImageDifferenceAction() {
		VIDEO_FALSE_POSITIVE_REMOVER_BY_REFERENCE_LEGACY.resetCurrentImageDifferenceThreshold();
		imageDifferenceThresholdSlider.setValue((int) VIDEO_FALSE_POSITIVE_REMOVER_BY_REFERENCE_LEGACY.getCurrentImageDifferenceThreshold()); 
	}
	
	private void performResetImageSurfSimilarityAction() {
		VIDEO_FALSE_POSITIVE_REMOVER_BY_REFERENCE_LEGACY.resetCurrentImageSurfThreshold();
		imageDifferenceThresholdSlider.setValue((int) VIDEO_FALSE_POSITIVE_REMOVER_BY_REFERENCE_LEGACY.getCurrentImageSurfSimilarity()); 
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
			else if(slider == imageSurfThresholdSlider) {
				int imageSurfSimilarityValue = imageSurfThresholdSlider.getValue();
				
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
						ThreadUtils.addThreadToHandleList("AutReview Exit", this);
						
						FileListAutomaticReviewWindow.this.dispose();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menu = new JMenu("Settings");
		menuBar.add(menu);
		
		menuItem = new JMenuItem("Reset Image Descriptor Similarity");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("AutReview ResetImSurf", this);
						
						performResetImageSurfSimilarityAction();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Reset Image Difference Threshold");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("AutReview ResetImDiff", this);
						
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
						ThreadUtils.addThreadToHandleList("AutReview ResetImSim", this);
						
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
						ThreadUtils.addThreadToHandleList("AutReview OpenDoc", this);
						
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
						ThreadUtils.addThreadToHandleList("AutReview About", this);
						
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

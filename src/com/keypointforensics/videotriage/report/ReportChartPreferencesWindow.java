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
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import com.keypointforensics.videotriage.chart.EVideoTriageChart;
import com.keypointforensics.videotriage.util.BorderUtils;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.Utils;
import com.keypointforensics.videotriage.util.WebUtils;
import com.keypointforensics.videotriage.util.WindowUtils;
import com.keypointforensics.videotriage.window.WindowRegistry;

public class ReportChartPreferencesWindow extends JFrame implements ItemListener {

	/*
	 * Author: Daniel Puckowski
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3931002670526389551L;

	private final ImageGallery IMAGE_GALLERY;
	private final boolean      HAS_CAPTURES;
	private final boolean      HAS_FACES;
	private final boolean      HAS_LICENSE_PLATES;
	private final boolean      HAS_PEDESTRIANS;
	private final boolean      HAS_CARS;
	private final boolean      HAS_EXPLICIT;
	
	private JCheckBox mTimeSeriesChartCheckBox;
	private JCheckBox mTimeSeriesScatterChartCheckBox;
	private JCheckBox mTimeSeriesFilledAreaChartCheckBox;
	private JCheckBox mTimeSeriesMassChartCheckBox;
	private JCheckBox mTimeSeriesFaceDetectionCheckBox;
	private JCheckBox mTimeSeriesLicensePlateDetectionCheckBox;
	private JCheckBox mTimeSeriesPedestrianDetectionCheckBox;
	private JCheckBox mTimeSeriesCarDetectionCheckBox;
	private JCheckBox mTimeSeriesExplicitDetectionCheckBox;
	private JCheckBox mBlobTrackingScatterCheckBox;
	private JCheckBox mBlobTrackingHeatMapCheckBox;
	private JCheckBox mBlobTrackingScatterBackgroundCheckBox;
	private JCheckBox mHourOfDayBarCheckBox;
	private JCheckBox mHistogramContourBackgroundCheckBox;
	private JCheckBox mDetectionCompositionPieCheckBox;
	
	public ReportChartPreferencesWindow(final ImageGallery imageGallery) {
		IMAGE_GALLERY = imageGallery;
		
		final String caseName = FileUtils.getShortFilename(IMAGE_GALLERY.getCapturePath());
		
		HAS_CAPTURES       = !FileUtils.isDirectoryEmpty(IMAGE_GALLERY.getCapturePath()); //!(FileUtilsLegacy.parseDirectoryRecursiveForAll(IMAGE_GALLERY.getCapturePath()).isEmpty());
		HAS_FACES          = !FileUtils.isDirectoryEmpty(FileUtils.FACES_DIRECTORY + caseName); //!(FileUtilsLegacy.parseDirectoryRecursiveForAll(FileUtils.FACES_DIRECTORY + caseName).isEmpty());
		HAS_LICENSE_PLATES = !FileUtils.isDirectoryEmpty(FileUtils.LICENSE_DIRECTORY + caseName); //!(FileUtilsLegacy.parseDirectoryRecursiveForAll(FileUtils.LICENSE_DIRECTORY + caseName).isEmpty());
		HAS_PEDESTRIANS    = !FileUtils.isDirectoryEmpty(FileUtils.PEDESTRIANS_DIRECTORY + caseName); //!(FileUtilsLegacy.parseDirectoryRecursiveForAll(FileUtils.PEDESTRIANS_DIRECTORY + caseName).isEmpty());
		HAS_CARS           = !FileUtils.isDirectoryEmpty(FileUtils.CARS_DIRECTORY + caseName);
		HAS_EXPLICIT       = !FileUtils.isDirectoryEmpty(FileUtils.EXPLICIT_DIRECTORY + caseName);
		
		buildMenuBar();
		buildFrame();
	}
	
	private void buildFrame() {
		this.setLayout(new BorderLayout());
		
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout());

		JPanel controlsPanel = new JPanel();
		controlsPanel.setLayout(new GridLayout(16, 1));
		controlsPanel.setBorder(BorderUtils.getEmptyBorder());
		
		mTimeSeriesChartCheckBox = new JCheckBox("Create Time Series Chart");
		mTimeSeriesChartCheckBox.setSelected(IMAGE_GALLERY.getReportChartSettings().hasRequestedChartBuilder(EVideoTriageChart.TIME_SERIES_STANDARD));
		mTimeSeriesChartCheckBox.addItemListener(this);
		controlsPanel.add(mTimeSeriesChartCheckBox);
		
		mTimeSeriesScatterChartCheckBox = new JCheckBox("Create Time Series Scatter Chart");
		mTimeSeriesScatterChartCheckBox.setSelected(IMAGE_GALLERY.getReportChartSettings().hasRequestedChartBuilder(EVideoTriageChart.TIME_SERIES_SCATTER));
		mTimeSeriesScatterChartCheckBox.addItemListener(this);
		controlsPanel.add(mTimeSeriesScatterChartCheckBox);
		
		mTimeSeriesFilledAreaChartCheckBox = new JCheckBox("Create Time Series Filled Area Chart");
		mTimeSeriesFilledAreaChartCheckBox.setSelected(IMAGE_GALLERY.getReportChartSettings().hasRequestedChartBuilder(EVideoTriageChart.TIME_SERIES_FILLED_AREA));
		mTimeSeriesFilledAreaChartCheckBox.addItemListener(this);
		controlsPanel.add(mTimeSeriesFilledAreaChartCheckBox);
		
		mTimeSeriesMassChartCheckBox = new JCheckBox("Create Time Series Mass Chart");
		mTimeSeriesMassChartCheckBox.setSelected(IMAGE_GALLERY.getReportChartSettings().hasRequestedChartBuilder(EVideoTriageChart.TIME_SERIES_MASS));
		mTimeSeriesMassChartCheckBox.addItemListener(this);
		controlsPanel.add(mTimeSeriesMassChartCheckBox);
		
		mTimeSeriesFaceDetectionCheckBox = new JCheckBox("Create Time Series Face Detection Chart");
		mTimeSeriesFaceDetectionCheckBox.setSelected(IMAGE_GALLERY.getReportChartSettings().hasRequestedChartBuilder(EVideoTriageChart.TIME_SERIES_FACES));
		mTimeSeriesFaceDetectionCheckBox.addItemListener(this);
		controlsPanel.add(mTimeSeriesFaceDetectionCheckBox);
		
		mTimeSeriesLicensePlateDetectionCheckBox = new JCheckBox("Create Time Series License Plate Detection Chart");
		mTimeSeriesLicensePlateDetectionCheckBox.setSelected(IMAGE_GALLERY.getReportChartSettings().hasRequestedChartBuilder(EVideoTriageChart.TIME_SERIES_LICENSE_PLATES));
		mTimeSeriesLicensePlateDetectionCheckBox.addItemListener(this);
		controlsPanel.add(mTimeSeriesLicensePlateDetectionCheckBox);
		
		mTimeSeriesPedestrianDetectionCheckBox = new JCheckBox("Create Time Series Pedestrian Detection Chart");
		mTimeSeriesPedestrianDetectionCheckBox.setSelected(IMAGE_GALLERY.getReportChartSettings().hasRequestedChartBuilder(EVideoTriageChart.TIME_SERIES_PEDESTRIANS));
		mTimeSeriesPedestrianDetectionCheckBox.addItemListener(this);
		controlsPanel.add(mTimeSeriesPedestrianDetectionCheckBox);
		
		mTimeSeriesCarDetectionCheckBox = new JCheckBox("Create Time Series Car Detection Chart");
		mTimeSeriesCarDetectionCheckBox.setSelected(IMAGE_GALLERY.getReportChartSettings().hasRequestedChartBuilder(EVideoTriageChart.TIME_SERIES_CARS));
		mTimeSeriesCarDetectionCheckBox.addItemListener(this);
		controlsPanel.add(mTimeSeriesCarDetectionCheckBox);
		
		mTimeSeriesExplicitDetectionCheckBox = new JCheckBox("Create Time Series Explicit Image Detection Chart");
		mTimeSeriesExplicitDetectionCheckBox.setSelected(IMAGE_GALLERY.getReportChartSettings().hasRequestedChartBuilder(EVideoTriageChart.TIME_SERIES_EXPLICIT));
		mTimeSeriesExplicitDetectionCheckBox.addItemListener(this);
		controlsPanel.add(mTimeSeriesExplicitDetectionCheckBox);
		
		if(HAS_FACES == false) {
			mTimeSeriesFaceDetectionCheckBox.setEnabled(false);
		}
		
		if(HAS_LICENSE_PLATES == false) {
			mTimeSeriesLicensePlateDetectionCheckBox.setEnabled(false);
		}
		
		if(HAS_PEDESTRIANS == false) {
			mTimeSeriesPedestrianDetectionCheckBox.setEnabled(false);
		}
		
		if(HAS_CARS == false) {
			mTimeSeriesCarDetectionCheckBox.setEnabled(false);
		}
	
		if(HAS_EXPLICIT == false) {
			mTimeSeriesExplicitDetectionCheckBox.setEnabled(false);
		}
		
		mBlobTrackingScatterCheckBox = new JCheckBox("Create Object Tracking Scatter Chart");
		mBlobTrackingScatterCheckBox.setSelected(IMAGE_GALLERY.getReportChartSettings().hasRequestedChartBuilder(EVideoTriageChart.BLOB_TRACKING_SCATTER_STANDARD));
		//mBlobTrackingScatterCheckBox.addItemListener(this);
		controlsPanel.add(mBlobTrackingScatterCheckBox);
		
		mBlobTrackingScatterBackgroundCheckBox = new JCheckBox("Create Object Tracking Scatter Background Chart");
		mBlobTrackingScatterBackgroundCheckBox.setSelected(IMAGE_GALLERY.getReportChartSettings().hasRequestedChartBuilder(EVideoTriageChart.BLOB_TRACKING_SCATTER_BACKGROUND));
		//mBlobTrackingScatterBackgroundCheckBox.addItemListener(this);
		controlsPanel.add(mBlobTrackingScatterBackgroundCheckBox);
		
		mBlobTrackingHeatMapCheckBox = new JCheckBox("Create Object Tracking Heat Map Chart");
		mBlobTrackingHeatMapCheckBox.setSelected(IMAGE_GALLERY.getReportChartSettings().hasRequestedChartBuilder(EVideoTriageChart.BLOB_TRACKING_HEAT_MAP_STANDARD));
		//mBlobTrackingHeatMapCheckBox.addItemListener(this);
		controlsPanel.add(mBlobTrackingHeatMapCheckBox);
		
		mHourOfDayBarCheckBox = new JCheckBox("Create Hour of Day Bar Chart");
		mHourOfDayBarCheckBox.setSelected(IMAGE_GALLERY.getReportChartSettings().hasRequestedChartBuilder(EVideoTriageChart.BAR_CHART_HOUR_OF_DAY_STANDARD));
		mHourOfDayBarCheckBox.addItemListener(this);
		controlsPanel.add(mHourOfDayBarCheckBox);
		
		mHistogramContourBackgroundCheckBox = new JCheckBox("Create Histogram Contour Background Chart");
		mHistogramContourBackgroundCheckBox.setSelected(IMAGE_GALLERY.getReportChartSettings().hasRequestedChartBuilder(EVideoTriageChart.HISTOGRAM_CONTOUR_BACKGROUND));
		//mHistogramContourBackgroundCheckBox.addItemListener(this);
		controlsPanel.add(mHistogramContourBackgroundCheckBox);
		
		mDetectionCompositionPieCheckBox = new JCheckBox("Create Detection Composition Pie Chart");
		mDetectionCompositionPieCheckBox.setSelected(IMAGE_GALLERY.getReportChartSettings().hasRequestedChartBuilder(EVideoTriageChart.DETECTION_COMPOSITION_PIE_STANDARD));
		mDetectionCompositionPieCheckBox.addItemListener(this);
		controlsPanel.add(mDetectionCompositionPieCheckBox);
		
		if(HAS_FACES == true || HAS_LICENSE_PLATES == true || HAS_PEDESTRIANS == true || HAS_CARS == true || HAS_EXPLICIT == true) {
			mDetectionCompositionPieCheckBox.setEnabled(true);
		}
		else {
			mDetectionCompositionPieCheckBox.setEnabled(false);
		}
		
		if(HAS_CAPTURES == false) {
			mTimeSeriesChartCheckBox.setEnabled(false);
			mTimeSeriesScatterChartCheckBox.setEnabled(false);
			mTimeSeriesFilledAreaChartCheckBox.setEnabled(false);
			mTimeSeriesMassChartCheckBox.setEnabled(false);
			mHourOfDayBarCheckBox.setEnabled(false);
			mDetectionCompositionPieCheckBox.setEnabled(false);
		}
		
		final String coordinateFilename = IMAGE_GALLERY.getBlobContextList().getCoordinateFilename();
		final File checkCoordinateFile = new File(coordinateFilename);
		
		if(checkCoordinateFile.exists() == false) {
			mBlobTrackingScatterCheckBox.setEnabled(false);
			mBlobTrackingHeatMapCheckBox.setEnabled(false);
			mBlobTrackingScatterBackgroundCheckBox.setEnabled(false);
			mHistogramContourBackgroundCheckBox.setEnabled(false);
		} else {
			mBlobTrackingScatterCheckBox.addItemListener(this);
			mBlobTrackingHeatMapCheckBox.addItemListener(this);
			mBlobTrackingScatterBackgroundCheckBox.addItemListener(this);
			mHistogramContourBackgroundCheckBox.addItemListener(this);
		}
		
		JButton closeWindowButton = new JButton("Done");
		closeWindowButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				ReportChartPreferencesWindow.this.dispose();
			}			
		});
		controlsPanel.add(closeWindowButton);
		
		JScrollPane controlsScrollPane = new JScrollPane(controlsPanel);
		
		JTabbedPane settingsTabPane = new JTabbedPane();
		settingsTabPane.addTab("Chart Settings", controlsScrollPane);
		
		contentPanel.add(settingsTabPane, BorderLayout.CENTER); 
		
		this.add(contentPanel, BorderLayout.CENTER);
		
		WindowUtils.setFrameIcon(this);
		this.setTitle("Report Settings");
		this.setPreferredSize(new Dimension(660, 690));
		this.pack();
		this.setVisible(true);
		WindowUtils.center(this);
		
		WindowRegistry.getInstance().registerFrame(this, "ReportChartPreferences");
	}
	
	@Override
	public void itemStateChanged(ItemEvent event) {
		if (event.getSource() instanceof JCheckBox) {
			JCheckBox checkBox = (JCheckBox) event.getSource();

			if (checkBox == mTimeSeriesChartCheckBox) {
				final boolean createTimeSeriesChart = mTimeSeriesChartCheckBox.isSelected();
				
				if(createTimeSeriesChart == true) {
					IMAGE_GALLERY.getReportChartSettings().addChartRequest(EVideoTriageChart.TIME_SERIES_STANDARD);
				}
				else {
					IMAGE_GALLERY.getReportChartSettings().removeChartRequest(EVideoTriageChart.TIME_SERIES_STANDARD);
				}
			}
			else if (checkBox == mTimeSeriesScatterChartCheckBox) {
				final boolean createTimeSeriesChart = mTimeSeriesScatterChartCheckBox.isSelected();
				
				if(createTimeSeriesChart == true) {
					IMAGE_GALLERY.getReportChartSettings().addChartRequest(EVideoTriageChart.TIME_SERIES_SCATTER);
				}
				else {
					IMAGE_GALLERY.getReportChartSettings().removeChartRequest(EVideoTriageChart.TIME_SERIES_SCATTER);
				}
			}
			else if(checkBox == mTimeSeriesFilledAreaChartCheckBox) {
				final boolean createTimeSeriesChart = mTimeSeriesFilledAreaChartCheckBox.isSelected();
				
				if(createTimeSeriesChart == true) {
					IMAGE_GALLERY.getReportChartSettings().addChartRequest(EVideoTriageChart.TIME_SERIES_FILLED_AREA);
				}
				else {
					IMAGE_GALLERY.getReportChartSettings().removeChartRequest(EVideoTriageChart.TIME_SERIES_FILLED_AREA);
				}
			}
			else if(checkBox == mTimeSeriesMassChartCheckBox) {
				final boolean createTimeSeriesChart = mTimeSeriesMassChartCheckBox.isSelected();
				
				if(createTimeSeriesChart == true) {
					IMAGE_GALLERY.getReportChartSettings().addChartRequest(EVideoTriageChart.TIME_SERIES_MASS);
				}
				else {
					IMAGE_GALLERY.getReportChartSettings().removeChartRequest(EVideoTriageChart.TIME_SERIES_MASS);
				}
			}
			else if(checkBox == mTimeSeriesFaceDetectionCheckBox) {
				final boolean createTimeSeriesFaceDetectionChart = mTimeSeriesFaceDetectionCheckBox.isSelected();
				
				if(createTimeSeriesFaceDetectionChart == true) {
					IMAGE_GALLERY.getReportChartSettings().addChartRequest(EVideoTriageChart.TIME_SERIES_FACES);
				}
				else {
					IMAGE_GALLERY.getReportChartSettings().removeChartRequest(EVideoTriageChart.TIME_SERIES_FACES);
				}
			}
			else if(checkBox == mTimeSeriesLicensePlateDetectionCheckBox) {
				final boolean createTimeSeriesLicensePlateDetectionChart = mTimeSeriesLicensePlateDetectionCheckBox.isSelected();
				
				if(createTimeSeriesLicensePlateDetectionChart == true) {
					IMAGE_GALLERY.getReportChartSettings().addChartRequest(EVideoTriageChart.TIME_SERIES_LICENSE_PLATES);
				}
				else {
					IMAGE_GALLERY.getReportChartSettings().removeChartRequest(EVideoTriageChart.TIME_SERIES_LICENSE_PLATES);
				}
			}
			else if(checkBox == mTimeSeriesPedestrianDetectionCheckBox) {
				final boolean createTimeSeriesPedestrianDetectionChart = mTimeSeriesPedestrianDetectionCheckBox.isSelected();
				
				if(createTimeSeriesPedestrianDetectionChart == true) {
					IMAGE_GALLERY.getReportChartSettings().addChartRequest(EVideoTriageChart.TIME_SERIES_PEDESTRIANS);
				}
				else {
					IMAGE_GALLERY.getReportChartSettings().removeChartRequest(EVideoTriageChart.TIME_SERIES_PEDESTRIANS);
				}
			}
			else if(checkBox == mTimeSeriesCarDetectionCheckBox) {
				final boolean createTimeSeriesCarDetectionChart = mTimeSeriesCarDetectionCheckBox.isSelected();
				
				if(createTimeSeriesCarDetectionChart == true) {
					IMAGE_GALLERY.getReportChartSettings().addChartRequest(EVideoTriageChart.TIME_SERIES_CARS);
				}
				else {
					IMAGE_GALLERY.getReportChartSettings().removeChartRequest(EVideoTriageChart.TIME_SERIES_CARS);
				}
			}
			else if(checkBox == mTimeSeriesExplicitDetectionCheckBox) {
				final boolean createTimeSeriesExplicitDetectionChart = mTimeSeriesExplicitDetectionCheckBox.isSelected();
				
				if(createTimeSeriesExplicitDetectionChart == true) {
					IMAGE_GALLERY.getReportChartSettings().addChartRequest(EVideoTriageChart.TIME_SERIES_EXPLICIT);
				}
				else {
					IMAGE_GALLERY.getReportChartSettings().removeChartRequest(EVideoTriageChart.TIME_SERIES_EXPLICIT);
				}
			}
			else if(checkBox == mBlobTrackingScatterCheckBox) {
				final boolean createBlobTrackingScatterChart = mBlobTrackingScatterCheckBox.isSelected();

				if(createBlobTrackingScatterChart == true) {
					IMAGE_GALLERY.getReportChartSettings().addChartRequest(EVideoTriageChart.BLOB_TRACKING_SCATTER_STANDARD);
				}
				else {
					IMAGE_GALLERY.getReportChartSettings().removeChartRequest(EVideoTriageChart.BLOB_TRACKING_SCATTER_STANDARD);
				}
			}
			else if(checkBox == mBlobTrackingScatterBackgroundCheckBox) {
				final boolean createBlobTrackingScatterBackgroundChart = mBlobTrackingScatterBackgroundCheckBox.isSelected();

				if(createBlobTrackingScatterBackgroundChart == true) {
					IMAGE_GALLERY.getReportChartSettings().addChartRequest(EVideoTriageChart.BLOB_TRACKING_SCATTER_BACKGROUND);
				}
				else {
					IMAGE_GALLERY.getReportChartSettings().removeChartRequest(EVideoTriageChart.BLOB_TRACKING_SCATTER_BACKGROUND);
				}
			}
			else if(checkBox == mBlobTrackingHeatMapCheckBox) {
				final boolean createBlobTrackingHeatMapChart = mBlobTrackingHeatMapCheckBox.isSelected();

				if(createBlobTrackingHeatMapChart == true) {
					IMAGE_GALLERY.getReportChartSettings().addChartRequest(EVideoTriageChart.BLOB_TRACKING_HEAT_MAP_STANDARD);
				}
				else {
					IMAGE_GALLERY.getReportChartSettings().removeChartRequest(EVideoTriageChart.BLOB_TRACKING_HEAT_MAP_STANDARD);
				}
			}
			else if(checkBox == mHourOfDayBarCheckBox) {
				final boolean createBlobTrackingHeatMapChart = mHourOfDayBarCheckBox.isSelected();

				if(createBlobTrackingHeatMapChart == true) {
					IMAGE_GALLERY.getReportChartSettings().addChartRequest(EVideoTriageChart.BAR_CHART_HOUR_OF_DAY_STANDARD);
				}
				else {
					IMAGE_GALLERY.getReportChartSettings().removeChartRequest(EVideoTriageChart.BAR_CHART_HOUR_OF_DAY_STANDARD);
				}
			}
			else if(checkBox == mHistogramContourBackgroundCheckBox) {
				final boolean createHistogramContourChart = mHistogramContourBackgroundCheckBox.isSelected();

				if(createHistogramContourChart == true) {
					IMAGE_GALLERY.getReportChartSettings().addChartRequest(EVideoTriageChart.HISTOGRAM_CONTOUR_BACKGROUND);
				}
				else {
					IMAGE_GALLERY.getReportChartSettings().removeChartRequest(EVideoTriageChart.HISTOGRAM_CONTOUR_BACKGROUND);
				}
			}
			else if(checkBox == mDetectionCompositionPieCheckBox) {
				final boolean createDetectionCompositionPieChart = mDetectionCompositionPieCheckBox.isSelected();

				if(createDetectionCompositionPieChart == true) {
					IMAGE_GALLERY.getReportChartSettings().addChartRequest(EVideoTriageChart.DETECTION_COMPOSITION_PIE_STANDARD);
				}
				else {
					IMAGE_GALLERY.getReportChartSettings().removeChartRequest(EVideoTriageChart.DETECTION_COMPOSITION_PIE_STANDARD);
				}
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
						ThreadUtils.addThreadToHandleList("ReportChartPref Exit", this);
						
						ReportChartPreferencesWindow.this.dispose();
						
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
						ThreadUtils.addThreadToHandleList("ReportChartPref OpenDoc", this);
						
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
						ThreadUtils.addThreadToHandleList("ReportChartPref About", this);
						
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

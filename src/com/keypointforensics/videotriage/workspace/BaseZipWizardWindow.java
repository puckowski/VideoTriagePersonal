package com.keypointforensics.videotriage.workspace;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.keypointforensics.videotriage.legacy.FileUtilsLegacy;
import com.keypointforensics.videotriage.progress.ProgressBundle;
import com.keypointforensics.videotriage.report.ZipFileDropList;
import com.keypointforensics.videotriage.util.BorderUtils;
import com.keypointforensics.videotriage.util.CursorUtils;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ProgressUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.Utils;
import com.keypointforensics.videotriage.util.WindowUtils;
import com.keypointforensics.videotriage.util.ZipUtils;
import com.keypointforensics.videotriage.window.WindowRegistry;

public abstract class BaseZipWizardWindow extends JFrame implements ActionListener, ChangeListener {

	/*
	 * Author: Daniel Puckowski
	 */
	
	private static final long serialVersionUID = 4271388619852805707L;
	
	private static final int CONTROL_GRID_LAYOUT_ROWS    = 14;
	private static final int CONTROL_GRID_LAYOUT_COLUMNS = 1;
	
	protected final String BASE_ZIP_PATH;
	protected final String ZIP_DATA_TYPE;
	
	protected ZipFileDropList mFileDropList;
	protected JButton mStartButton;
	protected JSlider mZipCompressionSlider;
	
	public BaseZipWizardWindow(final String baseZipPath, final String zipDataType) {		
		BASE_ZIP_PATH = baseZipPath;
		ZIP_DATA_TYPE = zipDataType;
		
		buildFrame();
		addDataFoldersAction();
		
		WindowRegistry.getInstance().registerFrame(this, "BaseZipWizard");
	}
	
	protected abstract void buildMenuBar();
	protected abstract void addDataFoldersAction();
	
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
		settingsTabPane.addTab("Zip " + ZIP_DATA_TYPE + " Controls", contentScrollPane);
				
		mFileDropList = new ZipFileDropList();
		mFileDropList.setPreferredSize(new Dimension(600, 300));
		 
		JLabel searchSimilarityLevelLabel = new JLabel("Zip Compression Level");
		contentPanel.add(searchSimilarityLevelLabel);
		
		int sliderMin = 1;
		int sliderMax = 100;
		int sliderInit = (ZipUtils.COMPRESSION_LEVEL_DEFAULT + 1) * 10;
		mZipCompressionSlider = new JSlider(JSlider.HORIZONTAL, sliderMin,
				sliderMax, sliderInit);
		mZipCompressionSlider.addChangeListener(this);
		try {
			mZipCompressionSlider.removeMouseWheelListener(mZipCompressionSlider.getMouseWheelListeners()[0]);
		} catch(Exception exception) {	

		}
		mZipCompressionSlider.setMajorTickSpacing(9);
		mZipCompressionSlider.setPaintTicks(true);
		mZipCompressionSlider.setPaintLabels(true);
		contentPanel.add(mZipCompressionSlider);
		
		mStartButton = new JButton("Export " + ZIP_DATA_TYPE);
		mStartButton.addActionListener(this);
		contentPanel.add(mStartButton);
		
		JSplitPane leftSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, settingsTabPane, mFileDropList);
		
		this.add(leftSplitPane, BorderLayout.CENTER);
		
		this.setTitle("Export " + ZIP_DATA_TYPE + " Wizard");
		this.pack();
		WindowUtils.setFrameIcon(this);
		WindowUtils.maximize(this);
		this.setVisible(true);
	}
	
	private void performStartAction() {
		final String selectedReportAbsolutePath = mFileDropList.getSelectedReportAbsolutePath();
		
		if(selectedReportAbsolutePath == null) {
			Utils.displayMessageDialog("No Selection", "Please select a " + ZIP_DATA_TYPE.toLowerCase() + " from the list.");
			
			CursorUtils.setDefaultCursor(this);
			
			return;
		}
		
		final ArrayList<String> filesToZip = (ArrayList<String>) FileUtilsLegacy.parseDirectoryRecursiveForAll(selectedReportAbsolutePath);
		final ProgressBundle zipProgressBundle = ProgressUtils.getProgressBundle("Zipping " + ZIP_DATA_TYPE, filesToZip.size());
		final String outputReportZipAbsolutePath = FileUtils.EXPORTS_DIRECTORY + FileUtils.getShortFilename(selectedReportAbsolutePath) + "_Export_" + Utils.getTimeStampForExport() + ".zip";
		
		File checkIfExportedAlready = new File(outputReportZipAbsolutePath);
		
		if(checkIfExportedAlready.exists() == true) {
			checkIfExportedAlready.delete();
		}
		
		checkIfExportedAlready = null;
		
		CursorUtils.setBusyCursor(this);
		
		int compressionLevel = (int) Math.round((double) mZipCompressionSlider.getValue() / 10.0) - 1;
		
		if(compressionLevel < 0) {
			compressionLevel = 0;
		}
		
		try {
			ZipUtils.zipFiles(filesToZip, BASE_ZIP_PATH, outputReportZipAbsolutePath, zipProgressBundle, compressionLevel);
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
		
		CursorUtils.setDefaultCursor(this);
		
		Utils.displayMessageDialog(ZIP_DATA_TYPE + " Export", "Finished zipping " + ZIP_DATA_TYPE + " Location exported:\n" +
			outputReportZipAbsolutePath);
	}
	
	@Override
	public void stateChanged(ChangeEvent changeEvent) {
		//Object changeSource = changeEvent.getSource();	
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() instanceof JButton) {
			JButton button = (JButton) event.getSource();

			if(button == mStartButton) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("BaseZipWizWindow PerformStart", this);
							
						performStartAction();
							
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
		}
	}
}

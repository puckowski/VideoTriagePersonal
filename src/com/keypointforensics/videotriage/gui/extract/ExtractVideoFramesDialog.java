package com.keypointforensics.videotriage.gui.extract;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.keypointforensics.videotriage.gui.main.GuiMain;
import com.keypointforensics.videotriage.legacy.ExtractVideoFrameBlob;
import com.keypointforensics.videotriage.legacy.UtilsLegacy;
import com.keypointforensics.videotriage.legacy.WindowsVideoFrameExtractorLegacy;
import com.keypointforensics.videotriage.progress.ProgressBundle;
import com.keypointforensics.videotriage.staticparams.LocalFileRuntimeParams;
import com.keypointforensics.videotriage.util.BorderUtils;
import com.keypointforensics.videotriage.util.CursorUtils;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ProgressUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.Utils;
import com.keypointforensics.videotriage.util.WebUtils;
import com.keypointforensics.videotriage.util.WindowUtils;
import com.keypointforensics.videotriage.window.WindowRegistry;

public class ExtractVideoFramesDialog extends JFrame implements ChangeListener, ActionListener {

	/*
	 * Author: Daniel Puckowski
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5468066048459750354L;
	
	private static final int CONTROL_GRID_LAYOUT_ROWS     = 9;
	private static final int CONTROL_GRID_LAYOUT_COLUMNS  = 1;
	
	private final GuiMain GUI_MAIN;
	private final String VIDEO_ABSOLUTE_PATH;
	
	private JSlider mFramesPerSecondSlider;
	private JButton mDoneButton;
		
	public ExtractVideoFramesDialog(final GuiMain guiMain, final String videoAbsolutePath) {
		GUI_MAIN = guiMain;
		VIDEO_ABSOLUTE_PATH = videoAbsolutePath;
				
		//buildFrame();
		
		WindowRegistry.getInstance().registerFrame(this, "ExtractVideoFramesDialog");
	}
	
	public void build() {		
		buildMenuBar();
		
		this.setLayout(new BorderLayout());
		
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new GridLayout(CONTROL_GRID_LAYOUT_ROWS, CONTROL_GRID_LAYOUT_COLUMNS));
		
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
		
		mDoneButton = new JButton("Extract Frames");
		mDoneButton.addActionListener(this);
		
		contentPanel.add(framesPerSecondLabel);
		contentPanel.add(mFramesPerSecondSlider);
		contentPanel.add(mDoneButton);
		
		JScrollPane optionsScrollPane = new JScrollPane(contentPanel);
		WindowUtils.setScrollBarIncrement(optionsScrollPane);
		optionsScrollPane.setPreferredSize(new Dimension(320, 420));
		optionsScrollPane.setBorder(BorderUtils.getEmptyBorder());
		
		this.add(optionsScrollPane, BorderLayout.CENTER);
		//this.add(new SimpleDiskUsageChartPanel(), BorderLayout.SOUTH);
				
		this.setTitle("Extract Video Frames");
		this.pack();
		WindowUtils.setFrameIcon(this);
		this.setVisible(true);
		WindowUtils.center(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() instanceof JButton) {
			JButton button = (JButton) event.getSource();

			if(button == mDoneButton) {
				performExtractVideoFramesAction();
			}
		}
	}
	
	private void performExtractVideoFramesAction() {
		new Thread() {
			@Override
			public void run() {
				ThreadUtils.addThreadToHandleList("ExtractVideoFramesDialog ExtractVideoFrames", this);
				
				ExtractVideoFramesDialog.this.dispose();
				
				CursorUtils.setBusyCursor(GUI_MAIN);
				
				ProgressBundle extractProgressBundle = ProgressUtils.getProgressBundle("Estimating Frame Count...");
				extractProgressBundle.progressBar.setIndeterminate(true);
				
				final int framesPerSecondTarget = mFramesPerSecondSlider.getValue();
				
				//final int videoFrameCount = WindowsVideoFrameExtractorLegacy.getVideoFrameCount(VIDEO_ABSOLUTE_PATH);
				final int videoDurationInSeconds = (int) Math.floor(WindowsVideoFrameExtractorLegacy.getVideoDurationInSeconds(VIDEO_ABSOLUTE_PATH));
				final int generalExtractionTarget = framesPerSecondTarget * videoDurationInSeconds;

				ExtractVideoFrameBlob extractVideoFrameBlob = WindowsVideoFrameExtractorLegacy.extractVideoFramesWithoutGlobalFrameTarget(VIDEO_ABSOLUTE_PATH, true, framesPerSecondTarget, 7);
				
				File extractionDirectory = new File(extractVideoFrameBlob.getAbsoluteExtractPath());
				
				extractProgressBundle.frame.setTitle("Extracting Video Frames...");
				
				extractProgressBundle.progressBar.setIndeterminate(false);
				extractProgressBundle.progressBar.setValue(0);
				extractProgressBundle.progressBar.setMaximum(generalExtractionTarget);

				ExecutorService executor = Executors.newCachedThreadPool();
				Callable<Object> task = new Callable<Object>() {
				   public Object call() {
					   int extractionProgress = extractionDirectory.list().length;
					   
					   while(extractionProgress < generalExtractionTarget) {
						   extractProgressBundle.progressBar.setValue(extractionProgress);
						   extractProgressBundle.progressBar.repaint();
							
						   extractionProgress = extractionDirectory.list().length;
					   }
					   
					   return 1;
				   }
				};
				
				Future<Object> future = executor.submit(task);
				try {
				   Object result = future.get(10, TimeUnit.MINUTES); 
				} catch (TimeoutException timeoutException) {
					//timeoutException.printStackTrace();
					
					Utils.displayMessageDialog("Notice", "Terminated video frame extraction for video:\n" + 
						VIDEO_ABSOLUTE_PATH + "\n" + 
						"Frame extraction was unexpectedly halted. Operation may not have completed successfully.");
				} catch (InterruptedException interruptedException) {
					//interruptedException.printStackTrace();
					
					Utils.displayMessageDialog("Notice", "Terminated video frame extraction for video:\n" + 
						VIDEO_ABSOLUTE_PATH + "\n" + 
						"Frame extraction was unexpectedly halted. Operation may not have completed successfully.");
				} catch (ExecutionException executionException) {
					//executionException.printStackTrace();
					
					Utils.displayMessageDialog("Notice", "Terminated video frame extraction for video:\n" + 
						VIDEO_ABSOLUTE_PATH + "\n" + 
						"Frame extraction was unexpectedly halted. Operation may not have completed successfully.");
				} finally {
				   future.cancel(true); 
				}
				
				extractProgressBundle.progressBar.setValue(extractProgressBundle.progressBar.getValue() + 1);
				extractProgressBundle.progressBar.repaint();
				extractProgressBundle.frame.dispose();
				
				CursorUtils.setDefaultCursor(GUI_MAIN);
				
				final int confirmOpenDirectoryChoice = UtilsLegacy.displayConfirmDialog("Extraction Complete", "Open extracted video directory?");
				
				if(confirmOpenDirectoryChoice == JOptionPane.OK_OPTION) {			
					new Thread() {
						@Override
						public void run() {	
							ThreadUtils.addThreadToHandleList("ExtractVideoFramesDialog OpenExtractionFolder", this);
							
							final File file = new File(FileUtils.EXTRACTS_DIRECTORY);
							final Desktop desktop = Desktop.getDesktop();
							
							try {
								desktop.browse(file.toURI());
							} catch (IOException ioException) {

							}
							
							ThreadUtils.removeThreadFromHandleList(this);
						}
					}.start();
				}
				
				ThreadUtils.removeThreadFromHandleList(this);
			}
		}.start();
	}
	
	@Override
	public void stateChanged(ChangeEvent changeEvent) {
		JSlider slider = (JSlider) changeEvent.getSource();

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
						ThreadUtils.addThreadToHandleList("ExtractVideoFramesDialog FileExit", this);
						
						ExtractVideoFramesDialog.this.dispose();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menu = new JMenu("File");
		menuBar.add(menu);
		
		menuItem = new JMenuItem("Extract Frames");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ExtractVideoFramesDialog ExtractVideoFrames", this);
						
						performExtractVideoFramesAction();
						
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
						ThreadUtils.addThreadToHandleList("ExtractVideoFramesDialog OpenDoc", this);
						
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
						ThreadUtils.addThreadToHandleList("ExtractVideoFramesDialog About", this);
						
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

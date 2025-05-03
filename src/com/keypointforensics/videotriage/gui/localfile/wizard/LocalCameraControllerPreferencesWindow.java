package com.keypointforensics.videotriage.gui.localfile.wizard;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.keypointforensics.videotriage.gui.controller.local.LocalCameraController;
import com.keypointforensics.videotriage.gui.main.controller.CameraControllerPreferencesBundle;
import com.keypointforensics.videotriage.legacy.UtilsLegacy;
import com.keypointforensics.videotriage.staticparams.LocalFileRuntimeParams;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.Utils;
import com.keypointforensics.videotriage.util.WebUtils;
import com.keypointforensics.videotriage.window.WindowRegistry;

public class LocalCameraControllerPreferencesWindow extends CameraControllerPreferencesWindow {

	/*
	 * Author: Daniel Puckowski
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6686474091459267131L;

	private final LocalFileWizardWindow LOCAL_FILE_WIZARD_WINDOW;
	private final LocalCameraController LOCAL_CAMERA_CONTROLLER;
		
	public LocalCameraControllerPreferencesWindow(final String windowTitle, final LocalFileWizardWindow localFileWizardWindow) {
		super(windowTitle);
		
		LOCAL_FILE_WIZARD_WINDOW = localFileWizardWindow;
		
		buildMenuBar();
		
		LOCAL_CAMERA_CONTROLLER = new LocalCameraController(null, null, null, null);
		JScrollPane leftScrollPane = LOCAL_CAMERA_CONTROLLER.createLeftScrollPane(10);
		getContentPanel().add(leftScrollPane, BorderLayout.CENTER);
		
		JButton savePreferencesButton = new JButton("Save Preferences");
		savePreferencesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("LocCamPrefWindow Save", this);
						
						performSavePreferencesAction();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
		});
		
		JButton exitPreferencesButton = new JButton("Exit");
		exitPreferencesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("LocCamPrefWindow Exit", this);
						
						final int confirmResult = UtilsLegacy.displayConfirmDialog("Advanced Preferences", "Leave without saving?");
						
						if(confirmResult == JOptionPane.YES_OPTION) {
							LocalCameraControllerPreferencesWindow.this.dispose();
						}
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
		});
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(savePreferencesButton);
		buttonPanel.add(exitPreferencesButton);
		
		getContentPanel().add(buttonPanel, BorderLayout.SOUTH);
		
		//WindowRegistry.getInstance().registerFrame(this, "LocalCameraControllerPreferences");
	}
	
	public LocalCameraController getLocalCameraController() {
		return LOCAL_CAMERA_CONTROLLER;
	}
	
	@Override
	protected void display() {
		LOCAL_CAMERA_CONTROLLER.formatLeftScrollPaneForPreferences();
		
		super.display();
	}
	
	private void performSavePreferencesAction() {
		LocalFileRuntimeParams.setCameraControllerPreferencesBundle(LOCAL_CAMERA_CONTROLLER.getAllParams()); 
		LOCAL_FILE_WIZARD_WINDOW.setFramesPerSecondSliderState(LOCAL_CAMERA_CONTROLLER.isExhaustiveSearchEnabled());
		
		LocalCameraControllerPreferencesWindow.this.dispose();
	}
	
	private void buildMenuBar() {
		JMenuBar menuBar = super.getPreferencesMenuBar();
		
		menuBar.removeAll();
		
		JMenu menu = new JMenu("File");
		menuBar.add(menu);
		
		JMenuItem menuItem = new JMenuItem("Exit");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("CamPrefWindow Exit", this);
						
						LocalCameraControllerPreferencesWindow.this.dispose();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menu = new JMenu("Settings");
		menuBar.add(menu);
		
		menuItem = new JMenuItem("Save Preferences");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("LocCamPrefWindow Save", this);
						
						performSavePreferencesAction();
						
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
						ThreadUtils.addThreadToHandleList("LocCamPrefWindow OpenDoc", this);
						
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
						ThreadUtils.addThreadToHandleList("LocCamPrefWindow About", this);
						
						Utils.displayMessageDialog("About", Utils.SOFTWARE_NAME + " \nVersion: " + Utils.SOFTWARE_VERSION + "\n© " + Utils.AUTHOR_NAME);
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
	}
}

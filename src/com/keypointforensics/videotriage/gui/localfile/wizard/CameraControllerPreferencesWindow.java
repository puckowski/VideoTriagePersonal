package com.keypointforensics.videotriage.gui.localfile.wizard;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.Utils;
import com.keypointforensics.videotriage.util.WebUtils;
import com.keypointforensics.videotriage.util.WindowUtils;
import com.keypointforensics.videotriage.window.WindowRegistry;

public class CameraControllerPreferencesWindow extends JFrame {

	/*
	 * Author: Daniel Puckowski
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7773797718340907366L;

	private JPanel mContentPanel;
	private JMenuBar mMenuBar;
	
	public CameraControllerPreferencesWindow(final String windowTitle) {
		buildMenuBar();
		
		this.setLayout(new BorderLayout());
		
		mContentPanel = new JPanel();
		mContentPanel.setLayout(new BorderLayout());
		mContentPanel.setPreferredSize(new Dimension(400, 600));
		
		this.add(mContentPanel, BorderLayout.CENTER);
		
		WindowUtils.setFrameIcon(this);
		this.setTitle(windowTitle);
		this.pack();
		
		WindowRegistry.getInstance().registerFrame(this, "CameraControllerPreferences");
	}
	
	protected void display() {
		this.setVisible(true);
		WindowUtils.center(this);
	}
	
	protected JPanel getContentPanel() {
		return mContentPanel;
	}
	
	protected JMenuBar getPreferencesMenuBar() {
		return mMenuBar;
	}
	
	private void buildMenuBar() {		
		mMenuBar = new JMenuBar();
		JMenu menu;

		mMenuBar = new JMenuBar();

		menu = new JMenu("File");
		mMenuBar.add(menu);
		
		JMenuItem menuItem = new JMenuItem("Exit");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("CamPrefWindow Exit", this);
						
						CameraControllerPreferencesWindow.this.dispose();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menu = new JMenu("Help");
		mMenuBar.add(menu);
		
		menuItem = new JMenuItem("Open Documentation");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("CamPrefWindow OpenDoc", this);
						
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
						ThreadUtils.addThreadToHandleList("CamPrefWindow About", this);
						
						Utils.displayMessageDialog("About", Utils.SOFTWARE_NAME + " \nVersion: " + Utils.SOFTWARE_VERSION + "\n© " + Utils.AUTHOR_NAME);
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		this.setJMenuBar(mMenuBar);
	}
}

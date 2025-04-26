package com.keypointforensics.videotriage.gui.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Normalizer;
import java.text.Normalizer.Form;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.keypointforensics.videotriage.legacy.FileUtilsLegacy;
import com.keypointforensics.videotriage.legacy.UtilsLegacy;
import com.keypointforensics.videotriage.util.BorderUtils;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.Utils;
import com.keypointforensics.videotriage.util.WebUtils;
import com.keypointforensics.videotriage.util.WindowUtils;

public class CreateNewCaseDialog extends JFrame {

	/*
	 * Author: Daniel Puckowski
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4836579500085072546L;

	public CreateNewCaseDialog() {
		buildMenuBar();
		
		this.setLayout(new BorderLayout());
		
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout());

		JPanel controlsPanel = new JPanel();
		controlsPanel.setLayout(new GridLayout(5, 1));
		
		JLabel caseNameLabel = new JLabel("Case Name:");
		controlsPanel.add(caseNameLabel);
		
		JTextField caseNameField = new JTextField();
		controlsPanel.add(caseNameField);
		
		JCheckBox localProcessorButton = new JCheckBox("Local Processor");
		JCheckBox remoteProcessorButton = new JCheckBox("Remote Processor");
		
		localProcessorButton.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent changeEvent) {
				if(((JCheckBox) changeEvent.getSource()).isSelected() == true) {
					remoteProcessorButton.setEnabled(false);
				}
				else {
					remoteProcessorButton.setEnabled(true);
				}
			}		
		});
		controlsPanel.add(localProcessorButton);
		
		remoteProcessorButton.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent changeEvent) {
				if(((JCheckBox) changeEvent.getSource()).isSelected() == true) {
					localProcessorButton.setEnabled(false);
				}
				else {
					localProcessorButton.setEnabled(true);
				}
			}		
		});
		controlsPanel.add(remoteProcessorButton);
		
		JButton createCaseButton = new JButton("Create Case");
		createCaseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				String normalized = Normalizer.normalize(caseNameField.getText(), Form.NFD);
				String caseDatabaseName = FileUtils.DATABASE_DIRECTORY + normalized.replaceAll("[^A-Za-z0-9 ]", "");
				
				if(caseDatabaseName.isEmpty() == true) {
					return;
				}
				else if(caseNameField.getText() == null) {
					return;
				}
				else if(caseNameField.getText().isEmpty() == true) {
					UtilsLegacy.displayMessageDialog("Case Name", "Please specify a case name.");
					
					return;
				}
				else if(caseNameField.getText().equals("db.videotriage") == true) {
					UtilsLegacy.displayMessageDialog("Case Name", "This case name is reserved for temporary databases.");
					
					return;
				}
				
				final boolean caseDatabaseExists = FileUtilsLegacy.isFileExist(caseDatabaseName);
				
				if(caseDatabaseExists == true) {
					final int deleteOldCaseFile = UtilsLegacy.displayConfirmDialog("Notice", "A case with that name already exists. Delete old case?");
					
					if(deleteOldCaseFile == JOptionPane.YES_OPTION) 
					{
						FileUtilsLegacy.deleteFileOrDirectory(new File(FileUtils.DATABASE_DIRECTORY + caseDatabaseName));
					}
					else
					{
						return;
					}
				}
				
				if(localProcessorButton.isSelected() == true) {
					GuiMain gui = new GuiMain(GuiMain.USE_LOCAL_CAMERA_CONTROLLER, caseDatabaseName);
					CreateNewCaseDialog.this.dispose();
				}
				else if(remoteProcessorButton.isSelected() == true) {
					GuiMain gui = new GuiMain(GuiMain.USE_REMOTE_CAMERA_CONTROLLER, caseDatabaseName);
					CreateNewCaseDialog.this.dispose();
				}
				else {
					final int openAsDefault = UtilsLegacy.displayConfirmDialog("Notice", "Open new case as local?");
					
					if(openAsDefault == JOptionPane.OK_OPTION) {
						GuiMain gui = new GuiMain(GuiMain.USE_LOCAL_CAMERA_CONTROLLER, caseDatabaseName);
						CreateNewCaseDialog.this.dispose();
					}
				}
			}
		});
		controlsPanel.add(createCaseButton);
		
		contentPanel.add(controlsPanel, BorderLayout.CENTER);
		contentPanel.setBorder(BorderUtils.getEmptyBorder());
		
		this.add(contentPanel, BorderLayout.CENTER);
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		WindowUtils.setFrameIcon(this);
		this.setTitle("Create Case");
		this.setPreferredSize(new Dimension(550, 350));
		this.pack();
		this.setVisible(true);
		WindowUtils.center(this);
	}
	
	private void buildMenuBar() {		
		JMenuBar menuBar;
		JMenu menu;

		menuBar = new JMenuBar();

		menu = new JMenu("File");
		menuBar.add(menu);
		
		JMenuItem menuItem = new JMenuItem("Exit to Case Menu");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("NewCase Exit", this);
						
						SelectCaseDialog selectCaseDialog = new SelectCaseDialog();
						CreateNewCaseDialog.this.dispose();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Exit");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {						
						System.exit(0);
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
						ThreadUtils.addThreadToHandleList("NewCase OpenDoc", this);
						
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
						ThreadUtils.addThreadToHandleList("NewCase About", this);
						
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

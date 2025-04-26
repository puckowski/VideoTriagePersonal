package com.keypointforensics.videotriage.gui.data;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.keypointforensics.videotriage.gui.main.SelectCaseDialog;
import com.keypointforensics.videotriage.util.BorderUtils;
import com.keypointforensics.videotriage.util.CursorUtils;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.LicenseUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.Utils;
import com.keypointforensics.videotriage.util.WebUtils;
import com.keypointforensics.videotriage.util.WindowUtils;
import com.keypointforensics.videotriage.window.WindowRegistry;

public class CaseAdvancedDeleteDataWindow extends BaseAdvancedDeleteDataWindow implements ActionListener {

	/*
	 * Author: Daniel Puckowski
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5468066048459750354L;
	
	private final SelectCaseDialog SELECT_CASE_DIALOG;
	
	public CaseAdvancedDeleteDataWindow(final SelectCaseDialog selectCaseDialog) {
		SELECT_CASE_DIALOG = selectCaseDialog;
		
		WindowRegistry.getInstance().registerFrame(this, "CaseAdvancedDeleteData");
	}
	
	public void buildFrame() {		
		buildMenuBar();
		
		this.setLayout(new BorderLayout());
		
		JPanel checkBoxPanel = new JPanel();
		checkBoxPanel.setLayout(new GridLayout(CONTROL_GRID_LAYOUT_ROWS, CONTROL_GRID_LAYOUT_COLUMNS));
		
		mDateRangeComboBox = new JComboBox<ERemovalDateRange>(DATE_RANGE_ENUM_ARRAY);
		mDateRangeComboBox.addActionListener(this);
		mDateRangeComboBox.setSelectedIndex(0);
		
		setRemovalStartDate();
		
		mDeleteButton = new JButton("Delete Data");
		mDeleteButton.addActionListener(this);
		
		JLabel deleteDataLabel = new JLabel("Delete Data Within Range");
		
		checkBoxPanel.add(deleteDataLabel);
		checkBoxPanel.add(mDateRangeComboBox);
		checkBoxPanel.add(mDeleteButton);
		
		JScrollPane optionsScrollPane = new JScrollPane(checkBoxPanel);
		WindowUtils.setScrollBarIncrement(optionsScrollPane);
		optionsScrollPane.setPreferredSize(new Dimension(320, 350));
		optionsScrollPane.setBorder(BorderUtils.getEmptyBorder());
		
		this.add(optionsScrollPane, BorderLayout.CENTER);
		
		this.setTitle("Advanced Delete Data");
		this.pack();
		WindowUtils.setFrameIcon(this);
		this.setVisible(true);
		WindowUtils.center(this);
	}
	
	private void deleteDataAction() {
		new Thread() {
			@Override
			public void run() {
				ThreadUtils.addThreadToHandleList("CaseAdvancedDeleteData FileExit", this);
				
				final int confirmCode = Utils.displayConfirmDialog("Delete Data", "Are you sure you want to delete application data for all cases? This cannot be undone.");
				
				if(confirmCode != Utils.DIALOG_CONFIRM) { 
					return;
				}
				
				if(mRemovalStartDate != null) {
					CaseAdvancedDeleteDataWindow.this.dispose();
					
					CursorUtils.setBusyCursor(SELECT_CASE_DIALOG);
					
					deleteDataWithinRange(mRemovalStartDate);
					
					CursorUtils.setDefaultCursor(SELECT_CASE_DIALOG);
				}
				
				ThreadUtils.removeThreadFromHandleList(this);
			}
		}.start();
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() instanceof JButton) {
			JButton button = (JButton) event.getSource();

			if (button == mDeleteButton) {
				deleteDataAction();
			} 
		} else if(event.getSource() instanceof JComboBox) {
			JComboBox comboBox = (JComboBox) event.getSource();
			
			if(comboBox == mDateRangeComboBox) {
				setRemovalStartDate();
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
						ThreadUtils.addThreadToHandleList("CaseAdvancedDeleteData FileExit", this);
						
						CaseAdvancedDeleteDataWindow.this.dispose();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menu = new JMenu("Data");
		menuBar.add(menu);
		
		menuItem = new JMenuItem("Delete License Data");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				FileUtils.deleteFile(new File(LicenseUtils.LICENSE_KEY_FILENAME));
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
						ThreadUtils.addThreadToHandleList("CaseAdvancedDeleteData OpenDoc", this);
						
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
						ThreadUtils.addThreadToHandleList("CaseAdvancedDeleteData About", this);
						
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

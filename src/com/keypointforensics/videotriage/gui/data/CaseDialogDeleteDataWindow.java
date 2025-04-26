package com.keypointforensics.videotriage.gui.data;

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

import com.keypointforensics.videotriage.gui.main.SelectCaseDialog;
import com.keypointforensics.videotriage.progress.ProgressBundle;
import com.keypointforensics.videotriage.thread.CaseDialogDeleteDataThread;
import com.keypointforensics.videotriage.util.BorderUtils;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.LicenseUtils;
import com.keypointforensics.videotriage.util.ProgressUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.Utils;
import com.keypointforensics.videotriage.util.WebUtils;
import com.keypointforensics.videotriage.util.WindowUtils;
import com.keypointforensics.videotriage.window.ChildWindowList;
import com.keypointforensics.videotriage.window.CloseChildrenWindowAdapter;
import com.keypointforensics.videotriage.window.WindowRegistry;

public class CaseDialogDeleteDataWindow extends JFrame implements ItemListener, ActionListener {

	/*
	 * Author: Daniel Puckowski
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5468066048459750354L;
	
	private static final int    CONTROL_GRID_LAYOUT_ROWS     = 19;
	private static final int    CONTROL_GRID_LAYOUT_COLUMNS  = 1;
	
	//private JCheckBox mLogCheckBox;
	private JCheckBox mImagesCheckBox;
	private JCheckBox mDatabaseCheckBox;
	private JCheckBox mEnhancedCheckBox;
	private JCheckBox mExportsCheckBox;
	private JCheckBox mExtractsCheckBox;
	private JCheckBox mDetectionsCheckBox;
	private JCheckBox mFilteredCheckBox;
	private JCheckBox mTemporaryCheckBox;
	private JCheckBox mReportsCheckBox;
	private JCheckBox mProcessingCheckBox;
	private JCheckBox mPreviewCheckBox;
	private JCheckBox mResizedCheckBox;
	private JCheckBox mReportExtractsCheckBox;
	private JCheckBox mRedactCheckBox;
	private JCheckBox mMergedCheckBox;
	private JCheckBox mNotesCheckBox;
	private JCheckBox mAuditCheckBox;
	private JCheckBox mAllCheckBox;
	private JButton   mDeleteButton;
	
	private SelectCaseDialog mSelectCaseDialog;

	private ChildWindowList mChildWindowList;
	
	public CaseDialogDeleteDataWindow(SelectCaseDialog selectCaseDialog) {
		mSelectCaseDialog = selectCaseDialog;
		
		mChildWindowList = new ChildWindowList();
		
		buildFrame();
		
		WindowRegistry.getInstance().registerFrame(this, "CaseDialogDeleteData");
	}
	
	private void buildFrame() {		
		buildMenuBar();
		
		this.setLayout(new BorderLayout());
		
		JPanel checkBoxPanel = new JPanel();
		checkBoxPanel.setLayout(new GridLayout(CONTROL_GRID_LAYOUT_ROWS, CONTROL_GRID_LAYOUT_COLUMNS));
		
		//mLogCheckBox = new JCheckBox("Delete Log Data");
		//mLogCheckBox.setSelected(false);
		//mLogCheckBox.addItemListener(this);
		
		mImagesCheckBox = new JCheckBox("Delete Image Data");
		mImagesCheckBox.setSelected(false);
		mImagesCheckBox.addItemListener(this);
		
		mDatabaseCheckBox = new JCheckBox("Delete Databases");
		mDatabaseCheckBox.setSelected(false);
		mDatabaseCheckBox.addItemListener(this);
		
		mEnhancedCheckBox = new JCheckBox("Delete Enhanced Videos");
		mEnhancedCheckBox.setSelected(false);
		mEnhancedCheckBox.addItemListener(this);
		
		mExportsCheckBox = new JCheckBox("Delete Exports");
		mExportsCheckBox.setSelected(false);
		mExportsCheckBox.addItemListener(this);
		
		mExtractsCheckBox = new JCheckBox("Delete Video Extracts");
		mExtractsCheckBox.setSelected(false);
		mExtractsCheckBox.addItemListener(this);
		
		mDetectionsCheckBox = new JCheckBox("Delete Detections");
		mDetectionsCheckBox.setSelected(false);
		mDetectionsCheckBox.addItemListener(this);
		
		mFilteredCheckBox = new JCheckBox("Delete Filtered Images");
		mFilteredCheckBox.setSelected(false);
		mFilteredCheckBox.addItemListener(this);
		
		mTemporaryCheckBox = new JCheckBox("Delete Temporary Files");
		mTemporaryCheckBox.setSelected(false);
		mTemporaryCheckBox.addItemListener(this);
		
		mReportsCheckBox = new JCheckBox("Delete Reports");
		mReportsCheckBox.setSelected(false);
		mReportsCheckBox.addItemListener(this);
		
		mProcessingCheckBox = new JCheckBox("Delete Processing Files");
		mProcessingCheckBox.setSelected(false);
		mProcessingCheckBox.addItemListener(this);
		
		mPreviewCheckBox = new JCheckBox("Delete Preview Files");
		mPreviewCheckBox.setSelected(false);
		mPreviewCheckBox.addItemListener(this);
		
		mResizedCheckBox = new JCheckBox("Delete Resized Files");
		mResizedCheckBox.setSelected(false);
		mResizedCheckBox.addItemListener(this);
		
		mReportExtractsCheckBox = new JCheckBox("Delete Report Extracts");
		mReportExtractsCheckBox.setSelected(false);
		mReportExtractsCheckBox.addItemListener(this);
		
		mRedactCheckBox = new JCheckBox("Delete Redacted Files");
		mRedactCheckBox.setSelected(false);
		mRedactCheckBox.addItemListener(this);
		
		mMergedCheckBox = new JCheckBox("Delete Merged Files");
		mMergedCheckBox.setSelected(false);
		mMergedCheckBox.addItemListener(this);
		
		mNotesCheckBox = new JCheckBox("Delete Note Files");
		mNotesCheckBox.setSelected(false);
		mNotesCheckBox.addItemListener(this);

		mAuditCheckBox = new JCheckBox("Delete Audit Files");
		mAuditCheckBox.setSelected(false);
		mAuditCheckBox.addItemListener(this);

		mAllCheckBox = new JCheckBox("Delete Everything");
		mAllCheckBox.setSelected(false);
		mAllCheckBox.addItemListener(this);
		
		mDeleteButton = new JButton("Delete Data");
		mDeleteButton.addActionListener(this);
		
		//checkBoxPanel.add(mLogCheckBox);
		checkBoxPanel.add(mImagesCheckBox);
		checkBoxPanel.add(mDatabaseCheckBox);
		checkBoxPanel.add(mEnhancedCheckBox);
		checkBoxPanel.add(mExportsCheckBox);
		checkBoxPanel.add(mExtractsCheckBox);
		checkBoxPanel.add(mDetectionsCheckBox);
		checkBoxPanel.add(mFilteredCheckBox);
		checkBoxPanel.add(mTemporaryCheckBox);
		checkBoxPanel.add(mReportsCheckBox);
		checkBoxPanel.add(mProcessingCheckBox);
		checkBoxPanel.add(mPreviewCheckBox);
		checkBoxPanel.add(mResizedCheckBox);
		checkBoxPanel.add(mReportExtractsCheckBox);
		checkBoxPanel.add(mRedactCheckBox);
		checkBoxPanel.add(mMergedCheckBox);
		checkBoxPanel.add(mNotesCheckBox);
		checkBoxPanel.add(mAuditCheckBox);
		checkBoxPanel.add(mAllCheckBox);
		checkBoxPanel.add(mDeleteButton);
		
		JScrollPane optionsScrollPane = new JScrollPane(checkBoxPanel);
		WindowUtils.setScrollBarIncrement(optionsScrollPane);
		optionsScrollPane.setPreferredSize(new Dimension(320, 480));
		optionsScrollPane.setBorder(BorderUtils.getEmptyBorder());
		
		this.add(optionsScrollPane, BorderLayout.CENTER);
		//this.add(new SimpleDiskUsageChartPanel(), BorderLayout.SOUTH);

		this.addWindowListener(new CloseChildrenWindowAdapter(mChildWindowList));
		
		this.setTitle("Delete Data");
		this.pack();
		WindowUtils.setFrameIcon(this);
		this.setVisible(true);
		WindowUtils.center(this);
	}
	
	public void closeChildWindows() {
		WindowRegistry.getInstance().closeFrames(mChildWindowList.getWindows());
	}
	
	private boolean isAnyBoxSelected() {
		return mAllCheckBox.isSelected() || 
				mImagesCheckBox.isSelected() ||
				mDatabaseCheckBox.isSelected() ||
				mEnhancedCheckBox.isSelected() ||
				mExportsCheckBox.isSelected() ||
				mExtractsCheckBox.isSelected() ||
				mDetectionsCheckBox.isSelected() ||
				mFilteredCheckBox.isSelected() ||
				mTemporaryCheckBox.isSelected() ||
				mProcessingCheckBox.isSelected() ||
				mPreviewCheckBox.isSelected() ||
				mResizedCheckBox.isSelected() ||
				mReportsCheckBox.isSelected() ||
				mReportExtractsCheckBox.isSelected() ||
				mMergedCheckBox.isSelected() ||
				mNotesCheckBox.isSelected() ||
				mRedactCheckBox.isSelected();
	}

	@Override
	public void itemStateChanged(ItemEvent event) {
		if (event.getSource() instanceof JCheckBox) {
			JCheckBox checkBox = (JCheckBox) event.getSource();

			if (checkBox == mAllCheckBox) {				
				if(mAllCheckBox.isSelected() == true) {
					//mLogCheckBox.setSelected(true);
					mImagesCheckBox.setSelected(true);
					mDatabaseCheckBox.setSelected(true);
					mEnhancedCheckBox.setSelected(true);
					mExportsCheckBox.setSelected(true);
					mExtractsCheckBox.setSelected(true);
					mDetectionsCheckBox.setSelected(true);
					mFilteredCheckBox.setSelected(true);
					mTemporaryCheckBox.setSelected(true);
					mReportsCheckBox.setSelected(true);
					mProcessingCheckBox.setSelected(true);
					mPreviewCheckBox.setSelected(true);
					mResizedCheckBox.setSelected(true);
					mReportExtractsCheckBox.setSelected(true);
					mRedactCheckBox.setSelected(true);
					mMergedCheckBox.setSelected(true);
					mNotesCheckBox.setSelected(true);
				} else if(mAllCheckBox.isSelected() == false) {
					//mLogCheckBox.setSelected(false);
					mImagesCheckBox.setSelected(false);
					mDatabaseCheckBox.setSelected(false);
					mEnhancedCheckBox.setSelected(false);
					mExportsCheckBox.setSelected(false);
					mExtractsCheckBox.setSelected(false);
					mDetectionsCheckBox.setSelected(false);
					mFilteredCheckBox.setSelected(false);
					mTemporaryCheckBox.setSelected(false);
					mReportsCheckBox.setSelected(false);
					mProcessingCheckBox.setSelected(false);
					mPreviewCheckBox.setSelected(false);
					mResizedCheckBox.setSelected(false);
					mReportExtractsCheckBox.setSelected(false);
					mRedactCheckBox.setSelected(false);
					mMergedCheckBox.setSelected(false);
					mNotesCheckBox.setSelected(false);
				}
			}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() instanceof JButton) {
			JButton button = (JButton) event.getSource();

			if (button == mDeleteButton && isAnyBoxSelected() == true) {
				final int confirmCode = Utils.displayConfirmDialog("Delete Data", "Are you sure you want to delete application data for all cases? This cannot be undone.");
				
				if(confirmCode != Utils.DIALOG_CONFIRM) { 
					return;
				}
			
				ProgressBundle deleteProgressBundle = ProgressUtils.getProgressBundle("Delete Data Progress...", 17);
				deleteProgressBundle.frame.invalidate();
				deleteProgressBundle.frame.repaint();
				
				CaseDialogDeleteDataThread deleteDataThread = new CaseDialogDeleteDataThread(this, mSelectCaseDialog, mImagesCheckBox, 
						mDatabaseCheckBox, mEnhancedCheckBox, mExportsCheckBox, mExtractsCheckBox, 
						mDetectionsCheckBox, mFilteredCheckBox, mTemporaryCheckBox, mReportsCheckBox, mProcessingCheckBox, mPreviewCheckBox, 
						mResizedCheckBox, mReportExtractsCheckBox, mRedactCheckBox, mMergedCheckBox, mNotesCheckBox, mAllCheckBox, deleteProgressBundle);
				deleteDataThread.start();

				closeChildWindows();
				this.dispose();
			} 
		}
	}
	
	private void buildMenuBar() {		
		JMenuBar menuBar;
		JMenu menu;

		menuBar = new JMenuBar();

		menu = new JMenu("File");
		menuBar.add(menu);
		
		JMenuItem menuItem = new JMenuItem("Advanced Delete");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("CaseDeleteDialog AdvancedDelete", this);
						
						CaseAdvancedDeleteDataWindow caseAdvancedDeleteDataWindow = new CaseAdvancedDeleteDataWindow(mSelectCaseDialog);
						caseAdvancedDeleteDataWindow.buildFrame();
						
						mChildWindowList.addWindow(caseAdvancedDeleteDataWindow);
						
						CaseDialogDeleteDataWindow.this.dispose();
						
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
						ThreadUtils.addThreadToHandleList("CaseDeleteDialog FileExit", this);
						
						WindowRegistry.getInstance().closeFrames(mChildWindowList.getWindows());
						CaseDialogDeleteDataWindow.this.dispose();
						
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
						ThreadUtils.addThreadToHandleList("CaseDeleteDialog OpenDoc", this);
						
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
						ThreadUtils.addThreadToHandleList("CaseDeleteDialog About", this);
						
						Utils.displayMessageDialog("About", Utils.SOFTWARE_NAME + " \nVersion: " + Utils.SOFTWARE_VERSION + "\n� " + Utils.AUTHOR_NAME);
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		this.setJMenuBar(menuBar);
	}
}

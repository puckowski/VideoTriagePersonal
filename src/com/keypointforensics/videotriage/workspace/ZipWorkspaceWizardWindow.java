package com.keypointforensics.videotriage.workspace;

import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import com.keypointforensics.videotriage.gui.main.SelectCaseDialog;
import com.keypointforensics.videotriage.legacy.UtilsLegacy;
import com.keypointforensics.videotriage.thread.SimpleDeleteDataThread;
import com.keypointforensics.videotriage.util.CursorUtils;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.Utils;
import com.keypointforensics.videotriage.util.WebUtils;
import com.keypointforensics.videotriage.util.ZipUtils;
import com.keypointforensics.videotriage.workspace.BaseZipWizardWindow;

public class ZipWorkspaceWizardWindow extends BaseZipWizardWindow {

	/*
	 * Author: Daniel Puckowski
	 */
	
	private static final long serialVersionUID = 4271388619852805707L;
	
	private SelectCaseDialog mSelectCaseDialog;
	
	public ZipWorkspaceWizardWindow() {	
		super(FileUtils.ROOT_DIRECTORY, "Workspace");
	}
	
	public ZipWorkspaceWizardWindow(SelectCaseDialog selectCaseDialog) {	
		super(FileUtils.ROOT_DIRECTORY, "Workspace");
		
		mSelectCaseDialog = selectCaseDialog;
	}
	
	protected void addDataFoldersAction() {		
		final File resourceDirectory = new File(FileUtils.RESOURCES_DIRECTORY);	
			
		EventQueue.invokeLater(new Runnable() { public void run() {
				mFileDropList.addFile(resourceDirectory.getAbsolutePath());
				mFileDropList.revalidate();
			}
		});
	}
	
	private void importWorkspace() {
		final int deleteWorkspaceChoice = UtilsLegacy.displayConfirmDialog("Import Workspace", "Importing a workspace will delete the current one.\n"
				+ "A backup is recommended. Continue with import?");
		
		if(deleteWorkspaceChoice == JOptionPane.OK_OPTION) {	
			CursorUtils.setBusyCursor(this);
			
			String absoluteWorkspacePath = FileUtils.performSelectFileAction();
			
			if(absoluteWorkspacePath == null) {
				Utils.displayMessageDialog("No Workspace", "Please select a valid Video Triage workspace to import.");
				
				return;
			}
			
			//FileUtilsLegacy.deleteDirectoryContents(new File(FileUtils.RESOURCES_DIRECTORY), true);
			//FileUtilsLegacy.deleteFileOrDirectory(new File(FileUtils.RESOURCES_DIRECTORY));
			
			final int deleteExistingDataChoice = UtilsLegacy.displayConfirmDialog("Cleanup Data", "Delete all existing workspace data, including workspace exports?");
			
			if(deleteExistingDataChoice == JOptionPane.OK_OPTION) {	
				SimpleDeleteDataThread simpleDeleteDataThread = new SimpleDeleteDataThread();
				simpleDeleteDataThread.start();
				
				try {
					simpleDeleteDataThread.join();
				} catch (InterruptedException interruptedException) {
					//interruptedException.printStackTrace();
				}
			}
			
			boolean importSuccess = true;
			
			try {
				//ZipUtils.unzipFile(absoluteWorkspacePath, FileUtils.ROOT_DIRECTORY);
				ZipUtils.unzipFile(absoluteWorkspacePath, FileUtils.ROOT_DIRECTORY);
			} catch (IOException ioException) {
				//ioException.printStackTrace();
				
				importSuccess = false;
				
				Utils.displayMessageDialog("Import Error", "Video Triage did not successfully import the selected workspace.");
			}
		
			CursorUtils.setDefaultCursor(this);
			
			mFileDropList.clearList();
			
			addDataFoldersAction();
			
			if(mSelectCaseDialog != null) {
				mSelectCaseDialog.updateDatabaseList();
			}
			
			if(importSuccess == true) {
				Utils.displayMessageDialog("Notice", "Import completed successfully.");
			}
		}
	}
	
	protected void buildMenuBar() {		
		JMenuBar menuBar;
		JMenu menu;

		menuBar = new JMenuBar();

		menu = new JMenu("File");
		menuBar.add(menu);
		
		JMenuItem menuItem = new JMenuItem("Import Workspace");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ExportReportWizWindow ImportWorkspace", this);
						
						importWorkspace();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Open Export Folder");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ExportReportWizWindow OpenExport", this);
						
						final File file = new File(FileUtils.EXPORTS_DIRECTORY);
						final Desktop desktop = Desktop.getDesktop();
						
						try {
							desktop.browse(file.toURI());
						} catch (IOException ioException) {

						}
						
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
						ThreadUtils.addThreadToHandleList("ExportReportWizWindow Exit", this);
						
						ZipWorkspaceWizardWindow.this.dispose();
						
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
						ThreadUtils.addThreadToHandleList("ExportReportWizWindow OpenDoc", this);
						
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
						ThreadUtils.addThreadToHandleList("ExportReportWizWindow About", this);
						
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

package com.keypointforensics.videotriage.report;

import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.Utils;
import com.keypointforensics.videotriage.util.WebUtils;
import com.keypointforensics.videotriage.workspace.BaseZipWizardWindow;

public class ZipReportWizardWindow extends BaseZipWizardWindow {

	/*
	 * Author: Daniel Puckowski
	 */
	
	private static final long serialVersionUID = 4271388619852805707L;
	
	public ZipReportWizardWindow() {	
		super(FileUtils.REPORTS_DIRECTORY, "Report");
	}
	
	protected void addDataFoldersAction() {		
		final File reportDirectory = new File(FileUtils.REPORTS_DIRECTORY);	
		final File[] selectedFiles = reportDirectory.listFiles();
			
		for(int i = 0; i < selectedFiles.length; ++i) {
			final File selectedFile = selectedFiles[i];
				
			EventQueue.invokeLater(new Runnable() { public void run() {
					mFileDropList.addFile(selectedFile.getAbsolutePath());
					mFileDropList.revalidate();
				}
			});
		}
	}
	
	protected void buildMenuBar() {		
		JMenuBar menuBar;
		JMenu menu;

		menuBar = new JMenuBar();

		menu = new JMenu("File");
		menuBar.add(menu);
		
		JMenuItem menuItem = new JMenuItem("Open Export Folder");
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
		
		menuItem = new JMenuItem("Open Report Folder");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ExportReportWizWindow OpenReport", this);
						
						final File file = new File(FileUtils.REPORTS_DIRECTORY);
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
						
						ZipReportWizardWindow.this.dispose();
						
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

package com.keypointforensics.videotriage.gui.main;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import com.keypointforensics.videotriage.gui.data.CaseDialogDeleteDataWindow;
import com.keypointforensics.videotriage.gui.localfile.wizard.convert.ConvertLocalFileWizardWindow;
import com.keypointforensics.videotriage.gui.localfile.wizard.video.EnhanceLocalFileWizardWindow;
import com.keypointforensics.videotriage.gui.resize.ResizeLocalFileWizardWindow;
import com.keypointforensics.videotriage.gui.wizard.preview.PreviewLocalFileWizardWindow;
import com.keypointforensics.videotriage.legacy.FileUtilsLegacy;
import com.keypointforensics.videotriage.legacy.UtilsLegacy;
import com.keypointforensics.videotriage.report.ZipReportWizardWindow;
import com.keypointforensics.videotriage.sqlite.MySqliteDatabaseHelper;
import com.keypointforensics.videotriage.thread.DeleteCaseDataThread;
import com.keypointforensics.videotriage.thread.EnhanceLocalVideoFilesThread;
import com.keypointforensics.videotriage.util.CursorUtils;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.Utils;
import com.keypointforensics.videotriage.util.WebUtils;
import com.keypointforensics.videotriage.util.WindowUtils;
import com.keypointforensics.videotriage.window.WindowRegistry;
import com.keypointforensics.videotriage.workspace.ZipWorkspaceWizardWindow;

public class SelectCaseDialog extends JFrame {

	/*
	 * Author: Daniel Puckowski
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8642971760265121619L;
	
	private static final int DOUBLE_CLICK = 2;
	private static final int SINGLE_CLICK = 1;
	
	private JTextArea caseInformationTextArea;
	private JList<String> caseDatabaseList;
	private JScrollPane caseDatabaseListScrollPane;
	
	public SelectCaseDialog() {
		buildMenuBar();
		
		this.setLayout(new BorderLayout());
		
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout());

		ArrayList<String> namesInDatabaseFolderArrayList = (ArrayList<String>) FileUtilsLegacy.parseDirectory(FileUtils.DATABASE_DIRECTORY);
		
		for(int i = 0; i < namesInDatabaseFolderArrayList.size(); ++i) {
			if(namesInDatabaseFolderArrayList.get(i).endsWith("-journal") == true) {
				namesInDatabaseFolderArrayList.remove(i);
				i--;
			}
			else if(namesInDatabaseFolderArrayList.get(i).endsWith("db.videotriage") == true) {
				namesInDatabaseFolderArrayList.set(i, "Test Database");
			}
		}
		
		String[] namesInDatabaseFolder = namesInDatabaseFolderArrayList.toArray(new String[0]);
		namesInDatabaseFolderArrayList.clear();
		namesInDatabaseFolderArrayList = null;
		
		int i;
		
		for(i = 0; i < namesInDatabaseFolder.length; ++i) {
			if(namesInDatabaseFolder[i].contains(File.separator)) {
				namesInDatabaseFolder[i] = namesInDatabaseFolder[i].substring(namesInDatabaseFolder[i].lastIndexOf(File.separator) + 1, namesInDatabaseFolder[i].length());
			}
		}
	
		caseDatabaseList = new JList<String>(namesInDatabaseFolder);
		caseDatabaseList.setPreferredSize(new Dimension(200, 400));
		
		boolean canSetInitialValues = false;
		if(namesInDatabaseFolder.length > 0) {
			canSetInitialValues = true;
			caseDatabaseList.setSelectedIndex(0);
		}
		
		caseDatabaseListScrollPane = new JScrollPane(caseDatabaseList);
		WindowUtils.setScrollBarIncrement(caseDatabaseListScrollPane);
		namesInDatabaseFolder = null;
		
		caseDatabaseList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent keyEvent) {
				updateCaseInformationTextArea();
			}
			
			@Override
			public void keyTyped(KeyEvent keyEvent) {
				updateCaseInformationTextArea();
			}
			
			@Override
			public void keyReleased(KeyEvent keyEvent) {
				updateCaseInformationTextArea();
			}
		});
		
		caseDatabaseList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == SINGLE_CLICK) {
					updateCaseInformationTextArea();
				}
				else if (mouseEvent.getClickCount() == DOUBLE_CLICK) {
					final String selectedValue = caseDatabaseList.getSelectedValue();

					if(selectedValue.equals(null) == true) {						
						return;
					}
					else if(selectedValue.equals("No cases created yet") == true) {						
						return;
					}
					
					openAsLocalAction();
				}
			}
		});
		
		JTabbedPane caseListTabbedPane = new JTabbedPane();
		caseListTabbedPane.addTab("Case List", caseDatabaseListScrollPane);
				
		caseInformationTextArea = new JTextArea(10, 60);
		caseInformationTextArea.setEditable(false);
		WindowUtils.setTextAreaUpdatePolicy(caseInformationTextArea);
		JScrollPane caseInformationTextAreaScrollPane = new JScrollPane(caseInformationTextArea);
		WindowUtils.setScrollBarIncrement(caseInformationTextAreaScrollPane);
		
		JPanel textAreaPanel = new JPanel();
		textAreaPanel.setLayout(new BorderLayout());
		
		JLabel textAreaLabel = new JLabel("Case Database Information");
		
		textAreaPanel.add(textAreaLabel, BorderLayout.NORTH);
		textAreaPanel.add(caseInformationTextAreaScrollPane, BorderLayout.CENTER);
						
		JSplitPane rightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, caseListTabbedPane, textAreaPanel);
		rightSplitPane.setResizeWeight(0.3);
		rightSplitPane.setOneTouchExpandable(true);
		rightSplitPane.setContinuousLayout(true);
		contentPanel.add(rightSplitPane, BorderLayout.CENTER); 
		
		if(canSetInitialValues == true) {			
			updateCaseInformationTextArea();
		}
		else {
			setPlaceholderCaseInformation();
		}
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		
		JButton createNewCaseButton = new JButton("Create New Case");
		createNewCaseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				createNewCaseAction();
			}
		});
		buttonPanel.add(createNewCaseButton);

		JButton openAsLocalButton = new JButton("Open As Local");
		openAsLocalButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				openAsLocalAction();
			}
		});
		buttonPanel.add(openAsLocalButton);
		
		JButton openAsRemoteButton = new JButton("Open As Remote");
		openAsRemoteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				openAsRemoteAction();
			}
		});
		buttonPanel.add(openAsRemoteButton);
		
		JButton openTestDatabaseAsLocalButton = new JButton("Open Test As Local");
		openTestDatabaseAsLocalButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				openTestAsLocalAction();
			}
		});
		buttonPanel.add(openTestDatabaseAsLocalButton);
		
		JButton openTestDatabaseAsRemoteButton = new JButton("Open Test As Remote");
		openTestDatabaseAsRemoteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				openTestAsRemoteAction();
			}
		});
		buttonPanel.add(openTestDatabaseAsRemoteButton);
		
		JButton removeDatabaseButton = new JButton("Delete Case");
		removeDatabaseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				new Thread() {
					@Override
					public void run() {
						//ThreadUtils.addThreadToHandleList("SelectCase DelAction", this);
																		
						removeSelectedDatabaseAction();
						
						//ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
		});
		buttonPanel.add(removeDatabaseButton);
		
		contentPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		this.add(contentPanel, BorderLayout.CENTER);
		
		this.setTitle("Select Case");
		WindowUtils.setFrameIcon(this);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setPreferredSize(new Dimension(900, 600)); 
		this.pack();
		WindowUtils.maximize(this);
		this.setVisible(true);
		//WindowUtils.center(this);
	}
	
	private void createNewCaseAction() {
		CreateNewCaseDialog createNewCaseDialog = new CreateNewCaseDialog();
		WindowRegistry.getInstance().closeAllActiveFrames();
		SelectCaseDialog.this.dispose();
	}
	
	private void openAsLocalAction() {
		String selectedValue = caseDatabaseList.getSelectedValue();

		if(selectedValue.equals(null) == true) {			
			return;
		}
		else if(selectedValue.equals("No cases created yet") == true) {			
			return;
		}
		
		if(selectedValue.equals("Test Database") == true) {
			selectedValue = "db.videotriage";
		}
		
		final String caseDatabaseName = FileUtils.DATABASE_DIRECTORY + selectedValue;

		GuiMain gui = new GuiMain(GuiMain.USE_LOCAL_CAMERA_CONTROLLER, caseDatabaseName); //namesInDatabaseFolderOriginal[n]);
		WindowRegistry.getInstance().closeAllActiveFrames();
		SelectCaseDialog.this.dispose();
	}
	
	private void openAsRemoteAction() {
		String selectedValue = caseDatabaseList.getSelectedValue();

		if(selectedValue.equals(null) == true) {			
			return;
		}
		else if(selectedValue.equals("No cases created yet") == true) {			
			return;
		}
		
		if(selectedValue.equals("Test Database") == true) {
			selectedValue = "db.videotriage";
		}
		
		final String caseDatabaseName = FileUtils.DATABASE_DIRECTORY + selectedValue;

		GuiMain gui = new GuiMain(GuiMain.USE_REMOTE_CAMERA_CONTROLLER, caseDatabaseName); 
		WindowRegistry.getInstance().closeAllActiveFrames();
		SelectCaseDialog.this.dispose();
	}
	
	private void openTestAsLocalAction() {
		final String caseDatabaseName = FileUtils.DATABASE_DIRECTORY + "db.videotriage";
		
		final boolean dbAlreadyExists = FileUtilsLegacy.isFileExist(caseDatabaseName);
		
		if(dbAlreadyExists == true) {			
			final int deleteDbChoice = UtilsLegacy.displayConfirmDialog("Notice", "A temporary database already exists.\n" +
					"Remove it and proceed?");
			
			if(deleteDbChoice == JOptionPane.OK_OPTION) {			
				removeSelectedDatabaseAction();
			} else {
				return;
			}
		}
		
		final boolean result = FileUtils.deleteFile(new File(caseDatabaseName));
		
		UtilsLegacy.displayMessageDialog("Notice", "Opening a local test database.\n" +
				"Please note that this is only a temporary database.\n" +
				"Data may be lost on application exit.");
		
		GuiMain gui = new GuiMain(GuiMain.USE_LOCAL_CAMERA_CONTROLLER, caseDatabaseName); 
		WindowRegistry.getInstance().closeAllActiveFrames();
		SelectCaseDialog.this.dispose();
	}
	
	private void openTestAsRemoteAction() {
		final String caseDatabaseName = FileUtils.DATABASE_DIRECTORY + "db.videotriage";
	
		final boolean dbAlreadyExists = FileUtilsLegacy.isFileExist(caseDatabaseName);
		
		if(dbAlreadyExists == true) {			
			final int deleteDbChoice = UtilsLegacy.displayConfirmDialog("Notice", "A temporary database already exists.\n" +
					"Remove it and proceed?");
			
			if(deleteDbChoice == JOptionPane.OK_OPTION) {
				removeSelectedDatabaseAction();
			} else {
				return;
			}
		}
		
		final boolean result = FileUtils.deleteFile(new File(caseDatabaseName));
		
		UtilsLegacy.displayMessageDialog("Notice", "Opening a remote test database.\n" +
				"Please note that this is only a temporary database.\n" +
				"Data may be lost on application exit.");
		
		GuiMain gui = new GuiMain(GuiMain.USE_REMOTE_CAMERA_CONTROLLER, caseDatabaseName); 
		WindowRegistry.getInstance().closeAllActiveFrames();
		SelectCaseDialog.this.dispose();
	}
	
	private void removeSelectedDatabaseAction() {
		String selectedValue = caseDatabaseList.getSelectedValue();
		final int selectedIndex  = caseDatabaseList.getSelectedIndex();

		if(selectedValue == null) {			
			return;
		} else if(selectedValue.equals("Test Database") == true) {
			selectedValue = "db.videotriage";
		}

		CursorUtils.setBusyCursor(this);
		
		final String caseDatabaseName = FileUtils.DATABASE_DIRECTORY + selectedValue;
		final String blobContextName = FileUtils.CONTEXT_DIRECTORY + selectedValue + "_blob_context.txt";
		final String contextName = FileUtils.CONTEXT_DIRECTORY + selectedValue + "_context.txt";
		final String captureName = FileUtils.CAPTURES_DIRECTORY + selectedValue;
		final String notesName = FileUtils.NOTES_DIRECTORY + selectedValue;
		final String reportExtractsName = FileUtils.REPORT_EXTRACTS_DIRECTORY + selectedValue;
		
		DeleteCaseDataThread deleteCaseDataThread = new DeleteCaseDataThread(selectedValue, caseDatabaseName, blobContextName, contextName, captureName,
			notesName, reportExtractsName);
		deleteCaseDataThread.start();
				
		try {
			deleteCaseDataThread.join();
		} catch (InterruptedException interruptedException) {
			//interruptedException.printStackTrace();
		}
		
		updateDatabaseList(selectedIndex - 1);
		
		CursorUtils.setDefaultCursor(this);
	}
	
	private void buildMenuBar() {		
		JMenuBar menuBar;
		JMenu menu;

		menuBar = new JMenuBar();

		menu = new JMenu("File");
		menuBar.add(menu);
		
		JMenuItem menuItem = new JMenuItem("Manage Workspace");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SelectCase ExportWorkspace", this);
						
						ZipWorkspaceWizardWindow zipWorkspaceWizardWindow = new ZipWorkspaceWizardWindow(SelectCaseDialog.this);
					
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Delete Data"); 
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SelectCase Delete", this);
												
						CaseDialogDeleteDataWindow deleteDataWindow = new CaseDialogDeleteDataWindow(SelectCaseDialog.this);
						
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
		
		menu = new JMenu("Case");
		menuBar.add(menu);
		
		menuItem = new JMenuItem("Refresh Case List");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SelectCase UpdateDatabaseList", this);
						
						updateDatabaseList();
					
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Create New");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SelectCase Create", this);
												
						createNewCaseAction();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Open As Local");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SelectCase OpenLoc", this);
												
						openAsLocalAction();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Open As Remote");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SelectCase OpenRem", this);
												
						openAsRemoteAction();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Open Test As Local");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SelectCase OpenTestLoc", this);
												
						openTestAsLocalAction();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Open Test As Remote");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SelectCase OpenTestRem", this);
												
						openTestAsRemoteAction();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Delete Case");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SelectCase RemSelect", this);
												
						removeSelectedDatabaseAction();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menu = new JMenu("Evidence");
		menuBar.add(menu);
		
		menuItem = new JMenuItem("Get Video Details");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {	
						ThreadUtils.addThreadToHandleList("SelectCase GetVidDetails", this);
						
						LocalEvidenceMetadataWindow localEvidenceMetadataWindow = new LocalEvidenceMetadataWindow();
						localEvidenceMetadataWindow.performSelectFileAction();
						localEvidenceMetadataWindow.buildAndDisplay();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menu = new JMenu("Report");
		menuBar.add(menu);
		
		menuItem = new JMenuItem("Export");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SelectCase ExportReport", this);
						
						ZipReportWizardWindow zipReportWizardWindow = new ZipReportWizardWindow();
					
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
						ThreadUtils.addThreadToHandleList("SelectCase OpenExport", this);
						
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
						ThreadUtils.addThreadToHandleList("SelectCase OpenReport", this);
						
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
		
		menu = new JMenu("Enhance");
		menuBar.add(menu);
		
		menuItem = new JMenuItem("Open Video Wizard");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SelectCase OpenVideoWizard", this);
												
						EnhanceLocalFileWizardWindow enhanceLocalFileWizardWindow = new EnhanceLocalFileWizardWindow();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Open Enhanced Video Folder");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SelectCase OpenEnhancedVideo", this);
						
						EnhanceLocalVideoFilesThread.performOpenEnhancedVideoFolderAction();
												
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
		});
		menu.add(menuItem);
		
		menu = new JMenu("Preview");
		menuBar.add(menu);
		
		menuItem = new JMenuItem("Create");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {	
						ThreadUtils.addThreadToHandleList("SelectCase CreatePreview", this);
						
						PreviewLocalFileWizardWindow previewLocalFileWizardWindow = new PreviewLocalFileWizardWindow(SelectCaseDialog.this);

						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Open Preview Folder");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SelectCase OpenPreview", this);
						
						final File file = new File(FileUtils.PREVIEWS_DIRECTORY);
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
		
		menu = new JMenu("Resize");
		menuBar.add(menu);
		
		menuItem = new JMenuItem("Create");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {	
						ThreadUtils.addThreadToHandleList("SelectCase CreateResized", this);
						
						ResizeLocalFileWizardWindow resizeLocalFileWizardWindow = new ResizeLocalFileWizardWindow(SelectCaseDialog.this);

						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Open Resized Folder");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SelectCase OpenResized", this);
						
						final File file = new File(FileUtils.RESIZED_DIRECTORY);
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
		
		menu = new JMenu("Convert");
		menuBar.add(menu);
		
		menuItem = new JMenuItem("Create");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {	
						ThreadUtils.addThreadToHandleList("SelectCase CreateConverted", this);
						
						ConvertLocalFileWizardWindow convertLocalFileWizardWindow = new ConvertLocalFileWizardWindow();

						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Open Exports Folder");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SelectCase OpenExports", this);
						
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
		
		menu = new JMenu("Help");
		menuBar.add(menu);
		
		menuItem = new JMenuItem("Open Documentation");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("SelectCase OpenDoc", this);
						
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
						ThreadUtils.addThreadToHandleList("SelectCase About", this);
						
						Utils.displayMessageDialog("About", Utils.SOFTWARE_NAME + " \nVersion: " + Utils.SOFTWARE_VERSION + "\n© " + Utils.AUTHOR_NAME);
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		this.setJMenuBar(menuBar);
	}
	
	private void updateCaseInformationTextArea() {
		final String displayCaseDatabaseName = caseDatabaseList.getSelectedValue();
		
		String caseDatabaseName = caseDatabaseList.getSelectedValue();

		if(caseDatabaseName == null) {			
			return;
		}
		else if(caseDatabaseName.equals("No cases created yet") == true) {			
			return;
		}
		else if(caseDatabaseName.equals("Test Database") == true) {
			caseDatabaseName = MySqliteDatabaseHelper.DB_NAME;
		}
		
		final String caseDatabaseAbsolutePath = FileUtils.DATABASE_DIRECTORY + caseDatabaseName;
		final File caseDatabaseFile           = new File(caseDatabaseAbsolutePath);
		
		String canRead = String.valueOf(caseDatabaseFile.canRead());
		String canWrite = String.valueOf(caseDatabaseFile.canWrite());

		//canRead = canRead.substring(0, 1).toUpperCase() + canRead.substring(1);
		//canWrite = canWrite.substring(0, 1).toUpperCase() + canWrite.substring(1);
		
		caseInformationTextArea.setText("");
		
		SimpleDateFormat lastModifiedDateFormatter = new SimpleDateFormat(Utils.LAST_MODIFIED_DATE_FORMAT);
		final String formattedLastModifiedDate = lastModifiedDateFormatter.format(caseDatabaseFile.lastModified());
		
		caseInformationTextArea.append("Name: " + displayCaseDatabaseName + "\n");
		caseInformationTextArea.append("Path: " + FileUtils.DATABASE_DIRECTORY + "\n");
		caseInformationTextArea.append("Absolute Path: " + caseDatabaseAbsolutePath + "\n\n");
		caseInformationTextArea.append("Last Modified: " + formattedLastModifiedDate + "\n\n");
		caseInformationTextArea.append("Can Read: " + canRead + "\n");
		caseInformationTextArea.append("Can Write: " + canWrite + "\n");
		caseInformationTextArea.append("Database Size: " + FileUtils.humanReadableByteCount(caseDatabaseFile.length()) + "\n");
		caseInformationTextArea.append("\n");
	}
	
	private void setPlaceholderCaseInformation() {
		caseInformationTextArea.setText("");
		
		//caseInformationTextArea.append("Case Database Information: \n\n");
		caseInformationTextArea.append("No cases to display information for.\n");
		
		String[] placeholderDataArray = {
				"No cases created yet"
		};

		caseDatabaseList.setListData(placeholderDataArray);
		caseDatabaseList.revalidate();
		caseDatabaseListScrollPane.revalidate();
	}
	
	public void updateDatabaseList() {
		ArrayList<String> namesInDatabaseFolderArrayList = (ArrayList<String>) FileUtilsLegacy.parseDirectory(FileUtils.DATABASE_DIRECTORY);
		
		for(int i = 0; i < namesInDatabaseFolderArrayList.size(); ++i) {
			if(namesInDatabaseFolderArrayList.get(i).endsWith("-journal") == true) {
				namesInDatabaseFolderArrayList.remove(i);
				i--;
			}
			else if(namesInDatabaseFolderArrayList.get(i).endsWith("db.videotriage") == true) {
				namesInDatabaseFolderArrayList.set(i, "Test Database");
			}
		}
		
		String[] namesInDatabaseFolder = namesInDatabaseFolderArrayList.toArray(new String[0]);
		namesInDatabaseFolderArrayList.clear();
		namesInDatabaseFolderArrayList = null;

		int i;

		for (i = 0; i < namesInDatabaseFolder.length; ++i) {
			if (namesInDatabaseFolder[i].contains(File.separator)) {
				namesInDatabaseFolder[i] = namesInDatabaseFolder[i].substring(
						namesInDatabaseFolder[i].lastIndexOf(File.separator) + 1, namesInDatabaseFolder[i].length());
			}
		}
		
		if(namesInDatabaseFolder.length > 0) {
			caseDatabaseList.removeAll();
			
			caseDatabaseList.revalidate();
			caseDatabaseListScrollPane.revalidate();
			
			caseDatabaseList.setListData(namesInDatabaseFolder);
			
			caseDatabaseList.revalidate();
			caseDatabaseListScrollPane.revalidate();
			
			updateCaseInformationTextArea();
		}
		else {
			setPlaceholderCaseInformation();
		}
	}
	
	public void updateDatabaseList(final int selectedIndex) {
		ArrayList<String> namesInDatabaseFolderArrayList = (ArrayList<String>) FileUtilsLegacy.parseDirectory(FileUtils.DATABASE_DIRECTORY);
		
		for(int i = 0; i < namesInDatabaseFolderArrayList.size(); ++i) {
			if(namesInDatabaseFolderArrayList.get(i).endsWith("-journal") == true) {
				namesInDatabaseFolderArrayList.remove(i);
				i--;
			}
			else if(namesInDatabaseFolderArrayList.get(i).endsWith("db.videotriage") == true) {
				namesInDatabaseFolderArrayList.set(i, "Test Database");
			}
		}
		
		String[] namesInDatabaseFolder = namesInDatabaseFolderArrayList.toArray(new String[0]);
		namesInDatabaseFolderArrayList.clear();
		namesInDatabaseFolderArrayList = null;

		int i;

		for (i = 0; i < namesInDatabaseFolder.length; ++i) {
			if (namesInDatabaseFolder[i].contains(File.separator)) {
				namesInDatabaseFolder[i] = namesInDatabaseFolder[i].substring(
						namesInDatabaseFolder[i].lastIndexOf(File.separator) + 1, namesInDatabaseFolder[i].length());
			}
		}
		
		if(namesInDatabaseFolder.length > 0) {
			caseDatabaseList.removeAll();
			
			caseDatabaseList.revalidate();
			caseDatabaseListScrollPane.revalidate();
			
			caseDatabaseList.setListData(namesInDatabaseFolder);
			caseDatabaseList.setSelectedIndex(selectedIndex);
			
			caseDatabaseList.revalidate();
			caseDatabaseListScrollPane.revalidate();
			
			updateCaseInformationTextArea();
		}
		else {
			setPlaceholderCaseInformation();
		}
	}
}

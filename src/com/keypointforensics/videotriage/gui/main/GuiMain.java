package com.keypointforensics.videotriage.gui.main;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.keypointforensics.videotriage.audit.AuditLogWindow;
import com.keypointforensics.videotriage.audit.CaseAuditor;
import org.tmatesoft.sqljet.core.SqlJetException;

import com.keypointforensics.videotriage.blob.context.BlobContextList;
import com.keypointforensics.videotriage.casestore.CaseMetadataWriter;
import com.keypointforensics.videotriage.detect.DetectionModuleWizardWindow;
import com.keypointforensics.videotriage.detect.ExtractDetectionModuleWizardWindow;
import com.keypointforensics.videotriage.detect.car.DatabaseCarCrawler;
import com.keypointforensics.videotriage.detect.explicit.DatabaseExplicitCrawler;
import com.keypointforensics.videotriage.detect.face.DatabaseFaceCrawler;
import com.keypointforensics.videotriage.detect.license.DatabaseLicensePlateCrawler;
import com.keypointforensics.videotriage.detect.pedestrian.DatabasePedestrianCrawler;
import com.keypointforensics.videotriage.gui.controller.local.LocalCameraController;
import com.keypointforensics.videotriage.gui.controller.remote.RemoteCameraController;
import com.keypointforensics.videotriage.gui.data.DeleteDataWindow;
import com.keypointforensics.videotriage.gui.database.DatabaseBrowseWindow;
import com.keypointforensics.videotriage.gui.extract.ExtractImageGallery;
import com.keypointforensics.videotriage.gui.extract.ExtractVideoFramesDialog;
import com.keypointforensics.videotriage.gui.gallery.FilterImageGallery;
import com.keypointforensics.videotriage.gui.gallery.UpdatedImageGallery;
import com.keypointforensics.videotriage.gui.localfile.wizard.LocalFileWizardWindow;
import com.keypointforensics.videotriage.gui.localfile.wizard.convert.ConvertLocalFileWizardWindow;
import com.keypointforensics.videotriage.gui.localfile.wizard.video.EnhanceLocalFileWizardWindow;
import com.keypointforensics.videotriage.gui.main.controller.CameraController;
import com.keypointforensics.videotriage.gui.main.controller.CameraControllerRegistry;
import com.keypointforensics.videotriage.gui.notes.CaseNotesEditor;
import com.keypointforensics.videotriage.gui.resize.ResizeLocalFileWizardWindow;
import com.keypointforensics.videotriage.gui.wizard.preview.PreviewImageFileWizardWindow;
import com.keypointforensics.videotriage.gui.wizard.preview.PreviewLocalFileWizardWindow;
import com.keypointforensics.videotriage.image.match.SearchCaptureFileWizardWindow;
import com.keypointforensics.videotriage.legacy.UtilsLegacy;
import com.keypointforensics.videotriage.legacy.WindowsVideoFrameExtractorLegacy;
import com.keypointforensics.videotriage.merge.MergeLocalFileWizardWindow;
import com.keypointforensics.videotriage.report.ImageGallery;
import com.keypointforensics.videotriage.report.ZipReportWizardWindow;
import com.keypointforensics.videotriage.report.chart.SimpleChartGenerator;
import com.keypointforensics.videotriage.sqlite.CaseSqliteDatabaseHelper;
import com.keypointforensics.videotriage.staticparams.LocalFileRuntimeParams;
import com.keypointforensics.videotriage.thread.CloseCameraControllerThread;
import com.keypointforensics.videotriage.thread.EnhanceLocalVideoFilesThread;
import com.keypointforensics.videotriage.thread.ProgressBarUpdateThread;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ImageUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.Utils;
import com.keypointforensics.videotriage.util.WebUtils;
import com.keypointforensics.videotriage.util.WindowUtils;
import com.keypointforensics.videotriage.window.WindowRegistry;
import com.keypointforensics.videotriage.workspace.ZipWorkspaceWizardWindow;

public class GuiMain extends JFrame {

	/*
	 * Author: Daniel Puckowski
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 949857885725097872L;
	
	public static final int USE_LOCAL_CAMERA_CONTROLLER  = 0;
	public static final int USE_REMOTE_CAMERA_CONTROLLER = 1;
	
	private final CameraControllerRegistry CONTROLLER_REGISTRY = CameraControllerRegistry.INSTANCE;
	private final int                      INVALID_INDEX       = -1;
	
	private final int             CONTROLLER_TYPE_ID;
	private final String          DATABASE_NAME;
	private final BlobContextList BLOB_CONTEXT_LIST;

	private JTabbedPane mCameraTabPane;
	private JPanel      mCameraPreviewPane;

	private final ProgressBarUpdateThread PROGRESS_BAR_UPDATE_THREAD;
	private JProgressBar mProgressBar;
	private String mProgressControllerId;
	
	public void setProgressBarId(final String progressControllerId) {
		mProgressControllerId = progressControllerId;
	}
	public JProgressBar getProgressBar() {
		return mProgressBar;
	}
	
	private String mContextFilename;
	
	public GuiMain(final int controllerTypeId, final String databaseName) {		
		CONTROLLER_TYPE_ID = controllerTypeId;
		
		DATABASE_NAME = databaseName;
		
		PROGRESS_BAR_UPDATE_THREAD = new ProgressBarUpdateThread(this);
		PROGRESS_BAR_UPDATE_THREAD.start();
		
		initDb();
		
		String dbNameFmt = DATABASE_NAME, databaseNamePath = DATABASE_NAME;
		
		if(dbNameFmt.contains(File.separator)) {
			dbNameFmt = dbNameFmt.substring(dbNameFmt.lastIndexOf(File.separator) + 1, dbNameFmt.length());
		}
		
		if(databaseNamePath.contains(File.separator)) {
			databaseNamePath = databaseNamePath.substring(0, databaseNamePath.lastIndexOf(File.separator) + 1);
		}
		
		BLOB_CONTEXT_LIST = new BlobContextList(databaseNamePath, dbNameFmt);
		
		createContextFilename();
		prepareAuditor();
		buildFrame();
	}

	private void prepareAuditor() {
		final String caseLogPath = FileUtils.AUDIT_DIRECTORY + FileUtils.getShortFilename(DATABASE_NAME);
		CaseAuditor.setCaseLogPath(caseLogPath);
	}

	private void createContextFilename() {
		mContextFilename = FileUtils.CONTEXT_DIRECTORY; 
		
		if(mContextFilename.endsWith(File.separator) == false) {
			mContextFilename += File.separator;
		}
		
		mContextFilename += FileUtils.getShortFilename(DATABASE_NAME) + "_context.txt";
		
		CaseMetadataWriter.createContextFilenameIfNeeded(mContextFilename);
	}
	
	public void initDb() {
		CaseSqliteDatabaseHelper dbHelper = CaseSqliteDatabaseHelper.getInstance(DATABASE_NAME);
		
		try {
			dbHelper.initDb(CaseSqliteDatabaseHelper.KEEP_EXISTING_DB_FILE);
		} catch (SqlJetException e) {
			e.printStackTrace();
			
			UtilsLegacy.displayMessageDialog("Notice", "Could not create database for case.\n" +
					"Closing application.");
			
			WindowRegistry.getInstance().closeAllActiveFrames();
			this.dispose();
		}
	}
	
	private void buildCameraPreviewPane() {		
		mCameraPreviewPane = new JPanel(new WrapLayout());
		mCameraPreviewPane.addContainerListener(new ContainerListener() {
			private boolean mIgnoreAddFlag;
			
			@Override
			public void componentAdded(ContainerEvent event) {
				if(mIgnoreAddFlag == true) {
					mIgnoreAddFlag = false;
					
					return;
				}
				
				for(int i = 0; i < mCameraPreviewPane.getComponentCount(); ++i) {
					if(mCameraPreviewPane.getComponent(i) instanceof JLabel) {
						mCameraPreviewPane.remove(i);
						mCameraPreviewPane.invalidate();
						mCameraPreviewPane.repaint();
						break;
					}
				}
			}
			@Override
			public void componentRemoved(ContainerEvent event) {
				if(mCameraPreviewPane.getComponentCount() == 0) {
					JLabel emptyLabel = new JLabel("Add a camera to view previews here.", SwingConstants.CENTER);
					
					mIgnoreAddFlag = true;
					mCameraPreviewPane.add(emptyLabel);
					mCameraPreviewPane.invalidate();
					mCameraPreviewPane.repaint();
				}
			}
		});
	}
	
	private void buildCameraTabPane() {		
		mCameraTabPane = new JTabbedPane();
		mCameraTabPane.addContainerListener(new ContainerListener() {
			private boolean mIgnoreAddFlag;
			
			@Override
			public void componentAdded(ContainerEvent event) {
				if(mIgnoreAddFlag == true) {
					mIgnoreAddFlag = false;
					
					return;
				}
				
				for(int i = 0; i < mCameraTabPane.getTabCount(); ++i) {
					if(mCameraTabPane.getTitleAt(i).equals("No Cameras") == true) {
						mCameraTabPane.remove(i);
						mCameraTabPane.invalidate();
						mCameraTabPane.repaint();
						
						break; 
					}
				}
			}
			
			@Override
			public void componentRemoved(ContainerEvent event) {
				if(mCameraTabPane.getComponentCount() == 1) { 
					JLabel emptyLabel = new JLabel("Add a camera to control and monitor video feeds here.", SwingConstants.CENTER);
					
					mIgnoreAddFlag = true;
					mCameraTabPane.addTab("No Cameras", null, emptyLabel, "Add a new camera to view controls.");
					mCameraTabPane.invalidate();
					mCameraTabPane.repaint();
				}
			}
		});
		
		mCameraTabPane.addChangeListener(new ChangeListener() {
		    public void stateChanged(ChangeEvent event) {		   
		    	final int selectedIndex = mCameraTabPane.getSelectedIndex();
		    	
		    	if(selectedIndex == INVALID_INDEX) {		    		
		    		return;
		    	}
		    	
		    	if(mCameraTabPane.getComponentCount() > 0) {
			    	final JComponent component = (JComponent) mCameraTabPane.getComponentAt(selectedIndex);
			    	
			    	if(component instanceof RemoteCameraController) {
			    		RemoteCameraController cameraController = (RemoteCameraController) component;
			    		cameraController.getIpOrUrlField().requestFocusInWindow();
			    	}
			    	else if(component instanceof LocalCameraController) {
			    		LocalCameraController cameraController = (LocalCameraController) component;
			    		cameraController.getLocalFileField().requestFocusInWindow();
			    	}
		    	}
		    }
		});
	}
	
	private void closeContextFileAndOpenGalleryAction(final String dbNameFmt) {
		BLOB_CONTEXT_LIST.closeContextFile();
		
		ImageGallery ig = new ImageGallery(DATABASE_NAME, FileUtils.CAPTURES_DIRECTORY + dbNameFmt, BLOB_CONTEXT_LIST);
		ig.build();
	}
	
	private void refreshProcessingDirectoryPrompt() {
		boolean containsTemporaryKeyFrameFiles = WindowsVideoFrameExtractorLegacy.containsTemporaryKeyFrameFiles();
		
		if(containsTemporaryKeyFrameFiles == true) {
			//final int refreshProcessingDirectoryChoice = UtilsLegacy.displayConfirmDialog("Processing Data", "Would you like to refresh processing data?\n" +
			//	"It is safe to do so if processing has finished.");
			
			//if(refreshProcessingDirectoryChoice == JOptionPane.OK_OPTION) {
			//	FileUtils.deleteDirectoryContents(new File(FileUtils.PROCESSING_DIRECTORY), false);
			//}

			final int refreshProcessingDirectoryChoice = UtilsLegacy.displayConfirmDialog("Processing Data", "Processing data exists. Open directory for manual cleanup?");
			
			if(refreshProcessingDirectoryChoice == JOptionPane.OK_OPTION) {
				new Thread() {
					@Override
					public void run() {	
						ThreadUtils.addThreadToHandleList("GuiMain OpenProcessingFolder", this);
						
						final File file = new File(FileUtils.PROCESSING_DIRECTORY);
						final Desktop desktop = Desktop.getDesktop();
						
						try {
							desktop.browse(file.toURI());
						} catch (IOException ioException) {
	
						}
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
		}
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
						ThreadUtils.addThreadToHandleList("GuiMain ExportWorkspace", this);
						
						ZipWorkspaceWizardWindow zipWorkspaceWizardWindow = new ZipWorkspaceWizardWindow();
					
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
						ThreadUtils.addThreadToHandleList("GuiMain Delete", this);
						
						DeleteDataWindow deleteDataWindow = new DeleteDataWindow(GuiMain.this);
												
						initDb();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Stop All Tasks"); 
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				performStopAllTasksAction(false, false, false);
			}	
		});
		menu.add(menuItem);
		
		
		menu.addSeparator();
		
		menuItem = new JMenuItem("Exit to Case Menu"); 
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						//ThreadUtils.addThreadToHandleList("GuiMain ExitCase", this);
						
						BLOB_CONTEXT_LIST.closeContextFile();
						performStopAllTasksAction(true, true, true);
						WindowRegistry.getInstance().closeAllActiveFrames();
						GuiMain.this.dispose();
						
						SelectCaseDialog selectCaseDialog = new SelectCaseDialog();

						/*
						HashMap<String, CameraController> controllerMap = CONTROLLER_REGISTRY.getControllerMap();
						
						//concurrent exception TODO
						for(Entry<String, CameraController> controllerEntry : controllerMap.entrySet()) {
							removeController(controllerEntry.getKey());	
						}
						*/
						
						//ThreadUtils.removeThreadFromHandleList(this);
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
		
		if(CONTROLLER_TYPE_ID == USE_LOCAL_CAMERA_CONTROLLER) {
			menu = new JMenu("Evidence");
			menuBar.add(menu);
			
			menuItem = new JMenuItem("Add Local Files");
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {	
					new Thread() {
						@Override
						public void run() {	
							ThreadUtils.addThreadToHandleList("GuiMain AddLoc", this);
							
							if(LocalFileRuntimeParams.getGlobalProcessLocalFileThread() == null) {
								final LocalFileWizardWindow localFileWizardWindow = new LocalFileWizardWindow(GuiMain.this);
							}
							else {
								//UtilsLegacy.displayMessageDialog("Notice", Utils.SOFTWARE_NAME + " is currently processing local files." +
								//	"\nPlease wait for the current task to finish.");		
								
								final int forceStopAllTasksChoice = UtilsLegacy.displayConfirmDialog("Notice", Utils.SOFTWARE_NAME + " is currently processing local files." +
										"\nStop all existing tasks and proceed?");
								
								if(forceStopAllTasksChoice == JOptionPane.OK_OPTION) {
									performStopAllTasksAction(true, true, true);
									refreshProcessingDirectoryPrompt();
									final LocalFileWizardWindow localFileWizardWindow = new LocalFileWizardWindow(GuiMain.this);
								} else {
									return;
								}
							}
							
							ThreadUtils.removeThreadFromHandleList(this);
						}
					}.start();
				}
				
			});
			menu.add(menuItem);
			
			menuItem = new JMenuItem("Get Video Details");
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {	
					new Thread() {
						@Override
						public void run() {	
							ThreadUtils.addThreadToHandleList("GuiMain GetVidDetails", this);
							
							LocalEvidenceMetadataWindow localEvidenceMetadataWindow = new LocalEvidenceMetadataWindow();
							localEvidenceMetadataWindow.performSelectFileAction();
							localEvidenceMetadataWindow.buildAndDisplay();
							
							ThreadUtils.removeThreadFromHandleList(this);
						}
					}.start();
				}
				
			});
			menu.add(menuItem);
			
			menuItem = new JMenuItem("Create Image Previews");
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {	
					new Thread() {
						@Override
						public void run() {	
							ThreadUtils.addThreadToHandleList("GuiMain CreateVidPreview", this);
							
							PreviewImageFileWizardWindow previewImageFileWizardWindow = new PreviewImageFileWizardWindow(GuiMain.this);
							
							ThreadUtils.removeThreadFromHandleList(this);
						}
					}.start();
				}
				
			});
			menu.add(menuItem);
		}

		menu.addSeparator();
		
		menuItem = new JMenuItem("Extract Video Frames");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {	
						ThreadUtils.addThreadToHandleList("GuiMain ExtractVideoFrames", this);
						
						final JFileChooser fileChooser = new JFileChooser(); 
						fileChooser.setMultiSelectionEnabled(false);
						final FileSelectVideoPreviewAccessory imagePreviewAccessory = new FileSelectVideoPreviewAccessory();
						fileChooser.setAccessory(imagePreviewAccessory);
						fileChooser.addPropertyChangeListener(imagePreviewAccessory);
						
						final int fileChooserResult = fileChooser.showOpenDialog(GuiMain.this);
								
						if(fileChooserResult == JFileChooser.APPROVE_OPTION) {
							final File selectedFile = fileChooser.getSelectedFile();
							
							ExtractVideoFramesDialog extractVideoFramesDialog = new ExtractVideoFramesDialog(GuiMain.this, selectedFile.getAbsolutePath());
							extractVideoFramesDialog.build();
						
							ThreadUtils.removeThreadFromHandleList(this);
						}
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Review Video Extractions");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {	
						ThreadUtils.addThreadToHandleList("GuiMain ReviewVidExtractions", this);
						
						final String extractionDirectory = FileUtils.performSelectFolderAction(FileUtils.EXTRACTS_DIRECTORY);

						ExtractImageGallery extractImageGallery = new ExtractImageGallery(mContextFilename, DATABASE_NAME, extractionDirectory);
						extractImageGallery.build();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Import Image Folder");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {	
						ThreadUtils.addThreadToHandleList("GuiMain ImportImageFolder", this);
						
						final String folderToImport = FileUtils.performSelectFolderAction();
							
						CopyImageToExtractWizard copyImageToExtractWizard = new CopyImageToExtractWizard();
						copyImageToExtractWizard.copyImagesToExtractFolder(folderToImport);
							
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menu.addSeparator();
		
		menuItem = new JMenuItem("Open Extraction Folder");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {	
						ThreadUtils.addThreadToHandleList("GuiMain OpenExtractionFolder", this);
						
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
			
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Open Redacted Folder");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {	
						ThreadUtils.addThreadToHandleList("GuiMain OpenRedactedFolder", this);
						
						final File file = new File(FileUtils.REDACT_DIRECTORY);
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
		
		menu = new JMenu("Database");
		menuBar.add(menu);
		
		menuItem = new JMenuItem("Search");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("GuiMain Search", this);
						
						DatabaseBrowseWindow dbBrowseWindow = new DatabaseBrowseWindow(DATABASE_NAME);
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Image Search");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("GuiMain ImageSearch", this);
						
						SearchCaptureFileWizardWindow searchCaptureFileWizardWindow = new SearchCaptureFileWizardWindow(DATABASE_NAME);
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
				
		menuItem = new JMenuItem("Open Detection Wizard");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("GuiMain OpenDetectionWizard", this);
						
						//DatabaseFaceCrawler databaseFaceCrawler = new DatabaseFaceCrawler(GuiMain.this, DATABASE_NAME);
						//DetectionSearchOptionsWindow faceSearchOptionsWindow = new DetectionSearchOptionsWindow(databaseFaceCrawler, "Face Search");
						//faceSearchOptionsWindow.build();
						
						DetectionModuleWizardWindow detectionModuleWizardWindow = new DetectionModuleWizardWindow(GuiMain.this, DATABASE_NAME);
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Open Extract Detection Wizard");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {	
						ThreadUtils.addThreadToHandleList("GuiMain OpenExtractDetectionWizard", this);
						
						final String extractionDirectory = FileUtils.performSelectFolderAction(FileUtils.EXTRACTS_DIRECTORY);
							
						ExtractDetectionModuleWizardWindow extractDetectionModuleWizardWindow = 
							new ExtractDetectionModuleWizardWindow(GuiMain.this, DATABASE_NAME, extractionDirectory);
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		/*
		menuItem = new JMenuItem("License Plate Search");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("GuiMain LicensePlateSearch", this);
						
						DatabaseLicensePlateCrawler databaseLicensePlateCrawler = new DatabaseLicensePlateCrawler(GuiMain.this, DATABASE_NAME);
						DetectionSearchOptionsWindow licensePlateSearchOptionsWindow = new DetectionSearchOptionsWindow(databaseLicensePlateCrawler, "License Plate Search");
						licensePlateSearchOptionsWindow.build();
												
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		*/
		
		menu.addSeparator();
		
		JMenu detectionSubmenu = new JMenu("Open Gallery");

		menuItem = new JMenuItem("Face Gallery");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("GuiMain OpenFaceGallery", this);
						
						DatabaseFaceCrawler databaseFaceCrawler = new DatabaseFaceCrawler(GuiMain.this, DATABASE_NAME);
						final String databaseFaceFolder = databaseFaceCrawler.getDetectionDatabaseFolder();
						File faceDirectory = new File(databaseFaceFolder);
												
						if(faceDirectory.exists() == true) {
							//ArrayList<String> allFacePaths = (ArrayList<String>) FileUtilsLegacy.parseDirectoryRecursiveForAll(faceDirectory.getAbsolutePath());
							//SearchImageGallery searchImageGallery = new SearchImageGallery(allFacePaths);
							//searchImageGallery.build();
							
							UpdatedImageGallery updatedImageGallery = new UpdatedImageGallery(faceDirectory.getAbsolutePath());
							updatedImageGallery.build();
						} else {
							Utils.displayMessageDialog("Not Processed", "No faces were found. Run a face search after processing evidence to view faces.");
						}
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		//menu.add(menuItem);
		detectionSubmenu.add(menuItem);
		
		menuItem = new JMenuItem("License Plate Gallery");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("GuiMain OpenLicensePlateGallery", this);
						
						DatabaseLicensePlateCrawler databaseLicensePlateCrawler = new DatabaseLicensePlateCrawler(GuiMain.this, DATABASE_NAME);
						final String databaseLicensePlateFolder = databaseLicensePlateCrawler.getDetectionDatabaseFolder();
						File licensePlateDirectory = new File(databaseLicensePlateFolder);
												
						if(licensePlateDirectory.exists() == true) {
							//ArrayList<String> allLicensePlatePaths = (ArrayList<String>) FileUtilsLegacy.parseDirectoryRecursiveForAll(licensePlateDirectory.getAbsolutePath());
							//SearchImageGallery searchImageGallery = new SearchImageGallery(allLicensePlatePaths);
							//searchImageGallery.build();
							
							UpdatedImageGallery updatedImageGallery = new UpdatedImageGallery(licensePlateDirectory.getAbsolutePath());
							updatedImageGallery.build();
						} else {
							Utils.displayMessageDialog("Not Processed", "No license plates were found. Run a license plate search after processing evidence to view license plates.");
						}
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		//menu.add(menuItem);
		detectionSubmenu.add(menuItem);
		
		menuItem = new JMenuItem("Pedestrian Gallery");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("GuiMain OpenPedestrianGallery", this);
						
						DatabasePedestrianCrawler databasePedestrianCrawler = new DatabasePedestrianCrawler(GuiMain.this, DATABASE_NAME);
						final String databasePedestrianFolder = databasePedestrianCrawler.getDetectionDatabaseFolder();
						File pedestrianDirectory = new File(databasePedestrianFolder);
												
						if(pedestrianDirectory.exists() == true) {
							UpdatedImageGallery updatedImageGallery = new UpdatedImageGallery(pedestrianDirectory.getAbsolutePath());
							updatedImageGallery.build();
						} else {
							Utils.displayMessageDialog("Not Processed", "No pedestrians were found. Run a pedestrian search after processing evidence to view pedestrians.");
						}
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		//menu.add(menuItem);
		detectionSubmenu.add(menuItem);
		
		menuItem = new JMenuItem("Car Gallery");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("GuiMain OpenCarGallery", this);
						
						DatabaseCarCrawler databaseCarCrawler = new DatabaseCarCrawler(GuiMain.this, DATABASE_NAME);
						final String databaseCarFolder = databaseCarCrawler.getDetectionDatabaseFolder();
						File carDirectory = new File(databaseCarFolder);
												
						if(carDirectory.exists() == true) {
							UpdatedImageGallery updatedImageGallery = new UpdatedImageGallery(carDirectory.getAbsolutePath());
							updatedImageGallery.build();
						} else {
							Utils.displayMessageDialog("Not Processed", "No cars were found. Run a car search after processing evidence to view cars.");
						}
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		//menu.add(menuItem);
		detectionSubmenu.add(menuItem);
		
		menuItem = new JMenuItem("Explicit Gallery");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("GuiMain OpenExplicitGallery", this);
						
						DatabaseExplicitCrawler databaseExplicitCrawler = new DatabaseExplicitCrawler(GuiMain.this, DATABASE_NAME);
						final String databaseExplicitFolder = databaseExplicitCrawler.getDetectionDatabaseFolder();
						File explicitDirectory = new File(databaseExplicitFolder);
												
						if(explicitDirectory.exists() == true) {
							UpdatedImageGallery updatedImageGallery = new UpdatedImageGallery(explicitDirectory.getAbsolutePath());
							updatedImageGallery.build();
						} else {
							Utils.displayMessageDialog("Not Processed", "No explicit images were found. Run an explicit search after processing evidence to view explicit images.");
						}
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		//menu.add(menuItem);
		detectionSubmenu.add(menuItem);
		
		menu.add(detectionSubmenu);
		
		menuItem = new JMenuItem("Open Database Folder");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("GuiMain OpenDb", this);
						
						final File file = new File(FileUtils.DATABASE_DIRECTORY);
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
		
		menu = new JMenu("Report");
		menuBar.add(menu);
		
		menuItem = new JMenuItem("Create");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {	
						ThreadUtils.addThreadToHandleList("GuiMain Create", this);
						
						String dbNameFmt = DATABASE_NAME;
						
						if(dbNameFmt.contains(File.separator)) {
							dbNameFmt = dbNameFmt.substring(dbNameFmt.lastIndexOf(File.separator) + 1, dbNameFmt.length());
						}
		
						boolean containsTemporaryKeyFrameFiles = WindowsVideoFrameExtractorLegacy.containsTemporaryKeyFrameFiles();
						
						if(containsTemporaryKeyFrameFiles == true) {
							Utils.displayMessageDialog("Notice", Utils.SOFTWARE_NAME + " is performing accelerated analysis. Report video times may not\n" +
								"be fully accurate until processing is complete.");
						}
						
						ImageGallery ig = new ImageGallery(DATABASE_NAME, FileUtils.CAPTURES_DIRECTORY + dbNameFmt, BLOB_CONTEXT_LIST);
						ig.build();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Create Quick Chart");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {	
						ThreadUtils.addThreadToHandleList("GuiMain Create", this);
						
						String dbNameFmt = DATABASE_NAME;
						
						if(dbNameFmt.contains(File.separator)) {
							dbNameFmt = dbNameFmt.substring(dbNameFmt.lastIndexOf(File.separator) + 1, dbNameFmt.length());
						}
		
						boolean containsTemporaryKeyFrameFiles = WindowsVideoFrameExtractorLegacy.containsTemporaryKeyFrameFiles();
						
						if(containsTemporaryKeyFrameFiles == true) {
							Utils.displayMessageDialog("Notice", Utils.SOFTWARE_NAME + " is performing accelerated analysis. Report video times may not\n" +
								"be fully accurate until processing is complete.");
						}
						
						SimpleChartGenerator simpleChartGenerator = new SimpleChartGenerator(DATABASE_NAME, FileUtils.CAPTURES_DIRECTORY + dbNameFmt, BLOB_CONTEXT_LIST);
						simpleChartGenerator.build();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Export");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("GuiMain ExportReport", this);
						
						ZipReportWizardWindow zipReportWizardWindow = new ZipReportWizardWindow();
					
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
						ThreadUtils.addThreadToHandleList("GuiMain OpenReport", this);
						
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
		
		menuItem = new JMenuItem("Open Capture Wizard");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {	
						ThreadUtils.addThreadToHandleList("GuiMain OpenExtractWizard", this);
						
						String dbNameFmt = DATABASE_NAME;
						
						if(dbNameFmt.contains(File.separator)) {
							dbNameFmt = dbNameFmt.substring(dbNameFmt.lastIndexOf(File.separator) + 1, dbNameFmt.length());
						}
						
						//if(dbNameFmt.contains(".")) {
						//	dbNameFmt = dbNameFmt.substring(0, dbNameFmt.lastIndexOf("."));
						//}
						
						FilterImageGallery ig = new FilterImageGallery(FileUtils.CAPTURES_DIRECTORY + dbNameFmt, BLOB_CONTEXT_LIST);
						ig.build();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Open Video Wizard");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("GuiMain OpenVideoWizard", this);
												
						EnhanceLocalFileWizardWindow enhanceLocalFileWizardWindow = new EnhanceLocalFileWizardWindow(mContextFilename);
						
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
						ThreadUtils.addThreadToHandleList("GuiMain OpenEnhancedVideo", this);
						
						EnhanceLocalVideoFilesThread.performOpenEnhancedVideoFolderAction();
												
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
		});
		menu.add(menuItem);
		
		menu = new JMenu("Merge");
		menuBar.add(menu);
		
		menuItem = new JMenuItem("Open Merge Wizard");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {	
						ThreadUtils.addThreadToHandleList("GuiMain OpenMergeWizard", this);
						
						MergeLocalFileWizardWindow mergeLocalFileWizardWindow = new MergeLocalFileWizardWindow(mContextFilename);
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Open Merged Video Folder");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("GuiMain OpenMergedVideo", this);
						
						final File file = new File(FileUtils.MERGED_DIRECTORY);
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
		
		menu = new JMenu("Preview");
		menuBar.add(menu);
		
		menuItem = new JMenuItem("Create");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {	
						ThreadUtils.addThreadToHandleList("GuiMain CreatePreview", this);
						
						PreviewLocalFileWizardWindow previewLocalFileWizardWindow = new PreviewLocalFileWizardWindow(GuiMain.this);

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
						ThreadUtils.addThreadToHandleList("GuiMain OpenPreview", this);
						
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
						ThreadUtils.addThreadToHandleList("GuiMain CreateResized", this);
						
						ResizeLocalFileWizardWindow resizeLocalFileWizardWindow = new ResizeLocalFileWizardWindow(GuiMain.this);

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
						ThreadUtils.addThreadToHandleList("GuiMain OpenResized", this);
						
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
						ThreadUtils.addThreadToHandleList("GuiMain CreateConverted", this);
						
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
						ThreadUtils.addThreadToHandleList("GuiMain OpenExports", this);
						
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
		
		menu = new JMenu("Notes");
		menuBar.add(menu);
		
		menuItem = new JMenuItem("Open Note Wizard");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {	
						ThreadUtils.addThreadToHandleList("GuiMain OpenNoteWizard", this);
						
						String dbNameFmt = DATABASE_NAME;
						
						if(dbNameFmt.contains(File.separator)) {
							dbNameFmt = dbNameFmt.substring(dbNameFmt.lastIndexOf(File.separator) + 1, dbNameFmt.length());
						}
						
						CaseNotesEditor caseNotesEditor = new CaseNotesEditor(dbNameFmt);
						caseNotesEditor.buildFrame();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Open Notes Folder");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("GuiMain OpenNotes", this);
						
						final File file = new File(FileUtils.NOTES_DIRECTORY + FileUtils.getShortFilename(DATABASE_NAME));
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

		menu = new JMenu("Audit");
		menuBar.add(menu);

		menuItem = new JMenuItem("Open Audit Log");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("AuditLogWindow OpenLog", this);

						AuditLogWindow auditLogWindow = new AuditLogWindow();
						auditLogWindow.buildAndDisplay();

						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}

		});
		menu.add(menuItem);

		menuItem = new JMenuItem("Open Audit Folder");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("GuiMain OpenAudit", this);

						final File file = new File(FileUtils.AUDIT_DIRECTORY + FileUtils.getShortFilename(DATABASE_NAME));
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
						ThreadUtils.addThreadToHandleList("GuiMain OpenDoc", this);
						
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
						ThreadUtils.addThreadToHandleList("GuiMain About", this);
						
						Utils.displayMessageDialog("About", Utils.SOFTWARE_NAME + " \nVersion: " + Utils.SOFTWARE_VERSION + "\n� " + Utils.AUTHOR_NAME);
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		this.setJMenuBar(menuBar);
	}
	
	private boolean hasActiveCameraControllers() {
		ConcurrentHashMap<String, CameraController> controllerMap = CONTROLLER_REGISTRY.getControllerMap();
		
		boolean hasActive = false;
		
		for(Entry<String, CameraController> controllerEntry : controllerMap.entrySet()) {
			if(controllerEntry.getValue().isRunning() == true) {
				hasActive = true;
				break;
			}
		}
		
		return hasActive;
	}
	
	private void closeAllVideoFeedsAction() {
		ConcurrentHashMap<String, CameraController> controllerMap = CONTROLLER_REGISTRY.getControllerMap();
		
		for(Entry<String, CameraController> controllerEntry : controllerMap.entrySet()) {
			removeController(controllerEntry.getKey());	
		}
	}
	
	private void stopAllTasksAction(boolean closeAllVideoFeeds) {
		ThreadUtils.stopAllKnownTasks(GuiMain.this);
		
		if(closeAllVideoFeeds == true) {
			closeAllVideoFeedsAction();
		} else {
			int removeAllPanelsResponse = Utils.displayConfirmDialog("Task Cleanup", "Close all video feeds?");
			
			if(removeAllPanelsResponse == JOptionPane.YES_OPTION) {
				closeAllVideoFeedsAction();
			}	
		}
		
		LocalFileRuntimeParams.setGlobalProcessLocalFileThread(null);
		LocalFileRuntimeParams.setCameraControllerPreferencesBundle(null);
		
		mProgressBar.setIndeterminate(false);
		mProgressBar.setMaximum(0);
		mProgressBar.setValue(0);
	}
	
	private void performStopAllTasksAction(boolean forceStopAllTasks, boolean closeAllVideoFeeds, boolean joinStopThread) {
		Thread stopAllTasksThread = new Thread() {
			@Override
			public void run() {	
				if(forceStopAllTasks == true) {
					stopAllTasksAction(closeAllVideoFeeds);
				} else {
					int verifyTaskCancellation = Utils.displayConfirmDialog("Confirm", "Cancel all active tasks? Operation may not be undone.");
				
					if(verifyTaskCancellation == JOptionPane.YES_OPTION) {
						stopAllTasksAction(closeAllVideoFeeds);
					}
				}
			}
		};
		stopAllTasksThread.start();
		
		if(joinStopThread == true) {
			try {
				stopAllTasksThread.join();
			} catch (InterruptedException interruptedException) {
				//interruptedException.printStackTrace();
			}
		}
	}
	
	private void buildFrame() {		
		this.setLayout(new BorderLayout());
		
		buildCameraPreviewPane();
		JScrollPane cameraPreviewScrollPane = new JScrollPane(mCameraPreviewPane);
		cameraPreviewScrollPane.setPreferredSize(new Dimension(360, 340));
		WindowUtils.setScrollBarIncrement(cameraPreviewScrollPane);
		
		buildCameraTabPane();
		addCameraPanel();
		JButton addCameraButton = new JButton("Add Camera");
		addCameraButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				addCameraPanel();
			}
		});
		addCameraButton.setPreferredSize(new Dimension(200, 50));

		JPanel cameraPreviewControlPanel = new JPanel();
		cameraPreviewControlPanel.setLayout(new BorderLayout());
		cameraPreviewControlPanel.add(addCameraButton, BorderLayout.NORTH);
		
		cameraPreviewControlPanel.add(cameraPreviewScrollPane, BorderLayout.CENTER);
		JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, cameraPreviewControlPanel, mCameraTabPane);
		mainSplitPane.setOneTouchExpandable(true);
        mainSplitPane.setContinuousLayout(true);
        
		this.add(mainSplitPane, BorderLayout.CENTER);
		
		mProgressBar = new JProgressBar();
		this.add(mProgressBar, BorderLayout.SOUTH);
		
		buildMenuBar();
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		this.setTitle(Utils.SOFTWARE_NAME);
		//WindowUtils.center(this);
		WindowUtils.maximize(this);
		WindowUtils.setFrameIcon(this);
		this.setVisible(true);
	}
	
	public void addCameraPanel() {		
		String newControllerId = CONTROLLER_REGISTRY.getNextId();
				
		CameraController newController = null;
		
		if(CONTROLLER_TYPE_ID == USE_REMOTE_CAMERA_CONTROLLER) {
			newController = new RemoteCameraController(newControllerId, this, DATABASE_NAME, BLOB_CONTEXT_LIST);
			((RemoteCameraController) newController).getIpOrUrlField().addAncestorListener(new RequestFocusListener());
			CONTROLLER_REGISTRY.putController(newControllerId, newController);			
		}
		else if(CONTROLLER_TYPE_ID == USE_LOCAL_CAMERA_CONTROLLER) {
			newController = new LocalCameraController(newControllerId, this, DATABASE_NAME, BLOB_CONTEXT_LIST);
			((LocalCameraController) newController).getLocalFileField().addAncestorListener(new RequestFocusListener());
			CONTROLLER_REGISTRY.putController(newControllerId, newController);
		}
		
		CameraPreviewPanel previewPanel = CONTROLLER_REGISTRY.getController(newControllerId).getPreviewPanel(); //newController.getPreviewPanel();
		previewPanel.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent event) {
				int cameraTabPaneIndex = -1;
				
				for(int i = 0; i < mCameraTabPane.getTabCount(); ++i) {
					if(mCameraTabPane.getTitleAt(i).equals(newControllerId) == true) {
						cameraTabPaneIndex = i;
						break;
					}
				}
				
				if(cameraTabPaneIndex != -1) {
					setFocusedTab(cameraTabPaneIndex);
				}
				
				PROGRESS_BAR_UPDATE_THREAD.setLocalViewProcessor(newControllerId);
			}

			@Override
			public void mouseEntered(MouseEvent event) {
				
			}

			@Override
			public void mouseExited(MouseEvent event) {
				
			}

			@Override
			public void mousePressed(MouseEvent event) {
				
			}

			@Override
			public void mouseReleased(MouseEvent event) {
				
			}
		});
		previewPanel.updateStatusString(newControllerId);
		
		mCameraTabPane.addTab(newControllerId, null, newController, newControllerId + " tab");
		mCameraTabPane.setTabComponentAt(mCameraTabPane.indexOfComponent(newController), getTitlePanel(mCameraTabPane, newController, previewPanel, newControllerId));
		mCameraPreviewPane.add(previewPanel);
		mCameraPreviewPane.revalidate();
	}
	
	private void setFocusedTab(final int previewIndex) {
		if(previewIndex < 0) {			
			return;
		}
		else if(previewIndex > (mCameraTabPane.getTabCount() - 1)) {			
			return;
		}
				
		mCameraTabPane.setSelectedIndex(previewIndex);
	}
	
	public void setFocusedTab(final String controllerId) {
		int cameraTabPaneIndex = -1;
		
		for(int i = 0; i < mCameraTabPane.getTabCount(); ++i) {
			if(mCameraTabPane.getTitleAt(i).equals(controllerId) == true) {
				cameraTabPaneIndex = i;
				break;
			}
		}
		
		if(cameraTabPaneIndex != -1) {
			setFocusedTab(cameraTabPaneIndex);
		}
		
		PROGRESS_BAR_UPDATE_THREAD.setLocalViewProcessor(controllerId);
	}
	
	private JPanel getTitlePanel(final JTabbedPane tabbedPane, final JComponent panel, final CameraPreviewPanel previewPanel, final String controllerId) {
		if(tabbedPane == null) {			
			return null;
		}
		
		if(panel == null) {		
			return null;
		}
		
		if(previewPanel == null) {			
			return null;
		}
		
		if(controllerId == null) {			
			return null;
		}
		else if(controllerId.isEmpty() == true) {			
			return null;
		}
		
		JPanel titlePanel = new JPanel();
		titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));
		titlePanel.setOpaque(false);

		JLabel titleLabel = new JLabel(controllerId);
		titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
		titlePanel.add(titleLabel);

		ImageButton imageButton = new ImageButton(ImageUtils.loadBufferedImage(FileUtils.GRAPHICS_DIRECTORY + "close_tab_icon.png"));
		imageButton.setPreferredSize(new Dimension(10, 10));
		
		imageButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent mouseEvent) {
				removeController(controllerId);
			}
		});
		titlePanel.add(imageButton);
		titlePanel.setName(controllerId);
		
		return titlePanel;
	}
	
	public CameraController getController(final String controllerId) {		
		return CONTROLLER_REGISTRY.getController(controllerId);
	}
	
	public boolean containsController(final String controllerId) {
		if(controllerId == null) {			
			return false;
		}
		else if(controllerId.isEmpty() == true) {			
			return false;
		}
		
		return (CONTROLLER_REGISTRY.getController(controllerId) == null);
	}
	
	private void clearProgressBarIfNecessary(final String controllerId) {
		if(controllerId.equals(mProgressControllerId) == true) {
			mProgressBar.setIndeterminate(false);
			mProgressBar.setMaximum(0);
			mProgressBar.setValue(0);
			mProgressBar.repaint();
		}
	}
	
	public void removeController(final String controllerId) {		
		if(controllerId == null) {	
			return;
		} else if(controllerId.isEmpty() == true) {	
			return;
		}

		//if(LocalFileRuntimeParams.getGlobalProcessLocalFileThread() != null) {
		//	LocalFileRuntimeParams.getGlobalProcessLocalFileThread().setRunning(false);
		//	LocalFileRuntimeParams.getGlobalProcessLocalFileThread().interrupt();
		//	LocalFileRuntimeParams.getGlobalProcessLocalFileThread().stop();
			
		//	LocalFileRuntimeParams.setGlobalProcessLocalFileThread(null);
		//}

		clearProgressBarIfNecessary(controllerId);

		CloseCameraControllerThread closeCameraControllerThread = new CloseCameraControllerThread(this, controllerId, mCameraTabPane, mCameraPreviewPane);
		closeCameraControllerThread.start();	

		try {
			closeCameraControllerThread.join();
		} catch (InterruptedException interruptedException) {
			//interruptedException.printStackTrace();
		} catch(Exception generalException) {
			//generalException.printStackTrace();
		}
	}
	
	public void stopController(final String controllerId) {	
		//if(LocalFileRuntimeParams.getGlobalProcessLocalFileThread() != null) {
		//	LocalFileRuntimeParams.getGlobalProcessLocalFileThread().setRunning(false);
		//	LocalFileRuntimeParams.getGlobalProcessLocalFileThread().interrupt();
		//	LocalFileRuntimeParams.getGlobalProcessLocalFileThread().stop();
			
			//LocalFileRuntimeParams.setGlobalProcessLocalFileThread(null);
		//}
		
		clearProgressBarIfNecessary(mProgressControllerId);
		
		final CameraController cameraController = CONTROLLER_REGISTRY.getController(controllerId);

		if (cameraController != null) {
			cameraController.stopRemoteCamera();
		}
	}
	
	public String getContextFilename() {
		return mContextFilename;
	}
	
	public void startController(final String controllerId) {		
		CONTROLLER_REGISTRY.getController(controllerId).processRemoteCamera();
	}
	
	public int getCameraTabCount() { 
		return mCameraTabPane.getTabCount();
	}
	
	public int getPreviewCount() { 
		return mCameraPreviewPane.getComponentCount();
	}
}

package com.keypointforensics.videotriage.gui.extract;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import com.keypointforensics.videotriage.casestore.CaseMetadataWriter;
import com.keypointforensics.videotriage.detect.HaarDetector;
import com.keypointforensics.videotriage.detect.SimpleFalsePositiveRemover;
import com.keypointforensics.videotriage.detect.SimpleHaarDetector;
import com.keypointforensics.videotriage.filter.blocking.BlockingExtractor;
import com.keypointforensics.videotriage.filter.discretewavelet.DWNoiseVarExtractor;
import com.keypointforensics.videotriage.filter.errorlevel.ErrorLevelAnalysisExtractor;
import com.keypointforensics.videotriage.filter.errorlevel.ErrorLevelAnalysisFilterRunner;
import com.keypointforensics.videotriage.filter.errorlevel.Pixel;
import com.keypointforensics.videotriage.gui.imagepanel.ScalableAnnotationImagePanel;
import com.keypointforensics.videotriage.legacy.FileUtilsLegacy;
import com.keypointforensics.videotriage.legacy.UtilsLegacy;
import com.keypointforensics.videotriage.progress.ProgressBundle;
import com.keypointforensics.videotriage.util.CascadeUtils;
import com.keypointforensics.videotriage.util.CursorUtils;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.FontUtils;
import com.keypointforensics.videotriage.util.ImageUtils;
import com.keypointforensics.videotriage.util.ProgressUtils;
import com.keypointforensics.videotriage.util.SystemUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.Utils;
import com.keypointforensics.videotriage.util.WebUtils;
import com.keypointforensics.videotriage.util.WindowUtils;
import com.keypointforensics.videotriage.window.ChildWindowList;
import com.keypointforensics.videotriage.window.WindowRegistry;

public class ExtractImageGallery extends JFrame {

	/*
	 * Author: Daniel Puckowski
	 */

	/**
	 * 
	 */
	protected static final long serialVersionUID = 232646536271988938L;

	private final String REMOVE_REDACTION_TEXT = "Remove Redaction";
	private final String REMOVING_REDACTION_TEXT = "Removing Redaction";

	private final String DATABASE_NAME;

	protected static final String NO_PREVIEW_AVAILABLE_IMAGE_PATH = FileUtils.GRAPHICS_DIRECTORY
			+ "no_preview_available.jpg";

	protected final JPanel contentPanel = new JPanel();

	private ScalableAnnotationImagePanel mImagePanel;

	protected JLabel mPageLabel;

	protected int currentIndex;
	protected int maxIndex;

	protected ArrayList<String> imgPaths;

	private String mContextFilename;
	private String mPath;

	private JButton mPreviousButton;
	private JButton mNextButton;
	private JButton mRevertButton;
	private JButton mRedactFacesButton;
	private JButton mAddToReportButton;
	private JButton mUndoActionButton;
	private JButton mRemoveRedactionButton;

	protected boolean mDisableFurtherActions;

	private JMenuBar mMenuBar;

	private JMenuItem mExitMenuItem;
	private JMenuItem mAboutMenuItem;
	private JMenuItem mPreviousMenuItem;
	private JMenuItem mNextMenuItem;
	private JMenuItem mRevertMenuItem;
	private JMenuItem mAddToReportMenuItem;
	private JMenuItem mUndoActionMenuItem;
	private JMenuItem mGoToFrameMenuItem;
	private JMenuItem mRedactFacesMenuItem;
	private JMenuItem mRedactVideoMenuItem;
	private JMenuItem mMergeImagesMenuItem;
	private JMenuItem mDocumentationMenuItem;
	private JMenuItem mRemoveRedactionMenuItem;
	private JMenuItem mRedactVideoWithoutMergeMenuItem;
	private JMenuItem mErrorLevelAnalysisPfMenuItem;
	private JMenuItem mErrorLevelAnalysisGhMenuItem;
	private JMenuItem mBlockingArtifactMenuItem;
	private JMenuItem mDiscreteWaveletMenuItem;

	private HashMap<String, String> mModifiedPathMap;
	private boolean mRedactingVideo;

	protected ChildWindowList mChildWindowList;

	public ExtractImageGallery(final String contextFilename, final String databaseName, final String path) {
		DATABASE_NAME = FileUtils.getShortFilename(databaseName);

		this.setLayout(new BorderLayout());

		mModifiedPathMap = new HashMap<String, String>();

		mChildWindowList = new ChildWindowList();

		if (path == null) {

		} else if (path.startsWith(FileUtils.EXTRACTS_DIRECTORY) == false) {
			Utils.displayMessageDialog("Directory Error",
					"Please select a valid video extraction directory for review.");
		} else {
			mPath = path;
			imgPaths = (ArrayList<String>) FileUtilsLegacy.parseDirectoryRecursiveForImages(path);
			Collections.sort(imgPaths);
		}

		mContextFilename = contextFilename;

		mDisableFurtherActions = false;
		mRedactingVideo = false;

		WindowRegistry.getInstance().registerFrame(this, "ExtractImageGallery");
	}

	public void addModifiedImagePath(final String modifiedImagePath, final String pathOfSavedImage, boolean forceSave) {
		if (mRedactingVideo == false) {
			mModifiedPathMap.put(modifiedImagePath, pathOfSavedImage);

			final String outputPath = getTemporaryRedactionForCurrentIndex();

			if (forceSave == true) {
				ImageUtils.saveBufferedImage(mImagePanel.getImage(), outputPath);
			} else {
				if (new File(outputPath).exists() == false) {
					ImageUtils.saveBufferedImage(mImagePanel.getImage(), outputPath);
				}
			}
		}
	}

	public void addModifiedImagePath(final String modifiedImagePath, boolean forceSave) {
		if (mRedactingVideo == false) {
			final String pathOfSavedImage = getTemporaryRedactionForCurrentIndex();

			mModifiedPathMap.put(modifiedImagePath, pathOfSavedImage);

			if (forceSave == true) {
				ImageUtils.saveBufferedImage(mImagePanel.getImage(), pathOfSavedImage);
			} else {
				if (new File(pathOfSavedImage).exists() == false) {
					ImageUtils.saveBufferedImage(mImagePanel.getImage(), pathOfSavedImage);
				}
			}
		}
	}

	private void disableFurtherActions() {
		mMenuBar.setEnabled(false);

		mPreviousButton.setEnabled(false);
		mNextButton.setEnabled(false);
		mRevertButton.setEnabled(false);
		mAddToReportButton.setEnabled(false);
		mRedactFacesButton.setEnabled(false);
		mUndoActionButton.setEnabled(false);
		mRemoveRedactionButton.setEnabled(false);

		mExitMenuItem.setEnabled(false);
		mAboutMenuItem.setEnabled(false);
		mPreviousMenuItem.setEnabled(false);
		mNextMenuItem.setEnabled(false);
		mRevertMenuItem.setEnabled(false);
		mAddToReportMenuItem.setEnabled(false);
		mUndoActionMenuItem.setEnabled(false);
		mGoToFrameMenuItem.setEnabled(false);
		mRedactFacesMenuItem.setEnabled(false);
		mDocumentationMenuItem.setEnabled(false);
		mRedactVideoMenuItem.setEnabled(false);
		mMergeImagesMenuItem.setEnabled(false);
		mRemoveRedactionMenuItem.setEnabled(false);
		mRedactVideoWithoutMergeMenuItem.setEnabled(false);
		mErrorLevelAnalysisGhMenuItem.setEnabled(false);
		mErrorLevelAnalysisPfMenuItem.setEnabled(false);
		mBlockingArtifactMenuItem.setEnabled(false);
		mDiscreteWaveletMenuItem.setEnabled(false);

		mDisableFurtherActions = true;
	}

	private void enableFurtherActions() {
		mMenuBar.setEnabled(true);

		mPreviousButton.setEnabled(true);
		mNextButton.setEnabled(true);
		mRevertButton.setEnabled(true);
		mAddToReportButton.setEnabled(true);
		mRedactFacesButton.setEnabled(true);
		mUndoActionButton.setEnabled(true);
		mRemoveRedactionButton.setEnabled(true);

		mExitMenuItem.setEnabled(true);
		mAboutMenuItem.setEnabled(true);
		mPreviousMenuItem.setEnabled(true);
		mNextMenuItem.setEnabled(true);
		mRevertMenuItem.setEnabled(true);
		mAddToReportMenuItem.setEnabled(true);
		mUndoActionMenuItem.setEnabled(true);
		mGoToFrameMenuItem.setEnabled(true);
		mRedactFacesMenuItem.setEnabled(true);
		mDocumentationMenuItem.setEnabled(true);
		mRedactVideoMenuItem.setEnabled(true);
		mMergeImagesMenuItem.setEnabled(true);
		mRemoveRedactionMenuItem.setEnabled(true);
		mRedactVideoWithoutMergeMenuItem.setEnabled(true);
		mErrorLevelAnalysisGhMenuItem.setEnabled(true);
		mErrorLevelAnalysisPfMenuItem.setEnabled(true);
		mBlockingArtifactMenuItem.setEnabled(true);
		mDiscreteWaveletMenuItem.setEnabled(true);

		mDisableFurtherActions = false;
	}

	private void buildMenuBar() {
		JMenu menu;

		mMenuBar = new JMenuBar();

		menu = new JMenu("File");
		mMenuBar.add(menu);

		mExitMenuItem = new JMenuItem("Exit");
		mExitMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ExtractImageGallery Exit", this);

						ExtractImageGallery.this.dispose();

						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}

		});
		menu.add(mExitMenuItem);

		menu = new JMenu("Results");
		mMenuBar.add(menu);

		mPreviousMenuItem = new JMenuItem("Previous");
		mPreviousMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (mDisableFurtherActions == true) {

					return;
				}

				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ExtractImageGallery Prev", this);

						disableFurtherActions();

						CursorUtils.setBusyCursor(ExtractImageGallery.this);

						performPreviousAction();

						CursorUtils.setDefaultCursor(ExtractImageGallery.this);

						enableFurtherActions();

						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}

		});
		menu.add(mPreviousMenuItem);

		mNextMenuItem = new JMenuItem("Next");
		mNextMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (mDisableFurtherActions == true) {

					return;
				}

				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ExtractImageGallery Next", this);

						disableFurtherActions();

						CursorUtils.setBusyCursor(ExtractImageGallery.this);

						performNextAction();

						CursorUtils.setDefaultCursor(ExtractImageGallery.this);

						enableFurtherActions();

						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}

		});
		menu.add(mNextMenuItem);

		mGoToFrameMenuItem = new JMenuItem("Go To Frame");
		mGoToFrameMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (mDisableFurtherActions == true) {

					return;
				}

				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ExtractImageGallery GoToFrame", this);

						disableFurtherActions();

						CursorUtils.setBusyCursor(ExtractImageGallery.this);

						performGoToFrameAction();

						CursorUtils.setDefaultCursor(ExtractImageGallery.this);

						enableFurtherActions();

						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}

		});
		menu.add(mGoToFrameMenuItem);

		menu.addSeparator();

		mRedactFacesMenuItem = new JMenuItem("Redact Faces");
		mRedactFacesMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (mDisableFurtherActions == true) {

					return;
				}

				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ExtractImageGallery RedactFaces", this);

						disableFurtherActions();

						CursorUtils.setBusyCursor(ExtractImageGallery.this);

						performAutomaticFaceRedactionAction();

						CursorUtils.setDefaultCursor(ExtractImageGallery.this);

						enableFurtherActions();

						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}

		});
		menu.add(mRedactFacesMenuItem);

		mRedactVideoWithoutMergeMenuItem = new JMenuItem("Redact All Faces");
		mRedactVideoWithoutMergeMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (mDisableFurtherActions == true) {

					return;
				}

				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ExtractImageGallery RedactVideoWithoutMerge", this);

						disableFurtherActions();

						CursorUtils.setBusyCursor(ExtractImageGallery.this);

						performAutomaticVideoRedactionWithoutMergeAction();

						CursorUtils.setDefaultCursor(ExtractImageGallery.this);

						enableFurtherActions();

						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}

		});
		menu.add(mRedactVideoWithoutMergeMenuItem);

		mRedactVideoMenuItem = new JMenuItem("Create Redacted Video");
		mRedactVideoMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (mDisableFurtherActions == true) {

					return;
				}

				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ExtractImageGallery RedactVideo", this);

						disableFurtherActions();

						CursorUtils.setBusyCursor(ExtractImageGallery.this);

						performAutomaticVideoRedactionAction();

						CursorUtils.setDefaultCursor(ExtractImageGallery.this);

						enableFurtherActions();

						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}

		});
		menu.add(mRedactVideoMenuItem);

		mMergeImagesMenuItem = new JMenuItem("Merge Images");
		mMergeImagesMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (mDisableFurtherActions == true) {

					return;
				}

				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ExtractImageGallery MergeImages", this);

						disableFurtherActions();

						CursorUtils.setBusyCursor(ExtractImageGallery.this);

						performRedactedSlidesMergerAction();

						CursorUtils.setDefaultCursor(ExtractImageGallery.this);

						enableFurtherActions();

						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}

		});
		menu.add(mMergeImagesMenuItem);

		menu = new JMenu("Edit");
		mMenuBar.add(menu);

		mUndoActionMenuItem = new JMenuItem("Undo");
		mUndoActionMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (mDisableFurtherActions == true) {

					return;
				}

				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ExtractImageGallery Undo", this);

						disableFurtherActions();

						CursorUtils.setBusyCursor(ExtractImageGallery.this);

						performUndoAction();

						CursorUtils.setDefaultCursor(ExtractImageGallery.this);

						enableFurtherActions();

						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}

		});
		menu.add(mUndoActionMenuItem);

		mRevertMenuItem = new JMenuItem("Revert");
		mRevertMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (mDisableFurtherActions == true) {

					return;
				}

				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ExtractImageGallery Revert", this);

						disableFurtherActions();

						CursorUtils.setBusyCursor(ExtractImageGallery.this);

						performRevertAction();

						CursorUtils.setDefaultCursor(ExtractImageGallery.this);

						enableFurtherActions();

						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}

		});
		menu.add(mRevertMenuItem);

		mAddToReportMenuItem = new JMenuItem("Add to Report");
		mAddToReportMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (mDisableFurtherActions == true) {

					return;
				}

				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ExtractImageGallery AddToReport", this);

						disableFurtherActions();

						CursorUtils.setBusyCursor(ExtractImageGallery.this);

						performAddToReportAction();

						CursorUtils.setDefaultCursor(ExtractImageGallery.this);

						enableFurtherActions();

						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}

		});
		menu.add(mAddToReportMenuItem);

		mRemoveRedactionMenuItem = new JMenuItem(REMOVE_REDACTION_TEXT);
		mRemoveRedactionMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (mDisableFurtherActions == true) {

					return;
				}

				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ExtractImageGallery RemoveRedactionMenu", this);

						disableFurtherActions();

						CursorUtils.setBusyCursor(ExtractImageGallery.this);

						performRemoveRedactionMenuAction();

						CursorUtils.setDefaultCursor(ExtractImageGallery.this);

						enableFurtherActions();

						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}

		});
		menu.add(mRemoveRedactionMenuItem);

		menu = new JMenu("Filter");
		mMenuBar.add(menu);

		mErrorLevelAnalysisPfMenuItem = new JMenuItem("Error Level Analysis Map");
		mErrorLevelAnalysisPfMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (mDisableFurtherActions == true) {

					return;
				}

				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ExtractImageGallery ErrorLevelAnalysisPf", this);

						disableFurtherActions();

						CursorUtils.setBusyCursor(ExtractImageGallery.this);

						performErrorLevelAnalysisMapAction();

						CursorUtils.setDefaultCursor(ExtractImageGallery.this);

						enableFurtherActions();

						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}

		});
		menu.add(mErrorLevelAnalysisPfMenuItem);

		mErrorLevelAnalysisGhMenuItem = new JMenuItem("Error Level Analysis Highlight");
		mErrorLevelAnalysisGhMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (mDisableFurtherActions == true) {

					return;
				}

				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ExtractImageGallery ErrorLevelAnalysisGh", this);

						disableFurtherActions();

						CursorUtils.setBusyCursor(ExtractImageGallery.this);

						performErrorLevelAnalysisHighlightAction();

						CursorUtils.setDefaultCursor(ExtractImageGallery.this);

						enableFurtherActions();

						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}

		});
		menu.add(mErrorLevelAnalysisGhMenuItem);

		mBlockingArtifactMenuItem = new JMenuItem("Blocking Artifact Inconsistencies");
		mBlockingArtifactMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (mDisableFurtherActions == true) {

					return;
				}

				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ExtractImageGallery BlockingArtifact", this);

						disableFurtherActions();

						CursorUtils.setBusyCursor(ExtractImageGallery.this);

						performBlockingArtifactInconsistenciesAction();

						CursorUtils.setDefaultCursor(ExtractImageGallery.this);

						enableFurtherActions();

						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}

		});
		menu.add(mBlockingArtifactMenuItem);

		mDiscreteWaveletMenuItem = new JMenuItem("Discrete Wavelet Noise Variance");
		mDiscreteWaveletMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (mDisableFurtherActions == true) {

					return;
				}

				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ExtractImageGallery DiscreteWaveletVariance", this);

						disableFurtherActions();

						CursorUtils.setBusyCursor(ExtractImageGallery.this);

						performDiscreteWaveletNoiseVarianceAction();

						CursorUtils.setDefaultCursor(ExtractImageGallery.this);

						enableFurtherActions();

						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}

		});
		menu.add(mDiscreteWaveletMenuItem);

		menu = new JMenu("Help");
		mMenuBar.add(menu);

		mDocumentationMenuItem = new JMenuItem("Open Documentation");
		mDocumentationMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ExtractImageGallery OpenDoc", this);

						try {
							WebUtils.openWebpage(new URL(WebUtils.URL_STRING_DOCUMENTATION));
						} catch (MalformedURLException malformedUrlException) {
							// malformedUrlException.printStackTrace();
						}

						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}

		});
		menu.add(mDocumentationMenuItem);

		mAboutMenuItem = new JMenuItem("About");
		mAboutMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (mDisableFurtherActions == true) {

					return;
				}

				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ExtractImageGallery About", this);

						Utils.displayMessageDialog("About", Utils.SOFTWARE_NAME + " \nVersion: "
								+ Utils.SOFTWARE_VERSION + "\n� " + Utils.AUTHOR_NAME);

						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}

		});
		menu.add(mAboutMenuItem);

		this.setJMenuBar(mMenuBar);
	}

	public void build() {
		buildMenuBar();

		contentPanel.setLayout(new BorderLayout());

		JTabbedPane imageTabPane = new JTabbedPane();
		imageTabPane.addTab("Extraction Results", contentPanel);

		maxIndex = imgPaths.size();
		currentIndex = 0;

		mPreviousButton = new JButton("Previous");
		mPreviousButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (mDisableFurtherActions == true) {

					return;
				}

				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ExtractImageGallery Prev", this);

						disableFurtherActions();

						CursorUtils.setBusyCursor(ExtractImageGallery.this);

						performPreviousAction();

						CursorUtils.setDefaultCursor(ExtractImageGallery.this);

						enableFurtherActions();

						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
		});

		mUndoActionButton = new JButton("Undo");
		mUndoActionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (mDisableFurtherActions == true) {

					return;
				}

				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ExtractImageGallery Undo", this);

						disableFurtherActions();

						CursorUtils.setBusyCursor(ExtractImageGallery.this);

						performUndoAction();

						CursorUtils.setDefaultCursor(ExtractImageGallery.this);

						enableFurtherActions();

						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
		});

		mRevertButton = new JButton("Revert");
		mRevertButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (mDisableFurtherActions == true) {

					return;
				}

				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ExtractImageGallery Revert", this);

						disableFurtherActions();

						CursorUtils.setBusyCursor(ExtractImageGallery.this);

						performRevertAction();

						CursorUtils.setDefaultCursor(ExtractImageGallery.this);

						enableFurtherActions();

						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
		});

		mRedactFacesButton = new JButton("Redact Faces");
		mRedactFacesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (mDisableFurtherActions == true) {

					return;
				}

				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ExtractImageGallery RedactFaces", this);

						disableFurtherActions();

						CursorUtils.setBusyCursor(ExtractImageGallery.this);

						performAutomaticFaceRedactionAction();

						CursorUtils.setDefaultCursor(ExtractImageGallery.this);

						enableFurtherActions();

						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
		});

		mAddToReportButton = new JButton("Add to Report");
		mAddToReportButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (mDisableFurtherActions == true) {

					return;
				}

				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ExtractImageGallery AddToReport", this);

						disableFurtherActions();

						CursorUtils.setBusyCursor(ExtractImageGallery.this);

						performAddToReportAction();

						CursorUtils.setDefaultCursor(ExtractImageGallery.this);

						enableFurtherActions();

						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
		});

		mRemoveRedactionButton = new JButton(REMOVE_REDACTION_TEXT);
		mRemoveRedactionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (mDisableFurtherActions == true) {

					return;
				}

				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ExtractImageGallery RemoveRedaction", this);

						disableFurtherActions();

						CursorUtils.setBusyCursor(ExtractImageGallery.this);

						performRemoveRedactionAction();

						CursorUtils.setDefaultCursor(ExtractImageGallery.this);

						enableFurtherActions();

						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
		});

		mNextButton = new JButton("Next");
		mNextButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (mDisableFurtherActions == true) {

					return;
				}

				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("ExtractImageGallery Next", this);

						disableFurtherActions();

						CursorUtils.setBusyCursor(ExtractImageGallery.this);

						performNextAction();

						CursorUtils.setDefaultCursor(ExtractImageGallery.this);

						enableFurtherActions();

						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(mPreviousButton);
		buttonPanel.add(mUndoActionButton);
		buttonPanel.add(mRevertButton);
		buttonPanel.add(mNextButton);
		buttonPanel.add(mRedactFacesButton);
		buttonPanel.add(mAddToReportButton);
		buttonPanel.add(mRemoveRedactionButton);

		JPanel buttonAndPagePanel = new JPanel();
		buttonAndPagePanel.setLayout(new BorderLayout());

		buttonAndPagePanel.add(buttonPanel, BorderLayout.CENTER);

		mPageLabel = new JLabel();
		mPageLabel.setHorizontalAlignment(SwingConstants.CENTER);
		mPageLabel.setFont(FontUtils.DEFAULT_FONT);
		mPageLabel.setText(getUpdatedPageLabelText());

		buttonAndPagePanel.add(mPageLabel, BorderLayout.SOUTH);

		JPanel galleryPane = new JPanel();
		galleryPane.setLayout(new BorderLayout());
		galleryPane.add(imageTabPane, BorderLayout.CENTER);
		galleryPane.add(buttonAndPagePanel, BorderLayout.SOUTH);

		mImagePanel = new ScalableAnnotationImagePanel(ExtractImageGallery.this, true);
		refresh();

		contentPanel.add(mImagePanel);

		this.add(galleryPane, BorderLayout.CENTER);

		WindowUtils.setFrameIcon(this);
		this.setTitle("Extraction Results");
		this.setPreferredSize(new Dimension(1400, 1000));
		this.pack();
		WindowUtils.maximize(this);
		this.setVisible(true);
		// WindowUtils.center(this);
	}

	private void setRemoveRedactionText(final String removeRedactionText) {
		mRemoveRedactionMenuItem.setText(removeRedactionText);
		mRemoveRedactionMenuItem.revalidate();

		mRemoveRedactionButton.setText(removeRedactionText);
		mRemoveRedactionButton.revalidate();
	}

	private void performRemoveRedactionMenuAction() {
		if (mRemoveRedactionMenuItem.getText().equals(REMOVE_REDACTION_TEXT) == true) {
			mImagePanel.setRemovingRedactions(true);

			/*
			 * mRemoveRedactionMenuItem.setText(REMOVING_REDACTION_TEXT);
			 * mRemoveRedactionMenuItem.revalidate();
			 * 
			 * mRemoveRedactionButton.setText(REMOVING_REDACTION_TEXT);
			 * mRemoveRedactionButton.revalidate();
			 */

			setRemoveRedactionText(REMOVING_REDACTION_TEXT);
		} else {
			mImagePanel.setRemovingRedactions(false);

			/*
			 * mRemoveRedactionMenuItem.setText(REMOVE_REDACTION_TEXT);
			 * mRemoveRedactionMenuItem.revalidate();
			 * 
			 * mRemoveRedactionButton.setText(REMOVE_REDACTION_TEXT);
			 * mRemoveRedactionButton.revalidate();
			 */

			setRemoveRedactionText(REMOVE_REDACTION_TEXT);
		}
	}

	private void performRemoveRedactionAction() {
		if (mRemoveRedactionButton.getText().equals(REMOVE_REDACTION_TEXT) == true) {
			mImagePanel.setRemovingRedactions(true);

			/*
			 * mRemoveRedactionMenuItem.setText(REMOVING_REDACTION_TEXT);
			 * mRemoveRedactionMenuItem.revalidate();
			 * 
			 * mRemoveRedactionButton.setText(REMOVING_REDACTION_TEXT);
			 * mRemoveRedactionButton.revalidate();
			 */

			setRemoveRedactionText(REMOVING_REDACTION_TEXT);
		} else {
			mImagePanel.setRemovingRedactions(false);

			/*
			 * mRemoveRedactionMenuItem.setText(REMOVE_REDACTION_TEXT);
			 * mRemoveRedactionMenuItem.revalidate();
			 * 
			 * mRemoveRedactionButton.setText(REMOVE_REDACTION_TEXT);
			 * mRemoveRedactionButton.revalidate();
			 */

			setRemoveRedactionText(REMOVE_REDACTION_TEXT);
		}
	}

	private void performDiscreteWaveletNoiseVarianceAction() {
		ProgressBundle blockingProgressBundle = ProgressUtils
				.getIndeterminateProgressBundle("Discrete Wavelet Noise Progress...");
		// elaProgressBundle.progressBar.setIndeterminate(true);
		blockingProgressBundle.progressBar.repaint();

		try {
			DWNoiseVarExtractor discreteWaveletExtractor = new DWNoiseVarExtractor(imgPaths.get(currentIndex));
			BufferedImage blockingMap = discreteWaveletExtractor.displaySurface;

			final String temporaryImage = FileUtils.TEMPORARY_DIRECTORY + Utils.getTimeStamp() + "_blocking_temp.jpg";
			ImageUtils.saveBufferedImage(blockingMap, temporaryImage);
			mImagePanel.update(ImageUtils.loadBufferedImage(new File(temporaryImage)), imgPaths.get(currentIndex));

			FileUtils.deleteFile(new File(temporaryImage));
		} catch (IOException ioException) {
			// ioException.printStackTrace();
		}

		blockingProgressBundle.frame.dispose();
	}

	private void performBlockingArtifactInconsistenciesAction() {
		ProgressBundle blockingProgressBundle = ProgressUtils
				.getIndeterminateProgressBundle("Blocking Artifact Progress...");
		// elaProgressBundle.progressBar.setIndeterminate(true);
		blockingProgressBundle.progressBar.repaint();

		try {
			BlockingExtractor blockingArtifactExtractor = new BlockingExtractor(imgPaths.get(currentIndex));
			BufferedImage blockingMap = blockingArtifactExtractor.displaySurface;

			final String temporaryImage = FileUtils.TEMPORARY_DIRECTORY + Utils.getTimeStamp() + "_blocking_temp.jpg";
			ImageUtils.saveBufferedImage(blockingMap, temporaryImage);
			mImagePanel.update(ImageUtils.loadBufferedImage(new File(temporaryImage)), imgPaths.get(currentIndex));

			FileUtils.deleteFile(new File(temporaryImage));
		} catch (IOException ioException) {
			// ioException.printStackTrace();
		}

		blockingProgressBundle.frame.dispose();
	}

	private void performErrorLevelAnalysisMapAction() {
		ProgressBundle elaProgressBundle = ProgressUtils
				.getIndeterminateProgressBundle("Error Level Analysis Progress...");
		// elaProgressBundle.progressBar.setIndeterminate(true);
		elaProgressBundle.progressBar.repaint();

		try {
			ErrorLevelAnalysisExtractor elaExtractor = new ErrorLevelAnalysisExtractor(imgPaths.get(currentIndex));
			BufferedImage elaMap = elaExtractor.displaySurface;

			final String temporaryImage = FileUtils.TEMPORARY_DIRECTORY + Utils.getTimeStamp()
					+ "_error_level_temp.jpg";
			ImageUtils.saveBufferedImage(elaMap, temporaryImage);
			mImagePanel.update(ImageUtils.loadBufferedImage(new File(temporaryImage)), imgPaths.get(currentIndex));

			FileUtils.deleteFile(new File(temporaryImage));
		} catch (IOException ioException) {
			// ioException.printStackTrace();
		}

		elaProgressBundle.frame.dispose();
	}

	private void performErrorLevelAnalysisHighlightAction() {
		ProgressBundle elaProgressBundle = ProgressUtils
				.getIndeterminateProgressBundle("Error Level Analysis Progress...");
		elaProgressBundle.progressBar.setIndeterminate(true);
		elaProgressBundle.progressBar.repaint();

		ErrorLevelAnalysisFilterRunner errorLevelAnalysisFilterRunner = new ErrorLevelAnalysisFilterRunner();
		mImagePanel.update(errorLevelAnalysisFilterRunner.runELA(new File(imgPaths.get(currentIndex)),
				imgPaths.get(currentIndex), Pixel.MAGENTA.RGB()), imgPaths.get(currentIndex));

		elaProgressBundle.frame.dispose();
	}

	public void performAutomaticFaceRedactionAction() {
		if (imgPaths.isEmpty() == true) {
			return;
		}

		final String pathToDraw = imgPaths.get(currentIndex);

		ArrayList<String> allCapturePaths = new ArrayList<String>();
		allCapturePaths.add(pathToDraw);

		SimpleHaarDetector mHaarDetector = new SimpleHaarDetector(SystemUtils.getNumberOfSystemCores());

		ArrayList<Rectangle> faceRectangles = mHaarDetector.performSearch(CascadeUtils.HAAR_CASCADE_FACE_DEFAULT,
				allCapturePaths);

		mImagePanel.addRedactionRectangles(faceRectangles);
	}

	public void performAutomaticVideoRedactionWithoutMergeAction() {
		if (imgPaths.isEmpty() == true) {
			return;
		}

		mRedactingVideo = true;

		ProgressBundle redactionProgressBundle = ProgressUtils.getProgressBundle("Video Redaction Progress...",
				imgPaths.size());

		final int saveCurrentIndex = currentIndex;

		String pathToDraw;

		for (currentIndex = 0; currentIndex < imgPaths.size(); ++currentIndex) {
			pathToDraw = imgPaths.get(currentIndex);
			refresh();

			ArrayList<String> allCapturePaths = new ArrayList<String>();
			allCapturePaths.add(pathToDraw);

			SimpleHaarDetector mHaarDetector = new SimpleHaarDetector(SystemUtils.getNumberOfSystemCores());

			ArrayList<Rectangle> faceRectangles = mHaarDetector.performSearch(CascadeUtils.HAAR_CASCADE_FACE_DEFAULT,
					allCapturePaths);

			mImagePanel.setHasFinishedPaint(false);
			mImagePanel.addRedactionRectangles(faceRectangles);

			while (mImagePanel.hasFinishedPaint() == false) {
				Thread.yield();
			}

			try {
				Thread.sleep(500);
			} catch (InterruptedException interruptedException) {
			}

			redactionProgressBundle.progressBar.setValue(redactionProgressBundle.progressBar.getValue() + 1);
			redactionProgressBundle.progressBar.repaint();
		}

		redactionProgressBundle.progressBar.repaint();
		redactionProgressBundle.frame.dispose();

		currentIndex = saveCurrentIndex;
		refresh();

		mRedactingVideo = false;
	}

	public void performAutomaticVideoRedactionAction() {
		if (imgPaths.isEmpty() == true) {
			return;
		}

		// final int confirmRedactChoice = UtilsLegacy.displayConfirmDialog("Notice",
		// "Automated face redaction is currently in beta.\n" +
		// "Proceed?");

		// if(confirmRedactChoice == JOptionPane.OK_OPTION) {
		//
		// } else {
		// return;
		// }

		String redactedVideoFilename = FileUtils.REDACT_DIRECTORY + FileUtils.getShortFilename(mPath);
		redactedVideoFilename = redactedVideoFilename.substring(0, redactedVideoFilename.lastIndexOf("."));
		redactedVideoFilename += "_redacted.mp4";

		File checkFile = new File(redactedVideoFilename);

		if (checkFile.exists() == true) {
			final int confirmContinueChoice = UtilsLegacy.displayConfirmDialog("Notice",
					"Redacted video already exists. Delete and proceed?");

			if (confirmContinueChoice == JOptionPane.OK_OPTION) {
				checkFile.delete();
			} else {
				return;
			}
		}

		mRedactingVideo = true;

		ProgressBundle redactionProgressBundle = ProgressUtils.getProgressBundle("Video Redaction Progress...",
				imgPaths.size());

		final int saveCurrentIndex = currentIndex;

		SimpleHaarDetector mHaarDetector = new SimpleHaarDetector(SystemUtils.getNumberOfSystemCores());
		ArrayList<String> allCapturePaths = new ArrayList<String>();
		ArrayList<Rectangle> faceRectangles;
		String pathToDraw;
		String redactedFilename;

		String redactedImageSuffix = "_" + FileUtils.getShortFilename(mPath);
		redactedImageSuffix = redactedImageSuffix.substring(0, redactedImageSuffix.lastIndexOf("."));
		redactedImageSuffix += ".jpg";
		redactedImageSuffix = redactedImageSuffix.replace(" ", "");

		for (currentIndex = 0; currentIndex < imgPaths.size(); ++currentIndex) {
			pathToDraw = imgPaths.get(currentIndex);
			refresh();// Simple();

			allCapturePaths.add(pathToDraw);

			faceRectangles = mHaarDetector.performSearch(CascadeUtils.HAAR_CASCADE_FACE_DEFAULT, allCapturePaths);

			mImagePanel.setHasFinishedPaint(false);
			mImagePanel.addRedactionRectangles(faceRectangles);

			while (mImagePanel.hasFinishedPaint() == false) {
				// Thread.yield();
				break;
			}

			redactedFilename = FileUtils.REDACT_DIRECTORY + String.format("%05d", currentIndex) + redactedImageSuffix;

			checkFile = new File(redactedFilename);

			if (checkFile.exists() == true) {
				checkFile.delete();
			}

			ImageUtils.saveBufferedImage(mImagePanel.getImage(), redactedFilename);

			allCapturePaths.clear();

			redactionProgressBundle.progressBar.setValue(redactionProgressBundle.progressBar.getValue() + 1);
			redactionProgressBundle.progressBar.repaint();
		}

		redactionProgressBundle.progressBar.repaint();
		redactionProgressBundle.frame.dispose();

		redactionProgressBundle = ProgressUtils.getProgressBundle("Redacted Video Creation Progress...",
				imgPaths.size());
		WindowsRedactedImagesToVideoProcess windowsRedactedImagesToVideoProcess = new WindowsRedactedImagesToVideoProcess();

		try {
			windowsRedactedImagesToVideoProcess.apply("%05d" + redactedImageSuffix, redactedVideoFilename,
					redactionProgressBundle);

			CaseMetadataWriter.writeNewRedactedSourceToContext(mContextFilename, redactedVideoFilename);
		} catch (InterruptedException interruptedException) {
			// interruptedException.printStackTrace();

			Utils.displayMessageDialog("Failed To Redact Video", "Could not redact video at path:\n" + mPath);
		}

		redactionProgressBundle.progressBar.setIndeterminate(true);
		redactionProgressBundle.progressBar.repaint();

		try {
			Thread.sleep(2000);
		} catch (InterruptedException interruptedException) {
			// interruptedException.printStackTrace();
		}

		// redactionProgressBundle.progressBar.setValue(redactionProgressBundle.progressBar.getValue()
		// + 1);
		redactionProgressBundle.progressBar.repaint();
		redactionProgressBundle.frame.dispose();

		// Cleanup
		for (currentIndex = 0; currentIndex < imgPaths.size(); ++currentIndex) {
			redactedFilename = FileUtils.REDACT_DIRECTORY + String.format("%05d", currentIndex) + redactedImageSuffix;

			checkFile = new File(redactedFilename);

			if (checkFile.exists() == true) {
				checkFile.delete();
			}
		}

		currentIndex = saveCurrentIndex;
		refresh();

		mRedactingVideo = false;

		final int confirmOpenDirectoryChoice = UtilsLegacy.displayConfirmDialog("Redaction Complete",
				"Open redacted video directory?");

		if (confirmOpenDirectoryChoice == JOptionPane.OK_OPTION) {
			new Thread() {
				@Override
				public void run() {
					ThreadUtils.addThreadToHandleList("ExtractImageGallery OpenRedactionFolder", this);

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
	}

	public void performRedactedSlidesMergerAction() {
		if (imgPaths.isEmpty() == true) {
			return;
		}

		String redactedVideoFilename = FileUtils.REDACT_DIRECTORY + FileUtils.getShortFilename(mPath);
		redactedVideoFilename = redactedVideoFilename.substring(0, redactedVideoFilename.lastIndexOf("."));
		redactedVideoFilename += "_redacted.mp4";

		File checkFile = new File(redactedVideoFilename);

		if (checkFile.exists() == true) {
			final int confirmContinueChoice = UtilsLegacy.displayConfirmDialog("Notice",
					"Redacted video already exists. Delete and proceed?");

			if (confirmContinueChoice == JOptionPane.OK_OPTION) {
				checkFile.delete();
			} else {
				return;
			}
		}

		ProgressBundle redactionProgressBundle = ProgressUtils.getProgressBundle("Saving Images Progress...",
				imgPaths.size());

		final int saveCurrentIndex = currentIndex;

		String redactedFilename;
		// String pathToDraw;

		String redactedImageSuffix = "_" + FileUtils.getShortFilename(mPath);
		redactedImageSuffix = redactedImageSuffix.substring(0, redactedImageSuffix.lastIndexOf("."));
		redactedImageSuffix += ".jpg";
		redactedImageSuffix = redactedImageSuffix.replace(" ", "");

		for (currentIndex = 0; currentIndex < imgPaths.size(); ++currentIndex) {
			// pathToDraw = imgPaths.get(currentIndex);
			refresh();// Simple();

			redactedFilename = FileUtils.REDACT_DIRECTORY + String.format("%05d", currentIndex) + redactedImageSuffix;

			checkFile = new File(redactedFilename);

			if (checkFile.exists() == true) {
				checkFile.delete();
			}

			ImageUtils.saveBufferedImage(mImagePanel.getImage(), redactedFilename);

			redactionProgressBundle.progressBar.setValue(redactionProgressBundle.progressBar.getValue() + 1);
			redactionProgressBundle.progressBar.repaint();
		}

		redactionProgressBundle.progressBar.repaint();
		redactionProgressBundle.frame.dispose();

		redactionProgressBundle = ProgressUtils.getProgressBundle("Merging Images Progress...", imgPaths.size());
		WindowsRedactedImagesToVideoProcess windowsRedactedImagesToVideoProcess = new WindowsRedactedImagesToVideoProcess();

		try {
			windowsRedactedImagesToVideoProcess.apply("%05d" + redactedImageSuffix, redactedVideoFilename,
					redactionProgressBundle);

			CaseMetadataWriter.writeNewRedactedSourceToContext(mContextFilename, redactedVideoFilename);
		} catch (InterruptedException interruptedException) {
			// interruptedException.printStackTrace();

			Utils.displayMessageDialog("Failed To Merge Images", "Could not create video at path:\n" + mPath);
		}

		redactionProgressBundle.progressBar.setIndeterminate(true);
		redactionProgressBundle.progressBar.repaint();

		try {
			Thread.sleep(2000);
		} catch (InterruptedException interruptedException) {
			// interruptedException.printStackTrace();
		}

		// redactionProgressBundle.progressBar.setValue(redactionProgressBundle.progressBar.getValue()
		// + 1);
		redactionProgressBundle.progressBar.repaint();
		redactionProgressBundle.frame.dispose();

		// Cleanup
		for (currentIndex = 0; currentIndex < imgPaths.size(); ++currentIndex) {
			redactedFilename = FileUtils.REDACT_DIRECTORY + String.format("%05d", currentIndex) + redactedImageSuffix;

			checkFile = new File(redactedFilename);

			if (checkFile.exists() == true) {
				checkFile.delete();
			}
		}

		currentIndex = saveCurrentIndex;
		refresh();

		final int openRedactedFolderChoice = UtilsLegacy.displayConfirmDialog("Redaction Complete",
				"Open redaction folder?");

		if (openRedactedFolderChoice == JOptionPane.OK_OPTION) {
			new Thread() {
				@Override
				public void run() {
					ThreadUtils.addThreadToHandleList("ExtractImageGallery OpenRedactedFolder", this);

					final File file = new File(FileUtils.REDACT_DIRECTORY);
					final Desktop desktop = Desktop.getDesktop();

					try {
						desktop.browse(file.toURI());
					} catch (IOException ioException) {

					}

					ThreadUtils.removeThreadFromHandleList(this);
				}
			}.start();
		} else {
			return;
		}
	}

	private void performGoToFrameAction() {
		String frameIndexString = Utils.displayInputDialog("Go to frame:", "Jump To Frame");

		try {
			Integer selectedFrameIndex = Integer.parseInt(frameIndexString);

			currentIndex = selectedFrameIndex - 1;

			if (currentIndex < 0) {
				Utils.displayMessageDialog("Invalid Frame Error", "Selection must be greater than zero.");
			} else if (currentIndex == maxIndex) {
				Utils.displayMessageDialog("Invalid Frame Error",
						"Selection \"" + frameIndexString + "\" is greater than number of frames.");
			} else {
				refresh();
			}
		} catch (NumberFormatException numberFormatException) {
			// numberFormatException.printStackTrace();

			if (frameIndexString == null) {
				frameIndexString = "None";
			}

			Utils.displayMessageDialog("Invalid Frame Error", "Could not jump to frame: \"" + frameIndexString + "\".");
		}
	}

	private void performUndoAction() {
		mImagePanel.performUndoAction();
	}

	private void performRevertAction() {
		final String imageToRevert = imgPaths.get(currentIndex);

		mModifiedPathMap.remove(imageToRevert);
		FileUtils.deleteFile(new File(getTemporaryRedactionForCurrentIndex()));

		refresh();
	}

	private String getCaseExtractDirectory() {
		return FileUtils.REPORT_EXTRACTS_DIRECTORY + DATABASE_NAME + File.separator;
	}

	private void checkCaseFolderInExtractDirectory() {
		FileUtils.createDirectory(getCaseExtractDirectory());
	}

	private String getSavedRedactionForCurrentIndex() {
		return getCaseExtractDirectory() + FileUtils.getShortFilename(mPath) + "_"
				+ FileUtils.getShortFilename(imgPaths.get(currentIndex));
	}

	private String getTemporaryRedactionForCurrentIndex() {
		return FileUtils.REPORT_EXTRACTS_TEMPORARY_DIRECTORY + FileUtils.getShortFilename(mPath) + "_"
				+ FileUtils.getShortFilename(imgPaths.get(currentIndex));
	}

	private void performSaveCurrentSlideAction() {
		final String imageToAdd = imgPaths.get(currentIndex);

		final String pathToSave = getSavedRedactionForCurrentIndex();

		File checkFile = new File(pathToSave);

		if (checkFile.exists() == true) {
			checkFile.delete();
		}

		if (mModifiedPathMap.containsKey(imageToAdd) == false) {
			addModifiedImagePath(imageToAdd, pathToSave, true);
		}

		ImageUtils.saveBufferedImage(mImagePanel.getImage(), pathToSave);
	}

	private void performAddToReportAction() {
		checkCaseFolderInExtractDirectory();

		performSaveCurrentSlideAction();
	}

	protected String getUpdatedPageLabelText() {
		if (imgPaths.isEmpty() == true) {
			return "Image: 0/0";
		} else {
			return "Image: " + (currentIndex + 1) + "/" + maxIndex;
		}
	}

	protected void performPreviousAction() {
		disableFurtherActions();

		currentIndex--;

		if (currentIndex < 0) {
			currentIndex = maxIndex - 1;
		}

		refresh();

		enableFurtherActions();
	}

	protected void performNextAction() {
		disableFurtherActions();

		currentIndex++;

		if (currentIndex == maxIndex) {
			currentIndex = 0;
		}

		refresh();

		enableFurtherActions();
	}

	public void refresh() {
		if (imgPaths.isEmpty() == true) {
			mImagePanel.update(ImageUtils.loadBufferedImage(NO_PREVIEW_AVAILABLE_IMAGE_PATH));

			EventQueue.invokeLater(new Runnable() {
				public void run() {
					mPageLabel.setText(getUpdatedPageLabelText());
					mImagePanel.revalidate();
				}
			});
		} else {
			final String pathToDraw = imgPaths.get(currentIndex);
			final String savedRedactionForCurrentIndexPath = getSavedRedactionForCurrentIndex();
			final String temporaryRedactionForCurrentIndexPath = getTemporaryRedactionForCurrentIndex();

			if (mModifiedPathMap.containsKey(pathToDraw) == true) {
				mImagePanel.update(ImageUtils.loadBufferedImage(mModifiedPathMap.get(pathToDraw)), pathToDraw);
			} else if (new File(temporaryRedactionForCurrentIndexPath).exists() == true) {
				mImagePanel.update(ImageUtils.loadBufferedImage(temporaryRedactionForCurrentIndexPath), pathToDraw);
			} else if (new File(savedRedactionForCurrentIndexPath).exists() == true) {
				mImagePanel.update(ImageUtils.loadBufferedImage(savedRedactionForCurrentIndexPath), pathToDraw);
			} else {
				mImagePanel.update(ImageUtils.loadBufferedImage(pathToDraw), pathToDraw);
			}

			EventQueue.invokeLater(new Runnable() {

				public void run() {
					mPageLabel.setText(getUpdatedPageLabelText());
					mImagePanel.revalidate();
				}
			});
		}
	}

	/*
	 * public void refreshSimple() { /* final String pathToDraw =
	 * imgPaths.get(currentIndex);
	 * 
	 * mImagePanel.update(ImageUtils.loadBufferedImage(pathToDraw), pathToDraw);
	 * 
	 * EventQueue.invokeLater(new Runnable() { public void run() {
	 * mPageLabel.setText(getUpdatedPageLabelText()); mImagePanel.revalidate(); }
	 * }); /
	 * 
	 * if(imgPaths.isEmpty() == true) {
	 * mImagePanel.update(ImageUtils.loadBufferedImage(
	 * NO_PREVIEW_AVAILABLE_IMAGE_PATH));
	 * 
	 * EventQueue.invokeLater(new Runnable() { public void run() {
	 * mPageLabel.setText(getUpdatedPageLabelText()); mImagePanel.revalidate(); }
	 * }); } else { final String pathToDraw = imgPaths.get(currentIndex);
	 * 
	 * if(mModifiedPathMap.containsKey(pathToDraw) == true) {
	 * mImagePanel.update(ImageUtils.loadBufferedImage(mModifiedPathMap.get(
	 * pathToDraw)), pathToDraw); } else {
	 * mImagePanel.update(ImageUtils.loadBufferedImage(pathToDraw), pathToDraw); }
	 * 
	 * EventQueue.invokeLater(new Runnable() { public void run() {
	 * mPageLabel.setText(getUpdatedPageLabelText()); mImagePanel.revalidate(); }
	 * }); } }
	 */
}

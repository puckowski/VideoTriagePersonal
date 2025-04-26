package com.keypointforensics.videotriage.gui.notes;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.DefaultListModel;
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

import com.keypointforensics.videotriage.legacy.FileUtilsLegacy;
import com.keypointforensics.videotriage.legacy.UtilsLegacy;
import com.keypointforensics.videotriage.util.BorderUtils;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.Utils;
import com.keypointforensics.videotriage.util.WebUtils;
import com.keypointforensics.videotriage.util.WindowUtils;
import com.keypointforensics.videotriage.window.WindowRegistry;

public class CaseNotesEditor extends JFrame {

	/*
	 * Author: Daniel Puckowski
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7735202780422258365L;

	private final String CASE_NAME;
	
	private File mCaseFolder;
	
	private JPanel mContentPanel;
	
	private JLabel mFilenameLabel;
	private JTextArea mNoteArea;
	private JList<String> mNoteList;
	private DefaultListModel mNoteListModel;
	private JButton mSaveButton;
	private JButton mClearButton;
	private JButton mDeleteButton;
	private JButton mDeleteListButton;
	private JButton mRenameListButton;
	private JButton mRenameButton;
	private JButton mNewButton;
	
	private JMenuItem mExitMenuItem;
	private JMenuItem mAboutMenuItem;
	private JMenuItem mDocumentationMenuItem;
	private JMenuItem mSaveMenuItem;
	private JMenuItem mClearMenuItem;
	private JMenuItem mDeleteMenuItem;
	private JMenuItem mRenameMenuItem;
	private JMenuItem mNewMenuItem;
	private JMenuBar mMenuBar;
	
	private ArrayList<String> mNoteFilenames;
	
	public CaseNotesEditor(final String caseName) {
		CASE_NAME = caseName;
		
		mNoteFilenames = new ArrayList<String>();
		
		createCaseFolderIfNecessary();
		
		WindowRegistry.getInstance().registerFrame(this, "CaseNotesEditor");
	}
	
	private void loadNoteContent(final String noteFile) throws IOException {
		try(BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(noteFile)))) {
		    for(String line; (line = bufferedReader.readLine()) != null; ) {
		        mNoteArea.append(line);
		        mNoteArea.append("\n");
		    }
		    
		    bufferedReader.close();
		}
	}
	
	private void displaySelectedContent() {
		final int selectedIndex = mNoteList.getSelectedIndex();
		
		if(selectedIndex >= 0) {
			mNoteArea.setText("");
			
			final String selectedValue = mNoteList.getModel().getElementAt(selectedIndex);
			
			mFilenameLabel.setText("Editing Note: " + selectedValue);
			mFilenameLabel.revalidate();
			
			try {
				loadNoteContent(selectedValue);
			} catch (IOException ioException) {
				//ioException.printStackTrace();
			}
			
			mNoteArea.revalidate();
		}
	}
	
	private void loadNoteFilenames() {
		mNoteFilenames.clear();
		
		mNoteFilenames.addAll(FileUtilsLegacy.parseDirectoryRecursiveForAll(mCaseFolder.getAbsolutePath()));
		Collections.sort(mNoteFilenames);
		
		mNoteList.removeAll();
		mNoteListModel.removeAllElements();
		
		for(String noteFilename : mNoteFilenames) {
			mNoteListModel.addElement(noteFilename);
		}
		
		mNoteList.revalidate();
	}
	
	private void createCaseFolderIfNecessary() {
		mCaseFolder = new File(FileUtils.NOTES_DIRECTORY + CASE_NAME);
		
		if(mCaseFolder.exists() == false) {
			mCaseFolder.mkdir();
		}
	}
	
	private void saveNote(final String noteFilename) {
		if(noteFilename.isEmpty() == true) {
			return;
		}
		
		final String noteFilenameFull = mCaseFolder.getAbsolutePath() + File.separator + noteFilename;
		
		try {
			PrintWriter noteWriter = new PrintWriter(noteFilenameFull);
			mFilenameLabel.setText("Editing Note: " + noteFilenameFull);
			mFilenameLabel.revalidate();
			
			noteWriter.append(mNoteArea.getText());
			
			noteWriter.flush();
			noteWriter.close();
		} catch (FileNotFoundException fileNotFoundException) {
			//fileNotFoundException.printStackTrace();
			
			Utils.displayMessageDialog("Save Error", "Could not save note: \n" + noteFilenameFull);
		}
	}
	
	private String getNoteFilename() {
		final String filenameLabel = mFilenameLabel.getText();
		String noteFilename = filenameLabel.substring(filenameLabel.indexOf(" ") + 1, filenameLabel.length());
		noteFilename = noteFilename.substring(noteFilename.indexOf(" ") + 1, noteFilename.length());
		
		return noteFilename;
	}
	
	private void performDeleteAction() {
		final String noteFilename = getNoteFilename();
		
		if(noteFilename.equals("None") == true) {
			return;
		}
		
		final int deleteFileChoice = UtilsLegacy.displayConfirmDialog("Confirm Delete", "Delete note? This process cannot be undone.");
		
		if(deleteFileChoice == JOptionPane.OK_OPTION) {			
			final boolean deleteResult = FileUtils.deleteFile(new File(noteFilename));
			
			if(deleteResult == false) {
				Utils.displayMessageDialog("Delete Error", "Failed to delete note: \n" + noteFilename);
			}
			
			mNoteArea.setText("");
			mNoteArea.revalidate();
			
			mFilenameLabel.setText("Editing Note: None");
			mFilenameLabel.revalidate();
			
			loadNoteFilenames();
		}
	}
	
	private void performDeleteFromListAction() {
		int selectedIndex = mNoteList.getSelectedIndex();
		
		if(selectedIndex < 0) {
			return;
		}
		
		final String noteFilename = mNoteList.getModel().getElementAt(selectedIndex);
		
		if(noteFilename.isEmpty() == true) {
			return;
		}
		
		final int deleteFileChoice = UtilsLegacy.displayConfirmDialog("Confirm Delete", "Delete note? This process cannot be undone.");
		
		if(deleteFileChoice == JOptionPane.OK_OPTION) {		
			final boolean deleteResult = FileUtils.deleteFile(new File(noteFilename));
			
			if(deleteResult == false) {
				Utils.displayMessageDialog("Delete Error", "Failed to delete note: \n" + noteFilename);
			}
			
			mNoteArea.setText("");
			mNoteArea.revalidate();
			
			mFilenameLabel.setText("Editing Note: None");
			mFilenameLabel.revalidate();
			
			loadNoteFilenames();
		}
	}
	
	private void performRenameFromListAction() {
		int selectedIndex = mNoteList.getSelectedIndex();
		
		if(selectedIndex < 0) {
			return;
		}
		
		final String noteFilename = mNoteList.getModel().getElementAt(selectedIndex);
		
		if(noteFilename.isEmpty() == true) {
			return;
		}
		
		final String saveAsNote = Utils.displayInputDialog("Save note as: ", "Save Note");
		
		File currentFile = new File(noteFilename);
		final boolean renameSuccess = currentFile.renameTo(new File(saveAsNote));
		
		if(renameSuccess == false) {
			Utils.displayMessageDialog("Rename Error", "Failed to rename note: \n" + noteFilename);
		}
		
		mFilenameLabel.setText("Editing Note: " + saveAsNote);
		mFilenameLabel.revalidate();
		
		loadNoteFilenames();
	}
	
	private void performRenameAction() {
		final String noteFilename = getNoteFilename();
		
		if(noteFilename.equals("None") == true) {
			return;
		}
		
		final String saveAsNote = Utils.displayInputDialog("Save note as: ", "Save Note");
		
		File currentFile = new File(noteFilename);
		final boolean renameSuccess = currentFile.renameTo(new File(saveAsNote));
		
		if(renameSuccess == false) {
			Utils.displayMessageDialog("Rename Error", "Failed to rename note: \n" + noteFilename);
		}
		
		mFilenameLabel.setText("Editing Note: " + saveAsNote);
		mFilenameLabel.revalidate();
		
		loadNoteFilenames();
	}
	
	private void performNewAction() {
		performClearAction();
		
		final String saveAsNote = Utils.displayInputDialog("Save note as: ", "Save Note");
		
		saveNote(saveAsNote);
		
		loadNoteFilenames();
	}
	
	private void performSaveAction() {
		final String noteFilenameCurrent = getNoteFilename();
		
		if(noteFilenameCurrent.isEmpty() == false && noteFilenameCurrent.equals("None") == false) {
			File deleteExisting = new File(noteFilenameCurrent);
			deleteExisting.delete();
			
			saveNote(FileUtils.getShortFilename(noteFilenameCurrent));
		} else {
			final String saveAsNote = Utils.displayInputDialog("Save note as: ", "Save Note");
		
			saveNote(saveAsNote);
		}
		
		loadNoteFilenames();
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
						ThreadUtils.addThreadToHandleList("CaseNotesEditor Exit", this);
						
						CaseNotesEditor.this.dispose();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(mExitMenuItem);
		
		menu = new JMenu("Edit");
		mMenuBar.add(menu);
		
		mNewMenuItem = new JMenuItem("New");
		mNewMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("CaseNotesEditor New", this);
						
						performNewAction();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(mNewMenuItem);
		
		mClearMenuItem = new JMenuItem("Clear");
		mClearMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("CaseNotesEditor Clear", this);
						
						performClearAction();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(mClearMenuItem);
		
		mDeleteMenuItem = new JMenuItem("Delete");
		mDeleteMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("CaseNotesEditor Delete", this);
						
						performDeleteAction();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(mDeleteMenuItem);
		
		mRenameMenuItem = new JMenuItem("Rename");
		mRenameMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("CaseNotesEditor Rename", this);
						
						performRenameAction();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(mRenameMenuItem);
		
		mSaveMenuItem = new JMenuItem("Save");
		mSaveMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("CaseNotesEditor Save", this);
						
						performSaveAction();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(mSaveMenuItem);
		
		menu = new JMenu("Help");
		mMenuBar.add(menu);
		
		mDocumentationMenuItem = new JMenuItem("Open Documentation");
		mDocumentationMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("CaseNotesEditor OpenDoc", this);
						
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
		menu.add(mDocumentationMenuItem);
		
		mAboutMenuItem = new JMenuItem("About");
		mAboutMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("CaseNotesEditor About", this);
						
						Utils.displayMessageDialog("About", Utils.SOFTWARE_NAME + " \nVersion: " + Utils.SOFTWARE_VERSION + "\n© " + Utils.AUTHOR_NAME);

						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(mAboutMenuItem);
		
		this.setJMenuBar(mMenuBar);
	}
	
	private void performClearAction() {
		mNoteArea.setText("");
		mNoteArea.revalidate();
		
		mFilenameLabel.setText("Editing Note: None");
		mFilenameLabel.revalidate();
	}
	
	public void buildFrame() {
		this.setLayout(new BorderLayout());
		
		buildMenuBar();
		
		mContentPanel = new JPanel();
		mContentPanel.setLayout(new BorderLayout());
		mContentPanel.setBorder(BorderUtils.getEmptyBorder());
		
		mFilenameLabel = new JLabel("Editing Note: None");
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		
		mNewButton = new JButton("New");
		mNewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				performNewAction();
			}	
		});
		
		mSaveButton = new JButton("Save");
		mSaveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				performSaveAction();
			}	
		});
		
		mClearButton = new JButton("Clear");
		mClearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				performClearAction();
			}	
		});
		
		mDeleteButton = new JButton("Delete");
		mDeleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				performDeleteAction();
			}	
		});
		
		mRenameButton = new JButton("Rename");
		mRenameButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				performRenameAction();
			}	
		});
		
		buttonPanel.add(mNewButton);
		buttonPanel.add(mClearButton);
		buttonPanel.add(mDeleteButton);
		buttonPanel.add(mRenameButton);
		buttonPanel.add(mSaveButton);
		
		JPanel notePanel = new JPanel();
		notePanel.setLayout(new BorderLayout());
		notePanel.setBorder(BorderUtils.getEmptyBorder());
		
		mNoteArea = new JTextArea();
		mNoteArea.setEditable(true);
		mNoteArea.setLineWrap(true);
		mNoteArea.setWrapStyleWord(true);
		mNoteArea.setFont(new Font("Sans Serif", Font.PLAIN, 16));
		WindowUtils.setTextAreaUpdatePolicy(mNoteArea);
		
		JScrollPane noteScrollPane = new JScrollPane(mNoteArea);
		
		notePanel.add(noteScrollPane, BorderLayout.CENTER);
		notePanel.add(mFilenameLabel, BorderLayout.NORTH);
		notePanel.add(buttonPanel, BorderLayout.SOUTH);
		
		mNoteListModel = new DefaultListModel();
		
		mNoteList = new JList<String>(mNoteListModel);
		mNoteList.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent mouseEvent) {
				displaySelectedContent();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent) {
				
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent) {
				
			}

			@Override
			public void mousePressed(MouseEvent mouseEvent) {
				
			}

			@Override
			public void mouseReleased(MouseEvent mouseEvent) {
				
			}
		});
		JScrollPane noteListScrollPane = new JScrollPane(mNoteList);
		
		JLabel noteListLabel = new JLabel("Case Note List");
		
		JPanel buttonListPanel = new JPanel();
		buttonListPanel.setLayout(new FlowLayout());
		
		mDeleteListButton = new JButton("Delete");
		mDeleteListButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				performDeleteFromListAction();
			}	
		});
		
		mRenameListButton = new JButton("Rename");
		mRenameListButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				performRenameFromListAction();
			}	
		});
		
		buttonListPanel.add(mDeleteListButton);
		buttonListPanel.add(mRenameListButton);
		
		JPanel noteListPanel = new JPanel();
		noteListPanel.setLayout(new BorderLayout());
		noteListPanel.setBorder(BorderUtils.getEmptyBorder());
		
		noteListPanel.add(noteListScrollPane, BorderLayout.CENTER);
		noteListPanel.add(noteListLabel, BorderLayout.NORTH);
		noteListPanel.add(buttonListPanel, BorderLayout.SOUTH);
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, notePanel, noteListPanel);
		splitPane.setResizeWeight(0.7);
		splitPane.setOneTouchExpandable(true);
		splitPane.setContinuousLayout(true);
		
		JTabbedPane tabPane = new JTabbedPane();
		tabPane.addTab("Edit", splitPane);
		
		//mContentPanel.add(notePanel, BorderLayout.CENTER);
		//mContentPanel.add(noteListPanel, BorderLayout.EAST);
		
		mContentPanel.add(tabPane, BorderLayout.CENTER);
		
		this.add(mContentPanel, BorderLayout.CENTER);
		
		WindowUtils.setFrameIcon(this);
		this.setTitle("Case Notes Editor");
		this.setPreferredSize(new Dimension(1400, 1000));
		this.pack();
		WindowUtils.maximize(this);
		this.setVisible(true);
		//WindowUtils.center(this);
		
		loadNoteFilenames();
		
		if(mNoteListModel.size() > 0) {
			mNoteList.setSelectedIndex(0);
		}
		
		displaySelectedContent();
	}
}

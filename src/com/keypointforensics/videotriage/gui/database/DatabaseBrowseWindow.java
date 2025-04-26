package com.keypointforensics.videotriage.gui.database;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import com.keypointforensics.videotriage.gui.imagepanel.ScalableSimpleImagePanel;
import com.keypointforensics.videotriage.sqlite.BlobRecord;
import com.keypointforensics.videotriage.thread.OpenFileLocationThread;
import com.keypointforensics.videotriage.thread.SearchDatabaseThread;
import com.keypointforensics.videotriage.util.BorderUtils;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ImageUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.Utils;
import com.keypointforensics.videotriage.util.WebUtils;
import com.keypointforensics.videotriage.util.WindowUtils;
import com.keypointforensics.videotriage.window.ChildWindowList;
import com.keypointforensics.videotriage.window.CloseChildrenWindowAdapter;
import com.keypointforensics.videotriage.window.WindowRegistry;

public class DatabaseBrowseWindow extends JFrame implements ActionListener {

	/*
	 * Author: Daniel Puckowski
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8474967752721014874L;
		
	private static final BufferedImage NO_PREVIEW_AVAILABLE_IMAGE = ImageUtils.loadBufferedImage(FileUtils.GRAPHICS_DIRECTORY + "no_preview_available.jpg");
	
	private final String DATABASE_NAME;
	
	public static final int RESULT_TABLE_RECOMMENDED_WIDTH  = 478;
	public static final int RESULT_TABLE_RECOMMENDED_HEIGHT = 320;
	
	private JTextField mDayField;
	private JTextField mMonthField;
	private JTextField mYearField;
	private JTextField mFilenameField;
	private JTextField mIpField;
	private JTextField mPortField;
	
	private JTable     mResultTable;
	
	private JButton    mOpenFileLocationButton;
	private JButton    mSearchDatabaseButton;
	private JButton    mClearFieldsButton;

	private ConcurrentHashMap<BlobRecord, Integer> mBlobRecordsMap;
	private ArrayList<BlobRecord> mBlobRecordsList;
	
	private ScalableSimpleImagePanel mPreviewPanel;
	
	private ChildWindowList mChildWindowList;
	
	public DatabaseBrowseWindow(final String databaseName) {
		DATABASE_NAME = databaseName;
	
		mChildWindowList = new ChildWindowList();
		
		buildFrame();
		
		WindowRegistry.getInstance().registerFrame(this, "DatabaseBrowseWindow");
	}
	
	public String getDatabaseName() {
		return DATABASE_NAME;
	}
	
	private void buildFrame() {		
		buildMenuBar();
		
		this.setLayout(new BorderLayout());
		
		JPanel searchPanel = new JPanel();
		searchPanel.setLayout(new GridLayout(14, 1));
		searchPanel.setBorder(BorderUtils.getEmptyBorder()); 
		
		JLabel filenameFieldLabel = new JLabel("Filename");
		mFilenameField = new JTextField();
		JLabel ipFieldLabel = new JLabel("IP Address");
		mIpField = new JTextField();
		JLabel portFieldLabel = new JLabel("Port");
		mPortField = new JTextField("8080");
		
		JPanel datePanel = new JPanel();
		datePanel.setLayout(new FlowLayout());
		
		JLabel dateFieldLabel = new JLabel("Date");
		mDayField = new JTextField(2);
		mMonthField = new JTextField(2);
		mYearField = new JTextField(6);
		
		datePanel.add(new JLabel("MM:"));
		datePanel.add(mMonthField);
		datePanel.add(new JLabel("DD:"));
		datePanel.add(mDayField);
		datePanel.add(new JLabel("YYYY:"));
		datePanel.add(mYearField);
	
		mSearchDatabaseButton = new JButton("Search Database");
		mSearchDatabaseButton.addActionListener(this);
		mSearchDatabaseButton.setPreferredSize(new Dimension(200, 50));
		
		mClearFieldsButton = new JButton("Clear Fields");
		mClearFieldsButton.addActionListener(this);
		mClearFieldsButton.setPreferredSize(new Dimension(200, 50));
		
		searchPanel.add(dateFieldLabel);
		searchPanel.add(datePanel);
		searchPanel.add(filenameFieldLabel);
		searchPanel.add(mFilenameField);
		searchPanel.add(ipFieldLabel);
		searchPanel.add(mIpField);
		searchPanel.add(portFieldLabel);
		searchPanel.add(mPortField);
		searchPanel.add(mClearFieldsButton);
		searchPanel.add(mSearchDatabaseButton);
		
		JScrollPane searchScrollPane = new JScrollPane(searchPanel);
		WindowUtils.setScrollBarIncrement(searchScrollPane);
		
		mResultTable = new JTable();
		
		ListSelectionModel selectionModel = mResultTable.getSelectionModel();
		selectionModel.addListSelectionListener(new ListSelectionListener() {
			@Override
            public void valueChanged(ListSelectionEvent event) {
                if (event.getValueIsAdjusting() == false) {
					if (mResultTable.getRowCount() == 0) {
						
						return;
					}
					else if(mResultTable.getColumnCount() == 0) {
						
						return;
					}

					final int selectedRow = mResultTable.getSelectedRow();
					
					if(selectedRow != -1) {
						String filename = (String) mResultTable.getValueAt(selectedRow, 0);
	
						if (filename == null || filename.isEmpty() == true) {						
							return;
						}
						
						mPreviewPanel.update(ImageUtils.loadBufferedImage(filename));
					}
                }
            }
		});
		
		DefaultTableModel model = new DefaultTableModel(new String[] { "Search Results" }, 0);
		mResultTable.setModel(model);
		mResultTable.setAutoCreateRowSorter(true);
		model.addRow(new Object[]{ "Nothing searched yet." });
		
		JScrollPane resultListScrollPane = new JScrollPane(mResultTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		resultListScrollPane.setPreferredSize(new Dimension(480, RESULT_TABLE_RECOMMENDED_HEIGHT));
		WindowUtils.setScrollBarIncrement(resultListScrollPane);
		
		mOpenFileLocationButton = new JButton("Open Results Location");
		mOpenFileLocationButton.addActionListener(this);
		mOpenFileLocationButton.setPreferredSize(new Dimension(200, 50));
		
		JPanel previewContentPanel = new JPanel();
		previewContentPanel.setLayout(new BorderLayout());
		previewContentPanel.setBorder(BorderUtils.getEmptyBorder()); 
	
		mPreviewPanel = new ScalableSimpleImagePanel(true);//DeprecatedTitlePanel("", true);
		mPreviewPanel.update(NO_PREVIEW_AVAILABLE_IMAGE);
		mPreviewPanel.setPreferredSize(new Dimension(320, 320));
		mPreviewPanel.setBorder(BorderUtils.getEmptyBorder()); 
		
		JPanel previewButtonPanel = new JPanel();
		previewButtonPanel.setLayout(new GridLayout(1, 1));
		previewButtonPanel.add(mOpenFileLocationButton);
		
		previewContentPanel.add(mPreviewPanel, BorderLayout.CENTER);
		previewContentPanel.add(mOpenFileLocationButton, BorderLayout.SOUTH);

		JSplitPane leftSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, searchScrollPane, resultListScrollPane);
		JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSplitPane, previewContentPanel);
		mainSplitPane.setResizeWeight(0.5);
		
		this.add(mainSplitPane, BorderLayout.CENTER);
		
		this.addWindowListener(new CloseChildrenWindowAdapter(mChildWindowList));
		
		this.setTitle("Search Database");
		this.pack();
		WindowUtils.setFrameIcon(this);
		WindowUtils.maximize(this);
		this.setVisible(true);
		//WindowUtils.center(this);
	}

	public BufferedImage getNoPreviewImage() {
		return NO_PREVIEW_AVAILABLE_IMAGE;
	}
	
	private String getTimeStampString() {
		String monthString = mMonthField.getText();
		
		if(monthString.equals("*") == false && monthString.length() == 1) {
			monthString = "0" + monthString;
			mMonthField.setText(monthString);
		}
		
		String dayString = mDayField.getText();
		
		if(dayString.equals("*") == false && dayString.length() == 1) {
			dayString = "0" + dayString;
			mDayField.setText(dayString);
		}
		
		String yearString = mYearField.getText();
		
		return monthString + "-" + dayString + "-" + yearString;
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() instanceof JButton) {
			JButton button = (JButton) event.getSource();

			if (button == mSearchDatabaseButton) {
				performSearchDatabaseAction();
			} else if(button == mOpenFileLocationButton) {
				performOpenResultsLocationAction();
			} else if(button == mClearFieldsButton) {
				performClearFieldsAction();
			}
		}
	}
	
	private void performClearFieldsAction() {
		mDayField.setText("");
		mMonthField.setText("");
		mYearField.setText("");
		mFilenameField.setText("");
		mIpField.setText("");
		mPortField.setText("");
	}
	
	private void performSearchDatabaseAction() {
		SearchDatabaseThread searchDatabaseThread = new SearchDatabaseThread(this, mResultTable, getTimeStampString(), mFilenameField.getText(),
				mIpField.getText(), mPortField.getText(), mBlobRecordsList, mBlobRecordsMap);
		searchDatabaseThread.start();
		
		//mResizeOnSearch = true;
	}
	
	private void performOpenResultsLocationAction() {
		OpenFileLocationThread openFileLocationThread = new OpenFileLocationThread(this, mResultTable);
		openFileLocationThread.start();
	}
	
	private void performCopySelectionToClipboardAction() {
		int[] selectedRows = mResultTable.getSelectedRows();
		
		if(selectedRows.length == 0) {
			
		}
		else if(selectedRows.length > 1) {
			Utils.displayMessageDialog("Clipboard Error", "Multiple rows are selected. Only one row can be exported to the clipboard.");
		}
		else {
			TableModel resultTableModel = mResultTable.getModel();
			
			StringBuilder selectionBuilder = new StringBuilder();
			StringBuilder messageDialogSelectionBuilder = new StringBuilder();
			final int columnCount = resultTableModel.getColumnCount();
			final int finalColumn = columnCount - 2;
			
			for(int i = 0; i < columnCount; ++i) {
				selectionBuilder.append(resultTableModel.getValueAt(selectedRows[0], i));
				messageDialogSelectionBuilder.append(resultTableModel.getValueAt(selectedRows[0], i));
				
				if(i != finalColumn) {
					selectionBuilder.append(", ");
					messageDialogSelectionBuilder.append("\n");
				}
			}
			
			String selectionText = selectionBuilder.toString();
			
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(new StringSelection(selectionText), null);
			
			Utils.displayMessageDialog("Clipboard Success", "Copied:\n" + messageDialogSelectionBuilder.toString());
		}
	}
	
	private void performExportSelectionsAction() {
		int[] selectedRows = mResultTable.getSelectedRows();
		
		if(selectedRows.length == 0) {
			
		}
		else {
			TableModel resultTableModel = mResultTable.getModel();
			
			StringBuilder selectionBuilder = new StringBuilder();
			final int columnCount = resultTableModel.getColumnCount();
			boolean isStartOfRow = true;
			
			selectionBuilder.append("Filename, Time Stamp, Source, Port\n");
			
			for(int n = 0; n < selectedRows.length; ++n) {
				for(int i = 0; i < columnCount; ++i) {
					if(isStartOfRow == true) {
						isStartOfRow = false;
					}
					else {
						selectionBuilder.append(", ");
					}
					
					selectionBuilder.append("\"");
					selectionBuilder.append(resultTableModel.getValueAt(selectedRows[n], i));
					selectionBuilder.append("\"");
				}
				
				selectionBuilder.append("\n");
				isStartOfRow = true;
			}
			
			String selectionText = selectionBuilder.toString();
			
			DatabaseExportWindow databaseExportWindow = new DatabaseExportWindow();
			databaseExportWindow.buildAndDisplay();
			databaseExportWindow.setExportText(selectionText);
		}
	}
	
	private void performExportAllAction() {		
		final int exportRowCount = mResultTable.getRowCount();
		
		if(exportRowCount > 0 && mResultTable.getColumnCount() > 1) {
			TableModel resultTableModel = mResultTable.getModel();
				
			StringBuilder selectionBuilder = new StringBuilder();
			final int columnCount = resultTableModel.getColumnCount();
			boolean isStartOfRow = true;
				
			selectionBuilder.append("Filename, Time Stamp, Source, Port\n");
			
			for(int n = 0; n < exportRowCount; ++n) {
				for(int i = 0; i < columnCount; ++i) {
					if(isStartOfRow == true) {
						isStartOfRow = false;
					}
					else {
						selectionBuilder.append(", ");
					}
					
					selectionBuilder.append("\"");
					selectionBuilder.append(resultTableModel.getValueAt(n, i));
					selectionBuilder.append("\"");
				}
				
				selectionBuilder.append("\n");
				isStartOfRow = true;
			}
				
			String selectionText = selectionBuilder.toString();
				
			DatabaseExportWindow databaseExportWindow = new DatabaseExportWindow();
			databaseExportWindow.buildAndDisplay();
			databaseExportWindow.setExportText(selectionText);
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
						ThreadUtils.addThreadToHandleList("DbBrowse Exit", this);
						
						WindowRegistry.getInstance().closeFrames(mChildWindowList.getWindows());
						DatabaseBrowseWindow.this.dispose();
						
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
						ThreadUtils.addThreadToHandleList("DbBrowse Search", this);
						
						performSearchDatabaseAction();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Advanced Search");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("DbBrowse AdvancedSearch", this);
						
						AdvancedDatabaseSearchWindow advancedDatabaseSearchWindow = new AdvancedDatabaseSearchWindow(DatabaseBrowseWindow.this, mResultTable, mBlobRecordsMap, mBlobRecordsList);
						mChildWindowList.addWindow(advancedDatabaseSearchWindow);
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Clear Fields");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("DbBrowse ClearFields", this);
						
						performClearFieldsAction();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Open Results Location");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("DbBrowse Open", this);
						
						performOpenResultsLocationAction();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menu = new JMenu("Export");
		menuBar.add(menu);
		
		menuItem = new JMenuItem("Export Selection To Clipboard");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("DbBrowse ExportToClipboard", this);
						
						performCopySelectionToClipboardAction();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Export Selections");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("DbBrowse ExportSelections", this);
						
						performExportSelectionsAction();
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Export All");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {	
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("DbBrowse ExportAll", this);
						
						performExportAllAction();
						
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
						ThreadUtils.addThreadToHandleList("DbBrowse OpenDoc", this);
						
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
						ThreadUtils.addThreadToHandleList("DbBrowse About", this);
						
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

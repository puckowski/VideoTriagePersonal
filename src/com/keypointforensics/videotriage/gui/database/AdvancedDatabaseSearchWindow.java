package com.keypointforensics.videotriage.gui.database;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.TimePicker;
import com.keypointforensics.videotriage.sqlite.BlobRecord;
import com.keypointforensics.videotriage.thread.AdvancedSearchDatabaseThread;
import com.keypointforensics.videotriage.thread.SearchDatabaseThread;
import com.keypointforensics.videotriage.util.BorderUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.Utils;
import com.keypointforensics.videotriage.util.WebUtils;
import com.keypointforensics.videotriage.util.WindowUtils;
import com.keypointforensics.videotriage.window.WindowRegistry;

public class AdvancedDatabaseSearchWindow extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8380969360214001236L;
	
	public static final SimpleDateFormat ADVANCED_SEARCH_DATE_FORMAT = new SimpleDateFormat("MM-dd-yyyy HH-mm-ss"); 
	
	//Digital video was first introduced commercially in 1986 with the Sony D1 format
	private final String DEFAULT_AFTER_DATE = "01-01-1986 00-00-00";
	
	private final DatabaseBrowseWindow                   DATABASE_BROWSE_WINDOW;
	private final JTable                                 RESULT_TABLE;
	private final ConcurrentHashMap<BlobRecord, Integer> BLOB_RECORDS_MAP;
	private final ArrayList<BlobRecord>                  BLOB_RECORDS_LIST;
	
	private DatePicker mBeforeDatePicker;
	private DatePicker mAfterDatePicker;
	private TimePicker mBeforeTimePicker;
	private TimePicker mAfterTimePicker;
	private JTextField mFilenameField;
	private JTextField mIpField;
	private JTextField mPortField;
		
	private JButton    mSearchDatabaseButton;
	private JButton    mClearFieldsButton;
	
	public AdvancedDatabaseSearchWindow(final DatabaseBrowseWindow databaseBrowseWindow, final JTable resultTable, final ConcurrentHashMap<BlobRecord, Integer> blobRecordsMap,
		final ArrayList<BlobRecord> blobRecordsList) {
		DATABASE_BROWSE_WINDOW = databaseBrowseWindow;
		RESULT_TABLE           = resultTable;
		BLOB_RECORDS_MAP       = blobRecordsMap;
		BLOB_RECORDS_LIST      = blobRecordsList;
		
		buildFrame();
		
		WindowRegistry.getInstance().registerFrame(this, "AdvDatabaseSearchWindow");
	}
	
	private String getSelectedBeforeDateString() {
		Date beforeDate = getSelectedBeforeDate();
		
		if(beforeDate != null) {
			return ADVANCED_SEARCH_DATE_FORMAT.format(getSelectedBeforeDate());
		} else {
			return ADVANCED_SEARCH_DATE_FORMAT.format(new Date());
		}
	}
	
	private String getSelectedAfterDateString() {
		Date afterDate = getSelectedAfterDate();
		
		if(afterDate != null) {
			return ADVANCED_SEARCH_DATE_FORMAT.format(getSelectedAfterDate());
		} else {
			return DEFAULT_AFTER_DATE;
		}
	}
	
	private Date getSelectedBeforeDate() {
		LocalDate beforeLocalDate = mBeforeDatePicker.getDate();
		LocalTime beforeLocalTime = mBeforeTimePicker.getTime();
		
		if(beforeLocalDate == null) {
			return null;
		} else if(beforeLocalTime == null) {
			return null;
		}
		
		LocalDateTime beforeDateTime = LocalDateTime.of(beforeLocalDate, beforeLocalTime);
		
		Date beforeDate = Date.from(beforeDateTime.atZone(ZoneId.systemDefault()).toInstant());
		
		return beforeDate;
	}
	
	private Date getSelectedAfterDate() {
		LocalDate afterLocalDate = mAfterDatePicker.getDate();
		LocalTime afterLocalTime = mAfterTimePicker.getTime();
		
		if(afterLocalDate == null) {
			return null;
		} else if(afterLocalTime == null) {
			return null;
		}
		
		LocalDateTime afterDateTime = LocalDateTime.of(afterLocalDate, afterLocalTime);
		
		Date afterDate = Date.from(afterDateTime.atZone(ZoneId.systemDefault()).toInstant());
		
		return afterDate;
	}
	
	public void buildFrame() {
		buildMenuBar();
		
		this.setLayout(new BorderLayout());
		
		JPanel searchPanel = new JPanel();
		searchPanel.setLayout(new GridLayout(10, 1));
		searchPanel.setBorder(BorderUtils.getEmptyBorder()); 
		
		JPanel beforeDatePanel = new JPanel();
		beforeDatePanel.setLayout(new FlowLayout());
		
		JLabel beforeDateFieldLabel = new JLabel("Before");
		beforeDatePanel.add(beforeDateFieldLabel);
		mBeforeDatePicker = new DatePicker();
		beforeDatePanel.add(mBeforeDatePicker);
		mBeforeTimePicker = new TimePicker();
        beforeDatePanel.add(mBeforeTimePicker);
       
		//
		
		JPanel afterDatePanel = new JPanel();
		afterDatePanel.setLayout(new FlowLayout());
		
		JLabel afterDateFieldLabel = new JLabel("After");
		afterDatePanel.add(afterDateFieldLabel);
		mAfterDatePicker = new DatePicker();
		afterDatePanel.add(mAfterDatePicker);
		mAfterTimePicker = new TimePicker();
        afterDatePanel.add(mAfterTimePicker);
		
        JLabel filenameFieldLabel = new JLabel("Filename");
		mFilenameField = new JTextField();
		JLabel ipFieldLabel = new JLabel("IP Address");
		mIpField = new JTextField();
		JLabel portFieldLabel = new JLabel("Port");
		mPortField = new JTextField("8080");
		
		mSearchDatabaseButton = new JButton("Search Database");
		mSearchDatabaseButton.addActionListener(this);
		mSearchDatabaseButton.setPreferredSize(new Dimension(200, 50));
		
		mClearFieldsButton = new JButton("Clear Fields");
		mClearFieldsButton.addActionListener(this);
		mClearFieldsButton.setPreferredSize(new Dimension(200, 50));
		
		//searchPanel.add(afterDateFieldLabel);
		searchPanel.add(afterDatePanel);
		//searchPanel.add(beforeDateFieldLabel);
		searchPanel.add(beforeDatePanel);
		
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
		
		this.add(searchScrollPane, BorderLayout.CENTER);
		this.setPreferredSize(new Dimension(440, 600));
		
		this.setTitle("Search Database");
		this.pack();
		WindowUtils.setFrameIcon(this);
		//WindowUtils.maximize(this);
		this.setVisible(true);
		WindowUtils.center(this);
	}

	private void performSearchDatabaseAction() {
		AdvancedSearchDatabaseThread advancedSearchDatabaseThread = new AdvancedSearchDatabaseThread(DATABASE_BROWSE_WINDOW, RESULT_TABLE, getSelectedAfterDateString(), getSelectedBeforeDateString(), 
				mFilenameField.getText(), mIpField.getText(), mPortField.getText(), BLOB_RECORDS_LIST, BLOB_RECORDS_MAP);
		advancedSearchDatabaseThread.start();

		this.dispose();
		
		//mResizeOnSearch = true;
	}
	
	private void performClearFieldsAction() {
		mBeforeDatePicker.clear();
		mAfterDatePicker.clear();
		mBeforeTimePicker.clear();
		mAfterTimePicker.clear();
		mFilenameField.setText("");
		mIpField.setText("");
		mPortField.setText("");
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() instanceof JButton) {
			JButton button = (JButton) event.getSource();

			if(button == mSearchDatabaseButton) {
				performSearchDatabaseAction();
			} else if(button == mClearFieldsButton) {
				performClearFieldsAction();
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
						ThreadUtils.addThreadToHandleList("AdvancedDbBrowse Exit", this);
						
						AdvancedDatabaseSearchWindow.this.dispose();
						
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
						ThreadUtils.addThreadToHandleList("AdvancedDbBrowse Search", this);
						
						performSearchDatabaseAction();
						
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
						ThreadUtils.addThreadToHandleList("AdvancedDbBrowse ClearFields", this);
						
						performClearFieldsAction();
						
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
						ThreadUtils.addThreadToHandleList("AdvancedDbBrowse OpenDoc", this);
						
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
						ThreadUtils.addThreadToHandleList("AdvancedDbBrowse About", this);
						
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

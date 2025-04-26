package com.keypointforensics.videotriage.gui.localfile.wizard.video;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import com.keypointforensics.videotriage.util.BorderUtils;
import com.keypointforensics.videotriage.util.FontUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.Utils;
import com.keypointforensics.videotriage.util.WebUtils;
import com.keypointforensics.videotriage.util.WindowUtils;
import com.keypointforensics.videotriage.window.WindowRegistry;

public class FailedToProcessListWindow extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3226035676767340984L;
	
	private final ArrayList<String> LIST_OF_FAILED_PATHS;

	private JTextArea mFailedVideoTextArea;
	private JButton mDoneButton;
		
	public FailedToProcessListWindow(final ArrayList<String> failedPathList) {
		LIST_OF_FAILED_PATHS = failedPathList;
		
		mFailedVideoTextArea = new JTextArea();
		mFailedVideoTextArea.setEditable(true);
		WindowUtils.setTextAreaUpdatePolicy(mFailedVideoTextArea);
		JScrollPane licenseAreaScrollPane = new JScrollPane(mFailedVideoTextArea);
		WindowUtils.setScrollBarIncrement(licenseAreaScrollPane);
		
		WindowRegistry.getInstance().registerFrame(this, "FailedToProcessList");
	}

	public void setFailedToProcessText() {
		for(String absoluteVideoFilePath : LIST_OF_FAILED_PATHS) {
			mFailedVideoTextArea.append(absoluteVideoFilePath);
			mFailedVideoTextArea.append("\n");
		}
	}
	
	public void buildAndDisplay() {
		buildMenuBar();

		this.setLayout(new BorderLayout());
		
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout());
		contentPanel.setBorder(BorderUtils.getEmptyBorder());
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());
		buttonPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
		
		mDoneButton = new JButton("Done");
		mDoneButton.setFont(FontUtils.DEFAULT_FONT);
		mDoneButton.addActionListener(this);
		
		buttonPanel.add(mDoneButton, BorderLayout.CENTER);
		
		JScrollPane termsGridPanelScrollPane = new JScrollPane(mFailedVideoTextArea);
		termsGridPanelScrollPane.setPreferredSize(new Dimension(700, 400));
		
		contentPanel.add(termsGridPanelScrollPane, BorderLayout.CENTER);
		contentPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		this.add(contentPanel, BorderLayout.CENTER);
		
		this.setTitle("Failed Videos");
		this.pack();
		WindowUtils.setFrameIcon(this);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setVisible(true);
		WindowUtils.center(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() instanceof JButton) {
			JButton button = (JButton) event.getSource();

			if (button == mDoneButton) {			
				FailedToProcessListWindow.this.dispose();
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
						ThreadUtils.addThreadToHandleList("FailedToProcessListWindow Exit", this);

						FailedToProcessListWindow.this.dispose();

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
						ThreadUtils.addThreadToHandleList("FailedToProcessListWindow OpenDoc", this);
						
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
						ThreadUtils.addThreadToHandleList("FailedToProcessListWindow Exit", this);
						
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

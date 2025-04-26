package com.keypointforensics.videotriage.video;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.keypointforensics.videotriage.gui.imagepanel.CenteredSimpleImagePanel;
import com.keypointforensics.videotriage.gui.imagepanel.ScalableSimpleImagePanel;
import com.keypointforensics.videotriage.util.BorderUtils;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.FontUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.Utils;
import com.keypointforensics.videotriage.util.WebUtils;
import com.keypointforensics.videotriage.util.WindowUtils;
import com.keypointforensics.videotriage.window.WindowRegistry;

public class PreviewFilterOutputWindow extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3226035676767340984L;
	
	private static final String BEFORE_PREVIEW_IMAGE_FILENAME = "before_preview.jpg";
	
	private final String FILTER_NAME;
	private final String AFTER_IMAGE_FILENAME;
	
	private JButton mDoneButton;
	
	public PreviewFilterOutputWindow(final String filterName, final String afterImageFilename) {		
		FILTER_NAME          = filterName;
		AFTER_IMAGE_FILENAME = afterImageFilename;
		
		WindowRegistry.getInstance().registerFrame(this, "PreviewFilterWindow");
	}
	
	public void buildAndDisplay() {
		buildMenuBar();

		this.setLayout(new BorderLayout());
		
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new GridLayout(2, 1));
		contentPanel.setBorder(BorderUtils.getEmptyBorder());
		
		CenteredSimpleImagePanel topCenteredSimpleImagePanel = new CenteredSimpleImagePanel(FileUtils.FILTER_PREVIEW_DIRECTORY + BEFORE_PREVIEW_IMAGE_FILENAME);
		topCenteredSimpleImagePanel.setBorder(BorderUtils.getEmptyBorder());
		
		CenteredSimpleImagePanel bottomCenteredSimpleImagePanel = new CenteredSimpleImagePanel(FileUtils.FILTER_PREVIEW_DIRECTORY + AFTER_IMAGE_FILENAME);
		bottomCenteredSimpleImagePanel.setBorder(BorderUtils.getEmptyBorder());
		
		contentPanel.add(topCenteredSimpleImagePanel);
		contentPanel.add(bottomCenteredSimpleImagePanel);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());
		buttonPanel.setBorder(new EmptyBorder(0, 5, 5, 5));
		
		mDoneButton = new JButton("Done");
		mDoneButton.setFont(FontUtils.DEFAULT_FONT);
		mDoneButton.addActionListener(this);
		
		buttonPanel.add(mDoneButton, BorderLayout.CENTER);
		
		this.add(contentPanel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);
		
		this.setPreferredSize(new Dimension(800, 800));
		
		this.setTitle(FILTER_NAME + " Preview");
		this.pack();
		WindowUtils.setFrameIcon(this);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		//WindowUtils.center(this);
		WindowUtils.maximize(this);
		this.setVisible(true);
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() instanceof JButton) {
			JButton button = (JButton) event.getSource();

			if (button == mDoneButton) {			
				PreviewFilterOutputWindow.this.dispose();
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
						ThreadUtils.addThreadToHandleList("DatabaseExport Exit", this);

						PreviewFilterOutputWindow.this.dispose();

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
						ThreadUtils.addThreadToHandleList("DatabaseExport OpenDoc", this);
						
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
						ThreadUtils.addThreadToHandleList("DatabaseExport About", this);
						
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

package com.keypointforensics.videotriage.gui.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.keypointforensics.videotriage.legacy.WindowsVideoFrameExtractorLegacy;
import com.keypointforensics.videotriage.util.BorderUtils;
import com.keypointforensics.videotriage.util.CursorUtils;
import com.keypointforensics.videotriage.util.FontUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.Utils;
import com.keypointforensics.videotriage.util.WebUtils;
import com.keypointforensics.videotriage.util.WindowUtils;
import com.keypointforensics.videotriage.window.WindowRegistry;

public class LocalEvidenceMetadataWindow extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2354314726861689224L;
	
	private JTextArea mMetadataTextArea;
	private JButton mDoneButton;
	
	public LocalEvidenceMetadataWindow() {
		mMetadataTextArea = new JTextArea();
		mMetadataTextArea.setEditable(false);
		WindowUtils.setTextAreaUpdatePolicy(mMetadataTextArea);
		JScrollPane licenseAreaScrollPane = new JScrollPane(mMetadataTextArea);
		WindowUtils.setScrollBarIncrement(licenseAreaScrollPane);
		
		WindowRegistry.getInstance().registerFrame(this, "LocalEvidenceMetadata");
	}

	public void setMetadataText(final String text) {
		mMetadataTextArea.setText(text);
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
		
		JScrollPane termsGridPanelScrollPane = new JScrollPane(mMetadataTextArea);
		termsGridPanelScrollPane.setPreferredSize(new Dimension(700, 400));
		
		contentPanel.add(termsGridPanelScrollPane, BorderLayout.CENTER);
		contentPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		this.add(contentPanel, BorderLayout.CENTER);
		
		this.setTitle("Video Details");
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
				LocalEvidenceMetadataWindow.this.dispose();
			}
		}
	}

	private void buildMenuBar() {
		JMenuBar menuBar;
		JMenu menu;

		menuBar = new JMenuBar();

		menu = new JMenu("File");
		menuBar.add(menu);

		JMenuItem menuItem = new JMenuItem("Copy to Clipboard");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("EvidenceMetadata CopyToClipboard", this);

						String metadataText = mMetadataTextArea.getText();
						
						Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
						clipboard.setContents(new StringSelection(metadataText), null);

						if(metadataText.length() > 240) {
							metadataText = metadataText.substring(0, 240) + "\n... (Continues)";
						}
						
						Utils.displayMessageDialog("Clipboard Success", "Copied:\n" + metadataText);
						
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
						ThreadUtils.addThreadToHandleList("EvidenceMetadata Exit", this);

						LocalEvidenceMetadataWindow.this.dispose();

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
				performSelectFileAction();
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
						ThreadUtils.addThreadToHandleList("EvidenceMetadata OpenDoc", this);
						
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
						ThreadUtils.addThreadToHandleList("EvidenceMetadata About", this);
						
						Utils.displayMessageDialog("About", Utils.SOFTWARE_NAME + " \nVersion: " + Utils.SOFTWARE_VERSION + "\n© " + Utils.AUTHOR_NAME);
						
						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}
			
		});
		menu.add(menuItem);
		
		this.setJMenuBar(menuBar);
	}
	
	private void updateVideoInformationTextArea(final File videoFile) {
		CursorUtils.setBusyCursor(this);
		
		mMetadataTextArea.setText("");
		mMetadataTextArea.append(WindowsVideoFrameExtractorLegacy.getVideoFileInformation(videoFile.getAbsolutePath()) + "\n");
		mMetadataTextArea.revalidate();
		
		CursorUtils.setDefaultCursor(this);
	}
	 
	public void performSelectFileAction() {	
		new Thread() {
			@Override
			public void run() {	
				ThreadUtils.addThreadToHandleList("LocalEvidence GetVidDetails", this);
				
				Boolean old = UIManager.getBoolean("FileChooser.readOnly");  
				UIManager.put("FileChooser.readOnly", Boolean.TRUE);  		
				
				final JFileChooser fileChooser = new JFileChooser(); 
				final FileSelectVideoPreviewAccessory imagePreviewAccessory = new FileSelectVideoPreviewAccessory();
				fileChooser.setAccessory(imagePreviewAccessory);
				fileChooser.addPropertyChangeListener(imagePreviewAccessory);
				
				final int fileChooserResult = fileChooser.showOpenDialog(LocalEvidenceMetadataWindow.this);
						
				if(fileChooserResult == JFileChooser.APPROVE_OPTION) {					
					final File selectedFile = fileChooser.getSelectedFile();
					
					if(selectedFile.isDirectory() == false) {
						EventQueue.invokeLater(new Runnable() { 
							public void run() {
								updateVideoInformationTextArea(selectedFile);
							}
						});
					}
				}
						
				UIManager.put("FileChooser.readOnly", old); 
				
				ThreadUtils.removeThreadFromHandleList(this);
			}
		}.start();
	}
}

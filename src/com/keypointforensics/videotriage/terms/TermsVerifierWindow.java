package com.keypointforensics.videotriage.terms;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import com.keypointforensics.videotriage.thread.TermsVerificationThread;
import com.keypointforensics.videotriage.util.BorderUtils;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.FontUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.Utils;
import com.keypointforensics.videotriage.util.WindowUtils;

public class TermsVerifierWindow extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3226035676767340984L;
	
	private final File LICENSE_FILE      = new File(FileUtils.LICENSE_DIRECTORY + "License.txt");
	private final File VERIFICATION_FILE = new File(FileUtils.LICENSE_DIRECTORY + ".verified");
	
	private JTextArea mLicenseTextArea;
	private JButton mAcceptTermsButton;

	private TermsVerificationThread mTermsVerificationThread;
	
	public TermsVerifierWindow() {
		mLicenseTextArea = new JTextArea();
		mLicenseTextArea.setEditable(false);
		WindowUtils.setTextAreaUpdatePolicy(mLicenseTextArea);
		JScrollPane licenseAreaScrollPane = new JScrollPane(mLicenseTextArea);
		WindowUtils.setScrollBarIncrement(licenseAreaScrollPane);
		
		try(BufferedReader br = new BufferedReader(new FileReader(LICENSE_FILE))) {
		    for(String line; (line = br.readLine()) != null; ) {
		        mLicenseTextArea.append(line);
		        mLicenseTextArea.append("\n");
		    }
		} catch (FileNotFoundException e) {
			Utils.displayMessageDialog("Terms Issue", "Failed to load " + Utils.SOFTWARE_NAME + " terms. Error ID: 003001.");
			
			mTermsVerificationThread.verificationAttempted(false);
		} catch (IOException e) {
			Utils.displayMessageDialog("Terms Issue", "Failed to load " + Utils.SOFTWARE_NAME + " terms. Error ID: 003013.");
			
			mTermsVerificationThread.verificationAttempted(false);
		}

		new Thread() {
			@Override
			public void run() {
				ThreadUtils.addThreadToHandleList("Terms Verify", this);

				mTermsVerificationThread = new TermsVerificationThread();
				mTermsVerificationThread.start();
				
				ThreadUtils.removeThreadFromHandleList(this);
			}
		}.start();
	}

	public TermsVerificationThread getVerificationThread() {
		return mTermsVerificationThread;
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
		
		mAcceptTermsButton = new JButton("Accept Terms");
		mAcceptTermsButton.setFont(FontUtils.DEFAULT_FONT);
		mAcceptTermsButton.addActionListener(this);
		
		buttonPanel.add(mAcceptTermsButton, BorderLayout.CENTER);
		
		JScrollPane termsGridPanelScrollPane = new JScrollPane(mLicenseTextArea);
		termsGridPanelScrollPane.setPreferredSize(new Dimension(700, 400));
		
		contentPanel.add(termsGridPanelScrollPane, BorderLayout.CENTER);
		contentPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		this.add(contentPanel, BorderLayout.CENTER);
		
		this.setTitle(Utils.SOFTWARE_NAME + " Terms");
		this.pack();
		WindowUtils.setFrameIcon(this);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
		WindowUtils.center(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() instanceof JButton) {
			JButton button = (JButton) event.getSource();

			if (button == mAcceptTermsButton) {			
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("Terms Accept", this);

						try {
							verify();
						} catch (IOException e) {
							Utils.displayMessageDialog("Terms Issue", "Failed to verify " + Utils.SOFTWARE_NAME + " terms. Error ID: 003022.");
						}
							
						mTermsVerificationThread.verificationAttempted(true);
						
						ThreadUtils.removeThreadFromHandleList(this);
						
						TermsVerifierWindow.this.dispose();
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

		JMenuItem menuItem = new JMenuItem("Exit");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				new Thread() {
					@Override
					public void run() {
						ThreadUtils.addThreadToHandleList("Terms Exit", this);

						TermsVerifierWindow.this.dispose();

						ThreadUtils.removeThreadFromHandleList(this);
					}
				}.start();
			}

		});
		menu.add(menuItem);

		this.setJMenuBar(menuBar);
	}

	public void verify() throws IOException {
		VERIFICATION_FILE.createNewFile();
	}
	
	public boolean needsVerification() {
		return !(VERIFICATION_FILE.exists());
	}
}

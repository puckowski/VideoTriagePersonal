package com.keypointforensics.videotriage.util;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.keypointforensics.videotriage.progress.ProgressBundle;

public class ProgressUtils {

	/*
	 * Author: Daniel Puckowski
	 */
	
	public static ProgressBundle getProgressBundle(final String title, int progressTarget) {
		JFrame progressBarFrame = new JFrame();
		progressBarFrame.setLayout(new BorderLayout());
		
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout());
		
		JProgressBar genericProgressBar = new JProgressBar();
		genericProgressBar.setMinimum(0);
		genericProgressBar.setMaximum(progressTarget);
		progressBarFrame.add(genericProgressBar);
		
		contentPanel.add(genericProgressBar, BorderLayout.CENTER);
		
		progressBarFrame.add(contentPanel, BorderLayout.CENTER);
		
		progressBarFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		WindowUtils.setFrameIcon(progressBarFrame);
		progressBarFrame.setTitle(title);
		progressBarFrame.setPreferredSize(new Dimension(440, 80));
		progressBarFrame.pack();
		progressBarFrame.setVisible(true);
		progressBarFrame.setResizable(false);
		WindowUtils.center(progressBarFrame);
		
		return new ProgressBundle(progressBarFrame, genericProgressBar);
	}
	
	public static ProgressBundle getProgressBundle(final String title) {
		JFrame progressBarFrame = new JFrame();
		progressBarFrame.setLayout(new BorderLayout());
		
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout());
		
		JProgressBar genericProgressBar = new JProgressBar();
		genericProgressBar.setMinimum(0);
		progressBarFrame.add(genericProgressBar);
		
		contentPanel.add(genericProgressBar, BorderLayout.CENTER);
		
		progressBarFrame.add(contentPanel, BorderLayout.CENTER);
		
		progressBarFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		WindowUtils.setFrameIcon(progressBarFrame);
		progressBarFrame.setTitle(title);
		progressBarFrame.setPreferredSize(new Dimension(440, 80));
		progressBarFrame.pack();
		progressBarFrame.setVisible(true);
		progressBarFrame.setResizable(false);
		WindowUtils.center(progressBarFrame);
		
		return new ProgressBundle(progressBarFrame, genericProgressBar);
	}
	
	public static ProgressBundle getIndeterminateProgressBundle(final String title) {
		JFrame progressBarFrame = new JFrame();
		progressBarFrame.setLayout(new BorderLayout());
		
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout());
		
		JProgressBar genericProgressBar = new JProgressBar();
		genericProgressBar.setIndeterminate(true);
		progressBarFrame.add(genericProgressBar);
		
		contentPanel.add(genericProgressBar, BorderLayout.CENTER);
		
		progressBarFrame.add(contentPanel, BorderLayout.CENTER);
		
		progressBarFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		WindowUtils.setFrameIcon(progressBarFrame);
		progressBarFrame.setTitle(title);
		progressBarFrame.setPreferredSize(new Dimension(440, 80));
		progressBarFrame.pack();
		progressBarFrame.setVisible(true);
		progressBarFrame.setResizable(false);
		WindowUtils.center(progressBarFrame);
		
		return new ProgressBundle(progressBarFrame, genericProgressBar);
	}
}

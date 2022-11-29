package com.keypointforensics.videotriage.progress;

import javax.swing.JFrame;
import javax.swing.JProgressBar;

public class ProgressBundle {

	public JFrame frame;
	public JProgressBar progressBar;
	
	public ProgressBundle(final JFrame frame, final JProgressBar progressBar) {
		this.frame = frame;
		this.progressBar = progressBar;
	}
}

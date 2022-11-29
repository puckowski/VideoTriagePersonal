package com.keypointforensics.videotriage.gui.data;

import java.awt.Color;
import java.awt.Graphics;
import java.io.File;

import javax.swing.JPanel;

import com.keypointforensics.videotriage.util.FileUtils;

public class SimpleDiskUsageChart extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6994902236068343822L;
	
	private long mApplicationDataUsage;
	private long mTotalSpace;
	
	private double mUsagePercent;
	
	public SimpleDiskUsageChart() {
		determineUsage();
	}
	
	public double getUsagePercent() {
		return mUsagePercent;
	}
	
	private void determineUsage() {
		mApplicationDataUsage = FileUtils.getFolderSize(new File(FileUtils.ROOT_DIRECTORY));
		mTotalSpace = FileUtils.getTotalSpace();
		
		mUsagePercent = (double) mApplicationDataUsage / (double) mTotalSpace;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		g.setColor(getBackground());
	    g.fillRect(0, 0, this.getWidth(), this.getHeight());
	    
	    g.setColor(Color.GREEN);
	    g.fillRoundRect(4, 4, this.getWidth() - 8, this.getHeight() - 8, 5, 5);
	    
	    g.setColor(Color.RED);
	    g.fillRoundRect(4, 4, ((int) mUsagePercent * this.getWidth()) - 8, this.getHeight() - 8, 5, 5);
	}
}

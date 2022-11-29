package com.keypointforensics.videotriage.gui.data;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.keypointforensics.videotriage.util.BorderUtils;

public class SimpleDiskUsageChartPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7278029665225572551L;

	private SimpleDiskUsageChart mSimpleDiskUsageChart;
		
	public SimpleDiskUsageChartPanel() {
		buildPanel();
	}
	
	private void buildPanel() {
		this.setLayout(new BorderLayout());
		this.setOpaque(true);
		this.setBorder(BorderUtils.getEmptyBorder());
		
		JPanel legendPanel = new JPanel();
		legendPanel.setLayout(new FlowLayout());
		
		SimpleColoredButton button = new SimpleColoredButton(Color.RED, this.getBackground());
		button.setPreferredSize(new Dimension(20, 20));
		
		JLabel label = new JLabel("Application Usage");
		
		legendPanel.add(button);
		legendPanel.add(label);
		
		button = new SimpleColoredButton(Color.GREEN, this.getBackground());
		button.setPreferredSize(new Dimension(20, 20));
		
		label = new JLabel("Free Space");
		
		legendPanel.add(button);
		legendPanel.add(label);
		
		mSimpleDiskUsageChart = new SimpleDiskUsageChart();
		
		String usageString = String.format("%.2f", mSimpleDiskUsageChart.getUsagePercent());
		
		label = new JLabel("Usage Percent: " + usageString);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		
		this.add(label, BorderLayout.NORTH);
		this.add(mSimpleDiskUsageChart, BorderLayout.CENTER);
		this.add(legendPanel, BorderLayout.SOUTH);
	}
}

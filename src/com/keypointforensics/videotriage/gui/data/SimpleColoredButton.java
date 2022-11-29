package com.keypointforensics.videotriage.gui.data;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JButton;

public class SimpleColoredButton extends JButton {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5860801910989481483L;
	
	private Color mColor;
	private Color mBackground;
	
	public SimpleColoredButton(Color color, Color background) {
		mColor = color;
		mBackground = background;
		
		this.setEnabled(false);
	}
	@Override 
	public void paintComponent(Graphics g) {		
		g.setColor(mBackground);
	    g.fillRect(0, 0, this.getWidth(), this.getHeight());
	    
	    g.setColor(mColor);
	    g.fillRoundRect(2, 2, this.getWidth() - 4, this.getHeight() - 4, 2, 2);
	}
}

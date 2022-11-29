package com.keypointforensics.videotriage.gui.main;

import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JSlider;

public class RotateSteppingSlider extends JSlider {

	/*
	 * Author: Daniel Puckowski
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8914321684173868415L;
	
	public static final int NO_ROTATION         = 0;
	public static final int NINTEY_DEGREES      = 90;
	public static final int ONE_EIGHTY_DEGREES  = 180;
	public static final int TWO_SEVENTY_DEGREES = 270;
	public static final int THREE_SIXTY_DEGREES = 360;
	
	private static final Integer[] VALUES = { 
		NO_ROTATION, 
		NINTEY_DEGREES, 
		ONE_EIGHTY_DEGREES, 
		TWO_SEVENTY_DEGREES, 
		THREE_SIXTY_DEGREES 
	};
	
	private static final Hashtable<Integer, JLabel> LABELS = new Hashtable<>();

	static {
		for (int i = 0; i < VALUES.length; ++i) {
			LABELS.put(i, new JLabel(VALUES[i].toString()));
		}
	}

	public RotateSteppingSlider() {
		super(0, VALUES.length - 1, 0);
		
		setLabelTable(LABELS);
		setPaintTicks(true);
		setPaintLabels(true);
		setSnapToTicks(true);
		setMajorTickSpacing(1);
	}

	public int getDomainValue() {
		return VALUES[getValue()];
	}
}
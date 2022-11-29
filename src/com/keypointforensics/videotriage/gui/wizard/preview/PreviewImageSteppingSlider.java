package com.keypointforensics.videotriage.gui.wizard.preview;

import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JSlider;

public class PreviewImageSteppingSlider extends JSlider {

	/*
	 * Author: Daniel Puckowski
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8914321684173868415L;
	
	public static final int PREVIEW_COUNT_FOUR        = 4;
	public static final int PREVIEW_COUNT_NINE        = 9;
	public static final int PREVIEW_COUNT_SIXTEEN     = 16;
	public static final int PREVIEW_COUNT_TWENTY_FIVE = 25;
	public static final int PREVIEW_COUNT_THIRTY_SIX  = 36;
	
	private static final Integer[] VALUES = { 
		PREVIEW_COUNT_FOUR,
		PREVIEW_COUNT_NINE,
		PREVIEW_COUNT_SIXTEEN,
		PREVIEW_COUNT_TWENTY_FIVE,
		PREVIEW_COUNT_THIRTY_SIX
	};
	
	private static final Hashtable<Integer, JLabel> LABELS = new Hashtable<>();

	static {
		for (int i = 0; i < VALUES.length; ++i) {
			LABELS.put(i, new JLabel(VALUES[i].toString()));
		}
	}

	public PreviewImageSteppingSlider() {
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
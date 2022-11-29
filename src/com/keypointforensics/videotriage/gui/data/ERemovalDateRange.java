package com.keypointforensics.videotriage.gui.data;

public enum ERemovalDateRange {

	LAST_HOUR("Last Hour"),
	LAST_DAY("Last Day"),
	LAST_WEEK("Last Week"),
	LAST_MONTH("Last Month"),
	ALL_TIME("All Time");
	
	private final String DISPLAY_TEXT;
	
	private ERemovalDateRange(final String displayText) {
		DISPLAY_TEXT = displayText;
	}
	
	@Override
	public String toString() {
		return DISPLAY_TEXT;
	}
}

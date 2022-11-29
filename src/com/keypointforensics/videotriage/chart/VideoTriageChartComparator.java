package com.keypointforensics.videotriage.chart;

import java.util.Comparator;

public class VideoTriageChartComparator implements Comparator<EVideoTriageChart> {

	@Override
	public int compare(EVideoTriageChart lhsChartType, EVideoTriageChart rhsChartType) {
		String self = lhsChartType.toString();
    	String other = rhsChartType.toString();
    	
    	return self.compareTo(other);
	}
}

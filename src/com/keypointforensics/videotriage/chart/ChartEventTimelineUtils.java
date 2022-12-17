package com.keypointforensics.videotriage.chart;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.keypointforensics.videotriage.util.Utils;

public class ChartEventTimelineUtils {

	public static List<String> sortEventTimeline(final List<String> eventTimeline) {
		final SimpleDateFormat reportDateFormat = new SimpleDateFormat(Utils.REPORT_DATE_FORMAT_DASH_LONG);

		return eventTimeline.stream().sorted(new Comparator<String>() {
			@Override
			public int compare(String lhs, String rhs) {
				try {
					final Date lhsDate = reportDateFormat.parse(lhs);
					final Date rhsDate = reportDateFormat.parse(rhs);

					if (lhsDate.getTime() < rhsDate.getTime()) {
						return -1;
					} else if (lhsDate.getTime() == rhsDate.getTime()) {
						return 0;
					} else {
						return 1;
					}
				} catch (final ParseException parseException) {
					// Do nothing, leave unsorted
				}

				return 0;
			}
		}).collect(Collectors.toList());
	}
}

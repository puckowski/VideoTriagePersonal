package com.keypointforensics.videotriage.list;

import java.util.ArrayList;

public class SortedList<T> extends ArrayList<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6904774124852991904L;

	public void insertSorted(T value) {
		int insertPoint = insertPoint(value);
		add(insertPoint, value);
	}

	@SuppressWarnings("unchecked")
	private int insertPoint(T key) {
		int low = 0;
		int high = size() - 1;

		int mid;
		Comparable<T> midVal;
		int cmp;

		while (low <= high) {
			mid = (low + high) >>> 1;
			midVal = (Comparable<T>) get(mid);
			cmp = midVal.compareTo(key);

			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else {
				return mid;
			}
		}

		return low;
	}
}
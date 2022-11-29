package com.keypointforensics.videotriage.util;

public class StringUtils {

	public static int countSubstringOccurrences(final String data, final String substring) {
		int lastIndex = 0;
		int count = 0;

		while(lastIndex != -1) {
		    lastIndex = data.indexOf(substring, lastIndex);

		    if(lastIndex != -1) {
		        count++;
		        lastIndex += substring.length();
		    }
		}

		return count;
	}
	
}

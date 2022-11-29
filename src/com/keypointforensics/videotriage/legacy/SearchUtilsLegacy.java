package com.keypointforensics.videotriage.legacy;

import java.util.ArrayList;

public class SearchUtilsLegacy {

	/*
	 * Author: Daniel Puckowski
	 */
	
	public static ArrayList<String> formatResultList(ArrayList<String> filenames) {
		if(filenames == null || filenames.isEmpty() == true) {
			return filenames;
		}
		
		for(int i = 0; i < filenames.size(); ++i) {			
			if(filenames.get(i).contains("false_positives") == true) {
				filenames.remove(i);
				i--;
			} else if(filenames.get(i).contains(".local.cache") == true) {
				filenames.remove(i);
				i--;
			}
		}
		
		return filenames;
	}
}

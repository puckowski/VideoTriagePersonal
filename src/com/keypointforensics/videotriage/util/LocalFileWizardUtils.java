package com.keypointforensics.videotriage.util;

import java.util.ArrayList;

public class LocalFileWizardUtils {

	/*
	 * Author: Daniel Puckowski
	 */
	
	public static ArrayList<String> parseLocalWizardListOfFiles(String formattedListOfFiles) {
		ArrayList<String> listOfFiles = new ArrayList<String>();
		
		while(formattedListOfFiles.contains("\"")) {
			formattedListOfFiles = formattedListOfFiles.substring(formattedListOfFiles.indexOf("\"") + 1, formattedListOfFiles.length());
			listOfFiles.add(formattedListOfFiles.substring(0, formattedListOfFiles.indexOf("\"")));
			formattedListOfFiles = formattedListOfFiles.substring(formattedListOfFiles.indexOf("\"") + 1, formattedListOfFiles.length());
		}
		
		return listOfFiles;
	}
	
	public static ArrayList<String> getLocalWizardListOfFiles(String localWizardListOfFiles) {
		ArrayList<String> listOfFiles = new ArrayList<String>();
		
		if(localWizardListOfFiles.isEmpty() == true) {
			return listOfFiles;
		}
		
		String[] fileListArray = localWizardListOfFiles.split("\\n");
		
		for(int i = 0; i < fileListArray.length; ++i) {			
			listOfFiles.add(fileListArray[i]);
		}
				
		return listOfFiles;
	}
	
	public static String getLocalWizardFormattedListOfFiles(String localWizardListOfFiles) {
		StringBuilder formattedList = new StringBuilder();
		
		String[] fileList = localWizardListOfFiles.split("\\n");
		
		for(int i = 0; i < fileList.length - 1; ++i) {			
			formattedList.append("\"");
			formattedList.append(fileList[i]);
			formattedList.append("\", ");
		}
		
		formattedList.append("\"");
		formattedList.append(fileList[fileList.length - 1]);
		formattedList.append("\"");
		
		return formattedList.toString();
	}
}

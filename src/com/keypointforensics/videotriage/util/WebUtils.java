package com.keypointforensics.videotriage.util;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

public class WebUtils {

	public static final String URL_STRING_DOCUMENTATION = "https://keypointforensics.blogspot.com/";
	
	public static ArrayList<String> downloadTextFromUrl(final String urlText, final int initArraySize) throws IOException {
		URL url = new URL(urlText);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

		ArrayList<String> lineList = new ArrayList<String>(initArraySize);
		String inputLine;
		
		while ((inputLine = in.readLine()) != null) {
			lineList.add(inputLine);
		}
		
		in.close();
		
		return lineList;
	}
	
	public static ArrayList<String> downloadTextFromUrl(final String urlText) throws IOException {
		URL url = new URL(urlText);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

		ArrayList<String> lineList = new ArrayList<String>(1000);
		String inputLine;
		
		while ((inputLine = in.readLine()) != null) {
			lineList.add(inputLine);
		}
		
		in.close();
		
		return lineList;
	}
	
	public static boolean openWebpage(URI uri) {
	    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
	   
	    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
	        try {
	            desktop.browse(uri);
	            return true;
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	    
	    return false;
	}

	public static boolean openWebpage(URL url) {
	    try {
	        return openWebpage(url.toURI());
	    } catch (URISyntaxException e) {
	        e.printStackTrace();
	    }
	    
	    return false;
	}

}

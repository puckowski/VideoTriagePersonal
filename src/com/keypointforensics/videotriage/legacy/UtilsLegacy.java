package com.keypointforensics.videotriage.legacy;

import java.awt.Cursor;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import com.keypointforensics.videotriage.util.Utils;
import com.keypointforensics.videotriage.util.WindowUtils;

public class UtilsLegacy {

	/*
	 * Author: Daniel Puckowski
	 */
	
	public static String getTimeStamp() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
				Utils.DEFAULT_DATE_FORMAT);

		return simpleDateFormat.format(calendar.getTime());
	}

	public static String getFormattedTimeStamp() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
				Utils.DEFAULT_DATE_FORMAT);

		return simpleDateFormat.format(calendar.getTime()).replaceAll(":", "-");
	}
	
	public static void displayMessageDialog(String title, final String message) {
		if(title == null || title.isEmpty()) {
			return;
		} 
		else if(message == null || message.isEmpty()) {
			return;
		}
		
		JOptionPane.showMessageDialog(null, message, title,
				JOptionPane.INFORMATION_MESSAGE, new ImageIcon(WindowUtils.DEFAULT_FRAME_ICON));
	}

	public static int displayConfirmDialog(String title, final String message) {
		if(title == null || title.isEmpty()) {
			return -1;
		} 
		else if(message == null || message.isEmpty()) {
			return -1;
		}
		
		int dialogResult = JOptionPane.showConfirmDialog(null, message, title,
				JOptionPane.YES_NO_OPTION);
				
		return dialogResult; 
	}
	
	public static String displayInputDialog(String dialogMessage, String title) {
		if(title == null || title.isEmpty()) {
			return "";
		} 
		else if(dialogMessage == null || dialogMessage.isEmpty()) {
			return "";
		}
		
		String input = JOptionPane.showInputDialog(null, dialogMessage, title,
				JOptionPane.QUESTION_MESSAGE);
		
		if (input == null) {
			input = "";
		}

		return input;
	}

	public static synchronized void requestGarbageCollecton() {
		Object object = new Object();
		WeakReference<Object> garbageReference = new WeakReference<Object>(object); 
		object = null;
		
		while(garbageReference.get() != null) {
			System.gc(); 
			System.runFinalization(); 
		}
	}
	
	public static synchronized boolean isBusyCursor(JComponent component) {
		if(component.getCursor().getType() == Cursor.WAIT_CURSOR) {
			return true; 
		} else {
			return false; 
		}
	}
	
	public static synchronized void setBusyCursor(JComponent component) {
		if(component == null) {			
			return;
		}
		
		component.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	
	public static synchronized void setDefaultCursor(JComponent component) {
		if(component == null) {			
			return;
		}
		
		component.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	
	public synchronized static long getUnixTimestamp() {
		 long unixTimestamp = Instant.now().getEpochSecond();
		 
		 return unixTimestamp;
	}
	
	public static Date getFileCreationDate(File file) {
		Date creationDate = null;
		Path filePath = file.toPath();

		BasicFileAttributes attributes = null;
		
		try {
			attributes = Files.readAttributes(filePath, BasicFileAttributes.class);
		} catch (IOException ioException) {

		}
		
		long milliseconds = attributes.creationTime().to(TimeUnit.MILLISECONDS);
		
		if ((milliseconds > Long.MIN_VALUE) && (milliseconds < Long.MAX_VALUE)) {
			creationDate = new Date(attributes.creationTime().to(TimeUnit.MILLISECONDS));
		}

		return creationDate;
	}
}

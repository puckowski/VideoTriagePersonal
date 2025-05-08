package com.keypointforensics.videotriage.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import com.keypointforensics.videotriage.gui.main.RequestFocusListener;

public class Utils {

	/*
	 * Author: Daniel Puckowski
	 */
	
	public static final String SOFTWARE_NAME           = "Video Triage";
	public static final String SOFTWARE_VERSION        = "0.8.3.2";
	public static final String SOFTWARE_VERSION_NUMBER = "5727";
	public static final String SOFTWARE_DATE_STAMP     = "2025-05-06";
	public static final String AUTHOR_NAME             = "Daniel Puckowski";
	
	public static final String DEFAULT_DATE_FORMAT           = "MM-dd-yyyy HH-mm-ss";
	public static final String REPORT_DATE_FORMAT            = "MM/dd/yyyy";
	public static final String EXPORT_DATE_FORMAT            = "MM-dd-yyyy_HH-mm";
	public static final String LAST_MODIFIED_DATE_FORMAT     = "MM/dd/yyyy HH:mm:ss";
	public static final String REPORT_DATE_FORMAT_DASH_LONG  = "MM-dd-yyyy HH:mm:ss";
	public static final String REPORT_DATE_FORMAT_DASH_SHORT = "MM-dd-yyyy";
	public static final String MM_DD_YYYY_DATE_FORMAT        = "MM-dd-yyyy";
	
	private static final SimpleDateFormat FULL_SIMPLE_DATE_FORMAT   = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
	private static final SimpleDateFormat SHORT_SIMPLE_DATE_FORMAT  = new SimpleDateFormat(MM_DD_YYYY_DATE_FORMAT);
	private static final SimpleDateFormat REPORT_SIMPLE_DATE_FORMAT = new SimpleDateFormat(REPORT_DATE_FORMAT);
	private static final SimpleDateFormat EXPORT_SIMPLE_DATE_FORMAT = new SimpleDateFormat(EXPORT_DATE_FORMAT);
	
	public static final int DIALOG_CONFIRM = 0;
	public static final int DIALOG_ERROR   = -1;
	
	public static String getTimeStampForReport() {
		Calendar calendar = Calendar.getInstance();

		return REPORT_SIMPLE_DATE_FORMAT.format(calendar.getTime());
	}
	
	public static String getTimeStampForExport() {
		Calendar calendar = Calendar.getInstance();

		return EXPORT_SIMPLE_DATE_FORMAT.format(calendar.getTime());
	}
	
	public static String getTimeStamp() {
		Calendar calendar = Calendar.getInstance();

		return FULL_SIMPLE_DATE_FORMAT.format(calendar.getTime());
	}

	public static String getMonthDayYearTimeStamp() {
		Calendar calendar = Calendar.getInstance();

		return SHORT_SIMPLE_DATE_FORMAT.format(calendar.getTime());
	}
	
	public static void displayMessageDialog(String title, final String message) {
		JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
	}

	public static int displayConfirmDialog(final String title, final String message) {
		final int dialogResult = JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION);
		
		return dialogResult; 
	}
	
	public static String displayInputDialog(final String dialogMessage, final String title) {
		final String input = JOptionPane.showInputDialog(null, dialogMessage, title, JOptionPane.QUESTION_MESSAGE);

		return input;
	}
	
	public static String displayPasswordDialog(final String title) {	
		final JPasswordField passwordField = new JPasswordField();

		passwordField.addAncestorListener(new RequestFocusListener());
		String input = null; 
		
		final int buttonSelection = JOptionPane.showConfirmDialog(null, passwordField, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

		if(buttonSelection == JOptionPane.OK_OPTION) {
			input = new String(passwordField.getPassword());
		} 
		
		return input;
	}
}

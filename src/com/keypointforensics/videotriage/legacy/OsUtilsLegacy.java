package com.keypointforensics.videotriage.legacy;


import java.util.Locale;

public class OsUtilsLegacy {

	/*
	 * Author: Daniel Puckowski
	 */

	public enum FrsOsType {
		Windows, MacOS, Linux, Other
	};

	protected static FrsOsType mDetectedOs;

	public static FrsOsType getOperatingSystemType() {
		if (mDetectedOs == null) {
			String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
			
			if ((os.indexOf("mac") >= 0) || (os.indexOf("darwin") >= 0)) {
				mDetectedOs = FrsOsType.MacOS;
			} else if (os.indexOf("win") >= 0) {
				mDetectedOs = FrsOsType.Windows;
			} else if (os.indexOf("nux") >= 0) {
				mDetectedOs = FrsOsType.Linux;
			} else {
				mDetectedOs = FrsOsType.Other;
			}
		}
		return mDetectedOs;
	}
}
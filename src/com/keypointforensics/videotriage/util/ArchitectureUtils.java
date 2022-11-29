package com.keypointforensics.videotriage.util;

public class ArchitectureUtils {

	public static boolean is64Bit() {
		if(System.getProperty("os.name").contains("Windows")) {
			return (System.getenv("ProgramFiles(x86)") != null);
		}
		else {
			return (System.getProperty("os.arch").indexOf("64") != -1); 
		}
	}
	
	public static boolean is32Bit() {
		if(System.getProperty("os.name").contains("Windows")) {
			return (System.getenv("ProgramFiles(x86)") == null);
		}
		else {
			return (System.getProperty("os.arch").indexOf("64") == -1); 
		}
	}
	
}

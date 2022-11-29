package com.keypointforensics.videotriage.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class IoUtils {

	/*
	 * Author: Daniel Puckowski
	 */
	
	public static void redirectStandardErrorStream() {
		System.setErr(new PrintStream(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
			
			}
		}));
	}
}

package com.keypointforensics.videotriage.main;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.keypointforensics.videotriage.obfuscated.main.Main;

public class ApplicationMain {

	/*
	 * Author: Daniel Puckowski
	 */
	
	public static void main(String[] args) throws UnsupportedEncodingException, IOException {
		Main m = new Main();
		m.run();
	}
}

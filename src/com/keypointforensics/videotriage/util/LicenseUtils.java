package com.keypointforensics.videotriage.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LicenseUtils {

	public static final String LICENSE_KEY_FILENAME = FileUtils.LICENSE_DIRECTORY + "key";

	public static boolean needsVerification() {
		return !(new File(LICENSE_KEY_FILENAME).exists());
	}
	
	public static void writeVerificationFile(final String licenseString) throws FileNotFoundException {
		PrintWriter licenseWriter = new PrintWriter(LICENSE_KEY_FILENAME);
		licenseWriter.println(licenseString);
		licenseWriter.close();
	}
	
	public static String getLicenseKey() throws IOException {
		return new String(Files.readAllBytes(Paths.get(LICENSE_KEY_FILENAME)));
	}
}

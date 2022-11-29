package com.keypointforensics.videotriage.filter.errorlevel;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class FileErrorLevelAnalysisRunnable implements Runnable {

	private final File IMAGE_FILE;
	private final String FILE_NAME;
	private final float COMP_PCT_DEF;
	private final int DIFF_THRESH_DEF;
	private final int[] PIXEL;

	FileErrorLevelAnalysisRunnable(String filename, File file, int[] pix, float compression, int thresh) {
		FILE_NAME = filename;
		PIXEL = pix;
		IMAGE_FILE = file;
		COMP_PCT_DEF = compression;
		DIFF_THRESH_DEF = thresh;
	}

	@Override
	public void run() {
		System.out.format("%nExamining File %s...%n", FILE_NAME);

		/*
		 * try { //Read image and create compressed version BufferedImage imgInput =
		 * ImageIO.read(IMAGE_FILE); BufferedImage imgCompressed =
		 * ErrorLevelAnalysisFilter.GetCompressedImage(imgInput, FILE_NAME,
		 * COMP_PCT_DEF);
		 * 
		 * //Get difference image and save it BufferedImage imgDifference =
		 * ErrorLevelAnalysisFilter.GetDifferenceImage(imgInput, imgCompressed);
		 * ImageIO.write(imgDifference, "jpg", new File(FILE_NAME + "_difference.jpg"));
		 * 
		 * //Mask original image with difference image and save it BufferedImage
		 * imgMasked = ImageUtils.MaskImages(imgInput, imgDifference, PIXEL,
		 * DIFF_THRESH_DEF); ImageIO.write(imgMasked, "jpg", new File(FILE_NAME +
		 * "_masked.jpg")); } catch(IOException ex) { System.out.
		 * format("RunELA: Error Running Error Level Analysis on file %s: %s...%n",
		 * FILE_NAME, ex.getMessage()); }
		 */
	}

}

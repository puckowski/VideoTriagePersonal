package com.keypointforensics.videotriage.filter.errorlevel;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.Utils;

public class ErrorLevelAnalysisFilterRunner {

	// Default settings
	private static enum Mode {
		FILE, FOLDER, ERR
	}; // Strings for different modes

	private static final float COMP_PCT_DEF = 0.95f; // Default JPG recompression percentage
	private static final int DIFF_THRESH_DEF = 25; // Default threshold for error level difference
	private static final int[] MASK_RGB = Pixel.MAGENTA.RGB(); // Default mask pixel color

	/*
	 * public static void main(String[] args) { //Check whether user wants single
	 * image file or all image files in directory File inputFile = new
	 * File(args[0]); boolean exists = inputFile.exists(); boolean isFile =
	 * inputFile.isFile(); boolean isFolder = inputFile.isDirectory(); Mode mode =
	 * (exists && isFile) ? Mode.FILE : (exists && isFolder) ? Mode.FOLDER :
	 * Mode.ERR; String filename = null;
	 * 
	 * switch (mode) { //Run ELA on a single image file... case FILE: filename =
	 * getFileName(args[0]); System.out.format("%nExamining File %s...%n",
	 * filename);
	 * 
	 * runELA(inputFile, filename, MASK_RGB);
	 * System.out.format("%nFinished...%n%n"); break; //Run ELA on all images in
	 * designated folder.... case FOLDER: //Make a list only of jpg, png files for
	 * now... List<Path> imageFiles = new ArrayList();
	 * 
	 * try (DirectoryStream<Path> stream =
	 * Files.newDirectoryStream(Paths.get(args[0]), "*.{jpg,jpeg,png}")) { for (Path
	 * filePath : stream) { //System.out.format("Adding File %s...%n",
	 * filePath.toString()); imageFiles.add(filePath); } } catch(IOException ex) {
	 * System.out.format("%nError Creating File List: %s...%n", ex.getMessage()); }
	 * 
	 * //TODO: Set up executorservice to //long startTime = System.nanoTime();
	 * 
	 * for (Path filePath : imageFiles) { //Old method, single threaded /* filename
	 * = getFileName(filePath.toString());
	 * 
	 * System.out.format("%nExamining File %s...", filePath.toString());
	 * runELA(filePath.toFile(), filename, MASK_RGB);
	 *
	 * 
	 * //New method: start new runnable for each file filename =
	 * getFileName(filePath.toString()); Runnable eval = new
	 * FileELARunnable(filename, filePath.toFile(), MASK_RGB, COMP_PCT_DEF,
	 * DIFF_THRESH_DEF); Thread thread = new Thread(eval); thread.start(); }
	 * 
	 * //long duration = System.nanoTime() - startTime;
	 * 
	 * //System.out.format("%n%nFinished directory in %d ns (%d ms)...%n%n",
	 * duration, duration / 1000000);
	 * System.out.format("%n%nFinished directory...%n%n"); break; } }
	 */

	/**
	 * Send this class an image File, a string for desired output file name, and an
	 * int[] RGB value to run ELA on that File, masking the difference with the
	 * given RGB value and saving the file to the file name.
	 * 
	 * @param inputFile
	 *            Image file input
	 * @param filename
	 *            Desired name for the output file.
	 * @param maskValue
	 *            RGB value to use when masking aberrant pixels
	 */
	public BufferedImage runELA(File inputFile, String filename, int[] maskValue) {
		String filenameWithoutExtension = FileUtils.getShortFilename(filename);

		if (filenameWithoutExtension.contains(".") == true) {
			filenameWithoutExtension = filenameWithoutExtension.substring(0, filenameWithoutExtension.lastIndexOf("."));
		}

		try {
			final String compressedFilename = FileUtils.TEMPORARY_DIRECTORY + filenameWithoutExtension + "_"
					+ Utils.getTimeStamp() + "_ela_compressed.jpg";

			// Read image and create compressed version
			BufferedImage imgInput = ImageIO.read(inputFile);
			BufferedImage imgCompressed = ErrorLevelAnalysisFilter.GetCompressedImage(imgInput, filename,
					compressedFilename, COMP_PCT_DEF);

			// Get difference image and save it
			BufferedImage imgDifference = ErrorLevelAnalysisFilter.GetDifferenceImage(imgInput, imgCompressed);
			// ImageIO.write(imgDifference, "jpg", new File(filename + "_difference.jpg"));

			// Mask original image with difference image and save it
			BufferedImage imgMasked = ImageUtils.MaskImages(imgInput, imgDifference, maskValue, DIFF_THRESH_DEF);

			//
			String tmpName = FileUtils.TEMPORARY_DIRECTORY + filenameWithoutExtension + "_" + Utils.getTimeStamp()
					+ "_ela_masked.jpg";
			ImageIO.write(imgMasked, "jpg", new File(tmpName));

			BufferedImage errorLevelAnalysisResult = com.keypointforensics.videotriage.util.ImageUtils
					.loadBufferedImage(tmpName);

			FileUtils.deleteFile(new File(tmpName));
			FileUtils.deleteFile(new File(compressedFilename));

			return errorLevelAnalysisResult;
		} catch (IOException ex) {
			System.out.format("RunELA: Error Running Error Level Analysis on file %s: %s...%n", filename,
					ex.getMessage());
		}

		return null;
	}

	/**
	 * Send this method a string of the input file name to get a modified string for
	 * the output file name.
	 * 
	 * @param name
	 *            String, file name of input file.
	 * @return String, file name for output file.
	 */
	private static String getFileName(String name) {
		// TODO: Make this more advanced later, not robust enough
		int length = name.length();

		if (name.charAt(length - 4) == '.') {
			name = name.substring(0, length - 4);
		} else if (name.charAt(length - 5) == '.') { // For .jpeg
			name = name.substring(0, length - 5);
		}

		return name;
	}

}

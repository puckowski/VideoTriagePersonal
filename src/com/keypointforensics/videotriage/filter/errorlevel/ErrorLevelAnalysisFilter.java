package com.keypointforensics.videotriage.filter.errorlevel;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.Utils;

public class ErrorLevelAnalysisFilter {

	/**
	 * Send this method a BufferedImage which needs to be compressed to an arbitrary
	 * JPEG level.
	 * 
	 * @param image
	 *            Source image to compress
	 * @param compressionLevel
	 *            JPEG compression level, generally ~0.95
	 * @return BufferedImage Compressed version of source image
	 */
	// TODO:Add exceptions for bad input
	public static BufferedImage GetCompressedImage(BufferedImage image, String fname, String outputFilename,
			float compressionLevel) {
		BufferedImage compressed = null;

		try {
			// Easiest to write to file at first, find a way to feed into stream...
			String tmpName = outputFilename;// FileUtils.TEMPORARY_DIRECTORY + filenameWithoutExtension + "_" +
											// Utils.getTimeStamp() + "_ela_compressed.jpg";
			File writeToFile = new File(tmpName);

			// Set JPEG compression settings
			ImageOutputStream imgStream = ImageIO.createImageOutputStream(writeToFile);
			ImageWriter imgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
			imgWriter.setOutput(imgStream);

			JPEGImageWriteParam jpgParams = new JPEGImageWriteParam(null);
			jpgParams.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
			jpgParams.setCompressionQuality(compressionLevel);

			// Write re-compressed jpg to file.
			imgWriter.write(null, new IIOImage(image, null, null), jpgParams);
			imgWriter.dispose();

			// Read re-compressed jpg to stream
			compressed = ImageIO.read(new File(tmpName));
			// Path path = Paths.get(tmpName);

			// Delete re-compressed jpg, not needed
			// try {
			// Files.delete(path);
			// } catch (IOException ex) {
			// System.out.format("GetCompressedImage: Error deleting temporary file:
			// %s...%n", ex.getMessage());
			// }
		} catch (IOException ex) {
			System.out.format("GetCompressedImage: Error creating compressed image: %s...%n", ex.getMessage());
		}

		return compressed;
	}

	/**
	 * Creates a difference image from the original image and the slightly
	 * re-compressed image.
	 * 
	 * @param image
	 *            The uncompressed original image
	 * @param compressed
	 *            The compressed version of the original
	 * @return BufferedImage Difference image: each pixel's RGB is the difference
	 *         between the original & compressed RGB values.
	 */
	public static BufferedImage GetDifferenceImage(BufferedImage image, BufferedImage compressed) {
		BufferedImage difference = null;
		int height = image.getHeight();
		int width = image.getWidth();

		if (height == compressed.getHeight() && width == compressed.getWidth()) {
			int[][][] original = ImageUtils.RGBArray(image);
			int[][][] comp = ImageUtils.RGBArray(compressed);
			int[][][] diff = new int[height][width][3];
			int[] chanMaxDiff = new int[3]; // Keep track of largest difference in each band

			for (int r = 0; r < height; r++) {
				for (int c = 0; c < width; c++) {
					for (int band = 0; band < 3; band++) {
						int d = Math.abs(original[r][c][band] - comp[r][c][band]);

						diff[r][c][band] = d;
						chanMaxDiff[band] = (d > chanMaxDiff[band]) ? d : chanMaxDiff[band];
					}
				}
			}

			// Pick largest difference of all bands, so that no value is scaled over 255
			int maxDiff = 0;

			for (int i = 0; i < 3; i++) {
				maxDiff = (chanMaxDiff[i] > maxDiff) ? chanMaxDiff[i] : maxDiff;
			}

			// Rescale all pixel values so that the max value now = 255.
			double scale = 255.0 / maxDiff;

			for (int r = 0; r < height; r++) {
				for (int c = 0; c < width; c++) {
					for (int band = 0; band < 3; band++) {
						diff[r][c][band] *= scale;
						diff[r][c][band] = (diff[r][c][band] > 255) ? 255
								: (diff[r][c][band] < 0) ? 0 : diff[r][c][band];
					}
				}
			}

			difference = ImageUtils.RGBImg(diff);
		}

		return difference;
	}

}

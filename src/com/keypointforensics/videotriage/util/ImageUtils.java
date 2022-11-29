package com.keypointforensics.videotriage.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;

import com.keypointforensics.videotriage.progress.ProgressBundle;

public class ImageUtils {
	
	/*
	 * Author: Daniel Puckowski
	 */
		
	public static final BufferedImage NO_PREVIEW_AVAILABLE_IMAGE     = ImageUtils.loadBufferedImage(FileUtils.GRAPHICS_DIRECTORY + "no_preview_available.jpg");
	public static final int           DEFAULT_MONTAGE_WIDTH_PIXELS   = 1920;
	public static final int           DEFAULT_MONTAGE_HEIGHT_PIXELS  = 1080;
	public static final int           DEFAULT_MONTAGE_PADDING_PIXELS = 40;
	
	public static final String NO_PREVIEW_AVAILABLE_IMAGE_PATH = FileUtils.GRAPHICS_DIRECTORY + "no_preview_available.jpg";
	private static final String DEFAULT_IMAGE_WRITER_TYPE      = "jpeg";
	private static final String DEFAULT_IMAGE_EXTENSION        = "jpg";
	
	public static String getImageBase64String(final RenderedImage imageToBase64, final String formatName) {
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		try {
			ImageIO.write(imageToBase64, formatName, byteArrayOutputStream);
			
			return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
		} catch (final IOException ioException) {
			//ioException.printStackTrace();
		}
		
		return null;
	}
	
	public static void getMontage(final String absoluteFilePath, final ArrayList<String> imagesToMontage, final int rows, final int columns) {
		BufferedImage bi = getMontage(imagesToMontage, rows, columns);
		
		File checkFile = new File(absoluteFilePath);
		if(checkFile.exists() == true) {
			checkFile.delete();
		}
		
		ImageUtils.saveBufferedImage(bi, absoluteFilePath);
	}
	
	public static void getMontageUnmonitored(final String absoluteFilePath, final ArrayList<String> imagesToMontage, final int rows, final int columns) {
		BufferedImage bi = getMontageUnmonitored(imagesToMontage, rows, columns);
		
		File checkFile = new File(absoluteFilePath);
		if(checkFile.exists() == true) {
			checkFile.delete();
		}
		
		ImageUtils.saveBufferedImage(bi, absoluteFilePath);
	}
	
	public static BufferedImage getMontageFromTemporaryDirectory(final ArrayList<String> imagesToMontage, final int rows, final int columns) {
		for(int i = 0; i < imagesToMontage.size(); ++i) {
			imagesToMontage.set(i, FileUtils.TEMPORARY_DIRECTORY + imagesToMontage.get(i));
		}
		
		return getMontage(imagesToMontage, rows, columns);
	}
	
	public static BufferedImage getMontage(final ArrayList<String> imagesToMontage, final int rows, final int columns) {
		return getMontage(imagesToMontage, rows, columns, DEFAULT_MONTAGE_WIDTH_PIXELS, DEFAULT_MONTAGE_HEIGHT_PIXELS, DEFAULT_MONTAGE_PADDING_PIXELS);
	}
	
	public static BufferedImage getMontageUnmonitored(final ArrayList<String> imagesToMontage, final int rows, final int columns) {
		return getMontageUnmonitored(imagesToMontage, rows, columns, DEFAULT_MONTAGE_WIDTH_PIXELS, DEFAULT_MONTAGE_HEIGHT_PIXELS, DEFAULT_MONTAGE_PADDING_PIXELS);
	}
	
	public static BufferedImage getMontageUnmonitored(final ArrayList<String> imagesToMontage, final int rows, final int columns, final int outputWidth, final int outputHeight, final int padding) {	
		BufferedImage montage = new BufferedImage(outputWidth, outputHeight, BufferedImage.TYPE_INT_RGB);
		Graphics graphics = montage.getGraphics();
		
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, outputWidth, outputHeight);

		final int numberOfGapColumns = columns + 1;
		final int numberOfGapRows = rows + 1;
		
		final int totalColumnGap = numberOfGapColumns * padding;
		final int totalRowGap = numberOfGapRows * padding;
		
		final int imageWidthRemainder = outputWidth - totalColumnGap;
		final int imageHeightRemainder = outputHeight - totalRowGap;
		
		final int imageWidth = (imageWidthRemainder / columns);
		final int imageHeight = imageHeightRemainder / rows;
		
		int paddingX = 0, paddingY = 0, subimageIndex = 0;
				
		for(int r = 0; r < rows; r++) {
			paddingY += padding;
			
			for(int c = 0; c < columns; c++) {
				paddingX += padding;
				
				graphics.drawImage(ImageUtils.getScaledImage(ImageUtils.loadBufferedImage(imagesToMontage.get(subimageIndex)), imageWidth, imageHeight),
					paddingX, paddingY, null);
				
				subimageIndex++;
					
				paddingX += imageWidth;
			}
			
			paddingY += imageHeight;
			paddingX = 0;
		}
				
		return montage;
	}
	
	public static BufferedImage getMontage(final ArrayList<String> imagesToMontage, final int rows, final int columns, final int outputWidth, final int outputHeight, final int padding) {	
		BufferedImage montage = new BufferedImage(outputWidth, outputHeight, BufferedImage.TYPE_INT_RGB);
		Graphics graphics = montage.getGraphics();
		
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, outputWidth, outputHeight);

		final int numberOfGapColumns = columns + 1;
		final int numberOfGapRows = rows + 1;
		
		final int totalColumnGap = numberOfGapColumns * padding;
		final int totalRowGap = numberOfGapRows * padding;
		
		final int imageWidthRemainder = outputWidth - totalColumnGap;
		final int imageHeightRemainder = outputHeight - totalRowGap;
		
		final int imageWidth = (imageWidthRemainder / columns);
		final int imageHeight = imageHeightRemainder / rows;
		
		int paddingX = 0, paddingY = 0, subimageIndex = 0;
		
		ProgressBundle montageProgressBundle = ProgressUtils.getProgressBundle("Creating Montage...", rows * columns);
		
		for(int r = 0; r < rows; r++) {
			paddingY += padding;
			
			for(int c = 0; c < columns; c++) {
				paddingX += padding;
				
				graphics.drawImage(ImageUtils.getScaledImage(ImageUtils.loadBufferedImage(imagesToMontage.get(subimageIndex)), imageWidth, imageHeight),
					paddingX, paddingY, null);
				
				subimageIndex++;
				
				montageProgressBundle.progressBar.setValue(subimageIndex);
				montageProgressBundle.progressBar.repaint();
				
				paddingX += imageWidth;
			}
			
			paddingY += imageHeight;
			paddingX = 0;
		}
		
		montageProgressBundle.frame.dispose();
		
		return montage;
	}
	
	public static int getMinimumDimension(final BufferedImage image) {
		return Math.min(image.getWidth(), image.getHeight());
	}
	
	public static boolean saveBufferedImage(final BufferedImage image, final String filename, final float jpegQuality) {		
		boolean success = false;

		final Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix(DEFAULT_IMAGE_WRITER_TYPE);
		
		final ImageWriter writer = (ImageWriter) writers.next();
        final ImageWriteParam imageWriteParam = writer.getDefaultWriteParam();
        imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        final IIOImage iioImage = new IIOImage(image, null, (IIOMetadata) null);
        
	    try {
			write(writer, imageWriteParam, iioImage, filename, jpegQuality);
			
			success = true;
		} catch (IOException ioException) {

		}
	    
	    return success;
	}
	
	private static void write(final ImageWriter writer, final ImageWriteParam imageWriteParam, final IIOImage iioImage, final String filename,
			final float compressionQuality) throws IOException {	
		final ImageOutputStream out = ImageIO.createImageOutputStream(new File(filename));
		imageWriteParam.setCompressionQuality(compressionQuality);
		
		writer.setOutput(out);
		writer.write((IIOMetadata) null, iioImage, imageWriteParam);
		
		out.flush();
		out.close();
	}
	
	public static boolean saveBufferedImage(final BufferedImage image, final String filename) {
		final File outputFile = new File(filename);
		
		boolean success = false;
		
		try {
			ImageIO.write(image, DEFAULT_IMAGE_EXTENSION, outputFile);
						
			success = true;
		} catch (IOException ioException) {

		}
		
		return success;
	}
	
	public static BufferedImage copyBufferedImage(final BufferedImage source) {
		final BufferedImage bufferedImage = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
		
		byte[] src = ((DataBufferByte) source.getRaster().getDataBuffer()).getData();
		byte[] dst = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
		
		System.arraycopy(src, 0, dst, 0, dst.length);
		
		return bufferedImage;
	}
	
	public static BufferedImage cropImage(final BufferedImage src, final int startX, final int startY, final int width, final int height) {
		final BufferedImage dest = src.getSubimage(startX, startY, width, height); //Crop the image with the given width and height parameters
		
		return dest;
	}

	public static BufferedImage getScaledImage(final BufferedImage srcImg, final int width, final int height) {
		final BufferedImage resizedImg = new BufferedImage(width, height, BufferedImage.TRANSLUCENT);
		final Graphics2D g2 = resizedImg.createGraphics();
	
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(srcImg, 0, 0, width, height, null);
		g2.dispose();
		
		return resizedImg;
	}
	
	public static BufferedImage getScaledImageWithAspectRatio(final BufferedImage srcImg, final int width, final int height) {
		final BufferedImage resizedImg = new BufferedImage(width, height, BufferedImage.TRANSLUCENT);
		final Graphics2D g2 = resizedImg.createGraphics();
	
		Dimension scaledDimension = DimenUtils.getScaledDimension(new Dimension(srcImg.getWidth(), srcImg.getHeight()), new Dimension(width, height));
		
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(srcImg, 0, 0, (int) scaledDimension.getWidth(), (int) scaledDimension.getHeight(), null);
		g2.dispose();
		
		return resizedImg;
	}
	
	public static BufferedImage getScaledImageSameChannel(final BufferedImage srcImg, final int width, final int height) {
		final Image tmp = srcImg.getScaledInstance(width, height, BufferedImage.SCALE_FAST);
		final BufferedImage buffered = new BufferedImage(width, height, srcImg.getType());
		
		buffered.getGraphics().drawImage(tmp, 0, 0, null);

		return buffered;
	}
	
	public static boolean isImageFile(final String absoluteFilePath) { 
		try {
			Image image = ImageIO.read(new File(absoluteFilePath));
			
			if (image == null) {				
				return false;
			}
			
			image.flush();
			image = null;
		} catch (IOException ioException) {			
			return false;
		}
		
		return true;
	}

	public static boolean isImageFile(final File file) {
		try {
			Image image = ImageIO.read(file);
			
			if (image == null) {				
				return false;
			}
			
			image.flush();
			image = null;
		} catch (IOException ioException) {			
			return false;
		}
		
		return true;
	}
	
	public static BufferedImage loadBufferedImage(final String absoluteFilePath) { 
		BufferedImage image = null;

		try {
			image = ImageIO.read(new File(absoluteFilePath));
		} catch (IOException ioException) {

		}
		
		return image;
	}
	
	public static BufferedImage loadBufferedImage(final File imageFile) { 
		BufferedImage image = null;

		try {
			image = ImageIO.read(imageFile);
		} catch (IOException ioException) {

		}
		
		return image;
	}
	
	public static double getShannonEntropy(BufferedImage actualImage) {
		HashMap<Integer, Integer> occ = new HashMap<Integer, Integer>();
		double e = 0.0, p;
		int pixel, red, green, blue, d, n = 0;//, cx;
		//int n = 0;
		
		for (int i = 0; i < actualImage.getHeight(); i++) {
			for (int j = 0; j < actualImage.getWidth(); j++) {
				pixel = actualImage.getRGB(j, i);
				red = (pixel >> 16) & 0xff;
				green = (pixel >> 8) & 0xff;
				blue = (pixel) & 0xff;
				
				d = (int) Math.round(0.2989 * red + 0.5870 * green + 0.1140 * blue);
				
				if (occ.containsKey(d)) {
					occ.put(d, occ.get(d) + 1);
				} else {
					occ.put(d, 1);
				}
				
				++n;
			}
		}
			
		for (Map.Entry<Integer, Integer> entry : occ.entrySet()) {
			//cx = entry.getKey();
			p = (double) entry.getValue() / n;
			e += p * Math.log(p) / Math.log(2); //log2(p);
		}
		
		return -e;
	}
}

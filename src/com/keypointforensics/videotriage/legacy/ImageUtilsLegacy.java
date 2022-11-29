package com.keypointforensics.videotriage.legacy;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.keypointforensics.videotriage.image.ssim.SsimCalculator;
import com.keypointforensics.videotriage.image.ssim.SsimException;

public class ImageUtilsLegacy {
	
	/*
	 * Author: Daniel Puckowski
	 */
	
	public static final double DEFAULT_IMAGE_DIFFERENCE_NEAR_MATCH_PERCENTAGE = 5.0;
	public static final double DEFAULT_IMAGE_DIFFERENCE_VERY_SIMILAR_PERCENTAGE = 10.0;
	public static final double DEFAULT_IMAGE_DIFFERENCE_SIMILAR_PERCENTAGE = 18;//15.0;

	public static final double DEFAULT_IMAGE_SSIM_NEAR_MATCH_PERCENTAGE = 0.80;
	public static final double DEFAULT_IMAGE_SSIM_VERY_SIMILAR_PERCENTAGE = 0.60; 
	public static final double DEFAULT_IMAGE_SSIM_SIMILAR_PERCENTAGE = 0.29;//0.40;
	
	public static BufferedImage cropImage(final BufferedImage src,
			final int startX, final int startY, final int width,
			final int height) {
		BufferedImage dest = src.getSubimage(startX, startY, width, height); 

		return dest;
	}

	public static BufferedImage getScaledImage(final Image srcImg,
			final int width, final int height) {
		BufferedImage resizedImg = new BufferedImage(width, height,
				BufferedImage.TRANSLUCENT);
		Graphics2D g2 = resizedImg.createGraphics();

		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(srcImg, 0, 0, width, height, null); 
		g2.dispose();

		return resizedImg;
	}

	public static double[] getImageData(final BufferedImage bufferedImage) {
		double[] inputFace = null;

		if (bufferedImage != null) {
			int imageWidth = bufferedImage.getWidth();
			int imageHeight = bufferedImage.getHeight();

			inputFace = new double[imageWidth * imageHeight];
			bufferedImage.getData().getPixels(0, 0, imageWidth, imageHeight,
					inputFace); 
		}

		return inputFace;
	}

	public static double[] getImageData(final String imageFileName)
			throws Exception {
		BufferedImage bufferedImage = null;
		double[] inputFace = null;

		try {
			bufferedImage = ImageIO.read(new File(imageFileName));
		} catch (final IOException ioException) {			
			throw new Exception(ioException.getMessage());
		}

		if (bufferedImage != null) {
			int imageWidth = bufferedImage.getWidth();
			int imageHeight = bufferedImage.getHeight();

			inputFace = new double[imageWidth * imageHeight];
			bufferedImage.getData().getPixels(0, 0, imageWidth, imageHeight,
					inputFace);
			
			bufferedImage.flush();
			bufferedImage = null;
		}

		return inputFace;
	}

	public synchronized static boolean isImageFile(final String absoluteFilePath) { 
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

	public synchronized static boolean isImageFile(File file) { 
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

	public synchronized static Image loadImage(String absoluteFilePath) {
		Image image = null;

		try {
			image = ImageIO.read(new File(absoluteFilePath));
		} catch (IOException ioException) {
		
		}

		return image;
	}
	
	public synchronized static BufferedImage loadBufferedImage(String absoluteFilePath) { 
		BufferedImage image = null;

		try {
			image = ImageIO.read(new File(absoluteFilePath));
		} catch (IOException ioException) {

		}

		return image;
	}

	public synchronized static Image loadImage(File imageFile) { 
		Image image = null;

		try {
			image = ImageIO.read(imageFile);
		} catch (IOException ioException) {

		}

		return image;
	}
	
	public synchronized static BufferedImage loadBufferedImage(File imageFile) {
		BufferedImage image = null;

		try {
			image = ImageIO.read(imageFile);
		} catch (IOException ioException) {

		}

		return image;
	}
	
	public static double getImagesSimilarityScore(BufferedImage imageOne,
			BufferedImage imageTwo, int compareSize) {
		if(imageOne == null || imageTwo == null) {			
			return 1.0; 
		}
		
		if(imageOne.getWidth() <= compareSize || imageOne.getHeight() <= compareSize) {
			compareSize = imageOne.getWidth();
		}
		else if(imageTwo.getWidth() <= compareSize || imageTwo.getHeight() <= compareSize) {
			compareSize = imageTwo.getWidth();
		}
		
		//Scale both images to the same size
		imageOne = getScaledImage(imageOne, compareSize, compareSize);
		imageTwo = getScaledImage(imageTwo, compareSize, compareSize);
		
		int width1 = imageOne.getWidth();
		int width2 = imageTwo.getWidth();
		
		int height1 = imageOne.getHeight();
		int height2 = imageTwo.getHeight();
		
		if ((width1 != width2) || (height1 != height2)) {			
			return -1.0; 
		}
		
		double diff = -1.0;
		
		try {
			diff = new SsimCalculator(imageOne).compareTo(imageTwo);
		} catch (SsimException ssimException) {

		} catch (IOException ioException) {

		}
		
		return diff;
	}
	
	public static double getImagesPercentageDifference(BufferedImage imageOne,
			BufferedImage imageTwo, int compareSize) {
		if(imageOne == null || imageTwo == null) {			
			return 100.00;
		}
		
		if(imageOne.getWidth() <= compareSize || imageOne.getHeight() <= compareSize) {
			compareSize = imageOne.getWidth();
		}
		else if(imageTwo.getWidth() <= compareSize || imageTwo.getHeight() <= compareSize) {
			compareSize = imageTwo.getWidth();
		}
		
		imageOne = getScaledImage(imageOne, compareSize, compareSize);
		imageTwo = getScaledImage(imageTwo, compareSize, compareSize);
		
		int width1 = imageOne.getWidth();
		int width2 = imageTwo.getWidth();
		
		int height1 = imageOne.getHeight();
		int height2 = imageTwo.getHeight();
		
		if ((width1 != width2) || (height1 != height2)) {			
			return 100.00; 
		}
		
		long diff = 0;
		
		int rgb1, rgb2;
		int r1, g1, b1;
		int r2, g2, b2;
		
		int[] pixelArrayOne = ((DataBufferInt) imageOne.getRaster().getDataBuffer()).getData();
		int[] pixelArrayTwo = ((DataBufferInt) imageTwo.getRaster().getDataBuffer()).getData();
	
		for(int pos = ((width1 * height1) - 1); pos > 0; --pos) {
			rgb1 = pixelArrayOne[pos];
			rgb2 = pixelArrayTwo[pos];
				
			r1 = (rgb1 >> 16) & 0xff;
			g1 = (rgb1 >> 8) & 0xff;
			b1 = (rgb1) & 0xff;
				
			r2 = (rgb2 >> 16) & 0xff;
			g2 = (rgb2 >> 8) & 0xff;
			b2 = (rgb2) & 0xff;
				
			diff += Math.abs(r1 - r2);
			diff += Math.abs(g1 - g2);
			diff += Math.abs(b1 - b2);
		}
		
		double n = width1 * height1 * 3;
		double p = diff / n / 255.0;
		
		double percentage = (p * 100.0);

		return percentage;
	}
	
	public static double getImagesPercentageDifference(BufferedImage imageOne,
			BufferedImage imageTwo) {
		if(imageOne == null || imageTwo == null) {
			
			return 100.00;
		}
		
		imageOne = getScaledImage(imageOne, 100, 100);
		imageTwo = getScaledImage(imageTwo, 100, 100);
		
		int width1 = imageOne.getWidth();
		int width2 = imageTwo.getWidth();
		
		int height1 = imageOne.getHeight();
		int height2 = imageTwo.getHeight();
		
		if ((width1 != width2) || (height1 != height2)) {			
			return 100.00; 
		}
		
		long diff = 0;
		
		int rgb1, rgb2;
		int r1, g1, b1;
		int r2, g2, b2;
		
		for (int y = 0; y < height1; ++y) {
			for (int x = 0; x < width1; ++x) {
				rgb1 = imageOne.getRGB(x, y);
				rgb2 = imageTwo.getRGB(x, y);
				
				r1 = (rgb1 >> 16) & 0xff;
				g1 = (rgb1 >> 8) & 0xff;
				b1 = (rgb1) & 0xff;
				
				r2 = (rgb2 >> 16) & 0xff;
				g2 = (rgb2 >> 8) & 0xff;
				b2 = (rgb2) & 0xff;
				
				diff += Math.abs(r1 - r2);
				diff += Math.abs(g1 - g2);
				diff += Math.abs(b1 - b2);
			}
		}
		
		double n = width1 * height1 * 3;
		double p = diff / n / 255.0;
		
		double percentage = (p * 100.0);
		
		return percentage;
	}
}

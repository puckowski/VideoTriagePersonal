package com.keypointforensics.videotriage.gui.imagepanel;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.swing.SwingUtilities;

import com.keypointforensics.videotriage.gui.extract.ConvolveWithEdgeOp;
import com.keypointforensics.videotriage.gui.extract.ExtractImageGallery;
import com.keypointforensics.videotriage.lang.Pair;
import com.keypointforensics.videotriage.util.DimenUtils;
import com.keypointforensics.videotriage.util.ImageUtils;
import com.keypointforensics.videotriage.util.KernelUtils;

public class ScalableAnnotationImagePanel extends SimpleImagePanel {

	/*
	 * Author: Daniel Puckowski
	 */

	/**
	 *
	 */
	private static final long serialVersionUID = -7874245682392760248L;

	protected static final int FACE_BLUR_CONVOLUTION_COUNT = 8;

	public static final boolean SCALE_TO_FIT_ENABLED  = true;
	public static final boolean SCALE_TO_FIT_DISABLED = false;

	public static final int SINGLE_CLICK = 1;

	protected final boolean SCALE_TO_FIT;

	private final ExtractImageGallery EXTRACT_IMAGE_GALLERY;

	protected String mImageAbsolutePath;

	private int mOriginalWidth;
	private int mOriginalHeight;

	private BufferedImage mImage;

	private ArrayList<Rectangle> mBlurBoxList;
	private ArrayList<Rectangle> mBlurBoxCaptureRectangleList;
	private ArrayList<Pair<Double, Double>> mBlurBoxRatioList;
	private ArrayList<Pair<Double, Double>> mBlurBoxSizeRatioList;

	private Point mMouseMovedTarget;
	private Point mPointStart;

	private boolean mRemovingRedactions;

	private boolean mFinishedPaint;

	private int mLastBlurBoxCount;

	private Random mRandom;

	public ScalableAnnotationImagePanel(ExtractImageGallery extractImageGallery, final boolean scaleToFit) {
		SCALE_TO_FIT          = scaleToFit;
		EXTRACT_IMAGE_GALLERY = extractImageGallery;

		mBlurBoxList = new ArrayList<Rectangle>();
		mBlurBoxCaptureRectangleList = new ArrayList<Rectangle>();
		mBlurBoxRatioList = new ArrayList<Pair<Double, Double>>();
		mBlurBoxSizeRatioList = new ArrayList<Pair<Double, Double>>();

		mRemovingRedactions = false;

		mLastBlurBoxCount = 0;

		mRandom = new Random();

		addMouseListener();
		addMouseMotionListener();
	}

	public ScalableAnnotationImagePanel(ExtractImageGallery extractImageGallery, final String imageAbsolutePath, final boolean scaleToFit) {
		SCALE_TO_FIT          = scaleToFit;
		EXTRACT_IMAGE_GALLERY = extractImageGallery;

		mImageAbsolutePath = imageAbsolutePath;

		update(ImageUtils.loadBufferedImage(mImageAbsolutePath));

		mBlurBoxList = new ArrayList<Rectangle>();

		mRemovingRedactions = false;

		mLastBlurBoxCount = 0;

		addMouseListener();
		addMouseMotionListener();
	}

	public void setRemovingRedactions(final boolean newRemovingRedactions) {
		mRemovingRedactions = newRemovingRedactions;
	}

	public void addRedactionRectangles(final ArrayList<Rectangle> captureRectangles) {
		for(Rectangle captureRectangle : captureRectangles) {
			addRedactionRectangle(captureRectangle);
		}

		if (captureRectangles.isEmpty()) {
			repaint();
		}
	}

	public void addRedactionRectangle(final Rectangle captureRectangle) {
        final double xRatio = ((double) captureRectangle.x / mOriginalWidth);
		final double yRatio = ((double) captureRectangle.y / mOriginalHeight);

		final double widthRatio = ((double) captureRectangle.width / mOriginalWidth);
		final double heightRatio = ((double) captureRectangle.height / mOriginalHeight);

		mBlurBoxRatioList.add(new Pair<Double, Double>(xRatio, yRatio));
		mBlurBoxSizeRatioList.add(new Pair<Double, Double>(widthRatio, heightRatio));
        mBlurBoxCaptureRectangleList.add(captureRectangle);

        repaint();
	}

	private void removeClickedRedactionAction(final Point clickPoint) {
		Point newPoint = new Point();

		final int panelHeight = ScalableAnnotationImagePanel.this.getHeight();
		final int panelWidth = ScalableAnnotationImagePanel.this.getWidth();
		Dimension resizedImageDimensions = DimenUtils.getScaledDimension(new Dimension(mOriginalWidth, mOriginalHeight), new Dimension(panelWidth, panelHeight));

		final int currentHeight = resizedImageDimensions.height;
		final int currentWidth = resizedImageDimensions.width;

		Rectangle captureRectangle = null;
		Pair<Double, Double> captureRectangleRatios = null;
		Pair<Double, Double> captureRectangleSizeRatios = null;

		for(int i = 0; i < mBlurBoxCaptureRectangleList.size(); ++i) {
			newPoint.x = clickPoint.x;
			newPoint.y = clickPoint.y;

			captureRectangle = new Rectangle(mBlurBoxCaptureRectangleList.get(i));
	        captureRectangleRatios = mBlurBoxRatioList.get(i);
	        captureRectangleSizeRatios = mBlurBoxSizeRatioList.get(i);

			captureRectangle.x = (int) Math.round(((double) mOriginalWidth) * captureRectangleRatios.getKey());
			captureRectangle.y = (int) Math.round(((double) mOriginalHeight) * captureRectangleRatios.getValue());

			if(captureRectangle.x < 0) {
				captureRectangle.x = 0;
			}

			if(captureRectangle.y < 0) {
				captureRectangle.y = 0;
			}

			captureRectangle.width = (int) Math.round(((double) mOriginalWidth) * captureRectangleSizeRatios.getKey());
			captureRectangle.height = (int) Math.round(((double) mOriginalHeight) * captureRectangleSizeRatios.getValue());

			if((captureRectangle.width + captureRectangle.x) > mOriginalWidth) {
				captureRectangle.width = (mOriginalWidth - captureRectangle.x);
			}

			if((captureRectangle.height + captureRectangle.y) > mOriginalHeight) {
				captureRectangle.height = (mOriginalHeight - captureRectangle.y);
			}

			newPoint.x = (int) Math.round(((double) mOriginalWidth) * ((double) clickPoint.x / currentWidth));
			newPoint.y = (int) Math.round(((double) mOriginalHeight) * ((double) clickPoint.y / currentHeight));

			if(captureRectangle.contains(newPoint) == true) {
				mBlurBoxCaptureRectangleList.remove(i);
				mBlurBoxRatioList.remove(i);
				mBlurBoxSizeRatioList.remove(i);

				--i;
			}
		}

		repaint();
	}

	private void calculateBlurBoxesForCurrentDimension() {
		mBlurBoxList.clear();

		Rectangle captureRectangle = null;
		Pair<Double, Double> captureRectangleRatios = null;
		Pair<Double, Double> captureRectangleSizeRatios = null;

		for(int i = 0; i < mBlurBoxCaptureRectangleList.size(); ++i) {
			captureRectangle = new Rectangle(mBlurBoxCaptureRectangleList.get(i));
	        captureRectangleRatios = mBlurBoxRatioList.get(i);
	        captureRectangleSizeRatios = mBlurBoxSizeRatioList.get(i);

			captureRectangle.x = (int) Math.round(((double) mOriginalWidth) * captureRectangleRatios.getKey());
			captureRectangle.y = (int) Math.round(((double) mOriginalHeight) * captureRectangleRatios.getValue());

			if(captureRectangle.x < 0) {
				captureRectangle.x = 0;
			}

			if(captureRectangle.y < 0) {
				captureRectangle.y = 0;
			}

			captureRectangle.width = (int) Math.round(((double) mOriginalWidth) * captureRectangleSizeRatios.getKey());
			captureRectangle.height = (int) Math.round(((double) mOriginalHeight) * captureRectangleSizeRatios.getValue());

			if((captureRectangle.width + captureRectangle.x) > mOriginalWidth) {
				captureRectangle.width = (mOriginalWidth - captureRectangle.x);
			}

			if((captureRectangle.height + captureRectangle.y) > mOriginalHeight) {
				captureRectangle.height = (mOriginalHeight - captureRectangle.y);
			}

			mBlurBoxList.add(captureRectangle);
			//EXTRACT_IMAGE_GALLERY.addModifiedImagePath(mImageAbsolutePath);
		}

		if(mBlurBoxList.size() != mLastBlurBoxCount) {
			EXTRACT_IMAGE_GALLERY.addModifiedImagePath(mImageAbsolutePath, true);
		}

		mLastBlurBoxCount = mBlurBoxList.size();
	}

	private void performRemoveRedactionAction(final Point clickPoint) {
		removeClickedRedactionAction(clickPoint);
		
		/*
		Rectangle currentRectangle;
		
		for(int i = 0; i < mBlurBoxList.size(); ++i) {
			currentRectangle = mBlurBoxList.get(i);
			
			if(currentRectangle.contains(clickPoint) == true) {
				mBlurBoxList.remove(i);
				mBlurBoxCaptureRectangleList.remove(i);
				mBlurBoxRatioList.remove(i);
				mBlurBoxSizeRatioList.remove(i);
				
				i--;
			}
		}
		*/
	}

	private void addMouseListener() {
		this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
            	if(mImageAbsolutePath != null && mRemovingRedactions == false) {
            		mPointStart = mouseEvent.getPoint();
            	} else if(mRemovingRedactions == true) {
            		performRemoveRedactionAction(mouseEvent.getPoint());
            	}
            }

            public void mouseReleased(MouseEvent mouseEvent) {
            	if(mPointStart == null) {
            		return;
            	}
            	else if(mMouseMovedTarget == null) {
            		return;
            	}

                Rectangle captureRectangle = getMouseDraggedRectangle();

    			if(captureRectangle.x < 0) {
    				captureRectangle.width += captureRectangle.x;
    				captureRectangle.x = 0;
    			}

    			if(captureRectangle.y < 0) {
    				captureRectangle.height += captureRectangle.y;
    				captureRectangle.y = 0;
    			}

    			final int panelHeight = ScalableAnnotationImagePanel.this.getHeight();
    			final int panelWidth = ScalableAnnotationImagePanel.this.getWidth();
    			Dimension resizedImageDimensions = DimenUtils.getScaledDimension(new Dimension(mOriginalWidth, mOriginalHeight), new Dimension(panelWidth, panelHeight));

    			final int currentHeight = resizedImageDimensions.height;
    			final int currentWidth = resizedImageDimensions.width;

    	        final double xRatio = ((double) captureRectangle.x / currentWidth);
    			final double yRatio = ((double) captureRectangle.y / currentHeight);

    			final double widthRatio = ((double) captureRectangle.width / currentWidth);
    			final double heightRatio = ((double) captureRectangle.height / currentHeight);

    			mBlurBoxRatioList.add(new Pair<Double, Double>(xRatio, yRatio));
    			mBlurBoxSizeRatioList.add(new Pair<Double, Double>(widthRatio, heightRatio));
                mBlurBoxCaptureRectangleList.add(captureRectangle);

                mPointStart = null;

                repaint();
            }
        });
	}

	public void performUndoAction() {
		if(mBlurBoxCaptureRectangleList.isEmpty() == true) {
			return;
		}

		mBlurBoxCaptureRectangleList.remove(mBlurBoxCaptureRectangleList.size() - 1);
		mBlurBoxRatioList.remove(mBlurBoxRatioList.size() - 1);
		mBlurBoxSizeRatioList.remove(mBlurBoxSizeRatioList.size() - 1);

		repaint();
	}

	private void addMouseMotionListener() {
		this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
            	if(mImageAbsolutePath != null) {
            		mMouseMovedTarget = mouseEvent.getPoint();

                	repaint();
            	}
            }
        });
	}

	public String getAbsolutePath() {
		return mImageAbsolutePath;
	}

	public void clear() {
		mImage = null;

		clearRedactionListData();

		this.repaint();
	}

	public void update(final BufferedImage image, final String imageAbsolutePath) {
		if(image == null) {
			return;
		}

		mImage = image;
		mImageAbsolutePath = imageAbsolutePath;
		mOriginalWidth = mImage.getWidth();
		mOriginalHeight = mImage.getHeight();

		clearRedactionListData();

		this.repaint();
	}

	private void clearRedactionListData() {
		mBlurBoxList.clear();
		mBlurBoxCaptureRectangleList.clear();
		mBlurBoxRatioList.clear();
		mBlurBoxSizeRatioList.clear();
	}

	public BufferedImage getImage() {
		return getImageWithScaledRedactions();
	}

	@Override
	public void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);

		drawGalleryImage(graphics);
	}

	private Rectangle getMouseDraggedRectangle() {
		int minX = Math.min(mPointStart.x, mMouseMovedTarget.x);
		int minY = Math.min(mPointStart.y, mMouseMovedTarget.y);

		int maxX = Math.max(mPointStart.x, mMouseMovedTarget.x);
		int maxY = Math.max(mPointStart.y, mMouseMovedTarget.y);

		return new Rectangle(minX, minY, maxX - minX, maxY - minY);
	}

	private void addNoise(BufferedImage image, int startX, int startY, int width, int height) {
		for (int y = startY; y < startY + height && y < image.getHeight(); y++) {
			for (int x = startX; x < startX + width && x < image.getWidth(); x++) {
				int rgb = image.getRGB(x, y);

				int r = (rgb >> 16) & 0xFF;
				int g = (rgb >> 8) & 0xFF;
				int b = rgb & 0xFF;

				int noiseLevel = 20;
				r = clamp(r + mRandom.nextInt(noiseLevel * 2 + 1) - noiseLevel);
				g = clamp(g + mRandom.nextInt(noiseLevel * 2 + 1) - noiseLevel);
				b = clamp(b + mRandom.nextInt(noiseLevel * 2 + 1) - noiseLevel);

				int noisyRGB = (0xFF << 24) | (r << 16) | (g << 8) | b;
				image.setRGB(x, y, noisyRGB);
			}
		}
	}

	private int clamp(int value) {
		return Math.max(0, Math.min(255, value));
	}

	private BufferedImage getImageWithScaledRedactions() {
		BufferedImage imageCopy = ImageUtils.copyBufferedImage(mImage);

		final float[] blurKernel = KernelUtils.getBlurKernel(KernelUtils.DEFAULT_BLUR_KERNEL_SQUARED_SIZE);

		final int borderX = KernelUtils.DEFAULT_BLUR_KERNEL_SIZE / 2;
        final int borderY = KernelUtils.DEFAULT_BLUR_KERNEL_SIZE / 2;
        imageCopy = ConvolveWithEdgeOp.addBorder(imageCopy, borderX, borderY);

        List<Rectangle> copiedList = new ArrayList<Rectangle>();
        for(Rectangle toCopy : mBlurBoxList) {
        	copiedList.add(toCopy);
        }

		for(Rectangle blurRectangle : copiedList) {
			final Rectangle expandedRectangle = getExpandedRectangle(blurRectangle);

			if(expandedRectangle.width == 0 ||
					expandedRectangle.height == 0) {
				continue;
			}

			for (int convolveIndex = 0; convolveIndex < FACE_BLUR_CONVOLUTION_COUNT; ++convolveIndex) {
				BufferedImage destinationImage = imageCopy.getSubimage(expandedRectangle.x, expandedRectangle.y,
						expandedRectangle.width + KernelUtils.DEFAULT_BLUR_KERNEL_SIZE, expandedRectangle.height + KernelUtils.DEFAULT_BLUR_KERNEL_SIZE);
				ColorModel colorModel = destinationImage.getColorModel();
				BufferedImage sourceImage = new BufferedImage(colorModel, destinationImage.copyData(destinationImage.getRaster().createCompatibleWritableRaster()),
					colorModel.isAlphaPremultiplied(), null).getSubimage(0, 0, destinationImage.getWidth(), destinationImage.getHeight());

				new ConvolveOp(new Kernel(KernelUtils.DEFAULT_BLUR_KERNEL_SIZE, KernelUtils.DEFAULT_BLUR_KERNEL_SIZE, blurKernel), ConvolveOp.EDGE_NO_OP, null).filter(sourceImage, destinationImage);
			}

			addNoise(imageCopy, expandedRectangle.x, expandedRectangle.y,
					expandedRectangle.width + KernelUtils.DEFAULT_BLUR_KERNEL_SIZE,
					expandedRectangle.height + KernelUtils.DEFAULT_BLUR_KERNEL_SIZE);

			addNoise(imageCopy, expandedRectangle.x, expandedRectangle.y,
					expandedRectangle.width + KernelUtils.DEFAULT_BLUR_KERNEL_SIZE,
					expandedRectangle.height + KernelUtils.DEFAULT_BLUR_KERNEL_SIZE);

			addNoise(imageCopy, expandedRectangle.x, expandedRectangle.y,
					expandedRectangle.width + KernelUtils.DEFAULT_BLUR_KERNEL_SIZE,
					expandedRectangle.height + KernelUtils.DEFAULT_BLUR_KERNEL_SIZE);

			addNoise(imageCopy, expandedRectangle.x, expandedRectangle.y,
					expandedRectangle.width + KernelUtils.DEFAULT_BLUR_KERNEL_SIZE,
					expandedRectangle.height + KernelUtils.DEFAULT_BLUR_KERNEL_SIZE);
		}

		imageCopy = imageCopy.getSubimage(borderX, borderY, mImage.getWidth(), mImage.getHeight());

		return imageCopy;
	}

	private Rectangle getExpandedRectangle(final Rectangle blurRectangle) {
		final Rectangle expandedRectangle = new Rectangle(blurRectangle);
		expandedRectangle.x = (int) (expandedRectangle.x - (expandedRectangle.width * 0.333));
		expandedRectangle.y = (int) (expandedRectangle.y - (expandedRectangle.height * 0.333));
		expandedRectangle.width = (int) (expandedRectangle.width + (expandedRectangle.width * 0.666));
		expandedRectangle.height = (int) (expandedRectangle.height + (expandedRectangle.height * 0.666));

		if (expandedRectangle.x < 0) {
			expandedRectangle.x = 0;
		}

		if (expandedRectangle.y < 0) {
			expandedRectangle.y = 0;
		}

		while (expandedRectangle.x + expandedRectangle.width > mImage.getWidth()) {
			expandedRectangle.width--;

			if (expandedRectangle.width <= 0) {
				expandedRectangle.width = 0;

				break;
			}
		}

		while (expandedRectangle.y + expandedRectangle.height > mImage.getHeight()) {
			expandedRectangle.height--;

			if (expandedRectangle.height <= 0) {
				expandedRectangle.height = 0;

				break;
			}
		}

		return expandedRectangle;
	}

	private void drawGalleryImage(final Graphics graphics) {
		calculateBlurBoxesForCurrentDimension();

		if (mImage != null && SCALE_TO_FIT == false) {
			BufferedImage imageCopy = getImageWithScaledRedactions();

			graphics.drawImage(imageCopy, 0, 0, this);

			if (mPointStart != null && mMouseMovedTarget != null) {
				Graphics2D graphics2d = (Graphics2D) graphics;

				Rectangle mouseDraggedRectangle = getMouseDraggedRectangle();

				graphics2d.setStroke(new BasicStroke(3));

				graphics2d.setColor(new Color(255, 255, 255));
                graphics2d.drawRect(mouseDraggedRectangle.x, mouseDraggedRectangle.y, mouseDraggedRectangle.width, mouseDraggedRectangle.height);

                graphics2d.setColor(new Color(255, 255, 255, 127));
                graphics2d.fillRect(mouseDraggedRectangle.x + 2, mouseDraggedRectangle.y + 2, mouseDraggedRectangle.width - 3, mouseDraggedRectangle.height - 3);
            }
		}
		else if(mImage != null && SCALE_TO_FIT == true) {
			int height = this.getHeight();
			int width = this.getWidth();
			Dimension resized = DimenUtils.getScaledDimension(new Dimension(mOriginalWidth, mOriginalHeight), new Dimension(width, height));

			BufferedImage imageCopy = getImageWithScaledRedactions();

			graphics.drawImage(imageCopy, 0, 0, resized.width, resized.height, this);

			if (mPointStart != null && mMouseMovedTarget != null) {
				Graphics2D graphics2d = (Graphics2D) graphics;

				Rectangle mouseDraggedRectangle = getMouseDraggedRectangle();

				graphics2d.setStroke(new BasicStroke(3));

				graphics2d.setColor(new Color(255, 255, 255));
                graphics2d.drawRect(mouseDraggedRectangle.x, mouseDraggedRectangle.y, mouseDraggedRectangle.width, mouseDraggedRectangle.height);

                graphics2d.setColor(new Color(255, 255, 255, 127));
                graphics2d.fillRect(mouseDraggedRectangle.x + 2, mouseDraggedRectangle.y + 2, mouseDraggedRectangle.width - 3, mouseDraggedRectangle.height - 3);
            }
		}

		mFinishedPaint = true;
	}

	public boolean hasFinishedPaint() {
		return mFinishedPaint;
	}

	public void setHasFinishedPaint(final boolean newHasFinishedPaint) {
		mFinishedPaint = newHasFinishedPaint;
	}
}

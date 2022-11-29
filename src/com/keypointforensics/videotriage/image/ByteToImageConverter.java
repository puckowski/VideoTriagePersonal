package com.keypointforensics.videotriage.image;

import java.awt.*;
import java.awt.image.*;

import com.keypointforensics.videotriage.gui.main.controller.CameraControllerRegistry;

import java.awt.color.*;

public class ByteToImageConverter {
	
	/*
	 * Author: Daniel Puckowski
	 */
	
	private static final CameraControllerRegistry CONTROLLER_REGISTRY = CameraControllerRegistry.INSTANCE;
	private static final int[]                    COLUMN_ORDER = { 2, 1, 0 };
	
	private String        mControllerId;
	private BufferedImage mImage;

	public ByteToImageConverter(final String controllerId, final int width, final int height, final byte[] data) {
		mControllerId = controllerId;
			
		DataBufferByte dataBuffer = new DataBufferByte(data, data.length);
		PixelInterleavedSampleModel sampleModel = new PixelInterleavedSampleModel(DataBuffer.TYPE_BYTE, width,
				height, 3, 3 * width, COLUMN_ORDER);

		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
		ColorModel colourModel = new ComponentColorModel(cs, false, false, Transparency.OPAQUE,
				DataBuffer.TYPE_BYTE);

		WritableRaster raster = Raster.createWritableRaster(sampleModel, dataBuffer, new Point(0, 0));

		mImage = new BufferedImage(colourModel, raster, false, null);
			
		dataBuffer = null;
		sampleModel = null;
		cs = null;
		colourModel = null;
		raster = null;
	}

	public BufferedImage getImage() {
		mImage = (BufferedImage) makeNonBlobPixelsTransparent(mImage);

		return mImage;
	}

	private BufferedImage imageToBufferedImage(final Image image) {
		BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics2d = bufferedImage.createGraphics();

		graphics2d.drawImage(image, 0, 0, null);
		graphics2d.dispose();
		
		return bufferedImage;
	}

	private BufferedImage makeNonBlobPixelsTransparent(final BufferedImage blobBufferedImage) {
		ImageFilter blobFilter = new RGBImageFilter() {
			private final int MOST_RECENT_COLOR_RGB = CONTROLLER_REGISTRY.getController(mControllerId).getBlobParams().getBorderColor().getRGB();
			
			public final int filterRGB(int x, int y, int rgb) {
				if (rgb == FastRgb.DEFAULT_ARGB_VALUE) { 
					return 0x00FFFFFF & rgb;
				} else {
					return MOST_RECENT_COLOR_RGB; 
				}
			}
		};

		ImageProducer imageProducer = new FilteredImageSource(blobBufferedImage.getSource(), blobFilter);

		return imageToBufferedImage(Toolkit.getDefaultToolkit().createImage(imageProducer));
	}
}

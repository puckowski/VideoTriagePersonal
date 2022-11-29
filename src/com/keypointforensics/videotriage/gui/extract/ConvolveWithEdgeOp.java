package com.keypointforensics.videotriage.gui.extract;

import java.awt.image.*;
import java.awt.*;

public class ConvolveWithEdgeOp {

    /**
     * Alias for {@link ConvolveOp#EDGE_ZERO_FILL}.
     * @see #EDGE_REFLECT
     */
    public static final int EDGE_ZERO_FILL = ConvolveOp.EDGE_ZERO_FILL;
    /**
     * Alias for {@link ConvolveOp#EDGE_NO_OP}.
     * @see #EDGE_REFLECT
     */
    public static final int EDGE_NO_OP = ConvolveOp.EDGE_NO_OP;
    /**
     * Adds a border to the image while convolving. The border will reflect the
     * edges of the original image. This is usually a good default.
     * Note that while this mode typically provides better quality than the
     * standard modes {@code EDGE_ZERO_FILL} and {@code EDGE_NO_OP}, it does so
     * at the expense of higher memory consumption and considerable more computation.
     */
    public static final int EDGE_REFLECT = 2; // as JAI BORDER_REFLECT
    /**
     * Adds a border to the image while convolving. The border will wrap the
     * edges of the original image. This is usually the best choice for tiles.
     * Note that while this mode typically provides better quality than the
     * standard modes {@code EDGE_ZERO_FILL} and {@code EDGE_NO_OP}, it does so
     * at the expense of higher memory consumption and considerable more computation.
     * @see #EDGE_REFLECT
     */
    public static final int EDGE_WRAP = 3; // as JAI BORDER_WRAP

    public static BufferedImage addBorder(final BufferedImage pOriginal, final int pBorderX, final int pBorderY) {
        // TODO: Might be faster if we could clone raster and stretch it...
        int w = pOriginal.getWidth();
        int h = pOriginal.getHeight();

        ColorModel cm = pOriginal.getColorModel();
        WritableRaster raster = cm.createCompatibleWritableRaster(w + 2 * pBorderX, h + 2 * pBorderY);
        BufferedImage bordered = new BufferedImage(cm, raster, cm.isAlphaPremultiplied(), null);

        Graphics2D g = bordered.createGraphics();
        try {
            g.setComposite(AlphaComposite.Src);
            g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);

            // Draw original in center
            g.drawImage(pOriginal, pBorderX, pBorderY, null);

            // TODO: I guess we need the top/left etc, if the corner pixels are covered by the kernel
            // Top/left (empty)
            g.drawImage(pOriginal, pBorderX, 0, pBorderX + w, pBorderY, 0, 0, w, 1, null); // Top/center
            // Top/right (empty)

            g.drawImage(pOriginal, -w + pBorderX, pBorderY, pBorderX, h + pBorderY, 0, 0, 1, h, null); // Center/left
            // Center/center (already drawn)
            g.drawImage(pOriginal, w + pBorderX, pBorderY, 2 * pBorderX + w, h + pBorderY, w - 1, 0, w, h, null); // Center/right

            // Bottom/left (empty)
            g.drawImage(pOriginal, pBorderX, pBorderY + h, pBorderX + w, 2 * pBorderY + h, 0, h - 1, w, h, null); // Bottom/center
            // Bottom/right (empty)
        }
        finally {
            g.dispose();
        }

        return bordered;
    }

    public static BufferedImage addBorder(final BufferedImage pOriginal, final int pBorderX, final int pBorderY, int edgeCondition) {
        if ((edgeCondition & 2) == 0) {
            return pOriginal;
        }

        // TODO: Might be faster if we could clone raster and stretch it...
        int w = pOriginal.getWidth();
        int h = pOriginal.getHeight();

        ColorModel cm = pOriginal.getColorModel();
        WritableRaster raster = cm.createCompatibleWritableRaster(w + 2 * pBorderX, h + 2 * pBorderY);
        BufferedImage bordered = new BufferedImage(cm, raster, cm.isAlphaPremultiplied(), null);

        Graphics2D g = bordered.createGraphics();
        try {
            g.setComposite(AlphaComposite.Src);
            g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);

            // Draw original in center
            g.drawImage(pOriginal, pBorderX, pBorderY, null);

            // TODO: I guess we need the top/left etc, if the corner pixels are covered by the kernel
            switch (edgeCondition) {
                case EDGE_REFLECT: {
                    // Top/left (empty)
                    g.drawImage(pOriginal, pBorderX, 0, pBorderX + w, pBorderY, 0, 0, w, 1, null); // Top/center
                    // Top/right (empty)

                    g.drawImage(pOriginal, -w + pBorderX, pBorderY, pBorderX, h + pBorderY, 0, 0, 1, h, null); // Center/left
                    // Center/center (already drawn)
                    g.drawImage(pOriginal, w + pBorderX, pBorderY, 2 * pBorderX + w, h + pBorderY, w - 1, 0, w, h, null); // Center/right

                    // Bottom/left (empty)
                    g.drawImage(pOriginal, pBorderX, pBorderY + h, pBorderX + w, 2 * pBorderY + h, 0, h - 1, w, h, null); // Bottom/center
                    // Bottom/right (empty)
                 
                    break;
                }
               case EDGE_WRAP: {
                    g.drawImage(pOriginal, -w + pBorderX, -h + pBorderY, null); // Top/left
                    g.drawImage(pOriginal, pBorderX, -h + pBorderY, null); // Top/center
                    g.drawImage(pOriginal, w + pBorderX, -h + pBorderY, null); // Top/right

                    g.drawImage(pOriginal, -w + pBorderX, pBorderY, null); // Center/left
                    // Center/center (already drawn)
                    g.drawImage(pOriginal, w + pBorderX, pBorderY, null); // Center/right

                    g.drawImage(pOriginal, -w + pBorderX, h + pBorderY, null); // Bottom/left
                    g.drawImage(pOriginal, pBorderX, h + pBorderY, null); // Bottom/center
                    g.drawImage(pOriginal, w + pBorderX, h + pBorderY, null); // Bottom/right
                
                    break;
               }
                default: {
                    throw new IllegalArgumentException("Illegal edge operation " + edgeCondition);              
                }             
            }

        }
        finally {
            g.dispose();
        }

        return bordered;
    }
    
}
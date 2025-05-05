package com.keypointforensics.videotriage.image.match;

import com.keypointforensics.videotriage.image.match.OrbKeypoint;

import java.util.*;

public class ORBFeatureExtractor {

    // Circle offsets for FAST (16 pixels around candidate)
    static int[][] circleOffsets = {
            {0, -3}, {1, -3}, {2, -2}, {3, -1}, {3, 0}, {3, 1}, {2, 2}, {1, 3},
            {0, 3}, {-1, 3}, {-2, 2}, {-3, 1}, {-3, 0}, {-3, -1}, {-2, -2}, {-1, -3}
    };

    // Test pattern for BRIEF descriptor (rotated later)
    static int[][] briefPairs = new int[256][4]; // x1, y1, x2, y2

    static {
        Random rand = new Random(0);
        for (int i = 0; i < briefPairs.length; i++) {
            briefPairs[i][0] = rand.nextInt(9) - 4;
            briefPairs[i][1] = rand.nextInt(9) - 4;
            briefPairs[i][2] = rand.nextInt(9) - 4;
            briefPairs[i][3] = rand.nextInt(9) - 4;
        }
    }

    public static List<OrbKeypoint> detectAndDescribe(int[][] image) {
        int h = image.length, w = image[0].length;
        List<OrbKeypoint> OrbKeypoints = new ArrayList<>();

        // FAST OrbKeypoint detection
        for (int y = 3; y < h - 3; y++) {
            for (int x = 3; x < w - 3; x++) {
                if (isCornerFAST(image, x, y)) {
                    double angle = computeOrientation(image, x, y);
                    OrbKeypoint kp = new OrbKeypoint(x, y, angle);
                    kp.descriptor = computeBriefDescriptor(image, kp);
                    OrbKeypoints.add(kp);
                }
            }
        }

        return OrbKeypoints;
    }

    static boolean isCornerFAST(int[][] img, int x, int y) {
        int center = img[y][x];
        int threshold = 20;
        int count = 0;

        for (int[] offset : circleOffsets) {
            int dx = x + offset[0], dy = y + offset[1];
            if (dy < 0 || dy >= img.length || dx < 0 || dx >= img[0].length) continue;
            if (Math.abs(img[dy][dx] - center) > threshold) {
                count++;
                if (count >= 12) return true;
            } else {
                count = 0;
            }
        }

        return false;
    }

    static double computeOrientation(int[][] img, int x, int y) {
        int m01 = 0, m10 = 0;
        int radius = 3;
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                int xx = x + dx, yy = y + dy;
                if (yy < 0 || yy >= img.length || xx < 0 || xx >= img[0].length) continue;
                int val = img[yy][xx];
                m01 += dy * val;
                m10 += dx * val;
            }
        }
        return Math.atan2(m01, m10);
    }

    static byte[] computeBriefDescriptor(int[][] img, OrbKeypoint kp) {
        byte[] desc = new byte[32]; // 256 bits = 32 bytes
        double cos = Math.cos(kp.angle), sin = Math.sin(kp.angle);

        for (int i = 0; i < 256; i++) {
            int x1 = (int) Math.round(kp.x + briefPairs[i][0] * cos - briefPairs[i][1] * sin);
            int y1 = (int) Math.round(kp.y + briefPairs[i][0] * sin + briefPairs[i][1] * cos);
            int x2 = (int) Math.round(kp.x + briefPairs[i][2] * cos - briefPairs[i][3] * sin);
            int y2 = (int) Math.round(kp.y + briefPairs[i][2] * sin + briefPairs[i][3] * cos);

            if (inBounds(img, x1, y1) && inBounds(img, x2, y2)) {
                int bit = (img[y1][x1] < img[y2][x2]) ? 1 : 0;
                desc[i / 8] |= (bit << (7 - (i % 8)));
            }
        }

        return desc;
    }

    static boolean inBounds(int[][] img, int x, int y) {
        return y >= 0 && y < img.length && x >= 0 && x < img[0].length;
    }

    public static List<OrbKeypoint> getKeypointsForImage(final int[][] image) {
        return detectAndDescribe(image);
    }
}

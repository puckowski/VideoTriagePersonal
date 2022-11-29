package com.keypointforensics.videotriage.detect;

public class CannyPruner {
	
	public int[] getIntegralCanny(int[] grayImage, int width, int height) {
		int[] canny = new int[grayImage.length];

		for (int i = 2; i < width - 2; i++) {
			for (int j = 2; j < height - 2; j++) {
				int sum = 0;
				sum += 2 * grayImage[i - 2 + (j - 2) * width];
				sum += 4 * grayImage[i - 2 + (j - 1) * width];
				sum += 5 * grayImage[i - 2 + (j + 0) * width];
				sum += 4 * grayImage[i - 2 + (j + 1) * width];
				sum += 2 * grayImage[i - 2 + (j + 2) * width];
				sum += 4 * grayImage[i - 1 + (j - 2) * width];
				sum += 9 * grayImage[i - 1 + (j - 1) * width];
				sum += 12 * grayImage[i - 1 + (j + 0) * width];
				sum += 9 * grayImage[i - 1 + (j + 1) * width];
				sum += 4 * grayImage[i - 1 + (j + 2) * width];
				sum += 5 * grayImage[i + 0 + (j - 2) * width];
				sum += 12 * grayImage[i + 0 + (j - 1) * width];
				sum += 15 * grayImage[i + 0 + (j + 0) * width];
				sum += 12 * grayImage[i + 0 + (j + 1) * width];
				sum += 5 * grayImage[i + 0 + (j + 2) * width];
				sum += 4 * grayImage[i + 1 + (j - 2) * width];
				sum += 9 * grayImage[i + 1 + (j - 1) * width];
				sum += 12 * grayImage[i + 1 + (j + 0) * width];
				sum += 9 * grayImage[i + 1 + (j + 1) * width];
				sum += 4 * grayImage[i + 1 + (j + 2) * width];
				sum += 2 * grayImage[i + 2 + (j - 2) * width];
				sum += 4 * grayImage[i + 2 + (j - 1) * width];
				sum += 5 * grayImage[i + 2 + (j + 0) * width];
				sum += 4 * grayImage[i + 2 + (j + 1) * width];
				sum += 2 * grayImage[i + 2 + (j + 2) * width];

				canny[i + j * width] = sum / 159;
				// System.out.println(canny[i][j]);
			}
		}

		int[] grad = new int[grayImage.length];
		for (int i = 1; i < width - 1; i++) {
			for (int j = 1; j < height - 1; j++) {
				int grad_x = -canny[i - 1 + (j - 1) * width] + canny[i + 1 + (j - 1) * width]
						- 2 * canny[i - 1 + (j) * width] + 2 * canny[i + 1 + (j) * width]
						- canny[i - 1 + (j + 1) * width] + canny[i + 1 + (j + 1) * width];
				int grad_y = canny[i - 1 + (j - 1) * width] + 2 * canny[i + (j - 1) * width]
						+ canny[i + 1 + (j - 1) * width] - canny[i - 1 + (j + 1) * width]
						- 2 * canny[i + (j + 1) * width] - canny[i + 1 + (j + 1) * width];
				grad[i + j * width] = Math.abs(grad_x) + Math.abs(grad_y);
				// System.out.println(grad[i][j]);
			}
		}

		for (int i = 0; i < width; i++) {
			int col = 0;
			for (int j = 0; j < height; j++) {
				int value = grad[i + j * width];
				// Not data parallel
				canny[i + j * width] = (i > 0 ? canny[i - 1 + j * width] : 0) + col + value;

				col += value;
			}
		}

		return canny;
	}
	
}
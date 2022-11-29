package com.keypointforensics.videotriage.filter.errorlevel;

public enum Pixel {

	// Access these pixel values by name
	BLACK(0, 0, 0), BLUE(0, 0, 255), CYAN(0, 255, 255), GREEN(0, 255, 0), MAGENTA(255, 0, 255), ORANGE(255, 128,
			0), RED(255, 0, 0), WHITE(255, 255, 255), YELLOW(255, 255, 0);

	private int[] rgb = new int[3];

	Pixel(int red, int green, int blue) {
		rgb[0] = red;
		rgb[1] = green;
		rgb[2] = blue;
	}

	/**
	 * Returns the RGB value of the enumerated color.
	 * 
	 * @return The RGB value of the enumerated color.
	 */
	public int[] RGB() {
		return rgb;
	}

}
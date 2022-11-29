package com.keypointforensics.videotriage.filter;

public enum EVideoTriageFilter {
    
	SHARPEN_FILTER_STANDARD("Sharpen Filter Standard"),
	SHARPEN_FILTER_MASKING("Sharpen Filter Masking"),
	CONTRAST_FILTER_STANDARD("Contrast Filter Standard"),
	CONVOLVE_FILTER_STANDARD("Convolve Filter Standard"),
	GAUSSIAN_FILTER_STANDARD("Gaussian Filter Standard"),
	MEDIAN_FILTER_STANDARD("Median Filter Standard"),
	REDUCE_NOISE_FILTER_STANDARD("Reduce Noise Filter Standard"),
	HISTOGRAM_EQUALIZATION_STANDARD("Histogram Equalization Filter Standard"),
	CONTRAST_STRETCH_STANDARD("Contrast Stretch Filter Standard"),
	EMBOSS_STANDARD("Emboss Filter Standard"),
	CANNY_EDGE_STANDARD("Canny Edge Filter Standard");
	
    private final String mName;
    
    private EVideoTriageFilter(final String name) {
        mName = name;
    }

    @Override
    public String toString() {
        return mName;
    }
    
}
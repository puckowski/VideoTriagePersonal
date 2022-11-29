package com.keypointforensics.videotriage.video;

public enum EVideoTriageVideoFilter {
    
	DESHAKE_FILTER_STANDARD("Stabilize Filter Standard", 
		"Stabilize shaky video.",
		"stabilized_preview.jpg"),
	LIGHTER_FILTER_STANDARD("Lighter Filter Standard", 
			"Fast way to lighten dark video.",
			"lighter_preview.jpg"),
	DARKER_FILTER_STANDARD("Darker Filter Standard", 
			"Fast way to darken light video.",
			"darker_preview.jpg"),
	NEGATIVE_FILTER_STANDARD("Negative Filter Standard", 
		"Inverts luma. Lightest areas appear darkest and darkest areas appear lightest.",
		"negative_preview.jpg"),
	COLOR_NEGATIVE_FILTER_STANDARD("Color Negative Filter Standard", 
			"Inverts luma and colors. Lightest areas appear darkest and darkest areas appear lightest. Colors are also reversed into their respective complementary colors.",
			"color_negative_preview.jpg"),
	STRONG_CONTRAST_FILTER_STANDARD("Strong Contrast Filter Standard", 
			"Strong video contrast preset.",
			"strong_contrast_preview.jpg"),
	MEDIUM_CONTRAST_FILTER_STANDARD("Medium Contrast Filter Standard", 
			"Medium video contrast preset.",
			"medium_contrast_preview.jpg"),
	LINEAR_CONTRAST_FILTER_STANDARD("Linear Contrast Filter Standard", 
			"Linear video contrast preset.",
			"linear_contrast_preview.jpg"),
	INCREASE_CONTRAST_FILTER_STANDARD("Increase Contrast Filter Standard", 
			"Fast way to increase video contrast.",
			"increase_contrast_preview.jpg"),
	NORMALIZE_FILTER_STANDARD("Normalize Filter Standard", 
			"Stretch video contrast with temporal smoothing.",
			"contrast_stretch_preview.jpg"),
	HISTOGRAM_EQUALIZATION_FILTER_STANDARD("Histogram Equalization Filter Standard", 
			"Equalize video contrast. This filter is useful only for correcting degraded or poorly captured video.",
			"contrast_equalize_preview.jpg"),
	SIMPLE_POST_PROCESS_FILTER_STANDARD("Simple Postprocessing Filter Standard", 
			"Simple postprocessing filter which applies averages of pixel values to increase video quality.",
			"simple_post_process_preview.jpg"),
	SIMPLE_POST_PROCESS_FILTER_CENTERED("Simple Postprocessing Filter Centered", 
			"Simple postprocessing filter which applies averages of pixel values to increase video quality. Only center sample is used after inverse discrete cosine transform.",
			"centered_simple_post_process_preview.jpg"),
	DEBLOCK_DERING_POST_PROCESS_FILTER_STANDARD("Deblocking and Deringing Filter Standard", 
			"Deblocking and deringing postprocessing filter to improve visual quality by smoothing sharp edges and by correcting compression artifacts.",
			"deblock_dering_post_process_preview.jpg"),
	TEMPORAL_AVERAGING_DENOISER_FILTER_STANDARD("Temporal Averaging Denoise Filter Standard", 
			"Apply an adaptive temporal averaging denoiser to the video.",
			"temporal_averaging_denoise_preview.jpg"),
	DEINTERLACE_FILTER_STANDARD("Deinterlace Filter Standard", 
			"Fast way to deinterlace interlaced video.",
			"deinterlace_preview.jpg"),
	DEINTERLACE_FILTER_HIGH_QUALITY("Deinterlace Filter High Quality", 
			"Deinterlace interlaced video with maximum possible quality. Slower than standard deinterlace.",
			"deinterlace_quality_preview.jpg");
	
    private final String mName;
    private final String mDescription;
    private final String mPreviewFilename;
    
    private EVideoTriageVideoFilter(final String name, final String description, final String previewFilename) {
        mName            = name;
        mDescription     = description;
        mPreviewFilename = previewFilename;
    }

    @Override
    public String toString() {
        return mName;
    }
    
    public String getName() {
    	return mName;
    }
    
    public String getDescription() {
    	return mDescription;
    }
    
    public String getPreviewFilename() {
    	return mPreviewFilename;
    }
}
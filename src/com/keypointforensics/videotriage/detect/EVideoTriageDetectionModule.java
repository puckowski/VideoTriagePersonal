package com.keypointforensics.videotriage.detect;

public enum EVideoTriageDetectionModule {

	DETECTION_MODULE_FACE("Facial Detection", "Face Detections"),
	DETECTION_MODULE_LICENSE_PLATE("License Plate Detection", "License Plate Detections"),
	DETECTION_MODULE_PEDESTRIAN("Pedestrian Detection", "Pedestrian Detections"),
	DETECTION_MODULE_CAR("Car Detection", "Car Detections"),
	DETECTION_MODULE_EXPLICIT("Explicit Detection", "Explicit Detections");
	
    private final String mName;
    private final String mChartDisplayName;
    
    private EVideoTriageDetectionModule(final String name, final String chartDisplayName) {
        mName = name;
        mChartDisplayName = chartDisplayName;
    }

    public String getChartDisplayName() {
    	return mChartDisplayName;
    }
    
    @Override
    public String toString() {
        return mName;
    }
    
}

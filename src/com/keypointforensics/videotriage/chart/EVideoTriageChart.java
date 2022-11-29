package com.keypointforensics.videotriage.chart;

public enum EVideoTriageChart {
    
	TIME_SERIES_STANDARD("Time Series", "time.html", "time.js"),
	TIME_SERIES_SCATTER("Time Series Scatter", "time_scatter.html", "time_scatter.js"),
	TIME_SERIES_FILLED_AREA("Time Series Filled Area", "time_filled_area.html", "time_filledarea.js"),
	TIME_SERIES_MASS("Time Series Mass", "time_mass.html", "time_mass.js"),
	TIME_SERIES_FACES("Time Series Face Detection", "time_face.html", "time_face.js"),
	TIME_SERIES_LICENSE_PLATES("Time Series License Plate Detection", "time_plate.html", "time_plate.js"),
	TIME_SERIES_PEDESTRIANS("Time Series Pedestrian Detection", "time_pedestrian.html", "time_pedestrian.js"),
	TIME_SERIES_CARS("Time Series Car Detection", "time_car.html", "time_car.js"),
	TIME_SERIES_EXPLICIT("Time Series Explicit Image Detection", "time_explicit.html", "time_explicit.js"),
	BLOB_TRACKING_SCATTER_STANDARD("Object Tracking Scatter", "blob_tracking_scatter.html", "blob_tracking_scatter.js"),
	BLOB_TRACKING_SCATTER_BACKGROUND("Object Tracking Scatter With Background", "blob_tracking_scatter_background.html", "blob_tracking_scatter_background.js"),
	BLOB_TRACKING_HEAT_MAP_STANDARD("Object Tracking Heat Map", "blob_tracking_heat_map.html", "blob_tracking_heat_map.js"),
	BAR_CHART_HOUR_OF_DAY_STANDARD("Hour of Day Bar Chart", "hour_day_bar.html", "hour_day_bar.js"),
	HISTOGRAM_CONTOUR_BACKGROUND("Histogram Contour Chart With Background", "histogram_contour_background.html", "histogram_contour_background.js"),
	DETECTION_COMPOSITION_PIE_STANDARD("Detection Composition Pie", "detection_composition.html", "detection_pie.js");
	
    private final String mName;
    private final String mPageNameShort;
    private final String mDataNameShort;
    
    private EVideoTriageChart(final String name, final String pageNameShort, final String dataNameShort) {
        mName = name;
        mPageNameShort = pageNameShort;
        mDataNameShort = dataNameShort;
    }

    @Override
    public String toString() {
        return mName;
    }
    
    public String getPageNameShort() {
    	return mPageNameShort;
    }
    
    public String getDataNameShort() {
    	return mDataNameShort;
    }
    
}
package com.keypointforensics.videotriage.util;

import java.util.ArrayList;

public class CascadeUtils {

	public static final String HAAR_CASCADE_FACE_DEFAULT      = "front_default.xml";
	public static final String HAAR_CASCADE_FACE_STUMP        = "front_alt_stump.xml";
	public static final String HAAR_CASCADE_FACE_STUMP_STAGED = "front_alt_stump_staged.xml";
	public static final String HAAR_CASCADE_FACE_TREE         = "front_alt_tree.xml";
	
	public static final String HAAR_CASCADE_LICENSE_PLATE_DEFAULT = "license_plate_default.xml";
	public static final String HAAR_CASCADE_LICENSE_PLATE_SIMPLE  = "license_plate_simple.xml";
	
	public static final String HAAR_CASCADE_PEDESTRIAN_DEFAULT = "pedestrian_default.xml";
	public static final String HAAR_CASCADE_PEDESTRIAN_SIMPLE  = "pedestrian_simple.xml";
	
	public static final String HAAR_CASCADE_CAR_DEFAULT = "car_default.xml";
	public static final String HAAR_CASCADE_CAR_SIMPLE  = "car_simple.xml";
	
	public static final String HAAR_CASCADE_EXPLICIT_DEFAULT = "explicit_default.xml";
	public static final String HAAR_CASCADE_EXPLICIT_SIMPLE = "explicit_simple.xml";
	
	public static ArrayList<String> getCompleteFaceHaarCascadeList() {
		ArrayList<String> allFaceCascades = new ArrayList<String>();
		
		allFaceCascades.add(FileUtils.CASCADES_DIRECTORY + HAAR_CASCADE_FACE_DEFAULT);
		allFaceCascades.add(FileUtils.CASCADES_DIRECTORY + HAAR_CASCADE_FACE_STUMP);
		allFaceCascades.add(FileUtils.CASCADES_DIRECTORY + HAAR_CASCADE_FACE_STUMP_STAGED);
		allFaceCascades.add(FileUtils.CASCADES_DIRECTORY + HAAR_CASCADE_FACE_TREE);
		
		return allFaceCascades;
	}
	
	public static ArrayList<String> getCompleteLicensePlateHaarCascadeList() {
		ArrayList<String> allFaceCascades = new ArrayList<String>();
		
		allFaceCascades.add(FileUtils.CASCADES_DIRECTORY + HAAR_CASCADE_LICENSE_PLATE_DEFAULT);
		allFaceCascades.add(FileUtils.CASCADES_DIRECTORY + HAAR_CASCADE_LICENSE_PLATE_SIMPLE);
		
		return allFaceCascades;
	}
	
	public static ArrayList<String> getCompletePedestrianHaarCascadeList() {
		ArrayList<String> allPedestrianCascades = new ArrayList<String>();
		
		allPedestrianCascades.add(FileUtils.CASCADES_DIRECTORY + HAAR_CASCADE_PEDESTRIAN_DEFAULT);
		allPedestrianCascades.add(FileUtils.CASCADES_DIRECTORY + HAAR_CASCADE_PEDESTRIAN_SIMPLE);
		
		return allPedestrianCascades;
	}
	
	public static ArrayList<String> getCompleteCarHaarCascadeList() {
		ArrayList<String> allCarCascades = new ArrayList<String>();
		
		allCarCascades.add(FileUtils.CASCADES_DIRECTORY + HAAR_CASCADE_CAR_DEFAULT);
		allCarCascades.add(FileUtils.CASCADES_DIRECTORY + HAAR_CASCADE_CAR_SIMPLE);
		
		return allCarCascades;
	}
	
	public static ArrayList<String> getCompleteExplicitHaarCascadeList() {
		ArrayList<String> allExplicitCascades = new ArrayList<String>();
		
		allExplicitCascades.add(FileUtils.CASCADES_DIRECTORY + HAAR_CASCADE_EXPLICIT_DEFAULT);
		allExplicitCascades.add(FileUtils.CASCADES_DIRECTORY + HAAR_CASCADE_EXPLICIT_SIMPLE);
		
		return allExplicitCascades;
	}
	
}

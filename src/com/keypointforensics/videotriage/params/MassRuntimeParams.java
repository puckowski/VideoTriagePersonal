package com.keypointforensics.videotriage.params;

public class MassRuntimeParams {

	/*
	 * Author: Daniel Puckowski
	 */
	
	public static final int INVALID_MASS_THRESHOLD = -1;
	public static final int MINIMUM_MASS_THRESHOLD = 0;
	public static final int MAXIMUM_MASS_THRESHOLD = 1;
	
	private final int MASS_THRESHOLD_CONSTANT = 1000;
	
	private final double            DEFAULT_MASS_THRESHOLD = 0.010;
	private double mMassThreshold = INVALID_MASS_THRESHOLD;
	
	private final double                         DEFAULT_MASS_CONSIDERATION_THRESHOLD = 0.001;
	private double mMassConsiderationThreshold = DEFAULT_MASS_CONSIDERATION_THRESHOLD;
	
	public MassRuntimeParams() {
		getThreshold();
		getConsiderationThreshold();
	}
	
	private void getConsiderationThreshold() {
		if(mMassConsiderationThreshold == INVALID_MASS_THRESHOLD) {
			mMassConsiderationThreshold = DEFAULT_MASS_CONSIDERATION_THRESHOLD;
		}
	}
	
	public int getConsiderationThresholdInt() {		
		return (int) (mMassConsiderationThreshold * MASS_THRESHOLD_CONSTANT);
	}
	
	public double getConsiderationThresholdDouble() {		
		return mMassConsiderationThreshold;
	}
	
	public void setConsiderationThreshold(final int newMassConsiderationThreshold) {
		if(newMassConsiderationThreshold < MINIMUM_MASS_THRESHOLD) {
			return;
		}
		else if(newMassConsiderationThreshold == MINIMUM_MASS_THRESHOLD) {			
			mMassConsiderationThreshold = MINIMUM_MASS_THRESHOLD;
			
			return;
		} 
		else {
			setConsiderationThresholdReal((double) newMassConsiderationThreshold / MASS_THRESHOLD_CONSTANT);
		}
	}
	
	private void setConsiderationThresholdReal(final double newMassConsiderationThreshold) {
		if(newMassConsiderationThreshold >= MINIMUM_MASS_THRESHOLD && newMassConsiderationThreshold <= MAXIMUM_MASS_THRESHOLD) {			
			mMassConsiderationThreshold = newMassConsiderationThreshold;
		} 
	}
	
	private void getThreshold() {
		if(mMassThreshold == INVALID_MASS_THRESHOLD) {
			mMassThreshold = DEFAULT_MASS_THRESHOLD;
		}
	}
	
	public int getThresholdInt() {		
		return (int) (mMassThreshold * MASS_THRESHOLD_CONSTANT);
	}
	
	public double getThresholdDouble() {		
		return mMassThreshold;
	}
	
	public void setThreshold(final int newMassThreshold) {
		if(newMassThreshold < MINIMUM_MASS_THRESHOLD) {
			return;
		}
		else if(newMassThreshold == MINIMUM_MASS_THRESHOLD) {
			mMassThreshold = MINIMUM_MASS_THRESHOLD;
						
			return;
		} 
		else {
			setThresholdReal((double) newMassThreshold / MASS_THRESHOLD_CONSTANT);
		}
	}
	
	private void setThresholdReal(final double newMassThreshold) {
		if(newMassThreshold >= MINIMUM_MASS_THRESHOLD && newMassThreshold <= MAXIMUM_MASS_THRESHOLD) {			
			mMassThreshold = newMassThreshold;
		} 
	}
}

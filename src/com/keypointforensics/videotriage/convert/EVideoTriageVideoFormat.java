package com.keypointforensics.videotriage.convert;

public enum EVideoTriageVideoFormat {
    
	MP4("MP4"),
	AVI("AVI"),
	MOV("MOV");
	
    private final String mName;
    
    private EVideoTriageVideoFormat(final String name) {
        mName = name;
    }

    @Override
    public String toString() {
        return mName;
    }
    
    public String getName() {
    	return mName;
    }
    
}
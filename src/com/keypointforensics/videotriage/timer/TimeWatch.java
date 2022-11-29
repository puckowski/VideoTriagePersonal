package com.keypointforensics.videotriage.timer;

import java.util.concurrent.TimeUnit;

public class TimeWatch { 
	
	/*
	 * Author: Daniel Puckowski
	 */
	
	public static final long INVALID_TIME = -1;
	
    private long mStartMillis;

    public static TimeWatch start() {    	
        return new TimeWatch();
    }

    private TimeWatch() {
        reset();
    }

    public TimeWatch reset() {    	
        mStartMillis = System.currentTimeMillis();
        
        return this;
    }

    public long time() {    	
        final long ends = System.currentTimeMillis();
        
        return ends - mStartMillis;
    }

    public long time(final TimeUnit unit) {	
    	return unit.convert(time(), TimeUnit.MILLISECONDS);
    }
}
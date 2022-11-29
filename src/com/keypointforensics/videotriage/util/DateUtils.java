package com.keypointforensics.videotriage.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateUtils {

	public static String calculateTimeFromSeconds(final long seconds) {
	    int day = (int) TimeUnit.SECONDS.toDays(seconds);
	    long hours = TimeUnit.SECONDS.toHours(seconds) -
	                 TimeUnit.DAYS.toHours(day);
	    long minute = TimeUnit.SECONDS.toMinutes(seconds) - 
	                  TimeUnit.DAYS.toMinutes(day) -
	                  TimeUnit.HOURS.toMinutes(hours);
	    long second = TimeUnit.SECONDS.toSeconds(seconds) -
	                  TimeUnit.DAYS.toSeconds(day) -
	                  TimeUnit.HOURS.toSeconds(hours) - 
	                  TimeUnit.MINUTES.toSeconds(minute);
	    
	    StringBuilder timeElapsedBuilder = new StringBuilder();
	    
	    timeElapsedBuilder.append(day);
	    timeElapsedBuilder.append(" days, ");
	    timeElapsedBuilder.append(hours);
	    timeElapsedBuilder.append(" hours, ");
	    timeElapsedBuilder.append(minute);
	    timeElapsedBuilder.append(" minute");
	    if(minute != 1) {
	    	timeElapsedBuilder.append("s");
	    }
	    timeElapsedBuilder.append(", and ");
	    timeElapsedBuilder.append(second);
	    timeElapsedBuilder.append(" second");
	    if(second != 1) {
	    	timeElapsedBuilder.append("s.");
	    } else {
	    	timeElapsedBuilder.append(".");
	    }
	    
	    return timeElapsedBuilder.toString();
	}
	
	private static String getDaysElapsedString(long diffSeconds, long diffMinutes, long diffHours, long diffDays) {
		StringBuilder elapsedTimeStringBuilder = new StringBuilder();
		
		elapsedTimeStringBuilder.append(diffDays);
		elapsedTimeStringBuilder.append(" days, ");
		elapsedTimeStringBuilder.append(diffHours);
		elapsedTimeStringBuilder.append(" hours, ");
		elapsedTimeStringBuilder.append(diffMinutes);
		elapsedTimeStringBuilder.append(" minute");
		if(diffMinutes != 1) {
			elapsedTimeStringBuilder.append("s");
		}
		elapsedTimeStringBuilder.append(", and ");
		elapsedTimeStringBuilder.append(diffSeconds);
		elapsedTimeStringBuilder.append(" second");
		if(diffSeconds != 1) {
			elapsedTimeStringBuilder.append("s");
		}
		elapsedTimeStringBuilder.append(".");
		
		return elapsedTimeStringBuilder.toString();
	}
	
	public static String getDaysElapsedBetweenDates(final String dateFormatString, final String dateStartString, final String dateStopString) {
		SimpleDateFormat format = new SimpleDateFormat(dateFormatString);

		Date startDate = null;
		Date stopDate = null;

		try {
			startDate = format.parse(dateStartString);
			stopDate = format.parse(dateStopString);
		} catch (ParseException parseException) {
			//parseException.printStackTrace();
			
			return getDaysElapsedString(0, 0, 0, 0);
		}

		long diff = stopDate.getTime() - startDate.getTime();

		long diffSeconds = diff / 1000 % 60;
		long diffMinutes = diff / (60 * 1000) % 60;
		long diffHours = diff / (60 * 60 * 1000) % 24;
		long diffDays = diff / (24 * 60 * 60 * 1000);
		
		return getDaysElapsedString(diffSeconds, diffMinutes, diffHours, diffDays);
	}
	
	public static String getDaysElapsedBetweenDates(final Date startDate, final Date stopDate) {
		long diff = stopDate.getTime() - startDate.getTime();

		long diffSeconds = diff / 1000 % 60;
		long diffMinutes = diff / (60 * 1000) % 60;
		long diffHours = diff / (60 * 60 * 1000) % 24;
		long diffDays = diff / (24 * 60 * 60 * 1000);
		
		return getDaysElapsedString(diffSeconds, diffMinutes, diffHours, diffDays);
	}
	
}

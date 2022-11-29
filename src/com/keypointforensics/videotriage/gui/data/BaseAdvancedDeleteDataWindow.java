package com.keypointforensics.videotriage.gui.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;

import com.keypointforensics.videotriage.legacy.FileUtilsLegacy;
import com.keypointforensics.videotriage.progress.ProgressBundle;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ProgressUtils;

public class BaseAdvancedDeleteDataWindow extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4978706941077530956L;
	
	protected static final int CONTROL_GRID_LAYOUT_ROWS    = 14;
	protected static final int CONTROL_GRID_LAYOUT_COLUMNS = 1;
	
	protected final ERemovalDateRange[] DATE_RANGE_ENUM_ARRAY = { 
		ERemovalDateRange.LAST_HOUR,
		ERemovalDateRange.LAST_DAY,
		ERemovalDateRange.LAST_WEEK,
		ERemovalDateRange.LAST_MONTH,
		ERemovalDateRange.ALL_TIME
	};
		
	protected JComboBox<ERemovalDateRange> mDateRangeComboBox;
	protected JButton mDeleteButton;
	
	protected Date mRemovalStartDate;
	
	protected void setRemovalStartDate() {
		final ERemovalDateRange dateRangeEnum = (ERemovalDateRange) mDateRangeComboBox.getSelectedItem();
		mRemovalStartDate = getRemovalStartDate(dateRangeEnum);
	}

	protected Date getRemovalStartDate(final ERemovalDateRange removalDateRangeEnum) {
		Date removalStartDate = null;
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(Date.from(Instant.now()));
		
		switch(removalDateRangeEnum) {
			case LAST_HOUR: {				
				calendar.add(Calendar.HOUR, -1);
				
				break;
			}
			case ALL_TIME: {
				//Digital video was first introduced commercially in 1986 with the Sony D1 format
				final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
				final String dateInString = "00-00-1986 00:00:00";

				try {
					Date date = simpleDateFormat.parse(dateInString);
					calendar.setTime(date);
				} catch (ParseException parseException) {
					//parseException.printStackTrace();
				}
				
				break;
			}
			case LAST_DAY: {
				calendar.add(Calendar.DAY_OF_YEAR, -1);
				
				break;
			}
			case LAST_MONTH: {
				calendar.add(Calendar.MONTH, -1);
				
				break;
			}
			case LAST_WEEK: {
				calendar.add(Calendar.WEEK_OF_YEAR, -1);
				
				break;
			}
		}
		
		removalStartDate = calendar.getTime();
		
		return removalStartDate;
	}
	
	protected void deleteDataWithinRange(final Date removalStartDate) {
		ProgressBundle deleteProgressBundle = ProgressUtils.getProgressBundle("Deleting Data...");
		deleteProgressBundle.progressBar.setIndeterminate(true);
		deleteProgressBundle.progressBar.repaint();
		
		ArrayList<String> allFiles = (ArrayList<String>) FileUtilsLegacy.parseDirectoryRecursiveForAll(FileUtils.CAPTURES_DIRECTORY);
		allFiles.addAll(FileUtilsLegacy.parseDirectoryRecursiveForAll(FileUtils.DATABASE_DIRECTORY));
		allFiles.addAll(FileUtilsLegacy.parseDirectoryRecursiveForAll(FileUtils.CONTEXT_DIRECTORY));
		allFiles.addAll(FileUtilsLegacy.parseDirectoryRecursiveForAll(FileUtils.ENHANCED_DIRECTORY));
		allFiles.addAll(FileUtilsLegacy.parseDirectoryRecursiveForAll(FileUtils.EXPORTS_DIRECTORY));
		allFiles.addAll(FileUtilsLegacy.parseDirectoryRecursiveForAll(FileUtilsLegacy.VIDEO_FRAME_EXTRACTS_DIRECTORY));
		allFiles.addAll(FileUtilsLegacy.parseDirectoryRecursiveForAll(FileUtils.PLATES_DIRECTORY));
		allFiles.addAll(FileUtilsLegacy.parseDirectoryRecursiveForAll(FileUtils.FACES_DIRECTORY));
		allFiles.addAll(FileUtilsLegacy.parseDirectoryRecursiveForAll(FileUtils.PEDESTRIANS_DIRECTORY));
		allFiles.addAll(FileUtilsLegacy.parseDirectoryRecursiveForAll(FileUtils.CARS_DIRECTORY));
		allFiles.addAll(FileUtilsLegacy.parseDirectoryRecursiveForAll(FileUtils.EXPLICIT_DIRECTORY));
		allFiles.addAll(FileUtilsLegacy.parseDirectoryRecursiveForAll(FileUtils.FILTERED_DIRECTORY));
		allFiles.addAll(FileUtilsLegacy.parseDirectoryRecursiveForAll(FileUtilsLegacy.TEMPORARY_DIRECTORY));
		allFiles.addAll(FileUtilsLegacy.parseDirectoryRecursiveForAll(FileUtils.REPORTS_DIRECTORY));
		allFiles.addAll(FileUtilsLegacy.parseDirectoryRecursiveForAll(FileUtils.PROCESSING_DIRECTORY));
		allFiles.addAll(FileUtilsLegacy.parseDirectoryRecursiveForAll(FileUtils.PREVIEWS_DIRECTORY));
		allFiles.addAll(FileUtilsLegacy.parseDirectoryRecursiveForAll(FileUtils.RESIZED_DIRECTORY));
		allFiles.addAll(FileUtilsLegacy.parseDirectoryRecursiveForAll(FileUtils.REPORT_EXTRACTS_DIRECTORY));
		allFiles.addAll(FileUtilsLegacy.parseDirectoryRecursiveForAll(FileUtils.REDACT_DIRECTORY));
		allFiles.addAll(FileUtilsLegacy.parseDirectoryRecursiveForAll(FileUtils.MERGED_DIRECTORY));
		allFiles.addAll(FileUtilsLegacy.parseDirectoryRecursiveForAll(FileUtils.NOTES_DIRECTORY));
		
		//ProgressBundle deleteProgressBundle = ProgressUtils.getProgressBundle("Deleting Data...", allFiles.size() + 1);
		deleteProgressBundle.progressBar.setIndeterminate(true);
		deleteProgressBundle.progressBar.setMaximum(allFiles.size() + 1);
		deleteProgressBundle.progressBar.setValue(0);
		deleteProgressBundle.progressBar.repaint();
		
		File toDelete;
		BasicFileAttributes attributes;
		Date creationDate;
		
		int progressValue = 0;
		
		for(String filename : allFiles) {
			toDelete = new File(filename);
			
			try {
				attributes = Files.readAttributes(toDelete.toPath(), BasicFileAttributes.class);
				creationDate = new Date(attributes.creationTime().to(TimeUnit.MILLISECONDS));
				
				if(creationDate.after(removalStartDate) == true) {
					FileUtils.deleteFile(toDelete);
					
					progressValue++;
					deleteProgressBundle.progressBar.setValue(progressValue);
					deleteProgressBundle.progressBar.repaint();
				}
			} catch (IOException ioException) {
				//ioException.printStackTrace();
			}	
		}
		
		FileUtils.createDirectory(FileUtils.REPORT_EXTRACTS_TEMPORARY_DIRECTORY);
		
		deleteProgressBundle.frame.dispose();
	}
	
}

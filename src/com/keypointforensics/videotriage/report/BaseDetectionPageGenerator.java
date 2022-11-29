package com.keypointforensics.videotriage.report;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import com.keypointforensics.videotriage.blob.context.BlobContextEntry;
import com.keypointforensics.videotriage.blob.context.BlobContextListParser;
import com.keypointforensics.videotriage.legacy.FileUtilsLegacy;
import com.keypointforensics.videotriage.list.SortedList;
import com.keypointforensics.videotriage.util.FileUtils;

public abstract class BaseDetectionPageGenerator {

	protected final DateFormat VIDEO_CREATION_DATE_FORMATTER = new SimpleDateFormat("MM-dd-yyyy HH-mm-ss");
	
	protected final String                ABSOLUTE_FACE_FOLDER_PATH;
	protected final String                REPORT_ROOT;
	protected final BlobContextListParser BLOB_CONTEXT_LIST_PARSER;
	
	protected String mReportPageHeader;
	protected ArrayList<String> mDetectionEventList;
	protected SortedList<ReportCaptureBundle> mCaptures;
	
	protected HashMap<String, Integer> mExtractCountForVideoMap;
	protected Calendar mGregorianCalendar;
	protected Date mTempDate;
	
	public BaseDetectionPageGenerator(final BlobContextListParser blobContextListParser, final String absoluteFaceFolderPath, final String reportRoot, final String reportPageHeader) {
		ABSOLUTE_FACE_FOLDER_PATH = absoluteFaceFolderPath;
		REPORT_ROOT               = reportRoot;
		BLOB_CONTEXT_LIST_PARSER  = blobContextListParser;
		
		mReportPageHeader = reportPageHeader;
		
		mExtractCountForVideoMap = new HashMap<String, Integer>();
		mGregorianCalendar = new GregorianCalendar();
		
		mDetectionEventList = new ArrayList<String>();
	}
	
	protected abstract String getTableHeaderLabel();
	public abstract String getDetectionPageNameShort();
	
	public String getFlatPageTableData() {
		ArrayList<String> faceExtracts = (ArrayList<String>) FileUtilsLegacy.parseDirectoryRecursiveForAll(ABSOLUTE_FACE_FOLDER_PATH);
		
		createReportCaptureBundles(faceExtracts);
		
		StringBuilder pageBuilder = new StringBuilder(2000);
		
		String faceExtractAbsolutePath;
		String extractFilenameWithoutDetectionHash;
		BlobContextEntry blobContextEntry;
		ReportCaptureBundle currentReportCaptureBundle;
		String shortExtractAbsolutePath;
		
		for(int i = 0; i < mCaptures.size(); ++i) {
			currentReportCaptureBundle = mCaptures.get(i);
			faceExtractAbsolutePath = currentReportCaptureBundle.getCaptureAbsolutePath();
			extractFilenameWithoutDetectionHash = formatDetectionForListParser(FileUtils.getShortFilename(faceExtractAbsolutePath));
			blobContextEntry = BLOB_CONTEXT_LIST_PARSER.getEntryByFilename(extractFilenameWithoutDetectionHash);
		
			shortExtractAbsolutePath = FileUtils.getShortFilename(faceExtractAbsolutePath);
			
			if(blobContextEntry == null && shortExtractAbsolutePath.startsWith("image-") == false) {
				mCaptures.remove(i);
				--i;
			}
		}
		
		pageBuilder.append("<table align=\"center\" class=\"table-title\"><tr><td><h3 align=\"center\">");
		pageBuilder.append(getTableHeaderLabel());
		pageBuilder.append("</h3><p align=\"center\">");
		pageBuilder.append(mCaptures.size());
		pageBuilder.append(" Detection");
		if(mCaptures.size() > 1 || mCaptures.size() == 0) {
			pageBuilder.append("s");
		}
		pageBuilder.append("</p></td></tr></table><table class=\"table-fill\"><thead><tr><th></th><th></th><th></th><th></th></thead><tbody class=\"table-hover\">");
		
		int columnIndex = 0;
		int videoExtractCount;
		String videoFilename;
		
		//for(String faceExtract : faceExtracts) {
		for(ReportCaptureBundle reportCaptureBundle : mCaptures) {
			if(columnIndex == 0) {
				pageBuilder.append("<tr>");
			}
		 
			faceExtractAbsolutePath = reportCaptureBundle.getCaptureAbsolutePath();
			extractFilenameWithoutDetectionHash = formatDetectionForListParser(FileUtils.getShortFilename(faceExtractAbsolutePath));
			blobContextEntry = BLOB_CONTEXT_LIST_PARSER.getEntryByFilename(extractFilenameWithoutDetectionHash);
		
			shortExtractAbsolutePath = FileUtils.getShortFilename(faceExtractAbsolutePath);
			
			if(blobContextEntry == null && shortExtractAbsolutePath.startsWith("image-") == false) {
				continue;
			}
			
			if(blobContextEntry != null) {
				videoFilename = blobContextEntry.videoFilename;
				
				pageBuilder.append("<td class=\"text-left\" width=\"300px\" height=\"300px\" style=\"vertical-align: top\"><a href=\"file:///");
				pageBuilder.append(faceExtractAbsolutePath);
				pageBuilder.append("\"><img src=\"file:///");
				pageBuilder.append(faceExtractAbsolutePath);
				pageBuilder.append("\" width=\"300px\" height=\"300px\"/></a><br><p>");
				pageBuilder.append(videoFilename);
				pageBuilder.append("</p>");
				
				videoExtractCount = getVideoExtractCount(mExtractCountForVideoMap, reportCaptureBundle, blobContextEntry);
	
				if (videoExtractCount > 0) {
					pageBuilder.append("<table class=\"table-fill\"><tbody class=\"table-hover\"><tr><td>Seconds into video: </td><td>");
					pageBuilder.append(reportCaptureBundle.getSecondsIntoVideo());
	
					pageBuilder.append("</td></tr><tr><td>Video time (rounded): </td><td>");
					pageBuilder.append(LocalTime.MIN.plusSeconds(reportCaptureBundle.getSecondsIntoVideoRounded()));
					pageBuilder.append("</td></tr></table>");
				} 
			} else {
				videoFilename = shortExtractAbsolutePath;//FileUtils.getShortFilename(faceExtractAbsolutePath);
				
				pageBuilder.append("<td class=\"text-left\" width=\"300px\" height=\"300px\" style=\"vertical-align: top\"><a href=\"file:///");
				pageBuilder.append(faceExtractAbsolutePath);
				pageBuilder.append("\"><img src=\"file:///");
				pageBuilder.append(faceExtractAbsolutePath);
				pageBuilder.append("\" width=\"300px\" height=\"300px\"/></a><br><p>");
				pageBuilder.append(videoFilename);
				pageBuilder.append("</p>");
				
				/*
				videoExtractCount = getVideoExtractCount(mExtractCountForVideoMap, reportCaptureBundle, blobContextEntry);
	
				if (videoExtractCount > 0) {
					pageBuilder.append("<table class=\"table-fill\"><tbody class=\"table-hover\"><tr><td>Seconds into video: </td><td>");
					pageBuilder.append(reportCaptureBundle.getSecondsIntoVideo());
	
					pageBuilder.append("</td></tr><tr><td>Video time (rounded): </td><td>");
					pageBuilder.append(LocalTime.MIN.plusSeconds(reportCaptureBundle.getSecondsIntoVideoRounded()));
					pageBuilder.append("</td></tr></table>");
				} 
				*/
			}
			
			pageBuilder.append("</td>");
			
			columnIndex++;
			
			if(columnIndex == 4) {
				pageBuilder.append("</tr>");
				columnIndex = 0;
			}
		}
		
		pageBuilder.append("</tbody></table><br>");
		
		return pageBuilder.toString();
	}
	
	public void buildDetectionPage() {
		ArrayList<String> faceExtracts = (ArrayList<String>) FileUtilsLegacy.parseDirectoryRecursiveForAll(ABSOLUTE_FACE_FOLDER_PATH);
		
		createReportCaptureBundles(faceExtracts);
		
		File metadataPageFile = new File(ABSOLUTE_FACE_FOLDER_PATH + getDetectionPageNameShort());
		StringBuilder pageBuilder = new StringBuilder(2000);
		
		mReportPageHeader = mReportPageHeader.substring(0, mReportPageHeader.lastIndexOf("<br>"));
		pageBuilder.append(mReportPageHeader);
		
		String faceExtractAbsolutePath;
		String extractFilenameWithoutDetectionHash;
		BlobContextEntry blobContextEntry;
		ReportCaptureBundle currentReportCaptureBundle;
		String shortExtractAbsolutePath;
		
		for(int i = 0; i < mCaptures.size(); ++i) {
			currentReportCaptureBundle = mCaptures.get(i);
			faceExtractAbsolutePath = currentReportCaptureBundle.getCaptureAbsolutePath();
			extractFilenameWithoutDetectionHash = formatDetectionForListParser(FileUtils.getShortFilename(faceExtractAbsolutePath));
			blobContextEntry = BLOB_CONTEXT_LIST_PARSER.getEntryByFilename(extractFilenameWithoutDetectionHash);
		
			shortExtractAbsolutePath = FileUtils.getShortFilename(faceExtractAbsolutePath);
			
			if(blobContextEntry == null && shortExtractAbsolutePath.startsWith("image-") == false) {
				mCaptures.remove(i);
				--i;
			}
		}
		
		pageBuilder.append("<table align=\"center\" class=\"table-title\"><tr><td><h3 align=\"center\">");
		pageBuilder.append(getTableHeaderLabel());
		pageBuilder.append("</h3><p align=\"center\">");
		pageBuilder.append(mCaptures.size());
		pageBuilder.append(" Detection");
		if(mCaptures.size() > 1 || mCaptures.size() == 0) {
			pageBuilder.append("s");
		}
		pageBuilder.append("</p></td></tr></table><table class=\"table-fill\"><thead><tr><th></th><th></th><th></th><th></th></thead><tbody class=\"table-hover\">");
		
		int columnIndex = 0;
		int videoExtractCount;
		String videoFilename;
		
		//for(String faceExtract : faceExtracts) {
		for(ReportCaptureBundle reportCaptureBundle : mCaptures) {
			if(columnIndex == 0) {
				pageBuilder.append("<tr>");
			}
		 
			faceExtractAbsolutePath = reportCaptureBundle.getCaptureAbsolutePath();
			extractFilenameWithoutDetectionHash = formatDetectionForListParser(FileUtils.getShortFilename(faceExtractAbsolutePath));
			blobContextEntry = BLOB_CONTEXT_LIST_PARSER.getEntryByFilename(extractFilenameWithoutDetectionHash);
		
			shortExtractAbsolutePath = FileUtils.getShortFilename(faceExtractAbsolutePath);
			
			if(blobContextEntry == null && shortExtractAbsolutePath.startsWith("image-") == false) {
				continue;
			}
			
			if(blobContextEntry != null) {
				videoFilename = blobContextEntry.videoFilename;
				
				pageBuilder.append("<td class=\"text-left\" width=\"300px\" height=\"300px\" style=\"vertical-align: top\"><a href=\"file:///");
				pageBuilder.append(faceExtractAbsolutePath);
				pageBuilder.append("\"><img src=\"file:///");
				pageBuilder.append(faceExtractAbsolutePath);
				pageBuilder.append("\" width=\"300px\" height=\"300px\"/></a><br><p>");
				pageBuilder.append(videoFilename);
				pageBuilder.append("</p>");
				
				videoExtractCount = getVideoExtractCount(mExtractCountForVideoMap, reportCaptureBundle, blobContextEntry);
	
				if (videoExtractCount > 0) {
					pageBuilder.append("<table class=\"table-fill\"><tbody class=\"table-hover\"><tr><td>Seconds into video: </td><td>");
					pageBuilder.append(reportCaptureBundle.getSecondsIntoVideo());
	
					pageBuilder.append("</td></tr><tr><td>Video time (rounded): </td><td>");
					pageBuilder.append(LocalTime.MIN.plusSeconds(reportCaptureBundle.getSecondsIntoVideoRounded()));
					pageBuilder.append("</td></tr></table>");
				} 
			} else {
				videoFilename = shortExtractAbsolutePath;//FileUtils.getShortFilename(faceExtractAbsolutePath);
				
				pageBuilder.append("<td class=\"text-left\" width=\"300px\" height=\"300px\" style=\"vertical-align: top\"><a href=\"file:///");
				pageBuilder.append(faceExtractAbsolutePath);
				pageBuilder.append("\"><img src=\"file:///");
				pageBuilder.append(faceExtractAbsolutePath);
				pageBuilder.append("\" width=\"300px\" height=\"300px\"/></a><br><p>");
				pageBuilder.append(videoFilename);
				pageBuilder.append("</p>");
				
				/*
				videoExtractCount = getVideoExtractCount(mExtractCountForVideoMap, reportCaptureBundle, blobContextEntry);
	
				if (videoExtractCount > 0) {
					pageBuilder.append("<table class=\"table-fill\"><tbody class=\"table-hover\"><tr><td>Seconds into video: </td><td>");
					pageBuilder.append(reportCaptureBundle.getSecondsIntoVideo());
	
					pageBuilder.append("</td></tr><tr><td>Video time (rounded): </td><td>");
					pageBuilder.append(LocalTime.MIN.plusSeconds(reportCaptureBundle.getSecondsIntoVideoRounded()));
					pageBuilder.append("</td></tr></table>");
				} 
				*/
			}
			
			pageBuilder.append("</td>");
			
			columnIndex++;
			
			if(columnIndex == 4) {
				pageBuilder.append("</tr>");
				columnIndex = 0;
			}
		}
		
		pageBuilder.append("</tbody></table><br><div align=\"center\"><a href=\"");
		pageBuilder.append(REPORT_ROOT);
		pageBuilder.append("\">Home</a></div></body><html>");
		
		PrintWriter pageWriter = null;
		
		try {
			pageWriter = new PrintWriter(metadataPageFile);
			
			pageWriter.append(pageBuilder.toString());
			
			pageWriter.flush();
			pageWriter.close();
		} catch (FileNotFoundException e) {
			//e.printStackTrace();
		}
	}
		
	public ArrayList<String> getDetectionEventList() {
		return mDetectionEventList;
	}
	
	protected SortedList<ReportCaptureBundle> getCaptureFilenamesSorted(ArrayList<String> faceExtracts) {
		SortedList<ReportCaptureBundle> reportCaptureBundles = new SortedList<ReportCaptureBundle>();
		
		for(String absoluteCaptureFilename : faceExtracts) {
			reportCaptureBundles.add(new ReportCaptureBundle(absoluteCaptureFilename));
		}
		
		return reportCaptureBundles;
	}
	
	protected String formatDetectionForListParser(String detectionNameShort) {
		String withoutExtension = detectionNameShort.substring(0, detectionNameShort.lastIndexOf("_"));
		withoutExtension += detectionNameShort.substring(detectionNameShort.lastIndexOf("."), detectionNameShort.length());
		
		return withoutExtension;
	}
	
	protected void createReportCaptureBundles(ArrayList<String> faceExtracts) {
		mCaptures = getCaptureFilenamesSorted(faceExtracts);
		
		double secondsIntoVideo;
		String shortExtractFilenameLowerCase;
		String modifiedDateString;
		BlobContextEntry blobContextEntry;
		
		for (ReportCaptureBundle capture : mCaptures) {
			shortExtractFilenameLowerCase = capture.getCaptureAbsolutePath().toLowerCase();

			if (shortExtractFilenameLowerCase.contains(File.separator) == true) {
				shortExtractFilenameLowerCase = shortExtractFilenameLowerCase.substring(
						shortExtractFilenameLowerCase.lastIndexOf(File.separator) + 1,
						shortExtractFilenameLowerCase.length());
			}

			//capture.setShortExtractFilenameLowerCase(shortExtractFilenameLowerCase);
			shortExtractFilenameLowerCase = formatDetectionForListParser(shortExtractFilenameLowerCase);
			capture.setShortExtractFilenameLowerCase(shortExtractFilenameLowerCase);
			
			blobContextEntry = BLOB_CONTEXT_LIST_PARSER.getEntryByFilename(shortExtractFilenameLowerCase);
			
			if(blobContextEntry == null) {
				continue;
			}
			
			secondsIntoVideo = ((double) blobContextEntry.frameIndex
					/ (double) blobContextEntry.framesPerSecondTarget);

			capture.setSecondsIntoVideo(secondsIntoVideo);

			try {
				mTempDate = VIDEO_CREATION_DATE_FORMATTER.parse(capture.getCaptureEventDate());
			} catch (ParseException parseException) {
				// parseException.printStackTrace();
			}

			if(mTempDate != null) {
				mGregorianCalendar.setTime(mTempDate);
				mGregorianCalendar.add(Calendar.SECOND, (int) Math.round(secondsIntoVideo));
				mTempDate = mGregorianCalendar.getTime();
	
				modifiedDateString = VIDEO_CREATION_DATE_FORMATTER.format(mTempDate).toString();
			
				capture.parseDateString(modifiedDateString);
				capture.setCaptureEventDate(modifiedDateString);
			} else {
				capture.setCaptureEventDate("No Date Recorded");
			}
		}
		
		Collections.sort(mCaptures);
		
		for(ReportCaptureBundle sortedCapture : mCaptures) {
			mDetectionEventList.add(sortedCapture.getCaptureEventDate());
		}
	}
	
	protected int getVideoExtractCount(final HashMap<String, Integer> extractCountForVideoMap, final ReportCaptureBundle reportCaptureBundle, final BlobContextEntry blobContextEntry) {
		int videoExtractCount = -1;
		if(blobContextEntry.shortExtractFrameIndex.contains("http") == true) {
			extractCountForVideoMap.put(blobContextEntry.videoFilename, -1);
		} else if (extractCountForVideoMap.containsKey(blobContextEntry.videoFilename) == false) {
			videoExtractCount = new File(blobContextEntry.shortExtractFrameIndex).list().length;

			extractCountForVideoMap.put(blobContextEntry.videoFilename, videoExtractCount);
		} else {
			videoExtractCount = extractCountForVideoMap.get(blobContextEntry.videoFilename);
		}

		return videoExtractCount;
	}
	
}

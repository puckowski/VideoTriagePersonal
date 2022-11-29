package com.keypointforensics.videotriage.report;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map.Entry;

import com.keypointforensics.videotriage.legacy.WindowsVideoFrameExtractorLegacy;
import com.keypointforensics.videotriage.thread.CreateSingleVideoPreviewImageThread;
import com.keypointforensics.videotriage.util.DimenUtils;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ImageUtils;

public class VideoMetadataPageGenerator {

	private final int NUMBER_OF_PREVIEW_IMAGES    = 9;
	private final int PREVIEW_IMAGE_WIDTH_PIXELS  = 640;
	private final int PREVIEW_IMAGE_HEIGHT_PIXELS = 640;
	
	private final String ABSOLUTE_METADATA_FOLDER_PATH;
	private final String REPORT_ROOT;
	private final String REPORT_PAGE_HEADER;
	
	private HashMap<String, String> mVideoMetadataPageMap;
	
	private int mVideoIndex;
	
	private String mMetadataPageName;
	
	public VideoMetadataPageGenerator(final String absoluteMetadataFolderPath, final String reportRoot, final String reportPageHeader) {
		ABSOLUTE_METADATA_FOLDER_PATH = absoluteMetadataFolderPath;
		REPORT_ROOT                   = reportRoot;
		REPORT_PAGE_HEADER            = reportPageHeader;
		
		mVideoMetadataPageMap = new HashMap<String, String>();
		
		mVideoIndex = 1;
	}
	
	public String getMetadataPageNameForAbsoluteFilename(final String absoluteVideoFilename) {
		if(mVideoMetadataPageMap.containsKey(absoluteVideoFilename) == false) {
			return "metadata.html";
		} else {
			return mVideoMetadataPageMap.get(absoluteVideoFilename);
		}
	}
	
	public void addAbsoluteVideoFilename(final String absoluteVideoFilename) {
		mVideoMetadataPageMap.put(absoluteVideoFilename, getMetadataPageNameForAbsoluteFilename(absoluteVideoFilename));
	}
	
	public String getMetadataPageName() {
		return mMetadataPageName;
	}
	
	public void buildMetadataPage() {
		mMetadataPageName = ABSOLUTE_METADATA_FOLDER_PATH + "metadata.html";
		File metadataPageFile = new File(mMetadataPageName);
		StringBuilder pageBuilder = new StringBuilder(2000);
		
		pageBuilder.append(REPORT_PAGE_HEADER);
		
		pageBuilder.append("<table class=\"table-fill\"><thead><tr><th class=\"text-left\">ID</th><th class=\"text-left\">Video Details</th><th class=\"text-left\">Preview</th></tr></thead><tbody class=\"table-hover\">");
				
		for(Entry<String, String> videoEntry : mVideoMetadataPageMap.entrySet()) {
			pageBuilder.append("<tr class=\"outer\"><td class=\"text-left\" style=\"vertical-align: top\">");
			pageBuilder.append(mVideoIndex);
			pageBuilder.append("</td>");

			mVideoIndex++;
			
			pageBuilder.append("<td class=\"text-left\">");
			pageBuilder.append("<table><tr><td>Source: </td><td>");
			pageBuilder.append(videoEntry.getKey());
			pageBuilder.append("</td></tr></table>");
			
			pageBuilder.append("<br><p>");
			
			if(videoEntry.getKey().contains("http") == false) {
				pageBuilder.append(WindowsVideoFrameExtractorLegacy.getVideoFileInformation(videoEntry.getKey()).replaceAll("\n", "<br>"));
			} else {
				pageBuilder.append(videoEntry.getKey());
			}
			
			pageBuilder.append("</td>");

			//final String videoFilePath = WindowsVideoFrameExtractorLegacy.checkExtractedPreviewFrame(videoEntry.getKey());
			
			CreateSingleVideoPreviewImageThread createSingleVideoPreviewImageThread = new CreateSingleVideoPreviewImageThread(videoEntry.getKey(), NUMBER_OF_PREVIEW_IMAGES);
			createSingleVideoPreviewImageThread.start();
			
			try {
				createSingleVideoPreviewImageThread.join();
			} catch (InterruptedException interruptedException) {
				//interruptedException.printStackTrace();
			}
			
			String videoPreviewFilePath = createSingleVideoPreviewImageThread.getPreviewImageAbsolutePath();
	        
			if(videoPreviewFilePath == null) {
				videoPreviewFilePath = ImageUtils.NO_PREVIEW_AVAILABLE_IMAGE_PATH;
			}
			
			String localVideoPreviewFilePath = FileUtils.getShortFilename(videoPreviewFilePath);
			
			try {
				Files.copy(Paths.get(videoPreviewFilePath),
						Paths.get(ABSOLUTE_METADATA_FOLDER_PATH + File.separator + localVideoPreviewFilePath), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException ioException) {
				//ioException.printStackTrace();
			}
			
			BufferedImage videoPreviewImage = ImageUtils.loadBufferedImage(videoPreviewFilePath);
			Dimension scaledDimension = DimenUtils.getScaledDimension(new Dimension(videoPreviewImage.getWidth(), videoPreviewImage.getHeight()), new Dimension(PREVIEW_IMAGE_WIDTH_PIXELS, PREVIEW_IMAGE_HEIGHT_PIXELS));
			
	        if(videoPreviewFilePath != null) {
	        	pageBuilder.append("</p></td><td class=\"text-left\" width=\"" + scaledDimension.getWidth() + "px\" height=\"" + scaledDimension.getHeight() + "px\" style=\"vertical-align: top\"><a href=\"");
				pageBuilder.append(localVideoPreviewFilePath);
				pageBuilder.append("\"><img src=\"");
				pageBuilder.append(localVideoPreviewFilePath);
				pageBuilder.append("\" width=\"" + scaledDimension.getWidth() + "px\" height=\"" + scaledDimension.getHeight() + "px\"/></a></td></tr>");
	        } else {
	        	
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
	
	public String getEmbeddableMetadataPage() {
		StringBuilder pageBuilder = new StringBuilder(2000);

		pageBuilder.append("<table class=\"table-fill\"><thead><tr><th class=\"text-left\">ID</th><th class=\"text-left\">Video Details</th><th class=\"text-left\">Preview</th></tr></thead><tbody class=\"table-hover\">");
				
		for(Entry<String, String> videoEntry : mVideoMetadataPageMap.entrySet()) {
			pageBuilder.append("<tr class=\"outer\"><td class=\"text-left\" style=\"vertical-align: top\">");
			pageBuilder.append(mVideoIndex);
			pageBuilder.append("</td>");

			mVideoIndex++;
			
			pageBuilder.append("<td class=\"text-left\">");
			pageBuilder.append("<table><tr><td>Source: </td><td>");
			pageBuilder.append(videoEntry.getKey());
			pageBuilder.append("</td></tr></table>");
			
			pageBuilder.append("<br><p>");
			
			if(videoEntry.getKey().contains("http") == false) {
				pageBuilder.append(WindowsVideoFrameExtractorLegacy.getVideoFileInformation(videoEntry.getKey()).replaceAll("\n", "<br>"));
			} else {
				pageBuilder.append(videoEntry.getKey());
			}
			
			pageBuilder.append("</td>");

			//final String videoFilePath = WindowsVideoFrameExtractorLegacy.checkExtractedPreviewFrame(videoEntry.getKey());
			
			CreateSingleVideoPreviewImageThread createSingleVideoPreviewImageThread = new CreateSingleVideoPreviewImageThread(videoEntry.getKey(), NUMBER_OF_PREVIEW_IMAGES);
			createSingleVideoPreviewImageThread.start();
			
			try {
				createSingleVideoPreviewImageThread.join();
			} catch (InterruptedException interruptedException) {
				//interruptedException.printStackTrace();
			}
			
			String videoPreviewFilePath = createSingleVideoPreviewImageThread.getPreviewImageAbsolutePath();
	        
			if(videoPreviewFilePath == null) {
				videoPreviewFilePath = ImageUtils.NO_PREVIEW_AVAILABLE_IMAGE_PATH;
			}
			
			String localVideoPreviewFilePath = FileUtils.getShortFilename(videoPreviewFilePath);
			
			try {
				Files.copy(Paths.get(videoPreviewFilePath),
						Paths.get(ABSOLUTE_METADATA_FOLDER_PATH + File.separator + localVideoPreviewFilePath), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException ioException) {
				//ioException.printStackTrace();
			}
			
			BufferedImage videoPreviewImage = ImageUtils.loadBufferedImage(videoPreviewFilePath);
			Dimension scaledDimension = DimenUtils.getScaledDimension(new Dimension(videoPreviewImage.getWidth(), videoPreviewImage.getHeight()), new Dimension(PREVIEW_IMAGE_WIDTH_PIXELS, PREVIEW_IMAGE_HEIGHT_PIXELS));
			
	        if(videoPreviewFilePath != null) {
	        	pageBuilder.append("</p></td><td class=\"text-left\" width=\"" + scaledDimension.getWidth() + "px\" height=\"" + scaledDimension.getHeight() + "px\" style=\"vertical-align: top\"><a href=\"");
				pageBuilder.append(localVideoPreviewFilePath);
				pageBuilder.append("\"><img src=\"");
				pageBuilder.append(localVideoPreviewFilePath);
				pageBuilder.append("\" width=\"" + scaledDimension.getWidth() + "px\" height=\"" + scaledDimension.getHeight() + "px\"/></a></td></tr>");
	        } else {
	        	
	        }
		}
		
		pageBuilder.append("</tbody></table><br>");
		
		return pageBuilder.toString();
	}
	
	/*
	public void buildMetadataPages() {
		for(Entry<String, String> videoEntry : mVideoMetadataPageMap.entrySet()) {
			File metadataPageFile = new File(ABSOLUTE_METADATA_FOLDER_PATH + videoEntry.getValue());
			metadataPageFile.cr
		}
	}
	*/
}

package com.keypointforensics.videotriage.legacy;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.keypointforensics.videotriage.progress.ProgressBundle;
import com.keypointforensics.videotriage.staticparams.LocalFileRuntimeParams;
import com.keypointforensics.videotriage.thread.CorrectKeyFrameExtractThread;
import com.keypointforensics.videotriage.thread.ExtractKeyframeThread;
import com.keypointforensics.videotriage.thread.ExtractVideoFrameThread;
import com.keypointforensics.videotriage.thread.ExtractVideoPreviewFrameThread;
import com.keypointforensics.videotriage.util.ArchitectureUtils;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ProgressUtils;

public class WindowsVideoFrameExtractorLegacy {

	/*
	 * Author: Daniel Puckowski
	 */
	
	private static final String KEYFRAME_TEMPORARY_EXTENSION = "_keyframe.tmp";
	
	public static final String KEYFRAME_NOTICE_FILENAME_EXTENSION = "_key";
	
	public static String FFPROBE_EXECUTABLE_NAME;
	public static String FFMPEG_EXECUTABLE_NAME;
	public static String FFMPEG4_EXECUTABLE_NAME;
	
	public static void setSupportingBinaryNames() {
		boolean is64Bit = ArchitectureUtils.is64Bit();
				
		if(is64Bit == true) {
			FFPROBE_EXECUTABLE_NAME = "w64p.exe";
			FFMPEG_EXECUTABLE_NAME  = "w64m.exe";
			FFMPEG4_EXECUTABLE_NAME = "w64m4.exe";
		}
		else {
			FFPROBE_EXECUTABLE_NAME = "w32p.exe";
			FFMPEG_EXECUTABLE_NAME  = "w32m.exe";
			FFMPEG4_EXECUTABLE_NAME = "w32m4.exe";
		}
	}
	
	public static ExtractVideoFrameBlob extractVideoFrames(String filename, boolean exhaustiveSearchEnabled, int framesPerSecond, int numberOfZerosPadding) {		
		ExtractVideoFrameBlob blob = null;
		
		if(filename == null || filename.isEmpty() == true) {			
			return blob;
		}
		
		framesPerSecond = LocalFileRuntimeParams.getGlobalFramesPerSecondTarget();
		
		if(numberOfZerosPadding <= 0) {			
			numberOfZerosPadding = 7;
		}

		String oldFilename = filename;

		if (filename.contains("\\") == true) { 
			filename = filename.substring(filename.lastIndexOf("\\") + 1,
					filename.length());
		}
	
		String captureFolderName = FileUtilsLegacy.VIDEO_FRAME_EXTRACTS_DIRECTORY
				+ "video_frames_";
		
		if(exhaustiveSearchEnabled == true) {
			captureFolderName += String.valueOf(framesPerSecond);
		} else {
			captureFolderName += "1";
		}
	
		captureFolderName += "_" + filename;
		
		if(FileUtilsLegacy.isDirectoryExist(captureFolderName) == true) {
			if(FileUtils.getLastModifiedMilliseconds(filename) > FileUtils.getLastModifiedMilliseconds(captureFolderName) == false) {
				blob = new ExtractVideoFrameBlob(null, captureFolderName);
			
				return blob;
			}
		}
		
		FileUtilsLegacy.createDirectory(captureFolderName);
		
		filename = oldFilename;

		String command = null;
		
		ExtractVideoFrameThread extractVideoFrameThread = null;
		
		if(exhaustiveSearchEnabled == true) {
			command = "\"" + FileUtilsLegacy.BINARIES_DIRECTORY
				+ FFMPEG_EXECUTABLE_NAME + "\" -threads 2 " 
				+ "-i " + "\"" + filename + "\" "
				+ "-r " + framesPerSecond + " "
				+ "-f image2 \"image-%" + String.valueOf(numberOfZerosPadding)
				+ "d.jpeg\"";
			
			extractVideoFrameThread = new ExtractVideoFrameThread(captureFolderName, command);
			extractVideoFrameThread.start();
		} else {
			command = "\"" + FileUtilsLegacy.BINARIES_DIRECTORY
				+ FFMPEG_EXECUTABLE_NAME + "\" -threads 2 " 
				+ "-discard nokey -i " + "\"" + filename + "\" "
				+ "-q:v 2 -vf select=\"eq(pict_type\\,PICT_TYPE_I)\" -vsync 0 \"image-%" + String.valueOf(numberOfZerosPadding)
				+ "d.jpeg\" -loglevel debug 2>&1| for /f \"tokens=4,8,9 delims=. \" %d in ('findstr \"pict_type:I\"') do echo %d %e.%f>>\""
				+ FileUtils.PROCESSING_DIRECTORY + FileUtils.getShortFilename(captureFolderName) + CorrectKeyFrameExtractThread.KEYFRAME_TIME_FILENAME_EXTENSION + "\"";
			
			File deleteOnTerminationFile = new File(FileUtils.TEMPORARY_DIRECTORY + FileUtils.getShortFilename(filename) + KEYFRAME_TEMPORARY_EXTENSION);
			try {
				deleteOnTerminationFile.createNewFile();
			} catch (IOException ioException) {
				//ioException.printStackTrace();
			}
			
			extractVideoFrameThread = new ExtractVideoFrameThread(captureFolderName, command, deleteOnTerminationFile);
			extractVideoFrameThread.start();
		}
		
		blob = new ExtractVideoFrameBlob(extractVideoFrameThread, captureFolderName);
	
		return blob;
	}
	
	public static ExtractVideoFrameBlob extractVideoFramesWithoutGlobalFrameTarget(String filename, boolean exhaustiveSearchEnabled, int framesPerSecond, int numberOfZerosPadding) {		
		ExtractVideoFrameBlob blob = null;
		
		if(filename == null || filename.isEmpty() == true) {			
			return blob;
		}
				
		if(numberOfZerosPadding <= 0) {			
			numberOfZerosPadding = 7;
		}

		String oldFilename = filename;

		if (filename.contains("\\") == true) { 
			filename = filename.substring(filename.lastIndexOf("\\") + 1,
					filename.length());
		}
	
		String captureFolderName = FileUtilsLegacy.VIDEO_FRAME_EXTRACTS_DIRECTORY
				+ "video_frames_";
		
		if(exhaustiveSearchEnabled == true) {
			captureFolderName += String.valueOf(framesPerSecond);
		} else {
			captureFolderName += "1";
		}
	
		captureFolderName += "_" + filename;
		
		if(FileUtilsLegacy.isDirectoryExist(captureFolderName) == true) {
			if(FileUtils.getLastModifiedMilliseconds(filename) > FileUtils.getLastModifiedMilliseconds(captureFolderName) == false) {
				blob = new ExtractVideoFrameBlob(null, captureFolderName);
			
				return blob;
			}
		}
		
		FileUtilsLegacy.createDirectory(captureFolderName);
		
		filename = oldFilename;

		String command = null;
		
		ExtractVideoFrameThread extractVideoFrameThread = null;
		
		if(exhaustiveSearchEnabled == true) {
			command = "\"" + FileUtilsLegacy.BINARIES_DIRECTORY
				+ FFMPEG_EXECUTABLE_NAME + "\" -threads 2 " 
				+ "-i " + "\"" + filename + "\" "
				+ "-r " + framesPerSecond + " "
				+ "-f image2 \"image-%" + String.valueOf(numberOfZerosPadding)
				+ "d.jpeg\"";
			
			extractVideoFrameThread = new ExtractVideoFrameThread(captureFolderName, command);
			extractVideoFrameThread.start();
		} else {
			command = "\"" + FileUtilsLegacy.BINARIES_DIRECTORY
				+ FFMPEG_EXECUTABLE_NAME + "\" -threads 2 " 
				+ "-discard nokey -i " + "\"" + filename + "\" "
				+ "-q:v 2 -vf select=\"eq(pict_type\\,PICT_TYPE_I)\" -vsync 0 \"image-%" + String.valueOf(numberOfZerosPadding)
				+ "d.jpeg\" -loglevel debug 2>&1| for /f \"tokens=4,8,9 delims=. \" %d in ('findstr \"pict_type:I\"') do echo %d %e.%f>>\""
				+ FileUtils.PROCESSING_DIRECTORY + FileUtils.getShortFilename(captureFolderName) + CorrectKeyFrameExtractThread.KEYFRAME_TIME_FILENAME_EXTENSION + "\"";
			
			File deleteOnTerminationFile = new File(FileUtils.TEMPORARY_DIRECTORY + FileUtils.getShortFilename(filename) + KEYFRAME_TEMPORARY_EXTENSION);
			try {
				deleteOnTerminationFile.createNewFile();
			} catch (IOException ioException) {
				//ioException.printStackTrace();
			}
			
			extractVideoFrameThread = new ExtractVideoFrameThread(captureFolderName, command, deleteOnTerminationFile);
			extractVideoFrameThread.start();
		}
		
		blob = new ExtractVideoFrameBlob(extractVideoFrameThread, captureFolderName);
	
		return blob;
	}
	
	public static boolean isKeyframeDirectory(final String captureFolderName) {		
		String captureDirectory = FileUtils.getLastDirectory(captureFolderName);
		
		File keyNoticeFile = new File(FileUtils.PROCESSING_DIRECTORY + captureDirectory + KEYFRAME_NOTICE_FILENAME_EXTENSION);
				
		if(keyNoticeFile.exists() == true) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean containsTemporaryKeyFrameFiles() {
		ArrayList<String> temporaryFiles = (ArrayList<String>) FileUtilsLegacy.parseDirectory(FileUtils.TEMPORARY_DIRECTORY);
		
		for(String temporaryFile : temporaryFiles) {
			if(temporaryFile.endsWith(KEYFRAME_TEMPORARY_EXTENSION) == true) {
				return true;
			}
		}
		
		return false;
	}
	
	public static String getVideoFileInformation(String absoluteFilePath) {
		String command = "\"" + FileUtilsLegacy.BINARIES_DIRECTORY + FFPROBE_EXECUTABLE_NAME + "\"" 
			+ " -v error -show_format -show_streams \"" + absoluteFilePath + "\"";
				
		String captureFolderName = absoluteFilePath.substring(0, absoluteFilePath.lastIndexOf("\\"));
		String commandOutput = "";
				
		try {
			commandOutput = changeDirectoryAndExecuteForOutput(captureFolderName, command);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (commandOutput == "ERROR") {
			return "Could not get additional information for video file.";
		}
		
		return commandOutput;
	}
	
	public static double getVideoDurationInSeconds(String absoluteFilePath) {
		String command = "\"" + FileUtilsLegacy.BINARIES_DIRECTORY + FFPROBE_EXECUTABLE_NAME + "\" " //windows64_ffprobe.exe\"" + " "
			+ "-v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 \""
			+ absoluteFilePath + "\"";
			
		String captureFolderName;
		String commandOutput = "0";

		if(absoluteFilePath.contains("\\"))
		{
			captureFolderName = absoluteFilePath.substring(0, absoluteFilePath.lastIndexOf("\\"));
		}
		else
		{				
			System.err.println("Fatal error in getVideoFrameCount()");
				
			return 0;
		}
					
		try {
			commandOutput = changeDirectoryAndExecuteForOutput(captureFolderName, command);
		} catch (IOException e) {
			e.printStackTrace();
		}
			
		if (commandOutput == "ERROR") {
			return 0;
		}
			
		return Double.parseDouble(commandOutput);
	}
	
	private static String parseVideoCreationDateString(String videoCreationDate) {
		videoCreationDate = videoCreationDate.substring(videoCreationDate.indexOf("creation_time=") + 14, videoCreationDate.length());
		videoCreationDate = videoCreationDate.substring(0, videoCreationDate.indexOf("[/"));
		
		int indexOfFirstDash = videoCreationDate.indexOf("-");
		int indexOfFirstSpace = videoCreationDate.indexOf(" ");
		
		String year = videoCreationDate.substring(0, indexOfFirstDash);
		String monthAndDay = videoCreationDate.substring(indexOfFirstDash + 1, indexOfFirstSpace);
		monthAndDay += "-" + year;
		
		videoCreationDate = monthAndDay + videoCreationDate.substring(indexOfFirstSpace, videoCreationDate.length()).replace(":", "-");
		videoCreationDate = videoCreationDate.substring(0, videoCreationDate.length() - 1);
		
		return videoCreationDate;
	}
	
	public static String getVideoCreationDateMetadata(String absoluteFilePath) {
		String command = "\"" + FileUtilsLegacy.BINARIES_DIRECTORY + FFPROBE_EXECUTABLE_NAME + "\" "
			+ "-v error \"" + absoluteFilePath + "\" -show_entries stream=index,codec_type:stream_tags=creation_time";
		
		String commandOutput = null;
				
		try {
			commandOutput = executeForOutput(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		if (commandOutput != null && commandOutput.contains("creation_time") == false) {
			return null;
		}
		
		return parseVideoCreationDateString(commandOutput);
	}
	
	public static int getVideoFrameCount(String absoluteFilePath) {
		String command = "\"" + FileUtilsLegacy.BINARIES_DIRECTORY + FFPROBE_EXECUTABLE_NAME + "\" "
			+ "-v error -count_frames -select_streams v:0 -show_entries stream=nb_read_frames -of default=nokey=1:noprint_wrappers=1 \""
			+ absoluteFilePath + "\"";
		
		String captureFolderName;
		String commandOutput = "0";

		if(absoluteFilePath.contains("\\")) {
			captureFolderName = absoluteFilePath.substring(0, absoluteFilePath.lastIndexOf("\\"));
		}
		else {			
			System.err.println("Fatal error in getVideoFrameCount()");
			
			return 0;
		}
				
		try {
			commandOutput = changeDirectoryAndExecuteForOutput(captureFolderName, command);
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		if (commandOutput == "ERROR") {
			return 0;
		}
		
		return Integer.parseInt(commandOutput);
	}
	
	private static String executeForOutput(String command) throws IOException {
		if(command == null || command.isEmpty() == true) {		
			return "";
		}
			
		ProcessBuilder processBuilder = new ProcessBuilder(command.split("\\s"));
		processBuilder.redirectErrorStream(true);
		
		Process process = processBuilder.start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
			
		StringBuilder commandOutput = new StringBuilder(2000);
		boolean firstAppend = true;
			
		while (true) {
			line = reader.readLine();
				
			if (line == null) {
				break;
			}

			if(firstAppend) {
				commandOutput.append(line);
				firstAppend = false;
			}
			else {
				commandOutput.append(line);
				commandOutput.append("\n");
			}
		}
			
		return commandOutput.toString();
	}
	
	private static String changeDirectoryAndExecuteForOutput(String newDirectory, String command) throws IOException {
		if(newDirectory == null || newDirectory.isEmpty() == true) {			
			return "";
		}
			
		if(command == null || command.isEmpty() == true) {		
			return "";
		}
			
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.redirectErrorStream(true);
			
		Process process = processBuilder.start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
			
		StringBuilder commandOutput = new StringBuilder(2000);
		boolean firstAppend = true;
			
		while (true) {
			line = reader.readLine();
				
			if (line == null) {
				break;
			}

			if(firstAppend) {
				commandOutput.append(line);
				firstAppend = false;
			}
			else {
				commandOutput.append(line);
				commandOutput.append("\n");
			}
		}
			
		return commandOutput.toString();
	}
	
	public static String checkExtractedPreviewFrame(String filename) {
		String shortFilename = null;
		
		if (filename.contains("\\") == true) {
			int indexLastSlash = filename.lastIndexOf("\\");
			int indexLastPeriod = filename.lastIndexOf(".");
			shortFilename = filename.substring(indexLastSlash + 1, indexLastPeriod);
			shortFilename += "_temp.png";
		} else {
			int indexLastPeriod = filename.lastIndexOf(".");
			shortFilename = filename.substring(0, indexLastPeriod);
			shortFilename += "_temp.png";
		}

		String potentialTemporaryFile = FileUtilsLegacy.TEMPORARY_DIRECTORY + shortFilename;
		
		if(FileUtilsLegacy.isFileExist(potentialTemporaryFile) == true) {
			if(FileUtils.getLastModifiedMilliseconds(filename) > FileUtils.getLastModifiedMilliseconds(potentialTemporaryFile) == true) {
				return null; 
				// Needs refresh
			} else {
				return potentialTemporaryFile;
			}
		}
		else {
			return null;
		}
	}
	
	public static String getShortFilenameForPreviewExtract(final String filename) {
		String shortFilename = null;
		
		if (filename.contains("\\") == true) {
			int indexLastSlash = filename.lastIndexOf("\\");
			int indexLastPeriod = filename.lastIndexOf(".");
			shortFilename = filename.substring(indexLastSlash + 1, indexLastPeriod);
			shortFilename += "_temp.png";
		} else {
			int indexLastPeriod = filename.lastIndexOf(".");
			shortFilename = filename.substring(0, indexLastPeriod);
			shortFilename += "_temp.png";
		}
		
		return shortFilename;
	}
	
	public static String getShortFilenameForPreviewExtractWithoutExtension(final String filename) {
		String shortFilename = null;
		
		if (filename.contains("\\") == true) {
			int indexLastSlash = filename.lastIndexOf("\\");
			int indexLastPeriod = filename.lastIndexOf(".");
			shortFilename = filename.substring(indexLastSlash + 1, indexLastPeriod);
			shortFilename += "_temp";
		} else {
			int indexLastPeriod = filename.lastIndexOf(".");
			shortFilename = filename.substring(0, indexLastPeriod);
			shortFilename += "_temp";
		}
		
		return shortFilename;
	}
	
	public static ArrayList<String> getShortFilenamesForPreviewExtract(final String filename, final int numberOfPreviewExtracts) {
		String shortFilename = null;
		
		if (filename.contains("\\") == true) {
			int indexLastSlash = filename.lastIndexOf("\\");
			int indexLastPeriod = filename.lastIndexOf(".");
			shortFilename = filename.substring(indexLastSlash + 1, indexLastPeriod);
			shortFilename += "_temp";
		} else {
			int indexLastPeriod = filename.lastIndexOf(".");
			shortFilename = filename.substring(0, indexLastPeriod);
			shortFilename += "_temp";
		}
		
		ArrayList<String> shortFilenames = new ArrayList<String>();
		
		for(int i = 1; i <= numberOfPreviewExtracts; ++i) {
			shortFilenames.add(shortFilename + "_" + String.format("%03d", i) + ".png");
		}
		
		return shortFilenames;
	}
	
	public static Thread extractPreviewFrames(final String absoluteVideoFilePath, final int numberOfPreviewExtracts) {
		final int videoFrameCount = getVideoFrameCount(absoluteVideoFilePath);
		
		final int numberOfFramesToSlice = (int) Math.floor((double) videoFrameCount / (double) numberOfPreviewExtracts);
		
		final String command = "\"" + FileUtilsLegacy.BINARIES_DIRECTORY
			+ FFMPEG_EXECUTABLE_NAME + "\" -threads 2 " 
			+ "-i " + "\"" + absoluteVideoFilePath + "\" "
			+ "-vf thumbnail=" + numberOfFramesToSlice + ",setpts=N/TB -r 1 -vframes " + numberOfPreviewExtracts + " \""
			+ getShortFilenameForPreviewExtractWithoutExtension(absoluteVideoFilePath) + "_%03d.png\"";
		
		ProgressBundle progressBundle = ProgressUtils.getProgressBundle("Extracting Images...", numberOfPreviewExtracts);
		
		ExtractVideoPreviewFrameThread extractVideoPreviewFrameThread = new ExtractVideoPreviewFrameThread(command, progressBundle);
		extractVideoPreviewFrameThread.start();
		
		return extractVideoPreviewFrameThread;
	}
	
	public static void extractPreviewFramesJoined(final String absoluteVideoFilePath, final int numberOfPreviewExtracts) throws InterruptedException {
		final int videoFrameCount = getVideoFrameCount(absoluteVideoFilePath);
		
		final int numberOfFramesToSlice = (int) Math.floor((double) videoFrameCount / (double) numberOfPreviewExtracts);
		
		final String command = "\"" + FileUtilsLegacy.BINARIES_DIRECTORY
			+ FFMPEG_EXECUTABLE_NAME + "\" -threads 2 " 
			+ "-i " + "\"" + absoluteVideoFilePath + "\" "
			+ "-vf thumbnail=" + numberOfFramesToSlice + ",setpts=N/TB -r 1 -vframes " + numberOfPreviewExtracts + " \""
			+ getShortFilenameForPreviewExtractWithoutExtension(absoluteVideoFilePath) + "_%03d.png\"";
		
		ProgressBundle progressBundle = ProgressUtils.getProgressBundle("Extracting Images...", numberOfPreviewExtracts);
		
		ExtractVideoPreviewFrameThread extractVideoPreviewFrameThread = new ExtractVideoPreviewFrameThread(command, progressBundle);
		extractVideoPreviewFrameThread.start();
		extractVideoPreviewFrameThread.join();
	}
	
	public static ArrayList<String> extractPreviewFramesJoinedWithAbsoluteList(final String absoluteVideoFilePath, final int numberOfPreviewExtracts) throws InterruptedException {
		final int videoFrameCount = getVideoFrameCount(absoluteVideoFilePath);
		final int numberOfFramesToSlice = (int) Math.floor((double) videoFrameCount / (double) numberOfPreviewExtracts);
		final String shortFilename = getShortFilenameForPreviewExtractWithoutExtension(absoluteVideoFilePath);
		
		final String command = "\"" + FileUtilsLegacy.BINARIES_DIRECTORY
			+ FFMPEG_EXECUTABLE_NAME + "\" -threads 2 " 
			+ "-i " + "\"" + absoluteVideoFilePath + "\" "
			+ "-vf thumbnail=" + numberOfFramesToSlice + ",setpts=N/TB -r 1 -vframes " + numberOfPreviewExtracts + " \""
			+ shortFilename + "_%03d.png\"";

		ProgressBundle progressBundle = ProgressUtils.getProgressBundle("Extracting Images...", numberOfPreviewExtracts);
		
		ExtractVideoPreviewFrameThread extractVideoPreviewFrameThread = new ExtractVideoPreviewFrameThread(command, progressBundle);
		extractVideoPreviewFrameThread.start();
		extractVideoPreviewFrameThread.join();
		
		ArrayList<String> absolutePreviewFilenames = new ArrayList<String>();
		
		for(int i = 1; i <= numberOfPreviewExtracts; ++i) {
			absolutePreviewFilenames.add(FileUtils.TEMPORARY_DIRECTORY + shortFilename + "_" + String.format("%03d", i) + ".png");
		}
		
		return absolutePreviewFilenames;
	}
	
	public static ArrayList<String> extractPreviewFramesJoinedByIndexWithAbsoluteList(final String absoluteVideoFilePath, final int numberOfPreviewExtracts) throws InterruptedException {
		final int videoFrameCount = getVideoFrameCount(absoluteVideoFilePath);
		final int numberOfFramesToSlice = (int) Math.floor((double) videoFrameCount / (double) (numberOfPreviewExtracts + 1));
		final String shortFilename = getShortFilenameForPreviewExtractWithoutExtension(absoluteVideoFilePath);
		
		ArrayList<String> absoluteExtractedFiles = new ArrayList<String>();
		
		String extractedFilename;
		int index = numberOfFramesToSlice;
		File checkExtractFile;
		
		for(int i = 0; i < numberOfPreviewExtracts; ++i) {
			extractedFilename = FileUtils.TEMPORARY_DIRECTORY + shortFilename + "_" + String.format("%03d", i) + ".png";
			absoluteExtractedFiles.add(extractedFilename);
			
			checkExtractFile = new File(extractedFilename);
			
			if(checkExtractFile.exists() == true) {
				//continue;
				checkExtractFile.delete();
			}
			
			extractPreviewFrame(absoluteVideoFilePath, extractedFilename, index);
			index += numberOfFramesToSlice;
		}
		
		return absoluteExtractedFiles;
	}
	
	public static void extractPreviewFrame(String filename, String absoluteOutputPath, int indexToExtract) {		
		String command = "\"" + FileUtilsLegacy.BINARIES_DIRECTORY + FFMPEG_EXECUTABLE_NAME + "\" -threads 2 ";
		command += "-i " + "\"" + filename + "\" ";
		command += "-vf \"select=gte(n\\," + indexToExtract + ")\" -vframes 1 \"" + absoluteOutputPath + "\"";

		try {
			ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", "cd \"" + FileUtilsLegacy.TEMPORARY_DIRECTORY + "\" && dir && " + command);
			processBuilder.redirectErrorStream(true);
			
			Process process = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			
			while (true) {
				line = reader.readLine();

				if (line == null) {
					break;
				}
			}
		} catch (IOException ioException) {

		}
	}
	
	public static String extractPreviewFrame(String filename) {
		String shortFilename = getShortFilenameForPreviewExtract(filename);
		String potentialTemporaryFile = FileUtilsLegacy.TEMPORARY_DIRECTORY + shortFilename;
		
		if(FileUtilsLegacy.isFileExist(potentialTemporaryFile) == true) {
			if(FileUtils.getLastModifiedMilliseconds(filename) > FileUtils.getLastModifiedMilliseconds(potentialTemporaryFile) == true) {
				//return potentialTemporaryFile;
				
				File removeExistingTemporaryFile = new File(potentialTemporaryFile);
				removeExistingTemporaryFile.delete();
			} else {
				return potentialTemporaryFile;
			}
		}
		
		final String videoPreviewFilename = FileUtilsLegacy.TEMPORARY_DIRECTORY + shortFilename;
		
		String command = "\"" + FileUtilsLegacy.BINARIES_DIRECTORY + FFMPEG_EXECUTABLE_NAME + "\" -threads 2 ";
		command += "-i " + "\"" + filename + "\" ";
		command += "-vf \"select=gte(n\\,100)\" -vframes 1 \"" + shortFilename + "\"";

		try {
			ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", "cd \"" + FileUtilsLegacy.TEMPORARY_DIRECTORY + "\" && dir && " + command);
			processBuilder.redirectErrorStream(true);
			
			Process process = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			
			while (true) {
				line = reader.readLine();
				
				if (line == null) {
					break;
				}
			}
		} catch (IOException ioException) {

		}
	
		if (FileUtilsLegacy.isFileExist(videoPreviewFilename) == true) { 
			return videoPreviewFilename;
		} else { 
			return null;
		}
	}
}

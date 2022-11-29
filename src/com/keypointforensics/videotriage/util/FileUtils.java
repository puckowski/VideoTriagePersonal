package com.keypointforensics.videotriage.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.JFileChooser;
import javax.swing.UIManager;

import com.keypointforensics.videotriage.gui.main.FileSelectImagePreviewAccessory;
import com.keypointforensics.videotriage.gui.main.FileSelectVideoPreviewAccessory;

public class FileUtils {

	/*
	 * Author: Daniel Puckowski
	 */
	
	public static final char   FILE_SEPARATOR            = File.separatorChar;
	public static final String ROOT_DIRECTORY            = new File("").getAbsolutePath() + File.separatorChar;
	public static final String RESOURCES_DIRECTORY       = ROOT_DIRECTORY + "resources" + File.separatorChar;
	public static final String LICENSE_DIRECTORY         = RESOURCES_DIRECTORY + "license" + File.separatorChar;
	public static final String CONTEXT_DIRECTORY         = RESOURCES_DIRECTORY + "context" + File.separatorChar;
	public static final String GRAPHICS_DIRECTORY        = RESOURCES_DIRECTORY + "graphics" + File.separatorChar;
	public static final String CAPTURES_DIRECTORY        = RESOURCES_DIRECTORY + "captures" + File.separatorChar;
	//public static final String LOGS_DIRECTORY            = RESOURCES_DIRECTORY + "logs" + File.separatorChar;
	public static final String DATABASE_DIRECTORY        = RESOURCES_DIRECTORY + "databases" + File.separatorChar;
	public static final String REPORTS_DIRECTORY         = RESOURCES_DIRECTORY + "reports" + File.separatorChar;
	public static final String TEMPORARY_DIRECTORY       = RESOURCES_DIRECTORY + "temporary" + File.separatorChar;
	public static final String CHART_DIRECTORY           = RESOURCES_DIRECTORY + "chart" + File.separatorChar;
	public static final String FONT_DIRECTORY            = RESOURCES_DIRECTORY + "font" + File.separatorChar;
	public static final String ENHANCED_DIRECTORY        = RESOURCES_DIRECTORY + "enhanced" + File.separatorChar;
	public static final String SEARCHES_DIRECTORY        = RESOURCES_DIRECTORY + "searches" + File.separatorChar;
	public static final String FILTERED_DIRECTORY        = RESOURCES_DIRECTORY + "filtered" + File.separatorChar;
	public static final String CASCADES_DIRECTORY        = RESOURCES_DIRECTORY + "cascades" + File.separatorChar;
	public static final String DETECTIONS_DIRECTORY      = RESOURCES_DIRECTORY + "detections" + File.separatorChar;
	public static final String EXTRACTS_DIRECTORY        = RESOURCES_DIRECTORY + "extracts" + File.separatorChar;
	public static final String PROCESSING_DIRECTORY      = RESOURCES_DIRECTORY + "processing" + File.separatorChar;
	public static final String PREVIEWS_DIRECTORY        = RESOURCES_DIRECTORY + "previews" + File.separatorChar;
	public static final String RESIZED_DIRECTORY         = RESOURCES_DIRECTORY + "resized" + File.separatorChar;
	public static final String REPORT_EXTRACTS_DIRECTORY = RESOURCES_DIRECTORY + "report_extracts" + File.separatorChar;
	public static final String REDACT_DIRECTORY          = RESOURCES_DIRECTORY + "redact" + File.separatorChar;
	public static final String MERGED_DIRECTORY          = RESOURCES_DIRECTORY + "merged" + File.separatorChar;
	public static final String NOTES_DIRECTORY           = RESOURCES_DIRECTORY + "notes" + File.separatorChar;
	public static final String EXPORTS_DIRECTORY         = RESOURCES_DIRECTORY + "exports" + File.separatorChar;

	public static final String REPORT_EXTRACTS_TEMPORARY_DIRECTORY = REPORT_EXTRACTS_DIRECTORY + "temporary" + File.separatorChar;
		
	public static final String FACES_DIRECTORY       = DETECTIONS_DIRECTORY + "faces" + File.separatorChar;
	public static final String PLATES_DIRECTORY      = DETECTIONS_DIRECTORY + "plates" + File.separatorChar;
	public static final String PEDESTRIANS_DIRECTORY = DETECTIONS_DIRECTORY + "pedestrians" + File.separatorChar;
	public static final String CARS_DIRECTORY        = DETECTIONS_DIRECTORY + "cars" + File.separatorChar;
	public static final String EXPLICIT_DIRECTORY    = DETECTIONS_DIRECTORY + "explicit" + File.separatorChar;
	
	public static final String FILTER_PREVIEW_DIRECTORY = GRAPHICS_DIRECTORY + "filter_previews" + File.separatorChar;
	
	public static boolean isDirectoryEmpty(final String absoluteDirectoryPath) {
		File directory = new File(absoluteDirectoryPath);
		String[] files = directory.list();
			
		if(files == null) {
			return true;
		} else {
			return files.length == 0;
		}
	}
	
	public static boolean isDirectoryEmpty(final File directory) {
		return directory.list().length == 0;
	}
	
	public static long getLastModifiedMilliseconds(final File file) {
		return file.lastModified();
	}
	
	public static long getLastModifiedMilliseconds(final String absoluteFilePath) {
		return new File(absoluteFilePath).lastModified();
	}
	
	public static String getFileDirectory(final String absoluteFilePath) {
		if(absoluteFilePath.contains(File.separator) == true) {
			return absoluteFilePath.substring(0, absoluteFilePath.lastIndexOf(File.separator) + 1);
		} else {
			return absoluteFilePath;
		}
	}
	
	public static String getShortFilename(final String absoluteFilePath) {
		if(absoluteFilePath.contains(File.separator) == true) {
			return absoluteFilePath.substring(absoluteFilePath.lastIndexOf(File.separator) + 1, absoluteFilePath.length());
		} else {
			return absoluteFilePath;
		}
	}
	
	public static String getLastDirectory(String absoluteFilePath) {
		if(absoluteFilePath.endsWith(File.separator) == true) {
			absoluteFilePath = absoluteFilePath.substring(0, absoluteFilePath.length() - File.separator.length());
		}
		
		return absoluteFilePath.substring(absoluteFilePath.lastIndexOf(File.separator) + 1, absoluteFilePath.length());
	}
	
	public static String humanReadableByteCount(long bytes) {
	    int unit = 1000;
	    
	    if (bytes < unit) {
	    	return bytes + " B";
	    }
	    
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = "kMGTPE".charAt(exp-1) + "";
	    
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
	
	public static String humanReadableByteCount(long bytes, boolean si) {
	    int unit = si ? 1000 : 1024;

	    if (bytes < unit) {
	    	return bytes + " B";
	    }
	    
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
	
	public static long getTotalSpace() {
		long totalSpace = 0;
		
		for (Path root : FileSystems.getDefault().getRootDirectories()) {
		    try {
		        FileStore store = Files.getFileStore(root);
		        totalSpace = store.getTotalSpace();
		    } catch (IOException e) {

		    }
		    
		    break;
		}
		
		return totalSpace;
	}
	
	public static long getFolderSize(File folder) {
	    long length = 0;
	    File[] files = folder.listFiles();
	 
	    if(files == null) {
	    	return length;
	    }
	    
	    int count = files.length;
	 
	    for (int i = 0; i < count; i++) {
	        if (files[i].isFile()) {
	            length += files[i].length();
	        }
	        else {
	            length += getFolderSize(files[i]);
	        }
	    }
	    	    
	    return length;
	}
	
	public static boolean isDirectoryExist(final String absoluteDirectoryPath) {
		final File directory = new File(absoluteDirectoryPath);

		if (directory.exists() == true) {
			return true;
		} else {			
			return false;
		}
	}

	public static boolean createDirectory(final String absoluteDirectoryPath) {
		final File directory = new File(absoluteDirectoryPath);

		if (directory.exists() == true) {
			if(directory.isDirectory() == true) {				
				FileUtils.deleteDirectoryContents(directory, true);
			}
		}

		if (directory.exists() == false) {
			return directory.mkdir();
		} else {
			return false;
		}
	}
	
	public static boolean createDirectory(final String absoluteDirectoryPath, boolean deleteExistingContents) {
		final File directory = new File(absoluteDirectoryPath);

		if (directory.exists() == true) {
			if(directory.isDirectory() == true) {
				if(deleteExistingContents == true) {					
					FileUtils.deleteDirectoryContents(directory, true);
				}
			}
		}

		if (directory.exists() == false) {
			return directory.mkdir();
		} else {
			return false;
		}
	}
	
	public static boolean deleteDirectoryContents(final File directory, boolean deleteContainedDirectories) {
		final File[] files = directory.listFiles();
		
		boolean allDeletionsSuccessful = true;

		if(files == null) {
			return deleteFileOrDirectory(directory);
		}
		
		for (File file : files) {
			if(file.isDirectory() == true) {
				boolean result = deleteDirectoryContents(file, true);
				
				if(result == false) {
					allDeletionsSuccessful = false;
				}
				
				result = deleteFileOrDirectory(file);
				
				if(result == false) {
					allDeletionsSuccessful = false;
				}
			}
			else if(deleteFileOrDirectory(file) == false) {
				allDeletionsSuccessful = false;
			}
		}
		
		return allDeletionsSuccessful;
	}
	
	public static boolean deleteFile(final File file) {
		if (file.isFile()) {
			return file.delete();
		} else {
			return false;
		}
	}

	public static boolean deleteFileOrDirectory(final File file) {
		return file.delete();
	}
	
	public static String performSelectFolderAction() {		
		Boolean old = UIManager.getBoolean("FileChooser.readOnly");  
		UIManager.put("FileChooser.readOnly", Boolean.TRUE);  		
		
		final JFileChooser fileChooser = new JFileChooser(); 
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		final int fileChooserResult = fileChooser.showOpenDialog(null);
				
		String selection = null;
		
		if(fileChooserResult == JFileChooser.APPROVE_OPTION) {
			final File selectedFile = fileChooser.getSelectedFile();
			
			selection = selectedFile.getAbsolutePath();
		}
		
		UIManager.put("FileChooser.readOnly", old); 
		
		return selection;
	}
	
	public static String performSelectFolderAction(final String baseDirectory) {		
		Boolean old = UIManager.getBoolean("FileChooser.readOnly");  
		UIManager.put("FileChooser.readOnly", Boolean.TRUE);  		
		
		final JFileChooser fileChooser = new JFileChooser(); 
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setCurrentDirectory(new File(baseDirectory));
		
		final int fileChooserResult = fileChooser.showOpenDialog(null);
				
		String selection = null;
		
		if(fileChooserResult == JFileChooser.APPROVE_OPTION) {
			final File selectedFile = fileChooser.getSelectedFile();
			
			selection = selectedFile.getAbsolutePath();
		}
		
		UIManager.put("FileChooser.readOnly", old); 
		
		return selection;
	}
	
	public static String performSelectFolderAction(final File baseDirectory) {		
		Boolean old = UIManager.getBoolean("FileChooser.readOnly");  
		UIManager.put("FileChooser.readOnly", Boolean.TRUE);  		
		
		final JFileChooser fileChooser = new JFileChooser(); 
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setCurrentDirectory(baseDirectory);
		
		final int fileChooserResult = fileChooser.showOpenDialog(null);
				
		String selection = null;
		
		if(fileChooserResult == JFileChooser.APPROVE_OPTION) {
			final File selectedFile = fileChooser.getSelectedFile();
			
			selection = selectedFile.getAbsolutePath();
		}
		
		UIManager.put("FileChooser.readOnly", old); 
		
		return selection;
	}
	
	public static String performSelectFileAction() {		
		Boolean old = UIManager.getBoolean("FileChooser.readOnly");  
		UIManager.put("FileChooser.readOnly", Boolean.TRUE);  		
		
		final JFileChooser fileChooser = new JFileChooser(); 
		fileChooser.setMultiSelectionEnabled(false);
		
		final int fileChooserResult = fileChooser.showOpenDialog(null);
				
		String selection = null;
		
		if(fileChooserResult == JFileChooser.APPROVE_OPTION) {
			final File selectedFile = fileChooser.getSelectedFile();
			
			selection = selectedFile.getAbsolutePath();
		}
		
		UIManager.put("FileChooser.readOnly", old); 
		
		return selection;
	}
	
	public static String performSelectImageFileAction() {		
		Boolean old = UIManager.getBoolean("FileChooser.readOnly");  
		UIManager.put("FileChooser.readOnly", Boolean.TRUE);  		
		
		final JFileChooser fileChooser = new JFileChooser(); 
		final FileSelectImagePreviewAccessory imagePreviewAccessory = new FileSelectImagePreviewAccessory();
		
		fileChooser.setAccessory(imagePreviewAccessory);
		fileChooser.addPropertyChangeListener(imagePreviewAccessory);
		fileChooser.setMultiSelectionEnabled(false);
		
		final int fileChooserResult = fileChooser.showOpenDialog(null);
				
		String selection = null;
		
		if(fileChooserResult == JFileChooser.APPROVE_OPTION) {
			final File selectedFile = fileChooser.getSelectedFile();
			
			selection = selectedFile.getAbsolutePath();
		}
		
		UIManager.put("FileChooser.readOnly", old); 
		
		return selection;
	}
	
	public static String performSelectVideoFileAction() {		
		Boolean old = UIManager.getBoolean("FileChooser.readOnly");  
		UIManager.put("FileChooser.readOnly", Boolean.TRUE);  		
		
		final JFileChooser fileChooser = new JFileChooser(); 
		final FileSelectVideoPreviewAccessory imagePreviewAccessory = new FileSelectVideoPreviewAccessory();
		
		fileChooser.setAccessory(imagePreviewAccessory);
		fileChooser.addPropertyChangeListener(imagePreviewAccessory);
		fileChooser.setMultiSelectionEnabled(false);
		
		final int fileChooserResult = fileChooser.showOpenDialog(null);
				
		String selection = null;
		
		if(fileChooserResult == JFileChooser.APPROVE_OPTION) {
			final File selectedFile = fileChooser.getSelectedFile();
			
			selection = selectedFile.getAbsolutePath();
		}
		
		UIManager.put("FileChooser.readOnly", old);
		
		return selection;
	}
}

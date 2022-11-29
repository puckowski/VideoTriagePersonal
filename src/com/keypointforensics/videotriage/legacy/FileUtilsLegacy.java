package com.keypointforensics.videotriage.legacy;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.keypointforensics.videotriage.util.FileUtils;

public class FileUtilsLegacy {

	/*
	 * Author: Daniel Puckowski
	 */
	
	public static final String BINARIES_DIRECTORY = FileUtils.RESOURCES_DIRECTORY
			+ "binaries" + File.separatorChar;
	
	public static final String VIDEO_FRAME_EXTRACTS_DIRECTORY = FileUtils.RESOURCES_DIRECTORY
			+ "extracts" + File.separatorChar;
	
	public static final String TEMPORARY_DIRECTORY = FileUtils.RESOURCES_DIRECTORY
			+ "temporary" + File.separatorChar;
	
	public synchronized static boolean isFileExist(final String absoluteFilePath) {
		File file = new File(absoluteFilePath);

		if (file.exists() && !file.isDirectory()) {
			return true;
		} else {
			return false;
		}
	}

	public synchronized static boolean isDirectoryExist(final String absoluteDirectoryPath) {
		File directory = new File(absoluteDirectoryPath);

		if (directory.exists()) {
			return true;
		} else {
			return false;
		}
	}

	public synchronized static boolean createDirectory(final String absoluteDirectoryPath) {
		File directory = new File(absoluteDirectoryPath);

		if (directory.exists() && directory.isDirectory()) {
			FileUtilsLegacy.deleteDirectoryContents(directory, true);
		}

		if (directory.exists() == false) {
			return directory.mkdir();
		} else {
			return false;
		}
	}
	
	public synchronized static boolean createDirectory(final String absoluteDirectoryPath, boolean deleteExistingContents) {
		File directory = new File(absoluteDirectoryPath);

		if (directory.exists() && directory.isDirectory() && deleteExistingContents == true) {
			FileUtilsLegacy.deleteDirectoryContents(directory, true);
		}

		if (directory.exists() == false) {
			return directory.mkdir();
		} else {
			return false;
		}
	}

	public synchronized static String getFileExtension(final String filename) {
		String extension = "";
		int periodIndex = filename.lastIndexOf('.');

		if ((periodIndex > 0) && (periodIndex < filename.length() - 1)) {
			extension = filename.substring(periodIndex + 1).toLowerCase();
		}

		return extension;
	}

	public synchronized static boolean isImageFile(final String imageFilename)
			throws IOException {
		boolean isImage = false;
		BufferedImage bufferedImage = ImageIO.read(new File(imageFilename));

		if (bufferedImage != null) {
			isImage = true;
			
			bufferedImage.flush();
			bufferedImage = null;
		}

		return isImage;
	}

	public synchronized static boolean isVideoFile(final String videoFilename)
			throws IOException {
		if(videoFilename.contains(".mp4") || videoFilename.contains(".avi")) {
			return true;
		} else {
			return false;
		}
	}
	
	public synchronized static boolean deleteFile(final File file) {
		if (file.isFile()) {
			return file.delete();
		} else {
			return false;
		}
	}

	public synchronized static boolean deleteFileOrDirectory(final File file) {
	    File[] contents = file.listFiles();
	    
	    if (contents != null) {
	        for (File f : contents) {
	        	deleteFileOrDirectory(f);
	        }
	    }
	   
	    return file.delete();
	}

	public synchronized static boolean deleteDirectoryContents(final File directory, boolean deleteContainedDirectories) {		
		if(directory == null) {
			return true;
		}
		
		File[] files = directory.listFiles();
		
		boolean allDeletionsSuccessful = true;

		if(files == null) {
			return deleteFileOrDirectory(directory);
		}
		
		for (File i : files) {
			if(i.isDirectory() == true) {
				allDeletionsSuccessful = deleteDirectoryContents(i, true);
				
				deleteFileOrDirectory(i);
			}
			else if(deleteFileOrDirectory(i) == false) {
				allDeletionsSuccessful = false;
			}
		}
		
		return allDeletionsSuccessful;
	}
	
	public synchronized static boolean moveFileToNewDirectory(String absoluteFilePath,
			String newDirectoryPath) {
		boolean moveSuccessful = false;
		
		try {
			File file = new File(absoluteFilePath);

			if (file.renameTo(new File(newDirectoryPath + file.getName()))) {				
				moveSuccessful = true;
			} 
		} catch (Exception exception) {

		}
		
		return moveSuccessful;
	}

	public static void openDocument(final String absoluteFilePath) {
		if(absoluteFilePath == null || absoluteFilePath.isEmpty() == true) {
			return;
		}
		
		String filename = absoluteFilePath;
		
		if(filename.contains("\\") == true) {
			filename = filename.substring(0, filename.lastIndexOf("\\"));
		}
		
		if (Desktop.isDesktopSupported()) {
			try {
				File document = new File(absoluteFilePath);
				
				if(FileUtilsLegacy.isFileExist(absoluteFilePath) == true) { 
					Desktop.getDesktop().open(document);
				} 
			} catch (IOException ioException) {
				UtilsLegacy.displayMessageDialog("Document load error", "Could not open \"" + filename + "\".\nCould not find application to open PDF file."); //or RTF file.");
			}
		} else {
			UtilsLegacy.displayMessageDialog("Document load error", "Desktop operations appear to be unsupported on your computer.");
		}
	}
	
	public synchronized static List<String> parseDirectory(String absoluteFilePath) {
		ArrayList<String> filenames = new ArrayList<String>();
		
		File folder = new File(absoluteFilePath);
		
		String[] filenamesArray = folder.list();
		
		for(int i = 0; i < filenamesArray.length; ++i) {
			filenames.add(absoluteFilePath + filenamesArray[i]);
		}
		
		return filenames;
	}
	
	public synchronized static List<String> parseDirectoryRecursiveForAll(String absoluteFilePath) {		
		ArrayList<String> filenames = new ArrayList<String>();

		try {
			Path startPath = Paths.get(absoluteFilePath);
			Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir,
						BasicFileAttributes attrs) {
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file,
						BasicFileAttributes attrs) {
					filenames.add(file.toString());
					
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException e) {
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}

		return filenames;
	}
	
	public synchronized static List<String> parseDirectoryRecursiveForImages(String absoluteFilePath) { 		
		ArrayList<String> filenames = new ArrayList<String>();

		try {
			Path startPath = Paths.get(absoluteFilePath);
			Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir,
						BasicFileAttributes attrs) {
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file,
						BasicFileAttributes attrs) {
					String filename = file.toString().toLowerCase();

					if (filename.contains("jpg") || filename.contains("jpeg")
							|| filename.contains("png")
							|| filename.contains("bmp")
							|| filename.contains("gif")) {
						
						filenames.add(filename);
					}

					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException e) {
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}

		return filenames;
	}
	
	public synchronized static List<String> parseDirectoryRecursiveForImagesWithoutFalsePositives(String absoluteFilePath) { 		
		ArrayList<String> filenames = new ArrayList<String>();

		try {
			Path startPath = Paths.get(absoluteFilePath);
			Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir,
						BasicFileAttributes attrs) {
					if(dir.toString().startsWith(absoluteFilePath) == false) {
						return FileVisitResult.TERMINATE;
					}
					else if(dir.toString().endsWith("false_positives") == true) { 
						return FileVisitResult.TERMINATE;
					}
					else {								
						return FileVisitResult.CONTINUE;
					}
				}

				@Override
				public FileVisitResult visitFile(Path file,
						BasicFileAttributes attrs) {
					String filename = file.toString().toLowerCase();

					if (filename.contains("jpg") || filename.contains("jpeg")
							|| filename.contains("png")
							|| filename.contains("bmp")
							|| filename.contains("gif")) {
						
						filenames.add(filename);
					}

					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException e) {
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return filenames;
	}
	
	public synchronized static List<String> parseDirectoryRecursiveAndPrint(String absoluteFilePath) {		
		ArrayList<String> filenames = new ArrayList<String>();

		try {
			Path startPath = Paths.get(absoluteFilePath);
			Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir,
						BasicFileAttributes attrs) {
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file,
						BasicFileAttributes attrs) {
					String filename = file.toString().toLowerCase();

					filenames.add(filename);
					
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException e) {
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}

		return filenames;
	}
	
	public synchronized static List<String> parseDirectoryRecursiveAndPrintShort(String absoluteFilePath) {		
		ArrayList<String> filenames = new ArrayList<String>();

		try {
			Path startPath = Paths.get(absoluteFilePath);
			Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir,
						BasicFileAttributes attrs) {
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file,
						BasicFileAttributes attrs) {
					String filename = file.toString(); 

					if(filename.contains(absoluteFilePath) == true) {
						filename = filename.replace(absoluteFilePath, "");
					}
					
					filename = filename.toLowerCase();
					
					filenames.add(filename);
					
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException e) {
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}

		return filenames;
	}
	
	public static void saveTextToFile(PrintWriter writer, String text) {
		writer.println(text);
	}
	
	public static boolean saveStringsToFile(String filename, ArrayList<String> text) {
		boolean success = true;
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(filename);
			
			for(int i = 0; i < text.size(); ++i) {
				saveTextToFile(writer, text.get(i));
			}
		} catch (FileNotFoundException e) {
			success = false;
		}
		
		if(writer != null) {
			writer.close();
		}
		
		return success;
	}
	
	public static int countFilesInDirectory(File directory) {
		int count = 0;
		for (File file : directory.listFiles()) {
			if (file.isFile()) {
				String filename = file.toString().toLowerCase();

				if (filename.contains("jpg") || filename.contains("jpeg")
						|| filename.contains("png") || filename.contains("bmp")
						|| filename.contains("gif")) {
					count++;
				}

			}
			if (file.isDirectory()) {
				count += countFilesInDirectory(file);
			}
		}
		return count;
	}
}

package com.keypointforensics.videotriage.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.keypointforensics.videotriage.progress.ProgressBundle;

public class ZipUtils {

	private static final int DEFAULT_BUFFER_SIZE = 4096;
	
	public static final int COMPRESSION_LEVEL_DEFAULT = 9;
	public static final int COMPRESSION_LEVEL_MINIMUM = 0;
	public static final int COMPRESSION_LEVEL_MAXIMUM = 9;
	
	public static void zipFiles(final ArrayList<String> absoluteFilePathsToZip, final String baseZipPath, final String outputAbsoluteFilePath) throws IOException {
        FileOutputStream fos = new FileOutputStream(outputAbsoluteFilePath);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        zipOut.setLevel(COMPRESSION_LEVEL_DEFAULT);
        
        File fileToZip;
        FileInputStream fis;
        ZipEntry zipEntry;
        
        byte[] bytes;
        int length;
        
        for (String srcFile : absoluteFilePathsToZip) {
            fileToZip = new File(srcFile);
            fis = new FileInputStream(fileToZip);
            zipEntry = new ZipEntry(fileToZip.getAbsolutePath().replace(baseZipPath, ""));
            zipOut.putNextEntry(zipEntry);
 
            bytes = new byte[DEFAULT_BUFFER_SIZE];

            while((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            
            fis.close();
        }
        
        zipOut.close();
        fos.close();
	}
	
	public static void zipFiles(final ArrayList<String> absoluteFilePathsToZip, final String baseZipPath, final String outputAbsoluteFilePath, final ProgressBundle progressBundle) throws IOException {
        FileOutputStream fos = new FileOutputStream(outputAbsoluteFilePath);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        zipOut.setLevel(COMPRESSION_LEVEL_DEFAULT);
        
        File fileToZip;
        FileInputStream fis;
        ZipEntry zipEntry;
        
        byte[] bytes;
        int length;
        
        for (String srcFile : absoluteFilePathsToZip) {
            fileToZip = new File(srcFile);
            fis = new FileInputStream(fileToZip);
            zipEntry = new ZipEntry(fileToZip.getAbsolutePath().replace(baseZipPath, ""));
            zipOut.putNextEntry(zipEntry);
 
            bytes = new byte[DEFAULT_BUFFER_SIZE];

            while((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            
            fis.close();
            
            progressBundle.progressBar.setValue(progressBundle.progressBar.getValue() + 1);
    		progressBundle.progressBar.repaint();
        }
        
        zipOut.close();
        fos.close();
        
        progressBundle.progressBar.setValue(progressBundle.progressBar.getValue() + 1);
		progressBundle.progressBar.repaint();
		progressBundle.frame.dispose();
	}
	
	public static void zipFile(final String absoluteFilePathToZip, final String outputAbsoluteFilePath) throws IOException {
        FileOutputStream fos = new FileOutputStream(outputAbsoluteFilePath);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        zipOut.setLevel(COMPRESSION_LEVEL_DEFAULT);
        
        byte[] bytes;
        int length;
        
        File fileToZip = new File(absoluteFilePathToZip);
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
        zipOut.putNextEntry(zipEntry);
 
        bytes = new byte[DEFAULT_BUFFER_SIZE];

        while((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
            
        fis.close();
        
        zipOut.close();
        fos.close();
	}
	
	public static void zipFile(final String absoluteFilePathToZip, final String outputAbsoluteFilePath, final ProgressBundle progressBundle) throws IOException {
        FileOutputStream fos = new FileOutputStream(outputAbsoluteFilePath);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        zipOut.setLevel(COMPRESSION_LEVEL_DEFAULT);
        
        byte[] bytes;
        int length;
        
        File fileToZip = new File(absoluteFilePathToZip);
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
        zipOut.putNextEntry(zipEntry);
 
        bytes = new byte[DEFAULT_BUFFER_SIZE];

        while((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
            
            progressBundle.progressBar.setValue(progressBundle.progressBar.getValue() + length);
    		progressBundle.progressBar.repaint();
        }
            
        fis.close();
        
        zipOut.close();
        fos.close();
        
        progressBundle.progressBar.setValue(progressBundle.progressBar.getValue() + 1);
		progressBundle.progressBar.repaint();
		progressBundle.frame.dispose();
	}
	
	public static int getZipFileEntryCount(final String absoluteFilePath) throws IOException {
        ZipInputStream zis = new ZipInputStream(new FileInputStream(absoluteFilePath));
        ZipEntry zipEntry = zis.getNextEntry();
        
        int count = 0;
        while(zipEntry != null) {
            count++;
           
            zipEntry = zis.getNextEntry();
        }
        
        zis.closeEntry();
        zis.close();
        
        return count;
	}
	
	public static void unzipFile(String absoluteFilePath, String outputFolder) throws IOException {
		ProgressBundle progressBundle = ProgressUtils.getIndeterminateProgressBundle("Preparing Unzip");
		
		final int zipFileEntryCount = getZipFileEntryCount(absoluteFilePath);
		
		progressBundle.frame.dispose();
		progressBundle = ProgressUtils.getProgressBundle("Unzip Progress", zipFileEntryCount);
		
		byte[] buffer = new byte[1024];

		try {
			File folder = new File(outputFolder);

			if (folder.exists() == false) {
				folder.mkdir();
			}

			ZipInputStream zis = new ZipInputStream(new FileInputStream(absoluteFilePath));
			ZipEntry ze = zis.getNextEntry();

			String fileName;
			File newFile;
			File parentFile;
			int len;
			
			while (ze != null) {
				fileName = ze.getName();
				newFile = new File(outputFolder + File.separator + fileName);

				parentFile = new File(newFile.getParent());
				parentFile.mkdirs();

				FileOutputStream fos = new FileOutputStream(newFile);

				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}

				fos.close();
				ze = zis.getNextEntry();
				
				progressBundle.progressBar.setValue(progressBundle.progressBar.getValue() + 1);
	    		progressBundle.progressBar.repaint();
			}

			zis.closeEntry();
			zis.close();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
		
		progressBundle.progressBar.setValue(progressBundle.progressBar.getValue() + 1);
      	progressBundle.progressBar.repaint();
      	progressBundle.frame.dispose();
	}
	 
	/*
	public static void unzipFile(final String absoluteFilePath, final String baseUnzipPath) throws IOException {
		ProgressBundle progressBundle = ProgressUtils.getIndeterminateProgressBundle("Preparing Unzip");
		
		final int zipFileEntryCount = getZipFileEntryCount(absoluteFilePath);
		
		progressBundle.frame.dispose();
		progressBundle = ProgressUtils.getProgressBundle("Unzip Progress", zipFileEntryCount);
		
		byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(absoluteFilePath));
        ZipEntry zipEntry = zis.getNextEntry();
        
        String fileName;
        File newFile;
        FileOutputStream fos;
        int len;
        
        while(zipEntry != null) {
        	fileName = zipEntry.getName();
        	 
        	if(zipEntry.isDirectory() == true) {
        		newFile = new File(baseUnzipPath + fileName);
        		newFile.mkdir();
        		
        		continue;
        	}
        	
            newFile = new File(baseUnzipPath + fileName);
			
            //if(newFile.exists()==false) {
           // 	newFile.createNewFile();
            //}
            try {
				fos = new FileOutputStream(newFile);
				//System.out.println(newFile);
				//System.out.println("FN:" + fileName);
		        while ((len = zis.read(buffer)) > 0) {
		        	fos.write(buffer, 0, len);
		        }
		            
		        fos.close();
		        
		        progressBundle.progressBar.setValue(progressBundle.progressBar.getValue() + 1);
	    		progressBundle.progressBar.repaint();
			} catch (FileNotFoundException fileNotFoundException) {
				fileNotFoundException.printStackTrace();
			}
           
            zipEntry = zis.getNextEntry();
        }
        
        zis.closeEntry();
        zis.close();
        
        progressBundle.progressBar.setValue(progressBundle.progressBar.getValue() + 1);
      	progressBundle.progressBar.repaint();
      	progressBundle.frame.dispose();
    }
    */
	
	public static void zipFiles(final ArrayList<String> absoluteFilePathsToZip, final String baseZipPath, final String outputAbsoluteFilePath, final int compressionLevel) throws IOException {
        FileOutputStream fos = new FileOutputStream(outputAbsoluteFilePath);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        zipOut.setLevel(compressionLevel);
        
        File fileToZip;
        FileInputStream fis;
        ZipEntry zipEntry;
        
        byte[] bytes;
        int length;
        
        for (String srcFile : absoluteFilePathsToZip) {
            fileToZip = new File(srcFile);
            fis = new FileInputStream(fileToZip);
            zipEntry = new ZipEntry(fileToZip.getAbsolutePath().replace(baseZipPath, ""));
            zipOut.putNextEntry(zipEntry);
 
            bytes = new byte[DEFAULT_BUFFER_SIZE];

            while((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            
            fis.close();
        }
        
        zipOut.close();
        fos.close();
	}
	
	public static void zipFiles(final ArrayList<String> absoluteFilePathsToZip, final String baseZipPath, final String outputAbsoluteFilePath, final ProgressBundle progressBundle,
			final int compressionLevel) throws IOException {
        FileOutputStream fos = new FileOutputStream(outputAbsoluteFilePath);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        zipOut.setLevel(compressionLevel);
        
        File fileToZip;
        FileInputStream fis;
        ZipEntry zipEntry;
        
        byte[] bytes;
        int length;
        
        for (String srcFile : absoluteFilePathsToZip) {
            fileToZip = new File(srcFile);
            fis = new FileInputStream(fileToZip);
            zipEntry = new ZipEntry(fileToZip.getAbsolutePath().replace(baseZipPath, ""));
            zipOut.putNextEntry(zipEntry);
 
            bytes = new byte[DEFAULT_BUFFER_SIZE];

            while((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            
            fis.close();
            
            progressBundle.progressBar.setValue(progressBundle.progressBar.getValue() + 1);
    		progressBundle.progressBar.repaint();
        }
        
        zipOut.close();
        fos.close();
        
        progressBundle.progressBar.setValue(progressBundle.progressBar.getValue() + 1);
		progressBundle.progressBar.repaint();
		progressBundle.frame.dispose();
	}
	
	public static void zipFile(final String absoluteFilePathToZip, final String outputAbsoluteFilePath, final int compressionLevel) throws IOException {
        FileOutputStream fos = new FileOutputStream(outputAbsoluteFilePath);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        zipOut.setLevel(compressionLevel);
        
        byte[] bytes;
        int length;
        
        File fileToZip = new File(absoluteFilePathToZip);
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
        zipOut.putNextEntry(zipEntry);
 
        bytes = new byte[DEFAULT_BUFFER_SIZE];

        while((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
            
        fis.close();
        
        zipOut.close();
        fos.close();
	}
	
	public static void zipFile(final String absoluteFilePathToZip, final String outputAbsoluteFilePath, final ProgressBundle progressBundle,
			final int compressionLevel) throws IOException {
        FileOutputStream fos = new FileOutputStream(outputAbsoluteFilePath);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        zipOut.setLevel(compressionLevel);
        
        byte[] bytes;
        int length;
        
        File fileToZip = new File(absoluteFilePathToZip);
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
        zipOut.putNextEntry(zipEntry);
 
        bytes = new byte[DEFAULT_BUFFER_SIZE];

        while((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
            
            progressBundle.progressBar.setValue(progressBundle.progressBar.getValue() + length);
    		progressBundle.progressBar.repaint();
        }
            
        fis.close();
        
        zipOut.close();
        fos.close();
        
        progressBundle.progressBar.setValue(progressBundle.progressBar.getValue() + 1);
		progressBundle.progressBar.repaint();
		progressBundle.frame.dispose();
	}
}

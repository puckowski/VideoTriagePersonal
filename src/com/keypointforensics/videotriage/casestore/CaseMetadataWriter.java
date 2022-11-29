package com.keypointforensics.videotriage.casestore;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;

import com.keypointforensics.videotriage.util.FileUtils;

public class CaseMetadataWriter {

	public static final boolean CONTEXT_FILE_MODE_APPEND               = true;
	public static final String  CONTEXT_FILE_DATA_SOURCE_NAME          = "SOURCE";
	public static final String  CONTEXT_FILE_DATA_SOURCE_ENHANCED_NAME = "SOURCE_ENHANCED";
	public static final String  CONTEXT_FILE_DATA_SOURCE_REDACTED_NAME = "SOURCE_REDACTED";
	public static final String  CONTEXT_FILE_DATA_SOURCE_MERGED_NAME   = "SOURCE_MERGED";
	public static final String  SUFFIX_CONTEXT_FILENAME                = "_context.txt";
	
	public static void createContextFilenameIfNeeded(final String absoluteContextFilePath) {
		File checkFile = new File(absoluteContextFilePath);
		
		if(checkFile.exists() == false) {
			try {
				checkFile.createNewFile();
			} catch (IOException ioException) {
				//ioException.printStackTrace();
			}
		}
	}
	
	private static PrintWriter getMetadataWriter(final String absoluteContextFilePath) throws IOException {
		FileWriter fileWriter = new FileWriter(absoluteContextFilePath, CONTEXT_FILE_MODE_APPEND);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		PrintWriter printWriter = new PrintWriter(bufferedWriter);
		
		return printWriter;
	}
	
	public static void writeNewSourceToContext(final String absoluteContextFilePath, final String sourceName) {
		try {
			PrintWriter printWriter = getMetadataWriter(absoluteContextFilePath);
			
			printWriter.write(CONTEXT_FILE_DATA_SOURCE_NAME);
			printWriter.write("\n");
			printWriter.write(sourceName); 
			printWriter.write("\n");
			
			printWriter.flush();
			printWriter.close();
		} catch (IOException ioException) {
			//ioException.printStackTrace();
		}
	}
	
	public static void writeNewMergedSourceToContext(final String absoluteContextFilePath, final String sourceName) {
		try {
			PrintWriter printWriter = getMetadataWriter(absoluteContextFilePath);
			
			printWriter.write(CONTEXT_FILE_DATA_SOURCE_MERGED_NAME);
			printWriter.write("\n");
			printWriter.write(sourceName); 
			printWriter.write("\n");
			
			printWriter.flush();
			printWriter.close();
		} catch (IOException ioException) {
			//ioException.printStackTrace();
		}
	}
	
	public static void writeNewEnhancedSourceToContext(final String absoluteContextFilePath, final String sourceName) {
		try {
			PrintWriter printWriter = getMetadataWriter(absoluteContextFilePath);
			
			printWriter.write(CONTEXT_FILE_DATA_SOURCE_ENHANCED_NAME);
			printWriter.write("\n");
			printWriter.write(sourceName); 
			printWriter.write("\n");
			
			printWriter.flush();
			printWriter.close();
		} catch (IOException ioException) {
			//ioException.printStackTrace();
		}
	}
	
	public static void writeNewRedactedSourceToContext(final String absoluteContextFilePath, final String sourceName) {
		try {
			PrintWriter printWriter = getMetadataWriter(absoluteContextFilePath);
			
			printWriter.write(CONTEXT_FILE_DATA_SOURCE_REDACTED_NAME);
			printWriter.write("\n");
			printWriter.write(sourceName); 
			printWriter.write("\n");
			
			printWriter.flush();
			printWriter.close();
		} catch (IOException ioException) {
			//ioException.printStackTrace();
		}
	}
	
	public static ArrayList<String> getMergedVideoSourceListing(final String absoluteContextFilePath) {
		ArrayList<String> listOfEnhancedFiles = new ArrayList<String>();
		HashSet<String> uniqueEnhancedFiles = new HashSet<String>();
		boolean addNext = false;
		
		try(BufferedReader br = new BufferedReader(new FileReader(new File(absoluteContextFilePath)))) {
		    for(String line; (line = br.readLine()) != null; ) {
		        if(line.equals(CaseMetadataWriter.CONTEXT_FILE_DATA_SOURCE_MERGED_NAME) == true) {
		        	addNext = true;
		        } else if(addNext == true) {
		        	addNext = false;
		        	
		        	if(uniqueEnhancedFiles.contains(line) == false) {
		        		listOfEnhancedFiles.add(line);
		        		uniqueEnhancedFiles.add(line);
		        	}
		        }
		    }
		} catch (FileNotFoundException fileNotFoundException) {
			fileNotFoundException.printStackTrace();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
		
		return listOfEnhancedFiles;
	}
	
	public static ArrayList<String> getEnhancedVideoSourceListing(final String absoluteContextFilePath) {
		ArrayList<String> listOfEnhancedFiles = new ArrayList<String>();
		HashSet<String> uniqueEnhancedFiles = new HashSet<String>();
		boolean addNext = false;
		
		try(BufferedReader br = new BufferedReader(new FileReader(new File(absoluteContextFilePath)))) {
		    for(String line; (line = br.readLine()) != null; ) {
		        if(line.equals(CaseMetadataWriter.CONTEXT_FILE_DATA_SOURCE_ENHANCED_NAME) == true) {
		        	addNext = true;
		        } else if(addNext == true) {
		        	addNext = false;
		        	
		        	if(uniqueEnhancedFiles.contains(line) == false) {
		        		listOfEnhancedFiles.add(line);
		        		uniqueEnhancedFiles.add(line);
		        	}
		        }
		    }
		} catch (FileNotFoundException fileNotFoundException) {
			fileNotFoundException.printStackTrace();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
		
		return listOfEnhancedFiles;
	}
	
	public static ArrayList<String> getRedactedVideoSourceListing(final String absoluteContextFilePath) {
		ArrayList<String> listOfEnhancedFiles = new ArrayList<String>();
		HashSet<String> uniqueEnhancedFiles = new HashSet<String>();
		boolean addNext = false;
		
		try(BufferedReader br = new BufferedReader(new FileReader(new File(absoluteContextFilePath)))) {
		    for(String line; (line = br.readLine()) != null; ) {
		        if(line.equals(CaseMetadataWriter.CONTEXT_FILE_DATA_SOURCE_REDACTED_NAME) == true) {
		        	addNext = true;
		        } else if(addNext == true) {
		        	addNext = false;
		        	
		        	if(uniqueEnhancedFiles.contains(line) == false) {
		        		listOfEnhancedFiles.add(line);
		        		uniqueEnhancedFiles.add(line);
		        	}
		        }
		    }
		} catch (FileNotFoundException fileNotFoundException) {
			fileNotFoundException.printStackTrace();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
		
		return listOfEnhancedFiles;
	}

	public static ArrayList<String> getVideoSourceListing(final String absoluteContextFilePath) {
		ArrayList<String> listOfEnhancedFiles = new ArrayList<String>();
		HashSet<String> uniqueEnhancedFiles = new HashSet<String>();
		boolean addNext = false;
		
		try(BufferedReader br = new BufferedReader(new FileReader(new File(absoluteContextFilePath)))) {
		    for(String line; (line = br.readLine()) != null; ) {
		        if(line.equals(CaseMetadataWriter.CONTEXT_FILE_DATA_SOURCE_NAME) == true) {
		        	addNext = true;
		        } else if(addNext == true) {
		        	addNext = false;
		        	
		        	if(uniqueEnhancedFiles.contains(line) == false) {
		        		listOfEnhancedFiles.add(line);
		        		uniqueEnhancedFiles.add(line);
		        	}
		        }
		    }
		} catch (FileNotFoundException fileNotFoundException) {
			fileNotFoundException.printStackTrace();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
		
		return listOfEnhancedFiles;
	}
	
	public static String getContextFilenameFromDatabaseName(String databaseName) {
		String contextFilename = FileUtils.CONTEXT_DIRECTORY;
		
		if(databaseName.contains(File.separator) == true) {
			databaseName = databaseName.substring(databaseName.lastIndexOf(File.separator) + 1, databaseName.length());
		}
		
		contextFilename += databaseName + SUFFIX_CONTEXT_FILENAME;
		
		return contextFilename;
	}
	
}

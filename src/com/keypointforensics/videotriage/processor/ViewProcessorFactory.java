package com.keypointforensics.videotriage.processor;

import com.keypointforensics.videotriage.blob.context.BlobContextList;
import com.keypointforensics.videotriage.gui.main.GuiMain;
import com.keypointforensics.videotriage.local.LocalMeanFilterProcessor;
import com.keypointforensics.videotriage.local.LocalViewProcessor;
import com.keypointforensics.videotriage.remote.GenericRemoteViewProcessor;
import com.keypointforensics.videotriage.remote.RemoteViewProcessor;

public class ViewProcessorFactory {

	/*
	 * Author: Daniel Puckowski
	 */
	
	public static final String GENERIC_REMOTE_PROCESSOR       = "Generic";
	public static final String ANDROID_BL_IP_REMOTE_PROCESSOR = "AndroidBlipIp";
	public static final String LOCAL_FILE_PROCESSOR           = "LocalFile";
	
	public RemoteViewProcessor getRemoteProcessor(final GuiMain guiMain, final String processorName, final BlobContextList blobContextList) {
		if(processorName.equals(GENERIC_REMOTE_PROCESSOR)) {			
			return new GenericRemoteViewProcessor(blobContextList);
		}
				
		return null;
	}
	
	//public LocalViewProcessor getLocalProcessor(final String absoluteVideoPath, final int preliminaryProgressTarget, final String processorName, final BlobContextList blobContextList) {
	public LocalViewProcessor getLocalProcessor(final String absoluteVideoPath, final String processorName, final BlobContextList blobContextList) {
		if(processorName.equals(LOCAL_FILE_PROCESSOR)) {			
			LocalMeanFilterProcessor localMeanFilterProcessor = new LocalMeanFilterProcessor(blobContextList);
			//LocalMeanFilterProcessor localMeanFilterProcessor = new LocalMeanFilterProcessor(preliminaryProgressTarget, blobContextList);
			localMeanFilterProcessor.setOriginalVideoFilename(absoluteVideoPath);//LocalFileViewMeanFilterThread();
			
			return localMeanFilterProcessor;
		}
				
		return null;
	}
}

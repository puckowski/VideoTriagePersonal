package com.keypointforensics.videotriage.gui.main.controller;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.keypointforensics.videotriage.params.BackgroundRuntimeParams;
import com.keypointforensics.videotriage.params.BlobRuntimeParams;
import com.keypointforensics.videotriage.params.MassRuntimeParams;
import com.keypointforensics.videotriage.params.MetadataRuntimeParams;
import com.keypointforensics.videotriage.params.SourceRuntimeParams;
import com.keypointforensics.videotriage.params.StatusBarRuntimeParams;
import com.keypointforensics.videotriage.params.WriteRuntimeParams;

public class CameraControllerRegistry {
		
	/*
	 * Author: Daniel Puckowski
	 */
	
	public static final CameraControllerRegistry INSTANCE = new CameraControllerRegistry();
	
	public static final String DEFAULT_ID_PREFIX = "Camera ";
	
	private ConcurrentHashMap<String, CameraController> mControllerRegistry;
	private String mLastControllerId;
		
	private CameraControllerRegistry() {
		mControllerRegistry = new ConcurrentHashMap<String, CameraController>();
	}
	
	public ConcurrentHashMap<String, CameraController> getControllerMap() {
		return mControllerRegistry;
	}
	
	public int getControllerCount() {
		return mControllerRegistry.size();
	}
	
	public void putController(final String id, final CameraController controller) {
		mLastControllerId  = id;
		mControllerRegistry.put(id, controller);
	}
	
	public String getLastControllerId() {
		return mLastControllerId;
	}
	
	public CameraController getController(final String controllerId) {
		return mControllerRegistry.get(controllerId);
	}
	
	public void removeController(final String controllerId) {
		mControllerRegistry.remove(controllerId);
	}
	
	public String getNextId() {
		int controllerCount = getControllerCount();
		String nextId = null;
		
		for(int i = 1; i < controllerCount + 1; ++i) {
			nextId = DEFAULT_ID_PREFIX + i;
			
			if(mControllerRegistry.containsKey(nextId) == false) {
				return nextId;
			}
		}
		
		controllerCount++;
		nextId = DEFAULT_ID_PREFIX + controllerCount;
				
		return nextId;
	}
	
	public MassRuntimeParams getMassParams(final String controllerId) {
		return mControllerRegistry.get(controllerId).getMassParams();
	}
	
	public void setMassParams(final String controllerId, final MassRuntimeParams massParams) {
		mControllerRegistry.get(controllerId).setMassParams(massParams);
	}
	
	public BackgroundRuntimeParams getBackgroundParams(final String controllerId) {
		return mControllerRegistry.get(controllerId).getBackgroundParams();
	}
	
	public void setBackgroundParams(final String controllerId, final BackgroundRuntimeParams backgroundParams) {
		mControllerRegistry.get(controllerId).setBackgroundParams(backgroundParams);
	}
	
	public BlobRuntimeParams getBlobParams(final String controllerId) {
		return mControllerRegistry.get(controllerId).getBlobParams();
	}
	
	public void setBlobParams(final String controllerId, final BlobRuntimeParams blobParams) {
		mControllerRegistry.get(controllerId).setBlobParams(blobParams);
	}
	
	public StatusBarRuntimeParams getStatusBarParams(final String controllerId) {
		return mControllerRegistry.get(controllerId).getStatusBarParams();
	}
	
	public void setStatusBarParams(final String controllerId, final StatusBarRuntimeParams statusBarParams) {
		mControllerRegistry.get(controllerId).setStatusBarParams(statusBarParams);
	}
	
	public WriteRuntimeParams getWriteParams(final String controllerId) {
		return mControllerRegistry.get(controllerId).getWriteParams();
	}
	
	public void setWriteParams(final String controllerId, final WriteRuntimeParams writeParams) {
		mControllerRegistry.get(controllerId).setWriteParams(writeParams);
	}
	
	public SourceRuntimeParams getSourceParams(final String controllerId) {
		return mControllerRegistry.get(controllerId).getSourceParams();
	}
	
	public void setSourceParams(final String controllerId, final SourceRuntimeParams sourceParams) {
		mControllerRegistry.get(controllerId).setSourceParams(sourceParams);
	}
	
	public MetadataRuntimeParams getMetadataParams(final String controllerId) {
		return mControllerRegistry.get(controllerId).getMetadataParams();
	}
	
	public void setMetadataParams(final String controllerId, final MetadataRuntimeParams sourceParams) {
		mControllerRegistry.get(controllerId).setMetadataParams(sourceParams);
	}
}

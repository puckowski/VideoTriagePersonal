package com.keypointforensics.videotriage.thread;

import java.util.ArrayList;

import com.keypointforensics.videotriage.gui.controller.local.LocalCameraController;
import com.keypointforensics.videotriage.gui.main.GuiMain;
import com.keypointforensics.videotriage.gui.main.controller.CameraController;
import com.keypointforensics.videotriage.gui.main.controller.CameraControllerRegistry;
import com.keypointforensics.videotriage.staticparams.LocalFileRuntimeParams;
import com.keypointforensics.videotriage.util.ThreadUtils;

public class ProcessLocalFileThread extends Thread {

	/*
	 * Author: Daniel Puckowski
	 */
	
	private final CameraControllerRegistry CONTROLLER_REGISTRY = CameraControllerRegistry.INSTANCE;

	private final GuiMain GUI_MAIN;
	private final ArrayList<String> LOCAL_FILES_TO_PROCESS;
	
	private boolean mRunning;
	
	public ProcessLocalFileThread(final GuiMain guiMain, final ArrayList<String> localFilesToProcess) {
		GUI_MAIN = guiMain;
		LOCAL_FILES_TO_PROCESS = localFilesToProcess;
	}
	
	public boolean isRunning() {
		return mRunning;
	}
	
	public void setRunning(final boolean isRunning) {
		mRunning = isRunning;
	}
	
	@Override
	public void run() {
		ThreadUtils.addThreadToHandleList("ProcLocal Run", this);
		
		mRunning = true;
		
		ArrayList<LocalCameraController> activeCameraControllers = new ArrayList<LocalCameraController>();

		final int maxNumberOfConcurrentControllers = LocalFileRuntimeParams.getGlobalNumberOfConcurrentMonitors();		
		int currentFileIndex = 0;
		String lastControllerId = null;
		CameraController cameraController = null;
		LocalCameraController localCameraController = null;

		while(activeCameraControllers.size() < maxNumberOfConcurrentControllers
				&& activeCameraControllers.size() < LOCAL_FILES_TO_PROCESS.size()) {	
			GUI_MAIN.addCameraPanel();
			lastControllerId = CONTROLLER_REGISTRY.getLastControllerId();
			
			cameraController = CONTROLLER_REGISTRY.getController(lastControllerId);
			
			localCameraController = (LocalCameraController) cameraController;
			cameraController = null;
				
			localCameraController.setLocalFileMode();
				
			activeCameraControllers.add(localCameraController);
		}
		
		boolean setDefaultProgressProcessor = false;

		mainLocalFileProcessLoop:
		do {			
			//TODO need try
			try {
				String nextFileToProcess;
				
				while(currentFileIndex < LOCAL_FILES_TO_PROCESS.size()) {	
					
					for(int i = 0; i < activeCameraControllers.size(); ++i) {
						if(activeCameraControllers.get(i).isRunning())
							continue;
						
						nextFileToProcess = LOCAL_FILES_TO_PROCESS.get(currentFileIndex);
						
						//TODO race with i?
						
						activeCameraControllers.get(i).getLocalFileField().setText(nextFileToProcess);
						while(activeCameraControllers.get(i).getLocalFileField().getText().equals(nextFileToProcess) == false) ThreadUtils.blockThread(50, "ProcessLocalFileThread pause");
						
						activeCameraControllers.get(i).processRemoteCamera();
						while(activeCameraControllers.get(i).isRunning() == false) ThreadUtils.blockThread(50, "ProcessLocalFileThread pause");
					
						if(setDefaultProgressProcessor == false) {
							setDefaultProgressProcessor = true;
							
							GUI_MAIN.setFocusedTab(activeCameraControllers.get(i).getControllerId());
						}
						
						currentFileIndex++;
					}
					
					ThreadUtils.blockThread(50, "ProcessLocalFileThread pause");
				}

				for(int i = 0; i < activeCameraControllers.size(); ++i) {
					while(activeCameraControllers.get(i).isRunning()) {
						ThreadUtils.blockThread(50, "ProcessLocalFileThread pause");
					}
				}

				//int currentControllerCount;
				/**
				for(int i = 0; i < activeCameraControllers.size(); ++i) {
					currentControllerCount = CONTROLLER_REGISTRY.getControllerCount();
		
					GUI_MAIN.removeController(activeCameraControllers.get(i).getControllerId());				
	
					while(currentControllerCount == CONTROLLER_REGISTRY.getControllerCount()) {
						ThreadUtils.blockThread(50, "ProcessLocalFileThread pause");
					}
				}
				
				//activeCameraControllers.clear();
				/**/
			}
			catch(Exception generalException) {
				generalException.printStackTrace();
				continue mainLocalFileProcessLoop;
			}
			
			break;
		} while (Thread.currentThread().isInterrupted() == false && mRunning);

		for(CameraController cameraController2 : activeCameraControllers) {
			while(cameraController2.isRunning()) {
				cameraController2.stopRemoteCamera();
			}
		}

		LocalFileRuntimeParams.setGlobalProcessLocalFileThread(null);
		LocalFileRuntimeParams.setCameraControllerPreferencesBundle(null);
		
		//int currentControllerCount, cameraTabCount, previewPaneCount;	

		for(int i = 0; i < activeCameraControllers.size(); ++i) {
			//currentControllerCount = CONTROLLER_REGISTRY.getControllerCount();
			//cameraTabCount = GUI_MAIN.getCameraTabCount();
			//previewPaneCount = GUI_MAIN.getPreviewCount();

			GUI_MAIN.removeController(activeCameraControllers.get(i).getControllerId());	

			//while(currentControllerCount == CONTROLLER_REGISTRY.getControllerCount());
			//while(cameraTabCount == GUI_MAIN.getCameraTabCount());
			//while(previewPaneCount == GUI_MAIN.getPreviewCount());
		}
		
		mRunning = false;
		
		ThreadUtils.removeThreadFromHandleList(this);
	}
}

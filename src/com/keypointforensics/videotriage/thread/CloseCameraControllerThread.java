package com.keypointforensics.videotriage.thread;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.keypointforensics.videotriage.gui.main.CameraPreviewPanel;
import com.keypointforensics.videotriage.gui.main.GuiMain;
import com.keypointforensics.videotriage.gui.main.controller.CameraController;
import com.keypointforensics.videotriage.gui.main.controller.CameraControllerRegistry;
import com.keypointforensics.videotriage.util.CursorUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;

public class CloseCameraControllerThread extends Thread {

	/*
	 * Author: Daniel Puckowski
	 */
	
	private static final Lock REMOVE_CONTROLLER_LOCK = new ReentrantLock();
	
	private final CameraControllerRegistry CONTROLLER_REGISTRY = CameraControllerRegistry.INSTANCE;
	
	private final GuiMain     GUI_MAIN;
	private final String      CONTROLLER_ID;
	private final JTabbedPane CAMERA_TAB_PANE;
	private final JPanel      CAMERA_PREVIEW_PANE;
	
	public CloseCameraControllerThread(final GuiMain guiMain, final String controllerId, final JTabbedPane cameraTabPane, final JPanel cameraPreviewPane) {
		GUI_MAIN            = guiMain;
		CONTROLLER_ID       = controllerId;
		CAMERA_TAB_PANE     = cameraTabPane;
		CAMERA_PREVIEW_PANE = cameraPreviewPane;
	}
	
	@Override
	public void run() {
		ThreadUtils.addThreadToHandleList("CloseCam Run", this);
		
		CursorUtils.setBusyCursor(GUI_MAIN);
		
		final CameraController cameraController = GUI_MAIN.getController(CONTROLLER_ID);
		
		GUI_MAIN.stopController(CONTROLLER_ID);
		
		int cameraTabPaneIndex = -1;
		
		REMOVE_CONTROLLER_LOCK.lock();
		
		cameraTabPaneIndex = getCameraTabPaneIndex();
		
		if(cameraTabPaneIndex != -1) {
			cameraController.forceShutdown();
			
			CAMERA_TAB_PANE.removeTabAt(cameraTabPaneIndex);
			CAMERA_TAB_PANE.revalidate(); //may throw exception
			CAMERA_TAB_PANE.repaint();
		}
		
		/*
		 * Registry remove
		 */
		CONTROLLER_REGISTRY.removeController(CONTROLLER_ID);
		
		int cameraPreviewPaneIndex = -1;
		CameraPreviewPanel cameraPreviewPanel = null;
		
		for(int i = 0; i < CAMERA_PREVIEW_PANE.getComponentCount(); ++i) {
			if(CAMERA_PREVIEW_PANE.getComponent(i) instanceof CameraPreviewPanel) {
				cameraPreviewPanel = (CameraPreviewPanel) CAMERA_PREVIEW_PANE.getComponent(i);

				if(cameraPreviewPanel.getControllerId().equals(CONTROLLER_ID) == true) {
					cameraPreviewPaneIndex = i;
					cameraPreviewPanel = null;
					break;
				}
				
				cameraPreviewPanel = null;
			}
		}
		
		if(cameraPreviewPaneIndex != -1) {
			CAMERA_PREVIEW_PANE.remove(cameraPreviewPaneIndex);
			CAMERA_PREVIEW_PANE.revalidate(); //may throw exception
			CAMERA_PREVIEW_PANE.repaint();
		}
				
		REMOVE_CONTROLLER_LOCK.unlock();
						
		CursorUtils.setDefaultCursor(GUI_MAIN);
		
		ThreadUtils.removeThreadFromHandleList(this);
	}
	
	private int getCameraTabPaneIndex() {
		for(int i = 0; i < CAMERA_TAB_PANE.getTabCount(); ++i) {
			if(CAMERA_TAB_PANE.getTitleAt(i).equals(CONTROLLER_ID) == true) {
				return i;
			}
		}
		
		return -1;
	}
}

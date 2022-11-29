package com.keypointforensics.videotriage.gui.main.controller;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.keypointforensics.videotriage.gui.imagepanel.VideoFeedImagePanel;
import com.keypointforensics.videotriage.gui.main.CameraPreviewPanel;
import com.keypointforensics.videotriage.local.LocalViewProcessor;
import com.keypointforensics.videotriage.params.BackgroundRuntimeParams;
import com.keypointforensics.videotriage.params.BlobRuntimeParams;
import com.keypointforensics.videotriage.params.MassRuntimeParams;
import com.keypointforensics.videotriage.params.MetadataRuntimeParams;
import com.keypointforensics.videotriage.params.SourceRuntimeParams;
import com.keypointforensics.videotriage.params.StatusBarRuntimeParams;
import com.keypointforensics.videotriage.params.WriteRuntimeParams;

public class CameraController extends JPanel implements CameraControllerInterface {
	
	/*
	 * Author: Daniel Puckowski
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3401974083303395519L;

	public String getDatabaseName() {
		return null;
	}
	
	@Override
	public void init() {
		
	}
	
	@Override
	public void forceShutdown() {
		
	}
	
	@Override
	public boolean isRunning() {
		return false;
	}
	
	@Override
	public void stopRemoteCamera() {
		
	}
	
	@Override
	public void processRemoteCamera() {
		
	}
	
	@Override
	public CameraPreviewPanel getPreviewPanel() {
		return null;
	}
	
	@Override
	public VideoFeedImagePanel getVideoFeedPanel() {
		return null;
	}
	
	@Override
	public MassRuntimeParams getMassParams() {
		return null;
	}
	
	@Override
	public void setMassParams(final MassRuntimeParams massParams) {
		return;
	}
	
	@Override
	public BackgroundRuntimeParams getBackgroundParams() {
		return null;
	}
	
	@Override
	public void setBackgroundParams(final BackgroundRuntimeParams backgroundParams) {
		return;
	}
	
	@Override
	public BlobRuntimeParams getBlobParams() {
		return null;
	}
	
	@Override
	public void setBlobParams(final BlobRuntimeParams blobParams) {
		return;
	}
	
	@Override
	public StatusBarRuntimeParams getStatusBarParams() {
		return null;
	}
	
	@Override
	public void setStatusBarParams(final StatusBarRuntimeParams statusBarParams) {
		return;
	}
	
	@Override
	public WriteRuntimeParams getWriteParams() {
		return null;
	}
	
	@Override
	public void setWriteParams(final WriteRuntimeParams writeParams) {
		return;
	}
	
	@Override
	public SourceRuntimeParams getSourceParams() {
		return null;
	}
	
	@Override
	public void setSourceParams(final SourceRuntimeParams sourceParams) {
		return;
	}

	@Override
	public MetadataRuntimeParams getMetadataParams() {
		return null;
	}
	
	@Override
	public void setMetadataParams(final MetadataRuntimeParams metadataParams) {
		return;
	}
	
	@Override
	public String getIpString() {
		return null;
	}

	@Override
	public String getUsername() {
		return null;
	}

	@Override
	public char[] getPassword() {
		return null;
	}

	@Override
	public String getPortString() {
		return null;
	}

	@Override
	public boolean getCustomUrlEnabled() {
		return false;
	}

	@Override
	public JComboBox<String> getIpOrUrlField() {
		return null;
	}

	@Override
	public void setAllParams(CameraControllerPreferencesBundle cameraControllerPreferencesBundle) {		
	}

	@Override
	public CameraControllerPreferencesBundle getAllParams() {
		return null;
	}

	@Override
	public LocalViewProcessor getLocalViewProcessor() {
		return null;
	}
}

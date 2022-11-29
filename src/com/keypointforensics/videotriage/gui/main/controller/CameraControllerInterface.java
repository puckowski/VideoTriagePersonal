package com.keypointforensics.videotriage.gui.main.controller;

import javax.swing.JComboBox;

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

public interface CameraControllerInterface {
	
	/*
	 * Author: Daniel Puckowski
	 */
	
	public void init();
	public void forceShutdown();
	public boolean isRunning();
	public void stopRemoteCamera();
	public void processRemoteCamera();
	
	public String getIpString();
	public String getUsername();
	public char[] getPassword();
	public String getPortString();
	public boolean getCustomUrlEnabled();
	
	public JComboBox<String> getIpOrUrlField();
	public CameraPreviewPanel getPreviewPanel();
	public VideoFeedImagePanel getVideoFeedPanel();
	
	public MassRuntimeParams getMassParams();
	public void setMassParams(final MassRuntimeParams massParams);
	
	public BackgroundRuntimeParams getBackgroundParams();
	public void setBackgroundParams(final BackgroundRuntimeParams backgroundParams);
	
	public BlobRuntimeParams getBlobParams();
	public void setBlobParams(final BlobRuntimeParams blobParams);
	
	public StatusBarRuntimeParams getStatusBarParams();
	public void setStatusBarParams(final StatusBarRuntimeParams statusBarParams);
	
	public WriteRuntimeParams getWriteParams();
	public void setWriteParams(final WriteRuntimeParams writeParams);

	public void setAllParams(final CameraControllerPreferencesBundle cameraControllerPreferencesBundle);
	public CameraControllerPreferencesBundle getAllParams();
	
	public SourceRuntimeParams getSourceParams();
	public void setSourceParams(final SourceRuntimeParams sourceParams);
	
	public MetadataRuntimeParams getMetadataParams();
	public void setMetadataParams(final MetadataRuntimeParams metadataParams);
	
	public LocalViewProcessor getLocalViewProcessor();
}

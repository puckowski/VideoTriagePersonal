package com.keypointforensics.videotriage.gui.main.controller;

import com.keypointforensics.videotriage.params.BackgroundRuntimeParams;
import com.keypointforensics.videotriage.params.BlobRuntimeParams;
import com.keypointforensics.videotriage.params.MassRuntimeParams;
import com.keypointforensics.videotriage.params.MetadataRuntimeParams;
import com.keypointforensics.videotriage.params.SourceRuntimeParams;
import com.keypointforensics.videotriage.params.StatusBarRuntimeParams;
import com.keypointforensics.videotriage.params.WriteRuntimeParams;

public class CameraControllerPreferencesBundle {

	/*
	 * Author: Daniel Puckowski
	 */
	
	public MassRuntimeParams       mMassParams;
	public BlobRuntimeParams       mBlobParams;
	public WriteRuntimeParams      mWriteParams;
	public StatusBarRuntimeParams  mStatusBarParams;
	public BackgroundRuntimeParams mBackgroundParams;
	public SourceRuntimeParams     mSourceParams;
	public MetadataRuntimeParams   mMetadataParams;
	public int                     mRotateDegrees;
		
	public CameraControllerPreferencesBundle(final MassRuntimeParams massParams, 
			final WriteRuntimeParams writeParams, final BackgroundRuntimeParams backgroundParams, 
			final BlobRuntimeParams blobParams, final StatusBarRuntimeParams statusBarParams, final SourceRuntimeParams sourceParams, 
			final MetadataRuntimeParams metadataParams, final int currentRotateDegrees) {
		mMassParams       = massParams;
		mWriteParams      = writeParams;
		mBackgroundParams = backgroundParams;
		mBlobParams       = blobParams;
		mStatusBarParams  = statusBarParams;
		mRotateDegrees    = currentRotateDegrees;
		mSourceParams     = sourceParams;
		mMetadataParams   = metadataParams;
	}
	
	public CameraControllerPreferencesBundle(final CameraControllerPreferencesBundle cameraControllerPreferencesBundle) {
		mMassParams       = cameraControllerPreferencesBundle.mMassParams;
		mWriteParams      = cameraControllerPreferencesBundle.mWriteParams;
		mBackgroundParams = cameraControllerPreferencesBundle.mBackgroundParams;
		mBlobParams       = cameraControllerPreferencesBundle.mBlobParams;
		mStatusBarParams  = cameraControllerPreferencesBundle.mStatusBarParams;
		mRotateDegrees    = cameraControllerPreferencesBundle.mRotateDegrees;
		mSourceParams     = cameraControllerPreferencesBundle.mSourceParams;
		mMetadataParams   = cameraControllerPreferencesBundle.mMetadataParams;
	}
}

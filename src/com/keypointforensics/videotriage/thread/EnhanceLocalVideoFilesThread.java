package com.keypointforensics.videotriage.thread;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.keypointforensics.videotriage.casestore.CaseMetadataWriter;
import com.keypointforensics.videotriage.gui.localfile.wizard.video.EnhanceLocalFileWizardWindow;
import com.keypointforensics.videotriage.gui.localfile.wizard.video.FailedToProcessListWindow;
import com.keypointforensics.videotriage.legacy.UtilsLegacy;
import com.keypointforensics.videotriage.legacy.WindowsVideoFrameExtractorLegacy;
import com.keypointforensics.videotriage.progress.ProgressBundle;
import com.keypointforensics.videotriage.util.CursorUtils;
import com.keypointforensics.videotriage.util.FileUtils;
import com.keypointforensics.videotriage.util.ProgressUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.video.EVideoTriageVideoFilter;
import com.keypointforensics.videotriage.video.WindowsVideoFilterCenteredSimplePostProcess;
import com.keypointforensics.videotriage.video.WindowsVideoFilterColorNegative;
import com.keypointforensics.videotriage.video.WindowsVideoFilterDarken;
import com.keypointforensics.videotriage.video.WindowsVideoFilterDeblockDeringPostProcess;
import com.keypointforensics.videotriage.video.WindowsVideoFilterDeinterlace;
import com.keypointforensics.videotriage.video.WindowsVideoFilterDeinterlaceQuality;
import com.keypointforensics.videotriage.video.WindowsVideoFilterDeshake;
import com.keypointforensics.videotriage.video.WindowsVideoFilterHistogramEqualization;
import com.keypointforensics.videotriage.video.WindowsVideoFilterIncreaseContrast;
import com.keypointforensics.videotriage.video.WindowsVideoFilterLighten;
import com.keypointforensics.videotriage.video.WindowsVideoFilterLinearContrast;
import com.keypointforensics.videotriage.video.WindowsVideoFilterMediumContrast;
import com.keypointforensics.videotriage.video.WindowsVideoFilterNegative;
import com.keypointforensics.videotriage.video.WindowsVideoFilterNormalize;
import com.keypointforensics.videotriage.video.WindowsVideoFilterSimplePostProcess;
import com.keypointforensics.videotriage.video.WindowsVideoFilterStrongContrast;
import com.keypointforensics.videotriage.video.WindowsVideoFilterTemporalAveragingDenoiser;

public class EnhanceLocalVideoFilesThread extends Thread {

	/*
	 * Author: Daniel Puckowski
	 */
	
	private final EnhanceLocalFileWizardWindow ENHANCE_LOCAL_FILE_WIZARD_WINDOW;
	private final ArrayList<String>            VIDEO_FILE_PATH_LIST;
	private final EVideoTriageVideoFilter      VIDEO_TRIAGE_VIDEO_FILTER_ENUM;
	
	private String mContextFilename;
	
	private int mVideoFrameTarget;
	private int mVideoFrameProgress;
	private ProgressBundle mProgressBundle;
	
	public EnhanceLocalVideoFilesThread(final EnhanceLocalFileWizardWindow enhanceLocalFileWizardWindow,
			final ArrayList<String> videoFilePathList, final EVideoTriageVideoFilter videoTriageVideoFilterEnum) {
		ENHANCE_LOCAL_FILE_WIZARD_WINDOW = enhanceLocalFileWizardWindow;
		VIDEO_FILE_PATH_LIST             = videoFilePathList;
		VIDEO_TRIAGE_VIDEO_FILTER_ENUM   = videoTriageVideoFilterEnum;
	}
	
	public EnhanceLocalVideoFilesThread(final EnhanceLocalFileWizardWindow enhanceLocalFileWizardWindow, final String contextFilename,
			final ArrayList<String> videoFilePathList, final EVideoTriageVideoFilter videoTriageVideoFilterEnum) {
		ENHANCE_LOCAL_FILE_WIZARD_WINDOW = enhanceLocalFileWizardWindow;
		VIDEO_FILE_PATH_LIST             = videoFilePathList;
		VIDEO_TRIAGE_VIDEO_FILTER_ENUM   = videoTriageVideoFilterEnum;
		
		mContextFilename = contextFilename;
	}
	
	public static void performOpenEnhancedVideoFolderAction() {
		final File file = new File(FileUtils.ENHANCED_DIRECTORY);
		final Desktop desktop = Desktop.getDesktop();
		
		try {
			desktop.browse(file.toURI());
		} catch (IOException ioException) {

		}
	}
	
	private void displayOpenEnhancedVideoFolderDialog(final String title, final String message) {
		final int openEnhancedVideoFolderSelection = UtilsLegacy.displayConfirmDialog(title, message);
		
		if(openEnhancedVideoFolderSelection == JOptionPane.OK_OPTION) {
			performOpenEnhancedVideoFolderAction();
		}
	}
	
	private void performHighQualityDeinterlaceVideoAction(final String absoluteVideoFilePath) throws InterruptedException {
		Thread deinterlaceQualityVideoThread = new Thread() {
			@Override
			public void run() {
				ThreadUtils.addThreadToHandleList("EnhanceLocalVideo DeinterlaceQualityVideo", this);
				
				//String absoluteVideoPath = FileUtils.performSelectVideoFileAction();
				
				WindowsVideoFilterDeinterlaceQuality windowsVideoFilterDeinterlaceQuality = new WindowsVideoFilterDeinterlaceQuality();
				//ProgressBundle progressBundle = null;
				
				try {
					//progressBundle = ProgressUtils.getIndeterminateProgressBundle("Stabilizing Video");
					windowsVideoFilterDeinterlaceQuality.apply(absoluteVideoFilePath, mVideoFrameProgress, mProgressBundle);
					mVideoFrameProgress += windowsVideoFilterDeinterlaceQuality.getNumberOfFramesProcessed();
				} catch (InterruptedException interruptedException) {
					//interruptedException.printStackTrace();
				}
				
				//if(progressBundle != null) {
				//	progressBundle.frame.dispose();
				//}
								
				ThreadUtils.removeThreadFromHandleList(this);
			}
		};
		
		deinterlaceQualityVideoThread.start();
		deinterlaceQualityVideoThread.join();
	}
	
	private void performDeinterlaceVideoAction(final String absoluteVideoFilePath) throws InterruptedException {
		Thread deinterlaceVideoThread = new Thread() {
			@Override
			public void run() {
				ThreadUtils.addThreadToHandleList("EnhanceLocalVideo DeinterlaceVideo", this);
				
				//String absoluteVideoPath = FileUtils.performSelectVideoFileAction();
				
				WindowsVideoFilterDeinterlace windowsVideoFilterDeinterlace = new WindowsVideoFilterDeinterlace();
				//ProgressBundle progressBundle = null;
				
				try {
					//progressBundle = ProgressUtils.getIndeterminateProgressBundle("Stabilizing Video");
					windowsVideoFilterDeinterlace.apply(absoluteVideoFilePath, mVideoFrameProgress, mProgressBundle);
					mVideoFrameProgress += windowsVideoFilterDeinterlace.getNumberOfFramesProcessed();
				} catch (InterruptedException interruptedException) {
					//interruptedException.printStackTrace();
				}
				
				//if(progressBundle != null) {
				//	progressBundle.frame.dispose();
				//}
								
				ThreadUtils.removeThreadFromHandleList(this);
			}
		};
		
		deinterlaceVideoThread.start();
		deinterlaceVideoThread.join();
	}
	
	private void performStabilizeVideoAction(final String absoluteVideoFilePath) throws InterruptedException {
		Thread stabilizeVideoThread = new Thread() {
			@Override
			public void run() {
				ThreadUtils.addThreadToHandleList("EnhanceLocalVideo StabilizeVideo", this);
				
				//String absoluteVideoPath = FileUtils.performSelectVideoFileAction();
				
				WindowsVideoFilterDeshake windowsVideoFilterDeshake = new WindowsVideoFilterDeshake();
				//ProgressBundle progressBundle = null;
				
				try {
					//progressBundle = ProgressUtils.getIndeterminateProgressBundle("Stabilizing Video");
					windowsVideoFilterDeshake.apply(absoluteVideoFilePath, mVideoFrameProgress, mProgressBundle);
					mVideoFrameProgress += windowsVideoFilterDeshake.getNumberOfFramesProcessed();
				} catch (InterruptedException interruptedException) {
					//interruptedException.printStackTrace();
				}
				
				//if(progressBundle != null) {
				//	progressBundle.frame.dispose();
				//}
								
				ThreadUtils.removeThreadFromHandleList(this);
			}
		};
		
		stabilizeVideoThread.start();
		stabilizeVideoThread.join();
	}
	
	private void performDarkerVideoAction(final String absoluteVideoFilePath) throws InterruptedException {
		Thread darkerVideoThread = new Thread() {
			@Override
			public void run() {
				ThreadUtils.addThreadToHandleList("EnhanceLocalVideo DarkenVideo", this);
								
				WindowsVideoFilterDarken windowsVideoFilterDarken = new WindowsVideoFilterDarken();
				
				try {
					windowsVideoFilterDarken.apply(absoluteVideoFilePath, mVideoFrameProgress, mProgressBundle);
					mVideoFrameProgress += windowsVideoFilterDarken.getNumberOfFramesProcessed();
				} catch (InterruptedException interruptedException) {
					//interruptedException.printStackTrace();
				}
				
				ThreadUtils.removeThreadFromHandleList(this);
			}
		};
		
		darkerVideoThread.start();
		darkerVideoThread.join();
	}
	
	private void performLighterVideoAction(final String absoluteVideoFilePath) throws InterruptedException {
		Thread lightenVideoThread = new Thread() {
			@Override
			public void run() {
				ThreadUtils.addThreadToHandleList("EnhanceLocalVideo LightenVideo", this);
								
				WindowsVideoFilterLighten windowsVideoFilterLighten = new WindowsVideoFilterLighten();
				
				try {
					windowsVideoFilterLighten.apply(absoluteVideoFilePath, mVideoFrameProgress, mProgressBundle);
					mVideoFrameProgress += windowsVideoFilterLighten.getNumberOfFramesProcessed();
				} catch (InterruptedException interruptedException) {
					//interruptedException.printStackTrace();
				}
				
				ThreadUtils.removeThreadFromHandleList(this);
			}
		};
		
		lightenVideoThread.start();
		lightenVideoThread.join();
	}
	
	private void performColorNegativeVideoAction(final String absoluteVideoFilePath) throws InterruptedException {
		Thread colorNegativeVideoThread = new Thread() {
			@Override
			public void run() {
				ThreadUtils.addThreadToHandleList("EnhanceLocalVideo ColorNegativeVideo", this);
								
				WindowsVideoFilterColorNegative windowsVideoFilterColorNegative = new WindowsVideoFilterColorNegative();
				
				try {
					windowsVideoFilterColorNegative.apply(absoluteVideoFilePath, mVideoFrameProgress, mProgressBundle);
					mVideoFrameProgress += windowsVideoFilterColorNegative.getNumberOfFramesProcessed();
				} catch (InterruptedException interruptedException) {
					//interruptedException.printStackTrace();
				}
				
				ThreadUtils.removeThreadFromHandleList(this);
			}
		};
		
		colorNegativeVideoThread.start();
		colorNegativeVideoThread.join();
	}
	
	private void performNegativeVideoAction(final String absoluteVideoFilePath) throws InterruptedException {
		Thread negativeVideoThread = new Thread() {
			@Override
			public void run() {
				ThreadUtils.addThreadToHandleList("EnhanceLocalVideo NegativeVideo", this);
								
				WindowsVideoFilterNegative windowsVideoFilterNegative = new WindowsVideoFilterNegative();
				
				try {
					windowsVideoFilterNegative.apply(absoluteVideoFilePath, mVideoFrameProgress, mProgressBundle);
					mVideoFrameProgress += windowsVideoFilterNegative.getNumberOfFramesProcessed();
				} catch (InterruptedException interruptedException) {
					//interruptedException.printStackTrace();
				}
				
				ThreadUtils.removeThreadFromHandleList(this);
			}
		};
		
		negativeVideoThread.start();
		negativeVideoThread.join();
	}
	
	private void performStrongContrastVideoAction(final String absoluteVideoFilePath) throws InterruptedException {
		Thread strongContrastVideoThread = new Thread() {
			@Override
			public void run() {
				ThreadUtils.addThreadToHandleList("EnhanceLocalVideo StrongContrastVideo", this);
								
				WindowsVideoFilterStrongContrast windowsVideoFilterStrongContrast = new WindowsVideoFilterStrongContrast();
				
				try {
					windowsVideoFilterStrongContrast.apply(absoluteVideoFilePath, mVideoFrameProgress, mProgressBundle);
					mVideoFrameProgress += windowsVideoFilterStrongContrast.getNumberOfFramesProcessed();
				} catch (InterruptedException interruptedException) {
					//interruptedException.printStackTrace();
				}
				
				ThreadUtils.removeThreadFromHandleList(this);
			}
		};
		
		strongContrastVideoThread.start();
		strongContrastVideoThread.join();
	}
	
	private void performHistogramEqualizationVideoAction(final String absoluteVideoFilePath) throws InterruptedException {
		Thread histogramEqualizationVideoThread = new Thread() {
			@Override
			public void run() {
				ThreadUtils.addThreadToHandleList("EnhanceLocalVideo HistogramEqualizationVideo", this);
								
				WindowsVideoFilterHistogramEqualization windowsVideoFilterHistogramEqualization = new WindowsVideoFilterHistogramEqualization();
				
				try {
					windowsVideoFilterHistogramEqualization.apply(absoluteVideoFilePath, mVideoFrameProgress, mProgressBundle);
					mVideoFrameProgress += windowsVideoFilterHistogramEqualization.getNumberOfFramesProcessed();
				} catch (InterruptedException interruptedException) {
					//interruptedException.printStackTrace();
				}
				
				ThreadUtils.removeThreadFromHandleList(this);
			}
		};
		
		histogramEqualizationVideoThread.start();
		histogramEqualizationVideoThread.join();
	}
	
	private void performIncreaseContrastVideoAction(final String absoluteVideoFilePath) throws InterruptedException {
		Thread increaseContrastVideoThread = new Thread() {
			@Override
			public void run() {
				ThreadUtils.addThreadToHandleList("EnhanceLocalVideo IncreaseContrastVideo", this);
								
				WindowsVideoFilterIncreaseContrast windowsVideoFilterIncreaseContrast = new WindowsVideoFilterIncreaseContrast();
				
				try {
					windowsVideoFilterIncreaseContrast.apply(absoluteVideoFilePath, mVideoFrameProgress, mProgressBundle);
					mVideoFrameProgress += windowsVideoFilterIncreaseContrast.getNumberOfFramesProcessed();
				} catch (InterruptedException interruptedException) {
					//interruptedException.printStackTrace();
				}
				
				ThreadUtils.removeThreadFromHandleList(this);
			}
		};
		
		increaseContrastVideoThread.start();
		increaseContrastVideoThread.join();
	}
	
	private void performLinearContrastVideoAction(final String absoluteVideoFilePath) throws InterruptedException {
		Thread linearContrastVideoThread = new Thread() {
			@Override
			public void run() {
				ThreadUtils.addThreadToHandleList("EnhanceLocalVideo LinearContrastVideo", this);
								
				WindowsVideoFilterLinearContrast windowsVideoFilterLinearContrast = new WindowsVideoFilterLinearContrast();
				
				try {
					windowsVideoFilterLinearContrast.apply(absoluteVideoFilePath, mVideoFrameProgress, mProgressBundle);
					mVideoFrameProgress += windowsVideoFilterLinearContrast.getNumberOfFramesProcessed();
				} catch (InterruptedException interruptedException) {
					//interruptedException.printStackTrace();
				}
				
				ThreadUtils.removeThreadFromHandleList(this);
			}
		};
		
		linearContrastVideoThread.start();
		linearContrastVideoThread.join();
	}
	
	private void performMediumContrastVideoAction(final String absoluteVideoFilePath) throws InterruptedException {
		Thread mediumContrastVideoThread = new Thread() {
			@Override
			public void run() {
				ThreadUtils.addThreadToHandleList("EnhanceLocalVideo MediumContrastVideo", this);
								
				WindowsVideoFilterMediumContrast windowsVideoFilterMediumContrast = new WindowsVideoFilterMediumContrast();
				
				try {
					windowsVideoFilterMediumContrast.apply(absoluteVideoFilePath, mVideoFrameProgress, mProgressBundle);
					mVideoFrameProgress += windowsVideoFilterMediumContrast.getNumberOfFramesProcessed();
				} catch (InterruptedException interruptedException) {
					//interruptedException.printStackTrace();
				}
				
				ThreadUtils.removeThreadFromHandleList(this);
			}
		};
		
		mediumContrastVideoThread.start();
		mediumContrastVideoThread.join();
	}
	
	private void performNormalizeVideoAction(final String absoluteVideoFilePath) throws InterruptedException {
		Thread normalizeVideoThread = new Thread() {
			@Override
			public void run() {
				ThreadUtils.addThreadToHandleList("EnhanceLocalVideo NormalizeVideo", this);
								
				WindowsVideoFilterNormalize windowsVideoFilterNormalize = new WindowsVideoFilterNormalize();
				
				try {
					windowsVideoFilterNormalize.apply(absoluteVideoFilePath, mVideoFrameProgress, mProgressBundle);
					mVideoFrameProgress += windowsVideoFilterNormalize.getNumberOfFramesProcessed();
				} catch (InterruptedException interruptedException) {
					//interruptedException.printStackTrace();
				}
				
				ThreadUtils.removeThreadFromHandleList(this);
			}
		};
		
		normalizeVideoThread.start();
		normalizeVideoThread.join();
	}
	
	private void performSimplePostProcessVideoAction(final String absoluteVideoFilePath) throws InterruptedException {
		Thread simplePostProcessVideoThread = new Thread() {
			@Override
			public void run() {
				ThreadUtils.addThreadToHandleList("EnhanceLocalVideo SimplePostProcessVideo", this);
								
				WindowsVideoFilterSimplePostProcess windowsVideoFilterSimplePostProcess = new WindowsVideoFilterSimplePostProcess();
				
				try {
					windowsVideoFilterSimplePostProcess.apply(absoluteVideoFilePath, mVideoFrameProgress, mProgressBundle);
					mVideoFrameProgress += windowsVideoFilterSimplePostProcess.getNumberOfFramesProcessed();
				} catch (InterruptedException interruptedException) {
					//interruptedException.printStackTrace();
				}
				
				ThreadUtils.removeThreadFromHandleList(this);
			}
		};
		
		simplePostProcessVideoThread.start();
		simplePostProcessVideoThread.join();
	}
	
	private void performCenteredSimplePostProcessVideoAction(final String absoluteVideoFilePath) throws InterruptedException {
		Thread centeredSimplePostProcessVideoThread = new Thread() {
			@Override
			public void run() {
				ThreadUtils.addThreadToHandleList("EnhanceLocalVideo CenteredSimplePostProcessVideo", this);
								
				WindowsVideoFilterCenteredSimplePostProcess windowsVideoFilterCenteredSimplePostProcess = new WindowsVideoFilterCenteredSimplePostProcess();
				
				try {
					windowsVideoFilterCenteredSimplePostProcess.apply(absoluteVideoFilePath, mVideoFrameProgress, mProgressBundle);
					mVideoFrameProgress += windowsVideoFilterCenteredSimplePostProcess.getNumberOfFramesProcessed();
				} catch (InterruptedException interruptedException) {
					//interruptedException.printStackTrace();
				}
				
				ThreadUtils.removeThreadFromHandleList(this);
			}
		};
		
		centeredSimplePostProcessVideoThread.start();
		centeredSimplePostProcessVideoThread.join();
	}
	
	private void performDeblockDeringPostProcessVideoAction(final String absoluteVideoFilePath) throws InterruptedException {
		Thread deblockDeringPostProcessVideoThread = new Thread() {
			@Override
			public void run() {
				ThreadUtils.addThreadToHandleList("EnhanceLocalVideo DeblockDeringPostProcessVideo", this);
								
				WindowsVideoFilterDeblockDeringPostProcess windowsVideoFilterDeblockDeringPostProcess = new WindowsVideoFilterDeblockDeringPostProcess();
				
				try {
					windowsVideoFilterDeblockDeringPostProcess.apply(absoluteVideoFilePath, mVideoFrameProgress, mProgressBundle);
					mVideoFrameProgress += windowsVideoFilterDeblockDeringPostProcess.getNumberOfFramesProcessed();
				} catch (InterruptedException interruptedException) {
					//interruptedException.printStackTrace();
				}
				
				ThreadUtils.removeThreadFromHandleList(this);
			}
		};
		
		deblockDeringPostProcessVideoThread.start();
		deblockDeringPostProcessVideoThread.join();
	}
	
	private void performTemporalAveragingDenoiserVideoAction(final String absoluteVideoFilePath) throws InterruptedException {
		Thread temporalAveragingDenoiserVideoThread = new Thread() {
			@Override
			public void run() {
				ThreadUtils.addThreadToHandleList("EnhanceLocalVideo TemporalAveragingDenoiseVideo", this);
								
				WindowsVideoFilterTemporalAveragingDenoiser windowsVideoFilterTemporalAveragingDenoiser = new WindowsVideoFilterTemporalAveragingDenoiser();
				
				try {
					windowsVideoFilterTemporalAveragingDenoiser.apply(absoluteVideoFilePath, mVideoFrameProgress, mProgressBundle);
					mVideoFrameProgress += windowsVideoFilterTemporalAveragingDenoiser.getNumberOfFramesProcessed();
				} catch (InterruptedException interruptedException) {
					//interruptedException.printStackTrace();
				}
				
				ThreadUtils.removeThreadFromHandleList(this);
			}
		};
		
		temporalAveragingDenoiserVideoThread.start();
		temporalAveragingDenoiserVideoThread.join();
	}
	
	private void enhanceVideo(final String absoluteVideoFilePath) throws InterruptedException {
		if(mContextFilename != null) {
			CaseMetadataWriter.writeNewEnhancedSourceToContext(mContextFilename, absoluteVideoFilePath);
		}
		
		switch(VIDEO_TRIAGE_VIDEO_FILTER_ENUM) {
			case DESHAKE_FILTER_STANDARD: {
				performStabilizeVideoAction(absoluteVideoFilePath);
				
				break;
			}
			case LIGHTER_FILTER_STANDARD: {
				performLighterVideoAction(absoluteVideoFilePath);
				
				break;
			}
			case NEGATIVE_FILTER_STANDARD: {
				performNegativeVideoAction(absoluteVideoFilePath);
				
				break;
			}
			case STRONG_CONTRAST_FILTER_STANDARD: {
				performStrongContrastVideoAction(absoluteVideoFilePath);
				
				break;
			}
			case COLOR_NEGATIVE_FILTER_STANDARD: {
				performColorNegativeVideoAction(absoluteVideoFilePath);
				
				break;
			}
			case DARKER_FILTER_STANDARD: {
				performDarkerVideoAction(absoluteVideoFilePath);
				
				break;
			}
			case HISTOGRAM_EQUALIZATION_FILTER_STANDARD: {
				performHistogramEqualizationVideoAction(absoluteVideoFilePath);
				
				break;
			}
			case INCREASE_CONTRAST_FILTER_STANDARD: {
				performIncreaseContrastVideoAction(absoluteVideoFilePath);
				
				break;
			}
			case LINEAR_CONTRAST_FILTER_STANDARD: {
				performLinearContrastVideoAction(absoluteVideoFilePath);
				
				break;
			}
			case MEDIUM_CONTRAST_FILTER_STANDARD: {
				performMediumContrastVideoAction(absoluteVideoFilePath);
				
				break;
			}
			case NORMALIZE_FILTER_STANDARD: {
				performNormalizeVideoAction(absoluteVideoFilePath);
				
				break;
			}
			case SIMPLE_POST_PROCESS_FILTER_STANDARD: {
				performSimplePostProcessVideoAction(absoluteVideoFilePath);
				
				break;
			}
			case TEMPORAL_AVERAGING_DENOISER_FILTER_STANDARD: {
				performTemporalAveragingDenoiserVideoAction(absoluteVideoFilePath);
				
				break;
			}
			case DEBLOCK_DERING_POST_PROCESS_FILTER_STANDARD: {
				performDeblockDeringPostProcessVideoAction(absoluteVideoFilePath);
				
				break;
			}
			case SIMPLE_POST_PROCESS_FILTER_CENTERED: {
				performCenteredSimplePostProcessVideoAction(absoluteVideoFilePath);
				
				break;
			}
			case DEINTERLACE_FILTER_STANDARD: {
				performDeinterlaceVideoAction(absoluteVideoFilePath);
				
				break;
			}
			case DEINTERLACE_FILTER_HIGH_QUALITY: {
				performHighQualityDeinterlaceVideoAction(absoluteVideoFilePath);
				
				break;
			}
		}
	}
	
	private void buildAndDisplayFailedToProcessDialog(ArrayList<String> listOfFailedAbsolutePaths) {
		FailedToProcessListWindow failedToProcessListWindow = new FailedToProcessListWindow(listOfFailedAbsolutePaths);
		failedToProcessListWindow.buildAndDisplay();
	}
	
	@Override
	public void start() {
		run();
	}
	
	@Override
	public void run() {
		ThreadUtils.addThreadToHandleList("EnhanceLocalVideo Run", this);
		
		CursorUtils.setBusyCursor(ENHANCE_LOCAL_FILE_WIZARD_WINDOW);
		
		//ProgressBundle deleteProgressBundle = null;
		final int videoFilePathListSize = VIDEO_FILE_PATH_LIST.size();
		
		//if(videoFilePathListSize > 1) {
		//	deleteProgressBundle = ProgressUtils.getProgressBundle("Enhance Videos Progress...", VIDEO_FILE_PATH_LIST.size());
		//}
			
		int failedToProcessCount = 0;
		ArrayList<String> failedToProcessPathList = new ArrayList<String>();
				
		mVideoFrameTarget = 0;
		mVideoFrameProgress = 0;
		
		for(String absoluteVideoFilePath : VIDEO_FILE_PATH_LIST) {
			mVideoFrameTarget += WindowsVideoFrameExtractorLegacy.getVideoFrameCount(absoluteVideoFilePath);
		}
		
		if(videoFilePathListSize > 1) {
			mProgressBundle = ProgressUtils.getProgressBundle("Enhance Videos Progress...", mVideoFrameTarget);
		} else {
			mProgressBundle = ProgressUtils.getProgressBundle("Enhance Video Progress...", mVideoFrameTarget);
		}
		
		for(String absoluteVideoFilePath : VIDEO_FILE_PATH_LIST) {
			try {
				enhanceVideo(absoluteVideoFilePath);
			} catch (InterruptedException interruptedException) {
				//interruptedException.printStackTrace();
				
				failedToProcessCount++;
				failedToProcessPathList.add(absoluteVideoFilePath);
			}
			
			//if(deleteProgressBundle != null) {
			//	deleteProgressBundle.progressBar.setValue(deleteProgressBundle.progressBar.getValue() + 1);
			//	deleteProgressBundle.progressBar.repaint();
			//}
		}
		
		//if(deleteProgressBundle != null) {
		//	deleteProgressBundle.progressBar.setValue(deleteProgressBundle.progressBar.getValue() + 1);
		//	deleteProgressBundle.progressBar.repaint();
		//	deleteProgressBundle.frame.dispose();
		//}
		
		mProgressBundle.progressBar.repaint();
		mProgressBundle.frame.dispose();
		
		if(failedToProcessCount > 0) {
			buildAndDisplayFailedToProcessDialog(failedToProcessPathList);
		}
		
		CursorUtils.setDefaultCursor(ENHANCE_LOCAL_FILE_WIZARD_WINDOW);
		
		ThreadUtils.removeThreadFromHandleList(this);
		
		if(videoFilePathListSize > 1) {
			displayOpenEnhancedVideoFolderDialog("Finished Enhancing Videos", "Open enhanced video folder?");
		} else {
			displayOpenEnhancedVideoFolderDialog("Finished Enhancing Video", "Open enhanced video folder?");
		}
	}
}

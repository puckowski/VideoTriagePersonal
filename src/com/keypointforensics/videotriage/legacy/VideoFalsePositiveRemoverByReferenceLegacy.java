package com.keypointforensics.videotriage.legacy;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

import javax.imageio.ImageIO;

import com.keypointforensics.videotriage.image.match.ORBFeatureExtractor;
import com.keypointforensics.videotriage.image.match.ORBMatcher;
import com.keypointforensics.videotriage.image.match.OrbKeypoint;
import com.keypointforensics.videotriage.progress.ProgressBundle;
import com.keypointforensics.videotriage.util.ImageUtils;
import com.keypointforensics.videotriage.util.ProgressUtils;

public class VideoFalsePositiveRemoverByReferenceLegacy implements FrsThreadLegacy {

	/*
	 * Author: Daniel Puckowski
	 */

	public final static int DEFAULT_MINIMUM_IMAGES_TRIGGER = 100;

	public final static double DEFAULT_FALSE_POSITIVE_THRESHOLD         = 10;
	public final static double DEFAULT_ORB_MATCH_PERCENT	= 15.0;
	public final static int    DEFAULT_IMAGE_COMPARE_SCALE_SIZE         = 152;

	private VideoFalsePositiveBundleLegacy mFilenames;

	private int mNumberComputed;

	private double mCurrentFalsePositiveThreshold;
	private int mCurrentMinimumImagesTrigger;
	private double mCurrentImageSimilarityThreshold;
	private double mCurrentImageDifferenceThreshold;
	private double mCurrentImageOrbSimilarity;

	final VideoFalsePositiveBundleLegacy mVideoFalsePositiveBundle;

	private ArrayList<VideoFrameBundleLegacy> mVideoFrameBundleLegacys;

	private static final int MAX_CACHE_SIZE = 50;
	private final Map<String, VideoFrameBundleLegacy> mVideoBundleCache =
			new LinkedHashMap<String, VideoFrameBundleLegacy>(MAX_CACHE_SIZE, 0.75f, true) {
				@Override
				protected boolean removeEldestEntry(Map.Entry<String, VideoFrameBundleLegacy> eldest) {
					return size() > MAX_CACHE_SIZE;
				}
			};

	public VideoFalsePositiveRemoverByReferenceLegacy(final String path) {
		mFilenames = null;
		mNumberComputed = 0;

		mVideoFalsePositiveBundle = new VideoFalsePositiveBundleLegacy(path);

		mCurrentFalsePositiveThreshold   = DEFAULT_FALSE_POSITIVE_THRESHOLD;
		mCurrentMinimumImagesTrigger     = DEFAULT_MINIMUM_IMAGES_TRIGGER;
		mCurrentImageDifferenceThreshold = ImageUtilsLegacy.DEFAULT_IMAGE_DIFFERENCE_SIMILAR_PERCENTAGE;
		mCurrentImageSimilarityThreshold = ImageUtilsLegacy.DEFAULT_IMAGE_SSIM_SIMILAR_PERCENTAGE;
		mCurrentImageOrbSimilarity      = DEFAULT_ORB_MATCH_PERCENT;
	}

	public VideoFalsePositiveRemoverByReferenceLegacy(final String path, VideoFalsePositiveBundleLegacy filenames) {
		mFilenames = filenames;
		mNumberComputed = 0;

		mVideoFalsePositiveBundle = new VideoFalsePositiveBundleLegacy(path);

		mCurrentFalsePositiveThreshold   = DEFAULT_FALSE_POSITIVE_THRESHOLD;
		mCurrentMinimumImagesTrigger     = DEFAULT_MINIMUM_IMAGES_TRIGGER;
		mCurrentImageDifferenceThreshold = ImageUtilsLegacy.DEFAULT_IMAGE_DIFFERENCE_SIMILAR_PERCENTAGE;
		mCurrentImageSimilarityThreshold = ImageUtilsLegacy.DEFAULT_IMAGE_SSIM_SIMILAR_PERCENTAGE;
		mCurrentImageOrbSimilarity      = DEFAULT_ORB_MATCH_PERCENT;
	}

	public VideoFalsePositiveRemoverByReferenceLegacy(final String path, final boolean simpleRecursiveParse) {
		mFilenames = null;
		mNumberComputed = 0;

		mVideoFalsePositiveBundle = new VideoFalsePositiveBundleLegacy(path, simpleRecursiveParse);

		mCurrentFalsePositiveThreshold   = DEFAULT_FALSE_POSITIVE_THRESHOLD;
		mCurrentMinimumImagesTrigger     = DEFAULT_MINIMUM_IMAGES_TRIGGER;
		mCurrentImageDifferenceThreshold = ImageUtilsLegacy.DEFAULT_IMAGE_DIFFERENCE_SIMILAR_PERCENTAGE;
		mCurrentImageSimilarityThreshold = ImageUtilsLegacy.DEFAULT_IMAGE_SSIM_SIMILAR_PERCENTAGE;
		mCurrentImageOrbSimilarity      = DEFAULT_ORB_MATCH_PERCENT;
	}

	public VideoFalsePositiveRemoverByReferenceLegacy(final String path, VideoFalsePositiveBundleLegacy filenames, final boolean simpleRecursiveParse) {
		mFilenames = filenames;
		mNumberComputed = 0;

		mVideoFalsePositiveBundle = new VideoFalsePositiveBundleLegacy(path, simpleRecursiveParse);

		mCurrentFalsePositiveThreshold   = DEFAULT_FALSE_POSITIVE_THRESHOLD;
		mCurrentMinimumImagesTrigger     = DEFAULT_MINIMUM_IMAGES_TRIGGER;
		mCurrentImageDifferenceThreshold = ImageUtilsLegacy.DEFAULT_IMAGE_DIFFERENCE_SIMILAR_PERCENTAGE;
		mCurrentImageSimilarityThreshold = ImageUtilsLegacy.DEFAULT_IMAGE_SSIM_SIMILAR_PERCENTAGE;
		mCurrentImageOrbSimilarity      = DEFAULT_ORB_MATCH_PERCENT;
	}

	public VideoFalsePositiveRemoverByReferenceLegacy(final ArrayList<String> listOfImages) {
		mFilenames = null;
		mNumberComputed = 0;

		mVideoFalsePositiveBundle = new VideoFalsePositiveBundleLegacy(listOfImages);

		mCurrentFalsePositiveThreshold   = DEFAULT_FALSE_POSITIVE_THRESHOLD;
		mCurrentMinimumImagesTrigger     = DEFAULT_MINIMUM_IMAGES_TRIGGER;
		mCurrentImageDifferenceThreshold = ImageUtilsLegacy.DEFAULT_IMAGE_DIFFERENCE_SIMILAR_PERCENTAGE;
		mCurrentImageSimilarityThreshold = ImageUtilsLegacy.DEFAULT_IMAGE_SSIM_SIMILAR_PERCENTAGE;
		mCurrentImageOrbSimilarity      = DEFAULT_ORB_MATCH_PERCENT;
	}

	public double getCurrentImageOrbSimilarity() {
		return mCurrentImageOrbSimilarity;
	}

	public void setCurrentImageOrbSimilarity(final double newImageOrbSimilarity) {
		mCurrentImageOrbSimilarity = newImageOrbSimilarity;
	}

	public double getCurrentFalsePositiveThreshold() {
		return mCurrentFalsePositiveThreshold;
	}

	public int getCurrentMinimumImagesTrigger() {
		return mCurrentMinimumImagesTrigger;
	}

	public double getCurrentImageDifferenceThreshold()
	{
		return mCurrentImageDifferenceThreshold;
	}

	public double getCurrentImageSimilarityThreshold()
	{
		return mCurrentImageSimilarityThreshold;
	}

	public void setCurrentFalsePositiveThreshold(final double newFalsePositiveThreshold) {
		mCurrentFalsePositiveThreshold = newFalsePositiveThreshold;
	}

	public void setCurrentMinimumImagesTrigger(final int newMinimumImagesTrigger) {
		mCurrentMinimumImagesTrigger = newMinimumImagesTrigger;
	}

	public void setCurrentImageSimilarityThreshold(final double newImageSimilarityThreshold)
	{
		mCurrentImageSimilarityThreshold = (newImageSimilarityThreshold / 100.0);
	}

	public void setCurrentImageDifferenceThreshold(final double newImageDifferenceThreshold)
	{
		mCurrentImageDifferenceThreshold = newImageDifferenceThreshold;
	}

	public void resetCurrentImageOrbSimilarity() {
		mCurrentImageOrbSimilarity = DEFAULT_ORB_MATCH_PERCENT;
	}

	public void resetCurrentFalsePositiveThreshold() {
		mCurrentFalsePositiveThreshold = DEFAULT_FALSE_POSITIVE_THRESHOLD;
	}

	public void resetCurrentMinimumImagesTrigger() {
		mCurrentMinimumImagesTrigger = DEFAULT_MINIMUM_IMAGES_TRIGGER;
	}

	public void resetCurrentImageSimilarityThreshold() {
		mCurrentImageSimilarityThreshold = ImageUtilsLegacy.DEFAULT_IMAGE_SSIM_SIMILAR_PERCENTAGE;
	}

	public void resetCurrentImageDifferenceThreshold() {
		mCurrentImageDifferenceThreshold = ImageUtilsLegacy.DEFAULT_IMAGE_DIFFERENCE_SIMILAR_PERCENTAGE;
	}

	public void resetCurrentImageOrbThreshold() {
		mCurrentImageOrbSimilarity = DEFAULT_ORB_MATCH_PERCENT;
	}

	public VideoFalsePositiveBundleLegacy getFalsePositiveBundle() {
		return mVideoFalsePositiveBundle;
	}

	public void removeFalsePositives(ArrayList<String> filenamesToDelete,
			final boolean deleteSelf) throws IOException {
		final ArrayList<String> filenames = mVideoFalsePositiveBundle.getCaptureFilenames();
		final ArrayList<String> falsePositives = mVideoFalsePositiveBundle.getFalsePositiveFilenames();

		if(filenamesToDelete == null || filenamesToDelete.isEmpty() == true) { //Nothing to process...
			return;
		}

		buildVideoFrameBundles();

		ArrayList<VideoFrameBundleLegacy> bundlesToRemove = new ArrayList<VideoFrameBundleLegacy>();
		VideoFrameBundleLegacy videoBundle = null;

		ProgressBundle progressBundle = null;

		if(deleteSelf == true) {
			progressBundle = ProgressUtils.getProgressBundle("Mark All Progress...", mVideoFrameBundleLegacys.size() * filenamesToDelete.size());
		}
		else {
			progressBundle = ProgressUtils.getProgressBundle("Mark All But One Progress...", mVideoFrameBundleLegacys.size() * filenamesToDelete.size());
		}

		for(String filename : filenamesToDelete) {
			for(int i = 0; i < mVideoFrameBundleLegacys.size(); ++i) {
				if(mVideoFrameBundleLegacys.get(i).getFilename().equalsIgnoreCase(filename) == false) {
					//progressBundle.progressBar.setValue(progressBundle.progressBar.getValue() + 1);
					//progressBundle.progressBar.repaint();

					continue;
				}

				bundlesToRemove.clear();
				videoBundle = mVideoFrameBundleLegacys.get(i);

				if(deleteSelf == true) {
					bundlesToRemove.add(videoBundle);
				}

				if (videoBundle.getImage() == null) {
					continue;
				}

				final List<OrbKeypoint> keypoints = ORBFeatureExtractor.getKeypointsForImage(ImageUtils.getGrayscaleArray(videoBundle.getImage()));

				for(int n = 0; n < mVideoFrameBundleLegacys.size(); ++n) {
					progressBundle.progressBar.setValue(progressBundle.progressBar.getValue() + 1);
					progressBundle.progressBar.repaint();

					if(n == i) {
						continue;
					}

					if (mVideoFrameBundleLegacys.get(n).getImage() == null) {
						continue;
					}

					final List<OrbKeypoint> compareKeypoints = ORBFeatureExtractor.getKeypointsForImage(ImageUtils.getGrayscaleArray(mVideoFrameBundleLegacys.get(n).getImage()));
					final List<ORBMatcher.Match> matches = ORBMatcher.match(keypoints, compareKeypoints);

					if(((double) matches.size() / (double) keypoints.size()) >= mCurrentImageOrbSimilarity) {
						bundlesToRemove.add(mVideoFrameBundleLegacys.get(n));
					}
					else if(ImageUtilsLegacy.getImagesPercentageDifference(videoBundle.getImage(), mVideoFrameBundleLegacys.get(n).getImage(), DEFAULT_IMAGE_COMPARE_SCALE_SIZE) <= mCurrentImageDifferenceThreshold) {
						bundlesToRemove.add(mVideoFrameBundleLegacys.get(n));
					}
					else if(ImageUtilsLegacy.getImagesSimilarityScore(videoBundle.getImage(), mVideoFrameBundleLegacys.get(n).getImage(), DEFAULT_IMAGE_COMPARE_SCALE_SIZE) >= mCurrentImageSimilarityThreshold) {
						bundlesToRemove.add(mVideoFrameBundleLegacys.get(n));
					}

					//progressBundle.progressBar.setValue(progressBundle.progressBar.getValue() + 1);
					//progressBundle.progressBar.repaint();
				}
			}
		}

		progressBundle.progressBar.setIndeterminate(true);
		progressBundle.progressBar.repaint();

		for(int t = 0; t < bundlesToRemove.size(); ++t) {
			videoBundle = bundlesToRemove.get(t);

			falsePositives.add(videoBundle.getFilename());
			filenames.remove(videoBundle.getFilename());

			mVideoFrameBundleLegacys.remove(bundlesToRemove.get(t));
		}

		progressBundle.frame.dispose();
	}

	public void removeFalsePositives(ArrayList<String> filenamesToDelete,
			final boolean deleteSelf, int numberOfFilesToProcess) throws IOException {
		final ArrayList<String> filenames = mVideoFalsePositiveBundle.getCaptureFilenames();
		final ArrayList<String> falsePositives = mVideoFalsePositiveBundle.getFalsePositiveFilenames();

		if(filenamesToDelete == null || filenamesToDelete.isEmpty() == true) {
			return;
		}

		buildVideoFrameBundles();

		ArrayList<VideoFrameBundleLegacy> bundlesToRemove = new ArrayList<VideoFrameBundleLegacy>();
		VideoFrameBundleLegacy videoBundle = null;

		ProgressBundle progressBundle = null;

		if(deleteSelf == true) {
			progressBundle = ProgressUtils.getProgressBundle("Mark All Progress...", mVideoFrameBundleLegacys.size() * filenamesToDelete.size());
		}
		else {
			progressBundle = ProgressUtils.getProgressBundle("Mark All But One Progress...", mVideoFrameBundleLegacys.size() * filenamesToDelete.size());
		}

		for(String filename : filenamesToDelete) {
			for(int i = 0; i < mVideoFrameBundleLegacys.size(); ++i) {
				if(mVideoFrameBundleLegacys.get(i).getFilename().equalsIgnoreCase(filename) == false) {
					//progressBundle.progressBar.setValue(progressBundle.progressBar.getValue() + 1);
					//progressBundle.progressBar.repaint();

					continue;
				}

				bundlesToRemove.clear();
				videoBundle = mVideoFrameBundleLegacys.get(i);

				if(deleteSelf == true) {
					bundlesToRemove.add(videoBundle);
				}

				if (videoBundle.getImage() == null) {
					continue;
				}

				final List<OrbKeypoint> keypoints = ORBFeatureExtractor.getKeypointsForImage(ImageUtils.getGrayscaleArray(videoBundle.getImage()));

				for(int n = i; n < (i + numberOfFilesToProcess) && n < mVideoFrameBundleLegacys.size(); ++n) {
					progressBundle.progressBar.setValue(progressBundle.progressBar.getValue() + 1);
					progressBundle.progressBar.repaint();

					if(n == i) {
						continue;
					}

					if (mVideoFrameBundleLegacys.get(n).getImage() == null) {
						continue;
					}

					final List<OrbKeypoint> compareKeypoints = ORBFeatureExtractor.getKeypointsForImage(ImageUtils.getGrayscaleArray(mVideoFrameBundleLegacys.get(n).getImage()));
					final List<ORBMatcher.Match> matches = ORBMatcher.match(keypoints, compareKeypoints);

					if(((double) matches.size() / (double) keypoints.size()) >= mCurrentImageOrbSimilarity) {
						bundlesToRemove.add(mVideoFrameBundleLegacys.get(n));

						numberOfFilesToProcess++;
					}
					else if(ImageUtilsLegacy.getImagesPercentageDifference(videoBundle.getImage(), mVideoFrameBundleLegacys.get(n).getImage(), DEFAULT_IMAGE_COMPARE_SCALE_SIZE) <= mCurrentImageDifferenceThreshold) {
						bundlesToRemove.add(mVideoFrameBundleLegacys.get(n));

						numberOfFilesToProcess++;
					}
					else if(ImageUtilsLegacy.getImagesSimilarityScore(videoBundle.getImage(), mVideoFrameBundleLegacys.get(n).getImage(), DEFAULT_IMAGE_COMPARE_SCALE_SIZE) >= mCurrentImageSimilarityThreshold) {
						bundlesToRemove.add(mVideoFrameBundleLegacys.get(n));

						numberOfFilesToProcess++;
					}

					//progressBundle.progressBar.setValue(progressBundle.progressBar.getValue() + 1);
					//progressBundle.progressBar.repaint();
				}
			}
		}

		progressBundle.progressBar.setIndeterminate(true);
		progressBundle.progressBar.repaint();

		for(int t = 0; t < bundlesToRemove.size(); ++t) {
			videoBundle = bundlesToRemove.get(t);

			falsePositives.add(videoBundle.getFilename());
			filenames.remove(videoBundle.getFilename());

			mVideoFrameBundleLegacys.remove(bundlesToRemove.get(t));
		}

		progressBundle.frame.dispose();
	}

	public void removeFalsePositive(String filename,
			final boolean deleteSelf) throws IOException {
		final ArrayList<String> filenames = mVideoFalsePositiveBundle.getCaptureFilenames();
		final ArrayList<String> falsePositives = mVideoFalsePositiveBundle.getFalsePositiveFilenames();

		if(filenames == null || filenames.isEmpty() == true) { //Nothing to process...
			return;
		}

		buildVideoFrameBundles();

		ArrayList<VideoFrameBundleLegacy> bundlesToRemove = new ArrayList<VideoFrameBundleLegacy>();
		VideoFrameBundleLegacy videoBundle = null;

		for(int i = 0; i < mVideoFrameBundleLegacys.size(); ++i) {
			if(mVideoFrameBundleLegacys.get(i).getFilename().equalsIgnoreCase(filename) == false) {
				continue;
			}

			//bundlesToRemove.clear();
			videoBundle = mVideoFrameBundleLegacys.get(i);

			if(deleteSelf == true) {
				bundlesToRemove.add(videoBundle);
			}

			if (videoBundle.getImage() == null) {
				continue;
			}

			final List<OrbKeypoint> keypoints = ORBFeatureExtractor.getKeypointsForImage(ImageUtils.getGrayscaleArray(videoBundle.getImage()));

			for(int n = 0; n < mVideoFrameBundleLegacys.size(); ++n) {
				if(n == i) {
					continue;
				}

				if (mVideoFrameBundleLegacys.get(n).getImage() == null) {
					continue;
				}

				final List<OrbKeypoint> compareKeypoints = ORBFeatureExtractor.getKeypointsForImage(ImageUtils.getGrayscaleArray(mVideoFrameBundleLegacys.get(n).getImage()));
				final List<ORBMatcher.Match> matches = ORBMatcher.match(keypoints, compareKeypoints);

				if(((double) matches.size() / (double) keypoints.size()) >= mCurrentImageOrbSimilarity) {
					bundlesToRemove.add(mVideoFrameBundleLegacys.get(n));
				}
				else if(ImageUtilsLegacy.getImagesPercentageDifference(videoBundle.getImage(), mVideoFrameBundleLegacys.get(n).getImage(), DEFAULT_IMAGE_COMPARE_SCALE_SIZE) <= mCurrentImageDifferenceThreshold) {
					bundlesToRemove.add(mVideoFrameBundleLegacys.get(n));
				}
				else if(ImageUtilsLegacy.getImagesSimilarityScore(videoBundle.getImage(), mVideoFrameBundleLegacys.get(n).getImage(), DEFAULT_IMAGE_COMPARE_SCALE_SIZE) >= mCurrentImageSimilarityThreshold) {
					bundlesToRemove.add(mVideoFrameBundleLegacys.get(n));
				}
			}

			for(int t = 0; t < bundlesToRemove.size(); ++t) {
				videoBundle = bundlesToRemove.get(t);

				falsePositives.add(videoBundle.getFilename());
				filenames.remove(videoBundle.getFilename());

				mVideoFrameBundleLegacys.remove(bundlesToRemove.get(t));
			}

			break;
		}
	}

    private VideoFrameBundleLegacy getCachedVideoBundle(final String fname) throws IOException {
        VideoFrameBundleLegacy videoBundleLegacy = mVideoBundleCache.get(fname);

        if (videoBundleLegacy == null) {
            final BufferedImage img = ImageIO.read(new File(fname));

            if (img == null) {
                return null;
            }

            videoBundleLegacy = new VideoFrameBundleLegacy(fname, img);

            mVideoBundleCache.put(fname, videoBundleLegacy);
        }

        return videoBundleLegacy;
    }

	public void removeFalsePositive(String filename,
			final boolean deleteSelf, int numberOfFilesToProcess) throws IOException {
		final ArrayList<String> filenames = mVideoFalsePositiveBundle.getCaptureFilenames();
		final ArrayList<String> falsePositives = mVideoFalsePositiveBundle.getFalsePositiveFilenames();

		if(filenames == null || filenames.isEmpty() == true) {
			return;
		}

		//buildVideoFrameBundles();

		ArrayList<VideoFrameBundleLegacy> bundlesToRemove = new ArrayList<VideoFrameBundleLegacy>();
		VideoFrameBundleLegacy videoBundle, compareBundle = null;

		for(int i = 0; i < filenames.size(); ++i) {
			if(filenames.get(i).equalsIgnoreCase(filename) == false) {
				continue;
			}

			videoBundle = new VideoFrameBundleLegacy(filenames.get(i), ImageIO.read(new File(filenames.get(i))));

			if(deleteSelf == true) {
				bundlesToRemove.add(videoBundle);
			}

			if (videoBundle.getImage() == null) {
				continue;
			}

			final List<OrbKeypoint> keypoints = ORBFeatureExtractor.getKeypointsForImage(ImageUtils.getGrayscaleArray(videoBundle.getImage()));

			for(int n = i; n < (i + numberOfFilesToProcess) && n < filenames.size(); ++n) {
				if(n == i) {
					continue;
				}

                compareBundle = getCachedVideoBundle(filenames.get(n));

				if (compareBundle.getImage() == null) {
					continue;
				}

				final List<OrbKeypoint> compareKeypoints = ORBFeatureExtractor.getKeypointsForImage(ImageUtils.getGrayscaleArray(compareBundle.getImage()));
				final List<ORBMatcher.Match> matches = ORBMatcher.matchWithoutCrossCheck(keypoints, compareKeypoints);

				if(((double) matches.size() / (double) keypoints.size()) >= mCurrentImageOrbSimilarity) {
					bundlesToRemove.add(compareBundle);

					numberOfFilesToProcess++;
				}
				else if(ImageUtilsLegacy.getImagesPercentageDifference(videoBundle.getImage(), compareBundle.getImage(), DEFAULT_IMAGE_COMPARE_SCALE_SIZE) <= mCurrentImageDifferenceThreshold) {
					bundlesToRemove.add(compareBundle);

					numberOfFilesToProcess++;
				}
				else if(ImageUtilsLegacy.getImagesSimilarityScore(videoBundle.getImage(), compareBundle.getImage(), DEFAULT_IMAGE_COMPARE_SCALE_SIZE) >= mCurrentImageSimilarityThreshold) {
					bundlesToRemove.add(compareBundle);

					numberOfFilesToProcess++;
				}
			}

			for(int t = 0; t < bundlesToRemove.size(); ++t) {
				videoBundle = bundlesToRemove.get(t);

				falsePositives.add(videoBundle.getFilename());
				filenames.remove(videoBundle.getFilename());
			}

			break;
		}

        mVideoBundleCache.clear();
	}

	public void buildVideoFrameBundles() throws IOException {
		if(mVideoFrameBundleLegacys != null) {
			return;
		}

		ArrayList<String> filenames = mVideoFalsePositiveBundle.getCaptureFilenames();
		filenames = SearchUtilsLegacy.formatResultList(filenames);

		mVideoFrameBundleLegacys = new ArrayList<VideoFrameBundleLegacy>(filenames.size());
		VideoFrameBundleLegacy videoBundle = null;

		for(int i = 0; i < filenames.size(); ++i) {
			videoBundle = new VideoFrameBundleLegacy(filenames.get(i), ImageIO.read(new File(filenames.get(i))));
			mVideoFrameBundleLegacys.add(videoBundle);
		}
	}

	@Override
	public void run() {
		boolean success = true;

		if(mFilenames != null && mFilenames.getCaptureFilenames().size() > 0) {

		}

		if(success == true) {

		}
	}

	@Override
	public void cleanup() {
		if (mFilenames != null) {
			mFilenames.getCaptureFilenames().clear();
			mFilenames.getFalsePositiveFilenames().clear();
			mFilenames = null;
		}

		mNumberComputed = 0;
	}

	@Override
	public int getObjectCount() {
		return mFilenames.getCaptureFilenames().size();
	}

	@Override
	public int numberOfObjectsComputed() {
		return mNumberComputed;
	}

	@Override
	public boolean hasFinishedComputing() {
		if(mFilenames == null) {
			return true;
		}

		if(mNumberComputed == mFilenames.getCaptureFilenames().size()) {
			return true;
		} else {
			return false;
		}
	}
}

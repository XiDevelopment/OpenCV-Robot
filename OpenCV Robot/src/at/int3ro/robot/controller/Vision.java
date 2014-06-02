package at.int3ro.robot.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.util.Log;
import at.int3ro.robot.model.DetectedObject;

public class Vision {
	private static final String TAG = "RobotVision";

	private static Vision instance = null;

	public static Vision getInstance() {
		if (instance == null)
			instance = new Vision();
		return instance;
	}

	// thresholds for color filtering
	private int thresholdH = 5;
	private int thresholdS = 65;
	private int minV = 25;
	private int maxV = 255;

	// Executor
	ExecutorService executor;

	// Homography Matrix
	Mat mHomography = null;

	private Vision() {
		executor = Executors.newFixedThreadPool(4);
	}

	private class ContoursCallable implements Callable<List<DetectedObject>> {
		Mat mat;
		Scalar color;

		public ContoursCallable(Mat mat, Scalar color) {
			this.mat = mat;
			this.color = color;
		}

		@Override
		public List<DetectedObject> call() throws Exception {
			return getObjectByColor(mat, color);
		}
	}

	public List<DetectedObject> getObjectByColorThreaded(Mat mat,
			List<Scalar> colors) {
		List<FutureTask<List<DetectedObject>>> tasks = new ArrayList<FutureTask<List<DetectedObject>>>();
		for (Scalar s : colors) {
			ContoursCallable callable = new ContoursCallable(mat, s);
			FutureTask<List<DetectedObject>> newTask = new FutureTask<List<DetectedObject>>(
					callable);
			tasks.add(newTask);
			executor.execute(newTask);
		}

		List<DetectedObject> result = new ArrayList<DetectedObject>();
		for (FutureTask<List<DetectedObject>> t : tasks) {
			try {
				result.addAll(t.get());
			} catch (InterruptedException e) {
				Log.e(TAG, e.getMessage());
				e.printStackTrace();
			} catch (ExecutionException e) {
				Log.e(TAG, e.getMessage());
				e.printStackTrace();
			}
		}

		return result;
	}

	/**
	 * Filters a given Mat in RGBA by a Color in HSV (180�) space and returns
	 * the found contours
	 * 
	 * @param mat
	 *            the mat to filter (RGBA Colorspace)
	 * @param color
	 *            the color to filter (Scalar (H, S, V))
	 * @return list of contours
	 */
	public List<DetectedObject> getObjectByColor(Mat mat, Scalar color) {
		Mat mRgba = mat.clone();

		// Calculate lower and upper color bound
		Scalar upperBound = new Scalar(color.val[0] + thresholdH, color.val[1]
				+ thresholdS, maxV);
		Scalar lowerBound = new Scalar(color.val[0] - thresholdH, color.val[1]
				- thresholdS, minV);

		// Convert to HSV Color Space
		Imgproc.cvtColor(mRgba, mRgba, Imgproc.COLOR_RGB2HSV);

		// Faster processing, also blurs the image
		Imgproc.pyrDown(mRgba, mRgba);
		Imgproc.pyrDown(mRgba, mRgba);

		// Thresholding
		Core.inRange(mRgba, lowerBound, upperBound, mRgba);

		// Opening -> Removing small objects
		Imgproc.erode(mRgba, mRgba, Imgproc.getStructuringElement(
				Imgproc.MORPH_ELLIPSE, new Size(3, 3)));
		Imgproc.dilate(mRgba, mRgba, Imgproc.getStructuringElement(
				Imgproc.MORPH_ELLIPSE, new Size(3, 3)));

		// Closing -> Make a nice ellipse
		Imgproc.dilate(mRgba, mRgba, Imgproc.getStructuringElement(
				Imgproc.MORPH_ELLIPSE, new Size(5, 5)));
		Imgproc.erode(mRgba, mRgba, Imgproc.getStructuringElement(
				Imgproc.MORPH_ELLIPSE, new Size(5, 5)));

		// Imgproc.pyrUp(mRgba, mRgba);
		// Imgproc.pyrUp(mRgba, mRgba);

		// Get Contours
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(mRgba, contours, new Mat(), Imgproc.RETR_LIST,
				Imgproc.CHAIN_APPROX_SIMPLE);

		List<DetectedObject> result = new ArrayList<DetectedObject>();
		for (MatOfPoint contour : contours) {
			Core.multiply(contour, new Scalar(4, 4), contour);
			result.add(new DetectedObject(contour, color));
			contour.release(); // release, not needed anymore
		}

		// Release allocated Mat
		mRgba.release();

		return result;
	}

	/**
	 * Calculates a point given a homography matrix
	 * 
	 * @param to
	 *            Point to transform
	 * @param homography
	 *            Homography Matrix
	 * @return real world point or null if no homography is set
	 */
	public Point calculateHomographyPoint(Point to, Mat homography) {
		if (homography != null) {
			// temporary mats
			Mat mTo = new MatOfPoint2f(to);
			Mat mResult = new Mat();

			// transform to scene point
			Core.perspectiveTransform(mTo, mResult, homography);

			// get point from Mat
			Point result = new Point();
			result.x = mResult.get(0, 0)[0];
			result.y = mResult.get(0, 0)[1];

			// fix robot offset for middle inside robot
			result.y += 100.0;

			// fix homography precision
			result.x = correctHomographyDistance(result.x);
			result.y = correctHomographyDistance(result.y);

			// release temporary
			mTo.release();
			mResult.release();

			return result;
		} else {
			return null;
		}
	}

	private double correctHomographyDistance(double x) {
		return 63.1866 + 0.701716 * x + 0.000460812 * x * x;
	}

	/**
	 * Calculates a point given a homography matrix
	 * 
	 * @param to
	 *            Point to transform
	 * @return real world point or null if no homography is set
	 */
	public Point calculateHomographyPoint(Point to) {
		return calculateHomographyPoint(to, mHomography);
	}

	/**
	 * Gets a color from the average of a 10x10 rectangle at exactly the middle
	 * of the image
	 * 
	 * @param mRgba
	 *            the image
	 * @return the color
	 */
	public Scalar getColorFromImage(Mat mRgba) {
		Mat mHsv = new Mat();
		Imgproc.cvtColor(mRgba, mHsv, Imgproc.COLOR_RGB2HSV);

		Mat subMat = mHsv.submat(mHsv.height() / 2 - 5, mHsv.height() / 2 + 5,
				mHsv.width() / 2 - 5, mHsv.width() / 2 + 5);

		Scalar color = Core.mean(subMat);

		subMat.release();
		mHsv.release();

		return color;
	}

	/**
	 * threshold of Hue of HSV filtering
	 * 
	 * @return the thresholdH
	 */
	public int getThresholdH() {
		return thresholdH;
	}

	/**
	 * threshold of Hue of HSV filtering
	 * 
	 * @param thresholdH
	 *            the thresholdH to set
	 */
	public void setThresholdH(int thresholdH) {
		this.thresholdH = thresholdH;
	}

	/**
	 * threshold of Saturation of HSV filtering
	 * 
	 * @return the thresholdS
	 */
	public int getThresholdS() {
		return thresholdS;
	}

	/**
	 * threshold of Saturation of HSV filtering
	 * 
	 * @param thresholdS
	 *            the thresholdS to set
	 */
	public void setThresholdS(int thresholdS) {
		this.thresholdS = thresholdS;
	}

	/**
	 * Min Value for HSV filtering
	 * 
	 * @return the minV
	 */
	public int getMinV() {
		return minV;
	}

	/**
	 * Min Value for HSV filtering
	 * 
	 * @param minV
	 *            the minV to set
	 */
	public void setMinV(int minV) {
		this.minV = minV;
	}

	/**
	 * Max Value for HSV filtering
	 * 
	 * @return the maxV
	 */
	public int getMaxV() {
		return maxV;
	}

	/**
	 * Max Value for HSV filtering
	 * 
	 * @param maxV
	 *            the maxV to set
	 */
	public void setMaxV(int maxV) {
		this.maxV = maxV;
	}

	/**
	 * @return the homography matrix
	 */
	public Mat getHomography() {
		return mHomography;
	}

	/**
	 * @param homography
	 *            the homography matrix to set
	 */
	public void setHomography(Mat homography) {
		this.mHomography = homography;
	}

	/**
	 * Calculate Homography Matrix
	 * 
	 * @param mRgba
	 * @return the Homography Matrix
	 */
	public boolean calculateHomographyMatrix(Mat mRgba) {
		Mat gray = new Mat();
		final Size mPatternSize = new Size(6, 9);
		MatOfPoint2f mCorners, RealWorldC;
		mCorners = new MatOfPoint2f();
		boolean mPatternWasFound = false;

		// defining real world coordinates
		RealWorldC = new MatOfPoint2f(new Point(-42.6f, 304.9f), new Point(
				-42.6f, 316.8f), new Point(-42.6f, 328.7f), new Point(-42.6f,
				340.6f), new Point(-42.6f, 352.5f), new Point(-42.6f, 364.4f),
				new Point(-30.7f, 304.9f), new Point(-30.7f, 316.8f),
				new Point(-30.7f, 328.7f), new Point(-30.7f, 340.6f),
				new Point(-30.7f, 352.5f), new Point(-30.7f, 364.4f),
				new Point(-18.8f, 304.9f), new Point(-18.8f, 316.8f),
				new Point(-18.8f, 328.7f), new Point(-18.8f, 340.6f),
				new Point(-18.8f, 352.5f), new Point(-18.8f, 364.4f),
				new Point(-6.9f, 304.9f), new Point(-6.9f, 316.8f), new Point(
						-6.9f, 328.7f), new Point(-6.9f, 340.6f), new Point(
						-6.9f, 352.5f), new Point(-6.9f, 364.4f), new Point(
						5.0f, 304.9f), new Point(5.0f, 316.8f), new Point(5.0f,
						328.7f), new Point(5.0f, 340.6f), new Point(5.0f,
						352.5f), new Point(5.0f, 364.4f), new Point(16.9f,
						304.9f), new Point(16.9f, 316.8f), new Point(16.9f,
						328.7f), new Point(16.9f, 340.6f), new Point(16.9f,
						352.5f), new Point(16.9f, 364.4f), new Point(28.8f,
						304.9f), new Point(28.8f, 316.8f), new Point(28.8f,
						328.7f), new Point(28.8f, 340.6f), new Point(28.8f,
						352.5f), new Point(28.8f, 364.4f), new Point(40.7f,
						304.9f), new Point(40.7f, 316.8f), new Point(40.7f,
						328.7f), new Point(40.7f, 340.6f), new Point(40.7f,
						352.5f), new Point(40.7f, 364.4f), new Point(52.5f,
						304.9f), new Point(52.5f, 316.8f), new Point(52.5f,
						328.7f), new Point(52.5f, 340.6f), new Point(52.5f,
						352.5f), new Point(52.5f, 364.4f));

		Imgproc.cvtColor(mRgba, gray, Imgproc.COLOR_RGBA2GRAY);
		// getting inner corners of chessboard
		List<Mat> mCornersBuffer = new ArrayList<Mat>();
		mPatternWasFound = Calib3d.findChessboardCorners(gray, mPatternSize,
				mCorners);

		if (mPatternWasFound) {
			// Calib3d.drawChessboardCorners(mRgb, mPatternSize, mCorners,
			// mPatternWasFound);// for testing
			mCornersBuffer.add(mCorners.clone());
			mHomography = Calib3d.findHomography(mCorners, RealWorldC);
		} else {
			// if no homography found, return null
			mHomography = null;
		}

		// Releasing
		if (gray != null)
			gray.release();
		if (mCorners != null)
			mCorners.release();
		if (RealWorldC != null)
			RealWorldC.release();
		for (Mat c : mCornersBuffer)
			if (c != null)
				c.release();

		return mPatternWasFound;
	}
}

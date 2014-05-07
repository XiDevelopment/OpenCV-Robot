package at.int3ro.robot;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui; // Exercise 1.1
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import at.int3ro.robot.controller.MoveFacade;
import at.int3ro.robot.model.BeaconBounds;
import at.int3ro.robot.model.ColorBounds;
import at.int3ro.robot.model.ContourExtremePoints;

public class RobotActivity extends Activity implements CvCameraViewListener2,
		OnTouchListener {
	private static final String TAG = "Robot::Activity";

	private RobotCamera mOpenCvCameraView;
	private TextView mStatusTextView;

	private MenuItem mItemSavePicture = null;
	private MenuItem mItemGetHomographyMatrix = null;
	private MenuItem mItemObjectColor = null;
	private MenuItem mItemAddBeaconColor = null;
	private MenuItem mItemClearBeaconColor = null;

	private Mat mRgb;
	private Mat mTreshhold;
	private Mat mHomography;
	private List<MatOfPoint> mContours;

	private List<ColorBounds> objectColors;
	private List<ColorBounds> beaconColors;

	private boolean showThresholdImage = false;

	private boolean objectColorSelect = false;
	private boolean beaconColorSelect = false;

	Toast toast;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
				mOpenCvCameraView.enableView();
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	public void setStatus(String status) {
		mStatusTextView.setText(status);
	}

	public RobotActivity() {
		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.robot_surface_view);

		mOpenCvCameraView = (RobotCamera) findViewById(R.id.robot_activity_java_surface_view);
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);
		mOpenCvCameraView.setOnTouchListener(this);

		objectColors = new ArrayList<ColorBounds>();
		beaconColors = new ArrayList<ColorBounds>();

		mStatusTextView = (TextView) findViewById(R.id.status_text_view);

		MoveFacade.getInstance().setContext(this);

		// TODO COLOR RED
		Scalar upper = new Scalar(20, 255, 255);
		Scalar lower = new Scalar(-5, 155, 80);
		ColorBounds color = new ColorBounds(upper, lower);
		beaconColors.add(color);

		// TODO COLOR YELLOW
		upper = new Scalar(60, 255, 255);
		lower = new Scalar(35, 105, 155);
		color = new ColorBounds(upper, lower);
		beaconColors.add(color);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);
	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i(TAG, "called onCreateOptionsMenu");
		mItemSavePicture = menu.add("Save Picture");
		mItemGetHomographyMatrix = menu.add("Get Homography Matrix");
		mItemObjectColor = menu.add("Object Color");
		mItemAddBeaconColor = menu.add("add Beacon Color");
		mItemClearBeaconColor = menu.add("clear Beacon Colors");

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

		if (item == mItemSavePicture) { // Exercise 1.1
			String msg; // for Toast

			// Set path to Pictures Directory on SD-Card
			File path = Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
			path.mkdirs(); // Create Directories if not present
			File file = new File(path, "exercise1.jpeg"); // Create file
			String filename = file.toString(); // Get Filename

			// Set Image Quality
			MatOfInt quality = new MatOfInt(Highgui.CV_IMWRITE_JPEG_QUALITY, 90);

			// Write File
			Mat mBgrTemp = new Mat(mRgb.height(), mRgb.width(), mRgb.type());
			// Convert RGB to BGR
			Imgproc.cvtColor(mRgb, mBgrTemp, Imgproc.COLOR_RGB2BGR);
			if (Highgui.imwrite(filename, mBgrTemp, quality)) {
				msg = "Image saved!";
				Log.i(TAG, "Image saved!");
			} else {
				msg = "Error while saving!";
				Log.e(TAG, "Error while saving!");
			}

			// Show Toast
			Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
			toast.show();
		} else if (item == mItemGetHomographyMatrix) {
			if (mHomography != null)
				mHomography.release();
			mHomography = getHomographyMatrix(mRgb);
		} else if (item == mItemObjectColor) {
			objectColors.clear();
			objectColorSelect = true;
		} else if (item == mItemAddBeaconColor) {
			beaconColorSelect = true;
			beaconColors.clear();
			Toast.makeText(this, "Start with Lower color of 1 beacon",
					Toast.LENGTH_SHORT).show();
		} else if (item == mItemClearBeaconColor) {
			beaconColors.clear();
			beaconColorSelect = false;
		}

		return true;
	}

	public void onCameraViewStarted(int width, int height) {
		mRgb = new Mat();
		mTreshhold = new Mat();
		mContours = new ArrayList<MatOfPoint>();
	}

	public void onCameraViewStopped() {
		// Release Mats
		mRgb.release();
		mTreshhold.release();

		if (mHomography != null)
			mHomography.release();
	}

	public List<ContourExtremePoints> getContoursByColor(Scalar lowerBound,
			Scalar upperBound) {
		Imgproc.cvtColor(mRgb, mTreshhold, Imgproc.COLOR_RGB2HSV_FULL);

		// Pyrdown (processes faster)
		Imgproc.pyrDown(mTreshhold, mTreshhold);
		Imgproc.pyrDown(mTreshhold, mTreshhold);

		// Threshold
		Core.inRange(mTreshhold, lowerBound, upperBound, mTreshhold);

		// Apply Filters
		Imgproc.GaussianBlur(mTreshhold, mTreshhold, new Size(3, 3), 0);
		Imgproc.dilate(mTreshhold, mTreshhold, Imgproc.getStructuringElement(
				Imgproc.MORPH_ELLIPSE, new Size(2, 2)));
		Imgproc.erode(mTreshhold, mTreshhold, Imgproc.getStructuringElement(
				Imgproc.MORPH_ELLIPSE, new Size(4, 4)));

		// Get Contours
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(mTreshhold, contours, new Mat(),
				Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

		// Find max contour area
		double maxArea = 0;
		Iterator<MatOfPoint> each = contours.iterator();
		while (each.hasNext()) {
			MatOfPoint wrapper = each.next();
			double area = Imgproc.contourArea(wrapper);
			if (area > maxArea)
				maxArea = area;
		}

		// Filter contours by area and resize to fit the original image size
		mContours.clear();
		for (MatOfPoint contour : contours) {
			if (Imgproc.contourArea(contour) > 0.5 * maxArea) {
				// undo PyrDown
				Core.multiply(contour, new Scalar(4, 4), contour);
				mContours.add(contour);
			}
		}

		// Find Extreme Points for each Contour
		ArrayList<ContourExtremePoints> extremePoints = new ArrayList<ContourExtremePoints>();
		for (MatOfPoint contour : mContours)
			extremePoints.add(new ContourExtremePoints(contour));

		return extremePoints;
	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		// Runtime rt = Runtime.getRuntime();
		// if (rt.freeMemory() < 10000000)
		// rt.gc();

		mRgb = inputFrame.rgba();

		List<ContourExtremePoints> objectContours = new ArrayList<ContourExtremePoints>();

		for (ColorBounds color : objectColors) {
			// Get Contours
			objectContours.addAll(getContoursByColor(color.getLowerBound(),
					color.getUpperBound()));
		}

		/*
		 * List<ContourExtremePoints> beaconContours = new
		 * ArrayList<ContourExtremePoints>(); try { for (int i = 0; i <
		 * beaconColors.size(); i++) { // Get Contours
		 * beaconContours.addAll(getContoursByColor(beaconColors.get(i)
		 * .getLowerBound(), beaconColors.get(i).getUpperBound())); Log.i(TAG +
		 * " onFrame", beaconColors.get(i).getLowerBound() + "x" +
		 * beaconColors.get(i).getUpperBound()); } } catch (Exception e) {
		 * Log.e("Error", Log.getStackTraceString(e)); }
		 */

		// draw Contour
		for (ContourExtremePoints contourExtremePoint : objectContours) {
			// Draw Contour
			List<MatOfPoint> contour = new ArrayList<MatOfPoint>();
			contour.add(contourExtremePoint.getContour());
			Imgproc.drawContours(mRgb, contour, -1, new Scalar(0, 0, 255));

			// Draw Circles on Extreme Points on each Contour
			Core.circle(mRgb, contourExtremePoint.getTop(), 3, new Scalar(255,
					0, 0), 3);
			Core.circle(mRgb, contourExtremePoint.getTop(), 3, new Scalar(255,
					0, 0), 3);
			Core.circle(mRgb, contourExtremePoint.getRight(), 5, new Scalar(
					255, 255, 0), 5);
			Core.circle(mRgb, contourExtremePoint.getLeft(), 3, new Scalar(255,
					0, 0), 3);

			// Draw Rectangle around each Contour
			Point leftTop = new Point(contourExtremePoint.getLeft().x,
					contourExtremePoint.getTop().y);
			Point bottomRight = new Point(contourExtremePoint.getRight().x,
					contourExtremePoint.getBottom().y);
			Core.rectangle(mRgb, leftTop, bottomRight,
					new Scalar(255, 255, 255));
		}

		// draw Beacons
		/*
		 * for (ContourExtremePoints contourExtremePoint : beaconContours) { //
		 * Draw Contour List<MatOfPoint> contour = new ArrayList<MatOfPoint>();
		 * contour.add(contourExtremePoint.getContour());
		 * Imgproc.drawContours(mRgb, contour, -1, new Scalar(0, 0, 255));
		 * 
		 * // Draw Circles on Extreme Points on each Contour Core.circle(mRgb,
		 * contourExtremePoint.getRight(), 2, new Scalar( 255, 0, 0), 2);
		 * 
		 * // Draw Rectangle around each Contour Point leftTop = new
		 * Point(contourExtremePoint.getLeft().x,
		 * contourExtremePoint.getTop().y); Point bottomRight = new
		 * Point(contourExtremePoint.getRight().x,
		 * contourExtremePoint.getBottom().y); Core.rectangle(mRgb, leftTop,
		 * bottomRight, new Scalar(0, 255, 255)); }
		 */

		// Calculate distances from bottom center to every bottommost point
		Point origin = new Point(mRgb.width() / 2, mRgb.height());
		StringBuilder sb = new StringBuilder();
		if (mHomography != null) {
			for (int i = 0; i < objectContours.size(); i++) {
				Point realWorldPoint = calculateHomographyPoint(objectContours
						.get(i).getBottom());

				double x = realWorldPoint.x - origin.x;
				double y = origin.y - realWorldPoint.y;

				sb.append("Object" + i + "-> x:" + Math.round(x) + "\ty: "
						+ Math.round(y) + "\n");
			}
		}
		final String distances = sb.toString();

		// Set text
		mStatusTextView.post(new Runnable() {
			public void run() {
				mStatusTextView.setText("Tracking ON\nObjects Tracked: "
						+ mContours.size() + "\n" + distances);
			}
		});

		if (objectColorSelect) {
			int size = 50;
			Point topLeft = new Point(mRgb.width() / 2 - size, mRgb.height()
					/ 2 - size);
			Point bottomRight = new Point(mRgb.width() / 2 + size,
					mRgb.height() / 2 + size);
			Core.rectangle(mRgb, topLeft, bottomRight, new Scalar(0, 255, 255),
					5);
		} else if (beaconColorSelect) {
			Scalar color1 = new Scalar(255, 0, 0);
			Scalar color2 = new Scalar(0, 255, 0);

			int size = 50;
			Point topLeft = new Point(mRgb.width() / 2 - size, mRgb.height()
					/ 2 - size);
			Point bottomRight = new Point(mRgb.width() / 2 + size,
					mRgb.height() / 2 + size);
			Core.rectangle(mRgb, topLeft, bottomRight,
					((beaconColors.size() % 2 == 0) ? color1 : color2), 5);
		}

		return mRgb;
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
			result.x = mTo.get(0, 0)[0];
			result.y = mTo.get(0, 0)[1];

			// release temporary
			mTo.release();
			mResult.release();

			return result;
		} else {
			return null;
		}
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

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// int x = (int) event.getX();
			// int y = (int) event.getY();
			// x -= 200; // View Difference on Nexus 5

			if (showThresholdImage)
				showThresholdImage = false;
			else if (objectColorSelect) {
				objectColorSelect = false;

				// Convert img to HSV
				Mat mHsv = new Mat();
				Imgproc.cvtColor(mRgb, mHsv, Imgproc.COLOR_RGB2HSV_FULL);

				Mat subMat = mHsv.submat(mHsv.height() / 2 - 40,
						mHsv.height() / 2 + 40, mHsv.width() / 2 - 40,
						mHsv.width() / 2 + 40);

				Scalar mean = Core.mean(subMat);

				// Set tolerances
				float toleranceH = 7;
				float toleranceS = 50;
				float toleranceV = 52;

				Scalar lowerBound = new Scalar(mean.val[0] - toleranceH,
						mean.val[1] - toleranceS, mean.val[2] - toleranceV);
				Scalar upperBound = new Scalar(mean.val[0] + toleranceH,
						mean.val[1] + toleranceS, mean.val[2] + toleranceV);

				objectColors.add(new ColorBounds(upperBound, lowerBound));

				subMat.release();
				mHsv.release();
			} else if (beaconColorSelect) {
				// For each beacon 2 colors (8*2)
				if (beaconColors.size() >= 16) {
					beaconColorSelect = false;
					BeaconBounds.createBounds(beaconColors);
				}

				if (beaconColors.size() > 0 && beaconColors.size() < 16) {
					if (beaconColors.size() % 2 == 0)
						Toast.makeText(
								this,
								"Lower Color of "
										+ ((beaconColors.size() / 2) + 1)
										+ " Beacon", Toast.LENGTH_SHORT).show();
					else
						Toast.makeText(
								this,
								"Upper Color of "
										+ ((beaconColors.size() / 2) + 1)
										+ " Beacon", Toast.LENGTH_SHORT).show();
				}

				// Convert img to HSV
				Mat mHsv = new Mat();
				Imgproc.cvtColor(mRgb, mHsv, Imgproc.COLOR_RGB2HSV_FULL);

				Mat subMat = mHsv.submat(mHsv.height() / 2 - 40,
						mHsv.height() / 2 + 40, mHsv.width() / 2 - 40,
						mHsv.width() / 2 + 40);

				Scalar mean = Core.mean(subMat);

				// Set tolerances
				float toleranceH = 7;
				float toleranceS = 50;
				float toleranceV = 52;

				Scalar lowerBound = new Scalar(mean.val[0] - toleranceH,
						mean.val[1] - toleranceS, mean.val[2] - toleranceV);
				Scalar upperBound = new Scalar(mean.val[0] + toleranceH,
						mean.val[1] + toleranceS, mean.val[2] + toleranceV);

				beaconColors.add(new ColorBounds(upperBound, lowerBound));

				subMat.release();
				mHsv.release();
			}
		}
		return true;
	}

	public Mat getHomographyMatrix(Mat mRgba) {
		Mat gray = new Mat();
		final Size mPatternSize = new Size(6, 9);
		MatOfPoint2f mCorners, RealWorldC;
		mCorners = new MatOfPoint2f();
		Mat homography = new Mat();
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
			homography = Calib3d.findHomography(mCorners, RealWorldC);

			toast = Toast.makeText(this, "Finished calculation successfully!",
					Toast.LENGTH_SHORT);
		} else {
			toast = Toast.makeText(this, "Couldn't find homography matrix",
					Toast.LENGTH_SHORT);

			// if no homography found, return null
			homography.release();
			homography = null;
		}
		toast.show();

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

		return homography;
	}
}

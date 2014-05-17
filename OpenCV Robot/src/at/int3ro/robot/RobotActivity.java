package at.int3ro.robot;

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
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
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
import at.int3ro.robot.model.BeaconContour;
import at.int3ro.robot.model.ColorBounds;
import at.int3ro.robot.model.ContourExtremePoints;
import at.int3ro.robot.model.RobotPosition;

// Exercise 1.1

public class RobotActivity extends Activity implements CvCameraViewListener2,
		OnTouchListener {
	private static final String TAG = "Robot::Activity";

	private RobotCamera mOpenCvCameraView;
	private TextView mStatusTextView;

	private MenuItem mItemDrive = null;
	private MenuItem mItemJustDrive = null;
	private MenuItem mItemGetHomographyMatrix = null;
	private MenuItem mItemObjectColor = null;
	private MenuItem mItemAddBeaconColor = null;
	private MenuItem mItemClearBeaconColor = null;
	private MenuItem mItemCameraCorrections = null;

	private Mat mRgb;
	private Mat mTreshhold;
	private Mat mHomography;
	private List<MatOfPoint> mContours;

	private List<ColorBounds> objectColors;
	private List<ColorBounds> beaconColors;

	private boolean showThresholdImage = false;

	private boolean objectColorSelect = false;
	private boolean beaconColorSelect = false;

	private boolean drawBeacons = false;

	private RobotPosition robotPosition = null;

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
		mItemDrive = menu.add("Drive");
		mItemJustDrive = menu.add("Just Drive");
		mItemGetHomographyMatrix = menu.add("Get Homography Matrix");
		mItemObjectColor = menu.add("Object Color");
		mItemAddBeaconColor = menu.add("add Beacon Color");
		mItemClearBeaconColor = menu.add("clear Beacon Colors");
		mItemCameraCorrections = menu.add("Camera Corrections toggle");

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

		if (item == mItemDrive) { // Exercise 1.1
			if (robotPosition != null && robotPosition.getCoords() != null) {
				Toast.makeText(this, "Driving!", Toast.LENGTH_SHORT).show();
				MoveFacade.getInstance().move(robotPosition.getCoords(),
						robotPosition.getAngle(), new Point(500, 500));

			}
		} else if (item == mItemJustDrive) {
			MoveFacade.getInstance().move(10);
			MoveFacade.getInstance().turnInPlace(90.0);
			Toast.makeText(this, "Driving!", Toast.LENGTH_SHORT).show();
		} else if (item == mItemGetHomographyMatrix) {
			if (mHomography != null)
				mHomography.release();
			mHomography = getHomographyMatrix(mRgb);
		} else if (item == mItemObjectColor) {
			objectColors.clear();
			objectColorSelect = true;
		} else if (item == mItemAddBeaconColor) {
			beaconColorSelect = true;
			drawBeacons = false;
			beaconColors.clear();
			Toast.makeText(this, "Red Color", Toast.LENGTH_SHORT).show();
		} else if (item == mItemClearBeaconColor) {
			beaconColors.clear();
			beaconColorSelect = false;
			drawBeacons = false;
		} else if (item == mItemCameraCorrections) {
			if (mOpenCvCameraView.getCameraOptimizationsOff()) {
				mOpenCvCameraView.setCameraOptimizationsOff(false);
				Toast.makeText(this, "Camera Auto Corrections are on",
						Toast.LENGTH_SHORT).show();
			} else {
				mOpenCvCameraView.setCameraOptimizationsOff(true);
				Toast.makeText(this, "Camera Auto Corrections are off",
						Toast.LENGTH_SHORT).show();
			}
		}

		return true;
	}

	public void onCameraViewStarted(int width, int height) {
		mRgb = new Mat();
		mTreshhold = new Mat();
		mContours = new ArrayList<MatOfPoint>();
		mOpenCvCameraView.setCameraOptimizationsOff(true);
	}

	public void onCameraViewStopped() {
		// Release Mats
		mRgb.release();
		mTreshhold.release();

		if (mHomography != null)
			mHomography.release();
	}

	private List<ContourExtremePoints> getContoursByColor(Scalar lowerBound,
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
			if (Imgproc.contourArea(contour) > 0.25 * maxArea) {
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

		/**
		 * Finding of object contours
		 */
		List<ContourExtremePoints> objectContours = new ArrayList<ContourExtremePoints>();
		for (ColorBounds color : objectColors) {
			// Get Contours
			objectContours.addAll(getContoursByColor(color.getLowerBound(),
					color.getUpperBound()));
		}

		/**
		 * Draw object contours
		 */
		drawContours(objectContours, new Scalar(0, 0, 255), new Scalar(255,
				255, 0));

		/**
		 * Finding of beacon contours
		 */
		// Start only when list is full and drawBeacons = true
		if (drawBeacons && BeaconBounds.getBeaconBounds() != null) {
			// CALCULATE
			List<BeaconContour> contours = new ArrayList<BeaconContour>();
			for (ColorBounds color : BeaconBounds.getColorsToFilter()) {
				List<ContourExtremePoints> c = getContoursByColor(
						color.getLowerBound(), color.getUpperBound());
				contours.add(new BeaconContour(c, color));
				drawContours(c, new Scalar(255, 255, 255),
						color.getColor(), 2);
			}
			
			for (BeaconBounds bound : BeaconBounds.getBeaconBounds()) {
				bound.calculateContours(contours);
				// drawContours(bound.getContours(), null, bound.getLowerBound()
				// .getLowerBound(), 1);
			}

		}

		/**
		 * Calculate Positions
		 */
		StringBuilder sb = new StringBuilder();

		if (BeaconBounds.getBeaconBounds() != null) {
			for (BeaconBounds b : BeaconBounds.getBeaconBounds()) {
				if (b.getBottomPoint() == null)
					continue;

				Point realWorldPoint = calculateHomographyPoint(b
						.getBottomPoint());

				if (realWorldPoint == null) {
					sb.append(b.toString() + " visible\n");
				} else {
					// double y = origin.x - realWorldPoint.x;
					// double x = (origin.y - realWorldPoint.y);
					double y = realWorldPoint.y;
					double x = realWorldPoint.x;

					double dist = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));

					sb.append(b.toString() + " :: x=" + Math.round(x) + " y="
							+ Math.round(y) + " :: dist=" + dist + "\n");
				}
			}

			/**
			 * Calculate robot world position
			 */
			List<BeaconBounds> visibleBounds = new ArrayList<BeaconBounds>();
			for (BeaconBounds b : BeaconBounds.getBeaconBounds())
				if (b.getBottomPoint() != null)
					visibleBounds.add(b);

			// Filter out bounds which are too close together
			double maxD = 0;
			int indexA = -1, indexB = -1;
			for (int i = 0; i < visibleBounds.size(); i++) {
				for (int j = 0; j < visibleBounds.size(); j++) {
					double dist = Math.abs(visibleBounds.get(i)
							.getBottomPoint().x)
							+ Math.abs(visibleBounds.get(j).getBottomPoint().x);
					if (i != j && dist > maxD) {
						maxD = dist;
						indexA = i;
						indexB = j;
					}
				}
			}

			if (visibleBounds.size() >= 2 && indexA >= 0 && indexB >= 0) {
				robotPosition = calculateRobotPosition(
						visibleBounds.get(indexA), visibleBounds.get(indexB));
			}

			if (robotPosition != null)
				sb.append("ROBOT POSITION="
						+ robotPosition.getCoords().toString() + "  ANGLE="
						+ robotPosition.getAngle() + "\n");
		}
		// }
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
			Scalar color;
			switch (beaconColors.size()) {
			case 1:
				color = new Scalar(255, 255, 0);
				break;
			case 2:
				color = new Scalar(0, 0, 255);
				break;
			case 3:
				color = new Scalar(255, 255, 255);
				break;
			default:
				color = new Scalar(255, 0, 0);
				break;
			}

			int size = 50;
			Point topLeft = new Point(mRgb.width() / 2 - size, mRgb.height()
					/ 2 - size);
			Point bottomRight = new Point(mRgb.width() / 2 + size,
					mRgb.height() / 2 + size);
			Core.rectangle(mRgb, topLeft, bottomRight, color, 5);
		}

		return mRgb;
	}

	private RobotPosition calculateRobotPosition(BeaconBounds b1,
			BeaconBounds b2) {

		if (b1 == null || b2 == null)
			return null;

		Point p1 = calculateHomographyPoint(b1.getBottomPoint());
		Point p2 = calculateHomographyPoint(b2.getBottomPoint());

		if (p1 == null || p2 == null)
			return null;

		// Distance to beacons
		double dist1 = Math.sqrt(Math.pow(p1.x, 2) + Math.pow(p1.y, 2));
		double dist2 = Math.sqrt(Math.pow(p2.x, 2) + Math.pow(p2.y, 2));

		// Distance between beacons
		double x = b2.getGlobalCoordinate().x - b1.getGlobalCoordinate().x;
		double y = b2.getGlobalCoordinate().y - b1.getGlobalCoordinate().y;
		double dist3 = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));

		double beta1 = Math
				.acos((Math.pow(dist3, 2) + Math.pow(dist1, 2) - Math.pow(
						dist2, 2)) / (2.0 * dist3 * dist1));
		// double gamma = Math
		// .acos((Math.pow(dist3, 2) + Math.pow(dist2, 2) - Math.pow(
		// dist1, 2)) / (2.0 * dist3 * dist2));
		double beta2 = Math.PI / 2 - beta1;

		int rot = 1;
		if (beta2 < 0) {
			beta2 = Math.abs(beta2);
			rot = -1;
		}

		x = dist1 * Math.sin(beta2);
		y = dist1 * Math.cos(beta2);

		Point result = new Point();
		// 1
		Log.i("calculateRobotPosition",
				"b1globalC=" + b1.getGlobalCoordinate().x + "  b2globalC="
						+ b2.getGlobalCoordinate().x + "   x=" + x);
		if (b1.getGlobalCoordinate().x == 0)
			result.x = b1.getGlobalCoordinate().x + x;
		else if (b1.getGlobalCoordinate().x == 1500)
			result.x = b1.getGlobalCoordinate().x - x;
		else if (b2.getGlobalCoordinate().x == 0)
			result.x = b1.getGlobalCoordinate().x + (rot * x);
		else if (b2.getGlobalCoordinate().x == 1500)
			result.x = b1.getGlobalCoordinate().x - (rot * x);

		// 2
		if (b1.getGlobalCoordinate().y == 0)
			result.y = b1.getGlobalCoordinate().y + y;
		else if (b1.getGlobalCoordinate().y == 1500)
			result.y = b1.getGlobalCoordinate().y - y;
		else if (b2.getGlobalCoordinate().y == 0)
			result.y = b1.getGlobalCoordinate().y + (rot * y);
		else if (b2.getGlobalCoordinate().y == 1500)
			result.y = b1.getGlobalCoordinate().y - (rot * y);

		// 3
		if ((b1.getGlobalCoordinate().x == 1500 && b2.getGlobalCoordinate().x == 1500)
				|| (b1.getGlobalCoordinate().x == 0 && b2.getGlobalCoordinate().x == 0)) {
			double temp = result.x;
			result.x = result.y;
			result.y = temp;
		}

		/**
		 * Calculation of Angle
		 */
		double angle = 270.0;

		// Log Positions
		Log.i("calculateRobotPosition", "dist1 = " + dist1);
		Log.i("calculateRobotPosition", "dist2 = " + dist2);
		Log.i("calculateRobotPosition", "dist3 = " + dist3);
		Log.i("calculateRobotPosition", "beta1 = " + beta1);
		Log.i("calculateRobotPosition", "beta2 = " + beta2);
		Log.i("calculateRobotPosition", "x = " + x);
		Log.i("calculateRobotPosition", "y = " + y);
		Log.i("calculateRobotPosition", "b1 = " + b1.getGlobalCoordinate());
		Log.i("calculateRobotPosition", "b2 = " + b2.getGlobalCoordinate());
		Log.i("calculateRobotPosition", "Result = " + result.toString());

		RobotPosition position = new RobotPosition(result, angle);

		return position;
	}

	private void drawContours(List<ContourExtremePoints> contours,
			Scalar colorContour, Scalar colorRectangle) {
		drawContours(contours, colorContour, colorRectangle, 1);
	}

	private void drawContours(List<ContourExtremePoints> contours,
			Scalar colorContour, Scalar colorRectangle, int thickness) {
		// draw Contour
		for (ContourExtremePoints contourExtremePoint : contours) {
			// Draw Contour
			if (colorContour != null) {
				List<MatOfPoint> contour = new ArrayList<MatOfPoint>();
				contour.add(contourExtremePoint.getContour());
				Imgproc.drawContours(mRgb, contour, -1, colorContour);
			}

			// Draw Circles on Extreme Points on each Contour
			// Core.circle(mRgb, contourExtremePoint.getTop(), 3, new
			// Scalar(255,
			// 0, 0), 3);
			Core.circle(mRgb, contourExtremePoint.getBottom(), 5, new Scalar(
					255, 0, 0), 3);
			// Core.circle(mRgb, contourExtremePoint.getRight(), 3, new Scalar(
			// 255, 255, 0), 5);
			Core.circle(mRgb, contourExtremePoint.getLeft(), 2, new Scalar(255,
					0, 255), 3);

			// Draw Rectangle around each Contour
			if (colorRectangle != null) {
				Point topRight = new Point(contourExtremePoint.getTop().x,
						contourExtremePoint.getRight().y);
				Point bottomLeft = new Point(contourExtremePoint.getBottom().x,
						contourExtremePoint.getLeft().y);
				Core.rectangle(mRgb, topRight, bottomLeft, colorRectangle,
						thickness);
			}
		}
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
	private Point calculateHomographyPoint(Point to, Mat homography) {
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
	private Point calculateHomographyPoint(Point to) {
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

				Scalar color = Core.mean(subMat);

				objectColors.add(new ColorBounds(color));

				for (ColorBounds c : objectColors)
					Log.i(TAG, "objectColors = " + c);

				subMat.release();
				mHsv.release();
			} else if (beaconColorSelect) {
				// Convert img to HSV
				Mat mHsv = new Mat();
				Imgproc.cvtColor(mRgb, mHsv, Imgproc.COLOR_RGB2HSV_FULL);

				Mat subMat = mHsv.submat(mHsv.height() / 2 - 40,
						mHsv.height() / 2 + 40, mHsv.width() / 2 - 40,
						mHsv.width() / 2 + 40);

				Scalar color = Core.mean(subMat);

				beaconColors.add(new ColorBounds(color));

				subMat.release();
				mHsv.release();

				if (beaconColors.size() >= 4) {
					beaconColorSelect = false;
					drawBeacons = BeaconBounds.createBounds(beaconColors);
				} else {
					// For each beacon 2 colors (8*2)
					String s = "";
					switch (beaconColors.size()) {
					case 1:
						s = "Yellow";
						break;
					case 2:
						s = "Blue";
						break;
					case 3:
						s = "White";
						break;
					}
					Toast.makeText(this, s + " Color", Toast.LENGTH_SHORT)
							.show();
				}
			}
		}
		return true;
	}

	public void onButtonThreshold(View view) {
		Log.i(TAG, "OnButtonThreshold called!");

		int h = 0, s = 0, v = 0;

		switch (view.getId()) {
		case R.id.btn_h_plus:
			h = 1;
			break;
		case R.id.btn_h_minus:
			h = -1;
			break;
		case R.id.btn_s_plus:
			s = 5;
			break;
		case R.id.btn_s_minus:
			s = -5;
			break;
		case R.id.btn_v_plus:
			v = 5;
			break;
		case R.id.btn_v_minus:
			v = -5;
			break;
		}

		if (objectColors != null)
			for (ColorBounds c : objectColors)
				c.setThreshold(new Scalar(c.getThreshold().val[0] + h, c
						.getThreshold().val[1] + s, c.getThreshold().val[2] + v));

		if (BeaconBounds.getColorsToFilter() != null)
			for (ColorBounds c : BeaconBounds.getColorsToFilter())
				c.setThreshold(new Scalar(c.getThreshold().val[0] + h, c
						.getThreshold().val[1] + s, c.getThreshold().val[2] + v));
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

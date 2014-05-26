package at.int3ro.robot;

import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import android.app.Activity;
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
import at.int3ro.robot.controller.BallController;
import at.int3ro.robot.controller.BeaconController;
import at.int3ro.robot.controller.PositionController;
import at.int3ro.robot.controller.StateMachine;
import at.int3ro.robot.controller.Vision;
import at.int3ro.robot.controller.move.MoveFacade;
import at.int3ro.robot.model.Beacon;
import at.int3ro.robot.model.DetectedBeacon;
import at.int3ro.robot.model.DetectedObject;
import at.int3ro.robot.model.RobotPosition;

// Exercise 1.1

public class RobotActivity extends Activity implements CvCameraViewListener2,
		OnTouchListener {
	private static final String TAG = "RobotActivity";
	private RobotCamera mOpenCvCameraView;

	private MenuItem mItemBall = null;
	private MenuItem mItemBlue = null;
	private MenuItem mItemRed = null;
	private MenuItem mItemYellow = null;
	private MenuItem mItemWhite = null;
	private MenuItem mItemClear = null;
	private MenuItem mItemAutoExposure = null;
	private MenuItem mItemHomography = null;
	private MenuItem mItemDrive = null;
	private MenuItem mItemDriveTest = null;
	private MenuItem mItemConnect = null;

	private Mat mRgba = null;

	private Beacon.Colors colorSelect = Beacon.Colors.None;

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

	public RobotActivity() {
		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.robot_surface_view);

		mOpenCvCameraView = (RobotCamera) findViewById(R.id.robot_activity_robot_surface_view);
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setOnTouchListener(this);

		mOpenCvCameraView.setCvCameraViewListener(this);

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
		// mItemSwitchCamera = menu.add("Toggle Native/Java camera");

		mItemBall = menu.add("Ball");
		mItemRed = menu.add("Red");
		mItemYellow = menu.add("Yellow");
		mItemBlue = menu.add("Blue");
		mItemWhite = menu.add("White");
		mItemClear = menu.add("Clear");
		mItemAutoExposure = menu.add("Light");
		mItemHomography = menu.add("Homography");

		mItemConnect = menu.add("Connect");
		mItemDrive = menu.add("Drive");
		mItemDriveTest = menu.add("Drive Test");

		return true;
	}

	public void writeStatusText(final String text) {
		final TextView status = (TextView) findViewById(R.id.status_text_view);
		status.post(new Runnable() {
			public void run() {
				status.setText(text);
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

		if (item == mItemBall)
			colorSelect = Beacon.Colors.Ball;
		else if (item == mItemRed)
			colorSelect = Beacon.Colors.Red;
		else if (item == mItemYellow)
			colorSelect = Beacon.Colors.Yellow;
		else if (item == mItemBlue)
			colorSelect = Beacon.Colors.Blue;
		else if (item == mItemWhite)
			colorSelect = Beacon.Colors.White;
		else if (item == mItemClear)
			BeaconController.getInstance().clearColors();
		else if (item == mItemAutoExposure) {
			boolean lock = mOpenCvCameraView.toogleAutoExposureLock();
			Toast.makeText(this, "Auto Exposure " + (!lock ? "on" : "off"),
					Toast.LENGTH_SHORT).show();
		} else if (item == mItemHomography) {
			if (Vision.getInstance().calculateHomographyMatrix(mRgba))
				Toast.makeText(this, "Homography successful!",
						Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(this, "Homography failed!", Toast.LENGTH_SHORT)
						.show();
		} else if (item == mItemConnect) {
			Toast.makeText(this,
					"Connect: " + MoveFacade.getInstance().connectRobot(),
					Toast.LENGTH_SHORT).show();
		} else if (item == mItemDrive) {
			RobotPosition position = PositionController.getInstance()
					.getLastPosition();
			if (position != null && position.getCoords() != null) {
				Toast.makeText(this, "Driving!", Toast.LENGTH_SHORT).show();
				try {
					MoveFacade.getInstance().move(position.getCoords(),
							position.getAngle(), new Point(500, 500));
				} catch (Exception ex) {
					Log.e(TAG, "mItemDrive: " + ex.getMessage());
					Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG)
							.show();
				}

			}
		} else if (item == mItemDriveTest) {
			try {
				MoveFacade.getInstance().move(10);
				MoveFacade.getInstance().turnInPlace(90.0);
				Toast.makeText(this, "Just Driving!", Toast.LENGTH_SHORT)
						.show();
			} catch (Exception ex) {
				Log.e(TAG, "mItemDriveTest: " + ex.getMessage());
				Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
			}
		}

		return true;
	}

	public void onCameraViewStarted(int width, int height) {
		mOpenCvCameraView.setCameraOptimizationsOff();
	}

	public void onCameraViewStopped() {
		MoveFacade.getInstance().close();
	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		Runtime rt = Runtime.getRuntime();
		if (rt.freeMemory() < 10000000)
			rt.gc();

		mRgba = inputFrame.rgba();
		StringBuilder sb = new StringBuilder();

		Scalar color = null;
		switch (colorSelect) {
		case Blue:
			color = new Scalar(0, 0, 255);
			break;
		case White:
			color = new Scalar(255, 255, 255);
			break;
		case Red:
			color = new Scalar(255, 0, 0);
			break;
		case Yellow:
			color = new Scalar(255, 255, 0);
			break;
		case Ball:
			color = new Scalar(0, 0, 0);
		default:
			break;
		}

		if (color != null) {
			int size = 50;
			Point topLeft = new Point(mRgba.width() / 2 - size, mRgba.height()
					/ 2 - size);
			Point bottomRight = new Point(mRgba.width() / 2 + size,
					mRgba.height() / 2 + size);
			Core.rectangle(mRgba, topLeft, bottomRight, color, 5);
		} else {
			List<DetectedBeacon> detectedBeacons = BeaconController
					.getInstance().searchImage(mRgba);

			for (DetectedBeacon beacon : detectedBeacons) {
				beacon.draw(mRgba, 3, new Scalar(0, 0, 0));
				sb.append("Beacon " + beacon.getGlobalCoordinate()
						+ " detected!");
				if (Vision.getInstance().getHomography() != null) {
					Point realPoint = Vision.getInstance()
							.calculateHomographyPoint(beacon.getBottom());
					sb.append(" Distances: "
							+ realPoint
							+ " - "
							+ PositionController.getInstance()
									.calculateDistance(realPoint,
											new Point(0, 0)));
				}
				sb.append("\n");
			}

			List<DetectedObject> detectedBalls = BallController.getInstance()
					.searchImage(mRgba);

			for (DetectedObject ball : detectedBalls) {
				ball.draw(mRgba, 5);

				if (Vision.getInstance().getHomography() != null) {
					Point realPoint = Vision.getInstance()
							.calculateHomographyPoint(ball.getBottom());
					sb.append("Ball: "
							+ realPoint
							+ " - "
							+ PositionController.getInstance()
									.calculateDistance(realPoint,
											new Point(0, 0)) + "\n");
				}
			}

			PositionController.getInstance()
					.calculatePositions(detectedBeacons);
			if (PositionController.getInstance().getLastPosition() != null)
				sb.append("Robot Position: "
						+ PositionController.getInstance().getLastPosition()
						+ "\n");
			
			
			// Test of State Machine
			StateMachine.getInstance().update(detectedBalls, detectedBeacons);
		}

		writeStatusText(sb.toString());

		return mRgba;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			switch (colorSelect) {
			case Ball:
				BallController.getInstance().setColor(
						Vision.getInstance().getColorFromImage(mRgba));
			case Blue:
				BeaconController.getInstance().setBlue(
						Vision.getInstance().getColorFromImage(mRgba));
				break;
			case White:
				BeaconController.getInstance().setWhite(
						Vision.getInstance().getColorFromImage(mRgba));
				break;
			case Red:
				BeaconController.getInstance().setRed(
						Vision.getInstance().getColorFromImage(mRgba));
				break;
			case Yellow:
				BeaconController.getInstance().setYellow(
						Vision.getInstance().getColorFromImage(mRgba));
				break;
			default:
				break;
			}
			colorSelect = Beacon.Colors.None;
		}
		return true;
	}
}

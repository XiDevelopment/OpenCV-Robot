package at.int3ro.robot.controller;

import java.util.Date;
import java.util.List;

import org.opencv.core.Point;

import android.util.Log;
import at.int3ro.robot.model.DetectedBeacon;
import at.int3ro.robot.model.RobotPosition;

public class PositionController {
	private static final String TAG = "RobotPositionController";

	private static PositionController instance = null;

	public static PositionController getInstance() {
		if (instance == null)
			instance = new PositionController();
		return instance;
	}

	private RobotPosition currentPosition;
	private RobotPosition lastPosition;
	private long lastPositionTime;

	public void calculatePositions(List<DetectedBeacon> beacons) {
		// Only perform every 5 sek
		if (Vision.getInstance().getHomography() != null
				&& lastPositionTime + 5000 < getTime()) {
			lastPosition = currentPosition;

			if (beacons.size() >= 2) {
				// TODO don't take first two, take two good picks
				currentPosition = calculateRobotPosition(beacons.get(0),
						beacons.get(1));
			} else {
				currentPosition = null;
			}
			lastPositionTime = getTime();
		}
	}

	private long getTime() {
		Date date = new Date();
		return date.getTime();
	}

	private RobotPosition calculateRobotPosition(DetectedBeacon b1,
			DetectedBeacon b2) {
		if (b1 == null || b2 == null)
			return null;

		if (b1.getBottom().x > b2.getBottom().x) {
			DetectedBeacon temp = b1;
			b1 = b2;
			b2 = temp;
		}

		Point p1 = Vision.getInstance()
				.calculateHomographyPoint(b1.getBottom());
		Point p2 = Vision.getInstance()
				.calculateHomographyPoint(b2.getBottom());

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
        Log.i(TAG, "Calc of dist to Beacon: dist1: " + dist1 + ", beta2: "+ Math.toDegrees(beta2));
		x = dist1 * Math.sin(beta2);
		y = dist1 * Math.cos(beta2);

		Point result = new Point();
		// 1
		Log.i(TAG, "b1globalC=" + b1.getGlobalCoordinate().x + "  b2globalC="
				+ b2.getGlobalCoordinate().x + "   x=" + x + "   y=" + y);
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
		double angle = PositionController.getInstance().getAngle(b1, result);
		// double angle = 90;

		// Log Positions
		Log.i(TAG, "Robot Position");
		Log.i(TAG, "dist1 = " + dist1);
		Log.i(TAG, "dist2 = " + dist2);
		Log.i(TAG, "dist3 = " + dist3);
		Log.i(TAG, "beta1 = " + beta1);
		Log.i(TAG, "beta2 = " + beta2);
		Log.i(TAG, "x = " + x);
		Log.i(TAG, "y = " + y);
		Log.i(TAG, "b1 = " + b1.getGlobalCoordinate());
		Log.i(TAG, "b2 = " + b2.getGlobalCoordinate());
		Log.i(TAG, "Result = " + result.toString());

		RobotPosition position = new RobotPosition(result, angle);

		Log.i(TAG, "Result = " + position.toString());

		return position;
	}

	public double calculateDistance(double x1, double y1, double x2, double y2) {
		return calculateDistance(new Point(x1, y1), new Point(x2, y2));
	}

	public double calculateDistance(Point p1, Point p2) {
		double x = Math.pow(p1.x - p2.x, 2);
		double y = Math.pow(p1.y - p2.y, 2);
		return Math.sqrt(x + y);
	}

	/**
	 * @return the currentPosition
	 */
	public RobotPosition getCurrentPosition() {
		return currentPosition;
	}

	/**
	 * @return the lastPosition
	 */
	public RobotPosition getLastPosition() {
		return lastPosition;
	}

	/**
	 * 
	 * @param x
	 *            distance to beacon from center (positive for right or
	 *            negative)
	 * @param bb
	 *            beaconbound nearest to the center of the screen
	 * @param robotX
	 *            x coordinate of robot
	 * @param robotY
	 *            y coordinate of robot
	 * @return angle to origin in degrees
	 */
	public double getAngle(DetectedBeacon bb, Point robot) {
		double x = Vision.getInstance()
				.calculateHomographyPoint(bb.getBottom()).x;

		Log.i(TAG, "getAngle");
		Log.i(TAG, "x: " + x);
		Log.i(TAG, "bb point: " + bb.getGlobalCoordinate().toString());
		Log.i(TAG, "robotX: " + robot.x);
		Log.i(TAG, "robotY: " + robot.y);

		// calculate bottomPointXPixel
		double beacon_x = bb.getGlobalCoordinate().x;
		double beacon_y = bb.getGlobalCoordinate().y;

		/*
		 * //rotate beacon to center int center = 540;
		 * 
		 * int difference1 = bottomPointXPixel - center;
		 * System.out.println(difference1);
		 * 
		 * double rotate = (double)difference1 / (2 * (double)center) * (Math.PI
		 * / 4); System.out.println(Math.toDegrees(rotate));
		 * 
		 * //MoveFacade.getInstance().turn(Math.toDegrees(rotate));
		 * 
		 * //get new bottomPointXPixel??? int bottomPointXPixelNew = 540;
		 * 
		 * int difference2 = bottomPointXPixelNew - center;
		 * System.out.println(difference2);
		 * 
		 * double alpha2 = rotate * (double) difference2 / ((double) difference1
		 * - (double) difference2); System.out.println(Math.toDegrees(alpha2));
		 */

		// distance robot to beacon
		double c = calculateDistance(robot.x, robot.y, beacon_x, beacon_y);
		// distance: display middle to beacon in mm
		double a = x;
		// distance to field boarder (depends on beacon)
		double b = 0.0;

		double alpha1 = 0.0; // degree normal (if beacon is exactly in the
								// middle)
		double alpha2 = 0.0; // degree (away from the middle)
		double alpha3 = 0.0; // real degree

		alpha2 = Math.asin(a / c);

		if (beacon_x == 0.0 && beacon_y == 0.0) {
			b = robot.y;
			alpha1 = Math.acos(b / c);
			alpha3 = Math.PI * 1.5 - alpha1;
		} else if (beacon_x == 0.0 && beacon_y == 750.0) {
			b = robot.x;
			alpha1 = Math.acos(b / c);
			if (robot.y < 750.0) {
				alpha3 = Math.PI - alpha1;
			} else {
				alpha3 = Math.PI + alpha1;
			}
		} else if (beacon_x == 0.0 && beacon_y == 1500.0) {
			b = 1500.0 - robot.y;
			alpha1 = Math.acos(b / c);
			alpha3 = Math.PI / 2 + alpha1;
		} else if (beacon_x == 750.0 && beacon_y == 1500.0) {
			b = 1500.0 - robot.y;
			alpha1 = Math.acos(b / c);
			if (robot.x < 750.0) {
				alpha3 = Math.PI / 2 - alpha1;
			} else {
				alpha3 = Math.PI / 2 + alpha1;
			}
		} else if (beacon_x == 1500.0 && beacon_y == 1500.0) {
			b = 1500.0 - robot.y;
			alpha1 = Math.acos(b / c);
			alpha3 = Math.PI / 2 - alpha1;
		} else if (beacon_x == 1500.0 && beacon_y == 750.0) {
			b = 1500.0 - robot.y;
			alpha1 = Math.acos(b / c);
			if (robot.y < 750.0) {
				alpha3 = alpha1;
			} else {
				alpha3 = Math.PI * 2 - alpha1;
			}
		} else if (beacon_x == 1500.0 && beacon_y == 0.0) {
			b = robot.y;
			alpha1 = Math.acos(b / c);
			alpha3 = Math.PI * 1.5 + alpha1;
		} else if (beacon_x == 750.0 && beacon_y == 0.0) {
			b = robot.y;
			alpha1 = Math.acos(b / c);
			if (robot.x < 750.0) {
				alpha3 = Math.PI * 1.5 + alpha1;
			} else {
				alpha3 = Math.PI * 1.5 - alpha1;
			}
		}

		alpha3 += alpha2;

		// calculateAngle

		return Math.toDegrees(alpha3);
	}
}

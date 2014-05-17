package at.int3ro.robot.controller;

import at.int3ro.robot.model.BeaconBounds;

public class PositionController {

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
	public static double getAngle(double x, BeaconBounds bb, double robotX,
			double robotY) {

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

		double c = calcDistance(robotX, robotY, beacon_x, beacon_y); // distance
																		// robot
																		// to
																		// beacon
		double a = x; // distance: display middle to beacon in mm
		double b = 0.0; // distance to field boarder (depends on beacon)

		double alpha1 = 0.0; // degree normal (if beacon is exactly in the
								// middle)
		double alpha2 = 0.0; // degree (away from the middle)
		double alpha3 = 0.0; // real degree		
		
		alpha2 = Math.asin(a / c);

		if (beacon_x == 0.0 && beacon_y == 0.0) {
			b = robotY;
			alpha1 = Math.acos(b / c);
			alpha3 = Math.PI * 1.5 - alpha1;
		} else if (beacon_x == 0.0 && beacon_y == 750.0) {
			b = robotX;
			alpha1 = Math.acos(b / c);
			if (robotY < 750.0) {
				alpha3 = Math.PI - alpha1;
			} else {
				alpha3 = Math.PI + alpha1;
			}
		} else if (beacon_x == 0.0 && beacon_y == 1500.0) {
			b = 1500.0 - robotY;
			alpha1 = Math.acos(b / c);
			alpha3 = Math.PI / 2 + alpha1;
		} else if (beacon_x == 750.0 && beacon_y == 1500.0) {
			b = 1500.0 - robotY;
			alpha1 = Math.acos(b / c);
			if (robotX < 750.0) {
				alpha3 = Math.PI / 2 - alpha1;
			} else {
				alpha3 = Math.PI / 2 + alpha1;
			}
		} else if (beacon_x == 1500.0 && beacon_y == 1500.0) {
			b = 1500.0 - robotY;
			alpha1 = Math.acos(b / c);
			alpha3 = Math.PI / 2 - alpha1;
		} else if (beacon_x == 1500.0 && beacon_y == 750.0) {
			b = 1500.0 - robotX;
			alpha1 = Math.acos(b / c);
			if (robotY < 750.0) {
				alpha3 = alpha1;
			} else {
				alpha3 = Math.PI * 2 - alpha1;
			}
		} else if (beacon_x == 1500.0 && beacon_y == 0.0) {
			b = robotY;
			alpha1 = Math.acos(b / c);
			alpha3 = Math.PI * 1.5 + alpha1;
		} else if (beacon_x == 750.0 && beacon_y == 0.0) {
			b = robotY;
			alpha1 = Math.acos(b / c);
			if (robotX < 750.0) {
				alpha3 = Math.PI * 1.5 + alpha1;
			} else {
				alpha3 = Math.PI * 1.5 - alpha1;
			}
		}

		alpha3 += alpha2;

		// calculateAngle

		return Math.toDegrees(alpha3);
	}

	public static double calcDistance(double x1, double y1, double x2, double y2) {
		double x = Math.pow(x1 - x2, 2);
		double y = Math.pow(y1 - y2, 2);
		return Math.sqrt(x + y);
	}
}

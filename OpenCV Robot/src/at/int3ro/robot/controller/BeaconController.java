package at.int3ro.robot.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import android.util.Log;
import at.int3ro.robot.model.Beacon;
import at.int3ro.robot.model.DetectedBeacon;
import at.int3ro.robot.model.DetectedObject;

public class BeaconController {
	private static final String TAG = "RobotBeaconController";
	private static final boolean debug = false;

	private static BeaconController instance = null;

	public static BeaconController getInstance() {
		if (instance == null)
			instance = new BeaconController();
		return instance;
	}

	List<Beacon> configuredBeacons = Collections
			.synchronizedList(new ArrayList<Beacon>());
	private Scalar red;
	private Scalar blue;
	private Scalar yellow;
	private Scalar white;

	// Tolerance for the Saturation value
	private int tolerance = 25;

	private BeaconController() {
		setUpBeaconLocations();
	}

	/**
	 * Set up beacons with pre defined locations
	 */
	public void setUpBeaconLocations() {
		configuredBeacons.clear();
		configuredBeacons.add(new Beacon(blue, yellow, new Point(0, 0)));
		configuredBeacons.add(new Beacon(blue, white, new Point(0, 750)));
		configuredBeacons.add(new Beacon(yellow, blue, new Point(0, 1500)));
		configuredBeacons.add(new Beacon(red, blue, new Point(750, 1500)));
		configuredBeacons.add(new Beacon(yellow, red, new Point(1500, 1500)));
		configuredBeacons.add(new Beacon(red, white, new Point(1500, 750)));
		configuredBeacons.add(new Beacon(red, yellow, new Point(1500, 0)));
		configuredBeacons.add(new Beacon(blue, red, new Point(750, 0)));
	}

	/**
	 * Searches a image for beacons
	 * 
	 * @param imageRgba
	 *            the image
	 * @return the beacons found
	 */
	public List<DetectedBeacon> searchImage(Mat imageRgba) {
		Log.v(TAG, "Started: searchImage");

		// Add colors to list, if they are set.
		List<Scalar> colors = new ArrayList<Scalar>();
		if (red != null)
			colors.add(red);
		if (blue != null)
			colors.add(blue);
		if (yellow != null)
			colors.add(yellow);
		if (white != null)
			colors.add(white);

		// Log
		for (Scalar color : colors)
			Log.v(TAG, "Color in List: " + color);

		// Get Objects by color with threading
		List<DetectedObject> detectedObjects = Vision.getInstance()
				.getObjectByColorThreaded(imageRgba, colors);

		// Log
		Log.v(TAG,
				"Objects detected by Vision class: " + detectedObjects.size());

		List<DetectedBeacon> detectedBeacons = new ArrayList<DetectedBeacon>();
		// Massive For loop, checking every possible combination of beacons
		for (DetectedObject lower : detectedObjects)
			for (Beacon beacon : configuredBeacons)
				if (lower.getColor().equals(beacon.getLowerColor()))
					for (DetectedObject upper : detectedObjects)
						if (lower != upper
								&& upper.getColor().equals(
										beacon.getUpperColor()))
							if (checkOverlap(upper, lower, tolerance))
								detectedBeacons.add(new DetectedBeacon(beacon
										.getLowerColor(), beacon
										.getUpperColor(), beacon
										.getGlobalCoordinate(), lower, upper));

		// If debug variable is set, drow EVERY object detected
		if (debug)
			for (DetectedObject obj : detectedObjects)
				obj.draw(imageRgba);

		// Log
		Log.v(TAG, "Beacons found: " + detectedBeacons.size());
		Log.v(TAG, "Finished: searchImage");
		
		return detectedBeacons;
	}

	/**
	 * Checks if two objects overlap
	 * @param upper uper object
	 * @param lower lower object
	 * @param tolerance
	 * @return true if overlap
	 */
	private boolean checkOverlap(DetectedObject upper, DetectedObject lower,
			int tolerance) {

		if ((upper != lower) && (lower.getLeft().x < upper.getRight().x)
				&& (lower.getRight().x > upper.getLeft().x)
				&& (lower.getTop().y - tolerance < upper.getBottom().y)
				&& (lower.getBottom().y > upper.getTop().y))
			if (lower.getBottom().y > upper.getBottom().y)
				return true;

		return false;
	}

	/**
	 * @return the red
	 */
	public Scalar getRed() {
		return red;
	}

	/**
	 * @param red
	 *            the red to set
	 */
	public void setRed(Scalar red) {
		this.red = red;
		setUpBeaconLocations();
	}

	/**
	 * @return the blue
	 */
	public Scalar getBlue() {
		return blue;
	}

	/**
	 * @param blue
	 *            the blue to set
	 */
	public void setBlue(Scalar blue) {
		this.blue = blue;
		setUpBeaconLocations();
	}

	/**
	 * @return the yellow
	 */
	public Scalar getYellow() {
		return yellow;
	}

	/**
	 * @param yellow
	 *            the yellow to set
	 */
	public void setYellow(Scalar yellow) {
		this.yellow = yellow;
		setUpBeaconLocations();
	}

	/**
	 * @return the white
	 */
	public Scalar getWhite() {
		return white;
	}

	/**
	 * @param white
	 *            the white to set
	 */
	public void setWhite(Scalar white) {
		this.white = white;
		setUpBeaconLocations();
	}

	/**
	 * @return the configuredBeacons
	 */
	public List<Beacon> getConfiguredBeacons() {
		return configuredBeacons;
	}

	/**
	 * Tolerance for overlap detection on the y coordinate
	 * 
	 * @return the tolerance
	 */
	public int getTolerance() {
		return tolerance;
	}

	/**
	 * Tolerance for overlap detection on the y coordinate
	 * 
	 * @param tolerance
	 *            the tolerance to set
	 */
	public void setTolerance(int tolerance) {
		this.tolerance = tolerance;
	}

	/**
	 * Clears all colors, turns of beacon detection
	 */
	public void clearColors() {
		this.blue = null;
		this.red = null;
		this.yellow = null;
		this.white = null;
		setUpBeaconLocations();
	}

}

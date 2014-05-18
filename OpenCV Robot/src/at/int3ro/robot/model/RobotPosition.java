package at.int3ro.robot.model;

import org.opencv.core.Point;

public class RobotPosition {
	private Point coords;
	private double angle;

	public RobotPosition(Point coords, double angle) {
		super();
		this.coords = coords;
		this.angle = angle;
	}

	/**
	 * @return the coords
	 */
	public Point getCoords() {
		return coords;
	}

	/**
	 * @param coords
	 *            the coords to set
	 */
	public void setCoords(Point coords) {
		this.coords = coords;
	}

	/**
	 * @return the angle
	 */
	public double getAngle() {
		return angle;
	}

	/**
	 * @param angle
	 *            the angle to set
	 */
	public void setAngle(double angle) {
		this.angle = angle;
	}

	@Override
	public String toString() {
		return "(" + coords + " " + angle + ")";
	}
}

package at.int3ro.robot.model;

import org.opencv.core.Point;

public class RobotPosition {
	private Point coords;
	private double angle;

	public Point getCoords() {
		return coords;
	}

	public void setCoords(Point coords) {
		this.coords = coords;
	}

	public double getAngle() {
		return angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}

	public RobotPosition(Point coords, double angle) {
		super();
		this.coords = coords;
		this.angle = angle;
	}

	@Override
	public String toString() {
		return "(" + coords + " " + angle + ")";
	}
}

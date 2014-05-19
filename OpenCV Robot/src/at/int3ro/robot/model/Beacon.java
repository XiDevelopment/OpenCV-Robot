package at.int3ro.robot.model;

import org.opencv.core.Point;
import org.opencv.core.Scalar;

public class Beacon {
	private Scalar lowerColor;
	private Scalar upperColor;
	private Point globalCoordinate;

	public Beacon(Scalar lowerColor, Scalar upperColor, Point globalCoordinate) {
		super();
		this.lowerColor = lowerColor;
		this.upperColor = upperColor;
		this.globalCoordinate = globalCoordinate;
	}

	public static enum Colors {
		None, Red, Yellow, Blue, White,
	}

	/**
	 * @return the lowerColor
	 */
	public Scalar getLowerColor() {
		return lowerColor;
	}

	/**
	 * @param lowerColor
	 *            the lowerColor to set
	 */
	public void setLowerColor(Scalar lowerColor) {
		this.lowerColor = lowerColor;
	}

	/**
	 * @return the upperColor
	 */
	public Scalar getUpperColor() {
		return upperColor;
	}

	/**
	 * @param upperColor
	 *            the upperColor to set
	 */
	public void setUpperColor(Scalar upperColor) {
		this.upperColor = upperColor;
	}

	/**
	 * @return the globalCoordinate
	 */
	public Point getGlobalCoordinate() {
		return globalCoordinate;
	}

	/**
	 * @param globalCoordinate
	 *            the globalCoordinate to set
	 */
	public void setGlobalCoordinate(Point globalCoordinate) {
		this.globalCoordinate = globalCoordinate;
	}

	@Override
	public String toString() {
		return "Beacon" + getGlobalCoordinate();
	}
}

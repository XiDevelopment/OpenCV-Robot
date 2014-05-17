package at.int3ro.robot.model;

import org.opencv.core.Scalar;

public class ColorBounds {
	Scalar color;
	Scalar threshold;

	// Default Threshold: H=10, S=50, V=100
	static Scalar defaultThreshold = new Scalar(7, 25, 125);

	public ColorBounds(Scalar color) {
		// if no threshold provided, start with default one
		this(color, defaultThreshold);
	}

	public ColorBounds(Scalar color, Scalar threshold) {
		super();
		this.color = color;
		this.threshold = threshold;
	}

	/**
	 * @return the upperBoundry
	 */
	public Scalar getUpperBound() {
		return new Scalar(color.val[0] + threshold.val[0], color.val[1]
				+ threshold.val[1], color.val[2] + threshold.val[2]);
	}

	/**
	 * @return the lowerBoundry
	 */
	public Scalar getLowerBound() {
		return new Scalar(color.val[0] - threshold.val[0], color.val[1]
				- threshold.val[1], color.val[2] - threshold.val[2]);
	}

	/**
	 * @return the color
	 */
	public Scalar getColor() {
		return color;
	}

	/**
	 * @param color
	 *            the color to set
	 */
	public void setColor(Scalar color) {
		this.color = color;
	}

	/**
	 * @return the String representing a Color Scalar
	 */
	@Override
	public String toString() {
		return "(" + this.getLowerBound().toString() + "x"
				+ this.getUpperBound().toString() + ")";
	}

	/**
	 * @return the threshold
	 */
	public Scalar getThreshold() {
		return threshold;
	}

	/**
	 * @param threshold
	 *            the threshold to set
	 */
	public void setThreshold(Scalar threshold) {
		this.threshold = threshold;
	}
}

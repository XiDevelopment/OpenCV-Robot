package at.int3ro.robot.model;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

public class DetectedBeacon extends Beacon {
	private DetectedObject lowerObject;
	private DetectedObject upperObject;

	public DetectedBeacon(Scalar lowerColor, Scalar upperColor,
			Point globalCoordinate, DetectedObject lowerObject,
			DetectedObject upperObject) {
		super(lowerColor, upperColor, globalCoordinate);
		this.lowerObject = lowerObject;
		this.upperObject = upperObject;
	}

	/**
	 * Draws the Object onto a Mat
	 * 
	 * @param matTo
	 *            the mat to draw on
	 * @param thickness
	 *            the thickness of the borders
	 * @param color
	 *            color of rectangle borders
	 */
	public void draw(Mat matTo, int thickness, Scalar color) {
		// Bottom Point
		Core.circle(matTo, this.getBottom(), 5, new Scalar(255, 0, 0), 3);

		// Rectangle
		Point topLeft = new Point(this.getUpperObject().getLeft().x, this
				.getUpperObject().getTop().y);
		Point bottomRight = new Point(this.getLowerObject().getRight().x, this
				.getLowerObject().getBottom().y);
		Core.rectangle(matTo, topLeft, bottomRight, color, thickness);
	}

	/**
	 * Draws the Object onto a Mat
	 * 
	 * @param matTo
	 *            the mat to draw on
	 */
	public void draw(Mat matTo) {
		draw(matTo, 1, new Scalar(0, 0, 0));
	}

	/**
	 * @return the lowerObject
	 */
	public DetectedObject getLowerObject() {
		return lowerObject;
	}

	/**
	 * @param lowerObject
	 *            the lowerObject to set
	 */
	public void setLowerObject(DetectedObject lowerObject) {
		this.lowerObject = lowerObject;
	}

	/**
	 * @return the upperObject
	 */
	public DetectedObject getUpperObject() {
		return upperObject;
	}

	/**
	 * @param upperObject
	 *            the upperObject to set
	 */
	public void setUpperObject(DetectedObject upperObject) {
		this.upperObject = upperObject;
	}

	/**
	 * Bottom Point of Beacon
	 * 
	 * @return the bottom
	 */
	public Point getBottom() {
		return this.getLowerObject().getBottom();
	}
}

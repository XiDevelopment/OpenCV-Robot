package at.int3ro.robot.model;

import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

public class DetectedObject {
	private Point bottom;
	private Point top;
	private Point right;
	private Point left;
	private Scalar color;

	public DetectedObject(MatOfPoint contour, Scalar color) {
		this.color = color;
		calculatePoints(contour);
	}

	private void calculatePoints(MatOfPoint contour) {
		// Start with extreme Values
		bottom = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);
		top = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
		right = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);
		left = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);

		// Calculate Extreme Points
		List<Point> points = contour.toList();
		for (Point p : points) {
			if (p.y > bottom.y)
				bottom = p;
			if (p.y < top.y)
				top = p;
			if (p.x > right.x)
				right = p;
			if (p.x < left.x)
				left = p;
		}
	}

	public void draw(Mat matTo, int thickness) {
		// Bottom Point
		Core.circle(matTo, this.getBottom(), 5, new Scalar(255, 0, 0), 3);

		// Left Point
		Core.circle(matTo, this.getLeft(), 2, new Scalar(255, 0, 255), 3);

		// Rectangle
		Point topLeft = new Point(this.getLeft().x, this.getTop().y);
		Point bottomRight = new Point(this.getRight().x, this.getBottom().y);
		Core.rectangle(matTo, topLeft, bottomRight, this.getColor(), thickness);
	}

	public void draw(Mat matTo) {
		draw(matTo, 1);
	}

	/**
	 * @return the bottom
	 */
	public Point getBottom() {
		return bottom;
	}

	/**
	 * @param bottom
	 *            the bottom to set
	 */
	public void setBottom(Point bottom) {
		this.bottom = bottom;
	}

	/**
	 * @return the top
	 */
	public Point getTop() {
		return top;
	}

	/**
	 * @param top
	 *            the top to set
	 */
	public void setTop(Point top) {
		this.top = top;
	}

	/**
	 * @return the right
	 */
	public Point getRight() {
		return right;
	}

	/**
	 * @param right
	 *            the right to set
	 */
	public void setRight(Point right) {
		this.right = right;
	}

	/**
	 * @return the left
	 */
	public Point getLeft() {
		return left;
	}

	/**
	 * @param left
	 *            the left to set
	 */
	public void setLeft(Point left) {
		this.left = left;
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

}

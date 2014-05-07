package at.int3ro.robot.model;

import java.util.List;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

public class ContourExtremePoints {
	private Point bottom;
	private Point top;
	private Point right;
	private Point left;

	private MatOfPoint contour;

	public ContourExtremePoints(MatOfPoint contour) {
		// Save Contour
		this.contour = contour;		
		calculatePoints();
	}
	
	private void calculatePoints() {
		// Start with extreme Values
		bottom = new Point(-1000000, -1000000);
		top = new Point(1000000, 1000000);
		right = new Point(-1000000, -1000000);
		left = new Point(1000000, 1000000);

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

	public MatOfPoint getContour() {
		return contour;
	}

	public void setContour(MatOfPoint contour) {
		this.contour = contour;		
		calculatePoints();	
	}

	public Point getBottom() {
		return bottom;
	}

	public Point getTop() {
		return top;
	}

	public Point getRight() {
		return right;
	}

	public Point getLeft() {
		return left;
	}
}

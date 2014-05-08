package at.int3ro.robot.model;

import java.util.List;

public class BeaconContour {
	private ColorBounds color;
	private List<ContourExtremePoints> contours;

	public BeaconContour(List<ContourExtremePoints> contours, ColorBounds color) {
		super();
		this.contours = contours;
		this.color = color;
	}

	public List<ContourExtremePoints> getContours() {
		return contours;
	}

	public void setContours(List<ContourExtremePoints> contours) {
		this.contours = contours;
	}

	public ColorBounds getColor() {
		return color;
	}

	public void setColor(ColorBounds color) {
		this.color = color;
	}
}

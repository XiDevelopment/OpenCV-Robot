package at.int3ro.robot.model;

import org.opencv.core.Scalar;

public class ColorBounds {
	Scalar upperBound;
	Scalar lowerBound;

	public Scalar getUpperBound() {
		return upperBound;
	}

	public void setUpperBound(Scalar upperBound) {
		this.upperBound = upperBound;
	}

	public Scalar getLowerBound() {
		return lowerBound;
	}

	public void setLowerBound(Scalar lowerBound) {
		this.lowerBound = lowerBound;
	}

	public ColorBounds(Scalar upperBound, Scalar lowerBound) {
		super();
		this.upperBound = upperBound;
		this.lowerBound = lowerBound;
	}

	@Override
	public String toString() {
		return "(" + lowerBound.toString() + "x" + upperBound.toString() + ")";
	}
}

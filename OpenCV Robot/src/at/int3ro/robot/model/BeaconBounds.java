package at.int3ro.robot.model;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Point;

import android.util.Log;

public class BeaconBounds {
	private ColorBounds lowerBound;
	private ColorBounds upperBound;
	private List<ContourExtremePoints> contours;
	private int calculateDelay = 0;

	private static final String TAG = "BeaconBounds::Class";

	public void startDelay(int time) {
		calculateDelay = time;
	}

	public boolean isDelay() {
		if (calculateDelay > 0) {
			calculateDelay--;
			return true;
		} else {
			return false;
		}
	}

	private Point globalCoordinate;

	static private List<BeaconBounds> beaconBounds;
	static private List<ColorBounds> colorsToFilter;

	static public List<BeaconBounds> getBeaconBounds() {
		return beaconBounds;
	}

	static public List<ColorBounds> getColorsToFilter() {
		return colorsToFilter;
	}

	static public void clearBeaconBounds() {
		if (beaconBounds != null)
			beaconBounds.clear();
	}

	public BeaconBounds(ColorBounds lowerBound, ColorBounds upperBound,
			Point globalCoordinate) {
		super();
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		this.globalCoordinate = globalCoordinate;
		this.contours = new ArrayList<ContourExtremePoints>();
	}

	public ColorBounds getLowerBound() {
		return lowerBound;
	}

	public void setLowerBound(ColorBounds lowerBound) {
		this.lowerBound = lowerBound;
	}

	public ColorBounds getUpperBound() {
		return upperBound;
	}

	public void setUpperBound(ColorBounds upperBound) {
		this.upperBound = upperBound;
	}

	public Point getGlobalCoordinate() {
		return globalCoordinate;
	}

	public void setGlobalCoordinate(Point globalCoordinate) {
		this.globalCoordinate = globalCoordinate;
	}

	public static boolean createBounds(List<ColorBounds> bounds) {
		Log.i("BeaconBounds::Class", "Started createBounds!");
		// must be 16 bounds, for 8 beacons
		if (bounds.size() != 4) {
			Log.e("BeaconBounds::Class",
					"Error at createBounds, bounds.size() != 16");
			return false;
		}

		for (ColorBounds b : bounds)
			Log.i("BeaconBounds::Class", b.toString());

		colorsToFilter = bounds;

		/**
		 * Creation of Beacons
		 */
		ColorBounds red = bounds.get(0);
		ColorBounds yellow = bounds.get(1);
		ColorBounds blue = bounds.get(2);
		ColorBounds white = bounds.get(3);

		beaconBounds = new ArrayList<BeaconBounds>();

		BeaconBounds b0 = new BeaconBounds(blue, yellow, new Point(0, 0));
		beaconBounds.add(b0);
		BeaconBounds b1 = new BeaconBounds(blue, white, new Point(0, 750));
		beaconBounds.add(b1);
		BeaconBounds b2 = new BeaconBounds(yellow, blue, new Point(0, 1500));
		beaconBounds.add(b2);
		BeaconBounds b3 = new BeaconBounds(red, blue, new Point(750, 1500));
		beaconBounds.add(b3);
		BeaconBounds b4 = new BeaconBounds(yellow, red, new Point(1500, 1500));
		beaconBounds.add(b4);
		BeaconBounds b5 = new BeaconBounds(red, white, new Point(1500, 750));
		beaconBounds.add(b5);
		BeaconBounds b6 = new BeaconBounds(red, yellow, new Point(1500, 0));
		beaconBounds.add(b6);
		BeaconBounds b7 = new BeaconBounds(blue, red, new Point(750, 0));
		beaconBounds.add(b7);

		Log.i("BeaconBounds::Class", "Finished createBounds!");
		return true;
	}

	public List<ContourExtremePoints> getContours() {
		return contours;
	}

	public void setContours(List<ContourExtremePoints> contours) {
		if (contours == null)
			this.contours.clear();
		else
			this.contours = contours;
	}

	public void calculateContours(List<BeaconContour> contours) {
		List<ContourExtremePoints> lower = null;
		List<ContourExtremePoints> upper = null;

		for (BeaconContour b : contours) {
			if (b.getColor() == this.lowerBound)
				lower = b.getContours();
			else if (b.getColor() == this.upperBound)
				upper = b.getContours();
		}

		if (lower != null && upper != null)
			this.contours = filterBeaconContours(lower, upper);
		else
			this.contours = null;
	}

	public Point getBottomPoint() {
		Point p = null;

		// TODO maybe more then one
		for (ContourExtremePoints cp : this.contours) {
			p = cp.getBottom();
		}

		return p;
	}

	public List<ContourExtremePoints> filterBeaconContours(
			List<ContourExtremePoints> lowerContours,
			List<ContourExtremePoints> upperContours) {
		List<ContourExtremePoints> filteredBeaconContours = new ArrayList<ContourExtremePoints>();
		for (ContourExtremePoints lower : lowerContours) {
			for (ContourExtremePoints upper : upperContours) {
				// check overlap
				if (lower.getLeft().y < upper.getRight().y
						|| lower.getRight().y > upper.getLeft().y) {
					// no overlap
					continue;
				}
				if (lower.getTop().x - 100 > upper.getBottom().x
						|| lower.getBottom().x < upper.getTop().x) {
					// no overlap
					continue;
				}
				// overlap
				if (lower.getBottom().x > upper.getBottom().x) {
					filteredBeaconContours.add(lower);
				}
			}
		}
		return filteredBeaconContours;
	}

	@Override
	public String toString() {
		return "BeaconBound: " + globalCoordinate.toString();
	}
}

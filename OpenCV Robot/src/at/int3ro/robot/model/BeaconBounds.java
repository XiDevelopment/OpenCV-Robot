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

	static public List<BeaconBounds> getBeaconBounds() {
		return beaconBounds;
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
		if (bounds.size() != 16) {
			Log.e("BeaconBounds::Class",
					"Error at createBounds, bounds.size() != 16");
			return false;
		}

		List<BeaconBounds> list = new ArrayList<BeaconBounds>();
		for (int i = 0; i < bounds.size(); i += 2) {
			BeaconBounds newBound = new BeaconBounds(bounds.get(i),
					bounds.get(i + 1), new Point(0, 0));
			list.add(newBound);
			Log.i("BeaconBounds::Class", newBound.toString());
		}
		beaconBounds = list;

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

	@Override
	public String toString() {
		return "BeaconBound: L: " + lowerBound.toString() + " U:"
				+ upperBound.toString() + " C:" + globalCoordinate.toString();
	}
}

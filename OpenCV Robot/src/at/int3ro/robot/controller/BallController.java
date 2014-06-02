package at.int3ro.robot.controller;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import at.int3ro.robot.model.DetectedObject;

public class BallController {
	@SuppressWarnings("unused")
	private static final String TAG = "RobotBeaconController";

	private static BallController instance = null;

	public static BallController getInstance() {
		if (instance == null)
			instance = new BallController();
		return instance;
	}

	private BallController() {
		detectedBalls = new ArrayList<DetectedObject>();
	}

	private Scalar color = null;
	private List<DetectedObject> detectedBalls;

	public List<DetectedObject> searchImage(Mat imageRgba) {
		if (color != null) {
			List<DetectedObject> detectedObjects = Vision.getInstance()
					.getObjectByColor(imageRgba, color);

			// set global var and filter balls
			detectedBalls = filterBalls(detectedObjects);
			
			return detectedObjects;
		} else {
			detectedBalls.clear();
			return new ArrayList<DetectedObject>();
		}
	}

	private List<DetectedObject> filterBalls(List<DetectedObject> balls) {
		List<DetectedObject> result = new ArrayList<DetectedObject>();

		for (DetectedObject b1 : balls) {
			for (DetectedObject b2 : balls) {
				if ((b1 != b2) && (b2.getLeft().x < b1.getRight().x)
						&& (b2.getRight().x > b1.getLeft().x)
						&& (b2.getTop().y < b1.getBottom().y)
						&& (b2.getBottom().y > b1.getTop().y)) {
					if (b1.size() > b2.size())
						result.add(b1);
					else
						result.add(b2);
				}
			}
		}

		return result;
	}

	public Scalar getColor() {
		return color;
	}

	public void setColor(Scalar color) {
		this.color = color;
	}

	public void clearColor() {
		this.color = null;
	}

	public List<DetectedObject> getDetectedBalls() {
		return detectedBalls;
	}
}

package at.int3ro.robot.controller;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import at.int3ro.robot.model.DetectedObject;

public class BallController {
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

			detectedBalls = detectedObjects;
			return detectedObjects;
		} else {
			detectedBalls.clear();
			return new ArrayList<DetectedObject>();
		}
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

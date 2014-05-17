package at.int3ro.robot;

import java.util.List;

import org.opencv.android.JavaCameraView;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;

public class RobotCamera extends JavaCameraView implements PictureCallback {
	private final String TAG = "JavaCameraViewClass";
	
	private boolean cameraOptimizationsOff = false;

	public RobotCamera(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public List<Size> getResolutionList() {
		return mCamera.getParameters().getSupportedPreviewSizes();
	}

	public void setResolution(Size resolution) {
		disconnectCamera();
		mMaxHeight = resolution.height;
		mMaxWidth = resolution.width;
		connectCamera(getWidth(), getHeight());
	}

	public void setResolution(int w, int h) {
		Size r = mCamera.new Size(w, h);
		setResolution(r);
	}

	public Size getResolution() {
		return mCamera.getParameters().getPreviewSize();
	}

	public String getResolutionS() {
		return mCamera.getParameters().getPreviewSize().width + "x"
				+ mCamera.getParameters().getPreviewSize().height;
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		// TODO Auto-generated method stub

	}

	/**
	 * Sets whether the camera should perform white and color corrections
	 * 
	 * @param corr
	 *            true to turn of auto camera corrections
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
	public boolean setCameraOptimizationsOff(boolean corr) {
		Log.i(TAG, "CameraOptimziationsOff start");

		Parameters params = mCamera.getParameters();
		params.setAutoExposureLock(corr);
		params.setAutoWhiteBalanceLock(corr);
		params.setColorEffect(Parameters.EFFECT_NONE);
		params.setFocusMode(Parameters.FOCUS_MODE_AUTO);
		params.setWhiteBalance(Parameters.WHITE_BALANCE_FLUORESCENT);
		params.setExposureCompensation(0);
		params.setVideoStabilization(corr);
		params.setPreviewFrameRate(30);
		mCamera.setParameters(params);

		Log.i(TAG,
				"CameraOptimziationsOff finished. Framerate: "
						+ params.getPreviewFrameRate());

		cameraOptimizationsOff = corr;
		return cameraOptimizationsOff;
	}

	/**
	 * Gets the current Optimizations status
	 * 
	 * @return true when Optimizations are off and false if on
	 */
	public boolean getCameraOptimizationsOff() {
		return cameraOptimizationsOff;
	}
}

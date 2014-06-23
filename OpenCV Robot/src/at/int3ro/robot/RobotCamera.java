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

/**
 * This class is needed to turn on some camera optimizations. 
 * This is done in the setCameraOptimizationsOff() function.
 */
public class RobotCamera extends JavaCameraView implements PictureCallback {
	private final String TAG = "JavaCameraViewClass";

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
	public void setCameraOptimizationsOff() {
		Log.i(TAG, "CameraOptimziationsOff start");

		Parameters params = mCamera.getParameters();

		// Camera Optimizations
		params.setAutoExposureLock(true);
		params.setAutoWhiteBalanceLock(true);
		params.setWhiteBalance(Parameters.WHITE_BALANCE_WARM_FLUORESCENT);
		params.setFocusMode(Parameters.FOCUS_MODE_INFINITY);
		params.setPreviewFrameRate(30);

		mCamera.setParameters(params);

		Log.i(TAG,
				"CameraOptimziationsOff finished. Framerate: "
						+ params.getPreviewFrameRate());
	}

	/**
	 * Toggle exposure lock
	 * 
	 * @return true if locked
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public boolean toogleAutoExposureLock() {
		Parameters params = mCamera.getParameters();
		boolean result = !params.getAutoExposureLock();
		params.setAutoExposureLock(result);
		mCamera.setParameters(params);

		return result;
	}
}

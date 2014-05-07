package at.int3ro.robot;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import org.opencv.android.JavaCameraView;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.util.AttributeSet;

public class RobotCamera extends JavaCameraView implements PictureCallback {
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
}

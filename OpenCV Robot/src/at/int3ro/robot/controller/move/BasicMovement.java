package at.int3ro.robot.controller.move;

import jp.ksksue.driver.serial.FTDriver;
import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.widget.TextView;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class BasicMovement {
	private static BasicMovement instance = null;

	public static BasicMovement getInstance() {
		if (instance == null)
			instance = new BasicMovement();
		return instance;
	}

	@SuppressWarnings("unused")
	private String TAG = "iRobot";
	@SuppressWarnings("unused")
	private TextView textLog;
	private FTDriver com = null;

	public boolean connect(Context context) {
		// TODO implement permission request
		com = new FTDriver(
				(UsbManager) context.getSystemService(Context.USB_SERVICE));

		if (com != null)
			return com.begin(9600);
		else
			return false;
	}

	public void disconnect() {
		if (com != null)
			com.end();
	}

	/**
	 * transfers given bytes via the serial connection.
	 * 
	 * @param data
	 */
	public boolean comWrite(byte[] data) {
		if (com.isConnected()) {
			com.write(data);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * reads from the serial buffer. due to buffering, the read command is
	 * issued 3 times at minimum and continuously as long as there are bytes to
	 * read from the buffer. Note that this function does not block, it might
	 * return an empty string if no bytes have been read at all.
	 * 
	 * @return buffer content as string
	 */
	public String comRead() {
		String s = "";
		int i = 0;
		int n = 0;
		while (i < 3 || n > 0) {
			byte[] buffer = new byte[256];
			n = com.read(buffer);
			s += new String(buffer, 0, n);
			i++;
		}
		return s;
	}

	/**
	 * write data to serial interface, wait 100 ms and read answer.
	 * 
	 * @param data
	 *            to write
	 * @return answer from serial interface
	 */
	public String comReadWrite(byte[] data) {
		if (com != null) {
			com.write(data);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// ignore
			}
			return comRead();
		} else
			return null;
	}

	public void robotSetLeds(byte red, byte blue) {
		comReadWrite(new byte[] { 'u', red, blue, '\r', '\n' });
	}

	public void robotSetVelocity(byte left, byte right) {
		comReadWrite(new byte[] { 'i', left, right, '\r', '\n' });
	}

	public void robotSetBar(byte value) {
		comReadWrite(new byte[] { 'o', value, '\r', '\n' });
	}

	// move forward
	public String moveForward() {
		return comReadWrite(new byte[] { 'w', '\r', '\n' });
	}

	// turn left
	public void turnLeft() {
		comReadWrite(new byte[] { 'a', '\r', '\n' });
	}

	// stop
	public void stop() {
		comReadWrite(new byte[] { 's', '\r', '\n' });
	}

	// turn right
	public void turnRight() {
		comReadWrite(new byte[] { 'd', '\r', '\n' });
	}

	public void turnPosLeft() {
		robotSetVelocity((byte) -30, (byte) 30);
	}

	public void turnPosRight() {
		robotSetVelocity((byte) 30, (byte) -30);
	}

	// move backward
	public void moveBackward() {
		// logText(comReadWrite(new byte[] { 'x', '\r', '\n' }));
		robotSetVelocity((byte) -30, (byte) -30);
	}

	// lower bar a few degrees
	public void lowerBar() {
		comReadWrite(new byte[] { '-', '\r', '\n' });
	}

	// rise bar a few degrees
	public void riseBar() {
		comReadWrite(new byte[] { '+', '\r', '\n' });
	}

	// fixed position for bar (low)
	public void fixedBarLow() {
		robotSetBar((byte) 0);
	}

	// fixed position for bar (high)
	public void fixedBarHigh() {
		robotSetBar((byte) 255);
	}

	public void ledOn() {
		// logText(comReadWrite(new byte[] { 'r', '\r', '\n' }));
		robotSetLeds((byte) 255, (byte) 128);
	}

	public void ledOff() {
		// logText(comReadWrite(new byte[] { 'e', '\r', '\n' }));
		robotSetLeds((byte) 0, (byte) 0);
	}

	public void sensor() {
		comReadWrite(new byte[] { 'q', '\r', '\n' });
	}
}
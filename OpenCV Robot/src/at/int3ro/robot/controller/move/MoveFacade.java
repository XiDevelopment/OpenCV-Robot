package at.int3ro.robot.controller.move;

import java.util.LinkedList;
import java.util.Queue;

import org.opencv.core.Point;

import android.content.Context;
import android.util.Log;
import at.int3ro.robot.model.MoveLog;
import at.int3ro.robot.model.MoveLog.Movement;

public class MoveFacade {
	private static final String TAG = "RobotMoveFacade";
	private static MoveFacade instance = null;

	private Queue<Thread> threadQueue = new LinkedList<Thread>();
	private Thread thread;

	public static MoveFacade getInstance() {
		if (instance == null)
			instance = new MoveFacade();

		return instance;
	}

	/**
	 * Instantiates and starts a new thread that is waiting for the queue to be
	 * filled. The thread then automatically starts the instructions in the
	 * queue.
	 */
	public MoveFacade() {
		thread = new Thread() {
			@Override
			public void run() {
				while (true) {
					while (threadQueue.isEmpty()) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
						}
					}
					threadQueue.peek().start();
					try {
						threadQueue.peek().join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
					}
					// removing the first object after it has finished
					threadQueue.poll();
				}
			}
		};
		thread.start();
	}

	private BasicMovement basicMovement = BasicMovement.getInstance();

	Context context = null;

	/**
	 * Sets the context for our robot. That has to be done to build a
	 * connection.
	 * 
	 * @param context
	 */
	public void setContext(Context context) {
		this.context = context;
		if( this.connectRobot() ) {
			blinkingLED(1, 3.0);
		}
		
	}
	
	/**
	 * this function lets the LED's blink for a defined count and time.
	 * @param count amount of times the LED's should blink
	 * @param time how long the LED's stay on and off
	 */
	public void blinkingLED(int count, final double time) {
		for(int i = 0; i < count; i++) {
			threadQueue.add(new Thread() {
				public void run() {
					try {
						basicMovement.ledOn();
						sleep((long) (time*1000));
						basicMovement.ledOff();
						sleep((long) (time*1000));
					} catch (Exception ex) {
						//do nothing
					} finally {
						basicMovement.ledOff();
					}
				}
			});
		}
	}

	public boolean connectRobot() {
		if (context != null)
			return basicMovement.connect(context);
		return false;
	}

	/**
	 * Closes the connection to the robot.
	 */
	public void close() {
		basicMovement.disconnect();
		thread.interrupt();
	}

	/**
	 * Returns a boolean if the MovementThread is working at the moment. True if
	 * it is working, false if it is sleeping.
	 * 
	 * @return
	 */
	public boolean isActive() {
		return !threadQueue.isEmpty();
	}

	/**
	 * Moves from the given Position and viewing angle to the specific target
	 * position. Calculates the angle between current and target. Rotates and
	 * then moves forward.
	 * 
	 * @param curPos
	 *            the current position
	 * @param curAngle
	 *            the current angle we are looking at given that 0Â° is in the
	 *            east
	 * @param toPos
	 *            target position
	 */
	public void move(Point curPos, double curAngle, Point toPos) {
		Log.i(TAG, "move called");
		Log.i(TAG, "Current: " + curPos.toString());
		Log.i(TAG, "Target: " + toPos.toString());
		Log.i(TAG, "Angle: " + curAngle);

		// set new points for angle calculations
		Point viewOrigin = new Point(10.0, 0.0);
		Point targetPos = new Point(toPos.x - curPos.x, toPos.y - curPos.y);

		double distancePos = Math.sqrt(Math.pow(targetPos.x, 2.0)
				+ Math.pow(targetPos.y, 2.0));

		// calculate angle between two vectors and convert the result into
		// degrees
		double targetAngle = Math.toDegrees(Math.acos((viewOrigin.x
				* targetPos.x + viewOrigin.y * targetPos.y)
				/ (10.0 * distancePos)));

		Log.i(TAG, "Target Angle: " + targetAngle);

		// check y coordinate if the target pos is in the lower quadrants
		// and then convert it to an angle greater than 180
		// we do that because arccos(x) only returns [0,180]
		if (targetPos.y < 0) {
			targetAngle = 360.0 - targetAngle;
		}

		// calculate the actual angle the robot has to rotate
		double rotateAngle = targetAngle - curAngle;

		// calculate shortest way to turn
		// if the angle is greater than 180, the turn is faster in the other
		// direction
		if (rotateAngle > 180.0)
			rotateAngle -= 360.0;
		else if (rotateAngle < -180.0)
			rotateAngle += 360.0;

		Log.i(TAG, "Rotate1: " + rotateAngle);
		turnInPlace(rotateAngle);
		Log.i(TAG, "Rotate2: " + rotateAngle);
		Log.i(TAG, "Distance: " + distancePos / 10);
		move(distancePos / 10);
	}

	/**
	 * Moves the robot to a target position specified by an x and y coordinate
	 * in mm. X is negative if it's left of the robot or positive if right, Y is
	 * always positive.
	 * 
	 * @param x
	 *            distance in mm
	 * @param y
	 *            distance in mm
	 */
	public void move(double x, double y) {
		Log.i(TAG, "move called");
		double angle = Math.atan(Math.abs(x) / y);
		double distance = Math.abs(x) / Math.sin(angle);
		if (x < 0.0)
			turnInPlace(angle);
		else
			turnInPlace(-angle);

		Log.i(TAG, "angle: " + angle);
		Log.i(TAG, "distance: " + distance / 10.0);

		// Move -> in cm and -10 for offset of robot center point
		move(distance / 10.0 - 10);
	}

	/**
	 * Moves the given centimeter in either direction. > 0 moves forward and < 0
	 * moves backward. If dist is 0 the robot does nothing.
	 * 
	 * @param dist
	 *            the centimeter to move forward or backward
	 */
	public void move(final double cm) {
		Log.i(TAG, "move called");
	
		threadQueue.add(new Thread() {
			@Override
			public void run() {
				if (cm < 0)
					basicMovement.moveBackward();
				else if (cm > 0)
					basicMovement.moveForward();

				try {
					Thread.sleep((long) (Math.abs(cm) / 28.6 * 1000.0));
				} catch (InterruptedException e) {
				} finally {
					basicMovement.stop();
				}
			}
		});
	}

	/**
	 * Turns the robot the given angle. > 0 rotates counter clockwise, < 0
	 * rotates clockwise.
	 * 
	 * @param angle
	 *            the angle to rotate
	 */
	public void turn(final double angle) {
		Log.i(TAG, "turn called with angle: " + angle);
		threadQueue.add(new Thread() {
			@Override
			public void run() {
				if (angle < 0)
					basicMovement.turnRight();
				else if (angle > 0)
					basicMovement.turnLeft();

				try {
					Thread.sleep((long) (Math.abs(angle) / 85.5 * 1000.0));
				} catch (InterruptedException e) {

				} finally {
					basicMovement.stop();
				}
			}
		});
	}

	public void turnInPlace(final double angle) {
		Log.i(TAG, "turnInPlace called with angle: " + angle);
		threadQueue.add(new Thread() {
			@Override
			public void run() {
				if (angle < 0)
					basicMovement.turnPosRight();
				else
					basicMovement.turnPosLeft();

				try {
					Thread.sleep((long) (Math.abs(angle) / 171 * 1000.0));
				} catch (Exception e) {
				} finally {
					basicMovement.stop();
				}
			}
		});
	}

	/**
	 * Raises the bar to a constant height at the top.
	 */
	public void raiseBar() {
		Log.i(TAG, "raiseBar called");
		threadQueue.add(new Thread() {
			@Override
			public void run() {
				try {
					basicMovement.fixedBarHigh();
				} catch(Exception e) {
					
				}
			}
		});
	}

	/**
	 * Lowers the bar to a constant height at the bottom.
	 */
	public void lowerBar() {
		Log.i(TAG, "lowerBar called");
		threadQueue.add(new Thread() {
			@Override
			public void run() {
				try {
					basicMovement.fixedBarLow();
				} catch(Exception e) {
					
				}
			}
		});
	}
	
	public void stopRobot() {
		Log.i(TAG, "stopRobot called");
		basicMovement.stop();
		if(this.isActive()) {
			Thread t = threadQueue.peek();
			threadQueue.clear();
			t.interrupt();
			if(!threadQueue.isEmpty())
				stopRobot();
		}
	}
	
	public void undoLog(LinkedList<MoveLog> log) {
		Log.i(TAG, "undoLog called with log: " + log.toString());
		if(!log.isEmpty()) {
			for(MoveLog ml : log) {
				if(ml.getMovement() == Movement.MOVE) {
					this.move(-(ml.getAmount()/10.0));
				} else {
					this.turnInPlace(-(ml.getAmount()));
				}
			}
		}
		
	}
}
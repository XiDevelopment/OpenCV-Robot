package at.int3ro.robot.controller;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.opencv.core.Point;

import android.util.Log;
import at.int3ro.robot.controller.move.MoveFacade;
import at.int3ro.robot.model.DetectedBeacon;
import at.int3ro.robot.model.DetectedObject;
import at.int3ro.robot.model.RobotPosition;

public class StateMachine {
	private final String TAG = "RobotStateMachine";

	private static StateMachine instance = null;

	public static StateMachine getInstance() {
		if (instance == null)
			instance = new StateMachine();
		return instance;
	}

	public enum State {
		START {
			@Override
			public Set<State> possibleFollowUps() {
				return EnumSet.of(DETECT_POSITION);
			}
		},

		DETECT_POSITION {
			@Override
			public Set<State> possibleFollowUps() {
				return EnumSet.of(DETECT_BALL, DETECT_FINISH);
			}
		},

		DETECT_BALL {
			@Override
			public Set<State> possibleFollowUps() {
				return EnumSet.of(DETECT_CAGED);
			}
		},

		DETECT_CAGED {
			@Override
			public Set<State> possibleFollowUps() {
				return EnumSet.of(DETECT_BALL, DETECT_POSITION);
			}
		},

		DETECT_FINISH {
			@Override
			public Set<State> possibleFollowUps() {
				return EnumSet.of(FINISH);
			}
		},

		FINISH {
			@Override
			public Set<State> possibleFollowUps() {
				return EnumSet.of(START);
			}
		},

		;
		public Set<State> possibleFollowUps() {
			return EnumSet.noneOf(State.class);
		}
	}

	// Starting State
	private State state = State.START;
	
	// Is cage down
	private boolean caged = false;

	// Current update
	private List<DetectedObject> cBalls;
	private List<DetectedBeacon> cBeacons;

	// Frame counter
	private int waitCounter = 0;

	public void update(List<DetectedObject> balls, List<DetectedBeacon> beacons) {
		// If no homography matrix present, stop robot
		if (Vision.getInstance().getHomography() == null) {
			this.stop();
			return;
		}

		// If robot is moving, do nothing till it finished
		if (MoveFacade.getInstance().isActive())
			return;

		if (waitCounter <= 0) {
			cBalls = balls;
			cBeacons = beacons;

			Log.i(TAG, "update() -> Before: " + state);

			switch (state) {
			case DETECT_POSITION:
				detectPositionState();
				break;
			case DETECT_BALL:
				detectBallState();
				break;
			case DETECT_CAGED:
				detectCageState();
				break;
			case DETECT_FINISH:
				detectFinishState();
				break;
			case FINISH:
				finishState();
				break;
			default:
				state = State.START;
			}

			waitCounter = 3; // Wait x frames before next action

			Log.i(TAG, "update() -> After: " + state);
		} else {
			waitCounter--;
		}
	}

	private void detectPositionState() {
		// TODO for now just directly go into next state
		setState(State.DETECT_BALL);

		if (cBeacons.size() > 2) {
			// Get Position, and if one is found -> next state
			if (PositionController.getInstance().calculatePositions(cBeacons,
					true))
				if(caged) {
					// Set current positions;
					RobotPosition robotPos = PositionController.getInstance().getLastPosition();
					Point goalPos = PositionController.GOAL_POSITION; 
					
					// Move to Goal
					MoveFacade.getInstance().move(robotPos.getCoords(), robotPos.getAngle(), goalPos);
					
					// Set state to Detect Finish
					this.setState(State.DETECT_FINISH);
				} else {
					// Set state to Detect Ball
					this.setState(State.DETECT_BALL);
				}

		} else {
			// if not enough beacons visible, turn robot
			Log.i(TAG, "send to MoveFacade: .turnInPlace(45) from State: "
					+ this.getState());			
			MoveFacade.getInstance().turnInPlace(45);
		}
	}

	private void detectBallState() {
		if (cBalls.size() > 0) {// Balls in sight
			// Get realworld Ball pos
			Point to = Vision.getInstance().calculateHomographyPoint(
					cBalls.get(0).getBottom());

			// Move to Ball
			Log.i(TAG, "send to MoveFacade: .move(" + to.x + ", " + to.y
					+ ") from State: " + this.getState());
			MoveFacade.getInstance().move(to.x, to.y);

			// Set followup state
			this.setState(State.DETECT_CAGED);
		} else { // No ball in sight
			// Try to find ball by turning 45 degrees
			Log.i(TAG, "send to MoveFacade: .turnInPlace(45) from State: "
					+ this.getState());
			MoveFacade.getInstance().turnInPlace(45);
		}
	}

	private void detectCageState() {
		if (cBalls.size() > 0) {
			// Get realworld Ball pos
			Point bottom = Vision.getInstance().calculateHomographyPoint(
					cBalls.get(0).getBottom());

			// Check if ball in range of Cage
			if (bottom.y < 250 && (bottom.x < 100 && bottom.x > -100)) {
				MoveFacade.getInstance().lowerBar();
				caged = true;
				this.setState(State.DETECT_POSITION);
				return;
			}
		}

		// If no if was successful, go back to state DETECT_BALL
		this.setState(State.DETECT_BALL);
	}
	
	private void detectFinishState() {
		// TODO	
		this.setState(State.FINISH);
	}
	
	private void finishState() {
		// Final State
		// Reset to Starting Configuration
		this.reset();
	}

	public State getState() {
		return state;
	}

	public boolean setState(State state) {
		if (this.state.possibleFollowUps().contains(state)) {
			this.state = state;
			return true;
		} else
			return false;
	}

	public void reset() {
		state = State.START;
		caged = false;
		try {
			MoveFacade.getInstance().raiseBar();
		} catch (Exception ex) {
			// do nothing
		}
	}

	public void start() {
		state = State.DETECT_POSITION;
	}

	public void stop() {
		this.reset();
		if (MoveFacade.getInstance().isActive())
			MoveFacade.getInstance().stopRobot();
	}

}

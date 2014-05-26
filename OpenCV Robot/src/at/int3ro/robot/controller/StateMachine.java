package at.int3ro.robot.controller;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.opencv.core.Point;

import android.util.Log;
import at.int3ro.robot.controller.move.MoveFacade;
import at.int3ro.robot.model.DetectedBeacon;
import at.int3ro.robot.model.DetectedObject;

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
				return EnumSet.of(POSITION);
			}
		},

		POSITION {
			@Override
			public Set<State> possibleFollowUps() {
				return EnumSet.of(NO_BALL, MOVING);
			}
		},

		NO_BALL {
			@Override
			public Set<State> possibleFollowUps() {
				return EnumSet.of(MOVING);
			}
		},

		MOVING {
			@Override
			public Set<State> possibleFollowUps() {
				return EnumSet.of(DETECT_CAGE);
			}
		},

		DETECT_CAGE {
			@Override
			public Set<State> possibleFollowUps() {
				return EnumSet.of(POSITION, POSITION_CAGED);
			}
		},

		POSITION_CAGED {
			@Override
			public Set<State> possibleFollowUps() {
				return EnumSet.of(MOVING_CAGED);
			}
		},

		MOVING_CAGED {
			@Override
			public Set<State> possibleFollowUps() {
				return EnumSet.of(CHECK_FINISH);
			}
		},

		CHECK_FINISH {
			@Override
			public Set<State> possibleFollowUps() {
				return EnumSet.of(POSITION, POSITION_CAGED);
			}
		},

		;
		public Set<State> possibleFollowUps() {
			return EnumSet.noneOf(State.class);
		}
	}

	// Starting State
	private State state = State.START;

	// Current update
	private List<DetectedObject> cBalls;
	private List<DetectedBeacon> cBeacons;

	public void update(List<DetectedObject> balls, List<DetectedBeacon> beacons) {
		cBalls = balls;
		cBeacons = beacons;

		Log.i(TAG, "update() -> Before: " + state);

		switch (state) {
		case NO_BALL:
			noBallState();
			break;
		case MOVING:
			movingState();
			break;
		case DETECT_CAGE:
			detectCageState();
			break;
		default:
			state = State.NO_BALL;
			break;
		}

		Log.i(TAG, "update() -> After: " + state);
	}

	private void noBallState() {
		if (cBalls.size() > 0 && Vision.getInstance().getHomography() != null) {
			Point to = Vision.getInstance().calculateHomographyPoint(
					cBalls.get(0).getBottom());
			MoveFacade.getInstance().move(to.x, to.y);
			setState(State.MOVING);
		}
	}

	private void movingState() {
		if (MoveFacade.getInstance().isActive()) {
			// do nothing
		} else {
			setState(State.DETECT_CAGE);
		}
	}

	private void detectCageState() {
		if (cBalls.size() > 0 && Vision.getInstance().getHomography() != null) {
			Point bottom = cBalls.get(0).getBottom();
			bottom = Vision.getInstance().calculateHomographyPoint(bottom);
			if (bottom.y < 250 && (bottom.x < 100 && bottom.x > -100))
				setState(State.POSITION_CAGED);
			else {
				setState(State.POSITION);
			}
		}
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
	}

}

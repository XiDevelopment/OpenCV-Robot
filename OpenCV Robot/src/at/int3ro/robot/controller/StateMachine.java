package at.int3ro.robot.controller;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import at.int3ro.robot.model.DetectedBeacon;
import at.int3ro.robot.model.RobotPosition;

public class StateMachine {
	public enum State {
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
	private State state = State.POSITION;
	Thread thread;
	
	RobotPosition position;

	public void position(List<DetectedBeacon> beacons) {
		
		// Try to calculate Position with current beacons
		PositionController.getInstance().calculatePositions(beacons);
		
		// Check if calculatPosition was successful
		if (PositionController.getInstance().getLastPosition() != null) {
			// Check if Position is valid
			// TODO
			
			
		} else {
			// Wait
		}
	}
	
	public void position_ball() {
		// TODO check for ball
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

}

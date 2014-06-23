package at.int3ro.robot.model;

public class MoveLog {
	public enum Movement {
		MOVE, TURN;
	}

	private Movement movement;
	private double amount;

	public MoveLog(Movement move, double amount) {
		this.movement = move;
		this.amount = amount;
	}


	/**
	 * @return the movement
	 */
	public Movement getMovement() {
		return movement;
	}


	/**
	 * @param movement the movement to set
	 */
	public void setMovement(Movement movement) {
		this.movement = movement;
	}


	/**
	 * @return the amount
	 */
	public double getAmount() {
		return amount;
	}


	/**
	 * @param amount the amount to set
	 */
	public void setAmount(double amount) {
		this.amount = amount;
	}


	@Override
	public String toString() {
		return "" + movement + ": " + amount;
	}
}

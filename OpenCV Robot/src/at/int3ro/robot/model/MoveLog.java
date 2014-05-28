package at.int3ro.robot.model;

public class MoveLog {
	public enum Movement {
		MOVE,
		TURN;
	}
	
	private Movement movement;
	private double amount;
	
	public MoveLog(Movement move, double amount) {
		this.movement = move;
		this.amount = amount;
	}
	
	public Movement getMovement() {
		return movement;
	}
	public void setMovement(Movement movement) {
		this.movement = movement;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	
	@Override
	public String toString() {
		return ""+movement+": "+amount;
	}
}

package expression;

import regs.RegisterFile;
import robot.RobotRun;

/**
 * A operand that represents a specific register entry.
 */
public class RegisterOp implements Operand {
	/**
	 * 
	 */
	private RobotRun robotRun;
	private final int listIdx;

	public RegisterOp(RobotRun robotRun) {
		this.robotRun = robotRun;
		listIdx = 0;
	}

	public RegisterOp(RobotRun robotRun, int i) {
		this.robotRun = robotRun;
		listIdx = i;
	}

	public Operand clone() {
		return new RegisterOp(this.robotRun, listIdx);
	}

	public int getIdx() { return listIdx; }

	public Object getValue() {
		return RegisterFile.getDReg(listIdx);
	}

	public String toString() {
		return String.format("R[%d]", listIdx);
	}
}
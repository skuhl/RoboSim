package programming;
import robot.RobotRun;

public class JumpInstruction extends Instruction {
	/**
	 * 
	 */
	private final RobotRun robotRun;
	private int tgtLblNum;

	public JumpInstruction(RobotRun robotRun) {
		this.robotRun = robotRun;
		setTgtLblNum(-1);
	}

	public JumpInstruction(RobotRun robotRun, int l) {
		this.robotRun = robotRun;
		setTgtLblNum(l);
	}

	/**
	 * Returns the index of the instruction to which to jump.
	 */
	public int execute() {
		Program p = this.robotRun.activeProgram();

		if (p != null) {
			int lblIdx = p.findLabelIdx(getTgtLblNum());

			if (lblIdx != -1) {
				// Return destination instrution index
				return lblIdx;
			} else {
				RobotRun.println("Invalid jump instruction!");
				return 1;
			}
		} else {
			RobotRun.println("No active program!");
			return 2;
		}
	}

	public Instruction clone() {
		Instruction copy = new JumpInstruction(this.robotRun, getTgtLblNum());
		copy.setIsCommented( isCommented() );

		return copy;
	}

	public String[] toStringArray() {
		String[] ret;
		// Target label number
		if (getTgtLblNum() == -1) {
			ret = new String[] {"JMP", "LBL[...]"};
		} else {
			ret = new String[] {"JMP", "LBL[" + getTgtLblNum() + "]"};
		}

		return ret;
	}

	public int getTgtLblNum() {
		return tgtLblNum;
	}

	public void setTgtLblNum(int tgtLblNum) {
		this.tgtLblNum = tgtLblNum;
	}
}
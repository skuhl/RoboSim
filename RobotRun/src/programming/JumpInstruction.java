package programming;
import robot.RobotRun;

public class JumpInstruction extends Instruction {
	private int tgtLblNum;

	public JumpInstruction() {
		setTgtLblNum(-1);
	}

	public JumpInstruction(int l) {
		setTgtLblNum(l);
	}

	public Instruction clone() {
		Instruction copy = new JumpInstruction(getTgtLblNum());
		copy.setIsCommented( isCommented() );

		return copy;
	}

	/**
	 * Returns the index of the instruction to which to jump.
	 */
	public int execute() {
		Program p = RobotRun.getRobot().getActiveProg();

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

	public int getTgtLblNum() {
		return tgtLblNum;
	}

	public void setTgtLblNum(int tgtLblNum) {
		this.tgtLblNum = tgtLblNum;
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
}

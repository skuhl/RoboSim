package programming;
import core.RobotRun;
import processing.core.PApplet;

public class JumpInstruction extends Instruction {
	private int tgtLblNum;

	public JumpInstruction() {
		setTgtLblNum(-1);
	}

	public JumpInstruction(int l) {
		setTgtLblNum(l);
	}

	@Override
	public Instruction clone() {
		Instruction copy = new JumpInstruction(getTgtLblNum());
		copy.setIsCommented( isCommented() );

		return copy;
	}

	/**
	 * Returns the index of the instruction to which to jump.
	 */
	@Override
	public int execute() {
		Program p = RobotRun.getInstance().getActiveProg();

		if (p != null) {
			int lblIdx = p.findLabelIdx(getTgtLblNum());

			if (lblIdx != -1) {
				// Return destination instruction index
				return lblIdx;
			} else {
				PApplet.println("Invalid jump instruction!");
				return -1;
			}
		} else {
			PApplet.println("No active program!");
			return -1;
		}
	}

	public int getTgtLblNum() {
		return tgtLblNum;
	}

	public void setTgtLblNum(int tgtLblNum) {
		this.tgtLblNum = tgtLblNum;
	}

	@Override
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

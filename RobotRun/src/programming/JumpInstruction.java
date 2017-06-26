package programming;

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

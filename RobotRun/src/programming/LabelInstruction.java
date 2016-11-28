package programming;

public class LabelInstruction extends Instruction {
	private int labelNum;

	public LabelInstruction(int num) {
		labelNum = num;
	}

	public int getLabelNum() { return labelNum; }
	public void setLabelNum(int n) { labelNum = n; }

	public String[] toStringArray() {
		String[] fields = new String[1];
		// Label number
		if (labelNum == -1) {
			fields[0] = "LBL[...]";
		} else {
			fields[0] = String.format("LBL[%d]", labelNum);
		}

		return fields;
	}

	public Instruction clone() {
		Instruction copy = new LabelInstruction(labelNum);
		copy.setIsCommented( isCommented() );

		return copy;
	}
}
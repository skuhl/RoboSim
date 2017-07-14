package programming;

public class Instruction {
	
	protected boolean isCommented;

	public Instruction() {
		isCommented = false;
	}
	
	public Instruction(boolean isComm) {
		isCommented = isComm;
	}
	
	/**
	 * Create an independent replica of this instruction.
	 */
	@Override
	public Instruction clone() {
		return new Instruction(isCommented);
	}
	
	public boolean isCommented(){ return isCommented; }

	public void setIsCommented(boolean comFlag) { isCommented = comFlag; }

	public void toggleCommented() { isCommented = !isCommented; }

	@Override
	public String toString() {
		String[] fields = toStringArray();
		String str = new String();

		/* Return a stirng which is the concatenation of all the elements in
		 * this instruction's toStringArray() method, separated by spaces */
		for (int fdx = 0; fdx < fields.length; ++fdx) {
			str += fields[fdx];

			if (fdx < (fields.length - 1)) {
				str += " ";
			}
		}

		return str;
	}

	public String[] toStringArray() {
		return new String[] {"..."};
	}
}
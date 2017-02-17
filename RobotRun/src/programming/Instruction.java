package programming;

public class Instruction {
	boolean com;

	public Instruction() {
		com = false;
	}
	
	/**
	 * Create an independent replica of this instruction.
	 */
	public Instruction clone() {
		Instruction copy = new Instruction();
		copy.setIsCommented( isCommented() );

		return copy;
	}
	
	public int execute() { return 0; }
	
	public boolean isCommented(){ return com; }

	public void setIsCommented(boolean comFlag) { com = comFlag; }

	public void toggleCommented() { com = !com; }

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
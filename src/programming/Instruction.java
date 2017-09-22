package programming;

/**
 * Defines the basic functionality of an instruction in a program.
 * 
 * @author Joshua Hooker and Vincent Druckte
 */
public abstract class Instruction {
	
	/**
	 * Whether or not this instruction should be ignored during program
	 * execution.
	 */
	protected boolean isCommented;

	/**
	 * Initializes an instruction, which is not commented.
	 */
	public Instruction() {
		isCommented = false;
	}
	
	/**
	 * Initializes an instruction with the given comment state.
	 * 
	 * @param isComm	The comment state of this instruction
	 */
	public Instruction(boolean isComm) {
		isCommented = isComm;
	}
	
	@Override
	public abstract Instruction clone();
	
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
	
	/**
	 * Defines a set of strings, which visually define this instruction, when
	 * it is output on the pendant's screen.
	 * 
	 * @return	A set of strings which define the relevant fields of this
	 * 			instruction
	 */
	public abstract String[] toStringArray();
}
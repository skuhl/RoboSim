package regs;

/**
 * A general class used to link all the different types of registers used by a robotic arm.
 * 
 * @author Joshua Hooker and Vincent Druckte
 */
public abstract class Register {
	public String comment;
	public final int idx;
	
	public Register() {
		comment = null;
		idx = -1;
	}

	public Register(int i) {
		comment = null;
		idx = i;
	}
	
	public Register(int i, String comm) {
		idx = i;
		comment = comm;
	}
	
	/**
	 * A variant of the register toString().
	 * 
	 * @return	A string representation of the register's comment or label
	 */
	public String getLabel() {
		
		if (comment == null || comment.equals("")) {
			return String.format("%s[%d]", regPrefix(), idx + 1);
			
		} else {
			return String.format("%s[%s]", regPrefix(), comment);
		}
		
	}
	
	/**
	 * Returns the characters that distinguish one register's string form
	 * another.
	 * 
	 * @return	Data -> 'R'
	 * 			Position -> 'PR'
	 * 			IO -> 'IO'
	 */
	public abstract String regPrefix();
	
	@Override
	public String toString() {
		String idxStr = (idx < 0) ? "..." : Integer.toString(idx + 1);
		// Include the register's prefix and index
		return String.format("%s[%s]", regPrefix(), idxStr);
	}
	
	/**
	 * Allows for the comment that is associated with the register to be
	 * included in the register's string form.
	 * 
	 * @return	A regiter's string form with the comment
	 */
	public String toStringWithComm() {
		String comm = (comment == null) ? "" : comment;
		// Add space padding for the index as well as the comment
		return String.format("%s[%3d: %-16s]", regPrefix(), idx + 1, comm);
	}
}
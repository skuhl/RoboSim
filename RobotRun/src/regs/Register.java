package regs;

public abstract class Register {
	public final int idx;
	public String comment;
	
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
	 * Returns the characters that distinguish one register's string form from
	 * another.
	 * 
	 * @return	Data -> 'R'
	 * 			Position -> 'PR'
	 * 			IO -> 'IO'
	 */
	protected abstract String regPrefix();
	
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
	
	@Override
	public String toString() {
		String idxStr = (idx < 0) ? "..." : Integer.toString(idx + 1);
		// Include the register's prefix and index
		return String.format("%s[%s]", regPrefix(), idxStr);
	}
}
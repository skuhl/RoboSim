package regs;

import global.Fields;

/* A simple class designed to hold a state value along with a name. */
public class IORegister extends Register {
	
	protected int state;

	public IORegister() {
		super();
		state = Fields.OFF;
	}
	
	public IORegister(int idx, String name) {
		super(idx, name);
		state = Fields.OFF;
	}

	public IORegister(int i, String comm, int iniState) {
		super(i, comm);
		state = iniState;
	}
	
	
	public void setState(int newState) {
		state = newState;
	}
	
	public int getState() {
		return state;
	}
	
	@Override
	protected String regPrefix() {
		return "IO";
	}
	
	@Override
	public String toString() {
		String idxStr = (idx < 0) ? "..." : Integer.toString(idx);
		// Include the register's prefix and index
		return String.format("%s[%s]", regPrefix(), idxStr);
	}
}
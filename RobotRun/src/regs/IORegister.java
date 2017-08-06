package regs;

import global.Fields;

/**
 * A simple class designed to hold a state value along with a name.
 * 
 * @author Joshua Hooker and Vincent Druckte
 */
public class IORegister extends Register {
	
	protected boolean state;

	public IORegister() {
		super();
		state = Fields.OFF;
	}
	
	public IORegister(int idx, String name) {
		super(idx, name);
		state = Fields.OFF;
	}

	public IORegister(int i, String comm, boolean initState) {
		super(i, comm);
		state = initState;
	}
	
	
	public boolean getState() {
		return state;
	}
	
	@Override
	public String regPrefix() {
		return "IO";
	}
	
	public void setState(boolean newState) {
		state = newState;
	}
	
	public void toggleState() {
		if(state == Fields.OFF) {
			state = Fields.ON;
		} else {
			state = Fields.OFF;
		}
	}

	@Override
	public String toString() {
		String idxStr = (idx < 0) ? "..." : Integer.toString(idx);
		// Include the register's prefix and index
		return String.format("%s[%s]", regPrefix(), idxStr);
	}
}
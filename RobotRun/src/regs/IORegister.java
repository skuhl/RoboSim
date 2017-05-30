package regs;

import global.Fields;

/* A simple class designed to hold a state value along with a name. */
public class IORegister extends Register {
	public int state;

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
	
	@Override
	protected String regPrefix() {
		return "IO";
	}
}
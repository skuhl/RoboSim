package regs;

import global.Fields;

/* A simple class designed to hold a state value along with a name. */
public class IORegister extends Register {
	public final String name;
	public int state;

	public IORegister() {
		name = "";
		state = Fields.OFF;
	}

	public IORegister(int i, int iniState) {
		idx = i;
		name = "";
		state = iniState;
	}

	public IORegister(int i, String comm, int iniState) {
		idx = i;
		name = comm;
		state = iniState;
	}
}
package regs;
/* A simple class for a Register of the Robot Arm, which holds a value associated with a comment. */
public class DataRegister extends Register {
	public Float value;

	public DataRegister() {
		comment = null;
		value = null;
	}

	public DataRegister(int i) {
		idx = i;
		comment = null;
		value = null;
	}

	public DataRegister(int i, String c, Float v) {
		idx = i;
		comment = c;
		value = v;
	}
}
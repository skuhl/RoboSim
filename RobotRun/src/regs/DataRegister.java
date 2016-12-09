package regs;
/* A simple class for a Register of the Robot Arm, which holds a value associated with a comment. */
public class DataRegister extends Register {
	public Float value;

	public DataRegister() {
		super();
		value = null;
	}

	public DataRegister(int i) {
		super(i, null);
		value = null;
	}

	public DataRegister(int i, String c, Float v) {
		super(i, c);
		value = v;
	}
}
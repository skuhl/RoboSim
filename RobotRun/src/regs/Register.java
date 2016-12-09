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
}
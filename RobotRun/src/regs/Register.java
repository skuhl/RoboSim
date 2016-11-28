package regs;

public abstract class Register {
	protected String comment;
	protected int idx;

	public Register() {
		comment = null;
		idx = -1;
	}

	public Register(int i) {
		comment = null;
		idx = i;
	}

	public String getComment() { return comment; }
	public int getIdx() { return idx; }
	public String setComment(String s) { return comment = s; }
	public int setIdx(int i) { return idx = i; }
}
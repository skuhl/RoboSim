package robot;

public class CallFrame {
	private int tgtRID;
	private int tgtProgID;
	private int tgtInstID;
	
	public CallFrame(int rid, int pid, int inst) {
		tgtRID = rid;
		tgtProgID = pid;
		tgtInstID = inst;
	}
	
	public int getTgtRID() {
		return tgtRID;
	}

	public int getTgtProgID() {
		return tgtProgID;
	}

	public int getTgtInstID() {
		return tgtInstID;
	}
}

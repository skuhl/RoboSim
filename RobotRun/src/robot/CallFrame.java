package robot;

import programming.ProgExecution;

public class CallFrame {
	public int tgtRID;
	public ProgExecution tgtExec; 
	
	public CallFrame(int rid, ProgExecution exec) {
		tgtRID = rid;
		tgtExec = exec;
	}
}

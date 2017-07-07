package regs;

import global.Fields;

/**
 * Defines an I/O register, the state of which is linked to the trace functionality.
 * 
 * @author Joshua Hooker
 */
public class IORegTrace extends IORegister {
	
	/**
	 * A reference to the trace associated to this I/O register
	 */
	private RTrace traceRef;
	
	public IORegTrace(RTrace robotTrace) {
		super();
		state = Fields.OFF;
		traceRef = robotTrace;
	}
	
	public IORegTrace(int idx, String name, RTrace robotTrace) {
		super(idx, name);
		traceRef = robotTrace;
	}
	
	public IORegTrace(int idx, String name, int iniState, RTrace robotTrace) {
		super(idx, name, iniState);
		traceRef = robotTrace;
	}
	
	@Override
	public void setState(int newState) {
		if (state == Fields.ON && newState == Fields.OFF) {
			// Add break point
			traceRef.addPt(null);
		}
		
		super.setState(newState);
	}
}

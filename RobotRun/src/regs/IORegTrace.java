package regs;

import global.Fields;
import robot.RTrace;

/**
 * Defines an I/O register, the state of which is linked to the trace
 * functionality.
 * 
 * @author Joshua Hooker
 */
public class IORegTrace extends IORegister {
	
	/**
	 * A reference to the trace associated to this I/O register
	 */
	private RTrace traceRef;
	
	public IORegTrace(int idx, String name, boolean initState, RTrace robotTrace) {
		super(idx, name, initState);
		traceRef = robotTrace;
	}
	
	public IORegTrace(int idx, String name, RTrace robotTrace) {
		super(idx, name);
		traceRef = robotTrace;
	}
	
	public IORegTrace(RTrace robotTrace) {
		super();
		state = Fields.OFF;
		traceRef = robotTrace;
	}
	
	@Override
	public void setState(boolean newState) {
		if (state == Fields.ON && newState == Fields.OFF) {
			// Add break point
			traceRef.addPt(null);
		}
		
		state = newState;
	}
	
	@Override
	public void toggleState() {
		if(state == Fields.OFF) {
			state = Fields.ON;
		} else {
			state = Fields.OFF;
			traceRef.addPt(null);
		}
	}
}

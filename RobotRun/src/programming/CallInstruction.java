package programming;
import robot.RobotRun;
import robot.RoboticArm;

public class CallInstruction extends Instruction {
	private RoboticArm tgtDevice;
	private Program tgt;

	public CallInstruction(RoboticArm robot) {
		tgtDevice = robot;
		tgt = null;
	}
		
	public CallInstruction(RoboticArm tgtDevice, Program tgt) {
		this.tgtDevice = tgtDevice;
		this.tgt = tgt;
	}

	@Override
	public Instruction clone() {
		return new CallInstruction(tgtDevice, tgt);
	}

	// Getters and setters for a call instruction's program id field

	@Override
	public int execute() {		
		RoboticArm r = RobotRun.getActiveRobot();
		
		// Save the current program state on tgt robot
		tgtDevice.pushActiveProg(r);
		// Set the new program state
		tgtDevice.setActiveProg(tgt);
		tgtDevice.setActiveInstIdx(0);
		RobotRun.getInstance().setRobot(tgtDevice.RID);
		// Update the screen
		RobotRun.getInstance().getContentsMenu().reset();
		RobotRun.getInstance().updatePendantScreen();

		return 0;
	}
	
	public Program getProg() { return tgt; }
	
	public RoboticArm getTgtDevice() { return tgtDevice; }

	/**
	 * Returns the name of the program associated with this call
	 * statement, or "..." if the call statement's program index
	 * is invalid.
	 */
	private String progName() {
		return (tgt == null) ? "..." : tgt.getName();
	}

	public void setProg(Program p) { tgt = p; }
	
	public void setTgtDevice(RoboticArm tgt) { tgtDevice = tgt; }

	@Override
	public String toString() {
		if(tgtDevice == RobotRun.getActiveRobot()) {
			return "Call " + progName();
			
		} else {
			return "RCall " + progName();
		}
	}

	@Override
	public String[] toStringArray() {
		String[] ret = new String[2];
		ret[0] = (tgtDevice == RobotRun.getActiveRobot()) ? "Call" : "RCall";
		ret[1] = progName();

		return ret;
	}
}
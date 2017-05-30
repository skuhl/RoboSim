package programming;
import core.RobotRun;
import robot.RoboticArm;

public class CallInstruction extends Instruction {
	
	private RoboticArm tgtDevice;
	private Program tgt;
	
	/**
	 * Primarily used for loading programs. Since programs are loaded
	 * sequentially, some call instructions need a temporary
	 * reference to a program. This field is NOT always equivalent
	 * with the tgt's name, since the user can rename programs.
	 */
	private String loadedName;

	public CallInstruction(RoboticArm robot) {
		tgtDevice = robot;
		tgt = null;
		loadedName = "...";
	}
		
	public CallInstruction(RoboticArm tgtDevice, Program tgt) {
		this.tgtDevice = tgtDevice;
		this.tgt = tgt;

		loadedName = null;
	}
	
	public CallInstruction(RoboticArm tgtDevice, String tgtName) {
		this.tgtDevice = tgtDevice;
		tgt= null;
		this.loadedName = tgtName;
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
	
	public String getLoadedName() { return loadedName; }
	
	public RoboticArm getTgtDevice() { return tgtDevice; }

	public void setProg(Program p) { tgt = p; }
	
	public void setTgtDevice(RoboticArm tgt) { tgtDevice = tgt; }
	
	private String getProgName() {
		return (tgt == null) ? "..." : tgt.getName();
	}

	@Override
	public String toString() {
		if(tgtDevice == RobotRun.getActiveRobot()) {
			return "Call " + getProgName();
			
		} else {
			return "RCall " + getProgName();
		}
	}

	@Override
	public String[] toStringArray() {
		String[] ret = new String[2];
		ret[0] = (tgtDevice == RobotRun.getActiveRobot()) ? "Call" : "RCall";
		ret[1] = getProgName();

		return ret;
	}
}
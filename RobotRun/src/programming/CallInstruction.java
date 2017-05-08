package programming;
import robot.RobotRun;
import robot.RoboticArm;

public class CallInstruction extends Instruction {
	int progIdx;
	RoboticArm tgtDevice;

	public CallInstruction(RoboticArm robot) {
		tgtDevice = robot;
		progIdx = -1;
	}
		
	public CallInstruction(RoboticArm tgt, int pdx) {
		tgtDevice = tgt;
		progIdx = pdx;
	}

	@Override
	public Instruction clone() {
		return new CallInstruction(tgtDevice, progIdx);
	}

	// Getters and setters for a call instruction's program id field

	@Override
	public int execute() {
		//Test validity of progIdx
		if (progIdx < 0 || progIdx >= tgtDevice.numOfPrograms()) {
			return -1;
		}
		
		RoboticArm r = RobotRun.getActiveRobot();
		
		// Save the current program state on tgt robot
		tgtDevice.pushActiveProg(r);
		// Set the new program state
		tgtDevice.setActiveProgIdx(progIdx);
		tgtDevice.setActiveInstIdx(0);
		RobotRun.getInstance().setRobot(tgtDevice.RID);
		// Update the screen
		RobotRun.getInstance().getContentsMenu().reset();
		RobotRun.getInstance().updatePendantScreen();

		return 0;
	}
	
	public int getProgIdx() { return progIdx; }
	
	public RoboticArm getTgtDevice() { return tgtDevice; }

	/**
	 * Returns the name of the program associated with this call
	 * statement, or "..." if the call statement's program index
	 * is invalid.
	 */
	private String progName() {
		if (tgtDevice != null && tgtDevice.getProgram(progIdx) != null) {
			return tgtDevice.getProgram(progIdx).getName();
		}

		return "...";
	}

	public void setProgIdx(int pdx) { progIdx = pdx; }
	
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
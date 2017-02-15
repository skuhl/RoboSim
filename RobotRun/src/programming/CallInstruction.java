package programming;
import robot.RobotRun;
import robot.RoboticArm;

public class CallInstruction extends Instruction {
	int progIdx;
	RoboticArm tgtDevice;

	public CallInstruction() {
		progIdx = -1;
		tgtDevice = null;
	}

	public CallInstruction(int pdx, RoboticArm tgt) {
		progIdx = pdx;
		tgtDevice = tgt;
	}

	public Instruction clone() {
		return new CallInstruction(progIdx, tgtDevice);
	}

	// Getters and setters for a call instruction's program id field

	public int execute() {
		
		
		// TODO associate with the correct Robot ID (RID)
		if (progIdx < 0 || progIdx >= tgtDevice.numOfPrograms()) {
			// Invalid program id
			return -1;
		}
		
		// Save the current program state
		RobotRun.getRobot().pushActiveProg();
		// Set the new program state
		tgtDevice.setActiveProgIdx(progIdx);
		tgtDevice.setActiveInstIdx(0);
		RobotRun.getInstance().setActiveRobot(tgtDevice);
		// Update the screen
		RobotRun.getInstance().getContentsMenu().reset();
		RobotRun.getInstance().updateScreen();

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
		if (progIdx >= 0 && progIdx < tgtDevice.numOfPrograms()) {
			return tgtDevice.getProgram(progIdx).getName();
		}

		return "...";
	}

	public void setProgIdx(int pdx) { progIdx = pdx; }
	
	public void setTgtDevice(RoboticArm tgt) { tgtDevice = tgt; }

	public String toString() {
		String robot;
		if(tgtDevice != null) robot = "Robot" + tgtDevice.RID + " ";
		else				  robot = "... ";
		return "Call " + robot + progName();
	}

	public String[] toStringArray() {
		String[] ret = new String[3];
		ret[0] = "Call";
		ret[1] = tgtDevice != null ? "Robot" + tgtDevice.RID : "...";
		ret[2] = progName();

		return ret;
	}
}
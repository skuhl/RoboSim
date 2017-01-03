package programming;
import robot.RobotRun;

public class CallInstruction extends Instruction {
	int progIdx;

	public CallInstruction() {
		progIdx = -1;
	}

	public CallInstruction(int pdx) {
		progIdx = pdx;
	}

	public Instruction clone() {
		return new CallInstruction(progIdx);
	}

	// Getters and setters for a call instruction's program id field

	public int execute() {
		
		// TODO associate with the correct Robot ID (RID)
		if (progIdx < 0 && progIdx >= RobotRun.getRobot().numOfPrograms()) {
			// Invalid program id
			return -1;
		}

		int[] p = new int[2];
		p[0] = RobotRun.getInstance().getActive_prog();
		p[1] = RobotRun.getInstance().getActive_instr() + 1;
		RobotRun.getInstance().getCall_stack().push(p);

		RobotRun.getInstance().setActive_prog(progIdx);
		RobotRun.getInstance().setActive_instr(0);
		RobotRun.getInstance().setRow_select(0);
		RobotRun.getInstance().setCol_select(0);
		RobotRun.getInstance().setStart_render(0);
		RobotRun.getInstance().updateScreen();

		return 0;
	}
	public int getProgIdx() { return progIdx; }

	/**
	 * Returns the name of the program associated with this call
	 * statement, or "..." if the call statement's program index
	 * is invalid.
	 */
	private String progName() {
		if (progIdx >= 0 && progIdx < RobotRun.getRobot().numOfPrograms()) {
			return RobotRun.getRobot().getProgram(progIdx).getName();
		}

		return "...";
	}

	public void setProgIdx(int pdx) { progIdx = pdx; }

	public String toString() {
		return "Call " + progName();
	}

	public String[] toStringArray() {
		String[] ret = new String[2];
		ret[0] = "Call";
		ret[1] = progName();

		return ret;
	}
}
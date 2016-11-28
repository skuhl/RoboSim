package programming;
import robot.RobotRun;

public class CallInstruction extends Instruction {

	private RobotRun robotRun;
	int progIdx;

	public CallInstruction(RobotRun robotRun) {
		this.robotRun = robotRun;
		progIdx = -1;
	}

	public CallInstruction(RobotRun robotRun, int pdx) {
		this.robotRun = robotRun;
		progIdx = pdx;
	}

	public int execute() {

		if (progIdx < 0 && progIdx >= robotRun.getPrograms().size()) {
			// Invalid program id
			return -1;
		}

		int[] p = new int[2];
		p[0] = robotRun.getActive_prog();
		p[1] = robotRun.getActive_instr() + 1;
		robotRun.getCall_stack().push(p);

		this.robotRun.setActive_prog(progIdx);
		this.robotRun.setActive_instr(0);
		this.robotRun.setRow_select(0);
		this.robotRun.setCol_select(0);
		this.robotRun.setStart_render(0);
		this.robotRun.updateScreen();

		return 0;
	}

	// Getters and setters for a call instruction's program id field

	public int getProgIdx() { return progIdx; }
	public void setProgIdx(int pdx) { progIdx = pdx; }

	public Instruction clone() {
		return new CallInstruction(this.robotRun, progIdx);
	}

	public String toString() {
		return "Call " + progName();
	}

	public String[] toStringArray() {
		String[] ret = new String[2];
		ret[0] = "Call";
		ret[1] = progName();

		return ret;
	}

	/**
	 * Returns the name of the program associated with this call
	 * statement, or "..." if the call statement's program index
	 * is invalid.
	 */
	private String progName() {
		if (progIdx >= 0 && progIdx < this.robotRun.getPrograms().size()) {
			return this.robotRun.getPrograms().get(progIdx).getName();
		}

		return "...";
	}
}
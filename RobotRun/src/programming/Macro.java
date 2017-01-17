package programming;
import robot.RobotRun;

public class Macro {
	private Program prog;
	boolean manual;
	private int progIdx;
	private int num;

	public Macro(Program p, int pidx) {
		prog = p;
		progIdx = pidx;
		manual = false;
		num = -1;
	}

	public void clearNum() {
		if(num != -1) {
			RobotRun.getInstance().getSU_macro_bindings()[num] = null;
			num = -1;
		}
	}

	public void execute() {
		// Stop any prior Robot movement
		RobotRun.getInstance().getArmModel().halt();
		// Safeguard against editing a program while it is running
		RobotRun.getInstance().getContentsMenu().setColumnIdx(0);
		RobotRun.getInstance().setActive_prog(progIdx);
		RobotRun.getInstance().setActive_instr(0);

		RobotRun.getInstance().setExecutingInstruction(false);
		// Run single instruction when step is set
		RobotRun.getInstance().execSingleInst = RobotRun.getInstance().isStep();

		RobotRun.getInstance().setProgramRunning(true);
	}
	
	public boolean isManual() { return manual; }
	public void setManual(boolean b) { manual = b; }

	public Macro setNum(int n) {
		if(n <= 6 && n >= 0 && RobotRun.getInstance().getSU_macro_bindings()[n] == null) {
			clearNum();
			RobotRun.getInstance().getSU_macro_bindings()[n] = this;
			num = n;

			return this;
		}

		return null;
	}

	public void setProgram(Program p, int idx) { prog = p; progIdx = idx; }

	public String toString() {
		String[] str = toStringArray();
		return str[0] + " " + str[1] + " " + str[2];
	}

	public String[] toStringArray() {
		String[] ret = new String[3];
		int name_pad = RobotRun.max(16 - prog.getName().length(), 0);

		ret[0] = String.format("[%-"+name_pad+"s]", prog.getName());
		ret[1] = manual ? "MF" : "SU";
		if(manual) ret[2] = "_";
		else {
			switch(num) {
			case 0: ret[2] = "TOOL1";  break;
			case 1: ret[2] = "TOOL2";  break;
			case 2: ret[2] = "MVMU";   break;
			case 3: ret[2] = "SETUP";  break;
			case 4: ret[2] = "STATUS"; break;
			case 5: ret[2] = "PSON";   break;
			case 6: ret[2] = "IO";     break;
			default: ret[2] = "...";   break;
			}
		}

		return ret;
	}
}
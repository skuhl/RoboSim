package programming;
import core.RobotRun;

public class Macro {
	boolean manual;
	private int progIdx;
	private int num;

	public Macro(int pidx) {
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
	
	public int getProgIdx() {
		return progIdx;
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

	public void setProgram(int idx) { progIdx = idx; }

	@Override
	public String toString() {
		String[] str = toStringArray();
		return str[0] + " " + str[1] + " " + str[2];
	}

	public String[] toStringArray() {
		String[] ret = new String[3];
		String name = RobotRun.getInstanceRobot().getProgram(progIdx).getName();
		int name_pad = Math.max(16 - name.length(), 0);

		ret[0] = String.format("[%-"+name_pad+"s]", name);
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
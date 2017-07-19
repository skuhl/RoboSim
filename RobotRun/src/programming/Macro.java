package programming;

import robot.RoboticArm;

public class Macro {
	boolean manual;
	private RoboticArm robot;
	private int progIdx;
	private int keyNum;

	public Macro(RoboticArm r, int pdx) {
		manual = false;
		robot = r;
		progIdx = pdx;
		keyNum = -1;
	}
	
	public Macro(boolean mf, RoboticArm r, int pdx, int num) {
		manual = mf;
		robot = r;
		progIdx = pdx;
		keyNum = num;
	}

	public void clearNum() {
		if(keyNum != -1) {
			robot.getMacroKeyBinds()[keyNum] = null;
			keyNum = -1;
		}
	}
	
	public boolean isManual() { return manual; }
	public RoboticArm getRobot() { return robot; }
	public int getProgIdx() { return progIdx; }	
	public int getKeyNum() { return keyNum; }
	
	public void setManual(boolean b) { manual = b; }
	public void setProgIdx(int pdx) { progIdx = pdx; }
	
	public Macro setNum(int n) {
		if(n <= 6 && n >= 0 && robot.getMacroKeyBinds()[n] == null) {
			clearNum();
			robot.getMacroKeyBinds()[n] = this;
			keyNum = n;

			return this;
		}

		return null;
	}

	@Override
	public String toString() {
		String[] str = toStringArray();
		return str[0] + " " + str[1] + " " + str[2];
	}

	public String[] toStringArray() {
		String[] ret = new String[3];
		String name = robot.getProgram(progIdx).getName();
		int name_pad = Math.max(16 - name.length(), 0);

		ret[0] = String.format("[%-"+name_pad+"s]", name);
		ret[1] = manual ? "MF" : "SU";
		if(manual) ret[2] = "_";
		else {
			switch(keyNum) {
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
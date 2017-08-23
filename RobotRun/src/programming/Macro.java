package programming;

import robot.RoboticArm;

public class Macro {
	boolean manual;
	private int keyNum;
	private Program prog;
	private RoboticArm robot;

	public Macro(boolean mf, RoboticArm r, Program p, int num) {
		manual = mf;
		robot = r;
		prog = p;
		keyNum = num;
	}
	
	public Macro(RoboticArm r, Program p) {
		manual = false;
		robot = r;
		prog = p;
		keyNum = -1;
	}
	
	public int getKeyNum() { return keyNum; }
	public Program getProg() { return prog; }
	public RoboticArm getRobot() { return robot; }	
	public boolean isManual() { return manual; }
	
	public void setManual(boolean b) { manual = b; }
	public void setKeyNum(int n) {
		keyNum = n;
	}
	
	public void setProg(Program p) { prog = p; }

	@Override
	public String toString() {
		String[] str = toStringArray();
		return str[0] + " " + str[1] + " " + str[2];
	}

	public String[] toStringArray() {
		String[] ret = new String[3];
		String name = (prog == null) ? "_" : prog.getName();
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
			case 6: ret[2] = "FCTN";  break;
			default: ret[2] = "...";   break;
			}
		}

		return ret;
	}
}
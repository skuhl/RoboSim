package programming;

import robot.RobotRun;
import robot.RoboticArm;

/**
 * A placeholder class used when loading call instructions from a save file.
 * 
 * @author Joshua Hooker
 */
public class CallInstPlaceholder extends Instruction {
	
	/**
	 * The target robot
	 */
	public RoboticArm tgtDevice;
	
	/**
	 * The name of the target program
	 */
	public String tgtName;
	
	public CallInstPlaceholder(RoboticArm r, String name) {
		tgtDevice = r;
		tgtName = name;
	}
	
	@Override
	public String[] toStringArray() {
		String[] ret = new String[2];
		ret[0] = "PHCall";
		ret[1] = tgtName;

		return ret;
	}
	
}

package programming;

import robot.RoboticArm;

/**
 * 
 * 
 * @author Joshua Hooker
 */
public class Process {
	private RoboticArm parent;
	private int progIdx;
	private int activeInst;
	
	/**
	 * Creates a process on the given Robot from the program of the given
	 * index.
	 * 
	 * @param p		The parent robot of the process
	 * @param pdx	The index of the parent program
	 */
	public Process(RoboticArm p, int pdx) {
		parent = p;
		progIdx = pdx;
		activeInst = -1;
	}
	
	/**
	 * 
	 */
	public void executeProgram() {
		if (parent != null) {
			Program p = parent.getProgram(progIdx);
			
			if (p != null) {
				
			}
		}
	}
}

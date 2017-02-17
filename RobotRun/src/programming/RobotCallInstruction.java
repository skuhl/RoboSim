package programming;

import robot.RobotRun;

/**
 * Allows a Robot to call a program, which is stored in another Robot.
 * 
 * @author Joshua Hooker
 */
public class RobotCallInstruction extends CallInstruction {
	/*
	 * Default constructor
	 */
	public RobotCallInstruction() {
		super(RobotRun.getInstance().getInactiveRobot());
	}
	
	/**
	 * Creates a call instruction with the given Robot ID and program index.
	 * 
	 * @param rid		The ID of the Robot to set as active
	 * @param progIdx	The index of the program to begin execution
	 */
	public RobotCallInstruction(int progIdx) {
		super(RobotRun.getInstance().getInactiveRobot(), progIdx);
	}
}

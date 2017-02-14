package programming;

import robot.RobotRun;

/**
 * Allows a Robot to call a program, which is stored in another Robot.
 * 
 * @author Joshua Hooker
 */
public class RobotCall extends CallInstruction {
	/**
	 * The ID of the Robot associated with the program to call
	 */
	private int RID;
	
	/**
	 * Default constructor
	 */
	public RobotCall() {
		super();
		RID = -1;
	}
	
	/**
	 * Creates a call instruction with the given Robot ID and program index.
	 * 
	 * @param rid		The ID of the Robot to set as active
	 * @param progIdx	The index of the program to begin execution
	 */
	public RobotCall(int rid, int progIdx) {
		super(progIdx);
		RID = rid;
	}
	
	public int getRID() { return RID; }
	
	@Override
	public int execute() {
		RobotRun.getInstance().callRobot(RID, getProgIdx());
		return 0;
	}
	
	@Override
	public Instruction clone() {
		return new RobotCall(RID, getProgIdx());
	}
}

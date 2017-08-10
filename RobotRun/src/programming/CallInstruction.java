package programming;

import core.Pointer;
import robot.RoboticArm;

public class CallInstruction extends Instruction {
	
	private static Pointer<RoboticArm> robotRef;
	
	/**
	 * Sets the robotic arm reference for all call instructions. This should be
	 * a reference to the active robot of the application.
	 * 
	 * @param ref	The active robot reference
	 */
	public static void setRobotRef(Pointer<RoboticArm> ref) {
		robotRef = ref;
	}
	
	/**
	 * The ID of the robot, with which this call instruction is associated.
	 * This field is only valid when initializing this instruction's parent
	 * program after loading the instructions from a binary file.
	 */
	private int loadedID;
	
	/**
	 * Primarily used for loading programs. Since programs are loaded
	 * sequentially, some call instructions need a temporary
	 * reference to a program. This field is NOT always equivalent
	 * with the tgt's name, since the user can rename programs.
	 */
	private String loadedName;
	
	private Program tgt;

	private RoboticArm tgtDevice;
		
	public CallInstruction(int tgtDID, String tgtName) {
		tgtDevice = null;
		tgt = null;
		
		loadedID = tgtDID;
		loadedName = tgtName;
	}
	
	public CallInstruction(RoboticArm robot) {
		tgtDevice = robot;
		tgt = null;
		
		loadedID = -1;
		loadedName = null;
	}
	
	public CallInstruction(RoboticArm tgtDevice, Program tgt) {
		this.tgtDevice = tgtDevice;
		this.tgt = tgt;
		
		loadedID = -1;
		loadedName = null;
	}

	@Override
	public Instruction clone() {
		return new CallInstruction(tgtDevice, tgt);
	}
	
	public int getLoadedID() {
		return loadedID;
	}
	
	public String getLoadedName() {
		return loadedName;
	}
	
	public Program getProg() {
		return tgt;
	}
	
	public RoboticArm getTgtDevice() {
		return tgtDevice;
	}

	public void setProg(Program p) {
		tgt = p;
	}
	
	public void setTgtDevice(RoboticArm tgt) {
		tgtDevice = tgt;
	}
	
	@Override
	public String toString() {
		if(tgtDevice == robotRef.get()) {
			return "Call " + getProgName();
			
		} else {
			return "RCall " + getProgName();
		}
	}

	@Override
	public String[] toStringArray() {
		String[] ret = new String[2];
		ret[0] = (tgtDevice == robotRef.get()) ? "Call" : "RCall";
		ret[1] = getProgName();

		return ret;
	}

	private String getProgName() {
		return (tgt == null) ? "..." : tgt.getName();
	}
}
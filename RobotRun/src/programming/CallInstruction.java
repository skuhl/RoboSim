package programming;

import core.Pointer;
import robot.RoboticArm;

public class CallInstruction extends Instruction {
	
	private static Pointer<RoboticArm> robotRef;
	
	private RoboticArm tgtDevice;
	private Program tgt;
	
	/**
	 * TODO comment this
	 */
	private int loadedID;
	
	/**
	 * Primarily used for loading programs. Since programs are loaded
	 * sequentially, some call instructions need a temporary
	 * reference to a program. This field is NOT always equivalent
	 * with the tgt's name, since the user can rename programs.
	 */
	private String loadedName;

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
	
	public CallInstruction(int tgtDID, String tgtName) {
		tgtDevice = null;
		tgt = null;
		
		loadedID = tgtDID;
		loadedName = tgtName;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param ref
	 */
	public static void setRobotRef(Pointer<RoboticArm> ref) {
		robotRef = ref;
	}

	@Override
	public Instruction clone() {
		return new CallInstruction(tgtDevice, tgt);
	}
	
	public Program getProg() {
		return tgt;
	}
	
	public int getLoadedID() {
		return loadedID;
	}
	
	public String getLoadedName() {
		return loadedName;
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
	
	private String getProgName() {
		return (tgt == null) ? "..." : tgt.getName();
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
}
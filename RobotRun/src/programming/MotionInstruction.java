package programming;

/**
 * TODO general comments
 * 
 * @author Joshua Hooker
 */
public abstract class MotionInstruction extends Instruction  {

	protected int motionType;
	protected float spdModifier;
	protected int termination;
	
	public MotionInstruction(boolean isComm) {
		// Doesn't do much
		super(isComm);
	}
	
	public MotionInstruction(boolean isComm, int mType, float spdMod,
			int term) {
		
		super(isComm);
		motionType = mType;
		spdModifier = spdMod;
		termination = term;
	}
	
	public abstract MotionInstruction clone();
	
	public int getMotionType() {
		return motionType;
	}
	
	public float getSpdMod() {
		return spdModifier;
	}
	
	public int getTermination() {
		return termination;
	}
	
	public void setMotionType(int mType) {
		motionType = mType;
	}
	
	public void setSpdMod(float spdMod) {
		spdModifier = spdMod;
	}
	
	public void setTermination(int term) {
		termination = term;
	}
	
	public abstract String[] toStringArray();
}


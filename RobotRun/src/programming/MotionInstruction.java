package programming;

/**
 * TODO general comments
 * 
 * @author Joshua Hooker and Vincent Druckte
 */
public abstract class MotionInstruction extends Instruction  {

	protected int motionType;
	protected int posType;
	protected int posIdx;
	protected float spdModifier;
	protected int termination;
	
	public MotionInstruction(boolean isComm) {
		// Doesn't do much
		super(isComm);
	}
	
	public MotionInstruction(boolean isComm, int mType, int posType,
			int posIdx, float spdMod, int term) {
		
		super(isComm);
		motionType = mType;
		this.posType = posType;
		this.posIdx = posIdx;
		spdModifier = spdMod;
		termination = term;
	}
	
	public abstract MotionInstruction clone();
	
	public int getMotionType() {
		return motionType;
	}
	
	public int getPosIdx() {
		return posIdx;
	}
	
	public int getPosType() {
		return posType;
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
	
	public void setPosIdx(int idx) {
		posIdx = idx;
	}
	
	public void setPosType(int type) {
		posType = type;
	}
	
	public void setSpdMod(float spdMod) {
		spdModifier = spdMod;
	}
	
	public void setTermination(int term) {
		termination = term;
	}
	
	public abstract String[] toStringArray();
}


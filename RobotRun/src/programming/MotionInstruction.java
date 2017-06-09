package programming;

import global.Fields;
import robot.RoboticArm;

/**
 * TODO general comments
 * 
 * @author Joshua Hooker
 */
public class MotionInstruction extends Instruction  {

	private int motionType;
	private boolean usePReg;
	private int posIdx;
	private boolean circUsePReg;
	private int circPosIdx;
	private float spdModifier;
	private int termination;
	private int tFrameIdx;
	private int uFrameIdx;
	private int offsetType;
	private int offRegIdx;
	
	public MotionInstruction() {
		super(false);
		// Doesn't do much ...
	}
	
	public MotionInstruction(int mType, boolean usePReg, int posIdx,
			float spdMod, int term) {
		
		this(false, mType, usePReg, posIdx, false, -1, spdMod, term, -1, -1,
				Fields.OFFSET_NONE, -1);
	}
	
	public MotionInstruction(int mType, int posIdx, float spdMod, int term,
			int toolIdx, int userIdx) {
		
		this(false, mType, false, posIdx, false, -1, spdMod, term, toolIdx,
				userIdx, Fields.OFFSET_NONE, -1);
	}
	
	public MotionInstruction(boolean isComm, int mType, boolean usePReg,
			int posIdx, boolean circUsePreg, int circPosIdx, float spdMod,
			int term, int toolIdx, int userIdx, int offType, int offRegIdx) {
		
		super(isComm);
		motionType = mType;
		this.usePReg = usePReg;
		this.posIdx = posIdx;
		this.circPosIdx = circPosIdx;
		spdModifier = spdMod;
		tFrameIdx = toolIdx;
		uFrameIdx = userIdx;
		termination = term;
		offsetType = offType;
		this.offRegIdx = offRegIdx;
	}
	
	public boolean circUsePReg() {
		return circUsePReg;
	}
	
	@Override
	public MotionInstruction clone() {
		return new MotionInstruction(com, motionType, usePReg, posIdx,
				circUsePReg, circPosIdx, spdModifier, termination, tFrameIdx,
				uFrameIdx, offsetType, offRegIdx);
	}
	
	public int getCircPosIdx() {
		return circPosIdx;
	}
	
	public int getMotionType() {
		return motionType;
	}
	
	public int getOffsetIdx() {
		return offRegIdx;
	}
	
	public int getOffsetType() {
		return offsetType;
	}
	
	public int getPosIdx() {
		return posIdx;
	}
	
	public float getSpdMod() {
		return spdModifier;
	}
	
	public int getTermination() {
		return termination;
	}
	
	public int getTFrameIdx() {
		return tFrameIdx;
	}
	
	public int getUFrameIdx() {
		return uFrameIdx;
	}
	
	public void setCircPosIdx(int idx) {
		circPosIdx = idx;
	}
	
	public void setCircRegType(boolean usePReg) {
		circUsePReg = usePReg;
	}
	
	public void setMotionType(int mType) {
		motionType = mType;
	}
	
	public void setOffsetIdx(int idx) {
		offRegIdx = idx;
	}
	
	public void setOffsetType(int type) {
		offsetType = type;
	}
	
	public void setPosIdx(int idx) {
		posIdx = idx;
	}
	
	public void setRegType(boolean usePReg) {
		this.usePReg = usePReg;
	}
	
	public void setSpdMod(float spdMod) {
		spdModifier = spdMod;
	}
	
	public void setTermination(int term) {
		termination = term;
	}
	
	public void setTFrameIdx(int idx) {
		tFrameIdx = idx;
	}
	
	public void setUFrameIdx(int idx) {
		uFrameIdx = idx;
	}
	
	@Override
	public String[] toStringArray() {
		int fieldLen = 6;
		
		if (motionType == Fields.MTYPE_CIRCULAR) {
			fieldLen += 3;
		}
		
		if (offsetType == Fields.OFFSET_PREG) {
			fieldLen += 1;	
		}
		
		String[] fields = new String[fieldLen];
		
		// Motion type symbol
		int idx = 0;
		if (motionType == Fields.MTYPE_CIRCULAR) {
			fields[idx] = "C";
			
		} else if (motionType == Fields.MTYPE_LINEAR) {
			fields[idx] = "L";
			
		} else {
			fields[idx] = "J";
		}
		
		// Position type
		++idx;
		if (usePReg()) {
			fields[idx] = "PR[";
			
		} else {
			fields[idx] = " P[";
		}
		
		// Position index
		++idx;
		if (posIdx == -1) {
			fields[idx] = "...]";
			
		} else {
			fields[idx] = String.format("%d]", posIdx + 1);
		}
		
		// Motion speed modifier
		++idx;
		if (motionType == Fields.MTYPE_JOINT) {
			fields[idx] = String.format("%d%%", Math.round(spdModifier * 100f));
			
		} else {
			fields[idx] = String.format("%dmm/s",
					Math.round(RoboticArm.motorSpeed * spdModifier));
		}
		
		// Termination percent
		++idx;
		if (termination == 0) {
			fields[idx] = "FINE";
			
		} else {
			fields[idx] = String.format("CONT%d", termination);
		}
		
		// Offset
		++idx;
		if(offsetType == Fields.OFFSET_PREG) {
			fields[idx] = "OFST PR[";
			++idx;
			
			if (offRegIdx == -1) {
				fields[idx] = "...]";
				
			} else {
				fields[idx] = String.format("%d]", offRegIdx + 1);
			}
			
		} else {
			fields[idx] = "\0";
		}
		
		// Circular motion fields
		++idx;
		if (motionType == Fields.MTYPE_CIRCULAR) {
			fields[idx] = "\n";
			
			// Circular position type
			++idx;
			if (circUsePReg()) {
				fields[idx] = ":PR[";
				
			} else {
				fields[idx] = ": P[";
			}
			
			// Circular position index
			++idx;
			if (circPosIdx == -1) {
				fields[idx] = "...]";
				
			} else {
				fields[idx] = String.format("%d]", circPosIdx + 1);
			}	
		}
		
		return fields;
	}
	
	public boolean usePReg() {
		return usePReg;
	}
}

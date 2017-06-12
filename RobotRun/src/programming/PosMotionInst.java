package programming;

import global.Fields;
import robot.RoboticArm;

public class PosMotionInst extends MotionInstruction {
	
	private boolean usePReg;
	private int posIdx;
	private boolean circUsePReg;
	private int circPosIdx;
	private int tFrameIdx;
	private int uFrameIdx;
	private int offsetType;
	private int offRegIdx;
	
	public PosMotionInst() {
		super(false);
		// Doesn't do much ...
	}
	
	public PosMotionInst(int mType, boolean usePReg, int posIdx,
			float spdMod, int term) {
		
		this(false, mType, usePReg, posIdx, false, -1, spdMod, term, -1, -1,
				Fields.OFFSET_NONE, -1);
	}
	
	public PosMotionInst(int mType, int posIdx, float spdMod, int term,
			int toolIdx, int userIdx) {
		
		this(false, mType, false, posIdx, false, -1, spdMod, term, toolIdx,
				userIdx, Fields.OFFSET_NONE, -1);
	}
	
	public PosMotionInst(boolean isComm, int mType, boolean usePReg,
			int posIdx, boolean circUsePreg, int circPosIdx, float spdMod,
			int term, int toolIdx, int userIdx, int offType, int offRegIdx) {
		
		super(isComm, mType, spdMod, term);
		this.usePReg = usePReg;
		this.posIdx = posIdx;
		this.circPosIdx = circPosIdx;
		tFrameIdx = toolIdx;
		uFrameIdx = userIdx;
		offsetType = offType;
		this.offRegIdx = offRegIdx;
	}
	
	public boolean circUsePReg() {
		return circUsePReg;
	}
	
	@Override
	public PosMotionInst clone() {
		return new PosMotionInst(com, motionType, usePReg, posIdx, circUsePReg,
				circPosIdx, spdModifier, termination, tFrameIdx, uFrameIdx,
				offsetType, offRegIdx);
	}
	
	public int getCircPosIdx() {
		return circPosIdx;
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

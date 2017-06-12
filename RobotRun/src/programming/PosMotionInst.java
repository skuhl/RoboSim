package programming;

import global.Fields;
import robot.RoboticArm;

/**
 * TODO general comments
 * 
 * @author Joshua Hooker
 */
public class PosMotionInst extends MotionInstruction {
	
	private int circPosType;
	private int circPosIdx;
	private int tFrameIdx;
	private int uFrameIdx;
	private int offsetType;
	private int offRegIdx;
	
	public PosMotionInst() {
		super(false);
		// Doesn't do much ...
	}
	
	public PosMotionInst(int mType, int posType, int posIdx, float spdMod,
			int term) {
		
		this(false, mType, posType, posIdx, Fields.PTYPE_PROG, -1,
				spdMod, term, -1, -1, Fields.OFFSET_NONE, -1);
	}
	
	public PosMotionInst(int mType, int posIdx, float spdMod, int term,
			int toolIdx, int userIdx) {
		
		this(false, mType, Fields.PTYPE_PROG, posIdx, Fields.PTYPE_PROG, -1,
				spdMod, term, toolIdx, userIdx, Fields.OFFSET_NONE, -1);
	}
	
	public PosMotionInst(boolean isComm, int mType, int posType, int posIdx,
			int circPosType, int circPosIdx, float spdMod, int term,
			int toolIdx, int userIdx, int offType, int offRegIdx) {
		
		super(isComm, mType, posType, posIdx, spdMod, term);
		this.circPosType = circPosType;
		this.circPosIdx = circPosIdx;
		tFrameIdx = toolIdx;
		uFrameIdx = userIdx;
		offsetType = offType;
		this.offRegIdx = offRegIdx;
	}
	
	@Override
	public PosMotionInst clone() {
		return new PosMotionInst(com, motionType, posType, posIdx, circPosType,
				circPosIdx, spdModifier, termination, tFrameIdx, uFrameIdx,
				offsetType, offRegIdx);
	}
	
	public int getCircPosIdx() {
		return circPosIdx;
	}
	
	public int getCircPosType() {
		return circPosType;
	}
	
	public int getOffsetIdx() {
		return offRegIdx;
	}
	
	public int getOffsetType() {
		return offsetType;
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
	
	public void setCircPosType(int type) {
		circPosType = type;
	}
	
	public void setOffsetIdx(int idx) {
		offRegIdx = idx;
	}
	
	public void setOffsetType(int type) {
		offsetType = type;
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
		if (posType == Fields.PTYPE_PREG) {
			fields[idx] = "PR[";
			
		} else if (posType == Fields.PTYPE_PROG) {
			fields[idx] = " P[";
			
		} else {
			fields[idx] = "  [";
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
			if (circPosType == Fields.PTYPE_PREG) {
				fields[idx] = ":PR[";
				
			} else if (circPosType == Fields.PTYPE_PROG) {
				fields[idx] = ": P[";
				
			} else {
				fields[idx] = ":  [";
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
}

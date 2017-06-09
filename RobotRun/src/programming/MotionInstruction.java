package programming;

import global.Fields;
import robot.RoboticArm;

public class MotionInstruction extends Instruction  {
	private int motionType;
	private int positionNum;
	private int offsetRegNum;
	private boolean offsetActive;
	private boolean isGPosReg;
	private float speed;
	private int termination;
	private int userFrame;
	private int toolFrame;
	private MotionInstruction circSubInstr;
	
	/**
	 * Default constructor
	 */
	public MotionInstruction() {
		// Doesn't do much ...
	}

	public MotionInstruction(int type, int pos, boolean globl, float spd, int term) {
		motionType = type;
		positionNum = pos;
		offsetRegNum = -1;
		offsetActive = false;
		isGPosReg = globl;
		speed = spd;
		termination = term;
		userFrame = -1;
		toolFrame = -1;
		if(motionType != -1) {
			circSubInstr = new MotionInstruction(-1, -1, false, 100, 0);
		} else {
			circSubInstr = null;
		}
	}

	public MotionInstruction(int type, int pos, boolean globl, float spd, int term, int uf,
			int tf) {
		
		motionType = type;
		positionNum = pos;
		offsetRegNum = -1;
		offsetActive = false;
		isGPosReg = globl;
		speed = spd;
		termination = term;
		userFrame = uf;
		toolFrame = tf;
		if(motionType != -1) {
			circSubInstr = new MotionInstruction(-1, -1, false, 100, 0, uf, tf);
		} else {
			circSubInstr = null;
		}
	}

	/**
	 * Verify that the given frame indices match those of the
	 * instructions frame indices.
	 */
	public boolean checkFrames(int activeToolIdx, int activeFrameIdx) {
		return (toolFrame == activeToolIdx) && (userFrame == activeFrameIdx);
	}
	@Override
	public Instruction clone() {
		Instruction copy = new MotionInstruction(motionType, positionNum, isGPosReg, speed, termination, userFrame, toolFrame);
		copy.setIsCommented( isCommented() );

		return copy;
	}
	
	public int getMotionType() { return motionType; }
	public int getOffset() { return offsetRegNum; } 
	
	public int getPositionNum() { return positionNum; }
	public MotionInstruction getSecondaryPoint() { return circSubInstr; }
	public float getSpeed() { return speed; }
	
	public int getTermination() { return termination; }
	public int getToolFrame() { return toolFrame; }
	public int getUserFrame() { return userFrame; }
	
	public void setGlobalPosRegUse(boolean in) { isGPosReg = in; }
	public void setMotionType(int in) { motionType = in; }
	public void setOffset(int in) { offsetRegNum = in; }
	public void setPositionNum(int in) { positionNum = in; }
	public void setSecondaryPoint(MotionInstruction p) { circSubInstr = p; }
	public void setSpeed(float in) { speed = in; }

	public void setTermination(int in) { termination = in; }

	public void setToolFrame(int in) { toolFrame = in; }

	public void setUserFrame(int in) { userFrame = in; }

	public boolean toggleOffsetActive() { return (offsetActive = !offsetActive); }

	@Override
	public String[] toStringArray() {
		String[] fields;
		int instrLen, subInstrLen;

		if(motionType == Fields.MTYPE_CIRCULAR && circSubInstr != null) {
			instrLen = offsetActive ? 7 : 6;
			subInstrLen = circSubInstr.offsetActive ? 5 : 4;      
			fields = new String[instrLen + subInstrLen];
		} else {
			instrLen = offsetActive ? 6 : 5;
			subInstrLen = 0;
			fields = new String[instrLen];
		}

		// Motion type
		switch(motionType) {
		case Fields.MTYPE_JOINT:
			fields[0] = "J";
			break;
		case Fields.MTYPE_LINEAR:
			fields[0] = "L";
			break;
		case Fields.MTYPE_CIRCULAR:
			fields[0] = "C";
			break;
		default:
			fields[0] = "\0";
		}

		// Regster type
		if (isGPosReg) {
			fields[1] = "PR[";
		} else {
			fields[1] = "P[";
		}

		// Register index
		if(positionNum == -1) {
			fields[2] = "...]";
		} else {
			fields[2] = String.format("%d]", positionNum + 1);
		}

		// Speed
		if (motionType == Fields.MTYPE_JOINT) {
			fields[3] = String.format("%d%%", Math.round(speed * 100f));
		} else {
			fields[3] = String.format("%dmm/s", Math.round(RoboticArm.motorSpeed * speed));
		}

		// Termination percent
		if (termination == 0) {
			fields[4] = "FINE";
		} else {
			fields[4] = String.format("CONT%d", termination);
		}

		if(offsetActive) {
			if(offsetRegNum == -1) {
				fields[5] = "OFST PR[...]";
			} else {
				fields[5] = String.format("OFST PR[%d]", offsetRegNum + 1);
			}
		}

		if(motionType == Fields.MTYPE_CIRCULAR) {
			String[] secondary = circSubInstr.toStringArray();
			fields[instrLen - 1] = "\n";
			fields[instrLen] = ":" + secondary[1];
			fields[instrLen + 1] = secondary[2];
			fields[instrLen + 2] = secondary[3];
			fields[instrLen + 3] = secondary[4];
			if(subInstrLen > 4) {
				fields[instrLen + 4] = secondary[5];
			}
		}

		return fields;
	}

	public boolean usesGPosReg() { return isGPosReg; }
}

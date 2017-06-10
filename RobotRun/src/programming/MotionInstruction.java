package programming;

import core.Scenario;
import global.Fields;
import robot.RoboticArm;

public class MotionInstruction extends Instruction  {
	private int motionType;
	private int registerType;
	private int positionNum;
	private float speed;
	private int termination;
	private boolean offsetActive;
	private int offsetRegNum;
	
	private int userFrame;
	private int toolFrame;
	
	private MotionInstruction circSubInstr;
	private Scenario scene;
	
	/**
	 * Default constructor
	 */
	public MotionInstruction() {
		// Doesn't do much ...
	}

	public MotionInstruction(int type, int pos, boolean isGlobl, float spd, int term) {
		motionType = type;
		registerType = isGlobl ? Fields.MREGTYPE_GPOS : Fields.MREGTYPE_POS;
		positionNum = pos;
		speed = spd;
		termination = term;
		offsetActive = false;
		offsetRegNum = -1;
		
		userFrame = -1;
		toolFrame = -1;
		
		scene = null;
		
		if(motionType != -1) {
			circSubInstr = new MotionInstruction(-1, -1, false, 100, 0);
		} else {
			circSubInstr = null;
		}
	}

	public MotionInstruction(int type, int pos, boolean isGlobl, float spd, int term, int uf, int tf) {
		motionType = type;
		registerType = isGlobl ? Fields.MREGTYPE_GPOS : Fields.MREGTYPE_POS;
		positionNum = pos;
		speed = spd;
		termination = term;
		offsetActive = false;
		offsetRegNum = -1;
		
		userFrame = uf;
		toolFrame = tf;
		
		scene = null;
		
		if(motionType != -1) {
			circSubInstr = new MotionInstruction(-1, -1, false, 100, 0, uf, tf);
		} else {
			circSubInstr = null;
		}
	}
	
	public MotionInstruction(int type, int pos, float spd, int term, Scenario s) {
		motionType = type;
		registerType = Fields.MREGTYPE_OBJ;
		positionNum = pos;
		speed = spd;
		termination = term;
		offsetActive = false;
		offsetRegNum = -1;
		
		userFrame = -1;
		toolFrame = -1;
		
		scene = s;
		
		if(motionType != -1) {
			circSubInstr = new MotionInstruction(-1, -1, false, 100, 0);
		} else {
			circSubInstr = null;
		}
	}
	
	private MotionInstruction(int type, int rType, int pos, float spd, int term, int uf, int tf, Scenario s) {
		motionType = type;
		registerType = rType;
		positionNum = pos;
		speed = spd;
		termination = term;
		offsetActive = false;
		offsetRegNum = -1;
		
		userFrame = uf;
		toolFrame = tf;
		
		scene = s;
		
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
		Instruction copy = new MotionInstruction(motionType, registerType, positionNum, speed, termination, userFrame, toolFrame, scene);
		copy.setIsCommented( isCommented() );

		return copy;
	}
	
	public int getMotionType() { return motionType; }
	public int getRegisterType() { return registerType; }
	public int getOffset() { return offsetRegNum; }
	public int getPositionNum() { return positionNum; }
	public MotionInstruction getSecondaryPoint() { return circSubInstr; }
	public float getSpeed() { return speed; }
	public int getTermination() { return termination; }
	public int getToolFrame() { return toolFrame; }
	public int getUserFrame() { return userFrame; }
	
	public void setRegisterType(int regType) { registerType = regType; }
	public void setMotionType(int type) { motionType = type; }
	public void setOffsetNum(int ofst) { offsetRegNum = ofst; }
	public void setPositionNum(int pos) { positionNum = pos; }
	public void setSecondaryPoint(MotionInstruction p) { circSubInstr = p; }
	public void setSpeed(float spd) { speed = spd; }
	public void setTermination(int term) { termination = term; }
	public void setToolFrame(int tf) { toolFrame = tf; }
	public void setUserFrame(int uf) { userFrame = uf; }

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
			break;
		}

		// Regster type
		switch(registerType) {
		case Fields.MREGTYPE_POS: 
			fields[1] = "PR["; 
			break;
		case Fields.MREGTYPE_GPOS: 
			fields[1] = "P["; 
			break;
		case Fields.MREGTYPE_OBJ: 
			fields[1] = "OBJ["; 
			break;
		default:
			fields[1] = "\0";
		}

		// Register index
		if(positionNum == -1) {
			fields[2] = "...]";
		} else if(registerType == 2) {
			fields[2] = scene.getWorldObject(positionNum).getName();
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

	public boolean usesGPosReg() { return registerType == Fields.MREGTYPE_GPOS; }
}

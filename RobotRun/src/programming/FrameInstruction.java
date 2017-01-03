package programming;
import robot.RobotRun;

public class FrameInstruction extends Instruction {
	int frameType;
	private int frameIdx;

	public FrameInstruction(int f) {
		super();
		frameType = f;
		setFrameIdx(-1);
	}

	public FrameInstruction(int f, int r) {
		super();
		frameType = f;
		setFrameIdx(r);
	}

	public Instruction clone() {
		Instruction copy = new FrameInstruction(frameType, getFrameIdx());
		copy.setIsCommented( isCommented() );

		return copy;
	}
	public int execute() {    
		if (frameType == RobotRun.getInstance().FTYPE_TOOL) {
			RobotRun.getRobot().setActiveToolFrame(getFrameIdx());
			
		} else if (frameType == RobotRun.getInstance().FTYPE_USER) {
			RobotRun.getRobot().setActiveUserFrame(getFrameIdx());
		}
		
		// Update the current active frames
		RobotRun.getInstance().updateCoordFrame();

		return 0;
	}
	public int getFrameIdx() {
		return frameIdx;
	}
	public int getFrameType(){ return frameType; }

	public int getReg(){ return getFrameIdx(); }

	public void setFrameIdx(int frameIdx) {
		this.frameIdx = frameIdx;
	}

	public void setFrameType(int t){ frameType = t; }

	public void setReg(int r){ setFrameIdx(r); }

	public String[] toStringArray() {
		String[] fields = new String[2];
		// Frame type
		if (frameType == RobotRun.getInstance().FTYPE_TOOL) {
			fields[0] = "TFRAME_NUM =";
		} else if (frameType == RobotRun.getInstance().FTYPE_USER) {
			fields[0] = "UFRAME_NUM =";
		} else {
			fields[0] = "?FRAME_NUM =";
		}
		// Frame index
		fields[1] = Integer.toString(getFrameIdx() + 1);

		return fields;
	}

}

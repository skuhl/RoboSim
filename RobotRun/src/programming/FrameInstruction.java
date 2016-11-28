package programming;
import robot.RobotRun;

public class FrameInstruction extends Instruction {
	/**
	 * 
	 */
	private final RobotRun robotRun;
	int frameType;
	private int frameIdx;

	public FrameInstruction(RobotRun robotRun, int f) {
		super();
		this.robotRun = robotRun;
		frameType = f;
		setFrameIdx(-1);
	}

	public FrameInstruction(RobotRun robotRun, int f, int r) {
		super();
		this.robotRun = robotRun;
		frameType = f;
		setFrameIdx(r);
	}

	public int getFrameType(){ return frameType; }
	public void setFrameType(int t){ frameType = t; }
	public int getReg(){ return getFrameIdx(); }
	public void setReg(int r){ setFrameIdx(r); }

	public int execute() {    
		if (frameType == this.robotRun.FTYPE_TOOL) {
			this.robotRun.setActiveToolFrame(getFrameIdx());
		} else if (frameType == this.robotRun.FTYPE_USER) {
			this.robotRun.setActiveUserFrame(getFrameIdx());
		}
		// Update the current active frames
		this.robotRun.updateCoordFrame();

		return 0;
	}

	public Instruction clone() {
		Instruction copy = new FrameInstruction(this.robotRun, frameType, getFrameIdx());
		copy.setIsCommented( isCommented() );

		return copy;
	}

	public String[] toStringArray() {
		String[] fields = new String[2];
		// Frame type
		if (frameType == this.robotRun.FTYPE_TOOL) {
			fields[0] = "TFRAME_NUM =";
		} else if (frameType == this.robotRun.FTYPE_USER) {
			fields[0] = "UFRAME_NUM =";
		} else {
			fields[0] = "?FRAME_NUM =";
		}
		// Frame index
		fields[1] = Integer.toString(getFrameIdx() + 1);

		return fields;
	}

	public int getFrameIdx() {
		return frameIdx;
	}

	public void setFrameIdx(int frameIdx) {
		this.frameIdx = frameIdx;
	}

} // end FrameInstruction class
package programming;
import frame.RFrame;
import global.Fields;

public class FrameInstruction extends Instruction {
	
	private int frameType;
	private int frameIdx;
	private RFrame frameRef;

	public FrameInstruction(int type) {
		super();
		frameType = type;
		frameIdx = -1;
		frameRef = null;
	}

	public FrameInstruction(int type, int idx, RFrame ref) {
		super();
		frameType = type;
		frameIdx = idx;
		frameRef = ref;
	}

	@Override
	public Instruction clone() {
		Instruction copy = new FrameInstruction(frameType, getFrameIdx(),
				frameRef);
		copy.setIsCommented( isCommented() );

		return copy;
	}
	
	public RFrame getFrame() {
		return frameRef;
	}
	
	public int getFrameIdx() {
		return frameIdx;
	}
	public int getFrameType(){ return frameType; }

	public int getReg(){ return getFrameIdx(); }

	public void setFrame(int frameIdx, RFrame ref) {
		this.frameIdx = frameIdx;
		frameRef = ref;
	}

	public void setFrameType(int t){ frameType = t; }

	@Override
	public String[] toStringArray() {
		String[] fields = new String[2];
		// Frame type
		if (frameType == Fields.FTYPE_TOOL) {
			fields[0] = "TFRAME =";
			
		} else if (frameType == Fields.FTYPE_USER) {
			fields[0] = "UFRAME =";
			
		} else {
			fields[0] = "?FRAME =";
		}
		
		// Frame index (and possibly the name of the frame)
		if (frameRef != null && frameRef.getName().length() > 0) {
			fields[1] = String.format("%s (%d)", frameRef.getName(), frameIdx + 1);
			
		} else {
			fields[1] = Integer.toString(frameIdx + 1);
		}

		return fields;
	}

}

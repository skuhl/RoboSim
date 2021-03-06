package screen.num_entry;

import core.RobotRun;
import frame.RFrame;
import global.Fields;
import programming.FrameInstruction;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenSetFramInstrIdx extends ST_ScreenNumEntry {

	public ScreenSetFramInstrIdx(RobotRun r) {
		super(ScreenMode.SET_FRAME_INSTR_IDX, r);
	}

	@Override
	public void actionEntr() {
		try {
			RoboticArm r = robotRun.getActiveRobot();
			int frameIdx = Integer.parseInt(workingText.toString()) - 1;

			if (frameIdx >= -1 && frameIdx < Fields.FRAME_NUM) {
				FrameInstruction fInst = (FrameInstruction) r.getInstToEdit(robotRun.getActiveProg(), 
						robotRun.getActiveInstIdx());
				RFrame frameRef;
				
				if (fInst.getFrameType() == Fields.FTYPE_USER) {
					frameRef = r.getUserFrame(frameIdx);
					
				} else {
					frameRef = r.getToolFrame(frameIdx);
				}
				
				fInst.setFrame(frameIdx, frameRef);
				robotRun.lastScreen();
				
			} else {
				// Out of bounds
				errorMessage("The frame index must be within the range 0 and 10");
			}
			
		} catch (NumberFormatException NFEx) {
			// Not a number
			errorMessage("The frame index must be an integer");
		}	
	}
	
	@Override
	protected void loadOptions() {
		options.addLine("Select frame index:");
		options.addLine("\0" + workingText);
	}
}

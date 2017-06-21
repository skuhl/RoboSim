package screen.num_entry;

import core.RobotRun;
import enums.ScreenMode;
import global.Fields;
import programming.FrameInstruction;
import robot.RoboticArm;

public class ScreenSetFramInstrIdx extends ST_ScreenNumEntry {

	public ScreenSetFramInstrIdx(RobotRun r) {
		super(ScreenMode.SET_FRAME_INSTR_IDX, r);
	}

	@Override
	protected void loadOptions() {
		options.addLine("Select frame index:");
		options.addLine("\0" + workingText);
	}
	
	@Override
	public void actionEntr() {
		try {
			RoboticArm r = robotRun.getActiveRobot();
			int frameIdx = Integer.parseInt(workingText.toString()) - 1;

			if (frameIdx >= -1 && frameIdx < Fields.FRAME_NUM) {
				FrameInstruction fInst = (FrameInstruction) r.getInstToEdit(robotRun.getActiveProg(), 
						robotRun.getActiveInstIdx());
				fInst.setReg(frameIdx);
			}
		} catch (NumberFormatException NFEx) {
		/* Ignore invalid input */ }

		robotRun.lastScreen();
	}
}

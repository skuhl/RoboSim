package screen;

import core.RobotRun;
import enums.ScreenMode;
import global.Fields;
import programming.PosMotionInst;
import robot.RoboticArm;

public class ScreenSetMotionInstrOffsetType extends ST_ScreenInstructionEdit {

	public ScreenSetMotionInstrOffsetType(RobotRun r) {
		super(ScreenMode.SET_MINST_OFF_TYPE, r);
	}

	@Override
	void loadOptions() {
		options.addLine("None");
		options.addLine("PR[...]");
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		PosMotionInst pMInst = (PosMotionInst)r.getInstToEdit(robotRun.getActiveProg(), 
				robotRun.getActiveInstIdx());
		int ldx = options.getLineIdx();
		// Set the offset type of the active motion instruction
		if (ldx == 0) {
			pMInst.setOffsetType(Fields.OFFSET_NONE);
			
		} else if (ldx == 1) {
			pMInst.setOffsetType(Fields.OFFSET_PREG);
		}
		
		robotRun.lastScreen();
	}

}

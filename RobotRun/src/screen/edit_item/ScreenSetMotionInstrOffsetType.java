package screen.edit_item;

import core.RobotRun;
import global.Fields;
import programming.PosMotionInst;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenSetMotionInstrOffsetType extends ST_ScreenEditItem {

	public ScreenSetMotionInstrOffsetType(RobotRun r) {
		super(ScreenMode.SET_MINST_OFF_TYPE, r);
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

	@Override
	protected void loadOptions() {
		options.addLine("None");
		options.addLine("PR[...]");
	}

}

package screen.instr_edit;

import core.RobotRun;
import enums.ScreenMode;
import global.Fields;
import programming.PosMotionInst;
import robot.RoboticArm;

public class ScreenSetMotionInstrCircRegType extends ST_ScreenInstructionEdit {

	public ScreenSetMotionInstrCircRegType(RobotRun r) {
		super(ScreenMode.SET_MINST_CREG_TYPE, r);
	}

	@Override
	protected void loadOptions() {
		options.addLine("1.LOCAL(P)");
		options.addLine("2.GLOBAL(PR)");
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		PosMotionInst pMInst = (PosMotionInst) r.getInstToEdit(robotRun.getActiveProg(), 
				robotRun.getActiveInstIdx());
		
		if (options.getLineIdx() == 0) {
			pMInst.setCircPosType(Fields.PTYPE_PROG);
		
		} else if (options.getLineIdx() == 1) {
			pMInst.setCircPosType(Fields.PTYPE_PREG);
		}

		robotRun.lastScreen();
	}

}

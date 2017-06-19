package screen;

import core.RobotRun;
import enums.ScreenMode;
import global.Fields;
import programming.MotionInstruction;
import robot.RoboticArm;

public class ScreenSetMotionInstrType extends ST_ScreenInstructionEdit {

	public ScreenSetMotionInstrType(RobotRun r) {
		super(ScreenMode.SET_MINST_TYPE, r);
	}

	@Override
	void loadOptions() {
		options.addLine("1.JOINT");
		options.addLine("2.LINEAR");
		options.addLine("3.CIRCULAR");
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		MotionInstruction m = (MotionInstruction) r.getInstToEdit(robotRun.getActiveProg(), 
				robotRun.getActiveInstIdx());
		
		if (options.getLineIdx() == 0) {
			m.setMotionType(Fields.MTYPE_JOINT);
			
		} else if (options.getLineIdx() == 1) {
			m.setMotionType(Fields.MTYPE_LINEAR);
			
		} else if (options.getLineIdx() == 2) {
			m.setMotionType(Fields.MTYPE_CIRCULAR);
			
		}
		
		robotRun.lastScreen();
	}
}

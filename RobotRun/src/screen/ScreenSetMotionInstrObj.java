package screen;

import core.RobotRun;
import core.Scenario;
import enums.ScreenMode;
import geom.WorldObject;
import programming.CamMoveToObject;
import robot.RoboticArm;

public class ScreenSetMotionInstrObj extends ST_ScreenInstructionEdit {

	public ScreenSetMotionInstrObj(RobotRun r) {
		super(ScreenMode.SET_MINST_WO, r);
	}

	@Override
	void loadOptions() {
		CamMoveToObject castIns = (CamMoveToObject)robotRun.getActiveInstruction();
		Scenario s = castIns.getScene();
		
		if (s != null && s.size() > 0) {
			options.addLine("Enter target object:");
			
			for (WorldObject wo : s) {
				options.addLine(wo.getName());
			}
			
		} else {
			options.addLine("No objects to select");
		}
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		CamMoveToObject cMInst = (CamMoveToObject) r.getInstToEdit(robotRun.getActiveProg(), 
				robotRun.getActiveInstIdx());
		cMInst.setPosIdx(options.getLineIdx() - 1);
		
		robotRun.lastScreen();
	}

}

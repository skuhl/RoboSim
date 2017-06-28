package screen.edit_item;

import core.RobotRun;
import core.Scenario;
import geom.WorldObject;
import programming.CamMoveToObject;
import robot.RoboticArm;
import screen.ScreenMode;
import screen.ScreenState;

public class ScreenSetMotionInstrObj extends ST_ScreenEditItem {

	public ScreenSetMotionInstrObj(RobotRun r) {
		super(ScreenMode.SET_MINST_OBJ, r);
	}

	@Override
	protected void loadOptions() {
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

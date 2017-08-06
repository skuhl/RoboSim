package screen.edit_item;

import java.util.ArrayList;

import camera.RobotCamera;
import core.RobotRun;
import expression.OperandCamObj;
import geom.CameraObject;
import geom.WorldObject;
import screen.ScreenMode;

public class ScreenSetObjectOperandTgt extends ST_ScreenEditItem {

	public ScreenSetObjectOperandTgt(RobotRun r) {
		super(ScreenMode.SET_OBJ_OPERAND_TGT, r);
	}

	@Override
	public void actionEntr() {
		RobotCamera cam = robotRun.getRobotCamera();
		ArrayList<CameraObject> objects = cam.getTaughtObjects();
		((OperandCamObj)robotRun.opEdit).setValue(objects.get(options.getLineIdx() - 1));
		robotRun.lastScreen();
	}

	@Override
	protected void loadOptions() {
		RobotCamera cam = robotRun.getRobotCamera();
		
		if (cam != null && cam.getTaughtObjects().size() > 0) {
			options.addLine("Enter object to match:");
			
			for (WorldObject wo : cam.getTaughtObjects()) {
				options.addLine(wo.getName());
			}
			
		} else {
			options.addLine("No objects to select");
		}
	}

}

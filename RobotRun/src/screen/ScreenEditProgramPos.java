package screen;

import core.RobotRun;
import enums.ScreenMode;
import geom.Point;
import global.DataManagement;
import global.Fields;
import programming.PosMotionInst;
import robot.RoboticArm;

public class ScreenEditProgramPos extends ST_ScreenPointEntry {

	public ScreenEditProgramPos(RobotRun r) {
		super(ScreenMode.EDIT_PROG_POS, r);
	}

	@Override
	String loadHeader() {
		return String.format("EDIT %s POSITION", robotRun.getActiveProg().getName());
	}

	@Override
	void loadLabels() {
		labels[0] = "";
		labels[1] = "";
		labels[2] = "";
		labels[3] = "";
		labels[4] = "";
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		PosMotionInst pMInst = (PosMotionInst) r.getInstToEdit(robotRun.getActiveProg(), 
				robotRun.getActiveInstIdx());
		Point pt = robotRun.parsePosFromContents(pMInst.getMotionType() != Fields.MTYPE_JOINT);

		if (pt != null) {
			// Update the position of the active motion instruction
			robotRun.getActiveProg().setPosition(pMInst.getPosIdx(), pt);
			DataManagement.saveRobotData(r, 1);
		}

		robotRun.lastScreen();
	}
}

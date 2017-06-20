package screen;

import core.RobotRun;
import enums.ScreenMode;
import geom.Point;
import global.DataManagement;
import regs.PositionRegister;
import regs.Register;

public class ScreenEditPosReg extends ST_ScreenPointEntry {

	public ScreenEditPosReg(RobotRun r) {
		super(ScreenMode.EDIT_PREG, r);
	}

	@Override
	String loadHeader() {
		Register reg = robotRun.getActiveRobot().getPReg(robotRun.getLastScreenState().conLnIdx);
		return String.format("%s: POSITION EDIT", reg.getLabel());
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
		PositionRegister pReg = robotRun.getActiveRobot().getPReg(robotRun.getLastScreenState().conLnIdx);
		Point pt = robotRun.parsePosFromContents(pReg.isCartesian);

		if (pt != null) {
			// Position was successfully pulled form the contents menu
			pReg.point = pt;
			DataManagement.saveRobotData(robotRun.getActiveRobot(), 3);
		}

		robotRun.lastScreen();
	}
}

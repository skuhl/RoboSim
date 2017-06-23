package screen.edit_point;

import java.util.ArrayList;

import core.RobotRun;
import enums.ScreenMode;
import geom.Point;
import global.DataManagement;
import regs.PositionRegister;
import regs.Register;
import robot.RoboticArm;
import ui.DisplayLine;

public class ScreenEditPosReg extends ST_ScreenPointEntry {

	public ScreenEditPosReg(RobotRun r) {
		super(ScreenMode.EDIT_PREG, r);
	}

	@Override
	protected String loadHeader() {
		Register reg = robotRun.getActiveRobot().getPReg(robotRun.getLastScreenState().conLnIdx);
		return String.format("%s: POSITION EDIT", reg.getLabel());
	}
	
	@Override
	protected void loadContents() {
		ArrayList<DisplayLine> disp;
		RoboticArm r = robotRun.getActiveRobot();
		PositionRegister pReg = r.getPReg(robotRun.getScreenStates().peek().conLnIdx);
		// Load the position associated with active position register
		if (pReg.point == null) {
			// Initialize an empty position register
			disp = robotRun.loadPosition(r.getDefaultPoint(), pReg.isCartesian);

		} else {
			disp = robotRun.loadPosition(pReg.point, pReg.isCartesian);
		}
		
		contents.setLines(disp);
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

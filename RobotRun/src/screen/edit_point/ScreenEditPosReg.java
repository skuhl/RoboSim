package screen.edit_point;

import core.RobotRun;
import geom.Point;
import global.DataManagement;
import regs.PositionRegister;
import regs.Register;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenEditPosReg extends ST_ScreenPointEntry {

	public ScreenEditPosReg(RobotRun r) {
		super(ScreenMode.EDIT_PREG, r);
		
	}
	
	@Override
	public void actionF3() {
		// Reset input values
		for (int idx = 0; idx < workingText.length; ++idx) {
			workingText[idx] = new StringBuilder("0.000");
		}
		
		updateScreen();
	}

	@Override
	protected String loadHeader() {
		Register reg = robotRun.getActiveRobot().getPReg(robotRun.getScreenStack().peek().getContentIdx());
		return String.format("%s: POSITION EDIT", reg.getLabel());
	}
	
	@Override
	protected void loadLabels() {
		labels[0] = "";
		labels[1] = "";
		labels[2] = "[Reset]";
		labels[3] = "";
		labels[4] = "";
	}
	
	@Override
	protected void loadWorkingText() {
		RoboticArm r = robotRun.getActiveRobot();
		PositionRegister pReg = r.getPReg(robotRun.getScreenStack().peek().getContentIdx());
		
		// Initialize the point if it is null
		if (pReg.point == null) {
			pReg.point = r.getDefaultPoint();
		}
				
		String[][] entries;
		
		if (pReg.isCartesian) {
			// List Cartesian values
			entries = pReg.point.toCartesianStringArray();
		} else {
			// List joint angles
			entries = pReg.point.toJointStringArray();
		}
		
		for(int i = 0; i < entries.length; i += 1) {
			prefixes[i] = entries[i][0];
			workingText[i] = new StringBuilder(entries[i][1]);
		}
	}
	
	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		PositionRegister pReg = r.getPReg(robotRun.getLastScreen().getContentIdx());
		Point pt = parsePosFromContents(pReg.isCartesian);

		if (pt != null) {
			// Position was successfully pulled form the contents menu
			pReg.point = pt;
			DataManagement.saveRobotData(r, 3);
		}

		robotRun.lastScreen();
	}
}

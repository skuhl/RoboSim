package screen.content_disp;

import core.RobotRun;
import enums.CoordFrame;
import frame.UserFrame;
import geom.Point;
import global.DataManagement;
import global.Fields;
import global.RMath;
import regs.PositionRegister;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenNavPosRegs extends ST_ScreenListContents {

	public ScreenNavPosRegs(RobotRun r) {
		super(ScreenMode.NAV_PREGS, r);
	}

	@Override
	protected String loadHeader() {
		return "POSTION REGISTERS";
	}

	@Override
	protected void loadContents() {
		RoboticArm r = robotRun.getActiveRobot();
		contents.setLines(robotRun.loadPositionRegisters(r));
	}
	
	@Override
	protected void loadLabels() {
		// F1 - F5
		if (robotRun.isShift()) {
			labels[0] = "[Clear]";
			labels[1] = "[Copy]";
			labels[2] = "[Switch]";
			labels[3] = "[Move To]";
			labels[4] = "[Record]";
		} else {
			labels[0] = "[Clear]";
			labels[1] = "[Copy]";
			labels[2] = "[Type]";
			labels[3] = "[Move To]";
			labels[4] = "[Record]";
		}
	}
	
	@Override
	protected void loadOptions() {
		PositionRegister pReg = robotRun.getActiveRobot().getPReg(contents.getCurrentItemIdx());
		Point pt = pReg.point;
		// Display the point with the Position register of the highlighted
		// line, when viewing the Position registers
		if (pt != null) {
			String[] pregEntry = pt.toLineStringArray(pReg.isCartesian);

			for (String line : pregEntry) {
				options.addLine(line);
			}
		}
	}

	@Override
	public void actionEntr() {
		if (contents.getColumnIdx() == 0) {
			// Edit register comment
			robotRun.nextScreen(ScreenMode.EDIT_PREG_COM);
		} else if (contents.getColumnIdx() >= 1) {
			// Edit Position Register value
			robotRun.nextScreen(ScreenMode.EDIT_PREG);
		}
	}

	@Override
	public void actionF1() {
		// Clear Position Register entry
		PositionRegister pReg = robotRun.getActiveRobot().getPReg(contents.getCurrentItemIdx());

		if (pReg != null) {
			pReg.comment = null;
			pReg.point = null;
		}
	}
	
	@Override
	public void actionF2() {
		// Position Register copy menus
		if (contents.getColumnIdx() == 0) {
			robotRun.nextScreen(ScreenMode.CP_PREG_COM);
		} else if (contents.getColumnIdx() == 1) {
			robotRun.nextScreen(ScreenMode.CP_PREG_PT);
		}
	}
	
	@Override
	public void actionF3() {
		if (robotRun.isShift()) {
			robotRun.switchScreen(ScreenMode.NAV_DREGS);
		} else {
			// Switch to Data Registers
			int itemIdx = contents.getCurrentItemIdx();
			if (itemIdx >= 0 && itemIdx < Fields.DPREG_NUM) {
				// Set the position type of the selected position register
				PositionRegister toEdit = robotRun.getActiveRobot().getPReg(itemIdx);
				toEdit.isCartesian = !toEdit.isCartesian;
				DataManagement.saveRobotData(robotRun.getActiveRobot(), 3);
			}
		}
	}
	
	@Override
	public void actionF4() {
		if (robotRun.isShift() && !robotRun.isProgExec()) {
			// Stop any prior jogging motion
			robotRun.button_hold();

			// Move To function
			RoboticArm r = robotRun.getActiveRobot();
			PositionRegister pReg = r.getPReg( contents.getCurrentItemIdx() );

			if (pReg.point != null) {
				Point pt = pReg.point.clone();
				// Move the Robot to the select point
				if (pReg.isCartesian) {
					if (r.getCurCoordFrame() == CoordFrame.USER) {
						// Move in terms of the user frame
						UserFrame uFrame = r.getActiveUser();
						pt = RMath.removeFrame(r, pt, uFrame.getOrigin(), uFrame.getOrientation());

						Fields.debug("pt: %s\n", pt.position.toString());
					}

					r.updateMotion(pt);
				} else {
					r.updateMotion(pt.angles);
				}
			} else {
				System.err.println("Position register is uninitialized!");
			}
		}
	}
	
	@Override
	public void actionF5() {
		RoboticArm r = robotRun.getActiveRobot();
		PositionRegister pReg = r.getPReg( contents.getCurrentItemIdx() );

		if (robotRun.isShift() && pReg != null) {
			// Save the Robot's current position and joint angles
			pReg.point = robotRun.getActiveRobot().getToolTipNative();
			pReg.isCartesian = true;
			DataManagement.saveRobotData(r, 3);
		}
	}
}

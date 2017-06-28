package screen.content_disp;

import core.RobotRun;
import regs.DataRegister;
import robot.RoboticArm;
import screen.ScreenMode;
import screen.ScreenState;

public class ScreenNavDataRegs extends ST_ScreenListContents {

	public ScreenNavDataRegs(ScreenState prevState, RobotRun r) {
		super(ScreenMode.NAV_DREGS, prevState, r);
	}

	@Override
	protected String loadHeader() {
		return "REGISTERS";
	}

	@Override
	protected void loadContents() {
		RoboticArm r = robotRun.getActiveRobot();
		contents.setLines(robotRun.loadDataRegisters(r));
	}
	
	@Override
	protected void loadLabels() {
		// F1 - F3
		labels[0] = "[Clear]";
		labels[1] = "[Copy]";
		labels[2] = "[Switch]";
		labels[3] = "";
		labels[4] = "";
	}

	@Override
	public void actionEntr() {
		if (contents.getColumnIdx() == 0) {
			// Edit register comment
			robotRun.nextScreen(ScreenMode.EDIT_DREG_COM);
		} else if (contents.getColumnIdx() >= 1) {
			// Edit Data Register value
			robotRun.nextScreen(ScreenMode.EDIT_DREG_VAL);
		}
	}

	@Override
	public void actionF1() {
		// Clear Data Register entry
		DataRegister dReg = robotRun.getActiveRobot().getDReg(contents.getCurrentItemIdx());

		if (dReg != null) {
			dReg.comment = null;
			dReg.value = null;
		}
	}
	
	@Override
	public void actionF2() {
		// Data Register copy menus
		if (contents.getColumnIdx() == 0) {
			robotRun.nextScreen(ScreenMode.CP_DREG_COM);
		} else if (contents.getColumnIdx() == 1) {
			robotRun.nextScreen(ScreenMode.CP_DREG_VAL);
		}
	}
	
	@Override
	public void actionF3() {
		// Switch to Position Registers
		robotRun.nextScreen(ScreenMode.NAV_PREGS);
	}
}

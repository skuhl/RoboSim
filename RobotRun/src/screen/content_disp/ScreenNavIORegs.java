package screen.content_disp;

import core.RobotRun;
import global.Fields;
import regs.IORegister;
import robot.RoboticArm;
import screen.ScreenMode;
import screen.ScreenState;

public class ScreenNavIORegs extends ST_ScreenListContents {

	public ScreenNavIORegs(RobotRun r) {
		super(ScreenMode.NAV_IOREGS, r);
	}

	@Override
	protected String loadHeader() {
		return "I/O REGISTERS";
	}
	
	@Override
	protected void loadLabels() {
		labels[0] = "[OFF]";
		labels[1] = "[ON]";
		labels[2] = "";
		labels[3] = "";
		labels[4] = "";
	}
	
	@Override
	protected void loadVars(ScreenState s) {
		setScreenIndices(0, 1, 0, 0, 0);
	}
	
	@Override
	protected void loadContents() {
		contents.setLines(loadIORegNav(robotRun.getActiveRobot()));
	}
	
	@Override
	public void actionUp() {
		// Disable page up functionality
		contents.moveUp(false);
	}
	
	@Override
	public void actionDn() {
		// Disable page down functionality
		contents.moveDown(false);
	}
	
	@Override
	public void actionLt() {
		// Disable the ability to change columns
	}
	
	@Override
	public void actionRt() {
		// Disable the ability to change columns
	}
	
	@Override
	public void actionF1() {
		RoboticArm r = robotRun.getActiveRobot();
		IORegister ioReg = r.getIOReg(contents.getCurrentItemIdx());
		// Set the selected I/ register's state to off
		ioReg.setState(Fields.OFF);
		robotRun.updatePendantScreen();
	}
	
	@Override
	public void actionF2() {
		RoboticArm r = robotRun.getActiveRobot();
		IORegister ioReg = r.getIOReg(contents.getCurrentItemIdx());
		// Set the selected I/ register's state to on
		ioReg.setState(Fields.ON);
		robotRun.updatePendantScreen();
	}
}

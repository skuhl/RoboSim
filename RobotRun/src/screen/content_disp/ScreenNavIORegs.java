package screen.content_disp;

import core.RobotRun;
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
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		int ioIdx = contents.getCurrentItemIdx();
		IORegister ioReg = r.getIOReg(ioIdx);
		
		if (ioReg != null) {
			// Toggle the state of the I/O register
			ioReg.setState(!ioReg.getState());
			robotRun.updatePendantScreen();
		}
	}
}

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
	protected void loadContents() {
		contents.setLines(robotRun.loadIORegNav(robotRun.getActiveRobot()));
	}
	
	protected void loadVars(ScreenState s) {
		this.setScreenIndices(0, 1, 0, 0, 0);
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		int ioIdx = contents.getCurrentItemIdx();
		IORegister ioReg = r.getIOReg(ioIdx);
		
		if (ioReg != null) {
			// Toggle the state of the I/O register
			ioReg.state = (ioReg.state == Fields.ON) ? Fields.OFF : Fields.ON;
			robotRun.updatePendantScreen();
		}
	}
	
	@Override
	public void actionLt() {
		// Disable column navigation
	}
	
	@Override
	public void actionRt() {
		// Disable column navigation
	}
}

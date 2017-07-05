package screen.content_disp;

import core.RobotRun;
import global.Fields;
import regs.IORegister;
import robot.RoboticArm;
import screen.ScreenMode;

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
		contents.setLines(loadIORegNav(robotRun.getActiveRobot()));
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
}

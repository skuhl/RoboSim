package screen;

import core.RobotRun;
import enums.ScreenMode;
import global.Fields;
import regs.IORegister;
import robot.RoboticArm;

public class ScreenNavIORegs extends ST_ScreenListContents {

	public ScreenNavIORegs(RobotRun r) {
		super(ScreenMode.NAV_IOREG, r);
	}

	@Override
	String loadHeader() {
		return "I/O REGISTERS";
	}

	@Override
	void loadContents() {
		contents.setLines(robotRun.loadIORegNav(robotRun.getActiveRobot()));
	}

	@Override
	void loadVars() {}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		int ioIdx = contents.getActiveIndex();
		IORegister ioReg = r.getIOReg(ioIdx);
		
		if (ioReg != null) {
			// Toggle the state of the I/O register
			ioReg.state = (ioReg.state == Fields.ON) ? Fields.OFF : Fields.ON;
			robotRun.updatePendantScreen();
		}
	}
}

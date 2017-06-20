package screen.opt_menu;

import core.RobotRun;
import enums.ScreenMode;
import global.DataManagement;
import global.Fields;
import regs.PositionRegister;
import regs.Register;
import robot.RoboticArm;

public class ScreenSwapPointType extends ST_ScreenOptionsMenu {

	public ScreenSwapPointType(RobotRun r) {
		super(ScreenMode.SWAP_PT_TYPE, r);
	}

	@Override
	protected String loadHeader() {
		Register reg = robotRun.getActiveRobot().getPReg(contents.getItemIdx());
		return String.format("%s: TYPE EDIT", reg.getLabel());
	}
	
	@Override
	protected void loadContents() {
		RoboticArm r = robotRun.getActiveRobot();
		contents.setLines(robotRun.loadPositionRegisters(r));
	}

	@Override
	protected void loadOptions() {
		options.addLine("1. Cartesian");
		options.addLine("2. Joint");
	}

	@Override
	public void actionEntr() {
		int itemIdx = contents.getItemIdx();
		if (itemIdx >= 0 && itemIdx < Fields.DPREG_NUM) {
			// Set the position type of the selected position register
			PositionRegister toEdit = robotRun.getActiveRobot().getPReg(itemIdx);
			toEdit.isCartesian = options.getLineIdx() == 0;
			DataManagement.saveRobotData(robotRun.getActiveRobot(), 3);
			robotRun.lastScreen();
		}
	}
}

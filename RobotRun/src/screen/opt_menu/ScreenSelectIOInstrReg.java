package screen.opt_menu;

import core.RobotRun;
import enums.ScreenMode;
import global.Fields;

public class ScreenSelectIOInstrReg extends ST_ScreenOptionsMenu {

	public ScreenSelectIOInstrReg(RobotRun r) {
		super(ScreenMode.SELECT_IO_INSTR_REG, r);
	}

	@Override
	protected String loadHeader() {
		return "SELECT IO REGISTER";
	}

	@Override
	protected void loadOptions() {
		options.setLines(robotRun.loadIORegInst(robotRun.getActiveRobot()));
	}
	
	@Override
	public void actionLt() {
		options.moveLeft();
	}
	
	@Override
	public void actionRt() {
		options.moveRight();
	}

	@Override
	public void actionEntr() {
		// IO registers are 1 indexed!
		int state = (options.getColumnIdx() == 1) ? Fields.ON : Fields.OFF;
		robotRun.newIOInstruction(options.getLineIdx() + 1, state);
		robotRun.screenStack.pop();
		robotRun.lastScreen();
	}

}

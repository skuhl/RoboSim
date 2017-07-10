package screen.opt_menu;

import core.RobotRun;
import global.Fields;
import screen.ScreenMode;
import screen.ScreenState;

public class ScreenSelectIOInstrReg extends ST_ScreenOptionsMenu {

	public ScreenSelectIOInstrReg(RobotRun r) {
		super(ScreenMode.SELECT_IO_INSTR_REG, r);
		// Restrict column index to the second column
		options.setSelectedColumnIdx(1);
	}

	@Override
	protected String loadHeader() {
		return "SELECT IO REGISTER";
	}

	@Override
	protected void loadOptions() {
		options.setLines(loadIORegInst(robotRun.getActiveRobot()));
	}
	
	@Override
	protected void loadVars(ScreenState s) {
		setScreenIndices(0, 1, 0, 0, 0);
	}
	
	@Override
	protected void loadVars(ScreenState s) {
		setScreenIndices(0, 1, 0, 0, 0);
	}
	
	@Override
	public void actionLt() {
		/* Restrict the column index in the I/O instruction creation (first
		 * column is the I/O register label) */
		int colIdx = options.getColumnIdx();
		
		if (colIdx > 1) {
			options.moveLeft();
		}
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
		robotRun.lastScreen();
		robotRun.lastScreen();
	}

}

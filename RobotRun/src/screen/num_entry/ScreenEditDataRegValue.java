package screen.num_entry;

import core.RobotRun;
import global.DataManagement;
import regs.DataRegister;
import regs.Register;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenEditDataRegValue extends ST_ScreenNumEntry {

	public ScreenEditDataRegValue(RobotRun r) {
		super(ScreenMode.EDIT_DREG_VAL, r);
	}

	@Override
	protected String loadHeader() {
		Register reg = robotRun.getActiveRobot().getDReg(robotRun.getScreenStack().peek().getContentIdx());
		return String.format("%s: VALUE EDIT", reg.getLabel());
	}
	
	@Override
	protected void loadContents() {
		RoboticArm r = robotRun.getActiveRobot();
		contents.setLines(robotRun.loadDataRegisters(r));
	}
	
	@Override
	protected void loadOptions() {
		options.addLine(String.format("Input R[%d]'s value:", robotRun.getScreenStack().peek().getContentIdx() + 1));
		options.addLine("\0" + workingText);
	}
	
	@Override
	public void actionEntr() {
		Float f = null;
		int itemIdx = contents.getCurrentItemIdx();
		
		try {
			// Read inputed Float value
			f = Float.parseFloat(workingText.toString());
			// Clamp the value between -9999 and 9999, inclusive
			f = Math.max(-9999f, Math.min(f, 9999f));
			DataRegister dReg = robotRun.getActiveRobot().getDReg(itemIdx);

			if (dReg != null) {
				// Save inputed value
				dReg.value = f;
				DataManagement.saveRobotData(robotRun.getActiveRobot(), 3);
			}
		} catch (NumberFormatException NFEx) {
			// Invalid input value
			System.err.println("Value must be a real number!");
		}

		robotRun.lastScreen();
	}
}

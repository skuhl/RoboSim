package screen.num_entry;

import core.RobotRun;
import io.DataManagement;
import regs.DataRegister;
import regs.Register;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenEditDataRegValue extends ST_ScreenNumEntry {

	public ScreenEditDataRegValue(RobotRun r) {
		super(ScreenMode.EDIT_DREG_VAL, r);
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
				DataManagement.saveRobotData(robotRun.getActiveRobot(), 4);
				robotRun.lastScreen();
				
			} else {
				System.err.printf("Index %d is not a value register index!\n", itemIdx);
			}
			
		} catch (NumberFormatException NFEx) {
			// Invalid input value
			errorMessage("Value must be a real number");
		}
	}
	
	@Override
	protected void loadContents() {
		RoboticArm r = robotRun.getActiveRobot();
		contents.setLines(loadDataRegisters(r));
	}
	
	@Override
	protected String loadHeader() {
		Register reg = robotRun.getActiveRobot().getDReg(robotRun.getLastScreen().getContentIdx());
		return String.format("%s: VALUE EDIT", reg.getLabel());
	}
	
	@Override
	protected void loadOptions() {
		options.addLine(String.format("Input R[%d]'s value:", robotRun.getLastScreen().getContentIdx() + 1));
		options.addLine("\0" + workingText);
	}
}

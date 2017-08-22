package screen.num_entry;

import core.RobotRun;
import global.DataManagement;
import regs.Register;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenCopyDataRegValue extends ST_ScreenNumEntry {

	public ScreenCopyDataRegValue(RobotRun r) {
		super(ScreenMode.CP_DREG_VAL, r);
	}

	@Override
	public void actionEntr() {
		int regIdx = -1;
		int itemIdx = contents.getCurrentItemIdx();

		try {
			// Copy the value of the curent Data register to the Data
			// register at the specified index
			regIdx = Integer.parseInt(workingText.toString()) - 1;
			robotRun.getActiveRobot().getDReg(regIdx).value = robotRun.getActiveRobot().getDReg(itemIdx).value;
			DataManagement.saveRobotData(robotRun.getActiveRobot(), 4);
			robotRun.lastScreen();

		} catch (NumberFormatException MFEx) {
			errorMessage("Only real numbers are valid!");
			
		} catch (IndexOutOfBoundsException IOOBEx) {
			errorMessage("Only positve integers between 1 and 100 are valid!");
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
		return String.format("%s: VALUE COPY", reg.getLabel());
	}

	@Override
	protected void loadOptions() {
		options.addLine(String.format("Move R[%d]'s value to:", robotRun.getLastScreen().getContentIdx() + 1));
		options.addLine(String.format("R[%s]", workingText));
	}

}

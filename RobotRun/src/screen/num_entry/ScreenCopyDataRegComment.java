package screen.num_entry;

import core.RobotRun;
import global.DataManagement;
import regs.Register;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenCopyDataRegComment extends ST_ScreenNumEntry {

	public ScreenCopyDataRegComment(RobotRun r) {
		super(ScreenMode.CP_DREG_COM, r);
	}
	
	@Override
	protected String loadHeader() {
		Register reg = robotRun.getActiveRobot().getDReg(contents.getCurrentItemIdx());
		return String.format("%s: COMMENT COPY", reg.getLabel());
	}
	
	@Override
	protected void loadContents() {
		RoboticArm r = robotRun.getActiveRobot();
		contents.setLines(robotRun.loadDataRegisters(r));
	}

	@Override
	protected void loadOptions() {
		options.addLine(String.format("Move R[%d]'s comment to:", contents.getCurrentItemIdx() + 1));
		options.addLine(String.format("R[%s]", workingText));
	}

	@Override
	public void actionEntr() {
		int regIdx = -1;
		int itemIdx = contents.getCurrentItemIdx();

		try {
			// Copy the comment of the curent Data register to the Data
			// register at the specified index
			regIdx = Integer.parseInt(workingText.toString()) - 1;
			robotRun.getActiveRobot().getDReg(regIdx).comment = robotRun.getActiveRobot().getDReg(itemIdx).comment;
			DataManagement.saveRobotData(robotRun.getActiveRobot(), 3);

		} catch (NumberFormatException MFEx) {
			System.err.println("Only real numbers are valid!");
		} catch (IndexOutOfBoundsException IOOBEx) {
			System.err.println("Only positve integers between 1 and 100 are valid!");
		}

		robotRun.lastScreen();
	}

}

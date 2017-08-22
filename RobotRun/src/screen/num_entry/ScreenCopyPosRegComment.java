package screen.num_entry;

import core.RobotRun;
import global.DataManagement;
import regs.Register;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenCopyPosRegComment extends ST_ScreenNumEntry {

	public ScreenCopyPosRegComment(RobotRun r) {
		super(ScreenMode.CP_PREG_COM, r);
	}

	@Override
	public void actionEntr() {
		int regIdx = -1;
		int itemIdx = contents.getCurrentItemIdx();
		
		try {
			// Copy the comment of the curent Position register to the
			// Position register at the specified index
			regIdx = Integer.parseInt(workingText.toString()) - 1;
			robotRun.getActiveRobot().getPReg(regIdx).comment =
					robotRun.getActiveRobot().getPReg(itemIdx).comment;
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
		contents.setLines(loadPositionRegisters(r));
	}
	
	@Override
	protected String loadHeader() {
		Register reg = robotRun.getActiveRobot().getDReg(robotRun.getLastScreen().getContentIdx());
		return String.format("%s: COMMENT COPY", reg.getLabel());
	}

	@Override
	protected void loadOptions() {
		options.addLine(String.format("Move PR[%d]'s comment to:", robotRun.getLastScreen().getContentIdx() + 1));
		options.addLine(String.format("PR[%s]", workingText));
	}

}

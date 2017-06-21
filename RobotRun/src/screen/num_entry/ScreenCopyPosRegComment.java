package screen.num_entry;

import core.RobotRun;
import enums.ScreenMode;
import global.DataManagement;
import regs.Register;
import robot.RoboticArm;

public class ScreenCopyPosRegComment extends ST_ScreenNumEntry {

	public ScreenCopyPosRegComment(RobotRun r) {
		super(ScreenMode.CP_PREG_COM, r);
	}

	@Override
	protected String loadHeader() {
		Register reg = robotRun.getActiveRobot().getDReg(contents.getItemIdx());
		return String.format("%s: COMMENT COPY", reg.getLabel());
	}
	
	@Override
	protected void loadContents() {
		RoboticArm r = robotRun.getActiveRobot();
		contents.setLines(robotRun.loadPositionRegisters(r));
	}
	
	@Override
	protected void loadOptions() {
		options.addLine(String.format("Move PR[%d]'s comment to:", contents.getItemIdx() + 1));
		options.addLine(String.format("PR[%s]", workingText));
	}

	@Override
	public void actionEntr() {
		int regIdx = -1;
		int itemIdx = contents.getItemIdx();
		
		try {
			// Copy the comment of the curent Position register to the
			// Position register at the specified index
			regIdx = Integer.parseInt(workingText.toString()) - 1;
			robotRun.getActiveRobot().getPReg(regIdx).comment = robotRun.getActiveRobot().getPReg(itemIdx).comment;
			DataManagement.saveRobotData(robotRun.getActiveRobot(), 3);

		} catch (NumberFormatException MFEx) {
			System.err.println("Only real numbers are valid!");
		} catch (IndexOutOfBoundsException IOOBEx) {
			System.err.println("Only positve integers between 1 and 100 are valid!");
		}

		robotRun.lastScreen();
	}

}

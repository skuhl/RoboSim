package screen.num_entry;

import core.RobotRun;
import io.DataManagement;
import regs.Register;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenCopyDataRegComment extends ST_ScreenNumEntry {

	public ScreenCopyDataRegComment(RobotRun r) {
		super(ScreenMode.CP_DREG_COM, r);
	}
	
	@Override
	public void actionEntr() {
		int regIdx = -1;
		int itemIdx = contents.getCurrentItemIdx();

		try {
			// Copy the comment of the curent Data register to the Data
			// register at the specified index
			regIdx = Integer.parseInt(workingText.toString()) - 1;
			robotRun.getActiveRobot().getDReg(regIdx).comment =
					robotRun.getActiveRobot().getDReg(itemIdx).comment;
			DataManagement.saveRobotData(robotRun.getActiveRobot(), 4);
			robotRun.lastScreen();

		} catch (NumberFormatException MFEx) {
			errorMessage("The index must be an integer");
			
		} catch (IndexOutOfBoundsException IOOBEx) {
			errorMessage("The index must be within the range 1 and 100");
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
		return String.format("%s: COMMENT COPY", reg.getLabel());
	}

	@Override
	protected void loadOptions() {
		options.addLine(String.format("Move R[%d]'s comment to:",
				robotRun.getLastScreen().getContentIdx() + 1));
		options.addLine(String.format("R[%s]", workingText));
	}

}

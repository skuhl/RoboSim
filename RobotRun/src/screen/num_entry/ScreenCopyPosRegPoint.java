package screen.num_entry;

import core.RobotRun;
import global.DataManagement;
import regs.PositionRegister;
import regs.Register;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenCopyPosRegPoint extends ST_ScreenNumEntry {

	public ScreenCopyPosRegPoint(RobotRun r) {
		super(ScreenMode.CP_PREG_PT, r);
	}

	@Override
	public void actionEntr() {
		int regIdx = -1;

		try {
			// Copy the point of the curent Position register to the
			// Position register at the specified index
			regIdx = Integer.parseInt(workingText.toString()) - 1;
			PositionRegister src = robotRun.getActiveRobot().getPReg(contents.getCurrentItemIdx());
			PositionRegister dest = robotRun.getActiveRobot().getPReg(regIdx);
			
			if (src.point != null) {
				dest.point = src.point.clone();
				
			} else {
				dest.point = null;
			}
			
			dest.isCartesian = src.isCartesian;
			DataManagement.saveRobotData(robotRun.getActiveRobot(), 3);
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
		Register reg = robotRun.getActiveRobot().getPReg(robotRun.getLastScreen().getContentIdx());
		return String.format("%s: POSITION COPY", reg.getLabel());
	}

	@Override
	protected void loadOptions() {
		options.addLine(String.format("Move PR[%d]'s point to:", robotRun.getLastScreen().getContentIdx() + 1));
		options.addLine(String.format("PR[%s]", workingText));
	}

}

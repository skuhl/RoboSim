package screen.content_disp;

import core.RobotRun;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenNavPrograms extends ST_ScreenListContents {

	public ScreenNavPrograms(RobotRun r) {
		super(ScreenMode.NAV_PROGRAMS, r);
	}
	
	@Override
	protected String loadHeader() {
		return "PROGRAMS";
	}
	
	@Override
	protected void loadContents() {
		contents.setLines(robotRun.getActiveRobot().printProgList());
	}
	
	@Override
	protected void loadLabels() {
		// F2, F3
		labels[0] = "[Create]";
		if (robotRun.getActiveRobot().numOfPrograms() > 0) {
			labels[1] = "[Rename]";
			labels[2] = "[Delete]";
			labels[3] = "[Copy]";
			labels[4] = "";
		} else {
			labels[1] = "";
			labels[2] = "";
			labels[3] = "";
			labels[4] = "";
		}
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		if (r.numOfPrograms() != 0) {
			robotRun.setActiveProgIdx(contents.getCurrentItemIdx());
			robotRun.nextScreen(ScreenMode.NAV_PROG_INSTR);
		}
	}
	
	public void actionEdit() {
		if (mode == ScreenMode.NAV_PROGRAMS) {
			// Load the selected program
			robotRun.setActiveProgIdx( contents.getCurrentItemIdx() );
			robotRun.setActiveInstIdx(0);
			robotRun.nextScreen(ScreenMode.NAV_PROG_INSTR);
			
		} else if (robotRun.getActiveProg() != null) {
			// Load the current active program
			robotRun.nextScreen(ScreenMode.NAV_PROG_INSTR);
			
		} else {
			// Load the program navigation menu
			robotRun.resetStack();
			robotRun.nextScreen(ScreenMode.NAV_PROGRAMS);
		}
	}

	@Override
	public void actionF1() {
		robotRun.nextScreen(ScreenMode.PROG_CREATE);
	}

	@Override
	public void actionF2() {
		if (robotRun.getActiveRobot().numOfPrograms() > 0) {
			robotRun.setActiveProgIdx(contents.getCurrentItemIdx());
			robotRun.nextScreen(ScreenMode.PROG_RENAME);
		}
	}

	@Override
	public void actionF3() {
		if (robotRun.getActiveRobot().numOfPrograms() > 0) {
			robotRun.setActiveProgIdx(contents.getCurrentItemIdx());
			robotRun.nextScreen(ScreenMode.CONFIRM_PROG_DELETE);
		}
	}

	@Override
	public void actionF4() {
		if (robotRun.getActiveRobot().numOfPrograms() > 0) {
			robotRun.setActiveProgIdx(contents.getCurrentItemIdx());
			robotRun.nextScreen(ScreenMode.PROG_COPY);
		}
	}
}

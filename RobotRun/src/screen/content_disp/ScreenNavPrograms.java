package screen.content_disp;

import core.RobotRun;
import global.Fields;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenNavPrograms extends ST_ScreenListContents {

	public ScreenNavPrograms(RobotRun r) {
		super(ScreenMode.NAV_PROGRAMS, r);
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
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		if (r.numOfPrograms() != 0) {
			robotRun.setActiveProgIdx(contents.getCurrentItemIdx());
			robotRun.nextScreen(ScreenMode.NAV_PROG_INSTR);
		}
	}
	
	@Override
	public void actionF1() {
		RoboticArm r = robotRun.getActiveRobot();
		
		if (r.numOfPrograms() < RoboticArm.PROG_NUM) {
			robotRun.nextScreen(ScreenMode.PROG_CREATE);
			
		} else {
			Fields.setMessage("This robot has already reached its capacityy for programs");
		}
	}

	@Override
	public void actionF2() {
		if (robotRun.getActiveRobot().numOfPrograms() > 0) {
			robotRun.setActiveProgIdx(contents.getCurrentItemIdx());
			robotRun.nextScreen(ScreenMode.PROG_RENAME);
			
		} else {
			Fields.setMessage("This robot has already reached its capacityy for programs");
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
		RoboticArm r = robotRun.getActiveRobot();
		
		if (r.numOfPrograms() > 0 && r.numOfPrograms() < RoboticArm.PROG_NUM) {
			robotRun.setActiveProgIdx(contents.getCurrentItemIdx());
			robotRun.nextScreen(ScreenMode.PROG_COPY);
		}
	}

	@Override
	protected void loadContents() {
		contents.setLines(robotRun.getActiveRobot().printProgList());
	}

	@Override
	protected String loadHeader() {
		return "PROGRAMS";
	}

	@Override
	protected void loadLabels() {
		labels[0] = "[Create]";
		if (robotRun.getActiveRobot().numOfPrograms() > 0) {
			labels[1] = "[Rename]";
			labels[2] = "[Delete]";
			labels[3] = "[Copy]";
		} else {
			labels[1] = "";
			labels[2] = "";
			labels[3] = "";
		}
		labels[4] = "";
	}
}

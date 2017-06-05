package screen;

import core.RobotRun;
import enums.ScreenMode;

public class ScreenProgs extends Screen {

	public ScreenProgs(RobotRun r) {
		super(ScreenMode.NAV_PROGRAMS, r);
	}
	
	@Override
	void loadHeader() {
		header = "PROGRAMS";
	}
	
	@Override
	void loadContents() {
		contents.setLines(RobotRun.getInstanceRobot().printProgList());
	}
	
	@Override
	void loadOptions() {}
	
	@Override
	void loadLabels() {
		if(RobotRun.getInstanceRobot().numOfPrograms() > 0) {
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
	public void loadVars() {
		robotRun.hold();
		contents.setLineIdx(RobotRun.getInstanceRobot().getActiveProgIdx());
		contents.setColumnIdx(0);
		RobotRun.getInstanceRobot().setActiveInstIdx(0);
	}

	@Override
	public void loadPrev() {
		
	}

	@Override
	public void actionUp() {
		RobotRun.getInstanceRobot().setActiveProgIdx(contents.moveUp(robotRun.isShift()));
	}

	@Override
	public void actionDn() {
		RobotRun.getInstanceRobot().setActiveProgIdx(contents.moveDown(robotRun.isShift()));
	}

	@Override
	public void actionLt() {}

	@Override
	public void actionRt() {}

	@Override
	public void actionEntr() {
		if(RobotRun.getInstanceRobot().numOfPrograms() != 0) {
			RobotRun.getInstanceRobot().setActiveInstIdx(0);
			contents.reset();
			// TODO robotRun.nextScreen(ScreenMode.NAV_PROG_INSTR);
		}
	}

	@Override
	public void actionF1() {
		// TODO robotRun.nextScreen(ScreenMode.PROG_CREATE);
	}

	@Override
	public void actionF2() {
		if(RobotRun.getInstanceRobot().numOfPrograms() > 0) {
			// TODO robotRun.nextScreen(ScreenMode.PROG_RENAME);
		}
	}

	@Override
	public void actionF3() {
		if(RobotRun.getInstanceRobot().numOfPrograms() > 0) {
			// TODO robotRun.nextScreen(ScreenMode.CONFIRM_PROG_DELETE);
		}
	}

	@Override
	public void actionF4() {
		if(RobotRun.getInstanceRobot().numOfPrograms() > 0) {
			// TODO robotRun.nextScreen(ScreenMode.PROG_COPY);
		}
	}

	@Override
	public void actionF5() {}
}

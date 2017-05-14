package screen;

import global.Fields;
import robot.RobotRun;

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
		contents.setLines(RobotRun.getActiveRobot().printProgList());
	}
	
	@Override
	void loadOptions() {}
	
	@Override
	void loadLabels() {
		if(RobotRun.getActiveRobot().numOfPrograms() > 0) {
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
		contents.setLineIdx(RobotRun.getActiveRobot().getActiveProgIdx());
		contents.setColumnIdx(0);
		RobotRun.getActiveRobot().setActiveInstIdx(0);
	}

	@Override
	public void loadPrev() {
		
	}

	@Override
	public void actionUp() {
		RobotRun.getActiveRobot().setActiveProgIdx(contents.moveUp(robotRun.isShift()));

		if(Fields.DEBUG) {
			System.out.printf("\nOpt: %d\nProg: %d\nTRS: %d\n\n",
					options.getLineIdx(), RobotRun.getActiveRobot().getActiveProgIdx(), contents.getRenderStart());
		}
	}

	@Override
	public void actionDn() {
		RobotRun.getActiveRobot().setActiveProgIdx(contents.moveDown(robotRun.isShift()));

		if(Fields.DEBUG) {
			System.out.printf("\nRow: %d\nProg: %d\nTRS: %d\n\n",  
				contents.getLineIdx(), RobotRun.getActiveRobot().getActiveProgIdx(), contents.getRenderStart());
		}
	}

	@Override
	public void actionLt() {}

	@Override
	public void actionRt() {}

	@Override
	public void actionEntr() {
		if(RobotRun.getActiveRobot().numOfPrograms() != 0) {
			RobotRun.getActiveRobot().setActiveInstIdx(0);
			contents.reset();
			robotRun.nextScreen(ScreenMode.NAV_PROG_INSTR);
		}
	}

	@Override
	public void actionF1() {
		robotRun.nextScreen(ScreenMode.PROG_CREATE);
	}

	@Override
	public void actionF2() {
		if(RobotRun.getActiveRobot().numOfPrograms() > 0) {
			robotRun.nextScreen(ScreenMode.PROG_RENAME);
		}
	}

	@Override
	public void actionF3() {
		if(RobotRun.getActiveRobot().numOfPrograms() > 0) {
			robotRun.nextScreen(ScreenMode.CONFIRM_PROG_DELETE);
		}
	}

	@Override
	public void actionF4() {
		if(RobotRun.getActiveRobot().numOfPrograms() > 0) {
			robotRun.nextScreen(ScreenMode.PROG_COPY);
		}
	}

	@Override
	public void actionF5() {}
}

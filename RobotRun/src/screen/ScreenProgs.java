package screen;

import global.Fields;
import robot.RobotRun;

public class ScreenProgs extends Screen {

	public ScreenProgs(RobotRun r) {
		super(ScreenMode.NAV_PROGRAMS, r);
	}
	
	void loadHeader() {
		header = "PROGRAMS";
	}
	
	void loadContents() {
		contents.setLines(robotRun.getActiveRobot().printProgList());
	}
	
	void loadOptions() {}
	
	void loadLabels() {
		if(robotRun.getActiveRobot().numOfPrograms() > 0) {
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
		robotRun.getActiveRobot().halt();
		contents.setLineIdx(robotRun.getActiveRobot().getActiveProgIdx());
		contents.setColumnIdx(0);
		robotRun.getActiveRobot().setActiveInstIdx(0);
	}

	@Override
	public void loadPrev() {
		
	}

	@Override
	public void actionUp() {
		robotRun.getActiveRobot().setActiveProgIdx(contents.moveUp(robotRun.isShift()));

		if(Fields.DEBUG) {
			System.out.printf("\nOpt: %d\nProg: %d\nTRS: %d\n\n",
					options.getLineIdx(), robotRun.getActiveRobot().getActiveProgIdx(), contents.getRenderStart());
		}
	}

	@Override
	public void actionDn() {
		robotRun.getActiveRobot().setActiveProgIdx(contents.moveDown(robotRun.isShift()));

		if(Fields.DEBUG) {
			System.out.printf("\nRow: %d\nProg: %d\nTRS: %d\n\n",  
				contents.getLineIdx(), robotRun.getActiveRobot().getActiveProgIdx(), contents.getRenderStart());
		}
	}

	@Override
	public void actionLt() {}

	@Override
	public void actionRt() {}

	@Override
	public void actionEntr() {
		if(robotRun.getActiveRobot().numOfPrograms() != 0) {
			robotRun.getActiveRobot().setActiveInstIdx(0);
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
		if(robotRun.getActiveRobot().numOfPrograms() > 0) {
			robotRun.nextScreen(ScreenMode.PROG_RENAME);
		}
	}

	@Override
	public void actionF3() {
		if(robotRun.getActiveRobot().numOfPrograms() > 0) {
			robotRun.nextScreen(ScreenMode.CONFIRM_PROG_DELETE);
		}
	}

	@Override
	public void actionF4() {
		if(robotRun.getActiveRobot().numOfPrograms() > 0) {
			robotRun.nextScreen(ScreenMode.PROG_COPY);
		}
	}

	@Override
	public void actionF5() {}
}

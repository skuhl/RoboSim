package screen;

import core.RobotRun;
import enums.ScreenMode;
import ui.MenuScroll;

public class ScreenProgs extends ST_ScreenListContents {

	public ScreenProgs(RobotRun r) {
		super(ScreenMode.NAV_PROGRAMS, r);
	}
	
	@Override
	String loadHeader() {
		return "PROGRAMS";
	}
	
	@Override
	MenuScroll loadContents() {
		contents.setLines(robotRun.getActiveRobot().printProgList());
		return contents;
	}
	
	@Override
	MenuScroll loadOptions() {
		return options;
	}
	
	@Override
	String[] loadLabels() {
		String[] lbls = new String[5];
		
		// F2, F3
		lbls[0] = "[Create]";
		if (robotRun.getActiveRobot().numOfPrograms() > 0) {
			lbls[1] = "[Rename]";
			lbls[2] = "[Delete]";
			lbls[3] = "[Copy]";
			lbls[4] = "";
		} else {
			lbls[1] = "";
			lbls[2] = "";
			lbls[3] = "";
			lbls[4] = "";
		}
		
		return lbls;
	}
	
	@Override
	public void loadVars() {
		if (robotRun.getActiveProg() == null) {
			robotRun.setActiveProgIdx(0);
			robotRun.setActiveInstIdx(0);
		}
		
		contents.setLineIdx( robotRun.getActiveProgIdx() );
	}

	@Override
	public void loadPrev() {
		
	}

	@Override
	public void actionEntr() {
		if(RobotRun.getInstanceRobot().numOfPrograms() != 0) {
			RobotRun.getInstance().setActiveInstIdx(0);
			contents.reset();
			// TODO robotRun.nextScreen(ScreenMode.NAV_PROG_INSTR);
		}
	}
	
	public void actionEdit() {
		if (mode == ScreenMode.NAV_PROGRAMS) {
			// Load the selected program
			robotRun.setActiveProgIdx( contents.getActiveIndex() );
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
			robotRun.nextScreen(ScreenMode.PROG_RENAME);
		}
	}

	@Override
	public void actionF3() {
		if (robotRun.getActiveRobot().numOfPrograms() > 0) {
			robotRun.setActiveProgIdx( contents.getActiveIndex() );
			robotRun.nextScreen(ScreenMode.CONFIRM_PROG_DELETE);
		}
	}

	@Override
	public void actionF4() {
		if (robotRun.getActiveRobot().numOfPrograms() > 0) {
			robotRun.nextScreen(ScreenMode.PROG_COPY);
		}
	}

	@Override
	public void actionF5() {}
}

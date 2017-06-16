package screen;

import core.RobotRun;
import enums.ScreenMode;
import ui.MenuScroll;

public class ScreenMainMenu extends ST_ScreenListContents {

	public ScreenMainMenu(RobotRun r) {
		super(ScreenMode.NAV_MAIN_MENU, r);
	}

	@Override
	String loadHeader() {
		return "MAIN MENU";
	}

	@Override
	MenuScroll loadContents() {
		contents.addLine("1 Frames");
		contents.addLine("2 Macros");
		contents.addLine("3 Manual Fncts");
		contents.addLine("4 I/O Registers");
		
		return contents;
	}
	
	@Override
	MenuScroll loadOptions() {
		options.clear();
		return options;
	}

	@Override
	String[] loadLabels() {
		// TODO Auto-generated method stub
		return new String[] { "", "", "", "", "" };
	}
	
	@Override
	public void loadVars() {
		contents.setColumnIdx(0);
	}

	@Override
	public void loadPrev() {}

	@Override
	public void actionUp() {
		System.out.println("up");
		contents.moveUp(false);
	}

	@Override
	public void actionDn() {
		System.out.println("dn");
		contents.moveDown(false);
	}

	@Override
	public void actionLt() {}

	@Override
	public void actionRt() {}

	@Override
	public void actionEntr() {
		if(options.getLineIdx() == 0) { // Frames
			// TODO robotRun.nextScreen(ScreenMode.SELECT_FRAME_MODE);
		} else if(options.getLineIdx() == 1) { // Macros
			// TODO robotRun.nextScreen(ScreenMode.NAV_MACROS);
		} else { // Manual Functions
			// TODO robotRun.nextScreen(ScreenMode.NAV_MF_MACROS);
		}
	}

	@Override
	public void actionF1() {}

	@Override
	public void actionF2() {}

	@Override
	public void actionF3() {}

	@Override
	public void actionF4() {}

	@Override
	public void actionF5() {}
	
}

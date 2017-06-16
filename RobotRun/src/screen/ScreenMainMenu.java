package screen;

import core.RobotRun;
import enums.ScreenMode;

public class ScreenMainMenu extends ST_ScreenListContents {

	public ScreenMainMenu(RobotRun r) {
		super(ScreenMode.NAV_MAIN_MENU, r);
	}

	@Override
	String loadHeader() {
		return "MAIN MENU";
	}

	@Override
	void loadContents() {
		contents.addLine("1 Frames");
		contents.addLine("2 Macros");
		contents.addLine("3 Manual Fncts");
		contents.addLine("4 I/O Registers");
	}
	
	@Override
	void loadOptions() {
		options.clear();
	}

	@Override
	void loadLabels() {
		labels[0] = "";
		labels[1] = "";
		labels[2] = "";
		labels[3] = "";
		labels[4] = "";
	}
	

	@Override
	void loadVars() {}
	
	@Override
	public void actionUp() {
		contents.moveUp(false);
	}

	@Override
	public void actionDn() {
		contents.moveDown(false);
	}

	@Override
	public void actionLt() {}

	@Override
	public void actionRt() {}

	@Override
	public void actionEntr() {
		if (contents.getLineIdx() == 0) { // Frames
			robotRun.nextScreen(ScreenMode.SELECT_FRAME_MODE);
		} else if (contents.getLineIdx() == 1) { // Macros
			robotRun.nextScreen(ScreenMode.NAV_MACROS);
		} else if (contents.getLineIdx() == 2) { // Manual Functions
			robotRun.nextScreen(ScreenMode.NAV_MF_MACROS);
		} else if (contents.getLineIdx() == 3) { // IO Registers
			robotRun.nextScreen(ScreenMode.NAV_IOREG);
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

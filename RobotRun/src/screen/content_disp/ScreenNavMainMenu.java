package screen.content_disp;

import core.RobotRun;
import screen.ScreenMode;
import screen.ScreenState;

public class ScreenNavMainMenu extends ST_ScreenListContents {

	public ScreenNavMainMenu(RobotRun r) {
		super(ScreenMode.NAV_MAIN_MENU, r);
	}

	@Override
	protected String loadHeader() {
		return "MAIN MENU";
	}

	@Override
	protected void loadContents() {
		contents.addLine("1 Frames");
		contents.addLine("2 Macros");
		contents.addLine("3 Manual Fncts");
		contents.addLine("4 I/O Registers");
	}
	
	@Override
	protected void loadOptions() {
		options.clear();
	}
	
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
			robotRun.nextScreen(ScreenMode.NAV_IOREGS);
		}
	}
}

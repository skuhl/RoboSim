package screen;

import core.RobotRun;
import enums.ScreenMode;

public class ScreenMainMenu extends Screen {

	public ScreenMainMenu(ScreenMode m, RobotRun r) {
		super(ScreenMode.NAV_MAIN_MENU, r);
	}

	@Override
	void loadHeader() {
		header = "MAIN MENU";
	}

	@Override
	void loadContents() {
		contents.addLine("1 Frames"           );
		contents.addLine("2 Macros"           );
		contents.addLine("3 Manual Fncts"     );
	}

	@Override
	void loadOptions() {}

	@Override
	void loadLabels() {}

	@Override
	public void loadVars() {
		contents.reset();
		contents.setColumnIdx(0);
	}

	@Override
	public void loadPrev() {}

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
		if(options.getLineIdx() == 0) { // Frames
			robotRun.nextScreen(ScreenMode.SELECT_FRAME_MODE);
		} else if(options.getLineIdx() == 1) { // Macros
			robotRun.nextScreen(ScreenMode.NAV_MACROS);
		} else { // Manual Functions
			robotRun.nextScreen(ScreenMode.NAV_MF_MACROS);
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

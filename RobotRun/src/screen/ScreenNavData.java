package screen;

import core.RobotRun;
import enums.ScreenMode;

public class ScreenNavData extends ST_ScreenListContents {

	public ScreenNavData(RobotRun r) {
		super(ScreenMode.NAV_DATA, r);
	}

	@Override
	String loadHeader() {
		return "VIEW REGISTERS";
	}

	@Override
	void loadContents() {
		contents.addLine("1. Data Registers");
		contents.addLine("2. Position Registers");
	}

	@Override
	void loadVars() {}

	@Override
	public void actionEntr() {
		int select = contents.getLineIdx();
		
		if (select == 0) {
			// Data Register Menu
			robotRun.nextScreen(ScreenMode.NAV_DREGS);
		} else if (select == 1) {
			// Position Register Menu
			robotRun.nextScreen(ScreenMode.NAV_PREGS);
		}
	}

}

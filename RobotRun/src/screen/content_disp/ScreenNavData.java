package screen.content_disp;

import core.RobotRun;
import screen.ScreenMode;
import screen.ScreenState;

public class ScreenNavData extends ST_ScreenListContents {

	public ScreenNavData(ScreenState prevState, RobotRun r) {
		super(ScreenMode.NAV_DATA, prevState, r);
	}

	@Override
	protected String loadHeader() {
		return "VIEW REGISTERS";
	}

	@Override
	protected void loadContents() {
		contents.addLine("1. Data Registers");
		contents.addLine("2. Position Registers");
	}

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

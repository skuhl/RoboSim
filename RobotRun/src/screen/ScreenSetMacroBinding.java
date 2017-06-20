package screen;

import core.RobotRun;
import enums.ScreenMode;

public class ScreenSetMacroBinding extends ST_ScreenOptionsMenu {

	public ScreenSetMacroBinding(RobotRun r) {
		super(ScreenMode.SET_MACRO_BINDING, r);
	}

	@Override
	String loadHeader() {
		return "VIEW/ EDIT MACROS";
	}
	
	@Override
	void loadContents() {
		contents.setLines(robotRun.loadMacros());
	}

	@Override
	void loadOptions() {
		options.addLine("1. Tool 1");
		options.addLine("2. Tool 2");
		options.addLine("3. MVMU");
		options.addLine("4. Setup");
		options.addLine("5. Status");
		options.addLine("6. POSN");
		options.addLine("7. I/O");
	}

	@Override
	public void actionEntr() {
		robotRun.macroEdit.setNum(options.getLineIdx());
		robotRun.lastScreen();
	}

}

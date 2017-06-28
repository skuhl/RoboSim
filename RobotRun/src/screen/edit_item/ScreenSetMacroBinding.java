package screen.edit_item;

import core.RobotRun;
import screen.ScreenMode;
import screen.ScreenState;

public class ScreenSetMacroBinding extends ST_ScreenEditItem {

	public ScreenSetMacroBinding(RobotRun r) {
		super(ScreenMode.SET_MACRO_BINDING, r);
	}

	@Override
	protected String loadHeader() {
		return "VIEW/ EDIT MACROS";
	}
	
	@Override
	protected void loadContents() {
		contents.setLines(robotRun.loadMacros());
	}

	@Override
	protected void loadOptions() {
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
		int idx = robotRun.getLastScreen().getContentIdx();
		robotRun.getMacro(idx).setNum(options.getLineIdx());
		robotRun.lastScreen();
	}

}

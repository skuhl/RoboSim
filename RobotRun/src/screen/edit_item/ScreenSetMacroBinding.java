package screen.edit_item;

import core.RobotRun;
import global.DataManagement;
import robot.RoboticArm;
import screen.ScreenMode;

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
		contents.setLines(loadMacros());
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
		RoboticArm r = robotRun.getActiveRobot();
		int idx = robotRun.getLastScreen().getContentIdx();
		
		r.getMacro(idx).setNum(options.getLineIdx());
		DataManagement.saveRobotData(r, 8);
		robotRun.lastScreen();
	}

}

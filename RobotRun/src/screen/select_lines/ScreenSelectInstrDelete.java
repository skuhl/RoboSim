package screen.select_lines;

import core.RobotRun;
import programming.Program;
import robot.RoboticArm;
import screen.ScreenMode;
import screen.ScreenState;

public class ScreenSelectInstrDelete extends ST_ScreenLineSelect {

	public ScreenSelectInstrDelete(ScreenState prevState, int numOfLines, RobotRun r) {
		super(ScreenMode.SELECT_INSTR_DELETE, prevState, numOfLines, r);
	}

	@Override
	protected void loadOptions() {
		options.addLine("Select lines to delete (ENTER).");
	}

	@Override
	protected void loadLabels() {
		// F4, F5
		labels[0] = "";
		labels[1] = "";
		labels[2] = "";
		labels[3] = "[Confirm]";
		labels[4] = "[Cancel]";
	}

	@Override
	public void actionF4() {
		Program p = robotRun.getActiveProg();
		RoboticArm r = robotRun.getActiveRobot();
		int instrIdx = 0;

		for (int i = 0; i < lineSelectState.length; i += 1) {
			if (isSelected(i)) {
				r.rmInstAt(p, instrIdx);
			} else {
				instrIdx += 1;
			}
		}

		robotRun.popScreenStack(1);
		robotRun.updateInstructions();
	}
	
	@Override
	public void actionF5() {
		robotRun.popScreenStack(1);
		robotRun.updateInstructions();
	}
}

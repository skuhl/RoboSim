package screen.select_lines;

import core.RobotRun;
import global.Fields;
import global.RMath;
import programming.Program;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenSelectInstrDelete extends ST_ScreenLineSelect {

	public ScreenSelectInstrDelete(RobotRun r) {
		super(ScreenMode.SELECT_INSTR_DELETE, r);
	}

	@Override
	public void actionF4() {
		Program p = robotRun.getActiveProg();
		RoboticArm r = robotRun.getActiveRobot();
		int instrIdx = 0;
		boolean isFirst = true;

		for (int i = 0; i < lineSelectState.length; i += 1) {
			if (lineSelectState[i]) {
				r.rmInstAt(p, instrIdx, !isFirst);
				isFirst = false;
				
			} else {
				instrIdx += 1;
			}
		}
		
		contents.setLineIdx(RMath.clamp(contents.getLineIdx(), 0, p.getNumOfInst()));
		Fields.debug( Integer.toString(contents.getLineIdx()) );

		robotRun.lastScreen();
		robotRun.getLastScreen().setContentIdx(contents.getLineIdx());
		robotRun.lastScreen();
	}

	@Override
	public void actionF5() {
		robotRun.lastScreen();
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
	protected void loadOptions() {
		options.addLine("Select lines to delete (ENTER).");
	}
}

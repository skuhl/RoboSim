package screen.num_entry;

import core.RobotRun;
import enums.ScreenMode;
import global.RMath;
import programming.Program;

public class ScreenJumpToLine extends ST_ScreenNumEntry {

	public ScreenJumpToLine(RobotRun r) {
		super(ScreenMode.JUMP_TO_LINE, r);
	}

	@Override
	protected void loadOptions() {
		options.addLine("Use number keys to enter line number to jump to");
		options.addLine("\0" + workingText);
	}

	@Override
	public void actionEntr() {
		Program p = robotRun.getActiveProg();
		int jumpToInst = Integer.parseInt(workingText.toString()) - 1;
		robotRun.setActiveInstIdx(RMath.clamp(jumpToInst, 0, p.getNumOfInst() - 1));
		robotRun.lastScreen();
	}
}

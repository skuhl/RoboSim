package screen.num_entry;

import core.RobotRun;
import global.Fields;
import programming.Program;
import screen.ScreenMode;

public class ScreenJumpToLine extends ST_ScreenNumEntry {

	public ScreenJumpToLine(RobotRun r) {
		super(ScreenMode.JUMP_TO_LINE, r);
	}

	@Override
	public void actionEntr() {
		Program p = robotRun.getActiveProg();
		int instIdx = Integer.parseInt(workingText.toString()) - 1;
		
		if (instIdx < 0 || instIdx > p.getNumOfInst()) {
			Fields.setMessage("The instruction index must be within the range 1 and %d",
					p.getNumOfInst());
			
		} else {
			robotRun.lastScreen();
			robotRun.setActiveInstIdx(instIdx);
			robotRun.getActiveScreen().getContents().jumpToItem(instIdx);
		}
	}

	@Override
	protected void loadOptions() {
		options.addLine("Use number keys to enter line number to jump to");
		options.addLine("\0" + workingText);
	}
}

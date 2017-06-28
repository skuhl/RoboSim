package screen.text_entry;

import core.RobotRun;
import programming.Instruction;
import programming.Program;
import screen.ScreenMode;
import screen.ScreenState;

public class ScreenFindReplace extends ST_ScreenTextEntry {

	public ScreenFindReplace(RobotRun r) {
		super(ScreenMode.FIND_REPL, r);
	}
	
	@Override
	protected String loadHeader() {
		return robotRun.getActiveProg().getName();
	}
	
	@Override
	protected void loadContents() {
		contents.setLines(robotRun.loadInstructions(robotRun.getActiveProg(), true));
	}
	
	@Override
	protected void loadOptions() {
		options.addLine("Enter text to search for:");
		options.addLine("\0" + workingText);
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
	protected void loadVars(ScreenState s) {
		setScreenIndices(s.conLnIdx, 0, s.conRenIdx, -1, 0);
	}

	@Override
	public void actionEntr() {
		robotRun.lastScreen();
	}
	
	@Override
	public void actionF1() {}

	@Override
	public void actionF2() {}
	
	@Override
	public void actionF3() {}
	
	@Override
	public void actionF4() {
		Program p = robotRun.getActiveProg();
		int lineIdx = 0;
		String s;

		for (Instruction instruct : p) {
			s = (lineIdx + 1) + ") " + instruct.toString();

			if (s.toUpperCase().contains(workingText.toString().toUpperCase())) {
				break;
			}

			lineIdx += 1;
		}

		robotRun.popScreenStack(1);
		robotRun.setActiveInstIdx(lineIdx);
		robotRun.updateInstructions();
	}
	
	@Override
	public void actionF5() {
		robotRun.popScreenStack(1);
		robotRun.updateInstructions();
	}
}

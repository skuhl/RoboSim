package screen.text_entry;

import core.RobotRun;
import programming.InstElement;
import programming.Instruction;
import programming.Program;
import screen.ScreenMode;

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
		contents.setLines(loadInstructions(robotRun.getActiveProg(), false));
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

		for (InstElement e : p) {
			Instruction instruct = e.getInst();
			s = (lineIdx + 1) + ") " + instruct.toString();

			if (s.toUpperCase().contains(workingText.toString().toUpperCase())) {
				break;
			}

			lineIdx += 1;
		}

		robotRun.lastScreen();
		robotRun.setActiveInstIdx(lineIdx);
		robotRun.updateInstructions();
	}
	
	@Override
	public void actionF5() {
		robotRun.lastScreen();
		robotRun.updateInstructions();
	}
}

package screen.num_entry;

import core.RobotRun;
import enums.ScreenMode;
import screen.Screen;

public abstract class ST_ScreenNumEntry extends Screen {
	static final int NUM_ENTRY_LEN = 9;
	protected StringBuilder workingText;
	
	public ST_ScreenNumEntry(ScreenMode m, RobotRun r) {
		super(m, r);
		workingText = new StringBuilder();
	}
	
	@Override
	protected void loadContents() {
		contents.setLines(robotRun.loadInstructions(robotRun.getActiveProg()));
	}

	@Override
	protected void loadOptions() {}
	
	@Override
	protected void loadLabels() {
		labels[0] = "";
		labels[1] = "";
		labels[2] = "";
		labels[3] = "";
		labels[4] = "";
	}

	@Override
	protected void loadVars() {}
	
	public void actionKeyPressed(char key) {
		if (mode == ScreenMode.SET_MINST_SPD) {
			// Special case for motion instruction speed number entry
			if ((key >= '0' && key <= '9') && workingText.length() < 4) {
				workingText.append(key);
			}
			
		} else if ((key >= '0' && key <= '9') || key == '.' || key == '-') {
			// Append the character
			workingText.append(key);
		}
	}

	@Override
	public void actionUp() {}

	@Override
	public void actionDn() {}

	@Override
	public void actionLt() {}

	@Override
	public void actionRt() {}
	
	public void actionBkspc() {
		// Functions as a backspace key
		if (workingText.length() > 0) {
			workingText.deleteCharAt(workingText.length() - 1);
		}
	}

	@Override
	public void actionEntr() {}
	
	@Override
	public void actionF1() {}

	@Override
	public void actionF2() {}

	@Override
	public void actionF3() {}

	@Override
	public void actionF4() {}

	@Override
	public void actionF5() {}
}
package screen.num_entry;

import core.RobotRun;
import global.Fields;
import screen.Screen;
import screen.ScreenMode;
import screen.ScreenState;

public abstract class ST_ScreenNumEntry extends Screen {
	static final int NUM_ENTRY_LEN = 9;
	protected StringBuilder workingText;
	
	public ST_ScreenNumEntry(ScreenMode m, RobotRun r) {
		super(m, r);
		workingText = new StringBuilder("");
	}
	
	@Override
	public void actionArrowDn() {}
	
	@Override
	public void actionArrowLt() {}
	
	@Override
	public void actionArrowRt() {}

	@Override
	public void actionArrowUp() {}
	
	@Override
	public void actionBkspc() {
		// Functions as a backspace key
		if (workingText.length() > 0) {
			workingText.deleteCharAt(workingText.length() - 1);
		}
	}

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
	
	@Override
	public void actionKeyPress(char key) {
		if (mode == ScreenMode.SET_MINST_SPD) {
			// Special case for motion instruction speed number entry
			if ((key >= '0' && key <= '9') && workingText.length() < 4) {
				workingText.append(key);
			}
			
		} else if ((key >= '0' && key <= '9') || key == '.' || key == '-') {
			// Append the character
			workingText.append(key);
		}
		
		robotRun.updatePendantScreen();
	}

	/**
	 * Renders the given message in the application UI and resets the working
	 * text for this screen.
	 * 
	 * @param msg	The message to render in the application UI
	 */
	protected void errorMessage(String msg) {
		Fields.setMessage(msg);
		workingText = new StringBuilder("");
		robotRun.updatePendantScreen();
	}

	/**
	 * Creates an error message from the given format String and arguments.
	 * 
	 * @param format
	 * @param args
	 */
	protected void errorMessage(String format, Object... args) {
		errorMessage( String.format(format, args) );
	}

	@Override
	protected void loadContents() {
		contents.setLines(loadInstructions(robotRun.getActiveProg(), false));
	}

	@Override
	protected String loadHeader() {
		return robotRun.getActiveProg().getName();
	}
	
	@Override
	protected void loadLabels() {
		labels[0] = "";
		labels[1] = "";
		labels[2] = "";
		labels[3] = "";
		labels[4] = "";
	}
	
	@Override
	protected void loadVars(ScreenState s) {
		setScreenIndices(s.conLnIdx, s.conColIdx, s.conRenIdx, 0, 0);
	}
}

package screen.edit_point;

import core.RobotRun;
import enums.ScreenMode;
import screen.Screen;
import screen.ScreenState;
import ui.DisplayLine;

public abstract class ST_ScreenPointEntry extends Screen {
	static final int NUM_ENTRY_LEN = 9;

	public ST_ScreenPointEntry(ScreenMode m, RobotRun r) {
		super(m, r);
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
	protected void loadVars(ScreenState s) {
		setScreenIndices(0, 1, 0, -1, -1);
	}

	public void actionKeyPressed(char key) {
		if ((key >= '0' && key <= '9') || key == '-' || key == '.') {
			DisplayLine entry = contents.getCurrentItem();
			int idx = contents.getColumnIdx();
			
			if (entry.get(idx) == "\0") {
				entry.set(idx, Character.toString(key));
				actionRt();
				
			// Include prefix in length	
			} else if (entry.size() < (NUM_ENTRY_LEN + 1)) {
				entry.add(idx, Character.toString(key));
				actionRt();
			}
		}
	}
	
	@Override
	public void actionUp() {
		contents.moveUp(false);
	}

	@Override
	public void actionDn() {
		contents.moveDown(false);
	}

	@Override
	public void actionLt() {
		contents.moveLeft();
	}

	@Override
	public void actionRt() {
		DisplayLine entry = contents.getCurrentItem();
		int idx = contents.getColumnIdx();
		int size = entry.size();

		// Delete a digit from the beginning of the number entry
		if (robotRun.isShift()) {
			if (size > 2) {
				entry.remove(idx);
			} else {
				// Leave at least one space value entry
				entry.set(idx, "\0");
			}
		} else {
			if (idx == (entry.size() - 1) && !entry.get(idx).equals("\0") && entry.size() < 10) {
				entry.add("\0");
			}

			contents.moveRight();
		}
	}
	
	public void actionBkspc() {
		DisplayLine entry = contents.getCurrentItem();
		int idx = contents.getColumnIdx();

		if (entry.size() > 2) {
			if (idx > 1) {
				contents.setSelectedColumnIdx(--idx);
			}

			entry.remove(idx);
		} else {
			entry.set(idx, "\0");
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
}

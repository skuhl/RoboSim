package screen.edit_point;

import core.RobotRun;
import enums.ScreenMode;
import screen.Screen;
import ui.DisplayLine;

public abstract class ST_ScreenPointEntry extends Screen {

	public ST_ScreenPointEntry(ScreenMode m, RobotRun r) {
		super(m, r);
	}

	@Override
	protected void loadContents() {
		//TODO
	}
	
	@Override
	protected void loadOptions() {}
	
	@Override
	protected void loadVars() {}

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
		DisplayLine entry = contents.getActiveLine();
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

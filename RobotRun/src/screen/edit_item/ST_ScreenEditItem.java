package screen.edit_item;

import core.RobotRun;
import screen.Screen;
import screen.ScreenMode;
import screen.ScreenState;

public abstract class ST_ScreenEditItem extends Screen {

	public ST_ScreenEditItem(ScreenMode m, RobotRun r) {
		super(m, r);
	}
	
	@Override
	public void actionArrowDn() {
		options.moveDown(false);
	}
	
	@Override
	public void actionArrowLt() {}
	
	@Override
	public void actionArrowRt() {}

	@Override
	public void actionArrowUp() {
		options.moveUp(false);
	}
	
	@Override
	public void actionBkspc() {}
	
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
	public void actionKeyPress(char key) {}

	@Override
	protected void loadContents() {
		contents.setLines(loadInstructions(robotRun.getActiveProg(), true));
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

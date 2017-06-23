package screen.content_disp;

import core.RobotRun;
import enums.ScreenMode;
import screen.Screen;
import screen.ScreenState;

public abstract class ST_ScreenListContents extends Screen {

	public ST_ScreenListContents(ScreenMode m, RobotRun r) {
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
		setScreenIndices(s.conLnIdx, s.conColIdx, s.conRenIdx, -1, 0);
	}

	@Override
	public void actionUp() {
		contents.moveUp(robotRun.isShift());
	}

	@Override
	public void actionDn() {
		contents.moveDown(robotRun.isShift());
	}

	@Override
	public void actionLt() {
		contents.moveLeft();
	}

	@Override
	public void actionRt() {
		contents.moveRight();
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

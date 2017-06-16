package screen;

import core.RobotRun;
import enums.ScreenMode;

public abstract class ST_ScreenListContents extends Screen {

	public ST_ScreenListContents(ScreenMode m, RobotRun r) {
		super(m, r);
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
	
}

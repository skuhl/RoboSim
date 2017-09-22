package screen.content_disp;

import core.RobotRun;
import screen.Screen;
import screen.ScreenMode;
import screen.ScreenState;

public abstract class ST_ScreenListContents extends Screen {

	public ST_ScreenListContents(ScreenMode m, RobotRun r) {
		super(m, r);
	}
	
	@Override
	public void actionArrowDn() {
		contents.moveDown(robotRun.isShift());
	}
	
	@Override
	public void actionArrowLt() {
		contents.moveLeft();
	}
	
	@Override
	public void actionArrowRt() {
		contents.moveRight();
	}
	
	@Override
	public void actionArrowUp() {
		contents.moveUp(robotRun.isShift());
	}

	@Override
	public void actionBkspc() {}

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

	@Override
	public void actionKeyPress(char key) {}

	@Override
	protected void loadLabels() {
		labels[0] = "";
		labels[1] = "";
		labels[2] = "";
		labels[3] = "";
		labels[4] = "";
	}

	@Override
	protected void loadOptions() {}

	@Override
	protected void loadVars(ScreenState s) {
		setScreenIndices(0, 0, 0, 0, 0);
	}
}

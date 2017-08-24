package screen.opt_menu;

import core.RobotRun;
import screen.Screen;
import screen.ScreenMode;
import screen.ScreenState;

public abstract class ST_ScreenOptionsMenu extends Screen {

	public ST_ScreenOptionsMenu(ScreenMode m, RobotRun r) {
		super(m, r);
	}
	
	public ST_ScreenOptionsMenu(ScreenMode m, RobotRun r, int cMax, int cX,
			int cY, int oMax, int oX, int oY) {
		
		super(m, r, cMax, cX, cY, oMax, oX, oY);
	}
	
	public ST_ScreenOptionsMenu(ScreenMode m, String header,
			RobotRun r) {
		
		super(m, header, r);
	}
	
	public ST_ScreenOptionsMenu(ScreenMode m, String header, RobotRun r,
			int cMax, int cX, int cY, int oMax, int oX, int oY) {
		
		super(m, header, r, cMax, cX, cY, oMax, oX, oY);
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
	protected void loadContents() {}

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
		setScreenIndices(0, 0, 0, 0, 0);
	}
}

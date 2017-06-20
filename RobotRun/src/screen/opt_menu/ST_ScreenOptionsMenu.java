package screen.opt_menu;

import core.RobotRun;
import enums.ScreenMode;
import screen.Screen;

public abstract class ST_ScreenOptionsMenu extends Screen {

	public ST_ScreenOptionsMenu(ScreenMode m, RobotRun r) {
		super(m, r);
	}
	
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
	protected void loadVars() {}

	@Override
	public void actionUp() {
		options.moveUp(false);
	}

	@Override
	public void actionDn() {
		options.moveDown(false);
	}

	@Override
	public void actionLt() {}

	@Override
	public void actionRt() {}

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

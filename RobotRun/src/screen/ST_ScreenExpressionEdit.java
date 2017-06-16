package screen;

import core.RobotRun;
import enums.ScreenMode;

public abstract class ST_ScreenExpressionEdit extends Screen {

	public ST_ScreenExpressionEdit(ScreenMode m, RobotRun r) {
		super(m, r);
	}
	
	@Override
	String loadHeader() {
		return robotRun.getActiveProg().getName();
	}
	
	@Override
	void loadContents() {
		contents.setLines(robotRun.loadInstructions(robotRun.getActiveProg()));
	}
	
	@Override
	void loadLabels() {}

	@Override
	void loadVars() {}

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

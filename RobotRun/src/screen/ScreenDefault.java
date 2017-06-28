package screen;

import core.RobotRun;

public class ScreenDefault extends Screen {
	
	public ScreenDefault(RobotRun r) {
		super(ScreenMode.DEFAULT, r);
	}

	@Override
	protected String loadHeader() {
		return "";
	}

	@Override
	protected void loadContents() {}

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
		setScreenIndices(0, 0, 0, 0, 0);
	}

	@Override
	public void actionKeyPress(char key) {}
	
	@Override
	public void actionUp() {}

	@Override
	public void actionDn() {}

	@Override
	public void actionLt() {}

	@Override
	public void actionRt() {}
	
	@Override
	public void actionEntr() {}
	
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
	
}

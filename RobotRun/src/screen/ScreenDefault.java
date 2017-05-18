package screen;

import enums.ScreenMode;
import robot.RobotRun;

public class ScreenDefault extends Screen {
	public ScreenDefault(RobotRun r) {
		super(ScreenMode.DEFAULT, r);
	}

	@Override
	void loadHeader() {
		header = "";
	}

	@Override
	void loadContents() {
		contents.clear();
	}

	@Override
	void loadOptions() {
		options.clear();
	}

	@Override
	void loadLabels() {
		labels[0] = "";
		labels[1] = "";
		labels[2] = "";
		labels[3] = "";
		labels[4] = "";
	}
	
	@Override
	public void loadVars() {}

	@Override
	public void loadPrev() {}

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

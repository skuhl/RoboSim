package screen;

import core.RobotRun;
import enums.ScreenMode;
import ui.MenuScroll;

public class ScreenDefault extends Screen {
	public ScreenDefault(RobotRun r) {
		super(ScreenMode.DEFAULT, r);
	}

	@Override
	String loadHeader() {
		return "";
	}

	@Override
	MenuScroll loadContents() {
		contents.clear();
		return contents;
	}

	@Override
	MenuScroll loadOptions() {
		options.clear();
		return options;
	}

	@Override
	String[] loadLabels() {
		String[] lbls = new String[5];
		
		lbls[0] = "";
		lbls[1] = "";
		lbls[2] = "";
		lbls[3] = "";
		lbls[4] = "";
		
		return lbls;
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

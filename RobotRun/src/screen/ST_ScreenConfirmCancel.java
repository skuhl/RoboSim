package screen;

import core.RobotRun;
import enums.ScreenMode;

public abstract class ST_ScreenConfirmCancel extends Screen {

	public ST_ScreenConfirmCancel(ScreenMode m, RobotRun r) {
		super(m, r);
	}
		
	String loadHeader() {
		return robotRun.getActiveProg().getName();
	}
	
	void loadContents() {}
	
	void loadLabels() {
		// F4, F5
		labels[0] = "";
		labels[1] = "";
		labels[2] = "";
		labels[3] = "[Confirm]";
		labels[4] = "[Cancel]";
	}
	
	void loadVars() {}
		
	public void actionUp() {}
	public void actionDn() {}
	public void actionLt() {}
	public void actionRt() {}
	
	public void actionEntr() {}
	public void actionF1() {}
	public void actionF2() {}
	public void actionF3() {}
	public void actionF4() {}
	public void actionF5() {}

}

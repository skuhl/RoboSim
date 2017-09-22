package screen.cnfrm_cncl;

import core.RobotRun;
import screen.Screen;
import screen.ScreenMode;
import screen.ScreenState;

public abstract class ST_ScreenConfirmCancel extends Screen {

	public ST_ScreenConfirmCancel(ScreenMode m, RobotRun r) {
		super(m, r);
	}
	
	@Override
	public void actionArrowDn() {}
	
	@Override
	public void actionArrowLt() {}
	
	@Override
	public void actionArrowRt() {}
	
	@Override
	public void actionArrowUp() {}
	
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
	protected void loadContents() {}
	
	@Override
	protected String loadHeader() {
		return robotRun.getActiveProg().getName();
	}
	
	@Override
	protected void loadLabels() {
		// F4, F5
		labels[0] = "";
		labels[1] = "";
		labels[2] = "";
		labels[3] = "[Confirm]";
		labels[4] = "[Cancel]";
	}
	
	@Override
	protected void loadVars(ScreenState s) {
		setScreenIndices(0, 0, 0, 0, 0);
	}

}

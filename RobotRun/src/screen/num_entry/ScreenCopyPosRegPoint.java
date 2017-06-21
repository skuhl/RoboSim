package screen.num_entry;

import core.RobotRun;
import enums.ScreenMode;

public class ScreenCopyPosRegPoint extends ST_ScreenNumEntry {

	public ScreenCopyPosRegPoint(RobotRun r) {
		super(ScreenMode.CP_PREG_PT, r);
	}

	@Override
	protected String loadHeader() {
		return null;
	}
	
	@Override
	protected void loadContents() {
		
	}
	
	@Override
	protected void loadOptions() {

	}

	@Override
	public void actionEntr() {

	}

}

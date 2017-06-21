package screen.num_entry;

import core.RobotRun;
import enums.ScreenMode;

public class ScreenCopyDataRegValue extends ST_ScreenNumEntry {

	public ScreenCopyDataRegValue(RobotRun r) {
		super(ScreenMode.CP_DREG_VAL, r);
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

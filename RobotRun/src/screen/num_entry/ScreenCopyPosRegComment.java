package screen.num_entry;

import core.RobotRun;
import enums.ScreenMode;

public class ScreenCopyPosRegComment extends ST_ScreenNumEntry {

	public ScreenCopyPosRegComment(RobotRun r) {
		super(ScreenMode.CP_PREG_COM, r);
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

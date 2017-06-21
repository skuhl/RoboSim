package screen.num_entry;

import core.RobotRun;
import enums.ScreenMode;

public class ScreenCopyDataRegComment extends ST_ScreenNumEntry {

	public ScreenCopyDataRegComment(RobotRun r) {
		super(ScreenMode.CP_DREG_COM, r);
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

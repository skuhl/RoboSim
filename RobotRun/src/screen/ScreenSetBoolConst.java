package screen;

import core.RobotRun;
import enums.ScreenMode;

public class ScreenSetBoolConst extends Screen {

	public ScreenSetBoolConst(ScreenMode m, RobotRun r) {
		super(ScreenMode.SET_BOOL_CONST, r);
	}

	@Override
	void loadHeader() {
		
	}

	@Override
	void loadContents() {
		contents.setLines(robotRun.loadInstructions(RobotRun.getInstanceRobot().getActiveProg()));
	}

	@Override
	void loadOptions() {
		
	}

	@Override
	void loadLabels() {
		
	}

	@Override
	public void loadVars() {
		
	}

	@Override
	public void loadPrev() {
		
	}

	@Override
	public void actionUp() {
		
	}

	@Override
	public void actionDn() {
		
	}

	@Override
	public void actionLt() {
		
	}

	@Override
	public void actionRt() {
		
	}

	@Override
	public void actionEntr() {
		// TODO robotRun.lastScreen();
	}

	@Override
	public void actionF1() {
		
	}

	@Override
	public void actionF2() {
		
	}

	@Override
	public void actionF3() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionF4() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionF5() {
		// TODO Auto-generated method stub
		
	}

}

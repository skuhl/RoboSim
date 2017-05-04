package screen;

import robot.RobotRun;

public class ScreenSetBoolConst extends Screen {

	public ScreenSetBoolConst(ScreenMode m, RobotRun r) {
		super(ScreenMode.SET_BOOL_CONST, r);
	}

	@Override
	void loadHeader() {
		
	}

	@Override
	void loadContents() {
		contents.setContents(robotRun.loadInstructions(RobotRun.getActiveRobot().getActiveProgIdx()));
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
		if(options.getLineIdx() == 0) {
			robotRun.opEdit.set(true);
		} else {
			robotRun.opEdit.set(false);
		}

		robotRun.lastScreen();
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

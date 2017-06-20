package screen.instr_edit;

import core.RobotRun;
import enums.ScreenMode;
import screen.Screen;

public abstract class ST_ScreenInstructionEdit extends Screen {

	public ST_ScreenInstructionEdit(ScreenMode m, RobotRun r) {
		super(m, r);
	}
	
	@Override
	protected String loadHeader() {
		return robotRun.getActiveProg().getName();
	}
	
	@Override
	protected void loadContents() {
		contents.setLines(robotRun.loadInstructions(robotRun.getActiveProg()));
	}
	
	@Override
	protected void loadLabels() {}

	@Override
	protected void loadVars() {}
	
	@Override
	public void actionUp() {
		options.moveUp(false);
	}
	
	@Override
	public void actionDn() {
		options.moveDown(false);
	}
	
	@Override
	public void actionLt() {}
	
	@Override
	public void actionRt() {}
	
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

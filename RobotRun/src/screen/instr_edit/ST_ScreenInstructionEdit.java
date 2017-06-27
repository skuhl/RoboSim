package screen.instr_edit;

import core.RobotRun;
import screen.Screen;
import screen.ScreenMode;
import screen.ScreenState;

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
	protected void loadLabels() {
		labels[0] = "";
		labels[1] = "";
		labels[2] = "";
		labels[3] = "";
		labels[4] = "";
	}

	@Override
	protected void loadVars(ScreenState s) {
		setScreenIndices(s.conLnIdx, s.conColIdx, s.conRenIdx, 0, 0);
	}
	
	@Override
	public void actionKeyPress(char key) {}
	
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
	public void actionBkspc() {}
	
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

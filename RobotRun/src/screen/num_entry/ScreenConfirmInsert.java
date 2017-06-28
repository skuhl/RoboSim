package screen.num_entry;

import core.RobotRun;
import programming.Instruction;
import programming.Program;
import screen.ScreenMode;
import screen.ScreenState;

public class ScreenConfirmInsert extends ST_ScreenNumEntry {

	public ScreenConfirmInsert(ScreenState prevState, RobotRun r) {
		super(ScreenMode.CONFIRM_INSERT, prevState, r);
	}

	@Override
	protected void loadOptions() {
		options.addLine("Enter number of lines to insert:");
		options.addLine("\0" + workingText);
	}
	
	@Override
	protected void loadLabels() {
		// F4, F5
		labels[0] = "";
		labels[1] = "";
		labels[2] = "";
		labels[3] = "";
		labels[4] = "";
	}
	
	@Override
	protected void loadVars(ScreenState s) {
		setScreenIndices(s.conLnIdx, 0, s.conRenIdx, -1, 0);
	}
	
	@Override
	public void actionDn() {
		robotRun.setActiveInstIdx(contents.moveDown(robotRun.isShift()));
	}
	
	@Override
	public void actionUp() {
		robotRun.setActiveInstIdx(contents.moveUp(robotRun.isShift()));
	}
	
	@Override
	public void actionEntr() {
		Program p = robotRun.getActiveProg();
		
		try {
			int lines_to_insert = Integer.parseInt(workingText.toString());
			for (int i = 0; i < lines_to_insert; i += 1) {
				p.addInstAt(robotRun.getActiveInstIdx(), new Instruction());
			}
			
			robotRun.updateInstructions();
		} catch (Exception e) {
			e.printStackTrace();
		}

		robotRun.lastScreen();
	}
}

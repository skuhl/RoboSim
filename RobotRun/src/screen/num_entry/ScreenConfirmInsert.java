package screen.num_entry;

import core.RobotRun;
import enums.ScreenMode;
import programming.Instruction;
import programming.Program;

public class ScreenConfirmInsert extends ST_ScreenNumEntry {

	public ScreenConfirmInsert(RobotRun r) {
		super(ScreenMode.CONFIRM_INSERT, r);
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
		labels[3] = "[Confirm]";
		labels[4] = "[Cancel]";
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
	
	
	//TODO remove unnecessary function operations?
	@Override
	public void actionF4() {
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
	
	@Override
	public void actionF5() {
		robotRun.screenStates.pop();
		robotRun.updateInstructions();
	}
}

package screen.num_entry;

import core.RobotRun;
import programming.Instruction;
import programming.Program;
import robot.RoboticArm;
import screen.ScreenMode;

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
		labels[3] = "";
		labels[4] = "";
	}
	
	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		Program p = robotRun.getActiveProg();
		
		try {
			int lines_to_insert = Integer.parseInt(workingText.toString());
			for (int i = 0; i < lines_to_insert; i += 1) {
				r.addAt(p, robotRun.getActiveInstIdx() + i, new Instruction(), i != 0);
			}
			
			robotRun.updateInstructions();
		} catch (Exception e) {
			e.printStackTrace();
		}

		robotRun.lastScreen();
	}
}

package screen.num_entry;

import core.RobotRun;
import global.Fields;
import programming.BlankInstruction;
import programming.Program;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenConfirmInsert extends ST_ScreenNumEntry {

	public ScreenConfirmInsert(RobotRun r) {
		super(ScreenMode.CONFIRM_INSERT, r);
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		Program p = robotRun.getActiveProg();
		
		try {
			int lines_to_insert = Integer.parseInt(workingText.toString());
			
			if (lines_to_insert > 0 && lines_to_insert <= 100) {
				for (int i = 0; i < lines_to_insert; i += 1) {
					r.addAt(p, robotRun.getActiveInstIdx() + i,
							new BlankInstruction(), i != 0);
				}
				
				robotRun.updateInstructions();
				robotRun.lastScreen();
				
			} else {
				// Out of bounds
				errorMessage("The number of lines must be within the range 0 and 99");
			}
			
		} catch (Exception e) {
			// Not an integer
			Fields.setMessage("The number of lines must be a real number");
		}
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
	protected void loadOptions() {
		options.addLine("Enter number of lines to insert:");
		options.addLine("\0" + workingText);
	}
}

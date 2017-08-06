package screen.num_entry;

import core.RobotRun;
import global.Fields;
import programming.IOInstruction;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenSetIOInstrIdx extends ST_ScreenNumEntry {

	public ScreenSetIOInstrIdx(RobotRun r) {
		super(ScreenMode.SET_IO_INSTR_IDX, r);
	}

	@Override
	public void actionEntr() {
		try {
			RoboticArm r = robotRun.getActiveRobot();
			int tempReg = Integer.parseInt(workingText.toString());

			if (tempReg < 1 || tempReg >= r.numOfEndEffectors()) {
				// Out of bounds
				Fields.setMessage("The index must be within the range 1 and %d",
						r.numOfEndEffectors() - 1);

			} else {
				IOInstruction ioInst = (IOInstruction) r.getInstToEdit(robotRun.getActiveProg(), 
						robotRun.getActiveInstIdx());
				ioInst.setReg(tempReg);
				robotRun.lastScreen();
			}
			
		} catch (NumberFormatException NFEx) {
			// Ignore invalid input
			errorMessage("The index must be an integer");
		}
	}

	@Override
	protected void loadOptions() {
		options.addLine("Select I/O register index:");
		options.addLine("\0" + workingText);
	}
}

package screen.num_entry;

import core.RobotRun;
import programming.IOInstruction;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenSetIOInstrIdx extends ST_ScreenNumEntry {

	public ScreenSetIOInstrIdx(RobotRun r) {
		super(ScreenMode.SET_IO_INSTR_IDX, r);
	}

	@Override
	protected void loadOptions() {
		options.addLine("Select I/O register index:");
		options.addLine("\0" + workingText);
	}

	@Override
	public void actionEntr() {
		try {
			RoboticArm r = robotRun.getActiveRobot();
			int tempReg = Integer.parseInt(workingText.toString());

			if (tempReg < 1 || tempReg >= r.numOfEndEffectors()) {
				System.err.println("Invalid index!");

			} else {
				IOInstruction ioInst = (IOInstruction) r.getInstToEdit(robotRun.getActiveProg(), 
						robotRun.getActiveInstIdx());
				ioInst.setReg(tempReg);
			}
		} catch (NumberFormatException NFEx) {/* Ignore invalid input */}

		robotRun.lastScreen();
	}

}

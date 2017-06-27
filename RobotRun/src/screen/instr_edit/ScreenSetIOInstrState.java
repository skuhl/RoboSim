package screen.instr_edit;

import core.RobotRun;
import global.Fields;
import programming.IOInstruction;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenSetIOInstrState extends ST_ScreenInstructionEdit {

	public ScreenSetIOInstrState(RobotRun r) {
		super(ScreenMode.SET_IO_INSTR_STATE, r);
	}

	@Override
	protected void loadOptions() {
		options.addLine("1. ON");
		options.addLine("2. OFF");
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		IOInstruction ioInst = (IOInstruction) r.getInstToEdit(robotRun.getActiveProg(), 
				robotRun.getActiveInstIdx());

		if (options.getLineIdx() == 0) {
			ioInst.setState(Fields.ON);
		} else {
			ioInst.setState(Fields.OFF);
		}

		robotRun.lastScreen();
	}
}

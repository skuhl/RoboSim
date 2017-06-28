package screen.edit_item;

import core.RobotRun;
import global.Fields;
import programming.IOInstruction;
import robot.RoboticArm;
import screen.ScreenMode;
import screen.ScreenState;

public class ScreenSetIOInstrState extends ST_ScreenEditItem {

	public ScreenSetIOInstrState(ScreenState prevState, RobotRun r) {
		super(ScreenMode.SET_IO_INSTR_STATE, prevState, r);
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

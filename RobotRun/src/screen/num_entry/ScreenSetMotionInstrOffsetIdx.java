package screen.num_entry;

import core.RobotRun;
import enums.ScreenMode;
import global.Fields;
import programming.PosMotionInst;
import robot.RoboticArm;

public class ScreenSetMotionInstrOffsetIdx extends ST_ScreenNumEntry {

	public ScreenSetMotionInstrOffsetIdx(RobotRun r) {
		super(ScreenMode.SET_MINST_OFFIDX, r);
	}

	@Override
	protected void loadOptions() {
		options.addLine("Enter desired offset register (1-100):");
		options.addLine("\0" + workingText);
	}

	@Override
	public void actionEntr() {
		try {
			RoboticArm r = robotRun.getActiveRobot();
			PosMotionInst pMInst = (PosMotionInst) r.getInstToEdit(robotRun.getActiveProg(), 
					robotRun.getActiveInstIdx());
			int tempRegister = Integer.parseInt(workingText.toString()) - 1;
			
			if (tempRegister < 0 || tempRegister > 99) {
				// Invalid register index
				String err = "Only registers 1 - 1000 are legal!";
				System.out.println(err);
				
			} else {
				pMInst.setOffsetType(Fields.OFFSET_PREG);
				pMInst.setOffsetIdx(tempRegister);
			}
			
		} catch (NumberFormatException NFEx) {/* Ignore invalid numbers */ }

		robotRun.lastScreen();
	}
}

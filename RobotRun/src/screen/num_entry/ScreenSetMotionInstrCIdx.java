package screen.num_entry;

import core.RobotRun;
import global.Fields;
import programming.PosMotionInst;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenSetMotionInstrCIdx extends ST_ScreenNumEntry {

	public ScreenSetMotionInstrCIdx(RobotRun r) {
		super(ScreenMode.SET_MINST_CIDX, r);
	}

	@Override
	protected void loadOptions() {
		options.addLine("Enter desired position/ register:");
		options.addLine("\0" + workingText);
	}

	@Override
	public void actionEntr() {
		try {
			RoboticArm r = robotRun.getActiveRobot();
			PosMotionInst pMInst = (PosMotionInst) r.getInstToEdit(robotRun.getActiveProg(), 
					robotRun.getActiveInstIdx());
			int tempRegister = Integer.parseInt(workingText.toString());
			int lbound = 1, ubound;
			
			if (pMInst.getPosType() == Fields.PTYPE_PREG) {
				ubound = 100;
			} else if (pMInst.getPosType() == Fields.PTYPE_PROG) {
				ubound = 1000;
			} else {
				ubound = 0;
			}

			if (tempRegister < lbound || tempRegister > ubound) {
				// Invalid register index
				String err = String.format("Only registers %d-%d are valid!", lbound, ubound);
				Fields.setMessage(err);
				robotRun.lastScreen();
				return;
			}
			
			pMInst.setCircPosIdx(tempRegister - 1);
		} catch (NumberFormatException NFEx) {
			String err = "Invalid entry!";
			Fields.setMessage(err);
		}
		
		robotRun.lastScreen();
	}
}

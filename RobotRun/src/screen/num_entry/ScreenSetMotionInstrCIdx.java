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
				// Out of bounds
				errorMessage("The index must be within the range %d and %d",
						lbound, ubound);
				
			} else {
				pMInst.setCircPosIdx(tempRegister - 1);
				robotRun.lastScreen();
			}
			
		} catch (NumberFormatException NFEx) {
			// Not an integer
			Fields.setMessage("Index must be an integer");
		}
	}

	@Override
	protected void loadOptions() {
		options.addLine("Enter desired position/ register:");
		options.addLine("\0" + workingText);
	}
}

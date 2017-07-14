package screen.num_entry;

import core.RobotRun;
import geom.Scenario;
import global.Fields;
import programming.CamMoveToObject;
import programming.MotionInstruction;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenSetMotionInstrIdx extends ST_ScreenNumEntry {

	public ScreenSetMotionInstrIdx(RobotRun r) {
		super(ScreenMode.SET_MINST_IDX, r);
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
			MotionInstruction mInst = (MotionInstruction) r.getInstToEdit(robotRun.getActiveProg(), 
					robotRun.getActiveInstIdx());
			int tempRegister = Integer.parseInt(workingText.toString());
			int lbound = 1, ubound = 0;
			
			if (mInst.getPosType() == Fields.PTYPE_PREG) {
				ubound = 100;

			} else if (mInst.getPosType() == Fields.PTYPE_PROG) {
				ubound = 1000;
				
			} else if (mInst instanceof CamMoveToObject) {
				Scenario s = ((CamMoveToObject) mInst).getScene();
				
				if (s != null) {
					ubound = s.size();
				}	
			}

			if (tempRegister < lbound || tempRegister > ubound) {
				// Out of bounds
				errorMessage("The index must be with the range %d and %d",
						lbound, ubound);
				
			} else {
				mInst.setPosIdx(tempRegister - 1);
				robotRun.lastScreen();
			}
			
		} catch (NumberFormatException NFEx) {
			// Not an integer
			errorMessage("The index must be integer");
		}
	}
}

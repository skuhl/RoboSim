package screen.num_entry;

import core.RobotRun;
import core.Scenario;
import global.Fields;
import programming.CamMoveToObject;
import programming.MotionInstruction;
import robot.RoboticArm;
import screen.ScreenMode;
import screen.ScreenState;

public class ScreenSetMotionInstrIdx extends ST_ScreenNumEntry {

	public ScreenSetMotionInstrIdx(ScreenState prevState, RobotRun r) {
		super(ScreenMode.SET_MINST_IDX, prevState, r);
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
				// Invalid register index
				String err = String.format("Only registers %d-%d are valid!", lbound, ubound);
				System.err.println(err);
			}
			
			mInst.setPosIdx(tempRegister - 1);
			
		} catch (NumberFormatException NFEx) {/* Ignore invalid numbers */}

		robotRun.lastScreen();
	}

}

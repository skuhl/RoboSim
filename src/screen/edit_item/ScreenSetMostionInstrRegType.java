package screen.edit_item;

import core.RobotRun;
import global.Fields;
import programming.CamMoveToObject;
import programming.MotionInstruction;
import programming.PosMotionInst;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenSetMostionInstrRegType extends ST_ScreenEditItem {

	public ScreenSetMostionInstrRegType(RobotRun r) {
		super(ScreenMode.SET_MINST_REG_TYPE, r);
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		MotionInstruction mInst = (MotionInstruction) r.getInstToEdit(robotRun.getActiveProg(), 
				robotRun.getActiveInstIdx());
		
		if (options.getLineIdx() == 2) {
			if (!(mInst instanceof CamMoveToObject)) {
				// Change to a camera motion instruction
				MotionInstruction mCInst = new CamMoveToObject(
						mInst.getMotionType(), 0,
						mInst.getSpdMod(), mInst.getTermination(),
						robotRun.getActiveScenario()
				);
				
				robotRun.getActiveProg().replaceInstAt(robotRun.getActiveInstIdx(), mCInst);
			}
			
		} else {
			int posType;
			
			if (options.getLineIdx() == 1) {
				posType = Fields.PTYPE_PREG;
				
			} else {
				posType = Fields.PTYPE_PROG;
			}
			
			if (mInst instanceof CamMoveToObject) {
				// Change to a position motion instruction
				MotionInstruction mPInst = new PosMotionInst(
						mInst.getMotionType(), posType, -1,
						mInst.getSpdMod(), mInst.getTermination()
				);
				
				robotRun.getActiveProg().replaceInstAt(robotRun.getActiveInstIdx(), mPInst);
				
			} else if (mInst instanceof PosMotionInst) {
				// Update motion type of the position motion instruction
				mInst.setPosType(posType);
			}
		}
		
		robotRun.lastScreen();
	}

	@Override
	protected void loadOptions() {
		options.addLine("1.LOCAL(P)");
		options.addLine("2.GLOBAL(PR)");
		options.addLine("3.CAM OBJECT(OBJ)");
	}

}

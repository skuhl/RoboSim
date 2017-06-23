package screen.edit_point;

import core.RobotRun;
import enums.ScreenMode;
import geom.Point;
import global.DataManagement;
import global.Fields;
import programming.PosMotionInst;
import programming.Program;
import robot.RoboticArm;

public class ScreenEditProgramPos extends ST_ScreenPointEntry {

	public ScreenEditProgramPos(RobotRun r) {
		super(ScreenMode.EDIT_PROG_POS, r);
	}

	@Override
	protected String loadHeader() {
		return String.format("EDIT %s POSITION", robotRun.getActiveProg().getName());
	}

	@Override
	protected void loadContents() {
		Program prog = robotRun.getActiveProg();
		PosMotionInst pMInst = (PosMotionInst)robotRun.getActiveInstruction();
		Point pt = prog.getPosition(pMInst.getPosIdx());
		
		// Initialize the point if it is null
		if (pt == null) {
			pt = new Point();
			prog.setPosition(pMInst.getPosIdx(), pt);
		}
		
		boolean isCartesian = pMInst.getMotionType() != Fields.MTYPE_JOINT;
		contents.setLines(robotRun.loadPosition(pt, isCartesian));
	}
	
	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		PosMotionInst pMInst = (PosMotionInst) r.getInstToEdit(robotRun.getActiveProg(), 
				robotRun.getActiveInstIdx());
		Point pt = robotRun.parsePosFromContents(pMInst.getMotionType() != Fields.MTYPE_JOINT);

		if (pt != null) {
			// Update the position of the active motion instruction
			robotRun.getActiveProg().setPosition(pMInst.getPosIdx(), pt);
			DataManagement.saveRobotData(r, 1);
		}

		robotRun.lastScreen();
	}
}

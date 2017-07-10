package screen.opt_menu;

import core.RobotRun;
import programming.CallInstruction;
import programming.IfStatement;
import programming.Instruction;
import programming.Program;
import programming.SelectStatement;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenSetCallProg extends ST_ScreenOptionsMenu {

	public ScreenSetCallProg(RobotRun r) {
		super(ScreenMode.SET_CALL_PROG, r);
	}

	@Override
	protected String loadHeader() {
		return "SELECT CALL TARGET";
	}

	@Override
	protected void loadOptions() {
		Instruction inst = robotRun.getActiveInstruction();
		CallInstruction cInst;
		
		// Get the call instruction
		if (inst instanceof IfStatement) {
			cInst = (CallInstruction) ((IfStatement) inst).getInstr();
			
		} else if (inst instanceof SelectStatement) {
			SelectStatement sStmt = (SelectStatement) inst;
			cInst = (CallInstruction) sStmt.getInstrs().get(robotRun.editIdx);
			
		} else {
			cInst =  (CallInstruction) inst;
		}
					
		// List the robot's program names
		options.setLines(loadPrograms(cInst.getTgtDevice()));
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		Instruction inst = r.getInstToEdit(robotRun.getActiveProg(), 
				robotRun.getActiveInstIdx());
		CallInstruction cInst;
		
		// Get the call instruction
		if (inst instanceof IfStatement) {
			cInst = (CallInstruction) ((IfStatement) inst).getInstr();
			
		} else if (inst instanceof SelectStatement) {
			SelectStatement sStmt = (SelectStatement) inst;
			cInst = (CallInstruction) sStmt.getInstrs().get(robotRun.editIdx);
			
		} else {
			cInst =  (CallInstruction) inst;
		}
		
		// Set the program of the call instruction
		Program tgt = cInst.getTgtDevice().getProgram(options.getCurrentItemIdx());
		cInst.setProg(tgt);

		robotRun.lastScreen();
	}
}

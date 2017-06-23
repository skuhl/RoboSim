package screen.instr_edit;

import core.RobotRun;
import enums.ScreenMode;
import programming.CallInstruction;
import programming.IfStatement;
import programming.JumpInstruction;
import robot.RoboticArm;

public class ScreenSetIfStmtAction extends ST_ScreenInstructionEdit {

	public ScreenSetIfStmtAction(RobotRun r) {
		super(ScreenMode.SET_IF_STMT_ACT, r);
	}

	@Override
	protected void loadOptions() {
		options.addLine("JMP LBL[x]");
		options.addLine("CALL");
		
		if (robotRun.getUI().getRobotButtonState()) {
			options.addLine("RCALL");
		}
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		IfStatement stmt = (IfStatement) r.getInstToEdit(robotRun.getActiveProg(), 
				robotRun.getActiveInstIdx());
		
		if (options.getLineIdx() == 0) {
			stmt.setInstr(new JumpInstruction());
			robotRun.switchScreen(ScreenMode.SET_JUMP_TGT);
		} else if (options.getLineIdx() == 1) {
			stmt.setInstr(new CallInstruction(r));
			robotRun.editIdx = r.RID;
			robotRun.switchScreen(ScreenMode.SET_CALL_PROG);
		} else {
			RoboticArm inactive = robotRun.getInactiveRobot();
			stmt.setInstr(new CallInstruction(inactive));
			
			if (inactive.numOfPrograms() > 0) {
				robotRun.switchScreen(ScreenMode.SET_CALL_PROG);
			} else {
				// No programs from which to choose
				robotRun.lastScreen();
			}
		}
	}

}

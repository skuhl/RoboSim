package screen.num_entry;

import core.RobotRun;
import programming.IfStatement;
import programming.Instruction;
import programming.JumpInstruction;
import programming.SelectStatement;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenSetJumpTgt extends ST_ScreenNumEntry {

	public ScreenSetJumpTgt(RobotRun r) {
		super(ScreenMode.SET_JUMP_TGT, r);
	}

	@Override
	protected void loadOptions() {
		options.addLine("Set jump target label:");
		options.addLine("\0" + workingText);
	}

	@Override
	public void actionEntr() {
		try {
			RoboticArm r = robotRun.getActiveRobot();
			int lblNum = Integer.parseInt(workingText.toString());
			Instruction inst = r.getInstToEdit(robotRun.getActiveProg(), 
					robotRun.getActiveInstIdx());
			
			if (lblNum < 0 || lblNum > 99) {
				// Out of bounds
				errorMessage("The ID must be with the range 0 and 99");
				
			} else if (inst instanceof IfStatement) {
				IfStatement ifStmt = (IfStatement) inst;
				((JumpInstruction) ifStmt.getInstr()).setTgtLblNum(lblNum);
				robotRun.lastScreen();
				
			} else if (inst instanceof SelectStatement) {
				SelectStatement sStmt = (SelectStatement) inst;
				((JumpInstruction) sStmt.getInstrs().get(robotRun.editIdx)).setTgtLblNum(lblNum);
				robotRun.lastScreen();
				
			} else {
				JumpInstruction jmp = (JumpInstruction) inst;
				jmp.setTgtLblNum(lblNum);
			}
			
		} catch (NumberFormatException NFEx) {
			// Not an integer
			errorMessage("The ID must be an integer");
		}
	}

}

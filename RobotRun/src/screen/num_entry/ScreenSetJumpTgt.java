package screen.num_entry;

import core.RobotRun;
import enums.ScreenMode;
import programming.IfStatement;
import programming.Instruction;
import programming.JumpInstruction;
import programming.SelectStatement;
import robot.RoboticArm;

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
			int lblIdx = robotRun.getActiveProg().findLabelIdx(lblNum);
			Instruction inst = r.getInstToEdit(robotRun.getActiveProg(), 
					robotRun.getActiveInstIdx());
			
			if (inst instanceof IfStatement) {
				IfStatement ifStmt = (IfStatement) inst;
				((JumpInstruction) ifStmt.getInstr()).setTgtLblNum(lblNum);
			} else if (inst instanceof SelectStatement) {
				SelectStatement sStmt = (SelectStatement) inst;
				((JumpInstruction) sStmt.getInstrs().get(robotRun.editIdx)).setTgtLblNum(lblNum);
			} else {
				if (lblIdx != -1) {
					JumpInstruction jmp = (JumpInstruction) inst;
					jmp.setTgtLblNum(lblNum);
				} else {
					System.err.println("Invalid label number!");
				}
			}
		} catch (NumberFormatException NFEx) {
		/* Ignore invalid input */ }

		robotRun.lastScreen();
	}

}
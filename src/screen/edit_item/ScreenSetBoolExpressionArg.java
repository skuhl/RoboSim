package screen.edit_item;

import core.RobotRun;
import expression.OperandDReg;
import expression.OperandFloat;
import expression.OperandPRegIdx;
import programming.IfStatement;
import regs.PositionRegister;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenSetBoolExpressionArg extends ST_ScreenEditItem {

	public ScreenSetBoolExpressionArg(RobotRun r) {
		super(ScreenMode.SET_BOOL_EXPR_ARG, r);
	}
	
	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		IfStatement stmt = (IfStatement) r.getInstToEdit(robotRun.getActiveProg(), 
				robotRun.getActiveInstIdx());
		
		if(options.getLineIdx() == 0) {
			// set arg to new data reg
			robotRun.opEdit = new OperandDReg();
			stmt.setOperand(robotRun.editIdx, robotRun.opEdit);
			robotRun.switchScreen(ScreenMode.INPUT_DREG_IDX, false);
		} else if(options.getLineIdx() == 2) {
			// set arg to new preg idx
			robotRun.opEdit = new OperandPRegIdx(new PositionRegister(), 0);
			stmt.setOperand(robotRun.editIdx, robotRun.opEdit);
			robotRun.switchScreen(ScreenMode.INPUT_PREG_IDX2, false);
			robotRun.nextScreen(ScreenMode.INPUT_PREG_IDX1);
		} else if(options.getLineIdx() == 1) {
			// set arg to new constant
			robotRun.opEdit = new OperandFloat();
			stmt.setOperand(robotRun.editIdx, robotRun.opEdit);
			robotRun.switchScreen(ScreenMode.INPUT_CONST, false);
		}
	}

	@Override
	protected void loadOptions() {
		options.addLine("R[x]");
		//options.addLine("PR[x, y]"); //TODO
		options.addLine("Const");
	}
}

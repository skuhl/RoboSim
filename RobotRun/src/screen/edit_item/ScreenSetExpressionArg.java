package screen.edit_item;

import core.RobotRun;
import expression.Expression;
import expression.Operand;
import expression.OperandDReg;
import expression.OperandFloat;
import expression.OperandIOReg;
import expression.OperandPReg;
import expression.OperandPRegIdx;
import expression.RobotPoint;
import regs.DataRegister;
import regs.IORegister;
import regs.PositionRegister;
import screen.ScreenMode;
import screen.ScreenState;

public class ScreenSetExpressionArg extends ST_ScreenEditItem {

	public ScreenSetExpressionArg(RobotRun r) {
		super(ScreenMode.SET_EXPR_ARG, r);
	}

	@Override
	protected void loadOptions() {
		options.addLine("R[x]");
		options.addLine("IO[x]");
		if (robotRun.opEdit instanceof Expression) {
			options.addLine("PR[x]");
			options.addLine("PR[x, y]");
			options.addLine("JPos");
			options.addLine("LPos");
			options.addLine("(...)");
		}
		options.addLine("Const");
	}

	@Override
	public void actionEntr() {
		Expression expr = (Expression)robotRun.opEdit;
		Operand<?> operand;
		
		if (options.getLineIdx() == 0) {
			// set arg to new data reg
			operand = new OperandDReg(new DataRegister());
			robotRun.opEdit = expr.setOperand(robotRun.editIdx, operand);
			robotRun.switchScreen(ScreenMode.INPUT_DREG_IDX);
		} else if (options.getLineIdx() == 1) {
			// set arg to new io reg
			operand = new OperandIOReg(new IORegister());
			robotRun.opEdit = expr.setOperand(robotRun.editIdx, operand);
			robotRun.switchScreen(ScreenMode.INPUT_IOREG_IDX);
		} else if (options.getLineIdx() == 2) {
			operand = new OperandPReg(new PositionRegister());
			robotRun.opEdit = expr.setOperand(robotRun.editIdx, operand);
			robotRun.switchScreen(ScreenMode.INPUT_PREG_IDX1);
		} else if (options.getLineIdx() == 3) {
			operand = new OperandPRegIdx(new PositionRegister(), 0);
			robotRun.opEdit = expr.setOperand(robotRun.editIdx, operand);
			robotRun.popScreenStack(1);
			robotRun.nextScreen(ScreenMode.INPUT_PREG_IDX2);
			robotRun.nextScreen(ScreenMode.INPUT_PREG_IDX1);
			
		} else if (options.getLineIdx() == 4) {
			// JPos operand
			operand = new RobotPoint(robotRun.getActiveRobot(), false);
			robotRun.opEdit = expr.setOperand(robotRun.editIdx, operand);
			robotRun.lastScreen();
			
		} else if (options.getLineIdx() == 5) {
			// LPos operand
			operand = new RobotPoint(robotRun.getActiveRobot(), true);
			robotRun.opEdit = expr.setOperand(robotRun.editIdx, operand);
			robotRun.lastScreen();
			
		} else if (options.getLineIdx() == 6) {
			// set arg to new expression
			operand = new Expression();
			robotRun.opEdit = expr.setOperand(robotRun.editIdx, operand);
			robotRun.lastScreen();
		} else {
			// set arg to new constant
			operand = new OperandFloat();
			robotRun.opEdit = expr.setOperand(robotRun.editIdx, operand);
			robotRun.switchScreen(ScreenMode.INPUT_CONST);
		}
	}
}

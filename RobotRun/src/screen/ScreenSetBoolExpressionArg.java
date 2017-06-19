package screen;

import core.RobotRun;
import enums.ScreenMode;
import expression.Expression;
import expression.OperandDReg;
import expression.OperandFloat;
import expression.OperandIOReg;
import programming.IfStatement;
import robot.RoboticArm;

public class ScreenSetBoolExpressionArg extends ST_ScreenExpressionEdit {

	public ScreenSetBoolExpressionArg(RobotRun r) {
		super(ScreenMode.SET_BOOL_EXPR_ARG, r);
	}
	
	@Override
	void loadOptions() {
		options.addLine("R[x]");
		options.addLine("IO[x]");
		if (robotRun.opEdit instanceof Expression) {
			options.addLine("PR[x]");
			options.addLine("PR[x, y]");
			options.addLine("(...)");
		}
		options.addLine("Const");
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		IfStatement stmt = (IfStatement) r.getInstToEdit(robotRun.getActiveProg(), 
				robotRun.getActiveInstIdx());
		
		if (options.getLineIdx() == 0) {
			// set arg to new data reg
			robotRun.opEdit = new OperandDReg();
			stmt.setOperand(robotRun.editIdx, robotRun.opEdit);
			robotRun.switchScreen(ScreenMode.INPUT_DREG_IDX);
		} else if (options.getLineIdx() == 1) {
			// set arg to new io reg
			robotRun.opEdit = new OperandIOReg();
			stmt.setOperand(robotRun.editIdx, robotRun.opEdit);
			robotRun.switchScreen(ScreenMode.INPUT_IOREG_IDX);
		} else {
			// set arg to new constant
			robotRun.opEdit = new OperandFloat();
			stmt.setOperand(robotRun.editIdx, robotRun.opEdit);
			robotRun.switchScreen(ScreenMode.INPUT_CONST);
		}
	}
}

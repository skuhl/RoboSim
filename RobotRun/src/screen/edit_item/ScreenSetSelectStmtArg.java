package screen.edit_item;

import core.RobotRun;
import expression.OperandDReg;
import expression.OperandGeneric;
import programming.SelectStatement;
import regs.DataRegister;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenSetSelectStmtArg extends ST_ScreenEditItem {

	public ScreenSetSelectStmtArg(RobotRun r) {
		super(ScreenMode.SET_SELECT_STMT_ARG, r);
	}

	@Override
	public void actionEntr() {
		RoboticArm r  = robotRun.getActiveRobot();
		SelectStatement s = (SelectStatement) r.getInstToEdit(robotRun.getActiveProg(), 
				robotRun.getActiveInstIdx());
		
		if (options.getLineIdx() == 0) {
			robotRun.opEdit = new OperandDReg(new DataRegister());
		} else {
			robotRun.opEdit = new OperandGeneric();
		}
		
		s.setOperand(robotRun.editIdx, robotRun.opEdit);
		robotRun.nextScreen(ScreenMode.SET_SELECT_ARGVAL);
	}

	@Override
	protected void loadOptions() {
		options.addLine("R[x]");
		options.addLine("Const");
	}

}

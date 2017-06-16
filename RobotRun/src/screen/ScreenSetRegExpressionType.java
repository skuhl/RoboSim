package screen;

import core.RobotRun;
import enums.ScreenMode;
import programming.RegisterStatement;
import regs.DataRegister;
import regs.IORegister;
import regs.PositionRegister;
import robot.RoboticArm;

public class ScreenSetRegExpressionType extends ST_ScreenInstructionEdit {

	public ScreenSetRegExpressionType(RobotRun r) {
		super(ScreenMode.SET_REG_EXPR_TYPE, r);
	}

	@Override
	void loadOptions() {
		options.addLine("1. R[x] = (...)");
		options.addLine("2. IO[x] = (...)");
		options.addLine("3. PR[x] = (...)");
		options.addLine("4. PR[x, y] = (...)");
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		RegisterStatement regStmt = (RegisterStatement) r.getInstToEdit(robotRun.getActiveProg(), 
				robotRun.getActiveInstIdx());
		
		if (options.getLineIdx() == 3) {
			regStmt.setRegister(new PositionRegister(), 0);
			
			robotRun.getScreenStates().pop();
			robotRun.pushScreen(ScreenMode.SET_REG_EXPR_IDX2, contents.getLineIdx(),
					contents.getColumnIdx(), contents.getRenderStart(), 0,
					0);
			robotRun.loadScreen(ScreenMode.SET_REG_EXPR_IDX1);
			
		} else {
			if (options.getLineIdx() == 0) {
				regStmt.setRegister(new DataRegister());
			} else if (options.getLineIdx() == 1) {
				regStmt.setRegister(new IORegister());
			} else if (options.getLineIdx() == 2) {
				regStmt.setRegister(new PositionRegister());
			}
			
			robotRun.switchScreen(ScreenMode.SET_REG_EXPR_IDX1);
		}
	}
}

package screen.opt_menu;

import core.RobotRun;
import regs.DataRegister;
import regs.IORegister;
import regs.PositionRegister;
import screen.ScreenMode;

public class ScreenSelectRegStmt extends ST_ScreenOptionsMenu {

	public ScreenSelectRegStmt(RobotRun r) {
		super(ScreenMode.SELECT_REG_STMT, r);
	}

	@Override
	protected String loadHeader() {
		return "SELECT REG STATEMENT TYPE";
	}

	@Override
	protected void loadOptions() {
		options.addLine("1. R[x] = (...)");
		options.addLine("2. IO[x] = (...)");
		options.addLine("3. PR[x] = (...)");
		options.addLine("4. PR[x, y] = (...)");
	}

	@Override
	public void actionEntr() {
		robotRun.getScreenStack().pop();
		robotRun.getScreenStack().pop();

		if (options.getLineIdx() == 0) {
			robotRun.newRegisterStatement(new DataRegister());
		} else if (options.getLineIdx() == 1) {
			robotRun.newRegisterStatement(new IORegister());
		} else if (options.getLineIdx() == 2) {
			robotRun.newRegisterStatement(new PositionRegister());
		} else {
			robotRun.newRegisterStatement(new PositionRegister(), 0);
			robotRun.nextScreen(ScreenMode.SET_REG_EXPR_IDX2);
		}

		robotRun.nextScreen(ScreenMode.SET_REG_EXPR_IDX1);
	}

}

package screen.opt_menu;

import core.RobotRun;
import screen.ScreenMode;

public class ScreenSelectContStmt extends ST_ScreenOptionsMenu {

	public ScreenSelectContStmt(RobotRun r) {
		super(ScreenMode.SELECT_COND_STMT, r);
	}
	
	@Override
	public void actionEntr() {
		if (options.getLineIdx() == 0) {
			robotRun.newIfStatement();
			robotRun.lastScreen();
			robotRun.switchScreen(ScreenMode.SET_EXPR_OP);
		} else if (options.getLineIdx() == 1) {
			robotRun.newIfExpression();
			robotRun.lastScreen();
			robotRun.lastScreen();
		} else if (options.getLineIdx() == 2) {
			robotRun.newSelectStatement();
			robotRun.lastScreen();
			robotRun.lastScreen();
		}
	}

	@Override
	protected String loadHeader() {
		 return "INSERT IF/ SELECT STATEMENT";
	}
	
	@Override
	protected void loadOptions() {
		options.addLine("1. IF Stmt");
		options.addLine("2. IF (...)");
		options.addLine("3. SELECT Stmt");
	}

}

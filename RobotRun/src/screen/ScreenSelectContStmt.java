package screen;

import core.RobotRun;
import enums.ScreenMode;

public class ScreenSelectContStmt extends ST_ScreenOptionsMenu {

	public ScreenSelectContStmt(RobotRun r) {
		super(ScreenMode.SELECT_COND_STMT, r);
	}
	
	@Override
	String loadHeader() {
		 return "INSERT IF/ SELECT STATEMENT";
	}

	@Override
	void loadOptions() {
		options.addLine("1. IF Stmt");
		options.addLine("2. IF (...)");
		options.addLine("3. SELECT Stmt");
	}
	
	@Override
	public void actionEntr() {
		if (options.getLineIdx() == 0) {
			robotRun.newIfStatement();
			robotRun.screenStates.pop();
			robotRun.switchScreen(ScreenMode.SET_EXPR_OP);
		} else if (options.getLineIdx() == 1) {
			robotRun.newIfExpression();
			robotRun.screenStates.pop();
			robotRun.lastScreen();
		} else {
			robotRun.newSelectStatement();
			robotRun.screenStates.pop();
			robotRun.lastScreen();
		}
	}

}

package screen.edit_item;

import core.RobotRun;
import programming.CallInstruction;
import programming.JumpInstruction;
import programming.SelectStatement;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenSetSelectStmtAction extends ST_ScreenEditItem {

	public ScreenSetSelectStmtAction(RobotRun r) {
		super(ScreenMode.SET_SELECT_STMT_ACT, r);
	}

	@Override
	protected void loadOptions() {
		options.addLine("JMP LBL[x]");
		options.addLine("CALL");
		
		if (robotRun.isSecondRobotUsed()) {
			options.addLine("RCALL");
		}
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		SelectStatement s = (SelectStatement) r.getInstToEdit(robotRun.getActiveProg(), 
				robotRun.getActiveInstIdx());
		int i = (contents.getItemColumnIdx() - 3) / 3;

		if (options.getLineIdx() == 0) {
			s.getInstrs().set(i, new JumpInstruction());
		} else if (options.getLineIdx() == 1) {
			s.getInstrs().set(i, new CallInstruction(r));
		} else {
			s.getInstrs().set(i, new CallInstruction(robotRun.getInactiveRobot()));
		}

		robotRun.lastScreen();
	}

}

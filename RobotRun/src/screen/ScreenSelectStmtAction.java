package screen;

import core.RobotRun;
import enums.ScreenMode;
import programming.CallInstruction;
import programming.JumpInstruction;
import programming.SelectStatement;
import robot.RoboticArm;

public class ScreenSelectStmtAction extends ST_ScreenInstructionEdit {

	public ScreenSelectStmtAction(RobotRun r) {
		super(ScreenMode.SET_SELECT_STMT_ACT, r);
	}

	@Override
	void loadOptions() {
		options.addLine("JMP LBL[x]");
		options.addLine("CALL");
		
		if (robotRun.getUI().getRobotButtonState()) {
			options.addLine("RCALL");
		}
	}

	@Override
	public void actionEntr() {
		RoboticArm r = robotRun.getActiveRobot();
		SelectStatement s = (SelectStatement) r.getInstToEdit(robotRun.getActiveProg(), 
				robotRun.getActiveInstIdx());
		int i = (robotRun.getSelectedIdx() - 3) / 3;

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

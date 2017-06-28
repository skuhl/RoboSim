package screen.num_entry;

import core.RobotRun;
import programming.RegisterStatement;
import regs.PositionRegister;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenSetRegExprIdx2 extends ST_ScreenNumEntry {

	public ScreenSetRegExprIdx2(RobotRun r) {
		super(ScreenMode.SET_REG_EXPR_IDX2, r);
	}

	@Override
	protected void loadOptions() {
		options.addLine("Select point index:");
		options.addLine("\0" + workingText);
	}

	@Override
	public void actionEntr() {
		try {
			RoboticArm r = robotRun.getActiveRobot();
			int idx = Integer.parseInt(workingText.toString());

			if (idx < 1 || idx > 6) {
				System.err.println("Invalid position index!");
			} else {
				RegisterStatement regStmt = (RegisterStatement) r.getInstToEdit(robotRun.getActiveProg(), 
						robotRun.getActiveInstIdx());
				if (regStmt.getReg() instanceof PositionRegister) {
					regStmt.setPosIdx(idx - 1);
				}
			}
		} catch (NumberFormatException NFEx) {/* Ignore invalid input */}

		robotRun.lastScreen();
	}

}

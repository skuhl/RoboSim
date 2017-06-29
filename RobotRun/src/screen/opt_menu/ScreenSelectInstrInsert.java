package screen.opt_menu;

import core.RobotRun;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenSelectInstrInsert extends ST_ScreenOptionsMenu {

	public ScreenSelectInstrInsert(RobotRun r) {
		super(ScreenMode.SELECT_INSTR_INSERT, r);
	}

	@Override
	protected String loadHeader() {
		return robotRun.getActiveProg().getName();
	}

	@Override
	protected void loadOptions() {
		options.addLine("1. I/O");
		options.addLine("2. Frames");
		options.addLine("3. Registers");
		options.addLine("4. IF/SELECT");
		options.addLine("5. JMP/LBL");
		options.addLine("6. CALL");
		
		/*
		 * Only allow the user to add robot call instructions when the
		 * second robot is in the application
		 */
		if (robotRun.getUI().getRobotButtonState()) {
			options.addLine("7. RCALL");
		}
	}

	@Override
	public void actionEntr() {
		switch (options.getLineIdx()) {
		case 0: // I/O
			robotRun.nextScreen(ScreenMode.SELECT_IO_INSTR_REG);
			break;
		case 1: // Offset/Frames
			robotRun.nextScreen(ScreenMode.SELECT_FRAME_INSTR_TYPE);
			break;
		case 2: // Register
			robotRun.nextScreen(ScreenMode.SELECT_REG_STMT);
			break;
		case 3: // IF/ SELECT
			robotRun.nextScreen(ScreenMode.SELECT_COND_STMT);
			break;
		case 4: // JMP/ LBL
			robotRun.nextScreen(ScreenMode.SELECT_JMP_LBL);
			break;
		case 5: // Call
			robotRun.newCallInstruction();
			robotRun.editIdx = robotRun.getActiveRobot().RID;
			robotRun.switchScreen(ScreenMode.SET_CALL_PROG);
			break;
		case 6: // RobotCall
			robotRun.newRobotCallInstruction();
			RoboticArm inactive = robotRun.getInactiveRobot();
			
			if (inactive.numOfPrograms() > 0) {
				robotRun.editIdx = robotRun.getInactiveRobot().RID;
				robotRun.switchScreen(ScreenMode.SET_CALL_PROG);
				
			} else {
				// No programs exist in the inactive robot
				robotRun.lastScreen();
			}
		}
	}

}

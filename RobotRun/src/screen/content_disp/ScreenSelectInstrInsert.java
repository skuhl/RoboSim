package screen.content_disp;

import core.RobotRun;
import robot.RoboticArm;
import screen.ScreenMode;

public class ScreenSelectInstrInsert extends ST_ScreenListContents {

	public ScreenSelectInstrInsert(RobotRun r) {
		super(ScreenMode.SELECT_INSTR_INSERT, r);
	}

	@Override
	public void actionEntr() {
		switch (contents.getLineIdx()) {
		case 0: // I/O
			robotRun.nextScreen(ScreenMode.SELECT_IO_INSTR_REG);
			break;
		case 1: // Offset/Frames
			robotRun.switchScreen(ScreenMode.SELECT_FRAME_INSTR_TYPE, true);
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
			callProgSwitchScreen(robotRun.getActiveRobot(), true);
			break;
		case 6: // RobotCall
			robotRun.newRobotCallInstruction();
			RoboticArm inactive = robotRun.getInactiveRobot();
			
			if (inactive.numOfPrograms() > 0) {
				callProgSwitchScreen(inactive, true);
				
			} else {
				// No programs exist in the inactive robot
				robotRun.lastScreen();
			}
		}
	}
	
	@Override
	protected void loadContents() {
		contents.addLine("1. I/O");
		contents.addLine("2. Frames");
		contents.addLine("3. Registers");
		contents.addLine("4. IF/SELECT");
		contents.addLine("5. JMP/LBL");
		contents.addLine("6. CALL");
		
		/*
		 * Only allow the user to add robot call instructions when the
		 * second robot is in the application
		 */
		if (robotRun.isSecondRobotUsed()) {
			contents.addLine("7. RCALL");
		}
	}

	@Override
	protected String loadHeader() {
		return robotRun.getActiveProg().getName();
	}
}

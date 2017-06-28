package screen.opt_menu;

import core.RobotRun;
import programming.Program;
import screen.ScreenMode;
import screen.ScreenState;

public class ScreenNavInstrMenu extends ST_ScreenOptionsMenu {

	public ScreenNavInstrMenu(RobotRun r) {
		super(ScreenMode.NAV_INSTR_MENU, r);
	}
	
	@Override
	protected String loadHeader() {
		return robotRun.getActiveProg().getName();
	}
	
	@Override
	protected void loadOptions() {
		options.addLine("1 Undo");
		options.addLine("2 Insert");
		options.addLine("3 Delete");
		options.addLine("4 Cut/ Copy");
		options.addLine("5 Paste");
		options.addLine("6 Find/ Replace");
		options.addLine("7 Renumber");
		options.addLine("8 Comment");
	}

	@Override
	public void actionEntr() {
		switch (options.getLineIdx()) {
		case 0: // Undo
			robotRun.getActiveRobot().popInstructionUndo(robotRun.getActiveProg());
			robotRun.lastScreen();
			break;
		case 1: // Insert
			robotRun.nextScreen(ScreenMode.CONFIRM_INSERT);
			break;
		case 2: // Delete
			Program prog = robotRun.getActiveProg();
			
			if (prog.getNumOfInst() > 0) {	
				robotRun.nextScreen(ScreenMode.SELECT_INSTR_DELETE);
				
			} else {
				System.err.println("No instructions to delete");
				robotRun.lastScreen();
			}
			break;
		case 3: // Cut/Copy
			prog = robotRun.getActiveProg();
			
			if (prog.getNumOfInst() > 0) {	
				robotRun.nextScreen(ScreenMode.SELECT_CUT_COPY);
				
			} else {
				System.err.println("No instructions to cut or copy");
				robotRun.lastScreen();
			}
			
			break;
		case 4: // Paste
			if (robotRun.clipBoard.size() > 0) {
				robotRun.nextScreen(ScreenMode.SELECT_PASTE_OPT);
				
			} else {
				System.err.println("No instructions to paste");
				robotRun.lastScreen();
			}
			
			break;
		case 5: // Find/Replace
			prog = robotRun.getActiveProg();
			
			if (prog.getNumOfInst() > 0) {	
				robotRun.nextScreen(ScreenMode.FIND_REPL);
				
			} else {
				System.err.println("Nothing to find");
				robotRun.lastScreen();
			}
			
			break;
		case 6: // Renumber
			robotRun.nextScreen(ScreenMode.CONFIRM_RENUM);
			break;
		case 7: // Comment
			prog = robotRun.getActiveProg();
			
			if (prog.getNumOfInst() > 0) {	
				robotRun.nextScreen(ScreenMode.SELECT_COMMENT);
				
			} else {
				System.err.println("No instructions to cut or copy");
				robotRun.lastScreen();
			}
			
			break;
		}
	}
}

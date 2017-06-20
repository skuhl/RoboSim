package screen.opt_menu;

import core.RobotRun;
import enums.ScreenMode;

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
			contents.resetSelection(robotRun.getActiveProg().getNumOfInst());
			robotRun.nextScreen(ScreenMode.SELECT_INSTR_DELETE);
			break;
		case 3: // Cut/Copy
			contents.resetSelection(robotRun.getActiveProg().getNumOfInst());
			robotRun.nextScreen(ScreenMode.SELECT_CUT_COPY);
			break;
		case 4: // Paste
			robotRun.nextScreen(ScreenMode.SELECT_PASTE_OPT);
			break;
		case 5: // Find/Replace
			robotRun.nextScreen(ScreenMode.FIND_REPL);
			break;
		case 6: // Renumber
			robotRun.nextScreen(ScreenMode.CONFIRM_RENUM);
			break;
		case 7: // Comment
			contents.resetSelection(robotRun.getActiveProg().getNumOfInst());
			robotRun.nextScreen(ScreenMode.SELECT_COMMENT);
			break;
		case 8: // Remark
		}
	}
}

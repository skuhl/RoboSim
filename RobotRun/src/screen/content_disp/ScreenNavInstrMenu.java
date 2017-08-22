package screen.content_disp;

import core.RobotRun;
import screen.ScreenMode;

public class ScreenNavInstrMenu extends ST_ScreenListContents {

	public ScreenNavInstrMenu(RobotRun r) {
		super(ScreenMode.NAV_INSTR_MENU, r);
	}
	
	@Override
	public void actionEntr() {
		switch (contents.getLineIdx()) {
		case 0: // Undo
			robotRun.getActiveRobot().undoProgramEdit();
			robotRun.lastScreen();
			break;
		case 1: // Insert
			robotRun.nextScreen(ScreenMode.CONFIRM_INSERT);
			break;
		case 2: // Delete
			robotRun.nextScreen(ScreenMode.SELECT_INSTR_DELETE);
			break;
		case 3: // Cut/Copy
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
			robotRun.nextScreen(ScreenMode.SELECT_COMMENT);
			break;
		case 8: // Remark
		}
	}
	
	@Override
	protected void loadContents() {
		contents.addLine("1 Undo");
		contents.addLine("2 Insert");
		contents.addLine("3 Delete");
		contents.addLine("4 Cut/ Copy");
		contents.addLine("5 Paste");
		contents.addLine("6 Find/ Replace");
		contents.addLine("7 Renumber");
		contents.addLine("8 Comment");
	}
	
	@Override
	protected String loadHeader() {
		return robotRun.getActiveProg().getName();
	}
}

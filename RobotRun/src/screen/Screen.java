package screen;

import core.RobotRun;
import enums.ScreenMode;
import ui.MenuScroll;

public abstract class Screen {
	public final ScreenMode mode;
	protected final RobotRun robotRun;
	
	protected final String header;
	protected MenuScroll contents;
	protected MenuScroll options;
	protected String[] labels;
	
	public static boolean useScreen = false;
	
	public static Screen getScreen(ScreenMode m, RobotRun r) {
		if(!useScreen) {
			return null;
		}
		
		switch(m) {
		case DEFAULT: return new ScreenDefault(r);

		/*
		 * Set of screens used to manipulate instruction parameters with a finite number of states
		 */
		case SET_BOOL_CONST: return new ScreenSetBoolConst(r);
		case SET_FRM_INSTR_TYPE: return new ScreenSetFrameInstrType(r);
		case SET_IF_STMT_ACT: return new ScreenSetIfStmtAction(r);
		case SET_IO_INSTR_STATE: return new ScreenSetIOInstrState(r);
		case SET_MINST_TYPE: return new ScreenSetMotionInstrType(r);
		case SET_MINST_REG_TYPE: return new ScreenSetMostionInstrRegType(r);
		case SET_MINST_CREG_TYPE: return new ScreenSetMotionInstrCircRegType(r);
		case SET_MINST_OFF_TYPE: return new ScreenSetMotionInstrOffsetType(r);
		case SET_REG_EXPR_TYPE: return new ScreenSetRegExpressionType(r);
		case SET_SELECT_STMT_ARG: return new ScreenSelectStmtArg(r);
		case SET_SELECT_STMT_ACT: return new ScreenSelectStmtAction(r);

		/*
		 * Set of screens used to edit expression elements
		 */
		case SET_BOOL_EXPR_ARG: return new ScreenSetBoolExpressionArg(r);
		case SET_EXPR_ARG: return new ScreenSetExpressionArg(r);
		case SET_EXPR_OP: return new ScreenSetExpressionOp(r);

		/*
		 * Screens used to display a several list of contents for the user to
		 * examine and interact with
		 */
		case NAV_MAIN_MENU: return new ScreenMainMenu(r);
		case NAV_MACROS: return new ScreenNavMacros(r);
		case NAV_MF_MACROS: return new ScreenNavMFMacros(r);
		case NAV_PROG_INSTR: return new ScreenProgInstructions(r);
		case NAV_TOOL_FRAMES: return new ScreenNavToolFrames(r);
		case NAV_USER_FRAMES: return new ScreenNavUserFrames(r);
		case NAV_PROGRAMS: return new ScreenProgs(r);
		case NAV_DATA: return new ScreenNavData(r);
		case NAV_PREGS: return new ScreenNavPosRegs(r);
		case NAV_DREGS: return new ScreenNavDataRegs(r);
		case NAV_IOREG: return new ScreenNavIORegs(r);
		case SET_MACRO_PROG: return new ScreenSetMacroProg(r);

		/*
		 * Screens used to perform arbitrary line-wise selection on a list of
		 * elements displayed on a screen of type 'TYPE_LIST_CONTENTS'
		 */
		case SELECT_COMMENT: return new ScreenSelectComment(r);
		case SELECT_CUT_COPY: return new ScreenSelectCutCopy(r);
		case SELECT_INSTR_DELETE: return new ScreenSelectInstrDelete(r);

		/*
		 * Screens used to confirm or cancel the execution of a selected function
		 */
		case CONFIRM_PROG_DELETE: return new ScreenConfirmProgramDelete(r);
		case CONFIRM_RENUM: return new ScreenConfirmRenumber(r);

		/*
		 * Screens used to display a context-based list of options to the user
		 */
		/*case NAV_INSTR_MENU: return new ScreenNavInstrMenu(r);
		case SELECT_COND_STMT: return new ScreenSelectContStmt(r);
		case SELECT_FRAME_INSTR_TYPE: return new ScreenSelectFrameInstrType(r);
		case SELECT_FRAME_MODE: return new ScreenSelectFrameMode(r);
		case SELECT_INSTR_INSERT: return new ScreenSelectInstrInsert(r);
		case SELECT_IO_INSTR_REG: return new ScreenSelectIOInstrReg(r);
		case SELECT_JMP_LBL: return new ScreenSelectJumpLabel(r);
		case SELECT_PASTE_OPT: return new ScreenSelectPasteOpt(r);
		case SELECT_REG_STMT: return new ScreenSelectRegStmt(r);
		case SET_MACRO_TYPE: return new ScreenSetMacroType(r);
		case SET_MACRO_BINDING: return new ScreenSetMacroBinding(r);
		case SET_CALL_PROG: return new ScreenSetCallProg(r);
		case SET_MINST_WO: return new ScreenSetMotionInstrObj(r);
		case SWAP_PT_TYPE: return new ScreenSwapPointType(r);
		case UFRAME_DETAIL: return new ScreenUserFrameDetail(r);
		case TFRAME_DETAIL: return new ScreenToolFrameDetail(r);
		case SET_DEF_TOOLTIP: return new ScreenSetDefaultTooltip(r);

		/*
		 * Screens involving the entry of text, either via keyboard input or function buttons
		 */
		case FIND_REPL: return new ScreenFindReplace(r);
		case EDIT_DREG_COM: return new ScreenEditDataRegComment(r);
		case EDIT_PREG_COM: return new ScreenEditPosRegComment(r);
		case PROG_COPY: return new ScreenProgramCopy(r);
		case PROG_CREATE: return new ScrenProgramCreate(r);
		case PROG_RENAME: return new ScreenProgramRename(r);
		case UFRAME_RENAME: return new ScreenUserFrameRename(r);
		case TFRAME_RENAME: return new ScreenToolFrameRename(r);

		/*
		 * Screens involving the entry of numeric values via either a physical numpad or
		 * the virtual numpad included in the simulator UI
		 */
		/*case ACTIVE_FRAMES: return new ScreenShowActiveFrames(r);
		case CONFIRM_INSERT: return new ScreenConfirmInsert(r);
		case EDIT_DREG_VAL: return new ScreenEditDataRegValue(r);
		case INPUT_DREG_IDX: return new ScreenInputDataRegIdx(r);
		case INPUT_IOREG_IDX: return new ScreenInputIORegIdx(r);
		case INPUT_PREG_IDX1: return new ScreenInputPosRegIdx();
		case INPUT_PREG_IDX2: return new ScreenInputPosRegSubIdx(r);
		case INPUT_CONST: return new ScreenInputConst(r);
		case JUMP_TO_LINE: return new ScreenJumpToLine(r);
		case SET_FRAME_INSTR_IDX: return new ScreenSetFramInstrIdx(r);
		case SET_IO_INSTR_IDX: return new ScreenSetIOInstrIdx(r);
		case SET_JUMP_TGT: return new ScreenSetJumpTgt(r);
		case SET_LBL_NUM: return new ScreenSetLabelNum(r);
		case SET_REG_EXPR_IDX1: return new ScreenSetRegExprIdx1(r);
		case SET_REG_EXPR_IDX2: return new ScreenSetRegExprIdx2(r);
		case SET_SELECT_ARGVAL: return new ScreenSetSelectArgValue(r);
		case SET_MINST_IDX: return new ScreenSetMotionInstrIdx(r);
		case SET_MINST_CIDX: return new ScreenSetMotionInstrCIdx(r);
		case SET_MINST_OFFIDX: return new ScreenSetMotionInstrOffsetIdx(r);
		case SET_MINST_SPD: return new ScreenSetMotionInstrSpeed(r);
		case SET_MINST_TERM: return new ScreenSetMotionInstrTerm(r);
		case CP_DREG_COM: return new ScreenCopyDataRegComment(r);
		case CP_DREG_VAL: return new ScreenCopyDataRegValue(r);
		case CP_PREG_COM: return new ScreenCopyPosRegComment(r);
		case CP_PREG_PT: return new ScreenCopyPosRegPoint(r);

		/*
		 * Frame input methods
		 */
		case TEACH_3PT_TOOL: return new ScreenTeach3PtTool(r);
		case TEACH_3PT_USER: return new ScreenTeach3PtUser(r);
		case TEACH_4PT: return new ScreenTeach4Pt(r);
		case TEACH_6PT: return new ScreenTeach6Pt(r);

		/*
		 * Screens involving direct entry of point values
		 */
		case DIRECT_ENTRY_TOOL: return new ScreenDirectEntryTool(r);
		case DIRECT_ENTRY_USER: return new ScreenDirectEntryUser(r);
		case EDIT_PREG: return new ScreenEditPosReg(r);
		case EDIT_PROG_POS: return new ScreenEditProgramPos(r);
		
		default: return null;
		}
	}
	
	public Screen(ScreenMode m, RobotRun r) {
		mode = m;
		robotRun = r;
		header = loadHeader();
		contents = new MenuScroll("cont", 8, 10, 20);
		options = new MenuScroll("opt", 3, 10, 180);
		labels = new String[5];
		
		updateScreen();
	}
	
	public void updateScreen() {
		contents.clear();
		options.clear();
		
		loadContents();
		loadOptions();
		loadLabels();
		loadScreenIdx();
		loadVars();
	}
	
	//Used for displaying screen text
	public String getHeader() { return header; }
	public MenuScroll getContents() { return contents; }
	public MenuScroll getOptions() { return options; }
	public String[] getLabels() { return labels; }
	
	public int getContentIdx() { return contents.getLineIdx(); }
	public int getContentColIdx() { return contents.getColumnIdx(); }
	public int getContentStart() { return contents.getRenderStart(); }

	public int getOptionIdx() { return options.getLineIdx(); }
	public int getOptionStart() { return options.getRenderStart(); }
	
	//Loads default screen state variables
	public void loadScreenIdx() {
		loadScreenIdx(0, 0, 0, 0, 0);
	}
	
	//Loads given set of screen state variables
	public void loadScreenIdx(int contLine, int col, int contRS, int optLine, int optRS) {
		contents.setLineIdx(contLine);
		contents.setColumnIdx(col);
		contents.setRenderStart(contRS);
		
		options.setLineIdx(optLine);
		options.setRenderStart(optRS);
	}
	
	//Sets text for each screen
	abstract String loadHeader();
	abstract void loadContents();
	abstract void loadOptions();
	abstract void loadLabels();
	abstract void loadVars();
		
	//Button actions
	public abstract void actionUp();
	public abstract void actionDn();
	public abstract void actionLt();
	public abstract void actionRt();
	public abstract void actionEntr();
	public abstract void actionF1();
	public abstract void actionF2();
	public abstract void actionF3();
	public abstract void actionF4();
	public abstract void actionF5();
}

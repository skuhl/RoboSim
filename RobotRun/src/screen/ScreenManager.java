package screen;

import java.util.Stack;

import core.RobotRun;
import global.Fields;
import screen.cnfrm_cncl.ScreenConfirmProgramDelete;
import screen.cnfrm_cncl.ScreenConfirmRenumber;
import screen.content_disp.ScreenCreateMacro;
import screen.content_disp.ScreenNavData;
import screen.content_disp.ScreenNavDataRegs;
import screen.content_disp.ScreenNavIORegs;
import screen.content_disp.ScreenNavMFMacros;
import screen.content_disp.ScreenNavMacros;
import screen.content_disp.ScreenNavMainMenu;
import screen.content_disp.ScreenNavPosRegs;
import screen.content_disp.ScreenNavProgInstructions;
import screen.content_disp.ScreenNavPrograms;
import screen.content_disp.ScreenNavToolFrames;
import screen.content_disp.ScreenNavUserFrames;
import screen.content_disp.ScreenSetMacroProg;
import screen.edit_item.ScreenSelectPasteOpt;
import screen.edit_item.ScreenSetBoolConst;
import screen.edit_item.ScreenSetBoolExpressionArg;
import screen.edit_item.ScreenSetExpressionArg;
import screen.edit_item.ScreenSetExpressionOp;
import screen.edit_item.ScreenSetFrameInstrType;
import screen.edit_item.ScreenSetIOInstrState;
import screen.edit_item.ScreenSetIfStmtAction;
import screen.edit_item.ScreenSetMacroBinding;
import screen.edit_item.ScreenSetMacroType;
import screen.edit_item.ScreenSetMostionInstrRegType;
import screen.edit_item.ScreenSetMotionInstrCircRegType;
import screen.edit_item.ScreenSetMotionInstrObj;
import screen.edit_item.ScreenSetMotionInstrOffsetType;
import screen.edit_item.ScreenSetMotionInstrType;
import screen.edit_item.ScreenSetObjectOperandTgt;
import screen.edit_item.ScreenSetRegExpressionType;
import screen.edit_item.ScreenSetSelectStmtAction;
import screen.edit_item.ScreenSetSelectStmtArg;
import screen.edit_point.ScreenDirectEntryTool;
import screen.edit_point.ScreenDirectEntryUser;
import screen.edit_point.ScreenEditPosReg;
import screen.edit_point.ScreenEditProgramPos;
import screen.num_entry.ScreenConfirmInsert;
import screen.num_entry.ScreenCopyDataRegComment;
import screen.num_entry.ScreenCopyDataRegValue;
import screen.num_entry.ScreenCopyPosRegComment;
import screen.num_entry.ScreenCopyPosRegPoint;
import screen.num_entry.ScreenEditDataRegValue;
import screen.num_entry.ScreenInputConst;
import screen.num_entry.ScreenInputDataRegIdx;
import screen.num_entry.ScreenInputIORegIdx;
import screen.num_entry.ScreenInputPosRegIdx;
import screen.num_entry.ScreenInputPosRegSubIdx;
import screen.num_entry.ScreenJumpToLine;
import screen.num_entry.ScreenSetFramInstrIdx;
import screen.num_entry.ScreenSetIOInstrIdx;
import screen.num_entry.ScreenSetJumpTgt;
import screen.num_entry.ScreenSetLabelNum;
import screen.num_entry.ScreenSetMotionInstrCIdx;
import screen.num_entry.ScreenSetMotionInstrIdx;
import screen.num_entry.ScreenSetMotionInstrOffsetIdx;
import screen.num_entry.ScreenSetMotionInstrSpeed;
import screen.num_entry.ScreenSetMotionInstrTerm;
import screen.num_entry.ScreenSetRegExprIdx1;
import screen.num_entry.ScreenSetRegExprIdx2;
import screen.num_entry.ScreenSetSelectArgValue;
import screen.num_entry.ScreenShowActiveFrames;
import screen.opt_menu.ScreenNavInstrMenu;
import screen.opt_menu.ScreenSelectContStmt;
import screen.opt_menu.ScreenSelectFrameInstrType;
import screen.opt_menu.ScreenSelectFrameMode;
import screen.opt_menu.ScreenSelectIOInstrReg;
import screen.opt_menu.ScreenSelectInstrInsert;
import screen.opt_menu.ScreenSelectJumpLabel;
import screen.opt_menu.ScreenSelectRegStmt;
import screen.opt_menu.ScreenSetCallProg;
import screen.opt_menu.ScreenSetDefaultTooltip;
import screen.opt_menu.ScreenToolFrameDetail;
import screen.opt_menu.ScreenUserFrameDetail;
import screen.select_lines.ScreenSelectComment;
import screen.select_lines.ScreenSelectCutCopy;
import screen.select_lines.ScreenSelectInstrDelete;
import screen.teach_frame.ScreenTeach3PtTool;
import screen.teach_frame.ScreenTeach3PtUser;
import screen.teach_frame.ScreenTeach4Pt;
import screen.teach_frame.ScreenTeach6Pt;
import screen.text_entry.ScreenEditDataRegComment;
import screen.text_entry.ScreenEditPosRegComment;
import screen.text_entry.ScreenFindReplace;
import screen.text_entry.ScreenProgramCopy;
import screen.text_entry.ScreenProgramCreate;
import screen.text_entry.ScreenProgramRename;
import screen.text_entry.ScreenToolFrameRename;
import screen.text_entry.ScreenUserFrameRename;
import ui.MenuScroll;

/**
 * Defines the manager for the pendant's active screen and previous screen
 * stack.
 * 
 * @author Joshua Hooker
 */
public class ScreenManager {
	
	/**
	 * A reference to the RobotRun application
	 */
	private final RobotRun robotRun;

	/**
	 * The set of previous screens with reference to the active screen.
	 */
	private final Stack<Screen> screenStack;
	
	/**
	 * The active pendant screen.
	 */
	private Screen activeScreen;
	
	/**
	 * Initializes the screen stack and the active screen as the default screen.
	 * 
	 * @param app	A reference to the RobotRun application
	 */
	public ScreenManager(RobotRun app) {
		robotRun = app;
		screenStack = new Stack<>();
		activeScreen = loadScreen(ScreenMode.DEFAULT);
	}
	
	/**
	 * @return	The active screen
	 */
	public Screen getActiveScreen() {
		return activeScreen;
	}
	
	public Screen getPrevScreen() {
		return screenStack.peek();
	}
	
	public ScreenMode getPrevMode() {		
		return screenStack.peek().mode;
	}
	
	/**
	 * @return	The maximum depth of all previous screens with reference to the active
	 * 			screen
	 */
	public int getScreenStackSize() {
		return screenStack.size();
	}
	
	/**
	 * Creates and initializes the screen with the given screen mode.
	 * 
	 * @param mode	The mode of the screen to load
	 * @return				The screen with the specified mode
	 */
	private Screen loadScreen(ScreenMode mode) {
		Screen prevScreen = null;
		
		if (mode == ScreenMode.DEFAULT) {
			prevScreen = null;
			
		// Give the previous program navigation screen to the option screens
		} else if (mode == ScreenMode.CONFIRM_INSERT || mode == ScreenMode.SELECT_INSTR_DELETE
			|| mode == ScreenMode.CONFIRM_RENUM || mode == ScreenMode.SELECT_COMMENT
			|| mode == ScreenMode.SELECT_CUT_COPY || mode == ScreenMode.FIND_REPL
			|| (mode == ScreenMode.SELECT_PASTE_OPT &&
			getPrevMode() != ScreenMode.SELECT_CUT_COPY)) {
			
			if (screenStack.size() > 2) {
				// Find the program navigation screen
				prevScreen = screenStack.get( screenStack.size() - 2 );
				
				if (prevScreen.mode != ScreenMode.NAV_PROG_INSTR) {
					prevScreen = null;
				}
			}
			
		} else {
			prevScreen = screenStack.peek();
		}
		
		Screen screen = initScreen(mode, prevScreen);
		screen.updateScreen();
		
		if (prevScreen != null) {
			// It is possible for the previous screen to be null
			screen.loadVars(prevScreen.getScreenState());
		}
		
		return screen;
	}
	
	/**
	 * Creates and initializes the screen with the given mode based on the
	 * given previous screen.
	 * 
	 * @param mode			The mode of the screen to initialize
	 * @param prevScreen	A previously active screen
	 * @return				The screen with specified mode
	 */
	private Screen initScreen(ScreenMode screenType, Screen prevScreen) {
		ScreenState prevState;
		Screen nextScreen;
		
		if(prevScreen == null) {
			prevState = new ScreenState(null, 0, 0, 0, 0, 0);
			
		} else {
			prevState = prevScreen.getScreenState();
		}
		
		switch(screenType) {
		case DEFAULT: nextScreen = new ScreenDefault(robotRun); break;

		/*
		 * Set of screens used to manipulate instruction parameters with a finite number of states
		 */
		case SET_BOOL_CONST: nextScreen = new ScreenSetBoolConst(robotRun); break;
		case SET_BOOL_EXPR_ARG: nextScreen = new ScreenSetBoolExpressionArg(robotRun); break;
		case SET_EXPR_ARG: nextScreen = new ScreenSetExpressionArg(robotRun); break;
		case SET_EXPR_OP: nextScreen = new ScreenSetExpressionOp(robotRun); break;
		case SET_FRAME_INSTR_TYPE: nextScreen = new ScreenSetFrameInstrType(robotRun); break;
		case SET_IF_STMT_ACT: nextScreen = new ScreenSetIfStmtAction(robotRun); break;
		case SET_IO_INSTR_STATE: nextScreen = new ScreenSetIOInstrState(robotRun); break;
		case SET_MINST_REG_TYPE: nextScreen = new ScreenSetMostionInstrRegType(robotRun); break;
		case SET_MINST_CREG_TYPE: nextScreen = new ScreenSetMotionInstrCircRegType(robotRun); break;
		case SET_MINST_OBJ: nextScreen = new ScreenSetMotionInstrObj(robotRun); break;
		case SET_MINST_OFF_TYPE: nextScreen = new ScreenSetMotionInstrOffsetType(robotRun); break;
		case SET_MINST_TYPE: nextScreen = new ScreenSetMotionInstrType(robotRun); break;
		case SET_REG_EXPR_TYPE: nextScreen = new ScreenSetRegExpressionType(robotRun); break;
		case SET_SELECT_STMT_ARG: nextScreen = new ScreenSetSelectStmtArg(robotRun); break;
		case SET_SELECT_STMT_ACT: nextScreen = new ScreenSetSelectStmtAction(robotRun); break;
		case SET_OBJ_OPERAND_TGT: nextScreen = new ScreenSetObjectOperandTgt(robotRun); break;

		/*
		 * Screens used to display a several list of contents for the user to
		 * examine and interact with
		 */
		case CREATE_MACRO: nextScreen = new ScreenCreateMacro(robotRun); break;
		case NAV_DATA: nextScreen = new ScreenNavData(robotRun); break;
		case NAV_DREGS: nextScreen = new ScreenNavDataRegs(robotRun); break;
		case NAV_IOREGS: nextScreen = new ScreenNavIORegs(robotRun); break;
		case NAV_MACROS: nextScreen = new ScreenNavMacros(robotRun); break;
		case NAV_MAIN_MENU: nextScreen = new ScreenNavMainMenu(robotRun); break;
		case NAV_MF_MACROS: nextScreen = new ScreenNavMFMacros(robotRun); break;
		case NAV_PREGS: nextScreen = new ScreenNavPosRegs(robotRun); break;
		case NAV_PROG_INSTR: nextScreen = new ScreenNavProgInstructions(robotRun); break;
		case NAV_PROGRAMS: nextScreen = new ScreenNavPrograms(robotRun); break;
		case NAV_TOOL_FRAMES: nextScreen = new ScreenNavToolFrames(robotRun); break;
		case NAV_USER_FRAMES: nextScreen = new ScreenNavUserFrames(robotRun); break;
		case SET_MACRO_PROG: nextScreen = new ScreenSetMacroProg(robotRun); break;
		
		/*
		 * Screens used to perform arbitrary line-wise selection on a list of
		 * elements displayed on a screen of type 'TYPE_LIST_CONTENTS'
		 */
		case SELECT_COMMENT: nextScreen = new ScreenSelectComment(robotRun); break;
		case SELECT_CUT_COPY: nextScreen = new ScreenSelectCutCopy(robotRun); break;
		case SELECT_INSTR_DELETE: nextScreen = new ScreenSelectInstrDelete(robotRun); break;

		/*
		 * Screens used to confirm or cancel the execution of a selected function
		 */
		case CONFIRM_PROG_DELETE: nextScreen = new ScreenConfirmProgramDelete(robotRun); break;
		case CONFIRM_RENUM: nextScreen = new ScreenConfirmRenumber(robotRun); break;

		/*
		 * Screens used to display a context-based list of options to the user
		 */
		case NAV_INSTR_MENU: nextScreen = new ScreenNavInstrMenu(robotRun); break;
		case SELECT_COND_STMT: nextScreen = new ScreenSelectContStmt(robotRun); break;
		case SELECT_FRAME_INSTR_TYPE: nextScreen = new ScreenSelectFrameInstrType(robotRun); break;
		case SELECT_FRAME_MODE: nextScreen = new ScreenSelectFrameMode(robotRun); break;
		case SELECT_INSTR_INSERT: nextScreen = new ScreenSelectInstrInsert(robotRun); break;
		case SELECT_IO_INSTR_REG: nextScreen = new ScreenSelectIOInstrReg(robotRun); break;
		case SELECT_JMP_LBL: nextScreen = new ScreenSelectJumpLabel(robotRun); break;
		case SELECT_PASTE_OPT: nextScreen = new ScreenSelectPasteOpt(robotRun); break;
		case SELECT_REG_STMT: nextScreen = new ScreenSelectRegStmt(robotRun); break;
		case SET_CALL_PROG: nextScreen = new ScreenSetCallProg(robotRun); break;
		case SET_DEF_TOOLTIP:
			int frameIdx = ((ScreenToolFrameDetail)prevScreen).getFrameIdx();
			nextScreen = new ScreenSetDefaultTooltip(robotRun, frameIdx);
			break;
			
		case SET_MACRO_BINDING: nextScreen = new ScreenSetMacroBinding(robotRun); break;
		case SET_MACRO_TYPE: nextScreen = new ScreenSetMacroType(robotRun); break;
		case TFRAME_DETAIL:
			MenuScroll prevContents = prevScreen.getContents();
			frameIdx = prevContents.getCurrentItemIdx();
			nextScreen = new ScreenToolFrameDetail(robotRun, frameIdx);
			break;
			
		case UFRAME_DETAIL:
			prevContents = prevScreen.getContents();
			frameIdx = prevContents.getCurrentItemIdx();
			nextScreen = new ScreenUserFrameDetail(robotRun, frameIdx);
			break;
		
		/*
		 * Screens involving the entry of text, either via keyboard input or function buttons
		 */
		case EDIT_DREG_COM: nextScreen = new ScreenEditDataRegComment(robotRun); break;
		case EDIT_PREG_COM: nextScreen = new ScreenEditPosRegComment(robotRun); break;
		case FIND_REPL: nextScreen = new ScreenFindReplace(robotRun); break;
		case PROG_COPY: nextScreen = new ScreenProgramCopy(robotRun); break;
		case PROG_CREATE: nextScreen = new ScreenProgramCreate(robotRun); break;
		case PROG_RENAME: nextScreen = new ScreenProgramRename(robotRun); break;
		case TFRAME_RENAME:
			frameIdx = ((ScreenToolFrameDetail)prevScreen).getFrameIdx();
			nextScreen = new ScreenToolFrameRename(robotRun, frameIdx);
			break;
			
		case UFRAME_RENAME:
			frameIdx = ((ScreenUserFrameDetail)prevScreen).getFrameIdx();
			nextScreen = new ScreenUserFrameRename(robotRun, frameIdx);
			break;

		/*
		 * Screens involving the entry of numeric values via either a physical numpad or
		 * the virtual numpad included in the simulator UI
		 */
		case ACTIVE_FRAMES: nextScreen = new ScreenShowActiveFrames(robotRun); break;
		case CONFIRM_INSERT: nextScreen = new ScreenConfirmInsert(robotRun); break;
		case EDIT_DREG_VAL: nextScreen = new ScreenEditDataRegValue(robotRun); break;
		case INPUT_DREG_IDX: nextScreen = new ScreenInputDataRegIdx(robotRun); break;
		case INPUT_IOREG_IDX: nextScreen = new ScreenInputIORegIdx(robotRun); break;
		case INPUT_PREG_IDX1: nextScreen = new ScreenInputPosRegIdx(robotRun); break;
		case INPUT_PREG_IDX2: nextScreen = new ScreenInputPosRegSubIdx(robotRun); break;
		case INPUT_CONST: nextScreen = new ScreenInputConst(robotRun); break;
		case JUMP_TO_LINE: nextScreen = new ScreenJumpToLine(robotRun); break;
		case SET_FRAME_INSTR_IDX: nextScreen = new ScreenSetFramInstrIdx(robotRun); break;
		case SET_IO_INSTR_IDX: nextScreen = new ScreenSetIOInstrIdx(robotRun); break;
		case SET_JUMP_TGT: nextScreen = new ScreenSetJumpTgt(robotRun); break;
		case SET_LBL_NUM: nextScreen = new ScreenSetLabelNum(robotRun); break;
		case SET_REG_EXPR_IDX1: nextScreen = new ScreenSetRegExprIdx1(robotRun); break;
		case SET_REG_EXPR_IDX2: nextScreen = new ScreenSetRegExprIdx2(robotRun); break;
		case SET_SELECT_ARGVAL: nextScreen = new ScreenSetSelectArgValue(robotRun); break;
		case SET_MINST_IDX: nextScreen = new ScreenSetMotionInstrIdx(robotRun); break;
		case SET_MINST_CIDX: nextScreen = new ScreenSetMotionInstrCIdx(robotRun); break;
		case SET_MINST_OFFIDX: nextScreen = new ScreenSetMotionInstrOffsetIdx(robotRun); break;
		case SET_MINST_SPD: nextScreen = new ScreenSetMotionInstrSpeed(robotRun); break;
		case SET_MINST_TERM: nextScreen = new ScreenSetMotionInstrTerm(robotRun); break;
		case CP_DREG_COM: nextScreen = new ScreenCopyDataRegComment(robotRun); break;
		case CP_DREG_VAL: nextScreen = new ScreenCopyDataRegValue(robotRun); break;
		case CP_PREG_COM: nextScreen = new ScreenCopyPosRegComment(robotRun); break;
		case CP_PREG_PT: nextScreen = new ScreenCopyPosRegPoint(robotRun); break;

		/*
		 * Frame input methods
		 */
		case TEACH_3PT_TOOL:
			frameIdx = ((ScreenToolFrameDetail)prevScreen).getFrameIdx();
			nextScreen = new ScreenTeach3PtTool(robotRun, frameIdx);
			break;
		
		case TEACH_3PT_USER:
			frameIdx = ((ScreenUserFrameDetail)prevScreen).getFrameIdx();
			nextScreen = new ScreenTeach3PtUser(robotRun, frameIdx);
			break;
		
		case TEACH_4PT:
			frameIdx = ((ScreenUserFrameDetail)prevScreen).getFrameIdx();
			nextScreen = new ScreenTeach4Pt(robotRun, frameIdx);
			break;
		
		case TEACH_6PT:
			frameIdx = ((ScreenToolFrameDetail)prevScreen).getFrameIdx();
			nextScreen = new ScreenTeach6Pt(robotRun, frameIdx);
			break;

		/*
		 * Screens involving direct entry of point values
		 */
		case DIRECT_ENTRY_TOOL:
			frameIdx = ((ScreenToolFrameDetail)prevScreen).getFrameIdx();
			nextScreen = new ScreenDirectEntryTool(robotRun, frameIdx);
			break;
			
		case DIRECT_ENTRY_USER:
			frameIdx = ((ScreenUserFrameDetail)prevScreen).getFrameIdx();
			nextScreen = new ScreenDirectEntryUser(robotRun, frameIdx);
			break;
			
		case EDIT_PREG: nextScreen = new ScreenEditPosReg(robotRun); break;
		case EDIT_PROG_POS: nextScreen = new ScreenEditProgramPos(robotRun); break;
		default: return null;
		}
		
		nextScreen.updateScreen(prevState);
		return nextScreen;
	}
	
	/**
	 * Trashes the active screen and sets the last active screen back as the
	 * active screen.
	 */
	public void lastScreen() {
		if (!screenStack.isEmpty()) {
			activeScreen = screenStack.pop();
		}
	}
	
	/**
	 * Pushes the active screen onto the screen stack, loads the screen with the
	 * specified mode and sets it as active.
	 * 
	 * @param mode	The mode of the next active screen
	 */
	public void nextScreen(ScreenMode mode) {
		screenStack.push(activeScreen);
		activeScreen = loadScreen(mode);
	}
	
	public void resetStack() {
		screenStack.clear();
		activeScreen = loadScreen(ScreenMode.DEFAULT);
	}
	
	/**
	 * Creates the screen with the specified mode and sets the screen as active
	 * without saving the last active screen onto the stack.
	 * 
	 * @param mode	The mode of the next active screen
	 */
	public void switchScreen(ScreenMode mode) {
		nextScreen(mode);
		// Remove the last screen from the screen stack
		screenStack.pop();
	}
	
	@Override
	public String toString() {
		return String.format("Active: %s\nStack: %s\n", activeScreen, screenStack);
	}
}

package screen;

import java.util.Stack;

import core.RobotRun;
import programming.Program;
import regs.DataRegister;
import regs.PositionRegister;
import robot.RoboticArm;
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
import screen.opt_menu.ScreenSelectPasteOpt;
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

/**
 * TODO comment this
 * 
 * @author Joshua Hooker
 */
public class ScreenManager {
	
	private final RobotRun APP;
	private final Stack<Screen> screenStack;
	private Screen activeScreen;
	
	/**
	 * Initializes the screen stack and the active screen as the default screen.
	 * 
	 * @param app	A reference to the RobotRun class
	 */
	public ScreenManager(RobotRun app) {
		APP = app;
		screenStack = new Stack<>();
		activeScreen = loadScreen(ScreenMode.DEFAULT);
	}
	
	public Screen getActiveScreen() {
		return activeScreen;
	}
	
	public Screen getPrevScreen(int depth) {
		if (depth > 0 && depth <= screenStack.size()) {
			return screenStack.get(screenStack.size() - depth);
		}
		
		return null;
	}
	
	public ScreenMode getPrevMode(int depth) {
		Screen prev = getPrevScreen(depth);
		
		if (prev != null) {
			return prev.mode;
		}
		
		return null;
	}
	
	public int getScreenStackSize() {
		return screenStack.size();
	}
	
	private Screen loadScreen(ScreenMode screenType) {
		Screen prevScreen = null;
		
		if (screenType == ScreenMode.DEFAULT) {
			prevScreen = null;
			
		// Give the previous program navigation screen to the option screens	
		} else if (screenType == ScreenMode.CONFIRM_INSERT || screenType == ScreenMode.SELECT_INSTR_DELETE
			|| screenType == ScreenMode.CONFIRM_RENUM || screenType == ScreenMode.SELECT_COMMENT
			|| screenType == ScreenMode.SELECT_CUT_COPY || screenType == ScreenMode.FIND_REPL
			|| (screenType == ScreenMode.SELECT_PASTE_OPT &&
			getPrevMode(1) != ScreenMode.SELECT_CUT_COPY)) {
			
			if (screenStack.size() > 2) {
				// Find the program navigation screen
				prevScreen = screenStack.get( screenStack.size() - 2 );
				
				if (prevScreen.mode != ScreenMode.NAV_PROG_INSTR) {
					prevScreen = null;
				}
			}
			
		} else {
			prevScreen = getPrevScreen(1);
		}
		
		Screen screen = initScreen(screenType, prevScreen);
		screen.updateScreen();
		
		Screen.printScreenInfo(screen);
		
		return screen;
	}
	
	private Screen initScreen(ScreenMode screenType, Screen prevScreen) {
		ScreenState prevState;
		
		if (prevScreen == null) {
			prevState = null;
			
		} else {
			prevState = prevScreen.getScreenState();
		}
		
		switch(screenType) {
		case DEFAULT: return new ScreenDefault(prevState, APP);

		/*
		 * Set of screens used to manipulate instruction parameters with a finite number of states
		 */
		case SET_BOOL_CONST: return new ScreenSetBoolConst(prevState, APP);
		case SET_BOOL_EXPR_ARG: return new ScreenSetBoolExpressionArg(prevState, APP);
		case SET_EXPR_ARG: return new ScreenSetExpressionArg(prevState, APP);
		case SET_EXPR_OP: return new ScreenSetExpressionOp(prevState, APP);
		case SET_FRAME_INSTR_TYPE: return new ScreenSetFrameInstrType(prevState, APP);
		case SET_IF_STMT_ACT: return new ScreenSetIfStmtAction(prevState, APP);
		case SET_IO_INSTR_STATE: return new ScreenSetIOInstrState(prevState, APP);
		case SET_MINST_REG_TYPE: return new ScreenSetMostionInstrRegType(prevState, APP);
		case SET_MINST_CREG_TYPE: return new ScreenSetMotionInstrCircRegType(prevState, APP);
		case SET_MINST_OBJ: return new ScreenSetMotionInstrObj(prevState, APP);
		case SET_MINST_OFF_TYPE: return new ScreenSetMotionInstrOffsetType(prevState, APP);
		case SET_MINST_TYPE: return new ScreenSetMotionInstrType(prevState, APP);
		case SET_REG_EXPR_TYPE: return new ScreenSetRegExpressionType(prevState, APP);
		case SET_SELECT_STMT_ARG: return new ScreenSetSelectStmtArg(prevState, APP);
		case SET_SELECT_STMT_ACT: return new ScreenSetSelectStmtAction(prevState, APP);

		/*
		 * Screens used to display a several list of contents for the user to
		 * examine and interact with
		 */
		case CREATE_MACRO: return new ScreenCreateMacro(prevState, APP);
		case NAV_DATA: return new ScreenNavData(prevState, APP);
		case NAV_DREGS: return new ScreenNavDataRegs(prevState, APP);
		case NAV_IOREGS: return new ScreenNavIORegs(prevState, APP);
		case NAV_MACROS: return new ScreenNavMacros(prevState, APP);
		case NAV_MAIN_MENU: return new ScreenNavMainMenu(prevState, APP);
		case NAV_MF_MACROS: return new ScreenNavMFMacros(prevState, APP);
		case NAV_PREGS: return new ScreenNavPosRegs(prevState, APP);
		case NAV_PROG_INSTR: return new ScreenNavProgInstructions(prevState, APP);
		case NAV_PROGRAMS: return new ScreenNavPrograms(prevState, APP);
		case NAV_TOOL_FRAMES: return new ScreenNavToolFrames(prevState, APP);
		case NAV_USER_FRAMES: return new ScreenNavUserFrames(prevState, APP);
		case SET_MACRO_PROG: return new ScreenSetMacroProg(prevState, APP);
		
		/*
		 * Screens used to perform arbitrary line-wise selection on a list of
		 * elements displayed on a screen of type 'TYPE_LIST_CONTENTS'
		 */
		case SELECT_COMMENT: 
			Program prog = APP.getActiveProg();
			
			return new ScreenSelectComment(prevState, prog.getNumOfInst(), APP);
		case SELECT_CUT_COPY:
			prog = APP.getActiveProg();
			
			return new ScreenSelectCutCopy(prevState, prog.getNumOfInst(), APP);
		case SELECT_INSTR_DELETE:
			prog = APP.getActiveProg();
			
			return new ScreenSelectInstrDelete(prevState, prog.getNumOfInst(), APP);

		/*
		 * Screens used to confirm or cancel the execution of a selected function
		 */
		case CONFIRM_PROG_DELETE: return new ScreenConfirmProgramDelete(prevState, APP);
		case CONFIRM_RENUM: return new ScreenConfirmRenumber(prevState, APP);

		/*
		 * Screens used to display a context-based list of options to the user
		 */
		case NAV_INSTR_MENU: return new ScreenNavInstrMenu(prevState, APP);
		case SELECT_COND_STMT: return new ScreenSelectContStmt(prevState, APP);
		case SELECT_FRAME_INSTR_TYPE: return new ScreenSelectFrameInstrType(prevState, APP);
		case SELECT_FRAME_MODE: return new ScreenSelectFrameMode(prevState, APP);
		case SELECT_INSTR_INSERT: return new ScreenSelectInstrInsert(prevState, APP);
		case SELECT_IO_INSTR_REG: return new ScreenSelectIOInstrReg(prevState, APP);
		case SELECT_JMP_LBL: return new ScreenSelectJumpLabel(prevState, APP);
		case SELECT_PASTE_OPT: return new ScreenSelectPasteOpt(prevState, APP);
		case SELECT_REG_STMT: return new ScreenSelectRegStmt(prevState, APP);
		case SET_CALL_PROG: return new ScreenSetCallProg(prevState, APP);
		case SET_DEF_TOOLTIP: return new ScreenSetDefaultTooltip(prevState, APP);
		case SET_MACRO_BINDING: return new ScreenSetMacroBinding(prevState, APP);
		case SET_MACRO_TYPE: return new ScreenSetMacroType(prevState, APP);
		case TFRAME_DETAIL: return new ScreenToolFrameDetail(prevState, APP);
		case UFRAME_DETAIL: return new ScreenUserFrameDetail(prevState, APP);
		

		/*
		 * Screens involving the entry of text, either via keyboard input or function buttons
		 */
		case EDIT_DREG_COM: return new ScreenEditDataRegComment(prevState, APP);
		case EDIT_PREG_COM: return new ScreenEditPosRegComment(prevState, APP);
		case FIND_REPL: return new ScreenFindReplace(prevState, APP);
		case PROG_COPY:
			prog = null;
			
			if (prevScreen != null) {
				prog = APP.getActiveRobot().getProgram( prevScreen.getContentIdx() );
			}
			
			return new ScreenProgramCopy(prevState, APP, prog);
		case PROG_CREATE: return new ScreenProgramCreate(prevState, APP);
		case PROG_RENAME:
			prog = null;
			
			if (prevScreen != null) {
				prog= APP.getActiveRobot().getProgram( prevScreen.getContentIdx() );
			}
			
			return new ScreenProgramRename(prevState, APP, prog);
		case TFRAME_RENAME: return new ScreenToolFrameRename(prevState, APP);
		case UFRAME_RENAME: return new ScreenUserFrameRename(prevState, APP);

		/*
		 * Screens involving the entry of numeric values via either a physical numpad or
		 * the virtual numpad included in the simulator UI
		 */
		case ACTIVE_FRAMES: return new ScreenShowActiveFrames(prevState, APP);
		case CONFIRM_INSERT: return new ScreenConfirmInsert(prevState, APP);
		case EDIT_DREG_VAL: return new ScreenEditDataRegValue(prevState, APP);
		case INPUT_DREG_IDX: return new ScreenInputDataRegIdx(prevState, APP);
		case INPUT_IOREG_IDX: return new ScreenInputIORegIdx(prevState, APP);
		case INPUT_PREG_IDX1: return new ScreenInputPosRegIdx(prevState, APP);
		case INPUT_PREG_IDX2: return new ScreenInputPosRegSubIdx(prevState, APP);
		case INPUT_CONST: return new ScreenInputConst(prevState, APP);
		case JUMP_TO_LINE: return new ScreenJumpToLine(prevState, APP);
		case SET_FRAME_INSTR_IDX: return new ScreenSetFramInstrIdx(prevState, APP);
		case SET_IO_INSTR_IDX: return new ScreenSetIOInstrIdx(prevState, APP);
		case SET_JUMP_TGT: return new ScreenSetJumpTgt(prevState, APP);
		case SET_LBL_NUM: return new ScreenSetLabelNum(prevState, APP);
		case SET_REG_EXPR_IDX1: return new ScreenSetRegExprIdx1(prevState, APP);
		case SET_REG_EXPR_IDX2: return new ScreenSetRegExprIdx2(prevState, APP);
		case SET_SELECT_ARGVAL: return new ScreenSetSelectArgValue(prevState, APP);
		case SET_MINST_IDX: return new ScreenSetMotionInstrIdx(prevState, APP);
		case SET_MINST_CIDX: return new ScreenSetMotionInstrCIdx(prevState, APP);
		case SET_MINST_OFFIDX: return new ScreenSetMotionInstrOffsetIdx(prevState, APP);
		case SET_MINST_SPD: return new ScreenSetMotionInstrSpeed(prevState, APP);
		case SET_MINST_TERM: return new ScreenSetMotionInstrTerm(prevState, APP);
		case CP_DREG_COM:
			// The header requires some pre-processing
			RoboticArm r = APP.getActiveRobot();
			DataRegister dReg = r.getDReg( prevScreen.getContentIdx() );
			String header = String.format("%s: COMMENT COPY", dReg.getLabel());
			
			return new ScreenCopyDataRegComment(prevState, header, APP);
		case CP_DREG_VAL:
			// The header requires some pre-processing
			r = APP.getActiveRobot();
			dReg = r.getDReg( prevScreen.getContentIdx() );
			header = String.format("%s: VALUE COPY", dReg.getLabel());
			
			return new ScreenCopyDataRegValue(prevState, header, APP);
		case CP_PREG_COM:
			// The header requires some pre-processing
			r = APP.getActiveRobot();
			PositionRegister pReg = r.getPReg( prevScreen.getContentIdx() );
			header = String.format("%s: COMMENT COPY", pReg.getLabel());
			
			return new ScreenCopyPosRegComment(prevState, header, APP);
		
		case CP_PREG_PT:
			// The header requires some pre-processing
			r = APP.getActiveRobot();
			pReg = r.getPReg( prevScreen.getContentIdx() );
			header = String.format("%s: POINT COPY", pReg.getLabel());
			
			return new ScreenCopyPosRegPoint(prevState, header, APP);

		/*
		 * Frame input methods
		 */
		case TEACH_3PT_TOOL: return new ScreenTeach3PtTool(prevState, APP);
		case TEACH_3PT_USER: return new ScreenTeach3PtUser(prevState, APP);
		case TEACH_4PT: return new ScreenTeach4Pt(prevState, APP);
		case TEACH_6PT: return new ScreenTeach6Pt(prevState, APP);

		/*
		 * Screens involving direct entry of point values
		 */
		case DIRECT_ENTRY_TOOL: return new ScreenDirectEntryTool(prevState, APP);
		case DIRECT_ENTRY_USER: return new ScreenDirectEntryUser(prevState, APP);
		case EDIT_PREG: return new ScreenEditPosReg(prevState, APP);
		case EDIT_PROG_POS: return new ScreenEditProgramPos(prevState, APP);
		
		default: return null;
		}
	}
	
	public void lastScreen() {
		if (!screenStack.isEmpty()) {
			activeScreen = screenStack.pop();
		}
	}
	
	public void nextScreen(ScreenMode nextScreenType) {
		screenStack.push(activeScreen);
		activeScreen = loadScreen(nextScreenType);
	}
	
	public Screen[] popScreenStack(int depth) {
		Screen[] poppedScreens = new Screen[Math.min(depth, screenStack.size())];
		int idx = 0;
		
		while (!screenStack.isEmpty() && idx < depth) {
			poppedScreens[idx++] = screenStack.pop();
		}
		
		return poppedScreens;
	}
	
	public void resetStack() {
		screenStack.clear();
		activeScreen = loadScreen(ScreenMode.DEFAULT);
	}
	
	public void switchScreen(ScreenMode nextScreenType) {
		nextScreen(nextScreenType);
		// Remove the last screen from the screen stack
		screenStack.pop();
	}
	
	@Override
	public String toString() {
		return String.format("Active: %s\nStack: %s\n", activeScreen, screenStack);
	}
}

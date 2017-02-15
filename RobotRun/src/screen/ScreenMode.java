package screen;

import window.DisplayMode;

public enum ScreenMode implements DisplayMode {

	/* 
	 * The "Home" screen, default root screen state displayed on startup
	 */
	DEFAULT(ScreenType.TYPE_DEFAULT), 

	/*
	 * Set of screens used to manipulate instruction parameters with a finite number of states
	 */
	SET_BOOL_CONST(ScreenType.TYPE_INSTRUCT_EDIT),
	SET_FRM_INSTR_TYPE(ScreenType.TYPE_INSTRUCT_EDIT),
	SET_IF_STMT_ACT(ScreenType.TYPE_INSTRUCT_EDIT),
	SET_IO_INSTR_STATE(ScreenType.TYPE_INSTRUCT_EDIT),
	SET_MV_INSTR_TYPE(ScreenType.TYPE_INSTRUCT_EDIT),
	SET_MV_INSTR_REG_TYPE(ScreenType.TYPE_INSTRUCT_EDIT),
	SET_REG_EXPR_TYPE(ScreenType.TYPE_INSTRUCT_EDIT),
	SET_SELECT_STMT_ARG(ScreenType.TYPE_INSTRUCT_EDIT),
	SET_SELECT_STMT_ACT(ScreenType.TYPE_INSTRUCT_EDIT),

	/*
	 * Set of screens used to edit expression elements
	 */
	SET_BOOL_EXPR_ARG(ScreenType.TYPE_EXPR_EDIT),
	SET_EXPR_ARG(ScreenType.TYPE_EXPR_EDIT),
	SET_EXPR_OP(ScreenType.TYPE_EXPR_EDIT),

	/*
	 * Screens used to display a sereal list of contents for the user to
	 * examine and interact with
	 */
	NAV_MACROS(ScreenType.TYPE_LIST_CONTENTS),
	NAV_MF_MACROS(ScreenType.TYPE_LIST_CONTENTS),
	NAV_PROG_INSTR(ScreenType.TYPE_LIST_CONTENTS),
	NAV_TOOL_FRAMES(ScreenType.TYPE_LIST_CONTENTS),
	NAV_USER_FRAMES(ScreenType.TYPE_LIST_CONTENTS),
	NAV_PROGRAMS(ScreenType.TYPE_LIST_CONTENTS),
	//Cartesian
	NAV_PREGS_C(ScreenType.TYPE_LIST_CONTENTS),
	//Joint
	NAV_PREGS_J(ScreenType.TYPE_LIST_CONTENTS),
	NAV_DREGS(ScreenType.TYPE_LIST_CONTENTS),
	NAV_IOREG(ScreenType.TYPE_LIST_CONTENTS),
	SET_CALL_PROG(ScreenType.TYPE_LIST_CONTENTS),
	SET_CALL_TGT_DEVICE(ScreenType.TYPE_LIST_CONTENTS),
	SET_MACRO_PROG(ScreenType.TYPE_LIST_CONTENTS),

	/*
	 * Screens used to perform arbitrary line-wise selection on a list of
	 * elements displayed on a screen of type 'TYPE_LIST_CONTENTS'
	 */
	SELECT_COMMENT(ScreenType.TYPE_LINE_SELECT),
	SELECT_CUT_COPY(ScreenType.TYPE_LINE_SELECT),
	SELECT_INSTR_DELETE(ScreenType.TYPE_LINE_SELECT),

	/*
	 * Screens used to confirm or cancel the execution of a selected function
	 */
	CONFIRM_PROG_DELETE(ScreenType.TYPE_CONFIRM_CANCEL),
	CONFIRM_RENUM(ScreenType.TYPE_CONFIRM_CANCEL),
	CONFIRM_UNDO(ScreenType.TYPE_CONFIRM_CANCEL),

	/*
	 * Screens used to display a context-based list of options to the user
	 */
	FRAME_METHOD_TOOL(ScreenType.TYPE_OPT_MENU),
	FRAME_METHOD_USER(ScreenType.TYPE_OPT_MENU),
	NAV_INSTR_MENU(ScreenType.TYPE_OPT_MENU),
	NAV_MAIN_MENU(ScreenType.TYPE_OPT_MENU),
	EDIT_IOREG(ScreenType.TYPE_OPT_MENU),
	SELECT_COND_STMT(ScreenType.TYPE_OPT_MENU),
	SELECT_FRAME_INSTR_TYPE(ScreenType.TYPE_OPT_MENU),
	SELECT_FRAME_MODE(ScreenType.TYPE_OPT_MENU),
	SELECT_INSTR_INSERT(ScreenType.TYPE_OPT_MENU),
	SELECT_IO_INSTR_REG(ScreenType.TYPE_OPT_MENU),
	SELECT_JMP_LBL(ScreenType.TYPE_OPT_MENU),
	SELECT_PASTE_OPT(ScreenType.TYPE_OPT_MENU),
	SET_MACRO_TYPE(ScreenType.TYPE_OPT_MENU),
	SET_MACRO_BINDING(ScreenType.TYPE_OPT_MENU),

	/*
	 * Screens involving the entry of text, either via keyboard input or function buttons
	 */
	FIND_REPL(ScreenType.TYPE_TEXT_ENTRY),
	EDIT_DREG_COM(ScreenType.TYPE_TEXT_ENTRY),
	EDIT_PREG_COM(ScreenType.TYPE_TEXT_ENTRY),
	PROG_COPY(ScreenType.TYPE_TEXT_ENTRY),
	PROG_CREATE(ScreenType.TYPE_TEXT_ENTRY),
	PROG_RENAME(ScreenType.TYPE_TEXT_ENTRY),

	/*
	 * Screens involving the entry of numeric values via either a physical num pad or
	 * the virtual numpad included in the simulator UI
	 */
	ACTIVE_FRAMES(ScreenType.TYPE_NUM_ENTRY),
	CONFIRM_INSERT(ScreenType.TYPE_NUM_ENTRY),
	EDIT_DREG_VAL(ScreenType.TYPE_NUM_ENTRY),
	INPUT_DREG_IDX(ScreenType.TYPE_NUM_ENTRY),
	INPUT_IOREG_IDX(ScreenType.TYPE_NUM_ENTRY),
	INPUT_PREG_IDX1(ScreenType.TYPE_NUM_ENTRY),
	INPUT_PREG_IDX2(ScreenType.TYPE_NUM_ENTRY),
	INPUT_CONST(ScreenType.TYPE_NUM_ENTRY),
	JUMP_TO_LINE(ScreenType.TYPE_NUM_ENTRY),
	SET_FRAME_INSTR_IDX(ScreenType.TYPE_NUM_ENTRY),
	SET_IO_INSTR_IDX(ScreenType.TYPE_NUM_ENTRY),
	SET_JUMP_TGT(ScreenType.TYPE_NUM_ENTRY),
	SET_LBL_NUM(ScreenType.TYPE_NUM_ENTRY),
	SET_REG_EXPR_IDX1(ScreenType.TYPE_NUM_ENTRY),
	SET_REG_EXPR_IDX2(ScreenType.TYPE_NUM_ENTRY),
	SET_SELECT_ARGVAL(ScreenType.TYPE_NUM_ENTRY),
	SET_MV_INSTR_IDX(ScreenType.TYPE_NUM_ENTRY),
	SET_MV_INSTR_OFFSET(ScreenType.TYPE_NUM_ENTRY),
	SET_MV_INSTR_SPD(ScreenType.TYPE_NUM_ENTRY),
	SET_MV_INSTR_TERM(ScreenType.TYPE_NUM_ENTRY),
	CP_DREG_COM(ScreenType.TYPE_NUM_ENTRY),
	CP_DREG_VAL(ScreenType.TYPE_NUM_ENTRY),
	CP_PREG_COM(ScreenType.TYPE_NUM_ENTRY),
	CP_PREG_PT(ScreenType.TYPE_NUM_ENTRY),

	/*
	 * Frame input methods
	 */
	TEACH_3PT_TOOL(ScreenType.TYPE_TEACH_POINTS),
	TEACH_3PT_USER(ScreenType.TYPE_TEACH_POINTS),
	TEACH_4PT(ScreenType.TYPE_TEACH_POINTS),
	TEACH_6PT(ScreenType.TYPE_TEACH_POINTS),

	/*
	 * Screens involving direct entry of point values
	 */
	DIRECT_ENTRY_TOOL(ScreenType.TYPE_POINT_ENTRY),
	DIRECT_ENTRY_USER(ScreenType.TYPE_POINT_ENTRY),
	EDIT_PREG_C(ScreenType.TYPE_POINT_ENTRY),
	EDIT_PREG_J(ScreenType.TYPE_POINT_ENTRY),

	/*
	 * Miscelanious screens/ not otherwise categorized
	 */
	SWAP_PT_TYPE,
	EDIT_RSTMT,
	UFRAME_DETAIL,
	TFRAME_DETAIL,
	INPUT_CONSTANT,
	INPUT_OPERATOR,
	INPUT_PRDX,
	INPUT_PRVDX,
	INPUT_RDX,
	SELECT_REG_STMT,
	PICK_LETTER,
	NAV_DATA,
	VIEW_INST_REG;

	final ScreenType type;

	private ScreenMode(){
		type = null;
	}

	private ScreenMode(ScreenType t){
		type = t;
	}

	public ScreenType getType(){
		return type;
	} 
}
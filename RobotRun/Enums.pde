public interface DisplayMode {}

public static enum ScreenType implements DisplayMode {
  TYPE_DEFAULT,
  TYPE_OPT_MENU,
  TYPE_LINE_SELECT,
  TYPE_LIST_CONTENTS,
  TYPE_CONFIRM_CANCEL,
  TYPE_INSTRUCT_EDIT,
  TYPE_FRAME_EDIT,
  TYPE_TEXT_ENTRY,
  TYPE_NUM_ENTRY,
  TYPE_POINT_ENTRY;
}

public static enum Screen implements DisplayMode {
   
  /* 
  * The "Home" screen, default root screen state displayed on startup
  */
  DEFAULT(ScreenType.TYPE_DEFAULT), 
  
  /*
  * Set of screens used to manipulate instruction parameters with a finite number of states
  */
  SET_MV_INSTRUCT_TYPE(ScreenType.TYPE_INSTRUCT_EDIT),
  SET_MV_INSTRUCT_REG_TYPE(ScreenType.TYPE_INSTRUCT_EDIT),
  SET_FRAME_INSTRUCTION(ScreenType.TYPE_INSTRUCT_EDIT),
   
  /*
  * Screens used to display a sereal list of contents for the user to
  * examine and interact with
  */
  ACTIVE_FRAMES(ScreenType.TYPE_LIST_CONTENTS),
  INSTRUCTION_NAV(ScreenType.TYPE_LIST_CONTENTS),
  NAV_TOOL_FRAMES(ScreenType.TYPE_LIST_CONTENTS),
  NAV_USER_FRAMES(ScreenType.TYPE_LIST_CONTENTS),
  PROGRAM_NAV(ScreenType.TYPE_LIST_CONTENTS),
  
  /*
  * Screens used to perform arbitrary line-wise selection on a list of
  * elements displayed on a screen of type 'TYPE_LIST_CONTENTS'
  */
  SELECT_COMMENT(ScreenType.TYPE_LINE_SELECT),
  SELECT_CUT_COPY(ScreenType.TYPE_LINE_SELECT),
  SELECT_DELETE(ScreenType.TYPE_LINE_SELECT),
  
  /*
  * Screens used to confirm or cancel the execution of a selected function
  */
  CONFIRM_INSTR_DELETE(ScreenType.TYPE_CONFIRM_CANCEL),
  CONFIRM_PROG_DELETE(ScreenType.TYPE_CONFIRM_CANCEL),
  CONFIRM_RENUM(ScreenType.TYPE_CONFIRM_CANCEL),
  CONFIRM_UNDO(ScreenType.TYPE_CONFIRM_CANCEL),
  
  /*
  * Screens used to display a context-based list of options to the user
  */
  INSTRUCT_MENU_NAV(ScreenType.TYPE_OPT_MENU),
  IO_SUBMENU(ScreenType.TYPE_OPT_MENU),
  MAIN_MENU_NAV(ScreenType.TYPE_OPT_MENU),
  PICK_FRAME_METHOD(ScreenType.TYPE_OPT_MENU),
  PICK_FRAME_MODE(ScreenType.TYPE_OPT_MENU),
  PICK_INSTRUCT(ScreenType.TYPE_OPT_MENU),
  SETUP_NAV(ScreenType.TYPE_OPT_MENU),
  
  /*
  * Screens involving the entry of text, either via keyboard input or function buttons
  */
  FIND_REPL(ScreenType.TYPE_TEXT_ENTRY),
  INPUT_REMARK_DREG(ScreenType.TYPE_TEXT_ENTRY),
  INPUT_REMARK_PREG(ScreenType.TYPE_TEXT_ENTRY),
  NEW_PROGRAM(ScreenType.TYPE_TEXT_ENTRY),
  
  /*
  * Screens involving the entry of numeric values via either a physical num pad or
  * the virtual numpad included in the simulator UI
  */
  CONFIRM_INSERT(ScreenType.TYPE_NUM_ENTRY),
  INPUT_INTEGER(ScreenType.TYPE_NUM_ENTRY),
  INPUT_FLOAT(ScreenType.TYPE_NUM_ENTRY),
  JUMP_TO_LINE(ScreenType.TYPE_NUM_ENTRY), 
  SET_DO_BRACKET(ScreenType.TYPE_NUM_ENTRY),
  SET_FRM_INSTR_IDX(ScreenType.TYPE_NUM_ENTRY),
  SET_MV_INSTR_REG_NUM(ScreenType.TYPE_NUM_ENTRY),
  SET_MV_INSTR_SPD(ScreenType.TYPE_NUM_ENTRY),
  SET_MV_INSTR_TERM(ScreenType.TYPE_NUM_ENTRY),
  SET_RO_BRACKET(ScreenType.TYPE_NUM_ENTRY),
  
  /*
   * Frame input methods
   */
  THREE_POINT_TOOL(ScreenType.TYPE_FRAME_EDIT),
  THREE_POINT_USER(ScreenType.TYPE_FRAME_EDIT),
  FOUR_POINT_MODE(ScreenType.TYPE_FRAME_EDIT),
  SIX_POINT_MODE(ScreenType.TYPE_FRAME_EDIT),
  
  /*
   * Screens involving direct entry of point values
   */
  DIRECT_ENTRY_TOOL(ScreenType.TYPE_POINT_ENTRY),
  DIRECT_ENTRY_USER(ScreenType.TYPE_POINT_ENTRY),
  INPUT_POINT_C(ScreenType.TYPE_POINT_ENTRY),
  INPUT_POINT_J(ScreenType.TYPE_POINT_ENTRY),
  
  /*
  * Miscelanious screens/ not otherwise categorized
  */
  SWITCH_PREG,
  EDIT_RSTMT,
  USER_FRAME_DETAIL,
  TOOL_FRAME_DETAIL,
  INPUT_CONSTANT,
  INPUT_OPERATOR,
  INPUT_PRDX,
  INPUT_PRVDX,
  INPUT_RDX,
  INPUT_RSTMT,
  PICK_LETTER,
  DATA_MENU_NAV,
  SET_DO_STATUS,
  SET_RO_STATUS,
  VIEW_INST_REG,
  //Cartesian
  VIEW_POS_REG_C,
  //Joint
  VIEW_POS_REG_J,
  VIEW_DATA_REG;
  
  private final ScreenType type;
  
  private Screen(){
    type = null;
  }
  
  private Screen(ScreenType t){
    type = t;
  }
  
  public ScreenType getType(){
    return type;
  } 
}
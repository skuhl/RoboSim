public interface DisplayMode {}

public static enum ScreenType implements DisplayMode {
  TYPE_DEFAULT,
  TYPE_OPT_MENU,
  TYPE_LINE_SELECT,
  TYPE_LIST_CONTENTS,
  TYPE_CONFIRM_CANCEL,
  TYPE_INSTRUCT_EDIT,
  TYPE_TEXT_ENTRY,
  TYPE_NUM_ENTRY;
}

public static enum Screen implements DisplayMode {
   
  /* 
  * The "Home" screen, default root screen state displayed on startup
  */
  NONE(ScreenType.TYPE_DEFAULT), 
  
  /*
  * Set of screens used to manipulate instruction parameters with a finite number of states
  */
  SET_FRAME_INSTRUCTION(ScreenType.TYPE_INSTRUCT_EDIT),
  SET_INSTRUCTION_SPEED(ScreenType.TYPE_INSTRUCT_EDIT),
   
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
  COM_UNCOM(ScreenType.TYPE_LINE_SELECT),
  SELECT_CUT_COPY(ScreenType.TYPE_LINE_SELECT),
  SELECT_DELETE(ScreenType.TYPE_LINE_SELECT),
  
  /*
  * Screens used to confirm or cancel the execution of a selected function
  */
  CONFIRM_INSTR_DELETE(ScreenType.TYPE_CONFIRM_CANCEL),
  CONFIRM_PROG_DELETE(ScreenType.TYPE_CONFIRM_CANCEL),
  CONFIRM_RENUM(ScreenType.TYPE_CONFIRM_CANCEL),
  CONFIRM_UNDO(ScreenType.TYPE_CONFIRM_CANCEL),
  INSTRUCTION_EDIT(ScreenType.TYPE_CONFIRM_CANCEL),
  
  /*
  * Screens used to display a context-based list of options to the user
  */
  INSTRUCT_MENU_NAV(ScreenType.TYPE_OPT_MENU),
  IO_SUBMENU(ScreenType.TYPE_OPT_MENU),
  MAIN_MENU_NAV(ScreenType.TYPE_OPT_MENU),
  PICK_FRAME_METHOD(ScreenType.TYPE_OPT_MENU),
  PICK_FRAME_MODE(ScreenType.TYPE_OPT_MENU),
  PICK_INSTRUCTION(ScreenType.TYPE_OPT_MENU),
  SETUP_NAV(ScreenType.TYPE_OPT_MENU),
  
  /*
  * Screens involving the entry of text, either via keyboard input or function buttons
  */
  ENTER_TEXT(ScreenType.TYPE_TEXT_ENTRY),
  FIND_REPL(ScreenType.TYPE_TEXT_ENTRY),
  INPUT_COMMENT_L(ScreenType.TYPE_TEXT_ENTRY),
  INPUT_COMMENT_U(ScreenType.TYPE_TEXT_ENTRY),
  NEW_PROGRAM(ScreenType.TYPE_TEXT_ENTRY),
  
  /*
  * Screens involving the entry of numeric values via either a physical num pad or
  * the virtual numpad included in the simulator UI
  */
  CONFIRM_INSERT(ScreenType.TYPE_NUM_ENTRY),
  INPUT_INTEGER(ScreenType.TYPE_NUM_ENTRY),
  JUMP_TO_LINE(ScreenType.TYPE_NUM_ENTRY), 
  SET_DO_BRACKET(ScreenType.TYPE_NUM_ENTRY),
  SET_FRAME_INSTRUCTION_IDX(ScreenType.TYPE_NUM_ENTRY),
  SET_INSTRUCTION_REGISTER(ScreenType.TYPE_NUM_ENTRY),
  SET_INSTRUCTION_TERMINATION(ScreenType.TYPE_NUM_ENTRY),
  SET_RO_BRACKET(ScreenType.TYPE_NUM_ENTRY),
  
  /*
  * Miscelanious screens/ not otherwise categorized
  */
  DIRECT_ENTRY_MODE,
  EDIT_RSTMT,
  FOUR_POINT_MODE,
  FRAME_DETAIL, 
  INPUT_CONSTANT,
  INPUT_FLOAT,
  INPUT_OPERATOR,
  INPUT_POINT_C,
  INPUT_POINT_J,
  INPUT_PRDX,
  INPUT_PRVDX,
  INPUT_RDX,
  INPUT_RSTMT,
  PICK_LETTER,
  PICK_REG_LIST,
  SET_DO_STATUS,
  SET_RO_STATUS,
  SIX_POINT_MODE,
  THREE_POINT_MODE,
  VIEW_INST_REG,
  //Cartesian
  VIEW_POS_REG_C,
  //Joint
  VIEW_POS_REG_J,
  VIEW_REG;
  
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
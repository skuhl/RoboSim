/* The possible values for the current Coordinate Frame */
public enum CoordFrame { JOINT, WORLD, TOOL, USER }
/* The possible types of End Effectors for the Robot */
public enum EndEffector { NONE, SUCTION, CLAW, POINTER; }
/* The different motion types for the Robot to when moving to specific joint angles, or positon and orientation. */
public enum RobotMotion { HALTED, MT_JOINT, MT_LINEAR; }
/* The states for displaying the current frame as axes */
public enum AxesDisplay { AXES, GRID, NONE };
/* The states for mapping the Robot's End Effector to the grid */
public enum EEMapping { LINE, DOT, NONE };

/* These are used to store the operators used in register statement expressions in the ExpressionSet Object */
public enum Operator implements ExpressionElement {
  ADDTN("+", ARITH), 
  SUBTR("-", ARITH), 
  MULT("*", ARITH), 
  DIV("/", ARITH), 
  MOD("%", ARITH), 
  INTDIV("|", ARITH),
  PAR_OPEN("(", -1),
  PAR_CLOSE(")", -1),
  EQUAL("=", BOOL),
  NEQUAL("<>", BOOL),
  GRTR(">", BOOL),
  LESS("<", BOOL),
  GREQ(">=", BOOL),
  LSEQ("<=", BOOL),
  AND("&&", BOOL),
  OR("&&", BOOL),
  NOT("!", BOOL),
  UNINIT("...", -1);
  
  public final String symbol;
  public final int type;
  
  private Operator(String s, int t) {
    symbol = s;
    type = t;
  }
  
  public int getLength() {
    return 1;
  }
  
  public String toString() {
    return symbol;
  }
  
  public String[] toStringArray() {
    return new String[] { toString() };
  }
}

/* The type of the position register to use in a register statement */
public enum PositionType { GLOBAL, LOCAL }

public interface DisplayMode {}

public static enum ScreenType implements DisplayMode {
  TYPE_DEFAULT,
  TYPE_OPT_MENU,
  TYPE_LINE_SELECT,
  TYPE_LIST_CONTENTS,
  TYPE_CONFIRM_CANCEL,
  TYPE_INSTRUCT_EDIT,
  TYPE_EXPR_EDIT,
  TYPE_TEACH_POINTS,
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
  SET_BOOL_EXPR_ACT(ScreenType.TYPE_EXPR_EDIT),
  SET_MV_INSTRUCT_TYPE(ScreenType.TYPE_INSTRUCT_EDIT),
  SET_MV_INSTRUCT_REG_TYPE(ScreenType.TYPE_INSTRUCT_EDIT),
  SET_FRM_INSTR_TYPE(ScreenType.TYPE_INSTRUCT_EDIT),
  SET_IO_INSTR_STATE(ScreenType.TYPE_INSTRUCT_EDIT),
  
  SET_BOOL_CONST(ScreenType.TYPE_INSTRUCT_EDIT),
  SET_EXPR_ARG(ScreenType.TYPE_EXPR_EDIT),
  SET_BOOL_EXPR_ARG(ScreenType.TYPE_EXPR_EDIT),
  SET_EXPR_OP(ScreenType.TYPE_EXPR_EDIT),
  
  /*
  * Screens used to display a sereal list of contents for the user to
  * examine and interact with
  */
  NAV_PROG_INST(ScreenType.TYPE_LIST_CONTENTS),
  NAV_TOOL_FRAMES(ScreenType.TYPE_LIST_CONTENTS),
  NAV_USER_FRAMES(ScreenType.TYPE_LIST_CONTENTS),
  NAV_PROGRAMS(ScreenType.TYPE_LIST_CONTENTS),
  //Cartesian
  NAV_PREGS_C(ScreenType.TYPE_LIST_CONTENTS),
  //Joint
  NAV_PREGS_J(ScreenType.TYPE_LIST_CONTENTS),
  NAV_DREGS(ScreenType.TYPE_LIST_CONTENTS),
  
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
  MAIN_MENU_NAV(ScreenType.TYPE_OPT_MENU),
  TOOL_FRAME_METHODS(ScreenType.TYPE_OPT_MENU),
  USER_FRAME_METHODS(ScreenType.TYPE_OPT_MENU),
  SELECT_COND_STMT(ScreenType.TYPE_OPT_MENU),
  SELECT_FRAME_INSTR_TYPE(ScreenType.TYPE_OPT_MENU),
  SELECT_FRAME_MODE(ScreenType.TYPE_OPT_MENU),
  SELECT_INSTR_INSERT(ScreenType.TYPE_OPT_MENU),
  SELECT_IO_INSTR_REG(ScreenType.TYPE_OPT_MENU),
  SELECT_JMP_LBL(ScreenType.TYPE_OPT_MENU),
  SELECT_REG_EXPR_TYPE(ScreenType.TYPE_OPT_MENU),
  SETUP_NAV(ScreenType.TYPE_OPT_MENU),
  
  /*
  * Screens involving the entry of text, either via keyboard input or function buttons
  */
  FIND_REPL(ScreenType.TYPE_TEXT_ENTRY),
  EDIT_DREG_COM(ScreenType.TYPE_TEXT_ENTRY),
  EDIT_PREG_COM(ScreenType.TYPE_TEXT_ENTRY),
  NEW_PROGRAM(ScreenType.TYPE_TEXT_ENTRY),
  
  /*
  * Screens involving the entry of numeric values via either a physical num pad or
  * the virtual numpad included in the simulator UI
  */
  ACTIVE_FRAMES(ScreenType.TYPE_NUM_ENTRY),
  CONFIRM_INSERT(ScreenType.TYPE_NUM_ENTRY),
  EDIT_DREG_VAL(ScreenType.TYPE_NUM_ENTRY),
  INPUT_DREG_IDX(ScreenType.TYPE_NUM_ENTRY),
  INPUT_IOREG_IDX(ScreenType.TYPE_NUM_ENTRY),
  INPUT_CONST(ScreenType.TYPE_NUM_ENTRY),
  JUMP_TO_LINE(ScreenType.TYPE_NUM_ENTRY),
  SET_FRAME_INSTR_IDX(ScreenType.TYPE_NUM_ENTRY),
  SET_IO_INSTR_IDX(ScreenType.TYPE_NUM_ENTRY),
  SET_JUMP_TGT(ScreenType.TYPE_NUM_ENTRY),
  SET_LBL_NUM(ScreenType.TYPE_NUM_ENTRY),
  SET_MV_INSTR_IDX(ScreenType.TYPE_NUM_ENTRY),
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
  INPUT_REG_STMT,
  PICK_LETTER,
  NAV_DATA,
  VIEW_INST_REG;
  
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
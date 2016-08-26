final int SMALL_BUTTON = 35,
          LARGE_BUTTON = 50;
final int BUTTON_DEFAULT = color(70),
          BUTTON_ACTIVE = color(220, 40, 40),
          BUTTON_TEXT = color(240),
          UI_LIGHT = color(240),
          UI_DARK = color(40);

int active_prog = -1; // the currently selected program
int active_instr = -1; // the currently selected instruction
int temp_select = 0;
boolean shift = false; // Is shift button pressed or not?
boolean step = false; // Is step button pressed or not?
int record = OFF;

Screen mode;
int g1_px, g1_py; // the left-top corner of group1
int g1_width, g1_height; // group 1's width and height
int display_px, display_py; // the left-top corner of display screen
int display_width, display_height; // height and width of display screen

Group g1, g2;
Button bt_record_normal, 
       bt_ee_normal;

String workingText; // when entering text or a number
String workingTextSuffix;
boolean speedInPercentage;

final int ITEMS_TO_SHOW = 8, // how many programs/ instructions to display on screen
          NUM_ENTRY_LEN = 16, // Maximum character length for a number input
          TEXT_ENTRY_LEN = 16; // Maximum character length for text entry

// Index of the current frame (Tool or User) selecting when in the Frame menus
int curFrameIdx = -1,
// Indices of currently active frames
    activeUserFrame = -1,
    activeToolFrame = -1;
// The Frame being taught, during a frame teaching process
Frame teachFrame = null;
// Expression operand currently being edited
ExprOperand opEdit = null;
int editIdx = -1;

//variables for keeping track of the last change made to the current program
Instruction lastInstruct;
boolean newInstruct;
int lastLine;

// display list of programs or motion instructions
ArrayList<DisplayLine> contents = new ArrayList<DisplayLine>();
// Display otions for a number of menus
ArrayList<String> options = new ArrayList<String>();
// store numbers pressed by the user
ArrayList<Integer> nums = new ArrayList<Integer>();
// container for instructions being coppied/ cut and pasted
ArrayList<Instruction> clipBoard = new ArrayList<Instruction>();
// string for displaying error message to user
String err = null;
// which element is on focus now?
int row_select = 0; //currently selected display row
int prev_select = -1; //saves row_select value if next screen also utilizes this variable
int col_select = 0; //currently selected display column
int opt_select = 0; //which option is on focus now?
int start_render = 0; //index of the first element in a list to be drawn on screen
int active_index = 0; //index of the cursor with respect to the first element on screen
boolean[] selectedLines; //array whose indecies correspond to currently selected lines
// how many textlabels have been created for display
int index_contents = 0, index_options = 100, index_nums = 1000;

/**
 * Used for comment name input. The user can cycle through the
 * six states for each function button in this mode:
 *
 * F1 -> A-F/a-f
 * F2 -> G-L/g-l
 * F3 -> M-R/m-r
 * F4 -> S-X/s-x
 * F5 -> Y-Z/y-z, _, @, *, .
 */
private int[] letterStates = {0, 0, 0, 0, 0};
private final char[][] letters = {{'a', 'b', 'c', 'd', 'e', 'f'},
                                  {'g', 'h', 'i', 'j', 'k', 'l'},
                                  {'m', 'n', 'o', 'p', 'q', 'r'},
                                  {'s', 't', 'u', 'v', 'w', 'x'},
                                  {'y', 'z', '_', '@', '*', '.'}};

void gui() {
  g1_px = 0;
  g1_py = (SMALL_BUTTON - 15) + 1;
  g1_width = 440;
  g1_height = 720;
  display_px = 10;
  display_py = 0;//(SMALL_BUTTON - 15) + 1;
  display_width = g1_width - 20;
  display_height = 280;
  
  display_stack.push(Screen.DEFAULT);
  mode = display_stack.peek();
  
  // group 1: display and function buttons
  g1 = cp5.addGroup("DISPLAY")
  .setPosition(g1_px, g1_py)
  .setBackgroundColor(color(127,127,127,100))
  .setWidth(g1_width)
  .setHeight(g1_height)
  .setBackgroundHeight(g1_height)
  .hideBar();
  
  cp5.addTextarea("txt")
  .setPosition(display_px, 0)
  .setSize(display_width, display_height)
  .setColorBackground(UI_LIGHT)
  .moveTo(g1);
  
  /**********************Top row buttons**********************/
  
  //calculate how much space each button will be given
  int button_offsetX = LARGE_BUTTON + 1;
  int button_offsetY = LARGE_BUTTON + 1;  
  
  int record_normal_px = WindowManager.lButtonWidth * 5 + LARGE_BUTTON + 1;
  int record_normal_py = 0;   
  PImage[] record = {loadImage("images/record-35x20.png"), 
    loadImage("images/record-over.png"), 
    loadImage("images/record-on.png")};   
  bt_record_normal = cp5.addButton("record_normal")
  .setPosition(record_normal_px, record_normal_py)
  .setSize(SMALL_BUTTON, SMALL_BUTTON)
  .setImages(record)
  .updateSize();     
  
  int EE_normal_px = record_normal_px + LARGE_BUTTON + 1;
  int EE_normal_py = 0;   
  PImage[] EE = {loadImage("images/EE_35x20.png"), 
    loadImage("images/EE_over.png"), 
    loadImage("images/EE_down.png")};   
  bt_ee_normal = cp5.addButton("EE")
  .setPosition(EE_normal_px, EE_normal_py)
  .setSize(SMALL_BUTTON, SMALL_BUTTON)
  .setImages(EE)
  .updateSize();

  /********************Function Row********************/
  
  int f1_px = display_px;
  int f1_py = display_py + display_height + 2;
  int f_width = display_width/5 - 1;
  cp5.addButton("f1")
  .setPosition(f1_px, f1_py)
  .setSize(f_width, LARGE_BUTTON)
  .setCaptionLabel("F1")
  .setColorBackground(BUTTON_DEFAULT)
  .setColorCaptionLabel(BUTTON_TEXT)  
  .moveTo(g1);     
  
  int f2_px = f1_px + f_width + 1;
  int f2_py = f1_py;
  cp5.addButton("f2")
  .setPosition(f2_px, f2_py)
  .setSize(f_width, LARGE_BUTTON)
  .setCaptionLabel("F2")
  .setColorBackground(BUTTON_DEFAULT)
  .setColorCaptionLabel(BUTTON_TEXT)  
  .moveTo(g1);  
  
  int f3_px = f2_px + f_width + 1;
  int f3_py = f2_py;
  cp5.addButton("f3")
  .setPosition(f3_px, f3_py)
  .setSize(f_width, LARGE_BUTTON)
  .setCaptionLabel("F3")
  .setColorBackground(BUTTON_DEFAULT)
  .setColorCaptionLabel(BUTTON_TEXT)  
  .moveTo(g1);    
  
  int f4_px = f3_px + f_width + 1;
  int f4_py = f3_py;   
  cp5.addButton("f4")
  .setPosition(f4_px, f4_py)
  .setSize(f_width, LARGE_BUTTON)
  .setCaptionLabel("F4")
  .setColorBackground(BUTTON_DEFAULT)
  .setColorCaptionLabel(BUTTON_TEXT)  
  .moveTo(g1);   
  
  int f5_px = f4_px + f_width + 1;
  int f5_py = f4_py;   
  cp5.addButton("f5")
  .setPosition(f5_px, f5_py)
  .setSize(f_width, LARGE_BUTTON)
  .setCaptionLabel("F5")
  .setColorBackground(BUTTON_DEFAULT)
  .setColorCaptionLabel(BUTTON_TEXT)  
  .moveTo(g1);
  
  /**********************Step/Shift Row**********************/
  
  int st_px = f1_px;
  int st_py = f1_py + button_offsetY + 10;   
  cp5.addButton("st")
  .setPosition(st_px, st_py)
  .setSize(LARGE_BUTTON, LARGE_BUTTON)
  .setCaptionLabel("STEP")
  .setColorBackground(BUTTON_DEFAULT)
  .setColorCaptionLabel(BUTTON_TEXT)  
  .moveTo(g1);
  
  int mu_px = st_px + LARGE_BUTTON + 19;
  int mu_py = st_py;   
  cp5.addButton("mu")
  .setPosition(mu_px, mu_py)
  .setSize(LARGE_BUTTON, SMALL_BUTTON)
  .setCaptionLabel("MENU")
  .setColorBackground(BUTTON_DEFAULT)
  .setColorCaptionLabel(BUTTON_TEXT)  
  .moveTo(g1);
  
  int se_px = mu_px + LARGE_BUTTON + 15;
  int se_py = mu_py;
  cp5.addButton("se")
  .setPosition(se_px, se_py)
  .setSize(LARGE_BUTTON, SMALL_BUTTON)
  .setCaptionLabel("SELECT")
  .setColorBackground(BUTTON_DEFAULT)
  .setColorCaptionLabel(BUTTON_TEXT)  
  .moveTo(g1);     
  
  int ed_px = se_px + button_offsetX;
  int ed_py = se_py;   
  cp5.addButton("ed")
  .setPosition(ed_px, ed_py)
  .setSize(LARGE_BUTTON, SMALL_BUTTON)
  .setCaptionLabel("EDIT")
  .setColorBackground(BUTTON_DEFAULT)
  .setColorCaptionLabel(BUTTON_TEXT)  
  .moveTo(g1);      
  
  int da_px = ed_px + button_offsetX;
  int da_py = ed_py;   
  cp5.addButton("da")
  .setPosition(da_px, da_py)
  .setSize(LARGE_BUTTON, SMALL_BUTTON)
  .setCaptionLabel("DATA")
  .setColorBackground(BUTTON_DEFAULT)
  .setColorCaptionLabel(BUTTON_TEXT)  
  .moveTo(g1);
  
  int fn_px = da_px + LARGE_BUTTON + 15;
  int fn_py = da_py;   
  cp5.addButton("Fn")
  .setPosition(fn_px, fn_py)
  .setSize(LARGE_BUTTON, SMALL_BUTTON)
  .setCaptionLabel("FCTN")
  .setColorBackground(BUTTON_DEFAULT)
  .setColorCaptionLabel(BUTTON_TEXT)  
  .moveTo(g1);
  
  int sf_px = fn_px + LARGE_BUTTON + 19;
  int sf_py = fn_py;
  cp5.addButton("sf")
  .setPosition(sf_px, sf_py)
  .setSize(LARGE_BUTTON, LARGE_BUTTON)
  .setCaptionLabel("SHIFT")
  .setColorBackground(BUTTON_DEFAULT)
  .setColorCaptionLabel(BUTTON_TEXT)  
  .moveTo(g1);
  
  int pr_px = mu_px;
  int pr_py = mu_py + button_offsetY;   
  cp5.addButton("pr")
  .setPosition(pr_px, pr_py + 15)
  .setSize(LARGE_BUTTON, SMALL_BUTTON)
  .setCaptionLabel("PREV")
  .setColorBackground(BUTTON_DEFAULT)
  .setColorCaptionLabel(BUTTON_TEXT)  
  .moveTo(g1);
  
  int ne_px = fn_px;
  int ne_py = mu_py + button_offsetY;
  cp5.addButton("ne")
  .setPosition(ne_px, ne_py + 15)
  .setSize(LARGE_BUTTON, SMALL_BUTTON)
  .setCaptionLabel("NEXT")
  .setColorBackground(BUTTON_DEFAULT)
  .setColorCaptionLabel(BUTTON_TEXT)  
  .moveTo(g1);
  
  /***********************Arrow Keys***********************/
  button_offsetY = SMALL_BUTTON + 1;
  
  PImage[] imgs_arrow_up = {loadImage("images/arrow-up.png"), 
    loadImage("images/arrow-up_over.png"), 
    loadImage("images/arrow-up_down.png")};   
  int up_px = ed_px + 5;
  int up_py = ed_py + button_offsetY + 10;
  cp5.addButton("up")
  .setPosition(up_px, up_py)
  .setSize(SMALL_BUTTON, SMALL_BUTTON)
  .setImages(imgs_arrow_up)
  .updateSize()
  .moveTo(g1);     
  
  PImage[] imgs_arrow_down = {loadImage("images/arrow-down.png"), 
    loadImage("images/arrow-down_over.png"), 
    loadImage("images/arrow-down_down.png")};   
  int dn_px = up_px;
  int dn_py = up_py + button_offsetY;
  cp5.addButton("dn")
  .setPosition(dn_px, dn_py)
  .setSize(SMALL_BUTTON, SMALL_BUTTON)
  .setImages(imgs_arrow_down)
  .updateSize()
  .moveTo(g1);    
  
  PImage[] imgs_arrow_l = {loadImage("images/arrow-l.png"), 
    loadImage("images/arrow-l_over.png"), 
    loadImage("images/arrow-l_down.png")};
  int lt_px = dn_px - button_offsetX;
  int lt_py = dn_py - button_offsetY/2;
  cp5.addButton("lt")
  .setPosition(lt_px, lt_py)
  .setSize(SMALL_BUTTON, SMALL_BUTTON)
  .setImages(imgs_arrow_l)
  .updateSize()
  .moveTo(g1);  
  
  PImage[] imgs_arrow_r = {loadImage("images/arrow-r.png"), 
    loadImage("images/arrow-r_over.png"), 
    loadImage("images/arrow-r_down.png")};
  int rt_px = dn_px + button_offsetX;
  int rt_py = lt_py;
  cp5.addButton("rt")
  .setPosition(rt_px, rt_py)
  .setSize(SMALL_BUTTON, SMALL_BUTTON)
  .setImages(imgs_arrow_r)
  .updateSize()
  .moveTo(g1);      
  
  //--------------------------------------------------------------//
  //                           Group 2                            //
  //--------------------------------------------------------------//
  int g2_offsetY = display_py + display_height + 4*LARGE_BUTTON - 10;
  
  /**********************Numpad Block*********************/
  
  int LINE_px = ed_px - 7*button_offsetX/2;
  int LINE_py = g2_offsetY + 5*button_offsetY;
  cp5.addButton("LINE")
  .setPosition(LINE_px, LINE_py)
  .setSize(LARGE_BUTTON, SMALL_BUTTON)
  .setCaptionLabel("-")
  .setColorBackground(BUTTON_DEFAULT)
  .setColorCaptionLabel(BUTTON_TEXT)  
  .moveTo(g1);    
  
  int PERIOD_px = LINE_px + button_offsetX;
  int PERIOD_py = LINE_py - button_offsetY;
  cp5.addButton("PERIOD")
  .setPosition(PERIOD_px, PERIOD_py)
  .setSize(LARGE_BUTTON, SMALL_BUTTON)
  .setCaptionLabel(".")
  .setColorBackground(BUTTON_DEFAULT)
  .setColorCaptionLabel(BUTTON_TEXT)  
  .moveTo(g1);   
  
  int COMMA_px = PERIOD_px + button_offsetX;
  int COMMA_py = PERIOD_py;
  cp5.addButton("COMMA")
  .setPosition(COMMA_px, COMMA_py)
  .setSize(LARGE_BUTTON, SMALL_BUTTON)
  .setCaptionLabel(",")
  .setColorBackground(BUTTON_DEFAULT)
  .setColorCaptionLabel(BUTTON_TEXT)  
  .moveTo(g1);
  
  int POSN_px = LINE_px + button_offsetX;
  int POSN_py = LINE_py;
  cp5.addButton("POSN")
  .setPosition(POSN_px, POSN_py)
  .setSize(LARGE_BUTTON, SMALL_BUTTON)
  .setCaptionLabel("POSN")
  .setColorBackground(BUTTON_DEFAULT)
  .setColorCaptionLabel(BUTTON_TEXT)  
  .moveTo(g1);
  
  int IO_px = POSN_px + button_offsetX;
  int IO_py = POSN_py;
  cp5.addButton("IO")
  .setPosition(IO_px, IO_py)
  .setSize(LARGE_BUTTON, SMALL_BUTTON)
  .setCaptionLabel("I/O")
  .setColorBackground(BUTTON_DEFAULT)
  .setColorCaptionLabel(BUTTON_TEXT)  
  .moveTo(g1);
  
  int NUM_px = LINE_px;
  int NUM_py = LINE_py - button_offsetY;
  for(int i = 0; i < 10; i += 1) {
    cp5.addButton("NUM"+i)
    .setPosition(NUM_px, NUM_py)
    .setSize(LARGE_BUTTON, SMALL_BUTTON)
    .setCaptionLabel(""+i)
    .setColorBackground(BUTTON_DEFAULT)
    .setColorCaptionLabel(BUTTON_TEXT)
    .moveTo(g1);
    
    if(i % 3 == 0) {
      NUM_px = LINE_px;
      NUM_py -= button_offsetY;
    }
    else {
      NUM_px += button_offsetX;
    }
  }
  
  int RESET_px = LINE_px;
  int RESET_py = NUM_py;
  cp5.addButton("RESET")
  .setPosition(RESET_px, RESET_py)
  .setSize(LARGE_BUTTON, SMALL_BUTTON)
  .setCaptionLabel("RESET")
  .setColorBackground(BUTTON_DEFAULT)
  .setColorCaptionLabel(BUTTON_TEXT)  
  .moveTo(g1);   

  int BKSPC_px = RESET_px + button_offsetX;
  int BKSPC_py = RESET_py;
  cp5.addButton("BKSPC")
  .setPosition(BKSPC_px, BKSPC_py)
  .setSize(LARGE_BUTTON, SMALL_BUTTON)
  .setCaptionLabel("BKSPC")
  .setColorBackground(BUTTON_DEFAULT)
  .setColorCaptionLabel(BUTTON_TEXT)  
  .moveTo(g1);      
  
  int ITEM_px = BKSPC_px + button_offsetX;
  int ITEM_py = BKSPC_py;
  cp5.addButton("ITEM")
  .setPosition(ITEM_px, ITEM_py)
  .setSize(LARGE_BUTTON, SMALL_BUTTON)
  .setCaptionLabel("ITEM")
  .setColorBackground(BUTTON_DEFAULT)
  .setColorCaptionLabel(BUTTON_TEXT)  
  .moveTo(g1);
  
  /***********************Util Block*************************/
  
  int ENTER_px = ed_px;
  int ENTER_py = g2_offsetY;
  cp5.addButton("ENTER")
  .setPosition(ENTER_px, ENTER_py)
  .setSize(LARGE_BUTTON, SMALL_BUTTON)
  .setCaptionLabel("ENTER")
  .setColorBackground(BUTTON_DEFAULT)
  .setColorCaptionLabel(BUTTON_TEXT)  
  .moveTo(g1);    
  
  int TOOL1_px = ENTER_px;
  int TOOL1_py = ENTER_py + button_offsetY;
  cp5.addButton("TOOL1")
  .setPosition(TOOL1_px, TOOL1_py)
  .setSize(LARGE_BUTTON, SMALL_BUTTON)
  .setCaptionLabel("TOOL1")
  .setColorBackground(BUTTON_DEFAULT)
  .setColorCaptionLabel(BUTTON_TEXT)  
  .moveTo(g1);   
  
  int TOOL2_px = TOOL1_px;
  int TOOL2_py = TOOL1_py + button_offsetY;
  cp5.addButton("TOOL2")
  .setPosition(TOOL2_px, TOOL2_py)
  .setSize(LARGE_BUTTON, SMALL_BUTTON)
  .setCaptionLabel("TOOL2")
  .setColorBackground(BUTTON_DEFAULT)
  .setColorCaptionLabel(BUTTON_TEXT)  
  .moveTo(g1);

  int MOVEMENU_px = TOOL2_px;
  int MOVEMENU_py = TOOL2_py + button_offsetY;
  cp5.addButton("MOVEMENU")
  .setPosition(MOVEMENU_px, MOVEMENU_py)
  .setSize(LARGE_BUTTON, SMALL_BUTTON)
  .setCaptionLabel("MVMU")
  .setColorBackground(BUTTON_DEFAULT)
  .setColorCaptionLabel(BUTTON_TEXT)  
  .moveTo(g1); 
  
  int SETUP_px = MOVEMENU_px;
  int SETUP_py = MOVEMENU_py + button_offsetY;
  cp5.addButton("SETUP")
  .setPosition(SETUP_px, SETUP_py)
  .setSize(LARGE_BUTTON, SMALL_BUTTON)
  .setCaptionLabel("SETUP")
  .setColorBackground(BUTTON_DEFAULT)
  .setColorCaptionLabel(BUTTON_TEXT)  
  .moveTo(g1);    
  
  int STATUS_px = SETUP_px;
  int STATUS_py = SETUP_py + button_offsetY;
  cp5.addButton("STATUS")
  .setPosition(STATUS_px, STATUS_py)
  .setSize(LARGE_BUTTON, SMALL_BUTTON)
  .setCaptionLabel("STATUS")
  .setColorBackground(BUTTON_DEFAULT)
  .setColorCaptionLabel(BUTTON_TEXT)  
  .moveTo(g1);
  
  /********************Joint Control Block*******************/
  
  int hd_px = STATUS_px + 3*button_offsetX/2;
  int hd_py = g2_offsetY;   
  cp5.addButton("hd")
  .setPosition(hd_px, hd_py)
  .setSize(LARGE_BUTTON, SMALL_BUTTON)
  .setCaptionLabel("HOLD")
  .setColorBackground(BUTTON_DEFAULT)
  .setColorCaptionLabel(BUTTON_TEXT)  
  .moveTo(g1);    
  
  int fd_px = hd_px;
  int fd_py = hd_py + button_offsetY;   
  cp5.addButton("fd")
  .setPosition(fd_px, fd_py)
  .setSize(LARGE_BUTTON, SMALL_BUTTON)
  .setCaptionLabel("FWD")
  .setColorBackground(BUTTON_DEFAULT)
  .setColorCaptionLabel(BUTTON_TEXT)  
  .moveTo(g1);   
  
  int bd_px = fd_px;
  int bd_py = fd_py + button_offsetY;   
  cp5.addButton("bd")
  .setPosition(bd_px, bd_py)
  .setSize(LARGE_BUTTON, SMALL_BUTTON)
  .setCaptionLabel("BWD")
  .setColorBackground(BUTTON_DEFAULT)
  .setColorCaptionLabel(BUTTON_TEXT)  
  .moveTo(g1);
  
  int COORD_px = bd_px;   
  int COORD_py = bd_py + button_offsetY;
  cp5.addButton("COORD")
  .setPosition(COORD_px, COORD_py)
  .setSize(LARGE_BUTTON, SMALL_BUTTON)
  .setCaptionLabel("COORD")
  .setColorBackground(BUTTON_DEFAULT)
  .setColorCaptionLabel(BUTTON_TEXT)
  .moveTo(g1);
  
  int SPEEDUP_px = COORD_px;
  int SPEEDUP_py = COORD_py + button_offsetY;
  cp5.addButton("SPEEDUP")
  .setPosition(SPEEDUP_px, SPEEDUP_py)
  .setSize(LARGE_BUTTON, SMALL_BUTTON)
  .setCaptionLabel("+%")
  .setColorBackground(BUTTON_DEFAULT)
  .setColorCaptionLabel(BUTTON_TEXT)  
  .moveTo(g1);   
  
  int SLOWDOWN_px = SPEEDUP_px;
  int SLOWDOWN_py = SPEEDUP_py + button_offsetY;
  cp5.addButton("SLOWDOWN")
  .setPosition(SLOWDOWN_px, SLOWDOWN_py)
  .setSize(LARGE_BUTTON, SMALL_BUTTON)
  .setCaptionLabel("-%")
  .setColorBackground(BUTTON_DEFAULT)
  .setColorCaptionLabel(BUTTON_TEXT)  
  .moveTo(g1);
  
  int JOINT_px = SLOWDOWN_px + button_offsetX;
  int JOINT_py = g2_offsetY;
  String[] labels = {" -X\n(J1)", " +X\n(J1)",
    " -Y\n(J2)", " +Y\n(J2)",
    " -Z\n(J3)", " +Z\n(J3)",
    "-XR\n(J4)", "+XR\n(J4)",
    "-YR\n(J5)", "+YR\n(J5)",
    "-ZR\n(J6)", "+ZR\n(J6)"};
  
  for(int i = 1; i <= 6; i += 1) {
    cp5.addButton("JOINT"+i+"_NEG")
    .setPosition(JOINT_px, JOINT_py)
    .setSize(LARGE_BUTTON, SMALL_BUTTON)
    .setCaptionLabel(labels[(i-1)*2])
    .setColorBackground(BUTTON_DEFAULT)
    .setColorCaptionLabel(BUTTON_TEXT)  
    .moveTo(g1)
    .getCaptionLabel()
    .alignY(TOP);
    
    JOINT_px += LARGE_BUTTON + 1; 
    cp5.addButton("JOINT"+i+"_POS")
    .setPosition(JOINT_px, JOINT_py)
    .setSize(LARGE_BUTTON, SMALL_BUTTON)
    .setCaptionLabel(labels[(i-1)*2 + 1])
    .setColorBackground(BUTTON_DEFAULT)
    .setColorCaptionLabel(BUTTON_TEXT)  
    .moveTo(g1)
    .getCaptionLabel()
    .alignY(TOP);
    
    JOINT_px = SLOWDOWN_px + button_offsetX;
    JOINT_py += SMALL_BUTTON + 1;
  }
  
  List<Button> buttons = cp5.getAll(Button.class);
  for(Button b : buttons) {
    b.getCaptionLabel().setFont(fnt_conB);
  }
}// End UI setup

/* mouse events */

public void mouseDragged(MouseEvent e) {
  if (mouseButton == CENTER) {
    // Drag the center mouse button to pan the camera
    float transScale = camera.getScale();
    camera.move(transScale * (mouseX - pmouseX), transScale * (mouseY - pmouseY), 0);
  }
  
  if (mouseButton == RIGHT) {
    // Drag right mouse button to rotate the camera
    float rotScale = DEG_TO_RAD / 4f;
    camera.rotate(rotScale * (mouseY - pmouseY), rotScale * (mouseX - pmouseX), 0);
  }
}

public void mouseWheel(MouseEvent event) {
  
  if (manager != null && manager.isMouseOverADropdownList()) {
    // Disable zomming when selecting an element from a dropdown list
    return;
  }
  
  float e = event.getCount();
  // Control scaling of the camera with the mouse wheel
  if (e > 0) {
    camera.changeScale(1.05f);
  } else if (e < 0) {
    camera.changeScale(0.95f);
  }
}

/*Keyboard events*/

public void keyPressed() {
  
  if (manager != null && manager.isATextFieldActive()) {
    // Disable other key events when typing in a text field
    return;
  }
  
  if(mode == Screen.PROG_CREATE) {
    // Modify the input name for the new program
    if(key == BACKSPACE && workingText.length() > 0) {
      
      if(workingText.length() > 1) {
        workingText = workingText.substring(0, workingText.length() - 1);
        col_select = min(col_select, workingText.length() - 1);
      }  else {
        workingText = "\0";
      }
      
      updateScreen();
    } else if(key == DELETE && workingText.length() > 0) {
      
      if(workingText.length() > 1) {
        workingText = workingText.substring(1, workingText.length());
        col_select = min(col_select, workingText.length() - 1);
      }  else {
        workingText = "\0";
      }
      
      updateScreen();
    // Valid characters in a program name or comment
    } else if(workingText.length() < TEXT_ENTRY_LEN && (key >= 'a' && key <= 'z') || (key >= 'A' && key <= 'Z')
          || (key >= '0' && key <= '9') || key == '.' || key == '@' || key == '*' || key == '_') {
      StringBuilder temp;
      // Insert the typed character
      if (workingText.length() > 0 && workingText.charAt(col_select) != '\0') {
        temp = new StringBuilder(workingText.substring(0, col_select) + "\0" + workingText.substring(col_select, workingText.length()));
      } else {
        temp = new StringBuilder(workingText); 
      }
        
      temp.setCharAt(col_select, key);
      workingText = temp.toString();
      
      // Add an insert element if the length of the current comment is less than 16
      int len = workingText.length();
      if(len <= TEXT_ENTRY_LEN && col_select == workingText.length() - 1 && workingText.charAt(len - 1) != '\0') {
        workingText += '\0';
      }
      
      col_select = min(col_select + 1, workingText.length() - 1);
      // Update contents to the new string
      updateScreen();
    }
    
    return;
  } else if (key == 'a') {
    // Cycle through Axes display states
    switch (axesState) {
      case NONE:
        axesState = AxesDisplay.AXES;
        break;
      case AXES:
        axesState = AxesDisplay.GRID;
        break;
      default:
        axesState = AxesDisplay.NONE;
    }
    
  } else if(key == 'e') {
    // Cycle through EE Mapping states
    switch (mappingState) {
      case NONE:
        mappingState = EEMapping.LINE;
        break;
      case LINE:
        mappingState = EEMapping.DOT;
        break;
      default:
        mappingState = EEMapping.NONE;
    }
    
  } else if (key == 'f' ) {
    // Display the User and Tool frames associated with the current motion instruction
    if (DISPLAY_TEST_OUTPUT && mode == Screen.NAV_PROG_INSTR && (col_select == 3 || col_select == 4)) {
      Instruction inst = activeInstruction();
      
      if (inst instanceof MotionInstruction) {
        MotionInstruction mInst = (MotionInstruction)inst;
        System.out.printf("\nUser frame: %d\nTool frame: %d\n", mInst.userFrame, mInst.toolFrame);
      }
    }
    
  } else if (key == 'm') {
    // Print the current mode to the console
    println(mode.toString());
    
  } else if (key == 'p') {
    // Toggle the Robot's End Effector state
    if (!programRunning) {
      armModel.toggleEEState();
    }
    
  } else if (key == 's') {
    // Save EVERYTHING!
    saveState();
    
  } else if (key == 't') {
    // Restore default Robot joint angles
    float[] rot = {0, 0, 0, 0, 0, 0};
    armModel.setJointAngles(rot);
    intermediatePositions.clear();
    
  } else if(key == 'w') {
    // Write anything stored in the String buffer to a text file
    writeBuffer();
    
  } else if (key == 'y') {
    // Apply another set of default Robot joint angles
    float[] rot = {PI, 0, 0, 0, 0, PI};
    armModel.setJointAngles(rot);
    intermediatePositions.clear();
    
  }
}

/* Button events */

public void FrontView() {
  // Default view
  camera.reset();
}

public void BackView() {
  // Back view
  camera.reset();
  camera.rotate(0, PI, 0);
}

public void LeftView() {
  // Left view
  camera.reset();
  camera.rotate(0, PI / 2f, 0);
}

public void RightView() {
  // Right view
  camera.reset();
  camera.rotate(0, 3f * PI / 2f, 0);
}

public void TopView() {
  // Top view
  camera.reset();
  camera.rotate(3f * PI / 2f, 0, 0);
}

public void BottomView() {
  // Bottom view
  camera.reset();
  camera.rotate(PI / 2f, 0, 0);
}

public void CreateWldObj() {
  /* Create a world object from the input fields in the Create window. */
  Scenario s = activeScenario();
  
  if (s != null) {
    WorldObject newObject = manager.createWorldObject();
    
    if (newObject != null) {
      newObject.setLocalCenter( new PVector(-500f, 0f, 0f) );
      s.addWorldObject(newObject);
    }
  }
}

public void ClearFields() {
  /* Clear all input fields for creating and editing world objects. */
  manager.clearCreateInputFields();
}

public void UpdateWldObj() {
  /* Confirm changes made to the orientation and
  * position of the selected world object. */
  manager.editWorldObject();
}

public void DeleteWldObj() {
  // Delete focused world object
  int ret = manager.deleteActiveWorldObject();
  if (DISPLAY_TEST_OUTPUT) { System.out.printf("World Object removed: %d\n", ret); }
}

public void NewScenario() {
  Scenario newScenario = manager.initializeScenario();
  
  if (newScenario != null) {
    // Add the new scenario
    SCENARIOS.add(newScenario);
  }
}

public void SaveScenario() {
  // Save all scenarios
  saveScenarioBytes( new File(sketchPath("tmp/scenarios.bin")) );
}

public void SetScenario() {
  Integer newActiveIdx = manager.getScenarioIndex();
  
  if (newActiveIdx != null) {
    // Set a new active scenario
    activeScenarioIdx = newActiveIdx;
  }
}

// Menu button
public void mu() {
  resetStack();
  nextScreen(Screen.NAV_MAIN_MENU);
}

// Select button
public void se() {
  // Save when exiting a program
  saveProgramBytes( new File(sketchPath("tmp/programs.bin")) ); 
  
  active_prog = 0;
  active_instr = -1;
  
  resetStack();
  nextScreen(Screen.NAV_PROGRAMS);
}

// Data button
public void da() {
  resetStack();
  nextScreen(Screen.NAV_DATA);
}

public void NUM0() {
  addNumber("0");
}

public void NUM1() {
  addNumber("1");
}

public void NUM2() {
  addNumber("2");
}

public void NUM3() {
  addNumber("3");
}

public void NUM4() {
  addNumber("4");
}

public void NUM5() {
  addNumber("5");
}

public void NUM6() {
  addNumber("6");
}

public void NUM7() {
  addNumber("7");
}

public void NUM8() {
  addNumber("8");
}

public void NUM9() {
  addNumber("9");
}

public void addNumber(String number) {
  if(mode.getType() == ScreenType.TYPE_NUM_ENTRY) {
    if (workingText.length() < NUM_ENTRY_LEN) {
      workingText += number;
    }
  }
  else if(mode == Screen.SET_MV_INSTR_SPD) {
    workingText += number;
    options.set(1, workingText + workingTextSuffix);
  } 
  else if(mode.getType() == ScreenType.TYPE_POINT_ENTRY) {
    if(row_select >= 0 && row_select < contents.size()) {
      String value = contents.get(row_select).get(1) + number;
      
      if(value.length() > 9) {
        // Max length of a an input value
        value = value.substring(0,  9);
      }
      
      // Concatenate the new digit
      contents.get(row_select).set(1, value);
    }
  }
  else if(mode.type == ScreenType.TYPE_TEXT_ENTRY) {
    // Replace current entry with a number
    StringBuilder temp = new StringBuilder(workingText);
    temp.setCharAt(col_select, number.charAt(0));
    workingText = temp.toString();
  }
  
  updateScreen();
}

public void RESET() {
  if (shift) {
    // Reset robot fault
    armModel.halt();
    robotFault = false;
  }
}

public void PERIOD() {
  if(mode.getType() == ScreenType.TYPE_POINT_ENTRY) {

    if(row_select >= 0 && row_select < contents.size()) {

      // Add decimal point
      String value = contents.get(row_select).get(1) + ".";

      if(value.length() > 9) {
        // Max length of a an input value
        value = value.substring(0,  9);
      }
      
      contents.get(row_select).set(1, value);
    }
  } else if(mode.type == ScreenType.TYPE_NUM_ENTRY) {
    
    if(workingText.length() < NUM_ENTRY_LEN) {
      workingText += ".";
    }
  } else if(mode != Screen.EDIT_DREG_COM) {
    workingText += ".";
  }
  
  updateScreen();
}

public void LINE() {
  if(mode.getType() == ScreenType.TYPE_POINT_ENTRY) {
    
    if(row_select >= 0 && row_select < contents.size()) {
      String value = contents.get(row_select).get(1);
      
      // Mutliply current number by -1
      if(value.length() > 0 && value.charAt(0) == '-') {
        contents.get(row_select).set(1, value.substring(1, value.length()));
      } else {
        contents.get(row_select).set(1, "-" + value);
      }
    }
    
  } else if(mode.type == ScreenType.TYPE_NUM_ENTRY) {
    
    // Mutliply current number by -1
    if(workingText.length() > 0 && workingText.charAt(0) == '-') {
      workingText = workingText.substring(1);
    } else {
      workingText = "-" + workingText;
    }
    
  }
  
  updateScreen();
}

public void IO() {
  if (!programRunning) {
    /* Do not allow the Robot's End Effector state to be changed
     * when a program is executing */
    armModel.toggleEEState();
  }
}

/*Arrow keys*/

public void up() {
  switch(mode) {
    case NAV_PROGRAMS:
      active_prog = moveUp(shift);
            
      if(DISPLAY_TEST_OUTPUT) {
        System.out.printf("\nOpt: %d\nProg: %d\nTRS: %d\n\n",
        opt_select, active_prog, start_render);
      }
      break;
    case NAV_PROG_INSTR:
    case SELECT_COMMENT:
    case SELECT_CUT_COPY:
    case SELECT_INSTR_DELETE:
      if (!programRunning) {
        // Lock movement when a program is running
        int prevRow = getSelectedRow();
        active_instr = moveUpInstr(shift);
        
        //special case for select statement column navigation
        if(activeInstruction() instanceof SelectStatement && getSelectedRow() == 0) {
          if(prevRow == 1) {
            col_select += 3;
          }
        }

            
        if(DISPLAY_TEST_OUTPUT) {
          System.out.printf("\nRow: %d\nColumn: %d\nInst: %d\nTRS: %d\n\n",
          row_select, col_select, active_instr, start_render);
        }
      }
      break;
    case NAV_DREGS:
    case NAV_PREGS_J:
    case NAV_PREGS_C:
    case NAV_TOOL_FRAMES:
    case NAV_USER_FRAMES:
      active_index = moveUp(shift);
      
      if(DISPLAY_TEST_OUTPUT) {
        System.out.printf("\nRow: %d\nColumn: %d\nIdx: %d\nTRS: %d\n\n",
        row_select, col_select, active_index, start_render);
      }
      break;
    case SET_CALL_PROG:
    case DIRECT_ENTRY_TOOL:
    case DIRECT_ENTRY_USER:
    case EDIT_PREG_C:
    case EDIT_PREG_J:
      moveUp(shift);
      break;
    case NAV_MAIN_MENU:
    case NAV_INSTR_MENU:
    case SELECT_FRAME_MODE:
    case FRAME_METHOD_USER:
    case FRAME_METHOD_TOOL:
    case SELECT_INSTR_INSERT:
    case SELECT_IO_INSTR_REG:
    case SELECT_FRAME_INSTR_TYPE:
    case SELECT_REG_STMT:
    case SELECT_COND_STMT:
    case SELECT_JMP_LBL:
    case TFRAME_DETAIL:
    case UFRAME_DETAIL:
    case TEACH_3PT_USER:
    case TEACH_3PT_TOOL:
    case TEACH_4PT:
    case TEACH_6PT:
    case NAV_DATA:
    case SWAP_PT_TYPE:
    case SET_MV_INSTR_TYPE:
    case SET_MV_INSTR_REG_TYPE:
    case SET_FRM_INSTR_TYPE:
    case SET_REG_EXPR_TYPE:
    case SET_IF_STMT_ACT:
    case SET_SELECT_STMT_ACT:
    case SET_SELECT_STMT_ARG:
    case SET_EXPR_ARG: //<>//
    case SET_BOOL_EXPR_ARG: //<>//
    case SET_EXPR_OP:
    case SET_IO_INSTR_STATE:
    case NAV_SETUP:
      opt_select = max(0, opt_select - 1);
      break;
    case ACTIVE_FRAMES:
      updateActiveFramesDisplay();
      workingText = Integer.toString(activeToolFrame + 1);
      row_select = max(0, row_select - 1);
      break;
    default:
      if (mode.type == ScreenType.TYPE_TEXT_ENTRY) {
        opt_select = max(0, opt_select - 1); 
        // Reset function key states
        for(int idx = 0; idx < letterStates.length; ++idx) { letterStates[idx] = 0; }
      }
  }
  
  updateScreen();
}
 //<>//
public void dn() {
  switch(mode) {
    case NAV_PROGRAMS: //<>//
      active_prog = moveDown(shift);
            
      if(DISPLAY_TEST_OUTPUT) {
        System.out.printf("\nRow: %d\nProg: %d\nTRS: %d\n\n", //<>//
        row_select, active_prog, start_render);
      }
      break;
    case NAV_PROG_INSTR:
    case SELECT_COMMENT:
    case SELECT_CUT_COPY:
    case SELECT_INSTR_DELETE:
      if (!programRunning) {
        // Lock movement when a program is running
        int prevIdx = getSelectedIdx();
        active_instr = moveDownInstr(shift); //<>//
        
        //special case for select statement column navigation
        if(activeInstruction() instanceof SelectStatement && getSelectedRow() == 1) {
          if(prevIdx >= 3) {
            col_select = prevIdx - 3;
          } else {
            col_select = 0;
          }
        }
      
        if(DISPLAY_TEST_OUTPUT) {
          System.out.printf("\nRow: %d\nColumn: %d\nInst: %d\nTRS: %d\n\n",
          row_select, col_select, active_instr, start_render);
        }
      }
      break;
    case NAV_TOOL_FRAMES:
    case NAV_USER_FRAMES:
    case NAV_DREGS:
    case NAV_PREGS_J:
    case NAV_PREGS_C:
      active_index = moveDown(shift);
      
      if(DISPLAY_TEST_OUTPUT) {
        System.out.printf("\nRow: %d\nColumn: %d\nIdx: %d\nTRS: %d\n\n",
        row_select, col_select, active_index, start_render);
      }
      break;
    case SET_CALL_PROG:
    case DIRECT_ENTRY_TOOL:
    case DIRECT_ENTRY_USER:
    case EDIT_PREG_C:
    case EDIT_PREG_J:
      moveDown(shift);
      break;
    case NAV_MAIN_MENU:
    case NAV_INSTR_MENU:
    case SELECT_FRAME_MODE:
    case FRAME_METHOD_USER:
    case FRAME_METHOD_TOOL:
    case SELECT_INSTR_INSERT:
    case SELECT_IO_INSTR_REG:
    case SELECT_FRAME_INSTR_TYPE:
    case SELECT_REG_STMT:
    case SELECT_COND_STMT:
    case SELECT_JMP_LBL:
    case TFRAME_DETAIL:
    case UFRAME_DETAIL:
    case TEACH_3PT_USER:
    case TEACH_3PT_TOOL:
    case TEACH_4PT:
    case TEACH_6PT:
    case NAV_DATA:
    case SWAP_PT_TYPE:
    case SET_MV_INSTR_TYPE:
    case SET_MV_INSTR_REG_TYPE:
    case SET_FRM_INSTR_TYPE:
    case SET_REG_EXPR_TYPE:
    case SET_IF_STMT_ACT:
    case SET_SELECT_STMT_ACT:
    case SET_SELECT_STMT_ARG:
    case SET_EXPR_ARG:
    case SET_BOOL_EXPR_ARG:
    case SET_EXPR_OP:
    case SET_IO_INSTR_STATE:
    case NAV_SETUP:
      opt_select = min(opt_select + 1, options.size() - 1);
      break;  //<>//
    case ACTIVE_FRAMES:
      updateActiveFramesDisplay();
      workingText = Integer.toString(activeUserFrame + 1);
      row_select = min(row_select + 1, contents.size() - 1);
      break;
    default:
      if (mode.type == ScreenType.TYPE_TEXT_ENTRY) {
        opt_select = min(opt_select + 1, options.size() - 1);
        // Reset function key states
        for(int idx = 0; idx < letterStates.length; ++idx) { letterStates[idx] = 0; }
      }
  }  
  
  updateScreen();
}

public void lt() { 
  switch(mode) { 
    case NAV_PROG_INSTR:
      if (!programRunning) {
        // Lock movement when a program is running
        moveLeft();
      }
      break;
    case NAV_DREGS:
    case NAV_PREGS_J:
    case NAV_PREGS_C:
      col_select = max(0, col_select - 1);
      break;
    default:
      if (mode.type == ScreenType.TYPE_TEXT_ENTRY) {
        col_select = max(0, col_select - 1);
        // Reset function key states //<>//
        for(int idx = 0; idx < letterStates.length; ++idx) { letterStates[idx] = 0; }
      } else if(mode.type == ScreenType.TYPE_EXPR_EDIT) { //<>//
        col_select -= (col_select - 4 >= options.size()) ? 4 : 0;
      }
  }
  
  updateScreen();
}


public void rt() {
  switch(mode) {
    case NAV_PROG_INSTR:
      if (!programRunning) {
        // Lock movement when a program is running
        moveRight();
      }
      break;
    case DIRECT_ENTRY_USER:
    case DIRECT_ENTRY_TOOL:
    case EDIT_PREG_C:
    case EDIT_PREG_J:
      // Delete a digit from the beginning of the number entry
      if(shift) {
        String entry = contents.get(row_select).get(1);
        
        if (entry.length() > 1 && !(entry.length() == 2 && entry.charAt(0) == '-')) {
          
          if(entry.charAt(0) == '-') {
            // Keep negative sign until the last digit is removed
            contents.get(row_select).set(1, "-" + entry.substring(2, entry.length()));
          } else {
            contents.get(row_select).set(1, entry.substring(1, entry.length()));
          }
        } else {
          contents.get(row_select).set(1, "");
        }
      }
      
      break;
    case NAV_DREGS:
    case NAV_PREGS_J:
    case NAV_PREGS_C:
      col_select = min(col_select + 1, contents.get(row_select).size() - 1);
      break;
    default:
      if (mode.type == ScreenType.TYPE_TEXT_ENTRY) {
        
        if(shift) {
          // Delete key function
          if(workingText.length() > 1) {
            workingText = workingText.substring(1, workingText.length());
            col_select = min(col_select, workingText.length() - 1);
          }  else {
            workingText = "\0";
          }
          
          col_select = max(0, min(col_select, contents.get(row_select).size() - 1));
        } else if (mode.type == ScreenType.TYPE_EXPR_EDIT) {
          col_select += (col_select + 4 < options.size()) ? 4 : 0;
        } else {
          // Add an insert element if the length of the current comment is less than 16
          int len = workingText.length();
          if(len <= TEXT_ENTRY_LEN && col_select == workingText.length() - 1 && workingText.charAt(len - 1) != '\0') {
            workingText += '\0';
            // Update contents to the new string
            updateScreen();
          }
          
          col_select = min(col_select + 1, contents.get(row_select).size() - 1);
        }
        
        // Reset function key states
        for(int idx = 0; idx < letterStates.length; ++idx) { letterStates[idx] = 0; }
      }
  }
  
  updateScreen();
}

//toggle shift on/ off and button highlight
public void sf() {
  if(!shift) {
    ((Button)cp5.get("sf")).setColorBackground(BUTTON_ACTIVE);
  } else {
    // Stop Robot jog movement when shift is off
    armModel.halt();
    ((Button)cp5.get("sf")).setColorBackground(BUTTON_DEFAULT);
  }
  
  shift = !shift;
  updateScreen();
}

//toggle step on/ off and button highlight
public void st() {
  if(!step) {
    ((Button)cp5.get("st")).setColorBackground(BUTTON_ACTIVE);
  }
  else {
    ((Button)cp5.get("st")).setColorBackground(BUTTON_DEFAULT);
  }
  
  
  step = !step;
  updateScreen();
}

public void pr() {
  lastScreen();
}

public void f1() {
  switch(mode) {
    case NAV_PROGRAMS:
      nextScreen(Screen.PROG_CREATE);
      break;
    case NAV_PROG_INSTR:
      if(shift) {
        newMotionInstruction();
        col_select = 0;
        
        if(getSelectedIdx() < 6) {
          row_select += 1;
          active_instr += 1;
        }
      }
      break;
    case NAV_TOOL_FRAMES:
      if(shift) {
        // Reset the highlighted frame in the tool frame list
        toolFrames[active_index] = new ToolFrame();
        saveFrameBytes( new File(sketchPath("tmp/frames.bin")) );
        updateScreen();
      } else {
        // Set the current tool frame
        activeToolFrame = row_select;
        updateCoordFrame();
      }
      break;
    case NAV_USER_FRAMES:
      if(shift) {
        // Reset the highlighted frame in the user frames list
        userFrames[active_index] = new UserFrame();
        saveFrameBytes( new File(sketchPath("tmp/frames.bin")) );
        updateScreen();
      } else {
        // Set the current user frame
        activeUserFrame = active_index;
        updateCoordFrame();
      }
      break;
    case ACTIVE_FRAMES:
      if(row_select == 0) {
        nextScreen(Screen.NAV_TOOL_FRAMES);
      } else if(row_select == 1) {
        nextScreen(Screen.NAV_USER_FRAMES);
      }
      break;
    case NAV_DREGS:
      // Clear Data Register entry
      DREG[active_index] = new DataRegister(active_index);
      saveRegisterBytes( new File(sketchPath("tmp/registers.bin")) );
      break;
    case NAV_PREGS_J:
    case NAV_PREGS_C:
      // Clear Position Register entry
      GPOS_REG[active_index] = new PositionRegister(active_index);
      saveRegisterBytes( new File(sketchPath("tmp/registers.bin")) );
      break;
    default:
      if (mode.type == ScreenType.TYPE_TEXT_ENTRY) {
        editTextEntry(0);
      }
  }
  
  updateScreen();
}

public void f2() {
  switch(mode) {
    case NAV_PROGRAMS:
      if(programs.size() > 0) {
        nextScreen(Screen.PROG_RENAME);
      }
      break;
    case NAV_PROG_INSTR:
      nextScreen(Screen.SELECT_INSTR_INSERT);
      break;
    case TFRAME_DETAIL:
      switchScreen(Screen.FRAME_METHOD_TOOL);
      //nextScreen(Screen.TOOL_FRAME_METHODS);
      break;
    case TEACH_3PT_TOOL:
    case TEACH_6PT:
    case DIRECT_ENTRY_TOOL:
      lastScreen();
      break;
    case UFRAME_DETAIL:
      switchScreen(Screen.FRAME_METHOD_USER);
      //nextScreen(Screen.USER_FRAME_METHODS);
      break;
    case TEACH_3PT_USER:
    case TEACH_4PT:
    case DIRECT_ENTRY_USER:
      lastScreen();
      break;
    case NAV_DREGS:
      // Data Register copy menus
      if (col_select == 0) {
        nextScreen(Screen.CP_DREG_COM);
      } else if (col_select == 1) {
        nextScreen(Screen.CP_DREG_VAL);
      }
      break;
    case NAV_PREGS_J:
    case NAV_PREGS_C:
    // Position Register copy menus
      if (col_select == 0) {
        nextScreen(Screen.CP_PREG_COM);
      } else if (col_select == 1) {
        nextScreen(Screen.CP_PREG_PT);
      }
      break;
    default:
      if (mode.type == ScreenType.TYPE_TEXT_ENTRY) {
        editTextEntry(1);
        updateScreen();
      }
  }
}

public void f3() {
  switch(mode){
    case NAV_PROGRAMS:
      if(programs.size() > 0) {
        nextScreen(Screen.CONFIRM_PROG_DELETE);
      }
      break;
    case NAV_PROG_INSTR:
      int selectIdx = getSelectedIdx();
      if(activeInstruction() instanceof IfStatement) {
        IfStatement stmt = (IfStatement)activeInstruction();
                
        if(stmt.expr instanceof Expression && selectIdx >= 2) {
          ((Expression)stmt.expr).insertElement(selectIdx - 3);
          updateScreen();
          rt();
        }
      } 
      else if(activeInstruction() instanceof SelectStatement) {
        SelectStatement stmt = (SelectStatement)activeInstruction();
        
        if(selectIdx >= 3) {
          stmt.addCase();
          updateScreen();
          rt();
        }
      }
      else if(activeInstruction() instanceof RegisterStatement) {
        RegisterStatement stmt = (RegisterStatement)activeInstruction();
        int rLen = (stmt.posIdx == -1) ? 2 : 3;
        
        if(selectIdx > rLen) {
          stmt.expr.insertElement(selectIdx - (rLen + 2));
          updateScreen();
          rt();
        }
      }
      
      updateScreen();
      break;
    case SELECT_CUT_COPY:
      ArrayList<Instruction> inst = activeProgram().getInstructions();
      clipBoard = new ArrayList<Instruction>();
      
      int remIdx = 0;
      for(int i = 0; i < selectedLines.length; i += 1){
        if(selectedLines[i]){
          clipBoard.add(inst.get(remIdx));
          inst.remove(remIdx);
        } else{
          remIdx += 1;
        }
      }
      
      updateInstructions();
      break;
    case NAV_TOOL_FRAMES:
      active_index = 0;
      switchScreen(Screen.NAV_USER_FRAMES);
      break;
    case NAV_USER_FRAMES:
      active_index = 0;
      switchScreen(Screen.NAV_TOOL_FRAMES);
      break;
    case NAV_DREGS:
      // Switch to Position Registers
      nextScreen(Screen.NAV_PREGS_C);
      break;
    case NAV_PREGS_J:
    case NAV_PREGS_C:
      if (shift) {
        switchScreen(Screen.SWAP_PT_TYPE);
      } else {
        // Switch to Data Registers
        nextScreen(Screen.NAV_DREGS);
      }
      break;
    default:
      if (mode.type == ScreenType.TYPE_TEXT_ENTRY) {
        editTextEntry(2);
        updateScreen();
      }
  }
}


public void f4() {
  Program p = activeProgram();
  
  switch(mode) {
  case NAV_PROGRAMS:
    if(programs.size() > 0) {
      nextScreen(Screen.PROG_COPY);
    }
    break;
  case NAV_PROG_INSTR:
    Instruction ins = activeInstruction();
    
    if (ins != null) {
      int selectIdx = getSelectedIdx();
      getInstrEdit(ins, selectIdx);
    }
    break;
  case CONFIRM_INSERT:
    try {
      int lines_to_insert = Integer.parseInt(workingText);
      for(int i = 0; i < lines_to_insert; i += 1)
        p.getInstructions().add(active_instr, new Instruction());
      
      updateInstructions();
    }
    catch(Exception e){
      e.printStackTrace();
    }
    
    lastScreen();
    break;
  case CONFIRM_PROG_DELETE:
    int progIdx = active_prog;
    
    if(progIdx >= 0 && progIdx < programs.size()) {
      programs.remove(progIdx);
      
      if(active_prog >= programs.size()) {
        active_prog = programs.size() - 1;
        
        row_select = min(active_prog, ITEMS_TO_SHOW - 1);
        start_render = active_prog - row_select;
      }
      
      lastScreen();
      saveProgramBytes( new File(sketchPath("tmp/programs.bin")) );
    }
    break;
  case SELECT_INSTR_DELETE:
    ArrayList<Instruction> inst = p.getInstructions();
      
    int remIdx = 0;
    for(int i = 0; i < selectedLines.length; i += 1){
      if(selectedLines[i]){
        inst.remove(remIdx);
      } else{
        remIdx += 1;
      }
    }
    
    display_stack.pop();
    updateInstructions();
    break;
  case SELECT_CUT_COPY:
    inst = p.getInstructions();
    clipBoard = new ArrayList<Instruction>();
    
    for(int i = 0; i < selectedLines.length; i += 1){
      if(selectedLines[i])
        clipBoard.add(inst.get(i));
    }
    
    display_stack.pop();
    display_stack.pop();
    updateInstructions();
    break;
  case FIND_REPL:
    int lineIdx = 0;
    String s;
        
    for(Instruction instruct: p.getInstructions()){
      s = (lineIdx + 1) + ") " + instruct.toString();
      
      if(s.toUpperCase().contains(workingText.toUpperCase())){
        break;
      }
      
      lineIdx += 1;
    }
    
    display_stack.pop();
    active_instr = lineIdx;
    updateInstructions();
    break;
  case SELECT_COMMENT:
    display_stack.pop();
    updateInstructions();
    break;
  case CONFIRM_RENUM:
    Point[] pTemp = new Point[1000];
    int posIdx = 0;
    
    //make a copy of the current positions in p
    for(int i = 0; i < 1000; i += 1){
      pTemp[i] = p.getPosition(i);
    }
    
    p.clearPositions();
    
    //rearrange positions
    for(int i = 0; i < p.getInstructions().size(); i += 1) {
      Instruction instr = p.getInstruction(i);
      if(instr instanceof MotionInstruction) {
        int instructPos = ((MotionInstruction)instr).getPosition();
        p.setPosition(posIdx, pTemp[instructPos]);
        ((MotionInstruction)instr).setPosition(posIdx);
        posIdx += 1;
      }
    }
    
    display_stack.pop();
    updateInstructions();
    break;
  case NAV_PREGS_J:
  case NAV_PREGS_C:
    if (shift && !programRunning) {
      // Stop any prior jogging motion
      armModel.halt();
      
      // Move To function
      Point pt = GPOS_REG[active_index].point.clone();
      
      if (pt != null) {
        // Move the Robot to the select point
        if (mode == Screen.NAV_PREGS_C) {
          Frame active = getActiveFrame(CoordFrame.USER);
          
          if (active != null) {
            pt = removeFrame(pt, active.getOrigin(), active.getOrientation());
            if (DISPLAY_TEST_OUTPUT) {
              System.out.printf("pt: %s\n", pt.position.toString());
            }
          }
          
          armModel.moveTo(pt.position, pt.orientation);
        } else {
          armModel.moveTo(pt.angles);
        }
      } else {
        println("Position register is uninitialized!");
      }
    }
    
    break;
  default:
    if (mode.type == ScreenType.TYPE_TEACH_POINTS) {
      
      if (shift && teachFrame != null) {
        Point tgt = teachFrame.getPoint(opt_select);
        
        if (tgt != null && tgt.angles != null) {
          // Move the Robot to the select point
          armModel.moveTo(tgt.angles);
        }
      }
    } else if (mode.type == ScreenType.TYPE_TEXT_ENTRY) {
      editTextEntry(3);
    }
    
  }
  
  updateScreen();
}

public void f5() {
  switch(mode) {
    case NAV_PROG_INSTR:
      Instruction i = activeInstruction();
      int selectIdx = getSelectedIdx();
      
      if(selectIdx == 0) {
        nextScreen(Screen.NAV_INSTR_MENU);
      }
      else if(i instanceof MotionInstruction && selectIdx == 3) {
        nextScreen(Screen.VIEW_INST_REG);
      }
      else if(i instanceof IfStatement) {
        IfStatement stmt = (IfStatement)i;
        if(stmt.expr instanceof Expression) {
          ((Expression)stmt.expr).removeElement(selectIdx - 3);
        }
      }
      else if(i instanceof SelectStatement) {
        SelectStatement stmt = (SelectStatement)i;
        if(selectIdx >= 3) {
          stmt.deleteCase((selectIdx - 3)/3);
        }
      }
      else if(i instanceof RegisterStatement) {
        RegisterStatement stmt = (RegisterStatement)i;
        int rLen = (stmt.posIdx == -1) ? 2 : 3;
        if(selectIdx > (rLen + 1) && selectIdx < stmt.expr.getLength() + rLen) {
          stmt.expr.removeElement(selectIdx - (rLen + 2));
        }
      }
      break;
    case TEACH_3PT_USER:
    case TEACH_3PT_TOOL:
    case TEACH_4PT:
    case TEACH_6PT:
      if (shift) {
        // Save the Robot's current position and joint angles
        teachFrame.setPoint(nativeRobotPoint(armModel.getJointAngles()), opt_select);
        saveFrameBytes( new File(sketchPath("tmp/frames.bin")) );
        updateScreen();
      }
      break;
    case CONFIRM_PROG_DELETE:
      opt_select = 0;
      
      lastScreen();
      break;
    case SELECT_INSTR_DELETE:
    case CONFIRM_INSERT:
    case CONFIRM_RENUM:
    case FIND_REPL:
    case SELECT_CUT_COPY:
      display_stack.pop();
      display_stack.pop();
      updateInstructions();
      break;
    case NAV_PREGS_J:
    case NAV_PREGS_C:
      
      if (shift && active_index >= 0 && active_index < GPOS_REG.length) {
        // Save the Robot's current position and joint angles
        Point curRP = nativeRobotEEPoint(armModel.getJointAngles());
        Frame active = getActiveFrame(CoordFrame.USER);
        
        if (active != null) {
          // Save Cartesian values in terms of the active User frame
          curRP = applyFrame(curRP, active.getOrigin(), active.getOrientation());
        } 
  
        GPOS_REG[active_index].point = curRP;
        GPOS_REG[active_index].isCartesian = (mode == Screen.NAV_PREGS_C);
        saveRegisterBytes( new File(sketchPath("tmp/registers.bin")) );
      }
      break;
    default:
       if (mode.type == ScreenType.TYPE_TEXT_ENTRY) {
         editTextEntry(4);
       }
  }
  
  updateScreen();
}

public void editTextEntry(int fIdx) {
  char newChar = letters[fIdx][letterStates[fIdx]];
  if(opt_select == 0 && !(fIdx == 4 && letterStates[fIdx] > 1)) {
      // Use uppercase character
      newChar = (char)(newChar - 32);
  }
  
  StringBuilder temp = new StringBuilder(workingText);
  temp.setCharAt(col_select, newChar);
  workingText = temp.toString();
  
  // Update current letter state
  letterStates[fIdx] = (letterStates[fIdx] + 1) % 6;
  for(int idx = 0; idx < letterStates.length; idx += 1) {
    // Reset all other letter states
    if (idx != fIdx) {
      letterStates[idx] = 0;
    }
  }
}

/* Stops all of the Robot's movement */
public void hd() {
  armModel.halt();
}

public void fd() {  
  if(mode == Screen.NAV_PROG_INSTR && !programRunning && shift) {
    // Stop any prior Robot movement
    armModel.halt();
    // Safeguard against editing a program while it is running
    col_select = 0;
    
    executingInstruction = false;
    // Run single instruction when step is set
    execSingleInst = step;
    programRunning = !executeProgram(activeProgram(), armModel, execSingleInst);
  }
}

public void bd() {
  // If there is a previous instruction, then move to it and reverse its affects
  if(mode == Screen.NAV_PROG_INSTR && !programRunning && shift && step) {
    // Stop any prior Robot movement
    armModel.halt();
    // Safeguard against editing a program while it is running
    col_select = 0;
    // TODO fix backwards
  }
}

public void ENTER() {
  Program p = activeProgram();
  
  switch(mode) {
    //Main menu
    case NAV_MAIN_MENU:
      if(opt_select == 5) { // SETUP
        nextScreen(Screen.NAV_SETUP);
      }
      break;
    //Setup menu
    case NAV_SETUP:
      nextScreen(Screen.SELECT_FRAME_MODE);
      break;
    //Frame nav and edit
    case SELECT_FRAME_MODE:
      if(opt_select == 0) {
        nextScreen(Screen.NAV_TOOL_FRAMES);
      }
      else if(opt_select == 1) {
        nextScreen(Screen.NAV_USER_FRAMES);
      }
      break;
    case ACTIVE_FRAMES:
      updateActiveFramesDisplay();
      break;
    case NAV_TOOL_FRAMES:
      curFrameIdx = contents.get(row_select).itemIdx;
      nextScreen(Screen.TFRAME_DETAIL);
      break;
    case NAV_USER_FRAMES:
      curFrameIdx = contents.get(row_select).itemIdx;
      nextScreen(Screen.UFRAME_DETAIL);
      break;
    case FRAME_METHOD_USER:
      // User Frame teaching methods
      teachFrame = userFrames[curFrameIdx];
      if(opt_select == 0) {
        nextScreen(Screen.TEACH_3PT_USER);
      } 
      else if(opt_select == 1) {
        nextScreen(Screen.TEACH_4PT);
      } 
      else if(opt_select == 2) {
        nextScreen(Screen.DIRECT_ENTRY_USER);
      }
      break;
    case FRAME_METHOD_TOOL:
      teachFrame = toolFrames[curFrameIdx];
      // Tool Frame teaching methods
      if(opt_select == 0) {
        nextScreen(Screen.TEACH_3PT_TOOL);
      } 
      else if(opt_select == 1) {
        nextScreen(Screen.TEACH_6PT);
      } 
      else if(opt_select == 2) {
        nextScreen(Screen.DIRECT_ENTRY_TOOL);
      }
      break;
    case TEACH_3PT_TOOL:
    case TEACH_3PT_USER:
      createFrame(teachFrame, 0);
      lastScreen();
      break;
    case TEACH_4PT:
    case TEACH_6PT:
      createFrame(teachFrame, 1);
      lastScreen();
      break;
    case DIRECT_ENTRY_TOOL:
    case DIRECT_ENTRY_USER:
      // User defined x, y, z, w, p, and r values
      float[] inputs = new float[] { 0f, 0f, 0f, 0f, 0f, 0f };
      
      try {
        // Parse each input value
        for(int val = 0; val < inputs.length; ++val) {
          String str = contents.get(val).get(1);
          
          if(str.length() < 0) {
            // No value entered
            updateScreen();
            println("All entries must have a value!");
            return;
          }
          
          // Remove prefix
          inputs[val] = Float.parseFloat(str);
          // Bring within range of values
          inputs[val] = max(-9999f, min(inputs[val], 9999f));
        }
        
        createFrameDirectEntry(teachFrame, inputs);
      } catch (NumberFormatException NFEx) {
        // Invalid number
        println("Entries must be real numbers!");
        return;
      }
      
      if (teachFrame instanceof UserFrame) {
        nextScreen(Screen.UFRAME_DETAIL);
      } else {
        nextScreen(Screen.TFRAME_DETAIL);
      }
      break;  
      
    //Program nav and edit
    case PROG_CREATE:
      if(!workingText.equals("\0")) {
        if (workingText.charAt(workingText.length() - 1) == '\0') {
          // Remove insert character
          workingText = workingText.substring(0, workingText.length() - 1);
        }
        
        int new_prog = addProgram(new Program(workingText));
        active_prog = new_prog;
        active_instr = 0;
        row_select = 0;
        col_select = 0;
        start_render = 0;
        
        saveProgramBytes( new File(sketchPath("tmp/programs.bin")) );    
        switchScreen(Screen.NAV_PROG_INSTR);
      }
      break;
    case PROG_RENAME:
      if(!workingText.equals("\0")) {
        if (workingText.charAt(workingText.length() - 1) == '\0') {
          // Remove insert character
          workingText = workingText.substring(0, workingText.length() - 1);
        }
        // Renmae the program
        activeProgram().setName(workingText);
        active_instr = 0;
        row_select = 0;
        col_select = 0;
        start_render = 0;
        
        saveProgramBytes( new File(sketchPath("tmp/programs.bin")) );
        resetStack();
        nextScreen(Screen.NAV_PROGRAMS);
      }
      break;
    case PROG_COPY:
      if(!workingText.equals("\0")) {
        if (workingText.charAt(workingText.length() - 1) == '\0') {
          // Remove insert character
          workingText = workingText.substring(0, workingText.length() - 1);
        }
        
        Program newProg = activeProgram().clone();
        newProg.setName(workingText);
        int new_prog = addProgram(newProg);
        active_prog = new_prog;
        active_instr = 0;
        row_select = 0;
        col_select = 0;
        start_render = 0;
        
        saveProgramBytes( new File(sketchPath("tmp/programs.bin")) );    
        resetStack();
        nextScreen(Screen.NAV_PROGRAMS);
      }
      break;
    case NAV_PROGRAMS:
      if(programs.size() != 0) {
        active_instr = 0;
        row_select = 0;
        col_select = 0;
        start_render = 0;
        nextScreen(Screen.NAV_PROG_INSTR);
      }
      break;
      
    //Instruction options menu
    case NAV_INSTR_MENU:
      MotionInstruction m;
  
      switch(opt_select) {
        case 0: //Insert
          nextScreen(Screen.CONFIRM_INSERT);
          break;
        case 1: //Delete
          selectedLines = resetSelection(p.getInstructions().size());
          nextScreen(Screen.SELECT_INSTR_DELETE);
          break;
        case 2: //Cut/Copy
          selectedLines = resetSelection(p.getInstructions().size());
          nextScreen(Screen.SELECT_CUT_COPY);
          break;
        case 3: //Paste
          break;
        case 4: //Find/Replace
          nextScreen(Screen.FIND_REPL);
          break;
        case 5: //Renumber
          nextScreen(Screen.CONFIRM_RENUM);
          break;
        case 6: //Comment
          selectedLines = resetSelection(p.getInstructions().size());
          nextScreen(Screen.SELECT_COMMENT);
          break;
        case 7: //Undo
        case 8: //Remark
      }
      
      break;
      
    //Instruction insert menus
    case SELECT_INSTR_INSERT:
      switch(opt_select){
        case 0: // I/O
          nextScreen(Screen.SELECT_IO_INSTR_REG);
          break;
        case 1: // Offset/Frames
          nextScreen(Screen.SELECT_FRAME_INSTR_TYPE);
          break;
        case 2: //Register 
          nextScreen(Screen.SELECT_REG_STMT);
          break;
        case 3: //IF/ SELECT
          nextScreen(Screen.SELECT_COND_STMT);
          break;
        case 4: //JMP/ LBL
          nextScreen(Screen.SELECT_JMP_LBL);
          break;
        case 5: //Call
          newCallInstruction();
          switchScreen(Screen.SET_CALL_PROG);
          break;
      }
      
      break;
    case SELECT_IO_INSTR_REG:
      newIOInstruction();
      display_stack.pop();
      lastScreen();
      break;
    case SELECT_FRAME_INSTR_TYPE:
      if(opt_select == 0){
        newFrameInstruction(FTYPE_TOOL);
      } else {
        newFrameInstruction(FTYPE_USER);
      }
      
      display_stack.pop();
      switchScreen(Screen.SET_FRAME_INSTR_IDX);
      break;
    case SELECT_REG_STMT:
      display_stack.pop();
      display_stack.pop();
      
      if(opt_select == 0) {
        newRegisterStatement(new DataRegister());
      } else if(opt_select == 1){
        newRegisterStatement(new IORegister());
      } else if(opt_select == 2){
        newRegisterStatement(new PositionRegister());
      } else {
        newRegisterStatement(new PositionRegister(), 0);
        display_stack.push(Screen.SET_REG_EXPR_IDX2);
      }
      
      nextScreen(Screen.SET_REG_EXPR_IDX1);
      break;
    case SELECT_COND_STMT:
      if(opt_select == 0) {
        newIfStatement();
        display_stack.pop();
        switchScreen(Screen.SET_EXPR_OP);
      } else if(opt_select == 1) {
        newIfExpression();
        display_stack.pop();
        lastScreen();
      } else {
        newSelectStatement();
        display_stack.pop();
        lastScreen();
      }
      
      break;
    case SELECT_JMP_LBL:
      display_stack.pop();
    
      if(opt_select == 0) {
        newLabel();
        switchScreen(Screen.SET_LBL_NUM);
      } else {
        newJumpInstruction();
        switchScreen(Screen.SET_JUMP_TGT);
      }
            
      break;
    
    //Movement instruction edit
    case SET_MV_INSTR_TYPE:
      m = activeMotionInst();
      if(opt_select == 0) {
        if(m.getMotionType() != MTYPE_JOINT) m.setSpeed(m.getSpeed()/armModel.motorSpeed);
        m.setMotionType(MTYPE_JOINT);
      } else if(opt_select == 1) {
        if(m.getMotionType() == MTYPE_JOINT) m.setSpeed(armModel.motorSpeed*m.getSpeed());
        m.setMotionType(MTYPE_LINEAR);
      } else if(opt_select == 2) {
        if(m.getMotionType() == MTYPE_JOINT) m.setSpeed(armModel.motorSpeed*m.getSpeed());
        m.setMotionType(MTYPE_CIRCULAR);
      }
      
      lastScreen();
      break;
    case SET_MV_INSTR_REG_TYPE:
      int selIdx = getSelectedIdx();
      m = selIdx < 6 ? activeMotionInst() : activeMotionInst().getSecondaryPoint();
      
      if(opt_select == 0) {
        m.setGlobalPosRegUse(false);
      } 
      else if(opt_select == 1) {  
        if(GPOS_REG[m.positionNum].point == null) {
          // Invalid register index
          err = "This register is uninitailized!";
          return;
        } else {
          m.setGlobalPosRegUse(true);
        }
      }
      lastScreen();
      break;
    case SET_MV_INSTR_SPD:
      selIdx = getSelectedIdx();
      m = selIdx < 6 ? activeMotionInst() : activeMotionInst().getSecondaryPoint();
    
      float tempSpeed = Float.parseFloat(workingText);
      if(tempSpeed >= 5.0) {
        if(speedInPercentage) {
          if(tempSpeed > 100) tempSpeed = 10; 
          tempSpeed /= 100.0;
        } else if(tempSpeed > armModel.motorSpeed) {
          tempSpeed = armModel.motorSpeed;
        }
        
        m.setSpeed(tempSpeed);
        saveProgramBytes( new File(sketchPath("tmp/programs.bin")) );
      }
      
      lastScreen();
      break;
    case SET_MV_INSTR_IDX:
      try {
        int tempRegister = Integer.parseInt(workingText) - 1;
        selIdx = getSelectedIdx();
        m = selIdx < 6 ? activeMotionInst() : activeMotionInst().getSecondaryPoint();
        
        if(tempRegister < 0 || tempRegister > 1000) {
          // Invalid register index
          err = "Only registers 1 - 1000 are legal!";
          lastScreen();
          return;
        }
        
        if(m.isGPosReg) {
          // Check global register
          if(GPOS_REG[tempRegister].point == null) {
            // Invalid register index
            err = "This register is uninitailized!";
            lastScreen();
            return;
          }
        }
        
        m.setPosition(tempRegister);
      } catch (NumberFormatException NFEx) { /* Ignore invalid numbers */ }
      
      lastScreen();
      break;
    case SET_MV_INSTR_TERM:
      try {
        int tempTerm = Integer.parseInt(workingText);
        selIdx = getSelectedIdx();
        m = selIdx < 6 ? activeMotionInst() : activeMotionInst().getSecondaryPoint();
        
        if(tempTerm >= 0 && tempTerm <= 100) {
          m.setTermination(tempTerm);
        }
      } catch (NumberFormatException NFEx) { /* Ignore invalid input */ }
      
      lastScreen();
      break;
      
    //Expression edit
    case SET_EXPR_ARG:
      Expression expr = (Expression)opEdit;
    
      if(opt_select == 0) {
        //set arg to new data reg
        ExprOperand operand = new ExprOperand(new DataRegister(), -1);
        opEdit = expr.setOperand(editIdx, operand);
        switchScreen(Screen.INPUT_DREG_IDX);
      } else if(opt_select == 1) {
        //set arg to new io reg
        ExprOperand operand = new ExprOperand(new IORegister(), -1);
        opEdit = expr.setOperand(editIdx, operand);
        switchScreen(Screen.INPUT_IOREG_IDX);
      } else if(opt_select == 2) {
        ExprOperand operand = new ExprOperand(new PositionRegister(), -1);
        opEdit = expr.setOperand(editIdx, operand);
        switchScreen(Screen.INPUT_PREG_IDX1);
      } else if(opt_select == 3) {
        ExprOperand operand = new ExprOperand(new PositionRegister(), -1, 0);
        opEdit = expr.setOperand(editIdx, operand);
        display_stack.pop();
        display_stack.push(Screen.INPUT_PREG_IDX2);
        nextScreen(Screen.INPUT_PREG_IDX1);
      } else if(opt_select == 4) {
        //set arg to new expression
        Expression oper = new Expression();
        expr.setOperand(editIdx, oper);
        lastScreen();
      } else {
        //set arg to new constant
        println(editIdx);
        opEdit = expr.getOperand(editIdx).reset();
        switchScreen(Screen.INPUT_CONST);
      }
      
      break;
    case SET_BOOL_EXPR_ARG:
      if(opt_select == 0) {
        //set arg to new data reg
        opEdit.set(new DataRegister(), -1);
        switchScreen(Screen.INPUT_DREG_IDX);
      } else if(opt_select == 1) {
        //set arg to new io reg
        opEdit.set(new IORegister(), -1);
        switchScreen(Screen.INPUT_IOREG_IDX);
      } else {
        //set arg to new constant
        opEdit.reset();
        switchScreen(Screen.INPUT_CONST);
      }
      break;
    case SET_IF_STMT_ACT:
      IfStatement stmt = (IfStatement)activeInstruction();
      if(opt_select == 0) {
        stmt.instr = new JumpInstruction();
        switchScreen(Screen.SET_JUMP_TGT);
      } else {
        stmt.instr = new CallInstruction();
        switchScreen(Screen.SET_CALL_PROG);
      }
      
      break;
    case SET_EXPR_OP:
      if(opEdit instanceof Expression) {
        expr = (Expression)opEdit;

        switch(opt_select) {
          case 0:
            expr.setOperator(editIdx, Operator.ADDTN);
            break;
          case 1:
            expr.setOperator(editIdx, Operator.SUBTR);
            break;
          case 2:
            expr.setOperator(editIdx, Operator.MULT);
            break;
          case 3:
            expr.setOperator(editIdx, Operator.DIV);
            break;
          case 4:
            expr.setOperator(editIdx, Operator.INTDIV);
            break;
          case 5:
            expr.setOperator(editIdx, Operator.MOD);
            break;
          case 6:
            expr.setOperator(editIdx, Operator.EQUAL);
            break;
          case 7:
            expr.setOperator(editIdx, Operator.NEQUAL);
            break;
          case 8:
            expr.setOperator(editIdx, Operator.GRTR);
            break;
          case 9:
            expr.setOperator(editIdx, Operator.LESS);
            break;
          case 10:
            expr.setOperator(editIdx, Operator.GREQ);
            break;
          case 11:
            expr.setOperator(editIdx, Operator.LSEQ);
            break;
          case 12:
            expr.setOperator(editIdx, Operator.AND);
            break;
          case 13:
            expr.setOperator(editIdx, Operator.OR);
            break;
          case 14:
            expr.setOperator(editIdx, Operator.NOT);
            break;
        }
      }
      else if(opEdit instanceof BooleanExpression) {
        BooleanExpression boolExpr = (BooleanExpression)opEdit;
        
        switch(opt_select) {
          case 0:
            boolExpr.setOperator(Operator.EQUAL);
            break;
          case 1:
            boolExpr.setOperator(Operator.NEQUAL);
            break;
          case 2:
            boolExpr.setOperator(Operator.GRTR);
            break;
          case 3:
            boolExpr.setOperator(Operator.LESS);
            break;
          case 4:
            boolExpr.setOperator(Operator.GREQ);
            break;
          case 5:
            boolExpr.setOperator(Operator.LSEQ);
            break;
        }
      }
      
      lastScreen();
      break;
    case INPUT_DREG_IDX:
    case INPUT_IOREG_IDX:
    case INPUT_PREG_IDX1:
    case INPUT_PREG_IDX2:
      try {
        int idx = Integer.parseInt(workingText);
        
        if(mode == Screen.INPUT_DREG_IDX) {
          opEdit.set(DREG[idx - 1], idx);
        } else if(mode == Screen.INPUT_IOREG_IDX) {
          opEdit.set(IO_REG[idx - 1], idx);
        } else if(mode == Screen.INPUT_PREG_IDX1) {
          opEdit.set(GPOS_REG[idx - 1], idx);
        } else {
          int reg = opEdit.regIdx;
          opEdit.set(GPOS_REG[reg - 1], reg, idx);
        }
        
      } catch(NumberFormatException e) {}
      
      lastScreen();
      break;
    case INPUT_CONST:
      try{
        float data = Float.parseFloat(workingText);
        opEdit.set(data);
      } catch(NumberFormatException e) {}
      
      lastScreen();
      break;
    case SET_BOOL_CONST:
      if(opt_select == 0) {
        opEdit.set(true);
      } else {
        opEdit.set(false);
      }
      
      lastScreen();
      break;
    
    //Select statement edit
    case SET_SELECT_STMT_ACT:
      SelectStatement s = (SelectStatement)activeInstruction();
      int i = (getSelectedIdx() - 3) / 3;
      
      if(opt_select == 0) {
        s.instrList.set(i, new JumpInstruction());
      } else {
        s.instrList.set(i, new CallInstruction());
      }
      
      lastScreen();
      break;
    case SET_SELECT_STMT_ARG:
      if(opt_select == 0) {
        opEdit.set(new DataRegister(), -1);
      } else {
        opEdit.reset();
      }
      
      nextScreen(Screen.SET_SELECT_ARGVAL);
      break;
    case SET_SELECT_ARGVAL:
      try {
        s = (SelectStatement)activeInstruction();
        float f = Float.parseFloat(workingText);
        
        if(opEdit.type == ExpressionElement.UNINIT) {
          opEdit.set(f);
        } else if(opEdit.type == ExpressionElement.DREG) {
          println(DREG[(int)f - 1].value);
          opEdit.set(DREG[(int)f - 1], (int)f);
        }
      } catch(NumberFormatException ex) {}
      
      display_stack.pop();
      lastScreen();
      break;
    
    //IO instruction edit
    case SET_IO_INSTR_STATE:
      IOInstruction ioInst = (IOInstruction)activeInstruction();
    
      if(opt_select == 0) {
        ioInst.setState(ON);
      } else {
        ioInst.setState(OFF);
      }
      
      lastScreen();
      break;
    case SET_IO_INSTR_IDX:
      try {
        int tempReg = Integer.parseInt(workingText);
        
        if(tempReg >= 0 && tempReg < 6){
          ioInst = (IOInstruction)activeInstruction();
          ioInst.setReg(tempReg);
        }
      }
      catch (NumberFormatException NFEx){ /* Ignore invalid input */ }
      
      lastScreen();
      break;
      
    //Frame instruction edit
    case SET_FRM_INSTR_TYPE:
      FrameInstruction fInst = (FrameInstruction)activeInstruction();
      
      if(opt_select == 0)
        fInst.setFrameType(FTYPE_TOOL);
      else
        fInst.setFrameType(FTYPE_USER);
        
      lastScreen();
      break;      
    case SET_FRAME_INSTR_IDX:
      try {
        int frameIdx = Integer.parseInt(workingText) - 1;
        
        if(frameIdx >= -1 && frameIdx < min(toolFrames.length, userFrames.length)){
          fInst = (FrameInstruction)activeInstruction();
          fInst.setReg(frameIdx);
        }
      }
      catch (NumberFormatException NFEx){ /* Ignore invalid input */ }
      
      lastScreen();
      break;
      
    //Register statement edit
    case SET_REG_EXPR_TYPE:
      RegisterStatement regStmt = (RegisterStatement)activeInstruction();
      display_stack.pop();
      
      if(opt_select == 0) {
        regStmt.setRegister(new DataRegister());
      } else if(opt_select == 1) {
        regStmt.setRegister(new IORegister());
      } else if(opt_select == 2) {
        regStmt.setRegister(new PositionRegister());
      } else {
        regStmt.setRegister(new PositionRegister(), 0);
        display_stack.push(Screen.SET_REG_EXPR_IDX2);
      }
      
      nextScreen(Screen.SET_REG_EXPR_IDX1);
      break;
    case SET_REG_EXPR_IDX1:
      try {
        int idx = Integer.parseInt(workingText);
        
        if (idx < 1 || idx > 1000) {
          println("Invalid register index!");
        } else {
          regStmt = (RegisterStatement)activeInstruction(); 
          if(regStmt.reg instanceof DataRegister) {
            (regStmt).setRegister(DREG[idx - 1]);
          } else if(regStmt.reg instanceof IORegister) {
            (regStmt).setRegister(IO_REG[idx - 1]);
          } else if(regStmt.reg instanceof PositionRegister && regStmt.posIdx == -1) { 
            (regStmt).setRegister(GPOS_REG[idx - 1]);
          } else {
            (regStmt).setRegister(GPOS_REG[idx - 1], 0);
          }
        }
      }
      catch (NumberFormatException NFEx){ /* Ignore invalid input */ }
      
      lastScreen();
      break;
    case SET_REG_EXPR_IDX2:
       try {
         int idx = Integer.parseInt(workingText);
         
         if (idx < 1 || idx > 6) {
           println("Invalid position index!"); 
         } else {
           regStmt = (RegisterStatement)activeInstruction();
           if(regStmt.reg instanceof PositionRegister) {
             regStmt.posIdx = idx;
           }
         }
       } catch (NumberFormatException NFEx){ /* Ignore invalid input */ }
       
       lastScreen();
       break;
    
    //Jump/ Label instruction edit
    case SET_LBL_NUM:
      try {
        int idx = Integer.parseInt(workingText);
        
        if (idx < 0 || idx > 99) {
          println("Invalid label index!");
        } else {
          ((LabelInstruction)activeInstruction()).labelNum = idx;
        }
      }
      catch (NumberFormatException NFEx){ /* Ignore invalid input */ }
      
      lastScreen();
      break;
    case SET_JUMP_TGT:
      try {
        int lblNum = Integer.parseInt(workingText);
        int lblIdx = p.findLabelIdx(lblNum);
        
        if(activeInstruction() instanceof IfStatement) {
          IfStatement ifStmt = (IfStatement)activeInstruction();
          ((JumpInstruction)ifStmt.instr).tgtLblNum = lblNum;
        } 
        else if(activeInstruction() instanceof SelectStatement) {
          SelectStatement sStmt = (SelectStatement)activeInstruction();
          ((JumpInstruction)sStmt.instrList.get(editIdx)).tgtLblNum = lblNum;
        }
        else {
          if(lblIdx != -1) {
            JumpInstruction jmp = (JumpInstruction)activeInstruction();
            jmp.tgtLblNum = lblNum;
          } else {
            err = "Invalid label number.";
          }
        }
      }
      catch (NumberFormatException NFEx){ /* Ignore invalid input */ }
      
      lastScreen();
      break;
    
    //Call instruction edit
    case SET_CALL_PROG:
      if(activeInstruction() instanceof IfStatement) {
        IfStatement ifStmt = (IfStatement)activeInstruction();
        ((CallInstruction)ifStmt.instr).callProg = programs.get(row_select);
        ((CallInstruction)ifStmt.instr).progIdx = opt_select;
      }
      else if(activeInstruction() instanceof SelectStatement) {
        SelectStatement sStmt = (SelectStatement)activeInstruction();
        CallInstruction c = (CallInstruction)sStmt.instrList.get(editIdx);
        c.callProg = programs.get(row_select);
        c.progIdx = row_select;
      }
      else {
        CallInstruction call = (CallInstruction)activeInstruction();
        call.callProg = programs.get(row_select);
        call.progIdx = row_select;
      }
      
      lastScreen();
      break;
      
    //Program instruction editing and navigation
    case SELECT_CUT_COPY:
    case SELECT_INSTR_DELETE:
      selectedLines[active_instr] = !selectedLines[active_instr];
      updateScreen();
      break;
    case SELECT_COMMENT:
      activeInstruction().toggleCommented();
      
      updateScreen(); 
      break;
    case VIEW_INST_REG:
      displayPoint = null;
      lastScreen();
      break;
    case FIND_REPL:
      lastScreen();  
      break;
    case JUMP_TO_LINE:
      int jumpToInst = Integer.parseInt(workingText) - 1;
      active_instr = max(0, min(jumpToInst, activeProgram().getInstructions().size() - 1));
      
      lastScreen();
      break;
    case SWAP_PT_TYPE:
      if(opt_select == 0) {
        // View Cartesian values
        nextScreen(Screen.NAV_PREGS_C);
      } else if(opt_select == 1) {
        // View Joint values
        nextScreen(Screen.NAV_PREGS_J);
      }
      break;
      
    //Register navigation/ edit
    case NAV_DATA:
      if(opt_select == 0) {
        // Data Register Menu
        nextScreen(Screen.NAV_DREGS);
      } else if(opt_select == 1) {
        // Position Register Menu
        nextScreen(Screen.NAV_PREGS_C);
      }
      break;
    case CP_DREG_COM:
      int regIdx = -1;
      
      try {
        // Copy the comment of the curent Data register to the Data register at the specified index
        regIdx = Integer.parseInt(workingText) - 1;
        DREG[regIdx].comment = DREG[active_index].comment;
        saveRegisterBytes( new File(sketchPath("tmp/registers.bin")) );
      } catch (NumberFormatException MFEx) {
        println("Only real numbers are valid!");
      } catch (IndexOutOfBoundsException IOOBEx) {
        println("Only positve integers between 0 and 100 are valid!");
      }
      
      lastScreen();
      break;
    case CP_DREG_VAL:
      regIdx = -1;
      
      try {
        // Copy the value of the curent Data register to the Data register at the specified index
        regIdx = Integer.parseInt(workingText) - 1;
        DREG[regIdx].value = DREG[active_index].value;
        saveRegisterBytes( new File(sketchPath("tmp/registers.bin")) );
      } catch (NumberFormatException MFEx) {
        println("Only real numbers are valid!");
      } catch (IndexOutOfBoundsException IOOBEx) {
        println("Only positve integers between 0 and 100 are valid!");
      }
      
      lastScreen();
      break;
    case CP_PREG_COM:
      regIdx = -1;
      
      try {
        // Copy the comment of the curent Position register to the Position register at the specified index
        regIdx = Integer.parseInt(workingText) - 1;
        GPOS_REG[regIdx].comment = GPOS_REG[active_index].comment;
        saveRegisterBytes( new File(sketchPath("tmp/registers.bin")) );
      } catch (NumberFormatException MFEx) {
        println("Only real numbers are valid!");
      } catch (IndexOutOfBoundsException IOOBEx) {
        println("Only positve integers between 0 and 100 are valid!");
      }
      
      lastScreen();
      break;
    case CP_PREG_PT:
      regIdx = -1;
      
      try {
        // Copy the point of the curent Position register to the Position register at the specified index
        regIdx = Integer.parseInt(workingText) - 1;
        GPOS_REG[regIdx].point = GPOS_REG[active_index].point.clone();
        saveRegisterBytes( new File(sketchPath("tmp/registers.bin")) );
      } catch (NumberFormatException MFEx) {
        println("Only real numbers are valid!");
      } catch (IndexOutOfBoundsException IOOBEx) {
        println("Only positve integers between 0 and 100 are valid!");
      }
      
      lastScreen();
      break;
    case EDIT_DREG_VAL:   
      Float f = null;
      
      try {
        // Read inputted Float value
        f = Float.parseFloat(workingText);
        // Clamp the value between -9999 and 9999, inclusive
        f = max(-9999f, min(f, 9999f));
        System.out.printf("Index; %d\n", active_index);
        if(active_index >= 0 && active_index < DREG.length) {
          // Save inputted value
          DREG[active_index].value = f;
          saveRegisterBytes( new File(sketchPath("tmp/registers.bin")) );
        }
      } catch (NumberFormatException NFEx) {
        // Invalid input value
        println("Value must be a real number!");
      }
      
      lastScreen();
      break;
    case NAV_DREGS:
      if(col_select == 0) {
        // Edit register comment
        nextScreen(Screen.EDIT_DREG_COM);
      } else if(col_select >= 1) {
        // Edit Data Register value
        nextScreen(Screen.EDIT_DREG_VAL);
      }
      break;
    case NAV_PREGS_J:
    case NAV_PREGS_C:   
      if(col_select == 0) {
        // Edit register comment
        nextScreen(Screen.EDIT_PREG_COM);
      } else if(col_select >= 1) {
        // Edit Position Register value
        nextScreen((mode == (Screen.NAV_PREGS_C)) ? Screen.EDIT_PREG_C : Screen.EDIT_PREG_J);
      }
      break;
    case EDIT_PREG_C:
      createRegisterPoint(false);  
      lastScreen();
      break;
    case EDIT_PREG_J:      
      createRegisterPoint(true);
      lastScreen();
      break;
    case EDIT_PREG_COM:
      if (!workingText.equals("\0")) {
        if(workingText.charAt(  workingText.length() - 1  ) == '\0') {
          workingText = workingText.substring(0, workingText.length() - 1);
        }
        // Save the inputted comment to the selected register
        GPOS_REG[active_index].comment = workingText;
        saveRegisterBytes( new File(sketchPath("tmp/registers.bin")) );
        workingText = "";
        lastScreen();
      }
      break;
    case EDIT_DREG_COM:
      if (!workingText.equals("\0")) {
        if(workingText.charAt(  workingText.length() - 1  ) == '\0') {
          workingText = workingText.substring(0, workingText.length() - 1);
        }
        // Save the inputted comment to the selected register\
        DREG[active_index].comment = workingText;
        saveRegisterBytes( new File(sketchPath("tmp/registers.bin")) );
        workingText = "";
        lastScreen();
      }
      break;
  }
}//End enter

public void ITEM() {
  if(mode == Screen.NAV_PROG_INSTR) {
    opt_select = 0;
    workingText = "";
    nextScreen(Screen.JUMP_TO_LINE);
  }
}

public void BKSPC() {
  if(mode.type == ScreenType.TYPE_NUM_ENTRY) {
    // Functions as a backspace key
    if(workingText.length() > 1) {
      workingText = workingText.substring(0, workingText.length() - 1);
    } 
    else {
      workingText = "";
    }
    
  } else if(mode.getType() == ScreenType.TYPE_POINT_ENTRY) {
    
    // backspace function for current row
    if(row_select >= 0 && row_select < contents.size()) {
      String value = contents.get(row_select).get(1);
      
      if (value.length() == 1) {
        contents.get(row_select).set(1, "");
      } else if(value.length() > 1) {
        
        if (value.length() == 2 && value.charAt(0) == '-') {
          // Do not remove line prefix until the last digit is removed
          contents.get(row_select).set(1, "");
        } else {
          contents.get(row_select).set(1, value.substring(0, value.length() - 1));
        }
      }
    }
  } else if(mode.type == ScreenType.TYPE_TEXT_ENTRY) {
    // Backspace function
    if(workingText.length() > 1) {
      // ifan insert space exists, preserve it
      if(workingText.charAt(workingText.length() - 1) == '\0') {
        workingText = workingText.substring(0, workingText.length() - 2) + "\0";
      } 
      else {
        workingText = workingText.substring(0, workingText.length() - 1);
      }
      
      col_select = min(col_select, workingText.length() - 1);
    } else {
      workingText = "\0";
    }
    
    for(int idx = 0; idx < letterStates.length; ++idx) { letterStates[idx] = 0; }
  }
  
  updateScreen();
}

public void COORD() {
  if(shift) {
    nextScreen(Screen.ACTIVE_FRAMES);
  } else {  
    // Update the coordinate mode
    coordFrameTransition();
    updateScreen();
  }
}

public void SPEEDUP() {
  // Increase the speed at which the Robot jogs
  if (shift) {
    
    if (liveSpeed < 5) {
      liveSpeed = 5;
    } else if (liveSpeed < 50) {
      liveSpeed = 50;
    } else {
      liveSpeed = 100;
    }
  } else if (liveSpeed < 100) {
    
    if (liveSpeed < 5) {
      ++liveSpeed;
    } else if (liveSpeed < 50) {
      liveSpeed += 5;
    } else if (liveSpeed < 100) {
      liveSpeed += 10f;
    }
  }
  
  // The Robot's speed multiplier is bounded to the range 1% to 100%
  liveSpeed = min(liveSpeed, 100);
}


public void SLOWDOWN() {
  // Reduce the speed at which the Robot jogs
  if (shift) {
    
    if (liveSpeed > 50) {
      liveSpeed = 50;
    } else if (liveSpeed > 5) {
      liveSpeed = 5;
    } else {
      liveSpeed = 1;
    }
  } else if (liveSpeed > 1) {
    
    if (liveSpeed <= 5f) {
      --liveSpeed;
    } else if (liveSpeed <= 50) {
      liveSpeed -= 5;
    } else {
      liveSpeed -= 10;
    }
  }
  
  // The Robot's speed multiplier is bounded to the range 1% to 100%
  liveSpeed = max(1, liveSpeed);
}

public void EE() {
  armModel.cycleEndEffector();
}

public void JOINT1_NEG() {
  updateRobotJogMotion(0, -1);
}

public void JOINT1_POS() {
  updateRobotJogMotion(0, 1);
}

public void JOINT2_NEG() {
  updateRobotJogMotion(1, -1);
}

public void JOINT2_POS() {
  updateRobotJogMotion(1, 1);
}

public void JOINT3_NEG() {
  if (shift) {
    updateRobotJogMotion(2, -1);
  }
}

public void JOINT3_POS() {
  updateRobotJogMotion(2, 1);
}

public void JOINT4_NEG() {
  updateRobotJogMotion(3, -1);
}

public void JOINT4_POS() {
  updateRobotJogMotion(3, 1);
}

public void JOINT5_NEG() {
  updateRobotJogMotion(4, -1);
}

public void JOINT5_POS() {
  updateRobotJogMotion(4, 1);
}

public void JOINT6_NEG() {updateRobotJogMotion(5, -1);
}

public void JOINT6_POS() {
  updateRobotJogMotion(5, 1);
}

public void updateRobotJogMotion(int button, int direction) {
  // Only six jog button pairs exist
  if (button >= 0 && button < 6) {
    float newDir;
    
    if(curCoordFrame == CoordFrame.JOINT) {
      // Move single joint
      newDir = activateLiveJointMotion(button, direction);
    } else {
      // Move entire robot in a single axis plane
      newDir = activateLiveWorldMotion(button, direction);
    }
    
    Button negButton = ((Button)cp5.get("JOINT" + (button + 1) + "_NEG")),
           posButton = ((Button)cp5.get("JOINT" + (button + 1) + "_POS"));
    
    if (newDir > 0) {
      // Positive motion
      negButton.setColorBackground(BUTTON_DEFAULT);
      posButton.setColorBackground(BUTTON_ACTIVE);
    } else if (newDir < 0) {
      // Negative motion
      negButton.setColorBackground(BUTTON_ACTIVE);
      posButton.setColorBackground(BUTTON_DEFAULT);
    } else {
      // No motion
      negButton.setColorBackground(BUTTON_DEFAULT);
      posButton.setColorBackground(BUTTON_DEFAULT);
    }
  }
}

/**
 * Updates the motion of one of the Robot's joints based on
 * the joint index given and the value of dir (-/+ 1). The
 * Robot's joint indices range from 0 to 5. ifthe joint
 * Associate with the given index is already in motion,
 * in either direction, then calling this method for that
 * joint index will stop that joint's motion.
 * 
 * @returning  The new motion direction of the Robot
 */
public float activateLiveJointMotion(int joint, int dir) {
  
  if (!shift || robotFault) {
    // Only move when shift is set and there is no error
    return 0f;
  }
  
  if(armModel.segments.size() >= joint+1) {

    Model model = armModel.segments.get(joint);
    // Checks all rotational axes
    for(int n = 0; n < 3; n++) {
      
      if(model.rotations[n]) {
        
        if(model.jointsMoving[n] == 0) {
          model.jointsMoving[n] = dir;
          return dir;
        } else {
          model.jointsMoving[n] = 0;
        }
      }
    }
  }
  
  return 0f;
}

/**
 * Updates the motion of the Robot with respect to one of the World axes for
 * either linear or rotational motion around the axis. Similiar to the
 * activateLiveJointMotion() method, calling this method for an axis, in which
 * the Robot is already moving, will result in the termination of the Robot's
 * motion in that axis. Rotational and linear motion for an axis are mutually
 * independent in this regard.
 * 
 * @param axis        The axis of movement for the robotic arm:
                      x - 0, y - 1, z - 2, w - 3, p - 4, r - 5
 * @pararm dir        +1 or -1: indicating the direction of motion
 * @returning         The new direction of motion in the given axis
 *
 */
public float activateLiveWorldMotion(int axis, int dir) {
  if (!shift || robotFault) {
    // Only move when shift is set and there is no error
    return 0f;
  }
  
  // Initiaize the Robot's destination
  Point RP = nativeRobotEEPoint(armModel.getJointAngles());
  armModel.tgtPosition = RP.position;
  armModel.tgtOrientation = RP.orientation;
  
  
  if(axis >= 0 && axis < 3) {
    if(armModel.jogLinear[axis] == 0) {
      // Begin movement on the given axis in the given direction
      armModel.jogLinear[axis] = dir;
    } else {
      // Halt movement
      armModel.jogLinear[axis] = 0;
    }
    
    return armModel.jogLinear[axis];
  }
  else if(axis >= 3 && axis < 6) {
    axis %= 3;
    if(armModel.jogRot[axis] == 0) {
      // Begin movement on the given axis in the given direction
      armModel.jogRot[axis] = dir;
    }
    else {
      // Halt movement
      armModel.jogRot[axis] = 0;
    }
    
    return armModel.jogRot[axis];
  }
  
  return 0f;
}

//turn of highlighting on all active movement buttons
public void resetButtonColors() {
  for(int i = 1; i <= 6; i += 1) {
    ((Button)cp5.get("JOINT"+i+"_NEG")).setColorBackground(BUTTON_DEFAULT);
    ((Button)cp5.get("JOINT"+i+"_POS")).setColorBackground(BUTTON_DEFAULT);
  }
}

/**
 * Transitions the display to the given screen and pushes that screen
 * onto the stack.
 * 
 * @param next    The new screen mode
 */
public void nextScreen(Screen next) {
  // Stop a program from executing when transition screens
  programRunning = false;
  if (DISPLAY_TEST_OUTPUT) { System.out.printf("%s => %s\n", mode, next);  }
  
  mode = next;
  display_stack.push(mode);
  loadScreen();
}

/**
 * Transitions to the given screen without saving the current screen on the stack.
 * 
 * @param nextScreen  The new screen mode
 */
public void switchScreen(Screen nextScreen) {
  // Stop a program from executing when transition screens
  programRunning = false;
  if (DISPLAY_TEST_OUTPUT) { System.out.printf("%s => %s\n", mode, nextScreen);  }
  
  mode = nextScreen;
  display_stack.pop();
  display_stack.push(mode);
  loadScreen();
}

/**
 * Transitions the display to the previous screen that the user was on.
 */
public boolean lastScreen() {
  // Stop a program from executing when transition screens
  programRunning = false;
  if (display_stack.peek() == Screen.DEFAULT) {
    if (DISPLAY_TEST_OUTPUT) { System.out.printf("%s\n", mode); }
    return false;
  }
  else{
    display_stack.pop();
    if (DISPLAY_TEST_OUTPUT) { System.out.printf("%s => %s\n", mode, display_stack.peek()); }
    mode = display_stack.peek();
    
    loadScreen();
    return true;
  }
}

public void resetStack(){
  // Stop a program from executing when transition screens
  programRunning = false;
  display_stack.clear();
  
  mode = Screen.DEFAULT;
  display_stack.push(mode);
}

public void loadScreen() {  
  switch(mode){
    //Main menu
    case NAV_MAIN_MENU:
      opt_select = 0;
      break;
      
    //Frames
    case NAV_SETUP:
      opt_select = 0;
      break;
    case ACTIVE_FRAMES:
      row_select = 0;
      col_select = 1;
      workingText = Integer.toString(activeToolFrame + 1);
      break;
    case SELECT_FRAME_MODE:
      active_index = 0;
      opt_select = 0;
      break;
    case NAV_TOOL_FRAMES:
    case NAV_USER_FRAMES:
      row_select = active_index*2;
      col_select = 0;
      break;
    case TFRAME_DETAIL:
    case UFRAME_DETAIL:
      row_select = -1;
      col_select = -1;
      start_render = 0;
      opt_select = -1;
      break;
    case TEACH_3PT_TOOL:
    case TEACH_3PT_USER:
    case TEACH_4PT:
    case TEACH_6PT:
      opt_select = 0;
      break;
    case FRAME_METHOD_TOOL:
    case FRAME_METHOD_USER:
      row_select = -1;
      col_select = -1;
      opt_select = 0;
      break;
    
    //Programs and instructions
    case NAV_PROGRAMS:
      // Stop Robot movement (i.e. program execution)
      armModel.halt();
      row_select = active_prog;
      col_select = 0;
      active_instr = 0;
      break;
    case PROG_CREATE:
      row_select = 1;
      col_select = 0;
      opt_select = 0;
      workingText = "\0";
      break;
    case PROG_RENAME:
      active_prog = row_select;
      row_select = 1;
      col_select = 0;
      opt_select = 0;
      workingText = activeProgram().getName();
      break;
    case PROG_COPY:
      active_prog = row_select;
      row_select = 1;
      col_select = 0;
      opt_select = 0;
      workingText = "\0";
      break;
    case NAV_PROG_INSTR:
      //need to enforce row/ column select limits based on 
      //program length/ instruction width
      if(prev_select != -1) {
        row_select = prev_select;
        start_render = row_select;
        prev_select = -1;
      }
      opt_select = -1;
      break;
    case SET_CALL_PROG:
      prev_select = row_select;
      row_select = 0;
      col_select = 0;
      start_render = 0;
      break;
    case CONFIRM_INSERT:
      workingText = "";
      break;
    case SELECT_INSTR_INSERT:
    case SELECT_JMP_LBL:
    case SELECT_REG_STMT:
    case SELECT_COND_STMT:
    case SET_IF_STMT_ACT:
    case SET_SELECT_STMT_ACT:
    case SET_SELECT_STMT_ARG:
    case SET_EXPR_ARG:
    case SET_BOOL_EXPR_ARG:
    case SET_EXPR_OP:
      opt_select = 0;
      break;
    case INPUT_DREG_IDX:
    case INPUT_IOREG_IDX:
    case INPUT_CONST:
      workingText = "";
      break;
    case SET_IO_INSTR_IDX:
    case SET_JUMP_TGT:
    case SET_LBL_NUM:
      col_select = 1;
      opt_select = 0;
      workingText = ""; //<>//
      break;
    case SET_MV_INSTR_TYPE:
      MotionInstruction mInst = activeMotionInst();
      
      switch (mInst.getMotionType()) {
        case MTYPE_JOINT:
          opt_select = 0;
          break;
        case MTYPE_LINEAR:
          opt_select = 1;
          break;
        case MTYPE_CIRCULAR:
          opt_select = 2;
          break;
        default:
          opt_select = -1;
      }
      
      break;
    case SET_MV_INSTR_SPD:
      mInst = activeMotionInst();
      int instSpd;
      // Convert speed into an integer value
      if (mInst.motionType == MTYPE_JOINT) {
        instSpd = Math.round(mInst.speed * 100f);
      } else {
       instSpd = Math.round(mInst.speed);
      }
      
      workingText = Integer.toString(instSpd);
    case SET_MV_INSTR_REG_TYPE:
      mInst = activeMotionInst();
      
      if (mInst.usesGPosReg()) {
        opt_select = 1;
      } else {
        opt_select = 0;
      }
      
      break;
    case SET_MV_INSTR_IDX:
      mInst = activeMotionInst();
      workingText = Integer.toString(mInst.getPosition());
      break;
    case SET_MV_INSTR_TERM:
      mInst = activeMotionInst();
      workingText = Integer.toString(mInst.getTermination());
      break;
    case SET_FRAME_INSTR_IDX:
    case SET_SELECT_ARGVAL:
    case SET_REG_EXPR_IDX1:
    case SET_REG_EXPR_IDX2:
      opt_select = 0;
      workingText = "";
      break;
    case SET_IO_INSTR_STATE:
    case SET_FRM_INSTR_TYPE:
    case SET_REG_EXPR_TYPE:
      opt_select = 0;
      break;
    case SELECT_INSTR_DELETE:
    case SELECT_COMMENT:
    case SELECT_CUT_COPY:
      int size = contents.size() - 1;
      row_select = max(0, min(row_select, size));
      col_select = 0;
      break;
    
    //Registers
    case NAV_DATA:
      opt_select = 0;
      active_index = 0;
      break;
    case NAV_DREGS:
    case NAV_PREGS_J:
    case NAV_PREGS_C:
      row_select = active_index;
      col_select = 0;
      break;
    case DIRECT_ENTRY_TOOL:
    case DIRECT_ENTRY_USER:
      row_select = 0;
      col_select = 1;
      contents = loadFrameDirectEntry(teachFrame);
      options = new ArrayList<String>();
      break;
    case NAV_INSTR_MENU:
      opt_select = 0;
      break;
    case VIEW_INST_REG:
      opt_select = -1;
      break;
    case SWAP_PT_TYPE:
      opt_select = 0;
      break;
    case CP_DREG_COM:
    case CP_DREG_VAL:
    case CP_PREG_COM:
    case CP_PREG_PT:
      opt_select = 1;
      workingText = Integer.toString((active_index + 1));
      break;
    case EDIT_DREG_COM:
      row_select = 1;
      col_select = 0;
      opt_select = 0;
      
      if(DREG[active_index].comment != null) {
        workingText = DREG[active_index].comment;
      }
      else {
        workingText = "\0";
      }
      break;   
    case EDIT_PREG_COM:
      row_select = 1;
      col_select = 0;
      opt_select = 0;
      
      if(GPOS_REG[active_index].comment != null) {
        workingText = GPOS_REG[active_index].comment;
      }
      else {
        workingText = "\0";
      }
      break;
    case EDIT_DREG_VAL:
      opt_select = 0;
      // Bring up float input menu
      if(DREG[active_index].value != null) {
        workingText = Float.toString(DREG[active_index].value);
      } else {
        workingText = "";
      }
      break;
    case EDIT_PREG_C:
    case EDIT_PREG_J:
      row_select = 0;
      col_select = 1;
      contents = loadPosRegEntry(GPOS_REG[active_index]);
      break;
    default:
      break;
  }
  
  updateScreen();
}

// update text displayed on screen
public void updateScreen() {
  int next_px = display_px;
  int next_py = display_py;
  int txt, bg;
  
  clearScreen();
  
  // draw display background
  cp5.addTextarea("txt")
  .setPosition(display_px,display_py)
  .setSize(display_width, display_height)
  .setColorBackground(UI_LIGHT)
  .moveTo(g1);
  
  String header = null;
  // display the name of the program that is being edited
  header = getHeader(mode);
    
  if(header != null) {
    // Display header field
    cp5.addTextarea("header")
    .setText(" " + header)
    .setFont(fnt_con14)
    .setPosition(next_px, next_py)
    .setSize(display_width, 20)
    .setColorValue(UI_LIGHT)
    .setColorBackground(UI_DARK)
    .hideScrollbar()
    .show()
    .moveTo(g1);
  }
  
  contents = getContents(mode);
  options = getOptions(mode);
  
  boolean selectMode = (mode.getType() == ScreenType.TYPE_LINE_SELECT);
    
  /*************************
   *    Display Contents   *
   *************************/
  
  if(contents.size() > 0) {
    row_select = clamp(row_select, 0, contents.size() - 1);
    col_select = clamp(col_select, 0, contents.get(row_select).size() - 1);
    start_render = clamp(start_render, row_select - (ITEMS_TO_SHOW - 1), row_select);
  }
  
  index_contents = 1;
  for(int i = start_render; i < contents.size() && i - start_render < ITEMS_TO_SHOW; i += 1) {
    //get current line
    DisplayLine temp = contents.get(i);
    next_px = display_px + temp.xAlign;
    next_py += 20;
        
    if(i == row_select) { bg = UI_DARK; }
    else                { bg = UI_LIGHT;}
    
    //if(i == 0 || contents.get(i - 1).itemIdx != contents.get(i).itemIdx) {
    //  //leading row select indicator []
    //  cp5.addTextarea(Integer.toString(index_contents))
    //  .setText("")
    //  .setPosition(next_px, next_py)
    //  .setSize(10, 20)
    //  .setColorBackground(bg)
    //  .hideScrollbar()
    //  .moveTo(g1);
    //}
    
    //index_contents++;
    //next_px += 10;
    
    //draw each element in current line
    for(int j = 0; j < temp.size(); j += 1) {
      if(i == row_select) {
        if(j == col_select && !selectMode){
          //highlight selected row + column
          txt = UI_LIGHT;
          bg = UI_DARK;          
        } 
        else if(selectMode && !selectedLines[contents.get(i).itemIdx]){
          //highlight selected line
          txt = UI_LIGHT;
          bg = UI_DARK;
        }
        else {
          txt = UI_DARK;
          bg = UI_LIGHT;
        }
      } else if(selectMode && selectedLines[contents.get(i).itemIdx]) {
        //highlight any currently selected lines
        txt = UI_LIGHT;
        bg = UI_DARK;
      } else {
        //display normal row
        txt = UI_DARK;
        bg = UI_LIGHT;
      }
      
      //grey text for comme also this
      if(temp.size() > 0 && temp.get(0).contains("//")) {
        txt = color(127);
      }
      
      cp5.addTextarea(Integer.toString(index_contents))
      .setText(temp.get(j))
      .setFont(fnt_con14)
      .setPosition(next_px, next_py)
      .setSize(temp.get(j).length()*8 + 20, 20)
      .setColorValue(txt)
      .setColorBackground(bg)
      .hideScrollbar()
      .moveTo(g1);
      
      index_contents++;
      next_px += temp.get(j).length() * 8 + 20; 
    }//end draw line elements
        
    if(i == row_select) { txt = UI_DARK;  }
    else                { txt = UI_LIGHT; }
    
    ////Trailing row select indicator []
    //cp5.addTextarea(Integer.toString(index_contents))
    //.setText("")
    //.setPosition(next_px, next_py)
    //.setSize(10, 20)
    //.setColorBackground(txt)
    //.hideScrollbar()
    //.moveTo(g1);
    
    //index_contents += 1;
  }//end display contents
  
  // display options for an element being edited
  next_px = display_px;
  next_py += contents.size() == 0 ? 20 : 40;
  
  int maxHeight;
  if(mode.getType() == ScreenType.TYPE_EXPR_EDIT) {
    maxHeight = 4;
  } else {
    maxHeight = options.size();
  }
  
  /*************************
   *    Display Options    *
   *************************/
  
  index_options = 100;
  for(int i = 0; i < options.size(); i += 1) {
    if(i == opt_select) {
      txt = UI_LIGHT;
      bg = UI_DARK;
    }
    else {
      txt = UI_DARK;
      bg = UI_LIGHT;
    }
    
    cp5.addTextarea(Integer.toString(index_options))
    .setText(" " + options.get(i))
    .setFont(fnt_con14)
    .setPosition(next_px, next_py)
    .setSize(options.get(i).length()*8 + 40, 20)
    .setColorValue(txt)
    .setColorBackground(bg)
    .hideScrollbar()
    .moveTo(g1);
    
    index_options++;
    next_px += (i % maxHeight == maxHeight - 1) ? 80 : 0;
    next_py += (i % maxHeight == maxHeight - 1) ? -20*(maxHeight - 1) : 20;    
  }
  
  // display the numbers that the user has typed
  next_py += 20;
  index_nums = 1000;
  for(int i = 0; i < nums.size(); i+= 1) {
    if(nums.get(i) == -1) {
      cp5.addTextarea(Integer.toString(index_nums))
      .setText(".")
      .setFont(fnt_con14)
      .setPosition(next_px, next_py)
      .setSize(40, 20)
      .setColorValue(UI_LIGHT)
      .setColorBackground(color(255, 0, 0))
      .hideScrollbar()
      .moveTo(g1);
    }
    else {
      cp5.addTextarea(Integer.toString(index_nums))
      .setText(Integer.toString(nums.get(i)))
      .setFont(fnt_con14)
      .setPosition(next_px, next_py)
      .setSize(40, 20)
      .setColorValue(UI_LIGHT)
      .setColorBackground(color(255, 0, 0))
      .hideScrollbar()
      .moveTo(g1);
    }
    
    index_nums++;
    next_px += options.get(i).length()*8 + 20;   
  }
  
  // display hints for function keys
  String[] funct;
  funct = getFunctionLabels(mode);
    
  //set f button text labels
  for(int i = 0; i < 5; i += 1) {
    cp5.addTextarea("lf"+i)
    .setText(funct[i])
    .setFont(fnt_con12)
     // Keep function labels in their original place
    .setPosition(display_width*i/5 + 15 , display_height - g1_py)
    .setSize(display_width/5 - 5, 20)
    .setColorValue(UI_DARK)
    .setColorBackground(UI_LIGHT)
    .hideScrollbar()
    .moveTo(g1);
  }
} // end updateScreen()

/*Text generation methods*/

//Header text
public String getHeader(Screen mode){
  String header = null;
  
  switch(mode) {
    case NAV_PROGRAMS:
      header = "PROGRAMS";
      break;
    case PROG_CREATE:
      header = "NAME PROGRAM";
      break;
    case PROG_RENAME:
      header = "RENAME PROGRAM";
      break;
    case PROG_COPY:
      header = "COPY PROGRAM";
      break;
    case CONFIRM_INSERT:
    case CONFIRM_RENUM:
    case NAV_PROG_INSTR:
    case NAV_INSTR_MENU:
    case SET_MV_INSTR_SPD:
    case SET_MV_INSTR_IDX:
    case SET_MV_INSTR_TERM:
    case SELECT_INSTR_INSERT:
    case SET_IO_INSTR_STATE:
    case SET_FRM_INSTR_TYPE:
    case SET_FRAME_INSTR_IDX:
    case SET_EXPR_ARG:
    case SET_BOOL_EXPR_ARG:
    case SET_JUMP_TGT:
    case SELECT_CUT_COPY:    
    case SELECT_INSTR_DELETE:
    case VIEW_INST_REG:
      header = activeProgram().getName();
      break;
    case SELECT_IO_INSTR_REG:
      header = "SELECT IO REGISTER";
      break;
    case SELECT_FRAME_INSTR_TYPE:
      header = "SELECT FRAME INSTRUCTION TYPE";
      break;
    case SELECT_COND_STMT:
      header = "INSERT IF/ SELECT STATEMENT";
      break;
    case SELECT_JMP_LBL:
      header = "INSERT JUMP/ LABEL INSTRUCTION";
      break;
    case SET_CALL_PROG:
      header = "SELECT CALL TARGET";
      break;
    case ACTIVE_FRAMES:
      header = "ACTIVE FRAMES";
      break;
    case NAV_TOOL_FRAMES:
      header = "TOOL FRAMES";
      break;
    case NAV_USER_FRAMES:
      header = "USER FRAMES";
      break;
    case TFRAME_DETAIL:
      header = String.format("TOOL FRAME: %d", curFrameIdx + 1);
      break;
    case UFRAME_DETAIL:
      header = String.format("USER FRAME: %d", curFrameIdx + 1);
      break;
    case FRAME_METHOD_TOOL:
      header = String.format("TOOL FRAME: %d", curFrameIdx + 1);
      break;
    case FRAME_METHOD_USER:
      header = String.format("USER FRAME: %d", curFrameIdx + 1);
      break;
    case TEACH_3PT_TOOL:
    case TEACH_3PT_USER:
      header = "THREE POINT METHOD";
      break;
    case TEACH_4PT:
      header = "FOUR POINT METHOD";
      break;
    case TEACH_6PT:
      header = "SIX POINT METHOD";
      break;
    case DIRECT_ENTRY_TOOL:
    case DIRECT_ENTRY_USER:
      header = "DIRECT ENTRY METHOD";
      break;
    case NAV_DATA:
      header = "VIEW REGISTERS";
      break;
    case NAV_DREGS:
    case CP_DREG_COM:
    case CP_DREG_VAL:
      header = "REGISTERS";
      break;
    case NAV_PREGS_J:
      header = "POSTION REGISTERS (J)";
      break;
    case NAV_PREGS_C:
      header = "POSTION REGISTERS (C)";
      break;
    case CP_PREG_COM:
    case CP_PREG_PT:
    case SWAP_PT_TYPE:
      header = "POSTION REGISTERS";
      break;
    case EDIT_DREG_VAL:
      header = "REGISTERS";
      break;
    case EDIT_PREG_C:
    case EDIT_PREG_J:
      header = "POSITION REGISTER: ";
      
      if(mode != Screen.EDIT_DREG_COM && GPOS_REG[active_index].comment != null) {
        // Show comment if it exists
        header += GPOS_REG[active_index].comment;
      } 
      else {
        header += active_index;
      }
      break;
    case EDIT_DREG_COM:
      header = String.format("Enter a name for R[%d]", active_index);
      break;
    case EDIT_PREG_COM:
      header = String.format("Enter a name for PR[%d]", active_index);
      break;
    default:
      break;
  }
  
  return header;
}

//Main display content text
public ArrayList<DisplayLine> getContents(Screen mode){
  ArrayList<DisplayLine> contents = new ArrayList<DisplayLine>();
  
  switch(mode) {
    //Program list navigation/ edit
    case NAV_PROGRAMS:
    case SET_CALL_PROG:
      contents = loadPrograms();
      break;
    
    case PROG_CREATE:
    case PROG_RENAME:
    case PROG_COPY:
      contents = loadTextInput();
      break;
    
    //View instructions
    case CONFIRM_INSERT:
    case CONFIRM_RENUM:
    case FIND_REPL:
    case NAV_PROG_INSTR:
    case VIEW_INST_REG:
    case SELECT_INSTR_DELETE:
    case SELECT_COMMENT:
    case SELECT_CUT_COPY:
    case SET_MV_INSTR_TYPE:
    case SET_MV_INSTR_REG_TYPE:
    case SET_MV_INSTR_IDX:
    case SET_MV_INSTR_SPD:
    case SET_MV_INSTR_TERM:
    case SET_IO_INSTR_STATE:
    case SET_IO_INSTR_IDX:
    case SET_FRM_INSTR_TYPE:
    case SET_FRAME_INSTR_IDX:
    case SET_REG_EXPR_TYPE:
    case SET_REG_EXPR_IDX1:
    case SET_REG_EXPR_IDX2:
    case SET_IF_STMT_ACT:
    case SET_SELECT_STMT_ARG:
    case SET_SELECT_ARGVAL:
    case SET_SELECT_STMT_ACT:
    case SET_EXPR_ARG:
    case SET_BOOL_EXPR_ARG:
    case SET_EXPR_OP:
    case INPUT_DREG_IDX:
    case INPUT_IOREG_IDX:
    case INPUT_CONST:
    case SET_BOOL_CONST:
    case SET_LBL_NUM:
    case SET_JUMP_TGT:
      contents = loadInstructions(active_prog);
      break;
      
    case ACTIVE_FRAMES:
      /* workingText corresponds to the active row's index display */
      if (row_select == 0) {
        contents.add(newLine("Tool: ", workingText));
        contents.add(newLine("User: ", Integer.toString(activeUserFrame + 1)));
      } else {
        contents.add(newLine("Tool: ", Integer.toString(activeToolFrame + 1)));
        contents.add(newLine("User: ", workingText));
      }
      break;
      
    //View frame details
    case NAV_TOOL_FRAMES:
      contents = loadFrames(CoordFrame.TOOL);
      break;
    case NAV_USER_FRAMES:
      contents = loadFrames(CoordFrame.USER);
      break;
    //View frame details
    case TFRAME_DETAIL:
    case TEACH_3PT_TOOL:
    case TEACH_6PT:
      contents = loadFrameDetail(CoordFrame.TOOL);
      break;
    case UFRAME_DETAIL:
    case TEACH_3PT_USER:
    case TEACH_4PT:
      contents = loadFrameDetail(CoordFrame.USER);
      break;
    case FRAME_METHOD_TOOL:
    case FRAME_METHOD_USER:
    case DIRECT_ENTRY_USER:
    case DIRECT_ENTRY_TOOL:
    case EDIT_DREG_VAL:
    case CP_DREG_COM:
    case CP_DREG_VAL:
    case CP_PREG_COM:
    case CP_PREG_PT:
    case SWAP_PT_TYPE:
      contents = this.contents;
      break;
      
    //View/ edit registers
    case NAV_DREGS:
    case NAV_PREGS_C:
    case NAV_PREGS_J:
      contents = loadRegisters();
      break;
    case EDIT_PREG_C:
    case EDIT_PREG_J:
      contents = this.contents;
      break;
    case EDIT_DREG_COM:
    case EDIT_PREG_COM:
      contents = loadTextInput();
      break;
     
    default:
      break;
  }
  
  return contents;
}
//Options menu text
public ArrayList<String> getOptions(Screen mode){
  ArrayList<String> options = new ArrayList<String>();
  
  switch(mode) {
    //Main menu and submenus
    case NAV_MAIN_MENU:
      options.add("1 UTILITIES (NA)"   );
      options.add("2 TEST CYCLE (NA)"  );
      options.add("3 MANUAL FCTNS (NA)");
      options.add("4 ALARM (NA)"       );
      options.add("5 I/O (NA)"         );
      options.add("6 SETUP"            );  
      options.add("7 FILE (NA)"        );
      options.add("8 USER (NA)"        );
      break;
    case NAV_SETUP:
      options.add("1 Prog Select (NA)" );
      options.add("2 General (NA)"     );
      options.add("3 Call Guard (NA)"  );
      options.add("4 Frames"           );
      options.add("5 Macro (NA)"       );
      options.add("6 Ref Position (NA)");
      options.add("7 Port Init (NA)"   );
      options.add("8 Ovrd Select (NA)" );
      options.add("9 User Alarm (NA)"  );
      options.add("0 --NEXT--"         );
      break;
      
    case CONFIRM_PROG_DELETE:
      options.add("Delete selected program?");
      break;
    
    //Instruction options
    case NAV_INSTR_MENU:
      options.add("1 Insert"           );
      options.add("2 Delete"           );
      options.add("3 Cut/ Copy"        );
      options.add("4 Paste (NA)"       );
      options.add("5 Find/ Replace"    );
      options.add("6 Renumber"         );
      options.add("7 Comment"          );
      options.add("8 Undo (NA)"        );
      options.add("9 Remark"           );
      break;
    case CONFIRM_INSERT:
      options.add("Enter number of lines to insert:");
      options.add("\0" + workingText);
      break;
    case SELECT_INSTR_DELETE:
      options.add("Select lines to delete (ENTER).");
      break;
    case SELECT_CUT_COPY:
      options.add("Select lines to cut/ copy (ENTER).");
      break;
    case FIND_REPL:
      options.add("Enter text to search for:");
      options.add("\0" + workingText);
      break;
    case CONFIRM_RENUM: 
      options.add("Renumber program positions?");
      break;
    case SELECT_COMMENT:
      options.add("Select lines to comment/uncomment.");
      break;
      
    //Instruction edit options
    case SET_MV_INSTR_TYPE:
    case SET_MV_INSTR_REG_TYPE:
    case SET_MV_INSTR_IDX:
    case SET_MV_INSTR_SPD:
    case SET_MV_INSTR_TERM:
    case SET_IO_INSTR_STATE:
    case SET_IO_INSTR_IDX:
    case SET_FRM_INSTR_TYPE:
    case SET_FRAME_INSTR_IDX:
    case SET_REG_EXPR_TYPE:
    case SET_REG_EXPR_IDX1:
    case SET_REG_EXPR_IDX2:
    case SET_IF_STMT_ACT:
    case SET_SELECT_STMT_ARG:
    case SET_SELECT_ARGVAL:
    case SET_SELECT_STMT_ACT:
    case SET_EXPR_ARG:
    case SET_BOOL_EXPR_ARG:
    case SET_EXPR_OP:
    case INPUT_DREG_IDX:
    case INPUT_IOREG_IDX:
    case INPUT_CONST:
    case SET_BOOL_CONST:
    case SET_LBL_NUM:
    case SET_JUMP_TGT:
      options = loadInstrEdit(mode);
      break;
    
    //Insert instructions (non-movemet)
    case SELECT_INSTR_INSERT:
      options.add("1. I/O"       );
      options.add("2. Frames"    );
      options.add("3. Registers" );
      options.add("4. IF/SELECT" );
      options.add("5. JMP/LBL"   );
      options.add("6. CALL"      );
      options.add("7. WAIT (NA)"      );
      options.add("8. Macro (NA)"     );
      break;
    case SELECT_IO_INSTR_REG:
      options = loadIORegisters();
      break;
    case SELECT_FRAME_INSTR_TYPE:
      options.add("1. TFRAME_NUM = ...");
      options.add("2. UFRAME_NUM = ...");
      break;
    case SELECT_REG_STMT:
      options.add("1. R[x] = (...)");
      options.add("2. IO[x] = (...)");
      options.add("3. PR[x] = (...)");
      options.add("4. PR[x, y] = (...)");
      break;
    case SELECT_COND_STMT:
      options.add("1. IF Stmt");
      options.add("2. IF (...)");
      options.add("3. SELECT Stmt");
      break;
    case SELECT_JMP_LBL:
      options.add("1. LBL[...]");
      options.add("2. JMP LBL[...]");
      break;
      
    //Frame navigation and edit menus
    case SELECT_FRAME_MODE:
      options.add("1. Tool Frame");
      options.add("2. User Frame");
      break;
    case FRAME_METHOD_TOOL:
      options.add("1. Three Point Method");
      options.add("2. Six Point Method");
      options.add("3. Direct Entry Method");
      break;
    case FRAME_METHOD_USER:
      options.add("1. Three Point Method");
      options.add("2. Four Point Method");
      options.add("3. Direct Entry Method");
      break;
    case VIEW_INST_REG:
      options = loadInstructionReg();
      break;
    
    case TEACH_3PT_TOOL:
    case TEACH_3PT_USER:
    case TEACH_4PT:
    case TEACH_6PT:
      options = loadPointList();
      break;
    //Data navigation and edit menus
    case NAV_DATA:
      options.add("1. Data Registers");
      options.add("2. Position Registers");
      break;
    case NAV_PREGS_J:
    case NAV_PREGS_C:
      opt_select = -1;
      // Display the point with the Position register of the highlighted line, when viewing the Position registers
      if (active_index >= 0 && active_index < GPOS_REG.length && GPOS_REG[active_index].point != null) {
        String[] pregEntry = GPOS_REG[active_index].point.toLineStringArray(mode == Screen.NAV_PREGS_C);
        
        for (String line : pregEntry) {
          options.add(line);
        }
      }
      
      break;
    case CP_DREG_COM:
      options.add(String.format("Move R[%d]'s comment to:", active_index + 1));
      options.add(String.format("R[%s]", workingText));
      break;
    case CP_DREG_VAL:
      options.add(String.format("Move R[%d]'s value to:", active_index + 1));
      options.add(String.format("R[%s]", workingText));
      break;
    case CP_PREG_COM:
      options.add(String.format("Move PR[%d]'s comment to:", active_index + 1));
      options.add(String.format("PR[%s]", workingText));
      break;
    case CP_PREG_PT:
      options.add(String.format("Move PR[%d]'s point to:", active_index + 1));
      options.add(String.format("PR[%s]", workingText));
      break;
    case SWAP_PT_TYPE:
      options.add("1. Cartesian");
      options.add("2. Joint");
      break;
    case EDIT_DREG_VAL:
      options.add("Input register value:");
      options.add("\0" + workingText);
      break;
    case DIRECT_ENTRY_TOOL:
    case DIRECT_ENTRY_USER:
    case EDIT_PREG_C:
    case EDIT_PREG_J:
      options = this.options;
      break;
    case EDIT_RSTMT:
      options.add("Register");
      options.add("Position Register Point");
      options.add("Position Register Value");
      break;
    
    //Misc functions
    case JUMP_TO_LINE:
      options.add("Use number keys to enter line number to jump to");
      options.add("\0" + workingText);
      break;
    default:
      if (mode.type == ScreenType.TYPE_TEXT_ENTRY) {
        options.add("1. Uppercase");
        options.add("1. Lowercase");
      }
  }

  return options;
}

//Function label text
public String[] getFunctionLabels(Screen mode){
  String[] funct = new String[5];
  
  switch(mode) {
    case NAV_PROGRAMS:
      // F2, F3
      funct[0] = "[Create]";
      if(programs.size() > 0) {
        funct[1] = "[Rename]";
        funct[2] = "[Delete]";
        funct[3] = "[Copy]";
        funct[4] = "";
      } else {
        funct[1] = "";
        funct[2] = "";
        funct[3] = "";
        funct[4] = "";
      }
      break;
    case NAV_PROG_INSTR:
      // F1, F4, F5f
      funct[0] = shift ? "[New Pt]" : "";
      funct[1] = "[New Ins]";
      funct[2] = "";
      funct[3] = "[Edit]";
      funct[4] = (col_select == 0) ? "[Opt]" : "";
      if(activeInstruction() instanceof MotionInstruction) {
        funct[4] = (col_select == 3) ? "[Reg]" : funct[4];
      } 
      else if(activeInstruction() instanceof IfStatement) {
        IfStatement stmt = (IfStatement)activeInstruction();
        int selectIdx = getSelectedIdx();
        
        if(stmt.expr instanceof Expression) {
          if(selectIdx > 1 && selectIdx < stmt.expr.getLength() + 1) {
            funct[2] = "[Insert]";
          }
          if(selectIdx > 2 && selectIdx < stmt.expr.getLength() + 1) {
            funct[4] = "[Delete]";
          }
        }
      } else if(activeInstruction() instanceof SelectStatement) {
        int selectIdx = getSelectedIdx();
        
        if(selectIdx >= 3) {
          funct[2] = "[Insert]";
          funct[4] = "[Delete]";
        }
      } else if(activeInstruction() instanceof RegisterStatement) {
        RegisterStatement stmt = (RegisterStatement)activeInstruction();
        int rLen = (stmt.posIdx == -1) ? 2 : 3;
        int selectIdx = getSelectedIdx();

        if(selectIdx > rLen && selectIdx < stmt.expr.getLength() + rLen) {
          funct[2] = "[Insert]";
        }
        if(selectIdx > (rLen + 1) && selectIdx < stmt.expr.getLength() + rLen) {
          funct[4] = "[Delete]";
        }
      }
      break;
    case SELECT_COMMENT:
      funct[0] = "";
      funct[1] = "";
      funct[2] = "";
      funct[3] = "[Done]";
      funct[4] = "";
      break;
    case SELECT_CUT_COPY:
      funct[0] = "";
      funct[1] = "";
      funct[2] = "[Cut]";
      funct[3] = "[Copy]";
      funct[4] = "[Cancel]";
      break;
    case NAV_TOOL_FRAMES:
    case NAV_USER_FRAMES:
      // F1, F2, F3
      if(shift) {
        funct[0] = "[Clear]";
        funct[1] = "";
        funct[2] = "[Switch]";
        funct[3] = "";
        funct[4] = "";
      } else {
        funct[0] = "[Set]";
        funct[1] = "";
        funct[2] = "[Switch]";
        funct[3] = "";
        funct[4] = "";
      }
      break;
    case TFRAME_DETAIL:
    case UFRAME_DETAIL:
      // F2
      funct[0] = "";
      funct[1] = "[Method]";
      funct[2] = "";
      funct[3] = "";
      funct[4] = "";
      break;
    case TEACH_3PT_TOOL:
    case TEACH_3PT_USER:
    case TEACH_4PT:
    case TEACH_6PT:
      // F1, F5
      funct[0] = "";
      funct[1] = "[Method]";
      funct[2] = "";
      funct[3] = "[Mov To]";
      funct[4] = "[Record]";
      break;
    case DIRECT_ENTRY_TOOL:
    case DIRECT_ENTRY_USER:
      funct[0] = "";
      funct[1] = "[Method]";
      funct[2] = "";
      funct[3] = "";
      funct[4] = "";
      break;
    case ACTIVE_FRAMES:
      // F1, F2
      funct[0] = "[List]";
      funct[1] = "";
      funct[2] = "";
      funct[3] = "";
      funct[4] = "";
      break;
    case NAV_PREGS_C:
    case NAV_PREGS_J:
      // F1 - F5
      if (shift) {
        funct[0] = "[Clear]";
        funct[1] = "[Copy]";
        funct[2] = "[Type]";
        funct[3] = "[Move To]";
        funct[4] = "[Record]";
      } else {
        funct[0] = "[Clear]";
        funct[1] = "[Copy]";
        funct[2] = "[Switch]";
        funct[3] = "[Move To]";
        funct[4] = "[Record]";
      }
     break;
    case NAV_DREGS:
      // F1 - F3
      funct[0] = "[Clear]";
      funct[1] = "[Copy]";
      funct[2] = "[Switch]";
      funct[3] = "";
      funct[4] = "";
      break;
    case CONFIRM_INSERT:
    case CONFIRM_PROG_DELETE:
    case CONFIRM_RENUM:
    case FIND_REPL:
    case SELECT_INSTR_DELETE:
      // F4, F5
      funct[0] = "";
      funct[1] = "";
      funct[2] = "";
      funct[3] = "[Confirm]";
      funct[4] = "[Cancel]";
      break;
    default:
      if (mode.type == ScreenType.TYPE_TEXT_ENTRY) {
        if(opt_select == 0) {
          // F1 - F5
          funct[0] = "[ABCDEF]";
          funct[1] = "[GHIJKL]";
          funct[2] = "[MNOPQR]";
          funct[3] = "[STUVWX]";
          funct[4] = "[YZ_@*.]";
        } else {
          funct[0] = "[abcdef]";
          funct[1] = "[ghijkl]";
          funct[2] = "[mnopqr]";
          funct[3] = "[stuvwx]";
          funct[4] = "[yz_@*.]";
        }
      } else {
        funct[0] = "";
        funct[1] = "";
        funct[2] = "";
        funct[3] = "";
        funct[4] = "";
      }
      break;
  }
  
  return funct;
}

/*
 * Removes all text on screen and prepares the UI to transition to a
 * new screen display.
 */
public void clearScreen() {
  //remove all text labels on screen  
  List<Textarea> displayText = cp5.getAll(Textarea.class);
  for(Textarea t: displayText) {
    // ONLY remove text areas from the Pendant!
    if (t.getParent().equals(g1)) {
      cp5.remove(t.getName());
    }
  }
  
  cp5.update();
}

public void clearContents() {
  for(int i = 0; i < index_contents; i += 1) {
    if(cp5.getGroup(Integer.toString(i)) != null)
    cp5.getGroup(Integer.toString(i)).remove();
  }
  
  index_contents = 0;
}

public void clearOptions() {
  for(int i = 100; i < index_options; i += 1) {
    if(cp5.getGroup(Integer.toString(i)) != null)
    cp5.getGroup(Integer.toString(i)).remove();
  }
  
  index_options = 100;
}

public void clearNums() {
  for(int i = 1000; i < index_nums; i += 1) {
    if(cp5.getGroup(Integer.toString(i)) != null)
    cp5.getGroup(Integer.toString(i)).remove();
  }
  
  index_nums = 1000;
}

ArrayList<DisplayLine> loadPrograms() {
  ArrayList<DisplayLine> progs = new ArrayList<DisplayLine>();
  int size = programs.size();
   
  //int start = start_render;
  //int end = min(start + ITEMS_TO_SHOW, size);
  
  for(int i = 0; i < size; i += 1) {
    progs.add(newLine(i, programs.get(i).getName()));
  }
  
  return progs;
}

// prepare for displaying motion instructions on screen
public ArrayList<DisplayLine> loadInstructions(int programID) {
  ArrayList<DisplayLine> instruct_list = new ArrayList<DisplayLine>();
  
  Program p = programs.get(programID);
  int size = p.getInstructions().size();
    
  for(int i = 0; i < size; i+= 1) {
    DisplayLine line = new DisplayLine(i);
    Instruction instr = p.getInstruction(i);
    int xPos = 10;
    
    // Add line number
    if(instr.isCommented()) {
      line.add("//"+Integer.toString(i+1) + ")");
      xPos += 52;
    } else {
      line.add(Integer.toString(i+1) + ")");
      xPos += 36;
    }
    
    if(instr instanceof MotionInstruction) {
      // Show '@' at the an instrution, if the Robot's position is close to that position stored in the instruction's register
      MotionInstruction a = (MotionInstruction)instr;
      
      Point ee_point = nativeRobotEEPoint(armModel.getJointAngles());
      Point instPt = a.getVector(p);
      
      if(instPt != null && ee_point.position.dist(instPt.position) < (liveSpeed / 100f)) {
        line.add("@");
      }
      else {
        line.add("\0");
      }
      
      xPos += 28;
    }
    
    String[] fields = instr.toStringArray();
    
    for (int j = 0; j < fields.length; j += 1) {
      String field = fields[j];
      xPos += field.length()*8 + 20;
      
      if(xPos > display_width) {
        instruct_list.add(line);
        xPos = 36;
        
        line = new DisplayLine(i, xPos);
        field = ": " + field;
        xPos += field.length()*8 + 20;
      } else if(field.equals("\n") && j != fields.length - 1) {
        instruct_list.add(line);
        if(instr instanceof SelectStatement) {
          xPos = 148;
        } else {
          xPos = 84;
        }
        
        line = new DisplayLine(i, xPos);
        xPos += field.length()*8 + 20;
      }
      
      if(!field.equals("\n")) {
        line.add(field);
      }
    }
    
    instruct_list.add(line);
  }
  
  if(mode.getType() != ScreenType.TYPE_LINE_SELECT) {
    instruct_list.add(newLine(size, "[End]"));
  }
  
  return instruct_list;
}

/**
 * Deals with updating the UI after confirming/canceling a deletion
 */
public void updateInstructions() {
  int instSize = activeProgram().getInstructions().size();
  
  active_instr = min(active_instr,  instSize - 1);
  row_select = min(active_instr, ITEMS_TO_SHOW - 1);
  col_select = 0;
  start_render = active_instr - row_select;
  
  lastScreen();
}

public void getInstrEdit(Instruction ins, int selectIdx) {
  if(ins instanceof MotionInstruction) {
    switch(selectIdx) {
      case 2: // motion type
        nextScreen(Screen.SET_MV_INSTR_TYPE);
        break;
      case 3: // register type
        nextScreen(Screen.SET_MV_INSTR_REG_TYPE);
        break;
      case 4: // register
        nextScreen(Screen.SET_MV_INSTR_IDX);
        break;
      case 5: // speed
        nextScreen(Screen.SET_MV_INSTR_SPD);
        break;
      case 6: // termination type
        nextScreen(Screen.SET_MV_INSTR_TERM);
        break;
      case 7: //circular motion secondary point edit
        nextScreen(Screen.SET_MV_INSTR_REG_TYPE);
        break;
      case 8: 
        nextScreen(Screen.SET_MV_INSTR_IDX);
        break;
      case 9:
        nextScreen(Screen.SET_MV_INSTR_SPD);
        break;
      case 10:
        nextScreen(Screen.SET_MV_INSTR_TERM);
        break;
    }
  }
  else if(ins instanceof FrameInstruction) {
    switch(selectIdx) {
      case 1:
        nextScreen(Screen.SET_FRM_INSTR_TYPE);
        break;
      case 2:
        nextScreen(Screen.SET_FRAME_INSTR_IDX);
        break;
    }
  }
  else if(ins instanceof IOInstruction) {
     switch(selectIdx) {
      case 1:
        nextScreen(Screen.SET_IO_INSTR_IDX);
        break;
      case 2:
        nextScreen(Screen.SET_IO_INSTR_STATE);
        break;
    }
  }
  else if(ins instanceof LabelInstruction){
    nextScreen(Screen.SET_LBL_NUM);
  }
  else if(ins instanceof JumpInstruction){
    nextScreen(Screen.SET_JUMP_TGT);
  }
  else if(ins instanceof CallInstruction){
    nextScreen(Screen.SET_CALL_PROG);
  } 
  else if(ins instanceof IfStatement){
    IfStatement stmt = (IfStatement)ins;
    
    if(stmt.expr instanceof Expression) {
      int len = stmt.expr.getLength();
      
      if(selectIdx >= 3 && selectIdx < len + 1) {
        editExpression((Expression)stmt.expr, 3);
      } else if(selectIdx == len + 2) {
        nextScreen(Screen.SET_IF_STMT_ACT);
      } else if(selectIdx == len + 3) {
        if(stmt.instr instanceof JumpInstruction) {
          nextScreen(Screen.SET_JUMP_TGT);
        } else {
          nextScreen(Screen.SET_CALL_PROG);
        }
      }
    } 
    else if(stmt.expr instanceof BooleanExpression) {
      if(selectIdx == 2) {
        opEdit = ((BooleanExpression)stmt.expr).getArg1();
        nextScreen(Screen.SET_BOOL_EXPR_ARG);
      } else if(selectIdx == 3) {
        opEdit = stmt.expr;
        nextScreen(Screen.SET_EXPR_OP);
      } else if(selectIdx == 4){
        opEdit = ((BooleanExpression)stmt.expr).getArg2();
        nextScreen(Screen.SET_BOOL_EXPR_ARG);
      } else if(selectIdx == 5){
        nextScreen(Screen.SET_IF_STMT_ACT);
      } else {
        if(stmt.instr instanceof JumpInstruction) {
          nextScreen(Screen.SET_JUMP_TGT);
        } else {
          nextScreen(Screen.SET_CALL_PROG);
        }
      }
    }
  } 
  else if(ins instanceof SelectStatement) {
    SelectStatement stmt = (SelectStatement)ins;
    
    if(selectIdx == 2) {
      opEdit = stmt.arg;
      nextScreen(Screen.SET_SELECT_STMT_ARG);
    } else if((selectIdx - 3) % 3 == 0 && selectIdx > 2) {
      opEdit = stmt.cases.get((selectIdx - 3)/3);
      nextScreen(Screen.SET_SELECT_STMT_ARG);
    } else if((selectIdx - 3) % 3 == 1) {
      editIdx = (selectIdx - 3)/3;
      nextScreen(Screen.SET_SELECT_STMT_ACT);
    } else if((selectIdx - 3) % 3 == 2) {
      editIdx = (selectIdx - 3)/3;
      if(stmt.instrList.get(editIdx) instanceof JumpInstruction) {
        nextScreen(Screen.SET_JUMP_TGT);
      } else if(stmt.instrList.get(editIdx) instanceof CallInstruction) {
        nextScreen(Screen.SET_CALL_PROG);
      }
    }
  } else if(ins instanceof RegisterStatement) {
    RegisterStatement stmt = (RegisterStatement)ins;
    int len = stmt.expr.getLength();
    int rLen = (stmt.posIdx == -1) ? 2 : 3;
    
    if(selectIdx == 1) {
      nextScreen(Screen.SET_REG_EXPR_TYPE);
    } else if(selectIdx == 2) {
      nextScreen(Screen.SET_REG_EXPR_IDX1);
    } else if(selectIdx == 3 && stmt.posIdx != -1) {
      nextScreen(Screen.SET_REG_EXPR_IDX2);
    } else if(selectIdx >= rLen + 1 && selectIdx <= len + rLen) {
      editExpression(stmt.expr, selectIdx - (rLen + 2));
    }
  }
}

public void editExpression(Expression expr, int selectIdx) {
  int[] elements = expr.mapToEdit();
  opEdit = expr;
  ExpressionElement e = expr.get(elements[selectIdx]);
  
  if(e instanceof Expression) {
    //if selecting the open or close paren
    if(selectIdx == 0 || selectIdx == e.getLength() || 
    elements[selectIdx - 1] != elements[selectIdx] || 
    elements[selectIdx + 1] != elements[selectIdx]) {
      nextScreen(Screen.SET_EXPR_ARG);
    } else {
      int startIdx = expr.getStartingIdx(elements[selectIdx]);
      editExpression((Expression)e, selectIdx - startIdx - 1);
    }
  } else if(e instanceof ExprOperand) {
    editOperand((ExprOperand)e, elements[selectIdx]);
  } else {
    editIdx = elements[selectIdx];
    nextScreen(Screen.SET_EXPR_OP);
  }
}

/**
 * Accepts an ExpressionOperand object and forwards the UI to the appropriate
 * menu to edit said object based on the operand type.
 *
 * @param o - The operand to be edited.
 * @ins_idx - The index of the operand's container ExpressionElement list into which this
 *     operand is stored.
 *
 */
public void editOperand(ExprOperand o, int ins_idx) {
  switch(o.type) {
    case -2: //Uninit
      editIdx = ins_idx;
      nextScreen(Screen.SET_EXPR_ARG);
      break;
    case 0: //Float const
      opEdit = o;
      nextScreen(Screen.INPUT_CONST);
      break;
    case 1: //Bool const
      opEdit = o;
      nextScreen(Screen.SET_BOOL_CONST);
      break;
    case 2: //Data reg
      opEdit = o;
      nextScreen(Screen.INPUT_DREG_IDX);
      break;
    case 3: //IO reg
      opEdit = o;
      switchScreen(Screen.INPUT_IOREG_IDX);
      break;
  }
}

public ArrayList<String> loadInstrEdit(Screen mode) {
  ArrayList<String> edit = new ArrayList<String>();
  
  switch(mode){
    case SET_MV_INSTR_TYPE:
      edit.add("1.JOINT");
      edit.add("2.LINEAR");
      edit.add("3.CIRCULAR");
      break;
    case SET_MV_INSTR_REG_TYPE:
      edit.add("1.LOCAL(P)");
      edit.add("2.GLOBAL(PR)");
      break;
    case SET_MV_INSTR_IDX:
      edit.add("Enter desired register number (1-1000)");
      edit.add("\0" + workingText);
      break;
    case SET_MV_INSTR_SPD:
      edit.add("Enter desired speed");
      MotionInstruction castIns = activeMotionInst();
      
      if(castIns.getMotionType() == MTYPE_JOINT) {
        speedInPercentage = true;
        workingTextSuffix = "%";
      } else {
        workingTextSuffix = "mm/s";
        speedInPercentage = false;
      }
      
      edit.add(workingText + workingTextSuffix);
      break;
    case SET_MV_INSTR_TERM:
      edit.add("Enter desired termination %(0-100):");
      edit.add("\0" + workingText);
      break;
    case SET_IO_INSTR_STATE:
      edit.add("1. ON");
      edit.add("2. OFF");
      break;
    case SET_IO_INSTR_IDX:
      edit.add("Select I/O register index:");
      edit.add("\0" + workingText);
      break;
    case SET_FRM_INSTR_TYPE:
      edit.add("1. TFRAME_NUM = x");
      edit.add("2. UFRAME_NUM = x");
      break;
    case SET_FRAME_INSTR_IDX:
      edit.add("Select frame index:");
      edit.add("\0" + workingText);
      break;
    case SET_REG_EXPR_TYPE:
      edit.add("1. R[x] = (...)");
      edit.add("2. IO[x] = (...)");
      edit.add("3. PR[x] = (...)");
      edit.add("4. PR[x, y] = (...)");
      break;
    case SET_REG_EXPR_IDX1:
      edit.add("Select register index:");
      edit.add("\0" + workingText);
      break;
    case SET_REG_EXPR_IDX2:
      edit.add("Select point index:");
      edit.add("\0" + workingText);
      break;
    case SET_EXPR_OP:
      if(opEdit instanceof BooleanExpression) {
        edit.add("1. ... =  ...");
        edit.add("2. ... <> ...");
        edit.add("3. ... >  ...");
        edit.add("4. ... <  ...");
        edit.add("5. ... >= ...");
        edit.add("6. ... <= ...");
      } else if(opEdit instanceof Expression) {
        if(activeInstruction() instanceof IfStatement) {
          edit.add("1. + ");
          edit.add("2. - ");
          edit.add("3. * ");
          edit.add("4. / ");
          edit.add("5. | ");
          edit.add("6. % ");
          edit.add("7. = ");
          edit.add("8. <> ");
          edit.add("9. > ");
          edit.add("10. < ");
          edit.add("11. >= ");
          edit.add("12. <= ");
          edit.add("13. AND ");
          edit.add("14. OR ");
          edit.add("15. NOT ");
          edit.add("16. ... ");
        } else {
          edit.add("1. + ");
          edit.add("2. - ");
          edit.add("3. * ");
          edit.add("4. / ");
          edit.add("5. | ");
          edit.add("6. % ");
        }
      }
      break;
    case SET_EXPR_ARG:
    case SET_BOOL_EXPR_ARG:
      edit.add("R[x]");
      edit.add("IO[x]");
      if(opEdit instanceof Expression) {
        edit.add("PR[x]");
        edit.add("PR[x, y]");
        edit.add("(...)");
      }
      edit.add("Const");
      break;
    case INPUT_DREG_IDX:
    case INPUT_IOREG_IDX:
    case INPUT_PREG_IDX1:
      edit.add("Input register index:");
      edit.add("\0" + workingText);
      break;
    case INPUT_PREG_IDX2:
      edit.add("Input position value index:");
      edit.add("\0" + workingText);
      break;
    case INPUT_CONST:
      edit.add("Input constant value:");
      edit.add("\0" + workingText);
      break;
    case SET_BOOL_CONST:
      edit.add("1. False");
      edit.add("2. True");
      break;
    case SET_IF_STMT_ACT:
    case SET_SELECT_STMT_ACT:
      edit.add("JMP LBL[x]");
      edit.add("CALL");
      break;
    case SET_SELECT_STMT_ARG:
      edit.add("R[x]");
      edit.add("Const");
      break;
    case SET_SELECT_ARGVAL:
      edit.add("Input value/ register index:");
      edit.add("\0" + workingText);
      break;
    case SET_LBL_NUM:
      edit.add("Set label number:");
      edit.add("\0" + workingText);
      break;
    case SET_JUMP_TGT:
      edit.add("Set jump target label:");
      edit.add("\0" + workingText);
      break;
    default:
      break;
  }
  
  return edit;
}

public ArrayList<String> loadInstructionReg() {
  ArrayList<String> instReg = new ArrayList<String>();
  
  // show register contents if you're highlighting a register
  Instruction ins = activeInstruction();
  if(ins instanceof MotionInstruction) {
    MotionInstruction castIns = (MotionInstruction)ins;
    Point p = castIns.getVector(activeProgram());
    
    if (p != null) {
      instReg.add("Position values (press ENTER to exit):");
      
      String[] regEntry = p.toLineStringArray(castIns.getMotionType() != MTYPE_JOINT);
      
      for (String line : regEntry) {
        instReg.add(line);
      }
      
      displayPoint = p;
    }
  }
  
  return instReg;
}

// clears the array of selected lines
boolean[] resetSelection(int n) {
  selectedLines = new boolean[n];
  for(int i = 0; i < n; i += 1){
    selectedLines[i] = false;
  }
  
  return selectedLines;
}

public void newMotionInstruction() {
  Point pt = nativeRobotEEPoint(armModel.getJointAngles());
  Frame active = getActiveFrame(CoordFrame.USER);
  
  if (active != null) {
    // Convert into currently active frame
    pt = applyFrame(pt, active.getOrigin(), active.getOrientation());
    
    if (DISPLAY_TEST_OUTPUT) {
      System.out.printf("New: %s\n", convertNativeToWorld(pt.position));
    }
  }
  
  // overwrite current instruction
  Program prog = activeProgram();
  int reg = prog.getNextPosition();
  
  prog.addPosition(pt, reg);
  
  if(getSelectedRow() > 0) {
    MotionInstruction m = (MotionInstruction)activeInstruction();
    m.getSecondaryPoint().setPosition(reg);
    prog.setNextPosition(reg + 1);
  }
  else {
    MotionInstruction insert = new MotionInstruction(
    curCoordFrame == CoordFrame.JOINT ? MTYPE_JOINT : MTYPE_LINEAR,
    reg,
    false,
    (curCoordFrame == CoordFrame.JOINT ? liveSpeed : liveSpeed*armModel.motorSpeed) / 100f,
    0,
    activeUserFrame,
    activeToolFrame);
    
    if(active_instr != prog.getInstructions().size()) {
      prog.overwriteInstruction(active_instr, insert);
    } else {
      prog.addInstruction(insert);
    }
  }
}

public void newFrameInstruction(int fType) {
  Program p = activeProgram();
  FrameInstruction f = new FrameInstruction(fType, -1);
  
  if(active_instr != p.getInstructions().size()) {
    p.overwriteInstruction(active_instr, f);
  } else {
    p.addInstruction(f);
  }
}

public void newIOInstruction() {
  Program p = activeProgram();
  IOInstruction io = new IOInstruction(opt_select, OFF);
  
  if(active_instr != p.getInstructions().size()) {
    p.overwriteInstruction(active_instr, io);
  } else {
    p.addInstruction(io);
  }
}

public void newLabel() {
  Program p = activeProgram();
  
  LabelInstruction l = new LabelInstruction(-1);
  
  if(active_instr != p.getInstructions().size()) {
    p.overwriteInstruction(active_instr, l);
  } else {
    p.addInstruction(l);
  }
}

public void newJumpInstruction() {
  Program p = activeProgram();
  JumpInstruction j = new JumpInstruction(-1);
  
  if(active_instr != p.getInstructions().size()) {
    p.overwriteInstruction(active_instr, j);
  } else {
    p.addInstruction(j);
  }
}


public void newCallInstruction() {
  Program p = activeProgram();
  CallInstruction call = new CallInstruction();
  
  if(active_instr != p.getInstructions().size()) {
    p.overwriteInstruction(active_instr, call);
  } else {
    p.addInstruction(call);
  }
}

public void newIfStatement() {
  Program p = activeProgram();
  IfStatement stmt = new IfStatement(Operator.EQUAL, null);
  opEdit = stmt.expr;
  
  if(active_instr != p.getInstructions().size()) {
    p.overwriteInstruction(active_instr, stmt);
  } else {
    p.addInstruction(stmt);
  }
}

public void newIfExpression() {
  Program p = activeProgram();
  IfStatement stmt = new IfStatement();
  
  if(active_instr != p.getInstructions().size()) {
    p.overwriteInstruction(active_instr, stmt);
  } else {
    p.addInstruction(stmt);
  }
}

public void newSelectStatement() {
  Program p = activeProgram();
  SelectStatement stmt = new SelectStatement();
  
  if(active_instr != p.getInstructions().size()) {
    p.overwriteInstruction(active_instr, stmt);
  } else {
    p.addInstruction(stmt);
  }
}

public void newRegisterStatement(Register r) {
  Program p = activeProgram();
  RegisterStatement stmt = new RegisterStatement(r);
  
  if(active_instr != p.getInstructions().size()) {
    p.overwriteInstruction(active_instr, stmt);
  } else {
    p.addInstruction(stmt);
  }
}

public void newRegisterStatement(Register r, int i) {
  Program p = activeProgram();
  RegisterStatement stmt = new RegisterStatement(r, i);
  
  if(active_instr != p.getInstructions().size()) {
    p.overwriteInstruction(active_instr, stmt);
  } else {
    p.addInstruction(stmt);
  }
}

/**
 * Updates the index display in the Active Frames menu based on the
 * current value of workingText
 */
public void updateActiveFramesDisplay() {
  // Attempt to parse the inputted integer value
  try {
    int frameIdx = Integer.parseInt(workingText) - 1;
    
    if (frameIdx >= -1 && frameIdx < 10) {
      // Update the appropriate active Frame index
      if (row_select == 0) {
        activeToolFrame = frameIdx;
      } else {
        activeUserFrame = frameIdx;
      }
      
      updateCoordFrame();
    }
      
  } catch(NumberFormatException NFEx) {
    // Non-integer value
  }
  // Update display
  if (row_select == 0) {
    workingText = Integer.toString(activeToolFrame + 1);
  } else {
    workingText = Integer.toString(activeUserFrame + 1);
  }
  
  contents.get(row_select).set(col_select, workingText);
  updateScreen();
}

/**
 * Loads the set of Frames that correspond to the given coordinate frame.
 * Only TOOL and USER have Frames sets as of now.
 * 
 * @param coorFrame  the integer value representing the coordinate frame
 *                   of the desired frame set
 */
public ArrayList<DisplayLine> loadFrames(CoordFrame coordFrame) {
  ArrayList<DisplayLine> frameDisplay = new ArrayList<DisplayLine>();
  
  Frame[] frames;
  
  switch(coordFrame) {
    // Only the Tool and User Frame lists have been implemented
    case TOOL:
      frames = toolFrames;
      break;
    case USER:
      frames = userFrames;
      break;
    default:
      System.err.println("Invalid frame type @GUI: loadFrames.");
      return null;
  }
  
  for(int idx = 0; idx < frames.length; idx += 1) {
    // Display each frame on its own line
    String[] strArray = frames[idx].toLineStringArray();
    frameDisplay.add(newLine(idx, String.format("%-4s %s", String.format("%d)", idx + 1), strArray[0])));
    frameDisplay.add(newLine(idx, String.format("%s", strArray[1])));
    frameDisplay.get(idx*2 + 1).xAlign = 38;
  }
  
  return frameDisplay;
}

/**
 * Transitions to the Frame Details menu, which displays
 * the x, y, z, w, p, r values associated with the Frame
 * at curFrameIdx in either the Tool Frames or User Frames,
 * based on the value of super_mode.
 */
public ArrayList<DisplayLine> loadFrameDetail(CoordFrame coordFrame) {
  ArrayList<DisplayLine> details = new ArrayList<DisplayLine>();
  
  // Display the frame set name as well as the index of the currently selected frame
  if(coordFrame == CoordFrame.TOOL) {
    println(curFrameIdx);
    String[] fields = toolFrames[curFrameIdx].toLineStringArray();
    // Place each value in the frame on a separate lien
    for(String field : fields) { details.add(newLine(field)); }
    
  } else if(coordFrame == CoordFrame.USER) {
    String[] fields = userFrames[curFrameIdx].toLineStringArray();
    // Place each value in the frame on a separate lien
    for(String field : fields) { details.add(newLine(field)); }
  
  } else {
    return null;
  }
  
  return details;
}

/**
 * Displays the points along with their respective titles for the
 * current frame teach method (discluding the Direct Entry method).
 */
public ArrayList<String> loadPointList() {
  ArrayList<String> points = new ArrayList<String>();
  
  if(teachFrame != null) {
    
    ArrayList<String> temp = new ArrayList<String>();
    // Display TCP teach points
    if(mode == Screen.TEACH_3PT_TOOL || mode == Screen.TEACH_6PT) {
      temp.add("First Approach Point: ");
      temp.add("Second Approach Point: ");
      temp.add("Third Approach Point: ");
    }
    // Display Axes Vectors teach points
    if(mode == Screen.TEACH_3PT_USER || mode == Screen.TEACH_4PT || mode == Screen.TEACH_6PT) {
      temp.add("Orient Origin Point: ");
      temp.add("X Axis Point: ");
      temp.add("Y Axis Point: ");
    }
    // Display origin offset point
    if(mode == Screen.TEACH_4PT) {
      // Name of fourth point for the four point method?
      temp.add("Origin: ");
    }
    
    // Determine if the point has been set yet
    for(int idx = 0; idx < temp.size(); ++idx) {
      // Add each line to options
      points.add( temp.get(idx) + ((teachFrame.getPoint(idx) != null) ? "RECORDED" : "UNINIT") );
    }
  } else {
    // No teach points
    points.add("Error: teachFrame not set!");
  }
  
  return points;
}

/**
 * Takes the values associated with the given Frame's direct entry values
 * (X, Y, Z, W, P, R) and fills a 2D ArrayList, where the first column is
 * the prefix for the value in the second column.
 * 
 * @param f        The frame to be displayed for editing
 * @returning      A 2D ArrayList with the prefixes and values associated
 *                 with the Frame
 */
public ArrayList<DisplayLine> loadFrameDirectEntry(Frame f) {
  ArrayList<DisplayLine> frame = new ArrayList<DisplayLine>();
  
  String[][] entries = f.directEntryStringArray();
  
  for (int line = 0; line < entries.length; ++line) {
    frame.add(newLine(line, entries[line][0], entries[line][1]));
  }
  
  return frame; 
}

/**
 * This method attempts to modify the Frame based on the given value of method.
 * If method is even, then the frame is taught via the 3-Point Method. Otherwise,
 * the Frame is taught by either the 4-Point or 6-Point Method based on if the
 * Frame is a Tool Frame or a User Frame.
 * 
 * @param frame    The frame to be taught
 * @param method  The method by which to each the new Frame
 */
public void createFrame(Frame frame, int method) {
  if (teachFrame.setFrame(abs(method) % 2)) {
    if (DISPLAY_TEST_OUTPUT) { System.out.printf("Frame set: %d\n", curFrameIdx); }
    
    // Set new Frame
    if (frame instanceof ToolFrame) {
      // Update the current frame of the Robot Arm
      activeToolFrame = curFrameIdx;
      toolFrames[activeToolFrame] = frame;
      updateCoordFrame();
      
      saveFrameBytes( new File(sketchPath("tmp/frames.bin")) );
    } else {
      // Update the current frame of the Robot Arm
      activeUserFrame = curFrameIdx;
      userFrames[activeUserFrame] = frame;
      updateCoordFrame();
      
      saveFrameBytes( new File(sketchPath("tmp/frames.bin")) );
    }
    
  } else {
    println("Invalid input points");
  }
}

/**
 * This method takes the current values stored in contents (assuming that they corresond to
 * the six direct entry values X, Y, Z, W, P, R), parses them, saves them to given Frame object,
 * and sets the current Frame's values to the direct entry value, setting the current frame as
 * the active frame in the process.
 * 
 * @param taughtFrame  the Frame, to which the direct entry values will be stored
 */
public void createFrameDirectEntry(Frame taughtFrame, float[] inputs) {
  // The user enters values with reference to the World Frame
  PVector origin, wpr;
  
  if (taughtFrame instanceof UserFrame) {
    origin = convertWorldToNative( new PVector(inputs[0], inputs[1], inputs[2]) );
  } else {
    // Tool frame origins are actually an offset of the Robot's EE position
    origin = new PVector(inputs[0], inputs[1], inputs[2]);
  }
  // Convert the angles from degrees to radians, then convert from World to Native frame
  wpr = convertWorldToNative( (new PVector(inputs[3], inputs[4], inputs[5])).mult(DEG_TO_RAD) );
  
  // Save direct entry values
  taughtFrame.DEOrigin = origin;
  taughtFrame.DEOrientation = eulerToQuat(wpr);
  taughtFrame.setFrame(2);
  
  if(DISPLAY_TEST_OUTPUT) {
    wpr = quatToEuler(taughtFrame.orientation).mult(RAD_TO_DEG);
    System.out.printf("\n\n%s\n%s\nFrame set: %d\n", origin.toString(),
                      wpr.toString(), curFrameIdx);
  }
  
  // Set New Frame
  if(taughtFrame instanceof ToolFrame) {
    // Update the current frame of the Robot Arm
    activeToolFrame = curFrameIdx;
  } else {
    // Update the current frame of the Robot Arm
    activeUserFrame = curFrameIdx;
  } 
  
  updateCoordFrame();
  saveFrameBytes( new File(sketchPath("tmp/frames.bin")) );
}

/**
 * Displays the list of Registers in mode VIEW_REG or the Position Registers
 * for modes VIEW_REG_J or VIEW_REG_C. In mode VIEW_REG_J the joint angles
 * associated with the Point are displayed and the Cartesian values are
 * displayed in mode VIEW_REG_C.
 */
public ArrayList<DisplayLine> loadRegisters() { 
  ArrayList<DisplayLine> regs = new ArrayList<DisplayLine>();
  
  // View Registers or Position Registers
  //int start = start_render;
  //int end = min(start + ITEMS_TO_SHOW, DREG.length);
  
  // Display a subset of the list of registers
  for(int idx = 0; idx < DREG.length; ++idx) {
    String lbl;
    
    if(mode == Screen.NAV_DREGS) {
      lbl = (DREG[idx].comment == null) ? "" : DREG[idx].comment;
    } else {
      lbl  = (GPOS_REG[idx].comment == null) ? "" : GPOS_REG[idx].comment;
    }
    
    int buffer = 16 - lbl.length();
    while(buffer-- > 0) { lbl += " "; }
    
    String spaces;
    
    if(idx < 9) {
      spaces = "  ";
    } else if(idx < 99) {
      spaces = " ";
    } else {
      spaces = "";
    }
    
    // Display the comment asscoiated with a specific Register entry
    String regLbl = String.format("%s[%d:%s%s]", (mode == Screen.NAV_DREGS) ? "R" : "PR", (idx + 1), spaces, lbl);
    // Display Register value (* ifuninitialized)
    String regEntry = "*";
    
    if(mode == Screen.NAV_DREGS) {
      if(DREG[idx].value != null) {
        // Dispaly Register value
        regEntry = String.format("%4.3f", DREG[idx].value);
      }
      
    } else if(GPOS_REG[idx].point != null) {
      regEntry = "...";
    }
    
    regs.add(newLine(idx, regLbl, regEntry));
  }
  
  return regs;
}

/**
 * This method will transition to the INPUT_POINT_C or INPUT_POINT_J modes
 * based whether the current mode is VIEW_REG_C or VIEW_REG_J. In either
 * mode, the user will be prompted to input 6 floating-point values (X, Y,
 * Z, W, P, R or J1 - J6 for INPUT_POINT_C or INPUT_POINT_J respectively).
 * The input method is similar to inputting the value in DIRECT_ENTRY mode.
 */
public ArrayList<DisplayLine> loadPosRegEntry(PositionRegister reg) {
  ArrayList<DisplayLine> register = new ArrayList<DisplayLine>();
  
  if(reg.point == null) {
    // Initialize values to zero if the entry is null
    if(mode == Screen.EDIT_PREG_C) {
      register.add(newLine(0, "X: ",  ""));
      register.add(newLine(1, "Y: ",  ""));
      register.add(newLine(2, "Z: ",  ""));
      register.add(newLine(3, "W: ",  ""));
      register.add(newLine(4, "P: ",  ""));
      register.add(newLine(5, "R: ",  ""));
      
    } else if(mode == Screen.EDIT_PREG_J) {
      for(int idx = 0; idx < 6; idx += 1) {
        register.add(newLine(idx, String.format("J%d: ", idx), ""));
      }
    }
  } else {
    
    // List current entry values if the Register is initialized
    String[][] entries;
    
    if (mode == Screen.EDIT_PREG_J) {
      // List joint angles
      entries = reg.point.toJointStringArray();
    } else {
      // Display Cartesian values
      entries = reg.point.toCartesianStringArray();
    }
    
    for(int idx = 0; idx < entries.length; ++idx) {
      register.add(newLine(idx, entries[idx][0], entries[idx][1]));
    }
  }
   
  return register;
}

public ArrayList<String> loadIORegisters() {
  ArrayList<String> ioRegs = new ArrayList<String>();
  
  for(int i = 0; i < IO_REG.length; i += 1){
    String state = (IO_REG[i].state == ON) ? "ON" : "OFF";
    String ee;
    
    if (IO_REG[i].name != null) {
      ee = IO_REG[i].name;
    } else {
      ee = "";
    }
    
    ioRegs.add( String.format("IO[%d:%-8s] = %s", i + 1, ee, state) );
  }
  
  return ioRegs;
}

public void createRegisterPoint(boolean fromJointAngles) {
  // Obtain point inputs from UI display text
  float[] inputs = new float[6];
  try {
    for(int idx = 0; idx < inputs.length; ++idx) {
      String inputStr = contents.get(idx).get(col_select);
      inputs[idx] = Float.parseFloat(inputStr);
      // Bring the input values with the range [-9999, 9999]
      inputs[idx] = max(-9999f, min(inputs[idx], 9999f));
    }
  } catch (NumberFormatException NFEx) {
    // Invalid input
    println("Values must be real numbers!");
    return;
  }
  
  if(fromJointAngles) {
    // Bring angles within range: (0, TWO_PI)
    for(int idx = 0; idx < inputs.length; ++idx) {
      inputs[idx] = mod2PI(inputs[idx] * DEG_TO_RAD);
    }
    
    GPOS_REG[active_index].point = nativeRobotEEPoint(inputs);
  } else {
    PVector position = convertWorldToNative( new PVector(inputs[0], inputs[1], inputs[2]) );
    // Convert the angles from degrees to radians, then convert from World to Native frame, and finally convert to a quaternion
    float[] orientation = eulerToQuat( convertWorldToNative( (new PVector(inputs[3], inputs[4], inputs[5]).mult(DEG_TO_RAD)) ) );
    
    // Use default the Robot's joint angles for computing inverse kinematics
    float[] jointAngles = inverseKinematics(new float[] {0f, 0f, 0f, 0f, 0f, 0f}, position, orientation);
    GPOS_REG[active_index].point = new Point(position, orientation, jointAngles);
  }
  
  GPOS_REG[active_index].isCartesian = !fromJointAngles;
  saveRegisterBytes( new File(sketchPath("tmp/registers.bin")) );
}

/**
 * This method loads text to screen in such a way as to allow the user
 * to input an arbitrary character string consisting of letters (a-z
 * upper and lower case) and/ or special characters (_, @, *, .) via
 * the function row, as well as numbers via the number pad. Strings are
 * limited to 16 characters and can be used to name new routines, as well
 * as set remark fields for frames and instructions.
 */
public ArrayList<DisplayLine> loadTextInput() {
  ArrayList<DisplayLine> remark = new ArrayList<DisplayLine>();
  
  remark.add(newLine("\0"));
 
  DisplayLine line = new DisplayLine();
  // Give each letter in the name a separate column
  for(int idx = 0; idx < workingText.length() && idx < TEXT_ENTRY_LEN; idx += 1) {
    line.add( Character.toString(workingText.charAt(idx)) );
  }
  
  remark.add(line);
  
  return remark;
}

/**
 * Given a set of Strings this method returns a single
 * String ArrayList, which contains all the given elements
 * in the order that they are given as arguments.
 * 
 * @param columns  A list of Strings
 * @return         An ArrayList containing all the given
 *                 Strings
 */
public DisplayLine newLine(String... columns) {
  DisplayLine line =  new DisplayLine();
  
  for(String col : columns) {
    line.add(col);
  }
  
  return line;
}

public DisplayLine newLine(int itemIdx, String... columns) {
  DisplayLine line =  new DisplayLine(itemIdx);
  
  for(String col : columns) {
    line.add(col);
  }
  
  return line;
}

/**
 * 
 */
public int moveUp(boolean page) {
  if (page && start_render > 0) {
    // Move display frame up an entire screen's display length
    row_select = max(0, row_select - (ITEMS_TO_SHOW - 1));
    start_render = max(0, start_render - (ITEMS_TO_SHOW - 1));
  } 
  else {
    // Move up a single row
    row_select = max(0, row_select - 1);
  }
  
  return contents.get(row_select).itemIdx;
}

public int moveUpInstr(boolean page) {
  if (page && start_render > 0) {
    // Move display frame up an entire screen's display length
    row_select = max(0, row_select - (ITEMS_TO_SHOW - 1));
    start_render = max(0, start_render - (ITEMS_TO_SHOW - 1));
  } 
  else {
    if(getSelectedIdx() == 0 && active_instr > 0) {
      // Move up a single instruction
      while(row_select > 0 && active_instr - 1 == contents.get(row_select - 1).itemIdx) {
        row_select = max(0, row_select - 1);
      }
    } else {
      // Move up a single row
      row_select = max(0, row_select - 1);
    }
  }
  
  return contents.get(row_select).itemIdx;  
}

/**
 * 
 */
public int moveDown(boolean page) {
  int size = contents.size();  
  
  if (page && size > (start_render + ITEMS_TO_SHOW)) {
    // Move display frame down an entire screen's display length
    row_select = min(size - 1, row_select + (ITEMS_TO_SHOW - 1));
    start_render = max(0, min(size - ITEMS_TO_SHOW, start_render + (ITEMS_TO_SHOW - 1)));
  } else {
    // Move down a single row
    row_select = min(size - 1, row_select + 1);
  }
  
  return contents.get(row_select).itemIdx;
}

public int moveDownInstr(boolean page) {
  int size = contents.size();  
  
  if (page && size > (start_render + ITEMS_TO_SHOW)) {
    // Move display frame down an entire screen's display length
    row_select = min(size - 1, row_select + (ITEMS_TO_SHOW - 1));
    start_render = max(0, min(size - ITEMS_TO_SHOW, start_render + (ITEMS_TO_SHOW - 1)));
  } else {
    int lenMod = 0;
    if(mode.getType() == ScreenType.TYPE_LINE_SELECT) lenMod = 1;
    if(getSelectedIdx() == 0 && active_instr < activeProgram().size() - lenMod) {
      // Move down a single instruction
      while(active_instr == contents.get(row_select).itemIdx) {
        row_select = min(size - 1, row_select + 1);
      }
    } else {
      // Move down a single row
      row_select = min(size - 1, row_select + 1);
    }
  }
  
  return contents.get(row_select).itemIdx;
}

public void moveLeft() {
  if(row_select > 0 && contents.get(row_select - 1).itemIdx == contents.get(row_select).itemIdx) {
    col_select -= 1;
    if(col_select < 0) {
      moveUp(false);
      col_select = contents.get(row_select).size() - 1;
    }
  } else {
    col_select = max(0, col_select - 1);
  }
}

public void moveRight() {
  if(row_select < contents.size() - 1 && contents.get(row_select + 1).itemIdx == contents.get(row_select).itemIdx) {
    col_select += 1;
    if(col_select > contents.get(row_select).size() - 1) {
      moveDown(false);
      col_select = 0;
    }
  } else {
    col_select = min(contents.get(row_select).size() - 1, col_select + 1);
  }
}

/**
 * Returns the first line in the current list of contents that the instruction 
 * matching the given index appears on.
 */
public int getInstrLine(int instrIdx) {
  ArrayList<DisplayLine> instr = loadInstructions(active_prog);
  int row = instrIdx;
  
  while(instr.get(row).itemIdx != instrIdx) {
    row += 1;
    if(row_select >= contents.size() - 1) break;
  }
  
  return row;
}

public int getSelectedRow() {
  int row = 0;
  DisplayLine currRow = contents.get(row_select);
  while(row_select - row >= 0 && currRow.itemIdx == contents.get(row_select - row).itemIdx) {
    row += 1;
  }
  
  return row - 1;
}

public int getSelectedIdx() {
  if(mode.getType() == ScreenType.TYPE_LINE_SELECT) return 0;
  
  int idx = col_select;
  for(int i = row_select - 1; i >= 0; i -= 1) {
    if(contents.get(i).itemIdx != contents.get(i + 1).itemIdx) break;
    idx += contents.get(i).size();
  }
  
  return idx;
}

public class DisplayLine {
  ArrayList<String> contents;
  int itemIdx;
  int xAlign;
      
  public DisplayLine() {
    contents = new ArrayList<String>();
    itemIdx = -1;
    xAlign = 0;
  }
  
  public DisplayLine(int idx) {
    contents = new ArrayList<String>();
    itemIdx = idx;
    xAlign = 0;
  }
  
  public DisplayLine(int idx, int align) {
    contents = new ArrayList<String>();
    itemIdx = idx;
    xAlign = align;
  }
  
  public DisplayLine(ArrayList<String> c, int idx, int align) {
    contents = c;
    itemIdx = idx;
    xAlign = align;
  }
  
  public int size() {
    return contents.size();
  }
  
  public String get(int idx) {
    return contents.get(idx);
  }
  
  public String set(int i, String s) {
    return contents.set(i, s);
  }
  
  public boolean add(String s) {
    return contents.add(s);
  }
  
  public void add(int i, String s) {
    contents.add(i, s);
  }
  
  public String remove(int i) {
    return contents.remove(i);
  }
}
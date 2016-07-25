final int SMALL_BUTTON = 35,
          LARGE_BUTTON = 50;
final int BUTTON_DEFAULT = color(70),
          BUTTON_ACTIVE = color(220, 40, 40),
          BUTTON_TEXT = color(240),
          TEXT_DEFAULT = color(240),
          TEXT_HIGHLIGHT = color(40);

//String displayFrame = "JOINT";
int active_prog = -1; // the currently selected program
int active_instr = -1; // the currently selected instruction
int NUM_MODE; // When NUM_MODE is ON, allows for entering numbers
boolean shift = false; // Is shift button pressed or not?
boolean step = false; // Is step button pressed or not?
int record = OFF;

Screen mode;
int g1_px, g1_py; // the left-top corner of group1
int g1_width, g1_height; // group 1's width and height
int display_px, display_py; // the left-top corner of display screen
int display_width, display_height; // height and width of display screen

PFont fnt_con14, fnt_con12, fnt_conB;
Group g1, g2;
Button bt_show, bt_hide, 
bt_zoomin_shrink, bt_zoomin_normal,
bt_zoomout_shrink, bt_zoomout_normal,
bt_pan_shrink, bt_pan_normal,
bt_rotate_shrink, bt_rotate_normal,
bt_record_shrink, bt_record_normal, 
bt_ee_normal;

String workingText; // when entering text or a number
String workingTextSuffix;
boolean speedInPercentage;
private static final int ITEMS_TO_SHOW = 7, // how many programs/ instructions to display on screen
                         NUM_ENTRY_LEN = 16, // Maximum character length for a number input
                         TEXT_ENTRY_LEN = 16; // Maximum character length for text entry

// Index of the current frame (Tool or User) selecting when in the Frame menus
int curFrameIdx = -1,
// Indices of currently active frames
    activeUserFrame = -1,
    activeToolFrame = -1;
// The Frame being taught, during a frame teaching process
Frame teachFrame = null;
// A point in space; used during the Frame Point Teaching processes
PVector ref_point;

//variables for keeping track of the last change made to the current program
Instruction lastInstruct;
boolean newInstruct;
int lastLine;

// display list of programs or motion instructions
ArrayList<ArrayList<String>> contents = new ArrayList<ArrayList<String>>();
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
int col_select = 0; //currently selected display column
int opt_select = 0; //which option is on focus now?
int renderStartIdx = 0; //index of the first element in a list to be drawn on screen
int active_index = 0; //index of the cursor with respect to the first element on screen
boolean[] selectedLines; //array whose indecies correspond to currently selected lines
// how many textlabels have been created for display
int index_contents = 0, index_options = 100, index_nums = 1000; 
int mouseDown = 0;

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
                                   
public static final boolean DISPLAY_TEST_OUTPUT = true;

void gui() {
  g1_px = 0;
  g1_py = 0;
  g1_width = 440;
  g1_height = 720;
  display_px = 10;
  display_py = (SMALL_BUTTON - 15) + 1;
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
  .setBackgroundHeight(g1_height);
  
  cp5.addTextarea("txt")
  .setPosition(display_px,display_py)
  .setSize(display_width, display_height)
  .setColorBackground(TEXT_DEFAULT)
  .moveTo(g1);
  
  //create font and text display background
  fnt_con14 = createFont("data/Consolas.ttf", 14);
  fnt_con12 = createFont("data/Consolas.ttf", 12);
  fnt_conB = createFont("data/ConsolasBold.ttf", 12);
  
  /**********************Top row buttons**********************/
  
  // button to show g1
  int bt_show_px = 1;
  int bt_show_py = 1;
  bt_show = cp5.addButton("show")
  .setPosition(bt_show_px, bt_show_py)
  .setSize(SMALL_BUTTON, SMALL_BUTTON - 15)
  .setLabel("SHOW")
  .setColorBackground(BUTTON_DEFAULT)
  .setColorCaptionLabel(BUTTON_TEXT)  
  .hide();
  
  //calculate how much space each button will be given
  int button_offsetX = LARGE_BUTTON + 1;
  int button_offsetY = LARGE_BUTTON + 1;
  
  int zoomin_shrink_px = bt_show_px + LARGE_BUTTON;
  int zoomin_shrink_py = bt_show_py;
  PImage[] zoomin_shrink = {loadImage("images/zoomin_35x20.png"), 
    loadImage("images/zoomin_over.png"), 
    loadImage("images/zoomin_down.png")};   
  bt_zoomin_shrink = cp5.addButton("zoomin_shrink")
  .setPosition(zoomin_shrink_px, zoomin_shrink_py)
  .setSize(SMALL_BUTTON, SMALL_BUTTON)
  .setImages(zoomin_shrink)
  .updateSize()
  .hide();   
  
  int zoomout_shrink_px = zoomin_shrink_px + LARGE_BUTTON;
  int zoomout_shrink_py = zoomin_shrink_py;   
  PImage[] zoomout_shrink = {loadImage("images/zoomout_35x20.png"), 
    loadImage("images/zoomout_over.png"), 
    loadImage("images/zoomout_down.png")};   
  bt_zoomout_shrink = cp5.addButton("zoomout_shrink")
  .setPosition(zoomout_shrink_px, zoomout_shrink_py)
  .setSize(SMALL_BUTTON, SMALL_BUTTON)
  .setImages(zoomout_shrink)
  .updateSize()
  .hide();    
  
  int pan_shrink_px = zoomout_shrink_px + LARGE_BUTTON;
  int pan_shrink_py = zoomout_shrink_py;
  PImage[] pan_shrink = {loadImage("images/pan_35x20.png"), 
    loadImage("images/pan_over.png"), 
    loadImage("images/pan_down.png")};   
  bt_pan_shrink = cp5.addButton("pan_shrink")
  .setPosition(pan_shrink_px, pan_shrink_py)
  .setSize(SMALL_BUTTON, SMALL_BUTTON)
  .setImages(pan_shrink)
  .updateSize()
  .hide();    
  
  int rotate_shrink_px = pan_shrink_px + LARGE_BUTTON;
  int rotate_shrink_py = pan_shrink_py;   
  PImage[] rotate_shrink = {loadImage("images/rotate_35x20.png"), 
    loadImage("images/rotate_over.png"), 
    loadImage("images/rotate_down.png")};   
  bt_rotate_shrink = cp5.addButton("rotate_shrink")
  .setPosition(rotate_shrink_px, rotate_shrink_py)
  .setSize(SMALL_BUTTON, SMALL_BUTTON)
  .setImages(rotate_shrink)
  .updateSize()
  .hide();     
  
  // button to hide g1
  int hide_px = display_px;
  int hide_py = display_py - (SMALL_BUTTON - 15);
  bt_hide = cp5.addButton("hide")
  .setPosition(hide_px, hide_py)
  .setSize(SMALL_BUTTON, SMALL_BUTTON - 15)
  .setCaptionLabel("HIDE")
  .setColorBackground(BUTTON_DEFAULT)
  .setColorCaptionLabel(BUTTON_TEXT)  
  .moveTo(g1);
  
  int zoomin_normal_px =  hide_px + LARGE_BUTTON + 1;
  int zoomin_normal_py = hide_py;
  PImage[] zoomin_normal = {loadImage("images/zoomin_35x20.png"), 
    loadImage("images/zoomin_over.png"), 
    loadImage("images/zoomin_down.png")};   
  bt_zoomin_normal = cp5.addButton("zoomin_normal")
  .setPosition(zoomin_normal_px, zoomin_normal_py)
  .setSize(SMALL_BUTTON, SMALL_BUTTON)
  .setImages(zoomin_normal)
  .updateSize()
  .moveTo(g1);   
  
  int zoomout_normal_px = zoomin_normal_px + LARGE_BUTTON + 1;
  int zoomout_normal_py = zoomin_normal_py;   
  PImage[] zoomout_normal = {loadImage("images/zoomout_35x20.png"), 
    loadImage("images/zoomout_over.png"), 
    loadImage("images/zoomout_down.png")};   
  bt_zoomout_normal = cp5.addButton("zoomout_normal")
  .setPosition(zoomout_normal_px, zoomout_normal_py)
  .setSize(SMALL_BUTTON, SMALL_BUTTON)
  .setImages(zoomout_normal)
  .updateSize()
  .moveTo(g1);    
  
  int pan_normal_px = zoomout_normal_px + LARGE_BUTTON + 1;
  int pan_normal_py = zoomout_normal_py;
  PImage[] pan = {loadImage("images/pan_35x20.png"), 
    loadImage("images/pan_over.png"), 
    loadImage("images/pan_down.png")};   
  bt_pan_normal = cp5.addButton("pan_normal")
  .setPosition(pan_normal_px, pan_normal_py)
  .setSize(SMALL_BUTTON, SMALL_BUTTON)
  .setImages(pan)
  .updateSize()
  .moveTo(g1);    
  
  int rotate_normal_px = pan_normal_px + LARGE_BUTTON + 1;
  int rotate_normal_py = pan_normal_py;   
  PImage[] rotate = {loadImage("images/rotate_35x20.png"), 
    loadImage("images/rotate_over.png"), 
    loadImage("images/rotate_down.png")};   
  bt_rotate_normal = cp5.addButton("rotate_normal")
  .setPosition(rotate_normal_px, rotate_normal_py)
  .setSize(SMALL_BUTTON, SMALL_BUTTON)
  .setImages(rotate)
  .updateSize()
  .moveTo(g1);     
  
  int record_normal_px = rotate_normal_px + LARGE_BUTTON + 1;
  int record_normal_py = rotate_normal_py;   
  PImage[] record = {loadImage("images/record-35x20.png"), 
    loadImage("images/record-over.png"), 
    loadImage("images/record-on.png")};   
  bt_record_normal = cp5.addButton("record_normal")
  .setPosition(record_normal_px, record_normal_py)
  .setSize(SMALL_BUTTON, SMALL_BUTTON)
  .setImages(record)
  .updateSize()
  .moveTo(g1);     
  
  int EE_normal_px = record_normal_px + LARGE_BUTTON + 1;
  int EE_normal_py = record_normal_py;   
  PImage[] EE = {loadImage("images/EE_35x20.png"), 
    loadImage("images/EE_over.png"), 
    loadImage("images/EE_down.png")};   
  bt_ee_normal = cp5.addButton("EE")
  .setPosition(EE_normal_px, EE_normal_py)
  .setSize(SMALL_BUTTON, SMALL_BUTTON)
  .setImages(EE)
  .updateSize()
  .moveTo(g1);

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

public void mousePressed() {
  mouseDown += 1;
  if(mouseButton == LEFT) {
    if(clickRotate%2 == 1) {
      doRotate = !doRotate;
    }
    else if(clickPan%2 == 1) {
      doPan = !doPan;
    }
  }
}

public void mouseDragged(MouseEvent e) {
  // Hold down the center mouse button and move the mouse to pan the camera
  if(mouseButton == CENTER) {
    panX += mouseX - pmouseX;
    panY += mouseY - pmouseY;
  }
  
  // Hold down the right omuse button an move the mouse to rotate the camera
  if(mouseButton == RIGHT) {
    myRotX += (mouseY - pmouseY) * 0.01;
    myRotY += (mouseX - pmouseX) * 0.01;
  }
}

public void mouseMoved() {
  if(doPan) {
    panX += mouseX - pmouseX;
    panY += mouseY - pmouseY;
  }
  if(doRotate) {
    myRotX += (mouseY - pmouseY) * 0.01;
    myRotY += (mouseX - pmouseX) * 0.01;
  }
}

public void mouseWheel(MouseEvent event) {
  float e = event.getCount();
  // Control scaling of the camera with the mouse wheel
  if(e > 0 ) {
    myscale *= 1.1;
    if(myscale > 2) {
      myscale = 2;
    }
  }
  if(e < 0) {
    myscale *= 0.9;
    if(myscale < 0.25) {
      myscale = 0.25;
    }
  }
}

public void mouseReleased() {
  mouseDown -= 1;
}

/*Keyboard events*/

public void keyPressed() {
  if(mode == Screen.NEW_PROGRAM) {
    // Modify the input name for the new program
    if(key == BACKSPACE && workingText.length() > 0) {
      workingText = workingText.substring(0, workingText.length() - 1);
    } else if(key == DELETE && workingText.length() > 0) {
      workingText = workingText.substring(1, workingText.length());
    // Valid characters in a program name or comment
    } else if(workingText.length() < TEXT_ENTRY_LEN && (key >= 'a' && key <= 'z') || (key >= 'A' && key <= 'Z')
          || (key >= '0' && key <= '9') || key == '.' || key == '@' || key == '*' || key == '_') {
      StringBuilder temp;
      // Insert the typed character
      if (workingText.charAt(col_select) != '\0') {
        temp = new StringBuilder(workingText.substring(0, col_select) + "\0" + workingText.substring(col_select, workingText.length()));
      } else {
        temp = new StringBuilder(workingText);
      }
      
      temp.setCharAt(col_select, key);
      workingText = temp.toString();
      
      // Move the cursor over for the next letter
      rt();
    }
    
    return;
  } else if(key == 'e') {
    EE_MAPPING = (EE_MAPPING + 1) % 3;
  } else if(key == 'f') {
    armModel.currentFrame = armModel.getRotationMatrix();
  } else if(key == 'g') {
    armModel.resetFrame();
  } else if(key == 'q') {
    System.out.printf("\n%s\n\n", arrayToString(armModel.getQuaternion()));
  } else if(key == 'r') {
    panX = 0;
    panY = 0;
    myscale = 0.5;
    myRotX = 0;
    myRotY = 0;
  } else if(key == 't') {
    // Release an object ifit is currently being held
    if(armModel.held != null) {
      armModel.releaseHeldObject();
      armModel.endEffectorStatus = OFF;
    }
    
    float[] rot = {0, 0, 0, 0, 0, 0};
    armModel.setJointAngles(rot);
    intermediatePositions.clear();
  } else if(key == 'w') {
    //armModel.currentFrame = armModel.getRotationMatrix();
  } else if (key == 'y') {
    float[] rot = {PI, 0, 0, 0, 0, PI};
    armModel.setJointAngles(rot);
    intermediatePositions.clear();
  } else if (key == 'm') {
    println(mode.toString());
  } else if(key == ENTER && (armModel.activeEndEffector == EndEffector.CLAW || 
        armModel.activeEndEffector == EndEffector.SUCTION)) { 
    // Pick up an object within reach of the EE when the 'ENTER' button is pressed for either
    // the suction or claw EE
    IOInstruction pickup = new IOInstruction(0, (armModel.endEffectorStatus+1)%2);
    pickup.execute();
  } else if(keyCode == KeyEvent.VK_1) {
    // Front view
    panX = 0;
    panY = 0;
    myRotX = 0f;
    myRotY = 0f;
  } else if(keyCode == KeyEvent.VK_2) {
    // Back view
    panX = 0;
    panY = 0;
    myRotX = 0f;
    myRotY = PI;
  } else if(keyCode == KeyEvent.VK_3) {
    // Left view
    panX = 0;
    panY = 0;
    myRotX = 0f;
    myRotY = PI / 2f;
  } else if(keyCode == KeyEvent.VK_4) {
    // Right view
    panX = 0;
    panY = 0;
    myRotX = 0f;
    myRotY = 3f * PI / 2F;
  } else if(keyCode == KeyEvent.VK_5) {
    // Top view
    panX = 0;
    panY = 0;
    myRotX = 3f * PI / 2F;
    myRotY = 0f;
  } else if(keyCode == KeyEvent.VK_6) {
    // Bottom view
    panX = 0;
    panY = 0;
    myRotX = PI / 2f;
    myRotY = 0f;
  }
  
  if(keyCode == UP) {
    float[] angles = armModel.getJointAngles();
    calculateJacobian(angles, true);
    angles[0] += DEG_TO_RAD;
    armModel.setJointAngles(angles);
  } else if(keyCode == DOWN) {
    float[] angles = armModel.getJointAngles();
    calculateJacobian(angles, true);
    angles[1] += DEG_TO_RAD;
    armModel.setJointAngles(angles);
  } else if(keyCode == LEFT) {
    float[] angles = armModel.getJointAngles();
    calculateJacobian(angles, true);
    angles[2] += DEG_TO_RAD;
    armModel.setJointAngles(angles);
  } else if(keyCode == RIGHT) {
    float[] angles = armModel.getJointAngles();
    calculateJacobian(angles, true);
    angles[3] += DEG_TO_RAD;
    armModel.setJointAngles(angles);
  } else if(key == 'z') {
    float[] angles = armModel.getJointAngles();
    calculateJacobian(angles, true);
    angles[4] += DEG_TO_RAD;
    armModel.setJointAngles(angles);
  } else if(key == 'x') {
    float[] angles = armModel.getJointAngles();
    calculateJacobian(angles, true);
    angles[5] += DEG_TO_RAD;
    armModel.setJointAngles(angles);
  }
  
  if(key == ' ') { 
    pan_normal();
  }
  
  if(keyCode == SHIFT) { 
    rotate_normal();
  }
}

/*Button events*/

public void hide() {
  g1.hide();
  bt_show.show();
  bt_zoomin_shrink.show();
  bt_zoomout_shrink.show();
  bt_pan_shrink.show();
  bt_rotate_shrink.show(); 
  
  // release buttons of pan and rotate
  clickPan = 0;
  clickRotate = 0;
  cursorMode = ARROW;
  PImage[] pan_released = {loadImage("images/pan_35x20.png"), 
    loadImage("images/pan_over.png"), 
    loadImage("images/pan_down.png")};
  
  cp5.getController("pan_normal")
  .setImages(pan_released);
  cp5.getController("pan_shrink")
  .setImages(pan_released);   
  doPan = false;    

  PImage[] rotate_released = {loadImage("images/rotate_35x20.png"), 
    loadImage("images/rotate_over.png"), 
    loadImage("images/rotate_down.png")};
  
  cp5.getController("rotate_normal")
  .setImages(rotate_released);
  cp5.getController("rotate_shrink")
  .setImages(rotate_released);   
  doRotate = false;   
  
  cursor(cursorMode);
}

public void show() {
  g1.show();
  bt_show.hide();
  bt_zoomin_shrink.hide();
  bt_zoomout_shrink.hide();
  bt_pan_shrink.hide();
  bt_rotate_shrink.hide();
  
  // release buttons of pan and rotate
  clickPan = 0;
  clickRotate = 0;
  cursorMode = ARROW;
  PImage[] pan_released = {loadImage("images/pan_35x20.png"), 
    loadImage("images/pan_over.png"), 
    loadImage("images/pan_down.png")}; 
  
  cp5.getController("pan_normal")
  .setImages(pan_released);
  doPan = false;    

  PImage[] rotate_released = {loadImage("images/rotate_35x20.png"), 
    loadImage("images/rotate_over.png"),
    loadImage("images/rotate_down.png")}; 
  
  cp5.getController("rotate_normal")
  .setImages(rotate_released);
  doRotate = false;
  
  cursor(cursorMode);
}

// Menu button
public void mu() {
  resetStack();
  nextScreen(Screen.MAIN_MENU_NAV);
}

// Select button
public void se() {
  // Save when exiting a program
  saveProgramBytes( new File(sketchPath("tmp/programs.bin")) ); 
  active_prog = -1;
  opt_select = 0;
  resetStack();
  nextScreen(Screen.NAV_PROGRAMS);
}

// Data button
public void da() {
  opt_select = 0;
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

public void PERIOD() {
  if(NUM_MODE == ON) {
    nums.add(-1);
  } else if(mode.getType() == ScreenType.TYPE_POINT_ENTRY) {

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
  if(armModel.endEffectorStatus == OFF)
  armModel.endEffectorStatus = ON;
  else
  armModel.endEffectorStatus = OFF;
}

/*Arrow keys*/

public void up() {
  switch(mode) {
    case NAV_PROGRAMS:
      int[] indices = moveUp(active_prog, opt_select, renderStartIdx, shift);
      
      active_prog = indices[0];
      opt_select = indices[1];
      renderStartIdx = indices[2];
      
      if(DISPLAY_TEST_OUTPUT) {
        System.out.printf("\nOpt: %d\nProg: %d\nTRS: %d\n\n",
        opt_select, active_prog, renderStartIdx);
      }
      
      break;
    case SELECT_COMMENT:
    case NAV_PROG_INST:
    case SELECT_CUT_COPY:
    case SELECT_DELETE:
      indices = moveUp(active_instr, row_select, renderStartIdx, shift);
      
      active_instr = indices[0];
      row_select = indices[1];
      renderStartIdx = indices[2];
      
      if(DISPLAY_TEST_OUTPUT) {
        System.out.printf("\nRow: %d\nColumn: %d\nInst: %d\nTRS: %d\n\n",
        row_select, col_select, active_instr, renderStartIdx);
      }
      
      break;
    case NAV_DREGS:
    case NAV_PREGS_J:
    case NAV_PREGS_C:
      indices = moveUp(active_index, row_select, renderStartIdx, shift);
      
      active_index = indices[0];
      row_select = indices[1];
      renderStartIdx = indices[2];
      
      if(DISPLAY_TEST_OUTPUT) {
        System.out.printf("\nRow: %d\nColumn: %d\nIdx: %d\nTRS: %d\n\n",
        row_select, col_select, active_index, renderStartIdx);
      }
      break;
    case MAIN_MENU_NAV:
    case INSTRUCT_MENU_NAV:
    case SELECT_FRAME_MODE:
    case USER_FRAME_METHODS:
    case TOOL_FRAME_METHODS:
    case SELECT_INSTR_INSERT:
    case SELECT_JMP_LBL:
    case SELECT_IF_SEL:
    case TFRAME_DETAIL:
    case UFRAME_DETAIL:
    case TEACH_3PT_USER:
    case TEACH_3PT_TOOL:
    case TEACH_4PT:
    case TEACH_6PT:
    case NAV_DATA:
    case SWAP_PT_TYPE:
    case SET_MV_INSTRUCT_TYPE:
    case SET_MV_INSTRUCT_REG_TYPE:
    case SET_FRM_INSTR_TYPE:
    case SET_IO_INSTR_STATE:
    case SETUP_NAV:
      opt_select = max(0, opt_select - 1);
      break;
    case NAV_TOOL_FRAMES:
    case NAV_USER_FRAMES:
    case DIRECT_ENTRY_TOOL:
    case DIRECT_ENTRY_USER:
    case EDIT_PREG_C:
    case EDIT_PREG_J:
      row_select = max(0, row_select - 1);
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

public void dn() {
  int size;
  switch(mode) {
    case NAV_PROGRAMS:
      size = programs.size(); //<>//
      int[] indices = moveDown(active_prog, size, opt_select, renderStartIdx, shift);
      
      active_prog = indices[0];
      opt_select = indices[1];
      renderStartIdx = indices[2];
      
      if(DISPLAY_TEST_OUTPUT) {
        System.out.printf("\nOpt: %d\nProg: %d\nTRS: %d\n\n",
        opt_select, active_prog, renderStartIdx);
      }
      
      break;
    case NAV_PROG_INST:
      size = programs.get(active_prog).getInstructions().size() + 1;
      indices = moveDown(active_instr, size, row_select, renderStartIdx, shift);
      
      active_instr = indices[0];
      row_select = indices[1];
      renderStartIdx = indices[2];
      
      col_select = max( 0, min( col_select, contents.get(row_select).size() - 1 ) );
      
      if(DISPLAY_TEST_OUTPUT) {
        System.out.printf("\nRow: %d\nColumn: %d\nInst: %d\nTRS: %d\n\n",
        row_select, col_select, active_instr, renderStartIdx);
      }
      break;
    case SELECT_COMMENT:
    case SELECT_CUT_COPY:
    case SELECT_DELETE: //<>//
      size = programs.get(active_prog).getInstructions().size();
      indices = moveDown(active_instr, size, row_select, renderStartIdx, shift);
      
      active_instr = indices[0];
      row_select = indices[1];
      renderStartIdx = indices[2];
      
      col_select = max( 0, min( col_select, contents.get(row_select).size() - 1 ) );
      
      if(DISPLAY_TEST_OUTPUT) {
        System.out.printf("\nRow: %d\nColumn: %d\nInst: %d\nTRS: %d\n\n",
        row_select, col_select, active_instr, renderStartIdx);
      } //<>//
      break;
    case NAV_DREGS:
    case NAV_PREGS_J:
    case NAV_PREGS_C:
      size = (mode == Screen.NAV_DREGS) ? DAT_REG.length : GPOS_REG.length;
      indices = moveDown(active_index, size, row_select, renderStartIdx, shift);
      
      active_index = indices[0];
      row_select = indices[1];
      renderStartIdx = indices[2];
      
      col_select = max( 0, min( col_select, contents.get(row_select).size() - 1 ) );
      
      if(DISPLAY_TEST_OUTPUT) {
        System.out.printf("\nRow: %d\nColumn: %d\nIdx: %d\nTRS: %d\n\n",
        row_select, col_select, active_index, renderStartIdx);
      }
      
      break;
    case MAIN_MENU_NAV:
    case INSTRUCT_MENU_NAV:
    case SELECT_FRAME_MODE:
    case USER_FRAME_METHODS:
    case TOOL_FRAME_METHODS:
    case SELECT_INSTR_INSERT:
    case SELECT_JMP_LBL:
    case SELECT_IF_SEL:
    case TFRAME_DETAIL:
    case UFRAME_DETAIL:
    case TEACH_3PT_USER:
    case TEACH_3PT_TOOL:
    case TEACH_4PT:
    case TEACH_6PT:
    case NAV_DATA:
    case SWAP_PT_TYPE:
    case SET_MV_INSTRUCT_TYPE:
    case SET_MV_INSTRUCT_REG_TYPE:
    case SET_FRM_INSTR_TYPE:
    case SET_IO_INSTR_STATE:
    case SETUP_NAV:
      opt_select = min(opt_select + 1, options.size() - 1);
      break;
    case NAV_TOOL_FRAMES:
    case NAV_USER_FRAMES:
    case EDIT_PREG_C:
    case EDIT_PREG_J:
    case DIRECT_ENTRY_TOOL:
    case DIRECT_ENTRY_USER:
      row_select = min(row_select + 1, contents.size() - 1);
      break;
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
    case NAV_PROG_INST:    
      col_select = max(0, col_select - 1);
      break;
    case NAV_DREGS:
    case NAV_PREGS_J:
    case NAV_PREGS_C:
      col_select = max(0, col_select - 1);
      break;
    default:
      if (mode.type == ScreenType.TYPE_TEXT_ENTRY) {
        col_select = max(0, col_select - 1);
        // Reset function key states //<>// //<>//
        for(int idx = 0; idx < letterStates.length; ++idx) { letterStates[idx] = 0; }
      } //<>//
  }
  
  updateScreen();
}


public void rt() {
  switch(mode) {
    case NAV_PROG_INST:
      col_select = min(col_select + 1, contents.get(row_select).size() - 1);
      updateScreen();
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
        } 
        else {
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
    hd();
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
    case NAV_PROG_INST:
      if(shift) {
        newMotionInstruction();
        col_select = 0;
      } else {
        nextScreen(Screen.SELECT_INSTR_INSERT);
      }
      break;
    case NAV_TOOL_FRAMES:
      if(shift) {
        // Reset the highlighted frame in the tool frame list
        toolFrames[row_select] = new ToolFrame();
        saveFrameBytes( new File(sketchPath("tmp/frames.bin")) );
        updateScreen();
      } else {
        // Set the current tool frame
        activeToolFrame = row_select;
        updateCoordFrame(armModel);
      }
      break;
    case NAV_USER_FRAMES:
      if(shift) {
        // Reset the highlighted frame in the user frames list
        userFrames[row_select] = new UserFrame();
        saveFrameBytes( new File(sketchPath("tmp/frames.bin")) );
        updateScreen();
      } else {
        // Set the current user frame
        activeUserFrame = row_select;
        updateCoordFrame(armModel);
      }
      break;
    case ACTIVE_FRAMES:
      if(row_select == 0) {
        nextScreen(Screen.NAV_TOOL_FRAMES);
      } else if(row_select == 1) {
        nextScreen(Screen.NAV_USER_FRAMES);
      }
    case TEACH_3PT_USER:
    case TEACH_3PT_TOOL:
    case TEACH_6PT:
    case TEACH_4PT:
      ref_point = (shift) ? null : armModel.getEEPos();
      updateScreen();
      break;
    case NAV_DREGS:
      // Clear Data Register entry
      DAT_REG[active_index] = new DataRegister();
      saveRegisterBytes( new File(sketchPath("tmp/registers.bin")) );
      break;
    case NAV_PREGS_J:
    case NAV_PREGS_C:
      // Clear Position Register entry
      GPOS_REG[active_index] = new PositionRegister();
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
      nextScreen(Screen.NEW_PROGRAM);
      break;
    case TFRAME_DETAIL:
      switchScreen(Screen.TOOL_FRAME_METHODS);
      //nextScreen(Screen.TOOL_FRAME_METHODS);
      break;
    case TEACH_3PT_TOOL:
    case TEACH_6PT:
    case DIRECT_ENTRY_TOOL:
      lastScreen();
      break;
    case UFRAME_DETAIL:
      switchScreen(Screen.USER_FRAME_METHODS);
      //nextScreen(Screen.USER_FRAME_METHODS);
      break;
    case TEACH_3PT_USER:
    case TEACH_4PT:
    case DIRECT_ENTRY_USER:
      lastScreen();
      break;
    case NAV_PREGS_J:
    case NAV_PREGS_C:
      switchScreen(Screen.SWAP_PT_TYPE);
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
      nextScreen(Screen.CONFIRM_PROG_DELETE);
      break;
    case SELECT_CUT_COPY:
      Program p = programs.get(active_prog);
      ArrayList<Instruction> inst = p.getInstructions();
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
      display_stack.pop();
      nextScreen(Screen.NAV_USER_FRAMES);
      break;
    case NAV_USER_FRAMES:
      display_stack.pop();
      nextScreen(Screen.NAV_TOOL_FRAMES);
      break;
    case NAV_DREGS:
      // Switch to Position Registers
      nextScreen(Screen.NAV_PREGS_C);
      break;
    case NAV_PREGS_J:
    case NAV_PREGS_C:
    // Switch to Data Registers
      nextScreen(Screen.NAV_DREGS);
      break;
    default:
      if (mode.type == ScreenType.TYPE_TEXT_ENTRY) {
        editTextEntry(2);
        updateScreen();
      }
  }
}


public void f4() {
  Program p;
  
  switch(mode) {
  case NAV_PROG_INST:
    p = programs.get(active_prog);
    if(p.instructions.size() == 0) break;
    Instruction ins = p.getInstructions().get(active_instr);
    opt_select = 0;
    workingText = "";
    
    if(ins instanceof MotionInstruction) {
      switch(col_select) {
        case 2: // motion type
          nextScreen(Screen.SET_MV_INSTRUCT_TYPE);
          break;
        case 3: // register type
          nextScreen(Screen.SET_MV_INSTRUCT_REG_TYPE);
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
      }
    } 
    else if(ins instanceof FrameInstruction) {
      switch(col_select) {
        case 1:
          nextScreen(Screen.SET_FRM_INSTR_TYPE);
          break;
        case 2:
          nextScreen(Screen.SET_FRM_INSTR_IDX);
          break;
      }
    }
    else if(ins instanceof IOInstruction) {
       switch(col_select) {
        case 1:
          nextScreen(Screen.SET_IO_INSTR_IDX);
          break;
        case 2:
          nextScreen(Screen.SET_IO_INSTR_STATE);
          break;
      }
    }
    else if(ins instanceof JumpInstruction){
      nextScreen(Screen.SET_JUMP_LBL);
    }
    break;
  case CONFIRM_INSERT:
    try {
      p = programs.get(active_prog);
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
        renderStartIdx = active_prog - row_select;
      }
      
      lastScreen();
      saveProgramBytes( new File(sketchPath("tmp/programs.bin")) );
    }
    break;
  case CONFIRM_INSTR_DELETE:
    p = programs.get(active_prog);
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
    display_stack.pop();
    updateInstructions();
    break;
  case SELECT_CUT_COPY:
    p = programs.get(active_prog);
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
    p = programs.get(active_prog);
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
    p = programs.get(active_prog);
    Point[] pTemp = new Point[1000];
    int posIdx = 0;
    int lblIdx = 0;
    
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
      //else if(instr instanceof LabelInstruction) {
      //  ((LabelInstruction)instr).labelNum = lblIdx;
      //  lblIdx += 1;
      //}
    }
    
    display_stack.pop();
    updateInstructions();
    break;
  case SELECT_DELETE:
      nextScreen(Screen.CONFIRM_INSTR_DELETE);
      break;
  case NAV_PREGS_J:
  case NAV_PREGS_C:
    if (shift) {
      // Move To function
      Point pt = GPOS_REG[active_index].point;
      
      if (pt != null) {
        // Move the Robot to the select point
        armModel.setupRotationInterpolation(pt.angles.clone());
        armModel.inMotion = true;
        currentInstruction = -2;
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
          armModel.setupRotationInterpolation(tgt.angles);
          armModel.inMotion = true;
          currentInstruction = -2;
        }
      }
    } else if (mode.type == ScreenType.TYPE_TEXT_ENTRY) {
      editTextEntry(3);
    }
  }
  
  updateScreen();
}

public void f5() {
  switch(mode){
    case NAV_PROG_INST:
      if(col_select == 0) {
        nextScreen(Screen.INSTRUCT_MENU_NAV);
      }
      else if(col_select == 2 || col_select == 3) {
        nextScreen(Screen.VIEW_INST_REG);
      }
      break;
    case TEACH_3PT_USER:
    case TEACH_3PT_TOOL:
    case TEACH_4PT:
    case TEACH_6PT:
      if (shift) {
        pushMatrix();
        resetMatrix();
        applyModelRotation(armModel, false);
        
        float[][] tMatrix = getTransformationMatrix();
        float[][] rMatrix = new float[3][3];
        popMatrix();
        
        for (int row = 0; row < 3; ++row) {
          for (int col = 0; col < 3; ++col) {
            // Transpose the rotation matrix portion of the transformation matrix
            rMatrix[row][col] = tMatrix[col][row];
          }
        }
        
        float[] orientation = matrixToQuat( rMatrix );
        Point curPosition = new Point(new PVector(tMatrix[0][3], tMatrix[1][3], tMatrix[2][3]), orientation);
        curPosition.angles = armModel.getJointAngles();
        // Save the current position of the Robot's Faceplate
        teachFrame.setPoint(curPosition, opt_select);
        saveFrameBytes( new File(sketchPath("tmp/frames.bin")) );
        updateScreen();
      }
      break;
    case CONFIRM_PROG_DELETE:
      opt_select = 0;
      
      lastScreen();
      break;
    case CONFIRM_INSTR_DELETE:
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
        // Save the Robot's current joint angles
        GPOS_REG[active_index].point = new Point(armModel.getEEPos(), armModel.getQuaternion(), armModel.getJointAngles());
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
  for(Model model : armModel.segments) {
    model.jointsMoving[0] = 0;
    model.jointsMoving[1] = 0;
    model.jointsMoving[2] = 0;
  }
  
  for(int idx = 0; idx < armModel.jogLinear.length; ++idx) {
    armModel.jogLinear[idx] = 0;
  }
  
  for(int idx = 0; idx < armModel.jogRot.length; ++idx) {
    armModel.jogRot[idx] = 0;
  }
  
  // Reset button highlighting
  for(int j = 1; j <= 6; ++j) {
    ((Button)cp5.get("JOINT" + j + "_NEG")).setColorBackground(BUTTON_DEFAULT);
    ((Button)cp5.get("JOINT" + j + "_POS")).setColorBackground(BUTTON_DEFAULT);
  }
  
  armModel.inMotion = false;
}

public void fd() {  
  if(!armModel.inMotion && shift) {
    currentProgram = programs.get(active_prog);
    executingInstruction = false;
    armModel.inMotion = true;
    
    if(step) {
      // Execute a single instruction
      currentInstruction = active_instr;
      execSingleInst = true;
      
      if(active_instr < currentProgram.getInstructions().size() - 1) {
        // Move to the next instruction
        boolean limbo = shift;
        shift = false;
        dn();
        shift = limbo;
      }
      
    } else {
      // Execute the whole program
      currentInstruction = active_instr;
      execSingleInst = false;
    }
  }
}

public void bd() {
  // If there is a previous instruction, then move to it and reverse its affects
  if(!armModel.inMotion && shift && step && active_instr > 0) {
    
    boolean limbo = shift;
    shift = false;
    up();
    shift = limbo;
    
    Instruction ins = programs.get(active_prog).getInstructions().get(active_instr);
    
    if(ins instanceof MotionInstruction) {
      currentProgram = programs.get(active_prog);
      executingInstruction = false;
      armModel.inMotion = true;
      currentInstruction = active_instr;
      execSingleInst = true;
      
      // Move backwards
      singleInstruction = (MotionInstruction)ins;
      setUpInstruction(currentProgram, armModel, singleInstruction);
    } else if(ins instanceof IOInstruction) {
      currentProgram = null;
      executingInstruction = false;
      armModel.inMotion = false;
      currentInstruction = -1;
      execSingleInst = true;
      
      IOInstruction tIns = (IOInstruction)ins;
      int status;
      
      if (tIns.state == ON) {
        status = OFF;
      } else {
        status = ON;
      }
      
      IOInstruction inverse = new IOInstruction(tIns.reg, status);
      // Reverse the tool status applied
      inverse.execute();
    }
  }
}

public void ENTER() {
  Program p;
  MotionInstruction m;
  
  switch(mode) {
    //Main menu
    case MAIN_MENU_NAV:
      if(opt_select == 5) { // SETUP
        nextScreen(Screen.SETUP_NAV);
      }
      break;
    //Setup menu
    case SETUP_NAV:
      nextScreen(Screen.SELECT_FRAME_MODE);
      break;
    case ACTIVE_FRAMES:
      updateActiveFramesDisplay();
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
    case NAV_TOOL_FRAMES:
      curFrameIdx = row_select;
      nextScreen(Screen.TFRAME_DETAIL);
      break;
    case NAV_USER_FRAMES:
      curFrameIdx = row_select;
      nextScreen(Screen.UFRAME_DETAIL);
      break;
    case USER_FRAME_METHODS:
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
    case TOOL_FRAME_METHODS:
      teachFrame = toolFrames[curFrameIdx];
      // Tool Frame traching methods
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
            println("All enetries must have a value!");
            return;
          }
          
          // Remove prefix
          inputs[val] = Float.parseFloat(str);
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
    case NEW_PROGRAM:
      println(workingText);
      if(!workingText.equals("\0")) {
        if (workingText.charAt(workingText.length() - 1) == '\0') {
          // Remove insert character
          workingText = workingText.substring(0, workingText.length() - 1);
        }
        
        int new_prog = addProgram(new Program(workingText));
        active_prog = new_prog;
        active_instr = 0;
        
        saveProgramBytes( new File(sketchPath("tmp/programs.bin")) );
        display_stack.pop();
        nextScreen(Screen.NAV_PROG_INST);
      }
      break;
    case NAV_PROGRAMS:
      if(programs.size() != 0) {
        active_instr = 0;
        nextScreen(Screen.NAV_PROG_INST);
      }
      break;
    case INSTRUCT_MENU_NAV:
      switch(opt_select) {
        case 0: //Insert
          nextScreen(Screen.CONFIRM_INSERT);
          break;
        case 1: //Delete
          p = programs.get(active_prog);
          selectedLines = resetSelection(p.getInstructions().size());
          nextScreen(Screen.SELECT_DELETE);
          break;
        case 2: //Cut/Copy
          p = programs.get(active_prog);
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
          p = programs.get(active_prog);
          selectedLines = resetSelection(p.getInstructions().size());
          nextScreen(Screen.SELECT_COMMENT);
          break;
        case 7: //Undo
        case 8: //Remark
      }
      break;
    case SELECT_INSTR_INSERT:
      switch(opt_select){
        case 0:
          newIOInstruction();
          lastScreen();
          break;
        case 1:
          // Offset/Frames
          newFrameInstruction();
          lastScreen();
          break;
        case 2:   
          nextScreen(Screen.INPUT_RSTMT);
          break;
        case 3:
          nextScreen(Screen.SELECT_IF_SEL);
          break;
        case 4:
          nextScreen(Screen.SELECT_JMP_LBL);
          break;
      }
      break;
    case INPUT_RSTMT:
      break;
    case SELECT_IF_SEL:
      if(opt_select == 0)
        //newIfStmt();
      //else
        //newSelStmt();
      
      display_stack.pop();
      lastScreen();
      break;
    case SELECT_JMP_LBL:
      if(opt_select == 0)
        newLabel();
      else
        newJumpInstruction();
      
      display_stack.pop();
      lastScreen();
      break;
    case SET_MV_INSTRUCT_TYPE:
      m = getActiveMotionInstruct();
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
    case SET_MV_INSTRUCT_REG_TYPE:
      m = getActiveMotionInstruct();
      if(opt_select == 0) {
        m.setGlobal(false);
      } else if(opt_select == 1) {
        
        if(GPOS_REG[m.positionNum].point == null) {
          // Invalid register index
          err = "This register is uninitailized!";
          return;
        } else {
          m.setGlobal(true);
        }
      }
      lastScreen();
      break;
    case SET_MV_INSTR_SPD:
      float tempSpeed = Float.parseFloat(workingText);
      if(tempSpeed >= 5.0) {
        if(speedInPercentage) {
          if(tempSpeed > 100) tempSpeed = 10; 
          tempSpeed /= 100.0;
        } else if(tempSpeed > armModel.motorSpeed) {
          tempSpeed = armModel.motorSpeed;
        }
        MotionInstruction castIns = getActiveMotionInstruct();
        castIns.setSpeed(tempSpeed);
        saveProgramBytes( new File(sketchPath("tmp/programs.bin")) );
      }
      
      lastScreen();
      break;
    case SET_MV_INSTR_IDX:
      try {
        int tempRegister = Integer.parseInt(workingText) - 1;
        MotionInstruction castIns = getActiveMotionInstruct();
        
        if(tempRegister < 0 || tempRegister > 1000) {
          // Invalid register index
          err = "Only registers 1 - 1000 are legal!";
          lastScreen();
          return;
        }
        
        if(castIns.globalRegister) {
          // Check global register
          if(GPOS_REG[tempRegister].point == null) {
            // Invalid register index
            err = "This register is uninitailized!";
            lastScreen();
            return;
          }
        }
        
        castIns.setPosition(tempRegister);
      } catch (NumberFormatException NFEx) { /* Ignore invalid numbers */ }
      
      lastScreen();
      break;
    case SET_MV_INSTR_TERM:
      try {
        float tempTerm = Float.parseFloat(workingText);
        
        if(tempTerm >= 0f && tempTerm <= 100f) {
          tempTerm /= 100f;
          MotionInstruction castIns = getActiveMotionInstruct();
          castIns.setTermination(tempTerm);
        }
      } catch (NumberFormatException NFEx) { /* Ignore invalid input */ }
      
      lastScreen();
      break;
    case SET_IO_INSTR_STATE:
      p = programs.get(active_prog);
      IOInstruction ioInst = (IOInstruction)p.getInstructions().get(active_instr);
    
      if(opt_select == 0)
        ioInst.setState(ON);
      else
        ioInst.setState(OFF);
        
      lastScreen();
      break;
    case SET_IO_INSTR_IDX:
      p = programs.get(active_prog);
      
      try {
        int tempReg = Integer.parseInt(workingText);
        
        if(tempReg >= 0 && tempReg < 6){
          ioInst = (IOInstruction)p.getInstructions().get(active_instr);
          ioInst.setReg(tempReg);
        }
      }
      catch (NumberFormatException NFEx){ /* Ignore invalid input */ }
      
      lastScreen();
      break;
    case SET_FRM_INSTR_TYPE:
      p = programs.get(active_prog);
      FrameInstruction fInst = (FrameInstruction)p.getInstructions().get(active_instr);
      
      if(opt_select == 0)
        fInst.setFrameType(FTYPE_TOOL);
      else
        fInst.setFrameType(FTYPE_USER);
        
      lastScreen();
      break;      
    case SET_FRM_INSTR_IDX:
      p = programs.get(active_prog);
      
      try {
        int tempReg = Integer.parseInt(workingText);
        
        if(tempReg >= 0 && tempReg < 6){
          fInst = (FrameInstruction)p.getInstructions().get(active_instr);
          fInst.setReg(tempReg);
        }
      }
      catch (NumberFormatException NFEx){ /* Ignore invalid input */ }
      
      lastScreen();
      break;
    case SET_JUMP_LBL:
      p = programs.get(active_prog);
      
      try {
        int tempLbl = Integer.parseInt(workingText);
        LabelInstruction l = p.getLabel(tempLbl);
        if(l != null){
          JumpInstruction jmp = (JumpInstruction)p.getInstruction(active_instr);
          jmp.tgtLabel = l;
        }
        else{
          err = "Invalid label number.";
        }
      }
      catch (NumberFormatException NFEx){ /* Ignore invalid input */ }
      
      lastScreen();
      break;
    case SELECT_CUT_COPY:
    case SELECT_DELETE:
      selectedLines[active_instr] = !selectedLines[active_instr];
      updateScreen();
      break;
    case SELECT_COMMENT:
      programs.get(active_prog)
      .getInstructions()
      .get(active_instr)
      .toggleCommented();
      
      updateScreen(); 
      break;
    case VIEW_INST_REG:
      lastScreen();
      break;
    case FIND_REPL:
      lastScreen();  
      break;
      
    case JUMP_TO_LINE:
      active_instr = Integer.parseInt(workingText)-1;
      if(active_instr < 0) active_instr = 0;
      if(active_instr >= programs.get(active_prog).getInstructions().size())
        active_instr = programs.get(active_prog).getInstructions().size()-1;
      
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
    case NAV_DATA:
      if(opt_select == 0) {
        // Data Register Menu
        nextScreen(Screen.NAV_DREGS);
      } else if(opt_select == 1) {
        // Position Register Menu
        nextScreen(Screen.NAV_PREGS_C);
      }
      break;
    case EDIT_DREG_VAL:   
      Float f = null;
      
      try {
        // Read inputted Float value
        f = Float.parseFloat(workingText);
        // Clamp the value between -9999 and 9999, inclusive
        f = max(-9999f, min(f, 9999f));
        
        if(active_index >= 0 && active_index < DAT_REG.length) {
          // Save inputted value
          DAT_REG[active_index].value = f;
          saveRegisterBytes( new File(sketchPath("tmp/registers.bin")) );
        }
      } catch (NumberFormatException NFEx) {
        // Invalid input value
        println("Value must be a real number!");
      }
      
      lastScreen();
      break;
    case NAV_DREGS:
      if(col_select == 1) {
        // Edit data register comment
        nextScreen(Screen.EDIT_DREG_COM);
      } else if(col_select == 2) {
        // Edit data register value
        nextScreen(Screen.EDIT_DREG_VAL);
      }
      break;
    case NAV_PREGS_J:
    case NAV_PREGS_C:   
      if(col_select == 1) {
        // Bring up comment menu
        nextScreen(Screen.EDIT_PREG_COM);
      } else if(col_select >= 2) {
        // Bring up Register editing menu
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
        DAT_REG[active_index].comment = workingText;
        saveRegisterBytes( new File(sketchPath("tmp/registers.bin")) );
        workingText = "";
        lastScreen();
      }
      break;
  }
}//End enter

public void ITEM() {
  if(mode == Screen.NAV_PROG_INST) {
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
    coordFrameTransition(armModel);
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


/* navigation buttons */
// zoomin button when interface is at full size
public void zoomin_normal() {
  myscale *= 1.1;
}

// zoomin button when interface is minimized
public void zoomin_shrink() {
  zoomin_normal();
}

// zoomout button when interface is at full size
public void zoomout_normal() {
  myscale *= 0.9;
}

// zoomout button when interface is minimized
public void zoomout_shrink() {
  zoomout_normal();
}

// pan button when interface is at full size
public void pan_normal() {
  clickPan += 1;
  if((clickPan % 2) == 1) {
    if((clickRotate % 2) == 1) {
      rotate_normal();
    }
    
    cursorMode = HAND;
    PImage[] pressed = {loadImage("images/pan_down.png"), 
      loadImage("images/pan_down.png"), 
      loadImage("images/pan_down.png")};
    
    cp5.getController("pan_normal")
    .setImages(pressed);
  }
  else {
    cursorMode = ARROW;
    PImage[] released = {loadImage("images/pan_35x20.png"), 
      loadImage("images/pan_over.png"), 
      loadImage("images/pan_down.png")};
    
    cp5.getController("pan_normal")
    .setImages(released);
    doPan = false;   
  }
  
  cursor(cursorMode);
}

// pan button when interface is minimized
public void pan_shrink() {
  pan_normal();
}

// rotate button when interface is at full size
public void rotate_normal() {
  clickRotate += 1;
  if((clickRotate % 2) == 1) {
    if((clickPan % 2) == 1) {
      pan_normal();
    }
    
    cursorMode = MOVE;
    PImage[] pressed = {loadImage("images/rotate_down.png"), 
      loadImage("images/rotate_down.png"), 
      loadImage("images/rotate_down.png")};
    
    cp5.getController("rotate_normal")
    .setImages(pressed);
  }
  else {
    cursorMode = ARROW;
    PImage[] released = {loadImage("images/rotate_35x20.png"), 
      loadImage("images/rotate_over.png"), 
      loadImage("images/rotate_down.png")};
    
    cp5.getController("rotate_normal")
    .setImages(released);
    doRotate = false;   
  }
  
  cursor(cursorMode);
}

// rotate button when interface is minized
public void rotate_shrink() {
  rotate_normal();
}

public void record_normal() {
  if(record == OFF) {
    record = ON;
    PImage[] record = {loadImage("images/record-on.png"), 
      loadImage("images/record-on.png"),
      loadImage("images/record-on.png")};   
    bt_record_normal.setImages(record);
    new Thread(new RecordScreen()).start();
  } else {
    record = OFF;
    PImage[] record = {loadImage("images/record-35x20.png"), 
      loadImage("images/record-over.png"), 
      loadImage("images/record-on.png")};   
    bt_record_normal.setImages(record);
    
  }
}

public void EE() {
  armModel.swapEndEffector();
}

public void JOINT1_NEG() {
  
  if (shift) {
    
    if (curCoordFrame == CoordFrame.JOINT) {
      // Move single joint
      activateLiveJointMotion(0, -1);
    } else {
      // Move entire robot in a single axis plane
      activateLiveWorldMotion(0, 1);
    }
    
    int c1 = ((Button)cp5.get("JOINT1_NEG")).getColor().getBackground();
    int c2 = ((Button)cp5.get("JOINT1_POS")).getColor().getBackground();
    
    if(c1 == BUTTON_DEFAULT && c2 == BUTTON_DEFAULT) {
      //both buttons have the default color, set this one to highlight
      ((Button)cp5.get("JOINT1_NEG")).setColorBackground(BUTTON_ACTIVE);
    }
    else {
      ((Button)cp5.get("JOINT1_NEG")).setColorBackground(BUTTON_DEFAULT);
      ((Button)cp5.get("JOINT1_POS")).setColorBackground(BUTTON_DEFAULT);
    }
  }
}

public void JOINT1_POS() {
  
  if (shift) {
    
    if(curCoordFrame == CoordFrame.JOINT) {
      // Move single joint
      activateLiveJointMotion(0, 1);
    } else  {
      // Move entire robot in a single axis plane
      activateLiveWorldMotion(0, -1);
    }
    
    int c1 = ((Button)cp5.get("JOINT1_NEG")).getColor().getBackground();
    int c2 = ((Button)cp5.get("JOINT1_POS")).getColor().getBackground();
    
    if(c1 == BUTTON_DEFAULT && c2 == BUTTON_DEFAULT) {
      //both buttons have the default color, set this one to highlight
      ((Button)cp5.get("JOINT1_POS")).setColorBackground(BUTTON_ACTIVE);
    }
    else {
      //stopping movement, set both buttons to default
      ((Button)cp5.get("JOINT1_NEG")).setColorBackground(BUTTON_DEFAULT);
      ((Button)cp5.get("JOINT1_POS")).setColorBackground(BUTTON_DEFAULT);
    }
  }
}

public void JOINT2_NEG() {
  
  if (shift) {
    
    if(curCoordFrame == CoordFrame.JOINT) {
      // Move single joint
      activateLiveJointMotion(1, -1);
    } else  {
      // Move entire robot in a single axis plane
      activateLiveWorldMotion(2, -1);
    }
    
    int c1 = ((Button)cp5.get("JOINT2_NEG")).getColor().getBackground();
    int c2 = ((Button)cp5.get("JOINT2_POS")).getColor().getBackground();
    
    if(c1 == BUTTON_DEFAULT && c2 == BUTTON_DEFAULT) {
      //both buttons have the default color, set this one to highlight
      ((Button)cp5.get("JOINT2_NEG")).setColorBackground(BUTTON_ACTIVE);
    }
    else {
      ((Button)cp5.get("JOINT2_NEG")).setColorBackground(BUTTON_DEFAULT);
      ((Button)cp5.get("JOINT2_POS")).setColorBackground(BUTTON_DEFAULT);
    }
  }
}

public void JOINT2_POS() {
  
  if (shift) {
    
    if(curCoordFrame == CoordFrame.JOINT) {
      // Move single joint
      activateLiveJointMotion(1, 1);
    } else  {
      // Move entire robot in a single axis plane
      activateLiveWorldMotion(2, 1);
    }
    
    int c1 = ((Button)cp5.get("JOINT2_NEG")).getColor().getBackground();
    int c2 = ((Button)cp5.get("JOINT2_POS")).getColor().getBackground();
    
    if(c1 == BUTTON_DEFAULT && c2 == BUTTON_DEFAULT) {
      //both buttons have the default color, set this one to highlight
      ((Button)cp5.get("JOINT2_POS")).setColorBackground(BUTTON_ACTIVE);
    }
    else {
      ((Button)cp5.get("JOINT2_NEG")).setColorBackground(BUTTON_DEFAULT);
      ((Button)cp5.get("JOINT2_POS")).setColorBackground(BUTTON_DEFAULT);
    }
  }
}

public void JOINT3_NEG() {
  
  if (shift) {
    
    if(curCoordFrame == CoordFrame.JOINT) {
      // Move single joint
      activateLiveJointMotion(2, -1);
    } else  {
      // Move entire robot in a single axis plane
      activateLiveWorldMotion(1, 1);
    }
    
    int c1 = ((Button)cp5.get("JOINT3_NEG")).getColor().getBackground();
    int c2 = ((Button)cp5.get("JOINT3_POS")).getColor().getBackground();
    
    if(c1 == BUTTON_DEFAULT && c2 == BUTTON_DEFAULT) {
      //both buttons have the default color, set this one to highlight
      ((Button)cp5.get("JOINT3_NEG")).setColorBackground(BUTTON_ACTIVE);
    }
    else {
      ((Button)cp5.get("JOINT3_NEG")).setColorBackground(BUTTON_DEFAULT);
      ((Button)cp5.get("JOINT3_POS")).setColorBackground(BUTTON_DEFAULT);
    }
  }
}

public void JOINT3_POS() {
  
  if (shift) {
    
    if(curCoordFrame == CoordFrame.JOINT) {
      // Move single joint
      activateLiveJointMotion(2, 1);
    } else  {
      // Move entire robot in a single axis plane
      activateLiveWorldMotion(1, -1);
    }
    
    int c1 = ((Button)cp5.get("JOINT3_NEG")).getColor().getBackground();
    int c2 = ((Button)cp5.get("JOINT3_POS")).getColor().getBackground();
    
    if(c1 == BUTTON_DEFAULT && c2 == BUTTON_DEFAULT) {
      //both buttons have the default color, set this one to highlight
      ((Button)cp5.get("JOINT3_POS")).setColorBackground(BUTTON_ACTIVE);
    }
    else {
      ((Button)cp5.get("JOINT3_NEG")).setColorBackground(BUTTON_DEFAULT);
      ((Button)cp5.get("JOINT3_POS")).setColorBackground(BUTTON_DEFAULT);
    }
  }
}

public void JOINT4_NEG() {
  
  if (shift) {
    
    if(curCoordFrame == CoordFrame.JOINT) {
      // Move single joint
      activateLiveJointMotion(3, -1);
    } else  {
      // Move entire robot in a single axis plane
      activateLiveWorldMotion(3, -1);
    }
    
    int c1 = ((Button)cp5.get("JOINT4_NEG")).getColor().getBackground();
    int c2 = ((Button)cp5.get("JOINT4_POS")).getColor().getBackground();
    
    if(c1 == BUTTON_DEFAULT && c2 == BUTTON_DEFAULT) {
      //both buttons have the default color, set this one to highlight
      ((Button)cp5.get("JOINT4_NEG")).setColorBackground(BUTTON_ACTIVE);
    }
    else {
      ((Button)cp5.get("JOINT4_NEG")).setColorBackground(BUTTON_DEFAULT);
      ((Button)cp5.get("JOINT4_POS")).setColorBackground(BUTTON_DEFAULT);
    }
  }
}

public void JOINT4_POS() {
  
  if (shift) {
    
    if(curCoordFrame == CoordFrame.JOINT) {
      // Move single joint
      activateLiveJointMotion(3, 1);
    } else {
      // Move entire robot in a single axis plane
      activateLiveWorldMotion(3, 1);
    }
    
    int c1 = ((Button)cp5.get("JOINT4_NEG")).getColor().getBackground();
    int c2 = ((Button)cp5.get("JOINT4_POS")).getColor().getBackground();
    
    if(c1 == BUTTON_DEFAULT && c2 == BUTTON_DEFAULT) {
      //both buttons have the default color, set this one to highlight
      ((Button)cp5.get("JOINT4_POS")).setColorBackground(BUTTON_ACTIVE);
    }
    else {
      ((Button)cp5.get("JOINT4_NEG")).setColorBackground(BUTTON_DEFAULT);
      ((Button)cp5.get("JOINT4_POS")).setColorBackground(BUTTON_DEFAULT);
  }
  }
}

public void JOINT5_NEG() {
  
  if (shift) {
    
    if(curCoordFrame == CoordFrame.JOINT) {
      // Move single joint
      activateLiveJointMotion(4, -1);
    } else {
      // Move entire robot in a single axis plane
      activateLiveWorldMotion(5, -1);
    }
    
    int c1 = ((Button)cp5.get("JOINT5_NEG")).getColor().getBackground();
    int c2 = ((Button)cp5.get("JOINT5_POS")).getColor().getBackground();
    
    if(c1 == BUTTON_DEFAULT && c2 == BUTTON_DEFAULT) {
      //both buttons have the default color, set this one to highlight
      ((Button)cp5.get("JOINT5_NEG")).setColorBackground(BUTTON_ACTIVE);
    }
    else {
      ((Button)cp5.get("JOINT5_NEG")).setColorBackground(BUTTON_DEFAULT);
      ((Button)cp5.get("JOINT5_POS")).setColorBackground(BUTTON_DEFAULT);
    }
  }
}

public void JOINT5_POS() {
  
  if (shift) {
    
    if(curCoordFrame == CoordFrame.JOINT) {
      // Move single joint
      activateLiveJointMotion(4, 1);
    } else {
      // Move entire robot in a single axis plane
      activateLiveWorldMotion(5, 1);
    }
    
    int c1 = ((Button)cp5.get("JOINT5_NEG")).getColor().getBackground();
    int c2 = ((Button)cp5.get("JOINT5_POS")).getColor().getBackground();
    
    if(c1 == BUTTON_DEFAULT && c2 == BUTTON_DEFAULT) {
      //both buttons have the default color, set this one to highlight
      ((Button)cp5.get("JOINT5_POS")).setColorBackground(BUTTON_ACTIVE);
    }
    else {
      ((Button)cp5.get("JOINT5_NEG")).setColorBackground(BUTTON_DEFAULT);
      ((Button)cp5.get("JOINT5_POS")).setColorBackground(BUTTON_DEFAULT);
    }
  }
}

public void JOINT6_NEG() {
  
  if (shift) {
    
    if(curCoordFrame == CoordFrame.JOINT) {
      // Move single joint
      activateLiveJointMotion(5, -1);
    } else {
      // Move entire robot in a single axis plane
      activateLiveWorldMotion(4, -1);
    }
    
    int c1 = ((Button)cp5.get("JOINT6_NEG")).getColor().getBackground();
    int c2 = ((Button)cp5.get("JOINT6_POS")).getColor().getBackground();
    
    if(c1 == BUTTON_DEFAULT && c2 == BUTTON_DEFAULT) {
      //both buttons have the default color, set this one to highlight
      ((Button)cp5.get("JOINT6_NEG")).setColorBackground(BUTTON_ACTIVE);
    }
    else {
      ((Button)cp5.get("JOINT6_NEG")).setColorBackground(BUTTON_DEFAULT);
      ((Button)cp5.get("JOINT6_POS")).setColorBackground(BUTTON_DEFAULT);
    }
  }
}

public void JOINT6_POS() {
  
  if (shift) {
    
    if(curCoordFrame == CoordFrame.JOINT) {
      // Move single joint
      activateLiveJointMotion(5, 1);
    } else {
      // Move entire robot in a single axis plane
      activateLiveWorldMotion(4, 1);
    }
    
    int c1 = ((Button)cp5.get("JOINT6_NEG")).getColor().getBackground();
    int c2 = ((Button)cp5.get("JOINT6_POS")).getColor().getBackground();
    
    if(c1 == BUTTON_DEFAULT && c2 == BUTTON_DEFAULT) {
      //both buttons have the default color, set this one to highlight
      ((Button)cp5.get("JOINT6_POS")).setColorBackground(BUTTON_ACTIVE);
    }
    else {
      ((Button)cp5.get("JOINT6_NEG")).setColorBackground(BUTTON_DEFAULT);
      ((Button)cp5.get("JOINT6_POS")).setColorBackground(BUTTON_DEFAULT);
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
 */
public void activateLiveJointMotion(int joint, int dir) {
  
  if(armModel.segments.size() >= joint+1) {

    Model model = armModel.segments.get(joint);
    // Checks all rotational axes
    for(int n = 0; n < 3; n++) {
      
      if(model.rotations[n]) {
        
        if(model.jointsMoving[n] == 0) {
          model.jointsMoving[n] = dir;
        } else {
          model.jointsMoving[n] = 0;
        }
      }
    }
  }
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
                      x - 0, y - 2, z - 1, w - 3, p - 5, r - 4
 * @pararm dir        +1 or -1: indicating the direction of motion
 *
 */
public void activateLiveWorldMotion(int axis, int dir) {
  armModel.tgtPos = armModel.getEEPos();
  armModel.tgtRot = armModel.getQuaternion();
  
  if(axis >= 0 && axis < 3) {
    if(armModel.jogLinear[axis] == 0) {
      //Begin movement on the given axis in the given direction
      armModel.jogLinear[axis] = dir;
    } else {
      //Halt movement
      armModel.jogLinear[axis] = 0;
    }
  }
  else if(axis >= 3 && axis < 6) {
    axis -= 3;
    if(armModel.jogRot[axis] == 0) {
      armModel.jogRot[axis] = dir;
    }
    else {
      armModel.jogRot[axis] = 0;
    }
  }
}

//turn of highlighting on all active movement buttons
public void resetButtonColors() {
  for(int i = 1; i <= 6; i += 1) {
    ((Button)cp5.get("JOINT"+i+"_NEG")).setColorBackground(BUTTON_DEFAULT);
    ((Button)cp5.get("JOINT"+i+"_POS")).setColorBackground(BUTTON_DEFAULT);
  }
}

//set button highlights for togglable buttons
public void updateButtonColors() {
  for(int i = 0; i < 6; i += 1) {
    Model m = armModel.segments.get(i);
    for(int j = 0; j < 3; j += 1) {
      if(m.rotations[j] && m.jointsMoving[j] == 0) {
        ((Button)cp5.get("JOINT"+(i+1)+"_NEG")).setColorBackground(BUTTON_DEFAULT);
        ((Button)cp5.get("JOINT"+(i+1)+"_POS")).setColorBackground(BUTTON_DEFAULT);
      }
    }
  }
}

/**
 * Transitions the display to the given screen and pushes that screen
 * onto the stack.
 * 
 * @param next    The new screen mode
 */
public void nextScreen(Screen next) {
  if (DISPLAY_TEST_OUTPUT) { System.out.printf("%s => %s\n", mode, next);  }
  
  mode = next;
  display_stack.push(mode);
  loadScreen();
  updateScreen();
}

/**
 * Transitions to the given screen without saving the current screen on the stack.
 * 
 * @param nextScreen  The new screen mode
 */
public void switchScreen(Screen nextScreen) {
  if (DISPLAY_TEST_OUTPUT) { System.out.printf("%s => %s\n", mode, nextScreen);  }
  
  mode = nextScreen;
  display_stack.pop();
  display_stack.push(mode);
  loadScreen();
  updateScreen();
}

/**
 * Transitions the display to the previous screen that the user was on.
 */
public boolean lastScreen() {
  opt_select = 0;
  
  if (display_stack.peek() == Screen.DEFAULT) {
    if (DISPLAY_TEST_OUTPUT) { System.out.printf("%s\n", mode); }
    return false;
  }
  else{
    display_stack.pop();
    if (DISPLAY_TEST_OUTPUT) { System.out.printf("%s => %s\n", mode, display_stack.peek()); }
    mode = display_stack.peek();
    
    updateScreen();
    return true;
  }
}

public void resetStack(){
  display_stack.clear();
  
  mode = Screen.DEFAULT;
  display_stack.push(mode);
}

public void loadScreen(){
  /*contents = new ArrayList<ArrayList<String>>();
  options = new ArrayList<String>();*/
  
  switch(mode){
    //Main menu
    case MAIN_MENU_NAV:
      opt_select = 0;
      break;
    
    //Programs and instructions
    case NEW_PROGRAM:
      row_select = 1;
      col_select = 0;
      opt_select = 0;
      workingText = "\0";
      break;
    case NAV_PROG_INST:
      row_select = 0;
      col_select = 0;
      opt_select = -1;
      break;
    case CONFIRM_INSERT:
      workingText = "";
      break;
    case SELECT_INSTR_INSERT:
    case SELECT_JMP_LBL:
    case SELECT_IF_SEL:
    case SET_IO_INSTR_STATE:
    case SET_FRM_INSTR_TYPE:
      opt_select = 0;
      break;
    case SET_FRM_INSTR_IDX:
      workingText = "";
      break;
    case SELECT_DELETE:
    case SELECT_COMMENT:
    case SELECT_CUT_COPY:
      int size = programs.get(active_prog).getInstructions().size();
      row_select = max(0, min(row_select, size));
      break;
          
    //Frames
    case SETUP_NAV:
      opt_select = 0;
      break;
    case NAV_DREGS:
    case NAV_PREGS_J:
    case NAV_PREGS_C:
      active_index = 0;
      row_select = 0;
      col_select = 0;
      renderStartIdx = 0;
      break;
    case ACTIVE_FRAMES:
      row_select = 0;
      col_select = 1;
      workingText = Integer.toString(activeToolFrame + 1);
      break;
    case SELECT_FRAME_MODE:
      opt_select = 0;
      break;
    case NAV_TOOL_FRAMES:
    case NAV_USER_FRAMES:
      row_select = 0;
      col_select = -1;
      break;
    case TFRAME_DETAIL:
    case UFRAME_DETAIL:
      row_select = -1;
      col_select = -1;
      opt_select = -1;
      break;
    case TOOL_FRAME_METHODS:
    case USER_FRAME_METHODS:
      row_select = -1;
      col_select = -1;
      opt_select = 0;
      break;
    
    //Registers
    case DIRECT_ENTRY_TOOL:
    case DIRECT_ENTRY_USER:
      row_select = 0;
      col_select = 1;
      contents = loadFrameDirectEntry(teachFrame);
      options = new ArrayList<String>();
      break;
    case NAV_PROGRAMS:
      // Stop Robot movement (i.e. program execution)
      hd();
      active_prog = 0;
      opt_select = 0;
      renderStartIdx = 0;
      break;
    case INSTRUCT_MENU_NAV:
      opt_select = 0;
      break;
    case VIEW_INST_REG:
      opt_select = -1;
      break;
    case NAV_DATA:
    case SWAP_PT_TYPE:
      opt_select = 0;
      break;
    case EDIT_DREG_COM:
      row_select = 1;
      col_select = 0;
      opt_select = 0;
      
      if(DAT_REG[active_index].comment != null) {
        workingText = DAT_REG[active_index].comment;
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
        System.out.printf("_%s_", DAT_REG[active_index].comment);
        workingText = GPOS_REG[active_index].comment;
      }
      else {
        workingText = "\0";
      }
      break;
    case EDIT_DREG_VAL:
      opt_select = 0;
      // Bring up float input menu
      if(DAT_REG[active_index].value != null) {
        workingText = Float.toString(DAT_REG[active_index].value);
      } else {
        workingText = "0.0";
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
}

// update text displayed on screen
public void updateScreen() {
  int next_px = display_px;
  int next_py = display_py;
  int c1, c2;
  
  clearScreen();
  
  // draw display background
  cp5.addTextarea("txt")
  .setPosition(display_px,display_py)
  .setSize(display_width, display_height)
  .setColorBackground(TEXT_DEFAULT)
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
    .setColorValue(TEXT_DEFAULT)
    .setColorBackground(TEXT_HIGHLIGHT)
    .hideScrollbar()
    .show()
    .moveTo(g1);
    
    next_px = display_px;
    next_py += 20;
  }
  
  contents = getContents(mode);
  options = getOptions(mode);
  
  boolean selectMode = false;
  if(mode.getType() == ScreenType.TYPE_LINE_SELECT)
    selectMode = true;
  
  // display the main list on screen
  index_contents = 1;
  for(int i = 0; i < contents.size(); i += 1) {
    ArrayList<String> temp = contents.get(i);
        
    if(i == row_select) { c1 = TEXT_HIGHLIGHT; }
    else                { c1 = TEXT_DEFAULT;   }
    
    //leading row select indicator []
    cp5.addTextarea(Integer.toString(index_contents))
    .setText("")
    .setPosition(next_px, next_py)
    .setSize(10, 20)
    .setColorBackground(c1)
    .hideScrollbar()
    .moveTo(g1);
    
    index_contents++;
    next_px += 10;
     
    for(int j = 0; j < temp.size(); j += 1) {
      if(i == row_select) {
        if(j != col_select && !selectMode){
          //highlight selected row
          c1 = TEXT_DEFAULT;
          c2 = TEXT_HIGHLIGHT;          
        } 
        else if(selectMode && !selectedLines[renderStartIdx + i]){
          c1 = TEXT_DEFAULT;
          c2 = TEXT_HIGHLIGHT;
        }
        else {
          //contrast selected column from selected row
          c1 = TEXT_HIGHLIGHT;
          c2 = TEXT_DEFAULT;
        }
      } else if(selectMode && selectedLines[renderStartIdx + i]) {
        //highlight any currently selected lines
        c1 = TEXT_DEFAULT;
        c2 = TEXT_HIGHLIGHT;
      } else {
        //display normal row
        c1 = TEXT_HIGHLIGHT;
        c2 = TEXT_DEFAULT;
      }
      
      //grey text for comme also this
      if(temp.size() > 0 && temp.get(0).contains("//")){
        c1 = color(127);
      }
      
      cp5.addTextarea(Integer.toString(index_contents))
      .setText(temp.get(j))
      .setFont(fnt_con14)
      .setPosition(next_px, next_py)
      .setSize(temp.get(j).length()*8 + 20, 20)
      .setColorValue(c1)
      .setColorBackground(c2)
      .hideScrollbar()
      .moveTo(g1);
      
      index_contents++;
      next_px += temp.get(j).length() * 8 + 18; 
    }
    
    if(i == row_select) { c1 = TEXT_HIGHLIGHT; }
    else                { c1 = TEXT_DEFAULT;   }
    
    //Trailing row select indicator []
    cp5.addTextarea(Integer.toString(index_contents))
    .setText("")
    .setPosition(next_px, next_py)
    .setSize(10, 20)
    .setColorBackground(c1)
    .hideScrollbar()
    .moveTo(g1);
    
    index_contents++;
    next_px = display_px;
    next_py += 20;
  }
  
  // display options for an element being edited
  if(contents.size() != 0)
    next_py += 20;
  
  index_options = 100;
  for(int i = 0; i < options.size(); i += 1) {   
    if(i == opt_select) {
      c1 = TEXT_DEFAULT;
      c2 = TEXT_HIGHLIGHT;
    }
    else{
      c1 = TEXT_HIGHLIGHT;
      c2 = TEXT_DEFAULT;
    }
    
    cp5.addTextarea(Integer.toString(index_options))
    .setText(" " + options.get(i))
    .setFont(fnt_con14)
    .setPosition(next_px, next_py)
    .setSize(options.get(i).length()*8 + 40, 20)
    .setColorValue(c1)
    .setColorBackground(c2)
    .hideScrollbar()
    .moveTo(g1);
    
    index_options++;
    next_px = display_px;
    next_py += 20;    
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
      .setColorValue(TEXT_DEFAULT)
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
      .setColorValue(TEXT_DEFAULT)
      .setColorBackground(color(255, 0, 0))
      .hideScrollbar()
      .moveTo(g1);
    }
    
    index_nums++;
    next_px += options.get(i).length()*8 + 18;   
  }
  
  // display hints for function keys
  String[] funct;
  funct = getFunctionLabels(mode);
    
  //set f button text labels
  for(int i = 0; i < 5; i += 1) {
    cp5.addTextarea("lf"+i)
    .setText(funct[i])
    .setFont(fnt_con12)
    .setPosition(display_width*i/5 + 15 , display_height)
    .setSize(display_width/5 - 5, 20)
    .setColorValue(TEXT_HIGHLIGHT)
    .setColorBackground(TEXT_DEFAULT)
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
    case NEW_PROGRAM:
      header = "NAME PROGRAM";
      break;
    case CONFIRM_INSTR_DELETE:
    case CONFIRM_INSERT:
    case CONFIRM_RENUM:
    case NAV_PROG_INST:
    case INSTRUCT_MENU_NAV:
    case SET_MV_INSTR_SPD:
    case SET_MV_INSTR_IDX:
    case SET_MV_INSTR_TERM:
    case SELECT_INSTR_INSERT:
    case SET_IO_INSTR_STATE:
    case SET_FRM_INSTR_TYPE:
    case SET_FRM_INSTR_IDX:
    case SET_JUMP_LBL:
    case SELECT_CUT_COPY:    
    case SELECT_DELETE:
    case VIEW_INST_REG:
      header = programs.get(active_prog).getName();
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
    case TOOL_FRAME_METHODS:
      header = String.format("TOOL FRAME: %d", curFrameIdx + 1);
      break;
    case USER_FRAME_METHODS:
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
      header = "REGISTERS";
      break;
    case NAV_PREGS_J:
    case NAV_PREGS_C:
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
public ArrayList<ArrayList<String>> getContents(Screen mode){
  ArrayList<ArrayList<String>> contents = new ArrayList<ArrayList<String>>();
  
  switch(mode) {
    case NEW_PROGRAM:
      contents = loadTextInput();
      break;
    
    //View instructions
    case CONFIRM_INSTR_DELETE:
    case CONFIRM_INSERT:
    case CONFIRM_RENUM:
    case FIND_REPL:
    case NAV_PROG_INST:
    case SELECT_DELETE:
    case SELECT_COMMENT:
    case SELECT_CUT_COPY:
      contents = loadInstructions(active_prog);
      if(mode.getType() == ScreenType.TYPE_LINE_SELECT)
        contents.remove(contents.size() - 1);
      break;
    case ACTIVE_FRAMES:
      /* workingText corresponds to the active row's index display */
      if (row_select == 0) {
        contents.add( newLine("Tool: ", workingText) );
        contents.add( newLine("User: ", Integer.toString(activeUserFrame + 1)) );
      } else {
        contents.add( newLine("Tool: ", Integer.toString(activeToolFrame + 1)) );
        contents.add( newLine("User: ", workingText) );
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
    case TOOL_FRAME_METHODS:
    case USER_FRAME_METHODS:
    case DIRECT_ENTRY_USER:
    case DIRECT_ENTRY_TOOL:
    case EDIT_DREG_VAL:
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
    //Program list navigation/ edit
    case NAV_PROGRAMS:
      options = loadPrograms();
      break;
    //Main menu and submenus
    case MAIN_MENU_NAV:
      options.add("1 UTILITIES (NA)"   );
      options.add("2 TEST CYCLE (NA)"  );
      options.add("3 MANUAL FCTNS (NA)");
      options.add("4 ALARM (NA)"       );
      options.add("5 I/O (NA)"         );
      options.add("6 SETUP"            );  
      options.add("7 FILE (NA)"        );
      options.add("8 USER (NA)"        );
      break;
    case SETUP_NAV:
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
    case INSTRUCT_MENU_NAV:
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
    case SELECT_DELETE:
      options.add("Select lines to delete.");
      break;
    case CONFIRM_INSTR_DELETE:
      options.add("Delete selected lines?");
      break;
    case SELECT_CUT_COPY:
      options.add("Select lines to cut/ copy.");
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
    case SET_MV_INSTRUCT_TYPE:
    case SET_MV_INSTRUCT_REG_TYPE:
    case SET_MV_INSTR_IDX:
    case SET_MV_INSTR_SPD:
    case SET_MV_INSTR_TERM:
    case SET_FRM_INSTR_TYPE:
    case SET_FRM_INSTR_IDX:
    case SET_IO_INSTR_STATE:
    case SET_IO_INSTR_IDX:
    case SET_JUMP_LBL:
      options = loadInstructEdit(mode);
      break;
    
    //Insert instructions (non-movemet)
    case SELECT_INSTR_INSERT:
      options.add("1. I/O"       );
      options.add("2. Frames"    );
      options.add("3. Registers" );
      options.add("4. IF/SELECT (NA)" );
      options.add("5. JMP/LBL"   );
      options.add("6. CALL (NA)"      );
      options.add("7. WAIT (NA)"      );
      options.add("8. Macro (NA)"     );
      break;
    case SELECT_IF_SEL:
      options.add("1. IF Stmt");
      options.add("2. SEL Stmt");
      break;
    case SELECT_JMP_LBL:
      options.add("1. Insert LBL");
      options.add("2. Insert JMP");
      break;
      
    //Frame navigation and edit menus
    case SELECT_FRAME_MODE:
      options.add("1. Tool Frame");
      options.add("2. User Frame");
      break;
    case TOOL_FRAME_METHODS:
      options.add("1. Three Point Method");
      options.add("2. Six Point Method");
      options.add("3. Direct Entry Method");
      break;
    case USER_FRAME_METHODS:
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
      funct[0] = "";
      funct[1] = "[Create]";
      funct[2] = "[Delete]";
      funct[3] = "";
      funct[4] = "";
      break;
    case NAV_PROG_INST:
      // F1, F4, F5
      if(shift) {
        funct[0] = "[New Pt]";
        funct[1] = "";
        funct[2] = "";
        funct[3] = "[Edit]";
        funct[4] = "[Opt]";
      } else {
        funct[0] = "[New Ins]";
        funct[1] = "";
        funct[2] = "";
        funct[3] = "[Edit]";
        funct[4] = "[Opt]";
      }
      break;
    case SELECT_DELETE:
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
      if(shift) {
        funct[0] = "[Rmv Ref]";
        funct[1] = "[Method]";
        funct[2] = "";
        funct[3] = "[Mov To]";
        funct[4] = "[Record]";
      } else {
        funct[0] = "[Sav Ref]";
        funct[1] = "[Method]";
        funct[2] = "";
        funct[3] = "";
        funct[4] = "";
      }
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
      // F1, F2
      if (shift) {
        funct[0] = "[Clear]";
        funct[1] = "[Type]";
        funct[2] = "[Switch]";
        funct[3] = "[Move To]";
        funct[4] = "[Record]";
      } else {
        funct[0] = "[Clear]";
        funct[1] = "[Type]";
        funct[2] = "[Switch]";
        funct[3] = "";
        funct[4] = "";
      }
     break;
    case NAV_DREGS:
      // F2
      funct[0] = "[Clear]";
      funct[1] = "";
      funct[2] = "[Switch]";
      funct[3] = "";
      funct[4] = "";
      break;
    case CONFIRM_INSERT:
    case CONFIRM_PROG_DELETE:
    case CONFIRM_INSTR_DELETE:
    case CONFIRM_RENUM:
    case FIND_REPL:
      // F4, F5
      funct[0] = "";
      funct[1] = "";
      funct[2] = "";
      funct[3] = "[CONFIRM]";
      funct[4] = "[CANCEL]";
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
    cp5.remove(t.getName());
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

ArrayList<String> loadPrograms() {
  ArrayList<String> progs = new ArrayList<String>();
  int size = programs.size();
  active_instr = 0;
   
  int start = renderStartIdx;
  int end = min(start + ITEMS_TO_SHOW, size);
  
  for(int i = start; i < end; i += 1) {
    progs.add(programs.get(i).getName());
  }
  
  return progs;
}

// prepare for displaying motion instructions on screen
public ArrayList<ArrayList<String>> loadInstructions(int programID) {
  ArrayList<ArrayList<String>> instruct_list = new ArrayList<ArrayList<String>>();
  if(programs.size() == 0) return instruct_list;
  
  Program p = programs.get(programID);
  int size = p.getInstructions().size();
  int start = renderStartIdx;
  int end = min(start + ITEMS_TO_SHOW, size + 1);
  
  for(int i = start; i < end; i+= 1) {
    if(i == size){
      instruct_list.add(newLine("[END]")); 
    }
    else {
      Instruction instr = p.getInstructions().get(i);
      ArrayList<String> m = new ArrayList<String>();
      
      if(instr.isCommented())
        m.add("//"+Integer.toString(i+1) + ")");
      else
        m.add(Integer.toString(i+1) + ")");
      
      if(instr instanceof MotionInstruction) {
        MotionInstruction a = (MotionInstruction)instr;
        
        if(armModel.getEEPos().dist(a.getVector(p).position) < (liveSpeed / 100f)) {
          m.add("@");
        }
        else {
          m.add("\0");
        }
        
        // add motion type
        switch(a.getMotionType()) {
          case MTYPE_JOINT:
            m.add("J");
            break;
          case MTYPE_LINEAR:
            m.add("L");
            break;
          case MTYPE_CIRCULAR:
            m.add("C");
            break; 
        }
        
        // load register no, speed and termination type
        if(a.getGlobal()) m.add("PR[");
        else m.add("P[");
        
        m.add((a.getPosition() + 1) +"]");
        
        if(a.getMotionType() == MTYPE_JOINT) m.add((a.getSpeed() * 100) + "%");
        else m.add((int)(a.getSpeed()) + "mm/s");
        
        if(a.getTermination() == 0) m.add("FINE");
        else m.add("CONT" + (int)(a.getTermination()*100));
      } 
      else if(instr instanceof FrameInstruction){
        FrameInstruction a = (FrameInstruction)instr;
        
        if(a.frameType == FTYPE_TOOL){
          m.add("TFRAME_NUM =");
        } else{
          m.add("UFRAME_NUM =");
        }
        
        m.add(""+a.getReg());
      }
      else if(instr instanceof IOInstruction){
        IOInstruction a = (IOInstruction)instr;
        
        m.add("IO[" + a.getReg() + "]=");
        
        if(a.getState() == ON){
          m.add("ON");
        } else {
          m.add("OFF");
        }
      }
      else {
        m.add(instr.toString());
      }
      
      instruct_list.add(m);
    }
  }
   
  return instruct_list;
}

/**
 * Deals with updating the UI after confirming/canceling a deletion
 */
public void updateInstructions() {
  Program prog = programs.get(active_prog);
  
  active_instr = min(active_instr,  prog.getInstructions().size() - 1);
  row_select = min(active_instr, ITEMS_TO_SHOW - 1);
  col_select = 0;
  renderStartIdx = active_instr - row_select;
  
  lastScreen();
}

public ArrayList<String> loadInstructEdit(Screen mode) {
  ArrayList<String> edit = new ArrayList<String>();
  
  switch(mode){
    case SET_MV_INSTRUCT_TYPE:
      edit.add("1.JOINT");
      edit.add("2.LINEAR");
      edit.add("3.CIRCULAR");
      break;
    case SET_MV_INSTRUCT_REG_TYPE:
      edit.add("1.LOCAL(P)");
      edit.add("2.GLOBAL(PR)");
      break;
    case SET_MV_INSTR_IDX:
      edit.add("Enter desired register number (1-1000)");
      edit.add("\0" + workingText);
      break;
    case SET_MV_INSTR_SPD:
      edit.add("Enter desired speed");
      MotionInstruction castIns = getActiveMotionInstruct();
      
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
    case SET_FRM_INSTR_TYPE:
      edit.add("1. Tool Frame");
      edit.add("2. User Frame");
      break;
    case SET_FRM_INSTR_IDX:
      edit.add("Select frame index:");
      edit.add("\0" + workingText);
      break;
    case SET_IO_INSTR_STATE:
      edit.add("1. IO ON");
      edit.add("2. IO OFF");
      break;
    case SET_IO_INSTR_IDX:
      edit.add("Select I/O register index:");
      edit.add("\0" + workingText);
      break;
    case SET_JUMP_LBL:
      edit.add("Set jump target label:");
      edit.add("\0" + workingText);
    default:
      break;
  }
  
  return edit;
}

public ArrayList<String> loadInstructionReg() {
  ArrayList<String> instReg = new ArrayList<String>();
  // show register contents if you're highlighting a register
  Instruction ins = programs.get(active_prog).getInstructions().get(active_instr);
  if(ins instanceof MotionInstruction) {
    MotionInstruction castIns = (MotionInstruction)ins;
    Point p = castIns.getVector(programs.get(active_prog));
    
    instReg.add("Position values (press ENTER to exit):");
    
    if(castIns.getMotionType() != MTYPE_JOINT) {
      // Show the vector in terms of the World Frame
      PVector wPos = convertNativeToWorld(p.position);
      instReg.add( String.format("X: %5.4f  Y: %5.4f  Z: %5.4f", wPos.x, wPos.y, wPos.z) );
      PVector wpr = quatToEuler(p.orientation);
      // Show angles in degrees
      instReg.add( String.format("W: %5.4f  P: %5.4f  R: %5.4f", 
      (wpr.x * RAD_TO_DEG), 
      (wpr.y * RAD_TO_DEG), 
      (wpr.z * RAD_TO_DEG)));
    }
    else {  
      instReg.add( String.format("J1: %5.4f  J2: %5.4f  J3: %5.4f", 
      (p.angles[0] * RAD_TO_DEG), 
      (p.angles[1] * RAD_TO_DEG), 
      (p.angles[2] * RAD_TO_DEG)));
      instReg.add( String.format("J4: %5.4f  J5: %5.4f  J6: %5.4f", 
      (p.angles[3] * RAD_TO_DEG), 
      (p.angles[4] * RAD_TO_DEG),
      (p.angles[5] * RAD_TO_DEG)));
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
  // overwrite current instruction
  PVector eep = armModel.getEEPos();
  float[] q = armModel.getQuaternion();
  float[] j = armModel.getJointAngles();
  
  Program prog = programs.get(active_prog);
  int reg = prog.getNextPosition();
  
  prog.addPosition(new Point(eep.x, eep.y, eep.z, q[0], q[1], q[2], q[3],
  j[0], j[1], j[2], j[3], j[4], j[5]), reg);
  
  MotionInstruction insert = new MotionInstruction(
  (curCoordFrame == CoordFrame.JOINT ? MTYPE_JOINT : MTYPE_LINEAR),
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

public void newFrameInstruction() {
  Program p = programs.get(active_prog);
  FrameInstruction f = new FrameInstruction(FTYPE_TOOL, 0);
  
  if(active_instr != p.getInstructions().size()) {
    p.overwriteInstruction(active_instr, f);
  } else {
    p.addInstruction(f);
  }
}

public void newIOInstruction() {
  Program p = programs.get(active_prog);
  IOInstruction io = new IOInstruction(0, OFF);
  
  if(active_instr != p.getInstructions().size()) {
    p.overwriteInstruction(active_instr, io);
  } else {
    p.addInstruction(io);
  }
}

public void newLabel() {
  Program p = programs.get(active_prog);
  
  int labelNum = p.getNextLabel();
  int labelIdx = active_instr;
  LabelInstruction l = new LabelInstruction(labelNum, labelIdx);
  
  if(active_instr != p.getInstructions().size()) {
    p.overwriteInstruction(active_instr, l);
  } else {
    p.addInstruction(l);
  }
}

public void newJumpInstruction() {
  Program p = programs.get(active_prog);
  JumpInstruction j = new JumpInstruction(-1);
  
  if(active_instr != p.getInstructions().size()) {
    p.overwriteInstruction(active_instr, j);
  } else {
    p.addInstruction(j);
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
      
      updateCoordFrame(armModel);
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
public ArrayList<ArrayList<String>> loadFrames(CoordFrame coordFrame) {
  ArrayList<ArrayList<String>> frameDisplay = new ArrayList<ArrayList<String>>();
  
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
    Frame frame = frames[idx];
    // For a Tool Frame, the origin is the offset of the End Effector from the Robot faceplate, so it is not technically a point.
    PVector frameOrigin = (frame instanceof ToolFrame) ? frame.getOrigin() : convertNativeToWorld(frame.getOrigin());
    frameDisplay.add( newLine(String.format("%d) %s", idx + 1, frameOrigin)) );
  }
  
  return frameDisplay;
}

/**
 * Transitions to the Frame Details menu, which displays
 * the x, y, z, w, p, r values associated with the Frame
 * at curFrameIdx in either the Tool Frames or User Frames,
 * based on the value of super_mode.
 */
public ArrayList<ArrayList<String>> loadFrameDetail(CoordFrame coordFrame) {
  ArrayList<ArrayList<String>> details = new ArrayList<ArrayList<String>>();
  
  // Display the frame set name as well as the index of the currently selected frame
  if(coordFrame == CoordFrame.TOOL) {
    String[] fields = toolFrames[curFrameIdx].toCondensedStringArray();
    // Place each value in the frame on a separate lien
    for(String field : fields) { details.add( newLine(field) ); }
    
  } else if(coordFrame == CoordFrame.USER) {
    String[] fields = userFrames[curFrameIdx].toCondensedStringArray();
    // Place each value in the frame on a separate lien
    for(String field : fields) { details.add( newLine(field) ); }
    
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
public ArrayList<ArrayList<String>> loadFrameDirectEntry(Frame f) {
  ArrayList<ArrayList<String>> frame = new ArrayList<ArrayList<String>>();
  
  String[][] entries = f.directEntryStringArray();
  
  for (int line = 0; line < entries.length; ++line) {
    frame.add( newLine(entries[line][0], entries[line][1]) );
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
      updateCoordFrame(armModel);
      
      saveFrameBytes( new File(sketchPath("tmp/frames.bin")) );
    } else {
      // Update the current frame of the Robot Arm
      activeUserFrame = curFrameIdx;
      userFrames[activeUserFrame] = frame;
      updateCoordFrame(armModel);
      
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
    origin = new PVector(inputs[0], inputs[1], inputs[2]);
  }
  
  origin.x = max(-9999f, min(origin.x, 9999f));
  origin.y = max(-9999f, min(origin.y, 9999f));
  origin.z = max(-9999f, min(origin.z, 9999f));
  
  wpr = new PVector(-inputs[3], -inputs[5], inputs[4]).mult(DEG_TO_RAD);
  
  // Save direct entry values
  taughtFrame.DEOrigin = origin;
  taughtFrame.DEAxesOffsets = eulerToQuat(wpr);
  taughtFrame.setFrame(2);
  
  if(DISPLAY_TEST_OUTPUT) {
    wpr = quatToEuler(taughtFrame.axes).mult(RAD_TO_DEG);
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
  
  updateCoordFrame(armModel);
  saveFrameBytes( new File(sketchPath("tmp/frames.bin")) );
}

/**
 * Displays the list of Registers in mode VIEW_REG or the Position Registers
 * for modes VIEW_REG_J or VIEW_REG_C. In mode VIEW_REG_J the joint angles
 * associated with the Point are displayed and the Cartesian values are
 * displayed in mode VIEW_REG_C.
 */
public ArrayList<ArrayList<String>> loadRegisters() { 
  ArrayList<ArrayList<String>> regs = new ArrayList<ArrayList<String>>();
  
  // View Registers or Position Registers
  int start = renderStartIdx;
  int end = min(start + ITEMS_TO_SHOW, DAT_REG.length);
  // Display a subset of the list of registers
  for(int idx = start; idx < end; ++idx) {
    String spaces;
    
    if(idx < 9) {
      spaces = "  ";
    } else if(idx < 99) {
      spaces = " ";
    } else {
      spaces = "";
    }
    // Display the line number
    String lineNum = String.format("%d)%s", (idx + 1), spaces);
    
    String lbl;
    
    if(mode == Screen.NAV_DREGS) {
      lbl = (DAT_REG[idx].comment == null) ? "" : DAT_REG[idx].comment;
    } else {
      lbl  = (GPOS_REG[idx].comment == null) ? "" : GPOS_REG[idx].comment;
    }
    
    int buffer = 16 - lbl.length();
    while(buffer-- > 0) { lbl += " "; }
    
    // Display the comment asscoiated with a specific Register entry
    String regLbl = String.format("%s[%d:%s%s]", (mode == Screen.NAV_DREGS) ? "R" : "PR", (idx + 1), spaces, lbl);
    // Display Register value (* ifuninitialized)
    String regEntry = "*";
    
    if(mode == Screen.NAV_DREGS) {
      if(DAT_REG[idx].value != null) {
        // Dispaly Register value
        regEntry = String.format("%4.3f", DAT_REG[idx].value);
      }
      
    } else if(GPOS_REG[idx].point != null) {
      // What to display for a point ...
      regEntry = "...";
    } else if(mode == Screen.NAV_PREGS_C && GPOS_REG[idx].point == null) {
      // Distinguish Joint from Cartesian mode for now
      regEntry = "#";
    }
    
    regs.add( newLine(lineNum, regLbl, regEntry) );
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
public ArrayList<ArrayList<String>> loadPosRegEntry(PositionRegister reg) {
  ArrayList<ArrayList<String>> register = new ArrayList<ArrayList<String>>();
  
  if(reg.point == null) {
    // Initialize values to zero if the entry is null
    if(mode == Screen.EDIT_PREG_C) {
      register.add( newLine("X: ",  "0.0") );
      register.add( newLine("Y: ",  "0.0") );
      register.add( newLine("Z: ",  "0.0") );
      register.add( newLine("W: ",  "0.0") );
      register.add( newLine("P: ",  "0.0") );
      register.add( newLine("R: ",  "0.0") );
      
    } else if(mode == Screen.EDIT_PREG_J) {
      for(int idx = 1; idx <= 6; ++idx) {
        register.add( newLine(String.format("J%d: ", idx), "0.0") );
      }
    }
  } else {
    
    // List current entry values if the Register is initialized
    String[][] entries = (mode == Screen.EDIT_PREG_C) ? reg.point.toCartesianStringArray() : reg.point.toJointStringArray();
    
    for(int idx = 0; idx < entries.length; ++idx) {
      register.add( newLine(entries[idx][0], entries[idx][1]) );
    }
  }
   
  return register;
}

public ArrayList<ArrayList<String>> loadToolIORegisters() {
  ArrayList<ArrayList<String>> ioRegs = new ArrayList<ArrayList<String>>();
  
  for(int i = 0; i < IO_REG.length; i += 1){
    if(IO_REG[i] == null) IO_REG[i] = new IORegister();
    
    String state = (IO_REG[i].state == ON) ? "ON" : "OFF";
    ioRegs.add(newLine((i+1) + ") IO[" + i + "] =", state));
  }
  
  return ioRegs;
}

public void createRegisterPoint(boolean jointAngles) {
  // Obtain point inputs from UI display text
  float[] inputs = new float[6];
  try {
    for(int idx = 0; idx < inputs.length; ++idx) {
      String inputStr = contents.get(idx).get(col_select);
      inputs[idx] = Float.parseFloat(inputStr);
    }
  } catch (NumberFormatException NFEx) {
    // Invalid input
    println("Values must be real numbers!");
    return;
  }
  
  if(jointAngles) {
    // Bring angles within range: (0, TWO_PI)
    for(int idx = 0; idx < inputs.length; ++idx) {
      inputs[idx] = clampAngle(inputs[idx] * DEG_TO_RAD);
    }
    
    GPOS_REG[active_index].point = new Point(inputs);
  } else {
    PVector position = new PVector();
    float[] orientation = new float[] { 1f, 0f, 0f, 0f };
  
    // Bring the input values with the range [-9999, 9999]
    for(int idx = 0; idx < inputs.length; ++idx) {
      inputs[idx] = max(-9999f, min(inputs[idx], 9999f));
    }
    
    position = new PVector(inputs[0], inputs[1], inputs[2]);
    /* Since all points are displayed with respect to the World Frame, it is
     * assumed that the user is entering a point with respect to the World Frame. */
    position = convertWorldToNative(position);
    
    orientation = eulerToQuat(new PVector(inputs[3] * DEG_TO_RAD, 
                                          inputs[4] * DEG_TO_RAD, 
                                          inputs[5] * DEG_TO_RAD));
    
    // Save joint values
    GPOS_REG[active_index].point = new Point(position, orientation);
  }
  
  GPOS_REG[active_index].isCartesian = !jointAngles;
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
public ArrayList<ArrayList<String>> loadTextInput() {
  ArrayList<ArrayList<String>> remark = new ArrayList<ArrayList<String>>();
  
  remark.add( newLine("\0") );
 
  ArrayList<String> line = new ArrayList<String>();
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
public ArrayList<String> newLine(String... columns) {
  ArrayList<String> line =  new ArrayList<String>();
  
  for(String col : columns) {
    line.add(col);
  }
  
  return line;
}

/**
 * This method updates the given list, row, and render start indices for a list of elements, whose contents potentially
 * cannot all be show on the screen, when moving backwards in the list. It is assumed that the lower bound of list index
 * is 0 and that the given list index is valid. Also, the row index should be within the bounds of [0, ITEMS_TO_DISPLAY].
 * Render start index corresponds to the index, in the list that is being displayed, that will appear at the top of the
 * list display; so, the subset of the displayed list ranging from render start index to render start index + ITEMS_TO_DISPLAY
 * is shown on the screen. The inPlace parameter determines how the list display will be shifted. If it is false, then the
 * display will be shift backward one element (if possible). However, if inPlace is set to true, then the list display will
 * shift backward at most ITEMS_TO_DISPLAY - 1 elements (depending on the value of render start index), while holding the
 * current row index constant. The updated list, row, and render start indices are returned in a three element integer in
 * that order.
 * 
 * @param listIdx         The index, of the currently highlighted row in the display, in the list
 * @param row             The index, of the currently highlighted row in the display, relative to the first element
 *                        displayed
 * @param renderstartIdx  The index of the first element displayed on the Screen
 * @param inPlace         Whether to move backward the list an entire Screen lenth of elements, while keeping the row
 *                        constant, or move backward a single element
 * @returning             The updated values of listIdx, row, renderStartIdx in a 3-element integer array in that order
 */
public int[] moveUp(int listIdx, int row, int renderStartIdx, boolean inPlace) {
  
  if (inPlace && renderStartIdx > 0) {
    // Move display frame up an entire screen's display length
    int t = renderStartIdx;
    
    renderStartIdx = max(0, t - ITEMS_TO_SHOW - 1);
    listIdx = listIdx + min(0, renderStartIdx - t);
  } else {
    // Move up a single element
    int i = listIdx,
    r = row;
    
    listIdx = max(0, i - 1);
    row = max(0, r + min(listIdx - i, 0));
    renderStartIdx = renderStartIdx + min((listIdx - i) - (row - r), 0);
  }
  
  return new int[] { listIdx, row, renderStartIdx };
}

/**
 * This method updates the list, row, render start indices for a list of elements, which may have too many elements to
 * fit on the screen, when moving forward in the list. It is assumed that the given list index is within the bounds of
 * 0 and listSize - 1 and that row is within the bounds of 0 and ITEMS_TO_DISPLAY - 1, and render start index = list
 * index - row index. Similar to moveUp(), if inPlace is set to true, that the list and render start indices will be
 * updated to move forward in the list at most ITEMS_TO_DISPLAY - 1 (depending on the current value of render start index),
 * while keeping the row index constant. Otherwise, the indices are updated for moving a single element forward in the
 * list and down the screen display. The updated indices are returned in an 3-element integer arrat in the order of list
 * index, row index, and finally render start index.
 * 
 * @param listIdx         The index, of the currently highlighted row in the display, in the list
 * @param listsize        The number of elements in the list
 * @param row             The index, of the currently highlighted row in the display, relative to the first element
 *                        displayed
 * @param renderstartIdx  The index of the first element displayed on the Screen
 * @param inPlace         Whether to move forward the list an entire Screen lenth of elements, while keeping the row
 *                        constant, or move forward a single element
 * @returning             The updated values of listIdx, row, renderStartIdx in a 3-element integer array in that order
 */
public int[] moveDown(int listIdx, int listSize, int row, int renderStartIdx, boolean inPlace) {
  
  if (inPlace && listSize > (renderStartIdx + ITEMS_TO_SHOW)) {
    // Move display frame down an entire screen's display length
    int t = renderStartIdx;
    
    renderStartIdx = min(renderStartIdx + (ITEMS_TO_SHOW - 1), listSize - ITEMS_TO_SHOW);
    listIdx = listIdx + max(0, renderStartIdx - t);
  } else {
    // Move down a single element
    int i = listIdx,
    r = row;
    
    listIdx = min(i + 1, listSize - 1);
    row = min(r + max(0, (listIdx - i)), (ITEMS_TO_SHOW - 1));
    renderStartIdx = renderStartIdx + max(0, (listIdx - i) - (row - r));
  }
  
  return new int[] { listIdx, row, renderStartIdx };
}
final int FRAME_JOINT = 0, 
          FRAME_JGFRM = 1, 
          FRAME_WORLD = 2, 
          FRAME_TOOL = 3, 
          FRAME_USER = 4;
final int SMALL_BUTTON = 20,
          LARGE_BUTTON = 35; 
final int NONE = 0, 
          PROGRAM_NAV = 1, 
          INSTRUCTION_NAV = 2,
          INSTRUCTION_EDIT = 3,
          SET_INSTRUCTION_SPEED = 4,
          SET_INSTRUCTION_REGISTER = 5,
          SET_INSTRUCTION_TERMINATION = 6,
          JUMP_TO_LINE = 7,
          VIEW_REGISTER = 8,
          ENTER_TEXT = 9,
          PICK_LETTER = 10,
          MENU_NAV = 11,
          SETUP_NAV = 12,
          NAV_TOOL_FRAMES = 13,
          NAV_USER_FRAMES = 14,
          PICK_FRAME_MODE = 15,
          FRAME_DETAIL = 16,
          PICK_FRAME_METHOD = 17,
          THREE_POINT_MODE = 18,
          FOUR_POINT_MODE = 19,
          SIX_POINT_MODE = 20,
          DIRECT_ENTRY_MODE = 21,
          ACTIVE_FRAMES = 22,
          PICK_INSTRUCTION = 23,
          IO_SUBMENU = 24,
          SET_DO_BRACKET = 25,
          SET_DO_STATUS = 26,
          SET_RO_BRACKET = 27,
          SET_RO_STATUS = 28,
          SET_FRAME_INSTRUCTION = 29,
          SET_FRAME_INSTRUCTION_IDX = 30,
          PICK_REG_LIST = 31,
          VIEW_REG = 32,
          // C for Cartesian
          VIEW_POS_REG_C = 33,
          // J for Joint
          VIEW_POS_REG_J = 34,
          EDIT_MENU = 35,
          INPUT_FLOAT = 36,
          INPUT_POINT_C = 37,
          INPUT_POINT_J = 38,
          INPUT_COMMENT_U = 39,
          INPUT_COMMENT_L = 40,
          CONFIRM_DELETE = 41;
final int COLOR_DEFAULT = -8421377,
          COLOR_ACTIVE = -65536;
// Determines what End Effector mapping should be display
static int EE_MAPPING = 2;

int frame = FRAME_JOINT; // current frame
//String displayFrame = "JOINT";
int active_program = -1; // the currently selected program
int active_instruction = -1; // the currently selected instruction
int mode = NONE;
// Used by some modes to refer to a previous mode
int super_mode = NONE;
int NUM_MODE; // When NUM_MODE is ON, allows for entering numbers
int shift = OFF; // Is shift button pressed or not?
int step = OFF; // Is step button pressed or not?
int record = OFF;
 
int g1_px, g1_py; // the left-top corner of group1
int g1_width, g1_height; // group 1's width and height
int display_px, display_py; // the left-top corner of display screen
int display_width = 340, display_height = 270; // height and width of display screen

PFont fnt;
Group g1, g2, txt;
Button bt_show, bt_hide, 
       bt_zoomin_shrink, bt_zoomin_normal,
       bt_zoomout_shrink, bt_zoomout_normal,
       bt_pan_shrink, bt_pan_normal,
       bt_rotate_shrink, bt_rotate_normal,
       bt_record_shrink, bt_record_normal, 
       bt_ee_normal
       ;
Textlabel fn_info, num_info;

String workingText; // when entering text or a number
String workingTextSuffix;
boolean speedInPercentage;
final int ITEMS_TO_SHOW = 8; // how many programs/ instructions to display on screen
int curFrameIdx = -1;
// Used to keep track a specific point in space
PVector ref_point;
ArrayList<float[][]> teachPointTMatrices = null;
int activeUserFrame = -1;
int activeJogFrame = -1;
int activeToolFrame = -1;

// display list of programs or motion instructions
ArrayList<ArrayList<String>> contents = new ArrayList<ArrayList<String>>();
// display options for an element in a motion instruction
ArrayList<String> options = new ArrayList<String>();
// store numbers pressed by the user
ArrayList<Integer> nums = new ArrayList<Integer>(); 
// which element is on focus now?
int active_row = 0,
    active_col = 0,
    // Keep track of focused element in a displayed list
    active_index = 0,
    // Used for drawing a subsection of a list on the screen
    text_render_start = 0;
// which option is on focus now?
int which_option = -1; 
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
 * F5 -> Y, Z, _, @, *, ./y, z, _, @, *, .
 */
private final int[] letterStates = new int[] { 0, 0, 0, 0, 0 };

public static final boolean DISPLAY_TEST_OUTPUT = true;

void gui(){
   g1_px = 0;
   g1_py = 0;
   g1_width = 100;
   g1_height = 100;
   display_px = g1_width / 2;
   display_py = SMALL_BUTTON + 1;
   /*
   PFont pfont = createFont("ArialNarrow",9,true); // new font
   ControlFont font = new ControlFont(pfont, 9);
   cp5.setFont(font);
   */
   
   // group 1: display and function buttons
   g1 = cp5.addGroup("DISPLAY")
      .setPosition(g1_px, g1_py)
      .setBackgroundColor(color(127,127,127,100));
   
   fnt = createFont("data/Consolas.ttf", 14);
   textDisplay = cp5.addTextarea("txt")
      .setPosition(display_px,display_py)
      .setSize(display_width, display_height)
      .setLineHeight(14)
      .setColor(color(128))
      .setColorBackground(color(240))
      .setColorForeground(color(15))
      .moveTo(g1);
   
   // expand group 1's width and height
   g1_width += 340;
   g1_height += 270;
   
   // text label to show how to use F1 - F5 keys
   fn_info = cp5.addTextlabel("fn_info")
       .hide();
       
   num_info = cp5.addTextlabel("num_info")
       .hide();   
   // button to show g1
   int bt_show_px = 1;
   int bt_show_py = 1;
   bt_show = cp5.addButton("show")
       .setPosition(bt_show_px, bt_show_py)
       .setSize(LARGE_BUTTON, SMALL_BUTTON)
       .setCaptionLabel("SHOW")
       .setColorBackground(color(127,127,255))
       .setColorCaptionLabel(color(255,255,255))  
       .hide()
       ;
       
    int zoomin_shrink_px =  bt_show_px + LARGE_BUTTON;
    int zoomin_shrink_py = bt_show_py;
    PImage[] zoomin_shrink = {loadImage("images/zoomin_35x20.png"), 
                              loadImage("images/zoomin_over.png"), 
                              loadImage("images/zoomin_down.png")};   
    bt_zoomin_shrink = cp5.addButton("zoomin_shrink")
       .setPosition(zoomin_shrink_px, zoomin_shrink_py)
       .setSize(LARGE_BUTTON, SMALL_BUTTON)
       .setImages(zoomin_shrink)
       .updateSize()
       .hide()
       ;   
       
    int zoomout_shrink_px = zoomin_shrink_px + LARGE_BUTTON ;
    int zoomout_shrink_py = zoomin_shrink_py;   
    PImage[] zoomout_shrink = {loadImage("images/zoomout_35x20.png"), 
                               loadImage("images/zoomout_over.png"), 
                               loadImage("images/zoomout_down.png")};   
    bt_zoomout_shrink = cp5.addButton("zoomout_shrink")
       .setPosition(zoomout_shrink_px, zoomout_shrink_py)
       .setSize(LARGE_BUTTON, SMALL_BUTTON)
       .setImages(zoomout_shrink)
       .updateSize()
       .hide()
       ;    
   
    int pan_shrink_px = zoomout_shrink_px + LARGE_BUTTON;
    int pan_shrink_py = zoomout_shrink_py ;
    PImage[] pan_shrink = {loadImage("images/pan_35x20.png"), 
                           loadImage("images/pan_over.png"), 
                           loadImage("images/pan_down.png")};   
    bt_pan_shrink = cp5.addButton("pan_shrink")
       .setPosition(pan_shrink_px, pan_shrink_py)
       .setSize(LARGE_BUTTON, SMALL_BUTTON)
       .setImages(pan_shrink)
       .updateSize()
       .hide()
       ;    
       
    int rotate_shrink_px = pan_shrink_px + LARGE_BUTTON;
    int rotate_shrink_py = pan_shrink_py;   
    PImage[] rotate_shrink = {loadImage("images/rotate_35x20.png"), 
                              loadImage("images/rotate_over.png"), 
                              loadImage("images/rotate_down.png")};   
    bt_rotate_shrink = cp5.addButton("rotate_shrink")
       .setPosition(rotate_shrink_px, rotate_shrink_py)
       .setSize(LARGE_BUTTON, SMALL_BUTTON)
       .setImages(rotate_shrink)
       .updateSize()
       .hide()
       ;     
      
   // button to hide g1
   int hide_px = display_px;
   int hide_py = display_py - SMALL_BUTTON - 1;
   bt_hide = cp5.addButton("hide")
       .setPosition(hide_px, hide_py)
       .setSize(LARGE_BUTTON, SMALL_BUTTON)
       .setCaptionLabel("HIDE")
       .setColorBackground(color(127,127,255))
       .setColorCaptionLabel(color(255,255,255))  
       .moveTo(g1);
     
    int zoomin_normal_px =  hide_px + LARGE_BUTTON + 1;
    int zoomin_normal_py = hide_py;
    PImage[] zoomin_normal = {loadImage("images/zoomin_35x20.png"), 
                              loadImage("images/zoomin_over.png"), 
                              loadImage("images/zoomin_down.png")};   
    bt_zoomin_normal = cp5.addButton("zoomin_normal")
       .setPosition(zoomin_normal_px, zoomin_normal_py)
       .setSize(LARGE_BUTTON, SMALL_BUTTON)
       .setImages(zoomin_normal)
       .updateSize()
       .moveTo(g1) ;   
       
    int zoomout_normal_px = zoomin_normal_px + LARGE_BUTTON + 1;
    int zoomout_normal_py = zoomin_normal_py;   
    PImage[] zoomout_normal = {loadImage("images/zoomout_35x20.png"), 
                               loadImage("images/zoomout_over.png"), 
                               loadImage("images/zoomout_down.png")};   
    bt_zoomout_normal = cp5.addButton("zoomout_normal")
       .setPosition(zoomout_normal_px, zoomout_normal_py)
       .setSize(LARGE_BUTTON, SMALL_BUTTON)
       .setImages(zoomout_normal)
       .updateSize()
       .moveTo(g1) ;    
   
    int pan_normal_px = zoomout_normal_px + LARGE_BUTTON + 1;
    int pan_normal_py = zoomout_normal_py ;
    PImage[] pan = {loadImage("images/pan_35x20.png"), 
                    loadImage("images/pan_over.png"), 
                    loadImage("images/pan_down.png")};   
    bt_pan_normal = cp5.addButton("pan_normal")
       .setPosition(pan_normal_px, pan_normal_py)
       .setSize(LARGE_BUTTON, SMALL_BUTTON)
       .setImages(pan)
       .updateSize()
       .moveTo(g1) ;    
       
    int rotate_normal_px = pan_normal_px + LARGE_BUTTON + 1;
    int rotate_normal_py = pan_normal_py;   
    PImage[] rotate = {loadImage("images/rotate_35x20.png"), 
                       loadImage("images/rotate_over.png"), 
                       loadImage("images/rotate_down.png")};   
    bt_rotate_normal = cp5.addButton("rotate_normal")
       .setPosition(rotate_normal_px, rotate_normal_py)
       .setSize(LARGE_BUTTON, SMALL_BUTTON)
       .setImages(rotate)
       .updateSize()
       .moveTo(g1) ;     
       
    int record_normal_px = rotate_normal_px + LARGE_BUTTON + 1;
    int record_normal_py = rotate_normal_py;   
    PImage[] record = {loadImage("images/record-35x20.png"), 
                       loadImage("images/record-over.png"), 
                       loadImage("images/record-on.png")};   
    bt_record_normal = cp5.addButton("record_normal")
       .setPosition(record_normal_px, record_normal_py)
       .setSize(LARGE_BUTTON, SMALL_BUTTON)
       .setImages(record)
       .updateSize()
       .moveTo(g1) ;     
      
    int EE_normal_px = record_normal_px + LARGE_BUTTON + 1;
    int EE_normal_py = record_normal_py;   
    PImage[] EE = {loadImage("images/EE_35x20.png"), 
                   loadImage("images/EE_over.png"), 
                   loadImage("images/EE_down.png")};   
    bt_ee_normal = cp5.addButton("EE")
       .setPosition(EE_normal_px, EE_normal_py)
       .setSize(LARGE_BUTTON, SMALL_BUTTON)
       .setImages(EE)
       .updateSize()
       .moveTo(g1) ;    
       
   PImage[] imgs_arrow_up = {loadImage("images/arrow-up.png"), 
                             loadImage("images/arrow-up_over.png"), 
                             loadImage("images/arrow-up_down.png")};   
   int up_px = display_px+display_width + 2;
   int up_py = display_py;
   cp5.addButton("up")
       .setPosition(up_px, up_py)
       .setSize(LARGE_BUTTON, LARGE_BUTTON)
       .setImages(imgs_arrow_up)
       .updateSize()
       .moveTo(g1) ;     
   
    PImage[] imgs_arrow_down = {loadImage("images/arrow-down.png"), 
                                loadImage("images/arrow-down_over.png"), 
                                loadImage("images/arrow-down_down.png")};   
    int dn_px = up_px;
    int dn_py = up_py + LARGE_BUTTON + 2;
    cp5.addButton("dn")
       .setPosition(dn_px, dn_py)
       .setSize(LARGE_BUTTON, LARGE_BUTTON)
       .setImages(imgs_arrow_down)
       .updateSize()
       .moveTo(g1) ;    
   
    PImage[] imgs_arrow_l = {loadImage("images/arrow-l.png"), 
                             loadImage("images/arrow-l_over.png"), 
                             loadImage("images/arrow-l_down.png")};
    int lt_px = dn_px;
    int lt_py = dn_py + LARGE_BUTTON + 2;
    cp5.addButton("lt")
       .setPosition(lt_px, lt_py)
       .setSize(LARGE_BUTTON, LARGE_BUTTON)
       .setImages(imgs_arrow_l)
       .updateSize()
       .moveTo(g1) ;  
    
    PImage[] imgs_arrow_r = {loadImage("images/arrow-r.png"), 
                             loadImage("images/arrow-r_over.png"), 
                             loadImage("images/arrow-r_down.png")};
    int rt_px = lt_px;
    int rt_py = lt_py + LARGE_BUTTON + 2;;
    cp5.addButton("rt")
       .setPosition(rt_px, rt_py)
       .setSize(LARGE_BUTTON, LARGE_BUTTON)
       .setImages(imgs_arrow_r)
       .updateSize()
       .moveTo(g1) ; 
    
    int fn_px = rt_px;
    int fn_py = rt_py + LARGE_BUTTON + 2;   
    cp5.addButton("Fn")
       .setPosition(fn_px, fn_py)
       .setSize(LARGE_BUTTON, LARGE_BUTTON)
       .setCaptionLabel("FCTN")
       .setColorBackground(color(127,127,255))
       .setColorCaptionLabel(color(255,255,255))  
       .moveTo(g1);    
       
    int sf_px = fn_px;
    int sf_py = fn_py + LARGE_BUTTON + 2;   
    cp5.addButton("sf")
       .setPosition(sf_px, sf_py)
       .setSize(LARGE_BUTTON, LARGE_BUTTON)
       .setCaptionLabel("SHIFT")
       .setColorBackground(color(127,127,255))
       .setColorActive(color(0))
       .setColorCaptionLabel(color(255,255,255))  
       .moveTo(g1);
       
    int ne_px = sf_px ;
    int ne_py = sf_py + LARGE_BUTTON + 2;   
    cp5.addButton("ne")
       .setPosition(ne_px, ne_py)
       .setSize(LARGE_BUTTON, LARGE_BUTTON)
       .setCaptionLabel("NEXT")
       .setColorBackground(color(127,127,255))
       .setColorCaptionLabel(color(255,255,255))  
       .moveTo(g1);    
       
    int se_px = display_px - 2 - LARGE_BUTTON;
    int se_py = display_py;   
    cp5.addButton("se")
       .setPosition(se_px, se_py)
       .setSize(LARGE_BUTTON, LARGE_BUTTON)
       .setCaptionLabel("SELECT")
       .setColorBackground(color(127,127,255))
       .setColorCaptionLabel(color(255,255,255))  
       .moveTo(g1);           
    
    int mu_px = se_px ;
    int mu_py = se_py + LARGE_BUTTON + 2;   
    cp5.addButton("mu")
       .setPosition(mu_px, mu_py)
       .setSize(LARGE_BUTTON, LARGE_BUTTON)
       .setCaptionLabel("MENU")
       .setColorBackground(color(127,127,255))
       .setColorCaptionLabel(color(255,255,255))  
       .moveTo(g1);      
    
    int ed_px = mu_px ;
    int ed_py = mu_py + LARGE_BUTTON + 2;   
    cp5.addButton("ed")
       .setPosition(ed_px, ed_py)
       .setSize(LARGE_BUTTON, LARGE_BUTTON)
       .setCaptionLabel("EDIT")
       .setColorBackground(color(127,127,255))
       .setColorCaptionLabel(color(255,255,255))  
       .moveTo(g1);      
     
    int da_px = ed_px ;
    int da_py = ed_py + LARGE_BUTTON + 2;   
    cp5.addButton("da")
       .setPosition(da_px, da_py)
       .setSize(LARGE_BUTTON, LARGE_BUTTON)
       .setCaptionLabel("DATA")
       .setColorBackground(color(127,127,255))
       .setColorCaptionLabel(color(255,255,255))  
       .moveTo(g1);  
    
    int sw_px = da_px ;
    int sw_py = da_py + LARGE_BUTTON + 2;   
    cp5.addButton("sw")
       .setPosition(sw_px, sw_py)
       .setSize(LARGE_BUTTON, LARGE_BUTTON)
       .setCaptionLabel("SWITH")
       .setColorBackground(color(127,127,255))
       .setColorCaptionLabel(color(255,255,255))  
       .moveTo(g1);     
    
    int st_px = sw_px ;
    int st_py = sw_py + LARGE_BUTTON + 2;   
    cp5.addButton("st")
       .setPosition(st_px, st_py)
       .setSize(LARGE_BUTTON, LARGE_BUTTON)
       .setCaptionLabel("STEP")
       .setColorBackground(color(127,127,255))
       .setColorCaptionLabel(color(255,255,255))  
       .moveTo(g1);        
    
    int pr_px = st_px ;
    int pr_py = st_py + LARGE_BUTTON + 2;   
    cp5.addButton("pr")
       .setPosition(pr_px, pr_py)
       .setSize(LARGE_BUTTON, LARGE_BUTTON)
       .setCaptionLabel("PREV")
       .setColorBackground(color(127,127,255))
       .setColorCaptionLabel(color(255,255,255))  
       .moveTo(g1);     
      
    int f1_px = display_px ;
    int f1_py = display_py + display_height + 2;   
    cp5.addButton("f1")
       .setPosition(f1_px, f1_py)
       .setSize(LARGE_BUTTON, LARGE_BUTTON)
       .setCaptionLabel("F1")
       .setColorBackground(color(127,127,255))
       .setColorCaptionLabel(color(255,255,255))  
       .moveTo(g1);     
         
    int f2_px = f1_px + 41 ;
    int f2_py = f1_py;   
    cp5.addButton("f2")
       .setPosition(f2_px, f2_py)
       .setSize(LARGE_BUTTON, LARGE_BUTTON)
       .setCaptionLabel("F2")
       .setColorBackground(color(127,127,255))
       .setColorCaptionLabel(color(255,255,255))  
       .moveTo(g1);  
       
    int f3_px = f2_px + 41 ;
    int f3_py = f2_py;   
    cp5.addButton("f3")
       .setPosition(f3_px, f3_py)
       .setSize(LARGE_BUTTON, LARGE_BUTTON)
       .setCaptionLabel("F3")
       .setColorBackground(color(127,127,255))
       .setColorCaptionLabel(color(255,255,255))  
       .moveTo(g1);    
       
    int f4_px = f3_px + 41 ;
    int f4_py = f3_py;   
    cp5.addButton("f4")
       .setPosition(f4_px, f4_py)
       .setSize(LARGE_BUTTON, LARGE_BUTTON)
       .setCaptionLabel("F4")
       .setColorBackground(color(127,127,255))
       .setColorCaptionLabel(color(255,255,255))  
       .moveTo(g1);   
      
    int f5_px = f4_px + 41;
    int f5_py = f4_py;   
    cp5.addButton("f5")
       .setPosition(f5_px, f5_py)
       .setSize(LARGE_BUTTON, LARGE_BUTTON)
       .setCaptionLabel("F5")
       .setColorBackground(color(127,127,255))
       .setColorCaptionLabel(color(255,255,255))  
       .moveTo(g1);   
    
    int hd_px = f5_px + 41;
    int hd_py = f5_py;   
    cp5.addButton("hd")
       .setPosition(hd_px, hd_py)
       .setSize(LARGE_BUTTON, LARGE_BUTTON)
       .setCaptionLabel("HOLD")
       .setColorBackground(color(127,127,255))
       .setColorCaptionLabel(color(255,255,255))  
       .moveTo(g1);    
       
    int fd_px = hd_px + 41;
    int fd_py = hd_py;   
    cp5.addButton("fd")
       .setPosition(fd_px, fd_py)
       .setSize(LARGE_BUTTON, LARGE_BUTTON)
       .setCaptionLabel("FWD")
       .setColorBackground(color(127,127,255))
       .setColorCaptionLabel(color(255,255,255))  
       .moveTo(g1);   
      
    int bd_px = fd_px + 41;
    int bd_py = fd_py;   
    cp5.addButton("bd")
       .setPosition(bd_px, bd_py)
       .setSize(LARGE_BUTTON, LARGE_BUTTON)
       .setCaptionLabel("BWD")
       .setColorBackground(color(127,127,255))
       .setColorCaptionLabel(color(255,255,255))  
       .moveTo(g1);    
       
   // adjust group 1's width to include all controllers  
   g1.setWidth(g1_width)
     .setBackgroundHeight(g1_height); 
  
    
   // group 2: tool bar
   Group g2 = cp5.addGroup("TOOLBAR")
                 .setPosition(0,display_py + display_height + LARGE_BUTTON + 15)
                 .setBackgroundColor(color(127,127,127, 50))
                 //.setWidth(g1_width)
                 //.setBackgroundHeight(740)
                 .moveTo(g1)   
                 ;
   g2.setOpen(true);              
   
   int RESET_px = 0;
   int RESET_py = 0;
   cp5.addButton("RESET")
      .setPosition(RESET_px, RESET_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel("RESET")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2);   
 
   int LEFT_px = RESET_px + LARGE_BUTTON + 1;
   int LEFT_py = RESET_py;
   PImage[] imgs_LEFT = {loadImage("images/LEFT.png"), 
                         loadImage("images/LEFT.png"), 
                         loadImage("images/LEFT.png")};  
   cp5.addButton("LEFT")
      .setPosition(LEFT_px, LEFT_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setImages(imgs_LEFT)
      .setColorBackground(color(127,127,255)) 
      .moveTo(g2);   
      
   int ITEM_px = LEFT_px + LARGE_BUTTON + 1 ;
   int ITEM_py = LEFT_py;
   cp5.addButton("ITEM")
      .setPosition(ITEM_px, ITEM_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel("ITEM")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2);    
    
   int ENTER_px = ITEM_px + LARGE_BUTTON + 1 ;
   int ENTER_py = ITEM_py;
   cp5.addButton("ENTER")
      .setPosition(ENTER_px, ENTER_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel("ENTER")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2);    
      
   int TOOL1_px = ENTER_px + LARGE_BUTTON + 1 ;
   int TOOL1_py = ENTER_py;
   cp5.addButton("TOOL1")
      .setPosition(TOOL1_px, TOOL1_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel("TOOL1")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2);   
      
   int TOOL2_px = TOOL1_px + LARGE_BUTTON + 1 ;
   int TOOL2_py = TOOL1_py;
   cp5.addButton("TOOL2")
      .setPosition(TOOL2_px, TOOL2_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel("TOOL2")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2);
 
   int MOVEMENU_px = TOOL2_px + LARGE_BUTTON + 1 ;
   int MOVEMENU_py = TOOL2_py;
   cp5.addButton("MOVEMENU")
      .setPosition(MOVEMENU_px, MOVEMENU_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel("MVMU")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2); 
      
   int SETUP_px = MOVEMENU_px + LARGE_BUTTON + 1 ;
   int SETUP_py = MOVEMENU_py;
   cp5.addButton("SETUP")
      .setPosition(SETUP_px, SETUP_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel("SETUP")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2);    
      
   int STATUS_px = SETUP_px + LARGE_BUTTON + 1 ;
   int STATUS_py = SETUP_py;
   cp5.addButton("STATUS")
      .setPosition(STATUS_px, STATUS_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel("STATUS")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2);    
      
   int NO_px = STATUS_px + LARGE_BUTTON + 1 ;
   int NO_py = STATUS_py;
   cp5.addButton("NO")
      .setPosition(NO_px, NO_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel("NO.")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2);   
     
   int POSN_px = NO_px + LARGE_BUTTON + 1 ;
   int POSN_py = NO_py;
   cp5.addButton("POSN")
      .setPosition(POSN_px, POSN_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel("POSN")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2);   
    
   int SPEEDUP_px = POSN_px + LARGE_BUTTON + 1 ;
   int SPEEDUP_py = POSN_py;
   cp5.addButton("SPEEDUP")
      .setPosition(SPEEDUP_px, SPEEDUP_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel("+%")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2);   
    
   int SLOWDOWN_px = SPEEDUP_px + LARGE_BUTTON + 1 ;
   int SLOWDOWN_py = SPEEDUP_py;
   cp5.addButton("SLOWDOWN")
      .setPosition(SLOWDOWN_px, SLOWDOWN_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel("-%")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2);    
   
   int NUM1_px = RESET_px ;
   int NUM1_py = RESET_py + SMALL_BUTTON + 1;
   cp5.addButton("NUM1")
      .setPosition(NUM1_px, NUM1_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel("1")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2);  
      
   int NUM2_px = NUM1_px + LARGE_BUTTON + 1;
   int NUM2_py = NUM1_py;
   cp5.addButton("NUM2")
      .setPosition(NUM2_px, NUM2_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel("2")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2);  
  
   int NUM3_px = NUM2_px + LARGE_BUTTON + 1;
   int NUM3_py = NUM2_py;
   cp5.addButton("NUM3")
      .setPosition(NUM3_px, NUM3_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel("3")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2); 
 
   int NUM4_px = NUM3_px + LARGE_BUTTON + 1;
   int NUM4_py = NUM3_py;
   cp5.addButton("NUM4")
      .setPosition(NUM4_px, NUM4_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel("4")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2); 

   int NUM5_px = NUM4_px + LARGE_BUTTON + 1;
   int NUM5_py = NUM4_py;
   cp5.addButton("NUM5")
      .setPosition(NUM5_px, NUM5_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel("5")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2);   
 
   int NUM6_px = NUM5_px + LARGE_BUTTON + 1;
   int NUM6_py = NUM5_py;
   cp5.addButton("NUM6")
      .setPosition(NUM6_px, NUM6_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel("6")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2);    
    
   int NUM7_px = NUM6_px + LARGE_BUTTON + 1;
   int NUM7_py = NUM6_py;
   cp5.addButton("NUM7")
      .setPosition(NUM7_px, NUM7_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel("7")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2);     
      
   int NUM8_px = NUM7_px + LARGE_BUTTON + 1;
   int NUM8_py = NUM7_py;
   cp5.addButton("NUM8")
      .setPosition(NUM8_px, NUM8_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel("8")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2);    
      
   int NUM9_px = NUM8_px + LARGE_BUTTON + 1;
   int NUM9_py = NUM8_py;
   cp5.addButton("NUM9")
      .setPosition(NUM9_px, NUM9_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel("9")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2);  
  
   int NUM0_px = NUM9_px + LARGE_BUTTON + 1;
   int NUM0_py = NUM9_py;
   cp5.addButton("NUM0")
      .setPosition(NUM0_px, NUM0_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel("0")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2);   
  
   int LINE_px = NUM0_px + LARGE_BUTTON + 1;
   int LINE_py = NUM0_py;
   cp5.addButton("LINE")
      .setPosition(LINE_px, LINE_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel("-")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2);    
   
   int PERIOD_px = LINE_px + LARGE_BUTTON + 1;
   int PERIOD_py = LINE_py;
   cp5.addButton("PERIOD")
      .setPosition(PERIOD_px, PERIOD_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel(".")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2);   
   
   int COMMA_px = PERIOD_px + LARGE_BUTTON + 1;
   int COMMA_py = PERIOD_py;
   cp5.addButton("COMMA")
      .setPosition(COMMA_px, COMMA_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel(",")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2);     
   
   int JOINT1_NEG_px = NUM1_px;
   int JOINT1_NEG_py = NUM1_py + SMALL_BUTTON + 1;
   cp5.addButton("JOINT1_NEG")
      .setPosition(JOINT1_NEG_px, JOINT1_NEG_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel("-X (J1)")
      .setColorBackground(color(127, 127, 255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2);
   
   int JOINT1_POS_px = JOINT1_NEG_px + LARGE_BUTTON + 1;
   int JOINT1_POS_py = JOINT1_NEG_py;
   cp5.addButton("JOINT1_POS")
      .setPosition(JOINT1_POS_px, JOINT1_POS_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel("+X (J1)")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2);      
      
   int JOINT2_NEG_px = JOINT1_POS_px + LARGE_BUTTON + 1;
   int JOINT2_NEG_py = JOINT1_POS_py;
   cp5.addButton("JOINT2_NEG")
      .setPosition(JOINT2_NEG_px, JOINT2_NEG_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel("-Y (J2)")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2);      
     
   int JOINT2_POS_px = JOINT2_NEG_px + LARGE_BUTTON + 1;
   int JOINT2_POS_py = JOINT2_NEG_py;
   cp5.addButton("JOINT2_POS")
      .setPosition(JOINT2_POS_px, JOINT2_POS_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel("+Y (J2)")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2);     
    
   int JOINT3_NEG_px = JOINT2_POS_px + LARGE_BUTTON + 1;
   int JOINT3_NEG_py = JOINT2_POS_py;
   cp5.addButton("JOINT3_NEG")
      .setPosition(JOINT3_NEG_px, JOINT3_NEG_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel("-Z (J3)")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2);  
   
   int JOINT3_POS_px = JOINT3_NEG_px + LARGE_BUTTON + 1;
   int JOINT3_POS_py = JOINT3_NEG_py;
   cp5.addButton("JOINT3_POS")
      .setPosition(JOINT3_POS_px, JOINT3_POS_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel("+Z (J3)")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2);    
      
   int JOINT4_NEG_px = JOINT3_POS_px + LARGE_BUTTON + 1;
   int JOINT4_NEG_py = JOINT3_POS_py;
   cp5.addButton("JOINT4_NEG")
      .setPosition(JOINT4_NEG_px, JOINT4_NEG_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel("-X (J4)")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2);    
     
   int JOINT4_POS_px = JOINT4_NEG_px + LARGE_BUTTON + 1;
   int JOINT4_POS_py = JOINT4_NEG_py;
   cp5.addButton("JOINT4_POS")
      .setPosition(JOINT4_POS_px, JOINT4_POS_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel("+X (J4)")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2);      
      
   int JOINT5_NEG_px = JOINT4_POS_px + LARGE_BUTTON + 1;
   int JOINT5_NEG_py = JOINT4_POS_py;
   cp5.addButton("JOINT5_NEG")
      .setPosition(JOINT5_NEG_px, JOINT5_NEG_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel("-Y (J5)")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2);      
     
   int JOINT5_POS_px = JOINT5_NEG_px + LARGE_BUTTON + 1;
   int JOINT5_POS_py = JOINT5_NEG_py;
   cp5.addButton("JOINT5_POS")
      .setPosition(JOINT5_POS_px, JOINT5_POS_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel("+Y (J5)")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2);     
    
   int JOINT6_NEG_px = JOINT5_POS_px + LARGE_BUTTON + 1;
   int JOINT6_NEG_py = JOINT5_POS_py;
   cp5.addButton("JOINT6_NEG")
      .setPosition(JOINT6_NEG_px, JOINT6_NEG_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel("-Z (J6)")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2);  
   
   int JOINT6_POS_px = JOINT6_NEG_px + LARGE_BUTTON + 1;
   int JOINT6_POS_py = JOINT6_NEG_py;
   cp5.addButton("JOINT6_POS")
      .setPosition(JOINT6_POS_px, JOINT6_POS_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel("+Z (J6)")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2);    
   
   int COORD_px = JOINT6_POS_px + LARGE_BUTTON + 1;
   int COORD_py = JOINT6_POS_py;
   cp5.addButton("COORD")
      .setPosition(COORD_px, COORD_py)
      .setSize(LARGE_BUTTON, SMALL_BUTTON)
      .setCaptionLabel("COORD")
      .setColorBackground(color(127,127,255))
      .setColorCaptionLabel(color(255,255,255))  
      .moveTo(g2); 
}

/* mouse events */
public void mousePressed(){
  mouseDown += 1;
  if(mouseButton == LEFT){
    if(clickRotate%2 == 1){
      doRotate = !doRotate;
    }
    else if(clickPan%2 == 1){
      doPan = !doPan;
    }
  }
}

public void mouseDragged(MouseEvent e) {
    // Hold down the center mouse button and move the mouse to pan the camera
   if (mouseButton == CENTER) {
      panX += mouseX - pmouseX;
      panY += mouseY - pmouseY;
   }
   
   // Hold down the right omuse button an move the mouse to rotate the camera
   if (mouseButton == RIGHT){
      myRotX += (mouseY - pmouseY) * 0.01;
      myRotY += (mouseX - pmouseX) * 0.01;
   }
}

public void mouseMoved(){
   if (doPan){
      panX += mouseX - pmouseX;
      panY += mouseY - pmouseY;
   }
   if (doRotate){
      myRotX += (mouseY - pmouseY) * 0.01;
      myRotY += (mouseX - pmouseX) * 0.01;
   }
}



public void mouseWheel(MouseEvent event){
  float e = event.getCount();
  // Control scaling of the camera with the mouse wheel
  if (e > 0 ) {
     myscale *= 1.1;
     if(myscale > 2){
       myscale = 2;
     }
  }
  if (e < 0){
     myscale *= 0.9;
     if(myscale < 0.25){
       myscale = 0.25;
     }
  }
}

public void mouseReleased() {
  mouseDown -= 1;
}

public void keyPressed(){
  if (mode == ENTER_TEXT) {
    // Modify the input name for the new program
    if (workingText.length() < 10 && ( (key >= '0' && key <= '9') || (key >= 'A' && key <= 'Z') || (key >= 'a' && key <= 'z') )) {
      workingText += key;
    } else if (keyCode == BACKSPACE && workingText.length() > 0) {
      workingText = workingText.substring(0, workingText.length() - 1);
    } else if (keyCode == DELETE && workingText.length() > 0) {
      println("HERE");
      workingText = workingText.substring(1, workingText.length());
    }
    
    inputProgramName();
    return;
  } else if (key == 'e') {
    EE_MAPPING = (EE_MAPPING + 1) % 3;
  } else if(key == 'f'){
    armModel.currentFrame = armModel.getRotationMatrix();
  } else if(key == 'g'){
    armModel.resetFrame();
  } else if (key == 'q') {
    armModel.getQuaternion();
  } else if(key == 'r'){
    panX = 0;
    panY = 0;
    myscale = 0.5;
    myRotX = 0;
    myRotY = 0;
  } else if(key == 't'){
    // Release an object if it is currently being held
    if (armModel.held != null) {
      armModel.releaseHeldObject();
      armModel.endEffectorStatus = OFF;
    }
    
    float[] rot = {0, 0, 0, 0, 0, 0};
    armModel.setJointRotations(rot);
    intermediatePositions.clear();
  } else if(key == 'w'){
    /*------------Test Quaternion Rotation-------------------*/
    //armModel.currentFrame = armModel.getRotationMatrix();
    //float[] q = rotateQuat(armModel.getQuaternion(), 0, new PVector(1, 1, 1));
    //println("q = " + q[0] + ", " + q[1] + ", " + q[2] + ", " + q[3]);
    //PVector wpr = quatToEuler(q).mult(RAD_TO_DEG);
    //println("ee = " + wpr);
    //println();
    //armModel.resetFrame(); 
    /*------------Test Conversion Functions------------------*/
    //float[] q = armModel.getQuaternion();
    //PVector wpr = armModel.getWPR();
    //float[] qP = eulerToQuat(wpr);
    //PVector wprP = quatToEuler(qP);
    //println("converted values:");
    //println(qP);
    //println(wprP.mult(RAD_TO_DEG));
    //println();
    armModel.currentFrame = armModel.getRotationMatrix();
  } else if(key == 'y'){
    float[] rot = {PI, 0, 0, 0, 0, PI};
    armModel.setJointRotations(rot);
    intermediatePositions.clear();
  } else if (key == ENTER && (armModel.activeEndEffector == ENDEF_CLAW || 
                              armModel.activeEndEffector == ENDEF_SUCTION)) { 
    // Pick up an object within reach of the EE when the 'ENTER' button is pressed for either
    // the suction or claw EE
    ToolInstruction pickup;
    
    if (armModel.endEffectorStatus == ON) {
      pickup = (armModel.activeEndEffector == ENDEF_CLAW) ? 
                new ToolInstruction("RO", 4, OFF) : 
                new ToolInstruction("DO", 101, OFF);
    } else {
      pickup = (armModel.activeEndEffector == ENDEF_CLAW) ? 
                new ToolInstruction("RO", 4, ON) : 
                new ToolInstruction("DO", 101, ON);
    }
    
    pickup.execute();
  } else if (keyCode == KeyEvent.VK_1) {
    // Front view
    panX = 0;
    panY = 0;
    myRotX = 0f;
    myRotY = 0f;
  } else if (keyCode == KeyEvent.VK_2) {
    // Back view
    panX = 0;
    panY = 0;
    myRotX = 0f;
    myRotY = PI;
  } else if (keyCode == KeyEvent.VK_3) {
    // Left view
    panX = 0;
    panY = 0;
    myRotX = 0f;
    myRotY = PI / 2f;
  } else if (keyCode == KeyEvent.VK_4) {
    // Right view
    panX = 0;
    panY = 0;
    myRotX = 0f;
    myRotY = 3f * PI / 2F;
  } else if (keyCode == KeyEvent.VK_5) {
    // Top view
    panX = 0;
    panY = 0;
    myRotX = 3f * PI / 2F;
    myRotY = 0f;
  } else if (keyCode == KeyEvent.VK_6) {
    // Bottom view
    panX = 0;
    panY = 0;
    myRotX = PI / 2f;
    myRotY = 0f;
  }
  
  if(keyCode == UP){
    float[] angles = armModel.getJointRotations();
    calculateJacobian(angles);
    angles[0] += DEG_TO_RAD;
    armModel.setJointRotations(angles);
  } else if(keyCode == DOWN){
    float[] angles = armModel.getJointRotations();
    calculateJacobian(angles);
    angles[1] += DEG_TO_RAD;
    armModel.setJointRotations(angles);
  } else if(keyCode == LEFT){
    float[] angles = armModel.getJointRotations();
    calculateJacobian(angles);
    angles[2] += DEG_TO_RAD;
    armModel.setJointRotations(angles);
  } else if(keyCode == RIGHT){
    float[] angles = armModel.getJointRotations();
    calculateJacobian(angles);
    angles[3] += DEG_TO_RAD;
    armModel.setJointRotations(angles);
  } else if(key == 'z'){
    float[] angles = armModel.getJointRotations();
    calculateJacobian(angles);
    angles[4] += DEG_TO_RAD;
    armModel.setJointRotations(angles);
  } else if(key == 'x'){
    float[] angles = armModel.getJointRotations();
    calculateJacobian(angles);
    angles[5] += DEG_TO_RAD;
    armModel.setJointRotations(angles);
  }
  
  
  if (key == ' '){ 
    pan_normal();
  }
   
  if (keyCode == SHIFT){ 
    rotate_normal();
  }
}

//private void 

public void hide(){
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

public void show(){
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


public void mu() {
  if (mode == INSTRUCTION_NAV || mode == INSTRUCTION_EDIT) { saveProgramBytes( new File(sketchPath("tmp/programs.bin")) ); }
  
  contents = new ArrayList<ArrayList<String>>();
  
  contents.add( newLine("1 UTILITIES (NA)") );
  contents.add( newLine("2 TEST CYCLE (NA)") );
  contents.add( newLine("3 MANUAL FCTNS (NA)") );
  contents.add( newLine("4 ALARM (NA)") );
  contents.add(newLine("5 I/O (NA)"));
  contents.add(newLine("6 SETUP"));
  contents.add(newLine("7 FILE (NA)"));
  contents.add(newLine("8"));
  contents.add(newLine("9 USER (NA)"));
  contents.add(newLine("0 --NEXT--"));
  
  active_col = active_row = 0;
  mode = MENU_NAV;
  updateScreen(color(255,0,0), color(0));
}

// Data button
public void da() {
  contents = new ArrayList<ArrayList<String>>();
  
  contents.add( newLine("VIEW REGISTERS") );
  
  active_row = -1;
  active_col = text_render_start = 0;
  
  pickRegisterList();
}


public void NUM0(){
   addNumber("0");
}

public void NUM1(){
   addNumber("1");
}

public void NUM2(){
   addNumber("2");
}

public void NUM3(){
   addNumber("3");
}

public void NUM4(){
   addNumber("4");
}

public void NUM5(){
   addNumber("5");
}

public void NUM6(){
   addNumber("6");
}

public void NUM7(){
   addNumber("7");
}

public void NUM8(){
   addNumber("8");
}

public void NUM9(){
   addNumber("9");
}

public void addNumber(String number) {
  if (mode == SET_INSTRUCTION_REGISTER || mode == SET_INSTRUCTION_TERMINATION ||
      mode == JUMP_TO_LINE || mode == SET_DO_BRACKET || mode == SET_RO_BRACKET ||
      mode == SET_FRAME_INSTRUCTION_IDX) {
    workingText += number;
    options.set(1, workingText);
    updateScreen(color(255, 0, 0), color(0));
  } else if (mode == SET_INSTRUCTION_SPEED) {
    workingText += number;
    options.set(1, workingText + workingTextSuffix);
  } else if (mode == DIRECT_ENTRY_MODE || mode == INPUT_POINT_J || mode == INPUT_POINT_C) {
    if (active_row >= 0 && active_row < contents.size()) {
      String line = contents.get(active_row).get(0) + number;
      
      if (line.length() > 9 + which_option) {
        // Max length of a an input value
        line = line.substring(0,  9 + which_option);
      }
      
      // Concatenate the new digit
      contents.get(active_row).set(0, line);
    }
  } else if (mode == INPUT_FLOAT) {
    
    if (workingText.length() < 16) {
      workingText += number;
      options.set(2, workingText);
    }
  } else if (mode == INPUT_COMMENT_U || mode == INPUT_COMMENT_L) {
    
    // Replace current entry with a number
    StringBuilder limbo = new StringBuilder(workingText);
    limbo.setCharAt(active_col, number.charAt(0));
    workingText = limbo.toString();
    
    updateComment();
  }
  
  updateScreen(color(255, 0, 0), color(0));
}

public void PERIOD() {
   if (NUM_MODE == ON){
      nums.add(-1);
   } else if (mode == DIRECT_ENTRY_MODE || mode == INPUT_POINT_J || mode == INPUT_POINT_C) {
     
    if (active_row >= 0 && active_row < contents.size()) {
      // Add decimal point
      String line = contents.get(active_row).get(0) + ".";
      
      if (line.length() > 9 + which_option) {
        // Max length of a an input value
        line = line.substring(0,  9 + which_option);
      }
      
      contents.get(active_row).set(0, line);
    }
  } else if (mode == INPUT_FLOAT) {
    
    if (workingText.length() < 16) {
      workingText += ".";
      options.set(2, workingText);
    }
  } else if (mode != INPUT_COMMENT_U || mode != INPUT_COMMENT_L) {
     workingText += ".";
  }
   
   updateScreen(color(255,0,0), color(0,0,0));
}

public void LINE() {
  if (mode == DIRECT_ENTRY_MODE || mode == INPUT_POINT_J || mode == INPUT_POINT_C) {
    
    if (active_row >= 0 && active_row < contents.size()) {
      String line = contents.get(active_row).get(0);
      
      // Mutliply current number by -1
      if (line.length() > (which_option + 1) && line.charAt(which_option) == '-') {
        line = line.substring(0, which_option) + line.substring(which_option + 1, line.length());
      } else if (line.length() > which_option) {
        line = line.substring(0, which_option) + "-" + line.substring(which_option, line.length());
      }
      
      if (line.length() > 9 + which_option) {
        // Max length of a an input value
        line = line.substring(0,  9 + which_option);
      }
      
      contents.get(active_row).set(0, line);
    }
    
    updateScreen(color(255, 0, 0), color(0));
  } else if ((mode != INPUT_COMMENT_U && mode != INPUT_COMMENT_L) && workingText != null && workingText.length() > 0) {
    // Mutliply current number by -1
    if (workingText.charAt(0) == '-') {
      workingText = workingText.substring(1);
    } else {
      workingText = "-" + workingText;
    }
    
    options.set(2, workingText);
    updateScreen(color(255,0,0), color(0,0,0));
  }
}

public void se(){
  // Save when exiting a program
   if (mode == INSTRUCTION_NAV || mode == INSTRUCTION_EDIT) { saveProgramBytes( new File(sketchPath("tmp/programs.bin")) ); }
   
   active_program = 0;
   active_instruction = 0;
   active_row = 0;
   active_col = -1;
   text_render_start = 0;
   mode = PROGRAM_NAV;
   clearScreen();
   loadPrograms();
   updateScreen(color(255,0,0), color(0,0,0));
}

public void up(){
   switch (mode){
      case PROGRAM_NAV:
         options = new ArrayList<String>();
         clearOptions();
         
         if (shift == ON && text_render_start > 0) {
           // Move display frame up an entire screen's display length
           int t = text_render_start;
           
           text_render_start = max(0, t - (ITEMS_TO_SHOW - 1));
           active_program = active_program + min(0, text_render_start - t);
         } else if (shift == OFF && active_program > 0) {
           // Move up a single row
           int i = active_program,
               r = active_row;
           
           active_program = max(0, i - 1);
           active_row = max(0, r + min(active_program - i, 0));
           text_render_start = text_render_start + min((active_program - i) - (active_row - r), 0);
         }
         
         loadPrograms();
         
         if (DISPLAY_TEST_OUTPUT) {
           System.out.printf("\nRow: %d\nColumn: %d\nProg: %d\nTRS: %d\n\n",
                             active_row, active_col, active_program, text_render_start);
         }
         
         break;
      case INSTRUCTION_NAV:
         options = new ArrayList<String>();
         clearOptions();
         
         if (shift == ON && text_render_start > 0) {
           // Move display frame up an entire screen's display length
           int t = text_render_start;
           
           text_render_start = max(0, t - (ITEMS_TO_SHOW - 1));
           active_instruction = active_instruction + min(0, text_render_start - t);
         } else if (shift == OFF && active_instruction > 0) {
           // Move up a single row
           int i = active_instruction,
               r = active_row;
           
           active_instruction = max(0, i - 1);
           active_row = max(0, r + min(active_instruction - i, 0));
           text_render_start = text_render_start + min((active_instruction - i) - (active_row - r), 0);
           /*active_row -= (active_row >= 1) ? 1 : 0;
           if(active_instruction < text_render_start)
             text_render_start -= 1;*/
         }
         
         active_col = max( 0, min( active_col, contents.get(active_row).size() - 1 ) );
         loadInstructions(active_program);
         
         if (DISPLAY_TEST_OUTPUT) {
           System.out.printf("\nRow: %d\nColumn: %d\nInst: %d\nTRS: %d\n\n",
                             active_row, active_col, active_instruction, text_render_start);
         }
         
         break;
      case VIEW_REG:
      case VIEW_POS_REG_J:
      case VIEW_POS_REG_C:
         
         if (shift == ON && text_render_start > 0) {
           // Move display frame up an entire screen's display length
           int t = text_render_start;
           
           text_render_start = max(0, t - (ITEMS_TO_SHOW - 2));
           active_index = active_index + min(0, text_render_start - t);
         } else if (shift == OFF && active_index > 0) {
           // Move up a single row
           int i = active_index,
               r = active_row;
           
           active_index = max(0, i - 1);
           active_row = max(2, r + min(active_index - i, 0));
           text_render_start = text_render_start + min((active_index - i) - (active_row - r), 0);
         }
         
         active_col = max( 0, min( active_col, contents.get(active_row).size() - 1 ) );
         viewRegisters();
         
         if (DISPLAY_TEST_OUTPUT) {
           System.out.printf("\nRow: %d\nColumn: %d\nIdx: %d\nTRS: %d\n\n",
                             active_row, active_col, active_index, text_render_start);
         }
         
         break;
      case INSTRUCTION_EDIT:
      case PICK_FRAME_MODE:
      case PICK_FRAME_METHOD:
      case THREE_POINT_MODE:
      case SIX_POINT_MODE:
      case FOUR_POINT_MODE:
      case SET_DO_STATUS:
      case SET_RO_STATUS:
      case PICK_REG_LIST:
         which_option = max(0, which_option - 1);
         
         break;
      case MENU_NAV:
      case SETUP_NAV:
      case NAV_TOOL_FRAMES:
      case NAV_USER_FRAMES:
      case PICK_INSTRUCTION:
      case IO_SUBMENU:
      case SET_FRAME_INSTRUCTION:
      case EDIT_MENU:
         active_row = max(0, active_row - 1);
         break;
      case ACTIVE_FRAMES:
      case DIRECT_ENTRY_MODE:
      case INPUT_POINT_C:
      case INPUT_POINT_J:
         active_row = max(1, active_row - 1);
         
         break;
      case INPUT_COMMENT_U:
      case INPUT_COMMENT_L:
         which_option = max(0, which_option - 1);
         // Navigate options menu to switch the function keys functions
         if (which_option == 0) {
           mode = INPUT_COMMENT_U;
         } else if (which_option == 1) {
           mode = INPUT_COMMENT_L;
         }
         // Reset function key states
         for (int idx = 0; idx < letterStates.length; ++idx) { letterStates[idx] = 0; }
         
         break;
   }
   
   updateScreen(color(255,0,0), color(0,0,0));
}

public void dn(){
  int size;
   switch (mode){
      case PROGRAM_NAV:
         options = new ArrayList<String>();
         clearOptions();
         
         size = programs.size();
         
         if (shift == ON && ( (text_render_start + ITEMS_TO_SHOW) < size )) {
           // Move display frame down an entire screen's display length
           int t = text_render_start;
           
           text_render_start = min(text_render_start + ITEMS_TO_SHOW - 1, size - ITEMS_TO_SHOW);
           active_program = active_program + max(0, text_render_start - t);
         } else if (shift == OFF && active_program < (size - 1)) {
           // Move down one row
           int i = active_program,
               r = active_row;
           
           active_program = min(i + 1, size - 1);
           active_row = min(r + max(0, (active_program - i)), contents.size() - 1);
           text_render_start = text_render_start + max(0, (active_program - i) - (active_row - r));
         }
         
         loadPrograms();
         
         if (DISPLAY_TEST_OUTPUT) {
           System.out.printf("\nRow: %d\nColumn: %d\nProg: %d\nTRS: %d\n\n",
                             active_row, active_col, active_program, text_render_start);
         }
         
         break;
      case INSTRUCTION_NAV:
         options = new ArrayList<String>();
         clearOptions();
         
         size = programs.get(active_program).getInstructions().size();
        
         if (shift == ON && ( (text_render_start + ITEMS_TO_SHOW) < size )) {
           // Move display frame down an entire screen's display length
           int t = text_render_start;
           
           text_render_start = min(text_render_start + ITEMS_TO_SHOW - 1, size - ITEMS_TO_SHOW);
           active_instruction = active_instruction + max(0, text_render_start - t);
         } else if (shift == OFF && active_instruction < (size - 1)) {
           // Move down one row
           int i = active_instruction,
               r = active_row;
           
           active_instruction = min(i + 1, size - 1);
           active_row = min(r + max(0, (active_instruction - i)), contents.size() - 1);
           text_render_start = text_render_start + max(0, (active_instruction - i) - (active_row - r));
           /*active_row += (active_row < ITEMS_TO_SHOW-1) ? 1 : 0;
           if(active_instruction > text_render_start+ITEMS_TO_SHOW-1)
             text_render_start += 1;*/
         }
          //<>//
         loadInstructions(active_program);
         
         if (DISPLAY_TEST_OUTPUT) {
           System.out.printf("\nRow: %d\nColumn: %d\nInst: %d\nTRS: %d\n\n",
                             active_row, active_col, active_instruction, text_render_start);
         }
         
         break;
      case VIEW_REG:
      case VIEW_POS_REG_J:
      case VIEW_POS_REG_C:
      
        size = (mode == VIEW_REG) ? REG.length : POS_REG.length;
        
        if (shift == ON && ( (text_render_start + ITEMS_TO_SHOW) < size )) {
          // Move display frame down an entire screen's display length
          int t = text_render_start;
          
          text_render_start = min(text_render_start + ITEMS_TO_SHOW - 2, size - (ITEMS_TO_SHOW - 1));
          active_index = active_index + max(0, text_render_start - t);
        } else if (shift == OFF && active_index < (size - 1)) {
          // Move down one row
          int i = active_index,
              r = active_row;
          
          active_index = min(i + 1, size - 1);
          active_row = min(r + max(0, (active_index - i)), contents.size() - 1);
          text_render_start = text_render_start + max(0, (active_index - i) - (active_row - r));
        }
        
        active_col = max( 0, min( active_col, contents.get(active_row).size() - 1 ) );
        viewRegisters();
        
        if (DISPLAY_TEST_OUTPUT) {
          System.out.printf("\nRow: %d\nColumn: %d\nIdx: %d\nTRS: %d\n\n",
                            active_row, active_col, active_index, text_render_start);
        }
        
        break;
      case INSTRUCTION_EDIT:
      case PICK_FRAME_MODE:
      case PICK_FRAME_METHOD:
      case THREE_POINT_MODE:
      case SIX_POINT_MODE:
      case FOUR_POINT_MODE:
      case SET_DO_STATUS:
      case SET_RO_STATUS:
      case PICK_REG_LIST:
         which_option = min(which_option + 1, options.size() - 1);
         
         break;
      case MENU_NAV:
      case SETUP_NAV:
      case NAV_TOOL_FRAMES:
      case NAV_USER_FRAMES:
      case ACTIVE_FRAMES:
      case DIRECT_ENTRY_MODE:
      case INPUT_POINT_C:
      case INPUT_POINT_J:
      case PICK_INSTRUCTION:
      case IO_SUBMENU:
      case SET_FRAME_INSTRUCTION:
      case EDIT_MENU:
         active_row = min(active_row  + 1, contents.size() - 1);
         
         break;
      case INPUT_COMMENT_U:
      case INPUT_COMMENT_L:
         which_option = min(which_option + 1, options.size() - 1);
         // Navigate options menu to switch the function keys functions
         if (which_option == 0) {
           mode = INPUT_COMMENT_U;
         } else if (which_option == 1) {
           mode = INPUT_COMMENT_L;
         }
         // Reset function key states
         for (int idx = 0; idx < letterStates.length; ++idx) { letterStates[idx] = 0; }
         
         break;
   }  
   
   updateScreen(color(255,0,0), color(0,0,0));
}

public void lt(){
   switch (mode){
      case PROGRAM_NAV:
          break;
      case INSTRUCTION_NAV:
          options = new ArrayList<String>();
          clearOptions();
          
          active_col = max(0, active_col - 1);
          
          updateScreen(color(255,0,0), color(0,0,0));
          break;
      case INSTRUCTION_EDIT:
          mode = INSTRUCTION_NAV;
          lt();
          break;
      case VIEW_REG:
      case VIEW_POS_REG_J:
      case VIEW_POS_REG_C:
        active_col = max(0, active_col - 1);
        updateScreen(color(255, 0, 0), color(0));
        
        break;
      case INPUT_COMMENT_U:
      case INPUT_COMMENT_L:
        active_col = max(0, active_col - 1);
        // Reset function key states
        for (int idx = 0; idx < letterStates.length; ++idx) { letterStates[idx] = 0; }
        updateScreen(color(255, 0, 0), color(0));
        
        break;
   }
   
}


public void rt(){
  switch (mode){
      case PROGRAM_NAV:
          break;
      case INSTRUCTION_NAV:
          options = new ArrayList<String>();
          clearOptions();
          
          active_col = min(active_col + 1, contents.get(active_row).size() - 1);
          println("active column: " + active_col);
          updateScreen(color(255,0,0), color(0,0,0));
          break; //<>//
      case INSTRUCTION_EDIT:
          mode = INSTRUCTION_NAV;
          rt();
          break;
      case MENU_NAV:
          if (active_row == 5) { // SETUP
            contents = new ArrayList<ArrayList<String>>();
            
            contents.add( newLine("1 Prog Select (NA)") );
            contents.add( newLine("2 General (NA)") );
            contents.add( newLine("3 Call Guard (NA)") );
            contents.add( newLine("4 Frames") );
            contents.add( newLine("5 Macro (NA)") );
            contents.add( newLine("6 Ref Position (NA)") );
            contents.add( newLine("7 Port Init (NA)") );
            contents.add( newLine("8 Ovrd Select (NA)") );
            contents.add( newLine("9 User Alarm (NA)") );
            contents.add( newLine("0 --NEXT--") );
            
            active_col = active_row = 0;
            mode = SETUP_NAV;
            updateScreen(color(255,0,0), color(0));
          }
          break;
       case PICK_INSTRUCTION:
          if (active_row == 0) { // I/O
            contents = new ArrayList<ArrayList<String>>();
            
            contents.add( newLine("1 Cell Intface (NA)") );
            contents.add( newLine("2 Custom (NA)") );
            contents.add( newLine("3 Digital") );
            contents.add( newLine("4 Analog (NA)") );
            contents.add( newLine("5 Group (NA)") );
            contents.add( newLine("6 Robot") );
            contents.add( newLine("7 UOP (NA)") );
            contents.add( newLine("8 SOP (NA)") );
            contents.add( newLine("9 Interconnect (NA)") );
            
            active_col = active_row = 0;
            mode = IO_SUBMENU;
            updateScreen(color(255,0,0), color(0));
          } else if (active_row == 1) { // Offset/Frames
            
            contents = new ArrayList<ArrayList<String>>();
            
            contents.add( newLine("1 UTOOL_NUM") );
            contents.add( newLine("1 UFRAME_NUM") );
            
            mode = SET_FRAME_INSTRUCTION;
            active_col = active_row = 0;
            updateScreen(color(255,0,0), color(0));
          }
          break;
       case DIRECT_ENTRY_MODE:
       case INPUT_POINT_C:
       case INPUT_POINT_J:
         
         // Delete a digit from the being of the number entry
         if (shift == ON && active_row >= 0 && active_row < contents.size()) {
           String entry = contents.get(active_row).get(0),
                  new_entry = "";
           
            if (entry.length() > which_option) {
              new_entry = entry.substring(0, which_option);
              
              if (entry.charAt(which_option) == '-') {
                if (entry.length() > (which_option + 2)) {
                  // Keep negative sign until the last digit is removed
                  new_entry += "-" + entry.substring((which_option + 2), entry.length());
                }
              } else if (entry.length() > (which_option + 1)) {
                new_entry += entry.substring((which_option + 1), entry.length());
              }
            } else {
              // Blank entry
              new_entry = entry;
            }
            
            contents.get(active_row).set(0, new_entry);
         }
        
         updateScreen(color(255, 0, 0), color(0));
         break;
       case VIEW_REG:
       case VIEW_POS_REG_J:
       case VIEW_POS_REG_C:
         active_col = min(active_col + 1, contents.get(active_row).size() - 1);
         updateScreen(color(255, 0, 0), color(0));
         
         break;
       case INPUT_COMMENT_U:
       case INPUT_COMMENT_L:
       
         if (shift == ON) {
           // Delete key function
           if (workingText.length() > 1) {
             workingText = workingText.substring(1, workingText.length());
             updateComment();
             active_col = min(active_col, contents.get(active_row).size() - 1);
           }
         } else {
           // Add an insert element if the length of the current comment is less than 16
           int len = workingText.length();
           if (len <= 16 && workingText.charAt(len - 1) != '\0') { workingText += '\0'; }
           updateComment();
         
           active_col = min(active_col + 1, contents.get(active_row).size() - 1);
           updateScreen(color(255, 0, 0), color(0));
         }
         
         // Reset function key states
         for (int idx = 0; idx < letterStates.length; ++idx) { letterStates[idx] = 0; }
         
         break;
   }
}

//toggle shift state and button highlight
public void sf(){
   if (shift == OFF){ 
	 shift = ON;
     ((Button)cp5.get("sf")).setColorBackground(color(255, 0, 0));
   }
   else{
     shift = OFF;
     ((Button)cp5.get("sf")).setColorBackground(color(127, 127, 255));
   }
}

public void st() {
   if (step == OFF){ 
     step = ON;
     ((Button)cp5.get("st")).setColorBackground(color(255, 0, 0));
   }
   else{
     step = OFF;
     ((Button)cp5.get("st")).setColorBackground(color(127, 127, 255));
   }
}

public void pr(){
   se();
}


public void goToEnterTextMode() {
    clearScreen();
    active_row = 0;
    active_col = -1;
    super_mode = mode;
    mode = ENTER_TEXT;
    
    inputProgramName();
}


public void f1() {
  switch (mode) {
    case PROGRAM_NAV:
      //shift = OFF;
      break;
    case INSTRUCTION_NAV:
      if (shift == ON) {
        
        PVector eep = armModel.getEEPos();
        eep = convertNativeToWorld(eep);
        Program prog = programs.get(active_program);
        int reg = prog.nextRegister();
        float[] q = armModel.getQuaternion();
        float[] j = armModel.getJointRotations();
        
        prog.addRegister(new Point(eep.x, eep.y, eep.z, q[0], q[1], q[2], q[3],
                                   j[0], j[1], j[2], j[3], j[4], j[5]), reg);
                                   
        MotionInstruction insert = new MotionInstruction(
          (curCoordFrame == COORD_JOINT ? MTYPE_JOINT : MTYPE_LINEAR),
          reg,
          false,
          (curCoordFrame == COORD_JOINT ? liveSpeed : liveSpeed*armModel.motorSpeed),
          0,
          activeUserFrame,
          activeToolFrame);
        prog.addInstruction(insert);
          
        active_instruction = prog.getInstructions().size() - 1;
        active_col = 0;
        /* 13 is the maximum number of instructions that can be displayed at one point in time */
        active_row = min(active_instruction, ITEMS_TO_SHOW - 1);
        text_render_start = active_instruction - active_row;
        
        loadInstructions(active_program);
        updateScreen(color(255,0,0), color(0,0,0));
      } else {
        
        contents = new ArrayList<ArrayList<String>>();
        
        contents.add( newLine("1 I/O") );
        contents.add( newLine("2 Offset/Frames") );
        contents.add( newLine("(Others not yet implemented)") );
        
        active_col = active_row = 0;
        mode = PICK_INSTRUCTION;
        updateScreen(color(255,0,0), color(0));
      }
      break;
    case NAV_TOOL_FRAMES:
      if (shift == ON) {
        
        super_mode = mode;
        curFrameIdx = active_row;
        loadFrameDetails();
      } else {
        // Set the current tool frame
        if (active_row >= 0) {
          activeToolFrame = active_row;
          // Update the Robot Arm's current frame rotation matrix
          if (curCoordFrame == COORD_TOOL) {
            armModel.currentFrame = toolFrames[activeToolFrame].getNativeAxes();
          }
        }
      }
      break;
    case NAV_USER_FRAMES:
      if (shift == ON) {
        
        super_mode = mode;
        curFrameIdx = active_row;
        loadFrameDetails();
      } else {
        // Set the current user frame
        if (active_row >= 0) {
          activeUserFrame = active_row;
          // Update the Robot Arm's current frame rotation matrix
          if (curCoordFrame == COORD_USER) {
            armModel.currentFrame = userFrames[activeUserFrame].getNativeAxes();
          }
        }
      }
      break;
    case ACTIVE_FRAMES:
      if (active_row == 1) {
        loadFrames(COORD_TOOL);
      } else if (active_row == 2) {
        loadFrames(COORD_USER);
      }
    case INSTRUCTION_EDIT:
      //shift = OFF;
      break;
    case THREE_POINT_MODE:
    case SIX_POINT_MODE:
    case FOUR_POINT_MODE:
      ref_point = (shift == ON) ? null : armModel.getEEPos();
      
      break;
    case VIEW_REG:
      if (active_col == 1) {
        // Bring up comment menu
        loadInputRegisterCommentMethod();
      } else if (active_col == 2) {
        // Bring up float input menu
        workingText = "";
        loadInputRegisterValueMethod();
      }
      
      break;
    case VIEW_POS_REG_J:
      if (active_col == 1) {
        // Bring up comment menu
        loadInputRegisterCommentMethod();
      } else if (active_col >= 2) {
        // Bring up Point editing menu
        super_mode = mode;
        mode = INPUT_POINT_J;
        loadInputRegisterPointMethod();
      }
      
      break;
    case VIEW_POS_REG_C:
      if (active_col == 1) {
        // Bring up comment menu
        loadInputRegisterCommentMethod();
      } else if (active_col >= 2) {
        // Bring up Point editing menu
        super_mode = mode;
        mode = INPUT_POINT_C;
        loadInputRegisterPointMethod();
      }
    
      break;
    case INPUT_COMMENT_U:
    case INPUT_COMMENT_L:
      char newChar = '\0';
      
      if (mode == INPUT_COMMENT_U) {
        newChar = (char)('A' + letterStates[0]);
      } else if (mode == INPUT_COMMENT_L) {
        newChar = (char)('a' + letterStates[0]);
      }
      
      // Insert a character A - F (or a - f)
      StringBuilder limbo = new StringBuilder(workingText);
      limbo.setCharAt(active_col, newChar);
      workingText = limbo.toString();
      // Update and reset the letter states
      letterStates[0] = (letterStates[0] + 1) % 6;
      for (int idx = 1; idx < letterStates.length; ++idx) { letterStates[idx] = 0; }
      
      updateComment();
      
      break;
  }
}


public void f2() {
  if (mode == PROGRAM_NAV) {
    workingText = "";
    active_program = -1;
    goToEnterTextMode();
  } else if (mode == FRAME_DETAIL) {
    options = new ArrayList<String>();
    
    if (super_mode == NAV_USER_FRAMES) {
      options.add("1. Three Point");
      options.add("2. Four Point");
      options.add("3. Direct Entry");
    } else if (super_mode == NAV_TOOL_FRAMES) {
      options.add("1. Three Point");
      options.add("2. Six Point");
      options.add("3. Direct Entry");
    }
    mode = PICK_FRAME_METHOD;
    which_option = 0;
    updateScreen(color(255,0,0), color(0));
  } if (mode == NAV_TOOL_FRAMES) {
     
     // Reset the highlighted frame in the tool frame list
     if (active_row >= 0) {
        toolFrames[active_row] = new Frame();
        saveFrameBytes( new File(sketchPath("tmp/frames.bin")) );
        loadFrames(COORD_TOOL);
      }
   } else if (mode == NAV_USER_FRAMES) {
     
     // Reset the highlighted frame in the user frames list
     if (active_row >= 0) {
       userFrames[active_row] = new Frame();
       saveFrameBytes( new File(sketchPath("tmp/frames.bin")) );
       loadFrames(COORD_USER);
     }
   } else if (mode == ACTIVE_FRAMES) {
     // Reset the active frames for the User or Tool Coordinate Frames
     if (active_row == 1) {
       
       activeToolFrame = -1;
       
       // Leave the Tool Frame
       if (curCoordFrame == COORD_TOOL || curCoordFrame == COORD_WORLD) {
         curCoordFrame = COORD_WORLD;
         armModel.resetFrame();
       }
     } else if (active_row == 2) {
       activeUserFrame = -1;
       
       // Leave the User Frame
       if (curCoordFrame == COORD_USER) {
         curCoordFrame = COORD_WORLD;
         armModel.resetFrame();
       }
     }
     
     loadActiveFrames();
   } else if (mode == VIEW_REG || mode == VIEW_POS_REG_J || mode == VIEW_POS_REG_C) {
     pickRegisterList();
  } else if (mode == INPUT_COMMENT_U || mode == INPUT_COMMENT_L) {
    char newChar = '\0';
    
    if (mode == INPUT_COMMENT_U) {
      newChar = (char)('G' + letterStates[1]);
    } else if (mode == INPUT_COMMENT_L) {
      newChar = (char)('g' + letterStates[1]);
    }
    
    // Insert a character G - L (or g - l)
    StringBuilder limbo = new StringBuilder(workingText);
    limbo.setCharAt(active_col, newChar);
    workingText = limbo.toString();
  // Update and reset the letter states
    letterStates[0] = 0;
    letterStates[1] = (letterStates[1] + 1) % 6;
    for (int idx = 2; idx < letterStates.length; ++idx) { letterStates[idx] = 0; }
    
    updateComment();
  }
}


public void f3() {
  if (mode == PROGRAM_NAV) {
    options = new ArrayList<String>();
    options.add("Delete this program?  F4 = YES, F5 = NO");
    which_option = 0;
    
    super_mode = mode;
    mode = CONFIRM_DELETE;
    updateScreen(color(255, 0, 0), color(0));
  } else if (mode == NAV_TOOL_FRAMES || mode == NAV_USER_FRAMES) {
    options = new ArrayList<String>();
    options.add("1.Tool Frame");
    options.add("2.User Frame");
    //options.add("3.Jog Frame");
    
    mode = PICK_FRAME_MODE;
    which_option = 0;
    updateScreen(color(255,0,0), color(0));
  } else if (mode == INPUT_COMMENT_U || mode == INPUT_COMMENT_L) {
    char newChar = '\0';
    
    if (mode == INPUT_COMMENT_U) {
      newChar = (char)('M' + letterStates[2]);
    } else if (mode == INPUT_COMMENT_L) {
      newChar = (char)('m' + letterStates[2]);
    }
    
    // Insert a character M - R (or m - r)
    StringBuilder limbo = new StringBuilder(workingText);
    limbo.setCharAt(active_col, newChar);
    workingText = limbo.toString();
    // Update and reset the letter states
    letterStates[0] = 0;
    letterStates[1] = 0;
    letterStates[2] = (letterStates[2] + 1) % 6;
    letterStates[3] = 0;
    letterStates[4] = 0;
    
    updateComment();
  }
}


public void f4() {
   switch (mode){
      case INSTRUCTION_NAV:
         Instruction ins = programs.get(active_program).getInstructions().get(active_instruction);
         if (ins instanceof MotionInstruction) {
           switch (active_col){
             case 2: // motion type
                options = new ArrayList<String>();
                options.add("1.JOINT");
                options.add("2.LINEAR");
                options.add("3.CIRCULAR");
                //NUM_MODE = ON;
                mode = INSTRUCTION_EDIT;
                which_option = 0;
                break;
             case 3: // register type
                options = new ArrayList<String>();
                options.add("1.LOCAL(P)");
                options.add("2.GLOBAL(PR)");
                //NUM_MODE = ON;
                mode = INSTRUCTION_EDIT;
                which_option = 0;
                break;
             case 4: // register
                options = new ArrayList<String>();
                options.add("Use number keys to enter a register number (0-999)");
                workingText = "";
                options.add(workingText);
                mode = SET_INSTRUCTION_REGISTER;
                which_option = 0;
                break;
             case 5: // speed
                options = new ArrayList<String>();
                options.add("Use number keys to enter a new speed");
                MotionInstruction castIns = getActiveMotionInstruct();
                if (castIns.getMotionType() == MTYPE_JOINT) {
                  speedInPercentage = true;
                  workingTextSuffix = "%";
                } else {
                  workingTextSuffix = "mm/s";
                  speedInPercentage = false;
                }
                workingText = "";
                options.add(workingText + workingTextSuffix);
                mode = SET_INSTRUCTION_SPEED;
                which_option = 0;
                break;
             case 6: // termination type
                options = new ArrayList<String>();
                options.add("Use number keys to enter termination percentage (0-100; 0=FINE)");
                workingText = "";
                options.add(workingText);
                mode = SET_INSTRUCTION_TERMINATION;
                which_option = 0;
                break;
           }
         } 
         break;
     case CONFIRM_DELETE:
         if (super_mode == PROGRAM_NAV) {
           int progIdx = active_program;
           
           if (progIdx >= 0 && progIdx < programs.size()) {
             programs.remove(progIdx);
             
             if (active_program >= programs.size()) {
               active_program = programs.size() - 1;
               /* 13 is the maximum number of instructions that can be displayed at one point in time */
               active_row = min(active_program, ITEMS_TO_SHOW - 1);
               text_render_start = active_program - active_row;
             }
             
             mode = super_mode;
             super_mode = NONE;
             loadPrograms();
             updateScreen(color(255,0,0), color(0));
             saveProgramBytes( new File(sketchPath("tmp/programs.bin")) );
           }
         } else if (super_mode == INSTRUCTION_NAV) {
             Program prog = programs.get(active_program);
             prog.getInstructions().remove(active_instruction);
             deleteInstEpilogue();
         }
         break;
     case INPUT_COMMENT_U:
     case INPUT_COMMENT_L:
       char newChar = '\0';
       
       if (mode == INPUT_COMMENT_U) {
         newChar = (char)('S' + letterStates[3]);
       } else if (mode == INPUT_COMMENT_L) {
         newChar = (char)('s' + letterStates[3]);
       }
       
       // Insert a character S - X (or s - x)
       StringBuilder limbo = new StringBuilder(workingText);
       limbo.setCharAt(active_col, newChar);
       workingText = limbo.toString();
       // Update and reset the letter states
       for (int idx = 0; idx < 3; ++idx) { letterStates[idx] = 0; }
       letterStates[3] = (letterStates[3] + 1) % 6;
       letterStates[4] = 0;
       
       
       updateComment();
       
       break;
   }
   
   updateScreen(color(255,0,0), color(0,0,0));
}

public void f5() {
  if (mode == INSTRUCTION_NAV) {
    if (shift == ON) {
      // overwrite current instruction
      PVector eep = armModel.getEEPos();
      eep = convertNativeToWorld(eep);
      Program prog = programs.get(active_program);
      int reg = prog.nextRegister();
      float[] q = armModel.getQuaternion();
      float[] j = armModel.getJointRotations();
      prog.addRegister(new Point(eep.x, eep.y, eep.z, q[0], q[1], q[2], q[3],
                                 j[0], j[1], j[2], j[3], j[4], j[5]), reg);
      MotionInstruction insert = new MotionInstruction(
        (curCoordFrame == COORD_JOINT ? MTYPE_JOINT : MTYPE_LINEAR),
        reg,
        false,
        (curCoordFrame == COORD_JOINT ? liveSpeed : liveSpeed*armModel.motorSpeed),
        0,
        activeUserFrame,
        activeToolFrame);
      prog.overwriteInstruction(active_instruction, insert);
      active_col = 0;
      loadInstructions(active_program);
      updateScreen(color(255,0,0), color(0,0,0));
    } else {
      if (active_col == 0) {
        // if you're on the line number, bring up a list of instruction editing options
        contents = new ArrayList<ArrayList<String>>();
        
        contents.add( newLine("1 Insert (NA)") );
        contents.add( newLine("2 Delete") );
        contents.add( newLine("3 Copy (NA)") );
        contents.add( newLine("4 Find (NA)") );
        contents.add( newLine("5 Replace (NA)") );
        contents.add( newLine("6 Renumber (NA)") );
        contents.add( newLine("7 Comment (NA)") );
        contents.add( newLine("8 Undo (NA)") );
        
        active_col = active_row = 0;
        mode = EDIT_MENU;
        updateScreen(color(255,0,0), color(0));
      } else if (active_col == 2 || active_col == 3) { 
        // show register contents if you're highlighting a register
        Instruction ins = programs.get(active_program).getInstructions().get(active_instruction);
         if (ins instanceof MotionInstruction) {
         MotionInstruction castIns = (MotionInstruction)ins;
          Point p = castIns.getVector(programs.get(active_program));
          options = new ArrayList<String>();
          options.add("Data of the point in this register (press ENTER to exit):");
          if (castIns.getMotionType() != MTYPE_JOINT) {
            options.add("x: " + p.pos.x + "  y: " + p.pos.y + "  z: " + p.pos.z);
            PVector wpr = quatToEuler(p.ori);
            options.add("w: " + wpr.x + "  p: " + wpr.y + "  r: " + wpr.z);
          } else {
            options.add("j1: " + p.joints[0] + "  j2: " + p.joints[1] + "  j3: " + p.joints[2]);
            options.add("j4: " + p.joints[3] + "  j5: " + p.joints[4] + "  j6: " + p.joints[5]);
          }
          mode = VIEW_REGISTER;
          which_option = 0;
          loadInstructions(active_program);
          updateScreen(color(255,0,0), color(0,0,0));
        }
      }
    }
  } else if (mode == THREE_POINT_MODE || mode == SIX_POINT_MODE || mode == FOUR_POINT_MODE) {
    if (teachPointTMatrices != null) {
      
      pushMatrix();
      resetMatrix();
      applyModelRotation(armModel, false);
      // Save current position of the EE
      float[][] tMatrix = getTransformationMatrix();
      
      // Add the current teach point to the running list of teach points
      if (which_option >= 0 && which_option < teachPointTMatrices.size()) {
        // Cannot override the origin once it is calculated for the six point method
        teachPointTMatrices.set(which_option, tMatrix);
      } else if ((mode == THREE_POINT_MODE && teachPointTMatrices.size() < 3) ||
                 (mode == FOUR_POINT_MODE && teachPointTMatrices.size() < 4) ||
                 (mode == SIX_POINT_MODE && teachPointTMatrices.size() < 6)) {
        
        // Add a new point as long as it does not exceed number of points for a specific method
        teachPointTMatrices.add(tMatrix);
        // increment which_option
        which_option = min(which_option + 1, options.size() - 1);
      }
      
      popMatrix();
    }
    
    int limbo = mode;
    loadFrameDetails();
    mode = limbo;
    loadPointList();
  } else if (mode == CONFIRM_DELETE) {
    
    if (super_mode == PROGRAM_NAV) {  
      options = new ArrayList<String>();
      which_option = -1;
      
      mode = super_mode;
      super_mode = NONE;
      updateScreen(color(250, 0, 0), color(0));
    } else if (super_mode == INSTRUCTION_NAV) {
      deleteInstEpilogue();
    }
  } else if (mode == INPUT_COMMENT_U || mode == INPUT_COMMENT_L) {
    char newChar = '\0';
    
    if (letterStates[4] < 2) {
      
      if (mode == INPUT_COMMENT_U) {
        newChar = (char)('Y' + letterStates[4]);
      } else if (mode == INPUT_COMMENT_L) {
        newChar = (char)('y' + letterStates[4]);
      }
    } else if (letterStates[4] == 2) {
      newChar = '_';
    } else if (letterStates[4] == 3) {
      newChar = '@';
    } else if (letterStates[4] == 4) {
      newChar = '*';
    } else if (letterStates[4] == 5) {
      newChar = '.';
    }
    
    // Insert a character Y, Z, (or y, z) _, @, *, .
    StringBuilder limbo = new StringBuilder(workingText);
    limbo.setCharAt(active_col, newChar);
    workingText = limbo.toString();
    // Update and reset the letter states
    for (int idx = 0; idx < letterStates.length - 1; ++idx) { letterStates[idx] = 0; }
    letterStates[4] = (letterStates[4] + 1) % 6;
    
    updateComment();
  }
}

/* Stops all of the Robot's movement */
public void hd() {
  
  for (Model model : armModel.segments) {
    model.jointsMoving[0] = 0;
    model.jointsMoving[1] = 0;
    model.jointsMoving[2] = 0;
  }
  
  for (int idx = 0; idx < armModel.mvLinear.length; ++idx) {
    armModel.mvLinear[idx] = 0;
  }
  
  for (int idx = 0; idx < armModel.mvRot.length; ++idx) {
    armModel.mvRot[idx] = 0;
  }
  
  // Reset button highlighting
  for (int j = 1; j <= 6; ++j) {
    ((Button)cp5.get("JOINT" + j + "_NEG")).setColorBackground(COLOR_DEFAULT);
    ((Button)cp5.get("JOINT" + j + "_POS")).setColorBackground(COLOR_DEFAULT);
  }
}

public void fd() {
  if (shift == ON) {
    currentProgram = programs.get(active_program);
    if (step == OFF){
      currentInstruction = 0;
      executingInstruction = false;
      execSingleInst = false;
      doneMoving = false;
    }
    else {
      currentInstruction = active_instruction;
      executingInstruction = false;
      execSingleInst = true;
      doneMoving = false;
      //Instruction ins = programs.get(active_program).getInstructions().get(active_instruction);
      //if (ins instanceof MotionInstruction) {
      //  singleInstruction = (MotionInstruction)ins;
      //  setUpInstruction(programs.get(active_program), armModel, singleInstruction);
      
      //  if (active_instruction < programs.get(active_program).getInstructions().size()-1){
      //    active_instruction = (active_instruction+1);
      //  }
      
      //  loadInstructions(active_program);
      //  updateScreen(color(255,0,0), color(0));
      //}
    }
  }
  //shift = OFF;
}

public void bd(){
  
  if (shift == ON && step == ON && active_instruction > 0) {
    
    currentProgram = programs.get(active_program);
    Instruction ins = programs.get(active_program).getInstructions().get(active_instruction - 1);
    
    if (ins instanceof MotionInstruction) {
      
      singleInstruction = (MotionInstruction)ins;
      setUpInstruction(programs.get(active_program), armModel, singleInstruction);
      
      if (active_instruction > 0)
        active_instruction = (active_instruction-1);
      
      loadInstructions(active_program);
      updateScreen(color(255,0,0), color(0));
    }
  }
}

public void ENTER(){
  switch (mode){
    case NONE:
       break;
    case PROGRAM_NAV:
       active_instruction = 0;
       text_render_start = 0;
       active_row = 0;
       active_col = 0;
       mode = INSTRUCTION_NAV;
       clearScreen();
       loadInstructions(active_program);
       updateScreen(color(255,0,0), color(0,0,0));
       break;
    case INSTRUCTION_NAV:
       if (active_col == 2 || active_col == 3){
          mode = INSTRUCTION_EDIT;
          NUM_MODE = ON;
          num_info.setText(" ");
       }
       break;
    case INSTRUCTION_EDIT:
       MotionInstruction m = getActiveMotionInstruct();
       switch (active_col){
          case 2: // motion type
             if (which_option == 0){
                if (m.getMotionType() != MTYPE_JOINT) m.setSpeed(m.getSpeed()/armModel.motorSpeed);
                m.setMotionType(MTYPE_JOINT);
             }else if (which_option == 1){
               if (m.getMotionType() == MTYPE_JOINT) m.setSpeed(armModel.motorSpeed*m.getSpeed());
                m.setMotionType(MTYPE_LINEAR);
             }else if(which_option == 2){
               if (m.getMotionType() == MTYPE_JOINT) m.setSpeed(armModel.motorSpeed*m.getSpeed());
                m.setMotionType(MTYPE_CIRCULAR);
             }
             break;
          case 3: // register type
             if (which_option == 0) {
               m.setGlobal(false);
             } else if (which_option == 1) {
               
               if (POS_REG[m.register].point == null) {
                 // Invalid register index
                 options = new ArrayList<String>();
                 options.add("This register is uninitailized!");
                 which_option = 0;
                 return;
               } else {
                 m.setGlobal(true);
               } 
             }
             break;
          case 4: // register
             break;
          case 5: // speed
             break;
          case 6: // termination type
             break;   
       }
       loadInstructions(active_program);
       mode = INSTRUCTION_NAV;
       NUM_MODE = OFF;
       options = new ArrayList<String>();
       which_option = -1;
       clearOptions();
       nums = new ArrayList<Integer>();
       clearNums();
       updateScreen(color(255,0,0), color(0,0,0));
       break;
    case SET_INSTRUCTION_SPEED:
       float tempSpeed = Float.parseFloat(workingText);
       if (tempSpeed >= 5.0) {
         if (speedInPercentage) {
           if (tempSpeed > 100) tempSpeed = 10; 
           tempSpeed /= 100.0;
         } else if (tempSpeed > armModel.motorSpeed) {
           tempSpeed = armModel.motorSpeed;
         }
         MotionInstruction castIns = getActiveMotionInstruct();
         castIns.setSpeed(tempSpeed);
         saveProgramBytes( new File(sketchPath("tmp/programs.bin")) );
       }
       loadInstructions(active_program);
       mode = INSTRUCTION_NAV;
       options = new ArrayList<String>();
       which_option = -1;
       clearOptions();
       updateScreen(color(255,0,0), color(0,0,0));
       break;
    case SET_INSTRUCTION_REGISTER:
       try {
         int tempRegister = Integer.parseInt(workingText);
         if (tempRegister >= 0 && tempRegister < POS_REG.length) {
           
           if (POS_REG[tempRegister].point == null) {
             // Invalid register index
             options = new ArrayList<String>();
             options.add("This register is uninitailized!");
             which_option = 0;
             return;
           }
           
           MotionInstruction castIns = getActiveMotionInstruct();
           castIns.setRegister(tempRegister);
         }
       } catch (NumberFormatException NFEx){ /* Ignore invalid numbers */ }
       
       loadInstructions(active_program);
       mode = INSTRUCTION_NAV;
       options = new ArrayList<String>();
       which_option = -1;
       clearOptions();
       updateScreen(color(255,0,0), color(0,0,0));
       break;
    case SET_INSTRUCTION_TERMINATION:
       float tempTerm = Float.parseFloat(workingText);
       if (tempTerm > 0.0) {
         tempTerm /= 100.0;
         MotionInstruction castIns = getActiveMotionInstruct();
         castIns.setTermination(tempTerm);
       }
       
       loadInstructions(active_program);
       mode = INSTRUCTION_NAV;
       options = new ArrayList<String>();
       which_option = -1;
       clearOptions();
       updateScreen(color(255,0,0), color(0,0,0));
       break;
    case JUMP_TO_LINE:
       active_instruction = Integer.parseInt(workingText)-1;
       if (active_instruction < 0) active_instruction = 0;
       if (active_instruction >= programs.get(active_program).getInstructions().size())
         active_instruction = programs.get(active_program).getInstructions().size()-1;
       mode = INSTRUCTION_NAV;
       options = new ArrayList<String>();
       which_option = -1;
       clearOptions();
       loadInstructions(active_program);
       updateScreen(color(255,0,0), color(0,0,0));
       break;
    case VIEW_REGISTER:
       mode = INSTRUCTION_NAV;
       options = new ArrayList<String>();
       which_option = -1;
       clearOptions();
       loadInstructions(active_program);
       updateScreen(color(255,0,0), color(0,0,0));
       break;
    case ENTER_TEXT:
       if (workingText.length() > 0) {
         int new_prog = addProgram(new Program(workingText));
         workingText = "";
         active_program = new_prog;
         active_instruction = 0;
         active_row = 0;
         active_col = 0;
         mode = INSTRUCTION_NAV;
         super_mode = NONE;
         clearScreen();
         options = new ArrayList<String>();
         loadInstructions(active_program);
         updateScreen(color(255,0,0), color(0,0,0));
       } else {
         mode = super_mode;
         super_mode = NONE;
         active_row = 0;
         active_col = 0;
         clearScreen();
         options = new ArrayList<String>();
         loadPrograms();
         updateScreen(color(255,0,0), color(0,0,0));
       }
       
       break;
    case SETUP_NAV:
       options = new ArrayList<String>();
       options.add("1.Tool Frame");
       options.add("2.User Frame");
       //options.add("3.Jog Frame");
       mode = PICK_FRAME_MODE;
       which_option = 0;
       updateScreen(color(255,0,0), color(0));
       break;
    case PICK_FRAME_MODE:
       options = new ArrayList<String>();
       clearOptions();
       
       if (which_option == 0) {
         loadFrames(COORD_TOOL);
       } else if (which_option == 1) {
         loadFrames(COORD_USER);
       } // Jog Frame not implemented
       
       which_option = -1;
       break;
    case PICK_FRAME_METHOD:
       if (which_option == 0) {
         which_option = 0;
         teachPointTMatrices = new ArrayList<float[][]>();
         loadFrameDetails();
         mode = THREE_POINT_MODE;
         loadPointList();
       } else if (which_option == 1) {
         which_option = 0;
         teachPointTMatrices = new ArrayList<float[][]>();
         loadFrameDetails();
         mode = (super_mode == NAV_TOOL_FRAMES) ? SIX_POINT_MODE : FOUR_POINT_MODE;
         loadPointList();
       } else if (which_option == 2) {
         options = new ArrayList<String>();
         which_option = -1;
         loadDirectEntryMethod();
       }
       break;
    case IO_SUBMENU:
       if (active_row == 2) { // digital
          options = new ArrayList<String>();
          options.add("Use number keys to enter DO[X]");
          workingText = "";
          options.add(workingText);
          mode = SET_DO_BRACKET;
          which_option = 0;
          updateScreen(color(255,0,0), color(0));
       } else if (active_row == 5) { // robot
          options = new ArrayList<String>();
          options.add("Use number keys to enter RO[X]");
          workingText = "";
          options.add(workingText);
          mode = SET_RO_BRACKET;
          which_option = 0;
          updateScreen(color(255,0,0), color(0));
       }
       break;
    case SET_DO_BRACKET:
    case SET_RO_BRACKET:
       options = new ArrayList<String>();
       options.add("ON");
       options.add("OFF");
       if (mode == SET_DO_BRACKET) mode = SET_DO_STATUS;
       else if (mode == SET_RO_BRACKET) mode = SET_RO_STATUS;
       which_option = 0;
       updateScreen(color(255,0,0), color(0));
       break;
    case SET_DO_STATUS:
    case SET_RO_STATUS:
       Program prog = programs.get(active_program);
       
       try {
         int bracketNum = Integer.parseInt(workingText);
         if (bracketNum >= 0) {
           ToolInstruction insert = new ToolInstruction(
              (mode == SET_DO_STATUS ? "DO" : "RO"),
              bracketNum,
              (which_option == 0 ? ON : OFF));
           prog.addInstruction(insert);
         }
       } catch (NumberFormatException NFEx) { /* Ignore invalid numbers */ }
       
       active_instruction = prog.getInstructions().size() - 1;
       active_col = 0;
       /* 13 is the maximum number of instructions that can be displayed at one point in time */
       active_row = min(active_instruction, ITEMS_TO_SHOW - 1);
       text_render_start = active_instruction - active_row;
       
       loadInstructions(active_program);
       active_row = contents.size()-1;
       mode = INSTRUCTION_NAV;
       options.clear();
       updateScreen(color(255,0,0), color(0,0,0));
       break;
    case SET_FRAME_INSTRUCTION:
        options = new ArrayList<String>();
        options.add("Select the index of the frame to use");
        workingText = "";
        options.add(workingText);
        
        which_option = 0;
        mode = SET_FRAME_INSTRUCTION_IDX;
        updateScreen(color(255, 0, 0), color(0));
        break;
    case SET_FRAME_INSTRUCTION_IDX:
       prog = programs.get(active_program);
       
       try {
         int num = Integer.parseInt(workingText)-1;
         if (num < -1) num = -1;
         else if (num >= userFrames.length) num = userFrames.length-1;
        
         int type = 0;
         if (active_row == 0) type = FTYPE_TOOL;
         else if (active_row == 1) type = FTYPE_USER;
         prog.addInstruction(new FrameInstruction(type, num));
      } catch (NumberFormatException NFEx) { /* Ignore invalid numbers */ }
       
      active_instruction = prog.getInstructions().size() - 1;
      active_col = 0;
      /* 13 is the maximum number of instructions that can be displayed at one point in time */
      active_row = min(active_instruction, ITEMS_TO_SHOW - 1);
      text_render_start = active_instruction - active_row;
      
      loadInstructions(active_program);
      mode = INSTRUCTION_NAV;
      which_option = -1;
      options.clear();
      updateScreen(color(255,0,0), color(0,0,0));
      break;
    case EDIT_MENU:
      if (active_row == 1) { // delete
         options = new ArrayList<String>();
         options.add("Delete this line? F4 = YES, F5 = NO");
         super_mode = INSTRUCTION_NAV;
         mode = CONFIRM_DELETE;
         which_option = 0;
         updateScreen(color(255,0,0), color(0,0,0));
      }
      break;
      
    case THREE_POINT_MODE:
    case FOUR_POINT_MODE:
    case SIX_POINT_MODE:
    
      if ((mode == THREE_POINT_MODE && teachPointTMatrices.size() == 3) ||
        (mode == FOUR_POINT_MODE && teachPointTMatrices.size() == 4) ||
        (mode == SIX_POINT_MODE && teachPointTMatrices.size() == 6)) {
  
        PVector origin = new PVector(0f, 0f, 0f), wpr = new PVector(0f, 0f, 0f);
        float[][] axes = new float[3][3];
        // Create identity matrix
        for (int diag = 0; diag < 3; ++diag) {
          axes[diag][diag] = 1f;
        }
        
        if (super_mode == NAV_TOOL_FRAMES && (mode == THREE_POINT_MODE || mode == SIX_POINT_MODE)) {
          // Calculate TCP via the 3-Point Method
          double[] tcp = calculateTCPFromThreePoints(teachPointTMatrices);
          
          if (tcp == null) {
            // Invalid point set
            loadFrameDetails();
            
            which_option = 0;
            options.add("Error: Invalid input values!");
            updateScreen(color(255, 0, 0), color(0));
        
            return;
          } else {
            origin = new PVector((float)tcp[0], (float)tcp[1], (float)tcp[2]);
          }
        } else if (mode == FOUR_POINT_MODE) {
          // Origin offset for the user frame
          origin = new PVector(teachPointTMatrices.get(3)[0][3], teachPointTMatrices.get(3)[1][3], teachPointTMatrices.get(3)[2][3]);
        }
        
        if (super_mode == NAV_USER_FRAMES || mode == SIX_POINT_MODE) {
          
          ArrayList<float[][]> axesPoints = new ArrayList<float[][]>();
          // Use the last three points to calculate the axes vectors
          if (mode == SIX_POINT_MODE) {
            axesPoints.add(teachPointTMatrices.get(3));
            axesPoints.add(teachPointTMatrices.get(4));
            axesPoints.add(teachPointTMatrices.get(5));
          } else {
            axesPoints.add(teachPointTMatrices.get(0));
            axesPoints.add(teachPointTMatrices.get(1));
            axesPoints.add(teachPointTMatrices.get(2));
          } 
          
          axes = createAxesFromThreePoints(axesPoints);
          
          if (axes == null) {
            // Invalid point set
            loadFrameDetails();
            
            which_option = 0;
            options.add("Error: Invalid input values!");
            updateScreen(color(255, 0, 0), color(0));
            return;
          }
          
          wpr = matrixToEuler(axes);
          
          if (DISPLAY_TEST_OUTPUT) { println(matrixToString(axes)); }
        }
          
        Frame[] frames = null;
        // Determine to which frame set (user or tool) to add the new frame
        if (super_mode == NAV_TOOL_FRAMES) {
          frames = toolFrames;
        } else if (super_mode == NAV_USER_FRAMES) {
          frames = userFrames;
        }
        
        if (frames != null) {
          
          if (curFrameIdx >= 0 && curFrameIdx < frames.length) {
            if (DISPLAY_TEST_OUTPUT) { System.out.printf("Frame set: %d\n", curFrameIdx); }
            
            frames[curFrameIdx] = new Frame();
            frames[curFrameIdx].setOrigin(origin);
            frames[curFrameIdx].setWpr(wpr);
            frames[curFrameIdx].setAxes(axes);
            saveFrameBytes( new File(sketchPath("tmp/frames.bin")) );
            
            // Set new Frame 
            if (super_mode == NAV_TOOL_FRAMES) {
              // Update the current frame of the Robot Arm
              activeToolFrame = curFrameIdx;
              armModel.currentFrame = userFrames[curFrameIdx].getNativeAxes();
            } else if (super_mode == NAV_USER_FRAMES) {
              // Update the current frame of the Robot Arm
              activeUserFrame = curFrameIdx;
              armModel.currentFrame = userFrames[curFrameIdx].getNativeAxes();
            }
          } else {
            System.out.printf("Error invalid index %d!\n", curFrameIdx);
          }
          
        } else {
          System.out.printf("Error: invalid frame list for mode: %d!\n", mode);
        }
        
        teachPointTMatrices = null;
        which_option = 0;
        options.clear();
        active_row = 0;
        
        if (super_mode == NAV_TOOL_FRAMES) {
          loadFrames(COORD_TOOL);
        } else if (super_mode == NAV_USER_FRAMES) {
          loadFrames(COORD_USER);
        } else {
          super_mode = MENU_NAV;
          mu();
        }
        
        mode = super_mode;
        super_mode = NONE;
        options.clear();
      }
  
      break;
    case DIRECT_ENTRY_MODE:
    
      options = new ArrayList<String>();
      which_option = -1;
      
      boolean error = false;
      // User defined x, y, z, w, p, and r values
      float[] inputs = new float[] { 0f, 0f, 0f, 0f, 0f, 0f };
      
      try {
        // Parse each input value
        for (int val = 0; val < inputs.length; ++val) {
          String str = contents.get(val + 1).get(0);
          
          if (str.length() < 4) {
            // No value entered
            error = true;
            options.add("All entries must have a value.");
            break;
          }
          
          // Remove prefix
          inputs[val] = Float.parseFloat(str.substring(3));
        }
      
      } catch (NumberFormatException NFEx) {
        // Invalid number
        error = true;
        options.add("Inputs must be real numbers.");
      }
      
      if (error) {
        which_option = 0;
        updateScreen(color(255, 0, 0) , color(0));
      } else {
        PVector origin = new PVector(inputs[0], inputs[1], inputs[2]),
                wpr = new PVector(inputs[3], inputs[4], inputs[5]);
        float[][] axesVectors = eulerToMatrix(wpr);
        
        origin.x = max(-9999f, min(origin.x, 9999f));
        origin.y = max(-9999f, min(origin.y, 9999f));
        origin.z = max(-9999f, min(origin.z, 9999f));
        wpr = matrixToEuler(axesVectors);
        
        if (DISPLAY_TEST_OUTPUT) { System.out.printf("\n\n%s\n%s\n%s\n", origin.toString(), wpr.toString(), matrixToString(axesVectors)); }
        
        Frame[] frames = null;
        // Determine to which frame set (user or tool) to add the new frame
        if (super_mode == NAV_TOOL_FRAMES) {
          frames = toolFrames;
        } else if (super_mode == NAV_USER_FRAMES) {
          frames = userFrames;
        }
      
        // Create axes vector and save the new frame
        if (frames != null && curFrameIdx >= 0 && curFrameIdx < frames.length) {
          if (DISPLAY_TEST_OUTPUT) { System.out.printf("Frame set: %d\n", curFrameIdx); }
          
          frames[curFrameIdx] = new Frame(origin, wpr, axesVectors);
          saveFrameBytes( new File(sketchPath("tmp/frames.bin")) );
          
          // Set New Frame
          if (super_mode == NAV_TOOL_FRAMES) {
            // Update the current frame of the Robot Arm
            activeToolFrame = curFrameIdx;
            armModel.currentFrame = userFrames[curFrameIdx].getNativeAxes();
          } else if (super_mode == NAV_USER_FRAMES) {
            // Update the current frame of the Robot Arm
            activeUserFrame = curFrameIdx;
            armModel.currentFrame = userFrames[curFrameIdx].getNativeAxes();
          }
          
          if (super_mode == NAV_TOOL_FRAMES) {
            loadFrames(COORD_TOOL);
          } else if (super_mode == NAV_USER_FRAMES) {
            loadFrames(COORD_USER);
          } else {
            super_mode = MENU_NAV;
            mu();
          }
          
          mode = super_mode;
          super_mode = NONE;
          options.clear();
        }
      }
      
      break;
    case PICK_REG_LIST:
      int modeCase = 0;
      
      if (super_mode == VIEW_REG) {
        modeCase = 1;
      } else if (super_mode == VIEW_POS_REG_J) {
        modeCase = 2;
      } else if (super_mode == VIEW_POS_REG_C) {
        modeCase = 3;
      }
      
      if (modeCase != 1 && which_option == 0) {
        // Register Menu
        mode = VIEW_REG;
      } else if ((modeCase == 1 && which_option == 0) ||
                 (modeCase != 1 && modeCase != 2 && which_option == 1)) {
        // Position Register Menu (in Joint mode)
        mode = VIEW_POS_REG_J;
      } else if ((modeCase == 0 && which_option == 2) ||
                 (modeCase != 0 && modeCase != 3 && which_option == 1)) {
        // Position Register Menu (in Cartesian mode)
        mode = VIEW_POS_REG_C;
      } else {
        mu();
      }
      
      active_row = 2;
      active_col = active_index = text_render_start = 0;
      super_mode = NONE;
      viewRegisters();
      
      break;
    case INPUT_FLOAT:
      
      Float input = null;
      
      try {
        // Read inputted Float value
        input = Float.parseFloat(workingText);
        // Clamp the value between -9999 and 9999, inclusive
        input = max(-9999f, min(input, 9999f));
      } catch (NumberFormatException NFEx) {
        // Invalid input value
        options = new ArrayList<String>();
        options.add("Only real numbers are acceptable input!");
        which_option = 0;
        
        mode = super_mode;
        super_mode = NONE;
        updateScreen(color(255, 0, 0), color(0));
        return;
      }
      
      if (active_index >= 0 && active_index < REG.length) {
          // Save inputted value
          REG[active_index].value = input;
          saveRegisterBytes( new File(sketchPath("tmp/registers.bin")) );
      }
    
      mode = super_mode;
      super_mode = NONE;
      viewRegisters();
      
      break;
    case INPUT_POINT_C:
    case INPUT_POINT_J:
      // TODO save inputted values
      
      break;
    case INPUT_COMMENT_U:
    case INPUT_COMMENT_L:
      
      if (workingText.charAt(  workingText.length() - 1  ) == '\0') {
        workingText = workingText.substring(0, workingText.length() - 1);
      }
      // Save the inputted comment to the selected register
      if (super_mode == VIEW_REG) {
        REG[active_index].comment = workingText;
      } else if (super_mode == VIEW_POS_REG_J || mode == VIEW_POS_REG_C) {
        POS_REG[active_index].comment = workingText;
      } else {
        // Invalid envocation of the INPUT_COMMENT_* modes
        super_mode = NONE;
        mu();
        return;
      }
      
      workingText = null;
      mode = super_mode;
      super_mode = NONE;
      active_row = 2;
      active_col = 0;
      text_render_start = active_index;
      viewRegisters();
      saveRegisterBytes( new File(sketchPath("tmp/registers.bin")) );
      
      break;
  }
  println(mode);
}

public void ITEM() {
  if (mode == INSTRUCTION_NAV) {
    options = new ArrayList<String>();
    options.add("Use number keys to enter line number to jump to");
    workingText = "";
    options.add(workingText);
    mode = JUMP_TO_LINE;
    which_option = 0;
    updateScreen(color(255,0,0), color(0,0,0));
  }
}

public void LEFT() {
  if (mode == INPUT_FLOAT) {
    // Functions as a backspace key
    if (workingText.length() > 1) {
      workingText = workingText.substring(0, workingText.length() - 1);
    } else {
      workingText = "";
    }
    
    options.set(2, workingText);
  } else if (mode == DIRECT_ENTRY_MODE || mode == INPUT_POINT_J || mode == INPUT_POINT_C) {
    
    // backspace function for current row
    if (active_row >= 0 && active_row < contents.size()) {
      String line = contents.get(active_row).get(0);
      
      // Do not remove line prefix
      if (line.length() > which_option) {
        contents.get(active_row).set(0, line.substring(0, line.length() - 1));
      }
    }
  } else if (mode == INPUT_COMMENT_U || mode == INPUT_COMMENT_L) {
    // Backspace function
    if (workingText.length() > 1) {
      // If an insert space exists, preserve it
      if (workingText.charAt(workingText.length() - 1) == '\0') {
        workingText = workingText.substring(0, workingText.length() - 2) + "\0";
      } else {
        workingText = workingText.substring(0, workingText.length() - 1);
      }
      
      updateComment();
      active_col = min(active_col, contents.get(active_row).size() - 1);
    }
    
    for (int idx = 0; idx < letterStates.length; ++idx) { letterStates[idx] = 0; }
  }
  
  updateScreen(color(255, 0, 0), color(0));
}


public void COORD() {
  if (shift == ON) {
    // Show frame indices in the pendant window
    active_row = 1;
    active_col = 0;
    workingText = "";
    loadActiveFrames();
  } else {  
    // Update the coordinate mode
    updateCoordinateMode(armModel);
    liveSpeed = 0.1;
  }
}

public void SPEEDUP() {
  if (liveSpeed == 0.01) liveSpeed += 0.04; 
  else if (liveSpeed < 0.5) liveSpeed += 0.05;
  else if (liveSpeed < 1) liveSpeed += 0.1;
  if (liveSpeed > 1) liveSpeed = 1;
}


public void SLOWDOWN() {
  if (liveSpeed > 0.5) liveSpeed -= 0.1;
  else if (liveSpeed > 0) liveSpeed -= 0.05;
  if (liveSpeed < 0.01) liveSpeed = 0.01;
}


/* navigation buttons */
// zoomin button when interface is at full size
public void zoomin_normal(){
   myscale *= 1.1;
}

// zoomin button when interface is minimized
public void zoomin_shrink(){
   zoomin_normal();
}

// zoomout button when interface is at full size
public void zoomout_normal(){
   myscale *= 0.9;
}

// zoomout button when interface is minimized
public void zoomout_shrink(){
   zoomout_normal();
}

// pan button when interface is at full size
public void pan_normal(){
  clickPan += 1;
  if ((clickPan % 2) == 1){
     if((clickRotate % 2) == 1){
       rotate_normal();
     }
     
     cursorMode = HAND;
     PImage[] pressed = {loadImage("images/pan_down.png"), 
                         loadImage("images/pan_down.png"), 
                         loadImage("images/pan_down.png")};
                         
     cp5.getController("pan_normal")
        .setImages(pressed);
  }
  else{
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
public void pan_shrink(){
  pan_normal();
}

// rotate button when interface is at full size
public void rotate_normal(){
   clickRotate += 1;
   if ((clickRotate % 2) == 1){
     if((clickPan % 2) == 1){
       pan_normal();
     }
     
     cursorMode = MOVE;
     PImage[] pressed = {loadImage("images/rotate_down.png"), 
                         loadImage("images/rotate_down.png"), 
                         loadImage("images/rotate_down.png")};
                         
     cp5.getController("rotate_normal")
        .setImages(pressed);
  }
  else{
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
public void rotate_shrink(){
  rotate_normal();
}

public void record_normal(){
   if (record == OFF){
      record = ON;
      PImage[] record = {loadImage("images/record-on.png"), 
                         loadImage("images/record-on.png"),
                         loadImage("images/record-on.png")};   
      bt_record_normal.setImages(record);
      new Thread(new RecordScreen()).start();
   }else{
      record = OFF;
      PImage[] record = {loadImage("images/record-35x20.png"), 
                         loadImage("images/record-over.png"), 
                         loadImage("images/record-on.png")};   
      bt_record_normal.setImages(record);
      
   }
}

public void EE(){
  armModel.activeEndEffector++;
  if (armModel.activeEndEffector > ENDEF_CLAW) armModel.activeEndEffector = 0;
  // Drop an object if held by the Robot currently
  armModel.releaseHeldObject();
  // TODO collision checking if an object was held by the Robot
}

/**
 * Updates the motion of one of the Robot's joints based on
 * the joint index given and the value of dir (-/+ 1). The
 * Robot's joint indices range from 0 to 5. If the joint
 * Associate with the given index is already in motion,
 * in either direction, then calling this method for that
 * joint index will stop that joint's motion.
 */
public void activateLiveJointMotion(int joint, int dir) {
  
  if (armModel.segments.size() >= joint+1) {

    Model model = armModel.segments.get(joint);
    // Checks all rotational axes
    for (int n = 0; n < 3; n++) {
    
      if (model.rotations[n]) {
      
        if (model.jointsMoving[n] == 0) {
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
  
  if (axis >= 0 && axis < 3) {
    if (armModel.mvLinear[axis] == 0) {
      //Begin movement on the given axis in the given direction
      armModel.mvLinear[axis] = dir;
    } else {
      //Halt movement
      armModel.mvLinear[axis] = 0;
    }
  }
  else if(axis >= 3 && axis < 6){
    axis -= 3;
    if(armModel.mvRot[axis] == 0){
      armModel.mvRot[axis] = dir;
    }
    else{
      armModel.mvRot[axis] = 0;
    }
  }
}

public void JOINT1_NEG() {
  
  if (curCoordFrame == COORD_JOINT) {
    // Move single joint
    activateLiveJointMotion(0, -1);
  } else {
    // Move entire robot in a single axis plane
    activateLiveWorldMotion(0, 1);
  }
  
  int c1 = ((Button)cp5.get("JOINT1_NEG")).getColor().getBackground();
  int c2 = ((Button)cp5.get("JOINT1_POS")).getColor().getBackground();
  
  if(c1 == COLOR_DEFAULT && c2 == COLOR_DEFAULT){
    //both buttons have the default color, set this one to highlight
    ((Button)cp5.get("JOINT1_NEG")).setColorBackground(COLOR_ACTIVE);
  }
  else{
    ((Button)cp5.get("JOINT1_NEG")).setColorBackground(COLOR_DEFAULT);
    ((Button)cp5.get("JOINT1_POS")).setColorBackground(COLOR_DEFAULT);
  }
}

public void JOINT1_POS() {
  
  if (curCoordFrame == COORD_JOINT) {
    // Move single joint
    activateLiveJointMotion(0, 1);
  } else  {
    // Move entire robot in a single axis plane
    activateLiveWorldMotion(0, -1);
  }
  
  int c1 = ((Button)cp5.get("JOINT1_NEG")).getColor().getBackground();
  int c2 = ((Button)cp5.get("JOINT1_POS")).getColor().getBackground();
  
  if(c1 == COLOR_DEFAULT && c2 == COLOR_DEFAULT){
    //both buttons have the default color, set this one to highlight
    ((Button)cp5.get("JOINT1_POS")).setColorBackground(COLOR_ACTIVE);
  }
  else{
    //stopping movement, set both buttons to default
    ((Button)cp5.get("JOINT1_NEG")).setColorBackground(COLOR_DEFAULT);
    ((Button)cp5.get("JOINT1_POS")).setColorBackground(COLOR_DEFAULT);
  }
}

public void JOINT2_NEG() {
  
  if (curCoordFrame == COORD_JOINT) {
    // Move single joint
    activateLiveJointMotion(1, -1);
  } else  {
    // Move entire robot in a single axis plane
    activateLiveWorldMotion(2, -1);
  }
  
  int c1 = ((Button)cp5.get("JOINT2_NEG")).getColor().getBackground();
  int c2 = ((Button)cp5.get("JOINT2_POS")).getColor().getBackground();
  
  if(c1 == COLOR_DEFAULT && c2 == COLOR_DEFAULT){
    //both buttons have the default color, set this one to highlight
    ((Button)cp5.get("JOINT2_NEG")).setColorBackground(COLOR_ACTIVE);
  }
  else{
    ((Button)cp5.get("JOINT2_NEG")).setColorBackground(COLOR_DEFAULT);
    ((Button)cp5.get("JOINT2_POS")).setColorBackground(COLOR_DEFAULT);
  }
}

public void JOINT2_POS() {
  
  if (curCoordFrame == COORD_JOINT) {
    // Move single joint
    activateLiveJointMotion(1, 1);
  } else  {
    // Move entire robot in a single axis plane
    activateLiveWorldMotion(2, 1);
  }
  
  int c1 = ((Button)cp5.get("JOINT2_NEG")).getColor().getBackground();
  int c2 = ((Button)cp5.get("JOINT2_POS")).getColor().getBackground();
  
  if(c1 == COLOR_DEFAULT && c2 == COLOR_DEFAULT){
    //both buttons have the default color, set this one to highlight
    ((Button)cp5.get("JOINT2_POS")).setColorBackground(COLOR_ACTIVE);
  }
  else{
    ((Button)cp5.get("JOINT2_NEG")).setColorBackground(COLOR_DEFAULT);
    ((Button)cp5.get("JOINT2_POS")).setColorBackground(COLOR_DEFAULT);
  }
}

public void JOINT3_NEG() {
  
  if (curCoordFrame == COORD_JOINT) {
    // Move single joint
    activateLiveJointMotion(2, -1);
  } else  {
    // Move entire robot in a single axis plane
    activateLiveWorldMotion(1, 1);
  }
  
  int c1 = ((Button)cp5.get("JOINT3_NEG")).getColor().getBackground();
  int c2 = ((Button)cp5.get("JOINT3_POS")).getColor().getBackground();
  
  if(c1 == COLOR_DEFAULT && c2 == COLOR_DEFAULT){
    //both buttons have the default color, set this one to highlight
    ((Button)cp5.get("JOINT3_NEG")).setColorBackground(COLOR_ACTIVE);
  }
  else{
    ((Button)cp5.get("JOINT3_NEG")).setColorBackground(COLOR_DEFAULT);
    ((Button)cp5.get("JOINT3_POS")).setColorBackground(COLOR_DEFAULT);
  }
}

public void JOINT3_POS() {
  
  if (curCoordFrame == COORD_JOINT) {
    // Move single joint
    activateLiveJointMotion(2, 1);
  } else  {
    // Move entire robot in a single axis plane
    activateLiveWorldMotion(1, -1);
  }
  
  int c1 = ((Button)cp5.get("JOINT3_NEG")).getColor().getBackground();
  int c2 = ((Button)cp5.get("JOINT3_POS")).getColor().getBackground();
  
  if(c1 == COLOR_DEFAULT && c2 == COLOR_DEFAULT){
    //both buttons have the default color, set this one to highlight
    ((Button)cp5.get("JOINT3_POS")).setColorBackground(COLOR_ACTIVE);
  }
  else{
    ((Button)cp5.get("JOINT3_NEG")).setColorBackground(COLOR_DEFAULT);
    ((Button)cp5.get("JOINT3_POS")).setColorBackground(COLOR_DEFAULT);
  }
}

public void JOINT4_NEG() {
  
  if (curCoordFrame == COORD_JOINT) {
    // Move single joint
    activateLiveJointMotion(3, -1);
  } else  {
    // Move entire robot in a single axis plane
    activateLiveWorldMotion(3, -1);
  }
  
  int c1 = ((Button)cp5.get("JOINT4_NEG")).getColor().getBackground();
  int c2 = ((Button)cp5.get("JOINT4_POS")).getColor().getBackground();
  
  if(c1 == COLOR_DEFAULT && c2 == COLOR_DEFAULT){
    //both buttons have the default color, set this one to highlight
    ((Button)cp5.get("JOINT4_NEG")).setColorBackground(COLOR_ACTIVE);
  }
  else {
    ((Button)cp5.get("JOINT4_NEG")).setColorBackground(COLOR_DEFAULT);
    ((Button)cp5.get("JOINT4_POS")).setColorBackground(COLOR_DEFAULT);
  }
}

public void JOINT4_POS() {
  
  if (curCoordFrame == COORD_JOINT) {
    // Move single joint
    activateLiveJointMotion(3, 1);
  } else {
    // Move entire robot in a single axis plane
    activateLiveWorldMotion(3, 1);
  }
  
  int c1 = ((Button)cp5.get("JOINT4_NEG")).getColor().getBackground();
  int c2 = ((Button)cp5.get("JOINT4_POS")).getColor().getBackground();
  
  if(c1 == COLOR_DEFAULT && c2 == COLOR_DEFAULT){
    //both buttons have the default color, set this one to highlight
    ((Button)cp5.get("JOINT4_POS")).setColorBackground(COLOR_ACTIVE);
  }
  else{
    ((Button)cp5.get("JOINT4_NEG")).setColorBackground(COLOR_DEFAULT);
    ((Button)cp5.get("JOINT4_POS")).setColorBackground(COLOR_DEFAULT);
  }
}

public void JOINT5_NEG() {
  
  if (curCoordFrame == COORD_JOINT) {
    // Move single joint
    activateLiveJointMotion(4, -1);
  } else {
    // Move entire robot in a single axis plane
    activateLiveWorldMotion(5, -1);
  }
  
  int c1 = ((Button)cp5.get("JOINT5_NEG")).getColor().getBackground();
  int c2 = ((Button)cp5.get("JOINT5_POS")).getColor().getBackground();
  
  if(c1 == COLOR_DEFAULT && c2 == COLOR_DEFAULT){
    //both buttons have the default color, set this one to highlight
    ((Button)cp5.get("JOINT5_NEG")).setColorBackground(COLOR_ACTIVE);
  }
  else{
    ((Button)cp5.get("JOINT5_NEG")).setColorBackground(COLOR_DEFAULT);
    ((Button)cp5.get("JOINT5_POS")).setColorBackground(COLOR_DEFAULT);
  }
}

public void JOINT5_POS() {
  
  if (curCoordFrame == COORD_JOINT) {
    // Move single joint
    activateLiveJointMotion(4, 1);
  } else {
    // Move entire robot in a single axis plane
    activateLiveWorldMotion(5, 1);
  }
  
  int c1 = ((Button)cp5.get("JOINT5_NEG")).getColor().getBackground();
  int c2 = ((Button)cp5.get("JOINT5_POS")).getColor().getBackground();
  
  if(c1 == COLOR_DEFAULT && c2 == COLOR_DEFAULT){
    //both buttons have the default color, set this one to highlight
    ((Button)cp5.get("JOINT5_POS")).setColorBackground(COLOR_ACTIVE);
  }
  else{
    ((Button)cp5.get("JOINT5_NEG")).setColorBackground(COLOR_DEFAULT);
    ((Button)cp5.get("JOINT5_POS")).setColorBackground(COLOR_DEFAULT);
  }
}

public void JOINT6_NEG() {
  
  if (curCoordFrame == COORD_JOINT) {
    // Move single joint
    activateLiveJointMotion(5, -1);
  } else {
    // Move entire robot in a single axis plane
    activateLiveWorldMotion(4, -1);
  }
  
  int c1 = ((Button)cp5.get("JOINT6_NEG")).getColor().getBackground();
  int c2 = ((Button)cp5.get("JOINT6_POS")).getColor().getBackground();
  
  if(c1 == COLOR_DEFAULT && c2 == COLOR_DEFAULT){
    //both buttons have the default color, set this one to highlight
    ((Button)cp5.get("JOINT6_NEG")).setColorBackground(COLOR_ACTIVE);
  }
  else{
    ((Button)cp5.get("JOINT6_NEG")).setColorBackground(COLOR_DEFAULT);
    ((Button)cp5.get("JOINT6_POS")).setColorBackground(COLOR_DEFAULT);
  }
}

public void JOINT6_POS() {
  
  if (curCoordFrame == COORD_JOINT) {
    // Move single joint
    activateLiveJointMotion(5, 1);
  } else {
    // Move entire robot in a single axis plane
    activateLiveWorldMotion(4, 1);
  }
  
  int c1 = ((Button)cp5.get("JOINT6_NEG")).getColor().getBackground();
  int c2 = ((Button)cp5.get("JOINT6_POS")).getColor().getBackground();
  
  if(c1 == COLOR_DEFAULT && c2 == COLOR_DEFAULT){
    //both buttons have the default color, set this one to highlight
    ((Button)cp5.get("JOINT6_POS")).setColorBackground(COLOR_ACTIVE);
  }
  else{
    ((Button)cp5.get("JOINT6_NEG")).setColorBackground(COLOR_DEFAULT);
    ((Button)cp5.get("JOINT6_POS")).setColorBackground(COLOR_DEFAULT);
  }
}

//turn of highlighting on all active movement buttons
public void resetButtonColors(){
  for(int i = 1; i <= 6; i += 1){
    ((Button)cp5.get("JOINT"+i+"_NEG")).setColorBackground(COLOR_DEFAULT);
    ((Button)cp5.get("JOINT"+i+"_POS")).setColorBackground(COLOR_DEFAULT);
  }
}

public void updateButtonColors(){
  for(int i = 0; i < 6; i += 1){
    Model m = armModel.segments.get(i);
    for(int j = 0; j < 3; j += 1){
      if(m.rotations[j] && m.jointsMoving[j] == 0){
        ((Button)cp5.get("JOINT"+(i+1)+"_NEG")).setColorBackground(COLOR_DEFAULT);
        ((Button)cp5.get("JOINT"+(i+1)+"_POS")).setColorBackground(COLOR_DEFAULT);
      }
    }
  }
}

// update what displayed on screen
public void updateScreen(color active, color normal){
   int next_px = display_px;
   int next_py = display_py;
   
   if (cp5.getController("-1") != null) cp5.getController("-1").remove();
   
   // display the name of the program that is being edited 
   switch (mode){
      case INSTRUCTION_NAV:
         cp5.addTextlabel("-1")
            .setText(programs.get(active_program).getName())
            .setPosition(next_px, next_py)
            .setColorValue(normal)
            .show()
            .moveTo(g1)
            ;
         next_px = display_px;
         next_py += 14;
         break;
      case INSTRUCTION_EDIT:
      case SET_INSTRUCTION_SPEED:
      case SET_INSTRUCTION_REGISTER:
      case SET_INSTRUCTION_TERMINATION:
         cp5.addTextlabel("-1")
            .setText(programs.get(active_program).getName()) 
            .setPosition(next_px, next_py)
            .setColorValue(normal)
            .show()
            .moveTo(g1)
            ;
         next_px = display_px;
         next_py += 14;
         break;
   }
   
   // clear main list
   for (int i = 0; i < ITEMS_TO_SHOW*8; i++) {
     if (cp5.getGroup(Integer.toString(i)) != null){
       cp5.getGroup(Integer.toString(i)).remove();
     }
     else if (cp5.getController(Integer.toString(i)) != null){
       cp5.getController(Integer.toString(i)).remove();
     }
   }

   // display the main list on screen
   index_contents = 1;
   for(int i=0;i<contents.size();i++){
     ArrayList<String> temp = contents.get(i);
     if(i == active_row){
       cp5.addTextarea(Integer.toString(index_contents))
         .setText("")
         .setPosition(next_px, next_py)
         .setSize(10, 20)
         .setColorBackground(color(15))
         .hideScrollbar()
         .moveTo(g1)
         ;
     }
     else{
       cp5.addTextarea(Integer.toString(index_contents))
         .setText("")
         .setPosition(next_px, next_py)
         .setSize(10, 20)
         .setColorBackground(color(240))
         .hideScrollbar()
         .moveTo(g1)
         ;
     }
     index_contents++;
     next_px += 10;
     
     for (int j=0;j<temp.size();j++){
       if (i == active_row && j != active_col){
         cp5.addTextarea(Integer.toString(index_contents))
           .setText(temp.get(j))
           .setFont(fnt)
           .setPosition(next_px, next_py)
           .setSize(temp.get(j).length()*8 + 18, 20)
           .setLineHeight(18)
           .setColorValue(color(240))
           .setColorBackground(color(15))
           .hideScrollbar()
           .moveTo(g1)
           ;
       }
       else{
         cp5.addTextarea(Integer.toString(index_contents))
           .setText(temp.get(j))
           .setFont(fnt)
           .setPosition(next_px, next_py)
           .setSize(temp.get(j).length()*8 + 18, 20)
           .setLineHeight(18)
           .setColorValue(color(15))
           .setColorBackground(color(240))
           .setColorValue(normal)
           .hideScrollbar()
           .moveTo(g1)
           ;  
       }
       index_contents++;
       next_px += temp.get(j).length() * 8 + 18; 
     }
     next_px = display_px;
     next_py += 20;
   }
   
   // display options for an element being edited
   next_py += 14;
   index_options = 100;
   if (options.size() > 0){
     for(int i=0;i<options.size();i++){
       if (i==which_option){
         cp5.addTextlabel(Integer.toString(index_options))
           .setText(options.get(i))
           .setPosition(next_px, next_py)
           .setColorValue(active)
           .moveTo(g1)
           ;
       }else{
         cp5.addTextlabel(Integer.toString(index_options))
           .setText(options.get(i))
           .setPosition(next_px, next_py)
           .setColorValue(normal)
           .moveTo(g1)
           ;
       }
        
       index_options++;
       next_px = display_px;
       next_py += 14;    
     }
   }
   
   // display the numbers that the user has typed
   next_py += 14;
   index_nums = 1000;
   if (nums.size() > 0){
      for(int i=0;i<nums.size();i++){
         if (nums.get(i) == -1){
            cp5.addTextlabel(Integer.toString(index_nums))
               .setText(".")
               .setPosition(next_px, next_py)
               .setColorValue(normal)
               .moveTo(g1)
               ;
         }else{
            cp5.addTextlabel(Integer.toString(index_nums))
               .setText(Integer.toString(nums.get(i)))
               .setPosition(next_px, next_py)
               .setColorValue(normal)
               .moveTo(g1)
               ;
         }
         
         index_nums++;
         next_px += 5;   
      }
   }
   
   // display the comment for the user's input
   num_info.setPosition(next_px, next_py)
           .setColorValue(normal) 
           .show()
           ;
   next_px = display_px;
   next_py += 14;   
   
   // display hints for function keys
   next_py += 100;
   String text = null;
   
   if (mode == PROGRAM_NAV) {
     
     text = "F2: CREATE     F3: DELETE";
   } else if (mode == INSTRUCTION_NAV) {
     
     text = "SHIFT+F1: NEW PT     F4: CHOICE     F5: VIEW REG     SHIFT+F5: OVERWRITE";
   } else if (mode == NAV_TOOL_FRAMES || mode == NAV_USER_FRAMES) {
     
     text = "F1: SET     SHIFT+F1: DETAIL     F2: RESET     F3: SWITCH";
   } else if (mode == FRAME_DETAIL) {
     
     text = "F2: METHOD";
   } else if (mode == THREE_POINT_MODE || mode == FOUR_POINT_MODE || mode == SIX_POINT_MODE) {
     
     text = "F1: SAV REF PT     SHIFT+F1: RMV REF PT     SHIFT+F5: RECORD";
   } else if (mode == ACTIVE_FRAMES) {
     
     text = "F1: LIST     F2: RESET";
   } else if (mode == VIEW_REG || mode == VIEW_POS_REG_C || mode == VIEW_POS_REG_J) {
     
     text = "F1: EDIT     F2: SWITCH";
   } else if (mode == INPUT_COMMENT_U) {
     text = "F1: ABCDEF     F2: GHIJKL     F3: MNOPQR     F4: STUVWX     F5: YZ_@*.";
   } else if (mode == INPUT_COMMENT_L) {
     text = "F1: abcdef     F2: ghijkl     F3: mnopqr     F4: stuvwx     F5: yz_@*.";
   }
   
   
   if (text != null) {
     fn_info.setText(text)
                 .setPosition(next_px, display_py+display_height-15)
                 .setColorValue(normal)
                 .show()
                 .moveTo(g1)
                 ;
   } else {
     fn_info.setText("")
                   .show()
                   .moveTo(g1)
                   ;
   }
   
} // end updateScreen()

// clear screen
public void clearScreen(){
   clearContents();
   clearOptions();
   
   // hide the text labels that show the start and end of a program
   if (mode != INSTRUCTION_NAV && mode != INSTRUCTION_EDIT){
      if (cp5.getController("-1") != null){
           cp5.getController("-1")
              .remove()
              ;
      }     
      if (cp5.getController("-2") != null){
           cp5.getController("-2")
              .remove()
              ;   
      }
      fn_info.remove();
   }
   
   clearNums();
   
   cp5.update();
   contents = new ArrayList<ArrayList<String>>();
}

public void clearContents(){
   for(int i=0;i<index_contents;i++){
     if(cp5.getController(Integer.toString(i)) != null)
       cp5.getController(Integer.toString(i)).remove();
     else if(cp5.getGroup(Integer.toString(i)) != null)
       cp5.getGroup(Integer.toString(i)).remove();
   }
   index_contents = 0;
}

public void clearOptions(){
   for(int i=100;i<index_options;i++){
      cp5.get(Integer.toString(i)).remove();
   }
   index_options = 100;
}

public void clearNums(){
   for(int i=1000;i<index_nums;i++){
      cp5.getController(Integer.toString(i)).remove();
   }
   index_nums = 1000;
}

/**
 * Displays the Interface for inputting the name of a program.
 */
public void inputProgramName() {
  active_row = active_col = -1;
  contents = new ArrayList<ArrayList<String>>();
  
  contents.add( newLine("") );
  contents.add( newLine("(ENTER: confirm name)") );
  contents.add( newLine("") );
  contents.add( newLine("") );
  contents.add( newLine("Program Name:   " + workingText) );
  
  which_option = -1;
  options = new ArrayList<String>();
  updateScreen(color(0), color(0));
}

/* Loads the set of Frames that correspond to the given coordinate frame.
 * Only COORD_TOOL and COOR_USER have Frames sets as of now.
 * 
 * @param coorFrame  the integer value representing the coordinate frame
 *                   of the desired frame set
 */
public void loadFrames(int coordFrame) {
  
  Frame[] frames = null;
  
  if (coordFrame == COORD_TOOL) {
    frames = toolFrames;
    mode = NAV_TOOL_FRAMES;
  } else if (coordFrame == COORD_USER) {
    frames = userFrames;
    mode = NAV_USER_FRAMES;
  }
  // Only the Tool and User Frame lists have been implemented
  if (frames != null) {
    contents = new ArrayList<ArrayList<String>>();
    options = new ArrayList<String>();
    which_option = -1;
    
    for (int idx = 0; idx < frames.length; ++idx) {
      // Display each frame on its own line
      Frame frame = frames[idx];
      contents.add ( newLine( String.format("%d) %s", idx + 1, frame.getOrigin()) ) );
    }
    
    active_col = active_row = 0;
    updateScreen(color(255,0,0), color(0));
  }
}

/**
 * Displays the points along with their respective titles for the
 * current frame teach method (discluding the Direct Entry method).
 */
public void loadPointList() {
  options = new ArrayList<String>();
  
  if (teachPointTMatrices != null) {
  
    ArrayList<String> limbo = new ArrayList<String>();
    // Display TCP teach points
    if ((super_mode == NAV_TOOL_FRAMES && mode == THREE_POINT_MODE) || mode == SIX_POINT_MODE) {
      limbo.add("First Approach Point: ");
      limbo.add("Second Approach Point: ");
      limbo.add("Third Approach Point: ");
    }
    // Display Axes Vectors teach points
    if ((super_mode == NAV_USER_FRAMES && mode == THREE_POINT_MODE) || mode == FOUR_POINT_MODE || mode == SIX_POINT_MODE) {
      
      limbo.add("Orient Origin Point: ");
      limbo.add("X Direction Point: ");
      limbo.add("Y Direction Point: ");
    }
    // Display origin offset point
    if (super_mode == NAV_USER_FRAMES && mode == FOUR_POINT_MODE) {
      // Name of fourth point for the four point method?
      limbo.add("Origin: ");
    }
    
    int size = teachPointTMatrices.size();
    // Determine if the point has been set yet
    for (int idx = 0; idx < limbo.size(); ++idx) {
      // Add each line to options
      options.add( limbo.get(idx) + ((size > idx) ? "RECORDED" : "UNINIT") );
    }
  } else {
    // No teach points
    options.add("Error: teachPointTMatrices not set!");
  }
  
  updateScreen(color(255,0,0), color(0));
}

/**
 * Transitions to the Direct Entry menu, which resembles
 * the Frame Detail menu, however, the user is allowed
 * to input values for the x, y, z, w, p, r fields.
 */
public void loadDirectEntryMethod() {
  contents = new ArrayList<ArrayList<String>>();
  
  String str = "";
  
  if (super_mode == NAV_TOOL_FRAMES) {
    str = "TOOL FRAME ";
  } else if (super_mode == NAV_USER_FRAMES) {
    str = "USER FRAME ";
  }
  
  contents.add( newLine(str) );
  
  contents.add( newLine("X: 0.0") );
  contents.add( newLine("Y: 0.0") );
  contents.add( newLine("Z: 0.0") );
  contents.add( newLine("W: 0.0") );
  contents.add( newLine("P: 0.0") );
  contents.add( newLine("R: 0.0") );
  
  // Defines the length of a line's prefix
  which_option = 3;
  active_row = 1;
  active_col = 0;
  mode = DIRECT_ENTRY_MODE;
  updateScreen(color(255, 0, 0), color(0));
}

/**
 * Given a valid set of at least 3 points, this method will return the average
 * TCP point. If more than three points exist in the list then, only the first
 * three are used. If the list contains less than 3 points, then null is returned.
 * If an avergae TCP cannot be calculated from the given points then null is
 * returned as well.
 *
 * @param points  a set of at least 3 4x4 transformation matrices representating
 *                the position and orientation of three points in space
 */
public double[] calculateTCPFromThreePoints(ArrayList<float[][]> points) {
  
  if (points != null && points.size() >= 3) {
      /****************************************************************
         Three Point Method Calculation
         
         ------------------------------------------------------------
         A, B, C      transformation matrices
         Ar, Br, Cr   rotational portions of A, B, C respectively
         At, Bt, Ct   translational portions of A, B, C repectively
         x            TCP point with respect to the EE
         ------------------------------------------------------------
         
         Ax = Bx = Cx
         Ax = (Ar)x + At
         
         (A - B)x = 0
         (Ar - Br)x + At - Bt = 0
         
         Ax + Bx - 2Cx = 0
         (Ar + Br - 2Cr)x + At + Bt - 2Ct = 0
         (Ar + Br - 2Cr)x = 2Ct - At - Bt
         x = (Ar + Br - 2Cr) ^ -1 * (2Ct - At - Bt)
         
       ****************************************************************/
      RealVector avg_TCP = new ArrayRealVector(new double[] {0.0, 0.0, 0.0} , false);
      
      for (int idxC = 0; idxC < 3; ++idxC) {
        
        int idxA = (idxC + 1) % 3,
            idxB = (idxA + 1) % 3;
        
        if (DISPLAY_TEST_OUTPUT) { System.out.printf("\nA = %d\nB = %d\nC = %d\n\n", idxA, idxB, idxC); }
        
        RealMatrix Ar = new Array2DRowRealMatrix(floatToDouble(points.get(idxA), 3, 3));
        RealMatrix Br = new Array2DRowRealMatrix(floatToDouble(points.get(idxB), 3, 3));
        RealMatrix Cr = new Array2DRowRealMatrix(floatToDouble(points.get(idxC), 3, 3));
        
        double [] t = new double[3];
        for (int idx = 0; idx < 3; ++idx) {
          // Build a double from the result of the translation portions of the transformation matrices
          t[idx] = 2 * points.get(idxC)[idx][3] - ( points.get(idxA)[idx][3] + points.get(idxB)[idx][3] );
        }
        
        /* 2Ct - At - Bt */
        RealVector b = new ArrayRealVector(t, false);
        /* Ar + Br - 2Cr */
        RealMatrix R = ( Ar.add(Br) ).subtract( Cr.scalarMultiply(2) );
        
        /* (R ^ -1) * b */
        avg_TCP = avg_TCP.add( (new SingularValueDecomposition(R)).getSolver().getInverse().operate(b) );
      }
      
      /* Take the average of the three cases: where C = the first point, the second point, and the third point */
      avg_TCP = avg_TCP.mapMultiply( 1.0 / 3.0 );
      
      if (DISPLAY_TEST_OUTPUT) {
        for (int pt = 0; pt < 3 && pt < points.size(); ++pt) {
          // Print out each matrix
          System.out.printf("Point %d:\n", pt);
          println( matrixToString(points.get(pt)) );
        }
        
        System.out.printf("(Ar + Br - 2Cr) ^ -1 * (2Ct - At - Bt):\n\n[%5.4f]\n[%5.4f]\n[%5.4f]\n\n", avg_TCP.getEntry(0), avg_TCP.getEntry(1), avg_TCP.getEntry(2));
      }
      
      for (int idx = 0 ; idx < avg_TCP.getDimension(); ++idx) {
        if (abs((float)avg_TCP.getEntry(idx)) > 1000.0) {
          return null;
        }
      }
      
      return avg_TCP.toArray();
  }
  
  return null;
}

/**
 * Creates a 3x3 rotation matrix based off of two vectors defined by the
 * given set of three transformation matrices representing points in space.
 * If the list contains more than three points, then only the first three
 * points will be used.
 * The three points are used to form two vectors. The first vector is treated
 * as the negative x-axis and the second one is the psuedo-negative z-axis.
 * These vectors are crossed to form the y-axis. The y-axis is then crossed
 * with the negative x-axis to form the true y-axis.
 *
 * @param points  a set of at least three 4x4 transformation matrices
 * @return        a set of three unit vectors (down the columns) that
 *                represent an axes
 */
public float[][] createAxesFromThreePoints(ArrayList<float[][]> points) {
  // 3 points are necessary for the creation of the axes
  if (points.size() >= 3) {
    float[][] axes = new float[3][];
    float[] x_ndir = new float[3],
            z_ndir = new float[3];
            
    // From preliminary negative x and z axis vectors
    for (int row = 0; row < 3; ++row) {
      x_ndir[row] = points.get(1)[row][3] - points.get(0)[row][3];
      z_ndir[row] = points.get(2)[row][3] - points.get(0)[row][3];
    }
    
    // Form axes
    axes[0] = negate(x_ndir);                         // X axis
    axes[1] = crossProduct(axes[0], negate(z_ndir));  // Y axis
    axes[2] = crossProduct(axes[0], axes[1]);         // Z axis
    
    if ((axes[0][0] == 0f && axes[0][1] == 0f && axes[0][2] == 0f) ||
        (axes[1][0] == 0f && axes[1][1] == 0f && axes[1][2] == 0f) ||
        (axes[2][0] == 0f && axes[2][1] == 0f && axes[2][2] == 0f)) {
      // One of the three axis vectors is the zero vector
      return null;
    }
    
    float[] magnitudes = new float[axes.length];
    
    for (int v = 0; v < axes.length; ++v) {
      // Find the magnitude of each axis vector
      for (int e = 0; e < axes[0].length; ++e) {
        magnitudes[v] += pow(axes[v][e], 2);
      }
      
      magnitudes[v] = sqrt(magnitudes[v]);
      // Normalize each vector
      for (int e = 0; e < axes.length; ++e) {
        axes[v][e] /= magnitudes[v];
      }
    }
    
    return axes;
  }
  
  return null;
}

/**
 * Transitions to the Frame Details menu, which displays
 * the x, y, z, w, p, r values associated with the Frame
 * at curFrameIdx in either the Tool Frames or User Frames,
 * based on the value of super_mode.
 */
public void loadFrameDetails() {
  contents = new ArrayList<ArrayList<String>>();
  
  String header = null;
  Frame[] frames = null;
  // Display the frame set name as well as the index of the currently selected frame
  if (super_mode == NAV_TOOL_FRAMES) {
    frames = toolFrames;
    header = String.format("TOOL FRAME: %d", curFrameIdx + 1);
  } else if (super_mode == NAV_USER_FRAMES) {
    frames = userFrames;
    header = String.format("USER FRAME: %d", curFrameIdx + 1);
  }
  
  if (frames != null) {
    
    contents.add( newLine(header) );
    contents.add( newLine(String.format("X: %8.4f", frames[curFrameIdx].getOrigin().x) ) );
    contents.add( newLine(String.format("Y: %8.4f", frames[curFrameIdx].getOrigin().y) ) );
    contents.add( newLine(String.format("Z: %8.4f", frames[curFrameIdx].getOrigin().z) ) );
    contents.add( newLine(String.format("W: %8.4f", frames[curFrameIdx].getWpr().x) ) );
    contents.add( newLine(String.format("P: %8.4f", frames[curFrameIdx].getWpr().y) ) );
    contents.add( newLine(String.format("R: %8.4f", frames[curFrameIdx].getWpr().z) ) );
    
    active_row = -1;
    which_option = -1;
    options = new ArrayList<String>();
    
    mode = FRAME_DETAIL;
    updateScreen(color(255,0,0), color(0));
  }
}

/**
 * Allow the user to choose between viewing the Registers and
 * Position Registers and choosing which form the points will
 * be display in for Position Registers (either Joint or
 * Cartesian).
 */
public void pickRegisterList() {
  options = new ArrayList<String>();
  
  // Determine what registers are available to switch based on the current mode
  if (mode == VIEW_REG) {
     options.add("1. Position Registers (Joint)");
     options.add("2. Position Registers (Cartesian)");
   } else if (mode == VIEW_POS_REG_J) {
     options.add("1. Registers");
     options.add("2. Position Registers (Cartesian)");
   } else if (mode == VIEW_POS_REG_C) {
     options.add("1. Registers");
     options.add("2. Position Registers (Joint)");
   } else {
    options.add("1. Registers");
    options.add("2. Position Registers (Joint)");
    options.add("3. Position Registers (Cartesian)");
  }
     
  which_option = 0;
  
  super_mode = mode;
  mode = PICK_REG_LIST;
  updateScreen(color(255, 0, 0), color(0));
}

/**
 * Displays the list of Registers in mode VIEW_REG or the Position Registers
 * for modes VIEW_REG_J or VIEW_REG_C. In mode VIEW_REG_J the joint angles
 * associated with the Point are displayed and the Cartesian values are
 * displayed in mode VIEW_REG_C.
 */
public void viewRegisters() {
  options = new ArrayList<String>();
  which_option = -1;
  
  contents = new ArrayList<ArrayList<String>>();
  
  // View Registers or Position Registers
  if (mode == VIEW_REG || mode == VIEW_POS_REG_J || mode == VIEW_POS_REG_C) {
    // Header
    String limbo = (mode == VIEW_REG) ? "REGISTERS" : "POSITION REGISTERS";
    contents.add( newLine(limbo) );
    contents.add( newLine("") );
    
    int start = text_render_start;
    int end = min(start + ITEMS_TO_SHOW - 1, REG.length);
    // Display a subset of the list of registers
    for (int idx = start; idx < end; ++idx) {
      String spaces;
      
      if (idx < 9) {
        spaces = "  ";
      } else if (idx < 99) {
        spaces = " ";
      } else {
        spaces = "";
      }
      // Display the line number
      String lineNum = String.format("%d)%s", (idx + 1), spaces);
      
      String lbl;
      
      if (mode == VIEW_REG) {
        lbl = (REG[idx].comment == null) ? "" : REG[idx].comment;
      } else {
        lbl  = (POS_REG[idx].comment == null) ? "" : POS_REG[idx].comment;
      }
      
      int buffer = 16 - lbl.length();
      while (buffer-- > 0) { lbl += " "; }
      
      // Display the comment asscoiated with a specific Register entry
      String regLbl = String.format("%s[%d:%s%s]", (mode == VIEW_REG) ? "R" : "PR", (idx + 1), spaces, lbl);
      // Display Register value (* if uninitialized)
      String regEntry = "*";
      
      if (mode == VIEW_REG) {
        if (REG[idx].value != null) {
          // Dispaly Register value
          regEntry = String.format("%4.3f", REG[idx].value);
        }
      
      } else if (POS_REG[idx].point != null) {
        // What to display for a point ...
        regEntry = "...";
      } else if (mode == VIEW_POS_REG_C && POS_REG[idx] == null) {
        // Distinguish Joint from Cartesian mode for now
        regEntry = "#";
      }
      
      contents.add( newLine(lineNum, regLbl, regEntry) );
    }
      
    /* Maybe useful later ...
      
      else {
        String[] entries = null;
        
        if (mode == VIEW_POS_REG_J) {
          entries = POS_REG[idx].point.toJointStringArray();
        } else {
          // mode == VIEW_POS_REG_C
          entries = POS_REG[idx].point.toCartesianStringArray();
        }
        
        /* Display each portion of the Point's position and orientation in
         * a separate column  whether it be X, Y, Z, W, P, R (Cartesian) or 
         * J1 - J6 (Joint angles) *
         contents.add( newLine(lineNum, regLbl, entries[0], entries[1], entries[2], entries[3], entries[4], entries[5]) );
      }*/
  } else {
    // mode must be VIEW_REG or VIEW_POS_REG_J(C)!
    contents.add( newLine( String.format("%d is not a valid mode for view registers!", mode)) );
    active_row = active_col = 0;
  }
  
  updateScreen(color(255, 0, 0), color(0));
}

/** TODO comment */
public void loadInputRegisterValueMethod() {
  options = new ArrayList<String>();
  options.add("Input a value using the keypad");
  options.add("");
  options.add(workingText);
  
  which_option = 0;
  super_mode = mode;
  mode = INPUT_FLOAT;
  updateScreen(color(255, 0, 0), color(0));
}

/** TODO comment */
public void loadInputRegisterPointMethod() {
  contents = new ArrayList<ArrayList<String>>();
  
  if (active_index >= 0 && active_index < POS_REG.length) {
    String header = "POSITION REGISTER";
    
    if (POS_REG[active_index].comment != null) {
      // Show comment if it exists
      header += ": " + POS_REG[active_index].comment;
    }
  
    contents.add( newLine(header) );
    
    if (POS_REG[active_index].point == null) {
      // Initialize valeus to zero if the entry is null
      if (mode == INPUT_POINT_C) {
        
        contents.add( newLine("X: 0.0") );
        contents.add( newLine("Y: 0.0") );
        contents.add( newLine("Z: 0.0") );
        contents.add( newLine("W: 0.0") );
        contents.add( newLine("P: 0.0") );
        contents.add( newLine("R: 0.0") );
      } else if (mode == INPUT_POINT_J) {
        
        for (int idx = 1; idx <= 6; ++idx) {
          contents.add( newLine(String.format("J%d: 0.0", idx)) );
        }
      }
    } else {
      // List current entry values if the Register is initialized
      String[] entries = (mode == INPUT_POINT_C) ? POS_REG[active_index].point.toCartesianStringArray()
                                                 : POS_REG[active_index].point.toJointStringArray();
      
      for (String entry : entries) {
        contents.add( newLine(entry) );
      }
    }
    
  }
  
  // Defines the length of a line's prefix
  which_option = (mode == INPUT_POINT_J) ? 4 : 3;
  active_row = 1;
  active_col = 0;
  updateScreen(color(255, 0, 0), color(0));
}

/** TODO comment */
public void loadInputRegisterCommentMethod() {
  contents = new ArrayList<ArrayList<String>>();
  options = new ArrayList<String>();
  
  workingText = "\0";
  // Load the current comment for the selected register if it exists
  if (mode == VIEW_REG) {
    if (active_index >= 0 && active_index < REG.length && REG[active_index].comment != null) {
      workingText = REG[active_index].comment;
    }
  } else if ((mode == VIEW_POS_REG_J || mode == VIEW_POS_REG_C) && POS_REG[active_index].comment != null) {
    if (active_index >= 0 && active_index < POS_REG.length) {
      workingText = POS_REG[active_index].comment;
    }
  }
  
  contents.add( newLine("Enter a name for the selected register") );
  contents.add( newLine("") );
  contents.add( newLine("") );
  updateComment();
  
  options.add("1. Uppercase");
  options.add("1. Lowercase");
  which_option = 0;
  
  super_mode = mode;
  // Navigate options menu to switch the function keys functions
  if (which_option == 0) {
    mode = INPUT_COMMENT_U;
  } else if (which_option == 1) {
    mode = INPUT_COMMENT_L;
  }
  
  active_row = 2;
  active_col = 0;
  updateScreen(color(255, 0, 0), color(0));
}

/** TODO comment */
public void updateComment() {
  
  ArrayList<String> line = new ArrayList<String>();
  // Give each letter in the name a separate column
  for (int idx = 0; idx < workingText.length() && idx < 16; ++idx) {
    line.add( Character.toString(workingText.charAt(idx)) );
  }
  
  contents.set(2, line);
  updateScreen(color(255, 0, 0), color(0));
}

// prepare for displaying motion instructions on screen
public void loadInstructions(int programID){
   Program p = programs.get(programID);
   contents = new ArrayList<ArrayList<String>>();
   int size = p.getInstructions().size();
   
   int start = text_render_start;
   int end = min(start + ITEMS_TO_SHOW, size);
   if (end >= size) end = size;
   for(int i=start;i<end;i++){
      ArrayList<String> m = new ArrayList<String>();
      m.add(Integer.toString(i+1) + ")");
      Instruction instruction = p.getInstructions().get(i);
      if (instruction instanceof MotionInstruction) {
        MotionInstruction a = (MotionInstruction)instruction;
        if(armModel.getEEPos().dist(a.getVector(p).pos) < liveSpeed){
          println("at tgt position");
          m.add("@");
        }
        else{
          println(a.getVector(p).pos);
          m.add("_");
        }
        // add motion type
        switch (a.getMotionType()){
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
        if (a.getGlobal()) m.add("PR[");
        else m.add("P[");
        
        m.add(a.getRegister()+"]");
        
        if (a.getMotionType() == MTYPE_JOINT) m.add((a.getSpeed() * 100) + "%");
        else m.add((int)(a.getSpeed()) + "mm/s");
        
        if (a.getTermination() == 0) m.add("FINE");
        else m.add("CONT" + (int)(a.getTermination()*100));
        
        contents.add(m);
      } 
      else if (instruction instanceof ToolInstruction ||
                 instruction instanceof FrameInstruction)
      {
        m.add(instruction.toString());
        contents.add(m);
      }
   } 
}

/**
 * Deals with updating the UI after confirming/canceling a deletion
 */
public void deleteInstEpilogue() {
  Program prog = programs.get(active_program);
  
  active_instruction = min(active_instruction,  prog.getInstructions().size() - 1);
  /* 13 is the maximum number of instructions that can be displayed at one point in time */
  active_row = min(active_instruction, ITEMS_TO_SHOW - 1);
  active_col = 0;
  text_render_start = active_instruction - active_row;
  
  loadInstructions(active_program);
  
  mode = super_mode;
  super_mode = NONE;
  options.clear();
  updateScreen(color(255,0,0), color(0,0,0));
}

/* Transitions to the active frames menu, which display
 * the label for each Frame set (Tool or User) as well
 * as the index of the currently active frame for each
 * respective frame set. */
void loadActiveFrames() {
  contents = new ArrayList<ArrayList<String>>();
  active_row = 1;
  options = new ArrayList<String>();
  which_option = -1;
  
  contents.add( newLine("ACTIVE FRAMES") );
  contents.add( newLine("Tool: " + (activeToolFrame + 1)) );
  contents.add( newLine("User: " + (activeUserFrame + 1)) );
  //contents.add( newLine("Jog: " + (activeJogFrame + 1)) );
  
  mode = ACTIVE_FRAMES;
  updateScreen(color(255,0,0), color(0));
}

void loadPrograms() {
   options = new ArrayList<String>(); // clear options
   nums = new ArrayList<Integer>(); // clear numbers
   
   if (cp5.getController("-2") != null) cp5.getController("-2").remove();
   fn_info.setText("");
   
   int size = programs.size();
   /*if (size <= 0){
      programs.add(new Program("My Program 1"));
   }/* */
   
   active_instruction = 0;
   
   contents.clear();  
   int start = text_render_start;
   int end = min(start + ITEMS_TO_SHOW, size);
   
   for(int i=start;i<end;i++) {
      contents.add( newLine(programs.get(i).getName()) );
   }
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
   
   for (String col : columns) {
     line.add(col);
   }
   
   return line;
}
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
          ACTIVE_FRAMES = 19,
          PICK_INSTRUCTION = 20,
          IO_SUBMENU = 21,
          SET_DO_BRACKET = 22,
          SET_DO_STATUS = 23,
          SET_RO_BRACKET = 24,
          SET_RO_STATUS = 25,
          SET_FRAME_INSTRUCTION = 26,
          EDIT_MENU = 27,
          CONFIRM_DELETE = 28;
static boolean STOP_MOVEMENT = false;
static int     EE_MAPPING = 0;

int frame = FRAME_JOINT; // current frame
//String displayFrame = "JOINT";

 
int active_program = -1; // which program is on focus when in PROGRAM_NAV mode?
int select_program = -1; // which program is being edited when in INSTRUCTION_NAV or INSTRUCTION_EDIT mode?
int active_instruction = -1; // which motion instruction is on focus when in INSTRUCTION_NAV mode?
int select_instruction = -1; // which motion instruction is being edited when in INSTRUCTION_EDIT mode?
int mode = NONE; 
int NUM_MODE; // When NUM_MODE is ON, allows for entering numbers
int shift = OFF; // Is shift button pressed or not?
int step = OFF; // Is step button pressed or not?
int record = OFF;
 
int g1_px, g1_py; // the left-top corner of group1
int g1_width, g1_height; // group 1's width and height
int display_px, display_py; // the left-top corner of display screen
int display_width = 340, display_height = 270; // height and width of display screen

Group g1;
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
final int ITEMS_TO_SHOW = 8; // how many instructions to show onscreen at a time
final int PROGRAMS_TO_SHOW = 16; // how many programs to show onscreen at a time
int letterSet; // which letter group to enter
Frame currentFrame;
int inFrame;
int teachingWhichPoint;
PVector[] tempPoints = new PVector[3];
PVector[] tempVectors = new PVector[3];
int activeUserFrame = -1;
int activeJogFrame = -1;
int activeToolFrame = -1;
PVector[] teachingPts = new PVector[3];

// display on screen
ArrayList<ArrayList<String>> contents = new ArrayList<ArrayList<String>>(); // display list of programs or motion instructions
ArrayList<String> options = new ArrayList<String>(); // display options for an element in a motion instruction
ArrayList<Integer> nums = new ArrayList<Integer>(); // store numbers pressed by the user
int active_row = 0, active_col = 0; // which element is on focus now?
int which_option = -1; // which option is on focus now?
int index_contents = 0, index_options = 100, index_nums = 1000; // how many textlabels have been created for display

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
                 .setBackgroundColor(color(127,127,127,50))
                 ;
   
   myTextarea = cp5.addTextarea("txt")
      .setPosition(display_px,display_py)
      .setSize(display_width, display_height)
      .setLineHeight(14)
      .setColor(color(128))
      .setColorBackground(color(200,255,255))
      .setColorForeground(color(0,0,0))
      .moveTo(g1); 
   
   // expand group 1's width and height
   g1_width += 340;
   g1_height += 270;
   
   // text label to show how to use F1 - F5 keys
   fn_info = cp5.addTextlabel("fn_info")
                .hide()
                ;    
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
    PImage[] zoomin_shrink = {loadImage("images/zoomin_35x20.png"), loadImage("images/zoomin_over.png"), loadImage("images/zoomin_down.png")};   
    bt_zoomin_shrink = cp5.addButton("zoomin_shrink")
       .setPosition(zoomin_shrink_px, zoomin_shrink_py)
       .setSize(LARGE_BUTTON, SMALL_BUTTON)
       .setImages(zoomin_shrink)
       .updateSize()
       .hide()
       ;   
       
    int zoomout_shrink_px = zoomin_shrink_px + LARGE_BUTTON ;
    int zoomout_shrink_py = zoomin_shrink_py;   
    PImage[] zoomout_shrink = {loadImage("images/zoomout_35x20.png"), loadImage("images/zoomout_over.png"), loadImage("images/zoomout_down.png")};   
    bt_zoomout_shrink = cp5.addButton("zoomout_shrink")
       .setPosition(zoomout_shrink_px, zoomout_shrink_py)
       .setSize(LARGE_BUTTON, SMALL_BUTTON)
       .setImages(zoomout_shrink)
       .updateSize()
       .hide()
       ;    
   
    int pan_shrink_px = zoomout_shrink_px + LARGE_BUTTON;
    int pan_shrink_py = zoomout_shrink_py ;
    PImage[] pan_shrink = {loadImage("images/pan_35x20.png"), loadImage("images/pan_over.png"), loadImage("images/pan_down.png")};   
    bt_pan_shrink = cp5.addButton("pan_shrink")
       .setPosition(pan_shrink_px, pan_shrink_py)
       .setSize(LARGE_BUTTON, SMALL_BUTTON)
       .setImages(pan_shrink)
       .updateSize()
       .hide()
       ;    
       
    int rotate_shrink_px = pan_shrink_px + LARGE_BUTTON;
    int rotate_shrink_py = pan_shrink_py;   
    PImage[] rotate_shrink = {loadImage("images/rotate_35x20.png"), loadImage("images/rotate_over.png"), loadImage("images/rotate_down.png")};   
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
    PImage[] zoomin_normal = {loadImage("images/zoomin_35x20.png"), loadImage("images/zoomin_over.png"), loadImage("images/zoomin_down.png")};   
    bt_zoomin_normal = cp5.addButton("zoomin_normal")
       .setPosition(zoomin_normal_px, zoomin_normal_py)
       .setSize(LARGE_BUTTON, SMALL_BUTTON)
       .setImages(zoomin_normal)
       .updateSize()
       .moveTo(g1) ;   
       
    int zoomout_normal_px = zoomin_normal_px + LARGE_BUTTON + 1;
    int zoomout_normal_py = zoomin_normal_py;   
    PImage[] zoomout_normal = {loadImage("images/zoomout_35x20.png"), loadImage("images/zoomout_over.png"), loadImage("images/zoomout_down.png")};   
    bt_zoomout_normal = cp5.addButton("zoomout_normal")
       .setPosition(zoomout_normal_px, zoomout_normal_py)
       .setSize(LARGE_BUTTON, SMALL_BUTTON)
       .setImages(zoomout_normal)
       .updateSize()
       .moveTo(g1) ;    
   
    int pan_normal_px = zoomout_normal_px + LARGE_BUTTON + 1;
    int pan_normal_py = zoomout_normal_py ;
    PImage[] pan = {loadImage("images/pan_35x20.png"), loadImage("images/pan_over.png"), loadImage("images/pan_down.png")};   
    bt_pan_normal = cp5.addButton("pan_normal")
       .setPosition(pan_normal_px, pan_normal_py)
       .setSize(LARGE_BUTTON, SMALL_BUTTON)
       .setImages(pan)
       .updateSize()
       .moveTo(g1) ;    
       
    int rotate_normal_px = pan_normal_px + LARGE_BUTTON + 1;
    int rotate_normal_py = pan_normal_py;   
    PImage[] rotate = {loadImage("images/rotate_35x20.png"), loadImage("images/rotate_over.png"), loadImage("images/rotate_down.png")};   
    bt_rotate_normal = cp5.addButton("rotate_normal")
       .setPosition(rotate_normal_px, rotate_normal_py)
       .setSize(LARGE_BUTTON, SMALL_BUTTON)
       .setImages(rotate)
       .updateSize()
       .moveTo(g1) ;     
       
    int record_normal_px = rotate_normal_px + LARGE_BUTTON + 1;
    int record_normal_py = rotate_normal_py;   
    PImage[] record = {loadImage("images/record-35x20.png"), loadImage("images/record-over.png"), loadImage("images/record-on.png")};   
    bt_record_normal = cp5.addButton("record_normal")
       .setPosition(record_normal_px, record_normal_py)
       .setSize(LARGE_BUTTON, SMALL_BUTTON)
       .setImages(record)
       .updateSize()
       .moveTo(g1) ;     
      
    int EE_normal_px = record_normal_px + LARGE_BUTTON + 1;
    int EE_normal_py = record_normal_py;   
    PImage[] EE = {loadImage("images/EE_35x20.png"), loadImage("images/EE_over.png"), loadImage("images/EE_down.png")};   
    bt_ee_normal = cp5.addButton("EE")
       .setPosition(EE_normal_px, EE_normal_py)
       .setSize(LARGE_BUTTON, SMALL_BUTTON)
       .setImages(EE)
       .updateSize()
       .moveTo(g1) ;    
       
   PImage[] imgs_arrow_up = {loadImage("images/arrow-up.png"), loadImage("images/arrow-up_over.png"), loadImage("images/arrow-up_down.png")};   
   int up_px = display_px+display_width + 2;
   int up_py = display_py;
   cp5.addButton("up")
       .setPosition(up_px, up_py)
       .setSize(LARGE_BUTTON, LARGE_BUTTON)
       .setImages(imgs_arrow_up)
       .updateSize()
       .moveTo(g1) ;     
   
    PImage[] imgs_arrow_down = {loadImage("images/arrow-down.png"), loadImage("images/arrow-down_over.png"), loadImage("images/arrow-down_down.png")};   
    int dn_px = up_px;
    int dn_py = up_py + LARGE_BUTTON + 2;
    cp5.addButton("dn")
       .setPosition(dn_px, dn_py)
       .setSize(LARGE_BUTTON, LARGE_BUTTON)
       .setImages(imgs_arrow_down)
       .updateSize()
       .moveTo(g1) ;    
   
    PImage[] imgs_arrow_l = {loadImage("images/arrow-l.png"), loadImage("images/arrow-l_over.png"), loadImage("images/arrow-l_down.png")};
    int lt_px = dn_px;
    int lt_py = dn_py + LARGE_BUTTON + 2;
    cp5.addButton("lt")
       .setPosition(lt_px, lt_py)
       .setSize(LARGE_BUTTON, LARGE_BUTTON)
       .setImages(imgs_arrow_l)
       .updateSize()
       .moveTo(g1) ;  
    
    PImage[] imgs_arrow_r = {loadImage("images/arrow-r.png"), loadImage("images/arrow-r_over.png"), loadImage("images/arrow-r_down.png")};
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
   PImage[] imgs_LEFT = {loadImage("images/LEFT.png"), loadImage("images/LEFT.png"), loadImage("images/LEFT.png")};  
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

/* Highlight the buttons in the pendant corresponding to the motion of the robot
 * for which its corresponding joint in the robot is in motion.
 *
 * TODO: handle linear coordinate motion */
public void updateButtonColors() {
  
  color highlighted = color(255, 0, 0);
  color regular = color(127, 127, 255);
  
  /* Loop through each joint of the robot and highlight the button in the pendant
   * corresponding to the motion of each joint (i.e. +, -, or stationary)
   *
   * NOTE: There are one less joints then segments */
  for (int seg = 0; seg < armModel.segments.size() - 1; ++seg) {
    
    Model j = armModel.segments.get(seg);
    
    if ((j.jointsMoving[0] < 0 || j.jointsMoving[1] < 0 || j.jointsMoving[2] < 0) || (seg < 3 && armModel.linearMoveSpeeds[seg] < 0)) {
      
      // Moving in negative direction
      ((Button)cp5.get("JOINT" + (seg + 1) + "_NEG")).setColorBackground(highlighted);
      ((Button)cp5.get("JOINT" + (seg + 1) + "_POS")).setColorBackground(regular);
    } else if ((j.jointsMoving[0] > 0 || j.jointsMoving[1] > 0 || j.jointsMoving[2] > 0) || (seg < 3 && armModel.linearMoveSpeeds[seg] > 0)) {
      
      // Moving in positive direction
      ((Button)cp5.get("JOINT" + (seg + 1) + "_NEG")).setColorBackground(regular);
      ((Button)cp5.get("JOINT" + (seg + 1) + "_POS")).setColorBackground(highlighted);
    } else {
      
      // Stationary
      ((Button)cp5.get("JOINT" + (seg + 1) + "_NEG")).setColorBackground(regular);
      ((Button)cp5.get("JOINT" + (seg + 1) + "_POS")).setColorBackground(regular);
    }
  }
}

/* mouse events */
public void mousePressed(){
  
  
   if ((clickPan % 2) == 1 ) { // pan button is pressed
      if (doPan) {
         doPan = false;
      } else {
         doPan = true;
      }
   }
   
   if ((clickRotate % 2) == 1 ) { // rotate button is pressed
      if (doRotate) {
         doRotate = false;
      } else {
         doRotate = true;
      }
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


// scroll mouse to zoom in / out
public void mouseWheel(MouseEvent event){
   float e = event.getCount();
   if (e > 0 ) {
      myscale *= 1.1;
   }
   if (e < 0){
      myscale *= 0.9; 
   }
}

public void keyPressed(){
  
  if (mode == ENTER_TEXT) {
    
    if (workingText.length() < 10 && ( (key >= 'A' && key <= 'Z') || (key >= 'a' && key <= 'z') )) {
      workingText += key;
    } else if (keyCode == BACKSPACE && workingText.length() > 0) {
      workingText = workingText.substring(0, workingText.length() - 1);
    }
    
    options = new ArrayList<String>();
    options.add("");
    options.add("(ENTER: confirm name)");
    options.add("");
    options.add("");
    options.add("Program Name:   " + workingText);
    which_option = 0;
    updateScreen(color(0), color(0));
    
    return;
  } else if(key == 'r'){
    panX = 0;
    panY = 0;
    myscale = 0.5;
    myRotX = 0;
    myRotY = 0;
  } else if(key == 't'){
    float[] rot = {0, 0, 0, 0, 0, 0};
    armModel.setJointRotations(rot);
  } else if (key == 'q') {
    hd(0);
    return;
  } else if (key == 'e') {
    EE_MAPPING = (EE_MAPPING + 1) % 2;
  }
  
   /* click spacebar once to activate pan button
    * click spacebar again to deactivate pan button
    */ 
   if (key == ' '){ 
      clickPan += 1;
      if ((clickPan % 2) == 1){
         cursorMode = HAND;
         PImage[] pressed = {loadImage("images/pan_down.png"), loadImage("images/pan_down.png"), loadImage("images/pan_down.png")};
         if (bt_show.isVisible()){
              cp5.getController("pan_shrink")
                 .setImages(pressed);
         }else{
              cp5.getController("pan_normal")
                 .setImages(pressed);
         }
            
      }else{
         cursorMode = ARROW;
         PImage[] released = {loadImage("images/pan_35x20.png"), loadImage("images/pan_over.png"), loadImage("images/pan_down.png")}; 
         if (bt_show.isVisible()){
              cp5.getController("pan_shrink")
                 .setImages(released);
         }else{
              cp5.getController("pan_normal")
                 .setImages(released);
         }
         doPan = false;   
      }
      
      cursor(cursorMode);
   }
   
   if (keyCode == SHIFT){ 
      clickRotate += 1;
      if ((clickRotate % 2) == 1){
         cursorMode = MOVE;
         PImage[] pressed = {loadImage("images/rotate_down.png"), loadImage("images/rotate_down.png"), loadImage("images/rotate_down.png")};
         if (bt_show.isVisible()){
            cp5.getController("rotate_shrink")
               .setImages(pressed); 
         }else{
            cp5.getController("rotate_normal")
               .setImages(pressed); 
         }
        
      }else{
         cursorMode = ARROW;
         PImage[] released = {loadImage("images/rotate_35x20.png"), loadImage("images/rotate_over.png"), loadImage("images/rotate_down.png")}; 
         if (bt_show.isVisible()){
            cp5.getController("rotate_shrink")
               .setImages(released); 
         }else{
            cp5.getController("rotate_normal")
               .setImages(released); 
         }
         doRotate = false;   
      }
      
      cursor(cursorMode);
   }
}

/* buttons event */

// need this function to call buttons' event
public void controlEvent(ControlEvent theEvent){
   //println(theEvent.getController().getName());
   
}

public void hide(int theValue){
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
   PImage[] pan_released = {loadImage("images/pan_35x20.png"), loadImage("images/pan_over.png"), loadImage("images/pan_down.png")}; 
   cp5.getController("pan_normal")
      .setImages(pan_released);
   cp5.getController("pan_shrink")
      .setImages(pan_released);   
   doPan = false;    

   PImage[] rotate_released = {loadImage("images/rotate_35x20.png"), loadImage("images/rotate_over.png"), loadImage("images/rotate_down.png")}; 
   cp5.getController("rotate_normal")
      .setImages(rotate_released);
   cp5.getController("rotate_shrink")
      .setImages(rotate_released);   
   doRotate = false;   
   
   cursor(cursorMode);
}

public void show(int theValue){
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
   PImage[] pan_released = {loadImage("images/pan_35x20.png"), loadImage("images/pan_over.png"), loadImage("images/pan_down.png")}; 
   cp5.getController("pan_normal")
      .setImages(pan_released);
   doPan = false;    

   PImage[] rotate_released = {loadImage("images/rotate_35x20.png"), loadImage("images/rotate_over.png"), loadImage("images/rotate_down.png")}; 
   cp5.getController("rotate_normal")
      .setImages(rotate_released);
   doRotate = false;
   
   cursor(cursorMode);
}


public void mu(int theValue) {
  contents = new ArrayList<ArrayList<String>>();
  ArrayList<String> line = new ArrayList<String>();
  line.add("1 UTILITIES (NA)");
  contents.add(line);
  line = new ArrayList<String>(); line.add("2 TEST CYCLE (NA)");
  contents.add(line);
  line = new ArrayList<String>(); line.add("3 MANUAL FCTNS (NA)");
  contents.add(line);
  line = new ArrayList<String>(); line.add("4 ALARM (NA)");
  contents.add(line);
  line = new ArrayList<String>(); line.add("5 I/O (NA)");
  contents.add(line);
  line = new ArrayList<String>(); line.add("6 SETUP");
  contents.add(line);
  line = new ArrayList<String>(); line.add("7 FILE (NA)");
  contents.add(line);
  line = new ArrayList<String>(); line.add("8");
  contents.add(line);
  line = new ArrayList<String>(); line.add("9 USER (NA)");
  contents.add(line);
  line = new ArrayList<String>(); line.add("0 --NEXT--");
  contents.add(line);
  active_col = active_row = 0;
  mode = MENU_NAV;
  updateScreen(color(255,0,0), color(0));
}


public void NUM0(int theValue){
   addNumber("0");
}

public void NUM1(int theValue){
   addNumber("1");
}

public void NUM2(int theValue){
   addNumber("2");
}

public void NUM3(int theValue){
   addNumber("3");
}

public void NUM4(int theValue){
   addNumber("4");
}

public void NUM5(int theValue){
   addNumber("5");
}

public void NUM6(int theValue){
   addNumber("6");
}

public void NUM7(int theValue){
   addNumber("7");
}

public void NUM8(int theValue){
   addNumber("8");
}

public void NUM9(int theValue){
   addNumber("9");
}

public void addNumber(String number) {
  if (mode == SET_INSTRUCTION_REGISTER || mode == SET_INSTRUCTION_TERMINATION ||
      mode == JUMP_TO_LINE || mode == SET_DO_BRACKET || mode == SET_RO_BRACKET)
  {
    workingText += number;
    options.set(1, workingText);
    updateScreen(color(255,0,0), color(0,0,0));
  } else if (mode == SET_INSTRUCTION_SPEED) {
    workingText += number;
    options.set(1, workingText + workingTextSuffix);
    updateScreen(color(255,0,0), color(0,0,0));
  } else if (mode == ACTIVE_FRAMES || mode == SET_FRAME_INSTRUCTION) {
    workingText += number;
  }
}

public void PERIOD(int theValue){
   if (NUM_MODE == ON){
      nums.add(-1);
   }
   updateScreen(color(255,0,0), color(0,0,0));
}

public void se(int theValue){
   active_program = 0;
   active_instruction = 0;
   active_row = 0;
   mode = PROGRAM_NAV;
   clearScreen();
   loadPrograms();
   updateScreen(color(255,0,0), color(0,0,0));
}

public void up(int theValue){
   switch (mode){
      case PROGRAM_NAV:
         options = new ArrayList<String>();
         clearOptions();
         if (active_program > 0) {
           active_program--;
           select_program = active_program;
           //saveState(); 
           active_col = 0;
         }
         loadPrograms();
         break;
      case INSTRUCTION_NAV:
         options = new ArrayList<String>();
         clearOptions();
         if (active_instruction > 0) {
           active_instruction--;
           select_instruction = active_instruction;
           active_col = 0;
         }
         loadInstructions(select_program);
         break;
      case INSTRUCTION_EDIT:
      case PICK_FRAME_MODE:
      case PICK_FRAME_METHOD:
      case SET_DO_STATUS:
      case SET_RO_STATUS:
         if (which_option == 0){
            // does nothing
         }else{
            which_option -= 1;
         }
         break;
      case MENU_NAV:
      case SETUP_NAV:
      case NAV_TOOL_FRAMES:
      case NAV_USER_FRAMES:
      case PICK_INSTRUCTION:
      case IO_SUBMENU:
      case SET_FRAME_INSTRUCTION:
      case EDIT_MENU:
         if (active_row > 0) active_row--;
         break;
      case ACTIVE_FRAMES:
         if (active_row > 1) active_row--;
         break;
   }
   
   updateScreen(color(255,0,0), color(0,0,0));
}

public void dn(int theValue){
   switch (mode){
      case PROGRAM_NAV:
         options = new ArrayList<String>();
         clearOptions();
         if (active_program < programs.size()-1) {
           active_program++;
           select_program = active_program;
           //saveState();
           active_col = 0;
         }
         loadPrograms();
         break;
      case INSTRUCTION_NAV:
         options = new ArrayList<String>();
         clearOptions();
         int size = programs.get(select_program).getInstructions().size();
         if (active_instruction < size-1) {
           active_instruction++;
           select_instruction = active_instruction;
           active_col = 0;
         }
         loadInstructions(select_program);
         break;
      case INSTRUCTION_EDIT:
      case PICK_FRAME_MODE:
      case PICK_FRAME_METHOD:
      case SET_DO_STATUS:
      case SET_RO_STATUS:
         if (which_option == options.size() - 1){
            // does nothing
         }else{
            which_option += 1;
         }
         break;
      case MENU_NAV:
      case SETUP_NAV:
      case NAV_TOOL_FRAMES:
      case NAV_USER_FRAMES:
      case ACTIVE_FRAMES:
      case PICK_INSTRUCTION:
      case IO_SUBMENU:
      case SET_FRAME_INSTRUCTION:
      case EDIT_MENU:
         if (active_row < contents.size()-1) active_row++;
         break;
   }  
   updateScreen(color(255,0,0), color(0,0,0));
}

public void lt(int theValue){
   switch (mode){
      case PROGRAM_NAV:
          break;
      case INSTRUCTION_NAV:
          options = new ArrayList<String>();
          clearOptions();
          if (active_col == 0){
              // does nothing
          }else{
              active_col -= 1;
          }
          updateScreen(color(255,0,0), color(0,0,0));
          break;
      case INSTRUCTION_EDIT:
          mode = INSTRUCTION_NAV;
          lt(1);
          break;
   }
   
}


public void rt(int theValue){
  switch (mode){
      case PROGRAM_NAV:
          break;
      case INSTRUCTION_NAV:
          options = new ArrayList<String>();
          clearOptions();
          if (active_col == contents.get(active_row).size()-1){
             // does nothing
          }else{
             active_col += 1;
          }
          updateScreen(color(255,0,0), color(0,0,0));
          break;
      case INSTRUCTION_EDIT:
          mode = INSTRUCTION_NAV;
          rt(1);
          break;
      case MENU_NAV:
          if (active_row == 5) { // SETUP
            contents = new ArrayList<ArrayList<String>>();
            ArrayList<String> line = new ArrayList<String>();
            line.add("1 Prog Select (NA)");
            contents.add(line);
            line = new ArrayList<String>(); line.add("2 General (NA)");
            contents.add(line);
            line = new ArrayList<String>(); line.add("3 Call Guard (NA)");
            contents.add(line);
            line = new ArrayList<String>(); line.add("4 Frames");
            contents.add(line);
            line = new ArrayList<String>(); line.add("5 Macro (NA)");
            contents.add(line);
            line = new ArrayList<String>(); line.add("6 Ref Position (NA)");
            contents.add(line);
            line = new ArrayList<String>(); line.add("7 Port Init (NA)");
            contents.add(line);
            line = new ArrayList<String>(); line.add("8 Ovrd Select (NA)");
            contents.add(line);
            line = new ArrayList<String>(); line.add("9 User Alarm (NA)");
            contents.add(line);
            line = new ArrayList<String>(); line.add("0 --NEXT--");
            contents.add(line);
            active_col = active_row = 0;
            mode = SETUP_NAV;
            updateScreen(color(255,0,0), color(0));
          }
          break;
       case PICK_INSTRUCTION:
          if (active_row == 0) { // I/O
            contents = new ArrayList<ArrayList<String>>();
            ArrayList<String> line = new ArrayList<String>();
            line.add("1 Cell Intface (NA)");
            contents.add(line);
            line = new ArrayList<String>(); line.add("2 Custom (NA)");
            contents.add(line);
            line = new ArrayList<String>(); line.add("3 Digital");
            contents.add(line);
            line = new ArrayList<String>(); line.add("4 Analog (NA)");
            contents.add(line);
            line = new ArrayList<String>(); line.add("5 Group (NA)");
            contents.add(line);
            line = new ArrayList<String>(); line.add("6 Robot");
            contents.add(line);
            line = new ArrayList<String>(); line.add("7 UOP (NA)");
            contents.add(line);
            line = new ArrayList<String>(); line.add("8 SOP (NA)");
            contents.add(line);
            line = new ArrayList<String>(); line.add("9 Interconnect (NA)");
            contents.add(line);
            active_col = active_row = 0;
            mode = IO_SUBMENU;
            updateScreen(color(255,0,0), color(0));
          } else if (active_row == 1) { // Offset/Frames
            contents = new ArrayList<ArrayList<String>>();
            ArrayList<String> line = new ArrayList<String>();
            line.add("1 UTOOL_NUM=()");
            contents.add(line);
            line = new ArrayList<String>(); line.add("2 UFRAME_NUM=()");
            contents.add(line);
            active_col = active_row = 0;
            mode = SET_FRAME_INSTRUCTION;
            workingText="0";
            updateScreen(color(255,0,0), color(0));
          }
          break;
   }
}

//toggle shift state and button highlight
public void sf(int theValue){
   if (shift == OFF){ 
	 shift = ON;
     ((Button)cp5.get("sf")).setColorBackground(color(255, 0, 0));
   }
   else{
     shift = OFF;
     ((Button)cp5.get("sf")).setColorBackground(color(127, 127, 255));
   }
}

public void st(int theValue) {
     if (step == OFF){ 
     step = ON;
     ((Button)cp5.get("st")).setColorBackground(color(255, 0, 0));
   }
   else{
     step = OFF;
     ((Button)cp5.get("st")).setColorBackground(color(127, 127, 255));
   }
}

public void pr(int theValue){
   se(1);
}


public void goToEnterTextMode() {
    clearScreen();
    options = new ArrayList<String>();
    options.add("");
    options.add("(ENTER: confirm name)");
    options.add("");
    options.add("");
    options.add("Program Name:   " + workingText);
    mode = ENTER_TEXT;
    which_option = 0;
    updateScreen(color(0), color(0));
}


public void f1(int theValue){
   switch (mode){
      case PROGRAM_NAV:
         //shift = OFF;
         break;
      case INSTRUCTION_NAV:
         if (shift == OFF) {
           contents = new ArrayList<ArrayList<String>>();
           ArrayList<String> line = new ArrayList<String>();
           line.add("1 I/O");
           contents.add(line);
           line = new ArrayList<String>(); line.add("2 Offset/Frames");
           contents.add(line);
           line = new ArrayList<String>(); line.add("(Others not yet implemented)");
           contents.add(line);
           active_col = active_row = 0;
           mode = PICK_INSTRUCTION;
           updateScreen(color(255,0,0), color(0));
         } else { // shift+f1 = add new motion instruction
           PVector eep = calculateEndEffectorPosition(armModel, false);
           eep = convertNativeToWorld(eep);
           Program prog = programs.get(select_program);
           int reg = prog.nextRegister();
           PVector r = armModel.getWpr();
           float[] j = armModel.getJointRotations();
           prog.addRegister(new Point(eep.x, eep.y, eep.z, r.x, r.y, r.z,
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
           active_instruction = select_instruction = prog.getInstructions().size()-1;
           active_col = 0;
           loadInstructions(select_program);
           active_row = contents.size()-1;
           updateScreen(color(255,0,0), color(0,0,0));
           saveState();
         }
         //shift = OFF;
         break;
      case INSTRUCTION_EDIT:
         //shift = OFF;
         break;
      case ENTER_TEXT:
         /*clearScreen();
         options = new ArrayList<String>();
         options.add("F1: A");
         options.add("F2: B");
         options.add("F3: C");
         options.add("F4: D");
         options.add("F5: E");
         options.add("");
         options.add(workingText);
         letterSet = 1;
         mode = PICK_LETTER;
         which_option = 0;
         updateScreen(color(0), color(0));*/
         break;
      case PICK_LETTER:
         /*switch (letterSet) {
            case 1: workingText += "A"; goToEnterTextMode(); break;
            case 2: workingText += "F"; goToEnterTextMode(); break;
            case 3: workingText += "K"; goToEnterTextMode(); break;
            case 4: workingText += "P"; goToEnterTextMode(); break;
            case 5: break;
            case 6: workingText += "U"; goToEnterTextMode(); break;
            case 7: workingText += "X"; goToEnterTextMode(); break;
         }
         if (letterSet == 5) {
           clearScreen();
           options = new ArrayList<String>();
           options.add("F1: U");
           options.add("F2: V");
           options.add("F3: W");
           options.add("");
           options.add(workingText);
           letterSet = 6;
           mode = PICK_LETTER;
           which_option = 0;
           updateScreen(color(0), color(0));
         }*/
         break;
   }
    
}


public void f2(int theValue) {
  if (mode == PROGRAM_NAV) {
    workingText = "";
    goToEnterTextMode();
  } else if (mode == ENTER_TEXT) {
    /*clearScreen();
    options = new ArrayList<String>();
    options.add("F1: F");
    options.add("F2: G");
    options.add("F3: H");
    options.add("F4: I");
    options.add("F5: J");
    options.add("");
    options.add(workingText);
    letterSet = 2;
    mode = PICK_LETTER;
    which_option = 0;
    updateScreen(color(0), color(0));*/
  } else if (mode == PICK_LETTER) {
    /*switch (letterSet) {
      case 1: workingText += "B"; goToEnterTextMode(); break;
      case 2: workingText += "G"; goToEnterTextMode(); break;
      case 3: workingText += "L"; goToEnterTextMode(); break;
      case 4: workingText += "Q"; goToEnterTextMode(); break;
      case 5: break;
      case 6: workingText += "V"; goToEnterTextMode(); break;
      case 7: workingText += "Y"; goToEnterTextMode(); break;
    }
    if (letterSet == 5) {
      clearScreen();
      options = new ArrayList<String>();
      options.add("F1: X");
      options.add("F2: Y");
      options.add("F3: Z");
      options.add("");
      options.add(workingText);
      letterSet = 7;
      mode = PICK_LETTER;
      which_option = 0;
      updateScreen(color(0), color(0));
    }*/
  } else if (mode == NAV_TOOL_FRAMES || mode == NAV_USER_FRAMES) {
    inFrame = mode;
    if (inFrame == NAV_TOOL_FRAMES) currentFrame = toolFrames[active_row];
    else if (inFrame == NAV_USER_FRAMES) currentFrame = userFrames[active_row];
    loadFrameDetails();
  } else if (mode == FRAME_DETAIL) {
    if (inFrame == NAV_USER_FRAMES) {
      options = new ArrayList<String>();
      options.add("1.Three Point");
      options.add("2.Four Point");
      options.add("3.Direct Entry");
    } else if (inFrame == NAV_TOOL_FRAMES) {
      options = new ArrayList<String>();
      options.add("1.Three Point");
      options.add("2.Six Point");
      options.add("3.Direct Entry");
    }
    mode = PICK_FRAME_METHOD;
    which_option = 0;
    updateScreen(color(255,0,0), color(0));
  }
}


public void f3(int theValue) {
  if (mode == ENTER_TEXT) {
    /*clearScreen();
    options = new ArrayList<String>();
    options.add("F1: K");
    options.add("F2: L");
    options.add("F3: M");
    options.add("F4: N");
    options.add("F5: O");
    options.add("");
    options.add(workingText);
    letterSet = 3;
    mode = PICK_LETTER;
    which_option = 0;
    updateScreen(color(0), color(0));*/
  } else if (mode == PICK_LETTER) {
    /*switch (letterSet) {
      case 1: workingText += "C"; goToEnterTextMode(); break;
      case 2: workingText += "H"; goToEnterTextMode(); break;
      case 3: workingText += "M"; goToEnterTextMode(); break;
      case 4: workingText += "R"; goToEnterTextMode(); break;
      case 5: break;
      case 6: workingText += "W"; goToEnterTextMode(); break;
      case 7: workingText += "Z"; goToEnterTextMode(); break;
    }
    goToEnterTextMode();*/
  } else if (mode == NAV_TOOL_FRAMES || mode == NAV_USER_FRAMES) {
    options = new ArrayList<String>();
    options.add("1.Tool Frame");
    options.add("2.Jog Frame");
    options.add("3.User Frame");
    mode = PICK_FRAME_MODE;
    which_option = 0;
    updateScreen(color(255,0,0), color(0));
  }
}


public void f4(int theValue){
   switch (mode){
      case INSTRUCTION_NAV:
         Instruction ins = programs.get(select_program).getInstructions().get(active_instruction);
         if (ins instanceof MotionInstruction) {
           switch (active_col){
             case 1: // motion type
                options = new ArrayList<String>();
                options.add("1.JOINT");
                options.add("2.LINEAR");
                options.add("3.CIRCULAR");
                //NUM_MODE = ON;
                select_instruction = active_instruction;
                mode = INSTRUCTION_EDIT;
                which_option = 0;
                break;
             case 2: // register type
                options = new ArrayList<String>();
                options.add("1.LOCAL(P)");
                options.add("2.GLOBAL(PR)");
                //NUM_MODE = ON;
                select_instruction = active_instruction;
                mode = INSTRUCTION_EDIT;
                which_option = 0;
                break;
             case 3: // register
                select_instruction = active_instruction;
                options = new ArrayList<String>();
                options.add("Use number keys to enter a register number (0-999)");
                workingText = "";
                options.add(workingText);
                mode = SET_INSTRUCTION_REGISTER;
                which_option = 0;
                break;
             case 4: // speed
                select_instruction = active_instruction;
                options = new ArrayList<String>();
                options.add("Use number keys to enter a new speed");
                MotionInstruction castIns = (MotionInstruction)(programs.get(select_program).getInstructions().get(select_instruction));
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
             case 5: // termination type
                select_instruction = active_instruction;
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
     case ENTER_TEXT:
         /*clearScreen();
         options = new ArrayList<String>();
         options.add("F1: P");
         options.add("F2: Q");
         options.add("F3: R");
         options.add("F4: S");
         options.add("F5: T");
         options.add("");
         options.add(workingText);
         letterSet = 4;
         mode = PICK_LETTER;
         which_option = 0;
         updateScreen(color(0), color(0));*/
         return;
     case PICK_LETTER:
         /*switch (letterSet) {
           case 1: workingText += "D"; goToEnterTextMode(); break;
           case 2: workingText += "I"; goToEnterTextMode(); break;
           case 3: workingText += "N"; goToEnterTextMode(); break;
           case 4: workingText += "S"; goToEnterTextMode(); break;
         }*/
         return;
     case CONFIRM_DELETE:
         Program prog = programs.get(select_program);
         prog.getInstructions().remove(select_instruction);
         if (select_instruction >= prog.getInstructions().size()) {
           select_instruction = active_instruction = prog.getInstructions().size()-1;
         }
         active_row = 0;
         active_col = 0;
         loadInstructions(select_program);
         mode = INSTRUCTION_NAV;
         options.clear();
         updateScreen(color(255,0,0), color(0,0,0));
         break;
   }
   //println("mode="+mode+" active_col"+active_col);
   updateScreen(color(255,0,0), color(0,0,0));
}

public void f5(int theValue) {
  if (mode == INSTRUCTION_NAV) {
    if (shift == OFF) {
      if (active_col == 0) { // if you're on the line number, bring up a list of instruction editing options
        contents = new ArrayList<ArrayList<String>>();
        ArrayList<String> line = new ArrayList<String>();
        line.add("1 Insert (NA)");
        contents.add(line);
        line = new ArrayList<String>(); line.add("2 Delete");
        contents.add(line);
        line = new ArrayList<String>(); line.add("3 Copy (NA)");
        contents.add(line);
        line = new ArrayList<String>(); line.add("4 Find (NA)");
        contents.add(line);
        line = new ArrayList<String>(); line.add("5 Replace (NA)");
        contents.add(line);
        line = new ArrayList<String>(); line.add("6 Renumber (NA)");
        contents.add(line);
        line = new ArrayList<String>(); line.add("7 Comment (NA)");
        contents.add(line);
        line = new ArrayList<String>(); line.add("8 Undo (NA)");
        contents.add(line);
        active_col = active_row = 0;
        mode = EDIT_MENU;
        updateScreen(color(255,0,0), color(0));
      } else if (active_col == 2 || active_col == 3) { // show register contents if you're highlighting a register
        Instruction ins = programs.get(select_program).getInstructions().get(select_instruction);
         if (ins instanceof MotionInstruction) {
         MotionInstruction castIns = (MotionInstruction)ins;
          Point p = castIns.getVector(programs.get(select_program));
          options = new ArrayList<String>();
          options.add("Data of the point in this register (press ENTER to exit):");
          if (castIns.getMotionType() != MTYPE_JOINT) {
            options.add("x: " + p.c.x + "  y: " + p.c.y + "  z: " + p.c.z);
            options.add("w: " + p.a.x + "  p: " + p.a.y + "  r: " + p.a.z);
          } else {
            options.add("j1: " + p.j[0] + "  j2: " + p.j[1] + "  j3: " + p.j[2]);
            options.add("j4: " + p.j[3] + "  j5: " + p.j[4] + "  j6: " + p.j[5]);
          }
          mode = VIEW_REGISTER;
          which_option = 0;
          loadInstructions(select_program);
          updateScreen(color(255,0,0), color(0,0,0));
        }
      }
    } else {
      // overwrite current instruction
      PVector eep = calculateEndEffectorPosition(armModel, false);
      eep = convertNativeToWorld(eep);
      Program prog = programs.get(select_program);
      int reg = prog.nextRegister();
      PVector r = armModel.getWpr();
      float[] j = armModel.getJointRotations();
      prog.addRegister(new Point(eep.x, eep.y, eep.z, r.x, r.y, r.z,
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
      loadInstructions(select_program);
      updateScreen(color(255,0,0), color(0,0,0));
    }
  } else if (mode == ENTER_TEXT) {
      /*clearScreen();
      options = new ArrayList<String>();
      /*options.add("F1: UVW");
      options.add("F2: XYZ");
      options.add("");
      options.add(workingText);
      letterSet = 5;
      mode = PICK_LETTER;
      which_option = 0;
      updateScreen(color(0), color(0));*/
  } else if (mode == PICK_LETTER) {
    /*switch (letterSet) {
      case 1: workingText += "E"; goToEnterTextMode(); break;
      case 2: workingText += "J"; goToEnterTextMode(); break;
      case 3: workingText += "O"; goToEnterTextMode(); break;
      case 4: workingText += "T"; goToEnterTextMode(); break;
    }*/
  } else if (mode == THREE_POINT_MODE) {
    if (shift == ON) {
      if (inFrame == NAV_USER_FRAMES) {
        if (teachingWhichPoint == 1) { // teaching origin
          PVector eep = calculateEndEffectorPosition(armModel, false);
          currentFrame.setOrigin(convertNativeToWorld(eep));
          teachingWhichPoint++;
          loadThreePointMethod();
        } else if (teachingWhichPoint == 2 || teachingWhichPoint == 3) { // x,y axis
          PVector eep = calculateEndEffectorPosition(armModel, false);
          PVector second = convertNativeToWorld(eep);
          PVector first = currentFrame.getOrigin();
          PVector vec = new PVector(second.x-first.x, second.y-first.y, second.z-first.z);
          vec.normalize();
          if (teachingWhichPoint == 2) { // x axis
            currentFrame.setAxis(0, vec);
            teachingWhichPoint++;
            loadThreePointMethod();
          } else { // y axis
            PVector xvec = currentFrame.getAxis(0);
            PVector yvec = computePerpendicular(xvec, vec);
            currentFrame.setAxis(1, yvec.normalize(null));
            currentFrame.setAxis(2, xvec.cross(yvec).normalize(null));
            loadUserFrames();
          }
        }
      } else if (inFrame == NAV_TOOL_FRAMES) {
        pushMatrix();
        applyModelRotation(armModel);
        PVector one = new PVector(modelX(0,0,0), modelY(0,0,0), modelZ(0,0,0));
        translate(0, 0, -100);
        PVector two = new PVector(modelX(0,0,0), modelY(0,0,0), modelZ(0,0,0));
        popMatrix();
        tempPoints[teachingWhichPoint-1] = one;
        teachingPts[teachingWhichPoint-1] = one;
        tempVectors[teachingWhichPoint-1] = new PVector(
          two.x-one.x, two.y-one.y, two.z-one.z).normalize(null);
        if (teachingWhichPoint < 3) {
          teachingWhichPoint++;
          loadThreePointMethod();
        } else {
          // calculate approx. intersection pt. of the provided vectors to get the tool end effector position
          PVector[] last = new PVector[3];
          PVector[] curr = new PVector[3];
          for (int n = 0; n < 3; n++)
            last[n] = new PVector(
              tempPoints[n].x + tempVectors[n].x,
              tempPoints[n].y + tempVectors[n].y,
              tempPoints[n].z + tempVectors[n].z
            );
          float lastDist =
            dist(last[0].x, last[0].y, last[0].z, last[1].x, last[1].y, last[1].z) +
            dist(last[0].x, last[0].y, last[0].z, last[2].x, last[2].y, last[2].z) +
            dist(last[2].x, last[2].y, last[2].z, last[1].x, last[1].y, last[1].z);
          float currentDist = lastDist;
          int iter = 0;
          while (true) {
            iter++;
            if (iter > 10000) { // FAIL CHECK
              loadToolFrames();
              return;
            }
            for (int n = 0; n < 3; n++)
              curr[n] = new PVector(
                last[n].x + tempVectors[n].x,
                last[n].y + tempVectors[n].y,
                last[n].z + tempVectors[n].z
              );
            currentDist =
              dist(curr[0].x, curr[0].y, curr[0].z, curr[1].x, curr[1].y, curr[1].z) +
              dist(curr[0].x, curr[0].y, curr[0].z, curr[2].x, curr[2].y, curr[2].z) +
              dist(curr[2].x, curr[2].y, curr[2].z, curr[1].x, curr[1].y, curr[1].z);
            if (currentDist > lastDist) break; // we've passed each other so we're near the closest approach
            for (int n = 0; n < 3; n++) {
              last[n].x = curr[n].x; last[n].y = curr[n].y; last[n].z = curr[n].z;
              lastDist = currentDist;
            }
          }
          // create a cube of dense points around approx. closest approach and check each one
          PVector approx = new PVector(
              (last[0].x+last[1].x+last[2].x)/3.0,
              (last[0].y+last[1].y+last[2].y)/3.0,
              (last[0].z+last[1].z+last[2].z)/3.0
            );
          float leastDiff = Float.MAX_VALUE;
          PVector closestPt = new PVector(0,0,0);
          for (float x = approx.x-1; x <= approx.x+1; x += 0.05) {
            for (float y = approx.y-1; y <= approx.y+1; y += 0.05) {
              for (float z = approx.z-1; z <= approx.z+1; z += 0.05) {
                float dist1 = dist(x, y, z, tempPoints[0].x, tempPoints[0].y, tempPoints[0].z);
                float dist2 = dist(x, y, z, tempPoints[1].x, tempPoints[1].y, tempPoints[1].z);
                float dist3 = dist(x, y, z, tempPoints[2].x, tempPoints[2].y, tempPoints[2].z);
                float mx = max(dist1, dist2, dist3);
                float mn = min(dist1, dist2, dist3);
                float diff = mx - mn;
                if (diff < leastDiff) {
                  leastDiff = diff;
                  closestPt.x = x; closestPt.y = y; closestPt.z = z;
                }
              }
            }
          }
          // now set the tool frame origin's Z offset as average of closest point's
          // distance from the 3 end effector positions when setting the approaches
          currentFrame.setOrigin(
              new PVector(0, 0,
                -(dist(closestPt.x, closestPt.y, closestPt.z, teachingPts[0].x, teachingPts[0].y, teachingPts[0].z) +
                 dist(closestPt.x, closestPt.y, closestPt.z, teachingPts[1].x, teachingPts[1].y, teachingPts[1].z) +
                 dist(closestPt.x, closestPt.y, closestPt.z, teachingPts[2].x, teachingPts[2].y, teachingPts[2].z))/3.0
                )
            );
          loadToolFrames();
        }
      } // end if inFrame == NAV_TOOL_FRAMES
    }
  } else if (mode == CONFIRM_DELETE) {
         Program prog = programs.get(select_program);
         if (select_instruction >= prog.getInstructions().size()) {
           select_instruction = active_instruction = prog.getInstructions().size()-1;
         }
         active_row = 0;
         active_col = 0;
         loadInstructions(select_program);
         mode = INSTRUCTION_NAV;
         options.clear();
         updateScreen(color(255,0,0), color(0,0,0));
  }
  
}

/* Stops all joint movement */
public void hd(int theValue) {
  STOP_MOVEMENT = true;
  
  for (Model model : armModel.segments) {
    model.jointsMoving[0] = 0;
    model.jointsMoving[1] = 0;
    model.jointsMoving[2] = 0;
    armModel.linearMoveSpeeds[0] = 0;
    armModel.linearMoveSpeeds[1] = 0;
    armModel.linearMoveSpeeds[2] = 0;
  }
}

public void fd(int theValue) {
  if (shift == ON) {
    currentProgram = programs.get(select_program);
    if (step == OFF) readyProgram();
    else {
      Instruction ins = programs.get(active_program).getInstructions().get(active_instruction);
      if (ins instanceof MotionInstruction) {
        singleInstruction = (MotionInstruction)ins;
        setUpInstruction(programs.get(active_program), armModel, singleInstruction);
        if (active_instruction < programs.get(active_program).getInstructions().size()-1)
          select_instruction = active_instruction = (active_instruction+1);
        loadInstructions(select_program);
        updateScreen(color(255,0,0), color(0));
      }
    }
  }
  //shift = OFF;
}

public void bd(int theValue){
  
  if (shift == ON && step == ON && active_instruction > 0) {
    
    currentProgram = programs.get(select_program);
    Instruction ins = programs.get(active_program).getInstructions().get(active_instruction - 1);
    
    if (ins instanceof MotionInstruction) {
      
      singleInstruction = (MotionInstruction)ins;
      setUpInstruction(programs.get(active_program), armModel, singleInstruction);
      
      if (active_instruction > 0)
        select_instruction = active_instruction = (active_instruction-1);
      
      loadInstructions(select_program);
      updateScreen(color(255,0,0), color(0));
    }
  }
}

public void ENTER(int theValue){
   switch (mode){
      case NONE:
         break;
      case PROGRAM_NAV:
         select_program = active_program;
         //saveState();
         select_instruction = active_instruction = 0;
         mode = INSTRUCTION_NAV;
         clearScreen();
         loadInstructions(select_program);
         updateScreen(color(255,0,0), color(0,0,0));
         break;
      case INSTRUCTION_NAV:
         if (active_col == 2 || active_col == 3){
            select_instruction = active_instruction;
            mode = INSTRUCTION_EDIT;
            NUM_MODE = ON;
            num_info.setText(" ");
         }
         break;
      case INSTRUCTION_EDIT:
         Program current_p = programs.get(select_program);
         MotionInstruction m = (MotionInstruction)current_p.getInstructions().get(select_instruction);
         switch (active_col){
            case 1: // motion type
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
            case 2: // register type
               if (which_option == 0) m.setGlobal(false);
               else m.setGlobal(true);
               break;
            case 3: // register
               break;
            case 4: // speed
               break;
            case 5: // termination type
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
         if (speedInPercentage) tempSpeed /= 100.0;
         else if (tempSpeed > armModel.motorSpeed) tempSpeed = armModel.motorSpeed;
         MotionInstruction castIns = (MotionInstruction)(programs.get(select_program).getInstructions().get(select_instruction));
         castIns.setSpeed(tempSpeed);
         loadInstructions(active_program);
         mode = INSTRUCTION_NAV;
         options = new ArrayList<String>();
         which_option = -1;
         clearOptions();
         updateScreen(color(255,0,0), color(0,0,0));
         break;
      case SET_INSTRUCTION_REGISTER:
         int tempRegister = Integer.parseInt(workingText);
         if (tempRegister >= 0 && tempRegister < pr.length) {
           castIns = (MotionInstruction)(programs.get(select_program).getInstructions().get(select_instruction));
           castIns.setRegister(tempRegister);
         }
         loadInstructions(active_program);
         mode = INSTRUCTION_NAV;
         options = new ArrayList<String>();
         which_option = -1;
         clearOptions();
         updateScreen(color(255,0,0), color(0,0,0));
         break;
      case SET_INSTRUCTION_TERMINATION:
         float tempTerm = Float.parseFloat(workingText);
         tempTerm /= 100.0;
         castIns = (MotionInstruction)(programs.get(select_program).getInstructions().get(select_instruction));
         castIns.setTermination(tempTerm);
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
         if (active_instruction >= programs.get(select_program).getInstructions().size())
           active_instruction = programs.get(select_program).getInstructions().size()-1;
         mode = INSTRUCTION_NAV;
         options = new ArrayList<String>();
         which_option = -1;
         clearOptions();
         loadInstructions(select_program);
         updateScreen(color(255,0,0), color(0,0,0));
         break;
      case VIEW_REGISTER:
         mode = INSTRUCTION_NAV;
         options = new ArrayList<String>();
         which_option = -1;
         clearOptions();
         loadInstructions(select_program);
         updateScreen(color(255,0,0), color(0,0,0));
         break;
      case ENTER_TEXT:
         if (workingText.length() > 0) {
           int new_prog = addProgram(new Program(workingText));
           workingText = "";
           select_program = active_program = new_prog;
           select_instruction = active_instruction = 0;
           mode = INSTRUCTION_NAV;
           saveState();
           clearScreen();
           options = new ArrayList<String>();
           loadInstructions(select_program);
           updateScreen(color(255,0,0), color(0,0,0));
         }
         
         break;
      case SETUP_NAV:
         if (active_row == 3) loadToolFrames();
         break;
      case PICK_FRAME_MODE:
         if (which_option == 1) return; // not implemented
         options = new ArrayList<String>();
         clearOptions();
         if (which_option == 0) loadToolFrames();
         else if (which_option == 2) loadUserFrames();
         which_option = -1;
         break;
      case PICK_FRAME_METHOD:
         if (which_option == 1 || which_option == 2) return; // not implemented
         options = new ArrayList<String>();
         which_option = -1;
         teachingWhichPoint = 1;
         loadThreePointMethod();
         break;
      case ACTIVE_FRAMES:
         int num = Integer.parseInt(workingText);
         if (num < 0) num = 0;
         else if (num > userFrames.length) num = userFrames.length;
         if (active_row == 1) activeUserFrame = num-1;
         else if (active_row == 2) ; // jog not implemented
         else if (active_row == 3) activeToolFrame = num-1;
         workingText = "";
         loadActiveFrames();
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
         int bracketNum = Integer.parseInt(workingText);
         Program prog = programs.get(select_program);
         ToolInstruction insert = new ToolInstruction(
            (mode == SET_DO_STATUS ? "DO" : "RO"),
            bracketNum,
            (which_option == 0 ? ON : OFF));
         prog.addInstruction(insert);
         active_instruction = select_instruction = prog.getInstructions().size()-1;
         active_col = 0;
         loadInstructions(select_program);
         active_row = contents.size()-1;
         mode = INSTRUCTION_NAV;
         options.clear();
         updateScreen(color(255,0,0), color(0,0,0));
         saveState();
         break;
      case SET_FRAME_INSTRUCTION:
         num = Integer.parseInt(workingText)-1;
         if (num < -1) num = -1;
         else if (num >= userFrames.length) num = userFrames.length-1;
         prog = programs.get(select_program);
         int type = 0;
         if (active_row == 0) type = FTYPE_TOOL;
         else if (active_row == 1) type = FTYPE_USER;
         prog.addInstruction(new FrameInstruction(type, num));
         active_instruction = select_instruction = prog.getInstructions().size()-1;
         active_col = 0;
         loadInstructions(select_program);
         active_row = contents.size()-1;
         mode = INSTRUCTION_NAV;
         options.clear();
         updateScreen(color(255,0,0), color(0,0,0));
         saveState();
         break;
      case EDIT_MENU:
         if (active_row == 1) { // delete
            options = new ArrayList<String>();
            options.add("Delete this line? F4 = YES, F5 = NO");
            mode = CONFIRM_DELETE;
            which_option = 0;
            updateScreen(color(255,0,0), color(0,0,0));
         }
         break;
   }
}


public void ITEM(int theValue) {
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


public void COORD(int theValue) {
  if (shift == ON) {
    active_row = 1;
    active_col = 0;
    workingText = "";
    loadActiveFrames();
    return;
  }  
  curCoordFrame++;
  if (curCoordFrame > COORD_WORLD) curCoordFrame = COORD_JOINT;
  liveSpeed = 0.1;
}



public void SPEEDUP(int theValue) {
  if (liveSpeed < 0.5) liveSpeed += 0.05;
  else if (liveSpeed < 1) liveSpeed += 0.1;
  if (liveSpeed > 1) liveSpeed = 1;
}


public void SLOWDOWN(int theValue) {
  if (liveSpeed > 0.5) liveSpeed -= 0.1;
  else if (liveSpeed > 0) liveSpeed -= 0.05;
  if (liveSpeed < 0.05) liveSpeed = 0.05;
}


/* navigation buttons */
// zoomin button when interface is at full size
public void zoomin_normal(int theValue){
   myscale *= 1.1;
}

// zoomin button when interface is minimized
public void zoomin_shrink(int theValue){
   myscale *= 1.1;
}

// zoomout button when interface is at full size
public void zoomout_normal(int theValue){
   myscale *= 0.9;
}

// zoomout button when interface is minimized
public void zoomout_shrink(int theValue){
   myscale *= 0.9;
}

// pan button when interface is at full size
public void pan_normal(int theValue){
  clickPan += 1;
  if ((clickPan % 2) == 1){
     cursorMode = HAND;
     PImage[] pressed = {loadImage("images/pan_down.png"), loadImage("images/pan_down.png"), loadImage("images/pan_down.png")};
     cp5.getController("pan_normal")
        .setImages(pressed);   
  }else{
     cursorMode = ARROW;
     PImage[] released = {loadImage("images/pan_35x20.png"), loadImage("images/pan_over.png"), loadImage("images/pan_down.png")}; 
     cp5.getController("pan_normal")
        .setImages(released);
     doPan = false;   
  }
  
  cursor(cursorMode);
}

// pan button when interface is minimized
public void pan_shrink(int theValue){
  clickPan += 1;
  if ((clickPan % 2) == 1){
     cursorMode = HAND;
     PImage[] pressed = {loadImage("images/pan_down.png"), loadImage("images/pan_down.png"), loadImage("images/pan_down.png")};
     cp5.getController("pan_shrink")
        .setImages(pressed);   
  }else{
     cursorMode = ARROW;
     PImage[] released = {loadImage("images/pan_35x20.png"), loadImage("images/pan_over.png"), loadImage("images/pan_down.png")}; 
     cp5.getController("pan_shrink")
        .setImages(released);
     doPan = false;   
  }
  
  cursor(cursorMode);
}

// rotate button when interface is at full size
public void rotate_normal(int theValue){
   clickRotate += 1;
   if ((clickRotate % 2) == 1){
     cursorMode = MOVE;
     PImage[] pressed = {loadImage("images/rotate_down.png"), loadImage("images/rotate_down.png"), loadImage("images/rotate_down.png")};
     cp5.getController("rotate_normal")
        .setImages(pressed);   
  }else{
     cursorMode = ARROW;
     PImage[] released = {loadImage("images/rotate_35x20.png"), loadImage("images/rotate_over.png"), loadImage("images/rotate_down.png")}; 
     cp5.getController("rotate_normal")
        .setImages(released);
     doRotate = false;   
  }
  
  cursor(cursorMode);
}

// rotate button when interface is minized
public void rotate_shrink(int theValue){
   clickRotate += 1;
   if ((clickRotate % 2) == 1){
     cursorMode = MOVE;
     PImage[] pressed = {loadImage("images/rotate_down.png"), loadImage("images/rotate_down.png"), loadImage("images/rotate_down.png")};
     cp5.getController("rotate_shrink")
        .setImages(pressed);   
  }else{
     cursorMode = ARROW;
     PImage[] released = {loadImage("images/rotate_35x20.png"), loadImage("images/rotate_over.png"), loadImage("images/rotate_down.png")}; 
     cp5.getController("rotate_shrink")
        .setImages(released);
     doRotate = false;   
  }
  
  cursor(cursorMode);
}

public void record_normal(int theValue){
   if (record == OFF){
      record = ON;
      PImage[] record = {loadImage("images/record-on.png"), loadImage("images/record-on.png"), loadImage("images/record-on.png")};   
      bt_record_normal.setImages(record);
      new Thread(new RecordScreen()).start();
   }else{
      record = OFF;
      PImage[] record = {loadImage("images/record-35x20.png"), loadImage("images/record-over.png"), loadImage("images/record-on.png")};   
      bt_record_normal.setImages(record);
      
   }
}

public void EE(int theValue){
  activeEndEffector++;
  if (activeEndEffector > ENDEF_CLAW) activeEndEffector = 0;
}

// axis: 0 = -x, 1= +x, 2 = -y, 3 = +y, 4 = -z, 5 = +z
public void activateLiveMotion(int joint, int dir, int axis) {
  if (curCoordFrame == COORD_JOINT) {
    if (armModel.segments.size() >= joint+1) {
      Model model = armModel.segments.get(joint);
      for (int n = 0; n < 3; n++) {
        if (model.rotations[n]) {
          if (model.jointsMoving[n] == 0) model.jointsMoving[n] = dir;
          else model.jointsMoving[n] = 0;
        }
      }
    }
  } else if (curCoordFrame == COORD_WORLD) {
    switch (axis) {
      // account for different axes in native Processing vs. RobotRun world coordinate systems
      case 0: 
        if (armModel.linearMoveSpeeds[0] == 0) armModel.linearMoveSpeeds[0] =  1;
        else armModel.linearMoveSpeeds[0] = 0;
        break;
      case 1:
        if (armModel.linearMoveSpeeds[0] == 0) armModel.linearMoveSpeeds[0] = -1;
        else armModel.linearMoveSpeeds[0] = 0;
        break;
      case 2:
        if (armModel.linearMoveSpeeds[2] == 0) armModel.linearMoveSpeeds[2] = -1;
        else armModel.linearMoveSpeeds[2] = 0;
        break;
      case 3:
        if (armModel.linearMoveSpeeds[2] == 0) armModel.linearMoveSpeeds[2] =  1;
        else armModel.linearMoveSpeeds[2] = 0;
        break;
      case 4:
        if (armModel.linearMoveSpeeds[1] == 0) armModel.linearMoveSpeeds[1] =  1;
        else armModel.linearMoveSpeeds[1] = 0;
        break;
      case 5:
        if (armModel.linearMoveSpeeds[1] == 0) armModel.linearMoveSpeeds[1] = -1;
        else armModel.linearMoveSpeeds[1] = 0;
        break;
    }
  }
} // end activateLiveMotion


public void JOINT1_NEG(int theValue) {
  activateLiveMotion(0, -1, 0);
}

public void JOINT1_POS(int theValue) {
  activateLiveMotion(0, 1, 1);
}

public void JOINT2_NEG(int theValue) {
  activateLiveMotion(1, -1, 2);
}

public void JOINT2_POS(int theValue) {
  activateLiveMotion(1, 1, 3);
}

public void JOINT3_NEG(int theValue) {
  activateLiveMotion(2, -1, 4);
}

public void JOINT3_POS(int theValue) {
  activateLiveMotion(2, 1, 5);
}

public void JOINT4_NEG(int theValue) {
  activateLiveMotion(3, -1, 0);
}

public void JOINT4_POS(int theValue) {
  activateLiveMotion(3, 1, 1);
}

public void JOINT5_NEG(int theValue) {
  activateLiveMotion(4, -1, 2);
}

public void JOINT5_POS(int theValue) {
  activateLiveMotion(4, 1, 3);
}

public void JOINT6_NEG(int theValue) {
  activateLiveMotion(5, -1, 4);
}

public void JOINT6_POS(int theValue) {
  activateLiveMotion(5, 1, 5);
}


// update what displayed on screen
public void updateScreen(color active, color normal){
   int next_px = display_px;
   int next_py = display_py;
   
   if (cp5.getController("-1") != null) cp5.getController("-1").hide();
   
   // display the name of the program that is being edited 
   switch (mode){
      case INSTRUCTION_NAV:
         cp5.addTextlabel("-1")
            .setText(programs.get(select_program).getName())
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
            .setText(programs.get(select_program).getName()) 
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
   for (int i = 0; i < PROGRAMS_TO_SHOW*7; i++) {
     if (cp5.getController(Integer.toString(i)) != null){
           cp5.getController(Integer.toString(i))
              .hide()
              ;
      }
   }

   // display the main list on screen
   index_contents = 0;
   for(int i=0;i<contents.size();i++){
      ArrayList<String> temp = contents.get(i);
      for (int j=0;j<temp.size();j++){
          if (i == active_row && j == active_col){
             cp5.addTextlabel(Integer.toString(index_contents))
                .setText(temp.get(j))
                .setPosition(next_px, next_py)
                .setColorValue(active)
                .moveTo(g1)
                ;
          }else{
             cp5.addTextlabel(Integer.toString(index_contents))
                .setText(temp.get(j))
                .setPosition(next_px, next_py)
                .setColorValue(normal)
                .moveTo(g1)
                ;  
          }
          index_contents++;
          next_px += temp.get(j).length() * 6 + 5; 
      }
      next_px = display_px;
      next_py += 14;
      
      
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
   if (mode == PROGRAM_NAV) {
          fn_info.setText("F2: CREATE")
                 .setPosition(next_px, display_py+display_height-15)
                 .setColorValue(normal)
                 .show()
                 .moveTo(g1)
                 ;
   } else if (mode == INSTRUCTION_NAV) {
          fn_info.setText("SHIFT+F1: NEW PT     F4: CHOICE     F5: VIEW REG     SHIFT+F5: OVERWRITE")
                 .setPosition(next_px, display_py+display_height-15)
                 .setColorValue(normal)
                 .show()
                 .moveTo(g1)
                 ;
   } else if (mode == NAV_TOOL_FRAMES || mode == NAV_USER_FRAMES) {
     fn_info.setText("F2: DETAIL     F3: OTHER")
                 .setPosition(next_px, display_py+display_height-15)
                 .setColorValue(normal)
                 .show()
                 .moveTo(g1)
                 ;
   } else if (mode == FRAME_DETAIL) {
     fn_info.setText("F2: METHOD")
                 .setPosition(next_px, display_py+display_height-15)
                 .setColorValue(normal)
                 .show()
                 .moveTo(g1)
                 ;
   } else if (mode == THREE_POINT_MODE) {
     fn_info.setText("SHIFT+F5: RECORD")
                 .setPosition(next_px, display_py+display_height-15)
                 .setColorValue(normal)
                 .show()
                 .moveTo(g1)
                 ;
   } else {
          fn_info.show()
                 .moveTo(g1)
                 ;
   }
} // end updateScreen()

// clear screen
public void clearScreen(){
   clearContents();
   clearOptions();
   
   // hide the text labels that show the start and end of a program
   if (mode == INSTRUCTION_NAV){
      
   } else if (mode == INSTRUCTION_EDIT){
     
   }else{
      if (cp5.getController("-1") != null){
           cp5.getController("-1")
              .hide()
              ;
      }     
      if (cp5.getController("-2") != null){
           cp5.getController("-2")
              .hide()
              ;   
      }
      fn_info.hide();
   }
   
   clearNums();
   
   cp5.update();
   active_row = 0;
   active_col = 0;
   contents = new ArrayList<ArrayList<String>>();
}

public void clearContents(){
   for(int i=0;i<index_contents;i++){
      cp5.getController(Integer.toString(i)).hide();
   }
   index_contents = 0;
}

public void clearOptions(){
   for(int i=100;i<index_options;i++){
      cp5.getController(Integer.toString(i)).hide();
   }
   index_options = 100;
}

public void clearNums(){
   for(int i=1000;i<index_nums;i++){
      cp5.getController(Integer.toString(i)).hide();
   }
   index_nums = 1000;
}


public void loadToolFrames() {
  contents = new ArrayList<ArrayList<String>>();
  for (int n = 0; n < toolFrames.length; n++) {
    Frame f = toolFrames[n];
    ArrayList<String> line = new ArrayList<String>();
    String str = "";
    str += (n+1);
    str += "   " + "Orig: " + f.getOrigin()/* + "   X: " + f.getAxis(0)*/;
//    str += "   Y: " + f.getAxis(1);
    line.add(str);
    contents.add(line);
  }
  active_col = active_row = 0;
  mode = NAV_TOOL_FRAMES;
  updateScreen(color(255,0,0), color(0));
}


public void loadUserFrames() {
  contents = new ArrayList<ArrayList<String>>();
  for (int n = 0; n < userFrames.length; n++) {
    Frame f = userFrames[n];
    ArrayList<String> line = new ArrayList<String>();
    String str = "";
    str += (n+1);
    str += "   " + "Orig: " + f.getOrigin()/* + "   X: " + f.getAxis(0)*/;
//    str += "   Y: " + f.getAxis(1);
    line.add(str);
    contents.add(line);
  }
  active_col = active_row = 0;
  mode = NAV_USER_FRAMES;
  updateScreen(color(255,0,0), color(0));
}


public void loadThreePointMethod() {
  contents = new ArrayList<ArrayList<String>>();
  
  ArrayList<String> line = new ArrayList<String>();
  String str = "";
  if (inFrame == NAV_USER_FRAMES) {
    if (teachingWhichPoint > 1) str = "Orient Origin Point: RECORDED";
    else str = "Orient Origin Point: UNINIT";
  } else if (inFrame == NAV_TOOL_FRAMES) {
    if (teachingWhichPoint > 1) str = "First Approach Point: RECORDED";
    else str = "First Approach Point: UNINIT";
  }
  line.add(str);
  contents.add(line);
  
  line = new ArrayList<String>();
  if (inFrame == NAV_USER_FRAMES) {
    if (teachingWhichPoint > 2) str = "X Direction Point: RECORDED";
    else str = "X Direction Point: UNINIT";
  } else if (inFrame == NAV_TOOL_FRAMES) {
    if (teachingWhichPoint > 2) str = "Second Approach Point: RECORDED";
    else str = "Second Approach Point: UNINIT";
  }
  line.add(str);
  contents.add(line);
  
  line = new ArrayList<String>();
  if (inFrame == NAV_USER_FRAMES) {
    if (teachingWhichPoint > 3) str = "Y Direction Point: RECORDED";
    else str = "Y Direction Point: UNINIT";
  } else if (inFrame == NAV_TOOL_FRAMES) {
    if (teachingWhichPoint > 3) str = "Third Approach Point: RECORDED";
    else str = "Third Approach Point: UNINIT";
  }
  line.add(str);
  contents.add(line);
  
  mode = THREE_POINT_MODE;
  updateScreen(color(0), color(0));
}


public void loadFrameDetails() {
  contents = new ArrayList<ArrayList<String>>();
  ArrayList<String> line = new ArrayList<String>();
  String str = "";
  if (inFrame == NAV_TOOL_FRAMES) str = "TOOL FRAME " + (active_row+1);
  else if (inFrame == NAV_USER_FRAMES) str = "USER FRAME " + (active_row+1);
  line.add(str);
  contents.add(line);
  
  line = new ArrayList<String>();
  str = "X: " + currentFrame.getOrigin().x;
  line.add(str);
  contents.add(line);
  line = new ArrayList<String>();
  str = "Y: " + currentFrame.getOrigin().y;
  line.add(str);
  contents.add(line);
  line = new ArrayList<String>();
  str = "Z: " + currentFrame.getOrigin().z;
  line.add(str);
  contents.add(line);
  line = new ArrayList<String>();
  str = "W: " + currentFrame.getWpr().x;
  line.add(str);
  contents.add(line);
  line = new ArrayList<String>();
  str = "P: " + currentFrame.getWpr().y;
  line.add(str);
  contents.add(line);
  line = new ArrayList<String>();
  str = "R: " + currentFrame.getWpr().z;
  line.add(str);
  contents.add(line);  
  mode = FRAME_DETAIL;
  updateScreen(color(0), color(0));
}


// prepare for displaying motion instructions on screen
public void loadInstructions(int programID){
   Program p = programs.get(programID);
   contents = new ArrayList<ArrayList<String>>();
   int size = p.getInstructions().size();
   
   int start = active_instruction;
   int end = start + ITEMS_TO_SHOW;
   if (end >= size) end = size;
   for(int i=start;i<end;i++){
      ArrayList<String> m = new ArrayList<String>();
      m.add(Integer.toString(i+1) + ")");
      Instruction instruction = p.getInstructions().get(i);
      if (instruction instanceof MotionInstruction) {
        MotionInstruction a = (MotionInstruction)instruction;
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
      } else if (instruction instanceof ToolInstruction ||
                 instruction instanceof FrameInstruction)
      {
        m.add(instruction.toString());
        contents.add(m);
      }
   } 
}


void loadActiveFrames() {
  options = new ArrayList<String>();
  contents = new ArrayList<ArrayList<String>>();
  ArrayList<String> line = new ArrayList<String>();
  String str = "";
  
  str = "ACTIVE FRAMES";
  line.add(str);
  contents.add(line);
  
  line = new ArrayList<String>();
  str = "User: " + (activeUserFrame+1);
  line.add(str);
  contents.add(line);
  
  line = new ArrayList<String>();
  str = "Jog: " + (activeJogFrame+1);
  line.add(str);
  contents.add(line);
  
  line = new ArrayList<String>();
  str = "Tool: " + (activeToolFrame+1);
  line.add(str);
  contents.add(line);
  
  mode = ACTIVE_FRAMES;
  updateScreen(color(255,0,0), color(0));
}


void loadPrograms() {
   options = new ArrayList<String>(); // clear options
   nums = new ArrayList<Integer>(); // clear numbers
   
   if (cp5.getController("-2") != null) cp5.getController("-2").hide();
   fn_info.setText("");
   
   int size = programs.size();
   /*if (size <= 0){
      programs.add(new Program("My Program 1"));
      saveState();
   }/* */
   
   active_instruction = 0;
   active_row = 0;
   
   contents = new ArrayList<ArrayList<String>>();  
   int start = active_program;
   int end = start + PROGRAMS_TO_SHOW;
   if (end >= size) end = size;
   for(int i=start;i<end;i++){
      ArrayList<String> temp = new ArrayList<String>();
      temp.add(programs.get(i).getName());
      contents.add(temp);
   }
}
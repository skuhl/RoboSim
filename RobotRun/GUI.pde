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
          EDIT_MENU = 30,
          CONFIRM_DELETE = 31;
final int COLOR_DEFAULT = -8421377,
          COLOR_ACTIVE = -65536;
static int     EE_MAPPING = 2;

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
final int ITEMS_TO_SHOW = 16; // how many programs/ instructions to display on screen
int letterSet; // which letter group to enter
Frame currentFrame;
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
int active_row = 0, active_col = 0; 
int text_render_start = 0;
// which option is on focus now?
int which_option = -1; 
// how many textlabels have been created for display
int index_contents = 0, index_options = 100, index_nums = 1000; 
int mouseDown = 0;

private static final boolean DISPLAY_TEST_OUTPUT = true;

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
      .setBackgroundColor(color(127,127,127,50));
   
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
   if (mouseDown == 2){
      panX += mouseX - pmouseX;
      panY += mouseY - pmouseY;
   }
   if (mouseDown == 1 && mouseButton == RIGHT){
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
  // TODO add textarea check for scrolling
  //if (sb != null && sb.focus) {
  //  sb.increment_slider(event.getCount() / 2f);
  //} else {
    // scroll mouse to zoom in / out
    float e = event.getCount();
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
  //}
}

public void mouseReleased() {
  mouseDown -= 1;
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
    //PVector wprP = quatToEuler(q);
    //println("converted values:");
    //println(qP);
    //println(wprP.mult(RAD_TO_DEG));
    //println();
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
  if (mode == INSTRUCTION_NAV || mode == INSTRUCTION_EDIT) { saveState(); }
  
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
      mode == JUMP_TO_LINE || mode == SET_DO_BRACKET || mode == SET_RO_BRACKET)
  {
    workingText += number;
    options.set(1, workingText);
    updateScreen(color(255,0,0), color(0,0,0));
  } else if (mode == SET_INSTRUCTION_SPEED) {
    workingText += number;
    options.set(1, workingText + workingTextSuffix);
    updateScreen(color(255,0,0), color(0,0,0));
  } else if (mode == SET_FRAME_INSTRUCTION) {
    workingText += number;
  } else if (mode == DIRECT_ENTRY_MODE) {
    // TODO add 
  }
}

public void PERIOD() {
   if (NUM_MODE == ON){
      nums.add(-1);
   } else {
     workingText += ".";
   }
   
   updateScreen(color(255,0,0), color(0,0,0));
}

public void LINE() {
  if (workingText != null && workingText.length() > 0) {
    // Mutliply current number by -1
    if (workingText.charAt(0) == '-') {
      workingText = workingText.substring(1);
    } else {
      workingText = "-" + workingText;
    }
    
    updateScreen(color(255,0,0), color(0,0,0));
  }
}

public void se(){
  // Save when exiting a program
   if (mode == INSTRUCTION_NAV || mode == INSTRUCTION_EDIT) { saveState(); }
   
   active_program = 0;
   active_instruction = 0;
   active_row = 0;
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
         if (active_program > 0) {
           if(active_program == text_render_start)
             text_render_start--;
           else
             active_row--;
           active_program--;
           active_col = 0;
         }
         loadPrograms();
         
         if (DISPLAY_TEST_OUTPUT) {
           System.out.printf("\nRow: %d\nColumn: %d\nInst: %d\nTRS: %d\n\n",
                             active_row, active_col, active_program, text_render_start);
         }
         
         break;
      case INSTRUCTION_NAV:
         options = new ArrayList<String>();
         clearOptions();
         if (active_instruction > 0) {
           if(active_instruction == text_render_start)
             text_render_start--; 
           else
             active_row--;
           active_instruction--;
           active_col = 0;
         }
         loadInstructions(active_program);
         
         if (DISPLAY_TEST_OUTPUT) {
           System.out.printf("\nRow: %d\nColumn: %d\nInst: %d\nTRS: %d\n\n",
                             active_row, active_col, active_instruction, text_render_start);
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
         active_row = max(1, active_row - 1);
         break;
   }
   
   updateScreen(color(255,0,0), color(0,0,0));
}

public void dn(){
   switch (mode){
      case PROGRAM_NAV:
         options = new ArrayList<String>();
         clearOptions();
         if (active_program < programs.size()-1) {
           if(active_program - text_render_start == ITEMS_TO_SHOW - 1)
             text_render_start++;
           else
             active_row++;
           
           active_program++;
           active_col = 0;
         }
         loadPrograms();
         
         if (DISPLAY_TEST_OUTPUT) {
           System.out.printf("\nRow: %d\nColumn: %d\nInst: %d\nTRS: %d\n\n",
                             active_row, active_col, active_program, text_render_start);
         }
         
         break;
      case INSTRUCTION_NAV:
         options = new ArrayList<String>();
         clearOptions();
         int size = programs.get(active_program).getInstructions().size();
         if (active_instruction < size-1) {
           if(active_instruction - text_render_start == ITEMS_TO_SHOW - 4)
             text_render_start++;
           else
             active_row++;
           active_instruction++;
           active_col = 0;
         }
         loadInstructions(active_program);
         
         if (DISPLAY_TEST_OUTPUT) {
           System.out.printf("\nRow: %d\nColumn: %d\nInst: %d\nTRS: %d\n\n",
                             active_row, active_col, active_instruction, text_render_start);
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
         which_option = min(which_option + 1, options.size() - 1);
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
         active_row = min(active_row  + 1, contents.size() - 1);
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
          
          updateScreen(color(255,0,0), color(0,0,0));
          break;
      case INSTRUCTION_EDIT:
          mode = INSTRUCTION_NAV;
          rt();
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
    options = new ArrayList<String>();
    options.add("");
    options.add("(ENTER: confirm name)");
    options.add("");
    options.add("");
    options.add("Program Name:   " + workingText);
    super_mode = mode;
    mode = ENTER_TEXT;
    which_option = 0;
    updateScreen(color(0), color(0));
}


public void f1(){
  if (shift == ON) {
    
    if (mode == INSTRUCTION_NAV) {
      PVector eep = armModel.getEEPos();
      eep = convertNativeToWorld(eep);
      Program prog = programs.get(active_program);
      int reg = prog.nextRegister();
      PVector r = armModel.getWPR();
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
        
      active_instruction = prog.getInstructions().size() - 1;
      active_col = 0;
      /* 13 is the maximum number of instructions that can be displayed at one point in time */
      active_row = min(active_instruction, ITEMS_TO_SHOW - 4);
      text_render_start = active_instruction - active_row;
      
      loadInstructions(active_program);
      updateScreen(color(255,0,0), color(0,0,0));
    }
    else if (mode == NAV_TOOL_FRAMES || mode == NAV_USER_FRAMES) {
        
      super_mode = mode;
      if (super_mode == NAV_TOOL_FRAMES) {
        currentFrame = toolFrames[active_row];
      } 
      else if (super_mode == NAV_USER_FRAMES) {
        currentFrame = userFrames[active_row];
      }
        
      loadFrameDetails(false);
    } 
    else if ( mode == ACTIVE_FRAMES) {
      
      if (active_row == 1) {
        loadFrames(COORD_TOOL);
      } 
      else if (active_row == 2) {
        loadFrames(COORD_USER);
      }
      
      updateScreen(color(255,0,0), color(0));
    } 
    else if (mode == THREE_POINT_MODE || mode == SIX_POINT_MODE || mode == FOUR_POINT_MODE) {
      ref_point = null;
    }
    
    return;
  }
  else {
    switch (mode) {
      case PROGRAM_NAV:
        //shift = OFF;
        break;
      case INSTRUCTION_NAV:
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
        //shift = OFF;
        break;
      case NAV_TOOL_FRAMES:
        // Set the current tool frame
        if (active_row >= 0) {
          activeToolFrame = active_row;
        }
        break;
      case NAV_USER_FRAMES:
        // Set the current user frame
        if (active_row >= 0) {
          activeUserFrame = active_row;
        }
        break;
      case INSTRUCTION_EDIT:
        //shift = OFF;
        break;
      case THREE_POINT_MODE:
      case SIX_POINT_MODE:
      case FOUR_POINT_MODE:
        ref_point = armModel.getEEPos();
        break;
    }
  }
}


public void f2() {
  if (shift == ON) {
    
    if (mode == ACTIVE_FRAMES) {
      // Reset the active frames for the User or Tool Coordinate Frames
      if (active_row == 1) {
        activeToolFrame = -1;
      } else if (active_row == 2) {
        activeUserFrame = -1;
      }
      
      loadActiveFrames();
      updateScreen(color(255,0,0), color(0));
    }
  } else {
    
    if (mode == PROGRAM_NAV) {
      workingText = "";
      active_program = -1;
      goToEnterTextMode();
    } else if (mode == FRAME_DETAIL) {
      options = new ArrayList<String>();
      
      if (super_mode == NAV_USER_FRAMES) {
        options.add("1.Three Point");
        options.add("2.Four Point");
        options.add("3.Direct Entry");
      } else if (super_mode == NAV_TOOL_FRAMES) {
        options.add("1.Three Point");
        options.add("2.Six Point");
        options.add("3.Direct Entry");
      }
      mode = PICK_FRAME_METHOD;
      which_option = 0;
      updateScreen(color(255,0,0), color(0));
    } if (mode == NAV_TOOL_FRAMES) {
       
       // Reset the highlighted frame in the tool frame list
       if (active_row >= 0) {
          toolFrames[active_row] = new Frame();
          saveState();
        }
     } else if (mode == NAV_USER_FRAMES) {
       
       // Reset the highlighted frame in the user frames list
       if (active_row >= 0) {
         userFrames[active_row] = new Frame();
         saveState();
       }
     } 
  }
}


public void f3() {
  if (mode == PROGRAM_NAV) {
    int progIdx = active_program;
    if (progIdx >= 0 && progIdx < programs.size()) {
      programs.remove(progIdx);
      
      if (active_program >= programs.size()) {
        active_program = programs.size() - 1;
        /* 13 is the maximum number of instructions that can be displayed at one point in time */
        active_row = min(active_program, ITEMS_TO_SHOW - 4);
        text_render_start = active_program - active_row;
      }
      
      loadPrograms();
      updateScreen(color(255,0,0), color(0));
      saveState();
    }
  } else if ((mode == THREE_POINT_MODE && teachPointTMatrices.size() == 3) ||
        (mode == FOUR_POINT_MODE && teachPointTMatrices.size() == 4) ||
        (mode == SIX_POINT_MODE && teachPointTMatrices.size() == 6)) {
    
    PVector origin = new PVector(0f, 0f, 0f), wpr = new PVector(0f, 0f, 0f);
    float[][] axes = null;
    
    if (mode == THREE_POINT_MODE || mode == SIX_POINT_MODE) {
      // Calculate TCP via the 3-Point Method
      double[] tcp = calculateTCPFromThreePoints(teachPointTMatrices);
      
      if (tcp == null) {
        // Invalid point set
        mode = FRAME_DETAIL;
        which_option = 0;
        loadFrameDetails(true);
        return;
      } else {
        origin = new PVector((float)tcp[0], (float)tcp[1], (float)tcp[2]);
      }
    } else {
      // TODO Four point mode offset
    }
    
    if (mode == FOUR_POINT_MODE || mode == SIX_POINT_MODE) {
      
      ArrayList<float[][]> axesPoints = new ArrayList<float[][]>();
      // Use the last three points to calculate the axes vectors
      if (mode == FOUR_POINT_MODE) {
        axesPoints.add(teachPointTMatrices.get(1));
        axesPoints.add(teachPointTMatrices.get(2));
        axesPoints.add(teachPointTMatrices.get(3));
      } else if (mode == SIX_POINT_MODE) {
        axesPoints.add(teachPointTMatrices.get(3));
        axesPoints.add(teachPointTMatrices.get(4));
        axesPoints.add(teachPointTMatrices.get(5));
      }
      
      axes = createAxesFromThreePoints(axesPoints);
      
      println(matrixToString(axes));
      // TODO actually save the axes ...
      wpr = new PVector(0.0f, 0.0f, 0.0f);
    }
      
    Frame[] frames = null;
    // Determine to which frame set (user or tool) to add the new frame
    if (super_mode == NAV_TOOL_FRAMES) {
      frames = toolFrames;
    } else if (super_mode == NAV_USER_FRAMES) {
      frames = userFrames;
    }
    
    if (frames != null) {
      
      if (active_row >= 0 && active_row < frames.length) {
        if (DISPLAY_TEST_OUTPUT) { System.out.printf("Frame set: %d\n", active_row); }
        
        frames[active_row] = new Frame();
        frames[active_row].setOrigin(origin);
        frames[active_row].setWpr(wpr);
        saveState();
        
        // Set new Frame 
        if (super_mode == NAV_TOOL_FRAMES) {
          activeToolFrame = active_row;
        } else if (super_mode == NAV_USER_FRAMES) {
          activeUserFrame = active_row;
        }
      } else {
        System.out.printf("Error invalid index %d!\n", active_row);
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
}


public void f4() {
   switch (mode){
      case INSTRUCTION_NAV:
         Instruction ins = programs.get(active_program).getInstructions().get(active_instruction);
         if (ins instanceof MotionInstruction) {
           switch (active_col){
             case 1: // motion type
                options = new ArrayList<String>();
                options.add("1.JOINT");
                options.add("2.LINEAR");
                options.add("3.CIRCULAR");
                //NUM_MODE = ON;
                mode = INSTRUCTION_EDIT;
                which_option = 0;
                break;
             case 2: // register type
                options = new ArrayList<String>();
                options.add("1.LOCAL(P)");
                options.add("2.GLOBAL(PR)");
                //NUM_MODE = ON;
                mode = INSTRUCTION_EDIT;
                which_option = 0;
                break;
             case 3: // register
                options = new ArrayList<String>();
                options.add("Use number keys to enter a register number (0-999)");
                workingText = "";
                options.add(workingText);
                mode = SET_INSTRUCTION_REGISTER;
                which_option = 0;
                break;
             case 4: // speed
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
             case 5: // termination type
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
         Program prog = programs.get(active_program);
         prog.getInstructions().remove(active_instruction);
         deleteInstEpilogue();
         break;
   }
   //println("mode="+mode+" active_col"+active_col);
   updateScreen(color(255,0,0), color(0,0,0));
}

public void f5() {
  if (mode == INSTRUCTION_NAV) {
    if (shift == OFF) {
      if (active_col == 0) {
        // if you're on the line number, bring up a list of instruction editing options
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
      } else if (active_col == 2 || active_col == 3) { 
        // show register contents if you're highlighting a register
        Instruction ins = programs.get(active_program).getInstructions().get(active_instruction);
         if (ins instanceof MotionInstruction) {
         MotionInstruction castIns = (MotionInstruction)ins;
          Point p = castIns.getVector(programs.get(active_program));
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
          loadInstructions(active_program);
          updateScreen(color(255,0,0), color(0,0,0));
        }
      }
    } else {
      // overwrite current instruction
      PVector eep = armModel.getEEPos();
      eep = convertNativeToWorld(eep);
      Program prog = programs.get(active_program);
      int reg = prog.nextRegister();
      PVector r = armModel.getWPR();
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
      loadInstructions(active_program);
      updateScreen(color(255,0,0), color(0,0,0));
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
    loadFrameDetails(false);
    mode = limbo;
    loadPointList();
  } else if (mode == CONFIRM_DELETE) {
     deleteInstEpilogue();
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
  println(mode);
  switch (mode){
    case NONE:
       break;
    case PROGRAM_NAV:
       active_instruction = 0;
       text_render_start = 0;
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
       if (tempSpeed >= 5.0) {
         if (speedInPercentage) {
           if (tempSpeed > 100) tempSpeed = 10; 
           tempSpeed /= 100.0;
         } else if (tempSpeed > armModel.motorSpeed) {
           tempSpeed = armModel.motorSpeed;
         }
         MotionInstruction castIns = getActiveMotionInstruct();
         castIns.setSpeed(tempSpeed);
         saveState();
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
         if (tempRegister >= 0 && tempRegister < pr.length) {
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
         mode = INSTRUCTION_NAV;
         super_mode = NONE;
         clearScreen();
         options = new ArrayList<String>();
         loadInstructions(active_program);
         updateScreen(color(255,0,0), color(0,0,0));
       } else {
         mode = super_mode;
         super_mode = NONE;
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
         loadFrameDetails(false);
         mode = THREE_POINT_MODE;
         loadPointList();
       } else if (which_option == 1) {
         which_option = 0;
         teachPointTMatrices = new ArrayList<float[][]>();
         loadFrameDetails(false);
         mode = (super_mode == NAV_TOOL_FRAMES) ? SIX_POINT_MODE : FOUR_POINT_MODE;
         loadPointList();
       } else if (which_option == 2) {
         // TODO direct entry setup
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
       active_row = min(active_instruction, ITEMS_TO_SHOW - 4);
       text_render_start = active_instruction - active_row;
       
       loadInstructions(active_program);
       active_row = contents.size()-1;
       mode = INSTRUCTION_NAV;
       options.clear();
       updateScreen(color(255,0,0), color(0,0,0));
       break;
    case SET_FRAME_INSTRUCTION:
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
      active_row = min(active_instruction, ITEMS_TO_SHOW - 4);
      text_render_start = active_instruction - active_row;
      
      loadInstructions(active_program);
      mode = INSTRUCTION_NAV;
      which_option = -1;
      active_row = 0;
      active_col = 0;
      options.clear();
      updateScreen(color(255,0,0), color(0,0,0));
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
   for (int i = 0; i < ITEMS_TO_SHOW*7; i++) {
     if (cp5.getController(Integer.toString(i)) != null){
           cp5.getController(Integer.toString(i))
              .remove()
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
          fn_info.setText("F2: CREATE     F3: DELETE")
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
     fn_info.setText("F1: SET     SHIFT+F1: DETAIL     F2: RESET")
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
   } else if (mode == THREE_POINT_MODE || mode == FOUR_POINT_MODE || mode == SIX_POINT_MODE) {
     fn_info.setText("F1: SAV REF PT     SHIFT+F1: RMV REF PT     F3: CONFIRM     SHIFT+F5: RECORD")
                 .setPosition(next_px, display_py+display_height-15)
                 .setColorValue(normal)
                 .show()
                 .moveTo(g1)
                 ;
   } else if (mode == ACTIVE_FRAMES) {
     fn_info.setText("SHIFT+F1: LIST     SHIFT+F2: RESET")
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
   if (mode == INSTRUCTION_EDIT){
     
   }
   else if (mode != INSTRUCTION_NAV){
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
   active_row = 0;
   active_col = 0;
   contents = new ArrayList<ArrayList<String>>();
}

public void clearContents(){
   for(int i=0;i<index_contents;i++){
      cp5.getController(Integer.toString(i)).remove();
   }
   index_contents = 0;
}

public void clearOptions(){
   for(int i=100;i<index_options;i++){
      cp5.getController(Integer.toString(i)).remove();
   }
   index_options = 100;
}

public void clearNums(){
   for(int i=1000;i<index_nums;i++){
      cp5.getController(Integer.toString(i)).remove();
   }
   index_nums = 1000;
}

/* Loads the set of Frames that correspond to the given coordinate frame.
 * Only COORD_TOOL and COOR_USER have Frames sets as of now.
 * 
 * @param coorFrame  the integer value representing the coordinate frame
 *                   of te desired frame set
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
    
    for (int idx = 0; idx < frames.length; ++idx) {
      // Display each frame on its own line
      Frame frame = frames[idx];
      ArrayList<String> line = new ArrayList<String>();
      String str = String.format("%d) %s", idx + 1, frame.getOrigin());
      
      line.add(str);
      contents.add(line);
    }
    
    active_col = active_row = 0;
    updateScreen(color(255,0,0), color(0));
  }
}

public void loadPointList() {
  options = new ArrayList<String>();
  
  if (teachPointTMatrices != null) {
  
    ArrayList<String> limbo = new ArrayList<String>();
    
    if ((super_mode == NAV_TOOL_FRAMES && mode == THREE_POINT_MODE) || mode == SIX_POINT_MODE) {
      limbo.add("First Approach Point: ");
      limbo.add("Second Approach Point: ");
      limbo.add("Third Approach Point: ");
    }
    
    if ((super_mode == NAV_USER_FRAMES && mode == THREE_POINT_MODE) || mode == FOUR_POINT_MODE || mode == SIX_POINT_MODE) {
      
      limbo.add("Orient Origin Point: ");
      limbo.add("X Direction Point: ");
      limbo.add("Y Direction Point: ");
    }
    
    if (super_mode == NAV_USER_FRAMES && mode == FOUR_POINT_MODE) {
      limbo.add("TODO: ");
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
        
        System.out.printf("\nA = %d\nB = %d\nC = %d\n\n", idxA, idxB, idxC);
        
        RealMatrix Ar = new Array2DRowRealMatrix(floatToDouble(teachPointTMatrices.get(idxA), 3, 3));
        RealMatrix Br = new Array2DRowRealMatrix(floatToDouble(teachPointTMatrices.get(idxB), 3, 3));
        RealMatrix Cr = new Array2DRowRealMatrix(floatToDouble(teachPointTMatrices.get(idxC), 3, 3));
        
        double [] t = new double[3];
        for (int idx = 0; idx < 3; ++idx) {
          // Build a double from the result of the translation portions of the transformation matrices
          t[idx] = 2 * points.get(idxC)[idx][3] - ( points.get(idxA)[idx][3] + points.get(idxB)[idx][3] );
        }
        
        /* 2Ct - At - Bt */
        RealVector b = new ArrayRealVector(t, false);
        /* Ar + Br - 2Cr */
        RealMatrix R = ( Ar.add(Br) ).subtract( Cr.scalarMultiply(2) );
        
        /*System.out.printf("R:\n%s\n", matrixToString( doubleToFloat(R.getData(), 3, 3) ));
        System.out.printf("t:\n\n[%5.4f]\n[%5.4f]\n[%5.4f]\n\n", b.getEntry(0), b.getEntry(1), b.getEntry(2));*/
        
        /* (R ^ -1) * b */
        avg_TCP = avg_TCP.add( (new SingularValueDecomposition(R)).getSolver().getInverse().operate(b) );
      }
      
      /* Take the average of the three cases: where C = the first point, the second point, and the third point */
      avg_TCP = avg_TCP.mapMultiply( 1.0 / 3.0 );
      
      if (DISPLAY_TEST_OUTPUT) {
        /*for (int pt = 0; pt < 3 && pt < points.size(); ++pt) {
          // Print out each matrix
          System.out.printf("Point %d:\n", pt);
          println( matrixToString(points.get(pt)) );
        }
        
        System.out.printf("(Ar + Br - 2Cr) ^ -1 * (2Ct - At - Bt):\n\n[%5.4f]\n[%5.4f]\n[%5.4f]\n\n", avg_TCP.getEntry(0), avg_TCP.getEntry(1), avg_TCP.getEntry(2));*/
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
 *
 *
 *
 *
 *
 *
 */
public float[][] createAxesFromThreePoints(ArrayList<float[][]> points) {
  // 3 points are necessary for the creation of the axes
  if (points.size() >= 3) {
    float[][] axes = new float[3][];
    float[] x_dir = new float[3],
            y_dir = new float[3];
            
    // From preliminary x and y axis vectors
    for (int row = 0; row < 3; ++row) {
      x_dir[row] = points.get(1)[row][3] - points.get(0)[row][3];
      y_dir[row] = points.get(2)[row][3] - points.get(0)[row][3];
    }
    
    // Form axes
    axes[0] = x_dir;                         // X axis
    axes[2] = crossProduct(x_dir, y_dir);    // Z axis
    axes[1] = crossProduct(x_dir, axes[2]);  // Y axis
    
    return axes;
  }
  
  return null;
}

/* 
 * @param fault  Used to determine whether to print an error message as a
 *               result of an error when calculating the origin in the
 *               3-Point and 6-Point Methods. */
public void loadFrameDetails(boolean fault) {
  contents = new ArrayList<ArrayList<String>>();
 
  ArrayList<String> line = new ArrayList<String>();
  String str = "";
  if (super_mode == NAV_TOOL_FRAMES) {
    str = "TOOL FRAME ";
    if (mode == FRAME_DETAIL) { str += (active_row + 1); }
  } else if (super_mode == NAV_USER_FRAMES) {
    str = "USER FRAME ";
    if (mode == FRAME_DETAIL) { str += (active_row + 1); }
  }
  
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
  
  if (fault) {
    which_option = 0;
    options = new ArrayList<String>();
    options.add("Error: Invalid input values!");
  }
  
  updateScreen(color(255,0,0), color(0));
}

// prepare for displaying motion instructions on screen
public void loadInstructions(int programID){
   Program p = programs.get(programID);
   contents = new ArrayList<ArrayList<String>>();
   int size = p.getInstructions().size();
   
   int start = text_render_start;
   int end = min(start + ITEMS_TO_SHOW - 3, size);
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

/* Deals with updating the UI after confirming/canceling a deletion */
public void deleteInstEpilogue() {
  Program prog = programs.get(active_program);
  
  active_instruction = min(active_instruction,  prog.getInstructions().size() - 1);
  /* 13 is the maximum number of instructions that can be displayed at one point in time */
  active_row = min(active_instruction, ITEMS_TO_SHOW - 4);
  active_col = 0;
  text_render_start = active_instruction - active_row;
  
  loadInstructions(active_program);
  mode = INSTRUCTION_NAV;
  options.clear();
  updateScreen(color(255,0,0), color(0,0,0));
}

void loadActiveFrames() {
  options = new ArrayList<String>();
  contents = new ArrayList<ArrayList<String>>();
  ArrayList<String> line = new ArrayList<String>();
  active_row = 1;
  
  line.add("ACTIVE FRAMES");
  contents.add(line);
  
  line = new ArrayList<String>();
  line.add("Tool: " + (activeToolFrame + 1));
  contents.add(line);
  
  line = new ArrayList<String>();
  line.add("User: " + (activeUserFrame + 1));
  contents.add(line);
  
  // Currently not implemented
  /*line = new ArrayList<String>();
  line.add("Jog:  " + (activeJogFrame + 1));
  contents.add(line);*/
  
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
   for(int i=start;i<end;i++){
      ArrayList<String> temp = new ArrayList<String>();
      temp.add(programs.get(i).getName());
      contents.add(temp);
   }
}
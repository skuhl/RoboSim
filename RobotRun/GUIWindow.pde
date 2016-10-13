/**
 * A extension of ControlP5's ButtonBar object that actually bloody
 * lets you figure out which button is active in a reasonable manner.
 */
public class ButtonTabs extends ButtonBar {
  
  private String selectedButtonName;
  
  public ButtonTabs(ControlP5 parent, String name) {
    super(parent, name);
    selectedButtonName = null;
  }
  
  public void onClick() {
    // Update active button state
    super.onClick();
    
    List items = getItems();
    selectedButtonName = null;
    // Determine which button is active
    for (Object item : items) {
      HashMap map = (HashMap)item;
      Object value = map.get("selected");
      
      if (value instanceof Boolean && (Boolean)value) {
        // Update selectedButtonName
        selectedButtonName = (String)map.get("name");
      }
    }
  }
  
  /**
   * Return the name of the button which is currenty active, or
   * null if no button is active.
   */
  public String getActiveButtonName() {
    return selectedButtonName;
  }
}

/**
 * An extension of the DropdownList class in ControlP5 that allows easier access of
 * the currently selected element's value.
 */
public class MyDropdownList extends DropdownList {
  
  public MyDropdownList( ControlP5 theControlP5 , String theName ) {
    super(theControlP5, theName);
  }

  protected MyDropdownList( ControlP5 theControlP5 , ControllerGroup< ? > theGroup , String theName , int theX , int theY , int theW , int theH ) {
    super( theControlP5 , theGroup , theName , theX , theY , theW , theH );
  }
  
  protected void onRelease() {
    super.onRelease();
    // Some dropdown lists influence the display
    manager.updateWindowContentsPositions();
  }
  
  /**
   * Updates the current active label for the dropdown list to the given
   * label, if it exists in the list.
   */
  public void setActiveLabel(String Elementlabel) {
    Map<String, Object> associatedObjects = getItem( getCaptionLabel().getText() );
    
    if (associatedObjects != null) {
      getCaptionLabel().setText(Elementlabel);
    }
  }
  
  /**
   * Updates the currently active label on the dropdown list based
   * on the current list of items.
   */
  public void updateActiveLabel() {
    Map<String, Object> associatedObjects = getItem( getCaptionLabel().getText() );
    
    if (associatedObjects == null || associatedObjects.isEmpty()) {
      getCaptionLabel().setText( getName() );
    }
  }
  
  /**
   * Returns the value associated with the active label of the Dropdown list.
   */
  public Object getActiveLabelValue() {    
    Map<String, Object> associatedObjects = getItem( getCaptionLabel().getText() );
    
    if (associatedObjects != null) {
      return associatedObjects.get("value");
    }
    
    // You got problems ...
    return null;
  }
  
  /**
   * Deactivates the currently selected option
   * in the Dropdown list.
   */
  public void resetLabel() {
    getCaptionLabel().setText( getName() );
    setValue(0);
  }
}

public class WindowManager {
  private ControlP5 UIManager;
  
  private Group createObjWindow, editObjWindow,
                sharedElements, scenarioWindow;

  private ButtonTabs windowTabs;
  private Button[] cameraViews;
  private Background background;
  
  private Textarea objNameLbl, scenarioNameLbl;
  private Textfield objName, scenarioName;
  
  private ArrayList<Textarea> shapeDefAreas;
  private ArrayList<Textfield> shapeDefFields;
  
  private Textarea[] objOrientationLbls;
  private Textfield[] objOrientationFields;
  
  private Textarea[] dropdownLbls;
  private MyDropdownList[] dropdownLists;
  
  private Button[] miscButtons;
  
  public static final int offsetX = 10,
                          distBtwFieldsY = 15,
                          distLblToFieldX = 5,
                          lLblWidth = 120,
                          mLblWidth = 86,
                          sLblWidth = 60,
                          fieldHeight = 20,
                          fieldWidth = 95,
                          lButtonWidth = 88,
                          mButtonWidth = 56,
                          sButtonWidth = 26,
                          sButtonHeight = 26,
                          tButtonHeight = 20,
                          sdropItemWidth = 80,
                          mdropItemWidth = 90,
                          ldropItemWidth = 120,
                          dropItemHeight = 21;
  
  private final color buttonDefColor, buttonActColor;
  
  /**
   * Creates a new window with the given ControlP5 object as the parent
   * and the given fonts which will be applied to the text in the window.
   */
  public WindowManager(ControlP5 manager, PFont small, PFont medium) {
    // Initialize content fields
    UIManager = manager;
    
    cameraViews = new Button[6];
    objOrientationLbls = new Textarea[6];
    objOrientationFields = new Textfield[6];
    shapeDefAreas = new ArrayList<Textarea>();
    shapeDefFields = new ArrayList<Textfield>();
    dropdownLbls = new Textarea[7];
    dropdownLists = new MyDropdownList[7];
    miscButtons = new Button[8];
    
    buttonDefColor = color(70);
    buttonActColor = color(220, 40, 40);
    
    // Create some temporary color and dimension variables
    color bkgrdColor = color(210),
          fieldTxtColor = color(0),
          fieldCurColor = color(0),
          fieldActColor = color(255, 0, 0),
          fieldBkgrdColor = color(255),
          fieldFrgrdColor = color(0),
          buttonTxtColor = color(255);

    int[] relPos = new int[] { 0, 0 };
    
    String[] windowList = new String[] { "Hide", "Pendant", "Create", "Edit", "Scenario" };
    // Create window tab bar
    windowTabs = (ButtonTabs)(new ButtonTabs(UIManager, "List:")
                  // Sets button text color
                  .setColorValue(buttonTxtColor)
                  .setColorBackground(buttonDefColor)
                  .setColorActive(buttonActColor)
                  .setPosition(relPos[0], relPos[1])
                  .setSize(windowList.length * lButtonWidth, tButtonHeight));
    
    windowTabs.getCaptionLabel().setFont(medium);
    windowTabs.addItems(windowList);
    
    // Initialize camera view buttons
    cameraViews[0] = UIManager.addButton("FrontView")
                              .setCaptionLabel("F")
                              .setColorValue(buttonTxtColor)
                              .setColorBackground(buttonDefColor)
                              .setColorActive(buttonActColor)
                              .moveTo(createObjWindow)
                              .setPosition(0, 0)
                              .setSize(sButtonWidth, sButtonHeight);
    
    cameraViews[1] = UIManager.addButton("BackView")
                              .setCaptionLabel("Bk")
                              .setColorValue(buttonTxtColor)
                              .setColorBackground(buttonDefColor)
                              .setColorActive(buttonActColor)
                              .moveTo(createObjWindow)
                              .setPosition(0, 0)
                              .setSize(sButtonWidth, sButtonHeight);
    
    cameraViews[2] = UIManager.addButton("LeftView")
                              .setCaptionLabel("L")
                              .setColorValue(buttonTxtColor)
                              .setColorBackground(buttonDefColor)
                              .setColorActive(buttonActColor)
                              .moveTo(createObjWindow)
                              .setPosition(0, 0)
                              .setSize(sButtonWidth, sButtonHeight);
    
    cameraViews[3] = UIManager.addButton("RightView")
                              .setCaptionLabel("R")
                              .setColorValue(buttonTxtColor)
                              .setColorBackground(buttonDefColor)
                              .setColorActive(buttonActColor)
                              .moveTo(createObjWindow)
                              .setPosition(0, 0)
                              .setSize(sButtonWidth, sButtonHeight);
    
    cameraViews[4] = UIManager.addButton("TopView")
                              .setCaptionLabel("T")
                              .setColorValue(buttonTxtColor)
                              .setColorBackground(buttonDefColor)
                              .setColorActive(buttonActColor)
                              .moveTo(createObjWindow)
                              .setPosition(0, 0)
                              .setSize(sButtonWidth, sButtonHeight);
    
    cameraViews[5] = UIManager.addButton("BottomView")
                              .setCaptionLabel("Bt")
                              .setColorValue(buttonTxtColor)
                              .setColorBackground(buttonDefColor)
                              .setColorActive(buttonActColor)
                              .moveTo(createObjWindow)
                              .setPosition(0, 0)
                              .setSize(sButtonWidth, sButtonHeight);
    
    
    relPos = relativePosition(windowTabs, RelativePoint.BOTTOM_LEFT, 0, 0);
    background = UIManager.addBackground("WindowBackground").setPosition(relPos[0], relPos[1])
                          .setBackgroundColor(bkgrdColor)
                          .setSize(windowTabs.getWidth(), 0);
    
    // Initialize the groups
    sharedElements = UIManager.addGroup("SHARED").setPosition(relPos[0], relPos[1])
                          .setBackgroundColor(bkgrdColor)
                          .setSize(windowTabs.getWidth(), 0)
                          .hideBar();
    
    createObjWindow = UIManager.addGroup("CREATEOBJ").setPosition(relPos[0], relPos[1])
                               .setBackgroundColor(bkgrdColor)
                               .setSize(windowTabs.getWidth(), 0)
                               .hideBar();
    
    editObjWindow = UIManager.addGroup("EDITOBJ").setPosition(relPos[0], relPos[1])
                             .setBackgroundColor(bkgrdColor)
                             .setSize(windowTabs.getWidth(), 0)
                             .hideBar();
    
    scenarioWindow = UIManager.addGroup("SCENARIO").setPosition(relPos[0], relPos[1])
                          .setBackgroundColor(bkgrdColor)
                          .setSize(windowTabs.getWidth(), 0)
                          .hideBar();
    
    // Initialize window contents
    for (int idx = 0; idx < 5; ++idx) {
      shapeDefAreas.add( UIManager.addTextarea(String.format("Dim%dLbl", idx), String.format("Dim(%d):", idx), 0, 0, mLblWidth, sButtonHeight)
                                  .setFont(medium)
                                  .setColor(fieldTxtColor)
                                  .setColorActive(fieldActColor)
                                  .setColorBackground(bkgrdColor)
                                  .setColorForeground(bkgrdColor)
                                  .moveTo(sharedElements) );
      
      shapeDefFields.add( UIManager.addTextfield(String.format("Dim%d", idx), 0, 0, fieldWidth, fieldHeight)
                                   .setColor(fieldTxtColor)
                                   .setColorCursor(fieldCurColor)
                                   .setColorActive(fieldActColor)
                                   .setColorLabel(bkgrdColor)
                                   .setColorBackground(fieldBkgrdColor)
                                   .setColorForeground(fieldFrgrdColor)
                                   .moveTo(sharedElements) );
    }
    
    dropdownLbls[0] = UIManager.addTextarea("ObjTypeLbl", "Type:", 0, 0, mLblWidth, sButtonHeight)
                         .setFont(medium)
                         .setColor(fieldTxtColor)
                         .setColorActive(fieldActColor)
                         .setColorBackground(bkgrdColor)
                         .setColorForeground(bkgrdColor)
                         .moveTo(createObjWindow);
    
    objNameLbl = UIManager.addTextarea("ObjNameLbl", "Name:", 0, 0, sLblWidth, fieldHeight)
                         .setFont(medium)
                         .setColor(fieldTxtColor)
                         .setColorActive(fieldActColor)
                         .setColorBackground(bkgrdColor)
                         .setColorForeground(bkgrdColor)
                         .moveTo(createObjWindow);
    
    objName = UIManager.addTextfield("ObjName", 0, 0, fieldWidth, fieldHeight)
                       .setColor(fieldTxtColor)
                       .setColorCursor(fieldCurColor)
                       .setColorActive(fieldActColor)
                       .setColorLabel(bkgrdColor)
                       .setColorBackground(fieldBkgrdColor)
                       .setColorForeground(fieldFrgrdColor)
                       .moveTo(createObjWindow);
    
    dropdownLbls[1] = UIManager.addTextarea("ShapeLbl", "Shape:", 0, 0, mLblWidth, sButtonHeight)
                         .setFont(medium)
                         .setColor(fieldTxtColor)
                         .setColorActive(fieldActColor)
                         .setColorBackground(bkgrdColor)
                         .setColorForeground(bkgrdColor)
                         .moveTo(createObjWindow);
    
    dropdownLbls[2] = UIManager.addTextarea("FillLbl", "Fill:", 0, 0, mLblWidth, sButtonHeight)
                         .setFont(medium)
                         .setColor(fieldTxtColor)
                         .setColorActive(fieldActColor)
                         .setColorBackground(bkgrdColor)
                         .setColorForeground(bkgrdColor)
                         .moveTo(createObjWindow);
    
    dropdownLbls[3] = UIManager.addTextarea("OutlineLbl", "Outline:", 0, 0, mLblWidth, sButtonHeight)
                         .setFont(medium)
                         .setColor(fieldTxtColor)
                         .setColorActive(fieldActColor)
                         .setColorBackground(bkgrdColor)
                         .setColorForeground(bkgrdColor)
                         .moveTo(createObjWindow);
    
    miscButtons[0] = UIManager.addButton("CreateWldObj")
                                .setCaptionLabel("Create")
                                .setColorValue(buttonTxtColor)
                                .setColorBackground(buttonDefColor)
                                .setColorActive(buttonActColor)
                                .moveTo(createObjWindow)
                                .setPosition(0, 0)
                                .setSize(mButtonWidth, sButtonHeight);
    
    miscButtons[2] = UIManager.addButton("ClearFields")
                                .setCaptionLabel("Clear")
                                .setColorValue(buttonTxtColor)
                                .setColorBackground(buttonDefColor)
                                .setColorActive(buttonActColor)
                                .moveTo(createObjWindow)
                                .setPosition(0, 0)
                                .setSize(mButtonWidth, sButtonHeight);
    
    dropdownLbls[4] = UIManager.addTextarea("ObjLabel", "Object:", 0, 0, mLblWidth, fieldHeight)
                         .setFont(medium)
                         .setColor(fieldTxtColor)
                         .setColorActive(fieldActColor)
                         .setColorBackground(bkgrdColor)
                         .setColorForeground(bkgrdColor)
                         .moveTo(editObjWindow);
    
    objOrientationLbls[0] = UIManager.addTextarea("XArea", "X:", 0, 0, sLblWidth, fieldHeight)
                         .setFont(medium)
                         .setColor(fieldTxtColor)
                         .setColorActive(fieldActColor)
                         .setColorBackground(bkgrdColor)
                         .setColorForeground(bkgrdColor)
                         .moveTo(editObjWindow);
    
    objOrientationFields[0] = UIManager.addTextfield("XField", 0, 0, fieldWidth, fieldHeight)
                                 .setColor(fieldTxtColor)
                                 .setColorCursor(fieldCurColor)
                                 .setColorActive(fieldActColor)
                                 .setColorLabel(bkgrdColor)
                                 .setColorBackground(fieldBkgrdColor)
                                 .setColorForeground(fieldFrgrdColor)
                                 .moveTo(editObjWindow);
    
    objOrientationLbls[1] = UIManager.addTextarea("YArea", "Y:", 0, 0, sLblWidth, fieldHeight)
                         .setFont(medium)
                         .setColor(fieldTxtColor)
                         .setColorActive(fieldActColor)
                         .setColorBackground(bkgrdColor)
                         .setColorForeground(bkgrdColor)
                         .moveTo(editObjWindow);
    
    objOrientationFields[1] = UIManager.addTextfield("YField", 0, 0, fieldWidth, fieldHeight)
                                 .setColor(fieldTxtColor)
                                 .setColorCursor(fieldCurColor)
                                 .setColorActive(fieldActColor)
                                 .setColorLabel(bkgrdColor)
                                 .setColorBackground(fieldBkgrdColor)
                                 .setColorForeground(fieldFrgrdColor)
                                 .moveTo(editObjWindow);
    
    objOrientationLbls[2] = UIManager.addTextarea("ZArea", "Z:", 0, 0, sLblWidth, fieldHeight)
                          .setFont(medium)
                          .setColor(fieldTxtColor)
                          .setColorActive(fieldActColor)
                          .setColorBackground(bkgrdColor)
                          .setColorForeground(bkgrdColor)
                          .moveTo(editObjWindow);
    
    objOrientationFields[2] = UIManager.addTextfield("ZField", 0, 0, fieldWidth, fieldHeight)
                                 .setColor(fieldTxtColor)
                                 .setColorCursor(fieldCurColor)
                                 .setColorActive(fieldActColor)
                                 .setColorLabel(bkgrdColor)
                                 .setColorBackground(fieldBkgrdColor)
                                 .setColorForeground(fieldFrgrdColor)
                                 .moveTo(editObjWindow);
    
    objOrientationLbls[3] = UIManager.addTextarea("WArea", "W:", 0, 0, sLblWidth, fieldHeight)
                         .setFont(medium)
                         .setColor(fieldTxtColor)
                         .setColorActive(fieldActColor)
                         .setColorBackground(bkgrdColor)
                         .setColorForeground(bkgrdColor)
                         .moveTo(editObjWindow);
    
    objOrientationFields[3] = UIManager.addTextfield("WField", 0, 0, fieldWidth, fieldHeight)
                                 .setColor(fieldTxtColor)
                                 .setColorCursor(fieldCurColor)
                                 .setColorActive(fieldActColor)
                                 .setColorLabel(bkgrdColor)
                                 .setColorBackground(fieldBkgrdColor)
                                 .setColorForeground(fieldFrgrdColor)
                                 .moveTo(editObjWindow);
    
    objOrientationLbls[4] = UIManager.addTextarea("PArea", "P:", 0, 0, sLblWidth, fieldHeight)
                         .setFont(medium)
                         .setColor(fieldTxtColor)
                         .setColorActive(fieldActColor)
                         .setColorBackground(bkgrdColor)
                         .setColorForeground(bkgrdColor)
                         .moveTo(editObjWindow);
    
    objOrientationFields[4] = UIManager.addTextfield("PField", 0, 0, fieldWidth, fieldHeight)
                                 .setColor(fieldTxtColor)
                                 .setColorCursor(fieldCurColor)
                                 .setColorActive(fieldActColor)
                                 .setColorLabel(bkgrdColor)
                                 .setColorBackground(fieldBkgrdColor)
                                 .setColorForeground(fieldFrgrdColor)
                                 .moveTo(editObjWindow);
    
    objOrientationLbls[5] = UIManager.addTextarea("RArea", "R:", 0, 0, sLblWidth, fieldHeight)
                         .setFont(medium)
                         .setColor(fieldTxtColor)
                         .setColorActive(fieldActColor)
                         .setColorBackground(bkgrdColor)
                         .setColorForeground(bkgrdColor)
                         .moveTo(editObjWindow);
    
    objOrientationFields[5] = UIManager.addTextfield("RField", 0, 0, fieldWidth, fieldHeight)
                                 .setColor(fieldTxtColor)
                                 .setColorCursor(fieldCurColor)
                                 .setColorActive(fieldActColor)
                                 .setColorLabel(bkgrdColor)
                                 .setColorBackground(fieldBkgrdColor)
                                 .setColorForeground(fieldFrgrdColor)
                                 .moveTo(editObjWindow);
    
    dropdownLbls[5] = UIManager.addTextarea("FixtureLbl", "Reference:", 0, 0, lLblWidth, sButtonHeight)
                          .setFont(medium)
                          .setColor(fieldTxtColor)
                          .setColorActive(fieldActColor)
                          .setColorBackground(bkgrdColor)
                          .setColorForeground(bkgrdColor)
                          .moveTo(editObjWindow);
    
    miscButtons[1] = UIManager.addButton("UpdateWldObj")
                                .setCaptionLabel("Confirm")
                                .setColorValue(buttonTxtColor)
                                .setColorBackground(buttonDefColor)
                                .setColorActive(buttonActColor)
                                .moveTo(editObjWindow)
                                .setSize(mButtonWidth, sButtonHeight);
    
    miscButtons[3] = UIManager.addButton("DeleteWldObj")
                                .setCaptionLabel("Delete")
                                .setColorValue(buttonTxtColor)
                                .setColorBackground(buttonDefColor)
                                .setColorActive(buttonActColor)
                                .moveTo(editObjWindow)
                                .setSize(mButtonWidth, sButtonHeight);
    
    scenarioNameLbl = UIManager.addTextarea("NewScenarioLbl", "Name:", 0, 0, sLblWidth, fieldHeight)
                          .setFont(medium)
                          .setColor(fieldTxtColor)
                          .setColorActive(fieldActColor)
                          .setColorBackground(bkgrdColor)
                          .setColorForeground(bkgrdColor)
                          .moveTo(scenarioWindow);
    
    scenarioName = UIManager.addTextfield("ScenarioName", 0, 0, fieldWidth, fieldHeight)
                            .setColor(fieldTxtColor)
                            .setColorCursor(fieldCurColor)
                            .setColorActive(fieldActColor)
                            .setColorLabel(bkgrdColor)
                            .setColorBackground(fieldBkgrdColor)
                            .setColorForeground(fieldFrgrdColor)
                            .moveTo(scenarioWindow);
    
    miscButtons[4] = UIManager.addButton("NewScenario")
                                .setCaptionLabel("New")
                                .setColorValue(buttonTxtColor)
                                .setColorBackground(buttonDefColor)
                                .setColorActive(buttonActColor)
                                .moveTo(scenarioWindow)
                                .setSize(mButtonWidth, sButtonHeight);
    
    dropdownLbls[6] = UIManager.addTextarea("ActiveScenarioLbl", "Scenario:", 0, 0, lLblWidth, sButtonHeight)
                          .setFont(medium)
                          .setColor(fieldTxtColor)
                          .setColorActive(fieldActColor)
                          .setColorBackground(bkgrdColor)
                          .setColorForeground(bkgrdColor)
                          .moveTo(scenarioWindow);
    
    miscButtons[5] = UIManager.addButton("SaveScenario")
                                .setCaptionLabel("Save")
                                .setColorValue(buttonTxtColor)
                                .setColorBackground(buttonDefColor)
                                .setColorActive(buttonActColor)
                                .moveTo(scenarioWindow)
                                .setSize(mButtonWidth, sButtonHeight);
    
    miscButtons[6] = UIManager.addButton("SetScenario")
                                .setCaptionLabel("Load")
                                .setColorValue(buttonTxtColor)
                                .setColorBackground(buttonDefColor)
                                .setColorActive(buttonActColor)
                                .moveTo(scenarioWindow)
                                .setSize(mButtonWidth, sButtonHeight);
    
    miscButtons[7] = UIManager.addButton("HideObjects")
                                .setCaptionLabel("Hide")
                                .setColorValue(buttonTxtColor)
                                .setColorBackground(buttonDefColor)
                                .setColorActive(buttonActColor)
                                .moveTo(scenarioWindow)
                                .setSize(mButtonWidth, sButtonHeight);
    
    // Initialize dropdown lists
   dropdownLists[6] = (MyDropdownList)((new MyDropdownList( UIManager, "Scenario"))
                        .setSize(ldropItemWidth, 4 * dropItemHeight)
                        .setBarHeight(dropItemHeight)
                        .setItemHeight(dropItemHeight)
                        .setColorValue(buttonTxtColor)
                        .setColorBackground(buttonDefColor)
                        .setColorActive(buttonActColor)
                        .moveTo(scenarioWindow)
                        .close());
                      
   dropdownLists[5] = (MyDropdownList)((new MyDropdownList( UIManager, "Fixture"))
                        .setSize(ldropItemWidth, 4 * dropItemHeight)
                        .setBarHeight(dropItemHeight)
                        .setItemHeight(dropItemHeight)
                        .setColorValue(buttonTxtColor)
                        .setColorBackground(buttonDefColor)
                        .setColorActive(buttonActColor)
                        .moveTo(editObjWindow)
                        .close());
   
   dropdownLists[4] = (MyDropdownList)((new MyDropdownList( UIManager, "Object"))
                        .setSize(ldropItemWidth, 4 * dropItemHeight)
                        .setBarHeight(dropItemHeight)
                        .setItemHeight(dropItemHeight)
                        .setColorValue(buttonTxtColor)
                        .setColorBackground(buttonDefColor)
                        .setColorActive(buttonActColor)
                        .moveTo(editObjWindow)
                        .close());
     
    dropdownLists[3] = (MyDropdownList)((new MyDropdownList( UIManager, "Outline"))
                        .setSize(sdropItemWidth, sButtonHeight + 3 * dropItemHeight)
                        .setBarHeight(dropItemHeight)
                        .setItemHeight(dropItemHeight)
                        .setColorValue(buttonTxtColor)
                        .setColorBackground(buttonDefColor)
                        .setColorActive(buttonActColor)
                        .moveTo(createObjWindow)
                        .close());
    
    dropdownLists[3].addItem("black", color(0));
    dropdownLists[3].addItem("red", color(255, 0, 0));
    dropdownLists[3].addItem("green", color(0, 255, 0));
    dropdownLists[3].addItem("blue", color(0, 0, 255));
    dropdownLists[3].addItem("orange", color(255, 60, 0));
    dropdownLists[3].addItem("yellow", color(255, 255, 0));
    dropdownLists[3].addItem("pink", color(255, 0, 255));
    dropdownLists[3].addItem("purple", color(90, 0, 255));
    
    
    dropdownLists[2] = (MyDropdownList)((new MyDropdownList( UIManager, "Fill"))
                        .setSize(mdropItemWidth, 4 * dropItemHeight)
                        .setBarHeight(dropItemHeight)
                        .setItemHeight(dropItemHeight)
                        .setColorValue(buttonTxtColor)
                        .setColorBackground(buttonDefColor)
                        .setColorActive(buttonActColor)
                        .moveTo(createObjWindow)
                        .close());
   
    dropdownLists[2].addItem("white", color(255));
    dropdownLists[2].addItem("black", color(0));
    dropdownLists[2].addItem("red", color(255, 0, 0));
    dropdownLists[2].addItem("green", color(0, 255, 0));
    dropdownLists[2].addItem("blue", color(0, 0, 255));
    dropdownLists[2].addItem("orange", color(255, 60, 0));
    dropdownLists[2].addItem("yellow", color(255, 255, 0));
    dropdownLists[2].addItem("pink", color(255, 0, 255));
    dropdownLists[2].addItem("purple", color(90, 0, 255));
    dropdownLists[2].addItem("sky blue", color(0, 255, 255));
    dropdownLists[2].addItem("dark green", color(0, 100, 15));
   
   dropdownLists[1] = (MyDropdownList)((new MyDropdownList( UIManager, "Shape"))
                       .setSize(sdropItemWidth, 4 * dropItemHeight)
                       .setBarHeight(dropItemHeight)
                       .setItemHeight(dropItemHeight)
                       .setColorValue(buttonTxtColor)
                       .setColorBackground(buttonDefColor)
                       .setColorActive(buttonActColor)
                       .moveTo(createObjWindow)
                       .close());
                         
   dropdownLists[1].addItem("Box", ShapeType.BOX);
   dropdownLists[1].addItem("Cylinder", ShapeType.CYLINDER);
   dropdownLists[1].addItem("Import", ShapeType.MODEL);
   
   dropdownLists[0] = (MyDropdownList)((new MyDropdownList( UIManager, "ObjType"))
                       .setSize(sdropItemWidth, 3 * dropItemHeight)
                       .setBarHeight(dropItemHeight)
                       .setItemHeight(dropItemHeight)
                       .setColorValue(buttonTxtColor)
                       .setColorBackground(buttonDefColor)
                       .setColorActive(buttonActColor)
                       .moveTo(createObjWindow)
                       .close());
     
   dropdownLists[0].addItem("Parts", 0.0);
   dropdownLists[0].addItem("Fixtures", 1.0);
   
   // Set fonts for buttons and dropdown lists
   for (Button b : cameraViews) {
     b.getCaptionLabel().setFont(small);
     b.hide();
   }
   
   for (Button b : miscButtons) {
     b.getCaptionLabel().setFont(small);
   }
   
   for (DropdownList list : dropdownLists) {
     list.getCaptionLabel().setFont(small);
   }
  }
  
  /**
   * Updates the current active window display based on the selected button on
   * windowTabs. Due to some problems with hiding groups with the ControlP5
   * object, when a new window is brought up a large white sphere is drawn oer
   * the screen to clear the image of the previous window.
   */
  public void updateWindowDisplay() {
    String windowState = windowTabs.getActiveButtonName();
    
    if (windowState == null || windowState.equals("Hide")) {
      // Hide any window
      g1.hide();
      setGroupVisible(createObjWindow, false);
      setGroupVisible(editObjWindow, false);
      setGroupVisible(sharedElements, false);
      setGroupVisible(scenarioWindow, false);
      
      updateWindowContentsPositions();
      
    } else if (windowState.equals("Pendant")) {
      // Show pendant
      setGroupVisible(createObjWindow, false);
      setGroupVisible(editObjWindow, false);
      setGroupVisible(sharedElements, false);
      setGroupVisible(scenarioWindow, false);
      
      if (!g1.isVisible()) {
        updateWindowContentsPositions();
      }
      
      g1.show();
      
    } else if (windowState.equals("Create")) {
      // Show world object creation window
      g1.hide();
      setGroupVisible(editObjWindow, false);
      setGroupVisible(scenarioWindow, false);
      
      if (!createObjWindow.isVisible()) {
        setGroupVisible(createObjWindow, true);
        setGroupVisible(sharedElements, true);
        
        clearAllInputFields();
        updateWindowContentsPositions();
        updateListContents();
        resetListLabels();
      }
      
    } else if (windowState.equals("Edit")) {
      // Show world object edit window
      g1.hide();
      setGroupVisible(createObjWindow, false);
      setGroupVisible(scenarioWindow, false);
      
      if (!editObjWindow.isVisible()) {
        setGroupVisible(editObjWindow, true);
        setGroupVisible(sharedElements, true);
        
        clearAllInputFields();
        updateWindowContentsPositions();
        updateListContents();
        resetListLabels();
      }
      
    } else if (windowState.equals("Scenario")) {
      // Show scenario creating/saving/loading
      g1.hide();
      setGroupVisible(createObjWindow, false);
      setGroupVisible(editObjWindow, false);
      
      if (!scenarioWindow.isVisible()) {
        setGroupVisible(scenarioWindow, true);
        
        clearAllInputFields();
        updateWindowContentsPositions();
        updateListContents();
        resetListLabels();
      }
    }
  }
  
  /**
   * Updates the positions of all the elements in the active window
   * based on the current button tab that is active.
   */
  public void updateWindowContentsPositions() {
    String windowState = windowTabs.getActiveButtonName();
    
    if (windowState == null || windowState.equals("Hide")) {
      // Window is hidden
      background.hide();
      for (Button b : cameraViews) {
        b.hide();
      }
      
      return;
      
    } else if (windowState.equals("Create")) {
      // Create window
      updateCreateWindowContentPositions();
      
    } else if (windowState.equals("Edit")) {
      // Edit window
      updateEditWindowContentPositions();
      
    } else if (windowState.equals("Scenario")) {
      // Scenario window
      updateScenarioWindowContentPositions();
    }
    
    // Update the camera view buttons
    int[] relPos = relativePosition(windowTabs, RelativePoint.BOTTOM_RIGHT, offsetX, 0);
    
    for (Button b : cameraViews) {  
      b.setPosition(relPos[0], relPos[1]).show();
      relPos = relativePosition(b, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
    }
    
    updateListContents();
  }
  
  /**
   * Updates the positions of all the contents of the world object creation window.
   */
  private void updateCreateWindowContentPositions() {
    updateDimLblsAndFields();
    
    // Object Type dropdown list and label
    int[] relPos = new int[] { offsetX, offsetX };
    dropdownLbls[0] = dropdownLbls[0].setPosition(relPos[0], relPos[1]);
    
    relPos = relativePosition(dropdownLbls[0], RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
    dropdownLists[0] = (MyDropdownList)dropdownLists[0].setPosition(relPos[0], relPos[1]);
    // Name label and field
    relPos = relativePosition(dropdownLbls[0], RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
    objNameLbl = objNameLbl.setPosition(relPos[0], relPos[1]);
    
    relPos = relativePosition(objNameLbl, RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
    objName = objName.setPosition(relPos[0], relPos[1]);
    // Shape type label and dropdown
    relPos = relativePosition(objNameLbl, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
    dropdownLbls[1] = dropdownLbls[1].setPosition(relPos[0], relPos[1]);
    
    relPos = relativePosition(dropdownLbls[1], RelativePoint.TOP_RIGHT, distLblToFieldX, abs(fieldHeight - dropItemHeight) / 2);
    dropdownLists[1] = (MyDropdownList)dropdownLists[1].setPosition(relPos[0], relPos[1]);
    // Dimension label and fields
    relPos = relativePosition(dropdownLbls[1], RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
    relPos = updateDimLblAndFieldPositions(relPos[0], relPos[1]);
    
    // Fill color label and dropdown
    dropdownLbls[2] = dropdownLbls[2].setPosition(relPos[0], relPos[1]);
    
    relPos = relativePosition(dropdownLbls[2], RelativePoint.TOP_RIGHT, distLblToFieldX, abs(fieldHeight - dropItemHeight) / 2);
    dropdownLists[2] = (MyDropdownList)dropdownLists[2].setPosition(relPos[0], relPos[1]);
    
    relPos = relativePosition(dropdownLbls[2], RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
    Object val = dropdownLists[1].getActiveLabelValue();
    
    if (val == ShapeType.MODEL) {
      // No stroke color for Model Shapes
      dropdownLbls[3] = dropdownLbls[3].hide();
      dropdownLists[3] = (MyDropdownList)dropdownLists[3].hide();
      
    } else {
      // Outline color label and dropdown
      dropdownLbls[3] = dropdownLbls[3].setPosition(relPos[0], relPos[1]).show();
      relPos = relativePosition(dropdownLbls[3], RelativePoint.TOP_RIGHT, distLblToFieldX, abs(fieldHeight - dropItemHeight) / 2);
      
      dropdownLists[3] = (MyDropdownList)dropdownLists[3].setPosition(relPos[0], relPos[1]).show();
      relPos = relativePosition(dropdownLbls[3], RelativePoint.BOTTOM_RIGHT, distLblToFieldX, distBtwFieldsY);
    } 

    // Create button
    miscButtons[0] = miscButtons[0].setPosition(relPos[0], relPos[1]);
    // Clear button
    relPos = relativePosition(miscButtons[0], RelativePoint.TOP_RIGHT, offsetX, 0);
    miscButtons[2] = miscButtons[2].setPosition(relPos[0], relPos[1]);
    // Update window background display
    relPos = relativePosition(miscButtons[2], RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
    background.setBackgroundHeight(relPos[1])
              .setHeight(relPos[1])
              .show();
  }
  
  /**
   * Updates the positions of all the contents of the world object editing window.
   */
  private void updateEditWindowContentPositions() {
    updateDimLblsAndFields();
    
    // Object list dropdown and label
    int[] relPos = new int[] { offsetX, offsetX };
    dropdownLbls[4] = dropdownLbls[4].setPosition(relPos[0], relPos[1]);
    
    relPos = relativePosition(dropdownLbls[4], RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
    dropdownLists[4] = (MyDropdownList)dropdownLists[4].setPosition(relPos[0], relPos[1]);
    // Dimension label and fields
    relPos = relativePosition(dropdownLbls[4], RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
    relPos = updateDimLblAndFieldPositions(relPos[0], relPos[1]);
    
    // X label and field
    objOrientationLbls[0] = objOrientationLbls[0].setPosition(relPos[0], relPos[1]);
    
    relPos = relativePosition(objOrientationLbls[0], RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
    objOrientationFields[0] = objOrientationFields[0].setPosition(relPos[0], relPos[1]);
    // Y label and field
    relPos = relativePosition(objOrientationLbls[0], RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
    objOrientationLbls[1] = objOrientationLbls[1].setPosition(relPos[0], relPos[1]);
    
    relPos = relativePosition(objOrientationLbls[1], RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
    objOrientationFields[1] = objOrientationFields[1].setPosition(relPos[0], relPos[1]);
    // Z label and field
    relPos = relativePosition(objOrientationLbls[1], RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
    objOrientationLbls[2] = objOrientationLbls[2].setPosition(relPos[0], relPos[1]);;
    
    relPos = relativePosition(objOrientationLbls[2], RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
    objOrientationFields[2] = objOrientationFields[2].setPosition(relPos[0], relPos[1]);
    // W label and field
    relPos = relativePosition(objOrientationLbls[2], RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
    objOrientationLbls[3] = objOrientationLbls[3].setPosition(relPos[0], relPos[1]);
    
    relPos = relativePosition(objOrientationLbls[3], RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
    objOrientationFields[3] = objOrientationFields[3].setPosition(relPos[0], relPos[1]);
    // P label and field
    relPos = relativePosition(objOrientationLbls[3], RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
    objOrientationLbls[4] = objOrientationLbls[4].setPosition(relPos[0], relPos[1]);
    
    relPos = relativePosition(objOrientationLbls[4], RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
    objOrientationFields[4] = objOrientationFields[4].setPosition(relPos[0], relPos[1]);
    // R label and field
    relPos = relativePosition(objOrientationLbls[4], RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
    objOrientationLbls[5] = objOrientationLbls[5].setPosition(relPos[0], relPos[1]);
    
    relPos = relativePosition(objOrientationLbls[5], RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
    objOrientationFields[5] = objOrientationFields[5].setPosition(relPos[0], relPos[1]);
   
    relPos = relativePosition(objOrientationLbls[5], RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
    
    if (getActiveWorldObject() instanceof Part) {
       // Reference fxiture (for Parts only) label and dropdown
      dropdownLbls[5] = dropdownLbls[5].setPosition(relPos[0], relPos[1]).show();
      relPos = relativePosition(dropdownLbls[5], RelativePoint.TOP_RIGHT, distLblToFieldX, abs(fieldHeight - dropItemHeight) / 2);
      
      dropdownLists[5] = (MyDropdownList)dropdownLists[5].setPosition(relPos[0], relPos[1]).show();
      relPos = relativePosition(dropdownLbls[5], RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
      
    } else {
      // Fixtures do not have a reference object
      dropdownLbls[5].hide();
      dropdownLists[5] = (MyDropdownList)dropdownLists[5].hide();
    }
    
    // Confirm button
    miscButtons[1] = miscButtons[1].setPosition(relPos[0], relPos[1]);
    // Delete button
    relPos = relativePosition(miscButtons[1], RelativePoint.TOP_RIGHT, offsetX, 0);
    miscButtons[3] = miscButtons[3].setPosition(relPos[0], relPos[1]);
    // Update window background display
    relPos = relativePosition(miscButtons[3], RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
    background.setBackgroundHeight(relPos[1])
              .setHeight(relPos[1])
              .show();
  }
  
  /**
   * Updates the positions of all the contents of the scenario window.
   */
  private void updateScenarioWindowContentPositions() {
    // New scenario name label
    int[] relPos = new int[] {offsetX, offsetX };
    scenarioNameLbl = scenarioNameLbl.setPosition(relPos[0], relPos[1]);
    // New scenario name field
    relPos = relativePosition(scenarioNameLbl, RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
    scenarioName = scenarioName.setPosition(relPos[0], relPos[1]);
    // New scenario button
    relPos = relativePosition(scenarioNameLbl, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
    miscButtons[4] = miscButtons[4].setPosition(relPos[0], relPos[1]);
    // Scenario dropdown list and label
    relPos = relativePosition(miscButtons[4], RelativePoint.BOTTOM_LEFT, 0, 2 * distBtwFieldsY);
    dropdownLbls[6] = dropdownLbls[6].setPosition(relPos[0], relPos[1]);
    
    relPos = relativePosition(dropdownLbls[6], RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
    dropdownLists[6] = (MyDropdownList)dropdownLists[6].setPosition(relPos[0], relPos[1]);
    // Save scenario button
    relPos = relativePosition(dropdownLbls[6], RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
    miscButtons[5] = miscButtons[5].setPosition(relPos[0], relPos[1]);
    // Load scenario button
    relPos = relativePosition(miscButtons[5], RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
    miscButtons[6] = miscButtons[6].setPosition(relPos[0], relPos[1]);
    // Toggle Object display button
    relPos = relativePosition(miscButtons[6], RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
    miscButtons[7] = miscButtons[7].setPosition(relPos[0], relPos[1]);
    
    // Update button color based on the value of the object display flag
    if (!showOOBs) {
      miscButtons[7].setColorBackground(buttonActColor);
      
    } else {
      miscButtons[7].setColorBackground(buttonDefColor);
    }
    
    // Update window background display
    relPos = relativePosition(miscButtons[7], RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
    background.setBackgroundHeight(relPos[1])
              .setHeight(relPos[1])
              .show();
  }
  
  /**
   * Updates positions of all the visible dimension text areas and fields. The given x and y positions are used to
   * place the first text area and field pair and updated through the process of updating the positions of the rest
   * of the visible text areas and fields. Then the x and y position of the last visible text area and field is returned
   * in the form a 2-element integer array.
   * 
   * @param initialXPos  The x position of the first text area-field pair
   * @param initialYPos  The y position of the first text area-field pair
   * @returning          The x and y position of the last visible text area  in a 2-element integer array
   */
  private int[] updateDimLblAndFieldPositions(int initialXPos, int initialYPos) {
    int[] relPos = new int[] { initialXPos, initialYPos };
    int idxDim = 0;
    
    // Update position and label text of the dimension fields based on the selected shape from the Shape dropDown List
    while (idxDim < shapeDefFields.size()) {
      Textfield dimField = shapeDefFields.get(idxDim);
      
      if (!dimField.isVisible()) { break; }
      
      Textarea dimLbl = shapeDefAreas.get(idxDim);
      shapeDefAreas.set(idxDim, dimLbl.setPosition(relPos[0], relPos[1]) );
      relPos = relativePosition(dimLbl, RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
      
      shapeDefFields.set(idxDim, dimField.setPosition(relPos[0], relPos[1]) );
      relPos = relativePosition(dimLbl, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
      
      ++idxDim;
    }
    
    return relPos;
  }
  
  /**
   * Returns a position that is relative to the dimensions and position of the Controller object given.
   */
  private <T> int[] relativePosition(ControllerInterface<T> obj, RelativePoint pos, int offsetX, int offsetY) {
    int[] relPosition = new int[] { 0, 0 };
    float[] objPosition = obj.getPosition();
    float[] objDimensions;
    
    if (obj instanceof Group) {
      // getHeight() does not function the same for Group objects for some reason ...
      objDimensions = new float[] { obj.getWidth(), ((Group)obj).getBackgroundHeight() };
    } else if (obj instanceof DropdownList) {
      // Ignore the number of items displayed by the DropdownList, when it is open
      objDimensions = new float[] { obj.getWidth(), ((DropdownList)obj).getBarHeight() };
    } else {
      objDimensions = new float[] { obj.getWidth(), obj.getHeight() };
    }
    
    switch(pos) {
      case TOP_RIGHT:
        relPosition[0] = (int)(objPosition[0] + objDimensions[0] + offsetX);
        relPosition[1] = (int)(objPosition[1] + offsetY);
        break;
        
      case TOP_LEFT:
        relPosition[0] = (int)(objPosition[0] + offsetX);
        relPosition[1] = (int)(objPosition[1] + offsetY);
        break;
        
      case BOTTOM_RIGHT:
       relPosition[0] = (int)(objPosition[0] + objDimensions[0] + offsetX);
       relPosition[1] = (int)(objPosition[1] + objDimensions[1] + offsetY);
       break;
       
      case BOTTOM_LEFT:
        relPosition[0] = (int)(objPosition[0] + offsetX);
        relPosition[1] = (int)(objPosition[1] + objDimensions[1] + offsetY);
        break;
        
      default:
    }
    
    return relPosition;
  }
  
  /**
   * Update the contents of the two dropdown menus that
   * contain world objects.
   */
  private void updateListContents() {
    
    if (activeScenario != null) {
      dropdownLists[4] = (MyDropdownList)dropdownLists[4].clear();
      dropdownLists[5] = (MyDropdownList)dropdownLists[5].clear();
      dropdownLists[5].addItem("None", null);
      
      for (WorldObject wldObj : activeScenario) {
        dropdownLists[4].addItem(wldObj.toString(), wldObj);
        
        if (wldObj instanceof Fixture) {
          // Load all fixtures from the active scenario
          dropdownLists[5].addItem(wldObj.toString(), wldObj);
        }
      }
      // Update each dropdownlist's active label
      dropdownLists[4].updateActiveLabel();
      dropdownLists[5].updateActiveLabel();
    }
    
    dropdownLists[6] = (MyDropdownList)dropdownLists[6].clear();
    for (int idx = 0; idx < SCENARIOS.size(); ++idx) {
      // Load all scenario indices
      Scenario s = SCENARIOS.get(idx);
      dropdownLists[6].addItem(s.getName(), s);
    }
    dropdownLists[6].updateActiveLabel();
  }
  
  /**
   * Update how many of the dimension field and label pairs are displayed in
   * the create world object window based on which shape type is chosen from the shape dropdown list.
   */
  private void updateDimLblsAndFields() {
    String activeButtonLabel = windowTabs.getActiveButtonName();
    String[] lblNames = new String[0];
    
    if (activeButtonLabel != null) {
      if (activeButtonLabel.equals("Create")) {
        ShapeType selectedShape = (ShapeType)dropdownLists[1].getActiveLabelValue();
        
        // Define the label text and the number of dimensionos fields to display
        if (selectedShape == ShapeType.BOX) {
          lblNames = new String[] { "Length:", "Height:", "Width" };
          
        } else if (selectedShape == ShapeType.CYLINDER) {
          lblNames = new String[] { "Radius", "Height" };
          
        } else if (selectedShape == ShapeType.MODEL) {
          lblNames = new String[] { "Source:", "Scale:", };
        }
        
      } else if (activeButtonLabel.equals("Edit")) {
        Object val = dropdownLists[4].getActiveLabelValue();
        
        if (val instanceof WorldObject) {
          Shape s = ((WorldObject)val).getForm();
          
          if (s instanceof Box) {
            lblNames = new String[] { "Length:", "Height:", "Width" };
            
          } else if (s instanceof Cylinder) {
            lblNames = new String[] { "Radius", "Height" };
          
          } else if (s instanceof ModelShape) {
            lblNames = new String[] { "Scale:" };
          }
        }
        
      }
    }
    
    for (int idxDim = 0; idxDim < shapeDefFields.size(); ++idxDim) {
      if (idxDim < lblNames.length) {
        // Show a number of dimension fields and labels equal to the value of dimSize
        shapeDefAreas.set(idxDim, shapeDefAreas.get(idxDim).setText(lblNames[idxDim]).show());
        shapeDefFields.set(idxDim, shapeDefFields.get(idxDim).show());
        
      } else {
        // Hide remaining dimension fields and labels
        shapeDefAreas.set(idxDim, shapeDefAreas.get(idxDim).hide());
        shapeDefFields.set(idxDim, shapeDefFields.get(idxDim).hide());
      }
    }
  }
  
  /**
   * Only update the group visiblility if it does not
   * match the given visiblity flag.
   */
  private void setGroupVisible(Group g, boolean setVisible) {
    if (g.isVisible() != setVisible) {
      g.setVisible(setVisible);
    }
  }
  
  /**
   * Creates a world object form the input fields in the Create window.
   */
  public WorldObject createWorldObject() {
    // Check the object type dropdown list
    Object val = dropdownLists[0].getActiveLabelValue();
    // Determine if the object to be create is a Fixture or a Part
    Float objectType = 0.0;
    
    if (val instanceof Float) {
      objectType = (Float)val;
    }
    
    pushMatrix();
    resetMatrix();
    WorldObject wldObj = null;
    
    try {
      
      if (objectType == 0.0) {
        // Create a Part
        String name = objName.getText();
        
        ShapeType type = (ShapeType)dropdownLists[1].getActiveLabelValue();
          
        color fill = (Integer)dropdownLists[2].getActiveLabelValue();
        
        switch(type) {
          case BOX:
            color strokeVal = (Integer)dropdownLists[3].getActiveLabelValue();
            Float[] shapeDims = getBoxDimensions();
            // Construct a box shape
            if (shapeDims != null && shapeDims[0] != null && shapeDims[1] != null && shapeDims[2] != null) {
              wldObj = new Part(name, fill, strokeVal, shapeDims[0], shapeDims[1], shapeDims[2]);
            }
            break;
            
          case CYLINDER:
            strokeVal = (Integer)dropdownLists[3].getActiveLabelValue();
            shapeDims = getCylinderDimensions();
            // Construct a cylinder
            if (shapeDims != null && shapeDims[0] != null && shapeDims[1] != null) {
              wldObj = new Part(name, fill, strokeVal, shapeDims[0], shapeDims[1]);
            }
            break;
            
          case MODEL:
            String srcFile = shapeDefFields.get(0).getText();
            shapeDims = getModelDimensions(true);
            // Construct a complex model
            if (shapeDims != null) {
              ModelShape model;
              
              if (shapeDims[0] != null) {
                // Define shape scale
                model = new ModelShape(srcFile, fill, shapeDims[0]);
              } else {
                model = new ModelShape(srcFile, fill);
              }
              
              wldObj = new Part(name, model);
            }
            break;
          default:
        }
        
      } else if (objectType == 1.0) {
        // Create a fixture
        String name = objName.getText();
        ShapeType type = (ShapeType)dropdownLists[1].getActiveLabelValue();
          
        color fill = (Integer)dropdownLists[2].getActiveLabelValue();
        
        switch(type) {
          case BOX:
            color strokeVal = (Integer)dropdownLists[3].getActiveLabelValue();
            Float[] shapeDims = getBoxDimensions();
            // Construct a box shape
            if (shapeDims != null && shapeDims[0] != null && shapeDims[1] != null && shapeDims[2] != null) {
              wldObj = new Fixture(name, fill, strokeVal, shapeDims[0], shapeDims[1], shapeDims[2]);
            }
            break;
            
          case CYLINDER:
            strokeVal = (Integer)dropdownLists[3].getActiveLabelValue();
            shapeDims = getCylinderDimensions();
            // Construct a cylinder
            if (shapeDims != null && shapeDims[0] != null && shapeDims[1] != null) {
              wldObj = new Fixture(name, fill, strokeVal, shapeDims[0], shapeDims[1]);
            }
            break;
            
          case MODEL:
            String srcFile = shapeDefFields.get(0).getText();
            shapeDims = getModelDimensions(false);
            // Construct a complex model
            ModelShape model;
            
            if (shapeDims != null && shapeDims[0] != null) {
              // Define model scale value
              model = new ModelShape(srcFile, fill, shapeDims[0]);
            } else {
              model = new ModelShape(srcFile, fill);
            }
            
            wldObj = new Fixture(name, model);
            break;
          default:
        }
      }
      
    } catch (NullPointerException NPEx) {
      println("Missing parameter!");
    } catch (ClassCastException CCEx) {
      println("Invalid field?");
      CCEx.printStackTrace();
    } catch (IndexOutOfBoundsException IOOBEx) {
      println("Missing field?");
      IOOBEx.printStackTrace();
    }
    
    popMatrix();
      
    return wldObj;
  }
  
  /**
   * Eit the position and orientation (as well as the fixture reference for Parts)
   * of the currently selected World Object in the Object dropdown list.
   */
  public void editWorldObject() {
    WorldObject toEdit = getActiveWorldObject();
    
    if (toEdit != null) {
      
      if (armModel != null && toEdit == armModel.held) {
        // Cannot edit an object being held by the Robot
        println("Cannot edit an object currently being held by the Robot!");
        return;
      }
      
      try {
        Shape s = toEdit.getForm();
        
        if (s instanceof Box) {
          Float[] newDims = getBoxDimensions();
          
          if (newDims[0] != null) {
            // Update the box's length
            s.setDim(newDims[0], DimType.LENGTH);
            
            if (toEdit instanceof Part) {
              // Update the bounding-box's length
              ((Part)toEdit).setOBBDim(newDims[0] + 10f, DimType.LENGTH);
            }
          }
          
          if (newDims[1] != null) {
            // Update the box's height
            s.setDim(newDims[1], DimType.HEIGHT);
            
            if (toEdit instanceof Part) {
              // Update the bounding-box's height
              ((Part)toEdit).setOBBDim(newDims[1] + 10f, DimType.HEIGHT);
            }
          }
          
          if (newDims[2] != null) {
            // Update the box's width
            s.setDim(newDims[2], DimType.WIDTH);
            
            if (toEdit instanceof Part) {
              // Update the bounding-box's width
              ((Part)toEdit).setOBBDim(newDims[2] + 10f, DimType.WIDTH);
            }
          }
          
        } else if (s instanceof Cylinder) {
          Float[] newDims = getCylinderDimensions();
          
          if (newDims[0] != null) {
            // Update the cylinder's radius
            s.setDim(newDims[0], DimType.RADIUS);
            
            if (toEdit instanceof Part) {
              // Update the bounding-box's length and height
              ((Part)toEdit).setOBBDim(2f * newDims[0] + 5f, DimType.LENGTH);
              ((Part)toEdit).setOBBDim(2f * newDims[0] + 5f, DimType.HEIGHT);
            }
          }
          
          if (newDims[1] != null) {
            // Update the cylinder's height
            s.setDim(newDims[1], DimType.HEIGHT);
            
            if (toEdit instanceof Part) {
              // Update the bounding-box's width
              ((Part)toEdit).setOBBDim(newDims[1] + 10f, DimType.WIDTH);
            }
          }
         
        } else if (s instanceof ModelShape) {
          Float[] newDims = getModelDimensions( (toEdit instanceof Part) );
          
          if (newDims[0] != null) {
            // Update the model's scale value
            s.setDim(newDims[0], DimType.SCALE);
            
            if (toEdit instanceof Part) {
              ((Part)toEdit).updateOBBDims();
            }
          }
        }
        
        // Convert origin position into the World Frame
        PVector oPosition = convertNativeToWorld( toEdit.getLocalCenter() ),
                oWPR = matrixToEuler(toEdit.getLocalOrientationAxes()).mult(RAD_TO_DEG);
        Float[] inputValues = getOrientationValues();
        // Update position and orientation
        if (inputValues[0] != null) { oPosition.x = inputValues[0]; }
        if (inputValues[1] != null) { oPosition.y = inputValues[1]; }
        if (inputValues[2] != null) { oPosition.z = inputValues[2]; }
        if (inputValues[3] != null) { oWPR.x = -inputValues[3]; }
        if (inputValues[5] != null) { oWPR.y = -inputValues[5]; }
        if (inputValues[4] != null) { oWPR.z = inputValues[4]; }
        
        // Convert values from the World to the Native coordinate system
        PVector position = convertWorldToNative( oPosition );
        PVector wpr = oWPR.mult(DEG_TO_RAD);
        float[][] orientation = eulerToMatrix(wpr);
        // Update the Objects position and orientaion
        toEdit.setLocalCenter(position);
        toEdit.setLocalOrientationAxes(orientation);
        
        if (toEdit instanceof Part) {
          // Set the reference of the Part to the currently active fixture
          Fixture refFixture = (Fixture)dropdownLists[5].getActiveLabelValue();
          ((Part)toEdit).setFixtureRef(refFixture);
        }
      } catch (NullPointerException NPEx) {
        println("Missing parameter!");
        NPEx.printStackTrace();
      }
    } else {
      println("No object selected!");
    }
    
    /* If the edited object is a fixture, then update the orientation
     * of all parts, which reference this fixture, in this scenario. */
    if (toEdit instanceof Fixture) {
      if (activeScenario != null) {
        
        for (WorldObject wldObj : activeScenario) {
          if (wldObj instanceof Part) {
            Part p = (Part)wldObj;
            
            if (p.getFixtureRef() == toEdit) {
              p.updateAbsoluteOrientation();
            }
          }
        }
      }
    }
    
  }
  
  /**
   * TODO
   */
  private Float[] getBoxDimensions() {
    try {
      // null values represent an uninitialized field
      final Float[] dimensions = new Float[] { null, null, null };
      
      // Pull from the dim fields
      String lenField = shapeDefFields.get(0).getText(),
             hgtField = shapeDefFields.get(1).getText(),
             wdhField = shapeDefFields.get(2).getText();
      
      if (lenField != null && !lenField.equals("")) {
        // Read length input
        float val = Float.parseFloat(lenField);
        
        if (val <= 0) {
          throw new NumberFormatException("Invalid length value!");
        }
        // Length cap of 9999
        dimensions[0] = min(val, 9999f);
      }
      
      if (hgtField != null && !hgtField.equals("")) {
        // Read height input
        float val = Float.parseFloat(hgtField);
        
        if (val <= 0) {
          throw new NumberFormatException("Invalid height value!");
        }
        // Height cap of 9999
        dimensions[1] = min(val, 9999f);
      }
      
      if (wdhField != null && !wdhField.equals("")) {
        // Read Width input
        float val = Float.parseFloat(wdhField);
        
        if (val <= 0) {
          throw new NumberFormatException("Invalid width value!");
        }
        // Width cap of 9999
        dimensions[2] = min(val, 9999f);
      }
      
      return dimensions;
      
    } catch (NumberFormatException NFEx) {
      println("Invalid number input!");
      return null;
      
    } catch (NullPointerException NPEx) {
      println("Missing parameter!");
      return null;
    }
  }
  
  /**
   *TODO
   */
  private Float[] getCylinderDimensions() {
    try {
      // null values represent an uninitialized field
      final Float[] dimensions = new Float[] { null, null };
      
      // Pull from the dim fields
      String radField = shapeDefFields.get(0).getText(),
             hgtField = shapeDefFields.get(1).getText();
      
      if (radField != null && !radField.equals("")) {
        // Read radius input
        float val = Float.parseFloat(radField);
        
        if (val <= 0) {
          throw new NumberFormatException("Invalid length value!");
        }
        // Radius cap of 9999
        dimensions[0] = min(val, 9999f);
      }
      
      if (hgtField != null && !hgtField.equals("")) {
        // Read height input
        float val = Float.parseFloat(hgtField);
        
        if (val <= 0) {
          throw new NumberFormatException("Invalid height value!");
        }
        // Height cap of 9999
        dimensions[1] = min(val, 9999f);
      }
      
      return dimensions;
      
    } catch (NumberFormatException NFEx) {
      println("Invalid number input!");
      return null;
      
    } catch (NullPointerException NPEx) {
      println("Missing parameter!");
      return null;
    }
  }
  
  /**
   * TODO
   */
  private Float[] getModelDimensions(boolean forAPart) {
    try {
      // null values represent an uninitialized field
      final Float[] dimensions = new Float[] { null };
      
      String activeWindow = windowTabs.getActiveButtonName(), sclField;
      // Pull from the Dim fields
      if (activeWindow != null && activeWindow.equals("Create")) {
        sclField = shapeDefFields.get(1).getText();
        
      } else {
        sclField = shapeDefFields.get(0).getText();
      }
      
      if (sclField != null && !sclField.equals("")) {
        // Read scale input
        float val = Float.parseFloat(sclField);
        
        if (val <= 0) {
          throw new NumberFormatException("Invalid scale value");
        }
        // Scale cap of 50
        dimensions[0] = min(val, 50f);
      }
      
      return dimensions;
      
    } catch (NumberFormatException NFEx) {
      println(NFEx.getMessage());
      return null;
      
    } catch (NullPointerException NPEx) {
      println("Missing parameter!");
      return null;
    }
  }
  
  /**
   * TODO
   */
  private Float[] getOrientationValues() {
    try {
        // Pull from x, y, z, w, p, r, fields input fields
        String xFieldVal = objOrientationFields[0].getText(), yFieldVal = objOrientationFields[1].getText(),
               zFieldVal = objOrientationFields[2].getText(), wFieldVal = objOrientationFields[3].getText(),
               pFieldVal = objOrientationFields[4].getText(), rFieldVal = objOrientationFields[5].getText();
        // NaN indicates an uninitialized field
        Float[] values = new Float[] { null, null, null, null, null, null };
        
        // Update x value
        if (xFieldVal != null && !xFieldVal.equals("")) {
          float val = Float.parseFloat(xFieldVal);
          // Bring value within the range [-9999, 9999]
          val = max(-9999f, min(val, 9999f));
          values[0] = val;
        }
        // Update y value
        if (yFieldVal != null && !yFieldVal.equals("")) {
          float val = Float.parseFloat(yFieldVal);
          // Bring value within the range [-9999, 9999]
          val = max(-9999f, min(val, 9999f));
          values[1] = val;
        }
        // Update z value
        if (zFieldVal != null && !zFieldVal.equals("")) {
          float val = Float.parseFloat(zFieldVal);
          // Bring value within the range [-9999, 9999]
          val = max(-9999f, min(val, 9999f));
          values[2] = val;
        }
        // Update w angle
        if (wFieldVal != null && !wFieldVal.equals("")) {
          float val = Float.parseFloat(wFieldVal);
          // Bring value within the range [-9999, 9999]
          val = max(-9999f, min(val, 9999f));
          values[3] = val;
        }
        // Update p angle
        if (pFieldVal != null && !pFieldVal.equals("")) {
          float val = Float.parseFloat(pFieldVal);
          // Bring value within the range [-9999, 9999]
          val = max(-9999f, min(val, 9999f));
          values[4] = val;
        }
        // Update r angle
        if (rFieldVal != null && !rFieldVal.equals("")) {
          float val = Float.parseFloat(rFieldVal);
          // Bring value within the range [-9999, 9999]
          val = max(-9999f, min(val, 9999f));
          values[5] = val;
        }
        
        return values;
        
    } catch (NumberFormatException NFEx) {
      println("Invalid number input!");
      return null;
      
    } catch (NullPointerException NPEx) {
      println("Missing parameter!");
      return null;
    }
  }
  
  /**
   * Reset the base label of every dropdown list.
   */
  private void resetListLabels() {
    for (MyDropdownList list : dropdownLists) {
      list.resetLabel();
    }
    
    for (int idxDim = 0; idxDim < shapeDefFields.size(); ++idxDim) {
      // Hide remaining dimension fields and labels
      shapeDefAreas.set(idxDim, shapeDefAreas.get(idxDim).hide());
      shapeDefFields.set(idxDim, shapeDefFields.get(idxDim).hide());
      ++idxDim;
    }
    
    dropdownLbls[5].hide();
    dropdownLists[5] = (MyDropdownList)dropdownLists[5].hide();
    updateDimLblsAndFields();
  }
  
  /**
   * Delete the world object that is selected in
   * the Object dropdown list, if any.
   * 
   * @returning  -1  if the active Scenario is null
   *              0  if the object was removed succesfully,
   *              1  if the object did not exist in the scenario,
   *              2  if the object was a Fixture that was removed
   *                 from the scenario and was referenced by at
   *                 least one Part in the scenario
   */
  public int deleteActiveWorldObject() {
    int ret = -1;
    
    if (activeScenario != null) {
      ret = activeScenario.removeWorldObject( getActiveWorldObject() );
    }
    
    return ret;
  }
  
  /**
   * Reinitialize any and all input fields
   */
  private void clearAllInputFields() {
    clearGroupInputFields(null);
    updateDimLblsAndFields();
  }
  
  /**
   * Reinitialize the input fields for any contents in the Create Object window
   */
  private void clearCreateInputFields() {
    clearGroupInputFields(createObjWindow);
    clearSharedInputFields();
    updateDimLblsAndFields();
  }
  
  /**
   * Reinitialize the input fields for any contents in the Edit Object window
   */
  private void clearEditInputFields() {
    clearGroupInputFields(editObjWindow);
    clearSharedInputFields();
    updateDimLblsAndFields();
  }
  
  /**
   * Reinitialize the input fields for any shared contents
   */
  private void clearSharedInputFields() {
    clearGroupInputFields(sharedElements);
    updateDimLblsAndFields();
  }
  
  /**
   * Reinitialize the input fields for any contents in the Scenario window
   */
  private void clearScenarioInputFields() {
    clearGroupInputFields(scenarioWindow);
    updateDimLblsAndFields();
  }
  
  /**
   * Reinitializes any controller interface in the given group that accepts user
   * input; currently only text fields and dropdown lists are updated.
   */
  private void clearGroupInputFields(Group g) {
    List<ControllerInterface<?>> contents = UIManager.getAll();
    
    for (ControllerInterface<?> controller : contents) {
      
      if (g == null || controller.getParent().equals(g)) {
        
        if (controller instanceof Textfield) {
          // Clear anything inputted into the text field
          controller = ((Textfield)controller).setValue("");
        } else if (controller instanceof MyDropdownList) {
          // Reset the caption label of the dropdown list and close the list
          ((MyDropdownList)controller).resetLabel();
          controller = ((DropdownList)controller).close();
        }
      }
    }
  }
  
  /**
   * Creates a new scenario with the name pulled from the scenario name text field.
   * If the name given is already given to another existing scenario, then no new
   * Scenario is created. Also, names can only consist of 16 letters or numbers.
   * 
   * @returning  A new Scenario object or null if the scenario name text field's
   *             value is invalid
   */
  public Scenario initializeScenario() {
    String activeButtonLabel = windowTabs.getActiveButtonName();
    
    if (activeButtonLabel != null && activeButtonLabel.equals("Scenario")) {
      String name = scenarioName.getText();
      
      if (name != null) {
        // Names only consist of letters and numbers
        if (Pattern.matches("[a-zA-Z0-9]+", name)) {
          
          for (Scenario s : SCENARIOS) {
            if (s.getName().equals(name)) {
              // Duplicate name
              println("Names must be unique!");
              return null;
            }
          }
          
          if (name.length() > 16) {
            // Names have a max length of 16 characters
            name = name.substring(0, 16);
          }
          
          return new Scenario(name);
        }
      }
    }
    
    // Invalid input or wrong window open 
    return null;
  }
  
  /**
   * Returns the scenario associated with the label that is active
   * for the scenario dropdown list.
   * 
   * @returning  The index value or null if no such index exists
   */
  public Scenario getActiveScenario() {
    String activeButtonLabel = windowTabs.getActiveButtonName();
    
    if (activeButtonLabel != null && activeButtonLabel.equals("Scenario")) {
      Object val = dropdownLists[6].getActiveLabelValue();
      
      if (val instanceof Scenario) {
        // Set the active scenario index
        return (Scenario)val;
      } else if (val != null) {
        // Invalid entry in the dropdown list
        System.out.printf("Invalid class type: %d!\n", val.getClass());
      }
    }
    
    return null;
  }
  
    /**
   * Returns the object that is currently being edited
   * in the world object editing menu.
   */
  protected WorldObject getActiveWorldObject() {
    Object wldObj = dropdownLists[4].getActiveLabelValue();
    
    if (editObjWindow.isVisible() && wldObj instanceof WorldObject) {
      return (WorldObject)wldObj;
    } else {
      return null;
    }
  }
  
  /**
   * Deterimes whether a single text field is active.
   */
  public boolean isATextFieldActive() {
    // Check EVERY text field
    if (objName.isFocus() || scenarioName.isFocus()) {
      return true;
    }
    
    for (Textfield tField : objOrientationFields) {
      if (tField.isFocus()) {
        return true;
      }
    }
    
    for (Textfield tField : shapeDefFields) {
      if (tField.isFocus()) {
        return true;
      }
    }
    
    return false;
  }
  
  /**
   * Determines whether the mouse is over a dropdown list.
   */
  public boolean isMouseOverADropdownList() {
    for (MyDropdownList dropdown : dropdownLists) {
      if (dropdown.isMouseOver()) {
        return true;
      }
    }
    
    return false;
  }
}
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
  
  private Group createObjWindow, editObjWindow, sharedElements;

  private ButtonTabs windowTabs;
  private Textarea[] labels;
  private Textfield objName;
  private ArrayList<Textfield> shapeDefFields;
  private Textfield[] objOrientation;
  
  private Button[] singleButtons;
  private MyDropdownList[] dropDownLists;
  
  public static final int offsetX = 10,
                          distBtwFieldsY = 15,
                          distLblToFieldX = 5,
                          fieldHeight = 20,
                          dropItemHeight = 21,
                          lLblWidth = 120,
                          mLblWidth = 86,
                          sLblWidth = 60,
                          fieldWidth = 95,
                          lButtonWidth = 80,
                          sButtonWidth = 56,
                          mButtonHeight = 26,
                          sButtonHeight = 20;
  
  /**
   * Creates a new window with the given ControlP5 object as the parent
   * and the given fonts which will be applied to the text in the window.
   */
  public WindowManager(ControlP5 manager, PFont small, PFont medium) {
    // Initialize content fields
    UIManager = manager;
    
    labels = new Textarea[15];
    objOrientation = new Textfield[6];
    shapeDefFields = new ArrayList<Textfield>();
    singleButtons = new Button[3];
    dropDownLists = new MyDropdownList[6];
    
    // Create some temporary color and dimension variables
    color bkgrdColor = color(210),
          fieldTxtColor = color(0),
          fieldCurColor = color(0),
          fieldActColor = color(255, 0, 0),
          fieldBkgrdColor = color(255),
          fieldFrgrdColor = color(0),
          buttonTxtColor = color(255),
          buttonDefColor = color(70),
          buttonActColor = color(220, 40, 40);
    
    int[] relPos = new int[] { offsetX, 0 };
    String[] windowList = new String[] { "Hide", "Pendant", "Create", "Edit" };
    // Create window tab bar
    windowTabs = (ButtonTabs)(new ButtonTabs(UIManager, "List:")
                  // Sets button text color
                  .setColorValue(buttonTxtColor)
                  .setColorBackground(buttonDefColor)
                  .setColorActive(buttonActColor)
                  .setPosition(relPos[0], relPos[1])
                  .setSize(windowList.length * lButtonWidth, sButtonHeight));
    
    windowTabs.getCaptionLabel().setFont(medium);
    
    windowTabs.addItems(windowList);
    // Initialize the groups
    relPos = relativePosition(windowTabs, RelativePoint.BOTTOM_LEFT, 0, 0);
    createObjWindow = UIManager.addGroup("CREATEOBJ").setPosition(relPos[0], relPos[1])
                  .setBackgroundColor(bkgrdColor)
                  .setSize(windowTabs.getWidth(), 420)
                  .hideBar();
    
    editObjWindow = UIManager.addGroup("EDITOBJ").setPosition(relPos[0], relPos[1])
                .setBackgroundColor(bkgrdColor)
                .setSize(windowTabs.getWidth(), 350)
                .hideBar();
    
    sharedElements = UIManager.addGroup("SHARED").setPosition(relPos[0], relPos[1])
                .setBackgroundColor(bkgrdColor)
                .setSize(lButtonWidth + offsetX + 10, sButtonHeight + offsetX)
                .hideBar();
    // Initialize window contents
    labels[0] = UIManager.addTextarea("Name", "Name:", 0, 0, sLblWidth, fieldHeight)
                          .setFont(medium)
                          .setColor(fieldTxtColor)
                          .setColorActive(fieldActColor)
                          .setColorBackground(bkgrdColor)
                          .setColorForeground(bkgrdColor)
                          .moveTo(createObjWindow);
    
    objName = UIManager.addTextfield("NField", 0, 0, fieldWidth, fieldHeight)
                          .setColor(fieldTxtColor)
                          .setColorCursor(fieldCurColor)
                          .setColorActive(fieldActColor)
                          .setColorLabel(bkgrdColor)
                          .setColorBackground(fieldBkgrdColor)
                          .setColorForeground(fieldFrgrdColor)
                          .moveTo(createObjWindow);
    
    labels[1] = UIManager.addTextarea("ShapeType", "Shape:", 0, 0, mLblWidth, mButtonHeight)
                          .setFont(medium)
                          .setColor(fieldTxtColor)
                          .setColorActive(fieldActColor)
                          .setColorBackground(bkgrdColor)
                          .setColorForeground(bkgrdColor)
                          .moveTo(createObjWindow);
    
    labels[10] = UIManager.addTextarea("Dim0", "Dim(0):", 0, 0, mLblWidth, mButtonHeight)
                          .setFont(medium)
                          .setColor(fieldTxtColor)
                          .setColorActive(fieldActColor)
                          .setColorBackground(bkgrdColor)
                          .setColorForeground(bkgrdColor)
                          .moveTo(createObjWindow);
    
    shapeDefFields.add( UIManager.addTextfield("D0Field", 0, 0, fieldWidth, fieldHeight)
                          .setColor(fieldTxtColor)
                          .setColorCursor(fieldCurColor)
                          .setColorActive(fieldActColor)
                          .setColorLabel(bkgrdColor)
                          .setColorBackground(fieldBkgrdColor)
                          .setColorForeground(fieldFrgrdColor)
                          .moveTo(createObjWindow) );
    
    labels[11] = UIManager.addTextarea("Dim1", "Dim(1):", 0, 0, mLblWidth, mButtonHeight)
                          .setFont(medium)
                          .setColor(fieldTxtColor)
                          .setColorActive(fieldActColor)
                          .setColorBackground(bkgrdColor)
                          .setColorForeground(bkgrdColor)
                          .moveTo(createObjWindow);
    
    shapeDefFields.add( UIManager.addTextfield("D1Field", 0, 0, fieldWidth, fieldHeight)
                          .setColor(fieldTxtColor)
                          .setColorCursor(fieldCurColor)
                          .setColorActive(fieldActColor)
                          .setColorLabel(bkgrdColor)
                          .setColorBackground(fieldBkgrdColor)
                          .setColorForeground(fieldFrgrdColor)
                          .moveTo(createObjWindow) );
    
    labels[12] = UIManager.addTextarea("Dim2", "Dim(2):", 0, 0, mLblWidth, mButtonHeight)
                          .setFont(medium)
                          .setColor(fieldTxtColor)
                          .setColorActive(fieldActColor)
                          .setColorBackground(bkgrdColor)
                          .setColorForeground(bkgrdColor)
                          .moveTo(createObjWindow);
    
    shapeDefFields.add( UIManager.addTextfield("D2Field", 0, 0, fieldWidth, fieldHeight)
                          .setColor(fieldTxtColor)
                          .setColorCursor(fieldCurColor)
                          .setColorActive(fieldActColor)
                          .setColorLabel(bkgrdColor)
                          .setColorBackground(fieldBkgrdColor)
                          .setColorForeground(fieldFrgrdColor)
                          .moveTo(createObjWindow) );
    
    labels[13] = UIManager.addTextarea("Dim3", "Dim(3):", 0, 0, mLblWidth, mButtonHeight)
                          .setFont(medium)
                          .setColor(fieldTxtColor)
                          .setColorActive(fieldActColor)
                          .setColorBackground(bkgrdColor)
                          .setColorForeground(bkgrdColor)
                          .moveTo(createObjWindow);
    
    shapeDefFields.add( UIManager.addTextfield("D3Field", 0, 0, fieldWidth, fieldHeight)
                          .setColor(fieldTxtColor)
                          .setColorCursor(fieldCurColor)
                          .setColorActive(fieldActColor)
                          .setColorLabel(bkgrdColor)
                          .setColorBackground(fieldBkgrdColor)
                          .setColorForeground(fieldFrgrdColor)
                          .moveTo(createObjWindow) );
    
    labels[2] = UIManager.addTextarea("FillColor", "Fill:", 0, 0, mLblWidth, mButtonHeight)
                          .setFont(medium)
                          .setColor(fieldTxtColor)
                          .setColorActive(fieldActColor)
                          .setColorBackground(bkgrdColor)
                          .setColorForeground(bkgrdColor)
                          .moveTo(createObjWindow);
    
    labels[3] = UIManager.addTextarea("OutlineColor", "Outline:", 0, 0, mLblWidth, mButtonHeight)
                          .setFont(medium)
                          .setColor(fieldTxtColor)
                          .setColorActive(fieldActColor)
                          .setColorBackground(bkgrdColor)
                          .setColorForeground(bkgrdColor)
                          .moveTo(createObjWindow);
    
    singleButtons[0] = UIManager.addButton("Create")
                        .setColorValue(buttonTxtColor)
                        .setColorBackground(buttonDefColor)
                        .setColorActive(buttonActColor)
                        .moveTo(createObjWindow)
                        .setPosition(0, 0)
                        .setSize(sButtonWidth, mButtonHeight);
    
    singleButtons[2] = UIManager.addButton("Clear")
                        .setColorValue(buttonTxtColor)
                        .setColorBackground(buttonDefColor)
                        .setColorActive(buttonActColor)
                        .moveTo(sharedElements)
                        .setPosition(0, 0)
                        .setSize(sButtonWidth, mButtonHeight);
    
    // Place below the objects dropdown list
    labels[4] = UIManager.addTextarea("XArea", "X:", 0, 0, sLblWidth, fieldHeight)
                          .setFont(medium)
                          .setColor(fieldTxtColor)
                          .setColorActive(fieldActColor)
                          .setColorBackground(bkgrdColor)
                          .setColorForeground(bkgrdColor)
                          .moveTo(editObjWindow);
    
    objOrientation[0] = UIManager.addTextfield("XField", 0, 0, fieldWidth, fieldHeight)
                          .setColor(fieldTxtColor)
                          .setColorCursor(fieldCurColor)
                          .setColorActive(fieldActColor)
                          .setColorLabel(bkgrdColor)
                          .setColorBackground(fieldBkgrdColor)
                          .setColorForeground(fieldFrgrdColor)
                          .moveTo(editObjWindow);
    
    labels[5] = UIManager.addTextarea("YArea", "Y:", 0, 0, sLblWidth, fieldHeight)
                          .setFont(medium)
                          .setColor(fieldTxtColor)
                          .setColorActive(fieldActColor)
                          .setColorBackground(bkgrdColor)
                          .setColorForeground(bkgrdColor)
                          .moveTo(editObjWindow);
    
    objOrientation[1] = UIManager.addTextfield("YField", 0, 0, fieldWidth, fieldHeight)
                          .setColor(fieldTxtColor)
                          .setColorCursor(fieldCurColor)
                          .setColorActive(fieldActColor)
                          .setColorLabel(bkgrdColor)
                          .setColorBackground(fieldBkgrdColor)
                          .setColorForeground(fieldFrgrdColor)
                          .moveTo(editObjWindow);
    
    labels[6] = UIManager.addTextarea("ZArea", "Z:", 0, 0, sLblWidth, fieldHeight)
                          .setFont(medium)
                          .setColor(fieldTxtColor)
                          .setColorActive(fieldActColor)
                          .setColorBackground(bkgrdColor)
                          .setColorForeground(bkgrdColor)
                          .moveTo(editObjWindow);
    
    objOrientation[2] = UIManager.addTextfield("ZField", 0, 0, fieldWidth, fieldHeight)
                          .setColor(fieldTxtColor)
                          .setColorCursor(fieldCurColor)
                          .setColorActive(fieldActColor)
                          .setColorLabel(bkgrdColor)
                          .setColorBackground(fieldBkgrdColor)
                          .setColorForeground(fieldFrgrdColor)
                          .moveTo(editObjWindow);
    
    labels[7] = UIManager.addTextarea("WArea", "W:", 0, 0, sLblWidth, fieldHeight)
                          .setFont(medium)
                          .setColor(fieldTxtColor)
                          .setColorActive(fieldActColor)
                          .setColorBackground(bkgrdColor)
                          .setColorForeground(bkgrdColor)
                          .moveTo(editObjWindow);
    
    objOrientation[3] = UIManager.addTextfield("WField", 0, 0, fieldWidth, fieldHeight)
                          .setColor(fieldTxtColor)
                          .setColorCursor(fieldCurColor)
                          .setColorActive(fieldActColor)
                          .setColorLabel(bkgrdColor)
                          .setColorBackground(fieldBkgrdColor)
                          .setColorForeground(fieldFrgrdColor)
                          .moveTo(editObjWindow);
    
    labels[8] = UIManager.addTextarea("PArea", "P:", 0, 0, sLblWidth, fieldHeight)
                          .setFont(medium)
                          .setColor(fieldTxtColor)
                          .setColorActive(fieldActColor)
                          .setColorBackground(bkgrdColor)
                          .setColorForeground(bkgrdColor)
                          .moveTo(editObjWindow);
    
    objOrientation[4] = UIManager.addTextfield("PField", 0, 0, fieldWidth, fieldHeight)
                          .setColor(fieldTxtColor)
                          .setColorCursor(fieldCurColor)
                          .setColorActive(fieldActColor)
                          .setColorLabel(bkgrdColor)
                          .setColorBackground(fieldBkgrdColor)
                          .setColorForeground(fieldFrgrdColor)
                          .moveTo(editObjWindow);
    
    labels[9] = UIManager.addTextarea("RArea", "R:", 0, 0, sLblWidth, fieldHeight)
                          .setFont(medium)
                          .setColor(fieldTxtColor)
                          .setColorActive(fieldActColor)
                          .setColorBackground(bkgrdColor)
                          .setColorForeground(bkgrdColor)
                          .moveTo(editObjWindow);
    
    objOrientation[5] = UIManager.addTextfield("RField", 0, 0, fieldWidth, fieldHeight)
                          .setColor(fieldTxtColor)
                          .setColorCursor(fieldCurColor)
                          .setColorActive(fieldActColor)
                          .setColorLabel(bkgrdColor)
                          .setColorBackground(fieldBkgrdColor)
                          .setColorForeground(fieldFrgrdColor)
                          .moveTo(editObjWindow);
    
    labels[14] = UIManager.addTextarea("FixRef", "Reference:", 0, 0, lLblWidth, mButtonHeight)
                          .setFont(medium)
                          .setColor(fieldTxtColor)
                          .setColorActive(fieldActColor)
                          .setColorBackground(bkgrdColor)
                          .setColorForeground(bkgrdColor)
                          .moveTo(editObjWindow);
    
    singleButtons[1] = UIManager.addButton("Confirm")
                        .setColorValue(buttonTxtColor)
                        .setColorBackground(buttonDefColor)
                        .setColorActive(buttonActColor)
                        .moveTo(editObjWindow)
                        .setSize(sButtonWidth, mButtonHeight);
    // Initialize dropdown lists
    dropDownLists[2] = (MyDropdownList)((new MyDropdownList( UIManager, "Shape"))
                          .setSize(lButtonWidth, 4 * mButtonHeight)
                          .setBarHeight(dropItemHeight)
                          .setItemHeight(dropItemHeight)
                          .setColorValue(buttonTxtColor)
                          .setColorBackground(buttonDefColor)
                          .setColorActive(buttonActColor)
                          .moveTo(createObjWindow)
                          .close());
                          
    dropDownLists[2].addItem("Box", ShapeType.BOX);
    dropDownLists[2].addItem("Cylinder", ShapeType.CYLINDER);
    dropDownLists[2].addItem("Import", ShapeType.MODEL);
    
    dropDownLists[3] = (MyDropdownList)((new MyDropdownList( UIManager, "ObjType"))
                          .setSize(lButtonWidth, 3 * mButtonHeight)
                          .setBarHeight(dropItemHeight)
                          .setItemHeight(dropItemHeight)
                          .setColorValue(buttonTxtColor)
                          .setColorBackground(buttonDefColor)
                          .setColorActive(buttonActColor)
                          .moveTo(sharedElements)
                          .close());
      
    dropDownLists[3].addItem("Parts", 0.0);
    dropDownLists[3].addItem("Fixtures", 1.0);
    
    dropDownLists[1] = (MyDropdownList)((new MyDropdownList( UIManager, "Outline"))
                          .setSize(lButtonWidth, 4 * mButtonHeight)
                          .setBarHeight(dropItemHeight)
                          .setItemHeight(dropItemHeight)
                          .setColorValue(buttonTxtColor)
                          .setColorBackground(buttonDefColor)
                          .setColorActive(buttonActColor)
                          .moveTo(createObjWindow)
                          .close());
    
    dropDownLists[1].addItem("red", color(255, 0, 0));
    dropDownLists[1].addItem("green", color(0, 255, 0));
    dropDownLists[1].addItem("blue", color(0, 0, 255));
    dropDownLists[1].addItem("black", color(0));
    
    dropDownLists[0] = (MyDropdownList)((new MyDropdownList( UIManager, "Fill"))
                          .setSize(lButtonWidth, 5 * mButtonHeight)
                          .setBarHeight(dropItemHeight)
                          .setItemHeight(dropItemHeight)
                          .setColorValue(buttonTxtColor)
                          .setColorBackground(buttonDefColor)
                          .setColorActive(buttonActColor)
                          .moveTo(createObjWindow)
                          .close());
    
    dropDownLists[0].addItem("white", color(255));
    dropDownLists[0].addItem("red", color(255, 0, 0));
    dropDownLists[0].addItem("green", color(0, 255, 0));
    dropDownLists[0].addItem("blue", color(0, 0, 255));
    dropDownLists[0].addItem("black", color(0));
    
    dropDownLists[4] = (MyDropdownList)((new MyDropdownList( UIManager, "Object"))
                          .setSize(lLblWidth, 4 * mButtonHeight)
                          .setBarHeight(dropItemHeight)
                          .setItemHeight(dropItemHeight)
                          .setColorValue(buttonTxtColor)
                          .setColorBackground(buttonDefColor)
                          .setColorActive(buttonActColor)
                          .moveTo(editObjWindow)
                          .close());
    
    dropDownLists[5] = (MyDropdownList)((new MyDropdownList( UIManager, "Fixture"))
                          .setSize(lLblWidth, 4 * mButtonHeight)
                          .setBarHeight(dropItemHeight)
                          .setItemHeight(dropItemHeight)
                          .setColorValue(buttonTxtColor)
                          .setColorBackground(buttonDefColor)
                          .setColorActive(buttonActColor)
                          .moveTo(editObjWindow)
                          .close());
                          
    for (Button button : singleButtons) {
      button.getCaptionLabel().setFont(small);
    }
    
    for (DropdownList list : dropDownLists) {
      list.getCaptionLabel().setFont(small);
    }
    
    // Set the positions of all the contents
    
  }
  
  /**
   * Updates the current active window display based on the selected button on
   * windowTabs. Due to some problems with hiding groups with the ControlP5
   * object, when a new window is brought up a large white sphere is drawn oer
   * the screen to clear the image of the previous window.
   */
  public void updateWindowDisplay() {
    String windowState = windowTabs.getActiveButtonName();
    boolean clearScreen = false;
    
    if (windowState == null || windowState.equals("Hide")) {
      // Hide any window
      clearScreen = (g1.isVisible() || createObjWindow.isVisible() || editObjWindow.isVisible() || sharedElements.isVisible());
      g1.hide();
      setGroupVisible(createObjWindow, false);
      setGroupVisible(editObjWindow, false);
      setGroupVisible(sharedElements, false);
      
    } else if (windowState.equals("Pendant")) {
      // Show pendant
      clearScreen = (!g1.isVisible() || createObjWindow.isVisible() || editObjWindow.isVisible() || sharedElements.isVisible());
      g1.show();
      setGroupVisible(createObjWindow, false);
      setGroupVisible(editObjWindow, false);
      setGroupVisible(sharedElements, false);
      
    } else if (windowState.equals("Create")) {
      // Show world object creation window
      clearScreen = (g1.isVisible() || !createObjWindow.isVisible() || editObjWindow.isVisible() || !sharedElements.isVisible());
      
      g1.hide();
      setGroupVisible(editObjWindow, false);
      
      if (!createObjWindow.isVisible()) {
        setGroupVisible(createObjWindow, true);
        setGroupVisible(sharedElements, true);
        updateCreateWindowContentPositions();
        updateListContents();
        resetDropdownLabels();
      }
      
    } else if (windowState.equals("Edit")) {
      // Show world object edit window
      clearScreen = (g1.isVisible() || createObjWindow.isVisible() || !editObjWindow.isVisible() || !sharedElements.isVisible());
      
      g1.hide();
      setGroupVisible(createObjWindow, false);
      
      if (!editObjWindow.isVisible()) {
        setGroupVisible(editObjWindow, true);
        setGroupVisible(sharedElements, true);
        updateEditWindowContentPositions();
        updateListContents();
        resetDropdownLabels();
      }
    }
    
    if (clearScreen) {
      // Draw over previous screen
      pushMatrix();
      resetMatrix();
      stroke(255);
      fill(255);
      sphere(250);
      popMatrix();
    }
  }
  
  /**
   * Updates the positions of all the elements in the active window
   * based on the current button tab that is active.
   */
  public void updateWindowContentsPositions() {
    String windowState = windowTabs.getActiveButtonName();
    
    if (windowState != null && windowState.equals("Create")) {
      // Create window
      updateCreateWindowContentPositions();
    } else if (windowState != null && windowState.equals("Edit")) {
      // Edit window
      updateEditWindowContentPositions();
    }
    
    updateListContents();
  }
  
  /**
   * Updates the positions of all the contents of the world object creation window.
   */
  private void updateCreateWindowContentPositions() {
    updateDimFieldsAndLabels();
      
    int[] relPos = new int[] { offsetX, offsetX };
    // Object Type dropdown
    dropDownLists[3] = (MyDropdownList)dropDownLists[3].setPosition(relPos[0], relPos[1]);
    // Name label and field
    relPos = relativePosition(dropDownLists[3], RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
    labels[0] = labels[0].setPosition(relPos[0], relPos[1]);
    
    relPos = relativePosition(labels[0], RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
    objName = objName.setPosition(relPos[0], relPos[1]);
    // Shape type label and dropdown
    relPos = relativePosition(labels[0], RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
    labels[1] = labels[1].setPosition(relPos[0], relPos[1]);
    
    relPos = relativePosition(labels[1], RelativePoint.TOP_RIGHT, distLblToFieldX, abs(fieldHeight - dropItemHeight) / 2);
    dropDownLists[2] = (MyDropdownList)dropDownLists[2].setPosition(relPos[0], relPos[1]);
    // Dimension label and fields
    int idxDim = 0;
    
    relPos = relativePosition(labels[1], RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
    // Update position and label text of the dimension fields based on the selected shape from the Shape dropDown List
    while (idxDim < shapeDefFields.size()) {
      Textfield dimField = shapeDefFields.get(idxDim);
      
      if (!dimField.isVisible()) { break; }
      
      int lblIdx = idxDim + 10;
        
      labels[lblIdx] = labels[lblIdx].setPosition(relPos[0], relPos[1]);
      relPos = relativePosition(labels[lblIdx], RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
      
      shapeDefFields.set(idxDim, dimField.setPosition(relPos[0], relPos[1]) );
      relPos = relativePosition(labels[lblIdx], RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
      
      ++idxDim;
    }
    
    // Fill color label and dropdown
    labels[2] = labels[2].setPosition(relPos[0], relPos[1]);
    
    relPos = relativePosition(labels[2], RelativePoint.TOP_RIGHT, distLblToFieldX, abs(fieldHeight - dropItemHeight) / 2);
    dropDownLists[0] = (MyDropdownList)dropDownLists[0].setPosition(relPos[0], relPos[1]);
    // Outline color label and dropdown
    relPos = relativePosition(labels[2], RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
    labels[3] = labels[3].setPosition(relPos[0], relPos[1]);
    
    relPos = relativePosition(labels[3], RelativePoint.TOP_RIGHT, distLblToFieldX, abs(fieldHeight - dropItemHeight) / 2);
    dropDownLists[1] = (MyDropdownList)dropDownLists[1].setPosition(relPos[0], relPos[1]);
    // Create button
    relPos = relativePosition(labels[3], RelativePoint.BOTTOM_RIGHT, distLblToFieldX, distBtwFieldsY);
    singleButtons[0] = singleButtons[0].setPosition(relPos[0], relPos[1]);
    // Clear button
    relPos = relativePosition(singleButtons[0], RelativePoint.TOP_RIGHT, offsetX, 0);
    singleButtons[2] = singleButtons[2].setPosition(relPos[0], relPos[1]);
  }
  
  /**
   * Updates the positions of all the contents of the world object editing window.
   */
  private void updateEditWindowContentPositions() {
    
    // Object type dropdown
    int[] relPos = new int[] { offsetX, offsetX };
    dropDownLists[3] = (MyDropdownList)dropDownLists[3].setPosition(relPos[0], relPos[1]);
    // Object list dropdown
    relPos = relativePosition(dropDownLists[3], RelativePoint.TOP_RIGHT, offsetX, 0);
    dropDownLists[4] = (MyDropdownList)dropDownLists[4].setPosition(relPos[0], relPos[1]);
    
    // X label and field
    relPos = relativePosition(dropDownLists[3], RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
    labels[4] = labels[4].setPosition(relPos[0], relPos[1]);
    
    relPos = relativePosition(labels[4], RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
    objOrientation[0] = objOrientation[0].setPosition(relPos[0], relPos[1]);
    // Y label and field
    relPos = relativePosition(labels[4], RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
    labels[5] = labels[5].setPosition(relPos[0], relPos[1]);
    
    relPos = relativePosition(labels[5], RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
    objOrientation[1] = objOrientation[1].setPosition(relPos[0], relPos[1]);
    // Z label and field
    relPos = relativePosition(labels[5], RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
    labels[6] = labels[6].setPosition(relPos[0], relPos[1]);;
    
    relPos = relativePosition(labels[6], RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
    objOrientation[2] = objOrientation[2].setPosition(relPos[0], relPos[1]);
    // W label and field
    relPos = relativePosition(labels[6], RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
    labels[7] = labels[7].setPosition(relPos[0], relPos[1]);
    
    relPos = relativePosition(labels[7], RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
    objOrientation[3] = objOrientation[3].setPosition(relPos[0], relPos[1]);
    // P label and field
    relPos = relativePosition(labels[7], RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
    labels[8] = labels[8].setPosition(relPos[0], relPos[1]);
    
    relPos = relativePosition(labels[8], RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
    objOrientation[4] = objOrientation[4].setPosition(relPos[0], relPos[1]);
    // R label and field
    relPos = relativePosition(labels[8], RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
    labels[9] = labels[9].setPosition(relPos[0], relPos[1]);
    
    relPos = relativePosition(labels[9], RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
    objOrientation[5] = objOrientation[5].setPosition(relPos[0], relPos[1]);
   
    relPos = relativePosition(labels[9], RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
    
    // Check value of the ObjType
    Object val = dropDownLists[3].getActiveLabelValue();
    
    if (val instanceof Float) {
      
      if (((Float)val) == 0.0) {
         // Reference fxiture (for Parts only) label and dropdown
        labels[14] = labels[14].setPosition(relPos[0], relPos[1]).show();
        relPos = relativePosition(labels[14], RelativePoint.TOP_RIGHT, distLblToFieldX, abs(fieldHeight - dropItemHeight) / 2);
        
        dropDownLists[5] = (MyDropdownList)dropDownLists[5].setPosition(relPos[0], relPos[1]).show();
        relPos = relativePosition(labels[14], RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
        
      } else {
        // Fixtures do not have a reference object
        labels[14].hide();
        dropDownLists[5] = (MyDropdownList)dropDownLists[5].hide();
      }
    }
    
    // Confirm button
    singleButtons[1] = singleButtons[1].setPosition(relPos[0], relPos[1]);
    // Clear button
    relPos = relativePosition(singleButtons[1], RelativePoint.TOP_RIGHT, offsetX, 0);
    singleButtons[2] = singleButtons[2].setPosition(relPos[0], relPos[1]);
  }
  
  /**
   * Returns the object that is currently being edited
   * in the world object editing menu.
   */
  protected WorldObject getActiveWorldObject() {
    Object wldObj = dropDownLists[4].getActiveLabelValue();
    
    if (editObjWindow.isVisible() && wldObj instanceof WorldObject) {
      return (WorldObject)wldObj;
    } else {
      return null;
    }
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
    
    // Check value of the ObjType dropdown list
    Object val = dropDownLists[3].getActiveLabelValue();
    ArrayList wldObjects;
    
    if (val != null && ((Float)val) == 1.0) {
      wldObjects = FIXTURES;
    } else {
      // Add the list of parts as default
      wldObjects = PARTS;
    }
    
    dropDownLists[4] = (MyDropdownList)dropDownLists[4].clear();
    for (Object obj : wldObjects) {
      // Add each world object to the dropdown list
      dropDownLists[4].addItem(obj.toString(), obj);
    }
    dropDownLists[4].updateActiveLabel();
    
    dropDownLists[5] = (MyDropdownList)dropDownLists[5].clear();
    for (Fixture obj : FIXTURES) {
      // Add each fixture to the dropdown list
      dropDownLists[5].addItem(obj.toString(), obj);
    }
    dropDownLists[5].updateActiveLabel();
  }
  
  /**
   * Update how many of the dimension field and label pairs are displayed in
   * the create world object window based on which shape type is chosen from the shape dropdown list.
   */
  private void updateDimFieldsAndLabels() {
    ShapeType selectedShape = (ShapeType)dropDownLists[2].getActiveLabelValue();
    int dimSize = 0;
    String[] lblNames;
    
    // Define the label text and the number of dimensionos fields to display
    if (selectedShape == ShapeType.BOX) {
      dimSize = 3;
      lblNames = new String[] { "Length:", "Height:", "Width" };
    } else if (selectedShape == ShapeType.CYLINDER) {
      dimSize = 2;
      lblNames = new String[] { "Radius", "Height" };
    } else if (selectedShape == ShapeType.MODEL) {
      Object objType = dropDownLists[3].getActiveLabelValue();
      
      if (objType instanceof Float && (Float)objType == 0.0) {
        // Define the dimensions of the bounding box of the Part
        dimSize = 4;
        lblNames = new String[] { "Source:", "Length:", "Height", "Width" };
      } else {
        dimSize = 1;
        lblNames = new String[] { "Source:" };
      }

    } else {
      dimSize = 0;
      lblNames = new String[0];
    }
    
    int idxDim = 0;
    
    // Show a number of dimension fields and labels equal to the value of idxDim
    for (; idxDim < dimSize; ++idxDim) {
      int lblIdx = idxDim + 10;
      
      labels[lblIdx] = labels[lblIdx].setText(lblNames[idxDim]).show();
      shapeDefFields.set(idxDim, shapeDefFields.get(idxDim).show());
    }
    
    while(idxDim < shapeDefFields.size()) {
      // Hide remaining dimension fields and labels
      int lblIdx = idxDim + 10;
      shapeDefFields.set(idxDim, shapeDefFields.get(idxDim).hide());
      labels[lblIdx] = labels[lblIdx].hide();
      ++idxDim;
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
   * Creates a world object
   */
  public WorldObject createWorldObject() {
    // Check the object type dropdown list
    Object val = dropDownLists[3].getActiveLabelValue();
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
        ShapeType type = (ShapeType)dropDownLists[2].getActiveLabelValue();
          
        color fill = (Integer)dropDownLists[0].getActiveLabelValue(),
              outline = (Integer)dropDownLists[1].getActiveLabelValue();
        
        switch(type) {
          case BOX:
            float len = Float.parseFloat( shapeDefFields.get(0).getText() ),
                  hgt = Float.parseFloat( shapeDefFields.get(1).getText() ),
                  wdh = Float.parseFloat( shapeDefFields.get(2).getText() );
            // Construct a box shape
            wldObj = new Part(name, fill, outline, len, hgt, wdh);
            break;
          case CYLINDER:
            float rad = Float.parseFloat( shapeDefFields.get(0).getText() );
            hgt = Float.parseFloat( shapeDefFields.get(1).getText() );
            // Construct a cylinder
            wldObj = new Part(name, fill, outline, rad, hgt);
            break;
          case MODEL:
            String srcFile = shapeDefFields.get(0).getText();
            len = Float.parseFloat( shapeDefFields.get(1).getText() );
            hgt = Float.parseFloat( shapeDefFields.get(2).getText() );
            wdh = Float.parseFloat( shapeDefFields.get(3).getText() );
            // Construct a complex model
            ModelShape model = new ModelShape(srcFile, fill, outline);
            wldObj = new Part(name, model, len, hgt, wdh);
            break;
          default:
        }
        
      } else if (objectType == 1.0) {
        // Create a fixture
        String name = objName.getText();
        ShapeType type = (ShapeType)dropDownLists[2].getActiveLabelValue();
          
        color fill = (Integer)dropDownLists[0].getActiveLabelValue(),
              outline = (Integer)dropDownLists[1].getActiveLabelValue();
        
        switch(type) {
          case BOX:
            float len = Float.parseFloat( shapeDefFields.get(0).getText() ),
                  hgt = Float.parseFloat( shapeDefFields.get(1).getText() ),
                  wdh = Float.parseFloat( shapeDefFields.get(2).getText() );
            // Construct a box
            wldObj = new Fixture(name, fill, outline, len, hgt, wdh);
            break;
          case CYLINDER:
            float rad = Float.parseFloat( shapeDefFields.get(0).getText() );
            hgt = Float.parseFloat( shapeDefFields.get(1).getText() );
            // Construct a cylinder
            wldObj = new Fixture(name, fill, outline, rad, hgt);
            break;
          case MODEL:
            String srcFile = shapeDefFields.get(0).getText();
            len = Float.parseFloat( shapeDefFields.get(1).getText() );
            hgt = Float.parseFloat( shapeDefFields.get(2).getText() );
            wdh = Float.parseFloat( shapeDefFields.get(3).getText() );
            // Construct a complex model
            ModelShape model = new ModelShape(srcFile, fill, outline);
            wldObj = new Fixture(name, model);
            break;
          default:
        }
      }
    
    } catch (NumberFormatException NFEx) {
      println("Invalid number input!");
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
      try {
        // Pull from all input fields
        String xFieldVal = objOrientation[0].getText(), yFieldVal = objOrientation[1].getText(),
               zFieldVal = objOrientation[2].getText(), wFieldVal = objOrientation[3].getText(),
               pFieldVal = objOrientation[4].getText(), rFieldVal = objOrientation[5].getText();
        // Convert origin position and orientation into the World Frame
        PVector oPosition = convertNativeToWorld( toEdit.getCenter() ),
                oWPR = convertNativeToWorld( matrixToEuler(toEdit.getOrientationAxes()).mult(RAD_TO_DEG) );
        // Update x value
        if (xFieldVal != null && !xFieldVal.equals("")) {
          float val = Float.parseFloat(xFieldVal);
          // Bring value within the range [-9999, 9999]
          val = max(-9999f, min(val, 9999f));
          oPosition.x = val;
        }
        // Update y value
        if (yFieldVal != null && !yFieldVal.equals("")) {
          float val = Float.parseFloat(yFieldVal);
          // Bring value within the range [-9999, 9999]
          val = max(-9999f, min(val, 9999f));
          oPosition.y = val;
        }
        // Update z value
        if (zFieldVal != null && !zFieldVal.equals("")) {
          float val = Float.parseFloat(zFieldVal);
          // Bring value within the range [-9999, 9999]
          val = max(-9999f, min(val, 9999f));
          oPosition.z = val;
        }
        // Update w angle
        if (wFieldVal != null && !wFieldVal.equals("")) {
          float val = Float.parseFloat(wFieldVal);
          // Bring value within the range [-9999, 9999]
          val = max(-9999f, min(val, 9999f));
          oWPR.x = val;
        }
        // Update p angle
        if (pFieldVal != null && !pFieldVal.equals("")) {
          float val = Float.parseFloat(pFieldVal);
          // Bring value within the range [-9999, 9999]
          val = max(-9999f, min(val, 9999f));
          oWPR.y = val;
        }
        // Update r angle
        if (rFieldVal != null && !rFieldVal.equals("")) {
          float val = Float.parseFloat(rFieldVal);
          // Bring value within the range [-9999, 9999]
          val = max(-9999f, min(val, 9999f));
          oWPR.z = val;
        }
        
        // Convert values from the World to the Native coordinate system
        PVector position = convertWorldToNative( oPosition );
        PVector wpr = convertWorldToNative( oWPR.mult(DEG_TO_RAD) );
        float[][] orientation = eulerToMatrix(wpr);
        // Update the Objects position and orientaion
        toEdit.setCenter(position);
        toEdit.setOrientationAxes(orientation);
        
        if (toEdit instanceof Part) {
          // Set the reference of the Part to the currently active fixture
          Fixture refFixture = (Fixture)dropDownLists[5].getActiveLabelValue();
          ((Part)toEdit).setFixtureRef(refFixture);
        }
      } catch (NumberFormatException NFEx) {
        println("Invalid number input!");
      } catch (NullPointerException NPEx) {
        println("Missing parameter!");
      }
    } else {
      println("No object selected!");
    }
    
  }
  
  /**
   * Reset the base label of every dropdown list.
   */
  private void resetDropdownLabels() {
    for (MyDropdownList list : dropDownLists) {
      list.resetLabel();
    }
    
    for (int idxDim = 0; idxDim < shapeDefFields.size(); ++idxDim) {
      // Hide remaining dimension fields and labels
      int lblIdx = idxDim + 10;
      shapeDefFields.set(idxDim, shapeDefFields.get(idxDim).hide());
      labels[lblIdx] = labels[lblIdx].hide();
      ++idxDim;
    }
    
    labels[14].hide();
    dropDownLists[5] = (MyDropdownList)dropDownLists[5].hide();
    updateDimFieldsAndLabels();
  }
  
  /**
   * Clear function for the Clear button
   */
  public void clearObjCreationInput() {
    clearGroupInputFields(createObjWindow);
    updateDimFieldsAndLabels();
  }
  
  /**
   * Reinitializes any controller interface in the given group that accepts user
   * input; currently only text fields and dropdown lists are updated.
   */
  private void clearGroupInputFields(Group g) {
    List<ControllerInterface<?>> contents = UIManager.getAll();
    
    for (ControllerInterface<?> controller : contents) {
      
      if (g != null || controller.getParent().equals(g)) {
        
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

}
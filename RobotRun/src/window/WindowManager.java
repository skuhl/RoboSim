package window;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import controlP5.Background;
import controlP5.Button;
import controlP5.ControlP5;
import controlP5.ControllerInterface;
import controlP5.DropdownList;
import controlP5.Group;
import controlP5.Textarea;
import controlP5.Textfield;
import geom.Box;
import geom.Cylinder;
import geom.DimType;
import geom.ModelShape;
import geom.Part;
import geom.Shape;
import geom.ShapeType;
import geom.WorldObject;
import processing.core.PFont;
import processing.core.PVector;
import robot.RoboticArm;
import robot.EEMapping;
import robot.Fixture;
import robot.RobotRun;
import robot.Scenario;
import ui.AxesDisplay;
import ui.ButtonTabs;
import ui.MyDropdownList;
import ui.RelativePoint;

public class WindowManager {
	public static final int offsetX = 10,
			distBtwFieldsY = 15,
			distLblToFieldX = 5,
			distFieldToFieldX = 20,
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

	private final ControlP5 UIManager;
	private final RobotRun app;

	private Group createObjWindow, editObjWindow,
	sharedElements, scenarioWindow, miscWindow;
	
	private final ButtonTabs windowTabs;
	private final Background background;

	private final int buttonDefColor, buttonActColor;

	/**
	 * Creates a new window with the given ControlP5 object as the parent
	 * and the given fonts which will be applied to the text in the window.
	 */
	 public WindowManager(RobotRun appRef, ControlP5 manager, PFont small, PFont medium) {
		// Initialize content fields
		 UIManager = manager;
		 app = appRef;

		 buttonDefColor = app.color(70);
		 buttonActColor = app.color(220, 40, 40);

		 // Create some temporary color and dimension variables
		 int bkgrdColor = app.color(210),
			fieldTxtColor = app.color(0),
			fieldCurColor = app.color(0),
			fieldActColor = app.color(255, 0, 0),
			fieldBkgrdColor = app.color(255),
			fieldFrgrdColor = app.color(0),
			buttonTxtColor = app.color(255);

		 int[] relPos = new int[] { 0, 0 };
		 
		 String[] windowList = new String[] { "Hide", "Robot1", "Create", "Edit", "Scenario", "Misc" };
		 
		 // Create window tab bar
		 windowTabs = (ButtonTabs)(new ButtonTabs(UIManager, "List:")
				 // Sets button text color
				 .setColorValue(buttonTxtColor)
				 .setColorBackground(buttonDefColor)
				 .setColorActive(buttonActColor)
				 .setPosition(relPos[0], relPos[1])
				 .setSize(440, tButtonHeight));

		 windowTabs.getCaptionLabel().setFont(medium);
		 windowTabs.addItems(windowList);

		 // Initialize camera view buttons
		 UIManager.addButton("FrontView")
				 .setCaptionLabel("F")
				 .setColorValue(buttonTxtColor)
				 .setColorBackground(buttonDefColor)
				 .setColorActive(buttonActColor)
				 .moveTo(createObjWindow)
				 .setPosition(0, 0)
				 .setSize(sButtonWidth, sButtonHeight)
				 .hide()
				 .getCaptionLabel().setFont(small);

		 UIManager.addButton("BackView")
				 .setCaptionLabel("Bk")
				 .setColorValue(buttonTxtColor)
				 .setColorBackground(buttonDefColor)
				 .setColorActive(buttonActColor)
				 .moveTo(createObjWindow)
				 .setPosition(0, 0)
				 .setSize(sButtonWidth, sButtonHeight)
				 .hide()
				 .getCaptionLabel().setFont(small);

		 UIManager.addButton("LeftView")
				 .setCaptionLabel("L")
				 .setColorValue(buttonTxtColor)
				 .setColorBackground(buttonDefColor)
				 .setColorActive(buttonActColor)
				 .moveTo(createObjWindow)
				 .setPosition(0, 0)
				 .setSize(sButtonWidth, sButtonHeight)
				 .hide()
				 .getCaptionLabel().setFont(small);

		 UIManager.addButton("RightView")
				 .setCaptionLabel("R")
				 .setColorValue(buttonTxtColor)
				 .setColorBackground(buttonDefColor)
				 .setColorActive(buttonActColor)
				 .moveTo(createObjWindow)
				 .setPosition(0, 0)
				 .setSize(sButtonWidth, sButtonHeight)
				 .hide()
				 .getCaptionLabel().setFont(small);

		 UIManager.addButton("TopView")
				 .setCaptionLabel("T")
				 .setColorValue(buttonTxtColor)
				 .setColorBackground(buttonDefColor)
				 .setColorActive(buttonActColor)
				 .moveTo(createObjWindow)
				 .setPosition(0, 0)
				 .setSize(sButtonWidth, sButtonHeight)
				 .hide()
				 .getCaptionLabel().setFont(small);

		 UIManager.addButton("BottomView")
				 .setCaptionLabel("Bt")
				 .setColorValue(buttonTxtColor)
				 .setColorBackground(buttonDefColor)
				 .setColorActive(buttonActColor)
				 .moveTo(createObjWindow)
				 .setPosition(0, 0)
				 .setSize(sButtonWidth, sButtonHeight)
				 .hide()
				 .getCaptionLabel().setFont(small);


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
		 
		 miscWindow = UIManager.addGroup("MISC").setPosition(relPos[0], relPos[1])
				 .setBackgroundColor(bkgrdColor)
				 .setSize(windowTabs.getWidth(), 0)
				 .hideBar();

		 // Initialize window contents
		 for (int idx = 0; idx < 3; ++idx) {
			 UIManager.addTextarea(String.format("Dim%dLbl", idx), String.format("Dim(%d):", idx), 0, 0, mLblWidth, sButtonHeight)
					 .setFont(medium)
					 .setColor(fieldTxtColor)
					 .setColorActive(fieldActColor)
					 .setColorBackground(bkgrdColor)
					 .setColorForeground(bkgrdColor)
					 .moveTo(sharedElements);

			 UIManager.addTextfield(String.format("Dim%d", idx), 0, 0, fieldWidth, fieldHeight)
					 .setColor(fieldTxtColor)
					 .setColorCursor(fieldCurColor)
					 .setColorActive(fieldActColor)
					 .setColorLabel(bkgrdColor)
					 .setColorBackground(fieldBkgrdColor)
					 .setColorForeground(fieldFrgrdColor)
					 .moveTo(sharedElements);
		 }

		 UIManager.addTextarea("ObjTypeLbl", "Type:", 0, 0, mLblWidth, sButtonHeight)
				 .setFont(medium)
				 .setColor(fieldTxtColor)
				 .setColorActive(fieldActColor)
				 .setColorBackground(bkgrdColor)
				 .setColorForeground(bkgrdColor)
				 .moveTo(createObjWindow);

		 UIManager.addTextarea("ObjNameLbl", "Name:", 0, 0, sLblWidth, fieldHeight)
				 .setFont(medium)
				 .setColor(fieldTxtColor)
				 .setColorActive(fieldActColor)
				 .setColorBackground(bkgrdColor)
				 .setColorForeground(bkgrdColor)
				 .moveTo(createObjWindow);

		 UIManager.addTextfield("ObjName", 0, 0, fieldWidth, fieldHeight)
				 .setColor(fieldTxtColor)
				 .setColorCursor(fieldCurColor)
				 .setColorActive(fieldActColor)
				 .setColorLabel(bkgrdColor)
				 .setColorBackground(fieldBkgrdColor)
				 .setColorForeground(fieldFrgrdColor)
				 .moveTo(createObjWindow);

		 UIManager.addTextarea("ShapeLbl", "Shape:", 0, 0, mLblWidth, sButtonHeight)
				 .setFont(medium)
				 .setColor(fieldTxtColor)
				 .setColorActive(fieldActColor)
				 .setColorBackground(bkgrdColor)
				 .setColorForeground(bkgrdColor)
				 .moveTo(createObjWindow);

		 UIManager.addTextarea("FillLbl", "Fill:", 0, 0, mLblWidth, sButtonHeight)
				 .setFont(medium)
				 .setColor(fieldTxtColor)
				 .setColorActive(fieldActColor)
				 .setColorBackground(bkgrdColor)
				 .setColorForeground(bkgrdColor)
				 .moveTo(createObjWindow);

		 UIManager.addTextarea("OutlineLbl", "Outline:", 0, 0, mLblWidth, sButtonHeight)
				 .setFont(medium)
				 .setColor(fieldTxtColor)
				 .setColorActive(fieldActColor)
				 .setColorBackground(bkgrdColor)
				 .setColorForeground(bkgrdColor)
				 .moveTo(createObjWindow);

		 UIManager.addButton("CreateWldObj")
				 .setCaptionLabel("Create")
				 .setColorValue(buttonTxtColor)
				 .setColorBackground(buttonDefColor)
				 .setColorActive(buttonActColor)
				 .moveTo(createObjWindow)
				 .setPosition(0, 0)
				 .setSize(mButtonWidth, sButtonHeight)
				 .getCaptionLabel().setFont(small);

		 UIManager.addButton("ClearFields")
				 .setCaptionLabel("Clear")
				 .setColorValue(buttonTxtColor)
				 .setColorBackground(buttonDefColor)
				 .setColorActive(buttonActColor)
				 .moveTo(sharedElements)
				 .setPosition(0, 0)
				 .setSize(mButtonWidth, sButtonHeight)
				 .getCaptionLabel().setFont(small);

		 UIManager.addTextarea("ObjLabel", "Object:", 0, 0, mLblWidth, fieldHeight)
				 .setFont(medium)
				 .setColor(fieldTxtColor)
				 .setColorActive(fieldActColor)
				 .setColorBackground(bkgrdColor)
				 .setColorForeground(bkgrdColor)
				 .moveTo(editObjWindow);
		 
		 UIManager.addTextarea("Blank", "Inputs", 0, 0, lLblWidth, fieldHeight)
			 .setFont(medium)
			 .setColor(fieldTxtColor)
			 .setColorActive(fieldActColor)
			 .setColorBackground(bkgrdColor)
			 .setColorForeground(bkgrdColor)
			 .moveTo(editObjWindow);
		 
		 UIManager.addTextarea("Current", "Current", 0, 0, fieldWidth, fieldHeight)
			 .setFont(medium)
			 .setColor(fieldTxtColor)
			 .setColorActive(fieldActColor)
			 .setColorBackground(bkgrdColor)
			 .setColorForeground(bkgrdColor)
			 .moveTo(editObjWindow);
		 
		 UIManager.addTextarea("Default", "Default", 0, 0, fieldWidth, fieldHeight)
			 .setFont(medium)
			 .setColor(fieldTxtColor)
			 .setColorActive(fieldActColor)
			 .setColorBackground(bkgrdColor)
			 .setColorForeground(bkgrdColor)
			 .moveTo(editObjWindow);
		 
		 UIManager.addTextarea("AFLbl", "Auto Fill", 0, 0, lLblWidth, fieldHeight)
			 .setFont(medium)
			 .setColor(fieldTxtColor)
			 .setColorActive(fieldActColor)
			 .setColorBackground(bkgrdColor)
			 .setColorForeground(bkgrdColor)
			 .moveTo(editObjWindow);
		 
		 UIManager.addButton("DefIntoCur")
				  .setCaptionLabel("Default")
				  .setColorValue(buttonTxtColor)
				  .setColorBackground(buttonDefColor)
				  .setColorActive(buttonActColor)
				  .moveTo(editObjWindow)
				  .setPosition(0, 0)
				  .setSize(mButtonWidth, sButtonHeight)
				  .getCaptionLabel().setFont(small);
		 
		 UIManager.addButton("CurIntoDef")
				  .setCaptionLabel("Current")
				  .setColorValue(buttonTxtColor)
				  .setColorBackground(buttonDefColor)
				  .setColorActive(buttonActColor)
				  .moveTo(editObjWindow)
				  .setPosition(0, 0)
				  .setSize(mButtonWidth, sButtonHeight)
				  .getCaptionLabel().setFont(small);

		 UIManager.addTextarea("XLbl", "X Position:", 0, 0, lLblWidth, fieldHeight)
				 .setFont(medium)
				 .setColor(fieldTxtColor)
				 .setColorActive(fieldActColor)
				 .setColorBackground(bkgrdColor)
				 .setColorForeground(bkgrdColor)
				 .moveTo(editObjWindow);

		 UIManager.addTextfield("XCur", 0, 0, fieldWidth, fieldHeight)
				 .setColor(fieldTxtColor)
				 .setColorCursor(fieldCurColor)
				 .setColorActive(fieldActColor)
				 .setColorLabel(bkgrdColor)
				 .setColorBackground(fieldBkgrdColor)
				 .setColorForeground(fieldFrgrdColor)
				 .moveTo(editObjWindow);
		 
		 UIManager.addTextfield("XDef", 0, 0, fieldWidth, fieldHeight)
			 .setColor(fieldTxtColor)
			 .setColorCursor(fieldCurColor)
			 .setColorActive(fieldActColor)
			 .setColorLabel(bkgrdColor)
			 .setColorBackground(fieldBkgrdColor)
			 .setColorForeground(fieldFrgrdColor)
			 .moveTo(editObjWindow);

		 UIManager.addTextarea("YLbl", "Y Position:", 0, 0, lLblWidth, fieldHeight)
				 .setFont(medium)
				 .setColor(fieldTxtColor)
				 .setColorActive(fieldActColor)
				 .setColorBackground(bkgrdColor)
				 .setColorForeground(bkgrdColor)
				 .moveTo(editObjWindow);

		 UIManager.addTextfield("YCur", 0, 0, fieldWidth, fieldHeight)
				 .setColor(fieldTxtColor)
				 .setColorCursor(fieldCurColor)
				 .setColorActive(fieldActColor)
				 .setColorLabel(bkgrdColor)
				 .setColorBackground(fieldBkgrdColor)
				 .setColorForeground(fieldFrgrdColor)
				 .moveTo(editObjWindow);
		 
		 UIManager.addTextfield("YDef", 0, 0, fieldWidth, fieldHeight)
			 .setColor(fieldTxtColor)
			 .setColorCursor(fieldCurColor)
			 .setColorActive(fieldActColor)
			 .setColorLabel(bkgrdColor)
			 .setColorBackground(fieldBkgrdColor)
			 .setColorForeground(fieldFrgrdColor)
			 .moveTo(editObjWindow);

		 UIManager.addTextarea("ZLbl", "Z Position:", 0, 0, lLblWidth, fieldHeight)
				 .setFont(medium)
				 .setColor(fieldTxtColor)
				 .setColorActive(fieldActColor)
				 .setColorBackground(bkgrdColor)
				 .setColorForeground(bkgrdColor)
				 .moveTo(editObjWindow);

		 UIManager.addTextfield("ZCur", 0, 0, fieldWidth, fieldHeight)
				 .setColor(fieldTxtColor)
				 .setColorCursor(fieldCurColor)
				 .setColorActive(fieldActColor)
				 .setColorLabel(bkgrdColor)
				 .setColorBackground(fieldBkgrdColor)
				 .setColorForeground(fieldFrgrdColor)
				 .moveTo(editObjWindow);
		 
		 UIManager.addTextfield("ZDef", 0, 0, fieldWidth, fieldHeight)
			 .setColor(fieldTxtColor)
			 .setColorCursor(fieldCurColor)
			 .setColorActive(fieldActColor)
			 .setColorLabel(bkgrdColor)
			 .setColorBackground(fieldBkgrdColor)
			 .setColorForeground(fieldFrgrdColor)
			 .moveTo(editObjWindow);

		 UIManager.addTextarea("WLbl", "W Rotation:", 0, 0, lLblWidth, fieldHeight)
				 .setFont(medium)
				 .setColor(fieldTxtColor)
				 .setColorActive(fieldActColor)
				 .setColorBackground(bkgrdColor)
				 .setColorForeground(bkgrdColor)
				 .moveTo(editObjWindow);

		 UIManager.addTextfield("WCur", 0, 0, fieldWidth, fieldHeight)
				 .setColor(fieldTxtColor)
				 .setColorCursor(fieldCurColor)
				 .setColorActive(fieldActColor)
				 .setColorLabel(bkgrdColor)
				 .setColorBackground(fieldBkgrdColor)
				 .setColorForeground(fieldFrgrdColor)
				 .moveTo(editObjWindow);
		 
		 UIManager.addTextfield("WDef", 0, 0, fieldWidth, fieldHeight)
			 .setColor(fieldTxtColor)
			 .setColorCursor(fieldCurColor)
			 .setColorActive(fieldActColor)
			 .setColorLabel(bkgrdColor)
			 .setColorBackground(fieldBkgrdColor)
			 .setColorForeground(fieldFrgrdColor)
			 .moveTo(editObjWindow);

		 UIManager.addTextarea("PLbl", "P Rotation:", 0, 0, lLblWidth, fieldHeight)
				 .setFont(medium)
				 .setColor(fieldTxtColor)
				 .setColorActive(fieldActColor)
				 .setColorBackground(bkgrdColor)
				 .setColorForeground(bkgrdColor)
				 .moveTo(editObjWindow);

		 UIManager.addTextfield("PCur", 0, 0, fieldWidth, fieldHeight)
				 .setColor(fieldTxtColor)
				 .setColorCursor(fieldCurColor)
				 .setColorActive(fieldActColor)
				 .setColorLabel(bkgrdColor)
				 .setColorBackground(fieldBkgrdColor)
				 .setColorForeground(fieldFrgrdColor)
				 .moveTo(editObjWindow);
		 
		 UIManager.addTextfield("PDef", 0, 0, fieldWidth, fieldHeight)
			 .setColor(fieldTxtColor)
			 .setColorCursor(fieldCurColor)
			 .setColorActive(fieldActColor)
			 .setColorLabel(bkgrdColor)
			 .setColorBackground(fieldBkgrdColor)
			 .setColorForeground(fieldFrgrdColor)
			 .moveTo(editObjWindow);

		 UIManager.addTextarea("RLbl", "R Rotation:", 0, 0, lLblWidth, fieldHeight)
				 .setFont(medium)
				 .setColor(fieldTxtColor)
				 .setColorActive(fieldActColor)
				 .setColorBackground(bkgrdColor)
				 .setColorForeground(bkgrdColor)
				 .moveTo(editObjWindow);

		 UIManager.addTextfield("RCur", 0, 0, fieldWidth, fieldHeight)
				 .setColor(fieldTxtColor)
				 .setColorCursor(fieldCurColor)
				 .setColorActive(fieldActColor)
				 .setColorLabel(bkgrdColor)
				 .setColorBackground(fieldBkgrdColor)
				 .setColorForeground(fieldFrgrdColor)
				 .moveTo(editObjWindow);
		 
		 UIManager.addTextfield("RDef", 0, 0, fieldWidth, fieldHeight)
			 .setColor(fieldTxtColor)
			 .setColorCursor(fieldCurColor)
			 .setColorActive(fieldActColor)
			 .setColorLabel(bkgrdColor)
			 .setColorBackground(fieldBkgrdColor)
			 .setColorForeground(fieldFrgrdColor)
			 .moveTo(editObjWindow);

		 UIManager.addTextarea("RefLbl", "Reference:", 0, 0, lLblWidth, sButtonHeight)
				 .setFont(medium)
				 .setColor(fieldTxtColor)
				 .setColorActive(fieldActColor)
				 .setColorBackground(bkgrdColor)
				 .setColorForeground(bkgrdColor)
				 .moveTo(editObjWindow);

		 UIManager.addButton("UpdateWldObj")
				 .setCaptionLabel("Confirm")
				 .setColorValue(buttonTxtColor)
				 .setColorBackground(buttonDefColor)
				 .setColorActive(buttonActColor)
				 .moveTo(editObjWindow)
				 .setSize(mButtonWidth, sButtonHeight)
				 .getCaptionLabel().setFont(small);

		 UIManager.addButton("DeleteWldObj")
				 .setCaptionLabel("Delete")
				 .setColorValue(buttonTxtColor)
				 .setColorBackground(buttonDefColor)
				 .setColorActive(buttonActColor)
				 .moveTo(editObjWindow)
				 .setSize(mButtonWidth, sButtonHeight)
				 .getCaptionLabel().setFont(small);

		 UIManager.addTextarea("NewScenarioLbl", "Name:", 0, 0, sLblWidth, fieldHeight)
				 .setFont(medium)
				 .setColor(fieldTxtColor)
				 .setColorActive(fieldActColor)
				 .setColorBackground(bkgrdColor)
				 .setColorForeground(bkgrdColor)
				 .moveTo(scenarioWindow);

		 UIManager.addTextfield("ScenarioName", 0, 0, fieldWidth, fieldHeight)
				 .setColor(fieldTxtColor)
				 .setColorCursor(fieldCurColor)
				 .setColorActive(fieldActColor)
				 .setColorLabel(bkgrdColor)
				 .setColorBackground(fieldBkgrdColor)
				 .setColorForeground(fieldFrgrdColor)
				 .moveTo(scenarioWindow);

		 UIManager.addButton("NewScenario")
				 .setCaptionLabel("New")
				 .setColorValue(buttonTxtColor)
				 .setColorBackground(buttonDefColor)
				 .setColorActive(buttonActColor)
				 .moveTo(scenarioWindow)
				 .setSize(mButtonWidth, sButtonHeight)
				 .getCaptionLabel().setFont(small);

		 UIManager.addTextarea("ActiveScenarioLbl", "Scenario:", 0, 0, lLblWidth, sButtonHeight)
				 .setFont(medium)
				 .setColor(fieldTxtColor)
				 .setColorActive(fieldActColor)
				 .setColorBackground(bkgrdColor)
				 .setColorForeground(bkgrdColor)
				 .moveTo(scenarioWindow);
		 
		 UIManager.addButton("SetScenario")
				 .setCaptionLabel("Load")
				 .setColorValue(buttonTxtColor)
				 .setColorBackground(buttonDefColor)
				 .setColorActive(buttonActColor)
				 .moveTo(scenarioWindow)
				 .setSize(mButtonWidth, sButtonHeight)
				 .getCaptionLabel().setFont(small);
		
		 UIManager.addTextarea("ActiveAxesDisplay", "Axes Display:", 0, 0, lLblWidth, sButtonHeight)
				 .setFont(medium)
				 .setColor(fieldTxtColor)
				 .setColorActive(fieldActColor)
				 .setColorBackground(bkgrdColor)
				 .setColorForeground(bkgrdColor)
				 .moveTo(miscWindow);
		 
		 UIManager.addTextarea("ActiveEEDisplay", "EE Display:", 0, 0, lLblWidth, sButtonHeight)
				 .setFont(medium)
				 .setColor(fieldTxtColor)
				 .setColorActive(fieldActColor)
				 .setColorBackground(bkgrdColor)
				 .setColorForeground(bkgrdColor)
				 .moveTo(miscWindow);
		 
		 UIManager.addButton("ToggleOBBs")
				 .setCaptionLabel("Hide OBBs")
				 .setColorValue(buttonTxtColor)
				 .setColorBackground(buttonDefColor)
				 .setColorActive(buttonActColor)
				 .moveTo(miscWindow)
				 .setSize(lButtonWidth, sButtonHeight)
				 .getCaptionLabel().setFont(small);
		 
		 UIManager.addButton("ToggleRobot")
				 .setCaptionLabel("Add Robot")
				 .setColorValue(buttonTxtColor)
				 .setColorBackground(buttonDefColor)
				 .setColorActive(buttonActColor)
				 .moveTo(miscWindow)
				 .setSize(lButtonWidth, sButtonHeight)
				 .getCaptionLabel().setFont(small);

		 // Initialize dropdown lists
		 MyDropdownList dropdown = (MyDropdownList)(new MyDropdownList(UIManager, "EEDisplay"))
				 .setSize(ldropItemWidth, 4 * dropItemHeight)
				 .setBarHeight(dropItemHeight)
				 .setItemHeight(dropItemHeight)
				 .setColorValue(buttonTxtColor)
				 .setColorBackground(buttonDefColor)
				 .setColorActive(buttonActColor)
				 .moveTo(miscWindow)
				 .close();

		 dropdown.getCaptionLabel().setFont(small);
		 dropdown.addItem(EEMapping.DOT.toString(), EEMapping.DOT);
		 dropdown.addItem(EEMapping.LINE.toString(), EEMapping.LINE);
		 dropdown.addItem(EEMapping.NONE.toString(), EEMapping.NONE);
		 dropdown.setActiveLabel(EEMapping.DOT.toString());
		 
		 dropdown = (MyDropdownList)((new MyDropdownList(UIManager, "AxesDisplay"))
				 .setSize(ldropItemWidth, 4 * dropItemHeight)
				 .setBarHeight(dropItemHeight)
				 .setItemHeight(dropItemHeight)
				 .setColorValue(buttonTxtColor)
				 .setColorBackground(buttonDefColor)
				 .setColorActive(buttonActColor)
				 .moveTo(miscWindow)
				 .close());

		 dropdown.getCaptionLabel().setFont(small);
		 dropdown.addItem(AxesDisplay.AXES.toString(), AxesDisplay.AXES);
		 dropdown.addItem(AxesDisplay.GRID.toString(), AxesDisplay.GRID);
		 dropdown.addItem(AxesDisplay.NONE.toString(), AxesDisplay.NONE);
		 dropdown.setActiveLabel(AxesDisplay.AXES.toString());
		 
		 dropdown = (MyDropdownList)((new MyDropdownList(UIManager, "Scenario"))
				 .setSize(ldropItemWidth, 4 * dropItemHeight)
				 .setBarHeight(dropItemHeight)
				 .setItemHeight(dropItemHeight)
				 .setColorValue(buttonTxtColor)
				 .setColorBackground(buttonDefColor)
				 .setColorActive(buttonActColor)
				 .moveTo(scenarioWindow)
				 .close());

		 dropdown.getCaptionLabel().setFont(small);
		 dropdown = (MyDropdownList)((new MyDropdownList(UIManager, "Fixture"))
				 .setSize(ldropItemWidth, 4 * dropItemHeight)
				 .setBarHeight(dropItemHeight)
				 .setItemHeight(dropItemHeight)
				 .setColorValue(buttonTxtColor)
				 .setColorBackground(buttonDefColor)
				 .setColorActive(buttonActColor)
				 .moveTo(editObjWindow)
				 .close());

		 dropdown.getCaptionLabel().setFont(small);
		 dropdown = (MyDropdownList)((new MyDropdownList(UIManager, "Object"))
				 .setSize(ldropItemWidth, 4 * dropItemHeight)
				 .setBarHeight(dropItemHeight)
				 .setItemHeight(dropItemHeight)
				 .setColorValue(buttonTxtColor)
				 .setColorBackground(buttonDefColor)
				 .setColorActive(buttonActColor)
				 .moveTo(editObjWindow)
				 .close());

		 dropdown.getCaptionLabel().setFont(small);
		 dropdown = (MyDropdownList)((new MyDropdownList(UIManager, "Outline"))
				 .setSize(sdropItemWidth, sButtonHeight + 3 * dropItemHeight)
				 .setBarHeight(dropItemHeight)
				 .setItemHeight(dropItemHeight)
				 .setColorValue(buttonTxtColor)
				 .setColorBackground(buttonDefColor)
				 .setColorActive(buttonActColor)
				 .moveTo(createObjWindow)
				 .close());

		dropdown.getCaptionLabel().setFont(small);
		dropdown.addItem("black", app.color(0));
		dropdown.addItem("red", app.color(255, 0, 0));
		dropdown.addItem("green", app.color(0, 255, 0));
		dropdown.addItem("blue", app.color(0, 0, 255));
		dropdown.addItem("orange", app.color(255, 60, 0));
		dropdown.addItem("yellow", app.color(255, 255, 0));
		dropdown.addItem("pink", app.color(255, 0, 255));
		dropdown.addItem("purple", app.color(90, 0, 255));


		 dropdown = (MyDropdownList)((new MyDropdownList(UIManager, "Fill"))
				 .setSize(mdropItemWidth, 4 * dropItemHeight)
				 .setBarHeight(dropItemHeight)
				 .setItemHeight(dropItemHeight)
				 .setColorValue(buttonTxtColor)
				 .setColorBackground(buttonDefColor)
				 .setColorActive(buttonActColor)
				 .moveTo(createObjWindow)
				 .close());

		 dropdown.getCaptionLabel().setFont(small);
		 dropdown.addItem("white", app.color(255));
		 dropdown.addItem("black", app.color(0));
		 dropdown.addItem("red", app.color(255, 0, 0));
		 dropdown.addItem("green", app.color(0, 255, 0));
		 dropdown.addItem("blue", app.color(0, 0, 255));
		 dropdown.addItem("orange", app.color(255, 60, 0));
		 dropdown.addItem("yellow", app.color(255, 255, 0));
		 dropdown.addItem("pink", app.color(255, 0, 255));
		 dropdown.addItem("purple", app.color(90, 0, 255));
		 dropdown.addItem("sky blue", app.color(0, 255, 255));
		 dropdown.addItem("dark green", app.color(0, 100, 15));

		 dropdown = (MyDropdownList)((new MyDropdownList(UIManager, "Shape"))
				 .setSize(sdropItemWidth, 4 * dropItemHeight)
				 .setBarHeight(dropItemHeight)
				 .setItemHeight(dropItemHeight)
				 .setColorValue(buttonTxtColor)
				 .setColorBackground(buttonDefColor)
				 .setColorActive(buttonActColor)
				 .moveTo(createObjWindow)
				 .close());

		 dropdown.getCaptionLabel().setFont(small);
		 dropdown.addItem("Box", ShapeType.BOX);
		 dropdown.addItem("Cylinder", ShapeType.CYLINDER);
		 dropdown.addItem("Import", ShapeType.MODEL);

		 dropdown = (MyDropdownList)((new MyDropdownList(UIManager, "ObjType"))
				 .setSize(sdropItemWidth, 3 * dropItemHeight)
				 .setBarHeight(dropItemHeight)
				 .setItemHeight(dropItemHeight)
				 .setColorValue(buttonTxtColor)
				 .setColorBackground(buttonDefColor)
				 .setColorActive(buttonActColor)
				 .moveTo(createObjWindow)
				 .close());
		 
		 dropdown.getCaptionLabel().setFont(small);
		 dropdown.addItem("Parts", 0.0f);
		 dropdown.addItem("Fixtures", 1.0f);
	 }
	 
	 /**
	  * Sets the current position and orientation inputs fields to the values of
	  * the active part's default position and orientation.
	  */
	 public void autoFillCurFields() {
		 WorldObject active = getActiveWorldObject();
		 
		 if (active instanceof Part) {
			 Part p = (Part)active;
			 // Get the part's default position and orientation
			 PVector pos = p.getDefaultCenter();
			 PVector wpr = RobotRun.matrixToEuler( p.getDefaultOrientationAxes() )
					 			   .mult(RobotRun.RAD_TO_DEG);
			 
			 pos = RobotRun.convertNativeToWorld(pos);
			 
			 // Fill the current position and orientation fields in the edit window
			 getTextField("XCur").setText( Float.toString(pos.x) );
			 getTextField("YCur").setText( Float.toString(pos.y) );
			 getTextField("ZCur").setText( Float.toString(pos.z) );
			 getTextField("WCur").setText( Float.toString(-wpr.x) );
			 getTextField("PCur").setText( Float.toString(-wpr.z) );
			 getTextField("RCur").setText( Float.toString(wpr.y) );
		 }
	 }
	 
	 /**
	  * Sets the default position and orientation inputs fields to the values of
	  * the active part's current position and orientation.
	  */
	 public void autoFillDefFields() {
		 WorldObject active = getActiveWorldObject();
		 
		 if (active instanceof Part) {
			 // Get the part's current position and orientation
			 PVector pos = active.getLocalCenter();
			 PVector wpr = RobotRun.matrixToEuler( active.getLocalOrientationAxes() )
					 			   .mult(RobotRun.RAD_TO_DEG);
			 
			 pos = RobotRun.convertNativeToWorld(pos);
			 
			 // Fill the default position and orientation fields in the edit window
			 getTextField("XDef").setText( Float.toString(pos.x) );
			 getTextField("YDef").setText( Float.toString(pos.y) );
			 getTextField("ZDef").setText( Float.toString(pos.z) );
			 getTextField("WDef").setText( Float.toString(-wpr.x) );
			 getTextField("PDef").setText( Float.toString(-wpr.z) );
			 getTextField("RDef").setText( Float.toString(wpr.y) );
		 }
	 }

	 /**
	  * Reinitialize any and all input fields
	  */
	 private void clearAllInputFields() {
		 clearGroupInputFields(null);
		 updateDimLblsAndFields();
	 }
	 
	 public void clearInputsFields() {
		 String winName = windowTabs.getActiveButtonName();
		 
		 if (winName.equals("Create")) {
			 clearGroupInputFields(createObjWindow);
			 clearSharedInputFields();
			 updateCreateWindowContentPositions();
			 
		 } else if (winName.equals("Edit")) {
			 clearGroupInputFields(editObjWindow);
			 clearSharedInputFields();
			 updateEditWindowContentPositions();
		 }
	 }

	 /**
	  * Reinitializes any controller interface in the given group that accepts user
	  * input; currently only text fields and dropdown lists are updated.
	  */
	 public void clearGroupInputFields(Group g) {
		 List<ControllerInterface<?>> contents = UIManager.getAll();

		 for (ControllerInterface<?> controller : contents) {

			 if (g == null || controller.getParent().equals(g)) {

				 if (controller instanceof Textfield) {
					 // Clear anything inputted into the text field
					 controller = ((Textfield)controller).setValue("");
					 
				 } else if (controller instanceof MyDropdownList) {
					 // Reset the caption label of each dropdown list and close the list
					 MyDropdownList dropdown = (MyDropdownList)controller;
					 
					 if(!dropdown.getParent().equals(miscWindow)) {
						 dropdown.resetLabel();
						 dropdown.close();
					 }
				 }
			 }
		 }
	 }

	 /**
	  * Reinitialize the input fields for any contents in the Scenario window
	  */
	 public void clearScenarioInputFields() {
		 clearGroupInputFields(scenarioWindow);
		 updateDimLblsAndFields();
	 }

	 /**
	  * Reinitialize the input fields for any shared contents
	  */
	 public void clearSharedInputFields() {
		 clearGroupInputFields(sharedElements);
		 updateDimLblsAndFields();
	 } 
	 
	 /**
	  * Returns the button, with the given name, if it exists in one of the UI
	  * (excluding the pendant). If a non-button UI element exists in the UI,
	  * then a ClassCastException will be thrown. If no UI element with the
	  * given name exists, then null is returned.
	  * 
	  * @param name	Then name of a button in the UI
	  * @return		The button with the given name or null
	  * @throws ClassCastException	If a non-button UI element with the given
	  * 							name exists in the UI
	  */
	 private Button getButton(String name) throws ClassCastException {
		 return (Button) UIManager.get(name);
	 }
	 
	 /**
	  * Returns a text-field, which corresponds to the given dimension type.
	  * The mapping from DimType to text-fields is:
	  * 	LENGTH, RADIUS, null	->	Dim0 text-field
	  * 	HEIGHT, SCALE			->	Dim1 text-field
	  * 	WIDTH					->	Dim2 text-field
	  * 
	  * @param name	A type of world object dimension, which corresponds to a
	  * 			text-field input in the UI
	  * @return		The text-field corresponding to the given dimension type
	  * @throws ClassCastException	Really shouldn't happen
	  */
	 private Textfield getDimTF(DimType t) throws ClassCastException {
		 
		 if (t == DimType.WIDTH) {
			 return (Textfield) UIManager.get("Dim2");
			 
		 } else if (t == DimType.HEIGHT || t == DimType.SCALE) {
			 return (Textfield) UIManager.get("Dim1");
			 
		 } else {
			 return (Textfield) UIManager.get("Dim0");
		 }
	 }
	 
	 /**
	  * Returns the drop-down, with the given name, if it exists in one of the
	  * UI (excluding the pendant). If a non-drop-down UI element exists in the
	  * UI, then a ClassCastException will be thrown. If no UI element with the
	  * given name exists, then null is returned.
	  * 
	  * @param name	Then name of a drop-down in the UI
	  * @return		The drop-down with the given name or null
	  * @throws ClassCastException	If a non-drop-down UI element with the given
	  * 							name exists in the UI
	  */
	 private MyDropdownList getDropdown(String name) throws ClassCastException {
		 return (MyDropdownList) UIManager.get(name);
	 }
	 
	 /**
	  * Returns the text-area, with the given name, if it exists in one of the
	  * UI (excluding the pendant). If a non-text-area UI element exists in the
	  * UI, then a ClassCastException will be thrown. If no UI element with the
	  * given name exists, then null is returned.
	  * 
	  * @param name	Then name of a text-area in the UI
	  * @return		The text-area with the given name or null
	  * @throws ClassCastException	If a non-text-area UI element with the given
	  * 							name exists in the UI
	  */
	 private Textarea getTextArea(String name) throws ClassCastException {
		 return (Textarea) UIManager.get(name);
	 }
	 
	 /**
	  * Returns the text-field, with the given name, if it exists in one of the
	  * UI (excluding the pendant). If a non-text-field UI element exists in the
	  * UI, then a ClassCastException will be thrown. If no UI element with the
	  * given name exists, then null is returned.
	  * 
	  * @param name	Then name of a text-field in the UI
	  * @return		The text-field with the given name or null
	  * @throws ClassCastException	If a non-text-field UI element with the given
	  * 							name exists in the UI
	  */
	 private Textfield getTextField(String name) throws ClassCastException {
		 return (Textfield) UIManager.get(name);
	 }

	 /**
	  * Creates a world object form the input fields in the Create window.
	  */
	 public WorldObject createWorldObject() {
		 // Check the object type dropdown list
		 Object val = getDropdown("ObjType").getActiveLabelValue();
		 // Determine if the object to be create is a Fixture or a Part
		 Float objectType = 0.0f;

		 if (val instanceof Float) {
			 objectType = (Float)val;
		 }

		 app.pushMatrix();
		 app.resetMatrix();
		 WorldObject wldObj = null;

		 try {

			 if (objectType == 0.0f) {
				 // Create a Part
				 String name = getTextField("ObjName").getText();

				 ShapeType type = (ShapeType)getDropdown("Shape").getActiveLabelValue();

				 int fill = (Integer)getDropdown("Fill").getActiveLabelValue();

				 switch(type) {
				 case BOX:
					 int strokeVal = (Integer)getDropdown("Outline").getActiveLabelValue();
					 Float[] shapeDims = getBoxDimensions();
					 // Construct a box shape
					 if (shapeDims != null && shapeDims[0] != null && shapeDims[1] != null && shapeDims[2] != null) {
						 wldObj = new Part(name, fill, strokeVal, shapeDims[0], shapeDims[1], shapeDims[2]);
					 }
					 break;

				 case CYLINDER:
					 strokeVal = (Integer)getDropdown("Outline").getActiveLabelValue();
					 shapeDims = getCylinderDimensions();
					 // Construct a cylinder
					 if (shapeDims != null && shapeDims[0] != null && shapeDims[1] != null) {
						 wldObj = new Part(name, fill, strokeVal, shapeDims[0], shapeDims[1]);
					 }
					 break;

				 case MODEL:
					 String srcFile = getDimTF(null).getText();
					 System.out.println(srcFile);
					 shapeDims = getModelDimensions();
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

			 } else if (objectType == 1.0f) {
				 // Create a fixture
				 String name = getTextField("ObjName").getText();
				 ShapeType type = (ShapeType)getDropdown("Shape").getActiveLabelValue();

				 int fill = (Integer)getDropdown("Fill").getActiveLabelValue();

				 switch(type) {
				 case BOX:
					 int strokeVal = (Integer)getDropdown("Outline").getActiveLabelValue();
					 Float[] shapeDims = getBoxDimensions();
					 // Construct a box shape
					 if (shapeDims != null && shapeDims[0] != null && shapeDims[1] != null && shapeDims[2] != null) {
						 wldObj = new Fixture(name, fill, strokeVal, shapeDims[0], shapeDims[1], shapeDims[2]);
					 }
					 break;

				 case CYLINDER:
					 strokeVal = (Integer)getDropdown("Outline").getActiveLabelValue();
					 shapeDims = getCylinderDimensions();
					 // Construct a cylinder
					 if (shapeDims != null && shapeDims[0] != null && shapeDims[1] != null) {
						 wldObj = new Fixture(name, fill, strokeVal, shapeDims[0], shapeDims[1]);
					 }
					 break;

				 case MODEL:
					 String srcFile = getDimTF(null).getText();
					 System.out.println(srcFile);
					 shapeDims = getModelDimensions();
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
			 RobotRun.println("Missing parameter!");
			 NPEx.printStackTrace();
			 
		 } catch (ClassCastException CCEx) {
			 RobotRun.println("Invalid field?");
			 CCEx.printStackTrace();
			 
		 } catch (IndexOutOfBoundsException IOOBEx) {
			 RobotRun.println("Missing field?");
			 IOOBEx.printStackTrace();
		 }

		 app.popMatrix();

		 return wldObj;
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

		 if (app.activeScenario != null) {
			 ret = app.activeScenario.removeWorldObject( getActiveWorldObject() );
		 }

		 return ret;
	 }

	 /**
	  * Eit the position and orientation (as well as the fixture reference for Parts)
	  * of the currently selected World Object in the Object dropdown list.
	  */
	 public void editWorldObject() {
		 WorldObject toEdit = getActiveWorldObject();
		 RoboticArm model = RobotRun.getActiveRobot();
		 
		 if (toEdit != null) {
			 
			 if (model != null && toEdit == model.held) {
				 // Cannot edit an object being held by the Robot
				 RobotRun.println("Cannot edit an object currently being held by the Robot!");
				 return;
			 }

			 try {
				 boolean dimChanged = false, edited = false;
				 WorldObject objSaveState = (WorldObject)toEdit.clone();
				 Shape s = toEdit.getForm();

				 if (s instanceof Box) {
					 Float[] newDims = getBoxDimensions();

					 if (newDims[0] != null) {
						 // Update the box's length
						 s.setDim(newDims[0], DimType.LENGTH);
						 dimChanged = true;
					 }

					 if (newDims[1] != null) {
						 // Update the box's height
						 s.setDim(newDims[1], DimType.HEIGHT);
						 dimChanged = true;
					 }

					 if (newDims[2] != null) {
						 // Update the box's width
						 s.setDim(newDims[2], DimType.WIDTH);
						 dimChanged = true;
					 }

				 } else if (s instanceof Cylinder) {
					 Float[] newDims = getCylinderDimensions();

					 if (newDims[0] != null) {
						 // Update the cylinder's radius
						 s.setDim(newDims[0], DimType.RADIUS);
						 dimChanged = true;
					 }

					 if (newDims[1] != null) {
						 // Update the cylinder's height
						 s.setDim(newDims[1], DimType.HEIGHT);
						 dimChanged = true;
					 }

				 } else if (s instanceof ModelShape) {
					 Float[] newDims = getModelDimensions();

					 if (newDims[0] != null) {
						 // Update the model's scale value
						 s.setDim(newDims[0], DimType.SCALE);
						 dimChanged = true;
					 }
				 }

				 if (dimChanged && toEdit instanceof Part) {
					 // Update the bounding box dimensions of a part
					 ((Part)toEdit).updateOBBDims();
				 }
				 
				 edited = dimChanged;

				 // Convert origin position into the World Frame
				 PVector oPosition = RobotRun.convertNativeToWorld( toEdit.getLocalCenter() ),
						 oWPR = RobotRun.matrixToEuler(toEdit.getLocalOrientationAxes()).mult(RobotRun.RAD_TO_DEG);
				 Float[] inputValues = getOrientationValues();
				 // Update position and orientation
				 if (inputValues[0] != null) {
					 oPosition.x = inputValues[0];
					 edited = true;
				 }
				 
				 if (inputValues[1] != null) {
					 oPosition.y = inputValues[1];
					 edited = true;
				 }
				 
				 if (inputValues[2] != null) {
					 oPosition.z = inputValues[2];
					 edited = true;
				 }
				 
				 if (inputValues[3] != null) {
					 oWPR.x = -inputValues[3];
					 edited = true;
				 }
				 
				 if (inputValues[5] != null) {
					 oWPR.y = -inputValues[5];
					 edited = true;
				 }
				 
				 if (inputValues[4] != null) {
					 oWPR.z = inputValues[4];
					 edited = true;
				 }

				 // Convert values from the World to the Native coordinate system
				 PVector position = RobotRun.convertWorldToNative( oPosition );
				 PVector wpr = oWPR.mult(RobotRun.DEG_TO_RAD);
				 float[][] orientation = RobotRun.eulerToMatrix(wpr);
				 // Update the Objects position and orientaion
				 toEdit.setLocalCenter(position);
				 toEdit.setLocalOrientationAxes(orientation);

				 if (toEdit instanceof Part) {
					Part p = (Part)toEdit;
					PVector defaultPos = RobotRun.convertNativeToWorld( p.getDefaultCenter() );
					PVector defaultWPR = RobotRun.matrixToEuler( p.getDefaultOrientationAxes() ).mult(RobotRun.RAD_TO_DEG);
					
					// Update default position and orientation
					 if (inputValues[6] != null) {
						 defaultPos.x = inputValues[6];
						 edited = true;
					 }
					 
					 if (inputValues[7] != null) {
						 defaultPos.y = inputValues[7];
						 edited = true;
					 }
					 
					 if (inputValues[8] != null) {
						 defaultPos.z = inputValues[8];
						 edited = true;
					 }
					 
					 if (inputValues[9] != null) {
						 defaultWPR.x = -inputValues[9];
						 edited = true;
					 }
					 
					 if (inputValues[11] != null) {
						 defaultWPR.y = -inputValues[11];
						 edited = true;
					 }
					 
					 if (inputValues[10] != null) {
						 defaultWPR.z = inputValues[10];
						 edited = true;
					 }

					 // Convert values from the World to the Native coordinate system
					 position = RobotRun.convertWorldToNative( defaultPos );
					 wpr = defaultWPR.mult(RobotRun.DEG_TO_RAD);
					 orientation = RobotRun.eulerToMatrix(wpr);
					 // Update the Object's default position and orientation
					 p.setDefaultCenter(position);
					 p.setDefaultOrientationAxes(orientation);
					 
					 // Set the reference of the Part to the currently active fixture
					 Fixture refFixture = (Fixture)getDropdown("Fixture").getActiveLabelValue();
					 
					 if (p.getFixtureRef() != refFixture) {
						 p.setFixtureRef(refFixture);
						 edited = true;
					 }
				 }
				 
				 if (edited) {
					 /* Save the previous version of the world object on the
					  * undo stack */
					 app.updateScenarioUndo(objSaveState);
				 }
				 
			 } catch (NullPointerException NPEx) {
				 RobotRun.println("Missing parameter!");
				 NPEx.printStackTrace();
			 }
		 } else {
			 RobotRun.println("No object selected!");
		 }

		 /* If the edited object is a fixture, then update the orientation
		  * of all parts, which reference this fixture, in this scenario. */
		 if (toEdit instanceof Fixture) {
			 if (app.activeScenario != null) {

				 for (WorldObject wldObj : app.activeScenario) {
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
	  * Returns the scenario associated with the label that is active
	  * for the scenario drop-down list.
	  * 
	  * @returning  The index value or null if no such index exists
	  */
	 public Scenario getActiveScenario() {
		 String activeButtonLabel = windowTabs.getActiveButtonName();

		 if (activeButtonLabel != null && activeButtonLabel.equals("Scenario")) {
			 Object val = getDropdown("Scenario").getActiveLabelValue();

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
	 public WorldObject getActiveWorldObject() {
		 Object wldObj = getDropdown("Object").getActiveLabelValue();

		 if (editObjWindow.isVisible() && wldObj instanceof WorldObject) {
			 return (WorldObject)wldObj;
		 } else {
			 return null;
		 }
	 }
	 
	 /**
	  * Returns the axes display associated with the axes display dropdown
	  * list.
	  * 
	  * @return	The active axes display state
	  */
	 public AxesDisplay getAxesDisplay() {
		 return (AxesDisplay)getDropdown("AxesDisplay").getActiveLabelValue();
	 }

	 /**
	  * TODO
	  */
	 private Float[] getBoxDimensions() {
		 try {
			 // null values represent an uninitialized field
			 final Float[] dimensions = new Float[] { null, null, null };

			 // Pull from the dim fields
			 String lenField = getDimTF(DimType.LENGTH).getText(),
					 hgtField = getDimTF(DimType.HEIGHT).getText(),
					 wdhField = getDimTF(DimType.WIDTH).getText();

			 if (lenField != null && !lenField.equals("")) {
				 // Read length input
				 float val = Float.parseFloat(lenField);

				 if (val <= 0) {
					 throw new NumberFormatException("Invalid length value!");
				 }
				 // Length cap of 800
				 dimensions[0] = RobotRun.max(10, RobotRun.min(val, 800f));
			 }

			 if (hgtField != null && !hgtField.equals("")) {
				 // Read height input
				 float val = Float.parseFloat(hgtField);

				 if (val <= 0) {
					 throw new NumberFormatException("Invalid height value!");
				 }
				 // Height cap of 800
				 dimensions[1] = RobotRun.max(10, RobotRun.min(val, 800f));
			 }

			 if (wdhField != null && !wdhField.equals("")) {
				 // Read Width input
				 float val = Float.parseFloat(wdhField);

				 if (val <= 0) {
					 throw new NumberFormatException("Invalid width value!");
				 }
				 // Width cap of 800
				 dimensions[2] = RobotRun.max(10, RobotRun.min(val, 800f));
			 }

			 return dimensions;

		 } catch (NumberFormatException NFEx) {
			 RobotRun.println("Invalid number input!");
			 return null;

		 } catch (NullPointerException NPEx) {
			 RobotRun.println("Missing parameter!");
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
			 String radField = getDimTF(DimType.RADIUS).getText(),
					 hgtField = getDimTF(DimType.HEIGHT).getText();

			 if (radField != null && !radField.equals("")) {
				 // Read radius input
				 float val = Float.parseFloat(radField);

				 if (val <= 0) {
					 throw new NumberFormatException("Invalid length value!");
				 }
				 // Radius cap of 9999
				 dimensions[0] = RobotRun.max(5, RobotRun.min(val, 800f));
			 }

			 if (hgtField != null && !hgtField.equals("")) {
				 // Read height input
				 float val = Float.parseFloat(hgtField);

				 if (val <= 0) {
					 throw new NumberFormatException("Invalid height value!");
				 }
				 // Height cap of 9999
				 dimensions[1] = RobotRun.max(10, RobotRun.min(val, 800f));
			 }

			 return dimensions;

		 } catch (NumberFormatException NFEx) {
			 RobotRun.println("Invalid number input!");
			 return null;

		 } catch (NullPointerException NPEx) {
			 RobotRun.println("Missing parameter!");
			 return null;
		 }
	 }
	 
	 /**
	  * Returns the end effector mapping associated with the ee mapping
	  * dropdown list.
	  * 
	  * @return	The active EEE Mapping state
	  */
	 public EEMapping getEEMapping() {
		 return (EEMapping)getDropdown("EEDisplay").getActiveLabelValue();
	 }

	 /**
	  * TODO
	  */
	 private Float[] getModelDimensions() {
		 try {
			 // null values represent an uninitialized field
			 final Float[] dimensions = new Float[] { null };

			 String activeWindow = windowTabs.getActiveButtonName(), sclField;
			 // Pull from the Dim fields
			 if (activeWindow != null && activeWindow.equals("Create")) {
				 sclField = getDimTF(DimType.SCALE).getText();

			 } else {
				 sclField = getDimTF(null).getText();
			 }

			 if (sclField != null && !sclField.equals("")) {
				 // Read scale input
				 float val = Float.parseFloat(sclField);

				 if (val <= 0) {
					 throw new NumberFormatException("Invalid scale value");
				 }
				 // Scale cap of 50
				 dimensions[0] = RobotRun.min(val, 50f);
			 }

			 return dimensions;

		 } catch (NumberFormatException NFEx) {
			 RobotRun.println(NFEx.getMessage());
			 return null;

		 } catch (NullPointerException NPEx) {
			 RobotRun.println("Missing parameter!");
			 return null;
		 }
	 }
	 
	 /**
	  * @return	Whether or not the OBB Display button is off
	  */
	 public boolean getOBBButtonState() {
		 return !getButton("ToggleOBBs").isOn();
	 }

	 /**
	  * TODO
	  */
	 private Float[] getOrientationValues() {
		 try {
			 // Pull from x, y, z, w, p, r, fields input fields
			 String[] orienVals = new String[] {
					getTextField("XCur").getText(), getTextField("YCur").getText(),
					getTextField("ZCur").getText(), getTextField("WCur").getText(),
					getTextField("PCur").getText(), getTextField("RCur").getText(),
					getTextField("XDef").getText(), getTextField("YDef").getText(),
					getTextField("ZDef").getText(), getTextField("WDef").getText(),
					getTextField("PDef").getText(), getTextField("RDef").getText()
			 };
			 
			 // NaN indicates an uninitialized field
			 Float[] values = new Float[] { null, null, null, null, null, null,
					 						null, null, null, null, null, null };
			 
			 for (int valIdx = 0; valIdx < orienVals.length; ++valIdx) {
				// Update the orientation value
				 if (orienVals[valIdx] != null && !orienVals[valIdx].equals("")) {
					 float val = Float.parseFloat(orienVals[valIdx]);
					 // Bring value within the range [-9999, 9999]
					 val = RobotRun.max(-9999f, RobotRun.min(val, 9999f));
					 values[valIdx] = val;
				 }
			 }

			 return values;

		 } catch (NumberFormatException NFEx) {
			 RobotRun.println("Invalid number input!");
			 return null;

		 } catch (NullPointerException NPEx) {
			 RobotRun.println("Missing parameter!");
			 return null;
		 }
	 }
	 
	 /**
	  * @return	Whether or not the robot display button is on
	  */
	 public boolean getRobotButtonState() {
		 return getButton("ToggleRobot").isOn();
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
			 String name = getTextField("ScenarioName").getText();

			 if (name != null) {
				 // Names only consist of letters and numbers
				 if (Pattern.matches("[a-zA-Z0-9]+", name)) {

					 for (Scenario s : app.SCENARIOS) {
						 if (s.getName().equals(name)) {
							 // Duplicate name
							 RobotRun.println("Names must be unique!");
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
	  * Determines whether a single text field is active.
	  */
	 public boolean isATextFieldActive() {
		 List<ControllerInterface<?>> controllers = UIManager.getAll();
		 
		 for (ControllerInterface<?> c : controllers) {
			 if (c instanceof Textfield && ((Textfield) c).isFocus()) {
				 return true;
			 }
		 }

		 return false;
	 }

	 /**
	  * Determines whether the mouse is over a dropdown list.
	  */
	 public boolean isMouseOverADropdownList() {
		 List<ControllerInterface<?>> controllers = UIManager.getAll();
		 
		 for (ControllerInterface<?> c : controllers) {
			 if (c instanceof MyDropdownList && c.isMouseOver()) {
				 return true;
			 }
		 }

		 return false;
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
	  * Reset the base label of every dropdown list.
	  */
	 private void resetListLabels() {
		 List<ControllerInterface<?>> controllers = UIManager.getAll();
		 
		 for (ControllerInterface<?> c : controllers) {
			 if (c instanceof MyDropdownList && !c.getParent().equals(miscWindow)) {
				 ((MyDropdownList)c).resetLabel();
				 
			 } else if (c.getName().length() > 4 && c.getName().substring(0, 4).equals("Dim") ||
					 c.getName().equals("RefLbl")) {
				 
				 c.hide();
				 
			 }
		 }
		 
		 updateDimLblsAndFields();
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
	 * Updates the tabs that are available in the applications main window.
	 * 
	 * @return	Whether the second Robot is hidden
	 */
	public boolean toggleSecondRobot() {
		
		if (windowTabs.getActiveButtonName().equals("Robot2")) {
			windowTabs.setLabel("Hide");
		}
		
		// Remove or add the second Robot based on the HideRobot button
		Button tr = getButton("ToggleRobot");
		
		if (tr.isOn()) {
			windowTabs.setItems(new String[] { "Hide", "Robot1", "Robot2", "Create", "Edit", "Scenario", "Misc" });
			tr.setLabel("Remove Robot");
						
		} else {
			windowTabs.setItems(new String[] { "Hide", "Robot1", "Create", "Edit", "Scenario", "Misc" });
			tr.setLabel("Add Robot");
		}
		
		return tr.isOn();
	}

	 /**
	  * Updates the positions of all the contents of the world object creation window.
	  */
	 private void updateCreateWindowContentPositions() {
		 updateDimLblsAndFields();

		 // Object Type dropdown list and label
		 int[] relPos = new int[] { offsetX, offsetX };
		 ControllerInterface<?> c = getTextArea("ObjTypeLbl").setPosition(relPos[0], relPos[1]);

		 relPos = relativePosition(c, RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
		 getDropdown("ObjType").setPosition(relPos[0], relPos[1]);
		 // Name label and field
		 relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		 c = getTextArea("ObjNameLbl").setPosition(relPos[0], relPos[1]);

		 relPos = relativePosition(c, RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
		 getTextField("ObjName").setPosition(relPos[0], relPos[1]);
		 // Shape type label and dropdown
		 relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		 c = getTextArea("ShapeLbl").setPosition(relPos[0], relPos[1]);

		 relPos = relativePosition(c, RelativePoint.TOP_RIGHT, distLblToFieldX, RobotRun.abs(fieldHeight - dropItemHeight) / 2);
		 getDropdown("Shape").setPosition(relPos[0], relPos[1]);
		 // Dimension label and fields
		 relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		 relPos = updateDimLblAndFieldPositions(relPos[0], relPos[1]);

		 // Fill color label and dropdown
		 c = getTextArea("FillLbl").setPosition(relPos[0], relPos[1]);

		 relPos = relativePosition(c, RelativePoint.TOP_RIGHT, distLblToFieldX, RobotRun.abs(fieldHeight - dropItemHeight) / 2);
		 getDropdown("Fill").setPosition(relPos[0], relPos[1]);

		 relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		 Object val = getDropdown("Shape").getActiveLabelValue();

		 if (val == ShapeType.MODEL) {
			 // No stroke color for Model Shapes
			 c = getTextArea("OutlineLbl").hide();
			 getDropdown("Outline").hide();

		 } else {
			 // Outline color label and dropdown
			 c = getTextArea("OutlineLbl").setPosition(relPos[0], relPos[1]).show();
			 relPos = relativePosition(c, RelativePoint.TOP_RIGHT, distLblToFieldX, RobotRun.abs(fieldHeight - dropItemHeight) / 2);

			 getDropdown("Outline").setPosition(relPos[0], relPos[1]).show();
			 relPos = relativePosition(c, RelativePoint.BOTTOM_RIGHT, distLblToFieldX, distBtwFieldsY);
		 } 

		 // Create button
		 c = getButton("CreateWldObj").setPosition(relPos[0], relPos[1]);
		 // Clear button
		 relPos = relativePosition(c, RelativePoint.TOP_RIGHT, offsetX, 0);
		 c = getButton("ClearFields").setPosition(relPos[0], relPos[1]);
		 // Update window background display
		 relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
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

		 // Update position and label text of the dimension fields based on the selected shape from the Shape dropDown List
		 for (int idxDim = 0; idxDim < 3; ++idxDim) {
			 Textfield dimField = getTextField( String.format("Dim%d", idxDim) );

			 if (!dimField.isVisible()) { break; }

			 Textarea dimLbl = getTextArea( String.format("Dim%dLbl", idxDim) ).setPosition(relPos[0], relPos[1]);
			 
			 relPos = relativePosition(dimLbl, RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
			 dimField.setPosition(relPos[0], relPos[1]);
			 
			 relPos = relativePosition(dimLbl, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		 }

		 return relPos;
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
				 ShapeType selectedShape = (ShapeType)getDropdown("Shape").getActiveLabelValue();

				 // Define the label text and the number of dimensionos fields to display
				 if (selectedShape == ShapeType.BOX) {
					 lblNames = new String[] { "Length:", "Height:", "Width" };

				 } else if (selectedShape == ShapeType.CYLINDER) {
					 lblNames = new String[] { "Radius", "Height" };

				 } else if (selectedShape == ShapeType.MODEL) {
					 lblNames = new String[] { "Source:", "Scale:", };
				 }

			 } else if (activeButtonLabel.equals("Edit")) {
				 Object val = getDropdown("Object").getActiveLabelValue();

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

		 for (int idxDim = 0; idxDim < 3; ++idxDim) {
			 
			 if (idxDim < lblNames.length) {
				 // Show a number of dimension fields and labels equal to the value of dimSize
				 getTextArea( String.format("Dim%dLbl", idxDim) ).setText( lblNames[idxDim] ).show();
				 getTextField( String.format("Dim%d", idxDim) ).show();

			 } else {
				 // Hide remaining dimension fields and labels
				 getTextArea( String.format("Dim%dLbl", idxDim) ).hide();
				 getTextField( String.format("Dim%d", idxDim) ).hide();
			 }
		 }
	 }

	 /**
	  * Updates the positions of all the contents of the world object editing window.
	  */
	 private void updateEditWindowContentPositions() {
		 updateDimLblsAndFields();

		 // Object list dropdown and label
		 int[] relPos = new int[] { offsetX, offsetX };
		 ControllerInterface<?> c = getTextArea("ObjLabel").setPosition(relPos[0], relPos[1]),
				 				c0 = null;
		 boolean isPart = getActiveWorldObject() instanceof Part;
		 
		 relPos = relativePosition(c, RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
		 getDropdown("Object").setPosition(relPos[0], relPos[1]);
		 // Dimension label and fields
		 relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		 relPos = updateDimLblAndFieldPositions(relPos[0], relPos[1]);
		// Orientation column labels
		 c = getTextArea("Blank").setPosition(relPos[0], relPos[1]);
		 
		 relPos = relativePosition(c, RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
		 c0 = getTextArea("Current").setPosition(relPos[0], relPos[1]);
		 
		 if (isPart) {
			 relPos = relativePosition(c0, RelativePoint.TOP_RIGHT, distFieldToFieldX, 0);
			 getTextArea("Default").setPosition(relPos[0], relPos[1]).show();
			 
			 // Auto fill buttons row
			 relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
			 c = getTextArea("AFLbl").setPosition(relPos[0], relPos[1]).show();
			 
			 relPos = relativePosition(c, RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
			 c0 = getButton("DefIntoCur").setPosition(relPos[0], relPos[1]).show();
			 
			 relPos = relativePosition(c0, RelativePoint.TOP_RIGHT, distFieldToFieldX +
					 fieldWidth - mButtonWidth, 0);
			 c0 = getButton("CurIntoDef").setPosition(relPos[0], relPos[1]).show();
			 
		 } else {
			 // Only show them for parts
			 getTextArea("Default").hide();
			 getTextArea("AFLbl").hide();
			 getButton("DefIntoCur").hide();
			 getButton("CurIntoDef").hide();
		 }
		 
		 // X label and fields
		 relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		 c = getTextArea("XLbl").setPosition(relPos[0], relPos[1]);

		 relPos = relativePosition(c, RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
		 c0 = getTextField("XCur").setPosition(relPos[0], relPos[1]);
		 
		 if (isPart) {
			 relPos = relativePosition(c0, RelativePoint.TOP_RIGHT, distFieldToFieldX, 0);
			 getTextField("XDef").setPosition(relPos[0], relPos[1]).show();
			 
		 } else {
			 getTextField("XDef").hide();
		 }
		 
		 // Y label and fields
		 relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		 c = getTextArea("YLbl").setPosition(relPos[0], relPos[1]);

		 relPos = relativePosition(c, RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
		 c0 = getTextField("YCur").setPosition(relPos[0], relPos[1]);
		 
		 if (isPart) {
			 relPos = relativePosition(c0, RelativePoint.TOP_RIGHT, distFieldToFieldX, 0);
			 getTextField("YDef").setPosition(relPos[0], relPos[1]).show();
			 
		 } else {
			 getTextField("YDef").hide();
		 }
		 
		 // Z label and fields
		 relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		 c = getTextArea("ZLbl").setPosition(relPos[0], relPos[1]);;

		 relPos = relativePosition(c, RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
		 c0 = getTextField("ZCur").setPosition(relPos[0], relPos[1]);
		 
		 if (isPart) {
			 relPos = relativePosition(c0, RelativePoint.TOP_RIGHT, distFieldToFieldX, 0);
			 getTextField("ZDef").setPosition(relPos[0], relPos[1]).show();
			 
		 } else {
			 getTextField("ZDef").hide();
		 }
		 
		 // W label and fields
		 relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		 c = getTextArea("WLbl").setPosition(relPos[0], relPos[1]);

		 relPos = relativePosition(c, RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
		 c0 = getTextField("WCur").setPosition(relPos[0], relPos[1]);
		 
		 if (isPart) {
			 relPos = relativePosition(c0, RelativePoint.TOP_RIGHT, distFieldToFieldX, 0);
			 getTextField("WDef").setPosition(relPos[0], relPos[1]).show();
			 
		 } else {
			 getTextField("WDef").hide();
		 }
		 
		 // P label and fields
		 relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		 c = getTextArea("PLbl").setPosition(relPos[0], relPos[1]);

		 relPos = relativePosition(c, RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
		 c0 = getTextField("PCur").setPosition(relPos[0], relPos[1]);
		 
		 if (isPart) {
			 relPos = relativePosition(c0, RelativePoint.TOP_RIGHT, distFieldToFieldX, 0);
			 getTextField("PDef").setPosition(relPos[0], relPos[1]).show();
			 
		 } else {
			 getTextField("PDef").hide();
		 }
		 
		 // R label and fields
		 relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		 c = getTextArea("RLbl").setPosition(relPos[0], relPos[1]);

		 relPos = relativePosition(c, RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
		 c0 = getTextField("RCur").setPosition(relPos[0], relPos[1]);
		 
		 if (isPart) {
			/* Default orientation and fixture references are only relevant for
			 * fixtures */
			relPos = relativePosition(c0, RelativePoint.TOP_RIGHT, distFieldToFieldX, 0);
			getTextField("RDef").setPosition(relPos[0], relPos[1]).show();
			
			relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
			c = getTextArea("RefLbl").setPosition(relPos[0], relPos[1]).show();
			
			relPos = relativePosition(c, RelativePoint.TOP_RIGHT, distLblToFieldX,
					RobotRun.abs(fieldHeight - dropItemHeight) / 2);
			getDropdown("Fixture").setPosition(relPos[0], relPos[1]).show();
			
		 } else {
			getTextField("RDef").hide();
			getTextArea("RefLbl").hide();
			getDropdown("Fixture").hide();
		 }
		 
		 // Confirm button
		 relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		 c = getButton("UpdateWldObj").setPosition(relPos[0], relPos[1]);
		 // Clear button
		 relPos = relativePosition(c, RelativePoint.TOP_RIGHT, distFieldToFieldX, 0);
		 c = getButton("ClearFields").setPosition(relPos[0], relPos[1]);
		 // Delete button
		 relPos = relativePosition(c, RelativePoint.TOP_RIGHT, distFieldToFieldX, 0);
		 c = getButton("DeleteWldObj").setPosition(relPos[0], relPos[1]);
		 // Update window background display
		 relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		 background.setBackgroundHeight(relPos[1])
		 .setHeight(relPos[1])
		 .show();
	 }

	 /**
	  * Update the contents of the two dropdown menus that
	  * contain world objects.
	  */
	 public void updateListContents() {
		 MyDropdownList dropdown;
		 
		 if (app.activeScenario != null) {
			 dropdown = getDropdown("Object");
			 dropdown.clear();
			 MyDropdownList limbo = getDropdown("Fixture");
			 limbo.clear();
			 limbo.addItem("None", null);
			 
			 for (WorldObject wldObj : app.activeScenario) {
				 dropdown.addItem(wldObj.toString(), wldObj);

				 if (wldObj instanceof Fixture) {
					 // Load all fixtures from the active scenario
					 limbo.addItem(wldObj.toString(), wldObj);
				 }
			 }
			 // Update each dropdownlist's active label
			 limbo.updateActiveLabel();
			 dropdown.updateActiveLabel();
		 }

		 dropdown = getDropdown("Scenario");
		 dropdown.clear();
		 for (int idx = 0; idx < app.SCENARIOS.size(); ++idx) {
			 // Load all scenario indices
			 Scenario s = app.SCENARIOS.get(idx);
			 dropdown.addItem(s.getName(), s);
		 }
		 dropdown.updateActiveLabel();
	 }

	 /**
	  * Updates the positions of all the contents of the scenario window.
	  */
	 public void updateScenarioWindowContentPositions() {
		 // New scenario name label
		 int[] relPos = new int[] { offsetX, offsetX };
		 ControllerInterface<?> c = getTextArea("NewScenarioLbl").setPosition(relPos[0], relPos[1]);
		 // New scenario name field
		 relPos = relativePosition(c, RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
		 getTextField("ScenarioName").setPosition(relPos[0], relPos[1]);
		 // New scenario button
		 relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		 c = getButton("NewScenario").setPosition(relPos[0], relPos[1]);
		 // Scenario dropdown list and label
		 relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, 2 * distBtwFieldsY);
		 c = getTextArea("ActiveScenarioLbl").setPosition(relPos[0], relPos[1]);

		 relPos = relativePosition(c, RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
		 getDropdown("Scenario").setPosition(relPos[0], relPos[1]);
		 // Set scenario button
		 relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		 c = getButton("SetScenario").setPosition(relPos[0], relPos[1]);
		 
		 // Update window background display
		 relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		 background.setBackgroundHeight(relPos[1])
		 .setHeight(relPos[1])
		 .show();
	 }
	 
	/**
	 * Updates the positions of all the contents of the miscellaneous window.
	 */
	public void updateMiscWindowContentPositions() {
		// Axes Display label
		int[] relPos = new int[] { offsetX, offsetX };
		ControllerInterface<?> c = getTextArea("ActiveAxesDisplay").setPosition(relPos[0], relPos[1]);
		// Axes Display dropdown
		relPos = relativePosition(c, RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
		getDropdown("AxesDisplay").setPosition(relPos[0], relPos[1]);
		
		// Axes Display label
		relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		c = getTextArea("ActiveEEDisplay").setPosition(relPos[0], relPos[1]);
		// Axes Display dropdown
		relPos = relativePosition(c, RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
		getDropdown("EEDisplay").setPosition(relPos[0], relPos[1]);
		
		// Bounding box display toggle button
		relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		Button b = getButton("ToggleOBBs").setPosition(relPos[0], relPos[1]);

		// Update button color based on the value of the object display flag
		if (b.isOn()) {
			b.setLabel("Show OBBs");
			b.setColorBackground(buttonActColor);
			
		} else {
			b.setLabel("Hide OBBs");
			b.setColorBackground(buttonDefColor);
		}
		
		// Second robot toggle button
		relPos = relativePosition(b, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		b = getButton("ToggleRobot").setPosition(relPos[0], relPos[1]);
		
		// Update button color based on the value of the object display flag
		if (b.isOn()) {
			b.setColorBackground(buttonActColor);
			
		} else {
			b.setColorBackground(buttonDefColor);
		}
	
		// Update window background display
		relPos = relativePosition(b, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		background.setBackgroundHeight(relPos[1])
		.setHeight(relPos[1])
		.show();
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
			 getButton("FrontView").hide();
			 getButton("BackView").hide();
			 getButton("LeftView").hide();
			 getButton("RightView").hide();
			 getButton("TopView").hide();
			 getButton("BottomView").hide();

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
			 
		 } else if (windowState.equals("Misc")) {
			// Miscellaneous window
			 updateMiscWindowContentPositions();
		 }

		 // Update the camera view buttons
		 int[] relPos = relativePosition(windowTabs, RelativePoint.BOTTOM_RIGHT, offsetX, 0);

		 Button b = getButton("FrontView").setPosition(relPos[0], relPos[1]).show();
		 relPos = relativePosition(b, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		 b = getButton("BackView").setPosition(relPos[0], relPos[1]).show();
		 relPos = relativePosition(b, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		 b = getButton("LeftView").setPosition(relPos[0], relPos[1]).show();
		 relPos = relativePosition(b, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		 b = getButton("RightView").setPosition(relPos[0], relPos[1]).show();
		 relPos = relativePosition(b, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		 b = getButton("TopView").setPosition(relPos[0], relPos[1]).show();
		 relPos = relativePosition(b, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		 b = getButton("BottomView").setPosition(relPos[0], relPos[1]).show();

		 updateListContents();
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
			 app.g1.hide();
			 setGroupVisible(createObjWindow, false);
			 setGroupVisible(editObjWindow, false);
			 setGroupVisible(sharedElements, false);
			 setGroupVisible(scenarioWindow, false);
			 setGroupVisible(miscWindow, false);

			 updateWindowContentsPositions();

		 } else if (windowState.equals("Robot1") || windowState.equals("Robot2")) {
			 // Show pendant
			 setGroupVisible(createObjWindow, false);
			 setGroupVisible(editObjWindow, false);
			 setGroupVisible(sharedElements, false);
			 setGroupVisible(scenarioWindow, false);
			 setGroupVisible(miscWindow, false);
			 
			 if (!app.g1.isVisible()) {
				 updateWindowContentsPositions();
			 }

			 app.g1.show();

		 } else if (windowState.equals("Create")) {
			 // Show world object creation window
			 app.g1.hide();
			 setGroupVisible(editObjWindow, false);
			 setGroupVisible(scenarioWindow, false);
			 setGroupVisible(miscWindow, false);

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
			 app.g1.hide();
			 setGroupVisible(createObjWindow, false);
			 setGroupVisible(scenarioWindow, false);
			 setGroupVisible(miscWindow, false);

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
			 app.g1.hide();
			 setGroupVisible(createObjWindow, false);
			 setGroupVisible(editObjWindow, false);
			 setGroupVisible(sharedElements, false);
			 setGroupVisible(miscWindow, false);

			 if (!scenarioWindow.isVisible()) {
				 setGroupVisible(scenarioWindow, true);

				 clearAllInputFields();
				 updateWindowContentsPositions();
				 updateListContents();
				 resetListLabels();
			 }
			 
		 } else if (windowState.equals("Misc")) {
			 // Show miscellaneous window
			 app.g1.hide();
			 setGroupVisible(createObjWindow, false);
			 setGroupVisible(editObjWindow, false);
			 setGroupVisible(sharedElements, false);
			 setGroupVisible(scenarioWindow, false);

			 if (!miscWindow.isVisible()) {
				 setGroupVisible(miscWindow, true);
				 
				 updateWindowContentsPositions();
				 updateListContents();
				 resetListLabels();
			 }
		 }
	 }
}
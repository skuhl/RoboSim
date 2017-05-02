package window;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import controlP5.Background;
import controlP5.Button;
import controlP5.ControlEvent;
import controlP5.ControlListener;
import controlP5.ControlP5;
import controlP5.ControllerInterface;
import controlP5.DropdownList;
import controlP5.Group;
import controlP5.RadioButton;
import controlP5.Textarea;
import controlP5.Toggle;
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
import robot.DataManagement;
import robot.EEMapping;
import robot.Fixture;
import robot.RobotRun;
import robot.Scenario;
import ui.AxesDisplay;
import ui.ButtonTabs;
import ui.MyDropdownList;
import ui.MyTextfield;
import ui.RelativePoint;

public class WindowManager implements ControlListener {
	
	public static final int offsetX = 10,
							radioDim = 16,
							distBtwFieldsY = 15,
							distLblToFieldX = 5,
							distFieldToFieldX = 20,
							lLblWidth = 120,
							mLblWidth = 86,
							sLblWidth = 60,
							fieldHeight = 20,
							fieldWidth = 110,
							lButtonWidth = 88,
							mButtonWidth = 56,
							sButtonWidth = 26,
							sButtonHeight = 26,
							tButtonHeight = 20,
							sdropItemWidth = 80,
							mdropItemWidth = 90,
							ldropItemWidth = 120,
							dropItemHeight = 21,
							DIM_LBL = 3,
							DIM_TXT = 3,
							DIM_DDL = 1;
	
	private final int BG_C, F_TEXT_C, F_CURSOR_C, F_ACTIVE_C, F_BG_C, F_FG_C,
					  B_TEXT_C, B_DEFAULT_C, B_ACTIVE_C;

	private final ControlP5 UIManager;
	private final RobotRun app;
	
	private WindowTab menu;

	private Group createObjWindow, editObjWindow,
				  sharedElements, scenarioWindow, miscWindow;
	
	private final ButtonTabs windowTabs;
	private final Background background;
	
	/**
	 * Determine which input to use for importing a shape for a world object
	 * when it is created.
	 */
	private String lastModImport;

	/**
	 * Creates a new window with the given ControlP5 object as the parent
	 * and the given fonts which will be applied to the text in the window.
	 */
	public WindowManager(RobotRun appRef, ControlP5 manager, PFont small, PFont medium) {
		// Initialize content fields
		BG_C = appRef.color(210);
		F_TEXT_C = appRef.color(0);
		F_CURSOR_C = appRef.color(0);
		F_ACTIVE_C = appRef.color(255, 0, 0);
		F_BG_C = appRef.color(255);
		F_FG_C = appRef.color(0);
		B_TEXT_C = appRef.color(255);
		B_DEFAULT_C = appRef.color(70);
		B_ACTIVE_C = appRef.color(220, 40, 40);
		
		UIManager = manager;		 
		app = appRef;
		menu = null;
		lastModImport = null;
		
		manager.addListener(this);

		int[] relPos = new int[] { 0, 0 };
		
		String[] windowList = new String[] { "Hide", "Robot1", "Create", "Edit", "Scenario", "Misc" };
		
		// Create window tab bar
		windowTabs = (ButtonTabs)(new ButtonTabs(UIManager, "Tabs")
			 // Sets button text color
			 .setColorValue(B_TEXT_C)
			 .setColorBackground(B_DEFAULT_C)
			 .setColorActive(B_ACTIVE_C)
			 .setPosition(relPos[0], relPos[1])
			 .setSize(440, tButtonHeight))
			 .addItems(windowList);
		
		windowTabs.getCaptionLabel().setFont(medium);

		// Initialize camera view buttons
		addButton("FrontView", "F", createObjWindow, sButtonWidth, sButtonHeight, small).hide();
		addButton("BackView", "Bk", createObjWindow, sButtonWidth, sButtonHeight, small).hide();
		addButton("LeftView", "L", createObjWindow, sButtonWidth, sButtonHeight, small).hide();
		addButton("RightView", "R", createObjWindow, sButtonWidth, sButtonHeight, small).hide();
		addButton("TopView", "T", createObjWindow, sButtonWidth, sButtonHeight, small).hide();
		addButton("BottomView", "Bt", createObjWindow, sButtonWidth, sButtonHeight, small).hide();
		
		// Initialize the window background
		relPos = relativePosition(windowTabs, RelativePoint.BOTTOM_LEFT, 0, 0);
		background = UIManager.addBackground("WindowBackground")
							  .setPosition(relPos[0], relPos[1])
							  .setBackgroundColor(BG_C)
							  .setSize(windowTabs.getWidth(), 0);

		// Initialize the window groups
		sharedElements = addGroup("SHARED", relPos[0], relPos[1], windowTabs.getWidth(), 0);
		createObjWindow = addGroup("CREATEOBJ", relPos[0], relPos[1], windowTabs.getWidth(), 0);
		editObjWindow = addGroup("EDITOBJ", relPos[0], relPos[1], windowTabs.getWidth(), 0);
		scenarioWindow = addGroup("SCENARIO", relPos[0], relPos[1], windowTabs.getWidth(), 0);
		miscWindow = addGroup("MISC", relPos[0], relPos[1], windowTabs.getWidth(), 0);

		// Elements shared amongst the create and edit windows
		for (int idx = 0; idx < 3; ++idx) {
			addTextarea(String.format("DimLbl%d", idx), String.format("Dim(%d):", idx),
					sharedElements, fieldWidth, sButtonHeight, medium);
			
			addTextfield(String.format("Dim%d", idx), sharedElements, fieldWidth,
					fieldHeight, medium);
		}
		
		addButton("ClearFields", "Clear", sharedElements, mButtonWidth, sButtonHeight, small);
		
		// Create world object window elements
		addTextarea("ObjTypeLbl", "Type:", createObjWindow, mLblWidth, sButtonHeight, medium);

		addTextarea("ObjNameLbl", "Name:", createObjWindow, sLblWidth, fieldHeight, medium);
		addTextfield("ObjName", createObjWindow, fieldWidth, fieldHeight, medium);

		addTextarea("ShapeLbl", "Shape:", createObjWindow, mLblWidth, sButtonHeight, medium);
		addTextarea("FillLbl", "Fill:", createObjWindow, mLblWidth, sButtonHeight, medium);
		addTextarea("OutlineLbl", "Outline:", createObjWindow, mLblWidth, sButtonHeight, medium);

		addButton("CreateWldObj", "Create", createObjWindow, mButtonWidth, sButtonHeight, small);
		
		// Edit world object window elements
		addTextarea("ObjLabel", "Object:", editObjWindow, mLblWidth, fieldHeight, medium);
		
		addTextarea("Blank", "Inputs", editObjWindow, lLblWidth, fieldHeight, medium);
		addTextarea("Current", "Current", editObjWindow, fieldWidth, fieldHeight, medium);
		addTextarea("Default", "Default", editObjWindow, fieldWidth, fieldHeight, medium);

		addTextarea("XLbl", "X Position:", editObjWindow, lLblWidth, fieldHeight, medium);
		addTextfield("XCur", editObjWindow, fieldWidth, fieldHeight, medium);
		addTextarea("XDef", "N/A", editObjWindow, fieldWidth, fieldHeight, medium);
		
		addTextarea("YLbl", "Y Position:", editObjWindow, lLblWidth, fieldHeight, medium);
		addTextfield("YCur", editObjWindow, fieldWidth, fieldHeight, medium);
		addTextarea("YDef", "N/A", editObjWindow, fieldWidth, fieldHeight, medium);
		
		addTextarea("ZLbl", "Z Position:", editObjWindow, lLblWidth, fieldHeight, medium);
		addTextfield("ZCur", editObjWindow, fieldWidth, fieldHeight, medium);
		addTextarea("ZDef", "N/A", editObjWindow, fieldWidth, fieldHeight, medium);
		
		addTextarea("WLbl", "X Rotation:", editObjWindow, lLblWidth, fieldHeight, medium);
		addTextfield("WCur", editObjWindow, fieldWidth, fieldHeight, medium);
		addTextarea("WDef", "N/A", editObjWindow, fieldWidth, fieldHeight, medium);
		
		addTextarea("PLbl", "Y Rotation:", editObjWindow, lLblWidth, fieldHeight, medium);
		addTextfield("PCur", editObjWindow, fieldWidth, fieldHeight, medium);
		addTextarea("PDef", "N/A", editObjWindow, fieldWidth, fieldHeight, medium);
		
		addTextarea("RLbl", "Z Rotation:", editObjWindow, lLblWidth, fieldHeight, medium);
		addTextfield("RCur", editObjWindow, fieldWidth, fieldHeight, medium);
		addTextarea("RDef", "N/A", editObjWindow, fieldWidth, fieldHeight, medium);
		
		addTextarea("RefLbl", "Reference:", editObjWindow, lLblWidth, sButtonHeight, medium);
		
		addButton("MoveToCur", "Move to Current", editObjWindow, fieldWidth, sButtonHeight, small);
		addButton("UpdateWODef", "Update Default", editObjWindow, fieldWidth, sButtonHeight, small);
		addButton("MoveToDef", "Move to Default", editObjWindow, fieldWidth, sButtonHeight, small);
		
		addButton("ResDefs", "Restore Defaults", editObjWindow, lLblWidth, sButtonHeight, small);

		addButton("DeleteWldObj", "Delete", editObjWindow, mButtonWidth, sButtonHeight, small);
		
		// Scenario window elements
		addTextarea("SOptLbl", "Options:", scenarioWindow, mLblWidth, fieldHeight, medium);
		
		HashMap<Float, String> toggles = new HashMap<Float, String>();
		toggles.put(0f, "New");
		toggles.put(1f, "Load");
		toggles.put(2f, "Rename");
		addRadioButtons("ScenarioOpt", scenarioWindow, radioDim, radioDim, medium, toggles, 0f);
		
		addTextarea("SInstructions", "N/A", scenarioWindow, background.getWidth() - (2 * offsetX),
				54, small).hideScrollbar();
		
		addTextfield("SInput", scenarioWindow, fieldWidth, fieldHeight, medium);
		addButton("SConfirm", "N/A", scenarioWindow, mButtonWidth, sButtonHeight, small);
		
		// Miscellaneous window elements
		addTextarea("ActiveAxesDisplay", "Axes Display:", miscWindow, lLblWidth, sButtonHeight, medium);
		addTextarea("ActiveEEDisplay", "EE Display:", miscWindow, lLblWidth, sButtonHeight, medium);
		
		addButton("ToggleOBBs", "Hide OBBs", miscWindow, lButtonWidth, sButtonHeight, small);
		addButton("ToggleRobot", "Add Robot", miscWindow, lButtonWidth, sButtonHeight, small);

		// Dropdown list elements
		DropdownList ddlLimbo = addDropdown("EEDisplay", miscWindow, ldropItemWidth, dropItemHeight, 4,
				small);
		ddlLimbo.addItem(EEMapping.DOT.toString(), EEMapping.DOT)
				.addItem(EEMapping.LINE.toString(), EEMapping.LINE)
				.addItem(EEMapping.NONE.toString(), EEMapping.NONE)
				.setValue(0);
		
		ddlLimbo = addDropdown("AxesDisplay", miscWindow, ldropItemWidth, dropItemHeight, 4,
				small);
		ddlLimbo.addItem(AxesDisplay.AXES.toString(), AxesDisplay.AXES)
				.addItem(AxesDisplay.GRID.toString(), AxesDisplay.GRID)
				.addItem(AxesDisplay.NONE.toString(), AxesDisplay.NONE)
				.setValue(0);
		
		addDropdown("Scenario", scenarioWindow, ldropItemWidth, dropItemHeight, 4, small);
		addDropdown("Fixture", editObjWindow, ldropItemWidth, dropItemHeight, 4, small);
		 
		for (int idx = 0; idx < 1; ++idx) {
			// Dropdown lists for the dimension fields of an object
			addDropdown(String.format("DimDdl%d", idx), sharedElements, ldropItemWidth,
					dropItemHeight, 4, small);
		}
		
		addDropdown("Object", editObjWindow, ldropItemWidth, dropItemHeight, 4, small);
		
		ddlLimbo = addDropdown("Outline", createObjWindow, sdropItemWidth, dropItemHeight,
				4, small);
		ddlLimbo.addItem("black", app.color(0))
				.addItem("red", app.color(255, 0, 0))
				.addItem("green", app.color(0, 255, 0))
				.addItem("blue", app.color(0, 0, 255))
				.addItem("orange", app.color(255, 60, 0))
				.addItem("yellow", app.color(255, 255, 0))
				.addItem("pink", app.color(255, 0, 255))
				.addItem("purple", app.color(90, 0, 255));

		ddlLimbo = addDropdown("Fill", createObjWindow, mdropItemWidth, dropItemHeight,
				4, small);
		ddlLimbo.addItem("white", app.color(255))
				.addItem("black", app.color(0))
				.addItem("red", app.color(255, 0, 0))
				.addItem("green", app.color(0, 255, 0))
				.addItem("blue", app.color(0, 0, 255))
				.addItem("orange", app.color(255, 60, 0))
				.addItem("yellow", app.color(255, 255, 0))
				.addItem("pink", app.color(255, 0, 255))
				.addItem("purple", app.color(90, 0, 255))
				.addItem("sky blue", app.color(0, 255, 255))
				.addItem("dark green", app.color(0, 100, 15));

		ddlLimbo = addDropdown("Shape", createObjWindow, sdropItemWidth, dropItemHeight,
				4, small);
		ddlLimbo.addItem("Box", ShapeType.BOX)
				.addItem("Cylinder", ShapeType.CYLINDER)
				.addItem("Import", ShapeType.MODEL);

		ddlLimbo = addDropdown("ObjType", createObjWindow, sdropItemWidth, dropItemHeight,
				3, small);
		ddlLimbo.addItem("Parts", 0.0f)
				.addItem("Fixtures", 1.0f);
	}
	
	/**
	 * TODO
	 * 
	 * @param name
	 * @param lblTxt
	 * @param parent
	 * @param wdh
	 * @param hgt
	 * @param lblFont
	 * @return
	 */
	private Button addButton(String name, String lblTxt, Group parent, int wdh,
			int hgt, PFont lblFont) {
		
		Button b = UIManager.addButton(name)
				 			.setCaptionLabel(lblTxt)
				 			.setColorValue(B_TEXT_C)
				 			.setColorBackground(B_DEFAULT_C)
				 			.setColorActive(B_DEFAULT_C)
				 			.moveTo(parent)
				 			.setSize(wdh, hgt);
		
		b.getCaptionLabel().setFont(lblFont);
		return b;
	}
	
	/**
	 * TODO
	 * 
	 * @param name
	 * @param parent
	 * @param lblWdh
	 * @param lblHgt
	 * @param listLen
	 * @param lblFont
	 * @return
	 */
	private MyDropdownList addDropdown(String name, Group parent, int lblWdh,
			int lblHgt, int listLen, PFont lblFont) {
		
		MyDropdownList dropdown = new MyDropdownList(UIManager, name);
		
		dropdown.setSize(lblWdh, lblHgt * listLen)
				.setBarHeight(lblHgt)
				.setItemHeight(lblHgt)
				.setColorValue(B_TEXT_C)
				.setColorBackground(B_DEFAULT_C)
				.setColorActive(B_ACTIVE_C)
				.moveTo(parent)
				.close()
				.getCaptionLabel().setFont(lblFont);
		
		return dropdown;
	}
	
	/**
	 * TODO
	 * 
	 * @param name
	 * @param posX
	 * @param posY
	 * @param wdh
	 * @param hgt
	 * @return
	 */
	private Group addGroup(String name, int posX, int posY, int wdh, int hgt) {
		return UIManager.addGroup(name).setPosition(posX, posY)
				 .setBackgroundColor(BG_C)
				 .setSize(wdh, hgt)
				 .hideBar();
	}
	
	/**
	 * TODO
	 * 
	 * @param name
	 * @param parent
	 * @param togWdh
	 * @param togHgt
	 * @param lblFont
	 * @param elements
	 * @param iniActive
	 * @return
	 */
	private RadioButton addRadioButtons(String name, Group parent, int togWdh,
			int togHgt, PFont lblFont, HashMap<Float, String> elements,
			Float iniActive) {
		
		RadioButton rb = UIManager.addRadioButton(name)
				.setColorValue(B_DEFAULT_C)
				.setColorLabel(F_TEXT_C)
				.setColorActive(B_ACTIVE_C)
				.setBackgroundColor(BG_C)
				.moveTo(parent)
				.setSize(togWdh, togHgt);
		
		if (elements != null) {
			rb.setBackgroundHeight(togHgt * elements.size());
			// Add elements
			Set<Float> keys = elements.keySet();
			
			for (Float k : keys) {
				String lbl = elements.get(k);
				
				if (k != null && lbl != null) {
					rb.addItem(lbl, k);
				}
			}
		}
		
		// Set label fonts
		List<Toggle> items = rb.getItems();
		for (Toggle t : items) {
			t.getCaptionLabel().setFont(lblFont);
		}
		
		return rb;
	}
	
	/**
	 * TODO
	 * 
	 * @param name
	 * @param iniTxt
	 * @param parent
	 * @param wdh
	 * @param hgt
	 * @param lblFont
	 * @return
	 */
	private Textarea addTextarea(String name, String iniTxt, Group parent,
			int wdh, int hgt, PFont lblFont) {
		
		return UIManager.addTextarea(name, iniTxt, 0, 0, wdh, hgt)
						.setFont(lblFont)
						.setColor(F_TEXT_C)
						.setColorActive(F_ACTIVE_C)
						.setColorBackground(BG_C)
						.setColorForeground(BG_C)
						.moveTo(parent);
	}
	
	/**
	 * TODO
	 * 
	 * @param name
	 * @param parent
	 * @param wdh
	 * @param hgt
	 * @param lblFont
	 * @return
	 */
	private MyTextfield addTextfield(String name, Group parent, int wdh,
			int hgt, PFont lblFont) {
		
		MyTextfield t = new MyTextfield(UIManager, name, 0, 0, wdh, hgt);
		t.setColor(F_TEXT_C)
		 .setColorCursor(F_CURSOR_C)
		 .setColorActive(F_CURSOR_C)
		 .setColorLabel(BG_C)
		 .setColorBackground(F_BG_C)
		 .setColorForeground(F_FG_C)
		 .moveTo(parent);
		
		return t;
	}
	
	/**
	 * Handles value changes in certain controllers.
	 * 
	 * @param arg0	The value change event
	 */
	@Override
	public void controlEvent(ControlEvent arg0) {
		
		if (arg0.isFrom(windowTabs)) {
			// Update the window based on the button tab selected
			String actLbl = windowTabs.getActButLbl();
			
			if (actLbl.equals("Robot1")) {
				updateView( WindowTab.ROBOT1 );
				
			} else if (actLbl.equals("Robot2")) {
				updateView( WindowTab.ROBOT2 );
				
			} else if (actLbl.equals("Create")) {
				updateView( WindowTab.CREATE );
				
			} else if (actLbl.equals("Edit")) {
				updateView( WindowTab.EDIT );
				
			} else if (actLbl.equals("Scenario")) {
				updateView( WindowTab.SCENARIO );
				
			} else if (actLbl.equals("Misc")) {
				updateView( WindowTab.MISC );
				
			} else {
				updateView( null );
			}
			 
		 } else {
			 if (arg0.isFrom("Object") || arg0.isFrom("Shape") ||
					 arg0.isFrom("ScenarioOpt")) {
				 /* The selected item in these lists influence the layout of
				  * the menu */
				 updateWindowContentsPositions();
			 }
			 
			 if (arg0.isFrom("Object")) {
				// Update the input fields on the edit menu
				updateEditWindowFields();
				
			 } else if (arg0.isFrom("Fixture")) {
				WorldObject selected = getSelectedWO();
				
				if (selected instanceof Part) {
					// Set the reference of the Part to the currently active fixture
					Part p = (Part)selected;
					Fixture refFixture = (Fixture)getDropdown("Fixture").getSelectedItem();
					
					if (p.getFixtureRef() != refFixture) {
						/* Save the previous version of the world object on the
						 * undo stack */
						app.updateScenarioUndo( (WorldObject)p.clone() );
						p.setFixtureRef(refFixture);
					}
				}
				
			 } else if (arg0.isFrom("DimDdl0") || arg0.isFrom("Dim0")) {
				
				if (menu == WindowTab.CREATE) {
					// Update source input field focus
					lastModImport = arg0.getName();
				}
				
			 }
		 }
	}

	 /**
	  * Reinitialize any and all input fields
	  */
	 private void clearAllInputFields() {
		 clearGroupInputFields(null);
		 updateDimLblsAndFields();
	 }
	 
	 /**
	  * Clear only the input fields in either the create or edit windows, if it
	  * is active.
	  */
	 public void clearInputsFields() {
		 
		 if (menu == WindowTab.CREATE) {
			 clearGroupInputFields(createObjWindow);
			 clearSharedInputFields();
			 updateCreateWindowContentPositions();
			 
		 } else if (menu == WindowTab.EDIT) {
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

				 if (controller instanceof MyTextfield) {
					 // Clear anything inputted into the text field
					 controller = ((MyTextfield)controller).setValue("");
					 
				 } else if (controller instanceof MyDropdownList) {
					 // Reset the caption label of each dropdown list and close the list
					 MyDropdownList dropdown = (MyDropdownList)controller;
					 
					 if(!dropdown.getParent().equals(miscWindow)) {
						 dropdown.setValue(-1);
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
	  * Creates a world object form the input fields in the Create window.
	  */
	 public WorldObject createWorldObject() {
		 // Check the object type dropdown list
		 Object val = getDropdown("ObjType").getSelectedItem();
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

				 ShapeType type = (ShapeType)getDropdown("Shape").getSelectedItem();

				 int fill = (Integer)getDropdown("Fill").getSelectedItem();

				 switch(type) {
					 case BOX:
						 int strokeVal = (Integer)getDropdown("Outline").getSelectedItem();
						 Float[] shapeDims = getBoxDimensions();
						 // Construct a box shape
						 if (shapeDims != null && shapeDims[0] != null && shapeDims[1] != null && shapeDims[2] != null) {
							 wldObj = new Part(name, fill, strokeVal, shapeDims[0], shapeDims[1], shapeDims[2]);
						 }
						 break;
	
					 case CYLINDER:
						 strokeVal = (Integer)getDropdown("Outline").getSelectedItem();
						 shapeDims = getCylinderDimensions();
						 // Construct a cylinder
						 if (shapeDims != null && shapeDims[0] != null && shapeDims[1] != null) {
							 wldObj = new Part(name, fill, strokeVal, shapeDims[0], shapeDims[1]);
						 }
						 break;
	
					 case MODEL:
						 String srcFile = getShapeSourceFile();
						 shapeDims = getModelDimensions();
						 // Construct a complex model
						 if (shapeDims != null) {
							 ModelShape model;
	
							 if (shapeDims[0] != null) {
								 // Define shape scale
								 model = new ModelShape(srcFile, fill, shapeDims[0], app);
							 } else {
								 model = new ModelShape(srcFile, fill, app);
							 }
	
							 wldObj = new Part(name, model);
						 }
						 break;
					 default:
				 }

			 } else if (objectType == 1.0f) {
				 // Create a fixture
				 String name = getTextField("ObjName").getText();
				 ShapeType type = (ShapeType)getDropdown("Shape").getSelectedItem();

				 int fill = (Integer)getDropdown("Fill").getSelectedItem();

				 switch(type) {
					 case BOX:
						 int strokeVal = (Integer)getDropdown("Outline").getSelectedItem();
						 Float[] shapeDims = getBoxDimensions();
						 // Construct a box shape
						 if (shapeDims != null && shapeDims[0] != null && shapeDims[1] != null && shapeDims[2] != null) {
							 wldObj = new Fixture(name, fill, strokeVal, shapeDims[0], shapeDims[1], shapeDims[2]);
						 }
						 break;
	
					 case CYLINDER:
						 strokeVal = (Integer)getDropdown("Outline").getSelectedItem();
						 shapeDims = getCylinderDimensions();
						 // Construct a cylinder
						 if (shapeDims != null && shapeDims[0] != null && shapeDims[1] != null) {
							 wldObj = new Fixture(name, fill, strokeVal, shapeDims[0], shapeDims[1]);
						 }
						 break;
	
					 case MODEL:
						 String srcFile = getShapeSourceFile();
						 shapeDims = getModelDimensions();
						 // Construct a complex model
						 ModelShape model;
	
						 if (shapeDims != null && shapeDims[0] != null) {
							 // Define model scale value
							 model = new ModelShape(srcFile, fill, shapeDims[0], app);
						 } else {
							 model = new ModelShape(srcFile, fill, app);
						 }
	
						 wldObj = new Fixture(name, model);
						 break;
					 default:
				 }
			 }

		 } catch (NullPointerException NPEx) {
			 RobotRun.println("Missing parameter!");
			 NPEx.printStackTrace();
			 wldObj = null;
			 
		 } catch (ClassCastException CCEx) {
			 RobotRun.println("Invalid field?");
			 CCEx.printStackTrace();
			 wldObj = null;
			 
		 } catch (IndexOutOfBoundsException IOOBEx) {
			 RobotRun.println("Missing field?");
			 IOOBEx.printStackTrace();
			 wldObj = null;
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
			 ret = app.activeScenario.removeWorldObject( getSelectedWO() );
			 clearAllInputFields();
		 }

		 return ret;
	 }
	 
	 /**
	  * Puts the current position and orientation values of the selected object,
	  * in the position and orientation input fields of the edit window.
	  */
	 private void fillCurWithCur() {
		 WorldObject active = getSelectedWO();
		 // Get the part's default position and orientation
		 PVector pos = active.getLocalCenter();
		 PVector wpr = RobotRun.matrixToEuler( active.getLocalOrientationAxes() )
				 			   .mult(RobotRun.RAD_TO_DEG);
		 
		 pos = RobotRun.convertNativeToWorld(pos);
		 
		 // Fill the current position and orientation fields in the edit window
		 getTextField("XCur").setText( String.format("%4.3f", pos.x) );
		 getTextField("YCur").setText( String.format("%4.3f", pos.y) );
		 getTextField("ZCur").setText( String.format("%4.3f", pos.z) );
		 getTextField("WCur").setText( String.format("%4.3f", -wpr.x) );
		 getTextField("PCur").setText( String.format("%4.3f", -wpr.z) );
		 getTextField("RCur").setText( String.format("%4.3f", wpr.y) );
	 }
	 
	 /**
	  * Puts the default position and orientation values of the selected object,
	  * into the current position and orientation input fields of the edit
	  * window.
	  */
	 public void fillCurWithDef() {
		 WorldObject active = getSelectedWO();
		 
		 if (active instanceof Part) {
			 Part p = (Part)active;
			 // Get the part's current position and orientation
			 PVector pos = p.getDefaultCenter();
			 PVector wpr = RobotRun.matrixToEuler( p.getDefaultOrientationAxes() )
					 			   .mult(RobotRun.RAD_TO_DEG);
			 
			 pos = RobotRun.convertNativeToWorld(pos);
			 
			 // Fill the default position and orientation fields in the edit window
			 getTextField("XCur").setText( String.format("%4.3f", pos.x) );
			 getTextField("YCur").setText( String.format("%4.3f", pos.y) );
			 getTextField("ZCur").setText( String.format("%4.3f", pos.z) );
			 getTextField("WCur").setText( String.format("%4.3f", -wpr.x) );
			 getTextField("PCur").setText( String.format("%4.3f", -wpr.z) );
			 getTextField("RCur").setText( String.format("%4.3f", wpr.y) );
		 }
	 }
	 
	 /**
	  * Puts the current position and orientation values of the selected object,
	  * in the edit window, into the default position and orientation text
	  * fields.
	  */
	 public void fillDefWithCur() {
		 WorldObject active = getSelectedWO();
		 
		 if (active instanceof Part) {
			 // Get the part's default position and orientation
			 PVector pos = active.getLocalCenter();
			 PVector wpr = RobotRun.matrixToEuler( active.getLocalOrientationAxes() )
					 			   .mult(RobotRun.RAD_TO_DEG);
			 
			 pos = RobotRun.convertNativeToWorld(pos);
			 
			 // Fill the default position and orientation fields in the edit window
			 getTextArea("XDef").setText( String.format("%4.3f", pos.x) );
			 getTextArea("YDef").setText( String.format("%4.3f", pos.y) );
			 getTextArea("ZDef").setText( String.format("%4.3f", pos.z) );
			 getTextArea("WDef").setText( String.format("%4.3f", -wpr.x) );
			 getTextArea("PDef").setText( String.format("%4.3f", -wpr.z) );
			 getTextArea("RDef").setText( String.format("%4.3f", wpr.y) );
		 }
	 }
	 
	 /**
	  * Puts the default position and orientation values of the selected
	  * object, in the edit window, into the default position and orientation
	  * text fields.
	  */
	 private void fillDefWithDef() {
		 WorldObject active = getSelectedWO();
		 
		 if (active instanceof Part) {
			 Part p = (Part)active;
			 // Get the part's current position and orientation
			 PVector pos = p.getDefaultCenter();
			 PVector wpr = RobotRun.matrixToEuler( p.getDefaultOrientationAxes() )
					 			   .mult(RobotRun.RAD_TO_DEG);
			 
			 pos = RobotRun.convertNativeToWorld(pos);
			 
			 // Fill the default position and orientation fields in the edit window
			 getTextArea("XDef").setText( String.format("%4.3f", pos.x) );
			 getTextArea("YDef").setText( String.format("%4.3f", pos.y) );
			 getTextArea("ZDef").setText( String.format("%4.3f", pos.z) );
			 getTextArea("WDef").setText( String.format("%4.3f", -wpr.x) );
			 getTextArea("PDef").setText( String.format("%4.3f", -wpr.z) );
			 getTextArea("RDef").setText( String.format("%4.3f", wpr.y) );
		 }
	 }
	 
	 /**
	  * Returns the scenario associated with the label that is active
	  * for the scenario drop-down list.
	  * 
	  * @returning  The index value or null if no such index exists
	  */
	 public Scenario getActiveScenario() {

		 if (menu == WindowTab.SCENARIO) {
			 Object val = getDropdown("Scenario").getSelectedItem();

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
	 public WorldObject getSelectedWO() {
		 Object wldObj = getDropdown("Object").getSelectedItem();

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
		 return (AxesDisplay)getDropdown("AxesDisplay").getSelectedItem();
	 }

	 /**
	  * Returns a post-processed list of the user's input for the dimensions of
	  * the box world object (i.e. length, height, width). Valid values for a
	  * box's dimensions are between 10 and 800, inclusive. Any inputed value
	  * that is positive and outside the valid range is clamped to the valid
	  * range. So, if the user inputed 900 for the length, then it would be
	  * changed to 800. However, if a input is not a number or negative, then
	  * no other inputs are processed and null is returned. Although, if a
	  * field is left blank (i.e. ""), then that field is ignored. The array of
	  * processed input returned contains three Float objects. If any of the
	  * input was ignored, then its corresponding array element will be null.
	  * 
	  * @return a 3-element array: [length, height, width], or null
	  */
	 private Float[] getBoxDimensions() {
		 try {
			 // null values represent an uninitialized field
			 final Float[] dimensions = new Float[] { null, null, null };

			 // Pull from the dim fields
			 String lenField = getDimText(DimType.LENGTH),
					 hgtField = getDimText(DimType.HEIGHT),
					 wdhField = getDimText(DimType.WIDTH);

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
	  * Returns a post-processed list of the user's input for the dimensions of
	  * the cylinder world object (i.e. radius and height). Valid values for a
	  * cylinder's dimensions are between 5 and 800, inclusive. Any inputed value
	  * that is positive and outside the valid range is clamped to the valid
	  * range. So, if the user inputed 2 for the radius, then it would be
	  * changed to 5. However, if a input is not a number or negative, then
	  * no other inputs are processed and null is returned. Although, if a
	  * field is left blank (i.e. ""), then that field is ignored. The array of
	  * processed input returned contains two Float objects. If any of the
	  * input was ignored, then its corresponding array element will be null.
	  * 
	  * @return a 3-element array: [radius, height], or null
	  */
	 private Float[] getCylinderDimensions() {
		 try {
			 // null values represent an uninitialized field
			 final Float[] dimensions = new Float[] { null, null };

			 // Pull from the dim fields
			 String radField = getDimText(DimType.RADIUS),
					 hgtField = getDimText(DimType.HEIGHT);

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
	  * Returns a text-field, which corresponds to the given dimension type.
	  * The mapping from DimType to text-fields is:
	  * 	LENGTH, RADIUS, null	->	Dim0 text-field
	  * 	HEIGHT					->	Dim1 text-field
	  * 	WIDTH					->	Dim2 text-field
	  * 	SCALE					->	Dim0 or Dim1 text-field
	  * 	*depends on what window tab is active
	  * 
	  * @param name	A type of world object dimension, which corresponds to a
	  * 			text-field input in the UI
	  * @return		The text-field corresponding to the given dimension type
	  * @throws ClassCastException	Really shouldn't happen
	  */
	 private String getDimText(DimType t) throws ClassCastException {
		 
		 if (t == DimType.WIDTH) {
			 return ( (MyTextfield) UIManager.get("Dim2") ).getText();
			 
		 } else if (t == DimType.HEIGHT) {
			 return ( (MyTextfield) UIManager.get("Dim1") ).getText();
			 
		 } if (t == DimType.SCALE) {
			 int dimNum = 0;
			
			 if (menu == WindowTab.CREATE) {
				// Different text field in the create window tab
				dimNum = 1;
			 }
			 
			 return ( (MyTextfield) UIManager.get( String.format("Dim%d", dimNum) ) ).getText();
			 
		 } else {
			 return ( (MyTextfield) UIManager.get("Dim0") ).getText();
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
	  * Returns the end effector mapping associated with the ee mapping
	  * dropdown list.
	  * 
	  * @return	The active EEE Mapping state
	  */
	 public EEMapping getEEMapping() {
		 return (EEMapping)getDropdown("EEDisplay").getSelectedItem();
	 }

	 /**
	  * Returns a post-processed list of the user's input for the dimensions of
	  * the model world object (i.e. scale). Valid values for a model's
	  * dimensions are between 1 and 50, inclusive. Any inputed value that is
	  * positive and outside the valid range is clamped to the valid range. So,
	  * if the user inputed 100 for the length, then it would be changed to 50.
	  * However, if a input is not a number or negative, then no other inputs
	  * are processed and null is returned. Although, if a field is left blank
	  * (i.e. ""), then that field is ignored. The array of processed input
	  * returned contains one Float object. If any of the input was ignored,
	  * then its corresponding array element will be null.
	  * 
	  * @return a 3-element array: [scale], or null
	  */
	 private Float[] getModelDimensions() {
		 try {
			 // null values represent an uninitialized field
			 final Float[] dimensions = new Float[] { null };

			 String sclField;
			 // Pull from the Dim fields
			 sclField = getDimText(DimType.SCALE);

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
	  * 
	  * @return
	  */
	 private Float[] getCurrentValues() {
		 try {
			 // Pull from x, y, z, w, p, r, fields input fields
			 String[] orienVals = new String[] {
					getTextField("XCur").getText(), getTextField("YCur").getText(),
					getTextField("ZCur").getText(), getTextField("WCur").getText(),
					getTextField("PCur").getText(), getTextField("RCur").getText()
			 };
			 
			 // NaN indicates an uninitialized field
			 Float[] values = new Float[] { null, null, null, null, null, null };
			 
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
	  * Returns the radio button, with the given name, if it exists in one of
	  * the UI (excluding the pendant). If a non-radio button UI element exists
	  * in the UI, then a ClassCastException will be thrown. If no UI element
	  * with the given name exists, then null is returned.
	  * 
	  * @param name	Then name of a radio button in the UI
	  * @return		The radio button with the given name or null
	  * @throws ClassCastException	If a non-radio button UI element with the
	  * 							given name exists in the UI
	  */
	 public RadioButton getRadioButton(String name) throws ClassCastException {
		 return (RadioButton) UIManager.get(name);
	 }
	 
	 /**
	  * @return	Whether or not the robot display button is on
	  */
	 public boolean getRobotButtonState() {
		 return getButton("ToggleRobot").isOn();
	 }
	 
	 /**
	  * TODO
	  * 
	  * @return	The name of the .stl file to use as a model for a world object
	  */
	 private String getShapeSourceFile() {
		String filename = null;
		 
		if (menu == WindowTab.CREATE) {
			/* Determine which method of the source file input was edited last
			 * and use that input method as the source file */
			ControllerInterface<?> c = UIManager.get(lastModImport);
			
			if (c instanceof MyTextfield) {
				filename = ((MyTextfield)c).getText();
				
			} else if (c instanceof MyDropdownList) {
				try {
					filename = (String) ((MyDropdownList)c).getSelectedItem();
					
				} catch (ClassCastException CCEx) {
					// Should not happen!
					CCEx.printStackTrace();;
				}
			}
		}
			 
		return filename;
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
	 private MyTextfield getTextField(String name) throws ClassCastException {
		 return (MyTextfield) UIManager.get(name);
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
		 if (menu == WindowTab.SCENARIO) {
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
			 if (c instanceof MyTextfield && ((MyTextfield) c).isFocus()) {
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
	  * Determines if the active menu is a pendant.
	  * 
	  * @return	If the active menu is a pendant
	  */
	 public boolean isPendantActive() {
		 return menu == WindowTab.ROBOT1 || menu == WindowTab.ROBOT2;
	 }

	 /**
	  * Returns a position that is relative to the dimensions and position of
	  * the Controller object given.
	  * 
	  * @param obj		The element, with respect to which to perform the
	  * 				calculation
	  * @param pos		The corner reference point (TOP-LEFT, BOTTOM-RIGHT, etc.)
	  * @param offsetX	The x position offset from obj's position
	  * @param offsetY	The y position offset from obj's position
	  * @return			A doubleton containing the absolute x and y positions
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
			 
		 } else if (obj instanceof RadioButton) {
			 objDimensions = new float[] { obj.getWidth(), ((RadioButton) obj).getBackgroundHeight() };
			 
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
				 ((MyDropdownList)c).setValue(-1);
				 
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
	 * Updates the current menu of the UI and communicates with the PApplet to
	 * update the active robot, if necessary.
	 * 
	 * @param newView	The new menu to render
	 */
	private void updateView(WindowTab newView) {
		menu = newView;
		
		// Update active robot if necessary
		if (menu == WindowTab.ROBOT1) {
			app.setRobot(0);
			
		} else if (menu == WindowTab.ROBOT2) {
			app.setRobot(1);
		}
	}
	 
	/**
	 * Updates the tabs that are available in the applications main window.
	 * 
	 * @return	Whether the second Robot is hidden
	 */
	public boolean toggleSecondRobot() {
		
		if (menu == WindowTab.ROBOT2) {
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
		 Object val = getDropdown("Shape").getSelectedItem();

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
		 int ddlIdx = 0;
		 
		 // Update the dimension dropdowns
		 for (ddlIdx = 0; ddlIdx < DIM_DDL; ++ddlIdx) {
			 DropdownList dimDdl = getDropdown( String.format("DimDdl%d", ddlIdx) );
			 
			 if (!dimDdl.isVisible()) { break; }

			 Textarea dimLbl = getTextArea( String.format("DimLbl%d", ddlIdx) )
					 .setPosition(relPos[0], relPos[1]);
			 
			 relPos = relativePosition(dimLbl, RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
			 dimDdl.setPosition(relPos[0], relPos[1]);
			 
			 relPos = relativePosition(dimLbl, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		 }
		 
		 // Update the dimension text fields
		 for (int idx = 0; idx < DIM_TXT; ++idx) {
			 MyTextfield dimTxt = getTextField( String.format("Dim%d", idx) );
			 
			 if (!dimTxt.isVisible()) { break; }

			 Textarea dimLbl = getTextArea( String.format("DimLbl%d", idx + ddlIdx) )
					 .setPosition(relPos[0], relPos[1]);
			 
			 relPos = relativePosition(dimLbl, RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
			 dimTxt.setPosition(relPos[0], relPos[1]);
			 
			 relPos = relativePosition(dimLbl, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		 }

		 return relPos;
	 }

	 /**
	  * Update how many of the dimension field and label pairs are displayed in
	  * the create world object window based on which shape type is chosen from the shape dropdown list.
	  */
	 private void updateDimLblsAndFields() {
		 String[] lblNames = new String[0];
		 int txtFields = 0, ddlFields = 0;

		 if (menu == WindowTab.CREATE) {
			 ShapeType selectedShape = (ShapeType)getDropdown("Shape").getSelectedItem();

			 // Define the label text and the number of dimensionos fields to display
			 if (selectedShape == ShapeType.BOX) {
				 lblNames = new String[] { "Length:", "Height:", "Width" };
				 txtFields = 3;

			 } else if (selectedShape == ShapeType.CYLINDER) {
				 lblNames = new String[] { "Radius", "Height" };
				 txtFields = 2;

			 } else if (selectedShape == ShapeType.MODEL) {
				 lblNames = new String[] { "Source (1):", "Source (2):", "Scale:", };
				 txtFields = 2;
				 ddlFields = 1;
			 }

		 } else if (menu == WindowTab.EDIT) {
			 Object val = getDropdown("Object").getSelectedItem();

			 if (val instanceof WorldObject) {
				 Shape s = ((WorldObject)val).getForm();

				 if (s instanceof Box) {
					 lblNames = new String[] { "Length:", "Height:", "Width" };
					 txtFields = 3;

				 } else if (s instanceof Cylinder) {
					 lblNames = new String[] { "Radius", "Height" };
					 txtFields = 2;

				 } else if (s instanceof ModelShape) {
					 lblNames = new String[] { "Scale:" };
					 txtFields = 1;
				 }
			 }
		 }
		 
		 // Update dimension labels, dropdowns, and text fields
		 
		 for (int idx = 0; idx < DIM_LBL; ++idx) {
			 Textarea ta = getTextArea( String.format("DimLbl%d", idx) );
			 
			 if (idx < lblNames.length) {
				 ta.setText( lblNames[idx] ).show();
				 
			 } else {
				 ta.hide();
			 }
		 }
		 
		 for (int idx = 0; idx < DIM_DDL; ++idx) {
			 DropdownList ddl = getDropdown( String.format("DimDdl%d", idx) );
			 
			 ddl = (idx < ddlFields) ? ddl.show() : ddl.hide();
		 }
		 
		 for (int idx = 0; idx < DIM_TXT; ++idx) {
			MyTextfield tf = getTextField( String.format("Dim%d", idx) );
			
			if (idx < txtFields) {
				tf.show();
				
			} else {
				tf.hide();
			}
		 }
	 }
	 
	 /**
	  * Sets the dimension text fields, current text fields, default text areas,
	  * as well as the reference dropdown list in the edit window based on the
	  * currently selected world object, in the Object dropdown list.
	  */
	 public void updateEditWindowFields() {
		 WorldObject selected = getSelectedWO();
		 
		 if (selected != null) {
				// Set the dimension fields
				if (selected.getForm() instanceof Box) {
					getTextField("Dim0").setText( String.format("%4.3f", selected.getForm().getDim(DimType.LENGTH)) );
					getTextField("Dim1").setText( String.format("%4.3f", selected.getForm().getDim(DimType.HEIGHT)) );
					getTextField("Dim2").setText( String.format("%4.3f", selected.getForm().getDim(DimType.WIDTH)) );
					
				} else if (selected.getForm() instanceof Cylinder) {
					getTextField("Dim0").setText( String.format("%4.3f", selected.getForm().getDim(DimType.RADIUS)) );
					getTextField("Dim1").setText( String.format("%4.3f", selected.getForm().getDim(DimType.HEIGHT)) );
					
					
				} else if (selected.getForm() instanceof ModelShape) {
					getTextField("Dim0").setText( String.format("%4.3f", selected.getForm().getDim(DimType.SCALE)) );
				}
				
				fillCurWithCur();
				fillDefWithDef();
				
				// Set the reference dropdown
				MyDropdownList ddl = getDropdown("Fixture");
				
				if (selected instanceof Part) {
				
					Fixture ref = ((Part)selected).getFixtureRef();
					ddl.setItem(ref);
				
				} else {
					ddl.setValue(0);
				}
				
			 }
	 }

	 /**
	  * Updates the positions of all the contents of the world object editing window.
	  */
	 private void updateEditWindowContentPositions() {
		 updateDimLblsAndFields();
		 getButton("ClearFields").hide();

		 // Object list dropdown and label
		 int[] relPos = new int[] { offsetX, offsetX };
		 ControllerInterface<?> c = getTextArea("ObjLabel").setPosition(relPos[0], relPos[1]),
				 				c0 = null;
		 boolean isPart = getSelectedWO() instanceof Part;
		 
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
			 
		 } else {
			 // Only show them for parts
			 getTextArea("Default").hide();
		 }
		 
		 // X label and fields
		 relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		 c = getTextArea("XLbl").setPosition(relPos[0], relPos[1]);

		 relPos = relativePosition(c, RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
		 c0 = getTextField("XCur").setPosition(relPos[0], relPos[1]);
		 
		 if (isPart) {
			 relPos = relativePosition(c0, RelativePoint.TOP_RIGHT, distFieldToFieldX, 0);
			 getTextArea("XDef").setPosition(relPos[0], relPos[1]).show();
			 
		 } else {
			 getTextArea("XDef").hide();
		 }
		 
		 // Y label and fields
		 relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		 c = getTextArea("YLbl").setPosition(relPos[0], relPos[1]);

		 relPos = relativePosition(c, RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
		 c0 = getTextField("YCur").setPosition(relPos[0], relPos[1]);
		 
		 if (isPart) {
			 relPos = relativePosition(c0, RelativePoint.TOP_RIGHT, distFieldToFieldX, 0);
			 getTextArea("YDef").setPosition(relPos[0], relPos[1]).show();
			 
		 } else {
			 getTextArea("YDef").hide();
		 }
		 
		 // Z label and fields
		 relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		 c = getTextArea("ZLbl").setPosition(relPos[0], relPos[1]);;

		 relPos = relativePosition(c, RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
		 c0 = getTextField("ZCur").setPosition(relPos[0], relPos[1]);
		 
		 if (isPart) {
			 relPos = relativePosition(c0, RelativePoint.TOP_RIGHT, distFieldToFieldX, 0);
			 getTextArea("ZDef").setPosition(relPos[0], relPos[1]).show();
			 
		 } else {
			 getTextArea("ZDef").hide();
		 }
		 
		 // W label and fields
		 relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		 c = getTextArea("WLbl").setPosition(relPos[0], relPos[1]);

		 relPos = relativePosition(c, RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
		 c0 = getTextField("WCur").setPosition(relPos[0], relPos[1]);
		 
		 if (isPart) {
			 relPos = relativePosition(c0, RelativePoint.TOP_RIGHT, distFieldToFieldX, 0);
			 getTextArea("WDef").setPosition(relPos[0], relPos[1]).show();
			 
		 } else {
			 getTextArea("WDef").hide();
		 }
		 
		 // P label and fields
		 relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		 c = getTextArea("PLbl").setPosition(relPos[0], relPos[1]);

		 relPos = relativePosition(c, RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
		 c0 = getTextField("PCur").setPosition(relPos[0], relPos[1]);
		 
		 if (isPart) {
			 relPos = relativePosition(c0, RelativePoint.TOP_RIGHT, distFieldToFieldX, 0);
			 getTextArea("PDef").setPosition(relPos[0], relPos[1]).show();
			 
		 } else {
			 getTextArea("PDef").hide();
		 }
		 
		 // R label and fields
		 relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		 c = getTextArea("RLbl").setPosition(relPos[0], relPos[1]);

		 relPos = relativePosition(c, RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
		 c0 = getTextField("RCur").setPosition(relPos[0], relPos[1]);
		 
		 if (isPart) {
			relPos = relativePosition(c0, RelativePoint.TOP_RIGHT, distFieldToFieldX, 0);
			getTextArea("RDef").setPosition(relPos[0], relPos[1]).show();
			
		 } else {
			getTextArea("RDef").hide();
		 }
		 
		 // Move to current button
		 relPos = relativePosition(c0, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		 c = getButton("MoveToCur").setPosition(relPos[0], relPos[1]);
		 
		 if (isPart) {
			 /* Default values and fixture references are only relevant for parts */
			 
			 // Update default button
			 relPos = relativePosition(c, RelativePoint.TOP_RIGHT, distLblToFieldX, 0);
			 getButton("UpdateWODef").setPosition(relPos[0], relPos[1]).show();
			
			 // Move to default button
			 relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
			 c0 = getButton("MoveToDef").setPosition(relPos[0], relPos[1]).show();
			 
			 // Restore Defaults button
			 relPos = relativePosition(c0, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
			 c0 = getButton("ResDefs").setPosition(relPos[0], relPos[1]).show();
			 
			 relPos =  new int[] { offsetX, ((int)c0.getPosition()[1]) + c0.getHeight() + distBtwFieldsY };
			 c = getTextArea("RefLbl").setPosition(relPos[0], relPos[1]).show();
			
			 relPos = relativePosition(c, RelativePoint.TOP_RIGHT, distLblToFieldX,
					RobotRun.abs(fieldHeight - dropItemHeight) / 2);
			 getDropdown("Fixture").setPosition(relPos[0], relPos[1]).show();
			
		 } else {
			getButton("UpdateWODef").hide();
			getButton("MoveToDef").hide();
			getTextArea("RefLbl").hide();
			getDropdown("Fixture").hide();
			
			// Restore Defaults button
			relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
			c = getButton("ResDefs").setPosition(relPos[0], relPos[1]).show();
		 }
		 
		 // Delete button
		 relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
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
		 MyDropdownList dropdown = getDropdown("DimDdl0");
		 ArrayList<String> files = DataManagement.getDataFileNames();
		 
		 if (files != null) {
			 dropdown.clear();
			 
			 // Initialize the source dropdown list
			 for (String name : files) {
				 dropdown.addItem(name.substring(0, name.length() - 4), name);
			 }
			 
		 } else {
			 System.err.println("Missing data subfolder!");
		 }
				 
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
		 }

		 dropdown = getDropdown("Scenario");
		 dropdown.clear();
		 for (int idx = 0; idx < app.SCENARIOS.size(); ++idx) {
			 // Load all scenario indices
			 Scenario s = app.SCENARIOS.get(idx);
			 dropdown.addItem(s.getName(), s);
		 }
	 }
	 
	 /**
	  * TODO
	  * 
	  * @param scenarios
	  * @return
	  */
	 public int updateScenarios(ArrayList<Scenario> scenarios) {
		 float val = getRadioButton("ScenarioOpt").getValue();
		 MyDropdownList scenarioList = getDropdown("Scenario");
		 
		 if (val == 2f) {
			 // Rename a scenario
			 String newName = validScenarioName(getTextField("SInput").getText(), scenarios);
			 
			 if (newName != null) {
				 Scenario selected = (Scenario) scenarioList.getSelectedItem();
				 
				 if (selected != null) {
					 // Remove the backup for the old file
					DataManagement.removeScenario(selected.getName());
					selected.setName(newName);
					
				 	updateListContents();
				 	scenarioList.setItem(selected);
				 	return 0;
				 	
				 } else {
					 return -3;
				 }
				 
			 } else {
				 return -4;
			 }
			 
		 } else if (val == 1f) {
			 // Set a scenario
			 Scenario selected = (Scenario) scenarioList.getSelectedItem();
			 return (selected != null) ? 1 : -2;
			 
		 } else {
			 // Create a scenario
			 String name = validScenarioName(getTextField("SInput").getText(), scenarios);
			 
			 if (name != null) {
				 Scenario newScenario = new Scenario(name);
				 scenarios.add(newScenario);
				 
				 updateListContents();
				 scenarioList.setItem(newScenario);
				 return 2;
				 
			 } else {
				 return -1;
			 }
		 }
	 }

	 /**
	  * Updates the positions of all the contents of the scenario window.
	  */
	 private void updateScenarioWindowContentPositions() {
		// Scenario options label and radio buttons
		int[] relPos = new int[] { offsetX, offsetX };
		ControllerInterface<?> c = getTextArea("SOptLbl").setPosition(relPos[0], relPos[1]);
		
		relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, 0);
		c = getRadioButton("ScenarioOpt").setPosition(relPos[0], relPos[1]);
		
		float winVar = c.getValue();
		Textarea ta = getTextArea("SInstructions");
		MyDropdownList mdl = getDropdown("Scenario");
		MyTextfield mtf = getTextField("SInput");
		Button b = getButton("SConfirm");
		
		if (winVar == 2f) { // Rename scenario variation
			// Scenario instructions
			relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
			ta.setPosition(relPos[0], relPos[1]);
			ta.setText("Select the scenario you wish to rename from the dorpdown list and enter the new name into the text field below. Press RENAME to confirm the scenario's new name.");
			// Scenario dropdown list
			relPos = relativePosition(ta, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
			mdl.setPosition(relPos[0], relPos[1]).show();
			// Scenario input field
			relPos = relativePosition(mdl, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
			mtf.setPosition(relPos[0], relPos[1]).show();
			// Scenario confirm button
			relPos = relativePosition(mtf, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
			c = b.setPosition(relPos[0], relPos[1]);
			b.getCaptionLabel().setText("Rename");
			
		} else if (winVar == 1f) { // Load scenario variation
			// Scenario instructions
			relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
			ta.setPosition(relPos[0], relPos[1]);
			ta.setText("Select the scenario you wish to set as active from the dropdown list. Press LOAD to confirm your choice. A scenario name has to be unique, consist of only letters and numbers, and be of length less than 16.");
			// Scenario dropdown list
			relPos = relativePosition(ta, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
			c = mdl.setPosition(relPos[0], relPos[1]).show();
			
			// Scenario confirm button
			relPos = relativePosition(mdl, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
			c = b.setPosition(relPos[0], relPos[1]);
			b.getCaptionLabel().setText("Load");
			
			mtf.hide();
			
		} else { // New scenario variation
			// Scenario instructions
			relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
			ta.setPosition(relPos[0], relPos[1]);
			ta.setText("Enter the name of the scenario you wish to create and press CONFIRM. A scenario name has to be unique, consist of only letters and numbers, and be of length less than 16.");
			// Scenario input field
			relPos = relativePosition(ta, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
			mtf.setPosition(relPos[0], relPos[1]).show();
			// Scenario confirm button
			relPos = relativePosition(mtf, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
			c = b.setPosition(relPos[0], relPos[1]);
			b.getCaptionLabel().setText("Create");
			
			mdl.hide();
		}
		
		// Update window background display
		relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		background.setBackgroundHeight(relPos[1])
				  .setHeight(relPos[1])
				  .show();
	 }
	 
	/**
	 * Updates the positions of all the contents of the miscellaneous window.
	 */
	private void updateMiscWindowContentPositions() {
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

		// Update button color based on the state of the button
		if (b.isOn()) {
			b.setLabel("Show OBBs");
			b.setColorBackground(B_ACTIVE_C);
			
		} else {
			b.setLabel("Hide OBBs");
			b.setColorBackground(B_DEFAULT_C);
		}
		
		// Second robot toggle button
		relPos = relativePosition(b, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		b = getButton("ToggleRobot").setPosition(relPos[0], relPos[1]);
		
		// Update button color based on the state of the button
		if (b.isOn()) {
			b.setColorBackground(B_ACTIVE_C);
			
		} else {
			b.setColorBackground(B_DEFAULT_C);
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
		 if (menu == null) {
			 // Window is hidden
			 background.hide();
			 getButton("FrontView").hide();
			 getButton("BackView").hide();
			 getButton("LeftView").hide();
			 getButton("RightView").hide();
			 getButton("TopView").hide();
			 getButton("BottomView").hide();

			 return;

		 } else if (menu == WindowTab.CREATE) {
			 // Create window
			 updateCreateWindowContentPositions();

		 } else if (menu == WindowTab.EDIT) {
			 // Edit window
			 updateEditWindowContentPositions();

		 } else if (menu == WindowTab.SCENARIO) {
			 // Scenario window
			 updateScenarioWindowContentPositions();
			 
		 } else if (menu == WindowTab.MISC) {
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
	  * windowTabs.
	  */
	 public void updateWindowDisplay() {
		 		 
		 if (menu == null) {
			 // Hide any window
			 app.g1.hide();
			 setGroupVisible(createObjWindow, false);
			 setGroupVisible(editObjWindow, false);
			 setGroupVisible(sharedElements, false);
			 setGroupVisible(scenarioWindow, false);
			 setGroupVisible(miscWindow, false);

			 updateWindowContentsPositions();

		 } else if (menu == WindowTab.ROBOT1 || menu == WindowTab.ROBOT2) {
			 
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

		 } else if (menu == WindowTab.CREATE) {
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

		 } else if (menu == WindowTab.EDIT) {
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

		 } else if (menu == WindowTab.SCENARIO) {
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
			 
		 } else if (menu == WindowTab.MISC) {
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
	 
	 /**
	  * Updates the dimensions as well as the current position and orientation
	  * as well as the dimensions (and the fixture reference for parts) of the
	  * selected world object.
	  * 
	  * @return	if the selected world object was successfully modified 
	  */
	 public boolean updateWOCurrent() {
		 WorldObject toEdit = getSelectedWO();
		 RoboticArm model = RobotRun.getActiveRobot();
		 boolean edited = false;
		 
		 if (toEdit != null) {
			 
			 if (model != null && toEdit == model.held) {
				 // Cannot edit an object being held by the Robot
				 RobotRun.println("Cannot edit an object currently being held by the Robot!");
				 return false;
			 }

			 try {
				 boolean dimChanged = false;
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
				 Float[] inputValues = getCurrentValues();
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
					 oWPR.y = inputValues[5];
					 edited = true;
				 }
				 
				 if (inputValues[4] != null) {
					 oWPR.z = -inputValues[4];
					 edited = true;
				 }

				 // Convert values from the World to the Native coordinate system
				 PVector position = RobotRun.convertWorldToNative( oPosition );
				 PVector wpr = oWPR.mult(RobotRun.DEG_TO_RAD);
				 float[][] orientation = RobotRun.eulerToMatrix(wpr);
				 // Update the Objects position and orientation
				 toEdit.setLocalCenter(position);
				 toEdit.setLocalOrientationAxes(orientation);
				 
			 } catch (NullPointerException NPEx) {
				 RobotRun.println("Missing parameter!");
				 NPEx.printStackTrace();
				 return false;
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
		 
		 return edited;
	 }
	 
	 /**
	  * Updates the default position and orientation values of a part based on
	  * the input fields in the edit window.
	  * 
	  * @return	if the selected was successfully modified
	  */
	 public boolean updateWODefault() {
		 WorldObject toEdit = getSelectedWO();
		 boolean edited = false;
		 
		 if (toEdit instanceof Part) {
			Part p = (Part)toEdit;
			// Pull the object's current position and orientation
			PVector defaultPos = RobotRun.convertNativeToWorld( p.getDefaultCenter() );
			PVector defaultWPR = RobotRun.matrixToEuler( p.getDefaultOrientationAxes() )
										 .mult(RobotRun.RAD_TO_DEG);
			Float[] inputValues = getCurrentValues();
			
			// Update default position and orientation
			 if (inputValues[0] != null) {
				 defaultPos.x = inputValues[0];
				 edited = true;
			 }
			 
			 if (inputValues[1] != null) {
				 defaultPos.y = inputValues[1];
				 edited = true;
			 }
			 
			 if (inputValues[2] != null) {
				 defaultPos.z = inputValues[2];
				 edited = true;
			 }
			 
			 if (inputValues[3] != null) {
				 defaultWPR.x = -inputValues[3];
				 edited = true;
			 }
			 
			 if (inputValues[5] != null) {
				 defaultWPR.y = inputValues[5];
				 edited = true;
			 }
			 
			 if (inputValues[4] != null) {
				 defaultWPR.z = -inputValues[4];
				 edited = true;
			 }

			 // Convert values from the World to the Native coordinate system
			 PVector position = RobotRun.convertWorldToNative( defaultPos );
			 PVector wpr = defaultWPR.mult(RobotRun.DEG_TO_RAD);
			 float[][] orientation = RobotRun.eulerToMatrix(wpr);
			 // Update the Object's default position and orientation
			 p.setDefaultCenter(position);
			 p.setDefaultOrientationAxes(orientation);
			 
			 fillDefWithDef();
		 }
		 
		 return edited;
	 }
	 
	 /**
	  * Determine if the given string is a valid name to give to a scenario. A
	  * scenario name must consist only of letters and numbers, be unique
	  * amongst all scenarios, and be of length less than or equal to 26. If
	  * the given name is not a valid name, then null is returned. However, in
	  * the case when the name is too long, then the name is trimmed first,
	  * before verifying that the other two criteria hold for the trimmed name. 
	  * 
	  * @param name			The string to verify as a scenario name
	  * @param scenarios	The current list of scenarios in the application
	  * @return				The string (or a trimmed version) if the name is
	  * 					valid, null otherwise
	  */
	 private static String validScenarioName(String name, ArrayList<Scenario> scenarios) {
		// Names only consist of letters and numbers
		 if (Pattern.matches("[a-zA-Z0-9]+", name)) {
			 
			 if (name.length() > 16) {
				 // Names have a max length of 16 characters
				 name = name.substring(0, 16);
			 }

			 for (Scenario s : scenarios) {
				 if (s.getName().equals(name)) {
					 // Duplicate name
					 return null;
				 }
			 }

			 return name;
		 }
		 
		 // Invalid characters
		 return null;
	 }
}
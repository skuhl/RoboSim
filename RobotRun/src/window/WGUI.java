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
import geom.Fixture;
import geom.ModelShape;
import geom.Part;
import geom.RMath;
import geom.RMatrix;
import geom.Shape;
import geom.ShapeType;
import geom.WorldObject;
import global.Fields;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PImage;
import processing.core.PVector;
import robot.DataManagement;
import robot.EEMapping;
import robot.RobotRun;
import robot.RoboticArm;
import robot.Scenario;
import screen.DisplayLine;
import screen.MenuScroll;
import screen.ScreenMode;
import screen.ScreenType;
import ui.AxesDisplay;
import ui.MyButton;
import ui.MyButtonBar;
import ui.MyDropdownList;
import ui.MyRadioButton;
import ui.MyTextfield;
import ui.RelativePoint;

public class WGUI implements ControlListener {
	
	/**
	 * A dimension value (length, width, displacement, etc.), which is used to
	 * format the layout of a window tab's visible elements.
	 */
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

	/**
	 * The manager object, which contains all the UI elements.
	 */
	private final ControlP5 manager;
	
	/**
	 * A reference to the application, in which the UI resides.
	 */
	private final RobotRun app;
	
	/**
	 * The current state of the window tabs, which determines what window tab
	 * is rendered.
	 */
	private WindowTab menu;
	
	/**
	 * A group, which defines a set of elements belonging to a window tab, or
	 * shared amongst the window tabs.
	 */
	public final Group pendantWindow, createObjWindow, editObjWindow,
						sharedElements, scenarioWindow, miscWindow;
	
	/**
	 * Temporary group for refactoring pendant window rendering.
	 */
	private final Group limbo;
	
	/**
	 * The button bar controlling the window tab selection.
	 */
	private final MyButtonBar windowTabs;
	
	/**
	 * The background shared amongst all windows
	 */
	private final Background background;
	
	/**
	 * A cached set of text-areas used to display the pendant contents and
	 * options output.
	 */
	private final ArrayList<Textarea> displayLines;
	
	/**
	 * Determine which input to use for importing a shape for a world object
	 * when it is created.
	 */
	private String lastModImport;

	/**
	 * Creates a new window with the given ControlP5 object as the parent
	 * and the given fonts which will be applied to the text in the window.
	 */
	public WGUI(RobotRun appRef, PImage[][] buttonImages) {
		app = appRef;
		
		manager = new ControlP5(appRef);
		// Explicitly draw the ControlP5 elements
		manager.setAutoDraw(false);
		manager.addListener(this);
		
		menu = null;
		lastModImport = null;
		displayLines = new ArrayList<>();
		
		/* A local reference to a position in the UI [x, y] used to position UI
		 * elements relative to other UI elements */
		float[] relPos = new float[] { 0f, 0f };
		ControllerInterface<?> c1 = null, c2 = null;
		
		// The default set of labels for window tabs
		String[] windowList = new String[] { "Hide", "Robot1", "Create", "Edit", "Scenario", "Misc" };
		
		// Initialize the window tab selection bar
		windowTabs = (MyButtonBar)(new MyButtonBar(manager, "Tabs")
			 // Sets button text color
			 .setColorValue(Fields.B_TEXT_C)
			 .setColorBackground(Fields.B_DEFAULT_C)
			 .setColorActive(Fields.B_ACTIVE_C)
			 .setPosition(relPos[0], relPos[1])
			 .setSize(Fields.PENDANT_WIDTH, tButtonHeight))
			 .addItems(windowList);
		
		windowTabs.getCaptionLabel().setFont(Fields.medium);
		
		// Initialize the shared window background
		relPos = relativePosition(windowTabs, RelativePoint.BOTTOM_LEFT, 0, 0);
		background = manager.addBackground("WindowBackground")
							.setPosition(relPos[0], relPos[1])
							.setBackgroundColor(Fields.BG_C)
							.setSize(windowTabs.getWidth(), 0);

		// Initialize the window groups
		pendantWindow = addGroup("PENDANT", 0, 2 * offsetX, 440, 720);
		sharedElements = addGroup("SHARED", relPos[0], relPos[1], windowTabs.getWidth(), 0);
		createObjWindow = addGroup("CREATEOBJ", relPos[0], relPos[1], windowTabs.getWidth(), 0);
		editObjWindow = addGroup("EDITOBJ", relPos[0], relPos[1], windowTabs.getWidth(), 0);
		scenarioWindow = addGroup("SCENARIO", relPos[0], relPos[1], windowTabs.getWidth(), 0);
		miscWindow = addGroup("MISC", relPos[0], relPos[1], windowTabs.getWidth(), 0);
		limbo = addGroup("LIMBO", 0, 2 * offsetX, windowTabs.getWidth(), 0);
		
		relPos = relativePosition(windowTabs, RelativePoint.TOP_RIGHT, Fields.LARGE_BUTTON + 1, 0);
		c1 = addButton("record", buttonImages[0], relPos[0], relPos[1], Fields.SMALL_BUTTON, Fields.SMALL_BUTTON);
		
		relPos = relativePosition(c1, RelativePoint.TOP_RIGHT, Fields.LARGE_BUTTON + 1, 0);
		addButton("EE", buttonImages[1], relPos[0], relPos[1], Fields.SMALL_BUTTON, Fields.SMALL_BUTTON);
		
		// Initialize camera view buttons
		addButton("FrontView", "F", sButtonWidth, sButtonHeight, Fields.small).hide();
		addButton("BackView", "Bk", sButtonWidth, sButtonHeight, Fields.small).hide();
		addButton("LeftView", "L", sButtonWidth, sButtonHeight, Fields.small).hide();
		addButton("RightView", "R", sButtonWidth, sButtonHeight, Fields.small).hide();
		addButton("TopView", "T", sButtonWidth, sButtonHeight, Fields.small).hide();
		addButton("BottomView", "Bt", sButtonWidth, sButtonHeight, Fields.small).hide();
		
		// Pendant screen background?
		c1 = addTextarea("txt", "", pendantWindow, offsetX, 0,
				Fields.PENDANT_SCREEN_WIDTH, Fields.PENDANT_SCREEN_HEIGHT,
				Fields.B_TEXT_C, Fields.UI_LIGHT_C, Fields.small);
		
		// Pendant header
		addTextarea("header", "\0", pendantWindow, offsetX,	0,
				Fields.PENDANT_SCREEN_WIDTH, 20, Fields.UI_LIGHT_C,
				Fields.UI_DARK_C, Fields.medium);
		
		// Start with 25 text-areas for pendant output
		for (int idx = 0; idx < 25; ++idx) {
			displayLines.add( addTextarea(String.format("ps%d", idx), "\0",
					pendantWindow, 10, 0, 10, 20, Fields.UI_DARK_C,
					Fields.UI_LIGHT_C, Fields.medium) );
		}
		
		// Function button labels
		for (int i = 0; i < 5; i += 1) {
			// Calculate the position of each function label
			int posX = Fields.PENDANT_SCREEN_WIDTH * i / 5 + 15;
			int posY = Fields.PENDANT_SCREEN_HEIGHT - Fields.PENDANT_Y;
			
			addTextarea("fl" + i, "\0", pendantWindow, posX, posY,
					Fields.PENDANT_SCREEN_WIDTH / 5 - 5, 20, 0, Fields.UI_LIGHT_C,
					Fields.small);
		}
		
		// Function buttons
		
		relPos = relativePosition(c1, RelativePoint.BOTTOM_LEFT, 0, 2);
		c1 = addButton("f1", "F1", pendantWindow, relPos[0], relPos[1],
				Fields.PENDANT_SCREEN_WIDTH / 5 - 1, Fields.LARGE_BUTTON, Fields.bond);
		
		relPos = relativePosition(c1, RelativePoint.TOP_RIGHT, 1, 0);
		c2 = addButton("f2", "F2", pendantWindow, relPos[0], relPos[1],
				Fields.PENDANT_SCREEN_WIDTH / 5 - 1, Fields.LARGE_BUTTON, Fields.bond);
		
		relPos = relativePosition(c2, RelativePoint.TOP_RIGHT, 1, 0);
		c2 = addButton("f3", "F3", pendantWindow, relPos[0], relPos[1],
				Fields.PENDANT_SCREEN_WIDTH / 5 - 1, Fields.LARGE_BUTTON, Fields.bond);
		
		relPos = relativePosition(c2, RelativePoint.TOP_RIGHT, 1, 0);
		c2 = addButton("f4", "F4", pendantWindow, relPos[0], relPos[1],
				Fields.PENDANT_SCREEN_WIDTH / 5 - 1, Fields.LARGE_BUTTON, Fields.bond);
		
		relPos = relativePosition(c2, RelativePoint.TOP_RIGHT, 1, 0);
		c2 = addButton("f5", "F5", pendantWindow, relPos[0], relPos[1],
				Fields.PENDANT_SCREEN_WIDTH / 5 - 1, Fields.LARGE_BUTTON, Fields.bond);
		
		
		// Step button
		relPos = relativePosition(c1, RelativePoint.BOTTOM_LEFT, 0, 11);
		c1 = addButton("step", "STEP", pendantWindow, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.LARGE_BUTTON, Fields.bond);
		
		// Menu button
		relPos = relativePosition(c1, RelativePoint.TOP_RIGHT, 19, 0);
		c1 = addButton("menu", "MENU", pendantWindow, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
		
		// Previous button
		float smLrDiff = Fields.LARGE_BUTTON - Fields.SMALL_BUTTON;
		relPos = relativePosition(c1, RelativePoint.BOTTOM_LEFT, 0,	smLrDiff +
				16);
		addButton("prev", "PREV", pendantWindow, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
		
		// Select button
		relPos = relativePosition(c1, RelativePoint.TOP_RIGHT, 15, 0);
		c2 = addButton("select", "SELECT", pendantWindow, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
		
		// Edit button
		relPos = relativePosition(c2, RelativePoint.TOP_RIGHT, 1, 0);
		c2 = addButton("edit", "EDIT", pendantWindow, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
		
		// Data button
		relPos = relativePosition(c2, RelativePoint.TOP_RIGHT, 1, 0);
		c2 = addButton("data", "DATA", pendantWindow, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
		
		// Function-Control button
		relPos = relativePosition(c2, RelativePoint.TOP_RIGHT, 15, 0);
		c2 = addButton("fctn", "FCTN", pendantWindow, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
		
		// Next button
		relPos = relativePosition(c2, RelativePoint.BOTTOM_LEFT, 0,	smLrDiff +
				16);
		addButton("next", "NEXT", pendantWindow, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
		
		// Shift button
		relPos = relativePosition(c2, RelativePoint.TOP_RIGHT, 19, 0);
		addButton("shift", "SHIFT", pendantWindow, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.LARGE_BUTTON, Fields.bond);

		// Arrow buttons
		
		relPos = relativePosition(getButton("edit"), RelativePoint.BOTTOM_LEFT,
				smLrDiff / 2, 11);
		c2 = addButton("arrow_up", pendantWindow, buttonImages[2], relPos[0],
				relPos[1], Fields.SMALL_BUTTON, Fields.SMALL_BUTTON);
		
		relPos = relativePosition(c2, RelativePoint.BOTTOM_LEFT, 0, 1);
		c2 = addButton("arrow_dn", pendantWindow, buttonImages[3], relPos[0],
				relPos[1], Fields.SMALL_BUTTON, Fields.SMALL_BUTTON);
		
		relPos = relativePosition(getButton("select"), RelativePoint.BOTTOM_LEFT,
				smLrDiff / 2, smLrDiff + 16);
		addButton("arrow_lt", pendantWindow, buttonImages[4], relPos[0],
				relPos[1], Fields.SMALL_BUTTON, Fields.SMALL_BUTTON);
		
		relPos = relativePosition(getButton("data"), RelativePoint.BOTTOM_LEFT,
				smLrDiff / 2, smLrDiff + 16);
		addButton("arrow_rt", pendantWindow, buttonImages[5], relPos[0],
				relPos[1], Fields.SMALL_BUTTON, Fields.SMALL_BUTTON);
		
		
		// Reset button column
		
		float btmColsY = (float)Math.ceil(c2.getPosition()[1] + c2.getWidth() + 11);
		float resPosX = getButton("step").getPosition()[0];
		c1 = addButton("reset", "RESET", pendantWindow, resPosX, btmColsY,
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
		
		relPos = relativePosition(c1, RelativePoint.BOTTOM_LEFT, 0, 1);
		c2 = addButton("num7", "7", pendantWindow, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
		
		relPos = relativePosition(c2, RelativePoint.BOTTOM_LEFT, 0, 1);
		c2 = addButton("num4", "4", pendantWindow, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
		
		relPos = relativePosition(c2, RelativePoint.BOTTOM_LEFT, 0, 1);
		c2 = addButton("num1", "1", pendantWindow, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
		
		relPos = relativePosition(c2, RelativePoint.BOTTOM_LEFT, 0, 1);
		c2 = addButton("num0", "0", pendantWindow, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
		
		relPos = relativePosition(c2, RelativePoint.BOTTOM_LEFT, 0, 1);
		c2 = addButton("dash", "-", pendantWindow, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
		
		
		// Backspace button column
		
		relPos = relativePosition(c1, RelativePoint.TOP_RIGHT, 1, 0);
		c1 = addButton("bkspc", "BKSPC", pendantWindow, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
		
		relPos = relativePosition(c1, RelativePoint.BOTTOM_LEFT, 0, 1);
		c2 = addButton("num8", "8", pendantWindow, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
		
		relPos = relativePosition(c2, RelativePoint.BOTTOM_LEFT, 0, 1);
		c2 = addButton("num5", "5", pendantWindow, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
		
		relPos = relativePosition(c2, RelativePoint.BOTTOM_LEFT, 0, 1);
		c2 = addButton("num2", "2", pendantWindow, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
		
		relPos = relativePosition(c2, RelativePoint.BOTTOM_LEFT, 0, 1);
		c2 = addButton("period", ".", pendantWindow, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
		
		relPos = relativePosition(c2, RelativePoint.BOTTOM_LEFT, 0, 1);
		c2 = addButton("posn", "POSN", pendantWindow, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

		
		// Item button column
		
		relPos = relativePosition(c1, RelativePoint.TOP_RIGHT, 1, 0);
		c1 = addButton("item", "ITEM", pendantWindow, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
		
		relPos = relativePosition(c1, RelativePoint.BOTTOM_LEFT, 0, 1);
		c2 = addButton("num9", "9", pendantWindow, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
		
		relPos = relativePosition(c2, RelativePoint.BOTTOM_LEFT, 0, 1);
		c2 = addButton("num6", "6", pendantWindow, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
		
		relPos = relativePosition(c2, RelativePoint.BOTTOM_LEFT, 0, 1);
		c2 = addButton("num3", "3", pendantWindow, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
		
		relPos = relativePosition(c2, RelativePoint.BOTTOM_LEFT, 0, 1);
		c2 = addButton("comma", ",", pendantWindow, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
		
		relPos = relativePosition(c2, RelativePoint.BOTTOM_LEFT, 0, 1);
		c2 = addButton("io", "I/O", pendantWindow, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
		
		
		// Util button column
		
		relPos = relativePosition(getButton("arrow_dn"), RelativePoint.BOTTOM_LEFT,
				-smLrDiff / 2, 10);
		c1 = addButton("enter", "ENTER", pendantWindow, relPos[0], btmColsY,
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
		
		relPos = relativePosition(c1, RelativePoint.BOTTOM_LEFT, 0, 1);
		c2 = addButton("tool1", "TOOL1", pendantWindow, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
		
		relPos = relativePosition(c2, RelativePoint.BOTTOM_LEFT, 0, 1);
		c2 = addButton("tool2", "TOOL2", pendantWindow, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
		
		relPos = relativePosition(c2, RelativePoint.BOTTOM_LEFT, 0, 1);
		c2 = addButton("mvmu", "MVMU", pendantWindow, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
		
		relPos = relativePosition(c2, RelativePoint.BOTTOM_LEFT, 0, 1);
		c2 = addButton("SETUP", "SETUP", pendantWindow, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
		
		relPos = relativePosition(c2, RelativePoint.BOTTOM_LEFT, 0, 1);
		c2 = addButton("status", "STATUS", pendantWindow, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
		
		
		// Jog button columns
		
		c1 = getButton("shift");
		relPos = relativePosition(c1, RelativePoint.BOTTOM_LEFT, 0, 0);
		relPos[1] = btmColsY;
		
		float[] relPos2 = relativePosition(c1, RelativePoint.BOTTOM_LEFT,
				-Fields.LARGE_BUTTON - 1, 0);
		relPos2[1] = btmColsY;
		
		for (int idx = 1; idx <= 6; ++idx) {
			
			int sym = 88 + (idx - 1) % 3;
			String name = String.format("joint%d_pos", idx);
			String format = (idx < 4) ? " +%c\n(J%d)" : "+%cR\n(J%d)";
			String lbl = String.format(format, sym, idx);
			
			MyButton b = addButton(name, lbl, pendantWindow, relPos[0], relPos[1],
					Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
			
			b.getCaptionLabel().alignY(RobotRun.TOP);
			relPos = relativePosition(b, RelativePoint.BOTTOM_LEFT, 0, 1);
			
			name = String.format("joint%d_neg", idx);
			format = (idx < 4) ? " -%c\n(J%d)" : "-%cR\n(J%d)";
			lbl = String.format(format, sym, idx);
			
			b = addButton(name, lbl, pendantWindow, relPos2[0], relPos2[1],
					Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
			
			b.getCaptionLabel().alignY(RobotRun.TOP);
			relPos2 = relativePosition(b, RelativePoint.BOTTOM_LEFT, 0, 1);
		}
		
		
		// Hold button column
		
		relPos = relativePosition(c1, RelativePoint.BOTTOM_LEFT,
				-2 * (Fields.LARGE_BUTTON + 1), 0);
		relPos[1] = btmColsY;
		c1 = addButton("hold", "HOLD", pendantWindow, relPos[0],
				btmColsY, Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
		
		relPos = relativePosition(c1, RelativePoint.BOTTOM_LEFT, 0, 1);
		c2 = addButton("fwd", "FWD", pendantWindow, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
		
		relPos = relativePosition(c2, RelativePoint.BOTTOM_LEFT, 0, 1);
		c2 = addButton("bwd", "BWD", pendantWindow, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
		
		relPos = relativePosition(c2, RelativePoint.BOTTOM_LEFT, 0, 1);
		c2 = addButton("coord", "COORD", pendantWindow, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
		
		relPos = relativePosition(c2, RelativePoint.BOTTOM_LEFT, 0, 1);
		c2 = addButton("spdup", "+%", pendantWindow, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);
		
		relPos = relativePosition(c2, RelativePoint.BOTTOM_LEFT, 0, 1);
		c2 = addButton("spddn", "-%", pendantWindow, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

		// Initialize the elements shared amongst the create and edit windows
		for (int idx = 0; idx < 3; ++idx) {
			addTextarea(String.format("DimLbl%d", idx), String.format("Dim(%d):", idx),
					sharedElements, fieldWidth, sButtonHeight, Fields.medium);
			
			addTextfield(String.format("Dim%d", idx), sharedElements, fieldWidth,
					fieldHeight, Fields.medium);
		}
		
		addButton("ClearFields", "Clear", sharedElements, mButtonWidth, sButtonHeight, Fields.small);
		
		// Initialize the world object creation window elements
		addTextarea("ObjTypeLbl", "Type:", createObjWindow, mLblWidth, sButtonHeight, Fields.medium);

		addTextarea("ObjNameLbl", "Name:", createObjWindow, sLblWidth, fieldHeight, Fields.medium);
		addTextfield("ObjName", createObjWindow, fieldWidth, fieldHeight, Fields.medium);

		addTextarea("ShapeLbl", "Shape:", createObjWindow, mLblWidth, sButtonHeight, Fields.medium);
		addTextarea("FillLbl", "Fill:", createObjWindow, mLblWidth, sButtonHeight, Fields.medium);
		addTextarea("OutlineLbl", "Outline:", createObjWindow, mLblWidth, sButtonHeight, Fields.medium);

		addButton("CreateWldObj", "Create", createObjWindow, mButtonWidth, sButtonHeight, Fields.small);
		
		// Initialize the world object edit window elements
		addTextarea("ObjLabel", "Object:", editObjWindow, mLblWidth, fieldHeight, Fields.medium);
		
		addTextarea("Blank", "Inputs", editObjWindow, lLblWidth, fieldHeight, Fields.medium);
		addTextarea("Current", "Current", editObjWindow, fieldWidth, fieldHeight, Fields.medium);
		addTextarea("Default", "Default", editObjWindow, fieldWidth, fieldHeight, Fields.medium);

		addTextarea("XLbl", "X Position:", editObjWindow, lLblWidth, fieldHeight, Fields.medium);
		addTextfield("XCur", editObjWindow, fieldWidth, fieldHeight, Fields.medium);
		addTextarea("XDef", "N/A", editObjWindow, fieldWidth, fieldHeight, Fields.medium);
		
		addTextarea("YLbl", "Y Position:", editObjWindow, lLblWidth, fieldHeight, Fields.medium);
		addTextfield("YCur", editObjWindow, fieldWidth, fieldHeight, Fields.medium);
		addTextarea("YDef", "N/A", editObjWindow, fieldWidth, fieldHeight, Fields.medium);
		
		addTextarea("ZLbl", "Z Position:", editObjWindow, lLblWidth, fieldHeight, Fields.medium);
		addTextfield("ZCur", editObjWindow, fieldWidth, fieldHeight, Fields.medium);
		addTextarea("ZDef", "N/A", editObjWindow, fieldWidth, fieldHeight, Fields.medium);
		
		addTextarea("WLbl", "X Rotation:", editObjWindow, lLblWidth, fieldHeight, Fields.medium);
		addTextfield("WCur", editObjWindow, fieldWidth, fieldHeight, Fields.medium);
		addTextarea("WDef", "N/A", editObjWindow, fieldWidth, fieldHeight, Fields.medium);
		
		addTextarea("PLbl", "Y Rotation:", editObjWindow, lLblWidth, fieldHeight, Fields.medium);
		addTextfield("PCur", editObjWindow, fieldWidth, fieldHeight, Fields.medium);
		addTextarea("PDef", "N/A", editObjWindow, fieldWidth, fieldHeight, Fields.medium);
		
		addTextarea("RLbl", "Z Rotation:", editObjWindow, lLblWidth, fieldHeight, Fields.medium);
		addTextfield("RCur", editObjWindow, fieldWidth, fieldHeight, Fields.medium);
		addTextarea("RDef", "N/A", editObjWindow, fieldWidth, fieldHeight, Fields.medium);
		
		addTextarea("RefLbl", "Reference:", editObjWindow, lLblWidth, sButtonHeight, Fields.medium);
		
		addButton("MoveToCur", "Move to Current", editObjWindow, fieldWidth, sButtonHeight, Fields.small);
		addButton("UpdateWODef", "Update Default", editObjWindow, fieldWidth, sButtonHeight, Fields.small);
		addButton("MoveToDef", "Move to Default", editObjWindow, fieldWidth, sButtonHeight, Fields.small);
		
		addButton("ResDefs", "Restore Defaults", editObjWindow, lLblWidth, sButtonHeight, Fields.small);

		addButton("DeleteWldObj", "Delete", editObjWindow, mButtonWidth, sButtonHeight, Fields.small);
		
		// Initialize the scenario window elements
		addTextarea("SOptLbl", "Options:", scenarioWindow, mLblWidth, fieldHeight, Fields.medium);
		
		HashMap<Float, String> toggles = new HashMap<>();
		toggles.put(0f, "New");
		toggles.put(1f, "Load");
		toggles.put(2f, "Rename");
		
		RadioButton rb = addRadioButtons("ScenarioOpt", scenarioWindow, radioDim, radioDim, Fields.medium, toggles, 0f);
		Toggle t = rb.getItem(0);
		
		rb.setItemsPerRow(3);
		rb.setSpacingColumn( (background.getWidth() - 2 * offsetX - 3 * t.getWidth()) / 3 );
		
		addTextarea("SInstructions", "N/A", scenarioWindow, background.getWidth() - (2 * offsetX),
				54, Fields.small).hideScrollbar();
		
		addTextfield("SInput", scenarioWindow, fieldWidth, fieldHeight, Fields.medium);
		addButton("SConfirm", "N/A", scenarioWindow, mButtonWidth, sButtonHeight, Fields.small);
		
		// Initialize the miscellaneous window elements
		addTextarea("ActiveAxesDisplay", "Axes Display:", miscWindow, lLblWidth, sButtonHeight, Fields.medium);
		addTextarea("ActiveEEDisplay", "EE Display:", miscWindow, lLblWidth, sButtonHeight, Fields.medium);
		
		addButton("ToggleOBBs", "Hide OBBs", miscWindow, lButtonWidth, sButtonHeight, Fields.small);
		addButton("ToggleRobot", "Add Robot", miscWindow, lButtonWidth, sButtonHeight, Fields.small);

		/* Initialize dropdown list elements
		 * 
		 * NOTE: the order in which the dropdown lists matters!
		 * 		(Adding the dropdown lists last places them in front of the
		 * other UI elements, which is important, when the list is open) */
		DropdownList ddlLimbo = addDropdown("EEDisplay", miscWindow, ldropItemWidth, dropItemHeight, 4,
				Fields.small);
		ddlLimbo.addItem(EEMapping.DOT.toString(), EEMapping.DOT)
				.addItem(EEMapping.LINE.toString(), EEMapping.LINE)
				.addItem(EEMapping.NONE.toString(), EEMapping.NONE)
				.setValue(0);
		
		ddlLimbo = addDropdown("AxesDisplay", miscWindow, ldropItemWidth, dropItemHeight, 4,
				Fields.small);
		ddlLimbo.addItem(AxesDisplay.AXES.toString(), AxesDisplay.AXES)
				.addItem(AxesDisplay.GRID.toString(), AxesDisplay.GRID)
				.addItem(AxesDisplay.NONE.toString(), AxesDisplay.NONE)
				.setValue(0);
		
		addDropdown("Scenario", scenarioWindow, ldropItemWidth, dropItemHeight, 4, Fields.small);
		addDropdown("Fixture", editObjWindow, ldropItemWidth, dropItemHeight, 4, Fields.small);
		 
		for (int idx = 0; idx < 1; ++idx) {
			// dimension field dropdown lists
			addDropdown(String.format("DimDdl%d", idx), sharedElements, ldropItemWidth,
					dropItemHeight, 4, Fields.small);
		}
		
		addDropdown("Object", editObjWindow, ldropItemWidth, dropItemHeight, 4, Fields.small);
		
		ddlLimbo = addDropdown("Outline", createObjWindow, sdropItemWidth, dropItemHeight,
				4, Fields.small);
		ddlLimbo.addItem("black", app.color(0))
				.addItem("red", app.color(255, 0, 0))
				.addItem("green", app.color(0, 255, 0))
				.addItem("blue", app.color(0, 0, 255))
				.addItem("orange", app.color(255, 60, 0))
				.addItem("yellow", app.color(255, 255, 0))
				.addItem("pink", app.color(255, 0, 255))
				.addItem("purple", app.color(90, 0, 255));

		ddlLimbo = addDropdown("Fill", createObjWindow, mdropItemWidth, dropItemHeight,
				4, Fields.small);
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
				4, Fields.small);
		ddlLimbo.addItem("Box", ShapeType.BOX)
				.addItem("Cylinder", ShapeType.CYLINDER)
				.addItem("Import", ShapeType.MODEL);

		ddlLimbo = addDropdown("ObjType", createObjWindow, sdropItemWidth, dropItemHeight,
				3, Fields.small);
		ddlLimbo.addItem("Parts", 0.0f)
				.addItem("Fixtures", 1.0f);
	}
	
	/**
	 * Adds a button with the given name, label, parent, width, height, and font
	 * to the UI. The UI's color scheme for a button are applied to the newly
	 * added button.
	 * 
	 * @param name		The name (or ID) of the UI element, which must unique
	 * 					amongst all other UI elements!
	 * @param lblTxt	The button's label or the text displayed on the button,
	 * 					when it is rendered
	 * @param parent	The window group, to which this button belongs
	 * @param wdh		The width of the button
	 * @param hgt		The length of the button
	 * @param lblFont	The button's label font
	 * @return			A reference to the new button
	 */
	private MyButton addButton(String name, String lblTxt, Group parent, int wdh,
			int hgt, PFont lblFont) {
		
		MyButton b = new MyButton(manager, name);
		
		b.setCaptionLabel(lblTxt)
		 .setColorValue(Fields.B_TEXT_C)
		 .setColorBackground(Fields.B_DEFAULT_C)
		 .setColorActive(Fields.B_ACTIVE_C)
	 	 .moveTo(parent)
		 .setSize(wdh, hgt)
		 .getCaptionLabel().setFont(lblFont);
		
		return b;
	}
	
	/**
	 * Adds a button to the UI with the given name, label, parent, xy position,
	 * width, height, and font. The UI's color scheme is applied to the button.
	 * 
	 * @param name		The name (or ID) of the button, which must be unique
	 * 					amongst all controllers in the UI!
	 * @param lblTxt	The text which will be rendered on the button
	 * @param parent	The window group, to which this button belongs
	 * @param posX		The x position of the button relative to the position
	 * 					of its parent
	 * @param posY		The y position of the button relative to the position
	 * 					of its parent
	 * @param wdh		The width of the button
	 * @param hgt		The height of the button
	 * @param lblFont	The font of the button label's text
	 * @return			A reference to the new button
	 */
	private MyButton addButton(String name, String lblTxt, Group parent,
			float posX, float posY, int wdh, int hgt, PFont lblFont) {
		
		MyButton b = new MyButton(manager, name);
		
		b.setCaptionLabel(lblTxt)
		 .setColorValue(Fields.B_TEXT_C)
		 .setColorBackground(Fields.B_DEFAULT_C)
		 .setColorActive(Fields.B_ACTIVE_C)
		 .setPosition(posX, posY)
	 	 .moveTo(parent)
		 .setSize(wdh, hgt)
		 .getCaptionLabel().setFont(lblFont);
		
		return b;
	}
	
	/**
	 * Adds a button to the UI with the given name, label text, width, height,
	 * and font.
	 * 
	 * @param name		The name (or ID) of the button, which must be unique
	 * 					amongst all UI elements!
	 * @param lblTxt	The text displayed on the button
	 * @param wdh		The width of the button
	 * @param hgt		The height of the button
	 * @param lblFont	The button text's font
	 * @return			A reference to the new button
	 */
	private MyButton addButton(String name, String lblTxt, int wdh, int hgt,
			PFont lblFont) {
		
		MyButton b = new MyButton(manager, name);
		
		b.setCaptionLabel(lblTxt)
		 .setColorValue(Fields.B_TEXT_C)
		 .setColorBackground(Fields.B_DEFAULT_C)
		 .setColorActive(Fields.B_ACTIVE_C)
		 .setSize(wdh, hgt)
		 .getCaptionLabel().setFont(lblFont);
		
		return b;
	}
	
	/**
	 * Adds a button to the UI with the given name, image labels, xy position,
	 * width, and height. The parent of the button is the top element of the
	 * UI.
	 * 
	 * @param name		The name (or ID) of the button, which must be unique
	 * 					amongst all UI elements!
	 * @param imgLbls	A list of images, which will be rendered on the button.
	 * @param posX		The x position of the button relative to the position
	 * 					of its parent
	 * @param posY		The y position of the button relative to the position
	 * 					of its parent
	 * @param wdh		The width of the button
	 * @param hgt		The height of the button
	 * @return			A reference to the new button
	 */
	private MyButton addButton(String name, PImage[] imgLbls, float posX,
			float posY, int wdh, int hgt) {
		
		MyButton b = new MyButton(manager, name);
		
		b.setImages(imgLbls)
		 .setPosition(posX, posY)
		 .setSize(wdh, hgt)
		 .updateSize();
		
		return b;
	}
	
	/**
	 * Adds a button to the UI with the given name, parent, image labels, xy
	 * position, width, and height.
	 * 
	 * @param name		The name (or ID) of the button, which must be unique
	 * 					amongst all UI elements!
	 * @param parent	The window group, to which this button belongs
	 * @param imgLbls	A list of images, which will be rendered on the button
	 * @param posX		The x position of the button relative to the position
	 * 					of its parent
	 * @param posY		The y position of the button relative to the position
	 * 					of its parent
	 * @param wdh		The width of the button
	 * @param hgt		The height of the button
	 * @return			A reference to the new button
	 */
	private MyButton addButton(String name, Group parent, PImage[] imgLbls,
			float posX, float posY, int wdh, int hgt) {
		
		MyButton b = new MyButton(manager, name);
		
		b.setImages(imgLbls)
		 .moveTo(parent)
		 .setPosition(posX, posY)
		 .setSize(wdh, hgt)
		 .updateSize();
		
		return b;
	}
	
	/**
	 * Adds an empty dropdown list with the given name, parent, label
	 * dimensions, list display length, and label font to the UI. The UI's
	 * color scheme is applied to the new dropdown list.
	 * 
	 * @param name		The name (or ID) of the UI element, which must unique
	 * 					amongst all other UI elements!
	 * @param parent	The window group, to which this dropdown list belongs
	 * @param lblWdh	The width of the dropdown list's label (as well as the
	 * 					label for a single element)
	 * @param lblHgt	The height of the dropdown list's label (as well as a
	 * 					the label for single element in the list)
	 * @param listLen	The maximum number of list elements to display at once
	 * 					(the display is scrollable)
	 * @param lblFont	The dropdown list's label font
	 * @return			A reference to the new dropdown list
	 */
	private MyDropdownList addDropdown(String name, Group parent, int lblWdh,
			int lblHgt, int listLen, PFont lblFont) {
		
		MyDropdownList dropdown = new MyDropdownList(manager, name);
		
		dropdown.setSize(lblWdh, lblHgt * listLen)
				.setBarHeight(lblHgt)
				.setItemHeight(lblHgt)
				.setColorValue(Fields.B_TEXT_C)
				.setColorBackground(Fields.B_DEFAULT_C)
				.setColorActive(Fields.B_ACTIVE_C)
				.moveTo(parent)
				.close()
				.getCaptionLabel().setFont(lblFont);
		
		return dropdown;
	}
	
	/**
	 * Adds a new group to the UI. In this UI, a group defines a list of
	 * elements rendered in a single window, or that are shared amongst
	 * multiple windows.
	 * 
	 * @param name	The name (or ID) of the UI group element, which must be
	 * 				unique amongst all UI elements!
	 * @param posX	The absolute (or reference) x position of the UI group
	 * @param posY	The absolute (or reference) y position of the UI group
	 * @param wdh	The width of the group element
	 * @param hgt	The height of the group element
	 * @return		A reference to the new group
	 */
	private Group addGroup(String name, float posX, float posY, int wdh, int hgt) {
		return manager.addGroup(name).setPosition(posX, posY)
				 .setBackgroundColor(Fields.BG_C)
				 .setSize(wdh, hgt)
				 .hideBar();
	}
	
	/**
	 * Adds a new radio button to the UI with the given name, parent, toggle
	 * dimensions, toggle label font, list of toggles, and the value of the
	 * initially active toggle. 
	 * 
	 * @param name		The name (or ID) of the UI element, which must be
	 * 					unique amongst all UI elements!
	 * @param parent	The window group, to which this radio button belongs
	 * @param togWdh	The width of a toggle element
	 * @param togHgt	The height of a toggle element
	 * @param lblFont	The font for the labels of the toggle elements
	 * @param elements	The list of toggles in the radio button
	 * @param iniActive	The value of the toggle, which is initially active
	 * @return			A reference to the new radio button
	 */
	private MyRadioButton addRadioButtons(String name, Group parent, int togWdh,
			int togHgt, PFont lblFont, HashMap<Float, String> elements,
			Float iniActive) {
		
		MyRadioButton rb = new MyRadioButton(manager, name);
		rb.setColorValue(Fields.B_DEFAULT_C)
		  .setColorLabel(Fields.F_TEXT_C)
		  .setColorActive(Fields.B_ACTIVE_C)
		  .setBackgroundColor(Fields.BG_C)
		  .moveTo(parent)
		  .setSize(togWdh, togHgt);
		
		if (elements != null) {
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
			t.setColorBackground(Fields.B_DEFAULT_C)
			 .setColorLabel(Fields.F_TEXT_C)
			 .setColorActive(Fields.B_ACTIVE_C)
			 .getCaptionLabel().setFont(lblFont);
		}
		
		return rb;
	}
	
	/**
	 * Adds a text area to the UI with the given name, text, parent,
	 * width, height, and label font. A text area cannot be directly
	 * modified by the user. Also, by default text areas are scrollable.
	 * 
	 * @param name		The name (or ID) of the UI element, which must be
	 * 					unique amongst all UI elements!
	 * @param iniTxt	The text to be displayed in text area
	 * @param parent	The window group, to which this text area belongs
	 * @param wdh		The width of the text area
	 * @param hgt		The height of the text area
	 * @param lblFont	The font of the text area's text
	 * @return			A reference to the new text area
	 */
	private Textarea addTextarea(String name, String iniTxt, Group parent,
			int wdh, int hgt, PFont lblFont) {
		
		return manager.addTextarea(name, iniTxt, 0, 0, wdh, hgt)
					  .setFont(lblFont)
					  .setColor(Fields.F_TEXT_C)
					  .setColorActive(Fields.F_ACTIVE_C)
					  .setColorBackground(Fields.BG_C)
					  .setColorForeground(Fields.BG_C)
					  .moveTo(parent)
					  .hideScrollbar();
	}
	
	/**
	 * Adds a text-are with the given name, text, parent, xy position, width,
	 * height, text color, background color, and font to the UI.
	 * 
	 * @param name		The name (or ID) of the text-area, which must be unique
	 * 					amongst all UI elements
	 * @param iniTxt	The text rendered in the text-area
	 * @param parent	The window group, to which this text-area belongs
	 * @param posX		The x position of the text-area relative to the
	 * 					position of its parent
	 * @param posY		The y position of the text-area relative to the
	 * 					position of its parent
	 * @param wdh		The width of the text-area
	 * @param hgt		The height of the text-area
	 * @param txtColor	The color of the text-area's text
	 * @param bgColor	The color of the text-area's background
	 * @param lblFont	The font of the text-area
	 * @return
	 */
	private Textarea addTextarea(String name, String iniTxt, Group parent,
			int posX, int posY, int wdh, int hgt, int txtColor, int bgColor,
			PFont lblFont) {
		
		return manager.addTextarea(name, iniTxt, posX, posY, wdh, hgt)
				.setFont(lblFont)
				.setColor(txtColor)
				.setColorActive(Fields.F_ACTIVE_C)
				.setColorBackground(bgColor)
				.setColorForeground(Fields.BG_C)
				.moveTo(parent)
				.hideScrollbar();
	}
	
	/**
	 * Adds a text field to the UI with the given name, parent, width, height,
	 * and text font.
	 * 
	 * @param name		The name (or ID) of the UI element, which must be
	 * 					unique amongst all UI elements!
	 * @param parent	The window group, to which this text field belongs
	 * @param wdh		The width of the text field
	 * @param hgt		The height of the text field
	 * @param lblFont	The text field's font
	 * @return			A reference to the new text field
	 */
	private MyTextfield addTextfield(String name, Group parent, int wdh,
			int hgt, PFont lblFont) {
		
		MyTextfield t = new MyTextfield(manager, name, 0, 0, wdh, hgt);
		t.setColor(Fields.F_TEXT_C)
		 .setColorCursor(Fields.F_CURSOR_C)
		 .setColorActive(Fields.F_CURSOR_C)
		 .setColorLabel(Fields.BG_C)
		 .setColorBackground(Fields.F_BG_C)
		 .setColorForeground(Fields.F_FG_C)
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
		 List<ControllerInterface<?>> contents = manager.getAll();

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
			 PApplet.println("Missing parameter!");
			 NPEx.printStackTrace();
			 wldObj = null;
			 
		 } catch (ClassCastException CCEx) {
			 PApplet.println("Invalid field?");
			 CCEx.printStackTrace();
			 wldObj = null;
			 
		 } catch (IndexOutOfBoundsException IOOBEx) {
			 PApplet.println("Missing field?");
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

		 if (app.getActiveScenario() != null) {
			 ret = app.getActiveScenario().removeWorldObject( getSelectedWO() );
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
		 PVector wpr = RMath.matrixToEuler( active.getLocalOrientationAxes() )
				 			.mult(PConstants.RAD_TO_DEG);
		 
		 pos = RMath.vToWorld(pos);
		 
		 // Fill the current position and orientation fields in the edit window
		 getTextField("XCur").setText( String.format("%4.3f", pos.x) );
		 getTextField("YCur").setText( String.format("%4.3f", pos.y) );
		 getTextField("ZCur").setText( String.format("%4.3f", pos.z) );
		 getTextField("WCur").setText( String.format("%4.3f", -wpr.x) );
		 getTextField("PCur").setText( String.format("%4.3f", wpr.z) );
		 getTextField("RCur").setText( String.format("%4.3f", -wpr.y) );
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
			 PVector wpr = RMath.matrixToEuler( p.getDefaultOrientationAxes() )
					 			.mult(PConstants.RAD_TO_DEG);
			 
			 pos = RMath.vToWorld(pos);
			 
			 // Fill the default position and orientation fields in the edit window
			 getTextField("XCur").setText( String.format("%4.3f", pos.x) );
			 getTextField("YCur").setText( String.format("%4.3f", pos.y) );
			 getTextField("ZCur").setText( String.format("%4.3f", pos.z) );
			 getTextField("WCur").setText( String.format("%4.3f", -wpr.x) );
			 getTextField("PCur").setText( String.format("%4.3f", wpr.z) );
			 getTextField("RCur").setText( String.format("%4.3f", -wpr.y) );
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
			 PVector wpr = RMath.matrixToEuler( active.getLocalOrientationAxes() )
					 			.mult(PConstants.RAD_TO_DEG);
			 
			 pos = RMath.vToWorld(pos);
			 
			 // Fill the default position and orientation fields in the edit window
			 getTextArea("XDef").setText( String.format("%4.3f", pos.x) );
			 getTextArea("YDef").setText( String.format("%4.3f", pos.y) );
			 getTextArea("ZDef").setText( String.format("%4.3f", pos.z) );
			 getTextArea("WDef").setText( String.format("%4.3f", -wpr.x) );
			 getTextArea("PDef").setText( String.format("%4.3f", wpr.z) );
			 getTextArea("RDef").setText( String.format("%4.3f", -wpr.y) );
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
			 PVector wpr = RMath.matrixToEuler( p.getDefaultOrientationAxes() )
					 			.mult(PConstants.RAD_TO_DEG);
			 
			 pos = RMath.vToWorld(pos);
			 
			 // Fill the default position and orientation fields in the edit window
			 getTextArea("XDef").setText( String.format("%4.3f", pos.x) );
			 getTextArea("YDef").setText( String.format("%4.3f", pos.y) );
			 getTextArea("ZDef").setText( String.format("%4.3f", pos.z) );
			 getTextArea("WDef").setText( String.format("%4.3f", -wpr.x) );
			 getTextArea("PDef").setText( String.format("%4.3f", wpr.z) );
			 getTextArea("RDef").setText( String.format("%4.3f", -wpr.y) );
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
				 System.err.printf("Invalid class type: %d!\n", val.getClass());
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
				 dimensions[0] = PApplet.max(10, PApplet.min(val, 800f));
			 }

			 if (hgtField != null && !hgtField.equals("")) {
				 // Read height input
				 float val = Float.parseFloat(hgtField);

				 if (val <= 0) {
					 throw new NumberFormatException("Invalid height value!");
				 }
				 // Height cap of 800
				 dimensions[1] = PApplet.max(10, PApplet.min(val, 800f));
			 }

			 if (wdhField != null && !wdhField.equals("")) {
				 // Read Width input
				 float val = Float.parseFloat(wdhField);

				 if (val <= 0) {
					 throw new NumberFormatException("Invalid width value!");
				 }
				 // Width cap of 800
				 dimensions[2] = PApplet.max(10, PApplet.min(val, 800f));
			 }

			 return dimensions;

		 } catch (NumberFormatException NFEx) {
			 PApplet.println("Invalid number input!");
			 return null;

		 } catch (NullPointerException NPEx) {
			 PApplet.println("Missing parameter!");
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
	 private MyButton getButton(String name) throws ClassCastException {
		 return (MyButton) manager.get(name);
	 }
	 
	 /**
	  * Parses a six-element float array from the six orientation input
	  * text-fields in the edit window UI elements. Each value is validated as
	  * a floating-point number and clamped within the bounds [-9999, 9999]. If
	  * the input for a field is blank or null, then its value in the array
	  * will also be null. However, if the input for any of the fields is not a
	  * real number, then a null array is returned. The first three elements of
	  * the array correspond to an xyz position input and the last three
	  * correspond to a wpr euler angle rotation set in degrees. Both of these
	  * values are assumed to correspond to a position and orientation with
	  * respect to the world frame 
	  * 
	  * @return	A list of input values from the orientation text-fields in the
	  * 		world object edit window
	  */
	 private Float[] getCurrentValues() {
		 try {
			 /* Pull from x, y, z, w, p, r, input values from their
			  * corresponding text-fields */
			 String[] orienVals = new String[] {
					getTextField("XCur").getText(), getTextField("YCur").getText(),
					getTextField("ZCur").getText(), getTextField("WCur").getText(),
					getTextField("PCur").getText(), getTextField("RCur").getText()
			 };
			 
			 // Null indicates an uninitialized field
			 Float[] values = new Float[] { null, null, null, null, null, null };
			 
			 for (int valIdx = 0; valIdx < orienVals.length; ++valIdx) {
				// Update the orientation value
				 if (orienVals[valIdx] != null && !orienVals[valIdx].equals("")) {
					 float val = Float.parseFloat(orienVals[valIdx]);
					 // Bring value within the range [-9999, 9999]
					 val = PApplet.max(-9999f, PApplet.min(val, 9999f));
					 values[valIdx] = val;
				 }
			 }

			 return values;

		 } catch (NumberFormatException NFEx) {
			 PApplet.println("Invalid number input!");
			 return null;

		 } catch (NullPointerException NPEx) {
			 PApplet.println("Missing parameter!");
			 return null;
		 }
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
				 dimensions[0] = PApplet.max(5, PApplet.min(val, 800f));
			 }

			 if (hgtField != null && !hgtField.equals("")) {
				 // Read height input
				 float val = Float.parseFloat(hgtField);

				 if (val <= 0) {
					 throw new NumberFormatException("Invalid height value!");
				 }
				 // Height cap of 9999
				 dimensions[1] = PApplet.max(10, PApplet.min(val, 800f));
			 }

			 return dimensions;

		 } catch (NumberFormatException NFEx) {
			 PApplet.println("Invalid number input!");
			 return null;

		 } catch (NullPointerException NPEx) {
			 PApplet.println("Missing parameter!");
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
			 return ( (MyTextfield) manager.get("Dim2") ).getText();
			 
		 } else if (t == DimType.HEIGHT) {
			 return ( (MyTextfield) manager.get("Dim1") ).getText();
			 
		 } if (t == DimType.SCALE) {
			 int dimNum = 0;
			
			 if (menu == WindowTab.CREATE) {
				// Different text field in the create window tab
				dimNum = 1;
			 }
			 
			 return ( (MyTextfield) manager.get( String.format("Dim%d", dimNum) ) ).getText();
			 
		 } else {
			 return ( (MyTextfield) manager.get("Dim0") ).getText();
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
		 return (MyDropdownList) manager.get(name);
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
				 dimensions[0] = PApplet.min(val, 50f);
			 }

			 return dimensions;

		 } catch (NumberFormatException NFEx) {
			 PApplet.println(NFEx.getMessage());
			 return null;

		 } catch (NullPointerException NPEx) {
			 PApplet.println("Missing parameter!");
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
	 * Get the pendant display text-area with the given index. If the given
	 * index is equal to the number of existing text-areas (i.e. 1 index out of
	 * bounds), then more text-areas to the list of text-ares to accommodate
	 * the given index.
	 * 
	 * @param TAIdx	The index of a pendant display text-area
	 * @return		The text area with the given index
	 */
	private Textarea getPendantDisplayTA(int TAIdx) {
		
		if (TAIdx >= 0 && TAIdx <= displayLines.size()) {
			
			if (TAIdx == displayLines.size()) {
				// Increase the number of text areas used for output
				int newSize = 3 * displayLines.size() / 2;
				
				for (int idx = displayLines.size(); idx < newSize; ++idx) {
					displayLines.add( idx, addTextarea(String.format("ps%d", idx),
							"\0", pendantWindow, 0, 0, 10, 20, Fields.UI_DARK_C,
							Fields.UI_LIGHT_C, Fields.medium) );
				}
			}
			
			return displayLines.get(TAIdx).show();
		}
		
		// Invalid input
		return null;
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
	 public MyRadioButton getRadioButton(String name) throws ClassCastException {
		 return (MyRadioButton) manager.get(name);
	 }
	 
	 /**
	  * @return	Whether or not the robot display button is on
	  */
	 public boolean getRobotButtonState() {
		 return getButton("ToggleRobot").isOn();
	 }
	 
	 /**
	  * Parses the name of a .stl model source file from one of two input
	  * fields: a text-field or dropdown list. The one used is dependent on
	  * which of the two fields that the user edited last
	  * (i.e.e lastModImport).
	  * 
	  * @return	The name of the .stl file to use as a model for a world object
	  */
	 private String getShapeSourceFile() {
		String filename = null;
		 
		if (menu == WindowTab.CREATE) {
			/* Determine which method of the source file input was edited last
			 * and use that input method as the source file */
			ControllerInterface<?> c = manager.get(lastModImport);
			
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
		 return (Textarea) manager.get(name);
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
		 return (MyTextfield) manager.get(name);
	 }
	 
	/*
	 * Hides all the text areas related to the pendant's main display.
	 */
	public void hidePendantScreen() {
		for (Textarea t : displayLines) {
			t.hide();
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
		 if (menu == WindowTab.SCENARIO) {
			 String name = getTextField("ScenarioName").getText();

			 if (name != null) {
				 // Names only consist of letters and numbers
				 if (Pattern.matches("[a-zA-Z0-9]+", name)) {

					 for (Scenario s : app.getScenarios()) {
						 if (s.getName().equals(name)) {
							 // Duplicate name
							 PApplet.println("Names must be unique!");
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
		 List<ControllerInterface<?>> controllers = manager.getAll();
		 
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
		 List<ControllerInterface<?>> controllers = manager.getAll();
		 
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
	 private <T> float[] relativePosition(ControllerInterface<T> obj, RelativePoint pos, float offsetX, float offsetY) {
		 float[] relPosition = new float[] { 0f, 0f };
		 float[] objPosition = obj.getPosition();
		 float[] objDimensions;

		 if (obj instanceof Group) {
			 // getHeight() does not function the same for Group objects for some reason ...
			 objDimensions = new float[] { obj.getWidth(), ((Group)obj).getBackgroundHeight() };
			 
		 } else if (obj instanceof DropdownList) {
			 // Ignore the number of items displayed by the DropdownList, when it is open
			 objDimensions = new float[] { obj.getWidth(), ((DropdownList)obj).getBarHeight() };
			 
		 } else if (obj instanceof MyRadioButton) {
			MyRadioButton rb = (MyRadioButton)obj;
			
			objDimensions = new float[] { obj.getWidth(), rb.getTotalHeight() };
			 
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
	 * Renders all the text on the pendant's screen based on the given input.
	 * 
	 * @param header	The header of the pendant screen
	 * @param contents	The main content fields
	 * @param options	The option fields
	 * @param funcLbls	The function button labels
	 */
	public void renderPendantScreen(String header, MenuScroll contents,
			MenuScroll options, String[] funcLbls) {
		
		Textarea headerLbl = getTextArea("header");

		if (header != null) {
			// Display header field
			headerLbl.setText(header).show();
			
		} else {
			headerLbl.hide();
		}
		
		hidePendantScreen();

		if (contents.size() == 0) {
			options.setLocation(10, 20);
			options.setMaxDisplay(8);
			
		} else {
			options.setLocation(10, 199);
			options.setMaxDisplay(3);
		}
		
		/* Keep track of the pendant display text-field indexes last used by
		 * each menu. */
		int lastTAIdx = renderMenu(contents, 0);
		lastTAIdx = renderMenu(options, lastTAIdx);

		// Set the labels for each function button
		for (int i = 0; i < 5; i += 1) {
			getTextArea("fl" + i).setText(funcLbls[i]);
		}
	}
	
	/**
	 * Renders the text and text highlighting for the given menu. The TAIdx
	 * field indicates the index of the first text-field, in the list of
	 * pendant display text-fields, to use for rendering the menu. In addition,
	 * the index of the next unused text-field is returned by this method.
	 * 
	 * @param menu	The menu contents to display on the pendant
	 * @param TAIdx	The index of the first text-field in displayLines to use
	 * 				for rendering the contents of menu.
	 * @return		The index of the next unused text-field in displayLines
	 */
	public int renderMenu(MenuScroll menu, int TAIdx) {
		ScreenMode m = app.getMode();
		DisplayLine active;
		boolean selectMode = false;
		
		if(m.getType() == ScreenType.TYPE_LINE_SELECT) { selectMode = true; } 
		
		menu.updateRenderIndices();
		active = menu.getActiveLine();
		
		int lineNo = 0;
		int bg, txt, selectInd = -1;
		int next_py = menu.getYPos();
		
		for(int i = menu.getRenderStart(); i < menu.size() && lineNo < menu.getMaxDisplay(); i += 1) {
			//get current line
			DisplayLine temp = menu.get(i);
			int next_px = temp.getxAlign() + menu.getXPos();

			if(i == 0 || menu.get(i - 1).getItemIdx() != menu.get(i).getItemIdx()) {
				selectInd = menu.get(i).getItemIdx();
				
				if (active != null && active.getItemIdx() == selectInd) {
					bg = Fields.UI_DARK_C;
					
				} else {
					bg = Fields.UI_LIGHT_C;
				}
				
				//leading row select indicator []
				getPendantDisplayTA(TAIdx++).setText("")
									   .setPosition(next_px, next_py)
									   .setSize(10, 20)
									   .setColorBackground(bg);
			}

			next_px += 10;
			
			//draw each element in current line
			for(int j = 0; j < temp.size(); j += 1) {
				if(i == menu.getLineIdx()) {
					if(j == menu.getColumnIdx() && !selectMode){
						//highlight selected row + column
						txt = Fields.UI_LIGHT_C;
						bg = Fields.UI_DARK_C;          
					} 
					else if(selectMode && menu.isSelected(temp.getItemIdx())) {
						//highlight selected line
						txt = Fields.UI_LIGHT_C;
						bg = Fields.UI_DARK_C;
					}
					else {
						txt = Fields.UI_DARK_C;
						bg = Fields.UI_LIGHT_C;
					}
				} else if(selectMode && menu.isSelected(temp.getItemIdx())) {
					//highlight any currently selected lines
					txt = Fields.UI_LIGHT_C;
					bg = Fields.UI_DARK_C;
				} else {
					//display normal row
					txt = Fields.UI_DARK_C;
					bg = Fields.UI_LIGHT_C;
				}

				//grey text for comment also this
				if(temp.size() > 0 && temp.get(0).contains("//")) {
					txt = app.color(127);
				}

				getPendantDisplayTA(TAIdx++).setText(temp.get(j))
									   .setPosition(next_px, next_py)
									   .setSize(temp.get(j).length()*Fields.CHAR_WDTH + Fields.TXT_PAD, 20)
									   .setColorValue(txt)
									   .setColorBackground(bg);

				next_px += temp.get(j).length()*Fields.CHAR_WDTH + (Fields.TXT_PAD - 8);
			} //end draw line elements

			//Trailing row select indicator []
			if(i == menu.size() - 1 || menu.get(i).getItemIdx() != menu.get(i + 1).getItemIdx()) {
				
				if (active != null && active.getItemIdx() == selectInd) {
					txt = Fields.UI_DARK_C;
					
				} else {
					txt = Fields.UI_LIGHT_C;
				}
				
				getPendantDisplayTA(TAIdx++).setText("")
									   .setPosition(next_px, next_py)
									   .setSize(10, 20)
									   .setColorBackground(txt);
			}

			next_py += 20;
			lineNo += 1;
		}//end display contents
		
		return TAIdx;
	}
	 
	/**
	 * Resets the background color of all the jog buttons.
	 */
	public void resetJogButtons() {
		for (int idx = 1; idx <= 6; idx += 1) {
			updateButtonBgColor( String.format("joint%d_pos", idx) , false);
			updateButtonBgColor( String.format("joint%d_neg", idx) , false);
		}
	}

	 /**
	  * Reset the base label of every dropdown list.
	  */
	 private void resetListLabels() {
		 List<ControllerInterface<?>> controllers = manager.getAll();
		 
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
	  * Only update the group visibility if it does not
	  * match the given visibility flag.
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
	 * Updates the background color of the button with the given name based off
	 * the given state (i.e. true is active, false is inactive).
	 * 
	 * @param name	The name of the button of which to update the background
	 * @param state	The indicator of what color to set as the button's background
	 */
	private void updateButtonBgColor(String name, boolean state) {
		MyButton b = getButton(name);
		// Set the color of the button's background based off its state
		if (state) {
			b.setColorBackground(Fields.B_ACTIVE_C);
			
		} else {
			b.setColorBackground(Fields.B_DEFAULT_C);
		}
	}

	 /**
	  * Updates the positions of all the contents of the world object creation window.
	  */
	 private void updateCreateWindowContentPositions() {
		 updateDimLblsAndFields();

		 // Object Type dropdown list and label
		 float[] relPos = new float[] { offsetX, offsetX };
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

		 relPos = relativePosition(c, RelativePoint.TOP_RIGHT, distLblToFieldX, PApplet.abs(fieldHeight - dropItemHeight) / 2);
		 getDropdown("Shape").setPosition(relPos[0], relPos[1]);
		 // Dimension label and fields
		 relPos = relativePosition(c, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		 relPos = updateDimLblAndFieldPositions(relPos[0], relPos[1]);

		 // Fill color label and dropdown
		 c = getTextArea("FillLbl").setPosition(relPos[0], relPos[1]);

		 relPos = relativePosition(c, RelativePoint.TOP_RIGHT, distLblToFieldX, PApplet.abs(fieldHeight - dropItemHeight) / 2);
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
			 relPos = relativePosition(c, RelativePoint.TOP_RIGHT, distLblToFieldX, PApplet.abs(fieldHeight - dropItemHeight) / 2);

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
		 background.setBackgroundHeight( (int)Math.ceil( relPos[1] ) )
		 .setHeight( (int)Math.ceil( relPos[1] ) )
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
	 private float[] updateDimLblAndFieldPositions(float initialXPos, float initialYPos) {
		 float[] relPos = new float[] { initialXPos, initialYPos };
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
		 float[] relPos = new float[] { offsetX, offsetX };
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
			 
			 relPos =  new float[] { offsetX, ((int)c0.getPosition()[1]) + c0.getHeight() + distBtwFieldsY };
			 c = getTextArea("RefLbl").setPosition(relPos[0], relPos[1]).show();
			
			 relPos = relativePosition(c, RelativePoint.TOP_RIGHT, distLblToFieldX,
					PApplet.abs(fieldHeight - dropItemHeight) / 2);
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
		 background.setBackgroundHeight( (int)Math.ceil( relPos[1] ) )
		 .setHeight( (int)Math.ceil( relPos[1] ) )
		 .show();
	 }
	 
	 /**
	  * Updates the background color of the robot jog buttons corresponding to
	  * the given pair index based on the given direction value. The are six
	  * pairs of jog buttons, where each pair corresponds to a distinct robot
	  * joint and axis motion.
	  * 
	  * Pair index		Motion
	  * 0		->		joint 1 and x-axis translational
	  * 1		->		joint 2 and y-axis translational
	  * 2		->		joint 3 and z-axis translational
	  * 3		->		joint 4 and x-axis rotational
	  * 4		->		joint 5 and y-axis rotation
	  * 5		->		joint 6 and z axis rotation
	  * 
	  * One button in a pair represent positive motion and the other negative
	  * motion. Only one button in a pair is active at one time, so if the
	  * inactive button is activated, then the active button becomes inactive.
	  * If neither are active, then the inactive button, which is pressed
	  * becomes active. If a button in a pair is active, then its background
	  * color becomes B_ACTIVE_C, otherwise it is B_DEFAULT_C.
	  * 
	  * The sign of the newDir parameter defines the direction of motion
	  * corresponding to a button pair.
	  * 
	  * If newDir > 0, then the positive button becomes active
	  * If newDir == 0, then both buttons become inactive
	  * If newDir < 0, then the negative button becomes active
	  * 
	  * 
	  * @param pair		The index of a jog button pair
	  * @param newDir	The new motion direction
	  */
	 public void updateJogButtons(int pair, float newDir) {
		// Positive jog button is active when the direction is positive
		updateButtonBgColor(String.format("joint%d_pos", pair + 1), newDir > 0);
		// Negative jog button is active when the direction is negative
		updateButtonBgColor(String.format("joint%d_neg", pair + 1), newDir < 0);
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
				 
		 if (app.getActiveScenario() != null) {
			 dropdown = getDropdown("Object");
			 dropdown.clear();
			 
			 MyDropdownList limbo = getDropdown("Fixture");
			 limbo.clear();
			 limbo.addItem("None", null);
			 
			 for (WorldObject wldObj : app.getActiveScenario()) {
				 dropdown.addItem(wldObj.toString(), wldObj);

				 if (wldObj instanceof Fixture) {
					 // Load all fixtures from the active scenario
					 limbo.addItem(wldObj.toString(), wldObj);
				 }
			 }
		 }

		 dropdown = getDropdown("Scenario");
		 dropdown.clear();
		 
		 ArrayList<Scenario> scenarios = app.getScenarios();
		 
		 for (int idx = 0; idx < scenarios.size(); ++idx) {
			 // Load all scenario indices
			 Scenario s = scenarios.get(idx);
			 dropdown.addItem(s.getName(), s);
		 }
	 }
	 
	 /**
	  * Updates the scenarios list based on the scenario edit window.
	  * 
	  * In the new subwindow, the user can create a new screnario, which will
	  * be added to the list of scenarios and set active.
	  * In the Load subwindow, the user can set an inactive scenario as active.
	  * In the rename subwindow, the user can rename an existing scenario.
	  * 
	  * The return value describes the result of the scenario list
	  * modification. A negative value indicates that an error occurred.
	  * 
	  * @param scenarios	The current list of scenarios
	  * @return				 0	An existing scenario is successfully renamed,
	  * 					 1	A new scenario is successfully added to the
	  * 					 	list of scenarios,
	  * 					 2	An existing scenario is successfully renamed,
	  * 					-1	The name for a new scenario is invalid,
	  * 					-2	A new scenario failed to be created,
	  * 					-3	No scenario is selected to be renamed,
	  * 					-4	The replacement name for a scenario is invalid
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
		float[] relPos = new float[] { offsetX, offsetX };
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
			ta.setText("Select the scenario you wish to rename from the dropdown list and enter the new name into the text field below. Press RENAME to confirm the scenario's new name.");
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
			ta.setText("Select the scenario you wish to set as active from the dropdown list. Press LOAD to confirm your choice.");
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
		background.setBackgroundHeight( (int)Math.ceil( relPos[1] ) )
				  .setHeight( (int)Math.ceil( relPos[1] ) )
				  .show();
	 }
	 
	/**
	 * Update the color of the shift button based off the given state value
	 * (i.e. true is active, false is inactive).
	 * 
	 * @param state	The state of the shift button
	 */
	public void updateShiftButton(boolean state) {
		updateButtonBgColor("shift", state);
		updateWindowDisplay();
	}
	
	/**
	 * Update the color of the set button based off the given state value (i.e.
	 * true is active, false is inactive).
	 * 
	 * @param state	The state of the step button
	 */
	public void updateStepButton(boolean state) {
		updateButtonBgColor("step", state);
		updateWindowDisplay();
	 }
	 
	/**
	 * Updates the positions of all the contents of the miscellaneous window.
	 */
	private void updateMiscWindowContentPositions() {
		// Axes Display label
		float[] relPos = new float[] { offsetX, offsetX };
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
			
		} else {
			b.setLabel("Hide OBBs");
		}
		updateButtonBgColor(b.getName(), b.isOn());
		
		// Second robot toggle button
		relPos = relativePosition(b, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		b = getButton("ToggleRobot").setPosition(relPos[0], relPos[1]);
		
		// Update button color based on the state of the button
		updateButtonBgColor(b.getName(), b.isOn());
	
		// Update window background display
		relPos = relativePosition(b, RelativePoint.BOTTOM_LEFT, 0, distBtwFieldsY);
		background.setBackgroundHeight( (int)Math.ceil( relPos[1] ) )
		.setHeight( (int)Math.ceil( relPos[1] ) )
		.show();
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
		 float[] relPos = relativePosition(windowTabs, RelativePoint.BOTTOM_RIGHT, offsetX, 0);

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
			 // Hide all windows
			 setGroupVisible(pendantWindow, false);
			 setGroupVisible(limbo, false);
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
			 
			 if (!pendantWindow.isVisible()) {
				 setGroupVisible(pendantWindow, true);
				 setGroupVisible(limbo, true);
				 
				 updateWindowContentsPositions();
			 }

		 } else if (menu == WindowTab.CREATE) {
			 // Show world object creation window
			 setGroupVisible(pendantWindow, false);
			 setGroupVisible(limbo, false);
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
			 setGroupVisible(pendantWindow, false);
			 setGroupVisible(limbo, false);
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
			 setGroupVisible(pendantWindow, false);
			 setGroupVisible(limbo, false);
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
			 setGroupVisible(pendantWindow, false);
			 setGroupVisible(limbo, false);
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
		 
		 manager.draw();
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
				 PApplet.println("Cannot edit an object currently being held by the Robot!");
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
				 PVector oPosition = RMath.vToWorld( toEdit.getLocalCenter() ),
						 oWPR = RMath.matrixToEuler(toEdit.getLocalOrientationAxes()).mult(PConstants.RAD_TO_DEG);
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
					 oWPR.y = -inputValues[5];
					 edited = true;
				 }
				 
				 if (inputValues[4] != null) {
					 oWPR.z = inputValues[4];
					 edited = true;
				 }

				 // Convert values from the World to the Native coordinate system
				 PVector position = RMath.vFromWorld( oPosition );
				 PVector wpr = oWPR.mult(PConstants.DEG_TO_RAD);
				 RMatrix orientation = RMath.eulerToMatrix(wpr);
				 // Update the Objects position and orientation
				 toEdit.setLocalCenter(position);
				 toEdit.setLocalOrientationAxes(orientation);
				 
			 } catch (NullPointerException NPEx) {
				 PApplet.println("Missing parameter!");
				 NPEx.printStackTrace();
				 return false;
			 }
			 
		 } else {
			 PApplet.println("No object selected!");
		 }

		 /* If the edited object is a fixture, then update the orientation
		  * of all parts, which reference this fixture, in this scenario. */
		 if (toEdit instanceof Fixture) {
			 if (app.getActiveScenario() != null) {

				 for (WorldObject wldObj : app.getActiveScenario()) {
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
			PVector defaultPos = RMath.vToWorld( p.getDefaultCenter() );
			PVector defaultWPR = RMath.matrixToEuler( p.getDefaultOrientationAxes() )
									  .mult(PConstants.RAD_TO_DEG);
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
				 defaultWPR.y = -inputValues[5];
				 edited = true;
			 }
			 
			 if (inputValues[4] != null) {
				 defaultWPR.z = inputValues[4];
				 edited = true;
			 }

			 // Convert values from the World to the Native coordinate system
			 PVector position = RMath.vFromWorld( defaultPos );
			 PVector wpr = defaultWPR.mult(PConstants.DEG_TO_RAD);
			 RMatrix orientation = RMath.eulerToMatrix(wpr);
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
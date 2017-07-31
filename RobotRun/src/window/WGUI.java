package window;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import camera.CamSelectArea;
import camera.RobotCamera;
import controlP5.Background;
import controlP5.Button;
import controlP5.ControlEvent;
import controlP5.ControlListener;
import controlP5.ControlP5;
import controlP5.ControllerInterface;
import controlP5.DropdownList;
import controlP5.Group;
import controlP5.Pointer;
import controlP5.Textarea;
import controlP5.Toggle;
import core.RobotRun;
import enums.Alignment;
import enums.AxesDisplay;
import enums.WindowTab;
import geom.CameraObject;
import geom.ComplexShape;
import geom.DimType;
import geom.Fixture;
import geom.Part;
import geom.RBox;
import geom.RCylinder;
import geom.RMatrix;
import geom.RShape;
import geom.Scenario;
import geom.WorldObject;
import global.DataManagement;
import global.Fields;
import global.RMath;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;
import robot.RoboticArm;
import screen.Screen;
import screen.select_lines.ST_ScreenLineSelect;
import ui.DisplayLine;
import ui.DropdownSearch;
import ui.KeyCodeMap;
import ui.KeyDownBehavior;
import ui.KeyDownMgmt;
import ui.MenuScroll;
import ui.MyButton;
import ui.MyButtonBar;
import ui.MyDropdownList;
import ui.MyRadioButton;
import ui.MySlider;
import ui.MyTextfield;
import ui.UIInputElement;
import undo.PartUndoDefault;
import undo.PartUndoFixRef;
import undo.WOUndoCurrent;
import undo.WOUndoDim;
import undo.WOUndoState;

public class WGUI implements ControlListener {

	/** Standard dimension values (length, width, displacement, etc.) used to
	 *  position a window tab's visible elements. */
	public static final int winMargin = 10,
			radioDim = 16,
			distBtwFieldsY = 15,
			distLblToFieldX = 5,
			distFieldToFieldX = 20,
			lLblWidth = 120,
			mLblWidth = 86,
			sLblWidth = 60,
			fieldHeight = 20,
			fieldWidthMed = 110,
			fieldWidthSm = 70,
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
	
	private static final String[] tabs = { "Hide", "Robot1", "Robot2", "Create", 
										  "Edit", "Scenario", "Camera", "Misc" };
	
	/** The manager object, which contains all the UI elements. */
	private final ControlP5 manager;
	
	/** A reference to the application, in which the UI resides. */
	private final RobotRun app;
	
	/** The current state of the window tabs, which determines what window tab
	 *  is rendered. */
	private WindowTab menu;
	
	/** A group, which defines a set of elements belonging to a window tab, or
	 *  shared amongst the window tabs. */
	private final Group pendant, createWO, editWO, editWOPos, editWOOther,
		sharedElements, scenario, camera, miscellaneous;
	
	/** The button bar controlling the window tab selection. */
	private final MyButtonBar windowTabs;
	
	/** The background shared amongst all windows */
	private final Background background;
	
	/** A cached set of text-areas used to display the pendant contents and
	 *  options output. */
	private final ArrayList<Textarea> displayLines;
	
	/**
	 * Creates a UI with the given RobotRun reference and button images.
	 * 
	 * @param appRef		A reference to the RobotRun class
	 * @param buttonImages	Sets of images used for certain buttons in the UI
	 */
	public WGUI(RobotRun appRef, PImage[][] buttonImages) {
		app = appRef;
		
		manager = new ControlP5(appRef);
		// Explicitly draw the ControlP5 elements
		manager.setAutoDraw(false);
		manager.addListener(this);

		menu = null;
		displayLines = new ArrayList<>();

		/* A local reference to a position in the UI [x, y] used to position UI
		 * elements relative to other UI elements */
		int[] relPos = new int[] { 0, 0 };
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
		windowTabs.setBehavior( new KeyDownMgmt(app.getKeyCodeMap()) );

		// Initialize the shared window background
		relPos = getAbsPosFrom(windowTabs, Alignment.BOTTOM_LEFT, 0, 0);
		background = manager.addBackground("WindowBackground")
				.setPosition(relPos[0], relPos[1])
				.setBackgroundColor(Fields.BG_C)
				.setSize(windowTabs.getWidth(), 0);

		// Initialize the window groups
		pendant = addGroup("PENDANT", relPos[0], relPos[1], Fields.PENDANT_WIDTH, Fields.PENDANT_HEIGHT);
		createWO = addGroup("CREATEWO", relPos[0], relPos[1], windowTabs.getWidth(), 0);
		editWO = addGroup("EDITWO", relPos[0], relPos[1], windowTabs.getWidth(), 0);
		editWOPos = addGroup("EDITWOPOS", editWO, 0, 0, windowTabs.getWidth(), 0);
		editWOOther = addGroup("EDITWOOTHER", editWO, 0, 0, windowTabs.getWidth(), 0);
		scenario = addGroup("SCENARIO", relPos[0], relPos[1], windowTabs.getWidth(), 0);
		camera = addGroup("CAMERA", relPos[0], relPos[1], windowTabs.getWidth(), 0);
		miscellaneous = addGroup("MISC", relPos[0], relPos[1], windowTabs.getWidth(), 0);
		sharedElements = addGroup("SHARED", relPos[0], relPos[1], windowTabs.getWidth(), 0);

		// Initialize camera view buttons
		addButton(WGUI_Buttons.CamViewDef, "D", sButtonWidth, sButtonHeight, Fields.small).hide();
		addButton(WGUI_Buttons.CamViewFr, "F", sButtonWidth, sButtonHeight, Fields.small).hide();
		addButton(WGUI_Buttons.CamViewBk, "Bk", sButtonWidth, sButtonHeight, Fields.small).hide();
		addButton(WGUI_Buttons.CamViewLt, "L", sButtonWidth, sButtonHeight, Fields.small).hide();
		addButton(WGUI_Buttons.CamViewRt, "R", sButtonWidth, sButtonHeight, Fields.small).hide();
		addButton(WGUI_Buttons.CamViewTp, "T", sButtonWidth, sButtonHeight, Fields.small).hide();
		addButton(WGUI_Buttons.CamViewBt, "Bt", sButtonWidth, sButtonHeight, Fields.small).hide();

		// Pendant screen background
		c1 = addTextarea("pendantScreen", "", pendant, winMargin, 0,
				Fields.PENDANT_SCREEN_WIDTH, Fields.PENDANT_SCREEN_HEIGHT,
				Fields.B_TEXT_C, Fields.UI_LIGHT_C, Fields.small);
		
		// Pendant header
		addTextarea("header", "\0", pendant, winMargin,	0,
				Fields.PENDANT_SCREEN_WIDTH, 20, Fields.UI_LIGHT_C,
				Fields.UI_DARK_C, Fields.medium);

		// Start with 25 text-areas for pendant output
		for (int idx = 0; idx < 25; ++idx) {
			displayLines.add( addTextarea(String.format("ps%d", idx), "\0",
					pendant, 10, 0, 10, 20, Fields.UI_DARK_C,
					Fields.UI_LIGHT_C, Fields.medium) );
		}

		// Function button labels
		for (int i = 0; i < 5; i += 1) {
			// Calculate the position of each function label
			int posX = Fields.PENDANT_SCREEN_WIDTH * i / 5 + 15;
			int posY = Fields.PENDANT_SCREEN_HEIGHT - Fields.PENDANT_Y;

			addTextarea("fl" + i, "\0", pendant, posX, posY,
					Fields.PENDANT_SCREEN_WIDTH / 5 - 5, 20, 0, Fields.UI_LIGHT_C,
					Fields.small);
		}

		// Function buttons

		relPos = getAbsPosFrom(c1, Alignment.BOTTOM_LEFT, 0, 2);
		c1 = addButton(WGUI_Buttons.F1, "F1", pendant, relPos[0], relPos[1],
				Fields.PENDANT_SCREEN_WIDTH / 5 - 1, Fields.LARGE_BUTTON, Fields.bond);

		relPos = getAbsPosFrom(c1, Alignment.TOP_RIGHT, 1, 0);
		c2 = addButton(WGUI_Buttons.F2, "F2", pendant, relPos[0], relPos[1],
				Fields.PENDANT_SCREEN_WIDTH / 5 - 1, Fields.LARGE_BUTTON, Fields.bond);

		relPos = getAbsPosFrom(c2, Alignment.TOP_RIGHT, 1, 0);
		c2 = addButton(WGUI_Buttons.F3, "F3", pendant, relPos[0], relPos[1],
				Fields.PENDANT_SCREEN_WIDTH / 5 - 1, Fields.LARGE_BUTTON, Fields.bond);

		relPos = getAbsPosFrom(c2, Alignment.TOP_RIGHT, 1, 0);
		c2 = addButton(WGUI_Buttons.F4, "F4", pendant, relPos[0], relPos[1],
				Fields.PENDANT_SCREEN_WIDTH / 5 - 1, Fields.LARGE_BUTTON, Fields.bond);

		relPos = getAbsPosFrom(c2, Alignment.TOP_RIGHT, 1, 0);
		c2 = addButton(WGUI_Buttons.F5, "F5", pendant, relPos[0], relPos[1],
				Fields.PENDANT_SCREEN_WIDTH / 5 - 1, Fields.LARGE_BUTTON, Fields.bond);


		// Step button
		relPos = getAbsPosFrom(c1, Alignment.BOTTOM_LEFT, 0, 11);
		c1 = addButton(WGUI_Buttons.Step, "STEP", pendant, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.LARGE_BUTTON, Fields.bond);

		// Menu button
		relPos = getAbsPosFrom(c1, Alignment.TOP_RIGHT, 19, 0);
		c1 = addButton(WGUI_Buttons.Menu, "MENU", pendant, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

		// Previous button
		int smLrDiff = Fields.LARGE_BUTTON - Fields.SMALL_BUTTON;
		relPos = getAbsPosFrom(c1, Alignment.BOTTOM_LEFT, 0,	smLrDiff +
				16);
		addButton(WGUI_Buttons.Prev, "PREV", pendant, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

		// Select button
		relPos = getAbsPosFrom(c1, Alignment.TOP_RIGHT, 15, 0);
		c2 = addButton(WGUI_Buttons.Selct, "SELECT", pendant, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

		// Edit button
		relPos = getAbsPosFrom(c2, Alignment.TOP_RIGHT, 1, 0);
		c2 = addButton(WGUI_Buttons.Edit, "EDIT", pendant, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

		// Data button
		relPos = getAbsPosFrom(c2, Alignment.TOP_RIGHT, 1, 0);
		c2 = addButton(WGUI_Buttons.Data, "DATA", pendant, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

		// Function-Control button
		relPos = getAbsPosFrom(c2, Alignment.TOP_RIGHT, 15, 0);
		c2 = addButton(WGUI_Buttons.Funct, "FCTN", pendant, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

		// Next button
		relPos = getAbsPosFrom(c2, Alignment.BOTTOM_LEFT, 0, smLrDiff +
				16);
		addButton(WGUI_Buttons.Next, "NEXT", pendant, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

		// Shift button
		relPos = getAbsPosFrom(c2, Alignment.TOP_RIGHT, 19, 0);
		addButton(WGUI_Buttons.Shift, "SHIFT", pendant, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.LARGE_BUTTON, Fields.bond);

		// Arrow buttons

		relPos = getAbsPosFrom(getButton(WGUI_Buttons.Edit), Alignment.BOTTOM_LEFT,
				smLrDiff / 2, 11);
		c2 = addButton(WGUI_Buttons.ArrowUp, pendant, buttonImages[0], relPos[0],
				relPos[1], Fields.SMALL_BUTTON, Fields.SMALL_BUTTON);

		relPos = getAbsPosFrom(c2, Alignment.BOTTOM_LEFT, 0, 1);
		c2 = addButton(WGUI_Buttons.ArrowDn, pendant, buttonImages[1], relPos[0],
				relPos[1], Fields.SMALL_BUTTON, Fields.SMALL_BUTTON);

		relPos = getAbsPosFrom(getButton(WGUI_Buttons.Selct), Alignment.BOTTOM_LEFT,
				smLrDiff / 2, smLrDiff + 16);
		addButton(WGUI_Buttons.ArrowLt, pendant, buttonImages[2], relPos[0],
				relPos[1], Fields.SMALL_BUTTON, Fields.SMALL_BUTTON);

		relPos = getAbsPosFrom(getButton(WGUI_Buttons.Data), Alignment.BOTTOM_LEFT,
				smLrDiff / 2, smLrDiff + 16);
		addButton(WGUI_Buttons.ArrowRt, pendant, buttonImages[3], relPos[0],
				relPos[1], Fields.SMALL_BUTTON, Fields.SMALL_BUTTON);


		// Reset button column

		int btmColsY = (int)Math.ceil(c2.getPosition()[1]) + c2.getWidth() + 11;
		int resPosX = (int)Math.ceil(getButton(WGUI_Buttons.Step).getPosition()[0]);
		c1 = addButton(WGUI_Buttons.Reset, "RESET", pendant, resPosX, btmColsY,
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

		relPos = getAbsPosFrom(c1, Alignment.BOTTOM_LEFT, 0, 1);
		c2 = addButton(WGUI_Buttons.Num7, "7", pendant, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

		relPos = getAbsPosFrom(c2, Alignment.BOTTOM_LEFT, 0, 1);
		c2 = addButton(WGUI_Buttons.Num4, "4", pendant, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

		relPos = getAbsPosFrom(c2, Alignment.BOTTOM_LEFT, 0, 1);
		c2 = addButton(WGUI_Buttons.Num1, "1", pendant, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

		relPos = getAbsPosFrom(c2, Alignment.BOTTOM_LEFT, 0, 1);
		c2 = addButton(WGUI_Buttons.Num0, "0", pendant, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

		relPos = getAbsPosFrom(c2, Alignment.BOTTOM_LEFT, 0, 1);
		c2 = addButton(WGUI_Buttons.Dash, "-", pendant, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);


		// Backspace button column

		relPos = getAbsPosFrom(c1, Alignment.TOP_RIGHT, 1, 0);
		c1 = addButton(WGUI_Buttons.Bkspc, "BKSPC", pendant, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

		relPos = getAbsPosFrom(c1, Alignment.BOTTOM_LEFT, 0, 1);
		c2 = addButton(WGUI_Buttons.Num8, "8", pendant, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

		relPos = getAbsPosFrom(c2, Alignment.BOTTOM_LEFT, 0, 1);
		c2 = addButton(WGUI_Buttons.Num5, "5", pendant, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

		relPos = getAbsPosFrom(c2, Alignment.BOTTOM_LEFT, 0, 1);
		c2 = addButton(WGUI_Buttons.Num2, "2", pendant, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

		relPos = getAbsPosFrom(c2, Alignment.BOTTOM_LEFT, 0, 1);
		c2 = addButton(WGUI_Buttons.Period, ".", pendant, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

		relPos = getAbsPosFrom(c2, Alignment.BOTTOM_LEFT, 0, 1);
		c2 = addButton(WGUI_Buttons.Posn, "POSN", pendant, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);


		// Item button column

		relPos = getAbsPosFrom(c1, Alignment.TOP_RIGHT, 1, 0);
		c1 = addButton(WGUI_Buttons.Item, "ITEM", pendant, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

		relPos = getAbsPosFrom(c1, Alignment.BOTTOM_LEFT, 0, 1);
		c2 = addButton(WGUI_Buttons.Num9, "9", pendant, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

		relPos = getAbsPosFrom(c2, Alignment.BOTTOM_LEFT, 0, 1);
		c2 = addButton(WGUI_Buttons.Num6, "6", pendant, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

		relPos = getAbsPosFrom(c2, Alignment.BOTTOM_LEFT, 0, 1);
		c2 = addButton(WGUI_Buttons.Num3, "3", pendant, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

		relPos = getAbsPosFrom(c2, Alignment.BOTTOM_LEFT, 0, 1);
		c2 = addButton(WGUI_Buttons.Comma, ",", pendant, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

		relPos = getAbsPosFrom(c2, Alignment.BOTTOM_LEFT, 0, 1);
		c2 = addButton(WGUI_Buttons.IO, "I/O", pendant, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);


		// Util button column

		relPos = getAbsPosFrom(getButton(WGUI_Buttons.ArrowDn), Alignment.BOTTOM_LEFT,
				-smLrDiff / 2, 10);
		c1 = addButton(WGUI_Buttons.Enter, "ENTER", pendant, relPos[0], btmColsY,
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

		relPos = getAbsPosFrom(c1, Alignment.BOTTOM_LEFT, 0, 1);
		c2 = addButton(WGUI_Buttons.Tool1, "TOOL1", pendant, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

		relPos = getAbsPosFrom(c2, Alignment.BOTTOM_LEFT, 0, 1);
		c2 = addButton(WGUI_Buttons.Tool2, "TOOL2", pendant, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

		relPos = getAbsPosFrom(c2, Alignment.BOTTOM_LEFT, 0, 1);
		c2 = addButton(WGUI_Buttons.Mvmu, "MVMU", pendant, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

		relPos = getAbsPosFrom(c2, Alignment.BOTTOM_LEFT, 0, 1);
		c2 = addButton(WGUI_Buttons.Setup, "SETUP", pendant, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

		relPos = getAbsPosFrom(c2, Alignment.BOTTOM_LEFT, 0, 1);
		c2 = addButton(WGUI_Buttons.Stat, "STATUS", pendant, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);


		// Jog button columns

		c1 = getButton(WGUI_Buttons.Shift);
		relPos = getAbsPosFrom(c1, Alignment.BOTTOM_LEFT, 0, 0);
		relPos[1] = btmColsY;

		int[] relPos2 = getAbsPosFrom(c1, Alignment.BOTTOM_LEFT,
				-Fields.LARGE_BUTTON - 1, 0);
		relPos2[1] = btmColsY;

		for (int idx = 1; idx <= 6; ++idx) {

			int sym = 88 + (idx - 1) % 3;
			String name = String.format(WGUI_Buttons.JointPos[idx-1], idx);
			String format = (idx < 4) ? " +%c\n(J%d)" : "+%cR\n(J%d)";
			String lbl = String.format(format, sym, idx);

			MyButton b = addButton(name, lbl, pendant, relPos[0], relPos[1],
					Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

			b.getCaptionLabel().alignY(PConstants.TOP);
			relPos = getAbsPosFrom(b, Alignment.BOTTOM_LEFT, 0, 1);

			name = String.format(WGUI_Buttons.JointNeg[idx-1], idx);
			format = (idx < 4) ? " -%c\n(J%d)" : "-%cR\n(J%d)";
			lbl = String.format(format, sym, idx);

			b = addButton(name, lbl, pendant, relPos2[0], relPos2[1],
					Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

			b.getCaptionLabel().alignY(PConstants.TOP);
			relPos2 = getAbsPosFrom(b, Alignment.BOTTOM_LEFT, 0, 1);
		}


		// Hold button column

		relPos = getAbsPosFrom(c1, Alignment.BOTTOM_LEFT,
				-2 * (Fields.LARGE_BUTTON + 1), 0);
		relPos[1] = btmColsY;
		c1 = addButton(WGUI_Buttons.Hold, "HOLD", pendant, relPos[0],
				btmColsY, Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

		relPos = getAbsPosFrom(c1, Alignment.BOTTOM_LEFT, 0, 1);
		c2 = addButton(WGUI_Buttons.Fwd, "FWD", pendant, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

		relPos = getAbsPosFrom(c2, Alignment.BOTTOM_LEFT, 0, 1);
		c2 = addButton(WGUI_Buttons.Bwd, "BWD", pendant, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

		relPos = getAbsPosFrom(c2, Alignment.BOTTOM_LEFT, 0, 1);
		c2 = addButton(WGUI_Buttons.Coord, "COORD", pendant, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

		relPos = getAbsPosFrom(c2, Alignment.BOTTOM_LEFT, 0, 1);
		c2 = addButton(WGUI_Buttons.SpdUp, "+%", pendant, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

		relPos = getAbsPosFrom(c2, Alignment.BOTTOM_LEFT, 0, 1);
		c2 = addButton(WGUI_Buttons.SpdDn, "-%", pendant, relPos[0], relPos[1],
				Fields.LARGE_BUTTON, Fields.SMALL_BUTTON, Fields.bond);

		// Initialize the elements shared amongst the create and edit windows
		for (int idx = 0; idx < 3; ++idx) {
			addTextarea(String.format("DimLbl%d", idx), String.format("Dim(%d):", idx),
					sharedElements, mLblWidth, sButtonHeight, Fields.medium);

			addTextfield(String.format("Dim%d", idx), sharedElements, fieldWidthMed,
					fieldHeight, Fields.medium, app.getKeyCodeMap());
		}
		
		addSlider("WOFillR", "Red", sharedElements, fieldWidthMed, fieldHeight, 0f,
				255f, 0, 10f / 255f, 0f, Fields.BLACK, Fields.color(255, 0, 0),
				Fields.B_DEFAULT_C, Fields.color(200, 0, 0), Fields.medium,
				Fields.ITYPE_TRANSIENT);
		addSlider("WOFillG", "Green", sharedElements, fieldWidthMed, fieldHeight, 0f,
				255f, 0, 10f / 255f, 0f, Fields.BLACK, Fields.color(0, 255, 0),
				Fields.B_DEFAULT_C, Fields.color(0, 200, 0), Fields.medium,
				Fields.ITYPE_TRANSIENT);
		addSlider("WOFillB", "Blue", sharedElements, fieldWidthMed, fieldHeight, 0f,
				255f, 0, 10f / 255f, 0f, Fields.BLACK, Fields.color(0, 0, 255),
				Fields.B_DEFAULT_C, Fields.color(0, 0, 200), Fields.medium,
				Fields.ITYPE_TRANSIENT);
		addTextarea("WOFillLbl", "Fill:", sharedElements, mLblWidth, sButtonHeight, Fields.medium);
		
		addSlider("WOOutlineR", "Red", sharedElements, fieldWidthMed, fieldHeight,
				0f, 255f, 0, 10f / 255f, 0f, Fields.BLACK, Fields.color(255, 0, 0),
				Fields.B_DEFAULT_C, Fields.color(200, 0, 0), Fields.medium,
				Fields.ITYPE_TRANSIENT);
		addSlider("WOOutlineG", "Green", sharedElements, fieldWidthMed, fieldHeight,
				0f, 255f, 0, 10f / 256f, 0f, Fields.BLACK, Fields.color(0, 255, 0),
				Fields.B_DEFAULT_C, Fields.color(0, 200, 0), Fields.medium,
				Fields.ITYPE_TRANSIENT);
		addSlider("WOOutlineB", "Blue", sharedElements, fieldWidthMed, fieldHeight,
				0f, 255f, 0, 10f / 256f, 0f, Fields.BLACK, Fields.color(0, 0, 255),
				Fields.B_DEFAULT_C, Fields.color(0, 0, 200), Fields.medium,
				Fields.ITYPE_TRANSIENT);
		addTextarea("WOOutlineLbl", "Outline:", sharedElements, mLblWidth, sButtonHeight, Fields.medium);

		addButton(WGUI_Buttons.ObjClearFields, "Clear", sharedElements, mButtonWidth, sButtonHeight, Fields.small);
		
		// Initialize the world object creation window elements
		addTextarea("WONameLbl", "Name:", createWO, mLblWidth, fieldHeight, Fields.medium);
		addTextfield("WOName", createWO, fieldWidthMed, fieldHeight, Fields.medium, app.getKeyCodeMap());
		addTextarea("WOTypeLbl", "Type:", createWO, mLblWidth, sButtonHeight, Fields.medium);
		
		float[] togValues = new float[] { 0f, 1f };
		String[] togNames = new String[] { "PartOpt", "FixtureOpt" };
		String[] togLbls = new String[] { "Part", "Fixture" };
		
		addRadioButton("WOType", createWO, radioDim, radioDim, Fields.medium,
				togValues, togNames, togLbls, false, Fields.ITYPE_TRANSIENT);
		
		addTextarea("WOShapeLbl", "Shape:", createWO, mLblWidth, sButtonHeight, Fields.medium);
		
		togValues = new float[] { 0f, 1f, 2f };
		togNames = new String[] { "BoxOpt", "CylinderOpt", "ImportOpt" };
		togLbls = new String[] { "Box", "Cylinder", "Import" };
		
		addRadioButton("Shape", createWO, radioDim, radioDim, Fields.medium,
				togValues, togNames, togLbls, false, Fields.ITYPE_TRANSIENT);
		
		addTextarea("WOFillSmp", "\0", createWO, sButtonHeight, sButtonHeight, Fields.medium);
		addTextarea("WOOutlineSmp", "\0", createWO, sButtonHeight, sButtonHeight, Fields.medium);
		addButton(WGUI_Buttons.ObjCreate, "Create", createWO, mButtonWidth, sButtonHeight, Fields.small);

		// Initialize the world object edit window elements
		addTextarea("WOEditLbl", "Object:", editWO, mLblWidth, fieldHeight, Fields.medium);

		togValues = new float[] { 0f, 1f };
		togNames = new String[] { "PositionOpt", "EditOpt" };
		togLbls = new String[] { "Position", "Edit" };
		
		addTextarea("EditTabLbl", "Options:", editWO, mLblWidth, fieldHeight,
				Fields.medium);
		MyRadioButton rb = addRadioButton("EditTab", editWO, radioDim, radioDim,
				Fields.medium, togValues, togNames, togLbls, false,
				Fields.ITYPE_PERMENANT);
		rb.setItemsPerRow(2);
		rb.setSpacingColumnOffset(distFieldToFieldX);
		
		addTextarea("Blank", "Inputs", editWOPos, lLblWidth, fieldHeight, Fields.medium);
		addTextarea("Current", "Current", editWOPos, fieldWidthMed, fieldHeight, Fields.medium);
		addTextarea("Default", "Default", editWOPos, fieldWidthMed, fieldHeight, Fields.medium);

		addTextarea("XLbl", "X Position:", editWOPos, lLblWidth, fieldHeight, Fields.medium);
		addTextfield("XCur", editWOPos, fieldWidthMed, fieldHeight, Fields.medium, app.getKeyCodeMap());
		addTextarea("XDef", "N/A", editWOPos, fieldWidthMed, fieldHeight, Fields.medium);

		addTextarea("YLbl", "Y Position:", editWOPos, lLblWidth, fieldHeight, Fields.medium);
		addTextfield("YCur", editWOPos, fieldWidthMed, fieldHeight, Fields.medium, app.getKeyCodeMap());
		addTextarea("YDef", "N/A", editWOPos, fieldWidthMed, fieldHeight, Fields.medium);

		addTextarea("ZLbl", "Z Position:", editWOPos, lLblWidth, fieldHeight, Fields.medium);
		addTextfield("ZCur", editWOPos, fieldWidthMed, fieldHeight, Fields.medium, app.getKeyCodeMap());
		addTextarea("ZDef", "N/A", editWOPos, fieldWidthMed, fieldHeight, Fields.medium);

		addTextarea("WLbl", "X Rotation:", editWOPos, lLblWidth, fieldHeight, Fields.medium);
		addTextfield("WCur", editWOPos, fieldWidthMed, fieldHeight, Fields.medium, app.getKeyCodeMap());
		addTextarea("WDef", "N/A", editWOPos, fieldWidthMed, fieldHeight, Fields.medium);

		addTextarea("PLbl", "Y Rotation:", editWOPos, lLblWidth, fieldHeight, Fields.medium);
		addTextfield("PCur", editWOPos, fieldWidthMed, fieldHeight, Fields.medium, app.getKeyCodeMap());
		addTextarea("PDef", "N/A", editWOPos, fieldWidthMed, fieldHeight, Fields.medium);

		addTextarea("RLbl", "Z Rotation:", editWOPos, lLblWidth, fieldHeight, Fields.medium);
		addTextfield("RCur", editWOPos, fieldWidthMed, fieldHeight, Fields.medium, app.getKeyCodeMap());
		addTextarea("RDef", "N/A", editWOPos, fieldWidthMed, fieldHeight, Fields.medium);

		addTextarea("RefLbl", "Reference:", editWOPos, lLblWidth, sButtonHeight, Fields.medium);

		addButton(WGUI_Buttons.ObjMoveToCur, "Move to Current", editWOPos, fieldWidthMed, sButtonHeight, Fields.small);
		addButton(WGUI_Buttons.ObjSetDefault, "Update Default", editWOPos, fieldWidthMed, sButtonHeight, Fields.small);
		addButton(WGUI_Buttons.ObjMoveToDefault, "Move to Default", editWOPos, fieldWidthMed, sButtonHeight, Fields.small);

		addButton(WGUI_Buttons.ObjResetDefault, "Restore Defaults", editWOPos, lLblWidth, sButtonHeight, Fields.small);
		
		addButton(WGUI_Buttons.ObjConfirmDims, "Confirm", editWOOther, mButtonWidth, sButtonHeight, Fields.small);
		addButton(WGUI_Buttons.ObjDelete, "Delete", editWOOther, mButtonWidth, sButtonHeight, Fields.small);

		// Initialize the scenario window elements
		addTextarea("SOptLbl", "Options:", scenario, mLblWidth, fieldHeight, Fields.medium);
		
		togValues = new float[] { 0f, 1f, 2f };
		togNames = new String[] { "NewOpt", "LoadOpt", "RenameOpt" };
		togLbls = new String[] { "New", "Load", "Rename" };

		rb = addRadioButton("ScenarioOpt", scenario, radioDim,
				radioDim, Fields.medium, togValues, togNames, togLbls,
				false, Fields.ITYPE_PERMENANT);
		rb.setItemsPerRow(3);
		rb.setSpacingColumnOffset(distFieldToFieldX);

		addTextarea("SInstructions", "N/A", scenario, windowTabs.getWidth() - (2 * winMargin),
				54, Fields.small).hideScrollbar();

		addTextfield("SInput", scenario, fieldWidthMed, fieldHeight, Fields.medium, app.getKeyCodeMap());
		addButton(WGUI_Buttons.ScenarioConfirm, "N/A", scenario, mButtonWidth, sButtonHeight, Fields.small);

		// Initialize the camera window
		addTextarea("CXLbl", "X Position:", camera, lLblWidth, fieldHeight, Fields.medium);
		addTextfield("CXCur", camera, fieldWidthSm, fieldHeight, Fields.medium, app.getKeyCodeMap());

		addTextarea("CYLbl", "Y Position:", camera, lLblWidth, fieldHeight, Fields.medium);
		addTextfield("CYCur", camera, fieldWidthSm, fieldHeight, Fields.medium, app.getKeyCodeMap());

		addTextarea("CZLbl", "Z Position:", camera, lLblWidth, fieldHeight, Fields.medium);
		addTextfield("CZCur", camera, fieldWidthSm, fieldHeight, Fields.medium, app.getKeyCodeMap());

		addTextarea("CWLbl", "X Rotation:", camera, lLblWidth, fieldHeight, Fields.medium);
		addTextfield("CWCur", camera, fieldWidthSm, fieldHeight, Fields.medium, app.getKeyCodeMap());

		addTextarea("CPLbl", "Y Rotation:", camera, lLblWidth, fieldHeight, Fields.medium);
		addTextfield("CPCur", camera, fieldWidthSm, fieldHeight, Fields.medium, app.getKeyCodeMap());

		addTextarea("CRLbl", "Z Rotation:", camera, lLblWidth, fieldHeight, Fields.medium);
		addTextfield("CRCur", camera, fieldWidthSm, fieldHeight, Fields.medium, app.getKeyCodeMap());
		
		addTextarea("CFOVLbl", "FOV:", camera, lLblWidth, fieldHeight, Fields.medium);
		addTextfield("CFOVCur", camera, fieldWidthSm, fieldHeight, Fields.medium, app.getKeyCodeMap());
		
		addTextarea("CAspectLbl", "Aspect Ratio:", camera, lLblWidth, fieldHeight, Fields.medium);
		addTextfield("CAspectCur", camera, fieldWidthSm, fieldHeight, Fields.medium, app.getKeyCodeMap());
		
		addTextarea("CCNearLbl", "Near Clip:", camera, lLblWidth, fieldHeight, Fields.medium);
		addTextfield("CCNearCur", camera, fieldWidthSm, fieldHeight, Fields.medium, app.getKeyCodeMap());
		
		addTextarea("CCFarLbl", "Far Clip:", camera, lLblWidth, fieldHeight, Fields.medium);
		addTextfield("CCFarCur", camera, fieldWidthSm, fieldHeight, Fields.medium, app.getKeyCodeMap());
		
		addSlider("CBright", camera, fieldWidthMed, fieldHeight, 0f, 10f, 1f,
				Fields.ITYPE_TRANSIENT);
		addSlider("CExp", camera, fieldWidthMed, fieldHeight, 0.01f, 1f, 0.1f,
				Fields.ITYPE_TRANSIENT);
		
		addButton(WGUI_Buttons.CamUpdate, "Update Camera", camera, fieldWidthMed, sButtonHeight, Fields.small);
		addDropdown("CamObjects", camera, ldropItemWidth, dropItemHeight, 0,
				Fields.small, Fields.ITYPE_PERMENANT);
		addTextarea("ObjPreviewLbl", "Recorded object:", camera, 150, fieldHeight, Fields.medium);
		addButton(WGUI_Buttons.CamObjPreview, "ObjPreview", camera, 150, 200, Fields.small);
		addTextarea("SnapPreviewLbl", "Camera view:", camera, 250, fieldHeight, Fields.medium);
		addButton(WGUI_Buttons.CamSnapPreview, "SnapPreview", camera, 250, 200, Fields.small);
		addButton(WGUI_Buttons.CamTeachObj, "Teach Object", camera, fieldWidthMed, sButtonHeight, Fields.small);
		addButton(WGUI_Buttons.CamDeleteObj, "Remove Object", camera, fieldWidthMed, sButtonHeight, Fields.small);
		
		// Initialize the miscellaneous window elements
		addTextarea("ActiveRobotEE", "EE:", miscellaneous, lLblWidth, sButtonHeight, Fields.medium);
		addTextarea("ActiveAxesDisplay", "Axes Display:", miscellaneous, lLblWidth, sButtonHeight, Fields.medium);

		addButton(WGUI_Buttons.ObjToggleBounds, "Hide OBBs", miscellaneous, mdropItemWidth, sButtonHeight, Fields.small);
		addButton(WGUI_Buttons.RobotToggleActive, "Add Robot", miscellaneous, mdropItemWidth, sButtonHeight, Fields.small);
		addButton(WGUI_Buttons.CamToggleActive, "Enable RCam", miscellaneous, mdropItemWidth, sButtonHeight, Fields.small);
		addButton(WGUI_Buttons.RobotToggleTrace, "Enable Trace", miscellaneous, mdropItemWidth, sButtonHeight, Fields.small);
		addButton(WGUI_Buttons.RobotClearTrace, "Clear Trace", miscellaneous, mdropItemWidth, sButtonHeight, Fields.small);
		
		togValues = new float[] { 0f, 1f };
		togNames = new String[] { "RenderMouseRayOpt", "RenderPointOpt" };
		togLbls = new String[] { "Render Mouse Ray", "Render Point" };
		rb = addRadioButton("DebugOptions", miscellaneous, radioDim,
				radioDim, Fields.medium, togValues, togNames, togLbls,
				true, Fields.ITYPE_PERMENANT);
		
		addToggle("TestOpt", "Test", miscellaneous, radioDim, radioDim,
				Fields.medium, false);
		
		if(app.isRCamEnable()) {
			getButton(WGUI_Buttons.CamToggleActive).setSwitch(true);
			getButton(WGUI_Buttons.CamToggleActive).setOn();
			getButton(WGUI_Buttons.CamToggleActive).setSwitch(false);
			System.out.println(getButton(WGUI_Buttons.CamToggleActive).isOn());
		}
		
		/* Initialize dropdown list elements
		 * 
		 * NOTE: the order in which the dropdown lists matters!
		 * 		(Adding the dropdown lists last places them in front of the
		 * other UI elements, which is important, when the list is open) */
		MyDropdownList ddlLimbo = addDropdown("AxesDisplay", miscellaneous, ldropItemWidth,
				dropItemHeight, 3, Fields.small, Fields.ITYPE_PERMENANT);
		ddlLimbo.addItem(AxesDisplay.AXES.name(), AxesDisplay.AXES)
		.addItem(AxesDisplay.GRID.name(), AxesDisplay.GRID)
		.addItem(AxesDisplay.NONE.name(), AxesDisplay.NONE)
		.setValue(0f);

		ddlLimbo = addDropdown("RobotEE", miscellaneous, ldropItemWidth,
				dropItemHeight, 4, Fields.small, Fields.ITYPE_PERMENANT);
		ddlLimbo.addItem("FACEPLATE", 0)
		.addItem("SUCTION", 1)
		.addItem("GRIPPER", 2)
		.addItem("POINTER", 3)
		.addItem("GLUE GUN", 4)
		.addItem("WIELDER", 5)
		.setValue(0f);
		
		addDropdown("Scenario", scenario, ldropItemWidth, dropItemHeight, 4,
				Fields.small, Fields.ITYPE_TRANSIENT);
		addDropdown("Fixture", editWOPos, ldropItemWidth, dropItemHeight, 4,
				Fields.small, Fields.ITYPE_TRANSIENT);

		for (int idx = 0; idx < 1; ++idx) {
			// dimension field dropdown lists
			addDropdownSearch(String.format("DimDdl%d", idx), sharedElements, ldropItemWidth,
					dropItemHeight, 4, Fields.small, Fields.ITYPE_TRANSIENT);
		}

		addDropdown("WO", editWO, ldropItemWidth, dropItemHeight, 4,
				Fields.small, Fields.ITYPE_TRANSIENT);
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
	 * @param inputType	How should this field by treated for input clear events
	 * @return			A reference to the new dropdown list
	 */
	private MyDropdownList addDropdown(String name, Group parent, int lblWdh,
			int lblHgt, int listLen, PFont lblFont, int inputType) {

		MyDropdownList dropdown = new MyDropdownList(manager, name, inputType);

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
	 * @param inputType	How should this field by treated for input clear events
	 * @return			A reference to the new dropdown list
	 */
	private MyDropdownList addDropdownSearch(String name, Group parent, int lblWdh,
			int lblHgt, int listLen, PFont lblFont, int inputType) {

		MyDropdownList dropdown = new DropdownSearch(manager, name, inputType);

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
	private Group addGroup(String name, int posX, int posY, int wdh, int hgt) {
		return manager.addGroup(name).setPosition(posX, posY)
				.setBackgroundColor(Fields.BG_C)
				.setSize(wdh, hgt)
				.hideBar();
	}
	
	private Group addGroup(String name, Group parent, int posX, int posY, int wdh, int hgt) {
		return manager.addGroup(name).setPosition(posX, posY)
				.setBackgroundColor(Fields.BG_C)
				.setSize(wdh, hgt)
				.moveTo(parent)
				.hideBar();
	}

	/**
	 * Adds a new radio button to the UI with the given name, parent, toggle
	 * dimensions, toggle label font, list of toggles, and the value of the
	 * initially active toggle.
	 * 
	 * NOTE:	the three toggle arrays (togValues, togNames, and togLbls) must
	 * 			be of equal length!
	 * 
	 * @param name		The name (or ID) of the UI element, which must be
	 * 					unique amongst all UI elements!
	 * @param parent	The window group, to which this radio button belongs
	 * @param togWdh	The width of a toggle element
	 * @param togHgt	The height of a toggle element
	 * @param lblFont	The font for the labels of the toggle elements
	 * @param togValues	The values associated with each toggle
	 * @param togNames	The names associated with each toggle (must be unique
	 * 					amongst all UI elements)
	 * @param togLbls	The labels associated with each toggle
	 * @param multi		Is this radio button set mutliple choice?
	 * @param inputType	How should this field by treated for input clear events
	 * @return			A reference to the new radio button
	 */
	private MyRadioButton addRadioButton(String name, Group parent, int togWdh,
			int togHgt, PFont lblFont, float[] togValues, String[] togNames,
			String[] togLbls, boolean multi, int inputType) {

		MyRadioButton rb = new MyRadioButton(manager, name, multi, inputType);
		rb.setColorValue(Fields.B_DEFAULT_C)
		.setColorLabel(Fields.F_TEXT_C)
		.setColorActive(Fields.B_ACTIVE_C)
		.setBackgroundColor(Fields.BG_C)
		.setNoneSelectedAllowed(false)
		.moveTo(parent)
		.setSize(togWdh, togHgt);
		
		// Add toggle elements
		for (int tdx = 0; tdx < togValues.length; ++tdx) {
			rb.addItem(togNames[tdx], togValues[tdx]);
			// Reinitialize select toggle fields
			Toggle t = rb.getItem(tdx);
			t
			.setLabel(togLbls[tdx])
			.setColorBackground(Fields.B_DEFAULT_C)
			.setColorLabel(Fields.F_TEXT_C)
			.setColorActive(Fields.B_ACTIVE_C)
			.getCaptionLabel().setFont(lblFont);
		}
		
		return rb;
	}

	private MySlider addSlider(String name, Group parent, int wdh, int hgt,
			float min, float max, float def, int inputType) {
		
		MySlider s = new MySlider(manager, name, inputType);
		s.setColorValue(Fields.B_DEFAULT_C)
		.setColorLabel(Fields.F_TEXT_C)
		.setColorActive(Fields.B_ACTIVE_C)
		.setRange(min, max)
		.setDefaultValue(def)
		.moveTo(parent)
		.setSize(wdh, hgt);
		
		return s;
	}
	
	private MySlider addSlider(String name, String lbl, Group parent, int wdh,
			int hgt, float min, float max, int percision,
			float scrollSensitivity, float def, int valColor, int actColor,
			int bgColor, int fgColor, PFont lblFont, int inputType) {
		
		MySlider s = new MySlider(manager, name, inputType);
		s.getCaptionLabel().set(lbl).setFont(lblFont);
		
		s.setColorValue(valColor)
		.setColorLabel(Fields.F_TEXT_C)
		.setColorActive(actColor)
		.setColorBackground(bgColor)
		.setColorForeground(fgColor)
		.setRange(min, max)
		.setDecimalPrecision(percision)
		.setDefaultValue(def)
		.setSize(wdh, hgt)
		.setScrollSensitivity(scrollSensitivity)
		.moveTo(parent);
		
		return s;
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
	 * @return			A reference to the new text-area
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
	 * @param keys		A reference to the key code map shared amongst many UI
	 * 					elements and the RobotRun PApplet
	 * @return			A reference to the new text field
	 */
	private MyTextfield addTextfield(String name, Group parent, int wdh,
			int hgt, PFont lblFont, KeyCodeMap keys) {

		MyTextfield t = new MyTextfield(manager, name, 0, 0, wdh, hgt,
				Fields.ITYPE_TRANSIENT);
		t.setColor(Fields.F_TEXT_C)
		.setColorCursor(Fields.F_CURSOR_C)
		.setColorActive(Fields.F_CURSOR_C)
		.setColorBackground(Fields.F_BG_C)
		.setColorForeground(Fields.F_FG_C)
		.moveTo(parent)
		.setFont(lblFont)
		.setBehavior(new KeyDownBehavior(keys));
		
		t.getCaptionLabel().hide();
		
		return t;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param name
	 * @param lbl
	 * @param parent
	 * @param wdh
	 * @param hgt
	 * @param lblFont
	 * @param iniState
	 * @return
	 */
	private Toggle addToggle(String name, String label, Group parent, int wdh,
			int hgt, PFont lblFont, boolean iniState) {
		
		Toggle t = new Toggle(manager, name);
		t.setSize(wdh, hgt)
		.setState(iniState)
		.moveTo(parent)
		.getCaptionLabel().setFont(lblFont)
		.setText(label);
		
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
			Fields.resetMessage();
			
			if (actLbl == null) {
				updateView( null );
				
			} else if (actLbl.equals("Robot1")) {
				updateView( WindowTab.ROBOT1 );

			} else if (actLbl.equals("Robot2")) {
				updateView( WindowTab.ROBOT2 );

			} else if (actLbl.equals("Create")) {
				updateView( WindowTab.CREATE );

			} else if (actLbl.equals("Edit")) {
				updateView( WindowTab.EDIT );

			} else if (actLbl.equals("Scenario")) {
				updateView( WindowTab.SCENARIO );
				
			} else if (actLbl.equals("Camera")) {
				updateView( WindowTab.CAMERA );
				
			} else if (actLbl.equals("Misc")) {
				updateView( WindowTab.MISC );
				
			} else {
				updateView( null );
			}

		} else {
			if (arg0.isFrom("WO") || arg0.isFrom("Shape") ||
					arg0.isFrom("EditTab") || arg0.isFrom("ScenarioOpt") ||
					arg0.isFrom("CamObjects")) {
				/* The selected item in these lists influence the layout of
				 * the menu */
				updateUIContentPositions();
			}

			if (arg0.isFrom("WO")) {
				WorldObject selectedWO = getSelectedWO();
				
				if (selectedWO != null) {
					// Update the input fields on the edit menu
					updateEditWindowFields(selectedWO);
				}

			} else if (arg0.isFrom("Fixture")) {
				WorldObject selectedWO = getSelectedWO();
				
				if (menu == WindowTab.EDIT && selectedWO instanceof Part) {
					// Set the reference of the Part to the currently active fixture
					Part p = (Part)selectedWO;
					Fixture refFixture = (Fixture)getDropdown("Fixture").getSelectedItem();

					if (p.getFixtureRef() != refFixture) {
						/* Update the scenario undo stack for the part's
						 * fixture reference change */
						app.updateScenarioUndo(new PartUndoFixRef(p));
						p.setFixtureRef(refFixture);
					}
				}

			} else if (arg0.isFrom("RobotEE")) {
				RoboticArm r = app.getActiveRobot();

				if (r != null) {
					/* Link the active robot's end effector to the selected
					 * item */
					MyDropdownList ddl = (MyDropdownList)arg0.getController();
					r.setActiveEE( (Integer)ddl.getSelectedItem() );
				}
			} else if (arg0.isFrom("CamObjPreview")) {
				CameraObject o = (CameraObject)getDropdown("CamObjects").getSelectedItem();	
				RMatrix mdlOrient = o.getLocalOrientation();
				Pointer p = getButton(WGUI_Buttons.CamObjPreview).getPointer();
				int x = p.x();
				int y = p.y();
				
				CamSelectArea a = o.getSelectAreaClicked(x, y, mdlOrient);
				
				if(a != null) {
					if(app.mouseButton == PConstants.RIGHT && !a.isIgnored()) {
						a.ignoreArea();
					}
					else if(app.mouseButton == PConstants.LEFT && !a.isEmphasized()) {
						a.emphasizeArea();
					}
					else {
						a.clearArea();
					}
				}
				
				o.updateModelPreview(mdlOrient);
				updateUIContentPositions();
				
			} else if (arg0.isFrom("WOFillR") || arg0.isFrom("WOFillG") ||
					arg0.isFrom("WOFillB")) {
				
				if (menu == WindowTab.CREATE) {
					// Update the sample fill color text area
					Textarea txa = getTextArea("WOFillSmp");
					
					if (txa != null) {
						txa.setColorBackground( getFillColor() );
					}
					
				} else if (menu == WindowTab.EDIT) {
					WorldObject wo = getSelectedWO();
					
					if (wo != null) {
						/* Update the fill color of the selected world object
						 * based on the fill color slider values */
						int newFill = getFillColor();
						wo.getModel().setFillValue(newFill);
					}
				}
				
			} else if (arg0.isFrom("WOOutlineR") || arg0.isFrom("WOOutlineG") ||
					arg0.isFrom("WOOutlineB")) {
				
				if (menu == WindowTab.CREATE) {
					// Update the sample fill color text area
					Textarea txa = getTextArea("WOOutlineSmp");
					
					if (txa != null) {
						txa.setColorBackground( getStrokeColor() );
					}
					
				} else if (menu == WindowTab.EDIT) {
					WorldObject wo = getSelectedWO();
					// Complex shapes have no stroke color
					if (wo != null && !(wo.getModel() instanceof ComplexShape)) {
						/* Update the stroke color of the selected world object
						 * based on the stroke color slider values */
						int newStroke = getStrokeColor();
						wo.getModel().setStrokeValue(newStroke);
					}
				}
				
			} else if (arg0.isFrom("TestOpt")) {
				System.out.printf("%f\n", arg0.getController().getValue());
				
			}
		}
	}

	/**
	 * Reinitializes all input fields (textfields, dropdown lists, etc.).
	 */
	public void clearAllInputFields() {
		clearGroupInputFields(null);
		updateDimLblsAndFields();
	}

	/**
	 * Clears all input fields, which belong to the given group, with the input
	 * type Fields.TRANSIENT. If g is null, then all input fields are checked.
	 * 
	 * @param g	The group of which to clear all child input fields with the
	 * 			transient input type
	 */
	public void clearGroupInputFields(Group g) {
		List<ControllerInterface<?>> contents = manager.getAll();

		for (ControllerInterface<?> controller : contents) {

			if (g == null || controller.getParent().equals(g)) {

				if (controller instanceof UIInputElement) {
					UIInputElement uiInput = (UIInputElement)controller;
					
					if (uiInput.getInputType() == Fields.ITYPE_TRANSIENT) {
						// Clear input field
						uiInput.clearInput();
					}
				}
			}
		}
	}

	/**
	 * Creates a world object form the input fields in the Create window.
	 * 
	 * @return	The new world object or null, if the input was invalid
	 */
	public WorldObject createWorldObject() {
		// Determine if the object to be create is a Fixture or a Part
		float typeVal = getRadioButton("WOType").getValue();

		app.pushMatrix();
		app.resetMatrix();
		WorldObject wldObj = null;

		try {

			if (typeVal == 0.0f) {
				// Create a Part
				String name = getTextField("WOName").getText();
				int typeID = (int)getRadioButton("Shape").getValue();
				int fill = getFillColor();

				switch(typeID) {
				case 0: // Box shape
					int strokeVal = getStrokeColor();
					Float[] shapeDims = getBoxDimensions();
					// Construct a box shape
					if (shapeDims != null && shapeDims[0] != null && shapeDims[1] != null && shapeDims[2] != null) {
						wldObj = new Part(name, fill, strokeVal, shapeDims[0], shapeDims[1], shapeDims[2]);
					}
					break;

				case 1: // Cylinder shape
					strokeVal = getStrokeColor();
					shapeDims = getCylinderDimensions();
					// Construct a cylinder
					if (shapeDims != null && shapeDims[0] != null && shapeDims[1] != null) {
						wldObj = new Part(name, fill, strokeVal, shapeDims[0], shapeDims[1]);
					}
					break;

				case 2: // Complex shape
					String srcFile = getShapeSourceFile();
					shapeDims = getModelDimensions();
					// Construct a complex model
					if (shapeDims != null) {
						ComplexShape shape;

						if (shapeDims[0] != null) {
							// Define shape scale
							shape = new ComplexShape(srcFile, fill, shapeDims[0]);
							
						} else {
							shape = new ComplexShape(srcFile, fill);
						}

						wldObj = new Part(name, shape);
					}
					break;
				default:
				}

			} else if (typeVal == 1.0f) {
				// Create a fixture
				String name = getTextField("WOName").getText();
				int typeID = (int)getRadioButton("Shape").getValue();
				int fill = getFillColor();

				switch(typeID) {
				case 0: // Box shape
					int strokeVal = getStrokeColor();
					Float[] shapeDims = getBoxDimensions();
					// Construct a box shape
					if (shapeDims != null && shapeDims[0] != null && shapeDims[1] != null && shapeDims[2] != null) {
						wldObj = new Fixture(name, fill, strokeVal, shapeDims[0], shapeDims[1], shapeDims[2]);
					}
					break;

				case 1: // Cylinder shape
					strokeVal = getStrokeColor();
					shapeDims = getCylinderDimensions();
					// Construct a cylinder
					if (shapeDims != null && shapeDims[0] != null && shapeDims[1] != null) {
						wldObj = new Fixture(name, fill, strokeVal, shapeDims[0], shapeDims[1]);
					}
					break;

				case 2: // Complex shape
					String srcFile = getShapeSourceFile();
					shapeDims = getModelDimensions();
					// Construct a complex model
					ComplexShape shape;

					if (shapeDims != null && shapeDims[0] != null) {
						// Define model scale value
						shape = new ComplexShape(srcFile, fill, shapeDims[0]);
					} else {
						shape = new ComplexShape(srcFile, fill);
					}

					wldObj = new Fixture(name, shape);
					break;
				default:
				}
			}
			
			if (wldObj == null) {
				Fields.setMessage("Missing field");
			}

		} catch (NullPointerException NPEx) {
			Fields.setMessage("Missing field");
			wldObj = null;

		} catch (ClassCastException CCEx) {
			Fields.setMessage("Invalid field");
			wldObj = null;

		} catch (IndexOutOfBoundsException IOOBEx) {
			Fields.setMessage("Missing field");
			wldObj = null;
			
		} catch (IllegalArgumentException IAEx) {
			Fields.setMessage("Missing field");
			wldObj = null;
		}

		app.popMatrix();

		return wldObj;
	}

	/**
	 * Updates the edit window's position and orientation input fields with
	 * that of the given world object. The outputted values are with respect
	 * to the world frame.
	 * 
	 * @param selected	The world object of which to display the coordinate
	 * 					frame
	 */
	public void fillCurWithCur(WorldObject selected) {
		// Get the part's default position and orientation
		PVector pos = RMath.vToWorld( selected.getLocalCenter() );
		PVector wpr = RMath.nRMatToWEuler( selected.getLocalOrientation() );

		// Fill the current position and orientation fields in the edit window
		getTextField("XCur").setText( String.format("%4.3f", pos.x) );
		getTextField("YCur").setText( String.format("%4.3f", pos.y) );
		getTextField("ZCur").setText( String.format("%4.3f", pos.z) );
		getTextField("WCur").setText( String.format("%4.3f", wpr.x) );
		getTextField("PCur").setText( String.format("%4.3f", wpr.y) );
		getTextField("RCur").setText( String.format("%4.3f", wpr.z) );
	}

	/**
	 * Updates the window's default position and orientation text labels with
	 * that of the current position and orientation of the given part. The
	 * outputted values are with respect to the world frame.
	 * 
	 * @param selected	The part, whose current position and orientation will
	 * 					be displayed
	 */
	public void fillCurWithDef(Part selected) {
		// Get the part's current position and orientation
		PVector pos = RMath.vToWorld( selected.getDefaultCenter() );
		PVector wpr = RMath.nRMatToWEuler( selected.getDefaultOrientation() );

		// Fill the default position and orientation fields in the edit window
		getTextField("XCur").setText( String.format("%4.3f", pos.x) );
		getTextField("YCur").setText( String.format("%4.3f", pos.y) );
		getTextField("ZCur").setText( String.format("%4.3f", pos.z) );
		getTextField("WCur").setText( String.format("%4.3f", wpr.x) );
		getTextField("PCur").setText( String.format("%4.3f", wpr.y) );
		getTextField("RCur").setText( String.format("%4.3f", wpr.z) );
	}

	/**
	 * Updates the window's default position and orientation text labels with
	 * that of the default position and orientation of the given part. The
	 * outputted values are with respect to the world frame.
	 * 
	 * @param selected	The part, whose default position and orientation will
	 * 					be displayed
	 */
	private void fillDefWithDef(Part selected) {
		// Get the part's current position and orientation
		PVector pos = RMath.vToWorld( selected.getDefaultCenter() );
		PVector wpr = RMath.nRMatToWEuler( selected.getDefaultOrientation() );

		// Fill the default position and orientation fields in the edit window
		getTextArea("XDef").setText( String.format("%4.3f", pos.x) );
		getTextArea("YDef").setText( String.format("%4.3f", pos.y) );
		getTextArea("ZDef").setText( String.format("%4.3f", pos.z) );
		getTextArea("WDef").setText( String.format("%4.3f", wpr.x) );
		getTextArea("PDef").setText( String.format("%4.3f", wpr.y) );
		getTextArea("RDef").setText( String.format("%4.3f", wpr.z) );
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
	 * Calculates an absolute position based off the relative position
	 * specified by the given parameters.
	 * 
	 * @param ref		The controller, whose position is used as a reference
	 * @param align	The aligment used for the position calculation with
	 * 				respect to ref
	 * @param offsetX	The x position offset with respect to ref
	 * @param offsetY	The y position offset with respect to ref
	 * @return			A doubleton containing the absolute xy position
	 */
	private static int[] getAbsPosFrom(ControllerInterface<?> ref,
			Alignment align, int offsetX, int offsetY) {

		return getAbsPosFrom(ref, align, offsetX, ref, align, offsetY);
	}

	/**
	 * Calculates an absolute position based off the relative position specified
	 * by the given parameters. The x position is calculated with the alignment,
	 * alignX, with respect to the controller refX, with the offset specified by
	 * offX. Similarly, the y position is calculated with the alignment, alignY,
	 * with respect to the controller refY, with the offset specified by offY. 
	 * 
	 * @param refX		The controller, whose x position is used as a reference
	 * @param alignX	The alignment used for the x position calculation with
	 * 					respect to refX
	 * @param offX		The x position offset with respect refX
	 * @param refY		The controller, whose y position is used as a reference
	 * @param alignY	The alignment used for the y position calculation with
	 * 					respect refY
	 * @param offY		The y position offset with respect to refY
	 * @return			A doubleton containing the absolute xy position
	 */
	private static int[] getAbsPosFrom(ControllerInterface<?> refX,
			Alignment alignX, int offX, ControllerInterface<?> refY,
			Alignment alignY, int offY) {

		return new int[] {
				getAbsPosOnAxis(refX, alignX, offX, 0), 
				getAbsPosOnAxis(refY, alignY, offY, 1)
		};
	}

	/**
	 * Calculates the absolute position from the position of the given
	 * controller and the given alignment offset by the given offset value
	 * along the given axis.
	 * 
	 * @param ref	The controller, whose position is used as a reference
	 * @param align	The alignment used for the position calculation with
	 * 				respect to ref
	 * @param off	The position offset with respect to ref
	 * @param axis	The axis on which to calculate the position (0 -> X, 1 ->
	 * 				Y)
	 * @return		The absolute position along the specified axis
	 */
	private static int getAbsPosOnAxis(ControllerInterface<?> ref,
			Alignment align, int off, int axis) {

		float[] refPos = ref.getPosition();
		int[] refDim = WGUI.getDimOf(ref, 0);
		int pos;

		if (axis == 1) {
			pos = off + (int)(refPos[1] + refDim[1] * align.factorY);

		} else {
			pos = off + (int)(refPos[0] + refDim[0] * align.factorX);
		}

		return pos;
	}

	/**
	 * Returns a post-processed list of the user's input for the dimensions of
	 * the box world object (i.e. length, height, width). Valid values for a
	 * box's dimensions are between 10 and 800, inclusive. If a input is not
	 * valid, then no other inputs are processed and null is returned.
	 * Although, if a field is left blank (i.e. ""), then that field is
	 * ignored. The array of processed input returned contains three Float
	 * objects. If any of the input was ignored, then its corresponding array
	 * element will be null.
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
				dimensions[0] = Float.parseFloat(lenField);
			}

			if (hgtField != null && !hgtField.equals("")) {
				// Read height input
				dimensions[1] = Float.parseFloat(hgtField);
			}

			if (wdhField != null && !wdhField.equals("")) {
				// Read Width input
				dimensions[2] = Float.parseFloat(wdhField);
			}

			return dimensions;

		} catch (NumberFormatException NFEx) {
			Fields.setMessage(NFEx.getMessage());
			return null;

		} catch (NullPointerException NPEx) {
			Fields.setMessage("All dimension fields must have a value");
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
	 * Attempts to find a button with the given name and returns its state.
	 * 
	 * @param name	The name of the button, of which to find the state
	 * @return		The state of the button with the given name
	 */
	public boolean getButtonState(String name) {
		ControllerInterface<?> controller = manager.get(name);
		
		if (controller instanceof MyButton) {
			return ((MyButton) controller).isOn();
		}
		// No button exists with the given name
		return false;
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
	private Float[] getCurrentWOValues() {
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
					values[valIdx] = RMath.clamp(val, -9999f, 9999f);
				}
			}

			return values;

		} catch (NumberFormatException NFEx) {
			Fields.setMessage("All fields must be real numbers");
			return null;

		} catch (NullPointerException NPEx) {
			Fields.setMessage("All fields must be real numbers");
			return null;
		}
	}

	/**
	 * Returns a post-processed list of the user's input for the dimensions of
	 * the cylinder world object (i.e. radius and height). Valid values for a
	 * cylinder's radius are between 5 and 400, inclusive. If a input is valid,
	 * then no other inputs are processed and null is returned. Although, if a
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
				dimensions[0] = Float.parseFloat(radField);
			}

			if (hgtField != null && !hgtField.equals("")) {
				// Read height input
				dimensions[1] = Float.parseFloat(hgtField);
			}

			return dimensions;

		} catch (NumberFormatException NFEx) {
			Fields.setMessage(NFEx.getMessage());
			return null;

		} catch (NullPointerException NPEx) {
			Fields.setMessage("All dimension fields must have a value");
			return null;
		}
	}

	/**
	 * Calculates the absolute dimensions of the given controller. Certain UI
	 * elements such as a dropdown list have several height settings.
	 * 
	 * TODO	implement option feature for controllers such as the radio button
	 * 		sets or dropdown lists
	 * 
	 * @param c		A UI element or controller
	 * @param opt	Not currently implemented
	 * @return		The dimensions of the given controller
	 */
	private static int[] getDimOf(ControllerInterface<?> c, int opt) {
		int[] dims = new int[2];

		if (c instanceof Group) {
			dims[0] = c.getWidth();
			/* getHeight() does not function the same for Group objects for
			 * some reason ... */
			dims[1] = ((Group) c).getBackgroundHeight();

		} else if (c instanceof DropdownList) {
			dims[0] = c.getWidth();
			/* Ignore the number of items displayed by the DropdownList, when
			 * it is open */
			dims[1] = ((DropdownList) c).getBarHeight();

		} else if (c instanceof MyRadioButton) {
			MyRadioButton rb = (MyRadioButton)c;

			dims[0] = rb.getTotalWidth();
			dims[1] = rb.getTotalHeight();

		} else {
			dims[0] = c.getWidth();
			dims[1] = c.getHeight();
		}

		return dims;
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
	 * @param t	A type of world object dimension, which corresponds to a
	 * 			text-field input in the UI
	 * @return		The text-field corresponding to the given dimension type
	 * @throws ClassCastException	Really shouldn't happen
	 */
	private String getDimText(DimType t) throws ClassCastException {

		if (t == DimType.WIDTH) {
			return ( (MyTextfield) manager.get("Dim2") ).getText();

		} else if (t == DimType.HEIGHT) {
			return ( (MyTextfield) manager.get("Dim1") ).getText();

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
	 * Returns a 32-bit color value based on the values of the world object
	 * fill color sliders.
	 * 
	 * @return	a 32-bit color value
	 */
	private int getFillColor() {
		// Pull color components from the color sliders
		MySlider limbo = getSlider("WOFillR");
		int r = (limbo == null) ? 255 : (int)limbo.getValue();
		
		limbo = getSlider("WOFillG");
		int g = (limbo == null) ? 255 : (int)limbo.getValue();
		
		limbo = getSlider("WOFillB");
		int b = (limbo == null) ? 255 : (int)limbo.getValue();
		
		return Fields.color(r, g, b);
	}
	
	/**
	 * @return	The active window
	 */
	public WindowTab getMenu() {
		return menu;
	}

	/**
	 * Returns a post-processed list of the user's input for the dimensions of
	 * the model world object (i.e. scale). The scale of a complex shape must
	 * be a positive number, however its range depends on the model's base
	 * dimensions. Although, if a field is left blank (i.e. ""), then that
	 * field is ignored. The array of processed input returned contains one
	 * Float object. If any of the input was ignored, then its corresponding
	 * array element will be null.
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
				dimensions[0] = Float.parseFloat(sclField);
			}

			return dimensions;

		} catch (NumberFormatException NFEx) {
			Fields.setMessage("The scale must be a real number");
			return null;

		} catch (NullPointerException NPEx) {
			Fields.setMessage("The scale must be a real number");
			return null;
		}
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
							"\0", pendant, 0, 0, 10, 20, Fields.UI_DARK_C,
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
	private MyRadioButton getRadioButton(String name) throws ClassCastException {
		return (MyRadioButton) manager.get(name);
	}

	/**
	 * @return	Whether or not the robot display button is on
	 */
	public boolean getRobotButtonState() {
		return getButton(WGUI_Buttons.RobotToggleActive).isOn();
	}
	
	/**
	 * Returns the scenario associated with the label that is active
	 * for the scenario drop-down list.
	 * 
	 * @return  The index value or null if no such index exists
	 */
	public Scenario getSelectedScenario() {

		if (menu == WindowTab.SCENARIO) {
			Object val = getDropdown("Scenario").getSelectedItem();

			if (val instanceof Scenario) {
				// Set the active scenario index
				return (Scenario)val;

			} else if (val != null) {
				// Invalid entry in the dropdown list
				Fields.setMessage("Invalid class type: %d!\n", val.getClass());
			}
		}

		return null;
	}


	/**
	 * @return	The world object currently selected in the Object dropdown list
	 */
	public WorldObject getSelectedWO() {
		Object wldObj = getDropdown("WO").getSelectedItem();

		if (wldObj instanceof WorldObject) {
			return (WorldObject)wldObj;
		} else {
			return null;
		}
	}
	
	public WorldObject getSelectedCamObj() {
		Object wldObj = getDropdown("CamObjects").getSelectedItem();
		
		if (wldObj instanceof WorldObject) {
			return (WorldObject)wldObj;
		} else {
			return null;
		}
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
		if (menu == WindowTab.CREATE) {
			MyDropdownList ddl = getDropdown("DimDdl0");
			return (String)ddl.getSelectedItem();
		}

		return null;
	}
	
	private MySlider getSlider(String name) {
		return (MySlider) manager.get(name);
	}
	
	/**
	 * Returns a 32-bit color value based on the values of the world object
	 * outline color sliders.
	 * 
	 * @return	a 32-bit color value
	 */
	private int getStrokeColor() {
		// Pull color components from the color sliders
		MySlider limbo = getSlider("WOOutlineR");
		int r = (limbo == null) ? 255 : (int)limbo.getValue();
		
		limbo = getSlider("WOOutlineG");
		int g = (limbo == null) ? 255 : (int)limbo.getValue();
		
		limbo = getSlider("WOOutlineB");
		int b = (limbo == null) ? 255 : (int)limbo.getValue();
		
		return Fields.color(r, g, b);
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
	
	/**
	 * TODO comment this
	 * 
	 * @param name
	 * @return
	 * @throws ClassCastException
	 */
	private Toggle getToggle(String name) throws ClassCastException {
		return (Toggle) manager.get(name);
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
	 * Determines whether a single text field is active.
	 * 
	 * @return	If a text-field is active
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
	 * Was the last mouse interaction with the UI?
	 * 
	 * @return	Is the UI the current focus
	 */
	public boolean isFocus() {
		return menu != null && manager.isMouseOver();
	}

	/**
	 * Determines whether the mouse is over certain UI elements.
	 * 
	 * @return	If the mouse is currently hovering over a UI element
	 */
	public boolean isMouseOverUIElement() {
		List<ControllerInterface<?>> controllers = manager.getAll();

		for (ControllerInterface<?> c : controllers) {
			if ((c instanceof DropdownList || c instanceof MySlider)
					&& c.isMouseOver()) {
				
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
	 * Renders all the text on the pendant's screen based on the given input.
	 * 
	 * @param screen	A reference to the screen to render
	 */
	public void renderPendantScreen(Screen screen) {
		Textarea headerLbl = getTextArea("header");

		if (screen.getHeader() != null) {
			// Display header field
			headerLbl.setText(screen.getHeader()).show();

		} else {
			headerLbl.hide();
		}

		hidePendantScreen();
		
		MenuScroll contents = screen.getContents();
		MenuScroll options = screen.getOptions();

		if (contents.size() == 0) {
			options.setLocation(10, 20);
			options.setMaxDisplay(8);

		} else {
			options.setLocation(10, 199);
			options.setMaxDisplay(3);
		}
		
		boolean[] lnSelectState = null;
		
		if (screen instanceof ST_ScreenLineSelect) {
			lnSelectState = ((ST_ScreenLineSelect)screen).getLineSelectStates();
		}

		/* Keep track of the pendant display text-field indexes last used by
		 * each menu. */
		int lastTAIdx = renderMenu(lnSelectState, contents, 0);
		lastTAIdx = renderMenu(null, options, lastTAIdx);
		
		String[] funcLbls = screen.getLabels();

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
	 * @param lineSelectStates	The list of line states indicating if a line is
	 * 							selected (i.e. should be highlighted)
	 * @param mScroll			The menu contents to display on the pendant
	 * @param TAIdx				The index of the first text-field in
	 * 							displayLines to use for rendering the contents
	 * 							of menu.
	 * @return					The index of the next unused text-field in
	 * 							displayLines
	 */
	private int renderMenu(boolean[] lineSelectStates, MenuScroll mScroll, int TAIdx) {
		DisplayLine active;

		mScroll.updateRenderIndices();
		active = mScroll.getCurrentItem();

		int lineNo = 0;
		int bg, txt, selectInd = -1;
		int next_py = mScroll.getYPos();

		for(int i = mScroll.getRenderStart(); i < mScroll.size() && lineNo < mScroll.getMaxDisplay(); i += 1) {
			//get current line
			DisplayLine temp = mScroll.get(i);
			int next_px = temp.getxAlign() + mScroll.getXPos();

			if(i == 0 || mScroll.get(i - 1).getItemIdx() != mScroll.get(i).getItemIdx()) {
				selectInd = mScroll.get(i).getItemIdx();

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
				if(i == mScroll.getLineIdx()) {
					if(j == mScroll.getColumnIdx() && lineSelectStates == null){
						//highlight selected row + column
						txt = Fields.UI_LIGHT_C;
						bg = Fields.UI_DARK_C;          
					} 
					else if(validateAndGet(lineSelectStates, temp.getItemIdx())) {
						//highlight selected line
						txt = Fields.UI_LIGHT_C;
						bg = Fields.UI_DARK_C;
					}
					else {
						txt = Fields.UI_DARK_C;
						bg = Fields.UI_LIGHT_C;
					}
				} else if(validateAndGet(lineSelectStates, temp.getItemIdx())) {
					/* highlight any currently selected lines a different color
					 * then the active line */
					txt = Fields.UI_LIGHT_C;
					bg = Fields.color(10, 10, 125);
				} else {
					//display normal row
					txt = Fields.UI_DARK_C;
					bg = Fields.UI_LIGHT_C;
				}

				//grey text for comment also this
				if(temp.size() > 0 && temp.get(0).contains("//")) {
					txt = Fields.color(127);
				}

				getPendantDisplayTA(TAIdx++).setText(temp.get(j))
				.setPosition(next_px, next_py)
				.setSize(temp.get(j).length()*Fields.CHAR_WDTH + Fields.TXT_PAD, 20)
				.setColorValue(txt)
				.setColorBackground(bg);

				next_px += temp.get(j).length()*Fields.CHAR_WDTH + (Fields.TXT_PAD - 8);
			} //end draw line elements

			//Trailing row select indicator []
			if(i == mScroll.size() - 1 || mScroll.get(i).getItemIdx() != mScroll.get(i + 1).getItemIdx()) {

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
	 * Convenience method for checking the bounds and getting an element from a
	 * boolean array. If the index is out of bounds, then false is returned.
	 * 
	 * @param arr	The array from which to get an element
	 * @param idx	The index of the element to get
	 * @return		The element at the given index in arr or false, if the
	 * 				index is invalid
	 */
	private static boolean validateAndGet(boolean[] arr, int idx) {
		if (arr != null && idx >= 0 && idx < arr.length) {
			return arr[idx];
		}
		
		return false;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @return
	 */
	public boolean renderMouseRay() {
		MyRadioButton rb = getRadioButton("DebugOptions");
		return rb.getState(0);
	}
	
	/**
	 * TODO comment this
	 * 
	 * @return
	 */
	public boolean renderPoint() {
		MyRadioButton rb = getRadioButton("DebugOptions");
		return rb.getState(1);
	}

	/**
	 * Resets the background color of all the jog buttons.
	 */
	public void resetJogButtons() {
		for (int idx = 1; idx <= 6; idx += 1) {
			updateButtonBgColor(String.format(WGUI_Buttons.JointPos[idx-1], idx), false);
			updateButtonBgColor(String.format(WGUI_Buttons.JointNeg[idx-1], idx), false);
		}
	}

	/**
	 * Only update the group visibility if it does not
	 * match the given visibility flag.
	 */
	private static void setGroupVisible(Group g, boolean setVisible) {
		if (g.isVisible() != setVisible) {
			g.setVisible(setVisible);
		}
	}
	
	/**
	 * Sets selected object in the "WO" drop down menu to the
	 * given WorldObject, 'wo.'
	 * 
	 * @param wo The world object to be set.
	 */
	public void setSelectedWO(WorldObject wo) {
		
		if (wo == null) {
			MyDropdownList objList = getDropdown("WO");
			objList.setValue(-1f);
			
		} else if ((menu == null || menu == WindowTab.EDIT)) {
			updateView(WindowTab.EDIT);
			getDropdown("WO").setItem(wo);
		}
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param name
	 * @param state
	 */
	public void setToggleState(String name, boolean state) {
		try {
			Toggle t = getToggle(name);
			
			if (t != null) {
				t.setState(state);
			}
			
		} catch (ClassCastException CCEx) {
			// Controller with the give name is not a Toggle
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

		// Remove or add the robot2 tab based on the robot toggle button
		Button tr = getButton(WGUI_Buttons.RobotToggleActive);
		
		if(tr.isOn()) {
			tr.setLabel("Remove Robot");
		}
		else {
			tr.setLabel("Add Robot");
		}

		updateWindowTabs();
		return tr.isOn();
	}
	
	public boolean toggleCamera() {
		if (menu == WindowTab.CAMERA) {
			windowTabs.setLabel("Hide");
		}

		// Remove or add the camera tab based on the camera toggle button
		Button tc = getButton(WGUI_Buttons.CamToggleActive);
		
		if(tc.isOn()) {
			tc.setLabel("Disable RCam");
		}
		else {
			tc.setLabel("Enable RCam");
		}

		updateWindowTabs();
		return tc.isOn();
	}
	
	private void updateWindowTabs() {
		windowTabs.clear();
		windowTabs.setItems(WGUI.tabs);
		
		if(!getButton(WGUI_Buttons.RobotToggleActive).isOn()) {
			windowTabs.removeItem("Robot2");
		}
		
		if(!getButton(WGUI_Buttons.CamToggleActive).isOn()) {
			windowTabs.removeItem("Camera");
		}
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
		
		// Name label and field
		int[] relPos = new int[] { winMargin, winMargin };
		ControllerInterface<?> c = getTextArea("WONameLbl").setPosition(relPos[0], relPos[1]);

		relPos = getAbsPosFrom(c, Alignment.TOP_RIGHT, distLblToFieldX, 0);
		getTextField("WOName").setPosition(relPos[0], relPos[1]);
		// Object Type dropdown list and label
		relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
		c = getTextArea("WOTypeLbl").setPosition(relPos[0], relPos[1]);

		relPos = getAbsPosFrom(c, Alignment.TOP_RIGHT, distLblToFieldX, 0);
		ControllerInterface<?> c0 = getRadioButton("WOType").setPosition(relPos[0], relPos[1]);
		// Shape type label and dropdown
		relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, c0,
				Alignment.BOTTOM_LEFT, distBtwFieldsY);
		c = getTextArea("WOShapeLbl").setPosition(relPos[0], relPos[1]);

		relPos = getAbsPosFrom(c, Alignment.TOP_RIGHT, distLblToFieldX, 0);
		c0 = getRadioButton("Shape").setPosition(relPos[0], relPos[1]);
		// Dimension label and fields
		relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, c0,
				Alignment.BOTTOM_LEFT, distBtwFieldsY);
		relPos = updateDimLblAndFieldPositions(relPos[0], relPos[1]);

		// Fill color label and dropdown
		c0 = getTextArea("WOFillLbl").setPosition(relPos[0], relPos[1]);

		relPos = getAbsPosFrom(c0, Alignment.BOTTOM_LEFT, 0, winMargin);
		c = getSlider("WOFillR").setPosition(relPos[0], relPos[1]);
		
		relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, winMargin);
		c = getSlider("WOFillG").setPosition(relPos[0], relPos[1]);
		
		relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, winMargin);
		c = getSlider("WOFillB").setPosition(relPos[0], relPos[1]);
		
		relPos = getAbsPosFrom(c0, Alignment.TOP_RIGHT, distFieldToFieldX, 0);
		c0 = getTextArea("WOFillSmp").setPosition(relPos[0], relPos[1]);
		
		float val = getRadioButton("Shape").getValue();

		if (val == 2f) {
			// No stroke color for Model Shapes
			getTextArea("WOOutlineLbl").hide();
			getTextArea("WOOutlineSmp").hide();
			getSlider("WOOutlineR").hide();
			getSlider("WOOutlineG").hide();
			getSlider("WOOutlineB").hide();

		} else {
			// Outline color labels and sliders
			relPos = getAbsPosFrom(c0, Alignment.TOP_RIGHT, distFieldToFieldX + 40, 0);
			c0 = getTextArea("WOOutlineLbl").setPosition(relPos[0], relPos[1]).show();
			
			relPos = getAbsPosFrom(c0, Alignment.TOP_RIGHT, distFieldToFieldX, 0);
			getTextArea("WOOutlineSmp").setPosition(relPos[0], relPos[1]).show();
			
			relPos = getAbsPosFrom(c0, Alignment.BOTTOM_LEFT, 0, winMargin);
			c0 = getSlider("WOOutlineR").setPosition(relPos[0], relPos[1]).show();
			
			relPos = getAbsPosFrom(c0, Alignment.BOTTOM_LEFT, 0, winMargin);
			c0 = getSlider("WOOutlineG").setPosition(relPos[0], relPos[1]).show();
			
			relPos = getAbsPosFrom(c0, Alignment.BOTTOM_LEFT, 0, winMargin);
			getSlider("WOOutlineB").setPosition(relPos[0], relPos[1]).show();
		}

		// Create button
		relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
		c = getButton(WGUI_Buttons.ObjCreate).setPosition(relPos[0], relPos[1]);
		// Clear button
		relPos = getAbsPosFrom(c, Alignment.TOP_RIGHT, winMargin, 0);
		c = getButton(WGUI_Buttons.ObjClearFields).setPosition(relPos[0], relPos[1]).show();

		// Update window background display
		relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
		background.setPosition(createWO.getPosition())
		.setBackgroundHeight(relPos[1])
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

			relPos = getAbsPosFrom(dimLbl, Alignment.TOP_RIGHT, distLblToFieldX, 0);
			dimDdl.setPosition(relPos[0], relPos[1]);

			relPos = getAbsPosFrom(dimLbl, Alignment.BOTTOM_LEFT, 0, winMargin);
		}

		// Update the dimension text fields
		for (int idx = 0; idx < DIM_TXT; ++idx) {
			MyTextfield dimTxt = getTextField( String.format("Dim%d", idx) );

			if (!dimTxt.isVisible()) { break; }

			Textarea dimLbl = getTextArea( String.format("DimLbl%d", idx + ddlIdx) )
					.setPosition(relPos[0], relPos[1]);

			relPos = getAbsPosFrom(dimLbl, Alignment.TOP_RIGHT, distLblToFieldX, 0);
			dimTxt.setPosition(relPos[0], relPos[1]);
			
			relPos = getAbsPosFrom(dimLbl, Alignment.BOTTOM_LEFT, 0, winMargin);
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
			int typeID = (int)getRadioButton("Shape").getValue();

			// Define the label text and the number of dimensionos fields to display
			if (typeID == 0) {
				lblNames = new String[] { "Length:", "Height:", "Width:" };
				txtFields = 3;

			} else if (typeID == 1) {
				lblNames = new String[] { "Radius:", "Height:" };
				txtFields = 2;

			} else if (typeID == 2) {
				lblNames = new String[] { "Source:", "Scale:", };
				txtFields = 1;
				ddlFields = 1;
			}

		} else if (menu == WindowTab.EDIT) {
			Object val = getDropdown("WO").getSelectedItem();

			if (val instanceof WorldObject) {
				RShape s = ((WorldObject)val).getModel();

				if (s instanceof RBox) {
					lblNames = new String[] { "Length:", "Height:", "Width:" };
					txtFields = 3;

				} else if (s instanceof RCylinder) {
					lblNames = new String[] { "Radius:", "Height:" };
					txtFields = 2;

				} else if (s instanceof ComplexShape) {
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
	 * Updates the edit window's dimension, position, and orientation input
	 * fields with that of the given world object and also updates the
	 * fixture reference dropdown, if the given world object is a part. The
	 * position and orientation values are outputted with respect to the world
	 * frame.
	 * 
	 * @param selected	The world object, whose fields will be displayed in the
	 * 					edit window
	 */
	public void updateEditWindowFields(WorldObject selected) {
		RShape form = selected.getModel();
		
		// Update the dimension fields
		if (form instanceof RBox) {
			getTextField("Dim0").setText( String.format("%4.3f", form.getDim(DimType.LENGTH)) );
			getTextField("Dim1").setText( String.format("%4.3f", form.getDim(DimType.HEIGHT)) );
			getTextField("Dim2").setText( String.format("%4.3f", form.getDim(DimType.WIDTH)) );

		} else if (form instanceof RCylinder) {
			getTextField("Dim0").setText( String.format("%4.3f", form.getDim(DimType.RADIUS)) );
			getTextField("Dim1").setText( String.format("%4.3f", form.getDim(DimType.HEIGHT)) );


		} else if (form instanceof ComplexShape) {
			getTextField("Dim0").setText( String.format("%4.3f", form.getDim(DimType.SCALE)) );
		}
		
		// Update color sliders
		int fillColor = form.getFillValue();
		int[] rgb = Fields.rgba(fillColor);
		getSlider("WOFillR").setValue(rgb[0]);
		getSlider("WOFillG").setValue(rgb[1]);
		getSlider("WOFillB").setValue(rgb[2]);
		
		if (!(form instanceof ComplexShape)) {
			int strokeColor = form.getStrokeValue();
			rgb = Fields.rgba(strokeColor);
			getSlider("WOOutlineR").setValue(rgb[0]);
			getSlider("WOOutlineG").setValue(rgb[1]);
			getSlider("WOOutlineB").setValue(rgb[2]);
		}
		
		// Update the position and orientation fields
		fillCurWithCur(selected);
		
		MyDropdownList ddl = getDropdown("Fixture");

		if (selected instanceof Part) {
			// Set the reference dropdown for a part
			Part p = (Part)selected;
			fillDefWithDef(p);
			
			Fixture ref = p.getFixtureRef();
			ddl.setItem(ref);

		} else {
			ddl.setValue(0);
		}
	}
	
	public void updateCameraWindowFields() {
		if(app.getCamera() != null) {
			RobotCamera c = app.getRobotCamera();
			PVector pos = RMath.vToWorld(c.getPosition());
			PVector ori = RMath.nQuatToWEuler(c.getOrientation());
			
			getTextField("CXCur").setText(String.format("%4.3f", pos.x));
			getTextField("CYCur").setText(String.format("%4.3f", pos.y));
			getTextField("CZCur").setText(String.format("%4.3f", pos.z));
			
			getTextField("CWCur").setText(String.format("%4.3f", ori.x));
			getTextField("CPCur").setText(String.format("%4.3f", ori.y));
			getTextField("CRCur").setText(String.format("%4.3f", ori.z));
			
			getTextField("CCNearCur").setText(String.format("%4.3f", c.getNearClipDist()));
			getTextField("CCFarCur").setText(String.format("%4.3f", c.getFarClipDist()));
			
			getTextField("CFOVCur").setText(String.format("%4.3f", c.getFOV()));
			getTextField("CAspectCur").setText(String.format("%4.3f", c.getAspectRatio()));
			
			getSlider("CBright").setValue(c.getBrightness());
			getSlider("CExp").setValue(c.getExposure());
		}
	}

	/**
	 * Updates the positions of all the contents of the world object editing window.
	 */
	private void updateEditWindowContentPositions() {
		updateDimLblsAndFields();
		getButton(WGUI_Buttons.ObjClearFields).hide();
		
		WorldObject wo = getSelectedWO();
		boolean isPart = wo instanceof Part;
		
		// Object list dropdown and label
		int[] relPos = new int[] { winMargin, winMargin };
		ControllerInterface<?> c = getTextArea("WOEditLbl").setPosition(relPos[0], relPos[1]),
				c0 = null;
		relPos = getAbsPosFrom(c, Alignment.TOP_RIGHT, distLblToFieldX, 0);
		getDropdown("WO").setPosition(relPos[0], relPos[1]);
		// Edit tab label and radio buttons
		relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
		c = getTextArea("EditTabLbl").setPosition(relPos[0], relPos[1]);
		
		relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, winMargin);
		MyRadioButton rb = getRadioButton("EditTab");
		c = rb.setPosition(relPos[0], relPos[1]);
		
		float val = rb.getValue();
		
		if (val == 0f) {
			sharedElements.hide();
			editWOPos.show();
			editWOOther.hide();
			
			if (isPart) {
				relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
				c = getTextArea("RefLbl").setPosition(relPos[0], relPos[1]).show();
		
				relPos = getAbsPosFrom(c, Alignment.TOP_RIGHT, distLblToFieldX, 0);
				getDropdown("Fixture").setPosition(relPos[0], relPos[1]).show();
			
			} else {
				getTextArea("RefLbl").hide();
				getDropdown("Fixture").hide();
			}
			
			// Orientation column labels
			relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
			c = getTextArea("Blank").setPosition(relPos[0], relPos[1]);

			relPos = getAbsPosFrom(c, Alignment.TOP_RIGHT, distLblToFieldX, 0);
			c0 = getTextArea("Current").setPosition(relPos[0], relPos[1]);

			if (isPart) {
				relPos = getAbsPosFrom(c0, Alignment.TOP_RIGHT, distFieldToFieldX, 0);
				getTextArea("Default").setPosition(relPos[0], relPos[1]).show();

			} else {
				// Only show them for parts
				getTextArea("Default").hide();
			}

			// X label and fields
			relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, winMargin);
			c = getTextArea("XLbl").setPosition(relPos[0], relPos[1]);

			relPos = getAbsPosFrom(c, Alignment.TOP_RIGHT, distLblToFieldX, 0);
			c0 = getTextField("XCur").setPosition(relPos[0], relPos[1]);

			if (isPart) {
				relPos = getAbsPosFrom(c0, Alignment.TOP_RIGHT, distFieldToFieldX, 0);
				getTextArea("XDef").setPosition(relPos[0], relPos[1]).show();

			} else {
				getTextArea("XDef").hide();
			}

			// Y label and fields
			relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, winMargin);
			c = getTextArea("YLbl").setPosition(relPos[0], relPos[1]);

			relPos = getAbsPosFrom(c, Alignment.TOP_RIGHT, distLblToFieldX, 0);
			c0 = getTextField("YCur").setPosition(relPos[0], relPos[1]);

			if (isPart) {
				relPos = getAbsPosFrom(c0, Alignment.TOP_RIGHT, distFieldToFieldX, 0);
				getTextArea("YDef").setPosition(relPos[0], relPos[1]).show();

			} else {
				getTextArea("YDef").hide();
			}

			// Z label and fields
			relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, winMargin);
			c = getTextArea("ZLbl").setPosition(relPos[0], relPos[1]);;

			relPos = getAbsPosFrom(c, Alignment.TOP_RIGHT, distLblToFieldX, 0);
			c0 = getTextField("ZCur").setPosition(relPos[0], relPos[1]);

			if (isPart) {
				relPos = getAbsPosFrom(c0, Alignment.TOP_RIGHT, distFieldToFieldX, 0);
				getTextArea("ZDef").setPosition(relPos[0], relPos[1]).show();

			} else {
				getTextArea("ZDef").hide();
			}

			// W label and fields
			relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, winMargin);
			c = getTextArea("WLbl").setPosition(relPos[0], relPos[1]);

			relPos = getAbsPosFrom(c, Alignment.TOP_RIGHT, distLblToFieldX, 0);
			c0 = getTextField("WCur").setPosition(relPos[0], relPos[1]);

			if (isPart) {
				relPos = getAbsPosFrom(c0, Alignment.TOP_RIGHT, distFieldToFieldX, 0);
				getTextArea("WDef").setPosition(relPos[0], relPos[1]).show();

			} else {
				getTextArea("WDef").hide();
			}

			// P label and fields
			relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, winMargin);
			c = getTextArea("PLbl").setPosition(relPos[0], relPos[1]);

			relPos = getAbsPosFrom(c, Alignment.TOP_RIGHT, distLblToFieldX, 0);
			c0 = getTextField("PCur").setPosition(relPos[0], relPos[1]);

			if (isPart) {
				relPos = getAbsPosFrom(c0, Alignment.TOP_RIGHT, distFieldToFieldX, 0);
				getTextArea("PDef").setPosition(relPos[0], relPos[1]).show();

			} else {
				getTextArea("PDef").hide();
			}

			// R label and fields
			relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, winMargin);
			c = getTextArea("RLbl").setPosition(relPos[0], relPos[1]);

			relPos = getAbsPosFrom(c, Alignment.TOP_RIGHT, distLblToFieldX, 0);
			c0 = getTextField("RCur").setPosition(relPos[0], relPos[1]);

			if (isPart) {
				relPos = getAbsPosFrom(c0, Alignment.TOP_RIGHT, distFieldToFieldX, 0);
				getTextArea("RDef").setPosition(relPos[0], relPos[1]).show();

			} else {
				getTextArea("RDef").hide();
			}

			// Move to current button
			relPos = getAbsPosFrom(c0, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
			c = getButton(WGUI_Buttons.ObjMoveToCur).setPosition(relPos[0], relPos[1]);

			if (isPart) {
				// Update default button
				relPos = getAbsPosFrom(c, Alignment.TOP_RIGHT, distLblToFieldX, 0);
				getButton(WGUI_Buttons.ObjSetDefault).setPosition(relPos[0], relPos[1]).show();

				// Move to default button
				relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
				c0 = getButton(WGUI_Buttons.ObjMoveToDefault).setPosition(relPos[0], relPos[1]).show();

				// Restore Defaults button
				relPos = getAbsPosFrom(c0, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
				c = getButton(WGUI_Buttons.ObjResetDefault).setPosition(relPos[0], relPos[1]).show();

			} else {
				getButton(WGUI_Buttons.ObjSetDefault).hide();
				getButton(WGUI_Buttons.ObjMoveToDefault).hide();

				// Restore Defaults button
				relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
				c = getButton(WGUI_Buttons.ObjResetDefault).setPosition(relPos[0], relPos[1]).show();
			}
			
		} else {
			sharedElements.show();
			editWOPos.hide();
			editWOOther.show();
			
			// Dimension label and fields
			relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
			relPos = updateDimLblAndFieldPositions(relPos[0], relPos[1]);
			
			c0 = getButton(WGUI_Buttons.ObjConfirmDims).setPosition(relPos[0], relPos[1]);
			
			// Fill color label and dropdown
			relPos = getAbsPosFrom(c0, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
			c0 = getTextArea("WOFillLbl").setPosition(relPos[0], relPos[1] + 5);

			relPos = getAbsPosFrom(c0, Alignment.BOTTOM_LEFT, 0, winMargin);
			c = getSlider("WOFillR").setPosition(relPos[0], relPos[1]);
			
			relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, winMargin);
			c = getSlider("WOFillG").setPosition(relPos[0], relPos[1]);
			
			relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, winMargin);
			c = getSlider("WOFillB").setPosition(relPos[0], relPos[1]);

			if (wo != null && wo.getModel() instanceof ComplexShape) {
				// No stroke color for Model Shapes
				getTextArea("WOOutlineLbl").hide();
				getSlider("WOOutlineR").hide();
				getSlider("WOOutlineG").hide();
				getSlider("WOOutlineB").hide();

			} else {
				// Outline color labels and sliders
				relPos = getAbsPosFrom(c0, Alignment.TOP_RIGHT, 2 * distFieldToFieldX + sButtonWidth + 40, 0);
				c0 = getTextArea("WOOutlineLbl").setPosition(relPos[0], relPos[1]).show();
				
				relPos = getAbsPosFrom(c0, Alignment.TOP_RIGHT, distFieldToFieldX, 0);
				getTextArea("WOOutlineSmp").setPosition(relPos[0], relPos[1]).show();
				
				relPos = getAbsPosFrom(c0, Alignment.BOTTOM_LEFT, 0, winMargin);
				c0 = getSlider("WOOutlineR").setPosition(relPos[0], relPos[1]).show();
				
				relPos = getAbsPosFrom(c0, Alignment.BOTTOM_LEFT, 0, winMargin);
				c0 = getSlider("WOOutlineG").setPosition(relPos[0], relPos[1]).show();
				
				relPos = getAbsPosFrom(c0, Alignment.BOTTOM_LEFT, 0, winMargin);
				getSlider("WOOutlineB").setPosition(relPos[0], relPos[1]).show();
			}
			
			// Delete button
			relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
			c = getButton(WGUI_Buttons.ObjDelete).setPosition(winMargin, relPos[1]);
		}

		// Update window background display
		relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, winMargin);
		background.setPosition(editWO.getPosition())
		.setBackgroundHeight(relPos[1])
		.setHeight(relPos[1])
		.show();
	}
	
	/**
	 * Updates the positions of all the contents of the world object editing window.
	 */
	private void updateCameraWindowContentPositions() {
		// X label and fields
		int[] relPos = new int[] { winMargin, winMargin };
		ControllerInterface<?> c0, c = getTextArea("CXLbl").setPosition(relPos[0], relPos[1]);
		
		relPos = getAbsPosFrom(c, Alignment.TOP_RIGHT, distLblToFieldX, 0);
		c0 = getTextField("CXCur").setPosition(relPos[0], relPos[1]);

		// Cam clip near label and fields
		relPos = getAbsPosFrom(c0, Alignment.TOP_RIGHT, distFieldToFieldX, 0);
		c0 = getTextArea("CCNearLbl").setPosition(relPos[0], relPos[1]);
		
		relPos = getAbsPosFrom(c0, Alignment.TOP_RIGHT, distLblToFieldX, 0);
		getTextField("CCNearCur").setPosition(relPos[0], relPos[1]);
		
		// Y label and fields
		relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
		c = getTextArea("CYLbl").setPosition(relPos[0], relPos[1]);

		relPos = getAbsPosFrom(c, Alignment.TOP_RIGHT, distLblToFieldX, 0);
		c0 = getTextField("CYCur").setPosition(relPos[0], relPos[1]);
		
		// Cam clip far label and fields
		relPos = getAbsPosFrom(c0, Alignment.TOP_RIGHT, distFieldToFieldX, 0);
		c0 = getTextArea("CCFarLbl").setPosition(relPos[0], relPos[1]);
		
		relPos = getAbsPosFrom(c0, Alignment.TOP_RIGHT, distLblToFieldX, 0);
		getTextField("CCFarCur").setPosition(relPos[0], relPos[1]);

		// Z label and fields
		relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
		c = getTextArea("CZLbl").setPosition(relPos[0], relPos[1]);;

		relPos = getAbsPosFrom(c, Alignment.TOP_RIGHT, distLblToFieldX, 0);
		c0 = getTextField("CZCur").setPosition(relPos[0], relPos[1]);
		
		// FOV label and fields
		relPos = getAbsPosFrom(c0, Alignment.TOP_RIGHT, distFieldToFieldX, 0);
		c0 = getTextArea("CFOVLbl").setPosition(relPos[0], relPos[1]);
		
		relPos = getAbsPosFrom(c0, Alignment.TOP_RIGHT, distLblToFieldX, 0);
		getTextField("CFOVCur").setPosition(relPos[0], relPos[1]);

		// W label and fields
		relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
		c = getTextArea("CWLbl").setPosition(relPos[0], relPos[1]);

		relPos = getAbsPosFrom(c, Alignment.TOP_RIGHT, distLblToFieldX, 0);
		c0 = getTextField("CWCur").setPosition(relPos[0], relPos[1]);
		
		// Aspect ratio label and fields
		relPos = getAbsPosFrom(c0, Alignment.TOP_RIGHT, distFieldToFieldX, 0);
		c0 = getTextArea("CAspectLbl").setPosition(relPos[0], relPos[1]);
		
		relPos = getAbsPosFrom(c0, Alignment.TOP_RIGHT, distLblToFieldX, 0);
		getTextField("CAspectCur").setPosition(relPos[0], relPos[1]);

		// P label and fields
		relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
		c = getTextArea("CPLbl").setPosition(relPos[0], relPos[1]);

		relPos = getAbsPosFrom(c, Alignment.TOP_RIGHT, distLblToFieldX, 0);
		c0 = getTextField("CPCur").setPosition(relPos[0], relPos[1]);
		
		// Brightness slider 
		relPos = getAbsPosFrom(c0, Alignment.TOP_RIGHT, distFieldToFieldX, 0);
		getSlider("CBright").setPosition(relPos[0], relPos[1]);

		// R label and fields
		relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
		c = getTextArea("CRLbl").setPosition(relPos[0], relPos[1]);
		
		relPos = getAbsPosFrom(c, Alignment.TOP_RIGHT, distLblToFieldX, 0);
		c0 = getTextField("CRCur").setPosition(relPos[0], relPos[1]);
		
		// Shutter speed timer
		relPos = getAbsPosFrom(c0, Alignment.TOP_RIGHT, distFieldToFieldX, 0);
		getSlider("CExp").setPosition(relPos[0], relPos[1]);
		
		// Cam update button
		relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
		c = getButton(WGUI_Buttons.CamUpdate).setPosition(relPos[0], relPos[1]);
		
		relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
		c0 = getTextArea("ObjPreviewLbl").setPosition(relPos[0], relPos[1]);
		
		relPos = getAbsPosFrom(c0, Alignment.TOP_RIGHT, distFieldToFieldX, 0);
		getTextArea("SnapPreviewLbl").setPosition(relPos[0], relPos[1]);

		relPos = getAbsPosFrom(c0, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
		c = getButton(WGUI_Buttons.CamObjPreview).setPosition(relPos[0], relPos[1]);
		
		relPos = getAbsPosFrom(c, Alignment.TOP_RIGHT, distFieldToFieldX, 0);
		getButton(WGUI_Buttons.CamSnapPreview).setPosition(relPos[0], relPos[1]);
		
		relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
		c = getButton(WGUI_Buttons.CamTeachObj).setPosition(relPos[0], relPos[1]);
		
		relPos = getAbsPosFrom(c, Alignment.TOP_RIGHT, distFieldToFieldX, 0);
		getButton(WGUI_Buttons.CamDeleteObj).setPosition(relPos[0], relPos[1]);
		
		relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
		c = getDropdown("CamObjects").setPosition(relPos[0], relPos[1]);

		CameraObject o = (CameraObject)getDropdown("CamObjects").getSelectedItem();
		if(o != null) {
			PGraphics preview = o.getModelPreview(o.getLocalOrientation());
			getButton(WGUI_Buttons.CamObjPreview).setImage(preview);
			getButton(WGUI_Buttons.CamObjPreview).show();
		}
		else {
			getButton(WGUI_Buttons.CamObjPreview).hide();
		}
		
		if(app.getRobotCamera().getSnapshot() != null) {
			getButton(WGUI_Buttons.CamSnapPreview).setImage(app.getRobotCamera().getSnapshot());
			getButton(WGUI_Buttons.CamSnapPreview).show();
		} else {
			getButton(WGUI_Buttons.CamSnapPreview).hide();
		}
		// Update window background display
		relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
		background.setPosition(camera.getPosition())
		.setBackgroundHeight(relPos[1])
		.setHeight(relPos[1])
		.show();
	}
	
	/**
	 * Update the jog buttons based on the set of jog motion values.
	 * 
	 * @param jogMotion	A 6-element array defining the direction of jog motion
	 */
	public void updateJogButtons(int[] jogMotion) {
		for (int mdx = 0; mdx < jogMotion.length; ++mdx) {
			updateJogButtons(mdx, jogMotion[mdx]);
		}
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
	private void updateJogButtons(int pair, int newDir) {
		// Positive jog button is active when the direction is positive
		updateButtonBgColor(String.format(WGUI_Buttons.JointPos[pair], pair + 1), newDir > 0);
		// Negative jog button is active when the direction is negative
		updateButtonBgColor(String.format(WGUI_Buttons.JointNeg[pair], pair + 1), newDir < 0);
	}

	/**
	 * Updates the contents of all dynamic dropdown lists.
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
		} 
		else {
			Fields.debug("Missing data subfolder!");
		}

		if (app.getActiveScenario() != null) {
			dropdown = getDropdown("WO");
			dropdown.clear();

			MyDropdownList limbo = getDropdown("Fixture");
			limbo.clear();
			limbo.addItem("None", null);

			for (WorldObject wldObj : app.getActiveScenario()) {
				dropdown.addItem(wldObj.getName(), wldObj);

				if (wldObj instanceof Fixture) {
					// Load all fixtures from the active scenario
					limbo.addItem(wldObj.getName(), wldObj);
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
		
		RoboticArm r = app.getActiveRobot();

		if (r != null) {
			// Link the active robot's end effector to the dropdown list
			int activeEE = r.getActiveEEIdx();
			getDropdown("RobotEE").setValue(activeEE);
		}
	}
	
	public void updateCameraListContents() {
		if(app.getRobotCamera() != null) {
			MyDropdownList d = getDropdown("CamObjects");
			d.clear();
			
			for(WorldObject o: app.getRobotCamera().getTaughtObjects()) {
				d.addItem(o.getName(), o);
			}
			
			if(d.getItems().size() == 0) {
				d.setValue(0);
			}
			
			d.setSize(ldropItemWidth, dropItemHeight * (app.getRobotCamera().getTaughtObjects().size() + 1));
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
		int[] relPos = new int[] { winMargin, winMargin };
		ControllerInterface<?> c = getTextArea("SOptLbl").setPosition(relPos[0], relPos[1]);

		relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, 0);
		c = getRadioButton("ScenarioOpt").setPosition(relPos[0], relPos[1]);

		float winVar = c.getValue();
		Textarea ta = getTextArea("SInstructions");
		MyDropdownList mdl = getDropdown("Scenario");
		MyTextfield mtf = getTextField("SInput");
		Button b = getButton(WGUI_Buttons.ScenarioConfirm);

		if (winVar == 2f) { // Rename scenario variation
			// Scenario instructions
			relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
			ta.setPosition(relPos[0], relPos[1]);
			ta.setText("Select the scenario you wish to rename from the dropdown list and enter the new name into the text field below. Press RENAME to confirm the scenario's new name.");
			// Scenario dropdown list
			relPos = getAbsPosFrom(ta, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
			mdl.setPosition(relPos[0], relPos[1]).show();
			// Scenario input field
			relPos = getAbsPosFrom(mdl, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
			mtf.setPosition(relPos[0], relPos[1]).show();
			// Scenario confirm button
			relPos = getAbsPosFrom(mtf, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
			c = b.setPosition(relPos[0], relPos[1]);
			b.getCaptionLabel().setText("Rename");

		} else if (winVar == 1f) { // Load scenario variation
			// Scenario instructions
			relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
			ta.setPosition(relPos[0], relPos[1]);
			ta.setText("Select the scenario you wish to set as active from the dropdown list. Press LOAD to confirm your choice.");
			// Scenario dropdown list
			relPos = getAbsPosFrom(ta, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
			c = mdl.setPosition(relPos[0], relPos[1]).show();

			// Scenario confirm button
			relPos = getAbsPosFrom(mdl, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
			c = b.setPosition(relPos[0], relPos[1]);
			b.getCaptionLabel().setText("Load");

			mtf.hide();

		} else { // New scenario variation
			// Scenario instructions
			relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
			ta.setPosition(relPos[0], relPos[1]);
			ta.setText("Enter the name of the scenario you wish to create and press CONFIRM. A scenario name has to be unique, consist of only letters and numbers, and be of length less than 16.");
			// Scenario input field
			relPos = getAbsPosFrom(ta, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
			mtf.setPosition(relPos[0], relPos[1]).show();
			// Scenario confirm button
			relPos = getAbsPosFrom(mtf, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
			c = b.setPosition(relPos[0], relPos[1]);
			b.getCaptionLabel().setText("Create");

			mdl.hide();
		}

		// Update window background display
		relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
		background.setPosition(scenario.getPosition())
		.setBackgroundHeight(relPos[1])
		.setHeight(relPos[1])
		.show();
	}

	/**
	 * Update the color of the shift button based off the given state value
	 * (i.e. true is active, false is inactive).
	 * 
	 * @param state	The state of the shift button
	 */
	public void updateShiftButton(boolean state) {
		updateButtonBgColor(WGUI_Buttons.Shift, state);
		updateAndDrawUI();
	}

	/**
	 * Update the color of the set button based off the given state value (i.e.
	 * true is active, false is inactive).
	 * 
	 * @param state	The state of the step button
	 */
	public void updateStepButton(boolean state) {
		updateButtonBgColor(WGUI_Buttons.Step, state);
		updateAndDrawUI();
	}

	/**
	 * Updates the positions of all the contents of the miscellaneous window.
	 */
	private void updateMiscWindowContentPositions() {
		// Robot End Effector label
		int[] relPos = new int[] { winMargin, winMargin };
		ControllerInterface<?> c = getTextArea("ActiveRobotEE")
				.setPosition(relPos[0], relPos[1]);
		// Robot End Effector dropdown
		relPos = getAbsPosFrom(c, Alignment.TOP_RIGHT, distLblToFieldX, 0);
		getDropdown("RobotEE").setPosition(relPos[0], relPos[1]);

		// Axes Display label
		relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
		c = getTextArea("ActiveAxesDisplay").setPosition(relPos[0], relPos[1]);
		// Axes Display dropdown
		relPos = getAbsPosFrom(c, Alignment.TOP_RIGHT, distLblToFieldX, 0);
		getDropdown("AxesDisplay").setPosition(relPos[0], relPos[1]);

		// Robot Camera toggle button
		relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
		Button b = getButton(WGUI_Buttons.CamToggleActive).setPosition(relPos[0], relPos[1]);
		updateButtonBgColor(b.getName(), b.isOn());
		c = b;
		
		// Bounding box display toggle button
		relPos = getAbsPosFrom(b, Alignment.TOP_RIGHT, distFieldToFieldX, 0);
		b = getButton(WGUI_Buttons.ObjToggleBounds).setPosition(relPos[0], relPos[1]);
				
		// Update button color based on the state of the button
		if (b.isOn()) {
			b.setLabel("Show OBBs");

		} else {
			b.setLabel("Hide OBBs");
		}
		
		updateButtonBgColor(b.getName(), b.isOn());
		
		// Trace Robot Tool Tip button
		relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
		b = getButton(WGUI_Buttons.RobotToggleTrace).setPosition(relPos[0], relPos[1]);
		c = b;
		
		if (b.isOn()) {
			b.setLabel("Disable Trace");
			
		} else {
			b.setLabel("Enable Trace");
		}
		
		updateButtonBgColor(b.getName(), b.isOn());
		
		// Second robot toggle button
		relPos = getAbsPosFrom(b, Alignment.TOP_RIGHT, distFieldToFieldX, 0);
		b = getButton(WGUI_Buttons.RobotToggleActive).setPosition(relPos[0], relPos[1]);
		updateButtonBgColor(b.getName(), b.isOn());
		
		// Clear trace button
		relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
		c = b = getButton(WGUI_Buttons.RobotClearTrace).setPosition(relPos[0], relPos[1]);
		
		relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
		c = getRadioButton("DebugOptions").setPosition(relPos[0], relPos[1]);
		
		relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
		c = getToggle("TestOpt").setPosition(relPos[0], relPos[1]);
		
		// Update window background display
		relPos = getAbsPosFrom(c, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY + 60);
		background.setPosition(miscellaneous.getPosition())
		.setBackgroundHeight(relPos[1])
		.setHeight(relPos[1])
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
		
		updateAndDrawUI();
	}

	
	/**
	 * Updates the current active window display based on the selected button on
	 * windowTabs.
	 */
	public void updateAndDrawUI() {

		if (menu == null) {
			// Hide all windows
			setGroupVisible(pendant, false);
			setGroupVisible(createWO, false);
			setGroupVisible(editWO, false);
			setGroupVisible(sharedElements, false);
			setGroupVisible(scenario, false);
			setGroupVisible(camera, false);
			setGroupVisible(miscellaneous, false);
			
			clearAllInputFields();
			updateUIContentPositions();

		} else if (menu == WindowTab.ROBOT1 || menu == WindowTab.ROBOT2) {
			// Show pendant
			setGroupVisible(createWO, false);
			setGroupVisible(editWO, false);
			setGroupVisible(sharedElements, false);
			setGroupVisible(scenario, false);
			setGroupVisible(camera, false);
			setGroupVisible(miscellaneous, false);

			if (!pendant.isVisible()) {
				setGroupVisible(pendant, true);
				
				clearAllInputFields();
				updateUIContentPositions();
			}

		} else if (menu == WindowTab.CREATE) {
			// Show world object creation window
			setGroupVisible(pendant, false);
			setGroupVisible(editWO, false);
			setGroupVisible(scenario, false);
			setGroupVisible(camera, false);
			setGroupVisible(miscellaneous, false);

			if (!createWO.isVisible()) {
				setGroupVisible(createWO, true);
				setGroupVisible(sharedElements, true);

				clearAllInputFields();
				updateUIContentPositions();
			}

		} else if (menu == WindowTab.EDIT) {
			// Show world object edit window
			setGroupVisible(pendant, false);
			setGroupVisible(createWO, false);
			setGroupVisible(scenario, false);
			setGroupVisible(camera, false);
			setGroupVisible(miscellaneous, false);

			if (!editWO.isVisible()) {
				setGroupVisible(editWO, true);
				setGroupVisible(sharedElements, true);

				clearAllInputFields();
				updateUIContentPositions();
			}

		} else if (menu == WindowTab.SCENARIO) {
			// Show scenario creating/saving/loading
			setGroupVisible(pendant, false);
			setGroupVisible(createWO, false);
			setGroupVisible(editWO, false);
			setGroupVisible(sharedElements, false);
			setGroupVisible(camera, false);
			setGroupVisible(miscellaneous, false);

			if (!scenario.isVisible()) {
				setGroupVisible(scenario, true);

				clearAllInputFields();
				updateUIContentPositions();
			}
			
		} else if (menu == WindowTab.CAMERA) {
			setGroupVisible(pendant, false);
			setGroupVisible(createWO, false);
			setGroupVisible(editWO, false);
			setGroupVisible(sharedElements, false);
			setGroupVisible(scenario, false);
			setGroupVisible(miscellaneous, false);
			
			if (!camera.isVisible()) {
				setGroupVisible(camera, true);
				setGroupVisible(sharedElements, true);

				clearAllInputFields();
				updateCameraWindowFields();
				updateUIContentPositions();
				updateCameraListContents();
			}
			
		} else if (menu == WindowTab.MISC) {
			// Show miscellaneous window
			setGroupVisible(pendant, false);
			setGroupVisible(createWO, false);
			setGroupVisible(editWO, false);
			setGroupVisible(sharedElements, false);
			setGroupVisible(scenario, false);
			setGroupVisible(camera, false);

			if (!miscellaneous.isVisible()) {
				setGroupVisible(miscellaneous, true);
				
				clearAllInputFields();
				updateUIContentPositions();
			}
		}

		manager.draw();
	}
	

	/**
	 * Updates the positions of all the elements in the active window
	 * based on the current button tab that is active.
	 */
	public void updateUIContentPositions() {
		if (menu == null) {
			// Window is hidden
			background.hide();
			getButton(WGUI_Buttons.CamViewDef).hide();
			getButton(WGUI_Buttons.CamViewFr).hide();
			getButton(WGUI_Buttons.CamViewBk).hide();
			getButton(WGUI_Buttons.CamViewLt).hide();
			getButton(WGUI_Buttons.CamViewRt).hide();
			getButton(WGUI_Buttons.CamViewTp).hide();
			getButton(WGUI_Buttons.CamViewBt).hide();

		} else {
			
			if (menu == WindowTab.CREATE) {
				// Create window
				updateCreateWindowContentPositions();
	
			} else if (menu == WindowTab.EDIT) {
				// Edit window
				updateEditWindowContentPositions();
	
			} else if (menu == WindowTab.SCENARIO) {
				// Scenario window
				updateScenarioWindowContentPositions();
			
			} else if (menu == WindowTab.CAMERA) {
				// Camera window
				updateCameraWindowContentPositions();
				
			} else if (menu == WindowTab.MISC) {
				// Miscellaneous window
				updateMiscWindowContentPositions();
			}
	
			// Update the camera view buttons
			int[] relPos = getAbsPosFrom(windowTabs, Alignment.BOTTOM_RIGHT, winMargin, 0);
	
			Button b = getButton(WGUI_Buttons.CamViewDef).setPosition(relPos[0], relPos[1]).show();
			relPos = getAbsPosFrom(b, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
			b = getButton(WGUI_Buttons.CamViewFr).setPosition(relPos[0], relPos[1]).show();
			relPos = getAbsPosFrom(b, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
			b = getButton(WGUI_Buttons.CamViewBk).setPosition(relPos[0], relPos[1]).show();
			relPos = getAbsPosFrom(b, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
			b = getButton(WGUI_Buttons.CamViewLt).setPosition(relPos[0], relPos[1]).show();
			relPos = getAbsPosFrom(b, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
			b = getButton(WGUI_Buttons.CamViewRt).setPosition(relPos[0], relPos[1]).show();
			relPos = getAbsPosFrom(b, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
			b = getButton(WGUI_Buttons.CamViewTp).setPosition(relPos[0], relPos[1]).show();
			relPos = getAbsPosFrom(b, Alignment.BOTTOM_LEFT, 0, distBtwFieldsY);
			b = getButton(WGUI_Buttons.CamViewBt).setPosition(relPos[0], relPos[1]).show();
	
			updateListContents();
		}
	}

	/**
	 * Updates the current position and orientation of the selected world
	 * object.
	 * 
	 * @param selectedWO	The object of which to update the position and
	 * 						orientation
	 * @return				The undo state for the given world object
	 */
	public WOUndoState updateWOCurrent(WorldObject selectWO) {
		
		try {
			boolean edited = false;
			WOUndoState undoState = new WOUndoCurrent(selectWO);
			
			// Convert origin position into the World Frame
			PVector oPosition = RMath.vToWorld( selectWO.getLocalCenter() );
			PVector oWPR = RMath.nRMatToWEuler( selectWO.getLocalOrientation() );
			Float[] inputValues = getCurrentWOValues();
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
				oWPR.x = inputValues[3];
				edited = true;
			}

			if (inputValues[4] != null) {
				oWPR.y = inputValues[4];
				edited = true;
			}

			if (inputValues[5] != null) {
				oWPR.z = inputValues[5];
				edited = true;
			}

			// Convert values from the World to the Native coordinate system
			PVector position = RMath.vFromWorld( oPosition );
			RMatrix orientation = RMath.wEulerToNRMat(oWPR);
			// Update the Objects position and orientation
			selectWO.setLocalCenter(position);
			selectWO.setLocalOrientation(orientation);
			
			if (edited) {
				return undoState;
			}
			
		} catch (NullPointerException NPEx) {
			Fields.setMessage("All fields must be real numbers");
		}
		
		return null;
	}

	/**
	 * Updates the default position and orientation values of a part based on
	 * the input fields in the edit window.
	 * 
	 * @param selectedPart	The part, of which to update the default position
	 * 						and orientation
	 * @return				The undo state for the given part
	 */
	public WOUndoState updateWODefault(Part selectedPart) {
		boolean edited = false;
		WOUndoState undoState = new PartUndoDefault(selectedPart);
		
		// Pull the object's current position and orientation
		PVector defaultPos = RMath.vToWorld( selectedPart.getDefaultCenter() );
		PVector defaultWPR = RMath.nRMatToWEuler( selectedPart.getDefaultOrientation() );
		Float[] inputValues = getCurrentWOValues();

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
			defaultWPR.x = inputValues[3];
			edited = true;
		}

		if (inputValues[4] != null) {
			defaultWPR.y = inputValues[4];
			edited = true;
		}

		if (inputValues[5] != null) {
			defaultWPR.z = inputValues[5];
			edited = true;
		}

		// Convert values from the World to the Native coordinate system
		PVector position = RMath.vFromWorld( defaultPos );
		RMatrix orientation = RMath.wEulerToNRMat(defaultWPR);
		// Update the Object's default position and orientation
		selectedPart.setDefaultCenter(position);
		selectedPart.setDefaultOrientation(orientation);

		fillDefWithDef(selectedPart);
		
		if (edited) {
			return undoState;
		}
		
		return null;
	}
	
	/**
	 * Updates the dimensions of the given world object based on the non-empty
	 * values of the dimension input fields. If all dimension fields are empty,
	 * then the given world object will remain unchanged.
	 * 
	 * @param selectedWO	The world object, of which to update the dimensions	
	 * @return				If the world object's dimensions were updated
	 */
	public WOUndoState updateWODims(WorldObject selectedWO) {
		boolean dimChanged = false;
		WOUndoState undoState = new WOUndoDim(selectedWO);
		RShape s = selectedWO.getModel();

		if (s instanceof RBox) {
			Float[] newDims = getBoxDimensions();

			if (newDims[0] != null) {
				// Update the box's length
				dimChanged = updateDim(s, DimType.LENGTH, newDims[0]);
			}

			if (newDims[1] != null) {
				// Update the box's height
				boolean ret = updateDim(s, DimType.HEIGHT, newDims[1]);
				dimChanged = dimChanged || ret;
			}

			if (newDims[2] != null) {
				// Update the box's width
				boolean ret = updateDim(s, DimType.WIDTH, newDims[2]);
				dimChanged = dimChanged || ret;
			}

		} else if (s instanceof RCylinder) {
			Float[] newDims = getCylinderDimensions();

			if (newDims[0] != null) {
				// Update the cylinder's radius
				dimChanged = updateDim(s, DimType.RADIUS, newDims[0]);
			}

			if (newDims[1] != null) {
				// Update the cylinder's height
				boolean ret = updateDim(s, DimType.HEIGHT, newDims[1]);
				dimChanged = dimChanged || ret;
			}

		} else if (s instanceof ComplexShape) {
			Float[] newDims = getModelDimensions();

			if (newDims[0] != null) {
				boolean ret = updateDim(s, DimType.SCALE, newDims[0]);
				dimChanged = dimChanged || ret;
			}
		}

		if (dimChanged && selectedWO instanceof Part) {
			// Update the bounding box dimensions of a part
			((Part)selectedWO).updateOBBDims();
		}
		
		if (dimChanged) {
			return undoState;
		}
		
		return null;
	}
	
	/**
	 * Validates the given value with the given shape's bound for the specified
	 * dimension. If val is within the bounds for the dimension, then the
	 * shape's dimensions is updated, otherwise an error message is displayed
	 * in the UI.
	 * 
	 * @param s		The shape for which to validate the given value and update
	 * 				if the value is valid
	 * @param dim	The dimension with which the given value is associated
	 * @param val	The given value for the specified dimension of the given
	 * 				shape
	 * @return		Whether or not the specified dimension of the given shape
	 * 				is updated to the given value
	 */
	private boolean updateDim(RShape s, DimType dim, float val) {
		float lbound = s.getDimLBound(dim);
		float ubound = s.getDimUBound(dim);
		
		if (val < lbound || val > ubound) {
			Fields.setMessage("The shape's %s must be within the range %4.5f and %4.5f",
					dim.name().toLowerCase(), lbound, ubound);
			return false;
			
		} else {
			// Update the dimension
			s.setDim(val, dim);
			return true;
		}
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

	public void updateCameraCurrent() {
		try {
			float x = Float.parseFloat(getTextField("CXCur").getText());
			float y = Float.parseFloat(getTextField("CYCur").getText());
			float z = Float.parseFloat(getTextField("CZCur").getText());
			
			float w = Float.parseFloat(getTextField("CWCur").getText());
			float p = Float.parseFloat(getTextField("CPCur").getText());
			float r = Float.parseFloat(getTextField("CRCur").getText());
			
			PVector pos = new PVector(x, y, z);
			PVector rot = new PVector(w, p, r);
			
			float clipNear = Float.parseFloat(getTextField("CCNearCur").getText());
			float clipFar = Float.parseFloat(getTextField("CCFarCur").getText());
			
			float fov = Float.parseFloat(getTextField("CFOVCur").getText());
			float aspect = Float.parseFloat(getTextField("CAspectCur").getText());
			
			float br = getSlider("CBright").getValue();
			float exp = getSlider("CExp").getValue();
			
			app.getRobotCamera().update(pos, rot, fov, aspect, clipNear, clipFar, br, exp);
		}
		catch (NumberFormatException NFEx) {
			Fields.setMessage("Invalid number input!");

		} 
		catch (NullPointerException NPEx) {
			Fields.setMessage("Missing parameter!");
		}
	}
}

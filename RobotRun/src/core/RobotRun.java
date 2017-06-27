package core;

import java.awt.event.KeyEvent;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;

import enums.AxesDisplay;
import enums.CoordFrame;
import enums.ExecState;
import enums.ExecType;
import enums.WindowTab;
import expression.Expression;
import expression.ExpressionElement;
import expression.Operand;
import expression.Operator;
import expression.RobotPoint;
import frame.Frame;
import frame.ToolFrame;
import frame.UserFrame;
import geom.BoundingBox;
import geom.ComplexShape;
import geom.CoordinateSystem;
import geom.Fixture;
import geom.MyPShape;
import geom.Part;
import geom.Point;
import geom.RMatrix;
import geom.RRay;
import geom.RShape;
import geom.Triangle;
import geom.WorldObject;
import global.DataManagement;
import global.Fields;
import global.RMath;
import global.RegisteredModels;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PShape;
import processing.core.PVector;
import processing.event.MouseEvent;
import processing.opengl.PGraphicsOpenGL;
import programming.CallInstruction;
import programming.FrameInstruction;
import programming.IOInstruction;
import programming.IfStatement;
import programming.Instruction;
import programming.JumpInstruction;
import programming.LabelInstruction;
import programming.Macro;
import programming.MotionInstruction;
import programming.PosMotionInst;
import programming.ProgExecution;
import programming.Program;
import programming.RegisterStatement;
import programming.SelectStatement;
import regs.DataRegister;
import regs.IORegister;
import regs.PositionRegister;
import regs.Register;
import robot.RoboticArm;
import screen.Screen;
import screen.ScreenMode;
import screen.content_disp.ScreenNavProgInstructions;
import screen.content_disp.ScreenNavPrograms;
import screen.edit_point.ST_ScreenPointEntry;
import screen.num_entry.ST_ScreenNumEntry;
import screen.teach_frame.ST_ScreenTeachPoints;
import screen.teach_frame.ScreenTeach4Pt;
import screen.teach_frame.ScreenTeach6Pt;
import screen.text_entry.ST_ScreenTextEntry;
import ui.Camera;
import ui.DisplayLine;
import ui.KeyCodeMap;
import window.WGUI;

/**
 * TODO general comments
 * 
 * @author Vincent Druckte, Joshua Hooker, and James Walker
 */
public class RobotRun extends PApplet {
	private static RobotRun instance;
	
	/**
	 * Returns the instance of this PApplet	
	 */
	public static RobotRun getInstance() {
		return instance;
	}
	
	public static RoboticArm getInstanceRobot() {
		return instance.activeRobot;
	}

	public static Scenario getInstanceScenario() {
		return instance.activeScenario;
	}

	public static void main(String[] args) {
		String[] appletArgs = new String[] { "core.RobotRun" };

		if (args != null) {
			PApplet.main(concat(appletArgs, args));

		} else {
			PApplet.main(appletArgs);
		}
	}

	private final ArrayList<Scenario> SCENARIOS = new ArrayList<>();
	private final Stack<WorldObject> SCENARIO_UNDO = new Stack<>();
	private final HashMap<Integer, RoboticArm> ROBOTS = new HashMap<>();

	private Scenario activeScenario;
	private RoboticArm activeRobot;
	
	private ProgExecution progExecState;
	private Stack<ProgExecution> progCallStack;
	
	private RobotCamera rCamera;
	private Camera camera;

	private WGUI UI;
	private KeyCodeMap keyCodeMap;
	private Screen curScreen;

	private Stack<Screen> screenStack;
	private ArrayList<Macro> macros = new ArrayList<>();
	private Macro[] macroKeyBinds = new Macro[7];

	private boolean shift = false; // Is shift button pressed or not?
	private boolean step = false; // Is step button pressed or not?
	private boolean camEnable = false;
	private boolean record;

	/**
	 * Index of the current frame (Tool or User) selecting when in the Frame
	 * menus
	 */
	public int curFrameIdx = -1;

	/**
	 * The Frame being taught, during a frame teaching process
	 */
	public Frame teachFrame = null;
	public Operand<?> opEdit = null;
	public int editIdx = -1;

	// container for instructions being copied/ cut and pasted
	public ArrayList<Instruction> clipBoard = new ArrayList<>();
	
	/**
	 * A list of instruction indexes for the active program, which point to
	 * motion instructions, whose position and orientation (or joint angles)
	 * are close enough to the robot's current position.
	 */
	private HashMap<Integer, Boolean> mInstRobotAt;
	
	/**
	 * Defines a set of tool tip positions that are drawn to form a trace of
	 * the robot's motion overtime.
	 */
	private LinkedList<PVector> tracePts;
	
	/**
	 * Keeps track of the world object that the mouse was over, when the mouse
	 * was first pressed down.
	 */
	private WorldObject mouseOverWO;
	
	/**
	 * Keeps track of when the mouse is being dragged to update the position or
	 * orientation of a world object.
	 */
	private boolean mouseDragWO;
	
	/**
	 * Applies the active camera to the matrix stack.
	 * 
	 * @param	The camera to apply
	 */
	public void applyCamera(Camera c) {
		PVector cPos = c.getPosition();
		PVector cOrien = c.getOrientation();
		float horizontalMargin = c.getScale() * width / 2f,
				verticalMargin = c.getScale() * height / 2f,
				near = -5000f,
				far = 5000f;
		
		translate(cPos.x + width / 2f, cPos.y + height / 2f,  cPos.z);

		rotateX(cOrien.x);
		rotateY(cOrien.y);
		rotateZ(cOrien.z);
		
		// Apply orthogonal camera view
		ortho(-horizontalMargin, horizontalMargin, -verticalMargin,
				verticalMargin, near, far);
	}
	
	/**
	 * Wrapper method for applying the coordinate frame defined by the given
	 * row major rotation matrix and position vector.
	 * 
	 * @param origin		The origin of the coordinate frame with respect
	 * 						to the native coordinate system
	 * @param axesVectors	A 3x3 row major rotation matrix, which represents
	 * 						the orientation of the coordinate frame with
	 * 						respect to the native coordinate system
	 */
	public void applyCoord(PVector origin, RMatrix axesVectors) {
		super.applyMatrix(
				axesVectors.getEntryF(0, 0), axesVectors.getEntryF(0, 1), axesVectors.getEntryF(0, 2), origin.x,
				axesVectors.getEntryF(1, 0), axesVectors.getEntryF(1, 1), axesVectors.getEntryF(1, 2), origin.y,
				axesVectors.getEntryF(2, 0), axesVectors.getEntryF(2, 1), axesVectors.getEntryF(2, 2), origin.z,
				0f, 0f, 0f, 1f
		);
	}
	
	/**
	 * Wrapper method for applying the coordinate frame defined by the given
	 * column major transformation matrix.
	 * 
	 * @param tMatrix	A 4x4 row major transformation matrix
	 */
	public void applyMatrix(RMatrix mat) {
		super.applyMatrix(
				mat.getEntryF(0, 0), mat.getEntryF(0, 1), mat.getEntryF(0, 2), mat.getEntryF(0, 3),
				mat.getEntryF(1, 0), mat.getEntryF(1, 1), mat.getEntryF(1, 2), mat.getEntryF(1, 3),
				mat.getEntryF(2, 0), mat.getEntryF(2, 1), mat.getEntryF(2, 2), mat.getEntryF(2, 3),
				mat.getEntryF(3, 0), mat.getEntryF(3, 1), mat.getEntryF(3, 2), mat.getEntryF(3, 3)
		);
	}
	
	/**
	 * Pendant DOWN button
	 * 
	 * Moves down one element in a list displayed on the pendant screen.
	 * Depending on what menu is active this may move the list pointer
	 * in either the content or options menu.
	 */
	public void button_arrowDn() {
		curScreen.actionDn();
		updatePendantScreen();
	}
	
	/**
	 * Pendant LEFT button
	 * 
	 * Moves one column to the left on a list element display on the pendant's
	 * screen. Depending on what menu is active, this may move the pointer in
	 * either the content or options menu.
	 */
	public void button_arrowLt() {
		curScreen.actionLt();
		updatePendantScreen();
	}

	/**
	 * Pendant RIGHT button
	 * 
	 * Moves one column to the right in a list element display on the
	 * pendant's screen. Depending on what menu is active, this may move the
	 * pointer in either the content or options menu. In addition, when shift
	 * is active, the RIGHT button functions as a delete button in number,
	 * text, and point entry menus.
	 */
	public void button_arrowRt() {
		curScreen.actionRt();
		updatePendantScreen();
	}
	
	/**
	 * Pendant UP button
	 * 
	 * Moves up one element in a list displayed on the pendant screen.
	 * Depending on what menu is active this may move the list pointer
	 * in either the content or options menu.
	 */
	public void button_arrowUp() {
		curScreen.actionUp();
		updatePendantScreen();
	}

	/**
	 * Pendant BKSPC button
	 * 
	 * Functions as a backspace key for number, text, and point input menus.
	 */
	public void button_bkspc() {
		curScreen.actionBkspc();
		updatePendantScreen();
	}
	
	/**
	 * Pendant BWD button
	 * 
	 * Executes a motion instruction two instructions prior to the active
	 * instruction (if one exists).
	 */
	public void button_bwd() {
		// Backwards is only functional when executing a program one instruction
		// at a time
		if (curScreen instanceof ScreenNavProgInstructions && isShift() && isStep()) {
			// Safeguard against editing a program while it is running
			curScreen.getContents().setSelectedColumnIdx(0);
			progExecBwd();
		}
	}
	
	public void button_camTeachObj() {
		if(activeScenario != null) {
			rCamera.teachObjectToCamera(activeScenario);
		}
		
		UI.updateCameraListContents();
	}

	public void button_camToggleActive() {
		camEnable = UI.toggleCamera();
				
		UI.updateUIContentPositions();
		updatePendantScreen();
	}
	
	public void button_camUpdate() {
		if (rCamera != null) {
			UI.updateCameraCurrent();
		}
	}
	
	/**
	 * Camera Bk button
	 * 
	 * Sets the camera to the default back view, which looks down the positive
	 * x-axis of the world coordinate system.
	 */
	public void button_camViewBack() {
		// Back view
		camera.reset();
		camera.setRotation(0f, PI, 0f);
	}
	
	/**
	 * Camera Bt button
	 * 
	 * Sets the camera to point down the positive z-axis of the world coordinate
	 * frame, so as to view the bottom of the robot.
	 */
	public void button_camViewBottom() {
		// Bottom view
		camera.reset();
		camera.setRotation(HALF_PI, 0f, 0f);
	}
	
	/**
	 * Camera F button
	 * 
	 * Sets the camera to the default position, facing down the negative y-axis
	 * of the world coordinate system.
	 */
	public void button_camViewFront() {
		// Default view
		camera.reset();
	}
	
	/**
	 * Camera L button
	 * 
	 * Sets the camera facing down the negative x-axis of the world coordinate
	 * frame.
	 */
	public void button_camViewLeft() {
		// Left view
		camera.reset();
		camera.setRotation(0f, HALF_PI, 0f);
	}
	
	/**
	 * Camera R button
	 * 
	 * Sets the camera to point down the positive x-axis of the world
	 * coordinate frame.
	 */
	public void button_camViewRight() {
		// Right view
		camera.reset();
		camera.setRotation(0, 3f * HALF_PI, 0f);
	}

	/**
	 * Camera T button
	 * 
	 * Sets the camera to point down the negative z-axis of the world
	 * coordinate frame.
	 */
	public void button_camViewTop() {
		// Top view
		camera.reset();
		camera.setRotation(3f * HALF_PI, 0f, 0f);
	}
	
	/**
	 * Pendant COORD button
	 * 
	 * If shift is off, then this button will change the coordinate frame of
	 * the active robot. If shift is on, then this button will change to the
	 * active frames menu on the pendant.
	 */
	public void button_coord() {
		if (isShift()) {
			nextScreen(ScreenMode.ACTIVE_FRAMES);

		} else {
			// Update the coordinate frame
			coordFrameTransition();
			updatePendantScreen();
		}
	}

	/**
	 * Pendant - button
	 * 
	 * Appends the '-' character to input for number, text, and point entry
	 * menus.
	 */
	public void button_dash() {
		curScreen.actionKeyPress('-');
	}

	/**
	 * Pendant DATA button
	 * 
	 * Displays a list of the register navigation menus (i.e. data or position
	 * registers).
	 */
	public void button_data() {
		nextScreen(ScreenMode.NAV_DATA);
	}
	
	/**
	 * Pendant EDIT button
	 * 
	 * Links to the active program's instruction navigation menu, except in the
	 * select screen, where it will open the instruction navigation menu for
	 * the selected program
	 */
	public void button_edit() {
		if (curScreen instanceof ScreenNavPrograms) {
			// Load the selected program
			setActiveProgIdx(curScreen.getContentIdx());
			setActiveInstIdx(0);
			nextScreen(ScreenMode.NAV_PROG_INSTR);
			
		} else if (getActiveProg() != null) {
			// Load the current active program
			nextScreen(ScreenMode.NAV_PROG_INSTR);
		} else {
			// Load the program navigation menu
			resetStack();
			nextScreen(ScreenMode.NAV_PROGRAMS);
		}
	}
	
	/**
	 * Pendant ENTER button
	 * 
	 * Functions as a confirmation button for almost all menus.
	 */
	public void button_enter() {
		curScreen.actionEntr();
		updatePendantScreen();
	}
	
	/**
	 * Pendant F1 button
	 * 
	 * Function varies amongst menus. A hint label will appear in the pendant
	 * screen above a function button, which has an action in the current
	 * menu.
	 */
	public void button_F1() {
		curScreen.actionF1();
		updatePendantScreen();
	}

	/**
	 * Pendant F2 button
	 * 
	 * Function varies amongst menus. A hint label will appear in the pendant
	 * screen above a function button, which has an action in the current
	 * menu.
	 */
	public void button_F2() {
		curScreen.actionF2();
		updatePendantScreen();
	}

	/**
	 * Pendant F3 button
	 * 
	 * Function varies amongst menus. A hint label will appear in the pendant
	 * screen above a function button, which has an action in the current
	 * menu.
	 */
	public void button_F3() {
		curScreen.actionF3();
		updatePendantScreen();
	}

	/**
	 * Pendant F4 button
	 * 
	 * Function varies amongst menus. A hint label will appear in the pendant
	 * screen above a function button, which has an action in the current
	 * menu.
	 */
	public void button_F4() {
		curScreen.actionF4();
		updatePendantScreen();
	}
  
	/**
	 * Pendant F5 button
	 * 
	 * Function varies amongst menus. A hint label will appear in the pendant
	 * screen above a function button, which has an action in the current
	 * menu.
	 */
	public void button_F5() {
		curScreen.actionF5();
		updatePendantScreen();
	}

	/**
	 * Pendant FWD button
	 * 
	 * Executes instructions in the instruction navigation of a program. If
	 * step is active, then only one instruction is executed at a time,
	 * otherwise the entire program is executed.
	 */
	public void button_fwd() {
		if (curScreen instanceof ScreenNavProgInstructions && !isProgExec() && isShift()) {
			// Stop any prior Robot movement
			button_hold();
			// Safeguard against editing a program while it is running
			curScreen.getContents().setSelectedColumnIdx(0);
			progExec(isStep());
		}
	}
	
	/**
	 * Pendant HOLD button
	 * 
	 * Stops all robot motion and program execution.
	 */
	public void button_hold() {
		boolean robotInMotion = activeRobot.inMotion();
		// Stop all robot motion and program execution
		activeRobot.halt();
		progExecState.halt();
		
		if (robotInMotion && !activeRobot.inMotion()) {
			// Robot has stopped moving
			updateInstList();
		}
	}
	
	/**
	 * Pendant I/O button
	 * 
	 * If shift is inactive, then this button toggles the state of the active
	 * robot's active end effector. If shift is active, then this button
	 * executes the program binded with a macro to this button.
	 */
	public void button_io() {
		if (isShift()) {
			if (getMacroKeyBinds()[6] != null) {
				execute(getMacroKeyBinds()[6]);
			}

		} else {
			if (!isProgExec()) {
				// Map I/O to the robot's end effector state, if shift is off
				toggleEEState(activeRobot);
			}
		}
	}
	
	/**
	 * Pendant ITEM button
	 * 
	 * Not sure what this does ...
	 */
	public void button_item() {
		if (curScreen instanceof ScreenNavProgInstructions) {
			nextScreen(ScreenMode.JUMP_TO_LINE);
		}
	}
	
	/**
	 * Pendant -X/(J1) button
	 * 
	 * This button jogs the active robot with reference to its active coordinate
	 * frame. In addition, this button is paired with +X/(J1) in such a way that
	 * at most one of the two can be active at one time.
	 */
	public void button_jointNeg1() {
		updateRobotJogMotion(0, -1);
	}
	
	/**
	 * Pendant -Y/(J2) button
	 * 
	 * This button jogs the active robot with reference to its active coordinate
	 * frame. In addition, this button is paired with +Y/(J2) in such a way that
	 * at most one of the two can be active at one time.
	 */
	public void button_jointNeg2() {
		updateRobotJogMotion(1, -1);
	}
	
	/**
	 * Pendant -Z/(J3) button
	 * 
	 * This button jogs the active robot with reference to its active coordinate
	 * frame. In addition, this button is paired with +Z/(J3) in such a way that
	 * at most one of the two can be active at one time.
	 */
	public void button_jointNeg3() {
		updateRobotJogMotion(2, -1);
	}
	
	/**
	 * Pendant -XR/(J4) button
	 * 
	 * This button jogs the active robot with reference to its active coordinate
	 * frame. In addition, this button is paired with +XR/(J4) in such a way that
	 * at most one of the two can be active at one time.
	 */
	public void button_jointNeg4() {
		updateRobotJogMotion(3, -1);
	}
	
	/**
	 * Pendant -YR/(J5) button
	 * 
	 * This button jogs the active robot with reference to its active coordinate
	 * frame. In addition, this button is paired with +YR/(J5) in such a way that
	 * at most one of the two can be active at one time.
	 */
	public void button_jointNeg5() {
		updateRobotJogMotion(4, -1);
	}
	
	/**
	 * Pendant -ZR/(J6) button
	 * 
	 * This button jogs the active robot with reference to its active coordinate
	 * frame. In addition, this button is paired with +ZR/(J6) in such a way that
	 * at most one of the two can be active at one time.
	 */
	public void button_jointNeg6() {
		updateRobotJogMotion(5, -1);
	}
	
	/**
	 * Pendant +X/(J1) button
	 * 
	 * This button jogs the active robot with reference to its active coordinate
	 * frame. In addition, this button is paired with -X/(J1) in such a way that
	 * at most one of the two can be active at one time.
	 */
	public void button_jointPos1() {
		updateRobotJogMotion(0, 1);
	}
	
	/**
	 * Pendant +Y/(J2) button
	 * 
	 * This button jogs the active robot with reference to its active coordinate
	 * frame. In addition, this button is paired with -Y/(J2) in such a way that
	 * at most one of the two can be active at one time.
	 */
	public void button_jointPos2() {
		updateRobotJogMotion(1, 1);
	}
	
	/**
	 * Pendant +Z/(J3) button
	 * 
	 * This button jogs the active robot with reference to its active coordinate
	 * frame. In addition, this button is paired with -Z/(J3) in such a way that
	 * at most one of the two can be active at one time.
	 */
	public void button_jointPos3() {
		updateRobotJogMotion(2, 1);
	}

	/**
	 * Pendant +XR/(J4) button
	 * 
	 * This button jogs the active robot with reference to its active coordinate
	 * frame. In addition, this button is paired with -XR/(J4) in such a way that
	 * at most one of the two can be active at one time.
	 */
	public void button_jointPos4() {
		updateRobotJogMotion(3, 1);
	}

	/**
	 * Pendant +YR/(J5) button
	 * 
	 * This button jogs the active robot with reference to its active coordinate
	 * frame. In addition, this button is paired with -YR/(J5) in such a way that
	 * at most one of the two can be active at one time.
	 */
	public void button_jointPos5() {
		updateRobotJogMotion(4, 1);
	}

	/**
	 * Pendant +YR/(J5) button
	 * 
	 * This button jogs the active robot with reference to its active coordinate
	 * frame. In addition, this button is paired with -YR/(J5) in such a way that
	 * at most one of the two can be active at one time.
	 */
	public void button_jointPos6() {
		updateRobotJogMotion(5, 1);
	}

	/**
	 * Pendant MENU button
	 * 
	 * A list of miscellaneous sub menus (frames, marcos, I/O registers).
	 */
	public void button_menu() {
		nextScreen(ScreenMode.NAV_MAIN_MENU);
	}
	
	/**
	 * Pendant MVMU button
	 * 
	 * A button used for macro binding
	 */
	public void button_mvmu() {
		if (getMacroKeyBinds()[2] != null && isShift()) {
			execute(getMacroKeyBinds()[2]);
		}
	}
	
	/**
	 * Pendant '0' button
	 * 
	 * Appends a '0' character to input for the text, number, and point entry
	 * menus.
	 */
	public void button_num0() {
		curScreen.actionKeyPress('0');
	}

	/**
	 * Pendant '1' button
	 * 
	 * Appends a '1' character to input for the text, number, and point entry
	 * menus.
	 */
	public void button_num1() {
		curScreen.actionKeyPress('1');
	}

	/**
	 * Pendant '2' button
	 * 
	 * Appends a '2' character to input for the text, number, and point entry
	 * menus.
	 */
	public void button_num2() {
		curScreen.actionKeyPress('2');
	}

	/**
	 * Pendant '3' button
	 * 
	 * Appends a '3' character to input for the text, number, and point entry
	 * menus.
	 */
	public void button_num3() {
		curScreen.actionKeyPress('3');
	}

	/**
	 * Pendant '4' button
	 * 
	 * Appends a '4' character to input for the text, number, and point entry
	 * menus.
	 */
	public void button_num4() {
		curScreen.actionKeyPress('4');
	}
	
	/**
	 * Pendant '5' button
	 * 
	 * Appends a '5' character to input for the text, number, and point entry
	 * menus.
	 */
	public void button_num5() {
		curScreen.actionKeyPress('5');
	}
	
	/**
	 * Pendant '6' button
	 * 
	 * Appends a '6' character to input for the text, number, and point entry
	 * menus.
	 */
	public void button_num6() {
		curScreen.actionKeyPress('6');
	}
	
	/**
	 * Pendant '7' button
	 * 
	 * Appends a '7' character to input for the text, number, and point entry
	 * menus.
	 */
	public void button_num7() {
		curScreen.actionKeyPress('7');
	}
	
	/**
	 * Pendant '8' button
	 * 
	 * Appends a '8' character to input for the text, number, and point entry
	 * menus.
	 */
	public void button_num8() {
		curScreen.actionKeyPress('8');
	}

	/**
	 * Pendant '9' button
	 * 
	 * Appends a '9' character to input for the text, number, and point entry
	 * menus.
	 */
	public void button_num9() {
		curScreen.actionKeyPress('9');
	}
	
	/**
	 * Clear button shared between the Create and Edit windows
	 * 
	 * Clears all input fields (textfields, dropdownlist, etc.) in the world
	 * object creation and edit windows.
	 */
	public void button_objClearFields() {
		UI.clearAllInputFields();
	}

	/**
	 * Create button in the Create window
	 * 
	 * Pulls the user's input from the input fields (name, shape type,
	 * dimensions, colors, etc.) in the Create window and attempts to create a
	 * new world object from the user's input. If creation of a world object
	 * was successful, then the new object is added to the active scenario and
	 * all data is saved.
	 */
	public void button_objCreate() {
		if (activeScenario != null) {
			WorldObject newObject = UI.createWorldObject();

			if (newObject != null) {
				newObject.setLocalCenter(new PVector(-500f, 0f, 0f));
				activeScenario.addWorldObject(newObject);
				DataManagement.saveScenarios(this);
			}
		}
		else {
			System.err.println("No active scenario!");
		}
	}
	
	/**
	 * Confirm button in the Edit window
	 * 
	 * Updates the dimensions of the selected world object based off the values
	 * of the dimension input fields.
	 */
	public void button_objConfirmDims() {
		if (activeScenario != null) {
			WorldObject selectedWO = UI.getSelectedWO();
			
			if (selectedWO != null) {
				WorldObject saveState = selectedWO.clone();
				// Update the dimensions of the world object
				boolean updated = UI.updateWODims(selectedWO);
				
				if (updated) {
					// Save original world object onto the undo stack
					updateScenarioUndo(saveState);
				}
			}
			
		} else {
			System.err.println("No active scenario!");
		}
	}

	/**
	 * Delete button in the edit window
	 * 
	 * Removes the selected world object from the active scenario.
	 */
	public void button_objDelete() {
		// Delete focused world object and add to the scenario undo stack
		WorldObject selected = UI.getSelectedWO();
		if (selected != null) {
			updateScenarioUndo( selected );
			int ret = getActiveScenario().removeWorldObject( selected );
			
			if (ret == 0) {
				UI.setSelectedWO(null);
			}
			
			Fields.debug("World Object removed: %d\n", ret);
			
			DataManagement.saveScenarios(this);
		}
	}

	/**
	 * Move to Current button in the edit window
	 * 
	 * Updates the current position and orientation of a selected object to the
	 * inputed values in the edit window.
	 */
	public void button_objMoveToCur() {
		// Only allow world object editing when no program is executing
		if (!isProgExec()) {
			RoboticArm r = activeRobot;
			WorldObject selectedWO = UI.getSelectedWO();
			
			if (selectedWO instanceof Fixture || (selectedWO instanceof Part &&
					(r == null || !r.isHeld((Part)selectedWO)))) {
				
				WorldObject savedState = selectedWO.clone();
	
				if (UI.updateWOCurrent(selectedWO)) {
					/*
					 * If the object was modified, then save the previous state
					 * of the object
					 */
					updateScenarioUndo(savedState);
					
					/* If the edited object is a fixture, then update the orientation
					 * of all parts, which reference this fixture, in this scenario. */
					if (selectedWO instanceof Fixture && getActiveScenario() != null) {

						for (WorldObject wldObj : getActiveScenario()) {
							if (wldObj instanceof Part) {
								Part p = (Part)wldObj;

								if (p.getFixtureRef() == selectedWO) {
									p.updateAbsoluteOrientation();
								}
							}
						}
					}
				}
	
				DataManagement.saveScenarios(this);
			}
		}
	}

	/**
	 * Move to Default button in the edit window
	 * 
	 * Updates the current position and orientation of a selected world object
	 * to that of its default fields.
	 */
	public void button_objMoveToDefault() {
		// Only allow world object editing when no program is executing
		if (!isProgExec()) {
			RoboticArm r = activeRobot;
			WorldObject selectedWO = UI.getSelectedWO();
			
			if (selectedWO instanceof Part && (r == null || !r.isHeld((Part)selectedWO))) {
				WorldObject savedState = (WorldObject) selectedWO.clone();
				UI.fillCurWithDef( (Part)selectedWO );

				if (UI.updateWOCurrent(selectedWO)) {
					// If the part was modified, then save its previous state
					updateScenarioUndo(savedState);
				}

				DataManagement.saveScenarios(this);
			}
		}
	}

	/**
	 * Restores all parts in the current scenario to their default position and
	 * orientation.
	 */
	public void button_objResetDefault() {

		for (WorldObject wo : activeScenario) {
			// Only applies to parts
			if (wo instanceof Part) {
				updateScenarioUndo((WorldObject) wo.clone());
				Part p = (Part) wo;
				p.setLocalCenter(p.getDefaultCenter());
				p.setLocalOrientation(p.getDefaultOrientation());
			}
		}
	}
	
	/**
	 * HIDE/SHOW OBBBS button in the miscellaneous window
	 * 
	 * Toggles bounding box display on or off.
	 */
	public void button_objToggleBounds() {
		UI.updateUIContentPositions();
	}

	/**
	 * Update Default button in the edit window
	 * 
	 * Updates the default position and orientation of a world object based on
	 * the input fields in the edit window.
	 */
	public void button_objUpdateDefault() {
		WorldObject selectedWO = UI.getSelectedWO();
		// Only parts have a default position and orientation
		if (selectedWO instanceof Part) {
			WorldObject saveState = selectedWO.clone();
			
			if (UI.updateWODefault( (Part)selectedWO )) {
				// If the part was modified, then save its previous state
				updateScenarioUndo(saveState);
			}
		}
	}
	
	/**
	 * Pendant '.' buttom
	 * 
	 * Appends a '.' character to input in the text, number, and point entry
	 * menus.
	 */
	public void button_period() {
		curScreen.actionKeyPress('.');
	}

	/**
	 * Pendant POSN button
	 * 
	 * A button used for marcos binding.
	 */
	public void button_posn() {
		if (getMacroKeyBinds()[5] != null && isShift()) {
			execute(getMacroKeyBinds()[5]);
		}
	}

	/**
	 * Pendant PREV button
	 * 
	 * Transitions to the previous menu screen, if one exists.
	 */
	public void button_prev() {
		lastScreen();
	}

	/**
	 * Pendant RESET button
	 * 
	 * Resets the motion fault flag for the active robot, when the motion fault
	 * flag is set on.
	 */
	public void button_reset() {
		if (isShift()) {
			button_hold();
			// Reset motion fault for the active robot
			activeRobot.setMotionFault(false);
		}
	}

	/**
	 * ADD/REMOVE ROBOT button in the miscellaneous window
	 * 
	 * Toggles the second Robot on or off.
	 */
	public void button_robotToggleActive() {
		UI.toggleSecondRobot();
		// Reset the active robot to the first if the second robot is removed
		if (activeRobot != ROBOTS.get(0)) {
			activeRobot = ROBOTS.get(0);
		}

		UI.updateUIContentPositions();
		updatePendantScreen();
	}
	
	/**
	 * ENABLE/DISABLE TRACE button in miscellaneous window
	 * 
	 * Toggles the robot tool tip trace function on or off.
	 */
	public void button_robotToggleTrace() {
		UI.updateUIContentPositions();
		
		if (!traceEnabled()) {
			// Empty trace when it is disabled
			tracePts.clear();
		}
	}

	/**
	 * The scenario window confirmation button
	 * 
	 * Deals with the confirm functionality of the scenario window (i.e.
	 * setting the new name of a scenario, creating a new scenario,
	 * loading an inactive scenario).
	 */
	public void button_scenarioConfirm() {
		int ret = UI.updateScenarios(SCENARIOS);

		if (ret > 0) {
			activeScenario = UI.getSelectedScenario();
			DataManagement.saveScenarios(this);

		} else if (ret == 0) {
			DataManagement.saveScenarios(this);
		}
		
		Fields.debug(String.format("SConfirm: %d\n", ret));
	}

	/**
	 * Pendant SELECT button
	 * 
	 * Transitions to the program navigation menu, where the user can manage
	 * their programs.
	 */
	public void button_select() {
		nextScreen(ScreenMode.NAV_PROGRAMS);
	}
	
	/**
	 * Pendant SETUP button
	 * 
	 * A button used for binding macros.
	 */
	public void button_setup() {
		if (getMacroKeyBinds()[3] != null && isShift()) {
			execute(getMacroKeyBinds()[3]);
		}
	}

	/**
	 * Pendant SHIFT button
	 * 
	 * Toggles the shift state on or off. Shift is required to be on for
	 * anything involving robot motion or point recording.
	 */
	public void button_shift() {
		setShift(!shift);
	}
	
	/**
	 * Pendant +% button
	 * 
	 * Increases the robot's jog speed.
	 */
	public void button_spdDn() {
		int curSpeed = activeRobot.getLiveSpeed();
		// Reduce the speed at which the Robot jogs
		if (isShift()) {
			if (curSpeed > 50) {
				activeRobot.setLiveSpeed(50);
			} else if (curSpeed > 5) {
				activeRobot.setLiveSpeed(5);
			} else {
				activeRobot.setLiveSpeed(1);
			}
		} else if (curSpeed > 1) {
			if (curSpeed > 50) {
				activeRobot.setLiveSpeed(curSpeed - 10);
			} else if (curSpeed > 5) {
				activeRobot.setLiveSpeed(curSpeed - 5);
			} else {
				activeRobot.setLiveSpeed(curSpeed - 1);
			}
		}
	}
	
	/**
	 * Pendant -% button
	 * 
	 * Decreases the robot's jog speed.
	 */
	public void button_spdUp() {
		int curSpeed = activeRobot.getLiveSpeed();
		// Increase the speed at which the Robot jogs
		if (isShift()) {
			if (curSpeed < 5) {
				activeRobot.setLiveSpeed(5);
			} else if (curSpeed < 50) {
				activeRobot.setLiveSpeed(50);
			} else {
				activeRobot.setLiveSpeed(100);
			}
		} else if (curSpeed < 100) {
			if (curSpeed < 5) {
				activeRobot.setLiveSpeed(curSpeed + 1);
			} else if (curSpeed < 50) {
				activeRobot.setLiveSpeed(curSpeed + 5);
			} else {
				activeRobot.setLiveSpeed(curSpeed + 10);
			}
		}
	}
	
	/**
	 * Pendant STATUS button
	 * 
	 * A button used for macros.
	 */
	public void button_status() {
		if (getMacroKeyBinds()[4] != null && isShift()) {
			execute(getMacroKeyBinds()[4]);
		}
	}
	
	/**
	 * Pendant STEP button
	 * 
	 * Toggles the step state on or off. When step is on, then instructions
	 * will be executed one at a time as opposed to all at once.
	 */
	public void button_step() {
		setStep(!isStep());
	}
	
	/**
	 * Pendant TOOl1 button
	 * 
	 * A button used for binding marcos.
	 */
	public void button_tool1() {
		if (getMacroKeyBinds()[0] != null && isShift()) {
			execute(getMacroKeyBinds()[0]);
		}
	}
	
	/**
	 * Pendant TOOl2 button
	 * 
	 * A button used for binding marcos.
	 */
	public void button_tool2() {
		if (getMacroKeyBinds()[1] != null && isShift()) {
			execute(getMacroKeyBinds()[1]);
		}
	}
	
	/**
	 * Removes all saved program states from the program execution call stack.
	 */
	public void clearCallStack() {
		progCallStack.clear();
	}
	
	/**
	 * Transitions to the next Coordinate frame in the cycle, updating the
	 * Robot's current frame in the process and skipping the Tool or User frame
	 * if there are no active frames in either one. Since the Robot's frame is
	 * potentially reset in this method, all Robot motion is halted.
	 *
	 * @param model
	 *            The Robot Arm, for which to switch coordinate frames
	 */
	public void coordFrameTransition() {
		RoboticArm r = activeRobot;
		// Stop Robot movement
		button_hold();

		// Increment the current coordinate frame
		switch (r.getCurCoordFrame()) {
		case JOINT:
			r.setCoordFrame(CoordFrame.WORLD);
			break;

		case WORLD:
			r.setCoordFrame(CoordFrame.TOOL);
			break;

		case TOOL:
			r.setCoordFrame(CoordFrame.USER);
			break;

		case USER:
			r.setCoordFrame(CoordFrame.JOINT);
			break;
		}
	}

	/**
	 * This method attempts to modify the Frame based on the given value of
	 * method. If method is even, then the frame is taught via the 3-Point
	 * Method. Otherwise, the Frame is taught by either the 4-Point or 6-Point
	 * Method based on if the Frame is a Tool Frame or a User Frame.
	 * 
	 * @param frame
	 *            The frame to be taught
	 * @param method
	 *            The method by which to each the new Frame
	 */
	public void createFrame(Frame frame, int method) {
		if (teachFrame.setFrame(abs(method) % 2)) {
			Fields.debug("Frame set: %d\n", curFrameIdx);

			// Set new Frame
			if (frame instanceof ToolFrame) {
				// Update the current frame of the Robot Arm
				activeRobot.setActiveToolFrame(curFrameIdx);
				DataManagement.saveRobotData(activeRobot, 2);
				
			} else {
				// Update the current frame of the Robot Arm
				activeRobot.setActiveUserFrame(curFrameIdx);
				DataManagement.saveRobotData(activeRobot, 2);
			}

		} else {
			println("Invalid input points");
		}
	}

	/**
	 * This method takes the current values stored in contents (assuming that
	 * they corresond to the six direct entry values X, Y, Z, W, P, R), parses
	 * them, saves them to given Frame object, and sets the current Frame's
	 * values to the direct entry value, setting the current frame as the active
	 * frame in the process.
	 * 
	 * @param taughtFrame
	 *            the Frame, to which the direct entry values will be stored
	 */
	public void createFrameDirectEntry(Frame taughtFrame, float[] inputs) {
		// The user enters values with reference to the World Frame
		PVector origin, wpr;

		if (taughtFrame instanceof UserFrame) {
			origin = RMath.vFromWorld(new PVector(inputs[0], inputs[1], inputs[2]));
			
		} else {
			// Tool frame origins are actually an offset of the Robot's EE
			// position
			origin = new PVector(inputs[0], inputs[1], inputs[2]);
		}
		
		wpr = new PVector(inputs[3], inputs[4], inputs[5]);

		// Save direct entry values
		taughtFrame.setDEOrigin(origin);
		taughtFrame.setDEOrientationOffset( RMath.wEulerToNQuat(wpr) );
		taughtFrame.setFrame(2);

		Fields.debug("\n\n%s\n%s\nFrame set: %d\n", origin.toString(),
				wpr.toString(), curFrameIdx);

		// Set New Frame
		if (taughtFrame instanceof ToolFrame) {
			// Update the current frame of the Robot Arm
			activeRobot.setActiveToolFrame(curFrameIdx);
		} else {
			// Update the current frame of the Robot Arm
			activeRobot.setActiveUserFrame(curFrameIdx);
		}
		
		DataManagement.saveRobotData(activeRobot, 2);
	}

	@Override
	public void dispose() {
		// Save data before exiting
		DataManagement.saveState(this);
		super.dispose();
	}
	
	@Override
	public void draw() {
		try {
			background(255);
			hint(ENABLE_DEPTH_TEST);
			directionalLight(255, 255, 255, 1, 1, 0);
			ambientLight(150, 150, 150);
			
			pushMatrix();
			// Apply the camera for drawing objects
			applyCamera(camera);
			renderScene();
			// Update jog buttons
			int[] jogMotion = activeRobot.getJogMotion();
			
			if (jogMotion == null) {
				UI.updateJogButtons(new int[] {0, 0, 0, 0, 0, 0});
				
			} else {
				UI.updateJogButtons(jogMotion);
			}
			
			if (teachFrame != null && curScreen instanceof ST_ScreenTeachPoints) {
				renderTeachPoints(teachFrame);
			}
	
			/* TESTING CODE: DRAW INTERMEDIATE POINTS *
			if(Fields.DEBUG && intermediatePositions != null) {
				int count = 0;
				for(Point p : intermediatePositions) {
					if(count % 4 == 0) {
						pushMatrix();
						stroke(0);
						translate(p.position.x, p.position.y, p.position.z);
						sphere(5);
						popMatrix();
					}
	
					count += 1;
				}
			}
			
			/*Camera Test Code*/
			if(camEnable) {
				Fields.drawAxes(getGraphics(), rCamera.getPosition(), rCamera.getOrientationMat(), 300, 0);
				
				PVector near[] = rCamera.getPlaneNear();
				PVector far[] = rCamera.getPlaneFar();
				pushMatrix();
				stroke(255, 126, 0, 255);
				beginShape();
				//Top
				vertex(near[0].x, near[0].y, near[0].z);
				vertex(far[0].x, far[0].y, far[0].z);
				vertex(far[1].x, far[1].y, far[1].z);
				vertex(near[1].x, near[1].y, near[1].z);
				//Right
				vertex(near[1].x, near[1].y, near[1].z);
				vertex(far[1].x, far[1].y, far[1].z);
				vertex(far[3].x, far[3].y, far[3].z);
				vertex(near[3].x, near[3].y, near[3].z);
				//Bottom
				vertex(near[3].x, near[3].y, near[3].z);
				vertex(far[3].x, far[3].y, far[3].z);
				vertex(far[2].x, far[2].y, far[2].z);
				vertex(near[2].x, near[2].y, near[2].z);
				//Left
				vertex(near[2].x, near[2].y, near[2].z);
				vertex(far[2].x, far[2].y, far[2].z);
				vertex(far[0].x, far[0].y, far[0].z);
				vertex(near[0].x, near[0].y, near[0].z);
				//Near
				vertex(near[1].x, near[1].y, near[1].z);
				vertex(near[3].x, near[3].y, near[3].z);
				vertex(near[2].x, near[2].y, near[2].z);
				vertex(near[0].x, near[0].y, near[0].z);
				endShape();
				
				popMatrix();
			}
			
			popMatrix();
			
			renderUI();
			
		} catch (Exception Ex) {
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	public void editExpression(Expression expr, int selectIdx) {
		int[] elements = expr.mapToEdit();
		
		try {
			opEdit = expr;
			ExpressionElement e = expr.get(elements[selectIdx]);
	
			if (e instanceof Expression) {
				// if selecting the open or close paren
				if (selectIdx == 0 || selectIdx == e.getLength() || elements[selectIdx - 1] != elements[selectIdx]
						|| elements[selectIdx + 1] != elements[selectIdx]) {
					nextScreen(ScreenMode.SET_EXPR_ARG);
				} else {
					int startIdx = expr.getStartingIdx(elements[selectIdx]);
					editExpression((Expression) e, selectIdx - startIdx - 1);
				}
			} else if (e instanceof Operand) {
				editOperand((Operand<?>) e, elements[selectIdx]);
			} else {
				editIdx = elements[selectIdx];
				nextScreen(ScreenMode.SET_EXPR_OP);
			}
			
		} catch (ArrayIndexOutOfBoundsException AIOOBEx) {
			System.err.printf("Invalid expression index: %d!\n", selectIdx);
		}
	}

	/**
	 * Accepts an ExpressionOperand object and forwards the UI to the
	 * appropriate menu to edit said object based on the operand type.
	 *
	 * @param o
	 *            - The operand to be edited.
	 * @ins_idx - The index of the operand's container ExpressionElement list
	 *          into which this operand is stored.
	 *
	 */
	public void editOperand(Operand<?> o, int ins_idx) {
		editIdx = ins_idx;
		
		switch (o.getType()) {
		case Operand.UNINIT: // Uninit
			nextScreen(ScreenMode.SET_EXPR_ARG);
			break;
		case Operand.FLOAT: // Float const
			opEdit = o;
			nextScreen(ScreenMode.INPUT_CONST);
			break;
		case Operand.BOOL: // Bool const
			opEdit = o;
			nextScreen(ScreenMode.SET_BOOL_CONST);
			break;
		case Operand.DREG: // Data reg
			opEdit = o;
			nextScreen(ScreenMode.INPUT_DREG_IDX);
			break;
		case Operand.IOREG: // IO reg
			opEdit = o;
			nextScreen(ScreenMode.INPUT_IOREG_IDX);
			break;
		case Operand.PREG: // Pos reg
			opEdit = o;
			nextScreen(ScreenMode.INPUT_PREG_IDX1);
			break;
		case Operand.PREG_IDX: // Pos reg at index
			opEdit = o;
			nextScreen(ScreenMode.INPUT_PREG_IDX2);
			nextScreen(ScreenMode.INPUT_PREG_IDX1);
			break;
		case Operand.ROBOT: // Robot point
			RobotPoint rp = (RobotPoint)o;
			rp.setType(!rp.isCartesian());
			break;
		}
	}

	/**
	 * Execute the given macro
	 * 
	 * @param m
	 */
	public void execute(Macro m) {
		// Stop any prior Robot movement
		button_hold();
		// Safeguard against editing a program while it is running
		curScreen.getContents().setSelectedColumnIdx(0);
		progExec(m.getProgIdx(), 0, isStep());
	}

	/**
	 * @return	The index of the active program's active instruction
	 */
	public int getActiveInstIdx() {
		return progExecState.getCurIdx();
	}

	/**
	 * @return	The active instruction of the active program, or null if no
	 * 			program is active
	 */
	public Instruction getActiveInstruction() {
		Program prog = getActiveProg();
		
		if (prog == null || getActiveInstIdx() < 0 || getActiveInstIdx() >= prog.size()) {
			// Invalid instruction or program index
			return null;
		}
		
		return prog.get(getActiveInstIdx());
	}

	/**
	 * @return	The active for the active Robot, or null if no program is active
	 */
	public Program getActiveProg() {
		if (getActiveProgIdx() >= 0 && getActiveProgIdx() <
				activeRobot.numOfPrograms()) {
			
			return activeRobot.getProgram(getActiveProgIdx());
		}
		
		// Invalid program index
		return null;
	}

	/**
	 * @return	The index of the active program
	 */
	public int getActiveProgIdx() {
		return progExecState.getProgIdx();
	}

	public RoboticArm getActiveRobot() {
		return activeRobot;
	}

	public Scenario getActiveScenario() {
		return activeScenario;
	}

	/**
	 * @return the active axes display state
	 */
	public AxesDisplay getAxesState() {
		return UI.getAxesDisplay();
	}
	
	public Camera getCamera() {
		return camera;
	}

	/**
	 * Pulls the origin and orientation from the top of the matrix stack and
	 * creates a coordinate system from them.
	 * 
	 * @return	A coordinate system system representing the top of matrix
	 */
	public CoordinateSystem getCoordFromMatrix() {
		PVector origin = getPosFromMatrix(0f, 0f, 0f);
		RMatrix axes = getOrientation();
		
		return new CoordinateSystem(origin, axes);
	}
	
	/**
	 * Sets the coordinate system of the given bounding box to match the top
	 * of the matrix stack.
	 * 
	 * @param obb	The bounding box, of which to set the coordinate system to 
	 * 				the top of the matrix stack
	 */
	public void getCoordFromMatrix(BoundingBox obb) {
		PVector center = getPosFromMatrix(0f, 0f, 0f);
		RMatrix orientation = getOrientation();
		
		obb.setCenter(center);
		obb.setOrientation(orientation);
	}

	/**
	 * Sets the given coordinate system to match the top of the matrix stack.
	 * 
	 * @param cs	The coordinate system to set as the top of the matrix stack
	 */
	public void getCoordFromMatrix(CoordinateSystem cs) {
		PVector origin = getPosFromMatrix(0f, 0f, 0f);
		RMatrix axes = getOrientation();
		
		cs.setOrigin(origin);
		cs.setAxes(axes);
	}

	public RoboticArm getInactiveRobot() {
		try {
			return ROBOTS.get((activeRobot.RID + 1) % 2);

		} catch (Exception Ex) {
			return null;
		}
	}

	/**
	 * Returns the first line in the current list of contents that the
	 * instruction matching the given index appears on.
	 */
	public int getInstrLine(int instrIdx) {
		ArrayList<DisplayLine> instr = loadInstructions(getActiveProg(), false);
		int row = instrIdx;
		
		try {	
			while (instr.get(row).getItemIdx() != instrIdx) {
				row += 1;
				if (curScreen.getContentIdx() >= curScreen.getContents().size() - 1)
					break;
			}
		
			return row;
			
		} catch (NullPointerException NPEx) {
			return 0;
			
		} catch (IndexOutOfBoundsException IOOBEx) {
			//System.err.printf("inst=%d row=%d size=%d\n", instrIdx, row, instr.size());
			return row;
		}
	}
	
	public KeyCodeMap getKeyCodeMap() {
		return keyCodeMap;
	}

	public Screen getLastScreen() {
		return screenStack.get(screenStack.size() - 2);
	}

	public Macro getMacro(int idx) {
		return macros.get(idx);
	}
	
	public Macro[] getMacroKeyBinds() {
		return macroKeyBinds;
	}

	public ArrayList<Macro> getMacroList() {
		return macros;
	}

	public ScreenMode getMode() {
		return curScreen.mode;
	}
	
	/**
	 * Copies the current rotation on the top matrix of Processing's matrix
	 * stack to a 3x3 floating-point array.
	 * 
	 * @return	A row major orthogonal rotation matrix
	 */
	public RMatrix getOrientation() {
		return RMath.getOrientationAxes(getGraphics());
	}

	/*
	 * This method transforms the given coordinates into a vector in the
	 * Processing's native coordinate system.
	 */
	public PVector getPosFromMatrix(float x, float y, float z) {
		return RMath.getPosition(getGraphics(), x, y, z);
	}

	public Stack<ProgExecution> getProgCallStack() {
		return progCallStack;
	}

	public boolean getRecord() {
		return record;
	}

	/**
	 * Returns the robot with the associated ID, or null if no such robot
	 * exists.
	 * 
	 * @param rid
	 *            A valid robot ID
	 * @return The robot with the given ID
	 */
	public RoboticArm getRobot(int rid) {
		return ROBOTS.get(rid);
	}

	public RobotCamera getRobotCamera() {
		return rCamera;
	}

	public ArrayList<Scenario> getScenarios() {
		return SCENARIOS;
	}

	public Stack<Screen> getScreenStack() {
		return screenStack;
	}

	/**
	 * Copies the current rotation and translations of the top matrix on
	 * Processing's matrix stack to a 4x4 floating-point array. Any scaling
	 * is ignored. 
	 * 
	 * @return	A 4x4 row major transformation matrix
	 */
	public RMatrix getTransformationMatrix() {
		PVector origin = getPosFromMatrix(0, 0, 0);
		PVector xAxis = getPosFromMatrix(1, 0, 0).sub(origin);
		PVector yAxis = getPosFromMatrix(0, 1, 0).sub(origin);
		PVector zAxis = getPosFromMatrix(0, 0, 1).sub(origin);

		return RMath.formTMat(
				xAxis.x, yAxis.x, zAxis.x, origin.x,
				xAxis.y, yAxis.y, zAxis.y, origin.y,
				xAxis.z, yAxis.z, zAxis.z, origin.z
		);
	}

	public WGUI getUI() {
		return UI;
	}

	/**
	 * @return Whether or not bounding boxes are displayed
	 */
	public boolean isOBBRendered() {
		return !UI.getButtonState("ToggleOBBs");
	}

	/**
	 * @return	Is the active robot executing a program?
	 */
	public boolean isProgExec() {
		return !progExecState.isDone();
	}

	/**
	 * @return Whether or not the second robot is used in the application
	 */
	public boolean isSecondRobotUsed() {
		return UI.getRobotButtonState();
	}

	public boolean isShift() {
		return shift;
	}
	
	public boolean isStep() {
		return step;
	}
	
	@Override
	public void keyPressed() {
		boolean ctrlDown = keyCodeMap.isKeyDown(KeyEvent.VK_CONTROL);
		keyCodeMap.keyPressed(keyCode, key);

		if (key == 27) {
			// Disable the window exiting function of the 'esc' key
			key = 0;
			
		} else if ((UI != null && UI.isATextFieldActive())) {
			
			/* Disable key events when typing in a text field or entering text
			 * on the pendant */
			return;
			
		}  else if (UI != null && UI.isPendantActive()) {
			// Suppress other key events when entering text for the pendant
			curScreen.actionKeyPress(key);
			updatePendantScreen();
			
			// Pendant button shortcuts
			if (!(curScreen instanceof ST_ScreenTextEntry) &&
					!(curScreen instanceof ST_ScreenNumEntry) &&
					!(curScreen instanceof ST_ScreenPointEntry)) {
				// Disable function shortcuts when entering in text or number input
				if (keyCode == KeyEvent.VK_1) {
					button_F1();
					
				} else if (keyCode == KeyEvent.VK_2) {
					button_F2();
					
				} else if (keyCode == KeyEvent.VK_3) {
					button_F3();
					
				} else if (keyCode == KeyEvent.VK_4) {
					button_F4();
					
				} else if (keyCode == KeyEvent.VK_5) {
					button_F5();
				}
				
			}
			
			if (keyCode == KeyEvent.VK_ENTER) {
				button_enter();
				
			} else if (keyCode == KeyEvent.VK_BACK_SPACE) {
				button_bkspc();
				
			} else if (keyCode == KeyEvent.VK_DOWN) {
				button_arrowDn();
				
			} else if (keyCode == KeyEvent.VK_LEFT) {
				button_arrowLt();
				
			} else if (keyCode == KeyEvent.VK_RIGHT) {
				button_arrowRt();
				
			} else if (keyCode == KeyEvent.VK_UP) {
				button_arrowUp();
			}
		}
		
		// General key functions
		if (ctrlDown) {
			
			if (keyCode == KeyEvent.VK_C) {
				// Update the coordinate frame
				coordFrameTransition();
				updatePendantScreen();
				
			} else if (keyCode == KeyEvent.VK_E) {
				// Cycle End Effectors
				if (!isProgExec()) {
					activeRobot.cycleEndEffector();
					UI.updateListContents();
				}
				
			} else if (keyCode == KeyEvent.VK_P) {
				// Toggle the Robot's End Effector state
				if (!isProgExec()) {
					toggleEEState(activeRobot);
				}
				
			} else if (keyCode == KeyEvent.VK_T) {
				// Restore default Robot joint angles
				button_hold();
				float[] rot = { 0, 0, 0, 0, 0, 0 };
				activeRobot.releaseHeldObject();
				activeRobot.setJointAngles(rot);
				
			} else if (keyCode == KeyEvent.VK_R) {
				
				if (keyCodeMap.isKeyDown(KeyEvent.VK_ALT)) {
					// Toggle record state
					setRecord( !getRecord() );
					
				} else {
					// Rest motion fault
					button_reset();
				}
				
			} else if ( keyCode == KeyEvent.VK_S) {
				// Save EVERYTHING!
				DataManagement.saveState(this);
				
			} else if (keyCode == KeyEvent.VK_Z) {
				// Scenario undo
				if (UI != null) {
					if (!UI.isPendantActive()) {
						undoScenarioEdit();
					}
				}	
			}
			
		} else if (!UIKeyboardUse()) {
			
			// Pendant button shortcuts
			switch(keyCode) {
			case KeyEvent.VK_SHIFT:		if (!(curScreen instanceof ST_ScreenTextEntry)) 
											setShift(true); break;
			case KeyEvent.VK_U: 		button_jointNeg1(); break;
			case KeyEvent.VK_I:			button_jointPos1(); break;
			case KeyEvent.VK_J: 		button_jointNeg2(); break;
			case KeyEvent.VK_K: 		button_jointPos2(); break;
			case KeyEvent.VK_M: 		button_jointNeg3(); break;
			case KeyEvent.VK_COMMA:		button_jointPos3(); break;
			case KeyEvent.VK_O: 		button_jointNeg4(); break;
			case KeyEvent.VK_P:			button_jointPos4(); break;
			case KeyEvent.VK_L: 		button_jointNeg5(); break;
			case KeyEvent.VK_SEMICOLON: button_jointPos5(); break;
			case KeyEvent.VK_PERIOD: 	button_jointNeg6(); break;
			case KeyEvent.VK_SLASH:		button_jointPos6(); break;
			case KeyEvent.VK_MINUS:		button_spdDn(); break;
			case KeyEvent.VK_EQUALS:	button_spdUp(); break;
			case KeyEvent.VK_S:			rCamera.teachObjectToCamera(getActiveScenario()); break;
			}
			
		}
	}
	
	public void keyReleased() {
		keyCodeMap.keyReleased(keyCode, key);
		
		if (!keyCodeMap.isKeyDown(KeyEvent.VK_CONTROL) &&
				!UIKeyboardUse()) {
			
			switch(keyCode) {
			case KeyEvent.VK_SHIFT: 	if (!(curScreen instanceof ST_ScreenTextEntry))
											setShift(false); break;
			case KeyEvent.VK_U: 		button_jointNeg1(); break;
			case KeyEvent.VK_I:			button_jointPos1(); break;
			case KeyEvent.VK_J: 		button_jointNeg2(); break;
			case KeyEvent.VK_K: 		button_jointPos2(); break;
			case KeyEvent.VK_M: 		button_jointNeg3(); break;
			case KeyEvent.VK_COMMA:		button_jointPos3(); break;
			case KeyEvent.VK_O: 		button_jointNeg4(); break;
			case KeyEvent.VK_P:			button_jointPos4(); break;
			case KeyEvent.VK_L: 		button_jointNeg5(); break;
			case KeyEvent.VK_SEMICOLON: button_jointPos5(); break;
			case KeyEvent.VK_PERIOD: 	button_jointNeg6(); break;
			case KeyEvent.VK_SLASH:		button_jointPos6(); break;
			}
		}
	}
	
	/**
	 * Pulls off the current screen state from the screen state stack and loads
	 * the previous screen state as the active screen state.
	 * 
	 * @return	If a previous screen exists
	 */
	public void lastScreen() {
		Screen cur = screenStack.peek();
		
		if (cur.mode != ScreenMode.DEFAULT) {
			screenStack.pop();
			curScreen = screenStack.peek();
			
			Fields.debug("\n%s <= %s\n", cur.mode, curScreen.mode);
			
			updatePendantScreen();
		}		
	}
	
	/**
	 * Loads the data registers of the given robotic arm into a list of display
	 * lines, which can be rendered onto the pendant screen.
	 * 
	 * @param r	The robotic arm, from which to load the data registers
	 * @return	The list of display lines representing the given robot's data
	 * 			registers
	 */
	public ArrayList<DisplayLine> loadDataRegisters(RoboticArm r) {
		ArrayList<DisplayLine> lines = new ArrayList<>();
		
		// Display a subset of the list of registers
		for (int idx = 0; idx < Fields.DPREG_NUM; ++idx) {
			DataRegister reg = r.getDReg(idx);

			// Display the comment associated with a specific Register entry
			String regLbl = reg.toStringWithComm();
			// Display Register value (* if uninitialized)
			String regEntry = "*";

			if (reg.value != null) {
				// Display Register value
				regEntry = String.format("%4.3f", reg.value);

			} else {
				regEntry = "*";
			}

			lines.add(new DisplayLine(idx, 0 , regLbl, regEntry));
		}
		
		return lines;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param robot
	 * @return
	 */
	public ArrayList<DisplayLine> loadEEToolTipDefaults(RoboticArm robot) {
		ArrayList<DisplayLine> lines = new ArrayList<>();
		
		for (int idx = 0; idx < activeRobot.numOfEndEffectors(); ++idx) {
			IORegister ioReg = activeRobot.getIOReg(idx + 1);
			PVector defToolTip = activeRobot.getToolTipDefault(idx);
			String lineStr = String.format("%s = (%4.3f, %4.3f, %4.3f)",
					ioReg.comment, defToolTip.x, defToolTip.y, defToolTip.z); 
			
			lines.add(new DisplayLine(idx, 0, lineStr));
		}
		
		return lines;
	}
	
	/**
	 * TODO
	 * 
	 * @param r
	 * @param coordFrame
	 * @param fdx
	 * @return
	 */
	public ArrayList<DisplayLine> loadFrameDetail(RoboticArm r, CoordFrame coordFrame, int fdx) {
		
		ArrayList<DisplayLine> lines = new ArrayList<>();
		Frame f = null;
		
		if (coordFrame == CoordFrame.TOOL) {
			f = r.getToolFrame(fdx);
			
		} else if (coordFrame == CoordFrame.USER) {
			f = r.getUserFrame(fdx);
		}
		
		if (f != null) {
			String[] fields = f.toLineStringArray();
			
			for (String field : fields) {
				lines.add(new DisplayLine(-1, 0, field));
			}
			
		} else {
			// Invalid coordFrame or frame index
			lines.add(new DisplayLine(-1, 0, String.format("CoordFrame=%s", coordFrame) ));
			lines.add(new DisplayLine(-1, 0, String.format("Frame Index=%d", fdx) ));
		}
		
		return lines;
	}
	
	/**
	 * TODO
	 * 
	 * @param r
	 * @param coordFrame
	 * @return
	 */
	public ArrayList<DisplayLine> loadFrames(RoboticArm r, CoordFrame coordFrame) {
		ArrayList<DisplayLine> lines = new ArrayList<>();
		
		if (coordFrame == CoordFrame.TOOL) {
			// Display Tool frames
			for (int idx = 0; idx < Fields.FRAME_NUM; idx += 1) {
				// Display each frame on its own line
				String[] strArray = r.getToolFrame(idx).toLineStringArray();
				String line = String.format("%-4s %s", String.format("%d)",
						idx + 1), strArray[0]);
				
				lines.add(new DisplayLine(idx, 0, line));
				lines.add(new DisplayLine(idx, 38, String.format("%s",
						strArray[1])));
			}

		} else {
			// Display User frames
			for (int idx = 0; idx < Fields.FRAME_NUM; idx += 1) {
				// Display each frame on its own line
				String[] strArray = r.getUserFrame(idx).toLineStringArray();
				String line = String.format("%-4s %s", String.format("%d)",
						idx + 1), strArray[0]);
				
				lines.add(new DisplayLine(idx, 0, line));
				lines.add(new DisplayLine(idx, 38, String.format("%s", strArray[1])));
			}
		}
		
		return lines;
	}
	
	// prepare for displaying motion instructions on screen
	public ArrayList<DisplayLine> loadInstructions(Program p, boolean includeEND) {
		ArrayList<DisplayLine> instruct_list = new ArrayList<>();
		int tokenOffset = Fields.TXT_PAD - Fields.PAD_OFFSET;
		
		int size = p.getNumOfInst();
		
		for (int i = 0; i < size; i += 1) {
			DisplayLine line = new DisplayLine(i);
			Instruction instr = p.get(i);
			int xPos = 10;

			// Add line number
			if (instr == null) {
				line.add(String.format("%d) ...", i + 1));
				instruct_list.add(line);
				continue;
			} else if (instr.isCommented()) {
				line.add("//" + Integer.toString(i + 1) + ")");
			} else {
				line.add(Integer.toString(i + 1) + ")");
			}

			int numWdth = line.get(line.size() - 1).length();
			xPos += numWdth * Fields.CHAR_WDTH + tokenOffset;
			
			if (instr instanceof MotionInstruction) {
				Boolean isRobotAt = mInstRobotAt.get(new Integer(i));
				
				if (isRobotAt != null && isRobotAt) {
					line.add("@");
					
				} else {
					// Add a placeholder for the '@' symbol
					line.add("\0");
				}
				
				xPos += Fields.CHAR_WDTH + tokenOffset;
			}
			
			String[] fields = instr.toStringArray();

			for (int j = 0; j < fields.length; j += 1) {
				String field = fields[j];
				xPos += field.length() * Fields.CHAR_WDTH + tokenOffset;

				if (field.equals("\n") && j != fields.length - 1) {
					instruct_list.add(line);
					if (instr instanceof SelectStatement) {
						xPos = 11 * Fields.CHAR_WDTH + 3 * tokenOffset;
					} else {
						xPos = 3 * Fields.CHAR_WDTH + 3 * tokenOffset;
					}

					line = new DisplayLine(i, xPos);
					xPos += field.length() * Fields.CHAR_WDTH + tokenOffset;
				} else if (xPos > Fields.PENDANT_SCREEN_WIDTH - 10) {
					instruct_list.add(line);
					xPos = 2 * Fields.CHAR_WDTH + tokenOffset;

					line = new DisplayLine(i, xPos);
					field = ": " + field;
					xPos += field.length() * Fields.CHAR_WDTH + tokenOffset;
				}

				if (!field.equals("\n")) {
					line.add(field);
				}
			}

			instruct_list.add(line);
		}
		
		if (includeEND) {
			DisplayLine endl = new DisplayLine(size);
			endl.add("[End]");
	
			instruct_list.add(endl);
		}
		
		return instruct_list;
	}

	/**
	 * TODO
	 * 
	 * @param r
	 * @return
	 */
	public ArrayList<DisplayLine> loadIORegInst(RoboticArm r) {
		ArrayList<DisplayLine> lines = new ArrayList<>();
		
		for (int idx = 1; idx <= r.numOfEndEffectors(); idx += 1) {
			IORegister ioReg = r.getIOReg(idx);
			
			String col0 = String.format("IO[%2d:%-10s] = ", idx,
					ioReg.comment);
			lines.add(new DisplayLine(idx, 0, col0, "ON", "OFF"));
		}
		
		return lines;
	}

	/**
	 * TODO
	 * 
	 * @param r
	 * @return
	 */
	public ArrayList<DisplayLine> loadIORegNav(RoboticArm r) {
		ArrayList<DisplayLine> lines = new ArrayList<>();
		
		for (int idx = 1; idx <= r.numOfEndEffectors(); ++idx) {
			IORegister ioReg = r.getIOReg(idx);
			String col0 = String.format("IO[%2d:%-10s] = ", idx,
					ioReg.comment);
			lines.add(new DisplayLine(idx, 0, col0, (ioReg.state == 0) ?
					"OFF" : "ON") );
		}
		
		return lines;
	}

	public ArrayList<DisplayLine> loadMacros() {
		ArrayList<DisplayLine> disp = new ArrayList<DisplayLine>();
		
		for (int i = 0; i < macros.size(); i += 1) {
			String[] strArray = macros.get(i).toStringArray();
			disp.add(new DisplayLine(i, Integer.toString(i + 1), strArray[0], strArray[1], strArray[2]));
		}
		
		return disp;
	}
	
	public ArrayList<DisplayLine> loadManualFunct() {
		ArrayList<DisplayLine> disp = new ArrayList<DisplayLine>();
		int macroNum = 0;

		for (int i = 0; i < macros.size(); i += 1) {
			if (macros.get(i).isManual()) {
				macroNum += 1;
				String manFunct = macros.get(i).toString();
				disp.add(new DisplayLine(macroNum, macroNum + " " + manFunct));
			}
		}
		
		return disp;
	}

	/**
	 * TODO
	 * 
	 * @param f
	 * @param teachMethod
	 * @return
	 */
	public ArrayList<DisplayLine> loadPointList(Frame f, int teachMethod) {
		ArrayList<DisplayLine> lines = new ArrayList<>();
		boolean validMethod = teachMethod == 0 || teachMethod == 1;
		
		
		if (f instanceof ToolFrame && validMethod) {
			
			String out = (f.getPoint(0) == null) ? "UNINIT" : "RECORDED";
			lines.add(new DisplayLine(0, 0, "First Approach Point: " + out));
			
			out = (f.getPoint(1) == null) ? "UNINIT" : "RECORDED";
			lines.add(new DisplayLine(1, 0, "Second Approach Point: " + out));
			
			out = (f.getPoint(2) == null) ? "UNINIT" : "RECORDED";
			lines.add(new DisplayLine(2, 0, "Third Approach Point: " + out));
			
			if (teachMethod == 1) {
				out = (f.getPoint(3) == null) ? "UNINIT" : "RECORDED";
				lines.add(new DisplayLine(3, 0, "Orient Origin Point: " + out));
				
				out = (f.getPoint(4) == null) ? "UNINIT" : "RECORDED";
				lines.add(new DisplayLine(4, 0, "X Axis Point: " + out));
				
				out = (f.getPoint(5) == null) ? "UNINIT" : "RECORDED";
				lines.add(new DisplayLine(5, 0, "Y Axis Point: " + out));
			}
			
		} else if (f instanceof UserFrame && validMethod) {
			
			String out = (f.getPoint(0) == null) ? "UNINIT" : "RECORDED";
			lines.add(new DisplayLine(0, 0, "Orient Origin Point: " + out));
			
			out = (f.getPoint(1) == null) ? "UNINIT" : "RECORDED";
			lines.add(new DisplayLine(1, 0, "X Axis Point: " + out));
			
			out = (f.getPoint(2) == null) ? "UNINIT" : "RECORDED";
			lines.add(new DisplayLine(2, 0, "Y Axis Point: " + out));
			
			if (teachMethod == 1) {
				out = (f.getPoint(3) == null) ? "UNINIT" : "RECORDED";
				lines.add(new DisplayLine(3, 0, "Origin: " + out));
			}
			
		} else {
			lines.add(new DisplayLine(-1, 0,
					(f == null) ? "Null frame" : f.getClass().toString())
					);
			lines.add(new DisplayLine(-1, 0, String.format("Method: %d",
					teachMethod)));
		}
		
		return lines;
	}

	/**
	 * TODO
	 * 
	 * @param r
	 * @return
	 */
	public ArrayList<DisplayLine> loadPositionRegisters(RoboticArm r) {
		ArrayList<DisplayLine> lines = new ArrayList<>();
		
		// Display a subset of the list of registers
		for (int idx = 0; idx < Fields.DPREG_NUM; ++idx) {
			PositionRegister reg = r.getPReg(idx);
			// Display the comment associated with a specific Register entry
			String regLbl = reg.toStringWithComm();
			// Display Register edit prompt (* if uninitialized)
			String regEntry = (reg.point == null) ? "*" : "...Edit...";
			
			lines.add( new DisplayLine(idx, 0, regLbl, regEntry) );
		}
		
		return lines;
	}
	
	/**
	 * TODO comment
	 * 
	 * @param rid
	 * @return
	 */
	public ArrayList<DisplayLine> loadPrograms(RoboticArm r) {
		ArrayList<DisplayLine> progList = null;
		
		if (r != null) {
			progList = new ArrayList<>();
			// Get a list of program names for the given robot
			for (int idx = 0; idx < r.numOfPrograms(); ++idx) {
				DisplayLine line = new DisplayLine(idx, 0,
						r.getProgram(idx).getName());
				progList.add(line);
			}
			
		}
		
		return progList;
	}

	/**
	 * Build a PShape object from the contents of the given .stl source file
	 * stored in /RobotRun/data/.
	 * 
	 * @throws NullPointerException
	 *             if the given filename does not pertain to a valid .stl file
	 *             located in RobotRun/data/
	 * @throws ClassCastException
	 * 				if the application does not use processing's opengl
	 * 				graphics library
	 */
	public MyPShape loadSTLModel(String filename, int fill) throws NullPointerException, ClassCastException {
		ArrayList<Triangle> triangles = new ArrayList<>();
		byte[] data = loadBytes(filename);

		int n = 84; // skip header and number of triangles

		while (n < data.length) {
			Triangle t = new Triangle();
			for (int m = 0; m < 4; m++) {
				byte[] bytesX = new byte[4];
				bytesX[0] = data[n + 3];
				bytesX[1] = data[n + 2];
				bytesX[2] = data[n + 1];
				bytesX[3] = data[n];
				n += 4;
				byte[] bytesY = new byte[4];
				bytesY[0] = data[n + 3];
				bytesY[1] = data[n + 2];
				bytesY[2] = data[n + 1];
				bytesY[3] = data[n];
				n += 4;
				byte[] bytesZ = new byte[4];
				bytesZ[0] = data[n + 3];
				bytesZ[1] = data[n + 2];
				bytesZ[2] = data[n + 1];
				bytesZ[3] = data[n];
				n += 4;
				t.components[m] = new PVector(ByteBuffer.wrap(bytesX).getFloat(), ByteBuffer.wrap(bytesY).getFloat(),
						ByteBuffer.wrap(bytesZ).getFloat());
			}
			triangles.add(t);
			n += 2; // skip meaningless "attribute byte count"
		}
		
		MyPShape mesh = new MyPShape((PGraphicsOpenGL)getGraphics(), PShape.GEOMETRY);
		mesh.beginShape(TRIANGLES);
		mesh.noStroke();
		mesh.fill(fill);
		
		for (Triangle t : triangles) {
			mesh.normal(t.components[0].x, t.components[0].y, t.components[0].z);
			mesh.vertex(t.components[1].x, t.components[1].y, t.components[1].z);
			mesh.vertex(t.components[2].x, t.components[2].y, t.components[2].z);
			mesh.vertex(t.components[3].x, t.components[3].y, t.components[3].z);
		}
		
		mesh.endShape();

		return mesh;
	}
	
	public void mouseDragged(MouseEvent e) {
		WorldObject selectedWO = UI.getSelectedWO();
		
		// Manipulate the selected world object
		if (selectedWO != null && selectedWO == mouseOverWO) {
			PVector camOrien = camera.getOrientation();
			
			pushMatrix();
			resetMatrix();
			rotateX(camOrien.x);
			rotateY(camOrien.y);
			rotateZ(camOrien.z);
			
			RMatrix camRMat = getOrientation();
			
			popMatrix();
			
			if (!mouseDragWO && (mouseButton == CENTER || mouseButton == RIGHT)) {
				// Save the selected world object's current state
				SCENARIO_UNDO.push(selectedWO.clone());
			}
			
			mouseDragWO = true;
			
			if (mouseButton == CENTER) {
				// Drag the center mouse button to move the object
				PVector translation = new PVector(
						camera.getScale() * (mouseX - pmouseX),
						camera.getScale() * (mouseY - pmouseY),
						0f
				);
				
				/* Translate the world object with respect to the native
				 * coordinate system */
				translation = RMath.rotateVector(translation, camRMat);
				
				selectedWO.translate(translation.x, translation.y, translation.z);
			}
	
			if (mouseButton == RIGHT) {
				// Drag the right mouse button to rotate the object
				float mouseXDiff = mouseX - pmouseX;
				float mouseYDiff = mouseY - pmouseY;
				float mouseDiff = (float) Math.sqrt(mouseXDiff * mouseXDiff + mouseYDiff * mouseYDiff);
				float angle = RobotRun.DEG_TO_RAD * mouseDiff / 4f;
				
				/* Form an axis perpendicular to the line formed by the previous
				 * and current mouse position to use as the axis of rotation. */
				PVector m = new PVector(mouseX - pmouseX, mouseY - pmouseY, 0f);
				PVector n = new PVector(0, 0, 1f);
				PVector axis = RMath.rotateVector(n.cross(m).normalize(), camRMat);
				
				selectedWO.rotateAroundAxis(axis, angle);
			}
			
			UI.fillCurWithCur(selectedWO);
			
			/* If the edited object is a fixture, then update the orientation
			 * of all parts, which reference this fixture, in this scenario. */
			if (selectedWO instanceof Fixture) {
				for (WorldObject wldObj : getActiveScenario()) {
					if (wldObj instanceof Part) {
						Part p = (Part)wldObj;

						if (p.getFixtureRef() == selectedWO) {
							p.updateAbsoluteOrientation();
						}
					}
				}
			}
			
		// Manipulate the camera otherwise
		} else {
			
			if (mouseButton == CENTER) {
				// Drag the center mouse button to pan the camera
				camera.translate(mouseX - pmouseX, mouseY - pmouseY, 0f);
			}
	
			if (mouseButton == RIGHT) {
				// Drag right mouse button to rotate the camera
				camera.rotate(mouseY - pmouseY, mouseX - pmouseX, 0f);
			}
		}
	}
	
	@Override
	public void mousePressed() {
		/* Check if the mouse position is colliding with a world object */
		if (!isProgExec() && !UI.isFocus() && activeRobot != null &&
				activeScenario != null) {
			
			// Scale the camera and mouse positions
			
			PVector camPos = camera.getPosition();
			camPos.x += width / 2f * camera.getScale();
			camPos.y += height / 2f * camera.getScale();
			
			PVector camOrien = camera.getOrientation();
			
			PVector mScreenPos = new PVector(mouseX, mouseY, camPos.z + 1500f);
			mScreenPos.x *= camera.getScale();
			mScreenPos.y *= camera.getScale();
			
			PVector mWorldPos, ptOnMRay;
			
			pushMatrix();
			resetMatrix();
			// Apply the inverse of the camera's coordinate system
			rotateZ(-camOrien.z);
			rotateY(-camOrien.y);
			rotateX(-camOrien.x);
			translate(-camPos.x, -camPos.y, -camPos.z);
			
			translate(mScreenPos.x, mScreenPos.y, mScreenPos.z);
			
			/* Form a ray pointing out of the screen's z-axis, in the
			 * native coordinate system */
			mWorldPos = getPosFromMatrix(0f, 0f, 0f);
			ptOnMRay = getPosFromMatrix(0f, 0f, -1f);
			
			popMatrix();
			// Set the mouse ray origin and direction
			RRay mouseRay = new RRay(mWorldPos, ptOnMRay, 10000f, Fields.BLACK);
			
			// Check for collisions with objects in the scene
			WorldObject collision = checkForCollisionsInScene(mouseRay);
			
			if (mouseButton == LEFT) {
				UI.setSelectedWO(collision);
			}
			
			mouseOverWO = collision;
			
		} else {
			mouseOverWO = null;
		}
	}
	
	@Override
	public void mouseReleased() {
		mouseOverWO = null;
		mouseDragWO = false;
	}
	
	@Override
	public void mouseWheel(MouseEvent event) {
		if (UI != null && UI.isMouseOverUIElement()) {
			// Disable zooming when selecting an element from a dropdown list
			return;
		}

		float wheelCount = event.getCount();
		/* Control scaling of the camera with the mouse wheel */
		if (wheelCount > 0) {
			camera.scale(1.05f);
			
		} else if (wheelCount < 0) {
			camera.scale(0.95f);
		}
	}
	
	public void newCallInstruction() {
		RoboticArm r = activeRobot;
		Program p = getActiveProg();
		CallInstruction call = new CallInstruction(activeRobot);

		if (getActiveInstIdx() != p.getNumOfInst()) {
			r.replaceInstAt(p, getActiveInstIdx(), call);
		} else {
			p.addInstAtEnd(call);
		}
	}

	public void newFrameInstruction(int fType) {
		RoboticArm r = activeRobot;
		Program p = getActiveProg();
		FrameInstruction f = new FrameInstruction(fType, -1);

		if (getActiveInstIdx() != p.getNumOfInst()) {
			r.replaceInstAt(p, getActiveInstIdx(), f);
		} else {
			p.addInstAtEnd(f);
		}
	}

	public void newIfExpression() {
		RoboticArm r = activeRobot;
		Program p = getActiveProg();
		IfStatement stmt = new IfStatement();

		if (getActiveInstIdx() != p.getNumOfInst()) {
			r.replaceInstAt(p, getActiveInstIdx(), stmt);
		} else {
			p.addInstAtEnd(stmt);
		}
	}

	public void newIfStatement() {
		RoboticArm r = activeRobot;
		Program p = getActiveProg();
		IfStatement stmt = new IfStatement(Operator.EQUAL, null);
		opEdit = stmt.getExpr();
		
		if (getActiveInstIdx() != p.getNumOfInst()) {
			r.replaceInstAt(p, getActiveInstIdx(), stmt);
		} else {
			p.addInstAtEnd(stmt);
		}
	}
	
	public void newIOInstruction(int ioIdx, int state) {
		RoboticArm r = activeRobot;
		Program p = getActiveProg();
		IOInstruction io = new IOInstruction(ioIdx, state);

		if (getActiveInstIdx() != p.getNumOfInst()) {
			r.replaceInstAt(p, getActiveInstIdx(), io);

		} else {
			p.addInstAtEnd(io);
		}
	}

	public void newJumpInstruction() {
		RoboticArm r = activeRobot;
		Program p = getActiveProg();
		JumpInstruction j = new JumpInstruction(-1);

		if (getActiveInstIdx() != p.getNumOfInst()) {
			r.replaceInstAt(p, getActiveInstIdx(), j);
		} else {
			p.addInstAtEnd(j);
		}
	}

	public void newLabel() {
		RoboticArm r = activeRobot;
		Program p = getActiveProg();

		LabelInstruction l = new LabelInstruction(-1);
		
		if (getActiveInstIdx() != p.getNumOfInst()) {
			r.replaceInstAt(p, getActiveInstIdx(), l);
		} else {
			p.addInstAtEnd(l);
		}
	}

	/**
	 * Adds a new position to the active program representing the active robot's
	 * current position and orientation. In addition, the active instruction of
	 * the active program is overridden with a new motion instruction. If the
	 * override instruction is a motion instruction, then the current motion
	 * instruction will simply be updated.
	 */
	public void newPosMotionInst() {
		Program prog = getActiveProg();
		
		Point pt = activeRobot.getToolTipUser();
		
		Instruction activeInst = getActiveInstruction();
		PosMotionInst mInst;
		int regNum = prog.getNextPosition();
		
		if (activeInst instanceof PosMotionInst) {
			// Edit a pre-existing motion instruction
			mInst = (PosMotionInst)activeInst;
			
		} else {
			mInst = new PosMotionInst();
			
			if (getActiveInstIdx() != prog.getNumOfInst()) {
				// Overwrite an existing non-motion instruction
				activeRobot.replaceInstAt(prog, getActiveInstIdx(), mInst);
			} 
			else {
				// Insert the new motion instruction
				getActiveProg().addInstAt(prog.getNumOfInst(), mInst);
			}
		}
		
		// Set the fields of the motion instruction
		
		CoordFrame coord = activeRobot.getCurCoordFrame();
		
		if (coord == CoordFrame.JOINT) {
			mInst.setMotionType(Fields.MTYPE_JOINT);
			
		} 
		else {
			/*
			 * Keep circular motion instructions as circular motion
			 * instructions in world, tool, or user frame modes
			 */
			if (mInst.getMotionType() == Fields.MTYPE_JOINT) {
				mInst.setMotionType(Fields.MTYPE_LINEAR);
			}
		}
		
		if (mInst.getPosType() == Fields.PTYPE_PREG) {
			PositionRegister pReg = activeRobot.getPReg(regNum);
			pReg.point = pt;
			
		}  else if (mInst.getPosType() == Fields.PTYPE_PROG) {
			prog.setPosition(regNum, pt);
			
			if (curScreen.getContents().getItemLineIdx() > 0) {
				mInst.setCircPosIdx(regNum);
				
			} else {
				mInst.setPosIdx(regNum);
			}
		}
		
		mInst.setSpdMod(0.5f);
		mInst.setTFrameIdx(activeRobot.getActiveToolIdx());
		mInst.setUFrameIdx(activeRobot.getActiveUserIdx());
	}

	public void newRegisterStatement(Register reg) {
		RoboticArm r = activeRobot;
		Program p = getActiveProg();
		RegisterStatement stmt = new RegisterStatement(reg);

		if (getActiveInstIdx() != p.getNumOfInst()) {
			r.replaceInstAt(p, getActiveInstIdx(), stmt);
		} else {
			p.addInstAtEnd(stmt);
		}
	}

	public void newRegisterStatement(Register reg, int i) {
		RoboticArm r = activeRobot;
		Program p = getActiveProg();
		RegisterStatement stmt = new RegisterStatement(reg, i);

		if (getActiveInstIdx() != p.getNumOfInst()) {
			r.replaceInstAt(p, getActiveInstIdx(), stmt);
		} else {
			p.addInstAtEnd(stmt);
		}
	}

	public void newRobotCallInstruction() {
		RoboticArm r = activeRobot;
		Program p = getActiveProg();
		CallInstruction rcall = new CallInstruction(getInactiveRobot());

		if (getActiveInstIdx() != p.getNumOfInst()) {
			r.replaceInstAt(p, getActiveInstIdx(), rcall);
		} else {
			p.addInstAtEnd(rcall);
		}
	}

	public void newSelectStatement() {
		RoboticArm r = activeRobot;
		Program p = getActiveProg();
		SelectStatement stmt = new SelectStatement();

		if (getActiveInstIdx() != p.getNumOfInst()) {
			r.replaceInstAt(p, getActiveInstIdx(), stmt);
		} else {
			p.addInstAtEnd(stmt);
		}
	}

	/**
	 * Updates the save state of the active screen and loads the given screen
	 * mode afterwards.
	 * 
	 * @param nextScreen	The new screen mode
	 */
	public void nextScreen(ScreenMode nextScreen) {
		Fields.debug("\n%s => %s\n", curScreen.mode, nextScreen);
		
		curScreen = Screen.getScreen(nextScreen, this);
		System.out.println("Loaded screen " + nextScreen.name());
		
		// Give the previous program navigation screen to the option screens
		if (nextScreen == ScreenMode.CONFIRM_INSERT || nextScreen == ScreenMode.SELECT_INSTR_DELETE
				|| nextScreen == ScreenMode.CONFIRM_RENUM || nextScreen == ScreenMode.SELECT_COMMENT
				|| nextScreen == ScreenMode.SELECT_PASTE_OPT || nextScreen == ScreenMode.FIND_REPL
				|| nextScreen == ScreenMode.SELECT_CUT_COPY) {
			
			System.out.printf("\nStack: %d\n", screenStack.size());
			
			if (screenStack.size() > 2) {
				// Find the program navigation screen
				Screen prevScreen = screenStack.get( screenStack.size() - 2 );
				System.out.println(prevScreen.mode);
				
				if (prevScreen.mode == ScreenMode.NAV_PROG_INSTR) {
					System.out.printf("HERE\n\n");
					curScreen.updateScreen(prevScreen.getScreenState());
				}
			}
			
		} else {
			curScreen.updateScreen(screenStack.peek().getScreenState());
		}
		
		pushActiveScreen();
		updatePendantScreen();
	}
	
	public void pasteInstructions() {
		pasteInstructions(0);
	}

	public void pasteInstructions(int options) {
		ArrayList<Instruction> pasteList = new ArrayList<>();
		Program p = getActiveProg();

		/* Pre-process instructions for insertion into program. */
		for (int i = 0; i < clipBoard.size(); i += 1) {
			Instruction instr = clipBoard.get(i).clone();

			if (instr instanceof PosMotionInst) {
				PosMotionInst m = (PosMotionInst) instr;

				if ((options & Fields.CLEAR_POSITION) == Fields.CLEAR_POSITION) {
					m.setPosIdx(-1);
				} else if ((options & Fields.NEW_POSITION) == Fields.NEW_POSITION) {
					/*
					 * Copy the current instruction's position to a new local
					 * position index and update the instruction to use this new
					 * position
					 */
					int instrPos = m.getPosIdx();
					int nextPos = p.getNextPosition();

					p.addPosition(p.getPosition(instrPos).clone());
					m.setPosIdx(nextPos);
				}

				if ((options & Fields.REVERSE_MOTION) == Fields.REVERSE_MOTION) {
					MotionInstruction next = null;

					for (int j = i + 1; j < clipBoard.size(); j += 1) {
						if (clipBoard.get(j) instanceof MotionInstruction) {
							next = (MotionInstruction) clipBoard.get(j).clone();
							break;
						}
					}

					if (next != null) {
						println("asdf");
						m.setMotionType(next.getMotionType());
						m.setSpdMod(next.getSpdMod());
					}
				}
			}

			pasteList.add(instr);
		}

		/* Perform forward/ reverse insertion. */
		for (int i = 0; i < clipBoard.size(); i += 1) {
			Instruction instr;
			if ((options & Fields.PASTE_REVERSE) == Fields.PASTE_REVERSE) {
				instr = pasteList.get(pasteList.size() - 1 - i);
			} else {
				instr = pasteList.get(i);
			}
			
			p.addInstAt(getActiveInstIdx() + i, instr);
		}
	}


	/**
	 * Pushes a screen on the stack to allow us to return to the screen later.
	 */
	public void pushScreen(Screen s) {
		screenStack.push(s);
	}
	
	/**
	 * Clears the screen state stack and sets the default screen as the active
	 * screen.
	 */
	public void resetStack() {
		// Stop a program from executing when transition screens
		screenStack.clear();
		curScreen = Screen.getScreen(ScreenMode.DEFAULT, this);
		pushScreen(curScreen);
	}
	
	/**
	 * Determines a integer state based on the active instruction and selected
	 * index in a program instruction menu.
	 * 
	 * @return	6 offset position register
	 * 			4 secondary point referencing a position register,
	 * 			3 secondary point referencing a position,
	 * 			2 primary point referencing a position register,
	 * 			1 primary point referencing a position,
	 * 			0 anything else
	 */
	public int selectedMInstRegState() {
		if (curScreen instanceof ScreenNavProgInstructions) {
			Instruction inst = getActiveInstruction();
			
			if (inst instanceof PosMotionInst) {
				PosMotionInst mInst = (PosMotionInst)inst;
				int sdx = curScreen.getContents().getItemColumnIdx();
				
				if (sdx == 3 || sdx == 4) {
					// Primary register is selected
					
					if (mInst.getPosType() == Fields.PTYPE_PREG) {
						return 2;
						
					} else if (mInst.getPosType() == Fields.PTYPE_PROG) {
						return 1;
					}
					
				} else if (mInst.getOffsetType() == Fields.OFFSET_NONE) {
					if (sdx == 8 || sdx == 9) {
						// Secondary register is selected
						if (mInst.getCircPosType() == Fields.PTYPE_PREG) {
							return 4;
							
						} else if (mInst.getCircPosType() == Fields.PTYPE_PROG) {
							return 3;
						}
					}
					
				} else if (mInst.getOffsetType() == Fields.OFFSET_PREG) {
					
					if (sdx == 7 || sdx == 8) {
						// Offset position register selected
						return 6;
						
					} else if (sdx == 9 || sdx == 10) {
						// Secondary register is selected
						if (mInst.getCircPosType() == Fields.PTYPE_PREG) {
							return 4;
							
						} else if (mInst.getCircPosType() == Fields.PTYPE_PROG) {
							return 3;
						}
					}
				}
			}
		}
		
		return 0;
	}
	
	/**
	 * Sets the active instruction of the active program corresponding to the
	 * index given.
	 * 
	 * @param instIdx	The index of the instruction to set as active
	 * @return			Whether an active program exists and the given index is
	 * 					valid for the active program
	 */
	public boolean setActiveInstIdx(int instIdx) {
		Program prog = getActiveProg();
		
		if (prog != null && instIdx >= 0 && instIdx <= prog.getNumOfInst()) {
			// Set the active instruction
			progExecState.setCurIdx(instIdx);
			return true;
		}
		
		return false;
	}
	
	/**
	 * Sets the given program as the robot's active program if the program
	 * exists in the robot's list of programs. Otherwise, the robot's active
	 * program remains unchanged.
	 * 
	 * @param active	The program to set as active
	 * @return			If the program exists in the robot's list of programs
	 */
	public boolean setActiveProg(Program active) {
		int progIdx = activeRobot.getProgIdx(active.getName());
		boolean exists = progIdx > 0;
		
		if (exists || progIdx == -1) {
			progExecState.setProgIdx(progIdx);
		}
		
		return exists;
	}
	
	/**
	 * Sets the active program of this Robot corresponding to the index value
	 * given.
	 * 
	 * @param progIdx	The index of the program to set as active
	 * @return			Whether the given index is valid
	 */
	public boolean setActiveProgIdx(int progIdx) {
		boolean exists = activeRobot.getProgram(progIdx) != null;
		
		if (exists || progIdx == -1) {
			// Set the active program
			progExecState.setProgIdx(progIdx);
		}
		
		return exists;
	}
	
	/**
	 * Sets the scenario with the given name as the active scenario in the
	 * application, if a scenario with the given name exists.
	 * 
	 * @param name	The name of the scenario to set as active
	 * @return		Whether the scenario with the given name was
	 * 				successfully set as active
	 */
	public boolean setActiveScenario(String name) {

		for (Scenario s : SCENARIOS) {
			if (s.getName().equals(name)) {
				activeScenario = s;
				
				if (UI != null) {
					UI.setSelectedWO(null);
				}
				
				return true;
			}
		}

		return false;

	}
	
	public void setMacroBindings(Macro[] usrKeyBinds) {
		macroKeyBinds = usrKeyBinds;
	}

	public void setRecord(boolean state) {
		record = state;
	}

	/**
	 * Update the active Robot to the Robot at the given index in the list of
	 * Robots.
	 * 
	 * @param rdx
	 *            The index of the new active Robot
	 */
	public void setRobot(int rdx) {
		if (rdx >= 0 && rdx < ROBOTS.size() && ROBOTS.get(rdx) != activeRobot) {
			button_hold();

			RoboticArm prevActive = activeRobot;
			activeRobot = ROBOTS.get(rdx);

			if (prevActive != activeRobot) {
				/*
				 * If the active robot actually changes then resort to the
				 * default screen
				 */
				nextScreen(ScreenMode.DEFAULT);
			}
		}
	}
	
	/**
	 * Update the state of the shift and robot motion based on the new state of
	 * shift.
	 * 
	 * @param flag	The new shift state
	 */
	public void setShift(boolean flag) {
		if (!flag) {
			// Stop all robot motion and program execution
			activeRobot.halt();
			progExecState.halt();
		}

		shift = flag;
		UI.updateShiftButton(shift);
		updatePendantScreen();
	}

	public void setStep(boolean step) {
		this.step = step;
		UI.updateStepButton(this.step);
	}
	
	@Override
	public void settings() {
		size(1080, 720, P3D);
	}
	
	@Override
	public void setup() {
		super.setup();
		
		mouseOverWO = null;
		mouseDragWO = false;
		
		PImage[][] buttonImages = new PImage[][] {
			
			{ loadImage("images/arrow-up.png"), loadImage("images/arrow-up_over.png"), loadImage("images/arrow-up_down.png") },
			{ loadImage("images/arrow-down.png"), loadImage("images/arrow-down_over.png"), loadImage("images/arrow-down_down.png") },
			{ loadImage("images/arrow-l.png"), loadImage("images/arrow-l_over.png"), loadImage("images/arrow-l_down.png") },
			{ loadImage("images/arrow-r.png"), loadImage("images/arrow-r_over.png"), loadImage("images/arrow-r_down.png") }
			
		};
		
		instance = this;
		
		RegisteredModels.loadModelDefs();
		
		// create font and text display background
		Fields.medium = createFont("fonts/Consolas.ttf", 14);
		Fields.small = createFont("fonts/Consolas.ttf", 12);
		Fields.bond = createFont("fonts/ConsolasBold.ttf", 12);
		
		record = false;
		camera = new Camera();
		activeScenario = null;
		
		background(255);
		
		// load model and save data
		try {
			keyCodeMap = new KeyCodeMap();
			DataManagement.initialize(this);
			
			RoboticArm r = createRobot(0, new PVector(200, Fields.FLOOR_Y, 200));
			ROBOTS.put(r.RID, r);
			
			r = createRobot(1, new PVector(200, Fields.FLOOR_Y, -750));
			ROBOTS.put(r.RID, r);

			activeRobot = ROBOTS.get(0);
			rCamera = new RobotCamera();
			
			activeScenario = null;
			
			DataManagement.loadState(this);
			
			setManager(new WGUI(this, buttonImages));
			
			screenStack = new Stack<>();
			curScreen = Screen.getScreen(ScreenMode.DEFAULT, this);
			pushScreen(curScreen);
			updatePendantScreen();
			
			progExecState = new ProgExecution();
			progCallStack = new Stack<>();
			
			tracePts = new LinkedList<PVector>();
			
			mInstRobotAt = new HashMap<Integer, Boolean>();
			
		} catch (NullPointerException NPEx) {
			DataManagement.errLog(NPEx);
			throw NPEx;
		}
		
		/**
		RMatrix rx = RMath.formRMat(new PVector(1f, 0f, 0f), 135f * DEG_TO_RAD);
		RMatrix ry = RMath.formRMat(new PVector(0f, 1f, 0f), 135f * DEG_TO_RAD);
		RMatrix rz = RMath.formRMat(new PVector(0f, 0f, 1f), 135f * DEG_TO_RAD);
		
		System.out.printf("%s\n%s\n%s\n", rx, ry, rz);
		/**/
	}

	/**
	 * Removes the current screen from the screen state stack and loads the
	 * given screen mode.
	 * 
	 * @param nextScreen	The new screen mode
	 */
	public void switchScreen(ScreenMode nextScreen) {
		screenStack.pop();
		// Load the new screen
		nextScreen(nextScreen);
	}

	/**
	 * Toggle the given robot's state between ON and OFF. Pickup collisions
	 * between this robot and the active scenario are checked based off the
	 * robot's new end effector state.
	 * 
	 * @param robot	The robot for which to change the end effector state
	 */
	public void toggleEEState(RoboticArm robot) {
		int edx = robot.getActiveEEIdx();
		int curState = robot.getEEState();
		
		if (curState == Fields.ON) {
			robot.setEEState(edx, Fields.OFF);
			
		} else {
			robot.setEEState(edx, Fields.ON);
		}
		// Check pickup collisions in active scenario
		robot.checkPickupCollision(activeScenario);
	}

	/**
	 * Is the trace function enabled. The user can enable/disable this function
	 * with a button in the miscellaneous window.
	 * 
	 * @return	If the trace functionality is enabled
	 */
	public boolean traceEnabled() {
		return UI.getButtonState("ToggleTrace");
	}
	
	/**
	 * Revert the most recent change to the active scenario
	 */
	public void undoScenarioEdit() {
		if (!SCENARIO_UNDO.empty()) {
			activeScenario.put( SCENARIO_UNDO.pop() );
			UI.updateListContents();
			WorldObject wo = UI.getSelectedWO();
			
			if (wo != null) {
				UI.updateEditWindowFields(wo);
			}
			
			/* Since objects are copied onto the undo stack, the robot may be
			 * reference the wrong copy of an undone object. */
			activeRobot.releaseHeldObject();
		}
	}

	public void UpdateCam() {
		if (rCamera != null) {
			UI.updateCameraCurrent();
		}
	}

	/**
	 * Deals with updating the UI after confirming/canceling a deletion
	 */
	public void updateInstructions() {
		int instSize = getActiveProg().getNumOfInst();
		setActiveInstIdx(min(getActiveInstIdx(), instSize));
		lastScreen();
	}

	/**
	 * Update the header, contents, options, and function button labels on the
	 * pendant.
	 */
	public void updatePendantScreen() {
		curScreen.updateScreen();
		UI.renderPendantScreen(curScreen);
	}
	
	/**
	 * Updates the end effector state of the active robot's end effector
	 * associated with the given index and checks for pickup collisions
	 * in the active scenario.
	 * 
	 * @param edx		The index of the end effector, of which to change the
	 * 					state
	 * @param newState	The new state of the end effector
	 */
	public void updateRobotEEState(int edx, int newState) {
		updateRobotEEState(activeRobot, edx, newState);
	}
	
	/**
	 * Updates the end effector state of the given robot's end effector
	 * associated with the given index and checks for pickup collisions
	 * in the active scenario.
	 * 
	 * @param r			The robot with which the end effector is associated
	 * @param edx		The index of the end effector, of which to change the
	 * 					state
	 * @param newState	The new state of the end effector
	 */
	public void updateRobotEEState(RoboticArm r, int edx, int newState) {
		r.setEEState(edx, newState);
		
		if (activeScenario != null) {
			r.checkPickupCollision(activeScenario);
		}
	}

	public void updateRobotJogMotion(int set, int direction) {
		if (isShift() && !isProgExec()) {
			boolean robotInMotion = activeRobot.inMotion();
			
			activeRobot.updateJogMotion(set, direction);
			
			if (robotInMotion && !activeRobot.inMotion()) {
				// Robot has stopped moving
				updateInstList();
			}
		}
	}
	
	/**
	 * Push a world object onto the undo stack for world objects.
	 * 
	 * @param saveState
	 *            The world object to save
	 */
	public void updateScenarioUndo(WorldObject saveState) {
		// Only the latest 40 world object save states can be undone
		if (SCENARIO_UNDO.size() >= 40) {
			SCENARIO_UNDO.remove(0);
		}

		SCENARIO_UNDO.push(saveState);
	}
	
	/**
	 * Checks for collisions between the given ray and objects in the scene
	 * (parts and fixtures in the active scenario as well as visible robots).
	 * The world object, closest to the ray, with which the ray collides is
	 * returned, if the closest collision is not with the robot. Otherwise null
	 * is returned.
	 * 
	 * @param ray	The ray, for which to check collisions in the scene
	 * @return		The closest object, with which the ray collided
	 */
	private WorldObject checkForCollisionsInScene(RRay ray) {
		if (UI.getRobotButtonState()) {
			return checkForRayCollisions(ray, activeScenario, ROBOTS.get(0));
			
		} else {
			return checkForRayCollisions(ray, activeScenario, ROBOTS.get(0),
					ROBOTS.get(1));
		}
		
	}
	
	/**
	 * Checks for collisions between the given ray and fixtures and parts in
	 * the given scenario and the given robots). The world object, closest to
	 * the ray, with which the ray collides is returned, if the closest
	 * collision is not with the robot. Otherwise null is returned.
	 * 
	 * @param ray		A ray, for which to check collisions
	 * @param scenario	The scenario, with which to check collisions with the
	 * 					given ray
	 * @param robot		The robots, which which to check collisions with the
	 * 					given ray
	 */
	private WorldObject checkForRayCollisions(RRay ray, Scenario scenario,
			RoboticArm... robots) {
		
		PVector closestCollPt = null;
		WorldObject collidedWith = null;
		
		// Check collision with the robots
		for (RoboticArm r : robots) {
			PVector collPt = r.closestCollision(ray);
			
			if (collPt != null && (closestCollPt == null ||
					PVector.dist(ray.getOrigin(), collPt) <
					PVector.dist(ray.getOrigin(), closestCollPt))) {
				
				closestCollPt = collPt;
			}
		}
		
		// Check collision with world objects
		
		for (WorldObject wo : scenario) {
			PVector collPt = wo.collision(ray);
			
			if (collPt != null && (closestCollPt == null ||
					PVector.dist(ray.getOrigin(), collPt) <
					PVector.dist(ray.getOrigin(), closestCollPt))) {
				
				if (wo instanceof Fixture) {
					RShape form = wo.getForm();
					
					if (form instanceof ComplexShape) {
						/* Check if the color at the mouse position matches
						 * the model's fill color. */
						int fill = form.getFillValue();
						int pixel = get(mouseX, mouseY);
						
						if (Fields.colorDiff(pixel, fill) < 200) {
							collidedWith = wo;
							closestCollPt = collPt;
						}
						
					} else {
						// Fixture with a non-model shape
						collidedWith = wo;
						closestCollPt = collPt;
					}
					
				} else {
					// Part
					collidedWith = wo;
					closestCollPt = collPt;
				}
			}
		}
		
		return collidedWith;
	}
	
	/**
	 * Creates a robot with the given id and base position and initializes all
	 * its segment and end effector data.
	 * 
	 * @param rid			The id of the robot, which must be unique amongst
	 * 						all other robots
	 * @param basePosition	The position of the robot's base segment
	 * @return				The initialized robot
	 */
	private RoboticArm createRobot(int rid, PVector basePosition) {
		MyPShape[] segModels = new MyPShape[6];
		MyPShape[] eeModels = new MyPShape[7];
		
		segModels[0] = loadSTLModel("robot/ROBOT_BASE.STL", color(200, 200, 0));
		segModels[1] = loadSTLModel("robot/ROBOT_SEGMENT_1.STL", color(40, 40, 40));
		segModels[2] = loadSTLModel("robot/ROBOT_SEGMENT_2.STL", color(200, 200, 0));;
		segModels[3] = loadSTLModel("robot/ROBOT_SEGMENT_3.STL", color(40, 40, 40));
		segModels[4] = loadSTLModel("robot/ROBOT_SEGMENT_4.STL", color(40, 40, 40));
		segModels[5] = loadSTLModel("robot/ROBOT_SEGMENT_5.STL", color(200, 200, 0));
		
		// Load end effector models
		eeModels[0] = loadSTLModel("robot/EE/FACEPLATE.STL", color(40, 40, 40));
		eeModels[1] = loadSTLModel("robot/EE/SUCTION.stl", color(108, 206, 214));
		eeModels[2] = loadSTLModel("robot/EE/GRIPPER.stl", color(108, 206, 214));;
		eeModels[3] = loadSTLModel("robot/EE/PINCER.stl", color(200, 200, 0));
		eeModels[4] = loadSTLModel("robot/EE/POINTER.stl", color(108, 206, 214));
		eeModels[5] = loadSTLModel("robot/EE/GLUE_GUN.stl", color(108, 206, 214));
		eeModels[6] = loadSTLModel("robot/EE/WIELDER.stl", color(108, 206, 214));
		
		return new RoboticArm(rid, basePosition, segModels, eeModels);
	}
	/**
	 * Draws the points stored for this robot's trace function with respect to
	 * the given graphics object's coordinate frame.
	 * 
	 * @param g	The graphics object used to drawn the trace
	 */
	private void drawTrace(PGraphics g) {		
		if (tracePts.size() > 1) {
			PVector lastPt = tracePts.getFirst();
			
			g.pushStyle();
			g.stroke(0);
			g.strokeWeight(3);
			
			for(PVector curPt : tracePts) {
				
				g.line(lastPt.x, lastPt.y, lastPt.z, curPt.x, curPt.y, curPt.z);
				
				lastPt = curPt;
			}
			
			g.popStyle();
		}
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param singleExec
	 */
	private void progExec(boolean singleExec) {
		ExecType pExec = (singleExec) ? ExecType.EXEC_SINGLE
				: ExecType.EXEC_FULL;
		progExecState.setType(pExec);
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param instIdx
	 * @param singleExec
	 */
	@SuppressWarnings("unused")
	private void progExec(int instIdx, boolean singleExec) {
		progExec(progExecState.getProgIdx(), instIdx, singleExec);
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param progIdx
	 * @param instIdx
	 * @param singleExec
	 */
	private void progExec(int progIdx, int instIdx, boolean singleExec) {
		Program p = activeRobot.getProgram(progIdx);
		// Validate active indices
		if (p != null && instIdx >= 0 && instIdx < p.size()) {
			ExecType pExec = (singleExec) ? ExecType.EXEC_SINGLE
					: ExecType.EXEC_FULL;
			
			progExec(activeRobot.RID, progIdx, instIdx, pExec);
		}
	}

	/**
	 * TODO comment this
	 * 
	 * @param progIdx
	 * @param instIdx
	 * @param exec
	 */
	private void progExec(int rid, int progIdx, int instIdx, ExecType exec) {
		progExecState.setExec(rid, exec, progIdx, instIdx);
	}
	
	/**
	 * TODO comment this
	 */
	private void progExecBwd() {
		Program p = getActiveProg();
		
		if (p != null && getActiveInstIdx() >= 1 && getActiveInstIdx() < p.size()) {
			/* The program must have a motion instruction prior to the active
			 * instruction for backwards execution to be valid. */
			Instruction prevInst = p.get(getActiveInstIdx() - 1);
			
			if (prevInst instanceof MotionInstruction) {
				progExec(activeRobot.RID, progExecState.getProgIdx(),
						getActiveInstIdx() - 1, ExecType.EXEC_BWD);
			}
		}
	}
	
	/**
	 * Pushes the active program onto the call stack and resets the active
	 * program and instruction indices.
	 */
	private void pushActiveProg() {
		progCallStack.push(progExecState.clone());
		
		setActiveProgIdx(-1);
		setActiveInstIdx(-1);
	}

	/**
	 * Pushes the current state of the screen, contents, and options fields
	 * onto the screen state stack.
	 */
	private void pushActiveScreen() {
		pushScreen(curScreen);
	}

	/**
	 * Updates the position and orientation of the Robot as well as all the
	 * World Objects associated with the current scenario. Updates the bounding
	 * box color, position and orientation of the Robot and all World Objects as
	 * well. Finally, all the World Objects and the Robot are drawn.
	 */
	private void renderScene() {
		
		if (isProgExec()) {
			updateProgExec();
		}
		
		boolean robotInMotion = activeRobot.inMotion();
		
		activeRobot.updateRobot();
		
		if (robotInMotion && !activeRobot.inMotion()) {
			// Robot has stopped moving
			updateInstList();
		}
		
		if (isProgExec()) {
			updateCurIdx();
		}

		if (activeScenario != null) {
			activeScenario.resetObjectHitBoxColors();
		}

		activeRobot.resetOBBColors();
		activeRobot.checkSelfCollisions();

		if (activeScenario != null) {
			WorldObject selected = UI.getSelectedWO();
			int numOfObjects = activeScenario.size();

			for (int idx = 0; idx < numOfObjects; ++idx) {
				WorldObject wldObj = activeScenario.getWorldObject(idx);
				
				if (wldObj instanceof Part) {
					Part p = (Part)wldObj;

					/* Update the transformation matrix of an object held by the Robotic Arm */
					if(activeRobot != null && activeRobot.isHeld(p) && activeRobot.inMotion()) {
						
						/***********************************************
						     Moving a part with the Robot:
						
						     P' = R^-1 x E' x E^-1 x P
						
						     where:
						     P' - new part local orientation
						     R  - part fixture reference orientation
						     E' - current Robot end effector orientation
						     E  - previous Robot end effector orientation
						     P  - current part local orientation
						 ***********************************************/
						
						RMatrix curTip = activeRobot.getFaceplateTMat(activeRobot.getJointAngles());
						RMatrix invMat = activeRobot.getLastTipTMatrix().getInverse();
						Fixture refFixture = p.getFixtureRef();
						
						pushMatrix();
						resetMatrix();
						
						if (refFixture != null) {
							refFixture.removeCoordinateSystem();
						}
						
						applyMatrix(curTip);
						applyMatrix(invMat);
						applyCoord(p.getCenter(), p.getOrientation());
						
						// Update the world object's position and orientation
						p.setLocalCenter( getPosFromMatrix(0f, 0f, 0f) );
						p.setLocalOrientation( getOrientation() );
						
						popMatrix();
					}
					
					
					if (activeScenario.isGravity() && activeRobot.isHeld(p) &&
							p != selected && p.getFixtureRef() == null &&
							p.getLocalCenter().y < Fields.FLOOR_Y) {
						
						// Apply gravity
						PVector c = wldObj.getLocalCenter();
						wldObj.updateLocalCenter(null, c.y + 10, null);
					}

					/* Collision Detection */
					if(isOBBRendered()) {
						if( activeRobot != null && activeRobot.checkCollision(p) ) {
							p.setBBColor(Fields.OBB_COLLISION);
						}

						// Detect collision with other objects
						for(int cdx = idx + 1; cdx < activeScenario.size(); ++cdx) {

							if (activeScenario.getWorldObject(cdx) instanceof Part) {
								Part p2 = (Part)activeScenario.getWorldObject(cdx);

								if(p.collision(p2)) {
									// Change hit box color to indicate Object collision
									p.setBBColor(Fields.OBB_COLLISION);
									p2.setBBColor(Fields.OBB_COLLISION);
									break;
								}
							}
						}

						if (activeRobot != null && !activeRobot.isHeld(p) &&
								activeRobot.canPickup(p)) {
							
							// Change hit box color to indicate tool tip collision
							p.setBBColor(Fields.OBB_HELD);
						}
					}

					if (p == selected) {
						p.setBBColor(Fields.OBB_SELECTED);
					}
				}
				
				// Draw the object
				if (wldObj instanceof Part) {
					((Part)wldObj).draw(getGraphics(), isOBBRendered());
					
				} else {
					wldObj.draw(getGraphics());
				}
			}
		}
		
		activeRobot.updateLastTipTMatrix();
		
		AxesDisplay axesType = getAxesState();
		
		if (axesType != AxesDisplay.NONE &&
			(activeRobot.getCurCoordFrame() == CoordFrame.WORLD
				|| activeRobot.getCurCoordFrame() == CoordFrame.TOOL
				|| (activeRobot.getCurCoordFrame() == CoordFrame.USER
					&& activeRobot.getActiveUser() == null))) {
			
			// Render the world frame
			PVector origin = new PVector(0f, 0f, 0f);
			
			if (axesType == AxesDisplay.AXES) {
				Fields.drawAxes(getGraphics(), origin, Fields.WORLD_AXES_MAT,
						10000f, Fields.BLACK);
				
			} else if (axesType == AxesDisplay.GRID) {
				activeRobot.drawGridlines(getGraphics(), Fields.WORLD_AXES_MAT,
						origin, 35, 100f);
			}
		}
		

		if (UI.getRobotButtonState()) {
			// Draw all robots
			for (RoboticArm r : ROBOTS.values()) {
				
				if (r == activeRobot) {
					// active robot
					r.draw(getGraphics(), isOBBRendered(), axesType);
					
				} else {
					r.draw(getGraphics(), false, AxesDisplay.NONE);
				}
				
			}

		} else {
			// Draw only the active robot
			activeRobot.draw(getGraphics(), isOBBRendered(), axesType);
		}
		
		if (activeRobot.inMotion() && traceEnabled()) {
			Point tipPosNative = activeRobot.getToolTipNative();
			// Update the robots trace points
			if(tracePts.isEmpty()) {
				tracePts.add(tipPosNative.position);
				
			} else {
				PVector lastTracePt = tracePts.getLast();
				
				if (PVector.sub(tipPosNative.position, lastTracePt).mag()
						> 0.5f) {
					
					tracePts.addLast(tipPosNative.position);
				}
			}
			
			if (tracePts.size() > 10000f) {
				// Begin to remove points after the limit is reached
				tracePts.removeFirst();
			}
		}
		
		if (traceEnabled()) {
			drawTrace(g);
		}
		
		/* Render the axes of the selected World Object */
		
		WorldObject wldObj = UI.getSelectedWO();
		
		if (wldObj != null) {
			PVector origin;
			RMatrix orientation;
			
			if (wldObj instanceof Part) {
				origin = ((Part) wldObj).getCenter();
				orientation = ((Part) wldObj).getOrientation();
				
			} else {
				origin = wldObj.getLocalCenter();
				orientation = wldObj.getLocalOrientation();
			}

			Fields.drawAxes(getGraphics(), origin, RMath.rMatToWorld(orientation),
					500f, Fields.BLACK);
		}
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param frame
	 */
	private void renderTeachPoints(Frame frame) {
		int size = 3;

		if (curScreen instanceof ScreenTeach6Pt && teachFrame instanceof ToolFrame) {
			size = 6;
		} else if (curScreen instanceof ScreenTeach4Pt && teachFrame instanceof UserFrame) {
			size = 4;
		}

		for (int idx = 0; idx < size; ++idx) {
			Point pt = teachFrame.getPoint(idx);
			
			if (pt != null) {
				pushMatrix();
				// Applies the point's position
				translate(pt.position.x, pt.position.y, pt.position.z);

				// Draw color-coded sphere for the point
				noFill();
				int pointColor = Fields.color(255, 0, 255);

				if (teachFrame instanceof ToolFrame) {

					if (idx < 3) {
						// TCP teach points
						pointColor = Fields.color(130, 130, 130);
					} else if (idx == 3) {
						// Orient origin point
						pointColor = Fields.color(255, 130, 0);
					} else if (idx == 4) {
						// Axes X-Direction point
						pointColor = Fields.color(255, 0, 0);
					} else if (idx == 5) {
						// Axes Y-Diretion point
						pointColor = Fields.color(0, 255, 0);
					}
				} else if (teachFrame instanceof UserFrame) {

					if (idx == 0) {
						// Orient origin point
						pointColor = Fields.color(255, 130, 0);
					} else if (idx == 1) {
						// Axes X-Diretion point
						pointColor = Fields.color(255, 0, 0);
					} else if (idx == 2) {
						// Axes Y-Diretion point
						pointColor = Fields.color(0, 255, 0);
					} else if (idx == 3) {
						// Axes Origin point
						pointColor = Fields.color(0, 0, 255);
					}
				}

				stroke(pointColor);
				sphere(3);

				popMatrix();
			}
		}
	}
	
	/**
	 * Displays all the windows and the right-hand text display.
	 */
	private void renderUI() {
		hint(DISABLE_DEPTH_TEST);
		noLights();
		
		pushMatrix();
		ortho();
		
		pushStyle();
		textFont(Fields.medium, 14);
		fill(0);
		textAlign(RIGHT, TOP);
		
		int lastTextPositionX = width - 20, lastTextPositionY = 20;
		CoordFrame coord = activeRobot.getCurCoordFrame();
		String coordFrame;
		
		if (coord == null) {
			// Invalid state for coordinate frame
			coordFrame = "Coordinate Frame: N/A";
			
		} else {
			coordFrame = "Coordinate Frame: " + coord.toString();
		}
		
		Point RP = activeRobot.getToolTipNative();

		String[] cartesian = RP.toLineStringArray(true), joints = RP.toLineStringArray(false);
		// Display the current Coordinate Frame name
		text(coordFrame, lastTextPositionX, lastTextPositionY);
		lastTextPositionY += 20;
		// Display the Robot's speed value as a percent
		text(String.format("Jog Speed: %d%%", activeRobot.getLiveSpeed()),
				lastTextPositionX, lastTextPositionY);
		lastTextPositionY += 20;

		if (activeScenario != null) {
			String out = String.format("Active scenario: %s", activeScenario.getName());
			text(out, lastTextPositionX, lastTextPositionY);
			
		} else {
			text("No active scenario", lastTextPositionX, lastTextPositionY);
		}
		lastTextPositionY += 40;
		
		// Display the Robot's current position and orientation in the World
		// frame
		text("Robot Position and Orientation", lastTextPositionX, lastTextPositionY);
		lastTextPositionY += 20;
		text("World", lastTextPositionX, lastTextPositionY);
		lastTextPositionY += 20;

		for (String line : cartesian) {
			text(line, lastTextPositionX, lastTextPositionY);
			lastTextPositionY += 20;
		}

		UserFrame active = activeRobot.getActiveUser();

		if (active != null) {
			// Display Robot's current position and orientation in the currently
			// active User frame
			RP.position = RMath.vToFrame(RP.position, active.getOrigin(), active.getOrientation());
			RP.orientation = active.getOrientation().transformQuaternion(RP.orientation);
			cartesian = RP.toLineStringArray(true);

			lastTextPositionY += 20;
			text(String.format("User: %d", activeRobot.getActiveUserIdx() + 1), lastTextPositionX,
					lastTextPositionY);
			lastTextPositionY += 20;

			for (String line : cartesian) {
				text(line, lastTextPositionX, lastTextPositionY);
				lastTextPositionY += 20;
			}
		}

		lastTextPositionY += 20;
		// Display the Robot's current joint angle values
		text("Joint", lastTextPositionX, lastTextPositionY);
		lastTextPositionY += 20;
		for (String line : joints) {
			text(line, lastTextPositionX, lastTextPositionY);
			lastTextPositionY += 20;
		}

		WorldObject selectedWO = UI.getSelectedWO();
		// Display the position and orientation of the active world object
		if (selectedWO != null) {
			String[] dimFields = selectedWO.dimFieldsToStringArray();
			// Convert the values into the World Coordinate System
			PVector position = RMath.vToWorld(selectedWO.getLocalCenter());
			PVector wpr = RMath.nRMatToWEuler( selectedWO.getLocalOrientation() );
			

			lastTextPositionY += 20;
			text(selectedWO.getName(), lastTextPositionX, lastTextPositionY);
			lastTextPositionY += 20;
			String dimDisplay = "";
			// Display the dimensions of the world object (if any)
			for (int idx = 0; idx < dimFields.length; ++idx) {
				if ((idx + 1) < dimFields.length) {
					dimDisplay += String.format("%-13s", dimFields[idx]);

				} else {
					dimDisplay += String.format("%s", dimFields[idx]);
				}
			}
			
			// Create a set of uniform Strings
			String[] lines = Fields.toLineStringArray(position, wpr);

			text(dimDisplay, lastTextPositionX, lastTextPositionY);
			lastTextPositionY += 20;
			text(lines[0], lastTextPositionX, lastTextPositionY);
			lastTextPositionY += 20;
			text(lines[1], lastTextPositionX, lastTextPositionY);
			lastTextPositionY += 20;

			if (selectedWO instanceof Part) {
				Part p = (Part) selectedWO;
				// Convert the values into the World Coordinate System
				position = RMath.vToWorld( p.getDefaultCenter() );
				wpr = RMath.nRMatToWEuler( p.getDefaultOrientation() );
				
				// Create a set of uniform Strings
				lines = Fields.toLineStringArray(position, wpr);
				
				lastTextPositionY += 20;
				text(lines[0], lastTextPositionX, lastTextPositionY);
				lastTextPositionY += 20;
				text(lines[1], lastTextPositionX, lastTextPositionY);
				lastTextPositionY += 20;
			}
		}
		
		pushStyle();
		fill(215, 0, 0);
		lastTextPositionY += 20;
		
		if (record) {
			text("Recording (press Ctrl + Alt + r)",
					lastTextPositionX, lastTextPositionY);
			lastTextPositionY += 20;
		}

		// Display a message when there is an error with the Robot's
		// movement
		if (activeRobot.hasMotionFault()) {
			text("Motion Fault (press SHIFT + RESET)", lastTextPositionX,
					lastTextPositionY);
			lastTextPositionY += 20;
		}

		// Display a message if the Robot is in motion
		if (activeRobot.inMotion()) {
			text("Robot is moving", lastTextPositionX, lastTextPositionY);
			lastTextPositionY += 20;
		}
		
		if (isProgExec()) {
			text("Program executing", lastTextPositionX, lastTextPositionY);
			lastTextPositionY += 20;
		}

		// Display a message while the robot is carrying an object
		if (!activeRobot.isHeld(null)) {
			text("Object held", lastTextPositionX, lastTextPositionY);
			lastTextPositionY += 20;
		}
		
		popStyle();

		// Display the current axes display state
		text(String.format("Axes Display: %s", getAxesState().name()), lastTextPositionX, height - 50);
		
		UI.updateAndDrawUI();
		
		popStyle();
		popMatrix();
	}
	
	private void setManager(WGUI ui) {
		this.UI = ui;
	}
	
	/**
	 * Detemines if the active menu uses keyboard input.
	 * 
	 * @return
	 */
	private boolean UIKeyboardUse() {
		
		if (UI.isPendantActive() && (curScreen instanceof ST_ScreenTextEntry ||
				curScreen instanceof ST_ScreenPointEntry ||
				curScreen instanceof ST_ScreenNumEntry)) {
			
			return true;
			
		} else if (UI.getMenu() == WindowTab.CREATE || UI.getMenu() == WindowTab.EDIT) {
			return true;
		}
		
		return false;
	}

	private void updateCurIdx() {
		if (progExecState.getState() == ExecState.EXEC_MINST &&
				!activeRobot.inMotion()) {
			
			if (activeRobot.hasMotionFault()) {
				// An issue occurred when running a motion instruction
				progExecState.setState(ExecState.EXEC_FAULT);
				
			} else {
				// Motion instruction has finished execution
				progExecState.setState(ExecState.EXEC_NEXT);
			}
			
		}
		
		Program prog = getActiveProg();
		ExecState state = progExecState.getState();
		int nextIdx = progExecState.getNextIdx();
		
		// Wait until an instruction is complete
		if (state == ExecState.EXEC_NEXT) {
			
			if (nextIdx < 0 || nextIdx > prog.size()) {
				// Encountered a fault in program execution
				progExecState.setState(ExecState.EXEC_FAULT);
				
			} else {
				progExecState.setCurIdx(nextIdx);
				
				if (nextIdx == prog.size()) {
					
					if (!progCallStack.isEmpty()) {
						// Return to the program state on the top of the call stack
						ProgExecution prevExec = progCallStack.pop();
						RoboticArm r = getRobot(prevExec.getRID());
						
						if (r != null) {
							progExecState = prevExec;
							
							if (r.RID != activeRobot.RID) {
								// Update the active robot
								activeRobot = ROBOTS.get(progExecState.getRID());
								curScreen.getContents().setSelectedColumnIdx(0);
							}
							
							progExecState.setCurIdx( progExecState.getNextIdx() );
						}
						
					} else {
						// Reached the end of execution
						progExecState.setState(ExecState.EXEC_DONE);
					}
					
				} else if (progExecState.isSingleExec()) {
					// Reached the end of execution
					progExecState.setState(ExecState.EXEC_DONE);
					
				} else {
					// Excute the next instruction
					progExecState.setState(ExecState.EXEC_INST);
				}
			}
		}
		
		// Update the display
		curScreen.getContents().setSelectedLineIdx(getInstrLine(getActiveInstIdx()) );
		updatePendantScreen();
	}

	/**
	 * Updates program instruction list display based on the active robot's
	 * current position and orientation. If the position defined by a motion
	 * instruction in the active program display is equal to the robot's
	 * position and orientation, then an '@' is displayed next to the motion
	 * instruction.
	 */
	private void updateInstList() {
		if (curScreen instanceof ScreenNavProgInstructions) {
			Program prog = getActiveProg();
			Point robotPos = activeRobot.getToolTipNative();
			boolean updatedLines = false;
			
			// Check each instruction in the active program
			for (int idx = 0; idx < prog.getNumOfInst(); ++idx) {
				Instruction inst = prog.getInstAt(idx);
				Integer keyInt = new Integer(idx);
				
				if (inst instanceof PosMotionInst) { 
					PosMotionInst mInst = (PosMotionInst)prog.getInstAt(idx);
					Point instPt = activeRobot.getVector(mInst, prog, false);
					
					if (instPt != null) {
						boolean closeEnough = (mInst.getMotionType() == Fields.MTYPE_JOINT
								&& instPt.compareJoint(robotPos) ||
								mInst.getMotionType() != Fields.MTYPE_JOINT
								&& instPt.compareCartesian(robotPos));
						
						Boolean prevState = mInstRobotAt.put(keyInt, closeEnough);
						
						if (prevState == null || prevState.booleanValue() != closeEnough) {
							updatedLines = true;
						}
					}
						
				} else if (mInstRobotAt.get(keyInt) != null) {
					// Remove previous motion instructions
					mInstRobotAt.remove(keyInt);
				}
			}
			
			if (updatedLines) {
				updatePendantScreen();
			}
		}
		
	}

	private int updateProgExec() {
		Program prog = getActiveProg();
		Instruction activeInstr = prog.getInstAt(progExecState.getCurIdx());
		int nextIdx;
		
		if (progExecState.getType() == ExecType.EXEC_BWD) {
			// Backward program execution only works for motion instructions
			nextIdx = progExecState.getCurIdx() - 1;
			
			if (!(prog.getInstAt(nextIdx) instanceof MotionInstruction)) {
				nextIdx = progExecState.getCurIdx();
			}
			
		} else {
			nextIdx = progExecState.getCurIdx() + 1;
		}
		
		if (progExecState.getState() == ExecState.EXEC_INST ||
				progExecState.getState() == ExecState.EXEC_START) {
			
			if (activeInstr != null && !activeInstr.isCommented()) {
				if (activeInstr instanceof MotionInstruction) {
					MotionInstruction motInstr = (MotionInstruction) activeInstr;
					int ret = activeRobot.setupMInstMotion(prog, motInstr, nextIdx,
							progExecState.isSingleExec());
					
					if (ret != 0) {
						// Issue occurred with setting up the motion instruction
						nextIdx = -1;
					}
					
				} else if (activeInstr instanceof FrameInstruction) {
					FrameInstruction fInst = (FrameInstruction)activeInstr;
					
					if (fInst.getFrameType() == Fields.FTYPE_TOOL) {
						activeRobot.setActiveToolFrame(fInst.getFrameIdx());
						
					} else if (fInst.getFrameType() == Fields.FTYPE_USER) {
						activeRobot.setActiveUserFrame(fInst.getFrameIdx());
					}
					
				} else if (activeInstr instanceof IOInstruction) {
					IOInstruction ioInst = (IOInstruction)activeInstr;
					updateRobotEEState(ioInst.getReg(), ioInst.getState());
					
				} else if (activeInstr instanceof JumpInstruction) {
					JumpInstruction jInst = (JumpInstruction)activeInstr;
					nextIdx = prog.findLabelIdx(jInst.getTgtLblNum());
	
				} else if (activeInstr instanceof CallInstruction) {
					CallInstruction cInst = (CallInstruction)activeInstr;
					
					if (cInst.getTgtDevice() != activeRobot &&
							!isSecondRobotUsed()) {
						// Cannot use robot call, when second robot is not active
						nextIdx = -1;
						
					} else {
						progExecState.setNextIdx(nextIdx);
						pushActiveProg();
						
						if (cInst.getTgtDevice() == activeRobot) {
							// Normal call instruction
							int progIdx = activeRobot.getProgIdx(cInst.getProg().getName());
							progExecState.setExec(activeRobot.RID, progExecState.getType(), progIdx, 0);
														
						} else {
							// Robot call instruction
							RoboticArm r = getInactiveRobot();
							activeRobot = r;
							int progIdx = r.getProgIdx(cInst.getProg().getName());
							progExecState.setExec(r.RID, progExecState.getType(), progIdx, 0);
						}
						
						nextIdx = 0;
					}
	
				} else if (activeInstr instanceof IfStatement) {
					IfStatement ifStmt = (IfStatement)activeInstr;
					
					int ret = ifStmt.evalExpression();
					
					if (ret == 0) {
						// Execute sub instruction
						Instruction subInst = ifStmt.getInstr();
						
						if (subInst instanceof JumpInstruction) {
							int lblId = ((JumpInstruction) subInst).getTgtLblNum();
							nextIdx = prog.findLabelIdx(lblId);
							
						} else if (subInst instanceof CallInstruction) {
							CallInstruction cInst = (CallInstruction)subInst;
							
							if (cInst.getTgtDevice() != activeRobot &&
									!isSecondRobotUsed()) {
								// Cannot use robot call, when second robot is not active
								nextIdx = -1;
								
							} else {
								progExecState.setNextIdx(nextIdx);
								pushActiveProg();
								
								if (cInst.getTgtDevice() == activeRobot) {
									// Normal call instruction
									int progIdx = activeRobot.getProgIdx(cInst.getProg().getName());
									progExecState.setExec(activeRobot.RID, progExecState.getType(), progIdx, 0);
																
								} else {
									// Robot call instruction
									RoboticArm r = getInactiveRobot();
									activeRobot = r;
									int progIdx = r.getProgIdx(cInst.getProg().getName());
									progExecState.setExec(r.RID, progExecState.getType(), progIdx, 0);
								}
								
								nextIdx = 0;
							}
							
						} else {
							nextIdx = -1;
						}
						
					} else if (ret == 2) {
						// Evaluation failed
						nextIdx = -1;
					}
	
				} else if (activeInstr instanceof SelectStatement) {
					SelectStatement selStmt = (SelectStatement)activeInstr;
					int caseIdx = selStmt.evalCases();
					Fields.debug("selStmt: %d\n", caseIdx);
					if (caseIdx == -2) {
						nextIdx = -1;
						
					} else if (caseIdx >= 0) {
						// Execute sub instruction
						Instruction subInst = selStmt.getCaseInst(caseIdx);
						
						if (subInst instanceof JumpInstruction) {
							int lblId = ((JumpInstruction) subInst).getTgtLblNum();
							nextIdx = prog.findLabelIdx(lblId);
							
						} else if (subInst instanceof CallInstruction) {
							CallInstruction cInst = (CallInstruction)subInst;
							
							if (cInst.getTgtDevice() != activeRobot &&
									!isSecondRobotUsed()) {
								// Cannot use robot call, when second robot is not active
								nextIdx = -1;
								
							} else {
								progExecState.setNextIdx(nextIdx);
								pushActiveProg();
								
								if (cInst.getTgtDevice() == activeRobot) {
									// Normal call instruction
									int progIdx = activeRobot.getProgIdx(cInst.getProg().getName());
									progExecState.setExec(activeRobot.RID, progExecState.getType(), progIdx, 0);
																
								} else {
									// Robot call instruction
									RoboticArm r = getInactiveRobot();
									activeRobot = r;
									int progIdx = r.getProgIdx(cInst.getProg().getName());
									progExecState.setExec(r.RID, progExecState.getType(), progIdx, 0);
								}
								
								nextIdx = 0;
							}
							
						} else {
							nextIdx = -1;
						}
					}
					
				} else if (activeInstr instanceof RegisterStatement) {
					RegisterStatement regStmt = (RegisterStatement)activeInstr;
					
					int ret = regStmt.evalExpression();
					
					if (ret != 0) {
						// Register expression evaluation failed
						nextIdx = -1;
					}
				}
			}
		}
		
		if (activeInstr instanceof MotionInstruction) {
			progExecState.setState(ExecState.EXEC_MINST);
			
		} else {
			progExecState.setState(ExecState.EXEC_NEXT);
		}
		
		progExecState.setNextIdx(nextIdx);
		
		return 0;
	}
}

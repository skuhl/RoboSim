package core;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import camera.RegisteredModels;
import camera.RobotCamera;
import enums.AxesDisplay;
import enums.CoordFrame;
import enums.ExecState;
import enums.ExecType;
import expression.Operand;
import expression.OperandCamObj;
import expression.Operator;
import frame.UserFrame;
import geom.BoundingBox;
import geom.ComplexShape;
import geom.CoordinateSystem;
import geom.Fixture;
import geom.Part;
import geom.Point;
import geom.RMatrix;
import geom.RRay;
import geom.RShape;
import geom.Scenario;
import geom.WorldObject;
import global.DataManagement;
import global.Fields;
import global.RMath;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.event.MouseEvent;
import programming.CallInstruction;
import programming.FrameInstruction;
import programming.IOInstruction;
import programming.IfStatement;
import programming.InstElement;
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
import regs.PositionRegister;
import regs.Register;
import robot.RTrace;
import robot.RoboticArm;
import screen.Screen;
import screen.ScreenManager;
import screen.ScreenMode;
import screen.content_disp.ScreenNavProgInstructions;
import screen.content_disp.ScreenNavPrograms;
import screen.edit_point.ST_ScreenPointEntry;
import screen.num_entry.ST_ScreenNumEntry;
import screen.teach_frame.ST_ScreenTeachPoints;
import screen.text_entry.ST_ScreenTextEntry;
import ui.Camera;
import ui.DisplayLine;
import ui.KeyCodeMap;
import ui.MenuScroll;
import ui.RecordScreen;
import undo.WOUndoCurrent;
import undo.WOUndoDelete;
import undo.WOUndoState;
import window.WGUI;
import window.WGUI_Buttons;

/**
 * The main class of the RoboRun application. This class handles the button
 * events as well as the main render loop of the application amongst other
 * various nuances in the program.
 * 
 * @author Vincent Druckte, Joshua Hooker, and James Walker
 */
public class RobotRun extends PApplet {
	private static RobotRun instance;
	
	public static RobotRun getInstance() {
		return instance;
	}
	
	public static RoboticArm getInstanceRobot() {
		return instance.getActiveRobot();
	}
	
	public static Scenario getInstanceScenario() {
		return instance.getActiveScenario();
	}

	public static void main(String[] args) {
		String[] appletArgs = new String[] { "core.RobotRun" };

		if (args != null) {
			PApplet.main(concat(appletArgs, args));
		} else {
			PApplet.main(appletArgs);
		}
	}

	// container for instructions being copied/ cut and pasted
	public ArrayList<Instruction> clipBoard = new ArrayList<>();
	
	public int editIdx = -1;

	public Operand<?> opEdit = null;
	
	private Pointer<RoboticArm> activeRobot;
	private Pointer<Scenario> activeScenario;
	
	private Camera camera;
	private KeyCodeMap keyCodeMap;
	
	/**
	 * A list of instruction indexes for the active program, which point to
	 * motion instructions, whose position and orientation (or joint angles)
	 * are close enough to the robot's current position.
	 */
	private HashMap<Integer, Boolean> mInstRobotAt;
	
	/**
	 * Keeps track of when the mouse is being dragged to update the position or
	 * orientation of a world object.
	 */
	private boolean mouseDragWO;
	
	/**
	 * Keeps track of the world object that the mouse was over, when the mouse
	 * was first pressed down.
	 */
	private WorldObject mouseOverWO;
	
	private Stack<ProgExecution> progCallStack;
	private ProgExecution progExecState;
	private boolean rCamEnable = false;
	private RobotCamera rCamera;

	private final HashMap<Integer, RoboticArm> ROBOTS = new HashMap<>();
	private RTrace robotTrace;
	
	private final Stack<WOUndoState> SCENARIO_UNDO = new Stack<>();
	private final ArrayList<Scenario> SCENARIOS = new ArrayList<>();
	
	private ScreenManager screens;
	
	private boolean shift = false; // Is shift button pressed or not?
	private boolean step = false; // Is step button pressed or not?

	private WGUI UI;
	
	private RecordScreen record;
	
	/**
	 * Keeps track of the last mouse click, so that it can be rendered on the
	 * screen as a ray, in the world frame.
	 */
	private RRay mouseRay;
	
	/**
	 * Keeps track of a point, so that is can be display in the world frame.
	 */
	private Point position;
	
	/**
	 * Applies the active camera to the matrix stack.
	 * 
	 * @param c	The camera to apply
	 */
	public void applyCamera(Camera c) {
		PVector cPos = c.getBasePosition();
		PVector cOrien = c.getOrientation();
		float horizontalMargin = c.getScale() * width / 2f,
				verticalMargin = c.getScale() * height / 2f,
				near = 1f,
				far = 1.5f * camera.getMaxZOffset();

		translate(width / 2f, height / 2f, -camera.getZOffset());
		
		rotateZ(-cOrien.z);
		rotateY(-cOrien.y);
		rotateX(-cOrien.x);
		
		translate(-cPos.x, -cPos.y, -cPos.z);
		
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
	 * @param mat	A 4x4 row major transformation matrix
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
		try {
			screens.getActiveScreen().actionDn();
			updatePendantScreen();
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Pendant LEFT button
	 * 
	 * Moves one column to the left on a list element display on the pendant's
	 * screen. Depending on what menu is active, this may move the pointer in
	 * either the content or options menu.
	 */
	public void button_arrowLt() {
		try {
			screens.getActiveScreen().actionLt();
			updatePendantScreen();
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
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
		try {
			screens.getActiveScreen().actionRt();
			updatePendantScreen();
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Pendant UP button
	 * 
	 * Moves up one element in a list displayed on the pendant screen.
	 * Depending on what menu is active this may move the list pointer
	 * in either the content or options menu.
	 */
	public void button_arrowUp() {
		try {
			screens.getActiveScreen().actionUp();
			updatePendantScreen();
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}

	/**
	 * Pendant BKSPC button
	 * 
	 * Functions as a backspace key for number, text, and point input menus.
	 */
	public void button_bkspc() {
		try {
			screens.getActiveScreen().actionBkspc();
			updatePendantScreen();
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Pendant BWD button
	 * 
	 * Executes a motion instruction two instructions prior to the active
	 * instruction (if one exists).
	 */
	public void button_bwd() {
		try {
			// Backwards is only functional when executing a program one instruction
			// at a time
			if (screens.getActiveScreen() instanceof ScreenNavProgInstructions
					&& isShift() && isStep()) {
				
				// Safeguard against editing a program while it is running
				screens.getActiveScreen().getContents().setColumnIdx(0);
				Fields.resetMessage();
				progExecBwd();
			}
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}

	public void button_camDeleteObj() {
		WorldObject o = UI.getSelectedCamObj();
		rCamera.getTaughtObjects().remove(o);
		UI.updateCameraListContents();
		UI.updateUIContentPositions();
	}
	
	public void button_camTeachObj() {
		try {
			if(getActiveScenario() != null) {
				rCamera.teachObjectToCamera(getActiveScenario());
			}
			
			UI.updateCameraListContents();
			UI.updateUIContentPositions();
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	public void button_camToggleActive() {
		try {
			rCamEnable = UI.toggleCamera();
					
			UI.updateUIContentPositions();
			updatePendantScreen();
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	public void button_camUpdate() {
		try {
			if (rCamera != null) {
				UI.updateCameraCurrent();
				Fields.resetMessage();
			}
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}

	/**
	 * Camera Bk button
	 * 
	 * Sets the camera to the default back view, which looks down the positive
	 * x-axis of the world coordinate system.
	 */
	public void button_camViewBack() {
		try {
			// Back view
			camera.setRotation(0f, PI, 0f);
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Camera Bt button
	 * 
	 * Sets the camera to point down the positive z-axis of the world coordinate
	 * frame, so as to view the bottom of the robot.
	 */
	public void button_camViewBottom() {
		try {
			// Bottom view
			camera.setRotation(HALF_PI, 0f, 0f);
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Camera D button
	 * 
	 * Resets the position of the camera to (0, 0, 0).
	 */
	public void button_camViewDefault() {
		try {
			// Bottom view
			camera.setPosition(0f, 0f, 0f);
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Camera F button
	 * 
	 * Sets the camera to the default position, facing down the negative y-axis
	 * of the world coordinate system.
	 */
	public void button_camViewFront() {
		try {
			// Default view
			camera.setRotation(0f, 0f, 0f);
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Camera L button
	 * 
	 * Sets the camera facing down the negative x-axis of the world coordinate
	 * frame.
	 */
	public void button_camViewLeft() {
		try {
			// Left view
			camera.setRotation(0f, HALF_PI, 0f);
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Camera R button
	 * 
	 * Sets the camera to point down the positive x-axis of the world
	 * coordinate frame.
	 */
	public void button_camViewRight() {
		try {
			// Right view
			camera.setRotation(0, 3f * HALF_PI, 0f);
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Camera T button
	 * 
	 * Sets the camera to point down the negative z-axis of the world
	 * coordinate frame.
	 */
	public void button_camViewTop() {
		try {
			// Top view
			camera.setRotation(3f * HALF_PI, 0f, 0f);
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Pendant COORD button
	 * 
	 * If shift is off, then this button will change the coordinate frame of
	 * the active robot. If shift is on, then this button will change to the
	 * active frames menu on the pendant.
	 */
	public void button_coord() {
		try {
			if (isShift()) {
				nextScreen(ScreenMode.ACTIVE_FRAMES);
			} else {
				// Update the coordinate frame
				coordFrameTransition();
				updatePendantScreen();
			}
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}

	/**
	 * Pendant - button
	 * 
	 * Appends the '-' character to input for number, text, and point entry
	 * menus.
	 */
	public void button_dash() {
		try {
			screens.getActiveScreen().actionKeyPress('-');
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Pendant DATA button
	 * 
	 * Displays a list of the register navigation menus (i.e. data or position
	 * registers).
	 */
	public void button_data() {
		try {
			nextScreen(ScreenMode.NAV_DATA);
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}

	/**
	 * Pendant EDIT button
	 * 
	 * Links to the active program's instruction navigation menu, except in the
	 * select screen, where it will open the instruction navigation menu for
	 * the selected program
	 */
	public void button_edit() {
		try {
			if (screens.getActiveScreen() instanceof ScreenNavPrograms) {
				// Load the selected program
				setActiveProgIdx(screens.getActiveScreen().getContentIdx());
				setActiveInstIdx(0);
				nextScreen(ScreenMode.NAV_PROG_INSTR);
				
			} else if (getActiveProg() != null) {
				// Load the current active program
				nextScreen(ScreenMode.NAV_PROG_INSTR);
			} else {
				// Load the program navigation menu
				screens.resetStack();
				nextScreen(ScreenMode.NAV_PROGRAMS);
			}
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}

	/**
	 * Pendant ENTER button
	 * 
	 * Functions as a confirmation button for almost all menus.
	 */
	public void button_enter() {
		try {
			Fields.resetMessage();
			screens.getActiveScreen().actionEntr();
			updatePendantScreen();
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Pendant F1 button
	 * 
	 * Function varies amongst menus. A hint label will appear in the pendant
	 * screen above a function button, which has an action in the current
	 * menu.
	 */
	public void button_F1() {
		try {
			screens.getActiveScreen().actionF1();
			updatePendantScreen();
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Pendant F2 button
	 * 
	 * Function varies amongst menus. A hint label will appear in the pendant
	 * screen above a function button, which has an action in the current
	 * menu.
	 */
	public void button_F2() {
		try {
			screens.getActiveScreen().actionF2();
			updatePendantScreen();
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Pendant F3 button
	 * 
	 * Function varies amongst menus. A hint label will appear in the pendant
	 * screen above a function button, which has an action in the current
	 * menu.
	 */
	public void button_F3() {
		try {
			screens.getActiveScreen().actionF3();
			updatePendantScreen();
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}

	/**
	 * Pendant F4 button
	 * 
	 * Function varies amongst menus. A hint label will appear in the pendant
	 * screen above a function button, which has an action in the current
	 * menu.
	 */
	public void button_F4() {
		try {
			screens.getActiveScreen().actionF4();
			updatePendantScreen();
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}

	/**
	 * Pendant F5 button
	 * 
	 * Function varies amongst menus. A hint label will appear in the pendant
	 * screen above a function button, which has an action in the current
	 * menu.
	 */
	public void button_F5() {
		try {
			screens.getActiveScreen().actionF5();
			updatePendantScreen();
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}

	/**
	 * Pendant FWD button
	 * 
	 * Executes instructions in the instruction navigation of a program. If
	 * step is active, then only one instruction is executed at a time,
	 * otherwise the entire program is executed.
	 */
	public void button_fwd() {
		try {
			if (screens.getActiveScreen() instanceof ScreenNavProgInstructions
					&& !isProgExec() && isShift()) {
				
				// Stop any prior Robot movement
				button_hold();
				// Safeguard against editing a program while it is running
				screens.getActiveScreen().getContents().setColumnIdx(0);
				progExec(isStep());
			}
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
  
	/**
	 * Pendant HOLD button
	 * 
	 * Stops all robot motion and program execution.
	 */
	public void button_hold() {
		try {
			boolean robotInMotion = getActiveRobot().inMotion();
			// Stop all robot motion and program execution
			getActiveRobot().halt();
			progExecState.halt();
			
			if (robotInMotion && !getActiveRobot().inMotion()) {
				// Robot has stopped moving
				updateInstList();
			}
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
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
		try {
			if (isShift()) {
				if (getActiveRobot().getMacroKeyBinds()[6] != null) {
					execute(getActiveRobot().getMacroKeyBinds()[6]);
				}
	
			} else {
				if (!isProgExec()) {
					// Map I/O to the robot's end effector state, if shift is off
					toggleEEState(getActiveRobot());
				}
			}
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Pendant ITEM button
	 * 
	 * Not sure what this does ...
	 */
	public void button_item() {
		try {
			if (screens.getActiveScreen() instanceof ScreenNavProgInstructions) {
				nextScreen(ScreenMode.JUMP_TO_LINE);
			}
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
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
		try {
			updateRobotJogMotion(0, -1);
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Pendant -Y/(J2) button
	 * 
	 * This button jogs the active robot with reference to its active coordinate
	 * frame. In addition, this button is paired with +Y/(J2) in such a way that
	 * at most one of the two can be active at one time.
	 */
	public void button_jointNeg2() {
		try {
			updateRobotJogMotion(1, -1);
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Pendant -Z/(J3) button
	 * 
	 * This button jogs the active robot with reference to its active coordinate
	 * frame. In addition, this button is paired with +Z/(J3) in such a way that
	 * at most one of the two can be active at one time.
	 */
	public void button_jointNeg3() {
		try {
			updateRobotJogMotion(2, -1);
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Pendant -XR/(J4) button
	 * 
	 * This button jogs the active robot with reference to its active coordinate
	 * frame. In addition, this button is paired with +XR/(J4) in such a way that
	 * at most one of the two can be active at one time.
	 */
	public void button_jointNeg4() {
		try {
			updateRobotJogMotion(3, -1);
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Pendant -YR/(J5) button
	 * 
	 * This button jogs the active robot with reference to its active coordinate
	 * frame. In addition, this button is paired with +YR/(J5) in such a way that
	 * at most one of the two can be active at one time.
	 */
	public void button_jointNeg5() {
		try {
			updateRobotJogMotion(4, -1);
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Pendant -ZR/(J6) button
	 * 
	 * This button jogs the active robot with reference to its active coordinate
	 * frame. In addition, this button is paired with +ZR/(J6) in such a way that
	 * at most one of the two can be active at one time.
	 */
	public void button_jointNeg6() {
		try {
			updateRobotJogMotion(5, -1);
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Pendant +X/(J1) button
	 * 
	 * This button jogs the active robot with reference to its active coordinate
	 * frame. In addition, this button is paired with -X/(J1) in such a way that
	 * at most one of the two can be active at one time.
	 */
	public void button_jointPos1() {
		try {
			updateRobotJogMotion(0, 1);
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Pendant +Y/(J2) button
	 * 
	 * This button jogs the active robot with reference to its active coordinate
	 * frame. In addition, this button is paired with -Y/(J2) in such a way that
	 * at most one of the two can be active at one time.
	 */
	public void button_jointPos2() {
		try {
			updateRobotJogMotion(1, 1);
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Pendant +Z/(J3) button
	 * 
	 * This button jogs the active robot with reference to its active coordinate
	 * frame. In addition, this button is paired with -Z/(J3) in such a way that
	 * at most one of the two can be active at one time.
	 */
	public void button_jointPos3() {
		try {
			updateRobotJogMotion(2, 1);
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Pendant +XR/(J4) button
	 * 
	 * This button jogs the active robot with reference to its active coordinate
	 * frame. In addition, this button is paired with -XR/(J4) in such a way that
	 * at most one of the two can be active at one time.
	 */
	public void button_jointPos4() {
		try {
			updateRobotJogMotion(3, 1);
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Pendant +YR/(J5) button
	 * 
	 * This button jogs the active robot with reference to its active coordinate
	 * frame. In addition, this button is paired with -YR/(J5) in such a way that
	 * at most one of the two can be active at one time.
	 */
	public void button_jointPos5() {
		try {
			updateRobotJogMotion(4, 1);
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}

	/**
	 * Pendant +YR/(J5) button
	 * 
	 * This button jogs the active robot with reference to its active coordinate
	 * frame. In addition, this button is paired with -YR/(J5) in such a way that
	 * at most one of the two can be active at one time.
	 */
	public void button_jointPos6() {
		try {
			updateRobotJogMotion(5, 1);
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}

	/**
	 * Pendant MENU button
	 * 
	 * A list of miscellaneous sub menus (frames, marcos, I/O registers).
	 */
	public void button_menu() {
		try {
			nextScreen(ScreenMode.NAV_MAIN_MENU);
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}

	/**
	 * Pendant MVMU button
	 * 
	 * A button used for macro binding
	 */
	public void button_mvmu() {
		try {
			if (getActiveRobot().getMacroKeyBinds()[2] != null && isShift()) {
				execute(getActiveRobot().getMacroKeyBinds()[2]);
			}
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}

	/**
	 * Pendant '0' button
	 * 
	 * Appends a '0' character to input for the text, number, and point entry
	 * menus.
	 */
	public void button_num0() {
		try {
			screens.getActiveScreen().actionKeyPress('0');
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Pendant '1' button
	 * 
	 * Appends a '1' character to input for the text, number, and point entry
	 * menus.
	 */
	public void button_num1() {
		try {
			screens.getActiveScreen().actionKeyPress('1');
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Pendant '2' button
	 * 
	 * Appends a '2' character to input for the text, number, and point entry
	 * menus.
	 */
	public void button_num2() {
		try {
			screens.getActiveScreen().actionKeyPress('2');
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}

	/**
	 * Pendant '3' button
	 * 
	 * Appends a '3' character to input for the text, number, and point entry
	 * menus.
	 */
	public void button_num3() {
		try {
			screens.getActiveScreen().actionKeyPress('3');
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}

	/**
	 * Pendant '4' button
	 * 
	 * Appends a '4' character to input for the text, number, and point entry
	 * menus.
	 */
	public void button_num4() {
		try {
			screens.getActiveScreen().actionKeyPress('4');
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}

	/**
	 * Pendant '5' button
	 * 
	 * Appends a '5' character to input for the text, number, and point entry
	 * menus.
	 */
	public void button_num5() {
		try {
			screens.getActiveScreen().actionKeyPress('5');
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}

	/**
	 * Pendant '6' button
	 * 
	 * Appends a '6' character to input for the text, number, and point entry
	 * menus.
	 */
	public void button_num6() {
		try {
			screens.getActiveScreen().actionKeyPress('6');
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Pendant '7' button
	 * 
	 * Appends a '7' character to input for the text, number, and point entry
	 * menus.
	 */
	public void button_num7() {
		try {
			screens.getActiveScreen().actionKeyPress('7');
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Pendant '8' button
	 * 
	 * Appends a '8' character to input for the text, number, and point entry
	 * menus.
	 */
	public void button_num8() {
		try {
			screens.getActiveScreen().actionKeyPress('8');
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Pendant '9' button
	 * 
	 * Appends a '9' character to input for the text, number, and point entry
	 * menus.
	 */
	public void button_num9() {
		try {
			screens.getActiveScreen().actionKeyPress('9');
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Clear button shared between the Create and Edit windows
	 * 
	 * Clears all input fields (textfields, dropdownlist, etc.) in the world
	 * object creation and edit windows.
	 */
	public void button_objClearFields() {
		try {
			Fields.resetMessage();
			UI.clearAllInputFields();
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}

	/**
	 * Confirm button in the Edit window
	 * 
	 * Updates the dimensions of the selected world object based off the values
	 * of the dimension input fields.
	 */
	public void button_objConfirmDims() {
		try {
			if (getActiveScenario() != null) {
				Fields.resetMessage();
				WorldObject selectedWO = UI.getSelectedWO();
				
				if (selectedWO != null) {
					// Update the dimensions of the world object
					WOUndoState undoState = UI.updateWODims(selectedWO);
					
					if (undoState != null) {
						// Save original world object onto the undo stack
						updateScenarioUndo(undoState);
					}
				}
				
			} else {
				Fields.setMessage("No active scenario!");
			}
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
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
		try {
			if (getActiveScenario() != null) {
				Fields.resetMessage();
				WorldObject newObject = UI.createWorldObject();
	
				if (newObject != null) {
					newObject.setLocalCenter(new PVector(-500f, 0f, 0f));
					getActiveScenario().addWorldObject(newObject);
					DataManagement.saveScenarios(this);
				}
			}
			else {
				Fields.setMessage("No active scenario!");
			}
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}

	/**
	 * Delete button in the edit window
	 * 
	 * Removes the selected world object from the active scenario.
	 */
	public void button_objDelete() {
		try {
			// Delete focused world object and add to the scenario undo stack
			WorldObject selected = UI.getSelectedWO();
			
			if (selected != null) {
				updateScenarioUndo(new WOUndoDelete(selected, getActiveScenario()));
				int ret = getActiveScenario().removeWorldObject( selected );
				
				if (ret == 0) {
					UI.setSelectedWO(null);
				}
				
				Fields.debug("World Object removed: %d\n", ret);
				
				DataManagement.saveScenarios(this);
			}
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Move to Current button in the edit window
	 * 
	 * Updates the current position and orientation of a selected object to the
	 * inputed values in the edit window.
	 */
	public void button_objMoveToCur() {
		try {
			// Only allow world object editing when no program is executing
			if (!isProgExec()) {
				RoboticArm r = getActiveRobot();
				Fields.resetMessage();
				WorldObject selectedWO = UI.getSelectedWO();
				
				if (selectedWO instanceof Fixture || (selectedWO instanceof Part &&
						(r == null || !r.isHeld((Part)selectedWO)))) {
					
					WOUndoState undoState = UI.updateWOCurrent(selectedWO);
					
					if (undoState != null) {
						/*
						 * If the object was modified, then save the previous state
						 * of the object
						 */
						updateScenarioUndo(undoState);
					}
					
					DataManagement.saveScenarios(this);
				}
			}
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}

	/**
	 * Move to Default button in the edit window
	 * 
	 * Updates the current position and orientation of a selected world object
	 * to that of its default fields.
	 */
	public void button_objMoveToDefault() {
		try {
			// Only allow world object editing when no program is executing
			if (!isProgExec()) {
				RoboticArm r = getActiveRobot();
				Fields.resetMessage();
				WorldObject selectedWO = UI.getSelectedWO();
				
				if (selectedWO instanceof Part && (r == null || !r.isHeld((Part)selectedWO))) {
					WOUndoState undoState = UI.updateWOCurrent(selectedWO);
					UI.fillCurWithDef( (Part)selectedWO );
	
					if (undoState != null) {
						// If the part was modified, then save its previous state
						updateScenarioUndo(undoState);
					}
					
					DataManagement.saveScenarios(this);
				}
			}
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}

	/**
	 * Restores all parts in the current scenario to their default position and
	 * orientation.
	 */
	public void button_objResetDefault() {
		try {
			Fields.resetMessage();
			
			for (WorldObject wo : getActiveScenario()) {
				// Only applies to parts
				if (wo instanceof Part) {
					updateScenarioUndo(new WOUndoCurrent(wo));
					
					Part p = (Part) wo;
					p.setLocalCenter(p.getDefaultCenter());
					p.setLocalOrientation(p.getDefaultOrientation());
				}
			}
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}

	/**
	 * HIDE/SHOW OBBBS button in the miscellaneous window
	 * 
	 * Toggles bounding box display on or off.
	 */
	public void button_objToggleBounds() {
		try {
			System.out.println("Hello world!");
			UI.updateUIContentPositions();
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}

	/**
	 * Update Default button in the edit window
	 * 
	 * Updates the default position and orientation of a world object based on
	 * the input fields in the edit window.
	 */
	public void button_objUpdateDefault() {
		try {
			Fields.resetMessage();
			WorldObject selectedWO = UI.getSelectedWO();
			// Only parts have a default position and orientation
			if (selectedWO instanceof Part) {
				WOUndoState undoState = UI.updateWODefault( (Part)selectedWO );
				
				if (undoState != null) {
					// If the part was modified, then save its previous state
					updateScenarioUndo(undoState);
				}
			}
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Pendant '.' buttom
	 * 
	 * Appends a '.' character to input in the text, number, and point entry
	 * menus.
	 */
	public void button_period() {
		try {
			screens.getActiveScreen().actionKeyPress('.');
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}

	/**
	 * Pendant POSN button
	 * 
	 * A button used for marcos binding.
	 */
	public void button_posn() {
		try {
			if (getActiveRobot().getMacroKeyBinds()[5] != null && isShift()) {
				execute(getActiveRobot().getMacroKeyBinds()[5]);
			}
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Pendant PREV button
	 * 
	 * Transitions to the previous menu screen, if one exists.
	 */
	public void button_prev() {
		try {
			lastScreen();
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}

	/**
	 * Pendant RESET button
	 * 
	 * Resets the motion fault flag for the active robot, when the motion fault
	 * flag is set on.
	 */
	public void button_reset() {
		try {
			if (isShift()) {
				button_hold();
				// Reset motion fault for the active robot
				getActiveRobot().setMotionFault(false);
			}
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}

	public void button_robotClearTrace() {
		try {
			robotTrace.clear();
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}

	/**
	 * ADD/REMOVE ROBOT button in the miscellaneous window
	 * 
	 * Toggles the second Robot on or off.
	 */
	public void button_robotToggleActive() {
		try {
			UI.toggleSecondRobot();
			/* Reset the active robot to the first if the second robot is
			 * removed */
			if (getActiveRobot() != ROBOTS.get(0)) {
				activeRobot.set(ROBOTS.get(0));
			}
	
			UI.updateUIContentPositions();
			updatePendantScreen();
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}

	/**
	 * ENABLE/DISABLE TRACE button in miscellaneous window
	 * 
	 * Toggles the robot tool tip trace function on or off.
	 */
	public void button_robotToggleTrace() {
		try {
			UI.updateUIContentPositions();
			
			if (!traceEnabled()) {
				// Empty trace when it is disabled
				robotTrace.addPt(null);
			}
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
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
		try {
			Fields.resetMessage();
			int ret = UI.updateScenarios(SCENARIOS);
	
			if (ret > 0) {
				activeScenario.set( UI.getSelectedScenario() );
				DataManagement.saveScenarios(this);
	
			} else if (ret == 0) {
				DataManagement.saveScenarios(this);
			}
			
			Fields.debug(String.format("SConfirm: %d\n", ret));
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Pendant SELECT button
	 * 
	 * Transitions to the program navigation menu, where the user can manage
	 * their programs.
	 */
	public void button_select() {
		try {
			nextScreen(ScreenMode.NAV_PROGRAMS);
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}

	/**
	 * Pendant SETUP button
	 * 
	 * A button used for binding macros.
	 */
	public void button_setup() {
		try {
			if (getActiveRobot().getMacroKeyBinds()[3] != null && isShift()) {
				execute(getActiveRobot().getMacroKeyBinds()[3]);
			}
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}

	/**
	 * Pendant SHIFT button
	 * 
	 * Toggles the shift state on or off. Shift is required to be on for
	 * anything involving robot motion or point recording.
	 */
	public void button_shift() {
		try {
			setShift(!shift);
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Pendant +% button
	 * 
	 * Increases the robot's jog speed.
	 */
	public void button_speedDn() {
		try {
			int curSpeed = getActiveRobot().getLiveSpeed();
			// Reduce the speed at which the Robot jogs
			if (isShift()) {
				if (curSpeed > 50) {
					getActiveRobot().setLiveSpeed(50);
				} else if (curSpeed > 5) {
					getActiveRobot().setLiveSpeed(5);
				} else {
					getActiveRobot().setLiveSpeed(1);
				}
			} else if (curSpeed > 1) {
				if (curSpeed > 50) {
					getActiveRobot().setLiveSpeed(curSpeed - 10);
				} else if (curSpeed > 5) {
					getActiveRobot().setLiveSpeed(curSpeed - 5);
				} else {
					getActiveRobot().setLiveSpeed(curSpeed - 1);
				}
			}
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}

	/**
	 * Pendant -% button
	 * 
	 * Decreases the robot's jog speed.
	 */
	public void button_speedUp() {
		try {
			int curSpeed = getActiveRobot().getLiveSpeed();
			// Increase the speed at which the Robot jogs
			if (isShift()) {
				if (curSpeed < 5) {
					getActiveRobot().setLiveSpeed(5);
				} else if (curSpeed < 50) {
					getActiveRobot().setLiveSpeed(50);
				} else {
					getActiveRobot().setLiveSpeed(100);
				}
			} else if (curSpeed < 100) {
				if (curSpeed < 5) {
					getActiveRobot().setLiveSpeed(curSpeed + 1);
				} else if (curSpeed < 50) {
					getActiveRobot().setLiveSpeed(curSpeed + 5);
				} else {
					getActiveRobot().setLiveSpeed(curSpeed + 10);
				}
			}
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Pendant STATUS button
	 * 
	 * A button used for macros.
	 */
	public void button_status() {
		try {
			if (getActiveRobot().getMacroKeyBinds()[4] != null && isShift()) {
				execute(getActiveRobot().getMacroKeyBinds()[4]);
			}
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Pendant STEP button
	 * 
	 * Toggles the step state on or off. When step is on, then instructions
	 * will be executed one at a time as opposed to all at once.
	 */
	public void button_step() {
		try {
			setStep(!isStep());
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Pendant TOOl1 button
	 * 
	 * A button used for binding marcos.
	 */
	public void button_tool1() {
		try {
			if (getActiveRobot().getMacroKeyBinds()[0] != null && isShift()) {
				execute(getActiveRobot().getMacroKeyBinds()[0]);
			}
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
		}
	}
	
	/**
	 * Pendant TOOl2 button
	 * 
	 * A button used for binding marcos.
	 */
	public void button_tool2() {
		try {
			if (getActiveRobot().getMacroKeyBinds()[1] != null && isShift()) {
				execute(getActiveRobot().getMacroKeyBinds()[1]);
			}
			
		} catch (Exception Ex) {
			// Log any errors
			DataManagement.errLog(Ex);
			throw Ex;
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
	 */
	public void coordFrameTransition() {
		RoboticArm r = getActiveRobot();
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
			int[] jogMotion = getActiveRobot().getJogMotion();
			
			if (jogMotion == null) {
				UI.updateJogButtons(new int[] {0, 0, 0, 0, 0, 0});
				
			} else {
				UI.updateJogButtons(jogMotion);
			}
			
			Screen activeScreen = getActiveScreen();
			
			if (activeScreen instanceof ST_ScreenTeachPoints) {
				// Draw the teach points for the teach point method screens
				((ST_ScreenTeachPoints) activeScreen).drawTeachPts(getGraphics());
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
			if(rCamEnable) {
				Fields.drawAxes(getGraphics(), rCamera.getPosition(), rCamera.getOrientationMat(), 300, 0);
				
				PVector near[] = rCamera.getPlaneNear();
				PVector far[] = rCamera.getPlaneFar();
				pushMatrix();
				stroke(255, 126, 0, 255);
				
				//Near plane
				line(near[0].x, near[0].y, near[0].z, near[1].x, near[1].y, near[1].z);
				line(near[1].x, near[1].y, near[1].z, near[3].x, near[3].y, near[3].z);
				line(near[3].x, near[3].y, near[3].z, near[2].x, near[2].y, near[2].z);
				line(near[2].x, near[2].y, near[2].z, near[0].x, near[0].y, near[0].z);
				//Far plane
				line(far[0].x, far[0].y, far[0].z, far[1].x, far[1].y, far[1].z);
				line(far[1].x, far[1].y, far[1].z, far[3].x, far[3].y, far[3].z);
				line(far[3].x, far[3].y, far[3].z, far[2].x, far[2].y, far[2].z);
				line(far[2].x, far[2].y, far[2].z, far[0].x, far[0].y, far[0].z);
				//Connecting lines
				line(near[0].x, near[0].y, near[0].z, far[0].x, far[0].y, far[0].z);
				line(near[1].x, near[1].y, near[1].z, far[1].x, far[1].y, far[1].z);
				line(near[2].x, near[2].y, near[2].z, far[2].x, far[2].y, far[2].z);
				line(near[3].x, near[3].y, near[3].z, far[3].x, far[3].y, far[3].z);
												
				popMatrix();
			}
			
			popMatrix();
			
			renderUI();
			
		} catch (Exception Ex) {
			DataManagement.errLog(Ex);
			throw Ex;
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
		screens.getActiveScreen().getContents().setColumnIdx(0);
		progExec(m.getRobot().RID, m.getProg(), 0, ExecType.EXEC_FULL);
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
		
		if (prog == null || getActiveInstIdx() < 0 || getActiveInstIdx() >= prog.getNumOfInst()) {
			// Invalid instruction or program index
			return null;
		}
		
		return prog.getInstAt(getActiveInstIdx());
	}

	/**
	 * @return	The active for the active Robot, or null if no program is active
	 */
	public Program getActiveProg() {
		return progExecState.getProg();
	}

	public RoboticArm getActiveRobot() {
		return activeRobot.get();
	}

	public Scenario getActiveScenario() {
		return activeScenario.get();
	}

	public Screen getActiveScreen() {
		return screens.getActiveScreen();
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
			return ROBOTS.get((getActiveRobot().RID + 1) % 2);

		} catch (Exception Ex) {
			return null;
		}
	}

	/**
	 * Returns the first line in the current list of contents that the
	 * instruction matching the given index appears on.
	 */
	public int getInstrLine(MenuScroll instrMenu, int instrIdx) {
		ArrayList<DisplayLine> instr = instrMenu.copyContents();
		int row = instrIdx;
		
		try {	
			while (instr.get(row).getItemIdx() != instrIdx) {
				row += 1;
				if (screens.getActiveScreen().getContentIdx() >= screens.getActiveScreen().getContents().size() - 1)
					break;
			}
		
			return row;
			
		} catch (NullPointerException NPEx) {
			return 0;
			
		} catch (IndexOutOfBoundsException IOOBEx) {
			//Fields.debug("inst=%d row=%d size=%d\n", instrIdx, row, instr.size());
			return row;
		}
	}

	public KeyCodeMap getKeyCodeMap() {
		return keyCodeMap;
	}

	public Screen getLastScreen() {
		return screens.getPrevScreen();
	}
	
	public ScreenMode getMode() {
		return screens.getActiveScreen().mode;
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
		return record.isRecording();
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
		return !UI.getButtonState(WGUI_Buttons.ObjToggleBounds);
	}

	/**
	 * @return	Is the active robot executing a program?
	 */
	public boolean isProgExec() {
		return !progExecState.isDone();
	}

	public boolean isRCamEnable() {
		return rCamEnable;
	}

	public Boolean isRobotAtPostn(int i) {
		return mInstRobotAt.get(new Integer(i));
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
		}
		
		if (UI != null) {
			
			if (ctrlDown) {
				// Pendant function key shortcuts
				if (keyCode == KeyEvent.VK_1) {
					if (UI.isPendantActive()) {
						button_F1();
					}
					
				} else if (keyCode == KeyEvent.VK_2) {
					if (UI.isPendantActive()) {
						button_F2();
					}
					
				} else if (keyCode == KeyEvent.VK_3) {
					if (UI.isPendantActive()) {
						button_F3();
					}
					
				} else if (keyCode == KeyEvent.VK_4) {
					if (UI.isPendantActive()) {
						button_F4();
					}
					
				} else if (keyCode == KeyEvent.VK_5) {
					if (UI.isPendantActive()) {
						button_F5();
					}
					
				// General key functions
				} else if (keyCode == KeyEvent.VK_D) {
					// Debug output
					Program p = getActiveProg();
					// Output all of the active program's instruction elements
					if (p != null) {
						
						for (InstElement e : p) {
							Fields.debug("%d:\t%s\n", e.getID(), e.getInst());
						}
						
						Fields.debug("");
					}
					
				} else if (keyCode == KeyEvent.VK_E) {
					// Cycle End Effectors
					if (!isProgExec()) {
						getActiveRobot().cycleEndEffector();
						UI.updateListContents();
					}
					
				} else if (keyCode == KeyEvent.VK_F) {
					// Toggle the Robot's End Effector state
					if (!isProgExec()) {
						toggleEEState(getActiveRobot());
					}
					
				} else if (keyCode == KeyEvent.VK_T) {
					// Restore default Robot joint angles
					button_hold();
					float[] rot = { 0, 0, 0, 0, 0, 0 };
					getActiveRobot().releaseHeldObject();
					getActiveRobot().setJointAngles(rot);
					
				} else if (keyCode == KeyEvent.VK_R) {
					// Toggle record state
					setRecord( !getRecord() );
					
				} else if (keyCode == KeyEvent.VK_S) {
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
				
			} else {
				
				if (UIKeyboardUse()) {
					getActiveScreen().actionKeyPress(key);
					
				} else {
					// Robot jogging shortcuts
					switch(keyCode) {
					case KeyEvent.VK_SHIFT:		setShift(true); break;
					case KeyEvent.VK_C:
							// Update the coordinate frame
							coordFrameTransition();
							updatePendantScreen();
							break;
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
					case KeyEvent.VK_MINUS:		button_speedDn(); break;
					case KeyEvent.VK_EQUALS:	button_speedUp(); break;
					case KeyEvent.VK_S:			rCamera.teachObjectToCamera(getActiveScenario()); break;
					case KeyEvent.VK_R:			button_reset();
					}
				}
				
				if (UI.isPendantActive()) {
					// Pendant shortcuts
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
			}
		}
	}
	
	@Override
	public void keyReleased() {
		keyCodeMap.keyReleased(keyCode, key);
		
		if (!keyCodeMap.isKeyDown(KeyEvent.VK_CONTROL) && !UIKeyboardUse()) {
			switch(keyCode) {
			case KeyEvent.VK_SHIFT: 	setShift(false); break;
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
	 */
	public void lastScreen() {
		Screen cur = screens.getActiveScreen();
		
		if (cur.mode != ScreenMode.DEFAULT) {
			Fields.resetMessage();
			screens.lastScreen();
			updatePendantScreen();
			
			Fields.debug("\n%s <= %s\n", cur.mode, screens.getActiveScreen().mode);
		}		
	}
	
	public void mouseDragged(MouseEvent e) {
		WorldObject selectedWO = UI.getSelectedWO();
		
		// Manipulate the selected world object
		if (selectedWO != null && selectedWO == mouseOverWO) {
			PVector camOrien = camera.getOrientation();
			
			pushMatrix();
			resetMatrix();
			rotateZ(-camOrien.z);
			rotateY(-camOrien.y);
			rotateX(-camOrien.x);
			
			RMatrix camRMat = getOrientation();
			
			popMatrix();
			
			if (!mouseDragWO && (mouseButton == CENTER || mouseButton == RIGHT)) {
				// Save the selected world object's current state
				updateScenarioUndo(new WOUndoCurrent(selectedWO));
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
				float mouseXDiff = pmouseX - mouseX;
				float mouseYDiff = pmouseY - mouseY;
				float mouseDiff = (float) Math.sqrt(mouseXDiff * mouseXDiff + mouseYDiff * mouseYDiff);
				float angle = DEG_TO_RAD * mouseDiff / 4f;
				
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
				PVector camOrien = camera.getOrientation();
				
				pushMatrix();
				resetMatrix();
				rotateZ(-camOrien.z);
				rotateY(-camOrien.y);
				rotateX(-camOrien.x);
				
				RMatrix camRMat = getOrientation();
				
				popMatrix();
				
				// Drag the center mouse button to move the object
				PVector translation = new PVector(
						(pmouseX - mouseX),
						(pmouseY - mouseY),
						0f
				);
				
				/* Translate the world object with respect to the native
				 * coordinate system */
				translation = RMath.rotateVector(translation, camRMat);
				
				camera.translate(translation.x, translation.y, translation.z);
			}
	
			if (mouseButton == RIGHT) {
				// Drag right mouse button to rotate the camera
				camera.rotate(pmouseY - mouseY, pmouseX - mouseX, 0f);
			}
		}
	}
	
	@Override
	public void mousePressed() {
		/* Check if the mouse position is colliding with a world object */
		if (!isProgExec() && !UI.isFocus() && getActiveRobot() != null &&
				getActiveScenario() != null) {
			
			PVector camPos = camera.getBasePosition();
			PVector camOrien = camera.getOrientation();
			// Scale the mouse screen position
			PVector mScreenPos = new PVector(mouseX - width / 2f, mouseY -
					height / 2f, camera.getZOffset() + 600f);
			mScreenPos.x *= camera.getScale();
			mScreenPos.y *= camera.getScale();
			
			PVector mWorldPos, ptOnMRay;
			
			pushMatrix();
			resetMatrix();
			// Apply the inverse of the camera's coordinate system
			translate(camPos.x, camPos.y, camPos.z);
			
			rotateX(camOrien.x);
			rotateY(camOrien.y);
			rotateZ(camOrien.z);
			
			translate(mScreenPos.x, mScreenPos.y, mScreenPos.z);
			
			/* Form a ray pointing out of the screen's z-axis, in the
			 * native coordinate system */
			mWorldPos = getPosFromMatrix(0f, 0f, 0f);
			ptOnMRay = getPosFromMatrix(0f, 0f, -1f);
			
			popMatrix();
			// Set the mouse ray origin and direction
			RRay mouseRay = new RRay(mWorldPos, ptOnMRay, 10000f, Fields.BLACK);
			
			if (mouseButton == LEFT) {
				this.mouseRay = mouseRay;
			}
			
			// Check for collisions with objects in the scene
			WorldObject collision = checkForCollisionsInScene(mouseRay);
			
			if (mouseButton == LEFT && collision != null) {
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
			float limboOffset = camera.getZOffset();
			camera.setZOffset(1.05f * limboOffset);
			
		} else if (wheelCount < 0) {
			float limboOffset = camera.getZOffset();
			camera.setZOffset(0.95f * limboOffset);
		}
	}

	public void newCallInstruction() {
		RoboticArm r = getActiveRobot();
		Program p = getActiveProg();
		CallInstruction call = new CallInstruction(getActiveRobot());

		if (getActiveInstIdx() != p.getNumOfInst()) {
			r.replaceInstAt(p, getActiveInstIdx(), call);
		} else {
			r.addInstAtEnd(p, call, false);
		}
	}

	public void newFrameInstruction(int fType) {
		RoboticArm r = getActiveRobot();
		Program p = getActiveProg();
		FrameInstruction f = new FrameInstruction(fType, -1);

		if (getActiveInstIdx() != p.getNumOfInst()) {
			r.replaceInstAt(p, getActiveInstIdx(), f);
		} else {
			r.addInstAtEnd(p, f, false);
		}
	}
	
	public void newIfExpression() {
		RoboticArm r = getActiveRobot();
		Program p = getActiveProg();
		IfStatement stmt = new IfStatement();

		if (getActiveInstIdx() != p.getNumOfInst()) {
			r.replaceInstAt(p, getActiveInstIdx(), stmt);
		} else {
			r.addInstAtEnd(p, stmt, false);
		}
	}

	public void newIfStatement() {
		RoboticArm r = getActiveRobot();
		Program p = getActiveProg();
		IfStatement stmt = new IfStatement(Operator.EQUAL, null);
		opEdit = stmt.getExpr();
		
		if (getActiveInstIdx() != p.getNumOfInst()) {
			r.replaceInstAt(p, getActiveInstIdx(), stmt);
		} else {
			r.addInstAtEnd(p, stmt, false);
		}
	}

	public void newIOInstruction(int ioIdx, boolean state) {
		RoboticArm r = getActiveRobot();
		Program p = getActiveProg();
		IOInstruction io = new IOInstruction(ioIdx, state);

		if (getActiveInstIdx() != p.getNumOfInst()) {
			r.replaceInstAt(p, getActiveInstIdx(), io);

		} else {
			r.addInstAtEnd(p, io, false);
		}
	}

	public void newJumpInstruction() {
		RoboticArm r = getActiveRobot();
		Program p = getActiveProg();
		JumpInstruction j = new JumpInstruction(-1);

		if (getActiveInstIdx() != p.getNumOfInst()) {
			r.replaceInstAt(p, getActiveInstIdx(), j);
		} else {
			r.addInstAtEnd(p, j, false);
		}
	}

	public void newLabel() {
		RoboticArm r = getActiveRobot();
		Program p = getActiveProg();

		LabelInstruction l = new LabelInstruction(-1);
		
		if (getActiveInstIdx() != p.getNumOfInst()) {
			r.replaceInstAt(p, getActiveInstIdx(), l);
		} else {
			r.addInstAtEnd(p, l, false);
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
		
		Point pt = getActiveRobot().getToolTipUser();
		
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
				getActiveRobot().replaceInstAt(prog, getActiveInstIdx(), mInst);
			} 
			else {
				// Insert the new motion instruction
				getActiveRobot().addInstAtEnd(prog, mInst, false);
			}
		}
		
		// Set the fields of the motion instruction
		
		CoordFrame coord = getActiveRobot().getCurCoordFrame();
		
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
			PositionRegister pReg = getActiveRobot().getPReg(regNum);
			pReg.point = pt;
			
		}  else if (mInst.getPosType() == Fields.PTYPE_PROG) {
			prog.setPosition(regNum, pt);
			
			if (screens.getActiveScreen().getContents().getItemLineIdx() > 0) {
				mInst.setCircPosIdx(regNum);
				
			} else {
				mInst.setPosIdx(regNum);
			}
		}
		
		mInst.setSpdMod(0.5f);
		mInst.setTFrameIdx(getActiveRobot().getActiveToolIdx());
		mInst.setUFrameIdx(getActiveRobot().getActiveUserIdx());
	}

	public void newRegisterStatement(Register reg) {
		RoboticArm r = getActiveRobot();
		Program p = getActiveProg();
		RegisterStatement stmt = new RegisterStatement(reg);

		if (getActiveInstIdx() != p.getNumOfInst()) {
			r.replaceInstAt(p, getActiveInstIdx(), stmt);
		} else {
			r.addInstAtEnd(p, stmt, false);
		}
	}

	public void newRegisterStatement(Register reg, int i) {
		RoboticArm r = getActiveRobot();
		Program p = getActiveProg();
		RegisterStatement stmt = new RegisterStatement(reg, i);

		if (getActiveInstIdx() != p.getNumOfInst()) {
			r.replaceInstAt(p, getActiveInstIdx(), stmt);
		} else {
			r.addInstAtEnd(p, stmt, false);
		}
	}
	
	public void newRobotCallInstruction() {
		RoboticArm r = getActiveRobot();
		Program p = getActiveProg();
		CallInstruction rcall = new CallInstruction(getInactiveRobot());

		if (getActiveInstIdx() != p.getNumOfInst()) {
			r.replaceInstAt(p, getActiveInstIdx(), rcall);
		} else {
			r.addInstAtEnd(p, rcall, false);
		}
	}

	public void newSelectStatement() {
		RoboticArm r = getActiveRobot();
		Program p = getActiveProg();
		SelectStatement stmt = new SelectStatement();

		if (getActiveInstIdx() != p.getNumOfInst()) {
			r.replaceInstAt(p, getActiveInstIdx(), stmt);
		} else {
			r.addInstAtEnd(p, stmt, false);
		}
	}
	
	/**
	 * Updates the save state of the active screen and loads the given screen
	 * mode afterwards.
	 * 
	 * @param nextScreen	The new screen mode
	 */
	public void nextScreen(ScreenMode nextScreen) {
		Screen prevScreen = screens.getActiveScreen();
		screens.nextScreen(nextScreen);
		updatePendantScreen();
		Fields.resetMessage();
				
		Fields.debug("\n%s => %s\n", prevScreen.mode, nextScreen);
	}
	
	public void pasteInstructions() {
		pasteInstructions(0);
	}
	
	public void pasteInstructions(int options) {
		RoboticArm r = getActiveRobot();
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
						Fields.debug("asdf");
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
			
			r.addAt(p, getActiveInstIdx() + i, instr, i != 0);
		}
	}
	
	/**
	 * Wrapper method for the ScreenManager.popScreenStack() of screens.
	 */
	public void resetStack() {
		screens.resetStack();
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
		if (screens.getActiveScreen() instanceof ScreenNavProgInstructions) {
			Instruction inst = getActiveInstruction();
			
			if (inst instanceof PosMotionInst) {
				PosMotionInst mInst = (PosMotionInst)inst;
				int sdx = screens.getActiveScreen().getContents().getItemColumnIdx();
				
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
	 * Sets the given program as the active program.
	 * 
	 * @param p	The program to set as active
	 */
	public void setActiveProg(Program p) {
		progExecState.setProg(p);
	}

	/**
	 * Sets the active program of this Robot corresponding to the index value
	 * given.
	 * 
	 * @param progIdx	The index of the program to set as active
	 * @return			Whether the given index is valid
	 */
	public boolean setActiveProgIdx(int progIdx) {
		Program p = getActiveRobot().getProgram(progIdx);
		
		if (p != null || progIdx == -1) {
			// Set the active program
			progExecState.setProg(p);
		}
		
		return p != null;
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
				activeScenario.set(s);
				
				if (UI != null) {
					UI.setSelectedWO(null);
				}
				
				return true;
			}
		}

		return false;

	}

	public void setRCamEnable(boolean enable) {
		rCamEnable = enable;
	}
	
	public void setRecord(boolean state) {
		record.setRecording(state);
	}
	
	/**
	 * Sets the position, which is render in the world frame, when the render
	 * point option is set.
	 * 
	 * @param pt	The point to render
	 */
	public void setRenderPoint(Point pt) {
		position = pt;
	}
	
	/**
	 * Update the active Robot to the Robot at the given index in the list of
	 * Robots.
	 * 
	 * @param rdx
	 *            The index of the new active Robot
	 */
	public void setRobot(int rdx) {
		if (rdx >= 0 && rdx < ROBOTS.size() && ROBOTS.get(rdx) != getActiveRobot()) {
			button_hold();

			RoboticArm prevActive = getActiveRobot();
			activeRobot.set( ROBOTS.get(rdx) );

			if (prevActive != activeRobot.get()) {
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
			getActiveRobot().halt();
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
		
		mouseRay = null;
		position = null;
		mouseOverWO = null;
		mouseDragWO = false;
		
		PImage[][] buttonImages = new PImage[][] {
			
			{ loadImage("images/arrow-up.png"), loadImage("images/arrow-up_over.png"), loadImage("images/arrow-up_down.png") },
			{ loadImage("images/arrow-down.png"), loadImage("images/arrow-down_over.png"), loadImage("images/arrow-down_down.png") },
			{ loadImage("images/arrow-l.png"), loadImage("images/arrow-l_over.png"), loadImage("images/arrow-l_down.png") },
			{ loadImage("images/arrow-r.png"), loadImage("images/arrow-r_over.png"), loadImage("images/arrow-r_down.png") }
			
		};
		
		instance = this;
		
		RegisteredModels.loadModelDefs(this);
		
		// create font and text display background
		Fields.medium = createFont("fonts/Consolas.ttf", 14);
		Fields.small = createFont("fonts/Consolas.ttf", 12);
		Fields.bond = createFont("fonts/ConsolasBold.ttf", 12);
		
		camera = new Camera(1000f, 100f, 8000f);
		rCamera = new RobotCamera();
		robotTrace = new RTrace();
		activeRobot = new Pointer<>(null);
		activeScenario = new Pointer<>(null);
		
		CallInstruction.setRobotRef(activeRobot);
		OperandCamObj.setCamRef(rCamera);
		OperandCamObj.setScenarioRef(activeScenario);
		
		keyCodeMap = new KeyCodeMap();
		progExecState = new ProgExecution();
		progCallStack = new Stack<>();
		mInstRobotAt = new HashMap<>();
		
		UI = new WGUI(this, buttonImages);
		record = new RecordScreen();
		
		background(255);
		
		// load model and save data
		try {
			DataManagement.initialize(this);
			
			RoboticArm r = new RoboticArm(0, new PVector(200, Fields.FLOOR_Y,
					200), robotTrace);
			ROBOTS.put(r.RID, r);
			
			r = new RoboticArm(1, new PVector(200, Fields.FLOOR_Y, -750),
					robotTrace);
			ROBOTS.put(r.RID, r);

			activeRobot.set( ROBOTS.get(0) );
			
			DataManagement.loadState(this);
			
			screens = new ScreenManager(this);
			
			if(rCamEnable) {
				UI.toggleCamera();
				UI.updateUIContentPositions();
			}
			
			updatePendantScreen();

		} catch (NullPointerException NPEx) {
			DataManagement.errLog(NPEx);
			throw NPEx;
		}
		
		/**
		RMatrix rx = RMath.formRMat(new PVector(1f, 0f, 0f), 135f * DEG_TO_RAD);
		RMatrix ry = RMath.formRMat(new PVector(0f, 1f, 0f), 135f * DEG_TO_RAD);
		RMatrix rz = RMath.formRMat(new PVector(0f, 0f, 1f), 135f * DEG_TO_RAD);
		
		Fields.debug("%s\n%s\n%s\n", rx, ry, rz);
		/**/
	}

	/**
	 * Removes the current screen from the screen state stack and loads the
	 * given screen mode.
	 * 
	 * @param nextScreen The new screen mode
	 */
	public void switchScreen(ScreenMode nextScreen) {
		Screen prevScreen = screens.getActiveScreen();
		screens.switchScreen(nextScreen);
		updatePendantScreen();
		Fields.resetMessage();
				
		Fields.debug("\n%s => %s\n", prevScreen.mode, nextScreen);
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
		boolean curState = robot.getEEState();
		
		if (curState == Fields.ON) {
			robot.setEEState(edx, Fields.OFF);
		} else {
			robot.setEEState(edx, Fields.ON);
		}
		// Check pickup collisions in active scenario
		robot.checkPickupCollision(getActiveScenario());
	}

	/**
	 * Is the trace function enabled. The user can enable/disable this function
	 * with a button in the miscellaneous window. In addition, the active
	 * robot's end effector trace overrides the state of the trace toggle
	 * button ( see RoboticArm.isEETraceEnabled() ).
	 * 
	 * @return	If the trace functionality is enabled
	 */
	public boolean traceEnabled() {
		return getActiveRobot().isEETraceEnabled() ||
				UI.getButtonState(WGUI_Buttons.RobotToggleTrace);
	}

	/**
	 * Revert the most recent change to the active scenario
	 */
	public void undoScenarioEdit() {
		if (!SCENARIO_UNDO.empty()) {
			SCENARIO_UNDO.pop().undo();
			UI.updateListContents();
			
			WorldObject wo = UI.getSelectedWO();
			
			if (wo != null) {
				UI.updateEditWindowFields(wo);
				
				if (wo instanceof Fixture) {
					for (WorldObject wldObj : getActiveScenario()) {
						if (wldObj instanceof Part) {
							Part p = (Part)wldObj;

							if (p.getFixtureRef() == wo) {
								p.updateAbsoluteOrientation();
							}
						}
					}
				}
			}
			
			/* Since objects are copied onto the undo stack, the robot may be
			 * reference the wrong copy of an undone object. */
			getActiveRobot().releaseHeldObject();
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
		screens.getActiveScreen().updateScreen();
		UI.renderPendantScreen(screens.getActiveScreen());
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
	public void updateRobotEEState(int edx, boolean newState) {
		updateRobotEEState(getActiveRobot(), edx, newState);
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
	public void updateRobotEEState(RoboticArm r, int edx, boolean newState) {
		r.setEEState(edx, newState);
		
		if (getActiveScenario() != null) {
			r.checkPickupCollision(getActiveScenario());
		}
	}
	
	public void updateRobotJogMotion(int set, int direction) {
		if (isShift() && !isProgExec()) {
			boolean robotInMotion = getActiveRobot().inMotion();
			
			getActiveRobot().updateJogMotion(set, direction);
			
			if (robotInMotion && !getActiveRobot().inMotion()) {
				// Robot has stopped moving
				updateInstList();
			}
		}
	}
	
	/**
	 * Push a world object onto the undo stack for world objects.
	 * 
	 * @param undoState	The world object to save
	 */
	public void updateScenarioUndo(WOUndoState undoState) {
		if (undoState != null) {
			// Only the latest 40 world object save states can be undone
			if (SCENARIO_UNDO.size() >= 40) {
				SCENARIO_UNDO.remove(0);
			}
	
			SCENARIO_UNDO.push(undoState);
		}
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
			return checkForRayCollisions(ray, getActiveScenario(), ROBOTS.get(0));
			
		}
		
		return checkForRayCollisions(ray, getActiveScenario(), ROBOTS.get(0),
				ROBOTS.get(1));
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
	 * @param robots	The robots, which which to check collisions with the
	 * 					given ray
	 * @return			The object, with which, the given ray collides
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
					RShape form = wo.getModel();
					
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
	 * Begins forward program execution of the active robot's active program
	 * from the active instruction index.
	 * 
	 * @param singleExec	Whether to execute a single instruction or the
	 * 						entire program
	 */
	private void progExec(boolean singleExec) {
		ExecType pExec = (singleExec) ? ExecType.EXEC_SINGLE
				: ExecType.EXEC_FULL;
		progExecState.setType(pExec);
	}
	
	/**
	 * Begins forward program execution of the active robot's active program
	 * from the specified instruction index.
	 * 
	 * @param instIdx		The index of the instruction from which to begin
	 * 						program execution
	 * @param singleExec	Whether to execute a single instruction or the
	 * 						entire program
	 */
	@SuppressWarnings("unused")
	private void progExec(int instIdx, boolean singleExec) {
		progExec(progExecState.getProg(), instIdx, singleExec);
	}

	/**
	 * Begins forward program execution for the program specified by the given
	 * program index in the active robot's list of programs. Program execution
	 * begins at the specified instruction index.
	 * 
	 * @param progIdx		The index of the program to execute
	 * @param instIdx		The index of the instruction form which to begin
	 * 						program execution
	 * @param singleExec	Whether to execute a single instruction or the
	 * 						entire program
	 */
	private void progExec(Program p, int instIdx, boolean singleExec) {
		// Validate active indices
		if (p != null && instIdx >= 0 && instIdx < p.getNumOfInst()) {
			ExecType pExec = (singleExec) ? ExecType.EXEC_SINGLE
					: ExecType.EXEC_FULL;
			
			progExec(getActiveRobot().RID, p, instIdx, pExec);
		}
	}
	
	/**
	 * Begins forward program execution for the program specified by the given
	 * program index in the robot's, specified by the given robot ID, list of
	 * programs. Program execution begins at the specified instruction index.
	 * 
	 * @param rid		The index of the robot for which to bring begin program
	 * 					execution
	 * @param progIdx	The index of the program to execute
	 * @param instIdx	The index of the instruction from which to begin
	 * 					program execution
	 * @param exec		Whether to execute a single instruction or the entire
	 * 					program
	 */
	private void progExec(int rid, Program p, int instIdx, ExecType exec) {
		progExecState.setExec(rid, exec, p, instIdx);
	}
	
	/**
	 * Begins backward program execution for the active program beginning from
	 * the active instruction index.
	 */
	private void progExecBwd() {
		Program p = getActiveProg();
		
		if (p != null && getActiveInstIdx() >= 1 && getActiveInstIdx() < p.getNumOfInst()) {
			/* The program must have a motion instruction prior to the active
			 * instruction for backwards execution to be valid. */
			Instruction prevInst = p.getInstAt(getActiveInstIdx() - 1);
			
			if (prevInst instanceof MotionInstruction) {
				progExec(getActiveRobot().RID, progExecState.getProg(),
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
	 * Updates the position and orientation of the Robot as well as all the
	 * World Objects associated with the current scenario. Updates the bounding
	 * box color, position and orientation of the Robot and all World Objects as
	 * well. Finally, all the World Objects and the Robot are drawn.
	 */
	private void renderScene() {
		
		if (isProgExec()) {
			updateProgExec();
		}
		
		boolean robotInMotion = getActiveRobot().inMotion();
		
		getActiveRobot().updateRobot();
		
		if (robotInMotion && !getActiveRobot().inMotion()) {
			// Robot has stopped moving
			updateInstList();
		}
		
		if (isProgExec()) {
			updateCurIdx();
		}

		if (getActiveScenario() != null) {
			getActiveScenario().resetObjectHitBoxColors();
		}

		getActiveRobot().resetOBBColors();
		getActiveRobot().checkSelfCollisions();

		if (getActiveScenario() != null) {
			WorldObject selected = UI.getSelectedWO();
			int numOfObjects = getActiveScenario().size();

			for (int idx = 0; idx < numOfObjects; ++idx) {
				WorldObject wldObj = getActiveScenario().getWorldObject(idx);
				
				if (wldObj instanceof Part) {
					Part p = (Part)wldObj;

					/* Update the transformation matrix of an object held by the Robotic Arm */
					if(getActiveRobot() != null && getActiveRobot().isHeld(p) && getActiveRobot().inMotion()) {
						
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
						
						RMatrix curTip = getActiveRobot().getFaceplateTMat(getActiveRobot().getJointAngles());
						RMatrix invMat = getActiveRobot().getLastTipTMatrix().getInverse();
						Fixture refFixture = p.getFixtureRef();
						
						pushMatrix();
						resetMatrix();
						
						if (refFixture != null) {
							applyMatrix(refFixture.getInvCoordinateSystem());
						}
						
						applyMatrix(curTip);
						applyMatrix(invMat);
						applyCoord(p.getCenter(), p.getOrientation());
						
						// Update the world object's position and orientation
						p.setLocalCenter( getPosFromMatrix(0f, 0f, 0f) );
						p.setLocalOrientation( getOrientation() );
						
						popMatrix();
					}
					
					
					if (getActiveScenario().isGravity() && getActiveRobot().isHeld(p) &&
							p != selected && p.getFixtureRef() == null &&
							p.getLocalCenter().y < Fields.FLOOR_Y) {
						
						// Apply gravity
						PVector c = wldObj.getLocalCenter();
						wldObj.updateLocalCenter(null, c.y + 10, null);
					}

					/* Collision Detection */
					if(isOBBRendered()) {
						if( getActiveRobot() != null && getActiveRobot().checkCollision(p) ) {
							p.setBBColor(Fields.OBB_COLLISION);
						}

						// Detect collision with other objects
						for(int cdx = idx + 1; cdx < getActiveScenario().size(); ++cdx) {

							if (getActiveScenario().getWorldObject(cdx) instanceof Part) {
								Part p2 = (Part)getActiveScenario().getWorldObject(cdx);

								if(p.collision(p2)) {
									// Change hit box color to indicate Object collision
									p.setBBColor(Fields.OBB_COLLISION);
									p2.setBBColor(Fields.OBB_COLLISION);
									break;
								}
							}
						}

						if (getActiveRobot() != null && !getActiveRobot().isHeld(p) &&
								getActiveRobot().canPickup(p)) {
							
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
		
		getActiveRobot().updateLastTipTMatrix();
		
		AxesDisplay axesType = getAxesState();
		
		if (axesType != AxesDisplay.NONE &&
			(getActiveRobot().getCurCoordFrame() == CoordFrame.WORLD
				|| getActiveRobot().getCurCoordFrame() == CoordFrame.TOOL
				|| (getActiveRobot().getCurCoordFrame() == CoordFrame.USER
					&& getActiveRobot().getActiveUser() == null))) {
			
			// Render the world frame
			PVector origin = new PVector(0f, 0f, 0f);
			
			if (axesType == AxesDisplay.AXES) {
				Fields.drawAxes(getGraphics(), origin, Fields.WORLD_AXES_MAT,
						10000f, Fields.BLACK);
				
			} else if (axesType == AxesDisplay.GRID) {
				g.pushMatrix();
				// Draw the gridlines at the base of the robot
				PVector basePos = getActiveRobot().getBasePosition();
				g.translate(0f, basePos.y, 0f);
				getActiveRobot().drawGridlines(getGraphics(), Fields.WORLD_AXES_MAT,
						origin, 35, 100f);
				g.popMatrix();
			}
		}
		

		if (UI.getRobotButtonState()) {
			// Draw all robots
			for (RoboticArm r : ROBOTS.values()) {
				
				if (r == getActiveRobot()) {
					// active robot
					r.draw(getGraphics(), isOBBRendered(), axesType);
					
				} else {
					r.draw(getGraphics(), false, AxesDisplay.NONE);
				}
				
			}

		} else {
			// Draw only the active robot
			getActiveRobot().draw(getGraphics(), isOBBRendered(), axesType);
		}
		
		if (getActiveRobot().inMotion() && traceEnabled()) {
			Point tipPosNative = getActiveRobot().getToolTipNative();
			// Update the robots trace points
			if(robotTrace.isEmpty()) {
				robotTrace.addPt(tipPosNative.position);
				
			} else {
				PVector lastTracePt = robotTrace.getLastPt();
				
				if (lastTracePt == null || PVector.sub(tipPosNative.position,
						lastTracePt).mag() > 0.5f) {
					
					robotTrace.addPt(tipPosNative.position);
				}
			}
		}
		
		robotTrace.draw(g);
		
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
		
		// Render last mouse ray
		if (UI.renderMouseRay() && mouseRay != null) {
			mouseRay.draw(getGraphics());
		}
		
		// Render a stored point
		if (UI.renderPoint() && position != null && position.position != null
				&& position.orientation != null) {
			
			Fields.drawAxes(getGraphics(), position.position,
					position.orientation.toMatrix(), 100f, 0);
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
		CoordFrame coord = getActiveRobot().getCurCoordFrame();
		String coordFrame;
		
		if (coord == null) {
			// Invalid state for coordinate frame
			coordFrame = "Coordinate Frame: N/A";
			
		} else {
			coordFrame = "Coordinate Frame: " + coord.toString();
		}
		
		Point RP = getActiveRobot().getToolTipNative();

		String[] cartesian = RP.toLineStringArray(true), joints = RP.toLineStringArray(false);
		// Display the current Coordinate Frame name
		text(coordFrame, lastTextPositionX, lastTextPositionY);
		lastTextPositionY += 20;
		// Display the Robot's speed value as a percent
		text(String.format("Jog Speed: %d%%", getActiveRobot().getLiveSpeed()),
				lastTextPositionX, lastTextPositionY);
		lastTextPositionY += 20;

		if (getActiveScenario() != null) {
			String out = String.format("Active scenario: %s", getActiveScenario().getName());
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

		UserFrame active = getActiveRobot().getActiveUser();

		if (active != null) {
			// Display Robot's current position and orientation in the currently
			// active User frame
			RP.position = RMath.vToFrame(RP.position, active.getOrigin(), active.getOrientation());
			RP.orientation = active.getOrientation().transformQuaternion(RP.orientation);
			cartesian = RP.toLineStringArray(true);

			lastTextPositionY += 20;
			text(String.format("User: %d", getActiveRobot().getActiveUserIdx() + 1), lastTextPositionX,
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
		
		// Render camera values
		String[] cameraValues = camera.toStringArray();
		lastTextPositionY += 20;
		text("Camera", lastTextPositionX, lastTextPositionY);
		lastTextPositionY += 20;
		String line = String.format("%-12s %-12s %-12s", cameraValues[0],
				cameraValues[1], cameraValues[2]);
		text(line, lastTextPositionX, lastTextPositionY);
		lastTextPositionY += 20;
		line = String.format("%-12s %-12s %-12s", cameraValues[3],
				cameraValues[4], cameraValues[5]);
		text(line, lastTextPositionX, lastTextPositionY);
		lastTextPositionY += 20;
		line = String.format("%-12s", cameraValues[6]);
		text(line, lastTextPositionX, lastTextPositionY);
		lastTextPositionY += 20;
		
		// Display the current axes display state
		lastTextPositionY += 20;
		text(String.format("Axes Display: %s", getAxesState().name()),
				lastTextPositionX, lastTextPositionY);
		lastTextPositionY += 20;	
		
		pushStyle();
		fill(215, 0, 0);
		lastTextPositionY += 20;
		
		// Display a message while the robot is carrying an object
		if (!getActiveRobot().isHeld(null)) {
			text("Object held", lastTextPositionX, lastTextPositionY);
			lastTextPositionY += 20;
		}

		// Display a message when there is an error with the Robot's
		// movement
		if (getActiveRobot().hasMotionFault()) {
			text("Motion Fault (press SHIFT + RESET)", lastTextPositionX,
					lastTextPositionY);
			lastTextPositionY += 20;
		}
		
		if (isProgExec()) {
			text("Program executing", lastTextPositionX, lastTextPositionY);
			lastTextPositionY += 20;
		}

		// Display a message if the Robot is in motion
		if (getActiveRobot().inMotion()) {
			text("Robot is moving", lastTextPositionX, lastTextPositionY);
			lastTextPositionY += 20;
		}
		
		if (record.isRecording()) {
			text("Recording (press Ctrl + Alt + r)",
					lastTextPositionX, lastTextPositionY);
			lastTextPositionY += 20;
		}
		
		popStyle();
		
		Screen activeScreen = getActiveScreen();
		
		if (activeScreen instanceof ST_ScreenTeachPoints &&
				((ST_ScreenTeachPoints) activeScreen).readyToTeach() &&
				Fields.msgSystem.getMessage() == null) {
			// Prompt the user to teach a frame when all points are taught
			Fields.setMessage("Press ENTER to teach the frame");
		}
		
		lastTextPositionY += 20;
		lastTextPositionY = Fields.msgSystem.draw(getGraphics(),
				lastTextPositionX, lastTextPositionY);
		
		UI.updateAndDrawUI();
		
		popStyle();
		popMatrix();
	}

	/**
	 * Determines if the the current window uses keyboard input.
	 * 
	 * @return	if the active UI element or window uses keyboard input
	 */
	private boolean UIKeyboardUse() {
		if (UI != null) {
			if (UI.isPendantActive()) {
				// Screens extending these screen types use keyboard input.
				Screen activeScreen = getActiveScreen();
				
				return activeScreen instanceof ST_ScreenPointEntry ||
						activeScreen instanceof ST_ScreenTextEntry ||
						activeScreen instanceof ST_ScreenNumEntry;
				
			} else if (UI.getMenu() != null) {
				// Textfields and some dropdown lists use text input
				return UI.isATextFieldActive() || UI.isMouseOverUIElement();
			}
		}
		
		return false;
	}
	
	/**
	 * Updates the instruction index based on the current program execution
	 * state and the base program execution state.
	 */
	private void updateCurIdx() {
		if (progExecState.getState() == ExecState.EXEC_MINST &&
				!getActiveRobot().inMotion()) {
			
			if (getActiveRobot().hasMotionFault()) {
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
			
			if (nextIdx < 0 || nextIdx > prog.getNumOfInst()) {
				// Encountered a fault in program execution
				progExecState.setState(ExecState.EXEC_FAULT);
				
			} else {
				progExecState.setCurIdx(nextIdx);
				
				if (nextIdx == prog.getNumOfInst()) {
					
					if (!progCallStack.isEmpty()) {
						// Return to the program state on the top of the call stack
						ProgExecution prevExec = progCallStack.pop();
						RoboticArm r = getRobot(prevExec.getRID());
						
						if (r != null) {
							progExecState = prevExec;
							
							if (r.RID != getActiveRobot().RID) {
								// Update the active robot
								activeRobot.set(ROBOTS.get(progExecState.getRID()));
								screens.getActiveScreen().getContents().setColumnIdx(0);
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
		if(screens.getActiveScreen().mode == ScreenMode.NAV_PROG_INSTR) {
			Screen s = screens.getActiveScreen();
			s.setContentIdx(getInstrLine(s.getContents(), getActiveInstIdx()));
		}
		
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
		if (screens.getActiveScreen() instanceof ScreenNavProgInstructions) {
			Program prog = getActiveProg();
			Point robotPos = getActiveRobot().getToolTipNative();
			boolean updatedLines = false;
			
			// Check each instruction in the active program
			for (int idx = 0; idx < prog.getNumOfInst(); ++idx) {
				Instruction inst = prog.getInstAt(idx);
				Integer keyInt = new Integer(idx);
				
				if (inst instanceof PosMotionInst) { 
					PosMotionInst mInst = (PosMotionInst)prog.getInstAt(idx);
					Point instPt = getActiveRobot().getVector(mInst, prog, false);
					
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
	
	/**
	 * Executes the program associated with the active instruction index for
	 * the current program execution state.
	 * 
	 * @return	0 ...
	 */
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
					int ret = getActiveRobot().setupMInstMotion(prog, motInstr, nextIdx,
							progExecState.isSingleExec());
					
					if (ret != 0) {
						// Issue occurred with setting up the motion instruction
						nextIdx = -1;
					}
					
				} else if (activeInstr instanceof FrameInstruction) {
					FrameInstruction fInst = (FrameInstruction)activeInstr;
					
					if (fInst.getFrameType() == Fields.FTYPE_TOOL) {
						getActiveRobot().setActiveToolFrame(fInst.getFrameIdx());
						
					} else if (fInst.getFrameType() == Fields.FTYPE_USER) {
						getActiveRobot().setActiveUserFrame(fInst.getFrameIdx());
					}
					
				} else if (activeInstr instanceof IOInstruction) {
					IOInstruction ioInst = (IOInstruction)activeInstr;
					updateRobotEEState(ioInst.getReg(), ioInst.getState());
					
				} else if (activeInstr instanceof JumpInstruction) {
					JumpInstruction jInst = (JumpInstruction)activeInstr;
					nextIdx = prog.findLabelIdx(jInst.getTgtLblNum());
	
				} else if (activeInstr instanceof CallInstruction) {
					CallInstruction cInst = (CallInstruction)activeInstr;
					
					if (cInst.getTgtDevice() != getActiveRobot() &&
							!isSecondRobotUsed()) {
						// Cannot use robot call, when second robot is not active
						nextIdx = -1;
						
					} else {
						progExecState.setNextIdx(nextIdx);
						pushActiveProg();
						
						if (cInst.getTgtDevice() == getActiveRobot()) {
							// Normal call instruction
							progExecState.setExec(getActiveRobot().RID,
									progExecState.getType(), cInst.getProg(),
									0);
														
						} else {
							// Robot call instruction
							RoboticArm r = getInactiveRobot();
							activeRobot.set(r);
							progExecState.setExec(r.RID, progExecState.getType(),
									cInst.getProg(), 0);
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
							
							if (cInst.getTgtDevice() != getActiveRobot() &&
									!isSecondRobotUsed()) {
								// Cannot use robot call, when second robot is not active
								nextIdx = -1;
								
							} else {
								progExecState.setNextIdx(nextIdx);
								pushActiveProg();
								
								if (cInst.getTgtDevice() == getActiveRobot()) {
									// Normal call instruction
									progExecState.setExec(getActiveRobot().RID,
											progExecState.getType(),
											cInst.getProg(), 0);
																
								} else {
									// Robot call instruction
									RoboticArm r = getInactiveRobot();
									activeRobot.set(r);
									progExecState.setExec(r.RID,
											progExecState.getType(),
											cInst.getProg(), 0);
								}
								
								nextIdx = 0;
							}
							
						} else {
							nextIdx = -1;
						}
						
					} else if (ret == 2) {
						// Evaluation failed
						Fields.setMessage("Expression evaluation error");
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
							
							if (cInst.getTgtDevice() != getActiveRobot() &&
									!isSecondRobotUsed()) {
								// Cannot use robot call, when second robot is not active
								nextIdx = -1;
								
							} else {
								progExecState.setNextIdx(nextIdx);
								pushActiveProg();
								
								if (cInst.getTgtDevice() == getActiveRobot()) {
									// Normal call instruction
									progExecState.setExec(getActiveRobot().RID,
											progExecState.getType(),
											cInst.getProg(), 0);
																
								} else {
									// Robot call instruction
									RoboticArm r = getInactiveRobot();
									activeRobot.set(r);
									progExecState.setExec(r.RID, progExecState.getType(),
											cInst.getProg(), 0);
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
						Fields.setMessage("Expression evaluation error");
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

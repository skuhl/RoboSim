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
import enums.ScreenMode;
import enums.ScreenType;
import expression.AtomicExpression;
import expression.Expression;
import expression.ExpressionElement;
import expression.Operand;
import expression.OperandBool;
import expression.OperandDReg;
import expression.OperandFloat;
import expression.OperandGeneric;
import expression.OperandIOReg;
import expression.OperandPReg;
import expression.OperandPRegIdx;
import expression.Operator;
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
import geom.RQuaternion;
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
import programming.CamMoveToObject;
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
import screen.ScreenState;
import ui.Camera;
import ui.DisplayLine;
import ui.KeyCodeMap;
import ui.MenuScroll;
import window.WGUI;

/**
 * TODO general comments
 * 
 * @author Vincent Druckte, Joshua Hooker, and James Walker
 */
public class RobotRun extends PApplet {

	/* A set of letters, used by the pendant function keys, when the users is
	 * inputing a text entry. */
	private static final char[][] LETTERS;
	private static final int ITEMS_TO_SHOW;
	private static final int NUM_ENTRY_LEN;
	private static final int TEXT_ENTRY_LEN;
	private static RobotRun instance;

	/**
	 * Initialize all static fields
	 */
	static {
		LETTERS = new char[][] {
			{ 'a', 'b', 'c', 'd', 'e', 'f' }, 
			{ 'g', 'h', 'i', 'j', 'k', 'l' },
			{ 'm', 'n', 'o', 'p', 'q', 'r' }, 
			{ 's', 't', 'u', 'v', 'w', 'x' }, 
			{ 'y', 'z', '_', '@', '*', '.' }
		};

		ITEMS_TO_SHOW = 8;
		NUM_ENTRY_LEN = 9;
		TEXT_ENTRY_LEN = 16;
		instance = null;
	}
	
	public static RoboticArm getInstanceRobot() {
		return instance.activeRobot;
	}
	
	public static Scenario getInstanceScenario() {
		return instance.activeScenario;
	}

	/**
	 * Returns the instance of this PApplet
	 */
	public static RobotRun getInstance() {
		return instance;
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

	private ScreenMode mode;
	private MenuScroll contents;
	private MenuScroll options;

	private Stack<ScreenState> screenStates;
	private ArrayList<Macro> macros = new ArrayList<>();
	private Macro[] SU_macro_bindings = new Macro[7];
	private Macro edit_macro;

	private boolean shift = false; // Is shift button pressed or not?
	private boolean step = false; // Is step button pressed or not?
	private boolean camEnable = false;

	private boolean record;
	
	/**
	 * A temporary storage string for user input in the pendant window.
	 */
	private StringBuilder workingText;
	
	/**
	 * Used with rendering of workingText.
	 */
	private String workingTextSuffix;
	boolean speedInPercentage;

	/**
	 * Index of the current frame (Tool or User) selecting when in the Frame
	 * menus
	 */
	private int curFrameIdx = -1;

	/**
	 * The Frame being taught, during a frame teaching process
	 */
	private Frame teachFrame = null;

	// Expression operand currently being edited
	public Operand<?> opEdit = null;

	public int editIdx = -1;

	// container for instructions being copied/ cut and pasted
	ArrayList<Instruction> clipBoard = new ArrayList<>();
	
	/**
	 * A list of instruction indexes for the active program, which point to
	 * motion instructions, whose position and orientation (or joint angles)
	 * are close enough to the robot's current position.
	 */
	private HashMap<Integer, Boolean> mInstRobotAt;
	
	private int active_index = 0;

	/**
	 * Used for comment name input. The user can cycle through the six states
	 * for each function button in this mode:
	 *
	 * F1 -> A-F/a-f F2 -> G-L/g-l F3 -> M-R/m-r F4 -> S-X/s-x F5 -> Y-Z/y-z,
	 * _, @, *, .
	 */
	private int[] letterStates;
	
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
	 * Keeps track of the mouse drag event is updating the orientation of a
	 * world object.
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
		// Transpose the rotation portion, because Processing
		this.applyMatrix(RMath.formTMat(origin, axesVectors));
	}
	
	/**
	 * Wrapper method for applying the coordinate frame defined by the given
	 * column major transformation matrix.
	 * 
	 * @param tMatrix	A 4x4 row major transformation matrix
	 */
	public void applyMatrix(RMatrix mat) {
		float[][] tMatrix = mat.getDataF();
		super.applyMatrix(
				tMatrix[0][0], tMatrix[0][1], tMatrix[0][2], tMatrix[0][3],
				tMatrix[1][0], tMatrix[1][1], tMatrix[1][2], tMatrix[1][3],
				tMatrix[2][0], tMatrix[2][1], tMatrix[2][2], tMatrix[2][3],
				tMatrix[3][0], tMatrix[3][1], tMatrix[3][2], tMatrix[3][3]
		);
	}
	
	/**
	 * @return Whether or not bounding boxes are displayed
	 */
	public boolean areOBBsRendered() {
		return !UI.getButtonState("ToggleOBBs");
	}

	/**
	 * Pendant DOWN button
	 * 
	 * Moves down one element in a list displayed on the pendant screen.
	 * Depending on what menu is active this may move the list pointer
	 * in either the content or options menu.
	 */
	public void arrow_dn() {
		switch (mode) {
		case NAV_PROGRAMS:
			contents.moveDown(isShift());

			Fields.debug("line=%d col=%d TRS=%d\n",	contents.getLineIdx(),
					contents.getColumnIdx(), contents.getRenderStart());
			break;
		case NAV_PROG_INSTR:
		case SELECT_COMMENT:
		case SELECT_CUT_COPY:
		case SELECT_INSTR_DELETE:
			if (!isProgExec()) {
				// Lock movement when a program is running
				Instruction i = getActiveInstruction();
				int prevIdx = getSelectedIdx();
				setActiveInstIdx(contents.moveDown(isShift()));
				int curLine = getSelectedLine();

				// special case for select statement column navigation
				if ((i instanceof SelectStatement || i instanceof MotionInstruction) && curLine == 1) {
					if (prevIdx >= 3) {
						contents.setColumnIdx(prevIdx - 3);
					} else {
						contents.setColumnIdx(0);
					}
				}

				Fields.debug("line=%d col=%d inst=%d TRS=%d\n",
						contents.getLineIdx(), contents.getColumnIdx(),
						getActiveInstIdx(),
						contents.getRenderStart());
			}
			break;
		case NAV_DREGS:
		case NAV_PREGS:
		case NAV_TOOL_FRAMES:
		case NAV_USER_FRAMES:
		case NAV_MACROS:
		case NAV_MF_MACROS:
			active_index = contents.moveDown(isShift());

			Fields.debug("line=%d col=%d adx=%d TRS=%d\n",
						contents.getLineIdx(), contents.getColumnIdx(),
						active_index, contents.getRenderStart());
			break;
		case NAV_MAIN_MENU:
		case NAV_DATA:
		case SET_MACRO_PROG:
		case NAV_IOREG:
			contents.moveDown(shift);
			break;
		case NAV_INSTR_MENU:
		case SELECT_FRAME_MODE:
		case SELECT_INSTR_INSERT:
		case SELECT_IO_INSTR_REG:
		case SELECT_FRAME_INSTR_TYPE:
		case SELECT_REG_STMT:
		case SELECT_COND_STMT:
		case SELECT_JMP_LBL:
		case SELECT_PASTE_OPT:
		case TFRAME_DETAIL:
		case UFRAME_DETAIL:
		case TEACH_3PT_USER:
		case TEACH_3PT_TOOL:
		case TEACH_4PT:
		case TEACH_6PT:
		case SWAP_PT_TYPE:
		case SET_MINST_TYPE:
		case SET_MINST_REG_TYPE:
		case SET_MINST_CREG_TYPE:
		case SET_MINST_OFF_TYPE:
		case SET_MINST_WO:
		case SET_MACRO_TYPE:
		case SET_MACRO_BINDING:
		case SET_FRM_INSTR_TYPE:
		case SET_REG_EXPR_TYPE:
		case SET_IF_STMT_ACT:
		case SET_SELECT_STMT_ACT:
		case SET_SELECT_STMT_ARG:
		case SET_EXPR_ARG:
		case SET_BOOL_EXPR_ARG:
		case SET_EXPR_OP:
		case SET_IO_INSTR_STATE:
		case SET_CALL_PROG:
		case SET_DEF_TOOLTIP:
			options.moveDown(false);
			break;
		case ACTIVE_FRAMES:
			updateActiveFramesDisplay();
			workingText = new StringBuilder(Integer.toString(activeRobot.getActiveUserIdx() + 1));

			contents.moveDown(false);
			break;
		default:
			if (mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
				options.moveDown(false);
				// Reset function key states
				for (int idx = 0; idx < letterStates.length; ++idx) {
					letterStates[idx] = 0;
				}

			} else if (mode.getType() == ScreenType.TYPE_POINT_ENTRY) {
				contents.moveDown(false);
			}
		}

		updatePendantScreen();
	}
	
	/**
	 * Pendant LEFT button
	 * 
	 * Moves one column to the left on a list element display on the pendant's
	 * screen. Depending on what menu is active, this may move the pointer in
	 * either the content or options menu.
	 */
	public void arrow_lt() {
		switch (mode) {
		case NAV_PROG_INSTR:
			if (!isProgExec()) {
				// Lock movement when a program is running
				contents.moveLeft();
			}
			break;
		case NAV_DREGS:
		case NAV_MACROS:
		case NAV_PREGS:
			contents.setColumnIdx(max(0, contents.getColumnIdx() - 1));
			break;
		case SELECT_IO_INSTR_REG:
			options.setColumnIdx(max(1, options.getColumnIdx() - 1));
			break;
		default:
			if (mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
				contents.setColumnIdx(max(0, contents.getColumnIdx() - 1));
				// Reset function key states
				for (int idx = 0; idx < letterStates.length; ++idx) {
					letterStates[idx] = 0;
				}

			} else if (mode.getType() == ScreenType.TYPE_POINT_ENTRY) {
				contents.setColumnIdx(Math.max(1, contents.getColumnIdx() - 1));

			} else if (mode.getType() == ScreenType.TYPE_EXPR_EDIT) {
				contents.setColumnIdx(
						contents.getColumnIdx() - ((contents.getColumnIdx() - 4 >= options.size()) ? 4 : 0));
			}
		}

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
	public void arrow_rt() {
		switch (mode) {
		case NAV_PROG_INSTR:
			if (!isProgExec()) {
				// Lock movement when a program is running
				contents.moveRight();
			}
			break;
		case NAV_DREGS:
		case NAV_MACROS:
		case NAV_PREGS:
			contents.setColumnIdx(min(contents.getColumnIdx() + 1, contents.getActiveLine().size() - 1));
			break;
		case SELECT_IO_INSTR_REG:
			options.setColumnIdx(min(options.getColumnIdx() + 1, options.get(options.getLineIdx()).size() - 1));
			break;
		default:
			if (mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {

				if (isShift()) {
					// Delete key function
					if (workingText.length() >= 1) {
						workingText.deleteCharAt(contents.getColumnIdx());
						contents.setColumnIdx(Math.max(0, Math.min(contents.getColumnIdx(), workingText.length() - 1)));
					}

				} else if (mode.getType() == ScreenType.TYPE_EXPR_EDIT) {
					contents.setColumnIdx(
							contents.getColumnIdx() + ((contents.getColumnIdx() + 4 < options.size()) ? 4 : 0));

				} else {
					// Add an insert element if the length of the current
					// comment is less than 16
					int len = workingText.length();
					int columnIdx = contents.getColumnIdx();
					
					if (len <= TEXT_ENTRY_LEN && columnIdx == len - 1 &&
							(len == 0 || workingText.charAt(len - 1) != '\0')) {

						workingText.append('\0');
					}

					contents.setColumnIdx(min(columnIdx + 1,  workingText.length() - 1));
					updatePendantScreen();
				}

				// Reset function key states
				for (int idx = 0; idx < letterStates.length; ++idx) {
					letterStates[idx] = 0;
				}

			} else if (mode.getType() == ScreenType.TYPE_POINT_ENTRY) {
				DisplayLine entry = contents.getActiveLine();
				int idx = contents.getColumnIdx();
				int size = entry.size();

				// Delete a digit from the beginning of the number entry
				if (isShift()) {
					if (size > 2) {
						entry.remove(idx);

					} else {
						// Leave at least one space value entry
						entry.set(idx, "\0");
					}

				} else {

					if (idx == (entry.size() - 1) && !entry.get(idx).equals("\0") && entry.size() < 10) {
						entry.add("\0");
					}

					// Move to the right one digit
					contents.setColumnIdx(Math.min(idx + 1, entry.size() - 1));
				}
			}
		}

		updatePendantScreen();
	}
	
	/**
	 * Pendant UP button
	 * 
	 * Moves up one element in a list displayed on the pendant screen.
	 * Depending on what menu is active this may move the list pointer
	 * in either the content or options menu.
	 */
	public void arrow_up() {
		switch (mode) {
		case NAV_PROGRAMS:
			contents.moveUp(isShift());

			Fields.debug("line=%d col=%d TRS=%d\n", contents.getLineIdx(),
					contents.getColumnIdx(), contents.getRenderStart());
			break;
		case NAV_PROG_INSTR:
		case SELECT_COMMENT:
		case SELECT_CUT_COPY:
		case SELECT_INSTR_DELETE:
			if (!isProgExec()) {
				try {
					// Lock movement when a program is running
					Instruction i = getActiveInstruction();
					int prevLine = getSelectedLine();
					setActiveInstIdx(contents.moveUp(isShift()));
					int curLine = getSelectedLine();

					// special case for select statement column navigation
					if ((i instanceof SelectStatement || i instanceof MotionInstruction) && curLine == 0) {
						if (prevLine == 1) {
							contents.setColumnIdx(contents.getColumnIdx() + 3);
						}
					}

				} catch (IndexOutOfBoundsException IOOBEx) {
					// Issue with loading a program, not sure if this helps ...
					IOOBEx.printStackTrace();
				}

				Fields.debug("line=%d col=%d inst=%d TRS=%d\n",
					contents.getLineIdx(), contents.getColumnIdx(),
					getActiveInstIdx(),
					contents.getRenderStart());
			}
			break;
		case NAV_DREGS:
		case NAV_PREGS:
		case NAV_TOOL_FRAMES:
		case NAV_USER_FRAMES:
		case NAV_MACROS:
		case NAV_MF_MACROS:
			active_index = contents.moveUp(isShift());

			Fields.debug("line=%d col=%d adx=%d TRS=%d\n",
					contents.getLineIdx(), contents.getColumnIdx(),
					active_index, contents.getRenderStart());
			break;
		case NAV_MAIN_MENU:
		case NAV_DATA:
		case SET_MACRO_PROG:
		case NAV_IOREG:
			contents.moveUp(isShift());
			break;
		case NAV_INSTR_MENU:
		case SELECT_FRAME_MODE:
		case SELECT_INSTR_INSERT:
		case SELECT_IO_INSTR_REG:
		case SELECT_FRAME_INSTR_TYPE:
		case SELECT_REG_STMT:
		case SELECT_COND_STMT:
		case SELECT_JMP_LBL:
		case SELECT_PASTE_OPT:
		case TFRAME_DETAIL:
		case UFRAME_DETAIL:
		case TEACH_3PT_USER:
		case TEACH_3PT_TOOL:
		case TEACH_4PT:
		case TEACH_6PT:
		case SWAP_PT_TYPE:
		case SET_MINST_TYPE:
		case SET_MINST_REG_TYPE:
		case SET_MINST_CREG_TYPE:
		case SET_MINST_OFF_TYPE:
		case SET_MINST_WO:
		case SET_MACRO_TYPE:
		case SET_MACRO_BINDING:
		case SET_FRM_INSTR_TYPE:
		case SET_REG_EXPR_TYPE:
		case SET_IF_STMT_ACT:
		case SET_SELECT_STMT_ACT:
		case SET_SELECT_STMT_ARG:
		case SET_EXPR_ARG:
		case SET_BOOL_EXPR_ARG:
		case SET_EXPR_OP:
		case SET_IO_INSTR_STATE:
		case SET_CALL_PROG:
		case SET_DEF_TOOLTIP:
			options.moveUp(false);
			break;
		case ACTIVE_FRAMES:
			updateActiveFramesDisplay();
			workingText = new StringBuilder(Integer.toString(activeRobot.getActiveToolIdx() + 1));
			contents.moveUp(false);
			break;
		default:
			if (mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
				options.moveUp(false);
				// Reset function key states
				for (int idx = 0; idx < letterStates.length; ++idx) {
					letterStates[idx] = 0;
				}

			} else if (mode.getType() == ScreenType.TYPE_POINT_ENTRY) {
				contents.moveUp(false);
			}
		}

		updatePendantScreen();
	}
	
	/**
	 * Camera Bk button
	 * 
	 * Sets the camera to the default back view, which looks down the positive
	 * x-axis of the world coordinate system.
	 */
	public void BackView() {
		// Back view
		camera.reset();
		camera.setRotation(0f, PI, 0f);
	}

	/**
	 * Pendant BKSPC button
	 * 
	 * Functions as a backspace key for number, text, and point input menus.
	 */
	public void bkspc() {
		if (mode.getType() == ScreenType.TYPE_NUM_ENTRY) {

			// Functions as a backspace key
			if (workingText.length() > 0) {
				workingText.deleteCharAt(workingText.length() - 1);
			}

		} else if (mode.getType() == ScreenType.TYPE_POINT_ENTRY) {
			DisplayLine entry = contents.getActiveLine();
			int idx = contents.getColumnIdx();

			if (entry.size() > 2) {

				if (idx > 1) {
					contents.setColumnIdx(--idx);
				}

				entry.remove(idx);

			} else {
				entry.set(idx, "\0");
			}

		} else if (mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {

			// Delete/Backspace function
			if (workingText.length() >= 1) {
				int colIdx = contents.getColumnIdx();

				if (colIdx < 1) {
					// Remove the beginning character
					workingText.deleteCharAt(0);

				} else if (colIdx < workingText.length()) {
					// Remove the character
					workingText.deleteCharAt(colIdx - 1);
				}

				contents.setColumnIdx(Math.max(0, Math.min(colIdx - 1, workingText.length() - 1)));
			}

			for (int idx = 0; idx < letterStates.length; ++idx) {
				letterStates[idx] = 0;
			}
		}

		updatePendantScreen();
	}
	
	/**
	 * Camera Bt button
	 * 
	 * Sets the camera to point down the positive z-axis of the world coordinate
	 * frame, so as to view the bottom of the robot.
	 */
	public void BottomView() {
		// Bottom view
		camera.reset();
		camera.setRotation(HALF_PI, 0f, 0f);
	}
	
	/**
	 * Pendant BWD button
	 * 
	 * Executes a motion instruction two instructions prior to the active
	 * instruction (if one exists).
	 */
	public void bwd() {
		// Backwards is only functional when executing a program one instruction
		// at a time
		if (mode == ScreenMode.NAV_PROG_INSTR && isShift() && isStep()) {
			// Safeguard against editing a program while it is running
			contents.setColumnIdx(0);
			progExecBwd();
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
	 * Removes all saved program states from the program execution call stack.
	 */
	public void clearCallStack() {
		progCallStack.clear();
	}
	
	/**
	 * Clear button shared between the Create and Edit windows
	 * 
	 * Clears all input fields (textfields, dropdownlist, etc.) in the world
	 * object creation and edit windows.
	 */
	public void ClearFields() {
		UI.clearAllInputFields();
	}

	/**
	 * Pendant COORD button
	 * 
	 * If shift is off, then this button will change the coordinate frame of
	 * the active robot. If shift is on, then this button will change to the
	 * active frames menu on the pendant.
	 */
	public void coord() {
		if (isShift()) {
			nextScreen(ScreenMode.ACTIVE_FRAMES);

		} else {
			// Update the coordinate frame
			coordFrameTransition();
			updatePendantScreen();
		}
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
		hold();

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
	
	/**
	 * Pendant - button
	 * 
	 * Appends the '-' character to input for number, text, and point entry
	 * menus.
	 */
	public void dash() {
		characterInput('-');
	}
	
	/**
	 * Pendant DATA button
	 * 
	 * Displays a list of the register navigation menus (i.e. data or position
	 * registers).
	 */
	public void data() {
		nextScreen(ScreenMode.NAV_DATA);
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
			
			if (teachFrame != null && mode.getType() == ScreenType.TYPE_TEACH_POINTS) {
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
	 * Pendant EDIT button
	 * 
	 * Links to the active program's instruction navigation menu, except in the
	 * select screen, where it will open the instruction navigation menu for
	 * the selected program
	 */
	public void edit() {
		if (mode == ScreenMode.NAV_PROGRAMS) {
			// Load the selected program
			setActiveProgIdx( contents.getActiveIndex() );
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

	public void editExpression(Expression expr, int selectIdx) {
		int[] elements = expr.mapToEdit();
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
		}
	}

	public void editTextEntry(int fIdx) {
		char newChar = LETTERS[fIdx][letterStates[fIdx]];
		if (options.getLineIdx() == 0 && !(fIdx == 4 && letterStates[fIdx] > 1)) {
			// Use uppercase character
			newChar = (char) (newChar - 32);
		}

		workingText.setCharAt(contents.getColumnIdx(), newChar);

		// Update current letter state
		letterStates[fIdx] = (letterStates[fIdx] + 1) % 6;
		for (int idx = 0; idx < letterStates.length; idx += 1) {
			// Reset all other letter states
			if (idx != fIdx) {
				letterStates[idx] = 0;
			}
		}
	}
	
	public RoboticArm getActiveRobot() {
		return activeRobot;
	}
	
	/**
	 * Pendant ENTER button
	 * 
	 * Functions as a confirmation button for almost all menus.
	 */
	public void enter() {
		RoboticArm r = activeRobot;
		Program p = getActiveProg();

		switch (mode) {
		// Main menu
		case NAV_MAIN_MENU:
			if (contents.getLineIdx() == 0) { // Frames
				nextScreen(ScreenMode.SELECT_FRAME_MODE);

			} else if (contents.getLineIdx() == 1) { // Macros
				nextScreen(ScreenMode.NAV_MACROS);

			} else if (contents.getLineIdx() == 2) { // Manual Functions
				nextScreen(ScreenMode.NAV_MF_MACROS);

			} else if (contents.getLineIdx() == 3) {
				nextScreen(ScreenMode.NAV_IOREG);
			}

			break;
		case NAV_IOREG:
			int ioIdx = contents.getActiveIndex();
			IORegister ioReg = r.getIOReg(ioIdx);
			
			if (ioReg != null) {
				// Toggle the state of the I/O register
				ioReg.state = (ioReg.state == Fields.ON) ? Fields.OFF : Fields.ON;
				updatePendantScreen();
			}
			
			break;
		case SELECT_FRAME_MODE:
			if (options.getLineIdx() == 0) {
				nextScreen(ScreenMode.NAV_TOOL_FRAMES);
			} else if (options.getLineIdx() == 1) {
				nextScreen(ScreenMode.NAV_USER_FRAMES);
			}
			break;
		case ACTIVE_FRAMES:
			updateActiveFramesDisplay();
			break;
		case NAV_TOOL_FRAMES:
			curFrameIdx = contents.getActiveIndex();
			nextScreen(ScreenMode.TFRAME_DETAIL);
			break;
		case NAV_USER_FRAMES:
			curFrameIdx = contents.getActiveIndex();
			nextScreen(ScreenMode.UFRAME_DETAIL);
			break;
		case UFRAME_DETAIL:
			// User Frame teaching methods
			teachFrame = activeRobot.getUserFrame(curFrameIdx);
			if (options.getLineIdx() == 0) {
				nextScreen(ScreenMode.TEACH_3PT_USER);
			} else if (options.getLineIdx() == 1) {
				nextScreen(ScreenMode.TEACH_4PT);
			} else if (options.getLineIdx() == 2) {
				nextScreen(ScreenMode.DIRECT_ENTRY_USER);
			}
			break;
		case TFRAME_DETAIL:
			teachFrame = activeRobot.getToolFrame(curFrameIdx);
			// Tool Frame teaching methods
			if (options.getLineIdx() == 0) {
				nextScreen(ScreenMode.TEACH_3PT_TOOL);
			} else if (options.getLineIdx() == 1) {
				nextScreen(ScreenMode.TEACH_6PT);
			} else if (options.getLineIdx() == 2) {
				nextScreen(ScreenMode.DIRECT_ENTRY_TOOL);
			}
			break;
		case SET_DEF_TOOLTIP:
			activeRobot.setDefToolTip(curFrameIdx, options.getLineIdx());
			activeRobot.setActiveToolFrame(curFrameIdx);
			DataManagement.saveRobotData(activeRobot, 2);
			lastScreen();
			break;
		case TEACH_3PT_TOOL:
		case TEACH_3PT_USER:
			createFrame(teachFrame, 0);
			lastScreen();
			break;
		case TEACH_4PT:
		case TEACH_6PT:
			createFrame(teachFrame, 1);
			lastScreen();
			break;
		case DIRECT_ENTRY_TOOL:
		case DIRECT_ENTRY_USER:
			// User defined x, y, z, w, p, and r values
			float[] inputs = new float[] { 0f, 0f, 0f, 0f, 0f, 0f };

			try {
				// Parse each input value
				for (int val = 0; val < inputs.length; ++val) {
					DisplayLine value = contents.get(val);
					String str = new String();
					int sdx;

					/*
					 * Combine all columns related to the value, ignoring the
					 * prefix and last columns
					 */
					for (sdx = 1; sdx < (value.size() - 1); ++sdx) {
						str += value.get(sdx);
					}

					// Ignore any trailing blank spaces
					if (!value.get(sdx).equals("\0")) {
						str += value.get(sdx);
					}

					if (str.length() < 0) {
						// No value entered
						updatePendantScreen();
						println("All entries must have a value!");
						return;
					}

					// Remove prefix
					inputs[val] = Float.parseFloat(str);
					// Bring within range of values
					inputs[val] = max(-9999f, min(inputs[val], 9999f));
				}

				createFrameDirectEntry(teachFrame, inputs);
				
			} catch (NumberFormatException NFEx) {
				// Invalid number
				println("Entries must be real numbers!");
				return;
			}

			if (teachFrame instanceof UserFrame) {
				nextScreen(ScreenMode.UFRAME_DETAIL);
				
			} else {
				nextScreen(ScreenMode.TFRAME_DETAIL);
			}
			break;

			// Program nav and edit
		case PROG_CREATE:
			if (workingText.length() > 0 && !workingText.equals("\0")) {
				if (workingText.charAt(workingText.length() - 1) == '\0') {
					// Remove insert character
					workingText.deleteCharAt(workingText.length() - 1);
				}

				int new_prog = activeRobot.addProgram(new Program(workingText.toString(), activeRobot));
				setActiveProgIdx(new_prog);
				setActiveInstIdx(0);

				DataManagement.saveRobotData(activeRobot, 1);
				switchScreen(ScreenMode.NAV_PROG_INSTR);
			}
			break;
		case PROG_RENAME:
			if (workingText.length() > 0 && !workingText.equals("\0")) {
				if (workingText.charAt(workingText.length() - 1) == '\0') {
					// Remove insert character
					workingText.deleteCharAt(workingText.length() - 1);
				}
				// Rename the active program
				Program prog = getActiveProg();

				if (prog != null) {
					prog.setName(workingText.toString());
					activeRobot.reorderPrograms();
					DataManagement.saveRobotData(activeRobot, 1);
				}

				lastScreen();
			}
			break;
		case PROG_COPY:
			if (workingText.length() > 0 && !workingText.equals("\0")) {
				if (workingText.charAt(workingText.length() - 1) == '\0') {
					// Remove insert character
					workingText.deleteCharAt(workingText.length() - 1);
				}

				Program prog = getActiveProg();

				if (prog != null) {
					Program newProg = prog.clone();
					newProg.setName(workingText.toString());
					int new_prog = activeRobot.addProgram(newProg);
					setActiveProgIdx(new_prog);
					setActiveInstIdx(0);
					DataManagement.saveRobotData(activeRobot, 1);
				}

				lastScreen();
			}
			break;
		case TFRAME_RENAME:
			// Update frame name
			if (workingText.length() > 0 && !workingText.equals("\0")) {
				if (workingText.charAt(workingText.length() - 1) == '\0') {
					// Remove insert character
					workingText.deleteCharAt(workingText.length() - 1);
				}
			}
			
			ToolFrame tFrame = activeRobot.getToolFrame(curFrameIdx);
			tFrame.setName(workingText.toString());
			
			DataManagement.saveRobotData(activeRobot, 1);
			lastScreen();
			break;
		case UFRAME_RENAME:
			// Update frame name
			if (workingText.length() > 0 && !workingText.equals("\0")) {
				if (workingText.charAt(workingText.length() - 1) == '\0') {
					// Remove insert character
					workingText.deleteCharAt(workingText.length() - 1);
				}
			}
			
			UserFrame uFrame = activeRobot.getUserFrame(curFrameIdx);
			uFrame.setName(workingText.toString());
			
			DataManagement.saveRobotData(activeRobot, 1);
			lastScreen();
			break;
		case NAV_PROGRAMS:
			r = activeRobot;
			if (r.numOfPrograms() != 0) {
				setActiveProgIdx( contents.getActiveIndex() );
				nextScreen(ScreenMode.NAV_PROG_INSTR);
			}
			break;

			// Instruction options menu
		case NAV_INSTR_MENU:

			switch (options.getLineIdx()) {
			case 0: // Undo
				activeRobot.popInstructionUndo(getActiveProg());
				lastScreen();
				break;
			case 1: // Insert
				nextScreen(ScreenMode.CONFIRM_INSERT);
				break;
			case 2: // Delete
				contents.resetSelection(p.getNumOfInst());
				nextScreen(ScreenMode.SELECT_INSTR_DELETE);
				break;
			case 3: // Cut/Copy
				contents.resetSelection(p.getNumOfInst());
				nextScreen(ScreenMode.SELECT_CUT_COPY);
				break;
			case 4: // Paste
				nextScreen(ScreenMode.SELECT_PASTE_OPT);
				break;
			case 5: // Find/Replace
				nextScreen(ScreenMode.FIND_REPL);
				break;
			case 6: // Renumber
				nextScreen(ScreenMode.CONFIRM_RENUM);
				break;
			case 7: // Comment
				contents.resetSelection(p.getNumOfInst());
				nextScreen(ScreenMode.SELECT_COMMENT);
				break;
			case 8: // Remark
			}

			break;

			// Instruction insert menus
		case SELECT_INSTR_INSERT:
			switch (options.getLineIdx()) {
			case 0: // I/O
				nextScreen(ScreenMode.SELECT_IO_INSTR_REG);
				break;
			case 1: // Offset/Frames
				nextScreen(ScreenMode.SELECT_FRAME_INSTR_TYPE);
				break;
			case 2: // Register
				nextScreen(ScreenMode.SELECT_REG_STMT);
				break;
			case 3: // IF/ SELECT
				nextScreen(ScreenMode.SELECT_COND_STMT);
				break;
			case 4: // JMP/ LBL
				nextScreen(ScreenMode.SELECT_JMP_LBL);
				break;
			case 5: // Call
				newCallInstruction();
				editIdx = activeRobot.RID;
				switchScreen(ScreenMode.SET_CALL_PROG);
				break;
			case 6: // RobotCall
				newRobotCallInstruction();
				RoboticArm inactive = getInactiveRobot();
				
				if (inactive.numOfPrograms() > 0) {
					editIdx = getInactiveRobot().RID;
					switchScreen(ScreenMode.SET_CALL_PROG);
					
				} else {
					// No programs exist in the inactive robot
					lastScreen();
				}
			}

			break;
		case SELECT_IO_INSTR_REG:
			// IO registers are 1 indexed!
			int state = (options.getColumnIdx() == 1) ? Fields.ON : Fields.OFF;
			newIOInstruction(options.getLineIdx() + 1, state);
			screenStates.pop();
			lastScreen();
			break;
		case SELECT_FRAME_INSTR_TYPE:
			if (options.getLineIdx() == 0) {
				newFrameInstruction(Fields.FTYPE_TOOL);
			} else {
				newFrameInstruction(Fields.FTYPE_USER);
			}

			screenStates.pop();
			switchScreen(ScreenMode.SET_FRAME_INSTR_IDX);
			break;
		case SELECT_REG_STMT:
			screenStates.pop();
			screenStates.pop();

			if (options.getLineIdx() == 0) {
				newRegisterStatement(new DataRegister());
			} else if (options.getLineIdx() == 1) {
				newRegisterStatement(new IORegister());
			} else if (options.getLineIdx() == 2) {
				newRegisterStatement(new PositionRegister());
			} else {
				newRegisterStatement(new PositionRegister(), 0);
				ScreenState instEdit = screenStates.peek();
				pushScreen(ScreenMode.SET_REG_EXPR_IDX2, instEdit.conLnIdx,
						instEdit.conColIdx, instEdit.conRenIdx, 0,
						0);
			}

			loadScreen(ScreenMode.SET_REG_EXPR_IDX1);
			break;
		case SELECT_COND_STMT:
			if (options.getLineIdx() == 0) {
				newIfStatement();
				screenStates.pop();
				switchScreen(ScreenMode.SET_EXPR_OP);
			} else if (options.getLineIdx() == 1) {
				newIfExpression();
				screenStates.pop();
				lastScreen();
			} else {
				newSelectStatement();
				screenStates.pop();
				lastScreen();
			}

			break;
		case SELECT_JMP_LBL:
			screenStates.pop();

			if (options.getLineIdx() == 0) {
				newLabel();
				switchScreen(ScreenMode.SET_LBL_NUM);
			} else {
				newJumpInstruction();
				switchScreen(ScreenMode.SET_JUMP_TGT);
			}

			break;

			// Movement instruction edit
		case SET_MINST_TYPE:
			MotionInstruction m = (MotionInstruction) r.getInstToEdit(getActiveProg(), getActiveInstIdx());
			
			if (options.getLineIdx() == 0) {
				m.setMotionType(Fields.MTYPE_JOINT);
				
			} else if (options.getLineIdx() == 1) {
				m.setMotionType(Fields.MTYPE_LINEAR);
				
			} else if (options.getLineIdx() == 2) {
				m.setMotionType(Fields.MTYPE_CIRCULAR);
				
			}
			
			lastScreen();
			break;
		case SET_MINST_REG_TYPE:
			MotionInstruction mInst = (MotionInstruction) r.getInstToEdit(getActiveProg(), getActiveInstIdx());
			
			if (options.getLineIdx() == 2) {
				if (!(mInst instanceof CamMoveToObject)) {
					// Change to a camera motion instruction
					MotionInstruction mCInst = new CamMoveToObject(
							mInst.getMotionType(), 0,
							mInst.getSpdMod(), mInst.getTermination(),
							getActiveScenario()
					);
					
					getActiveProg().replaceInstAt(getActiveInstIdx(), mCInst);
				}
				
			} else {
				int posType;
				
				if (options.getLineIdx() == 1) {
					posType = Fields.PTYPE_PREG;
					
				} else {
					posType = Fields.PTYPE_PROG;
				}
				
				if (mInst instanceof CamMoveToObject) {
					// Change to a position motion instruction
					MotionInstruction mPInst = new PosMotionInst(
							mInst.getMotionType(), posType, -1,
							mInst.getSpdMod(), mInst.getTermination()
					);
					
					getActiveProg().replaceInstAt(getActiveInstIdx(), mPInst);
					
				} else if (mInst instanceof PosMotionInst) {
					// Update motion type of the position motion instruction
					mInst.setPosType(posType);
				}
			}
			
			lastScreen();
			break;
		case SET_MINST_CREG_TYPE:
			PosMotionInst pMInst = (PosMotionInst) r.getInstToEdit(getActiveProg(), getActiveInstIdx());
			
			if (options.getLineIdx() == 0) {
				pMInst.setCircPosType(Fields.PTYPE_PROG);
			
			} else if (options.getLineIdx() == 1) {
				pMInst.setCircPosType(Fields.PTYPE_PREG);
			}

			lastScreen();
			break;
		case SET_MINST_SPD:
			m = (MotionInstruction) r.getInstToEdit(getActiveProg(), getActiveInstIdx());
			int motionType = m.getMotionType();
			
			try {
				float tempSpeed = Float.parseFloat(workingText.toString());
				
				if (motionType == Fields.MTYPE_LINEAR ||
						motionType == Fields.MTYPE_CIRCULAR) {
					
					tempSpeed /= RoboticArm.motorSpeed;
					
				} else {
					tempSpeed /= 100f;
				}
				
				m.setSpdMod(RMath.clamp(tempSpeed, 0.01f, 1f));
				
			} catch (NumberFormatException NFEx) {
				// Invalid input
			}
			
			lastScreen();
			break;
		case SET_MINST_IDX:
			mInst = (MotionInstruction) r.getInstToEdit(getActiveProg(), getActiveInstIdx());
			
			try {
				int tempRegister = Integer.parseInt(workingText.toString());
				int lbound = 1, ubound = 0;
				
				if (mInst.getPosType() == Fields.PTYPE_PREG) {
					ubound = 100;

				} else if (mInst.getPosType() == Fields.PTYPE_PROG) {
					ubound = 1000;
					
				} else if (mInst instanceof CamMoveToObject) {
					Scenario s = ((CamMoveToObject) mInst).getScene();
					
					if (s != null) {
						ubound = s.size();
						
					}	
				}

				if (tempRegister < lbound || tempRegister > ubound) {
					// Invalid register index
					String err = String.format("Only registers %d-%d are valid!", lbound, ubound);
					System.err.println(err);
				}
				
				mInst.setPosIdx(tempRegister - 1);
				
			} catch (NumberFormatException NFEx) {
			/* Ignore invalid numbers */ }

			lastScreen();
			break;
		case SET_MINST_CIDX:
			try {
				pMInst = (PosMotionInst) r.getInstToEdit(getActiveProg(), getActiveInstIdx());
				int tempRegister = Integer.parseInt(workingText.toString());
				int lbound = 1, ubound;
				
				if (pMInst.getPosType() == Fields.PTYPE_PREG) {
					ubound = 100;

				} else if (pMInst.getPosType() == Fields.PTYPE_PROG) {
					ubound = 1000;
					
				} else {
					ubound = 0;
				}

				if (tempRegister < lbound || tempRegister > ubound) {
					// Invalid register index
					String err = String.format("Only registers %d-%d are valid!", lbound, ubound);
					System.err.println(err);
					lastScreen();
					return;
				}
				
				pMInst.setCircPosIdx(tempRegister - 1);
				
			} catch (NumberFormatException NFEx) {
				String err = "Invalid entry!";
				System.err.println(err);
			}
			
			lastScreen();
			break;
		case SET_MINST_WO:
			CamMoveToObject cMInst = (CamMoveToObject) r.getInstToEdit(getActiveProg(), getActiveInstIdx());
			cMInst.setPosIdx(options.getLineIdx() - 1);
			
			lastScreen();
			break;
		case SET_MINST_TERM:
			try {
				m = (MotionInstruction) r.getInstToEdit(getActiveProg(), getActiveInstIdx());
				int tempTerm = Integer.parseInt(workingText.toString());

				if (tempTerm >= 0 && tempTerm <= 100) {
					m.setTermination(tempTerm);
				}
			} catch (NumberFormatException NFEx) {
			/* Ignore invalid input */ }

			lastScreen();
			break;
		case SET_MINST_OFF_TYPE:
			pMInst = (PosMotionInst) r.getInstToEdit(getActiveProg(), getActiveInstIdx());
			int ldx = options.getLineIdx();
			// Set the offset type of the active motion instruction
			if (ldx == 0) {
				pMInst.setOffsetType(Fields.OFFSET_NONE);
				
			} else if (ldx == 1) {
				pMInst.setOffsetType(Fields.OFFSET_PREG);
			}
			
			lastScreen();
			break;
		case SET_MINST_OFFIDX:
			try {
				pMInst = (PosMotionInst) r.getInstToEdit(getActiveProg(), getActiveInstIdx());
				int tempRegister = Integer.parseInt(workingText.toString()) - 1;
				
				if (tempRegister < 0 || tempRegister > 99) {
					// Invalid register index
					String err = "Only registers 1 - 1000 are legal!";
					System.out.println(err);
					
				} else {
					pMInst.setOffsetType(Fields.OFFSET_PREG);
					pMInst.setOffsetIdx(tempRegister);
				}
				
			} catch (NumberFormatException NFEx) {/* Ignore invalid numbers */ }

			lastScreen();
			break;

		// Expression edit
		case SET_EXPR_ARG:
			Expression expr = (Expression)opEdit;
			Operand<?> operand;
			
			if (options.getLineIdx() == 0) {
				// set arg to new data reg
				operand = new OperandDReg(new DataRegister());
				opEdit = expr.setOperand(editIdx, operand);
				switchScreen(ScreenMode.INPUT_DREG_IDX);
			} else if (options.getLineIdx() == 1) {
				// set arg to new io reg
				operand = new OperandIOReg(new IORegister());
				opEdit = expr.setOperand(editIdx, operand);
				switchScreen(ScreenMode.INPUT_IOREG_IDX);
			} else if (options.getLineIdx() == 2) {
				operand = new OperandPReg(new PositionRegister());
				opEdit = expr.setOperand(editIdx, operand);
				switchScreen(ScreenMode.INPUT_PREG_IDX1);
			} else if (options.getLineIdx() == 3) {
				operand = new OperandPRegIdx(new PositionRegister(), 0);
				opEdit = expr.setOperand(editIdx, operand);
				screenStates.pop();
				pushScreen(ScreenMode.INPUT_PREG_IDX2, contents.getLineIdx(),
						contents.getColumnIdx(), contents.getRenderStart(), 0,
						0);
				loadScreen(ScreenMode.INPUT_PREG_IDX1);
			} else if (options.getLineIdx() == 4) {
				// set arg to new expression
				operand = new Expression();
				opEdit = expr.setOperand(editIdx, operand);
				lastScreen();
			} else {
				// set arg to new constant
				operand = new OperandFloat();
				opEdit = expr.setOperand(editIdx, operand);
				switchScreen(ScreenMode.INPUT_CONST);
			}

			break;
		case SET_BOOL_EXPR_ARG:
			IfStatement stmt = (IfStatement) r.getInstToEdit(getActiveProg(), getActiveInstIdx());
			if (options.getLineIdx() == 0) {
				// set arg to new data reg
				opEdit = new OperandDReg();
				stmt.setOperand(editIdx, opEdit);
				switchScreen(ScreenMode.INPUT_DREG_IDX);
			} else if (options.getLineIdx() == 1) {
				// set arg to new io reg
				opEdit = new OperandIOReg();
				stmt.setOperand(editIdx, opEdit);
				switchScreen(ScreenMode.INPUT_IOREG_IDX);
			} else {
				// set arg to new constant
				opEdit = new OperandFloat();
				stmt.setOperand(editIdx, opEdit);
				switchScreen(ScreenMode.INPUT_CONST);
			}
			break;
		case SET_IF_STMT_ACT:
			stmt = (IfStatement) r.getInstToEdit(getActiveProg(), getActiveInstIdx());
			if (options.getLineIdx() == 0) {
				stmt.setInstr(new JumpInstruction());
				switchScreen(ScreenMode.SET_JUMP_TGT);
			} else if (options.getLineIdx() == 1) {
				stmt.setInstr(new CallInstruction(activeRobot));
				editIdx = activeRobot.RID;
				switchScreen(ScreenMode.SET_CALL_PROG);
			} else {
				RoboticArm inactive = getInactiveRobot();
				stmt.setInstr(new CallInstruction(inactive));
				
				if (inactive.numOfPrograms() > 0) {
					switchScreen(ScreenMode.SET_CALL_PROG);
					
				} else {
					// No programs from which to choose
					lastScreen();
				}
			}

			break;
		case SET_EXPR_OP:
			if (opEdit instanceof Expression) {
				expr = (Expression) opEdit;
				r.getInstToEdit(getActiveProg(), getActiveInstIdx());
				
				switch (options.getLineIdx()) {
				case 0:
					expr.setOperator(editIdx, Operator.ADD);
					break;
				case 1:
					expr.setOperator(editIdx, Operator.SUB);
					break;
				case 2:
					expr.setOperator(editIdx, Operator.MULT);
					break;
				case 3:
					expr.setOperator(editIdx, Operator.DIV);
					break;
				case 4:
					expr.setOperator(editIdx, Operator.IDIV);
					break;
				case 5:
					expr.setOperator(editIdx, Operator.MOD);
					break;
				case 6:
					expr.setOperator(editIdx, Operator.EQUAL);
					break;
				case 7:
					expr.setOperator(editIdx, Operator.NEQUAL);
					break;
				case 8:
					expr.setOperator(editIdx, Operator.GRTR);
					break;
				case 9:
					expr.setOperator(editIdx, Operator.LESS);
					break;
				case 10:
					expr.setOperator(editIdx, Operator.GREQ);
					break;
				case 11:
					expr.setOperator(editIdx, Operator.LSEQ);
					break;
				case 12:
					expr.setOperator(editIdx, Operator.AND);
					break;
				case 13:
					expr.setOperator(editIdx, Operator.OR);
					break;
				case 14:
					expr.setOperator(editIdx, Operator.NOT);
					break;
				}
			} else if (opEdit instanceof AtomicExpression) {
				AtomicExpression atmExpr = (AtomicExpression) opEdit;
				r.getInstToEdit(getActiveProg(), getActiveInstIdx());

				switch (options.getLineIdx()) {
				case 0:
					atmExpr.setOperator(Operator.EQUAL);
					break;
				case 1:
					atmExpr.setOperator(Operator.NEQUAL);
					break;
				case 2:
					atmExpr.setOperator(Operator.GRTR);
					break;
				case 3:
					atmExpr.setOperator(Operator.LESS);
					break;
				case 4:
					atmExpr.setOperator(Operator.GREQ);
					break;
				case 5:
					atmExpr.setOperator(Operator.LSEQ);
					break;
				}
			}

			lastScreen();
			break;
		case INPUT_DREG_IDX:
		case INPUT_IOREG_IDX:
		case INPUT_PREG_IDX1:
		case INPUT_PREG_IDX2:
			try {
				int idx = Integer.parseInt(workingText.toString());

				if (mode == ScreenMode.INPUT_DREG_IDX) {

					if (idx < 1 || idx > 100) {
						System.err.println("Invalid index!");

					} else {
						((OperandDReg)opEdit).setValue(activeRobot.getDReg(idx - 1));
					}

				} else if (mode == ScreenMode.INPUT_PREG_IDX1) {

					if (idx < 1 || idx > 100) {
						System.err.println("Invalid index!");

					} else if(opEdit instanceof OperandPReg) {
						((OperandPReg)opEdit).setValue(activeRobot.getPReg(idx - 1));
					} else if(opEdit instanceof OperandPRegIdx) {
						((OperandPRegIdx)opEdit).setValue(activeRobot.getPReg(idx - 1));
					}
					
				} else if (mode == ScreenMode.INPUT_PREG_IDX2) {

					if (idx < 1 || idx > 6) {
						System.err.println("Invalid index!");

					} else {
						((OperandPRegIdx)opEdit).setSubIdx(idx - 1);
					}

				} else if (mode == ScreenMode.INPUT_IOREG_IDX) {

					if (idx < 1 || idx > activeRobot.numOfEndEffectors()) {
						System.err.println("Invalid index!");

					} else {
						((OperandIOReg)opEdit).setValue(activeRobot.getIOReg(idx));
					}
				}
			} catch (NumberFormatException e) {
				//TODO display error to user
			}

			lastScreen();
			break;
		case INPUT_CONST:
			try {
				float data = Float.parseFloat(workingText.toString());
				/*Instruction i = r.getInstToEdit(getActiveProg(), getActiveInstIdx());
				if(i instanceof RegisterStatement) {
					((RegisterStatement)i).getExpr().setOperand(editIdx, new OperandFloat(data));
				}*/
				
				((OperandFloat)opEdit).setValue(data);
				
			} catch (NumberFormatException e) {
				e.printStackTrace(); //TODO report error to user
			}

			lastScreen();
			break;
		case SET_BOOL_CONST:
			if (options.getLineIdx() == 0) {
				((OperandBool)opEdit).setValue(true);
			} else {
				((OperandBool)opEdit).setValue(false);
			}

			lastScreen();
			break;

			// Select statement edit
		case SET_SELECT_STMT_ACT:
			SelectStatement s = (SelectStatement) r.getInstToEdit(getActiveProg(), getActiveInstIdx());
			int i = (getSelectedIdx() - 3) / 3;

			if (options.getLineIdx() == 0) {
				s.getInstrs().set(i, new JumpInstruction());
			} else if (options.getLineIdx() == 1) {
				s.getInstrs().set(i, new CallInstruction(activeRobot));
			} else {
				s.getInstrs().set(i, new CallInstruction(getInactiveRobot()));
			}

			lastScreen();
			break;
		case SET_SELECT_STMT_ARG:
			s = (SelectStatement) r.getInstToEdit(getActiveProg(), getActiveInstIdx());
			
			if (options.getLineIdx() == 0) {
				opEdit = new OperandDReg(new DataRegister());
			} else {
				opEdit = new OperandGeneric();
			}
			
			s.setOperand(editIdx, opEdit);
			nextScreen(ScreenMode.SET_SELECT_ARGVAL);
			break;
		case SET_SELECT_ARGVAL:
			try {
				s = (SelectStatement) r.getInstToEdit(getActiveProg(), getActiveInstIdx());
				float f = Float.parseFloat(workingText.toString());

				if (opEdit.getType() == Operand.UNINIT) {
					opEdit = new OperandFloat(f);
					s.setOperand(editIdx, opEdit);
					
				} else if (opEdit.getType() == Operand.DREG) {
					if (f >= 1f && f <= 100f) {
						opEdit = new OperandDReg(activeRobot.getDReg((int) f - 1));
						s.setOperand(editIdx, opEdit);
					}
				}	
				
			} catch (NumberFormatException NFex) {
				//TODO display error to user
			}

			screenStates.pop();
			lastScreen();
			break;

			// IO instruction edit
		case SET_IO_INSTR_STATE:
			IOInstruction ioInst = (IOInstruction) r.getInstToEdit(getActiveProg(), getActiveInstIdx());

			if (options.getLineIdx() == 0) {
				ioInst.setState(Fields.ON);
			} else {
				ioInst.setState(Fields.OFF);
			}

			lastScreen();
			break;
		case SET_IO_INSTR_IDX:
			try {
				int tempReg = Integer.parseInt(workingText.toString());

				if (tempReg < 1 || tempReg >= activeRobot.numOfEndEffectors()) {
					System.err.println("Invalid index!");

				} else {
					ioInst = (IOInstruction) r.getInstToEdit(getActiveProg(), getActiveInstIdx());
					ioInst.setReg(tempReg);
				}
			} catch (NumberFormatException NFEx) {
			/* Ignore invalid input */ }

			lastScreen();
			break;

			// Frame instruction edit
		case SET_FRM_INSTR_TYPE:
			FrameInstruction fInst = (FrameInstruction) r.getInstToEdit(getActiveProg(), getActiveInstIdx());

			if (options.getLineIdx() == 0)
				fInst.setFrameType(Fields.FTYPE_TOOL);
			else
				fInst.setFrameType(Fields.FTYPE_USER);

			lastScreen();
			break;
		case SET_FRAME_INSTR_IDX:
			try {
				int frameIdx = Integer.parseInt(workingText.toString()) - 1;

				if (frameIdx >= -1 && frameIdx < Fields.FRAME_NUM) {
					fInst = (FrameInstruction) r.getInstToEdit(getActiveProg(), getActiveInstIdx());
					fInst.setReg(frameIdx);
				}
			} catch (NumberFormatException NFEx) {
			/* Ignore invalid input */ }

			lastScreen();
			break;

			// Register statement edit
		case SET_REG_EXPR_TYPE:
			RegisterStatement regStmt = (RegisterStatement) r.getInstToEdit(getActiveProg(), getActiveInstIdx());
			
			if (options.getLineIdx() == 3) {
				regStmt.setRegister(new PositionRegister(), 0);
				
				screenStates.pop();
				pushScreen(ScreenMode.SET_REG_EXPR_IDX2, contents.getLineIdx(),
						contents.getColumnIdx(), contents.getRenderStart(), 0,
						0);
				loadScreen(ScreenMode.SET_REG_EXPR_IDX1);
				
			} else {
				if (options.getLineIdx() == 0) {
					regStmt.setRegister(new DataRegister());
				} else if (options.getLineIdx() == 1) {
					regStmt.setRegister(new IORegister());
				} else if (options.getLineIdx() == 2) {
					regStmt.setRegister(new PositionRegister());
				}
				
				switchScreen(ScreenMode.SET_REG_EXPR_IDX1);
			}
			
			break;
		case SET_REG_EXPR_IDX1:
			try {
				int idx = Integer.parseInt(workingText.toString());
				regStmt = (RegisterStatement) r.getInstToEdit(getActiveProg(), getActiveInstIdx());
				Register reg = regStmt.getReg();

				if (idx < 1 || ((reg instanceof DataRegister || reg instanceof PositionRegister) && idx > 100)
						|| (reg instanceof IORegister && idx > 5)) {
					// Index is out of bounds
					println("Invalid register index!");

				} else {

					if (regStmt.getReg() instanceof DataRegister) {
						regStmt.setRegister(activeRobot.getDReg(idx - 1));

					} else if (regStmt.getReg() instanceof IORegister) {
						regStmt.setRegister(activeRobot.getIOReg(idx - 1));

					} else if (regStmt.getReg() instanceof PositionRegister) {
						if (regStmt.getPosIdx() < 0) {
							// Update a position register operand
							regStmt.setRegister(activeRobot.getPReg(idx - 1));

						} else {
							// Update a position register index operand
							regStmt.setRegister(activeRobot.getPReg(idx - 1), regStmt.getPosIdx());
						}

					}
				}
			} catch (NumberFormatException NFEx) {
			/* Ignore invalid input */ }

			lastScreen();
			break;
		case SET_REG_EXPR_IDX2:
			try {
				int idx = Integer.parseInt(workingText.toString());

				if (idx < 1 || idx > 6) {
					println("Invalid position index!");
				} else {
					regStmt = (RegisterStatement) r.getInstToEdit(getActiveProg(), getActiveInstIdx());
					if (regStmt.getReg() instanceof PositionRegister) {
						regStmt.setPosIdx(idx - 1);
					}
				}
			} catch (NumberFormatException NFEx) {
			/* Ignore invalid input */ }

			lastScreen();
			break;

			// Jump/ Label instruction edit
		case SET_LBL_NUM:
			try {
				int idx = Integer.parseInt(workingText.toString());

				if (idx < 0 || idx > 99) {
					println("Invalid label index!");
				} else {
					((LabelInstruction) r.getInstToEdit(getActiveProg(), getActiveInstIdx())).setLabelNum(idx);
				}
			} catch (NumberFormatException NFEx) {
			/* Ignore invalid input */ }

			lastScreen();
			break;
		case SET_JUMP_TGT:
			try {
				int lblNum = Integer.parseInt(workingText.toString());
				int lblIdx = p.findLabelIdx(lblNum);
				Instruction inst = r.getInstToEdit(getActiveProg(), getActiveInstIdx());
				
				if (inst instanceof IfStatement) {
					IfStatement ifStmt = (IfStatement) inst;
					((JumpInstruction) ifStmt.getInstr()).setTgtLblNum(lblNum);
				} else if (inst instanceof SelectStatement) {
					SelectStatement sStmt = (SelectStatement) inst;
					((JumpInstruction) sStmt.getInstrs().get(editIdx)).setTgtLblNum(lblNum);
				} else {
					if (lblIdx != -1) {
						JumpInstruction jmp = (JumpInstruction) inst;
						jmp.setTgtLblNum(lblNum);
					} else {
						System.err.println("Invalid label number.");
					}
				}
			} catch (NumberFormatException NFEx) {
			/* Ignore invalid input */ }

			lastScreen();
			break;
		case SET_CALL_PROG:
			Instruction inst = r.getInstToEdit(getActiveProg(), getActiveInstIdx());
			CallInstruction cInst;
			
			// Get the call instruction
			if (inst instanceof IfStatement) {
				cInst = (CallInstruction) ((IfStatement) inst).getInstr();
				
			} else if (inst instanceof SelectStatement) {
				SelectStatement sStmt = (SelectStatement) inst;
				cInst = (CallInstruction) sStmt.getInstrs().get(editIdx);
				
			} else {
				cInst =  (CallInstruction) inst;
			}
			
			// Set the program of the call instruction
			Program tgt = cInst.getTgtDevice().getProgram( options.getActiveIndex() );
			cInst.setProg(tgt);

			lastScreen();
			break;
			// Macro edit screens
		case SET_MACRO_PROG:
			if (edit_macro == null) {
				edit_macro = new Macro(contents.getLineIdx());
				macros.add(edit_macro);
				switchScreen(ScreenMode.SET_MACRO_TYPE);
			} else {
				edit_macro.setProgram(contents.getLineIdx());
			}
			break;
		case SET_MACRO_TYPE:
			if (options.getLineIdx() == 0) {
				edit_macro.setManual(false);
				switchScreen(ScreenMode.SET_MACRO_BINDING);
			} else if (options.getLineIdx() == 1) {
				edit_macro.setManual(true);
				edit_macro.clearNum();
				lastScreen();
			}
			break;
		case SET_MACRO_BINDING:
			edit_macro.setNum(options.getLineIdx());
			lastScreen();
			break;

		case NAV_MF_MACROS:
			int macro_idx = contents.get(active_index).getItemIdx();
			execute(macros.get(macro_idx));
			break;

			// Program instruction editing and navigation
		case SELECT_CUT_COPY:
		case SELECT_INSTR_DELETE:
			contents.toggleSelect(getActiveInstIdx());
			updatePendantScreen();
			break;
		case SELECT_PASTE_OPT:
			if (options.getLineIdx() == 0) {
				pasteInstructions(Fields.CLEAR_POSITION);
			} else if (options.getLineIdx() == 1) {
				pasteInstructions(Fields.PASTE_DEFAULT);
			} else if (options.getLineIdx() == 2) {
				pasteInstructions(Fields.NEW_POSITION);
			} else if (options.getLineIdx() == 3) {
				pasteInstructions(Fields.PASTE_REVERSE | Fields.CLEAR_POSITION);
			} else if (options.getLineIdx() == 4) {
				pasteInstructions(Fields.PASTE_REVERSE);
			} else if (options.getLineIdx() == 5) {
				pasteInstructions(Fields.PASTE_REVERSE | Fields.NEW_POSITION);
			} else if (options.getLineIdx() == 6) {
				pasteInstructions(Fields.PASTE_REVERSE | Fields.REVERSE_MOTION);
			} else {
				pasteInstructions(Fields.PASTE_REVERSE | Fields.NEW_POSITION | Fields.REVERSE_MOTION);
			}
			
			
			ScreenState prev = null;
			
			while (!screenStates.isEmpty()) {
				prev = screenStates.peek();
				
				if (prev.mode == ScreenMode.NAV_INSTR_MENU) {
					break;
				}
				
				screenStates.pop();
			}
			
			lastScreen();
			break;
		case SELECT_COMMENT:
			r.getInstToEdit(getActiveProg(), getActiveInstIdx()).toggleCommented();

			updatePendantScreen();
			break;
		case EDIT_PROG_POS:
			pMInst = (PosMotionInst) r.getInstToEdit(getActiveProg(), getActiveInstIdx());
			Point pt = parsePosFromContents(pMInst.getMotionType() != Fields.MTYPE_JOINT);

			if (pt != null) {
				// Update the position of the active motion instruction
				getActiveProg().setPosition(pMInst.getPosIdx(), pt);
				DataManagement.saveRobotData(activeRobot, 1);
			}

			lastScreen();
			break;
		case FIND_REPL:
			lastScreen();
			break;
		case JUMP_TO_LINE:
			int jumpToInst = Integer.parseInt(workingText.toString()) - 1;
			setActiveInstIdx(max(0, min(jumpToInst, p.getNumOfInst() - 1)));

			lastScreen();
			break;
		case SWAP_PT_TYPE:

			if (active_index >= 0 && active_index < Fields.DPREG_NUM) {
				// Set the position type of the selected position register
				PositionRegister toEdit = activeRobot.getPReg(active_index);
				toEdit.isCartesian = options.getLineIdx() == 0;
				DataManagement.saveRobotData(activeRobot, 3);
				lastScreen();
			}

			break;

		case NAV_DATA:
			int select = contents.getLineIdx();
			
			if (select == 0) {
				// Data Register Menu
				nextScreen(ScreenMode.NAV_DREGS);
			} else if (select == 1) {
				// Position Register Menu
				nextScreen(ScreenMode.NAV_PREGS);
			}
			break;
		case CP_DREG_COM:
			int regIdx = -1;

			try {
				// Copy the comment of the curent Data register to the Data
				// register at the specified index
				regIdx = Integer.parseInt(workingText.toString()) - 1;
				activeRobot.getDReg(regIdx).comment = activeRobot.getDReg(active_index).comment;
				DataManagement.saveRobotData(activeRobot, 3);

			} catch (NumberFormatException MFEx) {
				println("Only real numbers are valid!");
			} catch (IndexOutOfBoundsException IOOBEx) {
				println("Only positve integers between 1 and 100 are valid!");
			}

			lastScreen();
			break;
		case CP_DREG_VAL:
			regIdx = -1;

			try {
				// Copy the value of the curent Data register to the Data
				// register at the specified index
				regIdx = Integer.parseInt(workingText.toString()) - 1;
				activeRobot.getDReg(regIdx).value = activeRobot.getDReg(active_index).value;
				DataManagement.saveRobotData(activeRobot, 3);

			} catch (NumberFormatException MFEx) {
				println("Only real numbers are valid!");
			} catch (IndexOutOfBoundsException IOOBEx) {
				println("Only positve integers between 1 and 100 are valid!");
			}

			lastScreen();
			break;
		case CP_PREG_COM:
			regIdx = -1;

			try {
				// Copy the comment of the curent Position register to the
				// Position register at the specified index
				regIdx = Integer.parseInt(workingText.toString()) - 1;
				activeRobot.getPReg(regIdx).comment = activeRobot.getPReg(active_index).comment;
				DataManagement.saveRobotData(activeRobot, 3);

			} catch (NumberFormatException MFEx) {
				println("Only real numbers are valid!");
			} catch (IndexOutOfBoundsException IOOBEx) {
				println("Only positve integers between 1 and 100 are valid!");
			}

			lastScreen();
			break;
		case CP_PREG_PT:
			regIdx = -1;

			try {
				// Copy the point of the curent Position register to the
				// Position register at the specified index
				regIdx = Integer.parseInt(workingText.toString()) - 1;
				PositionRegister src = activeRobot.getPReg(active_index);
				PositionRegister dest = activeRobot.getPReg(regIdx);
				
				dest.point = src.point.clone();
				dest.isCartesian = src.isCartesian;
				DataManagement.saveRobotData(activeRobot, 3);

			} catch (NumberFormatException MFEx) {
				println("Only real numbers are valid!");
			} catch (IndexOutOfBoundsException IOOBEx) {
				println("Only positve integers between 1 and 100 are valid!");
			}

			lastScreen();
			break;
		case EDIT_DREG_VAL:
			Float f = null;

			try {
				// Read inputed Float value
				f = Float.parseFloat(workingText.toString());
				// Clamp the value between -9999 and 9999, inclusive
				f = max(-9999f, min(f, 9999f));
				DataRegister dReg = activeRobot.getDReg(active_index);

				if (dReg != null) {
					// Save inputed value
					dReg.value = f;
					DataManagement.saveRobotData(activeRobot, 3);
				}

			} catch (NumberFormatException NFEx) {
				// Invalid input value
				println("Value must be a real number!");
			}

			lastScreen();
			break;
		case NAV_DREGS:
			if (contents.getColumnIdx() == 0) {
				// Edit register comment
				nextScreen(ScreenMode.EDIT_DREG_COM);
			} else if (contents.getColumnIdx() >= 1) {
				// Edit Data Register value
				nextScreen(ScreenMode.EDIT_DREG_VAL);
			}
			break;
		case NAV_PREGS:
			if (contents.getColumnIdx() == 0) {
				// Edit register comment
				nextScreen(ScreenMode.EDIT_PREG_COM);
			} else if (contents.getColumnIdx() >= 1) {
				// Edit Position Register value
				nextScreen(ScreenMode.EDIT_PREG);
			}
			break;
		case EDIT_PREG:
			PositionRegister pReg = activeRobot.getPReg(active_index);
			pt = parsePosFromContents(pReg.isCartesian);

			if (pt != null) {
				// Position was successfully pulled form the contents menu
				pReg.point = pt;
				DataManagement.saveRobotData(activeRobot, 3);
			}

			lastScreen();
			break;
		case EDIT_PREG_COM:
			if (!workingText.equals("\0")) {
				if (workingText.charAt(workingText.length() - 1) == '\0') {
					workingText.deleteCharAt(workingText.length() - 1);
				}
				// Save the inputed comment to the selected register
				activeRobot.getPReg(active_index).comment = workingText.toString();
				DataManagement.saveRobotData(activeRobot, 3);
				workingText = new StringBuilder();
				lastScreen();
			}
			break;
		case EDIT_DREG_COM:
			if (!workingText.equals("\0")) {
				if (workingText.charAt(workingText.length() - 1) == '\0') {
					workingText.deleteCharAt(workingText.length() - 1);
				}
				// Save the inputed comment to the selected register
				activeRobot.getDReg(active_index).comment = workingText.toString();
				DataManagement.saveRobotData(activeRobot, 3);
				workingText = new StringBuilder();
				lastScreen();
			}
			break;
		default:
			break;
		}
	}
	
	/**
	 * Pendant F1 button
	 * 
	 * Function varies amongst menus. A hint label will appear in the pendant
	 * screen above a function button, which has an action in the current
	 * menu.
	 */
	public void f1() {
		switch (mode) {
		case NAV_PROGRAMS:
			nextScreen(ScreenMode.PROG_CREATE);
			break;
		case NAV_PROG_INSTR:
			if (isShift()) {
				newPosMotionInst();
				contents.setColumnIdx(0);

				if (getSelectedLine() == 0) {
					contents.setLineIdx(contents.getLineIdx() + 1);
					updatePendantScreen();
					if (getSelectedLine() == 0) {
						setActiveInstIdx(getActiveInstIdx() + 1);
					}
				}
			}
			break;
		case NAV_TOOL_FRAMES:
			int frame = contents.getActiveLine().getItemIdx();
			
			if (isShift()) {
				// Reset the highlighted frame in the tool frame list
				activeRobot.getToolFrame(frame).reset();
				updatePendantScreen();
			} else {
				// Set the current tool frame
				activeRobot.setActiveToolFrame(frame);
			}
			
			break;
		case NAV_USER_FRAMES:
			frame = contents.getActiveLine().getItemIdx();
			
			if (isShift()) {
				// Reset the highlighted frame in the user frames list
				activeRobot.getUserFrame(frame).reset();
				updatePendantScreen();
			} else {
				// Set the current user frame
				activeRobot.setActiveUserFrame(frame);
			}
			break;
		case ACTIVE_FRAMES:
			if (contents.getLineIdx() == 0) {
				nextScreen(ScreenMode.NAV_TOOL_FRAMES);

			} else if (contents.getLineIdx() == 1) {
				nextScreen(ScreenMode.NAV_USER_FRAMES);
			}
			break;
		case TFRAME_DETAIL:
			nextScreen(ScreenMode.TFRAME_RENAME);
			break;
		case UFRAME_DETAIL:
			nextScreen(ScreenMode.UFRAME_RENAME);
			break;
		case NAV_MACROS:
			edit_macro = null;
			nextScreen(ScreenMode.SET_MACRO_PROG);
			break;
		case NAV_DREGS:
			// Clear Data Register entry
			DataRegister dReg = activeRobot.getDReg(active_index);

			if (dReg != null) {
				dReg.comment = null;
				dReg.value = null;
			}

			break;
		case NAV_PREGS:
			// Clear Position Register entry
			PositionRegister pReg = activeRobot.getPReg(active_index);

			if (pReg != null) {
				pReg.comment = null;
				pReg.point = null;
			}

			break;
		default:
			if (mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
				editTextEntry(0);
			}
		}

		updatePendantScreen();
	}
	
	/**
	 * Pendant F2 button
	 * 
	 * Function varies amongst menus. A hint label will appear in the pendant
	 * screen above a function button, which has an action in the current
	 * menu.
	 */
	public void f2() {
		switch (mode) {
		case NAV_PROGRAMS:
			if (activeRobot.numOfPrograms() > 0) {
				nextScreen(ScreenMode.PROG_RENAME);
			}
			break;
		case NAV_PROG_INSTR:
			nextScreen(ScreenMode.SELECT_INSTR_INSERT);
			break;
		case SELECT_CUT_COPY:
			nextScreen(ScreenMode.SELECT_PASTE_OPT);
			break;
		case TEACH_3PT_TOOL:
		case TEACH_6PT:
		case DIRECT_ENTRY_TOOL:
			lastScreen();
			break;
		case TEACH_3PT_USER:
		case TEACH_4PT:
		case DIRECT_ENTRY_USER:
			lastScreen();
			break;
		case NAV_DREGS:
			// Data Register copy menus
			if (contents.getColumnIdx() == 0) {
				nextScreen(ScreenMode.CP_DREG_COM);
			} else if (contents.getColumnIdx() == 1) {
				nextScreen(ScreenMode.CP_DREG_VAL);
			}
			break;
		case NAV_PREGS:
			// Position Register copy menus
			if (contents.getColumnIdx() == 0) {
				nextScreen(ScreenMode.CP_PREG_COM);
			} else if (contents.getColumnIdx() == 1) {
				nextScreen(ScreenMode.CP_PREG_PT);
			}
			break;
		default:
			if (mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
				editTextEntry(1);
				updatePendantScreen();
			}
		}
	}
	
	/**
	 * Pendant F3 button
	 * 
	 * Function varies amongst menus. A hint label will appear in the pendant
	 * screen above a function button, which has an action in the current
	 * menu.
	 */
	public void f3() {
		RoboticArm r = activeRobot;
		
		switch (mode) {
		case NAV_PROGRAMS:
			if (r.numOfPrograms() > 0) {
				setActiveProgIdx( contents.getActiveIndex() );
				nextScreen(ScreenMode.CONFIRM_PROG_DELETE);
			}
			break;
		case NAV_PROG_INSTR:
			Instruction inst = getActiveInstruction();
			int selectIdx = getSelectedIdx();

			if (inst instanceof PosMotionInst) {
				r.getInstToEdit(getActiveProg(), getActiveInstIdx());
				
				Point pt = r.getToolTipUser();
				Program p = getActiveProg();
				int actInst = getActiveInstIdx();

				if (getSelectedLine() == 1) {
					// Update the secondary position in a circular motion
					// instruction
					r.updateMCInstPosition(p, actInst, pt);

				} else {
					// Update the position associated with the active motion
					// instruction
					r.updateMInstPosition(p, actInst, pt);
				}

				PosMotionInst mInst = (PosMotionInst) inst;

				// Update the motion instruction's fields
				CoordFrame coord = activeRobot.getCurCoordFrame();

				if (coord == CoordFrame.JOINT) {
					mInst.setMotionType(Fields.MTYPE_JOINT);

				} else {
					/*
					 * Keep circular motion instructions as circular motion
					 * instructions in world, tool, or user frame modes
					 */
					if (mInst.getMotionType() == Fields.MTYPE_JOINT) {
						mInst.setMotionType(Fields.MTYPE_LINEAR);
					}					
				}
				
				mInst.setSpdMod(0.5f);
				mInst.setTFrameIdx(activeRobot.getActiveToolIdx());
				mInst.setUFrameIdx(activeRobot.getActiveUserIdx());

			} else if (inst instanceof IfStatement) {
				IfStatement stmt = (IfStatement) inst;

				if (stmt.getExpr() instanceof Expression && selectIdx >= 2) {
					r.getInstToEdit(getActiveProg(), getActiveInstIdx());
					((Expression) stmt.getExpr()).insertElement(selectIdx - 3);
					updatePendantScreen();
					arrow_rt();
				}
			} else if (inst instanceof SelectStatement) {
				SelectStatement stmt = (SelectStatement) inst;

				if (selectIdx >= 3) {
					r.getInstToEdit(getActiveProg(), getActiveInstIdx());
					stmt.addCase();
					updatePendantScreen();
					arrow_dn();
				}
			} else if (inst instanceof RegisterStatement) {
				RegisterStatement stmt = (RegisterStatement) inst;
				int rLen = (stmt.getPosIdx() == -1) ? 2 : 3;

				if (selectIdx > rLen) {
					r.getInstToEdit(getActiveProg(), getActiveInstIdx());
					stmt.getExpr().insertElement(selectIdx - (rLen + 2));
					updatePendantScreen();
					arrow_rt();
				}
			}

			updatePendantScreen();
			break;
		case SELECT_CUT_COPY:
			Program p = getActiveProg();
			int size = p.getNumOfInst();
			clipBoard = new ArrayList<>();

			int remIdx = 0;
			for (int i = 0; i < size; i += 1) {
				
				if (contents.isSelected(i)) {
					clipBoard.add(p.get(remIdx));
					p.rmInstAt(remIdx);
					
				} else {
					remIdx += 1;
				}
			}

			break;
		case NAV_TOOL_FRAMES:
			active_index = 0;
			switchScreen(ScreenMode.NAV_USER_FRAMES);
			break;
		case NAV_USER_FRAMES:
			active_index = 0;
			switchScreen(ScreenMode.NAV_TOOL_FRAMES);
			break;
		case NAV_DREGS:
			// Switch to Position Registers
			nextScreen(ScreenMode.NAV_PREGS);
			break;
		case NAV_PREGS:
			if (isShift()) {
				switchScreen(ScreenMode.NAV_DREGS);
			} else {
				// Switch to Data Registers
				nextScreen(ScreenMode.SWAP_PT_TYPE);
			}
			break;
		default:
			if (mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
				editTextEntry(2);
				updatePendantScreen();
			}
		}
	}
	
	/**
	 * Pendant F4 button
	 * 
	 * Function varies amongst menus. A hint label will appear in the pendant
	 * screen above a function button, which has an action in the current
	 * menu.
	 */
	public void f4() {
		RoboticArm r = activeRobot;
		Program p = getActiveProg();

		switch (mode) {
		case NAV_PROGRAMS:
			if (activeRobot.numOfPrograms() > 0) {
				nextScreen(ScreenMode.PROG_COPY);
			}
			break;
		case NAV_PROG_INSTR:
			Instruction ins = getActiveInstruction();

			if (ins != null) {
				int selectIdx = getSelectedIdx();
				getEditScreen(ins, selectIdx);
			}

			break;
		case CONFIRM_INSERT:
			try {
				int lines_to_insert = Integer.parseInt(workingText.toString());
				for (int i = 0; i < lines_to_insert; i += 1) {
					p.addInstAt(getActiveInstIdx(), new Instruction());
				}
				
				updateInstructions();
			} catch (Exception e) {
				e.printStackTrace();
			}

			lastScreen();
			break;
		case CONFIRM_PROG_DELETE:
			r = activeRobot;
			int progIdx = getActiveProgIdx();

			if (progIdx >= 0 && progIdx < r.numOfPrograms()) {
				r.rmProgAt(progIdx);
				lastScreen();
			}
			break;
		case SELECT_INSTR_DELETE:
			r = activeRobot;
			int instrIdx = 0;

			for (int i = 0; i < contents.getSelection().length; i += 1) {
				if (contents.isSelected(i)) {
					r.rmInstAt(p, instrIdx);
				} else {
					instrIdx += 1;
				}
			}

			screenStates.pop();
			updateInstructions();
			break;
		case SELECT_CUT_COPY:
			clipBoard = new ArrayList<>();

			for (int i = 0; i < p.getNumOfInst(); i += 1) {
				if (contents.isSelected(i))
					clipBoard.add(p.get(i).clone());
			}

			break;
		case FIND_REPL:
			int lineIdx = 0;
			String s;

			for (Instruction instruct : p) {
				s = (lineIdx + 1) + ") " + instruct.toString();

				if (s.toUpperCase().contains(workingText.toString().toUpperCase())) {
					break;
				}

				lineIdx += 1;
			}

			screenStates.pop();
			setActiveInstIdx(lineIdx);
			updateInstructions();
			break;
		case SELECT_COMMENT:
			screenStates.pop();
			updateInstructions();
			break;
		case CONFIRM_RENUM:
			Point[] pTemp = new Point[1000];
			int posIdx = 0;

			// make a copy of the current positions in p
			for (int i = 0; i < 1000; i += 1) {
				pTemp[i] = p.getPosition(i);
			}

			p.clearPositions();

			// rearrange positions
			for (int i = 0; i < p.getNumOfInst(); i += 1) {
				Instruction instr = p.get(i);

				if (instr instanceof PosMotionInst) {
					// Update the primary position
					PosMotionInst mInst = ((PosMotionInst) instr);
					int oldPosNum = mInst.getPosIdx();
					
					if (oldPosNum >= 0 && oldPosNum < pTemp.length) {
						p.setPosition(posIdx, pTemp[oldPosNum]);
						mInst.setPosIdx(posIdx++);
					}

					if (mInst.getMotionType() == Fields.MTYPE_CIRCULAR) {

						/*
						 * Update position for secondary point of a circular
						 * motion instruction
						 */
						oldPosNum = mInst.getCircPosIdx();
						
						if (oldPosNum >= 0 && oldPosNum < pTemp.length) {
							p.setPosition(posIdx, pTemp[oldPosNum]);
							mInst.setCircPosIdx(posIdx++);
						}
					}
				}
			}

			screenStates.pop();
			updateInstructions();
			break;
		case NAV_MACROS:
			if(macros.size() > 0) {
				edit_macro = macros.get(contents.getLineIdx());
				
				if (contents.getColumnIdx() == 1) {
					nextScreen(ScreenMode.SET_MACRO_PROG);
				} else if (contents.getColumnIdx() == 2) {
					nextScreen(ScreenMode.SET_MACRO_TYPE);
				} else if (contents.getColumnIdx() == 3){
					if (!macros.get(contents.getLineIdx()).isManual())
						nextScreen(ScreenMode.SET_MACRO_BINDING);
				}
			}
			break;
		case NAV_PREGS:
			if (isShift() && !isProgExec()) {
				// Stop any prior jogging motion
				hold();

				// Move To function
				PositionRegister pReg = r.getPReg( contents.getActiveIndex() );

				if (pReg.point != null) {
					Point pt = pReg.point.clone();
					// Move the Robot to the select point
					if (pReg.isCartesian) {
						
						if (r.getCurCoordFrame() == CoordFrame.USER) {
							// Move in terms of the user frame
							UserFrame uFrame = r.getActiveUser();
							pt = RMath.removeFrame(r, pt, uFrame.getOrigin(), uFrame.getOrientation());

							Fields.debug("pt: %s\n", pt.position.toString());
						}

						r.updateMotion(pt);

					} else {
						r.updateMotion(pt.angles);
					}
				} else {
					println("Position register is uninitialized!");
				}
			}

			break;
		default:
			if (mode.getType() == ScreenType.TYPE_TEACH_POINTS) {

				if (isShift() && teachFrame != null) {
					Point tgt = teachFrame.getPoint(options.getLineIdx());

					if (mode == ScreenMode.TEACH_3PT_USER || mode == ScreenMode.TEACH_4PT) {
						if (tgt != null && tgt.position != null && tgt.orientation != null) {
							// Move to the point's position and orientation
							activeRobot.updateMotion(tgt);
						}
					} else {
						if (tgt != null && tgt.angles != null) {
							// Move to the point's joint angles
							activeRobot.updateMotion(tgt.angles);
						}
					}
				}
			} else if (mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
				editTextEntry(3);
			}

		}

		updatePendantScreen();
	}
	
	/**
	 * Pendant F5 button
	 * 
	 * Function varies amongst menus. A hint label will appear in the pendant
	 * screen above a function button, which has an action in the current
	 * menu.
	 */
	public void f5() {
		RoboticArm r = activeRobot;
		Instruction inst = getActiveInstruction();
		
		switch (mode) {
		case NAV_PROG_INSTR:
			int selectIdx = getSelectedIdx();	

			if (selectIdx == 0) {
				nextScreen(ScreenMode.NAV_INSTR_MENU);
				
			} else if (inst instanceof MotionInstruction) {
				int regState = selectedMInstRegState();
				
				if (regState == 1) {
					// Only allow editing of primary position of motion instruction
					nextScreen(ScreenMode.EDIT_PROG_POS);	
				}
				
			} else if (inst instanceof IfStatement) {
				IfStatement stmt = (IfStatement) inst;
				if (stmt.getExpr() instanceof Expression) {
					r.getInstToEdit(getActiveProg(), getActiveInstIdx());
					((Expression) stmt.getExpr()).removeElement(selectIdx - 3);
				}
			} else if (inst instanceof SelectStatement) {
				SelectStatement stmt = (SelectStatement) inst;
				if (selectIdx >= 3) {
					r.getInstToEdit(getActiveProg(), getActiveInstIdx());
					stmt.deleteCase((selectIdx - 3) / 3);
				}
			} else if (inst instanceof RegisterStatement) {
				RegisterStatement stmt = (RegisterStatement) inst;
				int rLen = (stmt.getPosIdx() == -1) ? 2 : 3;
				
				if (selectIdx > (rLen + 1) && selectIdx < stmt.getExpr().getLength() + rLen) {
					r.getInstToEdit(getActiveProg(), getActiveInstIdx());
					stmt.getExpr().removeElement(selectIdx - (rLen + 2));
				}
			}
			break;
		case TEACH_3PT_USER:
		case TEACH_3PT_TOOL:
		case TEACH_4PT:
		case TEACH_6PT:
			if (isShift()) {
				// Save the Robot's current position and joint angles
				Point pt;
				
				if (mode == ScreenMode.TEACH_3PT_USER || mode == ScreenMode.TEACH_4PT) {
					pt = activeRobot.getToolTipNative();
					
				} else {
					pt = activeRobot.getFacePlatePoint();
				}

				teachFrame.setPoint(pt, options.getLineIdx());
				DataManagement.saveRobotData(activeRobot, 2);
				updatePendantScreen();
			}
			break;
		case CONFIRM_PROG_DELETE:
			lastScreen();
			break;
		case SELECT_INSTR_DELETE:
		case CONFIRM_INSERT:
		case CONFIRM_RENUM:
		case FIND_REPL:
		case SELECT_CUT_COPY:
			screenStates.pop();
			updateInstructions();
			break;
		case TFRAME_DETAIL:
			// Set a default tool tip for the selected tool frame
			nextScreen(ScreenMode.SET_DEF_TOOLTIP);
			break;
		case NAV_PREGS:
			PositionRegister pReg = r.getPReg( contents.getActiveIndex() );

			if (isShift() && pReg != null) {
				// Save the Robot's current position and joint angles
				pReg.point = activeRobot.getToolTipNative();
				pReg.isCartesian = true;
				DataManagement.saveRobotData(r, 3);
			}
			break;
		default:
			if (mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
				editTextEntry(4);
			}
		}

		updatePendantScreen();
	}
	
	/**
	 * Camera F button
	 * 
	 * Sets the camera to the default position, facing down the negative y-axis
	 * of the world coordinate system.
	 */
	public void FrontView() {
		// Default view
		camera.reset();
	}
	
	/**
	 * Pendant FWD button
	 * 
	 * Executes instructions in the instruction navigation of a program. If
	 * step is active, then only one instruction is executed at a time,
	 * otherwise the entire program is executed.
	 */
	public void fwd() {
		if (mode == ScreenMode.NAV_PROG_INSTR && !isProgExec() && isShift()) {
			// Stop any prior Robot movement
			hold();
			// Safeguard against editing a program while it is running
			contents.setColumnIdx(0);
			progExec(isStep());
		}
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

	/**
	 * @return the active axes display state
	 */
	public AxesDisplay getAxesState() {
		return UI.getAxesDisplay();
	}

	public Camera getCamera() {
		return camera;
	}
	
	public MenuScroll getContentsMenu() {
		return contents;
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

	public void getEditScreen(Instruction ins, int selectIdx) {
		if (ins instanceof MotionInstruction) {
			MotionInstruction mInst = (MotionInstruction)ins;
			int sdx = getSelectedIdx();
			
			if (sdx == 2) {
				// Motion type
				nextScreen(ScreenMode.SET_MINST_TYPE);
				
			} else if (sdx == 3) {
				// Position type
				nextScreen(ScreenMode.SET_MINST_REG_TYPE);
				
			} else if (sdx == 4) {
				
				if (mInst instanceof CamMoveToObject) {
					CamMoveToObject cMInst = (CamMoveToObject)mInst;
					
					if (cMInst.getScene() == null) {
						cMInst.setScene(activeScenario);
					}
					
					// Set World Object reference
					nextScreen(ScreenMode.SET_MINST_WO);
				} else {
					// Position index
					nextScreen(ScreenMode.SET_MINST_IDX);
				}
				
			} else if (sdx == 5) {
				// Speed modifier
				nextScreen(ScreenMode.SET_MINST_SPD);
				
			} else if (sdx == 6) {
				// Termination
				nextScreen(ScreenMode.SET_MINST_TERM);
				
			} else if (sdx == 7) {
				// Offset type
				nextScreen(ScreenMode.SET_MINST_OFF_TYPE);
				
			} else if (mInst instanceof PosMotionInst) {
				PosMotionInst pMInst = (PosMotionInst)mInst;
				
				if (pMInst.getOffsetType() == Fields.OFFSET_PREG) {
					
					if (sdx == 8) {
						// Offset index
						nextScreen(ScreenMode.SET_MINST_OFFIDX);
						
					} else if (sdx == 9) {
						// Circular position type
						nextScreen(ScreenMode.SET_MINST_CREG_TYPE);
						
					} else if (sdx == 10) {
						// Circular position index
						nextScreen(ScreenMode.SET_MINST_CIDX);
					}
					
				} else if (pMInst.getOffsetType() == Fields.OFFSET_NONE) {
					
					if (sdx == 8) {
						// Circular position type
						nextScreen(ScreenMode.SET_MINST_CREG_TYPE);
						
					} else if (sdx == 9) {
						// Circular position index
						nextScreen(ScreenMode.SET_MINST_CIDX);
					}
				}
				
			}
			
		} else if (ins instanceof FrameInstruction) {
			switch (selectIdx) {
			case 1:
				nextScreen(ScreenMode.SET_FRM_INSTR_TYPE);
				break;
			case 2:
				nextScreen(ScreenMode.SET_FRAME_INSTR_IDX);
				break;
			}
		} else if (ins instanceof IOInstruction) {
			switch (selectIdx) {
			case 1:
				nextScreen(ScreenMode.SET_IO_INSTR_IDX);
				break;
			case 2:
				nextScreen(ScreenMode.SET_IO_INSTR_STATE);
				break;
			}
		} else if (ins instanceof LabelInstruction) {
			nextScreen(ScreenMode.SET_LBL_NUM);
		} else if (ins instanceof JumpInstruction) {
			nextScreen(ScreenMode.SET_JUMP_TGT);
		} else if (ins instanceof CallInstruction) {
			if (((CallInstruction) ins).getTgtDevice() != null) {
				editIdx = ((CallInstruction) ins).getTgtDevice().RID;

			} else {
				editIdx = -1;
			}

			nextScreen(ScreenMode.SET_CALL_PROG);
		} else if (ins instanceof IfStatement) {
			IfStatement stmt = (IfStatement) ins;
			
			if (stmt.getExpr() instanceof Expression) {
				int len = stmt.getExpr().getLength();

				if (selectIdx >= 3 && selectIdx < len + 1) {
					editExpression((Expression) stmt.getExpr(), selectIdx - 3);
				} else if (selectIdx == len + 2) {
					nextScreen(ScreenMode.SET_IF_STMT_ACT);
				} else if (selectIdx == len + 3) {
					if (stmt.getInstr() instanceof JumpInstruction) {
						nextScreen(ScreenMode.SET_JUMP_TGT);
					} else if (stmt.getInstr() instanceof CallInstruction) {
						nextScreen(ScreenMode.SET_CALL_PROG);
					}
				}
			} else if (stmt.getExpr() instanceof AtomicExpression) {
				if (selectIdx == 2) {
					opEdit = stmt.getExpr().getArg1();
					editIdx = 0;
					nextScreen(ScreenMode.SET_BOOL_EXPR_ARG);
				} else if (selectIdx == 3) {
					opEdit = stmt.getExpr();
					nextScreen(ScreenMode.SET_EXPR_OP);
				} else if (selectIdx == 4) {
					opEdit = stmt.getExpr().getArg2();
					editIdx = 2;
					nextScreen(ScreenMode.SET_BOOL_EXPR_ARG);
				} else if (selectIdx == 5) {
					nextScreen(ScreenMode.SET_IF_STMT_ACT);
				} else if (selectIdx == 6) {
					if (stmt.getInstr() instanceof JumpInstruction) {
						nextScreen(ScreenMode.SET_JUMP_TGT);
					} else if (stmt.getInstr() instanceof CallInstruction) {
						nextScreen(ScreenMode.SET_CALL_PROG);
					}
				}
			}
		} else if (ins instanceof SelectStatement) {
			SelectStatement stmt = (SelectStatement) ins;
			editIdx = (selectIdx - 3) / 3;
			
			if (selectIdx == 2) {
				opEdit = stmt.getArg();
				editIdx = -1;
				nextScreen(ScreenMode.SET_SELECT_STMT_ARG);
			} else if ((selectIdx - 3) % 3 == 0 && selectIdx > 2) {
				opEdit = stmt.getCases().get((selectIdx - 3) / 3);
				nextScreen(ScreenMode.SET_SELECT_STMT_ARG);
			} else if ((selectIdx - 3) % 3 == 1) {
				nextScreen(ScreenMode.SET_SELECT_STMT_ACT);
			} else if ((selectIdx - 3) % 3 == 2) {
				Instruction toExec = stmt.getInstrs().get(editIdx);
				if (toExec instanceof JumpInstruction) {
					nextScreen(ScreenMode.SET_JUMP_TGT);
				} else if (toExec instanceof CallInstruction) {
					nextScreen(ScreenMode.SET_CALL_PROG);
				}
			}
		} else if (ins instanceof RegisterStatement) {
			RegisterStatement stmt = (RegisterStatement) ins;
			int len = stmt.getExpr().getLength();
			int rLen = (stmt.getPosIdx() == -1) ? 2 : 3;
			
			if (selectIdx == 1) {
				nextScreen(ScreenMode.SET_REG_EXPR_TYPE);
			} else if (selectIdx == 2) {
				nextScreen(ScreenMode.SET_REG_EXPR_IDX1);
			} else if (selectIdx == 3 && stmt.getPosIdx() != -1) {
				nextScreen(ScreenMode.SET_REG_EXPR_IDX2);
			} else if (selectIdx >= rLen + 1 && selectIdx <= len + rLen) {
				editExpression(stmt.getExpr(), selectIdx - (rLen + 2));
			}
		}
	}

	// Function label text
	public String[] getFunctionLabels(ScreenMode mode) {
		String[] funct = new String[5];

		switch (mode) {
		case NAV_PROGRAMS:
			// F2, F3
			funct[0] = "[Create]";
			if (activeRobot.numOfPrograms() > 0) {
				funct[1] = "[Rename]";
				funct[2] = "[Delete]";
				funct[3] = "[Copy]";
				funct[4] = "";
			} else {
				funct[1] = "";
				funct[2] = "";
				funct[3] = "";
				funct[4] = "";
			}
			break;
		case NAV_PROG_INSTR:
			Instruction inst = getActiveInstruction();

			// F1, F4, F5f
			funct[0] = "[New Pt]";
			funct[1] = "[New Ins]";
			funct[2] = "";
			funct[3] = "[Edit]";
			funct[4] = (contents.getColumnIdx() == 0) ? "[Opt]" : "";
						
			if (inst instanceof MotionInstruction) {
				funct[2] = "[Ovr Pt]";
				
				int regState = selectedMInstRegState();
				/* Only display edit function for a motion instruction's
				 * primary position referencing a position */
				if (regState == 1) {
					funct[4] = "[Reg]";
				}
			} 
			else if (inst instanceof IfStatement) {
				IfStatement stmt = (IfStatement) inst;
				int selectIdx = getSelectedIdx();

				if (stmt.getExpr() instanceof Expression) {
					if (selectIdx > 1 && selectIdx < stmt.getExpr().getLength() + 1) {
						funct[2] = "[Insert]";
					}
					if (selectIdx > 2 && selectIdx < stmt.getExpr().getLength() + 1) {
						funct[4] = "[Delete]";
					}
				}
			} 
			else if (inst instanceof SelectStatement) {
				int selectIdx = getSelectedIdx();

				if (selectIdx >= 3) {
					funct[2] = "[Insert]";
					funct[4] = "[Delete]";
				}
			} 
			else if (inst instanceof RegisterStatement) {
				RegisterStatement stmt = (RegisterStatement) inst;
				int rLen = (stmt.getPosIdx() == -1) ? 2 : 3;
				int selectIdx = getSelectedIdx();

				if (selectIdx > rLen && selectIdx < stmt.getExpr().getLength() + rLen) {
					funct[2] = "[Insert]";
				}
				if (selectIdx > (rLen + 1) && selectIdx < stmt.getExpr().getLength() + rLen) {
					funct[4] = "[Delete]";
				}
			}
			break;
		case EDIT_PROG_POS:
			funct[0] = "";
			funct[1] = "";
			funct[2] = "";
			funct[3] = "";
			funct[4] = "";
			break;
		case SELECT_COMMENT:
			funct[0] = "";
			funct[1] = "";
			funct[2] = "";
			funct[3] = "[Done]";
			funct[4] = "";
			break;
		case SELECT_CUT_COPY:
			funct[0] = "";
			funct[1] = clipBoard.isEmpty() ? "" : "[Paste]";
			funct[2] = "[Cut]";
			funct[3] = "[Copy]";
			funct[4] = "[Cancel]";
			break;
		case NAV_TOOL_FRAMES:
		case NAV_USER_FRAMES:
			// F1, F2, F3
			if (isShift()) {
				funct[0] = "[Clear]";
				funct[1] = "";
				funct[2] = "[Switch]";
				funct[3] = "";
				funct[4] = "";
			} else {
				funct[0] = "[Set]";
				funct[1] = "";
				funct[2] = "[Switch]";
				funct[3] = "";
				funct[4] = "";
			}
			break;
		case TFRAME_DETAIL:
			// F1, F2, F5
			funct[0] = "[Rename]";
			funct[1] = "[Method]";
			funct[2] = "";
			funct[3] = "";
			funct[4] = "[Default]";
			break;
		case UFRAME_DETAIL:
			// F1, F2
			funct[0] = "[Rename]";
			funct[1] = "[Method]";
			funct[2] = "";
			funct[3] = "";
			funct[4] = "";
			break;
		case TEACH_3PT_TOOL:
		case TEACH_3PT_USER:
		case TEACH_4PT:
		case TEACH_6PT:
			// F1, F5
			funct[0] = "";
			funct[1] = "[Method]";
			funct[2] = "";
			funct[3] = "[Mov To]";
			funct[4] = "[Record]";
			break;
		case DIRECT_ENTRY_TOOL:
		case DIRECT_ENTRY_USER:
			funct[0] = "";
			funct[1] = "[Method]";
			funct[2] = "";
			funct[3] = "";
			funct[4] = "";
			break;
		case ACTIVE_FRAMES:
			// F1, F2
			funct[0] = "[List]";
			funct[1] = "";
			funct[2] = "";
			funct[3] = "";
			funct[4] = "";
			break;
		case NAV_MACROS:
			funct[0] = "[New]";
			funct[1] = "";
			funct[2] = "";
			funct[3] = "[Edit]";
			funct[4] = "";
			break;
		case NAV_PREGS:
			// F1 - F5
			if (isShift()) {
				funct[0] = "[Clear]";
				funct[1] = "[Copy]";
				funct[2] = "[Switch]";
				funct[3] = "[Move To]";
				funct[4] = "[Record]";
			} else {
				funct[0] = "[Clear]";
				funct[1] = "[Copy]";
				funct[2] = "[Type]";
				funct[3] = "[Move To]";
				funct[4] = "[Record]";
			}
			break;
		case NAV_DREGS:
			// F1 - F3
			funct[0] = "[Clear]";
			funct[1] = "[Copy]";
			funct[2] = "[Switch]";
			funct[3] = "";
			funct[4] = "";
			break;
		case CONFIRM_INSERT:
		case CONFIRM_PROG_DELETE:
		case CONFIRM_RENUM:
		case FIND_REPL:
		case SELECT_INSTR_DELETE:
			// F4, F5
			funct[0] = "";
			funct[1] = "";
			funct[2] = "";
			funct[3] = "[Confirm]";
			funct[4] = "[Cancel]";
			break;
		default:
			if (mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
				if (options.getLineIdx() == 0) {
					// F1 - F5
					funct[0] = "[ABCDEF]";
					funct[1] = "[GHIJKL]";
					funct[2] = "[MNOPQR]";
					funct[3] = "[STUVWX]";
					funct[4] = "[YZ_@*.]";
				} else {
					funct[0] = "[abcdef]";
					funct[1] = "[ghijkl]";
					funct[2] = "[mnopqr]";
					funct[3] = "[stuvwx]";
					funct[4] = "[yz_@*.]";
				}
			} else {
				funct[0] = "";
				funct[1] = "";
				funct[2] = "";
				funct[3] = "";
				funct[4] = "";
			}
			break;
		}

		return funct;
	}

	// Header text
	public String getHeader(ScreenMode mode) {
		String header = null;

		switch (mode) {
		case NAV_MAIN_MENU:
			header = "MAIN MENU";
			break;
		case NAV_PROGRAMS:
			header = "PROGRAMS";
			break;
		case PROG_CREATE:
			header = "NAME PROGRAM";
			break;
		case PROG_RENAME:
			header = "RENAME PROGRAM";
			break;
		case PROG_COPY:
			header = "COPY PROGRAM";
			break;
		case CONFIRM_INSERT:
		case CONFIRM_RENUM:
		case INPUT_DREG_IDX:
		case INPUT_IOREG_IDX:
		case INPUT_PREG_IDX1:
		case INPUT_PREG_IDX2:
		case NAV_PROG_INSTR:
		case NAV_INSTR_MENU:
		case SET_MINST_SPD:
		case SET_MINST_IDX:
		case SET_MINST_TERM:
		case SET_MINST_OFF_TYPE:
		case SET_MINST_OFFIDX:
		case SELECT_INSTR_INSERT:
		case SET_IO_INSTR_STATE:
		case SET_FRM_INSTR_TYPE:
		case SET_FRAME_INSTR_IDX:
		case SET_EXPR_ARG:
		case SET_BOOL_EXPR_ARG:
		case SET_JUMP_TGT:
		case SET_LBL_NUM:
		case SELECT_CUT_COPY:
		case SELECT_INSTR_DELETE:
		case SET_MINST_WO:
			header = getActiveProg().getName();
			break;
		case EDIT_PROG_POS:
			Program p = getActiveProg();
			header = String.format("EDIT %s POSITION", p.getName());
			break;
		case SELECT_IO_INSTR_REG:
			header = "SELECT IO REGISTER";
			break;
		case SELECT_FRAME_INSTR_TYPE:
			header = "SELECT FRAME INSTRUCTION TYPE";
			break;
		case SELECT_COND_STMT:
			header = "INSERT IF/ SELECT STATEMENT";
			break;
		case SELECT_JMP_LBL:
			header = "INSERT JUMP/ LABEL INSTRUCTION";
			break;
		case SET_CALL_PROG:
			header = "SELECT CALL TARGET";
			break;
			
		case ACTIVE_FRAMES:
			header = "ACTIVE FRAMES";
			break;
		case SELECT_FRAME_MODE:
			header = "FRAME MODE";
			break;
		case NAV_TOOL_FRAMES:
			header = "TOOL FRAMES";
			break;
		case NAV_USER_FRAMES:
			header = "USER FRAMES";
			break;
		case TFRAME_RENAME:
			header = String.format("TOOL %d: RENAME", curFrameIdx + 1);
			break;
		case UFRAME_RENAME:
			header = String.format("USER %d: RENAME", curFrameIdx + 1);
			break;
		case TFRAME_DETAIL:
			header = String.format("TOOL %d: DETAIL", curFrameIdx + 1);
			break;
		case SET_DEF_TOOLTIP:
			header = "DEFAULT TOOL TIPS";
			break;
		case UFRAME_DETAIL:
			header = String.format("USER %d: DETAIL", curFrameIdx + 1);
			break;
		case TEACH_3PT_TOOL:
			header = String.format("TOOL %d: 3P METHOD", curFrameIdx + 1);
			break;
		case TEACH_3PT_USER:
			header = String.format("USER %d: 3P METHOD", curFrameIdx + 1);
			break;
		case TEACH_4PT:
			header = String.format("USER %d: 4P METHOD", curFrameIdx + 1);
			break;
		case TEACH_6PT:
			header = String.format("TOOL %d: 6P METHOD", curFrameIdx + 1);
			break;
		case DIRECT_ENTRY_TOOL:
			header = String.format("TOOL %d: DIRECT ENTRY", curFrameIdx + 1);
			break;
		case DIRECT_ENTRY_USER:
			header = String.format("USER %d: DIRECT ENTRY", curFrameIdx + 1);
			break;
		case NAV_MACROS:
		case SET_MACRO_TYPE:
		case SET_MACRO_BINDING:
			header = "VIEW/ EDIT MACROS";
			break;
		case NAV_MF_MACROS:
			header = "EXECUTE MANUAL FUNCTION";
			break;
		case SET_MACRO_PROG:
			header = "SELECT MACRO PROGRAM";
			break;
			
		case NAV_DATA:
			header = "VIEW REGISTERS";
			break;
		case NAV_DREGS:
			header = "REGISTERS";
			break;
		case NAV_PREGS:
			header = "POSTION REGISTERS";
			break;
		case EDIT_DREG_VAL:
			Register reg = activeRobot.getDReg(active_index);
			header = String.format("%s: VALUE EDIT", reg.getLabel());
			break;
		case EDIT_DREG_COM:
			header = String.format("R[%d]: COMMENT EDIT", active_index + 1);
			break;
		case CP_DREG_VAL:
			reg = activeRobot.getDReg(active_index);
			header = String.format("%s: VALUE COPY", reg.getLabel());
			break;
		case EDIT_PREG:
			reg = activeRobot.getPReg(active_index);
			header = String.format("%s: POSITION EDIT", reg.getLabel());
			break;
		case EDIT_PREG_COM:
			header = String.format("PR[%d]: COMMENT EDIT", active_index + 1);
			break;
		case CP_PREG_PT:
			reg = activeRobot.getPReg(active_index);
			header = String.format("%s: POSITION COPY", reg.getLabel());
			break;
		case SWAP_PT_TYPE:
			reg = activeRobot.getPReg(active_index);
			header = String.format("%s: TYPE EDIT", reg.getLabel());
			break;
		case CP_PREG_COM:
		case CP_DREG_COM:
			reg = activeRobot.getDReg(active_index);
			header = String.format("%s: COMMENT COPY", reg.getLabel());
			break;

		case NAV_IOREG:
			header = "I/O REGISTERS";
			break;

		default:
			break;
		}

		return header;
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
		ArrayList<DisplayLine> instr = loadInstructions(getActiveProg());
		int row = instrIdx;
		
		try {	
			while (instr.get(row).getItemIdx() != instrIdx) {
				row += 1;
				if (contents.getLineIdx() >= contents.size() - 1)
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
	
	public ScreenMode getMode() {
		return mode;
	}
	
	public MenuScroll getOptionsMenu() {
		return options;
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
	
	public Scenario getActiveScenario() {
		return activeScenario;
	}

	public RobotCamera getRobotCamera() {
		return rCamera;
	}

	public ArrayList<Scenario> getScenarios() {
		return SCENARIOS;
	}

	public int getSelectedIdx() {
		if (mode.getType() == ScreenType.TYPE_LINE_SELECT)
			return 0;

		int idx = contents.getColumnIdx();
		for (int i = contents.getLineIdx() - 1; i >= 0; i -= 1) {
			if (contents.get(i).getItemIdx() != contents.get(i + 1).getItemIdx())
				break;
			idx += contents.get(i).size();
		}
		
		return idx;
	}

	public int getSelectedLine() {
		int row = 0;
		DisplayLine currRow = contents.getActiveLine();
		while (contents.getLineIdx() - row >= 0
				&& currRow.getItemIdx() == contents.get(contents.getLineIdx() - row).getItemIdx()) {
			row += 1;
		}

		return row - 1;
	}

	public Macro[] getSU_macro_bindings() {
		return SU_macro_bindings;
	}

	/**
	 * Copies the current rotation and translations of the top matrix on
	 * Processing's matrix stack to a 4x4 floating-point array. Any scaling
	 * is ignored. 
	 * 
	 * @return	A 4x4 row major transformation matrix
	 */
	public RMatrix getTransformationMatrix() {
		float[][] transform = new float[3][3];

		PVector origin = getPosFromMatrix(0, 0, 0);
		PVector xAxis = getPosFromMatrix(1, 0, 0).sub(origin);
		PVector yAxis = getPosFromMatrix(0, 1, 0).sub(origin);
		PVector zAxis = getPosFromMatrix(0, 0, 1).sub(origin);

		transform[0][0] = xAxis.x;
		transform[1][0] = xAxis.y;
		transform[2][0] = xAxis.z;
		transform[0][1] = yAxis.x;
		transform[1][1] = yAxis.y;
		transform[2][1] = yAxis.z;
		transform[0][2] = zAxis.x;
		transform[1][2] = zAxis.y;
		transform[2][2] = zAxis.z;

		return RMath.formTMat(origin, new RMatrix(transform));
	}

	public WGUI getUI() {
		return UI;
	}
	
	/**
	 * Pendant HOLD button
	 * 
	 * Stops all robot motion and program execution.
	 */
	public void hold() {
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
	public void io() {
		if (isShift()) {
			if (getSU_macro_bindings()[6] != null) {
				execute(getSU_macro_bindings()[6]);
			}

		} else {
			if (!isProgExec()) {
				// Map I/O to the robot's end effector state, if shift is off
				toggleEEState(activeRobot);
			}
		}
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

	/**
	 * Pendant ITEM button
	 * 
	 * Not sure what this does ...
	 */
	public void item() {
		if (mode == ScreenMode.NAV_PROG_INSTR) {
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
	public void joint1_neg() {
		updateRobotJogMotion(0, -1);
	}

	/**
	 * Pendant +X/(J1) button
	 * 
	 * This button jogs the active robot with reference to its active coordinate
	 * frame. In addition, this button is paired with -X/(J1) in such a way that
	 * at most one of the two can be active at one time.
	 */
	public void joint1_pos() {
		updateRobotJogMotion(0, 1);
	}

	/**
	 * Pendant -Y/(J2) button
	 * 
	 * This button jogs the active robot with reference to its active coordinate
	 * frame. In addition, this button is paired with +Y/(J2) in such a way that
	 * at most one of the two can be active at one time.
	 */
	public void joint2_neg() {
		updateRobotJogMotion(1, -1);
	}
	
	/**
	 * Pendant +Y/(J2) button
	 * 
	 * This button jogs the active robot with reference to its active coordinate
	 * frame. In addition, this button is paired with -Y/(J2) in such a way that
	 * at most one of the two can be active at one time.
	 */
	public void joint2_pos() {
		updateRobotJogMotion(1, 1);
	}

	/**
	 * Pendant -Z/(J3) button
	 * 
	 * This button jogs the active robot with reference to its active coordinate
	 * frame. In addition, this button is paired with +Z/(J3) in such a way that
	 * at most one of the two can be active at one time.
	 */
	public void joint3_neg() {
		updateRobotJogMotion(2, -1);
	}
	
	/**
	 * Pendant +Z/(J3) button
	 * 
	 * This button jogs the active robot with reference to its active coordinate
	 * frame. In addition, this button is paired with -Z/(J3) in such a way that
	 * at most one of the two can be active at one time.
	 */
	public void joint3_pos() {
		updateRobotJogMotion(2, 1);
	}
	
	/**
	 * Pendant -XR/(J4) button
	 * 
	 * This button jogs the active robot with reference to its active coordinate
	 * frame. In addition, this button is paired with +XR/(J4) in such a way that
	 * at most one of the two can be active at one time.
	 */
	public void joint4_neg() {
		updateRobotJogMotion(3, -1);
	}
	
	/**
	 * Pendant +XR/(J4) button
	 * 
	 * This button jogs the active robot with reference to its active coordinate
	 * frame. In addition, this button is paired with -XR/(J4) in such a way that
	 * at most one of the two can be active at one time.
	 */
	public void joint4_pos() {
		updateRobotJogMotion(3, 1);
	}
	
	/**
	 * Pendant -YR/(J5) button
	 * 
	 * This button jogs the active robot with reference to its active coordinate
	 * frame. In addition, this button is paired with +YR/(J5) in such a way that
	 * at most one of the two can be active at one time.
	 */
	public void joint5_neg() {
		updateRobotJogMotion(4, -1);
	}
	
	/**
	 * Pendant +YR/(J5) button
	 * 
	 * This button jogs the active robot with reference to its active coordinate
	 * frame. In addition, this button is paired with -YR/(J5) in such a way that
	 * at most one of the two can be active at one time.
	 */
	public void joint5_pos() {
		updateRobotJogMotion(4, 1);
	}
	
	/**
	 * Pendant -ZR/(J6) button
	 * 
	 * This button jogs the active robot with reference to its active coordinate
	 * frame. In addition, this button is paired with +ZR/(J6) in such a way that
	 * at most one of the two can be active at one time.
	 */
	public void joint6_neg() {
		updateRobotJogMotion(5, -1);
	}
	
	/**
	 * Pendant +YR/(J5) button
	 * 
	 * This button jogs the active robot with reference to its active coordinate
	 * frame. In addition, this button is paired with -YR/(J5) in such a way that
	 * at most one of the two can be active at one time.
	 */
	public void joint6_pos() {
		updateRobotJogMotion(5, 1);
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
			if (mode.getType() == ScreenType.TYPE_NUM_ENTRY 
					|| mode.getType() == ScreenType.TYPE_POINT_ENTRY
					|| mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
				
				if (((key >= 'a' && key <= 'z') || (key >= 'A' && key <= 'Z') || (key >= '0' && key <= '9')
						|| key == '-' || key == '.' || key == '@' || key == '*' || key == '_')) {
					// Suppress other key events when entering text for the pendant
					characterInput(key);
					return;	
				}
			}
			
			// Pendant button shortcuts
			switch(keyCode) {
				case KeyEvent.VK_1:				f1(); break;
				case KeyEvent.VK_2:				f2(); break;
				case KeyEvent.VK_3:				f3(); break;
				case KeyEvent.VK_4:				f4(); break;
				case KeyEvent.VK_5:				f5(); break;
				case KeyEvent.VK_ENTER:			enter(); break;
				case KeyEvent.VK_BACK_SPACE:	bkspc(); break;
				case KeyEvent.VK_DOWN:			arrow_dn(); break;
				case KeyEvent.VK_LEFT:			arrow_lt(); break;
				case KeyEvent.VK_RIGHT:			arrow_rt(); break;
				case KeyEvent.VK_UP:			arrow_up(); break;
			}
		}
		
		// General key functions
		if (ctrlDown) {
			
			if (keyCode == KeyEvent.VK_C) {
				// Update the coordinate frame
				coordFrameTransition();
				updatePendantScreen();
				
			} else if (keyCode == KeyEvent.VK_D) {
				/* Debug output */
				Fields.debug("li=%d si=%d\n", contents.getLineIdx(), getSelectedIdx());
				/**
				if (activeRobot.inMotion()) {
					System.err.printf("Motion: %s\n",
							Arrays.toString(activeRobot.getJogMotion()));
				}
				/**
				updatePendantScreen();
				/**
				Fields.debug("Screen state: %s\n", screenStates.peek());
				/**
				if (mode == ScreenMode.NAV_PROG_INSTR && (contents.getColumnIdx() == 3
						|| contents.getColumnIdx() == 4)) {
					
					Instruction inst = getActiveInstruction();

					if (inst instanceof MotionInstruction) {
						MotionInstruction mInst = (MotionInstruction) inst;
						Fields.debug("\nUser frame: %d\nTool frame: %d\n",
								mInst.getUserFrame(), mInst.getToolFrame());
					}
				}
				/**
				Fields.debug(options.toString());
				/**/
				
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
				hold();
				float[] rot = { 0, 0, 0, 0, 0, 0 };
				activeRobot.releaseHeldObject();
				activeRobot.setJointAngles(rot);
				
			} else if (keyCode == KeyEvent.VK_R) {
				
				if (keyCodeMap.isKeyDown(KeyEvent.VK_ALT)) {
					// Toggle record state
					setRecord( !getRecord() );
					
				} else {
					// Rest motion fault
					reset();
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
			
		} else if (mode.getType() != ScreenType.TYPE_TEXT_ENTRY) {
			
			// Pendant button shortcuts
			switch(keyCode) {
			case KeyEvent.VK_SHIFT:		if (mode.getType() != ScreenType.TYPE_TEXT_ENTRY) 
											setShift(true); break;
			case KeyEvent.VK_U: 		joint1_neg(); break;
			case KeyEvent.VK_I:			joint1_pos(); break;
			case KeyEvent.VK_J: 		joint2_neg(); break;
			case KeyEvent.VK_K: 		joint2_pos(); break;
			case KeyEvent.VK_M: 		joint3_neg(); break;
			case KeyEvent.VK_COMMA:		joint3_pos(); break;
			case KeyEvent.VK_O: 		joint4_neg(); break;
			case KeyEvent.VK_P:			joint4_pos(); break;
			case KeyEvent.VK_L: 		joint5_neg(); break;
			case KeyEvent.VK_SEMICOLON: joint5_pos(); break;
			case KeyEvent.VK_PERIOD: 	joint6_neg(); break;
			case KeyEvent.VK_SLASH:		joint6_pos(); break;
			case KeyEvent.VK_MINUS:		spddn(); break;
			case KeyEvent.VK_EQUALS:	spdup(); break;
			case KeyEvent.VK_S:			rCamera.teachObjectToCamera(getActiveScenario()); break;
			}
			
		}
	}

	public void keyReleased() {
		keyCodeMap.keyReleased(keyCode, key);
		
		if (!keyCodeMap.isKeyDown(KeyEvent.VK_CONTROL) &&
				mode.getType() != ScreenType.TYPE_TEXT_ENTRY) {
			
			switch(keyCode) {
			case KeyEvent.VK_SHIFT: 	if (mode.getType() != ScreenType.TYPE_TEXT_ENTRY) 
											setShift(false); break;
			case KeyEvent.VK_U: 		joint1_neg(); break;
			case KeyEvent.VK_I:			joint1_pos(); break;
			case KeyEvent.VK_J: 		joint2_neg(); break;
			case KeyEvent.VK_K: 		joint2_pos(); break;
			case KeyEvent.VK_M: 		joint3_neg(); break;
			case KeyEvent.VK_COMMA:		joint3_pos(); break;
			case KeyEvent.VK_O: 		joint4_neg(); break;
			case KeyEvent.VK_P:			joint4_pos(); break;
			case KeyEvent.VK_L: 		joint5_neg(); break;
			case KeyEvent.VK_SEMICOLON: joint5_pos(); break;
			case KeyEvent.VK_PERIOD: 	joint6_neg(); break;
			case KeyEvent.VK_SLASH:		joint6_pos(); break;
			}
		}
	}

	/**
	 * Pulls off the current screen state from the screen state stack and loads
	 * the previous screen state as the active screen state.
	 * 
	 * @return	If a previous screen exists
	 */
	private boolean lastScreen() {
		ScreenState cur = screenStates.peek();
		
		if (cur.mode != ScreenMode.DEFAULT) {
			RoboticArm r = activeRobot;
			screenStates.pop();
			cur = screenStates.peek();
			
			Fields.debug("\n%s <= %s\n", cur, mode);
			
			// Update mode and menu indices
			mode = cur.mode;
			
			contents.clear();
			contents.setLineIdx(cur.conLnIdx);
			contents.setColumnIdx(cur.conColIdx);
			contents.setRenderStart(cur.conRenIdx);
			
			options.clear();
			options.setLineIdx(cur.optLnIdx);
			options.setRenderStart(cur.optRenIdx);
			
			switch (mode) {
			case NAV_PROG_INSTR:
				DisplayLine active = contents.getActiveLine();
				updateInstList();
				// Update the active robot's active instruction index
				if (active != null && active.getItemIdx() < getActiveProg().size()) {
					setActiveInstIdx(active.getItemIdx());
				}
				
				break;
			case ACTIVE_FRAMES:
				String idxTxt;
				
				if (contents.getLineIdx() == 0) {
					idxTxt = Integer.toString(r.getActiveToolIdx() + 1);
					
				} else {
					idxTxt = Integer.toString(r.getActiveUserIdx() + 1);
				}
				
				workingText = new StringBuilder(idxTxt);
				break;
			default:
				workingText = new StringBuilder();
			}
			
			updatePendantScreen();
			return true;
		}
		
		return false;
	}

	/**
	 * Camera L button
	 * 
	 * Sets the camera facing down the negative x-axis of the world coordinate
	 * frame.
	 */
	public void LeftView() {
		// Left view
		camera.reset();
		camera.setRotation(0f, HALF_PI, 0f);
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
	private ArrayList<DisplayLine> loadEEToolTipDefaults(RoboticArm robot) {
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
	 * @param f
	 * @return
	 */
	public ArrayList<DisplayLine> loadFrameDirectEntry(Frame f) {
		ArrayList<DisplayLine> lines = new ArrayList<>();
		
		if (f instanceof Frame) {
			String[][] entries = f.directEntryStringArray();
	
			for (int idx = 0; idx < entries.length; ++idx) {
				String[] line = new String[entries[idx][1].length() + 1];
				line[0] = entries[idx][0];
				// Give each character in the value String it own column
				for (int sdx = 0; sdx < entries[idx][1].length(); ++sdx) {
					line[sdx + 1] = Character.toString(entries[idx][1].charAt(sdx));
				}
	
				lines.add(new DisplayLine(idx, 0, line));
			}
			
		} else {
			lines.add(new DisplayLine(-1, 0, "Null frame"));
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
	public ArrayList<DisplayLine> loadInstructions(Program p) {
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

		DisplayLine endl = new DisplayLine(size);
		endl.add("[End]");

		instruct_list.add(endl);

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

	public void loadMacros() {
		for (int i = 0; i < macros.size(); i += 1) {
			String[] strArray = macros.get(i).toStringArray();
			contents.addLine(Integer.toString(i + 1), strArray[0], strArray[1], strArray[2]);
		}
	}

	public void loadManualFunct() {
		int macroNum = 0;

		for (int i = 0; i < macros.size(); i += 1) {
			if (macros.get(i).isManual()) {
				macroNum += 1;
				String manFunct = macros.get(i).toString();
				contents.addLine(macroNum + " " + manFunct);
			}
		}
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
	 * @param pt
	 * @param isCartesian
	 * @return
	 */
	public ArrayList<DisplayLine> loadPosition(Point pt, boolean isCartesian) {
		ArrayList<DisplayLine> lines = new ArrayList<>();
		
		if (pt == null) {
			lines.add(new DisplayLine(-1, 0, "Null point") );

		} else {
			String[][] entries;

			if (isCartesian) {
				// List Cartesian values
				entries = pt.toCartesianStringArray();

			} else {
				// List joint angles
				entries = pt.toJointStringArray();
			}

			for (int idx = 0; idx < entries.length; ++idx) {
				String[] line = new String[entries[idx][1].length() + 1];
				line[0] = entries[idx][0];
				// Give each character in the value String it own column
				for (int sdx = 0; sdx < entries[idx][1].length(); ++sdx) {
					line[sdx + 1] = Character.toString(entries[idx][1].charAt(sdx));
				}

				lines.add(new DisplayLine(idx, 0, line));
			}
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
	 * @return
	 */
	public ArrayList<DisplayLine> loadPrograms() {
		return loadPrograms( activeRobot );
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
	 * Sets the given screen mode as the active mode. In the process thereof,
	 * the contents and options menus are updated and redrawn based on the new
	 * active screen and the previous screen. In addition, the active screen
	 * state is pushed onto the stack.
	 * 
	 * @param m	The new active screen mode
	 */
	private void loadScreen(ScreenMode m) {
		loadScreen(m, screenStates.peek());
	}

	/**
	 * Sets the given screen mode m as the active screen. Current defines the
	 * state of the current active screen. In the process of setting the active
	 * screen, the contents and options menus are updated and redrawn based on
	 * the new active screen and the previous screen. In addition, the active
	 * screen state is pushed onto the stack.
	 * 
	 * @param m			The new active screen
	 * @param current	The current active screen state
	 */
	private void loadScreen(ScreenMode m, ScreenState current) {
		Fields.debug("\n%s => %s\n", current.mode, m);
			
		mode = m;
		workingText = new StringBuilder();
		contents.reset();
		options.reset();
		
		switch (mode) {
		case NAV_IOREG:
			contents.setColumnIdx(1);
			break;
			// Frames
		case ACTIVE_FRAMES:
			contents.setColumnIdx(1);
			workingText = new StringBuilder(Integer.toString(activeRobot.getActiveToolIdx() + 1));
			break;
		case TEACH_3PT_USER:
		case TEACH_4PT:
		case TEACH_3PT_TOOL:
		case TEACH_6PT:
		case TFRAME_DETAIL:
		case UFRAME_DETAIL:
			contents.setLineIdx(-1);
			contents.setColumnIdx(-1);
			break;
		case NAV_PROGRAMS:
			if (getActiveProg() == null) {
				setActiveProgIdx(0);
				setActiveInstIdx(0);
			}
			
			contents.setLineIdx( getActiveProgIdx() );
			break;
		case NAV_PROG_INSTR:
			progCallStack.clear();
			setActiveInstIdx(0);
			// Reinitialize the map of motion instructions
			updateInstList();
			
			break;
		case PROG_CREATE:
			contents.setLineIdx(1);
			break;
		case PROG_RENAME:
			setActiveProgIdx(current.conLnIdx);
			contents.setLineIdx(1);
			workingText = new StringBuilder(getActiveProg().getName());
			break;
		case TFRAME_RENAME:
			contents.setLineIdx(1);
			workingText = new StringBuilder(
					activeRobot.getToolFrame(curFrameIdx).getName()
			);
			break;
		case UFRAME_RENAME:
			contents.setLineIdx(1);
			workingText = new StringBuilder(
					activeRobot.getUserFrame(curFrameIdx).getName()
			);
			break;
		case PROG_COPY:
			setActiveProgIdx(current.conLnIdx);
			contents.setLineIdx(1);
			break;
		case SET_CALL_PROG:
			contents.setLineIdx( current.conLnIdx );
			contents.setRenderStart( current.conRenIdx );
			contents.setColumnIdx( current.conColIdx );
			break;
		case SELECT_INSTR_INSERT:
		case SELECT_JMP_LBL:
		case SELECT_REG_STMT:
		case SELECT_COND_STMT:
		case SELECT_PASTE_OPT:
		case SET_IF_STMT_ACT:
		case SET_SELECT_STMT_ACT:
		case SET_SELECT_STMT_ARG:
		case SET_EXPR_ARG:
		case SET_BOOL_EXPR_ARG:
		case SET_EXPR_OP:
			contents.setLineIdx( current.conLnIdx );
			contents.setColumnIdx( current.conColIdx );
			contents.setRenderStart(  current.conRenIdx );
			break;
		case SELECT_IO_INSTR_REG:
			contents.setLineIdx( current.conLnIdx );
			contents.setColumnIdx( current.conColIdx );
			contents.setRenderStart(  current.conRenIdx );
			options.setColumnIdx(1);
			break;
		case SET_MINST_OFF_TYPE:
		case SET_MINST_OFFIDX:
		case INPUT_DREG_IDX:
		case INPUT_IOREG_IDX:
		case INPUT_PREG_IDX1:
		case INPUT_PREG_IDX2:
		case INPUT_CONST:
			contents.setLineIdx( current.conLnIdx );
			contents.setColumnIdx( current.conColIdx );
			contents.setRenderStart(  current.conRenIdx );
			break;
		case SET_IO_INSTR_IDX:
		case SET_JUMP_TGT:
		case SET_LBL_NUM:
			contents.setLineIdx( current.conLnIdx );
			contents.setColumnIdx( current.conColIdx );
			contents.setRenderStart(  current.conRenIdx );
			break;
		case SET_MINST_TYPE:
			MotionInstruction mInst = (MotionInstruction) getActiveInstruction();
			
			int motionType = mInst.getMotionType();
			
			if (motionType == Fields.MTYPE_LINEAR) {
				options.setLineIdx(1);
				
			} else if (motionType == Fields.MTYPE_CIRCULAR) {
				options.setLineIdx(2);
			}
			
			contents.setLineIdx( current.conLnIdx );
			contents.setColumnIdx( current.conColIdx );
			contents.setRenderStart(  current.conRenIdx );

			break;
		case SET_MINST_SPD:
			mInst = (MotionInstruction) getActiveInstruction();	
			float instSpd = mInst.getSpdMod();
			
			if (mInst.getMotionType() == Fields.MTYPE_JOINT) {
				instSpd *= 100f;
				
			} else {
				instSpd *= RoboticArm.motorSpeed;
			}
			
			workingText = new StringBuilder(Integer.toString((int)instSpd));
			
			contents.setLineIdx( current.conLnIdx );
			contents.setColumnIdx( current.conColIdx );
			contents.setRenderStart(  current.conRenIdx );
			break;
		case SET_MINST_REG_TYPE:
			contents.setLineIdx( current.conLnIdx );
			contents.setColumnIdx( current.conColIdx );
			contents.setRenderStart(  current.conRenIdx );
			break;
		case SET_MINST_CREG_TYPE:
			contents.setLineIdx( current.conLnIdx );
			contents.setColumnIdx( current.conColIdx );
			contents.setRenderStart(  current.conRenIdx );
			break;
		case SET_MINST_IDX:
			workingText = new StringBuilder("");
			
			contents.setLineIdx( current.conLnIdx );
			contents.setColumnIdx( current.conColIdx );
			contents.setRenderStart(  current.conRenIdx );
			break;
		case SET_MINST_TERM:
			mInst = (MotionInstruction) getActiveInstruction();
			int term = mInst.getTermination();
			
			workingText = new StringBuilder(Integer.toString(term));
			
			contents.setLineIdx( current.conLnIdx );
			contents.setColumnIdx( current.conColIdx );
			contents.setRenderStart(  current.conRenIdx );
			break;
		case SET_FRAME_INSTR_IDX:
		case SET_SELECT_ARGVAL:
		case SET_REG_EXPR_IDX1:
		case SET_REG_EXPR_IDX2:
			contents.setLineIdx( current.conLnIdx );
			contents.setColumnIdx( current.conColIdx );
			contents.setRenderStart(  current.conRenIdx );
			break;
		case SET_IO_INSTR_STATE:
		case SET_FRM_INSTR_TYPE:
		case SET_REG_EXPR_TYPE:
			contents.setLineIdx( current.conLnIdx );
			contents.setColumnIdx( current.conColIdx );
			contents.setRenderStart(  current.conRenIdx );
			break;
		case EDIT_PROG_POS:
			contents.setLineIdx( current.conLnIdx );
			contents.setColumnIdx( current.conColIdx );
			contents.setRenderStart(  current.conRenIdx );
			Program prog = getActiveProg();
			PosMotionInst pMInst = (PosMotionInst) getActiveInstruction();
			Point pt = prog.getPosition(pMInst.getPosIdx());
			
			// Initialize the point if it is null
			if (pt == null) {
				pt = new Point();
				prog.setPosition(pMInst.getPosIdx(), pt);
			}
			
			boolean isCartesian = pMInst.getMotionType() != Fields.MTYPE_JOINT;
			contents.setLines( loadPosition(pt, isCartesian));
			break;
		case SELECT_INSTR_DELETE:
		case SELECT_COMMENT:
		case SELECT_CUT_COPY:
			prog = getActiveProg();
			int size = prog.getNumOfInst() - 1;
			setActiveInstIdx(max(0, min(getActiveInstIdx(), size)));
			break;

			// Macros
		case NAV_MACROS:
			contents.setLineIdx( current.conLnIdx );
			break;
		case NAV_PREGS:
			options.setLineIdx(-1);
			break;
		case SET_DEF_TOOLTIP:
			contents.setLineIdx(-1);
			break;
		case DIRECT_ENTRY_TOOL:
			contents.setColumnIdx(1);
			Frame tool = activeRobot.getToolFrame(curFrameIdx);
			contents.setLines( loadFrameDirectEntry(tool) );
			break;
		case DIRECT_ENTRY_USER:
			contents.setColumnIdx(1);
			Frame user = activeRobot.getUserFrame(curFrameIdx);
			contents.setLines( loadFrameDirectEntry(user) );
			break;
		case SWAP_PT_TYPE:
			contents.setLineIdx( current.conLnIdx );
			contents.setColumnIdx( current.conColIdx );
			contents.setRenderStart(  current.conRenIdx );
			break;
		case CP_DREG_COM:
		case CP_DREG_VAL:
			contents.setLineIdx( current.conLnIdx );
			contents.setColumnIdx( current.conColIdx );
			contents.setRenderStart(  current.conRenIdx );
			options.setLineIdx(1);
			workingText = new StringBuilder((active_index + 1));
			break;
		case CP_PREG_COM:
		case CP_PREG_PT:
			contents.setLineIdx( current.conLnIdx );
			contents.setColumnIdx( current.conColIdx );
			contents.setRenderStart(  current.conRenIdx );
			options.setLineIdx(1);
			workingText = new StringBuilder((active_index + 1));
			break;
		case EDIT_DREG_COM:
			contents.setLineIdx(1);

			String c = activeRobot.getDReg(active_index).comment;
			if (c != null && c.length() > 0) {
				workingText = new StringBuilder(c);
			} else {
				workingText = new StringBuilder("\0");
			}

			break;
		case EDIT_PREG_COM:
			contents.setLineIdx(1);

			c = activeRobot.getPReg(active_index).comment;
			if (c != null && c.length() > 0) {
				workingText = new StringBuilder(c);
			} else {
				workingText = new StringBuilder("\0");
			}

			println(workingText.length());
			break;
		case EDIT_DREG_VAL:
			contents.setLineIdx( current.conLnIdx );
			contents.setColumnIdx( current.conColIdx );
			contents.setRenderStart(  current.conRenIdx );
			// Bring up float input menu
			Float val = activeRobot.getDReg(active_index).value;
			if (val != null) {
				workingText = new StringBuilder(val.toString());

			}
			break;
		case EDIT_PREG:
			ArrayList<DisplayLine> limbo;
			PositionRegister pReg = activeRobot.getPReg(active_index);
			// Load the position associated with active position register
			if (pReg.point == null) {
				// Initialize an empty position register
				limbo = loadPosition(activeRobot.getDefaultPoint(), pReg.isCartesian);

			} else {
				limbo = loadPosition(pReg.point, pReg.isCartesian);
			}
			
			contents.setLines(limbo);
			contents.setColumnIdx(1);
			break;
		default:
			break;
		}
		
		pushActiveScreen();
		updatePendantScreen();
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
	
	/**
	 * This method loads text to screen in such a way as to allow the user to
	 * input an arbitrary character string consisting of letters (a-z upper and
	 * lower case) and/ or special characters (_, @, *, .) via the function row,
	 * as well as numbers via the number pad. Strings are limited to 16
	 * characters and can be used to name new routines, as well as set remark
	 * fields for frames and instructions.
	 */
	public DisplayLine loadTextInput(String txt) {
		contents.addLine("\0");

		DisplayLine line = new DisplayLine();
		// Give each letter in the name a separate column
		for (int idx = 0; idx < txt.length() && idx < TEXT_ENTRY_LEN; idx += 1) {
			line.add( Character.toString(txt.charAt(idx)) );
		}

		return line;
	}

	/**
	 * Pendant MENU button
	 * 
	 * A list of miscellaneous sub menus (frames, marcos, I/O registers).
	 */
	public void menu() {
		nextScreen(ScreenMode.NAV_MAIN_MENU);
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
			
			float[][] camRMat = getOrientation().getDataF();
			
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
		if (UI != null && UI.isMouseOverADropdownList()) {
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

	/**
	 * Move to Current button in the edit window
	 * 
	 * Updates the current position and orientation of a selected object to the
	 * inputed values in the edit window.
	 */
	public void MoveToCur() {
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
	public void MoveToDef() {
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
	 * Pendant MVMU button
	 * 
	 * A button used for macro binding
	 */
	public void mvmu() {
		if (getSU_macro_bindings()[2] != null && isShift()) {
			execute(getSU_macro_bindings()[2]);
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
			
			if (getSelectedLine() > 0) {
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
		if (!screenStates.isEmpty()) {
			ScreenState cur = screenStates.peek();
			// Update the current screen state
			cur.conLnIdx = contents.getLineIdx();
			cur.conColIdx = contents.getColumnIdx();
			cur.conRenIdx = contents.getRenderStart();
			cur.optLnIdx = options.getLineIdx();
			cur.optRenIdx = options.getRenderStart();
		}
		
		// Load the new screen
		loadScreen(nextScreen);
	}

	/**
	 * Pendant '0' button
	 * 
	 * Appends a '0' character to input for the text, number, and point entry
	 * menus.
	 */
	public void num0() {
		characterInput('0');
	}

	/**
	 * Pendant '1' button
	 * 
	 * Appends a '1' character to input for the text, number, and point entry
	 * menus.
	 */
	public void num1() {
		characterInput('1');
	}
	
	/**
	 * Pendant '2' button
	 * 
	 * Appends a '2' character to input for the text, number, and point entry
	 * menus.
	 */
	public void num2() {
		characterInput('2');
	}
	
	/**
	 * Pendant '3' button
	 * 
	 * Appends a '3' character to input for the text, number, and point entry
	 * menus.
	 */
	public void num3() {
		characterInput('3');
	}
	
	/**
	 * Pendant '4' button
	 * 
	 * Appends a '4' character to input for the text, number, and point entry
	 * menus.
	 */
	public void num4() {
		characterInput('4');
	}
	
	/**
	 * Pendant '5' button
	 * 
	 * Appends a '5' character to input for the text, number, and point entry
	 * menus.
	 */
	public void num5() {
		characterInput('5');
	}
	
	/**
	 * Pendant '6' button
	 * 
	 * Appends a '6' character to input for the text, number, and point entry
	 * menus.
	 */
	public void num6() {
		characterInput('6');
	}
	
	/**
	 * Pendant '7' button
	 * 
	 * Appends a '7' character to input for the text, number, and point entry
	 * menus.
	 */
	public void num7() {
		characterInput('7');
	}
	
	/**
	 * Pendant '8' button
	 * 
	 * Appends a '8' character to input for the text, number, and point entry
	 * menus.
	 */
	public void num8() {
		characterInput('8');
	}
	
	/**
	 * Pendant '9' button
	 * 
	 * Appends a '9' character to input for the text, number, and point entry
	 * menus.
	 */
	public void num9() {
		characterInput('9');
	}
	
	public Point parsePosFromContents(boolean isCartesian) {
		// Obtain point inputs from UI display text
		float[] inputs = new float[6];

		try {
			for (int idx = 0; idx < inputs.length; ++idx) {
				DisplayLine value = contents.get(idx);
				String inputStr = new String();
				int sdx;

				/*
				 * Combine all columns related to the value, ignoring the prefix
				 * and last column
				 */
				for (sdx = 1; sdx < (value.size() - 1); ++sdx) {
					inputStr += value.get(sdx);
				}

				// Ignore any trailing blank spaces
				if (!value.get(sdx).equals("\0")) {
					inputStr += value.get(sdx);
				}

				inputs[idx] = Float.parseFloat(inputStr);
				// Bring the input values with the range [-9999, 9999]
				inputs[idx] = max(-9999f, min(inputs[idx], 9999f));
			}

			if (isCartesian) {
				PVector position = RMath.vFromWorld(new PVector(inputs[0], inputs[1], inputs[2]));
				PVector wpr = new PVector(inputs[3], inputs[4], inputs[5]);
				// Convert the angles from degrees to radians, then convert from
				// World to Native frame, and finally convert to a quaternion
				RQuaternion orientation = RMath.wEulerToNQuat(wpr);

				// Use default the Robot's joint angles for computing inverse
				// kinematics
				float[] defJointAngles = new float[] { 0f, 0f, 0f, 0f, 0f, 0f };
				float[] jointAngles = RMath.inverseKinematics(activeRobot, defJointAngles, position,
						orientation);
				
				if (jointAngles == null) {
					// Inverse kinematics failed
					return new Point(position, orientation, defJointAngles);
				}
				
				return new Point(position, orientation, jointAngles);
			}
			
			// Bring angles within range: (0, TWO_PI)
			for (int idx = 0; idx < inputs.length; ++idx) {
				inputs[idx] = RMath.mod2PI(inputs[idx] * DEG_TO_RAD);
			}
			
			return activeRobot.getToolTipNative(inputs);

		} catch (NumberFormatException NFEx) {
			// Invalid input
			println("Values must be real numbers!");
			return null;
		}
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
	 * Pendant '.' buttom
	 * 
	 * Appends a '.' character to input in the text, number, and point entry
	 * menus.
	 */
	public void period() {
		characterInput('.');
	}

	/**
	 * Pendant POSN button
	 * 
	 * A button used for marcos binding.
	 */
	public void posn() {
		if (getSU_macro_bindings()[5] != null && isShift()) {
			execute(getSU_macro_bindings()[5]);
		}
	}
	
	/**
	 * Pendant PREV button
	 * 
	 * Transitions to the previous menu screen, if one exists.
	 */
	public void prev() {
		lastScreen();
	}

	/**
	 * Restores all parts in the current scenario to their default position and
	 * orientation.
	 */
	public void ResDefs() {

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
	 * Pendant RESET button
	 * 
	 * Resets the motion fault flag for the active robot, when the motion fault
	 * flag is set on.
	 */
	public void reset() {
		if (isShift()) {
			hold();
			// Reset motion fault for the active robot
			activeRobot.setMotionFault(false);
		}
	}
	
	/**
	 * Clears the screen state stack and sets the default screen as the active
	 * screen.
	 */
	public void resetStack() {
		// Stop a program from executing when transition screens
		screenStates.clear();

		mode = ScreenMode.DEFAULT;
		pushScreen(mode, -1, -1, 0, -1, 0);
	}

	/**
	 * Camera R button
	 * 
	 * Sets the camera to point down the positive x-axis of the world
	 * coordinate frame.
	 */
	public void RightView() {
		// Right view
		camera.reset();
		camera.setRotation(0, 3f * HALF_PI, 0f);
	}
	
	/**
	 * The scenario window confirmation button
	 * 
	 * Deals with the confirm functionality of the scenario window (i.e.
	 * setting the new name of a scenario, creating a new scenario,
	 * loading an inactive scenario).
	 */
	public void SConfirm() {
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
	public void select() {
		nextScreen(ScreenMode.NAV_PROGRAMS);
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
			hold();

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

	public void setSU_macro_bindings(Macro[] sU_macro_bindings) {
		SU_macro_bindings = sU_macro_bindings;
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
		letterStates = new int[] { 0, 0, 0, 0, 0 };
		workingText = new StringBuilder();
		
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
			
			screenStates = new Stack<>();
			mode = ScreenMode.DEFAULT;
			pushScreen(ScreenMode.DEFAULT, -1, -1, 0, -1, 0);
			
			contents = new MenuScroll("cont", ITEMS_TO_SHOW, 10, 20);
			options = new MenuScroll("opt", 3, 10, 180);
			
			progExecState = new ProgExecution();
			progCallStack = new Stack<>();
			
			tracePts = new LinkedList<PVector>();
			
			setManager(new WGUI(this, buttonImages));
			
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
	 * Pendant SETUP button
	 * 
	 * A button used for binding macros.
	 */
	public void SETUP() {
		if (getSU_macro_bindings()[3] != null && isShift()) {
			execute(getSU_macro_bindings()[3]);
		}
	}

	/**
	 * Pendant SHIFT button
	 * 
	 * Toggles the shift state on or off. Shift is required to be on for
	 * anything involving robot motion or point recording.
	 */
	public void shift() {
		setShift(!shift);
	}

	/**
	 * Pendant +% button
	 * 
	 * Increases the robot's jog speed.
	 */
	public void spddn() {
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
	public void spdup() {
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
	public void status() {
		if (getSU_macro_bindings()[4] != null && isShift()) {
			execute(getSU_macro_bindings()[4]);
		}
	}

	/**
	 * Pendant STEP button
	 * 
	 * Toggles the step state on or off. When step is on, then instructions
	 * will be executed one at a time as opposed to all at once.
	 */
	public void step() {
		setStep(!isStep());
	}


	/**
	 * Removes the current screen from the screen state stack and loads the
	 * given screen mode.
	 * 
	 * @param nextScreen	The new screen mode
	 */
	private void switchScreen(ScreenMode nextScreen) {
		screenStates.pop();
		// Load the new screen
		loadScreen(nextScreen);
	}
	
	public void TeachCamObj() {
		if(activeScenario != null) {
			rCamera.teachObjectToCamera(activeScenario);
		}
		
		UI.updateCameraListContents();
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
	 * HIDE/SHOW OBBBS button in the miscellaneous window
	 * 
	 * Toggles bounding box display on or off.
	 */
	public void ToggleOBBs() {
		UI.updateUIContentPositions();
	}
	
	/**
	 * ADD/REMOVE ROBOT button in the miscellaneous window
	 * 
	 * Toggles the second Robot on or off.
	 */
	public void ToggleRobot() {
		UI.toggleSecondRobot();
		// Reset the active robot to the first if the second robot is removed
		if (activeRobot != ROBOTS.get(0)) {
			activeRobot = ROBOTS.get(0);
		}

		UI.updateUIContentPositions();
		updatePendantScreen();
	}
	
	public void ToggleCamera() {
		camEnable = UI.toggleCamera();
				
		UI.updateUIContentPositions();
		updatePendantScreen();
	}
	
	/**
	 * ENABLE/DISABLE TRACE button in miscellaneous window
	 * 
	 * Toggles the robot tool tip trace function on or off.
	 */
	public void ToggleTrace() {
		UI.updateUIContentPositions();
		
		if (!traceEnabled()) {
			// Empty trace when it is disabled
			tracePts.clear();
		}
	}
	
	/**
	 * Pendant TOOl1 button
	 * 
	 * A button used for binding marcos.
	 */
	public void tool1() {
		if (getSU_macro_bindings()[0] != null && isShift()) {
			execute(getSU_macro_bindings()[0]);
		}
	}

	/**
	 * Pendant TOOl2 button
	 * 
	 * A button used for binding marcos.
	 */
	public void tool2() {
		if (getSU_macro_bindings()[1] != null && isShift()) {
			execute(getSU_macro_bindings()[1]);
		}
	}

	/**
	 * Camera T button
	 * 
	 * Sets the camera to point down the negative z-axis of the world
	 * coordinate frame.
	 */
	public void TopView() {
		// Top view
		camera.reset();
		camera.setRotation(3f * HALF_PI, 0f, 0f);
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
	
	/**
	 * Updates the index display in the Active Frames menu based on the current
	 * value of workingText
	 */
	public void updateActiveFramesDisplay() {
		// Attempt to parse the inputed integer value
		try {
			int frameIdx = Integer.parseInt(workingText.toString()) - 1;

			if (frameIdx >= -1 && frameIdx < 10) {
				// Update the appropriate active Frame index
				if (contents.getLineIdx() == 0) {
					activeRobot.setActiveToolFrame(frameIdx);
				} else {
					activeRobot.setActiveUserFrame(frameIdx);
				}
			}

		} catch (NumberFormatException NFEx) {
			// Non-integer value
		}
		// Update display
		if (contents.getLineIdx() == 0) {
			workingText = new StringBuilder(Integer.toString(activeRobot.getActiveToolIdx() + 1));

		} else {
			workingText = new StringBuilder(Integer.toString(activeRobot.getActiveUserIdx() + 1));
		}

		contents.getActiveLine().set(contents.getColumnIdx(), workingText.toString());
		updatePendantScreen();
	}
	
	public void updateContents() {
		RoboticArm r = activeRobot;
		ArrayList<DisplayLine> prevContents = contents.copyContents();
		contents.clear();

		switch (mode) {
		// Main menu and submenus
		case NAV_MAIN_MENU:
			contents.addLine("1 Frames");
			contents.addLine("2 Macros");
			contents.addLine("3 Manual Fncts");
			contents.addLine("4 I/O Registers");
			break;
		// Program list navigation/ edit
		case NAV_PROGRAMS:
		case SET_MACRO_PROG:
			contents.setLines( loadPrograms(r) );
			break;
			
		case PROG_CREATE:
		case PROG_RENAME:
		case PROG_COPY:
		case TFRAME_RENAME:
		case UFRAME_RENAME:
			DisplayLine line = loadTextInput(workingText.toString());
			contents.addLine(line);
			break;

		// View instructions
		case CONFIRM_INSERT:
		case CONFIRM_RENUM:
		case FIND_REPL:
		case NAV_PROG_INSTR:
		case SELECT_INSTR_DELETE:
		case SELECT_COMMENT:
		case SELECT_CUT_COPY:
		case SELECT_PASTE_OPT:
		case SET_MINST_TYPE:
		case SET_MINST_REG_TYPE:
		case SET_MINST_CREG_TYPE:
		case SET_MINST_IDX:
		case SET_MINST_CIDX:
		case SET_MINST_SPD:
		case SET_MINST_TERM:
		case SET_MINST_OFF_TYPE:
		case SET_MINST_OFFIDX:
		case SET_MINST_WO:
		case SET_IO_INSTR_STATE:
		case SET_IO_INSTR_IDX:
		case SET_FRM_INSTR_TYPE:
		case SET_FRAME_INSTR_IDX:
		case SET_REG_EXPR_TYPE:
		case SET_REG_EXPR_IDX1:
		case SET_REG_EXPR_IDX2:
		case SET_IF_STMT_ACT:
		case SET_SELECT_STMT_ARG:
		case SET_SELECT_ARGVAL:
		case SET_SELECT_STMT_ACT:
		case SET_EXPR_ARG:
		case SET_BOOL_EXPR_ARG:
		case SET_EXPR_OP:
		case INPUT_DREG_IDX:
		case INPUT_IOREG_IDX:
		case INPUT_PREG_IDX1:
		case INPUT_PREG_IDX2:
		case INPUT_CONST:
		case SET_BOOL_CONST:
		case SET_LBL_NUM:
		case SET_JUMP_TGT:
		case SET_CALL_PROG:
			contents.setLines( loadInstructions(getActiveProg()) );
			break;
		case NAV_DATA:
			contents.addLine("1. Data Registers");
			contents.addLine("2. Position Registers");
			break;
		case ACTIVE_FRAMES:
			/* workingText corresponds to the active row's index display */
			if (contents.getLineIdx() == 0) {
				contents.addLine("Tool: ", workingText.toString());
				contents.addLine("User: ", Integer.toString(r.getActiveUserIdx() + 1));

			} else {
				contents.addLine("Tool: ", Integer.toString(r.getActiveToolIdx() + 1));
				contents.addLine("User: ", workingText.toString());
			}
			break;

			// View frame details
		case NAV_TOOL_FRAMES:
			contents.setLines( loadFrames(activeRobot, CoordFrame.TOOL) );
			break;
		case NAV_USER_FRAMES:
			contents.setLines( loadFrames(activeRobot, CoordFrame.USER) );
			break;
			// View frame details
		case SET_DEF_TOOLTIP:
		case TFRAME_DETAIL:
		case TEACH_3PT_TOOL:
		case TEACH_6PT:
			contents.setLines(
				loadFrameDetail(r, CoordFrame.TOOL, curFrameIdx)
			);
			break;
		case UFRAME_DETAIL:
		case TEACH_3PT_USER:
		case TEACH_4PT:
			contents.setLines(
				loadFrameDetail(r, CoordFrame.USER, curFrameIdx)
			);
			break;
		case NAV_DREGS:
		case EDIT_DREG_VAL:
		case CP_DREG_COM:
		case CP_DREG_VAL:
			contents.setLines( loadDataRegisters(r) );
			break;
		case NAV_PREGS:
		case CP_PREG_COM:
		case CP_PREG_PT:
		case SWAP_PT_TYPE:
			contents.setLines( loadPositionRegisters(r) );
			break;
		case NAV_IOREG:
			contents.setLines( loadIORegNav(activeRobot) );
			break;
		// Position entry menus
		case EDIT_PROG_POS:
		case EDIT_PREG:
		case DIRECT_ENTRY_TOOL:
		case DIRECT_ENTRY_USER:
			contents.setLines(prevContents);
			break;
		case NAV_MACROS:
		case SET_MACRO_TYPE:
		case SET_MACRO_BINDING:
			loadMacros();
			break;
		case NAV_MF_MACROS:
			loadManualFunct();
			break;
		case EDIT_DREG_COM:
		case EDIT_PREG_COM:
			line = loadTextInput(workingText.toString());
			contents.addLine(line);
			break;
		default:
			break;
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

	public void updateOptions() {
		options.clear();
		
		Instruction inst = getActiveInstruction();

		switch (mode) {
		case NAV_PROG_INSTR:
			Program p = getActiveProg();
			int selectedReg = selectedMInstRegState();
			
			if (inst instanceof PosMotionInst && selectedReg > 0) {
				// Show the position associated with the active motion
				// instruction
				PosMotionInst mInst = (PosMotionInst) inst;
				boolean isCart = false;
				Point pt = null;
				
				if (selectedReg == 6) {
					PositionRegister pReg = activeRobot.getPReg(mInst.getOffsetIdx());
					
					if (pReg != null) {
						isCart = pReg.isCartesian;
						pt = pReg.point;
					}
					
				} else if (selectedReg == 4 || selectedReg == 3) {
					isCart = true;
					pt = activeRobot.getCPosition(mInst, p);
					
				} else if (selectedReg == 1 || selectedReg == 2) {
					isCart = mInst.getMotionType() != Fields.MTYPE_JOINT;
					pt = activeRobot.getPosition(mInst, p);
				}

				if (pt != null) {
					String[] pregEntry = pt.toLineStringArray(isCart);

					for (String line : pregEntry) {
						options.addLine(line);
					}
					
				} else {
					options.addLine("Uninitialized");
				}
			}

			break;
		case CONFIRM_PROG_DELETE:
			options.addLine("Delete selected program?");
			break;

			// Instruction options
		case NAV_INSTR_MENU:
			options.addLine("1 Undo");
			options.addLine("2 Insert");
			options.addLine("3 Delete");
			options.addLine("4 Cut/ Copy");
			options.addLine("5 Paste");
			options.addLine("6 Find/ Replace");
			options.addLine("7 Renumber");
			options.addLine("8 Comment");
			break;
		case CONFIRM_INSERT:
			options.addLine("Enter number of lines to insert:");
			options.addLine("\0" + workingText);
			break;
		case SELECT_INSTR_DELETE:
			options.addLine("Select lines to delete (ENTER).");
			break;
		case SELECT_CUT_COPY:
			options.addLine("Select lines to cut/ copy (ENTER).");
			break;
		case SELECT_PASTE_OPT:
			options.addLine("1 Logic");
			options.addLine("2 Position");
			options.addLine("3 Pos ID");
			options.addLine("4 R Logic");
			options.addLine("5 R Position");
			options.addLine("6 R Pos ID");
			options.addLine("7 RM Pos ID");
			break;
		case FIND_REPL:
			options.addLine("Enter text to search for:");
			options.addLine("\0" + workingText);
			break;
		case CONFIRM_RENUM:
			options.addLine("Renumber program positions?");
			break;
		case SELECT_COMMENT:
			options.addLine("Select lines to comment/uncomment.");
			break;

		case SET_MINST_TYPE:
			options.addLine("1.JOINT");
			options.addLine("2.LINEAR");
			options.addLine("3.CIRCULAR");
			break;
		case SET_MINST_REG_TYPE:
			options.addLine("1.LOCAL(P)");
			options.addLine("2.GLOBAL(PR)");
			options.addLine("3.CAM OBJECT(OBJ)");
			break;
		case SET_MINST_CREG_TYPE:
			options.addLine("1.LOCAL(P)");
			options.addLine("2.GLOBAL(PR)");
			break;
		case SET_MINST_IDX:
		case SET_MINST_CIDX:
			options.addLine("Enter desired position/ register:");
			options.addLine("\0" + workingText);
			break;
		case SET_MINST_WO:
			CamMoveToObject castIns = (CamMoveToObject)inst;
			Scenario s = castIns.getScene();
			
			if (s != null && s.size() > 0) {
				options.addLine("Enter target object:");
				
				for (WorldObject wo : s) {
					options.addLine(wo.getName());
				}
				
			} else {
				options.addLine("No objects to select");
			}
			
			break;
		case SET_MINST_SPD:
			inst = getActiveInstruction();

			if (inst instanceof MotionInstruction) {
				MotionInstruction mInst = (MotionInstruction)inst;
				
				if (mInst.getMotionType() == Fields.MTYPE_JOINT) {
					speedInPercentage = true;
					workingTextSuffix = "%";
				} else {
					workingTextSuffix = "mm/s";
					speedInPercentage = false;
				}
				
				options.addLine("Enter desired speed:");
				options.addLine(workingText + workingTextSuffix);
				
			} else {
				String line = String.format("Invalid instruction: %s", inst);
				options.addLine(line);
			}
			
			break;
		case SET_MINST_TERM:
			options.addLine("Enter desired termination %(0-100):");
			options.addLine("\0" + workingText);
			break;
		case SET_MINST_OFF_TYPE:
			options.addLine("None");
			options.addLine("PR[...]");
			break;
		case SET_MINST_OFFIDX:
			options.addLine("Enter desired offset register (1-100):");
			options.addLine("\0" + workingText);
			break;
		case SET_IO_INSTR_STATE:
			options.addLine("1. ON");
			options.addLine("2. OFF");
			break;
		case SET_IO_INSTR_IDX:
			options.addLine("Select I/O register index:");
			options.addLine("\0" + workingText);
			break;
		case SET_FRM_INSTR_TYPE:
			options.addLine("1. TFRAME_NUM = x");
			options.addLine("2. UFRAME_NUM = x");
			break;
		case SET_FRAME_INSTR_IDX:
			options.addLine("Select frame index:");
			options.addLine("\0" + workingText);
			break;
		case SET_REG_EXPR_TYPE:
			options.addLine("1. R[x] = (...)");
			options.addLine("2. IO[x] = (...)");
			options.addLine("3. PR[x] = (...)");
			options.addLine("4. PR[x, y] = (...)");
			break;
		case SET_REG_EXPR_IDX1:
			options.addLine("Select register index:");
			options.addLine("\0" + workingText);
			break;
		case SET_REG_EXPR_IDX2:
			options.addLine("Select point index:");
			options.addLine("\0" + workingText);
			break;
		case SET_EXPR_OP:
			if (opEdit instanceof Expression) {
				if (getActiveInstruction() instanceof IfStatement) {
					options.addLine("1. + ");
					options.addLine("2. - ");
					options.addLine("3. * ");
					options.addLine("4. / (Division)");
					options.addLine("5. | (Integer Division)");
					options.addLine("6. % (Modulus)");
					options.addLine("7. = ");
					options.addLine("8. <> (Not Equal)");
					options.addLine("9. > ");
					options.addLine("10. < ");
					options.addLine("11. >= ");
					options.addLine("12. <= ");
					options.addLine("13. AND ");
					options.addLine("14. OR ");
					options.addLine("15. NOT ");
				} else {
					options.addLine("1. + ");
					options.addLine("2. - ");
					options.addLine("3. * ");
					options.addLine("4. / (Division)");
					options.addLine("5. | (Integer Division)");
					options.addLine("6. % (Modulus)");
				}
			} else {
				options.addLine("1. ... =  ...");
				options.addLine("2. ... <> ...");
				options.addLine("3. ... >  ...");
				options.addLine("4. ... <  ...");
				options.addLine("5. ... >= ...");
				options.addLine("6. ... <= ...");
			}

			break;
		case SET_EXPR_ARG:
		case SET_BOOL_EXPR_ARG:
			options.addLine("R[x]");
			options.addLine("IO[x]");
			if (opEdit instanceof Expression) {
				options.addLine("PR[x]");
				options.addLine("PR[x, y]");
				options.addLine("(...)");
			}
			options.addLine("Const");
			break;
		case INPUT_DREG_IDX:
		case INPUT_IOREG_IDX:
		case INPUT_PREG_IDX1:
			options.addLine("Input register index:");
			options.addLine("\0" + workingText);
			break;
		case INPUT_PREG_IDX2:
			options.addLine("Input position value index:");
			options.addLine("\0" + workingText);
			break;
		case INPUT_CONST:
			options.addLine("Input constant value:");
			options.addLine("\0" + workingText);
			break;
		case SET_BOOL_CONST:
			options.addLine("1. False");
			options.addLine("2. True");
			break;
		case SET_IF_STMT_ACT:
		case SET_SELECT_STMT_ACT:
			options.addLine("JMP LBL[x]");
			options.addLine("CALL");
			
			if ( UI.getRobotButtonState() ) {
				options.addLine("RCALL");
			}
			break;
		case SET_SELECT_STMT_ARG:
			options.addLine("R[x]");
			options.addLine("Const");
			break;
		case SET_SELECT_ARGVAL:
			options.addLine("Input value/ register index:");
			options.addLine("\0" + workingText);
			break;
		case SET_LBL_NUM:
			options.addLine("Set label number:");
			options.addLine("\0" + workingText);
			break;
		case SET_JUMP_TGT:
			options.addLine("Set jump target label:");
			options.addLine("\0" + workingText);
			break;
		case SET_CALL_PROG:
			RoboticArm r = activeRobot;
			inst = r.getInstToEdit(getActiveProg(), getActiveInstIdx());
			CallInstruction cInst;
			
			// Get the call instruction
			if (inst instanceof IfStatement) {
				cInst = (CallInstruction) ((IfStatement) inst).getInstr();
				
			} else if (inst instanceof SelectStatement) {
				SelectStatement sStmt = (SelectStatement) inst;
				cInst = (CallInstruction) sStmt.getInstrs().get(editIdx);
				
			} else {
				cInst =  (CallInstruction) inst;
			}
						
			// List the robot's program names
			options.setLines( loadPrograms( cInst.getTgtDevice() ) );
			
			break;
			// Insert instructions (non-movement)
		case SELECT_INSTR_INSERT:
			options.addLine("1. I/O");
			options.addLine("2. Frames");
			options.addLine("3. Registers");
			options.addLine("4. IF/SELECT");
			options.addLine("5. JMP/LBL");
			options.addLine("6. CALL");
			
			/*
			 * Only allow the user to add robot call instructions when the
			 * second robot is in the application
			 */
			if (UI.getRobotButtonState()) {
				options.addLine("7. RCALL");
			}
			
			break;
		case SELECT_IO_INSTR_REG:
			options.setLines( loadIORegInst(activeRobot) );
			break;
		case SELECT_FRAME_INSTR_TYPE:
			options.addLine("1. TFRAME_NUM = ...");
			options.addLine("2. UFRAME_NUM = ...");
			break;
		case SELECT_REG_STMT:
			options.addLine("1. R[x] = (...)");
			options.addLine("2. IO[x] = (...)");
			options.addLine("3. PR[x] = (...)");
			options.addLine("4. PR[x, y] = (...)");
			break;
		case SELECT_COND_STMT:
			options.addLine("1. IF Stmt");
			options.addLine("2. IF (...)");
			options.addLine("3. SELECT Stmt");
			break;
		case SELECT_JMP_LBL:
			options.addLine("1. LBL[...]");
			options.addLine("2. JMP LBL[...]");
			break;

			// Frame navigation and edit menus
		case SELECT_FRAME_MODE:
			options.addLine("1. Tool Frame");
			options.addLine("2. User Frame");
			break;
		case TFRAME_DETAIL:
			options.addLine("1. Three Point Method");
			options.addLine("2. Six Point Method");
			options.addLine("3. Direct Entry Method");
			break;
		case SET_DEF_TOOLTIP:
			options.setLines( loadEEToolTipDefaults(activeRobot) );
			break;
		case UFRAME_DETAIL:
			options.addLine("1. Three Point Method");
			options.addLine("2. Four Point Method");
			options.addLine("3. Direct Entry Method");
			break;
		case TEACH_3PT_TOOL:
			r = activeRobot;
			ArrayList<DisplayLine> lines =
					loadPointList(r.getToolFrame(curFrameIdx), 0);
			options.setLines(lines);
			break;
		case TEACH_3PT_USER:
			r = activeRobot;
			lines = loadPointList(r.getUserFrame(curFrameIdx), 0);
			options.setLines(lines);
			break;
		case TEACH_4PT:
			r = activeRobot;
			lines = loadPointList(r.getUserFrame(curFrameIdx), 1);
			options.setLines(lines);
			break;
		case TEACH_6PT:
			r = activeRobot;
			lines = loadPointList(r.getToolFrame(curFrameIdx), 1);
			options.setLines(lines);
			break;

			// Macro edit menus
		case SET_MACRO_TYPE:
			options.addLine("1. Shift + User Key");
			options.addLine("2. Manual Function");
			break;
		case SET_MACRO_BINDING:
			options.addLine("1. Tool 1");
			options.addLine("2. Tool 2");
			options.addLine("3. MVMU");
			options.addLine("4. Setup");
			options.addLine("5. Status");
			options.addLine("6. POSN");
			options.addLine("7. I/O");
			break;
		case NAV_PREGS:
			PositionRegister pReg = activeRobot.getPReg(active_index);
			Point pt = pReg.point;
			// Display the point with the Position register of the highlighted
			// line, when viewing the Position registers
			if (pt != null) {
				String[] pregEntry = pt.toLineStringArray(pReg.isCartesian);

				for (String line : pregEntry) {
					options.addLine(line);
				}
			}

			break;
		case CP_DREG_COM:
			options.addLine(String.format("Move R[%d]'s comment to:", active_index + 1));
			options.addLine(String.format("R[%s]", workingText));
			break;
		case CP_DREG_VAL:
			options.addLine(String.format("Move R[%d]'s value to:", active_index + 1));
			options.addLine(String.format("R[%s]", workingText));
			break;
		case CP_PREG_COM:
			options.addLine(String.format("Move PR[%d]'s comment to:", active_index + 1));
			options.addLine(String.format("PR[%s]", workingText));
			break;
		case CP_PREG_PT:
			options.addLine(String.format("Move PR[%d]'s point to:", active_index + 1));
			options.addLine(String.format("PR[%s]", workingText));
			break;
		case SWAP_PT_TYPE:
			options.addLine("1. Cartesian");
			options.addLine("2. Joint");
			break;
		case EDIT_DREG_VAL:
			options.addLine(String.format("Input R[%d]'s value:", active_index + 1));
			options.addLine("\0" + workingText);
			break;

			// Misc functions
		case JUMP_TO_LINE:
			options.addLine("Use number keys to enter line number to jump to");
			options.addLine("\0" + workingText);
			break;
		default:
			if (mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
				options.addLine("1. Uppercase");
				options.addLine("1. Lowercase");
			}
		}
	}

	/**
	 * Update the header, contents, options, and function button labels on the
	 * pendant.
	 */
	public void updatePendantScreen() {
		updateContents();
		updateOptions();
		
		UI.renderPendantScreen(getHeader(mode), getContentsMenu(),
				getOptionsMenu(), getFunctionLabels(mode));
	}

	public void UpdateCam() {
		if (rCamera != null) {
			UI.updateCameraCurrent();
		}
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
			// Not sure if size - 1 should be used instead
			SCENARIO_UNDO.remove(0);
		}

		SCENARIO_UNDO.push(saveState);
	}

	/**
	 * Update Default button in the edit window
	 * 
	 * Updates the default position and orientation of a world object based on
	 * the input fields in the edit window.
	 */
	public void UpdateWODef() {
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
	 * Create button in the Create window
	 * 
	 * Pulls the user's input from the input fields (name, shape type,
	 * dimensions, colors, etc.) in the Create window and attempts to create a
	 * new world object from the user's input. If creation of a world object
	 * was successful, then the new object is added to the active scenario and
	 * all data is saved.
	 */
	public void WOCreateBtn() {
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
	 * Delete button in the edit window
	 * 
	 * Removes the selected world object from the active scenario.
	 */
	public void WODelBtn() {
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
	 * Updates the appropiate user input buffer based on the active pendant
	 * screen.
	 * 
	 * @param c	The character input by the user
	 */
	private void characterInput(char c) {
		if (mode.getType() == ScreenType.TYPE_TEXT_ENTRY && workingText.length() < TEXT_ENTRY_LEN
				&& ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
						|| c == '.' || c == '@' || c == '*' || c == '_')) {

			int columnIdx = contents.getColumnIdx();

			if (workingText.length() == 0 || columnIdx >= workingText.length()) {
				workingText.append(c);

			} else {
				workingText.insert(columnIdx, c);
			}
			// Edge case of adding a character to an empty text entry
			if (workingText.length() == 1 && workingText.charAt(0) != '\0') {
				workingText.append('\0');
				++columnIdx;
			}

			contents.setColumnIdx(min(columnIdx + 1, workingText.length() - 1));

		} else if (mode.getType() == ScreenType.TYPE_NUM_ENTRY && workingText.length() < NUM_ENTRY_LEN) {
			
			if (mode == ScreenMode.SET_MINST_SPD) {
				// Special case for motion instruction speed number entry
				if ((c >= '0' && c <= '9') && workingText.length() < 4) {
					workingText.append(c);
				}
				
			} else if ((c >= '0' && c <= '9') || c == '.' || c == '-') {
				// Append the character
				workingText.append(c);
			}

		} else if (mode.getType() == ScreenType.TYPE_POINT_ENTRY) {

			if ((c >= '0' && c <= '9') || c == '-' || c == '.') {
				DisplayLine entry = contents.getActiveLine();
				int idx = contents.getColumnIdx();
				
				if (entry.get(idx) == "\0") {
					entry.set(idx, Character.toString(c));
					arrow_rt();
					
				// Include prefix in length	
				} else if (entry.size() < (NUM_ENTRY_LEN + 1)) {
					entry.add(idx, Character.toString(c));
					arrow_rt();
				}
			}
			
		}
		
		// Update the screen after a character insertion
		updatePendantScreen();
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
	 * Execute the given macro
	 * 
	 * @param m
	 */
	private void execute(Macro m) {
		// Stop any prior Robot movement
		hold();
		// Safeguard against editing a program while it is running
		contents.setColumnIdx(0);
		progExec(m.getProgIdx(), 0, isStep());
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
		pushScreen(mode, contents.getLineIdx(), contents.getColumnIdx(),
				contents.getRenderStart(), options.getLineIdx(),
				options.getRenderStart());
	}

	/**
	 * Pushes a save state with the given values onto the screen state stack.
	 * 
	 * @param mode			The screen mode
	 * @param conLnIdx		The line index of the contents menu
	 * @param conColIdx		The column index of the contents menu
	 * @param conRenIdx		The render start index of the contents menu
	 * @param optLnIdx		The line index of the options menu
	 * @param optRenIdx		The render start index of the options menu
	 */
	private void pushScreen(ScreenMode mode, int conLnIdx, int conColIdx,
			int conRenIdx, int optLnIdx, int optRenIdx) {
		
		ScreenState curState = new ScreenState(mode, conLnIdx, conColIdx,
				conRenIdx, optLnIdx, optRenIdx);
		
		if (screenStates.size() > 10) {
			screenStates.remove(0);
		}
		
		screenStates.push(curState);
		
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
					if(areOBBsRendered()) {
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
					((Part)wldObj).draw(getGraphics(), areOBBsRendered());
					
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
					r.draw(getGraphics(), areOBBsRendered(), axesType);
					
				} else {
					r.draw(getGraphics(), false, AxesDisplay.NONE);
				}
				
			}

		} else {
			// Draw only the active robot
			activeRobot.draw(getGraphics(), areOBBsRendered(), axesType);
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

		if (mode == ScreenMode.TEACH_6PT && teachFrame instanceof ToolFrame) {
			size = 6;
		} else if (mode == ScreenMode.TEACH_4PT && teachFrame instanceof UserFrame) {
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
					dimDisplay += String.format("%-12s", dimFields[idx]);

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
	private int selectedMInstRegState() {
		if (mode == ScreenMode.NAV_PROG_INSTR) {
			Instruction inst = getActiveInstruction();
			
			if (inst instanceof PosMotionInst) {
				PosMotionInst mInst = (PosMotionInst)inst;
				int sdx = getSelectedIdx();
				
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
								contents.setColumnIdx(0);
							}
							
							progExecState.setCurIdx( progExecState.getNextIdx() );
						}
						
					} else {
						progExecState.setState(ExecState.EXEC_DONE);
					}
					
				} else if (progExecState.isSingleExec()) {
					// Reached the end of execution
					progExecState.setState(ExecState.EXEC_DONE);
					
				} else {
					progExecState.setState(ExecState.EXEC_INST);
				}
			}
		}
		
		// Update the display
		contents.setLineIdx( getInstrLine(getActiveInstIdx()) );
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
		if (mode == ScreenMode.NAV_PROG_INSTR) {
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


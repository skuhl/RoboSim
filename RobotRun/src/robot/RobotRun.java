package robot;

import java.awt.event.KeyEvent;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import expression.AtomicExpression;
import expression.ExprOperand;
import expression.Expression;
import expression.ExpressionElement;
import expression.Operator;
import frame.CoordFrame;
import frame.Frame;
import frame.ToolFrame;
import frame.UserFrame;
import geom.Fixture;
import geom.Part;
import geom.Point;
import geom.RMath;
import geom.RQuaternion;
import geom.Triangle;
import geom.WorldObject;
import global.Fields;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PShape;
import processing.core.PVector;
import processing.event.MouseEvent;
import programming.CallInstruction;
import programming.FrameInstruction;
import programming.IOInstruction;
import programming.IfStatement;
import programming.Instruction;
import programming.JumpInstruction;
import programming.LabelInstruction;
import programming.Macro;
import programming.MotionInstruction;
import programming.Program;
import programming.RegisterStatement;
import programming.SelectStatement;
import regs.DataRegister;
import regs.IORegister;
import regs.PositionRegister;
import regs.Register;
import screen.DisplayLine;
import screen.MenuScroll;
import screen.ScreenMode;
import screen.ScreenState;
import screen.ScreenType;
import ui.AxesDisplay;
import ui.Camera;
import window.WGUI;

public class RobotRun extends PApplet {

	/* A set of letters, used by the pendant function keys, when the users is
	 * inputing a text entry. */
	private static final char[][] LETTERS;
	private static final int ITEMS_TO_SHOW;
	private static final int NUM_ENTRY_LEN;
	private static final int TEXT_ENTRY_LEN;
	private static RobotRun instance;

	/* Initialize all static fields */
	static {
		LETTERS = new char[][] { { 'a', 'b', 'c', 'd', 'e', 'f' }, 
			{ 'g', 'h', 'i', 'j', 'k', 'l' },
			{ 'm', 'n', 'o', 'p', 'q', 'r' }, 
			{ 's', 't', 'u', 'v', 'w', 'x' }, 
			{ 'y', 'z', '_', '@', '*', '.' } };

		ITEMS_TO_SHOW = 8;
		NUM_ENTRY_LEN = 9;
		TEXT_ENTRY_LEN = 16;
		instance = null;
	}



	

	/**
	 * Applies the rotations and translations of the Robot Arm to get to the
	 * face plate center, given the set of six joint angles, each corresponding
	 * to a joint of the Robot Arm and each within the bounds of [0, TWO_PI).
	 * 
	 * @param jointAngles
	 *            A valid set of six joint angles (in radians) for the Robot
	 */
	public static void applyModelRotation(RoboticArm model, float[] jointAngles) {
		PVector basePos = model.getBasePosition();

		instance.translate(basePos.x, basePos.y, basePos.z);

		instance.translate(-50, -166, -358); // -115, -213, -413
		instance.rotateZ(PI);
		instance.translate(150, 0, 150);
		instance.rotateX(PI);
		instance.rotateY(jointAngles[0]);
		instance.rotateX(-PI);
		instance.translate(-150, 0, -150);
		instance.rotateZ(-PI);
		instance.translate(-115, -85, 180);
		instance.rotateZ(PI);
		instance.rotateY(PI / 2);
		instance.translate(0, 62, 62);
		instance.rotateX(jointAngles[1]);
		instance.translate(0, -62, -62);
		instance.rotateY(-PI / 2);
		instance.rotateZ(-PI);
		instance.translate(0, -500, -50);
		instance.rotateZ(PI);
		instance.rotateY(PI / 2);
		instance.translate(0, 75, 75);
		instance.rotateZ(PI);
		instance.rotateX(jointAngles[2]);
		instance.rotateZ(-PI);
		instance.translate(0, -75, -75);
		instance.rotateY(PI / 2);
		instance.rotateZ(-PI);
		instance.translate(745, -150, 150);
		instance.rotateZ(PI / 2);
		instance.rotateY(PI / 2);
		instance.translate(70, 0, 70);
		instance.rotateY(jointAngles[3]);
		instance.translate(-70, 0, -70);
		instance.rotateY(-PI / 2);
		instance.rotateZ(-PI / 2);
		instance.translate(-115, 130, -124);
		instance.rotateZ(PI);
		instance.rotateY(-PI / 2);
		instance.translate(0, 50, 50);
		instance.rotateX(jointAngles[4]);
		instance.translate(0, -50, -50);
		instance.rotateY(PI / 2);
		instance.rotateZ(-PI);
		instance.translate(150, -10, 95);
		instance.rotateY(-PI / 2);
		instance.rotateZ(PI);
		instance.translate(45, 45, 0);
		instance.rotateZ(jointAngles[5]);
		instance.rotateX(PI);
		instance.rotateY(PI/2);
	}

	/**
	 * Converts the rotation matrix from the native coordinate frame to the
	 * world frame.
	 * 
	 * @param orienMat
	 *            A valid rotation matrix
	 * @return The rotation matrix in terms of the world frame
	 */
	public static float[][] convertNativeToWorld(float[][] orienMat) {
		RealMatrix frameAxes = new Array2DRowRealMatrix(RMath.floatToDouble(orienMat, 3, 3));
		RealMatrix worldAxes = new Array2DRowRealMatrix(RMath.floatToDouble(Fields.WORLD_AXES, 3, 3));

		return RMath.doubleToFloat(worldAxes.multiply(frameAxes).getData(), 3, 3);
	}
	
	

	public static RoboticArm getActiveRobot() {
		return instance.activeRobot;
	}

	/**
	 * Returns the instance of this PApplet
	 */
	public static RobotRun getInstance() {
		return instance;
	}

	public static void main(String[] args) {
		String[] appletArgs = new String[] { "robot.RobotRun" };

		if (args != null) {
			PApplet.main(concat(appletArgs, args));

		} else {
			PApplet.main(appletArgs);
		}
	}

	/**
	 * Returns the Robot's End Effector position according to the active Tool
	 * Frame's offset in the native Coordinate System.
	 * 
	 * @param model
	 *            The Robot model of which to base the position off
	 * @param jointAngles
	 *            A valid set of six joint angles (in radians) for the Robot
	 * @returning The Robot's End Effector position
	 */
	public static Point nativeRobotEEPoint(RoboticArm model, float[] jointAngles) {
		Frame activeTool = getActiveRobot().getActiveFrame(CoordFrame.TOOL);
		PVector offset;

		if (activeTool != null) {
			// Apply the Tool Tip
			offset = ((ToolFrame) activeTool).getTCPOffset();
		} else {
			offset = new PVector(0f, 0f, 0f);
		}

		return nativeRobotPointOffset(model, jointAngles, offset);
	}

	/**
	 * Returns a point containing the Robot's faceplate position and orientation
	 * corresponding to the given joint angles, as well as the given joint
	 * angles.
	 * 
	 * @param model
	 *            The Robot model, of which to find the EE position
	 * @param jointAngles
	 *            A valid set of six joint angles (in radians) for the Robot
	 * @returning The Robot's faceplate position and orientation corresponding
	 *            to the given joint angles
	 */
	public static Point nativeRobotPoint(RoboticArm model, float[] jointAngles) {
		/*
		 * Return a point containing the faceplate position, orientation, and
		 * joint angles
		 */
		return nativeRobotPointOffset(model, jointAngles, new PVector(0f, 0f, 0f));
	}

	/**
	 * Returns a point containing the Robot's End Effector position and
	 * orientation corresponding to the given joint angles, as well as the given
	 * joint angles.
	 * 
	 * @param model
	 *            The Robot model, of which to find the EE position
	 * @param jointAngles
	 *            A valid set of six joint angles (in radians) for the Robot
	 * @param offset
	 *            The End Effector offset in the form of a vector
	 * @returning The Robot's EE position and orientation corresponding to the
	 *            given joint angles
	 */
	public static Point nativeRobotPointOffset(RoboticArm model, float[] jointAngles, PVector offset) {

		instance.pushMatrix();
		instance.resetMatrix();
		applyModelRotation(model, jointAngles);
		// Apply offset
		PVector ee = instance.getCoordFromMatrix(offset.x, offset.y, offset.z);
		float[][] orientationMatrix = instance.getRotationMatrix();
		instance.popMatrix();
		// Return a Point containing the EE position, orientation, and joint
		// angles
		return new Point(ee, RMath.matrixToQuat(orientationMatrix), jointAngles);
	}

	

	RobotCamera c;

	private final ArrayList<Scenario> SCENARIOS = new ArrayList<>();
	private final Stack<WorldObject> SCENARIO_UNDO = new Stack<>();
	private final HashMap<Integer, RoboticArm> ROBOTS = new HashMap<>();

	private Scenario activeScenario;
	private RoboticArm activeRobot;
	private Camera camera;

	private WGUI UI;

	private ScreenMode mode;
	private MenuScroll contents;
	private MenuScroll options;

	private Stack<ScreenState> display_stack;
	private ArrayList<Macro> macros = new ArrayList<>();
	private Macro[] SU_macro_bindings = new Macro[7];
	private Macro edit_macro;

	private boolean shift = false; // Is shift button pressed or not?
	private boolean step = false; // Is step button pressed or not?
	private boolean ctrl = false;

	// Indicates whether a program is currently running
	private boolean programRunning = false;
	private boolean executingInstruction = false;
	public boolean execSingleInst = false;

	int temp_select = 0;

	private int record = Fields.OFF;
	
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
	public ExprOperand opEdit = null;

	public int editIdx = -1;

	// store numbers pressed by the user
	ArrayList<Integer> nums = new ArrayList<>();

	// container for instructions being coppied/ cut and pasted
	ArrayList<Instruction> clipBoard = new ArrayList<>();

	// string for displaying error message to user
	String err = null;

	private int active_index = 0;

	/**
	 * Used for comment name input. The user can cycle through the six states
	 * for each function button in this mode:
	 *
	 * F1 -> A-F/a-f F2 -> G-L/g-l F3 -> M-R/m-r F4 -> S-X/s-x F5 -> Y-Z/y-z,
	 * _, @, *, .
	 */
	private int[] letterStates;

	ArrayList<Point> intermediatePositions;

	int motionFrameCounter = 0;

	float distanceBetweenPoints = 5.0f;

	int interMotionIdx = -1;

	private ArrayList<String> buffer;
	private Point displayPoint;
	
	/**
	 * TODO
	 * 
	 * @param c
	 * @return
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
			
			if (mode == ScreenMode.SET_MV_INSTR_SPD) {
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
	 * Wrapper method for applying the coordinate frame defined by the given
	 * column major transformation matrix.
	 * 
	 * @param tMatrix	A 4x4 column major transformation matrix
	 */
	public void applyMatrix(float[][] tMatrix) {
		super.applyMatrix(
				tMatrix[0][0], tMatrix[1][0], tMatrix[2][0], tMatrix[3][0],
				tMatrix[0][1], tMatrix[1][1], tMatrix[2][1], tMatrix[3][1],
				tMatrix[0][2], tMatrix[1][2], tMatrix[2][2], tMatrix[3][2],
				// Ignore scaling
				0, 0, 0, 1
		);
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
	public void applyMatrix(PVector origin, float[][] axesVectors) {
		super.applyMatrix(
				axesVectors[0][0], axesVectors[1][0], axesVectors[2][0], origin.x,
				axesVectors[0][1], axesVectors[1][1], axesVectors[2][1], origin.y,
				axesVectors[0][2], axesVectors[1][2], axesVectors[2][2], origin.z,
				0, 0, 0, 1
		);
	}

	/**
	 * @return Whether or not bounding boxes are displayed
	 */
	public boolean areOBBsDisplayed() {
		return UI.getOBBButtonState();
	}

	public void arrow_dn() {
		switch (mode) {
		case NAV_PROGRAMS:
			getActiveRobot().setActiveProgIdx(contents.moveDown(isShift()));

			if (Fields.DEBUG) {
				System.out.printf("\nRow: %d\nProg: %d\nTRS: %d\n\n", contents.getLineIdx(),
						getActiveRobot().getActiveProgIdx(), contents.getRenderStart());
			}
			break;
		case NAV_PROG_INSTR:
		case SELECT_COMMENT:
		case SELECT_CUT_COPY:
		case SELECT_INSTR_DELETE:
			if (!isProgramRunning()) {
				// Lock movement when a program is running
				Instruction i = getActiveRobot().getActiveInstruction();
				int prevIdx = getSelectedIdx();
				getActiveRobot().setActiveInstIdx(contents.moveDown(isShift()));
				int curLine = getSelectedLine();

				// special case for select statement column navigation
				if ((i instanceof SelectStatement || i instanceof MotionInstruction) && curLine == 1) {
					if (prevIdx >= 3) {
						contents.setColumnIdx(prevIdx - 3);
					} else {
						contents.setColumnIdx(0);
					}
				}

				if (Fields.DEBUG) {
					System.out.printf("\nRow: %d\nColumn: %d\nInst: %d\nTRS: %d\n\n", contents.getLineIdx(),
							contents.getColumnIdx(), getActiveRobot().getActiveInstIdx(), contents.getRenderStart());
				}
			}
			break;
		case NAV_DREGS:
		case NAV_PREGS:
		case NAV_TOOL_FRAMES:
		case NAV_USER_FRAMES:
		case NAV_MACROS:
		case NAV_MF_MACROS:
			active_index = contents.moveDown(isShift());

			if (Fields.DEBUG) {
				System.out.printf("\nRow: %d\nColumn: %d\nIdx: %d\nTRS: %d\n\n", contents.getLineIdx(),
						contents.getColumnIdx(), active_index, contents.getRenderStart());
			}
			break;
		case SET_MACRO_PROG:
		case NAV_IOREG:
			contents.moveDown(shift);
			break;
		case NAV_MAIN_MENU:
		case EDIT_IOREG:
		case NAV_INSTR_MENU:
		case SELECT_FRAME_MODE:
		case FRAME_METHOD_USER:
		case FRAME_METHOD_TOOL:
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
		case NAV_DATA:
		case SWAP_PT_TYPE:
		case SET_MV_INSTR_TYPE:
		case SET_MV_INSTR_REG_TYPE:
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
			options.moveDown(false);
			break;
		case ACTIVE_FRAMES:
			updateActiveFramesDisplay();
			workingText = new StringBuilder(Integer.toString(getActiveRobot().getActiveUserFrame() + 1));

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

	public void arrow_lt() {
		switch (mode) {
		case NAV_PROG_INSTR:
			if (!isProgramRunning()) {
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

	public void arrow_rt() {
		switch (mode) {
		case NAV_PROG_INSTR:
			if (!isProgramRunning()) {
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

	public void arrow_up() {
		switch (mode) {
		case NAV_PROGRAMS:
			getActiveRobot().setActiveProgIdx(contents.moveUp(isShift()));

			if (Fields.DEBUG) {
				System.out.printf("\nOpt: %d\nProg: %d\nTRS: %d\n\n", options.getLineIdx(),
						getActiveRobot().getActiveProgIdx(), contents.getRenderStart());
			}
			break;
		case NAV_PROG_INSTR:
		case SELECT_COMMENT:
		case SELECT_CUT_COPY:
		case SELECT_INSTR_DELETE:
			if (!isProgramRunning()) {
				try {
					// Lock movement when a program is running
					Instruction i = getActiveRobot().getActiveInstruction();
					int prevLine = getSelectedLine();
					getActiveRobot().setActiveInstIdx(contents.moveUp(isShift()));
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

				if (Fields.DEBUG) {
					System.out.printf("\nRow: %d\nColumn: %d\nInst: %d\nTRS: %d\n\n", contents.getLineIdx(),
							contents.getColumnIdx(), getActiveRobot().getActiveInstIdx(), contents.getRenderStart());
				}
			}
			break;
		case NAV_DREGS:
		case NAV_PREGS:
		case NAV_TOOL_FRAMES:
		case NAV_USER_FRAMES:
		case NAV_MACROS:
		case NAV_MF_MACROS:
			active_index = contents.moveUp(isShift());

			if (Fields.DEBUG) {
				System.out.printf("\nRow: %d\nColumn: %d\nIdx: %d\nTRS: %d\n\n", contents.getLineIdx(),
						contents.getColumnIdx(), active_index, contents.getRenderStart());
			}
			break;
		case SET_MACRO_PROG:
		case NAV_IOREG:
			contents.moveUp(isShift());
			break;
		case NAV_MAIN_MENU:
		case EDIT_IOREG:
		case NAV_INSTR_MENU:
		case SELECT_FRAME_MODE:
		case FRAME_METHOD_USER:
		case FRAME_METHOD_TOOL:
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
		case NAV_DATA:
		case SWAP_PT_TYPE:
		case SET_MV_INSTR_TYPE:
		case SET_MV_INSTR_REG_TYPE:
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
			options.moveUp(false);
			break;
		case ACTIVE_FRAMES:
			updateActiveFramesDisplay();
			workingText = new StringBuilder(Integer.toString(getActiveRobot().getActiveToolFrame() + 1));
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

	public void BackView() {
		// Back view
		camera.reset();
		camera.rotate(0, PI, 0);
	}

	/**
	 * Initiate a new circular motion instruction according to FANUC
	 * methodology.
	 * 
	 * @param p1
	 *            Point 1
	 * @param p2
	 *            Point 2
	 * @param p3
	 *            Point 3
	 */
	public void beginNewCircularMotion(Point start, Point inter, Point end) {
		calculateArc(start, inter, end);
		interMotionIdx = 0;
		motionFrameCounter = 0;
		if (intermediatePositions.size() > 0) {
			Point tgtPoint = intermediatePositions.get(interMotionIdx);
			getActiveRobot().jumpTo(tgtPoint.position, tgtPoint.orientation);
		}
	}

	/**
	 * Initiate a new continuous (curved) motion instruction.
	 * 
	 * @param model
	 *            Arm model to use
	 * @param start
	 *            Start point
	 * @param end
	 *            Destination point
	 * @param next
	 *            Point after the destination
	 * @param percentage
	 *            Intensity of the curve
	 */
	public void beginNewContinuousMotion(Point start, Point end, Point next, float p) {
		calculateContinuousPositions(start, end, next, p);
		motionFrameCounter = 0;
		if (intermediatePositions.size() > 0) {
			Point tgtPoint = intermediatePositions.get(interMotionIdx);
			getActiveRobot().jumpTo(tgtPoint.position, tgtPoint.orientation);
		}
	}

	/**
	 * Initiate a new fine (linear) motion instruction.
	 * 
	 * @param start
	 *            Start point
	 * @param end
	 *            Destination point
	 */
	public void beginNewLinearMotion(Point start, Point end) {
		calculateIntermediatePositions(start, end);
		motionFrameCounter = 0;
		if (intermediatePositions.size() > 0) {
			Point tgtPoint = intermediatePositions.get(interMotionIdx);
			getActiveRobot().jumpTo(tgtPoint.position, tgtPoint.orientation);
		}
	}

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

	public void BottomView() {
		// Bottom view
		camera.reset();
		camera.rotate(PI / 2f, 0, 0);
	}

	public void bwd() {
		// Backwards is only functional when executing a program one instruction
		// at a time
		if (mode == ScreenMode.NAV_PROG_INSTR && isShift() && isStep()) {
			Program p = activeRobot.getActiveProg();
			int instrIdx = activeRobot.getActiveInstIdx();

			// Execute the previous motion instruction
			if (p != null && instrIdx > 1 && p.getInstAt(instrIdx - 2) instanceof MotionInstruction) {
				// Stop robot motion and normal program execution
				hold();
				setProgramRunning(false);

				activeRobot.setActiveInstIdx(instrIdx - 2);
				execSingleInst = true;

				// Safeguard against editing a program while it is running
				contents.setColumnIdx(0);

				contents.moveUp(false);
				contents.moveUp(false);

				setProgramRunning(true);
			}
		}
	}

	/**
	 * Creates an arc from 'start' to 'end' that passes through the point
	 * specified by 'inter.'
	 * 
	 * @param start
	 *            First point
	 * @param inter
	 *            Second point
	 * @param end
	 *            Third point
	 */
	public void calculateArc(Point start, Point inter, Point end) {
		calculateDistanceBetweenPoints();
		intermediatePositions.clear();

		PVector a = start.position;
		PVector b = inter.position;
		PVector c = end.position;
		RQuaternion q1 = start.orientation;
		RQuaternion q2 = end.orientation;
		RQuaternion qi = new RQuaternion();

		// Calculate arc center point
		PVector[] plane = new PVector[3];
		plane = createPlaneFrom3Points(a, b, c);
		PVector center = circleCenter(vectorConvertTo(a, plane[0], plane[1], plane[2]),
				vectorConvertTo(b, plane[0], plane[1], plane[2]), vectorConvertTo(c, plane[0], plane[1], plane[2]));
		center = vectorConvertFrom(center, plane[0], plane[1], plane[2]);
		// Now get the radius (easy)
		float r = dist(center.x, center.y, center.z, a.x, a.y, a.z);
		// Calculate a vector from the center to point a
		PVector u = new PVector(a.x - center.x, a.y - center.y, a.z - center.z);
		u.normalize();
		// get the normal of the plane created by the 3 input points
		PVector tmp1 = new PVector(a.x - b.x, a.y - b.y, a.z - b.z);
		PVector tmp2 = new PVector(a.x - c.x, a.y - c.y, a.z - c.z);
		PVector n = tmp1.cross(tmp2);
		n.normalize();
		// calculate the angle between the start and end points
		PVector vec1 = new PVector(a.x - center.x, a.y - center.y, a.z - center.z);
		PVector vec2 = new PVector(c.x - center.x, c.y - center.y, c.z - center.z);
		float theta = atan2(vec1.cross(vec2).dot(n), vec1.dot(vec2));
		if (theta < 0)
			theta += PConstants.TWO_PI;
		// finally, draw an arc through all 3 points by rotating the u
		// vector around our normal vector
		float angle = 0, mu = 0;
		int numPoints = (int) (r * theta / distanceBetweenPoints);
		float inc = 1 / (float) numPoints;
		float angleInc = (theta) / numPoints;
		for (int i = 0; i < numPoints; i += 1) {
			PVector pos = RQuaternion.rotateVectorAroundAxis(u, n, angle).mult(r).add(center);
			if (i == numPoints - 1)
				pos = end.position;
			qi = RQuaternion.SLERP(q1, q2, mu);
			// println(pos + ", " + end.position);
			intermediatePositions.add(new Point(pos, qi));
			angle += angleInc;
			mu += inc;
		}
	}

	/**
	 * Calculate a "path" (series of intermediate positions) between two points
	 * in a a curved line. Need a third point as well, or a curved line doesn't
	 * make sense. Here's how this works: Assuming our current point is P1, and
	 * we're moving to P2 and then P3: 1 Do linear interpolation between points
	 * P2 and P3 FIRST. 2 Begin interpolation between P1 and P2. 3 When you're
	 * (cont% / 1.5)% away from P2, begin interpolating not towards P2, but
	 * towards the points defined between P2 and P3 in step 1. The mu for this
	 * is from 0 to 0.5 instead of 0 to 1.0.
	 *
	 * @param p1
	 *            Start point
	 * @param p2
	 *            Destination point
	 * @param p3
	 *            Third point, needed to figure out how to curve the path
	 * @param percentage
	 *            Intensity of the curve
	 */
	public void calculateContinuousPositions(Point start, Point end, Point next, float percentage) {
		// percentage /= 2;
		calculateDistanceBetweenPoints();
		percentage /= 1.5f;
		percentage = 1 - percentage;
		percentage = constrain(percentage, 0, 1);
		intermediatePositions.clear();

		PVector p1 = start.position;
		PVector p2 = end.position;
		PVector p3 = next.position;
		RQuaternion q1 = start.orientation;
		RQuaternion q2 = end.orientation;
		RQuaternion q3 = next.orientation;
		RQuaternion qi = new RQuaternion();

		ArrayList<Point> secondaryTargets = new ArrayList<>();
		float d1 = dist(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z);
		float d2 = dist(p2.x, p2.y, p2.z, p3.x, p3.y, p3.z);
		int numberOfPoints = 0;
		if (d1 > d2) {
			numberOfPoints = (int) (d1 / distanceBetweenPoints);
		} else {
			numberOfPoints = (int) (d2 / distanceBetweenPoints);
		}

		float mu = 0;
		float increment = 1.0f / numberOfPoints;
		for (int n = 0; n < numberOfPoints; n++) {
			mu += increment;
			qi = RQuaternion.SLERP(q2, q3, mu);
			secondaryTargets.add(new Point(new PVector(p2.x * (1 - mu) + (p3.x * mu), p2.y * (1 - mu) + (p3.y * mu),
					p2.z * (1 - mu) + (p3.z * mu)), qi));
		}

		mu = 0;
		int transitionPoint = (int) (numberOfPoints * percentage);
		for (int n = 0; n < transitionPoint; n++) {
			mu += increment;
			qi = RQuaternion.SLERP(q1, q2, mu);
			intermediatePositions.add(new Point(new PVector(p1.x * (1 - mu) + (p2.x * mu),
					p1.y * (1 - mu) + (p2.y * mu), p1.z * (1 - mu) + (p2.z * mu)), qi));
		}

		int secondaryIdx = 0; // accessor for secondary targets

		mu = 0;
		increment /= 2.0f;

		Point currentPoint;
		if (intermediatePositions.size() > 0) {
			currentPoint = intermediatePositions.get(intermediatePositions.size() - 1);
		} else {
			// NOTE orientation is in Native Coordinates!
			currentPoint = nativeRobotEEPoint(getActiveRobot(), getActiveRobot().getJointAngles());
		}

		for (int n = transitionPoint; n < numberOfPoints; n++) {
			mu += increment;
			Point tgt = secondaryTargets.get(secondaryIdx);
			qi = RQuaternion.SLERP(currentPoint.orientation, tgt.orientation, mu);
			intermediatePositions.add(new Point(new PVector(currentPoint.position.x * (1 - mu) + (tgt.position.x * mu),
					currentPoint.position.y * (1 - mu) + (tgt.position.y * mu),
					currentPoint.position.z * (1 - mu) + (tgt.position.z * mu)), qi));
			currentPoint = intermediatePositions.get(intermediatePositions.size() - 1);
			secondaryIdx++;
		}
		interMotionIdx = 0;
	} // end calculate continuous positions

	/**
	 * Determine how close together intermediate points between two points need
	 * to be based on current speed
	 */
	public void calculateDistanceBetweenPoints() {
		Instruction inst = getActiveRobot().getActiveInstruction();

		if (inst instanceof MotionInstruction) {
			MotionInstruction mInst = (MotionInstruction) inst;

			if (mInst != null && mInst.getMotionType() != Fields.MTYPE_JOINT)
				distanceBetweenPoints = mInst.getSpeed() / 60.0f;
			else if (getActiveRobot().getCurCoordFrame() != CoordFrame.JOINT)
				distanceBetweenPoints = getActiveRobot().motorSpeed * activeRobot.getLiveSpeed() / 6000f;
			else
				distanceBetweenPoints = 5.0f;
		}
	}

	// TODO: Add error check for colinear case (denominator is zero)
	public float calculateH(float x1, float y1, float x2, float y2, float x3, float y3) {
		float numerator = (x2 * x2 + y2 * y2) * y3 - (x3 * x3 + y3 * y3) * y2
				- ((x1 * x1 + y1 * y1) * y3 - (x3 * x3 + y3 * y3) * y1) + (x1 * x1 + y1 * y1) * y2
				- (x2 * x2 + y2 * y2) * y1;
		float denominator = (x2 * y3 - x3 * y2) - (x1 * y3 - x3 * y1) + (x1 * y2 - x2 * y1);
		denominator *= 2;
		return numerator / denominator;
	}

	/**
	 * Calculate a "path" (series of intermediate positions) between two points
	 * in a straight line.
	 * 
	 * @param start
	 *            Start point
	 * @param end
	 *            Destination point
	 */
	public void calculateIntermediatePositions(Point start, Point end) {
		calculateDistanceBetweenPoints();
		intermediatePositions.clear();

		PVector p1 = start.position;
		PVector p2 = end.position;
		RQuaternion q1 = start.orientation;
		RQuaternion q2 = end.orientation;
		RQuaternion qi = new RQuaternion();

		float mu = 0;
		float dist = dist(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z) + 100f * q1.dist(q2);
		int numberOfPoints = (int) (dist / distanceBetweenPoints);

		float increment = 1.0f / numberOfPoints;
		for (int n = 0; n < numberOfPoints; n++) {
			mu += increment;

			qi = RQuaternion.SLERP(q1, q2, mu);
			intermediatePositions.add(new Point(new PVector(p1.x * (1 - mu) + (p2.x * mu),
					p1.y * (1 - mu) + (p2.y * mu), p1.z * (1 - mu) + (p2.z * mu)), qi));
		}

		interMotionIdx = 0;
	} // end calculate intermediate positions

	public float calculateK(float x1, float y1, float x2, float y2, float x3, float y3) {
		float numerator = x2 * (x3 * x3 + y3 * y3) - x3 * (x2 * x2 + y2 * y2)
				- (x1 * (x3 * x3 + y3 * y3) - x3 * (x1 * x1 + y1 * y1)) + x1 * (x2 * x2 + y2 * y2)
				- x2 * (x1 * x1 + y1 * y1);
		float denominator = (x2 * y3 - x3 * y2) - (x1 * y3 - x3 * y1) + (x1 * y2 - x2 * y1);
		denominator *= 2;
		return numerator / denominator;
	}

	/**
	 * Finds the circle center of 3 points. (That is, find the center of a
	 * circle whose circumference intersects all 3 points.) The points must all
	 * lie on the same plane (all have the same Z value). Should have a check
	 * for colinear case, currently doesn't.
	 * 
	 * @param a
	 *            First point
	 * @param b
	 *            Second point
	 * @param c
	 *            Third point
	 * @return Position of circle center
	 */
	public PVector circleCenter(PVector a, PVector b, PVector c) {
		float h = calculateH(a.x, a.y, b.x, b.y, c.x, c.y);
		float k = calculateK(a.x, a.y, b.x, b.y, c.x, c.y);
		return new PVector(h, k, a.z);
	}

	/**
	 * Clears all input fields in the world object creation and edit windows.
	 */
	public void ClearFields() {
		UI.clearInputsFields();
	}

	/**
	 * Takes a vector and a (probably not quite orthogonal) second vector and
	 * computes a vector that's truly orthogonal to the first one and pointing
	 * in the direction closest to the imperfect second vector
	 * 
	 * @param in
	 *            First vector
	 * @param second
	 *            Second vector
	 * @return A vector perpendicular to the first one and on the same side from
	 *         first as the second one.
	 */
	public PVector computePerpendicular(PVector in, PVector second) {
		PVector[] plane = createPlaneFrom3Points(in, second, new PVector(in.x * 2, in.y * 2, in.z * 2));
		PVector v1 = vectorConvertTo(in, plane[0], plane[1], plane[2]);
		PVector v2 = vectorConvertTo(second, plane[0], plane[1], plane[2]);
		PVector perp1 = new PVector(v1.y, -v1.x, v1.z);
		PVector perp2 = new PVector(-v1.y, v1.x, v1.z);
		PVector orig = new PVector(v2.x * 5, v2.y * 5, v2.z);
		PVector p1 = new PVector(perp1.x * 5, perp1.y * 5, perp1.z);
		PVector p2 = new PVector(perp2.x * 5, perp2.y * 5, perp2.z);

		if (dist(orig.x, orig.y, orig.z, p1.x, p1.y, p1.z) < dist(orig.x, orig.y, orig.z, p2.x, p2.y, p2.z))
			return vectorConvertFrom(perp1, plane[0], plane[1], plane[2]);
		else
			return vectorConvertFrom(perp2, plane[0], plane[1], plane[2]);
	}

	public void coord() {
		if (isShift()) {
			nextScreen(ScreenMode.ACTIVE_FRAMES);

		} else {
			// Update the coordinate modeke
			coordFrameTransition();
			updatePendantScreen();
		}
	}

	/* Arrow keys */

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
		RoboticArm r = getActiveRobot();
		// Stop Robot movement
		hold();

		// Increment the current coordinate frame
		switch (r.getCurCoordFrame()) {
		case JOINT:
			r.setCurCoordFrame(CoordFrame.WORLD);
			break;

		case WORLD:
			r.setCurCoordFrame(CoordFrame.TOOL);
			break;

		case TOOL:
			r.setCurCoordFrame(CoordFrame.USER);
			break;

		case USER:
			r.setCurCoordFrame(CoordFrame.JOINT);
			break;
		}

		// Skip the Tool Frame, if there is no active frame
		if (r.getCurCoordFrame() == CoordFrame.TOOL
				&& !(r.getActiveToolFrame() >= 0 && r.getActiveToolFrame() < Fields.FRAME_NUM)) {
			r.setCurCoordFrame(CoordFrame.USER);
		}

		// Skip the User Frame, if there is no active frame
		if (r.getCurCoordFrame() == CoordFrame.USER
				&& !(r.getActiveUserFrame() >= 0 && r.getActiveUserFrame() < Fields.FRAME_NUM)) {
			r.setCurCoordFrame(CoordFrame.JOINT);
		}

		updateCoordFrame();
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
			if (Fields.DEBUG) {
				System.out.printf("Frame set: %d\n", curFrameIdx);
			}

			// Set new Frame
			if (frame instanceof ToolFrame) {
				// Update the current frame of the Robot Arm
				getActiveRobot().setActiveToolFrame(curFrameIdx);
				updateCoordFrame();

				DataManagement.saveRobotData(activeRobot, 2);
			} else {
				// Update the current frame of the Robot Arm
				getActiveRobot().setActiveUserFrame(curFrameIdx);
				updateCoordFrame();

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
		// Convert the angles from degrees to radians, then convert from World
		// to Native frame
		wpr = (new PVector(-inputs[3], -inputs[5], inputs[4])).mult(DEG_TO_RAD);

		// Save direct entry values
		taughtFrame.setDEOrigin(origin);
		taughtFrame.setDEOrientationOffset(RMath.eulerToQuat(wpr));
		taughtFrame.setFrame(2);

		if (Fields.DEBUG) {
			wpr = RMath.quatToEuler(taughtFrame.getDEOrientationOffset()).mult(DEG_TO_RAD);
			System.out.printf("\n\n%s\n%s\nFrame set: %d\n", origin.toString(), wpr.toString(), curFrameIdx);
		}

		// Set New Frame
		if (taughtFrame instanceof ToolFrame) {
			// Update the current frame of the Robot Arm
			getActiveRobot().setActiveToolFrame(curFrameIdx);
		} else {
			// Update the current frame of the Robot Arm
			getActiveRobot().setActiveUserFrame(curFrameIdx);
		}

		updateCoordFrame();
		DataManagement.saveRobotData(activeRobot, 2);
	}

	/**
	 * Create a plane (2D coordinate system) out of 3 input points.
	 * 
	 * @param a
	 *            First point
	 * @param b
	 *            Second point
	 * @param c
	 *            Third point
	 * @return New coordinate system defined by 3 orthonormal vectors
	 */
	public PVector[] createPlaneFrom3Points(PVector a, PVector b, PVector c) {
		PVector n1 = new PVector(a.x - b.x, a.y - b.y, a.z - b.z);
		n1.normalize();
		PVector n2 = new PVector(a.x - c.x, a.y - c.y, a.z - c.z);
		n2.normalize();
		PVector x = n1.copy();
		PVector z = n1.cross(n2);
		PVector y = x.cross(z);
		y.normalize();
		z.normalize();
		PVector[] coordinateSystem = new PVector[3];
		coordinateSystem[0] = x;
		coordinateSystem[1] = y;
		coordinateSystem[2] = z;
		return coordinateSystem;
	}

	public void CreateWldObj() {
		/* Create a world object from the input fields in the Create window. */
		if (activeScenario != null) {
			WorldObject newObject = UI.createWorldObject();

			if (newObject != null) {
				newObject.setLocalCenter(new PVector(-500f, 0f, 0f));
				activeScenario.addWorldObject(newObject);
				DataManagement.saveScenarios(this);
			}
		}
	}
	
	// Dash button
	public void dash() {
		characterInput('-');
	}

	// Data button
	public void data() {
		resetStack();
		nextScreen(ScreenMode.NAV_DATA);
	}

	public void DeleteWldObj() {
		// Delete focused world object and add to the scenario undo stack
		updateScenarioUndo(UI.getSelectedWO());
		int ret = UI.deleteActiveWorldObject();
		DataManagement.saveScenarios(this);
		if (Fields.DEBUG) {
			System.out.printf("World Object removed: %d\n", ret);
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

		// Apply the camera for drawing objects
		directionalLight(255, 255, 255, 1, 1, 0);
		ambientLight(150, 150, 150);

		background(127);

		hint(ENABLE_DEPTH_TEST);
		background(255);
		noStroke();
		noFill();

		pushMatrix();
		camera.apply();

		updateAndDrawObjects(activeScenario, getActiveRobot());

		renderCoordAxes();
		renderTeachPoints();

		WorldObject wldObj = UI.getSelectedWO();

		if (wldObj != null) {
			pushMatrix();

			if (wldObj instanceof Part) {
				Fixture reference = ((Part) wldObj).getFixtureRef();

				if (reference != null) {
					// Draw part's orientation with reference to its fixture
					reference.applyCoordinateSystem();
				}
			}

			renderOriginAxes(wldObj.getLocalCenter(), convertNativeToWorld(wldObj.getLocalOrientationAxes()), 500f,
					color(0));

			popMatrix();
		}

		if (displayPoint != null) {
			// Display the point with its local orientation axes
			renderOriginAxes(displayPoint.position, displayPoint.orientation.toMatrix(), 100f, color(0, 100, 15));
		}

		/*TESTING CODE: DRAW INTERMEDIATE POINTS*
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
		/**/

		/*Camera Test Code*/
		Fixture f = new Fixture("test", 160, 0, 50, 100, 200);
		f.setLocalCenter(new PVector(-700, -300, 0));
		Point p = RobotRun.nativeRobotPoint(activeRobot, activeRobot.getJointAngles());
		float[][] axes = RMath.quatToMatrix(p.orientation);
		c.setOrientation(p.orientation.mult(new RQuaternion(new PVector(axes[1][0], axes[1][1], axes[1][2]), -PI/2)));
		c.setPosition(p.position);
		renderOriginAxes(p.position, p.orientation.toMatrix(), 300, 0);
		
		PVector near[] = c.getPlaneNear();
		PVector far[] = c.getPlaneFar();
		PVector view[] = c.getPlane(c.checkObjectInFrame(f, true));
		pushMatrix();
		stroke(255, 126, 0, 255);
		//dfill(255, 126, 0, 255);
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
		
		fill(255, 126, 0, 126);
		beginShape();
		vertex(view[0].x, view[0].y, view[0].z);
		vertex(view[1].x, view[1].y, view[1].z);
		vertex(view[3].x, view[3].y, view[3].z);
		vertex(view[2].x, view[2].y, view[2].z);
		endShape();
		popMatrix();
						
		pushMatrix();
		f.draw();
		popMatrix();
		
		c.checkObjectInFrame(f, false);
		/*for(int i = 0; i < 8; i += 1) {
			pushMatrix();
			stroke(0);
			translate(p.position.x-30, p.position.y, p.position.z);
			translate(obj[i].x, obj[i].y, obj[i].z);
			sphere(1);
			popMatrix();
		}
		//RobotRun.printMat(c.getOrientationMat());
		/**/
		 

		noLights();
		noStroke();
		popMatrix();

		hint(DISABLE_DEPTH_TEST);
		// Apply the camera for drawing text and windows
		ortho();
		renderUI();
		
	}

	/**
	 * Function of the edit button: renders the instruction view of the active
	 * program, if one exists. Otherwise, the program navigation view is
	 * rendered.
	 */
	public void edit() {
		if (activeRobot.getActiveProg() != null) {
			nextScreen(ScreenMode.NAV_PROG_INSTR);
		} else {
			RoboticArm arm = getActiveRobot();

			if (arm.numOfPrograms() > 0) {
				arm.setActiveProgIdx(0);
			}
			
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
		} else if (e instanceof ExprOperand) {
			editOperand((ExprOperand) e, elements[selectIdx]);
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
	public void editOperand(ExprOperand o, int ins_idx) {
		switch (o.type) {
		case -2: // Uninit
			editIdx = ins_idx;
			nextScreen(ScreenMode.SET_EXPR_ARG);
			break;
		case 0: // Float const
			opEdit = o;
			nextScreen(ScreenMode.INPUT_CONST);
			break;
		case 1: // Bool const
			opEdit = o;
			nextScreen(ScreenMode.SET_BOOL_CONST);
			break;
		case 2: // Data reg
			opEdit = o;
			nextScreen(ScreenMode.INPUT_DREG_IDX);
			break;
		case 3: // IO reg
			opEdit = o;
			nextScreen(ScreenMode.INPUT_IOREG_IDX);
			break;
		case 4: // Pos reg
			opEdit = o;
			nextScreen(ScreenMode.INPUT_PREG_IDX1);
			break;
		case 5: // Pos reg at index
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

	public void EE() {
		getActiveRobot().cycleEndEffector();
	}

	public void enter() {
		RoboticArm r = getActiveRobot();
		Program p = r.getActiveProg();

		switch (mode) {
		// Main menu
		case NAV_MAIN_MENU:
			if (options.getLineIdx() == 0) { // Frames
				nextScreen(ScreenMode.SELECT_FRAME_MODE);

			} else if (options.getLineIdx() == 1) { // Macros
				nextScreen(ScreenMode.NAV_MACROS);

			} else if (options.getLineIdx() == 2) { // Manual Functions
				nextScreen(ScreenMode.NAV_MF_MACROS);

			} else if (options.getLineIdx() == 3) {
				nextScreen(ScreenMode.NAV_IOREG);
			}

			break;
		case NAV_IOREG:
			active_index = contents.getLineIdx();
			nextScreen(ScreenMode.EDIT_IOREG);
			break;
		case EDIT_IOREG:
			IORegister ioReg = activeRobot.getIOReg(active_index);
			ioReg.state = options.getLineIdx();

			lastScreen();
			break;
			// Frame nav and edit
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
		case FRAME_METHOD_USER:
			// User Frame teaching methods
			teachFrame = getActiveRobot().getUserFrame(curFrameIdx);
			if (options.getLineIdx() == 0) {
				nextScreen(ScreenMode.TEACH_3PT_USER);
			} else if (options.getLineIdx() == 1) {
				nextScreen(ScreenMode.TEACH_4PT);
			} else if (options.getLineIdx() == 2) {
				nextScreen(ScreenMode.DIRECT_ENTRY_USER);
			}
			break;
		case FRAME_METHOD_TOOL:
			teachFrame = getActiveRobot().getToolFrame(curFrameIdx);
			// Tool Frame teaching methods
			if (options.getLineIdx() == 0) {
				nextScreen(ScreenMode.TEACH_3PT_TOOL);
			} else if (options.getLineIdx() == 1) {
				nextScreen(ScreenMode.TEACH_6PT);
			} else if (options.getLineIdx() == 2) {
				nextScreen(ScreenMode.DIRECT_ENTRY_TOOL);
			}
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

				int new_prog = getActiveRobot().addProgram(new Program(workingText.toString(), activeRobot));
				getActiveRobot().setActiveProgIdx(new_prog);
				getActiveRobot().setActiveInstIdx(0);

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
				Program prog = getActiveRobot().getActiveProg();

				if (prog != null) {
					prog.setName(workingText.toString());
					getActiveRobot().setActiveInstIdx(0);
					DataManagement.saveRobotData(activeRobot, 1);
				}

				resetStack();
				nextScreen(ScreenMode.NAV_PROGRAMS);
			}
			break;
		case PROG_COPY:
			if (workingText.length() > 0 && !workingText.equals("\0")) {
				if (workingText.charAt(workingText.length() - 1) == '\0') {
					// Remove insert character
					workingText.deleteCharAt(workingText.length() - 1);
				}

				Program prog = getActiveRobot().getActiveProg();

				if (prog != null) {
					Program newProg = prog.clone();
					newProg.setName(workingText.toString());
					int new_prog = getActiveRobot().addProgram(newProg);
					getActiveRobot().setActiveProgIdx(new_prog);
					getActiveRobot().setActiveInstIdx(0);
					DataManagement.saveRobotData(activeRobot, 1);
				}

				resetStack();
				nextScreen(ScreenMode.NAV_PROGRAMS);
			}
			break;
		case NAV_PROGRAMS:
			if (getActiveRobot().numOfPrograms() != 0) {
				getActiveRobot().setActiveInstIdx(0);
				//contents.reset();
				nextScreen(ScreenMode.NAV_PROG_INSTR);
			}
			break;

			// Instruction options menu
		case NAV_INSTR_MENU:

			switch (options.getLineIdx()) {
			case 0: // Undo
				getActiveRobot().popInstructionUndo();
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
				editIdx = getActiveRobot().RID;
				switchScreen(ScreenMode.SET_CALL_PROG);
				break;
			case 6: // RobotCall
				newRobotCallInstruction();
				editIdx = getInactiveRobot().RID;
				switchScreen(ScreenMode.SET_CALL_PROG);
			}

			break;
		case SELECT_IO_INSTR_REG:
			newIOInstruction(options.getColumnIdx());
			display_stack.pop();
			lastScreen();
			break;
		case SELECT_FRAME_INSTR_TYPE:
			if (options.getLineIdx() == 0) {
				newFrameInstruction(Fields.FTYPE_TOOL);
			} else {
				newFrameInstruction(Fields.FTYPE_USER);
			}

			display_stack.pop();
			switchScreen(ScreenMode.SET_FRAME_INSTR_IDX);
			break;
		case SELECT_REG_STMT:
			display_stack.pop();
			display_stack.pop();

			if (options.getLineIdx() == 0) {
				newRegisterStatement(new DataRegister());
			} else if (options.getLineIdx() == 1) {
				newRegisterStatement(new IORegister());
			} else if (options.getLineIdx() == 2) {
				newRegisterStatement(new PositionRegister());
			} else {
				newRegisterStatement(new PositionRegister(), 0);
				pushScreen(ScreenMode.SET_REG_EXPR_IDX2, -1, -1, 0, 1, 0);
			}

			nextScreen(ScreenMode.SET_REG_EXPR_IDX1);
			break;
		case SELECT_COND_STMT:
			if (options.getLineIdx() == 0) {
				newIfStatement();
				display_stack.pop();
				switchScreen(ScreenMode.SET_EXPR_OP);
			} else if (options.getLineIdx() == 1) {
				newIfExpression();
				display_stack.pop();
				lastScreen();
			} else {
				newSelectStatement();
				display_stack.pop();
				lastScreen();
			}

			break;
		case SELECT_JMP_LBL:
			display_stack.pop();

			if (options.getLineIdx() == 0) {
				newLabel();
				switchScreen(ScreenMode.SET_LBL_NUM);
			} else {
				newJumpInstruction();
				switchScreen(ScreenMode.SET_JUMP_TGT);
			}

			break;

			// Movement instruction edit
		case SET_MV_INSTR_TYPE:
			MotionInstruction m = (MotionInstruction) r.getInstToEdit( r.getActiveInstIdx() );
			
			if (options.getLineIdx() == 0) {
				if (m.getMotionType() != Fields.MTYPE_JOINT)
					m.setSpeed(m.getSpeed() / getActiveRobot().motorSpeed);
				m.setMotionType(Fields.MTYPE_JOINT);
			} else if (options.getLineIdx() == 1) {
				if (m.getMotionType() == Fields.MTYPE_JOINT)
					m.setSpeed(getActiveRobot().motorSpeed * m.getSpeed());
				m.setMotionType(Fields.MTYPE_LINEAR);
			} else if (options.getLineIdx() == 2) {
				if (m.getMotionType() == Fields.MTYPE_JOINT)
					m.setSpeed(getActiveRobot().motorSpeed * m.getSpeed());
				m.setMotionType(Fields.MTYPE_CIRCULAR);
			}
			
			lastScreen();
			break;
		case SET_MV_INSTR_REG_TYPE:
			m = (MotionInstruction) r.getInstToEdit( r.getActiveInstIdx() );
			
			int line = getSelectedLine();
			m = line == 0 ? m : m.getSecondaryPoint();

			if (options.getLineIdx() == 0) {
				m.setGlobalPosRegUse(false);

			} else if (options.getLineIdx() == 1) {
				m.setGlobalPosRegUse(true);
			}

			lastScreen();
			break;
		case SET_MV_INSTR_SPD:
			m = (MotionInstruction) r.getInstToEdit( r.getActiveInstIdx() );
			line = getSelectedLine();
			m = line == 0 ? m : m.getSecondaryPoint();

			float tempSpeed = Float.parseFloat(workingText.toString());
			if (tempSpeed >= 5.0f) {
				if (speedInPercentage) {
					if (tempSpeed > 100)
						tempSpeed = 10;
					tempSpeed /= 100.0f;
				} else if (tempSpeed > getActiveRobot().motorSpeed) {
					tempSpeed = getActiveRobot().motorSpeed;
				}

				m.setSpeed(tempSpeed);
			}

			lastScreen();
			break;
		case SET_MV_INSTR_IDX:
			try {
				m = (MotionInstruction) r.getInstToEdit( r.getActiveInstIdx() );
				int tempRegister = Integer.parseInt(workingText.toString());
				int lbound = 1, ubound;

				if (m.usesGPosReg()) {
					ubound = 100;

				} else {
					ubound = 1000;
				}

				line = getSelectedLine();
				m = line == 0 ? m : m.getSecondaryPoint();

				if (tempRegister < lbound || tempRegister > ubound) {
					// Invalid register index
					err = String.format("Only registers %d-%d are valid!", lbound, ubound);
					lastScreen();
					return;
				}

				m.setPositionNum(tempRegister - 1);
			} catch (NumberFormatException NFEx) {
			/* Ignore invalid numbers */ }

			lastScreen();
			break;
		case SET_MV_INSTR_TERM:
			try {
				m = (MotionInstruction) r.getInstToEdit( r.getActiveInstIdx() );
				int tempTerm = Integer.parseInt(workingText.toString());
				line = getSelectedLine();
				m = line == 0 ? m : m.getSecondaryPoint();

				if (tempTerm >= 0 && tempTerm <= 100) {
					m.setTermination(tempTerm);
				}
			} catch (NumberFormatException NFEx) {
			/* Ignore invalid input */ }

			lastScreen();
			break;
		case SET_MV_INSTR_OFFSET:
			try {
				m = (MotionInstruction) r.getInstToEdit( r.getActiveInstIdx() );
				int tempRegister = Integer.parseInt(workingText.toString()) - 1;
				line = getSelectedLine();
				m = line == 0 ? m : m.getSecondaryPoint();

				if (tempRegister < 1 || tempRegister > 1000) {
					// Invalid register index
					err = "Only registers 1 - 1000 are legal!";
					lastScreen();
					return;
				} else if ((getActiveRobot().getPReg(tempRegister)).point == null) {
					// Invalid register index
					err = "This register is uninitailized!";
					lastScreen();
					return;
				}

				m.setOffset(tempRegister);
			} catch (NumberFormatException NFEx) {
			/* Ignore invalid numbers */ }

			lastScreen();
			break;

			// Expression edit
		case SET_EXPR_ARG:
			Expression expr = (Expression) opEdit;

			if (options.getLineIdx() == 0) {
				// set arg to new data reg
				ExprOperand operand = new ExprOperand(new DataRegister());
				opEdit = expr.setOperand(editIdx, operand);
				switchScreen(ScreenMode.INPUT_DREG_IDX);
			} else if (options.getLineIdx() == 1) {
				// set arg to new io reg
				ExprOperand operand = new ExprOperand(new IORegister());
				opEdit = expr.setOperand(editIdx, operand);
				switchScreen(ScreenMode.INPUT_IOREG_IDX);
			} else if (options.getLineIdx() == 2) {
				ExprOperand operand = new ExprOperand(new PositionRegister());
				opEdit = expr.setOperand(editIdx, operand);
				switchScreen(ScreenMode.INPUT_PREG_IDX1);
			} else if (options.getLineIdx() == 3) {
				ExprOperand operand = new ExprOperand(new PositionRegister(), 0);
				opEdit = expr.setOperand(editIdx, operand);
				display_stack.pop();
				pushScreen(ScreenMode.INPUT_PREG_IDX2, -1, -1, 0, 1, 0);
				nextScreen(ScreenMode.INPUT_PREG_IDX1);
			} else if (options.getLineIdx() == 4) {
				// set arg to new expression
				Expression oper = new Expression();
				expr.setOperand(editIdx, oper);
				lastScreen();
			} else {
				// set arg to new constant
				opEdit = expr.getOperand(editIdx).reset();
				switchScreen(ScreenMode.INPUT_CONST);
			}

			break;
		case SET_BOOL_EXPR_ARG:
			if (options.getLineIdx() == 0) {
				// set arg to new data reg
				opEdit.set(new DataRegister());
				switchScreen(ScreenMode.INPUT_DREG_IDX);
			} else if (options.getLineIdx() == 1) {
				// set arg to new io reg
				opEdit.set(new IORegister());
				switchScreen(ScreenMode.INPUT_IOREG_IDX);
			} else {
				// set arg to new constant
				opEdit.reset();
				switchScreen(ScreenMode.INPUT_CONST);
			}
			break;
		case SET_IF_STMT_ACT:
			IfStatement stmt = (IfStatement) r.getInstToEdit( r.getActiveInstIdx() );
			if (options.getLineIdx() == 0) {
				stmt.setInstr(new JumpInstruction());
				switchScreen(ScreenMode.SET_JUMP_TGT);
			} else if (options.getLineIdx() == 1) {
				stmt.setInstr(new CallInstruction(activeRobot));
				editIdx = activeRobot.RID;
				switchScreen(ScreenMode.SET_CALL_PROG);
			} else {
				stmt.setInstr(new CallInstruction(getInactiveRobot()));
				editIdx = getInactiveRobot().RID;
				switchScreen(ScreenMode.SET_CALL_PROG);
			}

			break;
		case SET_EXPR_OP:
			if (opEdit instanceof Expression) {
				expr = (Expression) opEdit;
				r.getInstToEdit( r.getActiveInstIdx() );
				
				switch (options.getLineIdx()) {
				case 0:
					expr.setOperator(editIdx, Operator.ADDTN);
					break;
				case 1:
					expr.setOperator(editIdx, Operator.SUBTR);
					break;
				case 2:
					expr.setOperator(editIdx, Operator.MULT);
					break;
				case 3:
					expr.setOperator(editIdx, Operator.DIV);
					break;
				case 4:
					expr.setOperator(editIdx, Operator.INTDIV);
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
				r.getInstToEdit( r.getActiveInstIdx() );

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
						System.out.println("Invalid index!");

					} else {
						r.getInstToEdit( r.getActiveInstIdx() );
						opEdit.set(getActiveRobot().getDReg(idx - 1));
					}

				} else if (mode == ScreenMode.INPUT_PREG_IDX1) {

					if (idx < 1 || idx > 100) {
						System.out.println("Invalid index!");

					} else {
						r.getInstToEdit( r.getActiveInstIdx() );
						opEdit.set(getActiveRobot().getPReg(idx - 1));
					}

				} else if (mode == ScreenMode.INPUT_PREG_IDX2) {

					if (idx < 1 || idx > 6) {
						System.out.println("Invalid index!");

					} else {
						r.getInstToEdit( r.getActiveInstIdx() );
						opEdit.set(idx - 1);
					}

				} else if (mode == ScreenMode.INPUT_IOREG_IDX) {

					if (idx < 1 || idx > 5) {
						System.out.println("Invalid index!");

					} else {
						r.getInstToEdit( r.getActiveInstIdx() );
						opEdit.set(getActiveRobot().getIOReg(idx - 1));
					}
				}

			} catch (NumberFormatException e) {
			}

			lastScreen();
			break;
		case INPUT_CONST:
			try {
				float data = Float.parseFloat(workingText.toString());
				r.getInstToEdit( r.getActiveInstIdx() );
				opEdit.set(data);
			} catch (NumberFormatException e) {
			}

			lastScreen();
			break;
		case SET_BOOL_CONST:
			r.getInstToEdit( r.getActiveInstIdx() );
			
			if (options.getLineIdx() == 0) {
				opEdit.set(true);
			} else {
				opEdit.set(false);
			}

			lastScreen();
			break;

			// Select statement edit
		case SET_SELECT_STMT_ACT:
			SelectStatement s = (SelectStatement) r.getInstToEdit( r.getActiveInstIdx() );
			int i = (getSelectedIdx() - 3) / 3;

			if (options.getLineIdx() == 0) {
				s.getInstrs().set(i, new JumpInstruction());
			} else if (options.getLineIdx() == 0) {
				s.getInstrs().set(i, new CallInstruction(activeRobot));
			} else {
				s.getInstrs().set(i, new CallInstruction(getInactiveRobot()));
			}

			lastScreen();
			break;
		case SET_SELECT_STMT_ARG:
			if (options.getLineIdx() == 0) {
				opEdit.set(new DataRegister());
			} else {
				opEdit.reset();
			}

			nextScreen(ScreenMode.SET_SELECT_ARGVAL);
			break;
		case SET_SELECT_ARGVAL:
			try {
				s = (SelectStatement) r.getInstToEdit( r.getActiveInstIdx() );
				float f = Float.parseFloat(workingText.toString());

				if (opEdit.type == ExpressionElement.UNINIT) {
					opEdit.set(f);
				} else if (opEdit.type == ExpressionElement.DREG) {
					// println(regFile.DAT_REG[(int)f - 1].value);
					opEdit.set(getActiveRobot().getDReg((int) f - 1));
				}
			} catch (NumberFormatException ex) {
			}

			display_stack.pop();
			lastScreen();
			break;

			// IO instruction edit
		case SET_IO_INSTR_STATE:
			IOInstruction ioInst = (IOInstruction) r.getInstToEdit( r.getActiveInstIdx() );

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

				if (tempReg < 1 || tempReg >= 6) {
					System.out.println("Invalid index!");

				} else {
					ioInst = (IOInstruction) r.getInstToEdit( r.getActiveInstIdx() );
					ioInst.setReg(tempReg - 1);
				}
			} catch (NumberFormatException NFEx) {
			/* Ignore invalid input */ }

			lastScreen();
			break;

			// Frame instruction edit
		case SET_FRM_INSTR_TYPE:
			FrameInstruction fInst = (FrameInstruction) r.getInstToEdit( r.getActiveInstIdx() );

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
					fInst = (FrameInstruction) r.getInstToEdit( r.getActiveInstIdx() );
					fInst.setReg(frameIdx);
				}
			} catch (NumberFormatException NFEx) {
			/* Ignore invalid input */ }

			lastScreen();
			break;

			// Register statement edit
		case SET_REG_EXPR_TYPE:
			RegisterStatement regStmt = (RegisterStatement) r.getInstToEdit( r.getActiveInstIdx() );
			display_stack.pop();

			if (options.getLineIdx() == 0) {
				regStmt.setRegister(new DataRegister());
			} else if (options.getLineIdx() == 1) {
				regStmt.setRegister(new IORegister());
			} else if (options.getLineIdx() == 2) {
				regStmt.setRegister(new PositionRegister());
			} else {
				regStmt.setRegister(new PositionRegister(), 0);
				pushScreen(ScreenMode.SET_REG_EXPR_IDX2, -1, -1, 0, 1, 0);
			}

			nextScreen(ScreenMode.SET_REG_EXPR_IDX1);
			break;
		case SET_REG_EXPR_IDX1:
			try {
				int idx = Integer.parseInt(workingText.toString());
				regStmt = (RegisterStatement) r.getInstToEdit( r.getActiveInstIdx() );
				Register reg = regStmt.getReg();

				if (idx < 1 || ((reg instanceof DataRegister || reg instanceof PositionRegister) && idx > 100)
						|| (reg instanceof IORegister && idx > 5)) {
					// Index is out of bounds
					println("Invalid register index!");

				} else {

					if (regStmt.getReg() instanceof DataRegister) {
						regStmt.setRegister(getActiveRobot().getDReg(idx - 1));

					} else if (regStmt.getReg() instanceof IORegister) {
						regStmt.setRegister(getActiveRobot().getIOReg(idx - 1));

					} else if (regStmt.getReg() instanceof PositionRegister) {
						if (regStmt.getPosIdx() < 0) {
							// Update a position register operand
							regStmt.setRegister(getActiveRobot().getPReg(idx - 1));

						} else {
							// Update a position register index operand
							regStmt.setRegister(getActiveRobot().getPReg(idx - 1), regStmt.getPosIdx());
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
					regStmt = (RegisterStatement) r.getInstToEdit( r.getActiveInstIdx() );
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
					((LabelInstruction) r.getInstToEdit( r.getActiveInstIdx() )).setLabelNum(idx);
				}
			} catch (NumberFormatException NFEx) {
			/* Ignore invalid input */ }

			lastScreen();
			break;
		case SET_JUMP_TGT:
			try {
				int lblNum = Integer.parseInt(workingText.toString());
				int lblIdx = p.findLabelIdx(lblNum);
				Instruction inst = r.getInstToEdit( r.getActiveInstIdx() );
				
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
						err = "Invalid label number.";
					}
				}
			} catch (NumberFormatException NFEx) {
			/* Ignore invalid input */ }

			lastScreen();
			break;

			// Call instruction edit
		case SET_CALL_PROG:
			Instruction cInst = r.getInstToEdit( r.getActiveInstIdx() );
			RoboticArm tgtDevice = ROBOTS.get( editIdx );
			Program tgt = tgtDevice.getProgram( options.getActiveIndex() );
			
			if (cInst instanceof IfStatement) {
				IfStatement ifStmt = (IfStatement) cInst;
				((CallInstruction) ifStmt.getInstr()).setProg(tgt);
			} else if (cInst instanceof SelectStatement) {
				SelectStatement sStmt = (SelectStatement) cInst;
				CallInstruction c = (CallInstruction) sStmt.getInstrs().get(editIdx);
				c.setProg(tgt);
			} else {
				CallInstruction call = (CallInstruction) cInst;
				call.setProg(tgt);
			}

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
			macros.get(macro_idx).execute();
			break;

			// Program instruction editing and navigation
		case SELECT_CUT_COPY:
		case SELECT_INSTR_DELETE:
			contents.toggleSelect(getActiveRobot().getActiveInstIdx());
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
			
			while (!display_stack.isEmpty()) {
				prev = display_stack.peek();
				
				if (prev.mode == ScreenMode.NAV_INSTR_MENU) {
					break;
				}
				
				display_stack.pop();
			}
			
			lastScreen();
			break;
		case SELECT_COMMENT:
			r.getInstToEdit( r.getActiveInstIdx() ).toggleCommented();

			updatePendantScreen();
			break;
		case EDIT_MINST_POS:
			MotionInstruction mInst = (MotionInstruction) r.getInstToEdit( r.getActiveInstIdx() );
			Point pt = parsePosFromContents(mInst.getMotionType() != Fields.MTYPE_JOINT);

			if (pt != null) {
				// Update the position of the active motion instruction
				activeRobot.getActiveProg().setPosition(mInst.getPositionNum(), pt);
				DataManagement.saveRobotData(activeRobot, 1);
			}

			displayPoint = null;
			lastScreen();
			break;
		case FIND_REPL:
			lastScreen();
			break;
		case JUMP_TO_LINE:
			int jumpToInst = Integer.parseInt(workingText.toString()) - 1;
			getActiveRobot().setActiveInstIdx(max(0, min(jumpToInst, p.getNumOfInst() - 1)));

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
			if (options.getLineIdx() == 0) {
				// Data Register Menu
				nextScreen(ScreenMode.NAV_DREGS);
			} else if (options.getLineIdx() == 1) {
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
				getActiveRobot().getDReg(regIdx).comment = getActiveRobot().getDReg(active_index).comment;
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
				getActiveRobot().getDReg(regIdx).value = getActiveRobot().getDReg(active_index).value;
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
				getActiveRobot().getPReg(regIdx).comment = getActiveRobot().getPReg(active_index).comment;
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
				getActiveRobot().getPReg(regIdx).point = getActiveRobot().getPReg(active_index).point.clone();
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
				System.out.printf("Index; %d\n", active_index);
				DataRegister dReg = getActiveRobot().getDReg(active_index);

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
				getActiveRobot().getPReg(active_index).comment = workingText.toString();
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
				getActiveRobot().getDReg(active_index).comment = workingText.toString();
				DataManagement.saveRobotData(activeRobot, 3);
				workingText = new StringBuilder();
				lastScreen();
			}
			break;
		default:
			break;
		}
	}// End enter

	/**
	 * Move the arm model between two points according to its current speed.
	 * 
	 * @param model
	 *            The arm model
	 * @param speedMult
	 *            Speed multiplier
	 */
	public boolean executeMotion(RoboticArm model, float speedMult) {
		motionFrameCounter++;
		// speed is in pixels per frame, multiply that by the current speed
		// setting
		// which is contained in the motion instruction
		float currentSpeed = model.motorSpeed * speedMult;
		if (currentSpeed * motionFrameCounter > distanceBetweenPoints) {
			interMotionIdx++;
			motionFrameCounter = 0;
			if (interMotionIdx >= intermediatePositions.size()) {
				interMotionIdx = -1;
				return true;
			}

			int ret = 0;
			if (intermediatePositions.size() > 0) {
				Point tgtPoint = intermediatePositions.get(interMotionIdx);
				ret = getActiveRobot().jumpTo(tgtPoint.position, tgtPoint.orientation);
			}

			if (ret == 1) {
				triggerFault();
				return true;
			}
		}

		return false;
	} // end execute linear motion

	/**
	 * Executes a program. Returns true when done.
	 * 
	 * @param model
	 *            - Arm model to use
	 * @return - True if done executing, false if otherwise.
	 */
	public boolean executeProgram(RoboticArm model, boolean singleInstr) {
		Program program = model.getActiveProg();
		Instruction activeInstr = model.getActiveInstruction();
		int nextInstr = getActiveRobot().getActiveInstIdx() + 1;

		// stop executing if no valid program is selected or we reach the end of
		// the program
		if (getActiveRobot().hasMotionFault() || activeInstr == null) {
			return true;
		} else if (!activeInstr.isCommented()) {
			if (activeInstr instanceof MotionInstruction) {
				MotionInstruction motInstr = (MotionInstruction) activeInstr;

				// start a new instruction
				if (!isExecutingInstruction()) {
					setExecutingInstruction(setUpInstruction(program, model, motInstr));

					if (!isExecutingInstruction()) {
						// Motion Instruction failed
						nextInstr = -1;
					}
				}
				// continue current motion instruction
				else {
					if (motInstr.getMotionType() == Fields.MTYPE_JOINT) {
						setExecutingInstruction(!(model.interpolateRotation(motInstr.getSpeedForExec(model))));
					} else {
						setExecutingInstruction(!(executeMotion(model, motInstr.getSpeedForExec(model))));
					}
				}
			} else if (activeInstr instanceof JumpInstruction) {
				setExecutingInstruction(false);
				nextInstr = activeInstr.execute();

			} else if (activeInstr instanceof CallInstruction) {
				setExecutingInstruction(false);

				if (((CallInstruction) activeInstr).getTgtDevice() != activeRobot) {
					// Call an inactive Robot's program
					if (UI.getRobotButtonState()) {
						nextInstr = activeInstr.execute();
					} else {
						// No second robot in application
						nextInstr = -1;
					}
				} else {
					nextInstr = activeInstr.execute();
				}

			} else if (activeInstr instanceof IfStatement || activeInstr instanceof SelectStatement) {
				setExecutingInstruction(false);
				int ret = activeInstr.execute();

				if (ret != -2) {
					nextInstr = ret;
				}

			} else {
				setExecutingInstruction(false);

				if (activeInstr.execute() != 0) {
					nextInstr = -1;
				}
			} // end of instruction type check
		} // skip commented instructions

		if (nextInstr == -1) {
			// If a command fails
			triggerFault();
			updatePendantScreen();
			return true;

		} else if (!isExecutingInstruction()) {
			RoboticArm r = getActiveRobot();
			// Move to next instruction after current is finished
			int size = program.getNumOfInst() + 1;
			r.setActiveInstIdx(max(0, min(nextInstr, size - 1)));
			
			if (!display_stack.isEmpty()) {
				ScreenState prev = display_stack.peek();
				
				if (prev.mode == ScreenMode.NAV_PROG_INSTR) {
					int activeInst = r.getActiveInstIdx();
					contents.setLineIdx( getInstrLine(activeInst) );
				}
			}
				
		}

		updatePendantScreen();

		return !isExecutingInstruction() && this.execSingleInst;
	}// end executeProgram

	public void f1() {
		switch (mode) {
		case NAV_PROGRAMS:
			nextScreen(ScreenMode.PROG_CREATE);
			break;
		case NAV_PROG_INSTR:
			if (isShift()) {
				newMotionInstruction();
				contents.setColumnIdx(0);

				if (getSelectedLine() == 0) {
					contents.setLineIdx(contents.getLineIdx() + 1);
					updatePendantScreen();
					if (getSelectedLine() == 0) {
						getActiveRobot().setActiveInstIdx(getActiveRobot().getActiveInstIdx() + 1);
					}
				}
			}
			break;
		case NAV_TOOL_FRAMES:
			int frame = contents.getActiveLine().getItemIdx();
			
			if (isShift()) {
				// Reset the highlighted frame in the tool frame list
				getActiveRobot().getToolFrame(frame).reset();
				updatePendantScreen();
			} else {
				// Set the current tool frame
				getActiveRobot().setActiveToolFrame(frame);
				updateCoordFrame();
			}
			
			break;
		case NAV_USER_FRAMES:
			frame = contents.getActiveLine().getItemIdx();
			
			if (isShift()) {
				// Reset the highlighted frame in the user frames list
				getActiveRobot().getUserFrame(frame).reset();
				updatePendantScreen();
			} else {
				// Set the current user frame
				getActiveRobot().setActiveUserFrame(frame);
				updateCoordFrame();
			}
			break;
		case ACTIVE_FRAMES:
			if (contents.getLineIdx() == 0) {
				nextScreen(ScreenMode.NAV_TOOL_FRAMES);

			} else if (contents.getLineIdx() == 1) {
				nextScreen(ScreenMode.NAV_USER_FRAMES);
			}
			break;
		case NAV_MACROS:
			edit_macro = null;
			nextScreen(ScreenMode.SET_MACRO_PROG);
			break;
		case NAV_DREGS:
			// Clear Data Register entry
			DataRegister dReg = getActiveRobot().getDReg(active_index);

			if (dReg != null) {
				dReg.comment = null;
				dReg.value = null;
			}

			break;
		case NAV_PREGS:
			// Clear Position Register entry
			PositionRegister pReg = getActiveRobot().getPReg(active_index);

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

	public void f2() {
		switch (mode) {
		case NAV_PROGRAMS:
			if (getActiveRobot().numOfPrograms() > 0) {
				nextScreen(ScreenMode.PROG_RENAME);
			}
			break;
		case NAV_PROG_INSTR:
			nextScreen(ScreenMode.SELECT_INSTR_INSERT);
			break;
		case SELECT_CUT_COPY:
			nextScreen(ScreenMode.SELECT_PASTE_OPT);
			break;
		case TFRAME_DETAIL:
			switchScreen(ScreenMode.FRAME_METHOD_TOOL);
			// nextScreen(Screen.TOOL_FRAME_METHODS);
			break;
		case TEACH_3PT_TOOL:
		case TEACH_6PT:
		case DIRECT_ENTRY_TOOL:
			lastScreen();
			break;
		case UFRAME_DETAIL:
			switchScreen(ScreenMode.FRAME_METHOD_USER);
			// nextScreen(Screen.USER_FRAME_METHODS);
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

	public void f3() {
		RoboticArm r = getActiveRobot();
		
		switch (mode) {
		case NAV_PROGRAMS:
			if (getActiveRobot().numOfPrograms() > 0) {
				nextScreen(ScreenMode.CONFIRM_PROG_DELETE);
			}
			break;
		case NAV_PROG_INSTR:
			Instruction inst = r.getActiveInstruction();
			int selectIdx = getSelectedIdx();

			if (inst instanceof MotionInstruction) {
				r.getInstToEdit( r.getActiveInstIdx() );
				Point pt = nativeRobotEEPoint(r, r.getJointAngles());
				Frame active = r.getActiveFrame(CoordFrame.USER);

				if (active != null) {
					// Convert into currently active frame
					pt = RMath.applyFrame(getActiveRobot(), pt, active.getOrigin(), active.getOrientation());
				}

				Program p = r.getActiveProg();
				int actInst = r.getActiveInstIdx();

				if (getSelectedLine() == 1) {
					// Update the secondary position in a circular motion
					// instruction
					p.updateMCInstPosition(actInst, pt);

				} else {
					// Update the position associated with the active motion
					// instruction
					p.updateMInstPosition(actInst, pt);
				}

				MotionInstruction mInst = (MotionInstruction) inst;

				if (getSelectedLine() > 0) {
					// Update the secondary point
					mInst = mInst.getSecondaryPoint();
				}

				// Update the motion instruction's fields
				CoordFrame coord = r.getCurCoordFrame();

				if (coord == CoordFrame.JOINT) {
					mInst.setMotionType(Fields.MTYPE_JOINT);
					mInst.setSpeed(0.5f);

				} else {
					/*
					 * Keep circular motion instructions as circular motion
					 * instructions in world, tool, or user frame modes
					 */
					if (mInst.getMotionType() == Fields.MTYPE_JOINT) {
						mInst.setMotionType(Fields.MTYPE_LINEAR);
					}

					mInst.setSpeed(50f * r.motorSpeed / 100f);
				}

				mInst.setToolFrame(r.getActiveToolFrame());
				mInst.setUserFrame(r.getActiveUserFrame());

			} else if (inst instanceof IfStatement) {
				IfStatement stmt = (IfStatement) inst;

				if (stmt.getExpr() instanceof Expression && selectIdx >= 2) {
					r.getInstToEdit( r.getActiveInstIdx() );
					((Expression) stmt.getExpr()).insertElement(selectIdx - 3);
					updatePendantScreen();
					arrow_rt();
				}
			} else if (inst instanceof SelectStatement) {
				SelectStatement stmt = (SelectStatement) inst;

				if (selectIdx >= 3) {
					r.getInstToEdit( r.getActiveInstIdx() );
					stmt.addCase();
					updatePendantScreen();
					arrow_dn();
				}
			} else if (inst instanceof RegisterStatement) {
				RegisterStatement stmt = (RegisterStatement) inst;
				int rLen = (stmt.getPosIdx() == -1) ? 2 : 3;

				if (selectIdx > rLen) {
					r.getInstToEdit( r.getActiveInstIdx() );
					stmt.getExpr().insertElement(selectIdx - (rLen + 2));
					updatePendantScreen();
					arrow_rt();
				}
			}

			updatePendantScreen();
			break;
		case SELECT_CUT_COPY:
			Program p = r.getActiveProg();
			int size = p.getNumOfInst();
			clipBoard = new ArrayList<>();

			int remIdx = 0;
			for (int i = 0; i < size; i += 1) {
				
				if (contents.isSelected(i)) {
					clipBoard.add(p.getInstAt(remIdx));
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

	public void f4() {
		RoboticArm r = getActiveRobot();
		Program p = r.getActiveProg();

		switch (mode) {
		case NAV_PROGRAMS:
			if (getActiveRobot().numOfPrograms() > 0) {
				nextScreen(ScreenMode.PROG_COPY);
			}
			break;
		case NAV_PROG_INSTR:
			Instruction ins = getActiveRobot().getActiveInstruction();

			if (ins != null) {
				int selectIdx = getSelectedIdx();
				getEditScreen(ins, selectIdx);
			}

			break;
		case CONFIRM_INSERT:
			try {
				int lines_to_insert = Integer.parseInt(workingText.toString());
				for (int i = 0; i < lines_to_insert; i += 1) {
					p.addInstAt(r.getActiveInstIdx(), new Instruction());
				}
				
				updateInstructions();
			} catch (Exception e) {
				e.printStackTrace();
			}

			lastScreen();
			break;
		case CONFIRM_PROG_DELETE:
			int progIdx = getActiveRobot().getActiveProgIdx();

			if (progIdx >= 0 && progIdx < getActiveRobot().numOfPrograms()) {
				getActiveRobot().removeProgram(progIdx);

				if (getActiveRobot().getActiveProgIdx() >= getActiveRobot().numOfPrograms()) {
					getActiveRobot().setActiveProgIdx(getActiveRobot().numOfPrograms() - 1);
					contents.setLineIdx(min(getActiveRobot().getActiveProgIdx(), ITEMS_TO_SHOW - 1));
				}

				lastScreen();
			}
			break;
		case SELECT_INSTR_DELETE:
			int instrIdx = 0;

			for (int i = 0; i < contents.getSelection().length; i += 1) {
				if (contents.isSelected(i)) {
					p.rmInstAt(instrIdx);
				} else {
					instrIdx += 1;
				}
			}

			display_stack.pop();
			updateInstructions();
			break;
		case SELECT_CUT_COPY:
			clipBoard = new ArrayList<>();

			for (int i = 0; i < p.getNumOfInst(); i += 1) {
				if (contents.isSelected(i))
					clipBoard.add(p.getInstAt(i).clone());
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

			display_stack.pop();
			getActiveRobot().setActiveInstIdx(lineIdx);
			updateInstructions();
			break;
		case SELECT_COMMENT:
			display_stack.pop();
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
				Instruction instr = p.getInstAt(i);

				if (instr instanceof MotionInstruction) {
					// Update the primary position
					MotionInstruction mInst = ((MotionInstruction) instr);
					p.setPosition(posIdx, pTemp[mInst.getPositionNum()]);
					mInst.setPositionNum(posIdx++);

					if (mInst.getMotionType() == Fields.MTYPE_CIRCULAR && mInst.getSecondaryPoint() != null) {

						/*
						 * Update position for secondary point of a circular
						 * motion instruction
						 */
						mInst = mInst.getSecondaryPoint();
						p.setPosition(posIdx, pTemp[mInst.getPositionNum()]);
						mInst.setPositionNum(posIdx++);
					}
				}
			}

			display_stack.pop();
			updateInstructions();
			break;
		case NAV_MACROS:
			edit_macro = macros.get(contents.getLineIdx());

			if (contents.getColumnIdx() == 1) {
				nextScreen(ScreenMode.SET_MACRO_PROG);
			} else if (contents.getColumnIdx() == 2) {
				nextScreen(ScreenMode.SET_MACRO_TYPE);
			} else {
				if (!macros.get(contents.getLineIdx()).isManual())
					nextScreen(ScreenMode.SET_MACRO_BINDING);
			}
			break;
		case NAV_PREGS:
			if (isShift() && !isProgramRunning()) {
				// Stop any prior jogging motion
				hold();

				// Move To function
				PositionRegister pReg = r.getPReg( contents.getActiveIndex() );

				if (pReg.point != null) {
					System.out.println(pReg.point);
					Point pt = pReg.point.clone();
					// Move the Robot to the select point
					if (pReg.isCartesian) {
						
						if (r.getCurCoordFrame() == CoordFrame.USER) {
							// Move in terms of the user frame
							Frame active = r.getActiveFrame(CoordFrame.USER);
							pt = RMath.removeFrame(r, pt, active.getOrigin(), active.getOrientation());

							if (Fields.DEBUG) {
								System.out.printf("pt: %s\n", pt.position.toString());
							}
						}

						r.moveTo(pt.position, pt.orientation);

					} else {
						r.moveTo(pt.angles);
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
							getActiveRobot().moveTo(tgt.position, tgt.orientation);
						}
					} else {
						if (tgt != null && tgt.angles != null) {
							// Move to the point's joint angles
							getActiveRobot().moveTo(tgt.angles);
						}
					}
				}
			} else if (mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
				editTextEntry(3);
			}

		}

		updatePendantScreen();
	}

	public void f5() {
		RoboticArm r = getActiveRobot();
		Instruction inst = r.getActiveInstruction();
		
		switch (mode) {
		case NAV_PROG_INSTR:
			int selectLine = getSelectedLine();
			int selectIdx = getSelectedIdx();	

			if (selectIdx == 0) {
				nextScreen(ScreenMode.NAV_INSTR_MENU);
			} else if (inst instanceof MotionInstruction) {
				if (selectIdx == 3 || (contents.getColumnIdx() == 0 && selectLine == 1)) {
					nextScreen(ScreenMode.EDIT_MINST_POS);
				}
			} else if (inst instanceof IfStatement) {
				IfStatement stmt = (IfStatement) inst;
				if (stmt.getExpr() instanceof Expression) {
					r.getInstToEdit( r.getActiveInstIdx() );
					((Expression) stmt.getExpr()).removeElement(selectIdx - 3);
				}
			} else if (inst instanceof SelectStatement) {
				SelectStatement stmt = (SelectStatement) inst;
				if (selectIdx >= 3) {
					r.getInstToEdit( r.getActiveInstIdx() );
					stmt.deleteCase((selectIdx - 3) / 3);
				}
			} else if (inst instanceof RegisterStatement) {
				RegisterStatement stmt = (RegisterStatement) inst;
				int rLen = (stmt.getPosIdx() == -1) ? 2 : 3;
				
				if (selectIdx > (rLen + 1) && selectIdx < stmt.getExpr().getLength() + rLen) {
					r.getInstToEdit( r.getActiveInstIdx() );
					stmt.getExpr().removeElement(selectIdx - (rLen + 2));
				}
			}
			break;
		case EDIT_MINST_POS:
			MotionInstruction m;

			if (inst instanceof MotionInstruction) {
				m = (MotionInstruction) inst;

			} else {
				m = null;
			}

			if (getSelectedIdx() == 3) {
				m.toggleOffsetActive();
			} else {
				m.getSecondaryPoint().toggleOffsetActive();
			}

			switchScreen(ScreenMode.SET_MV_INSTR_OFFSET);
			break;
		case TEACH_3PT_USER:
		case TEACH_3PT_TOOL:
		case TEACH_4PT:
		case TEACH_6PT:
			if (isShift()) {
				// Save the Robot's current position and joint angles
				Point pt;

				if (mode == ScreenMode.TEACH_3PT_USER || mode == ScreenMode.TEACH_4PT) {
					pt = nativeRobotEEPoint(getActiveRobot(), getActiveRobot().getJointAngles());
				} else {
					pt = nativeRobotPoint(getActiveRobot(), getActiveRobot().getJointAngles());
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
			display_stack.pop();
			updateInstructions();
			break;
		case NAV_PREGS:
			PositionRegister pReg = r.getPReg( contents.getActiveIndex() );

			if (isShift() && pReg != null) {
				// Save the Robot's current position and joint angles
				Point curRP = nativeRobotEEPoint(r, r.getJointAngles());
				Frame active = r.getActiveFrame(CoordFrame.USER);

				if (active != null) {
					// Save Cartesian values in terms of the active User frame
					curRP = RMath.applyFrame(r, curRP, active.getOrigin(), active.getOrientation());
				}

				pReg.point = curRP;
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

	public void FrontView() {
		// Default view
		camera.reset();
	}

	public void fwd() {
		if (mode == ScreenMode.NAV_PROG_INSTR && !isProgramRunning() && isShift()) {
			// Stop any prior Robot movement
			hold();
			// Safeguard against editing a program while it is running
			contents.setColumnIdx(0);

			setExecutingInstruction(false);
			// Run single instruction when step is set
			execSingleInst = isStep();

			setProgramRunning(true);
		}
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

	public MenuScroll getContentsMenu() {
		return contents;
	}

	/*
	 * This method transforms the given coordinates into a vector in the
	 * Processing's native coordinate system.
	 */
	public PVector getCoordFromMatrix(float x, float y, float z) {
		PVector vector = new PVector();

		vector.x = modelX(x, y, z);
		vector.y = modelY(x, y, z);
		vector.z = modelZ(x, y, z);

		return vector;
	}

	public void getEditScreen(Instruction ins, int selectIdx) {
		if (ins instanceof MotionInstruction) {
			if (getSelectedLine() == 0) {
				// edit movement instruction line 1
				switch (contents.getColumnIdx()) {
				case 2: // motion type
					nextScreen(ScreenMode.SET_MV_INSTR_TYPE);
					break;
				case 3: // register type
					nextScreen(ScreenMode.SET_MV_INSTR_REG_TYPE);
					break;
				case 4: // register
					nextScreen(ScreenMode.SET_MV_INSTR_IDX);
					break;
				case 5: // speed
					nextScreen(ScreenMode.SET_MV_INSTR_SPD);
					break;
				case 6: // termination type
					nextScreen(ScreenMode.SET_MV_INSTR_TERM);
					break;
				case 7: // offset register
					nextScreen(ScreenMode.SET_MV_INSTR_OFFSET);
					break;
				}
			} else {
				// edit movement instruciton line 2 (circular only)
				switch (contents.getColumnIdx()) {
				case 0: // register type
					nextScreen(ScreenMode.SET_MV_INSTR_REG_TYPE);
					break;
				case 1: // register
					nextScreen(ScreenMode.SET_MV_INSTR_IDX);
					break;
				case 2: // speed
					nextScreen(ScreenMode.SET_MV_INSTR_SPD);
					break;
				case 3: // termination type
					nextScreen(ScreenMode.SET_MV_INSTR_TERM);
					break;
				case 4: // offset register
					nextScreen(ScreenMode.SET_MV_INSTR_OFFSET);
					break;
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
						editIdx = ((CallInstruction) stmt.getInstr()).getTgtDevice().RID;
						nextScreen(ScreenMode.SET_CALL_PROG);
					}
				}
			} else if (stmt.getExpr() instanceof AtomicExpression) {
				if (selectIdx == 2) {
					opEdit = stmt.getExpr().getArg1();
					nextScreen(ScreenMode.SET_BOOL_EXPR_ARG);
				} else if (selectIdx == 3) {
					opEdit = stmt.getExpr();
					nextScreen(ScreenMode.SET_EXPR_OP);
				} else if (selectIdx == 4) {
					opEdit = stmt.getExpr().getArg2();
					nextScreen(ScreenMode.SET_BOOL_EXPR_ARG);
				} else if (selectIdx == 5) {
					nextScreen(ScreenMode.SET_IF_STMT_ACT);
				} else {
					if (stmt.getInstr() instanceof JumpInstruction) {
						nextScreen(ScreenMode.SET_JUMP_TGT);
					} else if (stmt.getInstr() instanceof CallInstruction) {
						editIdx = ((CallInstruction) stmt.getInstr()).getTgtDevice().RID;
						nextScreen(ScreenMode.SET_CALL_PROG);
					}
				}
			}
		} else if (ins instanceof SelectStatement) {
			SelectStatement stmt = (SelectStatement) ins;

			if (selectIdx == 2) {
				opEdit = stmt.getArg();
				nextScreen(ScreenMode.SET_SELECT_STMT_ARG);
			} else if ((selectIdx - 3) % 3 == 0 && selectIdx > 2) {
				opEdit = stmt.getCases().get((selectIdx - 3) / 3);
				nextScreen(ScreenMode.SET_SELECT_STMT_ARG);
			} else if ((selectIdx - 3) % 3 == 1) {
				editIdx = (selectIdx - 3) / 3;
				nextScreen(ScreenMode.SET_SELECT_STMT_ACT);
			} else if ((selectIdx - 3) % 3 == 2) {
				editIdx = (selectIdx - 3) / 3;
				Instruction toExec = stmt.getInstrs().get(editIdx);
				if (toExec instanceof JumpInstruction) {
					nextScreen(ScreenMode.SET_JUMP_TGT);
				} else if (toExec instanceof CallInstruction) {
					editIdx = ((CallInstruction) toExec).getTgtDevice().RID;
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

	/**
	 * @return The active End Effector mapping state
	 */
	public EEMapping getEEMapping() {
		return UI.getEEMapping();
	}

	// Function label text
	public String[] getFunctionLabels(ScreenMode mode) {
		String[] funct = new String[5];

		switch (mode) {
		case NAV_PROGRAMS:
			// F2, F3
			funct[0] = "[Create]";
			if (getActiveRobot().numOfPrograms() > 0) {
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
			Instruction inst = getActiveRobot().getActiveInstruction();

			// F1, F4, F5f
			funct[0] = "[New Pt]";
			funct[1] = "[New Ins]";
			funct[2] = (inst instanceof MotionInstruction) ? "[Ovr Pt]" : "";
			funct[3] = "[Edit]";
			funct[4] = (contents.getColumnIdx() == 0) ? "[Opt]" : "";
			if (inst instanceof MotionInstruction) {
				funct[4] = (contents.getColumnIdx() == 3) ? "[Reg]" : funct[4];
			} else if (inst instanceof IfStatement) {
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
			} else if (inst instanceof SelectStatement) {
				int selectIdx = getSelectedIdx();

				if (selectIdx >= 3) {
					funct[2] = "[Insert]";
					funct[4] = "[Delete]";
				}
			} else if (inst instanceof RegisterStatement) {
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
		case EDIT_MINST_POS:
			funct[0] = "";
			funct[1] = "";
			funct[2] = "";
			funct[3] = "";
			funct[4] = "[Offset]";
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
		case UFRAME_DETAIL:
			// F2
			funct[0] = "";
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
		case SET_MV_INSTR_SPD:
		case SET_MV_INSTR_IDX:
		case SET_MV_INSTR_TERM:
		case SET_MV_INSTR_OFFSET:
		case SELECT_INSTR_INSERT:
		case SET_IO_INSTR_STATE:
		case SET_FRM_INSTR_TYPE:
		case SET_FRAME_INSTR_IDX:
		case SET_EXPR_ARG:
		case SET_BOOL_EXPR_ARG:
		case SET_JUMP_TGT:
		case SELECT_CUT_COPY:
		case SELECT_INSTR_DELETE:
			header = activeRobot.getActiveProg().getName();
			break;
		case EDIT_MINST_POS:
			Program p = activeRobot.getActiveProg();
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
		case TFRAME_DETAIL:
			header = String.format("TOOL FRAME: %d", curFrameIdx + 1);
			break;
		case UFRAME_DETAIL:
			header = String.format("USER FRAME: %d", curFrameIdx + 1);
			break;
		case FRAME_METHOD_TOOL:
			header = String.format("TOOL FRAME: %d", curFrameIdx + 1);
			break;
		case FRAME_METHOD_USER:
			header = String.format("USER FRAME: %d", curFrameIdx + 1);
			break;
		case TEACH_3PT_TOOL:
		case TEACH_3PT_USER:
			header = "THREE POINT METHOD";
			break;
		case TEACH_4PT:
			header = "FOUR POINT METHOD";
			break;
		case TEACH_6PT:
			header = "SIX POINT METHOD";
			break;
		case DIRECT_ENTRY_TOOL:
		case DIRECT_ENTRY_USER:
			header = "DIRECT ENTRY METHOD";
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
		case CP_DREG_COM:
		case CP_DREG_VAL:
			header = "REGISTERS";
			break;
		case NAV_PREGS:
			header = "POSTION REGISTERS";
			break;
		case CP_PREG_COM:
		case CP_PREG_PT:
		case SWAP_PT_TYPE:
			header = "POSTION REGISTERS";
			break;
		case EDIT_DREG_VAL:
			header = "DATA REGISTER: ";
			String dRegComm = getActiveRobot().getDReg(active_index).comment;

			if (dRegComm != null) {
				// Show comment if it exists
				header += dRegComm;
			} else {
				header += Integer.toString(active_index + 1);
			}

			break;
		case EDIT_PREG:
			header = "POSITION REGISTER: ";
			String pRegComm = getActiveRobot().getPReg(active_index).comment;

			if (pRegComm != null) {
				// Show comment if it exists
				header += pRegComm;
			} else {
				header += Integer.toString(active_index + 1);
			}

			break;
		case EDIT_DREG_COM:
			header = String.format("Enter a name for R[%d]", active_index + 1);
			break;

		case EDIT_PREG_COM:
			header = String.format("Enter a name for PR[%d]", active_index + 1);
			break;

		case NAV_IOREG:
			header = "IO Registers";
			break;

		case EDIT_IOREG:
			header = "SET IO REGISTER";
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
		ArrayList<DisplayLine> instr = loadInstructions(getActiveRobot().getActiveProgIdx());
		int row = instrIdx;

		while (instr.get(row).getItemIdx() != instrIdx) {
			row += 1;
			if (contents.getLineIdx() >= contents.size() - 1)
				break;
		}

		return row;
	}

	public WGUI getUI() {
		return UI;
	}
	
	public ScreenMode getMode() {
		return mode;
	}
	
	public MenuScroll getOptionsMenu() {
		return options;
	}

	public int getRecord() {
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

	/**
	 * Copies the current rotation on the top matrix of Processing's matrix
	 * stack to a 3x3 floating-point array.
	 * 
	 * @return	A row major orthogonal rotation matrix
	 */
	public float[][] getRotationMatrix() {
		float[][] rMatrix = new float[3][3];
		// Pull the origin and axes vectors from the matrix stack
		PVector origin = getCoordFromMatrix(0f, 0f, 0f),
				vx = getCoordFromMatrix(1f, 0f, 0f).sub(origin),
				vy = getCoordFromMatrix(0f, 1f, 0f).sub(origin),
				vz = getCoordFromMatrix(0f, 0f, 1f).sub(origin);
		
		rMatrix[0][0] = vx.x;
		rMatrix[0][1] = vx.y;
		rMatrix[0][2] = vx.z;
		rMatrix[1][0] = vy.x;
		rMatrix[1][1] = vy.y;
		rMatrix[1][2] = vy.z;
		rMatrix[2][0] = vz.x;
		rMatrix[2][1] = vz.y;
		rMatrix[2][2] = vz.z;

		return rMatrix;
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
	public float[][] getTransformationMatrix() {
		float[][] transform = new float[4][4];

		PVector origin = getCoordFromMatrix(0, 0, 0);
		PVector xAxis = getCoordFromMatrix(1, 0, 0).sub(origin);
		PVector yAxis = getCoordFromMatrix(0, 1, 0).sub(origin);
		PVector zAxis = getCoordFromMatrix(0, 0, 1).sub(origin);

		transform[0][0] = xAxis.x;
		transform[0][1] = xAxis.y;
		transform[0][2] = xAxis.z;
		transform[0][3] = 0;
		transform[1][0] = yAxis.x;
		transform[1][1] = yAxis.y;
		transform[1][2] = yAxis.z;
		transform[1][3] = 0;
		transform[2][0] = zAxis.x;
		transform[2][1] = zAxis.y;
		transform[2][2] = zAxis.z;
		transform[2][3] = 0;
		
		transform[3][0] = origin.x;
		transform[3][1] = origin.y;
		transform[3][2] = origin.z;
		transform[3][3] = 1;

		return transform;
	}

	/**
	 * Stops all robot motion and program execution.
	 */
	public void hold() {
		// Stop all robot motion and program execution
		UI.resetJogButtons();
		activeRobot.halt();
		setProgramRunning(false);
	}

	public void io() {
		if (isShift()) {
			if (getSU_macro_bindings()[6] != null) {
				getSU_macro_bindings()[6].execute();
			}

		} else {
			if (!isProgramRunning()) {
				// Map I/O to the robot's end effector state, if shift is off
				activeRobot.toggleEEState();
			}
		}
	}

	public boolean isExecutingInstruction() {
		return executingInstruction;
	}

	public boolean isProgramRunning() {
		return programRunning;
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

	public void item() {
		if (mode == ScreenMode.NAV_PROG_INSTR) {
			workingText = new StringBuilder();
			nextScreen(ScreenMode.JUMP_TO_LINE);
		}
	}

	public void joint1_neg() {
		updateRobotJogMotion(0, -1);
	}

	public void joint1_pos() {
		updateRobotJogMotion(0, 1);
	}

	public void joint2_neg() {
		updateRobotJogMotion(1, -1);
	}

	public void joint2_pos() {
		updateRobotJogMotion(1, 1);
	}

	public void joint3_neg() {
		updateRobotJogMotion(2, -1);
	}

	public void joint3_pos() {
		updateRobotJogMotion(2, 1);
	}

	public void joint4_neg() {
		updateRobotJogMotion(3, -1);
	}

	public void joint4_pos() {
		updateRobotJogMotion(3, 1);
	}

	public void joint5_neg() {
		updateRobotJogMotion(4, -1);
	}

	public void joint5_pos() {
		updateRobotJogMotion(4, 1);
	}

	public void joint6_neg() {
		updateRobotJogMotion(5, -1);
	}

	public void joint6_pos() {
		updateRobotJogMotion(5, 1);
	}

	@Override
	public void keyPressed() {
		
		if (key == 'd') {
			// Debug output key
			if (mode == ScreenMode.INPUT_PREG_IDX2 || mode == ScreenMode.INPUT_PREG_IDX1) {
				
				updatePendantScreen();
				System.out.println(options);
				
			} else {
				
				System.out.println(isShift());
				
			}
			
		}

		if (key == 27) {
			// Disable the window exiting function of the 'esc' key
			key = 0;
		} else if (UI != null && UI.isATextFieldActive()) {
			// Disable key events when typing in a text field
			return;

		} else if (UI != null && UI.isPendantActive()) {
			if (mode.getType() == ScreenType.TYPE_NUM_ENTRY || mode.getType() == ScreenType.TYPE_POINT_ENTRY
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
				case KeyEvent.VK_ENTER:			enter(); break;
				case KeyEvent.VK_BACK_SPACE:	bkspc(); break;
				case KeyEvent.VK_DOWN:			arrow_dn(); break;
				case KeyEvent.VK_LEFT:			arrow_lt(); break;
				case KeyEvent.VK_RIGHT:			arrow_rt(); break;
				case KeyEvent.VK_UP:			arrow_up(); break;
			}
		}
		
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
		case KeyEvent.VK_CONTROL:	ctrl = true; break;
		}

		// General key functions
		if (key == 'f') {
			// Display the User and Tool frames associated with the current
			// motion instruction
			if (Fields.DEBUG && mode == ScreenMode.NAV_PROG_INSTR
					&& (contents.getColumnIdx() == 3 || contents.getColumnIdx() == 4)) {
				Instruction inst = getActiveRobot().getActiveInstruction();

				if (inst instanceof MotionInstruction) {
					MotionInstruction mInst = (MotionInstruction) inst;
					System.out.printf("\nUser frame: %d\nTool frame: %d\n", mInst.getUserFrame(), mInst.getToolFrame());
				}
			}

		} else if (key == 'a') {
			// Test instruction undo
			if (mode == ScreenMode.NAV_PROG_INSTR) {
				getActiveRobot().popInstructionUndo();
				updatePendantScreen();
			}
			
		} else if (ctrl && keyCode == KeyEvent.VK_T) {
			// Write anything stored in the String buffer to a text file
			writeBuffer();
		} else if (ctrl && keyCode == KeyEvent.VK_S) {
			// Save EVERYTHING!
			DataManagement.saveState(this);
		} else if (ctrl && keyCode == KeyEvent.VK_Z) {
			undoScenarioEdit();
			
		} else if (key == 'q') {
			// Print the current mode to the console
			println(mode.toString());
		} else if (key == 'e') {
			// Toggle the Robot's End Effector state
			if (!isProgramRunning()) {
				getActiveRobot().toggleEEState();
			}
		} else if (key == 'r') {
			// Restore default Robot joint angles
			float[] rot = { 0, 0, 0, 0, 0, 0 };
			getActiveRobot().releaseHeldObject();
			getActiveRobot().setJointAngles(rot);
			intermediatePositions.clear();
			
		}
	}

	public void keyReleased() {
		
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
		case KeyEvent.VK_CONTROL:	ctrl = false; break;
			}
	}

	/**
	 * Transitions the display to the previous screen that the user was on.
	 */
	public boolean lastScreen() {
		ScreenState cur = display_stack.peek();
		
		if (cur.mode != ScreenMode.DEFAULT) {
			display_stack.pop();
			cur = display_stack.peek();
			
			if (Fields.DEBUG) {
				System.out.printf("%s => %s\n", mode, cur);
			}
			// Update mode and menu indices
			mode = cur.mode;
			contents.clear();
			contents.setLineIdx(cur.conLnIdx);
			contents.setColumnIdx(cur.conColIdx);
			contents.setRenderStart(cur.conRenIdx);
			
			options.clear();
			options.setLineIdx(cur.optLnIdx);
			options.setRenderStart(cur.optRenIdx);
			
			updatePendantScreen();
			return true;
		}
		
		if (Fields.DEBUG) {
			System.out.printf("%s\n", cur.mode);
		}
		
		return false;
	}

	public void LeftView() {
		// Left view
		camera.reset();
		camera.rotate(0, PI / 2f, 0);
	}

	public void loadDataRegisters() {
		// View Registers or Position Registers
		// int start = start_render;
		// int end = min(start + ITEMS_TO_SHOW, DREG.length);

		// Display a subset of the list of registers
		for (int idx = 0; idx < Fields.DPREG_NUM; ++idx) {
			DataRegister reg = activeRobot.getDReg(idx);

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

			contents.addLine(new DisplayLine(idx, 0 , regLbl, regEntry));
		}
	}

	/**
	 * Transitions to the Frame Details menu, which displays the x, y, z, w, p,
	 * r values associated with the Frame at curFrameIdx in either the Tool
	 * Frames or User Frames, based on the value of super_mode.
	 */
	public void loadFrameDetail(CoordFrame coordFrame) {
		// Display the frame set name as well as the index of the currently
		// selected frame
		if (coordFrame == CoordFrame.TOOL) {
			String[] fields = getActiveRobot().getToolFrame(curFrameIdx).toLineStringArray();
			// Place each value in the frame on a separate lien
			for (String field : fields) {
				contents.addLine(field);
			}

		} else if (coordFrame == CoordFrame.USER) {
			String[] fields = getActiveRobot().getUserFrame(curFrameIdx).toLineStringArray();
			// Place each value in the frame on a separate lien
			for (String field : fields) {
				contents.addLine(field);
			}

		}
	}

	/**
	 * Takes the values associated with the given Frame's direct entry values
	 * (X, Y, Z, W, P, R) and fills a 2D ArrayList, where the first column is
	 * the prefix for the value in the second column.
	 * 
	 * @param f
	 *            The frame to be displayed for editing
	 * @returning A 2D ArrayList with the prefixes and values associated with
	 *            the Frame
	 */
	public void loadFrameDirectEntry(Frame f) {
		String[][] entries = f.directEntryStringArray();

		for (int idx = 0; idx < entries.length; ++idx) {
			String[] line = new String[entries[idx][1].length() + 1];
			line[0] = entries[idx][0];
			// Give each character in the value String it own column
			for (int sdx = 0; sdx < entries[idx][1].length(); ++sdx) {
				line[sdx + 1] = Character.toString(entries[idx][1].charAt(sdx));
			}

			contents.addLine(line);
		}
	}

	/**
	 * Loads the set of Frames that correspond to the given coordinate frame.
	 * Only TOOL and USER have Frames sets as of now.
	 * 
	 * @param coorFrame
	 *            the integer value representing the coordinate frame of the
	 *            desired frame set
	 */
	public void loadFrames(CoordFrame coordFrame) {
		if (coordFrame == CoordFrame.TOOL) {
			// Display Tool frames
			for (int idx = 0; idx < Fields.FRAME_NUM; idx += 1) {
				// Display each frame on its own line
				String[] strArray = getActiveRobot().getToolFrame(idx).toLineStringArray();
				contents.addLine(idx, String.format("%-4s %s", String.format("%d)", idx + 1), strArray[0]));
				contents.addLine(idx, String.format("%s", strArray[1]));
				contents.get(idx * 2 + 1).setxAlign(38);
			}

		} else {
			// Display User frames
			for (int idx = 0; idx < Fields.FRAME_NUM; idx += 1) {
				// Display each frame on its own line
				String[] strArray = getActiveRobot().getUserFrame(idx).toLineStringArray();
				contents.addLine(idx, String.format("%-4s %s", String.format("%d)", idx + 1), strArray[0]));
				contents.addLine(idx, String.format("%s", strArray[1]));
				contents.get(idx * 2 + 1).setxAlign(38);
			}
		}
	}

	public void loadInstrEdit(ScreenMode mode) {
		switch (mode) {
		case SET_MV_INSTR_TYPE:
			options.addLine("1.JOINT");
			options.addLine("2.LINEAR");
			options.addLine("3.CIRCULAR");
			break;
		case SET_MV_INSTR_REG_TYPE:
			options.addLine("1.LOCAL(P)");
			options.addLine("2.GLOBAL(PR)");
			break;
		case SET_MV_INSTR_IDX:
			options.addLine("Enter desired position/ register:");
			options.addLine("\0" + workingText);
			break;
		case SET_MV_INSTR_SPD:
			Instruction inst = getActiveRobot().getActiveInstruction();
			MotionInstruction castIns;

			if (inst instanceof MotionInstruction) {
				castIns = (MotionInstruction) inst;

			} else {
				castIns = null;
			}

			options.addLine("Enter desired speed:");

			if (castIns.getMotionType() == Fields.MTYPE_JOINT) {
				speedInPercentage = true;
				workingTextSuffix = "%";
			} else {
				workingTextSuffix = "mm/s";
				speedInPercentage = false;
			}

			options.addLine(workingText + workingTextSuffix);
			break;
		case SET_MV_INSTR_TERM:
			options.addLine("Enter desired termination %(0-100):");
			options.addLine("\0" + workingText);
			break;
		case SET_MV_INSTR_OFFSET:
			options.addLine("Enter desired offset register (1-1000):");
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
				if (getActiveRobot().getActiveInstruction() instanceof IfStatement) {
					options.addLine("1. + ");
					options.addLine("2. - ");
					options.addLine("3. * ");
					options.addLine("4. / ");
					options.addLine("5. | ");
					options.addLine("6. % ");
					options.addLine("7. = ");
					options.addLine("8. <> ");
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
					options.addLine("4. / ");
					options.addLine("5. | ");
					options.addLine("6. % ");
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
			options.addLine("RCALL");
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
		default:
			break;
		}
	}

	public void loadInstructionReg() {
		// show register contents if you're highlighting a register
		Instruction ins = getActiveRobot().getActiveInstruction();
		if (ins instanceof MotionInstruction) {
			MotionInstruction castIns = (MotionInstruction) ins;
			Point p = castIns.getPoint(getActiveRobot().getActiveProg());

			if (p != null) {
				options.addLine("Position values (press ENTER to exit):");

				String[] regEntry = p.toLineStringArray(castIns.getMotionType() != Fields.MTYPE_JOINT);

				for (String line : regEntry) {
					contents.addLine(line);
				}

				if (castIns.getUserFrame() != -1) {
					Frame uFrame = getActiveRobot().getUserFrame(castIns.getUserFrame());
					displayPoint = RMath.removeFrame(getActiveRobot(), p, uFrame.getOrigin(), uFrame.getOrientation());

				} else {
					displayPoint = p;
				}
			}

		} else {
			contents.addLine("This position is empty (press ENTER to exit):");
		}
	}

	// prepare for displaying motion instructions on screen
	public ArrayList<DisplayLine> loadInstructions(int programID) {
		ArrayList<DisplayLine> instruct_list = new ArrayList<>();
		int tokenOffset = Fields.TXT_PAD - Fields.PAD_OFFSET;

		Program p = getActiveRobot().getProgram(programID);
		int size = p.getNumOfInst();

		for (int i = 0; i < size; i += 1) {
			DisplayLine line = new DisplayLine(i);
			Instruction instr = p.getInstAt(i);
			int xPos = 10;

			// Add line number
			if (instr == null) {
				line.add(String.format("%d) ...", i + 1));
				continue;
			} else if (instr.isCommented()) {
				line.add("//" + Integer.toString(i + 1) + ")");
			} else {
				line.add(Integer.toString(i + 1) + ")");
			}

			int numWdth = line.get(line.size() - 1).length();
			xPos += numWdth * Fields.CHAR_WDTH + tokenOffset;

			if (instr instanceof MotionInstruction) {
				// Show '@' at the an instrution, if the Robot's position is
				// close to that position stored in the instruction's register
				MotionInstruction a = (MotionInstruction) instr;
				Point ee_point = nativeRobotEEPoint(getActiveRobot(), getActiveRobot().getJointAngles());
				Point instPt = a.getVector(p);

				if (instPt != null && ee_point.position.dist(instPt.position) < (activeRobot.getLiveSpeed() / 100f)) {
					line.add("@");
				} else {
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

		if (mode.getType() != ScreenType.TYPE_LINE_SELECT) {
			DisplayLine endl = new DisplayLine(size);
			endl.add("[End]");

			instruct_list.add(endl);
		}

		return instruct_list;
	}

	public void loadIORegisters() {
		for (int i = 0; i < Fields.IOREG_NUM; i += 1) {
			IORegister ioReg = getActiveRobot().getIOReg(i);
			String ee;

			if (ioReg.comment != null) {
				ee = ioReg.comment;
			} else {
				ee = "UINIT";
			}

			options.addLine(String.format("IO[%d:%-8s] = ", i + 1, ee), "ON", "OFF");
		}
	}

	public void loadIORegistersIntoContents() {
		for (int idx = 0; idx < Fields.IOREG_NUM; ++idx) {
			IORegister ioReg = activeRobot.getIOReg(idx);
			contents.addLine(String.format("IO[%s] = ", ioReg.comment), (ioReg.state == 0) ? "OFF" : "ON");

		}
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
	 * Displays the points along with their respective titles for the current
	 * frame teach method (discluding the Direct Entry method).
	 */
	public void loadPointList() {
		if (teachFrame != null) {

			ArrayList<String> temp = new ArrayList<>();
			// Display TCP teach points
			if (mode == ScreenMode.TEACH_3PT_TOOL || mode == ScreenMode.TEACH_6PT) {
				temp.add("First Approach Point: ");
				temp.add("Second Approach Point: ");
				temp.add("Third Approach Point: ");
			}
			// Display Axes Vectors teach points
			if (mode == ScreenMode.TEACH_3PT_USER || mode == ScreenMode.TEACH_4PT || mode == ScreenMode.TEACH_6PT) {
				temp.add("Orient Origin Point: ");
				temp.add("X Axis Point: ");
				temp.add("Y Axis Point: ");
			}
			// Display origin offset point
			if (mode == ScreenMode.TEACH_4PT) {
				// Name of fourth point for the four point method?
				temp.add("Origin: ");
			}

			// Determine if the point has been set yet
			for (int idx = 0; idx < temp.size(); ++idx) {
				// Add each line to options
				options.addLine(temp.get(idx) + ((teachFrame.getPoint(idx) != null) ? "RECORDED" : "UNINIT"));
			}
		} else {
			// No teach points
			options.addLine("Error: teachFrame not set!");
		}
	}

	public void loadPosition(Point pt, boolean isCartesian) {

		if (pt == null) {
			throw new NullPointerException("No point given!");

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

				contents.addLine(line);
			}
		}
	}

	/**
	 * Loads all position registers into the contents of the pendant display.
	 */
	public void loadPositionRegisters() {
		// Display a subset of the list of registers
		for (int idx = 0; idx < Fields.DPREG_NUM; ++idx) {
			DisplayLine line;
			PositionRegister reg = activeRobot.getPReg(idx);
			// Display the comment associated with a specific Register entry
			String regLbl = reg.toStringWithComm();
			// Display Register edit prompt (* if uninitialized)
			String regEntry = (reg.point == null) ? "*" : "...Edit...";
			
			contents.addLine( new DisplayLine(idx, 0, regLbl, regEntry) );
		}
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
	 * Loads all the models for a robot.
	 * 
	 * @return A list of the models for the robot
	 */
	private PShape[] loadRobotModels() {
		PShape[] models = new PShape[13];
		// End Effectors
		models[0] = loadSTLModel("SUCTION.stl", color(108, 206, 214));
		models[1] = loadSTLModel("GRIPPER.stl", color(108, 206, 214));
		models[2] = loadSTLModel("PINCER.stl", color(200, 200, 0));
		models[2].scale(1f);
		models[3] = loadSTLModel("POINTER.stl", color(108, 206, 214));
		models[4] = loadSTLModel("GLUE_GUN.stl", color(108, 206, 214));
		models[5] = loadSTLModel("WIELDER.stl", color(108, 206, 214));

		// Body/joint models
		models[6] = loadSTLModel("ROBOT_MODEL_1_BASE.STL", color(200, 200, 0));
		models[7] = loadSTLModel("ROBOT_MODEL_1_AXIS1.STL", color(40, 40, 40));
		models[8] = loadSTLModel("ROBOT_MODEL_1_AXIS2.STL", color(200, 200, 0));
		models[9] = loadSTLModel("ROBOT_MODEL_1_AXIS3.STL", color(40, 40, 40));
		models[10] = loadSTLModel("ROBOT_MODEL_1_AXIS4.STL", color(40, 40, 40));
		models[11] = loadSTLModel("ROBOT_MODEL_1_AXIS5.STL", color(200, 200, 0));
		models[12] = loadSTLModel("ROBOT_MODEL_1_AXIS6.STL", color(40, 40, 40));

		return models;
	}
	
	/**
	 * TODO comment
	 * 
	 * @param m
	 */
	public void loadScreen(ScreenMode m) {
		ScreenState prev = display_stack.peek();
		
		setProgramRunning(false);
		contents.reset();
		options.reset();
		
		MotionInstruction mInst;
		{
			Instruction inst = getActiveRobot().getActiveInstruction();

			if (inst instanceof MotionInstruction) {
				mInst = (MotionInstruction) inst;

			} else {
				mInst = null;
			}
		}
		;

		switch (mode) {
		// Main menu
		case NAV_MAIN_MENU:
			active_index = 0;
			contents.setColumnIdx(0);
			break;
		case NAV_IOREG:
			contents.setColumnIdx(1);
			break;
			// Frames
		case ACTIVE_FRAMES:
			contents.setLineIdx(0);
			contents.setColumnIdx(1);
			workingText = new StringBuilder(Integer.toString(getActiveRobot().getActiveToolFrame() + 1));
			break;
		case SELECT_FRAME_MODE:
			active_index = 0;
			break;
		case NAV_TOOL_FRAMES:
		case NAV_USER_FRAMES:
			contents.setLineIdx(active_index * 2);
			contents.setColumnIdx(0);
			break;

		case TEACH_3PT_USER:
		case TEACH_4PT:
			loadPointList();
		case UFRAME_DETAIL:
			loadFrameDetail(CoordFrame.USER);
			break;

		case TEACH_3PT_TOOL:
		case TEACH_6PT:
			loadPointList();
		case TFRAME_DETAIL:
			loadFrameDetail(CoordFrame.TOOL);
			break;

		case FRAME_METHOD_TOOL:
			loadFrameDetail(CoordFrame.TOOL);
			contents.setLineIdx(-1);
			contents.setColumnIdx(-1);
			break;
		case FRAME_METHOD_USER:
			loadFrameDetail(CoordFrame.USER);
			contents.setLineIdx(-1);
			contents.setColumnIdx(-1);
			break;

			// Programs and instructions
		case NAV_PROGRAMS:
			contents.setLineIdx(getActiveRobot().getActiveProgIdx());
			contents.setColumnIdx(0);
			getActiveRobot().setActiveInstIdx(0);
			break;
		case PROG_CREATE:
			contents.setLineIdx(1);
			contents.setColumnIdx(0);
			workingText = new StringBuilder("\0");
			System.out.println(workingText.length());
			break;
		case PROG_RENAME:
			getActiveRobot().setActiveProgIdx(contents.getLineIdx());
			contents.setLineIdx(1);
			contents.setColumnIdx(0);
			workingText = new StringBuilder(getActiveRobot().getActiveProg().getName());
			break;
		case PROG_COPY:
			getActiveRobot().setActiveProgIdx(contents.getLineIdx());
			contents.setLineIdx(1);
			contents.setColumnIdx(0);
			workingText = new StringBuilder("\0");
			break;
		case NAV_PROG_INSTR:
			break;
		case SET_CALL_PROG:
			contents.setLineIdx( prev.conLnIdx );
			contents.setColumnIdx( prev.conColIdx );
			break;
		case CONFIRM_INSERT:
			workingText = new StringBuilder();
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
			contents.setLineIdx( prev.conLnIdx );
			contents.setColumnIdx( prev.conColIdx );
			contents.setRenderStart(  prev.conRenIdx );
			break;
		case SELECT_IO_INSTR_REG:
			contents.setLineIdx( prev.conLnIdx );
			contents.setColumnIdx( prev.conColIdx );
			contents.setRenderStart(  prev.conRenIdx );
			options.setLineIdx(0);
			options.setColumnIdx(1);
			break;
		case SET_MV_INSTR_OFFSET:
		case INPUT_DREG_IDX:
		case INPUT_IOREG_IDX:
		case INPUT_PREG_IDX1:
		case INPUT_PREG_IDX2:
		case INPUT_CONST:
			contents.setLineIdx( prev.conLnIdx );
			contents.setColumnIdx( prev.conColIdx );
			contents.setRenderStart(  prev.conRenIdx );
			workingText = new StringBuilder();
			break;
		case SET_IO_INSTR_IDX:
		case SET_JUMP_TGT:
		case SET_LBL_NUM:
			contents.setLineIdx( prev.conLnIdx );
			contents.setColumnIdx( prev.conColIdx );
			contents.setRenderStart(  prev.conRenIdx );
			workingText = new StringBuilder();
			break;
		case SET_MV_INSTR_TYPE:
			contents.setLineIdx( prev.conLnIdx );
			contents.setColumnIdx( prev.conColIdx );
			contents.setRenderStart(  prev.conRenIdx );
			
			switch (mInst.getMotionType()) {
			case Fields.MTYPE_JOINT:
				break;
			case Fields.MTYPE_LINEAR:
				options.setLineIdx(1);
				break;
			case Fields.MTYPE_CIRCULAR:
				options.setLineIdx(2);
				break;
			}

			break;
		case SET_MV_INSTR_SPD:
			contents.setLineIdx( prev.conLnIdx );
			contents.setColumnIdx( prev.conColIdx );
			contents.setRenderStart(  prev.conRenIdx );
			int instSpd;
			// Convert speed into an integer value
			if (mInst.getMotionType() == Fields.MTYPE_JOINT) {
				instSpd = Math.round(mInst.getSpeed() * 100f);
			} else {
				instSpd = Math.round(mInst.getSpeed());
			}
			
			workingText = new StringBuilder(instSpd);
			break;
		case SET_MV_INSTR_REG_TYPE:
			contents.setLineIdx( prev.conLnIdx );
			contents.setColumnIdx( prev.conColIdx );
			contents.setRenderStart(  prev.conRenIdx );
			
			if (mInst.usesGPosReg()) {
				options.setLineIdx(1);
			} else {
			}

			break;
		case SET_MV_INSTR_IDX:
			contents.setLineIdx( prev.conLnIdx );
			contents.setColumnIdx( prev.conColIdx );
			contents.setRenderStart(  prev.conRenIdx );
			workingText = new StringBuilder(mInst.getPositionNum() + 1);
			break;
		case SET_MV_INSTR_TERM:
			contents.setLineIdx( prev.conLnIdx );
			contents.setColumnIdx( prev.conColIdx );
			contents.setRenderStart(  prev.conRenIdx );
			workingText = new StringBuilder(mInst.getTermination());
			break;
		case SET_FRAME_INSTR_IDX:
		case SET_SELECT_ARGVAL:
		case SET_REG_EXPR_IDX1:
		case SET_REG_EXPR_IDX2:
			contents.setLineIdx( prev.conLnIdx );
			contents.setColumnIdx( prev.conColIdx );
			contents.setRenderStart(  prev.conRenIdx );
			workingText = new StringBuilder();
			break;
		case SET_IO_INSTR_STATE:
		case SET_FRM_INSTR_TYPE:
		case SET_REG_EXPR_TYPE:
			contents.setLineIdx( prev.conLnIdx );
			contents.setColumnIdx( prev.conColIdx );
			contents.setRenderStart(  prev.conRenIdx );
			break;
		case EDIT_MINST_POS:
			contents.setLineIdx( prev.conLnIdx );
			contents.setColumnIdx( prev.conColIdx );
			contents.setRenderStart(  prev.conRenIdx );
			// Load in the position associated with the active motion
			// instruction
			mInst = (MotionInstruction) activeRobot.getActiveInstruction();
			loadPosition(mInst.getPoint(activeRobot.getActiveProg()), mInst.getMotionType() != Fields.MTYPE_JOINT);
			break;
		case SELECT_INSTR_DELETE:
		case SELECT_COMMENT:
		case SELECT_CUT_COPY:
			Program p = getActiveRobot().getActiveProg();
			int size = p.getNumOfInst() - 1;
			getActiveRobot().setActiveInstIdx(max(0, min(getActiveRobot().getActiveInstIdx(), size)));
			contents.setColumnIdx(0);
			break;

			// Macros
		case NAV_MACROS:
			contents.setLineIdx(active_index);
			break;
		case SET_MACRO_PROG:
			contents.setLineIdx(0);
			break;
		case SET_MACRO_TYPE:
		case SET_MACRO_BINDING:
			break;

			// Registers
		case NAV_DATA:
			active_index = 0;
			break;
		case NAV_DREGS:
			loadDataRegisters();
			contents.setLineIdx(active_index);
			contents.setColumnIdx(0);
			break;
		case NAV_PREGS:
			loadPositionRegisters();
			contents.setLineIdx(active_index);
			contents.setColumnIdx(0);
			break;
		case DIRECT_ENTRY_TOOL:
		case DIRECT_ENTRY_USER:
			loadFrameDirectEntry(teachFrame);
			contents.setLineIdx(0);
			contents.setColumnIdx(1);
			break;
		case NAV_INSTR_MENU:
			break;
		case SWAP_PT_TYPE:
			contents.setLineIdx( prev.conLnIdx );
			contents.setColumnIdx( prev.conColIdx );
			contents.setRenderStart(  prev.conRenIdx );
			break;
		case CP_DREG_COM:
		case CP_DREG_VAL:
			loadDataRegisters();
			contents.setLineIdx( prev.conLnIdx );
			contents.setColumnIdx( prev.conColIdx );
			contents.setRenderStart(  prev.conRenIdx );
			options.setLineIdx(1);
			workingText = new StringBuilder((active_index + 1));
			break;
		case CP_PREG_COM:
		case CP_PREG_PT:
			loadPositionRegisters();
			contents.setLineIdx( prev.conLnIdx );
			contents.setColumnIdx( prev.conColIdx );
			contents.setRenderStart(  prev.conRenIdx );
			options.setLineIdx(1);
			workingText = new StringBuilder((active_index + 1));
			break;
		case EDIT_DREG_COM:
			contents.setLineIdx(1);
			contents.setColumnIdx(0);

			String c = getActiveRobot().getDReg(active_index).comment;
			if (c != null && c.length() > 0) {
				workingText = new StringBuilder(c);
			} else {
				workingText = new StringBuilder("\0");
			}

			break;
		case EDIT_PREG_COM:
			contents.setLineIdx(1);
			contents.setColumnIdx(0);

			c = getActiveRobot().getPReg(active_index).comment;
			if (c != null && c.length() > 0) {
				workingText = new StringBuilder(c);
			} else {
				workingText = new StringBuilder("\0");
			}

			println(workingText.length());
			break;
		case EDIT_DREG_VAL:
			loadDataRegisters();
			contents.setLineIdx( prev.conLnIdx );
			contents.setColumnIdx( prev.conColIdx );
			contents.setRenderStart(  prev.conRenIdx );
			// Bring up float input menu
			Float val = getActiveRobot().getDReg(active_index).value;
			if (val != null) {
				workingText = new StringBuilder(val.toString());

			} else {
				workingText = new StringBuilder();
			}
			break;
		case EDIT_PREG:
			PositionRegister pReg = activeRobot.getPReg(active_index);
			// Load the position associated with active position register
			if (pReg.point == null) {
				// Initialize an empty position register
				loadPosition(activeRobot.getDefaultPoint(), pReg.isCartesian);

			} else {
				loadPosition(pReg.point, pReg.isCartesian);
			}

			contents.setLineIdx(0);
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
	 */
	public PShape loadSTLModel(String filename, int fill) throws NullPointerException {
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

		PShape mesh = createShape();
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
	public void loadTextInput() {
		contents.addLine("\0");

		DisplayLine line = new DisplayLine();
		// Give each letter in the name a separate column
		for (int idx = 0; idx < workingText.length() && idx < TEXT_ENTRY_LEN; idx += 1) {
			line.add(Character.toString(workingText.charAt(idx)));
		}

		contents.addLine(line);
	}

	/**
	 * This method will draw the End Effector grid mapping based on the value of
	 * EE_MAPPING:
	 *
	 * 0 -> a line is drawn between the EE and the grid plane 1 -> a point is
	 * drawn on the grid plane that corresponds to the EE's xz coordinates For
	 * any other value, nothing is drawn
	 */
	public void mapToRobotBasePlane() {

		PVector basePos = getActiveRobot().getBasePosition();
		PVector ee_pos = nativeRobotEEPoint(getActiveRobot(), getActiveRobot().getJointAngles()).position;

		// Change color of the EE mapping based on if it lies below or above the
		// ground plane
		int c = (ee_pos.y <= basePos.y) ? color(255, 0, 0) : color(150, 0, 255);

		// Toggle EE mapping type with 'e'
		switch (getEEMapping()) {
		case LINE:
			stroke(c);
			// Draw a line, from the EE to the grid in the xy plane, parallel to
			// the xy plane
			line(ee_pos.x, ee_pos.y, ee_pos.z, ee_pos.x, basePos.y, ee_pos.z);
			break;

		case DOT:
			noStroke();
			fill(c);
			// Draw a point, which maps the EE's position to the grid in the xy
			// plane
			pushMatrix();
			rotateX(PI / 2);
			translate(0, 0, -basePos.y);
			ellipse(ee_pos.x, ee_pos.z, 10, 10);
			popMatrix();
			break;

		default:
			// No EE grid mapping
		}
	}

	/**
	 * Returns a string representation of the given matrix.
	 * 
	 * @param matrix
	 *            A non-null matrix
	 */
	public String matrixToString(float[][] matrix) {
		String mStr = "";

		for (int row = 0; row < matrix.length; ++row) {
			mStr += "\n[";

			for (int col = 0; col < matrix[0].length; ++col) {
				// Account for the negative sign character
				if (matrix[row][col] >= 0) {
					mStr += " ";
				}

				mStr += String.format(" %5.6f", matrix[row][col]);
			}

			mStr += "  ]";
		}

		return (mStr + "\n");
	}

	// Menu button
	public void menu() {
		resetStack();
		nextScreen(ScreenMode.NAV_MAIN_MENU);
	}

	public void mouseDragged(MouseEvent e) {
		if (mouseButton == CENTER) {
			// Drag the center mouse button to pan the camera
			float transScale = camera.getScale();
			camera.move(transScale * (mouseX - pmouseX), transScale * (mouseY - pmouseY), 0);
		}

		if (mouseButton == RIGHT) {
			// Drag right mouse button to rotate the camera
			float rotScale = DEG_TO_RAD / 4f;
			camera.rotate(rotScale * (mouseY - pmouseY), rotScale * (mouseX - pmouseX), 0);
		}
	}

	@Override
	public void mouseWheel(MouseEvent event) {
		if (UI != null && UI.isMouseOverADropdownList()) {
			// Disable zomming when selecting an element from a dropdown list
			return;
		}

		float e = event.getCount();
		// Control scaling of the camera with the mouse wheel
		if (e > 0) {
			camera.changeScale(1.05f);
		} else if (e < 0) {
			camera.changeScale(0.95f);
		}
	}

	/**
	 * Updates the current position and orientation of a selected object to the
	 * inputed values in the edit window.
	 */
	public void MoveToCur() {
		// Only allow world object editing when no program is executing
		if (!isProgramRunning()) {
			WorldObject savedState = (WorldObject) UI.getSelectedWO().clone();

			if (UI.updateWOCurrent()) {
				/*
				 * If the object was modified, then save the previous state of
				 * the object
				 */
				updateScenarioUndo(savedState);
			}

			DataManagement.saveScenarios(this);
		}
	}

	/**
	 * Updates the current position and orientation of a selected world object
	 * to that of its default fields.
	 */
	public void MoveToDef() {
		// Only allow world object editing when no program is executing
		if (!isProgramRunning()) {
			WorldObject savedState = (WorldObject) UI.getSelectedWO().clone();
			UI.fillCurWithDef();

			if (UI.updateWOCurrent()) {
				/*
				 * If the object was modified, then save the previous state of
				 * the object
				 */
				updateScenarioUndo(savedState);
			}

			DataManagement.saveScenarios(this);
		}
	}

	public void mvmu() {
		if (getSU_macro_bindings()[2] != null && isShift()) {
			getSU_macro_bindings()[2].execute();
		}
	}

	public void newCallInstruction() {
		RoboticArm r = getActiveRobot();
		Program p = r.getActiveProg();
		CallInstruction call = new CallInstruction(activeRobot);

		if (getActiveRobot().getActiveInstIdx() != p.getNumOfInst()) {
			r.replaceInstAt(getActiveRobot().getActiveInstIdx(), call);
		} else {
			p.addInstAtEnd(call);
		}
	}

	public void newFrameInstruction(int fType) {
		RoboticArm r = getActiveRobot();
		Program p = r.getActiveProg();
		FrameInstruction f = new FrameInstruction(fType, -1);

		if (getActiveRobot().getActiveInstIdx() != p.getNumOfInst()) {
			r.replaceInstAt(getActiveRobot().getActiveInstIdx(), f);
		} else {
			p.addInstAtEnd(f);
		}
	}

	public void newIfExpression() {
		RoboticArm r = getActiveRobot();
		Program p = r.getActiveProg();
		IfStatement stmt = new IfStatement();

		if (getActiveRobot().getActiveInstIdx() != p.getNumOfInst()) {
			r.replaceInstAt(getActiveRobot().getActiveInstIdx(), stmt);
		} else {
			p.addInstAtEnd(stmt);
		}
	}

	public void newIfStatement() {
		RoboticArm r = getActiveRobot();
		Program p = r.getActiveProg();
		IfStatement stmt = new IfStatement(Operator.EQUAL, null);
		opEdit = stmt.getExpr();

		if (getActiveRobot().getActiveInstIdx() != p.getNumOfInst()) {
			r.replaceInstAt(getActiveRobot().getActiveInstIdx(), stmt);
		} else {
			p.addInstAtEnd(stmt);
		}
	}

	public void newIOInstruction(int columnIdx) {
		RoboticArm r = getActiveRobot();
		Program p = r.getActiveProg();
		IOInstruction io = new IOInstruction(options.getLineIdx(), (columnIdx == 1) ? Fields.ON : Fields.OFF);

		if (getActiveRobot().getActiveInstIdx() != p.getNumOfInst()) {
			r.replaceInstAt(getActiveRobot().getActiveInstIdx(), io);

		} else {
			p.addInstAtEnd(io);
		}
	}

	public void newJumpInstruction() {
		RoboticArm r = getActiveRobot();
		Program p = r.getActiveProg();
		JumpInstruction j = new JumpInstruction(-1);

		if (getActiveRobot().getActiveInstIdx() != p.getNumOfInst()) {
			r.replaceInstAt(getActiveRobot().getActiveInstIdx(), j);
		} else {
			p.addInstAtEnd(j);
		}
	}

	public void newLabel() {
		RoboticArm r = getActiveRobot();
		Program p = r.getActiveProg();

		LabelInstruction l = new LabelInstruction(-1);

		if (getActiveRobot().getActiveInstIdx() != p.getNumOfInst()) {
			r.replaceInstAt(getActiveRobot().getActiveInstIdx(), l);
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
	public void newMotionInstruction() {
		RoboticArm r = getActiveRobot();
		Point pt = nativeRobotEEPoint(r, r.getJointAngles());
		Frame active = r.getActiveFrame(CoordFrame.USER);

		if (active != null) {
			// Convert into currently active frame
			pt = RMath.applyFrame(r, pt, active.getOrigin(), active.getOrientation());

			if (Fields.DEBUG) {
				System.out.printf("New: %s\n", RMath.vToWorld(pt.position));
			}
		}

		Program prog = getActiveRobot().getActiveProg();
		int instIdx = r.getActiveInstIdx();
		int reg = prog.getNextPosition();

		prog.setPosition(reg, pt);

		if (instIdx < prog.getNumOfInst()) {
			// Check if the active instruction is a motion instruction
			Instruction i = prog.getInstAt( r.getActiveInstIdx() );

			if (i instanceof MotionInstruction) {
				// Modify the existing motion instruction
				MotionInstruction mInst = (MotionInstruction) r.getInstToEdit( r.getActiveInstIdx() );

				if (getSelectedLine() > 0) {
					/*
					 * update the position of the secondary point of a circular
					 * instruction
					 */
					mInst = mInst.getSecondaryPoint();
				}

				// Update the motion instruction's fields
				CoordFrame coord = r.getCurCoordFrame();

				if (coord == CoordFrame.JOINT) {
					mInst.setMotionType(Fields.MTYPE_JOINT);
					mInst.setSpeed(0.5f);

				} else {
					/*
					 * Keep circular motion instructions as circular motion
					 * instructions in world, tool, or user frame modes
					 */
					if (mInst.getMotionType() == Fields.MTYPE_JOINT) {
						mInst.setMotionType(Fields.MTYPE_LINEAR);
					}

					mInst.setSpeed(50f * r.motorSpeed / 100f);
				}

				mInst.setPositionNum(reg);
				mInst.setToolFrame(r.getActiveToolFrame());
				mInst.setUserFrame(r.getActiveUserFrame());
				return;
			}
		}

		MotionInstruction insert = new MotionInstruction(
				getActiveRobot().getCurCoordFrame() == CoordFrame.JOINT ? Fields.MTYPE_JOINT : Fields.MTYPE_LINEAR, reg, false,
						(getActiveRobot().getCurCoordFrame() == CoordFrame.JOINT ? 50 : 50 * getActiveRobot().motorSpeed)
						/ 100f,
						0, getActiveRobot().getActiveUserFrame(), getActiveRobot().getActiveToolFrame());

		if (getActiveRobot().getActiveInstIdx() != prog.getNumOfInst()) {
			// Overwrite an existing non-motion instruction
			r.replaceInstAt(getActiveRobot().getActiveInstIdx(), insert);

		} else {
			// Insert the new motion instruction
			r.getActiveProg().addInstAt(prog.getNumOfInst(), insert);
		}
	}

	public void newRegisterStatement(Register reg) {
		RoboticArm r = getActiveRobot();
		Program p = r.getActiveProg();
		RegisterStatement stmt = new RegisterStatement(reg);

		if (getActiveRobot().getActiveInstIdx() != p.getNumOfInst()) {
			r.replaceInstAt(getActiveRobot().getActiveInstIdx(), stmt);
		} else {
			p.addInstAtEnd(stmt);
		}
	}

	public void newRegisterStatement(Register reg, int i) {
		RoboticArm r = getActiveRobot();
		Program p = r.getActiveProg();
		RegisterStatement stmt = new RegisterStatement(reg, i);

		if (getActiveRobot().getActiveInstIdx() != p.getNumOfInst()) {
			r.replaceInstAt(getActiveRobot().getActiveInstIdx(), stmt);
		} else {
			p.addInstAtEnd(stmt);
		}
	}

	public void newRobotCallInstruction() {
		RoboticArm r = getActiveRobot();
		Program p = r.getActiveProg();
		CallInstruction rcall = new CallInstruction(getInactiveRobot());

		if (getActiveRobot().getActiveInstIdx() != p.getNumOfInst()) {
			r.replaceInstAt(getActiveRobot().getActiveInstIdx(), rcall);
		} else {
			p.addInstAtEnd(rcall);
		}
	}

	public void newSelectStatement() {
		RoboticArm r = getActiveRobot();
		Program p = r.getActiveProg();
		SelectStatement stmt = new SelectStatement();

		if (getActiveRobot().getActiveInstIdx() != p.getNumOfInst()) {
			r.replaceInstAt(getActiveRobot().getActiveInstIdx(), stmt);
		} else {
			p.addInstAtEnd(stmt);
		}
	}

	/**
	 * Transitions the display to the given screen and pushes that screen onto
	 * the stack.
	 * 
	 * @param next
	 *            The new screen mode
	 */
	public void nextScreen(ScreenMode next) {
		if (Fields.DEBUG) {
			System.out.printf("%s => %s\n", mode, next);
		}
		
		if (!display_stack.isEmpty()) {
			ScreenState cur = display_stack.peek();
			// Save current screen state
			cur.conLnIdx = contents.getLineIdx();
			cur.conColIdx = contents.getColumnIdx();
			cur.conRenIdx = contents.getRenderStart();
			cur.optLnIdx = options.getLineIdx();
			cur.optRenIdx = options.getRenderStart();
		}
		
		mode = next;
		loadScreen(next);
	}

	public void num0() {
		characterInput('0');
	}

	public void num1() {
		characterInput('1');
	}

	public void num2() {
		characterInput('2');
	}

	public void num3() {
		characterInput('3');
	}

	public void num4() {
		characterInput('4');
	}

	public void num5() {
		characterInput('5');
	}

	public void num6() {
		characterInput('6');
	}

	public void num7() {
		characterInput('7');
	}

	public void num8() {
		characterInput('8');
	}

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
				// Convert the angles from degrees to radians, then convert from
				// World to Native frame, and finally convert to a quaternion
				RQuaternion orientation = RMath.eulerToQuat((new PVector(-inputs[3], -inputs[5], inputs[4])
											   .mult(DEG_TO_RAD)));

				// Use default the Robot's joint angles for computing inverse
				// kinematics
				float[] jointAngles = RMath.inverseKinematics(activeRobot, new float[] { 0f, 0f, 0f, 0f, 0f, 0f }, position,
						orientation);
				return new Point(position, orientation, jointAngles);

			} else {
				// Bring angles within range: (0, TWO_PI)
				for (int idx = 0; idx < inputs.length; ++idx) {
					inputs[idx] = RMath.mod2PI(inputs[idx] * DEG_TO_RAD);
				}

				return nativeRobotEEPoint(activeRobot, inputs);
			}

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
		Program p = getActiveRobot().getActiveProg();

		/* Pre-process instructions for insertion into program. */
		for (int i = 0; i < clipBoard.size(); i += 1) {
			Instruction instr = clipBoard.get(i).clone();

			if (instr instanceof MotionInstruction) {
				MotionInstruction m = (MotionInstruction) instr;

				if ((options & Fields.CLEAR_POSITION) == Fields.CLEAR_POSITION) {
					m.setPositionNum(-1);
				} else if ((options & Fields.NEW_POSITION) == Fields.NEW_POSITION) {
					/*
					 * Copy the current instruction's position to a new local
					 * position index and update the instruction to use this new
					 * position
					 */
					int instrPos = m.getPositionNum();
					int nextPos = p.getNextPosition();

					p.addPosition(p.getPosition(instrPos).clone());
					m.setPositionNum(nextPos);
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
						m.setSpeed(next.getSpeed());
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

			p.addInstAt(getActiveRobot().getActiveInstIdx() + i, instr);
		}
	}

	public void period() {
		characterInput('.');
	}

	public void posn() {
		if (getSU_macro_bindings()[5] != null && isShift()) {
			getSU_macro_bindings()[5].execute();
		}
	}

	public void prev() {
		lastScreen();
	}
	
	/**
	 * TODO comment
	 */
	private void pushActiveScreen() {
		
		pushScreen(mode, contents.getLineIdx(), contents.getColumnIdx(),
				contents.getRenderStart(), options.getLineIdx(),
				options.getRenderStart());
		
	}
	
	/**
	 * TODO comment
	 * 
	 * @param mode
	 * @param conLnIdx
	 * @param conColIdx
	 * @param conRenIdx
	 * @param optLnIdx
	 * @param optRenIdx
	 */
	private void pushScreen(ScreenMode mode, int conLnIdx, int conColIdx,
			int conRenIdx, int optLnIdx, int optRenIdx) {
		
		ScreenState curState = new ScreenState(mode, conLnIdx, conColIdx,
				conRenIdx, optLnIdx, optRenIdx);
		
		if (display_stack.size() > 10) {
			display_stack.remove(0);
		}
		
		display_stack.push(curState);
		
	}
	
	/**
	 * Displays coordinate frame associated with the current Coordinate frame.
	 * The active User frame is displayed in the User and Tool Coordinate
	 * Frames. The World frame is display in the World Coordinate frame and the
	 * Tool Coordinate Frame in the case that no active User frame is set. The
	 * active Tool frame axes are displayed in the Tool frame in addition to the
	 * current User (or World) frame. Nothing is displayed in the Joint
	 * Coordinate Frame.
	 */
	public void renderCoordAxes() {

		Point eePoint = nativeRobotEEPoint(getActiveRobot(), getActiveRobot().getJointAngles());

		if (getAxesState() == AxesDisplay.AXES && getActiveRobot().getCurCoordFrame() == CoordFrame.TOOL) {
			Frame activeTool = getActiveRobot().getActiveFrame(CoordFrame.TOOL);

			// Draw the axes of the active Tool frame at the Robot End Effector
			renderOriginAxes(eePoint.position, convertNativeToWorld(activeTool.getNativeAxisVectors()), 200f,
					color(255, 0, 255));
		} else {
			// Draw axes of the Robot's End Effector frame for testing purposes
			// displayOriginAxes(eePoint.position,
			// eePoint.orientation.toMatrix(), 200f, color(255, 0, 255));

			/* Draw a pink point for the Robot's current End Effecot position */
			pushMatrix();
			translate(eePoint.position.x, eePoint.position.y, eePoint.position.z);

			stroke(color(255, 0, 255));
			noFill();
			sphere(4);

			popMatrix();
		}

		if (getAxesState() == AxesDisplay.AXES) {
			// Display axes
			if (getActiveRobot().getCurCoordFrame() != CoordFrame.JOINT) {
				Frame activeUser = getActiveRobot().getActiveFrame(CoordFrame.USER);

				if (getActiveRobot().getCurCoordFrame() != CoordFrame.WORLD && activeUser != null) {
					// Draw the axes of the active User frame
					renderOriginAxes(activeUser.getOrigin(), convertNativeToWorld(activeUser.getNativeAxisVectors()),
							10000f, color(0));

				} else {
					// Draw the axes of the World frame
					renderOriginAxes(new PVector(0f, 0f, 0f), Fields.WORLD_AXES, 10000f, color(0));
				}
			}

		} else if (getAxesState() == AxesDisplay.GRID) {
			// Display gridlines spanning from axes of the current frame
			Frame active;
			float[][] displayAxes;
			PVector displayOrigin;

			switch (getActiveRobot().getCurCoordFrame()) {
			case JOINT:
			case WORLD:
				displayAxes = new float[][] { { 1f, 0f, 0f }, { 0f, 1f, 0f }, { 0f, 0f, 1f } };
				displayOrigin = new PVector(0f, 0f, 0f);
				break;
			case TOOL:
				active = getActiveRobot().getActiveFrame(CoordFrame.TOOL);
				displayAxes = active.getNativeAxisVectors();
				displayOrigin = eePoint.position;
				break;
			case USER:
				active = getActiveRobot().getActiveFrame(CoordFrame.USER);
				displayAxes = active.getNativeAxisVectors();
				displayOrigin = active.getOrigin();
				break;
			default:
				// No gridlines are displayed in the Joint Coordinate Frame
				return;
			}

			// Draw grid lines every 100 units, from -3500 to 3500, in the x and
			// y plane, on the floor plane
			renderGridlines(displayAxes, displayOrigin, 35, 100);
		}
	}

	/**
	 * Gridlines are drawn, spanning from two of the three axes defined by the
	 * given axes vector set. The two axes that form a plane that has the lowest
	 * offset of the xz-plane (hence the two vectors with the minimum y-values)
	 * are chosen to be mapped to the xz-plane and their reflection on the
	 * xz-plane are drawn the along with a grid is formed from the the two
	 * reflection axes at the base of the Robot.
	 * 
	 * @param axesVectors
	 *            A rotation matrix (in row major order) that defines the axes
	 *            of the frame to map to the xz-plane
	 * @param origin
	 *            The xz-origin at which to drawn the reflection axes
	 * @param halfNumOfLines
	 *            Half the number of lines to draw for one of the axes
	 * @param distBwtLines
	 *            The distance between each gridline
	 */
	public void renderGridlines(float[][] axesVectors, PVector origin, int halfNumOfLines, float distBwtLines) {
		int vectorPX = -1, vectorPZ = -1;

		// Find the two vectors with the minimum y values
		for (int v = 0; v < axesVectors.length; ++v) {
			int limboX = (v + 1) % axesVectors.length, limboY = (limboX + 1) % axesVectors.length;
			// Compare the y value of the current vector to those of the other
			// two vectors
			if (abs(axesVectors[v][1]) >= abs(axesVectors[limboX][1])
					&& abs(axesVectors[v][1]) >= abs(axesVectors[limboY][1])) {
				vectorPX = limboX;
				vectorPZ = limboY;
				break;
			}
		}

		if (vectorPX == -1 || vectorPZ == -1) {
			println("Invalid axes-origin pair for grid lines!");
			return;
		}

		pushMatrix();
		// Map the chosen two axes vectors to the xz-plane at the y-position of
		// the Robot's base
		applyMatrix(
				axesVectors[vectorPX][0], 0, axesVectors[vectorPZ][0], origin.x,
				0, 1, 0, getActiveRobot().getBasePosition().y,
				axesVectors[vectorPX][2], 0, axesVectors[vectorPZ][2], origin.z,
				0, 0, 0, 1
		);

		float lineLen = halfNumOfLines * distBwtLines;

		// Draw axes lines in red
		stroke(255, 0, 0);
		line(-lineLen, 0, 0, lineLen, 0, 0);
		line(0, 0, -lineLen, 0, 0, lineLen);
		// Draw remaining gridlines in black
		stroke(25, 25, 25);
		for (int linePosScale = 1; linePosScale <= halfNumOfLines; ++linePosScale) {
			line(distBwtLines * linePosScale, 0, -lineLen, distBwtLines * linePosScale, 0, lineLen);
			line(-lineLen, 0, distBwtLines * linePosScale, lineLen, 0, distBwtLines * linePosScale);

			line(-distBwtLines * linePosScale, 0, -lineLen, -distBwtLines * linePosScale, 0, lineLen);
			line(-lineLen, 0, -distBwtLines * linePosScale, lineLen, 0, -distBwtLines * linePosScale);
		}

		popMatrix();
		mapToRobotBasePlane();
	}

	/**
	 * Given a set of 3 orthogonal unit vectors a point in space, lines are
	 * drawn for each of the three vectors, which intersect at the origin point.
	 *
	 * @param origin
	 *            A point in space representing the intersection of the three
	 *            unit vectors
	 * @param axesVectors
	 *            A set of three orthogonal unti vectors
	 * @param axesLength
	 *            The length, to which the all axes, will be drawn
	 * @param originColor
	 *            The color of the point to draw at the origin
	 */
	public void renderOriginAxes(PVector origin, float[][] axesVectors, float axesLength, int originColor) {

		pushMatrix();
		// Transform to the reference frame defined by the axes vectors
		applyMatrix(origin, axesVectors);

		// X axis
		stroke(255, 0, 0);
		line(-axesLength, 0, 0, axesLength, 0, 0);
		// Y axis
		stroke(0, 255, 0);
		line(0, -axesLength, 0, 0, axesLength, 0);
		// Z axis
		stroke(0, 0, 255);
		line(0, 0, -axesLength, 0, 0, axesLength);

		// Draw a sphere on the positive direction for each axis
		float dotPos = max(100f, min(axesLength, 500));
		textFont(Fields.bond, 18);

		stroke(originColor);
		sphere(4);
		stroke(0);
		translate(dotPos, 0, 0);
		sphere(4);

		pushMatrix();
		rotateX(-PI / 2f);
		rotateY(-PI);
		text("X-axis", 0, 0, 0);
		popMatrix();

		translate(-dotPos, dotPos, 0);
		sphere(4);

		pushMatrix();
		rotateX(-PI / 2f);
		rotateY(-PI);
		text("Y-axis", 0, 0, 0);
		popMatrix();

		translate(0, -dotPos, dotPos);
		sphere(4);

		pushMatrix();
		rotateX(-PI / 2f);
		rotateY(-PI);
		text("Z-axis", 0, 0, 0);
		popMatrix();

		popMatrix();
	}

	/**
	 * Display any currently taught points during the processes of either the
	 * 3-Point, 4-Point, or 6-Point Methods.
	 */
	public void renderTeachPoints() {
		// Teach points are displayed only while the Robot is being taught a
		// frame
		if (teachFrame != null && mode.getType() == ScreenType.TYPE_TEACH_POINTS) {

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
					int pointColor = color(255, 0, 255);

					if (teachFrame instanceof ToolFrame) {

						if (idx < 3) {
							// TCP teach points
							pointColor = color(130, 130, 130);
						} else if (idx == 3) {
							// Orient origin point
							pointColor = color(255, 130, 0);
						} else if (idx == 4) {
							// Axes X-Direction point
							pointColor = color(255, 0, 0);
						} else if (idx == 5) {
							// Axes Y-Diretion point
							pointColor = color(0, 255, 0);
						}
					} else if (teachFrame instanceof UserFrame) {

						if (idx == 0) {
							// Orient origin point
							pointColor = color(255, 130, 0);
						} else if (idx == 1) {
							// Axes X-Diretion point
							pointColor = color(255, 0, 0);
						} else if (idx == 2) {
							// Axes Y-Diretion point
							pointColor = color(0, 255, 0);
						} else if (idx == 3) {
							// Axes Origin point
							pointColor = color(0, 0, 255);
						}
					}

					stroke(pointColor);
					sphere(3);

					popMatrix();
				}
			}
		}
	}
	
	/**
	 * Displays all the windows and the right-hand text display.
	 */
	public void renderUI() {
		textFont(Fields.medium, 14);
		fill(0);
		textAlign(RIGHT, TOP);
		
		RoboticArm r = getActiveRobot();
		int lastTextPositionX = width - 20, lastTextPositionY = 20;
		CoordFrame coord = r.getCurCoordFrame();
		String coordFrame;
		
		if (coord == null) {
			// Invalid state for coordinate frame
			coordFrame = "Coordinate Frame: N/A";
			
		} else {
			coordFrame = "Coordinate Frame: " + coord.toString();
		}

		Point RP = nativeRobotEEPoint(getActiveRobot(), getActiveRobot().getJointAngles());

		String[] cartesian = RP.toLineStringArray(true), joints = RP.toLineStringArray(false);
		// Display the current Coordinate Frame name
		text(coordFrame, lastTextPositionX, lastTextPositionY);
		lastTextPositionY += 20;
		// Display the Robot's speed value as a percent
		text(String.format("Jog Speed: %d%%", activeRobot.getLiveSpeed()), lastTextPositionX, lastTextPositionY);
		lastTextPositionY += 20;
		// Display the title of the currently active scenario
		String scenarioTitle;

		if (activeScenario != null) {
			scenarioTitle = "Scenario: " + activeScenario.getName();
		} else {
			scenarioTitle = "No active scenario";
		}

		text(scenarioTitle, lastTextPositionX, lastTextPositionY);
		lastTextPositionY += 40;
		// Display the Robot's current position and orientation ini the World
		// frame
		text("Robot Position and Orientation", lastTextPositionX, lastTextPositionY);
		lastTextPositionY += 20;
		text("World", lastTextPositionX, lastTextPositionY);
		lastTextPositionY += 20;

		for (String line : cartesian) {
			text(line, lastTextPositionX, lastTextPositionY);
			lastTextPositionY += 20;
		}

		Frame active = r.getActiveFrame(CoordFrame.USER);

		if (active != null) {
			// Display Robot's current position and orientation in the currently
			// active User frame
			RP.position = RMath.vToFrame(RP.position, active.getOrigin(), active.getOrientation());
			RP.orientation = active.getOrientation().transformQuaternion(RP.orientation);
			cartesian = RP.toLineStringArray(true);

			lastTextPositionY += 20;
			text(String.format("User: %d", getActiveRobot().getActiveUserFrame() + 1), lastTextPositionX,
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

		WorldObject toEdit = UI.getSelectedWO();
		// Display the position and orientation of the active world object
		if (toEdit != null) {
			String[] dimFields = toEdit.dimFieldsToStringArray();
			// Convert the values into the World Coordinate System
			PVector position = RMath.vToWorld(toEdit.getLocalCenter());
			PVector wpr = RMath.matrixToEuler(toEdit.getLocalOrientationAxes()).mult(RAD_TO_DEG);
			// Create a set of uniform Strings
			String[] fields = new String[] { String.format("X: %4.3f", position.x),
					String.format("Y: %4.3f", position.y), String.format("Z: %4.3f", position.z),
					String.format("W: %4.3f", -wpr.x), String.format("P: %4.3f", wpr.z),
					String.format("R: %4.3f", -wpr.y) };

			lastTextPositionY += 20;
			text(toEdit.getName(), lastTextPositionX, lastTextPositionY);
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

			text(dimDisplay, lastTextPositionX, lastTextPositionY);

			lastTextPositionY += 20;
			// Add space padding
			text(String.format("%-12s %-12s %s", fields[0], fields[1], fields[2]), lastTextPositionX,
					lastTextPositionY);
			lastTextPositionY += 20;
			text(String.format("%-12s %-12s %s", fields[3], fields[4], fields[5]), lastTextPositionX,
					lastTextPositionY);
			lastTextPositionY += 20;

			if (toEdit instanceof Part) {
				Part p = (Part) toEdit;
				// Convert the values into the World Coordinate System
				position = RMath.vToWorld(p.getDefaultCenter());
				wpr = RMath.matrixToEuler(p.getDefaultOrientationAxes()).mult(RAD_TO_DEG);
				// Create a set of uniform Strings
				fields = new String[] { String.format("X: %4.3f", position.x), String.format("Y: %4.3f", position.y),
						String.format("Z: %4.3f", position.z), String.format("W: %4.3f", -wpr.x),
						String.format("P: %4.3f", wpr.z), String.format("R: %4.3f", -wpr.y) };

				lastTextPositionY += 20;
				// Add space padding
				text(String.format("%-12s %-12s %s", fields[0], fields[1], fields[2]), lastTextPositionX,
						lastTextPositionY);
				lastTextPositionY += 20;
				text(String.format("%-12s %-12s %s", fields[3], fields[4], fields[5]), lastTextPositionX,
						lastTextPositionY);
				lastTextPositionY += 20;
			}
		}
		
		fill(215, 0, 0);
		lastTextPositionY += 20;

		// Display a message when there is an error with the Robot's
		// movement
		if (r.hasMotionFault()) {
			text("Motion Fault (press SHIFT + Reset)", lastTextPositionX, lastTextPositionY);
			lastTextPositionY += 20;
		}

		// Display a message if the Robot is in motion
		if (r.modelInMotion()) {
			text("Robot is moving", lastTextPositionX, lastTextPositionY);
			lastTextPositionY += 20;
		}

		if (isProgramRunning()) {
			text("Program executing", lastTextPositionX, lastTextPositionY);
			lastTextPositionY += 20;
		}

		// Display a message while the robot is carrying an object
		if (r.held != null) {
			text("Object held", lastTextPositionX, lastTextPositionY);
			lastTextPositionY += 20;

			PVector held_pos = r.held.getLocalCenter();
			String obj_pos = String.format("(%f, %f, %f)", held_pos.x, held_pos.y, held_pos.z);
			text(obj_pos, lastTextPositionX, lastTextPositionY);
			lastTextPositionY += 20;
		}
		
		fill(0);

		// Display the current axes display state
		text(String.format("Axes Display: %s", getAxesState().name()), lastTextPositionX, height - 50);

		if (getAxesState() == AxesDisplay.GRID) {
			// Display the current ee mapping state
			text(String.format("EE Mapping: %s", getEEMapping().name()), lastTextPositionX, height - 30);
		}

		UI.updateWindowDisplay();
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
				p.setLocalOrientationAxes(p.getDefaultOrientationAxes());
			}
		}
	}

	public void reset() {
		if (isShift()) {
			hold();
			// Reset motion fault for the active robot
			RoboticArm r = getActiveRobot();

			if (r != null) {
				r.setMotionFault(false);
			}
		}
	}
	
	/**
	 * TODO comment
	 */
	public void resetStack() {
		// Stop a program from executing when transition screens
		setProgramRunning(false);
		display_stack.clear();

		mode = ScreenMode.DEFAULT;
		pushScreen(mode, -1, -1, 0, -1, 0);
	}

	/**
	 * Sets the Robot with the specified ID as the active Robot and immediately
	 * resume execution of the Robot's active program, if it has one.
	 * 
	 * @param rid
	 *            The ID of the Robot to call
	 */
	public void returnRobot(int rid) {
		if (rid >= 0 && rid < ROBOTS.size() && ROBOTS.get(rid) != activeRobot) {
			if (activeRobot != null) {
				hold();
			}

			activeRobot = ROBOTS.get(rid);

			// Resume execution of the Robot's active program
			if (activeRobot.getActiveProg() != null) {
				nextScreen(ScreenMode.NAV_PROG_INSTR);

				if (!shift) {
					shift();
				}
			}
		}
	}

	public void RightView() {
		// Right view
		camera.reset();
		camera.rotate(0, 3f * PI / 2f, 0);
	}

	/**
	 * Deals with the confirm functionality of the scenario window.
	 */
	public void SConfirm() {
		int ret = UI.updateScenarios(SCENARIOS);

		if (ret > 0) {
			activeScenario = UI.getActiveScenario();
			DataManagement.saveScenarios(this);

		} else if (ret == 0) {
			DataManagement.saveScenarios(this);
		}

		System.out.println(String.format("SConfirm: %d\n", ret));
	}

	// Select button
	public void select() {
		getActiveRobot().setActiveProgIdx(0);
		getActiveRobot().setActiveInstIdx(-1);

		resetStack();
		nextScreen(ScreenMode.NAV_PROGRAMS);
	}

	/**
	 * Sets the scenario with the given name as the active scenario in the
	 * application, if a scenario with the given name exists.
	 * 
	 * @param name	The name of the scenario to set as active
	 * @return		Whether the scenario with the given name was
	 * 				successfully set as active
	 */
	protected boolean setActiveScenario(String name) {

		for (Scenario s : SCENARIOS) {
			if (s.getName().equals(name)) {
				activeScenario = s;
				return true;
			}
		}

		return false;

	}

	public void setExecutingInstruction(boolean executingInstruction) {
		this.executingInstruction = executingInstruction;
	}

	private void setManager(WGUI ui) {
		this.UI = ui;
	}

	public void setProgramRunning(boolean programRunning) {
		this.programRunning = programRunning;

		if (programRunning == false) {
			setExecutingInstruction(false);
		}
	}

	public void setRecord(int record) {
		this.record = record;
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

	public void setShift(boolean flag) {
		
		if (!flag) {
			// Stop Robot jog movement when shift is off
			// Stop all robot motion and program execution
			UI.resetJogButtons();
			activeRobot.halt();
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
		
		PImage[][] buttonImages = new PImage[][] {
			
			{ loadImage("images/record-35x20.png"), loadImage("images/record-over.png"), loadImage("images/record-on.png") },
			{ loadImage("images/EE_35x20.png"), loadImage("images/EE_over.png"), loadImage("images/EE_down.png") },
			{ loadImage("images/arrow-up.png"), loadImage("images/arrow-up_over.png"), loadImage("images/arrow-up_down.png") },
			{ loadImage("images/arrow-down.png"), loadImage("images/arrow-down_over.png"), loadImage("images/arrow-down_down.png") },
			{ loadImage("images/arrow-l.png"), loadImage("images/arrow-l_over.png"), loadImage("images/arrow-l_down.png") },
			{ loadImage("images/arrow-r.png"), loadImage("images/arrow-r_over.png"), loadImage("images/arrow-r_down.png") }
			
		};
		
		instance = this;
		letterStates = new int[] { 0, 0, 0, 0, 0 };
		workingText = new StringBuilder();
		
		// size(1200, 800, P3D);
		// create font and text display background
		Fields.medium = createFont("data/Consolas.ttf", 14);
		Fields.small = createFont("data/Consolas.ttf", 12);
		Fields.bond = createFont("data/ConsolasBold.ttf", 12);

		camera = new Camera();
		activeScenario = null;

		// load model and save data

		try {
			ROBOTS.put(0, new RoboticArm(0, new PVector(200, 300, 200), loadRobotModels()));
			ROBOTS.put(1, new RoboticArm(1, new PVector(200, 300, -750), loadRobotModels()));

			for (RoboticArm r : ROBOTS.values()) {
				r.setDefaultRobotPoint();
			}

			activeRobot = ROBOTS.get(0);

			intermediatePositions = new ArrayList<>();
			activeScenario = null;

			DataManagement.initialize(this);
			DataManagement.loadState(this);
			
			display_stack = new Stack<>();
			mode = ScreenMode.DEFAULT;
			pushScreen(ScreenMode.DEFAULT, -1, -1, 0, -1, 0);
			
			contents = new MenuScroll(this, "cont", ITEMS_TO_SHOW, 10, 20);
			options = new MenuScroll(this, "opt", 3, 10, 180);
			
			setManager(new WGUI(this, buttonImages));
			
			buffer = new ArrayList<>();
			displayPoint = null;

			c = new RobotCamera(-200, -200, 0, activeRobot.getOrientation(), 90, 1, 30, 300, null);

		} catch (NullPointerException NPEx) {

			// TODO write to a log
			NPEx.printStackTrace();
		}
	}

	public void SETUP() {
		if (getSU_macro_bindings()[3] != null && isShift()) {
			getSU_macro_bindings()[3].execute();
		}
	}

	/**
	 * Sets up an instruction for execution.
	 *
	 * @param program
	 *            Program that the instruction belongs to
	 * @param model
	 *            Arm model to use
	 * @param instruction
	 *            The instruction to execute
	 * @return Returns false on failure (invalid instruction), true on success
	 */
	public boolean setUpInstruction(Program program, RoboticArm model, MotionInstruction instruction) {
		Point start = nativeRobotEEPoint(getActiveRobot(), model.getJointAngles());

		if (!instruction.checkFrames(getActiveRobot().getActiveToolFrame(), getActiveRobot().getActiveUserFrame())) {
			// Current Frames must match the instruction's frames
			System.out.printf("Tool frame: %d : %d\nUser frame: %d : %d\n\n", instruction.getToolFrame(),
					getActiveRobot().getActiveToolFrame(), instruction.getUserFrame(),
					getActiveRobot().getActiveUserFrame());
			return false;
		} else if (instruction.getVector(program) == null) {
			return false;
		}

		if (instruction.getMotionType() == Fields.MTYPE_JOINT) {
			getActiveRobot().setupRotationInterpolation(instruction.getVector(program).angles);
		} // end joint movement setup
		else if (instruction.getMotionType() == Fields.MTYPE_LINEAR) {

			if (instruction.getTermination() == 0 || execSingleInst) {
				beginNewLinearMotion(start, instruction.getVector(program));
			} else {
				Point nextPoint = null;
				for (int n = getActiveRobot().getActiveInstIdx() + 1; n < program.getNumOfInst(); n++) {
					Instruction nextIns = program.getInstAt(n);
					if (nextIns instanceof MotionInstruction) {
						MotionInstruction castIns = (MotionInstruction) nextIns;
						nextPoint = castIns.getVector(program);
						break;
					}
				}
				if (nextPoint == null) {
					beginNewLinearMotion(start, instruction.getVector(program));
				} else {
					beginNewContinuousMotion(start, instruction.getVector(program), nextPoint,
							instruction.getTermination() / 100f);
				}
			} // end if termination type is continuous
		} // end linear movement setup
		else if (instruction.getMotionType() == Fields.MTYPE_CIRCULAR) {
			MotionInstruction nextIns = instruction.getSecondaryPoint();
			Point nextPoint = nextIns.getVector(program);

			beginNewCircularMotion(start, instruction.getVector(program), nextPoint);
		} // end circular movement setup

		return true;
	} // end setUpInstruction

	// toggle shift on/ off and button highlight
	public void shift() {
		setShift(!shift);
	}

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

	public void status() {
		if (getSU_macro_bindings()[4] != null && isShift()) {
			getSU_macro_bindings()[4].execute();
		}
	}

	// toggle step on/off and button highlight
	public void step() {
		setStep(!isStep());
	}

	/**
	 * Transitions to the given screen without saving the current screen on the
	 * stack.
	 * 
	 * @param nextScreen
	 *            The new screen mode
	 */
	public void switchScreen(ScreenMode nextScreen) {
		if (Fields.DEBUG) {
			System.out.printf("%s => %s\n", mode, nextScreen);
		}

		display_stack.pop();
		mode = nextScreen;
		loadScreen(nextScreen);
	}

	/**
	 * Toggle bounding box display on or off.
	 */
	public void ToggleOBBs() {
		UI.updateWindowContentsPositions();
	}

	/**
	 * Toggle the second Robot on or off.
	 */
	public void ToggleRobot() {
		boolean robotRemoved = UI.toggleSecondRobot();
		// Reset the active robot to the first if the second robot is removed
		if (robotRemoved && activeRobot != ROBOTS.get(0)) {
			activeRobot = ROBOTS.get(0);
		}

		UI.updateWindowContentsPositions();
		updatePendantScreen();
	}

	public void tool1() {
		if (getSU_macro_bindings()[0] != null && isShift()) {
			getSU_macro_bindings()[0].execute();
		}
	}

	public void tool2() {
		if (getSU_macro_bindings()[1] != null && isShift()) {
			getSU_macro_bindings()[1].execute();
		}
	}

	public void TopView() {
		// Top view
		camera.reset();
		camera.rotate(3f * PI / 2f, 0, 0);
	}

	/**
	 * Trigger a motion fault. This stops robot motion as well as program
	 * execution.
	 */
	public void triggerFault() {
		hold();

		RoboticArm r = getActiveRobot();

		if (r != null) {
			r.setMotionFault(true);
		}
	}

	/**
	 * Revert the most recent change to the active scenario
	 */
	public void undoScenarioEdit() {
		if (!SCENARIO_UNDO.empty()) {
			activeScenario.put(SCENARIO_UNDO.pop());
			UI.updateListContents();
			UI.updateEditWindowFields();
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
					getActiveRobot().setActiveToolFrame(frameIdx);
				} else {
					getActiveRobot().setActiveUserFrame(frameIdx);
				}

				updateCoordFrame();
			}

		} catch (NumberFormatException NFEx) {
			// Non-integer value
		}
		// Update display
		if (contents.getLineIdx() == 0) {
			workingText = new StringBuilder(Integer.toString(getActiveRobot().getActiveToolFrame() + 1));

		} else {
			workingText = new StringBuilder(Integer.toString(getActiveRobot().getActiveUserFrame() + 1));
		}

		contents.getActiveLine().set(contents.getColumnIdx(), workingText.toString());
		updatePendantScreen();
	}

	/**
	 * Updates the position and orientation of the Robot as well as all the
	 * World Objects associated with the current scenario. Updates the bounding
	 * box color, position and oientation of the Robot and all World Objects as
	 * well. Finally, all the World Objects and the Robot are drawn.
	 * 
	 * @param s
	 *            The currently active scenario
	 * @param active
	 *            The currently selected program
	 * @param model
	 *            The Robot Arm model
	 */
	public void updateAndDrawObjects(Scenario s, RoboticArm model) {
		model.updateRobot();

		if (RobotRun.getInstance().isProgramRunning()) {
			Program ap = model.getActiveProg();

			// Check the call stack for any waiting processes
			if (ap != null && model.getActiveInstIdx() == ap.getNumOfInst()) {
				CallFrame ret = model.popCallStack();

				if (ret != null) {
					RoboticArm tgtDevice = ROBOTS.get(ret.getTgtRID());
					tgtDevice.setActiveProgIdx(ret.getTgtProgID());
					tgtDevice.setActiveInstIdx(ret.getTgtInstID());
					activeRobot = tgtDevice;

					// Update the display
					getContentsMenu().setLineIdx(model.getActiveInstIdx());
					getContentsMenu().setColumnIdx(0);
					updatePendantScreen();
				}
			}
		}

		if (s != null) {
			s.resetObjectHitBoxColors();
		}

		model.resetOBBColors();
		model.checkSelfCollisions();

		if (s != null) {
			s.updateAndDrawObjects(model);
		}

		if (UI.getRobotButtonState()) {
			// Draw all robots
			for (RoboticArm r : ROBOTS.values()) {
				r.draw();
			}

		} else {
			// Draw only the active robot
			activeRobot.draw();
		}

		model.updatePreviousEEOrientation();
	}
	
	public void updateContents() {
		ArrayList<DisplayLine> prevContents = contents.copyContents();
		contents.clear();

		switch (mode) {
		// Program list navigation/ edit
		case NAV_PROGRAMS:
		case SET_MACRO_PROG:
			contents.setContents( loadPrograms(activeRobot) );
			break;
			
		case PROG_CREATE:
		case PROG_RENAME:
		case PROG_COPY:
			loadTextInput();
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
		case SET_MV_INSTR_TYPE:
		case SET_MV_INSTR_REG_TYPE:
		case SET_MV_INSTR_IDX:
		case SET_MV_INSTR_SPD:
		case SET_MV_INSTR_TERM:
		case SET_MV_INSTR_OFFSET:
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
			contents.setContents(loadInstructions(getActiveRobot().getActiveProgIdx()));
			break;

		case EDIT_MINST_POS:
			contents.setContents(prevContents);
			break;

		case ACTIVE_FRAMES:
			/* workingText corresponds to the active row's index display */
			if (contents.getLineIdx() == 0) {
				contents.addLine("Tool: ", workingText.toString());
				contents.addLine("User: ", Integer.toString(getActiveRobot().getActiveUserFrame() + 1));

			} else {
				contents.addLine("Tool: ", Integer.toString(getActiveRobot().getActiveToolFrame() + 1));
				contents.addLine("User: ", workingText.toString());
			}
			break;

			// View frame details
		case NAV_TOOL_FRAMES:
			loadFrames(CoordFrame.TOOL);
			break;
		case NAV_USER_FRAMES:
			loadFrames(CoordFrame.USER);
			break;
			// View frame details
		case TFRAME_DETAIL:
		case TEACH_3PT_TOOL:
		case TEACH_6PT:
		case UFRAME_DETAIL:
		case TEACH_3PT_USER:
		case TEACH_4PT:
		case DIRECT_ENTRY_USER:
		case DIRECT_ENTRY_TOOL:
		case FRAME_METHOD_USER:
		case FRAME_METHOD_TOOL:
		case EDIT_PREG:
		case EDIT_IOREG:
			contents.setContents(prevContents);
			break;

		case EDIT_DREG_VAL:
		case CP_DREG_COM:
		case CP_DREG_VAL:
		case CP_PREG_COM:
		case CP_PREG_PT:
			contents.setContents(prevContents);
			break;

		case SWAP_PT_TYPE:
			loadPositionRegisters();
			break;

			// View/ edit macros
		case NAV_MACROS:
		case SET_MACRO_TYPE:
		case SET_MACRO_BINDING:
			loadMacros();
			break;

		case NAV_MF_MACROS:
			loadManualFunct();
			break;

			// View/ edit registers
		case NAV_DREGS:
			loadDataRegisters();
			break;
		case NAV_PREGS:
			loadPositionRegisters();
			break;
		case EDIT_DREG_COM:
		case EDIT_PREG_COM:
			loadTextInput();
			break;
		case NAV_IOREG:
			loadIORegistersIntoContents();
			break;
		default:
			break;
		}
	}

	/**
	 * Transition back to the World Frame, if the current Frame is Tool or User
	 * and there are no active frame set for that Coordinate Frame. This method
	 * will halt the motion of the Robot if the active frame is changed.
	 */
	public void updateCoordFrame() {

		// Return to the World Frame, if no User Frame is active
		if (getActiveRobot().getCurCoordFrame() == CoordFrame.TOOL && !(getActiveRobot().getActiveToolFrame() >= 0
				&& getActiveRobot().getActiveToolFrame() < Fields.FRAME_NUM)) {
			getActiveRobot().setCurCoordFrame(CoordFrame.WORLD);
			// Stop Robot movement
			hold();
		}

		// Return to the World Frame, if no User Frame is active
		if (getActiveRobot().getCurCoordFrame() == CoordFrame.USER && !(getActiveRobot().getActiveUserFrame() >= 0
				&& getActiveRobot().getActiveUserFrame() < Fields.FRAME_NUM)) {
			getActiveRobot().setCurCoordFrame(CoordFrame.WORLD);
			// Stop Robot movement
			hold();
		}
	}

	/**
	 * Deals with updating the UI after confirming/canceling a deletion
	 */
	public void updateInstructions() {
		int instSize = getActiveRobot().getActiveProg().getNumOfInst();
		getActiveRobot().setActiveInstIdx(min(getActiveRobot().getActiveInstIdx(), instSize));
		lastScreen();
	}
	
	public void updateOptions() {
		options.clear();

		switch (mode) {
		// Main menu and submenus
		case NAV_MAIN_MENU:
			options.addLine("1 Frames");
			options.addLine("2 Macros");
			options.addLine("3 Manual Fncts");
			options.addLine("4 I/O Registers");
			break;

		case NAV_PROG_INSTR:
			Program p = activeRobot.getActiveProg();
			Instruction inst = activeRobot.getActiveInstruction();
			int colIdx = contents.getColumnIdx();
			
			if (inst instanceof MotionInstruction && (colIdx == 3 || colIdx == 4)) {
				// Show the position associated with the active motion
				// instruction
				MotionInstruction mInst = (MotionInstruction) inst;
				Point pt = mInst.getPoint(p);

				if (pt != null) {
					boolean isCartesian = mInst.getMotionType() != Fields.MTYPE_JOINT;
					String[] pregEntry = pt.toLineStringArray(isCartesian);

					for (String line : pregEntry) {
						options.addLine(line);
					}
					
				} else {
					options.addLine("Uninitialized");
				}
			}

			break;

		case EDIT_IOREG:
			options.addLine("OFF");
			options.addLine("ON");
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

			// Instruction edit options
		case SET_MV_INSTR_TYPE:
		case SET_MV_INSTR_REG_TYPE:
		case SET_MV_INSTR_IDX:
		case SET_MV_INSTR_SPD:
		case SET_MV_INSTR_TERM:
		case SET_MV_INSTR_OFFSET:
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
			loadInstrEdit(mode);
			break;
		case SET_CALL_PROG:
			RoboticArm r = ROBOTS.get(editIdx);
			
			if (r == null) {
				options.addLine( new DisplayLine(-1, 0, "Invalid editIdx") );
				
			} else {
				// List the robot's program names
				options.setContents( loadPrograms( ROBOTS.get(editIdx) ) );
			}
			
			break;
			// Insert instructions (non-movemet)
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
				options.addLine("6. RCALL");
			}
			break;
		case SELECT_IO_INSTR_REG:
			loadIORegisters();
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
		case FRAME_METHOD_TOOL:
			options.addLine("1. Three Point Method");
			options.addLine("2. Six Point Method");
			options.addLine("3. Direct Entry Method");
			break;
		case FRAME_METHOD_USER:
			options.addLine("1. Three Point Method");
			options.addLine("2. Four Point Method");
			options.addLine("3. Direct Entry Method");
			break;
		case TEACH_3PT_TOOL:
		case TEACH_3PT_USER:
		case TEACH_4PT:
		case TEACH_6PT:
			loadPointList();
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

			// Data navigation and edit menus
		case NAV_DATA:
			options.addLine("1. Data Registers");
			options.addLine("2. Position Registers");
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

	public void updateRobotJogMotion(int set, int direction) {
		// Only six jog button pairs exist
		if (set >= 0 && set < 6) {
			float newDir;

			if (getActiveRobot().getCurCoordFrame() == CoordFrame.JOINT) {
				// Move single joint
				newDir = getActiveRobot().activateLiveJointMotion(set, direction);
			} else {
				// Move entire robot in a single axis plane
				newDir = getActiveRobot().activateLiveWorldMotion(set, direction);
			}
			
			UI.updateJogButtons(set, newDir);
		}
	}

	/**
	 * Push a world object onto the undo stack for world objects.
	 * 
	 * @param saveState
	 *            The world object to save
	 */
	public void updateScenarioUndo(WorldObject saveState) {

		// Only the latest 10 world object save states can be undone
		if (SCENARIO_UNDO.size() >= 40) {
			// Not sure if size - 1 should be used instead
			SCENARIO_UNDO.remove(0);
		}

		SCENARIO_UNDO.push(saveState);
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
	
	
	/**
	 * Updates the default position and orientation of a world object based on
	 * the input fields in the edit window.
	 */
	public void UpdateWODef() {
		WorldObject saveState = (WorldObject) UI.getSelectedWO().clone();

		if (UI.updateWODefault()) {
			/*
			 * If the object was modified, then save the previous state of the
			 * object
			 */
			updateScenarioUndo(saveState);
		}
	}

	/**
	 * Convert a point based on a coordinate system defined as 3 orthonormal
	 * vectors. Reverse operation of vectorConvertTo.
	 * 
	 * @param point
	 *            Point to convert
	 * @param xAxis
	 *            X axis of target coordinate system
	 * @param yAxis
	 *            Y axis of target coordinate system
	 * @param zAxis
	 *            Z axis of target coordinate system
	 * @return Coordinates of point after conversion
	 */
	public PVector vectorConvertFrom(PVector point, PVector xAxis, PVector yAxis, PVector zAxis) {
		PMatrix3D matrix = new PMatrix3D(xAxis.x, yAxis.x, zAxis.x, 0, xAxis.y, yAxis.y, zAxis.y, 0, xAxis.z, yAxis.z,
				zAxis.z, 0, 0, 0, 0, 1);
		PVector result = new PVector();
		matrix.mult(point, result);
		return result;
	}

	/**
	 * Convert a point based on a coordinate system defined as 3 orthonormal
	 * vectors.
	 * 
	 * @param point
	 *            Point to convert
	 * @param xAxis
	 *            X axis of target coordinate system
	 * @param yAxis
	 *            Y axis of target coordinate system
	 * @param zAxis
	 *            Z axis of target coordinate system
	 * @return Coordinates of point after conversion
	 */
	public PVector vectorConvertTo(PVector point, PVector xAxis, PVector yAxis, PVector zAxis) {
		PMatrix3D matrix = new PMatrix3D(xAxis.x, xAxis.y, xAxis.z, 0, yAxis.x, yAxis.y, yAxis.z, 0, zAxis.x, zAxis.y,
				zAxis.z, 0, 0, 0, 0, 1);
		PVector result = new PVector();
		matrix.mult(point, result);
		return result;
	}

	/**
	 * Writes anything stored in the ArrayList String buffers to tmp\test.out.
	 */
	private int writeBuffer() {
		try {
			PrintWriter out = new PrintWriter(sketchPath("tmp/test.out"));

			for (String line : buffer) {
				out.print(line);
			}

			println("Write to buffer successful.");
			out.close();

		} catch (Exception Ex) {
			Ex.printStackTrace();
			return 1;
		}

		buffer.clear();
		return 0;
	}
}

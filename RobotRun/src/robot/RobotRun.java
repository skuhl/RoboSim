package robot;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import controlP5.*;
import processing.core.*;
import processing.event.MouseEvent;

import expression.*;
import frame.*;
import geom.*;
import global.*;
import programming.*;
import regs.*;
import ui.*;
import window.*;

public class RobotRun extends PApplet {
	public static final PVector ROBOT_POSITION;
	public static final int EXEC_SUCCESS = 0, EXEC_FAILURE = 1, EXEC_PARTIAL = 2;
	
	private Camera camera;
	private PFont fnt_con14, fnt_con12, fnt_conB;
	// The position at which the Robot is drawn
	private ArmModel armModel;

	private ControlP5 cp5;
	private WindowManager manager;
	private Stack<Screen> display_stack;

	private ArrayList<Macro> macros = new ArrayList<Macro>();
	private Macro[] SU_macro_bindings = new Macro[7];
	private Macro edit_macro;
	
	/*******************************/
	/*      Global Variables       */
	/*******************************/

	// for Execution
	public boolean execSingleInst = false; 
	public boolean motionFault = false; //indicates motion error with the Robot
	private static RobotRun instance;

	/*******************************/
	/*      Debugging Stuff        */
	/*******************************/

	private ArrayList<String> buffer;
	private Point displayPoint;
	
	/*******************************/
	/*        GUI Stuff            */
	/*******************************/
	
	public static final float[][] WORLD_AXES;
												 


	private int active_prog = -1; // the currently selected program
	private int active_instr = -1; // the currently selected instruction
	int temp_select = 0;
	boolean shift = false; // Is shift button pressed or not?
	private boolean step = false; // Is step button pressed or not?
	private int record = Fields.OFF;

	Screen mode;
	int g1_px, g1_py; // the left-top corner of group1
	int g1_width, g1_height; // group 1's width and height
	int display_px, display_py; // the left-top corner of display screen
	int display_width, display_height; // height and width of display screen

	public Group g1, g2;
	Button bt_record_normal, 
	bt_ee_normal;

	String workingText; // when entering text or a number
	String workingTextSuffix;
	boolean speedInPercentage;

	final int ITEMS_TO_SHOW = 8, // how many programs/ instructions to display on screen
			NUM_ENTRY_LEN = 16, // Maximum character length for a number input
			TEXT_ENTRY_LEN = 16; // Maximum character length for text entry

	// Index of the current frame (Tool or User) selecting when in the Frame menus
	int curFrameIdx = -1;
	// The Frame being taught, during a frame teaching process
	Frame teachFrame = null;
	// Expression operand currently being edited
	ExprOperand opEdit = null;
	int editIdx = -1;

	//variables for keeping track of the last change made to the current program
	Instruction lastInstruct;
	boolean newInstruct;
	int lastLine;

	// display list of programs or motion instructions
	ArrayList<DisplayLine> contents = new ArrayList<DisplayLine>();
	// Display otions for a number of menus
	ArrayList<String> options = new ArrayList<String>();
	// store numbers pressed by the user
	ArrayList<Integer> nums = new ArrayList<Integer>();
	// container for instructions being coppied/ cut and pasted
	ArrayList<Instruction> clipBoard = new ArrayList<Instruction>();
	// string for displaying error message to user
	String err = null;
	// which element is on focus now?
	private int row_select = 0; //currently selected display row
	int prev_select = -1; //saves row_select value if next screen also utilizes this variable
	private int col_select = 0; //currently selected display column
	int opt_select = 0; //which option is on focus now?
	private int start_render = 0; //index of the first element in a list to be drawn on screen
	int active_index = 0; //index of the cursor with respect to the first element on screen
	boolean[] selectedLines; //array whose indecies correspond to currently selected lines
	// how many textlabels have been created for display
	int index_contents = 0, index_options = 100, index_nums = 1000;

	/**
	 * Used for comment name input. The user can cycle through the
	 * six states for each function button in this mode:
	 *
	 * F1 -> A-F/a-f
	 * F2 -> G-L/g-l
	 * F3 -> M-R/m-r
	 * F4 -> S-X/s-x
	 * F5 -> Y-Z/y-z, _, @, *, .
	 */
	private int[] letterStates;
	private static final char[][] letters;
	
	static {
		instance = null;
		ROBOT_POSITION = new PVector(200, 300, 200);
		WORLD_AXES = new float[][] { { -1,  0,  0 },
									 {  0,  0,  1 },
									 {  0, -1,  0 } };
		
		 letters = new char[][] {{'a', 'b', 'c', 'd', 'e', 'f'},
								 {'g', 'h', 'i', 'j', 'k', 'l'},
								 {'m', 'n', 'o', 'p', 'q', 'r'},
								 {'s', 't', 'u', 'v', 'w', 'x'},
								 {'y', 'z', '_', '@', '*', '.'}};
	}
	
	public void setup() {
		instance = this;
		letterStates = new int[] {0, 0, 0, 0, 0};
		
		//size(1200, 800, P3D);
		//create font and text display background
		fnt_con14 = createFont("data/Consolas.ttf", 14);
		fnt_con12 = createFont("data/Consolas.ttf", 12);
		fnt_conB = createFont("data/ConsolasBold.ttf", 12);

		camera = new Camera();

		//load model and save data
		armModel = new ArmModel(0);
		intermediatePositions = new ArrayList<Point>();
		activeScenario = null;
		showOOBs = true;

		loadState();

		//set up UI
		cp5 = new ControlP5(this);
		// Expllicitly draw the ControlP5 elements
		cp5.setAutoDraw(false);
		setManager(new WindowManager(cp5, fnt_con12, fnt_con14));
		display_stack = new Stack<Screen>();
		gui();

		buffer = new ArrayList<String>();
		displayPoint = null;
	}
	
	public void settings() {  size(1080, 720, P3D); }
	
	public static void main(String[] passedArgs) {
		String[] appletArgs = new String[] { "robot.RobotRun" };
		
		if (passedArgs != null) {
			PApplet.main(concat(appletArgs, passedArgs));
		} else {
			PApplet.main(appletArgs);
		}
	}

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

		Program p = activeProgram();

		updateAndDrawObjects(activeScenario, p, armModel);
		displayAxes();
		displayTeachPoints();

		WorldObject wldObj = getManager().getActiveWorldObject();

		if (wldObj != null) {
			pushMatrix();

			if (wldObj instanceof Part) {
				Fixture reference = ((Part)wldObj).getFixtureRef();

				if (reference != null) {
					// Draw part's orientation with reference to its fixture
					reference.applyCoordinateSystem();
				}
			}

			displayOriginAxes(wldObj.getLocalCenter(), wldObj.getLocalOrientationAxes(), 500f, color(0));

			popMatrix();
		}

		if (displayPoint != null) {
			// Display the point with its local orientation axes
			displayOriginAxes(displayPoint.position, displayPoint.orientation.toMatrix(), 100f, color(0, 100, 15));
		}

		//TESTING CODE: DRAW INTERMEDIATE POINTS
		noLights();
		noStroke();
		pushMatrix();
		//if(intermediatePositions != null) {
		//  int count = 0;
		//  for(Point pt : intermediatePositions) {
		//    if(count % 20 == 0) {
		//      pushMatrix();
		//      stroke(0);
		//      translate(pt.position.x, pt.position.y, pt.position.z);
		//      sphere(5);
		//      popMatrix();
		//    }
		//    count += 1;
		//  }
		//}
		popMatrix();
		popMatrix();

		hint(DISABLE_DEPTH_TEST);
		// Apply the camera for drawing text and windows
		ortho();
		showMainDisplayText();
		cp5.draw();
		//println(frameRate + " fps");
	}

	/*****************************************************************************************************************
 NOTE: All the below methods assume that current matrix has the camrea applied!
	 *****************************************************************************************************************/

	/**
	 * Updates the position and orientation of the Robot as well as all the World
	 * Objects associated with the current scenario. Updates the bounding box color,
	 * position and oientation of the Robot and all World Objects as well. Finally,
	 * all the World Objects and the Robot are drawn.
	 * 
	 * @param s       The currently active scenario
	 * @param active  The currently selected program
	 * @param model   The Robot Arm model
	 */
	public void updateAndDrawObjects(Scenario s, Program active, ArmModel model) {
		model.updateRobot(active);

		if (s != null) {
			s.resetObjectHitBoxColors();
		}

		model.resetOBBColors(); 
		model.checkSelfCollisions();

		if (s != null) {
			s.updateAndDrawObjects(model);
		}
		model.draw();

		model.updatePreviousEEOrientation();
	}

	/**
	 * Display any currently taught points during the processes of either the 3-Point, 4-Point, or 6-Point Methods.
	 */
	public void displayTeachPoints() {
		// Teach points are displayed only while the Robot is being taught a frame
		if(teachFrame != null && mode.getType() == ScreenType.TYPE_TEACH_POINTS) {

			int size = 3;

			if (mode == Screen.TEACH_6PT && teachFrame instanceof ToolFrame) {
				size = 6;
			} else if (mode == Screen.TEACH_4PT && teachFrame instanceof UserFrame) {
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
	 * Displays coordinate frame associated with the current Coordinate frame. The active User frame is displayed in the User and Tool
	 * Coordinate Frames. The World frame is display in the World Coordinate frame and the Tool Coordinate Frame in the case that no
	 * active User frame is set. The active Tool frame axes are displayed in the Tool frame in addition to the current User (or World)
	 * frame. Nothing is displayed in the Joint Coordinate Frame.
	 */
	public void displayAxes() {

		Point eePoint = nativeRobotEEPoint(armModel.getJointAngles());

		if (axesState == AxesDisplay.AXES && armModel.getCurCoordFrame() == CoordFrame.TOOL) {
			Frame activeTool = armModel.getActiveFrame(CoordFrame.TOOL);

			// Draw the axes of the active Tool frame at the Robot End Effector
			displayOriginAxes(eePoint.position, activeTool.getWorldAxisVectors(), 200f, color(255, 0, 255));
		} else {
			// Draw axes of the Robot's End Effector frame for testing purposes
			//displayOriginAxes(eePoint.position, eePoint.orientation.toMatrix(), 200f, color(255, 0, 255));

			/* Draw a pink point for the Robot's current End Effecot position */
			pushMatrix();
			translate(eePoint.position.x, eePoint.position.y, eePoint.position.z);

			stroke(color(255, 0, 255));
			noFill();
			sphere(4);

			popMatrix();
		}

		if (axesState == AxesDisplay.AXES) {
			// Display axes
			if (armModel.getCurCoordFrame() != CoordFrame.JOINT) {
				Frame activeUser = armModel.getActiveFrame(CoordFrame.USER);

				if(armModel.getCurCoordFrame() != CoordFrame.WORLD && activeUser != null) {
					// Draw the axes of the active User frame
					displayOriginAxes(activeUser.getOrigin(), activeUser.getWorldAxisVectors(), 10000f, color(0));

				} else {
					// Draw the axes of the World frame
					displayOriginAxes(new PVector(0f, 0f, 0f), WORLD_AXES, 10000f, color(0));
				}
			}

		} else if (axesState == AxesDisplay.GRID) {
			// Display gridlines spanning from axes of the current frame
			Frame active;
			float[][] displayAxes;
			PVector displayOrigin;

			switch(armModel.getCurCoordFrame()) {
			case JOINT:
			case WORLD:
				displayAxes = new float[][] { {1f, 0f, 0f}, {0f, 1f, 0f}, {0f, 0f, 1f} };
				displayOrigin = new PVector(0f, 0f, 0f);
				break;
			case TOOL:
				active = armModel.getActiveFrame(CoordFrame.TOOL);
				displayAxes = active.getNativeAxisVectors();
				displayOrigin = eePoint.position;
				break;
			case USER:
				active = armModel.getActiveFrame(CoordFrame.USER);
				displayAxes = active.getNativeAxisVectors();
				displayOrigin = active.getOrigin();
				break;
			default:
				// No gridlines are displayed in the Joint Coordinate Frame
				return;
			}

			// Draw grid lines every 100 units, from -3500 to 3500, in the x and y plane, on the floor plane
			displayGridlines(displayAxes, displayOrigin, 35, 100);
		}
	}

	/**
	 * Given a set of 3 orthogonal unit vectors a point in space, lines are
	 * drawn for each of the three vectors, which intersect at the origin point.
	 *
	 * @param origin       A point in space representing the intersection of the
	 *                     three unit vectors
	 * @param axesVectors  A set of three orthogonal unti vectors
	 * @param axesLength   The length, to which the all axes, will be drawn
	 * @param originColor  The color of the point to draw at the origin
	 */
	public void displayOriginAxes(PVector origin, float[][] axesVectors, float axesLength, int originColor) {

		pushMatrix();    
		// Transform to the reference frame defined by the axes vectors
		applyMatrix(axesVectors[0][0], axesVectors[1][0], axesVectors[2][0], origin.x,
				axesVectors[0][1], axesVectors[1][1], axesVectors[2][1], origin.y,
				axesVectors[0][2], axesVectors[1][2], axesVectors[2][2], origin.z,
				0, 0, 0, 1);

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

		stroke(originColor);
		sphere(4);
		stroke(0);
		translate(dotPos, 0, 0);
		sphere(4);
		translate(-dotPos, dotPos, 0);
		sphere(4);
		translate(0, -dotPos, dotPos);
		sphere(4);

		popMatrix();
	}

	/**
	 * Gridlines are drawn, spanning from two of the three axes defined by the given axes vector set. The two axes that form a
	 * plane that has the lowest offset of the xz-plane (hence the two vectors with the minimum y-values) are chosen to be
	 * mapped to the xz-plane and their reflection on the xz-plane are drawn the along with a grid is formed from the the two
	 * reflection axes at the base of the Robot.
	 * 
	 * @param axesVectors     A rotation matrix (in row major order) that defines the axes of the frame to map to the xz-plane
	 * @param origin          The xz-origin at which to drawn the reflection axes
	 * @param halfNumOfLines  Half the number of lines to draw for one of the axes
	 * @param distBwtLines    The distance between each gridline
	 */
	public void displayGridlines(float[][] axesVectors, PVector origin, int halfNumOfLines, float distBwtLines) {
		int vectorPX = -1, vectorPZ = -1;

		// Find the two vectors with the minimum y values
		for (int v = 0; v < axesVectors.length; ++v) {
			int limboX = (v + 1) % axesVectors.length,
					limboY = (limboX + 1) % axesVectors.length;
			// Compare the y value of the current vector to those of the other two vectors
			if (abs(axesVectors[v][1]) >= abs(axesVectors[limboX][1]) && abs(axesVectors[v][1]) >= abs(axesVectors[limboY][1])) {
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
		// Map the chosen two axes vectors to the xz-plane at the y-position of the Robot's base
		applyMatrix(axesVectors[vectorPX][0], 0, axesVectors[vectorPZ][0], origin.x,
				0, 1,                        0, ROBOT_POSITION.y,
				axesVectors[vectorPX][2], 0, axesVectors[vectorPZ][2], origin.z,
				0, 0,                        0,        1);

		float lineLen = halfNumOfLines * distBwtLines;

		// Draw axes lines in red
		stroke(255, 0, 0);
		line(-lineLen, 0, 0, lineLen, 0, 0);
		line(0, 0, -lineLen, 0, 0, lineLen);
		// Draw remaining gridlines in black
		stroke(25, 25, 25);
		for(int linePosScale = 1; linePosScale <= halfNumOfLines; ++linePosScale) {
			line(distBwtLines * linePosScale, 0, -lineLen, distBwtLines * linePosScale, 0, lineLen);
			line(-lineLen, 0, distBwtLines * linePosScale, lineLen, 0, distBwtLines * linePosScale);

			line(-distBwtLines * linePosScale, 0, -lineLen, -distBwtLines * linePosScale, 0, lineLen);
			line(-lineLen, 0, -distBwtLines * linePosScale, lineLen, 0, -distBwtLines * linePosScale);
		}

		popMatrix();
		mapToRobotBasePlane();
	}

	/**
	 * This method will draw the End Effector grid mapping based on the value of EE_MAPPING:
	 *
	 *  0 -> a line is drawn between the EE and the grid plane
	 *  1 -> a point is drawn on the grid plane that corresponds to the EE's xz coordinates
	 *  For any other value, nothing is drawn
	 */
	public void mapToRobotBasePlane() {

		PVector ee_pos = nativeRobotEEPoint(armModel.getJointAngles()).position;

		// Change color of the EE mapping based on if it lies below or above the ground plane
		int c = (ee_pos.y <= ROBOT_POSITION.y) ? color(255, 0, 0) : color(150, 0, 255);

		// Toggle EE mapping type with 'e'
		switch (mappingState) {
		case LINE:
			stroke(c);
			// Draw a line, from the EE to the grid in the xy plane, parallel to the xy plane
			line(ee_pos.x, ee_pos.y, ee_pos.z, ee_pos.x, ROBOT_POSITION.y, ee_pos.z);
			break;

		case DOT:
			noStroke();
			fill(c);
			// Draw a point, which maps the EE's position to the grid in the xy plane
			pushMatrix();
			rotateX(PI / 2);
			translate(0, 0, -ROBOT_POSITION.y);
			ellipse(ee_pos.x, ee_pos.z, 10, 10);
			popMatrix();
			break;

		default:
			// No EE grid mapping
		}
	}
	
	ArrayList<Point> intermediatePositions;
	int motionFrameCounter = 0;
	float distanceBetweenPoints = 5.0f;
	int interMotionIdx = -1;

	int liveSpeed = 10;
	private boolean executingInstruction = false;

	// Determines what End Effector mapping should be display
	private EEMapping mappingState = EEMapping.LINE;
	// Deterimes what type of axes should be displayed
	private static AxesDisplay axesState = AxesDisplay.AXES;

	public static final boolean DISPLAY_TEST_OUTPUT = true;

	/**
	 * Displays important information in the upper-right corner of the screen.
	 */
	public void showMainDisplayText() {
		fill(0);
		textAlign(RIGHT, TOP);
		int lastTextPositionX = width - 20,
				lastTextPositionY = 20;
		String coordFrame = "Coordinate Frame: ";

		switch(armModel.getCurCoordFrame()) {
		case JOINT:
			coordFrame += "Joint";
			break;
		case WORLD:
			coordFrame += "World";
			break;
		case TOOL:
			coordFrame += "Tool";
			break;
		case USER:
			coordFrame += "User";
			break;
		default:
		}

		Point RP = nativeRobotEEPoint(armModel.getJointAngles());

		String[] cartesian = RP.toLineStringArray(true),
				joints = RP.toLineStringArray(false);
		// Display the current Coordinate Frame name
		text(coordFrame, lastTextPositionX, lastTextPositionY);
		lastTextPositionY += 20;
		// Display the Robot's speed value as a percent
		text(String.format("Jog Speed: %d%%", liveSpeed), lastTextPositionX, lastTextPositionY);
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
		// Display the Robot's current position and orientation ini the World frame
		text("Robot Position and Orientation", lastTextPositionX, lastTextPositionY);
		lastTextPositionY += 20;
		text("World", lastTextPositionX, lastTextPositionY);
		lastTextPositionY += 20;

		for (String line : cartesian) {
			text(line, lastTextPositionX, lastTextPositionY);
			lastTextPositionY += 20;
		}

		Frame active = armModel.getActiveFrame(CoordFrame.USER);

		if (active != null) {
			// Display Robot's current position and orientation in the currently active User frame
			RP.position = convertToFrame(RP.position, active.getOrigin(), active.getOrientation());
			RP.orientation = active.getOrientation().transformQuaternion(RP.orientation);
			cartesian = RP.toLineStringArray(true);

			lastTextPositionY += 20;
			text(String.format("User: %d", armModel.getActiveUserFrame() + 1), lastTextPositionX, lastTextPositionY);
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

		WorldObject toEdit = getManager().getActiveWorldObject();
		// Display the position and orientation of the active world object
		if (toEdit != null) {
			String[] dimFields = toEdit.dimFieldsToStringArray();
			// Convert the values into the World Coordinate System
			PVector position = convertNativeToWorld(toEdit.getLocalCenter());
			PVector wpr =  matrixToEuler(toEdit.getLocalOrientationAxes()).mult(RAD_TO_DEG);
			// Create a set of uniform Strings
			String[] fields = new String[] { String.format("X: %4.3f", position.x), String.format("Y: %4.3f", position.y),
					String.format("Z: %4.3f", position.z), String.format("W: %4.3f", -wpr.x),
					String.format("P: %4.3f", -wpr.z), String.format("R: %4.3f", wpr.y) };

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
			// Add space patting
			text(String.format("%-12s %-12s %s", fields[0], fields[1], fields[2]), lastTextPositionX, lastTextPositionY);
			lastTextPositionY += 20;
			text(String.format("%-12s %-12s %s", fields[3], fields[4], fields[5]), lastTextPositionX, lastTextPositionY);
			lastTextPositionY += 20;
		}

		lastTextPositionY += 20;
		// Display the current axes display state
		text(String.format("Axes Display: %s", axesState.name()),  lastTextPositionX, height - 50);

		if (axesState == AxesDisplay.GRID) {
			// Display the current ee mapping state
			text(String.format("EE Mapping: %s", mappingState.name()),  lastTextPositionX, height - 30);
		}

		if (DISPLAY_TEST_OUTPUT) {
			fill(215, 0, 0);

			// Display a message when there is an error with the Robot's movement
			if (motionFault) {
				text("Motion Fault (press SHIFT + Reset)", lastTextPositionX, lastTextPositionY);
				lastTextPositionY += 20;
			}

			// Display a message if the Robot is in motion
			if (armModel.modelInMotion()) {
				text("Robot is moving", lastTextPositionX, lastTextPositionY);
				lastTextPositionY += 20;
			}

			if (isProgramRunning()) {
				text("Program executing", lastTextPositionX, lastTextPositionY);
				lastTextPositionY += 20;
			}

			// Display a message while the robot is carrying an object
			if(armModel.held != null) {
				text("Object held", lastTextPositionX, lastTextPositionY);
				lastTextPositionY += 20;

				PVector held_pos = armModel.held.getLocalCenter();
				String obj_pos = String.format("(%f, %f, %f)", held_pos.x, held_pos.y, held_pos.z);
				text(obj_pos, lastTextPositionX, lastTextPositionY);
				lastTextPositionY += 20;
			}
		}

		getManager().updateWindowDisplay();
	}

	/** 
	 * Transitions to the next Coordinate frame in the cycle, updating the Robot's current frame
	 * in the process and skipping the Tool or User frame if there are no active frames in either
	 * one. Since the Robot's frame is potentially reset in this method, all Robot motion is halted.
	 *
	 * @param model  The Robot Arm, for which to switch coordinate frames
	 */
	public void coordFrameTransition() {
		// Stop Robot movement
		armModel.halt();

		// Increment the current coordinate frame
		switch (armModel.getCurCoordFrame()) {
		case JOINT:
			armModel.setCurCoordFrame(CoordFrame.WORLD);
			break;

		case WORLD:
			armModel.setCurCoordFrame(CoordFrame.TOOL);
			break;

		case TOOL:
			armModel.setCurCoordFrame(CoordFrame.USER);
			break;

		case USER:
			armModel.setCurCoordFrame(CoordFrame.JOINT);
			break;
		}

		// Skip the Tool Frame, if there is no active frame
		if(armModel.getCurCoordFrame() == CoordFrame.TOOL && !(armModel.getActiveToolFrame() >= 0 && armModel.getActiveToolFrame() < Fields.FRAME_SIZE)) {
			armModel.setCurCoordFrame(CoordFrame.USER);
		}

		// Skip the User Frame, if there is no active frame
		if(armModel.getCurCoordFrame() == CoordFrame.USER && !(armModel.getActiveUserFrame() >= 0 && armModel.getActiveUserFrame() < Fields.FRAME_SIZE)) {
			armModel.setCurCoordFrame(CoordFrame.JOINT);
		}

		updateCoordFrame();
	}

	/**
	 * Transition back to the World Frame, if the current Frame is Tool or User and there are no active frame
	 * set for that Coordinate Frame. This method will halt the motion of the Robot if the active frame is changed.
	 */
	public void updateCoordFrame() {

		// Return to the World Frame, if no User Frame is active
		if(armModel.getCurCoordFrame() == CoordFrame.TOOL && !(armModel.getActiveToolFrame() >= 0 && armModel.getActiveToolFrame() < Fields.FRAME_SIZE)) {
			armModel.setCurCoordFrame(CoordFrame.WORLD);
			// Stop Robot movement
			armModel.halt();
		}

		// Return to the World Frame, if no User Frame is active
		if(armModel.getCurCoordFrame() == CoordFrame.USER && !(armModel.getActiveUserFrame() >= 0 && armModel.getActiveUserFrame() < Fields.FRAME_SIZE)) {
			armModel.setCurCoordFrame(CoordFrame.WORLD);
			// Stop Robot movement
			armModel.halt();
		}
	}

	/**
	 * Returns a point containing the Robot's faceplate position and orientation
	 * corresponding to the given joint angles, as well as the given joint angles.
	 * 
	 * @param jointAngles  A valid set of six joint angles (in radians) for the
	 *                     Robot
	 * @returning          The Robot's faceplate position and orientation
	 *                     corresponding to the given joint angles
	 */
	public static Point nativeRobotPoint(float[] jointAngles) {
		// Return a point containing the faceplate position, orientation, and joint angles
		return nativeRobotPointOffset(jointAngles, new PVector(0f, 0f, 0f));
	}

	/**
	 * Returns a point containing the Robot's End Effector position and orientation
	 * corresponding to the given joint angles, as well as the given joint angles.
	 * 
	 * @param jointAngles  A valid set of six joint angles (in radians) for the Robot
	 * @param offset       The End Effector offset in the form of a vector
	 * @returning          The Robot's EE position and orientation corresponding to
	 *                     the given joint angles
	 */
	public static Point nativeRobotPointOffset(float[] jointAngles, PVector offset) {
		instance.pushMatrix();
		instance.resetMatrix();
		applyModelRotation(jointAngles);
		// Apply offset
		PVector ee = instance.getCoordFromMatrix(offset.x, offset.y, offset.z);
		float[][] orientationMatrix = instance.getRotationMatrix();
		instance.popMatrix();
		// Return a Point containing the EE position, orientation, and joint angles
		return new Point(ee, matrixToQuat(orientationMatrix), jointAngles);
	}

	/**
	 * Returns the Robot's End Effector position according to the active Tool Frame's
	 * offset in the native Coordinate System.
	 * 
	 * @param jointAngles  A valid set of six joint angles (in radians) for the Robot
	 * @returning          The Robot's End Effector position
	 */
	public static Point nativeRobotEEPoint(float[] jointAngles) {
		Frame activeTool = instance.armModel.getActiveFrame(CoordFrame.TOOL);
		PVector offset;

		if (activeTool != null) {
			// Apply the Tool Tip
			offset = ((ToolFrame)activeTool).getTCPOffset();
		} else {
			offset = new PVector(0f, 0f, 0f);
		}

		return nativeRobotPointOffset(jointAngles, offset);
	}

	/**
	 * Takes a vector and a (probably not quite orthogonal) second vector
	 * and computes a vector that's truly orthogonal to the first one and
	 * pointing in the direction closest to the imperfect second vector
	 * @param in First vector
	 * @param second Second vector
	 * @return A vector perpendicular to the first one and on the same side
	 *         from first as the second one.
	 */
	public PVector computePerpendicular(PVector in, PVector second) {
		PVector[] plane = createPlaneFrom3Points(in, second, new PVector(in.x*2, in.y*2, in.z*2));
		PVector v1 = vectorConvertTo(in, plane[0], plane[1], plane[2]);
		PVector v2 = vectorConvertTo(second, plane[0], plane[1], plane[2]);
		PVector perp1 = new PVector(v1.y, -v1.x, v1.z);
		PVector perp2 = new PVector(-v1.y, v1.x, v1.z);
		PVector orig = new PVector(v2.x*5, v2.y*5, v2.z);
		PVector p1 = new PVector(perp1.x*5, perp1.y*5, perp1.z);
		PVector p2 = new PVector(perp2.x*5, perp2.y*5, perp2.z);

		if(dist(orig.x, orig.y, orig.z, p1.x, p1.y, p1.z) <
				dist(orig.x, orig.y, orig.z, p2.x, p2.y, p2.z))
			return vectorConvertFrom(perp1, plane[0], plane[1], plane[2]);
		else return vectorConvertFrom(perp2, plane[0], plane[1], plane[2]);
	}

	/**
	 * Calculate the Jacobian matrix for the robotic arm for
	 * a given set of joint rotational values using a 1 DEGREE
	 * offset for each joint rotation value. Each cell of the
	 * resulting matrix will describe the linear approximation
	 * of the robot's motion for each joint in units per radian. 
	 */
	public static float[][] calculateJacobian(float[] angles, boolean posOffset) {
		float dAngle = DEG_TO_RAD;
		if (!posOffset){ dAngle *= -1; }

		float[][] J = new float[7][6];
		//get current ee position
		Point curRP = nativeRobotEEPoint(angles);

		//examine each segment of the arm
		for(int i = 0; i < 6; i += 1) {
			//test angular offset
			angles[i] += dAngle;
			//get updated ee position
			Point newRP = nativeRobotEEPoint(angles);

			if (curRP.orientation.dot(newRP.orientation) < 0f) {
				// Use -q instead of q
				newRP.orientation.scalarMult(-1);
			}

			newRP.orientation.addValues( RQuaternion.scalarMult(-1, curRP.orientation) );

			//get translational delta
			J[0][i] = (newRP.position.x - curRP.position.x) / DEG_TO_RAD;
			J[1][i] = (newRP.position.y - curRP.position.y) / DEG_TO_RAD;
			J[2][i] = (newRP.position.z - curRP.position.z) / DEG_TO_RAD;
			//get rotational delta        
			J[3][i] = newRP.orientation.getValue(0) / DEG_TO_RAD;
			J[4][i] = newRP.orientation.getValue(1) / DEG_TO_RAD;
			J[5][i] = newRP.orientation.getValue(2) / DEG_TO_RAD;
			J[6][i] = newRP.orientation.getValue(3) / DEG_TO_RAD;
			//replace the original rotational value
			angles[i] -= dAngle;
		}

		return J;
	}

	/**
	 * Attempts to calculate the joint angles that would place the Robot in the given target position and
	 * orientation. The srcAngles parameter defines the position of the Robot from which to move, since
	 * this inverse kinematics uses a relative conversion formula. There is no guarantee that the target
	 * position and orientation can be reached; in the case that inverse kinematics fails, then null is
	 * returned. Otherwise, a set of six angles will be returned, though there is no guarantee that these
	 * angles are valid!
	 * 
	 * @param srcAngles       The initial position of the Robot
	 * @param tgtPosition     The desired position of the Robot
	 * @param tgtOrientation  The desited orientation of the Robot
	 */
	public static float[] inverseKinematics(float[] srcAngles, PVector tgtPosition, RQuaternion tgtOrientation) {
		final int limit = 1000;  // Max number of times to loop
		int count = 0;

		float[] angles = srcAngles.clone();

		while(count < limit) {
			Point cPoint = nativeRobotEEPoint(angles);

			if (tgtOrientation.dot(cPoint.orientation) < 0f) {
				// Use -q instead of q
				tgtOrientation.scalarMult(-1);
			}

			//calculate our translational offset from target
			PVector tDelta = PVector.sub(tgtPosition, cPoint.position);
			//calculate our rotational offset from target
			RQuaternion rDelta = RQuaternion.addValues(tgtOrientation, RQuaternion.scalarMult(-1, cPoint.orientation));
			float[] delta = new float[7];

			delta[0] = tDelta.x;
			delta[1] = tDelta.y;
			delta[2] = tDelta.z;
			delta[3] = rDelta.getValue(0);
			delta[4] = rDelta.getValue(1);
			delta[5] = rDelta.getValue(2);
			delta[6] = rDelta.getValue(3);

			float dist = PVector.dist(cPoint.position, tgtPosition);
			float rDist = rDelta.magnitude();
			//check whether our current position is within tolerance
			if ( (dist < (instance.liveSpeed / 100f)) && (rDist < (0.00005f * instance.liveSpeed)) ) { break; }

			//calculate jacobian, 'J', and its inverse
			float[][] J = calculateJacobian(angles, true);
			RealMatrix m = new Array2DRowRealMatrix(floatToDouble(J, 7, 6));
			RealMatrix JInverse = new SingularValueDecomposition(m).getSolver().getInverse();

			//calculate and apply joint angular changes
			float[] dAngle = {0, 0, 0, 0, 0, 0};
			for(int i = 0; i < 6; i += 1) {
				for(int j = 0; j < 7; j += 1) {
					dAngle[i] += JInverse.getEntry(i, j)*delta[j];
				}

				//update joint angles
				angles[i] += dAngle[i];
				angles[i] += TWO_PI;
				angles[i] %= TWO_PI;
			}

			count += 1;
			if (count == limit) {
				// IK failure
				//if (DISPLAY_TEST_OUTPUT) {
				//  System.out.printf("\nDelta: %s\nAngles: %s\n%s\n%s -> %s\n", arrayToString(delta), arrayToString(angles),
				//                      matrixToString(J), cPoint.orientation, tgtOrientation);
				//}

				return null;
			}
		}

		return angles;
	}

	/**
	 * Determine how close together intermediate points between two points
	 * need to be based on current speed
	 */
	public void calculateDistanceBetweenPoints() {
		MotionInstruction instruction = activeMotionInst();
		if(instruction != null && instruction.getMotionType() != MTYPE_JOINT)
			distanceBetweenPoints = instruction.getSpeed() / 60.0f;
		else if(armModel.getCurCoordFrame() != CoordFrame.JOINT)
			distanceBetweenPoints = armModel.motorSpeed * liveSpeed / 6000f;
		else distanceBetweenPoints = 5.0f;
	}

	/**
	 * Calculate a "path" (series of intermediate positions) between two
	 * points in a straight line.
	 * @param start Start point
	 * @param end Destination point
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
		int numberOfPoints = (int)(dist(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z) / distanceBetweenPoints);
		float increment = 1.0f / (float)numberOfPoints;
		for(int n = 0; n < numberOfPoints; n++) {
			mu += increment;

			qi = RQuaternion.SLERP(q1, q2, mu);
			intermediatePositions.add(new Point(new PVector(
					p1.x * (1 - mu) + (p2.x * mu),
					p1.y * (1 - mu) + (p2.y * mu),
					p1.z * (1 - mu) + (p2.z * mu)),
					qi));
		}

		interMotionIdx = 0;
	} // end calculate intermediate positions

	/**
	 * Calculate a "path" (series of intermediate positions) between two
	 * points in a a curved line. Need a third point as well, or a curved
	 * line doesn't make sense.
	 * Here's how this works:
	 *   Assuming our current point is P1, and we're moving to P2 and then P3:
	 *   1 Do linear interpolation between points P2 and P3 FIRST.
	 *   2 Begin interpolation between P1 and P2.
	 *   3 When you're (cont% / 1.5)% away from P2, begin interpolating not towards
	 *     P2, but towards the points defined between P2 and P3 in step 1.
	 *   The mu for this is from 0 to 0.5 instead of 0 to 1.0.
	 *
	 * @param p1 Start point
	 * @param p2 Destination point
	 * @param p3 Third point, needed to figure out how to curve the path
	 * @param percentage Intensity of the curve
	 */
	public void calculateContinuousPositions(Point start, Point end, Point next, float percentage) {
		//percentage /= 2;
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

		ArrayList<Point> secondaryTargets = new ArrayList<Point>();
		float d1 = dist(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z);
		float d2 = dist(p2.x, p2.y, p2.z, p3.x, p3.y, p3.z);
		int numberOfPoints = 0;
		if(d1 > d2) {
			numberOfPoints = (int)(d1 / distanceBetweenPoints);
		} 
		else {
			numberOfPoints = (int)(d2 / distanceBetweenPoints);
		}

		float mu = 0;
		float increment = 1.0f / (float)numberOfPoints;
		for(int n = 0; n < numberOfPoints; n++) {
			mu += increment;
			qi = RQuaternion.SLERP(q2, q3, mu);
			secondaryTargets.add(new Point(new PVector(
					p2.x * (1 - mu) + (p3.x * mu),
					p2.y * (1 - mu) + (p3.y * mu),
					p2.z * (1 - mu) + (p3.z * mu)),
					qi));
		}

		mu = 0;
		int transitionPoint = (int)((float)numberOfPoints * percentage);
		for(int n = 0; n < transitionPoint; n++) {
			mu += increment;
			qi = RQuaternion.SLERP(q1, q2, mu);
			intermediatePositions.add(new Point(new PVector(
					p1.x * (1 - mu) + (p2.x * mu),
					p1.y * (1 - mu) + (p2.y * mu),
					p1.z * (1 - mu) + (p2.z * mu)),
					qi));
		}

		int secondaryIdx = 0; // accessor for secondary targets

		mu = 0;
		increment /= 2.0f;

		Point currentPoint;
		if(intermediatePositions.size() > 0) {
			currentPoint = intermediatePositions.get(intermediatePositions.size()-1);
		}
		else {
			// NOTE orientation is in Native Coordinates!
			currentPoint = nativeRobotEEPoint(armModel.getJointAngles());
		}

		for(int n = transitionPoint; n < numberOfPoints; n++) {
			mu += increment;
			Point tgt = secondaryTargets.get(secondaryIdx);
			qi = RQuaternion.SLERP(currentPoint.orientation, tgt.orientation, mu);
			intermediatePositions.add(new Point(new PVector(
					currentPoint.position.x * (1 - mu) + (tgt.position.x * mu),
					currentPoint.position.y * (1 - mu) + (tgt.position.y * mu),
					currentPoint.position.z * (1 - mu) + (tgt.position.z * mu)), 
					qi));
			currentPoint = intermediatePositions.get(intermediatePositions.size()-1);
			secondaryIdx++;
		}
		interMotionIdx = 0;
	} // end calculate continuous positions

	/**
	 * Creates an arc from 'start' to 'end' that passes through the point specified
	 * by 'inter.'
	 * @param start First point
	 * @param inter Second point
	 * @param end Third point
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
				vectorConvertTo(b, plane[0], plane[1], plane[2]),
				vectorConvertTo(c, plane[0], plane[1], plane[2]));
		center = vectorConvertFrom(center, plane[0], plane[1], plane[2]);
		// Now get the radius (easy)
		float r = dist(center.x, center.y, center.z, a.x, a.y, a.z);
		// Calculate a vector from the center to point a
		PVector u = new PVector(a.x-center.x, a.y-center.y, a.z-center.z);
		u.normalize();
		// get the normal of the plane created by the 3 input points
		PVector tmp1 = new PVector(a.x-b.x, a.y-b.y, a.z-b.z);
		PVector tmp2 = new PVector(a.x-c.x, a.y-c.y, a.z-c.z);
		PVector n = tmp1.cross(tmp2);
		tmp1.normalize();
		tmp2.normalize();
		n.normalize();
		// calculate the angle between the start and end points
		PVector vec1 = new PVector(a.x-center.x, a.y-center.y, a.z-center.z);
		PVector vec2 = new PVector(c.x-center.x, c.y-center.y, c.z-center.z);
		vec1.normalize();
		vec2.normalize();
		float theta = atan2(vec1.cross(vec2).mag(), vec1.dot(vec2));

		// finally, draw an arc through all 3 points by rotating the u
		// vector around our normal vector
		float angle = 0, mu = 0;
		int numPoints = (int)(r*theta/distanceBetweenPoints);
		float inc = 1/(float)numPoints;
		float angleInc = (theta)/(float)numPoints;
		for(int i = 0; i < numPoints; i += 1) {
			PVector pos = RQuaternion.rotateVectorAroundAxis(u, n, angle).mult(r).add(center);
			if(i == numPoints-1) pos = end.position;
			qi = RQuaternion.SLERP(q1, q2, mu);
			//println(pos + ", " + end.position);
			intermediatePositions.add(new Point(pos, qi));
			angle += angleInc;
			mu += inc;
		}
	}

	/**
	 * Initiate a new continuous (curved) motion instruction.
	 * @param model Arm model to use
	 * @param start Start point
	 * @param end Destination point
	 * @param next Point after the destination
	 * @param percentage Intensity of the curve
	 */
	public void beginNewContinuousMotion(Point start, Point end, Point next, float p) {
		calculateContinuousPositions(start, end, next, p);
		motionFrameCounter = 0;
		if(intermediatePositions.size() > 0) {
			Point tgtPoint = intermediatePositions.get(interMotionIdx);
			armModel.jumpTo(tgtPoint.position, tgtPoint.orientation);
		}
	}

	/**
	 * Initiate a new fine (linear) motion instruction.
	 * @param start Start point
	 * @param end Destination point
	 */
	public void beginNewLinearMotion(Point start, Point end) {
		calculateIntermediatePositions(start, end);
		motionFrameCounter = 0;
		if(intermediatePositions.size() > 0) {
			Point tgtPoint = intermediatePositions.get(interMotionIdx);
			armModel.jumpTo(tgtPoint.position, tgtPoint.orientation);
		}
	}

	/**
	 * Initiate a new circular motion instruction according to FANUC methodology.
	 * @param p1 Point 1
	 * @param p2 Point 2
	 * @param p3 Point 3
	 */
	public void beginNewCircularMotion(Point start, Point inter, Point end) {
		calculateArc(start, inter, end);
		interMotionIdx = 0;
		motionFrameCounter = 0;
		if(intermediatePositions.size() > 0) {
			Point tgtPoint = intermediatePositions.get(interMotionIdx);
			armModel.jumpTo(tgtPoint.position, tgtPoint.orientation);
		}
	}

	/**
	 * Move the arm model between two points according to its current speed.
	 * @param model The arm model
	 * @param speedMult Speed multiplier
	 */
	public boolean executeMotion(ArmModel model, float speedMult) {
		motionFrameCounter++;
		// speed is in pixels per frame, multiply that by the current speed setting
		// which is contained in the motion instruction
		float currentSpeed = model.motorSpeed * speedMult;
		if(currentSpeed * motionFrameCounter > distanceBetweenPoints) {
			interMotionIdx++;
			motionFrameCounter = 0;
			if(interMotionIdx >= intermediatePositions.size()) {
				interMotionIdx = -1;
				return true;
			}

			int ret = EXEC_SUCCESS;
			if(intermediatePositions.size() > 0) {
				Point tgtPoint = intermediatePositions.get(interMotionIdx);
				ret = armModel.jumpTo(tgtPoint.position, tgtPoint.orientation);
			}

			if(ret == EXEC_FAILURE) {
				triggerFault();
				return true;
			}
		}

		return false;
	} // end execute linear motion

	/**
	 * Convert a point based on a coordinate system defined as
	 * 3 orthonormal vectors.
	 * @param point Point to convert
	 * @param xAxis X axis of target coordinate system
	 * @param yAxis Y axis of target coordinate system
	 * @param zAxis Z axis of target coordinate system
	 * @return Coordinates of point after conversion
	 */
	public PVector vectorConvertTo(PVector point, PVector xAxis,
			PVector yAxis, PVector zAxis)
	{
		PMatrix3D matrix = new PMatrix3D(xAxis.x, xAxis.y, xAxis.z, 0,
				yAxis.x, yAxis.y, yAxis.z, 0,
				zAxis.x, zAxis.y, zAxis.z, 0,
				0,       0,       0,       1);
		PVector result = new PVector();
		matrix.mult(point, result);
		return result;
	}


	/**
	 * Convert a point based on a coordinate system defined as
	 * 3 orthonormal vectors. Reverse operation of vectorConvertTo.
	 * @param point Point to convert
	 * @param xAxis X axis of target coordinate system
	 * @param yAxis Y axis of target coordinate system
	 * @param zAxis Z axis of target coordinate system
	 * @return Coordinates of point after conversion
	 */
	public PVector vectorConvertFrom(PVector point, PVector xAxis,
			PVector yAxis, PVector zAxis)
	{
		PMatrix3D matrix = new PMatrix3D(xAxis.x, yAxis.x, zAxis.x, 0,
				xAxis.y, yAxis.y, zAxis.y, 0,
				xAxis.z, yAxis.z, zAxis.z, 0,
				0,       0,       0,       1);
		PVector result = new PVector();
		matrix.mult(point, result);
		return result;
	}


	/**
	 * Create a plane (2D coordinate system) out of 3 input points.
	 * @param a First point
	 * @param b Second point
	 * @param c Third point
	 * @return New coordinate system defined by 3 orthonormal vectors
	 */
	public PVector[] createPlaneFrom3Points(PVector a, PVector b, PVector c) {  
		PVector n1 = new PVector(a.x-b.x, a.y-b.y, a.z-b.z);
		n1.normalize();
		PVector n2 = new PVector(a.x-c.x, a.y-c.y, a.z-c.z);
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

	/**
	 * Finds the circle center of 3 points. (That is, find the center of
	 * a circle whose circumference intersects all 3 points.)
	 * The points must all lie
	 * on the same plane (all have the same Z value). Should have a check
	 * for colinear case, currently doesn't.
	 * @param a First point
	 * @param b Second point
	 * @param c Third point
	 * @return Position of circle center
	 */
	public PVector circleCenter(PVector a, PVector b, PVector c) {
		float h = calculateH(a.x, a.y, b.x, b.y, c.x, c.y);
		float k = calculateK(a.x, a.y, b.x, b.y, c.x, c.y);
		return new PVector(h, k, a.z);
	}

	// TODO: Add error check for colinear case (denominator is zero)
	public float calculateH(float x1, float y1, float x2, float y2, float x3, float y3) {
		float numerator = (x2*x2+y2*y2)*y3 - (x3*x3+y3*y3)*y2 - 
				((x1*x1+y1*y1)*y3 - (x3*x3+y3*y3)*y1) +
				(x1*x1+y1*y1)*y2 - (x2*x2+y2*y2)*y1;
		float denominator = (x2*y3-x3*y2) -
				(x1*y3-x3*y1) +
				(x1*y2-x2*y1);
		denominator *= 2;
		return numerator / denominator;
	}

	public float calculateK(float x1, float y1, float x2, float y2, float x3, float y3) {
		float numerator = x2*(x3*x3+y3*y3) - x3*(x2*x2+y2*y2) -
				(x1*(x3*x3+y3*y3) - x3*(x1*x1+y1*y1)) +
				x1*(x2*x2+y2*y2) - x2*(x1*x1+y1*y1);
		float denominator = (x2*y3-x3*y2) -
				(x1*y3-x3*y1) +
				(x1*y2-x2*y1);
		denominator *= 2;
		return numerator / denominator;
	}

	/**
	 * Executes a program. Returns true when done.
	 * @param program - Program to execute
	 * @param model   - Arm model to use
	 * @return        - True if done executing, false if otherwise.
	 */
	public boolean executeProgram(Program program, ArmModel model, boolean singleInstr) {
		Instruction activeInstr = activeInstruction();
		int nextInstr = getActive_instr() + 1;

		//stop executing if no valid program is selected or we reach the end of the program
		if(motionFault || activeInstr == null) {
			return true;
		}
		else if (!activeInstr.isCommented()){
			if (activeInstr instanceof MotionInstruction) {
				MotionInstruction motInstr = (MotionInstruction)activeInstr;

				//start a new instruction
				if(!isExecutingInstruction()) {
					setExecutingInstruction(setUpInstruction(program, model, motInstr));

					if (!isExecutingInstruction()) {
						// Motion Instruction failed
						nextInstr = -1;
					}
				}
				//continue current motion instruction
				else {
					if(motInstr.getMotionType() == MTYPE_JOINT) {
						setExecutingInstruction(!(model.interpolateRotation(motInstr.getSpeedForExec(model))));  
					}
					else {  
						setExecutingInstruction(!(executeMotion(model, motInstr.getSpeedForExec(model))));
					}
				}
			} 
			else if (activeInstr instanceof JumpInstruction) {
				setExecutingInstruction(false);
				nextInstr = activeInstr.execute();

			} else if (activeInstr instanceof CallInstruction) {
				setExecutingInstruction(false);
				nextInstr = activeInstr.execute();

			} else {
				setExecutingInstruction(false);

				if(activeInstr.execute() != 0) {
					nextInstr = -1;
				}
			}//end of instruction type check
		} //skip commented instructions

		// Move to next instruction after current is finished
		if(!isExecutingInstruction()) {
			if (nextInstr == -1) {
				// If a command fails
				triggerFault();
				return true;

			}
			else {
				// Move to nextInstruction
				int size = activeProgram().getInstructions().size() + 1;      
				setActive_instr(max(0, min(nextInstr, size - 1)));
				if(display_stack.peek() == Screen.NAV_PROG_INSTR)
					setRow_select(getInstrLine(getActive_instr()));
			}

			updateScreen();
		}

		return (!isExecutingInstruction() && singleInstr);
	}//end executeProgram

	/**
	 * Sets up an instruction for execution.
	 *
	 * @param program Program that the instruction belongs to
	 * @param model Arm model to use
	 * @param instruction The instruction to execute
	 * @return Returns false on failure (invalid instruction), true on success
	 */
	public boolean setUpInstruction(Program program, ArmModel model, MotionInstruction instruction) {
		Point start = nativeRobotEEPoint(model.getJointAngles());

		if (!instruction.checkFrames(armModel.getActiveToolFrame(), armModel.getActiveUserFrame())) {
			// Current Frames must match the instruction's frames
			System.out.printf("Tool frame: %d : %d\nUser frame: %d : %d\n\n", instruction.getToolFrame(),
					armModel.getActiveToolFrame(), instruction.getUserFrame(), armModel.getActiveUserFrame());
			return false;
		} else if(instruction.getVector(program) == null) {
			return false;
		}

		if(instruction.getMotionType() == MTYPE_JOINT) {
			armModel.setupRotationInterpolation(instruction.getVector(program).angles);
		} // end joint movement setup
		else if(instruction.getMotionType() == MTYPE_LINEAR) {

			if(instruction.getTermination() == 0 || execSingleInst) {
				beginNewLinearMotion(start, instruction.getVector(program));
			} 
			else {
				Point nextPoint = null;
				for(int n = getActive_instr()+1; n < program.getInstructions().size(); n++) {
					Instruction nextIns = program.getInstructions().get(n);
					if(nextIns instanceof MotionInstruction) {
						MotionInstruction castIns = (MotionInstruction)nextIns;
						nextPoint = castIns.getVector(program);
						break;
					}
				}
				if(nextPoint == null) {
					beginNewLinearMotion(start, instruction.getVector(program));
				} 
				else {
					beginNewContinuousMotion(start, 
							instruction.getVector(program),
							nextPoint, 
							instruction.getTermination() / 100f);
				}
			} // end if termination type is continuous
		} // end linear movement setup
		else if(instruction.getMotionType() == MTYPE_CIRCULAR) {
			MotionInstruction nextIns = instruction.getSecondaryPoint();
			Point nextPoint = nextIns.getVector(program);

			beginNewCircularMotion(start, instruction.getVector(program), nextPoint);
		} // end circular movement setup

		return true;
	} // end setUpInstruction

	/**
	 * Stop robot motion, program execution
	 */
	public void triggerFault() {
		armModel.halt();
		motionFault = true;
	}

	/**
	 * Returns a string represenation of the given matrix.
	 * 
	 * @param matrix  A non-null matrix
	 */
	public String matrixToString(float[][] matrix) {
		String mStr = "";

		for(int row = 0; row < matrix.length; ++row) {
			mStr += "\n[";

			for(int col = 0; col < matrix[0].length; ++col) {
				// Account for the negative sign character
				if(matrix[row][col] >= 0) { mStr += " "; }

				mStr += String.format(" %5.6f", matrix[row][col]);
			}

			mStr += "  ]";
		}

		return (mStr + "\n");
	}

	public String arrayToString(float[] array) {
		String s = "[";

		for(int i = 0; i < array.length; i += 1) {
			s += String.format("%5.4f", array[i]);
			if(i != array.length-1) s += ", ";
		}

		return s + "]";
	}



	public void gui() {
		g1_px = 0;
		g1_py = (Fields.SMALL_BUTTON - 15) + 1;
		g1_width = 440;
		g1_height = 720;
		display_px = 10;
		display_py = 0;//(Fields.SMALL_BUTTON - 15) + 1;
		display_width = g1_width - 20;
		display_height = 280;

		display_stack.push(Screen.DEFAULT);
		mode = display_stack.peek();

		// group 1: display and function buttons
		g1 = cp5.addGroup("DISPLAY")
				.setPosition(g1_px, g1_py)
				.setBackgroundColor(color(127,127,127,100))
				.setWidth(g1_width)
				.setHeight(g1_height)
				.setBackgroundHeight(g1_height)
				.hideBar();

		cp5.addTextarea("txt")
		.setPosition(display_px, 0)
		.setSize(display_width, display_height)
		.setColorBackground(Fields.UI_LIGHT)
		.moveTo(g1);

		/**********************Top row buttons**********************/

		//calculate how much space each button will be given
		int button_offsetX = Fields.LARGE_BUTTON + 1;
		int button_offsetY = Fields.LARGE_BUTTON + 1;  

		int record_normal_px = WindowManager.lButtonWidth * 5 + Fields.LARGE_BUTTON + 1;
		int record_normal_py = 0;   
		PImage[] record = {loadImage("images/record-35x20.png"), 
				loadImage("images/record-over.png"), 
				loadImage("images/record-on.png")};   
		bt_record_normal = cp5.addButton("record_normal")
				.setPosition(record_normal_px, record_normal_py)
				.setSize(Fields.SMALL_BUTTON, Fields.SMALL_BUTTON)
				.setImages(record)
				.updateSize();     

		int EE_normal_px = record_normal_px + Fields.LARGE_BUTTON + 1;
		int EE_normal_py = 0;   
		PImage[] EE = {loadImage("images/EE_35x20.png"), 
				loadImage("images/EE_over.png"), 
				loadImage("images/EE_down.png")};   
		bt_ee_normal = cp5.addButton("EE")
				.setPosition(EE_normal_px, EE_normal_py)
				.setSize(Fields.SMALL_BUTTON, Fields.SMALL_BUTTON)
				.setImages(EE)
				.updateSize();

		/********************Function Row********************/

		int f1_px = display_px;
		int f1_py = display_py + display_height + 2;
		int f_width = display_width/5 - 1;
		cp5.addButton("f1")
		.setPosition(f1_px, f1_py)
		.setSize(f_width, Fields.LARGE_BUTTON)
		.setCaptionLabel("F1")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);     

		int f2_px = f1_px + f_width + 1;
		int f2_py = f1_py;
		cp5.addButton("f2")
		.setPosition(f2_px, f2_py)
		.setSize(f_width, Fields.LARGE_BUTTON)
		.setCaptionLabel("F2")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);  

		int f3_px = f2_px + f_width + 1;
		int f3_py = f2_py;
		cp5.addButton("f3")
		.setPosition(f3_px, f3_py)
		.setSize(f_width, Fields.LARGE_BUTTON)
		.setCaptionLabel("F3")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);    

		int f4_px = f3_px + f_width + 1;
		int f4_py = f3_py;   
		cp5.addButton("f4")
		.setPosition(f4_px, f4_py)
		.setSize(f_width, Fields.LARGE_BUTTON)
		.setCaptionLabel("F4")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);   

		int f5_px = f4_px + f_width + 1;
		int f5_py = f4_py;   
		cp5.addButton("f5")
		.setPosition(f5_px, f5_py)
		.setSize(f_width, Fields.LARGE_BUTTON)
		.setCaptionLabel("F5")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);

		/**********************Step/Shift Row**********************/

		int st_px = f1_px;
		int st_py = f1_py + button_offsetY + 10;   
		cp5.addButton("st")
		.setPosition(st_px, st_py)
		.setSize(Fields.LARGE_BUTTON, Fields.LARGE_BUTTON)
		.setCaptionLabel("STEP")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);

		int mu_px = st_px + Fields.LARGE_BUTTON + 19;
		int mu_py = st_py;   
		cp5.addButton("mu")
		.setPosition(mu_px, mu_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("MENU")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);

		int se_px = mu_px + Fields.LARGE_BUTTON + 15;
		int se_py = mu_py;
		cp5.addButton("se")
		.setPosition(se_px, se_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("SELECT")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);     

		int ed_px = se_px + button_offsetX;
		int ed_py = se_py;   
		cp5.addButton("ed")
		.setPosition(ed_px, ed_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("EDIT")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);      

		int da_px = ed_px + button_offsetX;
		int da_py = ed_py;   
		cp5.addButton("da")
		.setPosition(da_px, da_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("DATA")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);

		int fn_px = da_px + Fields.LARGE_BUTTON + 15;
		int fn_py = da_py;   
		cp5.addButton("Fn")
		.setPosition(fn_px, fn_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("FCTN")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);

		int sf_px = fn_px + Fields.LARGE_BUTTON + 19;
		int sf_py = fn_py;
		cp5.addButton("sf")
		.setPosition(sf_px, sf_py)
		.setSize(Fields.LARGE_BUTTON, Fields.LARGE_BUTTON)
		.setCaptionLabel("SHIFT")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);

		int pr_px = mu_px;
		int pr_py = mu_py + button_offsetY;   
		cp5.addButton("pr")
		.setPosition(pr_px, pr_py + 15)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("PREV")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);

		int ne_px = fn_px;
		int ne_py = mu_py + button_offsetY;
		cp5.addButton("ne")
		.setPosition(ne_px, ne_py + 15)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("NEXT")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);

		/***********************Arrow Keys***********************/
		button_offsetY = Fields.SMALL_BUTTON + 1;

		PImage[] imgs_arrow_up = {loadImage("images/arrow-up.png"), 
				loadImage("images/arrow-up_over.png"), 
				loadImage("images/arrow-up_down.png")};   
		int up_px = ed_px + 5;
		int up_py = ed_py + button_offsetY + 10;
		cp5.addButton("up")
		.setPosition(up_px, up_py)
		.setSize(Fields.SMALL_BUTTON, Fields.SMALL_BUTTON)
		.setImages(imgs_arrow_up)
		.updateSize()
		.moveTo(g1);     

		PImage[] imgs_arrow_down = {loadImage("images/arrow-down.png"), 
				loadImage("images/arrow-down_over.png"), 
				loadImage("images/arrow-down_down.png")};   
		int dn_px = up_px;
		int dn_py = up_py + button_offsetY;
		cp5.addButton("dn")
		.setPosition(dn_px, dn_py)
		.setSize(Fields.SMALL_BUTTON, Fields.SMALL_BUTTON)
		.setImages(imgs_arrow_down)
		.updateSize()
		.moveTo(g1);    

		PImage[] imgs_arrow_l = {loadImage("images/arrow-l.png"), 
				loadImage("images/arrow-l_over.png"), 
				loadImage("images/arrow-l_down.png")};
		int lt_px = dn_px - button_offsetX;
		int lt_py = dn_py - button_offsetY/2;
		cp5.addButton("lt")
		.setPosition(lt_px, lt_py)
		.setSize(Fields.SMALL_BUTTON, Fields.SMALL_BUTTON)
		.setImages(imgs_arrow_l)
		.updateSize()
		.moveTo(g1);  

		PImage[] imgs_arrow_r = {loadImage("images/arrow-r.png"), 
				loadImage("images/arrow-r_over.png"), 
				loadImage("images/arrow-r_down.png")};
		int rt_px = dn_px + button_offsetX;
		int rt_py = lt_py;
		cp5.addButton("rt")
		.setPosition(rt_px, rt_py)
		.setSize(Fields.SMALL_BUTTON, Fields.SMALL_BUTTON)
		.setImages(imgs_arrow_r)
		.updateSize()
		.moveTo(g1);      

		//--------------------------------------------------------------//
		//                           Group 2                            //
		//--------------------------------------------------------------//
		int g2_offsetY = display_py + display_height + 4*Fields.LARGE_BUTTON - 10;

		/**********************Numpad Block*********************/

		int LINE_px = ed_px - 7*button_offsetX/2;
		int LINE_py = g2_offsetY + 5*button_offsetY;
		cp5.addButton("LINE")
		.setPosition(LINE_px, LINE_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("-")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);    

		int PERIOD_px = LINE_px + button_offsetX;
		int PERIOD_py = LINE_py - button_offsetY;
		cp5.addButton("PERIOD")
		.setPosition(PERIOD_px, PERIOD_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel(".")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);   

		int COMMA_px = PERIOD_px + button_offsetX;
		int COMMA_py = PERIOD_py;
		cp5.addButton("COMMA")
		.setPosition(COMMA_px, COMMA_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel(",")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);

		int POSN_px = LINE_px + button_offsetX;
		int POSN_py = LINE_py;
		cp5.addButton("POSN")
		.setPosition(POSN_px, POSN_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("POSN")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);

		int IO_px = POSN_px + button_offsetX;
		int IO_py = POSN_py;
		cp5.addButton("IO")
		.setPosition(IO_px, IO_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("I/O")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);

		int NUM_px = LINE_px;
		int NUM_py = LINE_py - button_offsetY;
		for(int i = 0; i < 10; i += 1) {
			cp5.addButton("NUM"+i)
			.setPosition(NUM_px, NUM_py)
			.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
			.setCaptionLabel(""+i)
			.setColorBackground(Fields.BUTTON_DEFAULT)
			.setColorCaptionLabel(Fields.BUTTON_TEXT)
			.moveTo(g1);

			if(i % 3 == 0) {
				NUM_px = LINE_px;
				NUM_py -= button_offsetY;
			}
			else {
				NUM_px += button_offsetX;
			}
		}

		int RESET_px = LINE_px;
		int RESET_py = NUM_py;
		cp5.addButton("RESET")
		.setPosition(RESET_px, RESET_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("RESET")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);   

		int BKSPC_px = RESET_px + button_offsetX;
		int BKSPC_py = RESET_py;
		cp5.addButton("BKSPC")
		.setPosition(BKSPC_px, BKSPC_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("BKSPC")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);      

		int ITEM_px = BKSPC_px + button_offsetX;
		int ITEM_py = BKSPC_py;
		cp5.addButton("ITEM")
		.setPosition(ITEM_px, ITEM_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("ITEM")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);

		/***********************Util Block*************************/

		int ENTER_px = ed_px;
		int ENTER_py = g2_offsetY;
		cp5.addButton("ENTER")
		.setPosition(ENTER_px, ENTER_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("ENTER")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);    

		int TOOL1_px = ENTER_px;
		int TOOL1_py = ENTER_py + button_offsetY;
		cp5.addButton("TOOL1")
		.setPosition(TOOL1_px, TOOL1_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("TOOL1")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);   

		int TOOL2_px = TOOL1_px;
		int TOOL2_py = TOOL1_py + button_offsetY;
		cp5.addButton("TOOL2")
		.setPosition(TOOL2_px, TOOL2_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("TOOL2")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);

		int MOVEMENU_px = TOOL2_px;
		int MOVEMENU_py = TOOL2_py + button_offsetY;
		cp5.addButton("MVMU")
		.setPosition(MOVEMENU_px, MOVEMENU_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("MVMU")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1); 

		int SETUP_px = MOVEMENU_px;
		int SETUP_py = MOVEMENU_py + button_offsetY;
		cp5.addButton("SETUP")
		.setPosition(SETUP_px, SETUP_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("SETUP")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);    

		int STATUS_px = SETUP_px;
		int STATUS_py = SETUP_py + button_offsetY;
		cp5.addButton("STATUS")
		.setPosition(STATUS_px, STATUS_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("STATUS")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);

		/********************Joint Control Block*******************/

		int hd_px = STATUS_px + 3*button_offsetX/2;
		int hd_py = g2_offsetY;   
		cp5.addButton("hd")
		.setPosition(hd_px, hd_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("HOLD")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);    

		int fd_px = hd_px;
		int fd_py = hd_py + button_offsetY;   
		cp5.addButton("fd")
		.setPosition(fd_px, fd_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("FWD")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);   

		int bd_px = fd_px;
		int bd_py = fd_py + button_offsetY;   
		cp5.addButton("bd")
		.setPosition(bd_px, bd_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("BWD")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);

		int COORD_px = bd_px;   
		int COORD_py = bd_py + button_offsetY;
		cp5.addButton("COORD")
		.setPosition(COORD_px, COORD_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("COORD")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)
		.moveTo(g1);

		int SPEEDUP_px = COORD_px;
		int SPEEDUP_py = COORD_py + button_offsetY;
		cp5.addButton("SPEEDUP")
		.setPosition(SPEEDUP_px, SPEEDUP_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("+%")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);   

		int SLOWDOWN_px = SPEEDUP_px;
		int SLOWDOWN_py = SPEEDUP_py + button_offsetY;
		cp5.addButton("SLOWDOWN")
		.setPosition(SLOWDOWN_px, SLOWDOWN_py)
		.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
		.setCaptionLabel("-%")
		.setColorBackground(Fields.BUTTON_DEFAULT)
		.setColorCaptionLabel(Fields.BUTTON_TEXT)  
		.moveTo(g1);

		int JOINT_px = SLOWDOWN_px + button_offsetX;
		int JOINT_py = g2_offsetY;
		String[] labels = {" -X\n(J1)", " +X\n(J1)",
				" -Y\n(J2)", " +Y\n(J2)",
				" -Z\n(J3)", " +Z\n(J3)",
				"-XR\n(J4)", "+XR\n(J4)",
				"-YR\n(J5)", "+YR\n(J5)",
				"-ZR\n(J6)", "+ZR\n(J6)"};

		for(int i = 1; i <= 6; i += 1) {
			cp5.addButton("JOINT"+i+"_NEG")
			.setPosition(JOINT_px, JOINT_py)
			.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
			.setCaptionLabel(labels[(i-1)*2])
			.setColorBackground(Fields.BUTTON_DEFAULT)
			.setColorCaptionLabel(Fields.BUTTON_TEXT)  
			.moveTo(g1)
			.getCaptionLabel()
			.alignY(TOP);

			JOINT_px += Fields.LARGE_BUTTON + 1; 
			cp5.addButton("JOINT"+i+"_POS")
			.setPosition(JOINT_px, JOINT_py)
			.setSize(Fields.LARGE_BUTTON, Fields.SMALL_BUTTON)
			.setCaptionLabel(labels[(i-1)*2 + 1])
			.setColorBackground(Fields.BUTTON_DEFAULT)
			.setColorCaptionLabel(Fields.BUTTON_TEXT)  
			.moveTo(g1)
			.getCaptionLabel()
			.alignY(TOP);

			JOINT_px = SLOWDOWN_px + button_offsetX;
			JOINT_py += Fields.SMALL_BUTTON + 1;
		}

		List<Button> buttons = cp5.getAll(Button.class);
		for(Button b : buttons) {
			b.getCaptionLabel().setFont(fnt_conB);
		}
	}// End UI setup

	/* mouse events */

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

	public void mouseWheel(MouseEvent event) {

		if (getManager() != null && getManager().isMouseOverADropdownList()) {
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

	/*Keyboard events*/

	public void keyPressed() {

		if (key == 27) {
			key = 0;
		}

		if (getManager() != null && getManager().isATextFieldActive()) {
			// Disable other key events when typing in a text field
			return;
		}

		if(mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
			// Modify the input name for the new program
			if(key == BACKSPACE && workingText.length() > 0) {

				if(workingText.length() > 1) {
					workingText = workingText.substring(0, workingText.length() - 1);
					setCol_select(min(getCol_select(), workingText.length() - 1));
				}  else {
					workingText = "\0";
				}

				updateScreen();
			} else if(key == DELETE && workingText.length() > 0) {

				if(workingText.length() > 1) {
					workingText = workingText.substring(1, workingText.length());
					setCol_select(min(getCol_select(), workingText.length() - 1));
				}  else {
					workingText = "\0";
				}

				updateScreen();
				// Valid characters in a program name or comment
			} else if(workingText.length() < TEXT_ENTRY_LEN && (key >= 'a' && key <= 'z') || (key >= 'A' && key <= 'Z')
					|| (key >= '0' && key <= '9') || key == '.' || key == '@' || key == '*' || key == '_') {
				StringBuilder temp;
				// Insert the typed character
				if (workingText.length() > 0 && workingText.charAt(getCol_select()) != '\0') {
					temp = new StringBuilder(workingText.substring(0, getCol_select()) + "\0" + workingText.substring(getCol_select(), workingText.length()));
				} else {
					temp = new StringBuilder(workingText); 
				}

				temp.setCharAt(getCol_select(), key);
				workingText = temp.toString();

				// Add an insert element if the length of the current comment is less than 16
				int len = workingText.length();
				if(len <= TEXT_ENTRY_LEN && getCol_select() == workingText.length() - 1 && workingText.charAt(len - 1) != '\0') {
					workingText += '\0';
				}

				setCol_select(min(getCol_select() + 1, workingText.length() - 1));
				// Update contents to the new string
				updateScreen();
			}

			return;
		} else if (key == 'a') {
			// Cycle through Axes display states
			switch (axesState) {
			case NONE:
				axesState = AxesDisplay.AXES;
				break;
			case AXES:
				axesState = AxesDisplay.GRID;
				break;
			default:
				axesState = AxesDisplay.NONE;
			}

		} else if(key == 'e') {
			// Cycle through EE Mapping states
			switch (mappingState) {
			case NONE:
				mappingState = EEMapping.LINE;
				break;
			case LINE:
				mappingState = EEMapping.DOT;
				break;
			default:
				mappingState = EEMapping.NONE;
			}

		} else if (key == 'f' ) {
			// Display the User and Tool frames associated with the current motion instruction
			if (DISPLAY_TEST_OUTPUT && mode == Screen.NAV_PROG_INSTR && (getCol_select() == 3 || getCol_select() == 4)) {
				Instruction inst = activeInstruction();

				if (inst instanceof MotionInstruction) {
					MotionInstruction mInst = (MotionInstruction)inst;
					System.out.printf("\nUser frame: %d\nTool frame: %d\n", mInst.getUserFrame(), mInst.getToolFrame());
				}
			}

		} else if (key == 'm') {
			// Print the current mode to the console
			println(mode.toString());

		} else if (key == 'p') {
			// Toggle the Robot's End Effector state
			if (!isProgramRunning()) {
				armModel.toggleEEState();
			}

		} else if (key == 's') {
			// Save EVERYTHING!
			saveState();

		} else if (key == 't') {
			// Restore default Robot joint angles
			float[] rot = {0, 0, 0, 0, 0, 0};
			armModel.setJointAngles(rot);
			intermediatePositions.clear();

		} else if(key == 'w') {
			// Write anything stored in the String buffer to a text file
			writeBuffer();

		} else if (key == 'y') {
			// Apply another set of default Robot joint angles
			float[] rot = {PI, 0, 0, 0, 0, PI};
			armModel.setJointAngles(rot);
			intermediatePositions.clear();

		}
	}

	/* Button events */

	public void FrontView() {
		// Default view
		camera.reset();
	}

	public void BackView() {
		// Back view
		camera.reset();
		camera.rotate(0, PI, 0);
	}

	public void LeftView() {
		// Left view
		camera.reset();
		camera.rotate(0, PI / 2f, 0);
	}

	public void RightView() {
		// Right view
		camera.reset();
		camera.rotate(0, 3f * PI / 2f, 0);
	}

	public void TopView() {
		// Top view
		camera.reset();
		camera.rotate(3f * PI / 2f, 0, 0);
	}

	public void BottomView() {
		// Bottom view
		camera.reset();
		camera.rotate(PI / 2f, 0, 0);
	}

	public void CreateWldObj() {
		/* Create a world object from the input fields in the Create window. */
		if (activeScenario != null) {
			WorldObject newObject = getManager().createWorldObject();

			if (newObject != null) {
				newObject.setLocalCenter( new PVector(-500f, 0f, 0f) );
				activeScenario.addWorldObject(newObject);
			}
		}
	}

	public void ClearFields() {
		/* Clear all input fields for creating and editing world objects. */
		getManager().clearCreateInputFields();
	}

	public void UpdateWldObj() {
		/* Confirm changes made to the orientation and
		 * position of the selected world object. */
		getManager().editWorldObject();
	}

	public void DeleteWldObj() {
		// Delete focused world object
		int ret = getManager().deleteActiveWorldObject();
		if (DISPLAY_TEST_OUTPUT) { System.out.printf("World Object removed: %d\n", ret); }
	}

	public void NewScenario() {
		Scenario newScenario = getManager().initializeScenario();

		if (newScenario != null) {
			// Add the new scenario
			SCENARIOS.add(newScenario);
		}
	}

	public void SaveScenario() {
		// Save all scenarios
		saveScenarioBytes( new File(sketchPath("tmp/scenarios.bin")) );
	}

	public void SetScenario() {
		// Set the active scenario to a copy of the scenario associated with te scenario dropdown list
		activeScenario = (Scenario)getManager().getActiveScenario().clone();
	}

	public void HideObjects() {
		// Toggle object display on or off
		showOOBs = !showOOBs;
		getManager().updateScenarioWindowContentPositions();
	}

	// Menu button
	public void mu() {
		resetStack();
		nextScreen(Screen.NAV_MAIN_MENU);
	}

	// Select button
	public void se() {
		// Save when exiting a program
		saveProgramBytes( new File(sketchPath("tmp/programs.bin")) ); 

		setActive_prog(0);
		setActive_instr(-1);

		resetStack();
		nextScreen(Screen.NAV_PROGRAMS);
	}

	// Data button
	public void da() {
		resetStack();
		nextScreen(Screen.NAV_DATA);
	}

	public void NUM0() {
		addNumber("0");
	}

	public void NUM1() {
		addNumber("1");
	}

	public void NUM2() {
		addNumber("2");
	}

	public void NUM3() {
		addNumber("3");
	}

	public void NUM4() {
		addNumber("4");
	}

	public void NUM5() {
		addNumber("5");
	}

	public void NUM6() {
		addNumber("6");
	}

	public void NUM7() {
		addNumber("7");
	}

	public void NUM8() {
		addNumber("8");
	}

	public void NUM9() {
		addNumber("9");
	}

	public void addNumber(String number) {
		if(mode.getType() == ScreenType.TYPE_NUM_ENTRY) {
			if (workingText.length() < NUM_ENTRY_LEN) {
				workingText += number;
			}
		}
		else if(mode == Screen.SET_MV_INSTR_SPD) {
			workingText += number;
			options.set(1, workingText + workingTextSuffix);
		} 
		else if(mode.getType() == ScreenType.TYPE_POINT_ENTRY) {
			if(getRow_select() >= 0 && getRow_select() < contents.size()) {
				String value = contents.get(getRow_select()).get(1) + number;

				if(value.length() > 9) {
					// Max length of a an input value
					value = value.substring(0,  9);
				}

				// Concatenate the new digit
				contents.get(getRow_select()).set(1, value);
			}
		}
		else if(mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
			// Replace current entry with a number
			StringBuilder temp = new StringBuilder(workingText);
			temp.setCharAt(getCol_select(), number.charAt(0));
			workingText = temp.toString();
		}

		updateScreen();
	}

	public void RESET() {
		if (shift) {
			// Reset robot fault
			armModel.halt();
			motionFault = false;
		}
	}

	public void PERIOD() {
		if(mode.getType() == ScreenType.TYPE_POINT_ENTRY) {

			if(getRow_select() >= 0 && getRow_select() < contents.size()) {

				// Add decimal point
				String value = contents.get(getRow_select()).get(1) + ".";

				if(value.length() > 9) {
					// Max length of a an input value
					value = value.substring(0,  9);
				}

				contents.get(getRow_select()).set(1, value);
			}
		} else if(mode.getType() == ScreenType.TYPE_NUM_ENTRY) {

			if(workingText.length() < NUM_ENTRY_LEN) {
				workingText += ".";
			}
		} else if(mode != Screen.EDIT_DREG_COM) {
			workingText += ".";
		}

		updateScreen();
	}

	public void LINE() {
		if(mode.getType() == ScreenType.TYPE_POINT_ENTRY) {

			if(getRow_select() >= 0 && getRow_select() < contents.size()) {
				String value = contents.get(getRow_select()).get(1);

				// Mutliply current number by -1
				if(value.length() > 0 && value.charAt(0) == '-') {
					contents.get(getRow_select()).set(1, value.substring(1, value.length()));
				} else {
					contents.get(getRow_select()).set(1, "-" + value);
				}
			}

		} else if(mode.getType() == ScreenType.TYPE_NUM_ENTRY) {

			// Mutliply current number by -1
			if(workingText.length() > 0 && workingText.charAt(0) == '-') {
				workingText = workingText.substring(1);
			} else {
				workingText = "-" + workingText;
			}

		}

		updateScreen();
	}

	public void POSN() {
		if(getSU_macro_bindings()[5] != null && shift) {
			getSU_macro_bindings()[5].execute();
		}
	}

	public void IO() {
		if(getSU_macro_bindings()[6] != null && shift) {
			getSU_macro_bindings()[6].execute();
		}
	}

	/*Arrow keys*/

	public void up() {
		switch(mode) {
		case NAV_PROGRAMS:
			setActive_prog(moveUp(shift));

			if(DISPLAY_TEST_OUTPUT) {
				System.out.printf("\nOpt: %d\nProg: %d\nTRS: %d\n\n",
						opt_select, getActive_prog(), getStart_render());
			}
			break;
		case NAV_PROG_INSTR:
		case SELECT_COMMENT:
		case SELECT_CUT_COPY:
		case SELECT_INSTR_DELETE:
			if (!isProgramRunning()) {
				// Lock movement when a program is running
				Instruction i = activeInstruction();
				int prevLine = getSelectedLine();
				setActive_instr(moveUpInstr(shift));
				int curLine = getSelectedLine();

				//special case for select statement column navigation
				if((i instanceof SelectStatement || i instanceof MotionInstruction) && curLine == 0) {
					if(prevLine == 1) {
						setCol_select(getCol_select() + 3);
					}
				}


				if(DISPLAY_TEST_OUTPUT) {
					System.out.printf("\nRow: %d\nColumn: %d\nInst: %d\nTRS: %d\n\n",
							getRow_select(), getCol_select(), getActive_instr(), getStart_render());
				}
			}
			break;
		case NAV_DREGS:
		case NAV_PREGS_J:
		case NAV_PREGS_C:
		case NAV_TOOL_FRAMES:
		case NAV_USER_FRAMES:
		case NAV_MACROS:
		case NAV_MF_MACROS:
			active_index = moveUp(shift);

			if(DISPLAY_TEST_OUTPUT) {
				System.out.printf("\nRow: %d\nColumn: %d\nIdx: %d\nTRS: %d\n\n",
						getRow_select(), getCol_select(), active_index, getStart_render());
			}
			break;
		case SET_CALL_PROG:
		case SET_MACRO_PROG:
		case DIRECT_ENTRY_TOOL:
		case DIRECT_ENTRY_USER:
		case EDIT_PREG_C:
		case EDIT_PREG_J:
			moveUp(shift);
			break;
		case NAV_MAIN_MENU:
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
		case SET_EXPR_ARG: //<>//
		case SET_BOOL_EXPR_ARG: //<>//
		case SET_EXPR_OP:
		case SET_IO_INSTR_STATE:
			opt_select = max(0, opt_select - 1);
			break;
		case ACTIVE_FRAMES:
			updateActiveFramesDisplay();
			workingText = Integer.toString(armModel.getActiveToolFrame() + 1);
			setRow_select(max(0, getRow_select() - 1));
			break;
		default:
			if (mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
				opt_select = max(0, opt_select - 1); 
				// Reset function key states
				for(int idx = 0; idx < letterStates.length; ++idx) { letterStates[idx] = 0; }
			}
		}

		updateScreen();
	}
	//<>//
	public void dn() {
		switch(mode) {
		case NAV_PROGRAMS:  //<>//
			setActive_prog(moveDown(shift));

			if(DISPLAY_TEST_OUTPUT) {
				System.out.printf("\nRow: %d\nProg: %d\nTRS: %d\n\n",  //<>//
						getRow_select(), getActive_prog(), getStart_render());
			}
			break;
		case NAV_PROG_INSTR:
		case SELECT_COMMENT:
		case SELECT_CUT_COPY:
		case SELECT_INSTR_DELETE:
			if (!isProgramRunning()) {
				// Lock movement when a program is running
				Instruction i = activeInstruction();
				int prevIdx = getSelectedIdx();
				setActive_instr(moveDownInstr(shift)); //<>//
				int curLine = getSelectedLine();

				//special case for select statement column navigation
				if((i instanceof SelectStatement || i instanceof MotionInstruction) && curLine == 1) {
					if(prevIdx >= 3) {
						setCol_select(prevIdx - 3);
					} else {
						setCol_select(0);
					}
				}

				if(DISPLAY_TEST_OUTPUT) {
					System.out.printf("\nRow: %d\nColumn: %d\nInst: %d\nTRS: %d\n\n",
							getRow_select(), getCol_select(), getActive_instr(), getStart_render());
				}
			}
			break;
		case NAV_DREGS:
		case NAV_PREGS_J:
		case NAV_PREGS_C:
		case NAV_TOOL_FRAMES:
		case NAV_USER_FRAMES:
		case NAV_MACROS:
		case NAV_MF_MACROS:
			active_index = moveDown(shift);

			if(DISPLAY_TEST_OUTPUT) {
				System.out.printf("\nRow: %d\nColumn: %d\nIdx: %d\nTRS: %d\n\n",
						getRow_select(), getCol_select(), active_index, getStart_render());
			}
			break;
		case SET_CALL_PROG:
		case DIRECT_ENTRY_TOOL:
		case DIRECT_ENTRY_USER:
		case EDIT_PREG_C:
		case EDIT_PREG_J:
			moveDown(shift);
			break;
		case NAV_MAIN_MENU:
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
			opt_select = min(opt_select + 1, options.size() - 1);
			break;  //<>//
		case ACTIVE_FRAMES:
			updateActiveFramesDisplay();
			workingText = Integer.toString(armModel.getActiveUserFrame() + 1);
			setRow_select(min(getRow_select() + 1, contents.size() - 1));
			break;
		default:
			if (mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
				opt_select = min(opt_select + 1, options.size() - 1);
				// Reset function key states
				for(int idx = 0; idx < letterStates.length; ++idx) { letterStates[idx] = 0; }
			}
		}  

		updateScreen();
	}

	public void lt() {
		switch(mode) { 
		case NAV_PROG_INSTR:
			if (!isProgramRunning()) {
				// Lock movement when a program is running
				moveLeft();
			}
			break;
		case NAV_DREGS:
		case NAV_MACROS:
		case NAV_PREGS_J:
		case NAV_PREGS_C:
			setCol_select(max(0, getCol_select() - 1));
			break;
		default:
			if (mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
				setCol_select(max(0, getCol_select() - 1));
				// Reset function key states //<>//
				for(int idx = 0; idx < letterStates.length; ++idx) { letterStates[idx] = 0; }
			} else if(mode.getType() == ScreenType.TYPE_EXPR_EDIT) {  //<>//
				setCol_select(getCol_select() - ((getCol_select() - 4 >= options.size()) ? 4 : 0));
			}
		}

		updateScreen();
	}


	public void rt() {
		switch(mode) {
		case NAV_PROG_INSTR:
			if (!isProgramRunning()) {
				// Lock movement when a program is running
				moveRight();
			}
			break;
		case DIRECT_ENTRY_USER:
		case DIRECT_ENTRY_TOOL:
		case EDIT_PREG_C:
		case EDIT_PREG_J:
			// Delete a digit from the beginning of the number entry
			if(shift) {
				String entry = contents.get(getRow_select()).get(1);

				if (entry.length() > 1 && !(entry.length() == 2 && entry.charAt(0) == '-')) {

					if(entry.charAt(0) == '-') {
						// Keep negative sign until the last digit is removed
						contents.get(getRow_select()).set(1, "-" + entry.substring(2, entry.length()));
					} else {
						contents.get(getRow_select()).set(1, entry.substring(1, entry.length()));
					}
				} else {
					contents.get(getRow_select()).set(1, "");
				}
			}

			break;
		case NAV_DREGS:
		case NAV_MACROS:
		case NAV_PREGS_J:
		case NAV_PREGS_C:
			setCol_select(min(getCol_select() + 1, contents.get(getRow_select()).size() - 1));
			break;
		default:
			if (mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {

				if(shift) {
					// Delete key function
					if(workingText.length() > 1) {
						workingText = workingText.substring(1, workingText.length());
						setCol_select(min(getCol_select(), workingText.length() - 1));
					}  else {
						workingText = "\0";
					}

					setCol_select(max(0, min(getCol_select(), contents.get(getRow_select()).size() - 1)));
				} else if (mode.getType() == ScreenType.TYPE_EXPR_EDIT) {
					setCol_select(getCol_select() + ((getCol_select() + 4 < options.size()) ? 4 : 0));
				} else {
					// Add an insert element if the length of the current comment is less than 16
					int len = workingText.length();
					if(len <= TEXT_ENTRY_LEN && getCol_select() == workingText.length() - 1 && workingText.charAt(len - 1) != '\0') {
						workingText += '\0';
						// Update contents to the new string
						updateScreen();
					}

					setCol_select(min(getCol_select() + 1, contents.get(getRow_select()).size() - 1));
				}

				// Reset function key states
				for(int idx = 0; idx < letterStates.length; ++idx) { letterStates[idx] = 0; }
			}
		}

		updateScreen();
	}

	//toggle shift on/ off and button highlight
	public void sf() {
		if(!shift) {
			((Button)cp5.get("sf")).setColorBackground(Fields.BUTTON_ACTIVE);
		} else {
			// Stop Robot jog movement when shift is off
			armModel.halt();
			((Button)cp5.get("sf")).setColorBackground(Fields.BUTTON_DEFAULT);
		}

		shift = !shift;
		updateScreen();
	}

	//toggle step on/ off and button highlight
	public void st() {
		if(!isStep()) {
			((Button)cp5.get("st")).setColorBackground(Fields.BUTTON_ACTIVE);
		}
		else {
			((Button)cp5.get("st")).setColorBackground(Fields.BUTTON_DEFAULT);
		}


		setStep(!isStep());
		updateScreen();
	}

	public void pr() {
		lastScreen();
	}

	public void f1() {
		switch(mode) {
		case NAV_PROGRAMS:
			nextScreen(Screen.PROG_CREATE);
			break;
		case NAV_PROG_INSTR:
			if(shift) {
				newMotionInstruction();
				setCol_select(0);

				if(getSelectedLine() == 0) {
					setRow_select(getRow_select() + 1);
					updateScreen();
					if(getSelectedLine() == 0) {
						setActive_instr(getActive_instr() + 1);
					}
				}
			}
			break;
		case NAV_TOOL_FRAMES:
			if(shift) {
				// Reset the highlighted frame in the tool frame list
				armModel.getToolFrame(active_index).reset();
				saveFrameBytes( new File(sketchPath("tmp/frames.bin")) );
				updateScreen();
			} else {
				// Set the current tool frame
				armModel.setActiveToolFrame(active_index);
				updateCoordFrame();
			}
			break;
		case NAV_USER_FRAMES:
			if(shift) {
				// Reset the highlighted frame in the user frames list
				armModel.getUserFrame(active_index).reset();
				saveFrameBytes( new File(sketchPath("tmp/frames.bin")) );
				updateScreen();
			} else {
				// Set the current user frame
				armModel.setActiveUserFrame(active_index);
				updateCoordFrame();
			}
			break;
		case ACTIVE_FRAMES:
			if(getRow_select() == 0) {
				nextScreen(Screen.NAV_TOOL_FRAMES);
			} else if(getRow_select() == 1) {
				nextScreen(Screen.NAV_USER_FRAMES);
			}
			break;
		case NAV_MACROS:
			edit_macro = null;
			nextScreen(Screen.SET_MACRO_PROG);
			break;
		case NAV_DREGS:
			// Clear Data Register entry
			RegisterFile.setDReg(active_index, new DataRegister(active_index));
			saveRegisterBytes( new File(sketchPath("tmp/registers.bin")) );
			break;
		case NAV_PREGS_J:
		case NAV_PREGS_C:
			// Clear Position Register entry
			RegisterFile.setPReg(active_index, new PositionRegister(active_index));
			saveRegisterBytes( new File(sketchPath("tmp/registers.bin")) );
			break;
		default:
			if (mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
				editTextEntry(0);
			}
		}

		updateScreen();
	}

	public void f2() {
		switch(mode) {
		case NAV_PROGRAMS:
			if(armModel.numOfPrograms() > 0) {
				nextScreen(Screen.PROG_RENAME);
			}
			break;
		case NAV_PROG_INSTR:
			nextScreen(Screen.SELECT_INSTR_INSERT);
			break;
		case TFRAME_DETAIL:
			switchScreen(Screen.FRAME_METHOD_TOOL);
			//nextScreen(Screen.TOOL_FRAME_METHODS);
			break;
		case TEACH_3PT_TOOL:
		case TEACH_6PT:
		case DIRECT_ENTRY_TOOL:
			lastScreen();
			break;
		case UFRAME_DETAIL:
			switchScreen(Screen.FRAME_METHOD_USER);
			//nextScreen(Screen.USER_FRAME_METHODS);
			break;
		case TEACH_3PT_USER:
		case TEACH_4PT:
		case DIRECT_ENTRY_USER:
			lastScreen();
			break;
		case NAV_DREGS:
			// Data Register copy menus
			if (getCol_select() == 0) {
				nextScreen(Screen.CP_DREG_COM);
			} else if (getCol_select() == 1) {
				nextScreen(Screen.CP_DREG_VAL);
			}
			break;
		case NAV_PREGS_J:
		case NAV_PREGS_C:
			// Position Register copy menus
			if (getCol_select() == 0) {
				nextScreen(Screen.CP_PREG_COM);
			} else if (getCol_select() == 1) {
				nextScreen(Screen.CP_PREG_PT);
			}
			break;
		default:
			if (mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
				editTextEntry(1);
				updateScreen();
			}
		}
	}

	public void f3() {
		switch(mode){
		case NAV_PROGRAMS:
			if(armModel.numOfPrograms() > 0) {
				nextScreen(Screen.CONFIRM_PROG_DELETE);
			}
			break;
		case NAV_PROG_INSTR:
			int selectIdx = getSelectedIdx();
			if(activeInstruction() instanceof IfStatement) {
				IfStatement stmt = (IfStatement)activeInstruction();

				if(stmt.getExpr() instanceof Expression && selectIdx >= 2) {
					((Expression)stmt.getExpr()).insertElement(selectIdx - 3);
					updateScreen();
					rt();
				}
			} 
			else if(activeInstruction() instanceof SelectStatement) {
				SelectStatement stmt = (SelectStatement)activeInstruction();

				if(selectIdx >= 3) {
					stmt.addCase();
					updateScreen();
					dn();
				}
			}
			else if(activeInstruction() instanceof RegisterStatement) {
				RegisterStatement stmt = (RegisterStatement)activeInstruction();
				int rLen = (stmt.getPosIdx() == -1) ? 2 : 3;

				if(selectIdx > rLen) {
					stmt.getExpr().insertElement(selectIdx - (rLen + 2));
					updateScreen();
					rt();
				}
			}

			updateScreen();
			break;
		case SELECT_CUT_COPY:
			ArrayList<Instruction> inst = activeProgram().getInstructions();
			clipBoard = new ArrayList<Instruction>();

			int remIdx = 0;
			for(int i = 0; i < selectedLines.length; i += 1){
				if(selectedLines[i]){
					clipBoard.add(inst.get(remIdx));
					inst.remove(remIdx);
				} else{
					remIdx += 1;
				}
			}

			updateInstructions();
			break;
		case NAV_TOOL_FRAMES:
			active_index = 0;
			switchScreen(Screen.NAV_USER_FRAMES);
			break;
		case NAV_USER_FRAMES:
			active_index = 0;
			switchScreen(Screen.NAV_TOOL_FRAMES);
			break;
		case NAV_DREGS:
			// Switch to Position Registers
			nextScreen(Screen.NAV_PREGS_C);
			break;
		case NAV_PREGS_J:
		case NAV_PREGS_C:
			if (shift) {
				switchScreen(Screen.SWAP_PT_TYPE);
			} else {
				// Switch to Data Registers
				nextScreen(Screen.NAV_DREGS);
			}
			break;
		default:
			if (mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
				editTextEntry(2);
				updateScreen();
			}
		}
	}


	public void f4() {
		Program p = activeProgram();

		switch(mode) {
		case NAV_PROGRAMS:
			if(armModel.numOfPrograms() > 0) {
				nextScreen(Screen.PROG_COPY);
			}
			break;
		case NAV_PROG_INSTR:
			Instruction ins = activeInstruction();

			if (ins != null) {
				int selectIdx = getSelectedIdx();
				getInstrEdit(ins, selectIdx);
			}
			break;
		case CONFIRM_INSERT:
			try {
				int lines_to_insert = Integer.parseInt(workingText);
				for(int i = 0; i < lines_to_insert; i += 1)
					p.getInstructions().add(getActive_instr(), new Instruction());

				updateInstructions();
			}
			catch(Exception e){
				e.printStackTrace();
			}

			lastScreen();
			break;
		case CONFIRM_PROG_DELETE:
			int progIdx = getActive_prog();

			if(progIdx >= 0 && progIdx < armModel.numOfPrograms()) {
				armModel.removeProgram(progIdx);

				if(getActive_prog() >= armModel.numOfPrograms()) {
					setActive_prog(armModel.numOfPrograms() - 1);

					setRow_select(min(getActive_prog(), ITEMS_TO_SHOW - 1));
					setStart_render(getActive_prog() - getRow_select());
				}

				lastScreen();
				saveProgramBytes( new File(sketchPath("tmp/programs.bin")) );
			}
			break;
		case SELECT_INSTR_DELETE:
			ArrayList<Instruction> inst = p.getInstructions();

			int remIdx = 0;
			for(int i = 0; i < selectedLines.length; i += 1){
				if(selectedLines[i]){
					inst.remove(remIdx);
				} else{
					remIdx += 1;
				}
			}

			display_stack.pop();
			updateInstructions();
			break;
		case SELECT_CUT_COPY:
			inst = p.getInstructions();
			clipBoard = new ArrayList<Instruction>();

			for(int i = 0; i < selectedLines.length; i += 1){
				if(selectedLines[i])
					clipBoard.add(inst.get(i).clone());
			}

			display_stack.pop();
			updateInstructions();
			break;
		case FIND_REPL:
			int lineIdx = 0;
			String s;

			for(Instruction instruct: p.getInstructions()){
				s = (lineIdx + 1) + ") " + instruct.toString();

				if(s.toUpperCase().contains(workingText.toUpperCase())){
					break;
				}

				lineIdx += 1;
			}

			display_stack.pop();
			setActive_instr(lineIdx);
			updateInstructions();
			break;
		case SELECT_COMMENT:
			display_stack.pop();
			updateInstructions();
			break;
		case CONFIRM_RENUM:
			Point[] pTemp = new Point[1000];
			int posIdx = 0;

			//make a copy of the current positions in p
			for(int i = 0; i < 1000; i += 1){
				pTemp[i] = p.getPosition(i);
			}

			p.clearPositions();

			//rearrange positions
			for(int i = 0; i < p.getInstructions().size(); i += 1) {
				Instruction instr = p.getInstruction(i);
				if(instr instanceof MotionInstruction) {
					int instructPos = ((MotionInstruction)instr).getPositionNum();
					p.setPosition(posIdx, pTemp[instructPos]);
					((MotionInstruction)instr).setPositionNum(posIdx);
					posIdx += 1;
				}
			}

			display_stack.pop();
			updateInstructions();
			break;
		case NAV_MACROS:
			edit_macro = macros.get(getRow_select());

			if(getCol_select() == 1) {
				nextScreen(Screen.SET_MACRO_PROG);
			} else if(getCol_select() == 2) {
				nextScreen(Screen.SET_MACRO_TYPE);
			} else {
				if(!macros.get(getRow_select()).isManual())
					nextScreen(Screen.SET_MACRO_BINDING);
			}
			break;
		case NAV_PREGS_J:
		case NAV_PREGS_C:
			if (shift && !isProgramRunning()) {
				// Stop any prior jogging motion
				armModel.halt();

				// Move To function
				Point pt = (RegisterFile.getPReg(active_index)).point.clone();

				if (pt != null) {
					// Move the Robot to the select point
					if (mode == Screen.NAV_PREGS_C) {
						Frame active = armModel.getActiveFrame(CoordFrame.USER);

						if (active != null) {
							pt = removeFrame(pt, active.getOrigin(), active.getOrientation());
							if (DISPLAY_TEST_OUTPUT) {
								System.out.printf("pt: %s\n", pt.position.toString());
							}
						}

						armModel.moveTo(pt.position, pt.orientation);
					} else {
						armModel.moveTo(pt.angles);
					}
				} else {
					println("Position register is uninitialized!");
				}
			}

			break;
		default:
			if (mode.getType() == ScreenType.TYPE_TEACH_POINTS) {

				if (shift && teachFrame != null) {
					Point tgt = teachFrame.getPoint(opt_select);

					if (mode == Screen.TEACH_3PT_USER || mode == Screen.TEACH_4PT) {
						if (tgt != null && tgt.position != null && tgt.orientation != null) {
							// Move to the point's position and orientation
							armModel.moveTo(tgt.position, tgt.orientation);
						}
					}
					else {
						if (tgt != null && tgt.angles != null) {
							// Move to the point's joint angles
							armModel.moveTo(tgt.angles);
						}
					}
				}
			}
			else if (mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
				editTextEntry(3);
			}

		}

		updateScreen();
	}

	public void f5() {
		switch(mode) {
		case NAV_PROG_INSTR:
			Instruction i = activeInstruction();
			int selectLine = getSelectedLine();
			int selectIdx = getSelectedIdx();

			if(selectIdx == 0) {
				nextScreen(Screen.NAV_INSTR_MENU);
			}
			else if(i instanceof MotionInstruction) {
				if(selectIdx == 3 || (getCol_select() == 0 && selectLine == 1)) {
					nextScreen(Screen.VIEW_INST_REG); 
				}
			}
			else if(i instanceof IfStatement) {
				IfStatement stmt = (IfStatement)i;
				if(stmt.getExpr() instanceof Expression) {
					((Expression)stmt.getExpr()).removeElement(selectIdx - 3);
				}
			}
			else if(i instanceof SelectStatement) {
				SelectStatement stmt = (SelectStatement)i;
				if(selectIdx >= 3) {
					stmt.deleteCase((selectIdx - 3)/3);
				}
			}
			else if(i instanceof RegisterStatement) {
				RegisterStatement stmt = (RegisterStatement)i;
				int rLen = (stmt.getPosIdx() == -1) ? 2 : 3;
				if(selectIdx > (rLen + 1) && selectIdx < stmt.getExpr().getLength() + rLen) {
					stmt.getExpr().removeElement(selectIdx - (rLen + 2));
				}
			}
			break;
		case VIEW_INST_REG:
			MotionInstruction m = (MotionInstruction)activeInstruction();

			if(getSelectedIdx() == 3) {
				m.toggleOffsetActive();
			} else {
				m.getSecondaryPoint().toggleOffsetActive();
			}

			switchScreen(Screen.SET_MV_INSTR_OFFSET);
			break;      
		case TEACH_3PT_USER:
		case TEACH_3PT_TOOL:
		case TEACH_4PT:
		case TEACH_6PT:
			if (shift) {
				// Save the Robot's current position and joint angles
				Point pt;

				if (mode == Screen.TEACH_3PT_USER || mode == Screen.TEACH_4PT) {
					pt = nativeRobotEEPoint(armModel.getJointAngles());
				} else {
					pt = nativeRobotPoint(armModel.getJointAngles());
				}

				teachFrame.setPoint(pt, opt_select);
				saveFrameBytes( new File(sketchPath("tmp/frames.bin")) );
				updateScreen();
			}
			break;
		case CONFIRM_PROG_DELETE:
			opt_select = 0;

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
		case NAV_PREGS_J:
		case NAV_PREGS_C:

			if (shift && active_index >= 0 && active_index < RegisterFile.REG_SIZE) {
				// Save the Robot's current position and joint angles
				Point curRP = nativeRobotEEPoint(armModel.getJointAngles());
				Frame active = armModel.getActiveFrame(CoordFrame.USER);

				if (active != null) {
					// Save Cartesian values in terms of the active User frame
					curRP = applyFrame(curRP, active.getOrigin(), active.getOrientation());
				} 

				(RegisterFile.getPReg(active_index)).point = curRP;
				(RegisterFile.getPReg(active_index)).isCartesian = (mode == Screen.NAV_PREGS_C);
				saveRegisterBytes( new File(sketchPath("tmp/registers.bin")) );
			}
			break;
		default:
			if (mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
				editTextEntry(4);
			}
		}

		updateScreen();
	}

	public void editTextEntry(int fIdx) {
		char newChar = letters[fIdx][letterStates[fIdx]];
		if(opt_select == 0 && !(fIdx == 4 && letterStates[fIdx] > 1)) {
			// Use uppercase character
			newChar = (char)(newChar - 32);
		}

		StringBuilder temp = new StringBuilder(workingText);
		temp.setCharAt(getCol_select(), newChar);
		workingText = temp.toString();

		// Update current letter state
		letterStates[fIdx] = (letterStates[fIdx] + 1) % 6;
		for(int idx = 0; idx < letterStates.length; idx += 1) {
			// Reset all other letter states
			if (idx != fIdx) {
				letterStates[idx] = 0;
			}
		}
	}

	/* Stops all of the Robot's movement */
	public void hd() {
		armModel.halt();
	}

	public void fd() {  
		if(mode == Screen.NAV_PROG_INSTR && !isProgramRunning() && shift) {
			// Stop any prior Robot movement
			armModel.halt();
			// Safeguard against editing a program while it is running
			setCol_select(0);

			setExecutingInstruction(false);
			// Run single instruction when step is set
			execSingleInst = isStep();

			setProgramRunning(true);
		}
	}

	public void bd() {
		// If there is a previous instruction, then move to it and reverse its affects
		if(mode == Screen.NAV_PROG_INSTR && !isProgramRunning() && shift && isStep()) {
			// Stop any prior Robot movement
			armModel.halt();
			// Safeguard against editing a program while it is running
			setCol_select(0);
			// TODO fix backwards
		}
	}

	public void ENTER() {
		Program p = activeProgram();

		switch(mode) {
		//Main menu
		case NAV_MAIN_MENU:
			if(opt_select == 0) { // Frames
				nextScreen(Screen.SELECT_FRAME_MODE);
			} else if(opt_select == 1) { // Macros
				nextScreen(Screen.NAV_MACROS);
			} else { // Manual Functions
				nextScreen(Screen.NAV_MF_MACROS);
			}
			break; //<>//
			//Frame nav and edit
		case SELECT_FRAME_MODE:
			if(opt_select == 0) {
				nextScreen(Screen.NAV_TOOL_FRAMES);
			}
			else if(opt_select == 1) {
				nextScreen(Screen.NAV_USER_FRAMES);
			}
			break;
		case ACTIVE_FRAMES:
			updateActiveFramesDisplay();
			break;
		case NAV_TOOL_FRAMES:
			curFrameIdx = contents.get(getRow_select()).getItemIdx();
			nextScreen(Screen.TFRAME_DETAIL);
			break;
		case NAV_USER_FRAMES:
			curFrameIdx = contents.get(getRow_select()).getItemIdx();
			nextScreen(Screen.UFRAME_DETAIL);
			break;
		case FRAME_METHOD_USER:
			// User Frame teaching methods
			teachFrame = armModel.getUserFrame(curFrameIdx);
			if(opt_select == 0) {
				nextScreen(Screen.TEACH_3PT_USER);
			} 
			else if(opt_select == 1) {
				nextScreen(Screen.TEACH_4PT);
			} 
			else if(opt_select == 2) {
				nextScreen(Screen.DIRECT_ENTRY_USER);
			}
			break;
		case FRAME_METHOD_TOOL:
			teachFrame = armModel.getToolFrame(curFrameIdx);
			// Tool Frame teaching methods
			if(opt_select == 0) {
				nextScreen(Screen.TEACH_3PT_TOOL);
			} 
			else if(opt_select == 1) {
				nextScreen(Screen.TEACH_6PT);
			} 
			else if(opt_select == 2) {
				nextScreen(Screen.DIRECT_ENTRY_TOOL);
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
				for(int val = 0; val < inputs.length; ++val) {
					String str = contents.get(val).get(1);

					if(str.length() < 0) {
						// No value entered
						updateScreen();
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
				nextScreen(Screen.UFRAME_DETAIL);
			} else {
				nextScreen(Screen.TFRAME_DETAIL);
			}
			break;  

			//Program nav and edit
		case PROG_CREATE:
			if(!workingText.equals("\0")) {
				if (workingText.charAt(workingText.length() - 1) == '\0') {
					// Remove insert character
					workingText = workingText.substring(0, workingText.length() - 1);
				}

				int new_prog = armModel.addProgram(new Program(workingText));
				setActive_prog(new_prog);
				setActive_instr(0);
				setRow_select(0);
				setCol_select(0);
				setStart_render(0);

				saveProgramBytes( new File(sketchPath("tmp/programs.bin")) );    
				switchScreen(Screen.NAV_PROG_INSTR);
			}
			break;
		case PROG_RENAME:
			if(!workingText.equals("\0")) {
				if (workingText.charAt(workingText.length() - 1) == '\0') {
					// Remove insert character
					workingText = workingText.substring(0, workingText.length() - 1);
				}
				// Renmae the program
				activeProgram().setName(workingText);
				setActive_instr(0);
				setRow_select(0);
				setCol_select(0);
				setStart_render(0);

				saveProgramBytes( new File(sketchPath("tmp/programs.bin")) );
				resetStack();
				nextScreen(Screen.NAV_PROGRAMS);
			}
			break;
		case PROG_COPY:
			if(!workingText.equals("\0")) {
				if (workingText.charAt(workingText.length() - 1) == '\0') {
					// Remove insert character
					workingText = workingText.substring(0, workingText.length() - 1);
				}

				Program newProg = activeProgram().clone();
				newProg.setName(workingText);
				int new_prog = armModel.addProgram(newProg);
				setActive_prog(new_prog);
				setActive_instr(0);
				setRow_select(0);
				setCol_select(0);
				setStart_render(0);

				saveProgramBytes( new File(sketchPath("tmp/programs.bin")) );    
				resetStack();
				nextScreen(Screen.NAV_PROGRAMS);
			}
			break;
		case NAV_PROGRAMS:
			if(armModel.numOfPrograms() != 0) {
				setActive_instr(0);
				setRow_select(0);
				setCol_select(0);
				setStart_render(0);
				nextScreen(Screen.NAV_PROG_INSTR);
			}
			break;

			//Instruction options menu
		case NAV_INSTR_MENU:
			MotionInstruction m;

			switch(opt_select) {
			case 0: //Insert
				nextScreen(Screen.CONFIRM_INSERT);
				break;
			case 1: //Delete
				selectedLines = resetSelection(p.getInstructions().size());
				nextScreen(Screen.SELECT_INSTR_DELETE);
				break;
			case 2: //Cut/Copy
				selectedLines = resetSelection(p.getInstructions().size());
				nextScreen(Screen.SELECT_CUT_COPY);
				break;
			case 3: //Paste
				nextScreen(Screen.SELECT_PASTE_OPT);          
				break;
			case 4: //Find/Replace
				nextScreen(Screen.FIND_REPL);
				break;
			case 5: //Renumber
				nextScreen(Screen.CONFIRM_RENUM);
				break;
			case 6: //Comment
				selectedLines = resetSelection(p.getInstructions().size());
				nextScreen(Screen.SELECT_COMMENT);
				break;
			case 7: //Undo
			case 8: //Remark
			}

			break;

			//Instruction insert menus
		case SELECT_INSTR_INSERT:
			switch(opt_select){
			case 0: // I/O
				nextScreen(Screen.SELECT_IO_INSTR_REG);
				break;
			case 1: // Offset/Frames
				nextScreen(Screen.SELECT_FRAME_INSTR_TYPE);
				break;
			case 2: //Register 
				nextScreen(Screen.SELECT_REG_STMT);
				break;
			case 3: //IF/ SELECT
				nextScreen(Screen.SELECT_COND_STMT);
				break;
			case 4: //JMP/ LBL
				nextScreen(Screen.SELECT_JMP_LBL);
				break;
			case 5: //Call
				newCallInstruction();
				switchScreen(Screen.SET_CALL_PROG);
				break;
			}

			break;
		case SELECT_IO_INSTR_REG:
			newIOInstruction();
			display_stack.pop();
			lastScreen();
			break;
		case SELECT_FRAME_INSTR_TYPE:
			if(opt_select == 0){
				newFrameInstruction(FTYPE_TOOL);
			} else {
				newFrameInstruction(FTYPE_USER);
			}

			display_stack.pop();
			switchScreen(Screen.SET_FRAME_INSTR_IDX);
			break;
		case SELECT_REG_STMT:
			display_stack.pop();
			display_stack.pop();

			if(opt_select == 0) {
				newRegisterStatement(new DataRegister());
			} else if(opt_select == 1){
				newRegisterStatement(new IORegister());
			} else if(opt_select == 2){
				newRegisterStatement(new PositionRegister());
			} else {
				newRegisterStatement(new PositionRegister(), 0);
				display_stack.push(Screen.SET_REG_EXPR_IDX2);
			}

			nextScreen(Screen.SET_REG_EXPR_IDX1);
			break;
		case SELECT_COND_STMT:
			if(opt_select == 0) {
				newIfStatement();
				display_stack.pop();
				switchScreen(Screen.SET_EXPR_OP);
			} else if(opt_select == 1) {
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

			if(opt_select == 0) {
				newLabel();
				switchScreen(Screen.SET_LBL_NUM);
			} else {
				newJumpInstruction();
				switchScreen(Screen.SET_JUMP_TGT);
			}

			break;

			//Movement instruction edit
		case SET_MV_INSTR_TYPE:
			m = activeMotionInst();
			if(opt_select == 0) {
				if(m.getMotionType() != MTYPE_JOINT) m.setSpeed(m.getSpeed()/armModel.motorSpeed);
				m.setMotionType(MTYPE_JOINT);
			} else if(opt_select == 1) {
				if(m.getMotionType() == MTYPE_JOINT) m.setSpeed(armModel.motorSpeed*m.getSpeed());
				m.setMotionType(MTYPE_LINEAR);
			} else if(opt_select == 2) {
				if(m.getMotionType() == MTYPE_JOINT) m.setSpeed(armModel.motorSpeed*m.getSpeed());
				m.setMotionType(MTYPE_CIRCULAR);
			}

			lastScreen();
			break;
		case SET_MV_INSTR_REG_TYPE:
			int line = getSelectedLine();
			m = line == 0 ? activeMotionInst() : activeMotionInst().getSecondaryPoint();

			if(opt_select == 0) {
				m.setGlobalPosRegUse(false);

			} else if(opt_select == 1) {
				m.setGlobalPosRegUse(true);
			}

			lastScreen();
			break;
		case SET_MV_INSTR_SPD:
			line = getSelectedLine();
			m = line == 0 ? activeMotionInst() : activeMotionInst().getSecondaryPoint();

			float tempSpeed = Float.parseFloat(workingText);
			if(tempSpeed >= 5.0f) {
				if(speedInPercentage) {
					if(tempSpeed > 100) tempSpeed = 10; 
					tempSpeed /= 100.0f;
				} else if(tempSpeed > armModel.motorSpeed) {
					tempSpeed = armModel.motorSpeed;
				}

				m.setSpeed(tempSpeed);
				saveProgramBytes( new File(sketchPath("tmp/programs.bin")) );
			}

			lastScreen();
			break;
		case SET_MV_INSTR_IDX:
			try {
				int tempRegister = Integer.parseInt(workingText);
				int lbound = 1, ubound;

				if (activeMotionInst().usesGPosReg()) {
					ubound = 100;

				} else {
					ubound = 1000;
				}

				line = getSelectedLine();
				m = line == 0 ? activeMotionInst() : activeMotionInst().getSecondaryPoint();

				if(tempRegister < lbound || tempRegister > ubound) {
					// Invalid register index
					err = String.format("Only registers %d-%d are valid!", lbound, ubound);
					lastScreen();
					return;
				}

				m.setPositionNum(tempRegister - 1);
			} catch (NumberFormatException NFEx) { /* Ignore invalid numbers */ }

			lastScreen();
			break;
		case SET_MV_INSTR_TERM:
			try {
				int tempTerm = Integer.parseInt(workingText);
				line = getSelectedLine();
				m = line == 0 ? activeMotionInst() : activeMotionInst().getSecondaryPoint();

				if(tempTerm >= 0 && tempTerm <= 100) {
					m.setTermination(tempTerm);
				}
			} catch (NumberFormatException NFEx) { /* Ignore invalid input */ }

			lastScreen();
			break;
		case SET_MV_INSTR_OFFSET:
			try {
				int tempRegister = Integer.parseInt(workingText) - 1;
				line = getSelectedLine();
				m = line == 0 ? activeMotionInst() : activeMotionInst().getSecondaryPoint();

				if(tempRegister < 1 || tempRegister > 1000) {
					// Invalid register index
					err = "Only registers 1 - 1000 are legal!";
					lastScreen();
					return;
				} else if((RegisterFile.getPReg(tempRegister)).point == null) {
					// Invalid register index
					err = "This register is uninitailized!";
					lastScreen();
					return;
				}

				m.setOffset(tempRegister);
			} catch (NumberFormatException NFEx) { /* Ignore invalid numbers */ }

			lastScreen();
			break;

			//Expression edit
		case SET_EXPR_ARG:
			Expression expr = (Expression)opEdit;

			if(opt_select == 0) {
				//set arg to new data reg
				ExprOperand operand = new ExprOperand(new DataRegister(), -1);
				opEdit = expr.setOperand(editIdx, operand);
				switchScreen(Screen.INPUT_DREG_IDX);
			} else if(opt_select == 1) {
				//set arg to new io reg
				ExprOperand operand = new ExprOperand(new IORegister(), -1);
				opEdit = expr.setOperand(editIdx, operand);
				switchScreen(Screen.INPUT_IOREG_IDX);
			} else if(opt_select == 2) {
				ExprOperand operand = new ExprOperand(new PositionRegister(), -1);
				opEdit = expr.setOperand(editIdx, operand);
				switchScreen(Screen.INPUT_PREG_IDX1);
			} else if(opt_select == 3) {
				ExprOperand operand = new ExprOperand(new PositionRegister(), -1, 0);
				opEdit = expr.setOperand(editIdx, operand);
				display_stack.pop();
				display_stack.push(Screen.INPUT_PREG_IDX2);
				nextScreen(Screen.INPUT_PREG_IDX1);
			} else if(opt_select == 4) {
				//set arg to new expression
				Expression oper = new Expression();
				expr.setOperand(editIdx, oper);
				lastScreen();
			} else {
				//set arg to new constant
				opEdit = expr.getOperand(editIdx).reset();
				switchScreen(Screen.INPUT_CONST);
			}

			break;
		case SET_BOOL_EXPR_ARG:
			if(opt_select == 0) {
				//set arg to new data reg
				opEdit.set(new DataRegister(), -1);
				switchScreen(Screen.INPUT_DREG_IDX);
			} else if(opt_select == 1) {
				//set arg to new io reg
				opEdit.set(new IORegister(), -1);
				switchScreen(Screen.INPUT_IOREG_IDX);
			} else {
				//set arg to new constant
				opEdit.reset();
				switchScreen(Screen.INPUT_CONST);
			}
			break;
		case SET_IF_STMT_ACT:
			IfStatement stmt = (IfStatement)activeInstruction();
			if(opt_select == 0) {
				stmt.setInstr(new JumpInstruction());
				switchScreen(Screen.SET_JUMP_TGT);
			} else {
				stmt.setInstr(new CallInstruction());
				switchScreen(Screen.SET_CALL_PROG);
			}

			break;
		case SET_EXPR_OP:
			if(opEdit instanceof Expression) {
				expr = (Expression)opEdit;

				switch(opt_select) {
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
			}
			else if(opEdit instanceof BooleanExpression) {
				BooleanExpression boolExpr = (BooleanExpression)opEdit;

				switch(opt_select) {
				case 0:
					boolExpr.setOperator(Operator.EQUAL);
					break;
				case 1:
					boolExpr.setOperator(Operator.NEQUAL);
					break;
				case 2:
					boolExpr.setOperator(Operator.GRTR);
					break;
				case 3:
					boolExpr.setOperator(Operator.LESS);
					break;
				case 4:
					boolExpr.setOperator(Operator.GREQ);
					break;
				case 5:
					boolExpr.setOperator(Operator.LSEQ);
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
				int idx = Integer.parseInt(workingText);

				if(mode == Screen.INPUT_DREG_IDX) {
					opEdit.set(RegisterFile.getDReg(idx - 1), idx);
				} else if(mode == Screen.INPUT_IOREG_IDX) {
					opEdit.set(RegisterFile.getIOReg(idx - 1), idx);
				} else if(mode == Screen.INPUT_PREG_IDX1) {
					opEdit.set(RegisterFile.getPReg(idx - 1), idx);
				} else if(mode == Screen.INPUT_PREG_IDX2) {
					int reg = opEdit.getRegIdx();
					opEdit.set(RegisterFile.getPReg(idx - 1), reg, idx);
				}

			} catch(NumberFormatException e) {}

			lastScreen();
			break;
		case INPUT_CONST:
			try{
				float data = Float.parseFloat(workingText);
				opEdit.set(data);
			} catch(NumberFormatException e) {}

			lastScreen();
			break;
		case SET_BOOL_CONST:
			if(opt_select == 0) {
				opEdit.set(true);
			} else {
				opEdit.set(false);
			}

			lastScreen();
			break;

			//Select statement edit
		case SET_SELECT_STMT_ACT:
			SelectStatement s = (SelectStatement)activeInstruction();
			int i = (getSelectedIdx() - 3) / 3;

			if(opt_select == 0) {
				s.getInstrs().set(i, new JumpInstruction());
			} else {
				s.getInstrs().set(i, new CallInstruction());
			}

			lastScreen();
			break;
		case SET_SELECT_STMT_ARG:
			if(opt_select == 0) {
				opEdit.set(new DataRegister(), -1);
			} else {
				opEdit.reset();
			}

			nextScreen(Screen.SET_SELECT_ARGVAL);
			break;
		case SET_SELECT_ARGVAL:
			try {
				s = (SelectStatement)activeInstruction();
				float f = Float.parseFloat(workingText);

				if(opEdit.type == ExpressionElement.UNINIT) {
					opEdit.set(f);
				} else if(opEdit.type == ExpressionElement.DREG) {
					//println(regFile.DAT_REG[(int)f - 1].value);
					opEdit.set(RegisterFile.getDReg((int)f - 1), (int)f);
				}
			} catch(NumberFormatException ex) {}

			display_stack.pop();
			lastScreen();
			break;

			//IO instruction edit
		case SET_IO_INSTR_STATE:
			IOInstruction ioInst = (IOInstruction)activeInstruction();

			if(opt_select == 0) {
				ioInst.setState(Fields.ON);
			} else {
				ioInst.setState(Fields.OFF);
			}

			lastScreen();
			break;
		case SET_IO_INSTR_IDX:
			try {
				int tempReg = Integer.parseInt(workingText);

				if(tempReg >= 0 && tempReg < 6){
					ioInst = (IOInstruction)activeInstruction();
					ioInst.setReg(tempReg);
				}
			}
			catch (NumberFormatException NFEx){ /* Ignore invalid input */ }

			lastScreen();
			break;

			//Frame instruction edit
		case SET_FRM_INSTR_TYPE:
			FrameInstruction fInst = (FrameInstruction)activeInstruction();

			if(opt_select == 0)
				fInst.setFrameType(FTYPE_TOOL);
			else
				fInst.setFrameType(FTYPE_USER);

			lastScreen();
			break;      
		case SET_FRAME_INSTR_IDX:
			try {
				int frameIdx = Integer.parseInt(workingText) - 1;

				if(frameIdx >= -1 && frameIdx < Fields.FRAME_SIZE){
					fInst = (FrameInstruction)activeInstruction();
					fInst.setReg(frameIdx);
				}
			}
			catch (NumberFormatException NFEx){ /* Ignore invalid input */ }

			lastScreen();
			break;

			//Register statement edit
		case SET_REG_EXPR_TYPE:
			RegisterStatement regStmt = (RegisterStatement)activeInstruction();
			display_stack.pop();

			if(opt_select == 0) {
				regStmt.setRegister(new DataRegister());
			} else if(opt_select == 1) {
				regStmt.setRegister(new IORegister());
			} else if(opt_select == 2) {
				regStmt.setRegister(new PositionRegister());
			} else {
				regStmt.setRegister(new PositionRegister(), 0);
				display_stack.push(Screen.SET_REG_EXPR_IDX2);
			}

			nextScreen(Screen.SET_REG_EXPR_IDX1);
			break;
		case SET_REG_EXPR_IDX1:
			try {
				int idx = Integer.parseInt(workingText);

				if (idx < 1 || idx > 1000) {
					println("Invalid register index!");
				} else {
					regStmt = (RegisterStatement)activeInstruction(); 
					if(regStmt.getReg() instanceof DataRegister) {
						(regStmt).setRegister(RegisterFile.getDReg(idx - 1));
					} else if(regStmt.getReg() instanceof IORegister) {
						(regStmt).setRegister(RegisterFile.getIOReg(idx - 1));
					} else if(regStmt.getReg() instanceof PositionRegister && regStmt.getPosIdx() == -1) { 
						(regStmt).setRegister(RegisterFile.getPReg(idx - 1));
					} else {
						(regStmt).setRegister(RegisterFile.getPReg(idx - 1), 0);
					}
				}
			}
			catch (NumberFormatException NFEx){ /* Ignore invalid input */ }

			lastScreen();
			break;
		case SET_REG_EXPR_IDX2:
			try {
				int idx = Integer.parseInt(workingText);

				if (idx < 1 || idx > 6) {
					println("Invalid position index!"); 
				} else {
					regStmt = (RegisterStatement)activeInstruction();
					if(regStmt.getReg() instanceof PositionRegister) {
						regStmt.setPosIdx(idx);
					}
				}
			} catch (NumberFormatException NFEx){ /* Ignore invalid input */ }

			lastScreen();
			break;

			//Jump/ Label instruction edit
		case SET_LBL_NUM:
			try {
				int idx = Integer.parseInt(workingText);

				if (idx < 0 || idx > 99) {
					println("Invalid label index!");
				} else {
					((LabelInstruction)activeInstruction()).setLabelNum(idx);
				}
			}
			catch (NumberFormatException NFEx){ /* Ignore invalid input */ }

			lastScreen();
			break;
		case SET_JUMP_TGT:
			try {
				int lblNum = Integer.parseInt(workingText);
				int lblIdx = p.findLabelIdx(lblNum);

				if(activeInstruction() instanceof IfStatement) {
					IfStatement ifStmt = (IfStatement)activeInstruction();
					((JumpInstruction)ifStmt.getInstr()).setTgtLblNum(lblNum);
				} 
				else if(activeInstruction() instanceof SelectStatement) {
					SelectStatement sStmt = (SelectStatement)activeInstruction();
					((JumpInstruction)sStmt.getInstrs().get(editIdx)).setTgtLblNum(lblNum);
				}
				else {
					if(lblIdx != -1) {
						JumpInstruction jmp = (JumpInstruction)activeInstruction();
						jmp.setTgtLblNum(lblNum);
					} else {
						err = "Invalid label number.";
					}
				}
			}
			catch (NumberFormatException NFEx){ /* Ignore invalid input */ }

			lastScreen();
			break;

			//Call instruction edit
		case SET_CALL_PROG:
			if(activeInstruction() instanceof IfStatement) {
				IfStatement ifStmt = (IfStatement)activeInstruction();
				((CallInstruction)ifStmt.getInstr()).setProgIdx(opt_select);
			}
			else if(activeInstruction() instanceof SelectStatement) {
				SelectStatement sStmt = (SelectStatement)activeInstruction();
				CallInstruction c = (CallInstruction)sStmt.getInstrs().get(editIdx);
				c.setProgIdx(getRow_select());
			}
			else {
				CallInstruction call = (CallInstruction)activeInstruction();
				call.setProgIdx(getRow_select());
			}

			lastScreen();
			break;

			//Macro edit screens
		case SET_MACRO_PROG:
			if(edit_macro == null) {
				edit_macro = new Macro(armModel.getProgram(getRow_select()), getRow_select());
				macros.add(edit_macro);
				switchScreen(Screen.SET_MACRO_TYPE);
			} else {
				edit_macro.setProgram(armModel.getProgram(getRow_select()), getRow_select());
			}
			break;
		case SET_MACRO_TYPE:
			if(opt_select == 0) {
				edit_macro.setManual(false);
				switchScreen(Screen.SET_MACRO_BINDING);
			} else if(opt_select == 1) {
				edit_macro.setManual(true);
				edit_macro.clearNum();
				lastScreen();
			}
			break;
		case SET_MACRO_BINDING:
			edit_macro.setNum(opt_select);
			lastScreen();
			break;

		case NAV_MF_MACROS:
			int macro_idx = contents.get(active_index).getItemIdx();
			macros.get(macro_idx).execute();
			break;

			//Program instruction editing and navigation
		case SELECT_CUT_COPY:
		case SELECT_INSTR_DELETE:
			selectedLines[getActive_instr()] = !selectedLines[getActive_instr()];
			updateScreen();
			break;
		case SELECT_PASTE_OPT:         
			if(opt_select == 0) {
				pasteInstructions(Fields.CLEAR_POSITION);
			} else if(opt_select == 1) {
				pasteInstructions(Fields.PASTE_DEFAULT);
			} else if(opt_select == 2) {
				pasteInstructions(Fields.NEW_POSITION);
			} else if(opt_select == 3){
				pasteInstructions(Fields.PASTE_REVERSE | Fields.CLEAR_POSITION);
			} else if(opt_select == 4) {
				pasteInstructions(Fields.PASTE_REVERSE);
			} else if(opt_select == 5) {
				pasteInstructions(Fields.PASTE_REVERSE | Fields.NEW_POSITION );
			} else if(opt_select == 6) {
				pasteInstructions(Fields.PASTE_REVERSE | Fields.REVERSE_MOTION);
			} else {
				pasteInstructions(Fields.PASTE_REVERSE | Fields.NEW_POSITION | Fields.REVERSE_MOTION);
			}

			display_stack.pop();
			lastScreen();
			break;
		case SELECT_COMMENT:
			activeInstruction().toggleCommented();

			updateScreen(); 
			break;
		case VIEW_INST_REG:
			displayPoint = null;
			lastScreen();
			break;
		case FIND_REPL:
			lastScreen();  
			break;
		case JUMP_TO_LINE:
			int jumpToInst = Integer.parseInt(workingText) - 1;
			setActive_instr(max(0, min(jumpToInst, activeProgram().getInstructions().size() - 1)));

			lastScreen();
			break;
		case SWAP_PT_TYPE:
			if(opt_select == 0) {
				// View Cartesian values
				nextScreen(Screen.NAV_PREGS_C);
			} else if(opt_select == 1) {
				// View Joint values
				nextScreen(Screen.NAV_PREGS_J);
			}
			break;

			//Register navigation/ edit
		case NAV_DATA:
			if(opt_select == 0) {
				// Data Register Menu
				nextScreen(Screen.NAV_DREGS);
			} else if(opt_select == 1) {
				// Position Register Menu
				nextScreen(Screen.NAV_PREGS_C);
			}
			break;
		case CP_DREG_COM:
			int regIdx = -1;

			try {
				// Copy the comment of the curent Data register to the Data register at the specified index
				regIdx = Integer.parseInt(workingText) - 1;
				RegisterFile.getDReg(regIdx).setComment(RegisterFile.getDReg(active_index).getComment());
				saveRegisterBytes( new File(sketchPath("tmp/registers.bin")) );
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
				// Copy the value of the curent Data register to the Data register at the specified index
				regIdx = Integer.parseInt(workingText) - 1;
				(RegisterFile.getDReg(regIdx)).value = (RegisterFile.getDReg(active_index)).value;
				saveRegisterBytes( new File(sketchPath("tmp/registers.bin")) );
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
				// Copy the comment of the curent Position register to the Position register at the specified index
				regIdx = Integer.parseInt(workingText) - 1;
				RegisterFile.getPReg(regIdx).setComment(RegisterFile.getPReg(active_index).getComment());
				saveRegisterBytes( new File(sketchPath("tmp/registers.bin")) );
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
				// Copy the point of the curent Position register to the Position register at the specified index
				regIdx = Integer.parseInt(workingText) - 1;
				Point copy_pt = (RegisterFile.getPReg(active_index)).point;
				(RegisterFile.getPReg(regIdx)).point = copy_pt.clone();
				saveRegisterBytes( new File(sketchPath("tmp/registers.bin")) );
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
				// Read inputted Float value
				f = Float.parseFloat(workingText);
				// Clamp the value between -9999 and 9999, inclusive
				f = max(-9999f, min(f, 9999f));
				System.out.printf("Index; %d\n", active_index);
				if(active_index >= 0 && active_index < RegisterFile.REG_SIZE) {
					// Save inputted value
					(RegisterFile.getDReg(active_index)).value = f;
					saveRegisterBytes( new File(sketchPath("tmp/registers.bin")) );
				}
			} catch (NumberFormatException NFEx) {
				// Invalid input value
				println("Value must be a real number!");
			}

			lastScreen();
			break;
		case NAV_DREGS:
			if(getCol_select() == 0) {
				// Edit register comment
				nextScreen(Screen.EDIT_DREG_COM);
			} else if(getCol_select() >= 1) {
				// Edit Data Register value
				nextScreen(Screen.EDIT_DREG_VAL);
			}
			break;
		case NAV_PREGS_J:
		case NAV_PREGS_C:   
			if(getCol_select() == 0) {
				// Edit register comment
				nextScreen(Screen.EDIT_PREG_COM);
			} else if(getCol_select() >= 1) {
				// Edit Position Register value
				nextScreen((mode == (Screen.NAV_PREGS_C)) ? Screen.EDIT_PREG_C : Screen.EDIT_PREG_J);
			}
			break;
		case EDIT_PREG_C:
			createRegisterPoint(false);  
			lastScreen();
			break;
		case EDIT_PREG_J:      
			createRegisterPoint(true);
			lastScreen();
			break;
		case EDIT_PREG_COM:
			if (!workingText.equals("\0")) {
				if(workingText.charAt(  workingText.length() - 1  ) == '\0') {
					workingText = workingText.substring(0, workingText.length() - 1);
				}
				// Save the inputed comment to the selected register
				RegisterFile.getPReg(active_index).setComment(workingText);
				saveRegisterBytes( new File(sketchPath("tmp/registers.bin")) );
				workingText = "";
				lastScreen();
			}
			break;
		case EDIT_DREG_COM:
			if (!workingText.equals("\0")) {
				if(workingText.charAt(  workingText.length() - 1  ) == '\0') {
					workingText = workingText.substring(0, workingText.length() - 1);
				}
				// Save the inputed comment to the selected register\
				RegisterFile.getDReg(active_index).setComment(workingText);
				saveRegisterBytes( new File(sketchPath("tmp/registers.bin")) );
				workingText = "";
				lastScreen();
			}
			break;
		default:
			break;
		}
	}//End enter

	public void TOOL1() {
		if(getSU_macro_bindings()[0] != null && shift) {
			getSU_macro_bindings()[0].execute();
		}
	}

	public void TOOL2() {
		if(getSU_macro_bindings()[1] != null && shift) {
			getSU_macro_bindings()[1].execute();
		}
	}

	public void MVMU() {
		if(getSU_macro_bindings()[2] != null && shift) {
			getSU_macro_bindings()[2].execute();
		}
	}

	public void SETUP() {
		if(getSU_macro_bindings()[3] != null && shift) {
			getSU_macro_bindings()[3].execute();
		}
	}

	public void STATUS() {
		if(getSU_macro_bindings()[4] != null && shift) {
			getSU_macro_bindings()[4].execute();
		}
	}

	public void ITEM() {
		if(mode == Screen.NAV_PROG_INSTR) {
			opt_select = 0;
			workingText = "";
			nextScreen(Screen.JUMP_TO_LINE);
		}
	}

	public void BKSPC() {
		if(mode.getType() == ScreenType.TYPE_NUM_ENTRY) {
			// Functions as a backspace key
			if(workingText.length() > 1) {
				workingText = workingText.substring(0, workingText.length() - 1);
			} 
			else {
				workingText = "";
			}

		} else if(mode.getType() == ScreenType.TYPE_POINT_ENTRY) {

			// backspace function for current row
			if(getRow_select() >= 0 && getRow_select() < contents.size()) {
				String value = contents.get(getRow_select()).get(1);

				if (value.length() == 1) {
					contents.get(getRow_select()).set(1, "");
				} else if(value.length() > 1) {

					if (value.length() == 2 && value.charAt(0) == '-') {
						// Do not remove line prefix until the last digit is removed
						contents.get(getRow_select()).set(1, "");
					} else {
						contents.get(getRow_select()).set(1, value.substring(0, value.length() - 1));
					}
				}
			}
		} else if(mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
			// Backspace function
			if(workingText.length() > 1) {
				// ifan insert space exists, preserve it
				if(workingText.charAt(workingText.length() - 1) == '\0') {
					workingText = workingText.substring(0, workingText.length() - 2) + "\0";
				} 
				else {
					workingText = workingText.substring(0, workingText.length() - 1);
				}

				setCol_select(min(getCol_select(), workingText.length() - 1));
			} else {
				workingText = "\0";
			}

			for(int idx = 0; idx < letterStates.length; ++idx) { letterStates[idx] = 0; }
		}

		updateScreen();
	}

	public void COORD() {
		if(shift) {
			nextScreen(Screen.ACTIVE_FRAMES);
		} else {  
			// Update the coordinate mode
			coordFrameTransition();
			updateScreen();
		}
	}

	public void SPEEDUP() {
		// Increase the speed at which the Robot jogs
		if (shift) {

			if (liveSpeed < 5) {
				liveSpeed = 5;
			} else if (liveSpeed < 50) {
				liveSpeed = 50;
			} else {
				liveSpeed = 100;
			}
		} else if (liveSpeed < 100) {

			if (liveSpeed < 5) {
				++liveSpeed;
			} else if (liveSpeed < 50) {
				liveSpeed += 5;
			} else if (liveSpeed < 100) {
				liveSpeed += 10f;
			}
		}

		// The Robot's speed multiplier is bounded to the range 1% to 100%
		liveSpeed = min(liveSpeed, 100);
	}


	public void SLOWDOWN() {
		// Reduce the speed at which the Robot jogs
		if (shift) {

			if (liveSpeed > 50) {
				liveSpeed = 50;
			} else if (liveSpeed > 5) {
				liveSpeed = 5;
			} else {
				liveSpeed = 1;
			}
		} else if (liveSpeed > 1) {

			if (liveSpeed <= 5f) {
				--liveSpeed;
			} else if (liveSpeed <= 50) {
				liveSpeed -= 5;
			} else {
				liveSpeed -= 10;
			}
		}

		// The Robot's speed multiplier is bounded to the range 1% to 100%
		liveSpeed = max(1, liveSpeed);
	}

	public void EE() {
		armModel.cycleEndEffector();
	}

	public void JOINT1_NEG() {
		updateRobotJogMotion(0, -1);
	}

	public void JOINT1_POS() {
		updateRobotJogMotion(0, 1);
	}

	public void JOINT2_NEG() {
		updateRobotJogMotion(1, -1);
	}

	public void JOINT2_POS() {
		updateRobotJogMotion(1, 1);
	}

	public void JOINT3_NEG() {
		if (shift) {
			updateRobotJogMotion(2, -1);
		}
	}

	public void JOINT3_POS() {
		updateRobotJogMotion(2, 1);
	}

	public void JOINT4_NEG() {
		updateRobotJogMotion(3, -1);
	}

	public void JOINT4_POS() {
		updateRobotJogMotion(3, 1);
	}

	public void JOINT5_NEG() {
		updateRobotJogMotion(4, -1);
	}

	public void JOINT5_POS() {
		updateRobotJogMotion(4, 1);
	}

	public void JOINT6_NEG() {
		updateRobotJogMotion(5, -1);
	}

	public void JOINT6_POS() {
		updateRobotJogMotion(5, 1);
	}

	public void updateRobotJogMotion(int button, int direction) {
		// Only six jog button pairs exist
		if (button >= 0 && button < 6) {
			float newDir;

			if(armModel.getCurCoordFrame() == CoordFrame.JOINT) {
				// Move single joint
				newDir = activateLiveJointMotion(button, direction);
			} else {
				// Move entire robot in a single axis plane
				newDir = activateLiveWorldMotion(button, direction);
			}

			Button negButton = ((Button)cp5.get("JOINT" + (button + 1) + "_NEG")),
					posButton = ((Button)cp5.get("JOINT" + (button + 1) + "_POS"));

			if (newDir > 0) {
				// Positive motion
				negButton.setColorBackground(Fields.BUTTON_DEFAULT);
				posButton.setColorBackground(Fields.BUTTON_ACTIVE);
			} else if (newDir < 0) {
				// Negative motion
				negButton.setColorBackground(Fields.BUTTON_ACTIVE);
				posButton.setColorBackground(Fields.BUTTON_DEFAULT);
			} else {
				// No motion
				negButton.setColorBackground(Fields.BUTTON_DEFAULT);
				posButton.setColorBackground(Fields.BUTTON_DEFAULT);
			}
		}
	}

	/**
	 * Updates the motion of one of the Robot's joints based on
	 * the joint index given and the value of dir (-/+ 1). The
	 * Robot's joint indices range from 0 to 5. ifthe joint
	 * Associate with the given index is already in motion,
	 * in either direction, then calling this method for that
	 * joint index will stop that joint's motion.
	 * 
	 * @returning  The new motion direction of the Robot
	 */
	public float activateLiveJointMotion(int joint, int dir) {

		if (!shift || motionFault) {
			// Only move when shift is set and there is no error
			return 0f;
		}

		return armModel.setJointMotion(joint, dir);
	}

	/**
	 * Updates the motion of the Robot with respect to one of the World axes for
	 * either linear or rotational motion around the axis. Similiar to the
	 * activateLiveJointMotion() method, calling this method for an axis, in which
	 * the Robot is already moving, will result in the termination of the Robot's
	 * motion in that axis. Rotational and linear motion for an axis are mutually
	 * independent in this regard.
	 * 
	 * @param axis        The axis of movement for the robotic arm:
                  x - 0, y - 1, z - 2, w - 3, p - 4, r - 5
	 * @pararm dir        +1 or -1: indicating the direction of motion
	 * @returning         The new direction of motion in the given axis
	 *
	 */
	public float activateLiveWorldMotion(int axis, int dir) {
		if (!shift || motionFault) {
			// Only move when shift is set and there is no error
			return 0f;
		}

		// Initiaize the Robot's destination
		Point RP = nativeRobotEEPoint(armModel.getJointAngles());
		armModel.tgtPosition = RP.position;
		armModel.tgtOrientation = RP.orientation;


		if(axis >= 0 && axis < 3) {
			if(armModel.jogLinear[axis] == 0) {
				// Begin movement on the given axis in the given direction
				armModel.jogLinear[axis] = dir;
			} else {
				// Halt movement
				armModel.jogLinear[axis] = 0;
			}

			return armModel.jogLinear[axis];
		}
		else if(axis >= 3 && axis < 6) {
			axis %= 3;
			if(armModel.jogRot[axis] == 0) {
				// Begin movement on the given axis in the given direction
				armModel.jogRot[axis] = dir;
			}
			else {
				// Halt movement
				armModel.jogRot[axis] = 0;
			}

			return armModel.jogRot[axis];
		}

		return 0f;
	}

	//turn of highlighting on all active movement buttons
	public void resetButtonColors() {
		for(int i = 1; i <= 6; i += 1) {
			((Button)cp5.get("JOINT"+i+"_NEG")).setColorBackground(Fields.BUTTON_DEFAULT);
			((Button)cp5.get("JOINT"+i+"_POS")).setColorBackground(Fields.BUTTON_DEFAULT);
		}
	}

	/**
	 * Transitions the display to the given screen and pushes that screen
	 * onto the stack.
	 * 
	 * @param next    The new screen mode
	 */
	public void nextScreen(Screen next) {
		// Stop a program from executing when transition screens
		setProgramRunning(false);
		if (DISPLAY_TEST_OUTPUT) { System.out.printf("%s => %s\n", mode, next);  }

		mode = next;
		display_stack.push(mode);
		loadScreen();
	}

	/**
	 * Transitions to the given screen without saving the current screen on the stack.
	 * 
	 * @param nextScreen  The new screen mode
	 */
	public void switchScreen(Screen nextScreen) {
		// Stop a program from executing when transition screens
		setProgramRunning(false);
		if (DISPLAY_TEST_OUTPUT) { System.out.printf("%s => %s\n", mode, nextScreen);  }

		mode = nextScreen;
		display_stack.pop();
		display_stack.push(mode);
		loadScreen();
	}

	/**
	 * Transitions the display to the previous screen that the user was on.
	 */
	public boolean lastScreen() {
		// Stop a program from executing when transition screens
		setProgramRunning(false);
		if (display_stack.peek() == Screen.DEFAULT) {
			if (DISPLAY_TEST_OUTPUT) { System.out.printf("%s\n", mode); }
			return false;
		}
		else{
			display_stack.pop();
			if (DISPLAY_TEST_OUTPUT) { System.out.printf("%s => %s\n", mode, display_stack.peek()); }
			mode = display_stack.peek();

			loadScreen();
			return true;
		}
	}

	public void resetStack(){
		// Stop a program from executing when transition screens
		setProgramRunning(false);
		display_stack.clear();

		mode = Screen.DEFAULT;
		display_stack.push(mode);
	}

	public void loadScreen() {  
		switch(mode){
		//Main menu
		case NAV_MAIN_MENU:
			active_index = 0;
			opt_select = 0;
			setCol_select(0);
			break;

			//Frames
		case ACTIVE_FRAMES:
			setRow_select(0);
			setCol_select(1);
			workingText = Integer.toString(armModel.getActiveToolFrame() + 1);
			break;
		case SELECT_FRAME_MODE:
			active_index = 0;
			opt_select = 0;
			break;
		case NAV_TOOL_FRAMES:
		case NAV_USER_FRAMES:
			setRow_select(active_index*2);
			setCol_select(0);
			break;
		case TFRAME_DETAIL:
		case UFRAME_DETAIL:
			setRow_select(-1);
			setCol_select(-1);
			setStart_render(0);
			opt_select = -1;
			break;
		case TEACH_3PT_TOOL:
		case TEACH_3PT_USER:
		case TEACH_4PT:
		case TEACH_6PT:
			opt_select = 0;
			break;
		case FRAME_METHOD_TOOL:
		case FRAME_METHOD_USER:
			setRow_select(-1);
			setCol_select(-1);
			opt_select = 0;
			break;

			//Programs and instructions
		case NAV_PROGRAMS:
			// Stop Robot movement (i.e. program execution)
			armModel.halt();
			setRow_select(getActive_prog());
			setCol_select(0);
			setActive_instr(0);
			break;
		case PROG_CREATE:
			setRow_select(1);
			setCol_select(0);
			opt_select = 0;
			workingText = "\0";
			break;
		case PROG_RENAME:
			setActive_prog(getRow_select());
			setRow_select(1);
			setCol_select(0);
			opt_select = 0;
			workingText = activeProgram().getName();
			break;
		case PROG_COPY:
			setActive_prog(getRow_select());
			setRow_select(1);
			setCol_select(0);
			opt_select = 0;
			workingText = "\0";
			break;
		case NAV_PROG_INSTR:
			//need to enforce row/ column select limits based on 
			//program length/ instruction width
			if(prev_select != -1) {
				setRow_select(prev_select);
				setStart_render(getRow_select());
				prev_select = -1;
			}
			opt_select = -1;
			break;
		case SET_CALL_PROG:
			prev_select = getRow_select();
			setRow_select(0);
			setCol_select(0);
			setStart_render(0);
			break;
		case CONFIRM_INSERT:
			workingText = "";
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
			opt_select = 0;
			break;
		case SET_MV_INSTR_OFFSET:
		case INPUT_DREG_IDX:
		case INPUT_IOREG_IDX:
		case INPUT_PREG_IDX1:
		case INPUT_PREG_IDX2:
		case INPUT_CONST:
			workingText = "";
			break;
		case SET_IO_INSTR_IDX:
		case SET_JUMP_TGT:
		case SET_LBL_NUM:
			setCol_select(1);
			opt_select = 0;
			workingText = ""; //<>//
			break;
		case SET_MV_INSTR_TYPE:
			MotionInstruction mInst = activeMotionInst();

			switch (mInst.getMotionType()) {
			case MTYPE_JOINT:
				opt_select = 0;
				break;
			case MTYPE_LINEAR:
				opt_select = 1;
				break;
			case MTYPE_CIRCULAR:
				opt_select = 2;
				break;
			default: //<>//
				opt_select = -1;
			}

			break;
		case SET_MV_INSTR_SPD:
			mInst = activeMotionInst();
			int instSpd;
			// Convert speed into an integer value
			if (mInst.getMotionType() == MTYPE_JOINT) {
				instSpd = Math.round(mInst.getSpeed() * 100f);
			} else {
				instSpd = Math.round(mInst.getSpeed());
			}

			workingText = Integer.toString(instSpd);
		case SET_MV_INSTR_REG_TYPE:
			mInst = activeMotionInst();

			if (mInst.usesGPosReg()) {
				opt_select = 1;
			} else {
				opt_select = 0;
			}

			break;
		case SET_MV_INSTR_IDX:
			mInst = activeMotionInst();
			workingText = Integer.toString(mInst.getPositionNum() + 1);
			break;
		case SET_MV_INSTR_TERM:
			mInst = activeMotionInst();
			workingText = Integer.toString(mInst.getTermination());
			break;
		case SET_FRAME_INSTR_IDX:
		case SET_SELECT_ARGVAL:
		case SET_REG_EXPR_IDX1:
		case SET_REG_EXPR_IDX2:
			opt_select = 0;
			workingText = "";
			break;
		case SET_IO_INSTR_STATE:
		case SET_FRM_INSTR_TYPE:
		case SET_REG_EXPR_TYPE:
			opt_select = 0;
			break;
		case SELECT_INSTR_DELETE:
		case SELECT_COMMENT:
		case SELECT_CUT_COPY:
			Program p = activeProgram();
			int size = p.getInstructions().size() - 1;
			setActive_instr(max(0, min(getActive_instr(), size)));
			setCol_select(0);
			break;

			//Macros
		case NAV_MACROS:
			setRow_select(active_index);
			break;
		case SET_MACRO_PROG:
			setRow_select(0);
			break;
		case SET_MACRO_TYPE:
		case SET_MACRO_BINDING:
			opt_select = 0;
			break;

			//Registers
		case NAV_DATA:
			opt_select = 0;
			active_index = 0;
			break;
		case NAV_DREGS:
		case NAV_PREGS_J:
		case NAV_PREGS_C:
			setRow_select(active_index);
			setCol_select(0);
			break;
		case DIRECT_ENTRY_TOOL:
		case DIRECT_ENTRY_USER:
			setRow_select(0);
			setCol_select(1);
			contents = loadFrameDirectEntry(teachFrame);
			options = new ArrayList<String>();
			break;
		case NAV_INSTR_MENU:
			opt_select = 0;
			break;
		case VIEW_INST_REG:
			opt_select = -1;
			break;
		case SWAP_PT_TYPE:
			opt_select = 0;
			break;
		case CP_DREG_COM:
		case CP_DREG_VAL:
		case CP_PREG_COM:
		case CP_PREG_PT:
			opt_select = 1;
			workingText = Integer.toString((active_index + 1));
			break;
		case EDIT_DREG_COM:
			setRow_select(1);
			setCol_select(0);
			opt_select = 0;

			String c = RegisterFile.getDReg(active_index).getComment();
			if(c != null && c.length() > 0) {
				workingText = c;
			}
			else {
				workingText = "\0";
			}

			break;   
		case EDIT_PREG_COM:
			setRow_select(1);
			setCol_select(0);
			opt_select = 0;
			
			c = RegisterFile.getPReg(active_index).getComment();
			if(c != null && c.length() > 0) {
				workingText = c;
			}
			else {
				workingText = "\0";
			}

			println(workingText.length());
			break;
		case EDIT_DREG_VAL:
			opt_select = 0;
			// Bring up float input menu
			if((RegisterFile.getDReg(active_index)).value != null) {
				workingText = Float.toString((RegisterFile.getDReg(active_index)).value);
			} else {
				workingText = "";
			}
			break;
		case EDIT_PREG_C:
		case EDIT_PREG_J:
			setRow_select(0);
			setCol_select(1);
			contents = loadPosRegEntry(RegisterFile.getPReg(active_index));
			break;
		default:
			break;
		}

		updateScreen();
	}

	// update text displayed on screen
	public void updateScreen() {
		int next_px = display_px;
		int next_py = display_py;
		int txt, bg;

		clearScreen();

		// draw display background
		cp5.addTextarea("txt")
		.setPosition(display_px,display_py)
		.setSize(display_width, display_height)
		.setColorBackground(Fields.UI_LIGHT)
		.moveTo(g1);

		String header = null;
		// display the name of the program that is being edited
		header = getHeader(mode);

		if(header != null) {
			// Display header field
			cp5.addTextarea("header")
			.setText(" " + header)
			.setFont(fnt_con14)
			.setPosition(next_px, next_py)
			.setSize(display_width, 20)
			.setColorValue(Fields.UI_LIGHT)
			.setColorBackground(Fields.UI_DARK)
			.hideScrollbar()
			.show()
			.moveTo(g1);

			next_py += 20;
		}

		contents = getContents(mode);
		options = getOptions(mode);

		boolean selectMode = (mode.getType() == ScreenType.TYPE_LINE_SELECT);

		/*************************
		 *    Display Contents   *
		 *************************/

		if(contents.size() > 0) {
			setRow_select(clamp(getRow_select(), 0, contents.size() - 1));
			setCol_select(clamp(getCol_select(), 0, contents.get(getRow_select()).size() - 1));
			setStart_render(clamp(getStart_render(), getRow_select() - (ITEMS_TO_SHOW - 1), getRow_select()));
		}

		index_contents = 1;
		for(int i = getStart_render(); i < contents.size() && i - getStart_render() < ITEMS_TO_SHOW; i += 1) {
			//get current line
			DisplayLine temp = contents.get(i);
			next_px = display_px + temp.getxAlign();

			if(i == getRow_select()) { bg = Fields.UI_DARK; }
			else                { bg = Fields.UI_LIGHT;}

			//if(i == 0 || contents.get(i - 1).itemIdx != contents.get(i).itemIdx) {
			//  //leading row select indicator []
			//  cp5.addTextarea(Integer.toString(index_contents))
			//  .setText("")
			//  .setPosition(next_px, next_py)
			//  .setSize(10, 20)
			//  .setColorBackground(bg)
			//  .hideScrollbar()
			//  .moveTo(g1);
			//}

			//index_contents++;
			//next_px += 10;

			//draw each element in current line
			for(int j = 0; j < temp.size(); j += 1) {
				if(i == getRow_select()) {
					if(j == getCol_select() && !selectMode){
						//highlight selected row + column
						txt = Fields.UI_LIGHT;
						bg = Fields.UI_DARK;          
					} 
					else if(selectMode && !selectedLines[contents.get(i).getItemIdx()]) {
						//highlight selected line
						txt = Fields.UI_LIGHT;
						bg = Fields.UI_DARK;
					}
					else {
						txt = Fields.UI_DARK;
						bg = Fields.UI_LIGHT;
					}
				} else if(selectMode && selectedLines[contents.get(i).getItemIdx()]) {
					//highlight any currently selected lines
					txt = Fields.UI_LIGHT;
					bg = Fields.UI_DARK;
				} else {
					//display normal row
					txt = Fields.UI_DARK;
					bg = Fields.UI_LIGHT;
				}

				//grey text for comme also this
				if(temp.size() > 0 && temp.get(0).contains("//")) {
					txt = color(127);
				}

				cp5.addTextarea(Integer.toString(index_contents))
				.setText(temp.get(j))
				.setFont(fnt_con14)
				.setPosition(next_px, next_py)
				.setSize(temp.get(j).length()*Fields.CHAR_WDTH + Fields.TXT_PAD, 20)
				.setColorValue(txt)
				.setColorBackground(bg)
				.hideScrollbar()
				.moveTo(g1);

				index_contents++;
				next_px += temp.get(j).length()*Fields.CHAR_WDTH + (Fields.TXT_PAD - 8); 
			}//end draw line elements

			if(i == getRow_select()) { txt = Fields.UI_DARK;  }
			else                { txt = Fields.UI_LIGHT; }

			////Trailing row select indicator []
			//cp5.addTextarea(Integer.toString(index_contents))
			//.setText("")
			//.setPosition(next_px, next_py)
			//.setSize(10, 20)
			//.setColorBackground(txt)
			//.hideScrollbar()
			//.moveTo(g1);

			next_py += 20;
			//index_contents += 1;
		}//end display contents

		// display options for an element being edited
		next_px = display_px;
		next_py += contents.size() == 0 ? 0 : 20;

		int maxHeight;
		if(mode.getType() == ScreenType.TYPE_EXPR_EDIT) {
			maxHeight = 3;
		} else if(mode == Screen.SELECT_PASTE_OPT) {
			maxHeight = 3;
		} else {
			maxHeight = options.size();
		}

		/*************************
		 *    Display Options    *
		 *************************/

		index_options = 100;
		for(int i = 0; i < options.size(); i += 1) {
			if(i == opt_select) {
				txt = Fields.UI_LIGHT;
				bg = Fields.UI_DARK;
			}
			else {
				txt = Fields.UI_DARK;
				bg = Fields.UI_LIGHT;
			}

			cp5.addTextarea(Integer.toString(index_options))
			.setText(" " + options.get(i))
			.setFont(fnt_con14)
			.setPosition(next_px, next_py)
			.setSize(options.get(i).length()*8 + 40, 20)
			.setColorValue(txt)
			.setColorBackground(bg)
			.hideScrollbar()
			.moveTo(g1);

			index_options++;
			next_px += (i % maxHeight == maxHeight - 1) ? 120 : 0;
			next_py += (i % maxHeight == maxHeight - 1) ? -20*(maxHeight - 1) : 20;    
		}

		// display the numbers that the user has typed
		next_py += 20;
		index_nums = 1000;
		for(int i = 0; i < nums.size(); i+= 1) {
			if(nums.get(i) == -1) {
				cp5.addTextarea(Integer.toString(index_nums))
				.setText(".")
				.setFont(fnt_con14)
				.setPosition(next_px, next_py)
				.setSize(40, 20)
				.setColorValue(Fields.UI_LIGHT)
				.setColorBackground(color(255, 0, 0))
				.hideScrollbar()
				.moveTo(g1);
			}
			else {
				cp5.addTextarea(Integer.toString(index_nums))
				.setText(Integer.toString(nums.get(i)))
				.setFont(fnt_con14)
				.setPosition(next_px, next_py)
				.setSize(40, 20)
				.setColorValue(Fields.UI_LIGHT)
				.setColorBackground(color(255, 0, 0))
				.hideScrollbar()
				.moveTo(g1);
			}

			index_nums++;
			next_px += options.get(i).length()*8 + 20;   
		}

		// display hints for function keys
		String[] funct;
		funct = getFunctionLabels(mode);

		//set f button text labels
		for(int i = 0; i < 5; i += 1) {
			cp5.addTextarea("lf"+i)
			.setText(funct[i])
			.setFont(fnt_con12)
			// Keep function labels in their original place
			.setPosition(display_width*i/5 + 15 , display_height - g1_py)
			.setSize(display_width/5 - 5, 20)
			.setColorValue(Fields.UI_DARK)
			.setColorBackground(Fields.UI_LIGHT)
			.hideScrollbar()
			.moveTo(g1);
		}
	} // end updateScreen()

	/*Text generation methods*/

	//Header text
	public String getHeader(Screen mode){
		String header = null;

		switch(mode) {
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
		case VIEW_INST_REG:
			header = activeProgram().getName();
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
		case NAV_PREGS_J:
			header = "POSTION REGISTERS (J)";
			break;
		case NAV_PREGS_C:
			header = "POSTION REGISTERS (C)";
			break;
		case CP_PREG_COM:
		case CP_PREG_PT:
		case SWAP_PT_TYPE:
			header = "POSTION REGISTERS";
			break;
		case EDIT_DREG_VAL:
			header = "REGISTERS";
			break;
		case EDIT_PREG_C:
		case EDIT_PREG_J:
			header = "POSITION REGISTER: ";

			if(mode != Screen.EDIT_DREG_COM && RegisterFile.getPReg(active_index).getComment() != null) {
				// Show comment if it exists
				header += RegisterFile.getPReg(active_index).getComment();
			} 
			else {
				header += active_index;
			}
			break;
		case EDIT_DREG_COM:
			header = String.format("Enter a name for R[%d]", active_index);
			break;
		case EDIT_PREG_COM:
			header = String.format("Enter a name for PR[%d]", active_index);
			break;
		default:
			break;
		}

		return header;
	}

	//Main display content text
	public ArrayList<DisplayLine> getContents(Screen mode){
		ArrayList<DisplayLine> contents = new ArrayList<DisplayLine>();

		switch(mode) {
		//Program list navigation/ edit
		case NAV_PROGRAMS:
		case SET_CALL_PROG:
		case SET_MACRO_PROG:
			contents = loadPrograms();
			break;

		case PROG_CREATE:
		case PROG_RENAME:
		case PROG_COPY:
			contents = loadTextInput();
			break;

			//View instructions
		case CONFIRM_INSERT:
		case CONFIRM_RENUM:
		case FIND_REPL:
		case NAV_PROG_INSTR:
		case VIEW_INST_REG:
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
			contents = loadInstructions(getActive_prog());
			break;

		case ACTIVE_FRAMES:
			/* workingText corresponds to the active row's index display */
			if (getRow_select() == 0) {
				contents.add(newLine("Tool: ", workingText));
				contents.add(newLine("User: ", Integer.toString(armModel.getActiveUserFrame() + 1)));
			} else {
				contents.add(newLine("Tool: ", Integer.toString(armModel.getActiveToolFrame() + 1)));
				contents.add(newLine("User: ", workingText));
			}
			break;

			//View frame details
		case NAV_TOOL_FRAMES:
			contents = loadFrames(CoordFrame.TOOL);
			break;
		case NAV_USER_FRAMES:
			contents = loadFrames(CoordFrame.USER);
			break;
			//View frame details
		case TFRAME_DETAIL:
		case TEACH_3PT_TOOL:
		case TEACH_6PT:
			contents = loadFrameDetail(CoordFrame.TOOL);
			break;
		case UFRAME_DETAIL:
		case TEACH_3PT_USER:
		case TEACH_4PT:
			contents = loadFrameDetail(CoordFrame.USER);
			break;
		case FRAME_METHOD_TOOL:
		case FRAME_METHOD_USER:
		case DIRECT_ENTRY_USER:
		case DIRECT_ENTRY_TOOL:
		case EDIT_DREG_VAL:
		case CP_DREG_COM:
		case CP_DREG_VAL:
		case CP_PREG_COM:
		case CP_PREG_PT:
		case SWAP_PT_TYPE:
			contents = this.contents;
			break;

			//View/ edit macros
		case NAV_MACROS:
		case SET_MACRO_TYPE:
		case SET_MACRO_BINDING:
			contents = loadMacros();
			break;

		case NAV_MF_MACROS:
			contents = loadManualFunct();
			break;

			//View/ edit registers
		case NAV_DREGS:
		case NAV_PREGS_C:
		case NAV_PREGS_J:
			contents = loadRegisters();
			break;
		case EDIT_PREG_C:
		case EDIT_PREG_J:
			contents = this.contents;
			break;
		case EDIT_DREG_COM:
		case EDIT_PREG_COM:
			contents = loadTextInput();
			break;

		default:
			break;
		}

		return contents;
	}
	//Options menu text
	public ArrayList<String> getOptions(Screen mode){
		ArrayList<String> options = new ArrayList<String>();

		switch(mode) {
		//Main menu and submenus
		case NAV_MAIN_MENU:
			options.add("1 Frames"           );
			options.add("2 Macros"           );
			options.add("3 Manual Fncts"     );
			break;

		case CONFIRM_PROG_DELETE:
			options.add("Delete selected program?");
			break;

			//Instruction options
		case NAV_INSTR_MENU:
			options.add("1 Insert"           );
			options.add("2 Delete"           );
			options.add("3 Cut/ Copy"        );
			options.add("4 Paste"            );
			options.add("5 Find/ Replace"    );
			options.add("6 Renumber"         );
			options.add("7 Comment"          );
			options.add("8 Remark"           );
			break;
		case CONFIRM_INSERT:
			options.add("Enter number of lines to insert:");
			options.add("\0" + workingText);
			break;
		case SELECT_INSTR_DELETE:
			options.add("Select lines to delete (ENTER).");
			break;
		case SELECT_CUT_COPY:
			options.add("Select lines to cut/ copy (ENTER).");
			break;
		case SELECT_PASTE_OPT:
			options.add("1 Logic");
			options.add("2 Position");
			options.add("3 Pos ID");
			options.add("4 R Logic");
			options.add("5 R Position");
			options.add("6 R Pos ID");
			options.add("7 RM Pos ID");
			break;
		case FIND_REPL:
			options.add("Enter text to search for:");
			options.add("\0" + workingText);
			break;
		case CONFIRM_RENUM: 
			options.add("Renumber program positions?");
			break;
		case SELECT_COMMENT:
			options.add("Select lines to comment/uncomment.");
			break;

			//Instruction edit options
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
			options = loadInstrEdit(mode);
			break;

			//Insert instructions (non-movemet)
		case SELECT_INSTR_INSERT:
			options.add("1. I/O"       );
			options.add("2. Frames"    );
			options.add("3. Registers" );
			options.add("4. IF/SELECT" );
			options.add("5. JMP/LBL"   );
			options.add("6. CALL"      );
			options.add("7. WAIT (NA)"      );
			options.add("8. Macro (NA)"     );
			break;
		case SELECT_IO_INSTR_REG:
			options = loadIORegisters();
			break;
		case SELECT_FRAME_INSTR_TYPE:
			options.add("1. TFRAME_NUM = ...");
			options.add("2. UFRAME_NUM = ...");
			break;
		case SELECT_REG_STMT:
			options.add("1. R[x] = (...)");
			options.add("2. IO[x] = (...)");
			options.add("3. PR[x] = (...)");
			options.add("4. PR[x, y] = (...)");
			break;
		case SELECT_COND_STMT:
			options.add("1. IF Stmt");
			options.add("2. IF (...)");
			options.add("3. SELECT Stmt");
			break;
		case SELECT_JMP_LBL:
			options.add("1. LBL[...]");
			options.add("2. JMP LBL[...]");
			break;

			//Frame navigation and edit menus
		case SELECT_FRAME_MODE:
			options.add("1. Tool Frame");
			options.add("2. User Frame");
			break;
		case FRAME_METHOD_TOOL:
			options.add("1. Three Point Method");
			options.add("2. Six Point Method");
			options.add("3. Direct Entry Method");
			break;
		case FRAME_METHOD_USER:
			options.add("1. Three Point Method");
			options.add("2. Four Point Method");
			options.add("3. Direct Entry Method");
			break;
		case VIEW_INST_REG:
			options = loadInstructionReg();
			break;
		case TEACH_3PT_TOOL:
		case TEACH_3PT_USER:
		case TEACH_4PT:
		case TEACH_6PT:
			options = loadPointList();
			break;

			//Macro edit menus
		case SET_MACRO_TYPE:
			options.add("1. Shift + User Key");
			options.add("2. Manual Function");
			break;
		case SET_MACRO_BINDING:
			options.add("1. Tool 1");
			options.add("2. Tool 2");
			options.add("3. MVMU");
			options.add("4. Setup");
			options.add("5. Status");
			options.add("6. POSN");
			options.add("7. I/O");
			break;

			//Data navigation and edit menus
		case NAV_DATA:
			options.add("1. Data Registers");
			options.add("2. Position Registers");
			break;
		case NAV_PREGS_J:
		case NAV_PREGS_C:
			opt_select = -1;
			// Display the point with the Position register of the highlighted line, when viewing the Position registers
			if (active_index >= 0 && active_index < RegisterFile.REG_SIZE && RegisterFile.getPReg(active_index).point != null) {
				String[] pregEntry = RegisterFile.getPReg(active_index).point.toLineStringArray(mode == Screen.NAV_PREGS_C);

				for (String line : pregEntry) {
					options.add(line);
				}
			}

			break;
		case CP_DREG_COM:
			options.add(String.format("Move R[%d]'s comment to:", active_index + 1));
			options.add(String.format("R[%s]", workingText));
			break;
		case CP_DREG_VAL:
			options.add(String.format("Move R[%d]'s value to:", active_index + 1));
			options.add(String.format("R[%s]", workingText));
			break;
		case CP_PREG_COM:
			options.add(String.format("Move PR[%d]'s comment to:", active_index + 1));
			options.add(String.format("PR[%s]", workingText));
			break;
		case CP_PREG_PT:
			options.add(String.format("Move PR[%d]'s point to:", active_index + 1));
			options.add(String.format("PR[%s]", workingText));
			break;
		case SWAP_PT_TYPE:
			options.add("1. Cartesian");
			options.add("2. Joint");
			break;
		case EDIT_DREG_VAL:
			options.add("Input register value:");
			options.add("\0" + workingText);
			break;
		case DIRECT_ENTRY_TOOL:
		case DIRECT_ENTRY_USER:
			options = this.options;
			break;
		case EDIT_PREG_C:
		case EDIT_PREG_J:
			break;
		case EDIT_RSTMT:
			options.add("Register");
			options.add("Position Register Point");
			options.add("Position Register Value");
			break;

			//Misc functions
		case JUMP_TO_LINE:
			options.add("Use number keys to enter line number to jump to");
			options.add("\0" + workingText);
			break;
		default:
			if (mode.getType() == ScreenType.TYPE_TEXT_ENTRY) {
				options.add("1. Uppercase");
				options.add("1. Lowercase");
			}
		}

		return options;
	}

	//Function label text
	public String[] getFunctionLabels(Screen mode){
		String[] funct = new String[5];

		switch(mode) {
		case NAV_PROGRAMS:
			// F2, F3
			funct[0] = "[Create]";
			if(armModel.numOfPrograms() > 0) {
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
			// F1, F4, F5f
			funct[0] = shift ? "[New Pt]" : "";
			funct[1] = "[New Ins]";
			funct[2] = "";
			funct[3] = "[Edit]";
			funct[4] = (getCol_select() == 0) ? "[Opt]" : "";
			if(activeInstruction() instanceof MotionInstruction) {
				funct[4] = (getCol_select() == 3) ? "[Reg]" : funct[4];
			} 
			else if(activeInstruction() instanceof IfStatement) {
				IfStatement stmt = (IfStatement)activeInstruction();
				int selectIdx = getSelectedIdx();

				if(stmt.getExpr() instanceof Expression) {
					if(selectIdx > 1 && selectIdx < stmt.getExpr().getLength() + 1) {
						funct[2] = "[Insert]";
					}
					if(selectIdx > 2 && selectIdx < stmt.getExpr().getLength() + 1) {
						funct[4] = "[Delete]";
					}
				}
			} else if(activeInstruction() instanceof SelectStatement) {
				int selectIdx = getSelectedIdx();

				if(selectIdx >= 3) {
					funct[2] = "[Insert]";
					funct[4] = "[Delete]";
				}
			} else if(activeInstruction() instanceof RegisterStatement) {
				RegisterStatement stmt = (RegisterStatement)activeInstruction();
				int rLen = (stmt.getPosIdx() == -1) ? 2 : 3;
				int selectIdx = getSelectedIdx();

				if(selectIdx > rLen && selectIdx < stmt.getExpr().getLength() + rLen) {
					funct[2] = "[Insert]";
				}
				if(selectIdx > (rLen + 1) && selectIdx < stmt.getExpr().getLength() + rLen) {
					funct[4] = "[Delete]";
				}
			}
			break;
		case VIEW_INST_REG:
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
			funct[1] = "";
			funct[2] = "[Cut]";
			funct[3] = "[Copy]";
			funct[4] = "[Cancel]";
			break;
		case NAV_TOOL_FRAMES:
		case NAV_USER_FRAMES:
			// F1, F2, F3
			if(shift) {
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
		case NAV_PREGS_C:
		case NAV_PREGS_J:
			// F1 - F5
			if (shift) {
				funct[0] = "[Clear]";
				funct[1] = "[Copy]";
				funct[2] = "[Type]";
				funct[3] = "[Move To]";
				funct[4] = "[Record]";
			} else {
				funct[0] = "[Clear]";
				funct[1] = "[Copy]";
				funct[2] = "[Switch]";
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
				if(opt_select == 0) {
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

	/*
	 * Removes all text on screen and prepares the UI to transition to a
	 * new screen display.
	 */
	public void clearScreen() {
		//remove all text labels on screen  
		List<Textarea> displayText = cp5.getAll(Textarea.class);
		for(Textarea t: displayText) {
			// ONLY remove text areas from the Pendant!
			if (t.getParent().equals(g1)) {
				cp5.remove(t.getName());
			}
		}

		cp5.update();
	}

	public void clearContents() {
		for(int i = 0; i < index_contents; i += 1) {
			if(cp5.getGroup(Integer.toString(i)) != null)
				cp5.getGroup(Integer.toString(i)).remove();
		}

		index_contents = 0;
	}

	public void clearOptions() {
		for(int i = 100; i < index_options; i += 1) {
			if(cp5.getGroup(Integer.toString(i)) != null)
				cp5.getGroup(Integer.toString(i)).remove();
		}

		index_options = 100;
	}

	public void clearNums() {
		for(int i = 1000; i < index_nums; i += 1) {
			if(cp5.getGroup(Integer.toString(i)) != null)
				cp5.getGroup(Integer.toString(i)).remove();
		}

		index_nums = 1000;
	}

	public ArrayList<DisplayLine> loadPrograms() {
		ArrayList<DisplayLine> progs = new ArrayList<DisplayLine>();
		int size = armModel.numOfPrograms();

		//int start = start_render;
		//int end = min(start + ITEMS_TO_SHOW, size);

		for(int i = 0; i < size; i += 1) {
			progs.add(newLine(i, armModel.getProgram(i).getName()));
		}

		return progs;
	}

	// prepare for displaying motion instructions on screen
	public ArrayList<DisplayLine> loadInstructions(int programID) {
		ArrayList<DisplayLine> instruct_list = new ArrayList<DisplayLine>();
		int tokenOffset = Fields.TXT_PAD - Fields.PAD_OFFSET;

		Program p = armModel.getProgram(programID);
		int size = p.getInstructions().size();

		for(int i = 0; i < size; i+= 1) {
			DisplayLine line = new DisplayLine(i);
			Instruction instr = p.getInstruction(i);
			int xPos = 10;

			// Add line number
			if(instr.isCommented()) {
				line.add("//"+Integer.toString(i+1) + ")");
			} else {
				line.add(Integer.toString(i+1) + ")");
			}

			int numWdth = line.get(line.size() - 1).length();
			xPos += numWdth*Fields.CHAR_WDTH + tokenOffset;

			if(instr instanceof MotionInstruction) {
				// Show '@' at the an instrution, if the Robot's position is close to that position stored in the instruction's register
				MotionInstruction a = (MotionInstruction)instr;
				Point ee_point = nativeRobotEEPoint(armModel.getJointAngles());
				Point instPt = a.getVector(p);

				if(instPt != null && ee_point.position.dist(instPt.position) < (liveSpeed / 100f)) {
					line.add("@");
				}
				else {
					line.add("\0");
				}

				xPos += Fields.CHAR_WDTH + tokenOffset;
			}

			String[] fields = instr.toStringArray();

			for (int j = 0; j < fields.length; j += 1) {
				String field = fields[j];
				xPos += field.length()*Fields.CHAR_WDTH + tokenOffset;

				if(field.equals("\n") && j != fields.length - 1) {
					instruct_list.add(line);
					if(instr instanceof SelectStatement) {
						xPos = 11*Fields.CHAR_WDTH + 3*tokenOffset;
					} else {
						xPos = 3*Fields.CHAR_WDTH + 3*tokenOffset;
					}

					line = new DisplayLine(i, xPos);
					xPos += field.length()*Fields.CHAR_WDTH + tokenOffset;
				} else if(xPos > display_width) {
					instruct_list.add(line);
					xPos = 2*Fields.CHAR_WDTH + tokenOffset;

					line = new DisplayLine(i, xPos);
					field = ": " + field;
					xPos += field.length()*Fields.CHAR_WDTH + tokenOffset;
				}

				if(!field.equals("\n")) {
					line.add(field);
				}
			}

			instruct_list.add(line);
		}

		if(mode.getType() != ScreenType.TYPE_LINE_SELECT) {
			instruct_list.add(newLine(size, "[End]"));
		}

		return instruct_list;
	}

	/**
	 * Deals with updating the UI after confirming/canceling a deletion
	 */
	public void updateInstructions() {
		int instSize = activeProgram().getInstructions().size();

		setActive_instr(min(getActive_instr(),  instSize - 1));
		setRow_select(min(getActive_instr(), ITEMS_TO_SHOW - 1));
		setCol_select(0);
		setStart_render(getActive_instr() - getRow_select());

		lastScreen();
	}

	public void getInstrEdit(Instruction ins, int selectIdx) {
		if(ins instanceof MotionInstruction) {
			if(getSelectedLine() == 0) {
				// edit movement instruction line 1
				switch(getCol_select()) {
				case 2: // motion type
					nextScreen(Screen.SET_MV_INSTR_TYPE);
					break;
				case 3: // register type
					nextScreen(Screen.SET_MV_INSTR_REG_TYPE);
					break;
				case 4: // register
					nextScreen(Screen.SET_MV_INSTR_IDX);
					break;
				case 5: // speed
					nextScreen(Screen.SET_MV_INSTR_SPD);
					break;
				case 6: // termination type
					nextScreen(Screen.SET_MV_INSTR_TERM);
					break;
				case 7: // offset register
					nextScreen(Screen.SET_MV_INSTR_OFFSET);
					break;
				}
			} else {
				// edit movement instruciton line 2 (circular only)
				switch(getCol_select()) {
				case 0: // register type
					nextScreen(Screen.SET_MV_INSTR_REG_TYPE);
					break;
				case 1: // register
					nextScreen(Screen.SET_MV_INSTR_IDX);
					break;
				case 2: // speed
					nextScreen(Screen.SET_MV_INSTR_SPD);
					break;
				case 3: // termination type
					nextScreen(Screen.SET_MV_INSTR_TERM);
					break;
				case 4: // offset register
					nextScreen(Screen.SET_MV_INSTR_OFFSET);
					break;
				}
			}
		}
		else if(ins instanceof FrameInstruction) {
			switch(selectIdx) {
			case 1:
				nextScreen(Screen.SET_FRM_INSTR_TYPE);
				break;
			case 2:
				nextScreen(Screen.SET_FRAME_INSTR_IDX);
				break;
			}
		}
		else if(ins instanceof IOInstruction) {
			switch(selectIdx) {
			case 1:
				nextScreen(Screen.SET_IO_INSTR_IDX);
				break;
			case 2:
				nextScreen(Screen.SET_IO_INSTR_STATE);
				break;
			}
		}
		else if(ins instanceof LabelInstruction){
			nextScreen(Screen.SET_LBL_NUM);
		}
		else if(ins instanceof JumpInstruction){
			nextScreen(Screen.SET_JUMP_TGT);
		}
		else if(ins instanceof CallInstruction){
			nextScreen(Screen.SET_CALL_PROG);
		} 
		else if(ins instanceof IfStatement){
			IfStatement stmt = (IfStatement)ins;

			if(stmt.getExpr() instanceof Expression) {
				int len = stmt.getExpr().getLength();

				if(selectIdx >= 3 && selectIdx < len + 1) {
					editExpression((Expression)stmt.getExpr(), selectIdx - 3);
				} else if(selectIdx == len + 2) {
					nextScreen(Screen.SET_IF_STMT_ACT);
				} else if(selectIdx == len + 3) {
					if(stmt.getInstr() instanceof JumpInstruction) {
						nextScreen(Screen.SET_JUMP_TGT);
					} else {
						nextScreen(Screen.SET_CALL_PROG);
					}
				}
			} 
			else if(stmt.getExpr() instanceof BooleanExpression) {
				if(selectIdx == 2) {
					opEdit = ((BooleanExpression)stmt.getExpr()).getArg1();
					nextScreen(Screen.SET_BOOL_EXPR_ARG);
				} else if(selectIdx == 3) {
					opEdit = stmt.getExpr();
					nextScreen(Screen.SET_EXPR_OP);
				} else if(selectIdx == 4){
					opEdit = ((BooleanExpression)stmt.getExpr()).getArg2();
					nextScreen(Screen.SET_BOOL_EXPR_ARG);
				} else if(selectIdx == 5){
					nextScreen(Screen.SET_IF_STMT_ACT);
				} else {
					if(stmt.getInstr() instanceof JumpInstruction) {
						nextScreen(Screen.SET_JUMP_TGT);
					} else {
						nextScreen(Screen.SET_CALL_PROG);
					}
				}
			}
		} 
		else if(ins instanceof SelectStatement) {
			SelectStatement stmt = (SelectStatement)ins;

			if(selectIdx == 2) {
				opEdit = stmt.getArg();
				nextScreen(Screen.SET_SELECT_STMT_ARG);
			} else if((selectIdx - 3) % 3 == 0 && selectIdx > 2) {
				opEdit = stmt.getCases().get((selectIdx - 3)/3);
				nextScreen(Screen.SET_SELECT_STMT_ARG);
			} else if((selectIdx - 3) % 3 == 1) {
				editIdx = (selectIdx - 3)/3;
				nextScreen(Screen.SET_SELECT_STMT_ACT);
			} else if((selectIdx - 3) % 3 == 2) {
				editIdx = (selectIdx - 3)/3;
				if(stmt.getInstrs().get(editIdx) instanceof JumpInstruction) {
					nextScreen(Screen.SET_JUMP_TGT);
				} else if(stmt.getInstrs().get(editIdx) instanceof CallInstruction) {
					nextScreen(Screen.SET_CALL_PROG);
				}
			}
		} else if(ins instanceof RegisterStatement) {
			RegisterStatement stmt = (RegisterStatement)ins;
			int len = stmt.getExpr().getLength();
			int rLen = (stmt.getPosIdx() == -1) ? 2 : 3;

			if(selectIdx == 1) {
				nextScreen(Screen.SET_REG_EXPR_TYPE);
			} else if(selectIdx == 2) {
				nextScreen(Screen.SET_REG_EXPR_IDX1);
			} else if(selectIdx == 3 && stmt.getPosIdx() != -1) {
				nextScreen(Screen.SET_REG_EXPR_IDX2);
			} else if(selectIdx >= rLen + 1 && selectIdx <= len + rLen) {
				editExpression(stmt.getExpr(), selectIdx - (rLen + 2));
			}
		}
	}

	public void editExpression(Expression expr, int selectIdx) {
		int[] elements = expr.mapToEdit();
		opEdit = expr;
		ExpressionElement e = expr.get(elements[selectIdx]);

		if(e instanceof Expression) {
			//if selecting the open or close paren
			if(selectIdx == 0 || selectIdx == e.getLength() || 
					elements[selectIdx - 1] != elements[selectIdx] || 
					elements[selectIdx + 1] != elements[selectIdx]) {
				nextScreen(Screen.SET_EXPR_ARG);
			} else {
				int startIdx = expr.getStartingIdx(elements[selectIdx]);
				editExpression((Expression)e, selectIdx - startIdx - 1);
			}
		} else if(e instanceof ExprOperand) {
			editOperand((ExprOperand)e, elements[selectIdx]);
		} else {
			editIdx = elements[selectIdx];
			nextScreen(Screen.SET_EXPR_OP);
		}
	}

	/**
	 * Accepts an ExpressionOperand object and forwards the UI to the appropriate
	 * menu to edit said object based on the operand type.
	 *
	 * @param o - The operand to be edited.
	 * @ins_idx - The index of the operand's container ExpressionElement list into which this
	 *     operand is stored.
	 *
	 */
	public void editOperand(ExprOperand o, int ins_idx) {
		switch(o.type) {
		case -2: //Uninit
			editIdx = ins_idx;
			nextScreen(Screen.SET_EXPR_ARG);
			break;
		case 0: //Float const
			opEdit = o;
			nextScreen(Screen.INPUT_CONST);
			break;
		case 1: //Bool const
			opEdit = o;
			nextScreen(Screen.SET_BOOL_CONST);
			break;
		case 2: //Data reg
			opEdit = o;
			nextScreen(Screen.INPUT_DREG_IDX);
			break;
		case 3: //IO reg
			opEdit = o;
			nextScreen(Screen.INPUT_IOREG_IDX);
			break;
		case 4: // Pos reg
			opEdit = o;
			nextScreen(Screen.INPUT_PREG_IDX1);
			break;
		case 5: // Pos reg at index
			opEdit = o;
			nextScreen(Screen.INPUT_PREG_IDX2);
			nextScreen(Screen.INPUT_PREG_IDX1);
			break;
		}
	}

	public ArrayList<String> loadInstrEdit(Screen mode) {
		ArrayList<String> edit = new ArrayList<String>();

		switch(mode){
		case SET_MV_INSTR_TYPE:
			edit.add("1.JOINT");
			edit.add("2.LINEAR");
			edit.add("3.CIRCULAR");
			break;
		case SET_MV_INSTR_REG_TYPE:
			edit.add("1.LOCAL(P)");
			edit.add("2.GLOBAL(PR)");
			break;
		case SET_MV_INSTR_IDX:
			edit.add("Enter desired position/ register:");
			edit.add("\0" + workingText);
			break;
		case SET_MV_INSTR_SPD:
			edit.add("Enter desired speed:");
			MotionInstruction castIns = activeMotionInst();

			if(castIns.getMotionType() == MTYPE_JOINT) {
				speedInPercentage = true;
				workingTextSuffix = "%";
			} else {
				workingTextSuffix = "mm/s";
				speedInPercentage = false;
			}

			edit.add(workingText + workingTextSuffix);
			break;
		case SET_MV_INSTR_TERM:
			edit.add("Enter desired termination %(0-100):");
			edit.add("\0" + workingText);
			break;
		case SET_MV_INSTR_OFFSET:
			edit.add("Enter desired offset register (1-1000):");
			edit.add("\0" + workingText);
			break;
		case SET_IO_INSTR_STATE:
			edit.add("1. ON");
			edit.add("2. OFF");
			break;
		case SET_IO_INSTR_IDX:
			edit.add("Select I/O register index:");
			edit.add("\0" + workingText);
			break;
		case SET_FRM_INSTR_TYPE:
			edit.add("1. TFRAME_NUM = x");
			edit.add("2. UFRAME_NUM = x");
			break;
		case SET_FRAME_INSTR_IDX:
			edit.add("Select frame index:");
			edit.add("\0" + workingText);
			break;
		case SET_REG_EXPR_TYPE:
			edit.add("1. R[x] = (...)");
			edit.add("2. IO[x] = (...)");
			edit.add("3. PR[x] = (...)");
			edit.add("4. PR[x, y] = (...)");
			break;
		case SET_REG_EXPR_IDX1:
			edit.add("Select register index:");
			edit.add("\0" + workingText);
			break;
		case SET_REG_EXPR_IDX2:
			edit.add("Select point index:");
			edit.add("\0" + workingText);
			break;
		case SET_EXPR_OP:
			if(opEdit instanceof BooleanExpression) {
				edit.add("1. ... =  ...");
				edit.add("2. ... <> ...");
				edit.add("3. ... >  ...");
				edit.add("4. ... <  ...");
				edit.add("5. ... >= ...");
				edit.add("6. ... <= ...");
			} else if(opEdit instanceof Expression) {
				if(activeInstruction() instanceof IfStatement) {
					edit.add("1. + ");
					edit.add("2. - ");
					edit.add("3. * ");
					edit.add("4. / ");
					edit.add("5. | ");
					edit.add("6. % ");
					edit.add("7. = ");
					edit.add("8. <> ");
					edit.add("9. > ");
					edit.add("10. < ");
					edit.add("11. >= ");
					edit.add("12. <= ");
					edit.add("13. AND ");
					edit.add("14. OR ");
					edit.add("15. NOT ");
					edit.add("16. ... ");
				} else {
					edit.add("1. + ");
					edit.add("2. - ");
					edit.add("3. * ");
					edit.add("4. / ");
					edit.add("5. | ");
					edit.add("6. % ");
				}
			}
			break;
		case SET_EXPR_ARG:
		case SET_BOOL_EXPR_ARG:
			edit.add("R[x]");
			edit.add("IO[x]");
			if(opEdit instanceof Expression) {
				edit.add("PR[x]");
				edit.add("PR[x, y]");
				edit.add("(...)");
			}
			edit.add("Const");
			break;
		case INPUT_DREG_IDX:
		case INPUT_IOREG_IDX:
		case INPUT_PREG_IDX1:
			edit.add("Input register index:");
			edit.add("\0" + workingText);
			break;
		case INPUT_PREG_IDX2:
			edit.add("Input position value index:");
			edit.add("\0" + workingText);
			break;
		case INPUT_CONST:
			edit.add("Input constant value:");
			edit.add("\0" + workingText);
			break;
		case SET_BOOL_CONST:
			edit.add("1. False");
			edit.add("2. True");
			break;
		case SET_IF_STMT_ACT:
		case SET_SELECT_STMT_ACT:
			edit.add("JMP LBL[x]");
			edit.add("CALL");
			break;
		case SET_SELECT_STMT_ARG:
			edit.add("R[x]");
			edit.add("Const");
			break;
		case SET_SELECT_ARGVAL:
			edit.add("Input value/ register index:");
			edit.add("\0" + workingText);
			break;
		case SET_LBL_NUM:
			edit.add("Set label number:");
			edit.add("\0" + workingText);
			break;
		case SET_JUMP_TGT:
			edit.add("Set jump target label:");
			edit.add("\0" + workingText);
			break;
		default:
			break;
		}

		return edit;
	}

	public ArrayList<String> loadInstructionReg() {
		ArrayList<String> instReg = new ArrayList<String>();

		// show register contents if you're highlighting a register
		Instruction ins = activeInstruction();
		if(ins instanceof MotionInstruction) {
			MotionInstruction castIns = (MotionInstruction)ins;
			Point p = castIns.getPoint(activeProgram());

			if (p != null) {
				instReg.add("Position values (press ENTER to exit):");

				String[] regEntry = p.toLineStringArray(castIns.getMotionType() != MTYPE_JOINT);

				for (String line : regEntry) {
					instReg.add(line);
				}

				if (castIns.getUserFrame() != -1) {
					Frame uFrame = armModel.getUserFrame(castIns.getUserFrame());
					displayPoint = removeFrame(p, uFrame.getOrigin(), uFrame.getOrientation());

				} else {
					displayPoint = p;
				}
			}

		} else {
			instReg.add("This position is empty (press ENTER to exit):");
		}

		return instReg;
	}

	// clears the array of selected lines
	public boolean[] resetSelection(int n) {
		selectedLines = new boolean[n];
		for(int i = 0; i < n; i += 1){
			selectedLines[i] = false;
		}

		return selectedLines;
	}

	public void pasteInstructions() {
		pasteInstructions(0);
	}

	public void pasteInstructions(int options) {
		ArrayList<Instruction> pasteList = new ArrayList<Instruction>();
		Program p = activeProgram();

		/* Pre-process instructions for insertion into program. */
		for(int i = 0; i < clipBoard.size(); i += 1) {
			Instruction instr = clipBoard.get(i).clone();

			if(instr instanceof MotionInstruction) {
				MotionInstruction m = (MotionInstruction)instr;

				if((options & Fields.CLEAR_POSITION) == Fields.CLEAR_POSITION) {
					m.setPositionNum(-1);
				}
				else if((options & Fields.NEW_POSITION) == Fields.NEW_POSITION) {
					/* Copy the current instruction's position to a new local position
       index and update the instruction to use this new position */
					int instrPos = m.getPositionNum();
					int nextPos = p.getNextPosition();

					p.addPosition(p.getPosition(instrPos).clone());
					m.setPositionNum(nextPos);
				}

				if((options & Fields.REVERSE_MOTION) == Fields.REVERSE_MOTION) {
					MotionInstruction next = null;

					for(int j = i + 1; j < clipBoard.size(); j += 1) {
						if(clipBoard.get(j) instanceof MotionInstruction) {
							next = (MotionInstruction)clipBoard.get(j).clone();
							break;
						}
					}

					if(next != null) {
						println("asdf");
						m.setMotionType(next.getMotionType());
						m.setSpeed(next.getSpeed());
					}
				}
			}

			pasteList.add(instr);
		}

		/* Perform forward/ reverse insertion. */
		for(int i = 0; i < clipBoard.size(); i += 1) {
			Instruction instr;
			if((options & Fields.PASTE_REVERSE) == Fields.PASTE_REVERSE) {
				instr = pasteList.get(pasteList.size() - 1 - i);
			} else {
				instr = pasteList.get(i);
			}

			p.addInstruction(getActive_instr() + i, instr);
		}
	}

	public void newMotionInstruction() {
		Point pt = nativeRobotEEPoint(armModel.getJointAngles());
		Frame active = armModel.getActiveFrame(CoordFrame.USER);

		if (active != null) {
			// Convert into currently active frame
			pt = applyFrame(pt, active.getOrigin(), active.getOrientation());

			if (DISPLAY_TEST_OUTPUT) {
				System.out.printf("New: %s\n", convertNativeToWorld(pt.position));
			}
		}

		Program prog = activeProgram();
		int reg = prog.getNextPosition();

		if(getSelectedLine() > 0) {
			// Update the secondary point of a circular instruction
			MotionInstruction m = (MotionInstruction)activeInstruction();
			m.getSecondaryPoint().setPositionNum(reg);
			prog.setPosition(reg, pt);
		}
		else {
			MotionInstruction insert = new MotionInstruction(
					armModel.getCurCoordFrame() == CoordFrame.JOINT ? MTYPE_JOINT : MTYPE_LINEAR,
							getActive_instr(),
							false,
							(armModel.getCurCoordFrame() == CoordFrame.JOINT ? 50 : 50 * armModel.motorSpeed) / 100f,
							0,
							armModel.getActiveUserFrame(),
							armModel.getActiveToolFrame());

			prog.setPosition(getActive_instr(), pt);

			if(getActive_instr() != prog.getInstructions().size()) {
				// overwrite current instruction
				prog.overwriteInstruction(getActive_instr(), insert);

			} else {
				prog.addInstruction(insert);
			}
		}
	}

	public void newFrameInstruction(int fType) {
		Program p = activeProgram();
		FrameInstruction f = new FrameInstruction(fType, -1);

		if(getActive_instr() != p.getInstructions().size()) {
			p.overwriteInstruction(getActive_instr(), f);
		} else {
			p.addInstruction(f);
		}
	}

	public void newIOInstruction() {
		Program p = activeProgram();
		IOInstruction io = new IOInstruction(opt_select, Fields.OFF);

		if(getActive_instr() != p.getInstructions().size()) {
			p.overwriteInstruction(getActive_instr(), io);
		} else {
			p.addInstruction(io);
		}
	}

	public void newLabel() {
		Program p = activeProgram();

		LabelInstruction l = new LabelInstruction(-1);

		if(getActive_instr() != p.getInstructions().size()) {
			p.overwriteInstruction(getActive_instr(), l);
		} else {
			p.addInstruction(l);
		}
	}

	public void newJumpInstruction() {
		Program p = activeProgram();
		JumpInstruction j = new JumpInstruction(-1);

		if(getActive_instr() != p.getInstructions().size()) {
			p.overwriteInstruction(getActive_instr(), j);
		} else {
			p.addInstruction(j);
		}
	}


	public void newCallInstruction() {
		Program p = activeProgram();
		CallInstruction call = new CallInstruction();

		if(getActive_instr() != p.getInstructions().size()) {
			p.overwriteInstruction(getActive_instr(), call);
		} else {
			p.addInstruction(call);
		}
	}

	public void newIfStatement() {
		Program p = activeProgram();
		IfStatement stmt = new IfStatement(Operator.EQUAL, null);
		opEdit = stmt.getExpr();

		if(getActive_instr() != p.getInstructions().size()) {
			p.overwriteInstruction(getActive_instr(), stmt);
		} else {
			p.addInstruction(stmt);
		}
	}

	public void newIfExpression() {
		Program p = activeProgram();
		IfStatement stmt = new IfStatement();

		if(getActive_instr() != p.getInstructions().size()) {
			p.overwriteInstruction(getActive_instr(), stmt);
		} else {
			p.addInstruction(stmt);
		}
	}

	public void newSelectStatement() {
		Program p = activeProgram();
		SelectStatement stmt = new SelectStatement();

		if(getActive_instr() != p.getInstructions().size()) {
			p.overwriteInstruction(getActive_instr(), stmt);
		} else {
			p.addInstruction(stmt);
		}
	}

	public void newRegisterStatement(Register r) {
		Program p = activeProgram();
		RegisterStatement stmt = new RegisterStatement(r);

		if(getActive_instr() != p.getInstructions().size()) {
			p.overwriteInstruction(getActive_instr(), stmt);
		} else {
			p.addInstruction(stmt);
		}
	}

	public void newRegisterStatement(Register r, int i) {
		Program p = activeProgram();
		RegisterStatement stmt = new RegisterStatement(r, i);

		if(getActive_instr() != p.getInstructions().size()) {
			p.overwriteInstruction(getActive_instr(), stmt);
		} else {
			p.addInstruction(stmt);
		}
	}

	/**
	 * Updates the index display in the Active Frames menu based on the
	 * current value of workingText
	 */
	public void updateActiveFramesDisplay() {
		// Attempt to parse the inputted integer value
		try {
			int frameIdx = Integer.parseInt(workingText) - 1;

			if (frameIdx >= -1 && frameIdx < 10) {
				// Update the appropriate active Frame index
				if (getRow_select() == 0) {
					armModel.setActiveToolFrame(frameIdx);
				} else {
					armModel.setActiveUserFrame(frameIdx);
				}

				updateCoordFrame();
			}

		} catch(NumberFormatException NFEx) {
			// Non-integer value
		}
		// Update display
		if (getRow_select() == 0) {
			workingText = Integer.toString(armModel.getActiveToolFrame() + 1);
		} else {
			workingText = Integer.toString(armModel.getActiveUserFrame() + 1);
		}

		contents.get(getRow_select()).set(getCol_select(), workingText);
		updateScreen();
	}

	/**
	 * Loads the set of Frames that correspond to the given coordinate frame.
	 * Only TOOL and USER have Frames sets as of now.
	 * 
	 * @param coorFrame  the integer value representing the coordinate frame
	 *                   of the desired frame set
	 */
	public ArrayList<DisplayLine> loadFrames(CoordFrame coordFrame) {
		ArrayList<DisplayLine> frameDisplay = new ArrayList<DisplayLine>();
		
		if (coordFrame == CoordFrame.TOOL) {
			// Display Tool frames
			for(int idx = 0; idx < Fields.FRAME_SIZE; idx += 1) {
				// Display each frame on its own line
				String[] strArray = armModel.getToolFrame(idx).toLineStringArray();
				frameDisplay.add(newLine(idx, String.format("%-4s %s", String.format("%d)", idx + 1), strArray[0])));
				frameDisplay.add(newLine(idx, String.format("%s", strArray[1])));
				frameDisplay.get(idx*2 + 1).setxAlign(38);
			}
			
		} else {
			// Display User frames
			for(int idx = 0; idx < Fields.FRAME_SIZE; idx += 1) {
				// Display each frame on its own line
				String[] strArray = armModel.getUserFrame(idx).toLineStringArray();
				frameDisplay.add(newLine(idx, String.format("%-4s %s", String.format("%d)", idx + 1), strArray[0])));
				frameDisplay.add(newLine(idx, String.format("%s", strArray[1])));
				frameDisplay.get(idx*2 + 1).setxAlign(38);
			}
		}

		return frameDisplay;
	}

	public ArrayList<DisplayLine> loadMacros() {
		ArrayList<DisplayLine> macroDisplay = new ArrayList<DisplayLine>();

		for(int i = 0; i < macros.size(); i += 1) {
			String[] strArray = macros.get(i).toStringArray();
			macroDisplay.add(newLine(i, ""+(i+1), strArray[0], strArray[1], strArray[2]));  
		}

		return macroDisplay;
	}

	public ArrayList<DisplayLine> loadManualFunct() {
		ArrayList<DisplayLine> functionDisplay = new ArrayList<DisplayLine>();
		int macroNum = 0;

		for(int i = 0; i < macros.size(); i += 1) {
			if(macros.get(i).isManual()) {
				macroNum += 1;
				String manFunct = macros.get(i).toString();
				functionDisplay.add(newLine(i, macroNum + " " + manFunct));
			}
		}

		return functionDisplay;
	}

	/**
	 * Transitions to the Frame Details menu, which displays
	 * the x, y, z, w, p, r values associated with the Frame
	 * at curFrameIdx in either the Tool Frames or User Frames,
	 * based on the value of super_mode.
	 */
	public ArrayList<DisplayLine> loadFrameDetail(CoordFrame coordFrame) {
		ArrayList<DisplayLine> details = new ArrayList<DisplayLine>();

		// Display the frame set name as well as the index of the currently selected frame
		if(coordFrame == CoordFrame.TOOL) {
			String[] fields = armModel.getToolFrame(curFrameIdx).toLineStringArray();
			// Place each value in the frame on a separate lien
			for(String field : fields) { details.add(newLine(field)); }

		} else if(coordFrame == CoordFrame.USER) {
			String[] fields = armModel.getUserFrame(curFrameIdx).toLineStringArray();
			// Place each value in the frame on a separate lien
			for(String field : fields) { details.add(newLine(field)); }

		} else {
			return null;
		}

		return details;
	}

	/**
	 * Displays the points along with their respective titles for the
	 * current frame teach method (discluding the Direct Entry method).
	 */
	public ArrayList<String> loadPointList() {
		ArrayList<String> points = new ArrayList<String>();

		if(teachFrame != null) {

			ArrayList<String> temp = new ArrayList<String>();
			// Display TCP teach points
			if(mode == Screen.TEACH_3PT_TOOL || mode == Screen.TEACH_6PT) {
				temp.add("First Approach Point: ");
				temp.add("Second Approach Point: ");
				temp.add("Third Approach Point: ");
			}
			// Display Axes Vectors teach points
			if(mode == Screen.TEACH_3PT_USER || mode == Screen.TEACH_4PT || mode == Screen.TEACH_6PT) {
				temp.add("Orient Origin Point: ");
				temp.add("X Axis Point: ");
				temp.add("Y Axis Point: ");
			}
			// Display origin offset point
			if(mode == Screen.TEACH_4PT) {
				// Name of fourth point for the four point method?
				temp.add("Origin: ");
			}

			// Determine if the point has been set yet
			for(int idx = 0; idx < temp.size(); ++idx) {
				// Add each line to options
				points.add( temp.get(idx) + ((teachFrame.getPoint(idx) != null) ? "RECORDED" : "UNINIT") );
			}
		} else {
			// No teach points
			points.add("Error: teachFrame not set!");
		}

		return points;
	}

	/**
	 * Takes the values associated with the given Frame's direct entry values
	 * (X, Y, Z, W, P, R) and fills a 2D ArrayList, where the first column is
	 * the prefix for the value in the second column.
	 * 
	 * @param f        The frame to be displayed for editing
	 * @returning      A 2D ArrayList with the prefixes and values associated
	 *                 with the Frame
	 */
	public ArrayList<DisplayLine> loadFrameDirectEntry(Frame f) {
		ArrayList<DisplayLine> frame = new ArrayList<DisplayLine>();

		String[][] entries = f.directEntryStringArray();

		for (int line = 0; line < entries.length; ++line) {
			frame.add(newLine(line, entries[line][0], entries[line][1]));
		}

		return frame; 
	}

	/**
	 * This method attempts to modify the Frame based on the given value of method.
	 * If method is even, then the frame is taught via the 3-Point Method. Otherwise,
	 * the Frame is taught by either the 4-Point or 6-Point Method based on if the
	 * Frame is a Tool Frame or a User Frame.
	 * 
	 * @param frame    The frame to be taught
	 * @param method  The method by which to each the new Frame
	 */
	public void createFrame(Frame frame, int method) {
		if (teachFrame.setFrame(abs(method) % 2)) {
			if (DISPLAY_TEST_OUTPUT) { System.out.printf("Frame set: %d\n", curFrameIdx); }

			// Set new Frame
			if (frame instanceof ToolFrame) {
				// Update the current frame of the Robot Arm
				armModel.setActiveToolFrame(curFrameIdx);
				updateCoordFrame();

				saveFrameBytes( new File(sketchPath("tmp/frames.bin")) );
			} else {
				// Update the current frame of the Robot Arm
				armModel.setActiveUserFrame(curFrameIdx);
				updateCoordFrame();

				saveFrameBytes( new File(sketchPath("tmp/frames.bin")) );
			}

		} else {
			println("Invalid input points");
		}
	}

	/**
	 * This method takes the current values stored in contents (assuming that they corresond to
	 * the six direct entry values X, Y, Z, W, P, R), parses them, saves them to given Frame object,
	 * and sets the current Frame's values to the direct entry value, setting the current frame as
	 * the active frame in the process.
	 * 
	 * @param taughtFrame  the Frame, to which the direct entry values will be stored
	 */
	public void createFrameDirectEntry(Frame taughtFrame, float[] inputs) {
		// The user enters values with reference to the World Frame
		PVector origin, wpr;

		if (taughtFrame instanceof UserFrame) {
			origin = convertWorldToNative( new PVector(inputs[0], inputs[1], inputs[2]) );
		} else {
			// Tool frame origins are actually an offset of the Robot's EE position
			origin = new PVector(inputs[0], inputs[1], inputs[2]);
		}
		// Convert the angles from degrees to radians, then convert from World to Native frame
		wpr = (new PVector(-inputs[3], inputs[5], -inputs[4])).mult(DEG_TO_RAD);

		// Save direct entry values
		taughtFrame.setDEOrigin(origin);
		taughtFrame.setDEOrientationOffset(eulerToQuat(wpr));
		taughtFrame.setFrame(2);

		if(DISPLAY_TEST_OUTPUT) {
			wpr = quatToEuler(taughtFrame.getDEOrientationOffset()).mult(RAD_TO_DEG);
			System.out.printf("\n\n%s\n%s\nFrame set: %d\n", origin.toString(),
					wpr.toString(), curFrameIdx);
		}

		// Set New Frame
		if(taughtFrame instanceof ToolFrame) {
			// Update the current frame of the Robot Arm
			armModel.setActiveToolFrame(curFrameIdx);
		} else {
			// Update the current frame of the Robot Arm
			armModel.setActiveUserFrame(curFrameIdx);
		} 

		updateCoordFrame();
		saveFrameBytes( new File(sketchPath("tmp/frames.bin")) );
	}

	/**
	 * Displays the list of Registers in mode VIEW_REG or the Position Registers
	 * for modes VIEW_REG_J or VIEW_REG_C. In mode VIEW_REG_J the joint angles
	 * associated with the Point are displayed and the Cartesian values are
	 * displayed in mode VIEW_REG_C.
	 */
	public ArrayList<DisplayLine> loadRegisters() { 
		ArrayList<DisplayLine> regs = new ArrayList<DisplayLine>();

		// View Registers or Position Registers
		//int start = start_render;
		//int end = min(start + ITEMS_TO_SHOW, DREG.length);

		// Display a subset of the list of registers
		for(int idx = 0; idx < RegisterFile.REG_SIZE; ++idx) {
			String lbl;

			if(mode == Screen.NAV_DREGS) {
				lbl = (RegisterFile.getDReg(idx).getComment() == null) ? "" : RegisterFile.getDReg(idx).getComment();
			} else {
				lbl  = (RegisterFile.getPReg(idx).getComment() == null) ? "" : RegisterFile.getPReg(idx).getComment();
			}

			int buffer = 16 - lbl.length();
			while(buffer-- > 0) { lbl += " "; }

			String spaces;

			if(idx < 9) {
				spaces = "  ";
			} else if(idx < 99) {
				spaces = " ";
			} else {
				spaces = "";
			}

			// Display the comment asscoiated with a specific Register entry
			String regLbl = String.format("%s[%d:%s%s]", (mode == Screen.NAV_DREGS) ? "R" : "PR", (idx + 1), spaces, lbl);
			// Display Register value (* ifuninitialized)
			String regEntry = "*";

			if(mode == Screen.NAV_DREGS) {
				if((RegisterFile.getDReg(idx)).value != null) {
					// Dispaly Register value
					regEntry = String.format("%4.3f", (RegisterFile.getDReg(idx)).value);
				}

			} else if((RegisterFile.getPReg(idx)).point != null) {
				regEntry = "...Edit...";
			}

			regs.add(newLine(idx, regLbl, regEntry));
		}

		return regs;
	}

	/**
	 * This method will transition to the INPUT_POINT_C or INPUT_POINT_J modes
	 * based whether the current mode is VIEW_REG_C or VIEW_REG_J. In either
	 * mode, the user will be prompted to input 6 floating-point values (X, Y,
	 * Z, W, P, R or J1 - J6 for INPUT_POINT_C or INPUT_POINT_J respectively).
	 * The input method is similar to inputting the value in DIRECT_ENTRY mode.
	 */
	public ArrayList<DisplayLine> loadPosRegEntry(PositionRegister reg) {
		ArrayList<DisplayLine> register = new ArrayList<DisplayLine>();

		if(reg.point == null) {
			// Initialize values to zero if the entry is null
			if(mode == Screen.EDIT_PREG_C) {
				register.add(newLine(0, "X: ",  ""));
				register.add(newLine(1, "Y: ",  ""));
				register.add(newLine(2, "Z: ",  ""));
				register.add(newLine(3, "W: ",  ""));
				register.add(newLine(4, "P: ",  ""));
				register.add(newLine(5, "R: ",  ""));

			} else if(mode == Screen.EDIT_PREG_J) {
				for(int idx = 0; idx < 6; idx += 1) {
					register.add(newLine(idx, String.format("J%d: ", idx), ""));
				}
			}
		} else {

			// List current entry values if the Register is initialized
			String[][] entries;

			if (mode == Screen.EDIT_PREG_J) {
				// List joint angles
				entries = reg.point.toJointStringArray();
			} else {
				// Display Cartesian values
				entries = reg.point.toCartesianStringArray();
			}

			for(int idx = 0; idx < entries.length; ++idx) {
				register.add(newLine(idx, entries[idx][0], entries[idx][1]));
			}
		}

		return register;
	}

	public ArrayList<String> loadIORegisters() {
		ArrayList<String> ioRegs = new ArrayList<String>();

		for(int i = 0; i < RegisterFile.IO_REG_SIZE; i += 1){
			String state = ((RegisterFile.getIOReg(i)).state == Fields.ON) ? "ON" : "OFF";
			String ee;

			if ((RegisterFile.getIOReg(i)).name != null) {
				ee = (RegisterFile.getIOReg(i)).name;
			} else {
				ee = "";
			}

			ioRegs.add( String.format("IO[%d:%-8s] = %s", i + 1, ee, state) );
		}

		return ioRegs;
	}

	public void createRegisterPoint(boolean fromJointAngles) {
		// Obtain point inputs from UI display text
		float[] inputs = new float[6];
		try {
			for(int idx = 0; idx < inputs.length; ++idx) {
				String inputStr = contents.get(idx).get(getCol_select());
				inputs[idx] = Float.parseFloat(inputStr);
				// Bring the input values with the range [-9999, 9999]
				inputs[idx] = max(-9999f, min(inputs[idx], 9999f));
			}
		} catch (NumberFormatException NFEx) {
			// Invalid input
			println("Values must be real numbers!");
			return;
		}

		if(fromJointAngles) {
			// Bring angles within range: (0, TWO_PI)
			for(int idx = 0; idx < inputs.length; ++idx) {
				inputs[idx] = mod2PI(inputs[idx] * DEG_TO_RAD);
			}

			(RegisterFile.getPReg(active_index)).point = nativeRobotEEPoint(inputs);
		} else {
			PVector position = convertWorldToNative( new PVector(inputs[0], inputs[1], inputs[2]) );
			// Convert the angles from degrees to radians, then convert from World to Native frame, and finally convert to a quaternion
			RQuaternion orientation = eulerToQuat( (new PVector(-inputs[3], inputs[5], -inputs[4]).mult(DEG_TO_RAD)) );

			// Use default the Robot's joint angles for computing inverse kinematics
			float[] jointAngles = inverseKinematics(new float[] {0f, 0f, 0f, 0f, 0f, 0f}, position, orientation);
			(RegisterFile.getPReg(active_index)).point = new Point(position, orientation, jointAngles);
		}

		(RegisterFile.getPReg(active_index)).isCartesian = !fromJointAngles;
		saveRegisterBytes( new File(sketchPath("tmp/registers.bin")) );
	}

	/**
	 * This method loads text to screen in sfuch a way as to allow the user
	 * to input an arbitrary character string consisting of letters (a-z
	 * upper and lower case) and/ or special characters (_, @, *, .) via
	 * the function row, as well as numbers via the number pad. Strings are
	 * limited to 16 characters and can be used to name new routines, as well
	 * as set remark fields for frames and instructions.
	 */
	public ArrayList<DisplayLine> loadTextInput() {
		ArrayList<DisplayLine> remark = new ArrayList<DisplayLine>();

		remark.add(newLine("\0"));

		DisplayLine line = new DisplayLine();
		// Give each letter in the name a separate column
		for(int idx = 0; idx < workingText.length() && idx < TEXT_ENTRY_LEN; idx += 1) {
			line.add( Character.toString(workingText.charAt(idx)) );
		}

		remark.add(line);

		return remark;
	}

	/**
	 * Given a set of Strings this method returns a single
	 * String ArrayList, which contains all the given elements
	 * in the order that they are given as arguments.
	 * 
	 * @param columns  A list of Strings
	 * @return         An ArrayList containing all the given
	 *                 Strings
	 */
	public DisplayLine newLine(String... columns) {
		DisplayLine line =  new DisplayLine();

		for(String col : columns) {
			line.add(col);
		}

		return line;
	}

	public DisplayLine newLine(int itemIdx, String... columns) {
		DisplayLine line =  new DisplayLine(itemIdx);

		for(String col : columns) {
			line.add(col);
		}

		return line;
	}

	/**
	 * 
	 */
	public int moveUp(boolean page) {
		if (page && getStart_render() > 0) {
			// Move display frame up an entire screen's display length
			setRow_select(max(0, getRow_select() - (ITEMS_TO_SHOW - 1)));
			setStart_render(max(0, getStart_render() - (ITEMS_TO_SHOW - 1)));
		} 
		else {
			// Move up a single row
			setRow_select(max(0, getRow_select() - 1));
		}

		return contents.get(getRow_select()).getItemIdx();
	}

	public int moveUpInstr(boolean page) {
		if (page && getStart_render() > 0) {
			// Move display frame up an entire screen's display length
			setRow_select(max(0, getRow_select() - (ITEMS_TO_SHOW - 1)));
			setStart_render(max(0, getStart_render() - (ITEMS_TO_SHOW - 1)));
		} 
		else {
			if(getSelectedIdx() == 0 && getActive_instr() > 0) {
				// Move up a single instruction
				while(getRow_select() > 0 && getActive_instr() - 1 == contents.get(getRow_select() - 1).getItemIdx()) {
					setRow_select(max(0, getRow_select() - 1));
				}
			} else {
				// Move up a single row
				setRow_select(max(0, getRow_select() - 1));
			}
		}

		return contents.get(getRow_select()).getItemIdx();  
	}

	/**
	 * 
	 */
	public int moveDown(boolean page) {
		int size = contents.size();  

		if (page && size > (getStart_render() + ITEMS_TO_SHOW)) {
			// Move display frame down an entire screen's display length
			setRow_select(min(size - 1, getRow_select() + (ITEMS_TO_SHOW - 1)));
			setStart_render(max(0, min(size - ITEMS_TO_SHOW, getStart_render() + (ITEMS_TO_SHOW - 1))));
		} else {
			// Move down a single row
			setRow_select(min(size - 1, getRow_select() + 1));
		}

		return contents.get(getRow_select()).getItemIdx();
	}

	public int moveDownInstr(boolean page) {
		int size = contents.size();  

		if (page && size > (getStart_render() + ITEMS_TO_SHOW)) {
			// Move display frame down an entire screen's display length
			setRow_select(min(size - 1, getRow_select() + (ITEMS_TO_SHOW - 1)));
			setStart_render(max(0, min(size - ITEMS_TO_SHOW, getStart_render() + (ITEMS_TO_SHOW - 1))));
		} else {
			int lenMod = 0;
			if(mode.getType() == ScreenType.TYPE_LINE_SELECT) lenMod = 1;
			if(getSelectedIdx() == 0 && getActive_instr() < activeProgram().size() - lenMod) {
				// Move down a single instruction
				while(getActive_instr() == contents.get(getRow_select()).getItemIdx()) {
					setRow_select(min(size - 1, getRow_select() + 1));
				}
			} else {
				// Move down a single row
				setRow_select(min(size - 1, getRow_select() + 1));
			}
		}

		return contents.get(getRow_select()).getItemIdx();
	}

	public void moveLeft() {
		if(getRow_select() > 0 && contents.get(getRow_select() - 1).getItemIdx() == contents.get(getRow_select()).getItemIdx()) {
			setCol_select(getCol_select() - 1);
			if(getCol_select() < 0) {
				moveUp(false);
				setCol_select(contents.get(getRow_select()).size() - 1);
			}
		} else {
			setCol_select(max(0, getCol_select() - 1));
		}
	}

	public void moveRight() {
		if(getRow_select() < contents.size() - 1 && contents.get(getRow_select() + 1).getItemIdx() == contents.get(getRow_select()).getItemIdx()) {
			setCol_select(getCol_select() + 1);
			if(getCol_select() > contents.get(getRow_select()).size() - 1) {
				moveDown(false);
				setCol_select(0);
			}
		} else {
			setCol_select(min(contents.get(getRow_select()).size() - 1, getCol_select() + 1));
		}
	}

	/**
	 * Returns the first line in the current list of contents that the instruction 
	 * matching the given index appears on.
	 */
	public int getInstrLine(int instrIdx) {
		ArrayList<DisplayLine> instr = loadInstructions(getActive_prog());
		int row = instrIdx;

		while(instr.get(row).getItemIdx() != instrIdx) {
			row += 1;
			if(getRow_select() >= contents.size() - 1) break;
		}

		return row;
	}

	public int getSelectedLine() {
		int row = 0;
		DisplayLine currRow = contents.get(getRow_select());
		while(getRow_select() - row >= 0 && currRow.getItemIdx() == contents.get(getRow_select() - row).getItemIdx()) {
			row += 1;
		}

		return row - 1;
	}

	public int getSelectedIdx() {
		if(mode.getType() == ScreenType.TYPE_LINE_SELECT) return 0;

		int idx = getCol_select();
		for(int i = getRow_select() - 1; i >= 0; i -= 1) {
			if(contents.get(i).getItemIdx() != contents.get(i + 1).getItemIdx()) break;
			idx += contents.get(i).size();
		}

		return idx;
	}

	public final int MTYPE_JOINT = 0;
	public final int MTYPE_LINEAR = 1;
	public final int MTYPE_CIRCULAR = 2;
	public final int FTYPE_TOOL = 0;
	public final int FTYPE_USER = 1;
	//stack containing the previously running program state when a new program is called
	private Stack<int[]> call_stack = new Stack<int[]>();
	// Indicates whether a program is currently running
	private boolean programRunning = false;

	/**
	 * Returns the currently active program or null if no program is active
	 */
	public Program activeProgram() {
		if (getActive_prog() < 0 || getActive_prog() >= armModel.numOfPrograms()) {
			//System.out.printf("Not a valid program index: %d!\n", active_prog);
			return null;
		}

		return armModel.getProgram(getActive_prog());
	}

	/**
	 * Returns the instruction that is currently active in the currently active program.
	 * 
	 * @returning  The active instruction of the active program or null if no instruction
	 *             is active
	 */
	public Instruction activeInstruction() {
		Program activeProg = activeProgram();

		if (activeProg == null || getActive_instr() < 0 || getActive_instr() >= activeProg.getInstructions().size()) {
			//System.out.printf("Not a valid instruction index: %d!\n", active_instr);   
			return null;

		}

		return activeProg.getInstruction(getActive_instr());
	}

	/**
	 * Returns the active instructiob of the active program, if
	 * that instruction is a motion instruction.
	 */
	public MotionInstruction activeMotionInst() {
		Instruction inst = activeInstruction();

		if(inst instanceof MotionInstruction) {
			return (MotionInstruction)inst;
		}

		return null;
	}

	/**
	 * This method saves all programs, frames, and initialized registers,
	 * each to separate files
	 */
	public void saveState() {
		saveProgramBytes( new File(sketchPath("tmp/programs.bin")) );
		saveFrameBytes( new File(sketchPath("tmp/frames.bin")) );
		saveRegisterBytes( new File(sketchPath("tmp/registers.bin")) );
		saveScenarioBytes( new File(sketchPath("tmp/scenarios.bin")) );
	}

	/**
	 * Load program, frames, and registers from their respective
	 * binary files.
	 *
	 * @return an integer error code
	 */
	public int loadState() {
		int retCode = 0;
		
		File f = new File(sketchPath("tmp/"));
		if(!f.exists()) { f.mkdirs(); }

		/* Load and Initialize the Tool and User Frames */
		// TODO add frames to the current Robot
		File savedFrames = new File( sketchPath("tmp/frames.bin") );

		if(savedFrames.exists() && loadFrameBytes(savedFrames) == 0) {
			println("Successfully loaded frames!");
		} else {
			println("Failed to load frames ...");
			retCode += 1;
		}

		/* Load and Initialize the Position Register and Registers */
		RegisterFile.initRegisterFile();
		File savedRegs = new File(sketchPath("tmp/registers.bin"));

		if(savedRegs.exists() && loadRegisterBytes(savedRegs) == 0) {
			println("Successfully loaded registers!");
		} else {
			println("Failed to load registers ...");
			retCode += 2;
		}

		File scenarioFile = new File(sketchPath("tmp/scenarios.bin"));

		if(scenarioFile.exists() && loadScenarioBytes(scenarioFile) == 0) {
			println("Successfully loaded scenarios!");
		} else {
			println("Failed to load scenarios ...");
			retCode += 4;
		}

		/* Load all saved Programs */

		File progFile = new File( sketchPath("tmp/programs.bin") );

		if(progFile.exists() && loadProgramBytes(progFile) == 0) {
			println("Successfully loaded programs!");
		} else {
			println("Failed to load programs ...");
			retCode += 8;
		}
		
		return retCode;
	}

	/**
	 * Saves all the Programs currently in ArrayList programs to the
	 * given file.
	 * 
	 * @param dest  where to save all the programs
	 * @return      0 if the save was successful,
	 *              1 if dest could not be created or found,
	 *              2 if an error occurs when saving the Programs
	 */
	private int saveProgramBytes(File dest) {

		try {
			// Create dest if it does not already exist
			if(!dest.exists()) {      
				try {
					dest.createNewFile();
					System.out.printf("Successfully created %s.\n", dest.getName());
				} catch (IOException IOEx) {
					System.out.printf("Could not create %s ...\n", dest.getName());
					IOEx.printStackTrace();
					return 1;
				}
			} 

			FileOutputStream out = new FileOutputStream(dest);
			DataOutputStream dataOut = new DataOutputStream(out);
			// Save the number of programs
			dataOut.writeInt(armModel.numOfPrograms());

			for(int idx = 0; idx < armModel.numOfPrograms(); ++idx) {
				// Save each program
				saveProgram(armModel.getProgram(idx), dataOut);
			}

			dataOut.close();
			out.close();
			return 0;

		} catch (FileNotFoundException FNFEx) {
			// Could not locate dest
			System.out.printf("%s does not exist!\n", dest.getName());
			FNFEx.printStackTrace();
			return 1;

		} catch (IOException IOEx) {
			// An error occrued with writing to dest
			System.out.printf("%s is corrupt!\n", dest.getName());
			IOEx.printStackTrace();
			return 2;
		}
	}

	/**
	 * Loads all Programs stored in the given file. This method expects that the number of
	 * programs to be stored is stored at the immediate beginning of the file as an integer.
	 * Though, no more then 200 programs will be loaded.
	 * 
	 * @param src  The file from which to load the progarms
	 * @return     0 if the load was successful,
	 *             1 if src could not be found,
	 *             2 if an error occured while reading the programs,
	 *             3 if the end of the file is reached before all the expected programs are
	 *               read
	 */
	private int loadProgramBytes(File src) {

		try {
			FileInputStream in = new FileInputStream(src);
			DataInputStream dataIn = new DataInputStream(in);
			// Read the number of programs stored in src
			int size = max(0, min(dataIn.readInt(), 200));

			while(size-- > 0) {
				// Read each program from src
				armModel.addProgram( loadProgram(dataIn) );
			}

			dataIn.close();
			in.close();
			return 0;

		} catch (FileNotFoundException FNFEx) {
			// Could not locate src
			System.out.printf("%s does not exist!\n", src.getName());
			FNFEx.printStackTrace();
			return 1;

		} catch (EOFException EOFEx) {
			// Reached the end of src unexpectedly
			System.out.printf("End of file, %s, was reached unexpectedly!\n", src.getName());
			EOFEx.printStackTrace();
			return 3;

		} catch (IOException IOEx) {
			// An error occured with reading from src
			System.out.printf("%s is corrupt!\n", src.getName());
			IOEx.printStackTrace();
			return 2;
		}
	}

	/**
	 * Saves the data associated with the given Program to the given output stream.
	 * Not all the Points in a programs Point array are stored: only the Points
	 * associated with a MotionInstruction are saved after the MotionInstruction,
	 * to which it belongs.
	 * 
	 * @param  p            The program to save
	 * @param  out          The output stream to which to save the Program
	 * @throws IOException  If an error occurs with saving the Program
	 */
	private void saveProgram(Program p, DataOutputStream out) throws IOException {

		if (p == null) {
			// Indicates a null value is saved
			out.writeByte(0);

		} else {
			// Indicates a non-null value is saved
			out.writeByte(1);

			out.writeUTF(p.getName());

			for (int pdx = 0; pdx < 1000; ++pdx) {
				if (p.getPosition(pdx) != null) {
					// Save the position with its respective index
					out.writeInt(pdx);
					savePoint(p.getPosition(pdx), out);
				}
			}

			// End of saved positions
			out.writeInt(-1);

			out.writeInt(p.getInstructions().size());
			// Save each instruction
			for(Instruction inst : p.getInstructions()) {
				saveInstruction(inst, out);
			}
		}
	}

	/**
	 * Creates a program from data in the given input stream. A maximum of
	 * 500 instructions will be read for a single program
	 * 
	 * @param in            The input stream to read from
	 * @return              A program created from data in the input stream,
	 *                      or null
	 * @throws IOException  If an error occurs with reading from the input
	 *                      stream
	 */
	private Program loadProgram(DataInputStream in) throws IOException {
		// Read flag byte
		byte flag = in.readByte();

		if (flag == 0) {
			return null;

		} else {
			// Read program name
			String name = in.readUTF();
			Program prog = new Program(name);
			int nReg;

			// Read in all the positions saved for the program
			do {
				nReg = in.readInt();

				if (nReg == -1) {
					break;  
				}

				// Load the saved point
				Point pt = loadPoint(in);
				prog.setPosition(nReg, pt);

			} while (true);

			// Read the number of instructions stored for this porgram
			int numOfInst = max(0, min(in.readInt(), 500));

			while(numOfInst-- > 0) {
				// Read each instruction
				Instruction inst = loadInstruction(in);
				prog.addInstruction(inst);
			}

			return prog;
		}
	}

	/**
	 * Saves the data associated with the given Point object to the file opened
	 * by the given output stream. Null Points are saved a single zero byte.
	 * 
	 * @param   p            The Point of which to save the data
	 * @param   out          The output stream used to save the Point
	 * @throws  IOException  If an error occurs with writing the data of the Point
	 */
	private void savePoint(Point p, DataOutputStream out) throws IOException {

		if (p == null) {
			// Indicate a null value is saved
			out.writeByte(0);

		} else {
			// Indicate a non-null value is saved
			out.writeByte(1);
			// Write position of the point
			savePVector(p.position, out);
			// Write point's orientation
			saveRQuaternion(p.orientation, out);
			// Write the joint angles for the point's position
			saveFloatArray(p.angles, out);
		}
	}

	/**
	 * Loads the data of a Point from the file opened by the given
	 * input stream. It is possible that null will be returned by
	 * this method if a null Point was saved.
	 *
	 * @param  in           The input stream used to read the data of
	 *                      a Point
	 * @return              The Point stored at the current position
	 *                      of the input stream
	 * @throws IOException  If an error occurs with reading the data
	 *                      of the Point
	 */
	private Point loadPoint(DataInputStream in) throws IOException {
		// Read flag byte
		byte val = in.readByte();

		if (val == 0) {
			return null;

		} else {
			// Read the point's position
			PVector position = loadPVector(in);
			// Read the point's orientation
			RQuaternion orientation = loadRQuaternion(in);
			// Read the joint angles for the joint's position
			float[] angles = loadFloatArray(in);

			return new Point(position, orientation, angles);
		}
	}

	/**
	 * Saves the data stored in the given instruction to the file opened by the give output
	 * stream. Currently, this method will only work for instructions of type: Motion, Frame
	 * and Tool.
	 * 
	 * @param inst          The instruction of which to save the data
	 * @pararm out          The output stream used to save the given instruction
	 * @throws IOException  If an error occurs with saving the instruction
	 */
	private void saveInstruction(Instruction inst, DataOutputStream out) throws IOException {

		// Each Instruction subclass MUST have its own saving code block associated with its unique data fields
		if (inst instanceof MotionInstruction) {
			MotionInstruction m_inst = (MotionInstruction)inst;
			// Flag byte denoting this instruction as a MotionInstruction
			out.writeByte(2);
			// Write data associated with the MotionIntruction object
			out.writeBoolean(m_inst.isCommented());
			out.writeInt(m_inst.getMotionType());
			out.writeInt(m_inst.getPositionNum());
			out.writeBoolean(m_inst.usesGPosReg());
			out.writeFloat(m_inst.getSpeed());
			out.writeInt(m_inst.getTermination());
			out.writeInt(m_inst.getUserFrame());
			out.writeInt(m_inst.getToolFrame());

			MotionInstruction subInst = m_inst.getSecondaryPoint();

			if (subInst != null) {
				// Save secondary point for circular instructions
				out.writeByte(1);
				saveInstruction(subInst, out);

			} else {
				// No secondary point
				out.writeByte(0);
			}

		} else if(inst instanceof FrameInstruction) {
			FrameInstruction f_inst = (FrameInstruction)inst;
			// Flag byte denoting this instruction as a FrameInstruction
			out.writeByte(3);
			// Write data associated with the FrameInstruction object
			out.writeBoolean(f_inst.isCommented());
			out.writeInt(f_inst.getFrameType());
			out.writeInt(f_inst.getFrameIdx());

		} else if(inst instanceof IOInstruction) {
			IOInstruction t_inst = (IOInstruction)inst;
			// Flag byte denoting this instruction as a ToolInstruction
			out.writeByte(4);
			// Write data associated with the ToolInstruction object
			out.writeBoolean(t_inst.isCommented());
			out.writeInt(t_inst.getReg());
			out.writeInt( saveint(t_inst.getState()) );

		} else if(inst instanceof LabelInstruction) {
			LabelInstruction l_inst = (LabelInstruction)inst;

			out.writeByte(5);
			out.writeBoolean(l_inst.isCommented());
			out.writeInt(l_inst.getLabelNum());

		} else if(inst instanceof JumpInstruction) {
			JumpInstruction j_inst = (JumpInstruction)inst;

			out.writeByte(6);
			out.writeBoolean(j_inst.isCommented());
			out.writeInt(j_inst.getTgtLblNum());

		} else if (inst instanceof CallInstruction) {
			CallInstruction c_inst = (CallInstruction)inst;

			out.writeByte(7);
			out.writeBoolean(c_inst.isCommented());
			out.writeInt(c_inst.getProgIdx());

		} else if (inst instanceof RegisterStatement) {
			RegisterStatement rs = (RegisterStatement)inst;
			Register r = rs.getReg();

			out.writeByte(8);
			out.writeBoolean(rs.isCommented());

			// In what type of register will the result of the statement be placed?
			int regType;

			if (r instanceof IORegister) {
				regType = 2;

			} else if (r instanceof PositionRegister) {
				regType = 1;

			} else {
				regType = 0;
			}

			out.writeInt(regType);
			out.writeInt(r.getIdx());
			out.writeInt(rs.getPosIdx());

			saveExpression(rs.getExpr(), out);

		}/* Add other instructions here! */
		else if (inst instanceof Instruction) {
			/// A blank instruction
			out.writeByte(1);
			out.writeBoolean(inst.isCommented());

		} else {
			// Indicate a null-value is saved
			out.writeByte(0);
		}


	}

	/**
	 * The next instruction stored in the file opened by the given input stream
	 * is read, created, and returned. This method is currently only functional
	 * for instructions of type: Motion, Frame, and Tool.
	 *
	 * @param in            The input stream from which to read the data of an
	 *                      instruction
	 * @return              The instruction saved at the current position of the
	 *                      input stream
	 * @throws IOException  If an error occurs with reading the data of the
	 *                      instruciton
	 */
	private Instruction loadInstruction(DataInputStream in) throws IOException {
		Instruction inst = null;
		// Read flag byte
		byte instType = in.readByte();

		if(instType == 2) {
			// Read data for a MotionInstruction object
			boolean isCommented = in.readBoolean();
			int mType = in.readInt();
			int reg = in.readInt();
			boolean isGlobal = in.readBoolean();
			float spd = in.readFloat();
			int term = in.readInt();
			int uFrame = in.readInt();
			int tFrame = in.readInt();

			inst = new MotionInstruction(mType, reg, isGlobal, spd, term, uFrame, tFrame);
			inst.setIsCommented(isCommented);

			byte flag = in.readByte();

			if (flag == 1) {
				// Load the second point associated with a circular type motion instruction
				((MotionInstruction)inst).setSecondaryPoint((MotionInstruction)loadInstruction(in));
			}

		} else if(instType == 3) {
			// Read data for a FrameInstruction object
			boolean isCommented = in.readBoolean();
			inst = new FrameInstruction( in.readInt(), in.readInt() );
			inst.setIsCommented(isCommented);

		} else if(instType == 4) {
			// Read data for a ToolInstruction object
			boolean isCommented = in.readBoolean();
			int reg = in.readInt();
			int setting = in.readInt();

			inst = new IOInstruction(reg, loadint(setting));
			inst.setIsCommented(isCommented);

		} else if (instType == 5) {
			boolean isCommented = in.readBoolean();
			int labelNum = in.readInt();

			inst = new LabelInstruction(labelNum);
			inst.setIsCommented(isCommented);

		} else if (instType == 6) {
			boolean isCommented = in.readBoolean();
			int tgtLabelNum = in.readInt();

			inst = new JumpInstruction(tgtLabelNum);
			inst.setIsCommented(isCommented);

		} else if (instType == 7) {
			boolean isCommented = in.readBoolean();
			int pdx = in.readInt();

			inst = new CallInstruction(pdx);
			inst.setIsCommented(isCommented);

		} else if (instType == 8) {
			boolean isCommented = in.readBoolean();
			int regType = in.readInt();
			int regIdx = in.readInt();
			int posIdx = in.readInt();
			Expression expr = loadExpression(in);

			if (regType == 2) {
				inst = new RegisterStatement(RegisterFile.getIOReg(regIdx), expr);
				inst.setIsCommented(isCommented);

			} else if (regType == 1) {
				inst = new RegisterStatement(RegisterFile.getPReg(regIdx), posIdx, expr);
				inst.setIsCommented(isCommented);

			} else if (regType == 0) {
				inst = new RegisterStatement(RegisterFile.getDReg(regIdx), expr);
				inst.setIsCommented(isCommented);

			}

		}/* Add other instructions here! */
		else if (instType == 1) {
			inst = new Instruction();
			boolean isCommented = in.readBoolean();
			inst.setIsCommented(isCommented);

		} else {
			return null;
		}

		return inst;
	}

	/**
	 * Convert the given End Effector status
	 * to a unique integer value.
	 */
	private int saveint(int stat) {
		switch (stat) {
		case Fields.ON:  return 0;
		case Fields.OFF: return 1;
		default:  return -1;
		}
	}

	/**
	 * Converts a valid integer value to its
	 * corresponding End Effector Status.
	 */
	private int loadint(int val) {
		switch (val) {
		case 0:   return Fields.ON;
		case 1:   return Fields.OFF;
		default:  return -1;
		}
	}

	/**
	 * Given a valid file path, both the Tool Frame and then the User
	 * Frame sets are saved to the file. First the length of a list
	 * is saved and then its respective elements.
	 *
	 * @param dest  the file to which the frame sets will be saved
	 * @return      0 if successful,
	 *              1 if dest could not be created or found
	 *              2 if an error occurs with writing to the file
	 */
	private int saveFrameBytes(File dest) {

		try {
			// Create dest if it does not already exist
			if(!dest.exists()) {
				try {
					dest.createNewFile();
					System.out.printf("Successfully created %s.\n", dest.getName());
				} catch (IOException IOEx) {
					System.out.printf("Could not create %s ...\n", dest.getName());
					IOEx.printStackTrace();
				}
			}

			FileOutputStream out = new FileOutputStream(dest);
			DataOutputStream dataOut = new DataOutputStream(out);

			// Save Tool Frames
			dataOut.writeInt(Fields.FRAME_SIZE);
			for (int idx = 0; idx < Fields.FRAME_SIZE; ++idx) {
				saveFrame(armModel.getToolFrame(idx), dataOut);
			}
			
			// Save User Frames
			dataOut.writeInt(Fields.FRAME_SIZE);
			for (int idx = 0; idx < Fields.FRAME_SIZE; ++idx) {
				saveFrame(armModel.getUserFrame(idx), dataOut);
			}

			dataOut.close();
			out.close();
			return 0;

		} catch (FileNotFoundException FNFEx) {
			// Could not find dest
			System.out.printf("%s does not exist!\n", dest.getName());
			FNFEx.printStackTrace();
			return 1;

		} catch (IOException IOEx) {
			// Error with writing to dest
			System.out.printf("%s is corrupt!\n", dest.getName());
			IOEx.printStackTrace();
			return 2;
		}
	}

	/**
	 * Loads both the Tool and User Frames from the file path denoted
	 * by the given String. The Tool Frames are expected to come before
	 * the Usser Frames. In addition, it is expected that both frame
	 * sets store the length of the set before the first element.
	 * 
	 * @param src  the file, which contains the data for the Tool and
	 *             User Frames
	 * @return     0 if successful,
	 *             1 if an error occurs with accessing the give file
	 *             2 if an error occurs with reading from the file
	 *             3 if the end of the file is reached before reading
	 *             all the data for the frames
	 */
	private int loadFrameBytes(File src) {
		int idx = -1;

		try {
			FileInputStream in = new FileInputStream(src);
			DataInputStream dataIn = new DataInputStream(in);

			// Load Tool Frames
			int size = max(0, min(dataIn.readInt(), 10));
			
			for(idx = 0; idx < size; idx += 1) {
				loadFrame(armModel.getToolFrame(idx), dataIn);
			}

			// Load User Frames
			size = max(0, min(dataIn.readInt(), 10));

			for(idx = 0; idx < size; idx += 1) {
				loadFrame(armModel.getUserFrame(idx), dataIn);
			}

			dataIn.close();
			in.close();
			return 0;

		} catch (FileNotFoundException FNFEx) {
			// Could not find src
			System.out.printf("%s does not exist!\n", src.getName());
			FNFEx.printStackTrace();
			return 1;

		} catch (EOFException EOFEx) {
			// Reached the end of src unexpectedly
			System.out.printf("End of file, %s, was reached unexpectedly!\n", src.getName());
			EOFEx.printStackTrace();
			return 3;

		} catch (IOException IOEx) {
			// Error with reading from src
			System.out.printf("%s is corrupt!\n", src.getName());
			IOEx.printStackTrace();
			return 2;
		}
	}

	/**
	 * Saves the data of the given frame's origin, orientation and axes vectors
	 * to the file opened by the given DataOutputStream.
	 * 
	 * @param f    A non-null frame object
	 * @param out  An output stream used to write the given frame to a file
	 * @throw IOException  if an error occurs with writing the frame to the file
	 */
	private void saveFrame(Frame f, DataOutputStream out) throws IOException {

		// Save a flag to indicate what kind of frame was saved
		if (f == null) {
			out.writeByte(0);
			return;

		} else if (f instanceof ToolFrame) {
			out.writeByte(1);

		} else if (f instanceof UserFrame) {
			out.writeByte(2);

		} else {
			throw new IOException("Invalid Frame!");
		}
		
		int len;

		if (f instanceof UserFrame) {
			// Write User frame origin
			savePVector(f.getOrigin(), out);
			len = 3;

		} else {
			// Write Tool frame TCP offset
			savePVector( ((ToolFrame)f).getTCPOffset(), out );
			len = 6;
			
		}

		// Write frame axes
		saveRQuaternion(f.getOrientation(), out);
		
		// Write frame orientation (and tooltip teach points for tool frames) points
		for (int idx = 0; idx < len; ++idx) {
			savePoint(f.getPoint(idx), out);
		}

		// Write frame manual entry origin value
		savePVector(f.getDEOrigin(), out);
		// Write frame manual entry origin value
		saveRQuaternion(f.getDEOrientationOffset(), out);

		if (f instanceof UserFrame) {
			// Save point for the origin offset of the frame
			savePoint( ((UserFrame)f).getOrientOrigin(), out );
		}
	}

	/**
	 * Loads the data associated with a Frame object (origin,
	 * orientation and axes vectors) from the file opened by
	 * the given DataOutputStream.
	 *
	 * @param ref	The frame, in which to save the data
	 * @param in	An input stream used to read from a file
	 * @return		The next frame stored in the file
	 * @throw IOException  if an error occurs while reading the frame
	 *                     from to the file
	 */
	private void loadFrame(Frame ref, DataInputStream in) throws IOException {
		byte type = in.readByte();

		if ((ref instanceof ToolFrame && type != 1) ||
			(ref instanceof UserFrame && type != 2)) {
			// Types do not match
			throw new IOException("Invalid Frame type!");
		}

		PVector v = loadPVector(in);
		int len;
		
		if (ref instanceof UserFrame) {
			// Read origin value
			((UserFrame)ref).setOrigin(v);
			len = 3;
			
		} else {
			// Read TCP offset values
			((ToolFrame)ref).setTCPOffset(v);
			len = 6;
		}

		// Read axes quaternion values
		ref.setOrientation( loadRQuaternion(in) );
		//System.out.printf("%s\n", ref.getOrientation());
		
		// Read in orientation points (and tooltip teach points for tool frames)
		for (int idx = 0; idx < len; ++idx) {
			ref.setPoint(loadPoint(in), idx);
		}

		// Read manual entry origin values
		ref.setDEOrigin(loadPVector(in));
		ref.setDEOrientationOffset(loadRQuaternion(in));

		if (ref instanceof UserFrame) {
			// Load point for the origin offset of the frame
			((UserFrame)ref).setOrientOrigin(loadPoint(in));
		}
	}

	/**
	 * Saves all initialized Register and Position Register Entries with their
	 * respective indices in their respective lists to dest. In addition, the
	 * number of Registers and Position Registers saved is saved to the file
	 * before each respective set of entries.
	 * 
	 * @param dest  Some binary file to which to save the Register entries
	 * @return      0 if the save was successful,
	 *              1 if dest could not be found pr created
	 *              2 if an error occrued while writing to dest
	 */
	private int saveRegisterBytes(File dest) {

		try {

			// Create dest if it does not already exist
			if(!dest.exists()) {
				try {
					dest.createNewFile();
					System.out.printf("Successfully created %s.\n", dest.getName());
				} catch (IOException IOEx) {
					System.out.printf("Could not create %s ...\n", dest.getName());
					IOEx.printStackTrace();
				}
			}

			FileOutputStream out = new FileOutputStream(dest);
			DataOutputStream dataOut = new DataOutputStream(out);

			int numOfREntries = 0,
					numOfPREntries = 0;

			ArrayList<Integer> initializedR = new ArrayList<Integer>(),
					initializedPR = new ArrayList<Integer>();

			// Count the number of initialized entries and save their indices
			for(int idx = 0; idx < RegisterFile.REG_SIZE; ++idx) {
				if((RegisterFile.getDReg(idx)).value != null || RegisterFile.getDReg(idx).getComment() != null) {
					initializedR.add(idx);
					++numOfREntries;
				}

				if((RegisterFile.getPReg(idx)).point != null || RegisterFile.getPReg(idx).getComment() != null) {
					initializedPR.add(idx);
					++numOfPREntries;
				}
			}

			dataOut.writeInt(numOfREntries);
			// Save the Register entries
			for(Integer idx : initializedR) {
				dataOut.writeInt(idx);

				if(RegisterFile.getDReg(idx).value == null) {
					// save for null Float value
					dataOut.writeFloat(Float.NaN);
				} else {
					dataOut.writeFloat(RegisterFile.getDReg(idx).value);
				}

				if(RegisterFile.getDReg(idx).getComment() == null) {
					dataOut.writeUTF("");
				} else {
					dataOut.writeUTF(RegisterFile.getDReg(idx).getComment());
				}
			}

			dataOut.writeInt(numOfPREntries);
			// Save the Position Register entries
			for(Integer idx : initializedPR) {
				dataOut.writeInt(idx);
				savePoint(RegisterFile.getPReg(idx).point, dataOut);

				if(RegisterFile.getPReg(idx).getComment() == null) {
					dataOut.writeUTF("");
				} else {
					dataOut.writeUTF(RegisterFile.getPReg(idx).getComment());
				}

				dataOut.writeBoolean(RegisterFile.getPReg(idx).isCartesian);
			}

			dataOut.close();
			out.close();
			return 0;

		} catch (FileNotFoundException FNFEx) {
			// Could not be located dest
			System.out.printf("%s does not exist!\n", dest.getName());
			FNFEx.printStackTrace();
			return 1;

		} catch (IOException IOEx) {
			// Error occured while reading from dest
			System.out.printf("%s is corrupt!\n", dest.getName());
			IOEx.printStackTrace();
			return 2;
		}
	}

	/**
	 * Loads all the saved Registers and Position Registers from the
	 * given binary file. It is expected that the number of entries
	 * saved for both the Registers and Position Registers exist in the
	 * file before each respective list. Also, the index of an entry
	 * in the Register (or Position Register) list should also exist
	 * before each antry in the file.
	 * 
	 * @param src  The binary file from which to load the Register and
	 *             Position Register entries
	 * @return     0 if the load was successful,
	 *             1 if src could not be located,
	 *             2 if an error occured while reading from src
	 *             3 if the end of file is reached in source, before
	 *               all expected entries were read
	 */
	private int loadRegisterBytes(File src) {

		try {
			FileInputStream in = new FileInputStream(src);
			DataInputStream dataIn = new DataInputStream(in);

			int size = max(0, min(dataIn.readInt(), RegisterFile.REG_SIZE));

			// Load the Register entries
			while(size-- > 0) {
				// Each entry is saved after its respective index in REG
				int reg = dataIn.readInt();

				Float v = dataIn.readFloat();
				// Null values are saved as NaN
				if(Float.isNaN(v)) { v = null; }

				String c = dataIn.readUTF();
				// Null comments are saved as ""
				if(c.equals("")) { c = null; }

				RegisterFile.setDReg(reg, new DataRegister(reg, c, v));
			}

			size = max(0, min(dataIn.readInt(), RegisterFile.REG_SIZE));

			// Load the Position Register entries
			while(size-- > 0) {
				// Each entry is saved after its respective index in POS_REG
				int idx = dataIn.readInt();

				Point p = loadPoint(dataIn);
				String c = dataIn.readUTF();
				// Null comments are stored as ""
				if(c == "") { c = null; }
				boolean isCartesian = dataIn.readBoolean();

				RegisterFile.setPReg(idx, new PositionRegister(idx, c, p, isCartesian));
			}

			dataIn.close();
			in.close();
			return 0;

		} catch (FileNotFoundException FNFEx) {
			// Could not be located src
			System.out.printf("%s does not exist!\n", src.getName());
			FNFEx.printStackTrace();
			return 1;

		} catch (EOFException EOFEx) {
			// Unexpectedly reached the end of src
			System.out.printf("End of file, %s, was reached unexpectedly!\n", src.getName());
			EOFEx.printStackTrace();
			return 3;

		} catch (IOException IOEx) {
			// Error occrued while reading from src
			System.out.printf("%s is corrupt!\n", src.getName());
			IOEx.printStackTrace();
			return 2;
		}
	}

	/**
	 * Saves all the scenarios stored in SCENARIOS to given
	 * destination binary file.
	 * 
	 * @param dest  The binary file to which to save the scenarios
	 * @returning   0  if the saving of scenarios is successful,
	 *              1  if the file could not be found,
	 *              2  if some other error occurs with writing
	 *                 to dest
	 */
	private int saveScenarioBytes(File dest) {

		try {

			// Create dest if it does not already exist
			if(!dest.exists()) {
				try {
					dest.createNewFile();
					System.out.printf("Successfully created %s.\n", dest.getName());
				} catch (IOException IOEx) {
					System.out.printf("Could not create %s ...\n", dest.getName());
					IOEx.printStackTrace();
				}
			}

			FileOutputStream out = new FileOutputStream(dest);
			DataOutputStream dataOut = new DataOutputStream(out);

			int numOfScenarios = SCENARIOS.size();
			// Save the number of scenarios
			dataOut.writeInt(numOfScenarios);

			if (activeScenario == null) {
				// No active scenario
				dataOut.writeUTF("");
			} else {
				// Save the name of the active scenario
				dataOut.writeUTF(activeScenario.getName());
			}

			// Save all the scenarios
			for (int sdx = 0; sdx < SCENARIOS.size(); ++sdx) {
				Scenario s = SCENARIOS.get(sdx);

				if (s.getName().equals( activeScenario.getName() )) {
					// Update the previous version of the active scenario
					s = (Scenario)activeScenario.clone();
					SCENARIOS.set(sdx, s);
				}

				saveScenario(s, dataOut);
			}

			dataOut.close();
			out.close();
			return 0;

		} catch (FileNotFoundException FNFEx) {
			// Could not be located dest
			System.out.printf("%s does not exist!\n", dest.getName());
			FNFEx.printStackTrace();
			return 1;

		} catch (IOException IOEx) {
			// Error occrued while writing to dest
			System.out.printf("%s is corrupt!\n", dest.getName());
			IOEx.printStackTrace();
			return 2;

		}
	}

	/**
	 * Attempts to load scenarios from the given binary file.
	 * It is expected that an integer representing the number
	 * of scenarios is saved in the file first, followed by the
	 * index of the previously active scenario, and finally at
	 * least that number of scenarios.
	 * 
	 * @param src  The binary file from which to read scenarios
	 * @returning  0  if loading was succssful,
	 *             1  if the file could not be found,
	 *             2  if the file is corrupt,
	 *             3  if the end of file is reached unexpectedly,
	 *             4  if an error occurs with loading a .stl file
	 *                for the shape of a world object
	 */
	private int loadScenarioBytes(File src) {

		try {
			FileInputStream in = new FileInputStream(src);   //<>// //<>// //<>// //<>// //<>// //<>//
			DataInputStream dataIn = new DataInputStream(in);

			int numOfScenarios = dataIn.readInt();
			String activeScenarioName = dataIn.readUTF();

			// Load all scenarios saved
			while (numOfScenarios-- > 0) {
				Scenario s = loadScenario(dataIn);

				if (s.getName().equals(activeScenarioName)) {
					// Set the active scenario
					activeScenario = (Scenario)s.clone();
				}

				SCENARIOS.add(s);
			}

			dataIn.close();
			in.close();
			return 0;

		} catch (FileNotFoundException FNFEx) {
			// Could not be located src
			System.out.printf("%s does not exist!\n", src.getName());
			FNFEx.printStackTrace();
			return 1;

		} catch (EOFException EOFEx) {
			// Unexpectedly reached the end of src
			System.out.printf("End of file, %s, was reached unexpectedly!\n", src.getName());
			EOFEx.printStackTrace();
			return 3;

		} catch (IOException IOEx) {
			// Error occrued while reading from src
			System.out.printf("%s is corrupt!\n", src.getName());
			IOEx.printStackTrace();
			return 2;

		} catch (NullPointerException NPEx) {
			// Error with loading a .stl model
			System.out.printf("Missing source file!\n");
			NPEx.printStackTrace();
			return 4;
		}
	}

	private void saveExpression(Expression e, DataOutputStream out) throws IOException {

		if (e == null) {
			// Indicate the object saved is null
			out.writeByte(0);

		} else {
			out.writeByte(1);

			int exprLen = e.getSize();
			// Save the length of the expression
			out.writeInt(exprLen);

			// Save each expression element
			for (int idx = 0; idx < exprLen; ++idx) {
				saveExpressionElement(e.get(idx), out);
			}
		}
	}

	private Expression loadExpression(DataInputStream in) throws IOException, ClassCastException {
		byte nullFlag = in.readByte();

		if (nullFlag == 1) {
			Expression e = new Expression();
			// Read in expression length
			int len = in.readInt();

			for (int idx = 0; idx < len; ++idx) {
				// Read in an expression element
				ExpressionElement ee = loadExpressionElement(in);

				e.insertElement(idx);
				// Add it to the expression
				if (ee instanceof Operator) {
					e.setOperator(idx, (Operator)ee);

				} else {
					e.setOperand(idx, (ExprOperand)ee);
				}
			}

			return e;
		}

		return null;
	}

	private void saveExpressionElement(ExpressionElement ee, DataOutputStream out) throws IOException {

		if (ee == null) {
			// Indicate the object saved is null
			out.writeByte(0);

		} else {

			if (ee instanceof Operator) {
				// Operator
				Operator op = (Operator)ee;

				out.writeByte(1);
				out.writeInt( op.getOpID() );

			} else if (ee instanceof AtomicExpression) {
				// Subexpression
				AtomicExpression ae = (AtomicExpression)ee;

				out.writeByte(2);
				saveExpressionElement(ae.getArg1(), out);
				saveExpressionElement(ae.getArg2(), out);
				saveExpressionElement(ae.getOp(), out);

			} if (ee instanceof ExprOperand) {
				ExprOperand eo = (ExprOperand)ee;

				out.writeByte(3);
				// Indicate that the object is non-null
				out.writeInt(eo.type);

				if (eo.type == ExpressionElement.FLOAT) {
					// Constant float
					out.writeFloat( eo.getDataVal() );

				} else if (eo.type == ExpressionElement.BOOL) {
					// Constant boolean
					out.writeBoolean( eo.getBoolVal() );

				} else if (eo.type == ExpressionElement.DREG ||
						eo.type == ExpressionElement.IOREG ||
						eo.type == Expression.PREG ||
						eo.type == ExpressionElement.PREG_IDX) {

					// Data, Position, or IO register
					out.writeInt( eo.getRdx() );

					if (eo.type == ExpressionElement.PREG_IDX) {
						// Specific portion of a point
						out.writeInt( eo.getPosIdx() );
					}

				} else if (eo.type == ExpressionElement.POSTN) {
					// Robot position
					savePoint(eo.getPointVal(), out);

				} // Otherwise it is unitialized
			} 
		}
	}

	private ExpressionElement loadExpressionElement(DataInputStream in) throws
	IOException, ClassCastException {

		byte nullFlag = in.readByte();

		if (nullFlag == 1) {
			// Read in an operator
			int opFlag = in.readInt();
			return Operator.getOpFromID(opFlag);

		} else if (nullFlag == 2) {
			// Read in an atomic expression operand
			ExprOperand a0 = (ExprOperand)loadExpressionElement(in);
			ExprOperand a1 = (ExprOperand)loadExpressionElement(in);
			Operator op = (Operator)loadExpressionElement(in);

			return new AtomicExpression(a0, a1, op);

		} else if (nullFlag == 3) {
			// Read in a normal operand
			ExprOperand eo;
			int opType = in.readInt();

			if (opType == ExpressionElement.FLOAT) {
				// Constant float
				Float val = in.readFloat();
				eo = new ExprOperand(val);

			} else if (opType == ExpressionElement.BOOL) {
				// Constant boolean
				Boolean val = in.readBoolean();
				eo = new ExprOperand(val);

			} else if (opType == ExpressionElement.DREG ||
					opType == ExpressionElement.IOREG ||
					opType == Expression.PREG ||
					opType == ExpressionElement.PREG_IDX) {
				// Note: the register value of the operand is set to null!

				// Data, Position, or IO register
				Integer rdx = in.readInt();

				if (opType == ExpressionElement.DREG) {
					// Data register
					return new ExprOperand(RegisterFile.getDReg(rdx), rdx);

				} else if (opType == ExpressionElement.PREG) {
					// Position register
					eo = new ExprOperand(RegisterFile.getPReg(rdx), rdx);

				} else if (opType == ExpressionElement.PREG_IDX) {
					// Specific portion of a point
					Integer pdx = in.readInt();
					eo = new ExprOperand(RegisterFile.getPReg(rdx), rdx, pdx);

				} else if (opType == ExpressionElement.IOREG) {
					// I/O register
					eo = new ExprOperand(RegisterFile.getIOReg(rdx), rdx);

				} else {
					eo = new ExprOperand();
				}

			} else if (opType == ExpressionElement.POSTN) {
				// Robot position
				Point pt = loadPoint(in);
				eo = new ExprOperand(pt);

			} else {
				eo = new ExprOperand();
			}

			return eo;
		}

		// Uninitialized
		return new ExprOperand();
	}

	/**
	 * Saveds all the data associated with a scenario to the given output stream.
	 * First a single flag byte is saved to the stream followed by the number of
	 * objects in the scenario and then exxactly that number of world objects.
	 * 
	 * @param s    The scenario to save
	 * @param out  The output stream to which to save the scenario
	 * @throws     IOException if an erro occurs with writing to the output stream
	 */
	private void saveScenario(Scenario s, DataOutputStream out) throws IOException {

		if (s == null) {
			// Indicate the value saved is null
			out.writeByte(0);

		} else {
			// Indicate the value saved is non-null
			out.writeByte(1);
			// Write the name of the scenario
			out.writeUTF(s.getName());
			// Save the number of world objects in the scenario
			out.writeInt( s.size() );

			for (WorldObject wldObj : s) {
				// Save all the world objects associated with the scenario
				saveWorldObject(wldObj, out);  
			}
		}
	}

	/**
	 * Attempts to load the data of a Scenario from the given input stream. It is expected that
	 * the stream contains a single byte (the flag byte) followed by a String representing the
	 * name of the scenario. After the name of the scenario, there should be an positive integer
	 * value followed by exactly that many world objects.
	 * 
	 * @param in   The input stream from which to read bytes
	 * @returning  The Scenario pulled from the input stream
	 * @throws     IOException  if an error occurs with reading from the input stream
	 *             NullPointerException  if a world object has a model shape, whose source file is
	 *             corrupt or missing
	 */
	private Scenario loadScenario(DataInputStream in) throws IOException, NullPointerException {
		// Read flag byte
		byte flag = in.readByte();

		if (flag == 0) {
			return null;

		} else {
			// Read the name of the scenario
			String name = in.readUTF();
			Scenario s = new Scenario(name);
			// An extra set of only the loaded fixtures
			ArrayList<Fixture> fixtures = new ArrayList<Fixture>();
			// A list of parts which have a fixture reference defined
			ArrayList<LoadedPart> partsWithReferences = new ArrayList<LoadedPart>();

			// Read the number of objects in the scenario
			int size = in.readInt();
			// Read all the world objects contained in the scenario
			while (size-- > 0) {
				Object loadedObject = loadWorldObject(in);

				if (loadedObject instanceof WorldObject) {
					// Add all normal world objects to the scenario
					s.addWorldObject( (WorldObject)loadedObject );

					if (loadedObject instanceof Fixture) {
						// Save an extra reference of each fixture
						fixtures.add( (Fixture)loadedObject );
					}

				} else if (loadedObject instanceof LoadedPart) {
					LoadedPart lPart = (LoadedPart)loadedObject;

					if (lPart.part != null) {
						// Save the part in the scenario
						s.addWorldObject(lPart.part);

						if (lPart.referenceName != null) {
							// Save any part with a defined reference
							partsWithReferences.add(lPart);
						}
					}
				}
			}

			// Set all the Part's references
			for (LoadedPart lPart : partsWithReferences) {
				for (Fixture f : fixtures) {
					if (lPart.referenceName.equals(f.getName())) {
						lPart.part.setFixtureRef(f);
					}
				}
			}

			return s;
		}
	}

	/**
	 * Saved all the fields associated with the given world object to the given data output
	 * stream. First a single byte (the flag byte) is saved to the stream followed by the
	 * name and shape of the object and finally the fields associated with subclass of the object.
	 * 
	 * @param wldObj  The world object to save
	 * @param out     The output stream to which to save the world object
	 * @throws        IOException if an error occurs with writing to the output stream
	 */
	private void saveWorldObject(WorldObject wldObj, DataOutputStream out) throws IOException {

		if (wldObj == null) {   //<>// //<>// //<>// //<>// //<>// //<>//
			// Indicate that the value saved is null
			out.writeByte(0);

		} else {
			if (wldObj instanceof Part) {
				// Indicate that the value saved is a Part
				out.writeByte(1);
			} else if (wldObj instanceof Fixture) {
				// Indicate that the value saved is a Fixture
				out.writeByte(2);
			}

			// Save the name and form of the object
			out.writeUTF(wldObj.getName());
			saveShape(wldObj.getForm(), out);
			// Save the local orientation of the object
			savePVector(wldObj.getLocalCenter(), out);
			saveFloatArray2D(wldObj.getLocalOrientationAxes(), out);

			if (wldObj instanceof Part) {
				Part part = (Part)wldObj;
				String refName = "";

				savePVector(part.getOBBDims(), out);

				if (part.getFixtureRef() != null) {
					// Save the name of the part's fixture reference
					refName = part.getFixtureRef().getName();
				}

				out.writeUTF(refName);
			}
		}
	}

	/**
	 * TODO recomment this
	 * Attempts to load the data associated with a world object from the given data input stream. It
	 * is expected that the input stream contains a single byte (for the flag byte) followed by the
	 * name and shape of the object, which is followde by the data specific to the object's subclass.
	 * 
	 * @param in   The input stream from which to read bytes
	 * @returning  The world object pulled from the input streaam (which can be null!)
	 * @throws     IOException  if an error occurs with rading from the input stream
	 *             NullPointerExpcetion  if the world object has a model shape and its source file is
	 *             corrupt or missing
	 */
	private Object loadWorldObject(DataInputStream in) throws IOException, NullPointerException {
		// Load the flag byte
		byte flag = in.readByte();   //<>// //<>// //<>// //<>// //<>// //<>//
		Object wldObjFields = null;

		if (flag != 0) {
			// Load the name and shape of the object
			String name = in.readUTF();
			Shape form = loadShape(in);
			// Load the object's local orientation
			PVector center = loadPVector(in);
			float[][] orientationAxes = loadFloatArray2D(in);
			CoordinateSystem localOrientation = new CoordinateSystem();
			localOrientation.setOrigin(center);
			localOrientation.setAxes(orientationAxes);

			if (flag == 1) {
				// Load the part's bounding-box and fixture reference name
				PVector OBBDims = loadPVector(in);
				String refName = in.readUTF();

				if (refName.equals("")) {
					// A part object
					wldObjFields = new Part(name, form, OBBDims, localOrientation, null);
				} else {
					// A part object with its reference's name
					wldObjFields = new LoadedPart( new Part(name, form, OBBDims, localOrientation, null), refName );
				}

			} else if (flag == 2) {
				// A fixture object
				wldObjFields = new Fixture(name, form, localOrientation);
			} 
		}

		return wldObjFields;
	}

	/**
	 * Saves all the data associated with the given shape, in the form of bytes,
	 * to the given data output stream. First flag byte is saved, which indicates
	 * what subclass the object is (or if the object is null). Then the fields
	 * associated with the subclass saved followed by the color fields common among
	 * all shapes.
	 * 
	 * @param shape  The shape to save
	 * @param out    The output stream, to which to save the given shape
	 * @throws       IOException  if an error occurs with writing to the output stream
	 */
	private void saveShape(Shape shape, DataOutputStream out) throws IOException {
		if (shape == null) {
			// Indicate the saved value is null
			out.writeByte(0);

		} else {
			if (shape instanceof Box) {
				// Indicate the saved value is a box
				out.writeByte(1);
			} else if (shape instanceof Cylinder) {
				// Indicate the value saved is a cylinder
				out.writeByte(2);
			} else if (shape instanceof ModelShape) {
				// Indicate the value saved is a complex shape
				out.writeByte(3);
			}

			// Write fill color value
			saveInteger(shape.getFillValue(), out);

			if (shape instanceof Box) {
				// Write stroke value
				saveInteger(shape.getStrokeValue(), out);
				// Save length, height, and width of the box
				out.writeFloat(shape.getDim(DimType.LENGTH));
				out.writeFloat(shape.getDim(DimType.HEIGHT));
				out.writeFloat(shape.getDim(DimType.WIDTH));

			} else if (shape instanceof Cylinder) {
				// Write stroke value
				saveInteger(shape.getStrokeValue(), out);
				// Save the radius and height of the cylinder
				out.writeFloat(shape.getDim(DimType.RADIUS));
				out.writeFloat(shape.getDim(DimType.HEIGHT));

			} else if (shape instanceof ModelShape) {
				ModelShape m = (ModelShape)shape;

				out.writeFloat(m.getDim(DimType.SCALE));
				// Save the source path of the complex shape
				out.writeUTF(m.getSourcePath()); 
			}
		}
	}

	/**
	 * Attempts to load a Shape from the given data input stream. It is expected that the
	 * stream contains a single byte (the flag byte) followed by the fields unique to the
	 * subclass of the Shape object saved, which are followed by the color fields of the Shape.
	 * 
	 * @param in   The input stream, from which to read bytes
	 * @returning  The shape object pulled from the input stream (which can be null!)
	 * @throws     IOException  if an error occurs with reading from the input stream
	 *             NullPointerException  if the shape stored is a model shape and its source
	 *             file is either invalid or does not exist
	 */
	private Shape loadShape(DataInputStream in) throws IOException, NullPointerException {
		// Read flag byte
		byte flag = in.readByte();
		Shape shape = null;

		if (flag != 0) {
			// Read fiil color
			Integer fill = loadInteger(in);

			if (flag == 1) {
				// Read stroke color
				Integer strokeVal = loadInteger(in);
				float x = in.readFloat(),
						y = in.readFloat(),
						z = in.readFloat();
				// Create a box
				shape = new Box(fill, strokeVal, x, y, z);

			} else if (flag == 2) {
				// Read stroke color
				Integer strokeVal = loadInteger(in);
				float radius = in.readFloat(),
						hgt = in.readFloat();
				// Create a cylinder
				shape = new Cylinder(fill, strokeVal, radius, hgt);

			} else if (flag == 3) {
				float scale = in.readFloat();
				String srcPath = in.readUTF();

				// Creates a complex shape from the srcPath located in RobotRun/data/
				shape = new ModelShape(srcPath, fill, scale);
			}
		}

		return shape;
	}

	/**
	 * Writes the integer object to the given data output stream. Null values are accepted.
	 */
	private void saveInteger(Integer i, DataOutputStream out) throws IOException {

		if (i == null) {
			// Write byte flag
			out.writeByte(0);

		} else {
			// Write byte flag
			out.writeByte(1);
			// Write integer value
			out.writeInt(i);
		}
	}

	/**
	 * Attempts to read an Integer object from the given data input stream.
	 */
	private Integer loadInteger(DataInputStream in) throws IOException {
		// Read byte flag
		byte flag = in.readByte();

		if (flag == 0) {
			return null;

		} else {
			// Read integer value
			return in.readInt();
		}
	}

	/**
	 * Saves the x, y, z fields associated with the given PVector Object to the
	 * given output stream. Null values for p are accepted.
	 */
	private void savePVector(PVector p, DataOutputStream out) throws IOException {

		if (p == null) {
			// Write flag byte
			out.writeByte(0);

		} else {
			// Write flag byte
			out.writeByte(1);
			// Write vector data
			out.writeFloat(p.x);
			out.writeFloat(p.y);
			out.writeFloat(p.z);
		}
	}

	/**
	 * Attempts to load a PVector object from the given input stream.
	 */
	private PVector loadPVector(DataInputStream in) throws IOException {
		// Read flag byte
		int val = in.readByte();

		if (val == 0) {
			return null;

		} else {
			// Read vector data
			PVector v = new PVector();
			v.x = in.readFloat();
			v.y = in.readFloat();
			v.z = in.readFloat();
			return v;
		}
	}

	/**
	 * Saves the data associated with the given quaternion to the given output stream.
	 */
	private void saveRQuaternion(RQuaternion q, DataOutputStream out) throws IOException {
		if (q == null) {
			// Write flag byte
			out.writeByte(0);

		} else {
			// Write flag byte
			out.writeByte(1);

			for (int idx = 0; idx < 4; ++idx) {
				// Write each quaternion value
				out.writeFloat(q.getValue(idx));
			}
		}
	}

	/**
	 * Attempts to construct a quaternion object from the data in the given input stream.
	 */
	private RQuaternion loadRQuaternion(DataInputStream in) throws IOException {
		// Read flag byte
		byte flag = in.readByte();

		if (flag == 0) {
			return null;

		} else {
			// Read values of the quaternion
			float w = in.readFloat(),
					x = in.readFloat(),
					y = in.readFloat(),
					z = in.readFloat();

			return new RQuaternion(w, x, y, z);
		}
	}

	/**
	 * Saves the list of floats to the given data output stream. A flag byte is stored
	 * first, ten the length of list followed by each consecutive value in the list.
	 * 
	 * @param list  The array of floats to save
	 * @param out   The output stream, to which to save the float array
	 * @throws      IOException  if an error occurs with writing to the output stream
	 */
	private void saveFloatArray(float[] list, DataOutputStream out) throws IOException {
		if (list == null) {
			// Write flag value
			out.writeByte(0);

		} else {
			// Write flag value
			out.writeByte(1);
			// Write list length
			out.writeInt(list.length);
			// Write each value in the list
			for (int idx = 0; idx < list.length; ++idx) {
				out.writeFloat(list[idx]);
			}
		}
	}

	/**
	 * Attempts to parse a list of floats from the given data input stream.
	 * This method expects that a byte flag exists, follwed by a positive
	 * integer value for the length of the array, which is followed by at
	 * least that number of floating point values.
	 * 
	 * @param in   The input stream, from which to read bytes
	 * @returning  The float array pulled from the input stream (which
	 *             can be null!)
	 * @throws     IOException  if an error occurs with reading from the
	 *             output stream
	 */
	private float[] loadFloatArray(DataInputStream in) throws IOException {
		// Read byte flag
		byte flag = in.readByte();

		if (flag == 0) {
			return null;

		} else {
			// Read the length of the list
			int len = in.readInt();
			float[] list = new float[len];
			// Read each value of the list
			for (int idx = 0; idx < list.length; ++idx) {
				list[idx] = in.readFloat();
			}

			return list;
		}
	}

	/**
	 * Saves the 2D array of floats to the given data output stream. A flag byte is stored
	 * first, ten the dimensions of array followed by each consecutive value in the array.
	 * 
	 * @param list  The array matrix of floats to save
	 * @param out   The output stream, to which to save the float array matrix
	 * @throws      IOException  if an error occurs with writing to the output stream
	 */
	private void saveFloatArray2D(float[][] list, DataOutputStream out) throws IOException {
		if (list == null) {
			// Write flag value
			out.writeByte(0);

		} else {
			// Write flag value
			out.writeByte(1);
			// Write the dimensions of the list
			out.writeInt(list.length);
			out.writeInt(list[0].length);
			// Write each value in the list
			for (int row = 0; row < list.length; ++row) {
				for (int col = 0; col < list[0].length; ++col) {
					out.writeFloat(list[row][col]);
				}
			}
		}
	}

	/**
	 * Attempts to parse a 2D array of floats from the given data input stream.
	 * This method expects that a byte flag exists, follwed by two positive
	 * integer values for the dimensions of the array, which is followed by at
	 * least that number of floating point values.
	 * 
	 * @param in   The data input stream, from which to read bytes
	 * @returning  The float array matrix pulled from the input stream
	 * @throws     IOException  if an error occurs with reading from the
	 *             input stream
	 */
	private float[][] loadFloatArray2D(DataInputStream in) throws IOException {
		// Read byte flag
		byte flag = in.readByte();

		if (flag == 0) {
			return null;

		} else {
			// Read the length of the list
			int numOfRows = in.readInt(),
					numOfCols = in.readInt();
			float[][] list = new float[numOfRows][numOfCols];
			// Read each value of the list
			for (int row = 0; row < list.length; ++row) {
				for (int col = 0; col < list[0].length; ++col) {
					list[row][col] = in.readFloat();
				}
			}

			return list;
		}
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
		} catch(Exception Ex) {
			Ex.printStackTrace();
			return 1;
		}

		buffer.clear();
		return 0;
	}
	
	public final ArrayList<Scenario> SCENARIOS = new ArrayList<Scenario>();
	public Scenario activeScenario;
	public boolean showOOBs;

	/**
	 * Build a PShape object from the contents of the given .stl source file
	 * stored in /RobotRun/data/.
	 * 
	 * @throws NullPointerException  if the given filename does not pertain
	 *         to a valid .stl file located in RobotRun/data/
	 */
	public PShape loadSTLModel(String filename, int fill) throws NullPointerException {
		ArrayList<Triangle> triangles = new ArrayList<Triangle>();
		byte[] data = loadBytes(filename);

		int n = 84; // skip header and number of triangles

		while(n < data.length) {
			Triangle t = new Triangle();
			for(int m = 0; m < 4; m++) {
				byte[] bytesX = new byte[4];
				bytesX[0] = data[n+3]; bytesX[1] = data[n+2];
				bytesX[2] = data[n+1]; bytesX[3] = data[n];
				n += 4;
				byte[] bytesY = new byte[4];
				bytesY[0] = data[n+3]; bytesY[1] = data[n+2];
				bytesY[2] = data[n+1]; bytesY[3] = data[n];
				n += 4;
				byte[] bytesZ = new byte[4];
				bytesZ[0] = data[n+3]; bytesZ[1] = data[n+2];
				bytesZ[2] = data[n+1]; bytesZ[3] = data[n];
				n += 4;
				t.components[m] = new PVector(
						ByteBuffer.wrap(bytesX).getFloat(),
						ByteBuffer.wrap(bytesY).getFloat(),
						ByteBuffer.wrap(bytesZ).getFloat()
						);
			}
			triangles.add(t);
			n += 2; // skip meaningless "attribute byte count"
		}

		PShape mesh = createShape();
		mesh.beginShape(TRIANGLES);
		mesh.noStroke();
		mesh.fill(fill);
		for(Triangle t : triangles) {
			mesh.normal(t.components[0].x, t.components[0].y, t.components[0].z);
			mesh.vertex(t.components[1].x, t.components[1].y, t.components[1].z);
			mesh.vertex(t.components[2].x, t.components[2].y, t.components[2].z);
			mesh.vertex(t.components[3].x, t.components[3].y, t.components[3].z);
		}
		mesh.endShape();

		return mesh;
	} 


	/**
	 * Applies the rotations and translations of the Robot Arm to get to the
	 * face plate center, given the set of six joint angles, each corresponding
	 * to a joint of the Robot Arm and each within the bounds of [0, TWO_PI).
	 * 
	 * @param jointAngles  A valid set of six joint angles (in radians) for the Robot
	 */
	public static void applyModelRotation(float[] jointAngles) {
		instance.translate(ROBOT_POSITION.x, ROBOT_POSITION.y, ROBOT_POSITION.z);

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
		instance.rotateY(PI/2);
		instance.translate(0, 62, 62);
		instance.rotateX(jointAngles[1]);
		instance.translate(0, -62, -62);
		instance.rotateY(-PI/2);
		instance.rotateZ(-PI);   
		instance.translate(0, -500, -50);
		instance.rotateZ(PI);
		instance.rotateY(PI/2);
		instance.translate(0, 75, 75);
		instance.rotateZ(PI);
		instance.rotateX(jointAngles[2]);
		instance.rotateZ(-PI);
		instance.translate(0, -75, -75);
		instance.rotateY(PI/2);
		instance.rotateZ(-PI);
		instance.translate(745, -150, 150);
		instance.rotateZ(PI/2);
		instance.rotateY(PI/2);
		instance.translate(70, 0, 70);
		instance.rotateY(jointAngles[3]);
		instance.translate(-70, 0, -70);
		instance.rotateY(-PI/2);
		instance.rotateZ(-PI/2);    
		instance.translate(-115, 130, -124);
		instance.rotateZ(PI);
		instance.rotateY(-PI/2);
		instance.translate(0, 50, 50);
		instance.rotateX(jointAngles[4]);
		instance.translate(0, -50, -50);
		instance.rotateY(PI/2);
		instance.rotateZ(-PI);    
		instance.translate(150, -10, 95);
		instance.rotateY(-PI/2);
		instance.rotateZ(PI);
		instance.translate(45, 45, 0);
		instance.rotateZ(jointAngles[5]);
	}

	/**
	 * Converts the given point, pt, into the Coordinate System defined by the given origin
	 * vector and rotation quaternion axes. The joint angles associated with the point will
	 * be transformed as well, though, if inverse kinematics fails, then the original joint
	 * angles are used instead.
	 * 
	 * @param pt      A point with initialized position and orientation
	 * @param origin  The origin of the Coordinate System
	 * @param axes    The axes of the Coordinate System representing as a rotation quanternion
	 * @returning     The point, pt, interms of the given frame's Coordinate System
	 */
	public static Point applyFrame(Point pt, PVector origin, RQuaternion axes) {
		PVector position = convertToFrame(pt.position, origin, axes);
		RQuaternion orientation = axes.transformQuaternion(pt.orientation);
		// Update joint angles associated with the point
		float[] newJointAngles = inverseKinematics(pt.angles, position, orientation);

		if (newJointAngles != null) {
			return new Point(position, orientation, newJointAngles);
		} else {
			// If inverse kinematics fails use the old angles
			return new Point(position, orientation, pt.angles);
		}
	}

	/**
	 * Converts the given vector, v, into the Coordinate System defined by the given origin
	 * vector and rotation quaternion axes.
	 * 
	 * @param v      A vector in the XYZ vector space
	 * @param origin  The origin of the Coordinate System
	 * @param axes    The axes of the Coordinate System representing as a rotation quanternion
	 * @returning     The vector, v, interms of the given frame's Coordinate System
	 */
	public static PVector convertToFrame(PVector v, PVector origin, RQuaternion axes) {
		PVector vOffset = PVector.sub(v, origin);
		return axes.rotateVector(vOffset);
	}

	/**
	 * Converts the given point, pt, from the Coordinate System defined by the given origin
	 * vector and rotation quaternion axes. The joint angles associated with the point will
	 * be transformed as well, though, if inverse kinematics fails, then the original joint
	 * angles are used instead.
	 * 
	 * @param pt      A point with initialized position and orientation
	 * @param origin  The origin of the Coordinate System
	 * @param axes    The axes of the Coordinate System representing as a rotation quanternion
	 * @returning     The point, pt, interms of the given frame's Coordinate System
	 */
	public static Point removeFrame(Point pt, PVector origin, RQuaternion axes) {
		PVector position = convertFromFrame(pt.position, origin, axes);
		RQuaternion orientation = RQuaternion.mult(pt.orientation, axes);

		// Update joint angles associated with the point
		float[] newJointAngles = inverseKinematics(pt.angles, position, orientation);

		if (newJointAngles != null) {
			return new Point(position, orientation, newJointAngles);
		} else {
			// If inverse kinematics fails use the old angles
			return new Point(position, orientation, pt.angles);
		}
	}

	/**
	 * Converts the given vector, u, from the Coordinate System defined by the given origin
	 * vector and rotation quaternion axes.
	 * 
	 * @param v       A vector in the XYZ vector space
	 * @param origin  The origin of the Coordinate System
	 * @param axes    The axes of the Coordinate System representing as a rotation quanternion
	 * @returning     The vector, u, in the Native frame
	 */
	public static PVector convertFromFrame(PVector u, PVector origin, RQuaternion axes) {
		RQuaternion invAxes = axes.conjugate();
		invAxes.normalize();
		PVector vRotated = invAxes.rotateVector(u);
		return vRotated.add(origin);
	}

	/**
	 * Converts the given vector form the right-hand World Frame Coordinate System
	 * to the left-hand Native Coordinate System.
	 */
	public static PVector convertWorldToNative(PVector v) {
		float[][] tMatrix = transformationMatrix(new PVector(0f, 0f, 0f), WORLD_AXES);
		return transformVector(v, tMatrix);
	}

	/**
	 * Converts the given vector form the left-hand Native Coordinate System to the
	 * right-hand World Frame Coordinate System.
	 */
	public static PVector convertNativeToWorld(PVector v) {
		float[][] tMatrix = transformationMatrix(new PVector(0f, 0f, 0f), WORLD_AXES);
		return transformVector(v, invertHCMatrix(tMatrix));
	}

	/* Transforms the given vector from the coordinate system defined by the given
	 * transformation matrix (row major order). */
	public static PVector transformVector(PVector v, float[][] tMatrix) {
		if(tMatrix.length != 4 || tMatrix[0].length != 4) {
			return null;
		}

		PVector u = new PVector();
		// Apply the transformation matrix to the given vector
		u.x = v.x * tMatrix[0][0] + v.y * tMatrix[1][0] + v.z * tMatrix[2][0] + tMatrix[0][3];
		u.y = v.x * tMatrix[0][1] + v.y * tMatrix[1][1] + v.z * tMatrix[2][1] + tMatrix[1][3];
		u.z = v.x * tMatrix[0][2] + v.y * tMatrix[1][2] + v.z * tMatrix[2][2] + tMatrix[2][3];

		return u;
	}

	/* Transforms the given vector by the given 3x3 rotation matrix (row major order). */
	public static PVector rotateVector(PVector v, float[][] rotMatrix) {
		if(v == null || rotMatrix == null || rotMatrix.length != 3 || rotMatrix[0].length != 3) {
			return null;
		}

		PVector u = new PVector();
		// Apply the rotation matrix to the given vector
		u.x = v.x * rotMatrix[0][0] + v.y * rotMatrix[1][0] + v.z * rotMatrix[2][0];
		u.y = v.x * rotMatrix[0][1] + v.y * rotMatrix[1][1] + v.z * rotMatrix[2][1];
		u.z = v.x * rotMatrix[0][2] + v.y * rotMatrix[1][2] + v.z * rotMatrix[2][2];

		return u;
	}

	/**
	 * Find the inverse of the given 4x4 Homogeneous Coordinate Matrix. 
	 * 
	 * This method is based off of the algorithm found on this webpage:
	 *    https://web.archive.org/web/20130806093214/http://www-graphics.stanford.edu/
	 *      courses/cs248-98-fall/Final/q4.html
	 */
	public static float[][] invertHCMatrix(float[][] m) {
		if(m.length != 4 || m[0].length != 4) {
			return null;
		}

		float[][] inverse = new float[4][4];

		/* [ ux vx wx tx ] -1       [ ux uy uz -dot(u, t) ]
		 * [ uy vy wy ty ]     =    [ vx vy vz -dot(v, t) ]
		 * [ uz vz wz tz ]          [ wx wy wz -dot(w, t) ]
		 * [  0  0  0  1 ]          [  0  0  0      1     ]
		 */
		inverse[0][0] = m[0][0];
		inverse[0][1] = m[1][0];
		inverse[0][2] = m[2][0];
		inverse[0][3] = -(m[0][0] * m[0][3] + m[0][1] * m[1][3] + m[0][2] * m[2][3]);
		inverse[1][0] = m[0][1];
		inverse[1][1] = m[1][1];
		inverse[1][2] = m[2][1];
		inverse[1][3] = -(m[1][0] * m[0][3] + m[1][1] * m[1][3] + m[1][2] * m[2][3]);
		inverse[2][0] = m[0][2];
		inverse[2][1] = m[1][2];
		inverse[2][2] = m[2][2];
		inverse[2][3] = -(m[2][0] * m[0][3] + m[2][1] * m[1][3] + m[2][2] * m[2][3]);
		inverse[3][0] = 0;
		inverse[3][1] = 0;
		inverse[3][2] = 0;
		inverse[3][3] = 1;

		return inverse;
	}

	/* Returns a 4x4 vector array which reflects the current transform matrix on the top
	 * of the stack (ignores scaling values though) */
	public float[][] getTransformationMatrix() {
		float[][] transform = new float[4][4];

		// Caculate four vectors corresponding to the four columns of the transform matrix
		PVector origin = getCoordFromMatrix(0, 0, 0);
		PVector xAxis = getCoordFromMatrix(1, 0, 0).sub(origin);
		PVector yAxis = getCoordFromMatrix(0, 1, 0).sub(origin);
		PVector zAxis = getCoordFromMatrix(0, 0, 1).sub(origin);

		// Place the values of each vector in the correct cells of the transform  matrix
		transform[0][0] = xAxis.x;
		transform[0][1] = xAxis.y;
		transform[0][2] = xAxis.z;
		transform[0][3] = origin.x;
		transform[1][0] = yAxis.x;
		transform[1][1] = yAxis.y;
		transform[1][2] = yAxis.z;
		transform[1][3] = origin.y;
		transform[2][0] = zAxis.x;
		transform[2][1] = zAxis.y;
		transform[2][2] = zAxis.z;
		transform[2][3] = origin.z;
		transform[3][0] = 0;
		transform[3][1] = 0;
		transform[3][2] = 0;
		transform[3][3] = 1;

		return transform;
	}

	/**
	 * Forms the 4x4 transformation matrix (row major order) form the given
	 * origin offset and axes offset (row major order) of the Native Coordinate
	 * system.
	 * 
	 * @param origin  the X, Y, Z, offset of the origin for the Coordinate frame
	 * @param axes    a 3x3 rotatin matrix (row major order) representing the unit
	 *                vector axes offset of the new Coordinate Frame from the Native
	 *                Coordinate Frame
	 * @returning     the 4x4 transformation matrix (column major order) formed from
	 *                the given origin and axes offset
	 */
	public static float[][] transformationMatrix(PVector origin, float[][] axes) {
		float[][] transform = new float[4][4];

		transform[0][0] = axes[0][0];
		transform[1][0] = axes[1][0];
		transform[2][0] = axes[2][0];
		transform[3][0] = 0;
		transform[0][1] = axes[0][1];
		transform[1][1] = axes[1][1];
		transform[2][1] = axes[2][1];
		transform[3][1] = 0;
		transform[0][2] = axes[0][2];
		transform[1][2] = axes[1][2];
		transform[2][2] = axes[2][2];
		transform[3][2] = 0;
		transform[0][3] = origin.x;
		transform[1][3] = origin.y;
		transform[2][3] = origin.z;
		transform[3][3] = 1;

		return transform;
	}

	/**
	 * Returns a 3x3 rotation matrix of the current transformation
	 * matrix on the stack (in row major order).
	 */
	public float[][] getRotationMatrix() {
		float[][] rMatrix = new float[3][3];
		// Calculate origin point
		PVector origin = getCoordFromMatrix(0f, 0f, 0f),
				// Create axes vectors
				vx = getCoordFromMatrix(1f, 0f, 0f).sub(origin),
				vy = getCoordFromMatrix(0f, 1f, 0f).sub(origin),
				vz = getCoordFromMatrix(0f, 0f, 1f).sub(origin);
		// Save values in a 3x3 rotation matrix
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

	/* This method transforms the given coordinates into a vector
	 * in the Processing's native coordinate system. */
	public PVector getCoordFromMatrix(float x, float y, float z) {
		PVector vector = new PVector();

		vector.x = modelX(x, y, z);
		vector.y = modelY(x, y, z);
		vector.z = modelZ(x, y, z);

		return vector;
	}

	/* Calculate v x v */
	public static float[] crossProduct(float[] v, float[] u) {
		if(v.length != 3 && v.length != u.length) { return null; }

		float[] w = new float[v.length];
		// [a, b, c] x [d, e, f] = [ bf - ce, cd - af, ae - bd ]
		w[0] = v[1] * u[2] - v[2] * u[1];
		w[1] = v[2] * u[0] - v[0] * u[2];
		w[2] = v[0] * u[1] - v[1] * u[0];

		return w;
	}

	/* Returns a vector with the opposite sign
	 * as the given vector. */
	public static float[] negate(float[] v) {
		float[] u = new float[v.length];

		for(int e = 0; e < v.length; ++e) {
			u[e] = -v[e];
		}

		return u;
	}

	//calculates rotation matrix from euler angles
	public static float[][] eulerToMatrix(PVector wpr) {
		float[][] r = new float[3][3];
		float xRot = wpr.x;
		float yRot = wpr.y;
		float zRot = wpr.z;

		r[0][0] = (float)Math.cos(yRot)*(float)Math.cos(zRot);
		r[0][1] = (float)Math.sin(xRot)*(float)Math.sin(yRot)*(float)Math.cos(zRot) - (float)Math.cos(xRot)*(float)Math.sin(zRot);
		r[0][2] = (float)Math.cos(xRot)*(float)Math.sin(yRot)*(float)Math.cos(zRot) + (float)Math.sin(xRot)*(float)Math.sin(zRot);
		r[1][0] = (float)Math.cos(yRot)*(float)Math.sin(zRot);
		r[1][1] = (float)Math.sin(xRot)*(float)Math.sin(yRot)*(float)Math.sin(zRot) + (float)Math.cos(xRot)*(float)Math.cos(zRot);
		r[1][2] = (float)Math.cos(xRot)*(float)Math.sin(yRot)*(float)Math.sin(zRot) - (float)Math.sin(xRot)*(float)Math.cos(zRot);
		r[2][0] = -(float)Math.sin(yRot);
		r[2][1] = (float)Math.sin(xRot)*(float)Math.cos(yRot);
		r[2][2] = (float)Math.cos(xRot)*(float)Math.cos(yRot);

		float[] magnitudes = new float[3];

		for(int v = 0; v < r.length; ++v) {
			// Find the magnitude of each axis vector
			for(int e = 0; e < r[0].length; ++e) {
				magnitudes[v] += (float)Math.pow(r[v][e], 2);
			}

			magnitudes[v] = (float)Math.sqrt(magnitudes[v]);
			// Normalize each vector
			for(int e = 0; e < r.length; ++e) {
				r[v][e] /= magnitudes[v];
			}
		}
		/**/

		return r;
	}

	/**
	 * Converts the given Euler angle set values to a quaternion
	 */
	public static RQuaternion eulerToQuat(PVector wpr) {
		float w, x, y, z;
		float xRot = wpr.x;
		float yRot = wpr.y;
		float zRot = wpr.z;

		w = (float)Math.cos(xRot/2)*(float)Math.cos(yRot/2)*(float)Math.cos(zRot/2) + (float)Math.sin(xRot/2)*(float)Math.sin(yRot/2)*(float)Math.sin(zRot/2);
		x = (float)Math.sin(xRot/2)*(float)Math.cos(yRot/2)*(float)Math.cos(zRot/2) - (float)Math.cos(xRot/2)*(float)Math.sin(yRot/2)*(float)Math.sin(zRot/2);
		y = (float)Math.cos(xRot/2)*(float)Math.sin(yRot/2)*(float)Math.cos(zRot/2) + (float)Math.sin(xRot/2)*(float)Math.cos(yRot/2)*(float)Math.sin(zRot/2);
		z = (float)Math.cos(xRot/2)*(float)Math.cos(yRot/2)*(float)Math.sin(zRot/2) - (float)Math.sin(xRot/2)*(float)Math.sin(yRot/2)*(float)Math.cos(zRot/2);

		return new RQuaternion(w, x, y, z);
	}

	//calculates euler angles from rotation matrix
	public static PVector matrixToEuler(float[][] r) {
		float yRot1, xRot1, zRot1;
		PVector wpr;

		if(r[2][0] != 1 && r[2][0] != -1) {
			//rotation about y-axis
			yRot1 = -(float)Math.asin(r[2][0]);
			//rotation about x-axis
			xRot1 = (float)Math.atan2(r[2][1]/(float)Math.cos(yRot1), r[2][2]/(float)Math.cos(yRot1));
			//rotation about z-axis
			zRot1 = (float)Math.atan2(r[1][0]/(float)Math.cos(yRot1), r[0][0]/(float)Math.cos(yRot1));
		} else {
			zRot1 = 0;
			if(r[2][0] == -1) {
				yRot1 = PI/2;
				xRot1 = zRot1 + (float)Math.atan2(r[0][1], r[0][2]);
			} else {
				yRot1 = -PI/2;
				xRot1 = -zRot1 + (float)Math.atan2(-r[0][1], -r[0][2]);
			}
		}

		wpr = new PVector(xRot1, yRot1, zRot1);
		return wpr;
	}

	//calculates quaternion from rotation matrix
	public static RQuaternion matrixToQuat(float[][] r) {
		float[] limboQ = new float[4];
		float tr = r[0][0] + r[1][1] + r[2][2];

		if(tr > 0) {
			float S = (float)Math.sqrt(1.0f + tr) * 2; // S=4*q[0] 
			limboQ[0] = S / 4;
			limboQ[1] = (r[2][1] - r[1][2]) / S;
			limboQ[2] = (r[0][2] - r[2][0]) / S; 
			limboQ[3] = (r[1][0] - r[0][1]) / S;
		} else if((r[0][0] > r[1][1]) & (r[0][0] > r[2][2])) {
			float S = (float)Math.sqrt(1.0f + r[0][0] - r[1][1] - r[2][2]) * 2; // S=4*q[1] 
			limboQ[0] = (r[2][1] - r[1][2]) / S;
			limboQ[1] = S / 4;
			limboQ[2] = (r[0][1] + r[1][0]) / S; 
			limboQ[3] = (r[0][2] + r[2][0]) / S;
		} else if(r[1][1] > r[2][2]) {
			float S = (float)Math.sqrt(1.0f + r[1][1] - r[0][0] - r[2][2]) * 2; // S=4*q[2]
			limboQ[0] = (r[0][2] - r[2][0]) / S;
			limboQ[1] = (r[0][1] + r[1][0]) / S; 
			limboQ[2] = S / 4;
			limboQ[3] = (r[1][2] + r[2][1]) / S;
		} else {
			float S = (float)Math.sqrt(1.0f + r[2][2] - r[0][0] - r[1][1]) * 2; // S=4*q[3]
			limboQ[0] = (r[1][0] - r[0][1]) / S;
			limboQ[1] = (r[0][2] + r[2][0]) / S;
			limboQ[2] = (r[1][2] + r[2][1]) / S;
			limboQ[3] = S / 4;
		}

		RQuaternion q = new RQuaternion(limboQ[0], limboQ[1], limboQ[2], limboQ[3]);
		q.normalize();

		return q;
	}

	//calculates euler angles from quaternion
	public static PVector quatToEuler(RQuaternion q) {
		float[][] r = q.toMatrix();
		PVector wpr = matrixToEuler(r);
		return wpr;
	}

	//calculates rotation matrix from quaternion
	public static float[][] quatToMatrix(RQuaternion q) {
		float[][] r = new float[3][3];

		r[0][0] = 1 - 2*(q.getValue(2)*q.getValue(2) + q.getValue(3)*q.getValue(3));
		r[0][1] = 2*(q.getValue(1)*q.getValue(2) - q.getValue(0)*q.getValue(3));
		r[0][2] = 2*(q.getValue(0)*q.getValue(2) + q.getValue(1)*q.getValue(3));
		r[1][0] = 2*(q.getValue(1)*q.getValue(2) + q.getValue(0)*q.getValue(3));
		r[1][1] = 1 - 2*(q.getValue(1)*q.getValue(1) + q.getValue(3)*q.getValue(3));
		r[1][2] = 2*(q.getValue(2)*q.getValue(3) - q.getValue(0)*q.getValue(1));
		r[2][0] = 2*(q.getValue(1)*q.getValue(3) - q.getValue(0)*q.getValue(2));
		r[2][1] = 2*(q.getValue(0)*q.getValue(1) + q.getValue(2)*q.getValue(3));
		r[2][2] = 1 - 2*(q.getValue(1)*q.getValue(1) + q.getValue(2)*q.getValue(2));

		float[] magnitudes = new float[3];

		for(int v = 0; v < r.length; ++v) {
			// Find the magnitude of each axis vector
			for(int e = 0; e < r[0].length; ++e) {
				magnitudes[v] += pow(r[v][e], 2);
			}

			magnitudes[v] = sqrt(magnitudes[v]);
			// Normalize each vector
			for(int e = 0; e < r.length; ++e) {
				r[v][e] /= magnitudes[v];
			}
		}
		/**/

		return r;
	}

	//converts a float array to a double array
	public static double[][] floatToDouble(float[][] m, int l, int w) {
		double[][] r = new double[l][w];

		for(int i = 0; i < l; i += 1) {
			for(int j = 0; j < w; j += 1) {
				r[i][j] = (double)m[i][j];
			}
		}

		return r;
	}

	//converts a double array to a float array
	public static float[][] doubleToFloat(double[][] m, int l, int w) {
		float[][] r = new float[l][w];

		for(int i = 0; i < l; i += 1) {
			for(int j = 0; j < w; j += 1) {
				r[i][j] = (float)m[i][j];
			}
		}

		return r;
	}

	//produces a rotation matrix given a rotation 'theta' around
	//a given axis
	public static float[][] rotateAxisVector(float[][] m, float theta, PVector axis) {
		float s = sin(theta);
		float c = cos(theta);
		float t = 1-c;

		if(c > 0.9f)
			t = 2*sin(theta/2)*sin(theta/2);

		float x = axis.x;
		float y = axis.y;
		float z = axis.z;

		float[][] r = new float[3][3];

		r[0][0] = x*x*t+c;
		r[0][1] = x*y*t-z*s;
		r[0][2] = x*z*t+y*s;
		r[1][0] = y*x*t+z*s;
		r[1][1] = y*y*t+c;
		r[1][2] = y*z*t-x*s;
		r[2][0] = z*x*t-y*s;
		r[2][1] = z*y*t+x*s;
		r[2][2] = z*z*t+c;

		RealMatrix M = new Array2DRowRealMatrix(floatToDouble(m, 3, 3));
		RealMatrix R = new Array2DRowRealMatrix(floatToDouble(r, 3, 3));
		RealMatrix MR = M.multiply(R);

		return doubleToFloat(MR.getData(), 3, 3);
	}

	//returns the result of a vector 'v' multiplied by scalar 's'
	public static float[] vectorScalarMult(float[] v, float s) {
		float[] ret = new float[v.length];
		for(int i = 0; i < ret.length; i += 1) { 
			ret[i] = v[i]*s; 
		}

		return ret;
	}

	/**
	 * Determines if the lies within the range of angles that span from rangeStart to rangeEnd,
	 * going clockwise around the Unit Cycle. It is assumed that all parameters are in radians
	 * and within the range [0, TWO_PI).
	 * 
	 * @param angleToVerify  the angle in question
	 * @param rangeStart     the 'lower bounds' of the angle range to check
	 * @param rangeEnd       the 'upper bounds' of the angle range to check
	 */
	public static boolean angleWithinBounds(float angleToVerify, float rangeStart, float rangeEnd) {

		if(rangeStart < rangeEnd) {
			// Joint range does not overlap TWO_PI
			return angleToVerify >= rangeStart && angleToVerify <= rangeEnd;
		} else {
			// Joint range overlaps TWO_PI
			return !(angleToVerify > rangeEnd && angleToVerify < rangeStart);
		}
	}

	/**
	 * Brings the given angle (in radians) within the range: [0, TWO_PI).
	 * 
	 * @param angle  Some rotation in radians
	 * @returning    The equivalent angle within the range [0, TWO_PI)
	 */
	public static float mod2PI(float angle) {
		float temp = angle % TWO_PI;

		if (temp < 0f) {
			temp += TWO_PI;
		}

		return temp;
	}

	/**
	 * Computes the minimum rotational magnitude to move
	 * from src to dest, around the unit circle.
	 * 
	 * @param src   The source angle in radians
	 * @param dset  The destination angle in radians
	 * @returning   The minimum distance between src and dest
	 */
	public static float minimumDistance(float src, float dest) {
		// Bring angles within range [0, TWO_PI)
		float difference = mod2PI(dest) - mod2PI(src);

		if (difference > PI) {
			difference -= TWO_PI;
		} else if (difference < -PI) {
			difference += TWO_PI;
		}

		return difference;
	}

	public static int clamp(int in, int min, int max) {
		return min(max, max(min, in));
	}




	public Camera getCamera() {
		return camera;
	}

	public ArmModel getArmModel() {
		return armModel;
	}

	public ControlP5 getCp5() {
		return cp5;
	}

	public static AxesDisplay getAxesState() {
		return axesState;
	}

	public static void setAxesState(AxesDisplay axesState) {
		RobotRun.axesState = axesState;
	}

	public int getActive_prog() {
		return active_prog;
	}

	public void setActive_prog(int active_prog) {
		this.active_prog = active_prog;
	}

	public int getActive_instr() {
		return active_instr;
	}

	public void setActive_instr(int active_instr) {
		this.active_instr = active_instr;
	}

	public Stack<int[]> getCall_stack() {
		return call_stack;
	}

	public void setCall_stack(Stack<int[]> call_stack) {
		this.call_stack = call_stack;
	}

	public int getRow_select() {
		return row_select;
	}

	public void setRow_select(int row_select) {
		this.row_select = row_select;
	}

	public int getCol_select() {
		return col_select;
	}

	public void setCol_select(int col_select) {
		this.col_select = col_select;
	}

	public int getStart_render() {
		return start_render;
	}

	public void setStart_render(int start_render) {
		this.start_render = start_render;
	}

	public boolean isExecutingInstruction() {
		return executingInstruction;
	}

	public void setExecutingInstruction(boolean executingInstruction) {
		this.executingInstruction = executingInstruction;
	}

	public boolean isStep() {
		return step;
	}

	public void setStep(boolean step) {
		this.step = step;
	}

	public boolean isProgramRunning() {
		return programRunning;
	}

	public void setProgramRunning(boolean programRunning) {
		this.programRunning = programRunning;
	}

	public Macro[] getSU_macro_bindings() {
		return SU_macro_bindings;
	}

	public void setSU_macro_bindings(Macro[] sU_macro_bindings) {
		SU_macro_bindings = sU_macro_bindings;
	}

	public WindowManager getManager() {
		return manager;
	}

	public void setManager(WindowManager manager) {
		this.manager = manager;
	}

	public int getRecord() {
		return record;
	}

	public void setRecord(int record) {
		this.record = record;
	}
	
	/**
	 * Returns the instance of this PApplet
	 */
	public static RobotRun getInstance() {
		return instance;
	}
	
	/**
	 * Returns this PApplet instance's Arm model reference.
	 */
	public static ArmModel getRobot() {
		return instance.getArmModel();
	}
}

package robot;

import java.util.ArrayList;
import java.util.Stack;

import core.RobotRun;
import core.Scenario;
import enums.AxesDisplay;
import enums.CoordFrame;
import enums.ExecState;
import enums.InstOp;
import enums.ExecType;
import frame.ToolFrame;
import frame.UserFrame;
import geom.BoundingBox;
import geom.MyPShape;
import geom.Part;
import geom.Point;
import geom.RMatrix;
import geom.RQuaternion;
import geom.RRay;
import geom.WorldObject;
import global.Fields;
import global.RMath;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;
import programming.CallInstruction;
import programming.IfStatement;
import programming.Instruction;
import programming.JumpInstruction;
import programming.MotionInstruction;
import programming.ProgExecution;
import programming.Program;
import programming.SelectStatement;
import regs.DataRegister;
import regs.IORegister;
import regs.PositionRegister;
import screen.DisplayLine;
import screen.InstState;
import screen.MenuScroll;

public class RoboticArm {
	
	/**
	 * Defines the conversion between the robot's maximum rotation speed and
	 * its maximum linear motion speed.
	 */
	public static final int motorSpeed;
	
	/**
	 * The unique ID of this robot.
	 */
	public final int RID;
	
	/**
	 * The position of the center of the robot's base segment.
	 */
	private final PVector BASE_POSITION;
	
	/**
	 * Defines sets of indices which map to pairs of bounding boxes between
	 * two of the robot's segments. This is used for checking self-collisions
	 * of the robot's bounding boxes.
	 */
	private final int[] SEG_OBB_CHECKS;
	
	/**
	 * Defines sets of indices, which map to the robot's segment bounding
	 * boxes. This is used to check for collisions between the robot's end 
	 * effector and its segments.
	 */
	private final int[] EE_SEG_OBB_CHECKS;
	
	/**
	 * A list of the robot's arm segment models.
	 */
	private final RSegWithJoint[] SEGMENT;
	
	/**
	 * The list of the robot's end effectors.
	 */
	private final EndEffector[] EE_LIST;
	
	/**
	 * The list of programs associated with this robot.
	 */
	private final ArrayList<Program> PROGRAM;
	
	/**
	 * A program execution call stack for previously active programs associated
	 * with this robot.
	 */
	private final Stack<CallFrame> CALL_STACK;
	
	/**
	 * A stack of previous states of instructions that the user has since edited.
	 */
	private final Stack<InstState> PROG_UNDO;
	
	/**
	 * The data register associated with this robot.
	 */
	private final DataRegister[] DREG;
	
	/**
	 * The position registers associated with this robot.
	 */
	private final PositionRegister[] PREG;
	
	/**
	 * A set of user-defined frames associated with this robot. 
	 */
	private final ToolFrame[] TOOL_FRAME;
	
	/**
	 * A set of user-defined frames associated with this robot.
	 */
	private final UserFrame[] USER_FRAME;
	
	/**
	 * The initial position and orientation of the robot.
	 */
	private final Point DEFAULT_PT;
	
	/**
	 * Defines the speed multiplier for the robot's jog and move to motion.
	 */
	private int liveSpeed;
	
	/**
	 * The index corresponding to the active end effector in EE_LIST.
	 */
	private int activeEEIdx;
	
	/**
	 * The execution state of the active program.
	 */
	private ProgExecution progExecState;
	
	/**
	 * The index of the active program, in the robot's list of programs. A
	 * value of -1 indicates that no program is active.
	 */
	private int activeProgIdx;
	
	/**
	 * The index of the active instruction in the active program's list of
	 * instructions. A value of -1 indicates that no instruction is active.
	 */
	private int activeInstIdx;
	
	/**
	 * The rogot's current motion state.
	 */
	private RobotMotion motion;
	
	/**
	 * The current coordinate frame of the robot.
	 */
	private CoordFrame curCoordFrame;
	
	/**
	 * An index corresponding to the active frame for this robot.
	 */
	private int activeUserIdx, activeToolIdx;
	
	/**
	 * A reference for the part current held by the robot.
	 */
	private Part heldPart;
	
	/**
	 * Defines the last orientation and position of the robot's tool tip.
	 */
	private RMatrix lastTipTMatrix;
	
	/**
	 * Determines if the robot's tool tip position with be tracked and drawn.
	 */
	private boolean trace;
	
	/**
	 * Defines a set of tool tip positions that are drawn to form a trace of
	 * the robot's motion overtime.
	 */
	private ArrayList<PVector> tracePts;
	
	static {
		motorSpeed = 1000; // speed in mm/sec
	}
	
	/**
	 * Creates a robotic arm with the given ID, segment models, and end
	 * effector models. It is expected that there are 6 segment models
	 * and 7 end effector models.
	 * 
	 * @param rid				The ID of this robot, which must be unique
	 * 							amongst all robots
	 * @param basePos			The position of the robot's base segment
	 * @param segmentModels		The list of models for the robot's segment
	 * @param endEffectorModels	The list of models for the robot's end effectors
	 */
	public RoboticArm(int rid, PVector basePos, MyPShape[] segmentModels,
			MyPShape[] endEffectorModels) {
		
		
		RID = rid;
		liveSpeed = 10;
		BASE_POSITION = basePos;
		
		SEG_OBB_CHECKS = new int[] {
			0, 0, 2, 1,
			0, 0, 2, 2,
			0, 0, 3, 1,
			0, 0, 3, 2,
			1, 0, 2, 1,
			1, 0, 3, 2,
			1, 1, 3, 2,
			2, 0, 3, 1,
			2, 0, 3, 2
		};
		
		EE_SEG_OBB_CHECKS = new int[] {
				0, 0, 1, 0, 1, 1, 2, 0, 2, 1, 3, 1	
		};
		
		// Define the robot's segments
		SEGMENT = new RSegWithJoint[6];
		
		SEGMENT[0] = new RSegWithJoint(
			segmentModels[0],
			new BoundingBox[] { new BoundingBox(405, 105, 405) },
			/*0.0436f,*/ new PVector(-200f, -163f, -200f),
			new PVector(0f, 1f, 0f)
		);
		
		SEGMENT[1] = new RSegWithJoint(
			segmentModels[1],
			new BoundingBox[] {
					new BoundingBox(305, 80, 305),
					new BoundingBox(114, 98, 160)
			},
			/*0.0436f,*/ 4.34f, 2.01f, new PVector(-37f, -137f, 30f),
			new PVector(0f, 0f, -1f)
		);
		
		SEGMENT[2] = new RSegWithJoint(
			segmentModels[2],
			new BoundingBox[] {
					new BoundingBox(130, 120, 160),
					new BoundingBox(130, 316, 64),
					new BoundingBox(110, 163, 48)
			},
			/*0.0582f,*/ 1.955f, 1.134f, new PVector(-3f, -498f, -200f),
			new PVector(0f, 0f, -1f)
		);
		
		SEGMENT[3] = new RSegWithJoint(
			segmentModels[3],
			new BoundingBox[] {
					new BoundingBox(154, 154, 154),
					new BoundingBox(420, 126, 126),
					new BoundingBox(148, 154, 154),
			},
			/*0.0727f,*/ new PVector(-650f, 30f, 75f),
			new PVector(1f, 0f, 0f)
		);
		
		SEGMENT[4] = new RSegWithJoint(
			segmentModels[4],
			new BoundingBox[0],
			/*0.0727f,*/ 4.189f, 2.269f, new PVector(65f, 0f, 0f),
			new PVector(0f, 0f, -1f)
		);
		
		SEGMENT[5] = new RSegWithJoint(
			segmentModels[5],
			new BoundingBox[0],
			/*0.1222f,*/ new PVector(-95f, 0f, 0f),
			new PVector(-1f, 0f, 0f)
		);
		
		// Set default speed modifiers
		SEGMENT[0].setSpdMod(150f * PConstants.DEG_TO_RAD / 60f);
		SEGMENT[1].setSpdMod(150f * PConstants.DEG_TO_RAD / 60f);
		SEGMENT[2].setSpdMod(200f * PConstants.DEG_TO_RAD / 60f);
		SEGMENT[3].setSpdMod(250f * PConstants.DEG_TO_RAD / 60f);
		SEGMENT[4].setSpdMod(250f * PConstants.DEG_TO_RAD / 60f);
		SEGMENT[5].setSpdMod(420f * PConstants.DEG_TO_RAD / 60f);
		
		//Define the robot's end effectors
		EE_LIST = new EndEffector[6];
		
		EE_LIST[0] = new EndEffector(endEffectorModels[0], new BoundingBox[0],
				new BoundingBox[0], 0, "FACEPLATE");
		
		EE_LIST[1] = new EndEffector(
				endEffectorModels[1],
				new BoundingBox[] {
						new BoundingBox(26, 92, 92),
						new BoundingBox(84, 33, 33),
						new BoundingBox(32, 66, 32)
				},
				new BoundingBox[] {
						new BoundingBox(3, 25, 25),
						new BoundingBox(25, 3, 25)
				},
				1, "SUCTION"
		);
		
		EE_LIST[2] = new EndEffector(
				new MyPShape[] {
						endEffectorModels[2],
						endEffectorModels[3],
				},
				new BoundingBox[] {
						new BoundingBox(26, 92, 92),
						new BoundingBox(29, 18, 83),
						new BoundingBox(29, 18, 83)
				},
				new BoundingBox[] { new BoundingBox(15, 3, 55) },
				2, "GRIPPER"
		);
		
		EE_LIST[3] = new EndEffector( endEffectorModels[4], new BoundingBox[0],
				new BoundingBox[0], 3, "POINTER");
		
		EE_LIST[4] = new EndEffector(endEffectorModels[5], new BoundingBox[0],
				new BoundingBox[0], 4, "GLUE GUN");
		
		EE_LIST[5] = new EndEffector(endEffectorModels[6], new BoundingBox[0],
				new BoundingBox[0], 5, "WIELDER");
		
		activeEEIdx = 0;
		
		// Initialize program fields
		PROGRAM = new ArrayList<>();
		CALL_STACK = new Stack<>();
		PROG_UNDO = new Stack<>();
		
		motion = null;
		progExecState = new ProgExecution();
		activeProgIdx = -1;
		activeInstIdx = -1;
		
		// Initializes the frames
		
		TOOL_FRAME = new ToolFrame[Fields.FRAME_NUM];
		USER_FRAME = new UserFrame[Fields.FRAME_NUM];
		
		for (int idx = 0; idx < TOOL_FRAME.length; ++idx) {
			TOOL_FRAME[idx] = new ToolFrame();
			USER_FRAME[idx] = new UserFrame();
		}
		
		curCoordFrame = CoordFrame.JOINT;
		activeUserIdx = -1;
		activeToolIdx = -1;
		
		// Initialize the registers
		
		DREG = new DataRegister[Fields.DPREG_NUM];
		PREG = new PositionRegister[Fields.DPREG_NUM];
		
		for (int idx = 0; idx < DREG.length; ++idx) {
			DREG[idx] = new DataRegister(idx);
			PREG[idx] = new PositionRegister(idx);
		}

		heldPart = null;
		
		DEFAULT_PT = getFacePlatePoint(
				new float[] { 0f, 0f, 0f, 0f, 0f, 0f }
		);
		
		// Initializes the old transformation matrix for the arm model
		lastTipTMatrix = getFaceplateTMat( getJointAngles() );
		trace = false;
		tracePts = new ArrayList<PVector>();
	}
	
	/**
	 * Adds the given program to this Robot's list of programs.
	 * 
	 * @param p	The program to add to the Robot
	 */
	public int addProgram(Program p) {
		if (p == null) {
			return -1;
			
		} else {
			int idx = 0;

			if(PROGRAM.size() < 1) {
				PROGRAM.add(p);
				
			}  else {
				while(idx < PROGRAM.size() && PROGRAM.get(idx).getName().compareTo(p.getName()) < 0) { ++idx; }
				PROGRAM.add(idx, p);
			}

			return idx;
		}
	}
	
	/**
	 * Converts the given point, pt, into the Coordinate System defined by the
	 * given origin vector and rotation quaternion axes. The joint angles
	 * associated with the point will be transformed as well, though, if inverse
	 * kinematics fails, then the original joint angles are used instead.
	 * 
	 * @param pt
	 *            A point with initialized position and orientation
	 * @param origin
	 *            The origin of the Coordinate System
	 * @param axes
	 *            The axes of the Coordinate System representing as a rotation
	 *            quanternion
	 * @returning The point, pt, in terms of the given coordinate system
	 */
	public Point applyFrame(Point pt, PVector origin, RQuaternion axes) {
		PVector position = RMath.vToFrame(pt.position, origin, axes);
		RQuaternion orientation = axes.transformQuaternion(pt.orientation);
		
		// If inverse kinematics fails use the old angles
		return new Point(position, orientation, pt.angles);
	}
	
	/**
	 * Checks if the robot's active end effector can pickup up the given part.
	 * 
	 * @param p	The part with which to check for a pickup collision
	 * @return	If p can be picked up right now
	 */
	public boolean canPickup(Part p) {
		return getActiveEE().canPickup(p);
	}
	
	/**
	 * Checks for any collisions between the robot's bounding boxes and that of
	 * the given part. If a collision is detected between the given part and a
	 * bounding box of the robot, then the color of the robot's bounding box is
	 * updated accordingly.
	 * 
	 * @param p	The part with which to check collision with this robot
	 * @return	If at least one collision is detected between the given part
	 * 			and one of the robot's bounding boxes
	 */
	public boolean checkCollision(Part p) {
		boolean collision = false;
		// Check each segments OBBs
		for (RSegment seg : SEGMENT) {
			if (seg.checkCollision(p) == 1) {
				collision = true;
			}
		}
		// Check active end effector OBBs
		if (getActiveEE().checkCollision(p) == 1) {
			collision = true;
		}

		return collision;
	}
	
	/**
	 * Checks all objects in the given scenario to determine if a part in the
	 * scenario can picked up by the robot or a part in the scenario is
	 * currently being held by the robot. If the robot is not carrying a part
	 * and a part can be picked up by the robot, then the robot will pickup the
	 * part. If the robot is currently carrying a part, then it will release
	 * that part, instead.
	 * 
	 * @param scenario	The scenario, of which to check the world objects
	 * @return			0, if an part can be picked up
	 * 					1, if the robot releases a part
	 * 					2, if nothing occurs
	 */
	public int checkPickupCollision(Scenario scenario) {
		
		if (scenario != null) {
			EndEffector activeEE = getActiveEE();
			
			/* End Effector must be on and no object is currently held to be
			 * able to pickup an object */
			if (activeEE.canPickup() && activeEE.getState() == Fields.ON
					&& heldPart == null) {
				
				for (WorldObject wldObj : scenario) {
					// Only parts can be picked up
					if (wldObj instanceof Part && activeEE.canPickup((Part)wldObj)) {
						// Pickup the object
						heldPart = (Part)wldObj;
						return 0;
					}
				}

			} else if (activeEE.getState() == Fields.OFF && heldPart != null) {
				// Release the object
				releaseHeldObject();
				return 1;
			}
		}

		return 2;
	}
	

	/**
	 * Checks collisions between the bounding boxes of the robot's segments and
	 * end effectors. The colors of the robot's bounding boxes are updated
	 * based on the results of the collision detection as well.
	 * 
	 * NOTE: due to the lack of fit of some bounding boxes to parts of the
	 * robot, not all bounding box pairs are checked for collisions. Only
	 * bounding box pairs specified in the SEG_OBB_CHECK and EE_SEG_OBB_CHECK
	 * arrays are checked.
	 * 
	 * @return	If at least one collision occurred between two of the robot's
	 * 			bounding boxes
	 */
	public boolean checkSelfCollisions() {
		boolean selfCollision = false;
		
		// Check each specified pair of segment bounding boxes
		for (int cdx = 3; cdx < SEG_OBB_CHECKS.length; cdx += 4) {
			BoundingBox oob0 = SEGMENT[SEG_OBB_CHECKS[cdx - 3]]
					.OBBS[SEG_OBB_CHECKS[cdx - 2]];
			BoundingBox oob1 = SEGMENT[SEG_OBB_CHECKS[cdx - 1]]
					.OBBS[SEG_OBB_CHECKS[cdx]];
			
			if (Part.collision3D(oob0, oob1)) {
				// Update OBB colors
				oob0.setColor(Fields.OBB_COLLISION);
				oob1.setColor(Fields.OBB_COLLISION);
				
				selfCollision = true;
			}
		}
		
		EndEffector activeEE = getActiveEE();
		/* Check collisions between the active EE's OBBs and a specified set of
		 * the robot segment OBBs. */
		for (BoundingBox obb : activeEE.OBBS) {
			// Check the specific segment OBB with EE OBB
			for (int cdx = 1; cdx < EE_SEG_OBB_CHECKS.length; cdx += 2) {
				BoundingBox segOBB = SEGMENT[EE_SEG_OBB_CHECKS[cdx - 1]]
						.OBBS[EE_SEG_OBB_CHECKS[cdx]];
				
				if (Part.collision3D(obb, segOBB)) {
					// Update OBB colors
					obb.setColor(Fields.OBB_COLLISION);
					segOBB.setColor(Fields.OBB_COLLISION);
					
					selfCollision = true;
				}
			}
			
		}
		
		return selfCollision;
	}
	
	/**
	 * Removes all saved program states from the call stack of this robot.
	 */
	public void clearCallStack() {
		CALL_STACK.clear();
	}
	
	/**
	 * Checks if the given ray collides with any of the robot's bounding boxes.
	 * If the ray does collides with a bounding box, then the position of the
	 * collision, which is closest the ray's origin is returned.
	 * 
	 * @param ray	A ray with a defined position and direction
	 * @return		The closest collision point with a robot bounding box
	 */
	public PVector closestCollision(RRay ray) {
		PVector closestCollPt = null;
		
		for (RSegment seg : SEGMENT) {
			closestCollPt = seg.closestCollision(ray);
		}
		
		EndEffector activeEE = getActiveEE();
		PVector collPt = activeEE.closestCollision(ray);
		
		if (collPt != null && (closestCollPt == null ||
				PVector.dist(ray.getOrigin(), collPt) <
				PVector.dist(ray.getOrigin(), closestCollPt))) {
			
			// Find the closest collision to the ray origin
			closestCollPt = collPt;
		}
		
		return closestCollPt;
	}
	
	/**
	 * Transitions from the current End Effector
	 * to the next End Effector in a cyclic pattern:
	 * 
	 * NONE -> SUCTION -> CLAW -> POINTER -> GLUE_GUN -> WIELDER -> NONE
	 */
	public void cycleEndEffector() {
		// Cycle through range [0, EE_LIST.length - 1]
		activeEEIdx = (activeEEIdx + 1) % (EE_LIST.length);
		setActiveEE(activeEEIdx);
	}
	
	public void clearTrace() {
		tracePts.clear();
	}
	
	/**
	 * Draws the robotic arm along with its bounding boxes and active
	 * coordinate frame axes.
	 * 
	 * @param g			The graphics used to render the robot
	 * @param drawOBBs	Whether to render the bounding boxes of the robot
	 * @param axesType	Defines how to draw the axes of the active coordinate
	 * 					frame of the robot
	 */
	public void draw(PGraphics g, boolean drawOBBs, AxesDisplay axesType) {
		
		float[] jointAngles = getJointAngles();
		
		/* DRAW ROBOT SEGMENT */
		
		g.pushStyle();
		g.noStroke();

		g.pushMatrix();
		
		if (axesType != AxesDisplay.NONE && curCoordFrame == CoordFrame.USER) {
			
			UserFrame activeUser = getActiveUser();
			// Render the active user frame
			if (activeUser != null) {
				RMatrix userAxes = RMath.rMatToWorld(activeUser.getNativeAxisVectors());
				
				if (axesType == AxesDisplay.AXES) {
					Fields.drawAxes(g, activeUser.getOrigin(), userAxes, 10000f, Fields.ORANGE);
					
				} else if (axesType == AxesDisplay.GRID) {
					g.pushMatrix();
					// Draw the gridlines at the base of the robot
					g.translate(0f, BASE_POSITION.y, 0f);
					drawGridlines(g, userAxes, activeUser.getOrigin(), 35, 100);
					g.popMatrix();
				}
			}
		}
		
		// Base position
		g.translate(BASE_POSITION.x, BASE_POSITION.y, BASE_POSITION.z);
		
		g.rotateZ(PConstants.PI);
		g.rotateY(PConstants.HALF_PI);
		// Base segment
		g.shape(SEGMENT[0].MODEL_SET[0]);
		g.rotateY(-PConstants.HALF_PI);
		g.rotateZ(-PConstants.PI);
		
		// Translate for the first joint segment
		g.translate(-50f, -163f, -350f);
		
		g.translate(-150f, 0f, 150f);
		// First joint axis
		g.rotateY(jointAngles[0]);
		g.translate(150f, 0f, -150f);
		
		g.rotateX(PConstants.PI);
		g.rotateY(PConstants.PI);
		// First joint segment
		g.shape(SEGMENT[1].MODEL_SET[0]);
		g.rotateY(-PConstants.PI);
		g.rotateX(-PConstants.PI);
		
		// Translate for the second joint segment
		g.translate(-125f, -75f, 180f);
		
		g.translate(-62f, -62f, 0f);
		// Second joint axis
		g.rotateZ(-jointAngles[1]);
		g.translate(62f, 62f, 0f);
		
		g.rotateZ(PConstants.PI);
		g.rotateY(PConstants.HALF_PI);
		// Second joint segment
		g.shape(SEGMENT[2].MODEL_SET[0]);
		g.rotateY(-PConstants.HALF_PI);
		g.rotateZ(-PConstants.PI);
		
		// Translate for the third joint segment
		g.translate(10f, -605f, -200f);
		
		g.translate(-75f, 45f, 0f);
		// Third joint axis
		g.rotateZ(-jointAngles[2]);
		g.translate(75f, -45f, 0f);
		
		g.rotateY(-PConstants.HALF_PI);
		// Third joint segment
		g.shape(SEGMENT[3].MODEL_SET[0]);
		g.rotateY(PConstants.HALF_PI);
		
		// Translate for the fourth joint segment
		g.translate(-725f, 0f, 0f);
				
		g.translate(0f, 75f, 75f);
		// Fourth joint axis
		g.rotateX(jointAngles[3]);
		g.translate(0f, -75f, -75f);
		
		g.rotateZ(-PConstants.HALF_PI);
		g.rotateY(-PConstants.HALF_PI);
		// Fourth joint segment
		g.shape(SEGMENT[4].MODEL_SET[0]);
		g.rotateY(PConstants.HALF_PI);
		g.rotateZ(PConstants.HALF_PI);
		
		// Translation for the fifth joint segment
		g.translate(120f, 20f, 25f);
		
		g.translate(-55f, 55f, 50f);
		// Fifth joint axis
		g.rotateZ(-jointAngles[4]);
		g.translate(55f, -55f, -50f);

		g.rotateY(-PConstants.HALF_PI);
		// Fifth joint segment
		g.shape(SEGMENT[5].MODEL_SET[0]);
		g.rotateY(PConstants.HALF_PI);
		
		// Translation for the sixth joint segment
		g.translate(-150f, 10f, 95f);
		
		g.translate(0f, 45f, -45f);
		// Sixth joint axis
		g.rotateX(-jointAngles[5]);
		
		/* DRAW END EFFECTOR MODEL */
		g.pushMatrix();
		g.translate(0f, -45f, 45f);
		
		EndEffector activeEE = getActiveEE();
		
		if (activeEEIdx == 0) {
			// Faceplate
			g.rotateY(PConstants.HALF_PI);
			g.shape(EE_LIST[0].MODEL_SET[0]);
			
		} else if (activeEEIdx == 1) {
			// Suction
			g.translate(25, -37.5f, -90);
			
			g.rotateY(-PConstants.HALF_PI);
			g.shape(EE_LIST[1].MODEL_SET[0]);
			g.rotateY(PConstants.HALF_PI);
			
		} else if(activeEEIdx == 2) {
			// Gripper
			g.translate(25, 0, -90);
			
			g.rotateY(-PConstants.HALF_PI);
			g.shape(EE_LIST[2].MODEL_SET[0]);
			g.rotateY(PConstants.HALF_PI);
			
			float firstGripper, secondGripper;
			
			if(activeEE.getState() == Fields.ON) {
				// Draw closed grippers
				firstGripper = 22.5f;
				secondGripper = 25f;

			} else {
				// Draw open grippers
				firstGripper = 10f;
				secondGripper = 55f;
			}
			
			g.translate(-32.5f, firstGripper, 85);
			
			g.rotateY(-PConstants.HALF_PI);
			g.rotateZ(PConstants.HALF_PI);
			g.shape(EE_LIST[2].MODEL_SET[1]);
			g.rotateZ(-PConstants.HALF_PI);
			g.rotateY(PConstants.HALF_PI);
			
			g.translate(0, secondGripper, 0);
			
			g.rotateY(-PConstants.HALF_PI);
			g.rotateZ(PConstants.HALF_PI);
			g.shape(EE_LIST[2].MODEL_SET[1]);
			g.rotateZ(-PConstants.HALF_PI);
			g.rotateY(PConstants.HALF_PI);
			
		} else if (activeEEIdx == 3) {
			// Pointer
			g.translate(20, 45, -45);
			g.rotateY(-PConstants.HALF_PI);
			g.shape(EE_LIST[3].MODEL_SET[0]);

		} else if (activeEEIdx == 4) {
			// Glue Gun
			g.translate(20, 45, -45);
			g.rotateY(PConstants.HALF_PI);
			g.shape(EE_LIST[4].MODEL_SET[0]);

		} else if (activeEEIdx == 5) {
			// Wielder
			g.translate(20, 45, -45);
			g.rotateY(-PConstants.HALF_PI);
			g.shape(EE_LIST[5].MODEL_SET[0]);
		}
		
		g.popMatrix();
		
		/* DRAW TOOL TIP */
		
		drawToolTip(g, axesType);

		g.popMatrix();
		g.popStyle();
		
		if (drawOBBs) {
			for (RSegWithJoint seg : SEGMENT) {
				// Draw each segment's OBBs
				for (BoundingBox obb : seg.OBBS) {
					g.pushMatrix();
					Fields.transform(g, obb.getCenter(), obb.getOrientationAxes());
					obb.getFrame().draw(g);
					g.popMatrix();
				}
				
			}
			
			// Draw the active End Effector's OBBs
			
			for (BoundingBox obb : activeEE.OBBS) {
				g.pushMatrix();
				Fields.transform(g, obb.getCenter(), obb.getOrientationAxes());
				obb.getFrame().draw(g);
				g.popMatrix();
			}
			
			for (BoundingBox obb : activeEE.PICKUP_OBBS) {
				g.pushMatrix();
				Fields.transform(g, obb.getCenter(), obb.getOrientationAxes());
				obb.getFrame().draw(g);
				g.popMatrix();
			}
		}
		
		if (inMotion() && trace) {
			Point tipPosNative = getToolTipNative();
			// Update the robots trace points
			if(tracePts.isEmpty()) {
				tracePts.add(tipPosNative.position);
				
			} else {
				PVector lastTracePt = tracePts.get(tracePts.size() - 1);
				
				if (PVector.sub(tipPosNative.position, lastTracePt).mag()
						> 0.5f) {
					
					tracePts.add(tipPosNative.position);
				}
			}
		}
		
		if (trace) {
			drawTrace(g);
		}
	}
	
	
	
	/**
	 * Draws the position of the robot's tool tip with respect to the
	 * coordinate system of the given graphics object. If the robot has an
	 * active tool frame, then its axes will be drawn at the position of the
	 * tool tip. Otherwise only a pink sphere will be drawn at the tool tip
	 * position.
	 * 
	 * @param g			The graphics object used to render the tool tip position
	 * @param axesType	What is the current axes render method (none, axes, or
	 * 					grid)
	 */
	private void drawToolTip(PGraphics g, AxesDisplay axesType) {
		
		ToolFrame activeTool = getActiveTool();
		
		if (axesType != AxesDisplay.NONE && curCoordFrame == CoordFrame.TOOL
				&& activeTool != null) {
			
			// Render the active tool frame at the position of the tooltip
			RMatrix toolAxes = RMath.rMatToWorld(activeTool.getOrientationOffset().toMatrix());
			Fields.drawAxes(g, activeTool.getTCPOffset(), toolAxes, 500f, Fields.PINK);
			
		} else {
			// Render a point at the position of the tooltip
			g.pushStyle();
			g.stroke(Fields.PINK);
			g.noFill();
			
			g.pushMatrix();
			
			if (activeTool != null) {
				// Apply active tool frame offset
				PVector tipPos = activeTool.getTCPOffset();
				g.translate(tipPos.x, tipPos.y, tipPos.z);
			}
			
			g.sphere(4);
			
			g.popMatrix();
			g.popStyle();
		}
	}
	
	/**
	 * Draws gridlines, centered around the coordinate frame defined by the
	 * given axes and origin position, mapped to the y-axis of the graphics
	 * coordinate frame.
	 * 
	 * @param g					The graphics object used to render the gridlines
	 * @param axesVectors		The axes of the coordinate frame to draw
	 * @param origin			The origin of the coordinate frame to draw
	 * @param halfNumOfLines	Half the number of lines drawn for the each
	 * 							mapped axis
	 * @param distBwtLines		The spacing between between lines drawn for an
	 * 							axis
	 */
	public void drawGridlines(PGraphics g, RMatrix axesVectors, PVector origin,
			int halfNumOfLines, float distBwtLines) {
		
		float[][] axesDat = axesVectors.getDataF();
		int vectorPX = -1, vectorPZ = -1;

		// Find the two vectors with the minimum y values
		for (int v = 0; v < axesDat.length; ++v) {
			int limboX = (v + 1) % axesDat.length;
			int limboY = (limboX + 1) % axesDat.length;
			// Compare the y value of the current vector to those of the other
			// two vectors
			if (Math.abs(axesDat[1][v]) >= Math.abs(axesDat[1][limboX])
					&& Math.abs(axesDat[1][v]) >= Math.abs(axesDat[1][limboY])) {
				vectorPX = limboX;
				vectorPZ = limboY;
				break;
			}
		}

		if (vectorPX == -1 || vectorPZ == -1) {
			Fields.debug("Invalid axes-origin pair for grid lines!");
			return;
		}

		g.pushMatrix();
		// Map the chosen two axes vectors to the xz-plane at the y-position of
		// the Robot's base
		g.applyMatrix(
				axesDat[0][vectorPX], 0, axesDat[0][vectorPZ], origin.x,
				0, 1, 0, 0,
				axesDat[2][vectorPX], 0, axesDat[2][vectorPZ], origin.z,
				0, 0, 0, 1
		);

		float lineLen = halfNumOfLines * distBwtLines;

		// Draw axes lines in red
		g.stroke(255, 0, 0);
		g.line(-lineLen, 0, 0, lineLen, 0, 0);
		g.line(0, 0, -lineLen, 0, 0, lineLen);
		// Draw remaining gridlines in black
		g.stroke(25, 25, 25);
		for (int linePosScale = 1; linePosScale <= halfNumOfLines; ++linePosScale) {
			g.line(distBwtLines * linePosScale, 0, -lineLen, distBwtLines * linePosScale, 0, lineLen);
			g.line(-lineLen, 0, distBwtLines * linePosScale, lineLen, 0, distBwtLines * linePosScale);

			g.line(-distBwtLines * linePosScale, 0, -lineLen, -distBwtLines * linePosScale, 0, lineLen);
			g.line(-lineLen, 0, -distBwtLines * linePosScale, lineLen, 0, -distBwtLines * linePosScale);
		}

		g.popMatrix();
	}
	
	/**
	 * Draws the points stored for this robot's trace function with respect to
	 * the given graphics object's coordinate frame.
	 * 
	 * @param g	The graphics object used to drawn the trace
	 */
	private void drawTrace(PGraphics g) {		
		if (tracePts.size() > 1) {
			PVector lastPt = tracePts.get(0);
			
			g.pushStyle();
			g.stroke(0);
			g.strokeWeight(3);
			
			for(int i = 1; i < tracePts.size(); i += 1) {
				PVector curPt = tracePts.get(i);
				
				g.line(lastPt.x, lastPt.y, lastPt.z, curPt.x, curPt.y, curPt.z);
				
				lastPt = curPt;
			}
			
			g.popStyle();
		}
	}
	
	/**
	 * @return	The index of the active end effector
	 */
	public int getActiveEEIdx() {
		return activeEEIdx;
	}
	
	/**
	 * @return	The active end effector segment, or null if no end effector is
	 * 			active
	 */
	private EndEffector getActiveEE() {
		return EE_LIST[activeEEIdx];
	}
	
	/**
	 * @return	The index of the active program's active instruction
	 */
	public int getActiveInstIdx() {
		return activeInstIdx;
	}

	/**
	 * @return	The active instruction of the active program, or null if no
	 * 			program is active
	 */
	public Instruction getActiveInstruction() {
		Program prog = getActiveProg();
		
		if (prog == null || activeInstIdx < 0 || activeInstIdx >= prog.size()) {
			// Invalid instruction or program index
			return null;
		}
		
		return prog.get(activeInstIdx);
	}
	
	/**
	 * @return	The active for this Robot, or null if no program is active
	 */
	public Program getActiveProg() {
		if (activeProgIdx < 0 || activeProgIdx >= PROGRAM.size()) {
			// Invalid program index
			return null;
		}
		
		return PROGRAM.get(activeProgIdx);
	}

	/**
	 * @return	The index of the active program
	 */
	public int getActiveProgIdx() {
		return activeProgIdx;
	}
	
	/**
	 * @return	The active tool frame or null if no tool frame is active
	 */
	public ToolFrame getActiveTool() {
		
		if (activeToolIdx == -1) {
			return null;
		}
		
		return TOOL_FRAME[activeToolIdx];
	}
	
	/**
	 * @return	The ID for the Robot's active tool frame
	 */
	public int getActiveToolIdx() {
		return activeToolIdx;
	}
	
	/**
	 * @return	The active tool frame or null if no tool frame is active
	 */
	public UserFrame getActiveUser() {
		
		if (activeUserIdx == -1) {
			return null;
		}
		
		return USER_FRAME[activeUserIdx];
	}
	
	/**
	 * @return	The ID for the Robot's active user frame
	 */
	public int getActiveUserIdx() {
		return activeUserIdx;
	}

	/**
	 * @return	A copy of the position of the center of the Robot's base
	 * 			segment
	 */
	public PVector getBasePosition() {
		return BASE_POSITION.copy();
	}
	
	/**
	 * @return	The current coordinate frame of the Robot
	 */
	public CoordFrame getCurCoordFrame() {
		return curCoordFrame;
	}
	
	/**
	 * @return	A copy of the Robot's default position and orientation
	 */
	public Point getDefaultPoint() {
		return DEFAULT_PT.clone();
	}
	
	/**
	 * Returns the data register, associated with the given index, of the
	 * Robot, or null if the given index is invalid. A Robot has a total of 100
	 * data registers, which are zero-indexed.
	 * 
	 * @param rdx	A integer value between 0 and 99, inclusive
	 * @return		The data register associated with the given index, or null
	 * 				if the given index is invalid.
	 */
	public DataRegister getDReg(int rdx) {
		if (rdx >= 0 && rdx < DREG.length) {
			return DREG[rdx];
			
		} else {
			// Invalid index
			return null;
		}
	}
	
	/**
	 * @return	The state of the robot's current end effector
	 */
	public int getEEState() {
		return this.getActiveEE().getState();
	}
	
	/**
	 * @return	A point representing the robot's current face plate position
	 * 			and orientation
	 */
	public Point getFacePlatePoint() {
		return getFacePlatePoint( getJointAngles() );
	}
	
	/**
	 * Calculates the position and orientation of the robot's face plate based
	 * on the native coordinate system and the given joint angles.
	 * 
	 * @jointAngles	The angles used to calculate the robot's face plate point
	 * @return		A point representing the robot faceplate's position and
	 * 				orientation
	 */
	public Point getFacePlatePoint(float[] jointAngles) {
		RMatrix tipOrien = getFaceplateTMat(jointAngles);
		
		// Convert the orientation into the correct format for a Point
		PVector position = new PVector(
				 (float)tipOrien.getEntry(0, 3),
				 (float)tipOrien.getEntry(1, 3),
				 (float)tipOrien.getEntry(2, 3)
		);
		
		RQuaternion orientation = RMath.matrixToQuat(tipOrien);
		
		return new Point(position, orientation, jointAngles);
	}
	
	/**
	 * Computes the transformation matrix, which represents the position and
	 * orientation of the robot's face plate for the given set of joint angles.
	 * 
	 * @param jointAngles	The joint angles used to calculate the robot face
	 * 						plate's position and orientation
	 * @return				The robot face plate's transformation matrix
	 */
	public RMatrix getFaceplateTMat(float[] jointAngles) {
		RMatrix limbo = RMath.formTMat(BASE_POSITION);
		
		for (int jdx = 0; jdx < 6; ++jdx) {
			// Apply each set of joint translations and rotations
			RMath.translateTMat(limbo, SEGMENT[jdx].TRANSLATION);
			
			RMatrix jointOrien = RMath.formRMat(SEGMENT[jdx].AXIS, jointAngles[jdx]);
			limbo = limbo.multiply( RMath.fromTMat(jointOrien) );
		}
		
		return limbo;
	}
	
	/**
	 * A wrapper method for getting an instruction from the active program. A
	 * copy of the instruction is placed on the program undo stack for this
	 * active program.
	 * 
	 * NOTE: only use this method, if you intend to edit the instruction
	 * 		 returned by this method!!!!
	 * 
	 * @param idx	The index of the instruction in the active program's list
	 * 				of instructions
	 * @return		The instruction at the given index, in the active program's
	 * 				list of instructions
	 */
	public Instruction getInstToEdit(int idx) {
		Program p = getActiveProg();
		
		// Valid active program and instruction index
		if (p != null && idx >= 0 && idx < p.getNumOfInst()) {
			Instruction inst = p.get(idx);
			
			pushInstState(InstOp.REPLACED, idx, inst.clone());
			
			if (Fields.DEBUG) {
				//System.out.printf("\nEDIT %d %s\n\n", idx, inst.getClass());
			}
			
			return inst;
		}
		
		return null;
	}

	/**
	 * Returns the end effector's I/O register associated with the given index.
	 * 
	 * @param rdx	The index of the I/O register [0, EE_LISTlengt)
	 * @return		The associated I/O register or null if index is invalid
	 */
	public IORegister getIOReg(int rdx) {
		// Disclude the faceplate
		if (rdx >= 0 && rdx < (EE_LIST.length - 1)) {
			return EE_LIST[rdx + 1].reg;
		}
		
		return null;
	}

	/**
	 * @return	A 6-element array containing the robot's current joint angles
	 */
	public float[] getJointAngles() {
		return new float[] {
			SEGMENT[0].getJointRotation(),
			SEGMENT[1].getJointRotation(),
			SEGMENT[2].getJointRotation(),
			SEGMENT[3].getJointRotation(),
			SEGMENT[4].getJointRotation(),
			SEGMENT[5].getJointRotation()
		};
	}
	
	/**
	 * Returns the directions of the robot's current jog motion, or null if the
	 * robot is not jogging.
	 * 
	 * @return	A 6 element array where each entry is one of the robot's jog
	 * 			motion directions
	 */
	public int[] getJogMotion() {
		
		if (motion instanceof JointJog) {
			// Joint jog motion
			return ((JointJog) motion).getJogMotion();
			
		} else if (motion instanceof LinearJog) {
			// Linear jog motion
			return ((LinearJog) motion).getJogMotion();
		}
		
		// No jog motion
		return null;
	}

	public RMatrix getLastTipTMatrix() {
		return lastTipTMatrix;
	}

	public int getLiveSpeed() {
		return liveSpeed;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param mInst
	 * @param parent
	 * @return			A copy of the point associated with the given motion
	 * 					instruction
	 */
	public Point getPosition(MotionInstruction mInst, Program parent) {
		int posNum = mInst.getPositionNum();
		
		if (mInst.usesGPosReg()) {
			// The instruction references a global position register
			if (posNum == -1) {
				return null;	
			}
			
			return PREG[posNum].point.clone();
		}
		
		return parent.getPosition(posNum).clone();
	}
	
	/**
	 * Returns the position register, associated with the given index, of the
	 * Robot, or null if the given index is invalid. A Robot has a total of 100
	 * position registers, which are zero-indexed.
	 * 
	 * @param rdx	A integer value between 0 and 99, inclusive
	 * @return		The position register associated with the given index, or
	 * 				null if the given index is invalid.
	 */
	public PositionRegister getPReg(int rdx) {
		if (rdx >= 0 && rdx < PREG.length) {
			return PREG[rdx];
			
		} else {
			// Invalid index
			return null;
		}
	}
	
	/**
	 * Returns the program, which belongs to this Robot, associated with the
	 * given index value. IF the index value is invalid null is returned
	 * 
	 * @param pdx	A positive integer value less than the number of programs
	 * 				associated with this Robot, which corresponds to a program
	 * 				of the Robot
	 * @return		The program associated with the given index, or null if the
	 * 				index is invalid.
	 */
	public Program getProgram(int pdx) {
		if (pdx >= 0 && pdx < PROGRAM.size()) {
			return PROGRAM.get(pdx);
			
		} else {
			// Invalid index
			return null;
		}
	}

	/**
	 * Searches for the program with the given name, in this robot's list of
	 * programs. If no program with the given name exists, null is returned.
	 * 
	 * @param name	The name of the target program
	 * @return		The program with the given name, or null if no such program
	 * 				exists
	 */
	public Program getProgram(String name) {
		for (Program p : PROGRAM) {
			if (p.getName().equals(name)) {
				return p;
			}
			
		}
		
		// No such program exists
		return null;
	}
	
	/**
	 * Returns the robot segment with the specified index, in the robot's set
	 * of segments.
	 * 
	 * @param sdx	The index of a segment [0, 6)
	 * @return		The segment associated with the given index, or null, if no
	 * 				such segment exists
	 */
	protected RSegWithJoint getSegment(int sdx) {
		if (sdx >= 0 && sdx < SEGMENT.length) {
			return SEGMENT[sdx];
		}
		// Invalid segment index
		return null;
	}
	
	/**
	 * Returns the speed multiplier for the robot's motion based on its active
	 * coordinate frame state.
	 * 
	 * @return	The speed modifier for the robot's motion
	 */
	public float getSpeedForCoord() {
		if (curCoordFrame == CoordFrame.JOINT) {
			return liveSpeed / 100f;
		}
		
		return motorSpeed * liveSpeed / 100f;
	}
	
	/**
	 * Returns the tool frame, associated with the given index, of the Robot,
	 * or null if the given index is invalid. A Robot has a total of 10 tool
	 * frames, which are zero-indexed.
	 * 
	 * @param fdx	A integer value between 0 and 9, inclusive
	 * @return		The tool frame associated with the given index, or null if
	 * 				the given index is invalid.
	 */
	public ToolFrame getToolFrame(int fdx) {
		if (fdx >= 0 && fdx < TOOL_FRAME.length) {
			return TOOL_FRAME[fdx];
			
		} else {
			// Invalid index
			return null;
		}
	}
	
	/**
	 * @return	The robot's tooltip position and orientatio with respect to the
	 * 			active user frame
	 */
	public Point getToolTipUser() {
		return getToolTipPoint(getJointAngles(), getActiveTool(),
				getActiveUser());
	}
	
	/**
	 * @return	The robot's tooltip position and orientation in native
	 * 			coordinates
	 */
	public Point getToolTipNative() {
		return getToolTipPoint(getJointAngles(), getActiveTool(), null);
	}
	
	/**
	 * Calculates the robot's tooltip position and orientation based off
	 * the given joint angles and the robot's active tool frame's tooltip
	 * offset.
	 * 
	 * @param jointAngles	A 6-element array of joint angles used to
	 * 						calculate the robot's tooltip position
	 * @return				The robot's tooltip position in native coordinates
	 */
	public Point getToolTipNative(float[] jointAngles) {
		return getToolTipPoint(jointAngles, getActiveTool(), null);
	}
		
	/**
	 * Computes the position and orientation of the robot's tool tip based on
	 * the given joint angles, tool and user frame. The position offset of the
	 * tool frame defines the robot's tool tip offset, while the user frame
	 * defines the coordinate frame, for which the tool tip's position and
	 * orientation are defined. If the tool frame is null, then the tool tip
	 * offset will be (0, 0, 0) or the position of the face plate. If the user
	 * frame is null, then the position and orientation of the tool tip will be
	 * with respect to the world frame. 
	 * 
	 * @param jointAngles	The joint angles used to compute the robot's face
	 * 						plate position and orientatton
	 * @param tFrame		The frame, which defines the tool tip offset
	 * @param uFrame		The coordinate frame, with which the robot's tool
	 * 						tip position and orientation are defined 
	 * @return
	 */
	private Point getToolTipPoint(float[] jointAngles, ToolFrame tFrame,
			UserFrame uFrame) {
		
		Point toolTip = getFacePlatePoint(jointAngles);
		
		if (tFrame != null) {
			// Apply the tooltip offset of the given tool frame
			PVector toolOrigin = tFrame.getTCPOffset();
			RQuaternion invOrien = toolTip.orientation.conjugate();
			toolTip.position.add( invOrien.rotateVector(toolOrigin) );
		}
		
		if (uFrame != null) {
			// Apply the given user frame to the robot's tooltip position
			RQuaternion uOrien = uFrame.getOrientation();
			toolTip.position = RMath.vToFrame(toolTip.position,
					uFrame.getOrigin(), uOrien);
			
			toolTip.orientation = uOrien.transformQuaternion(toolTip.orientation);
		}
		
		return toolTip;
	}

	/**
	 * Returns the user frame, associated with the given index, of the Robot,
	 * or null if the given index is invalid. A Robot has a total of 10 user
	 * frames, which are zero-indexed.
	 * 
	 * @param fdx	A integer value between 0 and 9, inclusive
	 * @return		The user frame associated with the given index, or null if
	 * 				the given index is invalid.
	 */
	public UserFrame getUserFrame(int fdx) {
		if (fdx >= 0 && fdx < USER_FRAME.length) {
			return USER_FRAME[fdx];
			
		} else {
			// Invalid index
			return null;
		}
	}
	
	/**
	 * 
	 * @param mInst
	 * @param parent	
	 * @return			The modified position associated with the given motion
	 * 					instruction
	 */
	public Point getVector(MotionInstruction mInst, Program parent) {
		Point pt = getPosition(mInst, parent);
		
		if (mInst.getOffset() != -1) {
			// Apply the position offset
			Point offset = PREG[mInst.getOffset()].point;
			
			if (mInst.getMotionType() == Fields.MTYPE_JOINT) {
				pt = pt.add(offset.angles);
				
			} else {
				// TODO do circular instructions differ?
				pt = pt.add(offset.position, offset.orientation);
			}
		}
		
		if (mInst.getUserFrame() != -1) {
			// Remove the associated user frame
			UserFrame uFrame = getUserFrame(mInst.getUserFrame());
			pt = removeFrame(pt, uFrame.getOrigin(), uFrame.getOrientation());
		}
		
		return pt;
	}
	
	/**
	 * Stops all movement of this robot.
	 */
	public void halt() {
		/* TODO TEST CODE *
		try {
			
			throw new RuntimeException("HALT!");
			
		} catch (RuntimeException REx) {
			REx.printStackTrace();
		}
		/**/
		
		// Set default speed modifiers
		SEGMENT[0].setSpdMod(150f * PConstants.DEG_TO_RAD / 60f);
		SEGMENT[1].setSpdMod(150f * PConstants.DEG_TO_RAD / 60f);
		SEGMENT[2].setSpdMod(200f * PConstants.DEG_TO_RAD / 60f);
		SEGMENT[3].setSpdMod(250f * PConstants.DEG_TO_RAD / 60f);
		SEGMENT[4].setSpdMod(250f * PConstants.DEG_TO_RAD / 60f);
		SEGMENT[5].setSpdMod(420f * PConstants.DEG_TO_RAD / 60f);
		
		if (motion != null) {
			motion.halt();
		}
		
		progExecState.halt();
	}

	/**
	 * Indicates whether an issue occurred with inverse kinematics, when the
	 * robot is moving in a Cartesian reference frame.
	 * 
	 * @return	Whether the robot has a motion fault
	 */
	public boolean hasMotionFault() {
		if (motion instanceof LinearMotion) {
			return ((LinearMotion) motion).hasFault();
		}
		
		return false;
	}
	
	/**
	 * @return	Is the given part being held by the robot
	 */
	public boolean isHeld(Part p) {
		return p == heldPart;
	}
	
	/**
	 * @return	Is the robot executing its active program?
	 */
	public boolean isProgExec() {
		return !progExecState.isDone();
	}
	
	public boolean isTrace() {
		return trace;
	}

	/**
	 * Attempts to move the Robot to the given position and orientation from its current
	 * position using Inverse Kinematics.
	 * 
	 * @param destPosition     The desired position of the Robot End Effector in Native
	 *                         Coordinates
	 * @param destOrientation  The desired orientation of the Robot as a quaternion, in
	 *                         Native Coordinates
	 * @returning   1 if inverse kinematics fails or the joint angles returned
	 *              are invalid and 0 if the Robot is successfully moved to the
	 *              given position
	 */
	public int jumpTo(PVector destPosition, RQuaternion destOrientation) {
		boolean invalidAngle = false;
		float[] srcAngles = getJointAngles();
		// Calculate the joint angles for the desired position and orientation
		float[] destAngles = RMath.inverseKinematics(this, srcAngles, destPosition, destOrientation);

		// Check the destination joint angles with each joint's range of valid joint angles
		for(int joint = 0; !(destAngles == null) && joint < 6; joint += 1) {
			RSegWithJoint seg = SEGMENT[joint];
			
			if (!seg.anglePermitted(destAngles[joint])) {
				invalidAngle = true;
				
				System.err.printf("Invalid angle: J[%d] = %4.3f -> %4.3f : [%4.3f - %4.3f]\n",
						joint, getJointAngles()[joint], destAngles[joint], seg.LOW_BOUND,
						seg.UP_BOUND);
				break;
			}
		}

		// Did we successfully find the desired angles?
		if ((destAngles == null) || invalidAngle) {
			if (Fields.DEBUG && destAngles == null) {
				Point RP = getToolTipNative();
				Fields.debug("IK Failure ...\n%s -> %s\n%s -> %s\n\n",
						RP.position, destPosition, RP.orientation,
						destOrientation);
			}

			RobotRun.getInstance().triggerFault();
			return 1;
		}

		setJointAngles(destAngles);
		return 0;
	}

	/**
	 * Indicates that the Robot Arm is in motion.
	 * 
	 * @return	Whether the robot is moving in some way
	 */
	public boolean inMotion() {
		return motion != null && motion.hasMotion();
	}
	
	/**
	 * @return	The number of end effectors associated with this robot
	 */
	public int numOfEndEffectors() {
		return EE_LIST.length - 1;
	}

	/**
	 * Returns the number of programs associated with the Robot.
	 */
	public int numOfPrograms() {
		return PROGRAM.size();
	}
	
	/**
	 * Pops the program state that has been previously pushed onto the call
	 * stack. If the state points to a program on the active Robot, then the
	 * active program state is overridden with the popped one. In the other
	 * case, where an inactive Robot called the active Robot, then the active
	 * Robot then returns control to the caller Robot. 
	 * 
	 * @return	Whether or not a program state has been saved on the call stack
	 */
	public CallFrame popCallStack() {
		if (!CALL_STACK.isEmpty()) {
			CallFrame savedProgState = CALL_STACK.pop();
			return savedProgState;
		}
		
		return null;
	}
	
	/**
	 * If the robot's program undo stack is not empty, then the top
	 * modification is popped off the stack and reverted in the active program.
	 */
	public void popInstructionUndo() {
		
		if (!PROG_UNDO.isEmpty()) {
			InstState state = PROG_UNDO.pop();
			Program p = getActiveProg();
			
			if (p != null) {
				
				if (state.operation == InstOp.REPLACED) {
					// Replace the new instruction with the previous version
					p.replaceInstAt(state.originIdx, state.inst);
					
				} else if (state.operation == InstOp.REMOVED) {
					// Re-insert the removed instruction
					p.addInstAt(state.originIdx, state.inst);
					
				} else {
					System.err.printf("Invalid program state!\n", state);
				}
			}
			
		} else if (Fields.DEBUG) {
			Fields.debug("Empty program undo stack!");
		}
	}
	
	/**
	 * Returns a list of display lines, which contain the program instruction
	 * list output for the pendant display.
	 * 
	 * @return	A list of display lines for the pendant display, representing
	 * 			the list instructions in the program
	 */
	public ArrayList<DisplayLine> printProgList() {
		int size = numOfPrograms();

		//int start = start_render;
		//int end = min(start + ITEMS_TO_SHOW, size);

		ArrayList<DisplayLine> progList = new ArrayList<>();
		for(int i = 0; i < size; i += 1) {
			progList.add(new DisplayLine(i, 0, PROGRAM.get(i).getName()));
		}
		
		return progList;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param singleExec
	 */
	public void progExec(boolean singleExec) {
		progExec(activeProgIdx, activeInstIdx, singleExec);
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param instIdx
	 * @param singleExec
	 */
	public void progExec(int instIdx, boolean singleExec) {
		progExec(activeProgIdx, instIdx, singleExec);
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param progIdx
	 * @param instIdx
	 * @param singleExec
	 */
	public void progExec(int progIdx, int instIdx, boolean singleExec) {
		Program p = getProgram(progIdx);
		// Validate active indices
		if (p != null && instIdx >= 0 && instIdx < p.size()) {
			ExecType pExec = (singleExec) ? ExecType.EXEC_SINGLE
					: ExecType.EXEC_FULL;
			
			progExec(progIdx, instIdx, pExec);
		}
	}
	
	/**
	 * TODO comment this
	 */
	public void progExecBwd() {
		Program p = getProgram(activeProgIdx);
		
		if (p != null && activeInstIdx >= 1 && activeInstIdx < p.size()) {
			/* The program must have a motion instruction prior to the active
			 * instruction for backwards execution to be valid. */
			Instruction prevInst = p.get(activeInstIdx - 1);
			
			if (prevInst instanceof MotionInstruction) {
				progExec(activeProgIdx, activeInstIdx - 1, ExecType.EXEC_BWD);
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
	private void progExec(int progIdx, int instIdx, ExecType exec) {
		activeProgIdx = progIdx;
		setActiveInstIdx(instIdx);
		
		Program prog = PROGRAM.get(progIdx);
		progExecState.setExec(exec, prog, instIdx);
	}
			
	/**
	 * Pushes the active program onto the call stack and resets the active
	 * program and instruction indices.
	 * 
	 * @param r	The active robot
	 */
	public void pushActiveProg(RoboticArm r) {
		
		if (r.RID == RID && isProgExec()) {
			// Save call frame to return to the currently executing program
			CALL_STACK.push(new CallFrame(RID, activeProgIdx, activeInstIdx + 1));
		} else {
			// Save call frame to return to the caller robot's current program
			CALL_STACK.push(new CallFrame(r.RID, r.getActiveProgIdx(), r.getActiveInstIdx() + 1));
		}
		
		activeProgIdx = -1;
		activeInstIdx = -1;		
	}

	/**
	 * Pushes the given instruction and instruction index onto the robot's
	 * program undo stack. If the stack size exceeds the maximum undo size,
	 * then the oldest undo is removed from the stack
	 * 
	 * @param idx	The index of the instruction in the active program
	 * @param inst	The instruction of which to save the state
	 */
	private void pushInstState(InstOp op, int idx, Instruction inst) {
		
		if (PROG_UNDO.size() > 35) {
			PROG_UNDO.remove(0);
		}
		
		PROG_UNDO.push(new InstState(op, idx, inst));
	}
	
	/**
	 * If an object is currently being held by the Robot arm, then release it.
	 * Then, update the Robot's End Effector status and IO Registers.
	 */
	public void releaseHeldObject() {
		heldPart = null;
	}

	/**
	 * Reorders the program list of the robot, so that the programs are in
	 * alphabetical order.
	 */
	@SuppressWarnings("unchecked")
	public void reorderPrograms() {
		// Move programs to a temporary list
		ArrayList<Program> limboList = (ArrayList<Program>) PROGRAM.clone();
		PROGRAM.clear();
		
		for (int pdx = 0; pdx < limboList.size(); ++pdx) {
			int insertIdx = PROGRAM.size() - 1;
			Program toInsert = limboList.get(pdx);
			
			if (PROGRAM.size() > 0) {
				Program predecessor = PROGRAM.get(insertIdx);
				/* Search to new list from back to front to find where to
				 * insert the program */
				while (predecessor.getName().compareTo(toInsert.getName()) > 0 ) {
					
					if (--insertIdx < 0) { break; }
					predecessor = PROGRAM.get(insertIdx);
				}
			}
			
			PROGRAM.add(insertIdx + 1, toInsert);
		}
		
	}

	/**
	 * A wrapper method for replacing an instruction in the active program of
	 * this robot. The replacement is added onto the program undo stack for the
	 * active program.
	 * 
	 * @param idx	The index of the instruction to replace
	 * @param inst	The new instruction to add into the active program
	 * @return		The instruction, which was replaced by the given
	 * 				instruction
	 */
	public Instruction replaceInstAt(int idx, Instruction inst) {
		Program p = getActiveProg();
		Instruction replaced = null;
		
		// Valid active program and instruction index
		if (p != null && idx >= 0 && idx < p.getNumOfInst()) {	
			replaced = p.replaceInstAt(idx, inst);
			
			pushInstState(InstOp.REPLACED, idx, replaced.clone());
			
			if (Fields.DEBUG) {
				//System.out.printf("\nREPLACE %d %s\n\n", idx, inst.getClass());
			}
		}
		
		return replaced;
	}
	
	/**
	 * Resets all the robot segment's (and the active end effector's) bounding
	 * box colors to the default color.
	 */
	public void resetOBBColors() {
		for(RSegment seg : SEGMENT) {
			seg.resetOBBColors();
		}
		
		getActiveEE().resetOBBColors();
	}
	
	/**
	 * Converts the given point, pt, from the Coordinate System defined by the
	 * given origin vector and rotation quaternion axes.
	 * 
	 * @param pt
	 *            A point with initialized position and orientation
	 * @param origin
	 *            The origin of the Coordinate System
	 * @param axes
	 *            The axes of the Coordinate System representing as a rotation
	 *            quanternion
	 * @returning The point, pt, interms of the given coordinate system
	 */
	public Point removeFrame(Point pt, PVector origin, RQuaternion axes) {
		PVector position = RMath.vFromFrame(pt.position, origin, axes);
		RQuaternion orientation = RQuaternion.mult(pt.orientation, axes);
		
		return new Point(position, orientation, pt.angles);
	}
	
	/**
	 * A wrapper method for removing an instruction from the active program of
	 * this robot. The removal is added onto the program undo stack for the
	 * active program.
	 * 
	 * @param idx	The index of the instruction to remove
	 * @return		The instruction, which was removed
	 */
	public Instruction rmInstAt(int idx) {
		Program p = getActiveProg();
		Instruction removed = null;
		
		if (p != null && idx >= 0 && idx < p.getNumOfInst()) {
			removed = p.rmInstAt(idx);
			
			if (removed != null) {
				pushInstState(InstOp.REMOVED, idx, removed);
			}
		}
		
		return removed;
	}
	
	/**
	 * Removes the program, associated with the given index, from the Robot.
	 * 
	 * @param pdx	A positive integer value less than the number of programs
	 * 				the Robot possesses
	 * @return		The program that was removed, or null if the index given
	 * 				is invalid
	 */
	public Program rmProgAt(int pdx) {
		if (pdx >= 0 && pdx < PROGRAM.size()) {
			Program removed = PROGRAM.remove(pdx);
			setActiveProgIdx(-1);
			// Return the removed program
			return removed;
			
		} else {
			// Invalid index
			return null;
		}
	}
	
	/**
	 * Set the index of the robot's active end effector to the given index. The
	 * given index must be within the range [0, EE_LIST.length). -1 implies
	 * that no end effector is active. 
	 * 
	 * @param ee	The index of the end effector to set as active
	 */
	public void setActiveEE(int eeIdx) {
		if (eeIdx >= 0 & eeIdx < EE_LIST.length) {
			activeEEIdx = eeIdx;
			releaseHeldObject();
		}
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
			activeInstIdx = instIdx;
			return true;
		}
		else {
			activeInstIdx = -1;
			return false;
		}
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
		for (int idx = 0; idx < PROGRAM.size(); ++idx) {
			if (PROGRAM.get(idx) == active) {
				activeProgIdx = idx;
				return true;
			}
		}
		
		// Not a valid program for this robot
		return false;
	}
	
	/**
	 * Sets the active program of this Robot corresponding to the index value
	 * given.
	 * 
	 * @param progIdx	The index of the program to set as active
	 * @return			Whether the given index is valid
	 */
	public boolean setActiveProgIdx(int progIdx) {
		if (progIdx >= 0 && progIdx < PROGRAM.size()) {
			
			if (activeProgIdx != progIdx) {
				PROG_UNDO.clear();
			}
			
			// Set the active program
			activeProgIdx = progIdx;
			return true;
		}
		
		return false;
	}
	
	/**
	 * Sets this robot's active tool frame index to the given value.
	 * 
	 * @param activeToolFrame	The robot's new active tool frame index
	 */
	public void setActiveToolFrame(int activeToolFrame) {
		this.activeToolIdx = activeToolFrame;
	}
	
	/**
	 * Sets this robot's active user frame index to the given value.
	 * 
	 * @param activeToolIdx	The robot's new active user frame index
	 */
	public void setActiveUserFrame(int activeUserFrame) {
		this.activeUserIdx = activeUserFrame;
	}

	/**
	 * Update the Robot's current coordinate frame.
	 * @param newFrame	The new coordinate frame
	 */
	public void setCoordFrame(CoordFrame newFrame) {
		curCoordFrame = newFrame;
	}
	
	/**
	 * Updates the state of the robot's end effector associated with the given
	 * index.
	 * 
	 * @param rdx		The index of the I/O register
	 * @param newState	The new state of the I/O register
	 */
	public void setEEState(int rdx, int newState) {
		IORegister ioReg = getIOReg(rdx);
		
		if (ioReg != null) {
			ioReg.state = newState;
		}
	}
	
	/**
	 * Updates the robot's joint angles to the given set of joint angles.
	 * 
	 * @param newJointAngles	The robot's new set of joint angles
	 */
	public void setJointAngles(float[] newJointAngles) {
		
		for (int jdx = 0; jdx < 6; ++jdx) {
			SEGMENT[jdx].setJointRotation(newJointAngles[jdx]);
		}
	}

	/**
	 * Sets the robot's jog speed field to the given value.
	 * 
	 * @param liveSpeed	The robot's new jog speed
	 */
	public void setLiveSpeed(int liveSpeed) {
		this.liveSpeed = liveSpeed;
	}
	
	/**
	 * Sets (or resets) the motion fault state for the robot.
	 * 
	 * @param flag	Whether the robot has a motion fault
	 */
	public void setMotionFault(boolean flag) {
		
		if (motion instanceof LinearMotion) {
			((LinearMotion) motion).setFault(flag);
		}
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param prog
	 * @param mInst
	 * @param nextIdx
	 * @param singleExec
	 * @return
	 */
	public int setupMInstMotion(Program prog, MotionInstruction mInst,
			int nextIdx, boolean singleExec) {
		
		Point instPt = getVector(mInst, prog);
		
		if (!mInst.checkFrames(activeToolIdx, activeUserIdx)) {
			// Incorrect active frames for this motion instruction
			return 1;
			
		} else if (instPt == null) {
			// No point defined for given motion instruction
			return 2;
		}
		
		if (mInst.getMotionType() == Fields.MTYPE_JOINT) {
			// Setup joint motion instruction
			updateMotion(instPt.angles, mInst.getSpeed());
			
		} else if (mInst.getMotionType() == Fields.MTYPE_LINEAR) {
			// Setup linear motion instruction
			Instruction nextInst = prog.getInstAt(nextIdx);
			
			if (mInst.getTermination() > 0 && nextInst instanceof MotionInstruction
					&& !singleExec) {
				// Non-fine termination motion
				Point nextPt = getVector((MotionInstruction)nextInst, prog);
				updateMotion(instPt, nextPt, mInst.getSpeed(), mInst.getTermination() / 100f);
				
			} else {
				// Fine termination motion
				updateMotion(instPt, mInst.getSpeed());
			}
		
		} else if (mInst.getMotionType() == Fields.MTYPE_CIRCULAR) {
			// Setup circular motion instruction
			MotionInstruction sndPt = mInst.getSecondaryPoint();
			Point interPt = getVector(sndPt, prog);
			updateMotion(instPt, interPt, mInst.getSpeed());
			
		} else {
			// Invalid motion type
			return 3;
		}
		
		return 0;
	}
	
	public boolean toggleTrace() {
		trace = !trace;
		clearTrace();
		return trace;
	}
	
	@Override
	public String toString() {
		return String.format("R%d", RID);
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param mdx
	 * @param newDir
	 * @return
	 */
	public int updateJogMotion(int mdx, int newDir) {
		int oldDir;
		
		if (curCoordFrame == CoordFrame.JOINT) {
			// Update/set joint jog motion
			JointJog jogMotion;
			
			if (motion instanceof JointJog) {
				jogMotion = (JointJog)motion;
				
			} else {
				jogMotion = new JointJog();
				motion = jogMotion;
				
			}
			
			oldDir = jogMotion.setMotion(mdx, newDir);
			
		} else {
			// Update/set linear jog motion
			LinearJog jogMotion;
			
			if (motion instanceof LinearJog) {
				jogMotion = (LinearJog)motion;
				
			} else {
				jogMotion = new LinearJog();
				motion = jogMotion;
			}
			
			oldDir = jogMotion.setMotion(mdx, newDir);
		}
		
		return oldDir;
	}
	
	/**
	 * Updates the reference to the Robot's last tool tip position and
	 * orientation, which is used to move the object held by the Robot.
	 */
	public void updateLastTipTMatrix() {
		lastTipTMatrix = getFaceplateTMat(getJointAngles());
	}
	
	/**
	 * Updates the position associated with the motion instruction's secondary
	 * position index. The old point associated with the position is returned.
	 * 
	 * @param p			The program to edit
	 * @param instIdx	The index of a motion instruction in this program
	 * @param newPt		The new point to store at the motion instruction's
	 * 					associated position
	 * @return			The previous point stored at the position associated
	 * 					with the instruction
	 * @throws ClassCastException	If the instruction indexed at instIdx is
	 * 								not a motion instruction
	 * @throws NullPointerException	If the given point is null or the instruction
	 * 								indexed at instIdx is not a motion type
	 * 								instruction
	 */
	public Point updateMCInstPosition(Program p, int instIdx, Point newPt) throws
		ClassCastException, NullPointerException {
		
		if (newPt != null) {
			MotionInstruction mInst = (MotionInstruction) p.get(instIdx);
			MotionInstruction sndMInst = mInst.getSecondaryPoint();
			
			if (mInst.getMotionType() != Fields.MTYPE_CIRCULAR || sndMInst == null) {
				throw new NullPointerException(
					String.format("Instruction at %d is not a circular motion instruction!",
					instIdx)
				);	
			}
			
			int posNum = sndMInst.getPositionNum();
			
			if (mInst.usesGPosReg()) {
				// Update a position register on the robot
				PositionRegister pReg = getPReg(posNum);
				
				if (pReg != null) {
					Point prevPt = pReg.point;
					pReg.point = newPt;
					return prevPt;
				}
				// Uninitialized position register
				return null;
				
			} else {
				// Update a position in the program
				if (posNum == -1) {
					// In the case of an uninitialized position
					posNum = p.getNextPosition();
					sndMInst.setPositionNum(posNum);
				}
				
				return p.setPosition(posNum, newPt);
			}
		}
		
		throw new NullPointerException("arg, newPt, cannot be null for updateMInstPosition()!");
	}
	
	/**
	 * Updates the position associated with the motion instruction at the given
	 * instruction index to the given point. The old point associated with the
	 * position is returned.
	 * 
	 * @param p			The program to edit
	 * @param instIdx	The index of a motion instruction in this program
	 * @param newPt		The new point to store at the motion instruction's
	 * 					associated position
	 * @return			The previous point stored at the position associated
	 * 					with the instruction
	 * @throws ClassCastException	If the instruction indexed at instIdx is
	 * 								not a motion instruction
	 * @throws NullPointerException	If the given point is null
	 */
	public Point updateMInstPosition(Program p, int instIdx, Point newPt) throws
		ClassCastException, NullPointerException {
		
		if (newPt != null) {
			MotionInstruction mInst = (MotionInstruction)p.get(instIdx);
			int posNum = mInst.getPositionNum();
			
			if (mInst.usesGPosReg()) {
				// Update a position register on the robot
				PositionRegister pReg = getPReg(posNum);
				
				if (pReg != null) {
					Point prevPt = pReg.point;
					pReg.point = newPt;
					return prevPt;
				}
				// Uninitialized position register
				return null;
				
			} else {
				// Update a position in the program
				if (posNum == -1) {
					// In the case of an uninitialized position
					posNum = p.getNextPosition();
					mInst.setPositionNum(posNum);
				}
				
				return p.setPosition(posNum, newPt);
			}
		}
		
		throw new NullPointerException("arg, newPt, cannot be null for updateMInstPosition()!");
	}
	
	public void updateMotion(float[] jointAngles) {
		updateMotion(jointAngles, liveSpeed / 100f);
	}

	public void updateMotion(Point tgt) {
		updateMotion(tgt, liveSpeed / 100f);
	}
	
	public void updateMotion(float[] jointAngles, float speed) {
		if (motion instanceof JointInterpolation) {
			((JointInterpolation)motion).setupRotationalInterpolation(this,
					jointAngles, speed);
			
		} else {
			motion = new JointInterpolation(this, jointAngles, speed);
		}
	}

	public void updateMotion(Point tgt, float speed) {
		Point start = getToolTipNative();
		//float ptDist = calculateDistanceBetweenPoints();
		
		if (!(motion instanceof LinearInterpolation)) {
			motion = new LinearInterpolation();
		}
		
		((LinearInterpolation) motion).beginNewLinearMotion(start, tgt,
				speed * motorSpeed);
	}
	
	public void updateMotion(Point tgt, Point next, float speed, float p) {
		Point start = getToolTipNative();
		//float ptDist = calculateDistanceBetweenPoints();
		
		if (!(motion instanceof LinearInterpolation)) {
			motion = new LinearInterpolation();
		}
		
		((LinearInterpolation) motion).beginNewContinuousMotion(start, tgt, next, p,
				speed * motorSpeed / 100f);
	}
	
	public void updateMotion(Point tgt, Point inter, float speed) {
		Point start = getToolTipNative();
		//float ptDist = calculateDistanceBetweenPoints();
		
		if (!(motion instanceof LinearInterpolation)) {
			motion = new LinearInterpolation();
		}
		
		((LinearInterpolation) motion).beginNewCircularMotion(start, inter, tgt,
				speed * motorSpeed / 100f);
	}
	
	/**
	 * Updates the program execution for this robot and the position of the
	 * robot for linear or rotation interpolation.
	 * 
	 * @param app	A reference to the RobotRun application
	 */
	public void updateRobot(RobotRun app) {
		
		if (!hasMotionFault()) {
			if (!inMotion() && isProgExec()) {
				updateProgExec(app);
			}
		
			if (inMotion()) {
				motion.executeMotion(this);
			}
			
			if (!inMotion() && progExecState.getState() == ExecState.EXEC_MINST) {
				// Motion instruction has completed
				progExecState.setState(ExecState.EXEC_MEND);
				updateExecInstIdx();
			}
		}
	
		updateOBBs();
	}
	
	private void updateExecInstIdx() {
		progExecState.updateCurIdx();
		setActiveInstIdx(progExecState.getCurIdx());
		// TODO REFACTOR THIS
		RobotRun app = RobotRun.getInstance();
		app.getContentsMenu().setLineIdx( app.getInstrLine(activeInstIdx) );
		RobotRun.getInstance().updatePendantScreen();
	}
	
	/**
	 * Updates the position and orientation of the robot's OBBs based off its
	 * current joint angles.
	 */
	private void updateOBBs() {
		/* Segment OBBs */
		
		RMatrix base = RMath.formTMat(BASE_POSITION);
		
		RMath.translateTMat(base, -50.0, -163.0, -350.0);
		
		RMatrix obbTMat = base.copy();
		RMath.translateTMat(obbTMat, -150.0, 112.0, 150.0);
		SEGMENT[0].OBBS[0].setCoordinateSystem(obbTMat);
		
		obbTMat = base.copy();
		RMath.translateTMat(obbTMat, -150.0, 18.0, 150.0);
		SEGMENT[1].OBBS[0].setCoordinateSystem(obbTMat);
		
		// First joint rotation
		RMath.translateTMat(base, -150.0, 0.0, 150.0);
		RMatrix jointTMat = RMath.formTMat(SEGMENT[0].AXIS, SEGMENT[0].getJointRotation());
		base = base.multiply(jointTMat);
		RMath.translateTMat(base, 150.0, 0.0, -150.0);
		
		obbTMat = base.copy();
		RMath.translateTMat(obbTMat, -150.0, -73.0, 150.0);
		SEGMENT[1].OBBS[1].setCoordinateSystem(obbTMat);
		
		RMath.translateTMat(base, -125.0, -75.0, 180.0);
		
		// Second joint rotation
		RMath.translateTMat(base, -62.0, -62.0, 0.0);
		jointTMat = RMath.formTMat(SEGMENT[1].AXIS, SEGMENT[1].getJointRotation());
		base = base.multiply(jointTMat);
		RMath.translateTMat(base, 62.0, 62.0, 0.0);
		
		obbTMat = base.copy();
		RMath.translateTMat(obbTMat, -62.0, -59.0, -30.0);
		SEGMENT[2].OBBS[0].setCoordinateSystem(obbTMat);
		
		obbTMat = base.copy();
		RMath.translateTMat(obbTMat, -62.0, -279.0, -30.0);
		SEGMENT[2].OBBS[1].setCoordinateSystem(obbTMat);
		
		obbTMat = base.copy();
		RMath.translateTMat(obbTMat, -62.0, -520.0, -22.0);
		SEGMENT[2].OBBS[2].setCoordinateSystem(obbTMat);
		
		RMath.translateTMat(base, 10.0, -605.0, -200.0);
		
		// Third joint rotation
		RMath.translateTMat(base, -75.0, 45.0, 0.0);
		jointTMat = RMath.formTMat(SEGMENT[2].AXIS, SEGMENT[2].getJointRotation());
		base = base.multiply(jointTMat);
		RMath.translateTMat(base, 75.0, -45.0, 0.0);
		
		obbTMat = base.copy();
		RMath.translateTMat(obbTMat, -75.0, 75.0, 75.0);
		SEGMENT[3].OBBS[0].setCoordinateSystem(obbTMat);
		
		RMath.translateTMat(base, -725.0, 0.0, 0.0);
		
		// Fourth joint rotation
		RMath.translateTMat(base, 0.0, 75.0, 75.0);
		jointTMat = RMath.formTMat(SEGMENT[3].AXIS, SEGMENT[3].getJointRotation());
		base = base.multiply(jointTMat);
		RMath.translateTMat(base, 0.0, -75.0, -75.0);
		
		obbTMat = base.copy();
		RMath.translateTMat(obbTMat, 361.0, 75.0, 75.0);
		SEGMENT[3].OBBS[1].setCoordinateSystem(obbTMat);
		
		obbTMat = base.copy();
		RMath.translateTMat(obbTMat, 75.0, 75.0, 75.0);
		SEGMENT[3].OBBS[2].setCoordinateSystem(obbTMat);
		
		RMath.translateTMat(base, 120.0, 20.0, 25.0);
		
		// Fifth joint rotation
		RMath.translateTMat(base, -55.0, 55.0, 50.0);
		jointTMat = RMath.formTMat(SEGMENT[4].AXIS, SEGMENT[4].getJointRotation());
		base = base.multiply(jointTMat);
		RMath.translateTMat(base, 55.0, -55.0, -50.0);
		
		RMath.translateTMat(base, -150.0, 10.0, 95.0);
		
		// Sixth joint rotation
		RMath.translateTMat(base, 0.0, 45.0, -45.0);
		jointTMat = RMath.formTMat(SEGMENT[5].AXIS, SEGMENT[5].getJointRotation());
		base = base.multiply(jointTMat);
		RMath.translateTMat(base, 0.0, -45.0, 45.0);
		
		/* End Effector OBBs */
		EndEffector activeEE = getActiveEE();
		
		if (activeEEIdx == 1) {
			// Suction EE OBBs
			RMath.translateTMat(base, 25.0, -37.5, -90.0);
			
			obbTMat = base.copy();
			RMath.translateTMat(obbTMat, -8.0, 82.5, 45.0);
			activeEE.OBBS[0].setCoordinateSystem(obbTMat);
			
			obbTMat = base.copy();
			RMath.translateTMat(obbTMat, -64.0, 82.5, 45.0);
			activeEE.OBBS[1].setCoordinateSystem(obbTMat);
			
			obbTMat = base.copy();
			RMath.translateTMat(obbTMat, -45.0, 32.0, 45.0);
			activeEE.OBBS[2].setCoordinateSystem(obbTMat);
			
			obbTMat = base.copy();
			RMath.translateTMat(obbTMat, -106.5, 82.5, 45.0);
			activeEE.PICKUP_OBBS[0].setCoordinateSystem(obbTMat);
			
			obbTMat = base.copy();
			RMath.translateTMat(obbTMat, -45.0, -2.0, 45.0);
			activeEE.PICKUP_OBBS[1].setCoordinateSystem(obbTMat);
			
		} else if (activeEEIdx == 2) {
			// Gripper EE OBBs
			RMath.translateTMat(base, 25.0, 0.0, -90.0);
			
			obbTMat = base.copy();
			RMath.translateTMat(obbTMat, -8.0, 45.0, 45.0);
			activeEE.OBBS[0].setCoordinateSystem(obbTMat);
			
			float firstGripper, secondGripper;
			
			if(activeEE.getState() == Fields.ON) {
				// Closed grippers
				firstGripper = 22.5f;
				secondGripper = 25f;

			} else {
				// Open grippers
				firstGripper = 10f;
				secondGripper = 55f;
			}
			
			RMath.translateTMat(base, -32.5, 0.0, 85.0);
			
			obbTMat = base.copy();
			RMath.translateTMat(obbTMat, -24.0, 45.0, -40.0);
			activeEE.PICKUP_OBBS[0].setCoordinateSystem(obbTMat);
			
			RMath.translateTMat(base, 0.0, firstGripper, 0.0);
			
			obbTMat = base.copy();
			RMath.translateTMat(obbTMat, -24.0, 8.0, -40.0);
			activeEE.OBBS[1].setCoordinateSystem(obbTMat);
			
			RMath.translateTMat(base, 0, secondGripper, 0);
			
			obbTMat = base.copy();
			RMath.translateTMat(obbTMat, -24.0, 8.0, -40.0);
			activeEE.OBBS[2].setCoordinateSystem(obbTMat);
		}
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param app
	 * @return
	 */
	@SuppressWarnings("static-access")
	private void updateProgExec(RobotRun app) {	
		Instruction activeInstr = progExecState.prog.get(progExecState.getCurIdx());
		int nextIdx = progExecState.getCurIdx() + 1;
		
		if (!activeInstr.isCommented()) {
			if (activeInstr instanceof MotionInstruction) {
				MotionInstruction motInstr = (MotionInstruction) activeInstr;
				int ret = setupMInstMotion(progExecState.prog, motInstr,
						nextIdx, progExecState.isSingleExec());
				
				if (ret == 0) {
					progExecState.setState(ExecState.EXEC_MINST);
					
				} else {
					// Issue occurred with setting up the motion instruction
					nextIdx = -1;
				}
				
			} else if (activeInstr instanceof JumpInstruction) {
				nextIdx = activeInstr.execute();

			} else if (activeInstr instanceof CallInstruction) {
				// TODO REFACTOR THIS
				if (((CallInstruction) activeInstr).getTgtDevice() != app.getInstanceRobot()) {
					// Call an inactive Robot's program
					if (app.getUI().getRobotButtonState()) {
						nextIdx = activeInstr.execute();
						
					} else {
						// No second robot in application
						nextIdx = -1;
					}
				} else {
					progExecState.setNextIdx(activeInstr.execute());
				}

			} else if (activeInstr instanceof IfStatement ||
					activeInstr instanceof SelectStatement) {
				
				int ret = activeInstr.execute();

				if (ret != -2) {
					nextIdx = ret;
				}

			} else if (activeInstr.execute() != 0) {
				nextIdx = -1;
			}
		}
		
		progExecState.setNextIdx(nextIdx);
		updateExecInstIdx();
	}
}

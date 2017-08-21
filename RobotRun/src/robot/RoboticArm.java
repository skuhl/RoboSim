package robot;

import java.util.ArrayList;
import java.util.Stack;

import enums.AxesDisplay;
import enums.CoordFrame;
import enums.InstUndoType;
import frame.ToolFrame;
import frame.UserFrame;
import geom.BoundingBox;
import geom.MyPShape;
import geom.Part;
import geom.Point;
import geom.RMatrix;
import geom.RQuaternion;
import geom.RRay;
import geom.Scenario;
import geom.WorldObject;
import global.Fields;
import global.RMath;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PShape;
import processing.core.PVector;
import programming.CamMoveToObject;
import programming.InstElement;
import programming.InstUndoState;
import programming.Instruction;
import programming.Macro;
import programming.MotionInstruction;
import programming.PosMotionInst;
import programming.Program;
import regs.DataRegister;
import regs.IORegTrace;
import regs.IORegister;
import regs.PositionRegister;
import ui.DisplayLine;

public class RoboticArm {
	
	/**
	 * The maximum number of programs allowed for a single robotic arm.
	 */
	public static final int PROG_NUM = 100;
	
	/**
	 * Defines the conversion between the robot's maximum rotation speed and
	 * its maximum linear motion speed.
	 */
	public static final int motorSpeed;
	
	/**
	 * Defines a set of tool tip default offsets associated with each end
	 * effector.
	 */
	private static final PVector[] EE_TOOLTIP_DEFAULTS;
	
	static {
		motorSpeed = 1000; // speed in mm/sec
		
		EE_TOOLTIP_DEFAULTS = new PVector[5];
		EE_TOOLTIP_DEFAULTS[0] = new PVector(-81f, 0f, 0f);
		EE_TOOLTIP_DEFAULTS[1] = new PVector(-32f, 0f, 0f);
		EE_TOOLTIP_DEFAULTS[2] = new PVector(-180f, 55f, 0f);
		EE_TOOLTIP_DEFAULTS[3] = new PVector(-120f, -150f, 0f);
		EE_TOOLTIP_DEFAULTS[4] = new PVector(-295f, 53.5f, 0f);
	}
	
	/**
	 * The unique ID of this robot.
	 */
	public final int RID;
	
	/**
	 * The index corresponding to the active end effector in EE_LIST.
	 */
	private int activeEEIdx;
	
	/**
	 * An index corresponding to the active frame for this robot.
	 */
	private int activeUserIdx, activeToolIdx;
	
	/**
	 * The position of the center of the robot's base segment.
	 */
	private final PVector BASE_POSITION;
	
	/**
	 * The current coordinate frame of the robot.
	 */
	private CoordFrame curCoordFrame;
	
	/**
	 * The initial position and orientation of the robot.
	 */
	private final Point DEFAULT_PT;
	
	/**
	 * The data register associated with this robot.
	 */
	private final DataRegister[] DREG;
	
	/**
	 * The list of the robot's end effectors.
	 */
	private final EndEffector[] EE_LIST;
	
	/**
	 * Defines sets of indices, which map to the robot's segment bounding
	 * boxes. This is used to check for collisions between the robot's end 
	 * effector and its segments.
	 */
	private final int[] EE_SEG_OBB_CHECKS;
	
	/**
	 * A reference for the part current held by the robot.
	 */
	private Part heldPart;
	
	/**
	 * Returns a reference to the transformation matrix, which represents the
	 * orientation and position of this robot's tooltip that was recorded after
	 * previously rendering the robot.
	 */
	private RMatrix lastTipTMatrix;
	
	/**
	 * Defines the speed multiplier for the robot's jog and move to motion.
	 */
	private int liveSpeed;
	
	private Macro[] macroKeyBinds = new Macro[7];
	
	private ArrayList<Macro> macros = new ArrayList<>();
	
	/**
	 * The rogot's current motion state.
	 */
	private RobotMotion motion;
	
	/**
	 * The position registers associated with this robot.
	 */
	private final PositionRegister[] PREG;
	
	/**
	 * A stack of previous states of instructions that the user has since edited.
	 */
	private final Stack<InstUndoState> PROG_UNDO;
	
	/**
	 * The list of programs associated with this robot.
	 */
	private final ArrayList<Program> PROGRAM;
	
	/**
	 * Defines sets of indices which map to pairs of bounding boxes between
	 * two of the robot's segments. This is used for checking self-collisions
	 * of the robot's bounding boxes.
	 */
	private final int[] SEG_OBB_CHECKS;
	
	/**
	 * A list of the robot's arm segment models.
	 */
	private final RSegWithJoint[] SEGMENT;
	
	/**
	 * A set of user-defined frames associated with this robot. 
	 */
	private final ToolFrame[] TOOL_FRAME;
	/**
	 * A set of user-defined frames associated with this robot.
	 */
	private final UserFrame[] USER_FRAME;
	
	/**
	 * Creates a robotic arm with the given ID, segment models, and end
	 * effector models. It is expected that there are 6 segment models
	 * and 7 end effector models.
	 * 
	 * @param rid				The ID of this robot, which must be unique
	 * 							amongst all robots
	 * @param basePos			The position of the robot's base segment
	 * @param robotTrace		A reference to the trace in the robotRun
	 * 							application
	 */
	public RoboticArm(int rid, PVector basePos, RTrace robotTrace) {
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
		PShape[] segmentModels = loadJointModels();
		
		SEGMENT[0] = new RSegWithJoint(
			segmentModels[0],
			new BoundingBox[] { new BoundingBox(405, 105, 405) },
			new PVector(-200f, -163f, -200f), new PVector(0f, 1f, 0f)
		);
		
		SEGMENT[1] = new RSegWithJoint(
			segmentModels[1],
			new BoundingBox[] {
					new BoundingBox(305, 80, 305),
					new BoundingBox(114, 98, 160)
			},
			4.34f, 2.01f, new PVector(-37f, -137f, 30f),
			new PVector(0f, 0f, -1f)
		);
		
		SEGMENT[2] = new RSegWithJoint(
			segmentModels[2],
			new BoundingBox[] {
					new BoundingBox(130, 120, 160),
					new BoundingBox(130, 316, 64),
					new BoundingBox(110, 163, 48)
			},
			1.955f, 1.134f, new PVector(-3f, -498f, -200f),
			new PVector(0f, 0f, -1f)
		);
		
		SEGMENT[3] = new RSegWithJoint(
			segmentModels[3],
			new BoundingBox[] {
					new BoundingBox(154, 154, 154),
					new BoundingBox(420, 126, 126),
					new BoundingBox(148, 154, 154),
			},
			new PVector(-650f, 30f, 75f), new PVector(1f, 0f, 0f)
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
			new PVector(-95f, 0f, 0f), new PVector(-1f, 0f, 0f)
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
		PShape[] endEffectorModels = loadEEModels();
		
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
				new PShape[] {
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
				new BoundingBox[0], 4, "GLUE GUN", robotTrace);
		
		EE_LIST[5] = new EndEffector(endEffectorModels[6], new BoundingBox[0],
				new BoundingBox[0], 5, "WIELDER", robotTrace);
		
		activeEEIdx = 0;
		
		// Initialize program fields
		PROGRAM = new ArrayList<>();
		PROG_UNDO = new Stack<>();
		
		motion = null;
		
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
	}
	
	/**
	 * Adds the given instruction to the given program, if the given index is
	 * within the bounds of the given program's list of instructions. This
	 * insertion is added to the undo stack.
	 * 
	 * @param p		The program, to which to add the given instruction
	 * @param idx	The index at which to add the given instruction in the
	 * 				given program's list of instructions
	 * @param inst	The instruction to add to the given program
	 * @param group	Whether to group the insertion undo state with previous
	 * 				undo states
	 */
	public void addAt(Program p, int idx, Instruction inst, boolean group) {
		if (p != null && inst != null && idx >= 0 && idx <= p.getNumOfInst()) {
			int insertIdx = p.addInstAt(idx, inst);
			
			if (insertIdx != -1) {
				pushUndoState(InstUndoType.INSERTED, p, idx, p.get(idx),
						group);
			}
		}
	}
	
	/**
	 * Adds the given instruction to the end of the given program's list of
	 * instructions. This insertion is added to the undo stack.
	 * 
	 * @param p		The program, to which to add the given instruction
	 * @param inst	The instruction to add to the given program
	 * @param group	Whether to group the insertion undo state with previous
	 * 				undo states
	 */
	public void addInstAtEnd(Program p, Instruction inst, boolean group) {
		if (p != null) {
			int idx = p.getNumOfInst();
			addAt(p, idx, inst, group);
		}
	}

	/**
	 * Adds the given program to this Robot's list of programs.
	 * 
	 * @param p	The program to add to the Robot
	 * @return	-1		the given program is null,
	 * 			-2		this robot has already reached its program capacity,
	 * 			>= 0	the index of the newly inserted program
	 */
	public int addProgram(Program p) {
		if (p == null) {
			return -1;
			
		} else if (PROGRAM.size() >= PROG_NUM) {
			return -2;
			
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
			
			if (oob0.collision3D(oob1)) {
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
				
				if (obb.collision3D(segOBB)) {
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
			PVector collPt = seg.closestCollision(ray);
			
			if (collPt != null && (closestCollPt == null ||
					PVector.dist(ray.getOrigin(), collPt) <
					PVector.dist(ray.getOrigin(), closestCollPt))) {
				
				// Find the closest collision to the ray origin
				closestCollPt = collPt;
			}
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
					Fields.drawAxes(g, activeUser.getOrigin(), userAxes, 10000f);
					
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
	 * @return	The index of the active end effector
	 */
	public int getActiveEEIdx() {
		return activeEEIdx;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @return
	 */
	public String getActiveEEName() {
		EndEffector ee = this.getActiveEE();
		
		if (ee != null) {
			return ee.getName();
			
		}
		
		return null;
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
	 * Returns a copy of the secondary position of a position motion
	 * instruction.
	 * 
	 * @param mInst		The motion instruction, of which to get the associated
	 * 					position
	 * @param parent	The program, to which the given instruction belongs
	 * @return			A copy of the position associated with the secondary
	 * 					position of the given circular motion instruction
	 */
	public Point getCPosition(PosMotionInst mInst, Program parent) {
		int pType = mInst.getCircPosType();
		Point pt = null;
		
		if (pType == Fields.PTYPE_PREG) {
			// The instruction references a global position register
			PositionRegister pReg = getPReg(mInst.getCircPosIdx());
			
			if (pReg != null) {
				pt = pReg.point;
			}
			
		} else if (pType == Fields.PTYPE_PROG) {
			pt = parent.getPosition(mInst.getCircPosIdx());
		}
		
		if (pt != null) {
			return pt.clone();
		}
		
		// Uninitialized position or invalid position index
		return null;
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
	public boolean getEEState() {
		return this.getActiveEE().getState();
	}

	/**
	 * Returns the error messages that would describe the issue with forming
	 * the vector defined by the given motion instruction. If no issue is
	 * found, then null is returned.
	 * 
	 * @param mInst			The motion instruction, which defines the vector
	 * @param parent		The program, to which the given instruction belongs
	 * @param getCircPos	Whether to get the circular position or primary
	 * 						position of the given motion instruction
	 * @return				The error message describing the issue with forming
	 * 						the vector, or null if no issue is found
	 */
	public String getErrorMessage(PosMotionInst mInst, Program parent,
			boolean getCircPos) {
		
		Point pt;
		
		if (getCircPos) {
			pt = getCPosition(mInst, parent);
			
		} else {
			pt = getPosition(mInst, parent);
		}
		
		if (pt == null) {
			return String.format("Null position for %s\n", mInst);
		}
		
		UserFrame instUFrame = getUserFrame(mInst.getUFrameIdx());
		PositionRegister offReg = getPReg(mInst.getOffsetIdx());
		
		// Check if offset can be applied
		if (mInst.getOffsetType() != Fields.OFFSET_NONE) {
			
			if (offReg == null || offReg.point == null) {
				// Invalid offset
				return String.format("Null offset PR[%d] for %s\n",
						mInst.getOffsetIdx(), mInst);
			}
			
			Point offset = offReg.point;
			boolean offIsCart = offReg.isCartesian;
			boolean posIsCart = mInst.getMotionType() != Fields.MTYPE_JOINT;
			
			if (posIsCart && offIsCart) {
				// Add a Cartesian offset to a linear motion instruction
				pt = pt.add(offset.position, offset.orientation);
				
				if (instUFrame != null) {
					// Remove the associated user frame
					pt = removeFrame(pt, instUFrame.getOrigin(),
							instUFrame.getOrientation());
				}
				
				// Find the resulting joint angles
				float[] jointAngles = RMath.inverseKinematics(this, pt.angles,
						pt.position, pt.orientation);
				
				if (jointAngles == null) {
					// Inverse kinematics failure
					return String.format("IK failure for %s\n", mInst);
				}
				
			} else if (posIsCart) {
				// Add a joint offset to a linear motion instruction
				if (instUFrame != null) {
					// Remove the associated user frame
					pt = removeFrame(pt, instUFrame.getOrigin(),
							instUFrame.getOrientation());
				}
				
				float[] jointAngles = RMath.inverseKinematics(this, pt.angles,
						pt.position, pt.orientation);
				
				if (jointAngles == null) {
					// Inverse kinematics failure
					return String.format("IK failure for %s\n", mInst);
				}
				
			} else if (offIsCart) {
				// Add a Cartesian offset to a joint motion instruction
				if (instUFrame != null) {
					offset = removeFrame(offset, new PVector(), instUFrame.getOrientation());
				}
				
				pt = getToolTipNative(pt.angles);
				// Apply offset
				pt = pt.add(offset.position, offset.orientation);
				
				// Find the resulting joint angles
				float[] jointAngles = RMath.inverseKinematics(this, pt.angles,
						pt.position, pt.orientation);
				
				if (jointAngles == null) {
					// Inverse kinematics failure
					return String.format("IK failure for %s\n", mInst);
				}	
			}
		}
		
		return null;
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
				 tipOrien.getEntryF(0, 3),
				 tipOrien.getEntryF(1, 3),
				 tipOrien.getEntryF(2, 3)
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
	public Instruction getInstToEdit(Program p, int idx) {
		// Valid active program and instruction index
		if (p != null && idx >= 0 && idx < p.getNumOfInst()) {
			InstElement e = p.get(idx);
			
			pushUndoState(InstUndoType.EDITED, p, idx, new InstElement(e.getID(),
					e.getInst().clone()), false);
			
			/* TEST CODE *
			try {
				throw new RuntimeException();
				
			} catch (RuntimeException REx) {
				REx.printStackTrace();
			}
			/**/
			
			return e.getInst();
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
		// Exclude the faceplate
		if (rdx > 0 && rdx < EE_LIST.length) {
			return EE_LIST[rdx].reg;
		}
		
		return null;
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
	 * Returns a reference to the transformation matrix, which represents the
	 * orientation and position of this robot's tooltip that was recorded after
	 * previously rendering the robot.
	 * 
	 * @return	A transformation matrix represent the robot's last tool tip
	 * 			position and orientation
	 */
	public RMatrix getLastTipTMatrix() {
		return lastTipTMatrix;
	}
	
	
	public int getLiveSpeed() {
		return liveSpeed;
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
	
	/**
	 * Returns a copy of the primary position of the given position motion
	 * instruction.
	 * 
	 * @param mInst		The motion instruction, of which to get its associated
	 * 					primary position
	 * @param parent	The program, to which the given instruction belongs
	 * @return			A copy of the primary position associated with the
	 * 					given motion instruction
	 */
	public Point getPosition(PosMotionInst mInst, Program parent) {
		int pType = mInst.getPosType();
		Point pt = null;
		
		if (pType == Fields.PTYPE_PREG) {
			// The instruction references a global position register
			PositionRegister pReg = getPReg(mInst.getPosIdx());
			
			if (pReg != null) {
				pt = pReg.point;
			}
			
		} else if (pType == Fields.PTYPE_PROG) {
			pt = parent.getPosition(mInst.getPosIdx());
		}
		
		if (pt != null) {
			return pt.clone();
		}
		
		// Uninitialized position or invalid position index
		return null;
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
	 * Attempts to find the index of the program with the given name amongst
	 * this Robot's programs. If no program with the given name exists, then
	 * null is returned.
	 * 
	 * @param name	The name of the target program
	 * @return		The program with the given name, if it exists
	 */
	public int getProgIdx(String name) {
		for (int idx = 0; idx < PROGRAM.size(); ++idx) {
			if (PROGRAM.get(idx).getName().equals(name)) {
				// Return the index of the program with the match
				return idx;
			}
			
		}
		// No program with the given name exists
		return -1;
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
		return getProgram( getProgIdx(name) );
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
	 * Returns the default tool tip offset for the end effector with the given
	 * index.
	 * 
	 * @param idx	The index of an end effector of this robot
	 * @return		The default tool tip offset of the end effector associated
	 * 				with the given index
	 */
	public PVector getToolTipDefault(int idx) {
		if (idx >= 0 && idx < EE_TOOLTIP_DEFAULTS.length) {
			return EE_TOOLTIP_DEFAULTS[idx];
		}
		// invalid index
		return null;
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
	 * @return	The robot's tool tip position and orientation with respect to the
	 * 			active user frame
	 */
	public Point getToolTipUser() {
		return getToolTipPoint(getJointAngles(), getActiveTool(),
				getActiveUser());
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
	 * Returns the native position and orientation and robot joint angles
	 * resulting from the given motion instruction based on the position
	 * associated with the motion instruction, as well as its user frame,
	 * tool frame, and offset. Depending on the offset, the resulting point may
	 * be null.
	 * 
	 * @param mInst			The motion instruction defining the position index,
	 * 						position type, user frame, tool frame, and offset
	 * 						for the resulting point
	 * @param parent		The program, to which mInst belongs
	 * @param useCircPos	Whether to get the primary or secondary position of
	 * 						the motion instruction
	 * @return				The position and orientation of the point in the
	 * 						native coordinate system as well as the associated
	 * 						joint angles for that position
	 */
	public Point getVector(PosMotionInst mInst, Program parent, boolean getCircPos) {
		Point pt;
		
		if (getCircPos) {
			pt = getCPosition(mInst, parent);
			
		} else {
			pt = getPosition(mInst, parent);
		}
		
		if (pt == null) {
			return null;
		}
		
		UserFrame instUFrame = getUserFrame(mInst.getUFrameIdx());
		PositionRegister offReg = getPReg(mInst.getOffsetIdx());
		
		// Check if offset can be applied
		if (mInst.getOffsetType() != Fields.OFFSET_NONE) {
			
			if (offReg == null || offReg.point == null) {
				// Invalid offset
				return null;
			}
			
			Point offset = offReg.point;
			boolean offIsCart = offReg.isCartesian;
			boolean posIsCart = mInst.getMotionType() != Fields.MTYPE_JOINT;
			
			if (posIsCart && offIsCart) {
				// Add a Cartesian offset to a linear motion instruction
				pt = pt.add(offset.position, offset.orientation);
				
				if (instUFrame != null) {
					// Remove the associated user frame
					pt = removeFrame(pt, instUFrame.getOrigin(),
							instUFrame.getOrientation());
				}
				// Find the resulting joint angles
				float[] jointAngles = RMath.inverseKinematics(this, pt.angles,
						pt.position, pt.orientation);
				
				if (jointAngles == null) {
					// Inverse kinematics failure
					return null;
					
				} else {
					pt.angles = jointAngles;
				}
				
			} else if (posIsCart) {
				// Add a joint offset to a linear motion instruction
				if (instUFrame != null) {
					// Remove the associated user frame
					pt = removeFrame(pt, instUFrame.getOrigin(),
							instUFrame.getOrientation());
				}
				
				float[] jointAngles = RMath.inverseKinematics(this, pt.angles,
						pt.position, pt.orientation);
				
				if (jointAngles == null) {
					// Inverse kinematics failure
					return null;
				}
				// Apply the offset
				for (int jdx = 0; jdx < 6; ++jdx) {
					jointAngles[jdx] = RMath.mod2PI(jointAngles[jdx]
							+ offset.angles[jdx]);
				}
				
				// Calculate the new Cartesian position and orientation
				pt = getToolTipNative(jointAngles);
				
			} else if (offIsCart) {
				// Add a Cartesian offset to a joint motion instruction
				if (instUFrame != null) {
					offset = removeFrame(offset, new PVector(), instUFrame.getOrientation());
				}
				
				pt = getToolTipNative(pt.angles);
				// Apply offset
				pt = pt.add(offset.position, offset.orientation);
				
				// Find the resulting joint angles
				float[] jointAngles = RMath.inverseKinematics(this, pt.angles,
						pt.position, pt.orientation);
				
				if (jointAngles == null) {
					// Inverse kinematics failure
					return null;
					
				} else {
					pt.angles = jointAngles;
				}
				
			} else {
				// Add joint offset to a joint motion instruction
				for (int jdx = 0; jdx < 6; ++jdx) {
					pt.angles[jdx] = RMath.mod2PI(pt.angles[jdx]
							+ offset.angles[jdx]);
				}
				
				pt = getToolTipNative(pt.angles);
			}
		
		// No offset to apply
		} else if (instUFrame != null) {
			// Remove the associated user frame
			pt = removeFrame(pt, instUFrame.getOrigin(),
					instUFrame.getOrientation());
		}
		
		return pt;
	}
	
	/**
	 * Stops all movement of this robot.
	 */
	public void halt() {
		if (motion != null) {
			motion.halt();
		}
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
	 * Indicates that the Robot Arm is in motion.
	 * 
	 * @return	Whether the robot is moving in some way
	 */
	public boolean inMotion() {
		return motion != null && motion.hasMotion();
	}

	/**
	 * Certain end effectors have a trace functionality associated with certain
	 * states of the end effector. This method evaluates the state of and the
	 * active end effector of this robot and determines if the trace
	 * functionality is active.
	 * 
	 * @return	If the trace functionality is active based on the robot's end
	 * 			effector is enabled
	 */
	public boolean isEETraceEnabled() {
		EndEffector activeEE = getActiveEE();
		
		if (activeEE != null) {
			/* The trace functionality is active when the active end effector's
			 * I/O register is associated with the trace functionality and its
			 * state is ON. */
			IORegister ioReg = activeEE.getIORegister();
			return ioReg instanceof IORegTrace && ioReg.getState() == Fields.ON;
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
	 * Checks if the macro key binding associated with the given index is set
	 * to a macro.
	 * 
	 * @param idx	The index of a macro key binding
	 * @return		Whether the macro key binding is being used
	 */
	public boolean isMarcoSet(int idx) {
		return macroKeyBinds[idx] != null;
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
				
				Fields.debug("Invalid angle: J[%d] = %4.3f -> %4.3f : [%4.3f - %4.3f]\n",
						joint, getJointAngles()[joint], destAngles[joint], seg.LOW_BOUND,
						seg.UP_BOUND);
				break;
			}
		}

		// Did we successfully find the desired angles?
		if ((destAngles == null) || invalidAngle) {
			if (destAngles == null) {
				Point RP = getToolTipNative();
				Fields.debug("IK Failure ...\n%s -> %s\n%s -> %s\n\n",
						RP.position, destPosition, RP.orientation,
						destOrientation);
			}
			
			return 1;
		}

		setJointAngles(destAngles);
		return 0;
	}
	
	/**
	 * @return	The number of end effectors associated with this robot
	 */
	public int numOfEndEffectors() {
		// Exclude Faceplate IO Register
		return EE_LIST.length - 1;
	}
	
	/**
	 * Returns the number of programs associated with the Robot.
	 */
	public int numOfPrograms() {
		return PROGRAM.size();
	}
	
	/**
	 * Adds the given set of instructions to the given program, starting at the
	 * specified index, insertIdx.
	 * 
	 * @param p				The program to which to add the instructions
	 * @param insertIdx		The index in the program's list of instructions, at
	 * 						which to starting inserting the given instructions
	 * @param toInsert		The set of instructions to add to the given program
	 */
	public void pasteInstructions(Program p, int insertIdx,
			ArrayList<Instruction> toInsert) {
		
		pasteInstructions(p, insertIdx, toInsert, 0);
	}
	
	/**
	 * Adds the given set of instructions to the given program, starting at
	 * the specified index, insertIdx. The options argument defines different
	 * methods of pasting the given set of instructions into the program:
	 * 
	 * CLEAR_POSITION	Resets all the position indices of all pasted motion
	 * 					instructions
	 * NEW_POSITION		Defines a new position for each pasted motion
	 * 					instruction (with a copy of the origin instrucion's
	 * 					position values)
	 * REVERSE_MOTION	Reverses the order of the pasted motion instructions
	 * PASTE_REVERSE	Reverses the the order of all pasted instructions
	 * 
	 * These options can be combined with logic operators and are defined in
	 * the Fields class.
	 * 
	 * @param p				The program to which to add the instructions
	 * @param insertIdx		The index in the program's list of instructions, at
	 * 						which to starting inserting the given instructions
	 * @param toInsert		The set of instructions to add to the given program
	 * @param options		The options to apply to the pasting process
	 */
	public void pasteInstructions(Program p, int insertIdx,
			ArrayList<Instruction> toInsert, int options) {
		
		ArrayList<Instruction> pasteList = new ArrayList<>();

		/* Pre-process instructions for insertion into program. */
		for (int i = 0; i < toInsert.size(); i += 1) {
			Instruction instr = toInsert.get(i).clone();

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

					for (int j = i + 1; j < toInsert.size(); j += 1) {
						if (toInsert.get(j) instanceof MotionInstruction) {
							next = (MotionInstruction) toInsert.get(j).clone();
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
		for (int i = 0; p.getNumOfInst() < Program.MAX_SIZE &&
				i < toInsert.size(); i += 1) {
			
			Instruction instr;
			if ((options & Fields.PASTE_REVERSE) == Fields.PASTE_REVERSE) {
				instr = pasteList.get(pasteList.size() - 1 - i);
				
			} else {
				instr = pasteList.get(i);
			}
			
			addAt(p, insertIdx + i, instr, i != 0);
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
	 * If an object is currently being held by the Robot arm, then release it.
	 * Then, update the Robot's End Effector status and IO Registers.
	 */
	public void releaseHeldObject() {
		heldPart = null;
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
				while (predecessor.getName().compareTo(toInsert.getName())
						> 0) {
					
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
	 * @param p		The program to edit
	 * @param idx	The index of the instruction to replace
	 * @param inst	The new instruction to add into the active program
	 * @return		The instruction, which was replaced by the given
	 * 				instruction
	 */
	public Instruction replaceInstAt(Program p, int idx, Instruction inst) {
		// Valid active program and instruction index
		if (p != null && idx >= 0 && idx < p.getNumOfInst()) {
			InstElement e = p.get(idx);
			InstElement replaced = new InstElement(e.getID(), e.getInst());
			p.replaceInstAt(idx, inst);
			
			if (replaced != null) {
				pushUndoState(InstUndoType.REPLACED, p, idx, replaced, false);
				return replaced.getInst();
			}
		}
		
		return null;
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
	 * Removes the instruction associated wit the given index, if the given
	 * index is valid index in the given program. The deletion is added to
	 * the program undo stack as well.
	 * 
	 * @param p		The program to edit
	 * @param idx	The index of the instruction to be removed
	 * @param group	Whether to group this deletion with previous deletions in
	 * 				the undo stack
	 * @return		The instruction that was removed
	 */
	public Instruction rmInstAt(Program p, int idx, boolean group) {
		if (p != null && idx >= 0 && idx < p.getNumOfInst()) {
			InstElement e = p.rmInstAt(idx);
			
			if (e != null) {
				pushUndoState(InstUndoType.REMOVED,  p, idx, e, group);
				return e.getInst();
			}
		}
		
		return null;
	}
	
	/**
	 * Removes the given program from this robot's list of programs, if it
	 * exists.
	 * 
	 * @param p	The program to remove
	 * @return	If the program existed in this robot's list of programs
	 */
	public boolean rmProg(Program p) {
		return PROGRAM.remove(p);
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
			// Return the removed program
			return removed;
			
		} else {
			// Invalid index
			return null;
		}
	}

	/**
	 * Set the index of the robot's active end effector to the given index. The
	 * given index must be within the range [0, EE_LIST.length). 0 implies
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
	 * 
	 * @param newFrame	The new coordinate frame
	 */
	public void setCoordFrame(CoordFrame newFrame) {
		curCoordFrame = newFrame;
	}
	
	/**
	 * Sets the offset of the tool frame with the given index to that of the
	 * default tool tip with the other given index.
	 * 
	 * @param frameIdx	The index of the tool frame
	 * @param defTipIdx	The index of the default tool tip offset
	 */
	public void setDefToolTip(int frameIdx, int defTipIdx) {
		ToolFrame frame = getToolFrame(frameIdx);
		
		if (frame != null && defTipIdx >= 0 && defTipIdx <
				EE_TOOLTIP_DEFAULTS.length) {
			
			// Set the offset of the frame to the specified default tool tip
			PVector defToolTip = EE_TOOLTIP_DEFAULTS[defTipIdx];
			frame.setTCPOffset(defToolTip.copy());
		}
	}
	
	/**
	 * Updates the state of the robot's end effector associated with the given
	 * index.
	 * 
	 * @param rdx		The index of the I/O register
	 * @param newState	The new state of the I/O register
	 */
	public void setEEState(int rdx, boolean newState) {
		IORegister ioReg = getIOReg(rdx);
		
		if (ioReg != null) {
			ioReg.setState(newState);
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
	 * Updates this robot's macro key-binding set.
	 * 
	 * @param usrKeyBinds	The new set of macro key-binding for this robot
	 */
	public void setMacroBindings(Macro[] usrKeyBinds) {
		macroKeyBinds = usrKeyBinds;
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
	 * Initializes the motion defined by the given motion instruction and
	 * program execution state.
	 * 
	 * @param prog			The program, to which the given instruction belongs
	 * @param mInst			The motion instruction, which defines the motion
	 * @param nextIdx		The index of the next instruction in the given
	 * 						program
	 * @param singleExec	Whether or not the program execution type is
	 * 						stepwise or not
	 * @return				0 if the motion is successfully initialized,
	 * 						1 if the active frames do not match those of the
	 * 							given motion instruction,
	 * 						2 if the motion instruction's position is invalid,
	 * 						3 if the instruction is not a motion instruction
	 * 							and the given instruction is a non-fine
	 * 							termination instruction,
	 * 						4 if the motion type of the given motion
	 * 							instruction is invalid
	 */
	public int setupMInstMotion(Program prog, MotionInstruction mInst,
			int nextIdx, boolean singleExec) {
		
		if (mInst instanceof PosMotionInst) {
			PosMotionInst pMInst = (PosMotionInst)mInst;
			Point instPt = getVector(pMInst, prog, false);
			
			if (pMInst.getTFrameIdx() != activeToolIdx ||
					pMInst.getUFrameIdx() != activeUserIdx) {
				
				// Incorrect active frames for this motion instruction
				StaticLinearInterpolation liMotion = new StaticLinearInterpolation();
				liMotion.setFault(true);
				motion = liMotion;
				Fields.setMessage("Invalid active frames for %s", mInst);
				return 1;
				
			} else if (instPt == null) {
				// No point defined for given motion instruction
				String error = getErrorMessage(pMInst, prog, false);
				Fields.setMessage(error);
				StaticLinearInterpolation liMotion = new StaticLinearInterpolation();
				liMotion.setFault(true);
				motion = liMotion;
				return 2;
			}
			
			if (mInst.getMotionType() == Fields.MTYPE_JOINT) {
				// Setup joint motion instruction
				updateMotion(instPt.angles, mInst.getSpdMod());
				return 0;
				
			} else if (mInst.getMotionType() == Fields.MTYPE_LINEAR) {
				// Setup linear motion instruction
				Instruction nextInst = prog.getInstAt(nextIdx);
				
				if (mInst.getTermination() > 0 && nextInst instanceof MotionInstruction
						&& !singleExec) {
					// Non-fine termination motion
					Point nextPt;
					
					if (nextInst instanceof PosMotionInst) {
						nextPt = getVector((PosMotionInst)nextInst, prog, false);
						
					} else if (nextInst instanceof CamMoveToObject) {
						nextPt = ((CamMoveToObject) nextInst).getWOPosition();
						
					} else {
						// Invalid motion instruction
						nextPt = null;
						StaticLinearInterpolation liMotion = new StaticLinearInterpolation();
						liMotion.setFault(true);
						motion = liMotion;
						return 3;
					}
					
					updateMotion(instPt, nextPt, mInst.getSpdMod(),
							mInst.getTermination() / 100f);
					return 0;
					
				} else {
					// Fine termination motion
					updateMotion(instPt, mInst.getSpdMod());
					return 0;
				}
			
			} else if (mInst.getMotionType() == Fields.MTYPE_CIRCULAR) {
				// Setup circular motion instruction
				Point endPt = getVector(pMInst, prog, true);
				
				updateMotion(endPt, instPt, mInst.getSpdMod());
				return 0;
				
			} else {
				// Invalid motion type
				StaticLinearInterpolation liMotion = new StaticLinearInterpolation();
				liMotion.setFault(true);
				motion = liMotion;
				return 4;
			}
			
		} else if (mInst instanceof CamMoveToObject) {
			// Setup camera move to instruction
			Instruction nextInst = prog.getInstAt(nextIdx);
			Point tgt = ((CamMoveToObject) mInst).getWOPosition();
			
			if (tgt != null) {
				if (mInst.getTermination() > 0 && nextInst instanceof MotionInstruction
						&& !singleExec) {
					// Non-fine termination motion
					Point nextPt;
					
					if (nextInst instanceof PosMotionInst) {
						nextPt = getVector((PosMotionInst)nextInst, prog, false);
						
					} else if (nextInst instanceof CamMoveToObject) {
						nextPt = ((CamMoveToObject) nextInst).getWOPosition();
						
					} else {
						// Invalid motion instruction
						nextPt = null;
						StaticLinearInterpolation liMotion = new StaticLinearInterpolation();
						liMotion.setFault(true);
						motion = liMotion;
						return 3;
					}
					
					updateMotion(tgt, nextPt, mInst.getSpdMod(),
							mInst.getTermination() / 100f);
					return 0;
					
				} else {
					// Fine termination motion
					updateMotion(tgt, mInst.getSpdMod());
					return 0;
				}
			}
		}
		
		return 2;
	}
	
	@Override
	public String toString() {
		return String.format("R%d", RID);
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param idx
	 * @return
	 */
	public String toolLabel(int idx) {
		ToolFrame tFrame = getToolFrame(idx);
		
		if (tFrame != null) {
			if (tFrame.getName().length() > 0) {
				// Include the frame's name and the given index
				return String.format("%s (%d)", tFrame.getName(), idx + 1);
			}
			
			return Integer.toString(idx);
		}
		
		return null;
	}
	
	/**
	 * Reverts the active program's undo states that have the same group ID as
	 * the undo state on the top of the program undo stack.
	 */
	public void undoProgramEdit() {
		
		if (!PROG_UNDO.isEmpty()) {
			InstUndoState undoState = PROG_UNDO.pop();
			undoState.undo();
			
			// Chain undo states with the same group
			int gid = undoState.getGID();
			
			while (!PROG_UNDO.isEmpty()) {
				undoState = PROG_UNDO.peek();
				
				if (undoState.getGID() != gid) {
					break;
				}
				
				/* TEST CODE *
				Fields.debug("UNDO %s\n", undoState);
				/**/
				undoState.undo();
				PROG_UNDO.pop();
			}
			
		} else {
			Fields.debug("Empty program undo stack!");
		}
	}
	
	/**
	 * Updates the direction of the jog axis for the robot's current motion. If
	 * the robot's current motion is not jog motion, then the motion of the
	 * robot is set to jog motion.
	 * 
	 * @param mdx		The motion axis index
	 * @param newDir	The new direction of motion
	 * @return			The old direction of motion
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
				
				// Set default speed modifiers
				SEGMENT[0].setSpdMod(150f * PConstants.DEG_TO_RAD / 60f);
				SEGMENT[1].setSpdMod(150f * PConstants.DEG_TO_RAD / 60f);
				SEGMENT[2].setSpdMod(200f * PConstants.DEG_TO_RAD / 60f);
				SEGMENT[3].setSpdMod(250f * PConstants.DEG_TO_RAD / 60f);
				SEGMENT[4].setSpdMod(250f * PConstants.DEG_TO_RAD / 60f);
				SEGMENT[5].setSpdMod(420f * PConstants.DEG_TO_RAD / 60f);
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
			PosMotionInst mInst = (PosMotionInst) p.getInstAt(instIdx);
			int posNum = mInst.getCircPosIdx();
			
			if (mInst.getCircPosType() == Fields.PTYPE_PREG) {
				// Update a position register on the robot
				PositionRegister pReg = getPReg(posNum);
				
				if (pReg != null) {
					Point prevPt = pReg.point;
					pReg.point = newPt;
					return prevPt;
				}
				// Uninitialized position register
				return null;
				
			} else if (mInst.getCircPosType() == Fields.PTYPE_PROG) {
				// Update a position in the program
				if (posNum == -1) {
					// In the case of an uninitialized position
					posNum = p.getNextPosition();
					mInst.setCircPosIdx(posNum);
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
			PosMotionInst mInst = (PosMotionInst)p.getInstAt(instIdx);
			int posNum = mInst.getPosIdx();
			
			if (mInst.getPosType() == Fields.PTYPE_PREG) {
				// Update a position register on the robot
				PositionRegister pReg = getPReg(posNum);
				
				if (pReg != null) {
					Point prevPt = pReg.point;
					pReg.point = newPt;
					return prevPt;
				}
				// Uninitialized position register
				return null;
				
			} else if (mInst.getPosType() == Fields.PTYPE_PROG) {
				// Update a position in the program
				if (posNum == -1) {
					// In the case of an uninitialized position
					posNum = p.getNextPosition();
					mInst.setPosIdx(posNum);
				}
				
				return p.setPosition(posNum, newPt);
			}
		}
		
		throw new NullPointerException("arg, newPt, cannot be null for updateMInstPosition()!");
	}
	
	/**
	 * Redefines this robot's motion as rotational interpolation where the
	 * target orientation is defined by the given joint angles and the speed is
	 * defined by this robot's live speed.
	 *  
	 * @param jointAngles	The six joint angles defining the robot's target
	 * 						orientation
	 */
	public void updateMotion(float[] jointAngles) {
		if (motion instanceof JointInterpolation) {
			((JointInterpolation)motion).setupRotationalInterpolation(this,
					jointAngles);
			
		} else {
			motion = new JointInterpolation(this, jointAngles);
		}
	}
	
	/**
	 * Redefines this robot's motion as rotational interpolation where the
	 * target orientation is defined by the given joint angles and the motion
	 * speed is defined by the given speed value.
	 * 
	 * @param jointAngles	The six joint angles defining the robot's target
	 * 						orientation
	 * @param speed			The speed for the rotational interpolation (between
	 * 						0.0 and 1.0)
	 */
	public void updateMotion(float[] jointAngles, float speed) {
		if (motion instanceof JointInterpolation) {
			((JointInterpolation)motion).setupRotationalInterpolation(this,
					jointAngles, speed);
			
		} else {
			motion = new JointInterpolation(this, jointAngles, speed);
		}
	}
	
	/**
	 * Redefines this robot's motion as linear interpolation where target
	 * position and orientation of the robot's tool tip are defined by the
	 * given point. The motion speed is linked to the robot's livespeed.
	 * 
	 * @param tgt	The target point for the robot's tool tip
	 */
	public void updateMotion(Point tgt) {
		motion = new DynamicLinearInterpolation(getToolTipNative(), tgt);
	}
	
	/**
	 * Redefines this robot's motion as linear interpolation where the target
	 * position and orientation of the robot's tool tip are defined by the
	 * given point.
	 * 
	 * @param tgt		The target point for the robot's tool tip
	 * @param speed		The speed of the motion (between 0.0 and 1.0)
	 */
	public void updateMotion(Point tgt, float speed) {
		Point start = getToolTipNative();
		
		if (!(motion instanceof StaticLinearInterpolation)) {
			motion = new StaticLinearInterpolation();
		}
		
		((StaticLinearInterpolation) motion).beginNewLinearMotion(start, tgt,
				speed * motorSpeed);
	}
	
	/**
	 * Redefines this robot's motion as linear interpolation where the target
	 * position is defined by the first point argument and the intermediate
	 * point is defined by second point. The robot will move in the form of an
	 * arc from its current point, to the intermediate point, and finally to
	 * the target point.
	 * 
	 * @param tgt	The final point for the robot's tool tip
	 * @param inter	The intermediate point in the circular motion
	 * @param speed	The speed for the motion (between 0.0 and 1.0)
	 */
	public void updateMotion(Point tgt, Point inter, float speed) {
		Point start = getToolTipNative();
		
		if (!(motion instanceof StaticLinearInterpolation)) {
			motion = new StaticLinearInterpolation();
		}
		
		((StaticLinearInterpolation) motion).beginNewCircularMotion(start, inter, tgt,
				speed * motorSpeed);
	}
	
	/**
	 * Redefines this robot's motion linear interpolation where the target
	 * position and orientation are defined by the first point, the second
	 * point, and the argument, p, the percent of termination. With this motion
	 * type, the robot approaches the target position. However, the closer p is
	 * to 1, the greater the deviation from the target position.
	 * 
	 * @param tgt	The target point
	 * @param next	The next point
	 * @param speed	The speed of the motion
	 * @param p		The percentage of termination with respect to the target
	 * 				point
	 */
	public void updateMotion(Point tgt, Point next, float speed, float p) {
		Point start = getToolTipNative();
		
		if (!(motion instanceof StaticLinearInterpolation)) {
			motion = new StaticLinearInterpolation();
		}
		
		((StaticLinearInterpolation) motion).beginNewContinuousMotion(start, tgt, next, p,
				speed * motorSpeed);
	}

	/**
	 * Updates the robot's position and orientation as well as the robot's
	 * bounding boxes based on its defined motion.
	 */
	public void updateRobot() {	
		if (inMotion()) {
			motion.executeMotion(this);
		}
		
		updateOBBs();
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param idx
	 * @return
	 */
	public String userLabel(int idx) {
		UserFrame uFrame = getUserFrame(idx);
		
		if (uFrame != null) {
			if (uFrame.getName().length() > 0) {
				// Include the frame's name and the given index
				return String.format("%s (%d)", uFrame.getName(), idx + 1);
			}
			
			return Integer.toString(idx);
		}
		
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
			Fields.drawAxes(g, activeTool.getTCPOffset(), toolAxes, 500f);
			
		}
		
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
	
	/**
	 * @return	The active end effector segment, or null if no end effector is
	 * 			active
	 */
	private EndEffector getActiveEE() {
		return EE_LIST[activeEEIdx];
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
	 * 						plate position and orientation
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
	
	private PShape[] loadEEModels() {
		PShape[] eeModels = new PShape[7];
		
		// Load end effector models
		eeModels[0] = MyPShape.loadSTLModel("robot/EE/FACEPLATE.STL", Fields.ROBOT_GREY);
		eeModels[1] = MyPShape.loadSTLModel("robot/EE/SUCTION.stl", Fields.EE_DEFAULT);
		eeModels[2] = MyPShape.loadSTLModel("robot/EE/GRIPPER.stl", Fields.EE_DEFAULT);
		eeModels[3] = MyPShape.loadSTLModel("robot/EE/PINCER.stl", Fields.ROBOT_YELLOW);
		eeModels[4] = MyPShape.loadSTLModel("robot/EE/POINTER.stl", Fields.EE_DEFAULT);
		eeModels[5] = MyPShape.loadSTLModel("robot/EE/GLUE_GUN.stl", Fields.EE_DEFAULT);
		eeModels[6] = MyPShape.loadSTLModel("robot/EE/WIELDER.stl", Fields.EE_DEFAULT);
		
		return eeModels;
	}
	
	/**
	 * Loads 3D meshes for the individual robot joints from model files.
	 * 
	 * @return An array containing the joint model meshes
	 */
	private PShape[] loadJointModels() {
		PShape[] segModels = new PShape[6];
		
		segModels[0] = MyPShape.loadSTLModel("robot/ROBOT_BASE.STL", Fields.ROBOT_YELLOW);
		segModels[1] = MyPShape.loadSTLModel("robot/ROBOT_SEGMENT_1.STL", Fields.ROBOT_GREY);
		segModels[2] = MyPShape.loadSTLModel("robot/ROBOT_SEGMENT_2.STL", Fields.ROBOT_YELLOW);
		segModels[3] = MyPShape.loadSTLModel("robot/ROBOT_SEGMENT_3.STL", Fields.ROBOT_GREY);
		segModels[4] = MyPShape.loadSTLModel("robot/ROBOT_SEGMENT_4.STL", Fields.ROBOT_GREY);
		segModels[5] = MyPShape.loadSTLModel("robot/ROBOT_SEGMENT_5.STL", Fields.ROBOT_YELLOW);
		
		return segModels;
	}
	
	/**
	 * Adds the undo state defined the given parameters to the program undo
	 * stack.
	 * 
	 * @param type	The undo state type (i.e. edit, remove, etc.)
	 * @param idx	The index in the program of the modified instruction
	 * @param prog	The program referenced by the undo state
	 * @param inst	The instruction related to the undo state
	 * @param group	Whether to group this undo state with previous undo states
	 */
	private void pushUndoState(InstUndoType type, Program prog, int idx,
			InstElement inst, boolean group) {
		
		if (PROG_UNDO.size() >= Program.MAX_UNDO_SIZE) {
			// Remove old unused undo states to make room for new undo states
			PROG_UNDO.remove(0);
		}
		
		// Determine the group ID of the undo state
		int gid;
		
		if (PROG_UNDO.isEmpty()) {
			gid = 0;
			
		} else {
			InstUndoState top = PROG_UNDO.peek();
			
			if ((top.getGID() == 1 && group) || (top.getGID() == 0 && !group)) {
				gid = 1;
				
			} else {
				gid = 0;
			}
		}
		
		InstUndoState undoState = new InstUndoState(type, gid, prog, idx, inst);
		PROG_UNDO.push(undoState);
		/* TEST CODE *
		Fields.debug("%s\n", undoState);
		/**/
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
}


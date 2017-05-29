package robot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import enums.AxesDisplay;
import enums.CoordFrame;
import enums.EEType;
import enums.InstOp;
import enums.RobotMotion;
import frame.ToolFrame;
import frame.UserFrame;
import geom.BoundingBox;
import geom.Part;
import geom.Point;
import geom.RMatrix;
import geom.RQuaternion;
import geom.RRay;
import geom.WorldObject;
import global.Fields;
import global.RMath;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PShape;
import processing.core.PVector;
import programming.Instruction;
import programming.MotionInstruction;
import programming.Program;
import regs.DataRegister;
import regs.IORegister;
import regs.PositionRegister;
import screen.DisplayLine;
import screen.InstState;

public class RoboticArm {
	/**
	 * The unique ID of this robot.
	 */
	public final int RID;
	
	// Indicates the direction of motion of the Robot when jogging
	public final float[] jogLinear;
	public final float[] jogRot;
	
	public RQuaternion tgtOrientation;
	public PVector tgtPosition;
	
	public int liveSpeed;
	public float motorSpeed;
	
	public Part held;
	
	/**
	 * The position of the center of the robot's base segment
	 */
	private final PVector BASE_POSITION;
	
	/**
	 * A list of the robot's arm segment models.
	 */
	private final ArrayList<Model> SEGMENTS;
	
	/**
	 * A set of matrices used for calculations the robot's end effector
	 * position and draw the robot's segments.
	 */
	private final ArrayList<RMatrix> SEGMENT_TMATS;
	
	/**
	 * A model for one of the robot's end effectors
	 */
	private final Model EEM_SUCTION, EEM_CLAW, EEM_CLAW_PINCER, EEM_POINTER,
						EEM_GLUE_GUN, EEM_WIELDER;
	
	/**
	 * The set of bounding boxes for the robot's arm segments.
	 */
	private final BoundingBox[] ARM_OBBS;
	
	/**
	 * A set mapping each end effector to its respective bounding boxes.
	 */
	private final HashMap<EEType, ArrayList<BoundingBox>> EE_TO_OBBS;
	
	/**
	 * A set mapping each end effector to its pickup bounding boxes. As of now,
	 * only the claw and suction end effector have pickup boxes, since they are
	 * the only end effectors that can be used to pickup parts.
	 */
	private final HashMap<EEType, ArrayList<BoundingBox>> EE_TO_PICK_OBBS;
	
	/**
	 * A set mapping each end effector to the index of its I/O register
	 * associated with this robot.
	 */
	private final HashMap<EEType, Integer> EE_TO_IOREG;
	
	/**
	 * The list of programs associated with this robot.
	 */
	private final ArrayList<Program> PROGRAMS;
	
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
	 * The I/O registers associated with this robot.
	 */
	private final IORegister[] IOREG;
	
	/**
	 * A set of user-defined frames associated with this robot. 
	 */
	private final ToolFrame[] TOOL_FRAMES;
	
	/**
	 * A set of user-defined frames associated with this robot.
	 */
	private final UserFrame[] USER_FRAMES;
	
	/**
	 * The initial position and orientation of the robot.
	 */
	private Point robotPoint;
	
	/**
	 * The active end effector of the Robot/
	 */
	private EEType activeEndEffector;
	
	/**
	 * The state (i.e. on or off) of the Robot's current end effector.
	 */
	private int endEffectorState;
	
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
	 * The robot's current motion state. This indicates whether the robot is
	 * moving in the joint coordinate frame, a cartesian coordinate frame, or
	 * is not moving.
	 */
	private RobotMotion motionType;
	
	/**
	 * The current coordinate frame of the robot.
	 */
	private CoordFrame curCoordFrame;
	
	/**
	 * An index corresponding to the active frame for this robot.
	 */
	private int activeUserIdx, activeToolIdx;
	
	
	private RMatrix lastTipTMatrix;
	
	private boolean trace;
	
	private ArrayList<PVector> tracePts;

	/**
	 * Creates a robot with the given ID at the given position with the given
	 * segment and end effector models.
	 * 
	 * @param rid			The unique identifier associated with the Robot
	 * @param basePos		The center position of the Robot's base segment
	 * @param robotModels	The set of both the segment and end effector models
	 * 						for this robot
	 */
	public RoboticArm(int rid, PVector basePos, PShape[] robotModels, ArrayList<RMatrix> segmentTMats) {
		int idx;
		
		jogLinear = new float[3];
		jogRot = new float[3];
		
		motorSpeed = 1000.0f; // speed in mm/sec
		liveSpeed = 10;
		
		RID = rid;
		SEGMENTS = new ArrayList<>();
		SEGMENT_TMATS = segmentTMats;
		BASE_POSITION = basePos;
		
		// Initialize program fields
		PROGRAMS = new ArrayList<>();
		CALL_STACK = new Stack<>();
		PROG_UNDO = new Stack<>();
		
		activeProgIdx = -1;
		activeInstIdx = -1;
		
		// Initializes the frames
		
		TOOL_FRAMES = new ToolFrame[Fields.FRAME_NUM];
		USER_FRAMES = new UserFrame[Fields.FRAME_NUM];
		
		for (idx = 0; idx < TOOL_FRAMES.length; ++idx) {
			TOOL_FRAMES[idx] = new ToolFrame();
			USER_FRAMES[idx] = new UserFrame();
		}
		
		curCoordFrame = CoordFrame.JOINT;
		activeUserIdx = -1;
		activeToolIdx = -1;
		
		// Initialize the registers
		
		DREG = new DataRegister[Fields.DPREG_NUM];
		PREG = new PositionRegister[Fields.DPREG_NUM];
		IOREG = new IORegister[Fields.IOREG_NUM];
		
		for (idx = 0; idx < DREG.length; ++idx) {
			DREG[idx] = new DataRegister(idx);
			PREG[idx] = new PositionRegister(idx);
		}
		
		// Associated each End Effector with an I/O Register
		idx = 0;
		IOREG[idx] = new IORegister(idx++, (EEType.SUCTION).name(), Fields.OFF);
		IOREG[idx] = new IORegister(idx++, (EEType.CLAW).name(), Fields.OFF);
		IOREG[idx] = new IORegister(idx++, (EEType.POINTER).name(), Fields.OFF);
		IOREG[idx] = new IORegister(idx++, (EEType.GLUE_GUN).name(), Fields.OFF);
		IOREG[idx] = new IORegister(idx++, (EEType.WIELDER).name(), Fields.OFF);
		
		activeEndEffector = EEType.NONE;
		endEffectorState = Fields.OFF;
		// Initialize the End Effector to IO Register mapping
		EE_TO_IOREG = new HashMap<>();
		EE_TO_IOREG.put(EEType.SUCTION, 0);
		EE_TO_IOREG.put(EEType.CLAW, 1);
		EE_TO_IOREG.put(EEType.POINTER, 2);
		EE_TO_IOREG.put(EEType.GLUE_GUN, 3);
		EE_TO_IOREG.put(EEType.WIELDER, 4);

		EEM_SUCTION = new Model("suction", robotModels[0]);
		EEM_CLAW = new Model("grippers", robotModels[1]);
		EEM_CLAW_PINCER = new Model("pincer", robotModels[2]);
		EEM_POINTER = new Model("pointer", robotModels[3]);
		EEM_GLUE_GUN = new Model("glue_gun", robotModels[4]);
		EEM_WIELDER = new Model("wielder", robotModels[5]);

		motionType = RobotMotion.HALTED;
		
		// Base
		Model base = new Model("base", robotModels[6]);
		base.rotations[1] = true;
		base.jointRanges[1] = new PVector(0, PConstants.TWO_PI);
		base.rotationSpeed = PApplet.radians(150)/60.0f;
		// Joint 1
		Model axis1 = new Model("axis_1", robotModels[7]);
		axis1.rotations[2] = true;
		axis1.jointRanges[2] = new PVector(4.34f, 2.01f);
		axis1.rotationSpeed = PApplet.radians(150)/60.0f;
		// Joint 2
		Model axis2 = new Model("axis_2", robotModels[8]);
		axis2.rotations[2] = true;
		axis2.jointRanges[2] = new PVector(5.027f, 4.363f);
		axis2.rotationSpeed = PApplet.radians(200)/60.0f;
		// Joint 3
		Model axis3 = new Model("axis_3", robotModels[9]);
		axis3.rotations[0] = true;
		axis3.jointRanges[0] = new PVector(0, PConstants.TWO_PI);
		axis3.rotationSpeed = PApplet.radians(250)/60.0f;
		// Joint 4
		Model axis4 = new Model("axis_4", robotModels[10]);
		axis4.rotations[2] = true;
		axis4.jointRanges[2] = new PVector(240f * PConstants.DEG_TO_RAD, 130f * PConstants.DEG_TO_RAD);
		/** Origin bounds
		axis4.jointRanges[2] = new PVector(59f * Fields.PI / 40f, 11f * Fields.PI / 20f);
		/**/
		axis4.rotationSpeed = PApplet.radians(250)/60.0f;
		// Joint 5
		Model axis5 = new Model("axis_5", robotModels[11]);
		axis5.rotations[0] = true;
		axis5.jointRanges[0] = new PVector(0, PConstants.TWO_PI);
		axis5.rotationSpeed = PApplet.radians(420)/60.0f;
		// Joint 6
		Model axis6 = new Model("axis_6", robotModels[12]);
		SEGMENTS.add(base);
		SEGMENTS.add(axis1);
		SEGMENTS.add(axis2);
		SEGMENTS.add(axis3);
		SEGMENTS.add(axis4);
		SEGMENTS.add(axis5);
		SEGMENTS.add(axis6);

		for(idx = 0; idx < jogLinear.length; ++idx) {
			jogLinear[idx] = 0;
		}

		for(idx = 0; idx < jogRot.length; ++idx) {
			jogRot[idx] = 0;
		}

		/* Initializes dimensions of the Robot Arm's hit boxes */
		ARM_OBBS = new BoundingBox[7];

		ARM_OBBS[0] = new BoundingBox(420, 115, 420);
		ARM_OBBS[1] = new BoundingBox(317, 85, 317);
		ARM_OBBS[2] = new BoundingBox(130, 185, 170);
		ARM_OBBS[3] = new BoundingBox(74, 610, 135);
		ARM_OBBS[4] = new BoundingBox(165, 165, 165);
		ARM_OBBS[5] = new BoundingBox(160, 160, 160);
		ARM_OBBS[6] = new BoundingBox(128, 430, 128);

		EE_TO_OBBS = new HashMap<>();
		EE_TO_PICK_OBBS = new HashMap<>();
		// Faceplate
		ArrayList<BoundingBox> limbo = new ArrayList<BoundingBox>();
		limbo.add( new BoundingBox(36, 96, 96) );
		EE_TO_OBBS.put(EEType.NONE, limbo);
		// Cannot pickup
		limbo = new ArrayList<>();
		EE_TO_PICK_OBBS.put(EEType.NONE, limbo);

		// Claw Gripper
		limbo = new ArrayList<BoundingBox>();
		limbo.add( new BoundingBox(54, 96, 96) );
		limbo.add( new BoundingBox(31, 21, 89) );
		limbo.add( new BoundingBox(31, 21, 89) );
		EE_TO_OBBS.put(EEType.CLAW, limbo);
		// In between the grippers
		limbo = new ArrayList<BoundingBox>();
		limbo.add(new BoundingBox(15, 3, 55) );
		limbo.get(0).setColor(Fields.OBB_HELD);
		EE_TO_PICK_OBBS.put(EEType.CLAW, limbo);

		// Suction
		limbo = new ArrayList<BoundingBox>();
		limbo.add( new BoundingBox(54, 96, 96) );
		limbo.add( new BoundingBox(82, 37, 37) );
		limbo.add( new BoundingBox(37, 62, 37) );
		EE_TO_OBBS.put(EEType.SUCTION, limbo);
		// One for each suction cup
		limbo = new ArrayList<BoundingBox>();
		limbo.add(new BoundingBox(3, 25, 25) );
		limbo.get(0).setColor(Fields.OBB_HELD);
		limbo.add(new BoundingBox(25, 3, 25) );
		limbo.get(1).setColor(Fields.OBB_HELD);
		EE_TO_PICK_OBBS.put(EEType.SUCTION, limbo);

		// Pointer
		limbo = new ArrayList<>();
		EE_TO_OBBS.put(EEType.POINTER, limbo);
		// Cannot pickup
		limbo = new ArrayList<>();
		EE_TO_PICK_OBBS.put(EEType.POINTER, limbo);

		// Glue Gun
		limbo = new ArrayList<>();
		EE_TO_OBBS.put(EEType.GLUE_GUN, limbo);
		// Cannot pickup
		limbo = new ArrayList<>();
		EE_TO_PICK_OBBS.put(EEType.GLUE_GUN, limbo);

		// Wielder
		limbo = new ArrayList<>();
		EE_TO_OBBS.put(EEType.WIELDER, limbo);
		// Cannot pickup
		limbo = new ArrayList<>();
		EE_TO_PICK_OBBS.put(EEType.WIELDER, limbo);

		held = null;
		trace = false;
		tracePts = new ArrayList<PVector>();
		
		robotPoint = getFacePlatePoint(
				new float[] { 0f, 0f, 0f, 0f, 0f, 0f }
		);
		
		// Initializes the old transformation matrix for the arm model
		lastTipTMatrix = getRobotTransform( getJointAngles() );
		/* REMOVE AFTER REFACTOR *
		RobotRun.getInstance().pushMatrix();
		RobotRun.applyModelRotation(this, getJointAngles());
		lastEEOrientation = RobotRun.getInstance().getTransformationMatrix();
		RobotRun.getInstance().popMatrix();
		/**/
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
		RobotRun app = RobotRun.getInstance();

		if (!app.isShift() || hasMotionFault()) {
			// Only move when shift is set and there is no error
			return 0f;
		}

		return setJointMotion(joint, dir);
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
		RobotRun app = RobotRun.getInstance();
		
		if (!app.isShift() || hasMotionFault()) {
			// Only move when shift is set and there is no error
			return 0f;
		}

		/* Initiaize the Robot's destination */
		// TODO REMOVE AFTER REFACTOR Point RP = RobotRun.nativeRobotEEPoint(this, getJointAngles());
		Point RP = getToolTipNative();
		tgtPosition = RP.position;
		tgtOrientation = RP.orientation;
		/**/

		if(axis >= 0 && axis < 3) {
			if(jogLinear[axis] == 0) {
				// Begin movement on the given axis in the given direction
				jogLinear[axis] = dir;
			} else {
				// Halt movement
				jogLinear[axis] = 0;
			}

			return jogLinear[axis];
		}
		else if(axis >= 3 && axis < 6) {
			axis %= 3;
			if(jogRot[axis] == 0) {
				// Begin movement on the given axis in the given direction
				jogRot[axis] = dir;
			}
			else {
				// Halt movement
				jogRot[axis] = 0;
			}

			return jogRot[axis];
		}

		return 0f;
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

			if(PROGRAMS.size() < 1) {
				PROGRAMS.add(p);
				
			}  else {
				while(idx < PROGRAMS.size() && PROGRAMS.get(idx).getName().compareTo(p.getName()) < 0) { ++idx; }
				PROGRAMS.add(idx, p);
			}

			return idx;
		}
	}
	
	/**
	 * Determines if the given angle is within the bounds of valid angles for
	 * the Robot's joint corresponding to the given index value.
	 * 
	 * @param joint  An integer between 0 and 5 which corresponds to one of
	 *               the Robot's joints J1 - J6
	 * @param angle  The angle in question
	 */
	public boolean anglePermitted(int joint, float angle) {
		joint = PApplet.abs(joint) % 6;
		// Get the joint's range bounds
		PVector rangeBounds = getJointRange(joint);
		return RMath.angleWithinBounds(RMath.mod2PI(angle), rangeBounds.x, rangeBounds.y);
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
	 * @returning The point, pt, interms of the given coordinate system
	 */
	public Point applyFrame(Point pt, PVector origin, RQuaternion axes) {
		PVector position = RMath.vToFrame(pt.position, origin, axes);
		RQuaternion orientation = axes.transformQuaternion(pt.orientation);
		// Update joint angles associated with the point
		float[] newJointAngles = RMath.inverseKinematics(this, pt.angles, position, orientation);

		if (newJointAngles != null) {
			return new Point(position, orientation, newJointAngles);
		} else {
			// If inverse kinematics fails use the old angles
			return new Point(position, orientation, pt.angles);
		}
	}

	/**
	 * Determines if the given part's bounding box is colliding with the
	 * Robot's end effector's pickup bounding boxes. Only the claw gripper and
	 * suction end effectors have pickup bounding boxes, which appear blue in
	 * the application. When a part's bounding box is colliding with a special
	 * bounding box and not colliding with another bounding box of the active
	 * end effector, then true is returned. Otherwise,, false is returned.
	 * 
	 * @return	Whether can immediately be picked up by the robot
	 */
	public boolean canPickup(Part p) {
		if (p == null) {
			return false;
		}
		
		ArrayList<BoundingBox> curEEOBBs = EE_TO_OBBS.get(activeEndEffector);

		for (BoundingBox b : curEEOBBs) {
			// Cannot be colliding with a normal bounding box
			if (p.collision(b)) {
				return false;
			}
		}

		curEEOBBs = EE_TO_PICK_OBBS.get(activeEndEffector);

		for (BoundingBox b : curEEOBBs) {
			// Must be colliding with a pickup bounding box
			if (p.collision(b)) {
				return true;
			}
		}

		return false;
	}
	
	/* Determine if the given object is colliding with any part of the Robot. */
	public boolean checkObjectCollision(Part obj) {
		boolean collision = false;

		for(BoundingBox b : ARM_OBBS) {
			if( obj.collision(b) ) {
				b.setColor(Fields.OBB_COLLISION);
				collision = true;
			}
		}

		ArrayList<BoundingBox> eeHBs = EE_TO_OBBS.get(activeEndEffector);

		for(BoundingBox b : eeHBs) {
			if(obj.collision(b)) {
				b.setColor(Fields.OBB_COLLISION);
				collision = true;
			}
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
	 * @param active	The scenario, of which to check the world objects
	 * @return			0, if an part can be picked up
	 * 					1, if the robot releases a part
	 * 					2, if nothing occurs
	 */
	public int checkPickupCollision(Scenario active) {
		// End Effector must be on and no object is currently held to be able to pickup an object
		if (endEffectorState == Fields.ON && held == null) {
			ArrayList<BoundingBox> curPUEEOBBs = EE_TO_PICK_OBBS.get(activeEndEffector);

			// Can this End Effector pick up objects?
			if (active != null && curPUEEOBBs.size() > 0) {

				for (WorldObject wldObj : active) {
					// Only parts can be picked up
					if (wldObj instanceof Part && canPickup( (Part)wldObj )) {
						// Pickup the object
						held = (Part)wldObj;
						return 0;
					}
				}
			}

		} else if (endEffectorState == Fields.OFF && held != null) {
			// Release the object
			releaseHeldObject();
			return 1;
		}

		return 2;
	}
	
	/**
	 * Determine if select pairs of hit boxes of the robot are colliding.
	 * 
	 * The bounding box collisions checked between the body segments of the Arm:
	 * The base segment and the four upper arm segments
	 * The base rotating segment and lower long arm segment as well as the upper long arm and
	 *   upper rotating end segment
	 * The second base rotating hit box and the upper long arm segment as well as the upper
	 *   rotating end segment
	 * The lower long arm segment and the upper rotating end segment
	 * 
	 * @return	A self-collision has occurred with the robot
	 */
	public boolean checkSelfCollisions() {
		boolean collision = false;

		// Pairs of indices corresponding to two of the Arm body hit boxes, for which to check collisions
		int[] check_pairs = new int[] { 0, 3, 0, 4, 0, 5, 0, 6, 1, 5, 1, 6, 2, 5, 2, 6, 3, 5 };

		/**
		 * Check select collisions between the body segments of the Arm:
		 * The base segment and the four upper arm segments
		 * The base rotating segment and lower long arm segment as well as the upper long arm and
		 *   upper rotating end segment
		 * The second base rotating hit box and the upper long arm segment as well as the upper
		 *   rotating end segment
		 * The lower long arm segment and the upper rotating end segment
		 */
		for(int idx = 0; idx < check_pairs.length - 1; idx += 2) {
			if( Part.collision3D(ARM_OBBS[ check_pairs[idx] ], ARM_OBBS[ check_pairs[idx + 1] ]) ) {
				ARM_OBBS[ check_pairs[idx] ].setColor(Fields.OBB_COLLISION);
				ARM_OBBS[ check_pairs[idx + 1] ].setColor(Fields.OBB_COLLISION);
				collision = true;
			}
		}

		ArrayList<BoundingBox> eeHB = EE_TO_OBBS.get(activeEndEffector);

		// Check collisions between all EE hit boxes and base as well as the first long arm hit boxes
		for(BoundingBox hb : eeHB) {
			for(int idx = 0; idx < 4; ++idx) {
				if(Part.collision3D(hb, ARM_OBBS[idx]) ) {
					hb.setColor(Fields.OBB_COLLISION);
					ARM_OBBS[idx].setColor(Fields.OBB_COLLISION);
					collision = true;
				}
			}
		}

		return collision;
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
		
		for(BoundingBox b : ARM_OBBS) {
			PVector collPt = b.collision(ray);
			
			if (collPt != null && (closestCollPt == null ||
					PVector.dist(ray.getOrigin(), collPt) <
					PVector.dist(ray.getOrigin(), closestCollPt))) {
				
				// Find the closest collision to the ray origin
				closestCollPt = collPt;
			}
		}

		ArrayList<BoundingBox> eeHBs = EE_TO_OBBS.get(activeEndEffector);

		for(BoundingBox b : eeHBs) {
			PVector collPt = b.collision(ray);

			if (collPt != null && (closestCollPt == null ||
					PVector.dist(ray.getOrigin(), collPt) <
					PVector.dist(ray.getOrigin(), closestCollPt))) {
				
				// Find the closest collision to the ray origin
				closestCollPt = collPt;
			}
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
		EEType[] values = EEType.values();
		int nextEE = (activeEndEffector.ordinal() + 1) % values.length;
		// Switch to the next End Effector in the cycle
		setActiveEE( values[nextEE] );
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
		
		/* DRAW ROBOT SEGMENTS */
		
		g.pushStyle();
		g.noStroke();
		g.fill(200, 200, 0);

		g.pushMatrix();
		
		if (axesType != AxesDisplay.NONE && curCoordFrame == CoordFrame.USER) {
			
			UserFrame activeUser = getActiveUser();
			// Render the active user frame
			if (activeUser != null) {
				RMatrix userAxes = RMath.rMatToWorld(activeUser.getNativeAxisVectors());
				
				if (axesType == AxesDisplay.AXES) {
					Fields.drawGridlines(g, activeUser.getOrigin(), userAxes, 10000f, Fields.ORANGE);
					
				} else if (axesType == AxesDisplay.GRID) {
					drawGridlines(g, userAxes, activeUser.getOrigin(), 35, 100);
				}
			}
		}
		
		g.translate(BASE_POSITION.x, BASE_POSITION.y, BASE_POSITION.z);

		g.rotateZ(PConstants.PI);
		g.rotateY(PConstants.PI/2);
		SEGMENTS.get(0).draw(g);
		g.rotateY(-PConstants.PI/2);
		g.rotateZ(-PConstants.PI);

		//g.fill(50);

		g.translate(-50, -166, -358);
		g.rotateZ(PConstants.PI);
		g.translate(150, 0, 150);
		g.rotateX(PConstants.PI);
		g.rotateY(jointAngles[0]);
		g.rotateX(-PConstants.PI);
		g.translate(-150, 0, -150);
		SEGMENTS.get(1).draw(g);
		g.rotateZ(-PConstants.PI);

		//g.fill(200, 200, 0);

		g.translate(-115, -85, 180);
		g.rotateZ(PConstants.PI);
		g.rotateY(PConstants.PI/2);
		g.translate(0, 62, 62);
		g.rotateX(jointAngles[1]);
		g.translate(0, -62, -62);
		SEGMENTS.get(2).draw(g);
		g.rotateY(-PConstants.PI/2);
		g.rotateZ(-PConstants.PI);

		//g.fill(50);

		g.translate(0, -500, -50);
		g.rotateZ(PConstants.PI);
		g.rotateY(PConstants.PI/2);
		g.translate(0, 75, 75);
		g.rotateZ(PConstants.PI);
		g.rotateX(jointAngles[2]);
		g.rotateZ(-PConstants.PI);
		g.translate(0, -75, -75);
		SEGMENTS.get(3).draw(g);
		g.rotateY(PConstants.PI/2);
		g.rotateZ(-PConstants.PI);

		g.translate(745, -150, 150);
		g.rotateZ(PConstants.PI/2);
		g.rotateY(PConstants.PI/2);
		g.translate(70, 0, 70);
		g.rotateY(jointAngles[3]);
		g.translate(-70, 0, -70);
		SEGMENTS.get(4).draw(g);
		g.rotateY(-PConstants.PI/2);
		g.rotateZ(-PConstants.PI/2);

		//g.fill(200, 200, 0);

		g.translate(-115, 130, -124);
		g.rotateZ(PConstants.PI);
		g.rotateY(-PConstants.PI/2);
		g.translate(0, 50, 50);
		g.rotateX(jointAngles[4]);
		g.translate(0, -50, -50);
		SEGMENTS.get(5).draw(g);
		g.rotateY(PConstants.PI/2);
		g.rotateZ(-PConstants.PI);
		
		//g.fill(50);
		
		g.translate(150, -10, 95);
		g.rotateY(-PConstants.PI/2);
		g.rotateZ(PConstants.PI);
		g.translate(45, 45, 0);
		g.rotateZ(jointAngles[5]);
		
		g.pushMatrix();
		
		g.translate(-45, -45, 0);
		SEGMENTS.get(6).draw(g);
		
		/* DRAW END EFFECTOR MODEL */

		if (activeEndEffector == EEType.SUCTION) {
			g.rotateY(PConstants.PI);
			g.translate(-88, -37, 0);
			EEM_SUCTION.draw(g);

		} else if(activeEndEffector == EEType.CLAW) {
			g.rotateY(PConstants.PI);
			g.translate(-88, 0, 0);
			EEM_CLAW.draw(g);
			g.rotateZ(PConstants.PI/2);

			if(endEffectorState == Fields.OFF) {
				// Draw open grippers
				g.translate(10, -85, 30);
				EEM_CLAW_PINCER.draw(g);
				g.translate(55, 0, 0);
				EEM_CLAW_PINCER.draw(g);

			} else if(endEffectorState == Fields.ON) {
				// Draw closed grippers
				g.translate(28, -85, 30);
				EEM_CLAW_PINCER.draw(g);
				g.translate(20, 0, 0);
				EEM_CLAW_PINCER.draw(g);
			}
		} else if (activeEndEffector == EEType.POINTER) {
			g.rotateY(PConstants.PI);
			g.rotateZ(PConstants.PI);
			g.translate(45, -45, 10);
			EEM_POINTER.draw(g);

		} else if (activeEndEffector == EEType.GLUE_GUN) {
			g.rotateZ(PConstants.PI);
			g.translate(-48, -46, -12);
			EEM_GLUE_GUN.draw(g);

		} else if (activeEndEffector == EEType.WIELDER) {
			g.rotateY(PConstants.PI);
			g.rotateZ(PConstants.PI);
			g.translate(46, -44, 10);
			EEM_WIELDER.draw(g);
		}
		
		g.popMatrix();
		
		/* DRAW TOOL TIP */
		
		g.rotateX(PConstants.PI);
		g.rotateY(PConstants.PI/2);
		drawToolTip(g, axesType);

		g.popMatrix();
		g.popStyle();
		
		/* DRAW BOUNDING BOXES */
		
		if (drawOBBs) {
			// Draw hit boxes of the body portion of the Robot Arm
			for(BoundingBox b : ARM_OBBS) {
				g.pushMatrix();
				Fields.transform(g, b.getCenter(), b.getOrientationAxes());
				b.getFrame().draw(g);
				g.popMatrix();
			}

			ArrayList<BoundingBox> curEEHitBoxes = EE_TO_OBBS.get(activeEndEffector);

			// Draw End Effector hit boxes
			for(BoundingBox b : curEEHitBoxes) {
				g.pushMatrix();
				Fields.transform(g, b.getCenter(), b.getOrientationAxes());
				b.getFrame().draw(g);
				g.popMatrix();
			}

			curEEHitBoxes = EE_TO_PICK_OBBS.get(activeEndEffector);
			// Draw Pickup hit boxes
			for (BoundingBox b : curEEHitBoxes) {
				g.pushMatrix();
				Fields.transform(g, b.getCenter(), b.getOrientationAxes());
				b.getFrame().draw(g);
				g.popMatrix();
			}
		}
		
		if (modelInMotion() && trace) {
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
	 * TODO comment this
	 * 
	 * @param g			The graphics object used to render the tool tip position
	 * @param axesType
	 */
	private void drawToolTip(PGraphics g, AxesDisplay axesType) {
		
		ToolFrame activeTool = getActiveTool();
		
		if (axesType != AxesDisplay.NONE && curCoordFrame == CoordFrame.TOOL
				&& activeTool != null) {
			
			// Render the active tool frame at the position of the tooltip
			RMatrix toolAxes = RMath.rMatToWorld(activeTool.getOrientationOffset().toMatrix());
			Fields.drawGridlines(g, activeTool.getTCPOffset(), toolAxes, 500f, Fields.PINK);
			
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
	 * TODO comment this
	 * 
	 * @param g
	 * @param axesVectors
	 * @param origin
	 * @param halfNumOfLines
	 * @param distBwtLines
	 */
	public void drawGridlines(PGraphics g, RMatrix axesVectors, PVector origin,
			int halfNumOfLines, float distBwtLines) {
		
		float[][] axesDat = axesVectors.getFloatData();
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
				0, 1, 0, BASE_POSITION.y,
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
	 * TODO comment this
	 * 
	 * @param g	
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
	 * Move the Robot, based on the current Coordinate Frame and the current values
	 * of the each segments jointsMoving array or the values in the Robot's jogLinear
	 * and jogRot arrays.
	 */
	public void executeLiveMotion() {

		if (curCoordFrame == CoordFrame.JOINT) {
			// Jog in the Joint Frame
			for(int i = 0; i < SEGMENTS.size(); i += 1) {
				Model model = SEGMENTS.get(i);

				for(int n = 0; n < 3; n++) {
					if(model.rotations[n]) {
						float trialAngle = model.currentRotations[n] +
								model.rotationSpeed * model.jointsMoving[n] * liveSpeed / 100f;
						trialAngle = RMath.mod2PI(trialAngle);
						
						if(model.anglePermitted(n, trialAngle)) {
							model.currentRotations[n] = trialAngle;
						} 
						else {
							Fields.debug("A[i%d, n=%d]: %f\n", i, n, trialAngle);
							model.jointsMoving[n] = 0;
							RobotRun.getInstance().updateRobotJogMotion(i, 0);
							RobotRun.getInstance().hold();
						}
					}
				}
			}

		} else {
			// Jog in the World, Tool or User Frame
			Point curPoint = getToolTipNative();
			/* TODO REMOVE AFTER REFACTOR *
			Point curPoint = RobotRun.nativeRobotEEPoint(this, getJointAngles());
			/**/
			RQuaternion invFrameOrientation = null;
			
			if (curCoordFrame == CoordFrame.TOOL) {
				ToolFrame activeTool = getActiveTool();
				
				if (activeTool != null) {
					RQuaternion diff = curPoint.orientation.transformQuaternion(robotPoint.orientation.conjugate());
					invFrameOrientation = diff.transformQuaternion(activeTool.getOrientationOffset().clone()).conjugate();
				}
				
			} else if (curCoordFrame == CoordFrame.USER) {
				UserFrame activeUser = getActiveUser();
				
				if (activeUser != null) {
					invFrameOrientation = activeUser.getOrientation().conjugate();
				}
			}

			// Apply translational motion vector
			if (translationalMotion()) {
				// Respond to user defined movement
				float distance = motorSpeed / 6000f * liveSpeed;
				PVector translation = new PVector(-jogLinear[0], -jogLinear[2], jogLinear[1]);
				translation.mult(distance);

				if (invFrameOrientation != null) {
					// Convert the movement vector into the current reference frame
					translation = invFrameOrientation.rotateVector(translation);
				}

				tgtPosition.add(translation);
			} else {
				// No translational motion
				tgtPosition = curPoint.position;
			}

			// Apply rotational motion vector
			if (rotationalMotion()) {
				// Respond to user defined movement
				float theta = PConstants.DEG_TO_RAD * 0.025f * liveSpeed;
				PVector rotation = new PVector(-jogRot[0], -jogRot[2], jogRot[1]);

				if (invFrameOrientation != null) {
					// Convert the movement vector into the current reference frame
					rotation = invFrameOrientation.rotateVector(rotation);
				}
				rotation.normalize();

				tgtOrientation.rotateAroundAxis(rotation, theta);

				if (tgtOrientation.dot(curPoint.orientation) < 0f) {
					// Use -q instead of q
					tgtOrientation.scalarMult(-1);
				}
			} else {
				// No rotational motion
				tgtOrientation = curPoint.orientation;
			}

			jumpTo(tgtPosition, tgtOrientation);
		}
	}
	
	/**
	 * @return	The robot's active end effector
	 */
	public EEType getActiveEE() {
		return activeEndEffector;
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
		
		return prog.getInstAt(activeInstIdx);
	}
	
	/**
	 * @return	The active for this Robot, or null if no program is active
	 */
	public Program getActiveProg() {
		if (activeProgIdx < 0 || activeProgIdx >= PROGRAMS.size()) {
			// Invalid program index
			return null;
		}
		
		return PROGRAMS.get(activeProgIdx);
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
		
		return TOOL_FRAMES[activeToolIdx];
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
		
		return USER_FRAMES[activeUserIdx];
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
		return robotPoint.clone();
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
	 * @return	A point representing the robot's current faceplate position
	 * 			and orientation
	 */
	public Point getFacePlatePoint() {
		return getFacePlatePoint( getJointAngles() );
	}
	
	/**
	 * Calculates the position and orientation of the robot's faceplate based
	 * on the native coordinate system and the given joint angles.
	 * 
	 * @jointAngles	The angles used to calculate the robot's faceplate point
	 * @return		A point representing the robot faceplate's position and
	 * 				orientation
	 */
	public Point getFacePlatePoint(float[] jointAngles) {
		RMatrix tipOrien = getRobotTransform(jointAngles);
		
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
	 * TODO comment this
	 * 
	 * @param jointAngles
	 * @param tFrame
	 * @param uFrame
	 * @return
	 */
	private Point getToolTipPoint(float[] jointAngles, ToolFrame tFrame, UserFrame uFrame) {
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
	 * @return	The state of the robot's current end effector
	 */
	public int getEEState() {
		return endEffectorState;
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
			Instruction inst = p.getInstAt(idx);
			
			pushInstState(InstOp.REPLACED, idx, inst.clone());
			
			if (Fields.DEBUG) {
				//System.out.printf("\nEDIT %d %s\n\n", idx, inst.getClass());
			}
			
			return inst;
		}
		
		return null;
	}

	/**
	 * Returns the I/O register, associated with the given index, of the Robot,
	 * or null if the given index is invalid. A Robot has a total of 5 I/O
	 * registers, which are zero-indexed: one for each different end effector.
	 * 
	 * @param rdx	A integer value between 0 and 4, inclusive
	 * @return		The I/O register associated with the given index, or null
	 * 				if the given index is invalid.
	 */
	public IORegister getIOReg(int rdx) {
		if (rdx >= 0 && rdx < IOREG.length) {
			return IOREG[rdx];
			
		} else {
			// Invalid index
			return null;
		}
	}
	
	/**
	 * Returns the I/O register associated with the given End Effector
	 * type, or null if no such I/O register exists.
	 */
	public IORegister getIORegisterFor(EEType ee) {
		Integer regIdx = EE_TO_IOREG.get(ee);

		if (regIdx != null && regIdx >= 0 && regIdx < IOREG.length) {
			return IOREG[regIdx];
		}

		return null;
	}
	
	//returns the rotational values for each arm joint
	public float[] getJointAngles() {
		float[] rot = new float[6];
		for(int i = 0; i < SEGMENTS.size(); i += 1) {
			for(int j = 0; j < 3; j += 1) {
				if(SEGMENTS.get(i).rotations[j]) {
					rot[i] = SEGMENTS.get(i).currentRotations[j];
					break;
				}
			}
		}
		return rot;
	}

	/**
	 * Returns the start and endpoint of the range of angles, which
	 * 8 are valid for the joint of the Robot, corresponding to the
	 * given index. The range of valid angles spans from the x value
	 * of the returned PVector ot its y value, moving clockwise around
	 * the Unit Circle.
	 * 
	 * @param joint  An integer between 0 and 5 corresponding to the
	 *               of the Robot's joints: J1 - J6.
	 * @returning    A PVector, whose x and y values correspond to the
	 *               start and endpoint of the range of angles valid
	 *               for the joint corresponding to the given index.
	 */
	public PVector getJointRange(int joint) {
		joint = PApplet.abs(joint) % 6;
		Model seg = SEGMENTS.get(joint);

		for (int axes = 0; axes < 3; ++axes) {
			if (seg.rotations[axes]) {
				return seg.jointRanges[axes];
			}
		}
		// Should not be reachable
		return new PVector(0f, 0f, 0f);
	}

	public RMatrix getLastTipTMatrix() {
		return lastTipTMatrix;
	}

	public int getLiveSpeed() {
		return liveSpeed;
	}
	
	/**
	public RQuaternion getOrientation() {
		return RMath.matrixToQuat(getOrientationMatrix());
	}

	/* Calculate and returns a 3x3 matrix whose columns are the unit vectors of
	 * the end effector's current x, y, z axes with respect to the current frame.
	 *
	public RMatrix getOrientationMatrix() {
		RobotRun.getInstance().pushMatrix();
		RobotRun.getInstance().resetMatrix();
		RobotRun.applyModelRotation(this, getJointAngles());
		RMatrix matrix = RobotRun.getInstance().getOrientation();
		RobotRun.getInstance().popMatrix();

		return matrix;
	}
	
	/* Calculate and returns a 3x3 matrix whose columns are the unit vectors of
	 * the end effector's current x, y, z axes with respect to an arbitrary coordinate
	 * system specified by the rotation matrix 'frame.'
	 *
	public float[][] getOrientationMatrix(RMatrix frame) {
		RMatrix A = getOrientationMatrix();
		RMatrix B = new RMatrix(frame);
		RMatrix AB = A.multiply(B);

		return AB.getFloatData();
	}
	/**/
	
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
		if (pdx >= 0 && pdx < PROGRAMS.size()) {
			return PROGRAMS.get(pdx);
			
		} else {
			// Invalid index
			return null;
		}
	}

	/**
	 * TODO comment
	 * 
	 * @param name
	 * @return
	 */
	public Program getProgram(String name) {
		for (Program p : PROGRAMS) {
			if (p.getName().equals(name)) {
				return p;
			}
			
		}
		
		// No such program exists
		return null;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param jointAngles
	 * @return
	 */
	public RMatrix getRobotTransform(float[] jointAngles) {
		// Calculate the rotation matrices for the robot's joint angles
		RMatrix joint0Orien = RMath.tMatFromAxisAndAngle(new PVector(0f, 1f, 0f),
				jointAngles[0]);
		RMatrix joint1Orien = RMath.tMatFromAxisAndAngle(new PVector(1f, 0f, 0f),
				jointAngles[1]);
		RMatrix joint2Orien = RMath.tMatFromAxisAndAngle(new PVector(1f, 0f, 0f),
				jointAngles[2]);
		RMatrix joint3Orien = RMath.tMatFromAxisAndAngle(new PVector(0f, 1f, 0f),
				jointAngles[3]);
		RMatrix joint4Orien = RMath.tMatFromAxisAndAngle(new PVector(1f, 0f, 0f),
				jointAngles[4]);
		RMatrix joint5Orien = RMath.tMatFromAxisAndAngle(new PVector(0f, 0f, 1f),
				jointAngles[5]);
		
		// Apply all the orientations of the robot's segments and joints
		return new RMatrix(
				
				new float[][] {
					{1f, 0f, 0f, BASE_POSITION.x},
					{0f, 1f, 0f, BASE_POSITION.y},
					{0f, 0f, 1f, BASE_POSITION.z},
					{0f, 0f, 0f, 1f}
				}
				
			)
			.multiply(SEGMENT_TMATS.get(0))
			.multiply(joint0Orien)
			.multiply(SEGMENT_TMATS.get(1))
			.multiply(joint1Orien)
			.multiply(SEGMENT_TMATS.get(2))
			.multiply(joint2Orien)
			.multiply(SEGMENT_TMATS.get(3))
			.multiply(joint3Orien)
			.multiply(SEGMENT_TMATS.get(4))
			.multiply(joint4Orien)
			.multiply(SEGMENT_TMATS.get(5))
			.multiply(joint5Orien)
			.multiply(SEGMENT_TMATS.get(6));
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
		if (fdx >= 0 && fdx < TOOL_FRAMES.length) {
			return TOOL_FRAMES[fdx];
			
		} else {
			// Invalid index
			return null;
		}
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
		if (fdx >= 0 && fdx < USER_FRAMES.length) {
			return USER_FRAMES[fdx];
			
		} else {
			// Invalid index
			return null;
		}
	}
	
	/**
	 * Stops all movement of this robot.
	 */
	public void halt() {
		for (Model model : SEGMENTS) {
			model.jointsMoving[0] = 0;
			model.jointsMoving[1] = 0;
			model.jointsMoving[2] = 0;
		}

		for(int idx = 0; idx < jogLinear.length; ++idx) {
			jogLinear[idx] = 0;
		}

		for(int idx = 0; idx < jogRot.length; ++idx) {
			jogRot[idx] = 0;
		}
		
		motionType = RobotMotion.HALTED;
	}

	/**
	 * Indicates whether an issue occurred with inverse kinematics, when the
	 * robot is moving in a Cartesian reference frame.
	 * 
	 * @return	Whether the robot has a motion fault
	 */
	public boolean hasMotionFault() {
		return motionType == RobotMotion.MT_FAULT;
	}

	/**
	 * Updates the robot's joint angles, for the current target rotation, based on
	 * the given speed value.
	 * 
	 * @param speed	The speed of the robot's joint motion
	 * @return		If the robot has reached its target joint angles
	 */
	public boolean interpolateRotation(float speed) {
		boolean done = true;

		for(Model a : SEGMENTS) {
			for(int r = 0; r < 3; r++) {
				if(a.rotations[r]) {
					float distToDest = PApplet.abs(a.currentRotations[r] - a.targetRotations[r]);

					if (distToDest <= 0.0001f) {
						// Destination (basically) met
						continue;

					} else if (distToDest >= (a.rotationSpeed * speed)) {
						done = false;
						a.currentRotations[r] += a.rotationSpeed * a.rotationDirections[r] * speed;
						a.currentRotations[r] = RMath.mod2PI(a.currentRotations[r]);

					} else if (distToDest > 0.0001f) {
						// Destination too close to move at current speed
						a.currentRotations[r] = a.targetRotations[r];
						a.currentRotations[r] = RMath.mod2PI(a.currentRotations[r]);
					}
				}
			}
		}
		
		return done;
	}
	
	public boolean isTrace() {
		return trace;
	}

	/**
	 * @return	True if at least one joint of the Robot is in motion.
	 */
	public boolean jointMotion() {
		for(Model m : SEGMENTS) {
			// Check each segments active joint
			for(int idx = 0; idx < m.jointsMoving.length; ++idx) {
				if(m.jointsMoving[idx] != 0) {
					return true;
				}
			}
		}

		return false;
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
			if (!anglePermitted(joint, destAngles[joint])) {
				invalidAngle = true;
				
				PVector rangeBounds = getJointRange(joint);
				System.err.printf("Invalid angle: J[%d] = %4.3f -> %4.3f : [%4.3f - %4.3f]\n",
						joint, getJointAngles()[joint], destAngles[joint], rangeBounds.x,
						rangeBounds.y);
				break;
			}
		}

		// Did we successfully find the desired angles?
		if ((destAngles == null) || invalidAngle) {
			if (Fields.DEBUG) {
				Point RP = getToolTipNative();
				/* TODO REMOVE AFTER REFACTOR *
				Point RP = RobotRun.nativeRobotEEPoint(this, getJointAngles());
				/**/
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
	public boolean modelInMotion() {
		return RobotRun.getInstance().isProgramRunning() ||
				(motionType != RobotMotion.HALTED &&
				motionType != RobotMotion.MT_FAULT) || jointMotion() ||
				translationalMotion() || rotationalMotion();
	}

	/**
	 * Initializing rotational interpolation between this robot's current joint
	 * angles and the given set of joint angles.
	 */
	public void moveTo(float[] jointAngles) {
		
		if (!hasMotionFault()) {
			setupRotationInterpolation(jointAngles);
			motionType = RobotMotion.MT_JOINT;
		}
	}

	/**
	 * Initializes the linear interpolation between this robot end effector's
	 * current position and orientation and the given target position and
	 * orientation.
	 */
	public void moveTo(PVector position, RQuaternion orientation) {
		
		if (!hasMotionFault()) {
			Point start = getToolTipNative();
			/* TODO REMOVE AFTER REFACTOR *
			Point start = RobotRun.nativeRobotEEPoint(this, getJointAngles());
			/**/
			Point end = new Point(position.copy(), (RQuaternion)orientation.clone(), start.angles.clone());
			RobotRun.getInstance().beginNewLinearMotion(start, end);
			motionType = RobotMotion.MT_LINEAR;
		}
	}

	/**
	 * Returns the number of programs associated with the Robot.
	 */
	public int numOfPrograms() {
		return PROGRAMS.size();
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
			progList.add(new DisplayLine(i, 0, PROGRAMS.get(i).getName()));
		}
		
		return progList;
	}
			
	/**
	 * Pushes the active program onto the call stack and resets the active
	 * program and instruction indices.
	 * 
	 * @param r	The active robot
	 */
	public void pushActiveProg(RoboticArm r) {
		
		if (r.RID == RID && RobotRun.getInstance().isProgramRunning()) {
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
		if (held != null) {
			endEffectorState = Fields.OFF;
			updateIORegister();
			held = null;
		}
	}

	/**
	 * Reorders the program list of the robot, so that the programs are in
	 * alphabetical order.
	 */
	@SuppressWarnings("unchecked")
	public void reorderPrograms() {
		// Move programs to a temporary list
		ArrayList<Program> limboList = (ArrayList<Program>) PROGRAMS.clone();
		PROGRAMS.clear();
		
		for (int pdx = 0; pdx < limboList.size(); ++pdx) {
			int insertIdx = PROGRAMS.size() - 1;
			Program toInsert = limboList.get(pdx);
			
			if (PROGRAMS.size() > 0) {
				Program predecessor = PROGRAMS.get(insertIdx);
				/* Search to new list from back to front to find where to
				 * insert the program */
				while (predecessor.getName().compareTo(toInsert.getName()) > 0 ) {
					
					if (--insertIdx < 0) { break; }
					predecessor = PROGRAMS.get(insertIdx);
				}
			}
			
			PROGRAMS.add(insertIdx + 1, toInsert);
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
	 * Resets all the Robot Arm's bounding box colors to green.
	 */
	public void resetOBBColors() {
		for(BoundingBox b : ARM_OBBS) {
			b.setColor(Fields.OBB_DEFAULT);
		}

		ArrayList<BoundingBox> eeHB = EE_TO_OBBS.get(activeEndEffector);

		for(BoundingBox b : eeHB) {
			b.setColor(Fields.OBB_DEFAULT);
		}
	}
	
	/**
	 * Converts the given point, pt, from the Coordinate System defined by the
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
	 * @returning The point, pt, interms of the given coordinate system
	 */
	public Point removeFrame(Point pt, PVector origin, RQuaternion axes) {
		PVector position = RMath.vFromFrame(pt.position, origin, axes);
		RQuaternion orientation = RQuaternion.mult(pt.orientation, axes);

		// Update joint angles associated with the point
		float[] newJointAngles = RMath.inverseKinematics(this, pt.angles,
				position, orientation);

		if (newJointAngles != null) {
			return new Point(position, orientation, newJointAngles);
		} else {
			// If inverse kinematics fails use the old angles
			return new Point(position, orientation, pt.angles);
		}
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
		if (pdx >= 0 && pdx < PROGRAMS.size()) {
			Program removed = PROGRAMS.remove(pdx);
			setActiveProgIdx(-1);
			// Return the removed program
			return removed;
			
		} else {
			// Invalid index
			return null;
		}
	}

	/**
	 * @return	Whether the robot is rotating around at least one axis in the
	 * 			WORLD, TOOL, or USER frames.
	 */
	public boolean rotationalMotion() {
		return jogRot[0] != 0 || jogRot[1] != 0 || jogRot[2] != 0;
	}
	
	/**
	 * Sets the robot's active end effector and update the end effector state.
	 * 
	 * @param ee	A non-null end effector type
	 */
	public void setActiveEE(EEType ee) {
		if (ee != null) {
			activeEndEffector = ee;
			
			IORegister associatedIO = getIORegisterFor(activeEndEffector);
			// Update the end effector state
			if (associatedIO != null) {
				endEffectorState = associatedIO.state;
			} else {
				endEffectorState = Fields.OFF;
			}

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
	 * TODO  comment
	 * 
	 * @param active
	 * @return
	 */
	public boolean setActiveProg(Program active) {
		for (int idx = 0; idx < PROGRAMS.size(); ++idx) {
			if (PROGRAMS.get(idx) == active) {
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
		if (progIdx >= 0 && progIdx < PROGRAMS.size()) {
			
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
	 * Set the set of the robot's current end effector and update the part held
	 * by the robot.
	 * 
	 * @param state	The new state of the robot's end effector
	 */
	public void setEEState(int state) {
		
		if (state == Fields.ON || state == Fields.OFF) {
			endEffectorState = state;
			updateIORegister();
			checkPickupCollision(RobotRun.getInstance().getActiveScenario());
		}
	}
	
	/**
	 * Updates the robot's joint angles to the given set of joint angles.
	 * 
	 * @param rot	The robot's new set of joint angles
	 */
	public void setJointAngles(float[] rot) {
		for(int i = 0; i < SEGMENTS.size(); i += 1) {
			for(int j = 0; j < 3; j += 1) {
				if(SEGMENTS.get(i).rotations[j]) {
					SEGMENTS.get(i).currentRotations[j] = rot[i];
					SEGMENTS.get(i).currentRotations[j] %= PConstants.TWO_PI;
					if(SEGMENTS.get(i).currentRotations[j] < 0) {
						SEGMENTS.get(i).currentRotations[j] += PConstants.TWO_PI;
					}
				}
			}
		}
	}

	/**
	 * Updates the motion direction of the joint at the given joint index to
	 * the given direction value, if the index is valid.
	 * 
	 * @param joint	An index between 0 and 6, inclusive, which corresponds
	 * 				to one of the Robot's joints
	 * @param dir	The new direction of that joint's motion
	 * @return		The old direction, or 0 if the index is invalid
	 */
	public float setJointMotion(int joint, int dir) {
		if(joint >= 0 && joint < SEGMENTS.size()) {

			Model model = SEGMENTS.get(joint);
			// Checks all rotational axes
			for(int n = 0; n < 3; n++) {
				if(model.rotations[n]) {
					if(model.jointsMoving[n] == 0) {
						model.jointsMoving[n] = dir;
						return dir;
						
					} else {
						model.jointsMoving[n] = 0;
					}
				}
			}
		}

		return 0f;
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
		motionType = (flag) ? RobotMotion.MT_FAULT : RobotMotion.HALTED;
	}
	
	/**
	 * Sets the Model's target joint angles to the given set of angles and updates the
	 * rotation directions of each of the joint segments.
	 */
	public void setupRotationInterpolation(float[] tgtAngles) {
		// Set the Robot's target angles
		for(int n = 0; n < tgtAngles.length; n++) {
			for(int r = 0; r < 3; r++) {
				if(SEGMENTS.get(n).rotations[r]) {
					SEGMENTS.get(n).targetRotations[r] = tgtAngles[n];
				}
			}
		}

		// Calculate whether it's faster to turn CW or CCW
		for(int joint = 0; joint < 6; ++joint) {
			Model a = RobotRun.getActiveRobot().SEGMENTS.get(joint);

			for(int r = 0; r < 3; r++) {
				if(a.rotations[r]) {
					// The minimum distance between the current and target joint angles
					float dist_t = RMath.minimumDistance(a.currentRotations[r], a.targetRotations[r]);

					// check joint movement range
					if(a.jointRanges[r].x == 0 && a.jointRanges[r].y == PConstants.TWO_PI) {
						a.rotationDirections[r] = (dist_t < 0) ? -1 : 1;
					}
					else {  
						/* Determine if at least one bound lies within the range of the shortest angle
						 * between the current joint angle and the target angle. If so, then take the
						 * longer angle, otherwise choose the shortest angle path. */

						// The minimum distance from the current joint angle to the lower bound of the joint's range
						float dist_lb = RMath.minimumDistance(a.currentRotations[r], a.jointRanges[r].x);

						// The minimum distance from the current joint angle to the upper bound of the joint's range
						float dist_ub = RMath.minimumDistance(a.currentRotations[r], a.jointRanges[r].y);

						if(dist_t < 0) {
							if( (dist_lb < 0 && dist_lb > dist_t) || (dist_ub < 0 && dist_ub > dist_t) ) {
								// One or both bounds lie within the shortest path
								a.rotationDirections[r] = 1;
							} 
							else {
								a.rotationDirections[r] = -1;
							}
						} 
						else if(dist_t > 0) {
							if( (dist_lb > 0 && dist_lb < dist_t) || (dist_ub > 0 && dist_ub < dist_t) ) {  
								// One or both bounds lie within the shortest path
								a.rotationDirections[r] = -1;
							} 
							else {
								a.rotationDirections[r] = 1;
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Toggle the Robot's state between ON and OFF. Update the
	 * Robot's currently held world object as well.
	 */
	public void toggleEEState() {
		if (endEffectorState == Fields.ON) {
			setEEState( Fields.OFF );
			
		} else {
			setEEState( Fields.ON );
		}
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
	 * Returns true if the Robot is jogging translationally.
	 */
	public boolean translationalMotion() {
		return jogLinear[0] != 0 || jogLinear[1] != 0 || jogLinear[2] != 0;
	}

	/**
	 * Updates the position and orientation of the hit
	 * boxes related to the Robot Arm.
	 */
	private void updateCollisionOBBs(RobotRun app) {
		app.noFill();
		app.stroke(0, 255, 0);

		app.pushMatrix();
		app.resetMatrix();

		app.translate(BASE_POSITION.x, BASE_POSITION.y,
				BASE_POSITION.z);

		app.rotateZ(PConstants.PI);
		app.rotateY(PConstants.PI/2);
		app.translate(200, 50, 200);
		// Segment 0
		app.getCoordFromMatrix(ARM_OBBS[0]);

		app.translate(0, 100, 0);
		app.getCoordFromMatrix(ARM_OBBS[1]);

		app.translate(-200, -150, -200);

		app.rotateY(-PConstants.PI/2);
		app.rotateZ(-PConstants.PI);

		app.translate(-50, -166, -358);
		app.rotateZ(PConstants.PI);
		app.translate(150, 0, 150);
		app.rotateX(PConstants.PI);
		app.rotateY(SEGMENTS.get(0).currentRotations[1]);
		app.rotateX(-PConstants.PI);
		app.translate(10, 95, 0);
		app.rotateZ(-0.1f * PConstants.PI);
		// Segment 1
		app.getCoordFromMatrix(ARM_OBBS[2]);

		app.rotateZ(0.1f * PConstants.PI);
		app.translate(-160, -95, -150);
		app.rotateZ(-PConstants.PI);

		app.translate(-115, -85, 180);
		app.rotateZ(PConstants.PI);
		app.rotateY(PConstants.PI/2);
		app.translate(0, 62, 62);
		app.rotateX(SEGMENTS.get(1).currentRotations[2]);
		app.translate(30, 240, 0);
		// Segment 2
		app.getCoordFromMatrix(ARM_OBBS[3]);

		app.translate(-30, -302, -62);
		app.rotateY(-PConstants.PI/2);
		app.rotateZ(-PConstants.PI);

		app.translate(0, -500, -50);
		app.rotateZ(PConstants.PI);
		app.rotateY(PConstants.PI/2);
		app.translate(0, 75, 75);
		app.rotateZ(PConstants.PI);
		app.rotateX(SEGMENTS.get(2).currentRotations[2]);
		app.rotateZ(-PConstants.PI);
		app.translate(75, 0, 0);
		// Segment 3
		app.getCoordFromMatrix(ARM_OBBS[4]);

		app.translate(-75, -75, -75);
		app.rotateY(PConstants.PI/2);
		app.rotateZ(-PConstants.PI);

		app.translate(745, -150, 150);
		app.rotateZ(PConstants.PI/2);
		app.rotateY(PConstants.PI/2);
		app.translate(70, 0, 70);
		app.rotateY(SEGMENTS.get(3).currentRotations[0]);
		app.translate(5, 75, 5);
		// Segment 4;
		app.getCoordFromMatrix(ARM_OBBS[5]);
		

		app.translate(0, 295, 0);
		app.getCoordFromMatrix(ARM_OBBS[6]);

		app.translate(-75, -370, -75);

		app.rotateY(-PConstants.PI/2);
		app.rotateZ(-PConstants.PI/2);

		app.translate(-115, 130, -124);
		app.rotateZ(PConstants.PI);
		app.rotateY(-PConstants.PI/2);
		app.translate(0, 50, 50);
		app.rotateX(SEGMENTS.get(4).currentRotations[2]);
		app.translate(0, -50, -50);
		// Segment 5
		app.rotateY(PConstants.PI/2);
		app.rotateZ(-PConstants.PI);

		app.translate(150, -10, 95);
		app.rotateY(-PConstants.PI/2);
		app.rotateZ(PConstants.PI);
		app.translate(45, 45, 0);
		app.rotateZ(SEGMENTS.get(5).currentRotations[0]);
		app.rotateX(PConstants.PI);
		app.rotateY(PConstants.PI/2);
		
		// End Effector
		updateOBBBoxesForEE(activeEndEffector, app);
		
		app.popMatrix();
	}

	/**
	 * Update the I/O register associated with the Robot's current End Effector
	 * (if any) to the Robot's current End Effector state.
	 */
	public void updateIORegister() {
		// Get the I/O register associated with the current End Effector
		IORegister associatedIO = getIORegisterFor(activeEndEffector);

		if (associatedIO != null) {
			associatedIO.state = endEffectorState;
		}
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
			MotionInstruction mInst = (MotionInstruction) p.getInstAt(instIdx);
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
			MotionInstruction mInst = (MotionInstruction)p.getInstAt(instIdx);
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

	/**
	 * Updates position and orientation of the hit boxes associated
	 * with the given End Effector.
	 */
	private void updateOBBBoxesForEE(EEType current, RobotRun app) {
		ArrayList<BoundingBox> curEEOBBs = EE_TO_OBBS.get(current),
							   curPUEEOBBs = EE_TO_PICK_OBBS.get(current);

		RobotRun.getInstance().pushMatrix();
		
		switch(current) {
			case NONE:
				// Face Plate EE
				RobotRun.getInstance().translate(12, 0, 0);
				app.getCoordFromMatrix( curEEOBBs.get(0) );
				RobotRun.getInstance().translate(-12, 0, 0);
				break;
	
			case CLAW:
				// Claw Gripper EE
				RobotRun.getInstance().translate(3, 0, 0);
				app.getCoordFromMatrix( curEEOBBs.get(0) );
	
				RobotRun.getInstance().translate(-57, 0, -2);
				app.getCoordFromMatrix( curPUEEOBBs.get(0) );
	
				if (endEffectorState == Fields.OFF) {
					// When claw is open
					RobotRun.getInstance().translate(0, -27, 0);
					app.getCoordFromMatrix( curEEOBBs.get(1) );
					RobotRun.getInstance().translate(0, 54, 0);
					app.getCoordFromMatrix( curEEOBBs.get(2) );
					RobotRun.getInstance().translate(0, -27, 0);
	
				} else if (endEffectorState == Fields.ON) {
					// When claw is closed
					RobotRun.getInstance().translate(0, -10, 0);
					app.getCoordFromMatrix( curEEOBBs.get(1) );
					RobotRun.getInstance().translate(0, 20, 0);
					app.getCoordFromMatrix( curEEOBBs.get(2) );
					RobotRun.getInstance().translate(0, -10, 0);
				}
	
				RobotRun.getInstance().translate(54, 0, 2);
				break;
	
			case SUCTION:
				// Suction EE
				RobotRun.getInstance().translate(0, 0, 3);
				app.getCoordFromMatrix( curEEOBBs.get(0) );
	
				RobotRun.getInstance().translate(-67, 0, -2);
				BoundingBox limbo = curEEOBBs.get(1);
				app.getCoordFromMatrix( limbo );
	
				float dist = -43;
				RobotRun.getInstance().translate(dist, 0, 0);
				app.getCoordFromMatrix( curPUEEOBBs.get(0) );
				RobotRun.getInstance().translate(19 - dist, 50, 0);
				limbo = curEEOBBs.get(2);
				app.getCoordFromMatrix( limbo );
	
				dist = -33;
				RobotRun.getInstance().translate(0, -dist, 0);
				app.getCoordFromMatrix( curPUEEOBBs.get(1) );
				RobotRun.getInstance().translate(45, dist - 50, 2);
				break;
	
			case POINTER:
			case GLUE_GUN:
			case WIELDER:
			default:
		}

		RobotRun.getInstance().popMatrix();
	}
	
	/**
	 * Updates the reference to the Robot's last tool tip position and
	 * orientation, which is used to move the object held by the Robot.
	 */
	public void updateLastTipTMatrix() {
		lastTipTMatrix = getRobotTransform(getJointAngles());
		/* REMOVE AFTER REFACTOR *
		RobotRun app = RobotRun.getInstance();
		
		app.pushMatrix();
		app.resetMatrix();
		RobotRun.applyModelRotation(this, getJointAngles());
		// Keep track of the old coordinate frame of the armModel
		lastEEOrientation = app.getTransformationMatrix();
		app.popMatrix();
		/**/
	}
	
	public void updateRobot(RobotRun app) {
		
		if (!hasMotionFault()) {
			// Execute arm movement
			if(app.isProgramRunning()) {
				// Run active program
				app.setProgramRunning(!app.executeProgram(this,
						app.execSingleInst));

			} else if (motionType != RobotMotion.HALTED) {
				// Move the Robot progressively to a point
				boolean doneMoving = true;

				switch (motionType) {
				case MT_JOINT:
					doneMoving = interpolateRotation(liveSpeed / 100.0f);
					break;
				case MT_LINEAR:
					doneMoving = app.executeMotion(this, liveSpeed / 100.0f);
					break;
				default:
					break;
				}

				if (doneMoving) {
					app.hold();
				}

			} else if (modelInMotion()) {
				// Jog the Robot
				app.intermediatePositions.clear();
				executeLiveMotion();
			}
		}

		updateCollisionOBBs(app);
	}
}

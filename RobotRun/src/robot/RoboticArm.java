package robot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import frame.CoordFrame;
import frame.Frame;
import frame.ToolFrame;
import frame.UserFrame;
import geom.BoundingBox;
import geom.Part;
import geom.Point;
import geom.RMath;
import geom.RMatrix;
import geom.RQuaternion;
import geom.WorldObject;
import global.Fields;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;
import processing.core.PVector;
import programming.Instruction;
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
	private final Frame[] toolFrames, userFrames;
	
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
	 * Indicates whether an issue occurred with inverse kinematics, when the
	 * robot is moving in a Cartesian reference frame.
	 */
	private boolean motionFault;
	
	/**
	 * The current coordinate frame of the robot.
	 */
	private CoordFrame curCoordFrame;
	
	/**
	 * An index corresponding to the active frame for this robot.
	 */
	private int activeUserFrame, activeToolFrame;
	
	
	private float[][] lastEEOrientation;

	/**
	 * Creates a robot with the given ID at the given position with the given
	 * segment and end effector models.
	 * 
	 * @param rid			The unique identifier associated with the Robot
	 * @param basePos		The center position of the Robot's base segment
	 * @param robotModels	The set of both the segment and end effector models
	 * 						for this robot
	 */
	public RoboticArm(int rid, PVector basePos, PShape[] robotModels) {
		int idx;
		
		jogLinear = new float[3];
		jogRot = new float[3];
		
		motorSpeed = 1000.0f; // speed in mm/sec
		liveSpeed = 10;
		
		RID = rid;
		SEGMENTS = new ArrayList<>();
		BASE_POSITION = basePos;
		
		// Initialize program fields
		PROGRAMS = new ArrayList<>();
		CALL_STACK = new Stack<>();
		PROG_UNDO = new Stack<>();
		
		activeProgIdx = -1;
		activeInstIdx = -1;
		
		// Initializes the frames
		
		toolFrames = new Frame[Fields.FRAME_NUM];
		userFrames = new Frame[Fields.FRAME_NUM];
		
		for (idx = 0; idx < toolFrames.length; ++idx) {
			toolFrames[idx] = new ToolFrame();
			userFrames[idx] = new UserFrame();
		}
		
		curCoordFrame = CoordFrame.JOINT;
		activeUserFrame = -1;
		activeToolFrame = -1;
		
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
		motionFault = false;
		
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
		limbo.get(0).setColor(RobotRun.getInstance().color(0, 0, 255));
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
		limbo.get(0).setColor(RobotRun.getInstance().color(0, 0, 255));
		limbo.add(new BoundingBox(25, 3, 25) );
		limbo.get(1).setColor(RobotRun.getInstance().color(0, 0, 255));
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
		// Initializes the old transformation matrix for the arm model
		RobotRun.getInstance().pushMatrix();
		RobotRun.applyModelRotation(this, getJointAngles());
		lastEEOrientation = RobotRun.getInstance().getTransformationMatrix();
		RobotRun.getInstance().popMatrix();
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

		if (!app.isShift() || motionFault) {
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
		
		if (!app.isShift() || motionFault) {
			// Only move when shift is set and there is no error
			return 0f;
		}

		// Initiaize the Robot's destination
		Point RP = RobotRun.nativeRobotEEPoint(this, getJointAngles());
		tgtPosition = RP.position;
		tgtOrientation = RP.orientation;


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
				b.setColor(RobotRun.getInstance().color(255, 0, 0));
				collision = true;
			}
		}

		ArrayList<BoundingBox> eeHBs = EE_TO_OBBS.get(activeEndEffector);

		for(BoundingBox b : eeHBs) {
			if(obj.collision(b)) {
				b.setColor(RobotRun.getInstance().color(255, 0, 0));
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
				ARM_OBBS[ check_pairs[idx] ].setColor(RobotRun.getInstance().color(255, 0, 0));
				ARM_OBBS[ check_pairs[idx + 1] ].setColor(RobotRun.getInstance().color(255, 0, 0));
				collision = true;
			}
		}

		ArrayList<BoundingBox> eeHB = EE_TO_OBBS.get(activeEndEffector);

		// Check collisions between all EE hit boxes and base as well as the first long arm hit boxes
		for(BoundingBox hb : eeHB) {
			for(int idx = 0; idx < 4; ++idx) {
				if(Part.collision3D(hb, ARM_OBBS[idx]) ) {
					hb.setColor(RobotRun.getInstance().color(255, 0, 0));
					ARM_OBBS[idx].setColor(RobotRun.getInstance().color(255, 0, 0));
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
	 * Transitions from the current End Effector
	 * to the next End Effector in a cyclic pattern:
	 * 
	 * NONE -> SUCTION -> CLAW -> POINTER -> GLUE_GUN -> WIELDER -> NONE
	 */
	public void cycleEndEffector() {
		// Switch to the next End Effector in the cycle
		switch (activeEndEffector) {
		case NONE:
			activeEndEffector = EEType.SUCTION;
			break;

		case SUCTION:
			activeEndEffector = EEType.CLAW;
			break;

		case CLAW:
			activeEndEffector = EEType.POINTER;
			break;

		case POINTER:
			activeEndEffector = EEType.GLUE_GUN;
			break;

		case GLUE_GUN:
			activeEndEffector = EEType.WIELDER;
			break;

		case WIELDER:
		default:
			activeEndEffector = EEType.NONE;
			break;
		}

		IORegister associatedIO = getIORegisterFor(activeEndEffector);
		// Update the end effector state
		if (associatedIO != null) {
			endEffectorState = associatedIO.state;
		} else {
			endEffectorState = Fields.OFF;
		}

		releaseHeldObject();
	}
	
	/**
	 * Draws the robot arm model, with the center of its base at the robot's
	 * base position field.
	 */
	public void draw() {
		RobotRun.getInstance().noStroke();
		RobotRun.getInstance().fill(200, 200, 0);

		RobotRun.getInstance().pushMatrix();
		RobotRun.getInstance().translate(BASE_POSITION.x, BASE_POSITION.y,
						BASE_POSITION.z);

		RobotRun.getInstance().rotateZ(PConstants.PI);
		RobotRun.getInstance().rotateY(PConstants.PI/2);
		SEGMENTS.get(0).draw();
		RobotRun.getInstance().rotateY(-PConstants.PI/2);
		RobotRun.getInstance().rotateZ(-PConstants.PI);

		RobotRun.getInstance().fill(50);

		RobotRun.getInstance().translate(-50, -166, -358); // -115, -213, -413
		RobotRun.getInstance().rotateZ(PConstants.PI);
		RobotRun.getInstance().translate(150, 0, 150);
		RobotRun.getInstance().rotateX(PConstants.PI);
		RobotRun.getInstance().rotateY(SEGMENTS.get(0).currentRotations[1]);
		RobotRun.getInstance().rotateX(-PConstants.PI);
		RobotRun.getInstance().translate(-150, 0, -150);
		SEGMENTS.get(1).draw();
		RobotRun.getInstance().rotateZ(-PConstants.PI);

		RobotRun.getInstance().fill(200, 200, 0);

		RobotRun.getInstance().translate(-115, -85, 180);
		RobotRun.getInstance().rotateZ(PConstants.PI);
		RobotRun.getInstance().rotateY(PConstants.PI/2);
		RobotRun.getInstance().translate(0, 62, 62);
		RobotRun.getInstance().rotateX(SEGMENTS.get(1).currentRotations[2]);
		RobotRun.getInstance().translate(0, -62, -62);
		SEGMENTS.get(2).draw();
		RobotRun.getInstance().rotateY(-PConstants.PI/2);
		RobotRun.getInstance().rotateZ(-PConstants.PI);

		RobotRun.getInstance().fill(50);

		RobotRun.getInstance().translate(0, -500, -50);
		RobotRun.getInstance().rotateZ(PConstants.PI);
		RobotRun.getInstance().rotateY(PConstants.PI/2);
		RobotRun.getInstance().translate(0, 75, 75);
		RobotRun.getInstance().rotateZ(PConstants.PI);
		RobotRun.getInstance().rotateX(SEGMENTS.get(2).currentRotations[2]);
		RobotRun.getInstance().rotateZ(-PConstants.PI);
		RobotRun.getInstance().translate(0, -75, -75);
		SEGMENTS.get(3).draw();
		RobotRun.getInstance().rotateY(PConstants.PI/2);
		RobotRun.getInstance().rotateZ(-PConstants.PI);

		RobotRun.getInstance().translate(745, -150, 150);
		RobotRun.getInstance().rotateZ(PConstants.PI/2);
		RobotRun.getInstance().rotateY(PConstants.PI/2);
		RobotRun.getInstance().translate(70, 0, 70);
		RobotRun.getInstance().rotateY(SEGMENTS.get(3).currentRotations[0]);
		RobotRun.getInstance().translate(-70, 0, -70);
		SEGMENTS.get(4).draw();
		RobotRun.getInstance().rotateY(-PConstants.PI/2);
		RobotRun.getInstance().rotateZ(-PConstants.PI/2);

		RobotRun.getInstance().fill(200, 200, 0);

		RobotRun.getInstance().translate(-115, 130, -124);
		RobotRun.getInstance().rotateZ(PConstants.PI);
		RobotRun.getInstance().rotateY(-PConstants.PI/2);
		RobotRun.getInstance().translate(0, 50, 50);
		RobotRun.getInstance().rotateX(SEGMENTS.get(4).currentRotations[2]);
		RobotRun.getInstance().translate(0, -50, -50);
		SEGMENTS.get(5).draw();
		RobotRun.getInstance().rotateY(PConstants.PI/2);
		RobotRun.getInstance().rotateZ(-PConstants.PI);

		RobotRun.getInstance().fill(50);

		RobotRun.getInstance().translate(150, -10, 95);
		RobotRun.getInstance().rotateY(-PConstants.PI/2);
		RobotRun.getInstance().rotateZ(PConstants.PI);
		RobotRun.getInstance().translate(45, 45, 0);
		RobotRun.getInstance().rotateZ(SEGMENTS.get(5).currentRotations[0]);
		RobotRun.getInstance().translate(-45, -45, 0);
		SEGMENTS.get(6).draw();

		drawEndEffector(activeEndEffector, endEffectorState);

		RobotRun.getInstance().popMatrix();
		/* My sketchy work-around for drawing only the bounding boxes of the
		 * active robot */
		if (RobotRun.getActiveRobot() == this &&
				RobotRun.getInstance().areOBBsDisplayed()) {
			
			drawBoxes();
		}
	}
	
	/* Draws the Robot Arm's hit boxes in the world */
	private void drawBoxes() {
		// Draw hit boxes of the body poriotn of the Robot Arm
		for(BoundingBox b : ARM_OBBS) {
			b.draw();
		}

		ArrayList<BoundingBox> curEEHitBoxes = EE_TO_OBBS.get(activeEndEffector);

		// Draw End Effector hit boxes
		for(BoundingBox b : curEEHitBoxes) {
			b.draw();
		}

		curEEHitBoxes = EE_TO_PICK_OBBS.get(activeEndEffector);
		// Draw Pickup hit boxes
		for (BoundingBox b : curEEHitBoxes) {
			b.draw();
		}
	}
	
	/**
	 * Draw the End Effector model associated with the given
	 * End Effector type in the current coordinate system.
	 * 
	 * @param ee       The End Effector to draw
	 * @param eeState  The state of the End Effector to be drawn
	 */
	private void drawEndEffector(EEType ee, int eeState) {
		RobotRun.getInstance().pushMatrix();

		// Center the End Effector on the Robot's faceplate and draw it.
		if(ee == EEType.SUCTION) {
			RobotRun.getInstance().rotateY(PConstants.PI);
			RobotRun.getInstance().translate(-88, -37, 0);
			EEM_SUCTION.draw();

		} else if(ee == EEType.CLAW) {
			RobotRun.getInstance().rotateY(PConstants.PI);
			RobotRun.getInstance().translate(-88, 0, 0);
			EEM_CLAW.draw();
			RobotRun.getInstance().rotateZ(PConstants.PI/2);

			if(eeState == Fields.OFF) {
				// Draw open grippers
				RobotRun.getInstance().translate(10, -85, 30);
				EEM_CLAW_PINCER.draw();
				RobotRun.getInstance().translate(55, 0, 0);
				EEM_CLAW_PINCER.draw();

			} else if(eeState == Fields.ON) {
				// Draw closed grippers
				RobotRun.getInstance().translate(28, -85, 30);
				EEM_CLAW_PINCER.draw();
				RobotRun.getInstance().translate(20, 0, 0);
				EEM_CLAW_PINCER.draw();
			}
		} else if (ee == EEType.POINTER) {
			RobotRun.getInstance().rotateY(PConstants.PI);
			RobotRun.getInstance().rotateZ(PConstants.PI);
			RobotRun.getInstance().translate(45, -45, 10);
			EEM_POINTER.draw();

		} else if (ee == EEType.GLUE_GUN) {
			RobotRun.getInstance().rotateZ(PConstants.PI);
			RobotRun.getInstance().translate(-48, -46, -12);
			EEM_GLUE_GUN.draw();

		} else if (ee == EEType.WIELDER) {
			RobotRun.getInstance().rotateY(PConstants.PI);
			RobotRun.getInstance().rotateZ(PConstants.PI);
			RobotRun.getInstance().translate(46, -44, 10);
			EEM_WIELDER.draw();
		}

		RobotRun.getInstance().popMatrix();
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
							System.out.printf("A[i%d, n=%d]: %f\n", i, n, trialAngle);
							model.jointsMoving[n] = 0;
							RobotRun.getInstance().updateRobotJogMotion(i, 0);
							RobotRun.getInstance().hold();
						}
					}
				}
			}

		} else {
			// Jog in the World, Tool or User Frame
			RQuaternion invFrameOrientation = null;

			if (curCoordFrame == CoordFrame.TOOL) {
				Frame curFrame = getActiveFrame(CoordFrame.TOOL);

				if (curFrame != null) {
					invFrameOrientation = curFrame.getOrientation().conjugate();
				}
			} else if (curCoordFrame == CoordFrame.USER) {
				Frame curFrame = getActiveFrame(CoordFrame.USER);

				if (curFrame != null) {
					invFrameOrientation = curFrame.getOrientation().conjugate();
				}
			}

			Point curPoint = RobotRun.nativeRobotEEPoint(this, getJointAngles());

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
	 * Returns the active Tool frame TOOL, or the active User frame for USER. For either
	 * CoordFrame WORLD or JOINT null is always returned. If null is given as a parameter,
	 * then the active Coordinate Frame System is checked.
	 * 
	 * @param coord  The Coordinate Frame System to check for an active frame,
	 *               or null to check the current active Frame System.
	 */
	public Frame getActiveFrame(CoordFrame coord) {
		if (coord == null) {
			// Use current coordinate Frame
			coord = curCoordFrame;
		}

		// Determine if a frame is active in the given Coordinate Frame
		if (coord == CoordFrame.USER && activeUserFrame >= 0 &&
				activeUserFrame < userFrames.length) {
			// active User frame
			return userFrames[activeUserFrame];
			
		} else if (coord == CoordFrame.TOOL && activeToolFrame >= 0 &&
				activeToolFrame < toolFrames.length) {
			// active Tool frame
			return toolFrames[activeToolFrame];
			
		} else {
			// no active frame
			return null;
		}
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
	 * @return	The ID for the Robot's active tool frame
	 */
	public int getActiveToolFrame() {
		return activeToolFrame;
	}
	
	/**
	 * @return	The ID for the Robot's active user frame
	 */
	public int getActiveUserFrame() {
		return activeUserFrame;
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
			
			pushInstState(idx, inst.clone());
			
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

	public int getLiveSpeed() {
		return liveSpeed;
	}
	
	public float[][] getLastEEOrientation() {
		return lastEEOrientation;
	}
	
	public RQuaternion getOrientation() {
		return RMath.matrixToQuat(getOrientationMatrix());
	}
	
	/* Calculate and returns a 3x3 matrix whose columns are the unit vectors of
	 * the end effector's current x, y, z axes with respect to the current frame.
	 */
	public float[][] getOrientationMatrix() {
		RobotRun.getInstance().pushMatrix();
		RobotRun.getInstance().resetMatrix();
		RobotRun.applyModelRotation(this, getJointAngles());
		float[][] matrix = RobotRun.getInstance().getRotationMatrix();
		RobotRun.getInstance().popMatrix();

		return matrix;
	}

	/* Calculate and returns a 3x3 matrix whose columns are the unit vectors of
	 * the end effector's current x, y, z axes with respect to an arbitrary coordinate
	 * system specified by the rotation matrix 'frame.'
	 */
	public float[][] getOrientationMatrix(float[][] frame) {
		float[][] m = getOrientationMatrix();
		RMatrix A = new RMatrix(m);
		RMatrix B = new RMatrix(frame);
		RMatrix AB = (RMatrix)A.multiply(B.transpose());

		return AB.getFloatData();
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
	 * Returns the tool frame, associated with the given index, of the Robot,
	 * or null if the given index is invalid. A Robot has a total of 10 tool
	 * frames, which are zero-indexed.
	 * 
	 * @param fdx	A integer value between 0 and 9, inclusive
	 * @return		The tool frame associated with the given index, or null if
	 * 				the given index is invalid.
	 */
	public Frame getToolFrame(int fdx) {
		if (fdx >= 0 && fdx < toolFrames.length) {
			return toolFrames[fdx];
			
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
	public Frame getUserFrame(int fdx) {
		if (fdx >= 0 && fdx < userFrames.length) {
			return userFrames[fdx];
			
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

	public boolean hasMotionFault() { return motionFault; }

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
				
				if (Fields.DEBUG) {
					PVector rangeBounds = getJointRange(joint);
					System.err.printf("Invalid angle: J[%d] = %4.3f -> %4.3f : [%4.3f - %4.3f]\n", joint, getJointAngles()[joint],
							destAngles[joint], rangeBounds.x, rangeBounds.y);
				}
				
				break;
			}
		}

		// Did we successfully find the desired angles?
		if ((destAngles == null) || invalidAngle) {
			if (Fields.DEBUG) {
				Point RP = RobotRun.nativeRobotEEPoint(this, getJointAngles());
				System.out.printf("IK Failure ...\n%s -> %s\n%s -> %s\n\n", RP.position, destPosition,
						RP.orientation, destOrientation);
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
		return RobotRun.getInstance().isProgramRunning() || motionType != RobotMotion.HALTED ||
				jointMotion() || translationalMotion() || rotationalMotion();
	}

	/**
	 * Initializing rotational interpolation between this robot's current joint
	 * angles and the given set of joint angles.
	 */
	public void moveTo(float[] jointAngles) {
		setupRotationInterpolation(jointAngles);
		motionType = RobotMotion.MT_JOINT;
	}

	/**
	 * Initializes the linear interpolation between this robot end effector's
	 * current position and orientation and the given target position and
	 * orientation.
	 */
	public void moveTo(PVector position, RQuaternion orientation) {
		Point start = RobotRun.nativeRobotEEPoint(this, getJointAngles());
		Point end = new Point(position.copy(), (RQuaternion)orientation.clone(), start.angles.clone());
		RobotRun.getInstance().beginNewLinearMotion(start, end);
		motionType = RobotMotion.MT_LINEAR;
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
				p.replaceInstAt(state.originIdx, state.inst);
			}
			
		} else if (Fields.DEBUG) {
			System.out.println("Empty program undo stack!");
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
	private void pushInstState(int idx, Instruction inst) {
		
		if (PROG_UNDO.size() > 35) {
			PROG_UNDO.remove(0);
		}
		
		PROG_UNDO.push(new InstState(idx, inst));
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
	 * Removes the program, associated with the given index, from the Robot.
	 * 
	 * @param pdx	A positive integer value less than the number of programs
	 * 				the Robot possesses
	 * @return		The program that was removed, or null if the index given
	 * 				is invalid
	 */
	public Program removeProgram(int pdx) {
		if (pdx >= 0 && pdx < PROGRAMS.size()) {
			// Return the removed program
			return PROGRAMS.remove(pdx);
			
		} else {
			// Invalid index
			return null;
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
			
			pushInstState(idx, replaced.clone());
			
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
			b.setColor(RobotRun.getInstance().color(0, 255, 0));
		}

		ArrayList<BoundingBox> eeHB = EE_TO_OBBS.get(activeEndEffector);

		for(BoundingBox b : eeHB) {
			b.setColor(RobotRun.getInstance().color(0, 255, 0));
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
	 * Sets this robot's active tool frame index to the given value.
	 * 
	 * @param activeToolFrame	The robot's new active tool frame index
	 */
	public void setActiveToolFrame(int activeToolFrame) {
		this.activeToolFrame = activeToolFrame;
	}

	/**
	 * Sets this robot's active user frame index to the given value.
	 * 
	 * @param activeToolFrame	The robot's new active user frame index
	 */
	public void setActiveUserFrame(int activeUserFrame) {
		this.activeUserFrame = activeUserFrame;
	}

	/**
	 * Update the Robot's current coordinate frame.
	 * @param newFrame	The new coordinate frame
	 */
	public void setCurCoordFrame(CoordFrame newFrame) {
		curCoordFrame = newFrame;
	}
	
	/**
	 * Sets the Robot's default position field. This method should ONLY be
	 * called after the RobotRun's activeRobot field is set, since
	 * nativeRobotPoint relies on that method.
	 */
	protected void setDefaultRobotPoint() {
		// Define the default Robot position and orientation
		robotPoint = RobotRun.nativeRobotPoint(this,
				new float[] { 0f, 0f, 0f, 0f, 0f, 0f });
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
	
	public void setMotionFault(boolean flag) {
		motionFault = flag;
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
	private void updateCollisionOBBs() { 
		RobotRun.getInstance().noFill();
		RobotRun.getInstance().stroke(0, 255, 0);

		RobotRun.getInstance().pushMatrix();
		RobotRun.getInstance().resetMatrix();

		RobotRun.getInstance().translate(BASE_POSITION.x, BASE_POSITION.y,
				BASE_POSITION.z);

		RobotRun.getInstance().rotateZ(PConstants.PI);
		RobotRun.getInstance().rotateY(PConstants.PI/2);
		RobotRun.getInstance().translate(200, 50, 200);
		// Segment 0
		ARM_OBBS[0].setCoordinateSystem();

		RobotRun.getInstance().translate(0, 100, 0);
		ARM_OBBS[1].setCoordinateSystem();

		RobotRun.getInstance().translate(-200, -150, -200);

		RobotRun.getInstance().rotateY(-PConstants.PI/2);
		RobotRun.getInstance().rotateZ(-PConstants.PI);

		RobotRun.getInstance().translate(-50, -166, -358);
		RobotRun.getInstance().rotateZ(PConstants.PI);
		RobotRun.getInstance().translate(150, 0, 150);
		RobotRun.getInstance().rotateX(PConstants.PI);
		RobotRun.getInstance().rotateY(SEGMENTS.get(0).currentRotations[1]);
		RobotRun.getInstance().rotateX(-PConstants.PI);
		RobotRun.getInstance().translate(10, 95, 0);
		RobotRun.getInstance().rotateZ(-0.1f * PConstants.PI);
		// Segment 1
		ARM_OBBS[2].setCoordinateSystem();

		RobotRun.getInstance().rotateZ(0.1f * PConstants.PI);
		RobotRun.getInstance().translate(-160, -95, -150);
		RobotRun.getInstance().rotateZ(-PConstants.PI);

		RobotRun.getInstance().translate(-115, -85, 180);
		RobotRun.getInstance().rotateZ(PConstants.PI);
		RobotRun.getInstance().rotateY(PConstants.PI/2);
		RobotRun.getInstance().translate(0, 62, 62);
		RobotRun.getInstance().rotateX(SEGMENTS.get(1).currentRotations[2]);
		RobotRun.getInstance().translate(30, 240, 0);
		// Segment 2
		ARM_OBBS[3].setCoordinateSystem();

		RobotRun.getInstance().translate(-30, -302, -62);
		RobotRun.getInstance().rotateY(-PConstants.PI/2);
		RobotRun.getInstance().rotateZ(-PConstants.PI);

		RobotRun.getInstance().translate(0, -500, -50);
		RobotRun.getInstance().rotateZ(PConstants.PI);
		RobotRun.getInstance().rotateY(PConstants.PI/2);
		RobotRun.getInstance().translate(0, 75, 75);
		RobotRun.getInstance().rotateZ(PConstants.PI);
		RobotRun.getInstance().rotateX(SEGMENTS.get(2).currentRotations[2]);
		RobotRun.getInstance().rotateZ(-PConstants.PI);
		RobotRun.getInstance().translate(75, 0, 0);
		// Segment 3
		ARM_OBBS[4].setCoordinateSystem();

		RobotRun.getInstance().translate(-75, -75, -75);
		RobotRun.getInstance().rotateY(PConstants.PI/2);
		RobotRun.getInstance().rotateZ(-PConstants.PI);

		RobotRun.getInstance().translate(745, -150, 150);
		RobotRun.getInstance().rotateZ(PConstants.PI/2);
		RobotRun.getInstance().rotateY(PConstants.PI/2);
		RobotRun.getInstance().translate(70, 0, 70);
		RobotRun.getInstance().rotateY(SEGMENTS.get(3).currentRotations[0]);
		RobotRun.getInstance().translate(5, 75, 5);
		// Segment 4
		ARM_OBBS[5].setCoordinateSystem();

		RobotRun.getInstance().translate(0, 295, 0);
		ARM_OBBS[6].setCoordinateSystem();

		RobotRun.getInstance().translate(-75, -370, -75);

		RobotRun.getInstance().rotateY(-PConstants.PI/2);
		RobotRun.getInstance().rotateZ(-PConstants.PI/2);

		RobotRun.getInstance().translate(-115, 130, -124);
		RobotRun.getInstance().rotateZ(PConstants.PI);
		RobotRun.getInstance().rotateY(-PConstants.PI/2);
		RobotRun.getInstance().translate(0, 50, 50);
		RobotRun.getInstance().rotateX(SEGMENTS.get(4).currentRotations[2]);
		RobotRun.getInstance().translate(0, -50, -50);
		// Segment 5
		RobotRun.getInstance().rotateY(PConstants.PI/2);
		RobotRun.getInstance().rotateZ(-PConstants.PI);

		RobotRun.getInstance().translate(150, -10, 95);
		RobotRun.getInstance().rotateY(-PConstants.PI/2);
		RobotRun.getInstance().rotateZ(PConstants.PI);
		RobotRun.getInstance().translate(45, 45, 0);
		RobotRun.getInstance().rotateZ(SEGMENTS.get(5).currentRotations[0]);
		RobotRun.getInstance().translate(-45, -45, 0);
		RobotRun.getInstance().popMatrix();

		// End Effector
		updateOBBBoxesForEE(activeEndEffector);
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
	 * Updates position and orientation of the hit boxes associated
	 * with the given End Effector.
	 */
	private void updateOBBBoxesForEE(EEType current) {
		ArrayList<BoundingBox> curEEOBBs = EE_TO_OBBS.get(current),
							   curPUEEOBBs = EE_TO_PICK_OBBS.get(current);

		RobotRun.getInstance().pushMatrix();
		RobotRun.getInstance().resetMatrix();
		RobotRun.applyModelRotation(this, getJointAngles());
		
		switch(current) {
			case NONE:
				// Face Plate EE
				RobotRun.getInstance().translate(12, 0, 0);
				curEEOBBs.get(0).setCoordinateSystem();
				RobotRun.getInstance().translate(-12, 0, 0);
				break;
	
			case CLAW:
				// Claw Gripper EE
				RobotRun.getInstance().translate(3, 0, 0);
				curEEOBBs.get(0).setCoordinateSystem();
	
				RobotRun.getInstance().translate(-57, 0, -2);
				curPUEEOBBs.get(0).setCoordinateSystem();
	
				if (endEffectorState == Fields.OFF) {
					// When claw is open
					RobotRun.getInstance().translate(0, -27, 0);
					curEEOBBs.get(1).setCoordinateSystem();
					RobotRun.getInstance().translate(0, 54, 0);
					curEEOBBs.get(2).setCoordinateSystem();
					RobotRun.getInstance().translate(0, -27, 0);
	
				} else if (endEffectorState == Fields.ON) {
					// When claw is closed
					RobotRun.getInstance().translate(0, -10, 0);
					curEEOBBs.get(1).setCoordinateSystem();
					RobotRun.getInstance().translate(0, 20, 0);
					curEEOBBs.get(2).setCoordinateSystem();
					RobotRun.getInstance().translate(0, -10, 0);
				}
	
				RobotRun.getInstance().translate(54, 0, 2);
				break;
	
			case SUCTION:
				// Suction EE
				RobotRun.getInstance().translate(0, 0, 3);
				curEEOBBs.get(0).setCoordinateSystem();
	
				RobotRun.getInstance().translate(-67, 0, -2);
				BoundingBox limbo = curEEOBBs.get(1);
				limbo.setCoordinateSystem();
	
				float dist = -43;
				RobotRun.getInstance().translate(dist, 0, 0);
				curPUEEOBBs.get(0).setCoordinateSystem();
				RobotRun.getInstance().translate(19 - dist, 50, 0);
				limbo = curEEOBBs.get(2);
				limbo.setCoordinateSystem();
	
				dist = -33;
				RobotRun.getInstance().translate(0, -dist, 0);
				curPUEEOBBs.get(1).setCoordinateSystem();
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
	 * Updates the reference to the Robot's previous
	 * End Effector orientation, which is used to move
	 * the object held by the Robot.
	 */
	public void updatePreviousEEOrientation() {
		RobotRun app = RobotRun.getInstance();
		
		app.pushMatrix();
		app.resetMatrix();
		RobotRun.applyModelRotation(this, getJointAngles());
		// Keep track of the old coordinate frame of the armModel
		lastEEOrientation = app.getTransformationMatrix();
		app.popMatrix();
	}
	
	/**
	 * Sets the Robot's default position and orientation in a static variable.
	 * THIS METHOD MUST BE CALLED WHEN THE FIRST ROBOT IS CREATED!
	 */
	public void updateRobot() {
		if (!this.hasMotionFault()) {
			// Execute arm movement
			if(RobotRun.getInstance().isProgramRunning()) {
				// Run active program
				RobotRun.getInstance().setProgramRunning(!RobotRun.getInstance().executeProgram(this,
						RobotRun.getInstance().execSingleInst));

			} else if (motionType != RobotMotion.HALTED) {
				// Move the Robot progressively to a point
				boolean doneMoving = true;

				switch (RobotRun.getActiveRobot().motionType) {
				case MT_JOINT:
					doneMoving = interpolateRotation(liveSpeed / 100.0f);
					break;
				case MT_LINEAR:
					doneMoving = RobotRun.getInstance().executeMotion(this, liveSpeed / 100.0f);
					break;
				default:
				}

				if (doneMoving) {
					RobotRun.getInstance().hold();
				}

			} else if (modelInMotion()) {
				// Jog the Robot
				RobotRun.getInstance().intermediatePositions.clear();
				executeLiveMotion();
			}
		}

		updateCollisionOBBs();
	}
}

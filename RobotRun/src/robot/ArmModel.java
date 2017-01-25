package robot;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import frame.CoordFrame;
import frame.Frame;
import frame.ToolFrame;
import frame.UserFrame;
import geom.Part;
import geom.Point;
import geom.WorldObject;
import global.Fields;
import processing.core.PVector;
import programming.Instruction;
import programming.Program;
import regs.DataRegister;
import regs.IORegister;
import regs.PositionRegister;

public class ArmModel {
	// The position of the center of the Robot's base segment
	private final PVector BASE_POSITION;
	// Initial position and orientation of the Robot
	private Point robotPoint;
	
	/* The number of the user and tool frames, the number of the position and
	 * data registers, and the number of I/O registers */
	public static final int FRAME_NUM, DPREG_NUM, IOREG_NUM;
	
	static {
		FRAME_NUM = 10;
		DPREG_NUM = 100;
		IOREG_NUM = 5;
	}
	
	// The unique ID associated with a Robot
	private final int RID;
	
	// The programs associated with this Robot
	private ArrayList<Program> programs;
	// TODO: refactor into Process class
	private int activeProgIdx;
	private int activeInstIdx;
	
	// The set of frames associated with a Robot
	private Frame[] toolFrames, userFrames;
	// The current Coordinate Frame for the Robot
	private CoordFrame curCoordFrame = CoordFrame.JOINT;
	
	// Indices of currently active frames
	private int activeUserFrame,
				activeToolFrame;
	// The registers associated with a Robot
	private DataRegister[] DREG;
	private PositionRegister[] PREG;
	
	private IORegister[] IOREG;

	// The end effectors of the Robot
	private final Model eeMSuction, eeMClaw, eeMClawPincer, eeMPointer,
						eeMGlueGun, eeMWielder;

	private final HashMap<EEType, Integer> EEToIORegMap;
	public EEType activeEndEffector;

	public int endEffectorState;

	public RobotMotion motionType;

	private ArrayList<Model> segments = new ArrayList<Model>();
	public int type;
	public float motorSpeed;
	// Indicates the direction of motion of the Robot when jogging
	public float[] jogLinear = new float[3];

	public float[] jogRot = new float[3];
	/* Bounding Boxes of the Robot Arm */
	public final BoundingBox[] armOBBs;
	/* Bounding Boxes unique to each End Effector */
	private final HashMap<EEType, ArrayList<BoundingBox>> eeOBBsMap;

	private final HashMap<EEType, ArrayList<BoundingBox>> eePickupOBBs;
	public Part held;

	/* Keep track of the Robot End Effector's orientation at the previous draw
	 * state */
	public float[][] oldEEOrientation;
	public PVector tgtPosition;

	public RQuaternion tgtOrientation;

	public ArmModel(int rid, PVector basePos) {
		int idx;
		
		RID = rid;
		BASE_POSITION = basePos;
		
		// Initialize the program list
		programs = new ArrayList<Program>();
		activeProgIdx = -1;
		activeInstIdx = -1;
		
		// Initializes the frames
		
		toolFrames = new Frame[FRAME_NUM];
		userFrames = new Frame[FRAME_NUM];
		
		for (idx = 0; idx < toolFrames.length; ++idx) {
			toolFrames[idx] = new ToolFrame();
			userFrames[idx] = new UserFrame();
		}
		
		activeUserFrame = -1;
		activeToolFrame = -1;
		
		// Initialize the registers
		
		DREG = new DataRegister[DPREG_NUM];
		PREG = new PositionRegister[DPREG_NUM];
		IOREG = new IORegister[IOREG_NUM];
		
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
		EEToIORegMap = new HashMap<EEType, Integer>();
		EEToIORegMap.put(EEType.SUCTION, 0);
		EEToIORegMap.put(EEType.CLAW, 1);
		EEToIORegMap.put(EEType.POINTER, 2);
		EEToIORegMap.put(EEType.GLUE_GUN, 3);
		EEToIORegMap.put(EEType.WIELDER, 4);

		motorSpeed = 1000.0f; // speed in mm/sec

		eeMSuction = new Model("SUCTION.stl", RobotRun.getInstance().color(108, 206, 214));
		eeMClaw = new Model("GRIPPER.stl", RobotRun.getInstance().color(108, 206, 214));
		eeMClawPincer = new Model("PINCER.stl", RobotRun.getInstance().color(200, 200, 0));
		eeMPointer = new Model("POINTER.stl", RobotRun.getInstance().color(108, 206, 214), 1f);
		eeMGlueGun = new Model("GLUE_GUN.stl", RobotRun.getInstance().color(108, 206, 214));
		eeMWielder = new Model("WIELDER.stl", RobotRun.getInstance().color(108, 206, 214));

		motionType = RobotMotion.HALTED;
		// Joint 1
		Model base = new Model("ROBOT_MODEL_1_BASE.STL", RobotRun.getInstance().color(200, 200, 0));
		base.rotations[1] = true;
		base.jointRanges[1] = new PVector(0, Fields.TWO_PI);
		base.rotationSpeed = Fields.radians(150)/60.0f;
		// Joint 2
		Model axis1 = new Model("ROBOT_MODEL_1_AXIS1.STL", RobotRun.getInstance().color(40, 40, 40));
		axis1.rotations[2] = true;
		axis1.jointRanges[2] = new PVector(4.34f, 2.01f);
		axis1.rotationSpeed = Fields.radians(150)/60.0f;
		// Joint 3
		Model axis2 = new Model("ROBOT_MODEL_1_AXIS2.STL", RobotRun.getInstance().color(200, 200, 0));
		axis2.rotations[2] = true;
		axis2.jointRanges[2] = new PVector(5.027f, 4.363f);
		axis2.rotationSpeed = Fields.radians(200)/60.0f;
		// Joint 4
		Model axis3 = new Model("ROBOT_MODEL_1_AXIS3.STL", RobotRun.getInstance().color(40, 40, 40));
		axis3.rotations[0] = true;
		axis3.jointRanges[0] = new PVector(0, Fields.TWO_PI);
		axis3.rotationSpeed = Fields.radians(250)/60.0f;
		// Joint 5
		Model axis4 = new Model("ROBOT_MODEL_1_AXIS4.STL", RobotRun.getInstance().color(40, 40, 40));
		axis4.rotations[2] = true;
		axis4.jointRanges[2] = new PVector(59f * Fields.PI / 40f, 11f * Fields.PI / 20f);
		axis4.rotationSpeed = Fields.radians(250)/60.0f;
		// Joint 6
		Model axis5 = new Model("ROBOT_MODEL_1_AXIS5.STL", RobotRun.getInstance().color(200, 200, 0));
		axis5.rotations[0] = true;
		axis5.jointRanges[0] = new PVector(0, Fields.TWO_PI);
		axis5.rotationSpeed = Fields.radians(420)/60.0f;
		Model axis6 = new Model("ROBOT_MODEL_1_AXIS6.STL", RobotRun.getInstance().color(40, 40, 40));
		segments.add(base);
		segments.add(axis1);
		segments.add(axis2);
		segments.add(axis3);
		segments.add(axis4);
		segments.add(axis5);
		segments.add(axis6);

		for(idx = 0; idx < jogLinear.length; ++idx) {
			jogLinear[idx] = 0;
		}

		for(idx = 0; idx < jogRot.length; ++idx) {
			jogRot[idx] = 0;
		}

		/* Initializes dimensions of the Robot Arm's hit boxes */
		armOBBs = new BoundingBox[7];

		armOBBs[0] = new BoundingBox(420, 115, 420);
		armOBBs[1] = new BoundingBox(317, 85, 317);
		armOBBs[2] = new BoundingBox(130, 185, 170);
		armOBBs[3] = new BoundingBox(74, 610, 135);
		armOBBs[4] = new BoundingBox(165, 165, 165);
		armOBBs[5] = new BoundingBox(160, 160, 160);
		armOBBs[6] = new BoundingBox(128, 430, 128);

		eeOBBsMap = new HashMap<EEType, ArrayList<BoundingBox>>();
		eePickupOBBs = new HashMap<EEType, ArrayList<BoundingBox>>();
		// Faceplate
		ArrayList<BoundingBox> limbo = new ArrayList<BoundingBox>();
		limbo.add( new BoundingBox(96, 96, 36) );
		eeOBBsMap.put(EEType.NONE, limbo);
		// Cannot pickup
		limbo = new ArrayList<BoundingBox>();
		eePickupOBBs.put(EEType.NONE, limbo);

		// Claw Gripper
		limbo = new ArrayList<BoundingBox>();
		limbo.add( new BoundingBox(96, 96, 54) );
		limbo.add( new BoundingBox(89, 21, 31) );
		limbo.add( new BoundingBox(89, 21, 31) );
		eeOBBsMap.put(EEType.CLAW, limbo);
		// In between the grippers
		limbo = new ArrayList<BoundingBox>();
		limbo.add(new BoundingBox(55, 3, 15) );
		limbo.get(0).setColor(RobotRun.getInstance().color(0, 0, 255));
		eePickupOBBs.put(EEType.CLAW, limbo);

		// Suction 
		limbo = new ArrayList<BoundingBox>();
		limbo.add( new BoundingBox(96, 96, 54) );
		limbo.add( new BoundingBox(37, 37, 82) );
		limbo.add( new BoundingBox(37, 62, 37) );
		eeOBBsMap.put(EEType.SUCTION, limbo);
		// One for each suction cup
		limbo = new ArrayList<BoundingBox>();
		limbo.add(new BoundingBox(25, 25, 3) );
		limbo.get(0).setColor(RobotRun.getInstance().color(0, 0, 255));
		limbo.add(new BoundingBox(25, 3, 25) );
		limbo.get(1).setColor(RobotRun.getInstance().color(0, 0, 255));
		eePickupOBBs.put(EEType.SUCTION, limbo);

		// Pointer
		limbo = new ArrayList<BoundingBox>();
		limbo.add( new BoundingBox(96, 96, 54) );
		limbo.add( new BoundingBox(32, 32, 34) );
		limbo.add( new BoundingBox(18, 18, 56) );
		limbo.add( new BoundingBox(9, 9, 37) );
		eeOBBsMap.put(EEType.POINTER, limbo);
		// Cannot pickup
		limbo = new ArrayList<BoundingBox>();
		eePickupOBBs.put(EEType.POINTER, limbo);

		// TODO Glue Gun
		limbo = new ArrayList<BoundingBox>();
		eeOBBsMap.put(EEType.GLUE_GUN, limbo);
		// Cannot pickup
		limbo = new ArrayList<BoundingBox>();
		eePickupOBBs.put(EEType.GLUE_GUN, limbo);

		// TODO Wielder
		limbo = new ArrayList<BoundingBox>();
		eeOBBsMap.put(EEType.WIELDER, limbo);
		// Cannot pickup
		limbo = new ArrayList<BoundingBox>();
		eePickupOBBs.put(EEType.WIELDER, limbo);

		held = null;
		// Initializes the old transformation matrix for the arm model
		RobotRun.getInstance().pushMatrix();
		RobotRun.applyModelRotation(this, getJointAngles());
		oldEEOrientation = RobotRun.getInstance().getTransformationMatrix();
		RobotRun.getInstance().popMatrix();
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

			if(programs.size() < 1) {
				programs.add(p);
				
			}  else {
				while(idx < programs.size() && programs.get(idx).getName().compareTo(p.getName()) < 0) { ++idx; }
				programs.add(idx, p);
			}

			return idx;
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
		RobotRun app = RobotRun.getInstance();

		if (!app.shift || app.motionFault) {
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
		
		if (!app.shift || app.motionFault) {
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
	 * Determines if the given angle is within the bounds of valid angles for
	 * the Robot's joint corresponding to the given index value.
	 * 
	 * @param joint  An integer between 0 and 5 which corresponds to one of
	 *               the Robot's joints J1 - J6
	 * @param angle  The angle in question
	 */
	public boolean anglePermitted(int joint, float angle) {
		joint = Fields.abs(joint) % 6;
		// Get the joint's range bounds
		PVector rangeBounds = getJointRange(joint);
		return RobotRun.angleWithinBounds(RobotRun.mod2PI(angle), rangeBounds.x, rangeBounds.y);
	}
	
	/**
	 * TODO comment this
	 */
	public boolean canPickup(Part p) {
		ArrayList<BoundingBox> curEEOBBs = eeOBBsMap.get(activeEndEffector);

		for (BoundingBox b : curEEOBBs) {
			// Cannot be colliding with a normal bounding box
			if (p != null && p.collision(b)) {
				return false;
			}
		}

		curEEOBBs = eePickupOBBs.get(activeEndEffector);

		for (BoundingBox b : curEEOBBs) {
			// Must be colliding with a pickup bounding box
			if (p != null && p.collision(b)) {
				return true;
			}
		}

		return false;
	}
	
	/* Determine if the given ojbect is collding with any part of the Robot. */
	public boolean checkObjectCollision(Part obj) {
		boolean collision = false;

		for(BoundingBox b : armOBBs) {
			if( obj.collision(b) ) {
				b.setColor(RobotRun.getInstance().color(255, 0, 0));
				collision = true;
			}
		}

		ArrayList<BoundingBox> eeHBs = eeOBBsMap.get(activeEndEffector);

		for(BoundingBox b : eeHBs) {
			if(obj.collision(b)) {
				b.setColor(RobotRun.getInstance().color(255, 0, 0));
				collision = true;
			}
		}

		return collision;
	}
	
	/**
	 * TODO comment
	 */
	public int checkPickupCollision(Scenario active) {
		// End Effector must be on and no object is currently held to be able to pickup an object
		if (endEffectorState == Fields.ON && held == null) {
			ArrayList<BoundingBox> curPUEEOBBs = eePickupOBBs.get(activeEndEffector);

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
	
	/* Determine if select pairs of hit boxes of the Robot Arm are colliding */
	public boolean checkSelfCollisions() {
		boolean collision = false;

		// Pairs of indices corresponding to two of the Arm body hit boxes, for which to check collisions
		int[] check_pairs = new int[] { 0, 3, 0, 4, 0, 5, 0, 6, 1, 5, 1, 6, 2, 5, 2, 6, 3, 5 };

		/* Check select collisions between the body segments of the Arm:
		 * The base segment and the four upper arm segments
		 * The base rotating segment and lower long arm segment as well as the upper long arm and
		 *   upper rotating end segment
		 * The second base rotating hit box and the upper long arm segment as well as the upper
		 *   rotating end segment
		 * The lower long arm segment and the upper rotating end segment
		 */
		for(int idx = 0; idx < check_pairs.length - 1; idx += 2) {
			if( Part.collision3D(armOBBs[ check_pairs[idx] ], armOBBs[ check_pairs[idx + 1] ]) ) {
				armOBBs[ check_pairs[idx] ].setColor(RobotRun.getInstance().color(255, 0, 0));
				armOBBs[ check_pairs[idx + 1] ].setColor(RobotRun.getInstance().color(255, 0, 0));
				collision = true;
			}
		}

		ArrayList<BoundingBox> eeHB = eeOBBsMap.get(activeEndEffector);

		// Check collisions between all EE hit boxes and base as well as the first long arm hit boxes
		for(BoundingBox hb : eeHB) {
			for(int idx = 0; idx < 4; ++idx) {
				if(Part.collision3D(hb, armOBBs[idx]) ) {
					hb.setColor(RobotRun.getInstance().color(255, 0, 0));
					armOBBs[idx].setColor(RobotRun.getInstance().color(255, 0, 0));
					collision = true;
				}
			}
		}

		return collision;
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
		// Set end effector state
		if (associatedIO != null) {
			endEffectorState = associatedIO.state;
		} else {
			endEffectorState = Fields.OFF;
		}

		releaseHeldObject();
	}
	
	public void draw() {
		RobotRun.getInstance().noStroke();
		RobotRun.getInstance().fill(200, 200, 0);

		RobotRun.getInstance().pushMatrix();
		RobotRun.getInstance().translate(BASE_POSITION.x, BASE_POSITION.y,
						BASE_POSITION.z);

		RobotRun.getInstance().rotateZ(Fields.PI);
		RobotRun.getInstance().rotateY(Fields.PI/2);
		segments.get(0).draw();
		RobotRun.getInstance().rotateY(-Fields.PI/2);
		RobotRun.getInstance().rotateZ(-Fields.PI);

		RobotRun.getInstance().fill(50);

		RobotRun.getInstance().translate(-50, -166, -358); // -115, -213, -413
		RobotRun.getInstance().rotateZ(Fields.PI);
		RobotRun.getInstance().translate(150, 0, 150);
		RobotRun.getInstance().rotateX(Fields.PI);
		RobotRun.getInstance().rotateY(segments.get(0).currentRotations[1]);
		RobotRun.getInstance().rotateX(-Fields.PI);
		RobotRun.getInstance().translate(-150, 0, -150);
		segments.get(1).draw();
		RobotRun.getInstance().rotateZ(-Fields.PI);

		RobotRun.getInstance().fill(200, 200, 0);

		RobotRun.getInstance().translate(-115, -85, 180);
		RobotRun.getInstance().rotateZ(Fields.PI);
		RobotRun.getInstance().rotateY(Fields.PI/2);
		RobotRun.getInstance().translate(0, 62, 62);
		RobotRun.getInstance().rotateX(segments.get(1).currentRotations[2]);
		RobotRun.getInstance().translate(0, -62, -62);
		segments.get(2).draw();
		RobotRun.getInstance().rotateY(-Fields.PI/2);
		RobotRun.getInstance().rotateZ(-Fields.PI);

		RobotRun.getInstance().fill(50);

		RobotRun.getInstance().translate(0, -500, -50);
		RobotRun.getInstance().rotateZ(Fields.PI);
		RobotRun.getInstance().rotateY(Fields.PI/2);
		RobotRun.getInstance().translate(0, 75, 75);
		RobotRun.getInstance().rotateZ(Fields.PI);
		RobotRun.getInstance().rotateX(segments.get(2).currentRotations[2]);
		RobotRun.getInstance().rotateZ(-Fields.PI);
		RobotRun.getInstance().translate(0, -75, -75);
		segments.get(3).draw();
		RobotRun.getInstance().rotateY(Fields.PI/2);
		RobotRun.getInstance().rotateZ(-Fields.PI);

		RobotRun.getInstance().translate(745, -150, 150);
		RobotRun.getInstance().rotateZ(Fields.PI/2);
		RobotRun.getInstance().rotateY(Fields.PI/2);
		RobotRun.getInstance().translate(70, 0, 70);
		RobotRun.getInstance().rotateY(segments.get(3).currentRotations[0]);
		RobotRun.getInstance().translate(-70, 0, -70);
		segments.get(4).draw();
		RobotRun.getInstance().rotateY(-Fields.PI/2);
		RobotRun.getInstance().rotateZ(-Fields.PI/2);

		RobotRun.getInstance().fill(200, 200, 0);

		RobotRun.getInstance().translate(-115, 130, -124);
		RobotRun.getInstance().rotateZ(Fields.PI);
		RobotRun.getInstance().rotateY(-Fields.PI/2);
		RobotRun.getInstance().translate(0, 50, 50);
		RobotRun.getInstance().rotateX(segments.get(4).currentRotations[2]);
		RobotRun.getInstance().translate(0, -50, -50);
		segments.get(5).draw();
		RobotRun.getInstance().rotateY(Fields.PI/2);
		RobotRun.getInstance().rotateZ(-Fields.PI);

		RobotRun.getInstance().fill(50);

		RobotRun.getInstance().translate(150, -10, 95);
		RobotRun.getInstance().rotateY(-Fields.PI/2);
		RobotRun.getInstance().rotateZ(Fields.PI);
		RobotRun.getInstance().translate(45, 45, 0);
		RobotRun.getInstance().rotateZ(segments.get(5).currentRotations[0]);
		RobotRun.getInstance().translate(-45, -45, 0);
		segments.get(6).draw();

		drawEndEffector(activeEndEffector, endEffectorState);

		RobotRun.getInstance().popMatrix();
		// My sketchy work-around
		if (RobotRun.getRobot() == this &&
				RobotRun.getInstance().showOOBs) { drawBoxes(); }
	}
	
	/* Draws the Robot Arm's hit boxes in the world */
	private void drawBoxes() {
		// Draw hit boxes of the body poriotn of the Robot Arm
		for(BoundingBox b : armOBBs) {
			b.draw();
		}

		ArrayList<BoundingBox> curEEHitBoxes = eeOBBsMap.get(activeEndEffector);

		// Draw End Effector hit boxes
		for(BoundingBox b : curEEHitBoxes) {
			b.draw();
		}

		curEEHitBoxes = eePickupOBBs.get(activeEndEffector);
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
			RobotRun.getInstance().rotateY(Fields.PI);
			RobotRun.getInstance().translate(-88, -37, 0);
			eeMSuction.draw();

		} else if(ee == EEType.CLAW) {
			RobotRun.getInstance().rotateY(Fields.PI);
			RobotRun.getInstance().translate(-88, 0, 0);
			eeMClaw.draw();
			RobotRun.getInstance().rotateZ(Fields.PI/2);

			if(eeState == Fields.OFF) {
				// Draw open grippers
				RobotRun.getInstance().translate(10, -85, 30);
				eeMClawPincer.draw();
				RobotRun.getInstance().translate(55, 0, 0);
				eeMClawPincer.draw();

			} else if(eeState == Fields.ON) {
				// Draw closed grippers
				RobotRun.getInstance().translate(28, -85, 30);
				eeMClawPincer.draw();
				RobotRun.getInstance().translate(20, 0, 0);
				eeMClawPincer.draw();
			}
		} else if (ee == EEType.POINTER) {
			RobotRun.getInstance().rotateY(Fields.PI);
			RobotRun.getInstance().rotateZ(Fields.PI);
			RobotRun.getInstance().translate(45, -45, 10);
			eeMPointer.draw();

		} else if (ee == EEType.GLUE_GUN) {
			RobotRun.getInstance().rotateZ(Fields.PI);
			RobotRun.getInstance().translate(-48, -46, -12);
			eeMGlueGun.draw();

		} else if (ee == EEType.WIELDER) {
			RobotRun.getInstance().rotateY(Fields.PI);
			RobotRun.getInstance().rotateZ(Fields.PI);
			RobotRun.getInstance().translate(46, -44, 10);
			eeMWielder.draw();
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
			for(int i = 0; i < segments.size(); i += 1) {
				Model model = segments.get(i);

				for(int n = 0; n < 3; n++) {
					if(model.rotations[n]) {
						float trialAngle = model.currentRotations[n] +
								model.rotationSpeed * model.jointsMoving[n] * RobotRun.getInstance().liveSpeed / 100f;
						trialAngle = RobotRun.mod2PI(trialAngle);
						
						if(model.anglePermitted(n, trialAngle)) {
							model.currentRotations[n] = trialAngle;
						} 
						else {
							System.out.printf("A[i%d, n=%d]: %f\n", i, n, trialAngle);
							model.jointsMoving[n] = 0;
							RobotRun.getInstance().updateRobotJogMotion(i, 0);
							halt();
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
				float distance = motorSpeed / 6000f * RobotRun.getInstance().liveSpeed;
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
				float theta = Fields.DEG_TO_RAD * 0.025f * RobotRun.getInstance().liveSpeed;
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
	 * @return	The active instruction of the active program, or null if no
	 * 			program is active
	 */
	public Instruction getActiveInstruction() {
		Program prog = getActiveProg();
		
		if (prog == null || activeInstIdx < 0 || activeInstIdx >= prog.size()) {
			// Invalid instruction or program index
			return null;
		}
		
		return prog.getInstruction(activeInstIdx);
	}
	
	/**
	 * @return	The index of the active program's active instruction
	 */
	public int getActiveInstIdx() {
		return activeInstIdx;
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
	 * @return	The active for this Robot, or null if no program is active
	 */
	public Program getActiveProg() {
		if (activeProgIdx < 0 || activeProgIdx >= programs.size()) {
			// Invalid program index
			return null;
		}
		
		return programs.get(activeProgIdx);
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
		Integer regIdx = EEToIORegMap.get(ee);

		if (regIdx != null && regIdx >= 0 && regIdx < IOREG.length) {
			return IOREG[regIdx];
		}

		return null;
	}

	//returns the rotational values for each arm joint
	public float[] getJointAngles() {
		float[] rot = new float[6];
		for(int i = 0; i < segments.size(); i += 1) {
			for(int j = 0; j < 3; j += 1) {
				if(segments.get(i).rotations[j]) {
					rot[i] = segments.get(i).currentRotations[j];
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
		joint = Fields.abs(joint) % 6;
		Model seg = segments.get(joint);

		for (int axes = 0; axes < 3; ++axes) {
			if (seg.rotations[axes]) {
				return seg.jointRanges[axes];
			}
		}
		// Should not be reachable
		return new PVector(0f, 0f, 0f);
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
		RealMatrix A = new Array2DRowRealMatrix(RobotRun.floatToDouble(m, 3, 3));
		RealMatrix B = new Array2DRowRealMatrix(RobotRun.floatToDouble(frame, 3, 3));
		RealMatrix AB = A.multiply(B.transpose());

		return RobotRun.doubleToFloat(AB.getData(), 3, 3);
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
		if (pdx >= 0 && pdx < programs.size()) {
			return programs.get(pdx);
			
		} else {
			// Invalid index
			return null;
		}
	}

	/**
	 * Returns the unique ID of the Robot.
	 */
	public int getRID() { return RID; }
	
	/**
	 * @return	A copy of the Robot's default position and orientation
	 */
	public Point getDefaultPoint() {
		return robotPoint.clone();
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
	 * Stops all robot movement
	 */
	public void halt() {
		for(Model model : segments) {
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

		// Reset button highlighting
		RobotRun.getInstance().resetButtonColors();
		motionType = RobotMotion.HALTED;
		RobotRun.getInstance().setProgramRunning(false);
	}

	public boolean interpolateRotation(float speed) {
		boolean done = true;

		for(Model a : segments) {
			for(int r = 0; r < 3; r++) {
				if(a.rotations[r]) {
					float distToDest = Fields.abs(a.currentRotations[r] - a.targetRotations[r]);

					if (distToDest <= 0.0001f) {
						// Destination (basically) met
						continue;

					} else if (distToDest >= (a.rotationSpeed * speed)) {
						done = false;
						a.currentRotations[r] += a.rotationSpeed * a.rotationDirections[r] * speed;
						a.currentRotations[r] = RobotRun.mod2PI(a.currentRotations[r]);

					} else if (distToDest > 0.0001f) {
						// Destination too close to move at current speed
						a.currentRotations[r] = a.targetRotations[r];
						a.currentRotations[r] = RobotRun.mod2PI(a.currentRotations[r]);
					}
				}
			} // end loop through rotation axes
		} // end loop through arm segments
		return done;
	} // end interpolate rotation

	/**
	 * Returns true if at least one joint of the Robot is in motion.
	 */
	public boolean jointMotion() {
		for(Model m : segments) {
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
	 * @returning   EXEC_FAILURE if inverse kinematics fails or the joint angles returned
	 *              are invalid and EXEC_SUCCESS if the Robot is successfully moved to the
	 *              given position
	 */
	public int jumpTo(PVector destPosition, RQuaternion destOrientation) {
		boolean invalidAngle = false;
		float[] srcAngles = getJointAngles();
		// Calculate the joint angles for the desired position and orientation
		float[] destAngles = RobotRun.inverseKinematics(this, srcAngles, destPosition, destOrientation);

		// Check the destination joint angles with each joint's range of valid joint angles
		for(int joint = 0; !(destAngles == null) && joint < 6; joint += 1) {
			if (!anglePermitted(joint, destAngles[joint])) {
				invalidAngle = true;

				if (RobotRun.DISPLAY_TEST_OUTPUT) {
					PVector rangeBounds = getJointRange(joint);
					System.out.printf("Invalid angle: J[%d] = %4.3f : [%4.3f -> %4.3f]\n", joint,
							destAngles[joint], rangeBounds.x, rangeBounds.y);
				} 
			}
		}

		// Did we successfully find the desired angles?
		if ((destAngles == null) || invalidAngle) {
			if (RobotRun.DISPLAY_TEST_OUTPUT) {
				Point RP = RobotRun.nativeRobotEEPoint(this, getJointAngles());
				System.out.printf("IK Failure ...\n%s -> %s\n%s -> %s\n\n", RP.position, destPosition,
						RP.orientation, destOrientation);
			}

			RobotRun.getInstance().triggerFault();
			return RobotRun.EXEC_FAILURE;
		}

		setJointAngles(destAngles);
		return RobotRun.EXEC_SUCCESS;
	}

	/**
	 * Indicates that the Robot Arm is in motion.
	 */
	public boolean modelInMotion() {
		return RobotRun.getInstance().isProgramRunning() || motionType != RobotMotion.HALTED ||
				jointMotion() || translationalMotion() || rotationalMotion();
	}

	/**
	 * TODO comment
	 */
	public void moveTo(float[] jointAngles) {
		setupRotationInterpolation(jointAngles);
		motionType = RobotMotion.MT_JOINT;
	}

	/**
	 * TODO comment
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
		return programs.size();
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
		if (pdx >= 0 && pdx < programs.size()) {
			// Return the removed program
			return programs.remove(pdx);
			
		} else {
			// Invalid index
			return null;
		}
	}

	/* Changes all the Robot Arm's hit boxes to green */
	public void resetOBBColors() {
		for(BoundingBox b : armOBBs) {
			b.setColor(RobotRun.getInstance().color(0, 255, 0));
		}

		ArrayList<BoundingBox> eeHB = eeOBBsMap.get(activeEndEffector);

		for(BoundingBox b : eeHB) {
			b.setColor(RobotRun.getInstance().color(0, 255, 0));
		}
	}

	/**
	 * Returns true if the Robot is jogging rotationally.
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
		
		if (prog != null && instIdx >= 0 && instIdx < prog.getInstructions().size()) {
			// Set the active instruction
			activeInstIdx = instIdx;
			return true;
		}
		
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
		if (progIdx >= 0 && progIdx < programs.size()) {
			// Set the active program
			activeProgIdx = progIdx;
			return true;
		}
		
		return false;
	}

	public void setActiveToolFrame(int activeToolFrame) {
		this.activeToolFrame = activeToolFrame;
	}

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

	//convenience method to set all joint rotation values of the robot arm
	public void setJointAngles(float[] rot) {
		for(int i = 0; i < segments.size(); i += 1) {
			for(int j = 0; j < 3; j += 1) {
				if(segments.get(i).rotations[j]) {
					segments.get(i).currentRotations[j] = rot[i];
					segments.get(i).currentRotations[j] %= Fields.TWO_PI;
					if(segments.get(i).currentRotations[j] < 0) {
						segments.get(i).currentRotations[j] += Fields.TWO_PI;
					}
				}
			}
		}
	}//end set joint rotations

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
		if(joint >= 0 && joint < segments.size()) {

			Model model = segments.get(joint);
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
	 * Sets the Robot's default position and orientation in a static variable.
	 * THIS METHOD MUST BE CALLED WHEN THE FIRST ROBOT IS CREATED!
	 */
	protected void setDefaultRobotPoint() {
		// Define the default Robot position and orientation
		robotPoint = RobotRun.nativeRobotPoint(this,
				new float[] { 0f, 0f, 0f, 0f, 0f, 0f });
	}

	/**
	 * Sets the Model's target joint angles to the given set of angles and updates the
	 * rotation directions of each of the joint segments.
	 */
	public void setupRotationInterpolation(float[] tgtAngles) {
		// Set the Robot's target angles
		for(int n = 0; n < tgtAngles.length; n++) {
			for(int r = 0; r < 3; r++) {
				if(segments.get(n).rotations[r]) {
					segments.get(n).targetRotations[r] = tgtAngles[n];
				}
			}
		}

		// Calculate whether it's faster to turn CW or CCW
		for(int joint = 0; joint < 6; ++joint) {
			Model a = RobotRun.getInstance().getArmModel().segments.get(joint);

			for(int r = 0; r < 3; r++) {
				if(a.rotations[r]) {
					// The minimum distance between the current and target joint angles
					float dist_t = RobotRun.minimumDistance(a.currentRotations[r], a.targetRotations[r]);

					// check joint movement range
					if(a.jointRanges[r].x == 0 && a.jointRanges[r].y == Fields.TWO_PI) {
						a.rotationDirections[r] = (dist_t < 0) ? -1 : 1;
					}
					else {  
						/* Determine if at least one bound lies within the range of the shortest angle
						 * between the current joint angle and the target angle. If so, then take the
						 * longer angle, otherwise choose the shortest angle path. */

						// The minimum distance from the current joint angle to the lower bound of the joint's range
						float dist_lb = RobotRun.minimumDistance(a.currentRotations[r], a.jointRanges[r].x);

						// The minimum distance from the current joint angle to the upper bound of the joint's range
						float dist_ub = RobotRun.minimumDistance(a.currentRotations[r], a.jointRanges[r].y);

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
			endEffectorState = Fields.OFF;
		} else {
			endEffectorState = Fields.ON;
		}

		updateIORegister();
		checkPickupCollision(RobotRun.getInstance().activeScenario);
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

		RobotRun.getInstance().rotateZ(Fields.PI);
		RobotRun.getInstance().rotateY(Fields.PI/2);
		RobotRun.getInstance().translate(200, 50, 200);
		// Segment 0
		armOBBs[0].setCoordinateSystem();

		RobotRun.getInstance().translate(0, 100, 0);
		armOBBs[1].setCoordinateSystem();

		RobotRun.getInstance().translate(-200, -150, -200);

		RobotRun.getInstance().rotateY(-Fields.PI/2);
		RobotRun.getInstance().rotateZ(-Fields.PI);

		RobotRun.getInstance().translate(-50, -166, -358);
		RobotRun.getInstance().rotateZ(Fields.PI);
		RobotRun.getInstance().translate(150, 0, 150);
		RobotRun.getInstance().rotateX(Fields.PI);
		RobotRun.getInstance().rotateY(segments.get(0).currentRotations[1]);
		RobotRun.getInstance().rotateX(-Fields.PI);
		RobotRun.getInstance().translate(10, 95, 0);
		RobotRun.getInstance().rotateZ(-0.1f * Fields.PI);
		// Segment 1
		armOBBs[2].setCoordinateSystem();

		RobotRun.getInstance().rotateZ(0.1f * Fields.PI);
		RobotRun.getInstance().translate(-160, -95, -150);
		RobotRun.getInstance().rotateZ(-Fields.PI);

		RobotRun.getInstance().translate(-115, -85, 180);
		RobotRun.getInstance().rotateZ(Fields.PI);
		RobotRun.getInstance().rotateY(Fields.PI/2);
		RobotRun.getInstance().translate(0, 62, 62);
		RobotRun.getInstance().rotateX(segments.get(1).currentRotations[2]);
		RobotRun.getInstance().translate(30, 240, 0);
		// Segment 2
		armOBBs[3].setCoordinateSystem();

		RobotRun.getInstance().translate(-30, -302, -62);
		RobotRun.getInstance().rotateY(-Fields.PI/2);
		RobotRun.getInstance().rotateZ(-Fields.PI);

		RobotRun.getInstance().translate(0, -500, -50);
		RobotRun.getInstance().rotateZ(Fields.PI);
		RobotRun.getInstance().rotateY(Fields.PI/2);
		RobotRun.getInstance().translate(0, 75, 75);
		RobotRun.getInstance().rotateZ(Fields.PI);
		RobotRun.getInstance().rotateX(segments.get(2).currentRotations[2]);
		RobotRun.getInstance().rotateZ(-Fields.PI);
		RobotRun.getInstance().translate(75, 0, 0);
		// Segment 3
		armOBBs[4].setCoordinateSystem();

		RobotRun.getInstance().translate(-75, -75, -75);
		RobotRun.getInstance().rotateY(Fields.PI/2);
		RobotRun.getInstance().rotateZ(-Fields.PI);

		RobotRun.getInstance().translate(745, -150, 150);
		RobotRun.getInstance().rotateZ(Fields.PI/2);
		RobotRun.getInstance().rotateY(Fields.PI/2);
		RobotRun.getInstance().translate(70, 0, 70);
		RobotRun.getInstance().rotateY(segments.get(3).currentRotations[0]);
		RobotRun.getInstance().translate(5, 75, 5);
		// Segment 4
		armOBBs[5].setCoordinateSystem();

		RobotRun.getInstance().translate(0, 295, 0);
		armOBBs[6].setCoordinateSystem();

		RobotRun.getInstance().translate(-75, -370, -75);

		RobotRun.getInstance().rotateY(-Fields.PI/2);
		RobotRun.getInstance().rotateZ(-Fields.PI/2);

		RobotRun.getInstance().translate(-115, 130, -124);
		RobotRun.getInstance().rotateZ(Fields.PI);
		RobotRun.getInstance().rotateY(-Fields.PI/2);
		RobotRun.getInstance().translate(0, 50, 50);
		RobotRun.getInstance().rotateX(segments.get(4).currentRotations[2]);
		RobotRun.getInstance().translate(0, -50, -50);
		// Segment 5
		RobotRun.getInstance().rotateY(Fields.PI/2);
		RobotRun.getInstance().rotateZ(-Fields.PI);

		RobotRun.getInstance().translate(150, -10, 95);
		RobotRun.getInstance().rotateY(-Fields.PI/2);
		RobotRun.getInstance().rotateZ(Fields.PI);
		RobotRun.getInstance().translate(45, 45, 0);
		RobotRun.getInstance().rotateZ(segments.get(5).currentRotations[0]);
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
		ArrayList<BoundingBox> curEEOBBs = eeOBBsMap.get(current),
				curPUEEOBBs = eePickupOBBs.get(current);

		RobotRun.getInstance().pushMatrix();
		RobotRun.getInstance().resetMatrix();
		RobotRun.applyModelRotation(this, getJointAngles());

		switch(current) {
		case NONE:
			// Face Plate EE
			RobotRun.getInstance().translate(0, 0, 12);
			curEEOBBs.get(0).setCoordinateSystem();
			RobotRun.getInstance().translate(0, 0, -12);
			break;

		case CLAW:
			// Claw Gripper EE
			RobotRun.getInstance().translate(0, 0, 3);
			curEEOBBs.get(0).setCoordinateSystem();

			RobotRun.getInstance().translate(-2, 0, -57);
			curPUEEOBBs.get(0).setCoordinateSystem();

			if (endEffectorState == Fields.OFF) {
				// When claw is open
				RobotRun.getInstance().translate(0, 27, 0);
				curEEOBBs.get(1).setCoordinateSystem();
				RobotRun.getInstance().translate(0, -54, 0);
				curEEOBBs.get(2).setCoordinateSystem();
				RobotRun.getInstance().translate(0, 27, 0);

			} else if (endEffectorState == Fields.ON) {
				// When claw is closed
				RobotRun.getInstance().translate(0, 10, 0);
				curEEOBBs.get(1).setCoordinateSystem();
				RobotRun.getInstance().translate(0, -20, 0);
				curEEOBBs.get(2).setCoordinateSystem();
				RobotRun.getInstance().translate(0, 10, 0);
			}

			RobotRun.getInstance().translate(2, 0, 54);
			break;

		case SUCTION:
			// Suction EE
			RobotRun.getInstance().translate(0, 0, 3);
			curEEOBBs.get(0).setCoordinateSystem();

			RobotRun.getInstance().translate(-2, 0, -67);
			BoundingBox limbo = curEEOBBs.get(1);
			limbo.setCoordinateSystem();

			float dist = -43;
			RobotRun.getInstance().translate(0, 0, dist);
			curPUEEOBBs.get(0).setCoordinateSystem();
			RobotRun.getInstance().translate(0, -50, 19 - dist);
			limbo = curEEOBBs.get(2);
			limbo.setCoordinateSystem();

			dist = -33;
			RobotRun.getInstance().translate(0, dist, 0);
			curPUEEOBBs.get(1).setCoordinateSystem();
			RobotRun.getInstance().translate(2, 50 - dist, 45);
			break;

		case POINTER:
			// Pointer EE
			RobotRun.getInstance().translate(0, 0, 3);
			curEEOBBs.get(0).setCoordinateSystem();

			RobotRun.getInstance().translate(0, 0, -43);
			curEEOBBs.get(1).setCoordinateSystem();
			RobotRun.getInstance().translate(0, -18, -34);
			RobotRun.getInstance().rotateX(-0.75f);
			curEEOBBs.get(2).setCoordinateSystem();
			RobotRun.getInstance().rotateX(0.75f);
			RobotRun.getInstance().translate(0, -21, -32);
			curEEOBBs.get(3).setCoordinateSystem();
			RobotRun.getInstance().translate(0, 39, 109);
			break;

		case GLUE_GUN:
			// TODO
			break;

		case WIELDER:
			// TODO
			break;

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
		RobotRun.getInstance().pushMatrix();
		RobotRun.getInstance().resetMatrix();
		RobotRun.applyModelRotation(this, getJointAngles());
		// Keep track of the old coordinate frame of the armModel
		oldEEOrientation = RobotRun.getInstance().getTransformationMatrix();
		RobotRun.getInstance().popMatrix();
	}

	/**
	 * Update the Robot's position and orientation (as well as
	 * those of its bounding boxes) based on the active
	 * program or a move to command, or jogging.
	 */
	public void updateRobot() {
		if (!RobotRun.getInstance().motionFault) {
			// Execute arm movement
			if(RobotRun.getInstance().isProgramRunning()) {
				// Run active program
				RobotRun.getInstance().setProgramRunning(
						!RobotRun.getInstance().executeProgram(this,
								RobotRun.getInstance().execSingleInst));

				// Check the call stack for any waiting processes
				if (!RobotRun.getInstance().getCall_stack().isEmpty() &&
						activeInstIdx == getActiveProg().getInstructions().size()) {
					
					int[] prevProc = RobotRun.getInstance().getCall_stack().pop();
					// Return to the process on the top of the stack
					setActiveProgIdx(prevProc[0]);
					setActiveInstIdx(prevProc[1]);
					// Update the display
					RobotRun.getInstance().getContentsMenu().setLineIdx(activeInstIdx);
					RobotRun.getInstance().getContentsMenu().setColumnIdx(0);
					RobotRun.getInstance().updateScreen();
				}

			} else if (motionType != RobotMotion.HALTED) {
				// Move the Robot progressively to a point
				boolean doneMoving = true;

				switch (RobotRun.getInstance().getArmModel().motionType) {
				case MT_JOINT:
					doneMoving = interpolateRotation(
							(RobotRun.getInstance().liveSpeed / 100.0f));
					break;
				case MT_LINEAR:
					doneMoving = RobotRun.getInstance().executeMotion(this,
							(RobotRun.getInstance().liveSpeed / 100.0f));
					break;
				default:
				}

				if (doneMoving) {
					halt();
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

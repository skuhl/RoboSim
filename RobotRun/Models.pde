import java.util.concurrent.ArrayBlockingQueue;

public class Triangle {
  // normal, vertex 1, vertex 2, vertex 3
  public PVector[] components = new PVector[4];
}

public class Model {
  public PShape mesh;
  public String name;
  public boolean[] rotations = new boolean[3]; // is rotating on this joint valid?
  // Joint ranges follow a clockwise format running from the PVector.x to PVector.y, where PVector.x and PVector.y range from [0, TWO_PI]
  public PVector[] jointRanges = new PVector[3];
  public float[] currentRotations = new float[3]; // current rotation value
  public float[] targetRotations = new float[3]; // we want to be rotated to this value
  public int[] rotationDirections = new int[3]; // use shortest direction rotation
  public float rotationSpeed;
  public float[] jointsMoving = new float[3]; // for live control using the joint buttons
  
  /**
   * Use default scaling
   */
  public Model(String filename, color col) {
    for(int n = 0; n < 3; n++) {
      rotations[n] = false;
      currentRotations[n] = 0;
      jointRanges[n] = null;
    }
    rotationSpeed = 0.01;
    name = filename;
    loadSTLModel(filename, col, 1.0);
  }
  
  /**
   * Define the scaling of the Model.
   */
  public Model(String filename, color col, float scaleVal) {
    for(int n = 0; n < 3; n++) {
      rotations[n] = false;
      currentRotations[n] = 0;
      jointRanges[n] = null;
    }
    rotationSpeed = 0.01;
    name = filename;
    loadSTLModel(filename, col, scaleVal);
  }
  
  void loadSTLModel(String filename, color col, float scaleVal) {
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
    mesh = createShape();
    mesh.beginShape(TRIANGLES);
    mesh.noStroke();
    mesh.scale(scaleVal);
    mesh.fill(col);
    for(Triangle t : triangles) {
      mesh.normal(t.components[0].x, t.components[0].y, t.components[0].z);
      mesh.vertex(t.components[1].x, t.components[1].y, t.components[1].z);
      mesh.vertex(t.components[2].x, t.components[2].y, t.components[2].z);
      mesh.vertex(t.components[3].x, t.components[3].y, t.components[3].z);
    }
    mesh.endShape();
  } // end loadSTLModel
  
  public boolean anglePermitted(int idx, float angle) {
    
    if(jointRanges[idx].x < jointRanges[idx].y) {
      // Joint range does not overlap TWO_PI
      return angle >= jointRanges[idx].x && angle < jointRanges[idx].y;
    } else {
      // Joint range overlaps TWO_PI
      return !(angle >= jointRanges[idx].y && angle < jointRanges[idx].x);
    }
  }
  
  public void draw() {
    shape(mesh);
  }
  
} // end Model class

public class ArmModel {
  
  public EEType activeEndEffector;
  public int endEffectorState;
  private final HashMap<EEType, Integer> EEToIORegMap;
  
  private Model eeMSuction, eeMClaw, eeMClawPincer, eeMPointer, eeMGlueGun, eeMWielder;
  
  public RobotMotion motionType;
  
  public ArrayList<Model> segments = new ArrayList<Model>();
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
  /* Keep track of the Robot End Effector's orientation at the previous draw state */
  public float[][] oldEEOrientation;
  
  public PVector tgtPosition;
  public float[] tgtOrientation;
  
  public ArmModel() {
    activeEndEffector = EEType.NONE;
    endEffectorState = OFF;
    // Initialize the End Effector to IO Register mapping
    EEToIORegMap = new HashMap<EEType, Integer>();
    EEToIORegMap.put(EEType.SUCTION, 0);
    EEToIORegMap.put(EEType.CLAW, 1);
    EEToIORegMap.put(EEType.POINTER, 2);
    EEToIORegMap.put(EEType.GLUE_GUN, 3);
    EEToIORegMap.put(EEType.WIELDER, 4);
    
    motorSpeed = 1000.0; // speed in mm/sec
    
    eeMSuction = new Model("SUCTION.stl", color(108, 206, 214));
    eeMClaw = new Model("GRIPPER.stl", color(108, 206, 214));
    eeMClawPincer = new Model("PINCER.stl", color(200, 200, 0));
    eeMPointer = new Model("POINTER.stl", color(108, 206, 214), 1f);
    eeMGlueGun = new Model("GLUE_GUN.stl", color(108, 206, 214));
    eeMWielder = new Model("WIELDER.stl", color(108, 206, 214));
    
    motionType = RobotMotion.HALTED;
    // Joint 1
    Model base = new Model("ROBOT_MODEL_1_BASE.STL", color(200, 200, 0));
    base.rotations[1] = true;
    base.jointRanges[1] = new PVector(0, TWO_PI);
    base.rotationSpeed = radians(150)/60.0;
    // Joint 2
    Model axis1 = new Model("ROBOT_MODEL_1_AXIS1.STL", color(40, 40, 40));
    axis1.rotations[2] = true;
    axis1.jointRanges[2] = new PVector(4.34, 2.01);
    axis1.rotationSpeed = radians(150)/60.0;
    // Joint 3
    Model axis2 = new Model("ROBOT_MODEL_1_AXIS2.STL", color(200, 200, 0));
    axis2.rotations[2] = true;
    axis2.jointRanges[2] = new PVector(5.027f, 4.363f);
    axis2.rotationSpeed = radians(200)/60.0;
    // Joint 4
    Model axis3 = new Model("ROBOT_MODEL_1_AXIS3.STL", color(40, 40, 40));
    axis3.rotations[0] = true;
    axis3.jointRanges[0] = new PVector(0, TWO_PI);
    axis3.rotationSpeed = radians(250)/60.0;
    // Joint 5
    Model axis4 = new Model("ROBOT_MODEL_1_AXIS4.STL", color(40, 40, 40));
    axis4.rotations[2] = true;
    axis4.jointRanges[2] = new PVector(59f * PI / 40f, 11f * PI / 20f);
    axis4.rotationSpeed = radians(250)/60.0;
    // Joint 6
    Model axis5 = new Model("ROBOT_MODEL_1_AXIS5.STL", color(200, 200, 0));
    axis5.rotations[0] = true;
    axis5.jointRanges[0] = new PVector(0, TWO_PI);
    axis5.rotationSpeed = radians(420)/60.0;
    Model axis6 = new Model("ROBOT_MODEL_1_AXIS6.STL", color(40, 40, 40));
    segments.add(base);
    segments.add(axis1);
    segments.add(axis2);
    segments.add(axis3);
    segments.add(axis4);
    segments.add(axis5);
    segments.add(axis6);
    
    for(int idx = 0; idx < jogLinear.length; ++idx) {
      jogLinear[idx] = 0;
    }
    
    for(int idx = 0; idx < jogRot.length; ++idx) {
      jogRot[idx] = 0;
    }
    
    /* Initialies dimensions of the Robot Arm's hit boxes */
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
    limbo.add( new BoundingBox(102, 102, 36) );
    eeOBBsMap.put(EEType.NONE, limbo);
    // Cannot pickup
    limbo = new ArrayList<BoundingBox>();
    eePickupOBBs.put(EEType.NONE, limbo);
    
    // Claw Gripper
    limbo = new ArrayList<BoundingBox>();
    limbo.add( new BoundingBox(102, 102, 46) );
    limbo.add( new BoundingBox(89, 21, 31) );
    limbo.add( new BoundingBox(89, 21, 31) );
    eeOBBsMap.put(EEType.CLAW, limbo);
    // In between the grippers
    limbo = new ArrayList<BoundingBox>();
    limbo.add(new BoundingBox(55, 3, 15) );
    limbo.get(0).setColor(color(0, 0, 255));
    eePickupOBBs.put(EEType.CLAW, limbo);
    
    // Suction 
    limbo = new ArrayList<BoundingBox>();
    limbo.add( new BoundingBox(102, 102, 46) );
    limbo.add( new BoundingBox(37, 37, 82/*87*/) );
    limbo.add( new BoundingBox(37, 62/*67*/, 37) );
    eeOBBsMap.put(EEType.SUCTION, limbo);
    // One for each suction cup
    limbo = new ArrayList<BoundingBox>();
    limbo.add(new BoundingBox(25, 25, 3) );
    limbo.get(0).setColor(color(0, 0, 255));
    limbo.add(new BoundingBox(25, 3, 25) );
    limbo.get(1).setColor(color(0, 0, 255));
    eePickupOBBs.put(EEType.SUCTION, limbo);
    
    // Pointer
    limbo = new ArrayList<BoundingBox>();
    limbo.add( new BoundingBox(102, 102, 46) );
    limbo.add( new BoundingBox(24, 24, 32) );
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
    pushMatrix();
    applyModelRotation(getJointAngles());
    oldEEOrientation = getTransformationMatrix();
    popMatrix();
  } // end ArmModel constructor
  
  /**
   * Update the Robot's position and orientation (as well as
   * those of its bounding boxes) based on the active
   * program or a move to command, or jogging.
   */
  public void updateRobot(Program active) {
    if (!robotFault) {
      // Execute arm movement
      if(programRunning) {
        // Run active program
        programRunning = !executeProgram(active, this, execSingleInst);
        
      } else if (motionType != RobotMotion.HALTED) {
        // Move the Robot progressively to a point
        boolean doneMoving = true;
        
        switch (armModel.motionType) {
          case MT_JOINT:
            doneMoving = interpolateRotation((liveSpeed / 100.0));
            break;
          case MT_LINEAR:
            doneMoving = executeMotion(this, (liveSpeed / 100.0));
            break;
          default:
        }
        
        if (doneMoving) {
          halt();
        }
        
      } else if (modelInMotion()) {
        // Jog the Robot
        intermediatePositions.clear();
        executeLiveMotion();
      }
    }
    
    updateCollisionOBBs();
  }
  
  public void draw() {
    noStroke();
    fill(200, 200, 0);
    
    pushMatrix();
    translate(ROBOT_POSITION.x, ROBOT_POSITION.y, ROBOT_POSITION.z);
    
    rotateZ(PI);
    rotateY(PI/2);
    segments.get(0).draw();
    rotateY(-PI/2);
    rotateZ(-PI);
    
    fill(50);
    
    translate(-50, -166, -358); // -115, -213, -413
    rotateZ(PI);
    translate(150, 0, 150);
    rotateX(PI);
    rotateY(segments.get(0).currentRotations[1]);
    rotateX(-PI);
    translate(-150, 0, -150);
    segments.get(1).draw();
    rotateZ(-PI);
    
    fill(200, 200, 0);
    
    translate(-115, -85, 180);
    rotateZ(PI);
    rotateY(PI/2);
    translate(0, 62, 62);
    rotateX(segments.get(1).currentRotations[2]);
    translate(0, -62, -62);
    segments.get(2).draw();
    rotateY(-PI/2);
    rotateZ(-PI);
    
    fill(50);

    translate(0, -500, -50);
    rotateZ(PI);
    rotateY(PI/2);
    translate(0, 75, 75);
    rotateZ(PI);
    rotateX(segments.get(2).currentRotations[2]);
    rotateZ(-PI);
    translate(0, -75, -75);
    segments.get(3).draw();
    rotateY(PI/2);
    rotateZ(-PI);
    
    translate(745, -150, 150);
    rotateZ(PI/2);
    rotateY(PI/2);
    translate(70, 0, 70);
    rotateY(segments.get(3).currentRotations[0]);
    translate(-70, 0, -70);
    segments.get(4).draw();
    rotateY(-PI/2);
    rotateZ(-PI/2);
    
    fill(200, 200, 0);
    
    translate(-115, 130, -124);
    rotateZ(PI);
    rotateY(-PI/2);
    translate(0, 50, 50);
    rotateX(segments.get(4).currentRotations[2]);
    translate(0, -50, -50);
    segments.get(5).draw();
    rotateY(PI/2);
    rotateZ(-PI);
    
    fill(50);
    
    translate(150, -10, 95);
    rotateY(-PI/2);
    rotateZ(PI);
    translate(45, 45, 0);
    rotateZ(segments.get(5).currentRotations[0]);
    translate(-45, -45, 0);
    segments.get(6).draw();
    
    drawEndEffector(activeEndEffector, endEffectorState);
    
    popMatrix();
    
    if (COLLISION_DISPLAY) { drawBoxes(); }
  }//end draw arm model
  
  /**
   * Draw the End Effector model associated with the given
   * End Effector type in the current coordinate system.
   * 
   * @param ee       The End Effector to draw
   * @param eeState  The state of the End Effector to be drawn
   */
  private void drawEndEffector(EEType ee, int eeState) {
    pushMatrix();
    
    // Center the End Effector on the Robot's faceplate and draw it.
    if(ee == EEType.SUCTION) {
      rotateY(PI);
      translate(-88, -37, 0);
      eeMSuction.draw();
      
    } else if(ee == EEType.CLAW) {
      rotateY(PI);
      translate(-88, 0, 0);
      eeMClaw.draw();
      rotateZ(PI/2);
      
      if(eeState == OFF) {
        // Draw open grippers
        translate(10, -85, 30);
        eeMClawPincer.draw();
        translate(55, 0, 0);
        eeMClawPincer.draw();
        
      } else if(eeState == ON) {
        // Draw closed grippers
        translate(28, -85, 30);
        eeMClawPincer.draw();
        translate(20, 0, 0);
        eeMClawPincer.draw();
      }
    } else if (ee == EEType.POINTER) {
      rotateY(PI);
      rotateZ(PI);
      translate(45, -45, 10);
      eeMPointer.draw();
      
    } else if (ee == EEType.GLUE_GUN) {
      rotateZ(PI);
      translate(-48, -46, -12);
      eeMGlueGun.draw();
      
    } else if (ee == EEType.WIELDER) {
      rotateY(PI);
      rotateZ(PI);
      translate(46, -44, 10);
      eeMWielder.draw();
    }
    
    popMatrix();
  }
  
  /**
   * Updates the position and orientation of the hit
   * boxes related to the Robot Arm.
   */
  private void updateCollisionOBBs() { 
    noFill();
    stroke(0, 255, 0);
    
    pushMatrix();
    resetMatrix();
    
    translate(ROBOT_POSITION.x, ROBOT_POSITION.y, ROBOT_POSITION.z);
    
    rotateZ(PI);
    rotateY(PI/2);
    translate(200, 50, 200);
    // Segment 0
    armOBBs[0].setCoordinateSystem();
    
    translate(0, 100, 0);
    armOBBs[1].setCoordinateSystem();
    
    translate(-200, -150, -200);
    
    rotateY(-PI/2);
    rotateZ(-PI);
    
    translate(-50, -166, -358);
    rotateZ(PI);
    translate(150, 0, 150);
    rotateX(PI);
    rotateY(segments.get(0).currentRotations[1]);
    rotateX(-PI);
    translate(10, 95, 0);
    rotateZ(-0.1f * PI);
    // Segment 1
    armOBBs[2].setCoordinateSystem();
    
    rotateZ(0.1f * PI);
    translate(-160, -95, -150);
    rotateZ(-PI);
    
    translate(-115, -85, 180);
    rotateZ(PI);
    rotateY(PI/2);
    translate(0, 62, 62);
    rotateX(segments.get(1).currentRotations[2]);
    translate(30, 240, 0);
    // Segment 2
    armOBBs[3].setCoordinateSystem();
    
    translate(-30, -302, -62);
    rotateY(-PI/2);
    rotateZ(-PI);
    
    translate(0, -500, -50);
    rotateZ(PI);
    rotateY(PI/2);
    translate(0, 75, 75);
    rotateZ(PI);
    rotateX(segments.get(2).currentRotations[2]);
    rotateZ(-PI);
    translate(75, 0, 0);
    // Segment 3
    armOBBs[4].setCoordinateSystem();
    
    translate(-75, -75, -75);
    rotateY(PI/2);
    rotateZ(-PI);
    
    translate(745, -150, 150);
    rotateZ(PI/2);
    rotateY(PI/2);
    translate(70, 0, 70);
    rotateY(segments.get(3).currentRotations[0]);
    translate(5, 75, 5);
    // Segment 4
    armOBBs[5].setCoordinateSystem();
    
    translate(0, 295, 0);
    armOBBs[6].setCoordinateSystem();
    
    translate(-75, -370, -75);
    
    rotateY(-PI/2);
    rotateZ(-PI/2);
    
    translate(-115, 130, -124);
    rotateZ(PI);
    rotateY(-PI/2);
    translate(0, 50, 50);
    rotateX(segments.get(4).currentRotations[2]);
    translate(0, -50, -50);
    // Segment 5
    rotateY(PI/2);
    rotateZ(-PI);
    
    translate(150, -10, 95);
    rotateY(-PI/2);
    rotateZ(PI);
    translate(45, 45, 0);
    rotateZ(segments.get(5).currentRotations[0]);
    translate(-45, -45, 0);
    popMatrix();
    
    // End Effector
    updateOBBBoxesForEE(activeEndEffector);
  }
  
  /**
   * Updates position and orientation of the hit boxes associated
   * with the given End Effector.
   */
  private void updateOBBBoxesForEE(EEType current) {
    ArrayList<BoundingBox> curEEOBBs = eeOBBsMap.get(current),
                           curPUEEOBBs = eePickupOBBs.get(current);
    
    pushMatrix();
    resetMatrix();
    applyModelRotation(getJointAngles());
    
    switch(current) {
      case NONE:
        // Face Plate EE
        translate(0, 0, 10);
        curEEOBBs.get(0).setCoordinateSystem();
        translate(0, 0, -10);
        break;
        
      case CLAW:
        // Claw Gripper EE
        curEEOBBs.get(0).setCoordinateSystem();
        
        translate(-2, 0, -54);
        curPUEEOBBs.get(0).setCoordinateSystem();
        
        if (endEffectorState == OFF) {
          // When claw is open
          translate(0, 27, 0);
          curEEOBBs.get(1).setCoordinateSystem();
          translate(0, -54, 0);
          curEEOBBs.get(2).setCoordinateSystem();
          translate(0, 27, 0);
          
        } else if (endEffectorState == ON) {
          // When claw is closed
          translate(0, 10, 0);
          curEEOBBs.get(1).setCoordinateSystem();
          translate(0, -20, 0);
          curEEOBBs.get(2).setCoordinateSystem();
          translate(0, 10, 0);
        }
        
        translate(2, 0, 54);
        break;
        
      case SUCTION:
        // Suction EE
        curEEOBBs.get(0).setCoordinateSystem();
        
        translate(-2, 0, -64);
        BoundingBox limbo = curEEOBBs.get(1);
        limbo.setCoordinateSystem();
        
        float dist = -43;
        translate(0, 0, dist);
        curPUEEOBBs.get(0).setCoordinateSystem();
        translate(0, -50, 19 - dist);
        limbo = curEEOBBs.get(2);
        limbo.setCoordinateSystem();
        
        dist = -33;
        translate(0, dist, 0);
        curPUEEOBBs.get(1).setCoordinateSystem();
        translate(2, 50 - dist, 45);
        break;
        
      case POINTER:
        // Pointer EE
        curEEOBBs.get(0).setCoordinateSystem();

        translate(0, 0, -30);
        curEEOBBs.get(1).setCoordinateSystem();
        translate(0, -18, -34);
        rotateX(-0.75);
        curEEOBBs.get(2).setCoordinateSystem();
        rotateX(0.75);
        translate(0, -21, -32);
        curEEOBBs.get(3).setCoordinateSystem();
        translate(0, 21, 32);
        translate(0, 18, 34);
        translate(0, 0, 30);
        break;
      
      case GLUE_GUN:
        // TODO
        break;
      
      case WIELDER:
        // TODO
        break;
        
      default:
    }
    
    popMatrix();
  }
  
  /**
   * Updates the reference to the Robot's previous
   * End Effector orientation, which is used to move
   * the object held by the Robot.
   */
  public void updatePreviousEEOrientation() {
    pushMatrix();
    resetMatrix();
    applyModelRotation(armModel.getJointAngles());
    // Keep track of the old coordinate frame of the armModel
    oldEEOrientation = getTransformationMatrix();
    popMatrix();
  }
  
  /* Changes all the Robot Arm's hit boxes to green */
  public void resetOBBColors() {
    for(BoundingBox b : armOBBs) {
      b.setColor(color(0, 255, 0));
    }
    
    ArrayList<BoundingBox> eeHB = eeOBBsMap.get(activeEndEffector);
    
    for(BoundingBox b : eeHB) {
      b.setColor(color(0, 255, 0));
    }
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
      if( collision3D(armOBBs[ check_pairs[idx] ], armOBBs[ check_pairs[idx + 1] ]) ) {
        armOBBs[ check_pairs[idx] ].setColor(color(255, 0, 0));
        armOBBs[ check_pairs[idx + 1] ].setColor(color(255, 0, 0));
        collision = true;
      }
    }
    
    ArrayList<BoundingBox> eeHB = eeOBBsMap.get(activeEndEffector);
    
    // Check collisions between all EE hit boxes and base as well as the first long arm hit boxes
    for(BoundingBox hb : eeHB) {
      for(int idx = 0; idx < 4; ++idx) {
        if(collision3D(hb, armOBBs[idx]) ) {
          hb.setColor(color(255, 0, 0));
          armOBBs[idx].setColor(color(255, 0, 0));
          collision = true;
        }
      }
    }
    
    return collision;
  }
  
  /* Determine if the given ojbect is collding with any part of the Robot. */
  public boolean checkObjectCollision(Part obj) {
    boolean collision = false;
    
    for(BoundingBox b : armOBBs) {
      if( obj.collision(b) ) {
        b.setColor(color(255, 0, 0));
        collision = true;
      }
    }
    
    ArrayList<BoundingBox> eeHBs = eeOBBsMap.get(activeEndEffector);
    
    for(BoundingBox b : eeHBs) {
      if(obj.collision(b)) {
        b.setColor(color(255, 0, 0));
        collision = true;
      }
    }
    
    return collision;
  }
  
  /* Draws the Robot Arm's hit boxes in the world */
  public void drawBoxes() {
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
  }//end get joint rotations
  
  /**
   * Determines if the given angle is within the bounds of valid angles for
   * the Robot's joint corresponding to the given index value.
   * 
   * @param joint  An integer between 0 and 5 which corresponds to one of
   *               the Robot's joints J1 - J6
   * @param angle  The angle in question
   */
  public boolean anglePermitted(int joint, float angle) {
    joint = abs(joint) % 6;
    // Get the joint's range bounds
    PVector rangeBounds = getJointRange(joint);
    return angleWithinBounds(mod2PI(angle), rangeBounds.x, rangeBounds.y);
  }
  
  /**
   * Returns the start and endpoint of the range of angles, which
   8 are valid for the joint of the Robot, corresponding to the
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
    joint = abs(joint) % 6;
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
    pushMatrix();
    resetMatrix();
    applyModelRotation(getJointAngles());
    float[][] matrix = getRotationMatrix();
    popMatrix();
    
    return matrix;
  }
  
  /* Calculate and returns a 3x3 matrix whose columns are the unit vectors of
   * the end effector's current x, y, z axes with respect to an arbitrary coordinate
   * system specified by the rotation matrix 'frame.'
   */
  public float[][] getOrientationMatrix(float[][] frame) {
    float[][] m = getOrientationMatrix();
    RealMatrix A = new Array2DRowRealMatrix(floatToDouble(m, 3, 3));
    RealMatrix B = new Array2DRowRealMatrix(floatToDouble(frame, 3, 3));
    RealMatrix AB = A.multiply(B.transpose());
    
    return doubleToFloat(AB.getData(), 3, 3);
  }
  
  //convenience method to set all joint rotation values of the robot arm
  public void setJointAngles(float[] rot) {
    for(int i = 0; i < segments.size(); i += 1) {
      for(int j = 0; j < 3; j += 1) {
        if(segments.get(i).rotations[j]) {
          segments.get(i).currentRotations[j] = rot[i];
          segments.get(i).currentRotations[j] %= TWO_PI;
          if(segments.get(i).currentRotations[j] < 0) {
            segments.get(i).currentRotations[j] += TWO_PI;
          }
        }
      }
    }
  }//end set joint rotations
  
  public boolean interpolateRotation(float speed) {
    boolean done = true;
    
    for(Model a : segments) {
      for(int r = 0; r < 3; r++) {
        if(a.rotations[r]) {
          float distToDest = abs(a.currentRotations[r] - a.targetRotations[r]);
          
          if (distToDest <= 0.0001f) {
            // Destination (basically) met
            continue;
            
          } else if (distToDest >= (a.rotationSpeed * speed)) {
            done = false;
            a.currentRotations[r] += a.rotationSpeed * a.rotationDirections[r] * speed;
            a.currentRotations[r] = mod2PI(a.currentRotations[r]);
            
          } else if (distToDest > 0.0001f) {
            // Destination too close to move at current speed
            a.currentRotations[r] = a.targetRotations[r];
            a.currentRotations[r] = mod2PI(a.currentRotations[r]);
          }
        }
      } // end loop through rotation axes
    } // end loop through arm segments
    return done;
  } // end interpolate rotation
  
  /**
   * Sets the Model's target joint angles to the given set of angles and updates the
   * rotation directions of each of the joint segments.
   */
  public void setupRotationInterpolation(float[] tgtAngles) {
    // Set the Robot's target angles
    for(int n = 0; n < tgtAngles.length; n++) {
      for(int r = 0; r < 3; r++) {
        if(armModel.segments.get(n).rotations[r])
        armModel.segments.get(n).targetRotations[r] = tgtAngles[n];
      }
    }
    
    // Calculate whether it's faster to turn CW or CCW
    for(int joint = 0; joint < 6; ++joint) {
      Model a = armModel.segments.get(joint);
      
      for(int r = 0; r < 3; r++) {
        if(a.rotations[r]) {
          // The minimum distance between the current and target joint angles
          float dist_t = minimumDistance(a.currentRotations[r], a.targetRotations[r]);
          
          // check joint movement range
          if(a.jointRanges[r].x == 0 && a.jointRanges[r].y == TWO_PI) {
            a.rotationDirections[r] = (dist_t < 0) ? -1 : 1;
          }
          else {  
            /* Determine if at least one bound lies within the range of the shortest angle
            * between the current joint angle and the target angle. If so, then take the
            * longer angle, otherwise choose the shortest angle path. */
            
            // The minimum distance from the current joint angle to the lower bound of the joint's range
            float dist_lb = minimumDistance(a.currentRotations[r], a.jointRanges[r].x);
            
            // The minimum distance from the current joint angle to the upper bound of the joint's range
            float dist_ub = minimumDistance(a.currentRotations[r], a.jointRanges[r].y);
            
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
            model.rotationSpeed * model.jointsMoving[n] * liveSpeed / 100f;
            trialAngle = mod2PI(trialAngle);
            
            if(model.anglePermitted(n, trialAngle)) {
              
              float old_angle = model.currentRotations[n];
              model.currentRotations[n] = trialAngle;
              
              if(armModel.checkSelfCollisions()) {
                // End robot arm movement
                model.currentRotations[n] = old_angle;
                updateCollisionOBBs();
                model.jointsMoving[n] = 0;
                halt();
              }
            } 
            else {
              model.jointsMoving[n] = 0;
              halt();
            }
          }
        }
      }
      
    } else {
      // Jog in the World, Tool or User Frame
      Frame curFrame;
      
      if (curCoordFrame == CoordFrame.TOOL) {
        curFrame = getActiveFrame(CoordFrame.TOOL);
      } else if (curCoordFrame == CoordFrame.USER) {
        curFrame = getActiveFrame(CoordFrame.USER);
      } else {
        curFrame = null;
      }
      
      Point curPoint = nativeRobotEEPoint(getJointAngles());
      
      // Apply translational motion vector
      if (translationalMotion()) {
        // Respond to user defined movement
        float distance = motorSpeed / 6000f * liveSpeed;
        PVector translation = new PVector(jogLinear[0], jogLinear[1], jogLinear[2]);
        translation = rotateVector(translation.mult(distance), WORLD_AXES);
        
        if (curFrame != null) {
            // Convert the movement vector into the current reference frame
          translation = rotateVectorQuat(translation, curFrame.getOrientationNegation());
        }
        
        tgtPosition.add(translation);
      } else {
        // No translational motion
        tgtPosition = curPoint.position;
      }
      
      // Apply rotational motion vector
      if (rotationalMotion()) {
        // Respond to user defined movement
        float theta = DEG_TO_RAD * 0.025f * liveSpeed;
        PVector rotation = new PVector(jogRot[0], jogRot[1], jogRot[2]);
        rotation = convertWorldToNative(rotation);
        
        if (curFrame != null) {
          // Convert the movement vector into the current reference frame
          rotation = rotateVectorQuat(rotation, curFrame.getOrientationNegation());
        }
        rotation.normalize();
        
        tgtOrientation = rotateQuat(tgtOrientation, rotation, theta);
        
        if (quaternionDotProduct(tgtOrientation, curPoint.orientation) < 0f) {
          // Use -q instead of q
          tgtOrientation = vectorScalarMult(tgtOrientation, -1);
        }
      } else {
        // No rotational motion
        tgtOrientation = curPoint.orientation;
      }
      
      jumpTo(tgtPosition, tgtOrientation);
    }
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
  public int jumpTo(PVector destPosition, float[] destOrientation) {
    boolean invalidAngle = false;
    float[] srcAngles = getJointAngles();
    // Calculate the joint angles for the desired position and orientation
    float[] destAngles = inverseKinematics(srcAngles, destPosition, destOrientation);
    
    // Check the destination joint angles with each joint's range of valid joint angles
    for(int joint = 0; !(destAngles == null) && joint < 6; joint += 1) {
      if (!anglePermitted(joint, destAngles[joint])) {
        invalidAngle = true;
        
        if (DISPLAY_TEST_OUTPUT) {
          PVector rangeBounds = getJointRange(joint);
          System.out.printf("Invalid angle: J[%d] = %4.3f : [%4.3f -> %4.3f]\n", joint,
                             destAngles[joint], rangeBounds.x, rangeBounds.y);
        } 
      }
    }
    
    // Did we successfully find the desired angles?
    if ((destAngles == null) || invalidAngle) {
      if (DISPLAY_TEST_OUTPUT) {
        Point RP = nativeRobotEEPoint(getJointAngles());
        System.out.printf("IK Failure ...\n%s -> %s\n%s -> %s\n\n", RP.position, destPosition,
                                arrayToString(RP.orientation), arrayToString(destOrientation));
      }
      
      triggerFault();
      return EXEC_FAILURE;
    }

    setJointAngles(destAngles);
    return EXEC_SUCCESS;
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
  public void moveTo(PVector position, float[] orientation) {
    Point start = nativeRobotEEPoint(armModel.getJointAngles());
    Point end = new Point(position, orientation, start.angles);
    beginNewLinearMotion(start, end);
    motionType = RobotMotion.MT_LINEAR;
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
      endEffectorState = OFF;
    }
    
    releaseHeldObject();
  }
  
  /**
   * Toggle the Robot's state between ON and OFF. Update the
   * Robot's currently held world object as well.
   */
  public void toggleEEState() {
    if (endEffectorState == ON) {
      endEffectorState = OFF;
    } else {
      endEffectorState = ON;
    }
    
    updateIORegister();
    checkPickupCollision();
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
  
  /**
   * TODO comment
   */
  public int checkPickupCollision() {
    // End Effector must be on and no object is currently held to be able to pickup an object
    if (endEffectorState == ON && armModel.held == null) {
      ArrayList<BoundingBox> curPUEEOBBs = eePickupOBBs.get(activeEndEffector);
      Scenario s = activeScenario();
      // Can this End Effector pick up objects?
      if (s != null && curPUEEOBBs.size() > 0) {
        
        for (WorldObject wldObj : s) {
          // Only parts can be picked up
          if (wldObj instanceof Part && canPickup( (Part)wldObj )) {
              // Pickup the object
              held = (Part)wldObj;
              return 0;
          }
        }
      }
      
    } else if (endEffectorState == OFF && armModel.held != null) {
      // Release the object
      armModel.releaseHeldObject();
      return 1;
    }
    
    return 2;
  }
  
  /**
   * If an object is currently being held by the Robot arm, then release it.
   * Then, update the Robot's End Effector status and IO Registers.
   */
  public void releaseHeldObject() {
    if (held != null) {
      endEffectorState = OFF;
      updateIORegister();
      armModel.held = null;
    }
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
   * Returns the I/O register associated with the given End Effector
   * type, or null if noy such I/O register exists.
   */
  public IORegister getIORegisterFor(EEType ee) {
    Integer regIdx = EEToIORegMap.get(ee);
    
    if (regIdx != null && regIdx >= 0 && regIdx < IO_REG.length) {
      return IO_REG[regIdx];
    }
    
    return null;
  }
  
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
   * Returns true if the Robot is jogging translationally.
   */
  public boolean translationalMotion() {
    return jogLinear[0] != 0 || jogLinear[1] != 0 || jogLinear[2] != 0;
  }
  
  /**
   * Returns true if the Robot is jogging rotationally.
   */
  public boolean rotationalMotion() {
    return jogRot[0] != 0 || jogRot[1] != 0 || jogRot[2] != 0;
  }
  
  /**
   * Indicates that the Robot Arm is in motion.
   */
  public boolean modelInMotion() {
    return programRunning || motionType != RobotMotion.HALTED ||
           jointMotion() || translationalMotion() || rotationalMotion();
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
    resetButtonColors();
    motionType = RobotMotion.HALTED;
    programRunning = false;
  }
} // end ArmModel class
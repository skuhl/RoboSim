import java.util.concurrent.ArrayBlockingQueue;

//To-do list:
//Collision detection

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
  
  public Model(String filename, color col) {
    for(int n = 0; n < 3; n++) {
      rotations[n] = false;
      currentRotations[n] = 0;
      jointRanges[n] = null;
    }
    rotationSpeed = 0.01;
    name = filename;
    loadSTLModel(filename, col);
  }
  
  void loadSTLModel(String filename, color col) {
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

/* The possible values for the current Coordinate Frame */
public enum CoordFrame { JOINT, WORLD, TOOL, USER }
/* The current Coordinate Frame for the Robot */
public static CoordFrame curCoordFrame = CoordFrame.JOINT;

/* The possible types of End Effectors for the Robot */
public enum EndEffector { NONE, SUCTION, CLAW; }
/* The possible settings for the End Effector's status */
public enum EEStatus { ON, OFF }

public class ArmModel {
  
  public EndEffector activeEndEffector = EndEffector.NONE;
  public EEStatus endEffectorStatus = EEStatus.OFF;

  public ArrayList<Model> segments = new ArrayList<Model>();
  public int type;
  public float motorSpeed;
  // Indicates translational jog motion in the World Frame
  public float[] jogLinear = new float[3];
  // Indicates rotational jog motion in the World Frame
  public float[] jogRot = new float[3];
  // Indicates that the Robot is moving to a specific point
  public boolean inMotion = false;
  public float[] tgtRot = new float[4];
  public PVector tgtPos = new PVector();
  public float[][] currentFrame = {{1, 0, 0},
    {0, 1, 0}, 
    {0, 0, 1}};
  
  public Box[] bodyHitBoxes;
  private ArrayList<Box>[] eeHitBoxes;
  
  public WorldObject held;
  public float[][] oldEETMatrix;
  
  public ArmModel() {
    
    motorSpeed = 1000.0; // speed in mm/sec
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
    axis2.jointRanges[2] = new PVector(12f * PI / 20f, 8f * PI / 20f);
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
    bodyHitBoxes = new Box[7];
    
    bodyHitBoxes[0] = new Box(420, 115, 420, color(0, 255, 0));
    bodyHitBoxes[1] = new Box(317, 85, 317, color(0, 255, 0));
    bodyHitBoxes[2] = new Box(130, 185, 170, color(0, 255, 0));
    bodyHitBoxes[3] = new Box(74, 610, 135, color(0, 255, 0));
    bodyHitBoxes[4] = new Box(165, 165, 165, color(0, 255, 0));
    bodyHitBoxes[5] = new Box(160, 160, 160, color(0, 255, 0));
    bodyHitBoxes[6] = new Box(128, 430, 128, color(0, 255, 0));
    
    eeHitBoxes = (ArrayList<Box>[])new ArrayList[4]; 
    // Face plate
    eeHitBoxes[0] = new ArrayList<Box>();
    eeHitBoxes[0].add( new Box(102, 102, 36, color(0, 255, 0)) );
    // Claw Gripper (closed)
    eeHitBoxes[1] = new ArrayList<Box>();
    eeHitBoxes[1].add( new Box(102, 102, 46, color(0, 255, 0)) );
    eeHitBoxes[1].add( new Box(89, 43, 31, color(0, 255, 0)) );
    // Claw Gripper (open)
    eeHitBoxes[2] = new ArrayList<Box>();
    eeHitBoxes[2].add( new Box(102, 102, 46, color(0, 255, 0)) );
    eeHitBoxes[2].add( new Box(89, 21, 31, color(0, 255, 0)) );
    eeHitBoxes[2].add( new Box(89, 21, 31, color(0, 255, 0)) );
    // Suction 
    eeHitBoxes[3] = new ArrayList<Box>();
    eeHitBoxes[3].add( new Box(102, 102, 46, color(0, 255, 0)) );
    eeHitBoxes[3].add( new Box(37, 37, 87, color(0, 255, 0)) );
    eeHitBoxes[3].add( new Box(37, 67, 37, color(0, 255, 0)) );
    
    held = null;
    // Initializes the old transformation matrix for the arm model
    pushMatrix();
    applyModelRotation(this, false);
    oldEETMatrix = getTransformationMatrix();
    popMatrix();
  } // end ArmModel constructor
  
  public void draw() {
    
    noStroke();
    fill(200, 200, 0);
    
    translate(600, 200, 0);

    rotateZ(PI);
    rotateY(PI/2);
    segments.get(0).draw();
    rotateY(-PI/2);
    rotateZ(-PI);
    
    fill(50);
    
    translate(-50, -166, -358); // -115, -213, -413
    rotateZ(PI);
    translate(150, 0, 150);
    rotateY(segments.get(0).currentRotations[1]);
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
    rotateX(segments.get(2).currentRotations[2]);
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
    
    // next, the end effector
    if(activeEndEffector == EndEffector.SUCTION) {
      rotateY(PI);
      translate(-88, -37, 0);
      eeModelSuction.draw();
    } else if(activeEndEffector == EndEffector.CLAW) {
      rotateY(PI);
      translate(-88, 0, 0);
      eeModelClaw.draw();
      rotateZ(PI/2);
      if(endEffectorStatus == EEStatus.OFF) {
        translate(10, -85, 30);
        eeModelClawPincer.draw();
        translate(55, 0, 0);
        eeModelClawPincer.draw();
      } else if(endEffectorStatus == EEStatus.ON) {
        translate(28, -85, 30);
        eeModelClawPincer.draw();
        translate(20, 0, 0);
        eeModelClawPincer.draw();
      }
    }
  }//end draw arm model
  
  /* Updates the position and orientation of the hit boxes related
   * to the Robot Arm. */
  private void updateBoxes() { 
    noFill();
    stroke(0, 255, 0);
    
    pushMatrix();
    resetMatrix();
    translate(600, 200, 0);

    rotateZ(PI);
    rotateY(PI/2);
    translate(200, 50, 200);
    // Segment 0
    bodyHitBoxes[0].setTransform(getTransformationMatrix());
    
    translate(0, 100, 0);
    bodyHitBoxes[1].setTransform(getTransformationMatrix());
    
    translate(-200, -150, -200);
    
    rotateY(-PI/2);
    rotateZ(-PI);
    
    translate(-50, -166, -358);
    rotateZ(PI);
    translate(150, 0, 150);
    rotateY(segments.get(0).currentRotations[1]);
    translate(10, 95, 0);
    rotateZ(-0.1f * PI);
    // Segment 1
    bodyHitBoxes[2].setTransform(getTransformationMatrix());
    
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
    bodyHitBoxes[3].setTransform(getTransformationMatrix());
    
    translate(-30, -302, -62);
    rotateY(-PI/2);
    rotateZ(-PI);
    
    translate(0, -500, -50);
    rotateZ(PI);
    rotateY(PI/2);
    translate(0, 75, 75);
    rotateX(segments.get(2).currentRotations[2]);
    translate(75, 0, 0);
    // Segment 3
    bodyHitBoxes[4].setTransform(getTransformationMatrix());
    
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
    bodyHitBoxes[5].setTransform(getTransformationMatrix());
    
    translate(0, 295, 0);
    bodyHitBoxes[6].setTransform(getTransformationMatrix());
    
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
    
    // End Effector
    // Face Plate EE
    translate(0, 0, 10);
    eeHitBoxes[0].get(0).setTransform(getTransformationMatrix());
    translate(0, 0, -10);
    
    // Claw Gripper EE
    float[][] transform = getTransformationMatrix();
    eeHitBoxes[1].get(0).setTransform(transform);
    eeHitBoxes[2].get(0).setTransform(transform);
    eeHitBoxes[3].get(0).setTransform(transform);
    
    translate(-2, 0, -54);
    eeHitBoxes[1].get(1).setTransform(getTransformationMatrix());
    translate(2, 0, 54);
    // The Claw EE has two separate hit box lists: one for the open claw and another for the closed claw
    translate(-2, 27, -54);
    eeHitBoxes[2].get(1).setTransform(getTransformationMatrix());
    translate(0, -54, 0);
    eeHitBoxes[2].get(2).setTransform(getTransformationMatrix());
    translate(2, 27, 54);
    
    // Suction EE
    translate(-2, 0, -66);
    eeHitBoxes[3].get(1).setTransform(getTransformationMatrix());
    translate(0, -52, 21);
    eeHitBoxes[3].get(2).setTransform(getTransformationMatrix());
    translate(2, 52, 35);
    
    translate(-45, -45, 0);
    popMatrix();
  }
  
  /* Returns one of the Arraylists for the End Effector hit boxes depending on the
   * current active End Effector and the status of the End Effector. */
  public ArrayList<Box> currentEEHitBoxList() {
    // Determine which set of hit boxes to display based on the active End Effector
    if(activeEndEffector == EndEffector.CLAW) {
      return (endEffectorStatus == EEStatus.ON) ? eeHitBoxes[1] : eeHitBoxes[2];
    } else if(activeEndEffector == EndEffector.SUCTION) {
      return eeHitBoxes[3];
    }
    
    return eeHitBoxes[0];
  }
  
  /* Changes all the Robot Arm's hit boxes to green */
  public void resetBoxColors() {
    for(Box b : bodyHitBoxes) {
      b.outline = color(0, 255, 0);
    }
    
    ArrayList<Box> eeHB = currentEEHitBoxList();
    
    for(Box b : eeHB) {
      b.outline = color(0, 255, 0);
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
      if( collision3D(bodyHitBoxes[ check_pairs[idx] ], bodyHitBoxes[ check_pairs[idx + 1] ]) ) {
        bodyHitBoxes[ check_pairs[idx] ].outline = color(255, 0, 0);
        bodyHitBoxes[ check_pairs[idx + 1] ].outline = color(255, 0, 0);
        collision = true;
      }
    }
    
    ArrayList<Box> eeHB = currentEEHitBoxList();
    
    // Check collisions between all EE hit boxes and base as well as the first long arm hit boxes
    for(Box hb : eeHB) {
      for(int idx = 0; idx < 4; ++idx) {
        if(collision3D(hb, bodyHitBoxes[idx]) ) {
          hb.outline = color(255, 0, 0);
          bodyHitBoxes[idx].outline = color(255, 0, 0);
          collision = true;
        }
      }
    }
    
    return collision;
  }
  
  /* Determine if the given ojbect is collding with any part of the Robot. */
  public boolean checkObjectCollision(WorldObject obj) {
    Box ohb = (Box)obj.hit_box;
    boolean collision = false;
    
    for(Box b : bodyHitBoxes) {
      if( collision3D(ohb, b) ) {
        b.outline = color(255, 0, 0);
        collision = true;
      }
    }
    
    ArrayList<Box> eeHBs = currentEEHitBoxList();
    
    for(Box b : eeHBs) {
      // Special case for held objects
      if( (activeEndEffector != EndEffector.CLAW || activeEndEffector != EndEffector.SUCTION || endEffectorStatus != EEStatus.ON || b != eeHitBoxes[1].get(1) || obj != armModel.held) && collision3D(ohb, b) ) {
        b.outline = color(255, 0, 0);
        collision = true;
      }
    }
    
    return collision;
  }
  
  /* Draws the Robot Arm's hit boxes in the world */
  public void drawBoxes() {
    // Draw hit boxes of the body poriotn of the Robot Arm
    for(Box b : bodyHitBoxes) {
      pushMatrix();
      b.applyTransform();
      b.draw();
      popMatrix();
    }
    
    int eeIdx = 0;
    // Determine which set of hit boxes to display based on the active End Effector
    if(activeEndEffector == EndEffector.CLAW) {
      if(endEffectorStatus == EEStatus.ON) {
        eeIdx = 1;
      } else {
        eeIdx = 2;
      }
    } else if(activeEndEffector == EndEffector.SUCTION) {
      eeIdx = 3;
    }
    // Draw End Effector hit boxes
    for(Box b : eeHitBoxes[eeIdx]) {
      pushMatrix();
      b.applyTransform();
      b.draw();
      popMatrix();
    }
  }
  
  //returns the rotational values for each arm joint
  public float[] getJointRotations() {
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
  
  /* Resets the robot's current reference frame to that of the
   * default world frame.
   */
  public void resetFrame() {
    currentFrame[0][0] = 1;
    currentFrame[0][1] = 0;
    currentFrame[0][2] = 0;
    
    currentFrame[1][0] = 0;
    currentFrame[1][1] = 1;
    currentFrame[1][2] = 0;
    
    currentFrame[2][0] = 0;
    currentFrame[2][1] = 0;
    currentFrame[2][2] = 1;
  }
  
  /* Calculate and returns a 3x3 matrix whose columns are the unit vectors of
   * the end effector's current x, y, z axes with respect to the current frame.
   */
  public float[][] getRotationMatrix() {
    pushMatrix();
    resetMatrix();
    // Switch to End Effector reference Frame
    applyModelRotation(armModel, true);
    /* Define vectors { 0, 0, 0 }, { 1, 0, 0 }, { 0, 1, 0 }, and { 0, 0, 1 }
     * Swap vectors:
     *   x' = z
     *   y' = x
     *   z' = y
     */
    PVector origin = new PVector(modelX(0, 0, 0), modelY(0, 0, 0), modelZ(0, 0, 0)),
    x = new PVector(modelX(0, 0, -1), modelY(0, 0, -1), modelZ(0, 0, -1)),
    y = new PVector(modelX(0, 1, 0), modelY(0, 1, 0), modelZ(0, 1, 0)),
    z = new PVector(modelX(1, 0, 0), modelY(1, 0, 0), modelZ(1, 0, 0));
    
    float[][] matrix = new float[3][3];
    // Calcualte Unit Vectors form difference between each axis vector and the origin
    
    matrix[0][0] = x.x - origin.x;
    matrix[0][1] = x.y - origin.y;
    matrix[0][2] = x.z - origin.z;
    matrix[1][0] = y.x - origin.x;
    matrix[1][1] = y.y - origin.y;
    matrix[1][2] = y.z - origin.z;
    matrix[2][0] = z.x - origin.x;
    matrix[2][1] = z.y - origin.y;
    matrix[2][2] = z.z - origin.z;
    
    popMatrix();
    
    return matrix;
  }
  
  /* Calculate and returns a 3x3 matrix whose columns are the unit vectors of
   * the end effector's current x, y, z axes with respect to an arbitrary coordinate
   * system specified by the rotation matrix 'frame.'
   */
  public float[][] getRotationMatrix(float[][] frame) {
    float[][] m = getRotationMatrix();
    RealMatrix A = new Array2DRowRealMatrix(floatToDouble(m, 3, 3));
    RealMatrix B = new Array2DRowRealMatrix(floatToDouble(frame, 3, 3));
    RealMatrix AB = A.multiply(B.transpose());
    
    //println(AB);
    
    return doubleToFloat(AB.getData(), 3, 3);
  }
  
  /* Applies the transformation for the current tool frame.
   * NOTE: This method only works in the TOOL or WORLD frame! */
  public void applyToolFrame(int list_idx) {
    // If a tool Frame is active, then it overrides the World Frame
    if(list_idx >= 0 && list_idx < toolFrames.length) {
      
      // Apply a custom tool frame
      PVector tr = toolFrames[list_idx].getOrigin();
      translate(tr.x, tr.y, tr.z);
      /*
       Native Frame to World Frame
       x' = -x
       y' = -z
       z' = y
       
       World Frame to End Effector Frame, Native Frame to End Effector Frame
       x'' = -z' -> -y
       y'' = -y' -> z
       z'' = x'  -> -x
      */
    } else {
      
      // Apply a default tool frame based on the current EE
      if(activeEndEffector == EndEffector.CLAW) {
        translate(0, 0, -54);
      } else if(activeEndEffector == EndEffector.SUCTION) {
        translate(0, 0, -105);
      }
    }
  }
  
  /* This method calculates the Euler angular rotations: roll, pitch and yaw of the Robot's
   * End Effector in the form of a vector array.
   *
   * @param axesMatrix  A 3x3 matrix containing unti vectors representing the Robot's End
   *                    Effector's x, y, z axes in respect of the World Coordinate Frame;
   * @returning         A array containing the End Effector's roll, pitch, and yaw, in that
   *                    order
   *
   *  Method based off of procedure outlined in the pdf at this location:
   *     http://www.staff.city.ac.uk/~sbbh653/publications/euler.pdf
   *     rotation about: x - psi, y - theta, z - phi
   */
  public PVector getWPR() {
    float[][] m = getRotationMatrix(currentFrame);
    PVector wpr = matrixToEuler(m);
    
    return wpr;
  }
  
  public PVector getWPR(float[] testAngles) {
    float[] origAngles = getJointRotations();
    setJointRotations(testAngles);
    
    PVector ret = getWPR();
    setJointRotations(origAngles);
    return ret;
  }
  
  //returns the rotational value of the robot as a quaternion
  public float[] getQuaternion() {
    float[][] m = getRotationMatrix(currentFrame);
    float[] q = matrixToQuat(m);
    
    return q;
  }
  
  public float[] getQuaternion(float[] testAngles) {
    float[] origAngles = getJointRotations();
    setJointRotations(testAngles);
    
    float[] ret = getQuaternion();
    setJointRotations(origAngles);
    return ret;
  }
  
  /**
   * Gives the current position of the end effector in
   * Processing native coordinates.
   * @param model Arm model whose end effector position to calculate
   * @param test Determines whether to use arm segments' actual
   *             rotation values or if we're checking trial rotations
   * @return The current end effector position
   */
  public PVector getEEPos() {
    pushMatrix();
    resetMatrix();
    
    translate(600, 200, 0);
    translate(-50, -166, -358); // -115, -213, -413
    rotateZ(PI);
    translate(150, 0, 150);
    
    rotateY(getJointRotations()[0]);
    
    translate(-150, 0, -150);
    rotateZ(-PI);    
    translate(-115, -85, 180);
    rotateZ(PI);
    rotateY(PI/2);
    translate(0, 62, 62);
    
    rotateX(getJointRotations()[1]);
    
    translate(0, -62, -62);
    rotateY(-PI/2);
    rotateZ(-PI);   
    translate(0, -500, -50);
    rotateZ(PI);
    rotateY(PI/2);
    translate(0, 75, 75);
    
    rotateX(getJointRotations()[2]);
    
    translate(0, -75, -75);
    rotateY(PI/2);
    rotateZ(-PI);
    translate(745, -150, 150);
    rotateZ(PI/2);
    rotateY(PI/2);
    translate(70, 0, 70);
    
    rotateY(getJointRotations()[3]);
    
    translate(-70, 0, -70);
    rotateY(-PI/2);
    rotateZ(-PI/2);    
    translate(-115, 130, -124);
    rotateZ(PI);
    rotateY(-PI/2);
    translate(0, 50, 50);
    
    rotateX(getJointRotations()[4]);
    
    translate(0, -50, -50);
    rotateY(PI/2);
    rotateZ(-PI);    
    translate(150, -10, 95);
    rotateY(-PI/2);
    rotateZ(PI);
    translate(45, 45, 0);
    
    rotateZ(getJointRotations()[5]);
    
    if(curCoordFrame == CoordFrame.TOOL || curCoordFrame == CoordFrame.WORLD) { applyToolFrame(activeToolFrame); }
    
    PVector ret = new PVector(
    modelX(0, 0, 0),
    modelY(0, 0, 0),
    modelZ(0, 0, 0));
    
    popMatrix();
    return ret;
  } // end calculateEndEffectorPosition
  
  public PVector getEEPos(float[] testAngles) {
    float[] origAngles = getJointRotations();
    setJointRotations(testAngles);
    
    PVector ret = getEEPos();
    setJointRotations(origAngles);
    return ret;
    
  }
  
  //convenience method to set all joint rotation values of the robot arm
  public void setJointRotations(float[] rot) {
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
    
    if(COLLISION_DISPLAY) { updateBoxes(); }
  }//end set joint rotations
  
  public boolean interpolateRotation(float speed) {
    boolean done = true;
    for(Model a : segments) {
      for(int r = 0; r < 3; r++) {
        if(a.rotations[r]) {
          if(abs(a.currentRotations[r] - a.targetRotations[r]) > a.rotationSpeed*speed) {
            done = false;
            a.currentRotations[r] += a.rotationSpeed * a.rotationDirections[r] * speed;
            a.currentRotations[r] = clampAngle(a.currentRotations[r]);
          }
        }
      } // end loop through rotation axes
    } // end loop through arm segments
    if(COLLISION_DISPLAY) { updateBoxes(); }
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
    for(Model a : armModel.segments) {
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
  
  void updateOrientation() {
    PVector u = new PVector(0, 0, 0);
    float theta = DEG_TO_RAD*2.5*liveSpeed;
    
    u.x = jogRot[0];
    u.y = jogRot[1];
    u.z = jogRot[2];
    u.normalize();
    
    if(u.x != 0 || u.y != 0 || u.z != 0) {
      tgtRot = rotateQuat(tgtRot, u, theta);
    }
  }

  void executeLiveMotion() {
    if(curCoordFrame == CoordFrame.JOINT) {
      for(int i = 0; i < segments.size(); i += 1) {
        Model model = segments.get(i);
        
        for(int n = 0; n < 3; n++) {
          if(model.rotations[n]) {
            float trialAngle = model.currentRotations[n] +
            model.rotationSpeed * model.jointsMoving[n] * liveSpeed;
            trialAngle = clampAngle(trialAngle);
            
            if(model.anglePermitted(n, trialAngle)) {
              
              float old_angle = model.currentRotations[n];
              model.currentRotations[n] = trialAngle;
              if(COLLISION_DISPLAY) { updateBoxes(); }
              
              if(armModel.checkSelfCollisions()) {
                // end robot arm movement
                model.currentRotations[n] = old_angle;
                updateBoxes();
                model.jointsMoving[n] = 0;
              }
            } 
            else {
              model.jointsMoving[n] = 0;
            }
          }
        }
      }
      updateButtonColors();
    } else {
      //only move if our movement vector is non-zero
      if(jogLinear[0] != 0 || jogLinear[1] != 0 || jogLinear[2] != 0 || 
          jogRot[0] != 0 || jogRot[1] != 0 || jogRot[2] != 0) {
        
        PVector move = new PVector(jogLinear[0], jogLinear[1], jogLinear[2]);
        // Convert the movement vector into the current reference frame
        move = rotate(move, currentFrame);
        
        //respond to user defined movement
        float distance = motorSpeed/60.0 * liveSpeed;
        tgtPos.x += move.x * distance;
        tgtPos.y += move.y * distance;
        tgtPos.z += move.z * distance;
        updateOrientation();
        
        //if(DISPLAY_TEST_OUT_PUT) { System.out.printf("%s -> %s: %d\n", getEEPos(), tgtPos, getEEPos().dist(tgtPos)); }
        
        //println(lockOrientation);
        float[] destAngles = calculateIKJacobian(tgtPos, tgtRot);
        
        //did we successfully find the desired angles?
        if(destAngles == null) {
          println("IK failure");
          updateButtonColors();
          jogLinear[0] = 0;
          jogLinear[1] = 0;
          jogLinear[2] = 0;
          jogRot[0] = 0;
          jogRot[1] = 0;
          jogRot[2] = 0;
          return;
        }
        
        for(int i = 0; i < 6; i += 1) {
          Model s = armModel.segments.get(i);
          if(destAngles[i] > -0.000001 && destAngles[i] < 0.000001)
          destAngles[i] = 0;
          
          for(int j = 0; j < 3; j += 1) {
            if(s.rotations[j] && !s.anglePermitted(j, destAngles[i])) {
              //println("illegal joint angle on j" + i);
              updateButtonColors();
              jogLinear[0] = 0;
              jogLinear[1] = 0;
              jogLinear[2] = 0;
              jogRot[0] = 0;
              jogRot[1] = 0;
              jogRot[2] = 0;
              return;
            }
          }
        }
        
        float[] angleOffset = new float[6];
        float maxOffset = TWO_PI;
        for(int i = 0; i < 6; i += 1) {
          angleOffset[i] = abs(minimumDistance(destAngles[i], armModel.getJointRotations()[i]));
        }
        
        if(angleOffset[0] <= maxOffset && angleOffset[1] <= maxOffset && angleOffset[2] <= maxOffset && 
            angleOffset[3] <= maxOffset && angleOffset[4] <= maxOffset && angleOffset[5] <= maxOffset) {
          setJointRotations(destAngles);
        }
        else {
          tgtPos = armModel.getEEPos();
          tgtRot = armModel.getQuaternion();
        }
      }
    }
  } // end execute live motion
  
  public boolean checkAngles(float[] angles) {
    float[] oldAngles = new float[6];
    /* Save the original angles of the Robot and apply the new set of angles */
    for(int i = 0; i < segments.size(); i += 1) {
      for(int j = 0; j < 3; j += 1) {
        if(segments.get(i).rotations[j]) {
          oldAngles[i] = segments.get(i).currentRotations[j];
          segments.get(i).currentRotations[j] = angles[i];
        }
      }
    }
    
    updateBoxes();
    // Check a collision of the Robot with itself
    boolean collision = checkSelfCollisions();
    
    /* Check for a collision between the Robot Arm and any world object as well as an object
     * held by the Robot Arm and any other world object */
    for(WorldObject obj : objects) {
      if(checkObjectCollision(obj) || (held != null && held != obj && held.collision(obj))) {
        collision = true;
      }
    }
    
    if(collision) {
      // Reset the original position in the case of a collision
      setJointRotations(oldAngles);
    }
    
    return collision;
  }
  
  /**
   * Transitions from the current End Effector
   * to the next End Effector in a cyclic pattern:
   * 
   * NONE -> SUCTION -> CLAW -> NONE
   */
  public void swapEndEffector() {
    
    switch (activeEndEffector) {
      case NONE:
        activeEndEffector = EndEffector.SUCTION;
        break;
      
      case SUCTION:
        activeEndEffector = EndEffector.CLAW;
        break;
        
      case CLAW:
      default:
        activeEndEffector = EndEffector.NONE;
        break;
    }
    
    // Releases currently held object
    releaseHeldObject();
  }
  
  
  /* If an object is currently being held by the Robot arm, then release it */
  public void releaseHeldObject() {
    armModel.held = null;
  }
  
  /* Indicates that the Robot Arm is in Motion */
  public boolean modelInMotion() {
    for(Model m : segments) {
      for(int idx = 0; idx < m.jointsMoving.length; ++idx) {
        if(m.jointsMoving[idx] != 0) {
          return true;
        }
      }
    }
    
    return jogLinear[0] != 0 || jogLinear[1] != 0 || jogLinear[2] != 0 ||
    jogRot[0] != 0 || jogRot[1] != 0 || jogRot[2] != 0 || inMotion;
  }
  
} // end ArmModel class

void printCurrentModelCoordinates(String msg) {
  print(msg + ": " );
  print(modelX(0, 0, 0) + " ");
  print(modelY(0, 0, 0) + " ");
  print(modelZ(0, 0, 0));
  println();
}
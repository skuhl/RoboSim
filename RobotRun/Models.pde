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
  public int[] rotationDirections = new int[3]; // control rotation direction so we
                                                // don't "take the long way around"
  public float rotationSpeed;
  public float[] jointsMoving = new float[3]; // for live control using the joint buttons
  
  public Model(String filename, color col) {
    for (int n = 0; n < 3; n++) {
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
    
    while (n < data.length) {
      Triangle t = new Triangle();
      for (int m = 0; m < 4; m++) {
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
    for (Triangle t : triangles) {
      mesh.normal(t.components[0].x, t.components[0].y, t.components[0].z);
      mesh.vertex(t.components[1].x, t.components[1].y, t.components[1].z);
      mesh.vertex(t.components[2].x, t.components[2].y, t.components[2].z);
      mesh.vertex(t.components[3].x, t.components[3].y, t.components[3].z);
    }
    mesh.endShape();
  } // end loadSTLModel
    
  public boolean anglePermitted(int idx, float angle) {
    
    if (jointRanges[idx].x < jointRanges[idx].y) {
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

// The apporximate center of the base of the robot
public static final PVector base_center = new PVector(404, 137, -212);

public class ArmModel {
  
  public ArrayList<Model> segments = new ArrayList<Model>();
  public int type;
  //public boolean calculatingArms = false, movingArms = false;
  public float motorSpeed;
  // Indicates translational motion in the World Frame
  public float[] moveLinear = new float[3];
  // Indicates rotational motion in the World Frame
  public float[] moveOrientation = new float[3];
  public PVector lockPosition;
  public PVector lockOrientation;
  
  public Box[] bodyHitBoxes;
  private ArrayList<Box>[] eeHitBoxes;
  public Object held;
  public float[][] oldEETMatrix;
  
  public ArmModel() {
    
    motorSpeed = 4000.0; // speed in mm/sec
    // Joint 1
    Model base = new Model("ROBOT_MODEL_1_BASE.STL", color(200, 200, 0));
    base.rotations[1] = true;
    base.jointRanges[1] = new PVector(0, TWO_PI);
    base.rotationSpeed = radians(350)/60.0;
    // Joint 2
    Model axis1 = new Model("ROBOT_MODEL_1_AXIS1.STL", color(40, 40, 40));
    axis1.rotations[2] = true;
    axis1.jointRanges[2] = new PVector(4.34, 2.01);
    axis1.rotationSpeed = radians(350)/60.0;
    // Joint 3
    Model axis2 = new Model("ROBOT_MODEL_1_AXIS2.STL", color(200, 200, 0));
    axis2.rotations[2] = true;
    axis2.jointRanges[2] = new PVector(12f * PI / 20f, 8f * PI / 20f);
    axis2.rotationSpeed = radians(400)/60.0;
    // Joint 4
    Model axis3 = new Model("ROBOT_MODEL_1_AXIS3.STL", color(40, 40, 40));
    axis3.rotations[0] = true;
    axis3.jointRanges[0] = new PVector(0, TWO_PI);
    axis3.rotationSpeed = radians(450)/60.0;
    // Joint 5
    Model axis4 = new Model("ROBOT_MODEL_1_AXIS4.STL", color(40, 40, 40));
    axis4.rotations[2] = true;
    axis4.jointRanges[2] = new PVector(59f * PI / 40f, 11f * PI / 20f);
    axis4.rotationSpeed = radians(450)/60.0;
    // Joint 6
    Model axis5 = new Model("ROBOT_MODEL_1_AXIS5.STL", color(200, 200, 0));
    axis5.rotations[0] = true;
    axis5.jointRanges[0] = new PVector(0, TWO_PI);
    axis5.rotationSpeed = radians(720)/60.0;
    Model axis6 = new Model("ROBOT_MODEL_1_AXIS6.STL", color(40, 40, 40));
    segments.add(base);
    segments.add(axis1);
    segments.add(axis2);
    segments.add(axis3);
    segments.add(axis4);
    segments.add(axis5);
    segments.add(axis6);
    
    for (int idx = 0; idx < moveLinear.length; ++idx) {
      moveLinear[idx] = 0;
    }
    
    for (int idx = 0; idx < moveOrientation.length; ++idx) {
      moveOrientation[idx] = 0;
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
    applyModelRotation(this);
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
    if (activeEndEffector == ENDEF_SUCTION) {
      rotateY(PI);
      translate(-88, -37, 0);
      eeModelSuction.draw();
    } else if (activeEndEffector == ENDEF_CLAW) {
      rotateY(PI);
      translate(-88, 0, 0);
      eeModelClaw.draw();
      rotateZ(PI/2);
      if (endEffectorStatus == OFF) {
        translate(10, -85, 30);
        eeModelClawPincer.draw();
        translate(55, 0, 0);
        eeModelClawPincer.draw();
      } else if (endEffectorStatus == ON) {
        translate(28, -85, 30);
        eeModelClawPincer.draw();
        translate(20, 0, 0);
        eeModelClawPincer.draw();
      }
    }
    
    //updateBoxes();
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
    if (activeEndEffector == ENDEF_CLAW) {
        return (endEffectorStatus == ON) ? eeHitBoxes[1] : eeHitBoxes[2];
    } else if (activeEndEffector == ENDEF_SUCTION) {
      return eeHitBoxes[3];
    }
    
    return eeHitBoxes[0];
  }
  
  /* Determine if select pairs of hit boxes of the Robot Arm are colliding */
  public boolean checkSelfCollisions() {
    for (Box b : bodyHitBoxes) {
      b.outline = color(0, 255, 0);
    }
    
    ArrayList<Box> eeHB = currentEEHitBoxList();
    
    for (Box b : eeHB) {
      b.outline = color(0, 255, 0);
    }
    
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
    for (int idx = 0; idx < check_pairs.length - 1; idx += 2) {
      if ( collision3D(bodyHitBoxes[ check_pairs[idx] ], bodyHitBoxes[ check_pairs[idx + 1] ]) ) {
        bodyHitBoxes[ check_pairs[idx] ].outline = color(255, 0, 0);
        bodyHitBoxes[ check_pairs[idx + 1] ].outline = color(255, 0, 0);
        collision = true;
      }
    }
    
    // Check collisions between all EE hit boxes and base as well as the first long arm hit boxes
    for (Box hb : eeHB) {
      for (int idx = 0; idx < 4; ++idx) {
        if (collision3D(hb, bodyHitBoxes[idx]) ) {
          hb.outline = color(255, 0, 0);
          bodyHitBoxes[idx].outline = color(255, 0, 0);
          collision = true;
        }
      }
    }
    
    return collision;
  }
  
  /* Determine if the given ojbect is collding with any part of the Robot. */
  public boolean checkObjectCollision(Object obj) {
    Box ohb = (Box)obj.hit_box;
    boolean collision = false;
    
    for (Box b : bodyHitBoxes) {
      if ( collision3D(ohb, b) ) {
        
        ohb.outline = color(255, 0, 0);
        b.outline = color(255, 0, 0);
        collision = true;
      }
    }
    
    ArrayList<Box> eeHBs = currentEEHitBoxList();
    
    for (Box b : eeHBs) {
      // Special case for held objects
      if ( (activeEndEffector != ENDEF_CLAW || activeEndEffector != ENDEF_SUCTION || endEffectorStatus != ON || b != eeHitBoxes[1].get(1) || obj != armModel.held) && collision3D(ohb, b) ) {
        
        ohb.outline = color(255, 0, 0);
        b.outline = color(255, 0, 0);
        collision = true;
      }
    }
    
    return collision;
  }
  
  /* Draws the Robot Arm's hit boxes in the world */
  public void drawBoxes() {
    // Draw hit boxes of the body poriotn of the Robot Arm
    for (Box b : bodyHitBoxes) {
      pushMatrix();
      b.applyTransform();
      b.draw();
      popMatrix();
    }
    
    int eeIdx = 0;
    // Determine which set of hit boxes to display based on the active End Effector
    if (activeEndEffector == ENDEF_CLAW) {
      if (endEffectorStatus == ON) {
        eeIdx = 1;
      } else {
        eeIdx = 2;
      }
    } else if (activeEndEffector == ENDEF_SUCTION) {
      eeIdx = 3;
    }
    // Draw End Effector hit boxes
    for (Box b : eeHitBoxes[eeIdx]) {
      pushMatrix();
      b.applyTransform();
      b.draw();
      popMatrix();
    }
  }
  
  //returns the rotational values for each arm joint
  public float[] getJointRotations() {
    float[] rot = new float[6];
    for (int i = 0; i < segments.size(); i += 1) {
      for (int j = 0; j < 3; j += 1) {
        if (segments.get(i).rotations[j]) {
          rot[i] = segments.get(i).currentRotations[j];
          break;
        }
      }
    }
    return rot;
  }//end get joint rotations
  
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
    float theta1, theta2, psi1, psi2, phi1, phi2;
    PVector wpr, wpr2;
    
    float[][] r = calculateRotationMatrix();
    
    if(r[2][0] != 1 && r[2][0] != -1){
      //rotation about y-axis
      theta1 = -asin(r[2][0]);
      theta2 = PI - theta1;
      //rotation about x-axis
      psi1 = atan2(r[2][1]/cos(theta1), r[2][2]/cos(theta1));
      psi2 = atan2(r[2][1]/cos(theta2), r[2][2]/cos(theta2));
      //rotation about z-axis
      phi1 = atan2(r[1][0]/cos(theta1), r[0][0]/cos(theta1));
      phi2 = atan2(r[1][0]/cos(theta2), r[0][0]/cos(theta2));
    }
    else{
      phi1 = phi2 = 0;
      if(r[2][0] == -1){
        theta1 = theta2 = PI/2;
        psi1 = psi2 = phi1 + atan2(r[0][1], r[0][2]);
      }
      else{
        theta1 = theta2 = -PI/2;
        psi1 = psi2 = -phi1 + atan2(-r[0][1], -r[0][2]);
      }
    }
    
    wpr = new PVector(psi1, theta1, phi1);
    wpr2 = new PVector(psi2, theta2, phi2);
    
    //println("rotation vectors: ");
    //println(wpr);
    //println(wpr2);
    //println();
    
    return wpr;
  }
  
  public PVector getWPR(float[] testAngles){
    float[] origAngles = getJointRotations();
    setJointRotations(testAngles);
    
    PVector ret = getWPR();
    setJointRotations(origAngles);
    return ret;
  }
  
  //returns the rotational value of the robot as a quaternion
  public float[] getQuaternion(){
    float q[] = new float[4];
    float r[][] = calculateRotationMatrix();
    //our Euler vector will be the 'x' axis of the robotic arm end
    //effector reference frame; this is the direction the EE is "facing"
    PVector e = new PVector(r[0][0], r[0][1], r[0][2]);
    //this is the roll value for the EE, the rotation about 'e' that the
    //EE is currently experiencing
    float sigma = atan2(r[2][1], r[2][2]);
    
    println("vector: " + e + ", rotation:" + sigma*57.2958);
    
    q[0] = cos(sigma/2);
    q[1] = e.x*sin(sigma/2);
    q[2] = e.y*sin(sigma/2);
    q[3] = e.z*sin(sigma/2);
    
    println("quat: " + q[0] + ", " + q[1] + ", " + q[2] + ", " + q[3]);
    println();
    return q;
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
    
    if (activeToolFrame >= 0 && activeToolFrame < toolFrames.length) {
      PVector tr = toolFrames[activeToolFrame].getOrigin();
      translate(tr.x, tr.y, tr.z);
    }
    PVector ret = new PVector(
      modelX(0, 0, 0),
      modelY(0, 0, 0),
      modelZ(0, 0, 0));
    
    popMatrix();
    return ret;
  } // end calculateEndEffectorPosition
  
  public PVector getEEPos(float[] testAngles){
    float[] origAngles = getJointRotations();
    setJointRotations(testAngles);
    
    PVector ret = getEEPos();
    setJointRotations(origAngles);
    return ret;
    
  }
  
 //convenience method to set all joint rotation values of the robot arm
 public void setJointRotations(float[] rot){
   for(int i = 0; i < segments.size(); i += 1) {
     for(int j = 0; j < 3; j += 1) {
       if(segments.get(i).rotations[j]) {
         segments.get(i).currentRotations[j] = rot[i];
         segments.get(i).currentRotations[j] %= TWO_PI;
         if(segments.get(i).currentRotations[j] < 0){
           segments.get(i).currentRotations[j] += TWO_PI;
         }
       }
     }
   }
   
   updateBoxes();
 }//end set joint rotations
  
  public boolean interpolateRotation(float speed) {
    boolean done = true;
    for (Model a : segments){
      for (int r = 0; r < 3; r++){
        if (a.rotations[r]){
          if (abs(a.currentRotations[r] - a.targetRotations[r]) > a.rotationSpeed*speed){
            done = false;
            a.currentRotations[r] += a.rotationSpeed * a.rotationDirections[r] * speed;
            a.currentRotations[r] = clampAngle(a.currentRotations[r]);
          }
        }
      } // end loop through rotation axes
    } // end loop through arm segments
    
    updateBoxes();
    return done;
  } // end interpolate rotation

  void executeLiveMotion() {    
    if (curCoordFrame == COORD_JOINT) {
      for (int i = 0; i < segments.size(); i += 1) {
        Model model = segments.get(i);
        
        for (int n = 0; n < 3; n++) {
          if (model.rotations[n]) {
            float trialAngle = model.currentRotations[n] +
              model.rotationSpeed * model.jointsMoving[n] * liveSpeed;
              trialAngle = clampAngle(trialAngle);
            
            if (model.anglePermitted(n, trialAngle)) {
              
              //float old_angle = model.currentRotations[n];
              model.currentRotations[n] = trialAngle;
              updateBoxes();
              
              /*if (armModel.checkSelfCollisions()) {
                // end robot arm movement
                model.currentRotations[n] = old_angle;
                updateBoxes();
                model.jointsMoving[n] = 0;
              }*/
            } 
            else {
              model.jointsMoving[n] = 0;
            }
          }
        }
      }
      updateButtonColors();
    } else if (curCoordFrame == COORD_WORLD) {
      //only move if our movement vector is non-zero
      if (moveLinear[0] != 0 || moveLinear[1] != 0 || moveLinear[2] != 0
          || moveOrientation[0] != 0 || moveOrientation[1] != 0 || moveOrientation[2] != 0) {
        PVector move = new PVector(moveLinear[0], moveLinear[1], moveLinear[2]);
        //convert to user frame coordinates if currently in a user frame
        if (activeUserFrame >= 0 && activeUserFrame < userFrames.length) {
          PVector[] frame = userFrames[activeUserFrame].axes;
          move.y = -move.y;
          move.z = -move.z;
          move = vectorConvertTo(move, frame[0], frame[1], frame[2]);
        }
        
        float distance = motorSpeed/60.0 * liveSpeed;
        lockPosition.x += move.x * distance;
        lockPosition.y += move.y * distance;
        lockPosition.z += move.z * distance;
        lockOrientation.x += moveOrientation[0] * DEG_TO_RAD;
        lockOrientation.z += moveOrientation[2] * DEG_TO_RAD;
        lockOrientation.x -= lockOrientation.x > PI ? TWO_PI : 0;
        lockOrientation.z -= lockOrientation.z > PI ? TWO_PI : 0;
        lockOrientation.x += lockOrientation.x < -PI ? TWO_PI : 0;
        lockOrientation.z += lockOrientation.z < -PI ? TWO_PI : 0;
        //keep y orientation within [-PI, PI]
        if(lockOrientation.y <= PI - DEG_TO_RAD && lockOrientation.y >= -PI + DEG_TO_RAD){
          lockOrientation.y += moveOrientation[2] * DEG_TO_RAD;
        }
        else if(true){
          
        }
        
        
        //println(lockOrientation);
        int r = calculateIKJacobian(lockPosition, lockOrientation);
        if(r == EXEC_FAILURE){
          updateButtonColors();
          moveLinear[0] = 0;
          moveLinear[1] = 0;
          moveLinear[2] = 0;
          moveOrientation[0] = 0;
          moveOrientation[1] = 0;
          moveOrientation[2] = 0;
        }
      }
    }
  } // end execute live motion
  
  public boolean checkAngles(float[] angles) {
    float[] oldAngles = new float[6];
    /* Save the original angles of the Robot and apply the new set of angles */
    for(int i = 0; i < segments.size(); i += 1) {
      for(int j = 0; j < 3; j += 1) {
        if (segments.get(i).rotations[j]) {
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
    for (Object obj : objects) {
      if (checkObjectCollision(obj) || (held != null && held != obj && held.collision(obj))) {
        collision = true;
      }
    }
    
    if (collision) {
      // Reset the original position in the case of a collision
      setJointRotations(oldAngles);
    }
    
    return collision;
  }
  
  /* If an object is currently being held by the Robot arm, then release it */
  public void releaseHeldObject() {
    armModel.held = null;
  }
  
  /* Indicates that the Robot Arm is in Motion */
  public boolean modelInMotion() {
    for (Model m : segments) {
      for (int idx = 0; idx < m.jointsMoving.length; ++idx) {
        if (m.jointsMoving[idx] != 0) {
          return true;
        }
      }
    }
    
    return moveLinear[0] != 0 || moveLinear[1] != 0 || moveLinear[2] != 0 ||
           moveOrientation[0] != 0 || moveOrientation[1] != 0 || moveOrientation[2] != 0;
  }
  
} // end ArmModel class

void printCurrentModelCoordinates(String msg) {
  print(msg + ": " );
  print(modelX(0, 0, 0) + " ");
  print(modelY(0, 0, 0) + " ");
  print(modelZ(0, 0, 0));
  println();
}
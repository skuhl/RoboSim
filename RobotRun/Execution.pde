ArrayList<Point> intermediatePositions;
int motionFrameCounter = 0;
float distanceBetweenPoints = 5.0;
int interMotionIdx = -1;

final int COORD_JOINT = 0, COORD_WORLD = 1, COORD_TOOL = 2, COORD_USER = 3;
int curCoordFrame = COORD_JOINT;
float liveSpeed = 0.1;
boolean executingInstruction = false;

int errorCounter;
String errorText;
public static final boolean COLLISION_DISPLAY = false;

/**
 * Creates some programs for testing purposes.
 */
void createTestProgram() {
  Program program = new Program("Test Program");
  MotionInstruction instruction =
    new MotionInstruction(MTYPE_LINEAR, 0, true, 800, 1.0); //1.0
  program.addInstruction(instruction);
  instruction = new MotionInstruction(MTYPE_CIRCULAR, 1, true, 1600, 0.75); //0.75
  program.addInstruction(instruction);
  instruction = new MotionInstruction(MTYPE_LINEAR, 2, true, 400, 0.5); //0.5
  program.addInstruction(instruction);
  instruction = new MotionInstruction(MTYPE_JOINT, 3, true, 1.0, 0);
  program.addInstruction(instruction);
  instruction = new MotionInstruction(MTYPE_JOINT, 4, true, 1.0, 0);
  program.addInstruction(instruction);
  //for (int n = 0; n < 15; n++) program.addInstruction(
  //  new MotionInstruction(MTYPE_JOINT, 1, true, 0.5, 0));
  pr[0] = new Point(165, 116, -5, 0, 0, 0, 0, 0, 0, 0, 0, 0);
  pr[1] = new Point(166, -355, 120, 0, 0, 0, 0, 0, 0, 0, 0, 0);
  pr[2] = new Point(171, -113, 445, 0, 0, 0, 0, 0, 0, 0, 0, 0);
  pr[3] = new Point(725, 225, 50, 0, 0, 0, 5.6, 1.12, 5.46, 0, 5.6, 0);
  pr[4] = new Point(775, 300, 50, 0, 0, 0, 0, 0, 0, 0, 0, 0);
  pr[5] = new Point(-474, -218, 37, 0, 0, 0, 0, 0, 0, 0, 0, 0);
  pr[6] = new Point(-659, -412, -454, 0, 0, 0, 0, 0, 0, 0, 0, 0);
  programs.add(program);
  //currentProgram = program;
  
  Program program2 = new Program("Test Program 2");
  MotionInstruction instruction2 =
    new MotionInstruction(MTYPE_JOINT, 3, true, 1.0, 0);
  program2.addInstruction(instruction2);
  instruction2 = new MotionInstruction(MTYPE_JOINT, 4, true, 1.0, 0);
  program2.addInstruction(instruction2);
  programs.add(program2);
  currentProgram = program2;
  
  Program program3 = new Program("Circular Test");
  MotionInstruction instruction3 =
    new MotionInstruction(MTYPE_LINEAR, 0, true, 1.0, 0);
  program3.addInstruction(instruction3);
  instruction3 = new MotionInstruction(MTYPE_CIRCULAR, 1, true, 1.0, 0);
  program3.addInstruction(instruction3);
  instruction3 = new MotionInstruction(MTYPE_LINEAR, 2, true, 1.0, 0);
  program3.addInstruction(instruction3);
  instruction3 = new MotionInstruction(MTYPE_LINEAR, 3, true, 0.25, 0);
  program3.addInstruction(instruction3);
  programs.add(program3);
  //currentProgram = program3;
  
  Program program4 = new Program("New Arm Test");
  MotionInstruction instruction4 =
    new MotionInstruction(MTYPE_LINEAR, 5, true, 1.0, 0);
  program4.addInstruction(instruction4);
  instruction4 = new MotionInstruction(MTYPE_LINEAR, 6, true, 1.0, 0);
  program4.addInstruction(instruction4);
  programs.add(program4);
  currentProgram = program4;
  
  for (int n = 0; n < 22; n++) {
     programs.add(new Program("Xtra" + Integer.toString(n)));
     
  }
  saveState();
} // end createTestProgram()


/**
 * Displays important information in the upper-right corner of the screen.
 */
void showMainDisplayText() {
  fill(0);
  textAlign(RIGHT, TOP);
  String coordFrame = "CoordinateFrame: ";
  
  switch(curCoordFrame) {
    case COORD_JOINT:
      coordFrame += "Joint";
      break;
    case COORD_WORLD:
      coordFrame += "World";
      break;
    case COORD_TOOL:
      coordFrame += "Tool";
      break;
    case COORD_USER:
      coordFrame += "User";
  }
  
  text(coordFrame, width-20, 20);
  text("Speed: " + (Integer.toString((int)(Math.round(liveSpeed*100)))) + "%", width-20, 40);
  
  // Display the Current position and orientation of the Robot in the World Frame
  PVector ee_pos = armModel.getEEPos();
  //ee_pos = convertNativeToWorld(ee_pos);
  PVector wpr = armModel.getWPR();
  String dis_world = String.format("Coord  X: %8.4f  Y: %8.4f  Z: %8.4f  W: %8.4f  P: %8.4f  R: %8.4f", 
                     ee_pos.x, ee_pos.y, ee_pos.z, wpr.x*RAD_TO_DEG, wpr.y*RAD_TO_DEG, wpr.z*RAD_TO_DEG);
  
  // Display the Robot's joint angles
  float j[] = armModel.getJointRotations();
  String dis_joint = String.format("Joints  J1: %5.4f J2: %5.4f J3: %5.4f J4: %5.4f J5: %5.4f J6: %5.4f", 
                     j[0], j[1], j[2], j[3], j[4], j[5]);
  
  // Display the distance between the End Effector and the Based of the Robot
  float dist_base = PVector.dist(ee_pos, base_center);
  String dis_dist = String.format("Distance from EE to Robot Base: %f", dist_base);
  
  // Show the coordinates of the End Effector for the current Coordinate Frame
  if (curCoordFrame == COORD_JOINT) {          
    text(dis_joint, width - 20, 60);
    
    if (DISPLAY_TEST_OUTPUT) {
      text(dis_dist, width - 20, 80);
      text(dis_world, width - 20, 100);
    }
  } else {
    text(dis_world, width - 20, 60);
    
    if (DISPLAY_TEST_OUTPUT) {
      text(dis_dist, width - 20, 80);
      text(dis_joint, width - 20, 100);
    }
  }
  
  // Display a message while the robot is carrying an object
  if (armModel.held != null) {
    fill(200, 0, 0);
    text("Object held", width - 20, 120);
  }
  
  textAlign(LEFT);
  
  // Display message for camera pan-lock mode
  if (clickPan % 2 == 1) {
    textSize(14);
    fill(215, 0, 0);
    text("Press space on the keyboard to disable camera paning", 20, height / 2 + 30);
  }
  // Display message for camera rotation-lock mode
  if (clickRotate % 2 == 1) {
    textSize(14);
    fill(215, 0, 0);
    text("Press shift on the keyboard to disable camera rotation", 20, height / 2 + 55);
  }
  
  if (DISPLAY_TEST_OUTPUT) {
    textSize(12);
    fill(0, 0, 0);
    
    float[][] vectorMatrix = armModel.getRotationMatrix(armModel.currentFrame);
    String row = String.format("[  %f  %f  %f  ]", vectorMatrix[0][0], vectorMatrix[0][1], vectorMatrix[0][2]);
    text(row, 20, height / 2 + 80);
    row = String.format("[  %f  %f  %f  ]", vectorMatrix[1][0], vectorMatrix[1][1], vectorMatrix[1][2]);
    text(row, 20, height / 2 + 94);
    row = String.format("[  %f  %f  %f  ]", vectorMatrix[2][0], vectorMatrix[2][1], vectorMatrix[2][2]);
    text(row, 20, height / 2 + 108);
  }
  
  float[] q = armModel.getQuaternion();
  String quat = String.format("q: [%8.6f, %8.6f, %8.6f, %8.6f]", q[0], q[1], q[2], q[3]);
  text(quat, 20, height/2 + 148);
  
  if (ref_point != null) {
    String obj1_c = String.format("Ref Point: [%f, %f Z, %f]", ref_point.x, ref_point.y, ref_point.z);
    text(obj1_c, 20, height / 2 + 168);
  }
  textAlign(RIGHT);
  
  if (errorCounter > 0) {
    errorCounter--;
    fill(255, 0, 0);
    text(errorText, width-20, 100);
  }
}

/* Updates the curCoordFrame variable based on the current value of curCoordFrame, activeToolFrame,
 * activeUserFrame, and the current End Effector. If the new coordinate frame is user or tool and
 * no current tool or user frames are active, then the next coordinate frame is selected instead.
 *
 * @param model  The Robot Arm, for which to switch coordinate frames
 */
public void updateCoordinateMode(ArmModel model) {
  // Increment the current coordinate frame
  curCoordFrame = (curCoordFrame + 1) % 4;
  
  // Skip the tool frame, if there is no current active tool frame
  if (curCoordFrame == COORD_TOOL && !((activeToolFrame >= 0 && activeToolFrame < toolFrames.length)
                                  || model.activeEndEffector == ENDEF_SUCTION
                                  || model.activeEndEffector == ENDEF_CLAW)) {
    curCoordFrame = COORD_USER;
  }
  
  // Skip the user frame, if there is no current active user frame
  if (curCoordFrame == COORD_USER && !(activeUserFrame >= 0 && activeUserFrame < userFrames.length)) {
    curCoordFrame = COORD_JOINT;
  }
    
  // Update the Arm Model's rotation matrix for rotational motion based on the current frame
  if (curCoordFrame == COORD_TOOL || (curCoordFrame == COORD_WORLD && activeToolFrame != -1)) {
    // Active Tool Frames are used in the World Frame as well
    armModel.currentFrame = toolFrames[activeToolFrame].getAxes();
  } else if (curCoordFrame == COORD_USER) {
    
    armModel.currentFrame = userFrames[activeUserFrame].getAxes();
  } else {
    // Reset to the identity matrix
    armModel.currentFrame = new float[3][3];
    
    for (int diag = 0; diag < 3; ++diag) {
      armModel.currentFrame[diag][diag] = 1;
    }
  }
}

/**
 * Converts from RobotRun-defined world coordinates into
 * Processing's coordinate system.
 * Assumes that the robot starts out facing toward the LEFT.
 */
PVector convertWorldToNative(PVector in) {
  pushMatrix();
  float outx = modelX(0,0,0)-in.x;
  float outy = modelY(0,0,0)-in.z;
  float outz = -(modelZ(0,0,0)-in.y);
  popMatrix();
  return new PVector(outx, outy, outz);
}

/**
 * Converts from Processing's native coordinate system to
 * RobotRun-defined world coordinates.
 */
PVector convertNativeToWorld(PVector in) {
  pushMatrix();
  float outx = modelX(0,0,0)-in.x;
  float outy = in.z+modelZ(0,0,0);
  float outz = modelY(0,0,0)-in.y;
  popMatrix();
  return new PVector(outx, outy, outz);
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
PVector computePerpendicular(PVector in, PVector second) {
  PVector[] plane = createPlaneFrom3Points(in, second, new PVector(in.x*2, in.y*2, in.z*2));
  PVector v1 = vectorConvertTo(in, plane[0], plane[1], plane[2]);
  PVector v2 = vectorConvertTo(second, plane[0], plane[1], plane[2]);
  PVector perp1 = new PVector(v1.y, -v1.x, v1.z);
  PVector perp2 = new PVector(-v1.y, v1.x, v1.z);
  PVector orig = new PVector(v2.x*5, v2.y*5, v2.z);
  PVector p1 = new PVector(perp1.x*5, perp1.y*5, perp1.z);
  PVector p2 = new PVector(perp2.x*5, perp2.y*5, perp2.z);
  if (dist(orig.x, orig.y, orig.z, p1.x, p1.y, p1.z) <
      dist(orig.x, orig.y, orig.z, p2.x, p2.y, p2.z))
    return vectorConvertFrom(perp1, plane[0], plane[1], plane[2]);
  else return vectorConvertFrom(perp2, plane[0], plane[1], plane[2]);
}

/* This method will draw the End Effector grid mapping based on the value of EE_MAPPING:
 *
 *  0 -> a line is drawn between the EE and the grid plane
 *  1 -> a point is drawn on the grid plane that corresponds to the EE's xz coordinates
 *  For any other value, nothing is drawn
 */
public void drawEndEffectorGridMapping() {
  
  PVector ee_pos = armModel.getEEPos();
  
  // Change color of the EE mapping based on if it lies below or above the ground plane
  color c = (ee_pos.y <= PLANE_Z) ? color(255, 0, 0) : color(150, 0, 255);
  
  // Toggle EE mapping type with 'e'
  switch (EE_MAPPING) {
    case 0:
      stroke(c);
      // Draw a line, from the EE to the grid in the xy plane, parallel to the z plane
      line(ee_pos.x, ee_pos.y, ee_pos.z, ee_pos.x, PLANE_Z, ee_pos.z);
      break;
    
    case 1:
      noStroke();
      fill(c);
      // Draw a point, which maps the EE's position to the grid in the xy plane
      pushMatrix();
      rotateX(PI / 2);
      translate(0, 0, -PLANE_Z);
      ellipse(ee_pos.x, ee_pos.z, 10, 10);
      popMatrix();
      break;
      
    default:
      // No EE grid mapping
  }
}



/**
 * Performs rotations and translations to reach the end effector
 * position, similarly to calculateEndEffectorPosition(), but
 * this function doesn't return anything and also doesn't pop
 * the matrix after performing the transformations. Useful when
 * you're doing some graphical manipulation and you want to use
 * the end effector position as your start point.
 * 
 * @param model        The arm model whose transformations to apply
 * @param applyOffset  Whether to apply the Tool Frame End
 *                     Effector offset (if it exists)
 */
void applyModelRotation(ArmModel model, boolean applyOffset){   
  translate(600, 200, 0);
  translate(-50, -166, -358); // -115, -213, -413
  rotateZ(PI);
  translate(150, 0, 150);
  rotateY(model.segments.get(0).currentRotations[1]);
  translate(-150, 0, -150);
  rotateZ(-PI);    
  translate(-115, -85, 180);
  rotateZ(PI);
  rotateY(PI/2);
  translate(0, 62, 62);
  rotateX(model.segments.get(1).currentRotations[2]);
  translate(0, -62, -62);
  rotateY(-PI/2);
  rotateZ(-PI);   
  translate(0, -500, -50);
  rotateZ(PI);
  rotateY(PI/2);
  translate(0, 75, 75);
  rotateX(model.segments.get(2).currentRotations[2]);
  translate(0, -75, -75);
  rotateY(PI/2);
  rotateZ(-PI);
  translate(745, -150, 150);
  rotateZ(PI/2);
  rotateY(PI/2);
  translate(70, 0, 70);
  rotateY(model.segments.get(3).currentRotations[0]);
  translate(-70, 0, -70);
  rotateY(-PI/2);
  rotateZ(-PI/2);    
  translate(-115, 130, -124);
  rotateZ(PI);
  rotateY(-PI/2);
  translate(0, 50, 50);
  rotateX(model.segments.get(4).currentRotations[2]);
  translate(0, -50, -50);
  rotateY(PI/2);
  rotateZ(-PI);    
  translate(150, -10, 95);
  rotateY(-PI/2);
  rotateZ(PI);
  translate(45, 45, 0);
  rotateZ(model.segments.get(5).currentRotations[0]);
  
  if (applyOffset && (curCoordFrame == COORD_TOOL || curCoordFrame == COORD_WORLD)) { armModel.applyToolFrame(activeToolFrame); }
} // end apply model rotations

/**
 * Calculate the Jacobian matrix for the robotic arm for
 * a given set of joint rotational values using a 1 DEGREE
 * offset for each joint rotation value. Each cell of the
 * resulting matrix will describe the linear approximation
 * of the robot's motion for each joint in units per radian. 
 */
public float[][] calculateJacobian(float[] angles){
  float dAngle = DEG_TO_RAD;
  float[][] J = new float[7][6];
  //get current ee position
  PVector cPos = armModel.getEEPos(angles);
  float[] cRotQ = armModel.getQuaternion(angles);
  
  //examine each segment of the arm
  for(int i = 0; i < 6; i += 1){
    //test angular offset
    angles[i] += dAngle;
    //get updated ee position
    PVector nPos = armModel.getEEPos(angles);
    float[] nRotQ = armModel.getQuaternion(angles);

    //get translational delta
    J[0][i] = (nPos.x - cPos.x)/DEG_TO_RAD;
    J[1][i] = (nPos.y - cPos.y)/DEG_TO_RAD;
    J[2][i] = (nPos.z - cPos.z)/DEG_TO_RAD;
    //get rotational delta        
    J[3][i] = (nRotQ[0] - cRotQ[0])/DEG_TO_RAD;
    J[4][i] = (nRotQ[1] - cRotQ[1])/DEG_TO_RAD;
    J[5][i] = (nRotQ[2] - cRotQ[2])/DEG_TO_RAD;
    J[6][i] = (nRotQ[3] - cRotQ[3])/DEG_TO_RAD;
    //replace the original rotational value
    angles[i] -= dAngle;
  }
  
  return J;
}//end calculate jacobian

//attempts to calculate the joint rotation values
//required to move the end effector to the point specified
//by 'tgt' and the Euler angle orientation 'rot'
int calculateIKJacobian(PVector tgt, float[] rot){
  final int limit = 1000;  //max number of times to loop
  float[] angles = armModel.getJointRotations();
  float[][] frame = armModel.currentFrame;
  float[][] nFrame = armModel.getRotationMatrix();
  float[][] rMatrix = quatToMatrix(rot);
  armModel.currentFrame = nFrame;
  //translate target rotation to world ref frame
  RealMatrix M = new Array2DRowRealMatrix(floatToDouble(nFrame, 3, 3));
  RealMatrix O = new Array2DRowRealMatrix(floatToDouble(frame, 3, 3));
  RealMatrix MO = M.multiply(MatrixUtils.inverse(O));
  //translate target rotation to EE ref frame
  RealMatrix R = new Array2DRowRealMatrix(floatToDouble(rMatrix, 3, 3));
  RealMatrix OR = R.multiply(MatrixUtils.inverse(MO));
  rot = matrixToQuat(doubleToFloat(OR.getData(), 3, 3));
  
  int count = 0;
  
  while(count < limit){
    PVector cPos = armModel.getEEPos(angles);
    float[] cRotQ = armModel.getQuaternion(angles);
    
    //calculate our translational offset from target
    float[] tDelta = calculateVectorDelta(tgt, cPos);
    //calculate our rotational offset from target
    float[] rDelta = calculateVectorDelta(rot, cRotQ, 4);
    float[] delta = new float[7];
    
    delta[0] = tDelta[0];
    delta[1] = tDelta[1];
    delta[2] = tDelta[2];
    delta[3] = rDelta[0];
    delta[4] = rDelta[1];
    delta[5] = rDelta[2];
    delta[6] = rDelta[3];
    
    float dist = PVector.dist(cPos, tgt);
    float rDist = calculateQuatMag(rDelta);
                                                  
    //check whether our current position is within tolerance
    if(dist < liveSpeed && rDist < 0.005*liveSpeed) break;
    //calculate jacobian, 'J', and its inverse 
    float[][] J = calculateJacobian(angles);
    RealMatrix m = new Array2DRowRealMatrix(floatToDouble(J, 7, 6));
    //RealMatrix JInverse = MatrixUtils.inverse(m);
    RealMatrix JInverse = new SingularValueDecomposition(m).getSolver().getInverse();
        
    //calculate and apply joint angular changes
    float[] dAngle = {0, 0, 0, 0, 0, 0};
    for(int i = 0; i < 6; i += 1){
      for(int j = 0; j < 7; j += 1){
        dAngle[i] += JInverse.getEntry(i, j)*delta[j];
      }
      //update joint angles
      angles[i] += dAngle[i];
      angles[i] += TWO_PI;
      angles[i] %= TWO_PI;
    }
    
    count += 1;
  }
  
  armModel.currentFrame = frame;
  
  //did we successfully find the desired angles?
  if(count >= limit){
    println("IK failure");
    return EXEC_FAILURE;
  }
  else{
    for(int i = 0; i < 6; i += 1){
      Model s = armModel.segments.get(i);
      if(angles[i] > -0.000001 && angles[i] < 0.000001)
        angles[i] = 0;
        
      for(int j = 0; j < 3; j += 1){
        if(s.rotations[j] && !s.anglePermitted(j, angles[i])){
          //println("illegal joint angle on j" + i);
          //return EXEC_FAILURE;
        }
      }
    }
    
    armModel.setJointRotations(angles);
    return EXEC_SUCCESS;
  }
}

int calculateIKJacobian(Point p){
  PVector pos = p.pos;
  float[] rot = p.ori;
  return calculateIKJacobian(pos, rot);
}

/**
 * Determine how close together intermediate points between two points
 * need to be based on current speed
 */
void calculateDistanceBetweenPoints() {
  MotionInstruction instruction = getActiveMotionInstruct();
  if (instruction != null && instruction.getMotionType() != MTYPE_JOINT)
    distanceBetweenPoints = instruction.getSpeed() / 60.0;
  else if (curCoordFrame != COORD_JOINT)
    distanceBetweenPoints = armModel.motorSpeed * liveSpeed / 60.0;
  else distanceBetweenPoints = 5.0;
}

/**
 * Calculate a "path" (series of intermediate positions) between two
 * points in a straight line.
 * @param start Start point
 * @param end Destination point
 */
void calculateIntermediatePositions(Point start, Point end) {
  calculateDistanceBetweenPoints();
  intermediatePositions.clear();
  
  PVector p1 = start.pos;
  PVector p2 = end.pos;
  float[] q1 = start.ori;
  float[] q2 = end.ori;
  float[] qi = new float[4];
  
  float mu = 0;
  int numberOfPoints = (int)(dist(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z) / distanceBetweenPoints);
  float increment = 1.0 / (float)numberOfPoints;
  for (int n = 0; n < numberOfPoints; n++) {
    mu += increment;
    
    qi = quaternionSlerp(q1, q2, mu - increment);
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
void calculateContinuousPositions(Point start, Point end, Point next, float percentage) {
  //percentage /= 2;
  calculateDistanceBetweenPoints();
  percentage /= 1.5;
  percentage = 1 - percentage;
  percentage = constrain(percentage, 0, 1);
  intermediatePositions.clear();
  
  PVector p1 = start.pos;
  PVector p2 = end.pos;
  PVector p3 = next.pos;
  
  ArrayList<PVector> secondaryTargets = new ArrayList<PVector>();
  float mu = 0;
  int numberOfPoints = 0;
  if (dist(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z) >
      dist(p2.x, p2.y, p2.z, p3.x, p3.y, p3.z))
  {
    numberOfPoints = (int)
      (dist(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z) / distanceBetweenPoints);
  } else {
    numberOfPoints = (int)
      (dist(p2.x, p2.y, p2.z, p3.x, p3.y, p3.z) / distanceBetweenPoints);
  }
  float increment = 1.0 / (float)numberOfPoints;
  for (int n = 0; n < numberOfPoints; n++) {
    mu += increment;
    secondaryTargets.add(new PVector(
      p2.x * (1 - mu) + (p3.x * mu),
      p2.y * (1 - mu) + (p3.y * mu),
      p2.z * (1 - mu) + (p3.z * mu)));
  }
  mu = 0;
  int transitionPoint = (int)((float)numberOfPoints * percentage);
  for (int n = 0; n < transitionPoint; n++) {
    mu += increment;
    intermediatePositions.add(new Point(new PVector(
      p1.x * (1 - mu) + (p2.x * mu),
      p1.y * (1 - mu) + (p2.y * mu),
      p1.z * (1 - mu) + (p2.z * mu)),
      armModel.getWPR()));
  }
  int secondaryIdx = 0; // accessor for secondary targets
  mu = 0;
  increment /= 2.0;
  
  Point currentPoint;
  if(intermediatePositions.size() > 0){
    currentPoint = intermediatePositions.get(intermediatePositions.size()-1);
  }
  else{
    currentPoint = new Point(armModel.getEEPos(), armModel.getWPR());
  }
  
  for (int n = transitionPoint; n < numberOfPoints; n++) {
    mu += increment;
    intermediatePositions.add(new Point(new PVector(
      currentPoint.pos.x * (1 - mu) + (secondaryTargets.get(secondaryIdx).x * mu),
      currentPoint.pos.y * (1 - mu) + (secondaryTargets.get(secondaryIdx).y * mu),
      currentPoint.pos.z * (1 - mu) + (secondaryTargets.get(secondaryIdx).z * mu)), 
      armModel.getWPR()));
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
void calculateArc(Point start, Point inter, Point end){  
  calculateDistanceBetweenPoints();
  intermediatePositions.clear();
  
  PVector a = start.pos;
  PVector b = inter.pos;
  PVector c = end.pos;
  float[] q1 = start.ori;
  float[] q2 = end.ori;
  float[] qi = new float[4];
  
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
  // get n (a normal of the plane created by the 3 input points)
  PVector tmp1 = new PVector(a.x-b.x, a.y-b.y, a.z-b.z);
  PVector tmp2 = new PVector(a.x-c.x, a.y-c.y, a.z-c.z);
  PVector n = tmp1.cross(tmp2);
  tmp1.normalize();
  tmp2.normalize();
  n.normalize();
  //calculate the angle between the start and end points
  PVector vec1 = new PVector(a.x-center.x, a.y-center.y, a.z-center.z);
  PVector vec2 = new PVector(c.x-center.x, c.y-center.y, c.z-center.z);
  vec1.normalize();
  vec2.normalize();
  float theta = atan2(vec1.cross(vec2).mag(), vec1.dot(vec2));
  
  // Now plug all that into the parametric equation
  //   P = r*cos(t)*u + r*sin(t)*nxu+center [x is cross product]
  // to compute our points along the circumference.
  // We actually only want to create an arc from A to C, not the full
  // circle, so detect when we're close to those points to decide
  // when to start and stop adding points.
  float angle = 0, mu = 0;
  int numPoints = (int)(r*theta/distanceBetweenPoints);
  float inc = 1/(float)numPoints;
  float angleInc = (theta)/(float)numPoints;
  for (int i = 0; i < numPoints; i += 1) {
    PVector pos = rotateVectorQuat(u, n, angle).mult(r).add(center);
    qi = quaternionSlerp(q1, q2, mu);
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
void beginNewContinuousMotion(Point start, Point end, Point next, float p){
  calculateContinuousPositions(start, end, next, p);
  motionFrameCounter = 0;
  if(intermediatePositions.size() > 0){
    calculateIKJacobian(intermediatePositions.get(interMotionIdx));
  }
}

/**
 * Initiate a new fine (linear) motion instruction.
 * @param start Start point
 * @param end Destination point
 */
void beginNewLinearMotion(Point start, Point end) {
  calculateIntermediatePositions(start, end);
  motionFrameCounter = 0;
  if(intermediatePositions.size() > 0){
    calculateIKJacobian(intermediatePositions.get(interMotionIdx));
  }
}

/**
 * Initiate a new circular motion instruction according to FANUC methodology.
 * @param p1 Point 1
 * @param p2 Point 2
 * @param p3 Point 3
 */
void beginNewCircularMotion(Point start, Point inter, Point end) {
  calculateArc(start, inter, end);
  interMotionIdx = 0;
  motionFrameCounter = 0;
  if(intermediatePositions.size() > 0){
    calculateIKJacobian(intermediatePositions.get(interMotionIdx));
  }
}

/**
 * Move the arm model between two points according to its current speed.
 * @param model The arm model
 * @param speedMult Speed multiplier
 */
boolean executeMotion(ArmModel model, float speedMult) {
  motionFrameCounter++;
  // speed is in pixels per frame, multiply that by the current speed setting
  // which is contained in the motion instruction
  float currentSpeed = model.motorSpeed * speedMult;
  if (currentSpeed * motionFrameCounter > distanceBetweenPoints) {
    interMotionIdx++;
    motionFrameCounter = 0;
    if (interMotionIdx >= intermediatePositions.size()) {
      interMotionIdx = -1;
      return true;
    }
    
    int ret = EXEC_SUCCESS;
    if(intermediatePositions.size() > 0){
      calculateIKJacobian(intermediatePositions.get(interMotionIdx));
    }
      
    if(ret == EXEC_FAILURE){
      doneMoving = true;
    }
  }
  
  return false;
} // end execute linear motion

MotionInstruction getActiveMotionInstruct(){
  Instruction inst = new Instruction();
  Program p = programs.get(active_program);
  
  if(p != null && p.getInstructions().size() != 0)
    inst = p.getInstructions().get(active_instruction);
  else return null;
  
  if(inst instanceof MotionInstruction)
    return (MotionInstruction)inst;
  else return null;
}

/**
 * Convert a point based on a coordinate system defined as
 * 3 orthonormal vectors.
 * @param point Point to convert
 * @param xAxis X axis of target coordinate system
 * @param yAxis Y axis of target coordinate system
 * @param zAxis Z axis of target coordinate system
 * @return Coordinates of point after conversion
 */
PVector vectorConvertTo(PVector point, PVector xAxis,
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
PVector vectorConvertFrom(PVector point, PVector xAxis,
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
PVector[] createPlaneFrom3Points(PVector a, PVector b, PVector c) {  
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
PVector circleCenter(PVector a, PVector b, PVector c) {
  float h = calculateH(a.x, a.y, b.x, b.y, c.x, c.y);
  float k = calculateK(a.x, a.y, b.x, b.y, c.x, c.y);
  return new PVector(h, k, a.z);
}

// TODO: Add error check for colinear case (denominator is zero)
float calculateH(float x1, float y1, float x2, float y2, float x3, float y3) {
    float numerator = (x2*x2+y2*y2)*y3 - (x3*x3+y3*y3)*y2 - 
                      ((x1*x1+y1*y1)*y3 - (x3*x3+y3*y3)*y1) +
                      (x1*x1+y1*y1)*y2 - (x2*x2+y2*y2)*y1;
    float denominator = (x2*y3-x3*y2) -
                        (x1*y3-x3*y1) +
                        (x1*y2-x2*y1);
    denominator *= 2;
    return numerator / denominator;
}

float calculateK(float x1, float y1, float x2, float y2, float x3, float y3) {
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
 * @param program Program to execute
 * @param model Arm model to use
 * @return Finished yet (false=no, true=yes)
 */
boolean executeProgram(Program program, ArmModel model, boolean singleInst) {
  //stop executing if no valid program is selected or we reach the end of the program
  if (program == null || currentInstruction >= program.getInstructions().size()){
    return true;
  }
  
  //get the next program instruction
  Instruction ins = program.getInstructions().get(currentInstruction);
  
  //motion instructions
  if (ins instanceof MotionInstruction){
    MotionInstruction instruction = (MotionInstruction)ins;
    if (instruction.getUserFrame() != activeUserFrame){
      setError("ERROR: Instruction's user frame is different from currently active user frame.");
      return true;
    }
    //start a new instruction
    if (!executingInstruction){
      boolean setup = setUpInstruction(program, model, instruction);
      if(!setup){ return true; }
      else executingInstruction = true;
    }
    //continue current instruction
    else {
      if (instruction.getMotionType() == MTYPE_JOINT){
        executingInstruction = !(model.interpolateRotation(instruction.getSpeedForExec(model)));
      }
      else{
        executingInstruction = !(executeMotion(model, instruction.getSpeedForExec(model)));
      }
      
      //move to next instruction after current is finished
      if (!executingInstruction){
        currentInstruction++;
        if(singleInst){ return true; }
      }
    }
  }
  //tool instructions
  else if (ins instanceof ToolInstruction){
    ToolInstruction instruction = (ToolInstruction)ins;
    instruction.execute();
    currentInstruction++;
  }
  //frame instructions
  else if (ins instanceof FrameInstruction){
    FrameInstruction instruction = (FrameInstruction)ins;
    instruction.execute();
    currentInstruction++;
  }//end of instruction type check
  
  return false;
}//end executeProgram

/**
 * Sets up an instruction for execution.
 * @param program Program that the instruction belongs to
 * @param model Arm model to use
 * @param instruction The instruction to execute
 * @return Returns true on failure (invalid instruction), false on success
 */
boolean setUpInstruction(Program program, ArmModel model, MotionInstruction instruction) {
  Point start = new Point(armModel.getEEPos(), armModel.getWPR());
  
  if (instruction.getMotionType() == MTYPE_JOINT) {
    float[] j = instruction.getVector(program).joints;
    
    //set target rotational value for each joint
    for (int n = 0; n < j.length; n++) {
      for (int r = 0; r < 3; r++) {
        if (model.segments.get(n).rotations[r])
          model.segments.get(n).targetRotations[r] = j[n];
          //println("target rotation for joint " + n + ": " + j[n]);
      }
    }
    
    // calculate whether it's faster to turn CW or CCW
    for (Model a : model.segments) {
      for (int r = 0; r < 3; r++) {
        if (a.rotations[r]) {
         // The minimum distance between the current and target joint angles
         float dist_t = minimumDistance(a.currentRotations[r], a.targetRotations[r]);
         
         // check joint movement range
         if (a.jointRanges[r].x == 0 && a.jointRanges[r].y == TWO_PI) {
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
           
           if (dist_t < 0) {
             if ( (dist_lb < 0 && dist_lb > dist_t) || (dist_ub < 0 && dist_ub > dist_t) ) {
               // One or both bounds lie within the shortest path
               a.rotationDirections[r] = 1;
             } 
             else {
               a.rotationDirections[r] = -1;
             }
           } 
           else if (dist_t > 0){
             if ( (dist_lb > 0 && dist_lb < dist_t) || (dist_ub > 0 && dist_ub < dist_t) ) {  
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
  } // end joint movement setup
  else if (instruction.getMotionType() == MTYPE_LINEAR) {
    if (instruction.getTermination() == 0) {
      beginNewLinearMotion(start, instruction.getVector(program));
    } 
    else {
      Point nextPoint = null;
      for (int n = currentInstruction+1; n < program.getInstructions().size(); n++) {
        Instruction nextIns = program.getInstructions().get(n);
        if (nextIns instanceof MotionInstruction) {
          MotionInstruction castIns = (MotionInstruction)nextIns;
          nextPoint = castIns.getVector(program);
          break;
        }
      }
      if (nextPoint == null) {
        beginNewLinearMotion(start, instruction.getVector(program));
      } 
      else{
        beginNewContinuousMotion(start, 
                                 instruction.getVector(program),
                                 nextPoint, 
                                 instruction.getTermination());
      }
    } // end if termination type is continuous
  } // end linear movement setup
  else if (instruction.getMotionType() == MTYPE_CIRCULAR) {
    // If it is a circular instruction, the current instruction holds the intermediate point.
    // There must be another instruction after this that holds the end point.
    // If this isn't the case, the instruction is invalid, so return immediately.
    Point nextPoint = null;
    if (program.getInstructions().size() >= currentInstruction + 2) {
      Instruction nextIns = program.getInstructions().get(currentInstruction+1);
      //make sure next instruction is of valid type
      if (nextIns instanceof MotionInstruction){
        MotionInstruction castIns = (MotionInstruction)nextIns;
        nextPoint = castIns.getVector(program);
      }
      else{
        return false;
      }
    } 
    // invalid instruction
    else{
      return false; 
    }
    
    beginNewCircularMotion(start, instruction.getVector(program), nextPoint);
  } // end circular movement setup
  return true;
} // end setUpInstruction

/* Returns the angle with the smallest magnitude between
 * the two given angles on the Unit Circle */
public float minimumDistance(float angle1, float angle2) {
  float dist = clampAngle(angle2) - clampAngle(angle1);
  
  if (dist > PI) {
    dist -= TWO_PI;
  } else if (dist < -PI) {
    dist += TWO_PI;
  }
  
  return dist;
}

void setError(String text) {
  errorText = text;
  errorCounter = 600;
}

float clampAngle(float angle) {
  while (angle > TWO_PI) angle -= (TWO_PI);
  while (angle < 0) angle += (TWO_PI);
  // angles range: [0, TWO_PI)
  if (angle == TWO_PI) angle = 0;
  return angle;
}
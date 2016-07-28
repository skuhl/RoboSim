ArrayList<Point> intermediatePositions;
int motionFrameCounter = 0;
float distanceBetweenPoints = 5.0;
int interMotionIdx = -1;

int liveSpeed = 10;
boolean executingInstruction = false;

int errorCounter;
String errorText;

// Determines what End Effector mapping should be display
public static int EE_MAPPING = 2,
// Deterimes what type of axes should be displayed
                  AXES_DISPLAY = 1;
public static final boolean COLLISION_DISPLAY = false,
                            DISPLAY_TEST_OUTPUT = true;

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
  //for(int n = 0; n < 15; n++) program.addInstruction(
  //  new MotionInstruction(MTYPE_JOINT, 1, true, 0.5, 0));
  
  GPOS_REG[0] = new PositionRegister(null, new Point(new PVector(165, 116, -5), new float[] { 1f, 0f, 0f, 0f }), false);
  GPOS_REG[0] = new PositionRegister(null, new Point(new PVector(166, -355, 120), new float[] { 1, 0, 0 }), false);
  GPOS_REG[0] = new PositionRegister(null, new Point(new PVector(171, -113, 445), new float[] { 1, 0, 0 }), false);
  GPOS_REG[0] = new PositionRegister(null, new Point(new PVector(725, 225, 50), new float[] { 1, 0, 0 }), false);
  GPOS_REG[0] = new PositionRegister(null, new Point(new PVector(775, 300, 50), new float[] { 1, 0, 0 }), false);
  GPOS_REG[0] = new PositionRegister(null, new Point(new PVector(-474, -218, 37), new float[] { 1, 0, 0 }), false);
  GPOS_REG[0] = new PositionRegister(null, new Point(new PVector(-659, -412, -454), new float[] { 1, 0, 0 }), false);
  
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
  
  for(int n = 0; n < 22; n++) {
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
  String coordFrame = "Coordinate Frame: ";
  
  switch(curCoordFrame) {
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
  
  Point RP = frameRobotPosition(armModel.getJointAngles());
  
  /*
  // apply World Frame
  pos = convertNativeToWorld(RP.position);
  float[][] tMatrix = transformationMatrix(new PVector(0f, 0f, 0f), WORLD_AXES);
  float[][] orienMatrix = quatToMatrix(orien);
  orien = matrixToQuat( transform(orienMatrix, invertHCMatrix(tMatrix)) );
  */
  
  PVector wpr = quatToEuler(RP.orientation).mult(RAD_TO_DEG);
  // Show the coordinates of the End Effector for the current Coordinate Frame
  String cartesian = String.format("Coord  X: %5.3f  Y: %5.3f  Z: %5.3f  W: %5.3f  P: %5.3f  R: %5.3f",
                                    RP.position.x, RP.position.y, RP.position.z, wpr.x, wpr.y, wpr.z);
  
  // Display the Robot's joint angles
  String joints = String.format("Joints  J1: %4.3f J2: %4.3f J3: %4.3f J4: %4.3f J5: %4.3f J6: %4.3f", 
                                 RP.angles[0] * RAD_TO_DEG, RP.angles[1] * RAD_TO_DEG, RP.angles[2] * RAD_TO_DEG,
                                 RP.angles[3] * RAD_TO_DEG, RP.angles[4] * RAD_TO_DEG, RP.angles[5] * RAD_TO_DEG);
  
  text(coordFrame, width - 20, 20);
  text(String.format("Speed: %d%%" , liveSpeed), width - 20, 40);
  text(cartesian, width - 20, 60);
  text(joints, width - 20, 80);
  
  // Display a message if the Robot is in motion
  if (armModel.modelInMotion()) {
    fill(200, 0, 0);
    text("Robot is moving", width - 20, 120);
  }
  
  // Display a message while the robot is carrying an object
  if(armModel.held != null) {
    fill(200, 0, 0);
    text("Object held", width - 20, 140);
    
    float[] held_pos = armModel.held.hit_box.position();
    String obj_pos = String.format("(%f, %f, %f)", held_pos[0], held_pos[1], held_pos[1]);
    text(obj_pos, width - 20, 160);
  }
  
  // Display message for camera pan-lock mode
  if(clickPan % 2 == 1) {
    textSize(14);
    fill(215, 0, 0);
    text("Press space on the keyboard to disable camera paning", 20, height / 2 + 30);
  }
  // Display message for camera rotation-lock mode
  if(clickRotate % 2 == 1) {
    textSize(14);
    fill(215, 0, 0);
    text("Press shift on the keyboard to disable camera rotation", 20, height / 2 + 55);
  }
  
  if(errorCounter > 0) {
    errorCounter--;
    fill(255, 0, 0);
    text(errorText, width-20, 100);
  }
}

/**
 * Transitions to the next Coordinate frame in the cycle, updating the Robot's current frame
 * in the process and skipping the Tool or User frame if there are no active frames in either
 * one. Since the Robot's frame is potentially reset in this method, all Robot motion is halted.
 *
 * @param model  The Robot Arm, for which to switch coordinate frames
 */
public void coordFrameTransition(ArmModel model) {
  // Stop Robot movement
  armModel.halt(); //<>//
  
  // Increment the current coordinate frame
  switch (curCoordFrame) {
    case JOINT:
      curCoordFrame = CoordFrame.WORLD;
      break;
      
    case WORLD:
      curCoordFrame = CoordFrame.TOOL;
      break;
     
    case TOOL:
      curCoordFrame = CoordFrame.USER;
      break;
     
    case USER:
      curCoordFrame = CoordFrame.JOINT;
      break;
  }
  
  // Skip the Tool Frame, if there is no active frame
  if(curCoordFrame == CoordFrame.TOOL && !(activeToolFrame >= 0 && activeToolFrame < toolFrames.length)) {
    curCoordFrame = CoordFrame.USER;
  }
  
  // Skip the User Frame, if there is no active frame
  if(curCoordFrame == CoordFrame.USER && !(activeUserFrame >= 0 && activeUserFrame < userFrames.length)) {
    curCoordFrame = CoordFrame.JOINT;
  }
  
  updateCoordFrame(model);
}

/**
 * Transition back to the World Frame, if the current Frame is Tool or User and there are no active frame
 * set for that Coordinate Frame. This method will halt all Robot motion, since the current active frame
 * may be changed.
 * 
 * @param model  the Robot model, of which to change the frame
 */
public void updateCoordFrame(ArmModel model) {
  // Stop Robot movement
  armModel.halt();
  
  // Return to the World Frame, if no User Frame is active
  if(curCoordFrame == CoordFrame.TOOL && !(activeToolFrame >= 0 && activeToolFrame < toolFrames.length)) {
    curCoordFrame = CoordFrame.WORLD;
  }
  
  // Return to the World Frame, if no User Frame is active
  if(curCoordFrame == CoordFrame.USER && !(activeUserFrame >= 0 && activeUserFrame < userFrames.length)) {
    curCoordFrame = CoordFrame.WORLD;
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
public Point nativeRobotPosition(float[] jointAngles) {
  // Return a point containing the faceplate position, orientation, and joint angles
  return nativeRobotPositionOffset(jointAngles, new PVector(0f, 0f, 0f));
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
public Point nativeRobotPositionOffset(float[] jointAngles, PVector offset) {
  pushMatrix();
  resetMatrix();
  applyModelRotation(jointAngles);
  // Apply offset
  PVector ee = getCoordFromMatrix(offset.x, offset.y, offset.z);
  float[][] orientationMatrix = getRotationMatrix();
  popMatrix();
  // Return a Point containing the EE position, orientation, and joint angles
  return new Point(ee, matrixToQuat(orientationMatrix), jointAngles);
}

/**
 * Returns the Robot's End Effectir position according to the active Tool Frame's
 * offset in the native Coordinate System.
 * 
 * @param jointAngles  A valid set of six joint angles (in radians) for the Robot
 * @returning          The Robot's End Effector position
 */
public Point nativeRobotEEPosition(float[] jointAngles) {
  Frame activeTool = getActiveFrame(CoordFrame.TOOL);
  PVector offset;
  
  if (activeTool != null) {
    // Apply the Tool Tip
    offset = activeTool.getOrigin();
  } else {
    offset = new PVector(0f, 0f, 0f);
  }
  
  return nativeRobotPositionOffset(jointAngles, offset);
}

/**
 * Returns a Point containing the Robot's position, orientation in reference
 * to the currently active Tool or User Frame corresponding to the given set
 * of joint angles, as well as the joint angles themselves.
 * 
 * @param jointAngles  A valid set of six joint angles (in radians)
 *                     for the Robot
 */
public Point frameRobotPosition(float[] jointAngles) {
  Frame activeUser = getActiveFrame(CoordFrame.USER);
  Point RP = nativeRobotEEPosition(jointAngles);
  
  // Apply the active User frame
  if ((curCoordFrame == CoordFrame.USER || curCoordFrame == CoordFrame.TOOL) && activeUser != null) {
    return applyFrame(RP, activeUser.getOrigin(), activeUser.getAxes());
  }
  
  return RP;
}

/**
 * TODO comment
 */
public Point relativeRobotEEPosition(float[] jointAngles, float[] refQuaternion) {
  Point RP = nativeRobotEEPosition(jointAngles);
  float[] newQuaternion = quaternionNormalize( quaternionMult(RP.orientation, quaternionConjugate(refQuaternion)) );
  RP.orientation = newQuaternion;
  
  return RP;
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
public float[][] calculateJacobian(float[] angles, boolean posOffset, float[] frame) {
  float dAngle = DEG_TO_RAD;
  if(!posOffset){ dAngle *= -1; }
  float[][] J = new float[7][6];
  //get current ee position
  Point curRP = relativeRobotEEPosition(angles, frame);
  
  //examine each segment of the arm
  for(int i = 0; i < 6; i += 1) {
    //test angular offset
    angles[i] += dAngle;
    //get updated ee position
    Point newRP = relativeRobotEEPosition(angles, frame);
    
    //get translational delta
    J[0][i] = (newRP.position.x - curRP.position.x) / DEG_TO_RAD;
    J[1][i] = (newRP.position.y - curRP.position.y) / DEG_TO_RAD;
    J[2][i] = (newRP.position.z - curRP.position.z) / DEG_TO_RAD;
    //get rotational delta        
    J[3][i] = (newRP.orientation[0] - curRP.orientation[0]) / DEG_TO_RAD;
    J[4][i] = (newRP.orientation[1] - curRP.orientation[1]) / DEG_TO_RAD;
    J[5][i] = (newRP.orientation[2] - curRP.orientation[2]) / DEG_TO_RAD;
    J[6][i] = (newRP.orientation[3] - curRP.orientation[3]) / DEG_TO_RAD;
    //replace the original rotational value
    angles[i] -= dAngle;
  }
  
  return J;
}//end calculate jacobian

public float[] inverseKinematics(Point tgt, float[] frame) {
  final int limit = 1000;  //max number of times to loop
  int count = 0;
  
  float[] angles = tgt.angles.clone();
  float[] refTgt = quaternionNormalize( quaternionMult(tgt.orientation, quaternionConjugate(frame)) );
  
  while(count < limit) {
    Point cPoint = relativeRobotEEPosition(angles, frame);
    
    //calculate our translational offset from target
    float[] tDelta = calculateVectorDelta(tgt.position, cPoint.position);
    //calculate our rotational offset from target
    float[] rDelta = calculateVectorDelta(refTgt, cPoint.orientation, 4);
    System.out.printf("%s -> %s\n", arrayToString(cPoint.orientation), arrayToString(refTgt));
    float[] delta = new float[7];
    
    delta[0] = tDelta[0];
    delta[1] = tDelta[1];
    delta[2] = tDelta[2];
    delta[3] = rDelta[0];
    delta[4] = rDelta[1];
    delta[5] = rDelta[2];
    delta[6] = rDelta[3];
    
    float dist = PVector.dist(cPoint.position, tgt.position);
    float rDist = calculateQuatMag(rDelta);
    //println("distances from tgt: " + dist + ", " + rDist);
    //check whether our current position is within tolerance
    if (dist < (liveSpeed / 100f) && rDist < 0.00005f*liveSpeed) break;
    //calculate jacobian, 'J', and its inverse
    float[][] J = calculateJacobian(angles, true, frame);
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
      System.out.printf("\nDelta: %s\nAngles: %s\n%s\n%s -> %s\n", arrayToString(delta), arrayToString(angles), matrixToString(J), arrayToString(cPoint.orientation), arrayToString(refTgt));
      return null;
    }
  }
  
  return angles;
}

/**
 * Determine how close together intermediate points between two points
 * need to be based on current speed
 */
void calculateDistanceBetweenPoints() {
  MotionInstruction instruction = getActiveMotionInstruct();
  if(instruction != null && instruction.getMotionType() != MTYPE_JOINT)
  distanceBetweenPoints = instruction.getSpeed() / 60.0;
  else if(curCoordFrame != CoordFrame.JOINT)
  distanceBetweenPoints = armModel.motorSpeed * liveSpeed / 6000f;
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
  
  PVector p1 = start.position;
  PVector p2 = end.position;
  float[] q1 = start.orientation;
  float[] q2 = end.orientation;
  float[] qi = new float[4];
  
  float mu = 0;
  int numberOfPoints = (int)(dist(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z) / distanceBetweenPoints);
  float increment = 1.0 / (float)numberOfPoints;
  for(int n = 0; n < numberOfPoints; n++) {
    mu += increment;
    
    qi = quaternionSlerp(q1, q2, mu);
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
  
  PVector p1 = start.position;
  PVector p2 = end.position;
  PVector p3 = next.position;
  float[] q1 = start.orientation;
  float[] q2 = end.orientation;
  float[] q3 = next.orientation;
  float[] qi = new float[4];
  
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
  float increment = 1.0 / (float)numberOfPoints;
  for(int n = 0; n < numberOfPoints; n++) {
    mu += increment;
    qi = quaternionSlerp(q2, q3, mu);
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
    qi = quaternionSlerp(q1, q2, mu);
    intermediatePositions.add(new Point(new PVector(
    p1.x * (1 - mu) + (p2.x * mu),
    p1.y * (1 - mu) + (p2.y * mu),
    p1.z * (1 - mu) + (p2.z * mu)),
    qi));
  }
  
  int secondaryIdx = 0; // accessor for secondary targets
  
  mu = 0;
  increment /= 2.0;
  
  Point currentPoint;
  if(intermediatePositions.size() > 0) {
    currentPoint = intermediatePositions.get(intermediatePositions.size()-1);
  }
  else {
    // NOTE orientation is in Native Coordinates!
    currentPoint = nativeRobotEEPosition(armModel.getJointAngles());
  }
  
  for(int n = transitionPoint; n < numberOfPoints; n++) {
    mu += increment;
    Point tgt = secondaryTargets.get(secondaryIdx);
    qi = quaternionSlerp(currentPoint.orientation, tgt.orientation, mu);
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
void calculateArc(Point start, Point inter, Point end) {  
  calculateDistanceBetweenPoints();
  intermediatePositions.clear();
  
  PVector a = start.position;
  PVector b = inter.position;
  PVector c = end.position;
  float[] q1 = start.orientation;
  float[] q2 = end.orientation;
  float[] qi = new float[4];
  
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
    PVector pos = rotateVectorQuat(u, n, angle).mult(r).add(center);
    if(i == numPoints-1) pos = end.position;
    qi = quaternionSlerp(q1, q2, mu);
    println(pos + ", " + end.position);
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
void beginNewContinuousMotion(Point start, Point end, Point next, float p) {
  calculateContinuousPositions(start, end, next, p);
  motionFrameCounter = 0;
  if(intermediatePositions.size() > 0) {
    Point tgtPoint = intermediatePositions.get(interMotionIdx);
    float[] refQuaternion = nativeRobotEEPosition(armModel.getJointAngles()).orientation;
    inverseKinematics(tgtPoint, refQuaternion);
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
  if(intermediatePositions.size() > 0) {
    Point tgtPoint = intermediatePositions.get(interMotionIdx);
    float[] refQuaternion = nativeRobotEEPosition(armModel.getJointAngles()).orientation;
    inverseKinematics(tgtPoint, refQuaternion);
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
  if(intermediatePositions.size() > 0) {
    Point tgtPoint = intermediatePositions.get(interMotionIdx);
    float[] refQuaternion = nativeRobotEEPosition(armModel.getJointAngles()).orientation;
    inverseKinematics(tgtPoint, refQuaternion);
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
      float[] refQuaternion = nativeRobotEEPosition(armModel.getJointAngles()).orientation;
      inverseKinematics(tgtPoint, refQuaternion);
    }
    
    if(ret == EXEC_FAILURE) {
      armModel.inMotion = false;
    }
  }
  
  return false;
} // end execute linear motion

MotionInstruction getActiveMotionInstruct() {
  Instruction inst = null;
  
  if (active_prog < 0 || active_prog >= programs.size()) {
    return null;
  }
  
  Program p = programs.get(active_prog);
  
  if(p != null && p.getInstructions().size() != 0)
  inst = p.getInstructions().get(active_instr);
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
  if(program == null || currentInstruction < 0 || currentInstruction >= program.getInstructions().size()) {
    return true;
  }
  
  //get the next program instruction
  Instruction ins = program.getInstructions().get(currentInstruction);
  
  //skip commented instructions
  if(ins.isCommented()){
    currentInstruction++;
    if(singleInst) { return true; }
  }
  //motion instructions
  else if(ins instanceof MotionInstruction) {
    MotionInstruction instruction = (MotionInstruction)ins;
    
    if(instruction.getUserFrame() != activeUserFrame) {
      setError("ERROR: Instruction's user frame is different from currently active user frame.");
      return true;
    }
    
    //start a new instruction
    if(!executingInstruction) {
      boolean setup = setUpInstruction(program, model, instruction);
      if(!setup) { return true; }
      else executingInstruction = true;
    }
    //continue current instruction
    else {
      if(instruction.getMotionType() == MTYPE_JOINT) {
        executingInstruction = !(model.interpolateRotation(instruction.getSpeedForExec(model)));
      }
      else {
        executingInstruction = !(executeMotion(model, instruction.getSpeedForExec(model)));
      }
      
      // Move to next instruction after current is finished
      if(!executingInstruction) {
        currentInstruction++;
        if(singleInst) { return true; }
      }
    }
  }
  //tool instructions
  else if(ins instanceof IOInstruction) {
    IOInstruction instruction = (IOInstruction)ins;
    instruction.execute();
    currentInstruction++;
    
    if(singleInst) { return true; }
  }
  //frame instructions
  else if(ins instanceof FrameInstruction) {
    FrameInstruction instruction = (FrameInstruction)ins;
    instruction.execute();
    currentInstruction++;
    
    if(singleInst) { return true; }
  } 
  else if(ins instanceof JumpInstruction){
    ((JumpInstruction)ins).execute();
    currentInstruction++;
  }
  else if (ins instanceof Instruction) {
    // Blank instruction
    ++currentInstruction;
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
  // NOTE Orientation is in the Native Coordinate Frame
  Point start = nativeRobotEEPosition(armModel.getJointAngles());
  
  if(instruction.getMotionType() == MTYPE_JOINT) {
    armModel.setupRotationInterpolation( instruction.getVector(program).angles );
  } // end joint movement setup
  else if(instruction.getMotionType() == MTYPE_LINEAR) {
    if(instruction.getTermination() == 0) {
      beginNewLinearMotion(start, instruction.getVector(program));
    } 
    else {
      Point nextPoint = null;
      for(int n = currentInstruction+1; n < program.getInstructions().size(); n++) {
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
        instruction.getTermination());
      }
    } // end if termination type is continuous
  } // end linear movement setup
  else if(instruction.getMotionType() == MTYPE_CIRCULAR) {
    // If it is a circular instruction, the current instruction holds the intermediate point.
    // There must be another instruction after this that holds the end point.
    // If this isn't the case, the instruction is invalid, so return immediately.
    Point nextPoint = null;
    if(program.getInstructions().size() >= currentInstruction + 2) {
      Instruction nextIns = program.getInstructions().get(currentInstruction+1);
      //make sure next instruction is of valid type
      if(nextIns instanceof MotionInstruction) {
        MotionInstruction castIns = (MotionInstruction)nextIns;
        nextPoint = castIns.getVector(program);
      }
      else {
        return false;
      }
    } 
    // invalid instruction
    else {
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
  
  if(dist > PI) {
    dist -= TWO_PI;
  } else if(dist < -PI) {
    dist += TWO_PI;
  }
  
  return dist;
}

void setError(String text) {
  errorText = text;
  errorCounter = 600;
}

/**
 * Maps wth given angle to the range of 0 (inclusive) to
 * two PI (exclusive).
 *
 * @param angle  some angle in radians
 * @return       An angle between  0 (inclusive) to two
 *               PI (exclusive)
 */
float clampAngle(float angle) {
  while(angle > TWO_PI) angle -= (TWO_PI);
  while(angle < 0) angle += (TWO_PI);
  // angles range: [0, TWO_PI)
  if(angle == TWO_PI) angle = 0;
  return angle;
}
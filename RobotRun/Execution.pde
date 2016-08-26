ArrayList<Point> intermediatePositions;
int motionFrameCounter = 0;
float distanceBetweenPoints = 5.0;
int interMotionIdx = -1;

int liveSpeed = 10;
boolean executingInstruction = false;

// Determines what End Effector mapping should be display
private EEMapping mappingState = EEMapping.LINE;
// Deterimes what type of axes should be displayed
private static AxesDisplay axesState = AxesDisplay.AXES;

private static final boolean COLLISION_DISPLAY = true,
                             DISPLAY_TEST_OUTPUT = true;

/**
 * Displays important information in the upper-right corner of the screen.
 */
public void showMainDisplayText() {
  fill(0);
  textAlign(RIGHT, TOP);
  int lastTextPositionX = width - 20,
      lastTextPositionY = 20;
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
  
  Point RP = nativeRobotEEPoint(armModel.getJointAngles());
  Frame active = getActiveFrame(null);
  
  if (active != null) {
    // Convert into currently active frame
    RP = applyFrame(RP, active.getOrigin(), active.getOrientation());
  }
  
  String[] cartesian = RP.toLineStringArray(true),
           joints = RP.toLineStringArray(false);
  // Display the current Coordinate Frame name
  text(coordFrame, lastTextPositionX, lastTextPositionY);
  lastTextPositionY += 20;
  // Display the Robot's speed value as a percent
  text(String.format("Speed: %d%%", liveSpeed), lastTextPositionX, lastTextPositionY);
  lastTextPositionY += 20;
  // Display the title of the currently active scenario
  String scenarioTitle;
  Scenario s = activeScenario();
  
  if (s != null) {
    scenarioTitle = "Scenario: " + s.getName();
  } else {
    scenarioTitle = "No active scenario";
  }
  
  text(scenarioTitle, lastTextPositionX, lastTextPositionY);
  lastTextPositionY += 40;
  // Display the Robot's current XYZWPR values
  text("Robot Position and Orientation", lastTextPositionX, lastTextPositionY);
  lastTextPositionY += 20;
  for (String line : cartesian) {
    text(line, lastTextPositionX, lastTextPositionY);
    lastTextPositionY += 20;
  }
  
  lastTextPositionY += 20;
  // Display the Robot's current joint angle values
  text("Robot Joint Angles", lastTextPositionX, lastTextPositionY);
  lastTextPositionY += 20;
  for (String line : joints) {
    text(line, lastTextPositionX, lastTextPositionY);
    lastTextPositionY += 20;
  }
  
  WorldObject toEdit = manager.getActiveWorldObject();
  // Display the position and orientation of the active world object
  if (toEdit != null) {
    String[] dimFields = toEdit.dimFieldsToStringArray();
    // Convert the values into the World Coordinate System
    PVector position = convertNativeToWorld(toEdit.getLocalCenter());
    PVector wpr = convertNativeToWorld( matrixToEuler(toEdit.getLocalOrientationAxes()) ).mult(RAD_TO_DEG);
    // Create a set of uniform Strings
    String[] fields = new String[] { String.format("X: %4.3f", position.x), String.format("Y: %4.3f", position.y),
                                     String.format("Z: %4.3f", position.z), String.format("W: %4.3f", wpr.x),
                                     String.format("P: %4.3f", wpr.y), String.format("R: %4.3f", wpr.z) };
    
    lastTextPositionY += 20;
    text(toEdit.getName(), lastTextPositionX, lastTextPositionY);
    lastTextPositionY += 20;
    String dimDisplay = "";
    // Display the dimensions of the world object (if any)
    for (int idx = 0; idx < dimFields.length; ++idx) {
      if ((idx + 1) < dimFields.length) {
        dimDisplay += String.format("%-12s", dimFields[idx]);
        
      } else {
        dimDisplay += String.format("%s", dimFields[idx]);
      }
    }
    
    text(dimDisplay, lastTextPositionX, lastTextPositionY);
    
    lastTextPositionY += 20;
    // Add space patting
    text(String.format("%-12s %-12s %s", fields[0], fields[1], fields[2]), lastTextPositionX, lastTextPositionY);
    lastTextPositionY += 20;
    text(String.format("%-12s %-12s %s", fields[3], fields[4], fields[5]), lastTextPositionX, lastTextPositionY);
    lastTextPositionY += 20;
  }
  
  lastTextPositionY += 20;
  // Display the current axes display state
  text(String.format("Axes Display: %s", axesState.name()),  lastTextPositionX, height - 50);
  
  if (axesState == AxesDisplay.GRID) {
    // Display the current ee mapping state
    text(String.format("EE Mapping: %s", mappingState.name()),  lastTextPositionX, height - 30);
  }
   
  if (DISPLAY_TEST_OUTPUT) {
    String[] cameraFields = camera.toStringArray();
    // Display camera position, orientation, and scale
    for (String field : cameraFields) {
      lastTextPositionY += 20;
      text(field, lastTextPositionX, lastTextPositionY);
    }
    lastTextPositionY += 40;
    
    fill(215, 0, 0);
    
    // Display a message when there is an error with the Robot's movement
    if (robotFault) {
      text("Robot Fault (press SHIFT + Reset)", lastTextPositionX, lastTextPositionY);
      lastTextPositionY += 20;
    }
    
    // Display a message if the Robot is in motion
    if (armModel.modelInMotion()) {
      text("Robot is moving", lastTextPositionX, lastTextPositionY);
      lastTextPositionY += 20;
    }
    
    if (programRunning) {
      text("Program executing", lastTextPositionX, lastTextPositionY);
      lastTextPositionY += 20;
    }
    
    // Display a message while the robot is carrying an object
    if(armModel.held != null) {
      text("Object held", lastTextPositionX, lastTextPositionY);
      lastTextPositionY += 20;
      
      PVector held_pos = armModel.held.getLocalCenter();
      String obj_pos = String.format("(%f, %f, %f)", held_pos.x, held_pos.y, held_pos.z);
      text(obj_pos, lastTextPositionX, lastTextPositionY);
      lastTextPositionY += 20;
    }
  }
  
  manager.updateWindowDisplay();
}

/** //<>//
 * Transitions to the next Coordinate frame in the cycle, updating the Robot's current frame
 * in the process and skipping the Tool or User frame if there are no active frames in either
 * one. Since the Robot's frame is potentially reset in this method, all Robot motion is halted.
 *
 * @param model  The Robot Arm, for which to switch coordinate frames
 */
public void coordFrameTransition() {
  // Stop Robot movement
  armModel.halt();
  
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
  
  updateCoordFrame();
}

/**
 * Transition back to the World Frame, if the current Frame is Tool or User and there are no active frame
 * set for that Coordinate Frame. This method will halt the motion of the Robot if the active frame is changed.
 */
public void updateCoordFrame() {
  
  // Return to the World Frame, if no User Frame is active
  if(curCoordFrame == CoordFrame.TOOL && !(activeToolFrame >= 0 && activeToolFrame < toolFrames.length)) {
    curCoordFrame = CoordFrame.WORLD;
    // Stop Robot movement
    armModel.halt();
  }
  
  // Return to the World Frame, if no User Frame is active
  if(curCoordFrame == CoordFrame.USER && !(activeUserFrame >= 0 && activeUserFrame < userFrames.length)) {
    curCoordFrame = CoordFrame.WORLD;
    // Stop Robot movement
    armModel.halt();
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
public Point nativeRobotPoint(float[] jointAngles) {
  // Return a point containing the faceplate position, orientation, and joint angles
  return nativeRobotPointOffset(jointAngles, new PVector(0f, 0f, 0f));
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
public Point nativeRobotPointOffset(float[] jointAngles, PVector offset) {
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
 * Returns the Robot's End Effector position according to the active Tool Frame's
 * offset in the native Coordinate System.
 * 
 * @param jointAngles  A valid set of six joint angles (in radians) for the Robot
 * @returning          The Robot's End Effector position
 */
public Point nativeRobotEEPoint(float[] jointAngles) {
  Frame activeTool = getActiveFrame(CoordFrame.TOOL);
  PVector offset;
  
  if (activeTool != null) {
    // Apply the Tool Tip
    offset = ((ToolFrame)activeTool).getTCPOffset();
  } else {
    offset = new PVector(0f, 0f, 0f);
  }
  
  return nativeRobotPointOffset(jointAngles, offset);
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
public float[][] calculateJacobian(float[] angles, boolean posOffset) {
  float dAngle = DEG_TO_RAD;
  if (!posOffset){ dAngle *= -1; }
  
  float[][] J = new float[7][6];
  //get current ee position
  Point curRP = nativeRobotEEPoint(angles);
  
  //examine each segment of the arm
  for(int i = 0; i < 6; i += 1) {
    //test angular offset
    angles[i] += dAngle;
    //get updated ee position
    Point newRP = nativeRobotEEPoint(angles);
    
    if (quaternionDotProduct(curRP.orientation, newRP.orientation) < 0f) {
      // Use -q instead of q
      newRP.orientation = vectorScalarMult(newRP.orientation, -1);
    }
    
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
}

/**
 * Attempts to calculate the joint angles that would place the Robot in the given target position and
 * orientation. The srcAngles parameter defines the position of the Robot from which to move, since
 * this inverse kinematics uses a relative conversion formula. There is no guarantee that the target
 * position and orientation can be reached; in the case that inverse kinematics fails, then null is
 * returned. Otherwise, a set of six angles will be returned, though there is no guarantee that these
 * angles are valid!
 * 
 * @param srcAngles       The initial position of the Robot
 * @param tgtPosition     The desired position of the Robot
 * @param tgtOrientation  The desited orientation of the Robot
 */
public float[] inverseKinematics(float[] srcAngles, PVector tgtPosition, float[] tgtOrientation) {
  final int limit = 1000;  // Max number of times to loop
  int count = 0;
  
  float[] angles = srcAngles.clone();
  
  while(count < limit) {
    Point cPoint = nativeRobotEEPoint(angles);
    
    if (quaternionDotProduct(tgtOrientation, cPoint.orientation) < 0f) {
      // Use -q instead of q
      tgtOrientation = vectorScalarMult(tgtOrientation, -1);
    }
    
    //calculate our translational offset from target
    PVector tDelta = PVector.sub(tgtPosition, cPoint.position);
    //calculate our rotational offset from target
    float[] rDelta = calculateVectorDelta(tgtOrientation, cPoint.orientation, 4);
    float[] delta = new float[7];
    
    delta[0] = tDelta.x;
    delta[1] = tDelta.y;
    delta[2] = tDelta.z;
    delta[3] = rDelta[0];
    delta[4] = rDelta[1];
    delta[5] = rDelta[2];
    delta[6] = rDelta[3];
    
    float dist = PVector.dist(cPoint.position, tgtPosition);
    float rDist = getVectorMag(rDelta);
    //check whether our current position is within tolerance
    if ( (dist < (liveSpeed / 100f)) && (rDist < (0.00005f * liveSpeed)) ) { break; }
    
    //calculate jacobian, 'J', and its inverse
    float[][] J = calculateJacobian(angles, true);
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
      if (DISPLAY_TEST_OUTPUT) {
        System.out.printf("\nDelta: %s\nAngles: %s\n%s\n%s -> %s\n", arrayToString(delta), arrayToString(angles),
                            matrixToString(J), arrayToString(cPoint.orientation), arrayToString(tgtOrientation));
      }
      
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
  MotionInstruction instruction = activeMotionInst();
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
    currentPoint = nativeRobotEEPoint(armModel.getJointAngles());
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
    armModel.jumpTo(tgtPoint.position, tgtPoint.orientation);
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
    armModel.jumpTo(tgtPoint.position, tgtPoint.orientation);
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
    armModel.jumpTo(tgtPoint.position, tgtPoint.orientation);
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
      ret = armModel.jumpTo(tgtPoint.position, tgtPoint.orientation);
    }
    
    if(ret == EXEC_FAILURE) {
      triggerFault();
      return true;
    }
  }
  
  return false;
} // end execute linear motion

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
boolean executeProgram(Program program, ArmModel model, boolean singleInstr) {
  Instruction activeInstr = activeInstruction();
  int nextInstr = active_instr + 1;
  
  //stop executing if no valid program is selected or we reach the end of the program
  if(robotFault || activeInstr == null) {
    return true;
    
  } else if (!activeInstr.isCommented()){
    //motion instructions
    if (activeInstr instanceof MotionInstruction) {
      MotionInstruction motInstr = (MotionInstruction)activeInstr;
      //start a new instruction
      if(!executingInstruction) {
        executingInstruction = setUpInstruction(program, model, motInstr);
        
        if (!executingInstruction) {
          // Motion Instruction failed
          nextInstr = -1;
        }
      }
      //continue current motion instruction
      else {
        if(motInstr.getMotionType() == MTYPE_JOINT) {
          executingInstruction = !(model.interpolateRotation(motInstr.getSpeedForExec(model)));  
        }
        else {  
          executingInstruction = !(executeMotion(model, motInstr.getSpeedForExec(model)));
        }
      }
    } 
    //jump instructions
    else if (activeInstr instanceof JumpInstruction) {
      nextInstr = activeInstr.execute();
      executingInstruction = false;
    } 
    //other instructions
    else {
      executingInstruction = false;
      activeInstr.execute();
    }//end of instruction type check
  } //skip commented instructions
  
  // Move to next instruction after current is finished
  if(!executingInstruction) {
    if(nextInstr == -1) {
      // Failed to jump to target label
      triggerFault();
    } else if(nextInstr == activeProgram().size() && !call_stack.isEmpty()) {
      int[] p = call_stack.pop();
      active_prog = p[0];
      active_instr = p[1];
      
      row_select = active_instr;
      col_select = 0;
      start_render = 0;
      programRunning = !executeProgram(activeProgram(), armModel, false);
    } else {
      // Move to nextInstruction
      int size = activeProgram().getInstructions().size() + 1;
      int i = active_instr,
          r = row_select;
      
      active_instr = max(0, min(nextInstr, size - 1));
      row_select = max(0, min(r + active_instr - i, contents.size() - 1));
      start_render = start_render + (active_instr - i) - (row_select - r);
    }
    
    updateScreen();
  }
  
  return (!executingInstruction && singleInstr);
}//end executeProgram

/**
 * Sets up an instruction for execution.
 *
 * @param program Program that the instruction belongs to
 * @param model Arm model to use
 * @param instruction The instruction to execute
 * @return Returns false on failure (invalid instruction), true on success
 */
boolean setUpInstruction(Program program, ArmModel model, MotionInstruction instruction) {
  Point start = nativeRobotEEPoint(model.getJointAngles());
  
  if(instruction.getMotionType() == MTYPE_JOINT) {
    armModel.setupRotationInterpolation(instruction.getVector(program).angles);
  } // end joint movement setup
  else if(instruction.getMotionType() == MTYPE_LINEAR) {
    
    if (!instruction.checkFrames(activeToolFrame, activeUserFrame)) {
      // Current Frames must match the instruction's frames
      System.out.printf("Tool frame: %d : %d\nUser frame: %d : %d\n\n", instruction.getToolFrame(),
                                      activeToolFrame, instruction.getUserFrame(), activeUserFrame);
      return false;
      
    }
    
    if(instruction.getTermination() == 0) {
      beginNewLinearMotion(start, instruction.getVector(program));
    } 
    else {
      Point nextPoint = null;
      for(int n = active_instr+1; n < program.getInstructions().size(); n++) {
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
        instruction.getTermination() / 100f);
      }
    } // end if termination type is continuous
  } // end linear movement setup
  else if(instruction.getMotionType() == MTYPE_CIRCULAR) {
    // If it is a circular instruction, the current instruction holds the intermediate point.
    // There must be another instruction after this that holds the end point.
    // If this isn't the case, the instruction is invalid, so return immediately.
    Point nextPoint = null;
    if(program.getInstructions().size() >= active_instr + 2) {
      Instruction nextIns = program.getInstructions().get(active_instr+1);
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

/**
 * Stop robot motion, program execution
 */
public void triggerFault() {
  armModel.halt();
  robotFault = true;
}

/**
 * Returns a string represenation of the given matrix.
 * 
 * @param matrix  A non-null matrix
 */
public String matrixToString(float[][] matrix) {
  String mStr = "";
  
  for(int row = 0; row < matrix.length; ++row) {
    mStr += "\n[";

    for(int col = 0; col < matrix[0].length; ++col) {
      // Account for the negative sign character
      if(matrix[row][col] >= 0) { mStr += " "; }
      
      mStr += String.format(" %5.6f", matrix[row][col]);
    }

    mStr += "  ]";
  }
  
  return (mStr + "\n");
}

public String arrayToString(float[] array) {
  String s = "[";
  
  for(int i = 0; i < array.length; i += 1) {
    s += String.format("%5.4f", array[i]);
    if(i != array.length-1) s += ", ";
  }
  
  return s + "]";
}
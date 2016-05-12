ArrayList<PVector> intermediatePositions;
int motionFrameCounter = 0;
float distanceBetweenPoints = 5.0;
int interMotionIdx = -1;

final int COORD_JOINT = 0, COORD_WORLD = 1, COORD_TOOL = 2, COORD_USER = 3;
int curCoordFrame = COORD_JOINT;
float liveSpeed = 0.1;

int errorCounter;
String errorText;

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
  //currentProgram = program4;
  
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
  text("Coordinate Frame: " + (curCoordFrame == COORD_JOINT ? "Joint" : "World"), width-20, 20);
  text("Speed: " + (Integer.toString((int)(Math.round(liveSpeed*100)))) + "%", width-20, 40);
  
  PVector eep = calculateEndEffectorPosition(armModel, armModel.getJointRotations());
  //eep = convertNativeToWorld(eep);
  String ee_pos = String.format("Coord  X: %5.4f  Y: %5.4f  Z: %5.4f", eep.x, eep.y, eep.z);
  String ee_dist = String.format("Dist %4.5f", PVector.dist(eep, base_center));
  
  if (curCoordFrame == COORD_JOINT) {
    float j[] = armModel.getJointRotations();
    String s = String.format("Joints  J1: %5.4f J2: %5.4f J3: %5.4f J4: %5.4f J5: %5.4f J6: %5.4f", 
                      j[0], j[1], j[2], j[3], j[4], j[5]);
                      
    text(s, width-20, 60);
  } else {
    PVector wpr = armModel.getRot();
    String ee_rot = String.format("  W: %5.4f  P: %5.4f  R: %5.4f", wpr.x, wpr.y, wpr.z);
    text(ee_pos + ee_rot, width-20, 60);
  }
  
  text((shift == ON ? "Shift ON" : "Shift OFF"), width-120, 80);
  text((step == ON ? "Step ON" : "Step OFF"), width-20, 80);

  // Display the current position of the End Effector in the Plane
  text(ee_pos, width -20, 100);
  // Display the distance between the end effector and the center of the base of the robot (rough center)
  text(ee_dist, width - 20, 120);
  
  // Display message for camera pan-lock mode
  if (clickPan % 2 == 1) {
    textSize(14);
    fill(215, 0, 0);
    text("Press space on the keyboard to disable camera paning", 392, height / 2 + 15);
  }
  // Display message for camera rotation-lock mode
  if (clickRotate % 2 == 1) {
    textSize(14);
    fill(215, 0, 0);
    text("Press shift on the keyboard to disable camera rotation", 390, height / 2 + 40);
  }
  
  if (errorCounter > 0) {
    errorCounter--;
    fill(255, 0, 0);
    text(errorText, width-20, 100);
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


/**
 * Gives the current position of the end effector in
 * Processing native coordinates.
 * @param model Arm model whose end effector position to calculate
 * @param test Determines whether to use arm segments' actual
 *             rotation values or if we're checking trial rotations
 * @return The current end effector position
 */
PVector calculateEndEffectorPosition(ArmModel model, float[] rot) {
  pushMatrix();
  resetMatrix();
  if (model.type == ARM_TEST) {
    rotateY(rot[0]);
    translate(0, -200, 0);
    translate(-25, -130, 0);
    
    rotateZ(rot[1]);
    translate(-25, -130, 0);
    translate(0, -120, 0);
    
    rotateZ(rot[2]);
    translate(0, -120, 0);
    rotateZ(PI);
    translate(0, 102, 0);
  } 
  else if (model.type == ARM_STANDARD) {
    translate(600, 200, 0);
    translate(-50, -166, -358); // -115, -213, -413
    rotateZ(PI);
    translate(150, 0, 150);
    
    rotateY(rot[0]);
    
    translate(-150, 0, -150);
    rotateZ(-PI);    
    translate(-115, -85, 180);
    rotateZ(PI);
    rotateY(PI/2);
    translate(0, 62, 62);
    
    rotateX(rot[1]);
    
    translate(0, -62, -62);
    rotateY(-PI/2);
    rotateZ(-PI);   
    translate(0, -500, -50);
    rotateZ(PI);
    rotateY(PI/2);
    translate(0, 75, 75);
    
    rotateX(rot[2]);
    
    translate(0, -75, -75);
    rotateY(PI/2);
    rotateZ(-PI);
    translate(745, -150, 150);
    rotateZ(PI/2);
    rotateY(PI/2);
    translate(70, 0, 70);
    
    rotateY(rot[3]);
    
    translate(-70, 0, -70);
    rotateY(-PI/2);
    rotateZ(-PI/2);    
    translate(-115, 130, -124);
    rotateZ(PI);
    rotateY(-PI/2);
    translate(0, 50, 50);
    
    rotateX(rot[4]);
    
    translate(0, -50, -50);
    rotateY(PI/2);
    rotateZ(-PI);    
    translate(150, -10, 95);
    rotateY(-PI/2);
    rotateZ(PI);
    translate(45, 45, 0);
    
    rotateZ(rot[5]);
    
    if (activeToolFrame >= 0 && activeToolFrame < toolFrames.length) {
      PVector tr = toolFrames[activeToolFrame].getOrigin();
      translate(tr.x, tr.y, tr.z);
    }
  }
  PVector ret = new PVector(
    modelX(0, 0, 0),
    modelY(0, 0, 0),
    modelZ(0, 0, 0));
  
  popMatrix();
  return ret;
} // end calculateEndEffectorPosition

/* This method will draw the End Effector grid mapping based on the value of EE_MAPPING:
 *
 *  0 -> a line is drawn between the EE and the grid plane
 *  1 -> a point is drawn on the grid plane that corresponds to the EE's xz coordinates
 *  For any other value, nothing is drawn
 */
public void drawEndEffectorGridMapping() {
  
  PVector ee_pos = calculateEndEffectorPosition(armModel, armModel.getJointRotations());
  // Change color of the EE mapping based on if it lies below or above the ground plane
  color c = (ee_pos.y <= PLANE_Y) ? color(255, 0, 0) : color(150, 0, 255);
  
  // Toggle EE mapping type with 'e'
  switch (EE_MAPPING) {
    
    case 0:
      stroke(c);
      // Draw a line, from the EE to the grid in the xz plane, parallel to the y plane
      line(ee_pos.x, ee_pos.y, ee_pos.z, ee_pos.x, PLANE_Y, ee_pos.z);
      break;
    
    case 1:
      noStroke();
      fill(c);
      // Draw a point, which maps the EE's position to the grid in the xz plane
      pushMatrix();
      rotateX(PI / 2);
      translate(0, 0, -PLANE_Y);
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
 * @param model The arm model whose transformations to apply
 */
void applyModelRotation(ArmModel model) {
  if (model.type == ARM_TEST) {
    rotateY(model.segments.get(0).currentRotations[1]);
    translate(0, -200, 0);
    translate(-25, -130, 0);
    rotateZ(model.segments.get(1).currentRotations[2]);
    translate(-25, -130, 0);
    translate(0, -120, 0);
    rotateZ(model.segments.get(2).currentRotations[2]);
    translate(0, -120, 0);
    rotateZ(PI);
    translate(0, 102, 0);
  } else if (model.type == ARM_STANDARD) {
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
    if (activeToolFrame >= 0 && activeToolFrame < toolFrames.length) {
      PVector tr = toolFrames[activeToolFrame].getOrigin();
      translate(tr.x, tr.y, tr.z);
    }
  }
} // end apply model rotations

/**
 * Calculate the Jacobian matrix for the robotic arm for
 * a given set of joint rotational values.
 */
float[][] calculateJacobian(float[] angles){
  float dAngle = 0.0174553;
  float[][] jacobian = new float[6][3];
  PVector oPos = calculateEndEffectorPosition(armModel, angles);
  PVector nPos = new PVector(0, 0, 0);
  
  //examine each segment of the arm
  for(int i = 0; i < 6; i += 1){
    //test angular offset
    angles[i] += dAngle;
    nPos = calculateEndEffectorPosition(armModel, angles);
    jacobian[i] = calculateVectorDelta(nPos, oPos);
    //replace the original rotational value
    angles[i] -= dAngle;
  }
  
  return jacobian;
}//end calculate jacobian

int calculateIKJacobian(PVector tgt){
  float[] angles = armModel.getJointRotations();
  int count = 0;
  
  while(count < 1000){
    PVector cPos = calculateEndEffectorPosition(armModel, angles);
    float[] delta = calculateVectorDelta(tgt, cPos);
    float dist = PVector.dist(cPos, tgt);
    
    if(dist < 1) break;
    
    float[][] jacobian = calculateJacobian(angles);
    float[] dAngle = new float[6];
    
    for(int i = 0; i < 6; i += 1){
      dAngle[i] = calculateVectorDot(jacobian[i], delta);
    }
    
    float expectedChange[] = {0, 0, 0};
    for(int i = 0; i < 3; i += 1){
      for(int j = 0; j < 6; j += 1){
        expectedChange[i] += dAngle[j] * jacobian[j][i];
      }
    }
    
    float alpha = calculateVectorDot(expectedChange, delta)/
                  calculateVectorDot(expectedChange, expectedChange);
    
    for(int i = 0; i < 6; i += 1){
      angles[i] += alpha*dAngle[i]*0.0174553;
      angles[i] %= TWO_PI;
    }
    
    count += 1;
  }
  
  if(count >= 1000){
    return EXEC_FAILURE;
  }
  else{
    armModel.setJointRotations(angles);
    return EXEC_SUCCESS;
  }
}

//calculates the change in each coordinate to obtain p2 from p1
float[] calculateVectorDelta(PVector p1, PVector p2){
  float[] d = {p1.x - p2.x, p1.y - p2.y, p1.z - p2.z};
  return d;
}

//calculate the dot product of two vectors represented by float arrays
float calculateVectorDot(float[] v1, float[] v2){
  float dot = v1[0]*v2[0] + v1[1]*v2[1] + v1[2]*v2[2];
  return dot;
}

/**
 * Determine how close together intermediate points between two points
 * need to be based on current speed
 */
void calculateDistanceBetweenPoints() {
  MotionInstruction instruction =
    (MotionInstruction)currentProgram.getInstructions().get(currentInstruction);
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
void calculateIntermediatePositions(PVector start, PVector end) {
  calculateDistanceBetweenPoints();
  intermediatePositions.clear();
  float mu = 0;
  int numberOfPoints = (int)
    (dist(start.x, start.y, start.z, end.x, end.y, end.z) / distanceBetweenPoints);
  float increment = 1.0 / (float)numberOfPoints;
  for (int n = 0; n < numberOfPoints; n++) {
    mu += increment;
    intermediatePositions.add(new PVector(
      start.x * (1 - mu) + (end.x * mu),
      start.y * (1 - mu) + (end.y * mu),
      start.z * (1 - mu) + (end.z * mu)));
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
void calculateContinuousPositions(PVector p1, PVector p2, PVector p3, float percentage) {
  //percentage /= 2;
  calculateDistanceBetweenPoints();
  percentage /= 1.5;
  percentage = 1 - percentage;
  percentage = constrain(percentage, 0, 1);
  intermediatePositions.clear();
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
    intermediatePositions.add(new PVector(
      p1.x * (1 - mu) + (p2.x * mu),
      p1.y * (1 - mu) + (p2.y * mu),
      p1.z * (1 - mu) + (p2.z * mu)));
  }
  int secondaryIdx = 0; // accessor for secondary targets
  mu = 0;
  increment /= 2.0;
  PVector currentPoint = intermediatePositions.get(intermediatePositions.size()-1);
  for (int n = transitionPoint; n < numberOfPoints; n++) {
    mu += increment;
    intermediatePositions.add(new PVector(
      currentPoint.x * (1 - mu) + (secondaryTargets.get(secondaryIdx).x * mu),
      currentPoint.y * (1 - mu) + (secondaryTargets.get(secondaryIdx).y * mu),
      currentPoint.z * (1 - mu) + (secondaryTargets.get(secondaryIdx).z * mu)));
    currentPoint = intermediatePositions.get(intermediatePositions.size()-1);
    secondaryIdx++;
  }
  interMotionIdx = 0;
} // end calculate continuous positions

/**
 * Initiate a new continuous (curved) motion instruction.
 * @param model Arm model to use
 * @param start Start point
 * @param end Destination point
 * @param next Point after the destination
 * @param percentage Intensity of the curve
 */
void beginNewContinuousMotion(ArmModel model, PVector start, PVector end,
                              PVector next, float percentage)
{
  calculateContinuousPositions(start, end, next, percentage);
  motionFrameCounter = 0;
  calculateIKJacobian(intermediatePositions.get(interMotionIdx));
}

/**
 * Initiate a new fine (linear) motion instruction.
 * @param start Start point
 * @param end Destination point
 */
void beginNewLinearMotion(ArmModel model, PVector start, PVector end) {
  calculateIntermediatePositions(start, end);
  motionFrameCounter = 0;
  calculateIKJacobian(intermediatePositions.get(interMotionIdx));
}

/**
 * Initiate a new circular motion instruction according to FANUC methodology.
 * @param p1 Point 1
 * @param p2 Point 2
 * @param p3 Point 3
 */
void beginNewCircularMotion(ArmModel model, PVector p1, PVector p2, PVector p3) {
  // Generate the circle circumference,
  // then turn it into an arc from the current point to the end point
  intermediatePositions = createArc(createCircleCircumference(p1, p2, p3, 180), p1, p2, p3);
  interMotionIdx = 0;
  motionFrameCounter = 0;
  calculateIKJacobian(intermediatePositions.get(interMotionIdx));
}

boolean executingInstruction = false;

/**
 * Prepare a new program for execution.
 */
void readyProgram() {
  currentInstruction = 0;
  executingInstruction = false;
  doneMoving = false;
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
    model.instantRotation();
    interMotionIdx++;
    motionFrameCounter = 0;
    if (interMotionIdx >= intermediatePositions.size()) {
      interMotionIdx = -1;
      return true;
    }
    calculateIKJacobian(intermediatePositions.get(interMotionIdx));
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
  PVector x = n1.get();
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
 * Create points around the circumference of a circle calculated from
 * three input points.
 * @param a First point
 * @param b Second point
 * @param c Third point
 * @param numPoints Number of points to place on the circle circumference
 * @return List of points comprising a circle circumference that intersects
 *         the three input points.
 */
ArrayList<PVector> createCircleCircumference(PVector a,
                                      PVector b,
                                      PVector c,
                                      int numPoints)
{  
  // First, we need to compute the value of some variables that we'll
  // use in a parametric equation to get our answer.
  // First up is computing the circle center. This is much easier to
  // do in 2D, so first we'll convert our three input points into a 2D
  // plane, compute the circle center in those coordinates, then convert
  // back to our native 3D frame.
  PVector[] plane = new PVector[3];
  plane = createPlaneFrom3Points(a, b, c);
  PVector center = circleCenter(vectorConvertTo(a, plane[0], plane[1], plane[2]),
                                vectorConvertTo(b, plane[0], plane[1], plane[2]),
                                vectorConvertTo(c, plane[0], plane[1], plane[2]));
  center = vectorConvertFrom(center, plane[0], plane[1], plane[2]);
  // Now get the radius (easy)
  float r = dist(center.x, center.y, center.z, a.x, a.y, a.z);
  // Get u (a unit vector from the center to some point on the circumference)
  PVector u = new PVector(center.x-a.x, center.y-a.y, center.z-a.z);
  u.normalize();
  // get n (a normal of the plane created by the 3 input points)
  PVector tmp1 = new PVector(a.x-b.x, a.y-b.y, a.z-b.z);
  PVector tmp2 = new PVector(a.x-c.x, a.y-c.y, a.z-c.z);
  PVector n = tmp1.cross(tmp2);
  n.normalize();
  
  // Now plug all that into the parametric equation
  //   P = r*cos(t)*u + r*sin(t)*nxu+center [x is cross product]
  // to compute our points along the circumference.
  // We actually only want to create an arc from A to C, not the full
  // circle, so detect when we're close to those points to decide
  // when to start and stop adding points.
  float angle = 0;
  float angleInc = (TWO_PI)/(float)numPoints;
  ArrayList<PVector> points = new ArrayList<PVector>();
  boolean start = false, grace = false;
  for (int iter = 0; iter < numPoints; iter++) {
    PVector inter1 = PVector.mult(u, r * cos(angle));
    PVector inter2 =  n.cross(u);
    inter2 = PVector.mult(inter2, r * sin(angle));
    inter1.add(inter2);
    inter1.add(center);
    points.add(inter1);
    angle += angleInc;
  }
  return points;
}


/**
 * Helper method for the createArc method
 */
int cycleNumber(int number) {
  number++;
  if (number >= 4) number = 1;
  return number;
}


/**
 * Takes a list of points describing a circle circumference, three points
 * A, B, and C, and returns an arc built from the circumference that
 * runs from A to B to C.
 * @param points List of points describing the circle circumference.
 * @param a Point A
 * @param b Point b
 * @param c Point C
 * @return List of points describing the arc from A to B to C
 */
ArrayList<PVector> createArc(ArrayList<PVector> points, PVector a, PVector b, PVector c) {
  float CHKDIST = 15.0;
  while (true) {
    int seenA = 0, seenB = 0, seenC = 0, currentSee = 1;
    for (int n = 0; n < points.size(); n++) {
      PVector pt = points.get(n);
      if (dist(pt.x, pt.y, pt.z, a.x, a.y, a.z) <= CHKDIST) seenA = currentSee++;
      if (dist(pt.x, pt.y, pt.z, b.x, b.y, b.z) <= CHKDIST) seenB = currentSee++;
      if (dist(pt.x, pt.y, pt.z, c.x, c.y, c.z) <= CHKDIST) seenC = currentSee++;
    }
    while (seenA != 1) {
      seenA = cycleNumber(seenA);
      seenB = cycleNumber(seenB);
      seenC = cycleNumber(seenC);
    }
    // detect reverse case: if b > c then we're going the wrong way, so reverse
    if (seenB > seenC) {
      Collections.reverse(points);
      continue;
    }
    break;
  } // end while loop
  
  // now we're going in the right direction, so remove unnecessary points
  ArrayList<PVector> newPoints = new ArrayList<PVector>();
  boolean seenA = false, seenC = false;
  for (PVector pt : points) {
    if (seenA && !seenC) newPoints.add(pt);
    if (dist(pt.x, pt.y, pt.z, a.x, a.y, a.z) <= CHKDIST) seenA = true;
    if (seenA && dist(pt.x, pt.y, pt.z, c.x, c.y, c.z) <= CHKDIST) {
      seenC = true;
      break;
    }
  }
  // might have to go through a second time
  if (seenA && !seenC) {
    for (PVector pt : points) {
      newPoints.add(pt);
      if (dist(pt.x, pt.y, pt.z, c.x, c.y, c.z) <= CHKDIST) break;
    }
  }
  if (newPoints.size() > 0) newPoints.remove(0);
  newPoints.add(c);
  return newPoints;
} // end createArc


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
boolean executeProgram(Program program, ArmModel model) {
  if (program == null || currentInstruction >= program.getInstructions().size())
    return true;
  Instruction ins = program.getInstructions().get(currentInstruction);
  if (ins instanceof MotionInstruction) {
    MotionInstruction instruction = (MotionInstruction)ins;
    if (instruction.getUserFrame() != activeUserFrame) {
      setError("ERROR: Instruction's user frame is different from currently active user frame.");
      return true;
    }
    if (!executingInstruction) { // start executing new instruction
      if (setUpInstruction(program, model, instruction)) return true;
      executingInstruction = true;
      
    } else { // continue executing current instruction
      
      if (instruction.getMotionType() != MTYPE_JOINT)
        executingInstruction = !(executeMotion(model, instruction.getSpeedForExec(model)));
      else executingInstruction =
        !(model.interpolateRotation(instruction.getSpeedForExec(model)));
      if (!executingInstruction) {
        currentInstruction++;
        if (currentInstruction >= program.getInstructions().size()) return true;
      }
    }
  } else if (ins instanceof ToolInstruction) {
    ToolInstruction instruction = (ToolInstruction)ins;
    instruction.execute();
    currentInstruction++;
    if (currentInstruction >= program.getInstructions().size()) return true;
  } else if (ins instanceof FrameInstruction) {
    FrameInstruction instruction = (FrameInstruction)ins;
    instruction.execute();
    currentInstruction++;
    if (currentInstruction >= program.getInstructions().size()) return true;
  } // end of instruction type check
  return false;
} // end executeProgram


/**
 * Executes a single instruction. Returns true when done.
 * @param ins Instruction to execute.
 * @return Finished yet (false=no, true=yes)
 */
boolean executeSingleInstruction(Instruction ins) {
  if (ins instanceof MotionInstruction) {
    MotionInstruction instruction = (MotionInstruction)ins;
    if (instruction.getMotionType() != MTYPE_JOINT) {
      if (instruction.getUserFrame() != activeUserFrame) {
        setError("ERROR: Instruction's user frame is different from currently active user frame.");
        return true;
      }
      return executeMotion(armModel, instruction.getSpeedForExec(armModel));
    } else return armModel.interpolateRotation(instruction.getSpeedForExec(armModel));
  } else if (ins instanceof ToolInstruction) {
    ToolInstruction instruction = (ToolInstruction)ins;
    instruction.execute();
    return true;
  } else if (ins instanceof FrameInstruction) {
    FrameInstruction instruction = (FrameInstruction)ins;
    instruction.execute();
    return true;
  }
  return true;
}

/**
 * Sets up an instruction for execution.
 * @param program Program that the instruction belongs to
 * @param model Arm model to use
 * @param instruction The instruction to execute
 * @return Returns true on failure (invalid instruction), false on success
 */
boolean setUpInstruction(Program program, ArmModel model, MotionInstruction instruction) {
      PVector start = calculateEndEffectorPosition(model, armModel.getJointRotations());
      if (instruction.getMotionType() == MTYPE_JOINT) {
        float[] j = instruction.getVector(program).j;
        for (int n = 0; n < j.length; n++) {
          for (int r = 0; r < 3; r++) {
            if (model.segments.get(n).rotations[r])
              model.segments.get(n).targetRotations[r] = j[n];
          }
        }
        // calculate whether it's faster to turn CW or CCW
        for (Model a : model.segments) {
          for (int r = 0; r < 3; r++) {
            if (a.rotations[r]) {
             
             // The minimum distance between the current and target joint angles
             float dist_t = minimumDistance(a.currentRotations[r], a.targetRotations[r]);
             
             if (a.jointRanges[r].x == 0 && a.jointRanges[r].y == TWO_PI) {
               
               // Joint has full range of motion
               a.rotationDirections[r] = (dist_t < 0) ? -1 : 1;
             } else {
               
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
                 } else {
                   a.rotationDirections[r] = -1;
                 }
               } else if (dist_t > 0) {
               
                 if ( (dist_lb > 0 && dist_lb < dist_t) || (dist_ub > 0 && dist_ub < dist_t) ) {
                   
                   // One or both bounds lie within the shortest path
                   a.rotationDirections[r] = -1;
                 } else {
                   a.rotationDirections[r] = 1;
                 }
               }
             }
             
             /*float blueAngle = a.targetRotations[r] - a.currentRotations[r];
              blueAngle = clampAngle(blueAngle);
              if (blueAngle < PI) a.rotationDirections[r] = 1;
              else a.rotationDirections[r] = -1;*/
            }
          }
        }
      } else if (instruction.getMotionType() == MTYPE_LINEAR) {
        if (instruction.getTermination() == 0) {
          beginNewLinearMotion(model, start, instruction.getVector(program).c);
        } else {
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
            beginNewLinearMotion(model, start, instruction.getVector(program).c);
          } else beginNewContinuousMotion(model, start, instruction.getVector(program).c,
                                          nextPoint.c, instruction.getTermination());
        } // end if termination type is continuous
      } else if (instruction.getMotionType() == MTYPE_CIRCULAR) {
        // If it is a circular instruction, the current instruction holds the intermediate point.
        // There must be another instruction after this that holds the end point.
        // If this isn't the case, the instruction is invalid, so return immediately.
        Point nextPoint = null;
        if (program.getInstructions().size() >= currentInstruction + 2) {
          Instruction nextIns = program.getInstructions().get(currentInstruction+1);
          if (!(nextIns instanceof MotionInstruction)) return true;
          else {
            MotionInstruction castIns = (MotionInstruction)nextIns;
            nextPoint = castIns.getVector(program);
          }
        } else return true; // invalid instruction
        beginNewCircularMotion(model, start, instruction.getVector(program).c, nextPoint.c);
        
      } // end if motion type is circular
      return false;
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
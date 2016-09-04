/**
 * A class designed which contains the camera transformation values
 * and the methods to manipulate apply the Camera's transformation.
 */
public class Camera {
  private PVector position,
                  // Rotations in X, Y, Z in radians
                  orientation;
  private static final float MAX_SCALE = 8f;
  private float scale;
  
  /**
   * Creates a camera with the default position, orientation and scale.
   */
  public Camera() {
    position = new PVector(0f, 0f, -500f);
    orientation = new PVector(0f, 0f, 0f);
    scale = 2f;
  }
  
  /**
   * Apply the camer's scale, position, and orientation to the current matrix.
   */
  public void apply() {
    beginCamera();
    // Apply camera translations
    translate(position.x + width / 2f, position.y + height / 2f, position.z);
    
    // Apply camera rotations
    rotateX(orientation.x);
    rotateY(orientation.y);
    
     // Apply camera scaling
    float horizontalMargin = scale * width / 2f,
          verticalMargin = scale * height / 2f,
          near = MAX_SCALE / scale,
          far = scale * 5000f;
    ortho(-horizontalMargin, horizontalMargin, -verticalMargin, verticalMargin, near, far);
    
    endCamera();
  }
  
  /**
   * Return the camera perspective to the
   * default position, orientation and scale.
   */
  public void reset() {
    position.x = 0;
    position.y = 0;
    position.z = -500f;
    orientation.x = 0f;
    orientation.y = 0f;
    orientation.z = 0f;
    scale = 2f;
  }
  
  /**
   * Change the camera's position by the given values.
   */
  public void move(float x, float y, float z) {
    float horzontialLimit = MAX_SCALE * width / 3f,
          verticalLimit = MAX_SCALE * height / 3f;
    
    position.add( new PVector(x, y, z) );
    // Apply camera position restrictions
    position.x = max(-horzontialLimit, min(position.x, horzontialLimit));
    position.y = max(-verticalLimit, min(position.y, verticalLimit));
    position.z = max(-1000f, min(position.z, 1000f));
  }
  
  /**
   * Change the camera's rotation by the given values.
   */
  public void rotate(float w, float p, float r) {
    PVector rotation = new PVector(w, p, r);
    
    orientation.add( rotation );
    // Apply caerma rotation restrictions
    orientation.x = mod2PI(orientation.x);
    orientation.y = mod2PI(orientation.y);
    orientation.z = 0f;//mod2PI(orientation.z);
  }
  
  /**
   * Change the scaling of the camera.
   */
  public void changeScale(float multiplier) {
    scale = max(0.25f, min(scale * multiplier, MAX_SCALE));
  }
  
  /**
   * Returns the Camera's position, orientation, and scale
   * in the form of a formatted String array, where each
   * entry is one of the following values:
   * 
   * Title String
   * X - The camera's x -position value
   * Y - The camera's y-position value
   * Z - The camera's z-position value
   * W - The camera's x-rotation value
   * P - The camera's y-rotation value
   * R - The camera's z-rotation value
   * S - The camera's scale value
   * 
   * @returning  A 6-element String array
   */
  public String[] toStringArray() {
    String[] fields = new String[8];
    // Display rotation in degrees
    PVector inDegrees = PVector.mult(orientation, RAD_TO_DEG);
    
    fields[0] = "Camera Fields";
    fields[1] = String.format("X: %6.9f", position.x);
    fields[2] = String.format("Y: %6.9f", position.y);
    fields[3] = String.format("Z: %6.9f", position.z);
    fields[4] = String.format("W: %6.9f", inDegrees.x);
    fields[5] = String.format("P: %6.9f", inDegrees.y);
    fields[6] = String.format("R: %6.9f", inDegrees.z);
    fields[7] = String.format("S: %3.9f", scale);
    
    return fields;
  }
  
  /**
   * Returns an independent replica of the Camera object.
   */
  public Camera clone() {
    Camera copy = new Camera();
    // Copy position, orientation, and scale
    copy.position = position.copy();
    copy.orientation = orientation.copy();
    copy.scale = scale;
    
    return copy;
  }
  
  // Getters for the Camera's position, orientation, and scale
  public PVector getPosition() { return position; }
  public PVector getOrientation() { return orientation; }
  public float getScale() { return scale; }
}


/**
 * Applies the rotations and translations of the Robot Arm to get to the
 * face plate center, given the set of six joint angles, each corresponding
 * to a joint of the Robot Arm and each within the bounds of [0, TWO_PI).
 * 
 * @param jointAngles  A valid set of six joint angles (in radians) for the Robot
 */
public void applyModelRotation(float[] jointAngles) {
  translate(ROBOT_POSITION.x, ROBOT_POSITION.y, ROBOT_POSITION.z);
  
  translate(-50, -166, -358); // -115, -213, -413
  rotateZ(PI);
  translate(150, 0, 150);
  rotateX(PI);
  rotateY(jointAngles[0]);
  rotateX(-PI);
  translate(-150, 0, -150);
  rotateZ(-PI);    
  translate(-115, -85, 180);
  rotateZ(PI);
  rotateY(PI/2);
  translate(0, 62, 62);
  rotateX(jointAngles[1]);
  translate(0, -62, -62);
  rotateY(-PI/2);
  rotateZ(-PI);   
  translate(0, -500, -50);
  rotateZ(PI);
  rotateY(PI/2);
  translate(0, 75, 75);
  rotateZ(PI);
  rotateX(jointAngles[2]);
  rotateZ(-PI);
  translate(0, -75, -75);
  rotateY(PI/2);
  rotateZ(-PI);
  translate(745, -150, 150);
  rotateZ(PI/2);
  rotateY(PI/2);
  translate(70, 0, 70);
  rotateY(jointAngles[3]);
  translate(-70, 0, -70);
  rotateY(-PI/2);
  rotateZ(-PI/2);    
  translate(-115, 130, -124);
  rotateZ(PI);
  rotateY(-PI/2);
  translate(0, 50, 50);
  rotateX(jointAngles[4]);
  translate(0, -50, -50);
  rotateY(PI/2);
  rotateZ(-PI);    
  translate(150, -10, 95);
  rotateY(-PI/2);
  rotateZ(PI);
  translate(45, 45, 0);
  rotateZ(jointAngles[5]);
}

/**
 * Converts the given point, pt, into the Coordinate System defined by the given origin
 * vector and rotation quaternion axes.
 * 
 * @param pt      A point with initialized position and orientation
 * @param origin  The origin of the Coordinate System
 * @param axes    The axes of the Coordinate System representing as a rotation quanternion
 * @returning     The point, pt, interms of the given frame's Coordinate System
 */
public Point applyFrame(Point pt, PVector origin, RQuaternion axes) {
  PVector position = convertToFrame(pt.position, origin, axes);
  RQuaternion orientation = axes.transformQuaternion(pt.orientation);
  
  return new Point(position, orientation, pt.angles);
}

/**
 * Converts the given vector, v, into the Coordinate System defined by the given origin
 * vector and rotation quaternion axes.
 * 
 * @param v      A vector in the XYZ vector space
 * @param origin  The origin of the Coordinate System
 * @param axes    The axes of the Coordinate System representing as a rotation quanternion
 * @returning     The vector, v, interms of the given frame's Coordinate System
 */
public PVector convertToFrame(PVector v, PVector origin, RQuaternion axes) {
  PVector vOffset = PVector.sub(v, origin);
    return axes.rotateVector(vOffset);
}

/**
 * Converts the given point, pt, from the Coordinate System defined by the given origin
 * vector and rotation quaternion axes.
 * 
 * @param pt      A point with initialized position and orientation
 * @param origin  The origin of the Coordinate System
 * @param axes    The axes of the Coordinate System representing as a rotation quanternion
 * @returning     The point, pt, interms of the given frame's Coordinate System
 */
public Point removeFrame(Point pt, PVector origin, RQuaternion axes) {
  PVector position = convertFromFrame(pt.position, origin, axes);
  RQuaternion orientation = RQuaternion.mult(pt.orientation, axes);
  
  return new Point(position, orientation, pt.angles);
}

/**
 * Converts the given vector, u, from the Coordinate System defined by the given origin
 * vector and rotation quaternion axes.
 * 
 * @param v       A vector in the XYZ vector space
 * @param origin  The origin of the Coordinate System
 * @param axes    The axes of the Coordinate System representing as a rotation quanternion
 * @returning     The vector, u, in the Native frame
 */
public PVector convertFromFrame(PVector u, PVector origin, RQuaternion axes) {
  RQuaternion invAxes = axes.conjugate();
  invAxes.normalize();
  PVector vRotated = invAxes.rotateVector(u);
  return vRotated.add(origin);
}

/**
 * Converts the given vector form the right-hand World Frame Coordinate System
 * to the left-hand Native Coordinate System.
 */
public PVector convertWorldToNative(PVector v) {
  float[][] tMatrix = transformationMatrix(new PVector(0f, 0f, 0f), WORLD_AXES);
  return transformVector(v, invertHCMatrix(tMatrix));
}

/**
 * Converts the given vector form the left-hand Native Coordinate System to the
 * right-hand World Frame Coordinate System.
 */
public PVector convertNativeToWorld(PVector v) {
  float[][] tMatrix = transformationMatrix(new PVector(0f, 0f, 0f), WORLD_AXES);
  return transformVector(v, tMatrix);
}

/* Transforms the given vector from the coordinate system defined by the given
 * transformation matrix (row major order). */
public PVector transformVector(PVector v, float[][] tMatrix) {
  if(tMatrix.length != 4 || tMatrix[0].length != 4) {
    return null;
  }

  PVector u = new PVector();
  // Apply the transformation matrix to the given vector
  u.x = v.x * tMatrix[0][0] + v.y * tMatrix[1][0] + v.z * tMatrix[2][0] + tMatrix[0][3];
  u.y = v.x * tMatrix[0][1] + v.y * tMatrix[1][1] + v.z * tMatrix[2][1] + tMatrix[1][3];
  u.z = v.x * tMatrix[0][2] + v.y * tMatrix[1][2] + v.z * tMatrix[2][2] + tMatrix[2][3];

  return u;
}

/* Transforms the given vector by the given 3x3 rotation matrix (row major order). */
public PVector rotateVector(PVector v, float[][] rotMatrix) {
  if(v == null || rotMatrix == null || rotMatrix.length != 3 || rotMatrix[0].length != 3) {
    return null;
  }
  
  PVector u = new PVector();
  // Apply the rotation matrix to the given vector
  u.x = v.x * rotMatrix[0][0] + v.y * rotMatrix[1][0] + v.z * rotMatrix[2][0];
  u.y = v.x * rotMatrix[0][1] + v.y * rotMatrix[1][1] + v.z * rotMatrix[2][1];
  u.z = v.x * rotMatrix[0][2] + v.y * rotMatrix[1][2] + v.z * rotMatrix[2][2];
  
  return u;
}

/**
 * Find the inverse of the given 4x4 Homogeneous Coordinate Matrix. 
 * 
 * This method is based off of the algorithm found on this webpage:
 *    https://web.archive.org/web/20130806093214/http://www-graphics.stanford.edu/
 *      courses/cs248-98-fall/Final/q4.html
 */
public float[][] invertHCMatrix(float[][] m) {
  if(m.length != 4 || m[0].length != 4) {
    return null;
  }

  float[][] inverse = new float[4][4];

  /* [ ux vx wx tx ] -1       [ ux uy uz -dot(u, t) ]
   * [ uy vy wy ty ]     =    [ vx vy vz -dot(v, t) ]
   * [ uz vz wz tz ]          [ wx wy wz -dot(w, t) ]
   * [  0  0  0  1 ]          [  0  0  0      1     ]
   */
  inverse[0][0] = m[0][0];
  inverse[0][1] = m[1][0];
  inverse[0][2] = m[2][0];
  inverse[0][3] = -(m[0][0] * m[0][3] + m[0][1] * m[1][3] + m[0][2] * m[2][3]);
  inverse[1][0] = m[0][1];
  inverse[1][1] = m[1][1];
  inverse[1][2] = m[2][1];
  inverse[1][3] = -(m[1][0] * m[0][3] + m[1][1] * m[1][3] + m[1][2] * m[2][3]);
  inverse[2][0] = m[0][2];
  inverse[2][1] = m[1][2];
  inverse[2][2] = m[2][2];
  inverse[2][3] = -(m[2][0] * m[0][3] + m[2][1] * m[1][3] + m[2][2] * m[2][3]);
  inverse[3][0] = 0;
  inverse[3][1] = 0;
  inverse[3][2] = 0;
  inverse[3][3] = 1;

  return inverse;
}

/* Returns a 4x4 vector array which reflects the current transform matrix on the top
 * of the stack (ignores scaling values though) */
public float[][] getTransformationMatrix() {
  float[][] transform = new float[4][4];

  // Caculate four vectors corresponding to the four columns of the transform matrix
  PVector origin = getCoordFromMatrix(0, 0, 0);
  PVector xAxis = getCoordFromMatrix(1, 0, 0).sub(origin);
  PVector yAxis = getCoordFromMatrix(0, 1, 0).sub(origin);
  PVector zAxis = getCoordFromMatrix(0, 0, 1).sub(origin);

  // Place the values of each vector in the correct cells of the transform  matrix
  transform[0][0] = xAxis.x;
  transform[0][1] = xAxis.y;
  transform[0][2] = xAxis.z;
  transform[0][3] = origin.x;
  transform[1][0] = yAxis.x;
  transform[1][1] = yAxis.y;
  transform[1][2] = yAxis.z;
  transform[1][3] = origin.y;
  transform[2][0] = zAxis.x;
  transform[2][1] = zAxis.y;
  transform[2][2] = zAxis.z;
  transform[2][3] = origin.z;
  transform[3][0] = 0;
  transform[3][1] = 0;
  transform[3][2] = 0;
  transform[3][3] = 1;

  return transform;
}

/**
 * Forms the 4x4 transformation matrix (row major order) form the given
 * origin offset and axes offset (row major order) of the Native Coordinate
 * system.
 * 
 * @param origin  the X, Y, Z, offset of the origin for the Coordinate frame
 * @param axes    a 3x3 rotatin matrix (row major order) representing the unit
 *                vector axes offset of the new Coordinate Frame from the Native
 *                Coordinate Frame
 * @returning     the 4x4 transformation matrix (column major order) formed from
 *                the given origin and axes offset
 */
public float[][] transformationMatrix(PVector origin, float[][] axes) {
  float[][] transform = new float[4][4];
  
  transform[0][0] = axes[0][0];
  transform[1][0] = axes[1][0];
  transform[2][0] = axes[2][0];
  transform[3][0] = 0;
  transform[0][1] = axes[0][1];
  transform[1][1] = axes[1][1];
  transform[2][1] = axes[2][1];
  transform[3][1] = 0;
  transform[0][2] = axes[0][2];
  transform[1][2] = axes[1][2];
  transform[2][2] = axes[2][2];
  transform[3][2] = 0;
  transform[0][3] = origin.x;
  transform[1][3] = origin.y;
  transform[2][3] = origin.z;
  transform[3][3] = 1;
  
  return transform;
}

/**
 * Returns a 3x3 rotation matrix of the current transformation
 * matrix on the stack (in row major order).
 */
public float[][] getRotationMatrix() {
  float[][] rMatrix = new float[3][3];
  // Calculate origin point
  PVector origin = getCoordFromMatrix(0f, 0f, 0f),
          // Create axes vectors
          vx = getCoordFromMatrix(1f, 0f, 0f).sub(origin),
          vy = getCoordFromMatrix(0f, 1f, 0f).sub(origin),
          vz = getCoordFromMatrix(0f, 0f, 1f).sub(origin);
  // Save values in a 3x3 rotation matrix
  rMatrix[0][0] = vx.x;
  rMatrix[0][1] = vx.y;
  rMatrix[0][2] = vx.z;
  rMatrix[1][0] = vy.x;
  rMatrix[1][1] = vy.y;
  rMatrix[1][2] = vy.z;
  rMatrix[2][0] = vz.x;
  rMatrix[2][1] = vz.y;
  rMatrix[2][2] = vz.z;
  
  return rMatrix;
}

/* This method transforms the given coordinates into a vector
 * in the Processing's native coordinate system. */
public PVector getCoordFromMatrix(float x, float y, float z) {
  PVector vector = new PVector();

  vector.x = modelX(x, y, z);
  vector.y = modelY(x, y, z);
  vector.z = modelZ(x, y, z);

  return vector;
}

/* Calculate v x v */
public float[] crossProduct(float[] v, float[] u) {
  if(v.length != 3 && v.length != u.length) { return null; }
  
  float[] w = new float[v.length];
  // [a, b, c] x [d, e, f] = [ bf - ce, cd - af, ae - bd ]
  w[0] = v[1] * u[2] - v[2] * u[1];
  w[1] = v[2] * u[0] - v[0] * u[2];
  w[2] = v[0] * u[1] - v[1] * u[0];
  
  return w;
}

/* Returns a vector with the opposite sign
 * as the given vector. */
public float[] negate(float[] v) {
  float[] u = new float[v.length];
  
  for(int e = 0; e < v.length; ++e) {
    u[e] = -v[e];
  }
  
  return u;
}

//calculates rotation matrix from euler angles
float[][] eulerToMatrix(PVector wpr) {
  float[][] r = new float[3][3];
  float xRot = wpr.x;
  float yRot = wpr.y;
  float zRot = wpr.z;
  
  r[0][0] = cos(yRot)*cos(zRot);
  r[0][1] = sin(xRot)*sin(yRot)*cos(zRot) - cos(xRot)*sin(zRot);
  r[0][2] = cos(xRot)*sin(yRot)*cos(zRot) + sin(xRot)*sin(zRot);
  r[1][0] = cos(yRot)*sin(zRot);
  r[1][1] = sin(xRot)*sin(yRot)*sin(zRot) + cos(xRot)*cos(zRot);
  r[1][2] = cos(xRot)*sin(yRot)*sin(zRot) - sin(xRot)*cos(zRot);
  r[2][0] = -sin(yRot);
  r[2][1] = sin(xRot)*cos(yRot);
  r[2][2] = cos(xRot)*cos(yRot);
  
  float[] magnitudes = new float[3];
  
  for(int v = 0; v < r.length; ++v) {
    // Find the magnitude of each axis vector
    for(int e = 0; e < r[0].length; ++e) {
      magnitudes[v] += pow(r[v][e], 2);
    }
    
    magnitudes[v] = sqrt(magnitudes[v]);
    // Normalize each vector
    for(int e = 0; e < r.length; ++e) {
      r[v][e] /= magnitudes[v];
    }
  }
  /**/

  return r;
}

/**
 * Converts the given Euler angle set values to a quaternion
 */
RQuaternion eulerToQuat(PVector wpr) {
  float w, x, y, z;
  float xRot = wpr.x;
  float yRot = wpr.y;
  float zRot = wpr.z;
  
  w = cos(xRot/2)*cos(yRot/2)*cos(zRot/2) + sin(xRot/2)*sin(yRot/2)*sin(zRot/2);
  x = sin(xRot/2)*cos(yRot/2)*cos(zRot/2) - cos(xRot/2)*sin(yRot/2)*sin(zRot/2);
  y = cos(xRot/2)*sin(yRot/2)*cos(zRot/2) + sin(xRot/2)*cos(yRot/2)*sin(zRot/2);
  z = cos(xRot/2)*cos(yRot/2)*sin(zRot/2) - sin(xRot/2)*sin(yRot/2)*cos(zRot/2);
  
  return new RQuaternion(w, x, y, z);
}

//calculates euler angles from rotation matrix
PVector matrixToEuler(float[][] r) {
  float yRot1, yRot2, xRot1, xRot2, zRot1, zRot2;
  PVector wpr, wpr2;

  if(r[2][0] != 1 && r[2][0] != -1) {
    //rotation about y-axis
    yRot1 = -asin(r[2][0]);
    yRot2 = PI - yRot1;
    //rotation about x-axis
    xRot1 = atan2(r[2][1]/cos(yRot1), r[2][2]/cos(yRot1));
    xRot2 = atan2(r[2][1]/cos(yRot2), r[2][2]/cos(yRot2));
    //rotation about z-axis
    zRot1 = atan2(r[1][0]/cos(yRot1), r[0][0]/cos(yRot1));
    zRot2 = atan2(r[1][0]/cos(yRot2), r[0][0]/cos(yRot2));
  } else {
    zRot1 = zRot2 = 0;
    if(r[2][0] == -1) {
      yRot1 = yRot2 = PI/2;
      xRot1 = xRot2 = zRot1 + atan2(r[0][1], r[0][2]);
    } else {
      yRot1 = yRot2 = -PI/2;
      xRot1 = xRot2 = -zRot1 + atan2(-r[0][1], -r[0][2]);
    }
  }

  wpr = new PVector(xRot1, yRot1, zRot1);
  wpr2 = new PVector(xRot2, yRot2, zRot2);

  return wpr;
}

//calculates quaternion from rotation matrix
RQuaternion matrixToQuat(float[][] r) {
  float[] limboQ = new float[4];
  float tr = r[0][0] + r[1][1] + r[2][2];

  if(tr > 0) {
    float S = sqrt(1.0 + tr) * 2; // S=4*q[0] 
    limboQ[0] = S / 4;
    limboQ[1] = (r[2][1] - r[1][2]) / S;
    limboQ[2] = (r[0][2] - r[2][0]) / S; 
    limboQ[3] = (r[1][0] - r[0][1]) / S;
  } else if((r[0][0] > r[1][1]) & (r[0][0] > r[2][2])) {
    float S = sqrt(1.0 + r[0][0] - r[1][1] - r[2][2]) * 2; // S=4*q[1] 
    limboQ[0] = (r[2][1] - r[1][2]) / S;
    limboQ[1] = S / 4;
    limboQ[2] = (r[0][1] + r[1][0]) / S; 
    limboQ[3] = (r[0][2] + r[2][0]) / S;
  } else if(r[1][1] > r[2][2]) {
    float S = sqrt(1.0 + r[1][1] - r[0][0] - r[2][2]) * 2; // S=4*q[2]
    limboQ[0] = (r[0][2] - r[2][0]) / S;
    limboQ[1] = (r[0][1] + r[1][0]) / S; 
    limboQ[2] = S / 4;
    limboQ[3] = (r[1][2] + r[2][1]) / S;
  } else {
    float S = sqrt(1.0 + r[2][2] - r[0][0] - r[1][1]) * 2; // S=4*q[3]
    limboQ[0] = (r[1][0] - r[0][1]) / S;
    limboQ[1] = (r[0][2] + r[2][0]) / S;
    limboQ[2] = (r[1][2] + r[2][1]) / S;
    limboQ[3] = S / 4;
  }
  
  RQuaternion q = new RQuaternion(limboQ[0], limboQ[1], limboQ[2], limboQ[3]);
  q.normalize();
  
  return q;
}

//calculates euler angles from quaternion
PVector quatToEuler(RQuaternion q) {
  float[][] r = q.toMatrix();
  PVector wpr = matrixToEuler(r);
  return wpr;
}

//calculates rotation matrix from quaternion
float[][] quatToMatrix(RQuaternion q) {
  float[][] r = new float[3][3];
  
  r[0][0] = 1 - 2*(q.getValue(2)*q.getValue(2) + q.getValue(3)*q.getValue(3));
  r[0][1] = 2*(q.getValue(1)*q.getValue(2) - q.getValue(0)*q.getValue(3));
  r[0][2] = 2*(q.getValue(0)*q.getValue(2) + q.getValue(1)*q.getValue(3));
  r[1][0] = 2*(q.getValue(1)*q.getValue(2) + q.getValue(0)*q.getValue(3));
  r[1][1] = 1 - 2*(q.getValue(1)*q.getValue(1) + q.getValue(3)*q.getValue(3));
  r[1][2] = 2*(q.getValue(2)*q.getValue(3) - q.getValue(0)*q.getValue(1));
  r[2][0] = 2*(q.getValue(1)*q.getValue(3) - q.getValue(0)*q.getValue(2));
  r[2][1] = 2*(q.getValue(0)*q.getValue(1) + q.getValue(2)*q.getValue(3));
  r[2][2] = 1 - 2*(q.getValue(1)*q.getValue(1) + q.getValue(2)*q.getValue(2));
  
  float[] magnitudes = new float[3];
  
  for(int v = 0; v < r.length; ++v) {
    // Find the magnitude of each axis vector
    for(int e = 0; e < r[0].length; ++e) {
      magnitudes[v] += pow(r[v][e], 2);
    }
    
    magnitudes[v] = sqrt(magnitudes[v]);
    // Normalize each vector
    for(int e = 0; e < r.length; ++e) {
      r[v][e] /= magnitudes[v];
    }
  }
  /**/
  
  return r;
}

//converts a float array to a double array
double[][] floatToDouble(float[][] m, int l, int w) {
  double[][] r = new double[l][w];

  for(int i = 0; i < l; i += 1) {
    for(int j = 0; j < w; j += 1) {
      r[i][j] = (double)m[i][j];
    }
  }

  return r;
}

//converts a double array to a float array
float[][] doubleToFloat(double[][] m, int l, int w) {
  float[][] r = new float[l][w];

  for(int i = 0; i < l; i += 1) {
    for(int j = 0; j < w; j += 1) {
      r[i][j] = (float)m[i][j];
    }
  }

  return r;
}

//produces a rotation matrix given a rotation 'theta' around
//a given axis
float[][] rotateAxisVector(float[][] m, float theta, PVector axis) {
  float s = sin(theta);
  float c = cos(theta);
  float t = 1-c;

  if(c > 0.9)
  t = 2*sin(theta/2)*sin(theta/2);

  float x = axis.x;
  float y = axis.y;
  float z = axis.z;
  
  float[][] r = new float[3][3];

  r[0][0] = x*x*t+c;
  r[0][1] = x*y*t-z*s;
  r[0][2] = x*z*t+y*s;
  r[1][0] = y*x*t+z*s;
  r[1][1] = y*y*t+c;
  r[1][2] = y*z*t-x*s;
  r[2][0] = z*x*t-y*s;
  r[2][1] = z*y*t+x*s;
  r[2][2] = z*z*t+c;
  
  RealMatrix M = new Array2DRowRealMatrix(floatToDouble(m, 3, 3));
  RealMatrix R = new Array2DRowRealMatrix(floatToDouble(r, 3, 3));
  RealMatrix MR = M.multiply(R);

  return doubleToFloat(MR.getData(), 3, 3);
}

//returns the result of a vector 'v' multiplied by scalar 's'
float[] vectorScalarMult(float[] v, float s) {
  float[] ret = new float[v.length];
  for(int i = 0; i < ret.length; i += 1) { 
    ret[i] = v[i]*s; 
  }
  
  return ret;
}

/**
 * Determines if the lies within the range of angles that span from rangeStart to rangeEnd,
 * going clockwise around the Unit Cycle. It is assumed that all parameters are in radians
 * and within the range [0, TWO_PI).
 * 
 * @param angleToVerify  the angle in question
 * @param rangeStart     the 'lower bounds' of the angle range to check
 * @param rangeEnd       the 'upper bounds' of the angle range to check
 */
public boolean angleWithinBounds(float angleToVerify, float rangeStart, float rangeEnd) {
  
  if(rangeStart < rangeEnd) {
    // Joint range does not overlap TWO_PI
    return angleToVerify >= rangeStart && angleToVerify <= rangeEnd;
  } else {
    // Joint range overlaps TWO_PI
    return !(angleToVerify > rangeEnd && angleToVerify < rangeStart);
  }
}

/**
 * Brings the given angle (in radians) within the range: [0, TWO_PI).
 * 
 * @param angle  Some rotation in radians
 * @returning    The equivalent angle within the range [0, TWO_PI)
 */
public float mod2PI(float angle) {
  float temp = angle % TWO_PI;
  
  if (temp < 0f) {
    temp += TWO_PI;
  }
  
  return temp;
}

/**
 * Computes the minimum rotational magnitude to move
 * from src to dest, around the unit circle.
 * 
 * @param src   The source angle in radians
 * @param dset  The destination angle in radians
 * @returning   The minimum distance between src and dest
 */
public float minimumDistance(float src, float dest) {
  // Bring angles within range [0, TWO_PI)
  float difference = mod2PI(dest) - mod2PI(src);
  
  if (difference > PI) {
    difference -= TWO_PI;
  } else if (difference < -PI) {
    difference += TWO_PI;
  }
  
  return difference;
}

public int clamp(int in, int min, int max) {
  return min(max, max(min, in));
}

  
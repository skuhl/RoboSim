/**
 * Applies the rotations and translations of the Robot Arm to get to the
 * face plate center, given the set of six joint angles, each corresponding
 * to a joint of the Robot Arm and each within the bounds of [0, TWO_PI).
 * 
 * @param jointAngles  A valid set of six joint angles (in radians) for the Robot
 */
public void applyModelRotation(float[] jointAngles) {
  translate(600, 200, 0);
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
public Point applyFrame(Point pt, PVector origin, float[] axes) {
  float[] invAxes = quaternionConjugate(axes);
  PVector position = convertToFrame(pt.position, origin, axes);
  float[] orientation = quaternionMult(pt.orientation, invAxes);
  
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
public PVector convertToFrame(PVector v, PVector origin, float[] axes) {
  float[] invAxes = axes;
  PVector vOffset = PVector.sub(v, origin);
  return rotateVectorQuat(vOffset, invAxes);
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
public Point removeFrame(Point pt, PVector origin, float[] axes) {
  PVector position = convertFromFrame(pt.position, origin, axes);
  float[] orientation = quaternionMult(pt.orientation, axes);
  
  return new Point(position, orientation, pt.angles);
}

/**
 * Converts the given vector, v, from the Coordinate System defined by the given origin
 * vector and rotation quaternion axes.
 * 
 * @param v       A vector in the XYZ vector space
 * @param origin  The origin of the Coordinate System
 * @param axes    The axes of the Coordinate System representing as a rotation quanternion
 * @returning     The vector, v, interms of the given frame's Coordinate System
 */
public PVector convertFromFrame(PVector v, PVector origin, float[] axes) {
  PVector vRotated = rotateVectorQuat(v, axes);
  return vRotated.add(origin);
}

/**
 * Converts the given vector form the right-hand World Frame Coordinate System
 * to the left-hand Native Coordinate System.
 */
public PVector convertWorldToNative(PVector v) {
  float[][] tMatrix = transformationMatrix(new PVector(0f, 0f, 0f), WORLD_AXES);
  return transform(v, tMatrix);
}

/**
 * Converts the given vector form the left-hand Native Coordinate System to the
 * right-hand World Frame Coordinate System.
 */
public PVector convertNativeToWorld(PVector v) {
  float[][] tMatrix = transformationMatrix(new PVector(0f, 0f, 0f), WORLD_AXES);
  return transform(v, invertHCMatrix(tMatrix));
}

/* Transforms the given vector from the coordinate system defined by the given
 * transformation matrix (column major order). */
public PVector transform(PVector v, float[][] tMatrix) {
  if(tMatrix.length != 4 || tMatrix[0].length != 4) {
    return null;
  }

  PVector u = new PVector();
  // Apply the transformation matrix to the given vector
  u.x = v.x * tMatrix[0][0] + v.y * tMatrix[0][1] + v.z * tMatrix[0][2] + tMatrix[0][3];
  u.y = v.x * tMatrix[1][0] + v.y * tMatrix[1][1] + v.z * tMatrix[1][2] + tMatrix[1][3];
  u.z = v.x * tMatrix[2][0] + v.y * tMatrix[2][1] + v.z * tMatrix[2][2] + tMatrix[2][3];

  return u;
}

/* Transforms the given vector by the given 3x3 rotation matrix (row major order). */
public PVector rotate(PVector v, float[][] rotMatrix) {
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
  inverse[0][3] = -(m[0][0] * m[0][3] + m[1][0] * m[1][3] + m[2][0] * m[2][3]);
  inverse[1][0] = m[0][1];
  inverse[1][1] = m[1][1];
  inverse[1][2] = m[2][1];
  inverse[1][3] = -(m[0][1] * m[0][3] + m[1][1] * m[1][3] + m[2][1] * m[2][3]);
  inverse[2][0] = m[0][2];
  inverse[2][1] = m[1][2];
  inverse[2][2] = m[2][2];
  inverse[2][3] = -(m[0][2] * m[0][3] + m[1][2] * m[1][3] + m[2][2] * m[2][3]);
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
  PVector col_4 = getCoordFromMatrix(0, 0, 0);
  PVector col_1 = getCoordFromMatrix(1, 0, 0).sub(col_4);
  PVector col_2 = getCoordFromMatrix(0, 1, 0).sub(col_4);
  PVector col_3 = getCoordFromMatrix(0, 0, 1).sub(col_4);

  // Place the values of each vector in the correct cells of the transform  matrix
  transform[0][0] = col_1.x;
  transform[1][0] = col_1.y;
  transform[2][0] = col_1.z;
  transform[3][0] = 0;
  transform[0][1] = col_2.x;
  transform[1][1] = col_2.y;
  transform[2][1] = col_2.z;
  transform[3][1] = 0;
  transform[0][2] = col_3.x;
  transform[1][2] = col_3.y;
  transform[2][2] = col_3.z;
  transform[3][2] = 0;
  transform[0][3] = col_4.x;
  transform[1][3] = col_4.y;
  transform[2][3] = col_4.z;
  transform[3][3] = 1;

  return transform;
}

/**
 * Forms the 4x4 transformation matrix (column major order) form the given
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
  transform[1][0] = axes[0][1];
  transform[2][0] = axes[0][2];
  transform[3][0] = 0;
  transform[0][1] = axes[1][0];
  transform[1][1] = axes[1][1];
  transform[2][1] = axes[1][2];
  transform[3][1] = 0;
  transform[0][2] = axes[2][0];
  transform[1][2] = axes[2][1];
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
float[] eulerToQuat(PVector wpr) {
  
  float[] q = new float[4];
  float xRot = wpr.x;
  float yRot = wpr.y;
  float zRot = wpr.z;
  
  q[0] = cos(xRot/2)*cos(yRot/2)*cos(zRot/2) + sin(xRot/2)*sin(yRot/2)*sin(zRot/2);
  q[1] = sin(xRot/2)*cos(yRot/2)*cos(zRot/2) - cos(xRot/2)*sin(yRot/2)*sin(zRot/2);
  q[2] = cos(xRot/2)*sin(yRot/2)*cos(zRot/2) + sin(xRot/2)*cos(yRot/2)*sin(zRot/2);
  q[3] = cos(xRot/2)*cos(yRot/2)*sin(zRot/2) - sin(xRot/2)*sin(yRot/2)*cos(zRot/2);
  
  return q;
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
float[] matrixToQuat(float[][] r) {
  float[] q = new float[4];
  float tr = r[0][0] + r[1][1] + r[2][2];

  if(tr > 0) {
    float S = sqrt(1.0 + tr) * 2; // S=4*q[0] 
    q[0] = S / 4;
    q[1] = (r[2][1] - r[1][2]) / S;
    q[2] = (r[0][2] - r[2][0]) / S; 
    q[3] = (r[1][0] - r[0][1]) / S;
  } else if((r[0][0] > r[1][1]) & (r[0][0] > r[2][2])) {
    float S = sqrt(1.0 + r[0][0] - r[1][1] - r[2][2]) * 2; // S=4*q[1] 
    q[0] = (r[2][1] - r[1][2]) / S;
    q[1] = S / 4;
    q[2] = (r[0][1] + r[1][0]) / S; 
    q[3] = (r[0][2] + r[2][0]) / S;
  } else if(r[1][1] > r[2][2]) {
    float S = sqrt(1.0 + r[1][1] - r[0][0] - r[2][2]) * 2; // S=4*q[2]
    q[0] = (r[0][2] - r[2][0]) / S;
    q[1] = (r[0][1] + r[1][0]) / S; 
    q[2] = S / 4;
    q[3] = (r[1][2] + r[2][1]) / S;
  } else {
    float S = sqrt(1.0 + r[2][2] - r[0][0] - r[1][1]) * 2; // S=4*q[3]
    q[0] = (r[1][0] - r[0][1]) / S;
    q[1] = (r[0][2] + r[2][0]) / S;
    q[2] = (r[1][2] + r[2][1]) / S;
    q[3] = S / 4;
  }

  return quaternionNormalize(q);
}

//calculates euler angles from quaternion
PVector quatToEuler(float[] q) {
  float[][] r = quatToMatrix(q);
  PVector wpr = matrixToEuler(r);
  return wpr;
}

//calculates rotation matrix from quaternion
float[][] quatToMatrix(float[] q) {
  float[][] r = new float[3][3];
  
  r[0][0] = 1 - 2*(q[2]*q[2] + q[3]*q[3]);
  r[0][1] = 2*(q[1]*q[2] - q[0]*q[3]);
  r[0][2] = 2*(q[0]*q[2] + q[1]*q[3]);
  r[1][0] = 2*(q[1]*q[2] + q[0]*q[3]);
  r[1][1] = 1 - 2*(q[1]*q[1] + q[3]*q[3]);
  r[1][2] = 2*(q[2]*q[3] - q[0]*q[1]);
  r[2][0] = 2*(q[1]*q[3] - q[0]*q[2]);
  r[2][1] = 2*(q[0]*q[1] + q[2]*q[3]);
  r[2][2] = 1 - 2*(q[1]*q[1] + q[2]*q[2]);
  
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

//calculates the difference between each corresponding pair of
//elements for two vectors of n elements
float[] calculateVectorDelta(float[] v1, float[] v2, int n) {
  float[] d = new float[n];
  for(int i = 0; i < n; i += 1) {
    d[i] = v1[i] - v2[i];
  }

  return d;
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


/* Calculates the result of a rotation of quaternion 'p'
 * about axis 'u' by 'theta' degrees
 */
float[] rotateQuat(float[] p, PVector u, float theta) {
  float[] q = new float[4];
  
  q[0] = cos(theta/2);
  q[1] = sin(theta/2)*u.x;
  q[2] = sin(theta/2)*u.y;
  q[3] = sin(theta/2)*u.z;
  
  float[] pq = quaternionMult(p, q);

  return quaternionNormalize(pq);
}

PVector rotateVectorQuat(PVector v, PVector u, float theta) {
  float[] q = new float[4];
  float[] p = new float[4];
  float[] q_inv = new float[4];
  float[] p_prime = new float[4];
  
  q[0] = cos(theta/2);
  q[1] = sin(theta/2)*u.x;
  q[2] = sin(theta/2)*u.y;
  q[3] = sin(theta/2)*u.z;
  
  p[0] = 0;
  p[1] = v.x;
  p[2] = v.y;
  p[3] = v.z;
  
  q_inv[0] = q[0];
  q_inv[1] = -q[1];
  q_inv[2] = -q[2];
  q_inv[3] = -q[3];
  
  p_prime = quaternionMult(q, p);
  p_prime = quaternionMult(p_prime, q_inv);

  return new PVector(p_prime[1], p_prime[2], p_prime[3]);
}

/**
 * Rotates the given vector, v, by the given unit quaternion, q.
 * 
 * @param v    A vector in the xyz plane
 * @param q    A unit quaternion that defines a rotation
 * @returning  v rotated by q
 */
PVector rotateVectorQuat(PVector v, float[] q) {
  float[] p = new float[4];
  float[] q_inv = new float[4];
  float[] p_prime = new float[4];
  // v
  p[0] = 0;
  p[1] = v.x;
  p[2] = v.y;
  p[3] = v.z;
  // q'
  q_inv[0] = q[0];
  q_inv[1] = -q[1];
  q_inv[2] = -q[2];
  q_inv[3] = -q[3];
  // u = q * v * q'
  p_prime = quaternionMult(q, p);
  p_prime = quaternionMult(p_prime, q_inv);
  
  return new PVector(p_prime[1], p_prime[2], p_prime[3]);
}

/* Given 2 quaternions, calculates the quaternion representing the 
 * rotation from 'q1' to 'q2' such that 'qr'*'q1' = 'q2'. Note that 
 * the multiply operation should be taken to mean quaternion
 * multiplication, which is non-commutative.
 */
float[] calculateQuatOffset(float[] q1, float[] q2) {
  float[] q1_inv = new float[4];
  q1_inv[0] = q1[0];
  q1_inv[1] = -q1[1];
  q1_inv[2] = -q1[2];
  q1_inv[3] = -q1[3];
  
  float[] qr = quaternionMult(q2, q1_inv);
  
  for(int i = 0; i < 4; i += 1) {
    if(qr[i] < 0.00001)
    qr[i] = 0;
  }
  
  return qr;
}

/**
 * Returns the complex conjugate or inverse of the given quaternion.
 */
public float[] quaternionConjugate(float[] q) {
  return new float[] { q[0], -q[1], -q[2], -q[3] };
}

//returns the result of a quaternion 'q1' multiplied by quaternion 'q2'
float[] quaternionMult(float[] q1, float[] q2) {
  float[] r = new float[4];
  r[0] = q1[0]*q2[0] - q1[1]*q2[1] - q1[2]*q2[2] - q1[3]*q2[3];
  r[1] = q1[0]*q2[1] + q1[1]*q2[0] + q1[2]*q2[3] - q1[3]*q2[2];
  r[2] = q1[0]*q2[2] - q1[1]*q2[3] + q1[2]*q2[0] + q1[3]*q2[1];
  r[3] = q1[0]*q2[3] + q1[1]*q2[2] - q1[2]*q2[1] + q1[3]*q2[0];

  return r;
}

/**
 * Returns a quaternion, which represents the rotation of q, in terms of reference.
 */
public float[] quaternionRef(float[] q, float[] reference) {
  float[] invRef = quaternionNormalize( quaternionConjugate(reference) );
  return quaternionNormalize( quaternionMult(q, invRef) );
}

//returns the result of a quaternion 'q' multiplied by scalar 's'
float[] quaternionScalarMult(float[] q, float s) {
  float[] qr = new float[4];
  qr[0] = q[0]*s;
  qr[1] = q[1]*s;
  qr[2] = q[2]*s;
  qr[3] = q[3]*s;
  return qr;
}

//returns the result of the addition of two quaternions, 'q1' and 'q2'
float[] quaternionAdd(float[] q1, float[] q2) {
  float[] qr = new float[4];
  qr[0] = q1[0] + q2[0];
  qr[1] = q1[1] + q2[1];
  qr[2] = q1[2] + q2[2];
  qr[3] = q1[3] + q2[3];
  return qr;
}

/**
 * Computes the dot product between the two given quaternions.
 */
public float quaternionDotProduct(float[] q1, float[] q2) {
  float product = 0f;
  
  for (int idx = 0; idx < 4; ++idx) {
    product += q1[idx] * q2[idx];
  }
  
  return product;
}

//returns the magnitude of the input quaternion 'q'
float calculateQuatMag(float[] q) {
  return sqrt(pow(q[0], 2) + pow(q[1], 2) + pow(q[2], 2) + pow(q[3], 2));
}

float[] quaternionNormalize(float[] q) {
  float qMag = calculateQuatMag(q);
  return quaternionScalarMult(q, 1/qMag);
}

/* Given two input quaternions, 'q1' and 'q2', computes the spherical-
 * linear interpolation from 'q1' to 'q2' for a given fraction of the
 * complete transformation 'q1' to 'q2', denoted by 0 <= 'mu' <= 1. 
 */
float[] quaternionSlerp(float[] q1, float[] q2, float mu) {
  float[] qSlerp = new float[4];
  float[] q3 = new float[4];
  float cOmega = 0;
  
  if(mu == 0) return q1;
  if(mu == 1) return q2;
  
  for(int i = 0; i < 4; i += 1)
  cOmega += q1[i]*q2[i];
  
  if(cOmega < 0) {
    cOmega = -cOmega;
    q3 = quaternionScalarMult(q2, -1);
  }
  else {
    q3 = quaternionScalarMult(q2, 1);
  }
  
  if(cOmega > 0.99999995) {
    qSlerp[0] = q1[0]*(1-mu) + q3[0]*mu;
    qSlerp[1] = q1[1]*(1-mu) + q3[1]*mu;
    qSlerp[2] = q1[2]*(1-mu) + q3[2]*mu;
    qSlerp[3] = q1[3]*(1-mu) + q3[3]*mu;
  }
  else {
    float omega = acos(cOmega);
    float scale1 = sin(omega*(1-mu))/sin(omega);
    float scale2 = sin(omega*mu)/sin(omega);
    
    qSlerp[0] = q1[0]*scale1 + q3[0]*scale2;
    qSlerp[1] = q1[1]*scale1 + q3[1]*scale2;
    qSlerp[2] = q1[2]*scale1 + q3[2]*scale2;
    qSlerp[3] = q1[3]*scale1 + q3[3]*scale2;
  }
  
  return quaternionNormalize(qSlerp);
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
    return angleToVerify >= rangeStart && angleToVerify < rangeEnd;
  } else {
    // Joint range overlaps TWO_PI
    return !(angleToVerify >= rangeEnd && angleToVerify < rangeStart);
  }
}

/**
 * Brings the given angle (in radians) within the range: [0, TWO_PI).
 * 
 * @param angle  Some rotation in radians
 * @returning    The equivalent angle within the range [0, TWO_PI)
 */
public float mod2PI(float angle) {
  float limbo = angle % TWO_PI;
  
  if (limbo < 0f) {
    limbo += TWO_PI;
  }
  
  return limbo;
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
  
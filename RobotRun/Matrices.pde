/* Transforms the given vector from the coordinate system defined by the given
 * transformation matrix. */
public PVector transform(PVector v, float[][] tMatrix) {
  if (tMatrix.length != 4 || tMatrix[0].length != 4) {
    return null;
  }

  PVector u = new PVector();

  u.x = v.x * tMatrix[0][0] + v.y * tMatrix[0][1] + v.z * tMatrix[0][2] + tMatrix[0][3];
  u.y = v.x * tMatrix[1][0] + v.y * tMatrix[1][1] + v.z * tMatrix[1][2] + tMatrix[1][3];
  u.z = v.x * tMatrix[2][0] + v.y * tMatrix[2][1] + v.z * tMatrix[2][2] + tMatrix[2][3];

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
  if (m.length != 4 || m[0].length != 4) {
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
 * of the stack */
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

/* This method transforms the given coordinates into a vector
 * in the Processing's native coordinate system. */
public PVector getCoordFromMatrix(float x, float y, float z) {
  PVector vector = new PVector();

  vector.x = modelX(x, y, z);
  vector.y = modelY(x, y, z);
  vector.z = modelZ(x, y, z);

  return vector;
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

  //println("matrix: ");
  //  for(int i = 0; i < 3; i += 1){
  //    for(int j = 0; j < 3; j += 1){
  //      print(String.format("  %4.3f", r[i][j]));
  //    }
  //  println();
  //}
  //println();

  return r;
}

//calculates quaternion from euler angles
float[] eulerToQuat(PVector wpr) {
  //float[][] r = eulerToMatrix(wpr);
  //float[] q = matrixToQuat(r);

  /*Alternate computation method; produces equivalent result to above, but may
   *not have the same sign (certain quaternions are equivalent when negated).
   */
  float[] q = new float[4];
  float xRot = wpr.x;
  float yRot = wpr.y;
  float zRot = wpr.z;

  q[0] = sin(zRot/2)*sin(yRot/2)*sin(xRot/2) + cos(zRot/2)*cos(yRot/2)*cos(xRot/2);
  q[1] = -sin(zRot/2)*sin(yRot/2)*cos(xRot/2) + sin(xRot/2)*cos(zRot/2)*cos(yRot/2);
  q[2] = sin(zRot/2)*sin(xRot/2)*cos(yRot/2) + sin(yRot/2)*cos(zRot/2)*cos(zRot/2);
  q[3] = sin(zRot/2)*cos(yRot/2)*cos(xRot/2) - sin(yRot/2)*sin(xRot/2)*cos(xRot/2);

  return q;
}

//calculates euler angles from rotation matrix
PVector matrixToEuler(float[][] r) {
  float yRot1, yRot2, xRot1, xRot2, zRot1, zRot2;
  PVector wpr, wpr2;

  if (r[2][0] != 1 && r[2][0] != -1) {
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
    if (r[2][0] == -1) {
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

  if (tr > 0) {
    float S = sqrt(1.0 + tr) * 2; // S=4*q[0] 
    q[0] = S / 4;
    q[1] = (r[2][1] - r[1][2]) / S;
    q[2] = (r[0][2] - r[2][0]) / S; 
    q[3] = (r[1][0] - r[0][1]) / S;
  } else if ((r[0][0] > r[1][1]) & (r[0][0] > r[2][2])) {
    float S = sqrt(1.0 + r[0][0] - r[1][1] - r[2][2]) * 2; // S=4*q[1] 
    q[0] = (r[2][1] - r[1][2]) / S;
    q[1] = S / 4;
    q[2] = (r[0][1] + r[1][0]) / S; 
    q[3] = (r[0][2] + r[2][0]) / S;
  } else if (r[1][1] > r[2][2]) {
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

  return q;
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

  //println("matrix: ");
  //for(int i = 0; i < 3; i += 1){
  //  for(int j = 0; j < 3; j += 1){
  //    print(String.format("  %4.3f", m[i][j]));
  //  }
  //  println();
  //}
  //println();

  return r;
}

//converts a float array to a double array
double[][] floatToDouble(float[][] m, int l, int w) {
  double[][] r = new double[l][w];

  for (int i = 0; i < l; i += 1) {
    for (int j = 0; j < w; j += 1) {
      r[i][j] = (double)m[i][j];
    }
  }

  return r;
}

//converts a double array to a float array
float[][] doubleToFloat(double[][] m, int l, int w) {
  float[][] r = new float[l][w];

  for (int i = 0; i < l; i += 1) {
    for (int j = 0; j < w; j += 1) {
      r[i][j] = (float)m[i][j];
    }
  }

  return r;
}

//calculates the change in x, y, and z from p1 to p2
float[] calculateVectorDelta(PVector p1, PVector p2) {
  float[] d = {p1.x - p2.x, p1.y - p2.y, p1.z - p2.z};
  return d;
}

//calculates the difference between each corresponding pair of
//elements for two vectors of n elements
float[] calculateVectorDelta(float[] v1, float[] v2, int n) {
  float[] d = new float[n];
  for (int i = 0; i < n; i += 1) {
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

  if (c > 0.9)
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
float[] rotateQuat(float[] p, float theta, PVector u) {
  float[] q = new float[4];
  
  q[0] = cos(theta/2);
  q[1] = sin(theta/2)*u.x;
  q[2] = sin(theta/2)*u.y;
  q[3] = sin(theta/2)*u.z;
  
  float[] qp = quaternionMult(q, p);

  return qp;
}

/* Given 2 quaternions, calculates the quaternion representing the 
 * rotation from 'q1' to 'q2' such that 'qr'*'q1' = 'q2'. Note that 
 * the multiply operation should be taken to mean quaternion
 * multiplication, which is non-commutative.
 */
float[] calculateQuatOffset(float[] q1, float[] q2){
  float[] q1_inv = new float[4];
  q1_inv[0] = q1[0];
  q1_inv[1] = -q1[1];
  q1_inv[2] = -q1[2];
  q1_inv[3] = -q1[3];
  
  float[] qr = quaternionMult(q2, q1_inv);
  
  for(int i = 0; i < 4; i += 1){
    if(qr[i] < 0.00001)
      qr[i] = 0;
  }
  
  return qr;
}

float[] quaternionMult(float[] q1, float[] q2) {
  float[] r = new float[4];
  r[0] = q1[0]*q2[0] - q1[1]*q2[1] - q1[2]*q2[2] - q1[3]*q2[3];
  r[1] = q1[0]*q2[1] + q1[1]*q2[0] + q1[2]*q2[3] - q1[3]*q2[2];
  r[2] = q1[0]*q2[2] - q1[1]*q2[3] + q1[2]*q2[0] + q1[3]*q2[1];
  r[3] = q1[0]*q2[3] + q1[1]*q2[2] - q1[2]*q2[1] + q1[3]*q2[0];

  return r;
}

//returns the magnitude of the input quaternion 'q'
float calculateQuatMag(float[] q){
  return sqrt(pow(q[0], 2) + pow(q[1], 2) + pow(q[2], 2) + pow(q[3], 2));
}

/* Displays the contents of a 4x4 matrix in the command line */
public void printHCMatrix(float[][] m) {
  if (m.length != 4 || m[0].length != 4) { 
    return;
  }

  for (int r = 0; r < m.length; ++r) {
    String row = String.format("[ %5.4f %5.4f %5.4f %5.4f ]\n", m[r][0], m[r][1], m[r][2], m[r][3]);
    print(row);
  }
}

/* Returns a string represenation of the given matrix.
 * 
 * @param matrixx  A non-null matrix
 */
public String matrixToString(float[][] matrix) {
  String mStr = "";
  
  for (int row = 0; row < matrix.length; ++row) {
    mStr += "\n[";

    for (int col = 0; col < matrix[0].length; ++col) {
      // Account for the negative sign character
      if (matrix[row][col] >= 0) { mStr += " "; }
      
      mStr += String.format(" %5.6f", matrix[row][col]);
    }

    mStr += "  ]";
  }
  
  return (mStr + "\n");
}
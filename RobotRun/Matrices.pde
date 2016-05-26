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
 *    https://web.archive.org/web/20130806093214/http://www-graphics.stanford.edu/courses/cs248-98-fall/Final/q4.html
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

/* Calculate and returns a 3x3 matrix whose columns are the unit vectors of
 * the End Effector's x, y, z axes with respect to the World Frame. */
public float[][] calculateRotationMatrix() {
  pushMatrix();
  resetMatrix();
  // Switch to End Effector reference Frame
  applyModelRotation(armModel);
  /* Define vectors { 0, 0, 0 }, { 1, 0, 0 }, { 0, 1, 0 }, and { 0, 0, 1 }
   * Swap y and z coordinates, negating the original y coordinate
   * Swap vectors:
   *   x' = z
   *   y' = -y
   *   z' = x
   */
  PVector origin = new PVector(modelZ(0, 0, 0), modelY(0, 0, 0), modelX(0, 0, 0)),
               x = new PVector(modelZ(1, 0, 0), modelY(-1, 0, 0), modelX(1, 0, 0)),
               y = new PVector(modelZ(0, 1, 0), modelY(0, -1, 0), modelX(0, 1, 0)),
               z = new PVector(modelZ(0, 0, 1), modelY(0, 0, -1), modelX(0, 0, 1));
  
  popMatrix();
  
  float[][] matrix = new float[3][3];
  // Calcualte Unit Vectors form difference between each axis vector and the origin
  matrix[0][0] = x.x - origin.x;
  matrix[1][0] = x.y - origin.y;
  matrix[2][0] = x.z - origin.z;
  matrix[0][1] = y.x - origin.x;
  matrix[1][1] = y.y - origin.y;
  matrix[2][1] = y.z - origin.z;
  matrix[0][2] = z.x - origin.x;
  matrix[1][2] = z.y - origin.y;
  matrix[2][2] = z.z - origin.z;
  
  return matrix;
}

public float[][] calculateRotationMatrix(PVector wpr){
  float[][] matrix = new float[6][9];
  float phi = wpr.x;
  float theta = wpr.y;
  float psi = wpr.z;
  
  matrix[0][0] = cos(theta)*cos(psi);
  matrix[0][1] = sin(phi)*sin(theta)*cos(psi) - cos(phi)*sin(psi);
  matrix[0][2] = cos(phi)*sin(theta)*cos(psi) + sin(phi)*sin(psi);
  matrix[1][0] = cos(theta)*sin(psi);
  matrix[1][1] = sin(phi)*sin(theta)*sin(psi) + cos(phi)*cos(psi);
  matrix[1][2] = cos(phi)*sin(theta)*sin(psi) - sin(phi)*cos(psi);
  matrix[2][0] = -sin(theta);
  matrix[2][1] = sin(phi)*cos(theta);
  matrix[2][2] = cos(phi)*cos(theta);
  
  return matrix;
}

/* Computes the dot product of the two given vectors */
public float dotProduct(float[] v1, float[] v2) {
  if (v1.length != v2.length) { return 0f; }
  
  float product = 0f;
  
  for (int idx = 0; idx < v1.length; ++idx) {
    product += v1[idx] * v2[idx];
  }
  
  return product;
}

/* Returns a vector, whose components are the respective
 * sums of the components of the given vectors. */
public float[] sum(float[] v1, float[] v2) {
  if (v1.length != v2.length) { return null; }
  
  float[] s = new float[v1.length];
  
  for (int idx = 0; idx < s.length; ++idx) {
    s[idx] = v1[idx] + v2[idx];
  }
  
  return s;
}

/* Returns a vector, whose components are of the
 * opposite sign of the given vector. */
public float[] negate(float[] v) {
  float[] opp = new float[v.length];
  
  for (int idx = 0; idx < opp.length; ++idx) {
    opp[idx] = -v[idx];
  }
  
  return opp;
}

//calculates the change in x, y, and z from p1 to p2
float[] calculateVectorDelta(PVector p1, PVector p2){
  float[] d = {p1.x - p2.x, p1.y - p2.y, p1.z - p2.z};
  return d;
}

//calculate the dot product of two 3 element vectors represented by float arrays
float calculateVectorDot3(float[] v1, float[] v2){
  float dot = v1[0]*v2[0] + v1[1]*v2[1] + v1[2]*v2[2];
  return dot;
}

//calculate the dot product of two 6 element vectors represented by float arrays
float calculateVectorDot6(float[] v1, float[] v2){
  float dot = v1[0]*v2[0] + v1[1]*v2[1] + v1[2]*v2[2]
            + v1[3]*v2[3] + v1[4]*v2[4] + v1[5]*v2[5];
  return dot;
}

float[] calculateRotationalDelta(PVector p1, PVector p2){
  float[] d = new float[3];
  d[0] = minimumDistance(p1.x, p2.x);
  d[1] = minimumDistance(p1.y, p2.y);
  d[2] = minimumDistance(p1.z, p2.z);
  return d;
}
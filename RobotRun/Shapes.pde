/**
 * A simple class that defines the outline and fill color for a shape
 * along with some methods necessarry for a shape.
 */
public abstract class Shape {
  private color fillColor,
                outlineColor;
  private boolean noFill;
  
  public Shape() {
    fillColor = color(0);
    outlineColor = color(225);
    noFill = false;
  }
  
  public Shape(color fill, color outline) {
    fillColor = fill;
    outlineColor = outline;
    noFill = false;
  }
  
  public Shape(color outline) {
    fillColor = color(255);
    outlineColor = outline;
    noFill = true;
  }
  
  public void draw() {
    // Apply shape outline and fill color
    stroke(outlineColor);
    
    if (noFill) {
      noFill();
    } else {
      fill(fillColor);
    } 
  }
  
  /* Getters and Setters for shapes fill and outline colors */
  public color getOutlineColor() { return outlineColor; }
  public void setOutlineColor(color newColor) { outlineColor = newColor; }
  public color getFillColor() { return fillColor; }
  public void setFillColor(color newColor) { fillColor = newColor; }
  public boolean isFilled() { return !noFill; }
  public void setFillFlag(boolean notFilled) { noFill = !notFilled; }
  
  /**
   * Returns a copy of the Shape object
   */
  public abstract Shape clone();
}

/**
 * Defines the length, width, height values to draw a box.
 */
public class Box extends Shape {
  /**
   * X -> length
   * Y -> Height
   * Z -> Width
   */
  private PVector dimensions;
  
  /**
   * Create a cube, with an edge length of 10.
   */
  public Box() {
    super();
    dimensions = new PVector(10f, 10f, 10f);
  }
  
  /**
   * Create a box with the given colors and dinemsions.
   */
  public Box(color fill, color outline, float len, float hgt, float wdh) {
    super(fill, outline);
    dimensions = new PVector(len, hgt, wdh);
  }
  
  /**
   * Create an empty box with the given color and dinemsions.
   */
  public Box(color outline, float len, float hgt, float wdh) {
    super(outline);
    dimensions = new PVector(len, hgt, wdh);
  }
  
  /**
   * Create a cube with the given colors and dinemsion.
   */
  public Box(color fill, color outline, float edgeLen) {
    super(fill, outline);
    dimensions = new PVector(edgeLen, edgeLen, edgeLen);
  }
  
  /**
   * Create an empty cube with the given color and dinemsion.
   */
  public Box(color outline, float edgeLen) {
    super(outline);
    dimensions = new PVector(edgeLen, edgeLen, edgeLen);
  }
  
  public void draw() {
    // Apply colors
    super.draw();
    box(dimensions.x, dimensions.y, dimensions.z);
  }
  
  public PVector getDimensions() { return dimensions.copy(); }
  
  public Shape clone() {
    Box copy = new Box(getFillColor(), getOutlineColor(), dimensions.x, dimensions.y, dimensions.z);
    copy.setFillFlag( isFilled() );
    return copy;
  }
}

/**
 * Defines the radius and height to draw a uniform cylinder
 */
public class Cylinder extends Shape {
  private float radius, height;
  
  public Cylinder() {
    super();
    radius = 10f;
    height = 10f;
  }
  
  public Cylinder(color fill, color outline, float rad, float hgt) {
    super(fill, outline);
    radius = rad;
    height = hgt;
  }
  
  public Cylinder(color outline, float rad, float hgt) {
    super(outline);
    radius = rad;
    height = hgt;
  }
  
  /**
   * Assumes the center of the cylinder is halfway between the top and bottom of of the cylinder.
   */
  public void draw() {
    super.draw();
    float halfHeight = height / 2,
          diameter = 2 * radius;
    
    translate(0f, 0f, halfHeight);
    // Draw top of the cylinder
    ellipse(0f, 0f, diameter, diameter);
    translate(0f, 0f, -height);
    // Draw bottom of the cylinder
    ellipse(0f, 0f, diameter, diameter);
    translate(0f, 0f, halfHeight);
    
    beginShape(TRIANGLE_STRIP);
    // Draw a string of triangles around the circumference of the Cylinders top and bottom.
    for (int degree = 0; degree <= 360; ++degree) {
      float pos_x = cos(DEG_TO_RAD * degree) * radius,
            pos_y = sin(DEG_TO_RAD * degree) * radius;
      
      vertex(pos_x, pos_y, halfHeight);
      vertex(pos_x, pos_y, -halfHeight);
    }
    
    endShape();
  }
  
  public float getRadius() { return radius; }
  public float getHeight() { return height; }
  
  public Shape clone() {
    Cylinder copy = new Cylinder(getFillColor(), getOutlineColor(), radius, height);
    copy.setFillFlag( isFilled() );
    return copy;
  }
}

public class BoundingBox {
  /* The origin of the bounding box's local Coordinate System */
  private PVector center;
  /* A 3x3 rotation mtrix, which describes the object's local coordinate axes */
  private float[][] orientation;
  private Box boundingBox;
  
  /**
   * Create a cube object with the given colors and dimension
   */
  public BoundingBox() {
    setCoordinateSystem();
    boundingBox = new Box(color(0, 0, 255), 10f);
  }
  
  /**
   * Create a cube object with the given colors and dimension
   */
  public BoundingBox(float edgeLen) {
    setCoordinateSystem();
    boundingBox = new Box(color(0, 0, 255), edgeLen);
  }
  
  /**
   * Create a box object with the given colors and dimensions
   */
  public BoundingBox(float len, float hgt, float wdh) {
    /* Pull center and orientation from the current transformation matrix */
    center = getCoordFromMatrix(0f, 0f, 0f);
    orientation = getRotationMatrix();
    boundingBox = new Box(color(0, 0, 255), len, hgt, wdh);
  }
  
  /**
   * Apply the box's local Coordinate System
   */
  public void applyCoordinateSystem() {
    applyMatrix(orientation[0][0], orientation[1][0], orientation[2][0], center.x,
                orientation[0][1], orientation[1][1], orientation[2][1], center.y,
                orientation[0][2], orientation[1][2], orientation[2][2], center.z,
                                0,                 0,                 0,        1);
  }
  
  /**
   * Draw both the object and its bounding box;
   */
  public void draw() {
    pushMatrix();
    // Draw shape in its own coordinate system
    applyCoordinateSystem();
    boundingBox.draw();
    popMatrix();
  }
  
  /**
   * Sets the current Coordinate System of the bounding-box
   * to the current transformation matrix.
   */
  public void setCoordinateSystem() {
    /* Pull center and orientation from the current transformation matrix */
    center = getCoordFromMatrix(0f, 0f, 0f);
    orientation = getRotationMatrix();
  }
  
  /**
   * Reset the object's center point
   */
  public PVector setCenter(PVector newCenter) {
    PVector old = center;
    center = newCenter.copy();
    return old;
  }
  
  public PVector getCenter() { return center.copy(); }
  
  /**
   * Reset the object's orientation axes; the given rotation
   * matrix should be in row major order!
   */
  public float[][] setOrientation(float[][] newOrientation) {
    float[][] old = orientation;
    orientation = newOrientation.clone();
    return old;
  }
  
  /**
   * Return a copy of the orientation matrix in
   * row major order.
   */
  public float[][] getAxes() {
    float[][] copy = new float[3][3];
    // Copy orientation
    for (int row = 0; row < 3; ++row) {
      for (int col = 0; col < 3; ++col) {
        copy[row][col] = orientation[row][col];
      }
    }
    
    return copy;
  }
  
  /**
   * Return a copy of the orientation matrix in
   * column major order.
   */
  public float[][] getTransposeAxes() {
    float[][] transpose = new float[3][3];
    // Transpose and copy orientation
    for (int row = 0; row < 3; ++row) {
      for (int col = 0; col < 3; ++col) {
        transpose[row][col] = orientation[col][row];
      }
    }
    
    return transpose;
  }
  
  /**
   * Sets the outline color of this ounding-box
   * to the given value.
   */
  public void setColor(color newColor) {
    boundingBox.setOutlineColor(newColor);
  }
  
  /**
   * Return a reference to this world object's bounding box
   */
  public Box getBox() { return (Box)boundingBox; }
  
  /**
   * Returns the dimension of the world object's bounding
   * box corresponding to the axes index given; the axes
   * indices are as follows:
   * 
   * 0 -> x
   * 1 -> y
   * 2 -> z
   */
  public float getDim(int axes) {
    
    switch (axes) {
    case 0:   return boundingBox.dimensions.x;
    case 1:   return boundingBox.dimensions.y;
    case 2:   return boundingBox.dimensions.z;
    default:  return -1f;
    }
  }
  
  /**
   * Determine of a single position, in Native Coordinates, is with
   * the bounding box of the this world object.
   */
  public boolean collision(PVector point) {
    // Convert the point to the current reference frame
    float[][] tMatrix = transformationMatrix(center, orientation);
    PVector relPosition = transform(point, invertHCMatrix(tMatrix));
    
    PVector BBDim = boundingBox.getDimensions();
    // Determine if the point iw within the bounding-box of this object
    boolean is_inside = relPosition.x >= -(BBDim.x / 2f) && relPosition.x <= (BBDim.x / 2f)
                     && relPosition.y >= -(BBDim.y / 2f) && relPosition.y <= (BBDim.y / 2f)
                     && relPosition.z >= -(BBDim.z / 2f) && relPosition.z <= (BBDim.z / 2f);
    
    return is_inside;
  }
  
  /**
   * Return a replicate of this world object's Bounding Box
   */
  public BoundingBox clone() {
    pushMatrix();
    applyCoordinateSystem();
    BoundingBox copy = new BoundingBox(getDim(0), getDim(1), getDim(2));
    copy.setColor( boundingBox.getOutlineColor() );
    popMatrix();
    
    return copy;
  }
}

/**
 * Defines a world object, which has a shape and a bounding box. The bounding
 * box holds the local coordinate system of the object.
 */
public class WorldObject {
  private Shape form;
  private BoundingBox OOB;
  
  /**
   * Create a cube object with the given colors and dimension
   */
  public WorldObject(color fill, color outline, float edgeLen) {
    form = new Box(fill, outline, edgeLen);
    /* Pull center and orientation from the current transformation matrix */
    OOB = new BoundingBox(edgeLen + 15f, edgeLen + 15f, edgeLen + 15f);
  }
  
  /**
   * Create a box object with the given colors and dimensions
   */
  public WorldObject(color fill, color outline, float len, float wdh, float hgt) {
    form = new Box(fill, outline, len, wdh, hgt);
    /* Pull center and orientation from the current transformation matrix */
    OOB = new BoundingBox(len + 15f, wdh + 15f, hgt + 15f);
  }
  
  /**
   * Creates a cylinder objects with the given colors and dimensions.
   */
  public WorldObject(color fill, color outline, float rad, float hgt) {
    form = new Cylinder(fill, outline, rad, hgt);
    OOB = new BoundingBox(2 * rad + 5f, 2 * rad + 5f, hgt + 10f);
  }
  
  /**
   * Draw both the object and its bounding box;
   */
  public void draw() {
    pushMatrix();
    // Draw shape in its own coordinate system
    OOB.applyCoordinateSystem();
    form.draw();
    OOB.getBox().draw();
    popMatrix();
  }
  
  /**
   * Returns a reference to this world object's bounding-box.
   */
  public BoundingBox getBoundingBox() {
    return OOB;
  }
  
  /**
   * Sets the outline color of the world's bounding-box
   * to the given value.
   */
  public void setBBColor(color newColor) {
    OOB.setColor(newColor);
  }
  
  /**
   * Determine if the given world object is colliding
   * with this world object.
   */
  public boolean collision(WorldObject obj) {
    return collision3D(OOB, obj.getBoundingBox());
  }
}

/**
 * This algorithm uses the Separating Axis Theorm to project radi of each Box on to several 
 * axes to determine if a there is any overlap between the boxes. The method strongy resembles 
 * the method outlined in Section 4.4 of "Real Time Collision Detection" by Christer Ericson
 *
 * @param A  The hit box associated with some object in space
 * @param B  The hit box associated with another object in space
 * @return   Whether the two hit boxes intersect
 */
public static boolean collision3D(BoundingBox A, BoundingBox B) {
  // Rows are x, y, z axis vectors for A and B: Ax, Ay, Az, Bx, By, and Bz
  float[][] axes_A = A.getTransposeAxes();
  float[][] axes_B = B.getTransposeAxes();
  
  // Rotation matrices to convert B into A's coordinate system
  float[][] rotMatrix = new float[3][3];
  float[][] absRotMatrix = new float[3][3];
  
  for(int v = 0; v < axes_A.length; v += 1) {
    for(int u = 0; u < axes_B.length; u += 1) {
      // PLEASE do not change to matrix mutliplication
      rotMatrix[v][u] = axes_A[v][0] * axes_B[u][0] +  axes_A[v][1] * axes_B[u][1] +  axes_A[v][2] * axes_B[u][2];
      // Add offset for valeus close to zero (parallel axes)
      absRotMatrix[v][u] = abs(rotMatrix[v][u]) + 0.00000000175f;
    }
  }
  
  // T = B's position - A's
  PVector posA = new PVector().set(A.getCenter());
  PVector posB = new PVector().set(B.getCenter());
  PVector limbo = posB.sub(posA);
  // Convert T into A's coordinate frame
  float[] T = new float[] { limbo.dot(new PVector().set(axes_A[0])), 
    limbo.dot(new PVector().set(axes_A[1])), 
    limbo.dot(new PVector().set(axes_A[2])) };
  
  float radiA, radiB;
  
  for(int idx = 0; idx < absRotMatrix.length; ++idx) {
    radiA = (A.getDim(idx) / 2);
    radiB = (B.getDim(0) / 2) * absRotMatrix[idx][0] + 
    (B.getDim(1) / 2) * absRotMatrix[idx][1] + 
    (B.getDim(2) / 2) * absRotMatrix[idx][2];
    
    // Check Ax, Ay, and Az
    if(abs(T[idx]) > (radiA + radiB)) { return false; }
  }
  
  for(int idx = 0; idx < absRotMatrix[0].length; ++idx) {
    radiA = (A.getDim(0) / 2) * absRotMatrix[0][idx] + 
    (A.getDim(1) / 2) * absRotMatrix[1][idx] + 
    (A.getDim(2) / 2) * absRotMatrix[2][idx];
    radiB = (B.getDim(idx) / 2);
    
    float check = abs(T[0]*rotMatrix[0][idx] + 
    T[1]*rotMatrix[1][idx] + 
    T[2]*rotMatrix[2][idx]);
    
    // Check Bx, By, and Bz
    if(check > (radiA + radiB)) { return false; }
  }
  
  radiA = (A.getDim(1) / 2) * absRotMatrix[2][0] + (A.getDim(2) / 2) * absRotMatrix[1][0];
  radiB = (B.getDim(1) / 2) * absRotMatrix[0][2] + (B.getDim(2) / 2) * absRotMatrix[0][1];
  // Check axes Ax x Bx
  if(abs(T[2] * rotMatrix[1][0] - T[1] * rotMatrix[2][0]) > (radiA + radiB)) { return false; }
  
  radiA = (A.getDim(1) / 2) * absRotMatrix[2][1] + (A.getDim(2) / 2) * absRotMatrix[1][1];
  radiB = (B.getDim(0) / 2) * absRotMatrix[0][2] + (B.getDim(2) / 2) * absRotMatrix[0][0];
  // Check axes Ax x By
  if(abs(T[2] * rotMatrix[1][1] - T[1] * rotMatrix[2][1]) > (radiA + radiB)) { return false; }
  
  radiA = (A.getDim(1) / 2) * absRotMatrix[2][2] + (A.getDim(2) / 2) * absRotMatrix[1][2];
  radiB = (B.getDim(0) / 2) * absRotMatrix[0][1] + (B.getDim(1) / 2) * absRotMatrix[0][0];
  // Check axes Ax x Bz
  if(abs(T[2] * rotMatrix[1][2] - T[1] * rotMatrix[2][2]) > (radiA + radiB)) { return false; }
  
  radiA = (A.getDim(0) / 2) * absRotMatrix[2][0] + (A.getDim(2) / 2) * absRotMatrix[0][0];
  radiB = (B.getDim(1) / 2) * absRotMatrix[1][2] + (B.getDim(2) / 2) * absRotMatrix[1][1];
  // Check axes Ay x Bx
  if(abs(T[0] * rotMatrix[2][0] - T[2] * rotMatrix[0][0]) > (radiA + radiB)) { return false; }
  
  radiA = (A.getDim(0) / 2) * absRotMatrix[2][1] + (A.getDim(2) / 2) * absRotMatrix[0][1];
  radiB = (B.getDim(0) / 2) * absRotMatrix[1][2] + (B.getDim(2) / 2) * absRotMatrix[1][0];
  // Check axes Ay x By
  if(abs(T[0] * rotMatrix[2][1] - T[2] * rotMatrix[0][1]) > (radiA + radiB)) { return false; }
  
  radiA = (A.getDim(0) / 2) * absRotMatrix[2][2] + (A.getDim(2) / 2) * absRotMatrix[0][2];
  radiB = (B.getDim(0) / 2) * absRotMatrix[1][1] + (B.getDim(1) / 2) * absRotMatrix[1][0];
  // Check axes Ay x Bz
  if(abs(T[0] * rotMatrix[2][2] - T[2] * rotMatrix[0][2]) > (radiA + radiB)) { return false; }
  
  
  radiA = (A.getDim(0) / 2) * absRotMatrix[1][0] + (A.getDim(1) / 2) * absRotMatrix[0][0];
  radiB = (B.getDim(1) / 2) * absRotMatrix[2][2] + (B.getDim(2) / 2) * absRotMatrix[2][1];
  // Check axes Az x Bx
  if(abs(T[1] * rotMatrix[0][0] - T[0] * rotMatrix[1][0]) > (radiA + radiB)) { return false; }
  
  radiA = (A.getDim(0) / 2) * absRotMatrix[1][1] + (A.getDim(1) / 2) * absRotMatrix[0][1];
  radiB = (B.getDim(0) / 2) * absRotMatrix[2][2] + (B.getDim(2) / 2) * absRotMatrix[2][0];
  // Check axes Az x By
  if(abs(T[1] * rotMatrix[0][1] - T[0] * rotMatrix[1][1]) > (radiA + radiB)) { return false; }
  
  radiA = (A.getDim(0) / 2) * absRotMatrix[1][2] + (A.getDim(1) / 2) * absRotMatrix[0][2];
  radiB = (B.getDim(0) / 2) * absRotMatrix[2][1] + (B.getDim(1) / 2) * absRotMatrix[2][0];
  // Check axes Az x Bz
  if(abs(T[1] * rotMatrix[0][2] - T[0] * rotMatrix[1][2]) > (radiA + radiB)) { return false; }
  
  return true;
}
// The Y corrdinate of the ground plane
private static final float PLANE_Y = 200.5f;

private final ArrayList<Scenario> SCENARIOS = new ArrayList<Scenario>();
private int activeScenarioIdx;

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
  
  /**
   * Sets the value of the given dimension associated with
   * this shape, if that dimension exists.
   * 
   * @param newVal  The value to which to set the dimension
   * @param dim     The dimension of  which ro set the value
   */
  public abstract void setDim(Float newVal, DimType dim);
  
  /**
   * Returns the value of the given dimension associated with
   * this shape. If no such dimension exists, then -1 should
   * be returned.
   * 
   * @param dim  The dimension of which to get the value
   * @returning  The value of that dimension, or -1, if no
   *             such dimension exists
   */
  public abstract float getDim(DimType dim);
  
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
  
  @Override
  public void setDim(Float newVal, DimType dim) {
    
    switch (dim) {
      case LENGTH:
      // Update length
        dimensions.x = newVal;
        break;
      case HEIGHT:
      // Update height
        dimensions.y = newVal;
        break;
        
      case WIDTH:
        // Update width
        dimensions.z = newVal;
        break;
      // Invalid dimension
      default:
    }
  }
  
  @Override
  public float getDim(DimType dim) {    
    switch (dim) {
      case LENGTH:  return dimensions.x;
      case HEIGHT:  return dimensions.y;
      case WIDTH:   return dimensions.z;
      // Invalid dimension
      default:      return -1f;
    }
  }
  
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
   * 
   * Based off of the algorithm defined on Vormplus blog at:
   * http://vormplus.be/blog/article/drawing-a-cylinder-with-processing
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
  
  @Override
  public void setDim(Float newVal, DimType dim) {
    switch(dim) {
      case RADIUS:
      // Update radius
        radius = newVal;
        break;
        
      case HEIGHT:
        // Update height
        height = newVal;
        break;
        
      default:
    }
  }
  
  @Override
  public float getDim(DimType dim) {
    switch(dim) {
      case RADIUS:  return radius;
      case HEIGHT:  return height;
      // Invalid dimension
      default:      return -1f;
    }
  }
  
  public Shape clone() {
    Cylinder copy = new Cylinder(getFillColor(), getOutlineColor(), radius, height);
    copy.setFillFlag( isFilled() );
    return copy;
  }
}

/**
 * A complex shape formed from a .stl source file.
 */
public class ModelShape extends Shape {
  private PShape form;
  private String srcFilePath;
  
  /**
   * Create a complex model from the soruce .stl file of the
   * given name, filename, stored in the '/RobotRun/data/'
   * with the given fill and outline colors.
   * 
   * @throws NullPointerException  if the given filename is
   *         not a valid .stl file in RobotRun/data/
   */
  public ModelShape(String filename, color fill, color outline) throws NullPointerException {
    super(fill, outline);
    srcFilePath = filename;
    form = loadSTLModel(filename, fill, outline, 1.0);
  }
  
  public void draw() {
    shape(form);
  }
  
  @Override
  public void setDim(Float newVal, DimType dim) {}
  @Override
  public float getDim(DimType dim) { return -1f; }
  
  public String getSourcePath() { return srcFilePath; }
  
  /**
   * Create a new Model form the original source file.
   */
  public Shape clone() {
      return new ModelShape(srcFilePath, getFillColor(), getOutlineColor());
  }
}

public class Ray {
  private PVector origin;
  private PVector direction;
  
  public Ray() {
    origin = new PVector(0f, 0f, 0f);
    direction = new PVector(1f, 1f, 1f);
  }
  
  public Ray(PVector origin, PVector pointOnRay) {
    this.origin = origin.copy();
    direction = pointOnRay.sub(origin);
    direction.normalize();
  }
  
  public void draw() {
    stroke(0);
    noFill();
    PVector endpoint = PVector.add(origin, PVector.mult(direction, 5000f));
    line(origin.x, origin.y, origin.z, endpoint.x, endpoint.y, endpoint.z);
  }
}

/**
 * Defines the axes and origin vector associated with a Coordinate System.
 */
public class CoordinateSystem {
  private PVector origin;
  /* A 3x3 rotation matrix */
  private float[][] axesVectors;
  
  public CoordinateSystem() {
    /* Pull origin and axes from the current transformation matrix */
    origin = getCoordFromMatrix(0f, 0f, 0f);
    axesVectors = getRotationMatrix();
  }
  
  /**
   * Create a coordinate syste with the given origin and 3x3 rotation matrix.
   */
  public CoordinateSystem(PVector origin, float[][] axes) {
    this.origin = origin.copy();
    axesVectors = new float[3][3];
    // Copy axes into axesVectors
    for (int row = 0; row < 3; ++row) {
      for (int col = 0; col < 3; ++col) {
        axesVectors[row][col] = axes[row][col];
      }
    }
  }
  
  /**
   * Apply the coordinate system's origin and axes to the current transformation matrix.
   */
  public void apply() {
    applyMatrix(axesVectors[0][0], axesVectors[1][0], axesVectors[2][0], origin.x,
                axesVectors[0][1], axesVectors[1][1], axesVectors[2][1], origin.y,
                axesVectors[0][2], axesVectors[1][2], axesVectors[2][2], origin.z,
                                0,                 0,                 0,        1);
  }
  
  public void setOrigin(PVector newCenter) {
    origin = newCenter;
  }
  
  public PVector getOrigin() { return origin; }
  
  /**
   * Reset the coordinate system's axes vectors and return the
   * old axes; the given rotation matrix should be in row
   * major order!
   */
  public void setAxes(float[][] newAxes) {
    axesVectors = new float[3][3];
    
    // Copy axes into axesVectors
    for (int row = 0; row < 3; ++row) {
      for (int col = 0; col < 3; ++col) {
        axesVectors[row][col] = newAxes[row][col];
      }
    }
  }
  
  /**
   * Return this coordinate system's axes in row major order.
   */
  public float[][] getAxes() {
    return axesVectors;
  }
}

/**
 * A box object with its own local Coordinate system.
 */
public class BoundingBox {
  private CoordinateSystem localOrientation;
  /* The origin of the bounding box's local Coordinate System */
  private Box boundingBox;
  
  /**
   * Create a cube object with the given colors and dimension
   */
  public BoundingBox() {
    localOrientation = new CoordinateSystem();
    boundingBox = new Box(color(0, 0, 255), 10f);
  }
  
  /**
   * Create a cube object with the given colors and dimension
   */
  public BoundingBox(float edgeLen) {
    localOrientation = new CoordinateSystem();
    boundingBox = new Box(color(0, 0, 255), edgeLen);
  }
  
  /**
   * Create a box object with the given colors and dimensions
   */
  public BoundingBox(float len, float hgt, float wdh) {
    localOrientation = new CoordinateSystem();
    boundingBox = new Box(color(0, 0, 255), len, hgt, wdh);
  }
  
  /**
   * Apply the Coordinate System of the bounding-box onto the
   * current transformation matrix.
   */
  public void applyCoordinateSystem() {
    localOrientation.apply();
  }
  
  /**
   * Reset the bounding-box's coordinate system to the current
   * transformation matrix.
   */
  public void setCoordinateSystem() {
    localOrientation = new CoordinateSystem();
  }
  
  /**
   * Draw both the object and its bounding box;
   */
  public void draw() {
    pushMatrix();
    // Draw shape in its own coordinate system
    localOrientation.apply();
    boundingBox.draw();
    popMatrix();
  }
  
  /**
   * Reset the object's center point
   */
  public void setCenter(PVector newCenter) {
    localOrientation.setOrigin(newCenter);
  }
  
  public PVector getCenter() { return localOrientation.getOrigin(); }
  
  /**
   * Reset the object's orientation axes; the given rotation
   * matrix should be in row major order!
   */
  public void setOrientationAxes(float[][] newOrientation) {
    localOrientation.setAxes(newOrientation);
  }
  
  public float[][] getOrientationAxes() {
    return localOrientation.getAxes();
  }
  
  /**
   * Sets the outline color of this ounding-box
   * to the given value.
   */
  public void setColor(color newColor) {
    boundingBox.setOutlineColor(newColor);
  }
  
  /**
   * See Box.setDim()
   */
  public void setDim(Float newVal, DimType dim) {
    boundingBox.setDim(newVal, dim);
  }
  
  /**
   * See Box.getDim()
   */
  public float getDim(DimType dim) {
    return boundingBox.getDim(dim);
  }
  
  /**
   * Sets all the dimension values of the
   * bounding-box, where:
   * X -> length
   * Y -> height
   * Z -> width
   */
  public void setDims(PVector newDims) {
    boundingBox.setDim(newDims.x, DimType.LENGTH);
    boundingBox.setDim(newDims.y, DimType.HEIGHT);
    boundingBox.setDim(newDims.z, DimType.WIDTH);
  }
  
  /**
   * Returns the bounding-box's dimension in the
   * form of a PVector: (length, height, width).
   */
  public PVector getDims() {
    PVector dims = new PVector();
    dims.x = boundingBox.getDim(DimType.LENGTH);
    dims.y = boundingBox.getDim(DimType.HEIGHT);
    dims.z = boundingBox.getDim(DimType.WIDTH);
    return dims;
  }
  
  /**
   * Return a reference to this bounding-box's box.
   */
  public Box getBox() { return boundingBox; }
  
  /**
   * Determine of a single position, in Native Coordinates, is with
   * the bounding box of the this world object.
   */
  public boolean collision(PVector point) {
    // Convert the point to the current reference frame
    float[][] tMatrix = transformationMatrix(localOrientation.getOrigin(), localOrientation.getAxes());
    PVector relPosition = transform(point, invertHCMatrix(tMatrix));
    
    PVector OBBDim = getDims();
    // Determine if the point iw within the bounding-box of this object
    boolean is_inside = relPosition.x >= -(OBBDim.x / 2f) && relPosition.x <= (OBBDim.x / 2f)
                     && relPosition.y >= -(OBBDim.y / 2f) && relPosition.y <= (OBBDim.y / 2f)
                     && relPosition.z >= -(OBBDim.z / 2f) && relPosition.z <= (OBBDim.z / 2f);
    
    return is_inside;
  }
  
  /**
   * Return a replicate of this world object's Bounding Box
   */
  public BoundingBox clone() {
    pushMatrix();
    localOrientation.apply();
    PVector dims = getDims();
    BoundingBox copy = new BoundingBox(dims.x, dims.y, dims.z);
    copy.setColor( boundingBox.getOutlineColor() );
    popMatrix();
    
    return copy;
  }
}

/**
 * Any object in the World other than the Robot.
 */
public abstract class WorldObject {
  private String name;
  private Shape form;
  protected CoordinateSystem localOrientation;
  
  public WorldObject() {
    name = "Object";
    form = new Box();
    localOrientation = new CoordinateSystem();
  }
  
  public WorldObject(String n, Shape f) {
    name = n;
    form = f;
    localOrientation = new CoordinateSystem();
  }
  
  public WorldObject(String n, Shape f, CoordinateSystem cs) {
    name = n;
    form = f;
    localOrientation = cs;
  }
  
  /**
   * Apply the local Coordinate System of the World Object.
   */
  public void applyCoordinateSystem() {
    localOrientation.apply();
  }
  /**
   * Transform the World Object's local Coordinate System to
   * the current transformation matrix.
   */
  public void setCoordinateSystem() {
    localOrientation = new CoordinateSystem();
  }
  
  /**
   * Draw the world object in its local orientation.
   */
  public void draw() {
    pushMatrix();
    // Draw shape in its own coordinate system
    applyCoordinateSystem();
    getForm().draw();
    popMatrix();
  }
  
  /**
   * Returns a list of values with short prefix labels, which descibe
   * the dimensions of the this world object's shape (except for Model
   * shapes, because their dimensions are unknown).
   * 
   * @returning  A non-null, variable length string array
   */
  public String[] dimFieldsToStringArray() {
    String[] fields;
    
    if (form instanceof Box) {
      fields = new String[3];
      // Add the box's length, height, and width values
      fields[0] = String.format("L: %4.3f", form.getDim(DimType.LENGTH));
      fields[1] = String.format("H: %4.3f", form.getDim(DimType.HEIGHT));
      fields[2] = String.format("W: %4.3f", form.getDim(DimType.WIDTH));
      
    } else if (form instanceof Cylinder) {
      fields = new String[2];
      // Add the cylinder's radius and height values
      fields[0] = String.format("R: %4.3f", form.getDim(DimType.RADIUS));
      fields[1] = String.format("H: %4.3f", form.getDim(DimType.HEIGHT));
      
    } else if (form instanceof ModelShape) {
      if (this instanceof Part)  {
        // Use bounding-box dimensions instead
        fields = new String[3];
        PVector dims = ((Part)this).getOBBDims();
        
        fields[0] = String.format("L: %4.3f", dims.x);
        fields[1] = String.format("H: %4.3f", dims.y);
        fields[2] = String.format("W: %4.3f", dims.z);
        
      } else {
        // No dimensios to display
        fields = new String[0];
      }
      
    } else {
      // Invalid shape
      fields = new String[0];
    }
    
    return fields;
  }
  
  // Getter and Setter methods for the World Object's local orientation, name, and form
  
  public void setLocalCenter(PVector newCenter) { localOrientation.setOrigin(newCenter); }
  
  /**
   * Updates all non-null values of the object's center position.
   * If a given value is null, then the origin value remains unchanged.
   * 
   * @param x  The new x value*
   * @param y  The new y value*
   * @param z  The new z value*
   *           *null indicates that the origin value will remain unchanged
   */
  public void updateLocalCenter(Float x, Float y, Float z) {
    PVector center = localOrientation.getOrigin();
    
    if (x != null) {
      // Update x value
      center.x = x;
    }
    if (y != null) {
      // Update y value
      center.y = y;
    }
    if (z != null) {
      // update z value
      center.z = z;
    }
  }
  
  public PVector getLocalCenter() { return localOrientation.getOrigin(); }
  
  public void setLocalOrientationAxes(float[][] newAxes) {
    localOrientation.setAxes(newAxes);
  }
  
  public float[][] getLocalOrientationAxes() {
    return localOrientation.getAxes();
  }
  
  public void setName(String newName) { name = newName; }
  public String getName() { return name; }
  
  public Shape getForm() { return form; }
  public String toString() { return name; }
}

/**
 * A world object whose Coordinate System can be referenced by a Part
 * as its parent Coordinate System.
 */
public class Fixture extends WorldObject {
  
  /**
   * Create a cube object with the given colors and dimension
   */
  public Fixture(String n, color fill, color outline, float edgeLen) {
    super(n, new Box(fill, outline, edgeLen));
  }
  
  /**
   * Create a box object with the given colors and dimensions
   */
  public Fixture(String n, color fill, color outline, float len, float hgt, float wdh) {
    super(n, new Box(fill, outline, len, hgt, wdh));
  }
  
  /**
   * Creates a cylinder object with the given colors and dimensions.
   */
  public Fixture(String n, color fill, color outline, float rad, float hgt) {
    super(n, new Cylinder(fill, outline, rad, hgt));
  }
  
  /**
   * Creates a fixture with the given name and shape.
   */
  public Fixture(String n, ModelShape model) {
    super(n, model);
  }
  
  /**
   * Creates a fixture with the given name and shape, and coordinate system.
   */
  public Fixture(String n, Shape s, CoordinateSystem cs) {
    super(n, s, cs);
  }
}

/**
 * Defines a world object, which has a shape, a bounding box and a reference to a fixture.
 * The bounding box holds the local coordinate system of the object.
 */
public class Part extends WorldObject {
  private BoundingBox absOBB;
  private Fixture reference;
  
  /**
   * Create a cube object with the given colors and dimension
   */
  public Part(String n, color fill, color outline, float edgeLen) {
    super(n, new Box(fill, outline, edgeLen));
    absOBB = new BoundingBox(edgeLen + 15f);
  }
  
  /**
   * Create a box object with the given colors and dimensions
   */
  public Part(String n, color fill, color outline, float len, float hgt, float wdh) {
    super(n, new Box(fill, outline, len, hgt, wdh));
    absOBB = new BoundingBox(len + 15f, hgt + 15f, wdh + 15f);
  }
  
  /**
   * Creates a cylinder objects with the given colors and dimensions.
   */
  public Part(String n, color fill, color outline, float rad, float hgt) {
    super(n, new Cylinder(fill, outline, rad, hgt));
    absOBB = new BoundingBox(2f * rad + 5f, 2f * rad + 5f, hgt + 10f);
  }
  
  /**
   * Define a complex object as a part with given dimensions for its bounding-box.
   */
  public Part(String n, ModelShape model, float OBBLen, float OBBHgt, float OBBWid) {
    super(n, model);
    absOBB = new BoundingBox(OBBLen, OBBHgt, OBBWid);
  }
  
  /**
   * Creates a Part with the given name, shape, bounding-box dimensions, and fixture reference.
   */
  public Part(String n, Shape s, PVector OBBDims, CoordinateSystem local, Fixture fixRef) {
    super(n, s, local);
    absOBB = new BoundingBox(OBBDims.x, OBBDims.y, OBBDims.z);
    setFixtureRef(fixRef);
  }
  
  public void applyCoordinateSystem() {
    absOBB.applyCoordinateSystem();
  }
  
  public void setCoordinateSystem() {
    absOBB.setCoordinateSystem();
  }
  
  public void applyLocalCoordinateSystem() {
    super.applyCoordinateSystem();
  }
  
  public void setLocalCoordinateSystem() {
    super.setCoordinateSystem();
  }
  
  /**
   * Draw both the object and its bounding box in its local
   * orientaiton, in the local orientation of the part's
   * fixture reference.
   */
  public void draw() {
    pushMatrix();
    applyCoordinateSystem();
    getForm().draw();
    absOBB.getBox().draw();
    popMatrix();
  }
  
  /**
   * Set the fixture reference of this part and
   * update its absolute orientation.
   */
  public void setFixtureRef(Fixture refFixture) {
    reference = refFixture;
    updateAbsoluteOrientation();
  }
  
  public Fixture getFixtureRef() { return reference; }
  
  /**
   * Update the Part's absolute (or world) orientation
   * based om its local orientation and fixture
   * reference's orientation.
   */
  private void updateAbsoluteOrientation() {
    pushMatrix();
    resetMatrix();
    
    if (reference != null) {
      reference.applyCoordinateSystem();
    }
    
    super.applyCoordinateSystem();
    absOBB.setCoordinateSystem();
    popMatrix();
  }
  
  /**
   * See BoundingBox.setDim()
   */
  public void setOBBDim(Float newVal, DimType dim) {
    absOBB.setDim(newVal, dim);
  }
  
  /**
   * Set the dimensions of this part's bounding box.
   */
  public void setOBBDimenions(PVector newDims) {
    absOBB.setDims(newDims);
  }
  
  /**
   * Update all non-null dimensions of the part's bounding-box. This method functions
   * similiar to updateLocalCenter().
   * 
   * @param newLength  The new length value*
   * @param newHeight  The new height value*
   * @param newWidth   The new length value*
   *                   *null indicates that the origin value will be unchanged
   */
  public void updateOBBDimensions(Float newLength, Float newHeight, Float newWidth) {
    if (newLength != null) {
      // Update the length
      absOBB.setDim(newLength, DimType.LENGTH);
    }
    if (newHeight != null) {
      // Update the height
      absOBB.setDim(newHeight, DimType.HEIGHT);
    }
    if (newWidth != null) {
      // Update the width
      absOBB.setDim(newWidth, DimType.WIDTH);
    }
  }
  
  /**
   * Get the dimensions of the part's bounding-box
   */
  public PVector getOBBDims() {
    return absOBB.getDims();
  }
  
  /**
   * Return a reference to this object's bounding-box.
   */ 
  public BoundingBox getOBB() { return absOBB; }
  
  /**
   * Sets the outline color of the world's bounding-box
   * to the given value.
   */
  public void setBBColor(color newColor) {
    absOBB.setColor(newColor);
  }
  
  /**
   * Determine if the given world object is colliding
   * with this world object.
   */
  public boolean collision(Part obj) {
    return collision3D(absOBB, obj.getOBB());
  }
  
  /**
   * Determies if the given bounding box is colliding
   * with this Part's bounding box.
   */
  public boolean collision(BoundingBox obb) {
    return collision3D(absOBB, obb);
  }
  
  /**
   * Determine if the given point is within
   * this object's bounding box.
   */
  public boolean collision(PVector point) {
    return absOBB.collision(point);
  }
  
  public void setLocalCenter(float x, float y, float z) {
    super.updateLocalCenter(x, y, z);
    updateAbsoluteOrientation();
  }
  
  public void setLocalOrientationAxes(float[][] newAxes) {
    super.setLocalOrientationAxes(newAxes);
    updateAbsoluteOrientation();
  }
}

/**
 * A storage class for a collection of objects with an associated name for the collection.
 */
public class Scenario implements Iterable<WorldObject> {
  private String name;
  /**
   * A combine list of Parts and Fixtures
   */
  private final ArrayList<WorldObject> objList;
  
  /**
   * Create a new scenario of the given name.
   */
  public Scenario(String n) {
    name = n;
    objList = new ArrayList<WorldObject>();
  }
  
  /**
   * Only adds the given world objects that are non-null and do
   * not already exist in the scenario.
   * 
   * @param newObjs  The world objects to add to the scenario
   */
  public void addWorldObjects(WorldObject... newObjs) {
    
    for (WorldObject obj : newObjs) {
        addWorldObject(obj);
    }
  }
  
  /**
   * Add the given world object to the scenario. Though, if the name of
   * the given world object does not only contain letter and number
   * characters, then the object is not added to either list.
   * 
   * @param newObject  The object to be added to either the Part or Fixture
   *                   list
   * @returning        Whether the object was added to a list or not
   */
  public boolean addWorldObject(WorldObject newObject) {
    if (newObject == null || objList.contains(newObject)) {
      // Ignore nulls and duplicates
      if (newObject == null) {
        println("New Object is null");
      } else {
        println("New Object is: " + newObject.toString());
      }
      
      return false;
    }
    
    String originName = newObject.getName();
    
    if (originName.length() > 16) {
      // Base name length caps at 16 charcters
      newObject.setName( originName.substring(0, 16) );
      originName = newObject.getName();
    }
    
    if (Pattern.matches("[a-zA-Z0-9]+", originName)) {
    
      if (findObjectWithName(originName, objList) != null) {
        // Keep names unique
        newObject.setName( addSuffixForDuplicateName(originName, objList) );
      }
      
      // TODO add in alphabetical order
      objList.add(newObject);
      return true;
    }
    
    return false;
  }
  
  /**
   * Attempt to remove the given set of world objects from the scenario.
   * 
   * @param tgtObjs  The objects to remove from the scenario
   * @returning      The number of the given objects that were successfully
   *                 removed from the scenario
   */
  public int removeWorldObjects(WorldObject... tgtObjs) {
    int objsRemoved = 0;
    
    for (WorldObject tgt : tgtObjs) {
      int ret = removeWorldObject(tgt);
      // Keep track of the number of given targets that were successfully removed
      if (ret == 0 || ret == 2) {
        ++objsRemoved;
      }
    }
    
    return objsRemoved;
  }
  
  /**
   * Delete the given world object from the correct object
   * list, if it exists in the list.
   * 
   * @returning  0 if the object was removed succesfully,
   *             1 if the object did not exist in the scenario,
   *             2 if the object was a Fixture that was removed
   *                from the scenario and was referenced by at
   *                least one Part in the scenario
   */
  public int removeWorldObject(WorldObject toRemove) {
    if (toRemove == null) {
      return 1;
    }
    
    int ret;
    // Remove a fixture from the list
    boolean removed = objList.remove(toRemove);
    
    ret = (removed) ? 0 : 1;
    
    if (removed && toRemove instanceof Fixture) {
      // Remove the reference from all Part objects associated with this fixture
      for (WorldObject obj : objList) {
        
        if (obj instanceof Part) {
          Part part = (Part)obj;
          
          if (part.getFixtureRef() == toRemove) {
            part.setFixtureRef(null);
            ret = 2;
          }
        }
      }
    }
    
    return ret;
  }
  
  /**
   * Return the world object that corresponds to the given index in
   * the list of world objects contained in this scenario, or null
   * if the index is invalid.
   * 
   * @param idx  A valid index
   * @returning  The world object, at the given index in the list,
   *             or null
   */
  public WorldObject getWorldObject(int idx) {
    if (idx >= 0 && idx < size()) {
      return objList.get(idx);
    }
    
    return null;
  }
  
  /**
   * Updates the collision detection of all the Parts in the scenario,
   * using the given ArmModel to detect collisions between world objects
   * and the armModel, and draw every object.
   */
  public void updateAndDrawObjects(ArmModel model) {
    int numOfObjects = objList.size();
    
    for (WorldObject wldObj : objList) {
      if (wldObj instanceof Part) {
        // Reset all Part bounding-box colors
        ((Part)wldObj).setBBColor(color(0, 255, 0));
      }
    }
    
    for (int idx = 0; idx < numOfObjects; ++idx) {
      WorldObject wldObj = objList.get(idx);
      
      if (wldObj instanceof Part) {
        Part p = (Part)wldObj;
        
        /* Update the transformation matrix of an object held by the Robotic Arm */
        if(model != null && p == model.held && model.modelInMotion()) {
          pushMatrix();
          resetMatrix();
          
          // new object transform = EE transform x (old EE transform) ^ -1 x current object transform
          
          applyModelRotation(model.getJointAngles());
          
          float[][] invEETMatrix = invertHCMatrix(armModel.oldEETMatrix);
          applyMatrix(invEETMatrix[0][0], invEETMatrix[1][0], invEETMatrix[2][0], invEETMatrix[0][3],
                      invEETMatrix[0][1], invEETMatrix[1][1], invEETMatrix[2][1], invEETMatrix[1][3],
                      invEETMatrix[0][2], invEETMatrix[1][2], invEETMatrix[2][2], invEETMatrix[2][3],
                                       0,                 0,                   0,                  1);
          
          armModel.held.applyLocalCoordinateSystem();
          // Update the world object's position and orientation
          armModel.held.setLocalCoordinateSystem();
          armModel.held.updateAbsoluteOrientation();
          popMatrix();
        }
        
        /* Collision Detection */
        if(COLLISION_DISPLAY) {
          if( model != null && model.checkObjectCollision(p) ) {
            p.setBBColor(color(255, 0, 0));
          }
          
          // Detect collision with other objects
          for(int cdx = idx + 1; cdx < objList.size(); ++cdx) {
            
            if (objList.get(cdx) instanceof Part) {
              Part p2 = (Part)objList.get(cdx);
              
              if(p.collision(p2)) {
                // Change hit box color to indicate Object collision
                p.setBBColor(color(255, 0, 0));
                p2.setBBColor(color(255, 0, 0));
                break;
              }
            }
          }
          
          if( model != null && p != model.held && p.collision( nativeRobotEEPoint(model.getJointAngles()).position) ) {
            // Change hit box color to indicate End Effector collision
            p.setBBColor(color(0, 0, 255));
          }
        }
        
        if (p == manager.getActiveWorldObject()) {
          p.setBBColor(color(255, 255, 0));
        }
      }
      // Draw the object
      wldObj.draw();
    }
  }
  
  /**
   * Adds a number suffix to the given name, so that the name is unique amonst the names of all the other world
   * objects in the given list. So, if the given name is 'block' and objects with names 'block', 'block1', and
   * 'block2' exist in wldObjList, then the new name will be 'block3'.
   * 
   * @param originName  The origin name of the new world object
   * @param eldObjList  The list of world objects, of wixh to check names
   * @returning         A unique name amongst the names of the existing world objects in the given list, that
   *                    contains the original name as a prefix
   */
  private <T extends WorldObject> String addSuffixForDuplicateName(String originName, ArrayList<T> wldObjList) {
    int nameLen = originName.length();
    ArrayList<Integer> suffixes = new ArrayList<Integer>();
    
    for (T wldObj : wldObjList) {
      String objName = wldObj.getName();
      int objNameLen = objName.length();
      
      if (objNameLen > nameLen) {
        String namePrefix = objName.substring(0, nameLen),
               nameSuffix = objName.substring(nameLen, objNameLen);
        // Find all strings that have the given name as a prefix and an integer value suffix
        if (namePrefix.equals(originName) && Pattern.matches("[0123456789]+", nameSuffix)) {
          int suffix = Integer.parseInt(nameSuffix),
              insertIdx = 0;
          // Store suffixes in increasing order
          while (insertIdx < suffixes.size() && suffix > suffixes.get(insertIdx)) {
            ++insertIdx;
          }
          
          if (insertIdx == suffixes.size()) {
            suffixes.add(suffix);
          } else {
            suffixes.add(insertIdx, suffix);
          }
        }
      }
    }
    // Determine the minimum suffix value
    int suffix = 0;
    
    if (suffixes.size() == 1 && suffixes.get(0) == 0) {
      // If the only stirng with a suffix has a suffix of '0'
      suffix = 1;
      
    } else if (suffixes.size() >= 2) {
      int idx = 0;
      
      while ((idx + 1) < suffixes.size()) {
        // Find the first occurance of a gap between to adjacent suffix values (if any)
        if ((suffixes.get(idx + 1) - suffixes.get(idx)) > 1) {
          break;
        }
        
        ++idx;
      }
      
      suffix = suffixes.get(idx) + 1;
    }
    // Concatenate the origin name with the new suffix
    return String.format("%s%d", originName, suffix);
  }
  
  /**
   * Attempts to find the world object, in the given list, with the given name. If no such object exists,
   * then null is returned, otherwise the object with the given name is returned.
   * 
   * @param tgtName     The name of the world object to find
   * @param wldObjList  The list of world objects to check
   * @returning         The object with the given name, if it exists in the given list, or null.
   */
  private <T extends WorldObject> WorldObject findObjectWithName(String tgtName, ArrayList<T> wldObjList) {
    
    if (tgtName != null && wldObjList != null) {
      
      for (T obj : wldObjList) {
        // Determine if the object exists
        if (obj != null && obj.getName().equals(tgtName)) {
          return obj;
        }
      }
    }
    
    return null;
  }
  
  @Override
  public Iterator<WorldObject> iterator() {
    return objList.iterator();
  }
  
  public int size() { return objList.size(); }
  
  public void setName(String newName) { name = newName; }
  public String getName() { return name; }
  
  public String toString() { return name; }
}

/**
 * Build a PShape object from the contents of the given .stl source file
 * stored in /RobotRun/data/.
 * 
 * @throws NullPointerException  if hte given filename does not pertain
 *         to a valid .stl file located in RobotRun/data/
 */
public PShape loadSTLModel(String filename, color fill, color outline, float scaleVal) throws NullPointerException {
  ArrayList<Triangle> triangles = new ArrayList<Triangle>();
  byte[] data = loadBytes(filename);
  
  int n = 84; // skip header and number of triangles
  
  while(n < data.length) {
    Triangle t = new Triangle();
    for(int m = 0; m < 4; m++) {
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
  
  PShape mesh = createShape();
  mesh.beginShape(TRIANGLES);
  mesh.scale(scaleVal);
  mesh.stroke(outline);
  mesh.fill(fill);
  for(Triangle t : triangles) {
    mesh.normal(t.components[0].x, t.components[0].y, t.components[0].z);
    mesh.vertex(t.components[1].x, t.components[1].y, t.components[1].z);
    mesh.vertex(t.components[2].x, t.components[2].y, t.components[2].z);
    mesh.vertex(t.components[3].x, t.components[3].y, t.components[3].z);
  }
  mesh.endShape();
  
  return mesh;
} 

/**
 * Returns the scenario located that the index of activeScenarioIdx or null
 * if activeScenarioIdx is invalid.
 */
public Scenario activeScenario() {
  
  if (activeScenarioIdx >= 0 && activeScenarioIdx < SCENARIOS.size()) {
    return SCENARIOS.get(activeScenarioIdx);
    
  } else {
    //System.out.printf("Invalid scenaro index: %d!\n", activeScenarioIdx);
    return null;
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
  float[][] axes_A = A.getOrientationAxes();
  float[][] axes_B = B.getOrientationAxes();
  
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
    
    if (idx == 0) {
      radiA = A.getDim(DimType.LENGTH) / 2f;
    } else if (idx == 1) {
      radiA = A.getDim(DimType.HEIGHT) / 2f;
    } else {
      radiA = A.getDim(DimType.WIDTH) / 2f;
    }
    
    radiB = (B.getDim(DimType.LENGTH) / 2) * absRotMatrix[idx][0] + 
    (B.getDim(DimType.HEIGHT) / 2) * absRotMatrix[idx][1] + 
    (B.getDim(DimType.WIDTH) / 2) * absRotMatrix[idx][2];
    
    // Check Ax, Ay, and Az
    if(abs(T[idx]) > (radiA + radiB)) { return false; }
  }
  
  for(int idx = 0; idx < absRotMatrix[0].length; ++idx) {
    radiA = (A.getDim(DimType.LENGTH) / 2) * absRotMatrix[0][idx] + 
    (A.getDim(DimType.HEIGHT) / 2) * absRotMatrix[1][idx] + 
    (A.getDim(DimType.WIDTH) / 2) * absRotMatrix[2][idx];
    
    if (idx == 0) {
      radiB = B.getDim(DimType.LENGTH) / 2f;
    } else if (idx == 1) {
      radiB = B.getDim(DimType.HEIGHT) / 2f;
    } else {
      radiB = B.getDim(DimType.WIDTH) / 2f;
    }
    
    float check = abs(T[0]*rotMatrix[0][idx] + 
    T[1]*rotMatrix[1][idx] + 
    T[2]*rotMatrix[2][idx]);
    
    // Check Bx, By, and Bz
    if(check > (radiA + radiB)) { return false; }
  }
  
  radiA = (A.getDim(DimType.HEIGHT) / 2) * absRotMatrix[2][0] + (A.getDim(DimType.WIDTH) / 2) * absRotMatrix[1][0];
  radiB = (B.getDim(DimType.HEIGHT) / 2) * absRotMatrix[0][2] + (B.getDim(DimType.WIDTH) / 2) * absRotMatrix[0][1];
  // Check axes Ax x Bx
  if(abs(T[2] * rotMatrix[1][0] - T[1] * rotMatrix[2][0]) > (radiA + radiB)) { return false; }
  
  radiA = (A.getDim(DimType.HEIGHT) / 2) * absRotMatrix[2][1] + (A.getDim(DimType.WIDTH) / 2) * absRotMatrix[1][1];
  radiB = (B.getDim(DimType.LENGTH) / 2) * absRotMatrix[0][2] + (B.getDim(DimType.WIDTH) / 2) * absRotMatrix[0][0];
  // Check axes Ax x By
  if(abs(T[2] * rotMatrix[1][1] - T[1] * rotMatrix[2][1]) > (radiA + radiB)) { return false; }
  
  radiA = (A.getDim(DimType.HEIGHT) / 2) * absRotMatrix[2][2] + (A.getDim(DimType.WIDTH) / 2) * absRotMatrix[1][2];
  radiB = (B.getDim(DimType.LENGTH) / 2) * absRotMatrix[0][1] + (B.getDim(DimType.HEIGHT) / 2) * absRotMatrix[0][0];
  // Check axes Ax x Bz
  if(abs(T[2] * rotMatrix[1][2] - T[1] * rotMatrix[2][2]) > (radiA + radiB)) { return false; }
  
  radiA = (A.getDim(DimType.LENGTH) / 2) * absRotMatrix[2][0] + (A.getDim(DimType.WIDTH) / 2) * absRotMatrix[0][0];
  radiB = (B.getDim(DimType.HEIGHT) / 2) * absRotMatrix[1][2] + (B.getDim(DimType.WIDTH) / 2) * absRotMatrix[1][1];
  // Check axes Ay x Bx
  if(abs(T[0] * rotMatrix[2][0] - T[2] * rotMatrix[0][0]) > (radiA + radiB)) { return false; }
  
  radiA = (A.getDim(DimType.LENGTH) / 2) * absRotMatrix[2][1] + (A.getDim(DimType.WIDTH) / 2) * absRotMatrix[0][1];
  radiB = (B.getDim(DimType.LENGTH) / 2) * absRotMatrix[1][2] + (B.getDim(DimType.WIDTH) / 2) * absRotMatrix[1][0];
  // Check axes Ay x By
  if(abs(T[0] * rotMatrix[2][1] - T[2] * rotMatrix[0][1]) > (radiA + radiB)) { return false; }
  
  radiA = (A.getDim(DimType.LENGTH) / 2) * absRotMatrix[2][2] + (A.getDim(DimType.WIDTH) / 2) * absRotMatrix[0][2];
  radiB = (B.getDim(DimType.LENGTH) / 2) * absRotMatrix[1][1] + (B.getDim(DimType.HEIGHT) / 2) * absRotMatrix[1][0];
  // Check axes Ay x Bz
  if(abs(T[0] * rotMatrix[2][2] - T[2] * rotMatrix[0][2]) > (radiA + radiB)) { return false; }
  
  
  radiA = (A.getDim(DimType.LENGTH) / 2) * absRotMatrix[1][0] + (A.getDim(DimType.HEIGHT) / 2) * absRotMatrix[0][0];
  radiB = (B.getDim(DimType.HEIGHT) / 2) * absRotMatrix[2][2] + (B.getDim(DimType.WIDTH) / 2) * absRotMatrix[2][1];
  // Check axes Az x Bx
  if(abs(T[1] * rotMatrix[0][0] - T[0] * rotMatrix[1][0]) > (radiA + radiB)) { return false; }
  
  radiA = (A.getDim(DimType.LENGTH) / 2) * absRotMatrix[1][1] + (A.getDim(DimType.HEIGHT) / 2) * absRotMatrix[0][1];
  radiB = (B.getDim(DimType.LENGTH) / 2) * absRotMatrix[2][2] + (B.getDim(DimType.WIDTH) / 2) * absRotMatrix[2][0];
  // Check axes Az x By
  if(abs(T[1] * rotMatrix[0][1] - T[0] * rotMatrix[1][1]) > (radiA + radiB)) { return false; }
  
  radiA = (A.getDim(DimType.LENGTH) / 2) * absRotMatrix[1][2] + (A.getDim(DimType.HEIGHT) / 2) * absRotMatrix[0][2];
  radiB = (B.getDim(DimType.LENGTH) / 2) * absRotMatrix[2][1] + (B.getDim(DimType.HEIGHT) / 2) * absRotMatrix[2][0];
  // Check axes Az x Bz
  if(abs(T[1] * rotMatrix[0][2] - T[0] * rotMatrix[1][2]) > (radiA + radiB)) { return false; }
  
  return true;
}
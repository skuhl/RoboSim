/**
 * A basic definition of a shape in processing that has a fill and outline color.
 */
public abstract class Shape {
  protected color fill;
  protected color outline;
  protected final boolean no_fill;
  
  /* Create a shpae with the given outline/fill colors */
  public Shape(color f, color o) {
    fill = f;
    outline = o;
    no_fill = false;
  }
  
  /* Creates a shape with no fill */
  public Shape(color o) {
    outline = o;
    no_fill = true;
  } 
  
   /* Redefine the center point of the shape */
  public abstract void setCenter(float x, float y, float z);
  
  /* Returns the x, y, z values of the shape's center point */
  public abstract float[] getCenter();
  
  /* Define the transformation matrix for the coordinate system of the shape */
  public abstract void setTransform(float[][] tMatrix);
  
  /* Applies necessary rotations and translations to convert the Native cooridinate
   * system into the cooridnate system relative to the center of the Shape */
  public abstract void applyRelativeAxes();
  
  /* Returns a 3x3 matrix, whose rows contain the x, y, z axes of the Shape's relative
   * coordinate frame in native coordinates */
  public abstract float[][] getRelativeAxes();
  
  /* Define how a shape is drawn in the window */
  public abstract void draw();
}

/**
 * A shape that resembles a cube or rectangle
 */
public class Box extends Shape {
  public final PVector dimensions;
  public float[][] transform;
  
  /* Create a normal box */
  public Box(float x, float y, float z, float wdh, float hgt, float dph, color f, color o) {
    super(f, o);
    
    transform = new float[4][4];
    transform[0][3] = x;
    transform[1][3] = y;
    transform[2][3] = z;
    transform[0][0] = transform[1][1] = transform[2][2] = transform[3][3] = 1f;
    transform[3][0] = transform[3][1] = transform[3][2] = 0f;
    
    dimensions = new PVector(wdh, hgt, dph);
  }
  
  /* Create an empty box */
  public Box(float x, float y, float z, float wdh, float hgt, float dph, color o) {
    super(o);
    
    transform = new float[4][4];
    transform[0][3] = x;
    transform[1][3] = y;
    transform[2][3] = z;
    transform[0][0] = transform[1][1] = transform[2][2] = 1f;
    transform[3][3] = 1f;
    transform[3][0] = transform[3][1] = transform[3][2] = transform[3][3] = x;
    
    dimensions = new PVector(wdh, hgt, dph);
  }
  
  public void setCenter(float x, float y, float z) {
    transform[0][3] = x;
    transform[1][3] = y;
    transform[2][3] = z;
  }
  
  public float[] getCenter() {
    return new float[] { transform[0][3], transform[1][3], transform[2][3] };
  }
  
  public void setTransform(float[][] tMatrix) {
    transform = tMatrix;
  }
  
  public void draw() {
    stroke(outline);
    
    if (no_fill) {
      noFill();
    } else {
      fill(fill);
    }
    
    box(dimensions.x, dimensions.y, dimensions.z);
  }
  
  public float[][] getRelativeAxes() {
    float[][] Axes = new float[3][3];
    
    pushMatrix();
    resetMatrix();
    applyRelativeAxes();
    // Each ROW is a vector
    Axes[0][0] = modelX(1, 0, 0) - modelX(0, 0, 0);
    Axes[0][1] = modelY(1, 0, 0) - modelY(0, 0, 0);
    Axes[0][2] = modelZ(1, 0, 0) - modelZ(0, 0, 0);
    Axes[1][0] = modelX(0, 1, 0) - modelX(0, 0, 0);
    Axes[1][1] = modelY(0, 1, 0) - modelY(0, 0, 0);
    Axes[1][2] = modelZ(0, 1, 0) - modelZ(0, 0, 0);
    Axes[2][0] = modelX(0, 0, 1) - modelX(0, 0, 0);
    Axes[2][1] = modelY(0, 0, 1) - modelY(0, 0, 0);
    Axes[2][2] = modelZ(0, 0, 1) - modelZ(0, 0, 0);
    
    popMatrix();
    
    return Axes;
  }
  
  /* This method modifies the transform matrix! */
  public void applyRelativeAxes() {
    applyMatrix(transform[0][0], transform[0][1], transform[0][2], transform[0][3],
                transform[1][0], transform[1][1], transform[1][2], transform[1][3],
                transform[2][0], transform[2][1], transform[2][2], transform[2][3],
                transform[3][0], transform[3][1], transform[3][2], transform[3][3]);
  }
  
  /* Check if the given point is within the dimensions of the box */
  public boolean within(PVector pos) {
    
    boolean is_inside = pos.x >= -(dimensions.x / 2f) && pos.x <= (dimensions.x / 2f)
                     && pos.y >= -(dimensions.y / 2f) && pos.y <= (dimensions.y / 2f)
                     && pos.z >= -(dimensions.z / 2f) && pos.z <= (dimensions.z / 2f);
    
    return is_inside;
  }
}

public class Object {
  // The actual object
  public final Shape form;
  // The area around an object used for collision handling
  public final Shape hit_box;
  
  public Object(float x, float y, float z, float wdh, float hgt, float dph, color f, color o) {
    form = new Box(x, y, z, wdh, hgt, dph, f, o);
    // green outline for hitboxes
    hit_box = new Box(0, 0, 0, wdh + 20f, hgt + 20f, dph + 20f, color(0, 255, 0));
  }
  
  public void draw() {
    pushMatrix();
    
    form.applyRelativeAxes();
    
    form.draw();
    hit_box.draw();
    
    popMatrix();
  }
  
  public boolean collision(PVector pos) {
    pushMatrix();
    resetMatrix();
    // Switch to the Object's corrdinate system
    form.applyRelativeAxes();
    // Convert the point to the current reference frame
    pos = transform(pos, invertHCMatrix(getTransformationMatrix()));
    
    
    boolean collided = ((Box)hit_box).within(pos);
    
    popMatrix();
    
    return collided;
  }
  
  /* Determines if the collider boxes of this object
   * and the given object intersect. */
  public boolean collision(Object obj) {
    Box A = (Box)hit_box;
    Box B = (Box)obj.hit_box;
    
    return collision3D(A, B);
  }
}

public boolean collision3D(Box A, Box B) {
  return true;
}
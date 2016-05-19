/**
 * A basic definition of a shape in processing that has a fill and outline color.
 */
public abstract class Shape {
  protected color fill;
  protected color outline;
  protected final boolean no_fill;
  // The roll, pitch and yaw of the object in the world space
  protected final float[] orientation;
  
  /* Create a shpae with the given outline/fill colors */
  public Shape(color f, color o) {
    fill = f;
    outline = o;
    no_fill = false;
    
    orientation = new float[] { 0f, 0f, 0f };
  }
  
  /* Creates a shape with no fill */
  public Shape(color o) {
    outline = o;
    no_fill = true;
    
    orientation = new float[] { 0f, 0f, 0f };
  }
  
  /* Set the roll, pitch and yaw rotations of the shape in space. */
  public void setOrientation(float w, float p, float r) {
    orientation[0] = w;
    orientation[1] = p;
    orientation[2] = r;
  }
  
  /* Returns the center pooint of the shape */
  public abstract PVector center();
  
  /* Redefine the center point of the shape */
  public abstract void set_center_point(float x, float y, float z);
  
  /* Define how a shape is drawn in the window */
  public abstract void draw();

}

/**
 * A shape that resembles a cube or rectangle
 */
public class Box extends Shape {
  public final PVector center;
  public final float len, wdh, hgt;
  
  /* Create an normal box */
  public Box(PVector c, float l, float w, float h, color f, color o) {
    super(f, o);
    
    center = c;
    len = l;
    wdh = w;
    hgt = h;
  }
  
  /* Create an empty box */
  public Box(PVector c, float l, float w, float h, color o) {
    super(o);
    
    center = c;
    len = l;
    wdh = w;
    hgt = h;
  }
  
  /* Create a normal cube */
  public Box(PVector c, float edge_len, color f, color o) {
    super(f, o);
    
    center = c;
    len = edge_len;
    wdh = edge_len;
    hgt = edge_len;
  }
  
  /* Create a empty cube */
  public Box(PVector c, float edge_len,  color o) {
    super(o);
    
    center = c;
    len = edge_len;
    wdh = edge_len;
    hgt = edge_len;
  }
  
  public PVector center() {
    return new PVector(center.x, center.y, center.z);
  }
  
  public void set_center_point(float x, float y, float z) {
    center.x = x;
    center.y = y;
    center.z = z;
  }
  
  public void draw() {
    pushMatrix();
    rotateX(orientation[0]);
    rotateY(orientation[2]);
    rotateZ(orientation[1]);
    
    translate(center.x, center.y, center.z);
    stroke(outline);
    
    if (no_fill) {
      noFill();
    } else {
      fill(fill);
    }
    
    box(wdh, hgt, len);
    
    popMatrix();
  }
  
  /* Check if the given point is within the dimensions of the box */
  public boolean within(PVector pos) {
    pushMatrix();
    resetMatrix();
    
    boolean is_inside = pos.x >= (center.x - wdh / 2f) && pos.x <= (center.x + wdh / 2f)
                     && pos.y >= (center.y - hgt / 2f) && pos.y <= (center.y + hgt / 2f)
                     && pos.z >= (center.z - len / 2f) && pos.z <= (center.z + len / 2f);
    
    popMatrix();
    
    return is_inside;
  }
}

public class Object {
  // The actual object
  public final Shape form;
  // The area around an object used for collision handling
  public final Shape hit_box;
  
  public Object(Shape f, Box hb) {
    form = f;
    hit_box = hb;
  }
  
  public void draw() {
    form.draw();
    hit_box.draw();
  }
  
  public boolean collision(PVector pos) {
    return ((Box)hit_box).within(pos);
  }
}
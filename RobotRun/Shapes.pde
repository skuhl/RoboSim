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
  
  /* Define what happens to a shape when it is moved */
  public abstract void move(float x, float y, float z);
  
  /* Define how a shape is drawn in the window */
  public abstract void draw();

}

/**
 * A shape that is defined by a number of vertices
 */
public class Polygon extends Shape {
  private PVector[] vertices;
  
  /* Create a shpae with the given set of vertices and outline/fill colors */
  public Polygon(PVector[] v_set, color f, color o) {
    super(f, o);
    vertices = v_set;
  }
  
  /* Shift all the vertices for the shape by the given offset values */
  public void move(float x, float y, float z) {
    for (PVector v : vertices) {
      v.x += x;
      v.y += y;
      v.z += z;
    }
  }
  
  public void draw() {
    beginShape();
    stroke(outline);
    
    if (no_fill) {
      noFill();
    } else {
      fill(fill);
    }
    
    // draw each vertex
    for (PVector v : vertices) {
      vertex(v.x, v.y, v.z);
    }
    
    endShape();
  }
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
  
  /* Move the box by the given xyz coordinate values */
  public void move(float x, float y, float z) {
    center.x += x;
    center.y += y;
    center.z += z;
  }
  
  public void draw() {
    pushMatrix();
    
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
  // Used when an object is held by the robot
  public boolean disable_gravity;
  
  public Object(Shape f, Box hb) {
    form = f;
    hit_box = hb;
    disable_gravity = false;
  }
  
  public void draw() {
    form.draw();
  }
  
  public boolean collision(PVector pos) {
    return ((Box)hit_box).within(pos);
  }
}
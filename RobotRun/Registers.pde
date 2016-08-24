// Position Registers
private final PositionRegister[] GPOS_REG = new PositionRegister[100];
// Data Registers
private final DataRegister[] DREG = new DataRegister[100];
// IO Registers
private final IORegister[] IO_REG = new IORegister[5];

public abstract class Register {
  protected String comment;
  protected int idx;
  
  public Register() {
    comment = null;
    idx = -1;
  }
  
  public Register(int i) {
    comment = null;
    idx = i;
  }
  
  public String getComment() { return comment; }
  public int getIdx() { return idx; }
  public String setComment(String s) { return comment = s; }
  public int setIdx(int i) { return idx = i; }
}

/* A simple class for a Register of the Robot Arm, which holds a value associated with a comment. */
public class DataRegister extends Register {
  public Float value;
  
  public DataRegister() {
    comment = null;
    value = null;
  }
  
  public DataRegister(String c, Float v) {
    comment = c;
    value = v;
  }
}

/* A simple class for a Position Register of the Robot Arm, which holds a point associated with a comment. */
public class PositionRegister extends Register {
  /**
   * The point associated with this Position Register, which is saved in
   * the current User frame with the active Tool frame TCP offset, though
   * is independent of Frames
   */
  public Point point;
  public boolean isCartesian;
  
  public PositionRegister() {
    comment = null;
    point = null;
    isCartesian = false;
  }
  
  public PositionRegister(String c, Point pt, boolean isCart) {
    comment = c;
    point = pt;
    isCartesian = isCart;
  }
  
  /**
   * Returns the value of the point stored in this register which corresponds
   * to the register mode (joint or cartesian) and the given index 'idx.'
   * Note that 'idx' should be in the range of 0 to 5 inclusive, as this value
   * is meant to represent either 1 of 6 joint angles for a joint type point,
   * or 1 of 6 cartesian points (x, y, z, w, p, r) for a cartesian type point.
   */
  public float getPointValue(int idx) {
    if(!isCartesian) {
      return point.getValue(idx);
    }
    else if(idx < 3) {
      return point.getValue(idx + 6);
    }
    else {
      PVector pOrientation = quatToEuler(point.orientation);
      return pOrientation.array()[idx - 3];
    }
  }
}

/* A simple class designed to hold a state value along with a name. */
public class IORegister extends Register {
  public final String name;
  public int state;
  
  public IORegister() {
    name = "";
    state = OFF;
  }
  
  public IORegister(int iniState) {
    name = "";
    state = iniState;
  }
  
  public IORegister(String comm, int iniState) {
    name = comm;
    state = iniState;
  }
}
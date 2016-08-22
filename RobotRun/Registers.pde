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
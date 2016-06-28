
final int MTYPE_JOINT = 0, MTYPE_LINEAR = 1, MTYPE_CIRCULAR = 2;
final int FTYPE_TOOL = 0, FTYPE_USER = 1;

Frame[] toolFrames = null;
Frame[] userFrames = null;

// Position Registers
private final PositionRegister[] POS_REG = new PositionRegister[100];
// Registers
private final Register[] REG = new Register[100];

public class Point  {
  public PVector pos; // position
  public float[] ori = new float[4]; // orientation
  public float[] joints = new float[6]; // joint values
  
  public Point() {
    pos = new PVector(0,0,0);
    ori[0] = 1;
    ori[1] = 0;
    ori[2] = 0;
    ori[3] = 0; 
    for (int n = 0; n < joints.length; n++) joints[n] = 0;
  }
  
  public Point(float x, float y, float z, float r, float i, float j, float k,
               float j1, float j2, float j3, float j4, float j5, float j6)
  {
    pos = new PVector(x,y,z);
    ori[0] = r;
    ori[1] = i;
    ori[2] = j;
    ori[3] = k;
    joints[0] = j1;
    joints[1] = j2;
    joints[2] = j3;
    joints[3] = j4;
    joints[4] = j5;
    joints[5] = j6;
  }
  
  public Point(float x, float y, float z, float r, float i, float j, float k){
    pos = new PVector(x,y,z);
    ori[0] = r;
    ori[1] = i;
    ori[2] = j;
    ori[3] = k;
  }
  
  public Point(PVector position, float[] orientation){
    pos = position;
    ori = orientation;
  }
  
  ////create a new point with position, orientation, and associated joint angles
  //public Point(float x, float y, float z, float w, float p, float r,
  //             float j1, float j2, float j3, float j4, float j5, float j6)
  //{
  //  pos = new PVector(x,y,z);
  //  ori = eulerToQuat(new PVector(w,p,r));
  //  joints[0] = j1;
  //  joints[1] = j2;
  //  joints[2] = j3;
  //  joints[3] = j4;
  //  joints[4] = j5;
  //  joints[5] = j6;
  //}
  
  ////create a new point with position and orientation only
  //public Point(float x, float y, float z, float w, float p, float r){
  //  pos = new PVector(x,y,z);
  //  ori = eulerToQuat(new PVector(w,p,r));
  //}
  
  //public Point(PVector position, PVector orientation){
  //  pos = position;
  //  ori = eulerToQuat(orientation);
  //}
  
  public Point clone() {
    return new Point(pos.x, pos.y, pos.z, 
                     ori[0], ori[1], ori[2], ori[3], 
                     joints[0], joints[1], joints[2], joints[3], joints[4], joints[5]);
  }
  
  /**
   * Returns a String array, whose entries are the joint values of the
   * Point with their respective labels (J1-J6).
   * 
   * @return  A 6-element String array
   */
  public String[] toJointStringArray() {
    String[] entries = new String[6];
    
    for (int idx = 0; idx < joints.length; ++idx) {
      entries[idx] = String.format("J%d: %4.2f", (idx + 1), joints[idx]);
    }
    
    return entries;
  }
  
  /**
   * Returns a string array, where each entry is one of
   * the values of the Cartiesian represent of the Point:
   * (X, Y, Z, W, P, and R) and their respective labels.
   *
   * @return  A 6-element String array
   */
  public String[] toCartesianStringArray() {
    PVector angles = quatToEuler(ori);
    
    String[] entries = new String[6];
    entries[0] = String.format("X: %4.2f", pos.x);
    entries[1] = String.format("Y: %4.2f", pos.y);
    entries[2] = String.format("Z: %4.2f", pos.z);
    entries[3] = String.format("W: %4.2f", angles.x);
    entries[4] = String.format("P: %4.2f", angles.y);
    entries[5] = String.format("R: %4.2f", angles.z);
    
    return entries;
  }
} // end Point class

public class Frame {
  private PVector origin;
  private PVector wpr;
  // The unit vectors representing the x, y,z axes (in row major order)
  private float[][] axes;
  
  public Frame() {
    origin = new PVector(0,0,0);
    wpr = new PVector(0,0,0);
    axes = new float[3][3];
    // Create identity matrix
    for (int diag = 0; diag < 3; ++diag) {
      axes[diag][diag] = 1f;
    }
  }
  
  /* Used for loading Frames from a file */
  public Frame(PVector origin, PVector wpr, float[][] axesVectors) {
    this.origin = origin;
    this.wpr = wpr;
    this.axes = new float[3][3];
    
    for (int row = 0; row < 3; ++row) {
      for (int col = 0; col < 3; ++col) {
        axes[row][col] = axesVectors[row][col];
      }
     }
  }
  
  public PVector getOrigin() { return origin; }
  public void setOrigin(PVector in) { origin = in; }
  public PVector getWpr() { return wpr; }
  public void setWpr(PVector in) { wpr = in; }
  /* Returns a set of axes unit vectors representing the axes
   * of the frame in reference to the Native Coordinate System. */
  public float[][] getNativeAxes() { return axes.clone(); }
  /* Returns a set of axes unit vectors representing the axes
   * of the frame in reference to the World Coordinate System. */
  public float[][] getWorldAxes() {
    float[][] wAxes = new float[3][3];
    
    for (int col = 0; col < wAxes[0].length; ++col) {
      wAxes[0][col] = -axes[0][col];
      wAxes[1][col] = axes[2][col];
      wAxes[2][col] = -axes[1][col];
    }
    
    /*for (int row = 0; row < wAxes[0].length; ++row) {
      wAxes[row][0] = -axes[row][0];
      wAxes[row][1] = axes[row][2];
      wAxes[row][2] = -axes[row][1];
    }*/
    
    return wAxes;
  }
  
  public void setAxis(int idx, PVector in) {
    
    if (idx >= 0 && idx < axes.length) {
      axes[idx][0] = in.x;
      axes[idx][1] = in.y;
      axes[idx][2] = in.z;
      
      wpr = matrixToEuler(axes);
    }
  }
  
  public void setAxes(float[][] axesVectors) {
    axes = axesVectors.clone();
  }
} // end Frame class

public class Program  {
  private String name;
  private int nextRegister;
  private Point[] p = new Point[1000]; // local registers
  private ArrayList<Instruction> instructions;
  
  public Program(String theName) {
    instructions = new ArrayList<Instruction>();
    for (int n = 0; n < p.length; n++) p[n] = new Point();
    name = theName;
    nextRegister = 0;
  }
  
  public ArrayList<Instruction> getInstructions() {
    return instructions;
  }
  
  public void setName(String n) { name = n; }
  
  public String getName(){
    return name;
  }
  
  public void loadNextRegister(int next){
     nextRegister = next;
  }
  
  public int getRegistersLength(){
     return p.length;
  }
  /**** end ****/
  
  public void addInstruction(Instruction i) {
    instructions.add(i);
    if (i instanceof MotionInstruction ) {
      MotionInstruction castIns = (MotionInstruction)i;
      if (!castIns.getGlobal() && castIns.getRegister() >= nextRegister) {
        nextRegister = castIns.getRegister()+1;
        if (nextRegister >= p.length) nextRegister = p.length-1;
      }
    }
  }
  
  public void overwriteInstruction(int idx, Instruction i) {
    instructions.set(idx, i);
    nextRegister++;
  }
  
  public void addInstruction(int idx, Instruction i) {
    instructions.add(idx, i);
  }
  
  public void addRegister(Point in, int idx) {
    if (idx >= 0 && idx < p.length) p[idx] = in;
  }
  
  public int nextRegister() {
    return nextRegister;
  }
  
  public Point getRegister(int idx) {
    if (idx >= 0 && idx < p.length) return p[idx];
    else return null;
  }
} // end Program class


public int addProgram(Program p) {
  if (p == null) {
    return -1;
  } else {
    int idx = 0;
    
    if (programs.size() < 1) {
       programs.add(p);
     } else {
       while (idx < programs.size() && programs.get(idx).name.compareTo(p.name) < 0) { ++idx; }
       programs.add(idx, p);
     }
    
    return idx;
  }
}

public class Instruction { /* Serves as a blank instruction */ }

public final class MotionInstruction extends Instruction  {
  private int motionType;
  private int register;
  private boolean globalRegister;
  private float speed;
  private float termination;
  private int userFrame, toolFrame;
  
  public MotionInstruction(int m, int r, boolean g, float s, float t,
                           int uf, int tf)
  {
    motionType = m;
    register = r;
    globalRegister = g;
    speed = s;
    termination = t;
    userFrame = uf;
    toolFrame = tf;
  }
  
  public MotionInstruction(int m, int r, boolean g, float s, float t) {
    motionType = m;
    register = r;
    globalRegister = g;
    speed = s;
    termination = t;
    userFrame = -1;
    toolFrame = -1;
  }
  
  public int getMotionType() { return motionType; }
  public void setMotionType(int in) { motionType = in; }
  public int getRegister() { return register; }
  public void setRegister(int in) { register = in; }
  public boolean getGlobal() { return globalRegister; }
  public void setGlobal(boolean in) { globalRegister = in; }
  public float getSpeed() { return speed; }
  public void setSpeed(float in) { speed = in; }
  public float getTermination() { return termination; }
  public void setTermination(float in) { termination = in; }
  public float getUserFrame() { return userFrame; }
  public void setUserFrame(int in) { userFrame = in; }
  public float getToolFrame() { return toolFrame; }
  public void setToolFrame(int in) { toolFrame = in; }
  
  public float getSpeedForExec(ArmModel model) {
    if (motionType == MTYPE_JOINT) return speed;
    else return (speed / model.motorSpeed);
  }
  
  public Point getVector(Program parent) {
    if (motionType != COORD_JOINT) {
      Point out;
      if (globalRegister) out = POS_REG[register].point.clone();
      else out = parent.p[register].clone();
      out.pos = convertWorldToNative(out.pos);
      return out;
    } else {
      Point ret;
      if (globalRegister) ret = POS_REG[register].point.clone();
      else ret = parent.p[register].clone();
      if (userFrame != -1) {
        ret.pos = rotate(ret.pos, userFrames[userFrame].getNativeAxes());
      }
      return ret;
    }
  } // end getVector()
  
  public String toString(){
     String me = "";
     switch (motionType){
        case MTYPE_JOINT:
           me += "J ";
           break;
        case MTYPE_LINEAR:
           me += "L ";
           break;
        case MTYPE_CIRCULAR:
           me += "C ";
           break;
     }
     if (globalRegister) me += "PR[";
     else me += "P[";
     me += Integer.toString(register)+"] ";
     if (motionType == MTYPE_JOINT) me += Float.toString(speed * 100) + "%";
     else me += Integer.toString((int)speed) + "mm/s";
     if (termination == 0) me += "FINE";
     else me += "CONT" + (int)(termination*100);
     return me;
  } // end toString()
  
} // end MotionInstruction class



public class FrameInstruction extends Instruction {
  private int frameType;
  private int idx;
  
  public FrameInstruction(int f, int i) {
    frameType = f;
    idx = i;
  }
  
  public void execute() {
    if (frameType == FTYPE_TOOL) activeToolFrame = idx;
    else if (frameType == FTYPE_USER) activeUserFrame = idx;
  }
  
  public String toString() {
    String ret = "";
    if (frameType == FTYPE_TOOL) ret += "UTOOL_NUM=";
    else if (frameType == FTYPE_USER) ret += "UFRAME_NUM=";
    ret += idx+1;
    return ret;
  }
} // end FrameInstruction class



public class ToolInstruction extends Instruction {
  private String type;
  private int bracket;
  private int setToolStatus;
  
  public ToolInstruction(String d, int b, int t) {
    type = d;
    bracket = b;
    setToolStatus = t;
  }
  
  public void execute() {
    if ((type.equals("RO") && bracket == 4 && armModel.activeEndEffector == ENDEF_CLAW) ||
        (type.equals("DO") && bracket == 101 && armModel.activeEndEffector == ENDEF_SUCTION))
    {
      
      armModel.endEffectorStatus = setToolStatus;
      
      // Check if the Robot is placing an object or picking up and object
      if (armModel.activeEndEffector == ENDEF_CLAW || armModel.activeEndEffector == ENDEF_SUCTION) {
        
        if (setToolStatus == ON && armModel.held == null) {
          
          PVector ee_pos = armModel.getEEPos();
          
          // Determine if an object in the world can be picked up by the Robot
          for (Object s : objects) {
            
            if (s.collision(ee_pos)) {
              armModel.held = s;
              break;
            }
          }
        } else if (setToolStatus == OFF && armModel.held != null) {
          // Release the object
          armModel.releaseHeldObject();
        }
      }
    }
  }
  
  public String toString() {
    return type + "[" + bracket + "]=" + (setToolStatus == ON ? "ON" : "OFF");
  }
} // end ToolInstruction class

public class CoordinateFrame {
  private PVector origin = new PVector();
  private PVector rotation = new PVector();
  
  public PVector getOrigin() { return origin; }
  public void setOrigin(PVector in) { origin = in; }
  public PVector getRotation() { return rotation; }
  public void setRotation(PVector in) { rotation = in; }
} // end FrameInstruction class

public class RecordScreen implements Runnable{
   public RecordScreen(){
     System.out.format("Record screen...\n");
   }
    public void run(){
       try{ 
            // create a timestamp and attach it to the filename
            Calendar calendar = Calendar.getInstance();
            java.util.Date now = calendar.getTime();
            java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());
            String filename = "output_" + currentTimestamp.toString() + ".flv"; 
            filename = filename.replace(' ', '_');
            filename = filename.replace(':', '_');   

            // record screen
            System.out.format("run script to record screen...\n");
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec("ffmpeg -f dshow -i " + 
                           "video=\"screen-capture-recorder\":audio=\"Microphone" + 
                           " (Conexant SmartAudio HD)\" " + filename );
            //Process proc = rt.exec(script);
            while(record == ON){
              Thread.sleep(4000);
            }
            rt.exec("taskkill /F /IM ffmpeg.exe"); // close ffmpeg
            System.out.format("finish recording\n");
            
        }catch (Throwable t){
            t.printStackTrace();
        }
        
    }
}

/* A simple class for a Register of the Robot Arm, which holds a value associated with a comment. */
public class Register {
  public String comment = null;
  public Float value = null;
  
  public Register(String c, Float v) {
    value = v;
    comment = c;
  }
}

/* A simple class for a Position Register of the Robot Arm, which holds a point associated with a comment. */
public class PositionRegister {
  public String comment = null;
  public Point point = null;
  
  public PositionRegister() {
    point = new Point(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f);
  }
  
  public PositionRegister(String c, Point p) {
    point = p;
    comment = c;
  }
}
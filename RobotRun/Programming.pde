
final int MTYPE_JOINT = 0, MTYPE_LINEAR = 1, MTYPE_CIRCULAR = 2;
final int FTYPE_TOOL = 0, FTYPE_USER = 1;
Point[] pr = new Point[1000]; // global registers
Frame[] toolFrames = new Frame[10]; // tool frames
Frame[] userFrames = new Frame[10];


public class Point  {
  public PVector c; // coordinates
  public PVector a; // angles
  public float[] j = new float[6]; // joint values
  
  public Point() {
    c = new PVector(0,0,0);
    a = new PVector(0,0,0);
    for (int n = 0; n < j.length; n++) j[n] = 0;
  }
  
  //create a new point with position, orientation, and associated joint angles
  public Point(float x, float y, float z, float w, float p, float r,
               float j1, float j2, float j3, float j4, float j5, float j6)
  {
    c = new PVector(x,y,z);
    a = new PVector(w,p,r);
    j[0] = j1;
    j[1] = j2;
    j[2] = j3;
    j[3] = j4;
    j[4] = j5;
    j[5] = j6;
  }
  
  //create a new point with position and orientation only
  public Point(float x, float y, float z, float w, float p, float r){
    c = new PVector(x,y,z);
    a = new PVector(w,p,r);
  }
  
  public Point(PVector position, PVector orientation){
    c = position;
    a = orientation;
  }
  
  public Point clone() {
    return new Point(c.x, c.y, c.z, a.x, a.y, a.z, j[0], j[1], j[2], j[3], j[4], j[5]);
  }
  
  public String toExport(){
     String ret = "<Point> ";
     ret += Float.toString(c.x);
     ret += " ";
     ret += Float.toString(c.y);
     ret += " ";
     ret += Float.toString(c.z);
     ret += " ";
     ret += Float.toString(a.x);
     ret += " ";
     ret += Float.toString(a.y);
     ret += " ";
     ret += Float.toString(a.z);
     ret += " ";
     for (int i=0;i<j.length-1;i++){
        ret += Float.toString(j[i]);
        ret += " ";
     }
     ret += Float.toString(j[j.length-1]);
     ret += " ";
     ret += "</Point>";  
     return ret;
  }
} // end Point class

public class Frame {
  private PVector origin;
  private PVector wpr;
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
    
    for (int col = 0; col < 3; ++col) {
      for (int row = 0; row < 3; ++row) {
        axes[row][col] = axesVectors[row][col];
      }
     }
  }
  
  public PVector getOrigin() { return origin; }
  public void setOrigin(PVector in) { origin = in; }
  public PVector getWpr() { return wpr; }
  public void setWpr(PVector in) { wpr = in; }
  
  public float[][] getAxes() { return axes.clone(); }
  
  public void setAxis(int idx, PVector in) {
    
    if (idx >= 0 && idx < axes.length) {
      axes[0][idx] = in.x;
      axes[1][idx] = in.y;
      axes[2][idx] = in.z;
      
      wpr = matrixToEuler(axes);
    }
  }
  
  public void setAxes(float[][] axesVectors) {
    axes = axesVectors.clone();
  }
  
  /* Used for saving the Frame to a file */
  public String toExport() {
    String str = "<Frame>";
    str += " ";
    
    str += Float.toString(origin.x) + " " + Float.toString(origin.y) + " " + Float.toString(origin.z) + " ";
    str += Float.toString(wpr.x) + " " + Float.toString(wpr.y) + " "  + Float.toString(wpr.z) + " ";
    
    for (int col = 0; col < 3; ++col) {
        str += Float.toString(axes[0][col]) + " " + Float.toString(axes[1][col]) + " "  + Float.toString(axes[2][col]) + " ";
    }
    
    str += "</Frame>";
    return str;
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
  
  // added by Judy
  public String toExport(){
     String ret = "<Program> ";
     ret += name.replace(' ', '_');
     ret += " ";
     ret += Integer.toString(nextRegister);
     ret += " ";
     ret += "\n";
     for(int i = 0; i < p.length; i += 1){
       ret += "  "; 
       ret += p[i].toExport();
       ret += "\n"; 
     }
     for(int i = 0; i < instructions.size(); i += 1){
        Instruction ins = instructions.get(i);
        ret += "  ";
        if (ins instanceof MotionInstruction){
           MotionInstruction tmp = (MotionInstruction) ins;
           ret += tmp.toExport();
        }else if (ins instanceof FrameInstruction){
           FrameInstruction tmp = (FrameInstruction) ins;
           ret += tmp.toExport();
        }else if (ins instanceof ToolInstruction){
           ToolInstruction tmp = (ToolInstruction) ins;
           ret += tmp.toExport();
        }
        ret += "\n";
     }
     ret += "</Program> ";
     return ret;
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

public  class Instruction  {}

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
      if (globalRegister) out = pr[register].clone();
      else out = parent.p[register].clone();
      out.c = convertWorldToNative(out.c);
      return out;
    } else {
      Point ret;
      if (globalRegister) ret = pr[register].clone();
      else ret = parent.p[register].clone();
      if (userFrame != -1) {
        ret.c = rotate(ret.c, userFrames[userFrame].getAxes());
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
  
  public String toExport(){
     String ret = "<MotionInstruction> ";
     ret += Integer.toString(motionType);
     ret += " ";
     ret += Integer.toString(register);
     ret += " ";
     ret += Boolean.toString(globalRegister);
     ret += " ";
     ret += Float.toString(speed);
     ret += " ";
     ret += Float.toString(termination);
     ret += " ";
     ret += Integer.toString(userFrame);
     ret += " ";
     ret += Integer.toString(toolFrame);
     ret += " ";
     ret += "</MotionInstruction>";
     return ret;
  }
  
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
  
  public String toExport(){
     String ret = "<FrameInstruction> ";
     ret += Integer.toString(frameType);
     ret += " ";
     ret += Integer.toString(idx);
     ret += " ";
     ret += "</FrameInstruction>";
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
        
        if (setToolStatus == ON & armModel.held == null) {
          
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
  
  public String toExport(){
     String ret = "<ToolInstruction> ";
     ret += type;
     ret += " ";
     ret += Integer.toString(bracket);
     ret += " ";
     ret += Integer.toString(setToolStatus);
     ret += " ";
     ret += "</ToolInstruction>";
     return ret;
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
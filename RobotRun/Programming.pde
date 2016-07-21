final int MTYPE_JOINT = 0, MTYPE_LINEAR = 1, MTYPE_CIRCULAR = 2;
final int FTYPE_TOOL = 0, FTYPE_USER = 1;

public class Point  {
  // X, Y, Z
  public PVector position;
  // Q1 - Q4
  public float[] orientation;
  // J1 - J6
  public float[] angles;

  public Point() {
    angles = new float[] { 0f, 0f, 0f, 0f, 0f, 0f };
    position = armModel.getEEPos(angles.clone());
    orientation = armModel.getQuaternion(angles.clone());
  }
  
  public Point(float[] jointAngles) {
    angles = Arrays.copyOfRange(jointAngles, 0, 6);
    position = armModel.getEEPos(angles.clone());
    orientation = armModel.getQuaternion(angles.clone());
  }
  
  public Point(PVector pos, float[] orient) {
    position = new PVector(pos.x, pos.y, pos.z);
    orientation = Arrays.copyOfRange(orient, 0, 4);
    angles = calculateIKJacobian(position, orientation);
  }
  
  public Point(float x, float y, float z, float r, float i, float j, float k,
  float j1, float j2, float j3, float j4, float j5, float j6) {
    orientation = new float[4];
    angles = new float[6];
    position = new PVector(x,y,z);
    orientation[0] = r;
    orientation[1] = i;
    orientation[2] = j;
    orientation[3] = k;
    angles[0] = j1;
    angles[1] = j2;
    angles[2] = j3;
    angles[3] = j4;
    angles[4] = j5;
    angles[5] = j6;
  }
  
  public Point(PVector pos, float[] orient, float[] jointAngles) {
    position = new PVector(pos.x, pos.y, pos.z);
    orientation = Arrays.copyOfRange(orient, 0, 4);
    angles = Arrays.copyOfRange(jointAngles, 0, 6);
  }

  public Point clone() {
    return new Point(position.x, position.y, position.z, 
    orientation[0], orientation[1], orientation[2], orientation[3], 
    angles[0], angles[1], angles[2], angles[3], angles[4], angles[5]);
  }
  
  public Float getValue(int idx) {
      
    switch(idx) {
      // Joint angles
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:   return angles[idx];
      // Position
      case 6:   return position.x;
      case 7:   return position.y;
      case 8:   return position.z;
      // Orientation
      case 9:   
      case 10:  
      case 11:  
      case 12:  return orientation[idx - 9];
      default:
    }
    
    return null;
  }

  /**
   * Returns a String array, whose entries are the joint values of the
   * Point with their respective labels (J1-J6).
   * 
   * @return  A 6x2-element String array
   */
  public String[][] toJointStringArray() {
    String[][] entries = new String[6][2];
    
    for(int idx = 0; idx < angles.length; ++idx) {
      entries[idx][0] = String.format("J%d: ", (idx + 1));
      entries[idx][1] = String.format("%4.3f", angles[idx] * RAD_TO_DEG);
    }
    
    return entries;
  }

  /**
   * Returns a string array, where each entry is one of
   * the values of the Cartiesian represent of the Point:
   * (X, Y, Z, W, P, and R) and their respective labels.
   *
   * @return  A 6x2-element String array
   */
  public String[][] toCartesianStringArray() {
    String[][] entries = new String[6][2];
    // Show the vector in terms of the World Frame or the active User Frame
    PVector pos = convertNativeToWorld(this.position);
    // Convert W, P, R to User Frame
    PVector angles = quatToEuler(orientation);
    
    entries[0][0] = "X: ";
    entries[0][1] = String.format("%4.3f", pos.x);
    entries[1][0] = "Y: ";
    entries[1][1] = String.format("%4.3f", pos.y);
    entries[2][0] = "Z: ";
    entries[2][1] = String.format("%4.3f", pos.z);
    // Show angles in degrees
    entries[3][0] = "W: ";
    entries[3][1] = String.format("%4.3f", angles.x * RAD_TO_DEG);
    entries[4][0] = "P: ";
    entries[4][1] = String.format("%4.3f", angles.y * RAD_TO_DEG);
    entries[5][0] = "R: ";
    entries[5][1] = String.format("%4.3f", angles.z * RAD_TO_DEG);
    
    return entries;
  }
} // end Point class

public class Program  {
  private String name;
  private int nextRegister;
  private Point[] LPosReg = new Point[1000]; // program positions
  private ArrayList<Instruction> instructions;

  public Program(String theName) {
    instructions = new ArrayList<Instruction>();
    for(int n = 0; n < LPosReg.length; n++) LPosReg[n] = new Point();
    name = theName;
    nextRegister = 0;
  }

  public ArrayList<Instruction> getInstructions() {
    return instructions;
  }

  public void setName(String n) { name = n; }

  public String getName() {
    return name;
  }

  public void loadNextRegister(int next) {
    nextRegister = next;
  }

  public int getRegistersLength() {
    return LPosReg.length;
  }

  public void addInstruction(Instruction i) {
    //i.setProg(this);
    instructions.add(i);
    if(i instanceof MotionInstruction ) {
      MotionInstruction castIns = (MotionInstruction)i;
      if(!castIns.getGlobal() && castIns.getPosition() >= nextRegister) {
        nextRegister = castIns.getPosition()+1;
        if(nextRegister >= LPosReg.length) nextRegister = LPosReg.length-1;
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

  public void addPosition(Point in, int idx) {
    if(idx >= 0 && idx < LPosReg.length) LPosReg[idx] = in;
  }

  public int nextPosition() {
    return nextRegister;
  }

  public Point getPosition(int idx) {
    if(idx >= 0 && idx < LPosReg.length) return LPosReg[idx];
    else return null;
  }
  
  public void setPosition(int idx, Point pt){
    LPosReg[idx] = pt;
  }
  
  public void clearPositions(){
    LPosReg = new Point[1000];
  }
  
  public ArrayList<LabelInstruction> getLabels(){
    ArrayList<LabelInstruction> labels = new ArrayList<LabelInstruction>();
    for(Instruction i: instructions){
      if(i instanceof LabelInstruction)
        labels.add((LabelInstruction)i);
    }
    
    return labels;
  }
} // end Program class


public int addProgram(Program p) {
  if(p == null) {
    return -1;
  } 
  else {
    int idx = 0;
    
    if(programs.size() < 1) {
      programs.add(p);
    } 
    else {
      while(idx < programs.size() && programs.get(idx).name.compareTo(p.name) < 0) { ++idx; }
      programs.add(idx, p);
    }
    
    return idx;
  }
}

public class Instruction {
  Program p;
  boolean com;
  
  public Instruction() {
    p = null;
    com = false;
  }
  
  public Program getProg() { return p; }
  public void setProg(Program p) { this.p = p; }
  public boolean isCommented(){ return com; }
  public void toggleCommented(){ com = !com; }
  

  public String toString() {
    String str = "\0";
    return str;
  }
}

public final class MotionInstruction extends Instruction  {
  private int motionType;
  private int positionNum;
  private boolean globalRegister;
  private float speed;
  private float termination;
  private int userFrame, toolFrame;

  public MotionInstruction(int m, int p, boolean g, 
                           float s, float t, int uf, int tf) {
    super();
    motionType = m;
    positionNum = p;
    globalRegister = g;
    speed = s;
    termination = t;
    userFrame = uf;
    toolFrame = tf;
  }

  public MotionInstruction(int m, int p, boolean g, float s, float t) {
    super();
    motionType = m;
    positionNum = p;
    globalRegister = g;
    speed = s;
    termination = t;
    userFrame = -1;
    toolFrame = -1;
  }

  public int getMotionType() { return motionType; }
  public void setMotionType(int in) { motionType = in; }
  public int getPosition() { return positionNum; }
  public void setPosition(int in) { positionNum = in; }
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
    if(motionType == MTYPE_JOINT) return speed;
    else return (speed / model.motorSpeed);
  }

  public Point getVector(Program parent) {
    if(motionType != MTYPE_JOINT) {
      Point out;
      if(globalRegister) out = GPOS_REG[positionNum].point;
      else out = parent.LPosReg[positionNum].clone();
      //out.pos = convertWorldToNative(out.pos);
      return out;
    } 
    else {
      Point ret;
      
      if(globalRegister) ret = GPOS_REG[positionNum].point;
      else ret = parent.LPosReg[positionNum].clone();
      
      if(userFrame != -1) {
        ret.position = rotate(ret.position, userFrames[userFrame].getWorldAxes());
      }
      
      return ret;
    }
  } // end getVector()

  public String toString() {
    String me = "";
    switch (motionType) {
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
    if(globalRegister) me += "PR[ ";
    else me += "P[ ";
    me += Integer.toString(positionNum + 1)+"] ";
    if(motionType == MTYPE_JOINT) me += Float.toString(speed * 100) + "% ";
    else me += Integer.toString((int)speed) + "mm/s ";
    if(termination == 0) me += "FINE";
    else me += "CONT" + (int)(termination*100);
    return me;
  } // end toString()

} // end MotionInstruction class

public class FrameInstruction extends Instruction {
  private int frameType;
  private int idx;

  public FrameInstruction(int f, int i) {
    super();
    frameType = f;
    idx = i;
  }

  public void execute() {
    if(frameType == FTYPE_TOOL) activeToolFrame = idx;
    else if(frameType == FTYPE_USER) activeUserFrame = idx;
    // Update the Robot Arm's current frame rotation matrix
    updateCoordFrame(armModel);
  }

  public String toString() {
    String ret = "";
    if(frameType == FTYPE_TOOL) ret += "UTOOL_NUM=";
    else if(frameType == FTYPE_USER) ret += "UFRAME_NUM=";
    ret += idx+1;
    return ret;
  }
} // end FrameInstruction class

public class ToolInstruction extends Instruction {
  private int reg;
  private EEStatus status;

  public ToolInstruction(int r, EEStatus t) {
    super();
    reg = r;
    status = t;
  }

  public void execute() {
    armModel.endEffectorStatus = status;
    System.out.printf("EE: %s\n", armModel.endEffectorStatus);
      
    // Check if the Robot is placing an object or picking up and object
    if(armModel.activeEndEffector == EndEffector.CLAW || armModel.activeEndEffector == EndEffector.SUCTION) {
      
      if(status == EEStatus.ON && armModel.held == null) {
        
        PVector ee_pos = armModel.getEEPos();
        
        // Determine if an object in the world can be picked up by the Robot
        for(WorldObject s : objects) {
          
          if(s.collision(ee_pos)) {
            armModel.held = s;
            break;
          }
        }
      } 
      else if(status == EEStatus.OFF && armModel.held != null) {
        // Release the object
        armModel.releaseHeldObject();
      }
    }
  }

  public String toString() {
    return "IO[" + reg + "]=" + status.toString();
  }
} // end ToolInstruction class

public class LabelInstruction extends Instruction {
  int labelNum;
  int labelIdx;
  
  public LabelInstruction(int n, int i){
    super();
    labelNum = n;
    labelIdx = i;
  }
  
  public void execute(){
    if(active_instr < p.getInstructions().size()-1){
      active_instr += 1;
    }
  }
  
  public String toString(){
    return "";
  }
}

public class JumpInstruction extends Instruction {
  int tgtLabel;
  int tgtIdx;
  
  public JumpInstruction(int n){
    tgtLabel = n;
    for(LabelInstruction i: p.getLabels()){
      if(i.labelNum == tgtLabel)
        tgtIdx = i.labelIdx;
    }
  }
  
  public void execute(){
    active_instr = tgtIdx;
  }
  
  public String toString(){
    return "";
  }
}

public class RegisterStatement extends Instruction {
  /**
   * A singleton or doubleton array, which determines, whether the
   * result of the statement will be stored in a Register or a
   * Position Register.
   */
  private final int[] regIndices;
  /**
   * The expression associated with this statement.
   */
  private ExpressionSet statement;
  
  /**
   * Creates a register statement, whose result is associated with
   * a Position Register entry.
   * 
   * @param regIdx  the index in the Position Register list where
   *                the result of the expression will be stored
   * @param ptIdx   the index of the a value in the Point to store
   *                the result in the case that the result is a
   *                single Float value. This field should be -1 in
   *                the case that the whole Point should be saved.
   */
  public RegisterStatement(int regIdx, int ptIdx) {
    super();
    regIndices = new int[] { regIdx, ptIdx };
    statement = new ExpressionSet();
  }
  
  /**
   * Creates a register statement, whose result is associated with
   * a Register entry.
   * 
   * @param regIdx  the index in the Register list where the result
   *                of the expression will be stored
   */
  public RegisterStatement(int regIdx) {
    super();
    regIndices = new int[] { regIdx };
    statement = new ExpressionSet();
  }
  
  
  public void execute() {
    // TODO
  }
  
  /**
   * Convert the entire statement to a set of Strings, where each
   * operator and operand is a separate Stirng Object.
   */
  public ArrayList<String> toStringArrayList() {
    ArrayList<String> expression = statement.toStringArrayList();
    expression.add(0, statement.paramToString(regIndices) + " =");
    
    return expression;
  }
}

public class CoordinateFrame {
  private PVector origin = new PVector();
  private PVector rotation = new PVector();

  public PVector getOrigin() { return origin; }
  public void setOrigin(PVector in) { origin = in; }
  public PVector getRotation() { return rotation; }
  public void setRotation(PVector in) { rotation = in; }
} // end FrameInstruction class

public class RecordScreen implements Runnable{
  public RecordScreen() {
    System.out.format("Record screen...\n");
  }
  public void run() {
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
      while(record == ON) {
        Thread.sleep(4000);
      }
      rt.exec("taskkill /F /IM ffmpeg.exe"); // close ffmpeg
      System.out.format("finish recording\n");
      
    }catch (Throwable t) {
      t.printStackTrace();
    }
    
  }
}
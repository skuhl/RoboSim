private final int MTYPE_JOINT = 0, MTYPE_LINEAR = 1, MTYPE_CIRCULAR = 2;
private final int FTYPE_TOOL = 0, FTYPE_USER = 1;
//stack containing the previously running program state when a new program is called
private Stack<int[]> call_stack = new Stack<int[]>();
// Indicates whether a program is currently running
private boolean programRunning = false;

public class Point  {
  // X, Y, Z
  public PVector position;
  // Q1 - Q4
  public float[] orientation;
  // J1 - J6
  public float[] angles;

  public Point() {
    angles = new float[] { 0f, 0f, 0f, 0f, 0f, 0f };
    position = new PVector(0f, 0f, 0f);
    orientation = new float[] { 1f, 0f, 0f, 0f };
  }
  
  public Point(PVector pos, float[] orient) {
    angles = new float[] { 0f, 0f, 0f, 0f, 0f, 0f };
    position = pos.copy();
    orientation = Arrays.copyOfRange(orient, 0, 4);
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
    position = pos.copy();
    orientation = Arrays.copyOfRange(orient, 0, 4);
    angles = Arrays.copyOfRange(jointAngles, 0, 6);
  }

  public Point clone() { return new Point(position, orientation, angles); }
  
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
   * Computes and returns the result of the addition of this point with
   * another point, 'p.' Does not alter the original values of this point.
   */
  public Point add(Point p) {
    Point p3 = new Point();
    
    PVector p3Pos = PVector.add(position, p.position);
    float[] p3Orient = quaternionMult(orientation, p.orientation);
    float[] p3Joints = new float[6];
    
    for(int i = 0; i < 6; i += 1) {
      p3Joints[i] = angles[i] + p.angles[i];
    }
    
    p3.position = p3Pos;
    p3.orientation = p3Orient;
    p3.angles = p3Joints;
    
    return p3;
  }
  
  /**
   * Negates the current values of the point.
   */
  public Point negate() {
    position = position.mult(-1);
    orientation = vectorScalarMult(orientation, -1);
    angles = vectorScalarMult(angles, -1);
    return this;
  }
    
  /**
   * Converts the original toStringArray into a 2x1 String array, where the origin
   * values are in the first element and the W, P, R values are in the second
   * element (or in the case of a joint angles, J1-J3 on the first and J4-J6 on
   * the second), where each element has space buffers.
   * 
   * @param displayCartesian  whether to display the joint angles or the cartesian
   *                          values associated with the point
   * @returning               A 2-element String array
   */
  public String[] toLineStringArray(boolean displayCartesian) {
    String[][] entries;
    
    if (displayCartesian) {
      entries = toCartesianStringArray();
    } else {
      entries = toJointStringArray();
    }
    
    
    String[] line = new String[2];
    // X, Y, Z with space buffers
    line[0] = String.format("%-12s %-12s %s", entries[0][0].concat(entries[0][1]),
            entries[1][0].concat(entries[1][1]), entries[2][0].concat(entries[2][1]));
    // W, P, R with space buffers
    line[1] = String.format("%-12s %-12s %s", entries[3][0].concat(entries[3][1]),
            entries[4][0].concat(entries[4][1]), entries[5][0].concat(entries[5][1]));
    
    return line;
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
      
      if (angles == null) {
        entries[idx][1] = Float.toString(Float.NaN);
      } else {
        entries[idx][1] = String.format("%4.3f", angles[idx] * RAD_TO_DEG);
      }
    }
    
    return entries;
  }

  /**
   * Returns a string array, where each entry is one of the values of the Cartiesian
   * represent of the Point: (X, Y, Z, W, P, and R) and their respective labels.
   * 
   * @return  A 6x2-element String array
   */
  public String[][] toCartesianStringArray() {
    String[][] entries = new String[6][2];
    
    PVector pos;
    if (position == null) {
      // Uninitialized
      pos = new PVector(Float.NaN, Float.NaN, Float.NaN);
    } else {
      // Display in terms of the World Frame
      pos = convertNativeToWorld(position);
    }
    
    // Convert Quaternion to Euler Angles
    PVector angles;
    if (orientation == null) {
      // Uninitialized
      angles = new PVector(Float.NaN, Float.NaN, Float.NaN);
    } else {
       // Display in terms of the World Frame
      angles = convertNativeToWorld( quatToEuler(orientation) ).mult(RAD_TO_DEG);
    }
    
    entries[0][0] = "X: ";
    entries[0][1] = String.format("%4.3f", pos.x);
    entries[1][0] = "Y: ";
    entries[1][1] = String.format("%4.3f", pos.y);
    entries[2][0] = "Z: ";
    entries[2][1] = String.format("%4.3f", pos.z);
    entries[3][0] = "W: ";
    entries[3][1] = String.format("%4.3f", angles.x);
    entries[4][0] = "P: ";
    entries[4][1] = String.format("%4.3f", angles.y);
    entries[5][0] = "R: ";
    entries[5][1] = String.format("%4.3f", angles.z );
    
    return entries;
  }
} // end Point class

public class Program {
  private String name;
  private int nextRegister;
  private int nextInstr;
  private int progIdx;
  /**
   * The positions associated with this program, which are
   * stored in reference to the current User frame
   */
  private Point[] LPosReg = new Point[1000];
  private ArrayList<Instruction> instructions;

  public Program(String s) {
    name = s;
    nextRegister = 0;
    nextInstr = 0;
    for(int n = 0; n < LPosReg.length; n++) LPosReg[n] = new Point();
    instructions = new ArrayList<Instruction>();
  }

  public ArrayList<Instruction> getInstructions() {
    return instructions;
  }
  
  public void setName(String n) { name = n; }

  public String getName() {
    return name;
  }
  
  public int size() {
    return instructions.size();
  }

  public int getRegistersLength() {
    return LPosReg.length;
  }

  public Instruction getInstruction(int i){
    return instructions.get(i);
  }

  public void addInstruction(Instruction i) {
    //i.setProg(this);
    instructions.add(i);
    
    if(i instanceof MotionInstruction ) {
      MotionInstruction castIns = (MotionInstruction)i;
      if(!castIns.usesGPosReg() && castIns.getPosition() >= nextRegister) {
        nextRegister = castIns.getPosition()+1;
        if(nextRegister >= LPosReg.length) nextRegister = LPosReg.length-1;
      }
    }
  }

  public void overwriteInstruction(int idx, Instruction i) {
    instructions.set(idx, i);
    if(i instanceof MotionInstruction ) { 
      MotionInstruction castIns = (MotionInstruction)i;
      if(!castIns.usesGPosReg() && castIns.getPosition() >= nextRegister) {
        nextRegister = castIns.getPosition()+1;
        if(nextRegister >= LPosReg.length) nextRegister = LPosReg.length-1;
      }
    }
  }

  public void addPosition(Point in, int idx) {
    if(idx >= 0 && idx < LPosReg.length) LPosReg[idx] = in;
  }
  
  public int getNextPosition() { return nextRegister; }
  public void setNextRegister(int next) { nextRegister = next; }

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
  
  public LabelInstruction getLabel(int n){    
    for(Instruction i: instructions){
      if(i instanceof LabelInstruction){
        if(((LabelInstruction)i).labelNum == n){
          return (LabelInstruction)i;
        }
      }
    }
    
    return null;
  }
  
  /**
   * Determines if a label with the given number exists in the program and returns its
   * instruction index if it does.
   * 
   * @param lblNum  The target label index
   * @returning     The instruction index of the target label, or -1 if it exists
   */
  public int findLabelIdx(int lblNum) {
    
    for (int idx = 0; idx < instructions.size(); ++idx) {
      Instruction inst = instructions.get(idx);
      // Check the current instruction
      if (inst instanceof LabelInstruction && ((LabelInstruction)inst).labelNum == lblNum) {
        // Return the label's instruction index
        return idx;
      }
    }
    // A label with the given number does not exist
    return -1;
  }
  
  /**
   * Return an independent replica of this program object.
   */
  public Program clone() {
    Program copy = new Program(name);
    // Copy instructions
    for (Instruction inst : instructions) {
      copy.addInstruction(inst.clone());
    }
    // Copy positions
    for (int idx = 0; idx < LPosReg.length; ++idx) {
      copy.addPosition(LPosReg[idx].clone(), idx);
    }
    // Copy next register
    copy.setNextRegister(nextRegister);
    
    return copy;
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

/**
 * Returns the currently active program or null if no program is active
 */
public Program activeProgram() {
  if (active_prog < 0 || active_prog >= programs.size()) {
    //System.out.printf("Not a valid program index: %d!\n", active_prog);
    return null;
  }
  
  return programs.get(active_prog);
}

/**
 * Returns the instruction that is currently active in the currently active program.
 * 
 * @returning  The active instruction of the active program or null if no instruction
 *             is active
 */
public Instruction activeInstruction() {
  Program activeProg = activeProgram();
  
  if (activeProg == null || active_instr < 0 || active_instr >= activeProg.getInstructions().size()) {
    //System.out.printf("Not a valid instruction index: %d!\n", active_instr);
    return null;
  }
  
  return activeProg.getInstruction(active_instr);
}

/**
 * Returns the active instructiob of the active program, if
 * that instruction is a motion instruction.
 */
public MotionInstruction activeMotionInst() {
  Instruction inst = activeInstruction();
  
  if(inst instanceof MotionInstruction) {
    return (MotionInstruction)inst;
  }
  
  return null;
}

public class Instruction {
  protected boolean com = false;
  
  public boolean isCommented(){ return com; }
  public void setIsCommented(boolean comFlag) { com = comFlag; }
  public void toggleCommented() { com = !com; }
    
  public int execute() { return 0; }
    
  public String toString() {
    String[] fields = toStringArray();
    String str = new String();
    
    /* Return a stirng which is the concatenation of all the elements in
     * this instruction's toStringArray() method, separated by spaces */
    for (int fdx = 0; fdx < fields.length; ++fdx) {
      str += fields[fdx];
      
      if (fdx < (fields.length - 1)) {
        str += " ";
      }
    }
    
    return str;
  }
  
  public String[] toStringArray() {
    return new String[] {"..."};
  }
  
  /**
   * Create an independent replica of this instruction.
   */
  public Instruction clone() {
    Instruction copy = new Instruction();
    copy.setIsCommented( isCommented() );
    
    return copy;
  }
}

public final class MotionInstruction extends Instruction  {
  private int motionType;
  private int positionNum;
  private boolean isGPosReg;
  private float speed;
  private int termination;
  private int userFrame, toolFrame;

  public MotionInstruction(int m, int p, boolean g, 
                           float s, int t, int uf, int tf) {
    super();
    motionType = m;
    positionNum = p;
    isGPosReg = g;
    speed = s;
    termination = t;
    userFrame = uf;
    toolFrame = tf;
  }

  public MotionInstruction(int m, int p, boolean g, float s, int t) {
    super();
    motionType = m;
    positionNum = p;
    isGPosReg = g;
    speed = s;
    termination = t;
    userFrame = -1;
    toolFrame = -1;
  }

  public int getMotionType() { return motionType; }
  public void setMotionType(int in) { motionType = in; }
  public int getPosition() { return positionNum; }
  public void setPosition(int in) { positionNum = in; }
  public boolean usesGPosReg() { return isGPosReg; }
  public void setGlobalPosRegUse(boolean in) { isGPosReg = in; }
  public float getSpeed() { return speed; }
  public void setSpeed(float in) { speed = in; }
  public int getTermination() { return termination; }
  public void setTermination(int in) { termination = in; }
  public int getUserFrame() { return userFrame; }
  public void setUserFrame(int in) { userFrame = in; }
  public int getToolFrame() { return toolFrame; }
  public void setToolFrame(int in) { toolFrame = in; }

  public float getSpeedForExec(ArmModel model) {
    if(motionType == MTYPE_JOINT) return speed;
    else return (speed / model.motorSpeed);
  }
  
  /**
   * Verify that the given frame indices match those of the
   * instructions frame indices.
   */
  public boolean checkFrames(int activeToolIdx, int activeFrameIdx) {
    return (toolFrame == activeToolIdx) && (userFrame == activeFrameIdx);
  }
  
  /**
   * Returns the point associated with this motion instruction
   * (can be either a position in the program or a global position
   * register value) in Native Coordinates.
   * 
   * @param parent  The program, to which this instruction belongs
   * @returning     The point associated with this instruction
   */
  public Point getVector(Program parent) {
    Point pt;
    
    if (isGPosReg) {
        pt = GPOS_REG[positionNum].point.clone();
        
      } else {
        pt = parent.LPosReg[positionNum].clone();
      }
    
    
    if(motionType == MTYPE_JOINT) {
      return pt;
      
    }  else {
      // Remove the Point's User frame
      if (userFrame != -1) {
        Frame active = userFrames[userFrame];
        // Convert point into the Native Coordinate System
        Point convertedPt = removeFrame(pt, active.getOrigin(), active.getAxes());
        return convertedPt;
      }
      
      return pt;
    }
  } // end getVector()
  
  public Instruction clone() {
    Instruction copy = new MotionInstruction(motionType, positionNum, isGPosReg, speed, termination, userFrame, toolFrame);
    copy.setIsCommented( isCommented() );
    
    return copy;
  }
  
  public String[] toStringArray() {
    String[] fields = new String[5];
    
    // Motion type
    switch(motionType) {
      case MTYPE_JOINT:
        fields[0] = "J";
        break;
      case MTYPE_LINEAR:
        fields[0] = "L";
        break;
      case MTYPE_CIRCULAR:
        fields[0] = "C";
        break;
      default:
        fields[0] = "\0";
    }
    
    // Regster type
    if (isGPosReg) {
      fields[1] = "PR[";
    } else {
      fields[1] = "P[";
    }
    
    // Register index
    fields[2] = String.format("%d]", positionNum + 1);
    
    // Speed
    if (motionType == MTYPE_JOINT) {
      fields[3] = String.format("%d%%", Math.round(speed * 100));
    } else {
      fields[3] = String.format("%dmm/s", (int)(speed));
    }
    
    // Termination percent
    if (termination == 0) {
      fields[4] = "FINE";
    } else {
      fields[4] = String.format("CONT%d", termination);
    }
    
    return fields;
  }
} // end MotionInstruction class

public class FrameInstruction extends Instruction {
  private int frameType;
  private int frameIdx;
  
  public FrameInstruction(int f) {
    super();
    frameType = f;
    frameIdx = -1;
  }
  
  public FrameInstruction(int f, int r) {
    super();
    frameType = f;
    frameIdx = r;
  }
  
  public int getFrameType(){ return frameType; }
  public void setFrameType(int t){ frameType = t; }
  public int getReg(){ return frameIdx; }
  public void setReg(int r){ frameIdx = r; }
  
  public int execute() {
    int ret = 2;
    if (frameType == FTYPE_TOOL) {
      activeToolFrame = frameIdx;
      ret = 0;
    } else if (frameType == FTYPE_USER) {
      activeUserFrame = frameIdx;
      ret = 1;
    }
    // Update the current active frames
    updateCoordFrame();
    
    return ret;
  }
  
  public Instruction clone() {
    Instruction copy = new FrameInstruction(frameType, frameIdx);
    copy.setIsCommented( isCommented() );
    
    return copy;
  }
  
  public String[] toStringArray() {
    String[] fields = new String[2];
    // Frame type
    if (frameType == FTYPE_TOOL) {
      fields[0] = "TFRAME_NUM =";
    } else if (frameType == FTYPE_USER) {
      fields[0] = "UFRAME_NUM =";
    } else {
      fields[0] = "?FRAME_NUM =";
    }
    // Frame index
    fields[1] = Integer.toString(frameIdx + 1);
    
    return fields;
  }
  
} // end FrameInstruction class

public class IOInstruction extends Instruction {
  private int state;
  private int reg;
  
  public IOInstruction(){
    super();
    state = OFF;
    reg = -1;
  }
  
  public IOInstruction(int r, int t) {
    super();
    state = t;
    reg = r;
  }

  public int getState(){ return state; }
  public void setState(int s){ state = s; }
  public int getReg(){ return reg; }
  public void setReg(int r){ reg = r; }
  
  public int execute() {
    armModel.endEffectorState = state;
    return armModel.checkPickupCollision();
  }
  
  public Instruction clone() {
    Instruction copy = new IOInstruction(state, reg);
    copy.setIsCommented( isCommented() );
    
    return copy;
  }
  
  public String[] toStringArray() {
    String[] fields = new String[2];
    // Register index
    if (reg == -1) {
      fields[0] = "IO[...] =";
    } else {
      fields[0] = String.format("IO[%d] =", reg + 1);
    }
    // Register value
    if (state == ON) {
      fields[1] = "ON";
    } else {
      fields[1] = "OFF";
    }
    
    return fields;
  }
} // end ToolInstruction class

public class LabelInstruction extends Instruction {
  int labelNum;
  
  public LabelInstruction(int num) {
    labelNum = num;
  }
  
  public String[] toStringArray() {
    String[] fields = new String[1];
    // Label number
    if (labelNum == -1) {
      fields[0] = "LBL[...]";
    } else {
      fields[0] = String.format("LBL[%d]", labelNum);
    }
    
    return fields;
  }
  
  public Instruction clone() {
    Instruction copy = new LabelInstruction(labelNum);
    copy.setIsCommented( isCommented() );
    
    return copy;
  }
}

public class JumpInstruction extends Instruction {
  public int tgtLblNum;
  
  public JumpInstruction() {
    tgtLblNum = -1;
  }
  
  public JumpInstruction(int l) {
    tgtLblNum = l;
  }
  
  /**
   * Returns the index of the instruction to which to jump.
   */
  public int execute() {
    Program p = activeProgram();
    
    if (p != null) {
      int lblIdx = p.findLabelIdx(tgtLblNum);
      
      if (lblIdx != -1) {
        // Return destination instrution index
        return lblIdx;
      } else {
        println("Invalid jump instruction!");
        return 1;
      }
    } else {
      println("No active program!");
      return 2;
    }
  }
  
  public Instruction clone() {
    Instruction copy = new JumpInstruction(tgtLblNum);
    copy.setIsCommented( isCommented() );
    
    return copy;
  }
  
  public String[] toStringArray() {
    String[] ret;
    // Target label number
    if (tgtLblNum == -1) {
      ret = new String[] {"JMP", "LBL[...]"};
    } else {
      ret = new String[] {"JMP", "LBL[" + tgtLblNum + "]"};
    }
    
    return ret;
  }
}

public class CallInstruction extends Instruction {
  Program callProg;
  int progIdx;
  
  public CallInstruction() {
    callProg = null;
    progIdx = -1;
  }
  
  public CallInstruction(Program p, int i) {
    callProg = p;
    progIdx = i;
  }
  
  public int execute() {
    int[] p = new int[2];
    p[0] = active_prog;
    p[1] = active_instr + 1;
    call_stack.push(p);
    
    active_prog = progIdx;
    active_instr = 0;
    row_select = 0;
    col_select = 0;
    start_render = 0;
    updateScreen();
    programRunning = !executeProgram(callProg, armModel, false);
    
    return 1;
  }
  
  public String toString() {
    String progName = (callProg == null) ? "..." : callProg.name;
    return "Call " + progName;
  }
  
  public String[] toStringArray() {
    String[] ret = new String[2];
    ret[0] = "Call";
    ret[1] = (callProg == null) ? "..." : callProg.name;
    return ret;
  }
}

/**
 * An if statement consists of an expression and an instruction. If the expression evaluates
 * to true, the execution of this if statement will result in the execution of the associated
 * instruction.
 *
 * Legal operators for the if statement expression are "=, <>, >, <, >=, and <=," which 
 * correspond to the equal, not equal, greater-than, less-than, greater-than or equal to,
 * and less-than or equal to operations, respectively.
 *
 * @param o - the operator to use for this if statement's expression.
 * @param i - the instruction to be executed if the statement expression evaluates to true.
 */
public class IfStatement extends Instruction {
  AtomicExpression expr;
  Instruction instr;
  
  public IfStatement() {
    expr = new Expression();
    instr = null;
  }
  
  public IfStatement(Operator o, Instruction i){
    expr = new BooleanExpression(o);
    instr = i;
  }
  
  public int execute() {
    if(expr.evaluate().boolVal){
      instr.execute();
    }
    
    return 0;
  }
  
  public String toString() {
    return "IF " + expr.toString() + " : " + instr.toString();
  }
  
  public String[] toStringArray() {
    String[] exprArray = expr.toStringArray();
    String[] instArray, ret;
    if(instr == null) {
      ret = new String[exprArray.length + 2];
      ret[ret.length - 1] = "...";
    } else {
      ret = new String[exprArray.length + 3];
      instArray = instr.toStringArray();
      ret[ret.length - 2] = instArray[0];
      ret[ret.length - 1] = instArray[1];
    }
        
    ret[0] = "IF";
    for(int i = 1; i < exprArray.length + 1; i += 1) {
      ret[i] = exprArray[i - 1];
    }
    ret[exprArray.length] += " :";

    return ret;
  }
  
  public Instruction clone() {
    if (instr == this) {
      // Cannot copy!
      return null;
    }
    
    Instruction copy = new IfStatement(null, instr.clone());
    copy.setIsCommented( isCommented() );
    // TODO actually copy the if statement
    return copy;
  }
}

public class SelectStatement extends Instruction {
  ExprOperand arg;
  ArrayList<ExprOperand> cases;
  ArrayList<Instruction> instr;
    
  public SelectStatement() {
    arg = new ExprOperand();
    cases = new ArrayList<ExprOperand>();
    instr = new ArrayList<Instruction>();
    addCase();
  }
  
  public SelectStatement(ExprOperand a) {
    arg = a;
    cases = new ArrayList<ExprOperand>();
    instr = new ArrayList<Instruction>();
    addCase();
  }
  
  public int execute() {
    for(int i = 0; i < cases.size(); i += 1) {
      ExprOperand c = cases.get(i);
      if(c == null) return 1;
      
      println("testing case " + i + " = " + cases.get(i).getDataVal() + " against " + arg.getDataVal());
      
      if(c.type != ExpressionElement.UNINIT && arg.getDataVal() == c.dataVal) {
        Instruction ins = instr.get(i);
        
        if(ins instanceof JumpInstruction || ins instanceof CallInstruction) {
          println("executing " + instr.get(i).toString());
          ins.execute();
        }
        break;
      }
    }
    
    return 0;
  }
  
  public void addCase() {
    cases.add(new ExprOperand());
    instr.add(new Instruction());
  }
  
  public void addCase(ExprOperand e, Instruction i) {
    cases.add(e);
    instr.add(i);
  }
  
  public void deleteCase(int idx) {
    if(cases.size() > 1) {
      cases.remove(idx);
    }
    
    if(instr.size() > 1) {
      instr.remove(idx);
    }
  }
  
  public String toString() {
    return "";
  }
  
  public Instruction clone() {   
    Instruction copy = new SelectStatement();
    copy.setIsCommented( isCommented() );
    // TODO actually copy the select statement
    return copy;
  }
  
  public String[] toStringArray() {
    String[] ret = new String[2 + 4*cases.size()];
    ret[0] = "SELECT";
    ret[1] = arg.toString();
    
    for(int i = 0; i < cases.size(); i += 1) {
      String[] iString = instr.get(i).toStringArray();
      
      ret[i*4 + 2] = "= " + cases.get(i).toString();
      ret[i*4 + 3] = iString[0];
      ret[i*4 + 4] = iString.length == 1 ? "..." : iString[1];
      ret[i*4 + 5] = "\n";
    }
    
    return ret;
  }
}

public class RegisterStatement extends Instruction {
  Register reg;
  int regLength;
  Expression expr;
  
  /**
   * Creates a register statement with a given register and a blank Expression.
   *
   * @param reg - The destination register for this register expression. The
   *              result of the expression 'expr' will be assigned to 'reg'
   *              upon successful execution of this statement.
   * @param expr - The expression to be evaluated in conjunction with the execution
   *               of this statement. The value of this expression, if valid for the
                   register 
   */
  public RegisterStatement(Register r) {
    reg = r;
    expr = new Expression();
  }
  
  public RegisterStatement(Register r, Expression e) {
    reg = r;
    expr = e;
  }
  
  public Register setRegister(Register r) {
    if(!(r instanceof DataRegister) && !(r instanceof PositionRegister)) {
      return null;
    } else {
      return reg = r;
    }
  }
  
  public int execute() {
    ExprOperand result = expr.evaluate();
    
    if(reg instanceof DataRegister) {
      ((DataRegister)reg).value = result.dataVal;
      println(result.dataVal + ", " + ((DataRegister)reg).value);
    } else if(reg instanceof PositionRegister) {
      ((PositionRegister)reg).point = result.pointVal;
    }
        
    return 1;
  }
  
  public Instruction clone() {
    Instruction copy = new RegisterStatement(reg, (Expression)expr.clone());    
    return copy;
  }
  
  /**
   * Convert the entire statement to a set of Strings, where each
   * operator and operand is a separate String Object.
   */
  public String[] toStringArray() {
    String[] ret = new String[2 + expr.getLength()];
    String[] exprString = expr.toStringArray();
    
    ret[0] = (reg instanceof DataRegister) ? "R[" : "PR[";
    ret[1] = (reg.getIdx() == -1) ? "...] =" : (reg.getIdx() + 1) + "] =";
    
    for(int i = 0; i < exprString.length; i += 1) {
      ret[i + 2] = exprString[i];
    }
    
    return ret;
  }
}

public class RecordScreen implements Runnable {
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
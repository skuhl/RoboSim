
final int MTYPE_JOINT = 0, MTYPE_LINEAR = 1, MTYPE_CIRCULAR = 2;
final int FTYPE_TOOL = 0, FTYPE_USER = 1;

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
    for(int n = 0; n < joints.length; n++) joints[n] = 0;
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

  public Point(float x, float y, float z, float r, float i, float j, float k) {
    pos = new PVector(x,y,z);
    ori[0] = r;
    ori[1] = i;
    ori[2] = j;
    ori[3] = k;
  }

  public Point(PVector position, float[] orientation) {
    pos = position;
    ori = orientation;
  }

  public Point clone() {
    return new Point(pos.x, pos.y, pos.z, 
    ori[0], ori[1], ori[2], ori[3], 
    joints[0], joints[1], joints[2], joints[3], joints[4], joints[5]);
  }
  
  
  public Float getValue(int idx) {
    
    if (idx >= 0) {
      
      if (idx == 1) {
        return pos.x;  
      } else if (idx == 2) {
        return pos.y;
      } else if (idx == 3) {
        return pos.z;
      }
    }
    
    return null;
  }

  /**
    * Returns a String array, whose entries are the joint values of the
    * Point with their respective labels (J1-J6).
    * 
    * @return  A 6-element String array
    */
  public String[] toJointStringArray() {
    String[] entries = new String[6];
    
    for(int idx = 0; idx < joints.length; ++idx) {
      entries[idx] = String.format("J%d: %4.3f", (idx + 1), joints[idx] * RAD_TO_DEG);
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
    // Show the vector in terms of the World Frame
    PVector wPos = convertNativeToWorld(pos);
    
    entries[0] = String.format("X: %4.3f", wPos.x);
    entries[1] = String.format("Y: %4.3f", wPos.y);
    entries[2] = String.format("Z: %4.3f", wPos.z);
    // Show angles in degrees
    entries[3] = String.format("W: %4.3f", angles.x * RAD_TO_DEG);
    entries[4] = String.format("P: %4.3f", angles.y * RAD_TO_DEG);
    entries[5] = String.format("R: %4.3f", angles.z * RAD_TO_DEG);
    
    return entries;
  }
} // end Point class

public class Program  {
  private String name;
  private int nextRegister;
  private Point[] p = new Point[1000]; // program positions
  private ArrayList<Instruction> instructions;

  public Program(String theName) {
    instructions = new ArrayList<Instruction>();
    for(int n = 0; n < p.length; n++) p[n] = new Point();
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
    return p.length;
  }

  public void addInstruction(Instruction i) {
    i.setProg(this);
    instructions.add(i);
    if(i instanceof MotionInstruction ) {
      MotionInstruction castIns = (MotionInstruction)i;
      if(!castIns.getGlobal() && castIns.getPosition() >= nextRegister) {
        nextRegister = castIns.getPosition()+1;
        if(nextRegister >= p.length) nextRegister = p.length-1;
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
    if(idx >= 0 && idx < p.length) p[idx] = in;
  }

  public int nextPosition() {
    return nextRegister;
  }

  public Point getPosition(int idx) {
    if(idx >= 0 && idx < p.length) return p[idx];
    else return null;
  }
  
  public void setPosition(int idx, Point pt){
    p[idx] = pt;
  }
  
  public void clearPositions(){
    p = new Point[1000];
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
      if(globalRegister) out = POS_REG[positionNum].point.clone();
      else out = parent.p[positionNum].clone();
      //out.pos = convertWorldToNative(out.pos);
      return out;
    } 
    else {
      Point ret;
      
      if(globalRegister) ret = POS_REG[positionNum].point.clone();
      else ret = parent.p[positionNum].clone();
      
      if(userFrame != -1) {
        ret.pos = rotate(ret.pos, userFrames[userFrame].getNativeAxes());
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
  private String type;
  private int bracket;
  private EEStatus setToolStatus;

  public ToolInstruction(String d, int b, EEStatus t) {
    super();
    type = d;
    bracket = b;
    setToolStatus = t;
  }

  public void execute() {
    if((type.equals("RO") && bracket == 4 && armModel.activeEndEffector == EndEffector.CLAW) ||
        (type.equals("DO") && bracket == 101 && armModel.activeEndEffector == EndEffector.SUCTION))
    {
      
      armModel.endEffectorStatus = setToolStatus;
      System.out.printf("EE: %s\n", armModel.endEffectorStatus);
      
      // Check if the Robot is placing an object or picking up and object
      if(armModel.activeEndEffector == EndEffector.CLAW || armModel.activeEndEffector == EndEffector.SUCTION) {
        
        if(setToolStatus == EEStatus.ON && armModel.held == null) {
          
          PVector ee_pos = armModel.getEEPos();
          
          // Determine if an object in the world can be picked up by the Robot
          for(WorldObject s : objects) {
            
            if(s.collision(ee_pos)) {
              armModel.held = s;
              break;
            }
          }
        } 
        else if(setToolStatus == EEStatus.OFF && armModel.held != null) {
          // Release the object
          armModel.releaseHeldObject();
        }
      }
    }
  }

  public String toString() {
    return type + "[" + bracket + "]=" + setToolStatus.toString();
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

/* These are used to store the operators used in register statement expressions in the ExpressionSet Object */
public enum Operator { PLUS, MINUS, MUTLIPLY, DIVIDE, REMAINDER, INTDIVIDE, PAR_OPEN, PAR_CLOSE }

/**
 * This class is designed to save an arithmetic expression for a register statement instruction. Register statements include the
 * folllowing operands: floating-point constants, Register values, Position Register points, and Position Register values. Legal
 * operators for these statements include addition(+), subtraction(-), multiplication(*), division(/), remainder(%), integer
 * division(|), and parenthesis(). An expression is evaluated left to right, ignoring any operation prescendence, save for
 * parenthesis, whose contents are evaluated first. An expression will have a maximum of 5 operators (discluding closing parenthesis).
 * 
 * Singleton arrays indecate the result will be stored in a Register. Doubleton arrays indicate the result will be stored in a
 * Position Register. For either case, the first value in the array is the index of the destination register. For the doubleton
 * arrays, if the seond value is -1, then the result of the expression is expected to be a Point Object that will override the
 * Position Register entry. However, if the second value is between 0 and 11 inclusive, then the result is expected to be a
 * Floating-point value, which will be saved in a specific entry of the the Position Register.
 * 
 * Floating-point values     ->  Constants
 * Singleton integer arrays  ->  Register values
 * Doubleton integer arrays  ->  Position Register points/values
 *
 * For the second entry of the dobleton array:
 *   0 - 5  ->  J1 - J6
 *   6 - 11 ->  X, Y, Z, W, P, R
 * 
 */
public class ExpressionSet {
  /* The individual elements of the expressions */
  private final ArrayList<Object> parameters;
  
  /**
   * Creates a new expression with a single null value.
   */
  public ExpressionSet() {
    parameters = new ArrayList<Object>();
    // Expression begins as R[i]/PR[i]/PR[x, y] = _
    parameters.add(null);
  }
  
  /**
   * Addes the operator at the index in the list of parameters and
   * then adds the operand after the operator.
   * 
   * @param idx       Where to add the operator and operand
   * @param op        The operator to add to the expression
   * @ param operand  The operand to add to the expression
   */
  public void addParameter(int idx, Operator op, Object operand) {
    parameters.add(idx, op);
    parameters.add(idx + 1, operand);
  }
  
  /**
   * Adds a set of parenthesis to the expression at the given index
   * with an empty operand inside the parenthesis
   * 
   * @param idx  Where to add the set of parethesis to in the expression
   */
  public void addParenthesis(int idx) {
    parameters.add(idx, Operator.PAR_OPEN);
    parameters.add(idx + 1, null);
    parameters.add(idx + 2, Operator.PAR_CLOSE);
  }
  
  /**
   * Replaces the given index in the list of parameters
   * with the given parameter.
   */
  public void setParameter(int idx, Object param) {
    parameters.set(idx, param);
  }
  
  /**
   * This method will calculate the result of the current expression. The
   * expression is evaluated from left to right, ignoring normal order of
   * operations, however, parenthesis do act as normal. Each element is parsed
   * individually, keeping track of a current resulting value for every single
   * operation.
   * If an open parenthesis is found, then the current working
   * result is saved on the stack and the value is reset.
   * If an operator is found, then the next value in the list is taken and the
   * operator's operation is preformed on the current working result and the
   * next value.
   * If a closed parenthesis is found, then the current top of the stack value
   * is taken off of the stack, the next operator is taken from the list of
   * parameters, and its operation is preformed on the popped value and the
   * working result.
   * 
   * @return
   *
   * Once the entire expression is processed and no errors are found, the result
   * of the expression is returned as either a Floating-point value or a PointCoord
   * Object, depending on the nature of the expression.
   *
   * @throw ExpressionEvaluationException
   * 
   * Since, the expressions are only evaluated when a program is executed, it
   * is possible that the expression may contain errors such as mssing arguments,
   * invalid operation arguments, and so on. So, these errors are caught by this
   * method and a new ExpressionEvaluationException is thrown with an error message
   * indicating what the error was. 
   */
  public Object evaluate() throws ExpressionEvaluationException {
    
    try {
      int pdx = 1,
          len = parameters.size();
      Stack<Object> savedVals = new Stack<Object>();
      Object result = parameters.get(0);
      
      while (true) {
        Operator op = (Operator)( parameters.get(pdx++) );
        
        if (op == Operator.PAR_OPEN) {
          
          // Entering a parenthesis
          savedVals.push(result);
          result = parameters.get(pdx++);
        } else {
          Object operand;
          
          if (op == Operator.PAR_CLOSE && pdx < len) {
            
            // Exiting parenthesis
            operand = result;
            result = savedVals.pop();
            op = (Operator)( parameters.get(pdx++) );
          } else {
            // Normal operator
            Object param = parameters.get(pdx++);
            
            if (param instanceof Float) {
              
              // Constant value
              operand = param;
            } else if (param instanceof int[]) {
              int[] regIdx = (int[])param;
              
              if (regIdx.length == 1) {
                
                // Use Register value
                operand = REG[ regIdx[0] ].value;
              } else if (regIdx.length == 2) {
                PointCoord pt = new PointCoord(POS_REG[ regIdx[0] ].point);
                
                if (regIdx[1] == -1) {
                  
                  // Use Position Register point
                  operand = pt;
                } else if (regIdx[1] > 0 && regIdx[1] < 12) {
                  
                  // use a specific value from the Point
                  operand = pt.values[ regIdx[1] ];
                } else {
                  // Illegal parameter
                  throw new ExpressionEvaluationException(1);
                }
              } else {
                // Illegal parameter
                throw new ExpressionEvaluationException(1);
              }
              
            } else {
              // Illegal parameter
              throw new ExpressionEvaluationException(1);
            }
          }
          
          result = operation(result, operand, op);
        }
        
        // Reached the end of the expression (successfully)
        if (pdx >= len) { break; }
      }
      
      return result;
    } catch (NullPointerException NPEx) {
      // Missing a parameter
      throw new ExpressionEvaluationException(0, NPEx.getClass());
      
    } catch (IndexOutOfBoundsException IOOBEx) {
      // Missing a parameter
      throw new ExpressionEvaluationException(0, IOOBEx.getClass());
      
    } catch (ClassCastException CCEx) {
      // Illegal parameters or operations
      throw new ExpressionEvaluationException(1, CCEx.getClass());
      
    } catch (ArithmeticException AEx) {
      // Illegal parameters or operations
      throw new ExpressionEvaluationException(1, AEx.getClass());
      
    } catch (EmptyStackException ESEx) {
      // Invalid parenthesis
      throw new ExpressionEvaluationException(2, ESEx.getClass());
      
    }
  }
  
  /**
   * Evaluate the given parameters with the given operation. The onll valid parameters are floating-point values
   * and int arrays. The integer arrays should singeltons (for Registers) or doubletons (for Position Registers
   * and Position Register Values).
   * 
   * TODO define valid operations
   * 
   * @param param1  The first parameter of the opertion
   * @param param2  The second parameter for the operation
   * @param op      The operation to preform on the parameters
   * @return        The result of the operation on param1 and param2
   * 
   * @throws ExpressionEvaluationException  if the given combination of parameters with the given
   *                                        operation is illegal
   */
  private Object operation(Object param1, Object  param2, Operator op) throws ExpressionEvaluationException {
    
    // Call the correct method for the types of the given parameters
    if (param1 instanceof Float && param2 instanceof Float) {
      
      return evaluatePair((Float)param1, (Float)param2, op);
      
    } else if (param1 instanceof Float && param2 instanceof Point) {
      
      return evaluatePair((Float)param1, (PointCoord)param2, op);
      
    } else if (param1 instanceof Point && param2 instanceof Float) {
      
      throw new ExpressionEvaluationException(1);
      //return evaluatePair((Point)param1, (Float)param2, op);
      
    } else if (param1 instanceof Point && param2 instanceof Point) {
      
      return evaluatePair((PointCoord)param1, (PointCoord)param2, op);
      
    } else {
      // Invalid parameter types
      throw new ExpressionEvaluationException(1);
    }
  }
  
  private Object evaluatePair(Float value1, Float value2, Operator op) throws ExpressionEvaluationException {
    
    // Float-Float operations
    switch(op) {
      case PLUS:       return value1 + value2;
      case MINUS:      return value1 - value2;
      case MUTLIPLY:   return value1 * value2;
      case DIVIDE:     return value1 / value2;
      // Returns the remainder
      case REMAINDER:  return (value1 % value2) / value2;
      // Returns the quotient
      case INTDIVIDE:  return (float)( (int)(value1 / value2) );
      default:         throw new ExpressionEvaluationException(1);
    }
  }
  
  private Object evaluatePair(Float value, PointCoord point, Operator op) throws ExpressionEvaluationException {
    
    // TODO Float-Point operations
    switch(op) {
      case PLUS:
      case MINUS:
      case MUTLIPLY:
        for (int idx = 0; idx < point.values.length; ++idx) {
          point.values[idx] *= value;
        }
        
        return point;
      
      case DIVIDE:
      case REMAINDER:
      case INTDIVIDE:
      default:
    }
    
    throw new ExpressionEvaluationException(1);
  }
  
  private Object evaluatePair(PointCoord point1, PointCoord point2, Operator op) throws ExpressionEvaluationException {
      
    // TODO Point-Point operations
    switch(op) {
      case PLUS:
      case MINUS:
      case MUTLIPLY:
      case DIVIDE:
      case REMAINDER:
      case INTDIVIDE:
      default:
    }
    
    throw new ExpressionEvaluationException(1);
  }
  
  /**
   * Returns the number of operators AND operands in the expression
   */
  public int parameterSize() { return parameters.size(); }
  
  /**
   * Create an ArrayList of Strings, where the frist entry is the
   * Resulting register and all the following entries are the results
   * of converting the elements of parameters to String Objects.
   */
  public ArrayList<String> toStringArrayList() {
    ArrayList<String> fields = new ArrayList<String>();
    
    // Each parameter gets its own column
    for (Object param : parameters) {
      fields.add(paramToString(param));
    }
    
    return fields;
  }
  
  /**
   * Returns unique outputs for the four types of entries of Register
   * Statements: Contants, Registers, Position Register points, and
   * Position Register Values.
   */
  public String paramToString(Object param) {
    
    if (param == null) {
      // An empty parameter is displayed as an underscore
      return "_";
    } if (param instanceof int[]) {
      int[] regIdx = (int[])param;
      
      if (regIdx.length == 1) {
        
        // Register entries are stored as a singleton integer array
        return String.format("R[%d]", regIdx[0]);
      } else if (regIdx.length == 2) {
        
        // Position Register entries are stored as a doubleton integer array
        if (regIdx[1] >= 0) {
          int idx = regIdx[1] % 6;
          
          return String.format("PR[%d, %d]", regIdx[0], idx);
        } else {
          return String.format("PR[%d]", regIdx[0]);
        }
      }
    } else if (param instanceof Operator) {
      
      // Each operator has its own symbol
      switch ( (Operator)param ) {
        case PLUS:      return "+";
        case MINUS:     return "-";
        case MUTLIPLY:  return "*";
        case DIVIDE:    return "/";
        case REMAINDER:   return "%";
        case INTDIVIDE: return "|";
        case PAR_OPEN:  return "(";
        case PAR_CLOSE: return ")";
      }
    }
    
    // Simply print the value for constants
    return param.toString();
  }
}

/**
 * This class defines a Point, which stores a position and orientation
 * in space (X, Y, Z, W, P, R) along with the joint angles (J1 - J6)
 * necessary for the Robot to reach the position and orientation of
 * the Point.
 * This class is designed to temporary store the values of a Point object
 * in order to bypass multiple conversion between Euler angles and
 * Quaternions during the evaluation of Register Statement Expressions.
 */
public class PointCoord {
  /**
   * The values associated with this Point:
   *   0 - 5  ->  J1 - J6
   *   6 - 11 ->  X, Y, Z, W, P, R
   */
  public float[] values;
  
  /**
   * Converts the given Point Object to a PointCoord object
   */
  public PointCoord(Point pt) {
    values = new float[12];
    
    for (int jdx = 0; jdx < pt.joints.length; ++jdx) {
      values[jdx] = pt.joints[jdx];
    }
    
    values[6] = pt.pos.x;
    values[7] = pt.pos.y;
    values[8] = pt.pos.z;
    
    PVector wpr = quatToEuler(pt.ori);
    
    values[9] = wpr.x;
    values[10] = wpr.y;
    values[11] = wpr.z;
    
    
  }
}

/**
 * This class defines an error that occurs during the evaluation process of an ExpressionSet Object.
 */
public class ExpressionEvaluationException extends RuntimeException {
  
  public ExpressionEvaluationException(int flag, Class exception) {
    super( String.format("Error: %d (%s)", flag, exception.toString()) );
  }
  
  /**
   * TODO constructor comment
   */
  public ExpressionEvaluationException(int flag) {
    // TODO develop message for expression parsing error
    super( String.format("Error: %d", flag) );
  }
}
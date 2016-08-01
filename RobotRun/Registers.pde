// Position Registers
private final PositionRegister[] GPOS_REG = new PositionRegister[100];
// Data Registers
private final DataRegister[] DAT_REG = new DataRegister[100];
// IO Registers
private final IORegister[] IO_REG = new IORegister[6];


/* A simple class for a Register of the Robot Arm, which holds a value associated with a comment. */
public class DataRegister {
  public String comment;
  public Float value;
  
  public DataRegister() {
    comment = null;
    value = null;
  }
  
  public DataRegister(String c, Float v) {
    value = v;
    comment = c;
  }
}

/* A simple class for a Position Register of the Robot Arm, which holds a point associated with a comment. */
public class PositionRegister {
  public String comment;
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

/**
 * A class which is a special type of parameter allowed in register expressions, which specifics
 * that the current position of the Robot will be used as the point value of this parameter.
 */
public class RobotPoint {
  /**
   * The value of valIdx corresponds to:
   *   -1     ->  The point itself
   *   0 - 5  ->  J1 - J6
   *   6 - 11 ->  X, Y, Z, W, P, R
   */
  public final int valIdx;

  /**
   * Default to the entire point
   */
  public RobotPoint() {
    valIdx = -1;
  }
  
  /**
   * Specific the index of the value of the point
   */
  public RobotPoint(int vdx) {
    valIdx = vdx;
  }
  
  /**
   * Return the current position of the Robot or a specific value of the current position of
   * the Robot
   */
  public Object getValue() {
    Point RP = nativeRobotEEPoint(armModel.getJointAngles());
    RegStmtPoint pt = new RegStmtPoint( RP.position, RP.orientation );
    
    if (valIdx == -1) {
      // Return the entire point
      return pt;
    } else {
      // Return a specific value of the point
      return pt.values[ valIdx ];
    }
  }
  
  public String toString() {
    
    if (valIdx == -1) {
      return String.format("RP");
    } else {
      // Only show index if the whole point is not used
      return String.format("RP[%d]", valIdx);
    }
  }
}

/**
 * This class defines a Point, which stores a position and orientation
 * in space (X, Y, Z, W, P, R) or the joint angles (J1 - J6) necessary
 * for the Robot to reach the position and orientation of the register point.
 * 
 * This class is designed to temporary store the values of a Point object
 * in order to bypass multiple conversion between Euler angles and
 * Quaternions during the evaluation of Register Statement Expressions.
 */
public class RegStmtPoint {
  
  /**
   * The values associated with a register point:
   * 
   * For a Cartesian point:
   *   0, 1, 2 -> X, Y, Z
   *   3. 4, 5 -> W, P, R
   * 
   * For a Joint point:
   *   0 - 5 -> J1 - J6
   */
  public final float[] values;
  /**
   * Whether this point is a Cartesian or a Joint point
   */
  public final boolean isCartesian;
  
  /**
   * Creates a Joint point with all values equal to zero
   */
  public RegStmtPoint() {
    values = new float[] { 0f, 0f, 0f, 0f, 0f, 0f };
    isCartesian = false;
  }
  
  /**
   * Creates a Joint point
   */
  public RegStmtPoint(float[] jointAngles) {
    values = new float[6];
    isCartesian = false;
    
    for (int idx = 0; idx < 6; ++idx) {
      values[idx] = jointAngles[idx];
    }
  }
  
  /**
   * Creates a Cartesian point
   */
  public RegStmtPoint(PVector pos, float[] ori) {
    values = new float[6];
    isCartesian = true;
    
    // Get W, P, R values
    PVector wpr = quatToEuler(ori);
    
    values[0] = pos.x;
    values[1] = pos.y;
    values[2] = pos.z;
    values[3] = wpr.x;
    values[4] = wpr.y;
    values[5] = wpr.z;
  }
}

public class IORegister {
  public String comment;
  public int state;
  
  public IORegister(){
    comment = null;
    state = OFF;
  }
  
  public IORegister(int init, String com){
    comment = com;
    state = init;
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

public class AtomicExpression extends ExprOperand {
  private ExprOperand arg1;
  private ExprOperand arg2;
  private Operator op;
    
  public AtomicExpression(){
    super();
    op = Operator.UNINIT;
    len = 1;
  }
  
  public AtomicExpression(Operator o){
    super();
    ExprOperand arg1 = new ExprOperand();
    ExprOperand arg2 = new ExprOperand();
    op = o;
    len = 3;
  }
  
  public ExprOperand getArg1() { return arg1; }
  public void setArg1(ExprOperand a) { 
    arg1 = a;
    calculateLength();
  }
  
  public ExprOperand getArg2() { return arg1; }
  public void setArg2(ExprOperand a) { 
    arg1 = a;
    len = calculateLength();
  }
  
  public Operator getOp() { return op; }
  public void setOp(Operator o) {
    op = o;
    len = calculateLength();
  }
  
  private int calculateLength() {
    if(op == Operator.UNINIT) {
      return 1;    
    }
    
    int ret = 1;
    ret += arg1.len;
    ret += arg2.len;
    ret += (arg1.type == -1) ? 2 : 0;
    ret += (arg2.type == -1) ? 2 : 0;
    return ret;
  }
  
  public ExprOperand evaluate() {
    ExprOperand result;
    float o1, o2;
    if(arg1.type == -1) {
      o1 = ((AtomicExpression)arg1).evaluate().dataVal;
    } else {
      o1 = arg1.dataVal;
    }
    
    if(arg2.type == -1) {
      o2 = ((AtomicExpression)arg2).evaluate().dataVal;
    } else {
      o2 = arg2.dataVal;
    }
    
    //integer operands for integer operations
    int intop1 = Math.round(arg1.dataVal);
    int intop2 = Math.round(arg2.dataVal);
    
    switch(op) {
      case ADDTN:
        result = new ExprOperand(o1 + o2);
        break;
      case SUBTR:
        result = new ExprOperand(o1 - o2);
        break;
      case MULT:
        result = new ExprOperand(o1 * o2);
        break;
      case DIV:
        result = new ExprOperand(o1 / o2);
        break;
      case MOD:
        result = new ExprOperand(o1 % o2);
        break;
      case INTDIV:
        result = new ExprOperand(intop1 / intop2);
        break;
      case EQUAL:
        result = new ExprOperand(o1 == o2);
        break;
      case NEQUAL:
        result = new ExprOperand(o1 != o2);
        break;
      case GRTR:
        result = new ExprOperand(o1 > o2);
        break;
      case LESS:
        result = new ExprOperand(o1 < o2);
        break;
      case GREQ:
        result = new ExprOperand(o1 >= o2);
        break;
      case LSEQ:
        result = new ExprOperand(o1 <= o2);
        break;
      default:
        result = null;
        break;
    }
    
    return result;
  }
  
  public String toString(){
    String s = "";
    
    if(op == Operator.UNINIT){
      return "...";
    }
    
    s += arg1.toString();
    s += " " + op.symbol + " ";
    s += arg2.toString();
    
    return s;
  }
  
  public String[] toStringArray() {
    String[] s1, s2, ret;
    String opString = "";
    
    if(op == Operator.UNINIT) {
      return new String[]{"..."};
    }
    
    s1 = arg1.toStringArray();
    opString += " " + op.symbol + " ";
    s2 = arg2.toStringArray();
    
    int lm1 = (arg1 != null && arg1.type == -1) ? 2 : 0;
    int lm2 = (arg2 != null && arg2.type == -1) ? 2 : 0;
    ret = new String[s1.length + s2.length + 1 + lm1 + lm2];
    if(lm1 != 0) {
      ret[0] = "(";
      ret[s1.length + 1] = ")";
    }
    
    if(lm2 != 0) {
      ret[s1.length + 2] = "(";
      ret[ret.length - 1] = ")"; 
    }
    
    for(int i = 0; i < s1.length; i += 1){
      ret[i + lm1/2] = s1[i];
    }
  
    ret[s1.length + lm1] = opString;
    
    for(int i = 0; i < s2.length; i += 1){
      ret[i+s1.length+lm1+1+lm2/2] = s2[i];
    }
    
    ret[ret.length-1] += " :";
    return ret;
  }
}

public class ExprOperand {
  //type: 0 = numeric operand, 1 = boolean operand
  //      2 = data reg operand, 3 = IO reg operand
  //      4 = position reg operand, -1 = sub-expression
  //      -2 = uninit
  final int type;
  protected int len;
  
  int regIndex;
  float dataVal;
  boolean boolVal;
  
  public ExprOperand() {
    if(this instanceof AtomicExpression) {
      type = -1;
      regIndex = -1;
    } else {
      type = -2;
      len = 1;
      regIndex = -1;
    }
  }
  
  public ExprOperand(float d) {
    type = 0;
    len = 1;
    regIndex = -1;
    dataVal = d;
    boolVal = getBoolVal(dataVal);
  }
  
  public ExprOperand(boolean b) {
    type = 1;
    len = 1;
    regIndex = -1;
    dataVal = b ? 1 : 0;
    boolVal = b;
  }
  
  public ExprOperand(DataRegister dReg, int i) {
    type = 2;
    len = 2;
    regIndex = i;
    if(i != -1 && dReg.value != null) {
      dataVal = dReg.value;
      boolVal = getBoolVal(dataVal);
    }
  }
  
  public ExprOperand(IORegister ioReg, int i) {
    type = 3;
    len = 2;
    regIndex = i;
    if(ioReg.state == ON) {
      dataVal = 1;
      boolVal = true;
    } else {
      dataVal = 0;
      boolVal = false;
    }
  }
  
  public ExprOperand(PositionRegister pReg, int i){
    type = 4;
    len = 2;
    regIndex = i;
    
  }
  
  /* Returns the boolean value of a floating point value, where a value of 0 is considered to be
  *  false and all other values are true. Any floating point value close enough to zero within a
  *  given tolerance is considered to be 0 for the purposes of this function in order to avoid 
  *  issues with floating point errors.
  */
  private boolean getBoolVal(float d) {
    boolean bool;
    
    if(d > -0.00001 || d < 0.00001){
      bool = false;
    } else {
      bool = true;
    }
    
    return bool;
  }
  
  public String toString(){
    String s = "";
    switch(type){
      case -2:
        s = "...";
        break;
      case -1: 
        s = ((AtomicExpression)this).toString();
        break;
      case 0:
        s += dataVal;
        break;
      case 1:
        s += boolVal ? "TRUE" : "FALSE";
        break;
      case 2:
        String rNum = (regIndex == -1) ? "..." : ""+regIndex;
        s += "DR[" + rNum + "]";
        break;
      case 3:
        rNum = (regIndex == -1) ? "..." : ""+regIndex;
        s += "IO[" + rNum + "]";
        break;
    }
    
    return s;
  }
  
  public String[] toStringArray(){
    String[] s;
    switch(type){
      case -2:
        s = new String[] {"..."};
        break;
      case -1: 
        s = ((AtomicExpression)this).toStringArray();
        break;
      case 0:
      case 1:
        s = new String[] {this.toString()};
        break;
      case 2:
        String rNum = (regIndex == -1) ? "..." : ""+regIndex;
        s = new String[] {"DR[", rNum + "]"};
        break;
      case 3:
        rNum = (regIndex == -1) ? "..." : ""+regIndex;
        s = new String[] {"IO[", rNum + "]"};
        break;
      default:
        s = null;
        break;
    }
    
    return s;
  }
}

/**
 * This class is designed to save an arithmetic expression for a register statement instruction. Register statements include the
 * folllowing operands: floating-point constants, Register values, Position Register points, and Position Register values. Legal
 * operators for these statements include addition(+), subtraction(-), multiplication(*), division(/), modulus(%), integer
 * division(|), and parenthesis(). An expression is evaluated left to right, ignoring any operation prescendence, save for
 * parenthesis, whose contents are evaluated first. An expression will have a maximum of 5 operators (discluding closing parenthesis).
 * 
 * Singleton arrays indecate the result will be stored in a Register. tripleton arrays indicate the result will be stored in a
 * Position Register. For either case, the first value in the array is the index of the destination register. For the tripleton
 * arrays, if the second value in the array is 0, then a point from the current prorgrams local position registers is used,
 * otherwise a point from the global position register list is used. If the third value is -1, then the result of the expression
 * is expected to be a Point Object that will override the position register entry. However, if the second value is between 0 and
 * 5 inclusive, then the result is expected to be a floating-point value, which will be saved in a specific entry of the the
 * position register.
 * 
 * Floating-point values     ->  Constants
 * RobotPoint                ->  Robot's current position
 * Singleton integer arrays  ->  Register values
 * Tripleton integer arrays  ->  Position Register points/values
 *
 * For the third entry of the tripleton array:
 *   0 - 5  ->  J1 - J6           (for Joint points)
 *          ->  X, Y, Z, W, P, R  (for Cartesian points)
 * 
 */
public class RegExpression {
  /* The individual elements of the expressions */
  private final ArrayList<Object> parameters;
  
  /**
   * Creates a new expression with a single null value.
   */
  public RegExpression() {
    parameters = new ArrayList<Object>();
    // Expression begins as R[i]/PR[i]/PR[x, y] = _
    parameters.add(null);
  }
  
  /**
   * Adds the operator at the index in the list of parameters and
   * then adds the operand after the operator.
   * 
   * @param idx       Where to add the operator and operand
   * @param op        The operator to add to the expression
   * @ param operand  The operand to add to the expression
   */
  public void addParameter(int idx, Operator op, Object operand) {
    parameters.set(idx, op);
    parameters.add(idx + 1, operand);
  }
  
  /**
   * Adds a set of parenthesis to the expression at the given index
   * with an empty operand inside the parenthesis
   * 
   * @param idx  Where to add the set of parethesis to in the expression
   */
  public void addParenthesis(int idx) {
    parameters.set(idx, Operator.PAR_OPEN);
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
    // Empty expression
    if (parameters.size() == 0) { return null; }
    
    try {
      int pdx = 0,
          len = parameters.size();
      Stack<Object> savedOps = new Stack<Object>();
      Object result = Float.NaN;
      Operator op = Operator.PAR_OPEN;
      
      while (pdx < len) {
        Object param = parameters.get(pdx++);
        
        while (param == Operator.PAR_OPEN) {
          // Save current result and operator, when entering paranthesis
          savedOps.push(result);
          result = Float.NaN;
          savedOps.push(op);
          op = Operator.PAR_OPEN;
          
          param = parameters.get(pdx++);
        }
        
        if (op == Operator.PAR_OPEN) {
          // Reset result, when entering paranthesis
          result = getValueOf(param);
        } else {
          result = operation(result, getValueOf(param), op);
        }
        
        op = (Operator)( parameters.get(pdx++) );
        
        while (op == Operator.PAR_CLOSE) {
          // Remove and evaluate saved value-operator pairs, when exiting paranethesis
          param = savedOps.pop();
          
          if (param != Operator.PAR_OPEN) {
            result = operation(savedOps.pop(), result, (Operator)param);
            
            if (pdx == len) {
              // The ending paranthesis is the last parameter
              return result;
            }
            
            op = (Operator)( parameters.get(pdx++) );
            break;
          } else {
            savedOps.pop();
          }
        }
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
   * Given a value operand parameter, the value associated with that parameter is
   * returned. Valid parameters and their associated values are described in the
   * ExpressionSet class comment.
   * 
   * @param parameter  A floating-point value or integer array parameter
   * @returning        Either a floating-point value or CoordPoint object depending
   *                   on the given parameter
   * @throws ExpressionEvaluationException  if the given parameter is invalid
   */
  public Object getValueOf(Object parameter) throws ExpressionEvaluationException {
    if (parameter instanceof Float) {
      
      // Constant value
      return parameter;
    } else if (parameter instanceof int[]) {
      int[] regIdx = (int[])parameter;
      
      if (regIdx.length == 1) {
        
        // Use Register value
        return DAT_REG[ regIdx[0] ].value;
      } else if (regIdx.length == 3) {
        RegStmtPoint pt;
        
        if (regIdx[1] == 0) {
          // Use a local position register point
          Point p = programs.get(active_prog).LPosReg[ regIdx[0] ];
          // TODO Cartesian or Joint?
          pt = new RegStmtPoint(p.position, p.orientation);
        } else {
          // Use Position Register point
          PositionRegister Preg = GPOS_REG[ regIdx[0] ];
          
          if (Preg.isCartesian) {
            // Convert to a Cartesian value
            pt = new RegStmtPoint(Preg.point.position, Preg.point.orientation);
          } else {
            pt = new RegStmtPoint(Preg.point.angles);
          }
        }
        
        if (regIdx[2] == -1) {
          
          // Use Position Register point
          return pt;
        } else if (regIdx[2] > 0 && regIdx[2] < 6) {
          
          // Use a specific value from the Point
          return pt.values[ regIdx[2] ];
        } else {
          // Illegal parameter
          throw new ExpressionEvaluationException(1);
        }
      } else {
        // Illegal parameter
        throw new ExpressionEvaluationException(1);
      }
      
    } else if (parameter instanceof RobotPoint) {
      // Use the Robot's current position
      return ((RobotPoint)parameter).getValue();
    } else {
      // Illegal parameter
      throw new ExpressionEvaluationException(1);
    }
  }
  
  /**
   * Evaluate the given parameters with the given operation. The only valid parameters are floating-point values
   * and int arrays. The integer arrays should singeltons (for Registers) or tripletons (for Position Registers
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
      
    } else if (param1 instanceof Point && param2 instanceof Point) {
      
      return evaluatePair((RegStmtPoint)param1, (RegStmtPoint)param2, op);
      
    } else {
      // Invalid parameter types
      throw new ExpressionEvaluationException(1);
    }
  }
  
  private Object evaluatePair(Float value1, Float value2, Operator op) throws ExpressionEvaluationException {
    
    // Float-Float operations
    switch(op) {
      case ADDTN:       return value1 + value2;
      case SUBTR:      return value1 - value2;
      case MULT:   return value1 * value2;
      case DIV:     return value1 / value2;
      // Returns the remainder
      case MOD:    return (value1 % value2) / value2;
      // Returns the quotient
      case INTDIV:  return (int)(value1 / value2);
      default:         throw new ExpressionEvaluationException(1);
    }
  }
  
  private Object evaluatePair(RegStmtPoint point1, RegStmtPoint point2, Operator op) throws ExpressionEvaluationException {
      
    // TODO Point-Point operations
    switch(op) {
      case ADDTN:
      case SUBTR:
        RegStmtPoint pt = new RegStmtPoint();
        
        for (int idx = 0; idx < 6; ++idx) {
          if (op == Operator.ADDTN) {
            // Add each point's Joint values together
            pt.values[idx] = point1.values[idx] + point2.values[idx];
          } else {
            // Subtract the second point's Joint values from those of the first
            pt.values[idx] = point1.values[idx] - point2.values[idx];
          }
        }
        
        return pt;
      case MULT:
      case DIV:
      case MOD:
      case INTDIV:
        break;
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
      } else if (regIdx.length == 3) {
        String prefix = (regIdx[1] == 0) ? "P" : "PR",
               indices = null;
        
        // Index into the list of points
        indices = Integer.toString(regIdx[0]);
        
        if (regIdx[2] >= 0) {
          // Index into the values of the point
          indices += ", " + regIdx[2];
        }
        
        // Position Register entries are stored as a tripleton integer array
        return String.format("%s[%s]", prefix, indices);
      }
    } else if (param instanceof Operator) {
      return ((Operator)param).symbol;
    }
    
    // Simply print the value for constants
    return param.toString();
  }
}
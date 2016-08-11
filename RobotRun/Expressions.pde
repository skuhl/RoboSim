/**
 * This class is designed to save an arithmetic expression for a register statement instruction. Register statements include the
 * folllowing operands: floating-point constants, Register values, Position Register points, and Position Register values. Legal
 * operators for these statements include addition(+), subtraction(-), multiplication(*), division(/), modulus(%), integer
 * division(|), and parenthesis(). An expression is evaluated left to right, ignoring any operation prescendence, save for
 * parenthesis, whose contents are evaluated first. An expression will have a maximum of 5 operators (discluding closing parenthesis).
 * 
 * All valid operands should extend the Operand interface, which simply has a method to get the value of the operand. Constant operands
 * hold a single floating-point constants. Register operands holds an index for a specific Data register entry. Position operands hold
 * and index for a specific position (or Position register), a position index, and a position type. A Position operands type determines
 * if the operands represents a global Position register entry, or a local position entry of the active program. The position index
 * determines if the entire position value will be used, or if a specific value of the position will be used.
 * 
 * -1    -> use the Point itself
 * 0 - 6 -> use specific value of the Point corresponding to the poisition index
 *
 * Robot Point operands are a place holder for the Robot's current position/orientaion or joint anhles and function similar to a Position
 * operand, whose position index is -1. SubExpression operands hold an entirely inidependent expression.
 *
 *
 * ConstantOp       ->  Constants
 * RobotPositionOp  ->  Robot's current position
 * RegisterOp       ->  Register values
 * PositionOp       ->  Position Register points/values
 */
public class RegisterExpression {
  private final ArrayList<Object> parameters;
  
  /**
   * Creates an empty expression
   */
  public RegisterExpression() {
    parameters = new ArrayList<Object>();
  }
  
  /**
   * Creates an expression from a String. THe format of the String must strictly
   * include only the following substrings, wach separating by only spaces:
   * 
   * '+', '-', '*', '/',
   * '%', '|', ')', or '('  -> operators
   * R[x]                   -> Register operand
   * P[x]
   * P[x, y]
   * PR[x]
   * PR[x, y]               -> Position operand
   * LPos
   * JPos
   * LPos[x]
   * JPos[x]                -> Robot Point operand
   * z                      -> Floating-point value
   * 
   * where x and y are positive integer values and
   * z is a valid Floating-point value
   * 
   * @parameter exprString  A String, which contains a valid set of the above
   *                        substrings, each separated by only spaces
   * @throws IllegalArgumentException  If anything aside from the parameters
   *                                   defined above exists in the string
   */
  public RegisterExpression(String exprString) throws IllegalArgumentException {
    parameters = new ArrayList<Object>();
    // Split all the parameters into individual Strings
    String[] paramList = exprString.split(" ");
    
    for (String param : paramList) {
      // Parse operator
      if (Pattern.matches("[^0123456789]", param)) {
        switch (param) {
          case "+":
            parameters.add(Operator.ADDTN);
            break;
          case "-":
            parameters.add(Operator.SUBTR);
            break;
          case "*":
            parameters.add(Operator.MULT);
            break;
          case "/":
            parameters.add(Operator.DIV);
            break;
          case "%":
            parameters.add(Operator.MOD);
            break;
          case "|":
            parameters.add(Operator.INTDIV);
            break;
          case "(":
            parameters.add(Operator.PAR_OPEN);
            break;
          case ")":
            parameters.add(Operator.PAR_CLOSE);
            break;
          default:
            throw new IllegalArgumentException( String.format("'%s' is not a valid operator!", param) );
        }
        
      } else if (Pattern.matches("R\\[[0123456789]+\\]", param)) {
        // Parse Register operand
        String idxVal = param.substring(2, param.length() - 1);
        int idx = Integer.parseInt(idxVal);
        parameters.add(new RegisterOp(idx));
        
      } else if (Pattern.matches("P\\[[0123456789]+\\]", param)) {
        // Parse Position operand (local)
        String idxVal = param.substring(2, param.length() - 1);
        int idx = Integer.parseInt(idxVal);
        parameters.add(new PositionOp(idx, PositionType.LOCAL));
        
      } else if (Pattern.matches("P\\[[0123456789]+,[0123456789]+\\]", param)) {
        // Parse Position operand (local, value)
        int commaIdx = param.indexOf(',');
        String idxVal = param.substring(2, commaIdx),
             pdxVal = param.substring(commaIdx + 1, param.length() - 1);
        int idx = Integer.parseInt(idxVal);
        int pdx = Integer.parseInt(pdxVal);
        parameters.add(new PositionOp(idx, pdx, PositionType.LOCAL));
        
      } else if (Pattern.matches("PR\\[[0123456789]+\\]", param)) {
        // Parse Position operand (global)
        String idxVal = param.substring(3, param.length() - 1);
        int idx = Integer.parseInt(idxVal);
        parameters.add(new PositionOp(idx, PositionType.GLOBAL));
        
      } else if (Pattern.matches("PR\\[[0123456789]+,[0123456789]+\\]", param)) {
        // Parse Position operand (global, value)
        int commaIdx = param.indexOf(',');
        String idxVal = param.substring(3, commaIdx),
             pdxVal = param.substring(commaIdx + 1, param.length() - 1);
        int idx = Integer.parseInt(idxVal);
        int pdx = Integer.parseInt(pdxVal);
        parameters.add(new PositionOp(idx, pdx, PositionType.GLOBAL));
        
      } else if (param.equals("LPos")) {
        // Parse Robot Point operand (cartesian)
        parameters.add(new RobotPositionOp(true));
        
      } else if (param.equals("LPos")) {
        // Parse Robot Point operand (joint)
        parameters.add(new RobotPositionOp(false));
        
      } else if (Pattern.matches("LPos\\[[0123456789]+\\]", param)) {
        // Parse Robot Point operand (cartesian, value)
        String pdxVal = param.substring(5, param.length() - 1);
        int pdx = Integer.parseInt(pdxVal);
        parameters.add(new RobotPositionOp(pdx, true));
        
      } else if (Pattern.matches("JPos\\[[0123456789]+\\]", param)) {
        // Parse Robot Point operand (joint, value)
        String pdxVal = param.substring(5, param.length() - 1);
        int pdx = Integer.parseInt(pdxVal);
        parameters.add(new RobotPositionOp(pdx, false));
        
      } else {
        // Parse Floating-point value
        try {
          float val = Float.parseFloat(param);
          parameters.add(new ConstantOp(val));
          
        } catch (NumberFormatException NFEx) {
          throw new IllegalArgumentException( String.format("'%s' is not valid parameter type!", param.toString()) );
        }
      }
    }
  }
  
  /**
   * Creates an expression with the given set of operands and operators.
   */
  public RegisterExpression(Object... params) {
    parameters = new ArrayList<Object>();
    
    for (Object param : params) {
      addParameter(param);
    }
  }
  
  /**
   * Adds the given parameter to the end of this expression
   */
  public void addParameter(Object param) {
    
    if (param instanceof SubExpression) {
      // Add a copy of the given SubExpression
      parameters.add( ((SubExpression)param).clone() );
    } else {
      parameters.add(param);
    }
  }
  
  /**
   * Adds the given parameter to the index in the expression
   */
  public void addParameter(int idx, Object param) {
    
    if (param instanceof SubExpression) {
      // Add a copy of the given SubExpression
      parameters.add(idx, ((SubExpression)param).clone() );
    } else {
      parameters.add(idx, param);
    }
  }
  
  /**
   * Sets the parameter at the given index to the given
   * new pararmeter, in the expression.
   */
  public Object setParameter(int idx, Object param) {
    return parameters.set(idx, param);
  }
  
  /**
   * Removes the parameter at the given index from the expression
   */
  public Object removeParameter(int idx) {
    return parameters.remove(idx);
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
   * of the expression is returned as either a Floating-point value or a RegStmtPoint
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
      Object result = null;
      Operator op = Operator.PAR_OPEN;

      while (true) {
        Object param = parameters.get(pdx++);

        while (param == Operator.PAR_OPEN) {
          // Save current result and operator, when entering parenthesis
          savedOps.push(result);
          savedOps.push(op);
          result = null;
          op = Operator.PAR_OPEN;

          param = parameters.get(pdx++);
        }

        if (op == Operator.PAR_OPEN) {
          // Reset result, when entering parenthesis
          result = ((Operand)param).getValue();
        } else {
          result = evaluateOperation(result, ((Operand)param).getValue(), op);
        }

        if (pdx == len) {
          // Operand ends the expression
          return result;
        }

        op = (Operator)( parameters.get(pdx++) );

        while (op == Operator.PAR_CLOSE) {
          // Remove and evaluate saved value-operator pairs, when exiting parenthesis
          param = savedOps.pop();

          if (param == Operator.PAR_OPEN) {
            // Initial/Nested open parenthesis
            param = savedOps.pop();
          } else {
            // Previous result and operator exists
            result = evaluateOperation(savedOps.pop(), result, (Operator)param);
          }
          
          if (pdx == len) {
            // Parenthesis ends the expression
            return result;
          }

          op = (Operator)( parameters.get(pdx++) );
        }
      }
      
    } catch (NullPointerException NPEx) {
      // Missing a parameter
      throw new ExpressionEvaluationException(0, NPEx.getClass());

    } catch (IndexOutOfBoundsException IOOBEx) {
      // Invalid register index
      throw new ExpressionEvaluationException(0, IOOBEx.getClass());

    } catch (ClassCastException CCEx) {
      // Illegal parameters or operations
      throw new ExpressionEvaluationException(1, CCEx.getClass());

    } catch (ArithmeticException AEx) {
      // Illegal parameters or operations
      throw new ExpressionEvaluationException(2, AEx.getClass());

    } catch (EmptyStackException ESEx) {
      // Invalid parenthesis
      throw new ExpressionEvaluationException(3, ESEx.getClass());
      
    }
  }
  
  /**
   * Evaluate the given parameters with the given operation. The only valid parameters are floating-point values
   * and int arrays. The integer arrays should singeltons (for Registers) or tripletons (for Position Registers
   * and Position Register Values).
   * 
   * @param param1  The first parameter of the opertion
   * @param param2  The second parameter for the operation
   * @param op      The operation to preform on the parameters
   * @return        The result of the operation on param1 and param2
   * 
   * @throws ExpressionEvaluationException  if the given combination of parameters with the given
   *                                        operation is illegal
   */
  private Object evaluateOperation(Object a, Object b, Operator op) {
    if (a instanceof Float && b instanceof Float) {
      // Float-Float operation
      return evaluateFloatOperation((Float)a, (Float)b, op);
    } else if (a instanceof Point && b instanceof Point) {
      // Point-Point operation
      return evaluePointOperation((RegStmtPoint)a, (RegStmtPoint)b, op);
    }
    // Illegal operation
    throw new ExpressionEvaluationException(4);
  }
  
  private Float evaluateFloatOperation(Float a, Float b, Operator op) {
    // Float-to-Float operations
      switch(op) {
        case ADDTN:  return a + b;
        case SUBTR:  return a - b;
        case MULT:   return a * b;
        case DIV:    return a / b;
        case MOD:    return a % b;
        case INTDIV: return new Float(a.intValue() / b.intValue());
        default:
      }
      // Illegal operator
      throw new ExpressionEvaluationException(5);
  }
  
  private RegStmtPoint evaluePointOperation(RegStmtPoint a, RegStmtPoint b, Operator op) {
    // Point-to-Point operations
    switch(op) {
        case ADDTN:
          return a.add(b);
        case SUBTR:
          return a.subtract(b);
        default:
      }
      // Illegal operator
      throw new ExpressionEvaluationException(6);
  }
  
  /**
   * Returns the number of operators AND operands in the expression
   */
  public int parameterSize() { return parameters.size(); }
  
  /**
   * Returns a list of the parameters in the expression, each in the
   * form of a String and occupying individual elements in the list.
   * 
   * @returning  The list of parameters in the form of Strings
   */
  public ArrayList<String> toStringArrayList() {
    ArrayList<String> paramList = new ArrayList<String>();
    
    for (Object param : parameters) {
      
      if (param == null) {
        paramList.add("_");
      } else {
        paramList.add(param.toString());
      }
    }
    
    return paramList;
  }
  
  /**
   * Copies the current expression (cloning sub expressions) and
   * returns the a new Expression with the duplicate set of parameters.
   * 
   * @returning  A copy of the this expression
   */
  public Object clone() {
    RegisterExpression copy = new RegisterExpression();
    
    for (Object param : parameters) {
      copy.addParameter(param);
    }
    
    return copy;
  }
  
  public String toString() {
    String expressionString = new String();
    ArrayList<String> paramSet = toStringArrayList();
    
    for (int pdx = 0; pdx < paramSet.size(); ++pdx) {
      // Combine the list of parameter Strings, separating each by a single space
      expressionString += paramSet.get(pdx);
      
      if (pdx < (paramSet.size() - 1)) {
        expressionString += " ";
      }
    }
    
    return expressionString;
  }
}

public interface Operand {
  /* Should return either a Float or RegStmtPoint Object */
  public abstract Object getValue();
}

/**
 * A operand that represents a floating-point contant value.
 */
public class ConstantOp implements Operand {
  private final float value;
  
  public ConstantOp() {
    value = 0f;
  }
  
  public ConstantOp(float val) {
    value = val;
  }
  
  public Object getValue() { return new Float(value); }
  
  public String toString() {
    return String.format("%4.3f", value);
  }
}

/**
 * A operand that represents a specific register entry.
 */
public class RegisterOp implements Operand {
  private final int listIdx;
  
  public RegisterOp() {
    listIdx = 0;
  }
  
  public RegisterOp(int i) {
    listIdx = i;
  }
  
  public Object getValue() {
    return DREG[listIdx];
  }
  
  public int getIdx() { return listIdx; }
  
  public String toString() {
    return String.format("R[%d]", listIdx);
  }
}

/**
 * A operand that can represent either the joint angles or cartesian values
 * stored in a position register entry, or a specific value of that position,
 * from either the global Position Registers or the local positions of the active program.
 */
public class PositionOp extends RegisterOp {
  private final int posIdx;
  /* Determines whether a global Position register or a local position will be used */
  private final PositionType type;
  
  public PositionOp() {
    super(0);
    posIdx = -1;
    type = PositionType.LOCAL;
  }
  
  public PositionOp(int ldx, PositionType t) {
     super(ldx);
    posIdx = -1;
    type = t;
  }
  
  public PositionOp(int ldx, int pdx, PositionType t) {
    super(ldx);
    posIdx = pdx;
    type = t;
  }
  
  public Object getValue() {
    RegStmtPoint pt;
    
    if (type == PositionType.LOCAL) {
      // Use local position
      Program current = activeProgram();
      // TODO Use joint angles?
      pt = new RegStmtPoint( current.LPosReg[getIdx()], true);
    } else if (type == PositionType.GLOBAL) {
      // global Position register
      pt = new RegStmtPoint( GPOS_REG[getIdx()].point, GPOS_REG[getIdx()].isCartesian );
    } else {
      // Not a valid type
      return null;
    }
    
    if (posIdx == -1) {
      // Use the whole Point
      return pt;
    } else {
      // Use a specific value of the Point
      return pt.getValue(posIdx);
    }
  }
  
  public String toString() {
    if (posIdx == -1) {
      
      if (type == PositionType.GLOBAL) {
        return String.format("PR[%d]", getIdx());
      } else {
        return String.format("P[%d]", getIdx());
      }
    } else {
      
      if (type == PositionType.GLOBAL) {
        return String.format("PR[%d]", getIdx());
      } else {
        return String.format("P[%d, %d]", getIdx(), posIdx);
      }
    }
  }
}

/**
 * An operand thaht represents the current position and orientation of the Robot,
 * or its joint Angles, or a specific value of either point.
 */
public class RobotPositionOp implements Operand {
  /**
   * The value of valIdx corresponds to:
   *   -1     ->  The point itself
   *   0 - 5  ->  J1 - J6
   *   6 - 11 ->  X, Y, Z, W, P, R
   */
  private final int valIdx;
  private final boolean isCartesian;

  /**
   * Default to the entire point
   */
  public RobotPositionOp(boolean cartesian) {
    valIdx = -1;
    isCartesian = cartesian;
  }
  
  /**
   * Specific the index of the value of the point
   */
  public RobotPositionOp(int vdx, boolean cartesian) {
    valIdx = vdx;
    isCartesian = cartesian;
  }
  
  /**
   * Return the current position of the Robot or a specific value of the current position of
   * the Robot
   */
  public Object getValue() {
    Point RP = nativeRobotEEPoint(armModel.getJointAngles());
    RegStmtPoint pt = new RegStmtPoint(RP, isCartesian);
    
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
      if (isCartesian) {
        return String.format("LPos");
      } else {
        return String.format("JPos");
      }
    } else {
      // Only show index if the whole point is not used
      if (isCartesian) {
        return String.format("LPos[%d]", valIdx);
      } else {
        return String.format("JPos[%d]", valIdx);
      }
    }
  }
}

/**
 * Store a separate expression nested inside another expression as an operand
 */
public class SubExpression implements Operand {
  private final RegisterExpression expr;
  
  public SubExpression() {
    expr = new RegisterExpression();
    expr.addParameter(new ConstantOp(1));
  }
  
  public SubExpression(Object... params) {
    expr = new RegisterExpression(params);
  }
  
  public Object getValue() throws ExpressionEvaluationException { return expr.evaluate(); }
  
  public Object clone() {
    // Copy the expression into a new Sub Expression
    return new SubExpression(expr.clone());
  }
  
  public String toString() {
    return String.format("[ %s ]", expr.toString());
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
  private final float[] values;
  private boolean isCartesian;
  
  public RegStmtPoint() {
    values = new float[] { 0f, 0f, 0f, 0f, 0f, 0f };
    isCartesian = false;
  }
  
  public RegStmtPoint(float[] iniValues, boolean cartesian) {
    if (iniValues.length < 6) {
      // Not valid input values
      values = new float[] { 0f, 0f, 0f, 0f, 0f, 0f };
    } else {
      // Copy initial values
      values = Arrays.copyOfRange(iniValues, 0, 6);
    }
    
    isCartesian = cartesian;
  }
  
  public RegStmtPoint(Point initial, boolean cartesian) {
    values = new float[6];
    isCartesian = cartesian;
    // Conver to W, P, R values
    PVector wpr = quatToEuler(initial.orientation);
    // Copy values into this point
    values[0] = initial.position.x;
    values[1] = initial.position.x;
    values[2] = initial.position.x;
    values[3] = wpr.x;
    values[4] = wpr.y;
    values[5] = wpr.z;
  }
  
  public Float getValue(int val) {
    if (val < 0 || val >= values.length) {
      // Not a valid index
      return null;
    }
    // Return value associated with the index
    return new Float(values[val]);
  }
  
  public void setValue(int val, float newVal) {
    if (val >= 0 && val < values.length) {
      // Set the specified entry to the given value
      values[val] = newVal;
    }
  }
  
  public boolean isCartesian() { return isCartesian; }
  
  public RegStmtPoint add(RegStmtPoint pt) {
    if (pt == null || pt.isCartesian() != isCartesian) {
      // Must be the same type of point
      return null;
    }
    
    float[] sums = new float[6];
    // Compute sums
    for (int pdx = 0; pdx < values.length; ++pdx) {
      sums[pdx] = values[pdx] + pt.getValue(pdx);
    }
    
    return new RegStmtPoint(sums, isCartesian);
  }
  
  public RegStmtPoint subtract(RegStmtPoint pt) {
    if (pt == null || pt.isCartesian() != isCartesian) {
      // Must be the same type of point
      return null;
    }
    
    float[] differences = new float[6];
    // Compute sums
    for (int pdx = 0; pdx < values.length; ++pdx) {
      differences[pdx] = values[pdx] - pt.getValue(pdx);
    }
    
    return new RegStmtPoint(differences, isCartesian);
  }
  
  public Point toPoint() {
    
    if (isCartesian) {
      PVector position = new PVector(values[0], values[1], values[2]),
              wpr = new PVector(values[3], values[4], values[5]);
              // Convet back to quaterninos
      float[] orientation = eulerToQuat(wpr);
      // TODO initialize angles?
      return new Point(position, orientation);
    } else {
      // Use forward kinematics to find the position and orientation of the joint angles
      return nativeRobotEEPoint(values);
    }
  }
  
  public String toString() {
    if (isCartesian) {
      // X, Y, Z, W, P, R
      return String.format("[ %4.3f, %4.3f, %4.3f], [ %4.3f, %4.3f, %4.3f ]",
          values[0], values[1], values[2],
          Math.toDegrees(values[3]), Math.toDegrees(values[4]), Math.toDegrees(values[5]));
    } else {
      // J1 - J6
      return String.format("[ %4.3f, %4.3f, %4.3f, %4.3f, %4.3f, %4.3f ]",
          Math.toDegrees(values[0]), Math.toDegrees(values[1]), Math.toDegrees(values[2]),
          Math.toDegrees(values[3]), Math.toDegrees(values[4]), Math.toDegrees(values[5]));
    }
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

public interface ExpressionElement {
  
  public abstract int getLength();
  public abstract String toString();
  public abstract String[] toStringArray();
}

public class ExprOperand implements ExpressionElement {
  //type: 0 = numeric operand, 1 = boolean operand
  //      2 = data reg operand, 3 = IO reg operand
  //      4 = position reg operand, -1 = sub-expression
  //      -2 = uninit
  protected int type;
  protected int exprIndex;
  
  int regIndex;
  float dataVal;
  boolean boolVal;
  
  public ExprOperand() {
    type = -2;
    regIndex = -1;
  }
  
  public ExprOperand(float d) {
    type = 0;
    regIndex = -1;
    dataVal = d;
    boolVal = getBoolVal(dataVal);
  }
  
  public ExprOperand(boolean b) {
    type = 1;
    regIndex = -1;
    dataVal = b ? 1 : 0;
    boolVal = b;
  }
  
  public ExprOperand(DataRegister dReg, int i) {
    type = 2;
    regIndex = i;
    if(i != -1 && dReg.value != null) {
      dataVal = dReg.value;
      boolVal = getBoolVal(dataVal);
    }
  }
  
  public ExprOperand(IORegister ioReg, int i) {
    type = 3;
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
    regIndex = i;
  }
  
  public ExprOperand reset() {
    type = -2;
    regIndex = -1;
    return this;
  }
  
  public ExprOperand set(float d) {
    type = 0;
    regIndex = -1;
    dataVal = d;
    boolVal = getBoolVal(dataVal);
    return this;
  }
  
  public ExprOperand set(boolean b) {
    type = 1;
    regIndex = -1;
    dataVal = b ? 1 : 0;
    boolVal = b;
    return this;
  }
  
  public ExprOperand set(DataRegister dReg, int i) {
    type = 2;
    regIndex = i;
    if(i != -1 && dReg.value != null) {
      dataVal = dReg.value;
      boolVal = getBoolVal(dataVal);
    }
    return this;
  }
  
  public ExprOperand set(IORegister ioReg, int i) {
    type = 3;
    regIndex = i;
    if(ioReg.state == ON) {
      dataVal = 1;
      boolVal = true;
    } else {
      dataVal = 0;
      boolVal = false;
    }
    
    return this;
  }
  
  public ExprOperand set(PositionRegister pReg, int i){
    type = 4;
    regIndex = i;
    return this;
  }
  
  public int getLength() {
    return 1;
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
        s += "R[" + rNum + "]";
        break;
      case 3:
        rNum = (regIndex == -1) ? "..." : ""+regIndex;
        s += "IO[" + rNum + "]";
        break;
    }
    
    return s;
  }
  
  public String[] toStringArray() {
    return new String[] { this.toString() };
  }
}

public class AtomicExpression extends ExprOperand {
  protected ExprOperand arg1;
  protected ExprOperand arg2;
  protected Operator op;
  protected int len;
    
  public AtomicExpression(){
    type = -1;
    op = Operator.UNINIT;
    len = 1;
    arg1 = new ExprOperand();
    arg2 = new ExprOperand();
  }
  
  public AtomicExpression(Operator o){
    type = -1;
    op = o;
    len = 3;
    arg1 = new ExprOperand();
    arg2 = new ExprOperand();
  }
  
  public ExprOperand getArg1() { return arg1; }
  public ExprOperand setArg1(ExprOperand a) { 
    arg1 = a;
    len = getLength();
    return arg1;
  }
  
  public ExprOperand getArg2() { return arg2; }
  public ExprOperand setArg2(ExprOperand a) { 
    arg2 = a;
    len = getLength();
    return arg2;
  }
  
  public ExprOperand setArg(ExprOperand a, int argNo) {
    if(argNo == 1) {
      return setArg1(a);
    } else {
      return setArg2(a);
    }
  }
  
  public Operator getOperator() { return op; }
  public void setOperator(Operator o) {
    op = o;
    len = getLength();
  }
  
  public int getLength() {
    if(op == Operator.UNINIT) {
      return 1;    
    }
    
    int ret = 1;
    ret += arg1.getLength();
    ret += arg2.getLength();
    ret += (arg1.type == -1) ? 2 : 0;
    ret += (arg2.type == -1) ? 2 : 0;
    return ret;
  }
  
  public ExprOperand evaluate() {
    ExprOperand result;
    float o1, o2;
    boolean b1, b2;
    
    if(arg1.type == -1) {
      ExprOperand temp = ((AtomicExpression)arg1).evaluate();
      o1 = temp.dataVal;
      b1 = temp.boolVal;
    } else {
      o1 = arg1.dataVal;
      b1 = arg1.boolVal;
    }
    
    if(arg2.type == -1) {
      ExprOperand temp = ((AtomicExpression)arg2).evaluate();
      o2 = temp.dataVal;
      b2 = temp.boolVal;
    } else {
      o2 = arg1.dataVal;
      b2 = arg1.boolVal;
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
      case AND:
        result = new ExprOperand(b1 && b2);
      case OR:
        result = new ExprOperand(b1 || b2);
      default:
        result = null;
        break;
    }
    
    return result;
  }
  
  public ExprOperand evaluate(ExprOperand a, ExprOperand b) {
    setArg1(a);
    setArg2(b);
    return evaluate();
  }
  
  public String toString(){
    String s = "";
    
    if(op == Operator.UNINIT){
      return "...";
    }
    
    if(arg1 instanceof AtomicExpression)
      s += "(" + arg1.toString() + ")";
    else 
      s += arg1.toString();
      
    s += " " + op.symbol + " ";
    
    if(arg2 instanceof AtomicExpression)
      s += "(" + arg2.toString() + ")";
    else 
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
    
    ret[s1.length + lm1] = opString;
    
    if(lm2 != 0) {
      ret[s1.length + lm1 + 1] = "(";
      ret[ret.length - 1] = ")";
    }
    
    for(int i = lm1/2; i < ret.length; i += 1) {
      if(ret[i] == null) {
        if(i < s1.length + lm1/2) {
          ret[i] = s1[i - lm1/2];
        }
        else {
          ret[i] = s2[i - s1.length - lm1 - 1 - lm2/2];
        }
      }
    }
    
    return ret;
  }
}

public class Expression extends AtomicExpression {
  private ArrayList<ExpressionElement> elementList;
  
  public Expression() {
    elementList = new ArrayList<ExpressionElement>();
    elementList.add(new ExprOperand());
  }
  
  public ExpressionElement get(int idx) {
    return elementList.get(idx);
  }
  
  public ExprOperand getOperand(int idx) {
    if(elementList.get(idx) instanceof ExprOperand)
      return (ExprOperand)elementList.get(idx);
    else
      return null;
  }
  
  public Operator getOperator(int idx) {
    if(elementList.get(idx) instanceof Operator)
      return (Operator)elementList.get(idx);
    else
      return null;
  }
  
  public ExprOperand setOperand(int idx, ExprOperand o) {
    if(elementList.get(idx) instanceof ExprOperand) {
      elementList.set(idx, o);
      return (ExprOperand)elementList.get(idx);
    }
    else {
      return null;
    }
  }
  
  public Operator setOperator(int idx, Operator o) {
    if(elementList.get(idx) instanceof Operator) {
      elementList.set(idx, o);
      return (Operator)elementList.get(idx); 
    }
    else {
      return null;
    }
  }
  
  public void insertElement(int edit_idx) {
    if(getLength() >= 21) return;
    
    if(edit_idx == -1) {
      if(elementList.get(0) instanceof ExprOperand) {
        elementList.add(0, Operator.UNINIT);
      } else {
        elementList.add(0, new ExprOperand());
      }
    } 
    else {
      int[] elements = mapToEdit();
      int start_idx = getStartingIdx(elements[edit_idx]);
      ExpressionElement e = elementList.get(elements[edit_idx]);
      
      if(e instanceof Expression && (edit_idx != start_idx + e.getLength() - 1)) {
        edit_idx -= (start_idx + 1);
        ((Expression)e).insertElement(edit_idx);
      } 
      else {
        if(e instanceof ExprOperand) {
          elementList.add(elements[edit_idx] + 1, Operator.UNINIT);
        } else {
          elementList.add(elements[edit_idx] + 1, new ExprOperand());
        }
      }
    }
  }
  
  public void removeElement(int edit_idx) {
    if(elementList.size() > 1) {
      int[] elements = mapToEdit();
      int start_idx = getStartingIdx(elements[edit_idx]);
      ExpressionElement e = elementList.get(elements[edit_idx]);
      
      if(e instanceof Expression) {
        if(edit_idx == start_idx || edit_idx == start_idx + e.getLength() - 1) {
          elementList.remove(elements[edit_idx]);
        } else {
          edit_idx -= (start_idx + 1);
          ((Expression)e).removeElement(edit_idx);
        }
      } 
      else {
        elementList.remove(elements[edit_idx]);
      }
    }
  }
    
  public int getLength() {
    len = 2;
    for(ExpressionElement e: elementList) {
      len += e.getLength();
    }

    return len;
  }
  
  public ExprOperand evaluate() throws ExpressionEvaluationException {
    if(elementList.get(0) instanceof Operator) { return null; } //can throw error here
    
    ExprOperand result = (ExprOperand)elementList.get(0);
    
    for(int i = 1; i < elementList.size(); i += 2) {
      if(!(elementList.get(i) instanceof Operator) || !(elementList.get(i + 1) instanceof ExprOperand)) {
        return null; //and here
      } else {
        Operator op = (Operator) elementList.get(i);
        ExprOperand nextOperand = (ExprOperand) elementList.get(i + 1);
        AtomicExpression expr = new AtomicExpression(op);
        
        result = expr.evaluate(result, nextOperand);
      }
    }
    
    return result;
  }
  
  public int[] mapToEdit() {
    int[] ret = new int[getLength() - 2];
    int element_start = 0;
    int element_idx = 0;
    
    for(int i = 0; i < ret.length; i += 1) {
      int len = elementList.get(element_idx).getLength();
      ret[i] = element_idx;
      
      if(i - element_start >= len - 1) {
        element_idx += 1;
        element_start = i + 1;
      }
    }
    
    return ret;
  }
  
  public int getStartingIdx(int element) {
    int[] elements = mapToEdit();
    int idx = 0;
    
    while(elements[idx] != element) {
      idx += 1;
    }
    
    return idx;
  }
  
  public String toString() {
    String ret = "(" + elementList.get(0).toString();
    for(int i = 0; i < elementList.size(); i += 1) {
      ret += elementList.get(i).toString();
    }
    
    ret += ")";
    return ret;
  }
  
  public String[] toStringArray() {
    String[] ret = new String[this.getLength()];
    ret[0] = "(";
    
    int idx = 1;
    for(ExpressionElement e: elementList) {
      String[] temp = e.toStringArray();
      for(int i = 0; i < temp.length; i += 1) {
        ret[idx + i] = temp[i];
      }
      idx += temp.length;
    }
    
    ret[ret.length - 1] = ")";
    return ret;
  }
}

public class BooleanExpression extends AtomicExpression {
  public BooleanExpression() {
    super();
  }
  
  public BooleanExpression(Operator o) {
    if(o.type == BOOL) {
      type = -1;
      op = o;
      len = 3;
      arg1 = new ExprOperand();
      arg2 = new ExprOperand();
    }
    else {
      type = -1;
      op = Operator.UNINIT;
      len = 1;
    }
  }
  
  public void setOperator(Operator o) {
    if(o.type != BOOL) return;
    
    op = o;
    len = getLength();
  }
}
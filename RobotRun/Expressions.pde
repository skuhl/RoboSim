/* These are used to store the operators used in register statement expressions in the ExpressionSet Object */
public enum Operator { PLUS, MINUS, MUTLIPLY, DIVIDE, MODULUS, INTDIVIDE, PAR_OPEN, PAR_CLOSE; }

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
    CartPoint pt = new CartPoint( new Point(armModel.getEEPos(), armModel.getQuaternion()) );
    
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
 * in space (X, Y, Z, W, P, R) along with the joint angles (J1 - J6)
 * necessary for the Robot to reach the position and orientation of
 * the Point.
 * This class is designed to temporary store the values of a Point object
 * in order to bypass multiple conversion between Euler angles and
 * Quaternions during the evaluation of Register Statement Expressions.
 */
public class CartPoint {
  /**
   * The values associated with this Point:
   *   0 - 5  ->  J1 - J6
   *   6 - 11 ->  X, Y, Z, W, P, R
   */
  public float[] values;
  
  /**
   * Point for the default position of the Robot
   */
  public CartPoint() {
    values = new float[12];
    
    for (int idx = 0; idx < 6; ++idx) {
      values[idx] = 0;
    }
    
    float[] angles = Arrays.copyOfRange(values, 0, 5);
    PVector xyz = armModel.getEEPos(angles);
    PVector wpr = armModel.getWPR(angles);
    
    values[6] = xyz.x;
    values[7] = xyz.y;
    values[8] = xyz.z;
    values[9] = wpr.x;
    values[10] = wpr.y;
    values[11] = wpr.z;
  }
  
  /**
   * Converts the given Point Object to a PointCoord object
   */
  public CartPoint(Point pt) {
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
 * 11 inclusive, then the result is expected to be a floating-point value, which will be saved in a specific entry of the the
 * position register.
 * 
 * Floating-point values     ->  Constants
 * RobotPoint                ->  Robot's current position
 * Singleton integer arrays  ->  Register values
 * Tripleton integer arrays  ->  Position Register points/values
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
        return REG[ regIdx[0] ].value;
      } else if (regIdx.length == 3) {
        CartPoint pt;
        
        if (regIdx[1] == 0) {
          // Use Position Register point
          pt = new CartPoint( programs.get(active_prog).LPosReg[ regIdx[0] ] );
        } else {
          // Use a local position register point
          pt = new CartPoint(GPOS_REG[ regIdx[0] ].point);
        }
        
        if (regIdx[2] == -1) {
          
          // Use Position Register point
          return pt;
        } else if (regIdx[2] > 0 && regIdx[2] < 12) {
          
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
      
      return evaluatePair((CartPoint)param1, (CartPoint)param2, op);
      
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
      case MODULUS:  return (value1 % value2) / value2;
      // Returns the quotient
      case INTDIVIDE:  return (int)(value1 / value2);
      default:         throw new ExpressionEvaluationException(1);
    }
  }
  
  private Object evaluatePair(CartPoint point1, CartPoint point2, Operator op) throws ExpressionEvaluationException {
      
    // TODO Point-Point operations
    switch(op) {
      case PLUS:
      case MINUS:
        CartPoint pt = new CartPoint();
        
        for (int idx = 0; idx < 6; ++idx) {
          if (op == Operator.PLUS) {
            // Add each point's Joint values together
            pt.values[idx] = point1.values[idx] + point2.values[idx];
          } else {
            // Subtract the second point's Joint values from those of the first
            pt.values[idx] = point1.values[idx] - point2.values[idx];
          }
        }
        
        /* Update the Cartesian values of the point */
        float[] jointAngles = Arrays.copyOfRange(pt.values, 0, 5);
        PVector xyz = armModel.getEEPos( jointAngles );
        PVector wpr = armModel.getWPR( jointAngles );
        
        pt.values[6] = xyz.x;
        pt.values[7] = xyz.y;
        pt.values[8] = xyz.z;
        pt.values[9] = wpr.x;
        pt.values[10] = wpr.y;
        pt.values[11] = wpr.z;
        
        return pt;
      case MUTLIPLY:
      case DIVIDE:
      case MODULUS:
      case INTDIVIDE:
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
        String prefix = null;
        
        // Position Register entries are stored as a tripleton integer array
        if (regIdx[2] >= 0) {
          int idx = regIdx[2] % 6;
          
          
          if (regIdx[1] % 2 == 0) {
            // local position register
            prefix = (regIdx[1] == 0) ? "Pc" : "Pj";
          } else {
            // global Position Register
            prefix = (regIdx[1] == 0) ? "PRc" : "PRj";
          }
          
          return String.format("%s[%d, %d]", prefix, regIdx[0], idx);
        } else {
          
          if (regIdx[1] % 2 == 0) {
            // local position register
            prefix = (regIdx[1] == 0) ? "Pc" : "Pj";
          } else {
            // global Position Register
            prefix = (regIdx[1] == 0) ? "PRc" : "PRj";
          }
          
          return String.format("%s[%d]", prefix, regIdx[0]);
        }
      }
    } else if (param instanceof Operator) {
      
      // Each operator has its own symbol
      switch ( (Operator)param ) {
        case PLUS:      return "+";
        case MINUS:     return "-";
        case MUTLIPLY:  return "*";
        case DIVIDE:    return "/";
        case MODULUS:   return "%";
        case INTDIVIDE: return "|";
        case PAR_OPEN:  return "(";
        case PAR_CLOSE: return ")";
      }
    }
    
    // Simply print the value for constants
    return param.toString();
  }
}
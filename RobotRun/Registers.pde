// Position Registers
private final PositionRegister[] GPOS_REG = new PositionRegister[100];
// Data Registers
private final DataRegister[] DREG = new DataRegister[100];
// IO Registers
private final IORegister[] IO_REG = new IORegister[2];


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

/* A simple class designed to hold the current states of the Robot's various End Effectors */
public class IORegister {
  public final EndEffector associatedEE;
  public int state;
  
  public IORegister(EndEffector EE) {
    associatedEE = EE;
    state = OFF;
  }
  
  public IORegister(EndEffector EE, int init) {
    associatedEE = EE;
    state = init;
  }
}

public class ExprOperand {
  //type: 0 = numeric operand, 1 = boolean operand
  //      2 = data reg operand, 3 = IO reg operand
  //      4 = position reg operand, -1 = sub-expression
  //      -2 = uninit
  protected int type;
  protected int len;
  
  int regIndex;
  float dataVal;
  boolean boolVal;
  
  public ExprOperand() {
    type = -2;
    len = 1;
    regIndex = -1;
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
  
  public ExprOperand set(float d) {
    type = 0;
    len = 1;
    regIndex = -1;
    dataVal = d;
    boolVal = getBoolVal(dataVal);
    return this;
  }
  
  public ExprOperand set(boolean b) {
    type = 1;
    len = 1;
    regIndex = -1;
    dataVal = b ? 1 : 0;
    boolVal = b;
    return this;
  }
  
  public ExprOperand set(DataRegister dReg, int i) {
    type = 2;
    len = 2;
    regIndex = i;
    if(i != -1 && dReg.value != null) {
      dataVal = dReg.value;
      boolVal = getBoolVal(dataVal);
    }
    return this;
  }
  
  public ExprOperand set(IORegister ioReg, int i) {
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
    
    return this;
  }
  
  public ExprOperand set(PositionRegister pReg, int i){
    type = 4;
    len = 2;
    regIndex = i;
    return this;
  }
  
  public int getLength() {
    return len;
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

public class Expression extends AtomicExpression {
  private ArrayList<ExprOperand> operands;
  private ArrayList<Operator> opList;
  
  public Expression() {
    operands = new ArrayList<ExprOperand>();
    opList = new ArrayList<Operator>();
  }
  
  public ExprOperand evaluate() throws ExpressionEvaluationException {
    if(operands.size() - opList.size() != 1) {
      return null;
    }
    
    ExprOperand result = operands.get(0);
    
    for(int i = 0; i < opList.size(); i += 1) {
      ExprOperand nextOperand = operands.get(i + 1);
      AtomicExpression expr = new AtomicExpression(opList.get(i));
      
      result = expr.evaluate(result, nextOperand);
    }
    
    return result;
  }
  
  public String toString() {
    String ret = "(" + operands.get(0).toString();
    for(int i = 0; i < opList.size(); i += 1) {
      ret += opList.get(i).toString();
      ret += operands.get(i + 1).toString();
    }
    
    ret += ")";
    return ret;
  }
}

public class AtomicExpression extends ExprOperand {
  protected ExprOperand arg1;
  protected ExprOperand arg2;
  protected Operator op;
    
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
  
  public Operator getOp() { return op; }
  public void setOp(Operator o) {
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
  
  public void setOp(Operator o) {
    if(o.type != BOOL) return;
    
    op = o;
    len = getLength();
  }
}

public class ArithmeticExpression extends AtomicExpression{
  public ArithmeticExpression() {
    super();
  }
  
  public ArithmeticExpression(Operator o) {
    if(o.type == ARITH) {
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
  
  public void setOp(Operator o) {
    if(o.type != ARITH) return;
    
    op = o;
    len = getLength();
  }
}
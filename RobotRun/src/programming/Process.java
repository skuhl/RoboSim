package programming;

import enums.ExecState;
import enums.ExecType;

/**
 * Defines the state of a program's execution.
 * 
 * @author Joshua Hooker
 */
public class Process implements Cloneable {
	
	private int curIdx;
	private int nextIdx;
	private Program prog;
	private int rid;
	private ExecState state;
	private ExecType type;
	
	/**
	 * Initializes all the fields. Not a valid execution state!
	 */
	public Process() {
		rid = -1;
		prog = null;
		type = ExecType.EXEC_FULL;
		state = ExecState.EXEC_DONE;
		curIdx = 0;
		nextIdx = -1;
	}
	
	/**
	 * Define a program execution with all its fields. Used for cloning.
	 * 
	 * @param rid
	 * @param progIdx
	 * @param type
	 * @param state
	 * @param curIdx
	 * @param nextIdx
	 */
	private Process(int rid, Program prog, ExecType type, ExecState state,
			int curIdx, int nextIdx) {
		
		this.prog = prog;
		this.type = type;
		this.state = state;
		this.curIdx = curIdx;
		this.nextIdx = nextIdx;
	}
	
	@Override
	public Process clone() {
		return new Process(rid, prog, type, state, curIdx, nextIdx);
	}	
	
	public int getCurIdx() {
		return curIdx;
	}
	
	public int getNextIdx() {
		return nextIdx;
	}
	
	public Program getProg() {
		return prog;
	}
	
	public int getRID() {
		return rid;
	}
	
	public ExecState getState() {
		return state;
	}
	
	public ExecType getType() {
		return type;
	}
	
	/**
	 * Stop program execution
	 */
	public void halt() {
		state = ExecState.EXEC_DONE;
	}
	
	/**
	 * @return	Has the program finished execution
	 */
	public boolean isDone() {
		return state == ExecState.EXEC_DONE || state == ExecState.EXEC_FAULT;
	}
	
	/**
	 * @return	Is program execution only running a single instruction
	 */
	public boolean isSingleExec() {
		return type == ExecType.EXEC_SINGLE || type == ExecType.EXEC_BWD;
	}
	
	public void setCurIdx(int idx) {
		curIdx = idx;
	}
	
	public void setExec(int rid, ExecType type, Program prog, int curIdx) {
		this.rid = rid;
		this.type = type;
		this.state = ExecState.EXEC_START;
		this.prog = prog;
		this.curIdx = curIdx;
		
		if (type == ExecType.EXEC_BWD) {
			nextIdx = curIdx;
			
		} else {
			nextIdx = curIdx + 1;
		}
	}
	
	public void setNextIdx(int idx) {
		nextIdx = idx;
	}
	
	public void setProg(Program prog) {
		this.prog = prog;
	}
	
	public void setState(ExecState newState) {
		state = newState;
	}
	
	public void setType(ExecType type) {
		this.type = type;
		this.state = ExecState.EXEC_START;
	}
}

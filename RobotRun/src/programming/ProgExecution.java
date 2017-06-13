package programming;

import enums.ExecState;
import enums.ExecType;

/**
 * Defines the state of a program's execution.
 * 
 * @author Joshua Hooker
 */
public class ProgExecution implements Cloneable {
	
	private int rid;
	private int progIdx;
	private ExecType type;
	private ExecState state;
	private int curIdx;
	private int nextIdx;
	
	/**
	 * Initializes all the fields. Not a valid execution state!
	 */
	public ProgExecution() {
		rid = -1;
		progIdx = -1;
		type = ExecType.EXEC_FULL;
		state = ExecState.EXEC_DONE;
		curIdx = 0;
		nextIdx = -1;
	}
	
	public ProgExecution(int rid, int progIdx, ExecType type, int curIdx) {
		
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
	private ProgExecution(int rid, int progIdx, ExecType type, ExecState state,
			int curIdx, int nextIdx) {
		
		this.progIdx = progIdx;
		this.type = type;
		this.state = state;
		this.curIdx = curIdx;
		this.nextIdx = nextIdx;
	}
	
	@Override
	public ProgExecution clone() {
		return new ProgExecution(rid, progIdx, type, state, curIdx, nextIdx);
	}	
	
	public int getCurIdx() {
		return curIdx;
	}
	
	public int getProgIdx() {
		return progIdx;
	}
	
	public int getNextIdx() {
		return nextIdx;
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
	
	public void setExec(ExecType type) {
		this.type = type;
		this.state = ExecState.EXEC_START;
	}
	
	public void setExec(int rid, ExecType type, int progIdx, int curIdx) {
		this.rid = rid;
		this.type = type;
		this.state = ExecState.EXEC_START;
		this.progIdx = progIdx;
		this.curIdx = curIdx;
		nextIdx = curIdx + 1;
	}
	
	public void setNextIdx(int idx) {
		nextIdx = idx;
	}
	
	public void setProgIdx(int idx) {
		progIdx = idx;
	}
	
	public void setState(ExecState newState) {
		state = newState;
	}
}

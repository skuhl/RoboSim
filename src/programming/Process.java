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
	public Process(int rid, Program prog, ExecType type, ExecState state,
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
	
	public void setCurIdx(int idx) {
		curIdx = idx;
	}
	
	public void setNextIdx(int idx) {
		nextIdx = idx;
	}
	
	public void setProg(Program prog) {
		this.prog = prog;
	}
	
	public void setRID(int rid) {
		this.rid = rid;
	}
	
	public void setState(ExecState newState) {
		state = newState;
	}
	
	public void setType(ExecType type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		if (prog == null) {
			return String.format("type=%s rid=%d prog=n/a cur=%d state=%s next=%d",
					type, rid, curIdx, state, nextIdx);
		}
		
		return String.format("type=%s rid=%d prog=%s cur=%d state=%s next=%d",
				type, rid, prog.getName(), curIdx, state, nextIdx);
	}
}

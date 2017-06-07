package programming;

import enums.ExecState;
import enums.ExecType;

/**
 * Defines the state of a program's execution.
 * 
 * @author Joshua Hooker
 */
public class ProgExecution {
	
	public Program prog;
	
	private ExecType type;
	private ExecState state;
	private int curIdx;
	private int nextIdx;
	
	/**
	 * Initializes all the fields. Not a valid execution state!
	 */
	public ProgExecution() {
		type = ExecType.EXEC_FULL;
		state = ExecState.EXEC_DONE;
		prog = null;
		curIdx = 0;
		nextIdx = -1;
	}
	
	/**
	 * Initializes fields for beginning of program execution.
	 * 
	 * @param type		The type of program execution (i.e. single, all, etc.)
	 * @param prog		The program to execute
	 * @param curIdx	The index of the instruction to begin execution
	 */
	public ProgExecution(ExecType type, Program prog, int curIdx) {
		setExec(type, prog, curIdx);
	}
	
	public int getCurIdx() {
		return curIdx;
	}
	
	public int getNextIdx() {
		return nextIdx;
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
	
	/**
	 * TODO comment this
	 * 
	 * @param type
	 * @param prog
	 * @param curIdx
	 */
	public void setExec(ExecType type, Program prog, int curIdx) {
		this.type = type;
		this.prog = prog;
		this.curIdx = curIdx;
		
		if (curIdx < 0 || curIdx >= prog.size()) {
			this.state = ExecState.EXEC_FAULT;
			nextIdx = -1;
			
		} else {
			this.state = ExecState.EXEC_INST;
			nextIdx = curIdx + 1;
		}
	}
	
	public void setNextIdx(int idx) {
		nextIdx = idx;
	}
	
	public void setState(ExecState newState) {
		state = newState;
	}
	
	public void updateCurIdx() {
		// Wait until the motion instruction is complete
		if (state != ExecState.EXEC_DONE && state != ExecState.EXEC_FAULT &&
				state != ExecState.EXEC_MINST) {
			
			 if (nextIdx < 0 || nextIdx > prog.size()) {
				// Encountered a fault in program execution
				state = ExecState.EXEC_FAULT;
				
			} else {
				curIdx = nextIdx;
				
				if (isSingleExec() || nextIdx == prog.size()) {
					// Reached the end of execution
					state = ExecState.EXEC_DONE;
					
				}
			}
		}
	}
}

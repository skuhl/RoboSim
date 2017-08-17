package core;

import java.util.Stack;

import enums.ExecState;
import enums.ExecType;
import programming.Process;
import programming.Program;

/**
 * TODO general comments
 * 
 * @author Joshua Hooker
 */
public class ProgramExecution {
	
	private Process activeProc;
	private Stack<Process> procCallStack;
	
	/**
	 * Initializes an empty process and call stack.
	 */
	public ProgramExecution() {
		activeProc = new Process();
		procCallStack = new Stack<>();
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param rid
	 * @param prog
	 */
	public void callProgram(int rid, Program prog) {
		procCallStack.push(activeProc.clone());
		
		activeProc.setRID(rid);
		activeProc.setProg(prog);
		activeProc.setCurIdx(0);
	}
	
	/**
	 * TODO comment this
	 */
	public void clearCallStack() {
		procCallStack.clear();
	}
	
	/**
	 * TODO comment this
	 * 
	 * @return
	 */
	public Program getProg() {
		return activeProc.getProg();
	}
	
	/**
	 * TODO comment this
	 * 
	 * @return
	 */
	public int getProcRID() {
		return activeProc.getRID();
	}
	
	/**
	 * TODO comment this
	 * 
	 * @return
	 */
	public int getProcCurIdx() {
		return activeProc.getCurIdx();
	}
	
	/**
	 * TODO comment this
	 * 
	 * @return
	 */
	public int getProcNextIdx() {
		return activeProc.getNextIdx();
	}
	
	/**
	 * TODO comment this
	 * 
	 * @return
	 */
	public ExecState getProcState() {
		return activeProc.getState();
	}
	
	/**
	 * TODO comment this
	 * 
	 * @return
	 */
	public ExecType getProcType() {
		return activeProc.getType();
	}
	
	/**
	 * TODO comment this
	 */
	public void halt() {
		activeProc.setState(ExecState.EXEC_DONE);
	}
	
	/**
	 * TODO comment this
	 * 
	 * @return
	 */
	public boolean isCallStackEmpty() {
		return procCallStack.isEmpty();
	}
	
	/**
	 * TODO comment this
	 * 
	 * @return
	 */
	public boolean isDone() {
		ExecState curState = activeProc.getState();
		return curState == ExecState.EXEC_DONE || curState == ExecState.EXEC_FAULT;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @return
	 */
	public boolean isSingleExec() {
		ExecType type = activeProc.getType();
		return type == ExecType.EXEC_SINGLE || type == ExecType.EXEC_BWD;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param singleExec
	 */
	public void progExec(boolean singleExec) {
		ExecType pExec = (singleExec) ? ExecType.EXEC_SINGLE
				: ExecType.EXEC_FULL;
		activeProc.setType(pExec);
		execStart();
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param instIdx
	 * @param singleExec
	 * @return
	 */
	public boolean progExec(int instIdx, boolean singleExec) {
		ExecType pExec = (singleExec) ? ExecType.EXEC_SINGLE
				: ExecType.EXEC_FULL;
		return progExec(instIdx, pExec);
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param instIdx
	 * @param type
	 * @return
	 */
	public boolean progExec(int instIdx, ExecType type) {
		Program p = activeProc.getProg();
		
		if (p != null && instIdx >= 0 && instIdx <= p.getNumOfInst()) {
			
			if (type != null) {
				activeProc.setType(type);
			}
			
			activeProc.setCurIdx(instIdx);
			execStart();
			return true;
		}
		
		return false;
	}
	
	/**
	 * TODO comment this
	 */
	public void procReturn() {
		activeProc = procCallStack.pop();
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param prog
	 */
	public void setProg(Program prog) {
		clearCallStack();
		activeProc.setProg(prog);
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param RID
	 */
	public void setProcRID(int RID) {
		clearCallStack();
		activeProc.setRID(RID);
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param instIdx
	 * @return
	 */
	public boolean setProcCurIdx(int instIdx) {
		Program p = activeProc.getProg();
		
		if (p != null && instIdx >= 0 && instIdx <= p.getNumOfInst()) {
			activeProc.setCurIdx(instIdx);
			return true;
		}
		
		return false;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param nextIdx
	 * @return
	 */
	public void setProcNextIdx(int nextIdx) {
		activeProc.setNextIdx(nextIdx);
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param newState
	 */
	public void setProcState(ExecState newState) {
		activeProc.setState(newState);
	}
	
	/**
	 * TODO comment this
	 */
	private void execStart() {
		activeProc.setState(ExecState.EXEC_START);
		
		if (activeProc.getType() == ExecType.EXEC_BWD) {
			activeProc.setNextIdx(activeProc.getCurIdx());
			
		} else {
			activeProc.setNextIdx(activeProc.getCurIdx() + 1);
		}
	}
}

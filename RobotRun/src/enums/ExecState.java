package enums;

/**
 * Defines the states of program execution.
 * 
 * @author Joshua Hooker
 */
public enum ExecState {
	EXEC_START, EXEC_INST, EXEC_MINST, EXEC_NEXT, EXEC_CALL, EXEC_DONE,
	EXEC_FAULT;
}

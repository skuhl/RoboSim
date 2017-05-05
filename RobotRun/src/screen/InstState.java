package screen;

import programming.Instruction;
import programming.Program;

/**
 * A data class for storing states of a instructions to accommodate the undo
 * feature in program editing.
 * 
 * @author Joshua Hooker
 */
public class InstState {
	
	public Program parent;
	public int originIdx;
	public Instruction inst;
	public InstUndo type;
	
	/**
	 * Initialize the instruction state with an instruction and the type of
	 * operation performed on the instruction.
	 * 
	 * @param p		The program, to which the instruction belongs
	 * @param odx	The index of the instruction in the parent program
	 * @param i		The instruction state
	 * @param t		The type of operation performed
	 */
	public InstState(Program p, int odx, Instruction i, InstUndo t) {
		parent = p;
		originIdx = odx;
		inst = i;
		type = t;
	}
	
}

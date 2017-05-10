package screen;

import programming.Instruction;

/**
 * A data class for storing states of a instructions to accommodate the undo
 * feature in program editing.
 * 
 * @author Joshua Hooker
 */
public class InstState {
	
	public int originIdx;
	public Instruction inst;
	
	/**
	 * Initialize the instruction state with an instruction.
	 * 
	 * @param odx	The index of the instruction in the parent program
	 * @param i		The instruction state
	 */
	public InstState(int odx, Instruction i) {
		originIdx = odx;
		inst = i;
	}
	
}

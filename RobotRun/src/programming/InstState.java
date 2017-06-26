package programming;

import enums.InstOp;

/**
 * A data class for storing states of a instructions to accommodate the undo
 * feature in program editing.
 * 
 * @author Joshua Hooker
 */
public class InstState {
	
	public InstOp operation;
	public int originIdx;
	public Instruction inst;
	
	/**
	 * Initialize the instruction state with an instruction.
	 * 
	 * @param op	The operation performed on the instruction
	 * @param odx	The index of the instruction in the parent program
	 * @param i		The instruction state
	 */
	public InstState(InstOp op, int odx, Instruction i) {
		operation = op;
		originIdx = odx;
		inst = i;
	}
	
	@Override
	public String toString() {
		return String.format("op=%s odx=%d inst=%s", operation, originIdx,
				inst);
	}
	
}

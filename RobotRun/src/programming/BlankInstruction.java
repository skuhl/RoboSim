package programming;

/**
 * Defines an uninitialized instruction in a program.
 * 
 * @author Joshua Hooker
 */
public class BlankInstruction extends Instruction {
	
	/**
	 * Initializes a blank instruction, which is not commented.
	 */
	public BlankInstruction() {
		super(false);
	}
	
	/**
	 * Initializes a blank instruction with the given comment state.
	 * 
	 * @param isComm The commented state of this instruction
	 */
	public BlankInstruction(boolean isComm) {
		super(isComm);
	}
	
	/**
	 * Create an independent replica of this instruction.
	 */
	@Override
	public Instruction clone() {
		return new BlankInstruction(isCommented);
	}
	
	@Override
	public String[] toStringArray() {
		return new String[] {"..."};
	}
}

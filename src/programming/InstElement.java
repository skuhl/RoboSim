package programming;

/**
 * Relates an instruction to an ID value. A program has a list of InstElements,
 * so that each instruction with a program has a unique ID, which will not change
 * during the runtime of the RobotRun application.
 * 
 * @author Joshua Hooker
 */
public class InstElement {
	
	/**
	 * The ID associated with this element.
	 */
	private int ID;
	
	/**
	 * The instruction associated with this element.
	 */
	private Instruction inst;
	
	public InstElement(int id, Instruction inst) {
		this.ID = id;
		this.inst = inst;
	}
	
	/**
	 * @return	The ID associated with this element
	 */
	public int getID() {
		return ID;
	}
	
	/**
	 * @return	The instruction associated with this element
	 */
	public Instruction getInst() {
		return inst;
	}
	
	/**
	 * Sets the ID and instruction of this element.
	 * 
	 * @param id	The new ID associated with this element
	 * @param inst	The new instruction associated with this element
	 */
	protected void setElement(int id, Instruction inst) {
		this.ID = id;
		this.inst = inst;
	}
}

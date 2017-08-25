package programming;

import enums.InstUndoType;

/**
 * TODO general comments
 * 
 * @author Joshua Hooker
 */
public class InstUndoState {
	private InstElement elemRef;
	private int groupNum;
	private int idx;
	private Program parent;
	private InstUndoType type;
	
	public InstUndoState(InstUndoType type, int groupNum, Program parent,
			int idx, InstElement ref) {
		
		this.type = type;
		this.groupNum = groupNum;
		this.parent = parent;
		this.idx = idx;
		this.elemRef = ref;
		
	}
	
	public int groupNum() {
		return groupNum;
	}
	
	@Override
	public String toString() {
		return String.format("%s %d %s %d %s %d", type.name(), groupNum,
				parent.getName(), idx, elemRef.getInst().getClass(),
				elemRef.getID());
	}
	
	/**
	 * Reverts the state of the instruction element associated with this undo
	 * state.
	 */
	public void undo() {
		if (type == InstUndoType.EDITED || type == InstUndoType.REPLACED) {
			// Undo an edit or replacement
			parent.replace(idx, elemRef);
			
		} else if (type == InstUndoType.INSERTED) {
			// Undo an insertion
			parent.rmInst(elemRef.getID());
			
		} else if (type == InstUndoType.REMOVED) {
			// Undo a deletion
			parent.addAt(idx, elemRef);
		}
	}
	
}

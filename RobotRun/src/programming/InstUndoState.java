package programming;

import enums.InstUndoType;

public class InstUndoState {
	private InstElement elemRef;
	private int groupID;
	
	private int idx;
	
	private Program parent;
	private InstUndoType type;
	
	public InstUndoState(InstUndoType type, int groupID, Program parent,
			int idx, InstElement ref) {
		
		this.type = type;
		this.groupID = groupID;
		this.parent = parent;
		this.idx = idx;
		this.elemRef = ref;
		
	}
	
	public int getGID() {
		return groupID;
	}
	
	@Override
	public String toString() {
		return String.format("%s %d %s %d %s %d", type.name(), groupID,
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

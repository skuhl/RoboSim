package undo;

import enums.InstUndoType;
import programming.Instruction;
import programming.Program;

public class InstUndoState {
	private InstUndoType type;
	private int groupID;
	
	private Program parent;
	
	private Instruction instRef;
	private int ID;
	
	public InstUndoState(InstUndoType type, int groupID, Program parent,
			Instruction ref, int ID) {
		
		this.type = type;
		this.groupID = groupID;
		this.parent = parent;
		this.instRef = ref;
		this.ID = ID;
	}
	
	public int getGID() {
		return groupID;
	}
	
	/**
	 * Reverts the state of the instruction element associated with this undo
	 * state.
	 */
	public void undo() {
		if (type == InstUndoType.EDITED || type == InstUndoType.REPLACED) {
			// Undo an edit or replacement
			parent.replaceInstAt(ID, instRef);
			
		} else if (type == InstUndoType.INSERTED) {
			// Undo an insertion
			parent.rmInst(ID);
			
		} else if (type == InstUndoType.REMOVED) {
			// Undo a deletion
			parent.addInstAt(ID, instRef);
		}
		
	}
	
	@Override
	public String toString() {
		return String.format("%s %d %s %s %d", type.name(), groupID,
				parent.getName(), instRef.getClass(), ID);
	}
	
}

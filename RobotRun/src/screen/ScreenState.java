package screen;

/**
 * A save state for previous pendant screens.
 * 
 * @author Joshua Hooker
 */
public class ScreenState {
	
	public ScreenMode mode;
	public int conLnIdx, conColIdx, conRenIdx, optLnIdx, optRenIdx;
	
	public ScreenState(ScreenMode mode, int conLnIdx, int conColIdx,
			int colRenIdx, int optLnIdx, int optRenIdx) {
		
		this.mode = mode;
		this.conLnIdx = conLnIdx;
		this.conColIdx = conColIdx;
		this.conRenIdx = conRenIdx;
		this.optLnIdx = optLnIdx;
		this.optRenIdx = optRenIdx;
		
	}
	
	@Override
	public String toString() {
		return String.format("%s : [ cc=%d cl=%d cr=%d ol=%d or=%d ]",
				mode.name(), conLnIdx, conColIdx, conRenIdx, optLnIdx,
				optRenIdx);
	}
}

package frame;

public class FrameFile {
	public static final int FRAME_SIZE = 100;
	private static Frame[] U_FRAME = new UserFrame[FRAME_SIZE];
	private static Frame[] T_FRAME = new ToolFrame[FRAME_SIZE];
		
	public static Frame getTFrame(int idx) { return T_FRAME[idx]; }
	
	public static Frame[] getTFrameFile() { return T_FRAME; }
	public static Frame getUFrame(int idx) { return U_FRAME[idx]; }
	
	public static Frame[] getUFrameFile() { return U_FRAME; }
	public static void initFrameFile() {
		for(int n = 0; n < FRAME_SIZE; n += 1) {
			U_FRAME[n] = new UserFrame();
			T_FRAME[n] = new ToolFrame();
		}
	}
	
	public static Frame setTFrame(int idx, ToolFrame f) { return T_FRAME[idx] = f; }
	public static Frame setUFrame(int idx, UserFrame f) { return U_FRAME[idx] = f; }
}

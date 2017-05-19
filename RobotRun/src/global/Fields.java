package global;

import geom.RMatrix;
import processing.core.PFont;
import processing.core.PVector;

/**
 * TODO general comments
 * 
 * @author Vincent Druckte and Joshua Hooker
 */
public abstract class Fields {
	
	/**
	 * A flag used for displaying current debug output to standard out.
	 */
	public static final boolean DEBUG = true;
	
	/**
	 * The off state of an end effector
	 */
	public static final int OFF = 0;
	
	/**
	 * The on state of an end effector
	 */
	public static final int ON = 1;
	
	/**
	 * The maximum number of tool or user frames associated with a robot.
	 */
	public static final int FRAME_NUM = 10;
	
	/**
	 * The maximum number of data or position registers associated with a robot.
	 */
	public static final int DPREG_NUM = 100;
	
	/**
	 * The maximum number of I/O registers associated with a robot.
	 */
	public static final int IOREG_NUM = 5;
	
	/**
	 * The joint motion type of a motion instruction.
	 */
	public static final int MTYPE_JOINT = 0;
	
	/**
	 * The linear motion type of a motion instruction.
	 */
	public static final int MTYPE_LINEAR = 1;
	
	/**
	 * The circular motion type of a motion instruction.
	 */
	public static final int MTYPE_CIRCULAR = 2;
	
	/**
	 * The tool frame type of a motion instruction
	 */
	public static final int FTYPE_TOOL = 0;
	
	/**
	 * The user frame type of a motion instruction
	 */
	public static final int FTYPE_USER = 1;
	
	/**
	 * The 3x3 floating-point array representation of the identity matrix.
	 */
	public static final float[][] IDENTITY;
	
	/**
	 * The RMatrix representation of the identity matrix.
	 */
	public static final RMatrix IDENTITY_MAT;
	
	/**
	 * The orientation of the world frame with respect to the native coordinate
	 * system.
	 */
	public static final float[][] WORLD_AXES;
	
	/**
	 * The RMatrix representation of the world frame orientation.
	 */
	public static final RMatrix WORLD_AXES_MAT;
	
	/**
	 * The inverse of the world frame orientation, or the native coordinate
	 * system orientation in terms of the world frame.
	 */
	public static final float[][] NATIVE_AXES;
	
	/**
	 * The RMatrix representation of the inverse world frame orientation.
	 */
	public static final RMatrix NATIVE_AXES_MAT;
	
	public static final int SMALL_BUTTON = 35;
	public static final int LARGE_BUTTON = 50;
	public static final int CHAR_WDTH = 8;
	public static final int TXT_PAD = 18;
	public static final int PAD_OFFSET = 8;
	
	/**
	 * A dimension pertaining to the pendant or pendant screen UI elements.
	 */
	public static final int PENDANT_X = 0, 
							PENDANT_Y = SMALL_BUTTON - 14,
							PENDANT_WIDTH = 440, 
							PENDANT_HEIGHT = 720,
							PENDANT_SCREEN_WIDTH = PENDANT_WIDTH - 20,
							PENDANT_SCREEN_HEIGHT = 280;
	
	public static final int PASTE_DEFAULT = 0,
			PASTE_REVERSE = 0b1,
			CLEAR_POSITION = 0b10,
			NEW_POSITION = 0b100,
			REVERSE_MOTION = 0b1000;
	
	/**
	 * A color in the UI's color scheme.
	 */
	public static final int BG_C, F_TEXT_C, F_CURSOR_C, F_ACTIVE_C, F_BG_C,
			F_FG_C, B_TEXT_C, B_DEFAULT_C, B_ACTIVE_C, UI_LIGHT_C, UI_DARK_C;
	
	/**
	 * A font used for rendering text in the UI.
	 */
	public static PFont small, medium, bond;
	
	/**
	 * Initialize the static fields.
	 */
	static {
		IDENTITY = new float[][] {
			{ 1, 0, 0 },
			{ 0, 1, 0 },
			{ 0, 0, 1 }
		};
		
		WORLD_AXES = new float[][] {
			{ -1,  0,  0 },
			{  0,  0, -1 },
			{  0,  1,  0 }
			
		};
		
		NATIVE_AXES = new float[][] {
			{ -1,  0,  0 },
			{  0,  0,  1 },
			{  0, -1,  0 }
		};
		
		IDENTITY_MAT = new RMatrix(IDENTITY);
		WORLD_AXES_MAT = new RMatrix(WORLD_AXES);
		NATIVE_AXES_MAT = new RMatrix(NATIVE_AXES);
		
		BG_C = -2960686;
		F_TEXT_C = -16777216;
		F_CURSOR_C = -16777216;
		F_ACTIVE_C = -65536;
		F_BG_C = -1;
		F_FG_C = -16777216;
		B_TEXT_C = -1;
		B_DEFAULT_C = -12171706;
		B_ACTIVE_C = -2349016;
		UI_LIGHT_C = -986896;
		UI_DARK_C = -14145496;
		
		small = null;
		medium = null;
		bond = null;
	}
	
	/**
	 * Creates a 2-element a string array, whose entries are formatted String
	 * representations the given position and rotation.
	 *
	 * @param position	A 3D position vector
	 * @param rotation	A set of euler angles (W, P, R)
	 * @return  		A 2 element array [position, rotation]
	 */
	public static String[] toLineStringArray(PVector position, PVector rotation) {
		
		String strX = "X: " + MyFloatFormat.format(position.x);
		String strY = "Y: " + MyFloatFormat.format(position.y);
		String strZ = "Z: " + MyFloatFormat.format(position.z);
		String strW = "W: " + MyFloatFormat.format(rotation.x);
		String strP = "P: " + MyFloatFormat.format(rotation.y);
		String strR = "R: " + MyFloatFormat.format(rotation.z);
		
		return new String[] {
				String.format("%-12s %-12s %-12s", strX, strY, strZ),
				String.format("%-12s %-12s %-12s", strW, strP, strR)
		};
	}
	
	/**
	 * Calls System.out.printf(format, args), if the field, DEBUG, is true.
	 * 
	 * @param format	The format string
	 * @param args		The arguments to print to standard out
	 */
	public static void debug(String format, Object... args) {
		if (DEBUG) {
			System.out.printf(format, args);
		}
	}
	
	/**
	 * Calls System.out.println(out), if the field, DEBUG, is true.
	 * 
	 * @param out	The string to print to standard out
	 */
	public static void debug(String out) {
		if (DEBUG) {
			System.out.println(out);
		}
	}
}

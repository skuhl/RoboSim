package global;

import geom.RMatrix;
import processing.core.PFont;

public abstract class Fields {
	
	public static final boolean DEBUG = true;
	
	public static final int OFF = 0;
	public static final int ON = 1;
	
	public static final int FALSE = 0;
	public static final int TRUE = 1;
	
	/* The number of the user and tool frames, the number of the position and
	 * data registers, and the number of I/O registers */
	public static final int FRAME_NUM = 10;
	public static final int DPREG_NUM = 100;
	public static final int IOREG_NUM = 5;
	
	public static final int MTYPE_JOINT = 0;
	public static final int MTYPE_LINEAR = 1;
	public static final int MTYPE_CIRCULAR = 2;
	public static final int FTYPE_TOOL = 0;
	public static final int FTYPE_USER = 1;
	
	public static final float[][] IDENTITY = new float[][] {
		{ 1, 0, 0 },
		{ 0, 1, 0 },
		{ 0, 0, 1 }
	};
	
	public static final RMatrix IDENTITY_MAT = new RMatrix(IDENTITY);
	
	/**
	 * The rotation matrix representing the world coordinate system in terms of
	 * the native coordinate system.
	 */
	public static final float[][] WORLD_AXES = new float[][] {
		{ -1,  0,  0 },
		{  0,  0, -1 },
		{  0,  1,  0 }
		
	};
	
	public static final RMatrix WORLD_AXES_MAT = new RMatrix(WORLD_AXES); 
	
	/**
	 * The rotation matrix representing the native coordinate system in terms
	 * of the world coordinate system.
	 */
	public static final float[][] NATIVE_AXES = new float[][] {
		{ -1,  0,  0 },
		{  0,  0,  1 },
		{  0, -1,  0 }
	};
	
	public static final RMatrix NATIVE_AXES_MAT = new RMatrix(NATIVE_AXES);
	
	public static final int SMALL_BUTTON = 35;
	public static final int LARGE_BUTTON = 50;
	public static final int CHAR_WDTH = 8;
	public static final int TXT_PAD = 18;
	public static final int PAD_OFFSET = 8;
	
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
	public static final int BG_C = -2960686,
							F_TEXT_C = -16777216,
							F_CURSOR_C = -16777216,
							F_ACTIVE_C = -65536,
							F_BG_C = -1,
							F_FG_C = -16777216,
							B_TEXT_C = -1,
							B_DEFAULT_C = -12171706,
							B_ACTIVE_C = -2349016,
							UI_LIGHT_C = -986896,
							UI_DARK_C = -14145496;
	
	/**
	 * A font used for rendering text in the UI.
	 */
	public static PFont small, medium, bond;
}

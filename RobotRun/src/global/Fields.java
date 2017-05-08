package global;

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
	
	/**
	 * The rotation matrix representing the world axes orientation.
	 */
	public static final float[][] WORLD_AXES = new float[][] { { -1, 0,  0 },
															   {  0, 0,  1 },
															   {  0, -1, 0 } };
	
	public static final int SMALL_BUTTON = 35;
	public static final int LARGE_BUTTON = 50;
	public static final int CHAR_WDTH = 8;
	public static final int TXT_PAD = 18;
	public static final int PAD_OFFSET = 8;
	
	public static final int G1_PX = 0, 
			G1_PY = SMALL_BUTTON - 14, // the left-top corner of group 1
			G1_WIDTH = 440, 
			G1_HEIGHT = 720, // group 1's width and height
			DISPLAY_PX = 10,
			DISPLAY_PY = 0, // the left-top corner of display screen
			DISPLAY_WIDTH = G1_WIDTH - 20,
			DISPLAY_HEIGHT = 280; // height and width of display screen
	
	public static final int PASTE_DEFAULT = 0,
			PASTE_REVERSE = 0b1,
			CLEAR_POSITION = 0b10,
			NEW_POSITION = 0b100,
			REVERSE_MOTION = 0b1000;
	
	public static final int BUTTON_DEFAULT = -12171706,
			BUTTON_ACTIVE = -2349016,
			BUTTON_TEXT = -986896,
			UI_LIGHT = -986896,
			UI_DARK = -14145496;
	
	public static PFont small, medium, bond;
}

package global;

import processing.core.*;

public class Fields extends PApplet {
	public static final int OFF = 0;
	public static final int ON = 1;
	
	public static final int FALSE = 0;
	public static final int TRUE = 1;
	
	public static final int MTYPE_JOINT = 0;
	public static final int MTYPE_LINEAR = 1;
	public static final int MTYPE_CIRCULAR = 2;
	public static final int FTYPE_TOOL = 0;
	public static final int FTYPE_USER = 1;
	
	public static final int SMALL_BUTTON = 35;
	public static final int LARGE_BUTTON = 50;
	public static final int CHAR_WDTH = 8;
	public static final int TXT_PAD = 18;
	public static final int PAD_OFFSET = 8;
	
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
}

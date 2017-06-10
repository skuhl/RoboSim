package global;

import geom.CoordinateSystem;
import geom.RMatrix;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;
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
	 * The joint motion type of a motion instruction.
	 */
	public static final int MTYPE_JOINT = 0;
	
	/**
	 * The linear motion type of a motion instruction.
	 */
	public static final int MTYPE_LINEAR = 1;
	
	public static final int MREGTYPE_POS = 0;
	public static final int MREGTYPE_GPOS = 1;
	public static final int MREGTYPE_OBJ = 2;
	
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
	 * The y position of the floor of the world.
	 */
	public static final float FLOOR_Y;
	
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
	 * A color defining either the fill or outline color of a world object.
	 */
	public static final int BLACK, WHITE, RED, GREEN, BLUE, ORANGE, YELLOW,
			PINK, PURPLE, LT_BLUE, DK_GREEN;
	
	/**
	 * A color used to render the bounding box of a part that indicates the
	 * state the bounding box.
	 */
	public static final int OBB_DEFAULT, OBB_COLLISION, OBB_SELECTED, OBB_HELD;
	
	/**
	 * A color used in the UI's color scheme.
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
		FLOOR_Y = 300f;
		
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
		
		BLACK = 0xff000000;
		WHITE = 0xffffffff;
		RED = color(255, 0, 0);
		GREEN = color(0, 255, 0);
		BLUE = color(0, 0, 255);
		ORANGE = color(255, 60, 0);
		YELLOW = color(255, 255, 0);
		PINK = color(255, 0, 255);
		PURPLE = color(90, 0, 255);
		LT_BLUE = color(0, 255, 255);
		DK_GREEN = color(0, 100, 15);
		
		OBB_DEFAULT = GREEN;
		OBB_COLLISION = RED;
		OBB_SELECTED = ORANGE;
		OBB_HELD = BLUE;
		
		BG_C = 0xffd2d2d2;
		F_TEXT_C = BLACK;
		F_CURSOR_C = BLACK;
		F_ACTIVE_C = RED;
		F_BG_C = WHITE;
		F_FG_C = BLACK;
		B_TEXT_C = WHITE;
		B_DEFAULT_C = 0xff464646;
		B_ACTIVE_C = 0xffdc2828;
		UI_LIGHT_C = 0xfff0f0f0;
		UI_DARK_C = 0xff282828;
		
		small = null;
		medium = null;
		bond = null;
	}
	
	/**
	 * Applies the given coordinate system to the given graphics object.
	 * 
	 * @param g		The graphics object to transform
	 * @param cs	The coordinate system to apply to g
	 */
	public static void transform(PGraphics g, CoordinateSystem cs) {
		transform(g, cs.getOrigin(), cs.getAxes());
	}
	
	/**
	 * Applies the given rotation and translation to the graphics object.
	 * 
	 * @param g				The graphics object to transform
	 * @param translation	The translation to apply to g
	 * @param rotation		The rotation to apply to g
	 */
	public static void transform(PGraphics g, PVector translation, RMatrix rotation) {
		
		g.applyMatrix(
				(float)rotation.getEntry(0, 0), (float)rotation.getEntry(0, 1), (float)rotation.getEntry(0, 2), translation.x,
				(float)rotation.getEntry(1, 0), (float)rotation.getEntry(1, 1), (float)rotation.getEntry(1, 2), translation.y,
				(float)rotation.getEntry(2, 0), (float)rotation.getEntry(2, 1), (float)rotation.getEntry(2, 2), translation.z,
				0f, 0f, 0f, 1f
		);
		
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param g
	 * @param tMat
	 */
	public static void transform(PGraphics g, RMatrix tMat) {
		
		g.applyMatrix(
				(float)tMat.getEntry(0, 0), (float)tMat.getEntry(0, 1), (float)tMat.getEntry(0, 2), (float)tMat.getEntry(0, 3),
				(float)tMat.getEntry(1, 0), (float)tMat.getEntry(1, 1), (float)tMat.getEntry(1, 2), (float)tMat.getEntry(1, 3),
				(float)tMat.getEntry(2, 0), (float)tMat.getEntry(2, 1), (float)tMat.getEntry(2, 2), (float)tMat.getEntry(2, 3),
				(float)tMat.getEntry(3, 0), (float)tMat.getEntry(3, 1), (float)tMat.getEntry(3, 2), (float)tMat.getEntry(3, 3)
		);
		
	}
	
	/**
	 * Applies the given rotation to the graphics object.
	 * 
	 * @param g			The graphics object to rotate
	 * @param rotation	The rotation to apply to g
	 */
	public static void rotate(PGraphics g, RMatrix rotation) {
		
		g.applyMatrix(
				(float)rotation.getEntry(0, 0), (float)rotation.getEntry(0, 1), (float)rotation.getEntry(0, 2), 0f,
				(float)rotation.getEntry(1, 0), (float)rotation.getEntry(1, 1), (float)rotation.getEntry(1, 2), 0f,
				(float)rotation.getEntry(2, 0), (float)rotation.getEntry(2, 1), (float)rotation.getEntry(2, 2), 0f,
				0f, 0f, 0f, 1f
		);
		
	}
	
	/**
	 * Returns a 32-bit color from the value, which will be applied to the red,
	 * green, and blue byte values of the color.
	 * 
	 * @param rgb	The gray intensity value
	 * @return		A 32-bit color value
	 */
	public static int color(int rgb) {
		return color(255, rgb, rgb, rgb);
	}
	
	/**
	 * Creates a 32-bit color from the given red, green, and blue byte values.
	 * The alpha value is set to 255.
	 * 
	 * @param r	The red byte value of the color
	 * @param g	The green byte value of the color
	 * @param b	The blue byte value of the color
	 * @return	A 32-bit color value
	 */
	public static int color(int r, int g, int b) {
		return color(r, g, b, 255);
	}
	
	/**
	 * Creates a 32-bit color value from the given red, green, blue, and alpha
	 * byte values.
	 * 
	 * @param r	The red byte value of the color
	 * @param g	The green byte value of the color
	 * @param b	The blue byte value of the color
	 * @param a	The alpha byte value of the color
	 * @return	A 32-bit color value
	 */
	public static int color(int r, int g, int b, int a) {
		/*
		 * a	24 - 31
		 * r	16 - 23
		 * g	8 - 15
		 * b	0 - 7
		 */
		return (0xff000000 & (a << 24)) | (0xff0000 & (r << 16)) |
				(0xff00 & (g << 8)) | (0xff & b);
	}
	
	/**
	 * Calculates the distance between two 32-bit colors, c0 and c1, using the
	 * formula:
	 * 
	 * diff = ( (c0.alpha - c1.alpha) ^ 2 + (c0.red - c1.red) ^ 2 + (c0.green
	 * 			- c1.green) ^ 2 + (c0.blue - c1.blue) ^ 2 ) ^ 1/2
	 * 
	 * @param c0	A 32-bit color value
	 * @param c1	A 32-bit color value
	 * @return		The total difference between c0 and c1 in Euclidean space
	 */
	public static float colorDiff(int c0, int c1) {
		// Compute the rgba differences between c0 and c1
		int[] diffs = rgbaDiffs(c0,  c1);
		// Compute the square root of the sum of the rgba differences
		return (float) Math.sqrt(diffs[0] + diffs[1] + diffs[2] + diffs[3]);
	}
	
	/**
	 * TODO comments this
	 * 
	 * @param g
	 * @param origin
	 * @param axesVectors
	 * @param axesLength
	 * @param originColor
	 */
	public static void drawAxes(PGraphics g, PVector origin,
			RMatrix axesVectors, float axesLength, int originColor) {
		
		g.pushMatrix();
		// Transform to the reference frame defined by the axes vectors		
		Fields.transform(g, origin, axesVectors);
		drawAxes(g, axesLength, originColor);
		g.popMatrix();
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param axesLength	The length of the rendered axes
	 * @param originColor	The color of the point of origin of the axes
	 */
	public static void drawAxes(PGraphics g, float axesLength, int originColor) {
		g.pushStyle();
		
		// X axis
		g.stroke(255, 0, 0);
		g.line(-axesLength, 0, 0, axesLength, 0, 0);
		// Y axis
		g.stroke(0, 255, 0);
		g.line(0, -axesLength, 0, 0, axesLength, 0);
		// Z axis
		g.stroke(0, 0, 255);
		g.line(0, 0, -axesLength, 0, 0, axesLength);

		// Draw a sphere on the positive direction for each axis
		float dotPos = RMath.clamp(axesLength, 100f, 500f);
		g.textFont(Fields.bond, 18);

		g.stroke(originColor);
		g.fill(Fields.BLACK);
		
		g.pushMatrix();
		
		g.sphere(4);
		g.stroke(0);
		g.translate(dotPos, 0, 0);
		g.sphere(4);

		g.pushMatrix();
		g.rotateX(-PConstants.PI / 2f);
		g.rotateY(-PConstants.PI);
		g.text("X-axis", 0, 0, 0);
		g.popMatrix();

		g.translate(-dotPos, dotPos, 0);
		g.sphere(4);

		g.pushMatrix();
		g.rotateX(-PConstants.PI / 2f);
		g.rotateY(-PConstants.PI);
		g.text("Y-axis", 0, 0, 0);
		g.popMatrix();

		g.translate(0, -dotPos, dotPos);
		g.sphere(4);

		g.pushMatrix();
		g.rotateX(-PConstants.PI / 2f);
		g.rotateY(-PConstants.PI);
		g.text("Z-axis", 0, 0, 0);
		g.popMatrix();

		g.popMatrix();
		g.popStyle();
	}
	
	/**
	 * Calculates the square differences between the alpha, red, green, and
	 * blue byte values in the 32-bit colors, c0 and c1.
	 * 
	 * @param c0	A 32-bit color value
	 * @param c1	A 32-bit color value
	 * @return		A 4-element array containing the square differences of c0
	 * 				and c1's alpha, red, green, blue byte values.
	 */
	public static int[] rgbaDiffs(int c0, int c1) {
		int[] diffs = new int[4];
		// Separate each color into r, g, b, a portions
		int[] c1_rgba = rgba(c0);
		int[] c2_rgba = rgba(c1);
		
		// Calculate the square difference between the portions of c0 and c1
		for (int idx = 0; idx < diffs.length; ++idx) {
			int diff = c1_rgba[idx] - c2_rgba[idx];
			diffs[idx] = diff * diff;
		}
		
		return diffs;
	}
	
	/**
	 * Breaks up the given 32-bit color into the four parts of the color: red,
	 * green, blue, alpha.
	 * 
	 * @param color	A 32-bit color value
	 * @return		[red, green, blue, alpha]
	 */
	public static int[] rgba(int color) {
		int[] portions = new int[4];
		
		// alpha
		portions[0] = 0xff & (color >> 24);
		// red
		portions[1] = 0xff & (color >> 16);
		// green
		portions[2] = 0xff & (color >> 8);
		// blue
		portions[3] = 0xff & color;
		
		return portions;
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

package global;

/**
 * A wrapper class for formatting String representations of float-point values.
 * Specifically, format() adds space padding. 
 * 
 * @author Joshua Hooker
 */
public abstract class DebugFloatFormat {
	private static String format;
	private static int maxLen;
	
	static {
		format = "%4.3f";
		maxLen = 9;
	}
	
	/**
	 * Formats the floating-point value based on the defined format and
	 * maximum string length.
	 * 
	 * @param val	The value to format as a String
	 * @return		The formatted String
	 */
	public static String format(float val) {
		String formattedVal = String.format(format, val);
		
		if (formattedVal.length() < maxLen) {
			// Add space padding before the number
			int padding = maxLen - formattedVal.length();
			
			while (--padding >= 0) {
				formattedVal = " " + formattedVal;
			}
		}
		
		return formattedVal;
	}
}

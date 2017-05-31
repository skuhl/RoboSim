package robot;

/**
 * A class that wraps a float, so you can reference the same memory space from
 * several references AND modify the value without modifying the reference.
 * 
 * @author Joshua Hooker
 */
public class FloatWrapper {
	
	public float value;
	
	public FloatWrapper(float value) {
		 this.value = value;
	}
}

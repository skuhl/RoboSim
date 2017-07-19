package core;

/**
 * A pointer to an object reference.
 * 
 * @author Joshua Hooker
 * @param <T>	The type of object referenced by this pointer
 */
public class Pointer<T> {
	
	private T val;
	
	public Pointer(T val) {
		this.val = val;
	}
	
	public T get() {
		return val;
	}
	
	public void set(T newVal) {
		val = newVal;
	}
}

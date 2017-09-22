package frame;

/**
 * A basic link between Tool and User frames.
 * 
 * @author Joshua Hooker
 */
public interface RFrame {
	
	/**
	 * Returns the name associated with this frame
	 * 
	 * @return	This frame's name
	 */
	public abstract String getName();
	
	/**
	 * Updates the name of this frame.
	 * 
	 * @param name	The new name for the frame
	 */
	public abstract void setName(String name);
	
	/**
	 * Reinitializes ALL the frame's fields to their default values.
	 */
	public abstract void reset();
	
	/**
	 * 
	 * @see ToolFrame#toLineStringArray()
	 * @see UserFrame#toLineStringArray()
	 */
	public abstract String[] toLineStringArray();
	
	/**
	 * 
	 * @see ToolFrame#toStringArray()
	 * @see UserFrame#toStringArray()
	 */
	public abstract String[] toStringArray();
}

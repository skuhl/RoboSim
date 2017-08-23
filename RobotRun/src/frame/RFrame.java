package frame;

/**
 * A basic link between Tool and User frames.
 * 
 * @author Joshua Hooker
 */
public interface RFrame {
	
	/**
	 * 
	 * @see ToolFrame#getName()
	 * @see UserFrame#getName()
	 */
	public abstract String getName();
	
	/**
	 * 
	 * @see ToolFrame#setName(String)
	 * @see UserFrame#setName(String)
	 */
	public abstract void setName(String name);
	
	/**
	 * 
	 * @see ToolFrame#reset()
	 * @see UserFrame#reset()
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

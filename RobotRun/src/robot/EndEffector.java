package robot;

import geom.BoundingBox;
import geom.MyPShape;
import geom.Part;
import regs.IORegister;

/**
 * Defines an end effector for a robotic arm.
 * 
 * @author Joshua Hooker
 */
public class EndEffector extends RSegment {
	
	/**
	 * The set of bounding boxes used to determine if this end effector can
	 * pickup an object.
	 */
	protected final BoundingBox[] PICKUP_OBBS;
	
	/**
	 * The I/O register associated with this end effector.
	 */
	protected final IORegister reg;
	
	/**
	 * TODO comment this
	 * 
	 * @param model
	 * @param obbs
	 * @param drawActions
	 * @param pickupOBBs
	 * @param idx
	 * @param name
	 */
	public EndEffector(MyPShape model, BoundingBox[] obbs,
			DrawAction[] drawActions, BoundingBox[] pickupOBBs,	int idx,
			String name) {
		
		super(model, obbs, drawActions);
		
		PICKUP_OBBS = pickupOBBs;
		reg = new IORegister(idx, name);
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param modelSet
	 * @param obbs
	 * @param drawActions
	 * @param pickupOBBs
	 * @param idx
	 * @param name
	 */
	public EndEffector(MyPShape[] modelSet, BoundingBox[] obbs,
			DrawAction[] drawActions, BoundingBox[] pickupOBBs,	int idx,
			String name) {
		
		super(modelSet, obbs, drawActions);
		
		PICKUP_OBBS = pickupOBBs;
		reg = new IORegister(idx, name);
	}
	
	/**
	 * @return	Can this end effector pickup parts?
	 */
	public boolean canPickup() {
		return PICKUP_OBBS.length > 0;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param p
	 * @return
	 */
	public boolean canPickup(Part p) {
		int ret = super.checkCollision(p);
		
		if (ret == 0) {
			for (BoundingBox obb : PICKUP_OBBS) {
				// Pickup collision
				if (p.collision(obb)) {
					return true;
				}
				
			}
		}
		
		// No collision
		return false;
	}
	
	public int getIdx() {
		return reg.idx;
	}
	
	public IORegister getIORegister() {
		return reg;
	}
	
	public String getName() {
		return reg.comment;
	}
	
	public int getState() {
		return reg.state;
	}
	
	public void setState(int newState) {
		reg.state = newState;
	}
	
}

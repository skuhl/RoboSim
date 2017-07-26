package robot;

import geom.BoundingBox;
import geom.Part;
import global.Fields;
import processing.core.PShape;
import regs.IORegTrace;
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
	
	public EndEffector(PShape model, BoundingBox[] obbs,
			BoundingBox[] pickupOBBs, int idx, String name) {
		
		super(model, obbs);
		
		PICKUP_OBBS = pickupOBBs;
		reg = new IORegister(idx, name);
		
		// Set pickup OBB colors
		for (BoundingBox obb : PICKUP_OBBS) {
			obb.setColor(Fields.OBB_HELD);
		}
	}
	
	public EndEffector(PShape model, BoundingBox[] obbs,
			BoundingBox[] pickupOBBs, int idx, String name, RTrace robotTrace) {
		
		super(model, obbs);
		
		PICKUP_OBBS = pickupOBBs;
		reg = new IORegTrace(idx, name, robotTrace);
		
		// Set pickup OBB colors
		for (BoundingBox obb : PICKUP_OBBS) {
			obb.setColor(Fields.OBB_HELD);
		}
	}
	
	public EndEffector(PShape[] modelSet, BoundingBox[] obbs,
			BoundingBox[] pickupOBBs, int idx, String name) {
		
		super(modelSet, obbs);
		
		PICKUP_OBBS = pickupOBBs;
		reg = new IORegister(idx, name);
		
		// Set pickup OBB colors
		for (BoundingBox obb : PICKUP_OBBS) {
			obb.setColor(Fields.OBB_HELD);
		}
	}
	
	public EndEffector(PShape[] modelSet, BoundingBox[] obbs,
			BoundingBox[] pickupOBBs, int idx, String name, RTrace robotTrace) {
		
		super(modelSet, obbs);
		
		PICKUP_OBBS = pickupOBBs;
		reg = new IORegTrace(idx, name, robotTrace);
		
		// Set pickup OBB colors
		for (BoundingBox obb : PICKUP_OBBS) {
			obb.setColor(Fields.OBB_HELD);
		}
	}
	
	/**
	 * @return	Can this end effector pickup parts?
	 */
	public boolean canPickup() {
		return PICKUP_OBBS.length > 0;
	}
	
	/**
	 * Determines if the given part's bounding boxes collide with at least one
	 * of the pickup bounding boxes of this end effector. If there is a
	 * collision detected, then true is returned, otherwise false is returned.
	 * 
	 * @param p	The part with which to check collisions
	 * @return	Whether a collision is detected between this end effector's
	 * 			pickup bounding boxes and the given part's bounding box.
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
	
	public boolean getState() {
		return reg.getState();
	}
	
	public void setState(boolean newState) {
		reg.setState(newState);
	}
	
}

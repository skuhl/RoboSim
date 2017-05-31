package robot;

import enums.GLogic;
import geom.BoundingBox;
import geom.MyPShape;
import geom.Part;
import geom.RRay;
import global.Fields;
import processing.core.PGraphics;
import processing.core.PVector;

/**
 * Defines a segment of the robotic arm with its bounding boxes and model set.
 * 
 * @author Joshua Hooker
 */
public class RSegment {
	
	/**
	 * The bounding boxes associated with this segment.
	 */
	protected final BoundingBox[] OBBS;
	
	/**
	 * The models defining the shape of this segment.
	 */
	protected final MyPShape[] MODEL_SET;
	
	/**
	 * The bounding boxes associated with this segment.
	 */
	private final DrawAction[] DRAW_ACTIONS;
	
	/**
	 * Defines a segment with the given model, bounding boxes, and draw
	 * actions.
	 * 
	 * @param model			The model defining the shape of this segment
	 * @param obbs			The bounding boxes associated with this segment
	 * @param drawActions	The bounding boxes associated with this segment
	 */
	public RSegment(MyPShape model, BoundingBox[] obbs,
			DrawAction[] drawActions) {
		
		OBBS = obbs;
		DRAW_ACTIONS = drawActions;
		
		this.MODEL_SET = new MyPShape[] { model };
	}
	
	/**
	 * Defines a segment with the given model set, bounding boxes, and draw
	 * actions.
	 * 
	 * @param MODEL			The models defining the shape of this segment
	 * @param obbs			The bounding boxes associated with this segment
	 * @param drawActions	The bounding boxes associated with this segment
	 */
	public RSegment(MyPShape[] modelSet, BoundingBox[] obbs,
			DrawAction[] drawActions) {
		
		OBBS = obbs;
		DRAW_ACTIONS = drawActions;
		
		this.MODEL_SET = modelSet;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param p
	 * @return
	 */
	public int checkCollision(Part p) {
		int ret = 0;
		
		for (BoundingBox obb : OBBS) {
			// set the color of the bounding box
			if (p.collision(obb)) {
				obb.setColor(Fields.OBB_COLLISION);
				ret = 1;
			}
			
		}
		
		return ret;
	}
	
	/**
	 * TODO comment this
	 * 
	 * @param ray
	 * @return
	 */
	public PVector closestCollision(RRay ray) {
		PVector closestCollPt = null;
		
		for (BoundingBox obb : OBBS) {
			PVector collPt = obb.collision(ray);
			
			if (collPt != null && (closestCollPt == null ||
					PVector.dist(ray.getOrigin(), collPt) <
					PVector.dist(ray.getOrigin(), closestCollPt))) {
				
				// Find the closest collision to the ray origin
				closestCollPt = collPt;
			}
		}
		
		return closestCollPt;
	}
	
	/**
	 * Executes the list of draw actions associated with this segment.
	 * 
	 * @param g			The graphics object used to draw this segment
	 * @param drawOBBs	Whether to draw the segment's bounding boxes or not
	 */
	public void draw(PGraphics g, boolean drawOBBs) {
		/* TODO REMOVE AFTER REFACTOR */
		g.shape(MODEL_SET[0]);
		
		/**
		for (DrawAction action : DRAW_ACTIONS) {
			executeAction(g, action);
		}
		/**/
	}
	
	/**
	 * Applies the given graphics action to the given graphics object for this
	 * segment. See classes implementing the GraphicsDraw interface for more
	 * details about specific actions.
	 * 
	 * @param g			The graphics object upon which to act
	 * @param action	The action to apply on g
	 */
	protected void executeAction(PGraphics g, DrawAction action) {
		
		if (action instanceof GLogic) {
			
			if (action == GLogic.RESET_MAT) {
				// Reset the top matrix
				g.resetMatrix();
				
			} else if (action == GLogic.PUSH_MAT) {
				// Push the top matrix
				g.pushMatrix();
				
			} else if (action == GLogic.POP_MAT) {
				// Pop the top matrix
				g.popMatrix();
			}
			
		} else if (action instanceof GDrawModel) {
			// Draw the specified model
			g.shape( ((GDrawModel) action).MODEL );
			
		} else if (action instanceof GDrawOBB) {
			// Draw the shape associated with the given action
			((GDrawOBB) action).OBB.getFrame().draw(g);
			
		} else if (action instanceof GTranslate) {
			GTranslate tAction = (GTranslate)action;
			// Apply the translation associated with the given action
			PVector t = tAction.TRANSLATION;
			g.translate(t.x, t.y, t.z);
			
		} else if (action instanceof GRotateX) {
			/* Rotate around the x-axis the rotation associated with the given
			 * action */
			g.rotateX( ((GRotateX) action).ROTATION.value );
			
		} else if (action instanceof GRotateY) {
			/* Rotate around the y-axis the rotation associated with the given
			 * action */
			g.rotateY( ((GRotateY) action).ROTATION.value );
			
		} else if (action instanceof GRotateZ) {
			/* Rotate around the z-axis the rotation associated with the given
			 * action */
			g.rotateZ( ((GRotateZ) action).ROTATION.value );
			
		} else {
			// Invalid action
			System.err.printf("Invalid graphics action: %s!\n",
					action.getClass());
		}
		
	}
	
	/**
	 * Reset the color of the segment's bounding boxes to the default OBB
	 * color.
	 */
	public void resetOBBColors() {
		for (BoundingBox obb : OBBS) {
			obb.setColor(Fields.OBB_DEFAULT);
		}
	}
}

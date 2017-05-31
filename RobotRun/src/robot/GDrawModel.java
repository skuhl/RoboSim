package robot;

import geom.MyPShape;

/**
 * Defines a model to be drawn by a graphics object.
 * 
 * @author Joshua Hooker
 */
public class GDrawModel {
	public final MyPShape MODEL;
	
	public GDrawModel(MyPShape model) {
		this.MODEL = model;
	}
}

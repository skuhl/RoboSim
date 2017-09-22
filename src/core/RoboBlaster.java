package core;

import java.util.ArrayList;

import geom.RQuaternion;
import processing.core.PGraphics;
import processing.core.PVector;

public class RoboBlaster {
	private RQuaternion orient;
	private PVector pos;
	private ArrayList<Projectile> projectilesInFlight;
	
	public RoboBlaster(PVector p, RQuaternion o) {
		pos = p;
		orient = o;
		projectilesInFlight = new ArrayList<Projectile>();
	}
	
	public PVector getAimVect() {
		float[][] m = orient.toMatrix().getDataF();
		return new PVector(m[0][0], m[1][0], m[2][0]);
	}
	
	public void setOrientation(RQuaternion q) {
		orient = q;
	}
	
	public void setPosition(PVector p) {
		pos = p;
	}
	
	public void shoot() {
		System.out.println("pew!");
		projectilesInFlight.add(new Projectile(pos.copy(), getAimVect()));
	}
	
	public void updateAndDrawProjectiles(PGraphics g) {
		for(int i = 0; i < projectilesInFlight.size(); i += 1) {
			PVector pPos = projectilesInFlight.get(i).updatePos(10f);
			
			if(pPos.copy().sub(pos).mag() > 2000) {
				projectilesInFlight.remove(i);
			}
			else {
				g.pushMatrix();
				g.stroke(0);
				g.translate(pPos.x, pPos.y, pPos.z);
				g.sphere(3);
				g.popMatrix();
			}
		}
	}
}

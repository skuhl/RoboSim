package core;

import java.util.ArrayList;

import geom.RQuaternion;
import processing.core.PVector;

public class RoboBlaster {
	private PVector pos;
	private RQuaternion orient;
	private ArrayList<Projectile> projectilesInFlight;
	
	public RoboBlaster(PVector p, RQuaternion o) {
		pos = p;
		orient = o;
		projectilesInFlight = new ArrayList<Projectile>();
	}
	
	public void setPosition(PVector p) {
		pos = p;
	}
	
	public void setOrientation(RQuaternion q) {
		orient = q;
	}
	
	public void shoot() {
		System.out.println("pew!");
		projectilesInFlight.add(new Projectile(pos.copy(), getAimVect()));
	}
	
	public PVector getAimVect() {
		float[][] m = orient.toMatrix().getDataF();
		return new PVector(m[0][0], m[1][0], m[2][0]);
	}
	
	public void updateAndDrawProjectiles() {
		for(int i = 0; i < projectilesInFlight.size(); i += 1) {
			PVector pPos = projectilesInFlight.get(i).updatePos(10f);
			
			if(pPos.copy().sub(pos).mag() > 2000) {
				projectilesInFlight.remove(i);
			}
			else {
				RobotRun.getInstance().pushMatrix();
				RobotRun.getInstance().stroke(0);
				RobotRun.getInstance().translate(pPos.x, pPos.y, pPos.z);
				RobotRun.getInstance().sphere(3);
				RobotRun.getInstance().popMatrix();
			}
		}
	}
}

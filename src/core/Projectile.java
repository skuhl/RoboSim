package core;

import processing.core.PVector;

class Projectile {
	PVector projDir;
	PVector projPos;
	
	Projectile(PVector p, PVector t) {
		projPos = p;
		projDir = t;
	}
	
	PVector updatePos(float spd) {
		projPos = projPos.add(projDir.copy().mult(spd));
		return projPos.copy();
	}
}